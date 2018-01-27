package org.coreocto.dev.hf.clientlib.crypto;

import org.coreocto.dev.hf.clientlib.LibConstants;
import org.coreocto.dev.hf.commonlib.crypto.IKeyedHashFunc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class HmacSha1 implements IKeyedHashFunc {

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    public byte[] getHash(String key, String s) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {
        return this.getHash(key.getBytes(LibConstants.ENCODING_UTF8), s.getBytes(LibConstants.ENCODING_UTF8));
    }

    public byte[] getHash(byte[] key, byte[] data) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec signingKey = new SecretKeySpec(key, HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        return mac.doFinal(data);
    }
}
