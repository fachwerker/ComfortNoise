package com.example.comfortnoise

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import java.util.Arrays

class CanvasSpectogram @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var xHalf: Float = width*1.0f
    private var yHalf: Float = height*1.0f
    private var xCurrent: Float = 0f

    private val paint_ = Paint()
    private lateinit var mCanvas : Canvas
    val WS = 2048
    val nX = width

    val displayMetrics = DisplayMetrics()
    private val colorsArray = Array(1074) { IntArray( WS )  }
    private val colorsArrayCurrent = IntArray( WS )
    /*private val colorsArray = Array(R.layout.activity_main.com) {
        IntArray(
            WS
        )
    }*/
    private val colorPositions = Array(1074) { FloatArray( WS )  }
    private val colorPositionsCurrent = FloatArray( WS )
    //val colorsArray = IntArray(2048)
    //var colorPositions = FloatArray(2048)// FloatArray = Float.rangeTo(xHalf.toDouble())// 0f.. xHalf step xHalf/frame.size

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {
            mCanvas  = canvas
        }

        for (x in 0 until width-1) {
            val xfloat = x.toFloat()
            /*colorsArray[x] = colorsArray[x+1].copyOf()
            colorPositions[x] = colorPositions[x+1].copyOf()*/
            paint_.shader = LinearGradient(
                xfloat, 0f, xfloat, height.toFloat(),
                colorsArray[x],
                colorPositions[x],// distribution of colors along the length of gradient.
                Shader.TileMode.CLAMP
            )
            mCanvas?.drawLine(xfloat, 0f, xfloat, height.toFloat(), paint_)

        }

        /*colorsArray[width-1] = colorsArrayCurrent.copyOf()
        colorPositions[width-1] = colorPositionsCurrent.copyOf()*/
        paint_.shader = LinearGradient(
            width.toFloat(), 0f, width.toFloat(), height.toFloat(),
            colorsArray[width-1],
            colorPositions[width-1],// distribution of colors along the length of gradient.
            Shader.TileMode.CLAMP
        )
        mCanvas?.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), paint_)

       /*for (x in 0 until width) {
            xCurrent = (xCurrent + 1) % width
            paint_.shader = LinearGradient(
                xHalf, 0f, xHalf, yHalf,
                colorsArray[x],
                colorPositions[x],// distribution of colors along the length of gradient.
                Shader.TileMode.CLAMP
            )
            mCanvas?.drawLine(xCurrent, 0f, xCurrent, yHalf, paint_)

        }*/
    }

    fun drawSpectogram(frame: DoubleArray){
        for (idx in frame.indices) {
            colorPositionsCurrent[idx] = (idx) * 1f / frame.size
            // if value is close to one --> high level --> color == 0 --> red
            colorsArrayCurrent[idx] = getColor((1f - frame[idx].toFloat()))
        }


        for (x in 0 until width-1) {
            val xfloat = x.toFloat()
            colorsArray[x] = colorsArray[x+1].copyOf()
            colorPositions[x] = colorPositions[x+1].copyOf()
        }
        colorsArray[width-1] = colorsArrayCurrent.copyOf()
        colorPositions[width-1] = colorPositionsCurrent.copyOf()

        invalidate()

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