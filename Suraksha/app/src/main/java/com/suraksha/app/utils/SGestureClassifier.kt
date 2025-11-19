package com.suraksha.app.utils

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Simple path-based classifier to determine if a stroke approximates an 'S'.
 * Strategy:
 * 1. Normalize points to (0..1) box.
 * 2. Require sufficient vertical travel.
 * 3. Subdivide into top/mid/bottom segments; measure left-right-left or right-left-right sweep.
 */
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
        if (h < w * 0.6f) return false // S typically taller than wide
        if (h < 50f) return false // too small gesture

        // Normalize
        val norm = raw.map { Point((it.x - minX) / (w.coerceAtLeast(1f)), (it.y - minY) / (h.coerceAtLeast(1f))) }

        // Split thirds
        val top = norm.filter { it.y <= 0.33f }
        val mid = norm.filter { it.y > 0.33f && it.y <= 0.66f }
        val bottom = norm.filter { it.y > 0.66f }
        if (top.isEmpty() || mid.isEmpty() || bottom.isEmpty()) return false

        val topDir = horizontalDirection(top)
        val midDir = horizontalDirection(mid)
        val bottomDir = horizontalDirection(bottom)

        // Expect alternating horizontal directions
        if (topDir == 0 || midDir == 0 || bottomDir == 0) return false
        if (!(topDir != midDir && midDir != bottomDir)) return false

        // Ensure sufficient horizontal swing
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
