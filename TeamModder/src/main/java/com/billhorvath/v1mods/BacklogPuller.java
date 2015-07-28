package com.billhorvath.v1mods;

import java.util.*;
import java.util.List;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import com.versionone.*;
import com.versionone.apiclient.*;
import com.versionone.apiclient.filters.*;
import com.versionone.apiclient.interfaces.*;

/**
<p>A class for pulling the Product Backlog from VersionOne and making it available in plain text.</p> 
<p>As presently constructed, this class queries all open stories and defects in a particular project, sorts them according to Rank, and outputs a file containing the results.</p>
<p>To-Do:</p><ul><li>Include Story Point Estimates in the output.</li><li>Mark the items being developed in the current Sprint.</li><li>Include items in the current Sprint that are closed.</li></ul>
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
		PROJECTID	= 	"Scope:91791"; //4.3.4
		
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
	Pulls the backlog and pushes it out to a file.
	@param args These are ignored at the moment.
	*/
	public static void main(String[] args){

		//for now, we're ignoring the parameters...
		List<String> items = getInstance().pullBacklog();
		String output = "Backlog Items...\n";
		for (String item: items){
			output += "\t" + item + "\n";
		}
		System.out.println(output);
		File file = marshallOut(output);
		if (file != null){
			try{
				Desktop.getDesktop().open(file);
			}
			catch(Exception e){
				e.printStackTrace();
				System.err.println("The backlog has been put here: " + file);
				System.exit(1);
			}
		}
		System.exit(0);	
	}
	
	/**
	Factory method for generating a BacklogPuller instance. Note that it currently returns a singleton.
	@return a BacklogPuller instance.
	*/
	public static BacklogPuller getInstance(){
		if (instance == null) instance = new BacklogPuller();
		return instance;
	}
	
	/**
	Writes <code>output</code> as plain-text (UTF-8) to a file at [current working directory]/Backlog.txt.
	@param output The string to be written out.
	@return A file containing the output, or null if there was an error creating or writing to the file.
	*/
	private static File marshallOut(String output){
		File result = null;
		Path path = null;
		try{
			path = FileSystems.getDefault().getPath("Backlog.txt");
			BufferedWriter writer = Files.newBufferedWriter(path);
			writer.write(output);
			writer.flush();
			result = path.toFile();
		}
		catch(Exception e){
			e.printStackTrace();
			System.err.println(
				"There was an error creating the backlog file at " + path);
		}
		return result;
	}
	
	/**
	@return A list of strings containing the names and ranks of the items in the backlog.
	@see BacklogPuller#pullBacklog(BacklogType)
	*/
	public List<String> pullBacklog(){
		return pullBacklog(BacklogType.PRODUCT);
	}

	/**
	Pulls the backlog of open PrimaryWorkItems (stories and defects) for the project designed by PROJECTID, and returns a list of Strings containing the names and ranks of the items in the backlog.
	@param type The type of backlog the client wishes to retrieve (this is currently ignored.)
	@return A list of strings containing the names and ranks of the items in the backlog.
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
		return result;
	}
	
	/**
	Constructs an IFilterTerm that will limit a VersionOne Query to items that are still open, and which are associated with the project designated by PROJECTID.
	@param assetType The name of the type of asset on which the filter will operate. Also see <a href="https://www8.v1host.com/ParishSOFTLLC/meta.v1?xsl=api.xsl">the VersionOne meta page.</a>
	@return an IFilterTerm that will limit a VersionOne Query to items that are still open, and which are associated with the project designated by PROJECTID.
	*/
	private IFilterTerm buildFilter(String assetType){

		IFilterTerm result = null;
		IAttributeDefinition closedDef = V1Utils.getAttribute(assetType, ISCLOSED);
		FilterTerm closedTerm = new FilterTerm(closedDef);
		closedTerm.equal(Boolean.FALSE);
		
		IAttributeDefinition projectDef = V1Utils.getAttribute(assetType, PROJECT);
		FilterTerm projectTerm = new FilterTerm(projectDef);
		try{
			Oid oid = V1Services.getInstance().services().getOid(PROJECTID);
			projectTerm.equal(oid);
			result = new AndFilterTerm(closedTerm, projectTerm);
		}
		catch(Exception e){
			e.printStackTrace();
			System.err.println("Unable to pull the Oid of the project.");
		}
		return result;
	}
	
	/**
	Calculates a String to represent the value of the <code>def</code> attribute of <code>asset</code>.
	@param asset The asset which will be examined for the attribute defined by def.
	@param def The attribute of the asset from which the value will be extracted.
	@return a String to represent the value of the <code>def</code> attribute of <code>asset</code>.
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