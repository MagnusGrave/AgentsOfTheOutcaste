package data;

import java.awt.Point;
import java.io.Serializable;

import enums.SlotType;
import enums.WinConditionType;

public class BattleData implements Serializable {
	private static final long serialVersionUID = -8962566506620154475L;
	
	//used to instantiate nested classes
	public BattleData() {};
	
	//Alter BattleData to reply on the SceneData for the predefinedMap aspect
	/*public BattleData(Point[] emptyAllySlots, int[] predefinedMap, CharacterPlan[] allyCharacterPlans, CharacterPlan[] enemyCharacterPlans, WinCondition winCondition) {
		this.emptyAllySlots = emptyAllySlots;
		this.allyCharacterPlans = allyCharacterPlans;
		this.enemyCharacterPlans = enemyCharacterPlans;
		this.predefinedMap = predefinedMap;
		this.winCondition = winCondition;
	}*/
	//Don't store data for placementTiles in SceneData anymore, instead always set the placement locations manually via hardcoding 
	public BattleData(String name, PlacementSlot[] emptyAllySlots, CharacterPlan[] allyCharacterPlans, CharacterPlan[] enemyCharacterPlans, WinCondition winCondition, boolean isLossGameover) {
		this.name = name;
		this.emptyAllySlots = emptyAllySlots;
		this.allyCharacterPlans = allyCharacterPlans;
		this.enemyCharacterPlans = enemyCharacterPlans;
		this.winCondition = winCondition;
		this.isLossGameover = isLossGameover;
	}
	
	private String name;
	public String GetName() { return name; }
	
	//Slots where party members may be placed
	private PlacementSlot[] emptyAllySlots;
	public PlacementSlot[] EmptyAllySlots() { return emptyAllySlots; }
	
	//Predefined battle details
	//private int[] predefinedMap;
	//public int[] PredefinedMap() { return predefinedMap; }
	
	//Character Plans
	public class CharacterPlan implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 29851778268114869L;
		
		public CharacterPlan(CharacterData character, Point location, Point direction) {
			this.character = character;
			this.location = location;
			this.direction = direction;
		}
		private CharacterData character;
		public CharacterData Character() { return character; }
		private Point location;
		public Point Location() { return location; }
		private Point direction;
		public Point Direction() { return direction; }
	}
	private CharacterPlan[] allyCharacterPlans;
	public CharacterPlan[] AllyCharacterPlans() { return allyCharacterPlans; }
	private CharacterPlan[] enemyCharacterPlans;
	public CharacterPlan[] EnemyCharacterPlans() { return enemyCharacterPlans; }
	
	//Battle Win Conditions
	public class WinCondition implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7901874520120630711L;
		
		//public WinCondition(WinConditionType winConditionType, CharacterPlan assassinationTarget, Tile tileToOccupy, int turnsToSurvive) {
		public WinCondition(WinConditionType winConditionType, CharacterPlan assassinationTarget, Point tileToOccupy, int turnsToSurvive) {
			System.out.println("data.BattleData.WinCondition - winConditionType: " + winConditionType);
			this.winConditionType = winConditionType.ordinal();
			this.assassinationTarget = assassinationTarget;
			this.tileToOccupy = tileToOccupy;
			this.turnsToSurvive = turnsToSurvive;
		}
		//private WinConditionType winConditionType;
		private int winConditionType;
		//If this is causing a problem then inside the getter cast the member int variable into the enum
		public WinConditionType WinConditionType() {
			//System.out.println("BattleData.WinCondition.WinConditionType() - Ordinal value: " + winConditionType);
			return WinConditionType.values()[winConditionType];
		}
		//WinConditionType: Assassination
		private CharacterPlan assassinationTarget;
		public CharacterPlan AssassinationTarget() { return assassinationTarget; }
		//WinConditionType: OccupyTile
		//Doesn't make sense to use a Tile object here when all we need to know is the tile coordinate
		//private Tile tileToOccupy;
		//public Tile TileToOccupy() { return tileToOccupy; }
		private Point tileToOccupy;
		public Point TileToOccupy() { return tileToOccupy; }
		//WinConditionType: SurviveForTime
		private int turnsToSurvive;
		public int TurnsToSurvive() { return turnsToSurvive; }
	}
	private WinCondition winCondition;
	public WinCondition WinCondition() { return winCondition; }
	
	
	//Allow the player to continue to the next interaction and/or dialog, removes any consequences of losing at the price of the possible reward
	private boolean isLossGameover = false;
	public boolean IsLossGameover() { return isLossGameover; }

	
	public static BattleData GetExample() {
		return new BattleData("Example Battle", new PlacementSlot[] {new PlacementSlot(SlotType.AnyChar_Optional, new Point(), new Point())}, example_allyCharacterPlans, example_enemyCharacterPlans, example_winCondition, true);
	}
	
	//Example hard-coded data structures
	public static Point[] example_emptyAllySlots = new Point[] {
			new Point(0, 1),	
			new Point(1, 1),
			new Point(0, 2),
			new Point(1, 2),
			new Point(0, 3),
			new Point(1, 3),
	};
	
	//0 - GRASS, 1 - SAND, 2 - STONE, 3 - HOUSE, 4 - CASTLE, 5 - SHRINE, 6 - RIVER, 7 - TREE, 8 - MOUNTAIN
	/*public static int[] exampleMap = new int[] {
			
		  //1  2  3  4  5  6  7  8
			
			0, 8, 8, 0, 3, 0, 1, 6,   //1
			0, 0, 0, 0, 0, 1, 1, 6,   //2
			0, 0, 0, 1, 0, 1, 6, 6,   //3
			1, 1, 6, 6, 2, 6, 6, 6,   //4
			6, 6, 6, 6, 2, 1, 1, 0,   //5
			6, 6, 1, 5, 0, 0, 0, 7,   //6
			0, 7, 0, 0, 0, 2, 4, 0,   //7
			0, 0, 7, 0, 0, 0, 7, 0,   //8
			
	};*/
	
	public static CharacterPlan[] example_allyCharacterPlans = new CharacterPlan[] {
		//new BattleData().new CharacterPlan(new CharacterData("Elder Kami of Mt.Hideo", ClassType.KAMI, 5, 2, 4, 3, 10.0f, ""), new Point(3, 1), new Point(0, -1)),
	};
	
	public static CharacterPlan[] example_enemyCharacterPlans = new CharacterPlan[] {
		//new BattleData().new CharacterPlan(new CharacterData("Soldier", ClassType.SURF, 1, 2, 2, 1, 2.5f, ""), new Point(5, 5), new Point(0, -1)),	
		//new BattleData().new CharacterPlan(new CharacterData("Soldier", ClassType.SURF, 1, 2, 2, 2, 1.5f, ""), new Point(7, 6), new Point(0, -1)),
		//new BattleData().new CharacterPlan(new CharacterData("Samurai", ClassType.SURF, 3, 1, 2, 1, 3.0f, ""), new Point(6, 7), new Point(0, -1)),
		//new BattleData().new CharacterPlan(new CharacterData("Soldier", ClassType.SURF, 1, 2, 2, 2, 2.0f, ""), new Point(3, 7), new Point(0, -1)),
		//new BattleData().new CharacterPlan(new CharacterData("The Whispering One", ClassType.YOKAI, 2, 5, 4, 1, 8.0f, "portraits/Yurai.png"), new Point(7, 7), new Point(0, -1)),
	};
	
	public static WinCondition example_winCondition = new BattleData().new WinCondition(WinConditionType.DeathMatch, null, null, 0);
	
	@Override
	public String toString() {	
		return  "emptyAllySlots: " + emptyAllySlots.length +
				//", has predefinedMap: " + (predefinedMap != null) +
				", allyCharacterPlans: " + allyCharacterPlans.length +
				", enemyCharacterPlans: " + enemyCharacterPlans.length ;//+
				//", winCondition: " + winCondition.WinConditionType().toString();
	}
}
