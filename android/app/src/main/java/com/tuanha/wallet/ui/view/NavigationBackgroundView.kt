@file:Suppress("ConstantConditionIf")

package com.tuanha.wallet.ui.view

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.tuanha.coreapp.utils.extentions.animation
import com.tuanha.coreapp.utils.extentions.getColorFromAttr
import com.tuanha.coreapp.utils.extentions.toPx
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.min


class NavigationBackgroundView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    val path = Path()

    var rectF: RectF = RectF()

    var topRadius = -30.toPx().toFloat()


    private var tabs = hashMapOf<Int, Tab>()


    private var animation: ValueAnimator? = null


    private val paintDot by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    }

    private val paintBackground by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = context.getColorFromAttr(com.tuanha.coreapp.R.attr.colorNavigationBar) }
    }


    fun select(view: View) {

        animation?.cancel()

        if (tabs[view.id] == null) {
            tabs[view.id] = Tab()
        }

        tabs[view.id]?.location = (view.right - view.height / 2 - left).toFloat()

        animation = tabs.flatMap {

            listOf(
                PropertyValuesHolder.ofFloat(it.key.toString() + "dotRadius", it.value.dotRadius, if (it.key == view.id) it.value.dotRadiusMax else 0f),
                PropertyValuesHolder.ofFloat(it.key.toString() + "leftRadius", it.value.leftRadius, if (it.key == view.id) it.value.leftRadiusMax else 0f),
                PropertyValuesHolder.ofFloat(it.key.toString() + "rightRadius", it.value.rightRadius, if (it.key == view.id) it.value.rightRadiusMax else 0f),
            )
        }.animation(onStart = {

        }, onUpdate = { valueAnimator ->

            tabs.forEach {
                it.value.dotRadius = valueAnimator.getAnimatedValue(it.key.toString() + "dotRadius") as Float
                it.value.leftRadius = valueAnimator.getAnimatedValue(it.key.toString() + "leftRadius") as Float
                it.value.rightRadius = valueAnimator.getAnimatedValue(it.key.toString() + "rightRadius") as Float
            }

            path.reset()
            path.addPath(generatePath(this.rectF, topRadius, tabs))

            postInvalidate()
        }, onEnd = {

        })

    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (height <= 0 || width <= 0) {
            return
        }

        val rectF = RectF(0f, 0f, width - 0f, height - 0f)

        if (rectF == this.rectF) return
        this.rectF = rectF

        path.reset()
        path.addPath(generatePath(this.rectF, topRadius, tabs))
    }

    override fun onDraw(canvas: Canvas?) {

        if (true) {
            canvas?.drawPath(path, paintBackground)
        }

        tabs.filter { it.value.location > 0 && it.value.dotRadius > 0 }.forEach {

            canvas?.drawCircle(it.value.location, topRadius.absoluteValue + min(it.value.leftRadiusMax.absoluteValue, it.value.rightRadiusMax.absoluteValue) + 5f, it.value.dotRadius, paintDot)
        }
    }

    private fun generatePath(rect: RectF, topRadius: Float, tabs: HashMap<Int, Tab>): Path {
        val path = Path()

        val left = rect.left
        val top = rect.top
        val bottom = rect.bottom
        val right = rect.right


        val topRadiusAbs = abs(topRadius)


        path.moveTo(left, top + topRadiusAbs)
        path.lineTo(right, top + topRadiusAbs)
        path.lineTo(right, bottom)
        path.lineTo(left, bottom)
        path.lineTo(left, top + topRadiusAbs)


        if (true) {

            val path0 = Path()
            path0.addArc(RectF(left, top - topRadiusAbs, left + topRadiusAbs * 2, top + topRadiusAbs), -180f, -270f)
            path0.close()

            val path2 = Path()
            path2.moveTo(left, top)
            path2.lineTo(left + topRadiusAbs, top)
            path2.lineTo(left + topRadiusAbs, top + topRadiusAbs)
            path2.lineTo(left, top + topRadiusAbs)
            path2.lineTo(left, top)
            path2.op(path0, Path.Op.DIFFERENCE)
            path2.close()

            path.op(path2, Path.Op.UNION)
        }

        if (true) {

            val path0 = Path()
            path0.addArc(RectF(right - topRadiusAbs * 2, top - topRadiusAbs, right, top + topRadiusAbs), -180f, -270f)
            path0.close()

            val path2 = Path()
            path2.moveTo(right, top)
            path2.lineTo(right - topRadiusAbs, top)
            path2.lineTo(right - topRadiusAbs, top + topRadiusAbs)
            path2.lineTo(right, top + topRadiusAbs)
            path2.lineTo(right, top)
            path2.op(path0, Path.Op.DIFFERENCE)
            path2.close()

            path.op(path2, Path.Op.UNION)
        }

        tabs.filter { it.value.location > 0 && it.value.leftRadius > 0 && it.value.rightRadius > 0 }.forEach {

            val selectLocation = it.value.location

            val leftRadius = it.value.leftRadius.absoluteValue
            val rightRadius = it.value.rightRadius.absoluteValue


            val path0 = Path()
            path0.addArc(RectF(selectLocation - leftRadius * 2, top + topRadiusAbs, selectLocation, top + topRadiusAbs + leftRadius * 2), -90f, 90f)
            path0.close()

            val path1 = Path()
            path1.addArc(RectF(selectLocation, top + topRadiusAbs, selectLocation + rightRadius * 2, top + topRadiusAbs + rightRadius * 2), -90f, -90f)
            path1.close()

            val path2 = Path()
            path2.moveTo(selectLocation - leftRadius, top + topRadiusAbs)
            path2.lineTo(selectLocation + rightRadius, top + topRadiusAbs)
            path2.lineTo(selectLocation, top + topRadiusAbs + leftRadius)
            path2.lineTo(selectLocation - leftRadius, top + topRadiusAbs)

            path2.op(path0, Path.Op.DIFFERENCE)
            path2.op(path1, Path.Op.DIFFERENCE)
            path2.close()

            path.op(path2, Path.Op.XOR)
        }

        path.close()

        return path
    }

}

class Tab(
    var location: Float = 0f,

    var leftRadius: Float = 0f,
    var rightRadius: Float = 0f,

    var leftRadiusMax: Float = 30f,
    var rightRadiusMax: Float = 30f,

    var dotRadius: Float = 0f,
    var dotRadiusMax: Float = 5f,
)