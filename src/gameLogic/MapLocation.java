package gameLogic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import data.InteractionManager;
import data.ItemData;
import data.WorldTileData;
import dataShared.DialographyData;
import dataShared.LocationContentData.MapLocationStructure;
import enums.EnvironmentType;
import enums.InteractionType;
import enums.SceneLayeringType;
import enums.SettlementType;
import enums.WorldTileType;
import enums.SettlementDesignation;

public class MapLocation implements Serializable {
	private static final long serialVersionUID = -8849486915905410485L;
	
	
	/*public MapLocation(EnvironmentType enviType, WorldTileType tileType, SettlementType settlementType, SettlementDesignation designation, String name, String description,
			String sceneDirectory, String customBGImagePath, Interaction[] interactions, SceneLayeringType sceneLayeringType) {
		this.id = UUID.randomUUID().toString();
		this.enviType = enviType;
		this.tileType = tileType;
		this.settlementType = settlementType;
		this.settlementDesignation = designation;
		this.name = name;
		this.description = description;
		this.sceneDirectory = sceneDirectory;
		this.customBGImagePath = customBGImagePath;
		this.interactionManager = new InteractionManager(interactions);
	}*/
	
	/**
	 * For Nature Layer locations, generic Nature & Settlement locations or unique locations. (id will be supplied with a null parameter for everything except unique locations)
	 * @param id
	 * @param enviType
	 * @param tileType
	 * @param settlementType
	 * @param designation
	 * @param name
	 * @param description
	 * @param sceneDirectory
	 * @param customBGImagePath
	 * @param interactions
	 * @param interactionlessDialographies
	 * @param sceneLayeringType
	 */
	public MapLocation(String id, EnvironmentType enviType, WorldTileType tileType, SettlementType settlementType, SettlementDesignation designation, String name, String description,
			String sceneDirectory, String customBGImagePath, Interaction[] interactions, List<DialographyData> interactionlessDialographies, SceneLayeringType sceneLayeringType,
			MapLocationStructure mapLocationStructure
			) {
		this.id = id;
		this.enviType = enviType;
		this.tileType = tileType;
		this.settlementType = settlementType;
		this.settlementDesignation = designation;
		this.name = name;
		this.description = description;
		
		this.sceneDirectory = sceneDirectory;
		if(this.sceneDirectory != null && this.enviType == null) {
			System.out.println("MapLocation Constructor(Generic Nature & Settlement -OR- Unique Locations) - natureless sceneDirectory: " + sceneDirectory);
		}
		
		this.customBGImagePath = customBGImagePath;
		this.interactionManager = new InteractionManager(interactions);
		this.interactionlessDialographies = interactionlessDialographies;
		this.sceneLayeringType = sceneLayeringType;
		this.mapLocationStructure = mapLocationStructure;
	}
	
	/**
	 * For Both Layer combo locations that're merged and served to new WorldTiles. This merger happens after each individual MapLocation template has been instantiated with the constructor above.
	 * @param id
	 * @param enviType
	 * @param tileType
	 * @param settlementType
	 * @param designation
	 * @param name
	 * @param description
	 * @param sceneDirectory
	 * @param customBGImagePath
	 * @param interactions
	 * @param interactionlessDialographies
	 * @param sceneLayeringType
	 */
	public MapLocation(EnvironmentType enviType, WorldTileType tileType, SettlementType settlementType, SettlementDesignation designation, String name, String description,
			String[] comboSceneDirectories, String customBGImagePath, Interaction[] interactions, List<DialographyData> interactionlessDialographies, SceneLayeringType sceneLayeringType,
			MapLocationStructure mapLocationStructure
			) {
		this.enviType = enviType;
		this.tileType = tileType;
		this.settlementType = settlementType;
		this.settlementDesignation = designation;
		this.name = name;
		this.description = description;
		
		this.comboSceneDirectories = comboSceneDirectories;
		if(this.comboSceneDirectories != null) {
			String direcs = "";
			for(String dir : this.comboSceneDirectories) {
				direcs += dir + ", ";
			}
			System.out.println("MapLocation Constructor(Merger) - comboSceneDirectories: " + direcs);
		}
		
		this.customBGImagePath = customBGImagePath;
		this.interactionManager = new InteractionManager(interactions);
		this.interactionlessDialographies = interactionlessDialographies;
		this.sceneLayeringType = sceneLayeringType;
		this.mapLocationStructure = mapLocationStructure;
	}
	
	/**
	 * Used to copy MapLocation instances to keep the templates pure.
	 * @param mapLocation
	 */
	public MapLocation(MapLocation mapLocation) {
		this.id = mapLocation.id;
		this.enviType = mapLocation.enviType;
		this.tileType = mapLocation.tileType;
		this.settlementType = mapLocation.settlementType;
		this.settlementDesignation = mapLocation.settlementDesignation;
		this.name = mapLocation.name;
		this.description = mapLocation.description;
		this.sceneDirectory = mapLocation.sceneDirectory;
		if(this.sceneDirectory != null && this.enviType == null) {
			System.out.println("MapLocation Constructor(Copy) - natureless sceneDirectory: " + sceneDirectory);
		}
		
		this.comboSceneDirectories = mapLocation.comboSceneDirectories;
		if(this.comboSceneDirectories != null) {
			String direcs = "";
			for(String dir : this.comboSceneDirectories) {
				direcs += dir + ", ";
			}
			System.out.println("MapLocation Constructor(Copy) - comboSceneDirectories: " + direcs);
		}
		
		this.customBGImagePath = mapLocation.customBGImagePath;
		this.interactionManager = new InteractionManager( mapLocation.interactionManager.getInteractions() );
		this.interactionlessDialographies = mapLocation.interactionlessDialographies;
		this.sceneLayeringType = mapLocation.sceneLayeringType;
		this.mapLocationStructure = mapLocation.mapLocationStructure;
	}
	
	/**
	 * Generic template MapLocations instantiated during World Generation have no Id until they are placed on a WorldTile. Unique MapLocations have an id built into their LocationContentData
	 * that they carry forever.
	 */
	private String id;
	public String getId() { return id; }
	/**
	 * Called by WorldTile when it receive its MapLocation instance during World Generation.
	 * @param id
	 */
	public void SetRandomId() {
		this.id = UUID.randomUUID().toString();
	}
	
	private EnvironmentType enviType;
	public EnvironmentType getEnviType() { return enviType; }
	
	private WorldTileType tileType;
	public WorldTileType getTileType() { return tileType; }
	
	private SettlementType settlementType;
	public SettlementType getSettlementType() { return settlementType; }
	private SettlementDesignation settlementDesignation;
	public SettlementDesignation getSettlementDesignation() { return settlementDesignation; }
	
	private String name;
	public String getName() { return name; }
	
	private String description;
	public String getDescription() { return description; }
	
	//Use this to load a specific tile map JSON file, if this is null then use the generic map relating to the settlementType or worldTileType
	private String sceneDirectory;
	public String getSceneDirectory() {
		if(sceneDirectory == null)
			return null;
		else
			return Game.SceneDirectoryRoot() + sceneDirectory;
	}
	public String getRelativeSceneDirectory() { return sceneDirectory; }
	private String[] comboSceneDirectories;
	public String getComboNatureSceneDirectory() {
		if(comboSceneDirectories != null)
			return Game.SceneDirectoryRoot() + comboSceneDirectories[0];
		else
			return null;
	}
	public String getRelativeComboNatureSceneDirectory() {
		if(comboSceneDirectories != null)
			return comboSceneDirectories[0];
		else
			return null;
	}
	public String getComboSettlementSceneDirectory() {
		if(comboSceneDirectories != null)
			return Game.SceneDirectoryRoot() + comboSceneDirectories[1];
		else
			return null;
	}
	public String getRelativeComboSettlementSceneDirectory() {
		if(comboSceneDirectories != null)
			return comboSceneDirectories[1];
		else
			return null;
	}
	
	//if this is set then use the specified image, otherwise use the WorldTileType hex tile image as the bg
	private String customBGImagePath;
	public String getCustomBgImagePath() { return customBGImagePath; }
	
	private InteractionManager interactionManager;
	public InteractionManager GetInteractionManager() { return interactionManager; }
	
	//Record the game data stored in InteractionManager of Missions hosted at this MapLocation because MapLocations are serialized & loaded while Missions are not
	/*private Map<String, InteractionManager> missionInteractionManagerMap;
	public void SetNewInteractionManagerForMission(String missionsId, InteractionManager interactionManager) {
		if(missionInteractionManagerMap == null)
			missionInteractionManagerMap = new HashMap<String, InteractionManager>();
		System.out.println("MapLocation.SetNewInteractionManagerForMission( "+ missionsId +" )");
		missionInteractionManagerMap.put(missionsId, interactionManager);
	}
	public InteractionManager GetMissionsInteractionManager(String missionsId) {
		if(missionInteractionManagerMap == null)
			missionInteractionManagerMap = new HashMap<String, InteractionManager>();
		return missionInteractionManagerMap.get(missionsId);
	}*/
	//Missions are now serialized and can hold their own InteractionManagers
	
	/*private String governingMissionId;
	public String getGoverningMissionId() {return governingMissionId;}
	public void SetGoverningMission(Mission mission) {
		governingMissionId = mission.getId();
	}*/
	private List<String> missionIds = new ArrayList<String>();
	public List<String> GetMissionIds() { return new ArrayList<String>(missionIds); }
	public void AddMission(String missionId) {
		missionIds.add(missionId);
	}
	
	private List<DialographyData> interactionlessDialographies;
	public DialographyData GetDialography_default() {
		if(interactionlessDialographies != null && interactionlessDialographies.size() > 0)
			return interactionlessDialographies.stream().filter(x -> x.dialographyName.endsWith("Default")).findFirst().orElse(null);
		else
			return null;
	}
	public DialographyData GetDialography_specialCase(String caseName) {
		if(interactionlessDialographies != null && interactionlessDialographies.size() > 0)
			return interactionlessDialographies.stream().filter(x -> x.dialographyName.endsWith(caseName)).findFirst().orElse(null);
		else
			return null;
	}
	
	
	public void LinkLocation_OnInteraction(InteractionType type, MapLocation destination) {
		Interaction intera = interactionManager.GetInteraction(type);
		if(intera != null)
			intera.AddLocationFor_OnInteraction(destination);
		else
			System.err.println("LinkLocation_OnInteraction() - Location: " + name + " doesn't have an interaction of type: " + type);
	}
	
	public void LinkMission_OnInteraction(InteractionType type, Mission linkedMission) {
		Interaction intera = interactionManager.GetInteraction(type);
		if(intera != null)
			intera.AddMissionFor_OnInteraction(linkedMission);
		else
			System.err.println("LinkMission_OnInteraction() - Location: " + name + " doesn't have an interaction of type: " + type);
	}
	
	//Used after Mission construction to set self-referencial grantedItems for a specific interaction
	public void AddGrantedItemsForInteraction(InteractionType type, ItemData[] items) {
		Interaction intera = interactionManager.GetInteraction(type);
		if(intera != null) {
			intera.AddGrantedItems(items);
		} else
			System.err.println("AddGrantedItemsForInteraction() - Location: " + name + " doesn't have an interaction of type: " + type);
	}
	
	//Set when WorldTile.SetMapLocation(...) is called
	private WorldTileData occupiedWorldTileData;
	public WorldTileData getWorldTileData() { return occupiedWorldTileData; }
	public void SetWorldTileData(WorldTileData tileData) {
		occupiedWorldTileData = tileData;
	}
	
	//This will help in finding existing or identifying missing MapLocation to the appropriate SceneData and LocationContentData.MapLocationData/MissionData can be found
	private SceneLayeringType sceneLayeringType;
	public SceneLayeringType getSceneLayeringType() {
		return sceneLayeringType;
	}
	
	/**
	 * The latest and greatest in information management.
	 */
	private MapLocationStructure mapLocationStructure;
	public MapLocationStructure getMapLocationStructure() { return mapLocationStructure; }
}