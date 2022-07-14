package enums;

public enum WeightType {
	Light(0),
	Medium(1),
	Heavy(2);
	
	private final int value;
    private WeightType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
