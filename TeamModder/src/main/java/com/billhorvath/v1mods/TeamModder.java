package com.billhorvath.v1mods;

import com.versionone.*;
import com.versionone.apiclient.*;
import com.versionone.apiclient.services.*;
import com.versionone.apiclient.interfaces.*;


/**
 * The purpose of this class is to allow modification of VersionOne teams by way of the API.
*/

public class TeamModder{
	private final Services services;

	public TeamModder(){
		V1Connector connector = null;
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
		assert connector != null;
		
		this.services = new Services(connector);
		
	}
	
	/*
	*/
	public static void main(String[] arguments){
		TeamModder modder = getInstance();
		System.out.println(modder.memberList());
		
	}
	/*
	Returns an instance of TeamModder.
	@return A default TeamModder instance.
	*/
	public static TeamModder getInstance(){
		return new TeamModder();
	}
	/*
	Returns a String representation of the members attached to this VersionOne installation.
	@return A String representation of the members attached to this VersionOne installation.
	*/
	public String memberList(){
		IAssetType assetType = services.getMeta().getAssetType("Member");
		Query query = new Query(assetType);
		IAttributeDefinition nameAttr = assetType.getAttributeDefinition("Name");
		query.getSelection().add(nameAttr);
		String str = "VersionOne Members:"; 
		try{
			QueryResult result = services.retrieve(query);
		
			for (Asset member: result.getAssets()){
				str += "\n\tNumber: " + member.getOid().getToken() 
					+ "\tName: " + member.getAttribute(nameAttr).getValue();
			}
		}
		catch(Exception e){
			e.printStackTrace();
			str += " ERROR QUERYING FOR MEMBERS";
		}
		return str;
	}
	
	/*
	Returns a human-readable form of the fields in this TeamModder.
	@return A human-readable form of the fields in this TeamModder.
	*/
	@Override
	public String toString(){
		return "TeamModder:\n\tservices: " + services;
	}
}
