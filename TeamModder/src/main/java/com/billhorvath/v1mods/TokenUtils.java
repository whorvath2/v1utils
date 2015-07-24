package com.billhorvath.v1mods;

import java.io.*;
import java.net.*;
import java.nio.file.*;

/**
	This class provides a means of setting a new token for accessing the VersionOne API from other classes in this package.
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
	
	/**
	Encrypts <code>token</code> and stores it in <code>TOKEN_FILE_LOC</code>.
	@param token The plain-text token to be encrypted.
	*/
	private static void createEncFile(String token){
	
		if (setAccessToken(token, TOKEN_FILE_LOC)){
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
	@param tokenFile The location of the file to which the encrypted token will
		be written.	
	@return True if token was successfully written out to certFile; false otherwise.
	*/
	protected static boolean setAccessToken(String token, String tokenFile){

		boolean result = false;
		File file = new File(tokenFile);
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
	Returns a String view of the decrypted access token stored at TOKEN_FILE_LOC which authenticates an application with VersionOne.
	@return the access token which authenticates this application with VersionOne.
	*/
	protected static String getAccessToken(){
		return getAccessToken(TOKEN_FILE_LOC);
	}
	/**
	Returns a String view of the decrypted access token which authenticates an application with VersionOne.
	@param tokenFileLoc The location of the file containing the encrypted access token.
	@return the access token which authenticates this application with VersionOne.
	*/
	protected static String getAccessToken(String tokenFileLoc){
		
	
// 		File tokenFile = new File(tokenFileLoc);
// 		if (tokenFile == null 
// 			|| (!(tokenFile.exists())) 
// 			|| (!(tokenFile.length() > 0))
// 			|| (tokenFile.length() > Integer.MAX_VALUE)){
// 			assert false;
// 			throw new IllegalStateException("The file containing the token at " 
// 				+ tokenFileLoc 
// 				+ " is missing, has no content, or is too large!");
// 		}
		String result = "";
// 		int fileLength = (int)tokenFile.length();
// 		
// 		char[] buffer = new char[fileLength];
// 		assert buffer.length > 0;
// 		String str = "";
// 		
// 		FileReader in = null;
// 		try{
// 			in = new FileReader(tokenFile);
// 			in.read(buffer, 0, fileLength);
// 			in.close();
// 			str = new String(buffer);
// 		}
// 		catch (Exception e){
// 			e.printStackTrace();
// 			assert false;
// 		}
// 		finally{
// 			if (in != null){
// 				try{
// 					in.close();
// 				}
// 				catch(Exception e){
// 					e.printStackTrace();
// 					assert false;
// 				}
// 			}
// 		}
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
	If file exists, overwrites file with a file that is zero bytes in length. Otherwise, creates a new file at the same location.
	@param file The file that will be overwritten or created.
	@return true if the entire operation completed successfully; false otherwise.
	*/
	private static boolean resetFile(File file){
		boolean result = false;
		if (file.exists()){
			if (!(file.delete())){
				System.out.println("Unable to delete " + file);
				assert false;
				return result;
			}
		}
		try{
			if (!(file.createNewFile())){
				System.out.println("Unable to create" + file);
				assert false;
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