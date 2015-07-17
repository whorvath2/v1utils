package com.billhorvath.v1mods;

import java.io.*;
import java.net.*;
import com.versionone.*;
import com.versionone.apiclient.*;
import com.versionone.apiclient.services.*;
import com.versionone.apiclient.interfaces.*;


/**
	The purpose of this class is to allow modification of the membership of 
	VersionOne teams.
*/

public class TeamModder{

	private final IServices services;
	
	private TeamModder(){
		this.services = V1Services.getInstance().services();		
	}
	
	/**
	Allows this class to be run from the command line.
	@param arguments Command-line arguments. Ignored in this class.
	*/
	public static void main(String[] arguments){
		TeamModder modder = getInstance();
// 		boolean result = modder.addToTeam("10326", "Test Team");
		Asset team = modder.findTeam("Silver Bullet");
// 		Asset member = modder.findMember("Bill Horvath");
		System.out.println("Team = " + team);
// 		System.out.println("Member = " + member);	
	}
	
	/**
	Returns an instance of TeamModder.
	@return A default TeamModder instance.
	*/
	public static TeamModder getInstance(){
		return new TeamModder();
	}
	
	/**
	*/
	private Asset findMember(String name){

		Asset result = null;
		IAssetType assetType = services.getMeta().getAssetType("Member");
		Query query = new Query(assetType);
		IAttributeDefinition nameAttr = 
			assetType.getAttributeDefinition("Name");
		query.getSelection().add(nameAttr);
		
		try{
			QueryResult queryResult = services.retrieve(query);
			
			for (Asset member: queryResult.getAssets()){
				String str = member.getAttribute(nameAttr).getValue().toString();
				assert str != null;
				if (name.equals(str)){
					result = member;
					break;
				}
			}			
		}
		catch(Exception e){
			assert false;
			e.printStackTrace();
		}
		return result;
	}

	/**
	Finds the VersionOne Asset representing the team identified by teamName.
	@param teamName The name of the team to be retrieved.
	@return A VersionOne Asset representing the team.
	*/
	private Asset findTeam(String teamName){
	
		assert teamName != null;
		
		Asset result = null;
		IAssetType assetType = services.getMeta().getAssetType("Team");
		Query query = new Query(assetType);
		IAttributeDefinition nameAttr = 
			assetType.getAttributeDefinition("Name");
		query.getSelection().add(nameAttr);
		
		try{
			QueryResult queryResult = services.retrieve(query);
			
			for (Asset team: queryResult.getAssets()){
				String str = team.getAttribute(nameAttr).getValue().toString();
				assert str != null;
				if (teamName.equals(str)){
					result = team;
					break;
				}
			}			
		}
		catch(Exception e){
			assert false;
			e.printStackTrace();
		}
		return result;
		
	}
	
	
	/**
	Adds the Member identified by memberOid to the team called teamName.
	
	@param memberOid The string representation of the Oid for the 
		member to be added to the team.
	@param teamName The name of the team to which the member is to be added.	
	@return true if the member was successfully added to the team and the
		results were saved; false otherwise.
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
			assert false;
			e.printStackTrace();
		}
		return result;
		
	}
	
	/**
	Returns a String representation of the members attached to this
		VersionOne installation.
	@return A String representation of the members attached to this
		VersionOne installation.
	*/
	public String memberList(){
		IAssetType assetType = services.getMeta().getAssetType("Member");
		Query query = new Query(assetType);
		IAttributeDefinition nameAttr = 
			assetType.getAttributeDefinition("Name");
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
	
	/**
	Returns a human-readable form of the fields in this TeamModder.
	@return A human-readable form of the fields in this TeamModder.
	*/
	@Override
	public String toString(){
		return "TeamModder:\n\tservices: " + services;
	}
}
