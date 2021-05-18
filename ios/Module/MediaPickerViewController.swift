//
//  NavigationViewController.swift
//  MediaPicker
//
//  Created by Alex Nguyen on 15/04/2021.
//  Copyright © 2021 Facebook. All rights reserved.
//
import Photos
import UIKit

@available(iOS 11, *)
class MediaPickerViewController: UIViewController {
    
    var assetCollectionSubtypes: NSArray = [
        PHAssetCollectionSubtype.smartAlbumUserLibrary.rawValue,
        PHAssetCollectionSubtype.albumMyPhotoStream.rawValue,
        PHAssetCollectionSubtype.smartAlbumPanoramas.rawValue,
        PHAssetCollectionSubtype.smartAlbumBursts.rawValue
    ];
    
    var resolver: RCTPromiseResolveBlock?
    var rejector: RCTPromiseRejectBlock?
    var maxFileSize: Int64!
    var maxFiles: Int!
    var isTCSupport: Bool = false
    
    var albumsNavigationController: UINavigationController?
    var assetBundle: Bundle?
    
    func initMediaPickerViewController() {
        assetBundle = Bundle(for: type(of: self))
        let bundlePath: String = (assetBundle?.path(forResource: "MediaPicker", ofType: "bundle"))!
        if (bundlePath.isEmpty) {
            assetBundle = Bundle(path: bundlePath)
        }
        
        let storyboard: UIStoryboard = UIStoryboard(name: "MediaPickerSB", bundle: assetBundle)
        let navigationController: UINavigationController = storyboard.instantiateViewController(withIdentifier: "MediaPickerNavController") as! UINavigationController
        addChild(navigationController)
        navigationController.view.frame = view.bounds
        view.addSubview(navigationController.view)
        navigationController.didMove(toParent: self)
        albumsNavigationController = navigationController
        
        let vc: TAlbumsViewController = self.albumsNavigationController?.topViewController as! TAlbumsViewController
        vc.mediaPickerViewController = self
    }
    
    public func cancel() {
        dismiss(animated: true, completion: nil)
    }
    
    public func didFinishSelection(_ sender: TPhotosViewController) {
        let selectedPhotos = sender.selectedPhotos
        processPhotos(imageIdentifies: selectedPhotos, sender: sender)
    }
    
    @objc
    func processPhotos(imageIdentifies: [String], sender: TPhotosViewController) {
        autoreleasepool { () -> Void in
            let imageManager = PHImageManager.default()
            let fileManager: FileManager = FileManager.default
            let documentDirPath: String = NSTemporaryDirectory()
            var filePath: String?
            var results: [NSMutableDictionary] = []
            
            let assetResults = PHAsset.fetchAssets(withLocalIdentifiers: imageIdentifies, options: nil)
            
            let group = DispatchGroup()
            assetResults.enumerateObjects { [weak self] (photo, _, _) in
                let option = PHImageRequestOptions()
                option.deliveryMode = .highQualityFormat
                option.isNetworkAccessAllowed = true
                option.resizeMode = .exact
                
                let originalRatio = CGFloat(photo.pixelHeight) / CGFloat(photo.pixelWidth)
                let imageBytesCount = photo.pixelHeight * photo.pixelWidth * 4
                let ratio = sqrt(Double(self?.maxFileSize ?? Int64(0))/Double(imageBytesCount))
                var newSize: CGSize = CGSize(width: photo.pixelWidth, height: photo.pixelHeight)
                
                if(imageBytesCount > self?.maxFileSize ?? Int64(0)){
                    let newWidth = Int(CGFloat(photo.pixelWidth) * CGFloat(ratio))
                    let newHeight = Int(CGFloat(newWidth) * CGFloat(originalRatio))
                    newSize = CGSize(width: newWidth, height: newHeight)
                }
                
                group.enter()
                imageManager.requestImage(for: photo, targetSize: newSize, contentMode: .aspectFit, options: option, resultHandler: {( result: UIImage?, info)-> Void in
                    guard let data = result?.pngData() else {
                        NSLog("ERROR: requestImage" )
                        group.leave()
                        return
                    }
                    filePath = String(format: "%@%@.jpg", documentDirPath, ProcessInfo().globallyUniqueString)
                    fileManager.createFile(atPath: filePath!, contents: data, attributes: nil)
                    #if targetEnvironment(simulator)
                    let res:NSMutableDictionary = [:]
                    res["path"] = filePath!
                    res["size"] = data.count
                    res["width"] = newSize.width
                    res["height"] = newSize.height
                    results.append(res)
                    #else
                    let res:NSMutableDictionary = [:]
                    res["path"] = "file:/" + filePath!
                    res["size"] = data.count
                    res["width"] = newSize.width
                    res["height"] = newSize.height
                    results.append(res)
                    #endif
                    group.leave()
                })
            }
            
            group.notify(queue: DispatchQueue.main) {
                if sender.isCancel == false{
                    self.resolver!(results)
                } else {
                    self.rejector!("0", "User cancel", nil)
                }
                
                self.dismiss(animated: true, completion: nil)
            }
        }
    }
}
