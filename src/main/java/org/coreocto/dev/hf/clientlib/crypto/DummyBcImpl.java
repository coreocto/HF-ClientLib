package org.coreocto.dev.hf.clientlib.crypto;

import org.coreocto.dev.hf.commonlib.crypto.IByteCipher;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class DummyBcImpl implements IByteCipher {

    private static DummyBcImpl instance;

    private DummyBcImpl() {
    }

    public static DummyBcImpl getInstance() {
        if (instance == null) {
            instance = new DummyBcImpl();
        }
        return instance;
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
    public byte[] encrypt(byte[] bytes, byte[] keyBytes, byte[] ivBytes) throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        return bytes;
    }

    @Override
    public byte[] decrypt(byte[] bytes, byte[] keyBytes, byte[] ivBytes) throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        return bytes;
    }

    @Override
    public byte[] encrypt(byte[] bytes, byte[] keyBytes) throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        return bytes;
    }

    @Override
    public byte[] decrypt(byte[] bytes, byte[] keyBytes) throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        return bytes;
    }
}
