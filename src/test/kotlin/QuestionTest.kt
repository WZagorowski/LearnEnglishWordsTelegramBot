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
        val expectedConsoleString = "hello\n1 - привет\n2 - кошка\n3 - собака\n0 - exit to the menu"
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
        val expectedConsoleString = "hello\n1 - собака\n2 - привет\n3 - кошка\n0 - exit to the menu"
        assertEquals(expectedConsoleString, actualConsoleString)
    }

    @Test
    fun `Test asConsoleString() with one variant`() {
        val variants = listOf(Word("hello", "привет", 0))
        val correctAnswer = Word("hello", "привет", 0)
        val question = Question(variants, correctAnswer)

        val actualConsoleString = question.asConsoleString()
        val expectedConsoleString = "hello\n1 - привет\n0 - exit to the menu"
        assertEquals(expectedConsoleString, actualConsoleString)
    }

    @Test
    fun `Test asConsoleString() with empty variant`() {
        val variants = emptyList<Word>()
        val correctAnswer = Word("hello", "привет", 0)
        val question = Question(variants, correctAnswer)

        val actualConsoleString = question.asConsoleString()
        val expectedConsoleString = "hello\n\n0 - exit to the menu"
        assertEquals(expectedConsoleString, actualConsoleString)
    }
}