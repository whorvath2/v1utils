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
	@param assetTypeStr The name of the type of asset being sought.
	@param attributeNames The attributes which should be attached to each of the Assets in the returned list.
	@return a List of Assets of the assetTypeStr flavor and with attributeNames attributes attached. If assetTypeStr is null, or if any of attributeNames is null, the list will be empty.
	*/
	static List<Asset> findAssets(String assetTypeStr, String...attributeNames){

		return findAssets(assetTypeStr, (IFilterTerm)null, attributeNames);
	}
	/**
	Returns a List of Assets of the assetTypeStr flavor and with attributeNames attributes attached, with those matching <code>term</code> filtered into (or out of) the results.
	@param assetTypeStr The name of the type of asset being sought.
	@param term The filter to be applied in the search.	May be null.
	@param attributeNames The attributes which should be attached to each of the Assets in the returned list.
	@return a List of Assets of the assetTypeStr flavor that have been filtered according to term, with attributeNames attributes attached.
	*/
	static List<Asset> findAssets(String assetTypeStr, IFilterTerm term, String...attributeNames){

		if (assetTypeStr == null){
			System.err.println("You must supply a non-null value for the assetTypeStr parameter.");
			return new ArrayList<Asset>();
		}
		for (String attributeName : attributeNames){
			if (attributeName == null){
				System.err.println("You must supply a non-null value for each of the attributeNames parameters.");
				return new ArrayList<Asset>();
			}
		}
	
		Query query = buildQuery(assetTypeStr);
		if (query == null){
			System.err.println("Assets of type " + assetTypeStr + " do not exist! Returned list will contain no objects so it will fail gracefully.");
			return new ArrayList<Asset>();
		}
		if (term != null){
			query.setFilter(term);
		}
		return findAssets(query, assetTypeStr, attributeNames);
	}
	/**
	Returns a List of Assets which meet the criteria of query, are of the assetTypeStr flavor, and which have attributes named attributeNames attached.
	@param query The query used to filter the list of assets returned according to specific criteria.
	@param assetTypeStr The name of the type of asset being sought.
	@param attributeNames The names of the attributes which should be attached to each of the assets in the returned list.
	@return a List of Assets which meet the criteria of query, are of the assetTypeStr flavor, and which have attributes named attributeNames attached.

	*/
	static List<Asset> findAssets(Query query, String assetTypeStr, String...attributeNames){

		IServices services = V1Services.getInstance().services();
		List<Asset> result = new ArrayList<Asset>();
		
		for (String attributeName: attributeNames){
			IAttributeDefinition attribute = 
				getAttribute(assetTypeStr, attributeName);
			if (attribute == null){
				System.err.println("There is no attribute named " + attributeName + " associated with " + assetTypeStr + " assets.");
				return result;
			}
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
	Builds a Query that finds assets of type assetTypeStr.
	@param assetTypeStr The name of the type of asset being sought.
	@return a Query that finds assets of type assetTypeStr.
	*/
	static Query buildQuery(String assetTypeStr){
		Query result = null;
		IServices services = V1Services.getInstance().services();		
		IAssetType assetType = assetType(assetTypeStr);
		if (assetType != null){
			result = new Query(assetType);
		}
		return result;
	}
	
	/**
	Returns an IAssetType of the assetTypeStr flavor.
	@param assetTypeStr The name of the type of asset being sought.
	@return an IAssetType of the assetTypeStr flavor, or null if the asset type is not known.
	*/
	static IAssetType assetType(String assetTypeStr){
		IServices services = V1Services.getInstance().services();
		IAssetType result = null;
		try{
			result = services.getMeta().getAssetType(assetTypeStr);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	/**
	Returns an IAttributeDefinition which defines an attribute that is of Assets of type assetTypeStr, and which is named attrName.
	@param assetTypeStr The name of the type of asset being sought.
	@param attrName The name of the attribute definition to be returned.
	@return an IAttributeDefinition which is an attribute of Assets of type assetTypeStr and which is named attrName, or null if either parameter is null.
	*/
	static IAttributeDefinition getAttribute(String assetTypeStr, 
		String attrName){
			IAttributeDefinition result = null;
			if (assetTypeStr == null){
				System.err.println("You must supply a non-null value for the assetTypeStr parameter.");
				return null;
			}
			if (attrName == null){
				System.err.println("You must supply a non-null value for the attributeName parameter.");
				return null;
			}

			IAssetType assetType = assetType(assetTypeStr);
			if (assetType != null){
				try{
					result = assetType.getAttributeDefinition(attrName);
				}
				catch(Exception e){
					System.err.println(attrName + " is not an attribute of " + assetTypeStr);
// 					e.printStackTrace();
				}
			}
			else{
				System.err.println("There is no asset type named " + assetTypeStr);
			}
			return result;
	}

	/**
	Returns an asset which is identified by oidStr.
	@param oidStr The unique identifier for the asset being sought.
	@return an asset which is uniquely identified by oidStr.
	*/
	static Asset findAssetByOid(String oidStr){
			Asset result = null;
			IServices services = V1Services.getInstance().services();
			try{
				Oid oid = services.getOid(oidStr);
				Query query = new Query(oid);
				QueryResult queryResult = services.retrieve(query);
				if (queryResult.getAssets().length == 1){
					result = queryResult.getAssets()[0];
				}
				else {
					throw new Exception("Unable to find any assets by Oid " + oidStr);
				}
			}
			catch(Exception e){
				System.err.println("Unable to locate an Asset by Oid " + oidStr + "; This Oid is most likely bad or unknown.");
				e.printStackTrace();
			}
			return result;
	}
}

