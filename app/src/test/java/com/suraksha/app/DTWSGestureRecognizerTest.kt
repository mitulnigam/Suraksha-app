package com.suraksha.app

import com.suraksha.app.utils.DTWSGestureRecognizer
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class DTWSGestureRecognizerTest {

    @Test
    fun sTemplateMatches() {
        val templateStroke = (0..40).map { i ->
            val t = i / 40f
            val y = t * 300f
            val x = when {
                t < 0.33f -> 50f + t * 120f
                t < 0.66f -> 170f - (t - 0.33f) * 240f
                else ->  -10f + (t - 0.66f) * 120f
            }
            DTWSGestureRecognizer.P(x, y)
        }
        assertTrue(DTWSGestureRecognizer.isS(templateStroke))
    }

    @Test
    fun randomLineDoesNotMatch() {
        val lineStroke = (0..40).map { i ->
            val t = i / 40f
            DTWSGestureRecognizer.P(10f + t * 10f, 20f + t * 300f)
        }
        assertFalse(DTWSGestureRecognizer.isS(lineStroke))
    }
}

