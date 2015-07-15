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
	/* The URL for the VersionOne installation from which the Services 
		will be acquired. */
	private static final String V1_LOC = 
		"https://www8.v1host.com/ParishSOFTLLC/";
	
	
	private static V1Services instance;

	private Services services;


	/**
	Instantiates the VersionOne Services using the encrypted token located at certFile.
	*/
	private V1Services(String certFile){
	
		setServices(certFile);
		
	}
	
	/**
	Returns a VersionOne V1Services object which can be used to access the
		VersionOne API. Returns null if the file at certFile doesn't exist.
	@param certFile The location of the file containing the encrypted token.
	@return A V1Services instance connected to V1_LOC, or null if the file at certFile doesn't exist.
	*/
	public static V1Services getInstance(String certFile){
		File file = new File(certFile);
		if (!(file.exists())){
			System.out.println("ERROR: " + certFile + " is missing");
			return null;
		}
		if (instance == null){
			instance = new V1Services(certFile);
		}
		else {
			instance.setServices(certFile);
		}
		return instance;
		
	}
	/**
	*/
	private void setServices(String certFile){
		V1Connector connector = null;
		try{
			connector = V1Connector
				.withInstanceUrl(V1_LOC)
				.withUserAgentHeader("TeamModder", "1.0")
				.withAccessToken(getAccessToken(certFile))
				.build();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		assert connector != null;
		
		this.services = new Services(connector);
	}

	/**
	Returns a VersionOne Services instance.
	@return A VersionOne Services instance.
	*/
	public Services services(){
		return this.services;
	}
	
	/**
	Returns a String view of the access token which authenticates this application with VersionOne.
	@param certFile The location of the file containing the encrypted access token.
	@return the access token which authenticates this application with VersionOne.
	*/
	private static String getAccessToken(String certFile){
		String result = "";
		File file = new File(certFile);
		assert file.exists();
		assert (!(file.length()>Integer.MAX_VALUE));
		int fileLength = (int)file.length();
		
		char[] buffer = new char[fileLength];
		assert buffer.length > 0;
		String str = "";
		
		FileReader in = null;
		try{
			in = new FileReader(file);
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