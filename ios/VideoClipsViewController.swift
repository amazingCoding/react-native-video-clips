//
//  VideoClipsViewController.swift
//  react-native-video-clips
//
//  Created by 宋航 on 2021/5/26.
//

import Foundation
import MobileCoreServices
import AVFoundation
import Photos
import PhotosUI

protocol VideoClipsDelegate:AnyObject {
    func stopPlay()
    func seek(time:Int,isPlay:Bool)->Float64
    func setStart(time:Int)
    func play()->Float64
    func setEnd(time:Int)
}

class VideoClipsViewController:UIViewController{
    var resolve:RCTPromiseResolveBlock
    var reject:RCTPromiseRejectBlock
    var asset:AVURLAsset?
    var player:AVPlayer?
    weak var imageListView:ThumbnailsListView?
    var startRange:Float64 = 0
    var endRange:Float64 = 1.0
    var url:URL?
    var isLoad:Bool = false
    weak var loadView:UIActivityIndicatorView?
    static func start(vc:UIViewController,url:URL,resolve:@escaping RCTPromiseResolveBlock,reject:@escaping RCTPromiseRejectBlock){
        let videoVC = VideoClipsViewController.init(resolve: resolve, reject: reject)
        videoVC.url = url
        vc.present(videoVC, animated: false) {}
    }
    init(resolve:@escaping RCTPromiseResolveBlock,reject:@escaping RCTPromiseRejectBlock) {
        self.resolve = resolve
        self.reject = reject
        super.init(nibName: nil, bundle: nil)
        self.modalPresentationStyle = .fullScreen
    }
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    deinit {
        print("XXX--11")
    }
    override var prefersStatusBarHidden: Bool{
        return true
    }
    override func dismiss(animated flag: Bool, completion: (() -> Void)? = nil) {
        self.asset = nil
        if(self.player != nil){
            self.player?.pause()
        }
        self.player = nil
        self.url = nil
        self.imageListView?.removeFromSuperview()
        super.dismiss(animated: flag, completion: completion)
    }
    override func viewDidLoad() {
        self.view.backgroundColor = .clear
        self.addLoadingView()
        
    }
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated);
        
        if(!isLoad){
            guard let url = self.url else {
                return
            }
            self.loadVideo(url: url)
            self.setUpView()
        }
        isLoad = true
        
    }
    func addDoneLoad() -> UIView {
        let loadView = UIView.init(frame: self.view.bounds)
        loadView.backgroundColor = UIColor.init(red: 0/255, green: 0/255, blue: 0/255, alpha: 0.5)
        let load = UIActivityIndicatorView.init(style: .whiteLarge)
        load.center  = loadView.center
        loadView.addSubview(load)
        load.startAnimating()
        self.view.addSubview(loadView)
        return loadView
    }
    func rePlay(){
        if(self.player != nil){
            self.player?.pause()
            let time =  CMTime(seconds: Double(startRange / 1000), preferredTimescale: 1)
            self.player?.seek(to: time)
            self.player?.play()
            
            imageListView?.startPlay(time: Float(self.endRange - self.startRange))
        }
    }
    func clearView(){
        if(self.player != nil){
            self.player?.pause()
        }
        self.view.backgroundColor = .clear
        for view in self.view.subviews {
            view.removeFromSuperview()
        }
    }
    func addLoadingView(){
        self.view.backgroundColor = .black
        let load = UIActivityIndicatorView.init(style: .whiteLarge)
        load.center = self.view.center
        self.view.addSubview(load)
        load.startAnimating()
        self.loadView = load
    }
    func setUpView() {
        guard let asset = self.asset else {
            return
        }
        self.loadView?.removeFromSuperview()
        self.view.backgroundColor = UIColor.black
        var y:CGFloat = 0
        let width = self.view.frame.size.width
        var height = self.view.frame.size.height
        if #available(iOS 11, *) {
            if(UIApplication.shared.windows[0].safeAreaInsets.top > 0) {
                y = UIApplication.shared.windows[0].safeAreaInsets.top
                height -= y
            }
            if(UIApplication.shared.windows[0].safeAreaInsets.bottom > 0) {
                let b = UIApplication.shared.windows[0].safeAreaInsets.bottom
                height -= b
            }
        }// 70 + 40 + 15 + 45 = 160 + 10
        let videoView = UIView.init(frame: CGRect.init(x: 20, y: y + 10 , width:width - 40 , height:height - 170 ))
        let player = AVPlayer(url: asset.url)
        let playerLayer = AVPlayerLayer(player: player)
        playerLayer.frame = videoView.bounds
        videoView.layer.addSublayer(playerLayer)
        self.player = player
        self.view.addSubview(videoView)
        // list listView: 每秒一张图： 解析60张 - 展示 - 继续解析图片到最后
        let imageListViewFrame = CGRect.init(x: 0, y: videoView.frame.maxY + 30, width: width, height: 70)
        let imageListView = ThumbnailsListView.init(asset: self.asset!,frame: imageListViewFrame)
        self.imageListView = imageListView
        self.view.addSubview(imageListView)
        
        // btn
        weak var weakSelf = self
        let cancelBtn = OpacityButton.init(frame: CGRect.init(x: 10, y: imageListViewFrame.maxY + 20, width: 80, height: 40), text: "Cancel", mainColor: .clear, textColor: .white) {
            weakSelf?.player?.pause()
            weakSelf?.player = nil
            weakSelf?.dismiss(animated: true, completion: {
                
            })
        }
        self.view.addSubview(cancelBtn)
        
        let okBtn = OpacityButton.init(frame: CGRect.init(x: width - 90, y: imageListViewFrame.maxY + 20, width:80, height: 40), text: "Done", mainColor: UIColor.init(red: 88/255, green: 189/255, blue: 106/255, alpha: 1), textColor: .white) {
            weakSelf?.stopPlay()
            weakSelf?.imageListView?.stopPlay()
            let load = weakSelf?.addDoneLoad()
            weakSelf?.export(load: load)
        }
        self.view.addSubview(okBtn)
        
        // player
        player.addPeriodicTimeObserver(forInterval: CMTimeMakeWithSeconds(1, preferredTimescale: Int32(1.0)), queue: nil) { time in
            let item = weakSelf?.player?.currentItem
            let loadedRanges = item?.seekableTimeRanges
            if(loadedRanges == nil) { return }
            if(loadedRanges!.count > 0){
                let range = loadedRanges![0].timeRangeValue
                let duration = CMTimeGetSeconds(range.start) + CMTimeGetSeconds(range.duration)
                let current = CMTimeGetSeconds(time)
                
                if(current >= duration || current >= (self.endRange / 1000)){
                    weakSelf?.rePlay()
                }
//                print(current,duration,self.endRange / 1000,current >= self.endRange / 1000)
            }
        }
        if(imageListView.time < 10){
            setEnd(time: Int(imageListView.time) * 1000)
        }
        else{
            setEnd(time: 10 * 1000)
        }
        player.play()
        imageListView.delegate = self
        imageListView.startPlay(time: Float(self.endRange))
        
    }
    func export(load:UIView?){
        weak var weakSelf = self
        guard let asset = self.asset else {
            return
        }
        let newName = UUID.init().uuidString
        let newPath = NSTemporaryDirectory() + newName + ".mov"
        let outputUrl = URL.init(fileURLWithPath: newPath)
        let startcm = CMTime(seconds: Double(self.startRange / 1000), preferredTimescale: 1)
        let len = self.endRange - self.startRange
        let durationcm = CMTime(seconds: Double(len / 1000), preferredTimescale: 1)
        
        let trackTimeRange = CMTimeRangeMake(start: startcm, duration: durationcm)
        print("myTime",self.startRange / 1000 ,self.endRange / 1000,len / 1000)
        // 导出
        // AVAssetExportPresetPassthrough 9.9M
        // AVAssetExportPresetMediumQuality 0.7M
        guard let exportSession = AVAssetExportSession.init(asset: asset, presetName: AVAssetExportPresetMediumQuality) else {
            return
        }
        exportSession.outputURL = outputUrl
        exportSession.outputFileType = .mov
        exportSession.shouldOptimizeForNetworkUse = true
        
        exportSession.timeRange = trackTimeRange
        exportSession.exportAsynchronously {
            DispatchQueue.main.async {
                let status = exportSession.status
                switch status {
                case .failed:
                    load?.removeFromSuperview()
                    weakSelf?.player?.pause()
                    weakSelf?.player = nil
                    weakSelf?.reject("cancelled", "cancelled", nil)
                    self.dismiss(animated: true) {
                        self.clearView()
                    }
                    break
                case .cancelled:
                    weakSelf?.player?.pause()
                    weakSelf?.player = nil
                    load?.removeFromSuperview()
                    weakSelf?.reject("cancelled", "cancelled", nil)
                    weakSelf?.dismiss(animated: true) {
                        weakSelf?.clearView()
                    }
                    
                    break
                case .completed:
                    weakSelf?.player?.pause()
                    weakSelf?.player = nil
                    // 获取缩略图
                    var thum = ""
                    // 获取视频路径
                    let newAsset = AVURLAsset.init(url: outputUrl)
                    let generator = AVAssetImageGenerator.init(asset: newAsset)
                    do{
                        let res =  try FileManager.default.attributesOfItem(atPath: outputUrl.path)
                        let size = res[FileAttributeKey.size] as! Double
                        print("fileSize:",size / (1024 * 1024))
                    }
                    catch let err{
                        print(err)
                    }
                    generator.appliesPreferredTrackTransform = true
                    let time = CMTime(value: 1, timescale: 60)
                    do{
                        let imgRef = try generator.copyCGImage(at: time, actualTime: nil)
                        let image = UIImage.init(cgImage: imgRef)
                        let data = image.jpegData(compressionQuality: 1.0)
                        let outThumPath = NSTemporaryDirectory() + newName + ".jpg"
                        FileManager.default.createFile(atPath: outThumPath, contents: data, attributes: nil)
                        thum = "file://" + outThumPath
                    } catch let err {
                        print(err)
                    }
//                    PHPhotoLibrary.shared().performChanges({
//                                PHAssetChangeRequest.creationRequestForAssetFromVideo(atFileURL: outputUrl)
//                    }) { flag, error in
//
//                    }
                    // 返回参数
                    // 退出
                    weakSelf?.resolve([
                        "url":outputUrl.absoluteString,
                        "thum":thum,
                    ])
                    load?.removeFromSuperview()

                    weakSelf?.dismiss(animated: true) {
                        weakSelf?.clearView()
                    }
                    
                    break
                default:
                    break
                }
            }
        }
    }
    public func loadVideo(url:URL){
        self.asset = AVURLAsset.init(url: url, options: nil)
        self.url = url
        if(isLoad) {
            self.setUpView()
        }
    }
    
}
extension VideoClipsViewController:VideoClipsDelegate{
    func stopPlay() {
        self.player?.pause()
    }
    func play()->Float64{
        self.player?.play()
        return self.endRange - self.startRange
    }
    func setStart(time:Int){
        self.startRange = Float64(time)
        let time = CMTime(seconds: Double(startRange / 1000), preferredTimescale: 1)
        print("seek",startRange,endRange)
        // 保持最小 1 s 播放距离
        var len = (self.endRange - self.startRange)
        if(len < 1000) {
            len = 1000
            self.endRange = self.startRange + len
        }
        
        self.player?.seek(to: time)
    }
    func seek(time: Int,isPlay:Bool) -> Float64 {
        var len = (self.endRange - self.startRange)
        if(len < 1000) { len = 1000 }
        self.startRange = Float64(time)
        self.endRange = self.startRange + len
        print("seek",len,startRange,endRange)
        if isPlay {
            self.rePlay()
        }
        else{
            let time = CMTime(seconds: Double(startRange / 1000), preferredTimescale: 1)
            self.player?.seek(to: time)
        }
        return self.endRange - self.startRange
    }
    func setEnd(time: Int){
        self.endRange = Float64(time)
        print("self.endRange",self.endRange)
    }
}
