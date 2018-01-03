package org.coreocto.dev.hf.clientlib.sse.suise;

import org.coreocto.dev.hf.commonlib.crypto.BlockCipherFactory;
import org.coreocto.dev.hf.commonlib.crypto.IBlockCipherCbc;
import org.coreocto.dev.hf.commonlib.crypto.IFileCipher;
import org.coreocto.dev.hf.commonlib.crypto.IHashFunc;
import org.coreocto.dev.hf.commonlib.sse.suise.util.SuiseUtil;
import org.coreocto.dev.hf.commonlib.util.ILogger;
import org.coreocto.dev.hf.commonlib.util.Registry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SuiseClientTest {

    private SuiseClient suiseClient = null;
    private IFileCipher fileCipher = null;

    @Before
    public void setUp() throws Exception {
        Registry registry = new Registry();
        registry.setBlockCipherCbc(new IBlockCipherCbc() {
            @Override
            public byte[] encrypt(byte[] bytes, byte[] bytes1, byte[] bytes2) {
                return new byte[0];
            }

            @Override
            public byte[] decrypt(byte[] bytes, byte[] bytes1, byte[] bytes2) {
                return new byte[0];
            }
        });
        registry.setHashFunc(new IHashFunc() {
            private MessageDigest md = null;

            {
                try {
                    md = MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public byte[] getHash(String s) {
                return getHash(s.getBytes());
            }

            @Override
            public byte[] getHash(byte[] bytes) {
                return md.digest(bytes);
            }
        });
        registry.setLogger(new ILogger() {
            @Override
            public void log(String s, String s1) {
                System.out.println(s + ":" + s1);
            }
        });
        SuiseUtil util = new SuiseUtil(registry);
        suiseClient = new SuiseClient(registry, util);

        fileCipher = new IFileCipher() {

            private static final String CIPHER_AES = "AES";
            private static final String CIPHER_TRANSFORM = "AES/CBC/PKCS5Padding";

            private byte[] key = new byte[16];
            private byte[] iv = new byte[16];

            @Override
            public void encrypt(File in, File out) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IOException {
                this.encrypt(new FileInputStream(in), new FileOutputStream(out));
            }

            @Override
            public void encrypt(InputStream in, OutputStream out) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IOException {
                Cipher encryptCipher = BlockCipherFactory.getCipher(CIPHER_AES, CIPHER_TRANSFORM, Cipher.ENCRYPT_MODE, key, iv);

                BufferedInputStream is = new BufferedInputStream(in);
                BufferedOutputStream os = new BufferedOutputStream(new CipherOutputStream(out, encryptCipher));
                int buffer = -1;
                while ((buffer = is.read()) != -1) {
                    os.write(buffer);
                }
                os.flush();

                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            }

            @Override
            public void decrypt(File in, File out) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
                this.decrypt(new FileInputStream(in), new FileOutputStream(out));
            }

            @Override
            public void decrypt(InputStream in, OutputStream out) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IOException {
                Cipher decryptCipher = BlockCipherFactory.getCipher(CIPHER_AES, CIPHER_TRANSFORM, Cipher.DECRYPT_MODE, key, iv);

                BufferedInputStream is = new BufferedInputStream(new CipherInputStream(in, decryptCipher));
                BufferedOutputStream os = new BufferedOutputStream(out);
                int buffer = -1;
                while ((buffer = is.read()) != -1) {
                    os.write(buffer);
                }
                os.flush();

                if (is != null) {
                    is.close();
                }

                if (os != null) {
                    os.close();
                }
            }
        };
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() throws Exception {
        suiseClient.Gen(16);
        suiseClient.Enc(new File("C:\\Users\\John\\Desktop\\171_PT_subj_reg_arrangements.pdf"), new File("C:\\Users\\John\\Desktop\\171_PT_subj_reg_arrangements-enc.pdf"), fileCipher);
        suiseClient.Dec(new File("C:\\Users\\John\\Desktop\\171_PT_subj_reg_arrangements-enc.pdf"), new File("C:\\Users\\John\\Desktop\\171_PT_subj_reg_arrangements-dec.pdf"), fileCipher);
    }

}