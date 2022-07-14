package dataShared;

import java.io.Serializable;

import enums.EnvironmentType;
import enums.SettlementDesignation;
import enums.SettlementType;
import enums.WorldTileType;

public class MapLocationData implements Serializable {
	private static final long serialVersionUID = 571951736176673852L;
	
	public String id;
	public EnvironmentType enviType;
    public WorldTileType tileType;
    public SettlementType settlementType;
    public SettlementDesignation settlementDesignation;

    public String name;
    public String description;

    //Use this to load a specific tile map JSON file, if this is null then use the generic map relating to the settlementType or worldTileType
    //Expected format is "[SceneDirectory] + /". Ex: "DarkForest/"
    public String sceneDirectory;

    //if this is set then use the specified image, otherwise use the WorldTileType hex tile image as the bg
    //Expected format is "mapLocationBG/ + [FullImageName]" or "" for NULL. Ex: "mapLocationBG/bg_forest-forest.png"
    public String customBGImagePath;
    
    //[HideInInspector]
    //public Dialography.DialographyData dialographyData_default; //export this
    ///<summary>
    ///<para>Play this if there isn't a Mission entry dialography.</para>
    /// </summary> 
    //public Dialography dialography_default;//not this
    //Again redundant because of LocationContent structure

    //[HideInInspector]
    //public InteractionDataSlot.InteractionData[] interactionData; //export this
    //public InteractionDataSlot[] interactionData_Slots; //not this
    //this is redundant now because the Structure of LocationContent already knows how to group interactions with either their
    //governing Mission or MapLocation via hierarchy

    //[HideInInspector]
    //public string governingMissionId; //export this
    //public MissionDataSlot governingMissionId_slot; //not this
    //this doesn't make sense in any context because there can be multiple mission happening at one MapLocation over the course of the game,
    //use LocationContent to determine the active governing mission
}
