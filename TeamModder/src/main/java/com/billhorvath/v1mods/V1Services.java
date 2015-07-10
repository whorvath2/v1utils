package com.billhorvath.v1mods;

import java.io.*;
import java.net.*;
import com.versionone.*;
import com.versionone.apiclient.*;
import com.versionone.apiclient.services.*;
import com.versionone.apiclient.interfaces.*;

public class V1Services{

	private static final String DEFAULT_CERT_LOC = "./cert";
	
	private static V1Services instance;

	private Services services;


	/*
	*/
	private V1Services(String certFile){
	
		setServices(certFile);
		
	}
	
	/*
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
	/*
	*/
	private void setServices(String certFile){
		V1Connector connector = null;
		try{
			connector = V1Connector
				.withInstanceUrl("https://www8.v1host.com/ParishSOFTLLC/")
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

	/*
	*/
	public Services services(){
		return this.services;
	}
	
	/*
	Returns a String view of the access token which authenticates this application with VersionOne.
	@return the access token which authenticates this application with VersionOne.
	*/
	private static String getAccessToken(String certFile){
		String result = "";
		File file = new File(certFile);
		try{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			result = new String(reader.readLine().getBytes(), EncryptionUtils.ENCODING);
		}
		catch (Exception e){
			e.printStackTrace();
			assert false;
		}
		assert result != null;
		assert result.isEmpty() == false;
		
		result = EncryptionUtils.decrypt(result);
		return result;
	}
}