#import "RNJapaneseTokenizer.h"

@implementation RNJapaneseTokenizer

@synthesize cachedDatabases;

- (dispatch_queue_t)methodQueue
{
    return dispatch_queue_create("dog.craftz.japanese-tokenizer", DISPATCH_QUEUE_SERIAL);
}
RCT_EXPORT_MODULE()

- (id) init
{
  self = [super init];
  if (self!=nil) {
    [self pluginInitialize];
  }
  return self;
}

-(void)pluginInitialize {
  logDebug(@"pluginInitialize()");
}

-(NSString*) getDatabaseDir {
  NSString *libDir = [NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES) objectAtIndex: 0];
  return [libDir stringByAppendingPathComponent:@"NoCloud"];
}

RCT_EXPORT_METHOD(tokenize:(NSString *)string
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
  logDebug(@"tokenize: %@", string);
  NSArray * result = [self tokenArrayWithString:string];
  resolve(result);
}

- (NSArray *)tokenArrayWithString:(NSString *)string {  
  NSLocale *locale = [[NSLocale alloc] initWithLocaleIdentifier:@"ja"];
  CFRange range = CFRangeMake(0, CFStringGetLength((CFStringRef)string));
  CFStringTokenizerRef tokenizer = CFStringTokenizerCreate(kCFAllocatorDefault, (CFStringRef)string, range, kCFStringTokenizerUnitWordBoundary, (CFLocaleRef)locale);

  NSMutableArray *tokenArray = [NSMutableArray array];

  while(CFStringTokenizerAdvanceToNextToken(tokenizer) != kCFStringTokenizerTokenNone) {
    CFRange tokenRange = CFStringTokenizerGetCurrentTokenRange(tokenizer);
    if(range.location != kCFNotFound) {
      NSString *token = [string substringWithRange:NSMakeRange(tokenRange.location, tokenRange.length)];
      [tokenArray addObject:token];
    }
  }

  CFRelease(tokenizer);

  return tokenArray;
}

@end
