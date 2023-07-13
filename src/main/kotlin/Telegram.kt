import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String? = null,
    @SerialName("message")
    val message: Message? = null,
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboard>>,
)

@Serializable
data class InlineKeyboard(
    @SerialName("callback_data")
    val callbackData: String,
    @SerialName("text")
    val text: String,
)

fun main(args: Array<String>) {

    val botToken = args[0]
    val service = TelegramBotService(botToken)
    val trainer = LearnWordsTrainer(
        learnedCount = 3,
        numberOfAnswers = 4,
        fileName = "words.txt",
    )
    var lastUpdateId = 0L

    val json = Json {
        ignoreUnknownKeys = true
    }

    while (true) {
        Thread.sleep(2000)
        val responseString: String = service.getUpdates(lastUpdateId)
        println(responseString)

        val response: Response = json.decodeFromString(responseString)
        val updates = response.result
        val firstUpdate = updates.firstOrNull() ?: continue
        val updateId = firstUpdate.updateId
        lastUpdateId = updateId + 1

        val message = firstUpdate.message?.text
        val chatId = firstUpdate.message?.chat?.id ?: firstUpdate.callbackQuery?.message?.chat?.id
        val data = firstUpdate.callbackQuery?.data

        if (message?.lowercase() == "/start" && chatId != null) {
            service.sendMenu(json, chatId)
        }
        if (data?.lowercase() == STATISTICS && chatId != null) {
            val statistics = trainer.getStatistics()
            service.sendMessage(
                json,
                chatId,
                "Выучено ${statistics.learned} из ${statistics.total} слов | ${statistics.percent}%"
            )
        }

        if (data?.lowercase() == LEARNING && chatId != null)
            checkNextQuestionAndSend(json, service, trainer, chatId)

        if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true && chatId != null) {
            val answerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            if (trainer.checkAnswer(answerIndex)) {
                service.sendMessage(
                    json,
                    chatId,
                    "Правильно!",
                    )
            } else {
                val rightQuestion = trainer.question?.correctAnswer
                service.sendMessage(
                    json,
                    chatId,
                    "Не правильно: ${rightQuestion?.original} - ${rightQuestion?.translate}",
                )
            }
            checkNextQuestionAndSend(json, service, trainer, chatId)
        }
    }
}

fun checkNextQuestionAndSend(json: Json, service: TelegramBotService, trainer: LearnWordsTrainer, chatId: Long) {
    val question = trainer.getNextQuestion()

    if (question == null) {
        service.sendMessage(json, chatId, "Вы выучили все слова в базе")
    } else {
        service.sendQuestion(json, chatId, question)
    }
}

const val LEARNING = "learn_words_clicked"
const val STATISTICS = "statistics_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"