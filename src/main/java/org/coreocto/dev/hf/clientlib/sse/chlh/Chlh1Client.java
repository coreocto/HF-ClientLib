package org.coreocto.dev.hf.clientlib.sse.chlh;

import com.skjegstad.utils.BloomFilter;
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

public class Chlh1Client {

    private IBase64 base64;
    private byte[] secretKey = null;
    private static final int BITSET_SIZE = 512;
    private static final int EXPECTED_NUM_OF_ELEMENTS = 500;

    public Chlh1Client(IBase64 base64) {
        this.base64 = base64;
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

        for (String keyword : keywords) {
            char[] chars = keyword.toCharArray();

            BloomFilter<String> bloomFilter = new BloomFilter(BITSET_SIZE, EXPECTED_NUM_OF_ELEMENTS);

            int charLen = chars.length;

            for (int j = 0; j < charLen; j++) {
                bloomFilter.add("" + j + chars[j]);
            }

            for (int j = 0; j < 500 - charLen; j++) {
                bloomFilter.add(((int) Math.random()) + "");
            }

            docIndex.getBloomFilters().add(base64.encodeToString(bloomFilter.getBitSet().toByteArray()));
            bloomFilter.clear();
        }

        return docIndex;
    }

    public List<String> Trapdoor(String w) {

        List<String> out = new ArrayList<>();

        char[] chars = w.toCharArray();

        BloomFilter<String> bloomFilter = new BloomFilter(BITSET_SIZE, EXPECTED_NUM_OF_ELEMENTS);

        int charLen = chars.length;

        for (int j = 0; j < charLen; j++) {
            bloomFilter.add("" + j + chars[j]);
        }

        out.add(base64.encodeToString(bloomFilter.getBitSet().toByteArray()));
        bloomFilter.clear();

        return out;
    }
}

