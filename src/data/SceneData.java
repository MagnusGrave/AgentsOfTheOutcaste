package data;

import java.io.Serializable;
import java.util.List;

import enums.SceneLayeringType;


public class SceneData implements Serializable
{
    //public SceneData(Vector3Int boundsMapSize)
    //{
    //    this.sceneWidth = boundsMapSize.x;
    //    this.sceneHeight = boundsMapSize.y;
    //}

	private static final long serialVersionUID = 9041506241632095675L;
	
	public int sceneWidth;
    public int sceneHeight;
    
    public SceneLayeringType sceneLayeringType;
    public int settlementSceneOffsetX;
    public int settlementSceneOffsetY;
    public int settlementSceneWidth;
    public int settlementSceneHeight;

    public class Row implements Serializable
    {
		private static final long serialVersionUID = 5449731723275258059L;
		public class TileData implements Serializable
        {
			private static final long serialVersionUID = -4723624006125425245L;
			/*public TileData(int gridLocationX, int gridLocationY, TileBase tilebase)
            {
                this.gridLocationX = gridLocationX;
                this.gridLocationY = gridLocationY;

                if (tilebase is CollisionTile)
                {
                    CollisionTile collisionTile = (CollisionTile)tilebase;
                    isPassable = collisionTile.IsPassable;
                    penalty = collisionTile.Penalty;
                }
                else if (tilebase is PlacementTile)
                {
                    PlacementTile placementTile = (PlacementTile)tilebase;
                    isPassable = true;
                    isPlacementSlot = true;
                    isAllySlot = placementTile.IsAllySlot;
                }
                else
                {
                    Debug.LogError("Tile on collision tilemap isn't a CollisionTile or a PlacementTile. Use only those two Tile classes.");
                }
            }*/
            public int gridLocationX;
            public int gridLocationY;
            public boolean isPassable;
            public int penalty;
            public boolean isPlacementSlot;
            public boolean isAllySlot;
            
            // <- JAVA VERSION ONLY --
            //This is used purely for display purposes; the BattlePanel.TerrainPanel displays this location for the currently hovered Tile.TileData.
            //The rest of the game doesn't seem to care that there are dispondent tiles, the TileData's position variables must not be used anywhere else.
            public int comboSceneOffsetLocX;
            public int comboSceneOffsetLocY;
            // -- JAVA VERSION ONLY ->
        }
        //The data for each tile in the row (not the tiles above this specific row). Rows are counted from the bottom up
        public List<TileData> tileDatas; //= new List<TileData>();
        //Each layer, represented as a grid of images, gets added to this list in the order: breakaway_n(Top Most) -> breakaway_n-1 -> static_n -> static_n-1 -> base
        public class ImageLayer implements Serializable
        {
			private static final long serialVersionUID = -1444318895934163179L;
			//public ImageLayer(string imageFilePath, int layerHeight)
            //{
            //    this.layerHeight = layerHeight;
            //    this.imageFilePath = imageFilePath;
            //}
            public int layerHeight;
            public String imageFilePath;
            //This field is not imported from JSON exports from Unity, its populated when a nature and settlement scene are merged in Game.MergeSceneDatas() during the world building procedure for New Game.
            public boolean belongsToSettlement = false;
        }
        //This will always contain the base+static image, at the least. It may also contain breakaway image layers beyond that.
        public List<ImageLayer> imageLayers; //= new List<ImageLayer>();
    }
    public List<Row> rows; //= new List<Row>();

    public class Breakaway implements Serializable
    {
		private static final long serialVersionUID = 2837358712583195204L;
		//public Breakaway(int correspondingRowIndex, int correspondingImageLayerIndex)
        //{
        //    this.correspondingRowIndex = correspondingRowIndex;
        //    this.correspondingImageLayerIndex = correspondingImageLayerIndex;
        //}
        public int correspondingRowIndex;
        public int correspondingImageLayerIndex;
        public List<CoordinateData> activeCoordinates; //= new List<CoordinateData>();
        public class CoordinateData implements Serializable
        {
			private static final long serialVersionUID = -6777395847526519807L;
			//public CoordinateData(int gridLocationX, int gridLocationY)
            //{
            //    this.gridLocationX = gridLocationX;
            //    this.gridLocationY = gridLocationY;
            //}
            public int gridLocationX;
            public int gridLocationY;
        }
        //This field is not imported from JSON exports from Unity, its populated when a nature and settlement scene are merged in Game.MergeSceneDatas() during the world building procedure for New Game.
        public boolean belongsToSettlement = false;
    }
    public List<Breakaway> breakaways; //= new List<Breakaway>();
    
    //For AnimatedTiles and Doors
    public class VisualTileData implements Serializable
    {
		private static final long serialVersionUID = 1249947564127422463L;
		
		/*public VisualTileData(int gridLocationX, int gridLocationY, TileBase tilebase)
        {
            this.gridLocationX = gridLocationX;
            this.gridLocationY = gridLocationY;

            if (tilebase is AnimatedTile)
            {
                AnimatedTile animatedTile = (AnimatedTile)tilebase;
                //get m_AnimatedSprites_names
                m_AnimatedSprites_names = new string[animatedTile.m_AnimatedSprites.Length];
                for (int i = 0; i < animatedTile.m_AnimatedSprites.Length; i++)
                    m_AnimatedSprites_names[i] = animatedTile.m_AnimatedSprites[i].name;
            }
            else if (tilebase is DoorTile)
            {
                DoorTile doorTile = (DoorTile)tilebase;
                //get m_AnimatedSprites_names
                m_AnimatedSprites_names = new string[doorTile.m_AnimatedSprites.Length];
                for (int i = 0; i < doorTile.m_AnimatedSprites.Length; i++)
                    m_AnimatedSprites_names[i] = doorTile.m_AnimatedSprites[i].name;
                //get m_DoorClosedSprites_names
                m_DoorClosedSprites_names = new string[doorTile.m_DoorClosedSprites.Length];
                for (int i = 0; i < doorTile.m_DoorClosedSprites.Length; i++)
                    m_DoorClosedSprites_names[i] = doorTile.m_DoorClosedSprites[i].name;
            }
            else
            {
                Debug.LogError("VisualDataTile isn't a AnimatedTile or a DoorTile. Use only those two classes for special visuals.");
            }
        }*/
        public int gridLocationX;
        public int gridLocationY;
        public int rowIndex;
        //Static animated tile properties
        public String[] m_AnimatedSprites_names;
        //DoorTile properties
        public String[] m_DoorClosedSprites_names;
        
        //This field is not imported from JSON exports from Unity, its populated when a nature and settlement scene are merged in Game.MergeSceneDatas() during the world building procedure for New Game.
        public boolean belongsToSettlement = false;
    }
    public List<VisualTileData> visualTileDatas; //= new List<VisualTileData>();
}
