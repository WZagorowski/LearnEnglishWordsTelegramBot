import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    @SerialName("ok")
    var isOk: Boolean,
    @SerialName("result")
    val result: List<Update>,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String? = null,
    @SerialName("chat")
    val chat: Chat,
    @SerialName("message_id")
    val messageId: Long,
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
    val text: String? = null,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class SendPhotoRequest(
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("photo")
    val urlPhoto: String,
    @SerialName("caption")
    val caption: String,
    @SerialName("has_spoiler")
    val isHasSpoiler: Boolean,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup,
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

@Serializable
data class PhotoResponse(
    @SerialName("items")
    val searchItems: List<Item>,
)

@Serializable
data class Item(
    @SerialName("link")
    val link: String,
)

fun main(args: Array<String>) {

    val botToken = args[0]
    val searchKey = args[1]

    val botService = TelegramBotService(botToken = botToken)
    val googleService = GoogleCloudService(searchKey = searchKey)

    var lastUpdateId = 0L
    val trainers = HashMap<Long, LearnWordsTrainer>()

    while (true) {
        Thread.sleep(2000)
        val result = runCatching { botService.getUpdates(lastUpdateId) }
        val responseString: String = result.getOrNull() ?: continue
        println(responseString)

        val response: Response = botService.json.decodeFromString(responseString)
        if (!response.isOk) {
            Thread.sleep(5000)
            continue
        }
        if (response.result.isEmpty()) continue

        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, trainers, botService, googleService) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

fun handleUpdate(
    update: Update,
    trainers: HashMap<Long, LearnWordsTrainer>,
    botService: TelegramBotService,
    googleService: GoogleCloudService,
) {
    val message = update.message?.text
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val messageId = update.message?.messageId ?: update.callbackQuery?.message?.messageId ?: return
    val data = update.callbackQuery?.data
    val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

    if (message?.lowercase() == "/start") {
        botService.sendMenu(chatId)
    }
    if (data?.lowercase() == STATISTICS) {
        val statistics = trainer.getStatistics()
        botService.sendMessage(
            chatId,
            "Выучено ${statistics.learned} из ${statistics.total} слов | ${statistics.percent}%"
        )
    }
    if (data?.lowercase() == RESET_CLICKED) {
        trainer.resetProgress()
        botService.sendMessage(chatId, "Прогресс сброшен")
    }
    if (data?.lowercase() == LEARNING) {
        checkNextQuestionAndSend(trainer, botService, googleService, chatId, "Выбери правильный перевод")
    }
    if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
        val answerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
        try {
            botService.deleteMessage(chatId, messageId)
            botService.deleteMessage(chatId, messageId + 1)
        } catch (e: Exception) {
            botService.sendMessage(chatId, "Из-за ограничений телеграмма рекомендуется" +
                    " очистить историю сообщений и снова запустить бота. Статистика при этом не пострадает.")
        } finally {
            if (trainer.checkAnswer(answerIndex)) {
                checkNextQuestionAndSend(
                    trainer,
                    botService,
                    googleService,
                    chatId,
                    "Правильно!",
                )
            } else {
                val rightQuestion = trainer.question?.correctAnswer
                checkNextQuestionAndSend(
                    trainer,
                    botService,
                    googleService,
                    chatId,
                    "Не правильно!\n${rightQuestion?.original} - ${rightQuestion?.translate}",
                )
            }
        }
    }
}

fun checkNextQuestionAndSend(
    trainer: LearnWordsTrainer,
    botService: TelegramBotService,
    googleService: GoogleCloudService,
    chatId: Long,
    text: String,
) {
    val question = trainer.getNextQuestion()

    if (question == null) {
        botService.sendMessage(chatId, "Вы выучили все слова в базе")
    } else {
        val photoItem = googleService.getPhotoLinks(question.correctAnswer.original)
        botService.sendPhoto(chatId, photoItem, question, text)

        val audioFile = googleService.getAudioFile(question.correctAnswer.original)
        botService.sendAudio(chatId, question, audioFile)
    }
}

const val LEARNING = "learn_words_clicked"
const val STATISTICS = "statistics_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val RESET_CLICKED = "reset_clicked"