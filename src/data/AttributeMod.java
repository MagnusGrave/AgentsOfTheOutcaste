package data;

import java.io.Serializable;

import enums.AttributeModType;

public class AttributeMod implements Serializable {
	private static final long serialVersionUID = -4335236743881992358L;
	
	/**
	 * For Stat Mods, Base Value Mods and Tile Penalty
	 * @param attributeModType
	 * @param pointOffset
	 */
	public AttributeMod(AttributeModType attributeModType, int pointOffset) {
		this.attributeModType = attributeModType;
		this.pointOffset = pointOffset;
	}
	/**
	 * For Chance Mods, AbilityPotency and StatusResistance 
	 * @param attributeModType
	 * @param pointOffset
	 */
	public AttributeMod(AttributeModType attributeModType, float chanceOffset) {
		this.attributeModType = attributeModType;
		this.chanceOffset = chanceOffset;
	}
	public AttributeModType attributeModType;
	/* Stat, Base Values */
	public int pointOffset;
	/* Chance, Ability */
	public float chanceOffset;
}