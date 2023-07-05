import java.io.File
import java.io.FileWriter

data class Statistics(
    val learned: Int,
    val total: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer (private val  learnedCount: Int = 3, private val  numberOfAnswers: Int = 4) {

    private var question: Question? = null
    private val dictionary = loadDictionary()

    fun getStatistics(): Statistics {
        val learned = dictionary.filter { it.correctAnswersCount >= learnedCount }.size
        val total = dictionary.size
        val percent = 100 * learned / total
        return Statistics(learned, total, percent)
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < learnedCount }
        if (notLearnedList.isEmpty()) return null
        val questionWords = if (notLearnedList.size < numberOfAnswers) {
            val learnedList = dictionary.filter { it.correctAnswersCount >= learnedCount }.shuffled()
            notLearnedList.shuffled().take(numberOfAnswers) + learnedList.take(numberOfAnswers - notLearnedList.size)
        } else {
            notLearnedList.shuffled().take(numberOfAnswers)
        }.shuffled()
        val correctAnswer = questionWords.random()

        question = Question(
            variants = questionWords,
            correctAnswer = correctAnswer,
        )
        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            val correctAnswerId = it.variants.indexOf(it.correctAnswer)
            if (correctAnswerId == userAnswerIndex) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary(dictionary)
                true
            } else {
                false
            }
        } ?: false
    }
}

private fun loadDictionary(): List<Word> {
    try {
        val dictionary = mutableListOf<Word>()
        val wordsFile = File("words.txt")

        wordsFile.readLines().forEach {
            val splitLine = it.split("|")
            dictionary.add(Word(splitLine[0], splitLine[1], splitLine[2].toIntOrNull() ?: 0))
        }
        return dictionary
    } catch (e: IndexOutOfBoundsException) {
        throw IllegalStateException("некоректный файл")
    }
}

private fun saveDictionary(words: List<Word>) {
    val updatedWordsFile = FileWriter("words.txt")

    for (i in words.indices)
        updatedWordsFile.write("${words[i].original}|${words[i].translate}|${words[i].correctAnswersCount}\n")
    updatedWordsFile.close()
}