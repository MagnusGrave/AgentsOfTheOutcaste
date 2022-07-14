package data;

import java.io.Serializable;

import enums.WeaponGroup;
import enums.WeaponType;

public class WeaponTraits implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5021722744313309750L;
	
	public WeaponTraits(WeaponGroup weaponGroup, WeaponType weaponType, boolean twoHandsRequired, boolean canBeProjectile) {
		this.weaponGroup = weaponGroup;
		this.weaponType = weaponType;
		this.twoHandsRequired = twoHandsRequired;
		this.canBeProjectile = canBeProjectile;
	}
	//This is drawn from the column header of the section currently being iterated over
	public WeaponGroup weaponGroup;
	public WeaponType weaponType;
	
	public boolean twoHandsRequired;
	public boolean canBeProjectile;
}
