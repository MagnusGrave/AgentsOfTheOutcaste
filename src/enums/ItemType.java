package enums;

public enum ItemType {
	Weapon(0),
	Armor(1),
	BattleItem(2),
	JourneyConsumable(3),
	Misc(4);

    private final int value;
    private ItemType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
