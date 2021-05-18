//
//  MediaPickerDelegate.swift
//  MediaPickerDelegate
//
//  Created by Alex Nguyen on 15/12/2020.
//

import Foundation
import Photos

@available(iOS 11, *)
protocol MediaPickerDelegate: NSObject {
  func close(_ sender: Any)
  func didFinishSelection(_ sender: PhotoViewController)
  func navigateToPhotoPicker(_ sender: TAlbumsViewController, currentAlbum: PHAssetCollection)
  func navigateToAlbums(_ sender: PhotoViewController)
}
