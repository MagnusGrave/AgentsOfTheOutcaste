package data;

import java.io.Serializable;

public class BattleToolTraits implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1060414356958654834L;

	public BattleToolTraits(int minRange, int maxRange, int aoeRange, WeaponTraits weaponTraits, BattleItemTraits battleItemTraits) {
		this.minRange = minRange;
		this.maxRange = maxRange;
		this.aoeRange = aoeRange;
		this.weaponTraits = weaponTraits;
		this.battleItemTraits = battleItemTraits;
	}
	
	//These two are direct from Column I as a value "3" or range "2-4"
	/**
	 * The min distance the attack must be from the character's location.
	 */
	public int minRange;
	/**
	 * The max range of the attack.
	 */
	public int maxRange;
	/**
	 * The radius of the area expanded from the targeted location of the attack.
	 */
	public int aoeRange;
	
	//instead of subclassing continue to use the same pattern we have been, which allows json to properly serialize the structure
	public WeaponTraits weaponTraits;
	public BattleItemTraits battleItemTraits;
}
