package com.communication.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class FileDES {
    /**
     * 加密解密的key
     */
    private Key mKey;

    private final static String ENCIPHER_MODEL = "DES/ECB/NOPADDING";

    private final static String ALGORITHM = "DES";
    private byte[] key;

    public FileDES(String key) throws Exception {
        this.key = key.getBytes("UTF-8");
    }

    public FileDES(byte[] key) throws Exception {
        this.key = key;
    }

    public static byte[] desEncrypt(byte[] source, byte rawKeyData[])
            throws GeneralSecurityException {
        // 处理密钥
        SecretKeySpec key = new SecretKeySpec(rawKeyData, ALGORITHM);
        // 加密
        // DES/ECB/PKCS5Padding
        // DES/ECB/NOPADDING
        Cipher cipher = Cipher.getInstance(ENCIPHER_MODEL);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(source);
    }

    public static byte[] desDecrypt(byte[] mi, byte rawKeyData[])
            throws GeneralSecurityException {
        // 处理密钥
        SecretKeySpec key = new SecretKeySpec(rawKeyData, ALGORITHM);
        // 加密
        // DES/ECB/PKCS5Padding
        // DES/ECB/NOPADDING
        Cipher cipher = Cipher.getInstance(ENCIPHER_MODEL);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(mi);
    }

    public static byte[] readFile(String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists())
            return null;

        int size = (int) file.length();
        byte byteArr[] = new byte[size];

        FileInputStream inputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(
                inputStream);
        bufferedInputStream.read(byteArr, 0, byteArr.length);
        bufferedInputStream.close();

        return byteArr;
    }

    /**
     * 加密文件
     *
     * @param filePath 需要加密的文件路径
     * @param savePath 加密后保存的位置
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public void encryptFile(String filePath, String savePath)
            throws IOException, GeneralSecurityException {
        byte[] data = readFile(filePath);
        byte[] enCryptData = null;
        if (null != data) {
            enCryptData = desEncrypt(data, key);
            if (null != enCryptData && enCryptData.length > 0) {
                OutputStream os = new FileOutputStream(savePath);
                os.write(enCryptData);
                os.flush();
                os.close();
            }
        }

    }

    /**
     * 解密文件
     *
     * @param filePath 文件路径
     * @throws Exception
     */
    public void decryptFile(String filePath, String savePath)
            throws IOException, GeneralSecurityException {
        byte[] data = readFile(filePath);
        byte[] enCryptData = null;
        if (null != data) {
            enCryptData = desDecrypt(data, key);
            if (null != enCryptData && enCryptData.length > 0) {
                OutputStream os = new FileOutputStream(savePath);
                os.write(enCryptData);
                os.flush();
                os.close();
            }
        }
    }
}
