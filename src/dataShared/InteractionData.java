package dataShared;

import java.io.Serializable;

import enums.ClassType;
import enums.InteractionType;
import enums.StatType;
import enums.TestType;

public class InteractionData implements Serializable {
	private static final long serialVersionUID = -8621304596847239514L;
	
	public String id;
    public InteractionType type;
    public boolean isRevealed;
    public boolean doesBlockMapMovement;
    
    public TestType testType;
    public StatType testStat;
    public int passingStatValue;
    public float chanceToPass;
    public ItemRef[] requiredItems_refs;
    public ClassType requiredClass;

    public BattleData battleData;

    //Interaction Graph
    public boolean isPersistentIntr;
    public boolean isUniqueTreeIntr;
    public boolean virginRootEnduresReturnToBase;
    public int instanceRefreshTimer_days;
    public InteractionType[] cancelPersistentIntrTypes;

    public DialographyData dialographyData_Pre;

    public DialographyData dialographyData_onSuccess;
    public String[] nextInteractions_onSuccess_ids;

    public DialographyData dialographyData_onFailure;
    public String[] nextInteractions_onFailure_ids;

    public ItemRef[] grantedItems_refs;
    
    //For when characters join your team
    public String[] newRecruitIds;
    
    //public String gotoMapLocationId_onInteraction;
    //public String gotoMissionId_onInteraction; //this _onInteraction suffix is now misleading what with the new suffix scheme
    public String gotoMapLocationId;
    public String gotoMissionId;
    
    //Used to force-equip characters when being added to the battle roster
    public ItemRef[] manditoryEquipment_refs;
}
