package dataShared;

import java.awt.Point;
import java.io.Serializable;

import enums.SlotType;
import enums.WinConditionType;

public class BattleData implements Serializable {
	private static final long serialVersionUID = 6624962169697441711L;
	
	
	public class PlacementSlot implements Serializable {
		private static final long serialVersionUID = 9146829952522676858L;
		
		public SlotType slotType;
	    public Point point;
	    public Point suggestedDirection;
	}
	
	public class CharacterPlan implements Serializable {
		private static final long serialVersionUID = 96235192797238096L;

		public String characterId;
	    
	    public Point location;
	    public Point direction;
	}

	public class WinCondition implements Serializable {
		private static final long serialVersionUID = 8763304715245602133L;
		
		public WinConditionType winConditionType;
	    //WinConditionType: Assassination
	    public CharacterPlan assassinationTarget; //Collect this
	    //WinConditionType: OccupyTile
	    public Point tileToOccupy;
	    //WinConditionType: SurviveForTime
	    public int turnsToSurvive;
	}
	

	public String name;

    //Slots where party members may be placed
    public PlacementSlot[] emptyAllySlots;
    
    //Character Plans
    public CharacterPlan[] allyCharacterPlans; //collect these
    public CharacterPlan[] enemyCharacterPlans; //collect these

    //Battle Win Conditions
    public WinCondition winCondition; //collect this

    //Allow the player to continue to the next interaction and/or dialog, removes any consequences of losing at the price of the possible reward
    public boolean isLossGameover = false;
}
