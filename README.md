# react-native-media-picker

## Installation

```shell script
yarn add -D git+ssh://git@git.realestate.com.au:react-native/react-native-media-picker.git#v0.0.1
```

### Android

Require permission

```
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

### iOS

Update .plist

```
<key>NSPhotoLibraryUsageDescription</key>
<string></string>
<key>PHPhotoLibraryPreventAutomaticLimitedAccessAlert</key>
<true/>
```

Go to build phase add all xib files from '~/node_modules/react-native-media-picker/ios' to resource

## Usage

```js
import MediaPicker from 'react-native-media-picker';

const { photos } = await MediaPicker.openGallery({
  maxFiles: 40,
  maxAllowedBytes: 4194304,
});
```

Response object

```js
[
  {
    path: 'path_to_image',
    size: 1000000,
    width: 2000,
    height: 20000,
  },
];
```

## Example

```shell script
yarn & yarn example ios/android
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
