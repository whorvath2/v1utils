package com.billhorvath.v1mods;

import java.util.*;
import com.versionone.*;
import com.versionone.apiclient.*;
import com.versionone.apiclient.filters.*;
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
		Query query = buildQuery(assetTypeStr);
		return findAssets(query, assetTypeStr, attributeNames);
	}
	/**
	Returns a List of Assets of the assetTypeStr flavor and with attributeNames attributes attached.
	*/
	static List<Asset> findAssets(String assetTypeStr, IFilterTerm term, String...attributeNames){
	
		Query query = buildQuery(assetTypeStr);
		query.setFilter(term);		
		return findAssets(query, assetTypeStr, attributeNames);
	}
	/**/
	static Query buildQuery(String assetTypeStr){
		IServices services = V1Services.getInstance().services();		
		IAssetType assetType = assetType(assetTypeStr);
		Query query = new Query(assetType);
		return query;
	}
	/**/
	static IAssetType assetType(String assetTypeStr){
		IServices services = V1Services.getInstance().services();
		return services.getMeta().getAssetType(assetTypeStr);
	}
	/**
	*/
	static IAttributeDefinition getAttribute(String assetTypeStr, 
		String attrName){

			IAttributeDefinition attributeDef = assetType(assetTypeStr)
					.getAttributeDefinition(attrName);
			return attributeDef;
	}
	/**
	*/
	static List<Asset> findAssets(Query query, String assetTypeStr, String...attributeNames){

		IServices services = V1Services.getInstance().services();
		List<Asset> result = new ArrayList<Asset>();
		
		for (String attributeName: attributeNames){
			IAttributeDefinition attribute = 
				getAttribute(assetTypeStr, attributeName);
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
	static Asset findAssetByOid(String oidStr){
			Asset result = null;
			IServices services = V1Services.getInstance().services();
			try{
				Oid oid = services.getOid(oidStr);
				Query query = new Query(oid);
				QueryResult queryResult = services.retrieve(query);
				assert queryResult.getAssets().length == 1;
				result = queryResult.getAssets()[0];
			}
			catch(Exception e){
				e.printStackTrace();
				assert false;
			}
			return result;
	}
}

