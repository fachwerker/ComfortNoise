package com.example.comfortnoise

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColor

class CanvasSpectogram @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val paint = Paint()

        var xHalf: Float = width*0.5f
        var yHalf: Float = height*0.5f

        val colorPositions= floatArrayOf(0.6f,0.7f,0.85f,1f)
//        val colorsArray = IntArray(colorPositions.size)
//        for (idx in 0 until  colorPositions.size) {
//            colorsArray[idx] = getColor(colorPositions[idx])
//        }
        //val colorsArray = intArrayOf(getColor(colorPositions))
        /*for (colorsArray)
            intArrayOf (getColor(colorPositions)*/

        val colorsArray =
            intArrayOf (Color.RED, Color.BLUE, Color.YELLOW,
                Color.parseColor("#FFA500"))// orange color hex code



        // pass appropriately the coordinates you want here
        paint.shader  = LinearGradient(
            xHalf, 0f, xHalf, yHalf,
            colorsArray,
            colorPositions,// distribution of colors along the length of gradient.
            Shader.TileMode.CLAMP
        )
        //paint.shader = LinearGradient(xHalf, 0f, xHalf, yHalf, Color.RED, Color.BLUE, Shader.TileMode.MIRROR);
        canvas?.drawLine(0f, 0f, 0f, yHalf, paint)
        for (x in 0 until xHalf.toInt()) {
            //canvas?.drawLine(xHalf / 2f, 0f, xHalf / 2f, yHalf, paint)
            //canvas?.drawLine(xHalf, 0f, xHalf, yHalf, paint)
            canvas?.drawLine(x.toFloat(), 0f, x.toFloat(), yHalf, paint)
        }
    }

    fun drawSpectogram(frame: DoubleArray){

    }


    private fun getColor(power: Float): Int {
        val H = power * 0.4 // Hue (note 0.4 = Green, see huge chart below)
        val S = 1.0 // Saturation
        val B = 1.0 // Brightness
        val hsb = floatArrayOf(H.toFloat(), S.toFloat(), B.toFloat())

        return Color.HSVToColor(hsb)
    }

}