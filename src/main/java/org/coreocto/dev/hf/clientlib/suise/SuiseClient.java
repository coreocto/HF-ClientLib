package org.coreocto.dev.hf.clientlib.suise;

import org.coreocto.dev.hf.commonlib.suise.bean.AddTokenResult;
import org.coreocto.dev.hf.commonlib.suise.bean.SearchTokenResult;
import org.coreocto.dev.hf.commonlib.suise.util.SuiseUtil;
import org.coreocto.dev.hf.commonlib.util.CipherFactory;
import org.coreocto.dev.hf.commonlib.util.IAes128Cbc;
import org.coreocto.dev.hf.commonlib.util.ILogger;
import org.coreocto.dev.hf.commonlib.util.Util;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import java.io.*;
import java.util.*;

public class SuiseClient {

    private static final String SPACE = " ";

    private static final String TAG = "SuiseClient";
    private static final byte[] DEFAULT_IV = new byte[IAes128Cbc.BLOCK_SIZE_IN_BYTE];
    private String key1 = null;
    private String key2 = null;
    private Set<String> searchHistory = null;   //contains a set of searched keyword (in plain text)

    private SuiseUtil suiseUtil = null;
    private ILogger logger = null;

    /* create and reuse cipher objects */
    private Cipher key1EncryptCipher = null;    // for key1
    private Cipher key2EncryptCipher = null;    // for key2
    /* end create and reuse cipher objects */

    public SuiseClient(ILogger logger, SuiseUtil suiseUtil) {
        this.logger = logger;
        this.suiseUtil = suiseUtil;
        this.searchHistory = new HashSet<>();
    }

    public SuiseClient(ILogger logger, SuiseUtil suiseUtil, String key1, String key2) {
        this(logger, suiseUtil);
        this.key1 = key1;
        this.key2 = key2;
    }

    private Cipher getKey1EncryptCipher(byte[] key) {
        if (key1EncryptCipher == null) {
            this.key1EncryptCipher = new CipherFactory().getCipher(DEFAULT_IV, key, IAes128Cbc.CIPHER, IAes128Cbc.CIPHER_TRANSFORMATION, Cipher.ENCRYPT_MODE);
        }
        return key1EncryptCipher;
    }

    private Cipher getKey2EncryptCipher(byte[] key) {
        if (key2EncryptCipher == null) {
            this.key2EncryptCipher = new CipherFactory().getCipher(DEFAULT_IV, key, IAes128Cbc.CIPHER, IAes128Cbc.CIPHER_TRANSFORMATION, Cipher.ENCRYPT_MODE);
        }
        return key2EncryptCipher;
    }

    public String getKey1() {
        return key1;
    }

    public String getKey2() {
        return key2;
    }

    public void Gen(int noOfBytes) {
        this.key1 = suiseUtil.g(noOfBytes);
        this.key2 = suiseUtil.g(noOfBytes);
    }

    public void Enc(FileInputStream fis, OutputStream fos) {
        Cipher aesCipher = getKey1EncryptCipher(suiseUtil.getBase64().decodeToByteArray(key2));
        CipherOutputStream os = null;
        try {
            os = new CipherOutputStream(fos, aesCipher);
            suiseUtil.copy(4096, fis, os);
            os.flush();
        } catch (Exception e) {
            logger.log(TAG, "error when invoking LocalClientImpl.Enc(FileInputStream,OutputStream)");
            e.printStackTrace();
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

    public void Enc(File fi, File fo) {
        try {
            Enc(new FileInputStream(fi), new FileOutputStream(fo));
        } catch (IOException e) {
        }
    }

    private String encryptStr(String message) {
        String result = null;
        Cipher aesCipher = this.getKey1EncryptCipher(suiseUtil.getBase64().decodeToByteArray(key1));
        try {
            result = suiseUtil.getBase64().encodeToString(aesCipher.doFinal(message.getBytes()));
        } catch (Exception ex) {
            ex.printStackTrace();
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
            e.printStackTrace();
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

        for (int i = 0; i < uniqueWordCnt; i++) {

            suiseUtil.setRandomBytes(randomBytes, i);

            String randomVal = suiseUtil.getBase64().encodeToString(randomBytes);

            String searchToken = encryptStr(uniqueWordList.get(i));
            if (searchHistory.contains(searchToken)) {
                x.add(searchToken);
            }

            String h = suiseUtil.H(searchToken, randomVal);

            c.add(h + randomVal);
        }

        /*
        c.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        */

        String docId = suiseUtil.getBase64().encodeToString(suiseUtil.getMd5().getHash(inFile.getName().getBytes()));

        result.setId(docId);
        result.setC(c);
        result.setX(x);

        return result;
    }

    public SearchTokenResult SearchToken(String keyword) {

        SearchTokenResult result = new SearchTokenResult();

        Cipher aesCipher = this.getKey1EncryptCipher(suiseUtil.getBase64().decodeToByteArray(key1));

        try {
            result.setSearchToken(suiseUtil.getBase64().encodeToString(aesCipher.doFinal(keyword.getBytes("UTF-8"))));
            searchHistory.add(keyword);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
}
