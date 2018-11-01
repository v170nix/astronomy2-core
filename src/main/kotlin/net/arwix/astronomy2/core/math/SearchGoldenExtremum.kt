package net.arwix.astronomy2.core.math


import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * @param f        функция реализующа интерфейс [Function]
 * @param a        начальная точка отразка
 * @param b        конечная точка отрезка
 * @param e        желаемая ночность
 * @param maxSteps максимальное число шагов
 */
class SearchGoldenExtremum(private val a: Double,
                           private val b: Double,
                           private val e: Double,
                           private val maxSteps: Int,
                           private val function: suspend (x: Double) -> Double) {

    private companion object {
        private val GOLDEN_RATIO = 0.5 + sqrt(5.0) / 2.0
    }

    suspend fun getMax() = doMax(a, b)
    suspend fun getMin() = doMin(a, b)

    private suspend fun doMax(initA: Double, initB: Double): Double = coroutineScope {
        var a = initA
        var b = initB
        var step = 0
        do {
            step++
            val d = (b - a) / GOLDEN_RATIO
            val x1 = b - d
            val x2 = a + d
            val y1 = async { function(x1) }
            val y2 = async { function(x2) }
            if (y1.await() <= y2.await()) {
                a = x1
            } else {
                b = x2
            }
        } while (abs(a - b) > e && step < maxSteps)
        (a + b) / 2.0
    }


    private suspend fun doMin(initA: Double, initB: Double): Double = coroutineScope {
        var a = initA
        var b = initB
        var step = 0
        do {
            step++
            val d = (b - a) / GOLDEN_RATIO
            val x1 = b - d
            val x2 = a + d
            val y1 = async { function(x1) }
            val y2 = async { function(x2) }
            if (y1.await() >= y2.await()) {
                a = x1
            } else {
                b = x2
            }
        } while (abs(a - b) > e && step < maxSteps)
        (a + b) / 2.0
    }

}