package org.coreocto.dev.hf.clientlib.sse.mces;

import org.coreocto.dev.hf.commonlib.crypto.IByteCipher;

public class KeyCipher {
    private IByteCipher kdCipher;
    private IByteCipher kcCipher;
    private IByteCipher klCipher;
    private IByteCipher k1Cipher;
    private IByteCipher k2Cipher;
    private IByteCipher k3Cipher;
    private IByteCipher k4Cipher;

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

    public IByteCipher getK3Cipher() {
        return k3Cipher;
    }

    public void setK3Cipher(IByteCipher k3Cipher) {
        this.k3Cipher = k3Cipher;
    }

    public IByteCipher getK4Cipher() {
        return k4Cipher;
    }

    public void setK4Cipher(IByteCipher k4Cipher) {
        this.k4Cipher = k4Cipher;
    }

}
