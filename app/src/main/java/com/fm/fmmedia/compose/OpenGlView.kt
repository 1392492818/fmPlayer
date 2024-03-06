package com.fm.fmmedia.compose

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Build
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.annotation.RequiresApi
import com.fm.openglrender.OpenglRender


@RequiresApi(Build.VERSION_CODES.Q)
@SuppressLint("ViewConstructor")
class OpenGlView(
    context: Context
) : GLSurfaceView(context), ScaleGestureDetector.OnScaleGestureListener {
    private lateinit var openglRender: OpenglRender;
    private val TOUCH_SCALE_FACTOR = 180.0f / 320

    private var mPreviousY = 0f
    private var mPreviousX = 0f
    private var mXAngle = 0
    private var mYAngle = 0


    private val mRatioWidth = 0
    private val mRatioHeight = 0

    private var mScaleGestureDetector: ScaleGestureDetector? = null
    private var mPreScale = 1.0f
    private var mCurScale = 1.0f
    private var mLastMultiTouchTime: Long = 0


    init {
        this.setEGLContextClientVersion(2)
        this.setEGLConfigChooser(
            8,
            8,
            8,
            8,
            16,
            8
        )
        this.openglRender = OpenglRender()
        this.setRenderer(this.openglRender)
        mScaleGestureDetector = ScaleGestureDetector(context, this)
    }



    fun consumeTouchEvent(e: MotionEvent) {
        dealClickEvent(e)
        var touchX = -1f
        var touchY = -1f
        when (e.action) {
            MotionEvent.ACTION_MOVE -> {
                touchX = e.x
                touchY = e.y
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchX = -1f
                touchY = -1f
            }

            else -> {}
        }

        //滑动、触摸
        openglRender.setTouchLoc(touchX, touchY)
        requestRender()
        when (e.action) {
            MotionEvent.ACTION_MOVE -> {}
            MotionEvent.ACTION_UP -> {}
            MotionEvent.ACTION_CANCEL -> {}
            else -> {}
        }
    }

    fun dealClickEvent(e: MotionEvent) {
        var touchX = -1f
        var touchY = -1f
        when (e.action) {
            MotionEvent.ACTION_UP -> {
                touchX = e.x
                touchY = e.y
                run {
                    //点击
                    openglRender.setTouchLoc(touchX, touchY)
                }
            }

            else -> {}
        }
    }


    override fun onTouchEvent(e: MotionEvent?): Boolean {
        if (e?.getPointerCount() == 1) {
            consumeTouchEvent(e)
            val currentTimeMillis = System.currentTimeMillis()
            if (currentTimeMillis - mLastMultiTouchTime > 200) {
                val y: Float = e.getY()
                val x: Float = e.getX()
                when (e.getAction()) {
                    MotionEvent.ACTION_MOVE -> {
                        val dy = y - mPreviousY
                        val dx = x - mPreviousX
                        mYAngle += (dx * TOUCH_SCALE_FACTOR).toInt()
                        mXAngle += (dy * TOUCH_SCALE_FACTOR).toInt()
                    }
                }
                mPreviousY = y
                mPreviousX = x
                openglRender.updateTransformMatrix(mXAngle, mYAngle, mCurScale, mCurScale)
                requestRender()
            }
        } else {
            e?.let { mScaleGestureDetector!!.onTouchEvent(e) }
        }
        return true;
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        val preSpan = detector.previousSpan
        val curSpan = detector.currentSpan
        mCurScale = if (curSpan < preSpan) {
            mPreScale - (preSpan - curSpan) / 200
        } else {
            mPreScale + (curSpan - preSpan) / 200
        }
        mCurScale = Math.max(0.05f, Math.min(mCurScale, 80.0f))
        openglRender.updateTransformMatrix(mXAngle, mYAngle, mCurScale, mCurScale)
        requestRender()
        return false
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        return true;

    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        mPreScale = mCurScale
        mLastMultiTouchTime = System.currentTimeMillis()
    }

    fun release(){
        this.openglRender.unInit();
    }


}