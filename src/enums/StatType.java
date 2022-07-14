package enums;

public enum StatType {
	STRENGTH(0),
	INTELLECT(1),
	ENDURANCE(2),
	SPEED(3);
	
	private final int value;
    private StatType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
