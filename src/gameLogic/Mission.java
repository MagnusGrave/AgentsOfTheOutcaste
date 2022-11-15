package gameLogic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import data.InteractionManager;
import data.ItemData;
import dataShared.DialographyData;
import dataShared.LocationContentData.MissionStructure;
import enums.EnvironmentType;
import enums.InteractionType;
import enums.SettlementType;
import enums.WorldTileType;


public class Mission implements Serializable {
	private static final long serialVersionUID = 25659400152569567L;
	
	/*public Mission(String id, String name, String description, MapLocation mapLocation, InteractionType[] missionStipulations, ItemData[] rewards) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.mapLocation = mapLocation;
		this.mapLocation.SetGoverningMission(this);
		if(missionStipulations != null)
			this.missionStipulations = new ArrayList<>(Arrays.asList(missionStipulations));
		this.rewards = rewards;
		
		Missions.OnMissionConstruction(this);
	}
	
	//This type of mission with be used by a flexible transition to override a matching worldtile given the transition conditions are met
	public Mission(String id, String name, String description, EnvironmentType genericLocationEnvironmentType, MapLocation overridingMapLocation, InteractionType[] missionStipulations, ItemData[] rewards) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.genericLocationEnvironmentType = genericLocationEnvironmentType;
		this.mapLocation = overridingMapLocation;
		this.mapLocation.SetGoverningMission(this);
		if(missionStipulations != null)
			this.missionStipulations = new ArrayList<>(Arrays.asList(missionStipulations));
		this.rewards = rewards;
		
		Missions.OnMissionConstruction(this);
	}
	
	//This type of mission with be used by a flexible transition to use a matching generic worldTile as the stage for this mission (given the transition conditions are met)
	public Mission(String id, String name, String description, EnvironmentType genericLocationEnvironmentType, InteractionType[] missionStipulations, ItemData[] rewards) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.genericLocationEnvironmentType = genericLocationEnvironmentType;
		if(missionStipulations != null)
			this.missionStipulations = new ArrayList<>(Arrays.asList(missionStipulations));
		this.rewards = rewards;
		
		Missions.OnMissionConstruction(this);
	}*/
	
	//Instantiate from json data
	//public Mission(dataShared.MissionData missionData, MapLocation mapLocation, Interaction[] interactions, DialographyData dialography_entry) {
	public Mission(dataShared.MissionData missionData, Interaction[] interactions, DialographyData dialography_entry, MissionStructure missionStructure, WorldTileType sourceTileType) {
		this.id = missionData.id;
		this.name = missionData.name;
		System.out.println("Mission: " + this.name + ", id: " + this.id);
		this.description = missionData.description;
		this.genericLocationEnvironmentType = missionData.genericLocationEnvironmentType;
		this.genericPlotMinRadius = missionData.genericPlotMinRadius;
		this.genericLocationSettlementType = missionData.genericLocationSettlementType;
		
		this.sourceTileType = sourceTileType;
		
		if(missionData.missionStipulations != null && missionData.missionStipulations.length > 0)
			this.missionStipulations = new ArrayList<>(Arrays.asList(missionData.missionStipulations));
		this.rewards = Items.GetItemDatas(missionData.rewardsRefs);

		this.interactionManager = new InteractionManager(interactions);
		
		this.dialography_entry = dialography_entry;
		
		this.missionStatus = MissionStatusType.Pending;
		
		this.missionStructure = missionStructure;
	}
	
	private DialographyData dialography_entry;
	public DialographyData GetDialography_entry() { return dialography_entry; }
	
	// <- New Interactions Logic -
	
	//Missions now store their own unique interactions and/or choreography and MapLocations carry default/generalizeUtility interactions and/or choreographies
	//TODO Since Mission isn't serialized in saveData each mission's InteractionManager will need to be saved in some kind of structure, perhaps in MapLocation there
	//could be a Map<[MissionId], InteractionManager>
	private InteractionManager interactionManager;
	public InteractionManager GetInteractionManager() { return interactionManager; }
	
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
	
	// - New Interactions Logic ->
	
	//A value manually generated and passed into every instantiation, this should never change because all other scripts use it as the reference to this mission
	private String id;
	public String getId() { return id; }
	
	private String name;
	public String getName() { return name; }
	
	private String description;
	public String getDescription() { return description; }
	
	//Use a static unique MapLocation that'll be placed on the worldmap during its generation or a generic location environment type that convert a tile meeting the proper conditions into the unplacedMapLocation
	private EnvironmentType genericLocationEnvironmentType;
	public EnvironmentType getGenericLocationEnvironmentType() { return genericLocationEnvironmentType; }
	
	private int genericPlotMinRadius;
	public int getGenericPlotMinRadius() { return genericPlotMinRadius; }
	
	private SettlementType genericLocationSettlementType;
	public SettlementType getGenericLocationSettlementType() { return genericLocationSettlementType; }
	
	/**
	 * See Mission.getSourceTileType() for an explaination.
	 */
	private WorldTileType sourceTileType;
	/**
	 * This indicates which Scene the mission was designed for. Flexible transitions are only concerned with the generic EnvironmentType so when the flexible mission occurs it may be necessary to
	 * dynamically swap the host Tile's MapLocation scene with the Mission's source scene. This will cause a disconnect between the worldmap tile and MapLocation such that the MapLocation's WorldTileType will
	 * no longer match with the tile's hexigon image representation but this disconnect is a worthy price to pay for the increased number of potential locations a flexible transition could occur.
	 * For example: When a player enters a Forest WorldTileType tile, a flexible transition may allow a mission to occur there even if the mission's sourceTileType is ForestEdge and upon this mission's activation
	 * the WorldMapData will be overwritten so that from that moment forward that tile is a ForestEdge MapLocation even though its worldmap tile image will always appear as a Forest.
	 * 
	 * @return Mission.sourceTileType
	 */
	public WorldTileType getSourceTileType() { return sourceTileType; }
	
	//private MapLocation mapLocation;
	/**
	 * This can either be populated with an Id (meaning that it's tied to a MapLocation plotted on the worldmap)
	 * -or-
	 * It can be null (meaning that it's a mission that begins via FlexibleTransition, it doesn't have a home yet)
	 */
	private String mapLocationId;
	//public MapLocation getMapLocation() {	return mapLocation; }
	public String getMapLocationId() { return mapLocationId; }
	/**
	 * This method is used to set the mapLocation of a mission later, for missions beginning after FlexibleTransitions, ones that dont start out with a MapLocation set in their constructors
	 * @param mapLocationId
	 */
	public void setMapLocationId(String mapLocationId) {
		this.mapLocationId = mapLocationId;
	}
	
	//If the attempted interaction "succeeds" then call optionalMissionWrapper.CompleteMission()
	private List<InteractionType> missionStipulations;
	public List<InteractionType> getMissionStipulations() { return missionStipulations; }

	private ItemData[] rewards;
	public ItemData[] getRewards() { return rewards; }
	//Used after Mission construction to set self-referencial rewards
	public void AddRewardsItems(ItemData[] additionalRewards) {
		if(rewards != null && rewards.length > 0) {
			ItemData[] combinedItems = new ItemData[rewards.length + additionalRewards.length];
			for(int i = 0; i < rewards.length; i++)
				combinedItems[i] = rewards[i];
			for(int a = 0; a < additionalRewards.length; a++)
				combinedItems[rewards.length + a] = additionalRewards[a];
			rewards = combinedItems;
		} else {
			rewards = additionalRewards;
		}
	}
	
	public enum MissionStatusType { Pending, Active, Concluded };
	private MissionStatusType missionStatus;
	public MissionStatusType getMissionStatus() { return missionStatus; }
	public void SetMissionStatus(MissionStatusType newStatus) {
		missionStatus = newStatus;
	}
	
	/**
	 * The latest and greatest in information management.
	 */
	private MissionStructure missionStructure;
	public MissionStructure getMissionStructure() { return missionStructure; }
	
	//Set when WorldTile.SetMission(...) is called
	/*private WorldTileData occupiedWorldTileData;
	public WorldTileData getWorldTileData() {
		if(mapLocation != null)
			return mapLocation.getWorldTileData();
		else
			return occupiedWorldTileData;
	}
	public void SetWorldTileData(WorldTileData tileData) {
		if(mapLocation != null)
			mapLocation.SetWorldTileData(tileData);
		else
			occupiedWorldTileData = tileData;
	}*/
	//Missions are now completely decoupled from MapLocations so no more of this MapLocation->Mission hierarchy logic
	//The only link to a MapLocation on the worldMap is the mapLocationId field
	
	@Override
	public String toString() {
		return name;
	}
}
