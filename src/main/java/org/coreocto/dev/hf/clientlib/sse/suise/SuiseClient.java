package org.coreocto.dev.hf.clientlib.sse.suise;

import org.coreocto.dev.hf.clientlib.LibConstants;
import org.coreocto.dev.hf.clientlib.parser.IFileParser;
import org.coreocto.dev.hf.commonlib.crypto.IByteCipher;
import org.coreocto.dev.hf.commonlib.crypto.IFileCipher;
import org.coreocto.dev.hf.commonlib.sse.suise.bean.AddTokenResult;
import org.coreocto.dev.hf.commonlib.sse.suise.bean.SearchTokenResult;
import org.coreocto.dev.hf.commonlib.sse.suise.util.SuiseUtil;
import org.coreocto.dev.hf.commonlib.util.IBase64;
import org.coreocto.dev.hf.commonlib.util.Registry;
import org.coreocto.dev.hf.commonlib.util.Util;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class SuiseClient {

    private static final String TAG = "SuiseClient";

    private byte[] key1 = null;
    private byte[] key2 = null;

    private Set<String> searchHistory = null;   //contains a set of searched keyword (in plain text)
    private SuiseUtil suiseUtil = null;

    private Registry registry;

    public SuiseClient(Registry registry, SuiseUtil suiseUtil) {
        this.registry = registry;
        this.suiseUtil = suiseUtil;
        this.searchHistory = new HashSet<>();
    }

    public byte[] getKey1() {
        return key1;
    }

    public byte[] getKey2() {
        return key2;
    }

    public void Gen(int noOfBytes) {
        this.key1 = suiseUtil.g(noOfBytes);
        this.key2 = suiseUtil.g(noOfBytes);
    }

    public void Dec(InputStream fis, OutputStream fos, IFileCipher fileCipher) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IOException {
        fileCipher.decrypt(fis, fos);
    }

    public void Enc(InputStream fis, OutputStream fos, IFileCipher fileCipher) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IOException {
        fileCipher.encrypt(fis, fos);
    }

    public void Enc(File fi, File fo, IFileCipher fileCipher) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IOException {
        fileCipher.encrypt(fi, fo);
    }

    public void Dec(File fi, File fo, IFileCipher fileCipher) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IOException {
        fileCipher.decrypt(fi, fo);
    }

    private String encryptStr(String message, IByteCipher byteCipher) throws UnsupportedEncodingException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        byte[] enc = byteCipher.encrypt(message.getBytes(LibConstants.ENCODING_UTF8));
        return registry.getBase64().encodeToString(enc);
    }

    public AddTokenResult AddToken(InputStream inputStream, boolean includeSubStr, String docId, IFileParser fileParser, IByteCipher byteCipher) throws UnsupportedEncodingException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        AddTokenResult result = new AddTokenResult();

        Set<String> uniqueWordSet = new HashSet<>();
        List<String> x = new ArrayList<>();
        List<String> c = new ArrayList<>();

        uniqueWordSet.addAll(fileParser.getText(inputStream));

        List<String> uniqueWordList = new ArrayList<>(uniqueWordSet);

        if (includeSubStr) {
            Set<String> s = new HashSet<>();
            for (String uniqueWord : uniqueWordList) {
                List<String> substrings = Util.getSubstrings(uniqueWord, true);
                s.addAll(substrings);
            }

            uniqueWordList.clear();
            uniqueWordList.addAll(s);
            Collections.sort(uniqueWordList);
        }

        int uniqueWordCnt = uniqueWordList.size();

        byte[] randomBytes = new byte[16];

        IBase64 base64 = registry.getBase64();

        for (int i = 0; i < uniqueWordCnt; i++) {

            suiseUtil.setRandomBytes(randomBytes, i);

            byte[] encWord = byteCipher.encrypt(uniqueWordList.get(i).getBytes(LibConstants.ENCODING_UTF8));

            String searchToken = base64.encodeToString(encWord);

            if (searchHistory.contains(searchToken)) {
                x.add(searchToken);
            }

            byte[] h = suiseUtil.H(encWord, randomBytes);

            c.add(base64.encodeToString(h) + base64.encodeToString(randomBytes));
        }

        /*
        c.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        */

        result.setId(docId);
        result.setC(c);
        result.setX(x);

        return result;
    }

    public AddTokenResult AddToken(File inFile, boolean includeSubStr, String docId, IFileParser fileParser, IByteCipher byteCipher) throws FileNotFoundException, BadPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, IllegalBlockSizeException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        return this.AddToken(new BufferedInputStream(new FileInputStream(inFile)), includeSubStr, docId, fileParser, byteCipher);
    }

    public SearchTokenResult SearchToken(String keyword, IByteCipher byteCipher) throws BadPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, IllegalBlockSizeException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {

        SearchTokenResult result = new SearchTokenResult();

        result.setSearchToken(encryptStr(keyword, byteCipher));
        searchHistory.add(keyword);

        return result;
    }
}
