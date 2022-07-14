package gameLogic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;

import data.SceneData;
import data.SceneData.Row.TileData;
import enums.CharacterTurnActionState;
import data.BattleData;
import data.BattleState;
import data.ItemData;
import data.PlacementSlot;
import gameLogic.AbilityManager.Ability;
import gameLogic.Game.InteractiveActionType;
import gameLogic.Game.ObjectiveType;
import gui.BattlePanel;
import gui.ImagePanel;
import gui.MapLocationPanel;

public class Board {
	//private int size;
	private List<Tile> tiles = new ArrayList<Tile>();
	
	//private static int boardSize = 8;
	//public static int BoardSize() { return boardSize; }
	private static Dimension boardDimensions;
	public static Dimension BoardDimensions() { return boardDimensions; }
	
	private static int scaledTileSize = 16; //94;
	public static int ScaledTileSize() { return scaledTileSize; } 
	
	private BattlePanel battlePanel;
	
	SceneData sceneData;
	JPanel boardGrid;
	
	
	private TileData GetSceneTileData(Point location) {
		//return sceneData.rows.get(location.y).tileDatas.get(location.x);
		GridLayout gridLayout = (GridLayout)boardGrid.getLayout();
		int blankUpperRows = Game.Instance().GetSceneData().sceneHeight - gridLayout.getRows();
		int reversedY = sceneData.sceneHeight - 1 - blankUpperRows - location.y;
		
		//return sceneData.rows.get(reversedY).tileDatas.get(location.x);
		if(sceneData.rows.size() <= reversedY || sceneData.rows.get(reversedY).tileDatas.size() <= location.x) {
			System.err.println("Tile doesn't exist at: " + location + "! Returning a fake TileData to avoid further errors.");
			return new SceneData().new Row().new TileData();
		} else
			return sceneData.rows.get(reversedY).tileDatas.get(location.x);
	}
	
    public Board(BattlePanel battlePanel, BattleState battleStateToRestore) {
		this.battlePanel = battlePanel;
		
		//System.err.println("Board Constructor - Do any extra setup involving battleStateToRestore");
		//Do any extra setup involving battleStateToRestore, there aren't any currently
		
		
		sceneData = Game.Instance().GetSceneData();
		BattleData battleData = Game.Instance().GetBattleData();
		
		Point collisionMapMin = new Point(0, 0);
		Point collisionMapMax = new Point(sceneData.sceneWidth, sceneData.sceneHeight);
		List<TileData> collisionTileDatas = new ArrayList<TileData>();
		Point lastPointInPreviousRow = null;
		for(int y = 0; y < sceneData.sceneHeight; y++) {
			if(sceneData.rows.get(y).tileDatas == null || sceneData.rows.get(y).tileDatas.size() == 0) {
				//if we haven't found our first row yet keep iterating
				if(lastPointInPreviousRow == null) {
					continue;
				} else {
					//this is the max
					collisionMapMax = lastPointInPreviousRow;
				}
			} else { //if there is tileData information on this row
				List<TileData> rowTileData = sceneData.rows.get(y).tileDatas;
				//Find min
				int firstX = rowTileData.get(0).gridLocationX;
				if(firstX > collisionMapMin.x)
					collisionMapMin = new Point(firstX, y);
				
				//Add all collision in this row
				collisionTileDatas.addAll(rowTileData);
				
				lastPointInPreviousRow = new Point(rowTileData.get(rowTileData.size()-1).gridLocationX, rowTileData.get(rowTileData.size()-1).gridLocationY);
			}
		}
		boardDimensions = new Dimension(collisionMapMax.x - collisionMapMin.x, collisionMapMax.y - collisionMapMin.y);
		System.out.println("boardDimensions based on collisionMap min/max: " + boardDimensions.width+", "+boardDimensions.height);
		//-Investigating this and see if its causing issues in every combo scene or in some and not others
		//-It only seems right in certain scenarios because the differencial between the nature scene and the settlement scene match. This fix needs to be dynamically offsetting the board dimensions.
		//boardDimensions = new Dimension(collisionMapMax.x + 1 - collisionMapMin.x, collisionMapMax.y + 1 - collisionMapMin.y);
		//Point sceneSizeDifferencial = new Point(sceneData.sceneWidth - sceneData.settlementSceneWidth, sceneData.sceneHeight - sceneData.settlementSceneHeight);
		//boardDimensions = new Dimension(collisionMapMax.x + sceneSizeDifferencial.x - collisionMapMin.x, collisionMapMax.y + sceneSizeDifferencial.y - collisionMapMin.y);
		//-I now believe the issue was not a differential of size but a difference of even/odd width and/or height. Try only adding or substracting 1 to balance out the sizes.
		/*if(sceneData.sceneWidth > sceneData.settlementSceneWidth || sceneData.sceneHeight > sceneData.settlementSceneHeight) {
			Dimension sceneSizeDifferential = new Dimension(sceneData.sceneWidth - sceneData.settlementSceneWidth, sceneData.sceneHeight - sceneData.settlementSceneHeight);
			sceneSizeDifferential = new Dimension(Math.max(1, sceneSizeDifferential.width/2), Math.max(1, sceneSizeDifferential.height/2));
			boardDimensions = new Dimension(boardDimensions.width + sceneSizeDifferential.width, boardDimensions.height + sceneSizeDifferential.height);
			
			Dimension sceneSize = new Dimension(sceneData.sceneWidth, sceneData.sceneHeight);
			Dimension sceneSettlementSize = new Dimension(sceneData.settlementSceneWidth, sceneData.settlementSceneHeight);
			System.err.println(
				"Scaling boardDimensions - sceneSize: " + sceneSize.width+", "+sceneSize.height +
				", sceneSettlementSize: " + sceneSettlementSize.width+", "+sceneSettlementSize.height +
				" = sceneSizeDifferential: " + sceneSizeDifferential.width+", "+sceneSizeDifferential.height +
				" -> scaled boardDimensions: " + boardDimensions.width+", "+boardDimensions.height
			);
		}*/
		Dimension sceneSettlementSize = new Dimension(sceneData.settlementSceneWidth, sceneData.settlementSceneHeight);
		boolean isBoardWidthEven = boardDimensions.width % 2 == 0;
		boolean isBoardHeightEven = boardDimensions.height % 2 == 0;
		boolean isSettlementWidthEven = sceneSettlementSize.width % 2 == 0;
		boolean isSettlementHeightEven = sceneSettlementSize.height % 2 == 0;
		if(isBoardWidthEven != isSettlementWidthEven || isBoardHeightEven != isSettlementHeightEven) {
			Dimension sceneSizeDifferential = new Dimension(isBoardWidthEven != isSettlementWidthEven ? 1 : 0, isBoardHeightEven != isSettlementHeightEven ? 1 : 0);
			boardDimensions = new Dimension(boardDimensions.width + sceneSizeDifferential.width, boardDimensions.height + sceneSizeDifferential.height);
			
			System.err.println(
				"Scaling boardDimensions - boardDimensions: " + boardDimensions.width+", "+boardDimensions.height +
				", sceneSettlementSize: " + sceneSettlementSize.width+", "+sceneSettlementSize.height +
				" = sceneSizeDifferential: " + sceneSizeDifferential.width+", "+sceneSizeDifferential.height +
				" -> scaled boardDimensions: " + boardDimensions.width+", "+boardDimensions.height
			);
		}
		
		//Panel stuff
		//JPanel boardGrid = new JPanel(new GridLayout(boardDimensions.width, boardDimensions.height));
		boardGrid = new JPanel(new GridLayout(boardDimensions.height, boardDimensions.width));

		//System.out.println("^^^^^^^^^^ __ boardDimensions: " + boardDimensions + ", orient_Horizontal? " + boardGrid.getComponentOrientation().isHorizontal() + ", leftToRight? " + boardGrid.getComponentOrientation().isLeftToRight());
		
		//boardGrid.setSize(ScaledTileSize() * boardDimensions.width, ScaledTileSize() * boardDimensions.height);
		int tileSize = MapLocationPanel.GetAdjustedTileSize();
		Dimension boardSize = new Dimension(tileSize * boardDimensions.width, tileSize * boardDimensions.height);
		boardGrid.setSize(boardSize);
		boardGrid.setPreferredSize(boardSize);
		boardGrid.setMinimumSize(boardSize);
		boardGrid.setMaximumSize(boardSize);
		
		//Random random = new Random();
		
		System.out.println("boardDimensions: " + boardDimensions.width + ", " + boardDimensions.height + ", tile count: " + (boardDimensions.width * boardDimensions.height));
		
		//Dimension tileDimension = new Dimension(ScaledTileSize(), ScaledTileSize());
		Dimension tileDimension = new Dimension(tileSize, tileSize);
		int gridIndex = 0;
		//Create the new version of the board here		
		for(int y = 0; y < boardDimensions.height; y++) {
			//System.out.println("Board() - y row: " + y);
			
			//Set the gridIndex to the first index in the flipped row
			//gridIndex = boardTileCount - boardDimensions.width - (boardDimensions.width * y);
			
			int reverseY = boardDimensions.height - 1 - y;
			
			for(int x = 0; x < boardDimensions.width; x++) {
				//int tileDataIndex = (x % boardDimensions.width) + (y*boardDimensions.width);
				int tileDataIndex = (x % boardDimensions.width) + (reverseY*boardDimensions.width);
				
				//TODO Check reverseY and the resulting tileDataIndex
				
				
				//TileData tileData = collisionTileDatas.get(tileDataIndex);
				//Terrain terrain = new Terrain(TerrainType.GRASS, tileData.penalty, tileData.isPassable);
				//-Attempting to fix issue with the bounds being bigger than the collision area by creating non-passable tiles in the empty spaces
				//-This attempted fix failed, it only created more errors. Further understanding is required.
				//-Going deeper with the adaptations to make this code work; by adapting other classes.
				//-I finally see whats happneing here: the right-most columb and top-most row are highlight-able but are blank. These tiles go beyond the bounds of the
				//nature layer. This leads me to believe that the problem lies in the math used to determine the combo scenes size.
				Terrain terrain = null;
				if(tileDataIndex >= collisionTileDatas.size()) {
					System.err.println("Tile out of collision bounds at point: " + x + ", " + y + ", tileDataIndex: " + tileDataIndex + ". Making fake Terrain object.");
					terrain = new Terrain(TerrainType.GRASS, -1, false);
				} else {
					TileData tileData = collisionTileDatas.get(tileDataIndex);
					terrain = new Terrain(TerrainType.GRASS, tileData.penalty, tileData.isPassable);
				}
				//-Doing more in-depth debugging
				/*TileData tileData = null;
				if(tileDataIndex >= collisionTileDatas.size()) {
					System.err.println("Tile out of collision bounds at point: " + x + ", " + y + ", tileDataIndex: " + tileDataIndex);
					continue;
				} else {
					tileData = collisionTileDatas.get(tileDataIndex);
				}
				Terrain terrain = new Terrain(TerrainType.GRASS, tileData.penalty, tileData.isPassable);*/
				
				
				//Panel
				Point location = new Point(x, y);
				//Point location = new Point(x, reverseY);
				JComponent tileComponent = new Box.Filler(tileDimension, tileDimension, tileDimension);
				
				//Set size explicitly
				tileComponent.setSize(tileDimension);
				
				//System.out.println("Board() - location: " + location + ", tileDataIndex: " + tileDataIndex + ", gridIndex: " + gridIndex);
				
				//SetupGridTile(location, tileComponent);
				SetupGridTile(new Point(x, y), tileComponent);
				
				boardGrid.add(tileComponent);
				
				//Tile Data structure
				tiles.add(new Tile(location, terrain, tileComponent, boardGrid, gridIndex));
				gridIndex++;
			}
		}
		
		int sceneHeight = Game.Instance().GetSceneData().sceneHeight;
		GridLayout gridLayout = (GridLayout)boardGrid.getLayout();
		int blankUpperRows = sceneHeight - gridLayout.getRows();
		for(PlacementSlot placementSlot : battleData.EmptyAllySlots()) {
			Point blankShiftLoc = new Point(placementSlot.point.x,  sceneHeight - blankUpperRows - 1 - placementSlot.point.y);
			GetTileAt(blankShiftLoc).TogglePlacementSlot(true);
		}
		
		this.battlePanel.Initialize(this, boardGrid, battleStateToRestore);
	}
	
	private void SetupGridTile(Point location, JComponent comp) {
		//comp.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1)); //I dont like this
		//Add any generic panel styling
		
		
		comp.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				ClickTile(location);
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				FocusOnTile(location);
			}
			@Override
			public void mouseExited(MouseEvent e) {
				LoseFocusOnTile(location);
			}
			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseReleased(MouseEvent e) {}
		});
	}
	
	private final String tileOverlayPath = "battleTiles/Overlay.png";
	
	public class Tile {
		private Point location;
		public Point Location() { return location; }
		private CharacterBase occupant;
		public CharacterBase Occupant() { return occupant; }
		private boolean isHighlighted;
		public boolean IsHighlighted() { return isHighlighted; }
		
		private JComponent currentComponent;
		private JComponent fillerBox;
		private ImagePanel imagePanel; //this will now only be used in scenarios where a tile needs to be indicated on screen; movement, placement, etc
		private Container boardGrid;
		private int gridIndex;
		
		//private TerrainType terrainType;
		private Terrain terrain;
		//public TerrainType TerrainType() { return terrainType; }
		public TerrainType TerrainType() { return terrain.getType(); }
		public Terrain GetTerrain() {
			//Terrain terrain = null;
			//for(Terrain t : terrains) {
			//	if(t.getType() == terrainType) {
			//		terrain = t;
			//		break;
			//	}
			//}
			return terrain;
		}
		
		private final Border highlightBorder = BorderFactory.createLineBorder(Color.WHITE, 2);
		
		//public Tile(Point location, Terrain terrain, ImagePanel imagePanel) {
		public Tile(Point location, Terrain terrain, JComponent fillerBox, Container boardGrid, int gridIndex) {
			this.location = location;
			//this.terrainType = terrain.type;
			this.terrain = terrain;
			
			this.fillerBox = fillerBox;
			currentComponent = this.fillerBox;
			this.imagePanel = new ImagePanel(tileOverlayPath);
			this.imagePanel.setOpaque(false);
			this.imagePanel.setBackground(new Color(0,0,0,0));
			this.imagePanel.setSize(MapLocationPanel.GetAdjustedTileSize(), MapLocationPanel.GetAdjustedTileSize());
			this.imagePanel.setPreferredSize(new Dimension(MapLocationPanel.GetAdjustedTileSize(), MapLocationPanel.GetAdjustedTileSize()));
			this.imagePanel.setMaximumSize(new Dimension(MapLocationPanel.GetAdjustedTileSize(), MapLocationPanel.GetAdjustedTileSize()));
			SetupGridTile(location, this.imagePanel);
			
			this.boardGrid = boardGrid;
			this.gridIndex = gridIndex;
		}
		
		public void SetOccupant(CharacterBase occupant) {
			this.occupant = occupant;
			
			if(isSelectingCharacter && isPlacementSlot)
				isSelectingCharacter = false;
			ToggleHighlight(false);
		}
		
		public void ToggleHighlight(boolean isOn) {
			isHighlighted = isOn;
			if(isOn)
				currentComponent.setBorder(highlightBorder);
			else if(isPlacementSlot)
				currentComponent.setBorder(placementSlotBorder);
			else if(isOccupantsTurn)
				currentComponent.setBorder(turnTakerBorder);
			else
				currentComponent.setBorder(null);
		}
		
		private Border attackRadiusBorder = BorderFactory.createLineBorder(Color.RED, 1);
		
		public void ToggleAttackRadiusHighlight(boolean enabled) {
			fillerBox.setBorder(enabled ? attackRadiusBorder : null);
			imagePanel.setBorder(enabled ? attackRadiusBorder : null);
			
			if(enabled && !dirtyTiles.contains(this))
				dirtyTiles.add(this);
		}
		
		private boolean isPlacementSlot;
		private final Color placementSlotColor = new Color(0f, 0.2f, 0.2f, 1f);
		private final Border placementSlotBorder = BorderFactory.createLineBorder(Color.BLUE, 2);
		private boolean isOccupantsTurn;
		private final Border turnTakerBorder = BorderFactory.createLineBorder(Color.GREEN, 2);
		private final Color moveColor = new Color(0.1f, 0.1f, 0.7f, 1f);
		
		//Switch to ImagePanel component when using any kind of Tint operation
		private void SwapComponent(boolean switchToOverlayImage) {
			JComponent oldComp = currentComponent;
			currentComponent = switchToOverlayImage ? imagePanel : fillerBox;
			if(oldComp == currentComponent)
				return;
			
			Component installedComp = boardGrid.getComponent(gridIndex);
			boardGrid.remove(installedComp);
			boardGrid.add(currentComponent, gridIndex);
		}
		
		private final Color pathColor = new Color(0f, 0.5f, 0f, 1f);
		private final Color pathEndColor = new Color(0.25f, 0.25f, 0f, 1f);
		
		public void TogglePlacementSlot(boolean enabled) {
			isPlacementSlot = enabled;
			SwapComponent(enabled);
			if(enabled)
				imagePanel.SetTint(placementSlotColor);
			else
				imagePanel.ClearTint();
			currentComponent.setBorder(enabled ? placementSlotBorder : null);
			
			if(enabled && !dirtyTiles.contains(this))
				dirtyTiles.add(this);
		}
		
		public void ToggleTurnTaker(boolean enabled) {
			isOccupantsTurn = enabled;
			currentComponent.setBorder(enabled ? turnTakerBorder : null);
			
			//this is needed for when AI is taking turns
			currentComponent.repaint();
		}
		
		public void ToggleMoveTint(boolean enabled) {
			SwapComponent(enabled || (Game.Instance().IsPlacementPhase() && isPlacementSlot));
			if(enabled)
				imagePanel.SetTint(moveColor);
			else if(Game.Instance().IsPlacementPhase() && isPlacementSlot)
				imagePanel.SetTint(placementSlotColor);
			else
				imagePanel.ClearTint();
			currentComponent.repaint();
			
			if(enabled && !dirtyTiles.contains(this))
				dirtyTiles.add(this);
		}
		
		public void TogglePathTint(boolean enabled, boolean isDestination) {
			SwapComponent(true);
			if(enabled)
				imagePanel.SetTint(isDestination ? pathEndColor : pathColor);
			else
				imagePanel.SetTint(moveColor);
			currentComponent.repaint();
		}
		
		public void LayerAIObjectiveMoves(boolean enabled) {
			SwapComponent(enabled);
			if(enabled) {
				Color color = imagePanel.GetTintColor() != null ? imagePanel.GetTintColor() : moveColor;
				imagePanel.SetTint(new Color(color.getRed() + 15, color.getGreen(), color.getBlue()));
			} else {
				imagePanel.ClearTint();
			}
			currentComponent.repaint();
			
			if(enabled && !dirtyTiles.contains(this))
				dirtyTiles.add(this);
		}
		
		//Attack/Ability/ItemUse Target Tinting
		private Color targetColor = Color.GREEN.brighter();
		private Color targetSelectionColor = Color.RED;
		
		public void ToggleTargetTint(boolean enabled) {
			SwapComponent(enabled);
			if(enabled)
				imagePanel.SetTint(targetColor);
			else
				imagePanel.ClearTint();
			currentComponent.repaint();
			
			if(enabled && !dirtyTiles.contains(this))
				dirtyTiles.add(this);
		}
		
		public void ToggleTargetSelectionTint(boolean enabled) {
			SwapComponent(true);
			imagePanel.SetTint(enabled ? targetSelectionColor : targetColor);
			currentComponent.repaint();
		}
	}
	
	public class Terrain {
		//public Terrain(TerrainType type, String imagePath, int movementPenalty, boolean isPassable) {
		public Terrain(TerrainType type, int movementPenalty, boolean isPassable) {
			this.type = type;
			//this.imagePath = terrainTilePath + imagePath;
			this.movementPenalty = movementPenalty;
			this.isPassable = isPassable;
		}
		
		public TerrainType getType() {
			return type;
		}
		public int getMovementPenalty() {
			return movementPenalty;
		}
		public boolean isPassable() {
			return isPassable;
		}
		private TerrainType type;
		//private final String terrainTilePath = "terrainTile/";
		//private String imagePath;
		private int movementPenalty;
		private boolean isPassable;
	}
	
	public enum TerrainType { GRASS, SAND, STONE, HOUSE, CASTLE, SHRINE, WATER, FOREST, MOUNTAIN };
	
	/*Terrain[] terrains = new Terrain[] {
		//Ordinary terrain
		new Terrain(TerrainType.GRASS, "Grass.png", 1, true),
		new Terrain(TerrainType.SAND, "Sand.png", 2, true),
		new Terrain(TerrainType.STONE, "Stone.png", 1, true),
		//Special effect terrain
		new Terrain(TerrainType.HOUSE, "House.png", 1, true),
		new Terrain(TerrainType.CASTLE, "Castle.png", 1, true),
		new Terrain(TerrainType.SHRINE, "Shrine.png", 1, true),
		//Penalizing terrain
		new Terrain(TerrainType.RIVER, "River.png", 4, false),
		new Terrain(TerrainType.TREE, "Tree.png", 2, true),
		new Terrain(TerrainType.MOUNTAIN, "Mountain.png", 3, true)
	};*/

	public Tile GetTileAt(Point location) {
		if(location.x < 0 || location.x >= boardDimensions.width || location.y < 0 || location.y >= boardDimensions.height) {
			if(debugPathfinding)
				System.err.println("Board.GetTileAt(" + location + ") : is invalid. Returning null. boardDimensions: " + boardDimensions.width + ", " + boardDimensions.height);
			return null;
		} else {
			int attemptedIndex = location.y * boardDimensions.width + location.x;
			if(attemptedIndex < tiles.size())
				return tiles.get(attemptedIndex);
			else {
				System.err.println("Board.GetTileAt(" + location + ") - Tried getting tile at invalid index: " + attemptedIndex + " beyond size: " + tiles.size());
				return null;
			}
		}
	}
	
	private void FocusOnTile(Point location) {
		if(isSelectingCharacter || battlePanel.IgnoringMouseEvents()) {
			//System.out.println("Board.FocusOnTile() - Returning because either isSelectingCharacter: " + isSelectingCharacter + " or battlePanel.IgnoreMouseEvents(): " + battlePanel.IgnoringMouseEvents());
			return;
		}
		
		Tile tile = GetTileAt(location);
		
		tile.ToggleHighlight(true);
		
		//battlePanel.ToggleTerrainPanel(true, tile.GetTerrain(), location);
		battlePanel.ToggleTerrainPanel(true, GetSceneTileData(tile.Location()), location);
		
		CharacterBase activeChar = Game.Instance().GetActiveBattleCharacter();
		
		//Don't interfere with AI stuff
		if(activeChar != null && !Game.Instance().GetAllyCharacterList().contains(activeChar)) {
			//System.out.println("Board.FocusOnTile() - Returning because this is an AI turn.");
			return;
		}
		
		//System.out.println("Board.FocusOnTile()");
		InteractiveActionType actionType = null;
		switch(battlePanel.GetCurrentTurnActionState()) {
			case ActionMenu:
				//Nothing needs to happen here
				break;
				
			case MoveSelection:
				if(activeChar.GetPaths().containsKey(tile)) {
					//System.out.println("Show Path");
					for(Tile pathTile : activeChar.GetPaths().get(tile)) {
						pathTile.TogglePathTint(true, pathTile == tile);
						//System.out.println("    -Tile: " + pathTile.Location().toString());
					}
				}
				break;
				
			case AttackSelection:
				if(attackableTiles.contains(tile)) {
					actionType = InteractiveActionType.MainAttack;
					
					//GetTileAt(location).TogglePathTint(true, true);
					GetTileAt(location).ToggleTargetSelectionTint(true);
					HideAllButFocusedTargetTile(tile);
					
					//The Y axis is inverted because of the flipped coordinate system
					Point direction = new Point(Math.max(Math.min(1, location.x - activeChar.getLocation().x), -1), Math.max(Math.min(1, activeChar.getLocation().y - location.y), -1));
					battlePanel.UpdateDirection(direction);
				}
				break;
				
			case AbilityMenu:
				//Nothing needs to happen here
				break;
			case AbilitySelection:
				if(attackableTiles.contains(tile)) {
					actionType = InteractiveActionType.Ability;
					//GetTileAt(location).TogglePathTint(true, true);
					GetTileAt(location).ToggleTargetSelectionTint(true);
					
					//The Y axis is inverted because of the flipped coordinate system
					Point direction = new Point(Math.max(Math.min(1, location.x - activeChar.getLocation().x), -1), Math.max(Math.min(1, activeChar.getLocation().y - location.y), -1));
					battlePanel.UpdateDirection(direction);
					
					HideAllButFocusedTargetTile(tile);
				}
				break;
				
			case ItemMenu:
				//Nothing needs to happen here
				break;
			case ItemSelection:
				if(attackableTiles.contains(tile)) {
					actionType = InteractiveActionType.UseItem;
					//GetTileAt(location).TogglePathTint(true, true);
					GetTileAt(location).ToggleTargetSelectionTint(true);
					
					//The Y axis is inverted because of the flipped coordinate system
					Point direction = new Point(Math.max(Math.min(1, location.x - activeChar.getLocation().x), -1), Math.max(Math.min(1, activeChar.getLocation().y - location.y), -1));
					battlePanel.UpdateDirection(direction);
					
					HideAllButFocusedTargetTile(tile);
				}
				break;
				
			case WaitSelection:
				if(shownDirectionalTiles.contains(tile)) {
					GetTileAt(location).TogglePathTint(true, true);
					//The Y axis is inverted because of the flipped coordinate system
					Point direction = new Point(location.x - activeChar.getLocation().x, activeChar.getLocation().y - location.y);
					battlePanel.UpdateDirection(direction);
				}
				break;
			default:
				System.err.println("Board.FocusOnTile() - Add support for: " + battlePanel.GetCurrentTurnActionState());
				break;
		}
		
		
		//Show Damage and hit chances
		//TODO this needed to be ignored once the attack has started
		if(actionType != null) {
			List<CharacterBase> targetedCharacters = GetTargetsFromTile(tile);
			if(targetedCharacters != null && targetedCharacters.size() > 0)
				battlePanel.ShowTargets(actionType, targetedCharacters);
		}
		
		if(tile.Occupant() != null) {
			battlePanel.ShowCharacterCard(tile.Occupant().GetData(), location);
			
			if(Game.Instance().IsPlacementPhase() || (tile.Occupant() != activeChar && battlePanel.GetCurrentTurnActionState() == CharacterTurnActionState.ActionMenu)) {
				charWithMovesShowing = tile.Occupant();
				ShowMoves(tile.Occupant());
			}
		} else {
			battlePanel.HideCharacterCard();
		}
	}
	
	private void HideAllButFocusedTargetTile(Tile focusedTile) {
		List<Tile> focusedRadiiTiles = this.attackableTilesWithRadii.get(focusedTile);
		for(Tile bulkTile : attackRadiiTiles) {
			if(!focusedRadiiTiles.contains(bulkTile))
				bulkTile.ToggleAttackRadiusHighlight(false);
		}
	}
	
	private void ShowAllTargetTilesAgain() {
		for(Tile bulkTile : attackRadiiTiles) {
			bulkTile.ToggleAttackRadiusHighlight(true);
		}
	}
	
	public void ClearTargetTiles() {
		attackableTilesWithRadii.clear();
		attackRadiiTiles.clear();
	}
	
	//Use this to know whn to hide a highlighted character's moves during LoseFocusOnTile
	private CharacterBase charWithMovesShowing;
	
	private void LoseFocusOnTile(Point location) {
		if(isSelectingCharacter || battlePanel.IgnoringMouseEvents())
			return;
		
		Tile tile = GetTileAt(location);
		
		tile.ToggleHighlight(false);
		
		battlePanel.ToggleTerrainPanel(false, null, null);
		battlePanel.HideCharacterCard();
		
		CharacterBase activeChar = Game.Instance().GetActiveBattleCharacter();
		
		//Don't interfere with AI stuff
		if(activeChar != null && !Game.Instance().GetAllyCharacterList().contains(activeChar))
			return;
		
		switch(battlePanel.GetCurrentTurnActionState()) {
			case ActionMenu:
				//Nothing need to happen here
				break;
				
			case MoveSelection:
				if(activeChar.GetPaths().containsKey(tile)) {
					//System.out.println("Hide Path");
					for(Tile pathTile : activeChar.GetPaths().get(tile)) {
						pathTile.TogglePathTint(false, false);
						//System.out.println("    -Tile: " + pathTile.Location().toString());
					}
				}
				break;
				
			case AttackSelection:
				if(attackableTiles.contains(tile)) {
					//GetTileAt(location).TogglePathTint(false, false);
					GetTileAt(location).ToggleTargetSelectionTint(false);
					ShowAllTargetTilesAgain();
				}
				break;
				
			case AbilityMenu:
				//Nothing needs to happen here
				break;
			case AbilitySelection:
				if(attackableTiles.contains(tile)) {
					GetTileAt(location).ToggleTargetSelectionTint(false);
					ShowAllTargetTilesAgain();
				}
				break;
				
			case ItemMenu:
				//Nothing needs to happen here
				break;
			case ItemSelection:
				if(attackableTiles.contains(tile)) {
					GetTileAt(location).ToggleTargetSelectionTint(false);
					ShowAllTargetTilesAgain();
				}
				break;
				
			case WaitSelection:
				if(shownDirectionalTiles.contains(tile)) {
					GetTileAt(location).TogglePathTint(false, false);
				}
				break;
			default:
				System.err.println("Board.LoseFocusOnTile() - Add support for: " + battlePanel.GetCurrentTurnActionState());
				break;
		} 
		
		//Reset intensional effect highlighting the turntaker
		if(tile.occupant != null) {
			
			//if(Game.Instance().IsPlacementPhase() || tile.occupant != activeChar) {
			if(Game.Instance().IsPlacementPhase() || tile.occupant == charWithMovesShowing) { //Check against charWithMovesShowing acquired from FocusOnTile
				charWithMovesShowing = null;
				
				System.out.println("Board.LoseFocusOnTile() - Hiding moves for: " + tile.Occupant().GetData().getName());
				for(Tile destination : tile.Occupant().GetPaths().keySet()) {
					if(currentlyDisplayedMoveTiles.contains(destination))
						continue;
					destination.ToggleMoveTint(false);
				}
				
				//Re-enabled those tiles hidden when viewing another character's tiles
				for(Tile displayedTile : currentlyDisplayedMoveTiles)
					displayedTile.ToggleMoveTint(true);
			}
		}
		
		battlePanel.HideTargets();
		
		//Reset intensional effect highlighting the attack aoe radius
		CharacterTurnActionState state = battlePanel.GetCurrentTurnActionState();
		if(
			(state == CharacterTurnActionState.AbilityMenu || state == CharacterTurnActionState.AbilitySelection ||
					state == CharacterTurnActionState.ItemMenu || state == CharacterTurnActionState.ItemSelection)
			&&
			attackRadiiTiles.contains(tile)
		) {
			tile.ToggleAttackRadiusHighlight(true);
		}
	}
	
	private boolean isSelectingCharacter;
	
	private void ClickTile(Point location) {
		if(Game.Instance().IsPlacementPhase()) {
			if(isSelectingCharacter || battlePanel.IsPlacementTileAForcedType(location))
				return;

			Tile tile = GetTileAt(location);
			if(tile.isPlacementSlot && (!battlePanel.IsSelectionMaxedOut() || (battlePanel.IsSelectionMaxedOut() && tile.Occupant() != null))) {
				System.out.println("Click Placement Tile");
				isSelectingCharacter = true;
				battlePanel.ToggleCharacterSelection(true, location);
			}
		} else {
			//Keep from interefering during animations and AI turns
			if(battlePanel.IgnoringMouseEvents() || !Game.Instance().GetAllyCharacterList().contains(Game.Instance().GetActiveBattleCharacter()))
				return;
			
			Tile tile = GetTileAt(location);
			CharacterBase activeChar = Game.Instance().GetActiveBattleCharacter();
			switch(battlePanel.GetCurrentTurnActionState()) {
				case ActionMenu:
					//Nothing need to happen here
					break;
				case MoveSelection:
					if(activeChar.GetPaths().keySet().contains(tile)) {
						System.out.println("ClickTile() - Move to tile: " + tile.Location());
						List<Tile> movePath = new ArrayList<Tile>(activeChar.GetPaths().get(tile));
						GetTileAt(activeChar.getLocation()).ToggleTurnTaker(false);
						
						ResetDirtyHighlights();
						
						//Keep the board from fucking with logic that could affect the current animation
						battlePanel.ToggleIgnoreMouseEvents(true);
						
						battlePanel.MoveAlongPath(movePath);
						
						System.out.println("CLEAR DISPLAYED TILES");
						currentlyDisplayedMoveTiles.clear();
					}
					break;
				case AttackSelection:
					if(attackableTiles.contains(tile)) {
						if(tile.Occupant() == null) {
							System.err.println("Board.ClickTile() - Feedback Stub (attackable tile has no occupant)");
							break;
						}
						
						System.out.println("Board.ClickTile() - Picked Attack Tile: " + activeChar.GetData().getName());
						
						List<CharacterBase> targetedCharacters = GetTargetsFromTile(tile);
						
						if(targetedCharacters.size() == 0) {
							System.err.println("Board.ClickTile() - Feedback Stub (attackable tile has no occupant)");
							break;
						}
						
						ResetDirtyHighlights();
						
						//Keep the board from fucking with logic that could affect the current animation
						battlePanel.ToggleIgnoreMouseEvents(true);
						
						Game.Instance().DoAttack(activeChar, targetedCharacters);
					}
					break;
					
				case AbilityMenu:
					//Nothing needed here
					break;
				case AbilitySelection:
					if(attackableTiles.contains(tile)) {
						List<CharacterBase> targetedCharacters = GetTargetsFromTile(tile);
						
						if(targetedCharacters.size() == 0) {
							System.err.println("Board.ClickTile() - Feedback Stub (attackable tile has no occupant)");
							break;
						}
						
						System.out.println("Board.ClickTile() - An Ability Tile was chosen by CharBase: " + activeChar.GetData().getName());
						
						ResetDirtyHighlights();
						
						//Keep the board from fucking with logic that could affect the current animation
						battlePanel.ToggleIgnoreMouseEvents(true);
						
						Game.Instance().DoAbility(activeChar, battlePanel.getUserChosenAbility(), targetedCharacters);
					}
					break;
					
				case ItemMenu:
					//Nothing needed here
					break;
				case ItemSelection:
					if(attackableTiles.contains(tile)) {
						List<CharacterBase> targetedCharacters = GetTargetsFromTile(tile);
						
						if(targetedCharacters.size() == 0) {
							System.err.println("Board.ClickTile() - Feedback Stub (attackable tile has no occupant)");
							break;
						}
						
						System.out.println("Board.ClickTile() - An ItemUse Tile was chosen by CharBase: " + activeChar.GetData().getName());
						
						ResetDirtyHighlights();
						
						//Keep the board from fucking with logic that could affect the current animation
						battlePanel.ToggleIgnoreMouseEvents(true);
						
						Game.Instance().DoItem(activeChar, battlePanel.getUserChosenItem(), targetedCharacters);
					}
					break;
					
				case WaitSelection:
					if(shownDirectionalTiles.contains(tile)) {
						System.out.println("ClickTile() - Picked Direction: " + activeChar.GetData().getName());
						
						ResetDirtyHighlights();
						
						battlePanel.CompleteDirectionSelection();
					}
					break;
				default:
					System.err.println("Board.ClickTile() - Add support for: " + battlePanel.GetCurrentTurnActionState());
					break;
			}
		}
	}
	
	public void MoveAI(CharacterBase charBase, Tile tile) {
		System.out.println("Board.MoveAI(" + charBase.GetData().getName() + ", " + tile.location + ")");
		List<Tile> movePath = new ArrayList<Tile>(charBase.GetPaths().get(tile));
		GetTileAt(charBase.getLocation()).ToggleTurnTaker(false);
		
		//ResetHighlights();
		//This method is really intensive now that the battle dimensions are much larger
		ResetDirtyHighlights();
		
		battlePanel.MoveAlongPath(movePath);
	}
	
	public void SetAIDirection(Point direction) {
		System.out.println("SetAIDirection() - direction: " + direction);
		
		//ResetHighlights();
		//This method is really intensive now that the battle dimensions are much larger
		ResetDirtyHighlights();
		
		battlePanel.UpdateDirection(direction);

		CalcAllMoves();
	}
	
	public void ResetHighlights() {
		for(Tile tile : tiles) {
			tile.TogglePlacementSlot(false);
			tile.ToggleHighlight(false);
			tile.ToggleMoveTint(false);
		}
	}
	
	List<Tile> dirtyTiles = new ArrayList<Tile>();
	public void ResetDirtyHighlights() {
		for(Tile tile : dirtyTiles) {
			tile.TogglePlacementSlot(false);
			tile.ToggleHighlight(false);
			tile.ToggleMoveTint(false);
			
			tile.ToggleAttackRadiusHighlight(false);
		}
		dirtyTiles.clear();
	}
	
	
	//__ Movement Logic __\\
	
	private final boolean debugPathfinding = false;
	
	private class ForkLayer {
		public ForkLayer(Tile fork) {
			ForkedTile = fork;
		}
		
		//Hierarchy
		public ForkLayer ParentLayer = null;
		public List<ForkLayer> ChildLayers = new ArrayList<ForkLayer>();
		//Data
		public Tile ForkedTile;
		public List<Tile> TestedForkChoices = new ArrayList<Tile>();
	}
	
	private class MoveResult {
		private boolean canMoveThruTile;
		private int movementsRemaining;
	}
	
	private MoveResult CanMoveToTile(Tile tile, int movementsRemaining, int charsTilePenaltyMod, ForkLayer forkLayer, List<Tile> traversedTiles) {
		MoveResult moveResult = new MoveResult();
		Tile currentTile = traversedTiles.get(traversedTiles.size()-1);
		Tile previousTile = null;
		if(traversedTiles.size() > 1)
			previousTile = traversedTiles.get(traversedTiles.size()-2);
		
		//moveResult.movementsRemaining = movementsRemaining - GetSceneTileData(tile.Location()).penalty;
		//This is probably the best place to account for the tilepenalty and characters penalty mod
		int finalPenalty = Math.max(1, GetSceneTileData(tile.Location()).penalty + charsTilePenaltyMod);
		moveResult.movementsRemaining = movementsRemaining - finalPenalty;
		
		boolean isNotPreviousTile = tile != previousTile;
		//boolean isPassable = tile.GetTerrain().isPassable();
		boolean isPassable = GetSceneTileData(tile.Location()).isPassable;
		boolean isOnSameTeam = (Game.Instance().GetAllyCharacterList().contains(calcingCharBase) ? Game.Instance().GetAllyCharacterList() : Game.Instance().GetEnemyCharacterList()).contains(tile.Occupant());
		boolean isNotOccupiedByEnemyOrImpassableAlly = 
			(
		    	tile.Occupant() == null
		    	||
		    	(tile.Occupant() != null && isOnSameTeam && moveResult.movementsRemaining > 0)
		    );
		boolean isAffordable = moveResult.movementsRemaining >= 0;
		boolean isNotTestedForkChoice = 
			(
		    	forkLayer == null
		    	||
		    	(
		    		forkLayer != null
		    		&&
		    		(
		    			forkLayer.ForkedTile != currentTile
		    			||
		    			(forkLayer.ForkedTile == currentTile && !forkLayer.TestedForkChoices.contains(tile))
		    		)
		    	)
		    );
		
		String message = "";
		if(!isNotPreviousTile)
			message += "Is previous tile, ";
		if(!isPassable)
			message += "Impassable, ";
		if(!isNotOccupiedByEnemyOrImpassableAlly)
			message += "Occupied by enemy or impassable ally, ";
		if(!isAffordable)
			message += "Unaffordable, ";
		if(!isNotTestedForkChoice)
			message += "Is Tested Fork Choice";
		if(message != "") {
			if(debugPathfinding)
				System.out.println("            -CanMoveToTile() Fail for " + tile.Location().x + ", " + tile.Location().y + ": " + message);
		}
		
		moveResult.canMoveThruTile =
		isNotPreviousTile
		&&
		isPassable
		&&
		isNotOccupiedByEnemyOrImpassableAlly
	    &&
	    isAffordable
	    &&
	    isNotTestedForkChoice;
		
		return moveResult;
	}
	
	public void CalcAllMoves() {
		if(Game.Instance().IsPlacementPhase()) {
			for(CharacterBase charBase : Game.Instance().GetAllPlacedCharBases()) {
				CalcMoves(charBase);
			}
		} else {
			//This could be made more efficient; for calls to this method after the first
			//CalcMoves for the character that just moved, as usual
			//Then for each other character, check whether the moved character has moved onto a tile in one of its paths
			//Then only update those characters and only their paths that were affected
			//This approach will completely avoid characters who are far enough away and avoid the majority of paths belonging to characters that are affected
			for(CharacterBase charBase : Game.Instance().GetTurnOrderedCharBases())
				CalcMoves(charBase);
		}
	}
	
	//Used by CanMoveToTile during CalcMoves, this could be passed as a parameter to the method but that seems redundant given the number of times its called
	CharacterBase calcingCharBase;
	
	private void CalcMoves(CharacterBase charBase) {
		calcingCharBase = charBase;
		charBase.ClearPaths();
		
		if(debugPathfinding)
			System.out.println("Board.CalcMoves() - Starting calculations for: " + charBase.GetData().getName());
		
		//Tint all available move tiles
		int xLoc = charBase.getLocation().x;
		int yLoc = charBase.getLocation().y;
		int range = charBase.GetData().GetMoveRange();
		List<Tile> potentialTiles = new ArrayList<Tile>();
		
		if(debugPathfinding)
			System.out.println("Character start location - " + xLoc + ", " + yLoc);
		
		Point minPoint = new Point(Math.max(0, xLoc - range), Math.max(0, yLoc - range));
		Point maxPoint = new Point(Math.min(boardDimensions.width, xLoc + range + 1), Math.min(boardDimensions.height, yLoc + range + 1));
		//Point maxPoint = new Point(Math.min(Board.BoardSize() - 1, xLoc + range), Math.min(Board.BoardSize() - 1, yLoc + range));
		if(debugPathfinding)
			System.out.println("Roughly ranging movement - range: " + minPoint.x + ", " + minPoint.y + " to " + maxPoint.x + ", " + maxPoint.y);
	
		for(int x = minPoint.x; x < maxPoint.x; x++) {
			for(int y = minPoint.y; y < maxPoint.y; y++) {
				int distance = Math.abs(xLoc - x) + Math.abs(yLoc - y);
				Tile tile = GetTileAt(new Point(x, y));
				
				boolean isInRange = distance <= range;
				boolean isUnoccupied = tile.Occupant() == null;
				boolean isTilePassable = GetSceneTileData(tile.Location()).isPassable;
				
				//if(distance <= range && tile.Occupant() == null && tile.GetTerrain().isPassable()) {
				if(isInRange && isUnoccupied && isTilePassable) {
					
					if(debugPathfinding)
						System.out.println(x + ", " + y + " - VALID - In Range: " + distance + " <= " + range);
					potentialTiles.add(tile);
				} else {
					if(debugPathfinding) {
						if(!isInRange)
							System.out.println(x + ", " + y + " - NOT VALID - Out of Range: " + distance + " <= " + range);
						else if(!isUnoccupied)
							System.out.println(x + ", " + y + " - NOT VALID - Occupied by: " + tile.Occupant().GetData().getName());
						else
							System.out.println(x + ", " + y + " - NOT VALID - Impassable Tile");
					}
				}
			}
		}
		
		//Now that we've got all potential movement tiles, calculate pathing to each
		//For each potentialTile, test it as a destination. Either there will be a clear path to it and it'll be added to paths
		//or the pathfinding algorithm will try to find its way around obstacles and settle on other tiles along the way, 
		//then it'll add the path for the settled tiles to paths and it'll retire all settled tiles and the original destination tile 
		//by removing them from potentialTiles
		//Then it'll move on to the next remaining potentialTile and repeat the process
		Map<Tile, List<Tile>> paths = new HashMap<Tile, List<Tile>>();
		final int loopIterationCap = 52;
		boolean abortPathfinding = false;
		boolean areAllDirectionsBlocked = false;
		//Try to reach each potentialTile using the pathfinding algorithm
		while(potentialTiles.size() > 0) {
			//Get the next destination
			Tile destination = potentialTiles.get(0);
			if(debugPathfinding)
				System.out.println("Starting Path for Destination: " + destination.Location().toString());
			
			List<Tile> workingPath = new ArrayList<Tile>();
			int moveRangeRemaining = range;
			
			List<Tile> traversedTiles = new ArrayList<Tile>();
			ForkLayer currentForkLayer = null;
			
			//Tracks whether any point of the path has not been in an ideal direction
			boolean hasCompromisedIdealPath = false;
			//Tracks whether any point in the path has moved in the opposite direction of the destination
			boolean hasCompromisedCriticalPath = false;
			
			//Do procedural pathfinding
			int loopSafetyIndex = 0;
			//Keep pathfinding until we've reached the destination or until we've exhausted all options, and then break
			while(true) {
				loopSafetyIndex++;
				if(loopSafetyIndex >= loopIterationCap) {
					abortPathfinding = true;
					break;
				}
				
				if(debugPathfinding)
					System.out.println("    Pathfinding Loop #" + loopSafetyIndex);
				
				//Get the tile we're moving from whether it be the start location or the last workingTile
				Tile lastTile = null;
				if(workingPath.size() == 0) {
					lastTile = GetTileAt(new Point(xLoc, yLoc));
					if(debugPathfinding)
						System.out.println("        -lastPoint is start location: " + lastTile.Location().toString());
				} else {
					lastTile = workingPath.get(workingPath.size() - 1);
					if(debugPathfinding)
						System.out.println("        -lastPoint is workingTile.Location: " + lastTile.Location().toString());
				}
				Point lastPoint = lastTile.Location();
				traversedTiles.add(GetTileAt(lastPoint));
				
				int xDirection = Math.min(1, Math.max(destination.Location().x - lastPoint.x, -1));
				int yDirection = Math.min(1, Math.max(destination.Location().y - lastPoint.y, -1));
				if(debugPathfinding)
					System.out.println("        -Direction to destination: " + xDirection + ", " + yDirection);
				
				//Positive value means prioritize x axis, 0 means diagonal, negative value means prioritize y axis
				int axisPriority = Math.abs(destination.Location().x - lastPoint.x) - Math.abs(destination.Location().y - lastPoint.y);
				String axisName = "X Axis";
				if(axisPriority == 0)
					axisName = "Either Axis";
				else if(axisPriority < 0)
					axisName = "Y Axis";
				if(debugPathfinding)
					System.out.println("        -prioritize: " + axisName);
				
				Tile[] idealTiles = new Tile[2];
				Tile[] secondBestTiles = new Tile[2];
				Tile lastResortTile = null;
				
				if(axisPriority > 0) {
					idealTiles[0] = GetTileAt(new Point(lastPoint.x + xDirection, lastPoint.y));
					secondBestTiles[0] = GetTileAt(new Point(lastPoint.x, lastPoint.y - 1));
					secondBestTiles[1] = GetTileAt(new Point(lastPoint.x, lastPoint.y + 1));
					if(lastPoint.equals(new Point(xLoc, yLoc)))
						lastResortTile = GetTileAt(new Point(lastPoint.x - xDirection, lastPoint.y));
				} else if(axisPriority == 0) {
					idealTiles[0] = GetTileAt(new Point(lastPoint.x + xDirection, lastPoint.y));
					idealTiles[1] = GetTileAt(new Point(lastPoint.x, lastPoint.y + yDirection));
					secondBestTiles[0] = GetTileAt(new Point(lastPoint.x - xDirection, lastPoint.y));
					if(traversedTiles.contains(secondBestTiles[0]))
						secondBestTiles[0] = null;
					secondBestTiles[1] = GetTileAt(new Point(lastPoint.x, lastPoint.y - yDirection));
					if(traversedTiles.contains(secondBestTiles[1]))
						secondBestTiles[1] = null;
				} else {
					idealTiles[0] = GetTileAt(new Point(lastPoint.x, lastPoint.y + yDirection));
					secondBestTiles[0] = GetTileAt(new Point(lastPoint.x - 1, lastPoint.y));
					secondBestTiles[1] = GetTileAt(new Point(lastPoint.x + 1, lastPoint.y));
					if(lastPoint.equals(new Point(xLoc, yLoc)))
						lastResortTile = GetTileAt(new Point(lastPoint.x, lastPoint.y - yDirection));
				}
				
				//Fork rollback, if there are no remaining fork options then revert to parent fork
				if(currentForkLayer != null && currentForkLayer.ForkedTile == lastTile) {
					//If we're forking at the start location then consider all four directions, otherwise only check the three surrounding our path
					if( (workingPath.size() == 0 && currentForkLayer.TestedForkChoices.size() == 4)
						||
						(workingPath.size() > 0 && currentForkLayer.TestedForkChoices.size() == 3) )
					{
						currentForkLayer = currentForkLayer.ParentLayer;
					}
				}
				
				//Get the nextTile to inspect, start at players location and try to move directly towards destination, try moving around obstacles
				Tile nextTile = null;
				//Didn't end up being used for anything
				//MoveResult nextResult = null;
				
				//Record any tiles that we removed from potentialTiles as a result of reaching them with 0 movement points left
				List<Tile> movementExtentTiles = new ArrayList<Tile>();
				
				boolean destinationMayBeUnaffordable = false;
				
				boolean isUnreachableDestination = false;
				
				//If an axis is prioritized then run this for loop 3 times for idealTiles[0], secondBestTiles comparison and lastResort
				//i == 0 is used to test idealTile(if it exists), i == 1 is used to test and compare secondBestTiles, i == 2 tests lastResort(only exists when testing from starting tile)
				//Else if its diagonal then run the loop for the idealTiles comparison and the secondBest comparison
				for(int i = 0; i < (idealTiles[1] == null ? 3 : 2); i++) {
					if(i > 0)
						hasCompromisedIdealPath = true;
					
					if((idealTiles[1] == null && i > 1) || (idealTiles[1] != null && i > 0))
						hasCompromisedCriticalPath = true;
					
					//set generic variables based on i value
					Tile focusedTile = null;
					MoveResult moveResult = null;
					boolean compareFirst = (idealTiles[1] == null && i == 1) || idealTiles[1] != null;
					@SuppressWarnings("unused")
					String message = "    -";
					//Set as idealTile
					if(!compareFirst) {
						if(i == 0) {
							focusedTile = idealTiles[0];
							message += "Single Ideal, ";
						} else {
							if(lastResortTile == null) {
								if(debugPathfinding)
									System.out.println("        -lastResortTile == null. Breaking.");
								break;
							}
							focusedTile = lastResortTile;
							message += "Last Resort, ";
						}
					} else {
						//Pick from secondBest options
						Tile option1 = null;
						Tile option2 = null;
						if(i == 0) {
							option1 = idealTiles[0];
							option2 = idealTiles[1];
							message += "Double Ideal, ";
						} else {
							option1 = secondBestTiles[0];
							option2 = secondBestTiles[1];
							message += "Double Second Best, ";
						}
						
						boolean neither = false;
						MoveResult result1 = null;
						if(option1 != null)
							result1 = CanMoveToTile(option1, moveRangeRemaining, charBase.getTilePenaltyMod(), currentForkLayer, traversedTiles);
						MoveResult result2 = null;
						if(option2 != null)
							result2 = CanMoveToTile(option2, moveRangeRemaining, charBase.getTilePenaltyMod(), currentForkLayer, traversedTiles);
						if(option1 == null) {
							if(option2 != null)
								focusedTile = option2;
							else
								neither = true;
						} else if(option2 == null) {
							focusedTile = option1;
						} else {
							if(!result1.canMoveThruTile && !result2.canMoveThruTile) {
								neither = true;
							} else if(!result2.canMoveThruTile) {
								focusedTile = option1;
								moveResult = result1;
							} else if(!result1.canMoveThruTile) {
								focusedTile = option2;
								moveResult = result2;
							} else { //Choose the tile with the smaller penalty
								if(result1.movementsRemaining > result2.movementsRemaining) {
									focusedTile = option1;
									moveResult = result1;
								} else {
									focusedTile = option2;
									moveResult = result2;
								}
							}
						}
						if(neither) {
							if(debugPathfinding)
								System.out.println("        -Neither tile can be moved thru. Continuing.");
							continue;
						}
					}
					if(debugPathfinding)
						System.out.println(message + "focusedTile.location: " + focusedTile.Location());
					
					//Check nextTile's penalty against our remaining movement count
					if(moveResult == null)
						moveResult = CanMoveToTile(focusedTile, moveRangeRemaining, charBase.getTilePenaltyMod(), currentForkLayer, traversedTiles);
					if(moveResult.canMoveThruTile) {
						//If we run out of movements before reaching destination then record path to that tile and revert to last fork or deadend
						//if(moveResult.movementsRemaining == 0 && !focusedTile.Location().equals(destination.Location())) {
						if(moveResult.movementsRemaining == 0 && !focusedTile.Location().equals(destination.Location())) {
							movementExtentTiles.add(focusedTile);
							
							//Record path to focusedTile and then try another route in the next flow control statement, if there was a previous fork	
							if(!paths.containsKey(focusedTile) && !hasCompromisedCriticalPath) {
								List<Tile> extentWorkingPath =  new ArrayList<Tile>(workingPath);
								extentWorkingPath.add(focusedTile);
								paths.put(focusedTile, extentWorkingPath);
								potentialTiles.remove(focusedTile);
								if(debugPathfinding)
									System.out.println("        &-Reached end of movement range, adding valid path to: " + focusedTile.Location().toString() + ", with path tile count: " + extentWorkingPath.size() + ", trying other routes.");
							} else {
								if(debugPathfinding)
									System.out.println("        -Reached end of movement range, path has already been recorded or the ideal path has been compromised, trying other routes.");
							}
						} else {
							//We should choose the first traversable tile because our loop hierarchy provides tiles prioritized from greatest to least
							if(debugPathfinding)
								System.out.println("        -Found suitable focusedTile to set as nextTile. Breaking.");
							nextTile = focusedTile;
							//Didn't end up being used for anything
							//nextResult = moveResult;
						}
					} else {
						if(debugPathfinding)
							System.out.println("        -Cannot move thru focusedTile: " + focusedTile.Location().toString());
						
						if(moveResult.movementsRemaining < 0 && focusedTile == destination && !hasCompromisedIdealPath) {
							if(debugPathfinding)
								System.out.println("        -destinationMayBeUnaffordable");
							destinationMayBeUnaffordable = true;
						}
					}
					
					if(nextTile != null || destinationMayBeUnaffordable) {
						break;
					} else if(i == 0) {
						int distance = Math.abs(xLoc - destination.Location().x) + Math.abs(yLoc - destination.Location().y);
						if(distance >= range) {
							if(debugPathfinding)
								System.out.println("        -Destination is unreachable at distance: "+ distance +". Abandoning.");
							isUnreachableDestination = true;
							break;
						} else {
							if(debugPathfinding)
								System.out.println("        -Distance is possible: " + distance);
						}
					}
				}
				
				if(isUnreachableDestination)
					break;
				
				if(nextTile != null) {
					//Add nextTile to working path and continue pathignfinding along this path
					workingPath.add(nextTile);
					//moveRangeRemaining -= nextTile.GetTerrain().getMovementPenalty();
					moveRangeRemaining -= GetSceneTileData(nextTile.Location()).penalty;
					
					if(nextTile.Location().equals(destination.Location())) {
						paths.put(destination, workingPath);
						if(debugPathfinding)
							System.out.println("    *-Path Complete, adding nextTile to workingPath and destination to paths. Path tile count: " + workingPath.size() + ". Breaking.");
						break;
					} else {
						if(debugPathfinding)
							System.out.println("    *-Adding nextTile to workingPath. Moving on to next Pathfinding iteration.");
					}
				} else {
					//This section is for handling a dead-end
					
					//Record the path to this dead-end as long as its not our starting location
					if(!paths.containsKey(lastTile) && !lastTile.Location().equals(charBase.getLocation())) {
						if(debugPathfinding)
							System.out.println("    *-Recorded path to dead-end location: " + lastTile.Location());
						paths.put(lastTile, workingPath);
						potentialTiles.remove(lastTile);
					}
					
					Tile forkTile = null;
					Tile testedTile = null;
					//if(movementExtentTiles.size() > 0) {
					if(movementExtentTiles.size() > 0 || destinationMayBeUnaffordable) {
						//If we didnt find a nextTile to move to from all four directions but we recorded at least one extent tile then 
						//iterate thru the workingTile list and create a fork before the most expensive tile in the list
						//treat that tile as an obstacle and try to pathfind around it
						
						if(debugPathfinding)
							System.out.println("        -Movement extent reached at tile: " + lastTile.Location().toString() + ". Inspecting path for ineffecient penalties until we hit a previous fork or the starting tile.");
						
						int highestPenalty = 1;
						int highestPenaltyIndex = -1;
						int firstEncounteredForkIndex = -1;
						for(int i = traversedTiles.size() - 1; i > -1; i--) {
							//Ignore the start tile penalty. That tile's penalty is irrelevant because we're already there
							if(i == 0)
								break;
							
							Tile tile = traversedTiles.get(i);
							if(currentForkLayer != null && currentForkLayer.ForkedTile == tile) {
								firstEncounteredForkIndex = i;
								break;
							}
							
							//int penalty = tile.GetTerrain().getMovementPenalty();
							int penalty = GetSceneTileData(tile.Location()).penalty;
							if(penalty > highestPenalty) {
								highestPenalty = penalty;
								highestPenaltyIndex = i;
							}
						}
						
						if(highestPenaltyIndex > -1) {
							//then record the fork at that found location
							forkTile = traversedTiles.get(highestPenaltyIndex - 1);
							//find the tested fork tile for the new forkLayer by getting the index of the forkTile in the WorkingPath and then adding 1
							testedTile = traversedTiles.get(highestPenaltyIndex);
							
							if(debugPathfinding)
								System.out.println("            -highestPenaltyIndex: " + highestPenaltyIndex);
						} else if(firstEncounteredForkIndex > -1) {
							//We encountered a fork before hitting any tiles costing greater than 1
							forkTile = traversedTiles.get(firstEncounteredForkIndex);
							testedTile = traversedTiles.get(firstEncounteredForkIndex + 1);
							
							if(debugPathfinding)
								System.out.println("            -firstEncounteredForkIndex: " + firstEncounteredForkIndex);
						} else {
							if(destinationMayBeUnaffordable) {
								if(debugPathfinding)
									System.out.println("            -Destination is unafforadable even with the ideal path to it. Abandoning destination. Breaking.");
								break;
							} else {
								//We iterated all the way back to our starting point before hitting a tile costing more than 1 or a fork
								forkTile = traversedTiles.get(0);
								//testedTile = traversedTiles.get(1);
								if(traversedTiles.size() <= 1) {
									System.err.println("Board.CalcMoves() - traversedTiles is too small to get a testedTile."
											+ "I think we're trying to trace back the start of the fork but its probably the characters start location. Breaking.");
									break;
								} else {
									testedTile = traversedTiles.get(1);
								}
								
								if(debugPathfinding)
									System.out.println("            -Iterated back to starting point, adding testedTile: " + testedTile.Location());
							}
						}
					} else { //Else if we didn't find a nextTile and we didn't hit any movement extents then we truely reached a dead end
						//Adapt this logic for deadends by iterating thru the previous traversedtiles in reverse and picking the first one that has
						//another possible direction to move
							
						if(debugPathfinding)
							System.out.println("        -Dead-ended at: " + lastTile.Location());
						
						Point[] directions = new Point[] { new Point(1, 0), new Point(-1, 0), new Point(0, 1), new Point(0, -1) };
						int possibleForkIndex = -1;
						int firstEncounteredForkIndex = -1;
						int moveRangeRemainingTemp = moveRangeRemaining;
						for(int i = traversedTiles.size() - 2; i > -1; i--) {
							//Ignore the start tile penalty. That tile's penalty is irrelevant because we're already there
							if(i == 0)
								break;
							
							Tile tile = traversedTiles.get(i);
							if(currentForkLayer != null && currentForkLayer.ForkedTile == tile) {
								firstEncounteredForkIndex = i;
								break;
							}
							
							boolean isFirstPossibleFork = false;
							for(int d = 0; d < 4; d++) {
								Tile adjacentTile = GetTileAt( new Point(tile.Location().x + directions[d].x, tile.Location().y + directions[d].y) );
								if(traversedTiles.contains(adjacentTile))
									continue;
								MoveResult moveResult = CanMoveToTile(adjacentTile, moveRangeRemainingTemp, charBase.getTilePenaltyMod(), null, traversedTiles);
								if(moveResult.canMoveThruTile && moveResult.movementsRemaining > 0) {
									isFirstPossibleFork = true;
									break;
								}
							}
							if(isFirstPossibleFork) {
								possibleForkIndex = i;
								break;
							}
							
							//moveRangeRemainingTemp += tile.GetTerrain().getMovementPenalty();
							moveRangeRemainingTemp += GetSceneTileData(tile.Location()).penalty;
						}
						
						if(possibleForkIndex > -1) {
							//then record the fork at that found location
							forkTile = traversedTiles.get(possibleForkIndex);
							//find the tested fork tile for the new forkLayer by getting the index of the forkTile in the WorkingPath and then adding 1
							testedTile = traversedTiles.get(possibleForkIndex + 1);
						} else if(firstEncounteredForkIndex > -1) {
							//We encountered a fork before hitting any tiles costing greater than 1
							forkTile = traversedTiles.get(firstEncounteredForkIndex);
							testedTile = traversedTiles.get(firstEncounteredForkIndex + 1);
						} else {
							//We iterated all the way back to our starting point before hitting a tile costing more than 1 or a fork
							if(traversedTiles.size() >= 2) {
								forkTile = traversedTiles.get(0);
								testedTile = traversedTiles.get(1);
							} else {
								//if(debugPathfinding)
								//	System.out.println("        *-Unit is trapped. Ending Pathfinding.");
								//areAllDirectionsBlocked = true;
								//break;
								if(paths.size() == 0) {
									if(debugPathfinding)
										System.out.println("        *-Unit is trapped. Ending Pathfinding.");
									areAllDirectionsBlocked = true;
									break;
								} else {
									if(debugPathfinding)
										System.out.println("        *-Target tile is unreachable. Continuing Pathfinding.");
									break;
								}
							}
						}
					}
					
					if(currentForkLayer == null) {
						currentForkLayer = new ForkLayer(forkTile);
						if(debugPathfinding)
							System.out.println("        -Creating new root ForkLayer for forkTile: " + forkTile.Location().toString() + ", and testedForkChoice: " + testedTile.Location());
					} else if(currentForkLayer.ForkedTile != forkTile) {
						ForkLayer newLayer = new ForkLayer(forkTile);
						currentForkLayer.ChildLayers.add(newLayer);
						newLayer.ParentLayer = currentForkLayer;
						currentForkLayer = newLayer;
						if(debugPathfinding)
							System.out.println("        -Creating new child ForkLayer for forkTile: " + forkTile.Location().toString() + ", and testedForkChoice: " + testedTile.Location());
					} else {
						if(debugPathfinding)
							System.out.println("        -Updating currentForkLayer with testedTile: " + testedTile.Location().toString());
					}
					currentForkLayer.TestedForkChoices.add(testedTile);
					
					//then rewind our lists to restart the pathfinding from the fork
					//Remove entries after fork
					for(int f = traversedTiles.size() - 1; f > -1; f--) {
						if(traversedTiles.get(f) != currentForkLayer.ForkedTile) {
							if(debugPathfinding)
								System.out.println("        -Rewind: Removing tile " + traversedTiles.get(f).Location());
							//moveRangeRemaining += traversedTiles.get(f).GetTerrain().getMovementPenalty();
							moveRangeRemaining += GetSceneTileData(traversedTiles.get(f).Location()).penalty;
							
							traversedTiles.remove(f);
						} else {
							break;
						}
					}
					
					//Reset variables in this while loop to prepare to revert
					workingPath = new ArrayList<Tile>( traversedTiles );
					//Remove starting point entry
					workingPath.remove(0);
						
					hasCompromisedIdealPath = false;
					hasCompromisedCriticalPath = false;
					
					if(debugPathfinding)
						System.out.println("        *-Rewinding traversedTiles and workingPath to location: " + traversedTiles.get(traversedTiles.size()-1).Location().toString() + ", moveRangeRemaining: " + moveRangeRemaining);
				
					//Remove the last index because it will be re-added at the start of the next iteration
					traversedTiles.remove(traversedTiles.size() - 1);
				}
				//continue pathfinding from nextTile or most recent, valid deadend or fork
			}
			potentialTiles.remove(destination);
			
			if(abortPathfinding || areAllDirectionsBlocked)
				break;
		}
		
		if(abortPathfinding) {
			System.err.println("Pathfinding aborted early!");
		} else if(areAllDirectionsBlocked) {
			if(debugPathfinding)
				System.out.println("Pathfinding stopped, areAllDirectionsBlocked: " + areAllDirectionsBlocked);
		} else {
			if(debugPathfinding)
				System.out.println("All paths resolved.");
		}
			
		charBase.SetPaths(paths);
	}
	
	public void ShowMoves(CharacterBase charBase) {
		if(charBase != Game.Instance().GetActiveBattleCharacter()) {
			for(Tile displayedTile : currentlyDisplayedMoveTiles)
				displayedTile.ToggleMoveTint(false);
		}
		
		System.out.println("Board.ShowMoves() - for: " + charBase.GetData().getName());
		for(Tile tile : charBase.GetPaths().keySet()) {
			//System.out.println(" -" + tile.Location());
			tile.ToggleMoveTint(true);
		}
	}
	
	//Prevent the active turn takers move tiles from being reset before they've moved
	//	-When highlighting another character their movements will be shown and
	//    if theres overlap with the active movers tiles then they'll get unintentional reset when defocusing that other player)
	List<Tile> currentlyDisplayedMoveTiles = new ArrayList<Tile>();
	public void SaveTurnTakersMoveTiles(CharacterBase charBase) {
		System.out.println("SaveMoveTilesForMovingChar() - paths.keys.size: " + charBase.GetPaths().keySet().size());
		currentlyDisplayedMoveTiles.addAll(charBase.GetPaths().keySet());
	}
	public void ClearTurnTakersMoveTiles() {
		currentlyDisplayedMoveTiles.clear();
	}
	
	private List<Tile> shownDirectionalTiles = new ArrayList<Tile>();
	public List<Tile> getShownDirectionalTiles() { return shownDirectionalTiles; }
	
	public void ShowDirections(Point location) {
		shownDirectionalTiles.clear();
		for(Tile tile : GetSurroundingTiles(location)) {
			tile.ToggleMoveTint(true);
			shownDirectionalTiles.add(tile);
		}
	}
	
	public List<Tile> GetSurroundingTiles(Point coord) {
		List<Tile> directionalTiles = new ArrayList<Tile>();
		Point[] adjacentTiles = new Point[] {
			new Point(coord.x, coord.y + 1), //Front
			new Point(coord.x, coord.y - 1), //Back
			new Point(coord.x + 1, coord.y), //Right
			new Point(coord.x - 1, coord.y), //Left
		};
		for(int i = 0; i < 4; i++) {
			if(adjacentTiles[i].x < 0 || adjacentTiles[i].x >= boardDimensions.width || adjacentTiles[i].y < 0 || adjacentTiles[i].y >= boardDimensions.height)
				continue;
			directionalTiles.add(GetTileAt(adjacentTiles[i]));
		}
		return directionalTiles;
	}
	
	private List<Tile> attackableTiles = new ArrayList<Tile>();
	
	public void ShowAttacks(CharacterBase characterBase) {
		ResetDirtyHighlights();
		attackableTiles.clear();
		
		//use characters location and attack range to get all attackable tiles
		int attackRange = characterBase.GetMaxRangeForAction(ObjectiveType.Attack);
		for(int y = characterBase.getLocation().y - attackRange; y <= characterBase.getLocation().y + attackRange; y++) {
			for(int x = characterBase.getLocation().x - attackRange; x <= characterBase.getLocation().x + attackRange; x++) {
				if(x < 0 || x >= boardDimensions.width || y < 0 || y >= boardDimensions.height)
					continue;
				
				Point targetLoc = new Point(x, y);
				int distance = Game.GetDistance(characterBase.getLocation(), targetLoc);
				if(distance <= attackRange) {
					Tile tile = GetTileAt(targetLoc);
					
					if(tile.Occupant() == characterBase)
						continue;
					
					//tile.ToggleMoveTint(true);
					tile.ToggleTargetTint(true);
					
					attackableTiles.add(tile);	
				}
			}
		}
		
		//This is being adapted from Abilities and Items so some things may not translate
		int weaponAOE = characterBase.GetMaxWeaponAOE();
		attackRadiiTiles.clear();
		attackableTilesWithRadii.clear();
		//Also visualize the aoe radius from each selectable tile
		if(weaponAOE > 0) {
			for(Tile targetTile : attackableTiles) {	
				List<Tile> tilesWithinRadius = new ArrayList<Tile>();
				tilesWithinRadius.add(targetTile);
				
				int remainingRadiusCycles = weaponAOE;
				while(remainingRadiusCycles > 0) {
					List<Tile> newTiles = new ArrayList<Tile>();
					for(Tile tileWithin : tilesWithinRadius) {
						for(Tile surroundingTile : this.GetSurroundingTiles(tileWithin.Location())) {
							if(!tilesWithinRadius.contains(surroundingTile)) {
								newTiles.add(surroundingTile);
								attackRadiiTiles.add(surroundingTile);
								surroundingTile.ToggleAttackRadiusHighlight(true);
							}
						}
					}
					tilesWithinRadius.addAll(newTiles);
					remainingRadiusCycles--;
				}
				
				attackableTilesWithRadii.put(targetTile, tilesWithinRadius);
			}
		}
	}
	
	public void ShowAbilityAttacks(CharacterBase characterBase, Ability ability) {
		ResetDirtyHighlights();
		boardGrid.repaint(500);
		attackableTiles.clear();
		
		//use characters location and attack range to get all attackable tiles
		for(int y = characterBase.getLocation().y - ability.range_max; y <= characterBase.getLocation().y + ability.range_max; y++) {
			for(int x = characterBase.getLocation().x - ability.range_max; x <= characterBase.getLocation().x + ability.range_max; x++) {
				if(x < 0 || x >= boardDimensions.width || y < 0 || y >= boardDimensions.height)
					continue;
				
				Point targetLoc = new Point(x, y);
				int distance = Game.GetDistance(characterBase.getLocation(), targetLoc);
				if(distance <= ability.range_max && distance >= ability.range_min) {
					Tile tile = GetTileAt(targetLoc);
					
					//if(tile.Occupant() == characterBase)
					//	continue;
					//We want to be able to target ourselves
					
					//tile.ToggleMoveTint(true);
					tile.ToggleTargetTint(true);
					
					attackableTiles.add(tile);	
				}
			}
		}
		
		attackRadiiTiles.clear();
		attackableTilesWithRadii.clear();
		//Also visualize the aoe radius from each selectable tile
		if(ability.hitRadius > 0) {
			for(Tile targetTile : attackableTiles) {	
				List<Tile> tilesWithinRadius = new ArrayList<Tile>();
				tilesWithinRadius.add(targetTile);
				
				int remainingRadiusCycles = ability.hitRadius;
				while(remainingRadiusCycles > 0) {
					List<Tile> newTiles = new ArrayList<Tile>();
					for(Tile tileWithin : tilesWithinRadius) {
						for(Tile surroundingTile : this.GetSurroundingTiles(tileWithin.Location())) {
							if(!tilesWithinRadius.contains(surroundingTile)) {
								newTiles.add(surroundingTile);
								attackRadiiTiles.add(surroundingTile);
								surroundingTile.ToggleAttackRadiusHighlight(true);
							}
						}
					}
					tilesWithinRadius.addAll(newTiles);
					remainingRadiusCycles--;
				}
				
				attackableTilesWithRadii.put(targetTile, tilesWithinRadius);
			}
		}
	}
	
	/**
	 * This is a convenience list containing all the value lists from attackableTilesWithRadii
	 */
	List<Tile> attackRadiiTiles = new ArrayList<Tile>();
	/**
	 * Use this to get all the tiles that'll be hit by an attack based on the chosen epicenter point.
	 */
	Map<Tile,List<Tile>> attackableTilesWithRadii = new HashMap<Tile,List<Tile>>();
	
	/**
	 * Use this method only for user-controlled turn tasks. This method won't work for AI because the AI logic flow never populates the attackableTiles list or the attackableTilesWithRadii map used here.
	 * @param tile
	 * @return
	 */
	public List<CharacterBase> GetTargetsFromTile(Tile tile) {
		List<CharacterBase> targetedCharacters = new ArrayList<CharacterBase>();
		if(attackableTilesWithRadii.containsKey(tile)) {
			for(Tile targetedTile : attackableTilesWithRadii.get(tile)) {
				if(targetedTile.Occupant() != null) {
					targetedCharacters.add(targetedTile.Occupant());
				}
			}
		} else if(this.attackableTiles.contains(tile) && tile.Occupant() != null) { //Select the currently hovered character cause there isn't an aoe but they're still a valid single target
			targetedCharacters.add(tile.Occupant());
		}
		return targetedCharacters;
	}
	
	public void ShowItemTiles(CharacterBase characterBase, ItemData itemData) {
		/*ResetDirtyHighlights();
		boardGrid.repaint(500);
		attackableTiles.clear();
		
		int range_max = itemData.getStats().GetBattleToolTraits().maxRange;
		int range_min = itemData.getStats().GetBattleToolTraits().minRange;
		
		//use characters location and attack range to get all attackable tiles
		for(int y = characterBase.getLocation().y - range_max; y <= characterBase.getLocation().y + range_max; y++) {
			for(int x = characterBase.getLocation().x - range_max; x <= characterBase.getLocation().x + range_max; x++) {
				if(x < 0 || x >= boardDimensions.width || y < 0 || y >= boardDimensions.height)
					continue;
				
				Point targetLoc = new Point(x, y);
				int distance = Game.GetDistance(characterBase.getLocation(), targetLoc);
				if(distance <= range_max && distance >= range_min) {
					Tile tile = GetTileAt(targetLoc);
					
					//if(tile.Occupant() == characterBase)
					//	continue;
					//We want to be able to target ourselves
					
					//tile.ToggleMoveTint(true);
					tile.ToggleTargetTint(true);
					
					attackableTiles.add(tile);	
				}
			}
		}*/
		
		ResetDirtyHighlights();
		boardGrid.repaint(500);
		attackableTiles.clear();
		
		int range_max = itemData.getStats().GetBattleToolTraits().maxRange;
		int range_min = itemData.getStats().GetBattleToolTraits().minRange;
		int radius = itemData.getStats().GetBattleToolTraits().aoeRange;
		
		//use characters location and attack range to get all attackable tiles
		for(int y = characterBase.getLocation().y - range_max; y <= characterBase.getLocation().y + range_max; y++) {
			for(int x = characterBase.getLocation().x - range_max; x <= characterBase.getLocation().x + range_max; x++) {
				if(x < 0 || x >= boardDimensions.width || y < 0 || y >= boardDimensions.height)
					continue;
				
				Point targetLoc = new Point(x, y);
				int distance = Game.GetDistance(characterBase.getLocation(), targetLoc);
				if(distance <= range_max && distance >= range_min) {
					Tile tile = GetTileAt(targetLoc);
					tile.ToggleTargetTint(true);
					attackableTiles.add(tile);	
				}
			}
		}
		
		attackRadiiTiles.clear();
		attackableTilesWithRadii.clear();
		//Also visualize the aoe radius from each selectable tile
		if(radius > 0) {
			for(Tile targetTile : attackableTiles) {	
				List<Tile> tilesWithinRadius = new ArrayList<Tile>();
				tilesWithinRadius.add(targetTile);
				
				int remainingRadiusCycles = radius;
				while(remainingRadiusCycles > 0) {
					List<Tile> newTiles = new ArrayList<Tile>();
					for(Tile tileWithin : tilesWithinRadius) {
						for(Tile surroundingTile : this.GetSurroundingTiles(tileWithin.Location())) {
							if(!tilesWithinRadius.contains(surroundingTile)) {
								newTiles.add(surroundingTile);
								attackRadiiTiles.add(surroundingTile);
								surroundingTile.ToggleAttackRadiusHighlight(true);
							}
						}
					}
					tilesWithinRadius.addAll(newTiles);
					remainingRadiusCycles--;
				}
				
				attackableTilesWithRadii.put(targetTile, tilesWithinRadius);
			}
		}
	}
}
