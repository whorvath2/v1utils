package com.billhorvath.v1mods;

import java.util.*;
import com.versionone.*;
import com.versionone.apiclient.*;
import com.versionone.apiclient.filters.*;
// import com.versionone.apiclient.services.*;
import com.versionone.apiclient.interfaces.*;

/**
*/
public class BacklogPuller{

	private static BacklogPuller instance;
	public enum BacklogType {
		PRODUCT, CURRENT_SPRINT
	}
	
	private static final String
		NAME 		= 	"Name",
		ISCLOSED	= 	"IsClosed",
		ORDER		= 	"Order",
		PROJECT 	= 	"Scope",
		STORY 		=	"Story",
		DEFECT		=	"Defect",
		NA 			= 	"(N/A)",
		PROJECTID	= 	"Scope:91791";
		
// 	private static final Map<String, String> STORY_STATUSES = 
// 		new HashMap<String, String>();
// 
// 	static{
// 	
// 		String assetTypeStr = "StoryStatus";
// 		List<Asset> assets = V1Utils.findAssets(assetTypeStr, "Name");
// 		for (Asset asset : assets){
// 			IAttributeDefinition attDef = 
// 				V1Utils.getAttribute(assetTypeStr, "Name");
// 			Attribute attribute = asset.getAttribute(attDef);
// 			if (attribute != null){
// 				try{
// 					String attStr = attribute.getValue().toString();
// 					String attOidStr = asset.getOid().toString().split(":")[1];
// 					STORY_STATUSES.put(attOidStr, attStr);
// 				}
// 				catch(Exception e){
// 					assert false;
// 					e.printStackTrace();
// 					throw new IllegalStateException();
// 				}
// 			}
// 		}
// 	}
	
	/**
	*/
	public static void main(String[] args){
		//for now, we're ignoring the parameters...
		List<String> items = getInstance().pullBacklog();
		System.out.println("Backlog Items...\n");
		for (String item: items){
			System.out.println("\t" + item);
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

		final String[] attStrs = {NAME, ISCLOSED, ORDER};

		IFilterTerm groupTerm = buildFilter(STORY);
		
		List<Asset> assets = 
			new ArrayList<Asset>(V1Utils.findAssets(STORY, groupTerm, attStrs));
			
		groupTerm = buildFilter(DEFECT);
		assets.addAll(V1Utils.findAssets(DEFECT, groupTerm, attStrs));

		final IAttributeDefinition storyOrderAttDef = 
			V1Utils.getAttribute(STORY, ORDER);
			
		final IAttributeDefinition defectOrderAttDef = 
			V1Utils.getAttribute(DEFECT, ORDER);
		
		Collections.sort(assets, new Comparator<Asset>(){
		
			public int compare(Asset apple, Asset orange){
				
				try{
					IAssetType storyType = V1Utils.assetType(STORY);
					String appleStr = (apple.getAssetType().equals(storyType))
						? (String)apple.getAttribute(
							storyOrderAttDef).getValue()
						: (String)apple.getAttribute(
							defectOrderAttDef).getValue();
				
					String orangeStr =(orange.getAssetType().equals(storyType))
						? (String)orange.getAttribute(
							storyOrderAttDef).getValue()
						: (String)orange.getAttribute(
							defectOrderAttDef).getValue();
					return -1 * (new Long(appleStr).compareTo(new Long(orangeStr)));
				}
				catch(Exception e){
					e.printStackTrace();
					assert false;
				}
				return 0;
			}
			public boolean equals(Object obj){
				return this == obj;
			}
		});
				

		final IAttributeDefinition storyNameAttDef = 
			V1Utils.getAttribute(STORY, NAME);
			
		final IAttributeDefinition defectNameAttDef = 
			V1Utils.getAttribute(DEFECT, NAME);


		List<String> result = new ArrayList<String>(assets.size());
		int i = 0;
		for (Asset asset : assets){
			IAssetType storyType = V1Utils.assetType(STORY);
			IAttributeDefinition attDef = (asset.getAssetType().equals(storyType))
				? storyNameAttDef
				: defectNameAttDef;
			result.add(String.valueOf(++i) + ": " + attributeToString(asset, attDef));
		}
// 		for (Asset asset : assets){
// 			String str = "";
// 			for (String attStr : attStrs){
// 				IAttributeDefinition attDef = V1Utils.getAttribute(STORY, attStr);
// 				str += attributeToString(asset, attDef) + "\n\t";
// 			}
// 			result.add(str.trim());
// 		}
		return result;
	}
	
	/**/
	private IFilterTerm buildFilter(String assetType){
		IAttributeDefinition closedDef = V1Utils.getAttribute(assetType, ISCLOSED);
		FilterTerm closedTerm = new FilterTerm(closedDef);
		closedTerm.equal(Boolean.FALSE);
		
		IAttributeDefinition projectDef = V1Utils.getAttribute(assetType, PROJECT);
		FilterTerm projectTerm = new FilterTerm(projectDef);
		try{
			Oid oid = V1Services.getInstance().services().getOid(PROJECTID);
			projectTerm.equal(oid);
		}
		catch(Exception e){
			e.printStackTrace();
			assert false;
			throw new IllegalStateException("Unable to pull the Oid of the project.");
		}
		return new AndFilterTerm(closedTerm, projectTerm);
	}
	
	/**
	Calculates a String to represent the name and value of the <code>def</code> attribute of <code>asset</code>.
	@param asset The asset which will be examined for the attribute defined by def.
	@
	*/
	private String attributeToString(Asset asset, IAttributeDefinition def){
// 		String result = def.getName() + ": ";
		String result = "";
		Attribute attribute = asset.getAttribute(def);
		if (attribute != null){
			try{
				Object value = attribute.getValue();
				if (value != null){
					if (value instanceof Object[]){
						Object[] values = (Object[])value;
						for (Object obj : values){
							if (obj != null){
								result += obj.toString();
							}
							else result += NA;
							result += ", ";
						}
						result = result.substring(0, result.length() - 2);
					}
					else{
						String str = value.toString();
						if (str.equals("NULL")){
							str = NA;
						}
						else if (str.matches("[A-Za-z]+:[0-9]+")){
// 							String number = str.split(":")[1];
// 							str = STORY_STATUSES.get(number);
						}
						result += str;
					}
				}
				else result += NA;
			}
			catch(Exception e){
				assert false;
				e.printStackTrace();
				result += "ERROR!";
			}
		}
		else result += NA;
		return result;
	}
}