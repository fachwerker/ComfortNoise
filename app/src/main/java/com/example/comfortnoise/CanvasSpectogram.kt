package com.example.comfortnoise

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

class CanvasSpectogram : View {

    private lateinit var bitmap: Bitmap
    private lateinit var paint: Paint

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }
    val nY = WINDOW_SIZE/2 + 1
    val nX = width
    private fun init() {
        paint = Paint()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    }

    private val colorsArray = IntArray( nY )
    private val colorsPositionArray = FloatArray( nY ) {i -> i*1f/nY}
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.shader = LinearGradient(
            width.toFloat(), 0f, width.toFloat(), height.toFloat(),
            colorsArray,
            colorsPositionArray, // distribution of colors along the length of gradient.
            Shader.TileMode.CLAMP
        )

        val shiftedCanvas = Canvas(bitmap)
        shiftedCanvas.drawLine(width.toFloat(), 0f, width.toFloat(), height.toFloat(), paint)
        shiftedCanvas.drawBitmap(bitmap, -1f, 0f, null)
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        /* does not work!
        canvas.drawLine(width.toFloat(), 0f, width.toFloat(), height.toFloat(), paint)
        canvas.drawBitmap(bitmap, -1f, 0f, null)*/
    }

    fun drawSpectogram(frame: DoubleArray){
        updateCurrentColor(frame)
        invalidate()
    }

    private fun updateCurrentColor(frame: DoubleArray) {
        for (idx in frame.indices) {
            // if value is close to one --> high level --> color == 0 --> red
            colorsArray[idx] = getColor((1f - frame[idx].toFloat()))
        }
    }

    private fun getColor(power: Float): Int {
        //val H = power * 0.4 // Hue (note 0.4 = Green, see huge chart below)
        val H = power * 360 // Hue (note 0.4 = Green, see huge chart below)
        val S = 1.0 // Saturation
        val B = 1.0-power // Brightness
        val hsb = floatArrayOf(H.toFloat(), S.toFloat(), B.toFloat())

        return Color.HSVToColor(hsb) // int the resulting argb color
    }
}

