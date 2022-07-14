package enums;

public enum BattleItemType {
	/**Even though this is considered a separate item type, in
	 * practice it's a subset of the Status enum.
	 */
	Accelerant(0),
	Damage(1),
	Potion(2),
	Status(3),		
	Cure(4),
	Buff(5),
	Debuff(6),
	SpiritTool(7),
	Revive(8);
	
	private final int value;
    private BattleItemType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}