package org.coreocto.dev.hf.clientlib.sse.mces;

import com.google.gson.Gson;
import org.coreocto.dev.hf.clientlib.LibConstants;
import org.coreocto.dev.hf.clientlib.crypto.AesCbcPkcs5BcImpl;
import org.coreocto.dev.hf.clientlib.crypto.HmacMd5;
import org.coreocto.dev.hf.clientlib.parser.IFileParser;
import org.coreocto.dev.hf.commonlib.crypto.IByteCipher;
import org.coreocto.dev.hf.commonlib.util.IBase64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class McesClient {

    public static final String SEP = "<%>";

    private byte[] k1;
    private byte[] k2;
    private byte[] k3;

    public void setK1(byte[] k1) {
        this.k1 = k1;
    }

    public void setK2(byte[] k2) {
        this.k2 = k2;
    }

    public void setK3(byte[] k3) {
        this.k3 = k3;
    }

    public void setK4(byte[] k4) {
        this.k4 = k4;
    }

    public void setKd(byte[] kd) {
        this.kd = kd;
    }

    public void setKc(byte[] kc) {
        this.kc = kc;
    }

    public void setKl(byte[] kl) {
        this.kl = kl;
    }

    private byte[] k4;
    private byte[] kd;
    private byte[] kc;
    private byte[] kl;
    private IBase64 base64;
    private int noOfBytes;

    public McesClient(IBase64 base64) {
        this.base64 = base64;
    }

    public byte[] getK1() {
        return k1;
    }

    public byte[] getK2() {
        return k2;
    }

    public byte[] getK3() {
        return k3;
    }

    public byte[] getK4() {
        return k4;
    }

    public byte[] getKd() {
        return kd;
    }

    public byte[] getKc() {
        return kc;
    }

    public byte[] getKl() {
        return kl;
    }

    public void Gen(int noOfBytes) {
        k1 = new byte[noOfBytes];
        k2 = new byte[noOfBytes];
        k3 = new byte[noOfBytes];
        k4 = new byte[noOfBytes];
        kd = new byte[noOfBytes];
        kc = new byte[noOfBytes];
        kl = new byte[noOfBytes];
        Random random = new SecureRandom();
        random.nextBytes(k1);
        random.nextBytes(k2);
        random.nextBytes(k3);
        random.nextBytes(k4);
        random.nextBytes(kd);
        random.nextBytes(kc);
        random.nextBytes(kl);
        this.noOfBytes = noOfBytes;
    }

    public void GenZero(int noOfBytes) {
        k1 = new byte[noOfBytes];
        k2 = new byte[noOfBytes];
        k3 = new byte[noOfBytes];
        k4 = new byte[noOfBytes];
        kd = new byte[noOfBytes];
        kc = new byte[noOfBytes];
        kl = new byte[noOfBytes];
        this.noOfBytes = noOfBytes;
    }

    private String getRandomStringInBase64(int noOfBytes) {
        Random random = new Random();
        byte[] bytes = new byte[noOfBytes];
        random.nextBytes(bytes);
        return base64.encodeToString(bytes);
    }

    private HmacMd5 keyHashFunc = new HmacMd5();

    private String f1(SuffixTree.Node u) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {
        byte[] initpath_bytes = (u.initpath()).getBytes(LibConstants.ENCODING_UTF8);
        return base64.encodeToString(keyHashFunc.getHash(this.getK1(), initpath_bytes));
    }

    private String f2(SuffixTree.Node u) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {
        byte[] initpath_bytes = (u.initpath()).getBytes(LibConstants.ENCODING_UTF8);
        return base64.encodeToString(keyHashFunc.getHash(this.getK2(), initpath_bytes));
    }

    public List<CT> Enc(InputStream is, KeyCipher keyCipher, IFileParser fileParser) throws UnsupportedEncodingException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidAlgorithmParameterException {

        List<CT> ctList = new ArrayList<>();

        List<String> keywords = fileParser.getText(is);

        for (String keyword : keywords) {
            CT finalResult = new CT();

            SuffixTree suffixTree = new SuffixTree();

            suffixTree.assignLeafIdDfs();

            Map<String, List<String>> D = finalResult.getD();

            int n = keyword.length();

            List<SuffixTree.Node> allNodes = suffixTree.getAllNodes(false);
            int totalNodeCnt = allNodes.size();

            for (int z = 0; z < totalNodeCnt; z++) {

                SuffixTree.Node u = allNodes.get(z);

                List<String> g2 = new ArrayList<>();

                String f1 = f1(u);

                List<SuffixTree.Node> child = new ArrayList<>(u.values());//suffixTree.getChild(u);

                int childSize = child.size();

                for (int i = 0; i < childSize; i++) {
                    String encIniPath = f2(child.get(i));
                    g2.add(encIniPath);
                }

                byte[] randomBytes = new byte[noOfBytes];
                Random random = new Random(0);

                for (int i = childSize; i < n; i++) {
                    random.nextBytes(randomBytes);
                    String random_in_b64 = base64.encodeToString(randomBytes);
                    g2.add(random_in_b64);
                }

                //copy content from g2 to f2
                //and then perform permutation, shuffle the content
                List<String> f2 = new ArrayList<>(g2);
//            Collections.shuffle(f2, new Random(0));

                //Xu = (ind(u), leafpos(u), num(u), len(u), f1(u), f2,1(u),...,f2,d(u))
                X Xu = new X();
                Xu.setInd(u.getId());
                Xu.setLeafpos(u.leafpos());
                Xu.setNum(u.num());
                Xu.setLen(u.len());
                Xu.setF1(f1);
                Xu.getF2().addAll(f2);

                String Xu_in_json = new Gson().toJson(Xu);
//            System.out.println(Xu_in_json);

                byte[] Wu_in_bytes = keyCipher.getKdCipher().encrypt(Xu_in_json.getBytes(LibConstants.ENCODING_UTF8));

                String Wu = base64.encodeToString(Wu_in_bytes);

                List<String> Vu = new ArrayList<>(f2);
                Vu.add(Wu);

                System.out.println("\"" + u.initpath() + "\"\t\t->\t" + f1);
                D.put(f1, Vu);
            }

            //append dummy entries into dictionary D
            for (int i = 0; i < (2 * n - totalNodeCnt); i++) {
                String f1 = getRandomStringInBase64(noOfBytes);
                List<String> f2 = new ArrayList<>();
                for (int j = 0; j < n; j++) {
                    f2.add(getRandomStringInBase64(noOfBytes));
                }
                byte[] Wu_in_bytes = keyCipher.getKdCipher().encrypt(new byte[16]);
                String Wu = base64.encodeToString(Wu_in_bytes);

                List<String> Vu = new ArrayList<>(f2);
                Vu.add(Wu);
                D.put(f1, Vu);
            }

            // in original proposal, both C & L are arrays which use PRP to shuffle the content
            // i cannot find one right now, so i used the original index at the moment
            // update: added code to shuffle list contents
            Map<String, String> C = finalResult.getC();
            for (int i = 0; i < n; i++) {
                String tmp = keyword.substring(i, i + 1) + SEP + i;
                String key = base64.encodeToString(keyCipher.getKeyedHashFunc().getHash(this.getK3(), BigInteger.valueOf(i).toByteArray()));
                String val = base64.encodeToString(keyCipher.getKcCipher().encrypt(tmp.getBytes(LibConstants.ENCODING_UTF8)));
                C.put(key, val);
            }
//        Collections.shuffle(C, new Random(new BigInteger(this.getK3()).longValue()));

            Map<String, String> L = finalResult.getL();
            List<SuffixTree.Node> leaves = suffixTree.getAllNodes(true);
            int leafCnt = leaves.size();
            for (int i = 0; i < leafCnt; i++) {
                String tmp = leaves.get(i).getId() + SEP + i;
                String key = base64.encodeToString(keyCipher.getKeyedHashFunc().getHash(this.getK4(), BigInteger.valueOf(i).toByteArray()));
                String val = base64.encodeToString(keyCipher.getKlCipher().encrypt(tmp.getBytes(LibConstants.ENCODING_UTF8)));
                L.put(key, val);
            }
//        Collections.shuffle(L, new Random(new BigInteger(this.getK4()).longValue()));

            ctList.add(finalResult);
        }

        return ctList;
    }

    public CT Enc(String message, KeyCipher keyCipher) throws UnsupportedEncodingException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidAlgorithmParameterException {

        CT finalResult = new CT();

        SuffixTree suffixTree = new SuffixTree();
        suffixTree.addString(message);
        suffixTree.assignLeafIdDfs();

        Map<String, List<String>> D = finalResult.getD();

        int n = message.length();

        List<SuffixTree.Node> allNodes = suffixTree.getAllNodes(false);
        int totalNodeCnt = allNodes.size();

        for (int z = 0; z < totalNodeCnt; z++) {

            SuffixTree.Node u = allNodes.get(z);

            List<String> g2 = new ArrayList<>();

            String f1 = f1(u);

            List<SuffixTree.Node> child = new ArrayList<>(u.values());//suffixTree.getChild(u);

            int childSize = child.size();

            for (int i = 0; i < childSize; i++) {
                String encIniPath = f2(child.get(i));
                g2.add(encIniPath);
            }

            byte[] randomBytes = new byte[noOfBytes];
            Random random = new Random(0);

            for (int i = childSize; i < n; i++) {
                random.nextBytes(randomBytes);
                String random_in_b64 = base64.encodeToString(randomBytes);
                g2.add(random_in_b64);
            }

            //copy content from g2 to f2
            //and then perform permutation, shuffle the content
            List<String> f2 = new ArrayList<>(g2);
//            Collections.shuffle(f2, new Random(0));

            //Xu = (ind(u), leafpos(u), num(u), len(u), f1(u), f2,1(u),...,f2,d(u))
            X Xu = new X();
            Xu.setInd(u.getId());
            Xu.setLeafpos(u.leafpos());
            Xu.setNum(u.num());
            Xu.setLen(u.len());
            Xu.setF1(f1);
            Xu.getF2().addAll(f2);

            String Xu_in_json = new Gson().toJson(Xu);
//            System.out.println(Xu_in_json);

            byte[] Wu_in_bytes = keyCipher.getKdCipher().encrypt(Xu_in_json.getBytes(LibConstants.ENCODING_UTF8));

            String Wu = base64.encodeToString(Wu_in_bytes);

            List<String> Vu = new ArrayList<>(f2);
            Vu.add(Wu);

            System.out.println("\"" + u.initpath() + "\"\t\t->\t" + f1);
            D.put(f1, Vu);
        }

        //append dummy entries into dictionary D
        for (int i = 0; i < (2 * n - totalNodeCnt); i++) {
            String f1 = getRandomStringInBase64(noOfBytes);
            List<String> f2 = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                f2.add(getRandomStringInBase64(noOfBytes));
            }
            byte[] Wu_in_bytes = keyCipher.getKdCipher().encrypt(new byte[16]);
            String Wu = base64.encodeToString(Wu_in_bytes);

            List<String> Vu = new ArrayList<>(f2);
            Vu.add(Wu);
            D.put(f1, Vu);
        }

        // in original proposal, both C & L are arrays which use PRP to shuffle the content
        // i cannot find one right now, so i used the original index at the moment
        // update: added code to shuffle list contents
        Map<String, String> C = finalResult.getC();
        for (int i = 0; i < n; i++) {
            String tmp = message.substring(i, i + 1) + SEP + i;
            String key = base64.encodeToString(keyCipher.getKeyedHashFunc().getHash(this.k3, BigInteger.valueOf(i).toByteArray()));
            String val = base64.encodeToString(keyCipher.getKcCipher().encrypt(tmp.getBytes(LibConstants.ENCODING_UTF8)));
            C.put(key, val);
        }
//        Collections.shuffle(C, new Random(new BigInteger(this.getK3()).longValue()));

        Map<String, String> L = finalResult.getL();
        List<SuffixTree.Node> leaves = suffixTree.getAllNodes(true);
        int leafCnt = leaves.size();
        for (int i = 0; i < leafCnt; i++) {
            String tmp = leaves.get(i).getId() + SEP + i;
            String key = base64.encodeToString(keyCipher.getKeyedHashFunc().getHash(this.k4, BigInteger.valueOf(i).toByteArray()));
            String val = base64.encodeToString(keyCipher.getKlCipher().encrypt(tmp.getBytes(LibConstants.ENCODING_UTF8)));
            L.put(key, val);
        }
//        Collections.shuffle(L, new Random(new BigInteger(this.getK4()).longValue()));

        return finalResult;
    }

    public List<String> Query1(KeyCipher keyCipher, String p) throws UnsupportedEncodingException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidAlgorithmParameterException {

        List<String> output = new ArrayList<>();

        int m = p.length();

        HmacMd5 keyHashFunc = new HmacMd5();

        output.add(base64.encodeToString(keyHashFunc.getHash(this.getK1(), LibConstants.EMPTY_STRING.getBytes(LibConstants.ENCODING_UTF8))));

        for (int i = 0; i < m; i++) {
            String p1i = p.substring(0, i + 1);

            byte[] p1i_bytes = p1i.getBytes(LibConstants.ENCODING_UTF8);

            byte[] f1_in_bytes = keyHashFunc.getHash(this.getK1(), p1i_bytes);
//            String f1i = base64.encodeToString(f1_in_bytes);

            byte[] f2_in_bytes = keyHashFunc.getHash(this.getK2(), p1i_bytes);
//            String f2i = base64.encodeToString(f2_in_bytes);

            IByteCipher f2Cipher = new AesCbcPkcs5BcImpl(f2_in_bytes, new byte[noOfBytes]);

            byte[] Ti = f2Cipher.encrypt(f1_in_bytes);
            output.add(base64.encodeToString(Ti));
//            output.add(base64.encodeToString(f1_in_bytes));

            System.out.println(base64.encodeToString(f2_in_bytes) + " + " + base64.encodeToString(f1_in_bytes) + " = " + base64.encodeToString(Ti));
        }

        //(root,T1,...,Tm)
        return output;
    }

    //the return data type here is just temporary, may unify the data type later
    public List<String> Query3(KeyCipher keyCipher, String p, String X, List<String> Tis) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException {

        boolean canDec = true;

        String jsonStr = null;

        try {
            byte[] X_bytes = base64.decodeToByteArray(X);
            byte[] dec_X_bytes = keyCipher.getKdCipher().decrypt(X_bytes);
            jsonStr = new String(dec_X_bytes);
        } catch (BadPaddingException ex) {
            //sometime when different keys are used for encryption/decryption
            //it throws a BadPaddingException
            canDec = false;
        }

        if (!canDec) {
            return null;
        } else {
            Gson gson = new Gson();
            X x = gson.fromJson(jsonStr, X.class);

            HmacMd5 keyHashFunc = new HmacMd5();

            String f1 = x.getF1();

            String p_substr = p.substring(0, Math.min(p.length(), x.getLen()));

            byte[] enc_p_substr = keyHashFunc.getHash(this.getK1(), p_substr.getBytes(LibConstants.ENCODING_UTF8));

            String enc_p_substr_in_str = base64.encodeToString(enc_p_substr);

            if (f1 != null && f1.equals(enc_p_substr_in_str)) {

                int m = p.length();

                //the second half of the array should only consists of some random generated strings
                //so there should not be any successful decryption

                int d = x.getF2().size();

                for (int i = 0; i < d; i++) {

                    String s = x.getF2().get(i);
                    byte[] f2i_bytes = base64.decodeToByteArray(s);

                    IByteCipher byteCipher = new AesCbcPkcs5BcImpl(f2i_bytes, new byte[16]);

                    for (int j = x.getLen(); j < m; j++) {

                        boolean decryptFail = false;
                        byte[] tmp_bytes = null;
                        try {
                            tmp_bytes = byteCipher.decrypt(base64.decodeToByteArray(Tis.get(j)));
                        } catch (BadPaddingException ex) {
                            decryptFail = true;
                        }

                        if (!decryptFail) {
                            return null;
                        }
                    }
                }

                if (x.getInd() == 0) {
                    return new ArrayList<>();
                }

                int ind = x.getInd();

                //the loop below should be permutation instead of encryption
                //will implement later
                //TODO
                List<String> suffleX = new ArrayList<>();
                for (int i = 0; i < m; i++) {
                    byte[] data = BigInteger.valueOf(ind + i).toByteArray();
                    byte[] curX = keyCipher.getKeyedHashFunc().getHash(this.getK3(), data);
                    suffleX.add(base64.encodeToString(curX));
                }

                return suffleX;

            } else {
                return null;
            }
        }
    }

    public List<String> Query5(KeyCipher keyCipher, String p, List<String> C, String X) throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException {
        int m = p.length();

        String jsonStr = null;

        try {
            byte[] X_bytes = base64.decodeToByteArray(X);
            byte[] dec_X_bytes = keyCipher.getKdCipher().decrypt(X_bytes);
            jsonStr = new String(dec_X_bytes);
        } catch (BadPaddingException ex) {
            //sometime when different keys are used for encryption/decryption
            //it throws a BadPaddingException
            //canDec = false;
        }

        X x = new Gson().fromJson(jsonStr, X.class);

        StringBuilder pOutput = new StringBuilder();

        for (int i = 0; i < m; i++) {
            String c = C.get(i);
            byte[] c_bytes = base64.decodeToByteArray(c);

            boolean canDec = true;

            byte[] y_bytes = null;

            try {
                y_bytes = keyCipher.getKcCipher().decrypt(c_bytes);
            } catch (BadPaddingException ex) {
                //sometime when different keys are used for encryption/decryption
                //it throws a BadPaddingException
                canDec = false;
            }

            if (!canDec) {
                return null;
            } else {
                String data = new String(y_bytes);
                String[] dataArr = data.split(SEP);
                int j = Integer.valueOf(dataArr[1]);
                if (j != (x.getInd() + i)) {//need to minus 1 because the original scheme index begin with 1
                    return null;
                } else {
                    pOutput.append(dataArr[0]);
                }
            }
        }

        List<String> result = new ArrayList<>();

        if (!pOutput.toString().equals(p)) {
            return result;
        } else {
            for (int i = 0; i < x.getNum(); i++) {
                int val = x.getLeafpos() + i - 1;
                byte[] val_bytes = keyCipher.getKeyedHashFunc().getHash(this.getK4(), BigInteger.valueOf(val).toByteArray());
                result.add(base64.encodeToString(val_bytes));
            }
            return result;
        }
    }

    public List<String> Query7(List<String> L, KeyCipher keyCipher) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        int num = L.size();
        IByteCipher byteCipher = keyCipher.getKlCipher();

        List<String> output = new ArrayList<>();

        boolean decryptFailed = false;
        for (int i = 0; i < num; i++) {
            String l_base64 = L.get(i);
            byte[] l_bytes = base64.decodeToByteArray(l_base64);
            byte[] dec_bytes = null;
            try {
                dec_bytes = byteCipher.decrypt(l_bytes);
            } catch (BadPaddingException ex) {
                decryptFailed = true;
                break;
            }
            String tmp = new String(dec_bytes);
            output.add(tmp.substring(0, tmp.indexOf(SEP)));
        }

        if (decryptFailed) {
            return null;
        } else {
            return output;
        }
    }
}
