package com.billhorvath.v1mods;

import java.io.*;
import java.net.*;

/*
	This class provides a means of setting a new token for accessing the VersionOne API from other classes in this package.
*/
public class TokenUtils{

	protected final static String CERT_FILE_LOC = "./cert";
	
	/**/
	public static void main(String[] args){
		if (args == null || args.length == 0){
			System.out.println("You must initialize this program with the token for which "
				+ "you wish to create a certificate.\nUsage: java -cp . com.billhorvath.v1mods.TokenUtils [token]");
			System.exit(1);			
		}
		else{
			createCertFile(args[0]);
			System.exit(0);
		}
	}
	
	/**/
	private static void createCertFile(String token){
	
		if (setAccessToken(token, CERT_FILE_LOC)){
			System.out.println("Token created at " + CERT_FILE_LOC + "\nExiting...");
		}
		else{
			System.out.println("Token creation failure!");
			System.exit(1);
		}
	}
	/*
	
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
		String encryptedToken = EncryptionUtils.encrypt(token);
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(certFile)));
			writer.write(encryptedToken, 0, encryptedToken.length() - 1);
			result = true;
		}
		catch(Exception e){
			e.printStackTrace();
			assert false;
		}
		return result;
	}
}