package com.reactnativevideoclips

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import com.facebook.react.bridge.*


class VideoClipsModule(val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext),ActivityEventListener {
    val NAME = "VideoClips"
    var promise:Promise? = null
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
          res.putString("thum",thum)
          res.putString("url",url)
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
