package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import data.EnvironmentPlotData;
import data.WorldTileData;
import data.WorldmapData;
import dataShared.MissionData;
import gameLogic.Game;
import gameLogic.MapLocation;
import gameLogic.MapUtil;
import gameLogic.Mission;
import gameLogic.Mission.MissionStatusType;
import gameLogic.Missions;
import gameLogic.Missions.ClusterLink;
import gameLogic.Missions.InstructionSet;
import gameLogic.Missions.MissionPathInstruction;
import enums.ColorBlend;
import enums.EnvironmentType;
import enums.MenuType;
import enums.SettlementDesignation;
import enums.SettlementType;
import enums.WorldTileType;
import enums.MissionIndicatorType;
import gui.SpriteSheetUtility.AnimType;
import gui.WorldmapPanel.PathOptionsAnalysis.Path;


@SuppressWarnings("serial")
public class WorldmapPanel extends JPanel {
	/**
	 * Upon the creation of a new world do we want to export an image of the worldmap to the save directory?
	 * This is helpful for studying the map's procedural generation.
	 */
	private boolean saveWorldmapImage = false;
	/**
	 * Do we want to log all the debug messages associated with procedural world creation and install several helper fields into the worldmap container UI?
	 */
	private boolean isDebuggingGeneration = false;
	
	//private GUIManager manager;
	
	private final int worldWidth = 64;
	
	//Worldmap GUI Components
	JPanel journeyPanel;
	JLayeredPane terrainLayers;
	JPanel mapContainer;
	
	MapLocationPanel mapLocationPanel;

	private Random r = new Random();
	
	//Debugging UI helpers
	JLabel tileSelectionLabel;
	
	//Audio
	private BGMusicThread bgMusicThread = new BGMusicThread();
	public BGMusicThread getBgMusicThread() { return bgMusicThread; }
	
	public class ScaleListener implements ComponentListener {
		public ScaleListener(JComponent source) {
			this.source = source;
			this.source.addComponentListener(this);
			
			sourceAspectRatio = (double)this.source.getSize().height / this.source.getSize().width;
		}
		//The parent that'll be getting scaled by some external mechanism
		private JComponent source;
		//Heirarchy Invalid components(that we're placing using .SetLocation() and/or .SetSize())
		private class ChildTransform {
			public Point2D relativePosition;
			public Point2D relativeScale;
		}
		private Map<JComponent, ChildTransform> childrenTransformMap = new HashMap<JComponent, ChildTransform>();
		public ChildTransform GetTransformForChild(JComponent comp) {
			return childrenTransformMap.get(comp);
		}
		public void AddChild(JComponent childComp) {
			childrenTransformMap.put(childComp, GetChildTransform(childComp));
		}
		
		private boolean isScalingEnabled = false;
		public void EnableScaling() {
			isScalingEnabled = true;
		}
		
		//Called immediately before the source is scaled
		public void CaptureChildTransforms() {
			Set<JComponent> keys = childrenTransformMap.keySet();
			for(JComponent child : keys) {
				childrenTransformMap.put(child, GetChildTransform(child));
			}
		}
		
		private ChildTransform GetChildTransform(JComponent child) {
			ChildTransform transform = new ChildTransform();
			transform.relativePosition = new Point2D.Double(child.getLocation().getX() / source.getPreferredSize().getWidth(), child.getLocation().getY() / source.getPreferredSize().getHeight());
			transform.relativeScale = new Point2D.Double(child.getSize().getWidth() / source.getPreferredSize().getWidth(), child.getSize().getHeight() / source.getPreferredSize().getHeight());
			//System.out.println("Adding ChildTransform - position: " + transform.relativePosition + ", size: " + transform.relativeScale);
			return transform;
		}
		
		List<BorderScaler> borderScalers = new ArrayList<BorderScaler>();
		public class BorderScaler {
			public BorderScaler(JPanel panelWithBorder, MathOperation op, float factor) {
				this.panelWithBorder = panelWithBorder;
				this.op = op;
				this.factor = factor;
			}
			JPanel panelWithBorder;
			MathOperation op;
			float factor;
		}
		public void AddBorderScaler(JPanel panelWithBorder, MathOperation op, float factor) {
			borderScalers.add(new BorderScaler(panelWithBorder, op, factor));
		}
		
		//The Row Model is the scale listeners "Role Model" ;) ;) ;) thats a play-on-words. Thank you and you're welcome
		JComponent rowModel;
		public void SetRowModel(JComponent rowModel) {
			this.rowModel = rowModel;
		}
		public JComponent GetRowModel() { return rowModel; }
		
		private double sourceAspectRatio;
		public double GetSourceAspectRatio() { return sourceAspectRatio; }
		
		@Override
		public void componentHidden(ComponentEvent arg0) {}
		@Override
		public void componentShown(ComponentEvent arg0) {}
		@Override
		public void componentMoved(ComponentEvent arg0) {}
		@Override
		public void componentResized(ComponentEvent event) {
			if(!isScalingEnabled)
				return;
			
			for(JComponent child : childrenTransformMap.keySet()) {
				ChildTransform trans = childrenTransformMap.get(child);
				child.setLocation((int)Math.round(source.getSize().width * trans.relativePosition.getX()), (int)Math.round(source.getSize().height * trans.relativePosition.getY()));
				child.setSize((int)Math.round(source.getSize().width * trans.relativeScale.getX()), (int)Math.round(source.getSize().height * trans.relativeScale.getY()));

				//System.out.println("componentResized() - location: " + child.getLocation() + ", size: " + child.getSize());
			}
			
			//resize borders based on the panels new scale
			for(BorderScaler scaler : borderScalers) {
				int width = scaler.panelWithBorder.getSize().width;
				int newGap = 1;
				switch(scaler.op) {
					case Divide:
						newGap = (int)Math.round(width / scaler.factor);
						break;
					case Multiply:
						newGap = (int)Math.round(width * scaler.factor);
						break;
					default:
						System.err.println("Add support for: " + scaler.op.toString());
						break;
				}
				//BorderLayout bLay = (BorderLayout)scaler.panelWithBorder.getLayout();
				//bLay.setHgap(newGap);
				//bLay.setVgap(newGap);
				scaler.panelWithBorder.setBorder(BorderFactory.createEmptyBorder(newGap, newGap, newGap, newGap));
			}
		}
	}
	enum MathOperation { Divide, Multiply };
	private ScaleListener terrainLayersScaleListener;
	
	
	public void Initialize(GUIManager manager, MapLocationPanel mapLocationPanel) {
		//this.manager = manager;
		this.mapLocationPanel = mapLocationPanel;
		
		//Try adjusting the UIManager developer defaults to make changes to tabbedPane's appearance
		/*UIManager.put("TabbedPane.background", Color.RED);
		UIManager.put("TabbedPane.highlight", Color.RED);
		UIManager.put("TabbedPane.light", Color.RED);
		UIManager.put("TabbedPane.shadow", Color.RED);
		UIManager.put("TabbedPane.darkShadow", Color.RED);*/
		
		Color transparent = new Color(0,0,0,0);
		UIManager.put("TabbedPane.highlight", transparent);
		UIManager.put("TabbedPane.light", transparent);
		UIManager.put("TabbedPane.shadow", transparent);
		UIManager.put("TabbedPane.darkShadow", transparent);
		//Creates a gross dotted boarder around the selected tab button
		UIManager.put("TabbedPane.focus", transparent);
		
		//This is the line separating the content pane from the tabs
		UIManager.put("TabbedPane.contentAreaColor", Color.BLACK);
		
		//Makes the selected tab wider, adds a fun quasi-anim feel to changing tabs
		UIManager.put("TabbedPane.selectedTabPadInsets", new Insets(10,10,10,10));
		//Insets unselected tabs
		UIManager.put("TabbedPane.tabAreaInsets", new Insets(4,4,4,4));
		UIManager.put("TabbedPane.contentBorderInsets", new Insets(1,0,0,0));
		
		
		//Make changes to all ScrollBars throughout the application
		UIManager.put("ScrollBarUI", AgentsScrollBarUI.class.getName());

		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setUI(new CustomTabbedPaneUI(SpriteSheetUtility.HighlightBGNinecon(), SpriteSheetUtility.ValueBGNinecon()));
		//Dimension tabDimensions = GUIUtil.GetRelativeSize(0.15f, 0.068f);
		Dimension tabDimensions = GUIUtil.GetRelativeSize(0.15f, 0.061f);
		Font tabFont = GUIUtil.SubTitle;
		
		//Journey Panel - Start
		
		journeyPanel = new JPanel(new BorderLayout());
		journeyPanel.setBackground(Color.BLACK);
		//journeyPanel.setBorder(BorderFactory.createLineBorder(Color.RED,  1));
		tabbedPane.addTab("Journey", null, journeyPanel, "Travel across this intriguing land.");
		JFxLabel customLabel_journey = new JFxLabel("Journey", SwingConstants.CENTER, tabFont, Color.WHITE)
				.withStroke(Color.BLACK, 2, false);
		customLabel_journey.setPreferredSize(tabDimensions);
		tabbedPane.setTabComponentAt(0, customLabel_journey);
		
		//Debugging
		if(isDebuggingGeneration) {
			tileSelectionLabel = new JLabel("DEBUG Tile Event - ");
			tileSelectionLabel.setForeground(Color.WHITE);
			journeyPanel.add(tileSelectionLabel, BorderLayout.NORTH);
		}
		
		//Journey Panel - End
		
		//Party Panel - Start
		
		PartyPanel partyPanel = new PartyPanel();
		tabbedPane.addTab("Agents", null, partyPanel, "Manage your team.");
		JFxLabel customLabel_party = new JFxLabel("Agents", SwingConstants.CENTER, tabFont, Color.WHITE)
				.withStroke(Color.BLACK, 2, false);
		customLabel_party.setPreferredSize(tabDimensions);
		tabbedPane.setTabComponentAt(1, customLabel_party);
		
		//Party Panel - End
		
		//Inventory Panel - Start
		
		InventoryPanel inventoryPanel = new InventoryPanel();
		tabbedPane.addTab("Inventory", null, inventoryPanel, "Inspect your items.");
		JFxLabel customLabel_inventory = new JFxLabel("Inventory", SwingConstants.CENTER, tabFont, Color.WHITE)
				.withStroke(Color.BLACK, 2, false);
		customLabel_inventory.setPreferredSize(tabDimensions);
		tabbedPane.setTabComponentAt(2, customLabel_inventory);
		
		//Inventory Panel - End
		
		//Mission Panel - Start
		
		MissionPanel missionPanel = new MissionPanel();
		tabbedPane.addTab("Missions", null, missionPanel, "Keep track of your missions.");
		JFxLabel customLabel_mission = new JFxLabel("Missions", SwingConstants.CENTER, tabFont, Color.WHITE)
				.withStroke(Color.BLACK, 2, false);
		customLabel_mission.setPreferredSize(tabDimensions);
		tabbedPane.setTabComponentAt(3, customLabel_mission);
		
		
		
		//Mission Panel - End
		
		tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				Object comp = tabbedPane.getSelectedComponent();
				
				System.out.println("comp: " + comp.toString());
				
				//Refresh the upcoming panel
				IRefreshable refreshable = null;
				try {
					refreshable = (IRefreshable)comp;
				} catch(Exception e) {
					System.err.println("tabs - stateChanged, error: " + e.getMessage());
				}
				if(refreshable != null)
					refreshable.Refresh();
			}
		});
		
		this.add(tabbedPane, BorderLayout.CENTER);
		
		//disabling this during testing
		bgMusicThread.start();
		
		//Populates the TravelInfo maps
		DefineTravelMaps();
	}
	
	//Setup Worldmap - Start
	
	private final String worldMapImageFilePath = Game.SetupOrGetDataPathDirectory() + "WorldmapRender.png";
	
	public void LoadWorld() {
		//TODO We need to edit the loading scheme to support the rebuilding of world tiles from worldmap data
		//if(isDebuggingGeneration) {
		//	System.err.println("LoadWorld() - isDebuggingGeneration = true but is not available when loading a game. Disabling debugging. Select New Game to run debug protocol.");
		//	isDebuggingGeneration = false;
		//}
		
		//load the worldmap data from save data
		WorldmapData worldmapData = Game.Instance().GetWorldmapData();
		
		//Setup terrainLayers
		terrainLayers = new JLayeredPane();
		Dimension terrainLayersSize = GUIUtil.GetRelativeSize(worldmapData.worldMapWidth, worldmapData.worldMapHeight);
		terrainLayers.setSize(terrainLayersSize);
		terrainLayers.setPreferredSize(terrainLayersSize);
		terrainLayersScaleListener = new ScaleListener(terrainLayers);
		//terrainLayers.setBorder(BorderFactory.createLineBorder(Color.BLUE,  2));
		
		worldMap = new HashMap<Point2D, WorldTile>();
		for(Point2D point : worldmapData.GetWorldMapDatas().keySet()) {
			//System.out.println("worldMap tile mapLocation: " + (worldmapData.GetWorldMapDatas().get(point).mapLocation != null ? worldmapData.GetWorldMapDatas().get(point).mapLocation.getName() : "null"));
			worldMap.put(point, new WorldTile(worldmapData.GetWorldMapDatas().get(point)));
		}
		
		
		//Recreate the terrain tiles
		
		//System.out.println("terrainLayersSize: " + terrainLayersSize + ", startPoint: " + worldmapData.startPoint);
		//System.out.println("columnCount: " + worldmapData.columnCount + ", rowCount: " + worldmapData.rowCount);
		//System.out.println("tileWidth: " + worldmapData.tileWidth + ", tileHeight: " + worldmapData.tileHeight);
		//System.out.println("row Width: " + (worldmapData.tileWidth * worldmapData.columnCount) + ", row heightInterval: " + worldmapData.heightInterval);
		
		BufferedImage[] blankImages = SpriteSheetUtility.getTerraingroupBlank();
		for(int rows = 0; rows < worldmapData.rowCount; rows++) {
			JPanel columnGridPanel = new JPanel(new GridLayout(1, worldmapData.columnCount));
			columnGridPanel.setOpaque(false);
			columnGridPanel.setBackground(new Color(0,0,0,0));
			columnGridPanel.setSize(worldmapData.tileWidth * worldmapData.columnCount, worldmapData.tileHeight);
			int xOffset = rows % 2 == 0 ? 0 : (worldmapData.tileWidth / 2);
			int yOffset = rows * worldmapData.heightInterval;
			columnGridPanel.setLocation(worldmapData.startPoint.x + xOffset, worldmapData.startPoint.y - worldmapData.tileHeight - yOffset);
			terrainLayersScaleListener.AddChild(columnGridPanel);
			
			for(int columns = 0; columns < worldmapData.columnCount; columns++) {
				final int xCoord = columns;
				final int yCoord = worldmapData.rowCount - 1 - rows;
				
				WorldTile governingWorldTile = worldMap.get(this.convertToHexPosition(xCoord, yCoord, worldmapData.rowCount));
				
				//BufferedImage[] images = null;
				
				//We actually wanna let the tile be shown in case the saveWorldmapImage boolean is true and then after the rendering of the image we can set all tiles to be blank
				//if(governingWorldTile.IsBlank())
				//	images = SpriteSheetUtility.getTerraingroupBlank();
				//else
				//	images = SpriteSheetUtility.GetTerrainFromWorldTile(governingWorldTile.getTileType());
				
				//int index = r.nextInt(images.length);
				
				//governingWorldTile.SetTerrainImageIndex(index);
				ImagePanel tileImage = new ImagePanel(blankImages[governingWorldTile.getBlankTerrainImageIndex()]);
				
				//Color tintColor = GetTileTint(governingWorldTile);
				//if(tintColor != null)
				//	tileImage.SetTint(tintColor);
				
				tileImage.setOpaque(false);
				tileImage.setBackground(new Color(0,0,0,0));
				Dimension imageDimension = new Dimension(worldmapData.tileWidth, worldmapData.tileHeight);
				tileImage.setSize(imageDimension);
				columnGridPanel.add(tileImage);

				governingWorldTile.SetTerrainComponent(tileImage);
			}
			terrainLayers.add(columnGridPanel, -1, rows);
		}
		
		
		CreateWorldmapUI(worldmapData.startPoint, worldmapData.tileWidth, worldmapData.tileHeight, worldmapData.rowCount, worldmapData.columnCount, worldmapData.heightInterval);
		
		
		//retrieve worldmap image from data path and apply it to UI
		/*Runnable loadImageAndApply = new Runnable() {
			public void run() {
				System.out.println("WorldmapPanel.LoadWorld() - loadImageAndApply Runnable executed.");
				ImagePanel worldmapImagePanel = new ImagePanel( GUIUtil.GetBuffedImageFromAbsolutePath(worldMapImageFilePath) );	
				worldmapImagePanel.setBackground(worldmapBGColor);
				worldmapImagePanel.setSize(terrainLayers.getSize());
				worldmapImagePanel.setLocation(0, 0);
				terrainLayersScaleListener.AddChild(worldmapImagePanel);
				terrainLayers.add(worldmapImagePanel, -1, 0);
				
				//Get the size from the mapContainer while its a valid component taking up all available space in journey CENTER region and then invalidate it
				minWorldmapSize = mapContainer.getSize();
				maxWorldmapSize = new Dimension((int)Math.round(terrainLayers.getSize().width * zoomInLimitMax), (int)Math.round(terrainLayers.getSize().height * zoomInLimitMax));
				//Now that we've got our layout data, invalidate the mapContainer
				mapContainer.setLayout(null);
				
				//Show all the map location icons
				SetMapLocationIconsVisible(true);
				
				ignoreEventsDuringSetup = false;
				
				//Try setting up the Surroundings panel last
				//SetupSurroundingsPanel();
				mapLocationPanel.Initialize();
				
				WorldTile currentWorldTile = worldMap.get(playerSprite.GetWorldLocation());
				mapLocationPanel.OnEnterLocation(currentWorldTile.GetMapLocation());
				
				//Set to players location on the map so that the player sprite is center screen or as close as possible
				FocusViewOnTile(playerSprite.GetWorldLocation());
			}
		};
		SwingUtilities.invokeLater(loadImageAndApply);*/
		Runnable delayedSetup = new Runnable() {
			public void run() {
				System.out.println("WorldmapPanel.LoadWorld() - delayedSetup Runnable executed.");
				
				//Get the size from the mapContainer while its a valid component taking up all available space in journey CENTER region and then invalidate it
				minWorldmapSize = mapContainer.getSize();
				maxWorldmapSize = new Dimension((int)Math.round(terrainLayers.getSize().width * zoomInLimitMax), (int)Math.round(terrainLayers.getSize().height * zoomInLimitMax));
				//Now that we've got our layout data, invalidate the mapContainer
				mapContainer.setLayout(null);
				
				//Reveal all discovered tiles
				for(WorldTile worldTile : worldMap.values()) {
					if(worldTile.isDiscovered())
						worldTile.ShowTile_Full();
				}
				
				//Show all the map location icons
				SetMapLocationIconsVisible(true);
				
				ignoreEventsDuringSetup = false;
				
				mapLocationPanel.Initialize();
				
				WorldTile currentWorldTile = worldMap.get(playerSprite.GetWorldLocation());
				mapLocationPanel.OnEnterLocation(currentWorldTile.GetMapLocation());
				
				//Set to players location on the map so that the player sprite is center screen or as close as possible
				FocusViewOnTile(playerSprite.GetWorldLocation());
			}
		};
		SwingUtilities.invokeLater(delayedSetup);
		
		
		SwingUtilities.invokeLater(delayedRestructure);
	}
	
	Runnable delayedRestructure = new Runnable() {
		public void run() {
			RestructureWorldmapUI();
		}
	};
	
	private void FocusViewOnTile(Point2D worldLocation) {
		//Get the tiles location relative to the mapContainer using a while loop
		Point locationOnTerrainLayers = GetLocationOnTerrainLayers(worldLocation);
		
		//from that point subtract half the width and height of the mapContainer
		Dimension tileSize = GetCurrentTileSize();
		Point rawCenter = new Point(
				-locationOnTerrainLayers.x + (mapContainer.getSize().width / 2)  - (tileSize.width / 2),
				-locationOnTerrainLayers.y + (mapContainer.getSize().height / 2) - (tileSize.height / 2)
		);
		Point newTerrainLocation = GetClampedPosition(rawCenter);
		
		terrainLayers.setLocation(newTerrainLocation);
	}
	
	//Use these to hide and update the location label(BG, text and Hex tile icon)
	ImagePanel locationTileImage;
	ImagePanel settlementLocImage;
	JFxLabel locationText;
	JFxLabel locationSubtext;
	JLabel locationLabelBG;
	
	private void RestructureWorldmapUI() {
		System.out.println("WorldmapPanel.RestructureWorldmapUI()");
		
		//Remove terrainLayers from the hierarchy cause we're going to palce it into the LayeredPane
		mapContainer.remove(terrainLayers);
		
		JLayeredPane UIAndMapPane = new JLayeredPane();
		UIAndMapPane.setSize(mapContainer.getSize());
		int layerDepth = 0;
		
		//Setup general variables
		float normHeight = 0.12f;
		Point locLabBGlocation = GUIUtil.GetRelativePoint(0.006f, normHeight + 0.008f);
		locLabBGlocation = new Point(locLabBGlocation.x, mapContainer.getSize().height - locLabBGlocation.y);
		Color bgColor = new Color(0.83f, 0.72f, 0.61f, 1f);
		int cloudCount = 5;
		int cloudOffset = 8; //Represents the number of components that'll be layered in front of the clouds, adjust as necessary
		
		//Highlighted Location Label, comprised of three parts
		
		//Make Settlement icon
		settlementLocImage = new ImagePanel(GUIUtil.GetSettlementImage(SettlementType.AssassinationTarget, null));
		settlementLocImage.setOpaque(false);
		settlementLocImage.setBackground(new Color(0,0,0,0));
		settlementLocImage.setSize(GUIUtil.GetRelativeSize(0.13f, false));
		settlementLocImage.ConformSizeToAspectRatio(false);
		Point settleOffset = GUIUtil.GetRelativePoint(0.004f, 0.067f);
		Point labSettleLocation = new Point(locLabBGlocation.x + settleOffset.x, locLabBGlocation.y - settleOffset.y);
		settlementLocImage.setLocation(labSettleLocation);
		UIAndMapPane.add(settlementLocImage, cloudCount + cloudOffset, layerDepth);
		cloudOffset--;
		
		//Make hex tile icon
		locationTileImage = new ImagePanel(SpriteSheetUtility.GetRandomTerrainFromWorldTile(WorldTileType.peak));
		locationTileImage.setOpaque(false);
		locationTileImage.setBackground(new Color(0,0,0,0));
		locationTileImage.setSize(GUIUtil.GetRelativeSize(0.2f, false));
		locationTileImage.ConformSizeToAspectRatio(false);
		//Point tileOffset = GUIUtil.GetRelativePoint(0.02f, 0.085f);
		Point tileOffset = GUIUtil.GetRelativePoint(0.025f, 0.085f);
		Point labTileLocation = new Point(locLabBGlocation.x + tileOffset.x, locLabBGlocation.y - tileOffset.y);
		locationTileImage.setLocation(labTileLocation);
		UIAndMapPane.add(locationTileImage, cloudCount + cloudOffset, layerDepth);
		cloudOffset--;
		
		//make text label
		locationText = new JFxLabel("Location", SwingConstants.LEFT, GUIUtil.LocationLabel, Color.WHITE).withShadow(Color.DARK_GRAY, new Point(4,3));
		locationText.setSize(GUIUtil.GetRelativeSize(0.36f, normHeight - 0.025f));
		Point textOffset = GUIUtil.GetRelativePoint(0.115f, 0f);
		Point labTextLocation = new Point(locLabBGlocation.x + textOffset.x, locLabBGlocation.y + textOffset.y);
		locationText.setLocation(labTextLocation);
		//locationText.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		UIAndMapPane.add(locationText, cloudCount + cloudOffset, layerDepth);
		cloudOffset--;
		
		locationSubtext = new JFxLabel("Sublocation", SwingConstants.LEFT, GUIUtil.SublocationHeader, new Color(0.93f, 0.93f, 0.93f, 1f)).withShadow(Color.DARK_GRAY, new Point(3,3));
		locationSubtext.setSize(GUIUtil.GetRelativeSize(0.34f, normHeight / 2));
		Point subtextOffset = GUIUtil.GetRelativePoint(0.13f, 0.058f);
		Point labSubtextLocation = new Point(locLabBGlocation.x + subtextOffset.x, locLabBGlocation.y + subtextOffset.y);
		locationSubtext.setLocation(labSubtextLocation);
		//locationSubtext.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		UIAndMapPane.add(locationSubtext, cloudCount + cloudOffset, layerDepth);
		cloudOffset--;
		
		locationLabelBG = new JLabel(SpriteSheetUtility.CircularNinecon(bgColor, ColorBlend.Multiply));
		locationLabelBG.setLocation(locLabBGlocation);
		Dimension locationLabelSize = GUIUtil.GetRelativeSize(0.5f, normHeight);
		locationLabelBG.setSize(locationLabelSize);
		UIAndMapPane.add(locationLabelBG, cloudCount + cloudOffset, layerDepth);
		cloudOffset--;
		
		//Create enter button ui expansion from the edge of the location label panel
		List<CustomButtonUltra> customUltras = new ArrayList<CustomButtonUltra>();
		enterButton = new CustomButtonUltra(new JFxLabel("Enter", SwingConstants.CENTER, GUIUtil.Header, Color.BLACK),
				  null, Color.GREEN, SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(), new Color(240, 240, 240));
		enterButton.setLocation(Add(Add(locLabBGlocation, new Dimension(locationLabelSize.width, 0)), GUIUtil.GetRelativePoint(0.01f, 0.017f)));
		enterButton.setSize(GUIUtil.GetRelativeSize(0.1f, 0.09f));
		enterButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//stop timer
				timeTracker.stop();
				
				//GUIManager.ShowScreen(MenuType.LOCATION);
				//Fade into the scene
				GUIManager.GetFadeTransitionPanel().Fade(true, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						GUIManager.ShowScreen(MenuType.LOCATION);
						GUIManager.GetFadeTransitionPanel().Fade(false, 120);
					}
				});
			}
		});
		customUltras.add(enterButton);
		UIAndMapPane.add(enterButton, cloudCount + cloudOffset, layerDepth);
		cloudOffset--;
		enterButtonBg = new JLabel(SpriteSheetUtility.ValueBGNinecon());
		enterButtonBg.setLocation(Add(Add(locLabBGlocation, new Dimension(locationLabelSize.width, 0)), GUIUtil.GetRelativePoint(-0.02f, 0.01f)));
		enterButtonBg.setSize(GUIUtil.GetRelativeSize(0.14f, normHeight - 0.02f));
		UIAndMapPane.add(enterButtonBg, cloudCount + cloudOffset, layerDepth);
		cloudOffset--;
		
		HideLocationLabelPanel();
		//Add all buttons to a group so that they can elimate eachothers artifacting
		for(CustomButtonUltra ultra : customUltras)
			ultra.AddGroupList(customUltras);
		//Place next elements deeper than location label panel 
		layerDepth++;
		
		//Travel dialog
		travelPane = new TravelPane(mapContainer.getSize(), 0.35f, normHeight + 0.03f); 
		UIAndMapPane.add(travelPane, cloudCount + cloudOffset, layerDepth);
		layerDepth++;
		cloudOffset--;
		
		//Create travel anim panel
		travelAnimPane = new TravelAnimPane(mapContainer.getSize()); 
		UIAndMapPane.add(travelAnimPane, cloudCount + cloudOffset, layerDepth);
		layerDepth++;
		cloudOffset--;
		
		
		//Make cloud layer
		float avX = 0.1f;
		float avY = 0.15f;
		//BufferedImage transparentCloud = GUIUtil.GetTintedImage(GUIUtil.WorldmapCloudIcon, new Color(1f, 1f, 1f, 0.8f), ColorBlend.Multiply);
		for(int i = 0; i < cloudCount; i++) {
			float sizeScale = (100 + (float)r.nextInt(261)) / 300;
			Dimension cloudSize = GUIUtil.GetRelativeSize(avX * sizeScale, avY * sizeScale);
			Point randomPoint = new Point(r.nextInt(mapContainer.getSize().width), r.nextInt(mapContainer.getSize().height - cloudSize.height));
			
			VectorSprite newCloud = new VectorSprite(
					//transparentCloud,
					GUIUtil.WorldmapCloudIcon,
					randomPoint,
					cloudSize,
					//r.nextBoolean() ? new Point(1 + r.nextInt(2), 0) : new Point(-1 - r.nextInt(2), 0),
					r.nextBoolean() ? new Point(1, 0) : new Point(-1, 0),
					50,
					mapContainer.getSize()
			);
			cloudSprites.add(newCloud);
			UIAndMapPane.add(newCloud.GetImagePanel(), cloudCount-1-i, layerDepth);
		}
		layerDepth++;
		
		//Add terrainLayers back into the deepest layer
		UIAndMapPane.add(terrainLayers, layerDepth);
		layerDepth++;
		
		mapContainer.add(UIAndMapPane);
		
		//Start stamina regen timer
		timeTracker = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//It doesn't make sense to regen stamina while we're expending it during our travels
				if(travelAnimPane.IsAnimating())
					return;
				
				seconds++;
				if(seconds >= secondsPerRegenTick) {
					seconds = 0;
					int staminaInterval = 1;
					partyStamina = Math.min(partyStamina + staminaInterval, partyStaminaMax);
					//If we've got a simulated stamina bar for selected travel tiles then handle the stamina bar update differently
					if(travelTiles.size() == 0)
						travelPane.SetBar((float)partyStamina / partyStaminaMax);
					else {
						projectedStamina = Math.min(projectedStamina + staminaInterval, partyStaminaMax);
						travelPane.ShowTravelCost((projectedStamina - travelPane.pendingTravelInfo.staminaCost) / (float)partyStaminaMax);
					}
				}
			}
		});
		timeTracker.start();
		
		//I believe this to be the final point in World setup when either starting a new game or loading an old one so hook this event intended to demarcate the end of the setup process and allow for
		//the setup of subsystems and game flow actions
		OnWorldSetupComplete();
	}
	
	/**
	 * Called by GUIManager keyListener delegate for "Regen Stamina" developer command
	 */
	public void RefreshStaminaBar() {
		if(travelPane == null)
			return;
		
		partyStamina = Game.Instance().GetPartyStamina();
		if(travelTiles.size() == 0)
			travelPane.SetBar((float)partyStamina / partyStaminaMax);
		else {
			projectedStamina = Math.min(partyStamina, partyStaminaMax);
			travelPane.ShowTravelCost((projectedStamina - travelPane.pendingTravelInfo.staminaCost) / (float)partyStaminaMax);
		}
	}
	
	private void OnWorldSetupComplete() {
		//Show the map location screen upon completion of worldmap setup if this is the start of a new game
		if(GUIManager.getCurrentMenuType() != MenuType.LOCATION && Game.Instance().IsInMapLocation())
			GUIManager.ShowScreen(MenuType.LOCATION);
		
		//Fade in now, to either the Worldmap or MapLocation
		GUIManager.GetFadeTransitionPanel().Fade(false, 120);
	}
	
	public void OnPanelShown() {
		if(timeTracker != null)
			timeTracker.start();
	}
	
	//Track time to regerenate party stamina slowly
	private Timer timeTracker;
	private int secondsPerRegenTick = 4;
	private int seconds;
	
	private void HideLocationLabelPanel() {
		settlementLocImage.setVisible(false);
		locationTileImage.setVisible(false);
		locationText.setVisible(false);
		locationSubtext.setVisible(false);
		locationLabelBG.setVisible(false);
		enterButtonBg.setVisible(false);
		enterButton.setVisible(false);
	}
	
	JLabel enterButtonBg;
	
	Color worldmapBGColor = new Color(12, 45, 91, 255);
	
	private final int worldMapGrid_columnCount = 64;
	
	public void GenerateWorld() {
		//Create new system with location tile grid
		//create a layered pane to hold each row, offsetting every other row, with lower rows z ordered on top of higher rows
		terrainLayers = new JLayeredPane();
		float worldMapWidth = 4f;
		float worldMapHeight = 4f;
		Dimension terrainLayersSize = GUIUtil.GetRelativeSize(worldMapWidth, worldMapHeight);
		
		terrainLayers.setSize(terrainLayersSize);
		terrainLayers.setPreferredSize(terrainLayersSize);
		terrainLayersScaleListener = new ScaleListener(terrainLayers);
		//terrainLayers.setBorder(BorderFactory.createLineBorder(Color.BLUE,  2));
		
		float tileStartXPortion = 0.0175f;
		float tileEndXPortion = worldMapWidth - tileStartXPortion;
		float tileStartYPortion = 0.0175f;
		float tileEndYPortion = worldMapHeight - tileStartYPortion;
		
		Point startPoint = GUIUtil.GetRelativePoint(tileStartXPortion, tileEndYPortion);
		
		int availableWidth = GUIUtil.GetRelativeSize(tileEndXPortion - tileStartXPortion, true).width;
		int availableHeight = GUIUtil.GetRelativeSize(tileEndYPortion - tileStartYPortion, false).height;
		
		int columnCount = worldMapGrid_columnCount;
		int rowCount = Math.round( columnCount * ((float)availableHeight / availableWidth) * 4f/3f );
		Dimension worldGridDimension = new Dimension(columnCount, rowCount);
		
		System.out.println("Grid Dimensions: " + worldGridDimension.toString());
		
		//instantiate data structure for the worldTile map
		CreateMap(columnCount, rowCount);
		
		int tileWidth = Math.round( availableWidth / ((float)columnCount + 0.5f) );
		int tileHeight = Math.round(tileWidth * 1.5f);
		
		int heightInterval = tileWidth * 3 / 4;
		
		if(isDebuggingGeneration) {
			System.out.println("terrainLayersSize: " + terrainLayersSize + ", startPoint: " + startPoint);
			System.out.println("columnCount: " + columnCount + ", rowCount: " + rowCount);
			System.out.println("tileWidth: " + tileWidth + ", tileHeight: " + tileHeight);
			System.out.println("row Width: " + (tileWidth * columnCount) + ", row heightInterval: " + heightInterval);
		}
		
		//Assemble Map and save it as an image - start
		
		//List<JPanel> terrainGridPanels = new ArrayList<JPanel>();
		
		for(int rows = 0; rows < rowCount; rows++) {
			JPanel columnGridPanel = new JPanel(new GridLayout(1, columnCount));
			columnGridPanel.setOpaque(false);
			columnGridPanel.setBackground(new Color(0,0,0,0));
			columnGridPanel.setSize(tileWidth * columnCount, tileHeight);
			int xOffset = rows % 2 == 0 ? 0 : (tileWidth / 2);
			int yOffset = rows * heightInterval;
			columnGridPanel.setLocation(startPoint.x + xOffset, startPoint.y - tileHeight - yOffset);
			
			//if(isDebuggingGeneration)
			//we need this to support the terrain tiles
			terrainLayersScaleListener.AddChild(columnGridPanel);
			
			//columnGridPanel.setBorder(BorderFactory.createLineBorder(Color.MAGENTA, 3));
			
			for(int columns = 0; columns < columnCount; columns++) {
				final int xCoord = columns;
				final int yCoord = rowCount - 1 - rows;
				
				WorldTile governingWorldTile = worldMap.get(this.convertToHexPosition(xCoord, yCoord, rowCount));
				
				BufferedImage[] images = null;
				
				//We actually wanna let the tile be shown in case the saveWorldmapImage boolean is true and then after the rendering of the image we can set all tiles to be blank
				if(governingWorldTile.IsBlank())
					images = SpriteSheetUtility.getTerraingroupBlank();
				else
					images = SpriteSheetUtility.GetTerrainFromWorldTile(governingWorldTile.getTileType());
				
				int index = r.nextInt(images.length);
				
				governingWorldTile.SetTerrainImageIndex(index);
				ImagePanel tileImage = new ImagePanel(images[index]);
				
				Color tintColor = GetTileTint(governingWorldTile);
				if(tintColor != null)
					tileImage.SetTint(tintColor);
				
				tileImage.setOpaque(false);
				tileImage.setBackground(new Color(0,0,0,0));
				Dimension imageDimension = new Dimension(tileWidth, tileHeight);
				tileImage.setSize(imageDimension);
				columnGridPanel.add(tileImage);
				//tileImage.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
				
				//if(isDebuggingGeneration)
				//This is now part of the core map tile logic
					governingWorldTile.SetTerrainComponent(tileImage);
			}
			
			//terrainGridPanels.add(columnGridPanel);
			
			terrainLayers.add(columnGridPanel, -1, rows);
		}
		
		System.out.println("WorldmapPanel.GenerateWorld() - Calling Game.SetWorldmapData()");
		Map<Point2D, WorldTileData> worldTileDataMap = GetWorldTileDataMap();
		Game.Instance().SetWorldmapData(new WorldmapData(worldGridDimension, startPoint, worldMapWidth, worldMapHeight, tileWidth, tileHeight, rowCount, columnCount, heightInterval, worldTileDataMap));
		
		//CreateUI for new world
		CreateWorldmapUI(startPoint, tileWidth, tileHeight, rowCount, columnCount, heightInterval);
		
		//save as image and apply as the last layer of the terrainLayers pane
		Runnable saveImageAndApply = new Runnable() {
			public void run() {
				System.out.println("saveImageAndApply Runnable executed.");
				
				/*if(!isDebuggingGeneration) {
					ImagePanel worldmapImagePanel = new ImagePanel( GUIUtil.RenderPanelToPath(terrainLayers, worldMapImageFilePath) );
					worldmapImagePanel.setBackground(worldmapBGColor);
					worldmapImagePanel.setSize(terrainLayers.getSize());
					worldmapImagePanel.setLocation(0, 0);
					terrainLayersScaleListener.AddChild(worldmapImagePanel);
					
					//now remove the rows that were added to "Layer -1" for the map assembly
					for(JPanel panel : terrainGridPanels)
						terrainLayers.remove(panel);
					
					//add our image as the efficient stand-in
					terrainLayers.add(worldmapImagePanel, -1, 0);
				}*/
				//We no longer want to "apply" the image the map, we only want to save it or not and then carry on with the final setup of the worldmap
				if(saveWorldmapImage)
					GUIUtil.RenderPanelToPath(terrainLayers, worldMapImageFilePath);
				
				//Blank everything out now that we've, potentially, rendered the map
				BufferedImage[] blankImages = SpriteSheetUtility.getTerraingroupBlank();
				for(WorldTile worldTile : worldMap.values()) {
					//this is necessary because this runnable occurs after the player is placed at their starting location at which time the area is Discovered
					if(worldTile.isDiscovered())
						continue;
					
					int index = r.nextInt(blankImages.length);
					
					worldTile.SetBlankTerrainImageIndex(index);
					worldTile.getTerrainComponent().SetNewImage(blankImages[index]);
					
					worldTile.getTerrainComponent().ClearTint();
				}
				
				//Get the size from the mapContainer while its a valid component taking up all available space in journey CENTER region and then invalidate it
				//System.out.println("LATER mapContainer size: " + mapContainer.getSize());
				minWorldmapSize = mapContainer.getSize();
				maxWorldmapSize = new Dimension((int)Math.round(terrainLayers.getSize().width * zoomInLimitMax), (int)Math.round(terrainLayers.getSize().height * zoomInLimitMax));
				//Now that we've got our layout data, invalidate the mapContainer
				mapContainer.setLayout(null);
				
				//Show all the map location icons
				SetMapLocationIconsVisible(true);
				
				ignoreEventsDuringSetup = false;
				
				
				//SetupSurroundingsPanel();
				mapLocationPanel.Initialize();
				
				WorldTile currentWorldTile = worldMap.get(playerSprite.GetWorldLocation());
				MapLocation mapLocation = currentWorldTile.GetMapLocation();
				mapLocationPanel.OnEnterLocation(mapLocation);
				
				//Set to players location on the map so that the player sprite is center screen or as close as possible
				FocusViewOnTile(playerSprite.GetWorldLocation());
			}
		};
		SwingUtilities.invokeLater(saveImageAndApply);
		
		SwingUtilities.invokeLater(delayedRestructure);
	}
	
	public Map<Point2D, WorldTileData> GetWorldTileDataMap() {
		if(worldMap == null || worldMap.size() == 0)
			return null;
		Map<Point2D, WorldTileData> worldTileDataMap = new HashMap<Point2D, WorldTileData>();
		for(Point2D point : worldMap.keySet())
			worldTileDataMap.put(point, worldMap.get(point).getData());
		return worldTileDataMap;
	}
	
	/**
	 * Describes the target size for Mission Indicator Icons for WorldTiles
	 */
	private Dimension overlayDimension;
	
	/**
	 * Store the dimensions for filler boxes used throughout the worldmap GUI
	 */
	private Dimension fillerPref;
	
	private void CreateWorldmapUI(Point startPoint, int tileWidth, int tileHeight, int rowCount, int columnCount, int heightInterval) {
		System.out.println("WorldmapPanel.CreateWorldmapUI()");
		
		fillerPref = new Dimension(tileWidth, tileHeight);
		
		for(int rows = 0; rows < rowCount; rows++) {
			int xOffset = rows % 2 == 0 ? 0 : (tileWidth / 2);
			int yOffset = rows * heightInterval;
			
			//the second layer that holds the map location icons
			JPanel locationIconColumnGridPanel = new JPanel(new GridLayout(1, columnCount));
			locationIconColumnGridPanel.setOpaque(false);
			locationIconColumnGridPanel.setBackground(new Color(0,0,0,0));
			locationIconColumnGridPanel.setSize(tileWidth * columnCount, tileHeight * 2/3);
			locationIconColumnGridPanel.setPreferredSize(new Dimension(tileWidth * columnCount, tileHeight * 2/3));
			locationIconColumnGridPanel.setLocation(startPoint.x + xOffset, startPoint.y - tileHeight - yOffset + (tileHeight / 3));
			terrainLayersScaleListener.AddChild(locationIconColumnGridPanel);
			if(terrainLayersScaleListener.GetRowModel() == null)
				terrainLayersScaleListener.SetRowModel(locationIconColumnGridPanel);
			//locationIconColumnGridPanel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
			
			//For Indicator Overlays (Mission Indicator, maybe more later)
			JPanel missionIndicatorColumnGridPanel = new JPanel(new GridLayout(1, columnCount));
			missionIndicatorColumnGridPanel.setOpaque(false);
			missionIndicatorColumnGridPanel.setBackground(new Color(0,0,0,0));
			missionIndicatorColumnGridPanel.setSize(tileWidth * columnCount, tileHeight * 2/3);
			missionIndicatorColumnGridPanel.setPreferredSize(new Dimension(tileWidth * columnCount, tileHeight * 2/3));
			missionIndicatorColumnGridPanel.setLocation(startPoint.x + xOffset, startPoint.y - tileHeight - yOffset + (tileHeight / 3));
			terrainLayersScaleListener.AddChild(missionIndicatorColumnGridPanel);
			
			//Make a column for buttons
			JPanel debugSymbolColumnGridPanel = null;
			if(isDebuggingGeneration) {
				debugSymbolColumnGridPanel = new JPanel(new GridLayout(1, columnCount));
				debugSymbolColumnGridPanel.setOpaque(false);
				debugSymbolColumnGridPanel.setBackground(new Color(0,0,0,0));
				debugSymbolColumnGridPanel.setSize(tileWidth * columnCount, tileHeight / 3);
				debugSymbolColumnGridPanel.setLocation(startPoint.x + xOffset, startPoint.y - tileHeight - yOffset + (tileHeight / 2));
				terrainLayersScaleListener.AddChild(debugSymbolColumnGridPanel);
				//buttonColumnGridPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
			}
			
			for(int columns = 0; columns < columnCount; columns++) {
				final int xCoord = columns;
				final int yCoord = rowCount - 1 - rows;
				
				WorldTile governingWorldTile = worldMap.get(this.convertToHexPosition(xCoord, yCoord, rowCount));
				
				//Map Location Icons
				JComponent iconImage = null;
				if(governingWorldTile.GetSettlementType() != null) {
					//System.out.println("Adding Map Location Icons for Mission: " + governingWorldTile.GetMission().getName() + " - " + governingWorldTile.GetSettlementType() + ", " + governingWorldTile.GetSettlementDesignation());
					ImagePanel imagePanel = new ImagePanel(GUIUtil.GetSettlementImage(governingWorldTile.GetSettlementType(), governingWorldTile.GetSettlementDesignation()));
					iconImage = imagePanel;
					iconImage.setVisible(false);
					settledWorldTiles.add(governingWorldTile);
					
					//Shrink the icons with an invible border
					imagePanel.SetPaintInsideInsets(true);
					imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
					//Add to ScaleListener for borders that need scaled based on the panels width
					terrainLayersScaleListener.AddBorderScaler(imagePanel, MathOperation.Divide, 7f);
				} else {
					iconImage = new Box.Filler(fillerPref, fillerPref, fillerPref); //Attempted performance increase
				}
				iconImage.setOpaque(false);
				iconImage.setBackground(new Color(0,0,0,0));
				Dimension iconDimension = new Dimension(tileWidth, tileHeight * 2/3);
				iconImage.setSize(iconDimension);
				iconImage.setPreferredSize(iconDimension);
				locationIconColumnGridPanel.add(iconImage);
				
				
				//Create any overlay indicators
				JComponent missionIndicatorImage = null;
				if(governingWorldTile.GetMapLocation().GetMissionIds() != null && governingWorldTile.GetMapLocation().GetMissionIds().size() > 0) {
					//System.out.println("Adding Map Location Icons for Mission: " + governingWorldTile.GetMission().getName() + " - " + governingWorldTile.GetSettlementType() + ", "
					//+ governingWorldTile.GetSettlementDesignation());
					MissionIndicatorType indicatorType = MissionIndicatorType.MissionOrange;
					Mission mission = Missions.getById(governingWorldTile.GetMapLocation().GetMissionIds().get(0));
					boolean isMissionConcludedAndIndicatorRemoved = false;
					switch(mission.getMissionStatus()) {
						case Pending:
							indicatorType = mission.getMissionStructure().missionData.indicator_nextPendingMission;
							break;
						case Active:
							indicatorType = mission.getMissionStructure().missionData.indicator_activeMission;
							break;
						case Concluded:
							if(mission.getMissionStructure().missionData.indicator_removeAfterCompletion) {
								isMissionConcludedAndIndicatorRemoved = true;
								break;
							}
							indicatorType = mission.getMissionStructure().missionData.indicator_completedMission;
							break;
						default:
							System.err.println("WorldmapPanel.CreateWorldmapUI() - Add support for MissionStatusType: "+ mission.getMissionStatus() +". Fallingback to MissionIndicatorType.MissionOrange.");
							break;
					}
					
					if(!isMissionConcludedAndIndicatorRemoved) {
						ImagePanel imagePanel = new ImagePanel(GUIUtil.GetIndicatorOverlayImage(indicatorType));
						missionIndicatorImage = imagePanel;
						imagePanel.setVisible(false);
						missionIndicatedWorldTiles.add(governingWorldTile);
						
						//Shrink the icons with an invible border
						imagePanel.SetPaintInsideInsets(true);
						imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
						//Add to ScaleListener for borders that need scaled based on the panels width
						terrainLayersScaleListener.AddBorderScaler(imagePanel, MathOperation.Divide, 7f);
						
						System.out.println("WorldmapPanel.CreateWorldmapUI() - Adding mission indicator to location: " + governingWorldTile.GetMapLocation().getName() + ", at position: " + 
								governingWorldTile.getPosition() + ", with indicatorType: " + indicatorType);
					} else {
						missionIndicatorImage = new Box.Filler(fillerPref, fillerPref, fillerPref);
					}
				} else {
					missionIndicatorImage = new Box.Filler(fillerPref, fillerPref, fillerPref);
				}
				missionIndicatorImage.setOpaque(false);
				missionIndicatorImage.setBackground(new Color(0,0,0,0));
				missionIndicatorImage.setEnabled(false); //let user input ignore this icon
				overlayDimension = new Dimension(tileWidth, tileHeight * 2/3);
				missionIndicatorImage.setSize(overlayDimension);
				missionIndicatorImage.setPreferredSize(overlayDimension);
				missionIndicatorColumnGridPanel.add(missionIndicatorImage);
				
				
				Point2D hexPoint = convertToHexPosition(xCoord, yCoord, rowCount);
				iconImage.addMouseListener(new MouseListener() {
					@Override
					public void mouseClicked(MouseEvent arg0) {
						ClickTile(hexPoint);
					}
					@Override
					public void mouseEntered(MouseEvent arg0) {
						ToggleTileHover(true, hexPoint);
					}
					@Override
					public void mouseExited(MouseEvent arg0) {
						ToggleTileHover(false, hexPoint);
					}
					@Override
					public void mousePressed(MouseEvent arg0) {}
					@Override
					public void mouseReleased(MouseEvent arg0) {}
				});
				iconImage.addMouseMotionListener(new MouseMotionListener() {
					@Override
					public void mouseDragged(MouseEvent arg0) {
						DragMap(arg0);
					}
					@Override
					public void mouseMoved(MouseEvent arg0) {
						//Mouse move is called only when the user isn;t dragging
						isDragging = false;
					}
				});
				iconImage.addMouseWheelListener(new MouseWheelListener() {
					@Override
					public void mouseWheelMoved(MouseWheelEvent arg0) {
						ZoomMap(arg0);
					}
				});
				
				//Place generation helper symbols on the map
				JComponent tileSymbol = null;
				if(isDebuggingGeneration) {
					tileSymbol = new JLabel();
					if(governingWorldTile.IsEpicenter())
						tileSymbol = new ImagePanel(GUIUtil.WorldmapEpicenterIcon);
					
					tileSymbol.setOpaque(false);
					tileSymbol.setBackground(new Color(0,0,0,0));
					tileSymbol.setSize(tileWidth, tileHeight / 3);
					/*Point2D hexPoint_symbol = convertToHexPosition(xCoord, yCoord, rowCount);
					tileSymbol.addMouseListener(new MouseListener() {
						@Override
						public void mouseClicked(MouseEvent arg0) {
							ClickTile(hexPoint_symbol);
						}
						@Override
						public void mouseEntered(MouseEvent arg0) {
							ToggleTileHover(true, hexPoint_symbol);
						}
						@Override
						public void mouseExited(MouseEvent arg0) {
							ToggleTileHover(false, hexPoint_symbol);
						}
						@Override
						public void mousePressed(MouseEvent arg0) {}
						@Override
						public void mouseReleased(MouseEvent arg0) {}
					});
					
					tileSymbol.addMouseMotionListener(new MouseMotionListener() {
						@Override
						public void mouseDragged(MouseEvent arg0) {
							DragMap(arg0);
						}
						@Override
						public void mouseMoved(MouseEvent arg0) {
							//Mouse move is called only when the user isn;t dragging
							isDragging = false;
						}
					});
					tileSymbol.addMouseWheelListener(new MouseWheelListener() {
						@Override
						public void mouseWheelMoved(MouseWheelEvent arg0) {
							ZoomMap(arg0);
						}
					});*/
					//Instead of having two sets of components with identical interaction events, just let the events pass thru these debug components to the intended event handling component
					tileSymbol.setEnabled(false);
					
					debugSymbolColumnGridPanel.add(tileSymbol);
				}
				
				if(isDebuggingGeneration)
					governingWorldTile.setDebugSymbolComponent(tileSymbol);
				
				governingWorldTile.setSettlementComponent(iconImage);
				governingWorldTile.setMissionIndicatorComponent(missionIndicatorImage);
			}
			
			terrainLayers.add(locationIconColumnGridPanel, 0, rows);
			terrainLayers.add(missionIndicatorColumnGridPanel, 1, rows);
			
			if(isDebuggingGeneration)
				terrainLayers.add(debugSymbolColumnGridPanel, 2, rows);
		}
		
		mapContainer = new JPanel();
		mapContainer.setBackground(Color.BLACK);
		mapContainer.add(terrainLayers);
		//mapContainer.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 3));
		terrainLayers.setLocation(0, 0);
		journeyPanel.add(mapContainer, BorderLayout.CENTER);
		
		Point2D worldLocation = Game.Instance().GetWorldmapLocation();
		if(worldLocation == null) {
			//TODO DEBUGGING - Reenable this block once done overriding start location
			
			MapLocation firstMapLocation = GetMapLocationById( Missions.GetNextAvailableMissions()[0].getMapLocationId() );
			worldLocation = firstMapLocation.getWorldTileData().position;
			System.out.println("Start Mission: " + Missions.GetNextAvailableMissions()[0].getName() + ", at MapLocation: " + firstMapLocation.getName());
			
			//Set this on the first setup of a new game, everytime after that the Game script will load the worldmap location and use that to load the SceneData file
			Game.Instance().SetWorldmapLocation(worldLocation, firstMapLocation);
			
			
			//DEBUGGING - Override the initial start location with a simulated Battle at the first Estate MapLocation existing on the WorldMap
			//Actually, I'm going to kill two birds with one stone by using the approach of moving the first Mission out of DarkForest and into Estate. This will allow me to both test the MapLocation
			//Breakaway functionality during battle and test the flexibility of the Mission plotting logic.
		}
		
		//__ ** Setup Sprites ** __\\
		int layerCount = 2;
		
		//Create cursor sprite
		ignoreEventsDuringSetup = true;
		cursorSprite = new MapSprite(GUIUtil.WorldmapHighlightIcon, worldLocation, new Dimension(tileWidth, tileWidth), new Point2D.Float(0f, 0f), false);
		terrainLayers.add(cursorSprite.GetImagePanel(), layerCount, 0);
		layerCount++;
		
		//Create tile selection sprite
		tileSelectionSprite = new MapSprite(
				GUIUtil.WorldmapEpicenterIcon,
				worldLocation,
				new Dimension((int)Math.round(tileWidth*1.4f), (int)Math.round(tileWidth*1.4f)),
				new Point2D.Float(-0.14f, 0.22f),
				false);
		tileSelectionSprite.GetImagePanel().ConformSizeToAspectRatio(true);
		terrainLayers.add(tileSelectionSprite.GetImagePanel(), layerCount, 0);
		layerCount++;
		//create path node sprites
		for(int i = 0; i < pathNodeSprites.length; i++) {
			pathNodeSprites[i] = new MapSprite(
					GUIUtil.GetTintedImage(GUIUtil.WorldmapEpicenterIcon, Color.LIGHT_GRAY, ColorBlend.Multiply),
					worldLocation,
					new Dimension((int)Math.round(tileWidth), (int)Math.round(tileWidth)),
					new Point2D.Float(0f, 0.5f),
					false);
			pathNodeSprites[i].GetImagePanel().ConformSizeToAspectRatio(true);
			terrainLayers.add(pathNodeSprites[i].GetImagePanel(), layerCount, 0);
			layerCount++;
		}
		
		//Create player sprite
		SpriteSheet walkSheet = SpriteSheetUtility.GetWalkSheet(Game.Instance().GetPlayerData().getType());
		ImagePanel playerImagePanel = CreatePlayerSprite(worldLocation, walkSheet.GetSprite(1, 1, 1), new Dimension(tileHeight * 3/4 * 13/18, tileHeight * 3/4), walkSheet, new Point2D.Float(0.15f, -0.4f));
		terrainLayers.add(playerImagePanel, layerCount, 0);
		layerCount++;
		playerImagePanel.setVisible(false);
		
		//Create tile accessibility sprite that indicates whether a sprite is within travel range or currently "Enterable"
		travelIndicator = new MapSprite(
				GUIUtil.TravelArrow,
				worldLocation,
				new Dimension((int)Math.round(tileWidth*0.5f), (int)Math.round(tileWidth*0.5f)),
				new Point2D.Float(0.5f, -0.8f),
				false);
		travelIndicator.GetImagePanel().ConformSizeToAspectRatio(true);
		//terrainLayers.add(accessibilityIndicator.GetImagePanel(), 5, 0);
		terrainLayers.add(travelIndicator.GetImagePanel(), layerCount, 0);
		layerCount++;
		//Create the arrow indicating that the selected tile will be added to the travel path 
		travelPathIndicator = new MapSprite(
				GUIUtil.TravelPathArrow,
				worldLocation,
				new Dimension((int)Math.round(tileWidth*0.5f), (int)Math.round(tileWidth*0.5f)),
				new Point2D.Float(0.5f, -0.8f),
				false);
		travelPathIndicator.GetImagePanel().ConformSizeToAspectRatio(true);
		//terrainLayers.add(accessibilityIndicator.GetImagePanel(), 5, 0);
		terrainLayers.add(travelPathIndicator.GetImagePanel(), layerCount, 0);
		layerCount++;
		//When reverting to a tile in the path show this arrow instead
		revertPathIndicator = new MapSprite(
				GUIUtil.LeaveArrow,
				worldLocation,
				new Dimension((int)Math.round(tileWidth*0.5f), (int)Math.round(tileWidth*0.5f)),
				new Point2D.Float(0.5f, -0.8f),
				false);
		revertPathIndicator.GetImagePanel().ConformSizeToAspectRatio(true);
		//terrainLayers.add(revertIndicator.GetImagePanel(), 5, 0);
		terrainLayers.add(revertPathIndicator.GetImagePanel(), layerCount, 0);
		layerCount++;
		
		//Should be the last task in ui setup cause it'll call Scaler.CaptureScales() which set all the reference values for the components involved
		SetupDragAndZoom();
		terrainLayersScaleListener.EnableScaling();
		
		partyStamina = Game.Instance().GetPartyStamina();
		
		//System.out.println("&& -- Starting WorldmapPanel -- &&");
	}
	
	MapSprite travelIndicator;
	MapSprite travelPathIndicator;
	MapSprite revertPathIndicator;
	MapSprite[] pathNodeSprites = new MapSprite[10];
	
	boolean ignoreEventsDuringSetup;
	boolean isDragging;
	Point dragStartPoint = new Point();
	
	private void SetupDragAndZoom() {
		screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		double aspectRatio = screenSize.getHeight() / screenSize.getWidth();
		zoomHeightInterval = (int)Math.round(aspectRatio * zoomInterval);
		System.out.println("WorldmapPanel.SetupDragAndZoom() - zoomHeightInterval: " + zoomHeightInterval);
		
		//Record the components initial position and scale norms
		terrainLayersScaleListener.CaptureChildTransforms();
		
		showIconsTimer.setRepeats(false);
		
		///create the component listener to react to terrainLayers adjustments at the correct time, after the pending changes to it have finished
		terrainLayers.addComponentListener(new ComponentListener() {
			@Override
			public void componentHidden(ComponentEvent arg0) {}
			@Override
			public void componentShown(ComponentEvent arg0) {}
			@Override
			public void componentMoved(ComponentEvent arg0) {}
			Point pL = Multiply(terrainLayers.getLocation(), -1f);
			Dimension lastSize = terrainLayers.getSize();
			@Override
			public void componentResized(ComponentEvent arg0) {
				Point viewExtent = Divide(minWorldmapSize, 2f);
				Point pC = Add(pL, viewExtent);
				
				double lastNormX = (double)pC.x / lastSize.width;
				double lastNormY = (double)pC.y / lastSize.height;
				int xCenterOnNewView = (int)Math.round(terrainLayers.getSize().width * lastNormX);
				int yCenterOnNewView = (int)Math.round(terrainLayers.getSize().height * lastNormY);
				Point shiftVector = Subtract(pC, new Point(xCenterOnNewView, yCenterOnNewView));
				ScrollByVectorPosition(shiftVector);
				//System.out.println("pC: " + pC + ", shiftVector: " + shiftVector);
				
				lastSize = terrainLayers.getSize();
				pL = Multiply(GetClampedPosition(Subtract(terrainLayers.getLocation(), shiftVector)), -1f);
			}
		});
	}
	
	//Setup Worldmap - End
	
	
	public void ReloadWorldFromContinue() {
		System.out.println("WorldmapPanel.ReloadWorldFromContinue()");
		
		//Notify Game so that it can track whether the player ended the game in the MapLocation panel or the Worldmap panel
		Game.Instance().SetIsInMapLocation(true);
		//TODO - Send the player back to the Dark Forest and play the respawn dialography
		
		
		//TODO move player back to a respawn location; (WIP Concept) the origin tile or a to-be fixed settlement type like Temples OR Any variety of outcaste settlements
		
		//TODO move any roaming entities back to their previous locations on the worldmap they were occupying when the player traveled to the last "checkpoint"
		
		//TODO focus the view on the players location
		
		//TODO reverse the results of any interactions they've performed since entering the MapLocation; like items received, characters joining the party, missions started,
		//missions finished, etc
		
	}
	
	
	private void DragMap(MouseEvent event) {
		//This method is resonsible for filtering input
		if(event.getModifiersEx() != MouseEvent.BUTTON3_DOWN_MASK)
			return;
		
		Point newPosition = event.getLocationOnScreen();
		if(!isDragging) {
			isDragging = true;
			dragStartPoint = newPosition;
		}
		
		Point dragTravelVector = new Point(newPosition.x - dragStartPoint.x, newPosition.y - dragStartPoint.y);
		//System.out.println("DragMap - dragTravelVector: " + dragTravelVector);
		
		Point oldLoc = terrainLayers.getLocation();
		Point newLoc = GetClampedPosition(Add(terrainLayers.getLocation(), dragTravelVector));
		terrainLayers.setLocation(newLoc);
		Point clampIndicator = new Point(oldLoc.x == newLoc.x ? 0 : 1, oldLoc.y == newLoc.y ? 0 : 1);
		
		//Drag clouds
		for(VectorSprite cloud : cloudSprites) {
			Point clampedVector = new Point(dragTravelVector.x * clampIndicator.x, dragTravelVector.y * clampIndicator.y);
			Point newCloudLoc = Add(cloud.GetImagePanel().getLocation(), clampedVector);
			//cloud.GetImagePanel().setLocation(newLoc);
			//cloud.globalLocation = new Point2D.Float((float)newLoc.x, (float)newLoc.y);
			cloud.globalDestination = newCloudLoc;
			cloud.DoMoveFreeform();
		}
		
		dragStartPoint = newPosition;
	}
	
	private void ScrollByVectorPosition(Point vector) {
		terrainLayers.setLocation(GetClampedPosition(Add(terrainLayers.getLocation(), vector)));
	}
	
	private Point GetClampedPosition(Point targetPosition) {
		Point maxLimit = new Point(-(terrainLayers.getSize().width - minWorldmapSize.width), -(terrainLayers.getSize().height - minWorldmapSize.height));
		int clampedX = Math.min(0, Math.max(maxLimit.x, targetPosition.x));
		int clampedY = Math.min(0, Math.max(maxLimit.y, targetPosition.y));
		return new Point(clampedX, clampedY);
	}
	
	int zoomInterval = 1024;
	int zoomHeightInterval;
	Dimension screenSize;
	Dimension minWorldmapSize = new Dimension(0,0);
	double zoomInLimitMax = 2f; //precentage screen size
	Dimension maxWorldmapSize = new Dimension(0,0);
	boolean isZoomLocked;
	int zoomEventLimit = 4;
	int zoomEventCount;
	boolean areIconsVisible;
	//Populated during UI setup
	List<WorldTile> settledWorldTiles = new ArrayList<WorldTile>();
	List<WorldTile> missionIndicatedWorldTiles = new ArrayList<WorldTile>();
	
	public void AddTileToMissionIndicatedTiles(Point2D tilePosition) {
		missionIndicatedWorldTiles.add(worldMap.get(tilePosition));
	}
	
	private void SetMapLocationIconsVisible(boolean visible) {
		for(WorldTile settledTile : settledWorldTiles) {
			//settledTile.getSettlementComponent().setVisible(visible);
			//TODO make this visible only if the tile has been discivered
			if(settledTile.isDiscovered())
				settledTile.getSettlementComponent().setVisible(visible);
		}
		
		List<Mission> availableMissions = new ArrayList<>( Arrays.asList(Missions.GetNextAvailableMissions()) );
		for(WorldTile missionTile : missionIndicatedWorldTiles) {
			boolean setIndicatorVisible = visible;
			if(setIndicatorVisible) {
				if(!missionTile.isDiscovered()) {
					setIndicatorVisible = false;
				} else {
					//Check available missions
					Mission[] availableMissionsOnThisTile = availableMissions.stream().filter(x -> missionTile.GetMapLocation().GetMissionIds().contains(x.getId())).toArray(Mission[]::new);
					boolean canShowIndicator = false;
					for(Mission availableMission : availableMissionsOnThisTile) {
						System.out.println("WorldmapPanel.SetMapLocationIconsVisible() - Checking availableMission: " + availableMission.getName());
						if(availableMission.getMissionStatus() != MissionStatusType.Pending || !availableMission.getMissionStructure().missionData.indicator_onlyShowUponEntering) {
							canShowIndicator = true;
							break;
						}
					}
					//Also check completed missions
					if(Game.Instance().getMissions().stream().anyMatch(x -> missionTile.GetMapLocation().GetMissionIds().contains(x.getId()) && x.getMissionStatus() == MissionStatusType.Concluded))
						canShowIndicator = true;
					System.out.println("WorldmapPanel.SetMapLocationIconsVisible() - MissionTile: " + missionTile.GetMapLocation().getName() + ", canShowIndicator: " + canShowIndicator);
					setIndicatorVisible = canShowIndicator;
				}
			}
			
			missionTile.getMissionIndicatorComponent().setVisible(setIndicatorVisible);
		}
		
		SetSpritesVisible(visible);
		
		areIconsVisible = visible;
	}
	
	private void SetSpritesVisible(boolean visible) {
		playerSprite.imagePanel.setVisible(visible);
		for(VectorSprite cloud : cloudSprites) {
			if(!cloud.DisableZoomInteractions())
				cloud.GetImagePanel().setVisible(visible);
		}
		if(!visible)
			cursorSprite.GetImagePanel().setVisible(false);
	}
	
	private void ZoomMap(MouseWheelEvent event) {
		if(!isZoomLocked && zoomEventCount >= zoomEventLimit) {
			isZoomLocked = true;	
			//System.out.println("Zoom locked");
			SwingUtilities.invokeLater(zoomUnlock);
		}
		if(isZoomLocked)
			return;
		
		if(areIconsVisible) {
			//System.out.println("Hiding Icons");
			SetMapLocationIconsVisible(false);
			showIconsTimer.start();
			cursorSprite.GetImagePanel().setVisible(false);
		}
		
		int rot = -event.getWheelRotation();
		Dimension previousSize = terrainLayers.getSize();
		//For use with clouds
		terrainLayersSizeBeforeZoom = previousSize;
		//System.out.println("ZoomMap() - previousZoom: " + terrainLayers.getSize());
		
		int deltaX = zoomInterval * rot;
		int deltaY = zoomHeightInterval * rot;	
		int dimX = Math.min(maxWorldmapSize.width, Math.max(minWorldmapSize.width, previousSize.width + deltaX));
		int dimY = Math.min(maxWorldmapSize.height, Math.max(minWorldmapSize.height,  previousSize.height + deltaY));
		Dimension newSize = new Dimension(dimX, dimY);
		//System.out.println("ZoomMap() - newSize: " + newSize);
		
		
		//Get an adjusted size that'll ensure proper alignment of the worldmap image and the UI component grid
		ScaleListener.ChildTransform modelTransform = terrainLayersScaleListener.GetTransformForChild(terrainLayersScaleListener.GetRowModel());
		int resultingRowWidth = (int)Math.round(newSize.width * modelTransform.relativeScale.getX());
		double preciseHeight = (double)resultingRowWidth / worldWidth;
		int adjustedRowWidth = (int)Math.round(preciseHeight) * worldWidth;
		//Work our way back to the parent container size
		int adjustedSourceWidth = (int)Math.round(adjustedRowWidth / modelTransform.relativeScale.getX());

		//int adjustedSourceHeight = (int)Math.round(adjustedSourceWidth * terrainLayersScaleListener.GetSourceAspectRatio());
		double containerAspectRatio = (double)mapContainer.getSize().height / mapContainer.getSize().width;
		int adjustedSourceHeight = (int)Math.round(adjustedSourceWidth * containerAspectRatio);
		
		Dimension adjustedSourceSize = new Dimension(adjustedSourceWidth, adjustedSourceHeight);
		//set our source to match
		newSize = adjustedSourceSize;
		//System.out.println("ZoomMap() - adjustedSize: " + newSize);
		
		
		terrainLayers.setSize(newSize);
		terrainLayers.setPreferredSize(newSize);
		
		zoomEventCount++;
	}
	Dimension terrainLayersSizeBeforeZoom;
	
	private Dimension GetCurrentTileSize() {
		//this method takes zoom into account
		ScaleListener.ChildTransform modelTransform = terrainLayersScaleListener.GetTransformForChild(terrainLayersScaleListener.GetRowModel());
		int resultingRowWidth = (int)Math.round(terrainLayers.getSize().width * modelTransform.relativeScale.getX());
		
		Dimension currentTileSize = new Dimension(Math.round((float)resultingRowWidth / worldWidth), terrainLayersScaleListener.GetRowModel().getHeight());
		System.out.println("WorldmapPanel.GetCurrentTileSize() - currentTileSize: " + currentTileSize);
		return currentTileSize;
	}
	
	Runnable zoomUnlock = new Runnable() {
		@Override
		public void run() {
			//System.out.println("Running zoomUnlock");
			isZoomLocked = false;
			zoomEventCount = 0;
		}
	};
	
	Timer showIconsTimer = new Timer(100, new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			//System.out.println("Running showIcons");
			SetMapLocationIconsVisible(true);
		}
	});
	
	
	private Point Add(Point a, Point b) {
		return new Point(a.x + b.x, a.y + b.y);
	}
	private Point Add(Point a, Dimension b) {
		return new Point(a.x + b.width, a.y + b.height);
	}
	
	private Point Subtract(Point a, Point b) {
		return new Point(a.x - b.x, a.y - b.y);
	}
	/*private Point Subtract(Point a, Dimension b) {
		return new Point(a.x - b.width, a.y - b.height);
	}
	private Point Subtract(Dimension a, Point b) {
		return new Point(a.width - b.x, a.height - b.y);
	}
	private Point Subtract(Dimension a, Dimension b) {
		return new Point(a.width - b.width, a.height - b.height);
	}
	
	private Point Divide(Point a, Point b) {
		return new Point(b.x == 0 ? 0 : (a.x / b.x), b.y == 0 ? 0 : (a.y / b.y));
	}
	private Point2D Divide2D(Point a, Point b) {
		return new Point2D.Double(b.x == 0 ? 0 : ((double)a.x / b.x), b.y == 0 ? 0 : ((double)a.y / b.y));
	}
	private Point Divide(Point a, float factor) {
		return new Point((int)Math.round(a.x / factor), (int)Math.round(a.y / factor));
	}*/
	private Point Divide(Dimension a, float factor) {
		return new Point((int)Math.round(a.width / factor), (int)Math.round(a.height / factor));
	}
	
	//private Point Multiply(Point a, Point b) {
	//	return new Point(a.x * b.x, a.y * b.y);
	//}
	private Point Multiply(Point a, float factor) {
		return new Point((int)Math.round(a.x * factor), (int)Math.round(a.y * factor));
	}
	/*private Point Multiply(Point2D a, Point b) {
		return new Point((int)Math.round(a.getX() * b.x), (int)Math.round(a.getY() * b.y));
	}
	private Point Multiply(Point a, Dimension b) {
		return new Point(a.x * b.width, a.y * b.height);
	}*/

	//World Generation - Start
	
	private void DefineTravelMaps() {
		//Risk ranges from 1 - 10, 1 being the least risk and 10 being the greatest risk
		terrainRiskMap.put(WorldTileType.field, 1);
		terrainRiskMap.put(WorldTileType.prairie, 1);
		terrainRiskMap.put(WorldTileType.marsh, 4);
		terrainRiskMap.put(WorldTileType.farmland, 1);
		terrainRiskMap.put(WorldTileType.barrens, 1);
		terrainRiskMap.put(WorldTileType.forest, 3);
		terrainRiskMap.put(WorldTileType.forestEdge, 1);
		terrainRiskMap.put(WorldTileType.peak, 10);
		terrainRiskMap.put(WorldTileType.plateau, 5);
		terrainRiskMap.put(WorldTileType.foothills, 2);
		terrainRiskMap.put(WorldTileType.dunes, 6);
		terrainRiskMap.put(WorldTileType.water, 8);
		
		//Stamina ranges from {-100, 100}, in intervals of 5. 0 will consume 0 stamina, 50 will consume 50 stamina points, negative values will grant that amount of stamina
		terrainStaminaMap.put(WorldTileType.field, 5);
		terrainStaminaMap.put(WorldTileType.prairie, 5);
		terrainStaminaMap.put(WorldTileType.marsh, 15);
		terrainStaminaMap.put(WorldTileType.farmland, -5);
		terrainStaminaMap.put(WorldTileType.barrens, 5);
		terrainStaminaMap.put(WorldTileType.forest, 10);
		terrainStaminaMap.put(WorldTileType.forestEdge, 5);
		terrainStaminaMap.put(WorldTileType.peak, 30);
		terrainStaminaMap.put(WorldTileType.plateau, 10);
		terrainStaminaMap.put(WorldTileType.foothills, 10);
		terrainStaminaMap.put(WorldTileType.dunes, 15);
		terrainStaminaMap.put(WorldTileType.water, 20);
		
		//Mercantile/Maintenance Focused
		settlementRiskMap.put(SettlementType.Campsite, 2);
		settlementRiskMap.put(SettlementType.Teahouse, 3);
		settlementRiskMap.put(SettlementType.Market, 1);
		settlementRiskMap.put(SettlementType.Blacksmith, 1);
		settlementRiskMap.put(SettlementType.Doctor, 1);
		//Infastructure
		settlementRiskMap.put(SettlementType.Crossroads, 3);
		settlementRiskMap.put(SettlementType.NotableLocation, 4);
		settlementRiskMap.put(SettlementType.QuestBoard, 3);
		settlementRiskMap.put(SettlementType.Estate, 2);
		settlementRiskMap.put(SettlementType.Village, 1);
		settlementRiskMap.put(SettlementType.Garden, 1);
		settlementRiskMap.put(SettlementType.Shrine, 1);
		settlementRiskMap.put(SettlementType.Graveyard, 3);
		settlementRiskMap.put(SettlementType.Castle, 8);
		//Fleeting Locations
		settlementRiskMap.put(SettlementType.MilitaryEncampment, 9);
		settlementRiskMap.put(SettlementType.Battle, 10);
		settlementRiskMap.put(SettlementType.AssassinationTarget, 7);	
		settlementRiskMap.put(SettlementType.ElementalDisturbance, 8);
		settlementRiskMap.put(SettlementType.Kami, 2);
		settlementRiskMap.put(SettlementType.YokaiActivity, 6);
		settlementRiskMap.put(SettlementType.YokaiAttack, 10);
		
		//Mercantile/Maintenance Focused
		settlementStaminaMap.put(SettlementType.Campsite, -5);
		settlementStaminaMap.put(SettlementType.Teahouse, -10);
		settlementStaminaMap.put(SettlementType.Market, 0);
		settlementStaminaMap.put(SettlementType.Blacksmith, 0);
		settlementStaminaMap.put(SettlementType.Doctor, -20);
		//Infastructure
		settlementStaminaMap.put(SettlementType.Crossroads, 0);
		settlementStaminaMap.put(SettlementType.NotableLocation, -5);
		settlementStaminaMap.put(SettlementType.QuestBoard, 0);
		settlementStaminaMap.put(SettlementType.Estate, 5);
		settlementStaminaMap.put(SettlementType.Village, 10);
		settlementStaminaMap.put(SettlementType.Garden, -10);
		settlementStaminaMap.put(SettlementType.Shrine, -30);
		settlementStaminaMap.put(SettlementType.Graveyard, 20);
		settlementStaminaMap.put(SettlementType.Castle, 50);
		//Fleeting Locations
		settlementStaminaMap.put(SettlementType.MilitaryEncampment, 60);
		settlementStaminaMap.put(SettlementType.Battle, 80);	
		settlementStaminaMap.put(SettlementType.AssassinationTarget, 50);
		settlementStaminaMap.put(SettlementType.ElementalDisturbance, 60);
		settlementStaminaMap.put(SettlementType.Kami, -100);	
		settlementStaminaMap.put(SettlementType.YokaiActivity, 50);
		settlementStaminaMap.put(SettlementType.YokaiAttack, 90);
	}
	
	private final Color riverTint = new Color(0f, 0f, 0.4f);
	private final Color lakeTint = new Color(0f, 0.2f, 0f);
	private final Color bayTint = new Color(0f, 0.2f, 0.2f);
	
	public class EnvironmentProcedure {
		public EnvironmentType getEnviType() {
			return enviType;
		}

		public int getGenerationConcentrationMin() {
			return generationConcentrationMin;
		}

		public int getGenerationConcentrationMax() {
			return generationConcentrationMax;
		}

		public int getGenerationRangeMin() {
			return generationRangeMin;
		}

		public int getGenerationRangeMax() {
			return generationRangeMax;
		}

		private EnvironmentType enviType;
		private int generationConcentrationMin;
		private int generationConcentrationMax;
		private int generationRangeMin;
		private int generationRangeMax;
		
		public EnvironmentProcedure(EnvironmentType enviType, int generationConcentrationMin, int generationConcentrationMax, int generationRangeMin, int generationRangeMax) {
			this.enviType = enviType;
			this.generationConcentrationMin = generationConcentrationMin;
			this.generationConcentrationMax = generationConcentrationMax;
			this.generationRangeMin = generationRangeMin;
			this.generationRangeMax = generationRangeMax;
		}
	}
	
	private List<EnvironmentProcedure> procedures = new ArrayList<EnvironmentProcedure>();
	
	//Plot Blending
	public class BlendWeight {
		private WorldTileType tileType;
		private float startRadius;
		//Extra option to introduce randomness in the plotting
		//values == 0f will ignore randomness, values > 0 blend up to next higher weight, values < 0
		//Values are clamped between -1f & 1f, closer to 0 mean less of a chance of randomness, values farther from zero mean a greater chance of randomness
		private float randomnessFactor = 0f;
		public BlendWeight(WorldTileType tileType, float startRadius) {
			this.tileType = tileType;
			this.startRadius = startRadius;
		}
		public BlendWeight(WorldTileType tileType, float startRadius, float randomnessFactor) {
			this(tileType, startRadius);
			
			if(randomnessFactor > 1f || randomnessFactor < -1f) {
				System.err.println("randomnessFactor for: " + tileType.toString() + " must be clamped between[inclusive] -1f & 1f. Clamping.");
				if(randomnessFactor < 0)
					randomnessFactor = -1f;
				else
					randomnessFactor = 1f;
			}
			this.randomnessFactor = randomnessFactor;
		}
	}
	public class PlotBlend {
		EnvironmentType enviType;
		//These represent the radius blending pattern that are applied: with index 0 at the center and index "n" at the edge
		BlendWeight[] weights;
		public PlotBlend(EnvironmentType enviType, BlendWeight[] weights) {
			this.enviType = enviType;
			this.weights = weights;
		}
	}
	List<PlotBlend> plotBlends = new ArrayList<PlotBlend>();
	
	private void InstantiateEnvironmentProcedures() {
		procedures.add(new EnvironmentProcedure(EnvironmentType.Grassland,
												8, 16,
												1, 10));
		procedures.add(new EnvironmentProcedure(EnvironmentType.Marsh,
												6, 10,
												1, 4));
		procedures.add(new EnvironmentProcedure(EnvironmentType.Farmland,
												16, 20,
												0, 4));
		procedures.add(new EnvironmentProcedure(EnvironmentType.Forest,
												4, 8,
												4, 10));
		procedures.add(new EnvironmentProcedure(EnvironmentType.Mountainous,
												6, 12,
												2, 10));
		procedures.add(new EnvironmentProcedure(EnvironmentType.Dunes,
												2, 4,
												1, 4));
		procedures.add(new EnvironmentProcedure(EnvironmentType.Water,
												18, 24,
												1, 4));
		
		plotBlends.add(new PlotBlend(EnvironmentType.Grassland, new BlendWeight[] {
				new BlendWeight(WorldTileType.field, 0f, -0.2f),
				new BlendWeight(WorldTileType.prairie, 0.7f, 0.5f),
		}));
		plotBlends.add(new PlotBlend(EnvironmentType.Marsh, new BlendWeight[] {
				new BlendWeight(WorldTileType.marsh, 0f),
		}));
		plotBlends.add(new PlotBlend(EnvironmentType.Farmland, new BlendWeight[] {
				new BlendWeight(WorldTileType.farmland, 0f, -0.4f),
				new BlendWeight(WorldTileType.barrens, 0.9f, 1f),
		}));
		plotBlends.add(new PlotBlend(EnvironmentType.Forest, new BlendWeight[] {
				new BlendWeight(WorldTileType.forest, 0f, -0.4f),
				new BlendWeight(WorldTileType.forestEdge, 0.9f),
		}));
		plotBlends.add(new PlotBlend(EnvironmentType.Mountainous, new BlendWeight[] {
				new BlendWeight(WorldTileType.peak, 0f, -0.3f),
				new BlendWeight(WorldTileType.plateau, 0.5f, 0.7f),
				new BlendWeight(WorldTileType.foothills, 0.8f, 0.25f),
		}));
		plotBlends.add(new PlotBlend(EnvironmentType.Dunes, new BlendWeight[] {
				new BlendWeight(WorldTileType.dunes, 0f),
		}));
		plotBlends.add(new PlotBlend(EnvironmentType.Water, new BlendWeight[] {
				new BlendWeight(WorldTileType.water, 0f),
		}));
	}
	
	EnvironmentType[] procedureOrder = new EnvironmentType[] {
			EnvironmentType.Grassland,
			EnvironmentType.Forest,
			
			EnvironmentType.Dunes,
			
			EnvironmentType.Mountainous,
			
			EnvironmentType.Marsh,
			EnvironmentType.Water,
			
			EnvironmentType.Farmland,
	};
	
	public class EnvironmentPlot {
		public EnvironmentType getEnviType() {
			return enviType;
		}
		public Point2D getEpicenter() {
			return epicenter;
		}
		public int getRange() {
			return range;
		}
		public List<WorldTile> getTiles() {
			return tiles;
		}

		private EnvironmentType enviType;
		private Point2D epicenter;
		private int range;
		
		public EnvironmentPlot(EnvironmentType enviType, Point2D epicenter, int range) {
			this.enviType = enviType;
			this.epicenter = epicenter;
			this.range = range;
		}
		
		private List<WorldTile> tiles = new ArrayList<WorldTile>();
		public void AddWorldTile(WorldTile tile) {
			DisassociateWorldTile(tile.getPosition());
			
			tile.SetPlot(this);
			tiles.add(tile);
			
			//Also add to worldMap
			worldMap.put(tile.getPosition(), tile);
		}
		public void RemoveWorldTile(WorldTile tile) {
			if(tile.IsEpicenter()) {
				if(isDebuggingGeneration)
					System.err.println("You shouldn't be removing an epicenter tile without a good reason! tile at: " + tile.getPosition());
			}
			tiles.remove(tile);
		}
		
		public void AbsorbPlot(EnvironmentPlot absorbedPlot) {
			if(absorbedPlot == null) {
				System.err.println("EnvironmentPlot.AbsorbPlot() - Why the fuck is absorbedPlot null? this plot epicenter: " + epicenter);
				return;
			}
				
			for(WorldTile newTile : absorbedPlot.getTiles()) {
				//this should avoid adding duplicates
				if(tiles.stream().anyMatch(x -> x.getPosition() == newTile.getPosition()))
					continue;
				//Update tile info
				newTile.ReparentTo(this);
				tiles.add(newTile);
			}
		}
		
		public EnvironmentPlot(EnvironmentPlotData data) {
			this(data.enviType, data.epicenter, data.range);
			for(WorldTileData tileData : data.tileDatas)
				tiles.add(new WorldTile(tileData));
		}
		public EnvironmentPlotData getData() {
			return new EnvironmentPlotData(this);
		}
	}
	Map<EnvironmentType, List<EnvironmentPlot>> plotMap = new HashMap<EnvironmentType, List<EnvironmentPlot>>();
	
	public class WorldTile {
		private EnvironmentType enviType;
		public EnvironmentType getEnviType() { return enviType; }
		private WorldTileType tileType;
		public WorldTileType getTileType() { return tileType; }
		
		private int blankTerrainImageIndex;
		public int getBlankTerrainImageIndex() { return blankTerrainImageIndex; }
		public void SetBlankTerrainImageIndex(int index) {
			blankTerrainImageIndex = index;
		}
		
		private int terrainImageIndex;
		public int getTerrainImageIndex() { return terrainImageIndex; }
		public void SetTerrainImageIndex(int index) {
			terrainImageIndex = index;
		}
		
		private Point2D position;
		public Point2D getPosition() { return position; }
		
		//Used at startup. We must create empty WorldTiles so that terrain generation can run performantly by having GetNeighborPoints set a flag
		public WorldTile(Point2D position) {
			isBlank = true;
			
			//Use this for filling blanks
			this.position = position;
		}
		
		public WorldTile(EnvironmentType enviType, WorldTileType tileType, Point2D position) {
			this.enviType = enviType;
			this.tileType = tileType;
			this.position = position;
			
			//set the tally
			int newAmount = 1;
			if(environmentTally.containsKey(enviType))
				newAmount = environmentTally.get(enviType) + 1;
			environmentTally.put(enviType, newAmount);
			
			//debugging and potential permanent effect
			isRiver = isRiversStage;
			isLake = isPlottingLakes;
			//This may not belong here, not sure yet
			isBay = isPlottingBay;
		}
		
		public WorldTile(EnvironmentType enviType, WorldTileType tileType, Point2D position, boolean isEpicenter) {
			this(enviType, tileType, position);
			this.isEpicenter = isEpicenter;
		}
		
		public WorldTile(WorldTileData data) {
			this(data.enviType, data.tileType, data.position);
			this.settlementType = data.settlementType;
			this.settlementDesignation = data.settlementDesignation;
			this.terrainImageIndex = data.terrainImageIndex;
			
			this.isBlank = data.isBlank;
			this.isEpicenter = data.isEpicenter;
			//When loading a map, certain fields will be null because all the related generation data structures won't be created
			this.plot = null;
			this.isRiver = data.isRiver;
			this.isLake = data.isLake;
			this.isBay = data.isBay;
			
			this.mapLocation = data.mapLocation;
			this.isUniqueLocation = data.isUniqueLocation;
			
			this.blankTerrainImageIndex = data.blankTerrainImageIndex;
			this.discovered = data.isDiscovered;
		}
		public WorldTileData getData() {
			return new WorldTileData(this);
		}
		
		public void OverwriteMapLocation(MapLocation newMapLocation, Point2D newPosition) {
			boolean didOverwriteSelection = false;
			if(Game.Instance().DEBUG_GetSaveDatasSceneSelections().contains(this.mapLocation.getId())) {
				didOverwriteSelection = true;
				Game.Instance().DEBUG_GetSaveDatasSceneSelections().remove(this.mapLocation.getId());
			}
			
			
			this.enviType = newMapLocation.getEnviType();
			this.tileType = newMapLocation.getTileType();
			this.settlementType = newMapLocation.getSettlementType();
			this.settlementDesignation = newMapLocation.getSettlementDesignation();
			this.mapLocation = newMapLocation;
			this.position = newPosition;
			
			
			if(didOverwriteSelection)
				Game.Instance().DEBUG_GetSaveDatasSceneSelections().add(this.mapLocation.getId());
		}
		
		//Generation variables
		//flag describing that this tile hasn't been set by terrain generation yet 
		private boolean isBlank;
		public boolean IsBlank() {return isBlank; }
		//flag describing whether this tile should be disqualified from GetNeighborTiles searches
		private boolean isSetAsNeighbor;
		public boolean isSet() { return isSetAsNeighbor; }
		public void Set(boolean set) {
			isSetAsNeighbor = set;
		}
		//used to avoid overlapping an epicenter
		private boolean isEpicenter;
		public boolean IsEpicenter() { return isEpicenter; }
		public void SetAsEpicenter() {
			isEpicenter = true;
			if(plot != null)
				plot.epicenter = position;
		}
		private EnvironmentPlot plot;
		public EnvironmentPlot GetPlot() { return plot; }
		public void SetPlot(EnvironmentPlot plot) {
			this.plot = plot;
			
			//debugging and potential permanent effect
			isRiver = isRiversStage;
			isLake = isPlottingLakes;
			isBay = isPlottingBay;
		}
		
		public void ReparentTo(EnvironmentPlot newParentPlot) {
			SetPlot(newParentPlot);
			isEpicenter = false;
		}
		
		//Debugging and/or permanent tinting if it looks good
		private boolean isRiver;
		public boolean IsRiver() { return isRiver; }
		private boolean isLake;
		public boolean IsLake() { return isLake; }
		private boolean isBay;
		public boolean IsBay() { return isBay; }
		public void ResetTint() {
			isRiver = false;
			isLake = false;
			isBay = false;
		}
		
		//UI functionality
		private JComponent settlementImagePanel;
		public JComponent getSettlementComponent() { return settlementImagePanel; }
		public void setSettlementComponent(JComponent settlementImagePanel) {
			this.settlementImagePanel = settlementImagePanel;
		}
		
		private JComponent missionIndicatorImagePanel;
		public JComponent getMissionIndicatorComponent() { return missionIndicatorImagePanel; }
		public void setMissionIndicatorComponent(JComponent missionIndicatorImagePanel) {
			this.missionIndicatorImagePanel = missionIndicatorImagePanel;
		}
		
		//These two are for debugging the generation system
		private ImagePanel terrainImagePanel; //The hexigon terrain image
		public ImagePanel getTerrainComponent() { return terrainImagePanel; }
		public void SetTerrainComponent(ImagePanel terrainImagePanel) {
			this.terrainImagePanel = terrainImagePanel;
		}
		private JComponent debugSymbolPanel; //Indicators like epicenter markers
		public JComponent getDebugSymbolComponent() { return debugSymbolPanel; }
		public void setDebugSymbolComponent(JComponent debugSymbolPanel) {
			this.debugSymbolPanel = debugSymbolPanel;
		}
		
		//Runtime functionality
		//private Mission mission;
		//public Mission GetMission() { return mission; }
		/*public List<String> GetMissionIds() {
			return new ArrayList<String>( this.mapLocation.GetMissionIds() );
		}*/
		//just directly access the mapLocation.GetMissionIds() instead
		
		//public void SetMission(Mission mission) {
			//this.mission = mission;
			//this.mission.SetWorldTileData(this.getData());
		//}
		//I believe this should also be setting the MapLocation because thats the crucial link between the mission and the world
		public void AddMission(Mission mission) {
			System.out.println("WorldmapPanel.SetMission() - Updating worldTileData.");
			//this.mission = mission;
			//this.mapLocation = mission.getMapLocation();
			//this.mission.SetWorldTileData(this.getData());
			//this.settlementType = mission.getMapLocation().getSettlementType();
			//this.settlementDesignation = mission.getMapLocation().getSettlementDesignation();
			mission.setMapLocationId(this.mapLocation.getId());
			this.mapLocation.AddMission(mission.getId());
		}
		
		private MapLocation mapLocation;
		public MapLocation GetMapLocation() { return mapLocation; }
		private boolean isUniqueLocation;
		public boolean IsUniqueLocation() { return isUniqueLocation; }
		
		public void SetMapLocationFromTemplate(MapLocation mapLocation) {
			if(mapLocation.getId() != null && !mapLocation.getId().isEmpty())
				System.err.println("WorldmapPanel.WorldTile.SetMapLocationFromTemplate() - The provided mapLocation has an id: " + mapLocation.getId() + ", but it shouldn't."
						+ " Templates are expected to be id-less to signify their"
						+ " generic usage from the design side. Upon being attached to a WorldTile they receive a random Id that they'll carry for their lifetimes.");
			
			this.mapLocation = mapLocation;
			this.mapLocation.SetRandomId();
			this.enviType = mapLocation.getEnviType();
			this.tileType = mapLocation.getTileType();
			this.settlementType = mapLocation.getSettlementType();
			this.settlementDesignation = mapLocation.getSettlementDesignation();
			isUniqueLocation = false;
			this.mapLocation.SetWorldTileData(this.getData());
			
			//DEBUG
			if(!DEBUG_recordedGenericLocationSigTypes.stream().anyMatch(x -> x.worldType == this.mapLocation.getTileType() && x.settlementType == this.mapLocation.getSettlementType())) {
				Game.Instance().DEBUG_GetSaveDatasSceneSelections().add(this.mapLocation.getId());
				DEBUG_recordedGenericLocationSigTypes.add(new LocationSignature(this.mapLocation.getTileType(), this.mapLocation.getSettlementType()));
			}
		}
		
		public void SetMapLocationFromUniqueLocation(MapLocation mapLocation) {
			if(mapLocation.getId() == null || mapLocation.getId().isEmpty())
				System.err.println("WorldmapPanel.WorldTile.SetMapLocationFromUniqueLocation() - The provided mapLocation is missing its id. Unique MapLocation are expected to have an id generated on the"
						+ " design side to indicate their one-of-a-kind usage. Unique locations carry their ids through design to, and through, their application lifetimes.");
			else
				System.out.println("WorldmapPanel.WorldTile.SetMapLocationFromUniqueLocation() - Setting unique location: " + mapLocation.getName());
			
			this.mapLocation = mapLocation;
			this.enviType = mapLocation.getEnviType();
			this.tileType = mapLocation.getTileType();
			this.settlementType = mapLocation.getSettlementType();
			this.settlementDesignation = mapLocation.getSettlementDesignation();
			isUniqueLocation = true;
			this.mapLocation.SetWorldTileData(this.getData());
			
			//DEBUG
			Game.Instance().DEBUG_GetSaveDatasSceneSelections().add(this.mapLocation.getId());
		}
		
		//public boolean HasMissionOrStaticMapLocation() { return mapLocation.GetMissionIds().size() > 0; }
		//This style of conditional logic is now invalid, From start to finish a WorldTile will always have a MapLocation
		
		private SettlementType settlementType;
		public SettlementType GetSettlementType() {
			/*if(mission != null && mission.getMapLocation() != null) {
				return mission.getMapLocation().getSettlementType();
			} else if(mapLocation != null) {
				return mapLocation.getSettlementType();
			} else {
				return null;
			}*/
			//this is the old way which is no longer sufficient
			
			return settlementType;
		}
		private SettlementDesignation settlementDesignation;
		public SettlementDesignation GetSettlementDesignation() {
			/*if(mission != null && mission.getMapLocation() != null) {
				return mission.getMapLocation().getSettlementDesignation();
			} else if(mapLocation != null) {
				return mapLocation.getSettlementDesignation();
			} else {
				return null;
			}*/
			//this is the old way which is no longer sufficient
			
			return settlementDesignation;
		}
		
		// <- Fog of War -
		
		private boolean discovered;
		public boolean isDiscovered() { return discovered; }
		
		public void Discover() {
			ShowTile_Full();
			
			//discover surrounding tiles
			List<WorldTile> adjacentTiles = GetAdjacentTiles(this);
			adjacentTiles.stream().forEach((x) -> { x.DiscoverPassThru_Full(); });
		}
		
		public void DiscoverPassThru_Full() {
			ShowTile_Full();
		}
		
		public void ShowTile_Full() {
			discovered = true;
			
			//Show stuff
			terrainImagePanel.SetNewImage( SpriteSheetUtility.GetTerrainFromWorldTile(tileType)[this.terrainImageIndex] );
			Color tintColor = GetTileTint(this);
			if(tintColor != null)
				terrainImagePanel.SetTint(tintColor);
			if(settlementImagePanel != null)
				settlementImagePanel.setVisible(true);
			//Just don't fuck with MissionIndicators here AT ALL!
		}
		
		//  - Fog of War ->
	}
	
	
	//DEBUG - Start - Record one of each type of possible MapLocation configuration
	public class LocationSignature {
		public LocationSignature(WorldTileType worldType, SettlementType settlementType) {
			this.worldType = worldType;
			this.settlementType = settlementType;
		}
		public WorldTileType worldType;
		public SettlementType settlementType;
	}
	public List<LocationSignature> DEBUG_recordedGenericLocationSigTypes = new ArrayList<LocationSignature>();
	//DEBUG - End
	
	
	public void IndicateActiveMissionOnCurrentTile() {
		UpdateTilesMissionIndicator(playerSprite.GetWorldLocation(), mapLocationPanel.GetActiveMission().getMissionStructure().missionData.indicator_nextPendingMission);
	}
	
	public void HandleMissionIndicator(Mission mission) {
		System.out.println("WorldmapPanel.HandleMissionIndicator() - mission: " + mission.getName());
		
		MissionData missionData = mission.getMissionStructure().missionData;
		WorldTile missionsTile = worldMap.values().stream().filter(x -> x.GetMapLocation().getId().equals(mission.getMapLocationId())).findFirst().get();
		
		switch(mission.getMissionStatus()) {
			case Pending:
				if(missionData.indicator_revealTileOnMap)
					missionsTile.ShowTile_Full();
				if(!missionData.indicator_onlyShowUponEntering)
					UpdateTilesMissionIndicator(missionsTile.getPosition(), mission.getMissionStructure().missionData.indicator_nextPendingMission);
				break;
			case Active:
				UpdateTilesMissionIndicator(missionsTile.getPosition(), mission.getMissionStructure().missionData.indicator_activeMission);
				break;
			case Concluded:
				MissionIndicatorType indicatorType = mission.getMissionStructure().missionData.indicator_removeAfterCompletion ? null : mission.getMissionStructure().missionData.indicator_completedMission;
				UpdateTilesMissionIndicator(missionsTile.getPosition(), indicatorType);
				break;
			default:
				System.err.println("WorldmapPanel.HandleMissionIndicator() - Add support for MissionStatusType: " + mission.getMissionStatus());
				break;
		}
	}
	
	private void UpdateTilesMissionIndicator(Point2D tileLocation, MissionIndicatorType missionIndicatorType) {
		System.out.println("WorldmapPanel.UpdateTilesMissionIndicator() - tileLocation: " + tileLocation + ", missionIndicatorType: " + missionIndicatorType);
		
		WorldTile currentTile = worldMap.get(tileLocation);
		JComponent missionIndicatorImage = null;
		if(missionIndicatorType != null) {
			missionIndicatorImage = new ImagePanel(GUIUtil.GetIndicatorOverlayImage(missionIndicatorType));
			missionIndicatorImage.setOpaque(false);
			missionIndicatorImage.setBackground(new Color(0,0,0,0));
			missionIndicatorImage.setEnabled(false); //let user input ignore this icon
			missionIndicatorImage.setSize(overlayDimension);
			missionIndicatorImage.setPreferredSize(overlayDimension);
		} else {
			missionIndicatorImage = new Box.Filler(fillerPref, fillerPref, fillerPref);
			missionIndicatorImage.setOpaque(false);
			missionIndicatorImage.setBackground(new Color(0,0,0,0));
			missionIndicatorImage.setEnabled(false);
			missionIndicatorImage.setSize(overlayDimension);
			missionIndicatorImage.setPreferredSize(overlayDimension);
		}
		
		JPanel parentPanel = (JPanel)currentTile.getMissionIndicatorComponent().getParent();
		int compIndex = (int) Math.floor(currentTile.getPosition().getX());
		
		parentPanel.remove(currentTile.getMissionIndicatorComponent());
		currentTile.setMissionIndicatorComponent(missionIndicatorImage);
		
		parentPanel.add(missionIndicatorImage, compIndex);
		parentPanel.revalidate();
		parentPanel.repaint(100);
		
		currentTile.getMissionIndicatorComponent().setVisible(true);
	}
	
	//position store as
	//[0f  , 2f]  [int       , int] for every other row starting from the bottom-most row
	//[0.4f, 1f]  [int + 0.4f, int] for rows shifted to the right
	//0.4f signifies that the tile is in a shifted row. This position scheme allows for easy rectangular grid conversion via rounding
	Map<Point2D, WorldTile> worldMap; //= new HashMap<Point2D, WorldTile>();
	Map<EnvironmentType, Integer> environmentTally = new HashMap<EnvironmentType, Integer>();
	
	//These tiles lack a plot to contain them and thus, consequently, are neither spatially aware nor considered for the MapLocation association process
	private WorldTile ManuallyPlotTile(EnvironmentType enviType, WorldTileType tiletype, int x, int y, int worldHeight) {
		Point2D hexPosition = convertToHexPosition(x, y, worldHeight);
		DisassociateWorldTile(hexPosition);
		WorldTile newTile = new WorldTile(enviType, tiletype, hexPosition);
		worldMap.put(hexPosition, newTile);
		//This should not be here, it was an early remnant and its only purpose is to prevent ploting on the ocean border
		//newTile.Set(true);
		return worldMap.get(hexPosition); //return the new tile if one would like to set a manually tile and exclude it from neighbor searches in one fell swoop
	}
	
	//remove the tile from its EnvironmentPlot if it was one
	private void DisassociateWorldTile(Point2D hexPosition) {
		WorldTile existingTile = worldMap.get(hexPosition);
		if(existingTile == null || existingTile.IsBlank())
			return;
		EnvironmentPlot plot = existingTile.GetPlot();
		if(plot != null)
			plot.RemoveWorldTile(existingTile);
		
		//Untally the tile
		if(environmentTally.containsKey(existingTile.getEnviType()))
			environmentTally.put(existingTile.getEnviType(), environmentTally.get(existingTile.getEnviType())-1);
	}
	
	private final int oceanBorderThickness = 2;
	
	private boolean IsInsideOceanBorder(Point2D hexPoint, int width, int height) {
		int x = (int)Math.round((float)hexPoint.getX());
		int y = (int)hexPoint.getY();
		if(x >= oceanBorderThickness && x < width - oceanBorderThickness && y >= oceanBorderThickness && y < height - oceanBorderThickness)
			return true;
		else
			return false;
	}
	
	private void CreateMap(int width, int height) {
		//Prepare worldMap for flagging
		//Also add a border of water
		if(isDebuggingGeneration)
			System.out.println("- Stage WORLD BORDER -");
		worldMap = new HashMap<Point2D, WorldTile>();
		for(int y = 0; y < height; y++) {
			if(y >= oceanBorderThickness && y < height - oceanBorderThickness) {
				for(int x = 0; x < width; x++) {
					if(x >= oceanBorderThickness && x < width - oceanBorderThickness) {
						Point2D hexPoint = this.convertToHexPosition(x, y, height);
						WorldTile blankTile = new WorldTile(hexPoint);
						worldMap.put(hexPoint, blankTile);
					} else {
						ManuallyPlotTile(EnvironmentType.Water, WorldTileType.water, x, y, height).Set(true);
					}
				}
			} else {
				//If we're on the top or bottom rows then just make all of the tiles in the row into water automatically
				for(int columnIndex = 0; columnIndex < width; columnIndex++)
					ManuallyPlotTile(EnvironmentType.Water, WorldTileType.water, columnIndex, y, height).Set(true);
			}
		}
		
		//shrink operating area for terrain generaion for the ocean border
		int shrunkenWidth = width - (oceanBorderThickness * 2);
		int shrunkenHeight = height - (oceanBorderThickness * 2);
		
		//Prepare for generation
		InstantiateEnvironmentProcedures();
		
		//create EnvironmentPlots based on EnvironmentProcedures
		if(isDebuggingGeneration)
			System.out.println("- Stage ENVIRONMENT PROCEDURES -");
		for(EnvironmentType orderedType : procedureOrder) {
			//This is for tile tinting
			isPlottingLakes = orderedType == EnvironmentType.Water;
			
			Optional<EnvironmentProcedure> opt = procedures.stream().filter(x -> x.enviType == orderedType).findFirst();
			EnvironmentProcedure procedure = opt.isPresent() ? opt.get() : null;
			if(procedure == null) {
				System.err.println("Couldn't find procedure for EnvironmentType: " + orderedType + ". Continuing.");
				continue;
			}
			
			Random r_gen = new Random();
			
			//Decide how many of each Environment we want
			int occurance = procedure.generationConcentrationMin + r_gen.nextInt(procedure.generationConcentrationMax - procedure.generationConcentrationMin);
			
			List<EnvironmentPlot> typedPlots = new ArrayList<EnvironmentPlot>();
			for(int o = 0; o < occurance; o++) {
				Point2D position = null;
				do {
					//position = convertToHexPosition(r.nextInt(width), r.nextInt(height), height);
					position = convertToHexPosition(oceanBorderThickness + r_gen.nextInt(shrunkenWidth), oceanBorderThickness + r_gen.nextInt(shrunkenHeight), height);
				} while(worldMap.get(position).IsEpicenter());
				
				int range = procedure.generationRangeMin + r_gen.nextInt(procedure.generationRangeMax - procedure.generationRangeMin);
				typedPlots.add( new EnvironmentPlot(procedure.getEnviType(), position, range) );
			}
			
			//Now that we have our plots we can go about creating clusters of worldTiles surrounding them, using blending when possible(main forest, 2nd degree forest, 3rd degree forest)
			for(EnvironmentPlot plot : typedPlots) {
				//add the center tile, the epicenter
				WorldTile worldTile = new WorldTile(plot.getEnviType(), GetWorldType(plot.getEnviType(), 0, plot.getRange()), plot.getEpicenter(), true);
				plot.AddWorldTile(worldTile);
				
				if(isDebuggingGeneration)
					System.out.println("Epicenter: " + plot.getEpicenter() + ", Environment: " + plot.getEnviType() + ", range: " + plot.getRange());
				
				List<WorldTile> preexistingEpicenters = new ArrayList<WorldTile>();
				if(plot.getRange() > 0) {
					//start on 1 because we are ignoring ranges of 0, we need to look at ring as the literal distance from center to ring cells
					for(int ring = 1; ring <= plot.getRange(); ring++) {
						for(Point2D hexPoint : GetNeighborPoints(plot.getTiles(), width, height)) {
							//Continue if this tile is already assigned as a plot epicenter
							WorldTile preexistingTile = worldMap.get(hexPoint);
							//System.out.println("preexistingTile: " + preexistingTile.getEnviType() + ", " + preexistingTile.getTileType());
							if(preexistingTile.IsEpicenter()) {
								//System.out.println("** Preserving Epicenter: " + hexPoint);
								preexistingEpicenters.add(preexistingTile);
								continue;
							}

							//Environment Blending - Blend environments via "priority" values that compare environments to determine which will persist
							//preexistingTile
							
							
							worldTile = new WorldTile(plot.getEnviType(), GetWorldType(plot.getEnviType(), ring, plot.getRange()), hexPoint);
							plot.AddWorldTile(worldTile);
							//System.out.println("		Chosen point: " + hexPoint.toString());
						}
					}
				}
				//Reset the epicenters of other plots that were used by GetNeighborPoints
				preexistingEpicenters.forEach(x -> x.Set(false));
				
				//Reset all tile flags used by GetNeighborPoints so the next procedure has a clean slate
				plot.getTiles().forEach(x -> x.Set(false));
			}
			
			plotMap.put(procedure.getEnviType(), typedPlots);
		}
		
		//Fill any remaining blankspots, using a tally of all environment types pick environments with the least presence to help balance the world
		if(isDebuggingGeneration)
			System.out.println("- Stage FILL BLANKS -");
	
		List<WorldTile> blankTiles = new LinkedList<WorldTile>(  Arrays.asList( worldMap.values().stream().filter(x -> x.IsBlank()).toArray(WorldTile[]::new) )  );
		if(blankTiles.size() > 0) {
			if(isDebuggingGeneration)
				System.out.println("BlankTiles: " + blankTiles.size());
			final int clusterSizeCap = 40;
			List<List<WorldTile>> blankClusters = new ArrayList<List<WorldTile>>();
			while(blankTiles.size() > 0) {
				WorldTile startTile = blankTiles.get(0);
				if(isDebuggingGeneration)
					System.out.println("    Start group with: " + startTile.getPosition());
				
				//get cluster
				List<WorldTile> blankCluster = new ArrayList<WorldTile>();
				blankCluster.add(startTile);
				List<WorldTile> lastBlankNeighbors = new ArrayList<WorldTile>();
				do {
					List<Point2D> lastNeighborPoints = GetNeighborPoints(blankCluster, width, height);
					
					lastBlankNeighbors.clear();
					for(Point2D point : lastNeighborPoints) {
						WorldTile tile = worldMap.get(point);
						if(tile.IsBlank())
							lastBlankNeighbors.add(tile);
					}
					
					if(lastBlankNeighbors.size() > 0) {
						if(isDebuggingGeneration)
							System.out.println("        -Found blank neighbors: " + lastBlankNeighbors.size());
						blankCluster.addAll(lastBlankNeighbors);
					}
				} while(lastBlankNeighbors.size() > 0 && blankCluster.size() < clusterSizeCap);
				
				if(isDebuggingGeneration)
					System.out.println("        Collected section, count: " + blankCluster.size());
				
				//remove tiles in order to exaust the search
				blankTiles.removeAll(blankCluster);
				
				blankClusters.add(blankCluster);
			}
			if(isDebuggingGeneration)
				System.out.println("Collected all blankClusters, count: " + blankClusters.size());
			
			//sort by tally vlaue
			environmentTally = MapUtil.sortByValue(environmentTally);
			if(isDebuggingGeneration) {
				for(EnvironmentType enviType : environmentTally.keySet()) {
					System.out.println("Environment: " + enviType + ", tally: " + environmentTally.get(enviType).toString());
				}
			}
			EnvironmentType[] sortedTypes = environmentTally.keySet().stream().toArray(EnvironmentType[]::new);
			//preferencially add forest first regardless of its tally value
			sortedTypes[2] = sortedTypes[1];
			sortedTypes[1] = sortedTypes[0];
			sortedTypes[0] = EnvironmentType.Forest;
			//use the clusters and the environmentTally
			int uniqueEnviFillTypes = 3;
			//use 1/3 of the clusters for the first three least-occuring environments
			int clustersIndex = 0;
			for(int e = 0; e < uniqueEnviFillTypes; e++) {
				EnvironmentType envi = sortedTypes[e];
				int enviPlotCount = Math.max( Math.min( Math.round(blankClusters.size() / 3f), blankClusters.size()) , 1);
				if(clustersIndex >= blankClusters.size()) {
					if(isDebuggingGeneration)
						System.out.println("Used all the blankClusters early. Breaking.");
					break;
				}
				if(e == uniqueEnviFillTypes - 1) //if last iteration, use all remaining clusters
					enviPlotCount = blankClusters.size() - clustersIndex;
				if(isDebuggingGeneration)
					System.out.println("First Environment to fill: " + envi.toString() + ", with cluster count: " + enviPlotCount);
				//intergrate with the EnvironmentPlot system so these clusters can be used later by the MapLocation/Mission associations process
				List<EnvironmentPlot> plotList = plotMap.get(envi);
				for(int fC = 0; fC < enviPlotCount; fC++) {
					List<WorldTile> cluster = blankClusters.get(clustersIndex + fC);
					EnvironmentPlot clusterPlot = new EnvironmentPlot(envi, cluster.get(0).getPosition(), -1);
					for(WorldTile blankTile : cluster) {
						WorldTile newTile = null;
						if(clusterPlot.getTiles().size() == 0) //Make epicenter
							newTile = new WorldTile(envi, GetWorldType(envi, cluster.indexOf(blankTile), cluster.size()), blankTile.getPosition(), true);
						else
							newTile = new WorldTile(envi, GetWorldType(envi, cluster.indexOf(blankTile), cluster.size()), blankTile.getPosition());
						clusterPlot.AddWorldTile(newTile);
					}
					plotList.add(clusterPlot);
				}
				plotMap.put(envi, plotList);
				clustersIndex += enviPlotCount;
			}
		} else {
			if(isDebuggingGeneration)
				System.out.println("There are no blank spaces. Skipping the FILL BLANKS stage.");
		}
		
		//This happens after FILL BLANKS because we need all the continent populated so we know where to apply beaches
		if(isDebuggingGeneration)
			System.out.println("- Stage COASTAL EROSION -");
		//add small-medium sized water plots on a random range section stretching from the world edge to 1/4 map inland
		EnvironmentProcedure errosionProcedure = new EnvironmentProcedure(EnvironmentType.Water, 30, 31, 2, 7);
		ErodeCoast(errosionProcedure, width, height, 5, 5);
		
		//Create rivers leading from mountains and lakes to world edges via loosely directional winding
		if(isDebuggingGeneration)
			System.out.println("- Stage RIVERS -");
		CreateRivers(8, 12, width, height);
		
		if(isDebuggingGeneration)
			System.out.println("- Stage MAP LOCATION ASSOCIATION -");
		PlaceLocations();
	}
	
	Point2D newErosionEpicenter;
	
	List<EnvironmentPlot> erosionPlots = new ArrayList<EnvironmentPlot>();
	
	private void ErodeCoast(EnvironmentProcedure procedure, int width, int height, int rangeInland, int distanceBetweenEpicenters) {
		//Ensure that this flag is off while repurposing lake tiles
		isPlottingLakes = false;
		
		//Decide how many of each Environment we want
		int occurance = procedure.generationConcentrationMin + r.nextInt(procedure.generationConcentrationMax - procedure.generationConcentrationMin);
		
		erosionPlots = new ArrayList<EnvironmentPlot>();
		Rectangle rect = new Rectangle(rangeInland, rangeInland, width - (rangeInland*2), height - (rangeInland*2));
		for(int o = 0; o < occurance; o++) {
			Point2D position = null;
			int xInt = 0;
			int yInt = 0;
			do {
				xInt = r.nextInt(width);
				yInt = r.nextInt(height);
				position = convertToHexPosition(xInt, yInt, height);
				newErosionEpicenter = position;
			} while(
					worldMap.get(position).IsEpicenter()
					||
					rect.contains(new Point(xInt, yInt))
					||
					erosionPlots.stream().anyMatch(x -> x.getEpicenter().distance(newErosionEpicenter) < distanceBetweenEpicenters)  //choose a point thats a certain range away from other epicenters
			);
			
			int range = procedure.generationRangeMin + r.nextInt(procedure.generationRangeMax - procedure.generationRangeMin);
			erosionPlots.add( new EnvironmentPlot(procedure.getEnviType(), position, range) );
		}
		
		//This is for finding inland erosion plot
		worldMap.values().forEach(x -> x.Set(false));
		
		//Now that we have our plots we can go about creating clusters of worldTiles surrounding them, using blending when possible(main forest, 2nd degree forest, 3rd degree forest)
		List<EnvironmentPlot> potentialInlandLakes = new ArrayList<EnvironmentPlot>();
		List<WorldTile> oceanTileNetwork = new ArrayList<WorldTile>();
		for(EnvironmentPlot plot : erosionPlots) {
			//add the center tile, the epicenter
			WorldTile worldTile = new WorldTile(plot.getEnviType(), GetWorldType(plot.getEnviType(), 0, plot.getRange()), plot.getEpicenter(), true);
			plot.AddWorldTile(worldTile);
			
			if(isDebuggingGeneration)
				System.out.println("Erosion Epicenter: " + plot.getEpicenter() + ", Environment: " + plot.getEnviType());
			
			//decide up front if this plot will create a beach on its border
			boolean createBeach = Math.abs(r.nextInt(101))/100f >= 0.5f;
			EnvironmentPlot beach = null;
			
			if(plot.getRange() > 0) {
				//start on 1 because we are ignoring ranges of 0, we need to look at ring as the literal distance from center to ring cells
				for(int ring = 1; ring <= plot.getRange(); ring++) {
					for(Point2D hexPoint : GetNeighborPoints(plot.getTiles(), width, height)) {
						WorldTile preexistingTile = worldMap.get(hexPoint);
						//System.out.println("preexistingTile: " + preexistingTile.getEnviType() + ", " + preexistingTile.getTileType());
						
						//Continue if this tile is already assigned as a plot epicenter
						if(preexistingTile.IsEpicenter()) {
							//System.out.println("** Preserving Epicenter: " + hexPoint);
							continue;
						}

						//Environment Blending - Blend environments via "priority" values that compare environments to determine which will persist
						//preexistingTile
						
						
						//Add beach back along erosion plot border
						List<Point2D> neighborPoints = GetNeighborPoints(Arrays.asList(new WorldTile[]{preexistingTile}), width, height);
						boolean aNeighborIsLand = neighborPoints.stream().anyMatch(x -> worldMap.get(x).getEnviType() != EnvironmentType.Water);
						neighborPoints.stream().forEach(x -> worldMap.get(x).Set(false));
						boolean IsInsideOceanBorder = IsInsideOceanBorder(hexPoint, width, height);
						if(ring == plot.getRange() && createBeach && aNeighborIsLand && IsInsideOceanBorder) {
							if(beach == null)
								beach = new EnvironmentPlot(EnvironmentType.Dunes, hexPoint, -1);
							worldTile = new WorldTile(EnvironmentType.Dunes, WorldTileType.dunes, hexPoint, beach.getTiles().size() == 0);
							beach.AddWorldTile(worldTile);
						} else { //Add water as we normally would
							worldTile = new WorldTile(plot.getEnviType(), GetWorldType(plot.getEnviType(), ring, plot.getRange()), hexPoint);
							plot.AddWorldTile(worldTile);
							//System.out.println("		Chosen point: " + hexPoint.toString());
						}
					}
				}
			}
			
			if(beach != null) { //if theres been a beach created for this plot then add it to our world's dune plots list
				if(isDebuggingGeneration)
					System.out.println("    -Created beach, tile count: " + beach.getTiles().size());
				beach.getTiles().forEach(x -> x.Set(false));
				List<EnvironmentPlot> dunePlots = plotMap.get(EnvironmentType.Dunes);
				dunePlots.add(beach);
				plotMap.put(EnvironmentType.Dunes, dunePlots);
			}
			
			
			//Check for the potential of this erosion plot being an inland lake
			if(isDebuggingGeneration)
				System.out.println("  Checking connection to ocean.");
			List<EnvironmentPlot> adjacentErosionPlots = new ArrayList<EnvironmentPlot>();
			List<Point2D> neighborPoints = GetNeighborPoints(plot.getTiles(), width, height);
			plot.getTiles().forEach(x -> x.Set(false));
			neighborPoints.stream().forEach(x -> worldMap.get(x).Set(false));
			boolean isConnectedToOcean = false;
			for(Point2D point : neighborPoints) {
				WorldTile tile = worldMap.get(point);
				
				if(isDebuggingGeneration)
					System.out.println("    -Inspecting neighbor: " + tile.getEnviType() + ", " + tile.getPosition());
				
				if(tile.GetPlot() == null) {
					isConnectedToOcean = true;
					
				//if we're touching another erosion plot then record it
				} else if(!plotMap.get(tile.getEnviType()).contains(tile.GetPlot())) {
					adjacentErosionPlots.add(tile.GetPlot());
					
					//Still need to mark this as an ocean tile even if we're not touching an ocran border tile directly
					if(oceanTileNetwork.contains(tile))
						isConnectedToOcean = true;
				}
			}
			if(isConnectedToOcean) {
				if(isDebuggingGeneration)
					System.out.println("    *-Is connected to ocean");
				
				//Add all tiles in this plot to oceanTileNetwork so the network can quickly spread
				oceanTileNetwork.addAll(plot.getTiles());
				
				//Also add all touching erosionPlots to oceanTileNetwork
				for(EnvironmentPlot adjacentPlot : adjacentErosionPlots) {
					if(oceanTileNetwork.contains(adjacentPlot.getTiles().get(0)))
						continue;
					potentialInlandLakes.remove(adjacentPlot);
					oceanTileNetwork.addAll(adjacentPlot.getTiles());
				}
			} else {
				if(isDebuggingGeneration)
					System.out.println("    *-Is inland lake");
				potentialInlandLakes.add(plot);
			}
		}
		
		if(isDebuggingGeneration)
			System.out.println("PHASE -> Erosion generation complete.");
		
		
		isPlottingLakes = true;
		
		if(isDebuggingGeneration)
			System.out.println("BEGINNING PHASE -> Converting inland erosion plots into lakes.");
		//This is a redundent call but it does set the WorldTile.isLake flag to true
		potentialInlandLakes.stream().forEach(  x -> x.getTiles().forEach( t -> t.SetPlot(x) )  );
		plotMap.get(EnvironmentType.Water).addAll(potentialInlandLakes);
		erosionPlots.removeAll(potentialInlandLakes);
		if(isDebuggingGeneration)
			System.out.println("PHASE -> Converted "+ potentialInlandLakes.size() +" inland erosion plots into lakes.");
		
		if(isDebuggingGeneration)
			System.out.println("BEGINNING PHASE -> Validating Lakes.");
		//Check all lakes to see if they've been invalidated
		List<EnvironmentPlot> invalidLakes_encompassedByLake = new ArrayList<EnvironmentPlot>();
		List<EnvironmentPlot> invalidLakes_encompassedByOcean = new ArrayList<EnvironmentPlot>();
		Map<String, List<EnvironmentPlot>> invalidLakes_intersectingLakeGroups = new HashMap<String, List<EnvironmentPlot>>();
		for(EnvironmentPlot plot : plotMap.get(EnvironmentType.Water)) {
			String containingGroupID = null;
			//String[] ids = invalidLakes_intersectingLakeGroups.keySet().stream().toArray(String[]::new);
			List<String> ids = new LinkedList<String>( Arrays.asList(invalidLakes_intersectingLakeGroups.keySet().stream().toArray(String[]::new)) );
			for(String id : ids) {
				if(invalidLakes_intersectingLakeGroups.get(id).contains(plot)) {
					containingGroupID = id;
					break;
				}
			}
			
			if(isDebuggingGeneration) {
				if(containingGroupID != null) {
					System.out.println("Validating Lake: " + plot.getEpicenter() + "      -This Lake has already been added to an intersectingLakeGroup. &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
				} else {
					System.out.println("Validating Lake: " + plot.getEpicenter());
				}
			}
			
			List<Point2D> neighborPoints = GetNeighborPoints(plot.getTiles(), width, height);
			plot.getTiles().forEach(x -> x.Set(false));
			neighborPoints.stream().forEach(x -> worldMap.get(x).Set(false));
			List<WorldTile> waterNeighborTiles = new ArrayList<WorldTile>();
			boolean areAllNeighborsFromSamePlot = true;
			for(Point2D point : neighborPoints) {
				WorldTile tile = worldMap.get(point);
				
				if(isDebuggingGeneration)
					System.out.println("    -Inspecting neighbor: " + tile.getEnviType() + ", " + tile.getPosition());
				
				if(tile.getEnviType() == EnvironmentType.Water) {
					if(waterNeighborTiles.size() > 0 && tile.GetPlot() != waterNeighborTiles.get(0).GetPlot())
						areAllNeighborsFromSamePlot = false;
					
					waterNeighborTiles.add(tile);
				}
				
			}
			
			//if every neighbor is water then we know this lake has been completely encompassed by ocean or by another lake
			//I think we want to get only plots that are completely surrounded by one other lake or ocean plot cause if this lake is a segway from a lake to an erosion plot then we need to go the conditional below
			if(areAllNeighborsFromSamePlot && waterNeighborTiles.size() == neighborPoints.size()) {
				
				Optional<WorldTile> otherLakeTileOpt = waterNeighborTiles.stream().filter(x -> plotMap.get(EnvironmentType.Water).contains(x.GetPlot())).findFirst();
				if(otherLakeTileOpt.isPresent()) {
					//This needs to be merged into the encompasser immediately or it'll show up as ocean on the encompasser's iteration (if it occurs after this current iteration)
					if(isDebuggingGeneration)
						System.out.println("-> Lake is encompassed by another Lake: " + plot.getEpicenter() + ". Merging on the fly.");
					invalidLakes_encompassedByLake.add(plot);
					
					otherLakeTileOpt.get().GetPlot().AbsorbPlot(plot);
				} else {
					if(isDebuggingGeneration)
						System.out.println("-> Lake is encompassed by ocean: " + plot.getEpicenter() + ". Noted, continuing.");
					invalidLakes_encompassedByOcean.add(plot);
				}
			}
			//Determine if this lake should be merged into another intersecting lake and/or recognized as a bay
			else if(waterNeighborTiles.size() > 0) {
				//check if any of the neighbors belong to other lakes
				List<EnvironmentPlot> intersectingLakes = new ArrayList<EnvironmentPlot>();
						
				List<WorldTile> unmatchedWaterTiles = new ArrayList<WorldTile>(waterNeighborTiles);
				for(EnvironmentPlot otherPlot : plotMap.get(EnvironmentType.Water)) {
					if(otherPlot == plot)
						continue;
					List<WorldTile> sharedTiles = new ArrayList<WorldTile>( Arrays.asList(otherPlot.getTiles().stream().filter( t -> waterNeighborTiles.contains(t) ).toArray(WorldTile[]::new)) );
					if(sharedTiles.size() > 0) {
						//make a conditional that ignores adding it to the list if the otherPlot is already in an intersecting group
						if(containingGroupID == null || (containingGroupID != null && !invalidLakes_intersectingLakeGroups.get(containingGroupID).contains(otherPlot)) )
							intersectingLakes.add(otherPlot);
						
						unmatchedWaterTiles.removeAll(sharedTiles);	
					}
				}
				
				//This could detect bays early on
				if(unmatchedWaterTiles.size() > 0) {
					if(isDebuggingGeneration)
						System.out.println("There are non-plotMap neighbors for lake: " + plot.getEpicenter() + ". They're most likely ocean tiles or erosion plot tiles.");
				}
				
				List<EnvironmentPlot> sortedIntersectingLakes = null;
				if(intersectingLakes.size() > 0) {
					if(containingGroupID != null) {
						//We need to check all intersectingLakes and see if we need to absorb that group into this one
						for(EnvironmentPlot intersectingPlot : intersectingLakes) {
							String groupID = null;
							for(String id : ids) {
								if(invalidLakes_intersectingLakeGroups.get(id).contains(intersectingPlot)) {
									groupID = id;
									break;
								}
							}
							
							if(groupID != null) {
								invalidLakes_intersectingLakeGroups.get(containingGroupID).addAll( invalidLakes_intersectingLakeGroups.get(groupID) );
								invalidLakes_intersectingLakeGroups.remove(groupID);
								ids.remove(groupID);
								if(isDebuggingGeneration)
									System.out.println("Absorbing another intersection group.");
							} else {
								invalidLakes_intersectingLakeGroups.get(containingGroupID).add(intersectingPlot);
							}
						}
						
						if(isDebuggingGeneration) {
							if(invalidLakes_intersectingLakeGroups.get(containingGroupID) != null)
								System.out.println("Adding more lake plots into an existing intersectingLakeGroup, adding " +
									   intersectingLakes.size() + " more to " + invalidLakes_intersectingLakeGroups.get(containingGroupID).get(0).getEpicenter());
							else
								System.out.println("Intersecting Groups were absorbed.");
						}
					} else {
						
						//if any of the intersecting lakes have intersecting lake groups then add this lake and any other intersecting lakes to that group
						Map<EnvironmentPlot, String> lakeGroupMap = new HashMap<EnvironmentPlot, String>();
						for(EnvironmentPlot intersectingPlot : intersectingLakes) {
							String groupID = null;
							for(String id : ids) {
								if(invalidLakes_intersectingLakeGroups.get(id).contains(intersectingPlot)) {
									groupID = id;
									break;
								}
							}
							if(groupID != null) {
								if(isDebuggingGeneration)
									System.out.println("Found that: "+ intersectingPlot.getEpicenter() +" is in another intersection group.");
								lakeGroupMap.put(intersectingPlot, groupID);
							}
						}
						
						if(lakeGroupMap.size() > 0) {
							String masterID = lakeGroupMap.values().stream().toArray(String[]::new)[0];
							if(isDebuggingGeneration)
								System.out.println("Adding all intersecting lakes and groups into the group with epicenter: " + invalidLakes_intersectingLakeGroups.get(masterID).get(0).getEpicenter());
							//Add this and any other intersecting lakes and/or lake groups into one of the lake groups
							for(EnvironmentPlot intersectingPlot : intersectingLakes) {
								//ignore all plots in the master group
								if(invalidLakes_intersectingLakeGroups.get(masterID).contains(intersectingPlot))
									continue;
								
								if(lakeGroupMap.get(intersectingPlot) != null) {
									if(isDebuggingGeneration)
										System.out.println("Absorbing "+ invalidLakes_intersectingLakeGroups.get(lakeGroupMap.get(intersectingPlot)).get(0).getEpicenter() +" into master group.");
									invalidLakes_intersectingLakeGroups.get(masterID).addAll( invalidLakes_intersectingLakeGroups.get(lakeGroupMap.get(intersectingPlot)) );
									invalidLakes_intersectingLakeGroups.remove(lakeGroupMap.get(intersectingPlot));
									ids.remove(lakeGroupMap.get(intersectingPlot));
								} else {
									//just add the single lake itself
									invalidLakes_intersectingLakeGroups.get(masterID).add(intersectingPlot);
								}
							}
							//add the current plot as well
							invalidLakes_intersectingLakeGroups.get(masterID).add(plot);
						} else { //create the lake group and make the biggest lake plot the "master"
							if(isDebuggingGeneration)
								System.out.println("Creating new group for intersecting lakes.");
							//add our currently iterated plot because it was intentially excluded from the intersection loop test to avoid matching with itself
							intersectingLakes.add(plot);
							
							sortedIntersectingLakes = new ArrayList<EnvironmentPlot>();
							
							if(isDebuggingGeneration)
								System.out.println("  -Found a group of: " + intersectingLakes.size() + " intersecting lakes.");
							
							//figure out which one has the greatest range
							int largestIndex = 0;
							int largestRange = 0;
							for(int i = 0; i < intersectingLakes.size(); i++) {
								if(intersectingLakes.get(i).getRange() > largestRange) {
									largestIndex = i;
									largestRange = intersectingLakes.get(i).getRange();
								}
								if(isDebuggingGeneration)
									System.out.println("    -intersectingLake: " + intersectingLakes.get(i).getEpicenter());
							}
							//add them to a invalidLakes_intersectingLakeGroups with the biggest lake set as the first index
							sortedIntersectingLakes.add(intersectingLakes.get(largestIndex));
							if(intersectingLakes.size() > 1) {
								for(int i = 1; i < intersectingLakes.size(); i++) {
									if(i != largestIndex)
										sortedIntersectingLakes.add(intersectingLakes.get(i));
								}
							}
							invalidLakes_intersectingLakeGroups.put(UUID.randomUUID().toString(), sortedIntersectingLakes);
						}
					}
				}
				
				//or more accurately: soon-to-be-merged lake
				if(isDebuggingGeneration) {
					if(intersectingLakes.size() > 0) {
						if(sortedIntersectingLakes != null)
							System.out.println("-> Merged lake is valid at: " + sortedIntersectingLakes.get(0).getEpicenter());
						else if(containingGroupID != null)
							System.out.println("-> Merged lake and all other grouped lakes into lakeGroup with epicenter: " + invalidLakes_intersectingLakeGroups.get(containingGroupID).get(0).getEpicenter());
						else
							System.out.println("-> Merged lake and all other lakes and/or lake groups into a master lakeGroup.");
					} else {
						System.out.println("-> Lake is valid. Only touching ocean or another lake that's already been grouped with this one.");
					}
				}
			} else {
				if(isDebuggingGeneration)
					System.out.println("-> Lake is valid.");
			}
		}
		
		
		isPlottingLakes = false;
		
		//Remove the plots for lakes that were already merged into their encompassers'
		plotMap.get(EnvironmentType.Water).removeAll(invalidLakes_encompassedByLake);
		if(isDebuggingGeneration)
			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ Removed " + invalidLakes_encompassedByLake.size() + " lakes that were encompassed by other Lakes.");
		
		//remove all encompassed lakes, they're pointless. but first repurpose all their tiles
		for(EnvironmentPlot invalidLake : invalidLakes_encompassedByOcean) {
			List<Point> points = new ArrayList<Point>();
			for(WorldTile tile : invalidLake.getTiles())
				points.add(new Point(Math.round((float)tile.getPosition().getX()), (int)tile.getPosition().getY()));
			for(Point point : points)
				ManuallyPlotTile(EnvironmentType.Water, WorldTileType.water, point.x, point.y, height);
		}
		plotMap.get(EnvironmentType.Water).removeAll(invalidLakes_encompassedByOcean);
		if(isDebuggingGeneration)
			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ Removed " + invalidLakes_encompassedByOcean.size() + " lakes that were encompassed by Ocean.");
		
		isPlottingLakes = true;
		
		//Merge lake plots
		//the first index of the array elements are always the largest lake in the group and thus should be the master the others are merged into
		for(List<EnvironmentPlot> lakeGroup : invalidLakes_intersectingLakeGroups.values()) {
			for(int i = 1; i < lakeGroup.size(); i++) {
				lakeGroup.get(0).AbsorbPlot(lakeGroup.get(i));
				plotMap.get(EnvironmentType.Water).remove(lakeGroup.get(i));
			}
		}
		
		isPlottingLakes = false;
		
		isPlottingLakes = true;
		
		//Separate lakes that have been split into chunks via plotting overlap and erosion beaches
		if(isDebuggingGeneration)
			System.out.println("Processing lakes for segmentation.");
		List<EnvironmentPlot> newSegmentedLakes = new ArrayList<EnvironmentPlot>();
		for(EnvironmentPlot lake : plotMap.get(EnvironmentType.Water)) {
			if(isDebuggingGeneration)
				System.out.println("  Checking Lake: " + lake.getEpicenter() + ", tiles: " + lake.getTiles().size());
			List<List<WorldTile>> lakeClusters = new ArrayList<List<WorldTile>>();
			List<WorldTile> uncheckedTiles = new ArrayList<WorldTile>(lake.getTiles());
			while(uncheckedTiles.size() > 0) {
				WorldTile startTile = uncheckedTiles.get(0);
				if(isDebuggingGeneration)
					System.out.println("    Starting chunk detection with tile: " + startTile.getPosition());
				//get cluster
				List<WorldTile> cluster = new ArrayList<WorldTile>();
				cluster.add(startTile);
				List<WorldTile> matchingNeighbors = new ArrayList<WorldTile>();
				do {
					List<Point2D> neighborPoints = GetNeighborPoints(cluster, width, height);
					
					matchingNeighbors.clear();
					for(Point2D point : neighborPoints) {
						WorldTile tile = worldMap.get(point); 
						if(tile.GetPlot() == startTile.GetPlot()) //might also need to check whether this is null(if its already been turned to ocean)
							matchingNeighbors.add(tile);
						else
							tile.Set(false); //set this false casue we arent going to use it and its wasteful to store it just so we can set it to false later
					}
					
					if(matchingNeighbors.size() > 0) {
						if(isDebuggingGeneration)
							System.out.println("    -Found lake neighbors: " + matchingNeighbors.size());
						cluster.addAll(matchingNeighbors);
					}
				} while(matchingNeighbors.size() > 0);
				
				cluster.forEach(x -> x.Set(false));
				
				if(isDebuggingGeneration)
					System.out.println("    Collected lake chunk, tiles: " + cluster.size());
				
				//remove tiles in order to exaust the search
				int previousSize = uncheckedTiles.size();
				uncheckedTiles.removeAll(cluster);
				
				if(isDebuggingGeneration)
					System.out.println("    uncheckedTiles shrank from: " + previousSize + " to " + uncheckedTiles.size());
				
				lakeClusters.add(cluster);
			}
			
			//Split the clusters into their own lake plots
			if(lakeClusters.size() > 1) {
				if(isDebuggingGeneration)
					System.out.println("  Lake is fractured and will be segmented into: " + lakeClusters.size() + " separate lakes");
				//find the chunk with the epicenter and then exclude that from a loop that creates a environment plot for each chunk
				for(List<WorldTile> cluster : lakeClusters) {
					if(cluster.stream().anyMatch(x -> x.IsEpicenter()))
						continue;
					
					EnvironmentPlot newPlot = new EnvironmentPlot(EnvironmentType.Water, cluster.get(0).getPosition(), -1);
					for(WorldTile tile : cluster)
						newPlot.AddWorldTile(tile);
					cluster.get(0).SetAsEpicenter();
					newSegmentedLakes.add(newPlot);
				}
			} else {
				if(isDebuggingGeneration)
					System.out.println("  Lake is whole.");
			}
		}
		plotMap.get(EnvironmentType.Water).addAll(newSegmentedLakes);
		if(isDebuggingGeneration)
			System.out.println("Segmenting Lake Chunks Complete, added new lakes: " + newSegmentedLakes.size());
		
		isPlottingLakes = false;

		//Move bay detection below segmentation now that we've got our lakes merging correctly
		isPlottingBay = true;
		
		bayPlots = new ArrayList<EnvironmentPlot>();
		for(EnvironmentPlot lakePlot : plotMap.get(EnvironmentType.Water)) {
			List<Point2D> neighborPoints = GetNeighborPoints(lakePlot.getTiles(), width, height);
			lakePlot.getTiles().forEach(x -> x.Set(false));
			neighborPoints.stream().forEach(x -> worldMap.get(x).Set(false));
			boolean hasOceanNeighbor = false;
			if(isDebuggingGeneration)
				System.out.println("Checking if Lake has become a Bay: " + lakePlot.getEpicenter());
			
			for(Point2D point : neighborPoints) {
				WorldTile tile = worldMap.get(point);
				
				if(isDebuggingGeneration)
					System.out.println("  -Inspecting neighbor: " + tile.getEnviType() + ", " + tile.getPosition());
				
				//The erosion water areas DO have their own plots(in order to retain generation information for use in later Stages) but they aren't added to plotMap
				if(tile.getEnviType() == EnvironmentType.Water && !plotMap.get(EnvironmentType.Water).contains(tile.GetPlot())) {
					hasOceanNeighbor = true;
					if(isDebuggingGeneration)
						System.out.println("  *-Lake has an ocean neighbor");
					break;
				}
			}
			if(hasOceanNeighbor) {
				bayPlots.add(lakePlot);
				if(isDebuggingGeneration)
					System.out.println("-> Repurposing lake as bay at: " + bayPlots.get(bayPlots.size()-1).getEpicenter());
			}
		}
		//Repurpose bays, this should be simply as removing them from the plotMap after any intersecting lakes have already been merged
		plotMap.get(EnvironmentType.Water).removeAll(bayPlots);
		if(isDebuggingGeneration)
			System.out.println("Removed " + bayPlots.size() + " bays from plotMap lake list.");
		//Update all the WorldTile's parents to their new bay plot parent
		for(EnvironmentPlot bayPlot : bayPlots) {
			bayPlot.getTiles().forEach(x -> x.SetPlot(bayPlot));
			//bayPlot.getTiles().forEach(x -> x.ResetTint()); //Just to make sure that theres not an error with the conversion of lake tiles to bay tiles
		}
		
		isPlottingBay = false;
		
		
		//Remove single tile bays that aren't touching land
		if(isDebuggingGeneration)
			System.out.println("Removing single tile bays that aren't touching land.");
		EnvironmentPlot[] singleTileBays = bayPlots.stream().filter(x -> x.getTiles().size() == 1).toArray(EnvironmentPlot[]::new);
		for(EnvironmentPlot bay : singleTileBays) {
			if(isDebuggingGeneration)
				System.out.println("  Testing single-tile bay: " + bay.getEpicenter());
			if(IsIsland(bay.getTiles(), width, height)) {
				bayPlots.remove(bay);
				ManuallyPlotTile(EnvironmentType.Water, WorldTileType.water, (int)Math.round((float)bay.getEpicenter().getX()), (int)bay.getEpicenter().getY(), height);
				if(isDebuggingGeneration)
					System.out.println("    -Removed orphan bay at: " + bay.getEpicenter());
			} else {
				if(isDebuggingGeneration)
					System.out.println("    -Leaving valid bay at: " + bay.getEpicenter());
			}
		}
		if(isDebuggingGeneration)
			System.out.println("Done removing bays.");
		
		
		//Grow islands
		//Decide the average island size by comparing ocean and bay concentration vs land and lake concentration
		SeparatePlotsByIsland(width, height);
		
		int totalTileCount = width * height;
		int landAndLakeCount = 0;
		for(Island island : islands) {
			for(EnvironmentPlot plot : island.islandPlots)
				landAndLakeCount += plot.getTiles().size();
		}	
		int oceanAndBayCount = totalTileCount - landAndLakeCount;
		
		float oceanAndBayPercentage = (float)oceanAndBayCount/totalTileCount;
		float oceanFillPercentage = 0.3f; //this number will decide the percentage of ocean/bay space to be filled by island expansion
		int newIslandTileCount = Math.round(totalTileCount * (oceanAndBayPercentage * oceanFillPercentage));
		
		int smallEnoughIslands = islands.stream().filter(x -> x.GetTileCount() <= smallIsland_tileCountThreshold).toArray(Island[]::new).length;
		int averageIslandTileAdditions = Math.round((float)newIslandTileCount / smallEnoughIslands);
		if(isDebuggingGeneration)
			System.out.println("landAndLakeCount: " + landAndLakeCount + ", oceanAndBayCount: " + oceanAndBayCount + ", averageIslandTileAdditions: " + averageIslandTileAdditions);
		//use this number to expand islands into ocean and bay tiles semi-randomly
		//dont forget to merge adjacent islands on every growth cycle
		if(isDebuggingGeneration)
			System.out.println("Growing Islands.");
		List<Island> tempIslandsList = new ArrayList<Island>( islands );
		for(Island island : tempIslandsList) {
			//Ignore bigger islands and skip islands that have been merged already
			if(island.GetTileCount() > smallIsland_tileCountThreshold || !islands.contains(island))
				continue;
			
			//int additionGoal = Math.max( 1, averageIslandTileAdditions + (r.nextInt((int)Math.round(averageIslandTileAdditions*0.5f)) * (r.nextBoolean() ? 1 : -1)) );
			//Having plus 50% make them too big, so lets only decrease the goal instead
			int additionGoal = Math.max( 1, averageIslandTileAdditions - r.nextInt((int)Math.round(averageIslandTileAdditions*0.5f)));
			int currentTileAdditions = 0;
			boolean hasMergedWithAnotherIsland = false;
			
			//Setup directional preferences
			Map<HexDirection, Float> directionChanceMap = new HashMap<HexDirection, Float>();
			float halfWidth = width / 2f;
			float halfHeight = height / 2f;
			float xPref = Math.abs((float)island.islandPlots.get(0).getEpicenter().getY() - halfHeight) / halfHeight;
			float yPref = Math.abs(Math.round((float)island.islandPlots.get(0).getEpicenter().getX()) - halfWidth) / halfWidth;
			directionChanceMap.put(HexDirection.E, xPref);
			directionChanceMap.put(HexDirection.NE, xPref + yPref / 2);
			directionChanceMap.put(HexDirection.NW, xPref + yPref / 2);
			directionChanceMap.put(HexDirection.W, xPref);
			directionChanceMap.put(HexDirection.SW, xPref + yPref / 2);
			directionChanceMap.put(HexDirection.SE, xPref + yPref / 2);
			/*float xChance = 0.8f;
			float yChance = 0.8f;
			if(xPref > yPref)
				yChance = 0.2f;
			else if(xPref < yPref)
				xChance = 0.2f;
			directionChanceMap.put(HexDirection.E, xChance);
			directionChanceMap.put(HexDirection.NE, yChance);
			directionChanceMap.put(HexDirection.NW, yChance);
			directionChanceMap.put(HexDirection.W, xChance);
			directionChanceMap.put(HexDirection.SW, yChance);
			directionChanceMap.put(HexDirection.SE, yChance);*/
			//this didn't really turn out that well, i like the result of the Prefs better
			
			for(EnvironmentPlot plot : island.islandPlots) {
				if(isDebuggingGeneration)
					System.out.println("Growing plot: " + plot.getEnviType() + ", " + plot.getEpicenter());
				//Keep growing this plot until we're at the goal size or we've hit another island
				do {
					List<Point2D> neighborPoints = GetNeighborPoints(plot.getTiles(), width, height);
					//plot.getTiles().forEach(x -> x.Set(false));
					//lets not reset these for the sake of effeciency
					neighborPoints.stream().forEach(x -> worldMap.get(x).Set(false));
	
					for(Point2D point : neighborPoints) {
						//Skip if this tile is the last or first row so that theres always ocean an island and the edge of the world
						int xInt = Math.round((float)point.getX());
						int yInt = Math.round((float)point.getY());
						if(xInt == 0 || yInt == 0 || xInt == width-1 || yInt == height-1)
							continue;
						
						//Skip point if our chance failed
						int xDir = Math.min(1, Math.max(-1, xInt - Math.round((float)plot.getEpicenter().getX())));
						int yDir = Math.min(1, Math.max(-1, yInt - Math.round((float)plot.getEpicenter().getY())));
						if(xDir == 0)
							xDir = r.nextBoolean() ? 1 : -1;
						float directionChance = directionChanceMap.get(rectToHexMatrix.get(new Point(xDir, yDir))).floatValue();
						if(r.nextInt(101)/100f > directionChance)
							continue;
						
						WorldTile tile = worldMap.get(point);
						if(isDebuggingGeneration)
							System.out.println("  -Inspecting neighbor: " + tile.getEnviType() + ", " + tile.getPosition());
						
						if(tile.getEnviType() == EnvironmentType.Water && !plotMap.get(EnvironmentType.Water).contains(tile.GetPlot())) {
							if(tile.GetPlot() != null) {
								tile.GetPlot().RemoveWorldTile(tile);
								//pick another tile to be epicenter if we overwrote it
								if(tile.GetPlot().getTiles().size() > 0) {
									if(tile.IsEpicenter())
										tile.GetPlot().getTiles().get(r.nextInt(tile.GetPlot().getTiles().size())).SetAsEpicenter();
								} else {
									if(isDebuggingGeneration)
										System.out.println("Plot has been completely overtaken by an island.");
									if(erosionPlots.contains(tile.GetPlot())) {
										erosionPlots.remove(tile.GetPlot());
									} else if(bayPlots.contains(tile.GetPlot())) {
										bayPlots.remove(tile.GetPlot());
									} else {
										if(isDebuggingGeneration)
											System.out.println("  -Plot doesn't belong to the erosionPlots or bayPlots! So what does it belong to???");
									}
								}
							}
							WorldTile newTile = new WorldTile(plot.getEnviType(), GetWorldType(plot.getEnviType(), currentTileAdditions/6 + 1, additionGoal/6 + 1), tile.getPosition());
							plot.AddWorldTile(newTile);
							
							currentTileAdditions++;
							
							//Check and possibly merge islands
							//if any of the new tiles neighbors are tiles that aren't from this island then add this island to the other
							List<Point2D> newTileNeighbors = GetNeighborPoints( Arrays.asList(new WorldTile[]{tile}), width, height);
							//tile.Set(false);
							//lets not reset this for the sake of effeciency
							newTileNeighbors.stream().forEach(x -> worldMap.get(x).Set(false));
							for(Point2D neighborPoint : newTileNeighbors) {
								WorldTile neighborTile = worldMap.get(neighborPoint);
								//if we're touching a tile thats either land or lake and isn't on our island
								if(plotMap.get(neighborTile.getEnviType()).contains(neighborTile.GetPlot()) && !island.islandPlots.contains(neighborTile.GetPlot())) {
									if(isDebuggingGeneration)
										System.out.println("    -Island is touching another island, epicenter: " + plot.getEpicenter());
									hasMergedWithAnotherIsland = true;
									//Do merging
									islands.stream().filter(x -> x.islandPlots.contains(neighborTile.GetPlot())).findFirst().get().islandPlots.addAll(island.islandPlots);
									islands.remove(island);
								}
								if(hasMergedWithAnotherIsland)
									break;
							}
						}
						if(currentTileAdditions >= additionGoal || hasMergedWithAnotherIsland)
							break;
					}
				} while(currentTileAdditions < additionGoal && !hasMergedWithAnotherIsland);
				
				plot.getTiles().forEach(x -> x.Set(false));
				
				if(isDebuggingGeneration) {
					if(currentTileAdditions >= additionGoal)
						System.out.println("    -Island plot growth met goal, epicenter: " + plot.getEpicenter());
				}
				
				if(currentTileAdditions >= additionGoal || hasMergedWithAnotherIsland)
					break;
			}
		}
		if(isDebuggingGeneration)
			System.out.println("Island Growth Complete. Island count: " + islands.size());
		
		
		//Grow single tile bays(and maybe two tile bays as well) out into ocean neighbors (dont forget to check for and join bays after every growth ring)
		//If the bay is growing out into an erosion plot epicenter, it may look cool to have the bay absorb the whole erosion plot (if the plot isn't massive and it isn't going off the edge of the screen)
		//A good test for this could be to measure the ratio of land neighbors -to- plot range, ideal bays would have at least half or more land neighbors out of the total possible number of neighbors
		
		
		
		//After removing or merging any invalid lakes check our total remaining count
		int lakeAndBayCount = plotMap.get(EnvironmentType.Water).size() + bayPlots.size();
		if(lakeAndBayCount < procedures.stream().filter(x -> x.getEnviType() == EnvironmentType.Water).findFirst().get().getGenerationConcentrationMin())
			System.err.println("Lake merging and erosion have reduced the lake & bay count(currently: "+ lakeAndBayCount +") lower than the lake procedure minimum!");
		//To meet the minimum required number of lakes and bays, the difference will need to be validly regenerated inside the continent
		
		if(isDebuggingGeneration)
			System.out.println("PHASE -> Validating Lakes complete.");
	}
	
	//The main reason this separate list exists is so the RIVER STAGE can use plotMap.get(EnvironmentType.Water) with confidence that river sources 
	//will only be selected from valid lakes on the interior of the continent
	//Remember to use bays like lakes when associating MapLocations/Missions, since they function in nearly identical ways
	List<EnvironmentPlot> bayPlots;
	
	//This is be all land masses (including their lakes), even the main continent or continents.
	//since we dont know how the generation will turn out we'll treat everything like an island and use thresholds to differentiate continents from smaller islands
	List<Island> islands;
	int smallIsland_tileCountThreshold = 40; //or less
	
	public class Island {
		List<EnvironmentPlot> islandPlots = new ArrayList<EnvironmentPlot>();
		
		public int GetTileCount() {
			int count = 0;
			for(EnvironmentPlot plot : islandPlots) {
				count += plot.getTiles().size();
			}
			return count;
		}
	}
	
	private void SeparatePlotsByIsland(int width, int height) {
		//finding the island clusters isn't behaving properly, try rseting all GetNeighborPoints flags
		worldMap.values().forEach(x -> x.Set(false));
		
		islands = new ArrayList<Island>();

		//Do "blank space fill" type algorithm to get all the touching land tiles groups(islands)
		if(isDebuggingGeneration)
			System.out.println("Processing plotMap for islands.");
		List<List<WorldTile>> islandClusters = new ArrayList<List<WorldTile>>();
		
		//Loadup all land/lake tiles
		List<WorldTile> uncheckedTiles = new ArrayList<WorldTile>();
		for(List<EnvironmentPlot> typedPlots : plotMap.values()) {
			for(EnvironmentPlot plot : typedPlots)
				uncheckedTiles.addAll(plot.getTiles());
		}
		
		while(uncheckedTiles.size() > 0) {
			WorldTile startTile = uncheckedTiles.get(0);
			if(isDebuggingGeneration)
				System.out.println("    Starting chunk detection with tile: " + startTile.getPosition());
			//get cluster
			List<WorldTile> cluster = new ArrayList<WorldTile>();
			cluster.add(startTile);
			List<WorldTile> matchingNeighbors = new ArrayList<WorldTile>();
			do {
				List<Point2D> neighborPoints = GetNeighborPoints(cluster, width, height);
				
				matchingNeighbors.clear();
				for(Point2D point : neighborPoints) {
					WorldTile tile = worldMap.get(point); 
					if(plotMap.get(tile.getEnviType()).contains(tile.GetPlot()))
						matchingNeighbors.add(tile);
					else
						tile.Set(false); //set this false casue we arent going to use it and its wasteful to store it just so we can set it to false later
				}
				
				if(matchingNeighbors.size() > 0) {
					if(isDebuggingGeneration)
						System.out.println("    -Found land/lake neighbors: " + matchingNeighbors.size());
					cluster.addAll(matchingNeighbors);
				}
			} while(matchingNeighbors.size() > 0);
			
			cluster.forEach(x -> x.Set(false));
			
			if(isDebuggingGeneration)
				System.out.println("    Collected island chunk, tiles: " + cluster.size());
			
			//remove tiles in order to exaust the search
			int previousSize = uncheckedTiles.size();
			uncheckedTiles.removeAll(cluster);
			
			if(isDebuggingGeneration)
				System.out.println("    uncheckedTiles shrank from: " + previousSize + " to " + uncheckedTiles.size());
			
			islandClusters.add(cluster);
		}
		if(isDebuggingGeneration)
			System.out.println("Complete with islandCluster collection. Found " + islandClusters.size() + " clusters.");
		
		//sort each group with a Map<EnvironmentPlot, List<WorldTile>>
		if(isDebuggingGeneration)
			System.out.println("Finding dispersed plots in clusters.");
		Map<EnvironmentPlot, List<Integer>> dispersedPlots = new HashMap<EnvironmentPlot, List<Integer>>();
		int clusterIndex = 0;
		for(List<WorldTile> cluster : islandClusters) {
			Map<EnvironmentPlot, List<WorldTile>> plotClusterMap = new HashMap<EnvironmentPlot, List<WorldTile>>();
			for(WorldTile tile : cluster) {
				if(plotClusterMap.get(tile.GetPlot()) == null)
					plotClusterMap.put(tile.GetPlot(), new ArrayList<WorldTile>());
				plotClusterMap.get(tile.GetPlot()).add(tile);
			}
			for(EnvironmentPlot plot : plotClusterMap.keySet()) {
				if(plot.getTiles().size() != plotClusterMap.get(plot).size()) {
					if(dispersedPlots.get(plot) == null)
						dispersedPlots.put(plot, new ArrayList<Integer>());
					dispersedPlots.get(plot).add(clusterIndex);
				}
			}
			clusterIndex++;
		}
		//remove whole plots from map
		EnvironmentPlot[] plotKeys = dispersedPlots.keySet().stream().toArray(EnvironmentPlot[]::new);
		for(EnvironmentPlot plotKey : plotKeys) {
			if(dispersedPlots.get(plotKey).size() == 1) {
				dispersedPlots.remove(plotKey);
			} else {
				if(isDebuggingGeneration)
					System.out.println("  -Found dispersed plot with epicenter: " + plotKey.getEpicenter() + ", its split into: " + dispersedPlots.get(plotKey).size());
			}
		}
		
		//Separate each orphaned set of tiles (those without an epicenter) into their own plots
		if(isDebuggingGeneration)
			System.out.println("Separating dispersed tile clusters into new plots.");
		int newPlotCount = 0;
		for(EnvironmentPlot plotKey : dispersedPlots.keySet()) {
			for(Integer islandClusterInteger : dispersedPlots.get(plotKey)) {
				int index = islandClusterInteger.intValue();
				//Ignore the cluster that already has an epicenter, thats the plot that'll be left intact (asside from the dispersed tiles of course)
				if(islandClusters.get(index).stream().anyMatch(x -> x.GetPlot() == plotKey && x.IsEpicenter()))
					continue;
				
				WorldTile[] plotTilesOnIsland = islandClusters.get(index).stream().filter(x -> x.GetPlot() == plotKey).toArray(WorldTile[]::new);
				EnvironmentPlot newPlot = new EnvironmentPlot(plotKey.getEnviType(), plotTilesOnIsland[0].getPosition(), -1);
				for(WorldTile tile : plotTilesOnIsland) {
					newPlot.AddWorldTile(tile);
					if(newPlot.getTiles().size() == 1)
						tile.SetAsEpicenter();
				}
				plotMap.get(plotKey.getEnviType()).add(newPlot);
				newPlotCount++;
			}
		}
		if(isDebuggingGeneration)
			System.out.println("Created " + newPlotCount + " new plots for dispersed tiles.");
		
		//Finally compile this data into Island objects
		for(List<WorldTile> cluster : islandClusters) {
			Island newIsland = new Island();
			for(WorldTile tile : cluster) {
				if(!newIsland.islandPlots.contains(tile.GetPlot()))
					newIsland.islandPlots.add(tile.GetPlot());
			}
			islands.add(newIsland);
		}
		if(isDebuggingGeneration)
			System.out.println("DivideIslandsAndContinent() is complete, found "+ islands.size() +" islands.");
	}
	
	public class WorldTileInstanceModel {
		public WorldTileInstanceModel(WorldTile tile) {
			instance = tile;
			this.enviType = tile.getEnviType();
			this.tileType = tile.getTileType();
			this.position = tile.getPosition();
			this.isBlank = tile.IsBlank();
			this.isEpicenter = tile.IsEpicenter();
			this.isRiver = tile.IsRiver();
			this.isLake = tile.IsLake();
			this.isBay = tile.IsBay();
			this.isDiscovered = tile.isDiscovered();
			this.plot = tile.GetPlot();
		}
		
		public WorldTile instance;
		
		public EnvironmentType enviType;
		public WorldTileType tileType;
		public Point2D position;
		
		public boolean isBlank;

		public boolean isEpicenter;

		public boolean isRiver;
		public boolean isLake;
		public boolean isBay;
		
		public boolean isDiscovered;
		
		public EnvironmentPlot plot;
	}
	
	public class River {
		EnvironmentPlot sourcePlot;
		List<WorldTile> tiles = new ArrayList<WorldTile>();
		Point direction;
		List<WorldTileInstanceModel> previousTileDatas = new ArrayList<WorldTileInstanceModel>();
		int savedWorldHeight;
		
		public River(EnvironmentPlot sourcePlot, Point2D startTilePosition, Point direction, int worldHeight) {
			this.sourcePlot = sourcePlot;
			this.direction = direction;
			
			AddWorldTile(startTilePosition, worldHeight);
			
			savedWorldHeight = worldHeight;
		}
		
		public void AddWorldTile(Point2D position, int worldHeight) {
			//record the tile previously occupying this space in case we need to undo it later
			previousTileDatas.add(new WorldTileInstanceModel(worldMap.get(position)));
			
			ManuallyPlotTile(EnvironmentType.Water, WorldTileType.water, Math.round((float)position.getX()), (int)position.getY(), worldHeight);
			//Add the WorldTile like this because its been recreated by ManuallyPlotTile()
			tiles.add(worldMap.get(position));
		}
		
		public void UndoRiver() {
			for(WorldTileInstanceModel instanceModel : previousTileDatas) {
				if(instanceModel.plot != null) {
					instanceModel.plot.AddWorldTile(instanceModel.instance);
				} else {
					ManuallyPlotTile(instanceModel.enviType, instanceModel.tileType, Math.round((float)instanceModel.position.getX()), (int)instanceModel.position.getY(), savedWorldHeight);
				}
				WorldTile newlySetTile = worldMap.get(instanceModel.position);
				newlySetTile.isLake = instanceModel.isLake;
				newlySetTile.isBay = instanceModel.isBay;
				newlySetTile.isRiver = instanceModel.isRiver;
			}
		}
	}
	List<River> rivers = new ArrayList<River>();
	
	private boolean isPlottingLakes;
	private boolean isRiversStage;
	private boolean isPlottingBay;
	
	private void CreateRivers(int minRiverSources, int maxRiverSources, int worldWidth, int worldHeight) {
		//GetNeighborPoints is returning fucked up result, better reset everything, incase it wasn't properly reset in previous logic
		worldMap.values().forEach(x -> x.Set(false));
		
		isRiversStage = true;
		
		//pick a random-ranged quantity of mountains and lakes to use as river sources
		int riverSources = minRiverSources + r.nextInt(maxRiverSources - minRiverSources);
		
		//Pick random sources among our 2 environment types: mountains and lakes
		int mountainSourceCount = (int)Math.ceil(riverSources/2.0);
		if(plotMap.get(EnvironmentType.Mountainous).size() < mountainSourceCount) {
			System.err.println("There aren't enough mountain plots to support mountainSourceCount: " + mountainSourceCount + ", reducing mountain sources to: " + plotMap.get(EnvironmentType.Mountainous).size());
			mountainSourceCount = plotMap.get(EnvironmentType.Mountainous).size();
		}
		//instantiate rivers from lake sources
		int lakeSourceCount = riverSources - mountainSourceCount;
		if(plotMap.get(EnvironmentType.Water).size() < lakeSourceCount) {
			System.err.println("There aren't enough lake plots to support lakeSourceCount: " + lakeSourceCount + ", reducing lake sources to: " + plotMap.get(EnvironmentType.Water).size());
			lakeSourceCount = plotMap.get(EnvironmentType.Water).size();
		}
		
		List<Integer> validMountainIndices = new ArrayList<Integer>();
		for(int i = 0; i < plotMap.get(EnvironmentType.Mountainous).size(); i++)
			validMountainIndices.add(new Integer(i));
		
		//for(int m = 0; m < mountainSourceCount + lakeSourceCount; m++) {
		//Because river source choices and path outcomes can be undesirable, we want to keep iterating to create our quota
		boolean hasAbandonedMountainSources = false;
		while(rivers.size() < mountainSourceCount + lakeSourceCount) {
			int mountainCount = rivers.stream().filter(x -> x.sourcePlot.getEnviType() == EnvironmentType.Mountainous).toArray(River[]::new).length;
			
			EnvironmentPlot plot = null;
			//if(m < mountainSourceCount) {
			if(!hasAbandonedMountainSources && mountainCount < mountainSourceCount) {
				if(isDebuggingGeneration)
					System.out.println("Picking valid Mountain source...");
				
				boolean isInvalid = false;
				do {
					int randomIndex = validMountainIndices.get(r.nextInt(validMountainIndices.size()));
					plot = plotMap.get(EnvironmentType.Mountainous).get(randomIndex);
					
					if(isDebuggingGeneration)
						System.out.println("  IsIsland() - plot.epicenter: " + plot.getEpicenter() + ", tileCount: " + plot.getTiles().size());
					//isInvalid = IsIsland(plot.getTiles(), worldWidth, worldHeight);
					isInvalid = IsIsland(plot.getTiles(), worldWidth, worldHeight) && plot.getTiles().size() < 12;
					
					if(isInvalid) {
						validMountainIndices.remove(new Integer(randomIndex));
						if(isDebuggingGeneration)
							System.out.println("  -Mountain is invalid, its an island too small to support a decent river.");
					}
					
				} while(isInvalid && validMountainIndices.size() > 0);
				if(validMountainIndices.size() == 0) {
					System.err.println("There were not enough valid mountains for river placement. Abandoning Mountain sources. Continuing with lake sources only.");
					hasAbandonedMountainSources = true;
					continue;
				}
			} else {
				if(isDebuggingGeneration)
					System.out.println("Picking valid Lake source...");
				int randomIndex = r.nextInt(plotMap.get(EnvironmentType.Water).size());
				plot = plotMap.get(EnvironmentType.Water).get(randomIndex);
			}
			
			WorldTile newTile = null;
			Point2D startTilePosition = null;
			//if(m < mountainSourceCount) {
			if(mountainCount < mountainSourceCount) {
				//For mountains lets pick a value thats closer to the middle/epicenter
				if(isDebuggingGeneration)
					System.out.println("Found Mountain Source: " + plot.getEpicenter() + " - Picking valid start tile...");
				boolean isInvalid = false;
				List<Point2D> neighbors = null;
				List<Integer> validChoiceIndices = null;
				int startTileLoopLimit = 80;
				int startTileCurrentLoop = 0;
				//this is a fallback fi there are no valid mointain tiles to serve as a source
				boolean hasAbandonedInnerChoices = plot.getTiles().size() == 1;
				do {
					int originalRandomIndex = 0;
					if(plot.getTiles().size() > 1 && !hasAbandonedInnerChoices) {
						if(validChoiceIndices == null) {
							validChoiceIndices = new ArrayList<Integer>();
							for(int i = 1; i < plot.getTiles().size(); i++)
								validChoiceIndices.add(new Integer(i));
						}
						
						originalRandomIndex = validChoiceIndices.get( r.nextInt(validChoiceIndices.size()) );
						newTile = plot.getTiles().get(originalRandomIndex);
					} else {
						if(neighbors == null) {
							if(isDebuggingGeneration)
								System.out.println("Mountain is 1 tile. Adjusting mountain river placement procedure to consider surrounding tiles.");
							neighbors = GetNeighborPoints(Arrays.asList(new WorldTile[] {plot.getTiles().get(0)}), worldWidth, worldHeight);
							plot.getTiles().get(0).Set(false);
							neighbors.forEach(x -> worldMap.get(x).Set(false));
							validChoiceIndices = new ArrayList<Integer>();
							for(int i = 0; i < neighbors.size(); i++)
								validChoiceIndices.add(new Integer(i));
						}
						originalRandomIndex = validChoiceIndices.get(r.nextInt(validChoiceIndices.size()));
						newTile = worldMap.get(neighbors.get(originalRandomIndex));
					}
					
					isInvalid = newTile.IsEpicenter() || IsTouchingEnvironment(EnvironmentType.Water, newTile, worldWidth, worldHeight);
					
					if(isInvalid)
						validChoiceIndices.remove(new Integer(originalRandomIndex));
					
					//If we can't find a valid start point among mountain tiles inside the plot then search thru the plot's neighbor tiles
					if(!hasAbandonedInnerChoices && validChoiceIndices.size() == 0) {
						hasAbandonedInnerChoices = true;
					
						//fill the array so that another loop will happen, otherwise the do while loop would end
						validChoiceIndices.clear();
						neighbors = GetNeighborPoints(plot.getTiles(), worldWidth, worldHeight);
						plot.getTiles().forEach(x -> x.Set(false));
						neighbors.forEach(x -> worldMap.get(x).Set(false));
						for(int i = 0; i < neighbors.size(); i++)
							validChoiceIndices.add(new Integer(i));
					}
					
					startTileCurrentLoop++;
				} while(isInvalid && validChoiceIndices.size() > 0 && startTileCurrentLoop < startTileLoopLimit); //We use isSurroundedByWater because we dont want a mountain river to be touching a lake or other river
				if(validChoiceIndices.size() == 0) {
					System.err.println("Couldn't find any valid tiles on this mountain to serve as a source! Continuing to next river.");
					continue;
				}
				
				if(startTileCurrentLoop >= startTileLoopLimit)
					System.err.println("Error in Valid Start Tile Search loop");
				
				startTilePosition = newTile.getPosition();
			} else {
				//if a lake then use plots range to figure out long many tiles were in the last ring and pick a number in the range 0-LastRingCount and then pick the tile at index Size-#
				//this will always ensure that the river is starting at the edge of the lake
				if(isDebuggingGeneration)
					System.out.println("Lake river - searching for tile...");
				List<Point2D> neighborPoints = GetNeighborPoints(plot.getTiles(), worldWidth, worldHeight);
				plot.getTiles().forEach(x -> x.Set(false));
				neighborPoints.forEach(x -> worldMap.get(x).Set(false));
				if(isDebuggingGeneration)
					System.out.println("  -Getting neighbors.count: " + neighborPoints.size());
				int lakeSourceLoopLimit = 80;
				int lakeSourceLoopCount = 0;
				do {
					int randomNeighborIndex = Math.abs(r.nextInt(neighborPoints.size()));
					newTile = worldMap.get(neighborPoints.get(randomNeighborIndex));
					lakeSourceLoopCount++;
				} while( (newTile.IsEpicenter() || IsTouchingAnyOceanOrBay(newTile, worldWidth, worldHeight)) && lakeSourceLoopCount < lakeSourceLoopLimit);
				
				if(lakeSourceLoopCount >= lakeSourceLoopLimit) {
					System.err.println("Lake source search for plot: " + plot.getEpicenter() + " went over loop limit. The only conceivable explaination is that this lake is completely surrounded with epicenters and/or ocean/bay. Continuing.");
					continue;
				}
				
				startTilePosition = newTile.getPosition();
			}
			if(isDebuggingGeneration)
				System.out.println("  *-River start: " + startTilePosition);
			
			//choose east or west and an optional north or south
			//the direction will be its relative direction from the epicenter to the startTile
			int diffX = Math.round((float)startTilePosition.getX()) - Math.round((float)plot.getEpicenter().getX());
			int normX = Math.max(-1, Math.min(diffX, 1));
			if(normX == 0) //make sure there always a east or west influence
				normX = r.nextInt(101)/100f <= 0.5f ? -1 : 1;
			
			int diffY = Math.round((float)startTilePosition.getY()) - Math.round((float)plot.getEpicenter().getY());
			int normY = Math.max(-1, Math.min(diffY, 1));
			
			River river = new River(plot, startTilePosition, new Point(normX, normY), worldHeight);
			rivers.add(river);
			
			if(isDebuggingGeneration)
				System.out.println("Building river: " + river.tiles.get(0).getPosition() + ", direction: " + river.direction);
			
			WorldTile nextTile = null;
			boolean keepBuilding = true;
			int outerLoopLimit = 40;
			int outerCurrentLoop = 0;
			int loopLimit = 100;
			int currentLoop = 0;
			boolean isNextTileInvalid = false;
			Point2D lastPoint = null;
			
			do {
				currentLoop = 0;
				 
				EnvironmentPlot currentOtherMountain = null;
				Point otherMountainDir = null;
				boolean hasExhaustedOptionsAvoidingMountain = false;
				
				boolean hasExhaustedOptionsAvoidingStacking = false;
				
				List<Point> sixDirections = Arrays.asList( new Point[] {new Point(1,0), new Point(1,-1), new Point(-1,-1), new Point(-1,0), new Point(-1,1), new Point(1,1)} );
				List<Point> diminishingChoices = new ArrayList<Point>(sixDirections);
				//Ignore the direction of our last tile cause we can't double back on ourselves (if this isn't the first tile extending from the river's startTile)
				Point directionOfLastRiverTile = null;
				if(nextTile != null) {
					directionOfLastRiverTile = GetRectDirectionFromHexToHex(lastPoint, river.tiles.get(river.tiles.size()-2).getPosition());
					if(isDebuggingGeneration)
						System.out.println("directionOfLastRiverTile: " + directionOfLastRiverTile.toString());
					diminishingChoices.remove(directionOfLastRiverTile);
				}
				
				do {
					if(diminishingChoices.size() == 0) {
						//If we can't find a nextTile while trying to avoid stacking and avoiding other mountains then try to flow down the other mountain in order to avoid stacking
						if(!hasExhaustedOptionsAvoidingMountain) {
							hasExhaustedOptionsAvoidingMountain = true;
							diminishingChoices = new ArrayList<Point>(sixDirections);
							diminishingChoices.remove(directionOfLastRiverTile);
						} else {
							if(!hasExhaustedOptionsAvoidingStacking) {
								hasExhaustedOptionsAvoidingStacking = true;
								diminishingChoices = new ArrayList<Point>(sixDirections);
								diminishingChoices.remove(directionOfLastRiverTile);
							} else {
								System.err.println("Exhausted all three sets of options(Avoid Mountains & Stacking & Epicenter/Self, Avoid Stacking & Epicenter/Self, Avoid Epicenter/Self). Breaking. River start tile: " + river.tiles.get(0).getPosition());
								currentLoop = loopLimit;
								break;
							}
						}
					}
					
					int targetDirX = river.direction.x;
					int targetDirY = river.direction.y;
					if(hasExhaustedOptionsAvoidingMountain && otherMountainDir != null) {
						targetDirX = otherMountainDir.x;
						targetDirY = otherMountainDir.y;
					}
					
					Point nextPoint = null;
					//Find ideal target direction, two second best options and two third best options
					Point idealPoint = new Point(targetDirX, targetDirY);
					int idealIndex = sixDirections.indexOf(idealPoint);
					if(idealIndex == -1)
						System.err.println("sixDirections doesnt contain ideal point: " + idealPoint.toString());
					if(isDebuggingGeneration)
						System.out.println("idealIndex: " + idealIndex);
					if(!diminishingChoices.contains(idealPoint))
						idealPoint = null;
					Point[] idealPointArray = new Point[] { idealPoint, null };
					
					int CCWIndex = idealIndex + 1;
					if(CCWIndex > 5)
						CCWIndex = 0;
					Point CCWPoint = sixDirections.get(CCWIndex);
					if(!diminishingChoices.contains(CCWPoint))
						CCWPoint = null;
					
					int CWIndex = idealIndex - 1;
					if(CWIndex < 0)
						CWIndex = 5;
					Point CWPoint = sixDirections.get(CWIndex);
					if(!diminishingChoices.contains(CWPoint))
						CWPoint = null;
					
					Point[] secondBestPoints = new Point[] { CCWPoint, CWPoint };
					
					int CCWx2Index = idealIndex + 2;
					if(CCWx2Index > 5)
						CCWx2Index = CCWx2Index - 6;
					Point CCWx2Point = sixDirections.get(CCWx2Index);
					if(!diminishingChoices.contains(CCWx2Point))
						CCWx2Point = null;
					
					int CWx2Index = idealIndex - 2;
					if(CWx2Index < 0)
						CWx2Index = 6 + CWx2Index;
					Point CWx2Point = sixDirections.get(CWx2Index);
					if(!diminishingChoices.contains(CWx2Point))
						CWx2Point = null;
					
					Point[] thirdBestPoints = new Point[] { CCWx2Point, CWx2Point };
					
					//Pick an option based on randomization from the remaining tiles
					//decide from the possible group count 3(if theres ideal, second best and third best), 2(if theres only two groups available), 1(if theres only one remaining group to choose)
					//then choose fifty-fifty if there are two elements in the group
					//if there are no groups left then break cause that means the river can only flow backwards
					List<Point[]> tieredChoices = new ArrayList<Point[]>();
					if(idealPoint != null)
						tieredChoices.add(idealPointArray);
					if(CCWPoint != null || CWPoint != null)
						tieredChoices.add(secondBestPoints);
					if(CCWx2Point != null || CWx2Point != null)
						tieredChoices.add(thirdBestPoints);
					
					if(tieredChoices.size() == 0) {
						//System.err.println("River has no option but to run backwards onto itself, breaking. River start tile: " + river.tiles.get(0).getPosition());
						//System.err.println("tieredChoices.size() == 0, diminishingChoices.size(): " + diminishingChoices.size());
						//currentLoop = loopLimit;
						//break;
						nextPoint = diminishingChoices.get(0);
					} else {
						float randomValue = xDirRandom.nextInt(101)/100f;
						int tierIndex = 0;
						if(tieredChoices.size() == 3) {
							if(randomValue > 0.35f && randomValue <= 0.7f)
								tierIndex = 1;
							else if(randomValue > 0.7f)
								tierIndex = 2;
						} else if(tieredChoices.size() == 2) {
							if(randomValue > 0.55f)
								tierIndex = 1;
						}
						
						if(tieredChoices.get(tierIndex)[0] != null && tieredChoices.get(tierIndex)[1] != null)
							nextPoint = yDirRandom.nextBoolean() ? tieredChoices.get(tierIndex)[0] : tieredChoices.get(tierIndex)[1];
						else
							nextPoint = tieredChoices.get(tierIndex)[0] != null ? tieredChoices.get(tierIndex)[0] : tieredChoices.get(tierIndex)[1];
					}
					
					diminishingChoices.remove(nextPoint);
					
					//On first iteration
					if(nextTile == null)
						lastPoint = river.tiles.get(0).getPosition();
					Point2D hexPoint = GetHexPointByRectDirection(lastPoint, nextPoint.x, nextPoint.y);
					nextTile = worldMap.get(hexPoint);
					
					EnvironmentPlot currentPlot = plot; //for one of the tests
					
					isNextTileInvalid =
							nextTile.IsEpicenter()
							||
							river.tiles.contains(nextTile)
							||
							(   //Avoid choices that are adjacent to our source lake
								plot.enviType == EnvironmentType.Water
								&&
								GetTouchingEnvironmentTiles(EnvironmentType.Water, nextTile, worldWidth, worldHeight).stream().anyMatch(x -> x.GetPlot() == currentPlot)
							)
							||
							(
								!hasExhaustedOptionsAvoidingStacking
								&&
								IsRiverStackingOnSelf(nextTile, river, worldWidth, worldHeight)
							)
							||
							(
								!hasExhaustedOptionsAvoidingMountain
								&&
								IsTouchingEnvironment(EnvironmentType.Mountainous, nextTile, worldWidth, worldHeight)
								&&
								(
									plot.getEnviType() != EnvironmentType.Mountainous
									||
									(
										plot.getEnviType() == EnvironmentType.Mountainous
										&&
										!plot.getTiles().contains(nextTile)
									)
								)
							);
					
					//save this for the next iteration
					EnvironmentPlot otherPlot = nextTile.GetPlot();
					if(otherPlot != null && otherPlot.getEnviType() == EnvironmentType.Mountainous && (currentOtherMountain == null || currentOtherMountain != otherPlot)) {
						currentOtherMountain = otherPlot;
						//calculate runoff direction for other mountain 
						otherMountainDir = GetRectDirectionFromHexToHex(currentOtherMountain.getEpicenter(), nextTile.getPosition());
					}
					
					if(!isNextTileInvalid) {
						lastPoint = nextTile.getPosition();
						if(isDebuggingGeneration)
							System.out.println("  -Found valid tile: " + nextTile.getPosition());
					} else {
						if(isDebuggingGeneration)
							System.out.println("  -INVALID: " + nextTile.getPosition());
					}
					
					currentLoop++;
				} while(isNextTileInvalid && currentLoop < loopLimit);
				
				if(currentLoop < loopLimit) {
					river.AddWorldTile(nextTile.getPosition(), worldHeight);
					if(isDebuggingGeneration)
						System.out.println("    -next river tile: " + nextTile.getPosition());
					//once we've reached water we're done
					keepBuilding = !IsTouchingAnyWaterExceptRiver(nextTile, river, worldWidth, worldHeight);
					outerCurrentLoop++;
				}
			} while(keepBuilding && currentLoop < loopLimit && outerCurrentLoop < outerLoopLimit);
			
			if(river.tiles.size() < 5) {
				if(isDebuggingGeneration)
					System.out.println("Redo River, its too small.");
				river.UndoRiver();
				rivers.remove(river);
				continue;
			}
			
			if(currentLoop >= loopLimit) {
				System.err.println("Error in next random tile loop!");
			} else if(outerCurrentLoop >= outerLoopLimit) {
				System.err.println("Error in outer loop!");
			} else {
				if(isDebuggingGeneration)
					System.out.println("Completed river, length: " + river.tiles.size());
			}
		}
		
		isRiversStage = false;
		
		if(isDebuggingGeneration)
			System.out.println("- Stage RIVERS COMPLETE - Created " + rivers.size() + " rivers.");
	}
	
	Random xDirRandom = new Random();
	Random yDirRandom = new Random();
	
	public class ChainGuide {
		public List<ClusterLink> links = new ArrayList<ClusterLink>();
		
		//public List<EnvironmentType> typeNodes = new ArrayList<EnvironmentType>();
		//[GenericSettlementSupport]
		public List<VersitileTypeNode> versitileTypeNodes = new ArrayList<VersitileTypeNode>();
		
	}
	
	//[GenericSettlementSupport]
	/**
	 * This is a way of using either an Environment or a Settlement to build mission chains. It takes a secondary argument setting a requirement for the size of the chosen EnvironmentPlot.
	 * @author Magnus
	 *
	 */
	public class VersitileTypeNode {
		public VersitileTypeNode(EnvironmentType environmentType, int minPlotRadius) {
			this.environmentType = environmentType;
			this.minPlotRadius = minPlotRadius;
		}
		public VersitileTypeNode(SettlementType settlementType) {
			this.settlementType = settlementType;
		}
		public EnvironmentType environmentType;
		public SettlementType settlementType;
		public int minPlotRadius;
		
		@Override
		public String toString() {
			return environmentType != null ? environmentType.toString() : settlementType.toString();
		}
	}
	
	public class PathOptionsAnalysis {
		//These are provided from master lists that have already traversed the Cluster Chain
		public PathOptionsAnalysis(ChainGuide chainGuide) {
			this.chainGuide = chainGuide;
		}
		
		public ChainGuide chainGuide;
		
		//use this to record the gathered paths for later comparison
		public class Path {
			//Should include the central point at the first index
			public List<WorldTile> tileNodes = new ArrayList<WorldTile>();
			public float pathDistance;
		}
		public List<Path> paths = new ArrayList<Path>();
		
		public Map<WorldTile, Path> centralTilesShortestPath = new HashMap<WorldTile, Path>();
		public void FindShortestPathForEachCentralTile() {
			WorldTile centralTile = paths.get(0).tileNodes.get(0);
			if(debugLocationPlacement)
				System.out.println("PathOptionsAnalysis.FindShortestPathForEachCentralTile() - for centralTile at: " + centralTile.getPosition());
			float shortestPathDistance = -1f;
			Path shorestPath = null;
			for(Path path : paths) {
				if(debugLocationPlacement)
					System.out.println("  Reviewing Path for centralTile: "+ centralTile.getPosition() + " to: " + path.tileNodes.get(0).getPosition() + ", with distance: " + path.pathDistance);
				
				if(path.tileNodes.get(0) != centralTile) {
					
					//[GenericSettlementSupport] - This is just here as a failsafe, though so far it hasn't been the problem
					if(shorestPath == null)
						System.err.println("  ShortestPath is null?! Why would we ever set a null path?");
					
					//record the shortest path for the paths of the last central tile
					centralTilesShortestPath.put(centralTile, shorestPath);
					if(debugLocationPlacement)
						System.out.println("    Shortest Path for centralTile at: " + centralTile.getPosition() + " to: " + path.tileNodes.get(0).getPosition() + ", PathDistance: " + shortestPathDistance);
					
					centralTile = path.tileNodes.get(0);
					shortestPathDistance = -1f;
					shorestPath = null;
				}
				//compare the currently iterated path distance with the shortest path distance
				if(shortestPathDistance == -1f || path.pathDistance < shortestPathDistance) {
					shortestPathDistance = path.pathDistance;
					shorestPath = path;
				}
			}
		}
	}
	
	private boolean debugLocationPlacement = true;
	
	private void PlaceLocations() {
		System.out.println("WorldmapPanel.PlaceLocations()");
		
		// <- MapLocation Population -
		//Create all the MapLocations before doing the missions because the mission expect the tile's MapLocations to be set already 
		
		//Add more insignificant generic mapLocations like merchants, inns, tea houses, castles, etc
		/*for(MapLocation mapLocation : Missions.GetAllStaticMapLocations()) {
			//Get a random plot from the corresponding plotMap list that isn't already occupied by a mission or maplocation
			EnvironmentType enviType = mapLocation.getEnviType();
			if(enviType == null)
				enviType = sensibleLocationEnvironments[r.nextInt(sensibleLocationEnvironments.length)];
			List<EnvironmentPlot> typedPlots = plotMap.get(enviType);
			WorldTile chosenTile = null;
			do {
				EnvironmentPlot plot = typedPlots.get(r.nextInt(typedPlots.size()));
				for(WorldTile tile : plot.getTiles()) {
					chosenTile = tile;
					if(!chosenTile.HasMissionOrStaticMapLocation())
						break;
				}
			} while(chosenTile.HasMissionOrStaticMapLocation());
			
			chosenTile.SetMapLocation(mapLocation);
		}*/
		
		//Add generic locations randomly around the map to fill it out and make the game survivable
		genericSettlementProcedures.put(SettlementType.Campsite, new Range(6, 8));
		genericSettlementProcedures.put(SettlementType.Teahouse, new Range(6, 8));
		genericSettlementProcedures.put(SettlementType.Market, new Range(6, 8));
		genericSettlementProcedures.put(SettlementType.Blacksmith, new Range(6, 8));
		genericSettlementProcedures.put(SettlementType.Doctor, new Range(6, 8));
		//It doesn't make sense to plot crossroads randomly, they'll have to be specially placed in a way thats helpful for navigation
		//genericSettlementProcedures.put(SettlementType.Crossroads, new Range(6, 8));
		genericSettlementProcedures.put(SettlementType.NotableLocation, new Range(6, 8));
		genericSettlementProcedures.put(SettlementType.QuestBoard, new Range(6, 8));
		genericSettlementProcedures.put(SettlementType.Estate, new Range(6, 8));
		genericSettlementProcedures.put(SettlementType.Village, new Range(6, 8));
		genericSettlementProcedures.put(SettlementType.Garden, new Range(6, 8));
		genericSettlementProcedures.put(SettlementType.Shrine, new Range(6, 8));
		genericSettlementProcedures.put(SettlementType.Graveyard, new Range(6, 8));
		genericSettlementProcedures.put(SettlementType.Castle, new Range(6, 8));
		
		for(MapLocation genericLocation : Missions.GetAllSettlementTemplateCopys()) {
			Range range = genericSettlementProcedures.get(genericLocation.getSettlementType());
			if(range == null) {
				System.err.println("Generic MapLocation: " + genericLocation.getName() + " with settlementType: " + genericLocation.getSettlementType() + " doesn't exist in the "
						+ "genericSettlementProcedures map! Although, this type of settlement may have been intentionally left out of the map. Like, Crossroads for example.");
				continue;
			}
			for(int i = 0; i < range.GetRandomInRange(); i++) {
				//Get a random plot from the corresponding plotMap list that isn't already occupied by a mission or maplocation
				EnvironmentType enviType = genericLocation.getEnviType();
				if(enviType == null)
					enviType = sensibleLocationEnvironments[r.nextInt(sensibleLocationEnvironments.length)];
				List<EnvironmentPlot> typedPlots = plotMap.get(enviType);
				WorldTile chosenTile = null;
				do {
					EnvironmentPlot plot = typedPlots.get(r.nextInt(typedPlots.size()));
					for(WorldTile tile : plot.getTiles()) {
						chosenTile = tile;
						if(chosenTile.GetMapLocation() == null)
							break;
					}
					//Keep searching if we didnt find any existing or unoccupied tiles in this cluster
				} while(chosenTile == null || chosenTile.GetMapLocation() != null);
				
				chosenTile.SetMapLocationFromTemplate(
						Missions.GetMapLocationTemplateCopy(chosenTile.getEnviType(), chosenTile.getTileType(), genericLocation.getSettlementType(), genericLocation.getSettlementDesignation())
				);
			}
		}
		
		//Fill in the rest of the WorldMapTiles with Nature Layer MapLocations
		for(WorldTile worldTile : this.worldMap.values()) {
			if(worldTile.GetMapLocation() == null)
				worldTile.SetMapLocationFromTemplate( Missions.GetMapLocationTemplateCopy(worldTile.getEnviType(), worldTile.getTileType()) );
		}
		
		// - MapLocation Population ->
		
		// <- Mission Population -
		
		//The mission placing process is a somewhat cohesive procedure based on a set of rules
		for(InstructionSet instructionSet : Missions.GetInstructionSets()) {
			//Pick tiles based on instructions and their proximity to eachother
			if(instructionSet.GetInstructionCluster() != null) {
				Mission centralMission = instructionSet.GetInstructionCluster().GetCentralMission();
				
				//Handle cluster matching
				if(debugLocationPlacement)
					System.out.println("Placing InstructionCluster.");
				
				//chains spreading from central tile
				List<ChainGuide> chainGuides = new ArrayList<ChainGuide>();
				for(ClusterLink clusterLink : instructionSet.GetInstructionCluster().GetClusterChains()) {
					if(debugLocationPlacement)
						System.out.println("  Traversing ClusterLink with Central Mission: " + centralMission.getName());
					//Iterate as far as this chain extends and record the chain via a list of ENvironmentTypes
					ChainGuide newGuide = new ChainGuide();
					boolean reachedEndOfChain = false;
					ClusterLink currentLink = clusterLink;
					while(!reachedEndOfChain) {
						newGuide.links.add(currentLink);
						EnvironmentType enviType = GetEnvironmentFromMission(currentLink.GetLayersMission());
						
						/*
						if(enviType == null)
							System.err.println("  Every MapLocation in a Mission in an InstructionCluster must have an EnvironmentType! Mission: " + currentLink.GetLayersMission().getName()
									+ " doesn't have a type! Skipping ClusterLink.");
						else	
							newGuide.typeNodes.add(enviType);
						 */
						//[GenericSettlementSupport]
						if(enviType == null) {
							SettlementType settlementType = GetSettlementFromMission(currentLink.GetLayersMission());
							if(settlementType != null)
								newGuide.versitileTypeNodes.add(new VersitileTypeNode(settlementType));
							else
								System.err.println("  Every MapLocation in a Mission in an InstructionCluster must have an EnvironmentType or a generic SettlementType! "
									+ "Mission: " + currentLink.GetLayersMission().getName() + " doesn't have either! Skipping ClusterLink.");
						} else {
							if(currentLink.GetLayersMission().getGenericPlotMinRadius() > 0)
								System.err.println("min radius > 0!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
							
							newGuide.versitileTypeNodes.add(new VersitileTypeNode(enviType, currentLink.GetLayersMission().getGenericPlotMinRadius()));
						}
						System.err.println("Central Mission: "+ centralMission.getName() +", minPlotRadius: " + centralMission.getGenericPlotMinRadius());
						System.err.println("Mission: "+ currentLink.GetLayersMission().getName() +", minPlotRadius: " + currentLink.GetLayersMission().getGenericPlotMinRadius());
						
						
						
						if(debugLocationPlacement)
							System.out.println("    -Recording link with Mission: " + currentLink.GetLayersMission().getName() + " and enviType: " + enviType.toString());
						
						if(currentLink.NextLink() == null)
							reachedEndOfChain = true;
						else
							currentLink = currentLink.NextLink();
					}
					chainGuides.add(newGuide);
				}
				
				//iterate and analyze all tiles matching the centralType and create a PathOptionsAnalysis for each ChainGuide
				//MapLocation mapLocation = this.GetMapLocationById(instructionSet.GetInstructionCluster().GetCentralMission().getMapLocationId());
				MapLocation mapLocation = null;
				if(centralMission.getMapLocationId() != null && !centralMission.getMapLocationId().isEmpty())
					mapLocation = Missions.GetUniqueMapLocation(centralMission.getMapLocationId());
				
				EnvironmentType centralEnvironmentType = null;
				SettlementType centralSettlementType = null;
				if(mapLocation == null) {
					
					/*
					System.err.println("MapLocation is null for Mission: " + centralMission.getName() + ", with id: " + 
							centralMission.getMapLocationId()
							+ ". Mayybe we can use the genericLocationEnvironmentType: " + centralMission.getGenericLocationEnvironmentType());
					 */
					//[GenericSettlementSupport] - Create another approach for handling generic Settlements lacking a Nature layer
					if(centralMission.getGenericLocationEnvironmentType() != null) {
						System.out.println("MapLocation is null for Mission: " + centralMission.getName() +
							", with id: " + centralMission.getMapLocationId()
							+ ". We'll use the genericLocationEnvironmentType: " + centralMission.getGenericLocationEnvironmentType());
						centralEnvironmentType = centralMission.getGenericLocationEnvironmentType();
					} else if(centralMission.getGenericLocationSettlementType() != null) {
						System.out.println("MapLocation is null for Mission: " + centralMission.getName() +
							", with id: " + centralMission.getMapLocationId()
							+ ". genericLocationEnvironmentType is also null"
							+ ". We'll finally resort to using the genericLocationSettlementType: " + centralMission.getGenericLocationSettlementType());
						centralSettlementType = centralMission.getGenericLocationSettlementType();
					} else {
						System.err.println("MapLocation is null for Mission: " + centralMission.getName() +
								", with id: " + centralMission.getMapLocationId()
								+ ". genericLocationEnvironmentType AND genericLocationSettlementType are both null! There's nothing to indicate how to select this mission's WorldTile!");
					}
				} else {
					centralEnvironmentType = mapLocation.getEnviType();
				}
				VersitileTypeNode centralVersiNode = null;
				if(centralEnvironmentType != null)
					centralVersiNode = new VersitileTypeNode(centralEnvironmentType, centralMission.getGenericPlotMinRadius());
				else
					centralVersiNode = new VersitileTypeNode(centralSettlementType);
				
				Map<ChainGuide, PathOptionsAnalysis> clusterMap = new HashMap<ChainGuide, PathOptionsAnalysis>();
				for(ChainGuide guide : chainGuides) {
					PathOptionsAnalysis pathOptionsAnalysis = new PathOptionsAnalysis(guide);
					
					/*
					if(debugLocationPlacement) {
						System.out.println("Creating PathOptionsAnalysis for ChainGuide starting with enviType: " + guide.typeNodes.get(0));
					
					for(EnvironmentPlot centralCandidate : plotMap.get(centralType)) {
						WorldTile chosenTile = worldMap.get(centralCandidate.getEpicenter());
						if(chosenTile.GetMapLocation().GetMissionIds().size() > 0) {
							if(debugLocationPlacement)
								System.out.println("  Chosen central Tile already has a mission/s at: " + chosenTile.getPosition() + ". Continuing.");
							//TODO After we get the first test run working we need to adapt this approach to choosing unoccupied tiles from a plot rather than always using the epicenter tile
							//otherwise we could easily run out of epicenter tile to plot missions to
							
							continue;
						}
						if(debugLocationPlacement)
							System.out.println("  Chose central Tile at: " + chosenTile.getPosition());
						
						Path newPath = pathOptionsAnalysis.new Path();
						newPath.tileNodes.add(chosenTile);
						Point2D lastPoint = chosenTile.getPosition();
						
						for(EnvironmentType typeNode : guide.typeNodes) {
							if(debugLocationPlacement)
								System.out.println("    Finding closest typed tile matching: " + typeNode.toString());
							Map<Float, WorldTile> distanceTileMap = new HashMap<Float, WorldTile>();
							float smallestDistance = -1;
							for(EnvironmentPlot plot : plotMap.get(typeNode)) {
								WorldTile nextTile = worldMap.get(plot.getEpicenter());
								if(nextTile.GetMapLocation().GetMissionIds().size() > 0) {
									if(debugLocationPlacement)
										System.out.println("      Chosen central Tile already has a mission/s at: " + chosenTile.getPosition() + ". Continuing.");
									//After we get the first test run working we need to adapt this approach to choosing unoccupied tiles from a plot rather than always using the epicenter tile
									//otherwise we could easily run out of epicenter tile to plot missions to
									//TODO
									
									continue;
								}
								
								float distance = (float)Point2D.distance(lastPoint.getX(), lastPoint.getY(), nextTile.getPosition().getX(), nextTile.getPosition().getY());
								if(smallestDistance == -1 || distance < smallestDistance)
									smallestDistance = distance;
								distanceTileMap.put(new Float(distance), nextTile);
							}
							WorldTile closestTile = distanceTileMap.get(smallestDistance);
							if(debugLocationPlacement)
								System.out.println("      Found closest typed tile at: " + closestTile.getPosition());
							
							newPath.tileNodes.add(closestTile);
							newPath.pathDistance += smallestDistance;
							
							lastPoint = closestTile.getPosition();
						}
						
						pathOptionsAnalysis.paths.add(newPath);
					}*/
					//[GenericSettlementSupport]
					if(debugLocationPlacement)
						System.out.println("Creating PathOptionsAnalysis for ChainGuide starting with type: " + centralVersiNode);
					
					for(WorldTile chosenTile : GetPotentialMissionTiles(centralVersiNode)) {
						if(debugLocationPlacement)
							System.out.println("  Chose central Tile at: " + chosenTile.getPosition() + ", with type: " + centralVersiNode.toString());
						
						Path newPath = pathOptionsAnalysis.new Path();
						newPath.tileNodes.add(chosenTile);
						Point2D lastPoint = chosenTile.getPosition();
						
						for(VersitileTypeNode versitileTypeNode : guide.versitileTypeNodes) {
							if(debugLocationPlacement)
								System.out.println("    Finding closest typed tile matching: " + versitileTypeNode.toString());
							Map<Float, WorldTile> distanceTileMap = new HashMap<Float, WorldTile>();
							float smallestDistance = -1;
							for(WorldTile nextTile : GetPotentialMissionTiles(versitileTypeNode)) {
								//if(nextTile == chosenTile) {
								if(nextTile == chosenTile || newPath.tileNodes.contains(nextTile)) {
									if(debugLocationPlacement)
										//System.out.println("      Skipping nextTile, as its the same as the chosenTile.");
										System.out.println("      Skipping nextTile, as its the same as the chosenTile or one of the tiles in the path.");
									continue;
								}
								
								float distance = (float)Point2D.distance(lastPoint.getX(), lastPoint.getY(), nextTile.getPosition().getX(), nextTile.getPosition().getY());
								if(smallestDistance == -1 || distance < smallestDistance)
									smallestDistance = distance;
								distanceTileMap.put(new Float(distance), nextTile);
							}
							WorldTile closestTile = distanceTileMap.get(smallestDistance);
							if(debugLocationPlacement)
								System.out.println("      Found closest typed tile at: " + closestTile.getPosition());
							
							newPath.tileNodes.add(closestTile);
							newPath.pathDistance += smallestDistance;
							
							lastPoint = closestTile.getPosition();
						}
						
						pathOptionsAnalysis.paths.add(newPath);
					}
					
					
					
					pathOptionsAnalysis.FindShortestPathForEachCentralTile();
					clusterMap.put(guide, pathOptionsAnalysis);
				}
				
				//Find the matching start tiles for each path from each guide and add their distances to get the distance sum, the smallest sum is the desired central tile and paths
				float shortestClusterSum = -1f;
				WorldTile mostCompactCentralTile = null;
				for(WorldTile centralKeyTile : clusterMap.values().stream().toArray(PathOptionsAnalysis[]::new)[0].centralTilesShortestPath.keySet()) {
					float clusterSum = 0f;
					for(ChainGuide guide : chainGuides) {
						WorldTile worldTile = worldMap.get(centralKeyTile.getPosition());
						if(worldTile == null)
							System.err.println("worldMap doesn't contain a WorldTile at: " + centralKeyTile.getPosition());
						Path path = null;
						if(clusterMap.get(guide).centralTilesShortestPath.containsKey(worldTile)) {
							path = clusterMap.get(guide).centralTilesShortestPath.get(worldTile);
							if(path == null)
								System.err.println("centralTilesShortestPath contains an entry for the WorldTile at: " + centralKeyTile.getPosition() + " but its Path is null.");
						} else
							System.err.println("centralTilesShortestPath doesn't contain an entry for WorldTile at: " + centralKeyTile.getPosition());
						
						clusterSum += path.pathDistance;
					}
					if(debugLocationPlacement)
						System.out.println("Cluster sum for tile: " + centralKeyTile.getPosition() + " is: " + clusterSum);
					if(shortestClusterSum == -1f || clusterSum < shortestClusterSum) {
						mostCompactCentralTile = centralKeyTile;
						shortestClusterSum = clusterSum;
					}
				}
				
				//Use clusterMap.get(guide).centralTilesShortestPath.get(worldMap.get(mostCompactCentralPlot.getEpicenter())) with the clusterMap.get(guide).chainGuide to associate the worldTiles with the missions
				//Make sure we're not stacking any missions on a single WorldTile
				//Also use the instructionSet.GetInstructionCluster().GetCentralMission() as the first index and offset all chain lists by 1 to ignore the first centralTile index
				//Set central tile
				WorldTile centralTile = mostCompactCentralTile;
				
				//We need to overwrite the randomly populated MapLocation if the Unique one for the mission
				if(mapLocation != null)
					centralTile.SetMapLocationFromUniqueLocation(mapLocation);
				else
					System.out.println("There isn't a unique MapLocation for central mission: " + centralMission.getName() + " so it'll be placed on a generic location of "
							+ "WorldTileType: " + centralTile.GetMapLocation().getTileType() + " and settlementType: " + centralTile.GetMapLocation().getSettlementType());
				
				centralTile.AddMission(centralMission);
				if(debugLocationPlacement)
					System.out.println("Most compact cluster is centered at: " + mostCompactCentralTile.getPosition() + ". Setting mission: " + centralMission.getName());
				for(ChainGuide guide : chainGuides) {
					Path path = clusterMap.get(guide).centralTilesShortestPath.get(centralTile);
					for(int i = 0; i < guide.links.size(); i++) {
						WorldTile tile = path.tileNodes.get(i+1);
						Mission layerMission = guide.links.get(i).GetLayersMission();
						if(tile.GetMapLocation().GetMissionIds().size() > 0) {
							System.err.println("WorldTile: " + tile.getPosition() + " already has: " + tile.GetMapLocation().GetMissionIds().size() + " missions."
									+ " Can't place Mission: " + layerMission.getName());
						}
						
						//Check for and set a unique MapLocation if it exists
						if(layerMission.getMapLocationId() != null && !layerMission.getMapLocationId().isEmpty())
							tile.SetMapLocationFromUniqueLocation( Missions.GetUniqueMapLocation(layerMission.getMapLocationId()) );
						else
							System.out.println("There isn't a unique MapLocation for layer mission: " + layerMission.getName() + " so it'll be placed on a generic location of WorldTileType: "
									+ tile.GetMapLocation().getTileType());
						
						tile.AddMission(layerMission);
						if(debugLocationPlacement)
							System.out.println("  Setting next Mission: " + layerMission.getName() + " at " + tile.getPosition());
					}
				}
			} else {
				//handle series, one after the next
				for(MissionPathInstruction instruction : instructionSet.GetInstructionSeries()) {
					EnvironmentType nextEnviType = GetEnvironmentFromMission(instruction.getNextMission());
					if(nextEnviType == null)
						nextEnviType = sensibleLocationEnvironments[r.nextInt(sensibleLocationEnvironments.length)];
					
					
					System.err.println("Start Mission: "+ instruction.getStartMission().getName() +", minPlotRadius: " + instruction.getStartMission().getGenericPlotMinRadius());
					System.err.println("Next Mission: "+ instruction.getNextMission().getName() +", minPlotRadius: " + instruction.getNextMission().getGenericPlotMinRadius());
					
					
					Map<Float, EnvironmentPlot> distancePlotMap = new HashMap<Float, EnvironmentPlot>();
					
					MapLocation missionsMapLoc = GetMapLocationById(instruction.getStartMission().getMapLocationId());
					Point2D seriesStartPoint = missionsMapLoc.getWorldTileData().position;
					
					for(EnvironmentPlot plot : plotMap.get(nextEnviType))
						distancePlotMap.put(new Float(Point2D.distance(seriesStartPoint.getX(), seriesStartPoint.getY(), plot.getEpicenter().getX(), plot.getEpicenter().getY())), plot);
					List<Float> distances = Arrays.asList(distancePlotMap.keySet().stream().toArray(Float[]::new));
					Collections.sort(distances);
					
					WorldTile chosenTile = null;
					int failCount = 0;
					do {
						Float desiredKey = null;
						switch(instruction.getProximityOfStartToNext()) {
							case AnyTypedPlot: case AnyTypedTile:
								desiredKey = distances.get( r.nextInt(distances.size()) );
								break;
							case ClosestTypedPlot: case ClosestTypedTile:
								desiredKey = distances.get(failCount);
								break;
							case FarthestTypedPlot: case FarthestTypedTile:
								desiredKey = distances.get(distances.size() - 1 - failCount);
								break;
							default:
								System.err.println("WorldmapPanel.PlaceLocations() - Add support for: " + instruction.getProximityOfStartToNext().toString());
								break;
						}
						
						EnvironmentPlot chosenPlot = distancePlotMap.get(desiredKey);
						//ensure that the chosen WorldTile isn't already occupied by a mission
						for(WorldTile tile : chosenPlot.getTiles()) {
							chosenTile = tile;
							if(chosenTile.GetMapLocation().GetMissionIds().size() == 0)
								break;
						}
						
						failCount++;
					} while(chosenTile.GetMapLocation().GetMissionIds().size() > 0);
					
					if(instruction.getNextMission().getMapLocationId() != null && !instruction.getNextMission().getMapLocationId().isEmpty())
						chosenTile.SetMapLocationFromUniqueLocation( Missions.GetUniqueMapLocation(instruction.getNextMission().getMapLocationId()) );
					else
						System.out.println("There isn't a unique MapLocation for next mission: " + instruction.getNextMission().getName() + " so it'll be placed on a generic location of WorldTileType: "
								+ chosenTile.GetMapLocation().getTileType());
					
					chosenTile.AddMission(instruction.getNextMission());
					
					if(debugLocationPlacement)
						System.out.println("Setting next Mission: " + instruction.getNextMission().getName() + " at " + chosenTile.getPosition());
				}
			}
		}
		
		// - Mission Population ->
	}
	
	private List<WorldTile> GetPotentialMissionTiles(VersitileTypeNode versitileTypeNode) {
		List<WorldTile> potentials = new ArrayList<WorldTile>();
		if(versitileTypeNode.environmentType != null) {
			for(EnvironmentPlot candidate : plotMap.get(versitileTypeNode.environmentType)) {
				WorldTile chosenTile = worldMap.get(candidate.getEpicenter());
				
				if(chosenTile.GetMapLocation().GetMissionIds().size() > 0) {
					if(debugLocationPlacement)
						System.out.println("WorldmapPanel.GetPotentialMissionTiles() - Potential Tile already has a mission/s at: " + chosenTile.getPosition() + ". Continuing.");
					//TODO After we get the first test run working we need to adapt this approach to choosing unoccupied tiles from a plot rather than always using the epicenter tile
					//otherwise we could easily run out of epicenter tile to plot missions to
					
					continue;
				}
				
				System.err.println("WorldmapPanel.GetPotentialMissionTiles() - Assessing minPlotRadius: "+ versitileTypeNode.minPlotRadius +" for candidate with envir: " + chosenTile.getEnviType() + ", range: " + candidate.getRange() + ", tile Count: " + candidate.tiles.size());
				if(versitileTypeNode.minPlotRadius > 0) {
					if(debugLocationPlacement)
						System.out.println("WorldmapPanel.GetPotentialMissionTiles() - Assessing minPlotRadius: candidate envir: " + chosenTile.getEnviType() + ", range: " + candidate.getRange() + ", tile Count: " + candidate.tiles.size());
					//TODO - Include EnvironmentPlot radius(or general size) as criteria for selecting WorldTiles for the Mission
					int minCountBasedOnRadius = 1;
					if(versitileTypeNode.minPlotRadius > 1)
						minCountBasedOnRadius = 6*(versitileTypeNode.minPlotRadius - 2) + (6*(int)Math.pow(2, versitileTypeNode.minPlotRadius - 2)) + 1;
					if( (candidate.getRange() == -1 || candidate.getRange() >= versitileTypeNode.minPlotRadius) && candidate.tiles.size() >= minCountBasedOnRadius) {
						if(debugLocationPlacement)
							System.out.println("WorldmapPanel.GetPotentialMissionTiles() - Plot size satisfies versitileTypeNode.minPlotRadius: " + versitileTypeNode.minPlotRadius);
						//Dont need anything here
					} else {
						if(debugLocationPlacement)
							System.out.println("WorldmapPanel.GetPotentialMissionTiles() - EnvironmentPlot is smaller than min radius: " + versitileTypeNode.minPlotRadius);
						continue;
					}
				}
				
				
				potentials.add(chosenTile);
			}
		} else {
			//WorldTile[] matchingWorldTiles = worldMap.values().stream().filter(x -> x.GetSettlementType() == versitileTypeNode.settlementType && x.GetMapLocation().getRelativeSceneDirectory())
			//	.toArray(WorldTile[]::new);
			//for(WorldTile tile : worldMap.values()) {
			//	System.out.println("tile.GetMapLocation().getRelativeComboSettlementSceneDirectory(): " + tile.GetMapLocation().getRelativeComboSettlementSceneDirectory());
			//}
			String matchingDirectory = versitileTypeNode.settlementType.toString() + "/";
			WorldTile[] matchingWorldTiles = worldMap.values().stream().filter(x ->
					x.GetMapLocation().getRelativeComboSettlementSceneDirectory() != null
					&&
					x.GetMapLocation().getRelativeComboSettlementSceneDirectory().equals(matchingDirectory))
				.toArray(WorldTile[]::new);
			System.out.println("WorldmapPanel.GetPotentialMissionTiles() - For SettlementType: "+ versitileTypeNode.settlementType +", got matchingWorldTiles.size: " + matchingWorldTiles.length);
			
			for(WorldTile candidate : matchingWorldTiles) {
				if(candidate.GetMapLocation().GetMissionIds().size() > 0) {
					if(debugLocationPlacement)
						System.out.println("WorldmapPanel.GetPotentialMissionTiles() - Potential Tile already has a mission/s at: " + candidate.getPosition() + ". Continuing.");
					continue;
				}
				potentials.add(candidate);
			}
		}
		
		if(potentials.size() == 0)
			System.err.println("WorldmapPanel.GetPotentialMissionTiles() - Returning an empty list for versitileTypeNode of type: " + versitileTypeNode.toString());
		
		return potentials;
	}
	
	EnvironmentType[] sensibleLocationEnvironments = new EnvironmentType[] {
			EnvironmentType.Dunes,
			EnvironmentType.Farmland,
			EnvironmentType.Forest,
			EnvironmentType.Grassland,
			EnvironmentType.Marsh,
			EnvironmentType.Mountainous
	};
	
	private class Range {
		public Range(int rangeMin, int rangeMax) {
			this.rangeMin = rangeMin;
			this.rangeMax = rangeMax;
		}
		public int rangeMin;
		public int rangeMax;
		public int GetRandomInRange() {
			return rangeMin + r.nextInt(rangeMax - rangeMin + 1);
		}
	}
	Map<SettlementType, Range> genericSettlementProcedures = new HashMap<SettlementType, Range>();
	
	private EnvironmentType GetEnvironmentFromMission(Mission mission) {
		EnvironmentType enviType = null;
		
		//MapLocation missionsMapLoc = GetMapLocationById(mission.getMapLocationId());
		//dont use this, its only for getting MapLocations AFTER WorldGeneration is complete, instead as Missions for the uniqueLocationInstances
		MapLocation missionsMapLoc = null;
		if(mission.getMapLocationId() != null && !mission.getMapLocationId().isEmpty())
			missionsMapLoc = Missions.GetUniqueMapLocation(mission.getMapLocationId());
		
		if(missionsMapLoc == null) {
			if(mission.getGenericLocationEnvironmentType() == null)
				System.err.println("WorldmapPanel.GetEnvironmentFromMission() - Every Mission in an InstructionCluster needs a MapLocation or Mission: " + mission.getName() +
					" doesn't have one! Skipping ClusterLink.");
			else {
				System.out.println("WorldmapPanel.GetEnvironmentFromMission() - Resorting to getGenericLocationEnvironmentType: " + mission.getGenericLocationEnvironmentType() +
						", for mission: " + mission.getName());
				enviType = mission.getGenericLocationEnvironmentType();
			}
		} else {
			enviType = missionsMapLoc.getEnviType();
		}
		return enviType;
	}
	
	private SettlementType GetSettlementFromMission(Mission mission) {
		SettlementType settlementType = null;
		
		//MapLocation missionsMapLoc = GetMapLocationById(mission.getMapLocationId());
		//dont use this, its only for getting MapLocations AFTER WorldGeneration is complete, instead as Missions for the uniqueLocationInstances
		MapLocation missionsMapLoc = null;
		if(mission.getMapLocationId() != null && !mission.getMapLocationId().isEmpty())
			missionsMapLoc = Missions.GetUniqueMapLocation(mission.getMapLocationId());
		
		if(missionsMapLoc == null) {
			if(mission.getGenericLocationSettlementType() == null)
				System.err.println("WorldmapPanel.GetSettlementFromMission() - Every Mission in an InstructionCluster needs a MapLocation or Mission: " + mission.getName() +
						" doesn't have one! Skipping ClusterLink.");
			else {
				System.out.println("WorldmapPanel.GetSettlementFromMission() - Resorting to getGenericLocationSettlementType: " + mission.getGenericLocationSettlementType() +
						", for mission: " + mission.getName());
				settlementType = mission.getGenericLocationSettlementType();
			}
		} else {
			settlementType = missionsMapLoc.getSettlementType();
		}
		return settlementType;
	}
	
	private Point GetRectDirectionFromHexToHex(Point2D from, Point2D to) {
		int diffX = Math.round((float)to.getX()) - Math.round((float)from.getX());
		int normX = Math.max(-1, Math.min(diffX, 1));
		if(normX == 0) //make sure there always a east or west influence
			normX = r.nextInt(101)/100f <= 0.5f ? -1 : 1;
		
		int diffY = Math.round((float)to.getY()) - Math.round((float)from.getY());
		int normY = Math.max(-1, Math.min(diffY, 1));
		
		return new Point(normX, normY);
	}
	
	private boolean IsIsland(List<WorldTile> tiles, int worldWidth, int worldHeight) {
		List<Point2D> points = GetNeighborPoints(tiles, worldWidth, worldHeight);
		tiles.forEach(x -> x.Set(false));
		boolean isIsland = true;
		for(Point2D point : points) {
			WorldTile neighborTile = worldMap.get(point);
			neighborTile.Set(false);
			
			if(isDebuggingGeneration)
				System.out.println("IsIsland() - testing: "+ point +", enviType: " + neighborTile.getEnviType());
			if(neighborTile.getEnviType() != EnvironmentType.Water) {
				isIsland = false;
				break;
			}
		}
		return isIsland;
	}
	
	private boolean IsTouchingEnvironment(EnvironmentType enviType, WorldTile tile, int worldWidth, int worldHeight) {
		//TODO
		//This is the source of the problem
		//Its only testing tiles included in this tile's plot
		//List<Point2D> points = GetNeighborPoints(Arrays.asList(new WorldTile[] { tile }), worldWidth, worldHeight);
		//tile.Set(false);
		List<Point2D> points = GetNeighborPoints_PURE(tile, worldWidth, worldHeight);
		
		boolean isTouchingEnvironment = false;
		if(isDebuggingGeneration)
			System.out.println("  IsTouchingAnyWater(" + tile.getPosition() + ") - Neighbor count: " + points.size());
		for(Point2D point : points) {
			WorldTile neighborTile = worldMap.get(point);
			neighborTile.Set(false);
			
			if(isDebuggingGeneration)
				System.out.println("    -Testing Neighbor: " + point + ", " + neighborTile.getEnviType());
			
			if(neighborTile.getEnviType() == enviType) {
				isTouchingEnvironment = true;
				break;
			}
		}
		
		return isTouchingEnvironment;
	}
	
	private List<WorldTile> GetTouchingEnvironmentTiles(EnvironmentType enviType, WorldTile tile, int worldWidth, int worldHeight) {
		List<Point2D> points = GetNeighborPoints_PURE(tile, worldWidth, worldHeight);
		
		List<WorldTile> touchingTiles = new ArrayList<WorldTile>();
		if(isDebuggingGeneration)
			System.out.println("  IsTouchingAnyWater(" + tile.getPosition() + ") - Neighbor count: " + points.size());
		for(Point2D point : points) {
			WorldTile neighborTile = worldMap.get(point);
			neighborTile.Set(false);
			
			if(isDebuggingGeneration)
				System.out.println("    -Testing Neighbor: " + point + ", " + neighborTile.getEnviType());
			
			if(neighborTile.getEnviType() == enviType)
				touchingTiles.add(neighborTile);
		}
		
		return touchingTiles;
	}
	
	private boolean IsTouchingAnyWaterExceptRiver(WorldTile tile, River self, int worldWidth, int worldHeight) {
		List<Point2D> points = GetNeighborPoints(Arrays.asList(new WorldTile[] { tile }), worldWidth, worldHeight);
		tile.Set(false);
		boolean isTouchingAnyWaterExceptRiver = false;
		for(Point2D point : points) {
			WorldTile neighborTile = worldMap.get(point);
			neighborTile.Set(false);
			if(neighborTile.getEnviType() == EnvironmentType.Water) {
				if(!self.tiles.contains(neighborTile)) {
					isTouchingAnyWaterExceptRiver = true;
					break;
				} else {
					if(isDebuggingGeneration)
						System.out.println("    -IsTouchingAnyWaterExceptRiver(" + tile.getPosition() + ") - Found water neighbor that part of this river: " + neighborTile.getPosition());
				}
			}
		}
		return isTouchingAnyWaterExceptRiver;
	}
	
	//Is detects when a river tile touches more than one tile of itself
	private boolean IsRiverStackingOnSelf(WorldTile tile, River self, int worldWidth, int worldHeight) {
		List<Point2D> points = GetNeighborPoints(Arrays.asList(new WorldTile[] { tile }), worldWidth, worldHeight);
		tile.Set(false);
		boolean isRiverStackingOnSelf = false;
		int touchingRiverSelfTileCount = 0;
		for(Point2D point : points) {
			WorldTile neighborTile = worldMap.get(point);
			neighborTile.Set(false);
			if(neighborTile.getEnviType() == EnvironmentType.Water) {
				if(self.tiles.contains(neighborTile)) {
					touchingRiverSelfTileCount++;
					if(touchingRiverSelfTileCount == 2) {
						isRiverStackingOnSelf = true;
						break;
					}
				} //else {
					//System.out.println("    -IsTouchingAnyWaterExceptRiver(" + tile.getPosition() + ") - Found water neighbor that part of this river: " + neighborTile.getPosition());
				//}
			}
		}
		return isRiverStackingOnSelf;
	}
	
	private boolean IsTouchingAnyOceanOrBay(WorldTile tile, int worldWidth, int worldHeight) {
		List<Point2D> points = GetNeighborPoints(Arrays.asList(new WorldTile[] { tile }), worldWidth, worldHeight);
		tile.Set(false);
		boolean isTouchingAnyOceanOrBay = false;
		for(Point2D point : points) {
			WorldTile neighborTile = worldMap.get(point);
			neighborTile.Set(false);
			if(neighborTile.getEnviType() == EnvironmentType.Water && (neighborTile.GetPlot() == null || (neighborTile.GetPlot() != null && bayPlots.contains(neighborTile.GetPlot())))) {
				isTouchingAnyOceanOrBay = true;
				break;
			}
		}
		return isTouchingAnyOceanOrBay;
	}
	
	public enum HexDirection {
		E,
		NE,
		NW,
		W,
		SW,
		SE
	};
	
	HexDirection[] radialHexDirections = new HexDirection[] {
			HexDirection.E,
			HexDirection.NE,
			HexDirection.NW,
			HexDirection.W,
			HexDirection.SW,
			HexDirection.SE
	};
	
	static Map<Point, HexDirection> rectToHexMatrix;
	
	static {
		rectToHexMatrix = new HashMap<Point, HexDirection>();
		rectToHexMatrix.put(new Point(1, 0), HexDirection.E);
		rectToHexMatrix.put(new Point(1, -1), HexDirection.NE);
		rectToHexMatrix.put(new Point(-1, -1), HexDirection.NW);
		rectToHexMatrix.put(new Point(-1, 0), HexDirection.W);
		rectToHexMatrix.put(new Point(-1, 1), HexDirection.SW);
		rectToHexMatrix.put(new Point(1, 1), HexDirection.SE);
	}
	
	private Point2D GetHexPointByRectDirection(Point2D startHexPoint, int rectangularX, int rectangularY) {
		Point rectDir = new Point(rectangularX, rectangularY);
		if(!WorldmapPanel.rectToHexMatrix.containsKey(rectDir))
			System.err.println("rectToHexMatrix doesnt contain a mapping for rect point: " + rectDir);
		
		HexDirection dir = WorldmapPanel.rectToHexMatrix.get(rectDir);
		Point2D hexPoint = GetHexInDirection(startHexPoint, dir);
		
		if(worldMap.get(hexPoint) == null) {
			System.err.println("No such hex point exists: " + hexPoint + ", attempted HexDirection: " + dir.toString());
			//provide random hexpoint
			hexPoint = worldMap.keySet().stream().toArray(Point2D[]::new)[r.nextInt(worldMap.keySet().size())];
		}
			
		return hexPoint;
	}
	
	private Point2D GetHexInDirection(Point2D startPoint, HexDirection direction) {
		Point2D potentialPoint = null;
		boolean isSourceOdd = Math.ceil((float)startPoint.getX()) > (float)startPoint.getX();
		//System.out.println("GetHexInDirection() - startPoint: " + startPoint + ", isSourceOdd: " + isSourceOdd);
		if(isSourceOdd) { //is odd row
			switch(direction) {
			case E: //Right
				potentialPoint = new Point2D.Float((float)(startPoint.getX() + 1.0), (float)startPoint.getY());
				break;
			case NE: //Top Right
				potentialPoint = new Point2D.Float( Math.round((float)startPoint.getX()) + 1f, (float)startPoint.getY() - 1f);
				break;
			case NW: //Top Left
				potentialPoint = new Point2D.Float( Math.round((float)startPoint.getX()), (float)startPoint.getY() - 1f);
				break;
			case W: //Left
				potentialPoint = new Point2D.Float( (Math.round( (float)startPoint.getX() ) - 1) + 0.4f, (float)startPoint.getY());
				break;
			case SW: //Bottom Left
				potentialPoint = new Point2D.Float( Math.round((float)startPoint.getX()), (float)startPoint.getY() + 1f);
				break;
			case SE: //Bottom Right
				potentialPoint = new Point2D.Float( Math.round((float)startPoint.getX()) + 1f, (float)startPoint.getY() + 1f);
				break;
			}
		} else { //is even row
			switch(direction) {
			case E: //Right
				potentialPoint = new Point2D.Float((float)(startPoint.getX() + 1.0), (float)startPoint.getY());
				break;
			case NE: //Top Right
				potentialPoint = new Point2D.Float((float)(startPoint.getX() + 0.4), (float)startPoint.getY() - 1f);
				break;
			case NW: //Top Left
				potentialPoint = new Point2D.Float((float)(startPoint.getX() - 0.6), (float)startPoint.getY() - 1f);
				break;
			case W: //Left
				potentialPoint = new Point2D.Float( (float)(startPoint.getX() - 1.0), (float)startPoint.getY());
				break;
			case SW: //Bottom Left
				potentialPoint = new Point2D.Float((float)(startPoint.getX() - 0.6), (float)startPoint.getY() + 1f);
				break;
			case SE: //Bottom Right
				potentialPoint = new Point2D.Float((float)(startPoint.getX() + 0.4), (float)startPoint.getY() + 1f);
				break;
			}
		}
		return potentialPoint;
	}
	
	private List<Point2D> GetNeighborPoints_PURE(WorldTile tile, int worldWidth, int worldHeight) {
		List<Point2D> neighborPoints = new ArrayList<Point2D>();
		for(int i = 0; i < 6; i++) {
			Point2D potentialPoint = GetHexInDirection(tile.getPosition(), radialHexDirections[i]);
			
			//reject any tiles that would be outside the world bounds
			int xInt = Math.round((float)potentialPoint.getX());
			int yInt = Math.round((float)potentialPoint.getY());
			if(xInt < 0 || yInt < 0 || xInt >= worldWidth || yInt >= worldHeight) {
				//System.out.println("Caught out-of-bounds past worldWidth: " + worldWidth + " or worldHeight: " + worldHeight + " - " + potentialPoint + ", xInt: " + xInt + ", yInt: " + yInt);
				continue;
			}
			
			WorldTile worldTile = worldMap.get(potentialPoint);
			if(worldTile == null)
				System.err.println("WorldTile doesn't exist at: " + potentialPoint);
			
			neighborPoints.add(potentialPoint);
		}
		return neighborPoints;
	}
	
	private List<Point2D> GetNeighborPoints(List<WorldTile> tiles, int worldWidth, int worldHeight) {
		//Disqualify all tiles for neighbor consideration
		tiles.forEach(x -> x.Set(true));
		
		List<Point2D> neighborPoints = new ArrayList<Point2D>();
		
		for(WorldTile tile : tiles) {
			for(int i = 0; i < 6; i++) {
				Point2D potentialPoint = GetHexInDirection(tile.getPosition(), radialHexDirections[i]);
				
				//if(isRiversStage)
				//	System.out.println("    GetNeighborPoints - tile: " + potentialPoint);
				
				//reject any tiles that would be outside the world bounds
				int xInt = Math.round((float)potentialPoint.getX());
				int yInt = Math.round((float)potentialPoint.getY());
				if(xInt < 0 || yInt < 0 || xInt >= worldWidth || yInt >= worldHeight) {
					//System.out.println("Caught out-of-bounds past worldWidth: " + worldWidth + " or worldHeight: " + worldHeight + " - " + potentialPoint + ", xInt: " + xInt + ", yInt: " + yInt);
					continue;
				}
				
				//Instantiate all WorldTiles in worldMap at startup and have a isSet flag that we can check here
				WorldTile worldTile = worldMap.get(potentialPoint);
				
				if(worldTile != null) {
					if(!worldTile.isSet()) {
						neighborPoints.add(potentialPoint);
						worldTile.Set(true);
						//if(isRiversStage)
						//	System.out.println("    -tile available: " + worldTile.getEnviType());
					} else {
						//if(isRiversStage)
						//	System.out.println("    -already set: " + worldTile.getEnviType());
					}
				} else {
					System.err.println("no point on worldMap: " + potentialPoint + ", i: " + i + ", isSourceOdd: " + ((int)Math.ceil(potentialPoint.getX()) > (int)potentialPoint.getX()));
				}
			}
		}
		
		return neighborPoints;
	}
	
	private Point2D convertToHexPosition(int x, int y, int worldHeight) {
		float xShift = 0f;
		if(worldHeight % 2 == 0) {
			if(y % 2 == 0)
				xShift = 0.4f;
		} else {
			if(y % 2 != 0)
				xShift = 0.4f;
		}
		return new Point2D.Float((float)x + xShift, (float)y);
	}
	
	//Use this to improve expected results of randomness after having been considered for randomness to take effect
	Random r_worldType = new Random();
	
	//Now with randomization this method needs to be run individually for each WorldTile
	private WorldTileType GetWorldType(EnvironmentType enviType, int ring, int range) {
		BlendWeight[] weights = plotBlends.stream().filter(x -> x.enviType == enviType).findFirst().get().weights;
		
		int index = 0;
		float ringPercentile = (float)ring / range;
		if(range >= weights.length) { //if there isnt enough room for blending then do all main tiles on the plot
			for(int i = weights.length - 1; i > -1; i--) {
				if(ringPercentile >= weights[i].startRadius) {
					index = i;
					
					if(weights[index].randomnessFactor != 0f) {
						//Introduce some randomness for PlotBlends that incorporate it
						if(Math.abs(r.nextInt(101))/100f <= Math.abs(weights[index].randomnessFactor)) {
							//System.out.println("Randomness Occuring");
							int newIndex = index;
							int indexShift = r_worldType.nextInt(101)/100f <= 0.5f ? 1 : 0;
							if(weights[index].randomnessFactor > 0) {
								newIndex -= indexShift;
								if(newIndex < 0)
									System.err.println("Can't use a positive randomnessFactor for BlendWeights at the beginning of arrays. Use negative values to blend down. EnriType: " + enviType.toString());
							} else {
								newIndex += indexShift;
								if(newIndex >= weights.length)
									System.err.println("Can't use a negative randomnessFactor for BlendWeights at the end of arrays. Use positive values to blend up. EnriType: " + enviType.toString());
							}
							index = newIndex;
						}
					}
					
					break;
				}
			}
		}
		
		return weights[index].tileType;
	}
	
	// <- Worldmap Referencing -
	
	public MapLocation GetMapLocationById(String mapLocationId) {
		WorldTile worldTile = worldMap.values().stream().filter(x -> x.mapLocation.getId().equals(mapLocationId)).findFirst().orElse(null);
		MapLocation mapLocation = null;
		if(worldTile == null) {
			System.err.println("WorldmapPanel.GetMapLocationById() - Couldn't find mapLocation with id: " + mapLocationId);
			Thread.dumpStack();
		} else
			mapLocation = worldTile.mapLocation;
		return mapLocation;
	}
	
	//  - Worldmap Referencing ->
	
	// <- Tile GUI Interaction -
	private void ToggleTileHover(boolean enabled, Point2D hexPoint) {
		if(ignoreEventsDuringSetup || travelAnimPane.IsAnimating())
			return;
		
		//Ignore hovering events while dragging because this can lead to unintented enter events from the area surrounding the mouse
		if(isDragging)
			return;
		
		WorldTile worldTile = worldMap.get(hexPoint);
		
		if(isDebuggingGeneration) {
			String message = "";
			if(enabled)
				message += "Entered: ";
			else
				message += "Exited: ";
			
			message += hexPoint.toString();
			
			message += ", Environment: " + worldTile.getEnviType().toString();
			
			message += ", Type: ";
			if(!worldTile.IsBlank())
				message += worldTile.tileType.toString();
			else
				message += "null";
			
			if(worldTile.GetPlot() != null) {
				message += "      belongs to plotMap: " + plotMap.get(worldTile.getEnviType()).contains(worldTile.GetPlot());
			
				message += "      belongs to bays: " + bayPlots.contains(worldTile.GetPlot());
				
				message += "      belongs to erosionPlots: " + erosionPlots.contains(worldTile.GetPlot());
				
				message += "      plot epicenter: " + worldTile.GetPlot().getEpicenter();
			} else {
				message += "      no parent plot";
			}
			tileSelectionLabel.setText("Tile Event - " + message);
		}
		
		cursorSprite.GetImagePanel().setVisible(enabled);
		
		if(enabled) {
			cursorSprite.Move(hexPoint, true);
		
			if(currentlySelectedTile == null)
				UpdateLocationLabel(worldTile);
			
			if((CanMoveToTile(worldTile) && travelTiles.size() == 0)
				||
			   (worldTile.getPosition() == playerSprite.GetWorldLocation())
			    ||
			   (travelTiles.size() > 0 && GetAdjacentTiles(currentlySelectedTile).contains(worldTile) && !travelTiles.contains(worldTile))
			  )
			{
				if(travelTiles.size() == 0 || worldTile.getPosition() == playerSprite.GetWorldLocation()) {
					travelIndicator.Move(hexPoint, true);
					travelPathIndicator.FlagInvisible();
				//} else {
				} else if(travelPane.IsTravelCostAffordable()) {
					
					travelPathIndicator.Move(hexPoint, true);
					travelIndicator.FlagInvisible();
				}
				revertPathIndicator.FlagInvisible();
			} else if(travelTiles.contains(worldTile) && travelTiles.indexOf(worldTile) < travelTiles.size()-1) {
				revertPathIndicator.Move(hexPoint, true);
				travelIndicator.FlagInvisible();
				travelPathIndicator.FlagInvisible();
			}
		} else if(currentlySelectedTile == null) {
			HideLocationLabelPanel();
		}
		
		if(!enabled) {
			travelIndicator.FlagInvisible();
			travelPathIndicator.FlagInvisible();
			revertPathIndicator.FlagInvisible();
		}
	}
	
	Color highlightGreenColor = new Color(0.6f, 1f, 0.6f, 1f);
	Color disabledColor = new Color(1f, 1f, 1f, 0.55f);
	
	private void UpdateLocationLabel(WorldTile worldTile) {
		if(worldTile.isDiscovered()) {
			Color tileTint = GetTileTint(worldTile);
			if(tileTint != null)
				locationTileImage.SetNewImage(GUIUtil.GetTintedImage(SpriteSheetUtility.GetTerrainFromWorldTile(worldTile.getTileType())[worldTile.getTerrainImageIndex()], tileTint));
			else
				locationTileImage.SetNewImage(SpriteSheetUtility.GetTerrainFromWorldTile(worldTile.getTileType())[worldTile.getTerrainImageIndex()]);
		} else {
			locationTileImage.SetNewImage(SpriteSheetUtility.getTerraingroupBlank()[worldTile.getBlankTerrainImageIndex()]);
		}
		locationTileImage.setVisible(true);
		
		//Label generic Bodies of Water based by their isLake, isRiver, isBay properties
		if(worldTile.isDiscovered()) {
			if(!worldTile.IsUniqueLocation() && worldTile.getEnviType() == EnvironmentType.Water)
				locationText.setText(GetDisplayNameForWorldTileType(worldTile));
			else
				locationText.setText(worldTile.GetMapLocation().getName());
		} else {
			locationText.setText("???");
		}
		locationText.setVisible(true);
		
		if(worldTile.isDiscovered()) {
			if(worldTile.GetSettlementType() != null) {
				settlementLocImage.SetNewImage(GUIUtil.GetSettlementImage(worldTile.GetSettlementType(), worldTile.GetSettlementDesignation()));
				settlementLocImage.setVisible(true);
				locationSubtext.setText(GetDisplayNameForWorldTileType(worldTile));
				locationSubtext.setVisible(true);
			} else if(worldTile.IsUniqueLocation()) {
				locationSubtext.setText(GetDisplayNameForWorldTileType(worldTile));
				locationSubtext.setVisible(true);
			} else {
				settlementLocImage.setVisible(false);
				locationSubtext.setVisible(false);
			}
		} else {
			settlementLocImage.setVisible(false);
			locationSubtext.setVisible(false);
		}
		
		locationLabelBG.setVisible(true);
	}
	
	List<WorldTileType> worldTypeSpecialNames = new ArrayList<WorldTileType>();
	
	public String GetDisplayNameForWorldTileType(WorldTile worldTile) {
		if(worldTypeSpecialNames.size() == 0) {
			worldTypeSpecialNames.add(WorldTileType.forestEdge);
			worldTypeSpecialNames.add(WorldTileType.water);
		}
		
		WorldTileType tileType = worldTile.getTileType();
		String typeName = tileType.toString();
		if(worldTypeSpecialNames.contains(tileType)) {
			switch(tileType) {
				case forestEdge:
					typeName = "Forest Edge";
					break;
				case water:
					if(worldTile.isLake)
						typeName = "Lake";
					else if(worldTile.isRiver)
						typeName = "River";
					else if(worldTile.isBay)
						typeName = "Bay";
					else
						typeName = "Ocean";
					break;
				default:
					System.err.println("WorldmapPanel.GetDisplayNameForWorldTileType() - Add support for: " + tileType.toString());
					break;
			}
		}
		return typeName;
	}
	
	private Color GetTileTint(WorldTile tile) {
		if(tile.isBay)
			return bayTint;
		else if(tile.isLake)
			return lakeTint;
		else if(tile.isRiver)
			return riverTint;
		else
			return null;
	}
	
	List<WorldTile> adjacentWorldTiles = new ArrayList<WorldTile>();
	
	public class TravelInfo {
		public TravelInfo(int risk, int staminaCost) {
			this.risk = risk;
			this.staminaCost = staminaCost;
		}
		public int risk;
		public int staminaCost;
	}
	
	Map<WorldTileType, Integer> terrainRiskMap = new HashMap<WorldTileType, Integer>();
	Map<WorldTileType, Integer> terrainStaminaMap = new HashMap<WorldTileType, Integer>();
	
	Map<SettlementType, Integer> settlementRiskMap = new HashMap<SettlementType, Integer>();
	Map<SettlementType, Integer> settlementStaminaMap = new HashMap<SettlementType, Integer>();
	
	private int partyStamina;
	public static final int partyStaminaMax = 100;
	
	private TravelInfo GetTravelInfoBetween(WorldTile from, WorldTile to) {
		int from_terrainRisk = terrainRiskMap.get(from.getTileType());
		int from_terrainStamina = terrainStaminaMap.get(from.getTileType());
		
		int from_settlementRisk = 0;
		int from_settlementStamina = 0;
		if(from.GetSettlementType() != null) {
			settlementRiskMap.get(from.GetSettlementType());
			settlementStaminaMap.get(from.GetSettlementType());
		}
		
		int to_terrainRisk = terrainRiskMap.get(to.getTileType());
		int to_terrainStamina = terrainStaminaMap.get(to.getTileType());
		int to_settlementRisk = 0;
		int to_settlementStamina = 0;
		if(to.GetSettlementType() != null) {
			to_settlementRisk = settlementRiskMap.get(to.GetSettlementType());
			to_settlementStamina = settlementStaminaMap.get(to.GetSettlementType());
		}
		
		return new TravelInfo(from_terrainRisk + from_settlementRisk + to_terrainRisk + to_settlementRisk,
							  from_terrainStamina + from_settlementStamina + to_terrainStamina + to_settlementStamina);
	}
	
	private void ClickTile(Point2D hexPoint) {
		if(ignoreEventsDuringSetup || travelAnimPane.IsAnimating())
			return;
		
		WorldTile worldTile = worldMap.get(hexPoint);
		
		if(isDebuggingGeneration) {
			String message = "Clicked: " + hexPoint.toString();
			message += ", Type: ";
			if(!worldTile.IsBlank())
				message += worldTile.tileType.toString();
			else
				message += "null";
			tileSelectionLabel.setText("Tile Event - " + message);
		}
		
		//if the user is clicking the selected tile or they're trying to add more tiles to a path thats already overextended
		if(worldTile == currentlySelectedTile)
			return;
		
		//Find out if we can move along the path to the new tile 
		WorldTile currentOrQueuedTile = null;
		if(travelTiles.size() > 0)
			currentOrQueuedTile = travelTiles.get(travelTiles.size()-1);
		else {
			currentOrQueuedTile = worldMap.get(playerSprite.GetWorldLocation());
			projectedStamina = partyStamina;
		}
		TravelInfo travelInfo = GetTravelInfoBetween(currentOrQueuedTile, worldTile);
		
		System.out.println("worldTile.getPosition(): " + worldTile.getPosition() + ", playerSprite.GetWorldLocation(): " + playerSprite.GetWorldLocation());
		
		UpdateLocationLabel(worldTile);
		boolean isntLastIndex = travelTiles.indexOf(worldTile) < travelTiles.size()-1;
		if(worldTile.getPosition() == playerSprite.GetWorldLocation()) {
			//Clicked the currently occupied tile
			
			currentlySelectedTile = worldTile;
			
			//Hide stuff
			cursorSprite.GetImagePanel().setVisible(false);
			travelPane.Animate(false, null);
			travelIndicator.FlagInvisible();
			travelPathIndicator.FlagInvisible();
			revertPathIndicator.FlagInvisible();
			
			//Wipe Stuff
			ResetTravelPath();
			
			//Show stuff
			UpdateLocationLabel(worldTile);
			SelectEnterableTile(hexPoint);
		} else if( (CanMoveToTile(worldTile) && travelTiles.size() == 0)
				   ||
				   //(GetAdjacentTiles(currentOrQueuedTile).contains(worldTile) && !travelTiles.contains(worldTile) && projectedStamina - travelInfo.staminaCost > 0)
				   (GetAdjacentTiles(currentOrQueuedTile).contains(worldTile) && !travelTiles.contains(worldTile) && travelPane.IsTravelCostAffordable())
				   ||
				   (travelTiles.contains(worldTile) && isntLastIndex)
				 )
		{
			//Clicked a tile that can interact with our travelPath is some manner
			
			currentlySelectedTile = worldTile;
			
			boolean isBlockedByInteraction = mapLocationPanel.hasDisabledMovement;
			if(!isBlockedByInteraction) {
				if(CanMoveToTile(worldTile) && travelTiles.size() == 0) {
					cursorSprite.imagePanel.ClearTint();
					
					travelTiles.add(currentOrQueuedTile);
					travelTiles.add(worldTile);
					travelInfos.add(travelInfo);
				} else {
					cursorSprite.imagePanel.ClearTint();
					
					if(!travelTiles.contains(worldTile)) {
						//Add the tile to the path
						//subtract the cost of the last one cause the TravelPane will deduct the current cost on its own
						projectedStamina -= travelInfos.get(travelInfos.size()-1).staminaCost;
						//System.out.println("projectedStamina: " + projectedStamina);
						
						AddNextPathTile(currentOrQueuedTile);
						//Drop a path node down on the previous tile
						pathNodeSprites[travelInfos.size()-1].Move(currentOrQueuedTile.getPosition(), true);
					} else if(isntLastIndex) {
						//Rewind our path to the clicked tile
						int lastIndex = travelTiles.size() - 1;
						for(int r = lastIndex; r > -1; r--) {
							if(travelTiles.get(r) == worldTile)
								break;
							
							travelTiles.remove(r);
							
							projectedStamina += travelInfos.get(r-2).staminaCost;
							travelInfos.remove(r-1);
							
							//Remove path node
							pathNodeSprites[r-1].FlagInvisible();
						}
						travelInfo = travelInfos.get(travelInfos.size()-1);
						System.out.println("Rewinding to an earlier tile, adjusted projectedStamina to: " + projectedStamina);
					}
				}
			}
			
			//Show stuff
			//need to update the TravelPane.pendingTravelInfo via the TravelPane.Animate method before comparing it below
			tileSelectionSprite.Move(hexPoint, true);
			travelPane.Animate(true, travelInfo);
			
			tileSelectionSprite.GetImagePanel().setVisible(false);
			if(!travelPane.IsTravelCostAffordable() || isBlockedByInteraction)
				tileSelectionSprite.GetImagePanel().SetTint(new Color(1f, 0.6f, 0.6f), ColorBlend.Multiply);
			else
				tileSelectionSprite.GetImagePanel().ClearTint();
			
			//Hide stuff
			cursorSprite.GetImagePanel().setVisible(false);
			enterButtonBg.setVisible(false);
			enterButton.setVisible(false);
			travelIndicator.FlagInvisible();
			travelPathIndicator.FlagInvisible();
			revertPathIndicator.FlagInvisible();
		} else {
			//Clicked an unrelated/uninteractable tile
			
			currentlySelectedTile = null;
			
			//Show stuff
			cursorSprite.Move(hexPoint, true);
			
			//Hide stuff
			tileSelectionSprite.GetImagePanel().ClearTint();
			tileSelectionSprite.GetImagePanel().setVisible(false);
			enterButtonBg.setVisible(false);
			enterButton.setVisible(false);
			travelPane.Animate(false, null);
			
			//Wipe Stuff
			ResetTravelPath();
		}
		
		//Adjust cursor depnding on selections
		if(currentlySelectedTile != null) {
			if(cursorSprite.GetImagePanel().GetTintColor() != disabledColor)
				cursorSprite.GetImagePanel().SetTint(disabledColor, ColorBlend.Multiply);
		} else {
			if(cursorSprite.GetImagePanel().GetTintColor() != null)
				cursorSprite.GetImagePanel().ClearTint();
		}
	}
	
	//The user has pressed a button that allows them to chose the next tile along their desired path
	public void AddNextPathTile(WorldTile currentOrQueuedTile) {
		TravelInfo travelInfo = GetTravelInfoBetween(currentOrQueuedTile, currentlySelectedTile);
		
		travelTiles.add(currentlySelectedTile);
		travelInfos.add(travelInfo);
	}
	
	private void SelectEnterableTile(Point2D hexPoint) {
		tileSelectionSprite.GetImagePanel().SetTint(highlightGreenColor, ColorBlend.Multiply);
		tileSelectionSprite.Move(hexPoint, true);
		enterButtonBg.setVisible(true);
		enterButton.setVisible(true);
	}
	
	CustomButtonUltra enterButton;
	
	
	//Used to disable highlight events while a tile is selected
	WorldTile currentlySelectedTile;
	int projectedStamina;
	List<WorldTile> travelTiles = new ArrayList<WorldTile>();
	List<TravelInfo> travelInfos = new ArrayList<TravelInfo>();
	
	private boolean CanMoveToTile(WorldTile worldTile) {
		boolean result = adjacentWorldTiles.contains(worldTile) && !mapLocationPanel.HasDisabledMovement();
		//System.out.println("WorldmapPanel.CanMoveToTile(WorldTile worldTile) = "+ result +
		//		" - position: " + worldTile.getPosition() +
		//		", adjacentWorldTiles.contains(worldTile): " + adjacentWorldTiles.contains(worldTile) + 
		//		", mapLocationPanel.HasDisabledMovement(): " + mapLocationPanel.HasDisabledMovement());
		return result;
	}
	
	class TravelPane extends JLayeredPane {
		JFxLabel panelTitle;
		JLabel bgNineconLabel;
		JFxLabel riskLabel;
		JLabel riskValueBar;
		Ninecon greenRiskNinecon;
		Ninecon yellowRiskNinecon;
		Ninecon orangeRiskNinecon;
		Ninecon redRiskNinecon;
		JLabel riskBarSlot;
		float originalRiskBarWidth;
		//Stamina bar
		JFxLabel staminaLabel;
		Ninecon redBarNinecon;
		Ninecon greenBarNinecon;
		JLabel staminaValueBar;
		JLabel staminaValueBarTravelEx;
		JLabel staminaBarSlot;
		JLabel staminaBg;
		
		CustomButton travelButton;
		Timer animTimer;
		private boolean isAnimatingOpen;
		int closedPosY;
		int openYPos;
		int moveInterval = 64;
		
		JLabel blockedNotificationBG;
		JFxLabel blockedNotificationLabel;
		JFxLabel blockedNotificationLabel2;
		
		public TravelPane(Dimension containerSize, float relWidth, float relHeight) {
			super();
			setOpaque(false);
			setBackground(new Color(0,0,0,0));
			Dimension pixelSize = GUIUtil.GetRelativeSize(relWidth, relHeight);
			Point posFromBRCorner = new Point(containerSize.width - pixelSize.width, containerSize.height - pixelSize.height);
			setLocation(posFromBRCorner);
			setSize(pixelSize);
	
			
			//Movement Blocked by MapLocation Interaction
			float notifNormYLoc = relHeight * 0.25f;
			Point notifBGLoc = GUIUtil.GetRelativePoint(relWidth * 0.01f, notifNormYLoc);
			Dimension notifBgSize = GUIUtil.GetRelativeSize(relWidth * 0.76f, relHeight - (notifNormYLoc / 2));
			
			int notifLabelHeight = (pixelSize.height - notifBGLoc.y) / 2;
			blockedNotificationLabel = new JFxLabel("There's something preventing you from traveling.", SwingConstants.CENTER, GUIUtil.Body_2, Color.RED);
			blockedNotificationLabel.setLocation(notifBGLoc.x, notifBGLoc.y);
			blockedNotificationLabel.setSize(notifBgSize.width, notifLabelHeight);
			add(blockedNotificationLabel, 8, 1);
			blockedNotificationLabel.setVisible(false);
			
			blockedNotificationLabel2 = new JFxLabel("(Enter your current location to investigate)", SwingConstants.CENTER, GUIUtil.Body_2, Color.GRAY);
			blockedNotificationLabel2.setLocation(notifBGLoc.x, notifBGLoc.y + ((notifBgSize.height - notifBGLoc.y) / 2));
			blockedNotificationLabel2.setSize(notifBgSize.width, notifLabelHeight);
			add(blockedNotificationLabel2, 7, 1);
			blockedNotificationLabel2.setVisible(false);
			
			blockedNotificationBG = new JLabel(SpriteSheetUtility.ValueBGNinecon(new Color(0.9f, 0.9f, 0.7f), ColorBlend.Multiply));
			blockedNotificationBG.setLocation(notifBGLoc);
			blockedNotificationBG.setSize(notifBgSize);
			add(blockedNotificationBG, 6, 1);
			blockedNotificationBG.setVisible(false);
			
			
			
			//PanelTitle
			panelTitle = new JFxLabel("Travel", SwingConstants.LEFT, GUIUtil.ItalicHeader_L, new Color(0.96f, 0.92f, 0.9f)).withStroke(Color.DARK_GRAY, 2, true);
			panelTitle.setLocation((int)(pixelSize.width * 0.02f), (int)(pixelSize.height * 0f));
			panelTitle.setSize((int)(pixelSize.width * 0.22f), (int)(pixelSize.height * 0.2f));
			//add(panelTitle, 0);
			add(panelTitle, 1, 0);
			panelTitle.setVisible(false);
			
			
			//Create components
			riskLabel = new JFxLabel("Risk", SwingConstants.LEFT, GUIUtil.Header, Color.BLACK);
			riskLabel.setLocation((int)(pixelSize.width * 0.08f), (int)(pixelSize.height * 0.31f));
			riskLabel.setSize((int)(pixelSize.width * 0.22f), (int)(pixelSize.height * 0.2f));
			add(riskLabel, 5, 1);
			riskLabel.setVisible(false);
			
			riskBarSlot = new JLabel(SpriteSheetUtility.ValueBGNinecon(Color.GRAY, ColorBlend.Multiply));
			riskBarSlot.setLocation((int)(pixelSize.width * 0.28f), (int)(pixelSize.height * 0.3f));
			riskBarSlot.setSize((int)(pixelSize.width * 0.44f), (int)(pixelSize.height * 0.21f));
			add(riskBarSlot, 2, 1);
			riskBarSlot.setVisible(false);
			
			greenRiskNinecon = SpriteSheetUtility.ValueBGNinecon(new Color(0.5f, 0.99f, 0.5f), ColorBlend.Multiply);
			yellowRiskNinecon = SpriteSheetUtility.ValueBGNinecon(new Color(0.95f, 0.95f, 0.3f), ColorBlend.Multiply);
			orangeRiskNinecon = SpriteSheetUtility.ValueBGNinecon(new Color(0.99f, 0.6f, 0.2f), ColorBlend.Multiply);
			redRiskNinecon = SpriteSheetUtility.ValueBGNinecon(new Color(0.99f, 0.2f, 0.2f), ColorBlend.Multiply);
			riskValueBar = new JLabel(greenRiskNinecon);
			riskValueBar.setLocation((int)(pixelSize.width * 0.284f), (int)(pixelSize.height * 0.31f));
			riskValueBar.setSize((int)(pixelSize.width * 0.432f), (int)(pixelSize.height * 0.18f));
			add(riskValueBar, 4, 1);
			originalRiskBarWidth = riskValueBar.getSize().width;
			riskValueBar.setVisible(false);
			
			staminaLabel = new JFxLabel("Stamina", SwingConstants.LEFT, GUIUtil.Header, Color.BLACK);
			staminaLabel.setLocation((int)(pixelSize.width * 0.08f), (int)(pixelSize.height * 0.64f));
			staminaLabel.setSize((int)(pixelSize.width * 0.22f), (int)(pixelSize.height * 0.2f));
			add(staminaLabel, 3, 1);
			
			staminaValueBar = new JLabel(SpriteSheetUtility.ValueBGNinecon(Color.GREEN, ColorBlend.Multiply));
			staminaValueBar.setLocation((int)(pixelSize.width * 0.284f), (int)(pixelSize.height * 0.65f));
			staminaValueBar.setSize((int)(pixelSize.width * 0.432f), (int)(pixelSize.height * 0.18f));
			add(staminaValueBar, 4, 1);
			originalBarWidth = staminaValueBar.getSize().width;
			float percentage = (float)partyStamina / partyStaminaMax;
			SetBar(percentage);
			
			redBarNinecon = SpriteSheetUtility.ValueBGNinecon(Color.RED, ColorBlend.Multiply);
			greenBarNinecon = SpriteSheetUtility.ValueBGNinecon(new Color(0.85f, 0.99f, 0.75f), ColorBlend.Multiply);
			staminaValueBarTravelEx = new JLabel(redBarNinecon);
			staminaValueBarTravelEx.setLocation((int)(pixelSize.width * 0.284f), (int)(pixelSize.height * 0.65f));
			staminaValueBarTravelEx.setSize((int)(pixelSize.width * 0.432f), (int)(pixelSize.height * 0.18f));
			add(staminaValueBarTravelEx, 3, 1);
			staminaValueBarTravelEx.setVisible(false);
			
			staminaBarSlot = new JLabel(SpriteSheetUtility.ValueBGNinecon(Color.GRAY, ColorBlend.Multiply));
			staminaBarSlot.setLocation((int)(pixelSize.width * 0.28f), (int)(pixelSize.height * 0.64f));
			staminaBarSlot.setSize((int)(pixelSize.width * 0.44f), (int)(pixelSize.height * 0.21f));
			add(staminaBarSlot, 2, 1);
			
			travelButton = new CustomButton(GUIUtil.TravelOnFootSymbol, null, Color.GREEN, SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(), new Color(240, 240, 240));
			travelButton.setLocation((int)(pixelSize.width * 0.79f), (int)(pixelSize.height * 0.21f));
			travelButton.setSize((int)(pixelSize.width * 0.18f), (int)(pixelSize.width * 0.18f));
			add(travelButton, 1, 1);
			travelButton.setVisible(false);
			travelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					HideLocationLabelPanel();
					Animate(false, null);
					travelAnimPane.StartTravelAnim(travelTiles, travelInfos);
				}
			});
			
			bgNineconLabel = new JLabel(SpriteSheetUtility.ValueBGNinecon());
			Dimension bgSize = GUIUtil.GetRelativeSize(relWidth + 0.05f, relHeight + 0.05f);
			closedPosY = bgSize.height;
			openYPos = (int)(pixelSize.height * 0.1f);
			bgNineconLabel.setLocation(0, closedPosY);
			bgNineconLabel.setSize(bgSize);
			add(bgNineconLabel, 2);
			bgNineconLabel.setVisible(false);
			
			staminaBg = new JLabel(SpriteSheetUtility.ValueBGNinecon());
			staminaBg.setLocation((int)(pixelSize.width * 0.055f), (int)(pixelSize.height * 0.565f));
			staminaBg.setSize((int)(pixelSize.width * 0.7f), (int)(pixelSize.height * 0.35f));
			add(staminaBg, 3);
			
			animTimer = new Timer(10, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					int newYLoc = bgNineconLabel.getLocation().y + (isAnimatingOpen ? -moveInterval : moveInterval);
					if(isAnimatingOpen && newYLoc <= openYPos) {
						newYLoc = openYPos;
						animTimer.stop();
						SetComponentsVisible(true);
						ShowTravelCost((projectedStamina - pendingTravelInfo.staminaCost) / (float)partyStaminaMax);
						System.out.println("WorldmapPanel.TravelPane_Contructor.animTimer - projectedStamina: " + projectedStamina +
								", pendingTravelInfo.staminaCost: " + pendingTravelInfo.staminaCost +
								", partyStaminaMax: " + partyStaminaMax);
					} else if(!isAnimatingOpen && newYLoc >= closedPosY) {
						newYLoc = closedPosY;
						animTimer.stop();
						bgNineconLabel.setVisible(false);
						//System.out.println("Anim - close complete");
					} else {
						//System.out.println("Anim - newYLoc: " + newYLoc);
					}
					bgNineconLabel.setLocation(bgNineconLabel.getLocation().x, newYLoc);
					bgNineconLabel.repaint();
				}
			});
			animTimer.setInitialDelay(0);
		}
		
		private void SetComponentsVisible(boolean visible) {
			panelTitle.setVisible(visible);
			riskLabel.setVisible(visible);
			riskBarSlot.setVisible(visible);
			riskValueBar.setVisible(visible);
			travelButton.setVisible(visible);
			if(!visible || (visible && mapLocationPanel.hasDisabledMovement)) {
				blockedNotificationBG.setVisible(visible);
				blockedNotificationLabel.setVisible(visible);
				blockedNotificationLabel2.setVisible(visible);
			}
		}
		
		public void Animate(boolean animateOpen, TravelInfo travelInfo) {
			if(!animateOpen && !isAnimatingOpen)
				return;
				
			isAnimatingOpen = animateOpen;
			pendingTravelInfo = travelInfo;
			if(!animateOpen) {
				SetComponentsVisible(false);
				ResetCostBar();
			} else {
				bgNineconLabel.setVisible(true);
				Update(travelInfo);
			}
			animTimer.restart();
		}
		
		private void Update(TravelInfo travelInfo) {
			//riskValue.setText("" + travelInfo.risk);
			riskValueBar.setBounds(riskValueBar.getLocation().x, riskValueBar.getLocation().y,
					  (int)Math.round(originalRiskBarWidth * ((float)travelInfo.risk / 20)), riskValueBar.getSize().height);
			if(travelInfo.risk <= 7)
				riskValueBar.setIcon(greenRiskNinecon);
			else if(travelInfo.risk <= 12)
				riskValueBar.setIcon(yellowRiskNinecon);
			else if(travelInfo.risk <= 17)
				riskValueBar.setIcon(orangeRiskNinecon);
			else
				riskValueBar.setIcon(redRiskNinecon);
			
			//travelButton.setEnabled(travelInfo.staminaCost < projectedStamina);
			travelButton.setEnabled(travelInfo.staminaCost < projectedStamina && !mapLocationPanel.HasDisabledMovement());
		}
		
		public TravelInfo pendingTravelInfo;
		public void ShowTravelCost(float percentage) {
			float currentPercentage = (float)projectedStamina / partyStaminaMax;
			//System.out.println("ShowTravelCost() - projectedStamina: "+ projectedStamina +", percentage: " + percentage + ", currentPercentage: " + currentPercentage);
			
			if(percentage <= currentPercentage) {
				SetBar(percentage);
				staminaValueBarTravelEx.setBounds(staminaValueBarTravelEx.getLocation().x, staminaValueBarTravelEx.getLocation().y,
						  (int)Math.round(originalBarWidth * ((float)projectedStamina / partyStaminaMax)), staminaValueBarTravelEx.getSize().height);
				if(staminaValueBarTravelEx.getIcon() != redBarNinecon)
					staminaValueBarTravelEx.setIcon(redBarNinecon);
			} else {
				if(percentage > 1f)
					percentage = 1f;
				SetBar(currentPercentage);
				staminaValueBarTravelEx.setBounds(staminaValueBarTravelEx.getLocation().x, staminaValueBarTravelEx.getLocation().y,
												  (int)Math.round(originalBarWidth * percentage), staminaValueBarTravelEx.getSize().height);
				if(staminaValueBarTravelEx.getIcon() != greenBarNinecon)
					staminaValueBarTravelEx.setIcon(greenBarNinecon);
			}
			staminaValueBarTravelEx.setVisible(true);
		}
		public boolean IsTravelCostAffordable() {
			boolean result = pendingTravelInfo == null || projectedStamina - pendingTravelInfo.staminaCost > 0;
			System.out.println("WorldmapPanel.TravelPane.IsTravelCostAffordable() = " + result + 
					" - pendingTravelInfo == null: " + (pendingTravelInfo == null) +
					" || projectedStamina - pendingTravelInfo.staminaCost > 0: " + (pendingTravelInfo == null || projectedStamina - pendingTravelInfo.staminaCost > 0) +
					", projectedStamina: " + projectedStamina + " pendingTravelInfo.staminaCost: " + (pendingTravelInfo != null ? pendingTravelInfo.staminaCost : 0));
			return result;
		}
		
		public void ResetCostBar() {
			float percentage = (float)projectedStamina / partyStaminaMax;
			SetBar(percentage);
			staminaValueBarTravelEx.setVisible(false);
		}
		
		int originalBarWidth;
		public void SetBar(float percentage) {
			float clampedPercentage = Math.max(0f, Math.min(1f,  percentage));
			staminaValueBar.setBounds(staminaValueBar.getLocation().x, staminaValueBar.getLocation().y, (int)Math.round(originalBarWidth * clampedPercentage), staminaValueBar.getSize().height);
		}
	}
	TravelPane travelPane;
	
	class TravelAnimPane extends JLayeredPane implements ITransitionListener {
		//BG
		FadeTransitionPanel lightbox;
		
		//Terrain layers
		Dimension dimension;
		Dimension fullTileDimension;
		int startFadeEdge_left;
		int startFadeEdge_right;
		int startTerrainHeight;
		int startTerrainSideHeight;
		int startCharacterHeight;
		int targetTerrainHeight;
		int targetTerrainSideHeight;
		int targetCharacterHeight;
		
		Dimension settlementIconDimension;
		int targetSettlementHeight;
		int startSettlementHeight;
		
		JPanel terrainCarriage1_top;
		ImagePanel c1_terrainTile1;
		ImagePanel c1_terrainTile2;
		JPanel terrainCarriage1_bottom;
		ImagePanel c1_terrainSide1;
		ImagePanel c1_terrainSide2;
		JPanel terrainCarriage1_settlement;
		ImagePanel c1_settlementIcon1;
		ImagePanel c1_settlementIcon2;
		
		JPanel terrainCarriage2_top;
		ImagePanel c2_terrainTile1;
		ImagePanel c2_terrainTile2;
		JPanel terrainCarriage2_bottom;
		ImagePanel c2_terrainSide1;
		ImagePanel c2_terrainSide2;
		JPanel terrainCarriage2_settlement;
		ImagePanel c2_settlementIcon1;
		ImagePanel c2_settlementIcon2;
		
		JPanel terrainCarriage3_top;
		ImagePanel c3_terrainTile1;
		ImagePanel c3_terrainTile2;
		JPanel terrainCarriage3_bottom;
		ImagePanel c3_terrainSide1;
		ImagePanel c3_terrainSide2;
		JPanel terrainCarriage3_settlement;
		ImagePanel c3_settlementIcon1;
		ImagePanel c3_settlementIcon2;
		
		//Action layers
		BattleCharacterController travelCharacterController;
		ImagePanel emote;
		CustomButton reactButton1;
		CustomButton reactButton2;
		CustomButton reactButton3;
		//FG
		ImagePanel fadeOutOverlay_left;
		ImagePanel fadeOutOverlay_right;
		
		int timerDelay = 8;
		int alphaInterval = 8;
		int fadeOutEdgeSpeed = 16;
		int terrainSpeed = 32;
		int travelInterval = 15;
		int travelPixelsPerFrame = 2;
		Timer enterAnim;
		Timer travelTimer;
		Timer exitAnim;
		boolean isAnimating;
		
		public TravelAnimPane(Dimension dimension) {
			this.dimension = dimension;
			setSize(dimension);
			setLocation(0,0);
			setOpaque(false);
			setBackground(new Color(0,0,0,0));
			int paneIndex = 0;
			
			//setup fade out overlay
			fadeOutOverlay_left = new ImagePanel("worldmap/TravelAnim_EdgeFadeOut_Small_Left.png");
			fadeOutOverlay_left.setOpaque(false);
			fadeOutOverlay_left.setBackground(new Color(0,0,0,0));
			fadeOutOverlay_left.setSize(new Dimension(dimension.width / 2, dimension.height));
			startFadeEdge_left = -dimension.width / 2 * 25/32;
			fadeOutOverlay_left.setLocation(startFadeEdge_left, 0);
			add(fadeOutOverlay_left, paneIndex);
			paneIndex++;
			fadeOutOverlay_right = new ImagePanel("worldmap/TravelAnim_EdgeFadeOut_Small_Right.png");
			fadeOutOverlay_right.setOpaque(false);
			fadeOutOverlay_right.setBackground(new Color(0,0,0,0));
			fadeOutOverlay_right.setSize(new Dimension(dimension.width / 2, dimension.height));
			startFadeEdge_right = dimension.width - (dimension.width / 2 * 7/32);
			fadeOutOverlay_right.setLocation(startFadeEdge_right, 0);
			add(fadeOutOverlay_right, paneIndex);
			paneIndex++;
			
			//setup emote
			
			
			//setup character
			travelCharacterController = new BattleCharacterController(Game.Instance().GetPlayerData().getType(), null, null, true);
			travelCharacterController.GetImagePanel().setOpaque(false);
			travelCharacterController.GetImagePanel().setBackground(new Color(0f,0f,0f,0f));
			Dimension characerSize = GUIUtil.GetRelativeSize(0.12f, true);
			targetCharacterHeight = dimension.height / 2 - (int)Math.round(characerSize.height * 0.9f);
			startCharacterHeight = dimension.height + targetCharacterHeight;
			travelCharacterController.GetImagePanel().setBounds(dimension.width / 2 - (characerSize.width / 2), startCharacterHeight, characerSize.width, characerSize.height);
			add(travelCharacterController.GetImagePanel(), paneIndex);
			paneIndex++;
			
			//setup react buttons
			
			
			//General Purpose Variables
			fullTileDimension = GUIUtil.GetRelativeSize(0.25f, true);
			fullTileDimension.height = fullTileDimension.width * 2;
			targetTerrainHeight = dimension.height / 2 - (fullTileDimension.height / 2);
			startTerrainHeight = dimension.height + targetTerrainHeight;
			//General Purpose Settlement Variables
			settlementIconDimension = new Dimension(fullTileDimension.width / 2, fullTileDimension.width / 2);
			targetSettlementHeight = dimension.height / 2 - (fullTileDimension.height / 3);
			startSettlementHeight = dimension.height + targetSettlementHeight;
			int carriageWidth = fullTileDimension.width * 2;
			int settlementSideGap = (fullTileDimension.width - settlementIconDimension.width) / 2;
			
			//Setup Carriage 1 Settlement Icons
			terrainCarriage1_settlement = new JPanel(new GridLayout(1, 2));
			terrainCarriage1_settlement.setOpaque(false);
			terrainCarriage1_settlement.setBackground(new Color(0,0,0,0));
			terrainCarriage1_settlement.setBounds(dimension.width / 2 - (fullTileDimension.width / 2), startSettlementHeight,
												  carriageWidth, settlementIconDimension.height);
			
			c1_settlementIcon1 = new ImagePanel(GUIUtil.GetSettlementImage(SettlementType.AssassinationTarget, null));
			c1_settlementIcon1.setOpaque(false);
			c1_settlementIcon1.setBackground(new Color(0,0,0,0));
			c1_settlementIcon1.SetPaintInsideInsets(true);
			c1_settlementIcon1.setBorder(BorderFactory.createEmptyBorder(0, settlementSideGap, 0, settlementSideGap));
			terrainCarriage1_settlement.add(c1_settlementIcon1);
			c1_settlementIcon2 = new ImagePanel(GUIUtil.GetSettlementImage(SettlementType.AssassinationTarget, null));
			c1_settlementIcon2.setOpaque(false);
			c1_settlementIcon2.setBackground(new Color(0,0,0,0));
			c1_settlementIcon2.SetPaintInsideInsets(true);
			c1_settlementIcon2.setBorder(BorderFactory.createEmptyBorder(0, settlementSideGap, 0, settlementSideGap));
			terrainCarriage1_settlement.add(c1_settlementIcon2);
			add(terrainCarriage1_settlement, paneIndex);
			paneIndex++;
			
			//Setup Carriage 1 Terrain Tiles
			terrainCarriage1_top = new JPanel(new GridLayout(1, 2));
			terrainCarriage1_top.setOpaque(false);
			terrainCarriage1_top.setBackground(new Color(0,0,0,0));
			terrainCarriage1_top.setBounds(dimension.width / 2 - (fullTileDimension.width / 2), startTerrainHeight,
										   fullTileDimension.width * 2, fullTileDimension.height * 3/4);
			
			c1_terrainTile1 = new ImagePanel(SpriteSheetUtility.GetTerrainFromWorldTile(WorldTileType.blank)[0]);
			c1_terrainTile1.setOpaque(false);
			c1_terrainTile1.setBackground(new Color(0,0,0,0));
			terrainCarriage1_top.add(c1_terrainTile1);
			c1_terrainTile2 = new ImagePanel(SpriteSheetUtility.GetTerrainFromWorldTile(WorldTileType.blank)[0]);
			c1_terrainTile2.setOpaque(false);
			c1_terrainTile2.setBackground(new Color(0,0,0,0));
			terrainCarriage1_top.add(c1_terrainTile2);
			add(terrainCarriage1_top, paneIndex);
			paneIndex++;
			
			//Setup Carriage 1 Terrain Sides
			terrainCarriage1_bottom = new JPanel(new GridLayout(1, 2));
			terrainCarriage1_bottom.setOpaque(false);
			terrainCarriage1_bottom.setBackground(new Color(0,0,0,0));
			targetTerrainSideHeight = dimension.height / 2 - (fullTileDimension.height / 2) + (fullTileDimension.height * 7/12);
			startTerrainSideHeight = dimension.height + targetTerrainSideHeight;
			terrainCarriage1_bottom.setBounds(dimension.width / 2 - (fullTileDimension.width / 2), startTerrainSideHeight,
											  fullTileDimension.width * 2, fullTileDimension.height * 3/8);
			
			c1_terrainSide1 = new ImagePanel(SpriteSheetUtility.getTerrainUnderGround());
			c1_terrainSide1.setOpaque(false);
			c1_terrainSide1.setBackground(new Color(0,0,0,0));
			terrainCarriage1_bottom.add(c1_terrainSide1);
			c1_terrainSide2 = new ImagePanel(SpriteSheetUtility.getTerrainUnderGround());
			c1_terrainSide2.setOpaque(false);
			c1_terrainSide2.setBackground(new Color(0,0,0,0));
			terrainCarriage1_bottom.add(c1_terrainSide2);
			add(terrainCarriage1_bottom, paneIndex);
			paneIndex++;
			
			//Setup Carriage 2 Settlement Icons
			terrainCarriage2_settlement = new JPanel(new GridLayout(1, 2));
			terrainCarriage2_settlement.setOpaque(false);
			terrainCarriage2_settlement.setBackground(new Color(0,0,0,0));
			terrainCarriage2_settlement.setBounds(dimension.width / 2 - (fullTileDimension.width / 2), startSettlementHeight,
												  carriageWidth, settlementIconDimension.height);
			
			c2_settlementIcon1 = new ImagePanel(GUIUtil.GetSettlementImage(SettlementType.AssassinationTarget, null));
			c2_settlementIcon1.setOpaque(false);
			c2_settlementIcon1.setBackground(new Color(0,0,0,0));
			c2_settlementIcon1.SetPaintInsideInsets(true);
			c2_settlementIcon1.setBorder(BorderFactory.createEmptyBorder(0, settlementSideGap, 0, settlementSideGap));
			terrainCarriage2_settlement.add(c2_settlementIcon1);
			c2_settlementIcon2 = new ImagePanel(GUIUtil.GetSettlementImage(SettlementType.AssassinationTarget, null));
			c2_settlementIcon2.setOpaque(false);
			c2_settlementIcon2.setBackground(new Color(0,0,0,0));
			c2_settlementIcon2.SetPaintInsideInsets(true);
			c2_settlementIcon2.setBorder(BorderFactory.createEmptyBorder(0, settlementSideGap, 0, settlementSideGap));
			terrainCarriage2_settlement.add(c2_settlementIcon2);
			add(terrainCarriage2_settlement, paneIndex);
			paneIndex++;
			
			//Setup Carriage 2 Terrain Tiles
			terrainCarriage2_top = new JPanel(new GridLayout(1, 2));
			terrainCarriage2_top.setOpaque(false);
			terrainCarriage2_top.setBackground(new Color(0,0,0,0));
			terrainCarriage2_top.setBounds(dimension.width / 2 - (fullTileDimension.width / 2) + (fullTileDimension.width * 2), startTerrainHeight,
										   fullTileDimension.width * 2, fullTileDimension.height * 3/4);
			
			c2_terrainTile1 = new ImagePanel(SpriteSheetUtility.GetTerrainFromWorldTile(WorldTileType.blank)[0]);
			c2_terrainTile1.setOpaque(false);
			c2_terrainTile1.setBackground(new Color(0,0,0,0));
			terrainCarriage2_top.add(c2_terrainTile1);
			c2_terrainTile2 = new ImagePanel(SpriteSheetUtility.GetTerrainFromWorldTile(WorldTileType.blank)[0]);
			c2_terrainTile2.setOpaque(false);
			c2_terrainTile2.setBackground(new Color(0,0,0,0));
			terrainCarriage2_top.add(c2_terrainTile2);
			add(terrainCarriage2_top, paneIndex);
			paneIndex++;
			
			//Setup Carriage 2 Terrain Sides
			terrainCarriage2_bottom = new JPanel(new GridLayout(1, 2));
			terrainCarriage2_bottom.setOpaque(false);
			terrainCarriage2_bottom.setBackground(new Color(0,0,0,0));
			terrainCarriage2_bottom.setBounds(dimension.width / 2 - (fullTileDimension.width / 2) + (fullTileDimension.width * 2), startTerrainSideHeight,
											  fullTileDimension.width * 2, fullTileDimension.height * 3/8);
			
			c2_terrainSide1 = new ImagePanel(SpriteSheetUtility.getTerrainUnderGround());
			c2_terrainSide1.setOpaque(false);
			c2_terrainSide1.setBackground(new Color(0,0,0,0));
			terrainCarriage2_bottom.add(c2_terrainSide1);
			c2_terrainSide2 = new ImagePanel(SpriteSheetUtility.getTerrainUnderGround());
			c2_terrainSide2.setOpaque(false);
			c2_terrainSide2.setBackground(new Color(0,0,0,0));
			terrainCarriage2_bottom.add(c2_terrainSide2);
			add(terrainCarriage2_bottom, paneIndex);
			paneIndex++;
			
			//Setup Carriage 3 Settlement Icons
			terrainCarriage3_settlement = new JPanel(new GridLayout(1, 2));
			terrainCarriage3_settlement.setOpaque(false);
			terrainCarriage3_settlement.setBackground(new Color(0,0,0,0));
			terrainCarriage3_settlement.setBounds(dimension.width / 2 - (fullTileDimension.width / 2), startSettlementHeight,
												  carriageWidth, settlementIconDimension.height);
			
			c3_settlementIcon1 = new ImagePanel(GUIUtil.GetSettlementImage(SettlementType.AssassinationTarget, null));
			c3_settlementIcon1.setOpaque(false);
			c3_settlementIcon1.setBackground(new Color(0,0,0,0));
			c3_settlementIcon1.SetPaintInsideInsets(true);
			c3_settlementIcon1.setBorder(BorderFactory.createEmptyBorder(0, settlementSideGap, 0, settlementSideGap));
			terrainCarriage3_settlement.add(c3_settlementIcon1);
			c3_settlementIcon2 = new ImagePanel(GUIUtil.GetSettlementImage(SettlementType.AssassinationTarget, null));
			c3_settlementIcon2.setOpaque(false);
			c3_settlementIcon2.setBackground(new Color(0,0,0,0));
			c3_settlementIcon2.SetPaintInsideInsets(true);
			c3_settlementIcon2.setBorder(BorderFactory.createEmptyBorder(0, settlementSideGap, 0, settlementSideGap));
			terrainCarriage3_settlement.add(c3_settlementIcon2);
			add(terrainCarriage3_settlement, paneIndex);
			paneIndex++;
			
			//Setup Carriage 3 Terrain Tiles
			terrainCarriage3_top = new JPanel(new GridLayout(1, 2));
			terrainCarriage3_top.setOpaque(false);
			terrainCarriage3_top.setBackground(new Color(0,0,0,0));
			terrainCarriage3_top.setBounds(dimension.width / 2 - (fullTileDimension.width / 2) + (fullTileDimension.width * 4), startTerrainHeight,
										   fullTileDimension.width * 2, fullTileDimension.height * 3/4);
			
			c3_terrainTile1 = new ImagePanel(SpriteSheetUtility.GetTerrainFromWorldTile(WorldTileType.blank)[0]);
			c3_terrainTile1.setOpaque(false);
			c3_terrainTile1.setBackground(new Color(0,0,0,0));
			terrainCarriage3_top.add(c3_terrainTile1);
			c3_terrainTile2 = new ImagePanel(SpriteSheetUtility.GetTerrainFromWorldTile(WorldTileType.blank)[0]);
			c3_terrainTile2.setOpaque(false);
			c3_terrainTile2.setBackground(new Color(0,0,0,0));
			terrainCarriage3_top.add(c3_terrainTile2);
			add(terrainCarriage3_top, paneIndex);
			paneIndex++;
			
			//Setup Carriage 3 Terrain Sides
			terrainCarriage3_bottom = new JPanel(new GridLayout(1, 2));
			terrainCarriage3_bottom.setOpaque(false);
			terrainCarriage3_bottom.setBackground(new Color(0,0,0,0));
			terrainCarriage3_bottom.setBounds(dimension.width / 2 - (fullTileDimension.width / 2) + (fullTileDimension.width * 4), startTerrainSideHeight,
											  fullTileDimension.width * 2, fullTileDimension.height * 3/8);
			
			c3_terrainSide1 = new ImagePanel(SpriteSheetUtility.getTerrainUnderGround());
			c3_terrainSide1.setOpaque(false);
			c3_terrainSide1.setBackground(new Color(0,0,0,0));
			terrainCarriage3_bottom.add(c3_terrainSide1);
			c3_terrainSide2 = new ImagePanel(SpriteSheetUtility.getTerrainUnderGround());
			c3_terrainSide2.setOpaque(false);
			c3_terrainSide2.setBackground(new Color(0,0,0,0));
			terrainCarriage3_bottom.add(c3_terrainSide2);
			add(terrainCarriage3_bottom, paneIndex);
			paneIndex++;
			
			//Setup lightbox
			lightbox = new FadeTransitionPanel(new Color(0.02f, 0.02f, 0.03f, 0.8f));
			lightbox.setSize(dimension);
			add(lightbox, paneIndex);
			paneIndex++;
			
			
			enterAnim = new Timer(timerDelay, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					int nextLeftX = Math.min(0, fadeOutOverlay_left.getLocation().x + fadeOutEdgeSpeed);
					fadeOutOverlay_left.setBounds(nextLeftX, fadeOutOverlay_left.getLocation().y,
							  					  fadeOutOverlay_left.getSize().width, fadeOutOverlay_left.getSize().height);

					int nextRightX = Math.max(dimension.width/2, fadeOutOverlay_right.getLocation().x - fadeOutEdgeSpeed);
					fadeOutOverlay_right.setBounds(nextRightX, fadeOutOverlay_right.getLocation().y,
												   fadeOutOverlay_right.getSize().width, fadeOutOverlay_right.getSize().height);
					
					int nextSettlementY = Math.max(targetSettlementHeight, terrainCarriage1_settlement.getLocation().y - terrainSpeed);
					terrainCarriage1_settlement.setBounds(terrainCarriage1_settlement.getLocation().x, nextSettlementY,
							terrainCarriage1_settlement.getSize().width, terrainCarriage1_settlement.getSize().height);
					terrainCarriage2_settlement.setBounds(terrainCarriage2_settlement.getLocation().x, nextSettlementY,
							terrainCarriage2_settlement.getSize().width, terrainCarriage2_settlement.getSize().height);
					terrainCarriage3_settlement.setBounds(terrainCarriage3_settlement.getLocation().x, nextSettlementY,
							terrainCarriage3_settlement.getSize().width, terrainCarriage3_settlement.getSize().height);
					
					int nextTerrainY = Math.max(targetTerrainHeight, terrainCarriage1_top.getLocation().y - terrainSpeed);
					terrainCarriage1_top.setBounds(terrainCarriage1_top.getLocation().x, nextTerrainY,
							terrainCarriage1_top.getSize().width, terrainCarriage1_top.getSize().height);
					terrainCarriage2_top.setBounds(terrainCarriage2_top.getLocation().x, nextTerrainY,
							terrainCarriage2_top.getSize().width, terrainCarriage2_top.getSize().height);
					terrainCarriage3_top.setBounds(terrainCarriage3_top.getLocation().x, nextTerrainY,
							terrainCarriage3_top.getSize().width, terrainCarriage3_top.getSize().height);
					
					int nextTerrainBottomY = Math.max(targetTerrainSideHeight, terrainCarriage1_bottom.getLocation().y - terrainSpeed);
					terrainCarriage1_bottom.setBounds(terrainCarriage1_bottom.getLocation().x, nextTerrainBottomY,
							terrainCarriage1_bottom.getSize().width, terrainCarriage1_bottom.getSize().height);
					terrainCarriage2_bottom.setBounds(terrainCarriage2_bottom.getLocation().x, nextTerrainBottomY,
							terrainCarriage2_bottom.getSize().width, terrainCarriage2_bottom.getSize().height);
					terrainCarriage3_bottom.setBounds(terrainCarriage3_bottom.getLocation().x, nextTerrainBottomY,
							terrainCarriage3_bottom.getSize().width, terrainCarriage3_bottom.getSize().height);
					
					int nextCharacterY = Math.max(targetCharacterHeight, travelCharacterController.GetImagePanel().getLocation().y - terrainSpeed);
					travelCharacterController.GetImagePanel().setBounds(travelCharacterController.GetImagePanel().getLocation().x, nextCharacterY,
							travelCharacterController.GetImagePanel().getSize().width, travelCharacterController.GetImagePanel().getSize().height);
					
					if(nextLeftX == 0
							&& nextRightX == dimension.width/2
							&& nextSettlementY == targetSettlementHeight
							&& nextTerrainY == targetTerrainHeight
							&& nextTerrainBottomY == targetTerrainSideHeight
							&& nextCharacterY == targetCharacterHeight) {
						
						enterAnim.stop();
					}
				}
			});
			enterAnim.setInitialDelay(0);
			
			exitAnim = new Timer(timerDelay, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					int nextLeftX = Math.max(startFadeEdge_left, fadeOutOverlay_left.getLocation().x - fadeOutEdgeSpeed);
					fadeOutOverlay_left.setBounds(nextLeftX, fadeOutOverlay_left.getLocation().y,
							  					  fadeOutOverlay_left.getSize().width, fadeOutOverlay_left.getSize().height);

					int nextRightX = Math.min(startFadeEdge_right, fadeOutOverlay_right.getLocation().x + fadeOutEdgeSpeed);
					fadeOutOverlay_right.setBounds(nextRightX, fadeOutOverlay_right.getLocation().y,
												   fadeOutOverlay_right.getSize().width, fadeOutOverlay_right.getSize().height);
					
					int nextSettlementY = Math.min(startSettlementHeight, terrainCarriage1_settlement.getLocation().y + terrainSpeed);
					terrainCarriage1_settlement.setBounds(terrainCarriage1_settlement.getLocation().x, nextSettlementY,
							terrainCarriage1_settlement.getSize().width, terrainCarriage1_settlement.getSize().height);
					terrainCarriage2_settlement.setBounds(terrainCarriage2_settlement.getLocation().x, nextSettlementY,
							terrainCarriage2_settlement.getSize().width, terrainCarriage2_settlement.getSize().height);
					terrainCarriage3_settlement.setBounds(terrainCarriage3_settlement.getLocation().x, nextSettlementY,
							terrainCarriage3_settlement.getSize().width, terrainCarriage3_settlement.getSize().height);
					
					int nextTerrainY = Math.min(startTerrainHeight, terrainCarriage1_top.getLocation().y + terrainSpeed);
					terrainCarriage1_top.setBounds(terrainCarriage1_top.getLocation().x, nextTerrainY,
							terrainCarriage1_top.getSize().width, terrainCarriage1_top.getSize().height);
					terrainCarriage2_top.setBounds(terrainCarriage2_top.getLocation().x, nextTerrainY,
							terrainCarriage2_top.getSize().width, terrainCarriage2_top.getSize().height);
					terrainCarriage3_top.setBounds(terrainCarriage3_top.getLocation().x, nextTerrainY,
							terrainCarriage3_top.getSize().width, terrainCarriage3_top.getSize().height);
					
					int nextTerrainBottomY = Math.min(startTerrainSideHeight, terrainCarriage1_bottom.getLocation().y + terrainSpeed);
					terrainCarriage1_bottom.setBounds(terrainCarriage1_bottom.getLocation().x, nextTerrainBottomY,
							terrainCarriage1_bottom.getSize().width, terrainCarriage1_bottom.getSize().height);
					terrainCarriage2_bottom.setBounds(terrainCarriage2_bottom.getLocation().x, nextTerrainBottomY,
							terrainCarriage2_bottom.getSize().width, terrainCarriage2_bottom.getSize().height);
					terrainCarriage3_bottom.setBounds(terrainCarriage3_bottom.getLocation().x, nextTerrainBottomY,
							terrainCarriage3_bottom.getSize().width, terrainCarriage3_bottom.getSize().height);
					
					int nextCharacterY = Math.min(startCharacterHeight, travelCharacterController.GetImagePanel().getLocation().y + terrainSpeed);
					travelCharacterController.GetImagePanel().setBounds(travelCharacterController.GetImagePanel().getLocation().x, nextCharacterY,
							travelCharacterController.GetImagePanel().getSize().width, travelCharacterController.GetImagePanel().getSize().height);
					
					if(nextLeftX == startFadeEdge_left
							&& nextRightX == startFadeEdge_right
							&& nextSettlementY == startSettlementHeight
							&& nextTerrainY == startTerrainHeight
							&& nextTerrainBottomY == startTerrainSideHeight
							&& nextCharacterY == startCharacterHeight) {
						CompleteExitAnim();
						exitAnim.stop();
					}
				}
			});
			exitAnim.setInitialDelay(1200);
			
			travelTimer = new Timer(travelInterval, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					Point locSettlement = terrainCarriage1_settlement.getLocation();
					Point newLocSettlement = new Point(locSettlement.x - travelPixelsPerFrame, locSettlement.y);
					terrainCarriage1_settlement.setLocation(newLocSettlement);
					Point locTop = terrainCarriage1_top.getLocation();
					Point newLocTop = new Point(locTop.x - travelPixelsPerFrame, locTop.y);
					terrainCarriage1_top.setLocation(newLocTop);
					Point locBottom = terrainCarriage1_bottom.getLocation();
					Point newLocBottom = new Point(locBottom.x - travelPixelsPerFrame, locBottom.y);
					terrainCarriage1_bottom.setLocation(newLocBottom);
					
					Point locSettlement2 = terrainCarriage2_settlement.getLocation();
					Point newLocSettlement2 = new Point(locSettlement2.x - travelPixelsPerFrame, locSettlement2.y);
					terrainCarriage2_settlement.setLocation(newLocSettlement2);
					Point locTop2 = terrainCarriage2_top.getLocation();
					Point newLocTop2 = new Point(locTop2.x - travelPixelsPerFrame, locTop2.y);
					terrainCarriage2_top.setLocation(newLocTop2);
					Point locBottom2 = terrainCarriage2_bottom.getLocation();
					Point newLocBottom2 = new Point(locBottom2.x - travelPixelsPerFrame, locBottom2.y);
					terrainCarriage2_bottom.setLocation(newLocBottom2);
					
					Point locSettlement3 = terrainCarriage3_settlement.getLocation();
					Point newLocSettlement3 = new Point(locSettlement3.x - travelPixelsPerFrame, locSettlement3.y);
					terrainCarriage3_settlement.setLocation(newLocSettlement3);
					Point locTop3 = terrainCarriage3_top.getLocation();
					Point newLocTop3 = new Point(locTop3.x - travelPixelsPerFrame, locTop3.y);
					terrainCarriage3_top.setLocation(newLocTop3);
					Point locBottom3 = terrainCarriage3_bottom.getLocation();
					Point newLocBottom3 = new Point(locBottom3.x - travelPixelsPerFrame, locBottom3.y);
					terrainCarriage3_bottom.setLocation(newLocBottom3);

					int activeCarriageX = 0;
					switch(activeCarriage) {
						case 1:
							activeCarriageX = newLocTop.x;
							break;
						case 2:
							activeCarriageX = newLocTop2.x;
							break;
						case 3:
							activeCarriageX = newLocTop3.x;
							break;
						default:
							System.err.println("WorldmapPanel - travelTimer Listener needs to add support for activeCarriage: " + activeCarriage);
							break;
					}
					
					int centerPoint = (dimension.width / 2) - (fullTileDimension.width / 2);
					int carriageStackCount = newLocTop.x < centerPoint ? 1 : 0;
					carriageStackCount += newLocTop2.x < centerPoint ? 1 : 0;
					carriageStackCount += newLocTop3.x < centerPoint ? 1 : 0;
					
					int carriageRelativeIndex = (carriageStackCount - 1) * 2 + (reachedIndex % 2) + 1;
					int nextX = dimension.width / 2 - (fullTileDimension.width / 2) - (fullTileDimension.width * carriageRelativeIndex);
					
					float travelNormPercent = 1f - ((activeCarriageX - nextX) / (float)fullTileDimension.width);
					int transStamina = partyStamina - (int)Math.floor(travelInfos.get(reachedIndex).staminaCost * travelNormPercent);
					//System.out.println("activeCarriageX: " + activeCarriageX +" - nextX: "+ nextX +" / width: "+ fullTileDimension.width + " = travelNormPercent: " + travelNormPercent + ", transStamina: " + transStamina);
					travelPane.SetBar((float)transStamina / partyStaminaMax);
					
					if(activeCarriageX <= nextX) {
						
						//System.out.println("activeCarriageX:" + activeCarriageX + " > nextX:" + nextX + ". Reached Next, carriageRelativeIndex: " + carriageRelativeIndex);
						
						ReachedNextTile();
						if(carriageRelativeIndex == 4) {
						
							//System.out.println(">>__ ROTATED CARRIAGE __>>");
							
							SetupNextCarriage(activeCarriage, reachedIndex + 2, false);
							
							activeCarriage++;
							if(activeCarriage > 3)
								activeCarriage = 1;
						}
					} else {
						//System.out.println("activeCarriageX:" + activeCarriageX + " > nextX:" + nextX + " ... activeCarriage: " + activeCarriage + ", carriageStackCount: " + carriageStackCount + ", reachedIndex: " + reachedIndex + " => carriageRelativeIndex: " + carriageRelativeIndex);
					}
				}
			});
			travelTimer.setInitialDelay(0);
			
			SetComponentsVisible(false);
		}
		
		int activeCarriage;
		boolean isFadingIn;
		List<WorldTile> pathTiles = new ArrayList<WorldTile>();
		//the travelInfos list is parallel to the pathTiles so index 0 is the info from pathTiles index 0 to index 1, etc
		//There will always be one less travelInfos than pathTiles
		List<TravelInfo> travelInfos = new ArrayList<TravelInfo>();
		public void StartTravelAnim(List<WorldTile> pathTiles, List<TravelInfo> travelInfos) {
			isAnimating = true;
			
			isFadingIn = true;
			reachedIndex = 0;
			activeCarriage = 1;
			
			hasFadeFinished = false;
			hasExitAnimFinished = false;
			this.pathTiles.clear();
			this.pathTiles.addAll(pathTiles);
			this.travelInfos.clear();
			this.travelInfos.addAll(travelInfos);
			
			fadeOutOverlay_left.setVisible(true);
			fadeOutOverlay_right.setVisible(true);
			
			if(pathTiles.get(0).GetSettlementType() != null) {
				c1_settlementIcon1.SetNewImage(GUIUtil.GetSettlementImage(pathTiles.get(0).GetSettlementType(), pathTiles.get(0).GetMapLocation().getSettlementDesignation()));
				c1_settlementIcon1.setVisible(true);
			} else
				c1_settlementIcon1.setVisible(false);
			if(pathTiles.get(1).GetSettlementType() != null) {
				c1_settlementIcon2.SetNewImage(GUIUtil.GetSettlementImage(pathTiles.get(1).GetSettlementType(), pathTiles.get(1).GetSettlementDesignation()));
				c1_settlementIcon2.setVisible(true);
			} else
				c1_settlementIcon2.setVisible(false);
			c1_terrainTile1.SetNewImage(SpriteSheetUtility.GetTerrainFromWorldTile(pathTiles.get(0).getTileType())[pathTiles.get(0).getTerrainImageIndex()]);
			c1_terrainTile1.setVisible(true);
			c1_terrainTile2.SetNewImage(SpriteSheetUtility.GetTerrainFromWorldTile(pathTiles.get(1).getTileType())[pathTiles.get(1).getTerrainImageIndex()]);
			c1_terrainTile2.setVisible(true);
			c1_terrainSide1.SetNewImage(pathTiles.get(0).getTileType() == WorldTileType.water ? SpriteSheetUtility.getTerrainUnderWater() : SpriteSheetUtility.getTerrainUnderGround());
			c1_terrainSide1.setVisible(true);
			c1_terrainSide2.SetNewImage(pathTiles.get(1).getTileType() == WorldTileType.water ? SpriteSheetUtility.getTerrainUnderWater() : SpriteSheetUtility.getTerrainUnderGround());
			c1_terrainSide2.setVisible(true);
			
			terrainCarriage1_settlement.setLocation(dimension.width / 2 - (fullTileDimension.width / 2), terrainCarriage1_settlement.getLocation().y);
			terrainCarriage1_top.setLocation(dimension.width / 2 - (fullTileDimension.width / 2), terrainCarriage1_top.getLocation().y);
			terrainCarriage1_bottom.setLocation(dimension.width / 2 - (fullTileDimension.width / 2), terrainCarriage1_bottom.getLocation().y);
			
			SetupNextCarriage(2, 2, true);
			SetupNextCarriage(3, 4, true);
			
			travelCharacterController.GetImagePanel().setVisible(true);
			//travelCharacterController.ChangeDirection(true);
			travelCharacterController.PlayAnim(AnimType.Idle, true, false);
			
			enterAnim.start();
			
			lightbox.Fade(true, 1400, timerDelay, alphaInterval, 10, this);
		}
		
		public boolean IsAnimating() {
			return isAnimating;
		}
		
		private void SetupNextCarriage(int targetCarriage, int travelIndex, boolean isSetup) {
			JPanel terrainCarriage_settlement = null;
			JPanel terrainCarriage_top = null;
			JPanel terrainCarriage_bottom = null;
			ImagePanel settlementIcon1 = null;
			ImagePanel terrainTile1 = null;
			ImagePanel terrainSide1 = null;
			ImagePanel settlementIcon2 = null;
			ImagePanel terrainTile2 = null;
			ImagePanel terrainSide2 = null;
			switch(targetCarriage) {
				case 1:
					terrainCarriage_settlement = terrainCarriage1_settlement;
					terrainCarriage_top = terrainCarriage1_top;
					terrainCarriage_bottom = terrainCarriage1_bottom;
					settlementIcon1 = c1_settlementIcon1;
					terrainTile1 = c1_terrainTile1;
					terrainSide1 = c1_terrainSide1;
					settlementIcon2 = c1_settlementIcon2;
					terrainTile2 = c1_terrainTile2;
					terrainSide2 = c1_terrainSide2;
					break;
				case 2:
					terrainCarriage_settlement = terrainCarriage2_settlement;
					terrainCarriage_top = terrainCarriage2_top;
					terrainCarriage_bottom = terrainCarriage2_bottom;
					settlementIcon1 = c2_settlementIcon1;
					terrainTile1 = c2_terrainTile1;
					terrainSide1 = c2_terrainSide1;
					settlementIcon2 = c2_settlementIcon2;
					terrainTile2 = c2_terrainTile2;
					terrainSide2 = c2_terrainSide2;
					break;
				case 3:
					terrainCarriage_settlement = terrainCarriage3_settlement;
					terrainCarriage_top = terrainCarriage3_top;
					terrainCarriage_bottom = terrainCarriage3_bottom;
					settlementIcon1 = c3_settlementIcon1;
					terrainTile1 = c3_terrainTile1;
					terrainSide1 = c3_terrainSide1;
					settlementIcon2 = c3_settlementIcon2;
					terrainTile2 = c3_terrainTile2;
					terrainSide2 = c3_terrainSide2;
					break;
				default:
					System.err.println("WorldmapPanel - travelTimer Listener needs to add support for activeCarriage: " + activeCarriage);
					break;
			}
			
			int remainingTileCount = pathTiles.size() - travelIndex;
			if(remainingTileCount > 0) {
				if(pathTiles.get(travelIndex).GetSettlementType() != null) {
					settlementIcon1.SetNewImage(GUIUtil.GetSettlementImage(pathTiles.get(travelIndex).GetSettlementType(), pathTiles.get(travelIndex).GetSettlementDesignation()));
					settlementIcon1.setVisible(true);
				} else
					settlementIcon1.setVisible(false);
				terrainTile1.SetNewImage(SpriteSheetUtility.GetTerrainFromWorldTile(pathTiles.get(travelIndex).getTileType())[pathTiles.get(travelIndex).getTerrainImageIndex()]);
				terrainTile1.setVisible(true);
				terrainSide1.SetNewImage(pathTiles.get(travelIndex).getTileType() == WorldTileType.water ? SpriteSheetUtility.getTerrainUnderWater() : SpriteSheetUtility.getTerrainUnderGround());
				terrainSide1.setVisible(true);
				if(remainingTileCount > 1) {
					int nextTravelIndex = travelIndex + 1;
					if(pathTiles.get(nextTravelIndex).GetSettlementType() != null) {
						settlementIcon2.SetNewImage(GUIUtil.GetSettlementImage(pathTiles.get(nextTravelIndex).GetSettlementType(), pathTiles.get(nextTravelIndex).GetSettlementDesignation()));
						settlementIcon2.setVisible(true);
					} else
						settlementIcon2.setVisible(false);
					terrainTile2.SetNewImage(SpriteSheetUtility.GetTerrainFromWorldTile(pathTiles.get(nextTravelIndex).getTileType())[pathTiles.get(nextTravelIndex).getTerrainImageIndex()]);
					terrainTile2.setVisible(true);
					terrainSide2.SetNewImage(pathTiles.get(nextTravelIndex).getTileType() == WorldTileType.water ? SpriteSheetUtility.getTerrainUnderWater() : SpriteSheetUtility.getTerrainUnderGround());
					terrainSide2.setVisible(true);
				} else {
					settlementIcon2.setVisible(false);
					terrainTile2.setVisible(false);
					terrainSide2.setVisible(false);
				}
			} else {
				settlementIcon1.setVisible(false);
				terrainTile1.setVisible(false);
				terrainSide1.setVisible(false);
				settlementIcon2.setVisible(false);
				terrainTile2.setVisible(false);
				terrainSide2.setVisible(false);
			}
			int spaceMuliplier = isSetup ? (travelIndex <= 2 ? 2 : 4) : (2);
			terrainCarriage_settlement.setLocation(dimension.width / 2 - (fullTileDimension.width / 2) + (fullTileDimension.width * spaceMuliplier), terrainCarriage_settlement.getLocation().y);
			terrainCarriage_top.setLocation(dimension.width / 2 - (fullTileDimension.width / 2) + (fullTileDimension.width * spaceMuliplier), terrainCarriage_top.getLocation().y);
			terrainCarriage_bottom.setLocation(dimension.width / 2 - (fullTileDimension.width / 2) + (fullTileDimension.width * spaceMuliplier), terrainCarriage_bottom.getLocation().y);
		}
		
		int reachedIndex = 0;
		//Called everytime the player reaches the center of a new tile during the anim
		private void ReachedNextTile() {
			partyStamina -= travelInfos.get(reachedIndex).staminaCost;
			
			//Index it before all uses, not just the conditional
			reachedIndex++;
			
			WorldTile reachedTile = pathTiles.get(reachedIndex);
			System.out.println("ReachedNextTile() - " + reachedTile.getPosition().toString());

			playerSprite.Move(reachedTile.getPosition(), true);
			
			reachedTile.Discover();
			
			//reachedIndex++;
			//if(reachedIndex == pathTiles.size()) {
			if(reachedIndex == pathTiles.size() - 1) {
				travelTimer.stop();
				EndTravelAnim();
			}
		}
		
		//Called at the end of the anim
		private void EndTravelAnim() {
			isFadingIn = false;
			travelCharacterController.PlayAnim(AnimType.Idle, true, false);
			exitAnim.start();
			lightbox.Fade(false, 1000, timerDelay, alphaInterval, 10, this);
		}
		
		@Override
		public void TransitionComplete() {
			if(isFadingIn) {
				SetComponentsVisible(true);
				travelTimer.start();
				travelCharacterController.PlayAnim(AnimType.Walk, true, false);
			} else {
				hasFadeFinished = true;
				if(hasExitAnimFinished) {
					System.out.println("Fade initiating FinalizeTransition");
					FinalizeTransition();
				}
			}
		}
		
		//Wait till both the exit anim and the fade anim are complete before ending the transition
		boolean hasFadeFinished;
		boolean hasExitAnimFinished;
		public void CompleteExitAnim() {
			hasExitAnimFinished = true;
			if(hasFadeFinished) {
				System.out.println("Exit Anim initiating FinalizeTransition");
				FinalizeTransition();
			}
		}
		
		private void FinalizeTransition() {
			isAnimating = false;
			
			SetComponentsVisible(false);
			
			Game.Instance().SetPartyStamina(partyStamina);
			//Clear our path data, now that we're done traveling it, so that we're ready to build a new path
			ResetTravelPath();
			
			//set cursor on newly occupied tile for ease of entry
			UpdateLocationLabel(worldMap.get(playerSprite.GetWorldLocation()));
			SelectEnterableTile(playerSprite.GetWorldLocation());
		}
		
		private void SetComponentsVisible(boolean visible) {
			//Only hide them when ordered, showing them is a conditional matter handled solely by StartTravelAnim
			if(!visible) {
				c1_settlementIcon1.setVisible(false);
				c1_terrainTile1.setVisible(false);
				c1_terrainSide1.setVisible(false);
				c1_settlementIcon2.setVisible(false);
				c1_terrainTile2.setVisible(false);
				c1_terrainSide2.setVisible(false);
				c2_settlementIcon1.setVisible(false);
				c2_terrainTile1.setVisible(false);
				c2_terrainSide1.setVisible(false);
				c2_settlementIcon2.setVisible(false);
				c2_terrainTile2.setVisible(false);
				c2_terrainSide2.setVisible(false);
				c3_settlementIcon1.setVisible(false);
				c3_terrainTile1.setVisible(false);
				c3_terrainSide1.setVisible(false);
				c3_settlementIcon2.setVisible(false);
				c3_terrainTile2.setVisible(false);
				c3_terrainSide2.setVisible(false);
			}
			
			travelCharacterController.GetImagePanel().setVisible(visible);
			//emote.setVisible(visible);
			//reactButton1.setVisible(visible);
			//reactButton2.setVisible(visible);
			//reactButton3.setVisible(visible);
			fadeOutOverlay_left.setVisible(visible);
			fadeOutOverlay_right.setVisible(visible);
		}
	}
	TravelAnimPane travelAnimPane;
	
	public void MoveToNewTile(String missionId) {
		Point2D newWorldLocation = null;
		
		//This used to work when a mission was considered a settlement
		/*for(WorldTile settledTile : settledWorldTiles) {
			if(settledTile.GetMapLocation().GetMissionIds().size() > 0 && settledTile.GetMapLocation().GetMissionIds().contains(missionId)) {
				newWorldLocation = settledTile.getPosition();
				break;
			}
		}*/
		for(WorldTile missionTile : this.missionIndicatedWorldTiles) {
			if(missionTile.GetMapLocation().GetMissionIds().size() > 0 && missionTile.GetMapLocation().GetMissionIds().contains(missionId)) {
				newWorldLocation = missionTile.getPosition();
				break;
			}
		}
		
		
		if(newWorldLocation == null) {
			System.err.println("WorldmapPanel.MoveToNewTile(String missionId) - Couldn't find a WorldTile with a MissionId matching: " + missionId + ". Returning early without moving.");
			return;
		}
		
		DoMoveToNewTile(newWorldLocation);
	}
	public void MoveToNewTile(MapLocation mapLocation) {
		//use the mapLocation to trace back to a Mission owned MapLocation or one from Missions.staticMapLocations
		Point2D newWorldLocation = null;
		
		/*for(WorldTile occupiedTile : occupiedWorldTiles) {
			System.out.println("WorldmapPanel.MoveToNewTile() - checking occupiedTile: " + occupiedTile.mapLocation.getName());
			if(occupiedTile.HasMissionOrStaticMapLocation() &&
				//(occupiedTile.GetMapLocation().getId().equals(mapLocation.getId()) || (occupiedTile.GetMission() != null && occupiedTile.GetMission().getMapLocation().getId().equals(mapLocation.getId())))
				occupiedTile.GetMapLocation().getId().equals(mapLocation.getId())
			) {
				newWorldLocation = occupiedTile.getPosition();
				break;
			}
		}*/
		newWorldLocation = mapLocation.getWorldTileData().position;
		
		if(newWorldLocation == null) {
			System.err.println("WorldmapPanel.MoveToNewTile(MapLocation mapLocation) - Couldn't find a MapLocation matching: " + mapLocation.getName() + ". Returning");
			return;
		}
		
		DoMoveToNewTile(newWorldLocation);
	}
	private void DoMoveToNewTile(Point2D newWorldLocation) {
		//move the player sprite
		playerSprite.Move(newWorldLocation, true);
		//set cursor on newly occupied tile for ease of entry
		SelectEnterableTile(newWorldLocation);
		//Update tile label
		WorldTile worldTile = worldMap.get(newWorldLocation);
		UpdateLocationLabel(worldTile);
		
		worldTile.Discover();
	}
	
	
	private void ResetTravelPath() {
		System.out.println("WorldmapPanel.ResetTravelPath()");
		travelTiles.clear();
		travelInfos.clear();
		projectedStamina = partyStamina;
		for(int i = 0; i < pathNodeSprites.length; i++)
			pathNodeSprites[i].GetImagePanel().setVisible(false);
		travelPane.ResetCostBar();
		
		//this.enterButton.setVisible(false);
		//this.enterButtonBg.setVisible(false);
		HideLocationLabelPanel();
	}
	
	//World Generation - End
	
	
	//Called after the player has finished a move to another new tile
	public void RefreshPaths() {
		//Set the currentWorldPosition and load the SceneData for 
		Game.Instance().SetWorldmapLocation(playerSprite.GetWorldLocation(), MapLocationPanel.GetCurrentLocation());
		
		List<Point2D> adjacentPoints = this.GetNeighborPoints_PURE(worldMap.get(playerSprite.GetWorldLocation()),
																	Game.Instance().GetWorldmapData().worldGridDimension.width, Game.Instance().GetWorldmapData().worldGridDimension.height);
		adjacentWorldTiles.clear();
		for(Point2D point : adjacentPoints)
			adjacentWorldTiles.add(worldMap.get(point));
		System.out.println("WorldmapPanel.RefreshPaths() - adjacentWorldTiles.size(): " + adjacentWorldTiles.size());
	}
	
	private List<WorldTile> GetAdjacentTiles(WorldTile sourceTile) {
		List<WorldTile> tiles = new ArrayList<WorldTile>();
		List<Point2D> adjacentPoints = this.GetNeighborPoints_PURE(sourceTile, Game.Instance().GetWorldmapData().worldGridDimension.width, Game.Instance().GetWorldmapData().worldGridDimension.height);
		for(Point2D point : adjacentPoints)
			tiles.add(worldMap.get(point));
		return tiles;
	}
	
	public MapLocation RandomAdjacentTile() {
		int xDir = r.nextBoolean() ? -1 : 1;
		int[] dirs = new int[] { -1, 0, 1 };
		int yDir = dirs[r.nextInt(3)];
		Point2D pointInDirection = GetHexInDirection(playerSprite.GetWorldLocation(), rectToHexMatrix.get(new Point(xDir, yDir)));
		WorldTile worldTile = worldMap.get(pointInDirection);
		return worldTile.GetMapLocation();
	}
	
	//__ Sprites __\\
	
	public class MapSprite {
		public MapSprite(BufferedImage buffImage, Point2D worldLocation, Dimension imagePanelSize, Point2D tileNormOffset, boolean instantiateVisible) {
			ImagePanel imagePanel = new ImagePanel(buffImage);
			imagePanel.setOpaque(false);
			imagePanel.setBackground(new Color(0,0,0,0));
			
			this.worldLocation = worldLocation;
			this.imagePanel = imagePanel;
			this.imagePanel.setSize(imagePanelSize);
			this.imagePanel.setPreferredSize(imagePanelSize);

			if(worldLocation != null) {
				terrainLayersScaleListener.AddChild(imagePanel);
				Move(worldLocation, instantiateVisible);
				this.tileNormOffset = tileNormOffset;
				
				//Trying to have this image maintain its location after scaling
				imagePanel.addComponentListener(new ComponentListener() {
					@Override
					public void componentHidden(ComponentEvent arg0) {}
					@Override
					public void componentMoved(ComponentEvent arg0) {}
					@Override
					public void componentResized(ComponentEvent arg0) {
						DoMove(true);
					}
					@Override
					public void componentShown(ComponentEvent arg0) {}
				});
			}
		}
		
		private Point2D worldLocation;
		public Point2D GetWorldLocation() { return worldLocation; }
		protected Point2D tileNormOffset;
		private boolean setVisbleAfterMoveFlag;
		
		public void Move(Point2D newWorldLocation, boolean setVisble) {
			//set this false the first time we move it from outside the class
			setVisbleAfterMoveFlag = setVisble;
			
			worldLocationDestination = newWorldLocation;
			SwingUtilities.invokeLater(Move_Later);
		}
		
		public void FlagInvisible() {
			setVisbleAfterMoveFlag = false;
			imagePanel.setVisible(false);
		}
		
		Point2D worldLocationDestination;
		Runnable Move_Later = new Runnable() {
			@Override
			public void run() {
				DoMove(false);
			}
		};
		
		protected void DoMove(boolean ignoreVisibleFlagForZoomEvents) {
			Point locationOnTerrainLayers = GetLocationOnTerrainLayers(worldLocationDestination);	
			
			//System.out.println("Set Size and Bounds to locationOnTerrainLayers: " + locationOnTerrainLayers);
			Dimension size = imagePanel.getSize();
			Point offset = new Point((int)Math.round(size.width * tileNormOffset.getX()), (int)Math.round(size.height * tileNormOffset.getY()));
			imagePanel.setBounds(locationOnTerrainLayers.x + offset.x, locationOnTerrainLayers.y + offset.y, size.width, size.height);
			
			worldLocation = worldLocationDestination;
			
			if(!ignoreVisibleFlagForZoomEvents)
				imagePanel.setVisible(setVisbleAfterMoveFlag);
		}
		
		protected ImagePanel imagePanel;
		public ImagePanel GetImagePanel() { return imagePanel; }
	}
	private MapSprite cursorSprite;
	private MapSprite tileSelectionSprite;
	
	
	private Point GetLocationOnTerrainLayers(Point2D worldmapLocation) {
		Point locationOnTerrainLayers = new Point();
		Component currComponent = worldMap.get(worldmapLocation).getSettlementComponent();
		while ( currComponent != null && currComponent != terrainLayers ) {
		    Point relativeLocation = currComponent.getLocation();
		    locationOnTerrainLayers.translate( relativeLocation.x, relativeLocation.y );
		    currComponent = currComponent.getParent();
		}
		return locationOnTerrainLayers;
	}
	
	public class VectorSprite extends MapSprite {
		public VectorSprite(BufferedImage buffImage, Point2D worldLocation, Dimension imagePanelSize, Point2D tileNormOffset, Point vector, int timerDelay, JComponent boundingContainer) {
			super(vector.x > 0 ? GUIUtil.Mirror(buffImage) : buffImage, worldLocation, imagePanelSize, tileNormOffset, false);
			
			imagePanel.removeComponentListener(imagePanel.getComponentListeners()[0]);
			imagePanel.addComponentListener(new ComponentListener() {
				@Override
				public void componentHidden(ComponentEvent arg0) {}
				@Override
				public void componentMoved(ComponentEvent arg0) {}
				@Override
				public void componentResized(ComponentEvent arg0) {
					ZoomShift();
				}
				@Override
				public void componentShown(ComponentEvent arg0) {}
			});
			
			this.vector = vector;
			this.boundingContainer = boundingContainer;
			timer = new Timer(timerDelay, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					MoveFreeform(Add(imagePanel.getLocation(), vector));
				}
			});
			timer.start();
		}
		
		private boolean disableZoomInteractions;
		public boolean DisableZoomInteractions() { return disableZoomInteractions; }
		private Dimension boundingDimension;
		//An alternate use intented for installment in static boundingContainers
		public VectorSprite(BufferedImage buffImage, Point localLocation, Dimension imagePanelSize, Point vector, int timerDelay, Dimension boundingDimension) {
			super(vector.x > 0 ? GUIUtil.Mirror(buffImage) : buffImage, null, imagePanelSize, null, false);
			
			imagePanel.setBounds(localLocation.x, localLocation.y, imagePanel.getSize().width, imagePanel.getSize().height);
			globalLocation = new Point2D.Float((float)localLocation.x, (float)localLocation.y);
			disableZoomInteractions = true;
			
			this.vector = vector;
			this.boundingDimension = boundingDimension;
			timer = new Timer(timerDelay, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					MoveFreeform(Add(imagePanel.getLocation(), vector));
				}
			});
			timer.start();
		}
		
		private Point vector;
		private JComponent boundingContainer;
		
		private Timer timer;
		Point2D globalDestination;
		Point2D globalLocation;
		
		private void MoveFreeform(Point2D globalLocation) {
			globalDestination = globalLocation;
			SwingUtilities.invokeLater(MoveFreeform_Later);
		}
		Runnable MoveFreeform_Later = new Runnable() {
			@Override
			public void run() {
				DoMoveFreeform();
			}
		};
		
		int yReset_frameBuffer = 20;
		int currentFrameBuffer;
		private void DoMoveFreeform() {
			//System.out.println("Set Size and Bounds to locationOnTerrainLayers: " + locationOnTerrainLayers);
			Dimension size = imagePanel.getSize();
			Point offset = new Point(0,0);
			if(tileNormOffset != null)
				offset = new Point((int)Math.round(size.width * tileNormOffset.getX()), (int)Math.round(size.height * tileNormOffset.getY()));
			Point newLoc = new Point((int)globalDestination.getX() + offset.x, (int)globalDestination.getY() + offset.y);
			//Check our location against the boundingContainer and determine if we need to loop our position to the opposite edge of the container
			if(newLoc.x > boundingDimension.width && vector.x > 0) {
				newLoc.x = -size.width;
				newLoc.y = r.nextInt(boundingDimension.height-size.height);
				currentFrameBuffer = 0;
			} else if(newLoc.x < -size.width && vector.x < 0) {
				newLoc.x = boundingDimension.width;
				newLoc.y = r.nextInt(boundingDimension.height-size.height);
				currentFrameBuffer = 0;
			}
			
			if(currentFrameBuffer <= 0) {
				if(newLoc.y < -size.height || newLoc.y > boundingDimension.height) {
					newLoc.x = -size.width + r.nextInt(boundingDimension.width);
					newLoc.y = newLoc.y < -size.height ? boundingDimension.height : -size.height;
					currentFrameBuffer = yReset_frameBuffer;
				}
			}
			currentFrameBuffer--;
			
			imagePanel.setBounds(newLoc.x, newLoc.y, size.width, size.height);
			
			globalLocation = globalDestination;
		}
		
		protected void ZoomShift() {
			//System.out.println("Set Size and Bounds to locationOnTerrainLayers: " + locationOnTerrainLayers);
			Point2D normLocOnTerrainLayers = new Point2D.Float((float)globalLocation.getX() / terrainLayersSizeBeforeZoom.width, (float)globalLocation.getY() / terrainLayersSizeBeforeZoom.height);
			//if(this == cloudSprites.get(0))
			//	System.out.println("ZoomShift() - normLocOnTerrainLayers BEFORE MOVE: " + normLocOnTerrainLayers);
			Dimension size = imagePanel.getSize();
			Point offset = new Point((int)Math.round(size.width * tileNormOffset.getX()), (int)Math.round(size.height * tileNormOffset.getY()));
			Point newLocation = new Point(
					(int)Math.round(terrainLayers.getSize().width * normLocOnTerrainLayers.getX()) + offset.x,
					(int)Math.round(terrainLayers.getSize().height * normLocOnTerrainLayers.getY()) + offset.y
					);
			//if(this == cloudSprites.get(0))
			//	System.out.println("ZoomShift() - terrainLayers: " + terrainLayers.getSize() + ", cloud newLocation: " + newLocation);
			imagePanel.setBounds(newLocation.x, newLocation.y, size.width, size.height);
			
			globalLocation = new Point2D.Float((float)newLocation.x, (float)newLocation.y);
			globalDestination = globalLocation;
		}
	}
	private List<VectorSprite> cloudSprites = new ArrayList<VectorSprite>();
	
	
	private ImagePanel CreatePlayerSprite(Point2D worldLocation, BufferedImage buffImage, Dimension imagePanelSize, SpriteSheet spriteSheet, Point2D tileNormOffset) {
		ImagePanel playerImagePanel = new ImagePanel(buffImage);
		playerSprite = new PlayerSprite(worldLocation, playerImagePanel, imagePanelSize, spriteSheet, tileNormOffset);
		playerImagePanel.setOpaque(false);
		playerImagePanel.setBackground(new Color(0,0,0,0));
		terrainLayersScaleListener.AddChild(playerImagePanel);
		//playerImagePanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
		
		//Since placing the player at their first location isnt a move action, technically, we need to discover this area
		worldMap.get(worldLocation).Discover();
		
		return playerImagePanel;
	}
	
	public class PlayerSprite {
		public PlayerSprite(Point2D worldLocation, ImagePanel imagePanel, Dimension imagePanelSize, SpriteSheet spriteSheet, Point2D tileNormOffset) {
			this.worldLocation = worldLocation;
			this.imagePanel = imagePanel;
			this.imagePanel.setSize(imagePanelSize);
			this.imagePanel.setPreferredSize(imagePanelSize);
			Move(worldLocation);
			this.spriteSheet = spriteSheet;
			this.tileNormOffset = tileNormOffset;
			
			//Trying to have this image maintain its location after scaling
			imagePanel.addComponentListener(new ComponentListener() {
				@Override
				public void componentHidden(ComponentEvent arg0) {}
				@Override
				public void componentMoved(ComponentEvent arg0) {}
				@Override
				public void componentResized(ComponentEvent arg0) {
					DoMove();
				}
				@Override
				public void componentShown(ComponentEvent arg0) {}
			});
		}
		
		private Point2D worldLocation;
		public Point2D GetWorldLocation() { return worldLocation; }
		Point2D tileNormOffset;
		
		public void Move(Point2D newWorldLocation) {
			worldLocationDestination = newWorldLocation;
			SwingUtilities.invokeLater(Move_Later);
		}
		
		boolean centerMapAfterMove;
		public void Move(Point2D newWorldLocation, boolean centerMapAfterMove) {
			this.centerMapAfterMove = centerMapAfterMove;
			Move(newWorldLocation);
		}
		
		Point2D worldLocationDestination;
		Runnable Move_Later = new Runnable() {
			@Override
			public void run() {
				DoMove();
			}
		};
		
		private void DoMove() {
			Point locationOnTerrainLayers = GetLocationOnTerrainLayers(worldLocationDestination);
			
			//System.out.println("Set Size and Bounds to locationOnTerrainLayers: " + locationOnTerrainLayers);
			Dimension size = imagePanel.getSize();
			Point offset = new Point((int)Math.round(size.width * tileNormOffset.getX()), (int)Math.round(size.height * tileNormOffset.getY()));
			imagePanel.setBounds(locationOnTerrainLayers.x + offset.x, locationOnTerrainLayers.y + offset.y, size.width, size.height);
			
			//This method gets called to reposition the playerSprite during zooming events so ignore this stuff if thats the case
			if(worldLocation != worldLocationDestination) {
				//Put on a timer and animate running and movement eventually
				
				worldLocation = worldLocationDestination;
				mapLocationPanel.OnEnterLocation(worldMap.get(worldLocation).GetMapLocation());
			}
			
			if(centerMapAfterMove) {
				centerMapAfterMove = false;
				FocusViewOnTile(worldLocation);
			}
		}
		
		private ImagePanel imagePanel;
		private SpriteSheet spriteSheet;
	}
	private PlayerSprite playerSprite;
	
	public Point2D GetPlayerSpriteLocation() {
		return playerSprite.GetWorldLocation();
	}
}
