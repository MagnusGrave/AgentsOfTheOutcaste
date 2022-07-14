package enums;

public enum InteractionType {
	//Gateway Interations
	Explore(0), //a generic means of triggering a dialog *
	Camp(1), //Heal, possibly encounter other interactions *
	Steal(2), //Steal an item or quest item, may lead to fight or flee *
	Search(3), //Possibly obtain a random item or specfic quest item, could lead to other interations *
	Talk(4), //Some bit of small talk, a hint or a quest-line dialog. May lead to other interactions *
	
	//Conditional Interaction Gates based on battle outcomes.
	Fight(5), //Begins a battle, may lead to other interactions *
	
	//Non-dynamic interactions
	Trade(6), //Trade with an auto randomly generated or predefined merchant *
	Flee(7), //Attempt to  move to another MapLocation *
	Travel(8); //Instantly move to a specific new MapLocation *
	
	private final int value;
    private InteractionType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
    
    public static String GetMissionIcon(InteractionType testedInteractionType, SettlementType settlementType) {
    	String preffixPath = "missionEmblems/";
    	String imageName = "";
    	if(testedInteractionType != null) {
	    	switch(testedInteractionType) {
		    	case Explore: case Search:
		    		if(settlementType == SettlementType.ElementalDisturbance || settlementType == SettlementType.Kami)
		    			imageName = "mission_kamiRelated.png";
		    		else if(settlementType == SettlementType.YokaiActivity || settlementType == SettlementType.YokaiAttack)
		    			imageName = "mission_yokaiRelated.png";
		    		else
		    			imageName = "mission_exploration.png";
		    		break;
				case Steal:
					imageName = "mission_steal.png";
					break;
				case Talk:
					imageName = "mission_liaison.png";
					break;
				case Fight:
					if(settlementType == SettlementType.AssassinationTarget)
						imageName = "mission_assassination.png";
					else
						imageName = "mission_battle.png";
					break;
				case Trade:
					imageName = "mission_collection.png";
					break;
				case Travel:
					imageName = "mission_travel.png";
					break;
				case Camp: case Flee:
					imageName = "mission_nondescript.png";
					break;
				default:
					System.err.println("InteractionType.GetMissionIcon() - Add support for: " + testedInteractionType);
					break;
	    	}
    	} else {
    		imageName = "mission_nondescript.png";
    	}
    	return preffixPath + imageName;
    }
}
