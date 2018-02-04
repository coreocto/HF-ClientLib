package org.coreocto.dev.hf.clientlib.sse.suise;

import org.coreocto.dev.hf.clientlib.LibConstants;
import org.coreocto.dev.hf.clientlib.crypto.DummyFcImpl;
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

    public boolean isDataProtected() {
        return dataProtected;
    }

    public void setDataProtected(boolean dataProtected) {
        this.dataProtected = dataProtected;
    }

    private boolean dataProtected = true;

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

    public Collection<String> getSearchHistory() {
        return this.searchHistory;
    }

    public byte[] getKey1() {
        return key1;
    }

    public byte[] getKey2() {
        return key2;
    }

    public void setKey1(byte[] key1) {
        this.key1 = key1;
    }

    public void setKey2(byte[] key2) {
        this.key2 = key2;
    }

    public void Gen(int noOfBytes) {
        this.key1 = suiseUtil.g(noOfBytes);
        this.key2 = suiseUtil.g(noOfBytes);
    }

    public void Dec(InputStream fis, OutputStream fos, IFileCipher fileCipher) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IOException {
        if (dataProtected) {
            fileCipher.decrypt(fis, fos);
        } else {
            DummyFcImpl.getInstance().decrypt(fis, fos);
        }
    }

    public void Enc(InputStream fis, OutputStream fos, IFileCipher fileCipher) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IOException {
        if (dataProtected) {
            fileCipher.encrypt(fis, fos);
        } else {
            DummyFcImpl.getInstance().encrypt(fis, fos);
        }
    }

    public void Enc(File fi, File fo, IFileCipher fileCipher) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IOException {
        this.Enc(new FileInputStream(fi), new FileOutputStream(fo), fileCipher);
    }

    public void Dec(File fi, File fo, IFileCipher fileCipher) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IOException {
        this.Dec(new FileInputStream(fi), new FileOutputStream(fo), fileCipher);
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

        IBase64 base64 = registry.getBase64();

        if (dataProtected) {

            byte[] randomBytes = new byte[16];

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
        } else {
            for (int i = 0; i < uniqueWordCnt; i++) {

                byte[] encWord = uniqueWordList.get(i).getBytes(LibConstants.ENCODING_UTF8);

                String searchToken = base64.encodeToString(encWord);

                if (searchHistory.contains(searchToken)) {
                    x.add(searchToken);
                }

                c.add(searchToken);
            }
        }

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
        if (dataProtected) {
            result.setSearchToken(encryptStr(keyword, byteCipher));
        } else {
            result.setSearchToken(keyword);
        }
        searchHistory.add(keyword);

        return result;
    }
}
