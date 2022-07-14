package enums;

public enum HitSelectionType {
	HitOnlySelf(0),
	HitOnlySelfAndAlly(1),
	HitOnlyEnemy(2),
	HitAll(3);
	
	private final int value;
    private HitSelectionType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
