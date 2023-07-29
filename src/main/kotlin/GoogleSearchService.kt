import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException


class GoogleSearchService(
    val json: Json = Json { ignoreUnknownKeys = true },
) {

    companion object {
        const val key = "?key=AIzaSyDIf4uMqXbvHeRUYgEPscnfTqyQVB0gpcA"
        const val searchSystem = "e3fd0b8afbd7746dd"
        const val apiGoogle = "https://www.googleapis.com/customsearch/v1"
    }

    fun getImageLink(word: String): String {
        val client = OkHttpClient()
        val urlGetImage = "$apiGoogle$key&cx=$searchSystem&q=$word&searchType=image&num=1&imgType=clipart"
        val request = Request.Builder().url(urlGetImage).build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful)
            throw IOException("Запрос не был успешен: ${response.code} ${response.message}")
        else
            return response.body?.string() ?: throw IllegalStateException("Тело ответа пустое")
    }
}