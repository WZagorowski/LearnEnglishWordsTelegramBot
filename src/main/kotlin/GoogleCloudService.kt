import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.texttospeech.v1.*
import java.io.File

data class PhotoItem(
    val originalWord: String,
    val linkOne: String,
    val linkTwo: String,
)

class GoogleCloudService(
    private val filePhotoLinks: String = "photoLinks.txt",
    private val searchKey: String,
    private val mediaFolderPath: String = "C:/Users/vladi/IdeaProjects/LearnEnglishWordsTelegramBot/",
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    private val listPhotoItems = loadPhotoItems()
    private val photoClient = OkHttpClient()

    companion object {
        const val API_GOOGLE = "https://www.googleapis.com/customsearch/v1"
        const val SEARCH_SETTINGS = "&searchType=image&imgType=clipart&imgColorType=color&num=2"
        const val TTS_KEY_FILENAME = "googlespeech.json"
    }

    fun getPhotoLinks(originalWordText: String): PhotoItem {
        val photoItem = listPhotoItems.filter { it.originalWord == originalWordText }
        if (photoItem.isNotEmpty())
            return photoItem[0]
        else {
            val urlGetImage = "$API_GOOGLE?key=$searchKey&cx=e3fd0b8afbd7746dd&q=$originalWordText$SEARCH_SETTINGS"
            val request = Request.Builder().url(urlGetImage).build()

            photoClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Request failed: ${response.code} ${response.message}")
                } else {
                    val photoResponse: PhotoResponse = response.body?.string()
                        ?.let { json.decodeFromString(it) }
                        ?: throw IllegalStateException("The response body is empty")

                    val urlPhotoOne = photoResponse.searchItems[0].link
                    val urlPhotoTwo = photoResponse.searchItems[1].link
                    val newPhotoItem = PhotoItem(originalWordText, urlPhotoOne, urlPhotoTwo)
                    listPhotoItems.add(newPhotoItem)
                    savePhotoItems()

                    return newPhotoItem
                }
            }
        }
    }

    fun getAudioFile(text: String): File {
        val audioFile = File("$mediaFolderPath/audio/", "$text.mp3".replace(" ", ""))
        if (audioFile.exists()) {
            return audioFile
        } else {
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
                audioFile.outputStream().use { fileOutputStream ->
                    fileOutputStream.write(responseByteArray)
                }
                return audioFile
            }
        }
    }

    private fun loadPhotoItems(): MutableList<PhotoItem> {
        try {
            val photoItemsFile = File(mediaFolderPath, filePhotoLinks)
            val listPhotoItems = mutableListOf<PhotoItem>()

            photoItemsFile.readLines().forEach {
                val splitLine = it.split("|")
                listPhotoItems.add(PhotoItem(splitLine[0], splitLine[1], splitLine[2]))
            }
            return listPhotoItems
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("invalid file")
        }
    }

    private fun savePhotoItems() {
        val updatedPhotoItems = File(filePhotoLinks)
        updatedPhotoItems.writeText("")
        for (item in listPhotoItems) {
            updatedPhotoItems.appendText("${item.originalWord}|${item.linkOne}|${item.linkTwo}\n")
        }
    }
}