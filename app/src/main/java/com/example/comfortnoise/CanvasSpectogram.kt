package com.example.comfortnoise

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

import java.util.Arrays

class CanvasSpectogram @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint_ = Paint()
    private lateinit var mCanvas : Canvas
    val nY = WINDOW_SIZE/2 + 1
    val nX = width

    // TODO: check if Array of ints could be used
    //private lateinit var colorsArray: Array<Array<Int>>
    private val colorsArray = Array(1074) { IntArray( nY )  }
    private val colorsPositionArray = FloatArray( nY ) {i -> i*1f/nY}
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mCanvas  = canvas

        for (x in 0 until width) {
            val xfloat = x.toFloat()
            val rowArray = colorsArray[x]//.toIntArray()

            paint_.shader = LinearGradient(
                xfloat, 0f, xfloat, height.toFloat(),
                rowArray,
                colorsPositionArray, // distribution of colors along the length of gradient.
                Shader.TileMode.CLAMP
            )
            mCanvas.drawLine(xfloat, 0f, xfloat, height.toFloat(), paint_)

        }

    }

    fun drawSpectogram(frame: DoubleArray){
        shiftColorsArrayOneIndexLeft()
        updateCurrentColor(frame)
        invalidate()
    }

    private fun shiftColorsArrayOneIndexLeft() {
        for (x in 0 until width - 1) {
            colorsArray[x] = colorsArray[x + 1].copyOf()
        }
    }

    private fun updateCurrentColor(frame: DoubleArray) {
        for (idx in frame.indices) {
            // if value is close to one --> high level --> color == 0 --> red
            colorsArray[width-1][idx] = getColor((1f - frame[idx].toFloat()))
        }
    }

    private fun getColor(power: Float): Int {
        //val H = power * 0.4 // Hue (note 0.4 = Green, see huge chart below)
        val H = power * 144 // Hue (note 0.4 = Green, see huge chart below)
        val S = 1.0 // Saturation
        val B = 1.0 // Brightness
        val hsb = floatArrayOf(H.toFloat(), S.toFloat(), B.toFloat())

        return Color.HSVToColor(hsb) // int the resulting argb color
    }


}

