//
//  TPhotosViewController.swift
//  MediaPicker
//
//  Created by Alex Nguyen on 21/04/2021.
//  Copyright © 2021 Facebook. All rights reserved.
//

import UIKit
import Photos

@available(iOS 11, *)
class TPhotosViewController: UIViewController {

    fileprivate let imageManager = PHCachingImageManager()
    
    weak var mediaPickerViewController: MediaPickerViewController?
    weak var assetCollection: PHAssetCollection?
    
    @IBOutlet weak var photosCollectionView: UICollectionView!
    @IBOutlet weak var btnDone: UIButton!
    @IBOutlet weak var btnClearAll: UIButton!
    @IBOutlet weak var selectedPhotoText: UILabel!
    
    var selectedPhotos: [String] = []
    var isCancel: Bool = false
    
    private var fetchResult: PHFetchResult<PHAsset>!
    private var collectionViewFlowLayout: UICollectionViewFlowLayout!
    private var photosCollectionViewHeight: Int!
    
    @IBAction func onCancel(_ sender: Any) {
        mediaPickerViewController!.cancel()
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        navigationItem.title = assetCollection?.localizedTitle
        initPhotoCollectionView()
        PHPhotoLibrary.shared().register(self)
    }
    
    private func initPhotoCollectionView(){
        
        collectionViewFlowLayout = UICollectionViewFlowLayout()
        collectionViewFlowLayout.scrollDirection = .vertical

        photosCollectionView.delegate = self
        photosCollectionView.dataSource = self
        photosCollectionView.showsVerticalScrollIndicator = false
        photosCollectionView.allowsMultipleSelection = true
        photosCollectionView.setCollectionViewLayout(collectionViewFlowLayout, animated: true)
        
        btnClearAll.layer.cornerRadius = 4
        btnClearAll.layer.borderWidth = 1
        btnClearAll.layer.borderColor = #colorLiteral(red: 0, green: 0.5964992642, blue: 1, alpha: 1)
        btnClearAll.layer.backgroundColor = #colorLiteral(red: 0.9999960065, green: 1, blue: 1, alpha: 1).cgColor
        btnClearAll.layer.masksToBounds = true
        
        btnDone.layer.cornerRadius = 4
        btnDone.layer.backgroundColor = #colorLiteral(red: 0, green: 0.5964992642, blue: 1, alpha: 1)
        btnDone.setTitleColor(.white, for: .normal)

        btnClearAll.addTarget(self, action: #selector(clearAll(sender:)), for: .touchUpInside)
        btnDone.addTarget(self, action: #selector(done(sender:)), for: .touchUpInside)
        btnDone.setTitle(mediaPickerViewController!.isTCSupport ? DONE_KEY.localized : DONE, for: .normal)
        btnClearAll.setTitle(mediaPickerViewController!.isTCSupport ? CLEAR_ALL_KEY.localized : CLEAR_ALL, for: .normal)
        
        DispatchQueue.main.async {
            self.selectedPhotoText.text = self.getNumberOfSelectedPhotos()
        }
        
        updateFetchResults()
    }
    
    private func updateFetchResults() {
        let options: PHFetchOptions = PHFetchOptions()
        options.sortDescriptors = [NSSortDescriptor(key: "creationDate", ascending: false)]
        options.predicate = NSPredicate.init(format: "mediaType == %d", PHAssetMediaType.image.rawValue)
        
        fetchResult = PHAsset.fetchAssets(in: assetCollection!, options: options)
    }
    
    private func getCellWidth() -> CGFloat {
        let numberOfItemsInRow: CGFloat = 3
        let innerItemSpacing: CGFloat = 1
        return ((photosCollectionView.frame.width - innerItemSpacing * numberOfItemsInRow) / numberOfItemsInRow)
    }
}

@available(iOS 11, *)
fileprivate extension TPhotosViewController {
 
    @objc private func clearAll(sender: UIButton!) {
        selectedPhotos = []
        let indexPaths = photosCollectionView.indexPathsForSelectedItems
        for indexPath in indexPaths! {
            photosCollectionView.deselectItem(at: indexPath, animated: true)
        }
        DispatchQueue.main.async {
            self.selectedPhotoText.text = self.getNumberOfSelectedPhotos()
        }
    }
    
    func getNumberOfSelectedPhotos() -> String {
        return String(format: mediaPickerViewController!.isTCSupport ? NUMBER_OF_PHOTOS_SELECTED_KEY.localized : NUMBER_OF_PHOTOS_SELECTED, String(selectedPhotos.count))
    }
    
    @objc func done(sender: TPhotosViewController) {
        mediaPickerViewController!.didFinishSelection(self)
    }
}
@available(iOS 11, *)
extension TPhotosViewController: UICollectionViewDelegate {
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        
        selectedPhotos.append(fetchResult[indexPath.row].localIdentifier)
        DispatchQueue.main.async {
            self.selectedPhotoText.text = self.getNumberOfSelectedPhotos()
        }
    }
    
    func collectionView(_ collectionView: UICollectionView, didDeselectItemAt indexPath: IndexPath) {
        let index = selectedPhotos.firstIndex(of: fetchResult![indexPath.row].localIdentifier)
        selectedPhotos.remove(at: index!)
        DispatchQueue.main.async {
            self.selectedPhotoText.text = self.getNumberOfSelectedPhotos()
        }
    }
    
    func collectionView(_ collectionView: UICollectionView, shouldSelectItemAt indexPath: IndexPath) -> Bool {
        if(selectedPhotos.count > mediaPickerViewController!.maxFiles - 1) {
            let message = String(format: mediaPickerViewController!.isTCSupport ? PHOTO_LIMITATION_WARNING_KEY.localized : PHOTO_LIMITATION_WARNING, String(mediaPickerViewController!.maxFiles))
            showToast(message)
            return false
        }
        return true
    }
}

@available(iOS 11, *)
extension TPhotosViewController : UICollectionViewDataSource {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return fetchResult.count
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: PHOTO_CELL_IDENTIFIER, for: indexPath) as! PhotoCollectionViewCell
        cell.tag = indexPath.row
        let targetSize = CGSize(width: cell.frame.width * UIScreen.main.scale, height: cell.frame.height * UIScreen.main.scale)
        imageManager.requestImage(for: fetchResult[indexPath.row], targetSize: targetSize, contentMode: .aspectFill, options: nil) { (image, info) -> Void in
            if cell.tag == indexPath.row {
                let isDegraded = (info?[PHImageResultIsDegradedKey] as? Bool) ?? false
                if isDegraded {
                    return
                }
                cell.thumbnail.image = image
            }
        }
        return cell
    }
}

@available(iOS 11, *)
extension TPhotosViewController : UICollectionViewDelegateFlowLayout {
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        let width = getCellWidth()
        return CGSize(width: width, height: width)
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumInteritemSpacingForSectionAt section: Int) -> CGFloat {
        return 1.0
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumLineSpacingForSectionAt section: Int) -> CGFloat {
        return 1.0
    }
}

@available(iOS 11, *)
extension TPhotosViewController: UICollectionViewDataSourcePrefetching {
    func collectionView(_ collectionView: UICollectionView, prefetchItemsAt indexPaths: [IndexPath]) {
        let numberOfItemsInRow: CGFloat = 3
        let innerItemSpacing: CGFloat = 1
        let width = (collectionView.frame.width - innerItemSpacing * numberOfItemsInRow) / numberOfItemsInRow
        DispatchQueue.main.async {
            self.imageManager.startCachingImages(for: indexPaths.map{ self.fetchResult!.object(at: $0.item) }, targetSize: CGSize(width: width, height: width), contentMode: .aspectFill, options: nil)
        }
    }
    
    func collectionView(_ collectionView: UICollectionView, cancelPrefetchingForItemsAt indexPaths: [IndexPath]) {
        let numberOfItemsInRow: CGFloat = 3
        let innerItemSpacing: CGFloat = 1
        let width = (collectionView.frame.width - innerItemSpacing * numberOfItemsInRow) / numberOfItemsInRow
        self.imageManager.stopCachingImages(for: indexPaths.map{ self.fetchResult!.object(at: $0.item) }, targetSize: CGSize(width: width, height: width), contentMode: .aspectFill, options: nil)
    }
}


@available(iOS 11, *)
extension TPhotosViewController: PHPhotoLibraryChangeObserver {
    func photoLibraryDidChange(_ changeInstance: PHChange) {
        guard let changes = changeInstance.changeDetails(for: self.fetchResult)
        else { return }
        self.fetchResult = changes.fetchResultAfterChanges
        DispatchQueue.main.async {
            if changes.hasIncrementalChanges {
                self.photosCollectionView.performBatchUpdates({
                    if let removed = changes.removedIndexes, !removed.isEmpty {
                        self.photosCollectionView.deleteItems(at: removed.map({ IndexPath(item: $0, section: 0) }))
                    }
                    
                    if let inserted = changes.insertedIndexes, !inserted.isEmpty {
                        self.photosCollectionView.insertItems(at: inserted.map({ IndexPath(item: $0, section: 0) }))
                    }
                    
                    changes.enumerateMoves { fromIndex, toIndex in
                        self.photosCollectionView.moveItem(at: IndexPath(item: fromIndex, section: 0),
                                                     to: IndexPath(item: toIndex, section: 0))
                    }
                })
                
                if let changed = changes.changedIndexes, !changed.isEmpty {
                    self.photosCollectionView.reloadItems(at: changed.map({ IndexPath(item: $0, section: 0) }))
                }
                
            } else {
                self.photosCollectionView.reloadData()
            }
        }
    }
}

class ToastLabel: UILabel {
    var textInsets = UIEdgeInsets.zero {
        didSet { invalidateIntrinsicContentSize() }
    }
    
    override func textRect(forBounds bounds: CGRect, limitedToNumberOfLines numberOfLines: Int) -> CGRect {
        let insetRect = bounds.inset(by: textInsets)
        let textRect = super.textRect(forBounds: insetRect, limitedToNumberOfLines: numberOfLines)
        let invertedInsets = UIEdgeInsets(top: -textInsets.top, left: -textInsets.left, bottom: -textInsets.bottom, right: -textInsets.right)
        
        return textRect.inset(by: invertedInsets)
    }
    
    override func drawText(in rect: CGRect) {
        super.drawText(in: rect.inset(by: textInsets))
    }
}

@available(iOS 11.0, *)
extension UIViewController {
    static let DELAY_SHORT = 1.5
    static let DELAY_LONG = 3.0
    
    func showToast(_ text: String, delay: TimeInterval = DELAY_LONG) {
        let label = ToastLabel()
        label.backgroundColor = UIColor(white: 0, alpha: 0.75)
        label.textColor = .white
        label.textAlignment = .center
        label.font = UIFont.systemFont(ofSize: 15)
        label.alpha = 0
        label.text = text
        label.clipsToBounds = true
        label.layer.cornerRadius = 16
        label.numberOfLines = 0
        label.textInsets = UIEdgeInsets(top: 10, left: 15, bottom: 10, right: 15)
        label.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(label)
        
        let saveArea = view.safeAreaLayoutGuide
        label.centerXAnchor.constraint(equalTo: saveArea.centerXAnchor, constant: 0).isActive = true
        label.leadingAnchor.constraint(greaterThanOrEqualTo: saveArea.leadingAnchor, constant: 15).isActive = true
        label.trailingAnchor.constraint(lessThanOrEqualTo: saveArea.trailingAnchor, constant: -15).isActive = true
        label.bottomAnchor.constraint(equalTo: saveArea.bottomAnchor, constant: -30).isActive = true
        
        UIView.animate(withDuration: 0.5, delay: 0, options: .curveEaseIn, animations: {
            label.alpha = 1
        }, completion: { _ in
            UIView.animate(withDuration: 0.5, delay: delay, options: .curveEaseOut, animations: {
                label.alpha = 0
            }, completion: {_ in
                label.removeFromSuperview()
            })
        })
    }
}
