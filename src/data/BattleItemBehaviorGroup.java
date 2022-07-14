package data;

import java.io.Serializable;

import enums.BattleItemType;

public class BattleItemBehaviorGroup implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6343916182271008627L;

	public BattleItemBehaviorGroup(BattleItemType[] itemTypes) {
		this.itemTypes = itemTypes;
	}
	
	public BattleItemType[] itemTypes;
}
