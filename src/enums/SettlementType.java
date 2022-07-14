package enums;

public enum SettlementType {
	//Mercantile/Maintenance Focused
	Campsite(0),
	Teahouse(1),
	Market(2),				//Size Variety
	Blacksmith(3),
	Doctor(4),
	
	//Infrastructure
	Crossroads(5),
	NotableLocation(6),
	QuestBoard(7),
	Estate(8),
	Village(9),				//Size Variety
	Garden(10),
	Shrine(11),
	Graveyard(12),			//Alternate: Graveyard_Haunted
	Castle(13),				//Size Variety
	
	//Roaming Locations
	MilitaryEncampment(14),	//Size Variety
	Battle(15), 			//Size Variety
	
	AssassinationTarget(16),
	
	ElementalDisturbance(17),
	Kami(18),
	
	YokaiActivity(19),
	YokaiAttack(20);
	
	private final int value;
    private SettlementType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
    
    
    private static SettlementType[] roamingSettlementTypes;
    
    static {
    	roamingSettlementTypes = new SettlementType[] {
	    	MilitaryEncampment,
	    	Battle,
	    	AssassinationTarget,
	    	ElementalDisturbance,
	    	Kami,
	    	YokaiActivity,
	    	YokaiAttack
	    };
    }
    
    public static boolean IsRoamingSettlement(SettlementType type) {
    	boolean isRoaming = false;
    	for(SettlementType roamingType : roamingSettlementTypes) {
    		if(roamingType == type) {
    			isRoaming = true;
    			break;
    		}
    	}
    	return isRoaming;
    }
}