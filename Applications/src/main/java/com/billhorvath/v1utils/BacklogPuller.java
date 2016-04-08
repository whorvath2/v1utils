package com.billhorvath.v1utils;

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
<p>A class for pulling the Product Backlog from VersionOne and making it available in HTML format.</p> 
<p>As presently constructed, this class queries all open stories and defects in a particular project, sorts them according to Rank, and outputs a file containing the results.</p>
<p><b>IMPORTANT:</b> You must modify the BASE_URL_STORY and BASE_URL_DEFECT string constants to match your particular VersionOne instance.</p>
<p>To-Do:</p><ul><li>Include Story Point Estimates in the output.</li><li>[Alt. Done] Mark the items being developed in the current Sprint.</li><li>[Partially completed] Include items in the current Sprint that are closed.</li></ul>
*/
public class BacklogPuller{

	private static BacklogPuller instance;
	
	public enum BacklogType {
		PRODUCT, SPRINT
	}
	
	private static final String
		ANCHOR				= 	"<a href=\"" + V1Services.V1_LOC,
		BASE_URL_STORY		= 	ANCHOR + "story.mvc/Summary?oidToken=",
		BASE_URL_DEFECT		= 	ANCHOR + "defect.mvc/Summary?oidToken=",
		NAME 				= 	"Name",
		ID					=	"ID",
		NUMBER				= 	"Number",
		ISCLOSED			= 	"IsClosed",
		ORDER				= 	"Order",
		PROJECT 			= 	"Scope",
		STORY 				=	"Story",
		DEFECT				=	"Defect",
		NA					= 	"(N/A)",
		SPRINT				=	"Timebox",
		TEAM				=	"Team",
		PROJECTID			= 	"Scope:0", //System (All Projects)
		USAGE				=	"\nUsage: java -jar VersionOneInterface-1.5.jar [Scope:[projectOid]] [Timebox:[sprintOid]] [Team:[teamOid]] [IncludeClosed:[true|false]]\n\t...All parameters are optional, and may appear in any order.\n\n";
	
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
			List<String> argsList = Arrays.asList(args);
			String project = "";
			String sprint = "";
			String team = "";
			boolean includeClosed = false;
			
			for (String arg: argsList){
				arg = arg.trim();
				if (arg.matches("Scope:\\d+")){
					project = arg;
				}
				else if (arg.matches("Timebox:\\d+")){
					sprint = arg;
				}
				else if (arg.matches("Team:\\d+")){
					team = arg;
				}
				else if (arg.matches("IncludeClosed:(true|false)")){
					String str = arg.split(":")[1];
					includeClosed = str.equals("true");
				}
				else{
					System.out.println(USAGE);
					System.exit(1);
				}
			}
			
			//use the default project if it's not specified on the command line:
			if (project.equals("")) project = PROJECTID; 			

			items = getInstance().pullBacklog(project, sprint, team, includeClosed);
				
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
	Delegates to pullBacklog(String projectID, String sprintID, String teamID), with teamID being null.
	@see BacklogPuller#pullBacklog(String projectID, String sprintID, String teamID)
	@param projectID The VersionOne Oid for the desired project.
	@param sprintID The VersionOne Oid for the desired sprint.
	@return A list of strings containing the names and ranks of the items in the backlog.
	**/
	public List<String> pullBacklog(String projectID, String sprintID){
		return pullBacklog(projectID, sprintID, (String)null);
	}

	/**
	Delegates to pullBacklog(BacklogType type, String projectID, String sprintID, String teamID), with type being the default value BacklogType.PRODUCT.
	@see BacklogPuller#pullBacklog(String projectID, String sprintID, String teamID, boolean includeClosed)
	@param projectID The VersionOne Oid for the desired project.
	@param sprintID The VersionOne Oid for the desired sprint.
	@param teamID The VersionOne Oid for the desired team.
	@return A list of strings containing the names and ranks of the items in the backlog.
	**/
	public List<String> pullBacklog(String projectID, String sprintID, String teamID){
		return pullBacklog(projectID, sprintID, teamID, false);
	}
	
	
	
	public List<String> pullBacklog(String projectID, String sprintID, String teamID, boolean includeClosed){
		return pullBacklog(BacklogType.PRODUCT, projectID, sprintID, teamID, includeClosed);
	}
	
	/**
	Pulls the backlog of open PrimaryWorkItems (stories and defects) for the project designated by projectID, and (optionally) the sprint designated by sprintID, and returns an HTML page containing the names and ranks of the items in the backlog.
	@param type The type of backlog the client wishes to retrieve (this is currently ignored.)
	@param projectID The VersionOne Oid for the desired project.
	@param sprintID The VersionOne Oid for the desired sprint.
	@param teamID The VersionOne Oid for the desired team.
	@param includeClosed A boolean value indicating whether the returned results should include stories that have been closed.
	@return A list of strings containing the names and ranks of the items in the backlog.
	*/
	public List<String> pullBacklog(BacklogType type, String projectID, String sprintID, String teamID, boolean includeClosed){

		assert projectID != null;
		assert type != null;

		final String[] attStrs = {NAME, ISCLOSED, ORDER, ID, NUMBER, TEAM};

		IFilterTerm groupTerm = buildFilter(STORY, projectID, sprintID, teamID, includeClosed);
		
		List<Asset> assets = 
			new ArrayList<Asset>(V1Utils.findAssets(STORY, groupTerm, attStrs));
			
		groupTerm = buildFilter(DEFECT, projectID, sprintID, teamID, includeClosed);
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
			? BASE_URL_STORY
			: BASE_URL_DEFECT;
			
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
	Delegates to buildFilter(String assetType, String projectID, String sprintID, String teamID), and uses null as the (default) value for the teamID parameter.
	
	@see BacklogPuller#buildFilter(String assetType, String projectID, String sprintID)
	@param assetType The name of the type of asset on which the filter will operate. Also see <a href="https://www8.v1host.com/ParishSOFTLLC/meta.v1?xsl=api.xsl">the VersionOne meta page.</a>
	@param projectID The plain-text value of the unique identifier for a project, known in VersionOne as an Oid (Object identifier)
	@param sprintID The plain-text name of VersionOne's unique identifier for a Sprint. May be null.
	@return an IFilterTerm that will limit a VersionOne Query to items that are still open, and which are associated with the project designated by the value of projectID.
	**/

	private IFilterTerm buildFilter(String assetType, String projectID, 
		String sprintID){
		return buildFilter(assetType, projectID, sprintID, (String)null);
	}

	/**
	Delegates to buildFilter(String assetType, String projectID, String sprintID, String teamID), and specifies that closed items should not be included in the results.
	
	@param assetType The name of the type of asset on which the filter will operate. Also see <a href="https://www8.v1host.com/ParishSOFTLLC/meta.v1?xsl=api.xsl">the VersionOne meta page.</a>
	@param projectID The plain-text name of VersionOne's unique identifier for a project.
	@param sprintID The plain-text name of VersionOne's unique identifier for a Sprint. May be null.
	@param teamID The plain-text name of VersionOne's unique identifier for a Team. May be null.

	@return an IFilterTerm that will limit a VersionOne Query to items that are still open, and which are associated with the project designated by projectID and the sprint designated by sprintID. If assetType or projectID are null, or if assetType, projectID, or sprintID are not recognized by VersionOne, this method <i>may</i> return null.
	**/
	private IFilterTerm buildFilter(String assetType, String projectID, 
		String sprintID, String teamID){
		return buildFilter(assetType, projectID, sprintID, teamID, false);
	}
	
	
	
	/**
	Constructs an IFilterTerm that will limit a VersionOne Query to items that are associated with the project designated by projectID, the sprint designated by sprintID, the team designated by teamID, and which are open or closed based on the value of includeClosed.
	
	@param assetType The name of the type of asset on which the filter will operate. Also see <a href="https://www8.v1host.com/ParishSOFTLLC/meta.v1?xsl=api.xsl">the VersionOne meta page.</a>
	@param projectID The plain-text name of VersionOne's unique identifier for a project.
	@param sprintID The plain-text name of VersionOne's unique identifier for a Sprint. May be null.
	@param teamID The plain-text name of VersionOne's unique identifier for a Team. May be null.
	@param includeClosed The boolean value indicating whether closed items should be included in the results.

	@return an IFilterTerm that will limit a VersionOne Query to items that are still open, and which are associated with the project designated by projectID and the sprint designated by sprintID. If assetType or projectID are null, or if assetType, projectID, or sprintID are not recognized by VersionOne, this method <i>may</i> return null.
	**/
	private IFilterTerm buildFilter(String assetType, String projectID, 
		String sprintID, String teamID, boolean includeClosed){

		List<IFilterTerm> terms = new ArrayList<IFilterTerm>();

		IFilterTerm result = null;
		IAttributeDefinition closedDef = V1Utils.getAttribute(assetType, ISCLOSED);
		FilterTerm term = new FilterTerm(closedDef);
		term.equal(Boolean.valueOf(includeClosed));
		terms.add(term);
		
		IAttributeDefinition projectDef = V1Utils.getAttribute(assetType, PROJECT);
		term = new FilterTerm(projectDef);
		try{
			Oid oid = V1Services.getInstance().services().getOid(projectID);
			term.equal(oid);
			terms.add(term);
		}
		catch(Exception e){
			e.printStackTrace();
			System.err.println("Unable to construct the IFilterTerm.");
		}

		if (sprintID != null && !(sprintID.equals(""))){
			IAttributeDefinition sprintDef = V1Utils.getAttribute(assetType, SPRINT);
			term = new FilterTerm(sprintDef);
			term.equal(sprintID);
			terms.add(term);
		}
		
		if (teamID != null && !(teamID.equals(""))){
			IAttributeDefinition teamDef = V1Utils.getAttribute(assetType, TEAM);
			term = new FilterTerm(teamDef);
			term.equal(teamID);
			terms.add(term);
		}
		
		result = new AndFilterTerm((FilterTerm[])terms.toArray(new FilterTerm[terms.size()]));
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