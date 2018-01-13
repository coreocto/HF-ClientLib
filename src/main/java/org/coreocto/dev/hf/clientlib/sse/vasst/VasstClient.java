package org.coreocto.dev.hf.clientlib.sse.vasst;

import ca.rmen.porterstemmer.PorterStemmer;
import org.coreocto.dev.hf.clientlib.LibConstants;
import org.coreocto.dev.hf.clientlib.crypto.DummyFcImpl;
import org.coreocto.dev.hf.clientlib.parser.IFileParser;
import org.coreocto.dev.hf.commonlib.crypto.IByteCipher;
import org.coreocto.dev.hf.commonlib.crypto.IFileCipher;
import org.coreocto.dev.hf.commonlib.sse.vasst.bean.TermFreq;
import org.coreocto.dev.hf.commonlib.util.Registry;
import org.coreocto.dev.hf.commonlib.util.Util;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.math.BigDecimal;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class VasstClient {

    private static final String TAG = "VasstClient";
    private Registry registry;
    private byte[] secretKey = null;

    public boolean isDataProtected() {
        return dataProtected;
    }

    public void setDataProtected(boolean dataProtected) {
        this.dataProtected = dataProtected;
    }

    private boolean dataProtected = true;

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
        Util.fillRandomBytes(randomBytes);
        this.secretKey = randomBytes;
    }

    public TermFreq Preprocessing(InputStream inputStream, BigDecimal x, IFileParser fileParser, IByteCipher byteCipher) throws IOException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException {
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
        if (dataProtected) {
            Map<String, Integer> encTerms = new HashMap<>();

            for (Iterator<Map.Entry<String, Integer>> it = terms.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Integer> entry = it.next();
                String key = entry.getKey();
                if (key == null) {
                    continue;
                }
                String firstRd = encryptStr(key, byteCipher);
                String secondRd = encryptStrByCharPos(firstRd, x);
                encTerms.put(secondRd, entry.getValue());
            }

            terms.clear();
            terms.putAll(encTerms);
        }
        return termFreq;
    }

    //Preprocessing(files, sk, x)
    public TermFreq Preprocessing(File inFile, BigDecimal x, IFileParser fileParser, IByteCipher byteCipher) throws IOException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        return this.Preprocessing(new FileInputStream(inFile), x, fileParser, byteCipher);
    }

    private String encryptStrByCharPos(String message, BigDecimal x) {
        if (message == null || message.isEmpty()) {
            return message;
        } else {
            int len = message.length();
            //double sum = 0;
            BigDecimal sum = BigDecimal.ZERO;
            for (int i = 0; i < len; i++) {
                byte ascii = (byte) message.charAt(i);
                BigDecimal partB = x.pow(len - i);
                BigDecimal ascii_in_bd = BigDecimal.valueOf(ascii);
                sum = sum.add(partB.multiply(ascii_in_bd));
            }
            return sum.toPlainString();
        }
    }

    private String encryptStr(String message, IByteCipher byteCipher) throws UnsupportedEncodingException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        byte[] enc = byteCipher.encrypt(message.getBytes(LibConstants.ENCODING_UTF8));
        return registry.getBase64().encodeToString(enc);
    }

    private static List<String> stopWords = new ArrayList<>();

    //this is a method to cache stop word in memory, so that we don't have to load it over and over again.
    private static List<String> getStopWords() throws IOException {
        if (stopWords.isEmpty()) {
            // load stop words
            InputStream is = VasstClient.class.getResourceAsStream("/org/coreocto/dev/hf/clientlib/sse/vasst/eng-stopwords.txt");   //need absolute path to load resource correctly
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String tempStr = null;

            while ((tempStr = in.readLine()) != null) {
                tempStr = tempStr.toLowerCase();
                stopWords.add(tempStr);
            }
            // end load stop words

            if (in != null) {
                in.close();
            }
        }
        return stopWords;
    }

    private void removeStopWords(List<String> wordList) throws IOException {

        if (wordList == null || wordList.isEmpty()) {
            return;
        }

        List<String> stopWords = getStopWords();//new ArrayList<>();

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

    public List<String> CreateReq(String query, BigDecimal x, IByteCipher byteCipher) throws IOException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException {

        List<String> result = new ArrayList<>();
        if (query == null || query.isEmpty()) {
            return result;
        }

        List<String> keywords = Arrays.asList(query.split(LibConstants.REGEX_SPACE));

        //filter stop words
        removeStopWords(keywords);

        //do stemming with PorterStemmer
        doStemming(keywords);

        //filter unique words with HashSet
        Set<String> uniqueKeywords = new HashSet<>();
        uniqueKeywords.addAll(keywords);

        if (dataProtected) {
            for (String uniqueKeyword : uniqueKeywords) {
                String firstRd = encryptStr(uniqueKeyword, byteCipher);
                String secondRd = encryptStrByCharPos(firstRd, x);
                result.add(secondRd);
            }

            //this list is returned by Arrays.asList(...), it would throw a UnsupportedOperationException if remove() or clear() is invoked
            //keywords.clear();

            uniqueKeywords.clear();

            return result;
        } else {
            return new ArrayList<>(uniqueKeywords);
        }
    }

    public void Encrypt(File fi, File fo, IFileCipher fileCipher) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IOException {
        this.Encrypt(new FileInputStream(fi), new FileOutputStream(fo), fileCipher);
    }

    public void Decrypt(File fi, File fo, IFileCipher fileCipher) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IOException {
        this.Decrypt(new FileInputStream(fi), new FileOutputStream(fo), fileCipher);
    }

    public void Encrypt(InputStream fis, OutputStream fos, IFileCipher fileCipher) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IOException {
        if (dataProtected) {
            fileCipher.encrypt(fis, fos);
        } else {
            DummyFcImpl.getInstance().encrypt(fis, fos);
        }
    }

    public void Decrypt(InputStream fis, OutputStream fos, IFileCipher fileCipher) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IOException {
        if (dataProtected) {
            fileCipher.decrypt(fis, fos);
        } else {
            DummyFcImpl.getInstance().decrypt(fis, fos);
        }
    }

}
