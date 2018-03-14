package dog.craftz.japanese_tokenizer;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableNativeArray;

import android.content.Context;
import android.util.Log;

import java.io.StringReader;
import java.util.ArrayList;

import dog.craftz.io.BasicCodePointReader;
import dog.craftz.tinysegmenter.TinySegmenter;

public class RNJapaneseTokenizerModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  private static final boolean DEBUG_MODE = false;
  private static final String TAG = RNJapaneseTokenizerModule.class.getSimpleName();

  /**
   * Linked activity
   */
  protected Context context = null;

  public RNJapaneseTokenizerModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.context = reactContext.getApplicationContext();
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNJapaneseTokenizer";
  }

  @ReactMethod
  public void tokenize(final String string, final Promise promise) {
    new Thread(new Runnable() {
      public void run() {
        BasicCodePointReader reader = new BasicCodePointReader(new StringReader(string));
        TinySegmenter segmenter = new TinySegmenter(reader, 128, 1280);

        try {
          WritableNativeArray tokens = new WritableNativeArray();
          TinySegmenter.Token token;
          while ((token = segmenter.next()) != null) {
            tokens.pushString(token.str);
          }

          promise.resolve(tokens);
        } catch (Exception e) {
          promise.reject("TOKENIZE_ERROR", e);
        }
      }
    }).start();
  }
}
