package enums;

public enum SceneLayeringType {
	BothLayers(0),
	NatureLayer(1),
	SettlementLayer(2);
	
	private final int value;
    private SceneLayeringType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}