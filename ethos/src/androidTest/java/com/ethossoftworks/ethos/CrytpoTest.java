package com.ethossoftworks.ethos;

import android.support.test.runner.AndroidJUnit4;

import com.ethossoftworks.ethos.Util.Crypto;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class CrytpoTest {
    @Test
    public void encryptDecrypt() throws Exception {
        String encrypted = Crypto.encrypt("this is a test", "testkey");
        String decrypted = Crypto.decrypt(encrypted, "testkey");
        assertEquals("this is a test", decrypted);
    }


    @Test
    public void sha256() throws Exception {
        String hash = Crypto.sha256("wow this is awesome");
        assertEquals("7bd74c8bdb400e38411034f7b892188536d62c4deeaad5ba3fafb141bae3e48a", hash);
    }


    @Test
    public void sha512() throws Exception {
        String hash = Crypto.sha512("wow this is awesome");
        assertEquals("d55347ef944277167ee396c657ddf55195514787bd6f37b35c9294f7e5e06ca5f845df8dd88193f1b30a5764fae333982bf0290eeb50acea9a7058ed917ef6e1", hash);
    }


    @Test
    public void hmac256() throws Exception {
        String hash = Crypto.hmac256("wow this is awesome", "this is my key");
        assertEquals("3a6866c6b3d1f15928bda86ec1cb0ed89d900991bfa07a9338f4d26fc973a1ec", hash);
    }


    @Test
    public void hmac512() throws Exception {
        String hash = Crypto.hmac512("wow this is awesome", "this is my key");
        assertEquals("66183e239a7d69ca11d030e68fa4ab2d1fc29c5f1498aef7b1181577df0f2a6d25dbfe3ca1ea51941da363153afe456be4ae99bbf0f0d2ff8e81e1dfc44d5677", hash);
    }
}