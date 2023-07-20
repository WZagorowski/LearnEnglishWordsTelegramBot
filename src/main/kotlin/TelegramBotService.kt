import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(
    private val botToken: String,
    val json: Json = Json { ignoreUnknownKeys = true },
) {
    private val client: HttpClient = HttpClient.newBuilder().build()

    companion object {
        const val apiTelegramLink = "https://api.telegram.org"
    }

    fun getUpdates(updateId: Long): String {
        val urlGetUpdates = "$apiTelegramLink/bot$botToken/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(chatId: Long, message: String): String {
        val sendMessage = "$apiTelegramLink/bot$botToken/SendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = message,
        )
        val requestBodyString = json.encodeToString(requestBody)
        return getPostResponse(sendMessage, requestBodyString).body()
    }

    private fun sendAndDeleteMessage(chatId: Long, messageId: Long, message: String): String {
        val sendMessage = "$apiTelegramLink/bot$botToken/editMessageText"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            messageId = messageId,
            text = message,
        )
        val requestBodyString = json.encodeToString(requestBody)
        return getPostResponse(sendMessage, requestBodyString).body()
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
        return getPostResponse(sendMessage, requestBodyString).body()
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
        return getPostResponse(urlSendMessage, requestBodyString).body()
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
        return getPostResponse(urlEditMessage, requestBodyString).body()
    }

    private fun getPostResponse(url: String, requestBodyString: String): HttpResponse<String> {
        val postRequest: HttpRequest = HttpRequest.newBuilder().uri(URI.create(url))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        return client.send(postRequest, HttpResponse.BodyHandlers.ofString())
    }
}