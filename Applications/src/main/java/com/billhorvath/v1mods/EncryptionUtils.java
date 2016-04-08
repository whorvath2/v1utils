package com.billhorvath.v1utils;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.util.*;
import java.security.*;
import java.nio.file.*;
import org.bouncycastle.jce.provider.*;

/**	
	This is a utility class for encrypting and decrypting String values using a secret key located at KEY_LOCATION.
*/
public class EncryptionUtils{

	/** How we'll encode Strings. */
	private static final String ENCODING 				= "UTF-8";
	/** The Cipher algorithm we'll use for encryption/decryption. */
	private static final String CIPHER_ALGORITHM 		= "AES"; 
	/** The Key algorithm we'll use for encryption/decryption. */
	private static final String KEY_ALGORITHM 			= "AES"; 
	/** The Hash algorithm we'll use for encryption/decryption. */
	private static final String HASH_ALGORITHM 			= "SHA-256";
	/** The Random Number Generator algorithm used for generating the salt. */
	private static final String SALT_ALGORITHM			= "SHA1PRNG";
	/** The location of the file in which the key is stored. */
	private static final String KEY_LOCATION			= "/master";
	/** The location of the file in which the salt for the hash is stored. */
	private static final String SALT_LOCATION			= "/mortons";
	/** The encryption provider we're using. */
	private static final Provider PROVIDER = new BouncyCastleProvider();

	/**
	This class is composed entirely of static utility methods, and 
		cannot be instantiated.
	*/
	private EncryptionUtils(){}
	
	/**
	Accept a String argument, encrypts it according to HASH_ALGORITHM, 
		and returns the resulting encrypted String.

	@param encryptMe The String to be encrypted.
	@return A String that is the encrypted equivalent of the encryptMe 
		parameter.
	*/
	public static String encrypt(String encryptMe){

		Cipher cipher = buildCipher(Cipher.ENCRYPT_MODE);
		int encLength = encryptMe.length();
		int outSize = cipher.getOutputSize(encLength);

		byte[] outBytes = new byte[outSize];
		try {
			int written = cipher.doFinal(encryptMe.getBytes(), 0,
				encLength, outBytes);
        }
        catch(Exception e){
        	e.printStackTrace();
        	assert false;
        }
        String result = toHex(outBytes);
        return result;
	}
	
	/**
	Encodes an array of bytes in hex format and returns it as a String, so 
		that it can be written out in platform-independent fashion.
	@param outBytes The byte array to be converted to hex format.
	@return A String composed of characters that are the hex-format 
		equivalent of the bytes in the outBytes parameter.
	*/
	private static String toHex(byte[] outBytes){
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < outBytes.length; i++){
			builder.append(
				Integer.toString((outBytes[i] & 0xff) + 0x100, 16).substring(1));
		}
        return builder.toString();
	}
	/**
	Decrypts an encrypted String argument that is composed of characters
		representing hex-formatted bytes. This method first converts the
		String from a hex-based format to an array of bytes, decrypts 
		the string using a Cipher, then returns the decrypted value as a 
		character array.
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
			char[] chars = str.toCharArray();
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < chars.length; i++){
				if (chars[i] != 0) builder.append(chars[i]);
			}	
			result = builder.toString().toCharArray();
		}
		catch (Exception e){
			e.printStackTrace();
			assert false;
		}
        return result;
	}
	/**
	Converts a String composed of characters which represent bytes encoded 
		as hexadecimal numbers into an array of bytes.
	@param hex The String composed of a sequence of characters which represent 
		hexadecimal numbers.
	@return An array of byte values which correspond to the hexadecimal values
		represented by the characters in hex.
	*/
	public static byte[] fromHex(String hex){
		byte[] binary = new byte[hex.length() / 2];
		for (int i = 0; i < binary.length; i++){
			binary[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		}
		return binary;
	}
	/**
	Reads secret Strings from KEY_LOCATION and SALT_LOCATION and creates a cipher using that data which can be used to encrypt or decrypt data. If the file at SALT_LOCATION cannot be found, it will create a salt and store it there; otherwise, it will read the salt from the file and use it to create the cipher's key.
	
	DO NOT include the file at KEY_LOCATION or SALT_LOCATION in your source code commits, or you will compromise the security of your data.
	@param mode The mode of the Cipher; must be either Cipher.ENCRYPT or
		Cipher.DECRYPT.
	@return A Cipher which can be used to encrypt or decrypt data using the
		secret String found at KEY_LOCATION.
	*/    
    private static Cipher buildCipher(int mode){
    
		Cipher cipher = null;
		BufferedReader reader = null;
    	try{
			cipher = Cipher.getInstance(CIPHER_ALGORITHM, PROVIDER);
			InputStreamReader streamReader = new InputStreamReader(
				EncryptionUtils.class.getResourceAsStream(KEY_LOCATION));
			reader = new BufferedReader(streamReader);
			String keyStr = reader.readLine();
			assert keyStr != null;
			assert (!(keyStr.isEmpty()));
			
			byte[] saltBytes = new byte[0];
			//If we're encrypting, let's create and store the salt....
			if (mode == Cipher.ENCRYPT_MODE){
				saltBytes = createSalt();
			}
			//Otherwise, lets see if the salt is there and use it if it is...
			else{
				streamReader = new InputStreamReader(
					EncryptionUtils.class.getResourceAsStream(SALT_LOCATION));
				reader = new BufferedReader(streamReader);
				String saltStr = reader.readLine();
				assert saltStr != null;
				assert (!(saltStr.isEmpty()));
				saltBytes = fromHex(saltStr);
			}
			Key key = buildKey(keyStr, saltBytes);
			cipher.init(mode, key);
		}
		catch(Exception e){
			e.printStackTrace();
			assert false;
			throw new IllegalStateException();
		}
    	return cipher;
    }
    /**
	Creates a Key using the provided String parameter.
	@param secret The secret String used as the seed for the Key.
	@param salt The bytes used to salt the hash.
	@return A Key which can be used in a Cipher.
    */
    private static Key buildKey(String secret, byte[] salt){
		SecretKeySpec keySpec = null;
    	try{
    		MessageDigest digester = MessageDigest.getInstance(
    			HASH_ALGORITHM, PROVIDER);
    		if (salt.length > 0) digester.update(salt);
    		byte[] key = digester.digest(secret.getBytes());
    		digester.reset();
    		keySpec = new SecretKeySpec(key, KEY_ALGORITHM);
    	}
    	catch(Exception e){
    		e.printStackTrace();
    		assert false;
    		throw new IllegalStateException();
    	}
    	return keySpec;
    }
    /**
    	Generates a salt to use when building the key.
    	@return A byte array containing the salt value.
    */
    private static byte[] createSalt(){

    	byte[] salt = new byte[16];

		File file = new File("./src/main/resources" + SALT_LOCATION);
		OutputStream writer = null;
		try{
			SecureRandom.getInstance(SALT_ALGORITHM).nextBytes(salt);
			String saltStr = toHex(salt);
			byte[] hexBytes = saltStr.getBytes();
			writer = new BufferedOutputStream(new FileOutputStream(file));
			writer.write(hexBytes, 0, hexBytes.length);
		}
		catch(Exception e){
			e.printStackTrace();
			assert false;
			salt = new byte[0];
		}
		finally{
			if (writer != null){
				try{
					writer.flush();
					writer.close();
				}
				catch(Exception e){
					e.printStackTrace();
					assert false;
					salt = new byte[0];
				}
			}
			writer = null;
		}
// 		assert file.length() == salt.length;
    	return salt;
    }
}