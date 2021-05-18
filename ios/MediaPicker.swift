import UIKit
import Photos

@available(iOS 11, *)
@objc (MediaPicker)
class MediaPicker: NSObject {
    
    private var rootViewController: UIViewController?
    private var resolver: RCTPromiseResolveBlock?
    private var rejector: RCTPromiseRejectBlock?
    private var maxFileSize: Int64?
    
    @objc
    static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    func openPickerView(params: NSDictionary) {
        
        maxFileSize = params.object(forKey: "maxAllowedBytes") as! Int64?
        let maxFiles: Int? = params.object(forKey: "maxFiles") as! Int?
        
        let isTCSupport: Bool = params.object(forKey: "tcSupport") as! Bool
        
        DispatchQueue.main.async { [weak self] in
            let picker: MediaPickerViewController = MediaPickerViewController.init()
            picker.initMediaPickerViewController()
            picker.isTCSupport = isTCSupport
            picker.maxFiles = maxFiles ?? 0
            picker.maxFileSize = self?.maxFileSize ?? 0
            picker.resolver = self?.resolver
            picker.rejector = self?.rejector
            picker.modalPresentationStyle = .fullScreen
            let rootViewController = UIApplication.shared.windows.first?.rootViewController
            rootViewController?.present(picker, animated: true, completion: nil)
        }
    }
    
    @objc
    func openGallery(_ params: NSDictionary,
                                resolver resolve: @escaping RCTPromiseResolveBlock,
                                rejecter reject: @escaping RCTPromiseRejectBlock) {
        
        resolver = resolve
        rejector = reject
        
        let photos = PHPhotoLibrary.authorizationStatus()
        if photos == .notDetermined {
            PHPhotoLibrary.requestAuthorization({ [weak self]status in
                if status == .authorized{
                    self?.openPickerView(params: params)
                } else {
                    PHPhotoLibrary.requestAuthorization({
                        (newStatus) in
                        if newStatus ==  PHAuthorizationStatus.authorized {
                            self?.openPickerView(params: params)
                        } else {
                            reject("-1", "Permission denied!", nil)
                        }
                    })
                }
            })
        } else {
            openPickerView(params: params)
        }
    }
}


