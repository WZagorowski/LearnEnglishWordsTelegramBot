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
    val service = TelegramBotService(
        botToken = botToken,
        json = Json { ignoreUnknownKeys = true },
    )
    var lastUpdateId = 0L
    val trainers = HashMap<Long, LearnWordsTrainer>()

    while (true) {
        Thread.sleep(2000)
        val responseString: String = service.getUpdates(lastUpdateId)
        println(responseString)

        val response: Response = service.json.decodeFromString(responseString)
        if (response.result.isEmpty()) continue

        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, trainers, service) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

fun handleUpdate(update: Update, trainers: HashMap<Long, LearnWordsTrainer>, service: TelegramBotService) {

    val message = update.message?.text
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val data = update.callbackQuery?.data
    val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

    if (message?.lowercase() == START || data?.lowercase() == START) {
        service.sendMenu(chatId)
    }
    if (data?.lowercase() == STATISTICS) {
        val statistics = trainer.getStatistics()
        service.sendMessage(
            chatId,
            "Выучено ${statistics.learned} из ${statistics.total} слов | ${statistics.percent}%"
        )
    }
    if (data?.lowercase() == RESET_CLICKED) {
        trainer.resetProgress()
        service.sendMessage(chatId, "Прогресс сброшен")
    }
    if (data?.lowercase() == LEARNING) {
        checkNextQuestionAndSend(trainer, service, chatId)
    }
    if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
        val answerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()

        if (trainer.checkAnswer(answerIndex)) {
            service.sendMessage(
                chatId,
                "Правильно!",
            )
        } else {
            val rightQuestion = trainer.question?.correctAnswer
            service.sendMessage(
                chatId,
                "Не правильно: ${rightQuestion?.original} - ${rightQuestion?.translate}",
            )
        }
        checkNextQuestionAndSend(trainer, service, chatId)
    }
}

fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, service: TelegramBotService, chatId: Long) {
    val question = trainer.getNextQuestion()

    if (question == null) {
        service.sendMessage(chatId, "Вы выучили все слова в базе")
    } else {
        service.sendQuestion(chatId, question)
    }
}

const val LEARNING = "learn_words_clicked"
const val STATISTICS = "statistics_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val RESET_CLICKED = "reset_clicked"
const val START = "/start"