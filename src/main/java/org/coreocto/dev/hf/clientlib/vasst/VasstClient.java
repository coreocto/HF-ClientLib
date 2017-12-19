package org.coreocto.dev.hf.clientlib.vasst;

import ca.rmen.porterstemmer.PorterStemmer;
import org.coreocto.dev.hf.clientlib.Constants;
import org.coreocto.dev.hf.commonlib.crypto.BlockCipherFactory;
import org.coreocto.dev.hf.commonlib.util.Registry;
import org.coreocto.dev.hf.commonlib.vasst.bean.TermFreq;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

public class VasstClient {

    public static final int BLOCK_SIZE_IN_BYTE = 16;
    private static final String TAG = "VasstClient";
    private static final byte[] DEFAULT_IV = new byte[BLOCK_SIZE_IN_BYTE];
    private Registry registry;
    private byte[] secretKey = null;
    private Cipher key2EncryptCipher = null;
    private Cipher key2DecryptCipher = null;

    public VasstClient(Registry registry) {
        this.registry = registry;
    }

    public byte[] getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(byte[] secretKey) {
        this.secretKey = secretKey;
    }

    public void GenKey(int noOfBytes) {
        byte[] randomBytes = new byte[noOfBytes];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(randomBytes);
        this.secretKey = randomBytes;
    }

    //Preprocessing(files,sk,x)
    public TermFreq Preprocessing(File inFile, byte x) {
        BufferedReader in = null;

        List<String> wordList = new ArrayList<>();

        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), Constants.UTF8));
            String tempStr = null;
            while ((tempStr = in.readLine()) != null) {
                tempStr = tempStr.toLowerCase();
                wordList.addAll(Arrays.asList(tempStr.split(org.coreocto.dev.hf.clientlib.Constants.SPACE)));
            }
        } catch (Exception e) {
            registry.getLogger().log(TAG, "error when invoking " + TAG + ".Preprocessing(File,byte)");
            e.printStackTrace();
        }

        if (in != null) {
            try {
                in.close();
            } catch (IOException iox) {

            }
        }

        //filter stop words
        removeStopWords(wordList);

        //do stemming with PorterStemmer
        doStemming(wordList);

        int wordListSize = wordList.size();

        // word list de-duplication & calculate term freq.
        TermFreq termFreq = new TermFreq();
        for (int i = 0; i < wordListSize; i++) {
            termFreq.inc(wordList.get(i));
        }

        // encrypt each term
        Map<String, Integer> terms = termFreq.getTerms();
        Map<String, Integer> encTerms = new HashMap<>();

        for (Iterator<Map.Entry<String, Integer>> it = terms.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Integer> entry = it.next();
            String key = entry.getKey();
            if (key == null) {
                continue;
            }
            String firstRd = encryptStrByCharPos(key, x);
            String secondRd = encryptStr(firstRd);
            encTerms.put(secondRd, entry.getValue());
        }

        terms.clear();
        terms.putAll(encTerms);
        return termFreq;
    }

    private String encryptStrByCharPos(String message, byte x) {
        if (message == null || message.isEmpty()) {
            return message;
        } else {
            int len = message.length();
            double sum = 0;
            for (int i = 0; i < len; i++) {
                byte ascii = (byte) message.charAt(i);
                double result = ascii * Math.pow(x, len - i);
                sum += result;
            }
            return Double.toHexString(sum);
        }
    }

    private String encryptStr(String message) {
        String result = null;

        try {
            byte[] data = registry.getBlockCipherCbc().encrypt(DEFAULT_IV, secretKey, message.getBytes(Constants.UTF8));
            result = registry.getBase64().encodeToString(data);
        } catch (Exception ex) {
            registry.getLogger().log(TAG, "error when invoking " + TAG + ".encryptStr(String)");
        }
        return result;
    }

    private void removeStopWords(List<String> wordList) {

        if (wordList == null || wordList.isEmpty()) {
            return;
        }

        List<String> stopWords = new ArrayList<>();

        BufferedReader in = null;

        // load stop words
        try {
            InputStream is = this.getClass().getResourceAsStream("eng-stopwords.txt");
            in = new BufferedReader(new InputStreamReader(is));
            String tempStr = null;

            while ((tempStr = in.readLine()) != null) {
                tempStr = tempStr.toLowerCase();
                stopWords.add(tempStr);
            }

        } catch (Exception e) {
            registry.getLogger().log(TAG, "error when invoking " + TAG + ".removeStopWords(List<String>)");
        }
        // end load stop words

        if (in != null) {
            try {
                in.close();
            } catch (IOException iox) {

            }
        }

        wordList.remove(stopWords);
    }

    private void doStemming(List<String> wordList) {

        if (wordList == null || wordList.isEmpty()) {
            return;
        }

        int size = wordList.size();

        PorterStemmer porterStemmer = new PorterStemmer();
        for (int i = 0; i < size; i++) {
            wordList.set(i, porterStemmer.stemWord(wordList.get(i)));
        }
    }

    public void CreateReq(String query, int x) {
        List<String> splittedQuery = Arrays.asList(query.split(Constants.SPACE));


    }

    public void Encrypt(File fi, File fo) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        try {
            Encrypt(new FileInputStream(fi), new FileOutputStream(fo));
        } catch (IOException e) {
            registry.getLogger().log(TAG, "error when invoking " + TAG + ".Enc(File,File)");
        }
    }

    public void Decrypt(File fi, File fo) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        try {
            Decrypt(new FileInputStream(fi), new FileOutputStream(fo));
        } catch (IOException e) {
            registry.getLogger().log(TAG, "error when invoking " + TAG + ".Dec(File,File)");
        }
    }

    public void Encrypt(FileInputStream fis, OutputStream fos) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {

        if (key2EncryptCipher == null) {
            this.key2EncryptCipher = BlockCipherFactory.getCipher(BlockCipherFactory.CIPHER_AES,
                    BlockCipherFactory.CIPHER_AES + BlockCipherFactory.SEP + BlockCipherFactory.MODE_CBC + BlockCipherFactory.SEP + BlockCipherFactory.PADDING_PKCS5,
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    DEFAULT_IV);
        }

        BufferedInputStream is = null;
        BufferedOutputStream os = null;

        try {
            is = new BufferedInputStream(fis);
            os = new BufferedOutputStream(new CipherOutputStream(fos, key2EncryptCipher));
            int data = -1;
            while ((data = is.read()) != -1) {
                os.write(data);
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

    public void Decrypt(FileInputStream fis, OutputStream fos) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        if (key2DecryptCipher == null) {
            this.key2DecryptCipher = BlockCipherFactory.getCipher(BlockCipherFactory.CIPHER_AES,
                    BlockCipherFactory.CIPHER_AES + BlockCipherFactory.SEP + BlockCipherFactory.MODE_CBC + BlockCipherFactory.SEP + BlockCipherFactory.PADDING_PKCS5,
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    DEFAULT_IV);
        }

        BufferedInputStream is = null;
        BufferedOutputStream os = null;

        try {
            is = new BufferedInputStream(new CipherInputStream(fis, key2DecryptCipher));
            os = new BufferedOutputStream(fos);
            int data = -1;
            while ((data = is.read()) != -1) {
                os.write(data);
            }
            os.flush();

        } catch (Exception e) {
            registry.getLogger().log(TAG, "error when invoking " + TAG + ".Dec(FileInputStream,OutputStream)");
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

}
