package enums;

public enum EquipmentType {
	/*
	 * This is essentially the "Main Hand".
	 * All ItemType.Weapon types can use this hand.
	 * Two Handed Weapons always use this and
	 * the LeftHand.
	 */
	RightHand(0),
	Headware(1),
	Accessory(2),
	Clothing(3),
	Footware(4),
	/*
	 * This is the "Off Hand".
	 * It holds a second one handed ItemType.Weapon
	 * or a ItemType.Armor support item(for its Stats
	 * and/or special abilities).
	 */
	LeftHand(5);
	
	private final int value;
    private EquipmentType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}