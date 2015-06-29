package com.billhorvath.v1mods;

import com.versionone.*;
import com.versionone.apiclient.*;
import com.versionone.apiclient.services.*;
import com.versionone.apiclient.interfaces.*;


/**
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
		boolean result = modder.addToTeam("10326", "Test Team");
		
		System.out.println("result = " + result);
		
	}
	/*
	*/
	public static TeamModder getInstance(){
		return new TeamModder();
	}
	/*
	Adds the Member identified by memberOid to the team called teamName.
	
	@return true if the member was successfully added to the team and the results were saved; false otherwise.
	*/
	private boolean addToTeam(String memberOid, String teamName){
	
		assert teamName != null;
		assert memberOid != null;
		
		boolean result = false;
		IAssetType assetType = services.getMeta().getAssetType("Team");
		Query query = new Query(assetType);
		IAttributeDefinition nameAttr = assetType.getAttributeDefinition("Name");
		query.getSelection().add(nameAttr);
		
		try{
			QueryResult queryResult = services.retrieve(query);
			Asset targetTeam = null;
			
			for (Asset team: queryResult.getAssets()){
				String str = team.getAttribute(nameAttr).getValue().toString();
				assert str != null;
				if (teamName.equals(str)){
					targetTeam = team;
					break;
				}
			}
			
			IAttributeDefinition membersAttr = assetType.getAttributeDefinition(
				"CapacityExcludedMembers");
			targetTeam.addAttributeValue(membersAttr, "Member:" + memberOid);
			services.save(targetTeam);
			result = true;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
		
	}
	
	/*
	Returns a String representation of the members attached to this VersionOne installation.
	*/
	public String memberList(){
		IAssetType assetType = services.getMeta().getAssetType("Member");
		Query query = new Query(assetType);
		IAttributeDefinition nameAttr = assetType.getAttributeDefinition("Name");
		query.getSelection().add(nameAttr);
		String result = "VersionOne Members:"; 
		try{
			QueryResult queryResult = services.retrieve(query);
		
			for (Asset member: queryResult.getAssets()){
				result += "\n\tNumber: " + member.getOid().getToken() 
					+ "\tName: " + member.getAttribute(nameAttr).getValue();
			}
		}
		catch(Exception e){
			e.printStackTrace();
			result += " ERROR QUERYING FOR MEMBERS";
		}
		return result;
	}
	
	/*
	*/
	@Override
	public String toString(){
		return "TeamModder:\n\tservices: " + services;
	}
}