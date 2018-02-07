package org.coreocto.dev.hf.clientlib.sse.chlh;

import com.skjegstad.utils.BloomFilter;
import org.apache.commons.lang3.StringUtils;
import org.coreocto.dev.hf.clientlib.LibConstants;
import org.coreocto.dev.hf.clientlib.parser.IFileParser;
import org.coreocto.dev.hf.commonlib.crypto.IByteCipher;
import org.coreocto.dev.hf.commonlib.sse.chlh.Index;
import org.coreocto.dev.hf.commonlib.util.IBase64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Chlh2Client {

    private static final int BITSET_SIZE = 512 * 2;
    private static final int EXPECTED_NUM_OF_ELEMENTS = 500 * 2;
    private IBase64 base64;
    private byte[] secretKey = null;

    public Chlh2Client(IBase64 base64) {
        this.base64 = base64;
    }

    public byte[] getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(byte[] secretKey) {
        this.secretKey = secretKey;
    }

    public void KeyGen(int secParam) {
        secretKey = new byte[secParam];
    }

    public String EncId(String docId, IByteCipher byteCipher) throws UnsupportedEncodingException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        byte[] enc = byteCipher.encrypt(docId.getBytes(LibConstants.ENCODING_UTF8));
        return base64.encodeToString(enc);
    }

    public String DecId(String encDocId, IByteCipher byteCipher) throws UnsupportedEncodingException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        byte[] dec = byteCipher.decrypt(base64.decodeToByteArray(encDocId));
        return new String(dec, LibConstants.ENCODING_UTF8);
    }

    public Index BuildIndex(InputStream is, IFileParser fileParser, String docId, IByteCipher byteCipher) throws BadPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, IllegalBlockSizeException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        List<String> keywords = fileParser.getText(is);

        Index docIndex = new Index();
        docIndex.setDocId(this.EncId(docId, byteCipher));

        BloomFilter<String> bloomFilter = new BloomFilter(BITSET_SIZE, EXPECTED_NUM_OF_ELEMENTS);

        int wordCnt = 0;

        for (String keyword : keywords) {
//            char[] chars = keyword.toCharArray();

//            int charLen = chars.length;
            int keywordLen = keyword.length();

            for (int j = 0; j < keywordLen; j++) {
                char c = keyword.charAt(j);
                bloomFilter.add(c + LibConstants.EMPTY_STRING + j);
                bloomFilter.add(c + LibConstants.EMPTY_STRING + (j + 1 - keywordLen));
//                System.out.println(chars[j] + "," + j);
                bloomFilter.add(c + LibConstants.EMPTY_STRING + 0);
            }

//            for (int i = keywordLen - 1; i >= 0; i--) {
//                char c = keyword.charAt(i);
//                bloomFilter.add(c + LibConstants.EMPTY_STRING + (1 - keywordLen + i));
////                System.out.println(chars[i] + "," + (1 - charLen + i));
//            }
            wordCnt++;
        }

        for (int j = 0; j < EXPECTED_NUM_OF_ELEMENTS - wordCnt; j++) {
            bloomFilter.add(((int) Math.random()) + LibConstants.EMPTY_STRING);
        }

        docIndex.setWordCnt(wordCnt);

        docIndex.getBloomFilters().add(base64.encodeToString(bloomFilter.getBitSet().toByteArray()));

        bloomFilter.clear();

        return docIndex;
    }

    public List<String> Trapdoor(String w) {

        List<String> out = new ArrayList<>();

        if (w == null || w.isEmpty()) {
            return out;
        }

        BloomFilter<String> bloomFilter = new BloomFilter(BITSET_SIZE, EXPECTED_NUM_OF_ELEMENTS);

        int wLen = w.length();

        int leftMostPos = w.indexOf(WILDCARD_CHAR);

//        int count = countWildcards(w);

        if (leftMostPos != -1) {
            //partial match

            //handle one wildcard char

            int rightMostPos = w.lastIndexOf(WILDCARD_CHAR);

            for (int i = 0; i < leftMostPos; i++) {
                char c = w.charAt(i);
                bloomFilter.add(c + LibConstants.EMPTY_STRING + i);
                bloomFilter.add(c + LibConstants.EMPTY_STRING + 0);
            }

            for (int i = wLen - 1; i > rightMostPos; i--) {
                char c = w.charAt(i);
                bloomFilter.add(c + LibConstants.EMPTY_STRING + (i + 1 - wLen));
                bloomFilter.add(c + LibConstants.EMPTY_STRING + 0);
            }

        } else {
            //exact match

            for (int i = 0; i < wLen; i++) {
                bloomFilter.add(w.charAt(i) + LibConstants.EMPTY_STRING + i);
            }
        }

//        for (int i = wLen - 1; i>=0;i--){
//            bloomFilter.add(w.charAt(i) + LibConstants.EMPTY_STRING + (1 - wLen + i));
//        }

//        char[] chars = w.toCharArray();
//
//        int charLen = chars.length;
//
//        for (int j = 0; j < charLen; j++) {
//            bloomFilter.add(chars[j] + LibConstants.EMPTY_STRING + j);
//        }

        out.add(base64.encodeToString(bloomFilter.getBitSet().toByteArray()));
        bloomFilter.clear();

        return out;
    }

    public int countWildcards(String s) {
        return StringUtils.countMatches(s, '*');
    }

    public static final char WILDCARD_CHAR = '*';
}
