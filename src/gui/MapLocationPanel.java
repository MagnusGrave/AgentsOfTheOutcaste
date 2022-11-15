package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import gui.BattlePanel.CharacterSocket;
import gui.WorldmapPanel.WorldTile;
import data.BattleState;
import data.CharacterData;
import data.GraphPathNode;
import data.InteractionManager;
import data.InteractionManager.InteractionHandler;
import data.InteractionState;
import data.ItemData;
import data.SceneData;
import data.SceneData.Breakaway;
import data.SceneData.Breakaway.CoordinateData;
import data.SceneData.Row;
import data.SceneData.Row.ImageLayer;
import data.SceneData.VisualTileData;
import data.UnresolvedInteractionData;
import data.WorldTileData;

import dataShared.DialogLine;
import dataShared.DialographyData;
import dataShared.ActorData;
import dataShared.ActorPathData;
import enums.ColorBlend;
import enums.Direction;
import enums.InteractionType;
import enums.MenuType;
import enums.SceneLayeringType;
import gameLogic.Board;
import gameLogic.Game;
import gameLogic.Interaction;
import gameLogic.MapLocation;
import gameLogic.Mission;
import gameLogic.Mission.MissionStatusType;
import gameLogic.Missions;
import gameLogic.AbilityManager.Ability;
import gameLogic.Actor;
import gameLogic.Actor.RepeatingListener;


@SuppressWarnings("serial")
public class MapLocationPanel extends JPanel {
	WorldmapPanel worldmapPanel;
	
	//Contains the scene row images
	JLayeredPane scenePane;
	
	//Contains all the actors in the scene
	//JLayeredPane actorPane;
	
	//Current Map Location
	private JFxLabel locationTitle;
	private JTextArea descriptionTextArea;
	private JScrollPane locationDescription;
	private ImagePanel locationBG;
	private static MapLocation currentLocation;
	public static  MapLocation GetCurrentLocation() {
		return currentLocation;
	}
	boolean hasDisabledMovement;
	public boolean HasDisabledMovement() {
		return hasDisabledMovement;
	}
	
	//Interaction "Panel" Elements
	private CustomButtonUltra leaveButton;
	private ImagePanel leaveBG;
	private JLabel titleBG;
	private JLabel descBG;
	private JFxLabel interactTitle;
	private JPanel interactionOptions;
	private JLabel interactBG;
	
	//Interactions
	private HashMap<InteractionType, CustomButtonUltra> interactionButtonMap = new HashMap<InteractionType, CustomButtonUltra>();
	
	//Dialog
	private Random r = new Random();
	private int currentDialogFrame;
	private JFxLabel interactionDialogLabel;
	private JTextArea dialogText;
	private CustomButton dialogNextButton;
	private CustomButton dialogBackButton;
	private CustomButton proceedButton;
	private JLabel leftPortraitName;
	private JLabel rightPortraitName;
	private ImagePanel leftPortrait;
	private ImagePanel rightPortrait;
	private JLabel dialogBG;
	private int lastIndex;
	private boolean hasInteractionBeenApplied;
	private boolean wasInteractionSuccessful;
	
	JPanel battlePanelContainer;
	
	ImagePanel campfireLightImagePanel;
	//encapsulate this overlay in some kind of interface or a child class of the FadeTransitionPanel
	
	static Point sceneTopCornerLoc;
	static Dimension sceneSize;
	
	public enum SublayerType { Breakaway, DoorTile, StatusEffect, ActorOrProp, RowImage_Settlement, AnimatedTile_Settlement, RowImage_Nature, AnimatedTile_Nature };
	public class SublayerDepthManager {
		public SublayerDepthManager() {
			layerCountMap = new HashMap<SublayerType, Integer>();
			for(SublayerType type : SublayerType.values())
				layerCountMap.put(type, 0);
		}
		private Map<SublayerType, Integer> layerCountMap;
		
		public void TrackOccupantActivity(SublayerType sublayerType, boolean isJoiningSublayer) {
			int existingCount = layerCountMap.get(sublayerType);
			if(isJoiningSublayer)
				layerCountMap.put(sublayerType, ++existingCount);
			else
				layerCountMap.put(sublayerType, --existingCount);
		}
		
		public int GetStartIndexForSublayer(SublayerType sublayerType) {
			int cumulativeIndex = 0;
			for(int i = 0; i < SublayerType.values().length; i++) {
				SublayerType sequentialType = null;
				if(i == 0)
					sequentialType = SublayerType.Breakaway;
				else if(i == 1)
					sequentialType = SublayerType.DoorTile;
				else if(i == 2)
					sequentialType = SublayerType.StatusEffect;
				else if(i == 3)
					sequentialType = SublayerType.ActorOrProp;
				else if(i == 4)
					sequentialType = SublayerType.RowImage_Settlement;
				else if(i == 5)
					sequentialType = SublayerType.AnimatedTile_Settlement;
				else if(i == 6)
					sequentialType = SublayerType.RowImage_Nature;
				else if(i == 7)
					sequentialType = SublayerType.AnimatedTile_Nature;
				else
					System.err.println("MapLocationPanel.SceneDepthManager.GetStartIndexForSublayer() - Add support for all SublayerTypes!");
				
				//We've reached the start of the target SublayerType
				if(sequentialType == sublayerType)
					break;
				
				cumulativeIndex += layerCountMap.get(sequentialType);
			}
			
			return cumulativeIndex;
		}
	}
	//every non-blank row/layeredPane layer needs a sceneDepthManager
	List<SublayerDepthManager> sublayerDepthManagers = new ArrayList<SublayerDepthManager>();
	
	private Mission activeMission;
	public Mission GetActiveMission() {
		return activeMission;
	}
	
	DialographyData currentDialography;
	
	int defaultSceneLocY;
	int dialogSceneLocY = GUIUtil.GetRelativePoint(0f, 0.04f).y;
	
	/**
	 * This stands in place of any direct references to currentLocation.getInteractions() or activeMission.getInteractions().
	 * Its value is set during OnEnterLocation()
	 */
	InteractionManager currentInteractionManager;
	
	//when going forward thru index less than or equal to this int then ignore anim interactions, when moving past it then set anim notifications
    private int forwardProgress;
	
	boolean isLockedByActor;
	
	List<Actor> animatingActors = new ArrayList<Actor>();
	
	//Create all timers for referencing
	Timer campfireTimer = null;
	
	//Interaction Graph
	//This array is a remnant of older interaction logic
	//private Interaction[] currentLayerInteractions;
	private Interaction currentInteraction;
	private int interactionLayerDepth;
	
	
	/*public class InteractionHandler {
		public InteractionHandler(Interaction intr, InteractionState state, int layerDepth, InteractionType type, int elementIndex) {
			this.intr = intr;
			this.state = state;
			this.layerDepth = layerDepth;
			this.type = type;
			this.elementIndex = elementIndex;
		}
		Interaction intr;
		InteractionState state;
		//layer-based address
		int layerDepth; //serves both addresses
		InteractionType type;
		//jagged array address
		int elementIndex;
	}
	List<InteractionHandler> interactionHandlers = new ArrayList<InteractionHandler>();*/
	
	/**
	 * The interval between Updates. Use this consistent value instead of JVM's deltatime because it's ridiculously inconsistent.
	 */
    private static final int updateInterval_ms = 34;
    //this was used primarily for repaint grace periods but it was discovered that repaint need not be called at all
    public static int getUpdateInterval_ms() { return updateInterval_ms; }
    //Get the adjusted speed of the animation system, the adjustment is necessary for the java Update animation system to match the speed in Unity
    public static int getAdjustedUpdateInterval_ms() { return updateInterval_ms * 2; }
    //run mechanism to constantly call Update throughout the life of this instance
    Timer updateTimer = new Timer(updateInterval_ms, new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			//This is a good place to modify the speed of all anims
			int deltaTime_ms = getAdjustedUpdateInterval_ms();
			
			//System.out.println("updateTimer - actionPerformed, deltaTime_ms: " + deltaTime_ms);
			
			//Tick timerTasks
			List<UpdateTimer> tasksToRemove = new ArrayList<UpdateTimer>();
			//Use a temp list, otherwise Edit/Access Inconsistency errors will occur.
			UpdateTimer[] tempArray = updateTimers.stream().toArray(UpdateTimer[]::new);
			for(UpdateTimer timer : tempArray) {
				if(!updateTimers.contains(timer)) {
					//System.out.println("This Timer has been asynchronous removed from updateTimers, while we were iterating through it. Continuing.");
					continue;
				}
				
				boolean isTaskComplete = timer.Tick(deltaTime_ms);
				
				boolean removeTimer = false;
				if(isTaskComplete) {
					if(!timer.isRepeating) {
						removeTimer = true;
					} else {
						RepeatingListener repeatingListener = null;
						try {
							repeatingListener = (RepeatingListener)timer.listener;
							//System.out.println("Class WAS a RepeatingListener");
						} catch(ClassCastException e) {
							//System.out.println("Class WASN'T a RepeatingListener");
						}
						//if this is a RepeatingListener then check if its decided to stop itself
						removeTimer = repeatingListener != null && repeatingListener.IsDoneRepeating();
					}
				}
				
				if(removeTimer)
					tasksToRemove.add(timer);
			}
			updateTimers.removeAll(tasksToRemove);
			
			//TODO Test ways to make the actor images update properly
			//Try repaint the scene panel to guarantee that Actor's changes are shown
			scenePane.repaint();
		}
    });
    
    /**
     * Used to track whether the Update animation system has been started, this will indicate that we need to refresh and restart the system upon re-entry to this scene.
     */
    private boolean sceneAnimation_dirtyFlag;
    
    public class UpdateTimer {
		/**
		 * 
		 * @param duration_ms - May be zero or -1 to indicate that this timer should be complete on every tick, but that may not necessarily happen.
		 * 						It could be a frame late. I believe this is for the best; to keep everything synced.
		 * @param isRepeating
		 * @param listener
		 */
		public UpdateTimer(int duration_ms, boolean isRepeating, ActionListener listener) {
			if(duration_ms <= 0)
				this.duration_ms = updateInterval_ms;
			else
				this.duration_ms = duration_ms;
			timeRemaining_ms = this.duration_ms;
			this.isRepeating = isRepeating;
			this.listener = listener;
		}
		public int duration_ms;
		public boolean isRepeating;
		public ActionListener listener;
		//Activity properties
		public int timeRemaining_ms;
		
		/**
		 * Tick is called during the general update, for all UpdateTimers, to know whether a timer is finished and if so then either remove it from the
		 * updateTimers list or leave it to continue updating. (UpdateTimers "restart" themselves upon their completion, anticipating repeated calls)
		 * @param deltaTime_ms - The amount of thats passed since the last Tick.
		 * @return
		 */
		public boolean Tick(int deltaTime_ms) {
			boolean isTaskComplete = false;
			timeRemaining_ms -= deltaTime_ms;
			isTaskComplete = timeRemaining_ms <= 0;
			if(isTaskComplete) {
				listener.actionPerformed(null);
				//prepare it for another cycle incase this timerTask is repeated
				timeRemaining_ms = duration_ms;	
			}
			return isTaskComplete;
		}
	}
    
  //Need a runtime class defined for AnimatedTiles to store the data, the UI component and a timer to run the never ending anim loop
  	public class AnimatedTile {
  		public AnimatedTile(VisualTileData visualTileData, SpriteSheet spriteSheet, ImagePanel panel) {
  			this.visualTileData = visualTileData;
  			this.spriteSheet = spriteSheet;
  			this.panel = panel;
  			CreateTimer();
  		}
  		private void CreateTimer() {
  			CreateUpdateTimer(new UpdateTimer(interval_ms, true, new ActionListener() {
  				@Override
  				public void actionPerformed(ActionEvent arg0) {
  					frameIndex++;
  					if(frameIndex >= visualTileData.m_AnimatedSprites_names.length)
  						frameIndex = 0;
  					panel.SetNewImage(spriteSheet.GetSprite(visualTileData.m_AnimatedSprites_names[frameIndex]), getUpdateInterval_ms());
  					//System.out.println("AnimatedTile new frame: " + visualTileData.m_AnimatedSprites_names[frameIndex]);
  				}
  	        }));
  		}
  		//Data and runtime image holder
  		public VisualTileData visualTileData;
  		public SpriteSheet spriteSheet;
  		//UI Component
  		public ImagePanel panel;
  		private final int interval_ms = 612;
  		private int frameIndex;
  		public void Reset() {
  			frameIndex = 0;
  			panel.SetNewImage(spriteSheet.GetSprite(visualTileData.m_AnimatedSprites_names[frameIndex]), getUpdateInterval_ms());
  			CreateTimer();
  		}
  	}
  	List<AnimatedTile> animatedTiles = new ArrayList<AnimatedTile>();
  	
  	public final class SceneLayoutInfo {
		public SceneLayoutInfo(Dimension paneSize, Dimension sceneSize, Point sceneTopCornerLoc, SceneData sceneData, int tileSize) {
			this.paneSize = paneSize;
			this.sceneSize = sceneSize;
			this.sceneTopCornerLoc = sceneTopCornerLoc;
			this.sceneData = sceneData;
			columnCount = sceneData.sceneWidth;
			rowCount = sceneData.sceneHeight;
			tileWidth = tileSize;
			tileHeight = tileSize;
		}
		
		public final Dimension paneSize;
		public final Dimension sceneSize;
		public final Point sceneTopCornerLoc;

		public final SceneData sceneData;
		public final int columnCount;
		public final int rowCount;
		public final int tileWidth;
		public final int tileHeight;
	}
	private SceneLayoutInfo sceneLayoutInfo;
	
	private List<Actor> sceneActors = new ArrayList<Actor>();
	private List<Actor> activeActorsBeforeBattle = new ArrayList<Actor>();
	public List<ActorData> getActiveActorDatasBeforeBattle() {
		List<ActorData> datas = new ArrayList<ActorData>();
		for(Actor actor : activeActorsBeforeBattle)
			datas.add(actor.getActorData());
		return datas;
	}
	
	
	//Setup Methods - Start
	
	public MapLocationPanel(WorldmapPanel worldmapPanel) {
		super(new BorderLayout());
		//instance = this;
		this.worldmapPanel = worldmapPanel;
	}
	
	public void Initialize() {
		Dimension paneSize = this.getSize();
		List<CustomButtonUltra> customUltras = new ArrayList<CustomButtonUltra>();
		
		//encapsulate everything in a layer
		//UI in layer 0
		//SCene in layer 1
		//BG in layer 2
		JLayeredPane mainPane = new JLayeredPane();
		mainPane.setSize(paneSize);
		mainPane.setPreferredSize(paneSize);
		//surroundingsPane.setBorder(BorderFactory.createLineBorder(Color.RED,  2));
		
		//Font titleFont = new Font("Verdana", 0, 30);
		//Color panelBGColor = new Color(0.9f, 0.9f, 0.9f, 0.85f);
		
		//Location name
		JLayeredPane uiPane = new JLayeredPane();
		uiPane.setOpaque(false);
		uiPane.setBackground(new Color(0,0,0,0));
		uiPane.setSize(paneSize);
		uiPane.setPreferredSize(paneSize);
		//uiPane.setBorder(BorderFactory.createLineBorder(Color.ORANGE,  1));
		
		//Leave Button
		Point leaveButtonLoc = GUIUtil.GetRelativePoint(0.015f, 0.06f);
		Dimension leaveButtonSize = GUIUtil.GetRelativeSize(0.12f, 0.1f);
		JFxLabel leaveText = new JFxLabel("Leave", SwingConstants.CENTER, GUIUtil.Header, Color.BLACK);
		leaveButton = new CustomButtonUltra(leaveText, null, Color.GREEN, SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(), new Color(240, 240, 240));
		customUltras.add(leaveButton);
		leaveButton.setBounds(leaveButtonLoc.x, leaveButtonLoc.y, leaveButtonSize.width, leaveButtonSize.height);
		leaveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//Notify Game so that it can track whether the player ended the game in the MapLocation panel or the Worldmap panel
				Game.Instance().SetIsInMapLocation(false);
				//GUIManager.ShowScreen(MenuType.WORLDMAP);
				
				//[MISSION_FLOW_EDIT]
				//if this was a trailing mission or transitional mission then complete it upon leaving
				/*if(activeMission != null && (activeMission.getMissionStipulations() == null || activeMission.getMissionStipulations().size() == 0)) {
					System.out.println("MapLocationPanel.LeaveButton(ActionListener) - Completed trailing or transitional mission: " + activeMission.getName());
					Game.Instance().CompleteMission(activeMission);
					if(activeMission.getRewards() != null) {
						//TODO Display gained items on UI using governingMission.getRewards());
						System.err.println("MapLcoationPanel.LeaveButton Listener - STUB - Show rewards for completed mission.");
					}
					activeMission = null;
				}*/
				//I'm not sure what my impetus was for using this logic here in the leave action but it seems better to have all such logic in Resolutions, whether they be interactions or interactionless
				
				GUIManager.GetFadeTransitionPanel().Fade(true, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						GUIManager.ShowScreen(MenuType.WORLDMAP);
						GUIManager.GetFadeTransitionPanel().Fade(false, 120);
					}
				});
			}
		});
		uiPane.add(leaveButton, 1, 0);
		
		Point leaveBGLoc = GUIUtil.GetRelativePoint(0.03f, 0.08f);
		Dimension leavePanelSize = GUIUtil.GetRelativeSize(0.2f, true);
		leaveBG = new ImagePanel(SpriteSheetUtility.CircleBG());
		leaveBG.setOpaque(false);
		leaveBG.setBackground(new Color(0,0,0,0));
		leaveBG.setBounds(-leaveBGLoc.x, -leaveBGLoc.y, leavePanelSize.width, leavePanelSize.height);
		uiPane.add(leaveBG, 0, 0);
		
		
		//Location Title
		Dimension titleSectionSize = GUIUtil.GetRelativeSize(0.8f, 0.14f);
		
		int startLocX = (int)Math.round(titleSectionSize.width * 0.26f);
		locationTitle = new JFxLabel("AREA UNKNOWN", SwingConstants.CENTER, GUIUtil.LocationLabel, Color.BLACK);
		locationTitle.setBounds(startLocX, 0, titleSectionSize.width - startLocX, titleSectionSize.height);
		uiPane.add(locationTitle, 1, 1);
		
		titleBG = new JLabel(SpriteSheetUtility.CircularNinecon());
		titleBG.setOpaque(false);
		titleBG.setBackground(new Color(0,0,0,0));
		titleBG.setBounds(0, 0, titleSectionSize.width, titleSectionSize.height);
		uiPane.add(titleBG, 0, 1);
		
		
		//Location Description
		Point descBGPanelLoc = GUIUtil.GetRelativePoint(0f, 1f - 0.14f);
		Dimension descBGSize = GUIUtil.GetRelativeSize(0.8f, 0.14f);
		//Dimension descTextSize = GUIUtil.GetRelativeSize(0.6f, 0.14f);
		Dimension descTextSize = GUIUtil.GetRelativeSize(0.77f, 0.12f);
		descriptionTextArea = new JTextArea(); //5, 15); Shouldn't need to set the rows and columns since the setting of the scrollPane's size will control the textArea size
		descriptionTextArea.setEditable(false);
		descriptionTextArea.setHighlighter(null);
		descriptionTextArea.setLineWrap(true);
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setFont(GUIUtil.Body);
		descriptionTextArea.setBackground(new Color(240, 240, 240));
		descriptionTextArea.setText("Dialog dialog dialog dialog dialog dialog dialog dialog dialog dialog dialog dialog dialog dialog dialog dialog dialog dialog dialog dialog dialog dialog.");
		locationDescription = new JScrollPane(descriptionTextArea);
		int descTextInsetX = GUIUtil.GetRelativeSize(0.03f, true).width;
		int descTextInsetY = GUIUtil.GetRelativeSize(0.025f, false).height;
		locationDescription.setBounds(descTextInsetX, descBGPanelLoc.y + descTextInsetY, descTextSize.width - descTextInsetX, descTextSize.height - descTextInsetY);
		locationDescription.setBorder(null);
		uiPane.add(locationDescription, 1, 1);
		
		descBG = new JLabel(SpriteSheetUtility.CircularNinecon());
		descBG.setOpaque(false);
		descBG.setBackground(new Color(0,0,0,0));
		descBG.setBounds(descBGPanelLoc.x, descBGPanelLoc.y, descBGSize.width, descBGSize.height);
		uiPane.add(descBG, 0, 1);
		
		
		//Location Interactions
		Point interactBGLoc = GUIUtil.GetRelativePoint(0.82f, 0.16f);
		Dimension interactBGSize = GUIUtil.GetRelativeSize(0.3f, 1f - 0.18f);
		Point interactButsLoc = GUIUtil.GetRelativePoint(0.84f, 0.24f);
		Dimension interactButsSize = GUIUtil.GetRelativeSize(0.14f, 1f - 0.28f);
		
		interactTitle = new JFxLabel("Interact", SwingConstants.CENTER, GUIUtil.ItalicHeader_L, Color.DARK_GRAY);
		int interactTextOffsetY = GUIUtil.GetRelativeSize(0.07f, false).height;
		interactTitle.setBounds(interactButsLoc.x, interactButsLoc.y - interactTextOffsetY, interactButsSize.width, GUIUtil.GetRelativeSize(0.06f, false).height);
		//interactTitle.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		uiPane.add(interactTitle, 1, 1);
		
		interactionOptions = new JPanel();
		interactionOptions.setLayout( new BoxLayout(interactionOptions, BoxLayout.Y_AXIS) );
		interactionOptions.setOpaque(false);
		interactionOptions.setBackground(new Color(0,0,0,0));
		interactionOptions.setBounds(interactButsLoc.x, interactButsLoc.y, interactButsSize.width, interactButsSize.height);
		//interactionOptions.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		for(InteractionType type : InteractionType.values()) {
			//Dimension buttonSize = new Dimension(interactButsSize.width, GUIUtil.GetRelativeSize(0.06f, false).height);
			Dimension buttonSize = GUIUtil.GetRelativeSize(0.31f, 0.1f);
			JFxLabel text = new JFxLabel(type.name(), SwingConstants.CENTER, GUIUtil.Header, Color.BLACK);
			CustomButtonUltra button = new CustomButtonUltra(text, null, Color.GREEN, SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(), new Color(240, 240, 240));
			customUltras.add(button);
			button.setSize(buttonSize);
			button.setPreferredSize(buttonSize);
			button.setMaximumSize(buttonSize);
			
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					HandleInteraction(type, button);
				}
			});
			interactionButtonMap.put(type, button);
			interactionOptions.add(button);
		}
		uiPane.add(interactionOptions, 1, 1);
		
		interactBG = new JLabel(SpriteSheetUtility.ValueBGNinecon());
		interactBG.setOpaque(false);
		interactBG.setBackground(new Color(0,0,0,0));
		interactBG.setBounds(interactBGLoc.x, interactBGLoc.y, interactBGSize.width, interactBGSize.height);
		uiPane.add(interactBG, 0, 1);
		
		
		//Results pane for interaction with GrantedItems and mission with Reward items
		resultsPane = new ResultsPane(new Point2D.Float(0.75f, 0.75f),
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(missionResultsListener == null) {
						System.out.println("ResultsPane.ActionListener - Accept Button - ApplyInteractionEnd()");
						resultsPane.setVisible(false);
						ApplyInteractionEnd();
					} else
						missionResultsListener.actionPerformed(null);
				}
			}
		);
		uiPane.add(resultsPane, 11, 1);
		resultsPane.setVisible(false);
		
		
		//Interaction Dialog
		float dialogPanelInset = 0.02f;
		//Dimension panelInset = GUIUtil.GetRelativeSize(dialogPanelInset, true);
		
		Point dialogPanelLoc = GUIUtil.GetRelativePoint(0f, 0.75f);
		Dimension dialogPanelSize = GUIUtil.GetRelativeSize(1f, 0.25f);
		
		Point intDiaLabOffset = GUIUtil.GetRelativePoint(0.032f, 0.018f);
		Dimension intDiaLabSize = GUIUtil.GetRelativeSize(0.2f, 0.04f);
		interactionDialogLabel = new JFxLabel("INTERACTION", SwingConstants.LEFT, GUIUtil.ItalicHeader, Color.WHITE)
				.withShadow(Color.GRAY, new Point(2, 2))
				.withStroke(Color.DARK_GRAY, 2, true);
		interactionDialogLabel.setBounds(intDiaLabOffset.x, dialogPanelLoc.y - intDiaLabOffset.y, intDiaLabSize.width, intDiaLabSize.height);
		//interactionDialogLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		uiPane.add(interactionDialogLabel, 12, 1);
		
		float portraitNameLocNormY = 0.96f;
		Point leftPortraitNameLoc = GUIUtil.GetRelativePoint(dialogPanelInset, portraitNameLocNormY);
		float portraitNameSizeNormX = 0.0945f;
		Dimension portraitNameSize = GUIUtil.GetRelativeSize(portraitNameSizeNormX, 0.03f);
		leftPortraitName = new JLabel("???", SwingConstants.CENTER);
		leftPortraitName.setFont(GUIUtil.Body_2);
		leftPortraitName.setOpaque(true);
		leftPortraitName.setForeground(Color.WHITE);
		leftPortraitName.setBackground(Color.GRAY);
		leftPortraitName.setPreferredSize(portraitNameSize);
		leftPortraitName.setBounds(leftPortraitNameLoc.x, leftPortraitNameLoc.y, leftPortraitName.getPreferredSize().width, leftPortraitName.getPreferredSize().height);
		//leftPortraitName.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
		uiPane.add(leftPortraitName, 11, 1);
		
		Point rightPortraitLoc = GUIUtil.GetRelativePoint(1f - 0.115f, 0.77f);
		
		Point rightPortraitNameLoc = GUIUtil.GetRelativePoint(1f - portraitNameSizeNormX - dialogPanelInset, portraitNameLocNormY);
		rightPortraitName = new JLabel("???", SwingConstants.CENTER);
		rightPortraitName.setFont(GUIUtil.Body_2);
		rightPortraitName.setOpaque(true);
		rightPortraitName.setForeground(Color.WHITE);
		rightPortraitName.setBackground(Color.GRAY);
		rightPortraitName.setPreferredSize(portraitNameSize);
		rightPortraitName.setBounds(rightPortraitLoc.x, rightPortraitNameLoc.y, rightPortraitName.getPreferredSize().width, rightPortraitName.getPreferredSize().height);
		//rightPortraitName.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
		uiPane.add(rightPortraitName, 10, 1);
		
		Point leftPortraitLoc = GUIUtil.GetRelativePoint(dialogPanelInset, 0.77f);
		Dimension portraitSize = GUIUtil.GetRelativeSize(0.12f, 0.21f);
		leftPortrait = new ImagePanel(GUIUtil.GetBuffedImage("portraits/Attendant.png"));
		leftPortrait.setOpaque(true);
		leftPortrait.setBackground(Color.LIGHT_GRAY);
		leftPortrait.setPreferredSize(portraitSize);
		leftPortrait.ConformPreferredSizeToAspectRatio(false);
		leftPortrait.setBounds(leftPortraitLoc.x, leftPortraitLoc.y, leftPortrait.getPreferredSize().width, leftPortrait.getPreferredSize().height);
		leftPortrait.setBorder(BorderFactory.createLineBorder(Color.GRAY, 4));
		leftPortrait.SetPaintInsideInsets(true);
		uiPane.add(leftPortrait, 9, 1);
		
		dialogPortrait_originalHeight = leftPortrait.getPreferredSize().height;
		dialogPortrait_originalYPos = leftPortraitLoc.y;
		
		dialogText = new JTextArea("Dialog dialog dialog dialog dialog dialog.");
		dialogText.setFont(GUIUtil.Body_2);
		dialogText.setEditable(false);
		dialogText.setFocusable(false);
		dialogText.setLineWrap(true);
		dialogText.setWrapStyleWord(true);
		Dimension margins = GUIUtil.GetRelativeSize(0.007f, 0.005f);
		dialogText.setMargin(new Insets(margins.height, margins.width, margins.height, margins.width));
		Point dialogLoc = GUIUtil.GetRelativePoint(0.13f, 0.77f);
		Dimension dialogSize = GUIUtil.GetRelativeSize(0.735f, 0.14f);
		dialogText.setBounds(dialogLoc.x, dialogLoc.y, dialogSize.width, dialogSize.height);
		uiPane.add(dialogText, 8, 1);
		
		
		float dialogButStartX = 0.7f;
		Point2D buttonROffset = new Point2D.Float(0.01f, 0.005f);
		float buttonRWidth = 0.04f;
		Dimension dialogButSize = GUIUtil.GetRelativeSize(buttonRWidth, true);
		
		//dialogBackButton = new JButton("Back");
		dialogBackButton = new CustomButton(SpriteSheetUtility.LeftArrowSymbol(), null, Color.GREEN, SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(), new Color(240, 240, 240));
		dialogBackButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!isLockedByActor && currentDialogFrame > 0)
					IterateDialogFrame(false);
			}
		});
		Point dialogButBackLoc = GUIUtil.GetRelativePoint(dialogButStartX, 1f - (float)buttonROffset.getY());
		dialogBackButton.setBounds(dialogButBackLoc.x, dialogButBackLoc.y - dialogButSize.height, dialogButSize.width, dialogButSize.height);
		uiPane.add(dialogBackButton, 7, 1);
		
		dialogNextButton = new CustomButton(SpriteSheetUtility.RightArrowSymbol(), null, Color.GREEN, SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(), new Color(240, 240, 240));
		dialogNextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!isLockedByActor && currentDialogFrame < lastIndex)
					IterateDialogFrame(true);
			}
		});
		Point dialogButNextLoc = GUIUtil.GetRelativePoint(dialogButStartX + buttonRWidth + (float)buttonROffset.getX(), 1f - (float)buttonROffset.getY());
		dialogNextButton.setBounds(dialogButNextLoc.x, dialogButNextLoc.y - dialogButSize.height, dialogButSize.width, dialogButSize.height);
		uiPane.add(dialogNextButton, 6, 1);
		
		//proceedButton = new CustomButtonUltra(proceedText, null, Color.GREEN, SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(), new Color(240, 240, 240));
		//customUltras.add(proceedButton);
		proceedButton = new CustomButton(SpriteSheetUtility.CheckmarkSymbol(), null, Color.GREEN, SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(), new Color(240, 240, 240));
		proceedButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!hasInteractionBeenApplied) {
					if(currentInteraction == null)
						ResolveInteractionlessDialography();
					else
						ContinueInteraction();
				} else {
					ResolveInteraction();
				}
			}
		});
		proceedButton.setVisible(false);
		Point dialogButProceedLoc = GUIUtil.GetRelativePoint(dialogButStartX + (buttonRWidth * 2f) + ((float)buttonROffset.getX() * 4f), 1f - (float)buttonROffset.getY());
		proceedButton.setBounds(dialogButProceedLoc.x, dialogButProceedLoc.y - dialogButSize.height, dialogButSize.width, dialogButSize.height);
		uiPane.add(proceedButton, 5, 1);
		
		rightPortrait = new ImagePanel(GUIUtil.GetBuffedImage("portraits/Attendant.png"));
		rightPortrait.setOpaque(true);
		rightPortrait.setBackground(Color.LIGHT_GRAY);
		rightPortrait.setPreferredSize(portraitSize);
		rightPortrait.ConformPreferredSizeToAspectRatio(false);
		rightPortrait.setBounds(rightPortraitLoc.x, rightPortraitLoc.y, rightPortrait.getPreferredSize().width, rightPortrait.getPreferredSize().height);
		rightPortrait.setBorder(BorderFactory.createLineBorder(Color.GRAY, 4));
		rightPortrait.SetPaintInsideInsets(true);
		uiPane.add(rightPortrait, 4, 1);
		
		dialogBG = new JLabel(SpriteSheetUtility.CircularNinecon());
		dialogBG.setOpaque(false);
		dialogBG.setBackground(new Color(0,0,0,0));
		dialogBG.setBounds(dialogPanelLoc.x - GUIUtil.GetRelativeSize(0.03f, true).width, dialogPanelLoc.y, dialogPanelSize.width + GUIUtil.GetRelativeSize(0.06f, true).width, dialogPanelSize.height + GUIUtil.GetRelativeSize(0.05f, false).height);
		uiPane.add(dialogBG, 3, 1);
		
		
		//Add UI and the other layers to the mainPane
		int mainMenuPaneIndex = 0;
		
		
		//DEBUG - Start - This will add a scene selector to the mainPane
		//mainPane.add(DEBUG_CreateSceneSelectionPanel(paneSize), mainMenuPaneIndex++);
		//DEBUG - End
		
		
		mainPane.add(uiPane, mainMenuPaneIndex++);
		
		//scene layer
		/*sceneData = Game.Instance().GetSceneData();
		//Find a versitile way to scale the scene to scale it inside the available area
		sceneSize = new Dimension(sceneData.sceneWidth * GetAdjustedTileSize(), sceneData.sceneHeight * GetAdjustedTileSize());
		
		Point sceneTopCorner = new Point((paneSize.width - sceneSize.width) / 2, (paneSize.height - sceneSize.height) / 2);
		sceneTopCornerLoc = sceneTopCorner;
		defaultSceneLocY = sceneTopCorner.y;*/
		SetupSceneVariables();
		
		//Add Campfire Light Overlay
		campfireLightImagePanel = new ImagePanel("mapLocationScenes/CampfireLightOverlay_Pixelated.png");
		campfireLightImagePanel.setOpaque(false);
		campfireLightImagePanel.setBackground(new Color(0,0,0,0));
		campfireLightImagePanel.setSize(paneSize);
		campfireLightImagePanel.setPreferredSize(paneSize);
		campfireLightImagePanel.setVisible(false);
		mainPane.add(campfireLightImagePanel, mainMenuPaneIndex++);
		
		//Add layeredPane for Scene Actors
		//TODO - these actors need to be woven into scenePane
		/*actorPane = new JLayeredPane();
		actorPane.setSize(paneSize);
		actorPane.setPreferredSize(paneSize);
		actorPane.setLocation(0,0);
		//scenePane.setBorder(BorderFactory.createLineBorder(Color.BLUE,  1));
		mainPane.add(actorPane, mainMenuPaneIndex++);*/
		
		//Add a layer for the battlePanel
		//TODO - if this battlePanelContainer holds the battle characters then we need to separate them and weave them into scenePane
		battlePanelContainer = new JPanel(new BorderLayout());
		battlePanelContainer.setOpaque(false);
		battlePanelContainer.setBackground(new Color(0,0,0,0));
		battlePanelContainer.setSize(paneSize);
		battlePanelContainer.setPreferredSize(paneSize);
		mainPane.add(battlePanelContainer, mainMenuPaneIndex++);
		
		//Scene pane Setup
		scenePane = new JLayeredPane();
		scenePane.setSize(sceneSize);
		scenePane.setPreferredSize(sceneSize);
		scenePane.setLocation(sceneTopCornerLoc);
		//scenePane.setBorder(BorderFactory.createLineBorder(Color.BLUE,  1));
		mainPane.add(scenePane, mainMenuPaneIndex++);
		
		//bg layer
		locationBG = new ImagePanel("Question.png");
		locationBG.setSize(paneSize);
		locationBG.setPreferredSize(paneSize);
		locationBG.setBackground(Color.DARK_GRAY);
		mainPane.add(locationBG, mainMenuPaneIndex++);
		
		this.add(mainPane, BorderLayout.CENTER);
		
		//prepare the info for the scene Actors when their instantiated later
		//sceneLayoutInfo = new SceneLayoutInfo(paneSize, sceneSize, sceneTopCornerLoc, sceneData, GetAdjustedTileSize());
		
		//Add all buttons to a group so that they can elimate eachothers artifacting
		for(CustomButtonUltra ultra : customUltras)
			ultra.AddGroupList(customUltras);
	}
	
	SceneData sceneData;
	private void SetupSceneVariables() {
		sceneData = Game.Instance().GetSceneData();
		//Find a versitile way to scale the scene to fit it inside the available area
		sceneSize = new Dimension(sceneData.sceneWidth * GetAdjustedTileSize(), sceneData.sceneHeight * GetAdjustedTileSize());
		
		Point sceneTopCorner = new Point((this.getSize().width - sceneSize.width) / 2, (this.getSize().height - sceneSize.height) / 2);
		sceneTopCornerLoc = sceneTopCorner;
		defaultSceneLocY = sceneTopCorner.y;
		
		sceneLayoutInfo = new SceneLayoutInfo(this.getSize(), sceneSize, sceneTopCornerLoc, sceneData, GetAdjustedTileSize());
		
		//now that we have the new size and new location we need to apply it to the scenePane
		if(scenePane != null) {
			System.out.println("MapLocationPanel.SetupSceneVariables() - Setting scenePane to new size: " + sceneSize);
			scenePane.setSize(sceneSize);
			scenePane.setPreferredSize(sceneSize);
			scenePane.setLocation(sceneTopCornerLoc);
		}
	}
	
	
	public static int GetAdjustedTileSize() {
		return Math.round(Board.ScaledTileSize() * 2f);
	}
	
	
	public static Point GetSceneLoc() { return sceneTopCornerLoc; }
	
	
	public static Dimension GetSceneSize() { return sceneSize; }
	
	//Debugging tool, associates all scenePane components with a logical name: "Row Image #", "Breakaway #", "Visual Tile #" or "Character Sprite #"
	Map<Integer, String> sceneLayerIdentifiers = new HashMap<Integer, String>();
	
	private void AddToDebugMap(Integer hashcode, String nameLabel) {
		boolean isHashUnclaimed = sceneLayerIdentifiers.putIfAbsent(hashcode, nameLabel) == null;
		if(!isHashUnclaimed)
			System.err.println("Hashcode has already been added to the sceneLayerIdentifiers map!");
	}
	
	public void DebugScenePaneLayers() {
		System.out.println("MapLocationPanel.DebugScenePaneLayers() - Printing all components:");
		for(int i = scenePane.lowestLayer(); i <= scenePane.highestLayer(); i++) {
			//int invertedLayer = sceneData.sceneHeight - 1 - i;
			//System.out.println("  ScenePane Layer " + invertedLayer);
			System.out.println("  ScenePane Layer " + i);
			
			for(int l = scenePane.getComponentCountInLayer(i) - 1; l > -1; l--) {
				String nameLabel = sceneLayerIdentifiers.get(scenePane.getComponentsInLayer(i)[l].hashCode());
				System.out.println("	SubLayer " + l + ": " + nameLabel);
			}
		}
	}
	
	//This will need to be called later, after the Worldmap has been setup
	private void AssembleScene() {
		//clear previous layers of scenePane
		scenePane.removeAll();
		
		SceneData sceneData = Game.Instance().GetSceneData();
		//System.out.println("AssembleScene() - sceneData.width: " + sceneData.sceneWidth + ", sceneData.height: " + sceneData.sceneHeight);
		
		int columnCount = sceneData.sceneWidth;
		int rowCount = sceneData.sceneHeight;
		int tileWidth = GetAdjustedTileSize();
		int tileHeight = GetAdjustedTileSize();
		
		int layerWidth = tileWidth * columnCount;
		int sceneHeight = tileHeight * rowCount;
		
		int settlementLayerWidth = tileWidth * sceneData.settlementSceneWidth;
		int settlementOffsetX = sceneData.settlementSceneOffsetX * tileWidth;
		
		//int settlementOffsetY = sceneData.settlementSceneOffsetY * tileHeight;
		//Nagative values funk up the layering such that settlements taller than nature layers will be hidden beneath shallower nature rows.
		int settlementOffsetY = Math.max(0, sceneData.settlementSceneOffsetY * tileHeight);
		
		//Only one component can occupy one absolute index in the depth of the JLayeredPane. When a component is added to or set to occupy
		//the depth index of another component, all lower components are shifted back and the new component inserted.
		//List<Integer> breakawayTrackerProxy = new ArrayList<Integer>(); //Tracker: SublayerType and its original row(which will be inverted later)
		//A list of trackers means we could only track one breakaway per row, but there can be multiple breakaways on the same row
		/*Map<Integer, Integer> breakawayTrackerProxyMap = new HashMap<Integer, Integer>();
		for(int layer = 0; layer < rowCount; layer++) {
			Row row = sceneData.rows.get(layer);
			//Skip blank rows
			if(row == null || (row.tileDatas.size() == 0 && row.imageLayers.size() == 0))
				continue;
			
			SublayerDepthManager sublayerDepthManager = new SublayerDepthManager();
			sublayerDepthManagers.add(0, sublayerDepthManager);
			
			//one row could have multiple images for example: Layer1 = Base row layer image for scene, Layer2 = Breakaway image 1, Layer3 = Breakaway Image 2, etc
			//This approach is used to spoof the layering of combined scenes, like: Layer1 = Base nature layer image, Layer2 = Base settlement layer image, Layer3 = Nature Breakaway Image 1,
			//Layer4 = Settlement Breakaway Image 1, etc
			for(int posIndex = row.imageLayers.size() - 1; posIndex > -1; posIndex--) {
				ImagePanel layerPanel = new ImagePanel(Game.SceneDirectoryRoot() + row.imageLayers.get(posIndex).imageFilePath);
				layerPanel.setOpaque(false);
				layerPanel.setBackground(new Color(0,0,0,0));
				
				int currentLayerStartHeight = tileHeight * layer;
				if(!row.imageLayers.get(posIndex).belongsToSettlement) {
					int layerHeight = row.imageLayers.get(posIndex).layerHeight * tileHeight;
					Dimension layerDimensions = new Dimension(layerWidth, layerHeight);
					layerPanel.setSize(layerDimensions);
					layerPanel.setPreferredSize(layerDimensions);
					layerPanel.setLocation(0, sceneHeight - layerHeight - currentLayerStartHeight);
				} else {
					int settlementLayerHeight = row.imageLayers.get(posIndex).layerHeight * tileHeight;
					Dimension settlementLayerDimensions = new Dimension(settlementLayerWidth, settlementLayerHeight);
					layerPanel.setSize(settlementLayerDimensions);
					layerPanel.setPreferredSize(settlementLayerDimensions);
					layerPanel.setLocation(settlementOffsetX, sceneHeight - settlementLayerHeight - currentLayerStartHeight - settlementOffsetY);
				}
				
				
				if(row.imageLayers.get(posIndex).imageFilePath.startsWith("Estate"))
					layerPanel.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
				
				int invertedLayer = rowCount - 1 - layer;
				//Apparently this is supposed to happen before adding the component to the JLayeredPane, nowhere else am I doing this and it all seems to work properly regardless
				scenePane.setLayer(layerPanel, invertedLayer);
				
				int positivePosIndexSequence = row.imageLayers.size() - 1 - posIndex;
				
				//if(row.imageLayers.size() > 1) {
				//now that we're spoof the lcombo layers here we need a more comprehensive condition to know whether there are breakaway layer  or just base layers
				int breakawayCount = (int)row.imageLayers.stream().filter(x -> x.imageFilePath.contains("breakaway")).count();
				if(breakawayCount > 0) {
					//if(posIndex != 0) {
						System.out.println("MapLocationPanel.AssembleScene() - Encountered breakaways on row: " + layer + ", but saving entry in proxy for inverted recording, later.");
						breakawayTrackerProxyMap.put(invertedLayer, breakawayCount);
					//} else {
					//	System.err.println("MapLocationPanel.AssembleScene() - Encountered breakaways on last layer, but they aren't being tallied. Shouldn't they be tallied, though?");
					//}
				}
				
				scenePane.add(layerPanel, invertedLayer, positivePosIndexSequence);
				sublayerDepthManagers.get(0).TrackOccupantActivity(row.imageLayers.get(posIndex).belongsToSettlement ? SublayerType.RowImage_Settlement : SublayerType.RowImage_Nature , true);
				
				System.out.println("MapLocationPanel.AssembleScene() - Adding rows from bottom to top, with image: " + row.imageLayers.get(posIndex).imageFilePath + ", on scenePane layer: "
						+ invertedLayer);
				
				AddToDebugMap(layerPanel.hashCode(), row.imageLayers.get(posIndex).imageFilePath);
			}
		}
		
		//Because of the fucky reverse nature of the layer, and because we have to naturally populate the sublayerDepthManagers list, we tally the breakaway after the fact
		for(Integer invertedLayer : breakawayTrackerProxyMap.keySet()) {
			for(int i = 0; i < breakawayTrackerProxyMap.get(invertedLayer); i++) {
				sublayerDepthManagers.get(invertedLayer).TrackOccupantActivity(SublayerType.Breakaway, true);
				System.out.println("MapLocationPanel.AssembleScene() - Adding Breakaway to sublayer depth: " + invertedLayer);
			}
		}*/
		Map<Integer, Integer> breakawayTrackerProxyMap = new HashMap<Integer, Integer>();
		for(int layer = 0; layer < rowCount; layer++) {
			Row row = sceneData.rows.get(layer);
			//Skip blank rows
			if(row == null || (row.tileDatas.size() == 0 && row.imageLayers.size() == 0))
				continue;
			
			SublayerDepthManager sublayerDepthManager = new SublayerDepthManager();
			sublayerDepthManagers.add(0, sublayerDepthManager);
			
			//one row could have multiple images for example: Layer1 = Base row layer image for scene, Layer2 = Breakaway image 1, Layer3 = Breakaway Image 2, etc
			//This approach is used to spoof the layering o
			for(int posIndex = row.imageLayers.size() - 1; posIndex > -1; posIndex--) {
				ImageLayer imageLayer = row.imageLayers.get(posIndex);
				ImagePanel layerPanel = new ImagePanel(Game.SceneDirectoryRoot() + imageLayer.imageFilePath);
				layerPanel.setOpaque(false);
				layerPanel.setBackground(new Color(0,0,0,0));
				
				int currentLayerStartHeight = tileHeight * layer;
				if(!imageLayer.belongsToSettlement) {
					int layerHeight = imageLayer.layerHeight * tileHeight;
					Dimension layerDimensions = new Dimension(layerWidth, layerHeight);
					layerPanel.setSize(layerDimensions);
					layerPanel.setPreferredSize(layerDimensions);
					layerPanel.setLocation(0, sceneHeight - layerHeight - currentLayerStartHeight);
					System.out.println("MapLocationPanel.AssembleScene() - NATURE layer location: " + layerPanel.getLocation());
				} else {
					int settlementLayerHeight = imageLayer.layerHeight * tileHeight;
					Dimension settlementLayerDimensions = new Dimension(settlementLayerWidth, settlementLayerHeight);
					layerPanel.setSize(settlementLayerDimensions);
					layerPanel.setPreferredSize(settlementLayerDimensions);
					
					//The settlement layer is being positioned incorrectly, aligned with the top of the nature scene instead of being centered, and I believe this line
					//is the culprit.
					//layerPanel.setLocation(settlementOffsetX, sceneHeight - settlementLayerHeight - currentLayerStartHeight - settlementOffsetY);
					layerPanel.setLocation(settlementOffsetX, sceneHeight - settlementLayerHeight - currentLayerStartHeight);
					
					
					System.out.println("MapLocationPanel.AssembleScene() - SETTLEMENT layer location: " + layerPanel.getLocation());
				}
				
				if(imageLayer.imageFilePath.startsWith("Estate"))
					layerPanel.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
				
				int invertedLayer = rowCount - 1 - layer;
				//Apparently this is supposed to happen before adding the component to the JLayeredPane, nowhere else am I doing this and it all seems to work properly regardless
				scenePane.setLayer(layerPanel, invertedLayer);
				
				int positivePosIndexSequence = row.imageLayers.size() - 1 - posIndex;
				scenePane.add(layerPanel, invertedLayer, positivePosIndexSequence);
				
				//now that we're spoofing the combo layers here we need a more comprehensive condition to know whether there are breakaway layers or just base layers
				if(imageLayer.imageFilePath.contains("breakaway")) {
					System.out.println("MapLocationPanel.AssembleScene() - Encountered breakaways on row: " + layer + ", but saving entry in proxy for inverted recording, later.");
					int breakawaySum = 0;
					if(breakawayTrackerProxyMap.containsKey(invertedLayer))
						breakawaySum = breakawayTrackerProxyMap.get(invertedLayer);
					breakawaySum++;
					breakawayTrackerProxyMap.put(invertedLayer, breakawaySum);
				} else
					sublayerDepthManagers.get(0).TrackOccupantActivity(imageLayer.belongsToSettlement ? SublayerType.RowImage_Settlement : SublayerType.RowImage_Nature , true);
				
				System.out.println("MapLocationPanel.AssembleScene() - Adding rows from bottom to top, with image: " + row.imageLayers.get(posIndex).imageFilePath + ", on scenePane layer: "
						+ invertedLayer);
				
				AddToDebugMap(layerPanel.hashCode(), imageLayer.imageFilePath);
			}
		}
		
		//Because of the fucky reverse nature of the layer, and because we have to naturally populate the sublayerDepthManagers list, we tally the breakaway after the fact
		for(Integer invertedLayer : breakawayTrackerProxyMap.keySet()) {
			for(int i = 0; i < breakawayTrackerProxyMap.get(invertedLayer); i++) {
				sublayerDepthManagers.get(invertedLayer).TrackOccupantActivity(SublayerType.Breakaway, true);
				System.out.println("MapLocationPanel.AssembleScene() - Adding Breakaway to sublayer depth: " + invertedLayer);
			}
		}
		
		
		//Create AnimatedTiles and DoorTiles
		//TODO For combined scenes support the stacking of settlement's animated tiles on top of nature base layer images, instead of trying to tuck the animated tiles beneath
		//TODO the easiest thing to do might be to get the dimensions each animated settlement tile will occupy and remove those chunks from the nature base image layer so that they'll show thru
		int animatedTileCount = 0;
		int doorTileCount = 0;
		String debugLabelName = "";
		if(sceneData.visualTileDatas != null) {
			for(VisualTileData vizData : sceneData.visualTileDatas) {
				//Setup ImagePanel, setup is exactly the same for both types
				SpriteSheet spriteSheet = SpriteSheetUtility.GetTileSheet(SpriteSheetUtility.GetSheetNameFromFrame(vizData.m_AnimatedSprites_names[0]));
				ImagePanel panel = new ImagePanel(spriteSheet.GetSprite(vizData.m_AnimatedSprites_names[0]));
				int locY = sceneHeight - (tileHeight * (vizData.gridLocationY + 1));
				if(!vizData.belongsToSettlement)
					panel.setBounds(vizData.gridLocationX * tileWidth, locY, tileWidth, tileHeight);
				else
					panel.setBounds(vizData.gridLocationX * tileWidth + settlementOffsetX, locY + settlementOffsetY, tileWidth, tileHeight);
				
				panel.setBackground(new Color(0,0,0,0));
				panel.setOpaque(false);
				
				//Get the manager for this row
				SublayerType sublayerType = null;
				int rowIndex = 0;
				if(vizData.m_DoorClosedSprites_names == null || vizData.m_DoorClosedSprites_names.length == 0) {
					animatedTiles.add(new AnimatedTile(vizData, spriteSheet, panel));
					sublayerType = vizData.belongsToSettlement ? SublayerType.AnimatedTile_Settlement : SublayerType.AnimatedTile_Nature;
					rowIndex = vizData.gridLocationY;
					
					debugLabelName = "Animated Tile (" + (vizData.belongsToSettlement ? "Settlement" : "Nature") + ") " + animatedTileCount;
					animatedTileCount++;
				} else {
					doorTiles.add(new DoorTile(vizData, spriteSheet, panel));
					sublayerType = SublayerType.DoorTile;
					rowIndex = vizData.rowIndex;
					
					debugLabelName = "Door Tile " + doorTileCount;
					doorTileCount++;
				}
				
				int invertedLayer = rowCount - 1 - rowIndex;
				scenePane.add(panel, invertedLayer, sublayerDepthManagers.get(invertedLayer).GetStartIndexForSublayer(sublayerType));
				sublayerDepthManagers.get(invertedLayer).TrackOccupantActivity(sublayerType, true);
				
				//System.out.println("Adding visual tile to type: " + sublayerType + ", being added to scenePane on Layer: " + rowIndex);
				
				AddToDebugMap(panel.hashCode(), debugLabelName);
			}
		}
		
		//Figure out the sublayer indices for every breakaway, this data structure is only necessary for breakaways occupying the same layer(row) but all breakaways are listed for convenience
		breakawaySublayerIndexMap = new HashMap<Breakaway,Integer>();
		Map<Integer,List<Breakaway>> layerBreakawaysMap = new HashMap<Integer,List<Breakaway>>();
		for(Breakaway breakaway : sceneData.breakaways) {
			List<Breakaway> layerBreakaways = new ArrayList<Breakaway>();
			if(layerBreakawaysMap.containsKey(breakaway.correspondingRowIndex))
				layerBreakaways = layerBreakawaysMap.get(breakaway.correspondingRowIndex);
			layerBreakaways.add(breakaway);
			layerBreakawaysMap.put(breakaway.correspondingRowIndex, layerBreakaways);
		}
		for(Integer key : layerBreakawaysMap.keySet()) {
			List<Breakaway> sortedLayerBreakaways = layerBreakawaysMap.get(key);

			Collections.sort(sortedLayerBreakaways, new Comparator<Breakaway>() {
		        @Override public int compare(Breakaway b1, Breakaway b2) {
		            return b1.correspondingImageLayerIndex - b2.correspondingImageLayerIndex; // Ascending
		        }
		    });
			
			for(int i = 0; i < sortedLayerBreakaways.size(); i++) {
				breakawaySublayerIndexMap.put(sortedLayerBreakaways.get(i), i);
			}
		}

		CreateSceneActors();
	}
	
	
	//Setup Methods - End
	
	int battleCharacterCount;
	public void MigrateBattleCharacterToScenePane(CharacterSocket charSocket, Point previousLocation) {
		int startRow = Game.Instance().FindTargetCharacterBase(charSocket.ID).getLocation().y;
		System.out.println("MigrateBattleCharacterToScenePane() - CharacterBase loc: " + Game.Instance().FindTargetCharacterBase(charSocket.ID).getLocation());
		
		int blankRowOffset = 0;
		for(Row row : sceneData.rows) {
			if(row.tileDatas.size() == 0)
				blankRowOffset++;
		}
		startRow += blankRowOffset;
		//System.out.println("MapLocationPanel.MigrateBattleCharacterToScenePane() - blankRowOffset: " + blankRowOffset);
		
		
		//int safeNewRow = Math.min( Math.max(startRow, 0), sublayerDepthManagers.size()-1 );
		//DEBUGGING - 6/1/22
		//this seems to be fucking shit up, setting it equal to startRow
		int safeNewRow = startRow;
		
		
		int layerPosition = sublayerDepthManagers.get(safeNewRow).GetStartIndexForSublayer(SublayerType.ActorOrProp);
		
		System.out.println("MapLocationPanel.MigrateBattleCharacterToScenePane() - startRow: " + startRow + ", safeNewRow: " + safeNewRow + ", layerPosition: " + layerPosition +
				", previousLocation: " + previousLocation + ", width: " + charSocket.comboPanel.getWidth() + ", height: " + charSocket.comboPanel.getHeight());
		
		scenePane.add(charSocket.comboPanel, safeNewRow, layerPosition);
		scenePane.setLayer(charSocket.comboPanel, safeNewRow, layerPosition);
		sublayerDepthManagers.get(safeNewRow).TrackOccupantActivity(SublayerType.ActorOrProp, true);
		
		charSocket.SetAllBounds(new Point(previousLocation.x - scenePane.getLocation().x, previousLocation.y - scenePane.getLocation().y + charSocket.feedbackPanel.getSize().height),
				charSocket.comboPanel.getComponent(1).getSize());
		
		AddToDebugMap(charSocket.comboPanel.hashCode(), "Battle Character " + battleCharacterCount);
		battleCharacterCount++;
	}
	
	public void RemoveBattleCharacterFromScenePane(CharacterSocket charSocket) {
		scenePane.remove(charSocket.comboPanel);
	}
	
	public void UpdateActorRow(JPanel panel, int lastRow, int newRow, boolean isDebuggingActor, String identifier) {
		//Because the Actors can move in and out of the bounds determined by the Scene Rows we need to keep them within the bounds of the sublayers array
		int safeLastRow = Math.min( Math.max(lastRow, 0), sublayerDepthManagers.size()-1 );
		sublayerDepthManagers.get(safeLastRow).TrackOccupantActivity(SublayerType.ActorOrProp, false);
		
		//Because the Actors can move in and out of the bounds determined by the Scene Rows we need to keep them within the bounds of the sublayers array
		int safeNewRow = Math.min( Math.max(newRow, 0), sublayerDepthManagers.size()-1 );
		int layerPosition = sublayerDepthManagers.get(safeNewRow).GetStartIndexForSublayer(SublayerType.ActorOrProp);
		scenePane.setLayer(panel, safeNewRow, layerPosition);
		sublayerDepthManagers.get(safeNewRow).TrackOccupantActivity(SublayerType.ActorOrProp, true);
		
		//if(isDebuggingActor)
			System.out.println("MapLocationPanel.UpdateActorRow() - ActorId/Name: " + identifier + ", lastRow: " + lastRow + ", safeLastRow: " + safeLastRow +
					", newRow: " + newRow + ", safeNewRow: " + safeNewRow +
					", setting at position: " + layerPosition);
	}
	
	private Map<Breakaway,Integer> breakawayOccupancyMap = new HashMap<Breakaway,Integer>();
	
	/**
	 * This gets called when characters are placed into the scene during setup and during the character movement logic.
	 * @param oldLocation
	 * @param newLocation
	 */
	public void HandleBreakawayForCharacterMove(Point oldLocation, Point newLocation) {
		DebugScenePaneLayers();
		
		//If this scene has no breakaways, or they aren't acutally moving then skip this method
		if(sceneData.breakaways == null || sceneData.breakaways.size() == 0 || (oldLocation != null && oldLocation.equals(newLocation)) || Board.BoardDimensions() == null)
			return;
		
		if(newLocation == null)
			System.err.println("MapLocationPanel.HandleBreakawayForCharacterMove() - newLocation is null! It can never be only. Only oldLocation is permitted to be null during setup.");
		
		//Convert our coodinates into the orientation that the breakaway data uses
		if(oldLocation != null)
			oldLocation = Game.Instance().GetBattlePanel().GetTransformedCoordinate(oldLocation);
		newLocation = Game.Instance().GetBattlePanel().GetTransformedCoordinate(newLocation);
		
		//Check old Location to see if they've moved off of a breakaway zone, if their new location is still in the same zone then return early
		Breakaway[] oldBreakawayArray = null;
		if(oldLocation != null) { //oldLocation will be null when adding the characters to the scene initially
			CoordinateData oldCoord = sceneData.new Breakaway().new CoordinateData();
			oldCoord.gridLocationX = oldLocation.x;
			oldCoord.gridLocationY = oldLocation.y;
			oldBreakawayArray = sceneData.breakaways.stream().filter(x -> x.activeCoordinates.stream().anyMatch(
					c -> c.gridLocationX == oldCoord.gridLocationX && c.gridLocationY == oldCoord.gridLocationY)).toArray(Breakaway[]::new);
		}
		List<Breakaway> previouslyOccupiedBreakaways = null;
		if(oldBreakawayArray != null)
			previouslyOccupiedBreakaways = (List<Breakaway>)Arrays.asList(oldBreakawayArray);
		
		CoordinateData newCoord = sceneData.new Breakaway().new CoordinateData();
		newCoord.gridLocationX = newLocation.x;
		newCoord.gridLocationY = newLocation.y;
		Breakaway[] newBreakawayArray = sceneData.breakaways.stream().filter(x -> x.activeCoordinates.stream().anyMatch(
				c -> c.gridLocationX == newCoord.gridLocationX && c.gridLocationY == newCoord.gridLocationY)).toArray(Breakaway[]::new);
		List<Breakaway> newlyOccupiedBreakaways = null;
		if(newBreakawayArray != null)
			newlyOccupiedBreakaways = (List<Breakaway>)Arrays.asList(newBreakawayArray);
		
		//Debugging the "entered new breakaway zone" logic
		System.err.println("DEBUGGING - Breakaway Zone Detection for point: " + newLocation);
		for(Breakaway breakaway : sceneData.breakaways) {
			System.out.println("Breakaway w/ correspondingRowIndex: " + breakaway.correspondingRowIndex);
			for(CoordinateData coord : breakaway.activeCoordinates) {
				System.out.println("coord: " + coord.gridLocationX + ", " + coord.gridLocationY);
			}
		}
		
		
		int rowCount = sceneData.sceneHeight;
		
		if(previouslyOccupiedBreakaways != null) {
			for(Breakaway previouslyOccupiedBreakaway : previouslyOccupiedBreakaways) {
				//If we're still in this breakaway zone so nothing needs to change for this breakaway
				if(newlyOccupiedBreakaways != null && newlyOccupiedBreakaways.contains(previouslyOccupiedBreakaway))
					continue;
				
				//use sublayerDepthManagers to get the scenePane layer, we need to access the breakaway layer via inverted coords
				int invertedLayer = rowCount - 1 - previouslyOccupiedBreakaway.correspondingRowIndex;
				int firstBreakawayIndexOfThisRow = sublayerDepthManagers.get(invertedLayer).GetStartIndexForSublayer(SublayerType.Breakaway);
				int sublayerIndex = (int)breakawaySublayerIndexMap.get(previouslyOccupiedBreakaway);
				int thisBreakawaysIndex = firstBreakawayIndexOfThisRow + sublayerIndex;
				
				int occupantCount = (int)breakawayOccupancyMap.get(previouslyOccupiedBreakaway);
				occupantCount--;
				breakawayOccupancyMap.put(previouslyOccupiedBreakaway, occupantCount);
				
				if(occupantCount == 0)
					scenePane.getComponentsInLayer(invertedLayer)[thisBreakawaysIndex].setVisible(true);
			}
		}	
		
		//Check new location to see if they've moved onto a breakaway location
		if(newlyOccupiedBreakaways != null) {
			for(Breakaway newlyOccupiedBreakaway : newlyOccupiedBreakaways) {
				//use sublayerDepthManagers to get the scenePane layer, we need to access the breakaway layer via inverted coords
				int invertedLayer = rowCount - 1 - newlyOccupiedBreakaway.correspondingRowIndex;
				int firstBreakawayIndexOfThisRow = sublayerDepthManagers.get(invertedLayer).GetStartIndexForSublayer(SublayerType.Breakaway);
				int sublayerIndex = (int)breakawaySublayerIndexMap.get(newlyOccupiedBreakaway);
				int thisBreakawaysIndex = firstBreakawayIndexOfThisRow + sublayerIndex;
				
				//If we're still in this breakaway zone don't tally a new occupant
				if(previouslyOccupiedBreakaways == null || !previouslyOccupiedBreakaways.contains(newlyOccupiedBreakaway)) {
					int occupantCount = 0;
					if(breakawayOccupancyMap.containsKey(newlyOccupiedBreakaway))
						occupantCount = (int)breakawayOccupancyMap.get(newlyOccupiedBreakaway);
					occupantCount++;
					breakawayOccupancyMap.put(newlyOccupiedBreakaway, occupantCount);
				}
				
				scenePane.getComponentsInLayer(invertedLayer)[thisBreakawaysIndex].setVisible(false);
			}
		}
		
		System.out.println("MapLocationPanel.HandleBreakawayForCharacterMove() - Scene breakaway count: " + sceneData.breakaways.size() +
				", Leaving Breakaway Zone: " + (previouslyOccupiedBreakaways != null && previouslyOccupiedBreakaways.size() > 0) +
				", Entering Breakaway Zone: " + (newlyOccupiedBreakaways != null && newlyOccupiedBreakaways.size() > 0) +
				", Moving from: " + oldLocation + " to: " + newLocation);
	}
	
	/**
	 * Get the index of each breakaway relative to other breakaways on the same layer, when there are multiple per row then they are arranged in the order of their correspondingImageLayerIndex.
	 */
	Map<Breakaway,Integer> breakawaySublayerIndexMap;
	
	//General purpose method called by GUIManager.ShowScreen() showing this panel
	public void OnPanelShown() {
		//Notify Game so that it can track whether the player ended the game in the MapLocation panel or the Worldmap panel
		Game.Instance().SetIsInMapLocation(true);
		
		//If this method is called during a fresh LoadGame state then OnEnterLocation() hasn't been called yet but will be later so wait for it to handle everything
		if(currentLocation != null) {
			if(activeMission != null)
				worldmapPanel.HandleMissionIndicator(activeMission);
			
			Reset();
			
			System.out.println("MapLocationPanel.OnPanelShown() - Calling StartDialography()");
			StartDialography(null);
			
			StartUpdateLoop();
		}
	}
	
	//Activity Methods - Start
	
	private void SetLocationInfoPanelsVisible(boolean visible) {
		//Banner Section
		boolean leaveVisible = visible && !hasDisabledMovement;
		leaveButton.setVisible(leaveVisible);
		leaveBG.setVisible(leaveVisible);
		
		locationTitle.setVisible(visible);
		titleBG.setVisible(visible);
		
		//Description Section
		locationDescription.setVisible(visible);
		descBG.setVisible(visible);
	}
	
	private void SetInteractionPanelVisible(boolean visible) {
		//Interact Section
		interactTitle.setVisible(visible);
		interactionOptions.setVisible(visible);
		interactBG.setVisible(visible);
	}
	
	private void SetDialogPanelVisible(boolean visible) {
		//Shift the position of the scenePane up if visible, otherwise move it back down to its default location
		int yLoc = visible ? dialogSceneLocY : defaultSceneLocY;
		scenePane.setBounds(scenePane.getLocation().x, yLoc, scenePane.getSize().width, scenePane.getSize().height);
		
		System.out.println("MapLocationPanel.SetDialogPanelVisible() - visible: " + visible + ", currentInteraction exists: " + (currentInteraction != null));
		interactionDialogLabel.setVisible(visible && (currentInteraction != null || mostRecentInteractionTypeFromLoad != null));
		dialogText.setVisible(visible);
		dialogNextButton.setVisible(visible);
		dialogBackButton.setVisible(visible);
		proceedButton.setVisible(visible);
		leftPortrait.setVisible(visible);
		leftPortraitName.setVisible(visible);
		rightPortrait.setVisible(visible);
		rightPortraitName.setVisible(visible);
		dialogBG.setVisible(visible);
	}
	
	
	//MapLocation Scene Assembly Debugger - Start
	
	TreeMap<String, MapLocation> DEBUG_instantiatedMapLocationMap = new TreeMap<String, MapLocation>();
	
	private JPanel DEBUG_CreateSceneSelectionPanel(Dimension paneSize) {
		//Populate the HashMap using the SaveData's list of MapLocation IDs
		for(String id : Game.Instance().DEBUG_GetSaveDatasSceneSelections()) {
			MapLocation mapLoc = GUIManager.WorldmapPanel().GetMapLocationById(id);
			if(mapLoc == null) {
				System.err.println("MapLocationPanel.DEBUG_CreateSceneSelectionPanel() - Couldn't find MapLocation!");
				continue;
			}
			DEBUG_instantiatedMapLocationMap.put((mapLoc.getTileType() == null ? "null" : mapLoc.getTileType().toString()) + (mapLoc.getSettlementType() == null ? "" : " + " + mapLoc.getSettlementType().toString()), mapLoc);
		}
		
		Dimension panelSize = new Dimension(300, 800);
		Dimension buttonSize = new Dimension(-1, 80);
		JPanel selectionPanel = new JPanel(new BorderLayout());
		selectionPanel.setOpaque(false);
		selectionPanel.setBackground(new Color(0,0,0,0));
		selectionPanel.setPreferredSize(panelSize);
		selectionPanel.setBounds(0, paneSize.height/2 - (panelSize.height/2), panelSize.width, panelSize.height);
		selectionPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.RED, Color.BLUE));
		
		String[] mapLocationTypeNames = DEBUG_instantiatedMapLocationMap.keySet().stream().toArray(String[]::new);
		JList<String> selectionList = new JList<String>(mapLocationTypeNames);
		selectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selectionList.setLayoutOrientation(JList.VERTICAL);
		JScrollPane scrollPane = new JScrollPane(selectionList);
		scrollPane.setPreferredSize(new Dimension(-1, panelSize.height - buttonSize.height));
		selectionPanel.add(scrollPane, BorderLayout.CENTER);
		
		JButton enterButton = new JButton("Enter");
		enterButton.setMinimumSize(buttonSize);
		enterButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				OnEnterLocation(DEBUG_instantiatedMapLocationMap.get(mapLocationTypeNames[selectionList.getSelectedIndex()]));
			}
		});
		selectionPanel.add(enterButton, BorderLayout.SOUTH);
		
		return selectionPanel;
	}
	
	//MapLocation Scene Assembly Debugger - End
	
	
	public void OnEnterLocation(MapLocation newLocation) {
		System.out.println("MapLocationPanel.OnEnterLocation() - newLocation: " + newLocation.getName() + ", CustomBgImagePath: " + newLocation.getCustomBgImagePath());

		//Update Location UI
		locationTitle.setText(newLocation.getName());
		
		//descriptionTextArea.setText(newLocation.getDescription());
		
		//Get either a custom image or the terrain tile image otherwise
		if(newLocation.getCustomBgImagePath() != null) {
			locationBG.SetNewImage(newLocation.getCustomBgImagePath());
		} else {
			if(newLocation.getTileType() == null)
				locationBG.SetNewImage(SpriteSheetUtility.getTerraingroupBlank()[0]);
			else
				locationBG.SetNewImage(SpriteSheetUtility.GetRandomTerrainFromWorldTile(newLocation.getTileType()));
		}
		//set the imagePanel to the correct aspect and center it if its a tile image being used
		if(newLocation.getCustomBgImagePath() == null) {
			locationBG.ConformSizeToAspectRatio(true);
			locationBG.setBounds(0, Math.min(0, -Math.round(locationBG.getSize().height * ((float)2/3)) + (locationBG.getParent().getSize().height / 2)),
							 	 locationBG.getSize().width, locationBG.getSize().height);
		} else {
			//set the image back to filling the screen when its a rectangular dimension image
			locationBG.setBounds(0, 0, locationBG.getParent().getSize().width, locationBG.getParent().getSize().height);
		}
		
		SetLocationInfoPanelsVisible(true);
		SetDialogPanelVisible(false);
		
		currentLocation = newLocation;
		//currentLayerInteractions = currentLocation.getInteractions();
		
		//Prompts a reassessment of next available missions on the next call to Missions.GetNextAvailableMissions()
		Missions.ReassessNextAvailableMissions();
		Game.Instance().IterateVisitedLocationsSinceLastMissionCounter();
		
		//Inject mission interactions here so that they'll be available for the BuildInteractionHandlers() method
		for(Mission nextMission : Missions.GetNextAvailableMissions())
			System.out.println("MapLocationPanel.OnEnterLocation() - Next Mission: " + nextMission.getName());
		
		//TODO - Below - Add alternate logic for a GenericLocationSettlementType counterpart for FlexibleTransitions and, possibly, tertiary logic to support both Nature & Settlement types simultaneously
		
		Mission[] activeMissions = null;
		if(Missions.GetNextAvailableMissions() != null)
			activeMissions = new ArrayList<>( Arrays.asList(Missions.GetNextAvailableMissions()) ).stream()
									.filter(x ->
										(x.getMapLocationId() != null && x.getMapLocationId().equals(currentLocation.getId()))
										||
										(
											x.getMapLocationId() == null
											&&
											!currentLocation.getWorldTileData().isUniqueLocation
											&&
											currentLocation.getSettlementType() == null
											&&
											x.getGenericLocationEnvironmentType() == currentLocation.getEnviType()
										)
			).toArray(Mission[]::new);
		
		if(activeMissions != null && activeMissions.length > 0) {
			for(Mission mission : activeMissions)
				System.out.println("MapLocationPanel.OnEnterLocation() - Found Available Mission at this location: " + mission.getName());
			
			//Pick a mission to experience on this entering of the MapLocation
			System.err.println("MapLocationPanel.OnEnterLocation() - Current logic is choosing to activate the first mission the activeMission list, implement more sofisticated logic for this choice.");
			//TODO Implement more sofisticated logic when choosing which mission gets activated
			//maybe down the road let the player choose which mission they'd like to do when entering a MapLocation
			activeMission = activeMissions[0];
			
			if(activeMission.getMapLocationId() == null) {
				//Overwrite the generic MapLocation and set its mission stuff, etc
				WorldTileData matchingWorldTileData = Game.Instance().GetWorldmapData().GetWorldMapDatas().values().stream().filter(
						x -> !x.isUniqueLocation && x.enviType == activeMission.getGenericLocationEnvironmentType() && x.tileType == activeMission.getSourceTileType() && x.settlementType == null
						).findFirst().orElse(null);
				if(matchingWorldTileData == null) {
					System.err.println("MapLocationPanel.OnEnterLocation() - Can't find a tile of the desired type existing on the map, other approaches may need to used to generate a MapLocation for this"
							+ " flexible transition scene swap. The MapLocation we just entered will remain as it was which will cause errors and fail to facilitate the active mission properly.");
				} else {
					MapLocation overridingLocation = new MapLocation( matchingWorldTileData.mapLocation );
					//Get this up here so we can use the existing location's id
					WorldTile worldTile = worldmapPanel.worldMap.get(worldmapPanel.GetPlayerSpriteLocation());
					
					//Merge some details to make the mismatching less heinous
					currentLocation = new MapLocation(
						worldTile.GetMapLocation().getId(),
						overridingLocation.getEnviType(),
						//overridingLocation.getTileType(),
						//It seems more natural to leave this tile as the tileType we found it. This could cause problems with Mission logic where location tileType is taken into account
						currentLocation.getTileType(),
						null,
						null,
						currentLocation.getName(),
						currentLocation.getDescription(),
						overridingLocation.getRelativeSceneDirectory(),
						overridingLocation.getCustomBgImagePath(),
						overridingLocation.GetInteractionManager().getInteractions(),
						overridingLocation.getMapLocationStructure().interactionlessDialographyDatas,
						overridingLocation.getSceneLayeringType(),
						overridingLocation.getMapLocationStructure()
					);
					
					//Update all the instances and references to the new MapLocation and its new Mission
					WorldTileData worldTileData = Game.Instance().GetWorldmapData().GetWorldMapDatas().get(worldmapPanel.GetPlayerSpriteLocation());
					worldTileData.enviType = currentLocation.getEnviType();
					worldTileData.mapLocation = currentLocation;
					worldTileData.tileType = currentLocation.getTileType();
					worldTileData.position = worldmapPanel.GetPlayerSpriteLocation();
					currentLocation.SetWorldTileData(worldTileData);
					
					worldTile.OverwriteMapLocation(currentLocation, worldmapPanel.GetPlayerSpriteLocation());
					
					worldTile.AddMission(activeMission);
					worldmapPanel.worldMap.put(worldmapPanel.GetPlayerSpriteLocation(), worldTile);
					Game.Instance().GetWorldmapData().GetWorldMapDatas().put(worldmapPanel.GetPlayerSpriteLocation(), worldTile.getData());
					
					//Show the MissionIndicator for this mission occurance on the worldmap, and do it before activeMission.SetMissionStatus() because we want to display the pendingIndicator
					worldmapPanel.AddTileToMissionIndicatedTiles(worldmapPanel.GetPlayerSpriteLocation());
					worldmapPanel.IndicateActiveMissionOnCurrentTile();
				}
			}
			
			//Initiate this mission and, if its activating as the result of a flexible transition, consequently remove it from being considered as an eligible FlexibleTransition in the future
			if(activeMission.getMissionStatus() != MissionStatusType.Active)
				activeMission.SetMissionStatus(MissionStatusType.Active);
			
			//Inject interactions from activeMissionStructure
			currentInteractionManager = activeMission.GetInteractionManager();
			System.out.println("MapLocationPanel.OnEnterLocation() - Mission Interactions: " + currentInteractionManager.getInteractions().length);
		} else {
			System.out.println("MapLocationPanel.OnEnterLocation() - No Available Missions were found for location: " + currentLocation.getName());
			activeMission = null;
			currentInteractionManager = currentLocation.GetInteractionManager();
		}
		//this has been repaced by currentInteractionManager.GetCurrentInteractionLayers() down below
		//currentLayerInteractions = currentInteractionManager.getInteractions();
		
		//TODO - Above - Add additional support for a GenericLocationSettlementType counterpart for FlexibleTransitions
		
		//graphPath.clear();
		interactionLayerDepth = 0;
		//BuildInteractionHandlers(); //this is handled inside of InteractionManager now
		
		worldmapPanel.RefreshPaths();
		
		//Update Interaction UI after we've loaded or refresh paths
		for(InteractionType type : InteractionType.values()) {
			interactionButtonMap.get(type).setVisible(false);
		}
		
		hasDisabledMovement = false;
		hasInteractionBeenApplied = false;
		
		//GraphPathNode[] currentGraphPath = Game.Instance().GetCurrentGraphPath();
		//if(currentGraphPath != null && currentGraphPath.length > 0) {
			//graphPath = new ArrayList<GraphPathNode>();
			//Collections.addAll(graphPath, currentGraphPath);
			
			//I think theres a sutble but crucial offset of the interactionLayerDepth and the graphPath.size().
			//Interaction 0 represents the base layer while, imcomparably, the lack of graphPath represents 0 choices and a graphPath.size() of 1 represents
			//a choice between layer 0 to layer 1. Therefore, a graphPath.size() of 1 should equate to the display of Layer 1
			//interactionLayerDepth = graphPath.size() - 1;
		//if(currentInteractionManager.getGraphPath() != null)
			//interactionLayerDepth = currentInteractionManager.getGraphPath().size();
			//use a consistent method of searching the interaction tree
			//By the same logic this is flawed as well
			//int secondToLastIndex = graphPath.size()-2;
			//int secondToLastIndex = graphPath.size()-1;
			//GraphPathNode secondToLastNode = graphPath.get(secondToLastIndex);
			//Interaction secondToLastIntr = GetInteractionAt(secondToLastIndex, secondToLastNode.choiceType);
			//currentLayerInteractions = graphPath.get(graphPath.size()-1).wasSuccessfulOutcome ? secondToLastIntr.NextInteractions_OnSuccess() : secondToLastIntr.NextInteractions_OnFailure();
		//}
		//Either get the interactions based on a blank-slate entering of the MapLocation(uses InteractionManager.GetCurrentInteractionLayers()) or we get them based on a pre-existing GraphPath
		List<Interaction> activeIntrs = new ArrayList<Interaction>();
		if(currentInteractionManager.getGraphPath() == null || currentInteractionManager.getGraphPath().size() == 0) {
			//This array is a remnant of older interaction logic
			//currentLayerInteractions = currentInteractionManager.GetCurrentInteractionLayers();
			//for(Interaction intr : currentLayerInteractions)
			for(Interaction intr : currentInteractionManager.GetCurrentInteractionLayers())
				activeIntrs.add(intr);
		} else {
			interactionLayerDepth = currentInteractionManager.getGraphPath().size();
			activeIntrs.addAll( currentInteractionManager.GetActiveInteractions(interactionLayerDepth) );
			//This array is a remnant of older interaction logic
			//currentLayerInteractions = activeIntrs.stream().toArray(Interaction[]::new);
		}
		
		System.out.println("MapLocationPanel.OnEnterLocation() - activeMission: " + (activeMission == null ? "null" : activeMission.getName() + ", has entry dialog: " + (activeMission.GetDialography_entry() != null)));
		
		descriptionTextArea.setText(activeMission != null ? activeMission.GetDialography_entry().locationDescSummary : newLocation.getDescription());
		for(int i = 0; i < currentInteractionManager.getGraphPath().size(); i++) {
			int finalI = i;
			Interaction intr = currentInteractionManager.getInteractionHandlers().stream().filter(x -> x.layerDepth() == finalI && x.type() == currentInteractionManager.getGraphPath().get(finalI).choiceType).findFirst().get().intr();
			String outcomeDesc = "";
			if(currentInteractionManager.getGraphPath().get(i).wasSuccessfulOutcome) {
				if(intr.DialographyData_OnSuccess().locationDescSummary != null)
					outcomeDesc = intr.DialographyData_OnSuccess().locationDescSummary;
				else
					outcomeDesc = intr.DialographyData_Pre().locationDescSummary;
			} else {
				if(intr.DialographyData_OnFailure().locationDescSummary != null)
					outcomeDesc = intr.DialographyData_OnFailure().locationDescSummary;
			}
			if(outcomeDesc != "") //Keep the return out of the String if theres no desc for this intr
				outcomeDesc = "\n" + outcomeDesc;
			descriptionTextArea.append("\n* " + intr.Type() + " *" + outcomeDesc);
		}
		
		//Show Tree Intrs
		//Up above we either get the interactions based on a blank-slate entering of the MapLocation(uses InteractionManager.GetCurrentInteractionLayers()) or we get them based on a pre-existing GraphPath
		//List<Interaction> activeIntrs = currentInteractionManager.GetActiveInteractions(interactionLayerDepth);
		for(Interaction interaction : activeIntrs) {
			if(!hasDisabledMovement && interaction.DoesBlockMapMovement())
				hasDisabledMovement = true;
			interactionButtonMap.get(interaction.Type()).setVisible(true);
			
			System.out.println("MapLocationPanel.OnEnterLocation() - VISIBLE interaction: " + interaction.Type());
		}
			
		SetInteractionPanelVisible(activeIntrs != null && activeIntrs.size() > 0);
		
		//This probably needs to happen before AssembleScene() because I believe thats the current order of operations during initialization / game start
		//Initially I caught this as a missing piece for the resetting of Dialography system but i realized it should actual be happening before other lower level systems setup(scene imagery, etc)
		SetupSceneVariables();
		
		//Update scene tilemap images
		AssembleScene();
		
		if(activeMission != null && activeMission.GetDialography_entry() == null)	
			System.err.println("MapLocationPanel.OnEnterLocation() - The entry dialographyData is null; there's nothing to play when entering the activeMission: " + activeMission.getName());
		else if(activeMission == null && currentLocation.GetDialography_default() == null)
			System.err.println("MapLocationPanel.OnEnterLocation() - The default dialographyData is null; there's nothing to play when entering the currentLocation: " + currentLocation.getName());
		
		//if we are already viewing the MapLocationPanel then play the new dialography, otherwise wait till OnPanelShown()
		if(GUIManager.getCurrentMenuType() == MenuType.LOCATION) {
			if(activeMission != null)
				worldmapPanel.HandleMissionIndicator(activeMission);
			
			//[MISSION_FLOW_EDIT]
			UnresolvedInteractionData unresolvedInteractionData = currentInteractionManager.getUnresolvedInteractionData();
			if(unresolvedInteractionData != null && !unresolvedInteractionData.IsPreTestNotMidTest()) {
			//Restore the battle or other intermediate activity: camping animation, etc
				//Restore activity
				if(unresolvedInteractionData.getCurrentBattleState() != null) {
					System.out.println("MapLocationPanel.OnEnterLocation() - Restoring battle");
					
					currentInteraction = unresolvedInteractionData.getInteraction();
					hasInteractionBeenApplied = false;
					mostRecentInteractionTypeFromLoad = unresolvedInteractionData.getInteraction().Type();
					
					InitiateBattle(unresolvedInteractionData.getCurrentBattleState());
				} else {
					System.err.println("MapLocationPanel.OnEnterLocation() - STUB - Restore other activity(other than battle activity)");
					//TODO figure out what this activity is and restore it
					
				}
			} else {
			//Do a dialography to start
				
				SetLocationInfoPanelsVisible(false);
				SetInteractionPanelVisible(false);
	
				SetDialogPanelVisible(true);
				
				//Play the entry/default or most recent dialography now that we've entered a new location
				Reset();
				System.out.println("MapLocationPanel.OnEnterLocation() - Calling StartDialography()");
				StartDialography(null);
				
			}
		}
	}
	
	/**
	 * Get the manditory equipment of the current Interaction. These are to be forced onto characters when they're chosen to participate in the upcoming battle.
	 * @return
	 */
	public ItemData[] getManditoryEquipmentForBattle() {
		if(CombatAnimPane.DEBUG_manditoryEquipment() != null)
			return CombatAnimPane.DEBUG_manditoryEquipment();
		else
			return currentInteraction.manditoryEquipment;
	}
	
	
	//private void HandleInteraction(InteractionType type, JButton interactionButton) {
	private void HandleInteraction(InteractionType type, CustomButtonUltra interactionButton) {
		Reset();
		
		//Reset stuff
		hasInteractionBeenApplied = false;
		
		currentInteraction = currentInteractionManager.GetInteractionAt(interactionLayerDepth, type);
		
		System.out.println("MapLocationPanel.HandleInteraction() - interactionLayerDepth: " + interactionLayerDepth + ", type: " + type + 
				", GetInteractionAt() return exists: " + (currentInteraction != null) + ", interactionType: " + (currentInteraction != null ? currentInteraction.Type() : ""));
		
		//[MISSION_FLOW_EDIT]
		currentInteractionManager.RecordUnresolvedInteractionState(currentInteraction, true);
		
		//Disable UI until this interaction is resolved
		for(CustomButtonUltra but : interactionButtonMap.values()) {
			but.setEnabled(false);
		}
		//interactionButton.SetIdleColor(Color.GREEN);
		
		//Setup dialog panel
		this.interactionDialogLabel.setText(type.toString());
		
		//Play interaction's dialographies, Pre-Dialogs are optional so if its null for this interaction then Continue the interaction so the success or fail can be determined and the appropriate dialogs played
		if(currentInteraction.DialographyData_Pre().dialographyName != null && !currentInteraction.DialographyData_Pre().dialographyName.isEmpty())
			StartDialography(currentInteraction.DialographyData_Pre());
		else
			ContinueInteraction();
	}
	
	/**
	 * Used to offset settlement actors' locations based on the settlement layer's offset from the nature layer.
	 * @return Point2D - With the grid coordinate offset.
	 */
	public Point2D.Float getSettlementLayerOffset() {
		return new Point2D.Float(this.sceneData.settlementSceneOffsetX, this.sceneData.settlementSceneOffsetY);
	}
	
	private void Reset() {
        currentDialogFrame = 0;
        lastIndex = 0;
        forwardProgress = 0;
        isLockedByActor = false;
        animatingActors.clear();
        
        //This will update all animated entities in the scene, which the actors especially need for their animspeed modifiers
        ResetUpdateLoop();
    }
	
	//Use this to clear the presence of battle characters, etc at the start of the dialography proceeding a battle
	private boolean battleElementsPresentInScene_dirtyFlag;
	
	private void StartDialography(DialographyData dialographyData) {
		if(this.battleElementsPresentInScene_dirtyFlag) {
			this.battleElementsPresentInScene_dirtyFlag = false;
			Game.Instance().GetBattlePanel().RemoveBattleElementsForDialography();
		}
		
		
		//Null DialographyDatas are exported as blank instances so make it obvious for the following logic that the dialography is actually null
		if(dialographyData != null && dialographyData.dialographyName.isEmpty()) {
			System.out.println("MapLocationPanel.StartDialography() - Clearing what is assumed to be a blank Dialography");
			dialographyData = null;
		}
		
		//Setup first Dialography
		//get the current Dialography, it could be a MapLocation_Default or a MapLocation_[SpecialCase] or a Mission_Entry or a Mission_Pre
		//and then challenge that entryOrDefaultDialography with the most recent, fresh dialography the player experienced before exiting and loading the game
		if(dialographyData == null) {
			//figure out the dialography by:
			//*(If their are Missions in this MapLocation)
			if(activeMission != null) {
				System.out.println("MapLocationPanel.StartDialography() - dialographyData == null && activeMission != null");
				
				dialographyData = activeMission.getMissionStructure().interactionlessDialographyDatas.stream().filter(x -> x.dialographyName.endsWith("_Entry")).findFirst().orElse(null);
			//*(If there aren't Mission in this MapLocation or our current mission doesnt match)
			} else {
				System.out.println("MapLocationPanel.StartDialography() - dialographyData == null && activeMission == null");

				String specialCase = Game.ConsumeSpecialCase_DialographySuffix();
				if(specialCase != null)
					dialographyData = currentLocation.GetDialography_specialCase("_" + specialCase);
				else
					dialographyData = currentLocation.GetDialography_default();
			}
			dialographyData = GetMostRecentDialographyOrFallback(dialographyData, true);
		}
		currentDialography = dialographyData;
		
		System.out.println("MapLocationPanel.StartDialography() - Starting Dialography: " + dialographyData.dialographyName);
		
		//setup the actors
		animatingActors = new ArrayList<Actor>();
		for(Actor actor : sceneActors) {
			ActorPathData pathData = dialographyData.actorPathDatas.stream().filter(x -> x.actorId.equals(actor.getActorData().actorId)).findFirst().orElse(null);
			if(pathData != null) {				
				actor.SetActorPath(pathData, sceneLayoutInfo);
				
				//TODO track subLayerDepth here based on the start position for this ActorPath
				int safeIndex = Math.min( Math.max(actor.getCurrentRow()-2, 0), sublayerDepthManagers.size()-1 );
				sublayerDepthManagers.get(safeIndex).TrackOccupantActivity(SublayerType.ActorOrProp, true);
				
				if(!pathData.isLoopPath)
					animatingActors.add(actor);
				
				
				//DEBUGGING - This seems to fix a bug by updating their row now that we've been placed in the scene. The bug beign addressed is one that changes the character's
				//incorrectly when starting a new dialography.
				UpdateActorRow(actor.getImagePanel(), actor.getCurrentRow(), actor.getCurrentRow(), true, actor.getActorData().actorId);
				
				
			} else
				actor.Deactivate();
		}
		
		if (dialographyData.dialogLines.length > 0)
            StartDialog(dialographyData);
        else
            SetDialogPanelVisible(false);
		
		//If we're starting a dialography from a fresh LoadGame state then this hasn't been called yet
		if(!this.sceneAnimation_dirtyFlag)
			StartUpdateLoop();
	}
	
	/**
	 * Used to record and set the interactionDialogLabel GUI element when the the intial dialography for this MapLocation isn't a default, entry or special dialography.
	 */
	private InteractionType mostRecentInteractionTypeFromLoad;
	/**
	 * Challenge the entry or default dialography with the most recent, fresh dialography the player experienced before exiting and loading the game, according to the InteractionManager's graphPath.
	 * This method also sets a member variable for the most recent interaction choice that'll be used to set the interactionDialogLabel GUI element in SetDialog().
	 * @param entryOrDefaultFallback - The initial dialography that'd play for this MapLocation/Mission the first time it's entered or other times after its been reset
	 * @return - Either the most recent dialography experienced(according to the graphPath) or the entryOrDefaultFallback
	 */
	private DialographyData GetMostRecentDialographyOrFallback(DialographyData entryOrDefaultFallback, boolean loadCurrentInteractionFromPreviousInteraction) {
		DialographyData previousDialographyData = null;
		UnresolvedInteractionData unresolvedInteractionData = currentInteractionManager.getUnresolvedInteractionData();
		//This is looking at the last choice AND now it's also looking whether the recent mission was resolved -OR- whether theres a unresolvedInteractionData
		if(
			(
				currentInteractionManager.getGraphPath() != null && currentInteractionManager.getGraphPath().size() > 0
				//[MISSION_FLOW_EDIT]
				&&
				(activeMission == null || (activeMission != null && activeMission.getMissionStatus() == MissionStatusType.Active))
			)
			||
			(
				unresolvedInteractionData != null
			)
		) {
			//[MISSION_FLOW_EDIT]
			Interaction previousInteraction = null;
			if(unresolvedInteractionData != null) {
				//If we've made it this far we don't have to worry about supporting the MidTest unresolvedInteractionData cause that logic already branched off before reaching this method, at OnEnterLocation
				previousInteraction = unresolvedInteractionData.getInteraction();
				previousDialographyData = unresolvedInteractionData.getInteraction().DialographyData_Pre();
				mostRecentInteractionTypeFromLoad = unresolvedInteractionData.getInteraction().Type();
			} else {
				
				int lastGraphPathIndex = currentInteractionManager.getGraphPath().size() - 1;
				GraphPathNode lastGraphPathNode = currentInteractionManager.getGraphPath().get(lastGraphPathIndex);
				previousInteraction = currentInteractionManager.GetInteractionAt(lastGraphPathIndex, lastGraphPathNode.choiceType);
				if(lastGraphPathNode.wasSuccessfulOutcome && previousInteraction.DialographyData_OnSuccess() != null)
					previousDialographyData = previousInteraction.DialographyData_OnSuccess();
				else if(!lastGraphPathNode.wasSuccessfulOutcome && previousInteraction.DialographyData_OnFailure() != null)
					previousDialographyData = previousInteraction.DialographyData_OnFailure();
				else
					previousDialographyData = previousInteraction.DialographyData_Pre();
				mostRecentInteractionTypeFromLoad = lastGraphPathNode.choiceType;
				
			}
			
			//[MISSION_FLOW_EDIT]
			//This used when loading unresolved interaction states, when resolving an interaction it must seem as though the interaction is presently occuring
			//The condition promotes awareness to all sources calling this method
			if(loadCurrentInteractionFromPreviousInteraction) {
				currentInteraction = previousInteraction;
				//If we're loading an unresolved interaction then we know that it's been applied because its applied the same moment its recorded. This will help us get past continue and to resolution
				hasInteractionBeenApplied = unresolvedInteractionData == null;
			}
			
			System.out.println("MapLocationPanel.GetMostRecentDialographyOrFallback() - Found a recent dialography from last interaction type: " + mostRecentInteractionTypeFromLoad);
		}
		return previousDialographyData != null ? previousDialographyData : entryOrDefaultFallback;
	}
	
	private void StartDialog(DialographyData dialographyData) {
		currentDialogFrame = 0;
		
		//Hide other panels
		SetLocationInfoPanelsVisible(false);
		SetInteractionPanelVisible(false);
		
		//Setup Dialog elements
		if(currentInteraction == null) {
			if(mostRecentInteractionTypeFromLoad == null)
				interactionDialogLabel.setVisible(false);
			else {
				interactionDialogLabel.setText(mostRecentInteractionTypeFromLoad.toString());
				interactionDialogLabel.setVisible(true);
			}
		} else {
			interactionDialogLabel.setText(currentInteraction.Type().toString());
			interactionDialogLabel.setVisible(true);
		}
		dialogNextButton.setEnabled(true);
		dialogBackButton.setEnabled(false);
		SetDialogPanelVisible(true);
		proceedButton.setVisible(false);
		
		//This indexing scheme is flawed
		/*if(!hasInteractionBeenApplied)
			lastIndex = currentInteraction.Dialog().dialogLines.length;
		else
			lastIndex = wasInteractionSuccessful ? currentInteraction.DialographyData_OnSuccess().dialogLines.length : currentInteraction.DialographyData_OnFailure().dialogLines.length;
		lastIndex = Math.max(0, lastIndex - 2);*/
		lastIndex = dialographyData.dialogLines.length - 1;

		//If theres only one line of dialog then prepare the buttons immediately
		if(lastIndex == 0) {
			System.out.println("StartDialog() - Show Proceed");
			proceedButton.setVisible(true);
			dialogNextButton.setEnabled(false);
			dialogBackButton.setEnabled(false);
		}
		
		SetDialogText();
	}
	
	//Called by the back and next buttons
	public void IterateDialogFrame(boolean forward) {
		if(!forward && currentDialogFrame == 1) {
			dialogBackButton.setEnabled(false);
		} else if(forward) {
			dialogBackButton.setEnabled(true);
		}
		
		int nextFrameIndex = currentDialogFrame + (forward ? 1 : -1);
		
		//if(forward && currentDialogFrame >= lastIndex) {
		if(forward && nextFrameIndex == lastIndex) {
			//if(!proceedButton.isVisible()) {
			if(AreAllActorsDone()) {
				proceedButton.setVisible(true);
				proceedButton.setEnabled(true);	
			}
			//if (!isLockedByActor)
            //    proceedButton.setEnabled(AreAllActorsDone());
			
			dialogNextButton.setEnabled(false);
		} else if(!forward) {
			dialogNextButton.setEnabled(true);
		}
		
		
		//Set Actors flags to continue their anims when they reach, or am currently in, a state of dialog stall
        if (forward && currentDialogFrame >= forwardProgress && currentDialography.dialogLines[currentDialogFrame].doesStallChoreo)
        {
            List<String> ignoredActorIds = new ArrayList<>( Arrays.asList(currentDialography.dialogLines[currentDialogFrame].ignoreActorIds) );
            for (ActorPathData actorPathData : currentDialography.actorPathDatas)
            {
                Actor actor = sceneActors.stream().filter(x -> x.getActorData().actorId.equals(actorPathData.actorId)).findFirst().get();
                //we also need to ignore charactrers in a loop cause if their loop state is overridden at some point then they'd have a unintented EndStall_Flag pending.
                if(!ignoredActorIds.stream().anyMatch(x -> x.equals(actorPathData.actorId)) && !actorPathData.isLoopPath)
                {
                    System.out.println("EndStall Notifier for: " + actorPathData.actorId);
                    actor.EndStall();
                }
            }
        }
		
		
		currentDialogFrame += forward ? 1 : -1;
		forwardProgress = currentDialogFrame > forwardProgress ? currentDialogFrame : forwardProgress;
		SetDialogText();
	}
	
	private void SetDialogText() {
		//DialogLine dialogLine = null;
		//if(!hasInteractionBeenApplied)
		//	dialogLine = currentInteraction.Dialog().dialogLines[currentDialogFrame];
		//else
		//	dialogLine = wasInteractionSuccessful ? currentInteraction.DialographyData_OnSuccess().dialogLines[currentDialogFrame] : currentInteraction.DialographyData_OnFailure().dialogLines[currentDialogFrame];
		DialogLine dialogLine = currentDialography.dialogLines[currentDialogFrame];
		
		//Setup portraits
		CharacterData charData = Missions.GetActorCharacter(currentLocation, dialogLine.actorId);
		
		/*if(charData != null) {
			if(charData.getName().equals("[PLAYER]"))
				charData = Game.Instance().GetPlayerData();
			
			leftPortrait.setVisible(dialogLine.useLeftPortrait);
			rightPortrait.setVisible(!dialogLine.useLeftPortrait);
			ImagePanel activePortrait = dialogLine.useLeftPortrait ? leftPortrait : rightPortrait;
			if(charData.getPortraitPath() == null || charData.getPortraitPath().equals(""))
				activePortrait.SetNewImage(SpriteSheetUtility.GetWalkSheet(charData.getType()).GetSprite(1, 1, 1));
			else
				activePortrait.SetNewImage(charData.getPortraitPath());
		} else {
			leftPortrait.setVisible(false);
			rightPortrait.setVisible(false);
		}*/
		String charName = "???";
		if(charData != null) {
			if(charData.getName().equals("[PLAYER]"))
				charData = Game.Instance().GetPlayerData();
			charName = charData.getName();
		}
		leftPortraitName.setVisible(dialogLine.useLeftPortrait);
		rightPortraitName.setVisible(!dialogLine.useLeftPortrait);
		JLabel activePortraitName = dialogLine.useLeftPortrait ? leftPortraitName : rightPortraitName;
		activePortraitName.setText(charName);
		leftPortrait.setVisible(dialogLine.useLeftPortrait);
		rightPortrait.setVisible(!dialogLine.useLeftPortrait);
		ImagePanel activePortrait = dialogLine.useLeftPortrait ? leftPortrait : rightPortrait;
		if(charData != null) {
			if(charData.getPortraitPath() == null || charData.getPortraitPath().equals(""))
				activePortrait.SetNewImage(SpriteSheetUtility.GetWalkSheet(charData.getType()).GetSprite(1, 1, 1));
			else
				activePortrait.SetNewImage(charData.getPortraitPath());
		} else {
			Actor actor = sceneActors.stream().filter(x -> x.getActorData().actorId.equals(dialogLine.actorId)).findFirst().orElse(null);
			if(actor == null)
				System.err.println("MapLocationPanel.SetDialogText() - There is no actor in sceneActors with id: " + dialogLine.actorId + ". Their dialog Portrait can't be set.");
			else
				activePortrait.SetNewImage(actor.getSpriteSheet().GetSprite(1, 1, 1));
		}
		//This will support a variety of different portraits, from Character Portraits to sprite sheets
		activePortrait.ConformSizeToAspectRatio(true);
		activePortrait.setBounds(activePortrait.getLocation().x, dialogPortrait_originalYPos + (dialogPortrait_originalHeight - activePortrait.getSize().height),
				activePortrait.getSize().width, activePortrait.getSize().height);
		
		//Set elements
		dialogText.setText(dialogLine.dialogText);
	}
	
	int dialogPortrait_originalHeight;
	int dialogPortrait_originalYPos;
	
	//New Dialography Behaviors - Start
	
	
    public void SetLock(boolean isLocked)
    {
    	System.out.println("MapLocationPanel.SetLock()");
    	
    	isLockedByActor = isLocked;

        dialogNextButton.setEnabled(!isLocked);
        proceedButton.setEnabled(!isLocked);
    }

    
    

    public void ActorDoneAnimating(Actor doneActor)
    {
        System.out.println("MapLocationPanel.ActorDoneAnimating() - actor: " + doneActor.getActorData().actorId);
    	
    	animatingActors.remove(doneActor);

        if (!isLockedByActor && forwardProgress == currentDialography.dialogLines.length-1) {
        	boolean enable = AreAllActorsDone();
        	proceedButton.setVisible(enable);
            proceedButton.setEnabled(enable);
        }

        //This is not specific enough. A false positive occurs when the actors end their paths before the dialog reaches the last frame.
        if (currentDialography.dialogLines.length == 0 && AreAllActorsDone()) {
        	if(currentInteraction != null)
        		ContinueInteraction();
        	else
        		ResolveInteractionlessDialography();
        }
    }
    
    private boolean AreAllActorsDone() {
        return animatingActors.size() == 0;
    }
    
    //New Dialography Behaviors - End
	
	//Called by UI Proceed button
	private void ContinueInteraction() {
		System.out.println("MapLocationPanel.ContinueInteraction() - currentInteraction.Type(): " + currentInteraction.Type());
		
		//Assuming the interaction is non-tested
		wasInteractionSuccessful = true;
		
		//Either resolve the current Interaction or move to next special step in order to complete the interaction: go to battle, etc
		boolean readyToMoveOn = true;
		
		//Check testing procedure
		
		if(currentInteraction.TestType() != null) {
			List<CharacterData> partyList =  new ArrayList<>(Arrays.asList(Game.Instance().GetPartyData()));
			switch(currentInteraction.TestType()) {
				case None:
					//Nothing to do, move on
					break;
				case StatTest:
					//Test main characters stat
					CharacterData mainCharacter = Game.Instance().GetPlayerData();
					if(mainCharacter.GetStatValue(currentInteraction.TestStat()) < currentInteraction.PassingStatValue())
						wasInteractionSuccessful = false;
					break;
				case Random:
					if((r.nextInt(101)/100f) > currentInteraction.ChanceToPass())
						wasInteractionSuccessful = false;
					break;
				case BattleOutcome:
					break;
				case ItemPossession:
					wasInteractionSuccessful = Game.Instance().TryConsumeInventoryItems(currentInteraction.RequiredItems());
					break;
				case SkillPossession:
					//Check all characters in party for skill
					Optional<CharacterData> optData = partyList.stream()
							.filter(x -> x.getType() == currentInteraction.RequiredClass() && x.GetStatValue(x.GetGoverningStat()) >= 4)
							.findFirst();
					if(optData == null || !optData.isPresent() || optData.get() == null)
						wasInteractionSuccessful = false;
					break;
				case ClassPossession:
					//Check all characters in party for class
					Optional<CharacterData> charData = partyList.stream()
							.filter(x -> x.getType() == currentInteraction.RequiredClass())
							.findFirst();
					if(charData == null || !charData.isPresent() || charData.get() == null)
						wasInteractionSuccessful = false;
					break;
				default:
					System.err.println("Add support for: " + currentInteraction.TestType());
					break;
			}
		}
		
		switch(currentInteraction.Type()) {
			case Explore:
				break;
			case Camp:
				readyToMoveOn = false;
				
				//Hide ui menus
				SetLocationInfoPanelsVisible(false);
				SetInteractionPanelVisible(false);
				SetDialogPanelVisible(false);
				
				//Do the whole campfire sequence in one big timer split into phases
				int nightfallTicks = 70;
				int sitByFireTicks = 40;
				//int fadeInAndOutTicks = 10;
				campfireTimer = new Timer(100, new ActionListener() {
					int timerPhase = 0;
					int currentTicks = 0;
					@Override
					public void actionPerformed(ActionEvent arg0) {
						if(timerPhase == 0) {
							//maybe show a stamina bar, it could tick up as time passes during the Camp sequence
							//if(currentTicks == 0)
							
							if(currentTicks >= -1) {
								currentTicks = 0;
								timerPhase++;
							} else
								currentTicks++;
						} else if(timerPhase == 1) {
							//anim the character into position near center scene
							//if(currentTicks == 0)
							
							if(currentTicks >= -1) {
								currentTicks = 0;
								timerPhase++;
							} else
								currentTicks++;
						} else if(timerPhase == 2) {
							//create fire object in center scene
							//if(currentTicks == 0)
							
							if(currentTicks >= -1) {
								currentTicks = 0;
								timerPhase++;
							} else
								currentTicks++;
						} else if(timerPhase == 3) {
							//transition to nightfall by blending in CampfireLightOverlay
							if(currentTicks == 0)
								campfireLightImagePanel.setVisible(true);
							
							float a = (float)currentTicks / nightfallTicks;
							campfireLightImagePanel.SetTint(new Color(1f, 1f, 1f, a), ColorBlend.Multiply);
							campfireLightImagePanel.repaint(80);
							System.out.println("a: " + a);
							
							if(currentTicks >= nightfallTicks) {
								currentTicks = 0;
								timerPhase++;
								System.out.println("Nightfall anim due, waiting for a good while by the camp fire...");
							} else
								currentTicks++;
						} else if(timerPhase == 4) {
							//Wait for a while (twice as long as traveling from one tile to another)
							if(currentTicks >= sitByFireTicks) {
								currentTicks = 0;
								timerPhase++;
							} else
								currentTicks++;
						} else if(timerPhase == 5) {
							//fade to black
							if(currentTicks == 0) {
								System.out.println("Next phase, fade to black");
								GUIManager.GetFadeTransitionPanel().Fade(true, 0, 40, 2, 10, new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent arg0) {
										//reset scene elements
										//hide stamina bar
										
										//return character back to mark
										
										//hide campfire
										
										//hide overlay
										campfireLightImagePanel.setVisible(false);
										//Show ui
										//SetLocationInfoPanelsVisible(true);
										//SetInteractionPanelVisible(true);
										//this should happen by default in the rest of the Interaction loop
										//( AppluyInteration() -> ResolveInteraction() -> OnEnterLocation() )
										
										ApplyInteraction(true);
										
										GUIManager.GetFadeTransitionPanel().Fade(false, 400);
									}
								});
							}
							
							/*if(currentTicks >= fadeInAndOutTicks) {
								currentTicks = 0;
								timerPhase++;
							} else
								currentTicks++;*/
							campfireTimer.stop();
						} /*else if(timerPhase == 6) {
							//reset scene elements
							//hide stamina bar
							
							//return character back to mark
							
							//hide campfire
							
							//hide overlay
							campfireLightImagePanel.setVisible(false);
							//Show ui
							SetLocationInfoPanelsVisible(true);
							SetInteractionPanelVisible(true);
							
							ResolveInteraction();
							
							//GUIManager.GetFadeTransitionPanel().Fade(false);
							campfireTimer.stop();
						}*/
					}
				});
				campfireTimer.setInitialDelay(100);
				campfireTimer.setRepeats(true);
				campfireTimer.start();
				break;
			case Steal:
				//Determine items to be recieved and award them
				
				break;
			case Search:
				
				break;
			case Talk:
				break;
			case Fight:
				readyToMoveOn = false;
				
				InitiateBattle(null);
				
				break;
			case Trade:
				readyToMoveOn = false;
				//Show trade ui
				
				break;
			case Flee:
				break;
			case Travel:
				break;
			default:
				System.err.println("Add support for: " + currentInteraction.Type().name());
				break;
		}
		
		if(readyToMoveOn)
			ApplyInteraction(wasInteractionSuccessful);
		else
			//[MISSION_FLOW_EDIT]
			currentInteractionManager.RecordUnresolvedInteractionState(currentInteraction, false);
	}
	
	/*
	 * Called by ContinueInteraction() or OnEnterLocation() when restoring a battle
	 */
	private void InitiateBattle(BattleState battleStateToRestore) {
		System.out.println("MapLocationPanel.InitiateBattle() - Restoring suspended battleState: " + (battleStateToRestore != null));
		
		//Hide dialog stuff and other UI stuff
		SetDialogPanelVisible(false);
		SetLocationInfoPanelsVisible(false);
		SetInteractionPanelVisible(false);
		//Hide actors, but before we do record the active ones
		activeActorsBeforeBattle = new ArrayList<Actor>();
		for(Actor actor : sceneActors) {
			if(actor.getImagePanel().isVisible())
				activeActorsBeforeBattle.add(actor);
			actor.Deactivate();
		}

		//[MISSION_FLOW_EDIT]
		//Instead of changing to another panel, do the battle stuff here by adding the battlePanel to the scenePane
		if(battleStateToRestore == null)
			Game.Instance().StartBattle(currentInteraction.BattleData());
		else
			Game.Instance().StartBattle(battleStateToRestore);
		
		BattlePanel battlePanel = Game.Instance().GetBattlePanel();
		battlePanel.setOpaque(false);
		battlePanel.setBackground(new Color(0,0,0,0));
		battlePanel.setSize(battlePanelContainer.getSize());
		battlePanel.setPreferredSize(battlePanelContainer.getSize());
		//battlePanel.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
		battlePanelContainer.add(battlePanel, BorderLayout.CENTER);
		
		battleElementsPresentInScene_dirtyFlag = true;
	}
	
	//Reward Item UI, used for Interaction GrantedItems and Mission Rewards
	ResultsPane resultsPane;
	//If there are both Interaction Results AND mission results then this will hold the information for the mission results panel to be shown directly after clicking "ok" on the interaction results.
	ActionListener missionResultsListener;
	
	//This will be called either internally from ContinueInteraction() or by Game once the battle is concluded
	public void ApplyInteraction(boolean wasSuccessful) {
		System.out.println("MapLocationPanel.ApplyInteraction(boolean wasSucccessful)");
		
		//clean up battle gui stuff
		battlePanelContainer.removeAll(); //this is the quick and dirty way for now, when battle character panels are incorporated into the scene layer hierarchy they need to be separately unplugged
		activeActorsBeforeBattle.clear();
		
		hasInteractionBeenApplied = true;
		
		//Overwrite our member flag in order to prioritize the outcome as determined by the battle system
		wasInteractionSuccessful = wasSuccessful;
		
		//Debugging Graphpath (paired with block below)
		//This may need to be placed before the two methods above, not sure if the graphPath is used in such a way that requires the most recent decision
		//Seems like it needs to happen before, because the graphPath appears to be used with the expectation that the most recent choice is already present in the list
		//int previousGraphPathSize = currentInteractionManager.getGraphPath().size();
		//if(activeMission != null)
		//	Game.Instance().RecordPreviousGraphPathSize(currentLocation.getId(), activeMission.getId());
		
		currentInteractionManager.RecordGraphPathNode(new GraphPathNode(currentInteraction.Type(), wasSuccessful));
		//[MISSION_FLOW_EDIT]
		//Clears the information regarding the currently unresolved interaction, its state, and possible BattleState information
		currentInteractionManager.ClearUnresolvedInteractionState();
		
		//Debugging Graphpath (paired with block above)
		//System.out.println("MapLocationPanel.ApplyInteraction() - previousGraphPathSize: " + previousGraphPathSize + ", size after adding new GraphPathNode: " + currentInteractionManager.getGraphPath().size());
		//if(activeMission != null)
		//	Game.Instance().CheckGraphPathSize(currentLocation.getId(), activeMission.getId());
		
		UseActiveInteractionOfType(currentInteraction.Type());
		
		//TODO Display GrantedItems from Interation
		missionResultsListener = null;
		boolean hasNewRecruits = currentInteraction.NewRecruitIds() != null && currentInteraction.NewRecruitIds().length > 0;
		boolean hasGrantedItems = currentInteraction.GrantedItems() != null && currentInteraction.GrantedItems().length > 0;
		if(hasNewRecruits || hasGrantedItems) {
			resultsPane.UpdateResults("Interaction: " + currentInteraction.Type(), wasInteractionSuccessful, currentInteraction.NewRecruitIds(), currentInteraction.GrantedItems());
			resultsPane.setVisible(true);
			dialogNextButton.setVisible(false);
			dialogBackButton.setVisible(false);
			proceedButton.setVisible(false);
			
			if(hasNewRecruits) {
				for(String recruitId : currentInteraction.NewRecruitIds())
					Game.Instance().AddTeammate(Missions.GetCharacterById(recruitId));
			}
			if(hasGrantedItems)
				Game.Instance().ReceiveItems(currentInteraction.GrantedItems());
		}
		
		/**
		 * 11/14/22 - This block had a null ref error during the Resounding Aura mission because the activeMission was already set to null when completing the first of two interactions in the dialography.
		 * The first interaction is the stipulation for mission completion and the second interaction, Talk, is the one that gives the reward.
		 * So I added this condition. Hopefully it doesn't cause any errors in other circumstances.
		 */
		if(activeMission != null) {
			//Check interactions mission status
			//In the new scheme, the mission flow is less strict and supports two approaches to progression:
			// 1. either leaving via a goto location/mission(representing the end of the current mission) with no regard for mission stipulations and interaction success
			// 2. or relying solely on the mission stipulations and interaction success while disregarding a goto location/mission as related to mission completion
			// *The rule of thumb: if the mission has stipulations then use approach #2, otherwise step thru the interactions until a goto location/mission is encountered then complete mission and move on 
			if(
				(
					//Are there no stipulations?
					(activeMission.getMissionStipulations() == null || activeMission.getMissionStipulations().size() == 0)
					&&
					//Do we have a goto location/mission?
					(!currentInteraction.GotoMapLocationID().isEmpty() || !currentInteraction.GotoMissionId().isEmpty())
				)
				||
				(
					//Are there stipulations?
					(activeMission.getMissionStipulations() != null && activeMission.getMissionStipulations().size() >= 0)
					&&
					//Did we succeed on one of our stipulation?
					(wasInteractionSuccessful && activeMission.getMissionStipulations().contains(currentInteraction.Type()))
				)
			  )
			{
				//System.err.println("MapLocationPanel.ApplyInteraction() - Completed activeMission. Display gainedItems STUB");
				//TODO Display gained items on UI using governingMission.getRewards());
				if(activeMission.getRewards() != null && activeMission.getRewards().length > 0) {
					if(resultsPane.isVisible()) {
						missionResultsListener = new ActionListener() {
							Mission missionCapture = activeMission;
							boolean wasSuccessfulCapture = wasInteractionSuccessful;
							@Override
							public void actionPerformed(ActionEvent e) {
								resultsPane.UpdateResults("Mission: " + missionCapture.getName(), wasSuccessfulCapture, null, missionCapture.getRewards());
								//Set this null so when the ok button is clicked again, the ResultsPane will know to close itself
								missionResultsListener = null;
							}
						};
					} else {
						resultsPane.UpdateResults("Mission: " + activeMission.getName(), wasInteractionSuccessful, null, activeMission.getRewards());
						resultsPane.setVisible(true);
					}
				}
				
				//[MISSION_FLOW_EDIT]
				/*System.out.println("MapLocationPanel.ApplyInteraction() - Calling CompleteMission()");
				Game.Instance().CompleteMission(activeMission);
				activeMission = null;*/
				//This mission logic block is happening a bit too early, I think we should be waiting till ResolveInteraction() so that we can get back to the post-interaction dialography if the user exited early
			}
		}
		
		if(!resultsPane.isVisible())
			ApplyInteractionEnd();
		//else let the ResultsPane call ApplyInteractionEnd()
	}
	
	
	
	public void ApplyInteractionEnd() {
		DialographyData resultingDialographyData = wasInteractionSuccessful ? currentInteraction.DialographyData_OnSuccess() : currentInteraction.DialographyData_OnFailure();
		if(resultingDialographyData.dialogLines != null && resultingDialographyData.dialogLines.length > 0) {
			Reset();
			StartDialography(resultingDialographyData);
		} else if(currentInteraction != null)
			ResolveInteraction();
		else
			System.err.println("MapLocationPanel.ApplyInteractionEnd() - There are no resultingDialographyData.dialogLines or a currentInteraction.");
	}
	
	//Interaction Graph - Start
	
	//this method now disables all applicable intr choices from this layer and all previous layers
	public void UseActiveInteractionOfType(InteractionType type) {
		Interaction intr = currentInteractionManager.GetInteractionAt(interactionLayerDepth, type);
		List<InteractionType> cancelTypes = new ArrayList<InteractionType>();
		if(intr.getCancelPersistentIntrTypes() != null && intr.getCancelPersistentIntrTypes().length > 0)
			cancelTypes = (List<InteractionType>)Arrays.asList(intr.getCancelPersistentIntrTypes());
		
		/*InteractionState[] intrStates = currentLocation.GetInteractionManager().getInteractionLayeredStates()[interactionLayerDepth];
		System.out.println("MapLocationPanel.UseActiveInteractionOfType() - type: " + type + ", layeredStates.length: " + currentLocation.GetInteractionManager().getInteractionLayeredStates().length + ", intrStates.length: " + intrStates.length + ", interactionLayerDepth: " + interactionLayerDepth);
		for(int i = 0; i < intrStates.length; i++) {
			if(intrStates[i].type != type) {
				//this is getting the same interaction everytime, i think its supposed to get different interactions in this layer
				//intr = GetInteractionAt(interactionLayerDepth, type);
				intr = currentInteractionManager.GetInteractionAt(interactionLayerDepth, intrStates[i].type);
				
				if(!intr.getIsPersistentIntr() || (intr.getIsPersistentIntr() && cancelTypes.contains(intr.Type())) ) {
					intrStates[i].isUsed = true;
					if(intr.getInstanceRefreshTimer_days() > 0)
						intrStates[i].daysTillRefresh = intr.getInstanceRefreshTimer_days();
					interactionButtonMap.get(intr.Type()).setVisible(false);
					System.out.println("MapLocationPanel.UseActiveInteractionOfType() - Hiding other non-persistent intr in this layer or a persistent intr in this layer that's being canceled; of type: " + type);
				} else {
					System.out.println("MapLocationPanel.UseActiveInteractionOfType() - NOT Hiding other non-persistent intr in this layer or a persistent intr in this layer that's being canceled");
				}
			} else {
				intrStates[i].isUsed = true;
				if(intr.getInstanceRefreshTimer_days() > 0)
					intrStates[i].daysTillRefresh = intr.getInstanceRefreshTimer_days();
				interactionButtonMap.get(type).setVisible(false);
				System.out.println("MapLocationPanel.UseActiveInteractionOfType() - Hiding the active interaction; of type: " + type);
			}
		}
		
		//Mark active persistent intrs from previous layers if their in the cancelTypes list
		if(cancelTypes != null && cancelTypes.size() > 0 && interactionLayerDepth > 0) {
			for(int p = interactionLayerDepth-1; p > -1; p--) {
				for(InteractionState state : currentLocation.GetInteractionManager().getInteractionLayeredStates()[p]) {
					if(cancelTypes.contains(state.type)) {
						state.isUsed = true;
						interactionButtonMap.get(state.type).setVisible(false);
						System.out.println("MapLocationPanel.UseActiveInteractionOfType() - Hiding persistent intr in previous layer that's being canceled; of type: " + type);
					}
				}
			}
		}*/
		
		InteractionHandler[] intrHandlers = currentInteractionManager.getInteractionHandlers().stream().filter(x -> x.layerDepth() == interactionLayerDepth).toArray(InteractionHandler[]::new);
		
		System.out.println("MapLocationPanel.UseActiveInteractionOfType() - type: " + type + ", intrHandlers.length: " + intrHandlers.length);
		
		for(int i = 0; i < intrHandlers.length; i++) {
			InteractionState intrState = intrHandlers[i].state();
			if(intrState.type != type) {
				intr = intrHandlers[i].intr();
				if(!intr.getIsPersistentIntr() || (intr.getIsPersistentIntr() && cancelTypes.contains(intr.Type())) ) {
					intrState.isUsed = true;
					if(intr.getInstanceRefreshTimer_days() > 0)
						intrState.daysTillRefresh = intr.getInstanceRefreshTimer_days();
					interactionButtonMap.get(intr.Type()).setVisible(false);
					System.out.println("MapLocationPanel.UseActiveInteractionOfType() - Hiding other non-persistent intr in this layer or a persistent intr in this layer that's being canceled; of type: " + intrState.type);
				}
			} else {
				intrState.isUsed = true;
				if(intr.getInstanceRefreshTimer_days() > 0)
					intrState.daysTillRefresh = intr.getInstanceRefreshTimer_days();
				interactionButtonMap.get(type).setVisible(false);
				System.out.println("MapLocationPanel.UseActiveInteractionOfType() - Hiding the active interaction; of type: " + intrState.type);
			}
		}
		
		//Mark active persistent intrs from previous layers if their in the cancelTypes list
		if(cancelTypes != null && cancelTypes.size() > 0 && interactionLayerDepth > 0) {
			InteractionHandler[] earlierIntrHandlers = currentInteractionManager.getInteractionHandlers().stream().filter(x -> x.layerDepth() < interactionLayerDepth).toArray(InteractionHandler[]::new);
			for(InteractionHandler earlierIntrHandler : earlierIntrHandlers) {
				InteractionState state = earlierIntrHandler.state();
				if(cancelTypes.contains(state.type) && earlierIntrHandler.intr().getIsPersistentIntr() && !state.isUsed) {
					state.isUsed = true;
					interactionButtonMap.get(state.type).setVisible(false);
					System.out.println("MapLocationPanel.UseActiveInteractionOfType() - Hiding persistent intr in previous layer that's being canceled; of type: " + state.type);
				}
			}
		}
	}
	
	
	//Interaction Graph - End
	
	private void ResolveInteraction() {
		System.out.println("MapLocationPanel.ResolveInteraction()");
		
		//Set all interactions back to enabled
		for(CustomButtonUltra but : interactionButtonMap.values()) {
			but.setEnabled(true);
		}
		
		//This array is a remnant of older interaction logic
		//currentLayerInteractions = wasInteractionSuccessful ? currentInteraction.NextInteractions_OnSuccess() : currentInteraction.NextInteractions_OnFailure();
		interactionLayerDepth++;
		
		//get the next interactions that'll be available, Persistent and current Tree Intr's
		List<Interaction> nextInteractions = currentInteractionManager.GetActiveInteractions(interactionLayerDepth);
		
		hasDisabledMovement = false;
		for(Interaction interaction : nextInteractions) {
			if(!hasDisabledMovement && interaction.DoesBlockMapMovement())
				hasDisabledMovement = true;
			interactionButtonMap.get(interaction.Type()).setVisible(true);
		}
		
		
		//Get MapLocation or Location from Mission if it exits
		MapLocation destination = currentInteraction.GotoLocation();
		String gotoMissionId = currentInteraction.GotoMissionId();
		boolean useExistingMissionIdInsteadOfDestination = false;
		if(gotoMissionId != null && !gotoMissionId.isEmpty()) {
			destination = worldmapPanel.GetMapLocationById(Missions.getById(gotoMissionId).getMapLocationId());
			if(destination == null) {
				//This means that the mission doesn't govern the MapLocation but instead the Worldmap carries the association between the MapLocation and the WorldTile
				useExistingMissionIdInsteadOfDestination = true;
			}
		}
		//regardless of the interaction type check if theres a goto location for this type
		//boolean travelToGotoLocation = nextInteractions == null && (destination != null || useExistingMissionIdInsteadOfDestination);
		boolean travelToGotoLocation = destination != null || useExistingMissionIdInsteadOfDestination;
		
		//Handle ui/game changes upon resolution, like fleeing or traveling, etc
		switch(currentInteraction.Type()) {
			case Explore:
				break;
			case Camp:
				break;
			case Steal:
				break;
			case Search:
				break;
			case Talk:
				break;
			case Fight:
				break;
			case Trade:
				break;
			case Flee:
				//if there isnt a predetermined place that we'll flee to hten just pick a random adjacent tile
				if(!travelToGotoLocation) {
					travelToGotoLocation = true;
					//Change to random location
					destination = worldmapPanel.RandomAdjacentTile();
				}
				break;
			case Travel:
				break;
			default:
				System.err.println("Add support for: " + currentInteraction.Type());
				break;
		}
		
		//This needs to be moved up here so that we can resolve and closeout all current happens before moving to a new location
		//Finalization - Start
		
		//Clear the path and save it so it can be properly loaded in OnEnterLocation
		//if(travelToGotoLocation || currentInteraction.getIsPersistentIntr())
			//graphPath.clear();
			//currentInteractionManager.getGraphPath().clear();
		//We dont want to clear the graphPath cause then we have no record of what happened here
		
		System.out.println("MapLocationPanel.ResolveInteraction() - currentIntType: " + currentInteraction.Type() + ", travelToGotoLocation: " + travelToGotoLocation + 
				", nextInteractions == null: " + (nextInteractions == null) + ", destination != null: " + (destination != null) +
				", gotoMissionId: " + gotoMissionId
				);
		
		//This resolution method signifies that we're no longer experiencing a particular Interaction
		Interaction resolvingInteraction = currentInteraction;
		currentInteraction = null;
	
		
		//[MISSION_FLOW_EDIT]
		//We may want to wait till here to complete the mission; otherwise if users exit the game before finishing the post-interaction dialography then they've been recorded finishing a mission without
		//having experienced the results of it, mainly transitional results(i.e. teleporting to the goToLocation/goToMission).
		//11-14-22 - This is creating null refs for missions that have interactions beyond their success stipulation in ApplyInteractions(), like in the mission Resounding Aura. Check that this is the
		//last mission before clearing.
		//if(nextInteractions == null) { //This condition bugged other missions. Taking another approach by catching null activeMission in the results panel logic.
		if(activeMission != null) { //This is necessary, regardless of the intricate interworkings of the mission system. If there's no active mission then dont try to complete it.
			System.out.println("MapLocationPanel.ResolveInteraction() - Calling CompleteMission() and setting activeMission to null.");
			Game.Instance().CompleteMission(activeMission);
			activeMission = null;
		}
		//}
		
		//Finalization - End
		
		if(travelToGotoLocation) {
			final boolean _useExistingMissionIdInsteadOfDestination = useExistingMissionIdInsteadOfDestination;
			final MapLocation _destination = destination;
			//The fade doesn't seem to work when called here, possibly some kind of swing execution order restriction?
			GUIManager.GetFadeTransitionPanel().Fade(true, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(_useExistingMissionIdInsteadOfDestination)
						MoveToLocation(gotoMissionId);
					else
						MoveToLocation(_destination);
					
					GUIManager.GetFadeTransitionPanel().Fade(false, 120);
				}
			});
		} else if(resolvingInteraction.getIsPersistentIntr()) {
			//Return to Base if there isn't a pending location to move to
			OnEnterLocation(currentLocation);
		//} else if(nextInteractions != null && nextInteractions.size() > 0) {
		//we also need this to happen when there are no remaining interactions but we're finished with our current one
		} else {
			System.out.println("MapLocationPanel.ResolveInteraction() - Show default MapLocation view");
			//Show the new layer of interactions
			SetInteractionPanelVisible(true);
			
			//Add the new line of interaction path text to the description panel
			String outcomeDesc = "";
			if(wasInteractionSuccessful) {
				if(resolvingInteraction.DialographyData_OnSuccess() != null)
					outcomeDesc = resolvingInteraction.DialographyData_OnSuccess().locationDescSummary;
			} else {
				if(resolvingInteraction.DialographyData_OnFailure() != null)
					outcomeDesc = resolvingInteraction.DialographyData_OnFailure().locationDescSummary;
			}
			//Try the pre data if there wasnt either
			if(outcomeDesc.isEmpty() && resolvingInteraction.DialographyData_Pre() != null) {
				outcomeDesc = resolvingInteraction.DialographyData_Pre().locationDescSummary;
				System.out.println("MapLocationPanel.ResolveInteraction() - Setting dialog pre desc: " + outcomeDesc);
			}
			if(outcomeDesc != "") //Keep the return out of the String if theres no desc for this intr
				outcomeDesc = "\n" + outcomeDesc;
			descriptionTextArea.append("\n* " + resolvingInteraction.Type() + " *" + outcomeDesc);
			
			SetLocationInfoPanelsVisible(true);
			
			//hide dialogpanel
			SetDialogPanelVisible(false);
		}
	}
	
	private void ResolveInteractionlessDialography() {
		System.out.println("MapLocationPanel.ResolveInteractionlessDialography()");
		
		//[MISSION_FLOW_EDIT]
		//if this was a trailing mission then complete it upon finishing the entry dialography
		//This conditional needs refining to avoid a false positive on mission entry dialographies
		if(activeMission != null
		   &&
		   (activeMission.getMissionStipulations() == null || activeMission.getMissionStipulations().size() == 0)
		   &&
		   activeMission.GetInteractionManager().getInteractions().length == 0
		) {
			System.out.println("MapLocationPanel.ResolveInteractionlessDialography() - Completed trailing mission: " + activeMission.getName());
			Game.Instance().CompleteMission(activeMission);
			if(activeMission.getRewards() != null && activeMission.getRewards().length > 0) {
				//TODO Display gained items on UI using governingMission.getRewards());
				System.err.println("MapLocationPanel.ResolveInteractionlessDialography() - STUB - Show rewards for completed mission.");
			}
			activeMission = null;
		}
		
		
		//Dialographies originating from the load state of a previous interaction choice should always end here so this is where we clear the value
		mostRecentInteractionTypeFromLoad = null;
		
		//Show the new layer of interactions
		SetInteractionPanelVisible(true);
		
		//If this is an entry for a mission then overwrite the default mapLocation description completely
		//if(activeMission != null) 
		//	descriptionTextArea.setText(currentDialography.locationDescSummary);
		//otherwise, preserve the mapLocation description and add the new line of interaction path text to it
		//else
		//	descriptionTextArea.append("\n* " + currentDialography.locationDescSummary);
		//the above bit actually needs to hapen at scene load
		//descriptionTextArea.append("\n* " + currentDialography.locationDescSummary);
		//don't update the desc at all for an interactionless dialography because the player hasn't made any choices yet
		
		SetLocationInfoPanelsVisible(true);
		
		//hide dialogpanel
		SetDialogPanelVisible(false);
	}
	
	/**
	 * This is a convenience methods to bundle the OnEnterLocation method and the movement instructions sent to the WorldmapPanel to move our party to the
	 * destination tile. Perhaps all that's needed is a call to the WorldmapPanel and that process in turn will call OnEnterLocation for us.
	 */
	private void MoveToLocation(MapLocation mapLocation) {
		//Tell WorldmapPanel to instantaneously move to the destination
		worldmapPanel.MoveToNewTile(mapLocation);
	}
	private void MoveToLocation(String missionIdForLocation) {
		//Tell WorldmapPanel to instantaneously move to the destination of the missionId
		worldmapPanel.MoveToNewTile(missionIdForLocation);
	}
	
	//Activity Methods - End
	
	//General Purpose Update System - Start
	
	//In this class we need to time:
	//AnimatedTiles 	- done, with SetNewImage gracePeriod
	//CampFireAnim		- 
	//In ActorPath, we need to time:
	//State Animations  - done, lacks SetNewImage
	//Update 			- done, with SetNewImage gracePeriod
	//Wait timer 		- done, lacks SetNewImage
	//DoorTiles 		- done, with SetNewImage gracePeriod
	//Fade timer 		- done
	
	
	
	List<UpdateTimer> updateTimers = new ArrayList<UpdateTimer>();
	public void CreateUpdateTimer(UpdateTimer timer) {
		updateTimers.add(timer);
		//System.out.println("Create Timer, size is now: " + updateTimers.size());
	}
	public void RemoveUpdateTimerImmediately(UpdateTimer timer) {
		//System.out.println("Remove Timer Immediately");
		updateTimers.remove(timer);
	}
	
	
    
    private void StartUpdateLoop() {
    	System.out.println("MapLocationPanel.StartUpdateLoop()");
    	
    	//if(sceneAnimation_dirtyFlag) {
    	//	sceneAnimation_dirtyFlag = false;
    	//	ResetUpdateLoop();
    	//}
    	//This already happens in the Resst method so its unnecessary and erronious here
    	
    	updateTimer.setRepeats(true);
    	updateTimer.start();
    	sceneAnimation_dirtyFlag = true;
    }
    
    //call this when leaving the MapLocationPanel, called by GUIManager.ShowScreen()
    public void StopUpdateLoop() {
    	System.out.println("MapLocationPanel.StopUpdateLoop()");
    	if(updateTimer.isRunning())
    		updateTimer.stop();
    }
    
    //reset the Update animation system
    private void ResetUpdateLoop() {
    	System.out.println("MapLocationPanel.ResetUpdateLoop()");
    	
    	updateTimers.clear();
    	for(AnimatedTile animTile : animatedTiles)
    		animTile.Reset();
    	for(DoorTile doorTile : doorTiles)
    		doorTile.Reset();
    	for(Actor actor : sceneActors) {
    		actor.Reset();
    	}
    }
	
    //General Purpose Update System - End
	
	//Animated Tiles (Static scene features and doors) - Start
	
	
	
	List<DoorTile> doorTiles = new ArrayList<DoorTile>();
	/**
     * This is for call upon the stored data for special animating tile in the scene, Examples of these would be:
     * doors, interactive props, fawna actors, water and all other animated superficial decoration tiles.
     * @param lowerBounds_gridPoint - the min point
     * @param upperBounds_gridPoint - the max point
     * @return DoorTile[] - All the doorTiles living in the bounding area
     */
    public DoorTile[] GetDoorTilesInBounds(Point lowerBounds_gridPoint, Point upperBounds_gridPoint) {
    	DoorTile[] affectedDoorTiles = doorTiles.stream().filter(x ->
    		x.GetVisualTileData().gridLocationX >= lowerBounds_gridPoint.x && x.GetVisualTileData().gridLocationY >= lowerBounds_gridPoint.y
    		&&
    		x.GetVisualTileData().gridLocationX <= upperBounds_gridPoint.x && x.GetVisualTileData().gridLocationY <= upperBounds_gridPoint.y
    	).toArray(DoorTile[]::new);
    	return affectedDoorTiles;
    }
	
    //Animated Tiles (Static scene features and doors) - End
	
	//Scene Actors - Start

	
	private void CreateSceneActors() {
		for(Actor previousSceneActor : sceneActors) {
			sublayerDepthManagers.get(previousSceneActor.getCurrentRow()).TrackOccupantActivity(SublayerType.ActorOrProp, false);
			scenePane.remove(previousSceneActor.getImagePanel());
		}
		sceneActors.clear();
		int actorIndex = 0;
		
		List<ActorData> actorDatas = null;
		if(currentLocation.getRelativeSceneDirectory() != null)
			actorDatas = Missions.GetAllLocationActors(currentLocation.getRelativeSceneDirectory());
		else
			actorDatas = Missions.GetAllLocationActors(currentLocation.getRelativeComboSettlementSceneDirectory());
		
		//Mark the actors from the Settlement so their positions and movements can be offset to align with the Settlement layer.
		if(currentLocation.getSceneLayeringType() == SceneLayeringType.BothLayers) {
			for(ActorData actorData : actorDatas)
				actorData.SetIsSettlementLayerActor();
		}
		
		System.err.println("MapLocationPanel.CreateSceneActors() - The actors being added into the sublayerDepthManagers here might be placed at an erroneous depth.");
		int rowIndex = sublayerDepthManagers.size() - 1;
		int actorCount = 0;
		for(ActorData actorData : actorDatas) {
			ImagePanel imagePanel = new ImagePanel();
			//add them to the ui heirarchy. Place the actors, temporarily into the first row/layer
			scenePane.add(imagePanel, rowIndex, sublayerDepthManagers.get(rowIndex).GetStartIndexForSublayer(SublayerType.ActorOrProp));
			
			AddToDebugMap(imagePanel.hashCode(), "Actor Sprite " + actorCount);
			actorCount++;
			
			//sublayerDepthManagers.get(rowIndex).TrackOccupantActivity(SublayerType.ActorOrProp, true);
			//we shouldn't be tracking their sublayer depth yet cause we don't know where they'll be moved to when they start their dialography
			//TODO ensure proper tracking is setup for Actors somewhere down the line
			
			Actor newActor = new Actor(this, actorData, imagePanel, sceneLayoutInfo);
			sceneActors.add(newActor);
			
			//Debug target actor at actorIndex
			//int targetActorIndex = 3;
			//if(actorIndex == targetActorIndex)
			//	sceneActors.get(targetActorIndex).SetDebug(true);
			//actorIndex++;
			
			//Debug everything
			//sceneActors.get(sceneActors.size()-1).SetDebug(true);
			
			//Debug effects
			//if(sceneActors.get(sceneActors.size()-1).IsEffect())
			//	sceneActors.get(sceneActors.size()-1).SetDebug(true);
		}
	}
	
	public void DebugPanelsCurrentLayer(ImagePanel panel) {
		System.out.println("MapLocationPanel.DebugPanelsCurrentLayer() - panels current layer: " + scenePane.getIndexOf(panel));
	}
	
	
	//Scene Actors - End
}
