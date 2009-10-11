/*
 * Copyright (c) 2009 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.crypt.camellia;

import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;


/**
 * CamelliaCipher. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2009/02/22 nsano initial version <br>
 */
public final class CamelliaCipher extends CipherSpi {

    /* @see javax.crypto.CipherSpi#engineDoFinal(byte[], int, int) */
    @Override
    protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen) throws IllegalBlockSizeException, BadPaddingException {
        byte[] output = engineUpdate(input, inputOffset, inputLen);
        finalized = true;
        return output;
    }

    /* @see javax.crypto.CipherSpi#engineDoFinal(byte[], int, int, byte[], int) */
    @Override
    protected int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        int outputLen = engineUpdate(input, inputOffset, inputLen, output, outputOffset);
        finalized = true;
        return outputLen;
    }

    /* @see javax.crypto.CipherSpi#engineGetBlockSize() */
    @Override
    protected int engineGetBlockSize() {
        return 4;
    }

    /* @see javax.crypto.CipherSpi#engineGetIV() */
    @Override
    protected byte[] engineGetIV() {
        // TODO Auto-generated method stub
        return null;
    }

    /* @see javax.crypto.CipherSpi#engineGetOutputSize(int) */
    @Override
    protected int engineGetOutputSize(int inputLen) {
        if (opmode == Cipher.ENCRYPT_MODE) {
            int pad = inputLen % engineGetBlockSize();
            return (inputLen + (pad == 0 ? 0 : engineGetBlockSize() - pad)) * 4;
        } else {
            return inputLen / 4;
        }
    }

    /* @see javax.crypto.CipherSpi#engineGetParameters() */
    @Override
    protected AlgorithmParameters engineGetParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    private Camellia camellia = new Camellia();

    private boolean finalized = false;

    private int opmode;

    private int[] keyTable = new int[52];

    /* @see javax.crypto.CipherSpi#engineInit(int, java.security.Key, java.security.SecureRandom) */
    @Override
    protected void engineInit(int opmode, Key key, SecureRandom random) throws InvalidKeyException {
        this.opmode = opmode;

        int[] keyInts = new int[16];
        byte[] keyBytes = key.getEncoded();
        for (int i = 0; i < keyBytes.length; i++) {
            keyInts[i] = keyBytes[i] & 0xff;
        }

        camellia.genEkey(keyInts, keyTable);

        finalized = false;
    }

    /* @see javax.crypto.CipherSpi#engineInit(int, java.security.Key, java.security.spec.AlgorithmParameterSpec, java.security.SecureRandom) */
    @Override
    protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        engineInit(opmode, key, random);
    }

    /* @see javax.crypto.CipherSpi#engineInit(int, java.security.Key, java.security.AlgorithmParameters, java.security.SecureRandom) */
    @Override
    protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        engineInit(opmode, key, random);
    }

    /* @see javax.crypto.CipherSpi#engineSetMode(java.lang.String) */
    @Override
    protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
        // TODO Auto-generated method stub
    }

    /* @see javax.crypto.CipherSpi#engineSetPadding(java.lang.String) */
    @Override
    protected void engineSetPadding(String padding) throws NoSuchPaddingException {
        // TODO Auto-generated method stub
    }

    /* @see javax.crypto.CipherSpi#engineUpdate(byte[], int, int) */
    @Override
    protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
        byte[] output = new byte[engineGetOutputSize(input.length)];
        try {
            engineUpdate(input, inputOffset, inputLen, output, 0);
        } catch (ShortBufferException e) {
            assert false : e;
        }
        return output;
    }

    /* @see javax.crypto.CipherSpi#engineUpdate(byte[], int, int, byte[], int) */
    @Override
    protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException {
        if (finalized) {
            throw new IllegalStateException("finalized"); // TODO check
        }

        int blockSize = engineGetBlockSize();

        int[] in = new int[4];
        int[] out = new int[4];

        if (opmode == Cipher.ENCRYPT_MODE) {
            byte[] dataBytes = new byte[engineGetOutputSize(inputLen) / 4];
//System.err.println("dataBytes: " + dataBytes.length);
            System.arraycopy(input, inputOffset, dataBytes, 0, inputLen);

            for (int i = 0; i < dataBytes.length; i += blockSize) {
                for (int j = 0; j < blockSize; j++) {
                    in[j] = dataBytes[i + j] & 0xff;
                }
                camellia.encryptBlock(in, keyTable, out);
                for (int j = 0; j < blockSize; j++) {
                    // TODO consider endian
                    for (int k = 0; k < 4; k++) {
                        output[outputOffset + i * 4 + j * 4 + k] = (byte) (out[j] >> ((3 - k) * 8));
//System.err.printf("Y[%02d] %02x\n", outputOffset + i * 4 + j * 4 + k, output[outputOffset + i + j * 4 + k]);
                    }
//System.err.printf("E: in[%02d]=%02x, out[%02d]=%08x\n", inputOffset + i + j, dataBytes[i + j], outputOffset + i + j, out[j]);
                }
            }
        } else if (opmode == Cipher.DECRYPT_MODE) {
//System.err.println("inputLen: " + inputLen / 4);
            for (int i = 0; i < inputLen / 4; i += blockSize) {
                for (int j = 0; j < blockSize; j++) {
                    // TODO consider endian
                    in[j] = 0;
                    for (int k = 0; k < 4; k++) {
//System.err.printf("X[%02d] %02x\n", inputOffset + i * 4 + j * 4 + k, input[inputOffset + i * 4 + j * 4 + k] & 0xff);
                        in[j] |= (input[inputOffset + i * 4 + j * 4 + k] & 0xff) << ((3 - k) * 8);
                    }
                }
                camellia.decryptBlock(in, keyTable, out);
                for (int j = 0; j < blockSize; j++) {
                    output[outputOffset + i + j] = (byte) out[j];
//System.err.printf("D: in[%02d]=%08x, out[%02d]=%02x\n", inputOffset + i + j, in[j], outputOffset + i + j, output[outputOffset + i + j]);
                }
            }
        } else {
            assert false : opmode;
        }

        return engineGetOutputSize(inputLen);
    }

    /** */
    public static class CamelliaKey implements Key {
        /** 16 bytes (128 bit) key */
        byte[] key;
        /** @param key 16 bytes using UTF-8 encoding */
        public CamelliaKey(String key) {
            try {
                this.key = key.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                assert false;
            }
        }
        public byte[] getEncoded() {
            return key;
        }
        public String getAlgorithm() {
            return "Camellia";
        }
        public String getFormat() {
            return "B]";
        }
    };
}

/* */

