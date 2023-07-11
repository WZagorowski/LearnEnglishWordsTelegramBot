fun main(args: Array<String>) {

    val botToken = args[0]
    val service = TelegramBotService(botToken)
    val trainer = LearnWordsTrainer(
        learnedCount = 3,
        numberOfAnswers = 4,
        fileName = "words.txt",
    )
    var lastUpdateId = 0

    val updateIdRegex: Regex = "\"update_id\":(\\d+)".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val chatIdRegex: Regex = "\"chat\":\\{\"id\":(\\d+)".toRegex()
    val dataRegex: Regex = "\"data\":\"(.+?)\"".toRegex()

    while (true) {
        Thread.sleep(2000)
        val updates: String = service.getUpdates(lastUpdateId)
        println(updates)

        val updateId = updateIdRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue
        lastUpdateId = updateId + 1

        val message = messageTextRegex.find(updates)?.groups?.get(1)?.value
        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value?.toInt()
        val data = dataRegex.find(updates)?.groups?.get(1)?.value

        if (message?.lowercase() == "/start" && chatId != null) {
            service.sendMenu(chatId)
        }
        if (data?.lowercase() == STATISTICS && chatId != null) {
            val statistics = trainer.getStatistics()
            service.sendMessage(
                chatId,
                "Выучено ${statistics.learned} из ${statistics.total} слов | ${statistics.percent}%"
            )
        }

        if (data?.lowercase() == LEARNING && chatId != null)
            checkNextQuestionAndSend(service, trainer, chatId)

        if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true && chatId != null) {
            val answerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            if (trainer.checkAnswer(answerIndex)) {
                service.sendMessage(chatId, "Правильно!")
            } else {
                val rightQuestion = trainer.question?.correctAnswer
                service.sendMessage(
                    chatId,
                    "Не правильно: ${rightQuestion?.original} - ${rightQuestion?.translate}"
                )
            }
            checkNextQuestionAndSend(service, trainer, chatId)
        }
    }
}

fun checkNextQuestionAndSend(service: TelegramBotService, trainer: LearnWordsTrainer, chatId: Int) {
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