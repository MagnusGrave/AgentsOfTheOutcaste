package gameLogic;

import java.io.Serializable;

import data.ItemData;
import data.BattleData.CharacterPlan;
import dataShared.DialographyData;
import dataShared.InteractionData;
import enums.ClassType;
import enums.InteractionType;
import enums.StatType;
import enums.TestType;

public class Interaction implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2287089591967106629L;
	
	
	//Interaction Tree Graph
	//General - (Persistent and Tree Intr's)
	//private boolean isPersistentIntr;
	/**
	 * Is this a Persistent Intr or a Tree Intr? If its persistent it will always trigger a Return To Base Layer resolution
	 * @return boolean
	 */
	//public boolean getIsPersistentIntr() { return isPersistentIntr; }
	//Tree Intr
	//private boolean isUniqueTreeIntr;
	/**
	 * @TreeTypes Unique, Instance
	 * Unique Tree Intr may only occur once every playthrough. Instance Tree Intr may reoccur upon refresh and/or travel timer.
	 * @return boolean
	 */
	//public boolean getIsUniqueTreeIntr() { return isUniqueTreeIntr; }
	//private boolean virginRootEnduresReturnToBase;
	/**
	 * Will the untouched root of this tree remain available upon an interaction that returns to the base interaction layer?
	 * @return bool
	 */
	//public boolean getVirginRootEnduresReturnToBase() { return virginRootEnduresReturnToBase; }
	//private int instanceRefreshTimer_days;
	/**
	 * If this is a Tree Instance Intr then how long does it take to reset. A value of 0 is irrelevant because at the very least the user will have to leave and come back
	 * to this MapLocation before the interaction will reset.
	 * @return int
	 */
	//public int getInstanceRefreshTimer_days() { return instanceRefreshTimer_days; }
	//private InteractionType[] cancelPersistentIntrTypes;
	/**
	 * Disable possible Persistent interactions past the point of handling this Interaction 
	 * @return InteractionType[]
	 */
	//public InteractionType[] getCancelPersistentIntrTypes() { return cancelPersistentIntrTypes; }
	
	
	//Used by non-tested interactions after Interaction Resolution in WorldmapPanel.ResolveCurrentInteraction(...)
	/*private DialogLine[] resultingDialog_onSuccess;
	public DialogLine[] ResultingDialog_OnSuccess() { return resultingDialog_onSuccess; }
	private String resultSummary_onSuccess;
	public String ResultSummary_onSuccess() { return resultSummary_onSuccess; }
	//Can be accompanied by resultingDialog_onSuccess or not
	private Interaction[] nextInteractions_onSuccess;
	public Interaction[] NextInteractions_OnSuccess() { return nextInteractions_onSuccess; }
	
	//Not used for non-tested interactions
	private DialogLine[] resultingDialog_onFailure;
	public DialogLine[] ResultingDialog_OnFailure() { return resultingDialog_onFailure; }
	private String resultSummary_onFailure;
	public String ResultSummary_onFailure() { return resultSummary_onFailure; }
	//AND OR
	private Interaction[] nextInteractions_onFailure;
	public Interaction[] NextInteractions_OnFailure() { return nextInteractions_onFailure; }*/
	
	
	//New Interaction properties - Start
	
	public Interaction(InteractionData interactionData) {
		//Initialize all the runtime refs
		this.interactionData = interactionData;
		grantedItems = Items.GetItemDatas(interactionData.grantedItems_refs);
		requiredItems = Items.GetItemDatas(interactionData.requiredItems_refs);
		manditoryEquipment = Items.GetItemDatas(interactionData.manditoryEquipment_refs);
		
		if(interactionData.battleData != null) {
			data.PlacementSlot[] emptyAllySlots = new data.PlacementSlot[interactionData.battleData.emptyAllySlots.length];
			for(int i = 0; i < interactionData.battleData.emptyAllySlots.length; i++) {
				dataShared.BattleData.PlacementSlot sharedSlot = interactionData.battleData.emptyAllySlots[i];
				emptyAllySlots[i] = new data.PlacementSlot(sharedSlot.slotType, sharedSlot.point, sharedSlot.suggestedDirection);
			}
			
			data.BattleData.CharacterPlan[] allyCharacterPlans = new data.BattleData.CharacterPlan[interactionData.battleData.allyCharacterPlans.length];
			for(int i = 0; i < interactionData.battleData.allyCharacterPlans.length; i++) {
				dataShared.BattleData.CharacterPlan sharedPlan = interactionData.battleData.allyCharacterPlans[i];
				allyCharacterPlans[i] = new data.BattleData().new CharacterPlan(Missions.GetCharacterById(sharedPlan.characterId), sharedPlan.location, sharedPlan.direction);
			}
			
			data.BattleData.CharacterPlan[] enemyCharacterPlans = new data.BattleData.CharacterPlan[interactionData.battleData.enemyCharacterPlans.length];
			for(int i = 0; i < interactionData.battleData.enemyCharacterPlans.length; i++) {
				dataShared.BattleData.CharacterPlan sharedPlan = interactionData.battleData.enemyCharacterPlans[i];
				enemyCharacterPlans[i] = new data.BattleData().new CharacterPlan(Missions.GetCharacterById(sharedPlan.characterId), sharedPlan.location, sharedPlan.direction);
			}
			
			dataShared.BattleData.WinCondition sharedWinCon = interactionData.battleData.winCondition;
			dataShared.BattleData.CharacterPlan sharedPlan = interactionData.battleData.winCondition.assassinationTarget;
			CharacterPlan targetCharacterPlan = new data.BattleData().new CharacterPlan(Missions.GetCharacterById(sharedPlan.characterId), sharedPlan.location, sharedPlan.direction);
			data.BattleData.WinCondition winCondition = new data.BattleData().new WinCondition(sharedWinCon.winConditionType, targetCharacterPlan, sharedWinCon.tileToOccupy, sharedWinCon.turnsToSurvive);
			
			runtimeBattleData = new data.BattleData(interactionData.battleData.name, emptyAllySlots, allyCharacterPlans, enemyCharacterPlans, winCondition, interactionData.battleData.isLossGameover);
		}
	}
	
	InteractionData interactionData;
	
	//Getters
	
	public String getId() { return interactionData.id; }
	
	public InteractionType Type() { return interactionData.type; }
	
	//ALTERED METHOD SIGNATURE
	public DialographyData DialographyData_Pre() { return interactionData.dialographyData_Pre; }
	
	private ItemData[] grantedItems;
	public ItemData[] GrantedItems() { return grantedItems; }
	
	public String[] NewRecruitIds() { return interactionData.newRecruitIds; }
	
	public boolean IsRevealed() {
		if(hasBeenRevealed_runtimeOnly)
			return true;
		else
			return interactionData.isRevealed;
	}
	
	public boolean DoesBlockMapMovement() { return interactionData.doesBlockMapMovement; }
	
	public TestType TestType() { return interactionData.testType; }
	
	public StatType TestStat() { return interactionData.testStat; }

	public int PassingStatValue() { return interactionData.passingStatValue; }
	
	public float ChanceToPass() { return interactionData.chanceToPass; }
	
	private ItemData[] requiredItems;
	public ItemData[] RequiredItems() { return requiredItems; }

	public ClassType RequiredClass() { return interactionData.requiredClass; }
	
	private data.BattleData runtimeBattleData;
	public data.BattleData BattleData() { return runtimeBattleData; }
	
	
	public boolean getIsPersistentIntr() { return interactionData.isPersistentIntr; }
	
	public boolean getIsUniqueTreeIntr() { return interactionData.isUniqueTreeIntr; }
	
	public boolean getVirginRootEnduresReturnToBase() { return interactionData.virginRootEnduresReturnToBase; }
	
	public int getInstanceRefreshTimer_days() { return interactionData.instanceRefreshTimer_days; }
	
	public InteractionType[] getCancelPersistentIntrTypes() { return interactionData.cancelPersistentIntrTypes; }
	
	//ALTERED METHOD SIGNATURE
	public DialographyData DialographyData_OnSuccess() { return interactionData.dialographyData_onSuccess; }

	private Interaction[] nextInteractions_onSuccess;
	//For json data based instantiation
	public void SetNextInteractions_OnSuccess(Interaction[] interactions) {
		nextInteractions_onSuccess = interactions;
	}
	public Interaction[] NextInteractions_OnSuccess() { return nextInteractions_onSuccess; }
	
	//ALTERED METHOD SIGNATURE
	public DialographyData DialographyData_OnFailure() { return interactionData.dialographyData_onFailure; }

	private Interaction[] nextInteractions_onFailure;
	//For json data based instantiation
	public void SetNextInteractions_OnFailure(Interaction[] interactions) {
		nextInteractions_onFailure = interactions;
	}
	public Interaction[] NextInteractions_OnFailure() { return nextInteractions_onFailure; }
	
	
	public String GotoMapLocationID() { return interactionData.gotoMapLocationId; }
	public MapLocation GotoLocation() {
		if(interactionData.gotoMapLocationId == null || interactionData.gotoMapLocationId.isEmpty()) {
			System.out.println("Interaction.GotoLocation() - interactionData.gotoMapLocationId is blank. Returning null.");
			return null;
		} else
			//return Missions.GetMapLocation(interactionData.gotoMapLocationId);
			//We should rely on the active data rather than the blank templates loaded from LocationContentData files
			return Game.Instance().GetWorldmapData().GetWorldMapDatas().values().stream().filter(x -> x.mapLocation.getId().equals(interactionData.gotoMapLocationId)).findFirst().orElse(null).mapLocation;
	}
	
	public String GotoMissionId() {
		if(interactionData.gotoMissionId == null || interactionData.gotoMissionId.isEmpty()) {
			System.out.println("Interaction.GotoMissionId() - interactionData.gotoMissionId is blank. Returning null.");
			return null;
		} else
			return interactionData.gotoMissionId;
	}
	
	
	public ItemData[] manditoryEquipment;
	public ItemData[] ManditoryEquipment() { return manditoryEquipment; }
	
	
	//New Interaction properties - End
	
	//Constructed Variables - End
	
	//Post-Construction References (For Code-Created Instances) - Start
	
	//Non-dynamic interactions
	//Trade: Trade with an auto randomly generated or predefined merchant
	//Flee: Attempt to  move to another MapLocation
	//Travel: Instantly move to a specific new MapLocation
	
	//private MapLocation gotoLocation_onInteraction;
	//public MapLocation GotoLocation_onInteraction() { return gotoLocation_onInteraction; }
	
	public void AddLocationFor_OnInteraction(MapLocation destination) {
		//gotoLocation_onInteraction = destination;
		interactionData.gotoMapLocationId = destination.getId();
	}
	
	public Interaction WithGotoLocation(MapLocation destination) {
		//this.gotoLocation_onInteraction = destination;
		this.interactionData.gotoMapLocationId = destination.getId();
		return this;
	}
	
	//private String gotoMissionId_onInteraction;
	//public String GotoMissionId_onInteraction() { return gotoMissionId_onInteraction; }
	
	public void AddMissionFor_OnInteraction(Mission linkedMission) {
		//gotoMissionId_onInteraction = linkedMission.getId();
		interactionData.gotoMissionId = linkedMission.getId();
	}
	
	public void AddGrantedItems(ItemData[] additionalItems) {
		if(grantedItems != null && grantedItems.length > 0) {
			ItemData[] combinedItems = new ItemData[grantedItems.length + additionalItems.length];
			for(int i = 0; i < grantedItems.length; i++)
				combinedItems[i] = grantedItems[i];
			for(int a = 0; a < additionalItems.length; a++)
				combinedItems[grantedItems.length + a] = additionalItems[a];
			grantedItems = combinedItems;
		} else {
			grantedItems = additionalItems;
		}
	}
	//I dont think an ItemRef equivalent is necessary
	/*
	public void AddGrantedItems(ItemRef[] additionalItemRefs) {
		if(interactionData.grantedItems_refs != null && interactionData.grantedItems_refs.length > 0) {
			ItemRef[] combinedItemRefs = new ItemRef[interactionData.grantedItems_refs.length + additionalItemRefs.length];
			for(int i = 0; i < interactionData.grantedItems_refs.length; i++)
				combinedItemRefs[i] = interactionData.grantedItems_refs[i];
			for(int a = 0; a < additionalItemRefs.length; a++)
				combinedItemRefs[interactionData.grantedItems_refs.length + a] = additionalItemRefs[a];
			interactionData.grantedItems_refs = combinedItemRefs;
		} else {
			interactionData.grantedItems_refs = additionalItemRefs.clone();
		}		
	}
	*/
	
	//Post-Construction References (For Code-Created Instances) - End
	
	//Runtime Methods - Start
	
	boolean hasBeenRevealed_runtimeOnly;
	
	public void Activate() {
		//isRevealed = true;
		hasBeenRevealed_runtimeOnly = true;
	}
	
	//Runtime Methods - End
}
