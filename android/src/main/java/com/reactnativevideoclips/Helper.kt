package com.reactnativevideoclips

import android.app.Activity
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.DisplayMetrics
import android.view.WindowManager
import java.io.*
import java.nio.channels.FileChannel


/**
 * Create By songhang in 2021/5/31
 */
object Helper {
  val PRE_CODE = 10020
  val CODE = 10021

  fun creatTempPath(context: Context,name:String):File{
    val file = File(context.cacheDir,name)
    if(file.exists()){
      FileController.clearDir(file)
    }
    file.mkdir()
    return file
  }
  fun toSelect(that:Activity){
    val intent = Intent(Intent.ACTION_PICK)
    intent.type = "video/*"
    that.startActivityForResult(intent,CODE)
  }
  fun toCancel(that:Activity){
    val intent = Intent()
    intent.putExtra("cancel",true)
    that.setResult(PRE_CODE,intent)
    that.finish()
  }
  fun toSuccess(that: Activity, url: String, thum:String){
    val intent = Intent()
    intent.putExtra("url",url)
    intent.putExtra("thum",thum)
    intent.putExtra("cancel",false)
    that.setResult(PRE_CODE,intent)
    that.finish()
  }
  fun generateThumbImage(context: Context,uri: Uri,time:Int): Bitmap? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(context,uri)
    return retriever.getFrameAtTime(((time * 1000 * 1000).toLong()), MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
  }
  fun getDrawable(context: Context, id: Int): Drawable {
    return context.resources.getDrawable(id)
  }
  fun dip2px(context: Context, dip: Int): Int {
    val scale: Float = context.resources.displayMetrics.density
    return (dip.toFloat() * scale + 0.5f).toInt()
  }
  fun getScreenWidth(context: Context): Int {
    val metric = DisplayMetrics()
    val wm = context.getSystemService(WINDOW_SERVICE) as WindowManager
    wm.defaultDisplay.getMetrics(metric)
    return metric.widthPixels
  }
  fun getScreenHeight(context: Context): Int {
    val metric = DisplayMetrics()
    val wm = context.getSystemService(WINDOW_SERVICE) as WindowManager
    wm.defaultDisplay.getMetrics(metric)
    return metric.heightPixels
  }
  fun ListViewItemWidth(context:Context,num:Int): Int {
    var count = 10
    if(num < 10) count = num
    return (getScreenWidth(context) - dip2px(context,80)) / count
  }
  fun prefixNumberToString(num:Int):String{
    if(num < 10) return "00$num"
    if(num < 100) return "0$num"
    return  "$num"
  }
  fun ts2Time(ts:Int):String{
    var timeString = ""
    timeString += if(ts > 60){
      val m :Int = ts / 60
      "0${m}:"
    }
    else{
      "00:"
    }
    val s = ts % 60
    timeString += if(s >= 10) "$s" else "0${s}"
    return timeString
  }
  fun copyFile(from: String, to: String):Boolean {
    var input: FileChannel? = null
    var output: FileChannel? = null
    var res = false
    try {
      input = FileInputStream(File(from)).channel
      output = FileOutputStream(File(to)).channel
      output.transferFrom(input, 0, input.size())
      res = true
    } catch (e: Exception) {
    } finally {
      input?.close()
      output?.close()
    }
    return res
  }
}
