import java.io.File
import java.io.FileWriter

fun main() {

    val dictionary = loadDictionary()
    while (true) {
        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")

        when (readln().toIntOrNull()) {

            1 -> {
                while (true) {
                    val notLearnedWords = dictionary.filter { it.correctAnswersCount < 3 }

                    if (notLearnedWords.isEmpty()) {
                        println("Вы выучили все слова!")
                        break

                    } else {
                        val questionWords = notLearnedWords.take(NUMBER_ANSWERS).shuffled().toMutableList()
                        val correctAnswer = questionWords.random()

                        if (questionWords.size < NUMBER_ANSWERS) {
                            val learnedWords = dictionary.filter { it.correctAnswersCount >= 3 }
                            questionWords.addAll(learnedWords.shuffled().take(NUMBER_ANSWERS - questionWords.size))
                            questionWords.shuffle()
                        }

                        println("Выбери перевод слова ${correctAnswer.original}:")
                        for (i in questionWords.indices)
                            print("${i + 1} - ${questionWords[i].translate}, ")
                        println("0 - Меню")

                        val userAnswerInput = readln().toIntOrNull() ?: 0
                        if (userAnswerInput == 0) break
                        val correctAnswerIndex = questionWords.indexOf(correctAnswer)

                        if (userAnswerInput == correctAnswerIndex + 1) {
                            correctAnswer.correctAnswersCount++
                            saveDictionary(dictionary)
                            println("Правильно!\n")
                        } else
                            println("Неправильно! - ${correctAnswer.original} - это ${correctAnswer.translate}\n")
                    }
                }
            }

            2 -> {
                val learned = dictionary.filter { it.correctAnswersCount >= 3 }.size
                val total = dictionary.size
                val percent = 100 * learned / total
                println("Выучено $learned из $total слов | $percent%")
            }

            0 -> break
            else -> println("Введите 1, 2 или 0")
        }
    }
}

fun saveDictionary(words: List<Word>) {
    val updatedWordsFile = FileWriter("words.txt")

    for (i in words.indices)
        updatedWordsFile.write("${words[i].original}|${words[i].translate}|${words[i].correctAnswersCount}\n")
    updatedWordsFile.close()
}

fun loadDictionary(): List<Word> {
    val dictionary = mutableListOf<Word>()
    val wordsFile = File("words.txt")

    wordsFile.readLines().forEach {
        val splitLine = it.split("|")
        dictionary.add(Word(splitLine[0], splitLine[1], splitLine[2].toIntOrNull() ?: 0))
    }
    return dictionary
}

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

const val NUMBER_ANSWERS = 4