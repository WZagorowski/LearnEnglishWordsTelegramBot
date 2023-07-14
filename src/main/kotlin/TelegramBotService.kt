import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(private val botToken: String) {

    fun getUpdates(updateId: Long): String {

        val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(json: Json, chatId: Long, message: String): String {

        val sendMessage = "https://api.telegram.org/bot$botToken/SendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = message,
        )
        val requestBodyString = json.encodeToString(requestBody)

        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu(json: Json, chatId: Long): String {

        val sendMessage = "https://api.telegram.org/bot$botToken/SendMessage"
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

        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendQuestion(json: Json, chatId: Long, question: Question): String {

        val urlSendMessage = "https://api.telegram.org/bot$botToken/SendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = question.correctAnswer.original,
            replyMarkup = ReplyMarkup(
                question.variants.mapIndexed { index, word ->
                    listOf(
                        InlineKeyboard(text = word.translate, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index")
                    )
                }.plus(
                    listOf(
                        listOf(
                            InlineKeyboard(text = "Вернуться в меню", callbackData = START)
                        )
                    )
                )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)

        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }
}