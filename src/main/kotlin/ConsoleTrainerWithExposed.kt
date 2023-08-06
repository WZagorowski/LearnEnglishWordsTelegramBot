import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object DictionaryTable : IntIdTable() {
    val english = varchar("english", 50)
    val russian = varchar("russian", 50)
    val correctCount = integer("correctCount").default(0)
}

class ConsoleTrainerWithExposed(
    private val learnedCount: Int = 1,
    private val numberOfAnswers: Int = 5
) {
    private var question: Question? = null
    companion object {
        init {
            Database.connect(url = "jdbc:sqlite:myDatabase.db", driver = "org.sqlite.JDBC")
        }
    }

    fun getStatistics(): Statistics {
        val learned = transaction {
            DictionaryTable.select { DictionaryTable.correctCount greaterEq learnedCount }
                .count().toInt()
        }
        val total = transaction {
            DictionaryTable.selectAll().count().toInt()
        }
        val percent = 100 * learned / total
        return Statistics(learned, total, percent)
    }

    fun getNextQuestion(): Question? {
        val dictionary = transaction {
            DictionaryTable.selectAll().map {
                Word(
                    original = it[DictionaryTable.english],
                    translate = it[DictionaryTable.russian],
                    correctAnswersCount = it[DictionaryTable.correctCount]
                )
            }
        }
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
        val currentQuestion = question ?: return false
        val correctAnswerIndex = currentQuestion.variants.indexOf(currentQuestion.correctAnswer)

        if (userAnswerIndex == correctAnswerIndex) {
            updateCorrectCount(currentQuestion.correctAnswer)
            return true
        }
        return false
    }

    fun resetProgress() {
        transaction {
            DictionaryTable.update {
                it[correctCount] = 0
            }
        }
    }

    fun createNewDatabaseFromFile() {
        transaction {
            SchemaUtils.create(DictionaryTable)
            val wordsFile = File("words.txt")
            val dictionary = mutableListOf<Word>()

            wordsFile.readLines().forEach {
                val splitLine = it.split("|")
                dictionary.add(Word(splitLine[0], splitLine[1], splitLine[2].toIntOrNull() ?: 0))
            }
            dictionary.forEach { word ->
                DictionaryTable.insert {
                    it[english] = word.original
                    it[russian] = word.translate
                    it[correctCount] = word.correctAnswersCount
                }
            }
        }
    }

    private fun updateCorrectCount(correctAnswer: Word) {
        transaction {
            DictionaryTable.update({ DictionaryTable.english eq correctAnswer.original }) {
                it[correctCount] = correctAnswer.correctAnswersCount + 1
            }
        }
    }
}