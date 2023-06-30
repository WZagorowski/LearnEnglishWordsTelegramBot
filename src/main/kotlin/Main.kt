import java.io.File

fun main() {
    val wordsFile = File("words.txt")
    val dictionary = Dictionary()

    for (line in wordsFile.readLines()) {
        val line = line.split("|")
        val word = Word(original = line[0], translate = line[1], correctAnswersCount = line[2].toInt())
        dictionary.listOfWords.add(word)
    }
    dictionary.listOfWords.forEach { println(it) }
}

class Dictionary {
    val listOfWords: MutableList<Word> = mutableListOf()
}

data class Word(
    val original: String,
    val translate: String,
    val correctAnswersCount: Int = 0,
)