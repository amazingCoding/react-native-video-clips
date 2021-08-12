## video clips Tools 

### 基本功能构想
* 提供完整的界面
  * 选择图片界面 - 播放/剪辑界面 - 生成视频 - 返回 JS
* 提供简化版功能
  * 传入选择图片的地址 - 播放/剪辑界面 - 生成视频 - 返回 JS
* 提供纯函数功能
  * 解码视频（按照时间获取视频截图）
  * 生成视频

### TODO
* IOS
  * 替换核心功能库： FFMEG
  * 修改逻辑 - 先选择图片 - 再弹出自定义弹层
* android
  * 修改逻辑

### 配置
* IOS
  * 获取相册权限(iOS14 以及以上不需要)
  ```
    <key>NSPhotoLibraryUsageDescription</key>
    <string>In order to select/save photo, we need your permission to use camera</string>
    <key>NSPhotoLibraryAddUsageDescription</key>
    <string>In order to select/save photo, we need your permission to use camera</string>
  ```
* Android
  * 获取相册权限
  * 本地存储权限


