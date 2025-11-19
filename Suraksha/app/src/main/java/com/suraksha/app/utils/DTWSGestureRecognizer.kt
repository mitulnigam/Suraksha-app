package com.suraksha.app.utils

import kotlin.math.abs
import kotlin.math.min

/**
 * Dynamic Time Warping based S gesture recognizer.
 * Stores template strokes; incoming stroke is resampled and compared.
 */
object DTWSGestureRecognizer {

    data class P(val x: Float, val y: Float)

    private val templates: MutableList<List<P>> = mutableListOf()

    init {
        // Basic synthetic 'S' template (left->right top, right->left mid, left->right bottom)
        templates.add(generateSTemplate())
    }

    private fun generateSTemplate(): List<P> {
        val pts = mutableListOf<P>()
        for (i in 0..30) {
            val t = i / 30f
            val y = t
            val x = when {
                t < 0.33f -> t * 0.6f // sweep right
                t < 0.66f -> 0.6f - (t - 0.33f) * 1.2f // sweep back left
                else -> (t - 0.66f) * 0.6f // sweep right again
            }
            pts.add(P(x, y))
        }
        return pts
    }

    fun isS(raw: List<P>, maxDistance: Float = 0.35f): Boolean {
        if (raw.size < 10) return false
        val norm = normalize(raw)
        val resampled = resample(norm, 32)
        return templates.any { t -> dtwDistance(t, resampled) <= maxDistance }
    }

    private fun normalize(raw: List<P>): List<P> {
        val minX = raw.minOf { it.x }
        val maxX = raw.maxOf { it.x }
        val minY = raw.minOf { it.y }
        val maxY = raw.maxOf { it.y }
        val w = (maxX - minX).coerceAtLeast(1f)
        val h = (maxY - minY).coerceAtLeast(1f)
        return raw.map { P((it.x - minX) / w, (it.y - minY) / h) }
    }

    private fun resample(points: List<P>, target: Int): List<P> {
        if (points.isEmpty()) return points
        val dist = cumulativeDistances(points)
        val total = dist.last()
        val out = mutableListOf<P>()
        for (i in 0 until target) {
            val d = total * i / (target - 1)
            out.add(interpolate(points, dist, d))
        }
        return out
    }

    private fun cumulativeDistances(points: List<P>): List<Float> {
        val out = MutableList(points.size) { 0f }
        for (i in 1 until points.size) {
            val dx = points[i].x - points[i - 1].x
            val dy = points[i].y - points[i - 1].y
            out[i] = out[i - 1] + kotlin.math.sqrt(dx * dx + dy * dy)
        }
        return out
    }

    private fun interpolate(points: List<P>, dist: List<Float>, d: Float): P {
        if (d <= 0f) return points.first()
        val total = dist.last()
        if (d >= total) return points.last()
        var i = 1
        while (i < dist.size && dist[i] < d) i++
        val prev = points[i - 1]
        val next = points[i]
        val segment = dist[i] - dist[i - 1]
        val ratio = if (segment == 0f) 0f else (d - dist[i - 1]) / segment
        return P(prev.x + (next.x - prev.x) * ratio, prev.y + (next.y - prev.y) * ratio)
    }

    private fun dtwDistance(a: List<P>, b: List<P>): Float {
        val n = a.size
        val m = b.size
        val dp = Array(n + 1) { FloatArray(m + 1) { Float.POSITIVE_INFINITY } }
        dp[0][0] = 0f
        for (i in 1..n) {
            for (j in 1..m) {
                val cost = abs(a[i - 1].x - b[j - 1].x) + abs(a[i - 1].y - b[j - 1].y)
                dp[i][j] = cost + min(dp[i - 1][j], min(dp[i][j - 1], dp[i - 1][j - 1]))
            }
        }
        return dp[n][m] / (n + m)
    }
}

