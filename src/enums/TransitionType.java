package enums;

public enum TransitionType {
	Direct(0), Flexible(1);
	
	private final int value;
    private TransitionType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
