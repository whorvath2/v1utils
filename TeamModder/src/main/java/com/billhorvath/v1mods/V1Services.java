package com.billhorvath.v1mods;

import java.io.*;
import java.net.*;
import com.versionone.*;
import com.versionone.apiclient.*;
import com.versionone.apiclient.services.*;
import com.versionone.apiclient.interfaces.*;

/**
	This class serves to instantiate and return a connection to VersionOne in the form of a VersionOne Services instance.
*/
public class V1Services{

	/** The URL for the VersionOne installation from which the Services 
		will be acquired. */
	private static final String V1_LOC = 
		"https://www8.v1host.com/ParishSOFTLLC/";
	/** The name of the application, sent as a header item to VersionOne. */
	private static final String APPLICATION_NAME = "TeamModder";
	/** The version of the application, sent as a header item to VersionOne. */
	private static final String APPLICATION_VERSION = "1.1";

	/** The file from which the authentication token is read. */
	private final File tokenFile;
	
	/** The services which will be provided to the client class. */
	private final IServices services;
	
	private static V1Services instance;

	/**
	Instantiates the VersionOne Services using the encrypted token located at certFile.
	*/
	private V1Services(String tokenFileLoc){
	
		this.tokenFile = new File(tokenFileLoc);
		if (this.tokenFile == null 
			|| (!(this.tokenFile.exists())) 
			|| (!(this.tokenFile.length() > 0))
			|| (this.tokenFile.length() > Integer.MAX_VALUE)){
			assert false;
			throw new IllegalStateException("The file containing the token at " 
				+ tokenFileLoc 
				+ " is missing, has no content, or is too large!");
		}
		V1Connector connector = null;
		try{
			connector = V1Connector
				.withInstanceUrl(V1_LOC)
				.withUserAgentHeader(APPLICATION_NAME, APPLICATION_VERSION)
				.withAccessToken(getAccessToken())
				.build();
		}
		catch (Exception e){
			e.printStackTrace();
			assert false;
			throw new IllegalStateException("Unable to initiate the " 
				+ "connection to VersionOne.");
		}
		assert connector != null;
		this.services = new Services(connector);
		
	}
	/**
	Returns a VersionOne V1Services object which can be used to access the
		VersionOne API. Looks for the access token at TokenUtils.TOKEN_FILE_LOC.
	@return A V1Services instance connected to V1_LOC, or null if the file at certFile doesn't exist.
	*/
	public static V1Services getInstance(){
		return getInstance(TokenUtils.TOKEN_FILE_LOC);
	}
	
	/**
	Returns a VersionOne V1Services object which can be used to access the
		VersionOne API. Looks for the access token at <code>tokenFileLoc</code>.
	@param tokenFileLoc The location of the file containing the encrypted token.
	@return A V1Services instance connected to V1_LOC, or null if the file at certFile doesn't exist.
	*/
	public static V1Services getInstance(String tokenFileLoc){
		instance = new V1Services(tokenFileLoc);
		return instance;
	}

	/**
	Returns a VersionOne Services instance.
	@return A VersionOne Services instance.
	*/
	public IServices services(){
		return services;
	}
	
	/**
	Returns a String view of the access token which authenticates this application with VersionOne.
	@param certFile The location of the file containing the encrypted access token.
	@return the access token which authenticates this application with VersionOne.
	*/
	private String getAccessToken(){
		String result = "";
		int fileLength = (int)tokenFile.length();
		
		char[] buffer = new char[fileLength];
		assert buffer.length > 0;
		String str = "";
		
		FileReader in = null;
		try{
			in = new FileReader(tokenFile);
			in.read(buffer, 0, fileLength);
			in.close();
			str = new String(buffer);
		}
		catch (Exception e){
			e.printStackTrace();
			assert false;
		}
		finally{
			if (in != null){
				try{
					in.close();
				}
				catch(Exception e){
					e.printStackTrace();
					assert false;
				}
			}
		}
		
		result = String.valueOf(EncryptionUtils.decrypt(str));
		assert result != null;
		assert result.isEmpty() == false;

		return result;
	}
}