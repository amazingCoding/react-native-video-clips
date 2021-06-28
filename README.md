# react-native-video-clips

video clips

## Installation

```sh
npm install react-native-video-clips
```

## Usage

```js
import VideoClips from "react-native-video-clips";

// ...

try {
      const res = await VideoClips.select()
      console.log(res);
      if (!res.cancel) {
        // get url to show
      }
    } catch (error) {
      
    }
```


```
packagingOptions {
    pickFirst 'lib/arm64-v8a/libc++_shared.so'
    pickFirst 'lib/x86/libc++_shared.so'
    pickFirst 'lib/armeabi-v7a/libc++_shared.so'
}
defaultConfig {
    ndk {
      abiFilters 'armeabi-v7a', 'arm64-v8a'     //过滤的so库版本
    }
}
```

0. 操作逻辑
  * 选择视频
  * 解码图片（30%进度展示）
1. 图片解码服务
  * 创建目录（删除已有的）
  * 每次切换视频的时候不会删除，只有 finish 才会（直接退出 APP 造成垃圾在下次进入的时候删除）
  * 切换视频的时候查看本地是否有缓存的解码图片了
  * 切换视频的时候把前一个的任务都删除
2. 播放
  * 循环播放
  * 自定时间段播放


## TODO
* 指针跟随播放移动
* 滚动操作
* 按照位置导出视频
