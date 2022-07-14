package enums;

public enum KarmaType {
	Cursed(0),
	Neutral(1),
	Divine(2);
	
	private final int value;
    private KarmaType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}