package data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import enums.BattleItemType;
import enums.StatusType;

public class BattleItemTraits implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7011259739242837269L;
	
	public BattleItemTraits(int effectDuration, StatusType status, BattleItemBehaviorGroup behaviorGroup) {
		this.effectDuration = effectDuration;
		this.status = status;
		this.behaviorGroup = behaviorGroup;
	}
	
	//public BattleItemTraits(int minRange, int maxRange, int aoeRange, int aoe, int effectDuration, StatusType status, BattleItemType[][] behaviorGroupOptions) {
	public BattleItemTraits(int effectDuration, StatusType status, BattleItemBehaviorGroup[] behaviorGroupOptions) {
		this.effectDuration = effectDuration;
		this.status = status;
		this.behaviorGroupOptions = behaviorGroupOptions;
	}
	
	public int effectDuration;
	public StatusType status;
	//For types that are occuring in unison, i.e. if all their types are separated by a comma
	public BattleItemBehaviorGroup behaviorGroup;
	//For BattleItems with a variety of uses, i.e. if their types are described using "or"
	public BattleItemBehaviorGroup[] behaviorGroupOptions;
	
	
	public static List<BattleItemType> GetAllBattleItemTypes(ItemData itemData) {
		List<BattleItemType> types = new ArrayList<BattleItemType>();
		if(itemData.getStats().GetBattleToolTraits().battleItemTraits.behaviorGroup != null)
			types.addAll( (List<BattleItemType>)Arrays.asList(itemData.getStats().GetBattleToolTraits().battleItemTraits.behaviorGroup.itemTypes) );
		else if(itemData.getStats().GetBattleToolTraits().battleItemTraits.behaviorGroupOptions != null) {
			for(BattleItemBehaviorGroup behavGroup : itemData.getStats().GetBattleToolTraits().battleItemTraits.behaviorGroupOptions)
				types.addAll( (List<BattleItemType>)Arrays.asList( behavGroup.itemTypes ) );
		} else
			System.err.println("BattleItemTraits.GetAllBattleItemTypes() - Can't get any BattleItemTypes for ItemData: " + itemData.getName());
		return types;
	}
}
