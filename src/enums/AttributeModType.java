package enums;

import data.AttributeMod;

public enum AttributeModType {
	//ints
	BaseDamage(0), BaseArmor(1),
	TilePenalty(2),
	SequenceShift(3),
	//floats
	ChanceToHit(4), ChanceToDodge(5),
	AbilityPotency(6), ItemPotency(7),
	/**
	 * Allows those who possess it to have a separate chance to avoid status effects from being applied during ability attacks that're consider to have HIT. While they will be affected by the other
	 * CombatEffects of the hitting Ability they're not guaranteed to be hit by the Status CombatEffects.
	 */
	StatusResistance(8),
	/**
	 * This will factor into elemental damage calculation, this AttributeModType is somewhat the opposite of AbilityPotency(in that the AbilityPotency affects the base damage of an Ability attack
	 * which raises the potential elemental damage, if the attack has an elemental type). In addition to that parity, this mod type will also reduce the elemental damage of basic attacks
	 * that receive an elemental property from the used weapon.
	 */
	ElementalResistance(9);
	
	private final int value;
    private AttributeModType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
    
    public static String GetDisplayName(AttributeModType type) {
		String shortName = "[SHORT_NAME]";
		switch(type) {
			case BaseDamage:
				shortName = "Damage";
				break;
			case BaseArmor:
				shortName = "Armor";
				break;
			case TilePenalty:
				shortName = "Move Cost";
				break;
			case SequenceShift:
				shortName = "Time Warp";
				break;
			case ChanceToHit:
				shortName = "Hit Chance";
				break;
			case ChanceToDodge:
				shortName = "Dodge Chance";
				break;
			case AbilityPotency:
				shortName = "Ability Pow.";
				break;
			case ItemPotency:
				shortName = "Item Pow.";
				break;
			case StatusResistance:
				shortName = "Status Res.";
				break;
			case ElementalResistance:
				shortName = "Element Res.";
				break;
			default:
				System.err.println("AbilityManager.GetDisplayName() - Add support for AttributeModType: " + type);
				break;
		}
		return shortName;
	}
    
    public static String GetDisplayNameWithValue(boolean isBuff, AttributeMod mod) {
    	return AttributeModType.GetDisplayName(mod.attributeModType) +" "+ (mod.chanceOffset != 0f ?
    			(isBuff?"+":"") + Math.round(mod.chanceOffset*100f) + "%"
    			:
    			(isBuff?"+":"") + mod.pointOffset);
    }
}
