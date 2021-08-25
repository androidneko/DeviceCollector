package com.androidcat.catlibs.persistance.securepref;


import android.os.Build;
import android.os.Process;
import android.util.Base64;
import android.util.Log;

import com.androidcat.catlibs.utils.CommonMethods;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.SecureRandomSpi;
import java.security.Security;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/**
 * The Class AesCbcWithIntegrity.
 */
public class AesCbcWithIntegrity {

    /**
     * The Constant BASE64_FLAGS.
     */
    public static final int BASE64_FLAGS = Base64.NO_WRAP;
    /**
     * The Constant prngFixed.
     */
    //default for testing
    static final AtomicBoolean prngFixed = new AtomicBoolean(false);
    /**
     * The Constant ALLOW_BROKEN_PRNG.
     */
    private static final boolean ALLOW_BROKEN_PRNG = false;
    /**
     * The Constant CIPHER_TRANSFORMATION.
     */
    private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    /**
     * The Constant CIPHER.
     */
    private static final String CIPHER = "AES";
    /**
     * The Constant RANDOM_ALGORITHM.
     */
    private static final String RANDOM_ALGORITHM = "SHA1PRNG";
    /**
     * The Constant AES_KEY_LENGTH_BITS.
     */
    private static final int AES_KEY_LENGTH_BITS = 128;
    /**
     * The Constant IV_LENGTH_BYTES.
     */
    private static final int IV_LENGTH_BYTES = 16;
    /**
     * The Constant PBE_ITERATION_COUNT.
     */
    private static final int PBE_ITERATION_COUNT = 10000;
    /**
     * The Constant PBE_SALT_LENGTH_BITS.
     */
    private static final int PBE_SALT_LENGTH_BITS = AES_KEY_LENGTH_BITS; // same size as key output
    /**
     * The Constant PBE_ALGORITHM.
     */
    private static final String PBE_ALGORITHM = "PBKDF2WithHmacSHA1";
    /**
     * The Constant HMAC_ALGORITHM.
     */
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /**
     * The Constant HMAC_KEY_LENGTH_BITS.
     */
    private static final int HMAC_KEY_LENGTH_BITS = 256;


    /**
     * Key string.
     *
     * @param keys the keys
     * @return the string
     */
    public static String keyString(SecretKeys keys) {
        return keys.toString();
    }


    /**
     * Keys.
     *
     * @param keysStr the keys str
     * @return the secret keys
     * @throws InvalidKeyException the invalid key exception
     */
    public static SecretKeys keys(String keysStr) throws InvalidKeyException {
        String[] keysArr = keysStr.split(":");

        if (keysArr.length != 2) {
            throw new IllegalArgumentException("Cannot parse aesKey:hmacKey");

        } else {
            byte[] confidentialityKey = Base64.decode(keysArr[0], BASE64_FLAGS);
            if (confidentialityKey.length != AES_KEY_LENGTH_BITS / 8) {
                throw new InvalidKeyException("Base64 decoded key is not " + AES_KEY_LENGTH_BITS + " bytes");
            }
            byte[] integrityKey = Base64.decode(keysArr[1], BASE64_FLAGS);
            if (integrityKey.length != HMAC_KEY_LENGTH_BITS / 8) {
                throw new InvalidKeyException("Base64 decoded key is not " + HMAC_KEY_LENGTH_BITS + " bytes");
            }

            return new SecretKeys(
                    new SecretKeySpec(confidentialityKey, 0, confidentialityKey.length, CIPHER),
                    new SecretKeySpec(integrityKey, HMAC_ALGORITHM));
        }
    }


    /**
     * Generate key.
     *
     * @return the secret keys
     * @throws GeneralSecurityException the general security exception
     */
    public static SecretKeys generateKey() throws GeneralSecurityException {
        fixPrng();
        KeyGenerator keyGen = KeyGenerator.getInstance(CIPHER);

        keyGen.init(AES_KEY_LENGTH_BITS);
        SecretKey confidentialityKey = keyGen.generateKey();


        byte[] integrityKeyBytes = randomBytes(HMAC_KEY_LENGTH_BITS / 8);//to get bytes
        SecretKey integrityKey = new SecretKeySpec(integrityKeyBytes, HMAC_ALGORITHM);

        return new SecretKeys(confidentialityKey, integrityKey);
    }


    /**
     * Generate key from password.
     *
     * @param password the password
     * @param salt     the salt
     * @return the secret keys
     * @throws GeneralSecurityException the general security exception
     */
    public static SecretKeys generateKeyFromPassword(String password, byte[] salt) throws GeneralSecurityException {

        /*fixPrng();

        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt,
                PBE_ITERATION_COUNT, AES_KEY_LENGTH_BITS + HMAC_KEY_LENGTH_BITS);

        SecretKeyFactory keyFactory = SecretKeyFactory
                .getInstance(PBE_ALGORITHM);

        byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();*/

        byte[] keyBytes = CommonMethods.str2bytes(password);

        byte[] confidentialityKeyBytes = copyOfRange(keyBytes, 0, AES_KEY_LENGTH_BITS / 8);
        byte[] integrityKeyBytes = copyOfRange(keyBytes, AES_KEY_LENGTH_BITS / 8, AES_KEY_LENGTH_BITS / 8 + HMAC_KEY_LENGTH_BITS / 8);


        SecretKey confidentialityKey = new SecretKeySpec(confidentialityKeyBytes, CIPHER);

        SecretKey integrityKey = new SecretKeySpec(integrityKeyBytes, HMAC_ALGORITHM);

        return new SecretKeys(confidentialityKey, integrityKey);
    }

    /**
     * Generate key from password.
     *
     * @param password the password
     * @return the secret keys
     * @throws GeneralSecurityException the general security exception
     */
    public static SecretKeys generateKeyFromPassword(byte[] password) throws GeneralSecurityException {
        if (password == null || password.length < 48){
            throw new GeneralSecurityException("illegal password");
        }
        byte[] confidentialityKeyBytes = copyOfRange(password, 0, AES_KEY_LENGTH_BITS / 8);
        byte[] integrityKeyBytes = copyOfRange(password, AES_KEY_LENGTH_BITS / 8, AES_KEY_LENGTH_BITS / 8 + HMAC_KEY_LENGTH_BITS / 8);


        SecretKey confidentialityKey = new SecretKeySpec(confidentialityKeyBytes, CIPHER);

        SecretKey integrityKey = new SecretKeySpec(integrityKeyBytes, HMAC_ALGORITHM);

        return new SecretKeys(confidentialityKey, integrityKey);
    }


    /**
     * Generate key from password.
     *
     * @param password the password
     * @param salt     the salt
     * @return the secret keys
     * @throws GeneralSecurityException the general security exception
     */
    public static SecretKeys generateKeyFromPassword(String password, String salt) throws GeneralSecurityException {
        return generateKeyFromPassword(password, Base64.decode(salt, BASE64_FLAGS));
    }


    /**
     * Generate salt.
     *
     * @return the byte[]
     * @throws GeneralSecurityException the general security exception
     */
    public static byte[] generateSalt() throws GeneralSecurityException {
        return randomBytes(PBE_SALT_LENGTH_BITS);
    }


    /**
     * Salt string.
     *
     * @param salt the salt
     * @return the string
     */
    public static String saltString(byte[] salt) {
        return Base64.encodeToString(salt, BASE64_FLAGS);
    }


    /**
     * Generate iv.
     *
     * @return the byte[]
     * @throws GeneralSecurityException the general security exception
     */
    public static byte[] generateIv() throws GeneralSecurityException {
        return randomBytes(IV_LENGTH_BYTES);
    }

    /**
     * Random bytes.
     *
     * @param length the length
     * @return the byte[]
     * @throws GeneralSecurityException the general security exception
     */
    private static byte[] randomBytes(int length) throws GeneralSecurityException {
        fixPrng();
        SecureRandom random = SecureRandom.getInstance(RANDOM_ALGORITHM);
        byte[] b = new byte[length];
        random.nextBytes(b);
        return b;
    }


    /**
     * Encrypt.
     *
     * @param plaintext  the plaintext
     * @param secretKeys the secret keys
     * @return the cipher text iv mac
     * @throws UnsupportedEncodingException the unsupported encoding exception
     * @throws GeneralSecurityException     the general security exception
     */
    public static CipherTextIvMac encrypt(String plaintext, SecretKeys secretKeys)
            throws UnsupportedEncodingException, GeneralSecurityException {
        return encrypt(plaintext, secretKeys, "UTF-8");
    }


    /**
     * Encrypt.
     *
     * @param plaintext  the plaintext
     * @param secretKeys the secret keys
     * @param encoding   the encoding
     * @return the cipher text iv mac
     * @throws UnsupportedEncodingException the unsupported encoding exception
     * @throws GeneralSecurityException     the general security exception
     */
    public static CipherTextIvMac encrypt(String plaintext, SecretKeys secretKeys, String encoding)
            throws UnsupportedEncodingException, GeneralSecurityException {
        return encrypt(plaintext.getBytes(encoding), secretKeys);
    }


    /**
     * Encrypt.
     *
     * @param plaintext  the plaintext
     * @param secretKeys the secret keys
     * @return the cipher text iv mac
     * @throws GeneralSecurityException the general security exception
     */
    public static CipherTextIvMac encrypt(byte[] plaintext, SecretKeys secretKeys)
            throws GeneralSecurityException {
        byte[] iv = generateIv();
        Cipher aesCipherForEncryption = Cipher.getInstance(CIPHER_TRANSFORMATION);
        aesCipherForEncryption.init(Cipher.ENCRYPT_MODE, secretKeys.getConfidentialityKey(), new IvParameterSpec(iv));


        iv = aesCipherForEncryption.getIV();
        byte[] byteCipherText = aesCipherForEncryption.doFinal(plaintext);
        byte[] ivCipherConcat = CipherTextIvMac.ivCipherConcat(iv, byteCipherText);

        byte[] integrityMac = generateMac(ivCipherConcat, secretKeys.getIntegrityKey());
        return new CipherTextIvMac(byteCipherText, iv, integrityMac);
    }


    /**
     * Fix prng.
     */
    private static void fixPrng() {
        if (!prngFixed.get()) {
            synchronized (PrngFixes.class) {
                if (!prngFixed.get()) {
                    PrngFixes.apply();
                    prngFixed.set(true);
                }
            }
        }
    }


    /**
     * Decrypt string.
     *
     * @param civ        the civ
     * @param secretKeys the secret keys
     * @param encoding   the encoding
     * @return the string
     * @throws UnsupportedEncodingException the unsupported encoding exception
     * @throws GeneralSecurityException     the general security exception
     */
    public static String decryptString(CipherTextIvMac civ, SecretKeys secretKeys, String encoding)
            throws UnsupportedEncodingException, GeneralSecurityException {
        return new String(decrypt(civ, secretKeys), encoding);
    }


    /**
     * Decrypt string.
     *
     * @param civ        the civ
     * @param secretKeys the secret keys
     * @return the string
     * @throws UnsupportedEncodingException the unsupported encoding exception
     * @throws GeneralSecurityException     the general security exception
     */
    public static String decryptString(CipherTextIvMac civ, SecretKeys secretKeys)
            throws UnsupportedEncodingException, GeneralSecurityException {
        return decryptString(civ, secretKeys, "UTF-8");
    }


    /**
     * Decrypt.
     *
     * @param civ        the civ
     * @param secretKeys the secret keys
     * @return the byte[]
     * @throws GeneralSecurityException the general security exception
     */
    public static byte[] decrypt(CipherTextIvMac civ, SecretKeys secretKeys)
            throws GeneralSecurityException {

        byte[] ivCipherConcat = CipherTextIvMac.ivCipherConcat(civ.getIv(), civ.getCipherText());
        byte[] computedMac = generateMac(ivCipherConcat, secretKeys.getIntegrityKey());
        if (constantTimeEq(computedMac, civ.getMac())) {
            Cipher aesCipherForDecryption = Cipher.getInstance(CIPHER_TRANSFORMATION);
            aesCipherForDecryption.init(Cipher.DECRYPT_MODE, secretKeys.getConfidentialityKey(),
                    new IvParameterSpec(civ.getIv()));
            return aesCipherForDecryption.doFinal(civ.getCipherText());
        } else {
            throw new GeneralSecurityException("MAC stored in civ does not match computed MAC.");
        }
    }


    /**
     * Generate mac.
     *
     * @param byteCipherText the byte cipher text
     * @param integrityKey   the integrity key
     * @return the byte[]
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws InvalidKeyException      the invalid key exception
     */
    public static byte[] generateMac(byte[] byteCipherText, SecretKey integrityKey) throws NoSuchAlgorithmException, InvalidKeyException {

        Mac sha256_HMAC = Mac.getInstance(HMAC_ALGORITHM);
        sha256_HMAC.init(integrityKey);
        return sha256_HMAC.doFinal(byteCipherText);
    }

    /**
     * Constant time eq.
     *
     * @param a the a
     * @param b the b
     * @return true, if successful
     */
    public static boolean constantTimeEq(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }

    /**
     * Copy of range.
     *
     * @param from  the from
     * @param start the start
     * @param end   the end
     * @return the byte[]
     */
    private static byte[] copyOfRange(byte[] from, int start, int end) {
        int length = end - start;
        byte[] result = new byte[length];
        System.arraycopy(from, start, result, 0, length);
        return result;
    }

    /**
     * The Class SecretKeys.
     */
    public static class SecretKeys {

        /**
         * The confidentiality key.
         */
        private SecretKey confidentialityKey;

        /**
         * The integrity key.
         */
        private SecretKey integrityKey;


        /**
         * Instantiates a new secret keys.
         *
         * @param confidentialityKeyIn the confidentiality key in
         * @param integrityKeyIn       the integrity key in
         */
        public SecretKeys(SecretKey confidentialityKeyIn, SecretKey integrityKeyIn) {
            setConfidentialityKey(confidentialityKeyIn);
            setIntegrityKey(integrityKeyIn);
        }

        /**
         * Gets the confidentiality key.
         *
         * @return the confidentiality key
         */
        public SecretKey getConfidentialityKey() {
            return confidentialityKey;
        }

        /**
         * Sets the confidentiality key.
         *
         * @param confidentialityKey the new confidentiality key
         */
        public void setConfidentialityKey(SecretKey confidentialityKey) {
            this.confidentialityKey = confidentialityKey;
        }

        /**
         * Gets the integrity key.
         *
         * @return the integrity key
         */
        public SecretKey getIntegrityKey() {
            return integrityKey;
        }

        /**
         * Sets the integrity key.
         *
         * @param integrityKey the new integrity key
         */
        public void setIntegrityKey(SecretKey integrityKey) {
            this.integrityKey = integrityKey;
        }


        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return Base64.encodeToString(getConfidentialityKey().getEncoded(), BASE64_FLAGS)
                    + ":" + Base64.encodeToString(getIntegrityKey().getEncoded(), BASE64_FLAGS);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + confidentialityKey.hashCode();
            result = prime * result + integrityKey.hashCode();
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SecretKeys other = (SecretKeys) obj;
            if (!integrityKey.equals(other.integrityKey))
                return false;
            if (!confidentialityKey.equals(other.confidentialityKey))
                return false;
            return true;
        }
    }

    /**
     * The Class CipherTextIvMac.
     */
    public static class CipherTextIvMac {

        /**
         * The cipher text.
         */
        private final byte[] cipherText;

        /**
         * The iv.
         */
        private final byte[] iv;

        /**
         * The mac.
         */
        private final byte[] mac;

        /**
         * Instantiates a new cipher text iv mac.
         *
         * @param c the c
         * @param i the i
         * @param h the h
         */
        public CipherTextIvMac(byte[] c, byte[] i, byte[] h) {
            cipherText = new byte[c.length];
            System.arraycopy(c, 0, cipherText, 0, c.length);
            iv = new byte[i.length];
            System.arraycopy(i, 0, iv, 0, i.length);
            mac = new byte[h.length];
            System.arraycopy(h, 0, mac, 0, h.length);
        }

        /**
         * Instantiates a new cipher text iv mac.
         *
         * @param base64IvAndCiphertext the base64 iv and ciphertext
         */
        public CipherTextIvMac(String base64IvAndCiphertext) {
            String[] civArray = base64IvAndCiphertext.split(":");
            if (civArray.length != 3) {
                throw new IllegalArgumentException("Cannot parse iv:ciphertext:mac");
            } else {
                iv = Base64.decode(civArray[0], BASE64_FLAGS);
                mac = Base64.decode(civArray[1], BASE64_FLAGS);
                cipherText = Base64.decode(civArray[2], BASE64_FLAGS);
            }
        }

        /**
         * Iv cipher concat.
         *
         * @param iv         the iv
         * @param cipherText the cipher text
         * @return the byte[]
         */
        public static byte[] ivCipherConcat(byte[] iv, byte[] cipherText) {
            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);
            return combined;
        }

        /**
         * Gets the cipher text.
         *
         * @return the cipher text
         */
        public byte[] getCipherText() {
            return cipherText;
        }

        /**
         * Gets the iv.
         *
         * @return the iv
         */
        public byte[] getIv() {
            return iv;
        }

        /**
         * Gets the mac.
         *
         * @return the mac
         */
        public byte[] getMac() {
            return mac;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            String ivString = Base64.encodeToString(iv, BASE64_FLAGS);
            String cipherTextString = Base64.encodeToString(cipherText, BASE64_FLAGS);
            String macString = Base64.encodeToString(mac, BASE64_FLAGS);
            return String.format(ivString + ":" + macString + ":" + cipherTextString);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(cipherText);
            result = prime * result + Arrays.hashCode(iv);
            result = prime * result + Arrays.hashCode(mac);
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CipherTextIvMac other = (CipherTextIvMac) obj;
            if (!Arrays.equals(cipherText, other.cipherText))
                return false;
            if (!Arrays.equals(iv, other.iv))
                return false;
            if (!Arrays.equals(mac, other.mac))
                return false;
            return true;
        }
    }

    /**
     * The Class PrngFixes.
     */
    public static final class PrngFixes {

        /**
         * The Constant VERSION_CODE_JELLY_BEAN.
         */
        private static final int VERSION_CODE_JELLY_BEAN = 16;

        /**
         * The Constant VERSION_CODE_JELLY_BEAN_MR2.
         */
        private static final int VERSION_CODE_JELLY_BEAN_MR2 = 18;

        /**
         * The Constant BUILD_FINGERPRINT_AND_DEVICE_SERIAL.
         */
        private static final byte[] BUILD_FINGERPRINT_AND_DEVICE_SERIAL = getBuildFingerprintAndDeviceSerial();


        /**
         * Instantiates a new prng fixes.
         */
        private PrngFixes() {
        }


        /**
         * Apply.
         */
        public static void apply() {
            applyOpenSSLFix();
            installLinuxPRNGSecureRandom();
        }


        /**
         * Apply open ssl fix.
         *
         * @throws SecurityException the security exception
         */
        private static void applyOpenSSLFix() throws SecurityException {
            if ((Build.VERSION.SDK_INT < VERSION_CODE_JELLY_BEAN)
                    || (Build.VERSION.SDK_INT > VERSION_CODE_JELLY_BEAN_MR2)) {

                return;
            }

            try {

                Class.forName("org.apache.harmony.xnet.provider.jsse.NativeCrypto")
                        .getMethod("RAND_seed", byte[].class).invoke(null, generateSeed());


                int bytesRead = (Integer) Class
                        .forName("org.apache.harmony.xnet.provider.jsse.NativeCrypto")
                        .getMethod("RAND_load_file", String.class, long.class)
                        .invoke(null, "/dev/urandom", 1024);
                if (bytesRead != 1024) {
                    throw new IOException("Unexpected number of bytes read from Linux PRNG: "
                            + bytesRead);
                }
            } catch (Exception e) {
                if (ALLOW_BROKEN_PRNG) {
                    Log.w(PrngFixes.class.getSimpleName(), "Failed to seed OpenSSL PRNG", e);
                } else {
                    throw new SecurityException("Failed to seed OpenSSL PRNG", e);
                }
            }
        }


        /**
         * Install linux prng secure random.
         *
         * @throws SecurityException the security exception
         */
        private static void installLinuxPRNGSecureRandom() throws SecurityException {
            if (Build.VERSION.SDK_INT > VERSION_CODE_JELLY_BEAN_MR2) {
                // No need to apply the fix
                return;
            }


            Provider[] secureRandomProviders = Security.getProviders("SecureRandom.SHA1PRNG");


            synchronized (Security.class) {
                if ((secureRandomProviders == null)
                        || (secureRandomProviders.length < 1)
                        || (!secureRandomProviders[0].getClass().getSimpleName().equals("LinuxPRNGSecureRandomProvider"))) {
                    Security.insertProviderAt(new LinuxPRNGSecureRandomProvider(), 1);
                }


                SecureRandom rng1 = new SecureRandom();
                if (!rng1.getProvider().getClass().getSimpleName().equals("LinuxPRNGSecureRandomProvider")) {
                    if (ALLOW_BROKEN_PRNG) {
                        Log.w(PrngFixes.class.getSimpleName(),
                                "new SecureRandom() backed by wrong Provider: " + rng1.getProvider().getClass());
                        return;
                    } else {
                        throw new SecurityException("new SecureRandom() backed by wrong Provider: "
                                + rng1.getProvider().getClass());
                    }
                }

                SecureRandom rng2 = null;
                try {
                    rng2 = SecureRandom.getInstance("SHA1PRNG");
                } catch (NoSuchAlgorithmException e) {
                    if (ALLOW_BROKEN_PRNG) {
                        Log.w(PrngFixes.class.getSimpleName(), "SHA1PRNG not available", e);
                        return;
                    } else {
                        new SecurityException("SHA1PRNG not available", e);
                    }
                }
                if (!rng2.getProvider().getClass().getSimpleName().equals("LinuxPRNGSecureRandomProvider")) {
                    if (ALLOW_BROKEN_PRNG) {
                        Log.w(PrngFixes.class.getSimpleName(),
                                "SecureRandom.getInstance(\"SHA1PRNG\") backed by wrong" + " Provider: "
                                        + rng2.getProvider().getClass());
                        return;
                    } else {
                        throw new SecurityException(
                                "SecureRandom.getInstance(\"SHA1PRNG\") backed by wrong" + " Provider: "
                                        + rng2.getProvider().getClass());
                    }
                }
            }
        }

        /**
         * Generate seed.
         *
         * @return the byte[]
         */
        private static byte[] generateSeed() {
            try {
                ByteArrayOutputStream seedBuffer = new ByteArrayOutputStream();
                DataOutputStream seedBufferOut = new DataOutputStream(seedBuffer);
                seedBufferOut.writeLong(System.currentTimeMillis());
                seedBufferOut.writeLong(System.nanoTime());
                seedBufferOut.writeInt(Process.myPid());
                seedBufferOut.writeInt(Process.myUid());
                seedBufferOut.write(BUILD_FINGERPRINT_AND_DEVICE_SERIAL);
                seedBufferOut.close();
                return seedBuffer.toByteArray();
            } catch (IOException e) {
                throw new SecurityException("Failed to generate seed", e);
            }
        }

        /**
         * Gets the device serial number.
         *
         * @return the device serial number
         */
        private static String getDeviceSerialNumber() {
            // We're using the Reflection API because Build.SERIAL is only
            // available since API Level 9 (Gingerbread, Android 2.3).
            try {
                return (String) Build.class.getField("SERIAL").get(null);
            } catch (Exception ignored) {
                return null;
            }
        }

        /**
         * Gets the builds the fingerprint and device serial.
         *
         * @return the builds the fingerprint and device serial
         */
        private static byte[] getBuildFingerprintAndDeviceSerial() {
            StringBuilder result = new StringBuilder();
            String fingerprint = Build.FINGERPRINT;
            if (fingerprint != null) {
                result.append(fingerprint);
            }
            String serial = getDeviceSerialNumber();
            if (serial != null) {
                result.append(serial);
            }
            try {
                return result.toString().getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UTF-8 encoding not supported");
            }
        }

        /**
         * The Class LinuxPRNGSecureRandomProvider.
         */
        private static class LinuxPRNGSecureRandomProvider extends Provider {

            /**
             * Instantiates a new linux prng secure random provider.
             */
            public LinuxPRNGSecureRandomProvider() {
                super("LinuxPRNG", 1.0, "A Linux-specific random number provider that uses"
                        + " /dev/urandom");

                put("SecureRandom.SHA1PRNG", LinuxPRNGSecureRandom.class.getName());
                put("SecureRandom.SHA1PRNG ImplementedIn", "Software");
            }
        }

        /**
         * The Class LinuxPRNGSecureRandom.
         */
        public static class LinuxPRNGSecureRandom extends SecureRandomSpi {


            /**
             * The Constant URANDOM_FILE.
             */
            private static final File URANDOM_FILE = new File("/dev/urandom");

            /**
             * The Constant sLock.
             */
            private static final Object sLock = new Object();


            /**
             * The s urandom in.
             */
            private static DataInputStream sUrandomIn;


            /**
             * The s urandom out.
             */
            private static OutputStream sUrandomOut;


            /**
             * The m seeded.
             */
            private boolean mSeeded;

            /* (non-Javadoc)
             * @see java.security.SecureRandomSpi#engineSetSeed(byte[])
             */
            @Override
            protected void engineSetSeed(byte[] bytes) {
                try {
                    OutputStream out;
                    synchronized (sLock) {
                        out = getUrandomOutputStream();
                    }
                    out.write(bytes);
                    out.flush();
                } catch (IOException e) {

                    Log.w(PrngFixes.class.getSimpleName(), "Failed to mix seed into "
                            + URANDOM_FILE);
                } finally {
                    mSeeded = true;
                }
            }

            /* (non-Javadoc)
             * @see java.security.SecureRandomSpi#engineNextBytes(byte[])
             */
            @Override
            protected void engineNextBytes(byte[] bytes) {
                if (!mSeeded) {

                    engineSetSeed(generateSeed());
                }

                try {
                    DataInputStream in;
                    synchronized (sLock) {
                        in = getUrandomInputStream();
                    }
                    synchronized (in) {
                        in.readFully(bytes);
                    }
                } catch (IOException e) {
                    throw new SecurityException("Failed to read from " + URANDOM_FILE, e);
                }
            }

            /* (non-Javadoc)
             * @see java.security.SecureRandomSpi#engineGenerateSeed(int)
             */
            @Override
            protected byte[] engineGenerateSeed(int size) {
                byte[] seed = new byte[size];
                engineNextBytes(seed);
                return seed;
            }

            /**
             * Gets the urandom input stream.
             *
             * @return the urandom input stream
             */
            private DataInputStream getUrandomInputStream() {
                synchronized (sLock) {
                    if (sUrandomIn == null) {

                        try {
                            sUrandomIn = new DataInputStream(new FileInputStream(URANDOM_FILE));
                        } catch (IOException e) {
                            throw new SecurityException("Failed to open " + URANDOM_FILE
                                    + " for reading", e);
                        }
                    }
                    return sUrandomIn;
                }
            }

            /**
             * Gets the urandom output stream.
             *
             * @return the urandom output stream
             * @throws IOException Signals that an I/O exception has occurred.
             */
            private OutputStream getUrandomOutputStream() throws IOException {
                synchronized (sLock) {
                    if (sUrandomOut == null) {
                        sUrandomOut = new FileOutputStream(URANDOM_FILE);
                    }
                    return sUrandomOut;
                }
            }
        }
    }
}
