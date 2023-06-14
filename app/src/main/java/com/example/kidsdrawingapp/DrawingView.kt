package com.example.kidsdrawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context:Context,attrs:AttributeSet):View(context,attrs) {
    private var mDrawPath:CustomPath?=null
    private var mCanvasBitmap:Bitmap?=null
    private var mDrawPaint:Paint?=null
    private var mCanvasPaint :Paint?=null
    private var color =Color.BLACK
    private var canvas:Canvas?=null
    private var brushSize:Float=0.toFloat()
    private var mPath=ArrayList<CustomPath>()
    init {
        setUpDrawing()
    }
    fun undoClick(){
        if(mPath.size>0) {
            mPath.removeAt(mPath.lastIndex)
            invalidate()
        }
    }
    private fun setUpDrawing(){
       // brushSize=20.toFloat()
        mDrawPath=CustomPath(color,brushSize)
        mDrawPaint=Paint()
        mDrawPaint!!.color=color
        mDrawPaint!!.style=Paint.Style.STROKE
        mDrawPaint!!.strokeJoin=Paint.Join.ROUND
        mDrawPaint!!.strokeCap=Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap= Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas= Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!,0f,0f,mCanvasPaint )
        for (p in mPath){
            mDrawPaint!!.color=p.color
            mDrawPaint!!.strokeWidth=p.brushThickness
            canvas.drawPath(p,mDrawPaint!!)
        }
        if(!mDrawPath!!.isEmpty)
        {
            mDrawPaint!!.color=mDrawPath!!.color
            mDrawPaint!!.strokeWidth=mDrawPath!!.brushThickness
            canvas.drawPath(mDrawPath!!,mDrawPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touvhx=event?.x
        val touchy=event?.y
        when(event?.action){
            MotionEvent.ACTION_DOWN->{
                mDrawPath!!.brushThickness=brushSize
                mDrawPath!!.color=color
                mDrawPath?.moveTo(touvhx!!,touchy!!)
            }
            MotionEvent.ACTION_MOVE->{
                mDrawPath?.lineTo(touvhx!!,touchy!!)
            }
            MotionEvent.ACTION_UP->{
                mPath.add(mDrawPath!!)
                mDrawPath=CustomPath(color,brushSize)
            }
        }
        invalidate()
        return true
    }
    fun setSizeForBrush(newSize:Float){
        brushSize=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newSize
            , resources.displayMetrics
        )
        mDrawPaint!!.strokeWidth=brushSize
    }
    fun setColor(newColor:String){
        color=Color.parseColor(newColor)
        mDrawPaint!!.color=color
    }
    internal inner class CustomPath(var color:Int ,var brushThickness:Float):Path(){

    }}