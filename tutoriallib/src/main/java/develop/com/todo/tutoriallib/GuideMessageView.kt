package develop.com.todo.tutoriallib

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Spannable
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Created by Mohammad Reza Eram  on 20/01/2018.
 */

internal class GuideMessageView(context: Context) : LinearLayout(context) {

    private val mPaint: Paint
    private val mRect: RectF

    private val mTitleTextView: TextView
    private val mContentTextView: TextView

    var location = IntArray(2)


    init {

        val density = context.resources.displayMetrics.density
        setWillNotDraw(false)
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER

        mRect = RectF()
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.strokeCap = Paint.Cap.ROUND

        val padding = (10 * density).toInt()
        val paddingBetween = (3 * density).toInt()

        mTitleTextView = TextView(context)
        mTitleTextView.setPadding(padding, padding, padding, paddingBetween)
        mTitleTextView.gravity = Gravity.CENTER
        mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
        mTitleTextView.setTextColor(Color.BLACK)
        addView(mTitleTextView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        mContentTextView = TextView(context)
        mContentTextView.setTextColor(Color.BLACK)
        mContentTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
        mContentTextView.setPadding(padding, paddingBetween, padding, padding)
        mContentTextView.gravity = Gravity.CENTER
        addView(mContentTextView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }


    fun setTitle(title: String?) {
        if (title == null) {
            removeView(mTitleTextView)
            return
        }
        mTitleTextView.text = title
    }


    fun setContentText(content: String) {
        mContentTextView.text = content
    }

    fun setContentSpan(content: Spannable) {
        mContentTextView.text = content
    }

    fun setContentTypeFace(typeFace: Typeface) {
        mContentTextView.typeface = typeFace
    }

    fun setTitleTypeFace(typeFace: Typeface) {
        mTitleTextView.typeface = typeFace
    }

    fun setTitleTextSize(size: Int) {
        mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size.toFloat())
    }

    fun setContentTextSize(size: Int) {
        mContentTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size.toFloat())
    }

    fun setColor(color: Int) {

        mPaint.alpha = 255
        mPaint.color = color

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


        this.getLocationOnScreen(location)


        mRect.set(paddingLeft.toFloat(),
                paddingTop.toFloat(),
                (width - paddingRight).toFloat(),
                (height - paddingBottom).toFloat())


        canvas.drawRoundRect(mRect, 15f, 15f, mPaint)
    }
}
