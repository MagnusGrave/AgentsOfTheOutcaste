package enums;

public enum WorldTileType {
	//Grassland
	field(0), //main //used to be grass
	prairie(1), //2nd
	
	//Marsh
	marsh(2), //main
	
	//Farmland
	farmland(3), //main
	barrens(4), //2nd //used to be barren
	
	//Forest
	forest(5), //main
	forestEdge(6), //2nd //used to be bushland
	
	//Mountainous
	peak(7), //main //used to be mountain
	plateau(8), //2nd //used to be rockyGrass
	foothills(9), //3nd //used to be rockyLush
	
	//Dunes
	dunes(10), //main //used to be dune
	
	//Ocean, lake, river
	water(11), //main
	
	
	//Will most likey be unused
	blank(12),
	space(13),
	
	winter(14),
	tropical(15),
	volcanic(16),
	desert(17);
	
	private final int value;
    private WorldTileType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}