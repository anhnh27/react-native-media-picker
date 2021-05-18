import { NativeModules } from 'react-native';
const {
  MediaPicker
} = NativeModules;
const NativeMediaPicker = {
  openGallery: options => MediaPicker.openGallery(options),
  openViewFromStoryboard: options => MediaPicker.openViewFromStoryboard(options)
};
export default NativeMediaPicker;
//# sourceMappingURL=index.js.map