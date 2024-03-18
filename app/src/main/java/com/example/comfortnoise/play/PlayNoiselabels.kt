package com.example.comfortnoise.play

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class PlayNoiselabels @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    private val paint = Paint()
    override fun onDraw(canvas: Canvas) {

        super.onDraw(canvas)
        paint.textSize = 25f
        val nightModeFlags = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> paint.color = Color.WHITE
            Configuration.UI_MODE_NIGHT_NO -> paint.color = Color.BLACK
            Configuration.UI_MODE_NIGHT_UNDEFINED -> paint.color = Color.BLACK
        }

        canvas.drawText("f",20f,30f,paint)

        canvas.drawText("[kHz]",20f,60f,paint)
        paint.textSize = 40f

        class Label(val textlabel: String,val ypos: Float)
        val labels: Array<Label> = arrayOf(
            Label("20",height*2/22f),
            Label("18",height*4/22f),
            Label("16",height*6/22f),
            Label("14",height*8/22f),
            Label("12",height*10/22f),
            Label("10",height*12/22f),
            Label("8",height*14/22f),
            Label("6",height*16/22f),
            Label("4",height*18/22f),
            Label("2",height*20/22f),
        )
        for (label in labels) {
            canvas.drawLine(0f, label.ypos, 10f, label.ypos, paint)
            canvas.drawText(label.textlabel, 20f, label.ypos+10, paint)
        }

        canvas.drawLine(0f, height.toFloat(), 10f, height.toFloat(), paint)
        canvas.drawText("0", 20f, height.toFloat(), paint)
    }
}