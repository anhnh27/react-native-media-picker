//
//  TAlbumsViewController.swift
//  MediaPicker
//
//  Created by Alex Nguyen on 15/04/2021.
//  Copyright © 2021 Facebook. All rights reserved.
//

import UIKit
import Photos

@available(iOS 11, *)
class TAlbumsViewController: UIViewController {
    
    fileprivate let imageManager = PHCachingImageManager()
    
    weak var mediaPickerViewController: MediaPickerViewController?
    
    var isCancel: Bool = false
    var fetchResults: NSMutableArray = []
    var assetCollections: NSArray = []
    var collectionViewFlowLayout: UICollectionViewFlowLayout!
    var loadingIndicator: UIActivityIndicatorView!
    
    let margin: CGFloat = 10
    
    private var targetSize: CGSize {
        let numberOfItemsInRow: CGFloat = 2
        let innerItemSpacing: CGFloat = margin
        let width = ((albumsCollectionView.frame.width - innerItemSpacing * (numberOfItemsInRow - 1) - 20) / numberOfItemsInRow).rounded(.towardZero)
        
        return CGSize(width: width, height: width)
    }
    
    @IBOutlet weak var btnCancel: UIBarButtonItem!
    @IBOutlet weak var albumsCollectionView: UICollectionView!
    @IBOutlet weak var manageAccessView: UIView!
    @IBOutlet weak var manageAccessLabel: UILabel!
    @IBOutlet weak var btnManage: UIButton!
    
    @IBAction func btnManageOnPress(_ sender: Any) {
        if let settingsUrl = URL(string: UIApplication.openSettingsURLString) {
            UIApplication.shared.open(settingsUrl, options: [:], completionHandler: { [self] (success) in
                isCancel = true
                mediaPickerViewController!.cancel()
            })
        }
    }
    
    @IBAction func onCancel(_ sender: Any) {
        mediaPickerViewController!.cancel()
    }
    
    deinit {
        PHPhotoLibrary.shared().unregisterChangeObserver(self)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationItem.title = self.mediaPickerViewController!.isTCSupport ? MY_ALBUMS_KEY.localized : MY_ALBUMS_VALUE
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        albumsCollectionView.contentInset = UIEdgeInsets(top: margin, left: margin, bottom: margin, right: margin)
        manageAccessView.isHidden = true
        setUpToolbarItems()
        
        let smartAlbums: PHFetchResult = PHAssetCollection.fetchAssetCollections(with: .smartAlbum, subtype: .any, options: nil)
        let userAlbums: PHFetchResult = PHAssetCollection.fetchAssetCollections(with: .album, subtype: .any, options: nil)
        fetchResults = [smartAlbums, userAlbums];
        
        initAlbumsCollectionView()
        
        updateAssetCollections()
        
        PHPhotoLibrary.shared().register(self)
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        let photosViewController: TPhotosViewController = segue.destination as! TPhotosViewController
        photosViewController.mediaPickerViewController = mediaPickerViewController
        photosViewController.view.backgroundColor = .white
        let index: Int? = albumsCollectionView.indexPathsForSelectedItems?.first?.row
        photosViewController.assetCollection = assetCollections[index!] as? PHAssetCollection
    }
    
    func setUpToolbarItems() -> Void {
        let leftSpace: UIBarButtonItem = UIBarButtonItem.init(barButtonSystemItem: .flexibleSpace, target: nil, action: nil)
        let rightSpace: UIBarButtonItem = UIBarButtonItem.init(barButtonSystemItem: .flexibleSpace, target: nil, action: nil)
        
        // Info label
        let attributes: NSDictionary = [NSAttributedString.Key.foregroundColor: UIColor.black ]
        
        let infoButtonItem: UIBarButtonItem = UIBarButtonItem.init(title: "", style: UIBarButtonItem.Style.plain, target: nil, action: nil)
        infoButtonItem.isEnabled = false;
        infoButtonItem.setTitleTextAttributes((attributes as! [NSAttributedString.Key : Any]), for: UIControl.State.normal)
        infoButtonItem.setTitleTextAttributes((attributes as! [NSAttributedString.Key : Any]), for: UIControl.State.disabled)
        
        self.toolbarItems = [leftSpace, infoButtonItem, rightSpace];
        
        
    }
    
    func updateAssetCollections() {
        loadingIndicator = UIActivityIndicatorView(style: .gray)
        loadingIndicator.center = view.center
        view.addSubview(loadingIndicator)
        loadingIndicator.hidesWhenStopped = true
        loadingIndicator.startAnimating()
        DispatchQueue.global(qos: .userInitiated).async {
            let assetCollectionSubtypes: NSArray = self.mediaPickerViewController!.assetCollectionSubtypes
            let smartAlbums: NSMutableDictionary = NSMutableDictionary.init(capacity: assetCollectionSubtypes.count)
            let userAlbums: NSMutableArray = NSMutableArray.init()
            
            for fetchResult in self.fetchResults {
                (fetchResult as! PHFetchResult<PHAssetCollection>).enumerateObjects({(assetCollection, index, stop) in
                    let subtype: PHAssetCollectionSubtype = assetCollection.assetCollectionSubtype
                    
                    if (subtype.rawValue == PHAssetCollectionSubtype.albumRegular.rawValue) {
                        userAlbums.add(assetCollection)
                    } else if(assetCollectionSubtypes.contains(subtype.rawValue)) {
                        let assetCollectionObj: PHAssetCollection = assetCollection
                        if(assetCollectionObj.photosCount > 0){
                            smartAlbums.mutableArrayValue(forKey: String(subtype.rawValue)).add(assetCollection)
                        }
                    }
                })
            }
            
            let assetCollections: NSMutableArray = NSMutableArray.init()
            for assetCollectionSubtype in assetCollectionSubtypes {
                let collections:NSArray = smartAlbums.mutableArrayValue(forKey: "\(assetCollectionSubtype)")
                if (collections.count > 0) {
                    assetCollections.addObjects(from: collections as! [Any])
                }
            }
            
            userAlbums.enumerateObjects({(assetCollection, index, stop) in
                let assetCollectionObj: PHAssetCollection = assetCollection as! PHAssetCollection
                if(assetCollectionObj.photosCount > 0){
                    assetCollections.add(assetCollection)
                }
            })
            
            self.assetCollections = assetCollections
            
            DispatchQueue.main.async {
                self.loadingIndicator.stopAnimating()
                self.albumsCollectionView.reloadData()
            }
        }
    }
    
    private func initAlbumsCollectionView(){
        collectionViewFlowLayout = UICollectionViewFlowLayout()
        collectionViewFlowLayout.scrollDirection = .vertical
        
        albumsCollectionView.delegate = self
        albumsCollectionView.dataSource = self
        albumsCollectionView.setCollectionViewLayout(collectionViewFlowLayout, animated: true)
        albumsCollectionView.showsVerticalScrollIndicator = false
        albumsCollectionView.delaysContentTouches = false
        
        btnManage.layer.cornerRadius = 12
        btnManage.layer.borderWidth = 1
        btnManage.layer.borderColor = UIColor.systemBlue.cgColor
        btnManage.layer.backgroundColor = UIColor.systemBlue.cgColor
        btnManage.layer.masksToBounds = true
        btnManage.titleEdgeInsets = UIEdgeInsets(top: 4,left: 4,bottom: 4,right: 4)
        btnManage.setTitleColor(UIColor.white, for: .normal)
        btnManage.setTitle(self.mediaPickerViewController!.isTCSupport ? MANAGE_KEY.localized : MANAGE, for: .normal)
        
        manageAccessLabel.text = self.mediaPickerViewController!.isTCSupport ? LIMITED_PHOTO_ACCESS_MESSAGE_KEY.localized : LIMITED_PHOTO_ACCESS_MESSAGE
        
        if #available(iOS 14, *) {
            PHPhotoLibrary.requestAuthorization(for: .readWrite) { status in
                switch status {
                case .authorized:
                    DispatchQueue.main.async { [self] in
                        manageAccessView.isHidden = true
                    }
                    break;
                case .denied, .restricted, .notDetermined:
                    DispatchQueue.main.async { [self] in
                        manageAccessView.isHidden = false
                        manageAccessLabel.text = mediaPickerViewController!.isTCSupport ? PLEASE_ALLOW_ACCESS_TO_PHOTOS_KEY.localized : PLEASE_ALLOW_ACCESS_TO_PHOTOS
                    }
                    break;
                case .limited:
                    DispatchQueue.main.async { [self] in
                        manageAccessView.isHidden = false
                        manageAccessLabel.text = mediaPickerViewController!.isTCSupport ? LIMITED_PHOTO_ACCESS_MESSAGE_KEY.localized : LIMITED_PHOTO_ACCESS_MESSAGE
                    }
                    break;
                default:
                    DispatchQueue.main.async { [self] in
                        manageAccessView.isHidden = true
                    }
                    break;
                }
                
            }
        } else {
            let status = PHPhotoLibrary.authorizationStatus()
            switch status {
            case .authorized:
                DispatchQueue.main.async { [self] in
                    manageAccessView.isHidden = true
                }
                break;
            case .denied, .restricted, .notDetermined:
                DispatchQueue.main.async { [self] in
                    manageAccessView.isHidden = false
                    manageAccessLabel.text = mediaPickerViewController!.isTCSupport ? PLEASE_ALLOW_ACCESS_TO_PHOTOS_KEY.localized : PLEASE_ALLOW_ACCESS_TO_PHOTOS
                }
                break;
            default:
                DispatchQueue.main.async { [self] in
                    manageAccessView.isHidden = true
                }
                break;
            }
        }
    }
}

@available(iOS 11, *)
extension TAlbumsViewController: UICollectionViewDataSource {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return assetCollections.count
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: ALBUM_CELL_IDENTIFIER, for: indexPath) as! AlbumCollectionViewCell
        cell.tag = indexPath.row
        
        let assetCollection: PHAssetCollection = assetCollections[indexPath.row] as! PHAssetCollection
        let options: PHFetchOptions = PHFetchOptions()
        options.predicate = NSPredicate(format: "mediaType = %d", PHAssetMediaType.image.rawValue)
        
        let fetchResult: PHFetchResult = PHAsset.fetchAssets(in: assetCollection, options: options)
        
        cell.title.text = assetCollection.localizedTitle
        cell.totalPhotos.text = String(format: self.mediaPickerViewController!.isTCSupport ? "number_of_photos".localized : "%@ photo(s)", String(fetchResult.count))
        
        if(fetchResult.count == 0) {
            cell.totalPhotos.text = ""
            return cell
        }
        
        imageManager.requestImage(for: fetchResult[fetchResult.count - 1], targetSize: CGSize(width: targetSize.width * UIScreen.main.nativeScale, height: targetSize.height * UIScreen.main.nativeScale), contentMode: PHImageContentMode.aspectFill, options: nil) { (image, info) -> Void in
            if cell.tag == indexPath.row {
                cell.thumbnail.image = image
            }
        }
        return cell
    }
}

@available(iOS 11, *)
extension TAlbumsViewController : UICollectionViewDelegateFlowLayout {
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        return targetSize
    }
}

@available(iOS 11, *)
extension TAlbumsViewController: PHPhotoLibraryChangeObserver {
    
    func photoLibraryDidChange(_ changeInstance: PHChange) {
        DispatchQueue.main.async {
            // Update fetch results
            let fetchResults: NSMutableArray = self.fetchResults.mutableCopy() as! NSMutableArray
            
            self.fetchResults.enumerateObjects({[weak self] (fetchResult, index, stop) in
                guard let changeDetails: PHFetchResultChangeDetails = changeInstance.changeDetails(for: fetchResult as! PHFetchResult<PHObject>) else { return }
                
                if (changeDetails.hasIncrementalChanges) {
                    self?.fetchResults.replaceObject(at: index, with: changeDetails.fetchResultAfterChanges)
                }
            })
            
            if (!self.fetchResults.isEqual(to: fetchResults as! [Any])) {
                self.fetchResults = fetchResults;
                
                // Reload albums
                self.updateAssetCollections()
                self.albumsCollectionView.reloadData()
            }
        }
    }
}

@available(iOS 11, *)
extension String {
    var localized: String {
        let path = Bundle(for: TAlbumsViewController.self).path(forResource: "MediaPicker", ofType: "bundle")!
        let bundle = Bundle(path: path) ?? Bundle.main
        return NSLocalizedString(self, bundle: bundle, comment: "")
    }
}

extension PHAssetCollection {
    var photosCount: Int {
        let fetchOptions = PHFetchOptions()
        fetchOptions.predicate = NSPredicate(format: "mediaType == %d", PHAssetMediaType.image.rawValue)
        let result = PHAsset.fetchAssets(in: self, options: fetchOptions)
        return result.count
    }
}
