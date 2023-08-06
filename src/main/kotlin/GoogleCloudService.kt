import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.texttospeech.v1.*
import java.io.File

class GoogleCloudService(
    private val searchKey: String,
    private val mediaFolderPath: String = "C:/Users/vladi/IdeaProjects/LearnEnglishWordsTelegramBot/",
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    private val photoClient = OkHttpClient()
    companion object {
        const val API_GOOGLE = "https://www.googleapis.com/customsearch/v1"
        const val SEARCH_SETTINGS = "&searchType=image&imgType=clipart&imgColorType=color&num=2"
        const val TTS_KEY_FILENAME = "googlespeech.json"
    }

    fun getPhotoItems(text: String): PhotoResponse {
        val urlGetImage = "$API_GOOGLE?key=$searchKey&cx=e3fd0b8afbd7746dd&q=$text$SEARCH_SETTINGS"
        val request = Request.Builder().url(urlGetImage).build()
        photoClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Request failed: ${response.code} ${response.message}")
            } else {
                return response.body?.string()
                    ?.let { json.decodeFromString(it) }
                    ?: throw IllegalStateException("The response body is empty")
            }
        }
    }

    fun getAudioFile(chatId: Long, text: String): File {
        val googleKeyStream = File(mediaFolderPath, TTS_KEY_FILENAME).inputStream()
        val credentials = GoogleCredentials.fromStream(googleKeyStream)
        val ttsSettings = TextToSpeechSettings.newBuilder()
            .setCredentialsProvider { credentials }
            .build()

        TextToSpeechClient.create(ttsSettings).use { speechClient ->
            val input = SynthesisInput.newBuilder().setText(text).build()
            val voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode("en-US")
                .setSsmlGender(SsmlVoiceGender.MALE)
                .build()
            val audioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build()
            val response = speechClient.synthesizeSpeech(input, voice, audioConfig)
                ?: throw IllegalStateException("The response body is empty")

            val responseByteArray = response.audioContent.toByteArray()
            val audioFile = File(mediaFolderPath, "audio$chatId.mp3")
            audioFile.outputStream().use { fileOutputStream ->
                fileOutputStream.write(responseByteArray)
            }
            return audioFile
        }
    }
}