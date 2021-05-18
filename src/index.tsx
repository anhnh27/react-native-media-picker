import { NativeModules } from 'react-native';

type Options = {
  maxFiles: number;
  maxAllowedBytes: number;
  tcSupport: boolean;
};

type Image = {
  path: string;
  width: number;
  height: number;
  size: number;
};

interface IMediaPicker {
  openGallery(options?: Options): Promise<Array<Image>>;
  openViewFromStoryboard(options?: Options): Promise<Array<Image>>;
}

const { MediaPicker } = NativeModules;
const NativeMediaPicker: IMediaPicker = {
  openGallery: (options: Options) => MediaPicker.openGallery(options),
  openViewFromStoryboard: (options: Options) =>
    MediaPicker.openViewFromStoryboard(options),
};
export default NativeMediaPicker;
