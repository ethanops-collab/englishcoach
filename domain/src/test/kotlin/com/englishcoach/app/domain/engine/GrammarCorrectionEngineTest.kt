package com.englishcoach.app.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GrammarCorrectionEngineTest {

    private val engine = GrammarCorrectionEngine()

    @Test
    fun `parses a correction response into a GrammarCorrection`() {
        val response = """
            CORRECTED: I went to school yesterday.
            EXPLANATION: Use "went" because "go" is irregular.
            REPLY: Nice, tell me more about your day at school.
        """.trimIndent()

        val parsed = engine.parse("I goed to school yesterday.", response)

        assertNotNull(parsed.correction)
        assertEquals("I went to school yesterday.", parsed.correction!!.correctedText)
        assertEquals("Use \"went\" because \"go\" is irregular.", parsed.correction.explanation)
        assertEquals("Nice, tell me more about your day at school.", parsed.coachReply)
    }

    @Test
    fun `parses a no-error response with no correction`() {
        val response = """
            NO_ERROR: true
            REPLY: Great, let's keep going.
        """.trimIndent()

        val parsed = engine.parse("Can I have a coffee, please.", response)

        assertNull(parsed.correction)
        assertEquals("Great, let's keep going.", parsed.coachReply)
    }
}
