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

    var centerX = 0f
    var centerY = 0f
    var endPtX = 0f
    var endPtY = 0f
    var scale_factor = 0f
    var clockRadius = CLOCK_HAND_LENGTH
    var trans : Matrix? = null

    init {
        Log.i(TAG,"ClockFace init()")
        myInit()
        val res = resources

        mBitmap = BitmapFactory.decodeResource(res, R.drawable.tidal_clock_face2)
        mCanvas = Canvas(mBitmap.copy(Bitmap.Config.ARGB_8888, true))
        trans = myScale()

    }

    fun myInit() {
        Log.i(TAG,"ClockFace myInit()")
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


        /*val metrics = DisplayMetrics()
        metrics.scaledDensity*/
        /*val res = resources
        mBitmap = BitmapFactory.decodeResource(res, R.drawable.tidal_clock_face)*/

        //mCanvas = Canvas(mBitmap.copy(Bitmap.Config.ARGB_8888, true))

    }

    //See: https://stackoverflow.com/questions/15440647/scaled-bitmap-maintaining-aspect-ratio
    private fun myScale() : Matrix {
        Log.i(TAG,"ClockFace Scale()")
        val originalWidth: Float = mBitmap.width.toFloat()
        val originalHeight: Float = mBitmap.height.toFloat()

        scale_factor = width / originalWidth

        val xTranslation = 0.0f
        val yTranslation = (height - originalHeight * scale_factor) / 2.0f

        val transformation = Matrix()
        transformation.postTranslate(xTranslation, yTranslation)
        transformation.preScale(scale_factor, scale_factor)

        return transformation
    }

    //See: https://stackoverflow.com/questions/11975636/how-to-draw-an-arrow-using-android-graphic-class
    private fun fillArrow(canvas: Canvas, x0: Float, y0: Float, x1: Float, y1: Float) {
        mBitmapPaint.setStyle(Paint.Style.FILL)
        val deltaX = x1 - x0
        val deltaY = y1 - y0
        val distance = Math.sqrt((deltaX * deltaX + deltaY * deltaY).toDouble())
        val frac = (1 / (distance / 30)).toFloat()
        val point_x_1 = x0 + ((1 - frac) * deltaX + frac * deltaY)
        val point_y_1 = y0 + ((1 - frac) * deltaY - frac * deltaX)
        val point_x_3 = x0 + ((1 - frac) * deltaX - frac * deltaY)
        val point_y_3 = y0 + ((1 - frac) * deltaY + frac * deltaX)

        val path = Path()
        path.fillType = Path.FillType.EVEN_ODD
        path.moveTo(point_x_1, point_y_1)
        path.lineTo(x1, y1)
        path.lineTo(point_x_3, point_y_3)
        path.lineTo(point_x_1, point_y_1)
        path.lineTo(point_x_1, point_y_1)
        path.close()
        canvas.drawPath(path, mBitmapPaint)
    }

    fun setEndPoints(xPt: Float, yPt: Float) {
        Log.i(TAG,"ClockFace setEndPoints(). endPtX = $endPtX, xPt = $xPt, endPtX = $endPtY, xPt = $yPt")
        endPtX = xPt
        endPtY = yPt
    }

    fun clearCanvas() {
        Log.i(TAG,"ClockFace ClearCanvas(). appStarted = $appStarted")
        myInit()
        appStarted = true
        val res = resources
        mBitmap = BitmapFactory.decodeResource(res, R.drawable.tidal_clock_face2)
        mCanvas = Canvas(mBitmap.copy(Bitmap.Config.ARGB_8888, true))
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        // call the super method to keep any drawing from the parent side.
        super.onDraw(canvas)
        Log.i(TAG,"ClockFace onDraw(). centerX = $centerX, centerY = $centerY, trans = $trans, clockRadius = $clockRadius")
        canvas.save()

        trans = myScale()
        canvas.drawBitmap(mBitmap, trans!!, mBitmapPaint)

        centerX = (width / 2).toFloat()
        centerY = (height / 2).toFloat()
        //clockRadius = clockRadius * scale_factor

        if (!appStarted) {
            setEndPoints(centerX, centerY - clockRadius)
            clockRadius *= scale_factor
        }

        //fillArrow(canvas, centerX, centerY, centerX, centerY + CLOCK_HAND_LENGTH)
        //if (appStarted)
        canvas.drawLine(centerX, centerY, endPtX, endPtY, mBitmapPaint)
/*        else
            canvas.drawLine(centerX, centerY, centerX, centerY - 300, mBitmapPaint)*/
    }


}