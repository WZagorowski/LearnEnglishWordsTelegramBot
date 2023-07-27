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

    private fun sendAndDeleteMessage(chatId: Long, messageId: Long, message: String): String {
        val sendMessage = "$apiTelegramLink/bot$botToken/editMessageText"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            messageId = messageId,
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

    fun sendFirstQuestion(chatId: Long, question: Question): String {
        val urlSendMessage = "$apiTelegramLink/bot$botToken/SendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Выбери правильный перевод слова:\n\n${question.correctAnswer.original}",
            replyMarkup = ReplyMarkup(
                question.variants.mapIndexed { index, word ->
                    listOf(
                        InlineKeyboard(text = word.translate, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index")
                    )
                }
            )
        )
        val requestBodyString = json.encodeToString(requestBody)
        return getPostResponse(urlSendMessage, requestBodyString)
    }

    fun editAndSendQuestion(chatId: Long, messageId: Long, question: Question, text: String): String {
        val urlEditMessage = "$apiTelegramLink/bot$botToken/editMessageReplyMarkup"
        sendAndDeleteMessage(chatId, messageId, "$text\n\n${question.correctAnswer.original}")
        val requestBody = SendMessageRequest(
            chatId = chatId,
            messageId = messageId,
            replyMarkup = ReplyMarkup(
                question.variants.mapIndexed { index, word ->
                    listOf(
                        InlineKeyboard(text = word.translate, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index")
                    )
                }
            )
        )
        val requestBodyString = json.encodeToString(requestBody)
        return getPostResponse(urlEditMessage, requestBodyString)
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