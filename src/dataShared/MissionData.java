package dataShared;

import java.io.Serializable;

import enums.EnvironmentType;
import enums.InteractionType;
import enums.MissionIndicatorType;
import enums.SettlementType;

public class MissionData implements Serializable {
	private static final long serialVersionUID = 4847193630216793118L;
	
	public String id;
    public String name;
    public String description;

    //[HideInInspector]
    //public MapLocationSlot.MapLocationData mapLocationData; //export this
    //[Tooltip("This option provides a customized or \"SpecialUse\"(non-generic) MapLocation data instead of a generic one.")]
    //public MapLocationSlot mapLocation_Slot; //not this
    //Rendered redundant by the LocationContent structure

    //[HideInInspector]
    //public Dialography.DialographyData dialographyData_entry; //export this
    ///<summary>
    ///<para>Play this before the interactions or showing the standard MapLocation UI.</para>
    /// </summary> 
    //public Dialography dialography_entry; //not this
    //redundant because of Location Content

    public InteractionType[] missionStipulations;
    
    public ItemRef[] rewardsRefs; //collect this
    
    public EnvironmentType genericLocationEnvironmentType;
    //TODO - Add this property to the Unity side
    /**
     * Determines the min radius of the EnvironmentPlot that this mission can be placed in.
     */
    public int genericPlotMinRadius;
    //this doesnt make sense to set in unity because of the MapLocation centered data structuring of Missions. It only makes sense inside java runtime, if missions are constructed on the fly
    //but its still a good way to pack the information
    public SettlementType genericLocationSettlementType;
    
    public String gotoMissionIdAfterEntry;
    
    //Worldmap Mission Indicators
  	//Intro
  	public MissionIndicatorType indicator_nextPendingMission;
  	public boolean indicator_onlyShowUponEntering;
  	public boolean indicator_revealTileOnMap;
  	//Active
  	public MissionIndicatorType indicator_activeMission;
  	//Outro
  	public MissionIndicatorType indicator_completedMission;
  	public boolean indicator_removeAfterCompletion;
}
