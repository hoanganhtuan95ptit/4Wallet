package com.tuanha.wallet.ui.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.tuanha.wallet.R

class ButtonView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    init {
        inflate(context, R.layout.layout_button, this)

        val attrsArray = intArrayOf(
            android.R.attr.text,
            android.R.attr.textColor
        )

        val typedArray = context.obtainStyledAttributes(attrs, attrsArray)

        findViewById<TextView>(R.id.tv_text).text = typedArray.getText(0) ?: ""
        findViewById<TextView>(R.id.tv_text).setTextColor(typedArray.getColor(1, Color.BLACK))

        typedArray.recycle()
    }
}