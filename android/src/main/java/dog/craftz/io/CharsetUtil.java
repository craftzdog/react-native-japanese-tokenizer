/*
 **
 **  Mar. 1, 2008
 **
 **  The author disclaims copyright to this source code.
 **  In place of a legal notice, here is a blessing:
 **
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 **
 **                                         Stolen from SQLite :-)
 **  Any feedback is welcome.
 **  Kohei TAKETA <k-tak@void.in>
 **
 */
package dog.craftz.io;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;

/**
 * 文字コード変換用のヘルパークラス。
 * 
 * @author takedaku
 */
public abstract class CharsetUtil
{
    /**
     * Unicodeから指定した文字コードへの変換器を作成する。
     * 
     * @param charset
     *            文字コード
     * @param malformedInputAction
     *            不正な入力への対処方法
     * @param unmappableCharacterAction
     *            指定した文字コードに存在しない文字への対処方法
     * @return 作成した変換器
     * @throws java.nio.charset.IllegalCharsetNameException
     *             文字コード名が不正
     * @throws UnsupportedCharsetException
     *             JVMが文字コードをサポートしていない
     * @throws IllegalArgumentException
     *             malformedInputAction, unmappableCharacterActionに無効な値を指定。
     */
    public static CharsetEncoder createEncoder(String charset,
            CodingErrorAction malformedInputAction,
            CodingErrorAction unmappableCharacterAction)
    {
        Charset cset = Charset.forName(charset);
        if (!cset.canEncode()) {
            throw new UnsupportedCharsetException(charset);
        }
        CharsetEncoder encoder = cset.newEncoder();
        encoder.onMalformedInput(malformedInputAction);
        encoder.onUnmappableCharacter(unmappableCharacterAction);

        return encoder;
    }

    /**
     * 指定した文字コードからUnicodeへの変換器を作成する。
     * 
     * @param charset
     *            文字コード
     * @param malformedInputAction
     *            不正な入力への対処方法
     * @param unmappableCharacterAction
     *            指定した文字コードに存在しない文字への対処方法
     * @return 作成した変換器
     * @throws java.nio.charset.IllegalCharsetNameException
     *             文字コード名が不正。
     * @throws UnsupportedCharsetException
     *             JVMが文字コードをサポートしていない。
     * @throws IllegalArgumentException
     *             malformedInputAction, unmappableCharacterActionに無効な値を指定。
     */
    public static CharsetDecoder createDecoder(String charset,
            CodingErrorAction malformedInputAction,
            CodingErrorAction unmappableCharacterAction)
    {
        Charset cset = Charset.forName(charset);
        CharsetDecoder decoder = cset.newDecoder();
        decoder.onMalformedInput(malformedInputAction);
        decoder.onUnmappableCharacter(unmappableCharacterAction);
        return decoder;
    }

    /**
     * 指定したエンコーダを用いて、Unicode文字列をバイト配列にエンコードする。
     * 
     * @param encoder
     *            エンコーダ
     * @param text
     *            Unicode文字列
     * @param terminateWithNull
     *            バイト配列の最後の要素としてヌル文字を詰めるかどうか
     * @return バイト配列
     * @throws CharacterCodingException
     *             変換エラーの発生
     */
    public static byte[] encode(CharsetEncoder encoder, CharSequence text,
            boolean terminateWithNull) throws CharacterCodingException
    {
        encoder.reset();
        ByteBuffer buf = encoder.encode(CharBuffer.wrap(text));
        int size = buf.limit();

        byte[] ret;
        if (terminateWithNull) {
            // \0を追加する。
            ret = new byte[size + 1];
            buf.get(ret, 0, size);
            ret[size] = 0;
        } else {
            ret = new byte[size];
            buf.get(ret, 0, size);
        }

        return ret;
    }

    /**
     * 指定したデコーダを用いて、バイト配列をUnicode文字列にデコードする。
     * 
     * @param decoder
     *            デコーダ
     * @param rawText
     *            バイト配列
     * @return Unicode文字列
     * @throws CharacterCodingException
     *             変換エラーの発生
     */
    public static String decode(CharsetDecoder decoder, byte[] rawText)
    throws CharacterCodingException
    {
        decoder.reset();
        CharBuffer buf = decoder.decode(ByteBuffer.wrap(rawText));
        return buf.toString();
    }
}
