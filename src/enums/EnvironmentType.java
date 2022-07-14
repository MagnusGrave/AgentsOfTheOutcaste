package enums;

//General (and common across different biomes) Environments
public enum EnvironmentType {
	Grassland(0),
	Marsh(1),
	Farmland(2),
	Forest(3),
	Mountainous(4),
	Dunes(5),
	Water(6);
	
	private final int value;
    private EnvironmentType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}