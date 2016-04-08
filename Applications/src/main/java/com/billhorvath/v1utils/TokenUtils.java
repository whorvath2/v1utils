package com.billhorvath.v1utils;

import java.io.*;
import java.net.*;
import java.nio.file.*;

/**
	This class provides a means of setting a new authorization token for accessing the VersionOne API from other classes in this package.
*/
public class TokenUtils{

	protected final static String TOKEN_FILE_LOC = "/coin";
	
	/**
	Creates a file containing the platform-independent, encrypted form of the access token submitted as the first argument. <b>If no arguments are submitted, this class will exit with an error message.</b>
	@param args String arguments submitted from the command line.
	*/
	public static void main(String[] args){
		if (args == null || args.length == 0){
			System.out.println("You must initialize this program with the "
				+ "token for which you wish to create a certificate."
				+ "\nUsage: java -cp . com.billhorvath.v1utils.TokenUtils "
				+ "[token]\n\tor java -jar [jarname].jar " 
				+ "com.billhorvath.v1utils.TokenUtils [token]");
			System.exit(1);			
		}
		else{
			createEncFile(args[0]);
			System.exit(0);
		}
	}
	
	/**
	Encrypts <code>token</code> and stores it in <code>TOKEN_FILE_LOC</code>.
	@param token The plain-text token to be encrypted.
	*/
	private static void createEncFile(String token){
	
		if (setAccessToken(token)){
			System.out.println("Token created at " + TOKEN_FILE_LOC + "\nExiting...");
		}
		else{
			System.out.println("Token creation failure!");
			System.exit(1);
		}
	}
	/**
	Encrypts <code>token</code> and stores it in <code>certFile</code>.
	@param token The plain-text token to be encrypted.
	@return True if token was successfully written out to certFile; false otherwise.
	*/
	protected static boolean setAccessToken(String token){

		boolean result = false;
		File file = new File("./src/main/resources" + TOKEN_FILE_LOC);

		if (!(resetFile(file))) return result;
		
		byte[] encryptedBytes = EncryptionUtils.encrypt(token).getBytes();

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
	/**
	Returns a String view of the decrypted access token which authenticates an application with VersionOne.
	@return the access token which authenticates this application with VersionOne.
	*/
	protected static String getAccessToken(){
		String result = "";
		BufferedReader reader = null;
    	try{
			InputStreamReader streamReader = new InputStreamReader(
				TokenUtils.class.getResourceAsStream(TOKEN_FILE_LOC));
			reader = new BufferedReader(streamReader);
			String str = reader.readLine();
			assert str != null;
			assert (!(str.isEmpty()));
			result = String.valueOf(EncryptionUtils.decrypt(str));
		}
		catch(Exception e){
			e.printStackTrace();
			assert false;
		}
		assert result != null;
		assert result.isEmpty() == false;

		return result;
	}
	/**
	If <code>file</code> exists, overwrites <code>file</code> with a file that is zero bytes in length. Otherwise, creates a new file at the same location.
	@param file The file that will be overwritten or created.
	@return true if the entire operation completed successfully; false otherwise.
	*/
	private static boolean resetFile(File file){
		boolean result = false;
		try{
			if (file.exists()){
				if (!(file.delete())){
					throw new Exception("File deletion failed.");
				}
			}
			if (!(file.createNewFile())){
				throw new Exception("File creation failed.");
			}
			else result = true;
		}
		catch(Exception e){
			System.out.println("Unable to create" + file);
			e.printStackTrace();
			assert false;
		}
		return result;
	}
}