# react-native-japanese-tokenizer

Async Japanese tokenizer native plugin for React Native that works on iOS and Android.
It tokenizes Japanese text in background thread so it won't block UI. It comes in handy for such as indexing a lot of texts for full-text search.

![screenshot](https://github.com/craftzdog/react-native-japanese-tokenizer/blob/master/docs/screenshot.png?raw=true)

## How it works

### iOS

It utilizes [`CFStringTokenizer`](https://developer.apple.com/documentation/corefoundation/cfstringtokenizer-rf8) to tokenize strings into words.

### Android

The implementation is based on [TinySegmenter](http://chasen.org/~taku/software/TinySegmenter/). It is lightweight word segmenter which works without dictionaries.

## Getting started

```shell
$ npm install react-native-japanese-tokenizer --save
```

### Mostly automatic installation

```shell
$ react-native link react-native-japanese-tokenizer
```

### Manual installation

#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-japanese-tokenizer` and add `RNJapaneseTokenizer.xcodeproj`
3. In Xcode, in the project navigator, select your project. Add `libRNJapaneseTokenizer.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import dog.craftz.japanese_tokenizer.RNJapaneseTokenizerPackage;` to the imports at the top of the file
  - Add `new RNJapaneseTokenizerPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
     ```
     include ':react-native-japanese-tokenizer'
     project(':react-native-japanese-tokenizer').projectDir = new File(rootProject.projectDir,  '../node_modules/react-native-japanese-tokenizer/android')
     ```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
     ```
      compile project(':react-native-japanese-tokenizer')
     ```

## Usage

Using `Promise`:

```javascript
import Tokenizer from 'react-native-japanese-tokenizer'

var text = "週休七日で働きたい"
Tokenizer.tokenize(text).then(tokens => {
  ...
})
```

`async/await`:

```javascript
import Tokenizer from 'react-native-japanese-tokenizer'

var text = "週休七日で働きたい"
const tokens = await Tokenizer.tokenize(text)
```

Also check out [the test](https://github.com/craftzdog/react-native-japanese-tokenizer/tree/master/test) to learn more.

## License

MIT License. By Takuya Matsuyama ([@craftzdog](https://twitter.com/craftzdog)).

Note that this module is based on following libraries with respective license. Thank you for the fantastic works!

 * [TinySegmenter: Javascriptだけで実装されたコンパクトな分かち書きソフトウェア](http://chasen.org/~taku/software/TinySegmenter/)
   * BSD-3-Clause
 * [TinySegmenter for Java by takscape](https://github.com/takscape/cmecab-java)
   * Public Domain
