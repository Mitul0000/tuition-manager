# ============================================================================
#  TutorDesk - R8 rules   (AGP 9.1, R8 full mode, minSdk 24)
# ============================================================================
# Base rules come from "proguard-android-optimize.txt" (set in build.gradle.kts).
#
# Retrofit, OkHttp, Firebase, Lottie, Compose and Navigation ship their own
# "consumer rules" inside their AARs, so they are intentionally NOT repeated
# here. Do not add blanket rules like  -keep class com.google.firebase.** { *; }
# They only make the app bigger and hide real problems.
#
# Rule of thumb: only code that is reached through REFLECTION (looked up by
# name at runtime) needs a keep rule. R8 cannot see those calls, so it would
# delete or rename that code.


# ----------------------------------------------------------------------------
# 1. Readable crash reports
#    Keep line numbers, hide the real source file name. Upload the mapping file
#    (app/build/outputs/mapping/release/mapping.txt) to Play Console so crash
#    stack traces are de-obfuscated.
# ----------------------------------------------------------------------------
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile


# ----------------------------------------------------------------------------
# 2. Metadata that is read at runtime
#    Signature   = generic types, e.g. List<McqQuestion>, Response<T>
#    Annotations = Retrofit's @POST/@Body/@Part and Gson/Firestore annotations
# ----------------------------------------------------------------------------
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault


# ----------------------------------------------------------------------------
# 3. Data models: Gson (Retrofit JSON) and Firestore (toObject / set)
#    Both match JSON keys / Firestore fields to your field and getter NAMES, so
#    the names must not be renamed or removed.
#
#    Also keeps the no-arg constructor. Kotlin generates it when every
#    constructor parameter has a default value, but your own code never calls
#    it, so R8 would delete it. Without it Gson falls back to Unsafe, which
#    skips the defaults (emptyList(), "", 0). A missing JSON array then becomes
#    null in a non-null List and the app crashes - only in release builds.
# ----------------------------------------------------------------------------
-keep class com.digifello.tutordesk.data.model.** { *; }

# Request bodies sent with @Body (declared next to the Retrofit interface)
-keep class com.digifello.tutordesk.data.remote.RegenerateRequestBody { *; }
-keep class com.digifello.tutordesk.data.remote.DeleteSessionRequestBody { *; }


# ----------------------------------------------------------------------------
# 4. ViewModels created with viewModel()
#    The default factory finds the constructor by reflection:
#      - LoginViewModel(), HomeViewModel() ...  -> no-arg constructor
#        (Kotlin makes it because all parameters have defaults)
#      - QuestionGeneratorViewModel(Application) -> made by @JvmOverloads
#    BatchDetailsViewModel uses its own factory (direct call), so it is fine.
# ----------------------------------------------------------------------------
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>();
}
-keepclassmembers class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}


# ----------------------------------------------------------------------------
# 5. EncryptedSharedPreferences  (androidx.security-crypto -> Tink -> protobuf)
#    Tink's protobuf messages read their own fields by reflection.
# ----------------------------------------------------------------------------
-keepclassmembers class * extends com.google.crypto.tink.shaded.protobuf.GeneratedMessageLite {
    <fields>;
}

# Optional classes Tink mentions but the app never loads. This only silences
# R8's "Missing class" error for them.
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-dontwarn com.google.api.client.**
-dontwarn org.joda.time.**


# ----------------------------------------------------------------------------
# 6. EMERGENCY SWITCHES (all off by default)
#    If a release build behaves differently from debug and you cannot find the
#    cause quickly, uncomment ONE line, rebuild, and test again. Then use
#    "adb logcat" to find the missing class/field and add a proper keep rule.
#
#    -dontoptimize     # no code optimization (still shrinks + renames)
#    -dontobfuscate    # no renaming (still shrinks + optimizes)
#
#    Last resort: set isMinifyEnabled = false in build.gradle.kts to ship the
#    app exactly as in debug (bigger and easier to reverse engineer).
# ----------------------------------------------------------------------------