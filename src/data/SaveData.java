package data;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gameLogic.Mission;
import gui.WorldmapPanel;


public class SaveData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8100849546698431055L;
	
	public SaveData() {
		playerData = null;
		teamDatas = new CharacterData[] {};
		inventory = new ItemData[] {};
		gameOvers = 0;
		
		//completedMissionIds = new String[] {};
		missions = new ArrayList<Mission>();
		
		worldmapData = null;
		currentWorldmapLocation = null;
		isInMapLocation = true;
		partyStamina = WorldmapPanel.partyStaminaMax;
		
		setting_bgVolume = 70;
	}
	
	public SaveData(SaveData saveData) {
		this();
		
		//retain setting properties
		setting_bgVolume = saveData.setting_bgVolume;
	}
	
	//Team Data
	public CharacterData playerData;
	public CharacterData[] teamDatas;
	public ItemData[] inventory;
	/**
	 * The number of times this save profile has reached the Continue screen
	 */
	public int gameOvers;
	
	//public String[] completedMissionIds;
	/**
	 * Stores all current missions, which includes every main mission (created on NewGame) and every current side mission(created when conditions/randomness is right and destroyed once completed or
	 * timed out).
	 */
	public List<Mission> missions;
	
	/**
	 * Holds general mission progression hierarchy
	 */
	public List<MissionNode> missionTree;
	/**
	 * Holds the transition data for missions which only occur under special conditions. These are created for all main missions during world generation and will be preserved throughout the game.
	 */
	public List<FlexibleTransition> flexibleMissionTransitions;
	public int locationsVisitedSinceLastMissionCounter;
	
	//World Data
	public WorldmapData worldmapData;
	public Point2D currentWorldmapLocation;
	/**
	 * This mechanism remembers whether a player had entered into a mapLocation or were on the Worldmap when the previous game session ended.
	 * The default value should be TRUE because we want to start the game focused on the first MapLocation/Mission dialography.
	 */
	public boolean isInMapLocation = true;
	public int partyStamina;
	
	/**
	 * Interaction data of currently occupied MapLocation
	 */
	//public GraphPathNode[] currentGraphPath;
	//This is no longer sufficient to support the multifaceted structure of MapLocations and Missions
	
	
	//DEBUG - Records the instance of one of each possible type of MapLocation Nature/Settlement configuration and each unique location so that they may be inspected and debugged.
	public List<String> DEBUG_sceneSelections = new ArrayList<String>();
	
	
	// <- Lingering Data Values (These are retained across all new games) -
	
	//Settings Controls
	/**
	 * Volume ranging from 0-100, starts at 70%.
	 */
	public int setting_bgVolume = 70;
	
	// - Lingering Data Values ->

	
	@Override
	public String toString() {
		String message =
			"PlayerData: " + playerData.toString() +
			" _*_ TeamData.length: " + (teamDatas != null ? teamDatas.length : 0);
		return message;
	}
	
	public void print() {
		System.out.println("_SaveData_");
		System.out.println("playerData: " + (playerData != null ? playerData.toString(true) : "null"));
		System.out.println("teamDatas.length: " + (teamDatas != null ? teamDatas.length : 0));
		System.out.println("inventory.length: " + (inventory != null ? inventory.length : 0));
		System.out.println("missions.length: " + (missions != null ? missions.size() : 0));
		System.out.println("currentMapLocation: " + currentWorldmapLocation);
		System.out.println("setting_bgVolume: " + setting_bgVolume);
		System.out.println("is worldmapData present: " + (worldmapData != null));
		if(worldmapData != null)
			worldmapData.print();
	}
}
