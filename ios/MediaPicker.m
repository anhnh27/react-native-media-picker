#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(MediaPicker, NSObject)

RCT_EXTERN_METHOD(openGallery: (NSDictionary *)params
                  resolver: (RCTPromiseResolveBlock)resolve
                  rejecter: (RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(openViewFromStoryboard: (NSDictionary *)params
                  resolver: (RCTPromiseResolveBlock)resolve
                  rejecter: (RCTPromiseRejectBlock)reject)

@end
