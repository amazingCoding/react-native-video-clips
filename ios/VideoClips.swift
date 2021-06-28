@objc(VideoClips)
class VideoClips: NSObject {

    @objc(select:withRejecter:)
    func select(resolve:@escaping RCTPromiseResolveBlock,reject:@escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.main.async {
            let rootViewController = RCTPresentedViewController()
            VideoClipsViewController.start(vc: rootViewController!, resolve: resolve, reject: reject)
        }
    }
    
    @objc static func requiresMainQueueSetup() -> Bool {
        return false
    }
}
