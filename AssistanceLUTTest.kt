package ch.unifr.fuzzy.travelease

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource


class AssistanceLUTTest {

    private val epsilon = 1e-3f

    @Test
    fun `test low difficulty always yields low assistance regardless of urgency`() {
        for (t in 0..10) {
            val layout = AssistanceLUT.queryFromIntegerScale(
                difficulty = 1,
                timePressure = t
            )
            assertEquals(AssistanceLayout.LOW, layout, "Failed at T = $t")
        }
    }

    @Test
    fun `test high difficulty and low urgency yields high assistance`() {
        val score = AssistanceLUT.evaluate(difficulty = 1.0f, timePressure = 0.0f)
        assertTrue(score > 0.65f, "Expected High assistance (> 0.65), got $score")
        assertEquals(AssistanceLayout.HIGH, AssistanceLUT.resolveLayout(score))
    }

    @Test
    fun `test urgency suppression on high difficulty`() {
        // high difficulty + low urgency -> high assistance 
        val relaxedScore = AssistanceLUT.evaluate(difficulty = 1.0f, timePressure = 0.1f)
        assertEquals(AssistanceLayout.HIGH, AssistanceLUT.resolveLayout(relaxedScore))

        // high difficulty + high urgency -> moderate assistance
        val urgentScore = AssistanceLUT.evaluate(difficulty = 1.0f, timePressure = 1.0f)
        assertEquals(AssistanceLayout.MODERATE, AssistanceLUT.resolveLayout(urgentScore))
        assertTrue(urgentScore in 0.35f..0.65f)
    }

    @Test
    fun `test urgency suppression on moderate difficulty`() {
        // moderate difficulty + low urgency -> moderate assistance
        val relaxed = AssistanceLUT.evaluate(difficulty = 0.5f, timePressure = 0.2f)
        assertEquals(AssistanceLayout.MODERATE, AssistanceLUT.resolveLayout(relaxed))

        // moderate difficulty + high urgency -> low assistance
        val rushed = AssistanceLUT.evaluate(difficulty = 0.5f, timePressure = 1.0f)
        assertEquals(AssistanceLayout.LOW, AssistanceLUT.resolveLayout(rushed))
    }

    @Test
    fun `test continuous bilinear interpolation between grid nodes`() {
        val nodeA = AssistanceLUT.evaluate(0.4f, 0.4f) // 0.4137
        val nodeB = AssistanceLUT.evaluate(0.5f, 0.4f) // 0.5000
        val mid = AssistanceLUT.evaluate(0.45f, 0.4f)

        // iterpolated midpoint must lie strictly between adjacent sample nodes
        assertTrue(mid in nodeA..nodeB)
        assertEquals((nodeA + nodeB) / 2.0f, mid, epsilon)
    }

    @ParameterizedTest
    @CsvSource(
        "-0.5, 0.5, 0.0, 0.5",
        "1.5, 0.5, 1.0, 0.5",
        "0.5, -1.0, 0.5, 0.0",
        "0.5, 2.0, 0.5, 1.0"
    )
    fun `test out-of-bounds input values are clamped safely`(
        rawD: Float, rawT: Float, clampedD: Float, clampedT: Float
    ) {
        val rawResult = AssistanceLUT.evaluate(rawD, rawT)
        val expectedResult = AssistanceLUT.evaluate(clampedD, clampedT)
        assertEquals(expectedResult, rawResult, epsilon)
    }
}
