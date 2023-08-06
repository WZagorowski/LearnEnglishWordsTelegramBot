import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class QuestionTest {

    @Test
    fun `Test asConsoleString() with multiply variants`() {
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
    fun `Test asConsoleString() with multiply variants with another order`() {
        val variants = listOf(
            Word("dog", "собака", 0),
            Word("hello", "привет", 0),
            Word("cat", "кошка", 0),
        )
        val correctAnswer = Word("hello", "привет", 0)
        val question = Question(variants, correctAnswer)

        val actualConsoleString = question.asConsoleString()
        val expectedConsoleString = "hello\n 1 - собака\n 2 - привет\n 3 - кошка\n 0 - выйти в меню"
        assertEquals(expectedConsoleString, actualConsoleString)
    }

    @Test
    fun `Test asConsoleString() with one variant`() {
        val variants = listOf(Word("hello", "привет", 0))
        val correctAnswer = Word("hello", "привет", 0)
        val question = Question(variants, correctAnswer)

        val actualConsoleString = question.asConsoleString()
        val expectedConsoleString = "hello\n 1 - привет\n 0 - выйти в меню"
        assertEquals(expectedConsoleString, actualConsoleString)
    }

    @Test
    fun `Test asConsoleString() with empty variant`() {
        val variants = emptyList<Word>()
        val correctAnswer = Word("hello", "привет", 0)
        val question = Question(variants, correctAnswer)

        val actualConsoleString = question.asConsoleString()
        val expectedConsoleString = "hello\n\n 0 - выйти в меню"
        assertEquals(expectedConsoleString, actualConsoleString)
    }
}