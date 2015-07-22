package com.billhorvath.v1mods;

import java.util.*;
import com.versionone.*;
import com.versionone.apiclient.*;
import com.versionone.apiclient.services.*;
import com.versionone.apiclient.interfaces.*;

/**
	A collection of utility methods for operating on the VersionOne API.
*/

class V1Utils{
	
// 	private static final IServices SERVICES =
// 		V1Services.getInstance().services();
	/**
	*/
	
	V1Utils(){}
	/**
	Returns a List of Assets of the assetTypeStr flavor and with attributeNames attributes attached.
	*/
	static List<Asset> findAssets(String assetTypeStr, String...attributeNames){

		IServices services = V1Services.getInstance().services();
		List<Asset> result = null;
		
		IAssetType assetType = services.getMeta().getAssetType(assetTypeStr);
		Query query = new Query(assetType);

		for (String attributeName: attributeNames){
			IAttributeDefinition attribute = 
				assetType.getAttributeDefinition(attributeName);
			query.getSelection().add(attribute);
		}
		try{
			QueryResult queryResult = services.retrieve(query);
			result = Arrays.asList(queryResult.getAssets());
		}
		catch(Exception e){
			e.printStackTrace();
			assert false;
		}
		return result;
	}
	/**
	*/
	static IAttributeDefinition getAttribute(String assetTypeStr, 
		String attrName){

			IServices services = V1Services.getInstance().services();
			IAttributeDefinition attributeDef =
				services.getMeta().getAssetType(assetTypeStr)
					.getAttributeDefinition(attrName);
			return attributeDef;
	}
}

