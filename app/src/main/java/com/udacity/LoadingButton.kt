package com.udacity

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.withStyledAttributes
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates


class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private var label = context.getString(R.string.download)
    private var textColor = 0
    lateinit var txtPaint: Paint

    private var backgroundColor = 0
    private var backgroundAnimValue = 0f
    private var backgroundAnim = ValueAnimator()

    private var loadingAnimValue = 0f
    private val loadingAnim = ValueAnimator.ofFloat(0f, 360f).apply {
        repeatMode = ValueAnimator.RESTART
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            loadingAnimValue = it.animatedValue as Float
            invalidate()
        }
    }

    private val animSet = AnimatorSet().apply {
        duration = TimeUnit.SECONDS.toMillis(1)
        doOnStart { isEnabled = false }
        doOnEnd { isEnabled = true }
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        if (new == ButtonState.Loading) {
            label = context.getString(R.string.button_loading)
            animSet.start()
        } else {
            label = context.getString(R.string.download)
            if (new == ButtonState.Completed) {
                animSet.cancel()
            }
        }
    }

    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            backgroundColor = getColor(R.styleable.LoadingButton_backgroundColor, 0)
            textColor = getColor(R.styleable.LoadingButton_textColor, 0)
        }
        txtPaint = Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            color = textColor
            textSize = 60f
        }
    }

    fun setState(state: ButtonState) {
        if (state != buttonState) {
            buttonState = state
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBackground()
        canvas.drawLabel()
        canvas.drawLoading()
    }

    private fun Canvas.drawBackground() {
        if (buttonState == ButtonState.Loading) {
            val btnDarkPaint = Paint().apply {
                color = backgroundColor
            }
            drawRect(
                0f,
                0f,
                backgroundAnimValue,
                heightSize.toFloat(),
                btnDarkPaint
            )

            val btnPaint = Paint().apply {
                color = context.getColor(R.color.colorPrimary)
            }
            drawRect(
                backgroundAnimValue,
                0f,
                widthSize.toFloat(),
                heightSize.toFloat(),
                btnPaint
            )
            return
        }

        drawColor(backgroundColor)
    }

    private fun Canvas.drawLabel() {
        val textHeight: Float = txtPaint.descent() - txtPaint.ascent()
        val textOffset: Float = textHeight / 2 - txtPaint.descent()
        val txtBounds = RectF(0f, 0f, widthSize.toFloat(), heightSize.toFloat())

        drawText(
            label,
            txtBounds.centerX(),
            txtBounds.centerY() + textOffset,
            txtPaint
        )
    }

    private fun Canvas.drawLoading() {
        if (buttonState != ButtonState.Loading) return
        val loadingPaint = Paint().apply {
            color = context.getColor(R.color.colorAccent)
        }
        val txtBounds = Rect()
        txtPaint.getTextBounds(label, 0, label.length, txtBounds)
        val loadingSize = txtBounds.height() - 20
        val loadingRectF = RectF().apply {
            left = (txtBounds.right + txtBounds.width() - loadingSize).toFloat()
            top = (heightSize / 2 - loadingSize).toFloat()
            right = (txtBounds.right + txtBounds.width() + loadingSize).toFloat()
            bottom = (heightSize / 2 + loadingSize).toFloat()
        }

        drawArc(
            loadingRectF,
            0f,
            loadingAnimValue,
            true,
            loadingPaint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    override fun performClick(): Boolean {
        super.performClick()
        if (buttonState == ButtonState.Completed) {
            buttonState = ButtonState.Clicked
            invalidate()
        }
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initValueAnimator()
    }

    private fun initValueAnimator() {
        backgroundAnim = ValueAnimator.ofFloat(0f, widthSize.toFloat()).apply {
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                backgroundAnimValue = it.animatedValue as Float
                invalidate()
            }
        }
        animSet.playTogether(backgroundAnim, loadingAnim)
    }
}