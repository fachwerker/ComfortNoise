package com.example.comfortnoise

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

class CanvasSpectogram @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var xHalf: Float = width*1.0f
    private var yHalf: Float = height*1.0f
    private var xCurrent: Float = 0f

    private val paint_ = Paint()
    private lateinit var mCanvas : Canvas
    val WS = 2048
    val nX = 164
    val colorsArray = Array(nX) {
        IntArray(
            WS
        )
    }
    val colorPositions = Array(nX) { FloatArray( WS )  }
    //val colorsArray = IntArray(2048)
    //var colorPositions = FloatArray(2048)// FloatArray = Float.rangeTo(xHalf.toDouble())// 0f.. xHalf step xHalf/frame.size

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {
            mCanvas  = canvas
        }
        xHalf = width * 1.0f
        yHalf = height * 1.0f


        if (false)
        {
            val colorPositions = floatArrayOf(0.6f, 0.7f, 0.85f, 1f)
    //        val colorsArray = IntArray(colorPositions.size)
    //        for (idx in 0 until  colorPositions.size) {
    //            colorsArray[idx] = getColor(colorPositions[idx])
    //        }
            //val colorsArray = intArrayOf(getColor(colorPositions))
            /*for (colorsArray)
                intArrayOf (getColor(colorPositions)*/

            val colorsArray =
                intArrayOf(
                    Color.RED, Color.BLUE, Color.YELLOW,
                    Color.parseColor("#FFA500")
                )// orange color hex code


            // pass appropriately the coordinates you want here
            paint_.shader = LinearGradient(
                xHalf, 0f, xHalf, yHalf,
                colorsArray,
                colorPositions,// distribution of colors along the length of gradient.
                Shader.TileMode.CLAMP
            )
            //paint.shader = LinearGradient(xHalf, 0f, xHalf, yHalf, Color.RED, Color.BLUE, Shader.TileMode.MIRROR);
            mCanvas?.drawLine(0f, 0f, 0f, yHalf, paint_)
            for (x in 0 until xHalf.toInt()) {
                //canvas?.drawLine(xHalf / 2f, 0f, xHalf / 2f, yHalf, paint)
                //canvas?.drawLine(xHalf, 0f, xHalf, yHalf, paint)
                mCanvas?.drawLine(x.toFloat(), 0f, x.toFloat(), yHalf, paint_)
            }
        }

       for (x in 0 until nX) {
            xCurrent = (xCurrent + 1) % width
            paint_.shader = LinearGradient(
                xHalf, 0f, xHalf, yHalf,
                colorsArray[x],
                colorPositions[x],// distribution of colors along the length of gradient.
                Shader.TileMode.CLAMP
            )
            mCanvas?.drawLine(xCurrent, 0f, xCurrent, yHalf, paint_)

        }


        /*for ( x in 0 until nX) {
            for (y in 0 until WS) {
                paint_.color = colorsArray[x][y]
                mCanvas.drawPoint(x.toFloat(), y.toFloat(), paint_)
            }
        }*/
    }

    fun drawSpectogram(frame: Array<DoubleArray>){
        //var stepsize = (xHalf/frame.size)
        //var positionArray = 0.. frame.size step 1
        mCanvas?.drawLine(0f, 0f, 0f, yHalf, paint_)
        for (x in 0 until nX) {
            for (idx in frame.indices) {
                colorPositions[x][idx] = (idx) * 1f / frame.size
                colorsArray[x][idx] = getColor((1f - frame[x][idx].toFloat()))
            }
        }

        /*for (x in 0 until nX) {
            xCurrent = (xCurrent + 1) % width
            paint_.shader = LinearGradient(
                xHalf, 0f, xHalf, yHalf,
                colorsArray[x],
                colorPositions[x],// distribution of colors along the length of gradient.
                Shader.TileMode.CLAMP
            )
            mCanvas?.drawLine(xCurrent, 0f, xCurrent, yHalf, paint_)

        }*/
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