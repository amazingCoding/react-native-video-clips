package com.reactnativevideoclips

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import android.util.Log
import com.facebook.react.bridge.*
import com.mobile.ffmpeg.util.FFmpegAsyncUtils2
import com.mobile.ffmpeg.util.FFmpegExecuteCallback
import java.io.File


class VideoClipsModule(val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext),ActivityEventListener {
    val NAME = "VideoClips"
    var promise:Promise? = null
    private var asyncTask:FFmpegAsyncUtils2? = null
    init {
        this.reactContext.addActivityEventListener(this)
    }
    override fun getName(): String {
        return NAME
    }
    @ReactMethod
    fun select(promise: Promise) {
      this.promise = promise
      currentActivity?.startActivityForResult(Intent(currentActivity,VideoClipActivity::class.java),Helper.PRE_CODE)
    }

  @ReactMethod
  fun compression(name:String,promise: Promise) {
    // 根据 URL 找寻 video
    // 获取 video
    val file = File(name)
    if(this.asyncTask != null){
      this.asyncTask?.onCancel()
    }
    val asyncTask = FFmpegAsyncUtils2()
    this.asyncTask = asyncTask
    val videoTempFile = Helper.creatTempPath(this.reactContext,"compression_video_temp")
    val newVideoPath = videoTempFile.path +  "/" + file.name
    asyncTask.setCallback(object: FFmpegExecuteCallback {
      override fun onFFmpegStart() {

      }

      override fun onFFmpegSucceed(executeOutput: String?) {
        val res =  Arguments.createMap()
        res.putString("videoPath",newVideoPath)
        res.putString("url","file://$newVideoPath")
        promise.resolve(res)
      }

      override fun onFFmpegFailed(executeOutput: String?) {
        promise.reject("error",executeOutput);
      }

      override fun onFFmpegProgress(progress: Int?) {
      }

      override fun onFFmpegCancel() {
      }
    })


    asyncTask.execute("-i ${file.absolutePath} -vf \"scale=iw/2:ih/2\" $newVideoPath")
    // this.promise = promise
  }

  override fun onActivityResult(activity: Activity?, requestCode: Int, resultCode: Int, data: Intent?) {
    if(resultCode == Helper.PRE_CODE){
      val error =  data?.getStringExtra("error")
      if(error.isNullOrEmpty()){
        val cancel = data?.getBooleanExtra("cancel",false)
        val res =  Arguments.createMap()
        if(cancel == true){
          res.putBoolean("cancel",cancel)
        }
        else{
          val thum =  data?.getStringExtra("thum")
          val url =  data?.getStringExtra("url")
          val name =  data?.getStringExtra("name")
          res.putString("thum",thum)
          res.putString("url",url)
          res.putString("name",name)
        }
        this.promise?.resolve(res)

      }
      else{
        this.promise?.reject("error",error)
      }

    }
  }
  override fun onNewIntent(intent: Intent?) {

  }


}
