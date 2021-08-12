import Photos
import PhotosUI
import MobileCoreServices
import AVFoundation

@objc(VideoClips)
class VideoClips: NSObject {
    var resolve:RCTPromiseResolveBlock?
    var reject:RCTPromiseRejectBlock?
    @objc(select:withRejecter:)
    func select(resolve:@escaping RCTPromiseResolveBlock,reject:@escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.main.async {

            self.resolve = resolve
            self.reject = reject
            guard let rootViewController = RCTPresentedViewController() else {
                return
            }
            if #available(iOS 14, *) {
                var configuration = PHPickerConfiguration.init()
                configuration.preferredAssetRepresentationMode = .current
                configuration.filter = PHPickerFilter.videos
                let picker = PHPickerViewController.init(configuration: configuration)
                picker.delegate = self
                rootViewController.present(picker, animated: true) {
                }
                return
            }

            let picker = UIImagePickerController()
            picker.sourceType = .photoLibrary
            picker.mediaTypes = [kUTTypeMovie as String]
            picker.modalPresentationStyle = .fullScreen
            picker.delegate = self
            picker.allowsEditing = false
            rootViewController.present(picker, animated: true) {
            }
        }
    }
    
    @objc(compression:widthResolve:withRejecter:)
    func compression(name:String,resolve:@escaping RCTPromiseResolveBlock,reject:@escaping RCTPromiseRejectBlock) -> Void {
        print(name)
    }
    
    @objc static func requiresMainQueueSetup() -> Bool {
        return false
    }
}

extension VideoClips:UIImagePickerControllerDelegate,UINavigationControllerDelegate,PHPickerViewControllerDelegate{
    @available(iOS 14, *)
    func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
        weak var weakSelf = self
        if(results.count == 0){
            self.resolve?(["cancel": true])
            picker.dismiss(animated: true, completion: {})
        }
        else{
            let provider = results[0].itemProvider
            provider.loadFileRepresentation(forTypeIdentifier: kUTTypeMovie as String) { url, Error in
                guard let videoURL = url else {
                    return
                }
                weakSelf?.toVideoVc(videoURL: videoURL, picker: picker)
                
                
            }
        }
        
    }
    func toVideoVc(videoURL:URL,picker: UIViewController){
        guard let resolve = self.resolve else {
            return
        }
        guard let reject = self.reject else {
            return
        }
        var videoVC : VideoClipsViewController?
        let path = NSTemporaryDirectory() + videoURL.lastPathComponent
        let newURL = NSURL.fileURL(withPath: path)
        let fm = FileManager.default
        if(fm.fileExists(atPath: path)){
            do {
                try fm.removeItem(at: newURL)
            } catch let err {
                print(err)
            }
            
        }
        if(fm.isWritableFile(atPath: videoURL.path)){
            do {
                try fm.moveItem(at: videoURL, to: newURL)
            } catch let err {
                print(err)
            }
        }
        else{
            do {
                try fm.copyItem(at: videoURL, to: newURL)
                
                
            } catch let err {
                print(err)
            }
        }
        // to video
        DispatchQueue.main.async {
            videoVC =  VideoClipsViewController.init(resolve: resolve, reject: reject)
            picker.dismiss(animated: true) {
                guard let rootViewController = RCTPresentedViewController() else {
                    return
                }
                rootViewController.present(videoVC!, animated: false) {}
            }
        }
        DispatchQueue.main.sync {
            videoVC!.loadVideo(url: newURL)
        }
    }
    
    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        self.resolve?(["cancel": true])
        picker.dismiss(animated: true) {}
    }
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        weak var weakSelf = self
        let videoURL = info[UIImagePickerController.InfoKey.mediaURL] as! URL
        guard let resolve = weakSelf?.resolve else {
            return
        }
        guard let reject = weakSelf?.reject else {
            return
        }
        let path = NSTemporaryDirectory() + videoURL.lastPathComponent
        let newURL = NSURL.fileURL(withPath: path)
        let fm = FileManager.default
        if(fm.fileExists(atPath: path)){
            do {
                try fm.removeItem(at: newURL)
            } catch let err {
                print(err)
            }
            
        }
        if(fm.isWritableFile(atPath: videoURL.path)){
            do {
                try fm.moveItem(at: videoURL, to: newURL)
            } catch let err {
                print(err)
            }
        }
        else{
            do {
                try fm.copyItem(at: videoURL, to: newURL)
            } catch let err {
                print(err)
            }
        }
        DispatchQueue.main.async {
            picker.dismiss(animated: true) {
                guard let rootViewController = RCTPresentedViewController() else {
                    return
                }
                VideoClipsViewController .start(vc: rootViewController, url: videoURL, resolve: resolve, reject: reject)
            }
            
        }
    }
}
