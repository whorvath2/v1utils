package com.billhorvath.v1mods;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.util.*;
import java.security.*;
import org.bouncycastle.jce.provider.*;

/*	
	This is a utility class for encrypting and decrypting String values using a secret key located at KEY_LOCATION.
*/
public class EncryptionUtils{

	public static final String ENCODING 				= "UTF-8";
	public static final String CIPHER_ALGORITHM 		= "AES"; 
	public static final String KEY_ALGORITHM 			= "AES"; 
	public static final String HASH_ALGORITHM 			= "SHA-256";

	private static final String KEY_LOCATION			= "./key";
	private static final Provider PROVIDER = new BouncyCastleProvider();
	
	/*
	This class is composed entirely of static utility methods, and cannot be instantiated.
	*/
	private EncryptionUtils(){}
	
	/*
	Accept a String argument, encrypts it according to HASH_ALGORITHM, and returns the resulting encrypted String.

	@param encryptMe The String to be encrypted.
	@return A String that is the decrypted equivalent of the decryptMe parameter.
	*/
	public static String encrypt(String encryptMe){

		Cipher cipher = buildCipher(Cipher.ENCRYPT_MODE);
		int outSize = cipher.getOutputSize(encryptMe.length());
		byte[] outBytes = new byte[outSize];
		try {
			int written = cipher.doFinal(encryptMe.getBytes(), 0, encryptMe.length(), 
				outBytes);
        }
        catch(Exception e){
        	e.printStackTrace();
        	assert false;
        }
        String result = toHex(outBytes);
        return result;
	}
	
	/*
	Encodes an array of bytes in hex format and returns it as a String, so that it can be written out in platform-independent fashion.
	@param outBytes The byte array to be converted to hex format.
	@return A String composed of characters that are the hex-format equivalent of the bytes in the outBytes parameter.
	*/
	private static String toHex(byte[] outBytes){
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < outBytes.length; i++){
			builder.append(
				Integer.toString((outBytes[i] & 0xff) + 0x100, 16).substring(1));
		}
        return builder.toString();
	}
	/*
	Decrypts an encrypted String argument that is composed of characters representing hex-formatted bytes. This method first converts the String from a hex-based format to an array of bytes, decrypts the string using a Cipher, then returns the decrypted value as a character array.
	@param hex A String composed of characters in hex format representing bytes.
	@return An array of characters representing the decrypted value of hex.
	*/
	public static char[] decrypt(String hex){
		byte[] unhexed = fromHex(hex);
		Cipher cipher = buildCipher(Cipher.DECRYPT_MODE);
		int outSize = cipher.getOutputSize(unhexed.length);
		char[] result = new char[outSize];
		byte[] decryptedBytes = new byte[outSize];
		try{
			int written = cipher.doFinal(unhexed, 0, outSize, decryptedBytes);
		}
        catch(Exception e){
        	e.printStackTrace();
        	assert false;
        }
		try{
			String str = new String(decryptedBytes, ENCODING);
			result = str.toCharArray();
		}
		catch (Exception e){
			e.printStackTrace();
			assert false;
		}
        return result;
	}
	/*
	Converts a String composed of characters which represent bytes encoded as hexadecimal numbers into an array of bytes.
	@param hex The String composed of a sequence of characters which represent hexadecimal numbers.
	@return An array of byte values which correspond to the hexadecimal values represented by the characters in hex.
	*/
	public static byte[] fromHex(String hex){
		byte[] binary = new byte[hex.length() / 2];
		for (int i = 0; i < binary.length; i++){
			binary[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		}
		return binary;
	}
	/*
	Reads a secret String from KEY_LOCATION and creates a cipher using that key which can be used to encrypt or decrypt data. DO NOT include the file at KEY_LOCATION in your source code commits, or you will compromise the security of your data.
	@param mode The mode of the Cipher; must be either Cipher.ENCRYPT or Cipher.DECRYPT.
	@return A Cipher which can be used to encrypt or decrypt data using the secret String found at KEY_LOCATION.
	*/    
    private static Cipher buildCipher(int mode){
    
		Cipher cipher = null;
		BufferedReader reader = null;
    	try{
			cipher = Cipher.getInstance(CIPHER_ALGORITHM, PROVIDER);
			File file = new File(KEY_LOCATION);
			assert file.exists();
			assert (file.length() > 0);
			reader = new BufferedReader(new FileReader(file));
			String keyStr = reader.readLine();
			assert keyStr != null;
			assert (!(keyStr.isEmpty()));
			Key key = buildKey(keyStr);
			cipher.init(mode, key);
		}
		catch(Exception e){
			e.printStackTrace();
			assert false;
			throw new IllegalStateException();
		}
    	return cipher;
    }
    /*
	Creates a Key using the provided String parameter.
	@param secret The secret String used as the seed for the Key.
	@return A Key which can be used in a Cipher.
    */
    private static Key buildKey(String secret){
		SecretKeySpec keySpec = null;
    	try{
    		MessageDigest digester = MessageDigest.getInstance(HASH_ALGORITHM, PROVIDER);
    		digester.update(secret.getBytes());
    		byte[] key = digester.digest();
    		keySpec = new SecretKeySpec(key, KEY_ALGORITHM);
    	}
    	catch(Exception e){
    		e.printStackTrace();
    		assert false;
    		throw new IllegalStateException();
    	}
    	return keySpec;
    }
    
}