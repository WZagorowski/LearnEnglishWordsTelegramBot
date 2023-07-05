import java.io.File
import java.io.FileWriter

fun main() {
    val wordsFile = File("words.txt")
    val dictionary = Dictionary("English dictionary")

    for (line in wordsFile.readLines()) {
        val item = line.split("|")
        val word = Word(original = item[0], translate = item[1], correctAnswersCount = item[2].toIntOrNull() ?: 0)
        dictionary.listOfWords.add(word)
    }

    while (true) {
        println("Меню: 1–Учить слова, 2–Статистика, 0–Выход")

        when (readln()) {

            "1" -> {
                while (true) {
                    val unlearnedWords = dictionary.listOfWords.filter { it.correctAnswersCount < 3 }.toMutableList()

                    if (unlearnedWords.isEmpty()) {
                        println("Вы выучили все слова!")
                        return

                    } else {
                        val listAnswers = unlearnedWords.shuffled().take(NUMBER_ANSWERS).toMutableList()
                        val wordToLearn = listAnswers.random()

                        if (listAnswers.size < NUMBER_ANSWERS) {
                            val learnedWords = dictionary.listOfWords.filter { it.correctAnswersCount > 2 }
                            listAnswers.addAll(learnedWords.shuffled().take(NUMBER_ANSWERS - listAnswers.size))
                            listAnswers.shuffle()
                        }

                        println("Выберите правильный перевод слова ${wordToLearn.original}:")
                        for (i in listAnswers.indices)
                            print("${i + 1}-${listAnswers[i].translate}, ")
                        println("0-Меню")

                        val chosenNumber = readln().toIntOrNull() ?: 0
                        if (chosenNumber in 1..NUMBER_ANSWERS) {

                            if (listAnswers[chosenNumber - 1] == wordToLearn) {
                                println("Правильно!\n")
                                wordToLearn.correctAnswersCount++
                                saveDictionary(dictionary.listOfWords)

                            } else println("Неправильно! - ${wordToLearn.original} [${wordToLearn.translate}]\n")

                        } else break
                    }
                }
            }

            "2" -> {
                val numberCorrectAnswers = dictionary.listOfWords.filter { it.correctAnswersCount > 2 }.size
                val numberAllAnswers = dictionary.listOfWords.size
                val percentCorrectAnswers = 100 * numberCorrectAnswers / numberAllAnswers
                println("Выучено $numberCorrectAnswers из $numberAllAnswers слов | $percentCorrectAnswers%")
            }

            else -> return
        }
    }
}

data class Dictionary(val name: String) {
    val listOfWords: MutableList<Word> = mutableListOf()
}

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

fun saveDictionary(dictionary: List<Word>) {
    val updatedWordsFile = FileWriter("words.txt")

    for (i in dictionary.indices)
        updatedWordsFile.write(
            "${dictionary[i].original}|${dictionary[i].translate}|${dictionary[i].correctAnswersCount}\n"
        )
    updatedWordsFile.close()
}

const val NUMBER_ANSWERS = 4