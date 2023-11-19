package com.example.comfortnoise

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CanvasLabels @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val paint = Paint()
        paint.textSize = 20f
        paint.color = Color.DKGRAY
        canvas.drawText("f[kHz]",20f,20f,paint)
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
            Label("0",height*22/22f),

        )
        for (label in labels) {
            canvas.drawText(label.textlabel, 20f, label.ypos, paint);
        }
    }
}