package org.coreocto.dev.hf.clientlib.sse.suise;

import org.coreocto.dev.hf.clientlib.LibConstants;
import org.coreocto.dev.hf.clientlib.crypto.DummyFcImpl;
import org.coreocto.dev.hf.clientlib.parser.IFileParser;
import org.coreocto.dev.hf.commonlib.crypto.IFileCipher;
import org.coreocto.dev.hf.commonlib.crypto.IKeyedHashFunc;
import org.coreocto.dev.hf.commonlib.sse.suise.bean.AddTokenResult;
import org.coreocto.dev.hf.commonlib.sse.suise.bean.SearchTokenResult;
import org.coreocto.dev.hf.commonlib.sse.suise.util.SuiseUtil;
import org.coreocto.dev.hf.commonlib.util.IBase64;
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
    private IBase64 base64 = null;

//    private Registry registry;

    public SuiseClient(SuiseUtil suiseUtil, IBase64 base64) {
//        this.registry = registry;
        this.suiseUtil = suiseUtil;
        this.searchHistory = new HashSet<>();
        this.base64 = base64;
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

//    private String encryptStr(String message, IByteCipher byteCipher) throws UnsupportedEncodingException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidAlgorithmParameterException {
//        byte[] enc = byteCipher.encrypt(message.getBytes(LibConstants.ENCODING_UTF8));
//        return registry.getBase64().encodeToString(enc);
//    }

    public AddTokenResult AddToken(InputStream inputStream, boolean includePrefix, boolean includeSuffix, String docId, IFileParser fileParser, IKeyedHashFunc keyedHashFunc, Random random) throws UnsupportedEncodingException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        AddTokenResult result = new AddTokenResult();

        Set<String> uniqueWordSet = new HashSet<>();
        List<String> x = new ArrayList<>();
        List<String> c = new ArrayList<>();

        uniqueWordSet.addAll(fileParser.getText(inputStream));

        List<String> uniqueWordList = new ArrayList<>(uniqueWordSet);

        if (includePrefix && includeSuffix) {
            Set<String> s = new HashSet<>();
            for (String uniqueWord : uniqueWordList) {
                List<String> substrings = Util.getSubstrings(uniqueWord, true);
                s.addAll(substrings);
            }

            uniqueWordList.clear();
            uniqueWordList.addAll(s);
            Collections.sort(uniqueWordList);
        } else if (includePrefix) {
            Set<String> s = new HashSet<>();
            for (String uniqueWord : uniqueWordList) {
                int wordLen = uniqueWord.length();
                for (int i = 1; i <= wordLen; i++) {
                    s.add(uniqueWord.substring(0, i));
                }
            }
            uniqueWordList.clear();
            uniqueWordList.addAll(s);
            Collections.sort(uniqueWordList);
        } else if (includeSuffix) {
            Set<String> s = new HashSet<>();
            for (String uniqueWord : uniqueWordList) {
                int wordLen = uniqueWord.length();
                for (int i = 0; i < wordLen; i++) {
                    s.add(uniqueWord.substring(i, wordLen));
                }
            }
            uniqueWordList.clear();
            uniqueWordList.addAll(s);
            Collections.sort(uniqueWordList);
        }

        int uniqueWordCnt = uniqueWordList.size();

        if (dataProtected) {

            byte[] randomBytes = new byte[16];

            for (int i = 0; i < uniqueWordCnt; i++) {

//                suiseUtil.setRandomBytes(randomBytes, i);

                byte[] encWord = keyedHashFunc.getHash(this.getKey1(), uniqueWordList.get(i).getBytes(LibConstants.ENCODING_UTF8));

//                byte[] encWord = byteCipher.encrypt(uniqueWordList.get(i).getBytes(LibConstants.ENCODING_UTF8));

                String searchToken = base64.encodeToString(encWord);

                if (searchHistory.contains(searchToken)) {
                    x.add(searchToken);
                }

                random.nextBytes(randomBytes);

                byte[] h = keyedHashFunc.getHash(encWord, randomBytes);

                byte[] combine = new byte[h.length + randomBytes.length];
                System.arraycopy(h, 0, combine, 0, h.length);
                System.arraycopy(randomBytes, 0, combine, h.length, randomBytes.length);

//                byte[] h = suiseUtil.H(encWord, randomBytes);

//                c.add(base64.encodeToString(h) + base64.encodeToString(randomBytes));
                c.add(base64.encodeToString(combine));
            }

            Collections.sort(c);

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

//    public AddTokenResult AddToken(InputStream inputStream, boolean includePrefix, boolean includeSuffix, String docId, IFileParser fileParser, IByteCipher byteCipher) throws UnsupportedEncodingException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidAlgorithmParameterException {
//        AddTokenResult result = new AddTokenResult();
//
//        Set<String> uniqueWordSet = new HashSet<>();
//        List<String> x = new ArrayList<>();
//        List<String> c = new ArrayList<>();
//
//        uniqueWordSet.addAll(fileParser.getText(inputStream));
//
//        List<String> uniqueWordList = new ArrayList<>(uniqueWordSet);
//
//        Set<String> s = new HashSet<>();
//        for (String uniqueWord : uniqueWordList) {
//            List<String> substrings = Util.getSubstrings(uniqueWord, true);
//            s.addAll(substrings);
//        }
//
//        uniqueWordList.clear();
//        uniqueWordList.addAll(s);
//        Collections.sort(uniqueWordList);
//
//        int uniqueWordCnt = uniqueWordList.size();
//
//        IBase64 base64 = registry.getBase64();
//
//        if (dataProtected) {
//
//            byte[] randomBytes = new byte[16];
//
//            for (int i = 0; i < uniqueWordCnt; i++) {
//
//                suiseUtil.setRandomBytes(randomBytes, i);
//
//                byte[] encWord = byteCipher.encrypt(uniqueWordList.get(i).getBytes(LibConstants.ENCODING_UTF8));
//
//                String searchToken = base64.encodeToString(encWord);
//
//                if (searchHistory.contains(searchToken)) {
//                    x.add(searchToken);
//                }
//
//                byte[] h = suiseUtil.H(encWord, randomBytes);
//
//                c.add(base64.encodeToString(h) + base64.encodeToString(randomBytes));
//            }
//
//            /*
//            c.sort(new Comparator<String>() {
//                @Override
//                public int compare(String o1, String o2) {
//                    return o1.compareTo(o2);
//                }
//            });
//            */
//        } else {
//            for (int i = 0; i < uniqueWordCnt; i++) {
//
//                byte[] encWord = uniqueWordList.get(i).getBytes(LibConstants.ENCODING_UTF8);
//
//                String searchToken = base64.encodeToString(encWord);
//
//                if (searchHistory.contains(searchToken)) {
//                    x.add(searchToken);
//                }
//
//                c.add(searchToken);
//            }
//        }
//
//        result.setId(docId);
//        result.setC(c);
//        result.setX(x);
//
//        return result;
//    }

    public AddTokenResult AddToken(File inFile, boolean includePrefix, boolean includeSuffix, String docId, IFileParser fileParser, IKeyedHashFunc keyedHashFunc, Random random) throws FileNotFoundException, BadPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, IllegalBlockSizeException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        return this.AddToken(new BufferedInputStream(new FileInputStream(inFile)), includePrefix, includeSuffix, docId, fileParser, keyedHashFunc, random);
    }

    private String hashStr(String keyword, IKeyedHashFunc keyedHashFunc) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        byte[] hashBytes = keyedHashFunc.getHash(this.getKey1(), keyword.getBytes(LibConstants.ENCODING_UTF8));
        return base64.encodeToString(hashBytes);
    }

    public SearchTokenResult SearchToken(String keyword, IKeyedHashFunc keyedHashFunc) throws BadPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, IllegalBlockSizeException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {

        SearchTokenResult result = new SearchTokenResult();
        if (dataProtected) {
            result.setSearchToken(hashStr(keyword, keyedHashFunc));
        } else {
            byte[] keywordBytes = keyword.getBytes(LibConstants.ENCODING_UTF8);
            result.setSearchToken(base64.encodeToString(keywordBytes));
        }
        searchHistory.add(keyword);

        return result;
    }
}
