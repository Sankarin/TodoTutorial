package develop.com.todo.tutoriallib

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.support.v4.widget.TextViewCompat
import android.text.Spannable
import android.util.TypedValue
import android.view.Gravity.CENTER
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AlphaAnimation
import android.widget.FrameLayout

import develop.com.todo.tutoriallib.config.DismissType
import develop.com.todo.tutoriallib.config.Gravity
import develop.com.todo.tutoriallib.listener.GuideListener

import android.widget.Button

/**
 * Created by Mohammad Reza Eram on 20/01/2018.
 */

class GuideView private constructor(context: Context, private val target: View?) : FrameLayout(context) {

    private val selfPaint = Paint()
    private val paintLine = Paint()
    private val paintCircle = Paint()
    private val paintCircleInner = Paint()
    private val targetPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val X_FER_MODE_CLEAR = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    private var targetRect: RectF? = null
    private val selfRect = Rect()

    private val density: Float
    private var stopY: Float = 0.toFloat()
    private var isTop: Boolean = false
    var isShowing: Boolean = false
        private set
    private var yMessageView = 0

    private var startYLineAndCircle: Float = 0.toFloat()
    private var circleIndicatorSize = 0f
    private var circleIndicatorSizeFinal: Float = 0.toFloat()
    private var circleInnerIndicatorSize = 0f
    private var lineIndicatorWidthSize: Float = 0.toFloat()
    private var messageViewPadding: Int = 0
    private var marginGuide: Float = 0.toFloat()
    private var strokeCircleWidth: Float = 0.toFloat()
    private var indicatorHeight: Float = 0.toFloat()

    private var isPerformedAnimationSize = false

    private var mGuideListener: GuideListener? = null
    private var mGravity: Gravity? = null
    private var dismissType: DismissType? = null
    private val mMessageView: GuideMessageView
    private var cross: Button? = null


    private val navigationBarSize: Int
        get() {
            val resources = context.resources
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            return if (resourceId > 0) {
                resources.getDimensionPixelSize(resourceId)
            } else 0
        }

    private val isLandscape: Boolean
        get() {
            val display_mode = resources.configuration.orientation
            return display_mode != Configuration.ORIENTATION_PORTRAIT
        }


    init {
        setWillNotDraw(false)
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        density = context.resources.displayMetrics.density
        init()

        initCross(context)

        val locationTarget = IntArray(2)
        target!!.getLocationOnScreen(locationTarget)
        targetRect = RectF(locationTarget[0].toFloat(),
                locationTarget[1].toFloat(),
                (locationTarget[0] + target.width).toFloat(),
                (locationTarget[1] + target.height).toFloat())

        mMessageView = GuideMessageView(getContext())
        mMessageView.setPadding(messageViewPadding, messageViewPadding, messageViewPadding, messageViewPadding)
        mMessageView.setColor(Color.WHITE)

        addView(mMessageView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        setMessageLocation(resolveMessageViewLocation())


        val layoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN)
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                else
                    viewTreeObserver.removeGlobalOnLayoutListener(this)

                setMessageLocation(resolveMessageViewLocation())
                val locationTarget = IntArray(2)
                target.getLocationOnScreen(locationTarget)

                targetRect = RectF(locationTarget[0].toFloat(),
                        locationTarget[1].toFloat(),
                        (locationTarget[0] + target.width).toFloat(),
                        (locationTarget[1] + target.height).toFloat())

                selfRect.set(paddingLeft,
                        paddingTop,
                        width - paddingRight,
                        height - paddingBottom)

                marginGuide = (if (isTop) marginGuide else -marginGuide).toInt().toFloat()
                startYLineAndCircle = (if (isTop) targetRect!!.bottom else targetRect!!.top) + marginGuide
                stopY = yMessageView + indicatorHeight
                startAnimationSize()
                viewTreeObserver.addOnGlobalLayoutListener(this)
            }
        }
        viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
    }

    private fun initCross(context: Context) {


        cross = Button(context)
        val layoutParams = FrameLayout.LayoutParams(400, FrameLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.gravity = android.view.Gravity.TOP or CENTER
        layoutParams.topMargin = 80
        layoutParams.bottomMargin = 20
        cross!!.layoutParams = layoutParams
        cross!!.text="CLOSE DEMO"
        cross!!.gravity = android.view.Gravity.CENTER
        cross!!.setTextColor(Color.parseColor("#ffffff"))
        cross!!.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
        cross!!.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_cross_24_white,0)
        cross!!.setPadding(15,0,15,0)
        cross!!.setBackgroundResource(R.drawable.cross_bg)
        TextViewCompat.setTextAppearance(cross!!,R.style.textStyle)

        addView(cross)

        cross!!.setOnClickListener {
            val buttonClick = AlphaAnimation(1f, 0.8f)
            it.startAnimation(buttonClick)
            mGuideListener!!.onClose()
        }
    }

    private fun startAnimationSize() {
        if (!isPerformedAnimationSize) {
            val circleSizeAnimator = ValueAnimator.ofFloat(0f, circleIndicatorSizeFinal)
            circleSizeAnimator.addUpdateListener {
                circleIndicatorSize = circleSizeAnimator.animatedValue as Float
                circleInnerIndicatorSize = circleSizeAnimator.animatedValue as Float - density
                postInvalidate()
            }

            val linePositionAnimator = ValueAnimator.ofFloat(stopY, startYLineAndCircle)
            linePositionAnimator.addUpdateListener {
                startYLineAndCircle = linePositionAnimator.animatedValue as Float
                postInvalidate()
            }

            linePositionAnimator.duration = SIZE_ANIMATION_DURATION.toLong()
            linePositionAnimator.start()
            linePositionAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {

                }

                override fun onAnimationEnd(animator: Animator) {
                    circleSizeAnimator.duration = SIZE_ANIMATION_DURATION.toLong()
                    circleSizeAnimator.start()
                    isPerformedAnimationSize = true
                }

                override fun onAnimationCancel(animator: Animator) {

                }

                override fun onAnimationRepeat(animator: Animator) {

                }
            })
        }
    }

    private fun init() {
        lineIndicatorWidthSize = LINE_INDICATOR_WIDTH_SIZE * density
        marginGuide = MARGIN_INDICATOR * density
        indicatorHeight = INDICATOR_HEIGHT * density
        messageViewPadding = (MESSAGE_VIEW_PADDING * density).toInt()
        strokeCircleWidth = STROKE_CIRCLE_INDICATOR_SIZE * density
        circleIndicatorSizeFinal = CIRCLE_INDICATOR_SIZE * density
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (target != null) {

            selfPaint.color = BACKGROUND_COLOR
            selfPaint.style = Paint.Style.FILL
            selfPaint.isAntiAlias = true
            canvas.drawRect(selfRect, selfPaint)

            paintLine.style = Paint.Style.FILL
            paintLine.color = LINE_INDICATOR_COLOR
            paintLine.strokeWidth = lineIndicatorWidthSize
            paintLine.isAntiAlias = true

            paintCircle.style = Paint.Style.STROKE
            paintCircle.color = CIRCLE_INDICATOR_COLOR
            paintCircle.strokeCap = Paint.Cap.ROUND
            paintCircle.strokeWidth = strokeCircleWidth
            paintCircle.isAntiAlias = true

            paintCircleInner.style = Paint.Style.FILL
            paintCircleInner.color = CIRCLE_INNER_INDICATOR_COLOR
            paintCircleInner.isAntiAlias = true


            val x = targetRect!!.left / 2 + targetRect!!.right / 2
            canvas.drawLine(x,
                    startYLineAndCircle,
                    x,
                    stopY,
                    paintLine)

            canvas.drawCircle(x, startYLineAndCircle, circleIndicatorSize, paintCircle)
            canvas.drawCircle(x, startYLineAndCircle, circleInnerIndicatorSize, paintCircleInner)

            targetPaint.xfermode = X_FER_MODE_CLEAR
            targetPaint.isAntiAlias = true

            canvas.drawRoundRect(targetRect!!, RADIUS_SIZE_TARGET_RECT.toFloat(), RADIUS_SIZE_TARGET_RECT.toFloat(), targetPaint)
        }
    }

    fun dismiss() {
        ((context as Activity).window.decorView as ViewGroup).removeView(this)
        isShowing = false
        if (mGuideListener != null) {
            if (target != null) {
                mGuideListener!!.onDismiss(target)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        if (event.action == MotionEvent.ACTION_DOWN) {
            when (dismissType) {

                DismissType.outside -> if (!isViewContains(mMessageView, x, y)) {
                    dismiss()
                }

                DismissType.anywhere -> dismiss()

                DismissType.targetView -> if (targetRect!!.contains(x, y)) {
                    target!!.performClick()
                    dismiss()
                }
            }
            return true
        }
        return false
    }

    private fun isViewContains(view: View, rx: Float, ry: Float): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1]
        val w = view.width
        val h = view.height

        return !(rx < x || rx > x + w || ry < y || ry > y + h)
    }

    private fun setMessageLocation(p: Point) {
        mMessageView.x = p.x.toFloat()
        mMessageView.y = p.y.toFloat()
        postInvalidate()
    }

    fun updateGuideViewLocation() {
        requestLayout()
    }

    private fun resolveMessageViewLocation(): Point {

        var xMessageView = 0
        if (mGravity == Gravity.center) {
            xMessageView = (targetRect!!.left - mMessageView.width / 2 + target!!.width / 2).toInt()
        } else
            xMessageView = targetRect!!.right.toInt() - mMessageView.width

        if (isLandscape) {
            xMessageView -= navigationBarSize
        }

        if (xMessageView + mMessageView.width > width)
            xMessageView = width - mMessageView.width
        if (xMessageView < 0)
            xMessageView = 0


        //set message view bottom
        if (targetRect!!.top + indicatorHeight > height / 2) {
            isTop = false
            yMessageView = (targetRect!!.top - mMessageView.height.toFloat() - indicatorHeight).toInt()
        } else {
            isTop = true
            yMessageView = (targetRect!!.top + target!!.height.toFloat() + indicatorHeight).toInt()
        }//set message view top

        if (yMessageView < 0)
            yMessageView = 0


        return Point(xMessageView, yMessageView)
    }


    fun show() {
        this.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        this.isClickable = false

        ((context as Activity).window.decorView as ViewGroup).addView(this)
        val startAnimation = AlphaAnimation(0.0f, 1.0f)
        startAnimation.duration = APPEARING_ANIMATION_DURATION.toLong()
        startAnimation.fillAfter = true
        this.startAnimation(startAnimation)
        isShowing = true
    }

    fun setTitle(str: String?) {
        mMessageView.setTitle(str)
    }

    fun setContentText(str: String) {
        mMessageView.setContentText(str)
    }


    fun setContentSpan(span: Spannable) {
        mMessageView.setContentSpan(span)
    }

    fun setTitleTypeFace(typeFace: Typeface) {
        mMessageView.setTitleTypeFace(typeFace)
    }

    fun setContentTypeFace(typeFace: Typeface) {
        mMessageView.setContentTypeFace(typeFace)
    }


    fun setTitleTextSize(size: Int) {
        mMessageView.setTitleTextSize(size)
    }


    fun setContentTextSize(size: Int) {
        mMessageView.setContentTextSize(size)
    }


    class Builder(private val context: Context) {
        private var targetView: View? = null
        private var title: String? = null
        private var contentText: String? = null
        private var gravity: Gravity? = null
        private var dismissType: DismissType? = null
        private var contentSpan: Spannable? = null
        private var titleTypeFace: Typeface? = null
        private var contentTypeFace: Typeface? = null
        private var guideListener: GuideListener? = null
        private var titleTextSize: Int = 0
        private var contentTextSize: Int = 0
        private var lineIndicatorHeight: Float = 0.toFloat()
        private var lineIndicatorWidthSize: Float = 0.toFloat()
        private var circleIndicatorSize: Float = 0.toFloat()
        private var circleInnerIndicatorSize: Float = 0.toFloat()
        private var strokeCircleWidth: Float = 0.toFloat()

        fun setTargetView(view: View): Builder {
            this.targetView = view
            return this
        }

        /**
         * gravity GuideView
         *
         * @param gravity it should be one type of Gravity enum.
         */
        fun setGravity(gravity: Gravity): Builder {
            this.gravity = gravity
            return this
        }

        /**
         * defining a title
         *
         * @param title a title. for example: submit button.
         */
        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        /**
         * defining a description for the target view
         *
         * @param contentText a description. for example: this button can for submit your information..
         */
        fun setContentText(contentText: String): Builder {
            this.contentText = contentText
            return this
        }

        /**
         * setting spannable type
         *
         * @param span a instance of spannable
         */
        fun setContentSpan(span: Spannable): Builder {
            this.contentSpan = span
            return this
        }

        /**
         * setting font type face
         *
         * @param typeFace a instance of type face (font family)
         */
        fun setContentTypeFace(typeFace: Typeface): Builder {
            this.contentTypeFace = typeFace
            return this
        }

        /**
         * adding a listener on show case view
         *
         * @param guideListener a listener for events
         */
        fun setGuideListener(guideListener: GuideListener): Builder {
            this.guideListener = guideListener
            return this
        }

        /**
         * setting font type face
         *
         * @param typeFace a instance of type face (font family)
         */
        fun setTitleTypeFace(typeFace: Typeface): Builder {
            this.titleTypeFace = typeFace
            return this
        }

        /**
         * the defined text size overrides any defined size in the default or provided style
         *
         * @param size title text by sp unit
         * @return builder
         */
        fun setContentTextSize(size: Int): Builder {
            this.contentTextSize = size
            return this
        }

        /**
         * the defined text size overrides any defined size in the default or provided style
         *
         * @param size title text by sp unit
         * @return builder
         */
        fun setTitleTextSize(size: Int): Builder {
            this.titleTextSize = size
            return this
        }

        /**
         * this method defining the type of dismissing function
         *
         * @param dismissType should be one type of DismissType enum. for example: outside -> Dismissing with click on outside of MessageView
         */
        fun setDismissType(dismissType: DismissType): Builder {
            this.dismissType = dismissType
            return this
        }

        /**
         * changing line height indicator
         *
         * @param height you can change height indicator (Converting to Dp)
         */
        fun setIndicatorHeight(height: Float): Builder {
            this.lineIndicatorHeight = height
            return this
        }

        /**
         * changing line width indicator
         *
         * @param width you can change width indicator
         */
        fun setIndicatorWidthSize(width: Float): Builder {
            this.lineIndicatorWidthSize = width
            return this
        }

        /**
         * changing circle size indicator
         *
         * @param size you can change circle size indicator
         */
        fun setCircleIndicatorSize(size: Float): Builder {
            this.circleIndicatorSize = size
            return this
        }

        /**
         * changing inner circle size indicator
         *
         * @param size you can change inner circle indicator size
         */
        fun setCircleInnerIndicatorSize(size: Float): Builder {
            this.circleInnerIndicatorSize = size
            return this
        }

        /**
         * changing stroke circle size indicator
         *
         * @param size you can change stroke circle indicator size
         */
        fun setCircleStrokeIndicatorSize(size: Float): Builder {
            this.strokeCircleWidth = size
            return this
        }


        fun build(): GuideView {
            val guideView = GuideView(context, targetView)
            guideView.mGravity = if (gravity != null) gravity else Gravity.auto
            guideView.dismissType = if (dismissType != null) dismissType else DismissType.targetView
            val density = context.resources.displayMetrics.density

            guideView.setTitle(title)
            if (contentText != null)
                guideView.setContentText(contentText!!)
            if (titleTextSize != 0)
                guideView.setTitleTextSize(titleTextSize)
            if (contentTextSize != 0)
                guideView.setContentTextSize(contentTextSize)
            if (contentSpan != null)
                guideView.setContentSpan(contentSpan!!)
            if (titleTypeFace != null) {
                guideView.setTitleTypeFace(titleTypeFace!!)
            }
            if (contentTypeFace != null) {
                guideView.setContentTypeFace(contentTypeFace!!)
            }
            if (guideListener != null) {
                guideView.mGuideListener = guideListener
            }
            if (lineIndicatorHeight != 0f) {
                guideView.indicatorHeight = lineIndicatorHeight * density
            }
            if (lineIndicatorWidthSize != 0f) {
                guideView.lineIndicatorWidthSize = lineIndicatorWidthSize * density
            }
            if (circleIndicatorSize != 0f) {
                guideView.circleIndicatorSize = circleIndicatorSize * density
            }
            if (circleInnerIndicatorSize != 0f) {
                guideView.circleInnerIndicatorSize = circleInnerIndicatorSize * density
            }
            if (strokeCircleWidth != 0f) {
                guideView.strokeCircleWidth = strokeCircleWidth * density
            }



            return guideView
        }


    }

    companion object {


        internal val TAG = "GuideView"

        private val INDICATOR_HEIGHT = 40
        private val MESSAGE_VIEW_PADDING = 5
        private val SIZE_ANIMATION_DURATION = 700
        private val APPEARING_ANIMATION_DURATION = 400
        private val CIRCLE_INDICATOR_SIZE = 6
        private val LINE_INDICATOR_WIDTH_SIZE = 3
        private val STROKE_CIRCLE_INDICATOR_SIZE = 3
        private val RADIUS_SIZE_TARGET_RECT = 15
        private val MARGIN_INDICATOR = 15

        private val BACKGROUND_COLOR = -0x67000000
        private val CIRCLE_INNER_INDICATOR_COLOR = -0x333334
        private val CIRCLE_INDICATOR_COLOR = Color.WHITE
        private val LINE_INDICATOR_COLOR = Color.WHITE
    }
}

