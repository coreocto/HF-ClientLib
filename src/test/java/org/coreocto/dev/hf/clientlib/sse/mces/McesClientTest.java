package org.coreocto.dev.hf.clientlib.sse.mces;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import org.coreocto.dev.hf.clientlib.crypto.AesCbcPkcs5BcImpl;
import org.coreocto.dev.hf.commonlib.crypto.IByteCipher;
import org.coreocto.dev.hf.commonlib.util.IBase64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class McesClientTest {
    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() throws Exception {
//        GeneralizedSuffixTree suffixTree = new GeneralizedSuffixTree();
//        String word = "cocoon";
//        suffixTree.put(word, 0);
//        Collection<Integer> srhResult = suffixTree.search("coc");
//        System.out.println(srhResult);
//        System.out.println(suffixTree);

        byte[] iv = new byte[16];
        byte[] key = new byte[16];

        IBase64 base64 = new IBase64() {
            @Override
            public String encodeToString(byte[] bytes) {
                return Base64.encode(bytes);
            }

            @Override
            public byte[] decodeToByteArray(String s) {
                byte[] data = null;

                try {
                    data = Base64.decode(s);
                } catch (Base64DecodingException e) {
                    e.printStackTrace();
                }
                return data;
            }
        };

        McesClient client = new McesClient(base64); //client with K and p
        client.GenZero(16);

        IByteCipher k1ByteCipher = new AesCbcPkcs5BcImpl(client.getK1(), iv);
        IByteCipher k2ByteCipher = new AesCbcPkcs5BcImpl(client.getK2(), iv);
        IByteCipher k3ByteCipher = new AesCbcPkcs5BcImpl(client.getK3(), iv);
        IByteCipher k4ByteCipher = new AesCbcPkcs5BcImpl(client.getK4(), iv);
        IByteCipher kdByteCipher = new AesCbcPkcs5BcImpl(client.getKd(), iv);
        IByteCipher kcByteCipher = new AesCbcPkcs5BcImpl(client.getKc(), iv);
        IByteCipher klByteCipher = new AesCbcPkcs5BcImpl(client.getKl(), iv);

        {
            k1ByteCipher = new AesCbcPkcs5BcImpl(key, iv);
            k2ByteCipher = new AesCbcPkcs5BcImpl(key, iv);
            k3ByteCipher = new AesCbcPkcs5BcImpl(key, iv);
            k4ByteCipher = new AesCbcPkcs5BcImpl(key, iv);
            kdByteCipher = new AesCbcPkcs5BcImpl(key, iv);
            kcByteCipher = new AesCbcPkcs5BcImpl(key, iv);
            klByteCipher = new AesCbcPkcs5BcImpl(key, iv);
        }

        KeyCipher keyCipher = new KeyCipher();
        keyCipher.setK1Cipher(k1ByteCipher);
        keyCipher.setK2Cipher(k2ByteCipher);
        keyCipher.setK3Cipher(k3ByteCipher);
        keyCipher.setK4Cipher(k4ByteCipher);
        keyCipher.setKdCipher(kdByteCipher);
        keyCipher.setKcCipher(kcByteCipher);
        keyCipher.setKlCipher(klByteCipher);

        String word = "cocoon";

        System.out.println("------------------------------");

        CT ct = client.Enc(word, keyCipher);

        System.out.println("------------------------------");

        McesServer server = new McesServer(base64); //server with CT
        server.receive(ct);

        String testWord = "co";

        List<String> query1_output = client.Query1(keyCipher, testWord);    //step1

        System.out.println("------------------------------");

        String W = server.Query2(query1_output);  //step2

        System.out.println("W = "+W);

        System.out.println("------------------------------");

        List<Integer> x = client.Query3(keyCipher, testWord, W, query1_output);

        System.out.println("x = "+x);

        System.out.println("------------------------------");

        List<String> C = server.Query4(x);

        System.out.println("C = "+C);

        System.out.println("------------------------------");

        List<String> L = client.Query5(keyCipher, testWord, C, W);

        server.

        System.out.println("------------------------------");
//
//        List<Integer> decW = client.Query3(keyCipher, word, W, query1_output); //step3.1
//        System.out.println(decW);
    }
}
