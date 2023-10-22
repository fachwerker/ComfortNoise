package com.example.comfortnoise

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import java.util.Arrays

class FFT(private val fftsize: Int, private val samplingrate: Double)  {
    private var windowsize_: Int = fftsize
    private var samplingrate_: Double = samplingrate.toDouble()
    var m: Int = 0

    // Lookup tables. Only need to recompute when size of FFT changes.
    private lateinit var cos: DoubleArray
    private lateinit var sin: DoubleArray

    private lateinit var spectogramview_:CanvasSpectogram

    init {
        var n = windowsize_
        m = (Math.log(n.toDouble()) / Math.log(2.0)).toInt()

        // Make sure n is a power of 2
        if (n != 1 shl m) throw RuntimeException("FFT length must be power of 2")

        // precompute tables
        cos = DoubleArray(n / 2)
        sin = DoubleArray(n / 2)
        for (i in 0 until n / 2) {
            cos[i] = Math.cos(-2 * Math.PI * i / n)
            sin[i] = Math.sin(-2 * Math.PI * i / n)
        }
    }



    private fun printSpectrogram(frame_out: List<Double>)
    {


        //get raw double array containing .WAV data
        val rawData: DoubleArray = frame_out.toDoubleArray()
        val length = frame_out.size // this is actually the size of the whole array

        //initialize parameters for FFT
        val OF = 8 //OF = overlap factor
        val WS = windowsize_
        val windowStep = WS / OF

        //calculate FFT parameters
        val SR: Double = samplingrate_
        val time_resolution = WS / SR
        val frequency_resolution = SR / WS
        val highest_detectable_frequency = SR / 2.0
        val lowest_detectable_frequency = 5.0 * SR / WS
        println("time_resolution:              " + time_resolution * 1000 + " ms")
        println("frequency_resolution:         $frequency_resolution Hz")
        println("highest_detectable_frequency: $highest_detectable_frequency Hz")
        println("lowest_detectable_frequency:  $lowest_detectable_frequency Hz")

        //initialize plotData array
        val nX = (length - WS) / windowStep
        val plotData = Array(nX) {
            DoubleArray(
                WS
            )
        }
        //apply FFT and find MAX and MIN amplitudes
        var maxAmp = Double.MIN_VALUE
        var minAmp = Double.MAX_VALUE
        var amp_square: Double
        val inputImag = DoubleArray(length)
        for (i in 0 until nX) {
            Arrays.fill(inputImag, 0.0)
            val WS_array = DoubleArray(length)
            fft(
                Arrays.copyOfRange(rawData, i * windowStep, i * windowStep + WS),
                WS_array
            )
            for (j in 0 until WS) {
                amp_square =
                    WS_array[2 * j] * WS_array[2 * j] + WS_array[2 * j + 1] * WS_array[2 * j + 1]
                if (amp_square == 0.0) {
                    plotData[i][j] = amp_square
                } else {
                    plotData[i][j] = 10 * Math.log10(amp_square)
                }

                //find MAX and MIN amplitude
                if (plotData[i][j] > maxAmp) maxAmp =
                    plotData[i][j] else if (plotData[i][j] < minAmp) minAmp =
                    plotData[i][j]
            }
        }
        println("---------------------------------------------------")
        println("Maximum amplitude: $maxAmp")
        println("Minimum amplitude: $minAmp")
        println("---------------------------------------------------")

        //Normalization
        val diff = maxAmp - minAmp
        for (i in 0 until nX) {
            for (j in 0 until WS) {
                plotData[i][j] = (plotData[i][j] - minAmp) / diff
            }
        }

        //plot image
        val paint = Paint()
        var ratio: Double
        for (x in 0 until nX) {
            /*for (y in 0 until WS) {
                ratio = plotData[x][y]

                //theImage.setRGB(x, y, new Color(red, green, 0).getRGB());
                val newColor: Color = getColor((1.0 - ratio).toInt())
                theImage.setRGB(x, y, newColor.getRGB())
            }*/
            spectogramview_.drawSpectogram(plotData[x])
        }
    }

    fun fft(x: DoubleArray, y: DoubleArray) {
        var n = windowsize_
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
