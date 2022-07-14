package enums;

public enum SettlementDesignation {
	Outcaste(0),
	Small(1),
	Medium(2),
	Large(3);
	
	private final int value;
    private SettlementDesignation(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}