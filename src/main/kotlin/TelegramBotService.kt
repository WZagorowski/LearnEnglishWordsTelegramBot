import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class TelegramBotService(
    private val botToken: String,
    val json: Json = Json { ignoreUnknownKeys = true },
) {
    private val client = OkHttpClient()

    companion object {
        const val apiTelegramLink = "https://api.telegram.org"
    }

    fun getUpdates(updateId: Long): String {
        val urlGetUpdates = "$apiTelegramLink/bot$botToken/getUpdates?offset=$updateId"
        val request = Request.Builder().url(urlGetUpdates).build()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful)
            throw IOException("Запрос не был успешен: ${response.code} ${response.message}")
        else
            return response.body?.string() ?: throw IllegalStateException("Тело ответа пустое")
    }

    fun sendMessage(chatId: Long, message: String): String {
        val sendMessage = "$apiTelegramLink/bot$botToken/SendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = message,
        )
        val requestBodyString = json.encodeToString(requestBody)
        return getPostResponse(sendMessage, requestBodyString)
    }

    fun sendMenu(chatId: Long): String {
        val sendMessage = "$apiTelegramLink/bot$botToken/SendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyboard(text = "Изучать слова", callbackData = LEARNING),
                        InlineKeyboard(text = "Статистика", callbackData = STATISTICS),
                    ),
                    listOf(
                        InlineKeyboard(text = "Сбросить прогресс", callbackData = RESET_CLICKED),
                    )
                )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)
        return getPostResponse(sendMessage, requestBodyString)
    }

    fun sendPhoto(chatId: Long, urlPhoto: String, question: Question, text: String): String {
        val urlSendPhoto = "$apiTelegramLink/bot$botToken/sendPhoto"
        var responseString: String? = null
        var url = urlPhoto
        var count = 1
        while (responseString.isNullOrEmpty()) {
            val requestBody = SendPhotoRequest(
                chatId = chatId,
                urlPhoto = url,
                caption = "$text\n\nСледующее слово:\n${question.correctAnswer.original}",
                isHasSpoiler = true,
                replyMarkup = ReplyMarkup(
                    question.variants.mapIndexed { index, word ->
                        listOf(
                            InlineKeyboard(text = word.translate, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index")
                        )
                    }
                )
            )
            val requestBodyString = json.encodeToString(requestBody)
            val body = requestBodyString.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().url(urlSendPhoto).post(body).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                if (count-- > 0) {
                    url = "http://surl.li/jnglg"
                    continue
                } else {
                    throw IOException("Запрос не был успешен: ${response.code} ${response.message}")
                }
            } else
                responseString = response.body?.string() ?: throw IllegalStateException("Тело ответа пустое")
        }
        return responseString
    }

    fun deleteMessage(chatId: Long, messageId: Long): String {
        val urlDeleteMessage = "$apiTelegramLink/bot$botToken/deleteMessage?chat_id=$chatId&message_id=$messageId"
        val request = Request.Builder().url(urlDeleteMessage).build()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful)
            throw IOException("Запрос не был успешен: ${response.code} ${response.message}")
        else
            return response.body?.string() ?: throw IllegalStateException("Тело ответа пустое")
    }

    private fun getPostResponse(url: String, requestBodyString: String): String {
        val body = requestBodyString.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder().url(url).post(body).build()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful)
            throw IOException("Запрос не был успешен: ${response.code} ${response.message}")
        else
            return response.body?.string() ?: throw IllegalStateException("Тело ответа пустое")
    }
}