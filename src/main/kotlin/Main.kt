fun main() {

    val trainer = try {
        LearnWordsTrainer(
            learnedCount = 3,
            numberOfAnswers = 4,
            fileName = "words.txt",
        )
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

    while (true) {

        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")
        when (readln().toIntOrNull()) {
            1 -> {
                while (true) {

                    val question = trainer.getNextQuestion()
                    if (question == null) {
                        println("Вы выучили все слова!")
                        break
                    } else {
                        println(question.asConsoleString())

                        val userAnswerInput = readln().toIntOrNull()
                        if (userAnswerInput == 0) break

                        if (trainer.checkAnswer(userAnswerInput?.minus(1))) {
                            println("Правильно!\n")
                        } else {
                            val answer = question.correctAnswer
                            println("Неправильно! - ${answer.original} - это ${answer.translate}\n")
                        }
                    }
                }
            }

            2 -> {
                val statistics = trainer.getStatistics()
                println("Выучено ${statistics.learned} из ${statistics.total} слов | ${statistics.percent}%")
            }

            0 -> break
            else -> println("Введите 1, 2 или 0")
        }
    }
}

fun Question.asConsoleString(): String {
    val variants = this.variants
        .mapIndexed { index: Int, word: Word -> " ${index + 1} - ${word.translate}" }
        .joinToString(separator = "\n")
    return this.correctAnswer.original + "\n" + variants + "\n 0 - выйти в меню"
}