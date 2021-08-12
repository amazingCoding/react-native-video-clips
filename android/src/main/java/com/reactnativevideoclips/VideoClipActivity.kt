package com.reactnativevideoclips

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobile.ffmpeg.util.FFmpegAsyncUtils2
import com.mobile.ffmpeg.util.FFmpegExecuteCallback
import java.io.File


class VideoClipActivity : AppCompatActivity(),VideoInfo.VideoInfoPareImage,
  ControlView.ControlViewController {

  private var recyclerview:RecyclerView? = null
  private var adapter:ListAdapter? = null
  private var tempFile: File? = null
  private var videoTempFile: File? = null
  private var asyncTasks:Array<FFmpegAsyncUtils2?>? = null
  private var pageState:Int = 0 // 0 选择视频 1 播放视频
  private var videoInfo:VideoInfo? = null
  private var isPlayer = false
  private var mainPage:LinearLayout? = null
  private var loadPage:LinearLayout? = null
  private var videoView:VideoView? = null
  private var controlView:ControlView? = null
  private var handler:Handler? = null
  private var run:Runnable? = null
  private var isControl:Boolean = false
  private var asyncTask:FFmpegAsyncUtils2? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    this.requestWindowFeature(Window.FEATURE_NO_TITLE)
    this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)
    setContentView(R.layout.activity_video_clip)
    this.tempFile = Helper.creatTempPath(this,"clip_temp")
    this.videoTempFile = Helper.creatTempPath(this,"clip_video_temp")
    Helper.toSelect(this)
    setUpView()
  }
  override fun onRestart() {
    super.onRestart()
    if(this.isPlayer){
      // TODO 重新播放
      videoView?.seekTo(videoInfo!!.editStart * 1000)
      videoView?.start()
      controlView?.startPlay(videoInfo!!.editDuration - videoInfo!!.editStart)
    }
  }
  // 解析视频进度
  override fun onFFmpegProgress(progress:Double) {
    super.onFFmpegProgress(progress)
    Log.d("progress123", progress.toString())
    if(progress > 0.1 && !isPlayer && pageState == 1){
      loadPage?.visibility = View.GONE
      mainPage?.visibility = View.VISIBLE
      selectVideo()
    }
    this.adapter?.notifyDataSetChanged()
  }
  override fun onFFmpegFailed(){
    Toast.makeText(this,"Video Parsing Error",Toast.LENGTH_LONG).show()
    Helper.toSelect(this)
  }
  // 选择完视频
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    Log.d("onResume","onActivityResult" + this.pageState)
    if(resultCode != Activity.RESULT_OK){
      Helper.toCancel(this)
    }
    else{
      if(requestCode == Helper.CODE){
        val url = data?.data
        val videoInfo = VideoInfo(url!!,this,this.tempFile!!.path,this)
        // 大于 5 分钟的返回
        if(!videoInfo.checkDuration()) {
          Toast.makeText(this,"Video Parsing Error",Toast.LENGTH_LONG).show()
          Helper.toSelect(this)
          return
        }
        this.loadPage?.visibility = View.VISIBLE
        this.pageState = 1
        this.videoInfo = videoInfo
        this.asyncTask = null
        controlView?.reset(Helper.ListViewItemWidth(this,videoInfo.duration))
      }
    }
  }
  // 安卓 特有返回键逻辑
  override fun onBackPressed() {
    if(asyncTask != null) return
    Helper.toCancel(this)
  }
  override fun finish() {
    if(this.tempFile != null) FileController.clearDir(this.tempFile!!)
    handler?.removeCallbacks(this.run!!)
    videoView?.pause()
    videoInfo?.closeTask()
    asyncTask?.onCancel()
    super.finish()
  }
  private fun cancelEvent(){
    this.mainPage?.visibility = View.GONE
    this.pageState = 0
    this.isPlayer = false
    handler?.removeCallbacks(this.run!!)
    Helper.toSelect(this)
  }
  private fun doneEvent(){
    this.asyncTask = FFmpegAsyncUtils2()
    val startTimeString = Helper.ts2Time(videoInfo!!.editStart)
    val endTimeString = Helper.ts2Time(videoInfo!!.editDuration)
    val name = this.videoTempFile!!.path +  "/" + videoInfo!!.name
    val that = this
    asyncTask!!.setCallback(object: FFmpegExecuteCallback {
      override fun onFFmpegStart() {
        // Success
        mainPage?.visibility = View.GONE
        mainPage?.visibility = View.VISIBLE
      }

      override fun onFFmpegSucceed(executeOutput: String?) {
          // Success get path to js
        asyncTask = null
        val thum = "${name}.jpeg"
        Helper.copyFile(videoInfo!!.imageList!![videoInfo!!.editDuration - 1],thum)
        Helper.toSuccess(that,"file://${name}.mp4","file://$thum",videoInfo!!.name + ".mp4")
      }
      override fun onFFmpegFailed(executeOutput: String?) {
        Toast.makeText(that,"Video Parsing Error",Toast.LENGTH_LONG).show()
        asyncTask = null
        Helper.toCancel(that)
      }
      override fun onFFmpegProgress(progress: Int?) {
      }
      override fun onFFmpegCancel() {
      }

    })
    Log.d("doneEvent","size: ${videoInfo!!.imageList!![videoInfo!!.editDuration - 1]}")
    asyncTask!!.execute("-ss $startTimeString -t $endTimeString -i ${videoInfo!!.path} -codec copy ${name}.mp4")
    this.videoView!!.pause()
//    Log.d("doneEvent","-ss $startTimeString -t $endTimeString -i ${videoInfo!!.path} -codec copy ${name}.mp4");
  }
  private fun setUpView(){
    controlView = findViewById(R.id.video_clip_id_controlView)
    controlView!!.controlViewController = this
    val cancelBtn = findViewById<TextView>(R.id.video_clip_id_cancel_button)
    val doneBtn = findViewById<TextView>(R.id.video_clip_id_done_button)
    val that = this
    cancelBtn.setOnClickListener {
      that.cancelEvent()
    }
    doneBtn.setOnClickListener{
      that.doneEvent()
    }
    this.recyclerview = findViewById<RecyclerView>(R.id.video_clip_id_recyclerview)
    val linearLayoutManager = LinearLayoutManager(this)
    linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
    this.recyclerview?.layoutManager = linearLayoutManager
    this.mainPage = findViewById(R.id.main_page)
    this.loadPage = findViewById(R.id.load_page)
    this.videoView = findViewById(R.id.video_clip_id_video_view)
    this.recyclerview?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)

        // 开始滚动
        if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
          // 设置 flag
          isControl = true
          // 停止播放
          videoView?.pause()
          // 停止动画
          controlView?.animatorStop()
        }
        if (newState == RecyclerView.SCROLL_STATE_IDLE && isControl) {
          isControl = false
          // controlView 加上一个 前缀距离
          controlView?.prefiX = recyclerView.computeHorizontalScrollOffset()
          // 执行 controlView 的 changeTimeRange
          controlView?.changeTimeRange()
//          Log.i("recyclerview", "x: ${recyclerView.computeHorizontalScrollOffset()}");
        }
      }
    })
  }
  private fun selectVideo(){
    // 设置 listView
    isPlayer = true
    this.adapter = ListAdapter(this,this.videoInfo!!)
    this.recyclerview?.adapter = this.adapter
    // 播放视频
    videoView?.setVideoURI(videoInfo!!.uri)
    videoView?.start()
    controlView?.startPlay(videoInfo!!.editDuration - videoInfo!!.editStart)
    handler = Handler()
    run = object : Runnable {
      var currentPosition = 0
      override fun run() {
        currentPosition = videoView!!.currentPosition
        val time = currentPosition / 1000
        Log.d("duration123", time.toString()  + " - "+ videoInfo!!.editDuration.toString())
        // TO 重头播放视频
        if(time >= videoInfo!!.editDuration){
          videoView?.pause()
          videoView?.seekTo(videoInfo!!.editStart * 1000)
          videoView?.start()
          controlView?.startPlay(videoInfo!!.editDuration - videoInfo!!.editStart)
        }
        handler?.postDelayed(run!!, 500)
      }
    }
    this.handler?.post(run!!)

  }
  override fun isControl(flag: Boolean,start:Int,end:Int){
    isControl = flag
    if(flag){
      this.videoView?.pause()
    }
    else{
      videoInfo!!.changeTimeRange(start,end)
      videoView?.seekTo(videoInfo!!.editStart * 1000)
      this.videoView?.start()
      controlView?.startPlay(videoInfo!!.editDuration - videoInfo!!.editStart)
    }
  }
}
