#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(VideoClips, NSObject)
RCT_EXTERN_METHOD(select:(RCTPromiseResolveBlock)resolve withRejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(compression:(NSString *)name widthResolve:(RCTPromiseResolveBlock)resolve withRejecter:(RCTPromiseRejectBlock)reject)
@end
