package com.amaurypm.medidor

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.Interpolator
import android.widget.TextView

class MedidorWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val DEFAULT_ANIMATION_TIME = 1000
    private var mInterpolator: Interpolator? = null

    private var mStartValue = 0
    private var mCurrentValue = 0
    private var mEndValue = 0

    private var mStrokeWidth = 0f
    private var mAnimationDuration = 0
    private var mBackCircleColor = 0
    private var mForegroundCircleColor = 0
    private var mAnimationSpeed = 0f

    private var mBackCirclePaint: Paint? = null
    private var mForegroundCirclePaint: Paint? = null
    private var mCurrentAngle = 0f
    private var mEndAngle: Float = 0f

    private var mlastFrame: Long = 0
    private var mAnimationStartTime: Long = 0
    private var mAnimateOnDisplay = false
    private var rectF: RectF? = null
    private var porcentajeTanque: TextView? = null


    init{
        mInterpolator = AccelerateInterpolator()

        readAttributesAndSetupFields(context, attrs)
        setupPaint()
    }


    override fun onDraw(canvas: Canvas) {
        //canvas.drawColor(Color.WHITE);
        if (mAnimationStartTime == 0L) {
            mAnimationStartTime = System.currentTimeMillis()
        }
        canvas.drawCircle(
            (
                    getWidth() / 2).toFloat(), (
                    getHeight() / 2).toFloat(),
            getHeight() / 2 - mStrokeWidth / 2,
            mBackCirclePaint!!
        )
        rectF = RectF()

        //float a = (getHeight()/2)+(mStrokeWidth)*1.5f;
        val a: Float = (getWidth() / 2 - getHeight() / 2).toFloat()

        //rectF.left = 0 + mStrokeWidth / 2;
        rectF!!.left = a + mStrokeWidth / 2
        rectF!!.top = 0 + mStrokeWidth / 2
        rectF!!.right = getHeight() - mStrokeWidth / 2 + a
        rectF!!.bottom = getHeight() - mStrokeWidth / 2


        /*
        para API Level 21 y más
        canvas.drawArc(
                0 + mStrokeWidth / 2,
                0 + mStrokeWidth / 2,
                getWidth() - mStrokeWidth / 2,
                getHeight() - mStrokeWidth / 2,
                -90,
                mAnimateOnDisplay ? getNextFrameAngle() : mEndAngle,
                false,
                mForegroundCirclePaint
        );*/

        canvas.drawArc(
            rectF!!, -91f,
            (if (mAnimateOnDisplay) getNextFrameAngle() else mEndAngle),
            false,
            mForegroundCirclePaint!!
        )
        if (mAnimateOnDisplay && mCurrentAngle < mEndAngle) {
            invalidate()
        }


        canvas.drawArc(rectF!!, -91f, if (mAnimateOnDisplay) getNextFrameAngle() else mEndAngle,false, mForegroundCirclePaint!!)
    }

    fun showAnimation() {
        mAnimateOnDisplay = true
        mAnimationStartTime = 0
        mCurrentAngle = 0f
        invalidate()
    }

    fun readAttributesAndSetupFields(context: Context, attrs: AttributeSet?) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.Vista, 0, 0)
        try {
            applyAttributes(context, a)
            setEndAngle()
            setAnimationSpeed()
        } finally {
            a.recycle()
        }
    }

    private fun applyAttributes(context: Context, a: TypedArray) {
        mStartValue = a.getInt(R.styleable.Vista_startValue, 0)
        mCurrentValue = a.getInt(R.styleable.Vista_currentValue, 0)
        mEndValue = a.getInt(R.styleable.Vista_endValue, 0)
        mAnimationDuration = a.getInt(R.styleable.Vista_animationDuration, DEFAULT_ANIMATION_TIME)
        mAnimateOnDisplay = a.getBoolean(R.styleable.Vista_animateOnDisplay, true)
        readBackCircleColorFromAttributes(a)
        readForegroundColorFromAttributes(a)
        mStrokeWidth =
            a.getDimension(R.styleable.Vista_strokeWidth, getDefaultStrokeWidth(context).toFloat())
    }

    private fun readForegroundColorFromAttributes(a: TypedArray) {
        val fc = a.getColorStateList(R.styleable.Vista_foregroundCircleColor)
        mForegroundCircleColor = fc?.defaultColor ?: Color.parseColor("FF00ABDC")
    }

    private fun readBackCircleColorFromAttributes(a: TypedArray) {
        val bc = a.getColorStateList(R.styleable.Vista_backgroundCircleColor)
        mBackCircleColor = bc?.defaultColor ?: Color.parseColor("16000000")
    }

    private fun setAnimationSpeed() {
        val seconds = mAnimationDuration.toFloat() / 1000
        val i = (seconds * 60).toInt()
        mAnimationSpeed = mEndAngle.toFloat() / i
    }

    private fun setEndAngle() {
        val totalLength = mEndValue - mStartValue
        val pathGone = mCurrentValue - mStartValue
        val v = pathGone.toFloat() / totalLength
        mEndAngle = (360 * v)
    }


    private fun setupPaint() {
        setupBackCirclePaint()
        setupFrontCirclePaint()
    }

    private fun setupFrontCirclePaint() {
        mForegroundCirclePaint = Paint()
        mForegroundCirclePaint!!.color = mForegroundCircleColor
        mForegroundCirclePaint!!.style = Paint.Style.STROKE
        mForegroundCirclePaint!!.strokeWidth = mStrokeWidth
    }


    private fun setupBackCirclePaint() {
        mBackCirclePaint = Paint()
        mBackCirclePaint!!.color = mBackCircleColor
        mBackCirclePaint!!.style = Paint.Style.STROKE
        mBackCirclePaint!!.strokeWidth = mStrokeWidth
    }


    fun getNextFrameAngle(): Float {
        val now = System.currentTimeMillis()
        val pathGone = (now - mAnimationStartTime).toFloat() / mAnimationDuration
        mCurrentAngle = if (pathGone < 1.0f) {
            mEndAngle * pathGone
        } else {
            mEndAngle.toFloat()
        }
        mlastFrame = now
        porcentajeTanque!!.text = "${Math.round(mCurrentAngle * 100 / 360).toString()}%"
        return mCurrentAngle
    }



    private fun getDefaultStrokeWidth(context: Context): Int {
        return (context.resources.displayMetrics.density * 10).toInt()
    }

    fun setCurrentValue(valor: Int, porcentajeTanque: TextView?) {
        mCurrentValue = valor
        setEndAngle()
        this.porcentajeTanque = porcentajeTanque
    }
}