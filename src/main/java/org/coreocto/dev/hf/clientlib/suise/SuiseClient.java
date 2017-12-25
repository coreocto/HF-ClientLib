package org.coreocto.dev.hf.clientlib.suise;

import org.coreocto.dev.hf.clientlib.LibConstants;
import org.coreocto.dev.hf.clientlib.parser.IFileParser;
import org.coreocto.dev.hf.commonlib.crypto.BlockCipherFactory;
import org.coreocto.dev.hf.commonlib.crypto.IBlockCipherCbc;
import org.coreocto.dev.hf.commonlib.suise.bean.AddTokenResult;
import org.coreocto.dev.hf.commonlib.suise.bean.SearchTokenResult;
import org.coreocto.dev.hf.commonlib.suise.util.SuiseUtil;
import org.coreocto.dev.hf.commonlib.util.IBase64;
import org.coreocto.dev.hf.commonlib.util.Registry;
import org.coreocto.dev.hf.commonlib.util.Util;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
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
    private byte[] iv1 = null;
    private byte[] iv2 = null;
    private Set<String> searchHistory = null;   //contains a set of searched keyword (in plain text)
    private SuiseUtil suiseUtil = null;

    /* create and reuse cipher objects */
    private Cipher key2EncryptCipher = null;
    private Cipher key2DecryptCipher = null;
    /* end create and reuse cipher objects */

    private Registry registry;

    public SuiseClient(Registry registry, SuiseUtil suiseUtil) {
        this.registry = registry;
        this.suiseUtil = suiseUtil;
        this.searchHistory = new HashSet<>();
    }

    public SuiseClient(Registry registry, SuiseUtil suiseUtil, byte[] key1, byte[] key2, byte[] iv1, byte[] iv2) {
        this(registry, suiseUtil);
        this.key1 = key1;
        this.key2 = key2;
        this.iv1 = iv1;
        this.iv2 = iv2;
    }

    public byte[] getIv1() {
        return iv1;
    }

    public void setIv1(byte[] iv1) {
        this.iv1 = iv1;
    }

    public byte[] getIv2() {
        return iv2;
    }

    public void setIv2(byte[] iv2) {
        this.iv2 = iv2;
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

    public void Dec(InputStream fis, OutputStream fos) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        if (key2DecryptCipher == null) {
            this.key2DecryptCipher = BlockCipherFactory.getCipher(BlockCipherFactory.CIPHER_AES,
                    BlockCipherFactory.CIPHER_AES + BlockCipherFactory.SEP + BlockCipherFactory.MODE_CBC + BlockCipherFactory.SEP + BlockCipherFactory.PADDING_PKCS5,
                    Cipher.DECRYPT_MODE,
                    key2,
                    iv2);
        }

        BufferedInputStream is = null;
        BufferedOutputStream os = null;
        try {
            is = new BufferedInputStream(new CipherInputStream(fis, key2DecryptCipher));
            os = new BufferedOutputStream(fos);
            int buffer = -1;
            while ((buffer = is.read()) != -1) {
                os.write(buffer);
            }
            os.flush();
        } catch (Exception e) {
            registry.getLogger().log(TAG, "error when invoking " + TAG + ".Dec(FileInputStream,OutputStream)");
        }

        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
            }
        }

        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
            }
        }
    }

    public void Enc(InputStream fis, OutputStream fos) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {

        if (key2EncryptCipher == null) {
            this.key2EncryptCipher = BlockCipherFactory.getCipher(BlockCipherFactory.CIPHER_AES,
                    BlockCipherFactory.CIPHER_AES + BlockCipherFactory.SEP + BlockCipherFactory.MODE_CBC + BlockCipherFactory.SEP + BlockCipherFactory.PADDING_PKCS5,
                    Cipher.ENCRYPT_MODE,
                    key2,
                    iv2);
        }

        BufferedOutputStream os = null;
        BufferedInputStream is = null;
        try {
            is = new BufferedInputStream(fis);
            os = new BufferedOutputStream(new CipherOutputStream(fos, key2EncryptCipher));
            int buffer = -1;
            while ((buffer = is.read()) != -1) {
                os.write(buffer);
            }
            os.flush();
        } catch (Exception e) {
            registry.getLogger().log(TAG, "error when invoking " + TAG + ".Enc(FileInputStream,OutputStream)");
        }

        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
            }
        }

        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
    }

    public void Enc(File fi, File fo) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        try {
            Enc(new FileInputStream(fi), new FileOutputStream(fo));
        } catch (IOException e) {
            registry.getLogger().log(TAG, "error when invoking " + TAG + ".Enc(File,File)");
        }
    }

    public void Dec(File fi, File fo) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        try {
            Dec(new FileInputStream(fi), new FileOutputStream(fo));
        } catch (IOException e) {
            registry.getLogger().log(TAG, "error when invoking " + TAG + ".Dec(File,File)");
        }
    }

    private String encryptStr(String message) {
        String result = null;

        try {
            byte[] data = registry.getBlockCipherCbc().encrypt(iv1, key1, message.getBytes(LibConstants.ENCODING_UTF8));
            result = registry.getBase64().encodeToString(data);
        } catch (Exception ex) {
            registry.getLogger().log(TAG, "error when invoking " + TAG + ".encryptStr(String)");
        }
        return result;
    }

    public AddTokenResult AddToken(InputStream inputStream, boolean includeSubStr, String docId, IFileParser fileParser) {
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
        IBlockCipherCbc aes128Cbc = registry.getBlockCipherCbc();

        for (int i = 0; i < uniqueWordCnt; i++) {

            suiseUtil.setRandomBytes(randomBytes, i);

            byte[] encWord = null;
            try {
                encWord = aes128Cbc.encrypt(iv1, key1, uniqueWordList.get(i).getBytes(LibConstants.ENCODING_UTF8));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

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

    public AddTokenResult AddToken(File inFile, boolean includeSubStr, String docId, IFileParser fileParser) {
        AddTokenResult result = null;

        try {
            result = this.AddToken(new BufferedInputStream(new FileInputStream(inFile)), includeSubStr, docId, fileParser);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return result;
    }

    public SearchTokenResult SearchToken(String keyword) {

        SearchTokenResult result = new SearchTokenResult();

        try {
            byte[] data = registry.getBlockCipherCbc().encrypt(iv1, key1, keyword.getBytes(LibConstants.ENCODING_UTF8));
            result.setSearchToken(registry.getBase64().encodeToString(data));
            searchHistory.add(keyword);
        } catch (Exception ex) {
            registry.getLogger().log(TAG, "error when invoking " + TAG + ".SearchToken(File,boolean)");
        }
        return result;
    }
}
