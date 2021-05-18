"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;

var _reactNative = require("react-native");

const {
  MediaPicker
} = _reactNative.NativeModules;
const NativeMediaPicker = {
  openGallery: options => MediaPicker.openGallery(options),
  openViewFromStoryboard: options => MediaPicker.openViewFromStoryboard(options)
};
var _default = NativeMediaPicker;
exports.default = _default;
//# sourceMappingURL=index.js.map