package com.digifello.tutordesk.data.repository

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.digifello.tutordesk.data.model.ApiErrorResponse
import com.digifello.tutordesk.data.model.DeleteSessionResponse
import com.digifello.tutordesk.data.model.GeneratePaperRequest
import com.digifello.tutordesk.data.model.GenerateQuestionResponse
import com.digifello.tutordesk.data.model.GenerateRequirements
import com.digifello.tutordesk.data.model.MyPapersResponse
import com.digifello.tutordesk.data.model.PaperMetadata
import com.digifello.tutordesk.data.model.SavePaperRequest
import com.digifello.tutordesk.data.model.SavePaperResponse
import com.digifello.tutordesk.data.model.SelectedFile
import com.digifello.tutordesk.data.model.SelectedQuestionsPayload
import com.digifello.tutordesk.data.remote.DeleteSessionRequestBody
import com.digifello.tutordesk.data.remote.QuestionGeneratorApi
import com.digifello.tutordesk.data.remote.RegenerateRequestBody
import com.digifello.tutordesk.data.remote.RetrofitClient
import com.digifello.tutordesk.util.NotificationHelper
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
class QuestionGeneratorRepository(
    private val api: QuestionGeneratorApi = RetrofitClient.instance.create(QuestionGeneratorApi::class.java),
    private val gson: Gson = Gson()
) {

    suspend fun generateQuestions(
        contentResolver: ContentResolver,
        files: List<SelectedFile>,
        requirements: GenerateRequirements,
        apiKey: String
    ): GenerateQuestionResponse = withContext(Dispatchers.IO) {
        val fileParts = files.map { it.toMultipartPart(contentResolver) }
        val requirementsBody = gson.toJson(requirements).toRequestBody("application/json".toMediaTypeOrNull())
        val apiKeyBody = apiKey.toRequestBody("text/plain".toMediaTypeOrNull())

        val response = api.generateQuestions(fileParts, requirementsBody, apiKeyBody)
        if (!response.isSuccessful) throw Exception(parseError(response.errorBody()?.string()))
        response.body() ?: throw Exception("Empty response from server")
    }

    suspend fun regenerateQuestions(
        sessionId: String,
        requirements: GenerateRequirements,
        apiKey: String
    ): GenerateQuestionResponse = withContext(Dispatchers.IO) {
        val response = api.regenerateQuestions(RegenerateRequestBody(sessionId, requirements, apiKey))
        if (!response.isSuccessful) throw Exception(parseError(response.errorBody()?.string()))
        response.body() ?: throw Exception("Empty response from server")
    }

    suspend fun deleteSession(sessionId: String): DeleteSessionResponse = withContext(Dispatchers.IO) {
        val response = api.deleteSession(DeleteSessionRequestBody(sessionId))
        response.body() ?: DeleteSessionResponse()
    }

    suspend fun generatePdf(
        context: Context,
        selectedQuestions: SelectedQuestionsPayload,
        metadata: PaperMetadata,
        fileName: String = "question-paper.pdf"
    ): Uri = withContext(Dispatchers.IO) {
        val response = api.generatePdf(GeneratePaperRequest(selectedQuestions, metadata))
        if (!response.isSuccessful) throw Exception(parseError(response.errorBody()?.string()))
        val body = response.body() ?: throw Exception("Empty PDF response from server")
        val uri = savePdfToDownloads(context, body.bytes(), fileName)
        NotificationHelper.showDownloadComplete(context, fileName, uri)
        uri
    }

    suspend fun generateAnswerSheet(
        context: Context,
        selectedQuestions: SelectedQuestionsPayload,
        metadata: PaperMetadata,
        fileName: String = "answer-key.pdf"
    ): Uri = withContext(Dispatchers.IO) {
        val response = api.generateAnswerSheet(GeneratePaperRequest(selectedQuestions, metadata))
        if (!response.isSuccessful) throw Exception(parseError(response.errorBody()?.string()))
        val body = response.body() ?: throw Exception("Empty PDF response from server")
        val uri = savePdfToDownloads(context, body.bytes(), fileName)
        NotificationHelper.showDownloadComplete(context, fileName, uri)
        uri
    }

    suspend fun savePaper(
        title: String,
        metadata: PaperMetadata,
        selectedQuestions: SelectedQuestionsPayload
    ): SavePaperResponse = withContext(Dispatchers.IO) {
        val response = api.savePaper(SavePaperRequest(title, metadata, selectedQuestions))
        if (!response.isSuccessful) throw Exception(parseError(response.errorBody()?.string()))
        response.body() ?: throw Exception("Empty response from server")
    }

    suspend fun getMyPapers(): MyPapersResponse = withContext(Dispatchers.IO) {
        val response = api.getMyPapers()
        if (!response.isSuccessful) throw Exception(parseError(response.errorBody()?.string()))
        response.body() ?: MyPapersResponse()
    }

    /** Writes PDF bytes to the device's public Downloads folder and returns a display path/URI string. */
    private fun savePdfToDownloads(context: Context, bytes: ByteArray, fileName: String): Uri {
        val uniqueName = uniqueFileName(fileName)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, uniqueName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw Exception("Could not create file in Downloads")
            resolver.openOutputStream(uri)?.use { it.write(bytes) }
                ?: throw Exception("Could not write file")
            uri
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val file = File(downloadsDir, uniqueName)
            file.writeBytes(bytes)
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }
    }
    private fun uniqueFileName(fileName: String): String {
        val dot = fileName.lastIndexOf('.')
        val base = if (dot != -1) fileName.substring(0, dot) else fileName
        val ext = if (dot != -1) fileName.substring(dot) else ""
        val stamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
        return "${base}_$stamp$ext"
    }
    private fun parseError(raw: String?): String {
        if (raw.isNullOrBlank()) return "Something went wrong"
        return try {
            gson.fromJson(raw, ApiErrorResponse::class.java)?.let { it.error ?: it.message } ?: raw
        } catch (e: Exception) {
            raw
        }
    }
}

private fun SelectedFile.toMultipartPart(resolver: ContentResolver): MultipartBody.Part {
    val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
        ?: throw Exception("Could not read file: $name")
    val mediaType = (mimeType ?: "application/octet-stream").toMediaTypeOrNull()
    return MultipartBody.Part.createFormData("files", name, bytes.toRequestBody(mediaType))
}