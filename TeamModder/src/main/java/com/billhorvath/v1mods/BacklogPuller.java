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
		PRODUCT, SPRINT
	}
	
	private static final String
		USAGE		=	"Informative message on how to use this program. TO-DO",
		NAME 		= 	"Name",
		ID			=	"ID",
		NUMBER		= 	"Number",
		ISCLOSED	= 	"IsClosed",
		ORDER		= 	"Order",
		PROJECT 	= 	"Scope",
		STORY 		=	"Story",
		DEFECT		=	"Defect",
		NA 			= 	"(N/A)",
		SPRINT		=	"Timebox",
//		PROJECTID	= 	"Scope:91791"; //4.3.4
// 		PROJECTID	= 	"Scope:108271"; //Accounting/CNA Release 2016 - 1.00
		PROJECTID	= 	"Scope:0"; //System (All Projects)
		
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
		List<String> items = new ArrayList<String>();
		
		if (args == null || args.length == 0){
			items = getInstance().pullBacklog();
		}
		else{
			String project = args[0];
			if (project == null || project.matches("\\s*")){
				System.out.println(USAGE);
			}
			String sprint = null;
			if (args.length == 2){
				sprint = args[1];
			}
			items = getInstance().pullBacklog(project, sprint);
				
		}
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
	Writes <code>output</code> as plain-text (UTF-8) to a file at [current working directory]/Backlog.html.
	@param output The string to be written out.
	@return A file containing the output, or null if there was an error creating or writing to the file.
	*/
	private static File marshallOut(String output){
		File result = null;
		Path path = null;
		try{
			path = FileSystems.getDefault().getPath("Backlog.html");
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
	Delegates to pullBacklog(String projectID), with projectID being the default value PROJECTID.
	
	@see BacklogPuller#pullBacklog(String projectID)
	@return A list of strings containing the names and ranks of the items in the backlog.
	*/
	public List<String> pullBacklog(){
		return pullBacklog(PROJECTID);
	}

	/**
	Delegates to pullBacklog(String projectID, String sprintID), with sprintID being null.
	@see BacklogPuller#pullBacklog(String projectID, String sprintID)
	@param projectID The VersionOne Oid for the desired project.
	@return A list of strings containing the names and ranks of the items in the backlog.
	**/
	public List<String> pullBacklog(String projectID){
		return pullBacklog(projectID, (String)null);
	}
	/**
	Delegates to pullBacklog(BacklogType type, String projectID, String sprintID), with type being the default value BacklogType.PRODUCT.
	@see BacklogPuller#pullBacklog(BacklogType type, String projectID, String sprintID)
	@param projectID The VersionOne Oid for the desired project.
	@param sprintID The VersionOne Oid for the desired sprint.
	@return A list of strings containing the names and ranks of the items in the backlog.
	**/
	public List<String> pullBacklog(String projectID, String sprintID){
		return pullBacklog(BacklogType.PRODUCT, projectID, sprintID);
	}

	/**
	Pulls the backlog of open PrimaryWorkItems (stories and defects) for the project designated by projectID, and (optionally) the sprint designated by sprintID, and returns a list of Strings containing the names and ranks of the items in the backlog.
	@param type The type of backlog the client wishes to retrieve (this is currently ignored.)
	@param projectID The VersionOne Oid for the desired project.
	@param sprintID The VersionOne Oid for the desired sprint.
	@return A list of strings containing the names and ranks of the items in the backlog.
	*/
	public List<String> pullBacklog(BacklogType type, String projectID, String sprintID){

		assert projectID != null;
		assert type != null;

		final String[] attStrs = {NAME, ISCLOSED, ORDER, ID, NUMBER};

		IFilterTerm groupTerm = buildFilter(STORY, projectID, sprintID);
		
		List<Asset> assets = 
			new ArrayList<Asset>(V1Utils.findAssets(STORY, groupTerm, attStrs));
			
		groupTerm = buildFilter(DEFECT, projectID, sprintID);
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

					return new Long(appleStr).compareTo(new Long(orangeStr));
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

		final IAttributeDefinition storyIDAttDef = 
			V1Utils.getAttribute(STORY, ID);
		final IAttributeDefinition defectIDAttDef = 
			V1Utils.getAttribute(DEFECT, ID);

		final IAttributeDefinition storyNumAttDef = 
			V1Utils.getAttribute(STORY, NUMBER);			
		final IAttributeDefinition defectNumAttDef = 
			V1Utils.getAttribute(DEFECT, NUMBER);

		final IAssetType storyType = V1Utils.assetType(STORY);

		List<String> result = new ArrayList<String>(assets.size() + 4);
		result.add("<html><body><ol>");
		
		int i = 0;
		String name, id, num, url = null;
		String baseUrlStory = "<a href=\"https://www8.v1host.com/ParishSOFTLLC/story.mvc/Summary?oidToken=";
		String baseUrlDefect = "<a href=\"https://www8.v1host.com/ParishSOFTLLC/defect.mvc/Summary?oidToken=";
		String midLink = "\">";
		String endLink = "</a>";
		for (Asset asset : assets){
			boolean isStory = asset.getAssetType().equals(storyType);
			IAttributeDefinition attDef = (isStory)
				? storyNameAttDef
				: defectNameAttDef;
			name = attributeToString(asset, attDef);

			attDef = (isStory)
				? storyNumAttDef
				: defectNumAttDef;
			num = attributeToString(asset, attDef);

			attDef = (isStory)
				? storyIDAttDef
				: defectIDAttDef;
			id = attributeToString(asset, attDef);
			String[] idArr = id.split(":",2);
			String baseUrl = (isStory)
			? baseUrlStory
			: baseUrlDefect;
			
			String startLink = baseUrl + idArr[0] + "%3A" + idArr[1] + midLink;
			result.add(
			"<li>"
// 			+ String.valueOf(++i) 
// 			+ ": " 
			+ startLink
			+ num 
			+ endLink
			+ "\t" 
			+ name
			+ "</li>");
		}
		result.add("</ol></body></html>");
		return result;
	}
	
	/**
	Delegates to buildFilter(String assetType, String projectID) and uses the constant PROJECTID as the (default) value for the projectID parameter.
	
	@see BacklogPuller#buildFilter(String assetType, String projectID)
	@param assetType The name of the type of asset on which the filter will operate. Also see <a href="https://www8.v1host.com/ParishSOFTLLC/meta.v1?xsl=api.xsl">the VersionOne meta page.</a>
	@return an IFilterTerm that will limit a VersionOne Query to items that are still open, and which are associated with the project designated by the default value of PROJECTID, a constant of this class.
	*/
	private IFilterTerm buildFilter(String assetType){

		return buildFilter(assetType, PROJECTID);
	}
	
	
	/**
	Delegates to buildFilter(String assetType, String projectID, String sprintID), and uses null as the (default) value for the sprintID parameter.
	
	@see BacklogPuller#buildFilter(String assetType, String projectID, String sprintID)
	@param assetType The name of the type of asset on which the filter will operate. Also see <a href="https://www8.v1host.com/ParishSOFTLLC/meta.v1?xsl=api.xsl">the VersionOne meta page.</a>
	@param projectID The plain-text name of the unique identifier for a project, known in VersionOne as an Oid (Object identifier)

	@return an IFilterTerm that will limit a VersionOne Query to items that are still open, and which are associated with the project designated by the value of projectID.
	**/
	private IFilterTerm buildFilter(String assetType, String projectID){
		return buildFilter(assetType, projectID, (String)null);		
	}

	/**
	Constructs an IFilterTerm that will limit a VersionOne Query to items that are still open, and which are associated with the project designated by projectID and the sprint designated by sprintID.
	
	@param assetType The name of the type of asset on which the filter will operate. Also see <a href="https://www8.v1host.com/ParishSOFTLLC/meta.v1?xsl=api.xsl">the VersionOne meta page.</a>
	@param projectID The plain-text name of VersionOne's unique identifier for a project.
	@param sprintID The plain-text name of VersionOne's unique identifier for a Sprint. May be null.

	@return an IFilterTerm that will limit a VersionOne Query to items that are still open, and which are associated with the project designated by projectID and the sprint designated by sprintID. If assetType or projectID are null, or if assetType, projectID, or sprintID are not recognized by VersionOne, this method <i>may</i> return null.
	**/
	private IFilterTerm buildFilter(String assetType, String projectID, 
		String sprintID){

		IFilterTerm result = null;
		IAttributeDefinition closedDef = V1Utils.getAttribute(assetType, ISCLOSED);
		FilterTerm closedTerm = new FilterTerm(closedDef);
		closedTerm.equal(Boolean.FALSE);
		
		IAttributeDefinition projectDef = V1Utils.getAttribute(assetType, PROJECT);
		FilterTerm projectTerm = new FilterTerm(projectDef);

		FilterTerm sprintTerm = null;
		if (sprintID != null && !(sprintID.equals(""))){
			IAttributeDefinition sprintDef = V1Utils.getAttribute(assetType, SPRINT);
			sprintTerm = new FilterTerm(sprintDef);
			sprintTerm.equal(sprintID);
		}
		
		try{
			Oid oid = V1Services.getInstance().services().getOid(projectID);
			projectTerm.equal(oid);
			
			result = (sprintTerm != null)
			? new AndFilterTerm(closedTerm, projectTerm, sprintTerm)
			: new AndFilterTerm(closedTerm, projectTerm);
		}
		catch(Exception e){
			e.printStackTrace();
			System.err.println("Unable to construct the IFilterTerm.");
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
							else {
								result += NA;
							}
							result += ", ";
						}
						result = result.substring(0, result.length() - 2);
					}
					else{
						String str = value.toString();
						if (str.equals("NULL")){
							str = NA;
						}
// 						else if (str.matches("[A-Za-z]+:[0-9]+")){
// 							String number = str.split(":")[1];
// 							str = STORY_STATUSES.get(number);
// 						}
						result += str;
					}
				}
				else {
					result += NA;
				}
			}
			catch(Exception e){
				assert false;
				e.printStackTrace();
				result += "ERROR!";
			}
		}
		else{
			System.err.println("No value for attribute " + def.getName());
			String str = "Available assets include...\n";
			Map<String, Attribute> attributes = asset.getAttributes();
			Set<String> attNames = attributes.keySet();
			for (String attName : attNames){
				try{
					str += "\t" + attName + ": " + attributes.get(attName).getValue() + "\n";
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
			System.err.println(str);
		 	result += NA;
		}
		return result;
	}
}