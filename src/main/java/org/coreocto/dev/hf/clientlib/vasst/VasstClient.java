package org.coreocto.dev.hf.clientlib.vasst;

import ca.rmen.porterstemmer.PorterStemmer;
import org.coreocto.dev.hf.clientlib.LibConstants;
import org.coreocto.dev.hf.clientlib.parser.IFileParser;
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

    private static final String TAG = "VasstClient";
    private Registry registry;
    private byte[] secretKey = null;
    private byte[] iv = null;
    private Cipher keyEncryptCipher = null;
    private Cipher keyDecryptCipher = null;

    public VasstClient(Registry registry) {
        this.registry = registry;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
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

    public TermFreq Preprocessing(InputStream inputStream, byte x, IFileParser fileParser) {
        List<String> wordList = fileParser.getText(new BufferedInputStream(inputStream));

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

    //Preprocessing(files,sk,x)
    public TermFreq Preprocessing(File inFile, byte x, IFileParser fileParser) {
        TermFreq result = null;
        try {
            result = this.Preprocessing(new FileInputStream(inFile), x, fileParser);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
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
            byte[] data = registry.getBlockCipherCbc().encrypt(iv, secretKey, message.getBytes(LibConstants.ENCODING_UTF8));
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

    public List<String> CreateReq(String query, byte x) {

        List<String> result = new ArrayList<>();
        if (query == null || query.isEmpty()) {
            return result;
        }

        List<String> keywords = Arrays.asList(query.split(LibConstants.REGEX_SPACE));

        //filter stop words
        removeStopWords(keywords);

        //do stemming with PorterStemmer
        doStemming(keywords);

        Set<String> uniqueKeywords = new HashSet<>();
        uniqueKeywords.addAll(keywords);

        for (String uniqueKeyword : uniqueKeywords) {
            String firstRd = encryptStrByCharPos(uniqueKeyword, x);
            String secondRd = encryptStr(firstRd);
            result.add(secondRd);
        }

        //this list is returned by Arrays.asList(...), it would throw a UnsupportedOperationException if remove() or clear() is invoked
        //keywords.clear();

        uniqueKeywords.clear();

        return result;
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

    public void Encrypt(InputStream fis, OutputStream fos) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {

        if (keyEncryptCipher == null) {
            this.keyEncryptCipher = BlockCipherFactory.getCipher(BlockCipherFactory.CIPHER_AES,
                    BlockCipherFactory.CIPHER_AES + BlockCipherFactory.SEP + BlockCipherFactory.MODE_CBC + BlockCipherFactory.SEP + BlockCipherFactory.PADDING_PKCS5,
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    iv);
        }

        BufferedInputStream is = null;
        BufferedOutputStream os = null;

        try {
            is = new BufferedInputStream(fis);
            os = new BufferedOutputStream(new CipherOutputStream(fos, keyEncryptCipher));
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

    public void Decrypt(InputStream fis, OutputStream fos) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        if (keyDecryptCipher == null) {
            this.keyDecryptCipher = BlockCipherFactory.getCipher(BlockCipherFactory.CIPHER_AES,
                    BlockCipherFactory.CIPHER_AES + BlockCipherFactory.SEP + BlockCipherFactory.MODE_CBC + BlockCipherFactory.SEP + BlockCipherFactory.PADDING_PKCS5,
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    iv);
        }

        BufferedInputStream is = null;
        BufferedOutputStream os = null;

        try {
            is = new BufferedInputStream(new CipherInputStream(fis, keyDecryptCipher));
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
