package org.coreocto.dev.hf.clientlib.crypto;

import org.coreocto.dev.hf.commonlib.crypto.IByteCipher;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class DummyBcImpl implements IByteCipher {

    private DummyBcImpl() {
    }

    @Override
    public byte[] encrypt(byte[] bytes) throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        return bytes;
    }

    @Override
    public byte[] decrypt(byte[] bytes) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {
        return bytes;
    }

    @Override
    public byte[] encrypt(byte[] bytes, byte[] bytes1, byte[] bytes2) throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        return bytes;
    }

    @Override
    public byte[] decrypt(byte[] bytes, byte[] bytes1, byte[] bytes2) throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        return bytes;
    }

    @Override
    public byte[] encrypt(byte[] bytes, byte[] bytes1) throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        return bytes;
    }

    @Override
    public byte[] decrypt(byte[] bytes, byte[] bytes1) throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        return bytes;
    }

    private static DummyBcImpl instance;

    public static DummyBcImpl getInstance() {
        if (instance == null) {
            instance = new DummyBcImpl();
        }
        return instance;
    }
}
