//
//  View.swift
//  react-native-video-clips
//
//  Created by 宋航 on 2021/5/26.
//

import Foundation
import AVFoundation
class OpacityButton: UIButton {
    var callBack:()->Void
    var initX:CGFloat = 0
    init(frame:CGRect,text:String,mainColor:UIColor,textColor:UIColor,callBack:@escaping ()->Void) {
        self.callBack = callBack
        super.init(frame: frame)
        self.setTitleColor(textColor, for: .normal)
        self.setTitle(text, for: .normal)
        self.backgroundColor = mainColor
        self.layer.cornerRadius = 5
        self.layer.masksToBounds = true
        self.addTarget(self, action: #selector(btnEvent(_:)), for: .touchUpInside)
        self.addTarget(self, action: #selector(btnDown(_:)), for: .touchDown)
        self.addTarget(self, action: #selector(btnExit(_:)), for: .touchDragExit)
    }
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    @objc func btnEvent(_ btn: UIButton){
        self.alpha = 1
        self.callBack()
    }
    @objc func btnDown(_ btn: UIButton){
        self.alpha = 0.8
    }
    @objc func btnExit(_ btn: UIButton){
        self.alpha = 1
    }
}
protocol SlideButtonDelegate:AnyObject {
    func changePos(type:Int)
    func startControl()
    func stopControl()
}
class SlideButton: UIView {
    var minX :CGFloat = 0.0
    var maxX :CGFloat = 0.0
    var currentX:CGFloat = 0.0
    var type:Int = 0
    var delegate:SlideButtonDelegate?
    init(frame:CGRect,type:Int) {
        self.type = type
        super.init(frame: frame)
        let w:CGFloat = 8
        let mainView = UIView.init(frame: CGRect.init(x: (frame.size.width - w) / 2, y: 0, width: w, height: frame.size.height))
        mainView.backgroundColor = .white
        addSubview(mainView)
        
        let dot1 = UIView.init(frame: CGRect.init(x: 2.5, y: (frame.size.height - 10) * 0.5, width: 1, height: 10))
        dot1.backgroundColor = .darkGray
        dot1.layer.cornerRadius = 1
        dot1.layer.masksToBounds = true
        dot1.alpha = 0.5
        
        let dot2 = UIView.init(frame: CGRect.init(x: 4.5, y: (frame.size.height - 10) * 0.5, width: 1, height: 10))
        dot2.backgroundColor = .darkGray
        dot2.layer.cornerRadius = 1
        dot2.layer.masksToBounds = true
        dot2.alpha = 0.5
        mainView.addSubview(dot1)
        mainView.addSubview(dot2)
        isUserInteractionEnabled = true
    }
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        self.delegate?.startControl()
    }
    override func touchesMoved(_ touches: Set<UITouch>, with event: UIEvent?) {
        let touch = touches.first
        let p = touch?.location(in: self)
        let p1 = touch?.previousLocation(in: self)
        let offsetX = p!.x - p1!.x
        let nextX = currentX + offsetX
        var x = self.center.x
        x += nextX
        if(x < minX){
            x = minX
        }
        else if(x > maxX){
            x = maxX
        }
        self.center.x = x
        self.delegate?.changePos(type: self.type)
        
    }
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        self.delegate?.stopControl()
    }
    override func touchesCancelled(_ touches: Set<UITouch>, with event: UIEvent?) {
        self.delegate?.stopControl()
    }
    public func setMaxX(maxX:CGFloat){
        self.maxX = maxX
    }
    public func setMinX(minX:CGFloat){
        self.minX = minX
    }
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
class ImageCell:UICollectionViewCell{
    weak var imageView:UIImageView?
    override init(frame: CGRect) {
        let imageView = UIImageView.init(frame: CGRect.zero)
        self.imageView = imageView
        super.init(frame: frame)
        addSubview(imageView)
        self.backgroundColor = .white
    }
    func setImage(image:UIImage){
        self.layer.masksToBounds = true
        self.imageView?.image = image
        let cgImage = imageView!.image?.cgImage
        let width = cgImage?.width
        let height = cgImage?.height
        let imageW = self.frame.size.height * CGFloat(width!) / CGFloat(height!)
        let x = (self.frame.size.width - imageW) * 0.5
        imageView?.frame = CGRect(x: x, y: 0, width: imageW, height: self.frame.size.height).integral
        imageView?.contentMode = .scaleAspectFit
    }
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
class ThumbnailsListView: UIView {
    static let ThumbnailsListViewID = "ThumbnailsListViewID"
    static let MaxTime = 10
    var currentPlayerTime = 0.0
    public var time:Int32 = 0
    var asset:AVURLAsset?
    var generator:AVAssetImageGenerator?
    var itemWidth:CGFloat = 0.0
    var minStep = 1
    weak var pointView:UIView?
    weak var delegate:VideoClipsDelegate?
    weak var rightBorderView:SlideButton?
    weak var leftBorderView:SlideButton?
    weak var topBorderView:UIView?
    weak var bottomBorderView:UIView?
    
    weak var contentView:UICollectionView?
    init(asset:AVURLAsset,frame:CGRect) {
        self.asset = asset
        super.init(frame: frame)
        setUpView(asset: asset)
    }
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    override func removeFromSuperview() {
        for view in self.subviews {
            view.removeFromSuperview()
        }
        self.generator = nil
    }
    func setUpView(asset:AVURLAsset) {
        let frame = self.frame
        backgroundColor = .black
        self.time = Int32(CMTimeGetSeconds(asset.duration))
        self.generator = AVAssetImageGenerator.init(asset: asset)
        self.generator?.appliesPreferredTrackTransform = true
        if(self.time >= ThumbnailsListView.MaxTime){
        }
        else{
            var num = 0
            let step = Int(Int(self.time) * 1000 / ThumbnailsListView.MaxTime)
            var total = 0
            var flag = false
            print("step",step)
            while num < ThumbnailsListView.MaxTime {
                num += 1
                total += step
                if(total > 800 && !flag) {
                    self.minStep = num
                    flag = true
                    break
                }
            }
        }
        let itemWidth =  (frame.size.width - 80) / CGFloat(ThumbnailsListView.MaxTime)
        self.itemWidth = itemWidth
        let layer = UICollectionViewFlowLayout.init()
        layer.itemSize = CGSize(width: itemWidth, height: frame.size.height)
        layer.minimumLineSpacing = 0
        layer.minimumLineSpacing = 0
        layer.sectionInset = UIEdgeInsets.init(top: 0, left: 0, bottom: 0, right: 0)
        layer.scrollDirection = .horizontal
        
        let contentView = UICollectionView.init(frame: CGRect.init(x: 40, y: 0, width: frame.size.width - 40, height: frame.size.height), collectionViewLayout: layer)
        contentView.showsHorizontalScrollIndicator = false
        contentView.dataSource = self
        contentView.backgroundColor = .black
        contentView.delegate = self
        contentView.contentInset = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 40)
        contentView.register(ImageCell.self, forCellWithReuseIdentifier: ThumbnailsListView.ThumbnailsListViewID)
        addSubview(contentView)
        self.contentView = contentView
        
        let pointView = UIView.init(frame: CGRect.init(x: 40, y: 0, width: 2, height: frame.size.height))
        pointView.backgroundColor = .white
        pointView.alpha = 0.8
        self.pointView = pointView
        addSubview(pointView)
        
        let slideWith:CGFloat = 20
        let leftBorderX:CGFloat = CGFloat(-slideWith / 2) + 40
        let width = frame.size.width - 80
        let rightBorderX:CGFloat = width + 40 + -slideWith / 2
        
        let leftBorderView = SlideButton.init(frame: CGRect.init(x: leftBorderX, y: -1, width: slideWith, height: frame.size.height + 2),type: 0)
        
        leftBorderView.delegate = self
        
        let rightBorderView = SlideButton.init( frame: CGRect.init(x: rightBorderX, y: -1, width: slideWith, height: frame.size.height + 2),type: 1)
        rightBorderView.delegate = self
        
        leftBorderView.setMinX(minX: leftBorderView.center.x)
        leftBorderView.setMaxX(maxX: rightBorderView.center.x - (itemWidth * CGFloat(minStep)))
        rightBorderView.setMinX(minX: leftBorderView.center.x + (itemWidth * CGFloat(minStep)))
        rightBorderView.setMaxX(maxX: rightBorderView.center.x)
        
        let HViewX = leftBorderView.center.x
        let HViewW = rightBorderView.center.x - leftBorderView.center.x
        let topBorderView = UIView.init(frame: CGRect.init(x: HViewX, y: -1, width:HViewW, height: 2))
        topBorderView.backgroundColor = .white
        self.topBorderView = topBorderView
        
        let bottomBorderView = UIView.init(frame: CGRect.init(x: HViewX, y: frame.size.height - 1, width: HViewW, height: 2))
        bottomBorderView.backgroundColor = .white
        self.bottomBorderView = bottomBorderView
        
        self.rightBorderView = rightBorderView
        self.leftBorderView = leftBorderView
        
        addSubview(topBorderView)
        addSubview(bottomBorderView)
        addSubview(leftBorderView)
        addSubview(rightBorderView)
        
    }
    public func startPlay(time:Float){
        self.stopPlay()
        guard let pointView = self.pointView else {
            return
        }
        self.pointView?.alpha = 1
        print("startPlay",TimeInterval(time/1000))
        var t = time / 1000
        if(t < 1){
            t = 1
        }
        UIView.animate(withDuration: TimeInterval(t), delay: 0.0, options: [.curveLinear]) {
            pointView.frame = CGRect.init(x: self.rightBorderView!.center.x, y: 0, width: 2, height: self.frame.size.height)
        } completion: { finnish in
            
        }

    }
    public func stopPlay(){
        pointView?.alpha = 0
        pointView?.layer.removeAllAnimations()
        pointView?.frame = CGRect.init(x: leftBorderView!.center.x, y: 0, width: 2, height: self.frame.size.height)
    }
    func scrollViewTime(scrollView:UIScrollView){
        let cursorX = leftBorderView!.center.x - contentView!.frame.origin.x
        let index = round((scrollView.contentOffset.x + cursorX) / self.itemWidth)
        let time =  self.delegate?.seek(time: Int(index) * 1000,isPlay: true)
        startPlay(time: Float(time!))
    }
    func getImageFromVideo(second:Int32)->UIImage?{
        print("getImageFromVideo",second / 1000)
        let time = CMTime(seconds: Double(second / 1000), preferredTimescale: 1)
        do{
            let imgRef = try self.generator?.copyCGImage(at: time, actualTime: nil)
            let image = UIImage.init(cgImage: imgRef!)
            return image
        } catch let err {
            print(err)
            return nil
        }
    }
}

extension ThumbnailsListView:UICollectionViewDataSource,UICollectionViewDelegate,SlideButtonDelegate{
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return time < 10 ? 10 :  Int(time)
    }
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: ThumbnailsListView.ThumbnailsListViewID, for: indexPath)
        let item = cell as! ImageCell
        if(time < 10){
            let step = Int(Int(self.time) * 1000 / ThumbnailsListView.MaxTime)
            let image = getImageFromVideo(second: Int32(step * indexPath.row * 1000))
            item.setImage(image: image!)
        }
        else{
            let image = getImageFromVideo(second: Int32(indexPath.row * 1000))
            item.setImage(image: image!)
        }
        return cell
    }
    func scrollViewWillBeginDragging(_ scrollView: UIScrollView) {
        self.delegate?.stopPlay()
        self.stopPlay()
    }
    func scrollViewWillEndDragging(_ scrollView: UIScrollView, withVelocity velocity: CGPoint, targetContentOffset: UnsafeMutablePointer<CGPoint>) {
        if(velocity == .zero){
            self.scrollViewTime(scrollView: scrollView)
        }
    }
    func scrollViewDidEndDecelerating(_ scrollView: UIScrollView) {
        self.scrollViewTime(scrollView: scrollView)
    }
    func scrollViewDidScroll(_ scrollView: UIScrollView) {
        // 滚动距离 + 游标距离
        let cursorX = leftBorderView!.center.x - contentView!.frame.origin.x
        let index = round((scrollView.contentOffset.x + cursorX) / self.itemWidth)
        print("index",index)
        if(index > 0){
            let _ =  self.delegate?.seek(time: Int(index) * 1000,isPlay: false)
        }
        
    }
    func changePos(type:Int) {
        let x = self.leftBorderView!.center.x
        let width = self.rightBorderView!.center.x - x
        var topFrame = self.topBorderView!.frame
        var bottomFrame = self.bottomBorderView!.frame
        topFrame.origin.x = x
        topFrame.size.width = width
        bottomFrame.origin.x = x
        bottomFrame.size.width = width
        self.topBorderView?.frame = topFrame
        self.bottomBorderView?.frame = bottomFrame
        // change left / right => maxX / minX
        leftBorderView?.setMaxX(maxX: rightBorderView!.center.x - (self.itemWidth * CGFloat(self.minStep)))
        rightBorderView?.setMinX(minX: leftBorderView!.center.x + (self.itemWidth * CGFloat(self.minStep)))
        // 如果左边变化了，则需要重置跳转时间
        if(type == 0){
            // 改变指针位置
            var frame = self.pointView!.frame
            frame.origin.x = leftBorderView!.center.x
            self.pointView!.frame = frame
            // 滚动距离 + 游标距离
            let cursorX = leftBorderView!.center.x - contentView!.frame.origin.x
            let dist = self.contentView!.contentOffset.x + cursorX
            let index = round(dist / self.itemWidth)
            // 少于 10 s & 大于 10 s 的
            if(self.time >= ThumbnailsListView.MaxTime){
                self.delegate?.setStart(time: Int(index) * 1000)
            }
            else{
                let step = Int(Int(self.time) * 1000 / ThumbnailsListView.MaxTime)
                self.delegate?.setStart(time: Int(index * CGFloat(step)))
            }
        }
        else{
            // 设置 end 位置
            let cursorX = rightBorderView!.center.x - contentView!.frame.origin.x
            let dist = self.contentView!.contentOffset.x + cursorX
            let index = round(dist / self.itemWidth)
            print("rightBorderView",index)
            if(self.time >= ThumbnailsListView.MaxTime){
                self.delegate?.setEnd(time: Int(index) * 1000)
            }
            else{
                let step = Int(Int(self.time) * 1000 / ThumbnailsListView.MaxTime)
                self.delegate?.setEnd(time: Int(index * CGFloat(step)))
            }
        }
        
    }
    func startControl() {
        self.delegate?.stopPlay()
        stopPlay()
    }
    func stopControl() {
        let time = self.delegate?.play()
        startPlay(time: Float(time!))
        
    }
}

