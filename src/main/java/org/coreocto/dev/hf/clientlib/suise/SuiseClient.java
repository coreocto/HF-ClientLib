package org.coreocto.dev.hf.clientlib.suise;

import org.coreocto.dev.hf.commonlib.suise.bean.AddTokenResult;
import org.coreocto.dev.hf.commonlib.suise.bean.SearchTokenResult;
import org.coreocto.dev.hf.commonlib.suise.util.SuiseUtil;
import org.coreocto.dev.hf.commonlib.util.*;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class SuiseClient {

    private static final String SPACE = " ";

    private static final String TAG = "SuiseClient";
    private static final byte[] DEFAULT_IV = new byte[IAes128Cbc.BLOCK_SIZE_IN_BYTE];
    private static final String UTF8 = "UTF-8";
//    private String key1 = null;
//    private String key2 = null;

    private byte[] key1 = null;
    private byte[] key2 = null;

    private Set<String> searchHistory = null;   //contains a set of searched keyword (in plain text)

    private SuiseUtil suiseUtil = null;
    private ILogger logger = null;

    /* create and reuse cipher objects */
//    private Cipher key1EncryptCipher = null;    // for key1
    private Cipher key2EncryptCipher = null;    // for key2
    /* end create and reuse cipher objects */

    private Registry registry;

    public SuiseClient(Registry registry, SuiseUtil suiseUtil) {
        this.registry = registry;
        this.suiseUtil = suiseUtil;
        this.searchHistory = new HashSet<>();
    }

//    public SuiseClient(Registry registry, SuiseUtil suiseUtil, String key1, String key2) {
//        this(registry, suiseUtil);
//        this.key1 = registry.getBase64().decodeToByteArray(key1);
//        this.key2 = registry.getBase64().decodeToByteArray(key2);
//    }

    public SuiseClient(Registry registry, SuiseUtil suiseUtil, byte[] key1, byte[] key2) {
        this(registry, suiseUtil);
        this.key1 = key1;
        this.key2 = key2;
    }

//    private Cipher getKey1EncryptCipher(byte[] key) {
//        if (key1EncryptCipher == null) {
//            this.key1EncryptCipher = new CipherFactory().getCipher(DEFAULT_IV, key, IAes128Cbc.CIPHER, IAes128Cbc.CIPHER_TRANSFORMATION, Cipher.ENCRYPT_MODE);
//        }
//        return key1EncryptCipher;
//    }

//    private Cipher getKey2EncryptCipher(byte[] key) {
//        if (key2EncryptCipher == null) {
//            this.key2EncryptCipher = new CipherFactory().getCipher(DEFAULT_IV, key, IAes128Cbc.CIPHER, IAes128Cbc.CIPHER_TRANSFORMATION, Cipher.ENCRYPT_MODE);
//        }
//        return key2EncryptCipher;
//    }

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

    public void Enc(FileInputStream fis, OutputStream fos) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {

        if (key2EncryptCipher == null) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key2, IAes128Cbc.CIPHER);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(DEFAULT_IV);

            this.key2EncryptCipher = Cipher.getInstance(IAes128Cbc.CIPHER_TRANSFORMATION);
            this.key2EncryptCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        }

        CipherOutputStream os = null;
        try {
            os = new CipherOutputStream(fos, key2EncryptCipher);
            suiseUtil.copy(4096, fis, os);
            os.flush();
        } catch (Exception e) {
            logger.log(TAG, "error when invoking " + TAG + ".Enc(FileInputStream,OutputStream)");
        }

        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
            }
        }

        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
            }
        }
    }

    public void Enc(File fi, File fo) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        try {
            Enc(new FileInputStream(fi), new FileOutputStream(fo));
        } catch (IOException e) {
            logger.log(TAG, "error when invoking " + TAG + ".Enc(File,File)");
        }
    }

    private String encryptStr(String message) {
        String result = null;

        try {
            byte[] data = registry.getAes128Cbc().encrypt(DEFAULT_IV, key1, message.getBytes(UTF8));
            result = registry.getBase64().encodeToString(data);
        } catch (Exception ex) {
            logger.log(TAG, "error when invoking " + TAG + ".encryptStr(String)");
        }
        return result;
    }

    public AddTokenResult AddToken(File inFile, boolean includeSubStr) {

        AddTokenResult result = new AddTokenResult();

        Set<String> uniqueWordSet = new HashSet<>();
        List<String> x = new ArrayList<>();
        List<String> c = new ArrayList<>();

        BufferedReader in = null;

        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), "UTF-8"));
            String tempStr = null;
            while ((tempStr = in.readLine()) != null) {
                //for (String word : tempStr.split("\\s")) {
                for (String word : tempStr.split(SPACE)) {
                    uniqueWordSet.add(word);
                }
            }
        } catch (Exception e) {
            logger.log(TAG, "error when invoking " + TAG + ".AddToken(File,boolean)");
        }

        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
            }
        }

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

        byte[] randomBytes = new byte[IAes128Cbc.BLOCK_SIZE_IN_BYTE];

        IBase64 base64 = registry.getBase64();
        IAes128Cbc aes128Cbc = registry.getAes128Cbc();

        for (int i = 0; i < uniqueWordCnt; i++) {

            suiseUtil.setRandomBytes(randomBytes, i);

            byte[] encWord = null;
            try {
                encWord = aes128Cbc.encrypt(DEFAULT_IV, key1, uniqueWordList.get(i).getBytes(UTF8));
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

        String fileName = inFile.getAbsolutePath();

        String docId = base64.encodeToString(aes128Cbc.encrypt(DEFAULT_IV, key1, fileName.getBytes()));

        result.setId(docId);
        result.setC(c);
        result.setX(x);

        return result;
    }

    public SearchTokenResult SearchToken(String keyword) {

        SearchTokenResult result = new SearchTokenResult();

//        Cipher aesCipher = this.getKey1EncryptCipher(suiseUtil.getBase64().decodeToByteArray(key1));

        try {
//            result.setSearchToken(suiseUtil.getBase64().encodeToString(aesCipher.doFinal(keyword.getBytes("UTF-8"))));
            byte[] data = registry.getAes128Cbc().encrypt(DEFAULT_IV, key1, keyword.getBytes(UTF8));
            result.setSearchToken(registry.getBase64().encodeToString(data));
            searchHistory.add(keyword);
        } catch (Exception ex) {
            logger.log(TAG, "error when invoking " + TAG + ".SearchToken(File,boolean)");
        }
        return result;
    }
}
