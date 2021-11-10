package com.example.minipaint

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat


    //define a constant for the stroke width
    private const val STROKE_WIDTH = 12f // has to be float

class MyCanvasView(context: Context) : View(context) {

    ////These are your bitmap and canvas for caching what has been drawn before.
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap

    //a class level variable backgroundColor, for the background color of the canvas and initialize it to the colorBackground
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorBackground, null)

    //for holding the color to draw with
    private val drawColor = ResourcesCompat.getColor(resources, R.color.colorPaint, null)

    // Set up the paint with which to draw.
    private val paint = Paint().apply {
        color = drawColor
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
    }

    private var path = Path()

    //variables for caching the x and y coordinates of the current touch event
    //After the user stops moving and lifts their touch, these are the starting point for the next path
    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f

    //variables to cache the latest x and y values
    private var currentX = 0f
    private var currentY = 0f

    //Set sensitivity to low so performance is not impacted.
    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    //This callback method is called by the Android system with the changed screen dimensions,
    // that is, with a new width and height (to change to) and the old width and height (to change from).
    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (::extraBitmap.isInitialized) extraBitmap.recycle()

        //create an instance of Bitmap with the new width and height, which are the screen size, and assign it to extraBitmap
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        //Create a Canvas instance from extraBitmap
        extraCanvas = Canvas(extraBitmap)

        extraCanvas.drawColor(backgroundColor)

    }

    //Override onDraw() and draw the contents of the cached extraBitmap on the canvas associated with the view. The drawBitmap()
    // Canvas method comes in several versions. In this code, you provide the bitmap, the x and y coordinates (in pixels) of the top left corner,
    // and null for the Paint, as you'll set that later.
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null) //Note: The 2D coordinate system used for drawing on a Canvas is in pixels, and the origin (0,0) is at the top left corner of the Canvas.
    }

    //Upon a new path being created
    private fun touchStart() {
        path.reset()
        path.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
    }

    //Calculate the traveled distance (dx, dy), create a curve between the two points and store it in path,
    // update the running currentX and currentY tally, and draw the path. Then call invalidate() to force redrawing of the screen with the updated path.
    private fun touchMove() {
        val dx = Math.abs(motionTouchEventX - currentX)
        val dy = Math.abs(motionTouchEventY - currentY) //Calculate the distance that has been moved (dx, dy).
        if (dx >= touchTolerance || dy >= touchTolerance) { //If the movement was further than the touch tolerance, add a segment to the path.
            // QuadTo() adds a quadratic bezier from the last point,
            // approaching control point (x1,y1), and ending at (x2,y2).
            path.quadTo(currentX, currentY, (motionTouchEventX + currentX) / 2, (motionTouchEventY + currentY) / 2)
            currentX = motionTouchEventX
            currentY = motionTouchEventY
            // Draw the path in the extra bitmap to cache it.
            extraCanvas.drawPath(path, paint)
        }
        invalidate()
    }

    private fun touchUp() {
        // Reset the path so it doesn't get drawn again.
        path.reset()
    }

    //override the onTouchEvent() method to cache the x and y coordinates of the passed in event.
    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y

        //handle motion events for touching down on the screen, moving on the screen, and releasing touch on the screen
        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart()
            MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }



}