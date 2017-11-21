package org.coreocto.dev.hf.clientlib.vasst;

import org.coreocto.dev.hf.clientlib.Constants;
import org.coreocto.dev.hf.commonlib.util.Registry;

import java.io.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VasstClient {

    private static final String TAG = "VasstClient";
    private Registry registry;
    private byte[] secretKey = null;

    public VasstClient(Registry registry) {
        this.registry = registry;
    }

    public void GenKey(int noOfBytes) {
        byte[] randomBytes = new byte[noOfBytes];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(randomBytes);
        this.secretKey = randomBytes;
    }

    public byte[] encrypt(String s){
return null;
    }

    //Preprocessing(files,sk,x)
    public void Preprocessing(File[] f) {
        BufferedReader in = null;

        int max = f.length;

        Set<String> uniqueWordSet = new HashSet<>();

        List<String> stopWords = new ArrayList<>();

        //load stop words
        try {
            in = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("eng-stopwords.txt")));
            String tempStr = null;

            while ((tempStr = in.readLine()) != null) {
                stopWords.add(tempStr);
            }

        } catch (Exception e) {
            registry.getLogger().log(TAG, "error when invoking " + TAG + ".AddToken(File,boolean)");
        }

        if (in != null) {
            try {
                in.close();
            } catch (IOException iox) {

            }
        }

        //extract token from file collections
        for (int i = 0; i < max; i++) {

            File inFile = f[i];

            try {
                in = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), "UTF-8"));
                String tempStr = null;
                while ((tempStr = in.readLine()) != null) {
                    for (String word : tempStr.split(Constants.SPACE)) {
                        uniqueWordSet.add(word);
                    }
                }
            } catch (Exception e) {
                registry.getLogger().log(TAG, "error when invoking " + TAG + ".AddToken(File,boolean)");
            }
        }

        if (in != null) {
            try {
                in.close();
            } catch (IOException iox) {

            }
        }

        //filter stop words
        uniqueWordSet.removeAll(stopWords);

        //calculate relevance scores for each file using tf-idf

        //encrypt indexes using symmetric key sk

        //encrypt the file
    }
}
