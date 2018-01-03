package org.coreocto.dev.hf.clientlib.sse.vasst;

import org.coreocto.dev.hf.clientlib.parser.TxtFileParserImpl;
import org.coreocto.dev.hf.commonlib.crypto.BlockCipherFactory;
import org.coreocto.dev.hf.commonlib.crypto.IBlockCipherCbc;
import org.coreocto.dev.hf.commonlib.crypto.IByteCipher;
import org.coreocto.dev.hf.commonlib.crypto.IFileCipher;
import org.coreocto.dev.hf.commonlib.util.IBase64;
import org.coreocto.dev.hf.commonlib.util.ILogger;
import org.coreocto.dev.hf.commonlib.util.Registry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class VasstClientTest {

    private VasstClient vasstClient = null;
    private IByteCipher byteCipher = null;
    private IFileCipher fileCipher = null;

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

        byteCipher = new IByteCipher() {

            private static final String CIPHER_AES = "AES";
            private static final String CIPHER_TRANSFORM = "AES/CBC/PKCS5Padding";

            private byte[] key = new byte[16];
            private byte[] iv = new byte[16];

            @Override
            public byte[] encrypt(byte[] data) throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
                Cipher encryptCipher = BlockCipherFactory.getCipher(CIPHER_AES, CIPHER_TRANSFORM, Cipher.ENCRYPT_MODE, key, iv);
                return encryptCipher.doFinal(data);
            }

            @Override
            public byte[] decrypt(byte[] data) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {
                Cipher decryptCipher = BlockCipherFactory.getCipher(CIPHER_AES, CIPHER_TRANSFORM, Cipher.DECRYPT_MODE, key, iv);
                return decryptCipher.doFinal(data);
            }
        };

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
        String docId = "someDocId";
        byte x = (byte) (Math.random() * (128));
        vasstClient.GenKey(16);
        vasstClient.Preprocessing(new File("C:\\Users\\John\\Desktop\\20_newsgroups\\alt.atheism\\53519"), x, new TxtFileParserImpl(), byteCipher);
    }
}
