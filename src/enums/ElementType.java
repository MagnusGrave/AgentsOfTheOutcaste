package enums;

public enum ElementType {
	Wind(0),
	Water(1),
	Fire(2),
	Earth(3),
	
	Lightning(4),
	Ice(5),
	Darkness(6), Holy(7)
	;
	
	private final int value;
    private ElementType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
