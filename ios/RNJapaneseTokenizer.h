#import <React/RCTBridgeModule.h>

#import <Foundation/Foundation.h>

#define logDebug

@interface RNJapaneseTokenizer : NSObject <RCTBridgeModule>

@property (nonatomic, copy) NSMutableDictionary *cachedDatabases;

@end
  
