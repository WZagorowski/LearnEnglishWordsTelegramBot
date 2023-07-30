import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.texttospeech.v1.*
import java.io.File
import java.io.FileInputStream

class GoogleCloudService(
    private val key: String,
    val path: String = "C:/Users/vladi/IdeaProjects/LearnEnglishWordsTelegramBot/",
    val json: Json = Json { ignoreUnknownKeys = true },
) {
    private val photoClient = OkHttpClient()

    companion object {
        const val apiGoogle = "https://www.googleapis.com/customsearch/v1"
        const val searchSettings = "&searchType=image&imgType=clipart&imgColorType=color&num=2"
    }

    fun getPhotoItems(text: String): String {
        val urlGetImage = "$apiGoogle?key=$key&cx=e3fd0b8afbd7746dd&q=$text$searchSettings"
        val request = Request.Builder().url(urlGetImage).build()
        photoClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Запрос не был успешен: ${response.code} ${response.message}")
            } else {
                return response.body?.string() ?: throw IllegalStateException("Тело ответа пустое")
            }
        }
    }

    fun getAudioFile(text: String): SynthesizeSpeechResponse {
        val input = SynthesisInput.newBuilder().setText(text).build()

        val voice = VoiceSelectionParams.newBuilder()
            .setLanguageCode("en-US")
            .setSsmlGender(SsmlVoiceGender.MALE)
            .build()

        val audioConfig = AudioConfig.newBuilder()
            .setAudioEncoding(AudioEncoding.MP3)
            .build()

        val verification = FileInputStream(File("${path}googlespeech.json"))
        val textToSpeechSettings = TextToSpeechSettings.newBuilder()
            .setCredentialsProvider { GoogleCredentials.fromStream(verification) }
            .build()

        val speechClient = TextToSpeechClient.create(textToSpeechSettings)

        val response = speechClient.synthesizeSpeech(input, voice, audioConfig)
            ?: throw IllegalStateException("Тело ответа пустое")
        speechClient.close()
        return response
    }
}