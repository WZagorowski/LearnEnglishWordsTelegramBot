fun main() {

    val trainer = try {
        ConsoleTrainerWithExposed()
    } catch (e: Exception) {
        println("Failed to load dictionary")
        return
    }

    while (true) {
        println("Menu: 1 - Learn words, 2 - Statistics, 3 - Reset statistics, 0 - Exit")
        when (readln().toIntOrNull()) {
            1 -> {
                while (true) {
                    val question = trainer.getNextQuestion()

                    if (question == null) {
                        println("You learned all words")
                        break
                    } else {
                        println(question.asConsoleString())
                        val userAnswerInput = readln().toIntOrNull()

                        if (userAnswerInput == 0) break

                        if (trainer.checkAnswer(userAnswerInput?.minus(1))) {
                            println("Right!\n")
                        } else {
                            val answer = question.correctAnswer
                            println("Wrong! - ${answer.original} - it's ${answer.translate}\n")
                        }
                    }
                }
            }

            2 -> {
                val statistics = trainer.getStatistics()
                println("Learned ${statistics.learned} of ${statistics.total} words | ${statistics.percent}%")
            }

            3 -> {
                trainer.resetProgress()
                println("Progress has been reset")
            }

            0 -> break
            else -> println("Enter the number 1, 2, 3 or 0")
        }
    }
}

fun Question.asConsoleString(): String {
    val variants = this.variants
        .mapIndexed { index: Int, word: Word -> "${index + 1} - ${word.translate}" }
        .joinToString(separator = "\n")
    return this.correctAnswer.original + "\n" + variants + "\n0 - exit to the menu"
}