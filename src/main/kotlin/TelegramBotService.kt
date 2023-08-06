import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

class TelegramBotService(
    private val botToken: String,
    val json: Json = Json { ignoreUnknownKeys = true },
) {
    private val client = OkHttpClient()

    companion object {
        const val API_TELEGRAM = "https://api.telegram.org"
    }

    fun getUpdates(updateId: Long): String {
        val urlGetUpdates = "$API_TELEGRAM/bot$botToken/getUpdates?offset=$updateId"
        val request = Request.Builder().url(urlGetUpdates).build()
        return getResponse(request)
    }

    fun sendMessage(chatId: Long, message: String): String {
        val urlSendMessage = "$API_TELEGRAM/bot$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = message,
        )
        val requestBodyString = json.encodeToString(requestBody)
        val body = requestBodyString.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder().url(urlSendMessage).post(body).build()
        return getResponse(request)
    }

    fun sendMenu(chatId: Long): String {
        val urlSendMessage = "$API_TELEGRAM/bot$botToken/sendMessage"
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
        val body = requestBodyString.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder().url(urlSendMessage).post(body).build()
        return getResponse(request)
    }

    fun sendPhoto(chatId: Long, urlPhoto: String, urlPhotoReserve: String, question: Question, text: String): String {
        val urlSendPhoto = "$API_TELEGRAM/bot$botToken/sendPhoto"
        var responseString: String? = null
        var url = urlPhoto
        var count = 1
        while (responseString.isNullOrEmpty()) {
            val requestBody = SendPhotoRequest(
                chatId = chatId,
                urlPhoto = url,
                caption = "$text\n\nСледующее слово: ${question.correctAnswer.original}",
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
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    if (count-- > 0) {
                        url = urlPhotoReserve
                    } else {
                        throw IOException("Request failed: ${response.code} ${response.message}")
                    }
                } else
                    responseString = response.body?.string()
            }
            if (responseString.isNullOrEmpty()) continue
        }
        return responseString ?: throw IllegalStateException("The response body is empty")
    }

    fun sendAudio(chatId: Long, question: Question, file: File): String {
        val urlSendAudio = "$API_TELEGRAM/bot$botToken/sendAudio"
        val contentType = "audio/mpeg".toMediaTypeOrNull()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("chat_id", chatId.toString())
            .addFormDataPart("performer", "Воспроизвести слово")
            .addFormDataPart("title", question.correctAnswer.original)
            .addFormDataPart("audio", file.name, file.readBytes().toRequestBody(contentType))
            .build()

        val request = Request.Builder().url(urlSendAudio).post(requestBody).build()
        return getResponse(request)
    }

    fun deleteMessage(chatId: Long, messageId: Long): String {
        val urlDeleteMessage = "$API_TELEGRAM/bot$botToken/deleteMessage?chat_id=$chatId&message_id=$messageId"
        val request = Request.Builder().url(urlDeleteMessage).build()
        return getResponse(request)
    }

    private fun getResponse(request: Request): String {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Request failed: ${response.code} ${response.message}")
            } else {
                return response.body?.string() ?: throw IllegalStateException("The response body is empty")
            }
        }
    }
}