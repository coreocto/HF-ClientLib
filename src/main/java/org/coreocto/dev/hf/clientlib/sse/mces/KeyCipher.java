package org.coreocto.dev.hf.clientlib.sse.mces;

import org.coreocto.dev.hf.commonlib.crypto.IByteCipher;
import org.coreocto.dev.hf.commonlib.crypto.IKeyedHashFunc;

public class KeyCipher {
    private IByteCipher kdCipher;
    private IByteCipher kcCipher;
    private IByteCipher klCipher;
    private IByteCipher k1Cipher;
    private IByteCipher k2Cipher;
    private IKeyedHashFunc keyedHashFunc;

    public IKeyedHashFunc getKeyedHashFunc() {
        return keyedHashFunc;
    }

    public void setKeyedHashFunc(IKeyedHashFunc keyedHashFunc) {
        this.keyedHashFunc = keyedHashFunc;
    }

    public IByteCipher getKdCipher() {
        return kdCipher;
    }

    public void setKdCipher(IByteCipher kdCipher) {
        this.kdCipher = kdCipher;
    }

    public IByteCipher getKcCipher() {
        return kcCipher;
    }

    public void setKcCipher(IByteCipher kcCipher) {
        this.kcCipher = kcCipher;
    }

    public IByteCipher getKlCipher() {
        return klCipher;
    }

    public void setKlCipher(IByteCipher klCipher) {
        this.klCipher = klCipher;
    }

    public IByteCipher getK1Cipher() {
        return k1Cipher;
    }

    public void setK1Cipher(IByteCipher k1Cipher) {
        this.k1Cipher = k1Cipher;
    }

    public IByteCipher getK2Cipher() {
        return k2Cipher;
    }

    public void setK2Cipher(IByteCipher k2Cipher) {
        this.k2Cipher = k2Cipher;
    }

}
