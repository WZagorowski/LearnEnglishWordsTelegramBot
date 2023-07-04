import java.io.File
import java.io.FileWriter

fun main() {
    val wordsFile = File("words.txt")
    val dictionary = Dictionary("English dictionary")

    for (line in wordsFile.readLines()) {
        val line = line.split("|")
        val word = Word(original = line[0], translate = line[1], correctAnswersCount = line[2].toIntOrNull() ?: 0)
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
                        val listAnswers = unlearnedWords.shuffled().take(4).toMutableList()
                        val wordToLearn = listAnswers.random()

                        if (listAnswers.size < 4) {
                            val learnedWords = dictionary.listOfWords.filter { it.correctAnswersCount > 2 }
                            val addedAnswers = learnedWords.shuffled().take(4 - listAnswers.size)
                            addedAnswers.forEach { listAnswers.add(it) }
                            listAnswers.shuffle()
                        }

                        println("Выберите правильный перевод слова ${wordToLearn.original}:")
                        for (i in listAnswers.indices)
                            print("${i + 1}-${listAnswers[i].translate}, ")
                        println("0-Меню")

                        val chosenNumber = readln().toIntOrNull() ?: 0
                        if (chosenNumber in 1..4) {

                            if (listAnswers[chosenNumber - 1] == wordToLearn) {
                                println("Правильно!\n")
                                wordToLearn.correctAnswersCount++
                                saveDictionary(dictionary)

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

fun saveDictionary(dictionary: Dictionary) {
    val dictionaryToString = mutableListOf<String>()
    val updatedDictionary = FileWriter("words.txt")

    dictionary.listOfWords.forEach { word ->
        dictionaryToString.add("${word.original}|${word.translate}|${word.correctAnswersCount}")
    }

    for (i in 1..dictionaryToString.size)
        updatedDictionary.write("${dictionaryToString[i - 1]}\n")

    updatedDictionary.close()
}