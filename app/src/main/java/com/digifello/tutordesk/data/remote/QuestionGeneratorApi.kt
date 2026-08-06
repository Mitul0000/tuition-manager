package com.digifello.tutordesk.data.remote


import com.digifello.tutordesk.data.model.DeleteSessionResponse
import com.digifello.tutordesk.data.model.GenerateQuestionResponse
import com.digifello.tutordesk.data.model.GenerateRequirements
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import com.digifello.tutordesk.data.model.GeneratePaperRequest
import com.digifello.tutordesk.data.model.MyPapersResponse
import com.digifello.tutordesk.data.model.SavePaperRequest
import com.digifello.tutordesk.data.model.SavePaperResponse
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming

interface QuestionGeneratorApi {

    @Multipart
    @POST("api/generate")
    suspend fun generateQuestions(
        @Part files: List<MultipartBody.Part>,
        @Part("requirements") requirements: RequestBody,
        @Part("api_key") apiKey: RequestBody
    ): Response<GenerateQuestionResponse>

    @POST("api/regenerate")
    suspend fun regenerateQuestions(
        @Body body: RegenerateRequestBody
    ): Response<GenerateQuestionResponse>

    @HTTP(method = "DELETE", path = "api/session", hasBody = true)
    suspend fun deleteSession(
        @Body body: DeleteSessionRequestBody
    ): Response<DeleteSessionResponse>

    @Streaming
    @POST("api/generate-pdf")
    suspend fun generatePdf(
        @Body body: GeneratePaperRequest
    ): Response<ResponseBody>

    @Streaming
    @POST("api/generate-answersheet")
    suspend fun generateAnswerSheet(
        @Body body: GeneratePaperRequest
    ): Response<ResponseBody>

    @POST("api/papers")
    suspend fun savePaper(
        @Body body: SavePaperRequest
    ): Response<SavePaperResponse>

    @GET("api/papers")
    suspend fun getMyPapers(): Response<MyPapersResponse>
}

data class RegenerateRequestBody(
    val sessionId: String,
    val requirements: GenerateRequirements,
    val api_key: String
)

data class DeleteSessionRequestBody(
    val sessionId: String
)
