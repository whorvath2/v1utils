package com.billhorvath.v1mods;

import java.util.*;
import com.versionone.*;
import com.versionone.apiclient.*;
import com.versionone.apiclient.services.*;
import com.versionone.apiclient.interfaces.*;

/**
*/
public class BacklogPuller{

	private static BacklogPuller instance;
	public enum BacklogType {
		PRODUCT, CURRENT_SPRINT
	}
	
	/**
	*/
	public static void main(String[] args){
		//for now, we're ignoring the parameters...
		List<String> items = getInstance().pullBacklog();
		System.out.println("Backlog Items...\n");
		for (String item: items){
			System.out.println("\t" + item + "\n");
		}
		System.exit(0);	
	}
	
	/**
	*/
	public static BacklogPuller getInstance(){
		if (instance == null) instance = new BacklogPuller();
		return instance;
	}

	/**
	*/
	public List<String> pullBacklog(){
		return pullBacklog(BacklogType.PRODUCT);
	}

	/**
		//TO-DO: Include Defects; only include items that are to-do or in process
	*/
	public List<String> pullBacklog(BacklogType type){

// 		List<Asset> assets = V1Utils.findAssets("Story", "Name", "Scope", "Estimate");
		List<Asset> assets = V1Utils.findAssets("Story", "Name", "Estimate");
		
		List<String> result = new ArrayList<String>(assets.size());
		
		IAttributeDefinition nameDef = V1Utils.getAttribute("Story", "Name");
// 		IAttributeDefinition scopeDef = V1Utils.getAttribute("Story", "Scope");
		IAttributeDefinition estDef = V1Utils.getAttribute("Story", "Estimate");
		
		for (Asset asset : assets){
			String str = attributeToString(asset, nameDef) + "\n\t"
// 				+ attributeToString(asset, scopeDef) + " "
				+ attributeToString(asset, estDef);
			result.add(str);
		}
		return result;
	}

	private String attributeToString(Asset asset, IAttributeDefinition def){
// 		String result = def.getDisplayName() + ": ";
		String result = def.getName() + ": ";
		Attribute attribute = asset.getAttribute(def);
		if (attribute != null){
			try{
				Object value = attribute.getValue();
				if (value != null){
					result += value.toString();
				}
				else result += "(N/A)";
			}
			catch(Exception e){
				assert false;
				e.printStackTrace();
				result += "ERROR!";
			}
		}
		return result;
	}
}