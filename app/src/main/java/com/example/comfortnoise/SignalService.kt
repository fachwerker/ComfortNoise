package com.example.comfortnoise

import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.sin


class SignalService(private val fftsize: Int, val overlap_factor: Int)  {
    private var windowSize: Int = fftsize
    var m: Int = 0

    // Lookup tables. Only need to recompute when size of FFT changes.
    private lateinit var cos: DoubleArray
    private lateinit var sin: DoubleArray


    init {
        val n = windowSize
        m = (ln(n.toDouble()) / ln(2.0)).toInt()

        // Make sure n is a power of 2
        if (n != 1 shl m) throw RuntimeException("FFT length must be power of 2")

        // precompute tables
        cos = DoubleArray(n / 2)
        sin = DoubleArray(n / 2)
        for (i in 0 until n / 2) {
            cos[i] = cos(-2 * Math.PI * i / n)
            sin[i] = sin(-2 * Math.PI * i / n)
        }
    }

    fun getSpectrogram(signal: DoubleArray): Array<DoubleArray> {

        val rawData: DoubleArray = signal
        val length = signal.size // this is actually the size of the whole array

        //initialize parameters for FFT
        val WS = windowSize
        val windowStep = WS / overlap_factor

        //initialize plotData array
        val nX = (length - WS) / windowStep
        val nY = WS/2 + 1
        val plotData = Array(nX) {
            DoubleArray(
                nY
            )
        }
        //apply FFT and find MAX and MIN amplitudes
        var maxAmp = Double.MIN_VALUE
        var minAmp = Double.MAX_VALUE
        for (i in 0 until nX) {
            val imaginaryPart = DoubleArray(WS) { 0.0 } // signal is real
            val realPart = rawData.copyOfRange(i * windowStep, i * windowStep + WS)
            val startIdx = i * windowStep
            for (idx in 0 until WS) {
                realPart[idx] =
                    (rawData[startIdx + idx] * 0.5 * (1.0 - cos(2.0 * Math.PI * idx / WS)))
            }
            fft(
                realPart,
                imaginaryPart
            )
            for (j in 0 until nY) {
                var power =
                    realPart[j] * realPart[j] + imaginaryPart[j] * imaginaryPart[j]


                // e.g. 80dB below your signal's spectrum peak amplitude
                // select threshold based on the expected spectrum amplitudes
                val threshold = 1.0
                // limit values and convert to dB
                val valueDb = 10 * log10(Math.max(power, threshold))
                plotData[i][nY-j-1] = valueDb

                //find MAX and MIN amplitude
                if (valueDb > maxAmp)
                    maxAmp = valueDb
                else if (valueDb < minAmp)
                    minAmp = valueDb
            }
        }

        //Normalization
        val diff = maxAmp - minAmp
        for (i in 0 until nX) {
            for (j in 0 until nY) {
                plotData[i][j] = (plotData[i][j] - minAmp) / diff
            }
        }

        return plotData
    }

    /**********************************************************/
    /* fft.c                                                  */
    /* (c) Douglas L. Jones                                   */
    /* University of Illinois at Urbana-Champaign             */
    /* January 19, 1992                                       */
    /*                                                        */
    /*   fft: in-place radix-2 DIT DFT of a complex input     */
    /*                                                        */
    /*   input:                                               */
    /* n: length of FFT: must be a power of two               */
    /* m: n = 2**m                                            */
    /*   input/output                                         */
    /* x: double array of length n with real part of data     */
    /* y: double array of length n with imag part of data     */
    /*                                                        */
    /*   Permission to copy and use this program is granted   */
    /*   under a Creative Commons "Attribution" license       */
    /*   http://creativecommons.org/licenses/by/1.0/          */
    /**********************************************************/
    fun fft(x: DoubleArray, y: DoubleArray) {
        var n = windowSize
        var i: Int
        var j: Int
        var k: Int
        var n1: Int
        var n2: Int
        var a: Int
        var c: Double
        var s: Double
        var t1: Double
        var t2: Double

        // Bit-reverse
        j = 0
        n2 = n / 2
        i = 1
        while (i < n - 1) {
            n1 = n2
            while (j >= n1) {
                j = j - n1
                n1 = n1 / 2
            }
            j = j + n1
            if (i < j) {
                t1 = x[i]
                x[i] = x[j]
                x[j] = t1
                t1 = y[i]
                y[i] = y[j]
                y[j] = t1
            }
            i++
        }

        // FFT
        n1 = 0
        n2 = 1
        i = 0
        while (i < m) {
            n1 = n2
            n2 = n2 + n2
            a = 0
            j = 0
            while (j < n1) {
                c = cos[a]
                s = sin[a]
                a += 1 shl m - i - 1
                k = j
                while (k < n) {
                    t1 = c * x[k + n1] - s * y[k + n1]
                    t2 = s * x[k + n1] + c * y[k + n1]
                    x[k + n1] = x[k] - t1
                    y[k + n1] = y[k] - t2
                    x[k] = x[k] + t1
                    y[k] = y[k] + t2
                    k = k + n2
                }
                j++
            }
            i++
        }
    }

}
