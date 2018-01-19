package org.coreocto.dev.hf.clientlib.sse.mces;

import org.coreocto.dev.hf.clientlib.LibConstants;
import org.coreocto.dev.hf.commonlib.crypto.IByteCipher;
import org.coreocto.dev.hf.commonlib.util.IBase64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

public class McesClient {

    private byte[] k1;
    private byte[] k2;
    private byte[] k3;
    private byte[] k4;
    private byte[] kd;
    private byte[] kc;
    private byte[] kl;

    private IBase64 base64;

    public void Gen(int noOfBytes) {
        k1 = new byte[16];
        k2 = new byte[16];
        k3 = new byte[16];
        k4 = new byte[16];
        kd = new byte[16];
        kc = new byte[16];
        kl = new byte[16];
        Random random = new SecureRandom();
        random.nextBytes(k1);
        random.nextBytes(k2);
        random.nextBytes(k3);
        random.nextBytes(k4);
        random.nextBytes(kd);
        random.nextBytes(kc);
        random.nextBytes(kl);
    }

    public byte[] Enc(String message, IByteCipher k1Cipher, IByteCipher k2Cipher, IByteCipher kcCipher, IByteCipher klCipher) throws UnsupportedEncodingException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidAlgorithmParameterException {

        CT finalResult = new CT();

        SuffixTree suffixTree = new SuffixTree(message);
        suffixTree.assignLeafIdDfs();

        {
            SuffixTree.Node rootNode = suffixTree.getRoot();
            Stack<SuffixTree.Node> tmpStack = new Stack<>();
            tmpStack.add(rootNode);

            while (tmpStack.isEmpty() == false) {
                SuffixTree.Node x = tmpStack.pop();
                for (SuffixTree.Node n : x.values()) {
                    tmpStack.add(n);
                    System.out.println(suffixTree.getInitPath(n));
                }
                //do things here
            }
        }

        Map<String, String> dictionary = new HashMap<>();
//        k1Cipher.encrypt()

        int messageLen = message.length();

        List<SuffixTree.Node> allNodes = suffixTree.getAllNodes(false);
        int totalNodeCnt = allNodes.size();

        List<String> g2 = new ArrayList<>();

        final int secParam = 16;

        for (int z = 0; z < totalNodeCnt; z++) {

            SuffixTree.Node curNode = allNodes.get(z);

            List<SuffixTree.Node> child = new ArrayList<>(curNode.values());

            int childSize = child.size();

            for (int i = 0; i < childSize; i++) {
                byte[] iniPath = suffixTree.getInitPath(child.get(i)).getBytes(LibConstants.ENCODING_UTF8);
                String encIniPath = base64.encodeToString(k2Cipher.encrypt(iniPath));
                g2.add(encIniPath);
            }

            byte[] randomBytes = new byte[secParam];
            Random random = new Random();


            for (int i = childSize; i < messageLen; i++) {
                random.nextBytes(randomBytes);
                String random_in_b64 = base64.encodeToString(randomBytes);
                g2.add(random_in_b64);
            }
        }
        for (SuffixTree.Node v : allNodes) {

        }

        // in original proposal, both C & L are arrays which use PRP to shuffle the content
        // i cannot find one right now, so i used the original index at the moment
        List<String> C = finalResult.getC();
        for (int i = 0; i < messageLen; i++) {
            String encChar = base64.encodeToString(kcCipher.encrypt(message.substring(i, i + 1).getBytes(LibConstants.ENCODING_UTF8)));
            C.add(encChar);
        }

        List<String> L = finalResult.getL();
        List<SuffixTree.Node> leaves = suffixTree.getAllNodes(true);
        int leafCnt = leaves.size();
        for (int i = 0; i < leafCnt; i++) {
            byte[] data = ByteBuffer.allocate(4).putInt(leaves.get(i).getId()).array();
            String encStr = base64.encodeToString(klCipher.encrypt(data));
            L.add(encStr);
        }

        return new byte[0];
    }

//    public v

}
