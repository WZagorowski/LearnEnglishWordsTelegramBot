import org.junit.jupiter.api.Test
import java.io.File

class LearnWordsTrainerTest {

    @Test
    fun `test statistics with 4 words of 7`() {
        val trainer = LearnWordsTrainer("src/test/4_words_of_7.txt")
        kotlin.test.assertEquals(
            Statistics(learned = 4, total = 7, percent = 57),
            trainer.getStatistics()
        )
    }

    @Test
    fun `test statistics with corrupted file`() {
        val trainer = LearnWordsTrainer("corrupted_file.txt")
        kotlin.test.assertEquals(
            Statistics(learned = 0, total = 200, percent = 0),
            trainer.getStatistics()
        )
    }

    @Test
    fun `test getNextQuestion() with multiply number of answers`() {
        val trainer = LearnWordsTrainer(
            fileName = "src/test/1_word_of_2.txt",
            learnedCount = 3,
            numberOfAnswers = 2,
        )
        val variants = listOf(
            Word("cat", "кошка", 3),
            Word("dog", "собака", 0)
        )
        val actual = trainer.getNextQuestion()
        kotlin.test.assertTrue(actual?.variants?.containsAll(variants)!!)
    }

    @Test
    fun `test getNextQuestion() with 1 number of answers`() {
        val trainer = LearnWordsTrainer(
            fileName = "src/test/1_word_of_2.txt",
            learnedCount = 3,
            numberOfAnswers = 1,
        )
        val variants = listOf(
            Word("dog", "собака", 0)
        )
        val correctAnswer = Word("dog", "собака", 0)
        kotlin.test.assertEquals(
            Question(variants, correctAnswer),
            trainer.getNextQuestion()
        )
    }

    @Test
    fun `test getNextQuestion() with all words learned`() {
        val trainer = LearnWordsTrainer(
            fileName = "src/test/all_words_learned.txt",
            learnedCount = 3,
            numberOfAnswers = 1,
        )
        kotlin.test.assertEquals(
            null,
            trainer.getNextQuestion()
        )
    }

    @Test
    fun `test checkAnswer() with true`() {
        val trainer = LearnWordsTrainer()
        val variants = listOf(
            Word("hello", "привет", 0),
            Word("cat", "кошка", 0),
            Word("dog", "собака", 0),
        )
        val correctAnswer = Word("hello", "привет", 0)
        trainer.question = Question(variants, correctAnswer)
        kotlin.test.assertEquals(
            true,
            trainer.checkAnswer(0)
        )
    }

    @Test
    fun `test checkAnswer() with false`() {
        val trainer = LearnWordsTrainer()
        val variants = listOf(
            Word("hello", "привет", 0),
            Word("cat", "кошка", 0),
            Word("dog", "собака", 0),
        )
        val correctAnswer = Word("hello", "привет", 0)
        trainer.question = Question(variants, correctAnswer)
        kotlin.test.assertEquals(
            false,
            trainer.checkAnswer(1)
        )
    }

    @Test
    fun `test resetProgress() with 2 words`() {
        val wordsFile = File("src/test/2_words.txt")
        val trainer = LearnWordsTrainer("src/test/2_words.txt")
        kotlin.test.assertEquals(
            wordsFile.writeText("define|определять|0\nsolve|решать|0\n"),
            trainer.resetProgress()
        )
    }
}