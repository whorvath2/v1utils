package com.billhorvath.v1mods;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.util.*;
import java.security.*;

public class EncryptionUtils{

	public static final String ENCODING = "UTF8";

	private static Cipher cipher;
	private static SecretKeySpec keySpec;
	private static IvParameterSpec ivSpec;
	static{
		init();
	}

	/*
	This class is composed entirely of static utility methods, and cannot be instantiated.
	*/
	private EncryptionUtils(){ 
	}
	/*
	*/
	public static String encrypt(String str){
		String result = null;
		
		try {
            byte[] encoded = str.getBytes(ENCODING);
            System.out.println("***");
//             cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            System.out.println("***");
            byte[] encrypted = cipher.doFinal(encoded);
            System.out.println("***");
            result = new String(encrypted, ENCODING);
        }
        catch(Exception e){
        	e.printStackTrace();
        	assert false;
        }
        return result;
	}
	/*
	*/
    public static String decrypt(String str) {
    	String result = "";
        try {
            byte[] encoded = str.getBytes(ENCODING);
// 			cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
			cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(encoded);
            result = new String(decrypted, ENCODING);
        }
        catch(Exception e){
        	assert false;
        	e.printStackTrace();
        }
        return result;
    }
    
   private static void init(){
		try{
			byte[] keyBytes = new byte[16];
			SecureRandom.getInstanceStrong().nextBytes(keyBytes);
			byte[] ivBytes = new byte[16];			
			keySpec = new SecretKeySpec(keyBytes, "AES");
			ivSpec = new IvParameterSpec(ivBytes);
// 			cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
			cipher = Cipher.getInstance("AES");
		}
		catch(Exception e){
			e.printStackTrace();
			assert false;
		}
	}
}