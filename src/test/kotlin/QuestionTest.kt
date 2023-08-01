import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class QuestionTest {

    @Test
    fun testAsConsoleStringMultiplyVariants() {
        val variants = listOf(
            Word("hello", "привет", 0),
            Word("cat", "кошка", 0),
            Word("dog", "собака", 0),
        )
        val correctAnswer = Word("hello", "привет", 0)
        val question = Question(variants, correctAnswer)

        val actualConsoleString = question.asConsoleString()
        val expectedConsoleString = "hello\n 1 - привет\n 2 - кошка\n 3 - собака\n 0 - выйти в меню"
        assertEquals(expectedConsoleString, actualConsoleString)
    }

    @Test
    fun testAsConsoleStringOneVariant() {
        val variants = listOf(Word("hello", "привет", 0))
        val correctAnswer = Word("hello", "привет", 0)
        val question = Question(variants, correctAnswer)

        val actualConsoleString = question.asConsoleString()
        val expectedConsoleString = "hello\n 1 - привет\n 0 - выйти в меню"
        assertEquals(expectedConsoleString, actualConsoleString)
    }

    @Test
    fun testAsConsoleStringEmptyVariants() {
        val variants = emptyList<Word>()
        val correctAnswer = Word("hello", "привет", 0)
        val question = Question(variants, correctAnswer)

        val actualConsoleString = question.asConsoleString()
        val expectedConsoleString = "hello\n\n 0 - выйти в меню"
        assertEquals(expectedConsoleString, actualConsoleString)
    }
}