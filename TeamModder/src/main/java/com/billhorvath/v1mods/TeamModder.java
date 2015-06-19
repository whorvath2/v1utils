package com.billhorvath.v1mods;

import com.versionone.*;
import com.versionone.apiclient.*;

/**
*/

public class TeamModder{
	private static V1Connector connector;

	public TeamModder(){
		try{
			connector = V1Connector
				.withInstanceUrl("https://www8.v1host.com/ParishSOFTLLC/")
				.withUserAgentHeader("TeamModder", "1.0")
				.withAccessToken("1.bpCz5yA+H0uMvuMlCJwTlh+5dAA=")
				.build();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/*
	*/
	public static void main(String[] arguments){
		new TeamModder();
		System.out.println("Connector = " + connector);
	}

}