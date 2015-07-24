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
	
	/** The services which will be provided to the client class. */
	private final IServices services;
	
	private static V1Services instance;

	/**
	Instantiates the VersionOne Services using the encrypted token located at certFile.
	*/
	private V1Services(){

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
		if (instance == null){
			instance = new V1Services();
		}
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
		BufferedReader reader = null;
    	try{
			InputStreamReader streamReader = new InputStreamReader(
				V1Services.class.getResourceAsStream(
					TokenUtils.TOKEN_FILE_LOC));
			reader = new BufferedReader(streamReader);
			String str = reader.readLine();
			assert str != null;
			assert (!(str.isEmpty()));
		
			result = String.valueOf(EncryptionUtils.decrypt(str));
			assert result != null;
			assert result.isEmpty() == false;
		}
		catch(Exception e){
			e.printStackTrace();
			assert false;
		}
		return result;
	}
}