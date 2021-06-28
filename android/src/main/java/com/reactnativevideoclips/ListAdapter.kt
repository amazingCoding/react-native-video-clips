package com.reactnativevideoclips

import android.R.attr.rotation
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File


/**
 * Create By songhang in 2021/5/31
 */
class ListAdapter(private val context:Context, val video:VideoInfo): RecyclerView.Adapter<ListAdapter.Holder>() {
  class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var imageView: ImageView = itemView.findViewById(R.id.video_clip_id_recyclerview_item)
//    var imageView: TextView = itemView.findViewById(R.id.video_clip_id_recyclerview_item)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
    val view: View = LayoutInflater.from(context).inflate(R.layout.activity_video_clip_item, parent, false)
    val layoutParams = view.layoutParams
    layoutParams.width = Helper.ListViewItemWidth(context,video.duration)
    view.layoutParams = layoutParams
    return Holder(view)
  }

  override fun onBindViewHolder(holder: Holder, position: Int) {
    // 设置大小 & 获取图片
    if(video.imageList != null && video.imageList!![position] != "0"){
      val file = File(video.imageList!![position])
      if(file.exists()){
        holder.imageView.visibility = View.VISIBLE
        Glide.with(context).load(file.path)
          .into(holder.imageView)
      }
    }
    else{
      holder.imageView.visibility = View.INVISIBLE
    }
  }

  override fun getItemCount(): Int {
    return video.duration
  }
}
