//
//  PhotoCollectionViewCell.swift
//  IMGPicker
//
//  Created by Alex Nguyen on 15/12/2020.
//

import UIKit

class PhotoCollectionViewCell: UICollectionViewCell {
    @IBOutlet weak var thumbnail: UIImageView!
    @IBOutlet weak var checkmark: UIImageView!
    
    override var isSelected: Bool {
        didSet {
            checkmark.isHidden = isSelected ? false : true
            thumbnail.alpha = isSelected ? 0.5 : 1
        }
    }
    
    override func awakeFromNib() {
        contentView.layer.masksToBounds = true
        thumbnail.contentMode = .scaleAspectFill
        clipsToBounds = true
    }
}
