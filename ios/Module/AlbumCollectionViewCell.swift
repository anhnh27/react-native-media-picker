//
//  AlbumCollectionViewCell.swift
//  IMGPicker
//
//  Created by Alex Nguyen on 14/12/2020.
//

import UIKit
import Photos

class AlbumCollectionViewCell: UICollectionViewCell {
    
    @IBOutlet weak var thumbnail: UIImageView!
    @IBOutlet weak var title: UILabel!
    @IBOutlet weak var totalPhotos: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        contentView.layer.cornerRadius = 6.0
        contentView.layer.masksToBounds = true
                
        // Set masks to bounds to false to avoid the shadow
        // from being clipped to the corner radius
        layer.cornerRadius = 8.0
        layer.masksToBounds = false
        
        // How blurred the shadow is
        layer.shadowRadius = 4.0

        // The color of the drop shadow
        layer.shadowColor = UIColor.black.cgColor

        // How far the shadow is offset from the UICollectionViewCell’s frame
        layer.shadowOffset = CGSize(width: 0, height: 4)
        thumbnail.contentMode = .scaleAspectFill
        clipsToBounds = true
    }
}
