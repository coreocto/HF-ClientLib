package org.coreocto.dev.hf.clientlib.vasst;

import org.coreocto.dev.hf.commonlib.crypto.IBlockCipherCbc;
import org.coreocto.dev.hf.commonlib.util.IBase64;
import org.coreocto.dev.hf.commonlib.util.ILogger;
import org.coreocto.dev.hf.commonlib.util.Registry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;

public class VasstClientTest {

    private VasstClient vasstClient = null;

    @Before
    public void setUp() throws Exception {
        Registry registry = new Registry();
        registry.setLogger(new ILogger() {
            @Override
            public void log(String s, String s1) {
                System.out.println(s + ": " + s1);
            }
        });
        registry.setBase64(new IBase64() {
            @Override
            public String encodeToString(byte[] bytes) {
                return new BASE64Encoder().encode(bytes);
            }

            @Override
            public byte[] decodeToByteArray(String s) {
                byte[] data = null;
                try {
                    data = new BASE64Decoder().decodeBuffer(s);
                } catch (IOException e) {

                }
                return data;
            }
        });
        registry.setBlockCipherCbc(new IBlockCipherCbc() {
            @Override
            public byte[] encrypt(byte[] bytes, byte[] bytes1, byte[] bytes2) {
                byte[] encrypted = null;
                try {
                    IvParameterSpec iv = new IvParameterSpec(bytes);
                    SecretKeySpec skeySpec = new SecretKeySpec(bytes1, "AES");

                    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
                    cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

                    encrypted = cipher.doFinal(bytes2);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return encrypted;
            }

            @Override
            public byte[] decrypt(byte[] bytes, byte[] bytes1, byte[] bytes2) {
                byte[] decrypted = null;
                try {
                    IvParameterSpec iv = new IvParameterSpec(bytes);
                    SecretKeySpec skeySpec = new SecretKeySpec(bytes1, "AES");

                    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
                    cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

                    decrypted = cipher.doFinal(bytes2);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return decrypted;
            }
        });
        vasstClient = new VasstClient(registry);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() throws Exception {
        String docId = "someDocId";
        byte x = (byte) (Math.random() * (128));
        vasstClient.GenKey(16);
        vasstClient.Preprocessing(new File("C:\\Users\\John\\Desktop\\20_newsgroups\\alt.atheism\\53519"), x);
    }
}
