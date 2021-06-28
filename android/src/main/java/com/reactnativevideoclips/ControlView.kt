package com.reactnativevideoclips

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import kotlin.math.floor


/**
 * Create By songhang in 2021/6/8
 */
class ControlView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
  : FrameLayout(context, attrs, defStyleAttr){
  private var mContext: Context = context
  private var topLine:View?=null
  private var bottomLine:View?=null
  private var leftX:Float = 0f
  private var rightX:Float = 0f
  private var touchStartX:Int = 0
  private var touchStartRightX:Int = 0
  private var itemWidth:Int = 0
  private var leftControlView:View?=null
  private var rightControlView:View?=null
  private var pointView:View?=null
  private var animator:ObjectAnimator? = null
  var controlViewController:ControlViewController? = null
  var prefiX = 0
  interface ControlViewController{
    fun isControl(flag: Boolean,start:Int,end:Int){

    }
  }
  init {
    val HM = Helper.dip2px(mContext,40)
    val H = Helper.dip2px(mContext,70)
    val W = Helper.dip2px(mContext,20)
    val VW = Helper.dip2px(mContext,2)
    val one = Helper.dip2px(mContext,1)
    val SW = Helper.getScreenWidth(mContext)
    val topLine = View(mContext)
    val bottomLine = View(mContext)
    val lineW = SW - HM * 2
    topLine.setBackgroundColor(Color.WHITE)
    bottomLine.setBackgroundColor(Color.WHITE)
    val topLayoutParams =  LayoutParams(lineW,VW)
    val bottomLayoutParams =  LayoutParams(lineW,VW)
    topLine.x = HM.toFloat()
    bottomLine.x = HM.toFloat()
    bottomLine.y = H.toFloat()
    this.topLine = topLine
    this.bottomLine = bottomLine
    addView(topLine,topLayoutParams)
    addView(bottomLine,bottomLayoutParams)


    // point
    val pointView = View(mContext)
    pointView.x = HM.toFloat()
    pointView.setBackgroundColor(Color.WHITE)
    val pointViewParams =  LayoutParams(one,H)
    addView(pointView,pointViewParams)
    this.pointView = pointView

    // slide
    val leftControlView = View(mContext)
    val rightControlView = View(mContext)
    this.leftControlView = leftControlView
    this.rightControlView = rightControlView
    leftControlView.background = Helper.getDrawable(mContext,R.drawable.controller)
    rightControlView.background = Helper.getDrawable(mContext,R.drawable.controller)
    val leftLayoutParams =  LayoutParams(W,Helper.dip2px(mContext,72))
    leftX = ((HM - W * 0.5).toFloat())
    leftControlView.x = leftX


    val rightLayoutParams =  LayoutParams(W,Helper.dip2px(mContext,72))
    rightX = (lineW + HM - W * 0.5).toFloat()
    rightControlView.x = rightX
    addView(leftControlView,leftLayoutParams)
    addView(rightControlView,rightLayoutParams)

    leftControlView.setOnTouchListener(OnTouchListener() { v, event ->
      val action = event.action
      if(action == MotionEvent.ACTION_DOWN){
        touchStartX = event.x.toInt();
        this.animatorStop()
        this.controlViewController?.isControl(true,0,0)
      }
      else if(action == MotionEvent.ACTION_MOVE){
        var newX = leftControlView.x + (event.x.toInt() - touchStartX)
        if(newX < leftX) newX = leftX
        if(newX > rightControlView.x - itemWidth){
          newX = rightControlView.x - itemWidth
        }
        leftControlView.translationX = newX
        val H_x = newX + W * 0.5
        topLine.x = H_x.toFloat()
        bottomLine.x = H_x.toFloat()

        val tllp =  topLine.layoutParams
        val bllp =  bottomLine.layoutParams
        tllp.width = (rightControlView.x - leftControlView.x).toInt()
        bllp.width = tllp.width
        topLine.layoutParams = tllp
        bottomLine.layoutParams = bllp
      }
      else if(action == MotionEvent.ACTION_UP){
        v.performClick()
        this.changeTimeRange()
      }
      else if(action == MotionEvent.ACTION_CANCEL){
        this.changeTimeRange()
      }

      true
    })
    rightControlView.setOnTouchListener(OnTouchListener() { v, event ->
      val action = event.action
      if(action == MotionEvent.ACTION_DOWN){
        touchStartRightX = event.x.toInt();
        this.animatorStop()
        this.controlViewController?.isControl(true,0,0)
      }
      else if(action == MotionEvent.ACTION_MOVE){
        var newX = rightControlView.x + (event.x.toInt() - touchStartRightX)
        if(newX > rightX) newX = rightX
        if(newX < leftControlView.x + itemWidth){
          newX = leftControlView.x + itemWidth
        }
        rightControlView.translationX = newX
        val tllp =  topLine.layoutParams
        val bllp =  bottomLine.layoutParams
        tllp.width = (rightControlView.x - leftControlView.x).toInt()
        bllp.width = tllp.width
        topLine.layoutParams = tllp
        bottomLine.layoutParams = bllp
      }
      else if(action == MotionEvent.ACTION_UP){
        v.performClick()
        this.changeTimeRange()
      }
      else if(action == MotionEvent.ACTION_CANCEL){
        this.changeTimeRange()
      }

      true
    })
  }

  fun reset(itemWidth:Int){
    val HM = Helper.dip2px(mContext,40)
    val SW = Helper.getScreenWidth(mContext)
    this.itemWidth = itemWidth
    leftControlView?.x = leftX
    rightControlView?.x = rightX
    val tllp =  topLine!!.layoutParams
    val bllp =  bottomLine!!.layoutParams

    topLine?.x = HM.toFloat()
    bottomLine?.x = HM.toFloat()
    tllp.width = SW - HM *2
    bllp.width = tllp.width

    topLine!!.layoutParams = tllp
    bottomLine!!.layoutParams = bllp
    prefiX = 0
  }
  fun changeTimeRange(){
    val W = Helper.dip2px(mContext,20)
    val leftX = leftControlView!!.translationX + W * 0.5 - W * 2 + prefiX
    val rightX = rightControlView!!.translationX + W * 0.5 - W * 2 + prefiX
    val leftIndex = floor(leftX / itemWidth)
    val rightIndex = floor(rightX / itemWidth)
    Log.d("changeTimeRange","left: $leftX right: $rightX")
    Log.d("changeTimeRange","leftIndex: $leftIndex rightIndex: $rightIndex")
    this.controlViewController?.isControl(false,leftIndex.toInt(),rightIndex.toInt())
  }
  fun startPlay(time:Int){
    this.pointView?.visibility = View.VISIBLE
    val W = Helper.dip2px(mContext,20)
    val leftX = (leftControlView!!.translationX + W * 0.5).toFloat()
    val rightX = (rightControlView!!.translationX + W * 0.5).toFloat()
    val animator = ObjectAnimator.ofFloat(this.pointView!!, "translationX", leftX, rightX)
    animator.duration = (time * 1000).toLong()
//    animator.repeatCount = ValueAnimator.INFINITE
    animator.interpolator = LinearInterpolator()
//    animator.repeatMode = ValueAnimator.RESTART
    this.animator = animator
    animator.start()
  }
  fun animatorStop(){
    this.pointView?.visibility = View.INVISIBLE
    this.animator?.cancel()
  }
}
