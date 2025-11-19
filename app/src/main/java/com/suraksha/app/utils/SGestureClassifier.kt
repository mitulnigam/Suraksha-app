package com.suraksha.app.utils

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


@Suppress("unused")
object SGestureClassifier {

    data class Point(val x: Float, val y: Float)

    fun isSGesture(raw: List<Point>): Boolean {
        if (raw.size < 12) return false
        val xs = raw.map { it.x }
        val ys = raw.map { it.y }
        val minX = xs.minOrNull() ?: return false
        val maxX = xs.maxOrNull() ?: return false
        val minY = ys.minOrNull() ?: return false
        val maxY = ys.maxOrNull() ?: return false
        val w = maxX - minX
        val h = maxY - minY
        if (h < w * 0.6f) return false
        if (h < 50f) return false

        val norm = raw.map { Point((it.x - minX) / (w.coerceAtLeast(1f)), (it.y - minY) / (h.coerceAtLeast(1f))) }

        val top = norm.filter { it.y <= 0.33f }
        val mid = norm.filter { it.y > 0.33f && it.y <= 0.66f }
        val bottom = norm.filter { it.y > 0.66f }
        if (top.isEmpty() || mid.isEmpty() || bottom.isEmpty()) return false

        val topDir = horizontalDirection(top)
        val midDir = horizontalDirection(mid)
        val bottomDir = horizontalDirection(bottom)

        if (topDir == 0 || midDir == 0 || bottomDir == 0) return false
        if (!(topDir != midDir && midDir != bottomDir)) return false

        val swing = (maxX - minX)
        if (swing < 40f) return false

        return true
    }

    private fun horizontalDirection(segment: List<Point>): Int {
        if (segment.size < 2) return 0
        val firstX = segment.first().x
        val lastX = segment.last().x
        val delta = lastX - firstX
        return when {
            delta > 0.05f -> 1
            delta < -0.05f -> -1
            else -> 0
        }
    }
}
