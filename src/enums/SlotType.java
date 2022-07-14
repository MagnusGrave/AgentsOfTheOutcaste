package enums;

public enum SlotType {
	ForcePlayerChar(0),
	AnyChar_Manditory(1),
	AnyChar_Optional(2);
	
	private final int value;
    private SlotType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}