import java.io.File

fun main() {
    val wordsFile = File("words.txt")
    val dictionary = Dictionary("English dictionary")

    for (line in wordsFile.readLines()) {
        val line = line.split("|")
        val word = Word(original = line[0], translate = line[1], correctAnswersCount = line[2].toInt())
        dictionary.listOfWords.add(word)
    }

    while (true) {
        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")

        when (readln()) {
            "1" -> println("1")
            "2" -> {
                val numberCorrectAnswers = (dictionary.listOfWords.filter { it.correctAnswersCount > 2 }).size
                val numberAllAnswers = (dictionary.listOfWords).size
                val percentCorrectAnswers = 100 * numberCorrectAnswers / numberAllAnswers
                println("Выучено $numberCorrectAnswers из $numberAllAnswers слов | $percentCorrectAnswers%")
            }
            "0" -> return
            else -> println("Ошибка! Введен неправильный тип данных.")
        }
    }
}

data class Dictionary(val name: String) {
    val listOfWords: MutableList<Word> = mutableListOf()
}

data class Word(
    val original: String,
    val translate: String,
    val correctAnswersCount: Int = 0,
)