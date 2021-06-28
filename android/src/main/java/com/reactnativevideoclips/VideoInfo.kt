package com.reactnativevideoclips

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.mobile.ffmpeg.util.FFmpegAsyncUtils2
import com.mobile.ffmpeg.util.FFmpegExecuteCallback
import kotlin.math.roundToInt

/**
 * Create By songhang in 2021/6/2
 */
class VideoInfo constructor(val uri: Uri, val context: Context, private val tempPath:String,private  val holder:VideoInfoPareImage) {
  var path:String? = null
  private var width:Int = 0
  private var height:Int = 0
  private var rotation:Float = 0F
  var duration:Int = 0
  var editStart:Int = 0
  var editDuration:Int = 10
  private var asyncTask:FFmpegAsyncUtils2? = null
  var imageList:Array<String>? = null
  var name:String = ""
  interface VideoInfoPareImage{
    fun onFFmpegProgress(progress: Float){

    }
    fun onFFmpegFailed(){

    }
  }

  fun checkDuration():Boolean{
    // 获取 path (google 云相册会有线上照片获取不到)
    val path = FileController.getRealPathFromUri(context,uri)
    if(path.isNullOrEmpty()) return false

    val retr = MediaMetadataRetriever()
    retr.setDataSource(context, uri)
    val duration = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toFloat().roundToInt() / 1000
    // 解析 5 分钟内的视频
    if(duration == 0 || duration > 60 * 5) {
      retr.release()
      return false
    }
    this.duration = duration
    if(duration < 10){  editDuration = duration }
    this.path = path
    val width = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
    val height = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
    val rotation = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)

    this.rotation = rotation.toFloat()
    if(this.rotation == 90F){
      this.width = height.toInt()
      this.height = width.toInt()
    }
    else{
      this.width = width.toInt()
      this.height = height.toInt()
    }
    val imageList = Array(duration){""}
    var index = 0
    val name = tempPath + "/" +  uri.lastPathSegment
    for (item in imageList){
      imageList[index] = "${name}_${Helper.prefixNumberToString(index + 1)}.jpeg"
      index += 1
    }
    this.imageList = imageList
    retr.release()
    getImageFromVideo()
    return true
  }
  private fun getImageFromVideo(){
    val asyncTask = FFmpegAsyncUtils2()
    val that = this
    asyncTask.setCallback(object: FFmpegExecuteCallback {
      override fun onFFmpegStart() {

      }

      override fun onFFmpegSucceed(executeOutput: String?) {

      }

      override fun onFFmpegFailed(executeOutput: String?) {
        that.holder.onFFmpegFailed()
      }

      override fun onFFmpegProgress(progress: Int?) {

        if (progress != null) {
          Log.d("onFFmpegProgress", (progress   / (duration * 1000)).toFloat().toString())
          that.holder.onFFmpegProgress((progress   / (duration * 1000)).toFloat())
        }
      }

      override fun onFFmpegCancel() {
      }

    })
    val w = 100
    val h = this.height * 100 / this.width
    val name = tempPath + "/" +  uri.lastPathSegment
    this.name = uri.lastPathSegment.toString()
    val timeString = Helper.ts2Time(duration)

    asyncTask.execute("-ss 00:00 -i ${this.path} -f image2 -r 1 -t $timeString -s ${w}x${h} ${name}_%3d.jpeg")
    this.asyncTask = asyncTask
  }
  fun changeTimeRange(start:Int,end:Int){
    this.editStart = start
    this.editDuration = end
  }
  fun closeTask(){
    this.asyncTask?.onCancel()
  }
}
