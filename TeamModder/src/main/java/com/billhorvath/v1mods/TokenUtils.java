package com.billhorvath.v1mods;

import java.io.*;
import java.net.*;

/*
	This class provides a means of setting a new token for accessing the VersionOne API from other classes in this package.
*/
public class TokenUtils{

	protected final static String CERT_FILE_LOC = "./enc_token";
	
	/*
	Creates a file containing the platform-independent, encrypted form of the access token submitted as the first argument.
	
	*/
	public static void main(String[] args){
		if (args == null || args.length == 0){
			System.out.println("You must initialize this program with the "
				+ "token for which you wish to create a certificate."
				+ "\nUsage: java -cp . com.billhorvath.v1mods.TokenUtils "
				+ "[token]\n\tor java -jar [jarname].jar " 
				+ "com.billhorvath.v1mods.TokenUtils [token]");
			System.exit(1);			
		}
		else{
			createEncFile(args[0]);
			System.exit(0);
		}
	}
	
	/*
	Encrypts token and stores it in CERT_FILE_LOC.
	@param token The plain-text token to be encrypted.
	*/
	private static void createEncFile(String token){
	
		if (setAccessToken(token, CERT_FILE_LOC)){
			System.out.println("Token created at " + CERT_FILE_LOC + "\nExiting...");
		}
		else{
			System.out.println("Token creation failure!");
			System.exit(1);
		}
	}
	/*
	Encrypts token and stores it in certFile.
	@param token The plain-text token to be encrypted.
	@param certFile The location of the file to which the encrypted token will
		be written.	
	*/
	public static boolean setAccessToken(String token, String certFile){
		File file = new File(certFile);
		if (file.exists()){
			if (!(file.delete())){
				System.out.println("Unable to delete " + file);
				assert false;
				return false;
			}
		}
		try{
			if (!(file.createNewFile())){
				System.out.println("Unable to create" + file);
				assert false;
				return false;
			}
		}
		catch(Exception e){
			System.out.println("Unable to create" + file);
			e.printStackTrace();
			assert false;
			return false;
		}
		
		boolean result = false;
		byte[] encryptedBytes = EncryptionUtils.encrypt(token).getBytes();
		System.out.println("encryptedBytes = " + new String(encryptedBytes));
		System.out.println("\tencryptedBytes.length = " + encryptedBytes.length);

		OutputStream writer = null;
		try{
			writer = new BufferedOutputStream(new FileOutputStream(file));
			writer.write(encryptedBytes, 0, encryptedBytes.length);
			result = true;
		}
		catch(Exception e){
			e.printStackTrace();
			assert false;
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
				}
			}
			writer = null;
		}
		assert file.length() == encryptedBytes.length;
		return result;
	}
}