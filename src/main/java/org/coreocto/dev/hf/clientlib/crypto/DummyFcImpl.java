package org.coreocto.dev.hf.clientlib.crypto;

import org.coreocto.dev.hf.commonlib.crypto.IFileCipher;
import org.coreocto.dev.hf.commonlib.util.Util;

import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class DummyFcImpl implements IFileCipher {

    private DummyFcImpl() {
    }

    @Override
    public void encrypt(File in, File out) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IOException {
        this.encrypt(new FileInputStream(in), new FileOutputStream(out));
    }

    @Override
    public void encrypt(InputStream inputStream, OutputStream outputStream) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IOException {
        Util.copy(inputStream, outputStream);
    }

    @Override
    public void decrypt(File in, File out) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        this.decrypt(new FileInputStream(in), new FileOutputStream(out));
    }

    @Override
    public void decrypt(InputStream inputStream, OutputStream outputStream) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IOException {
        Util.copy(inputStream, outputStream);
    }

    private static DummyFcImpl instance;

    public static DummyFcImpl getInstance() {
        if (instance == null) {
            instance = new DummyFcImpl();
        }
        return instance;
    }
}
