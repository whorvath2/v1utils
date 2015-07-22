package com.billhorvath.v1mods;

import java.util.*;
import java.math.*;
import com.versionone.*;
import com.versionone.apiclient.*;
import com.versionone.apiclient.services.*;
import com.versionone.apiclient.interfaces.*;

/**
	This class hasn't been written yet.
*/
public class EstimatesRounder{
	private final IServices services;

	/**
	*/
	private EstimatesRounder(){
		this.services = V1Services.getInstance().services();
	}
	/**
	*/
	public static void main(String[] args){
		Scanner scanner = new Scanner(System.in);
		System.out.print("This program will iterate through the entire VersionOne database looking for Tasks and Tests, and round any that have partial-hour estimates of n.x hours up to n+1. Continue? ");
		String yesNo = scanner.next();
		if (yesNo == null || yesNo.isEmpty()){
			System.out.println("Oh, fine...Exiting now...");
			System.exit(0);
		}
		yesNo = yesNo.trim();
		if (!(yesNo.equalsIgnoreCase("yes") || yesNo.equalsIgnoreCase("y"))){
			System.out.println("Whatever you say, boss...Exiting now...");
			System.exit(0);
		}
		else{
			EstimatesRounder rounder = getInstance();
			rounder.round("Task");
			rounder.round("Test");
		}
		System.out.println("Exiting...");
		System.exit(0);
	}
	/**
	*/
	public static EstimatesRounder getInstance(){
		return new EstimatesRounder();
	}
	/**
	Iterates through the assets of type assetTypeStr, and checks if they have a DetailEstimate attribute. If so, the value of the DetailEstimate is acquired, and checked to see if it is a non-integer. If so, it is rounded up to the next highest integer (regardless of the fractional value) and set as the value of the DetailEstimate attribute. All of the changed assets are then written back out to the database.
	*/
	private void round(String assetTypeStr){
// 		IAssetType assetType = services.getMeta().getAssetType(assetTypeStr);
// 		Query query = new Query(assetType);
// 		IAttributeDefinition estAttr = 
// 			assetType.getAttributeDefinition("DetailEstimate");
// 		query.getSelection().add(estAttr);

		String attributeName = "DetailEstimate";
		List<Asset> matches = V1Utils.findAssets(assetTypeStr,
			attributeName);
		
		try{
// 			QueryResult queryResult = services.retrieve(query);
// 			IAttributeDefinition estAttr =
// 				services.getMeta().getAssetType(assetTypeStr)
// 					.getAttributeDefinition(attributeName);
			IAttributeDefinition estAttr = V1Utils.getAttribute(assetTypeStr, attributeName);

			List<Asset> assetsToChange = new ArrayList<Asset>();
			
// 			for (Asset match: queryResult.getAssets()){
			for (Asset match: matches){
				Attribute attribute = match.getAttribute(estAttr);
				if (attribute == null) continue;
				Object value = attribute.getValue();
				if (value == null) continue;
				String str = value.toString();
				assert str != null;
				if (str != null){
					try{
						double d = Double.parseDouble(str);
						if ((d % 1) > 0){
// 							System.out.print("Oid = " + match.getOid() + "\tEstimate = " + str);
							d = Math.ceil(d);
// 							System.out.println("\tRounded = " 
// 								+ String.valueOf(d));
							match.setAttributeValue(estAttr, String.valueOf(d));
							assetsToChange.add(match);
						}
					}
					catch(NumberFormatException e){
						assert false;
						System.out.println("Great Googly Moogly! Bad data!");
						continue;
					}
				}
			}
			Asset[] newAssets = assetsToChange.toArray(new Asset[0]);
// 			services.save(newAssets);			
		}
		catch(Exception e){
			assert false;
			e.printStackTrace();
		}
	}
}