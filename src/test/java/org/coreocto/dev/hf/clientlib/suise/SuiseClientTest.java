package org.coreocto.dev.hf.clientlib.suise;

import org.coreocto.dev.hf.commonlib.crypto.IBlockCipherCbc;
import org.coreocto.dev.hf.commonlib.crypto.IHashFunc;
import org.coreocto.dev.hf.commonlib.suise.util.SuiseUtil;
import org.coreocto.dev.hf.commonlib.util.ILogger;
import org.coreocto.dev.hf.commonlib.util.Registry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SuiseClientTest {

    private SuiseClient suiseClient = null;

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
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() throws Exception {
        suiseClient.Gen(16);
        suiseClient.Enc(new File("/Users/john/Desktop/se.pdf"), new File("/Users/john/Desktop/se-enc.pdf"));
        suiseClient.Dec(new File("/Users/john/Desktop/se-enc.pdf"), new File("/Users/john/Desktop/se-dec.pdf"));
    }

}