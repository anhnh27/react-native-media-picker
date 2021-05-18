declare type Options = {
    maxFiles: number;
    maxAllowedBytes: number;
    tcSupport: boolean;
};
declare type Image = {
    path: string;
    width: number;
    height: number;
    size: number;
};
interface IMediaPicker {
    openGallery(options?: Options): Promise<Array<Image>>;
    openViewFromStoryboard(options?: Options): Promise<Array<Image>>;
}
declare const NativeMediaPicker: IMediaPicker;
export default NativeMediaPicker;
