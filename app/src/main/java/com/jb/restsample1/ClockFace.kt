package com.jb.restsample1

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View


class ClockFace @JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_COLOR = Color.RED
        private const val DEFAULT_STRIKE_WIDTH = 15f
        private const val CLOCK_HAND_LENGTH = 300f
    }

    private var mCanvas: Canvas? = null
    private var mBitmap: Bitmap
    private var mBitmapPaint = Paint(Paint.DITHER_FLAG)
    private var appStarted = false
    private var scaleFactor = 0f
    private var trans : Matrix? = null

    var centerX = 0f
    var centerY = 0f
    var endPtX = 0f
    var endPtY = 0f
    var clockRadius = CLOCK_HAND_LENGTH

    init {
        Log.i(TAG,"ClockFace init()")

        val res = resources

        mBitmap = BitmapFactory.decodeResource(res, R.drawable.tidal_clock_face2)
        mCanvas = Canvas(mBitmap.copy(Bitmap.Config.ARGB_8888, true))

        mBitmapPaint.isAntiAlias = true
        mBitmapPaint.isDither = true
        mBitmapPaint.color = DEFAULT_COLOR
        mBitmapPaint.strokeWidth = DEFAULT_STRIKE_WIDTH
        mBitmapPaint.style = Paint.Style.STROKE
        mBitmapPaint.strokeJoin = Paint.Join.ROUND
        mBitmapPaint.strokeCap = Paint.Cap.ROUND
        mBitmapPaint.xfermode = null
        mBitmapPaint.alpha = 0xff
        mBitmapPaint.isFilterBitmap = true


/*        val displayMetrics = DisplayMetrics()
        //getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
        val dHeight = displayMetrics.heightPixels
        val dWidth = displayMetrics.widthPixels

        centerX = (dWidth / 2).toFloat()
        centerY = (dHeight / 2).toFloat()*/
        Log.i(TAG,"ClockFace init. centerX = $centerX, centerY = $centerY, clockRadius = $clockRadius")
    }

    //See: https://stackoverflow.com/questions/15440647/scaled-bitmap-maintaining-aspect-ratio
    private fun myScale() : Matrix {
        Log.i(TAG,"ClockFace Scale()")
        val originalWidth: Float = mBitmap.width.toFloat()
        val originalHeight: Float = mBitmap.height.toFloat()

        scaleFactor = width / originalWidth

        val xTranslation = 0.0f
        val yTranslation = (height - originalHeight * scaleFactor) / 2.0f

        val transformation = Matrix()
        transformation.postTranslate(xTranslation, yTranslation)
        transformation.preScale(scaleFactor, scaleFactor)

        return transformation
    }

    //Example fillArrow()
    //See: https://stackoverflow.com/questions/11975636/how-to-draw-an-arrow-using-android-graphic-class

    fun setEndPoints(xPt: Float, yPt: Float) {
        Log.i(TAG,"ClockFace setEndPoints(). endPtX = $endPtX, xPt = $xPt, endPtY = $endPtY, yPt = $yPt\n\n")
        endPtX = xPt
        endPtY = yPt
    }

    fun clearCanvas() {
        Log.i(TAG,"ClockFace ClearCanvas(). appStarted = $appStarted")

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        // call the super method to keep any drawing from the parent side.
        super.onDraw(canvas)
        Log.i(TAG,"ClockFace onDraw() - 1.  centerX = $centerX, centerY = $centerY, endPtX = $endPtX, endPtY = $endPtY")

        canvas.save()

        if (!appStarted) {
            appStarted = true

            trans = myScale()


            centerX = (width / 2).toFloat()
            centerY = (height / 2).toFloat()
            clockRadius *= scaleFactor
            setEndPoints(centerX, centerY - clockRadius)
            Log.i(TAG,"ClockFace onDraw() - 2. centerX = $centerX, centerY = $centerY, endPtX = $endPtX, endPtY = $endPtY")
            Log.i(TAG,"ClockFace onDraw() - 2. trans = $trans, clockRadius = $clockRadius")


            //canvas.drawLine(centerX, centerY, endPtX, endPtY, mBitmapPaint)
        }

        Log.i(TAG,"ClockFace onDraw() - 3. drawLine(centerX = $centerX, centerY = $centerY, endPtX = $endPtX, endPtY = $endPtY...\n\n")

        canvas.drawBitmap(mBitmap, trans!!, mBitmapPaint)
        canvas.drawLine(centerX, centerY, endPtX, endPtY, mBitmapPaint)

    }

}