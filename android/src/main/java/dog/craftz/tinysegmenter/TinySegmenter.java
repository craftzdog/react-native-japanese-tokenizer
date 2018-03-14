/*
 * Based on TinySegmenter 0.1 -- Super compact Japanese tokenizer in Javascript
 * (c) 2008 Taku Kudo <taku@chasen.org>
 * TinySegmenter is freely distributable under the terms of a new BSD licence.
 * For details, see http://chasen.org/~taku/software/TinySegmenter/LICENCE.txt
 * 
 * Ported to Java by Kohei TAKETA <k-tak@void.in>
 */
package dog.craftz.tinysegmenter;

import dog.craftz.io.CodePointReader;

import java.io.IOException;
import java.util.*;

import static dog.craftz.tinysegmenter.TinySegmenterConstants.*;

/**
 * TinySegmenterのJava移植版。
 * 
 * @author taketa
 *
 */
public class TinySegmenter
{
    public static class CharInfo
    {
        public int cp;
        public String str;
        public String ctype;
        public long start;
        public long end;
    }
    
    public static class Token
    {
        public String str;
        public long start;
        public long end;
        
        public boolean equals(Object obj)
        {
            if (!(obj instanceof Token)) {
                return false;
            }
            
            Token another = (Token)obj;
            return ((str == null ? another.str == null : str.equals(another.str)) &&
                    (start == another.start) && (end == another.end));
        }
        
        public String toString()
        {
            return ("(" +
                    str + "," +
                    Long.toString(start) + "," +
                    Long.toString(end) + ")");
        }
    }
    
    private static String getCharType(int cp)
    {
        if (CHINESE_NUMBER_SET.contains(cp)) {
            // [一二三四五六七八九十百千万億兆]
            return "M";
        } else if (
                (0x4E00 <= cp && cp <= 0x9fa0) ||
                cp == '々' || cp == '〆' || cp == 'ヵ' || cp == 'ヶ') {
            // [一-龠々〆ヵヶ]
            return "H";
        } else if (0x3041 <= cp && cp <= 0x3093) {
            // [ぁ-ん]
            return "I";
        } else if (
                (0x30a1 <= cp && cp <= 0x30f4) || cp == 0x30fc ||
                (0xff71 <= cp && cp <= 0xff9e) || cp == 0xff70) {
            // [ァ-ヴーｱ-ﾝﾞｰ]
            return "K";
        } else if (
                ('a' <= cp && cp <= 'z') || ('A' <= cp && cp <= 'Z') ||
                ('ａ' <= cp && cp <= 'ｚ') || ('Ａ' <= cp && cp <= 'Ｚ')) {
            // [a-zA-Zａ-ｚＡ-Ｚ]
            return "A";
        } else if (
                ('0' <= cp && cp <= '9') || ('０' <= cp && cp <= '９')) {
            // [0-9０-９]
            return "N";
        } else {
            return "O";
        }
    }

    private static List<Integer> getCodePoints(String str)
    {
        int count = str.codePointCount(0, str.length());
        List<Integer> result = new ArrayList<Integer>(count);
        int charIndex = 0;
        while (charIndex < str.length()) {
            int cp = str.codePointAt(charIndex);
            result.add(cp);
            charIndex += Character.charCount(cp);
        }

        return result;
    }
    
    public static final int DEFAULT_BUFFER_SIZE = 4096;
    public static final int DEFAULT_MAX_TOKEN_SIZE = 255;
    
    @SuppressWarnings("serial")
    private static final Set<Integer> CHINESE_NUMBER_SET =
        Collections.unmodifiableSet(new HashSet<Integer>() {{
            addAll(getCodePoints("一二三四五六七八九十百千万億兆"));
        }});
    
    private CodePointReader reader;
    private boolean eos;
    private int eosCount;
    
    private CharInfo[] buffer;
    private int end;
    private int position;
    private boolean done;
    private String p1;
    private String p2;
    private String p3;
    
    private CharInfo[] tokenBuffer;
    private int tokenPosition;
    
    public TinySegmenter(CodePointReader reader)
    {
        this(reader, DEFAULT_BUFFER_SIZE, DEFAULT_MAX_TOKEN_SIZE);
    }
    
    /**
     * コンストラクタ
     * @param reader CodePointReaderオブジェクト。
     * @param bufferSize バッファのサイズ。7以上でなければならない。
     * @param maxTokenSize トークンの最大サイズ。2以上でなければならない。
     */
    public TinySegmenter(CodePointReader reader, int bufferSize, int maxTokenSize)
    {
        assert (bufferSize > 6);
        assert (maxTokenSize > 1);
        
        this.reader = reader;
        this.p1 = "U";
        this.p2 = "U";
        this.p3 = "U";
        this.buffer = new CharInfo[bufferSize];
        this.tokenBuffer = new CharInfo[maxTokenSize];
    }
    
    public CharInfo readChar() throws IOException
    {
        CharInfo c = new CharInfo();
        
        if (eos) {
            c.start = -1;
            c.end = -1;
            c.cp = -1;
        } else {
            c.start = reader.getPosition();
            c.cp = reader.read();
            c.end = reader.getPosition();
        }

        if (c.cp < 0) {
            eos = true;
            switch (eosCount) {
            case 0:
                c.str = "E1";
                c.ctype = "O";
                ++eosCount;
                break;
            case 1:
                c.str = "E2";
                c.ctype = "O";
                ++eosCount;
                break;
            case 2:
                c.str = "E3";
                c.ctype = "O";
                ++eosCount;
                break;
            default:
                return null;
            }
        } else {
            c.str = new String(Character.toChars(c.cp));
            c.ctype = getCharType(c.cp);
        }

        return c;
    }

    private void initBuffer()
    {
        CharInfo c;

        {
            c = new CharInfo();
            c.cp = 0;
            c.str = "B3";
            c.ctype = "O";
            c.start = -1;
            c.end = -1;
            buffer[0] = c;
            
            c = new CharInfo();
            c.cp = 0;
            c.str = "B2";
            c.ctype = "O";
            c.start = -1;
            c.end = -1;
            buffer[1] = c;
            
            c = new CharInfo();
            c.cp = 0;
            c.str = "B1";
            c.ctype = "O";
            c.start = -1;
            c.end = -1;
            buffer[2] = c;
        }
        end = 3;
        position = 3;
    }
    
    private int fillBuffer()
    throws IOException
    {
        CharInfo c;
        
        // 末尾の6アイテムを、先頭にコピーする。
        int src = end - 6;
        int dst = 0;
        if (src < 0) {
            src = 0;
        }
        while (src < end) {
            buffer[dst++] = buffer[src++];
        }
        
        // end, positionをリセット
        int start = dst;
        end = dst;
        position = 3;
        
        // 残りの部分にデータを読み込む
        while (end < buffer.length) {
            c = readChar();
            if (c != null) {
                buffer[end++] = c;
            } else {
                break;
            }
        }
        
        return (end - start);
    }
    
    public Token next()
    throws IOException
    {
        if (done) {
            return null;
        }
        
        if (end <= 0) {
            // 初回呼び出し
            initBuffer();
            if (fillBuffer() > 3) {
                // 最初の文字をtokenBufferに格納
                tokenBuffer[tokenPosition++] = buffer[position++];
            } else {
                // 空のストリーム
                done = true;
                return null;
            }
        }
        
        Token token = null;
        do {
            while (position < end-3) {
                if (isBoundary()) {
                    // トークン境界と判定
                    if (tokenPosition > 0) {
                        token = makeToken();
                        tokenBuffer[tokenPosition++] = buffer[position++];
                        break;
                    }
                } else {
                    // トークン境界ではない
                    tokenBuffer[tokenPosition++] = buffer[position++];
                    if (tokenPosition >= tokenBuffer.length) {
                        // バッファがいっぱいになったので、一旦トークンとして切り出す
                        token = makeToken();
                        break;
                    }
                }
            }
        } while (token == null && fillBuffer() > 0);
        
        if (token == null) {
            // 最後のトークンを切り出す
            done = true;
            token = makeToken();
        }
        
        return token;
    }
    
    private Token makeToken()
    {
        Token token = new Token();
        StringBuilder builder = new StringBuilder();
        
        token.start = tokenBuffer[0].start;
        for (int i=0; i<tokenPosition; ++i) {
            builder.append(tokenBuffer[i].str);
            token.end = tokenBuffer[i].end;
        }
        
        token.str = builder.toString();
        
        tokenPosition = 0;
        
        return token;
    }
    
    private boolean isBoundary()
    {
        int score = BIAS;
        
        CharInfo c1 = buffer[position-3];
        CharInfo c2 = buffer[position-2];
        CharInfo c3 = buffer[position-1];
        CharInfo c4 = buffer[position];
        CharInfo c5 = buffer[position+1];
        CharInfo c6 = buffer[position+2];
        
        score += getScore(UP1, p1);
        score += getScore(UP2, p2);
        score += getScore(UP3, p3);
        score += getScore(BP1, p1 + p2);
        score += getScore(BP2, p2 + p3);
        score += getScore(UW1, c1.str);
        score += getScore(UW2, c2.str);
        score += getScore(UW3, c3.str);
        score += getScore(UW4, c4.str);
        score += getScore(UW5, c5.str);
        score += getScore(UW6, c6.str);
        score += getScore(BW1, c2.str + c3.str);
        score += getScore(BW2, c3.str + c4.str);
        score += getScore(BW3, c4.str + c5.str);
        score += getScore(TW1, c1.str + c2.str + c3.str);
        score += getScore(TW2, c2.str + c3.str + c4.str);
        score += getScore(TW3, c3.str + c4.str + c5.str);
        score += getScore(TW4, c4.str + c5.str + c6.str);
        score += getScore(UC1, c1.ctype);
        score += getScore(UC2, c2.ctype);
        score += getScore(UC3, c3.ctype);
        score += getScore(UC4, c4.ctype);
        score += getScore(UC5, c5.ctype);
        score += getScore(UC6, c6.ctype);
        score += getScore(BC1, c2.ctype + c3.ctype);
        score += getScore(BC2, c3.ctype + c4.ctype);
        score += getScore(BC3, c4.ctype + c5.ctype);
        score += getScore(TC1, c1.ctype + c2.ctype + c3.ctype);
        score += getScore(TC2, c2.ctype + c3.ctype + c4.ctype);
        score += getScore(TC3, c3.ctype + c4.ctype + c5.ctype);
        score += getScore(TC4, c4.ctype + c5.ctype + c6.ctype);
        score += getScore(UQ1, p1 + c1.ctype);
        score += getScore(UQ2, p2 + c2.ctype);
        score += getScore(UQ3, p3 + c3.ctype);
        //score += getScore(UQ1, p3 + c3.ctype); // オリジナルコード。UQ3の誤り？
        score += getScore(BQ1, p2 + c2.ctype + c3.ctype);
        score += getScore(BQ2, p2 + c3.ctype + c4.ctype);
        score += getScore(BQ3, p3 + c2.ctype + c3.ctype);
        score += getScore(BQ4, p3 + c3.ctype + c4.ctype);
        score += getScore(TQ1, p2 + c1.ctype + c2.ctype + c3.ctype);
        score += getScore(TQ2, p2 + c2.ctype + c3.ctype + c4.ctype);
        score += getScore(TQ3, p3 + c1.ctype + c2.ctype + c3.ctype);
        score += getScore(TQ4, p3 + c2.ctype + c3.ctype + c4.ctype);
        
        boolean result = false;
        String p = "O";
        if (score > 0) {
            p = "B";
            result = true;
        }
        p1 = p2;
        p2 = p3;
        p3 = p;
        
        return result;
    }
    
    private int getScore(Map<String, Integer> m, String key)
    {
        Integer s = m.get(key);
        return (s != null ? s : 0);
    }
}
