package gameLogic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dataShared.ActorData;
import dataShared.ActorPathData;
import enums.ClassType;
import enums.ColorBlend;
import enums.Direction;
import enums.SceneLayeringType;
import enums.StateType;
import data.AnimState;
import gui.BattlePanel;
import gui.DoorTile;
import gui.GUIManager;
import gui.ImagePanel;
import gui.MapLocationPanel;
import gui.SpriteSheet;
import gui.SpriteSheetUtility;
import gui.MapLocationPanel.SceneLayoutInfo;
import gui.MapLocationPanel.UpdateTimer;


/**Adapted from a C# class bearing the same name. This class serves as the mechanism for animating actors throughout the scene.
 * NEW- This class has been renamed Actor in Unity and ActorPath has been changed to an AnimState[] storage class placed on the Actor's child gameObjects
 * @author Magnus
 *
 */
public class Actor {
	
	//Selective Debugging - Start
	
	private boolean isDebugging;
	public void SetDebug(boolean enabled) {
		isDebugging = enabled;
	}
	
	//Debug Measurements
	private long lastNanoSinceMoveStart;
	private long lastNanoSinceWaitStart;
	
	//Selectice Debugging - End
	
	//Special Java Adaptations - Start
	
	private MapLocationPanel mapLocationPanel;
	private ActorPathData currentPathData;
	/**
	 * Holds the names of the Effect animation frames; in looping anim sequence. Upon construction, these names are extracted from the data encoded in actor properties.
	 */
	private List<String> frameNames;
	
	private ActorData actorData;
	public ActorData getActorData() { return actorData; }
	
	SceneLayoutInfo sceneLayoutInfo;
	
	private final Dimension actorSize;
	private Dimension actorOffset;
	
	//Animation stuff
	float currentAnimSpeed = 1f;
    UpdateTimer animatorUpdateTimer;
    boolean isUpdateTimerActive;
    private SpriteSheet spriteSheet;
    public SpriteSheet getSpriteSheet() { return spriteSheet; }
    List<Integer> currentAnimIndices = new ArrayList<Integer>();
    int currentAnimFrameIndex;
	
    public List<AnimState> animStates;

    private final float moveSpeed_PixPerSec = 24f;

    private int stateIndex = -1;
    private Map<Direction, Point> directionVectorMap = new HashMap<Direction, Point>();
    
    private ImagePanel panel;
    public ImagePanel getImagePanel() { return panel; }
    
    private int lastYRow;
    private int lastNextRow;
    private boolean isEffect;
    public boolean IsEffect() { return isEffect; }
    
    //Movement
    //Pixel screen space
    Point startPos;
    //pixel screen space
    Point destination;
    float currentTime;
    float scheduledTime;

    //directional tracking
    Direction previousDirection = Direction.Down;

    //Wait timer
    boolean isWaiting;
    boolean isWaitingOnDoor;
    
    //Fade In/Out
    Color originalColor;
    final float fadeInterval = 0.05f;
    
    //Dialography control
    private boolean endStall_flag;
    
    //Is wasteful to keep getting this during update loops, just grab it during construction
    ClassType classType;
    
    
    public interface IRepeater {
    	public boolean IsDoneRepeating();
    }
    
    public interface RepeatingListener extends ActionListener, IRepeater {
		@Override
		public void actionPerformed(ActionEvent arg0);
		
		/**
		 * Check whether this class should stop repeating. The isDone member variable will need to be set to true in actionPerformed() for this to happen.
		 */
		@Override
		public boolean IsDoneRepeating();
    }
	
    
    public Actor(MapLocationPanel mapLocationPanel, ActorData actorData, ImagePanel panel, SceneLayoutInfo sceneLayoutInfo) {
		this.mapLocationPanel = mapLocationPanel;
		
		this.actorData = actorData;
		
		this.sceneLayoutInfo = sceneLayoutInfo;
		this.panel = panel;
		
		//If this is an effect then the setup is different
		data.CharacterData charData = Missions.GetCharacterById(actorData.characterDataId);
		classType = GetActorsClassTypeOrDefault();
		float scaleRatio = (float)BattlePanel.GetCharacterSize().width / SpriteSheetUtility.GetWalkSheet(ClassType.RONIN).GetSprite(0, 1, 1).getTileWidth();
		if(charData == null && actorData.nonActorFrames_startIndex != -1) {
			isEffect = true;
			System.out.println("Actor Contructor - About to attempt to get Effect Sheet at: " + actorData.javaSheetFilePath);
			spriteSheet = SpriteSheetUtility.GetEffectSheet(actorData.javaSheetFilePath);
			
			String[] splits = actorData.javaSheetFilePath.split("/");
			frameNames = new ArrayList<String>();
			currentAnimIndices = new ArrayList<Integer>();
			for(int i = actorData.nonActorFrames_startIndex; i <= actorData.nonActorFrames_endIndex; i++) {
				frameNames.add( splits[splits.length-1].split("\\.")[0] + "_" + i);
				currentAnimIndices.add(i - actorData.nonActorFrames_startIndex);
			}
			System.out.println("Actor Contructor - frameName #1: " + frameNames.get(0) + ", frameNames.size(): " + frameNames.size());
			BufferedImage sprite = spriteSheet.GetSprite(frameNames.get(currentAnimIndices.get(0)));
			
			//Now that we know which spriteSheet to use we can derive the size and offset based on that
			actorSize = new Dimension(
				Math.round(sprite.getWidth() * scaleRatio), 
				Math.round(sprite.getHeight() * scaleRatio)
			);
			actorOffset = new Dimension(Math.round(actorSize.width * 0.5f), Math.round(actorSize.height * 0.5f));
			this.panel.SetNewImage(sprite);
			
		//Else if this is a playable character or an npc character
		} else {
			//Get the sheet for the player character
			if(charData != null && charData.getName().equals("[PLAYER]")) {
				//Use the player designed character
				spriteSheet = SpriteSheetUtility.GetWalkSheet(Game.Instance().GetPlayerData().getType());
				actorSize = BattlePanel.GetCharacterSize();
				//System.out.println("**!**!**!** Player CharacterSize: " + actorSize);
			} else {
				spriteSheet = SpriteSheetUtility.GetActorSheet(actorData.javaSheetFilePath);
				//Identify whether this is a standard character or something else; a non-combative NPC or a Kami
				//A Kami character
				if(charData != null && charData.getType().toString().startsWith("KAMI")) {
					System.err.println("Actor Constructor - Kami Setup Stub");
					actorSize = new Dimension(
						Math.round(spriteSheet.GetSprite(0, 1, 1).getTileWidth() * scaleRatio), 
						Math.round(spriteSheet.GetSprite(0, 1, 1).getTileHeight() * scaleRatio)
					);
				//A standard character or non-combative NPC person/animal
				} else {
					actorSize = new Dimension(
						Math.round(spriteSheet.GetSprite(0, 1, 1).getTileWidth() * scaleRatio), 
						Math.round(spriteSheet.GetSprite(0, 1, 1).getTileHeight() * scaleRatio)
					);
				}
				//System.out.println("^^!^^!^^!^^!^^ sheet tileWidth: " + SpriteSheetUtility.GetWalkSheet(ClassType.RONIN).GetSprite(0, 1, 1).getTileWidth()
				//		+ ", characterSize Width: " + BattlePanel.GetCharacterSize().width + ", scaleRatio: " + scaleRatio + ", actorSize: " + actorSize);
			}
			actorOffset = new Dimension(Math.round(actorSize.width * 0.5f), Math.round(actorSize.height * (1f - 0.083f)));
			this.panel.SetNewImage(spriteSheet.GetSprite(1, 1, 1));
		}
		this.panel.setOpaque(false);
		this.panel.setBackground(new Color(0,0,0,0));
		
		originalColor = actorData.originalColor.getColor();
		this.panel.SetTint(originalColor, ColorBlend.Multiply);
		
		//Apply the actors image rotation
		this.panel.SetZRotation(actorData.zRotation);
		
		//Set this up before its used anywhere
		animatorUpdateTimer = mapLocationPanel.new UpdateTimer(GetMsAnimatorDelay(1f), true, new ActionListener() {
    		@Override
    		public void actionPerformed(ActionEvent arg0) {
    			if(!isUpdateTimerActive) {
    				System.err.println("Intercepting invalid Animator update. Returning.");
    				return;
    			}
    			
    			currentAnimFrameIndex++;
				if(currentAnimFrameIndex >= currentAnimIndices.size())
					currentAnimFrameIndex = 0;
				BufferedImage sprite = null;
				
				if(!isEffect) {
					if (animStates.get(stateIndex).isLayingState) {
		                sprite = SpriteSheetUtility.GetDeadStateSprite(classType);
		                if(isDebugging)
		                	System.out.println("Animator Tick - LayingState - classType: " + classType + ", sprite.width: " + sprite.getWidth());
		    		} else
		    			sprite = spriteSheet.GetSprite(currentAnimIndices.get(currentAnimFrameIndex), 1, 1);
				} else
					sprite = spriteSheet.GetSprite(frameNames.get(currentAnimIndices.get(currentAnimFrameIndex)));
				panel.SetNewImage(sprite, MapLocationPanel.getUpdateInterval_ms());
				
				if(isDebugging) {
					if(!isEffect)
						System.out.println("Animator Tick - ACTOR, frame: " + currentAnimIndices.get(currentAnimFrameIndex));
					else
						System.out.println("Animator Tick - EFFECT, frameName: " + frameNames.get(currentAnimIndices.get(currentAnimFrameIndex)));
				}
    		}
        });
	}
	
	private ClassType GetActorsClassTypeOrDefault() {
		ClassType classType = ClassType.PRIEST;
		data.CharacterData charData = Missions.GetCharacterById(actorData.characterDataId);
		if(charData != null && charData.getName().equals("[PLAYER]"))
			classType = Game.Instance().GetPlayerData().getType();
		else {
			for(ClassType type : ClassType.values()) {
				if(actorData.javaSheetFilePath.contains(type.toString())) {
					classType = type;
					break;
				}
			}
		}
		return classType;
	}
	
	/**
	 * Called for dialographies during subsequent OnEnterLocation events.
	 * @param pathData - The path to play.
	 * @param sceneLayoutInfo - This needs to be updated for the new scene.
	 */
	public void SetActorPath(ActorPathData pathData, SceneLayoutInfo sceneLayoutInfo) {
		if(isDebugging)
			System.out.println("Actor.SetActorPath(ActorPathData, SceneLayoutInfo) - isEffect: " + isEffect);
		
		this.sceneLayoutInfo = sceneLayoutInfo;
		currentPathData = pathData;
		animStates = new ArrayList<AnimState>();
		for(AnimState state : pathData.animStates) {
			animStates.add(state);
		}
		
		//transform_setPosition(pathData.startLocation);
		transform_setPosition(TryTranslateBySettlementOffset(pathData.startLocation));
		
		
		//Bug Fix: Setting the actor's layer in case this is the first dialography they've performed, otherwise they start on
		//Layer 0 of the scene pane.
		int lastRow = getCurrentRow();
        
		int newRow = GetNextMoveTileRow(transform_getPosition().y, animStates.get(0).direction == Direction.Down) + 2;
		//Checking to see if this is whats throwing off the vendors in the Market_Outcaste
		//Research: Taking away the "+ 2" makes the Market_Outcaste vendors render correctly but screws up everyone else???
		//int newRow = GetNextMoveTileRow(transform_getPosition().y, false) + 2;
		//This seems to work for all actors, until the vendors move; at which time they're layered erroneously again.
		//Going to try setting lastYRow to this newRow value in the hopes that it'll keep the vendor's row layer going.
		//this.lastYRow = newRow;
		//this.lastNextRow = newRow;
		//didn't have any positive effects
		
		
		mapLocationPanel.UpdateActorRow(panel, lastRow, newRow, true, this.actorData.actorId);
		
		panel.setVisible(true);
		
		Start();
	}
	
	/**
	 * Actor's from the Settlement Layer need to have their locations offset to match the offset of the Settlement bounds from the nature bounds.
	 * @param originalLocation
	 * @return
	 */
	private Point2D.Float TryTranslateBySettlementOffset(Point2D.Float originalLocation) {
		Point2D.Float newLocation = originalLocation;
		if(this.actorData.IsSettlementLayerActor() || (Game.Instance().GetPlayerData().getId().matches(actorData.characterDataId) && MapLocationPanel.GetCurrentLocation().getSceneLayeringType() == SceneLayeringType.BothLayers))
			newLocation = new Point2D.Float(newLocation.x + mapLocationPanel.getSettlementLayerOffset().x, newLocation.y + mapLocationPanel.getSettlementLayerOffset().y);
		return newLocation;
	}
	
	/**
	 * Called on Actors who have been instantiated as part of this Scene but are not active in the current Dialography
	 */
	public void Deactivate() {
		if(isDebugging)
			System.out.println("Actor.Deactivate() - panel.setVisible(false)");
		panel.setVisible(false);
	}
	
	public void Reset() {
		panel.SetTint(originalColor, ColorBlend.Multiply);
		
		if(!isEffect)
			panel.SetNewImage(spriteSheet.GetSprite(1, 1, 1));
		else
			panel.SetNewImage(spriteSheet.GetSprite(frameNames.get(currentAnimIndices.get(0))));
		
		//if(currentPathData != null)
		//	transform_setPosition(currentPathData.startLocation);
		if(currentPathData != null) {
			transform_setPosition(TryTranslateBySettlementOffset(currentPathData.startLocation));
		}
		
		currentAnimFrameIndex = 0;
		currentAnimSpeed = 1f;
		animatorUpdateTimer.duration_ms = GetMsAnimatorDelay(currentAnimSpeed);
		isUpdateTimerActive = false;
		if(!isEffect)
			currentAnimIndices.clear();
		
		stateIndex = -1;
		startPos = null;
	    destination = null;
	    currentTime = 0f;
	    scheduledTime = 0f;
	    previousDirection = Direction.Down; //this can't be null so this will have to do as far as reseting goes 
	    isWaiting = false;
	    isWaitingOnDoor = false;
	    
	    endStall_flag = false;
	}
	
	/**
	 * Used for reporting this actor's previous position to MapLocationPanel().HandleBreakawayForCharacterMove(...). This value is intended to be null during setup(on the first call from each character).
	 */
	Point2D.Float lastGridPosition = null;
    
    /**
     * Set the transform based on a direct worldspace coordinate
     * @param newPosition - Raw world space coordinate
     */
    private void transform_setPosition(Point2D.Float newGridPosition) {
    	//Now that we're in the scenePane we don't need to offset the position by the sceneTopCornerLoc
    	//int pixX = sceneLayoutInfo.sceneTopCornerLoc.x + Math.round( newGridPosition.x * sceneLayoutInfo.tileWidth );
    	//int pixY = sceneLayoutInfo.sceneTopCornerLoc.y + sceneLayoutInfo.sceneSize.height - Math.round( newGridPosition.y * sceneLayoutInfo.tileWidth );
    	int pixX = Math.round( newGridPosition.x * sceneLayoutInfo.tileWidth );
    	int pixY = sceneLayoutInfo.sceneSize.height - Math.round( newGridPosition.y * sceneLayoutInfo.tileWidth );
    	
    	if(isDebugging)
    		System.out.println("Actor.transform_setPosition() - pixX: " + pixX + ", pixY: " + pixY);
    	//panel.setBounds(pixX, pixY, actorSize.width, actorSize.height);
    	//Offset the character size to center them on their mark
    	panel.setBounds(pixX - actorOffset.width, pixY - actorOffset.height, actorSize.width, actorSize.height);
    	
    	//Set this here because it will need to be utiltized by move anims
    	lastYRow = (int)(newGridPosition.y - 0.5f);
    	
    	System.out.println("Actor.transform_setPosition() - lastYRow: " + lastYRow);
    	
    	//Show/Hide any Breakaways
    	Point lastPoint = null;
    	if(lastGridPosition != null)
    		lastPoint = FloatPointToIntPoint(lastGridPosition);
    	GUIManager.MapLocationPanel().HandleBreakawayForCharacterMove(lastPoint, FloatPointToIntPoint(newGridPosition));
    	lastGridPosition = newGridPosition;
    }
    
    /**
     * Take a literally position of where the character will be placed relative to the scene grid, like [5.5, 10.5], and convert it into a grid position, like [5, 10].
     * @param floatPoint
     * @return
     */
    private Point FloatPointToIntPoint(Point2D.Float floatPoint) {
    	return new Point((int)(floatPoint.x - 0.5f), (int)(floatPoint.y - 0.5f));
    }
    
    /**
     * Set the transform based on a grid point instead of setting it as a direct worldspace coordinate
     * @param newGridPosition - A grid point in the Scene
     */
    private void transform_setPosition(Point newPixelPos) {
    	panel.setBounds(newPixelPos.x, newPixelPos.y, actorSize.width, actorSize.height);
    	
    	//Don't set lastYRow here because this is the incremental type of position used by animation to blend travel from one tile to another
    }
    
    private Point transform_getPosition() {
    	return panel.getLocation();
    }
    
    private Point point_lerp(Point start, Point end, float norm) {
    	return new Point(lerp(start.x, end.x, norm), lerp(start.y, end.y, norm));
    }
    
    float lerp(float a, float b, float f)
    {
        return a + (f * (b - a));
    }
    
    int lerp(int a, int b, float f)
    {
        return a + Math.round(f * (b - a));
    }
    
    Color ColorLerp(Color start, Color end, float norm) {
    	return new Color(
    			lerp(start.getRed(), end.getRed(), norm),
    			lerp(start.getGreen(), end.getGreen(), norm),
    			lerp(start.getBlue(), end.getBlue(), norm),
    			lerp(start.getAlpha(), end.getAlpha(), norm));
    }
    
    //Animation - Start
    
    private void animator_setSpeed(float newSpeed) {
    	currentAnimSpeed = newSpeed;
    	//modify the timer used as the animator
    	if(newSpeed == 0f) {
    		if(isUpdateTimerActive) {
    			//System.out.println("Actor: "+ this.actorData.actorId.substring(0, 4) +" - animator_setSpeed("+newSpeed+") - Removing Timer");
    			
    			mapLocationPanel.RemoveUpdateTimerImmediately(animatorUpdateTimer);
    			isUpdateTimerActive = false;
    		}
    		
    		//stop the animator on an idle frame
    		currentAnimFrameIndex = 0;
    		BufferedImage sprite = null;
    		if(!isEffect)
    			sprite = spriteSheet.GetSprite(currentAnimIndices.get(0), 1, 1);
    		else
    			sprite = spriteSheet.GetSprite(frameNames.get(currentAnimIndices.get(0)));
    		panel.SetNewImage(sprite, MapLocationPanel.getUpdateInterval_ms());
    		
    		if(isDebugging)
    			System.out.println("Actor.animator_setSpeed() - Stalled anim at frame index: " + currentAnimIndices.get(0));
    	} else {
    		animatorUpdateTimer.duration_ms = GetMsAnimatorDelay(currentAnimSpeed);
    		if(!isUpdateTimerActive) {
    			//System.out.println("Actor: "+ this.actorData.actorId.substring(0, 4) +" - animator_setSpeed("+newSpeed+") - Creating New Timer");
    			
    			isUpdateTimerActive = true;
    			mapLocationPanel.CreateUpdateTimer(animatorUpdateTimer);	
    		}
    	}
    }
    
    //private void animator_play(Direction direction) {
    //This ensures that we're getting the correct animState data (isLayingState)
    private void animator_play(Direction direction) {
    	if(!isEffect) {
	    	//set first frame for new anim
	    	currentAnimIndices.clear();
	    	switch(direction) {
		    	case Down:
		    		currentAnimIndices.add(1);
		    		currentAnimIndices.add(2);
		    		currentAnimIndices.add(1);
		    		currentAnimIndices.add(0);
		    		break;
		    	case Left:
		    		currentAnimIndices.add(4);
		    		currentAnimIndices.add(5);
		    		currentAnimIndices.add(4);
		    		currentAnimIndices.add(3);
		    		break;
		    	case Right:
		    		currentAnimIndices.add(7);
		    		currentAnimIndices.add(8);
		    		currentAnimIndices.add(7);
		    		currentAnimIndices.add(6);
		    		break;
		    	case Up:
		    		currentAnimIndices.add(10);
		    		currentAnimIndices.add(11);
		    		currentAnimIndices.add(10);
		    		currentAnimIndices.add(9);
		    		break;
	    	}
    	}
    	
    	if(isDebugging) {
    		System.out.println("Actor.animator_play() - isEffect: " + isEffect + ", panel.position: " + panel.getLocation());
    		mapLocationPanel.DebugPanelsCurrentLayer(panel);
    	}
    	
    	//This whole block may be ineffectual because it seems the real thing driving animation is the animatorUpdateTimer logic
    	currentAnimFrameIndex = 0;
    	BufferedImage sprite = null;
    	if(!isEffect) {
    		if (animStates.get(stateIndex).isLayingState) {
    			sprite = SpriteSheetUtility.GetDeadStateSprite(classType);
                if(isDebugging)
                	System.out.println("Actor.animator_play() - LayingState - classType: " + classType + ", sprite.width: " + sprite.getWidth());
    		} else {
            	sprite = spriteSheet.GetSprite(currentAnimIndices.get(0), 1, 1);
    		}
    	} else
    		sprite = spriteSheet.GetSprite(frameNames.get(currentAnimIndices.get(0)));
    	panel.SetNewImage(sprite, MapLocationPanel.getUpdateInterval_ms());
    	
    	animatorUpdateTimer.duration_ms = GetMsAnimatorDelay(currentAnimSpeed);
    	if(!isUpdateTimerActive) {
    		if(isDebugging)
    			System.out.println("Actor.animator_play() - Creating New Timer");
    		
    		isUpdateTimerActive = true;
    		mapLocationPanel.CreateUpdateTimer(animatorUpdateTimer);
    	}
    }
    
    private int GetMsAnimatorDelay(float newSpeed) {
    	//In unity, the anims are set to a base speed of 4 samples per second, which is very slow.
    	//four samples per second equals:
    	final int animatorBaseSpeed_ms = 250;
    	return Math.round( animatorBaseSpeed_ms / newSpeed );
    }
    
    //Animation - End
	
	//Special Java Adaptations - End
    
    void Start() {
    	if(isDebugging)
    		System.out.println("Actor.Start() - actorId: " + this.actorData.actorId);
    	
        directionVectorMap.put(Direction.Down, new Point(0, -1));
        directionVectorMap.put(Direction.Left, new Point(-1, 0));
        directionVectorMap.put(Direction.Right, new Point(1, 0));
        directionVectorMap.put(Direction.Up, new Point(0, 1));
        
        //If this is a looping anim then consider it "done" already, in terms of its affecting the flow of dialog
        if(!animStates.stream().anyMatch(x -> x.stateType == StateType.EndLoop))
            mapLocationPanel.ActorDoneAnimating(this);

        //Because stateIndex always starts at -1 call this in start will begin our logic on index 0
        StartNextState();
        
        CreateUpdate();
    }
    
    private void CreateUpdate() {
    	//Subscribe to the MapLocation Update
        mapLocationPanel.CreateUpdateTimer(mapLocationPanel.new UpdateTimer(-1, true, new ActionListener() {
    		@Override
    		public void actionPerformed(ActionEvent arg0) {
    			Update();
    		}
        }));
    }
    
    /**
     * The magic number here is 16f. It's derived from the tilemap tile resolution: 16x16.
   	 * So this method's calculation breaks down to mean that the default move speed is 1.5 tiles per second.
     * @return
     */
    private float GetSpeed()
    {
    	return (moveSpeed_PixPerSec + animStates.get(stateIndex).speedMod) / 16f;
    }
    
    /**
     * Use to track when we make it to a different row so we can tell the MapLocation to change the actors layer
     * @return lastYRow + 2
     */
    public int getCurrentRow() {
    	return lastYRow + 2;
    }
    
    void Update()
    {
    	if (animStates.size() == 0 || stateIndex >= animStates.size() || animStates.get(stateIndex).stateType == StateType.EndLoop)
            return;
    	
    	if(isDebugging) {
    		if(isEffect)
    			System.out.println("Actor.Update() - EFFECT - AnimState.stateType: " + animStates.get(stateIndex).stateType);
    	}
    	
        boolean isStateDone = false;

        switch (animStates.get(stateIndex).stateType)
        {
            case Move:
                //animate position
                float normTime = currentTime / scheduledTime;
               
                if (normTime >= 1f)
                {
                    isStateDone = true;
                    transform_setPosition( destination );
                    
                    if(isDebugging) {
                    	double actuallyMoveDuration_s = (System.nanoTime() - lastNanoSinceMoveStart) / 1000000000.0;
                    	System.out.println("Actor.Update() - Move done in: " + actuallyMoveDuration_s + ", while scheduledTime was: " + scheduledTime);
                    }
                    
                    //Row Update - Start
                    Point position = transform_getPosition();
                    if(isDebugging)
                		System.out.println("Actor.Update() - Finishing Move, UpdatingActorRow, lastNextRow: " + lastNextRow);
                	
                	boolean goingDown = animStates.get(stateIndex).direction == Direction.Down;
	                int newRow = lastNextRow;
	                	
	                int offsetLastRow = lastYRow + 2;
	                int offsetNewRow = newRow + 2;
	                
	                mapLocationPanel.UpdateActorRow(panel, offsetLastRow, offsetNewRow, isDebugging,  this.actorData.actorId);
	                
	                lastYRow = newRow;
	                //BUG FIX: This fixed the bug causing the Market_Outcaste Venders to render in front of their stalls.(With the help of alterations to
	                //SetActorPath method)
	                //lastYRow = offsetNewRow;
	                
	                lastNextRow = GetNextMoveTileRow(position.y, goingDown);
                    //Row Update - End
                } else {
                	transform_setPosition( point_lerp(startPos, destination, normTime) );
                	
                	//See if we just arrived at a new tile with a different y coord
                    Point position = transform_getPosition();
                    if(currentTime > 0f
                        &&
                       (animStates.get(stateIndex).direction == Direction.Down || animStates.get(stateIndex).direction == Direction.Up)
                      ) {
                    	if(isDebugging)
                    		System.out.println("Actor.Update() - Move State: normTime >= 1f, lastNextRow: " + lastNextRow);
                    	
                    	boolean goingDown = animStates.get(stateIndex).direction == Direction.Down;
                    	
                    	//Attempting to make the row update occur at 50%
                    	int halfTile = Math.round(sceneLayoutInfo.tileHeight / 2f);
                        position.y = position.y + (goingDown ? halfTile : 0);
                    	
                    	int lastPixelPos = lastNextRow * sceneLayoutInfo.tileHeight;
    	                if(lastNextRow != lastYRow &&  ((position.y >= lastPixelPos && goingDown) || (position.y <= lastPixelPos && !goingDown)) ) {
    	                	int newRow = lastNextRow;
    	                	
    	                	if(isDebugging)
    	                		System.out.println("Actor.Update() - lastYRow: " + lastYRow);
    	                	
    	                	int offsetLastRow = lastYRow + (goingDown ? 2 : 1);
    	                	int offsetNewRow = newRow + (goingDown ? 2 : 1);
    	                	
    	                	mapLocationPanel.UpdateActorRow(panel, offsetLastRow, offsetNewRow, isDebugging,  this.actorData.actorId);
    	                	
    	                	lastYRow = newRow;
    	                }
    	                lastNextRow = GetNextMoveTileRow(position.y, goingDown);
                    }
                }
                currentTime += GetSpeed() * (MapLocationPanel.getAdjustedUpdateInterval_ms() / 1000f);
                break;
            case Turn:
                isStateDone = true;
                break;
            case Wait:
                isStateDone = !isWaiting;
                
                if(isDebugging && isStateDone) {
                	double actuallyWaitDuration_s = (System.nanoTime() - lastNanoSinceWaitStart) / 1000000000.0;
                	System.out.println("Actor.Update() - Wait done in: " + actuallyWaitDuration_s + ", while scheduledTime was: " + animStates.get(stateIndex).waitTime);
                }
                break;
            case OpenDoor: case CloseDoor:
                isStateDone = !isWaitingOnDoor;
                break;
            case Teleport:
                isStateDone = true;
                break;
            case EndLoop:
                System.err.println("Actor.Update() - The logic shouldn't make it this far when EndLoop is the current anim state!");
                break;
                
            case D_StallPoint:
                if (endStall_flag) {
                    endStall_flag = false;
                    isStateDone = true;
                }
                break;
            case D_Lock:
                isStateDone = true;
                //Make a call to the dialog controller
                mapLocationPanel.SetLock(true);
                break;
            case D_Release:
                isStateDone = true;
                //Make a call to the dialog controller
                mapLocationPanel.SetLock(false);
                break;
                
            default:
                System.err.println("Actor.Update() - Add support for: " + animStates.get(stateIndex).stateType);
                break;
        }

        if(isStateDone)
        {
            StartNextState();
        }
    }
    
    private int GetNextMoveTileRow(int pixelPosY, boolean goingDown) {
    	return (pixelPosY + ((pixelPosY % sceneLayoutInfo.tileHeight) * (goingDown ? 1 : -1))) / sceneLayoutInfo.tileHeight;
    }
    
    private void StartNextState()
    {
        if (animStates.size() == 0)
            return;

        if(isDebugging)
        	System.out.println("Actor.StartNextState()");
        
        stateIndex++;

        if (stateIndex >= animStates.size())
            stateIndex = 0;

        AnimState state = animStates.get(stateIndex);
        StateType stateType = state.stateType;

        if(isEffect) {
        	if(isDebugging)
        		System.out.println("Actor.StartNextState() - EFFECT - SetVisible(" + state.isLayingState + ")");
        		
        	if (state.isLayingState)
                panel.setVisible(true);
        	else
        		panel.setVisible(false);
        }
        
        if (stateType == StateType.Move)
        {
            animator_setSpeed(1f + (animStates.get(stateIndex).speedMod / moveSpeed_PixPerSec));
            
            animator_play(state.direction);
            previousDirection = state.direction;

            //Move state prep
            startPos = transform_getPosition();
            Point direction = directionVectorMap.get(state.direction);

            //convert the vectors, which are "number of tiles" space, into pixel screen space
            int xVector = direction.x * state.numberOfTiles * sceneLayoutInfo.tileWidth;
            int yVector = -( direction.y * state.numberOfTiles * sceneLayoutInfo.tileHeight );
            
            destination = new Point(startPos.x + xVector, startPos.y + yVector);
            
            currentTime = 0f;
            scheduledTime = state.numberOfTiles / GetSpeed();
            
            if(isDebugging) {
            	//System.out.println("Move State - startPos: " + startPos + ", destination: " + destination + ", scheduledTime: " + scheduledTime);
            	lastNanoSinceMoveStart = System.nanoTime();
            }
            
            int startRow = startPos.y / sceneLayoutInfo.tileHeight;
            
            lastNextRow = startRow + 2;
            
            if(isDebugging)
            	System.out.println("Actor.StartNextState() - Move - start row: " + startRow + ", nextTileRow: " + lastNextRow);
        }
        else
        {
            Direction direction = previousDirection;
            if (stateType == StateType.Turn || stateType == StateType.Teleport || stateType == StateType.Wait || stateType == StateType.EndLoop)
            {
                direction = state.direction;
                previousDirection = direction;
                
                if(isDebugging)
                	System.out.println("Actor.StartNextState() - NON-Move - Direction: " + direction);
            }
            
            boolean setToPlaySpeed = isEffect || state.isLayingState;
            if(setToPlaySpeed)
            	animator_setSpeed(1f + (state.speedMod / moveSpeed_PixPerSec));
            
            animator_play(direction);
            
            if(!setToPlaySpeed)
            	animator_setSpeed(0f);
        }
        
        //wait state
        if (stateType == StateType.Wait)
        {
            isWaiting = true;
            mapLocationPanel.CreateUpdateTimer(mapLocationPanel.new UpdateTimer(Math.round(state.waitTime * 1000), false, new ActionListener() {
        		@Override
        		public void actionPerformed(ActionEvent arg0) {
        			WaitComplete();
        		}
            }));
            
            if(isDebugging)
            	lastNanoSinceWaitStart = System.nanoTime();
        }
        
        //Open door state
        if (stateType == StateType.OpenDoor || stateType == StateType.CloseDoor)
        {
        	//Use the coordinates to animate the DoorTiles from the "doors" tilemap
            isWaitingOnDoor = true;
            boolean open = stateType == StateType.OpenDoor;
            DoorTile[] doors = mapLocationPanel.GetDoorTilesInBounds(state.doorCoord_min, state.doorCoord_max);
            if (doors == null || doors.length == 0)
                System.err.println("Actor.StartNextState() - Couldn't find any door tiles in the bounds: " + state.doorCoord_min + ", " + state.doorCoord_max);
            else
            	StartCoroutine_AnimateDoorTile(doors, open, 0.15f);
        }

        if (stateType == StateType.Teleport)
        {
        	//transform_setPosition(state.teleportPosition);
        	transform_setPosition(TryTranslateBySettlementOffset(state.teleportPosition));
        	
            //Set the sprites to visible or invisible based on the fadeIn bool
        	panel.SetTint(state.fadeIn ? new Color(originalColor.getRed(), originalColor.getGreen(), originalColor.getBlue(), 0) : originalColor, ColorBlend.Multiply);
        }

        if ((state.fadeDuration > 0f || state.fadeIn) && (stateType == StateType.Move || stateType == StateType.Wait || stateType == StateType.EndLoop))
        {
            if(state.fadeDuration <= 0f) {
                //Set invisible immediately once, right here. no need to animate
            	panel.SetTint(new Color(originalColor.getRed(), originalColor.getGreen(), originalColor.getBlue(), 0), ColorBlend.Multiply);
            	if(isDebugging)
            		System.out.println("Actor.StartNextState() - Setting transparent in anticipation of Fade In");
            } else {
            	if(isDebugging)
            		System.out.println("Actor.StartNextState() - Starting AnimateFade Timer");
            	StartCoroutine_AnimateFade(state.fadeDuration, state.fadeIn);
            }
        }
        
        if(stateType == StateType.EndLoop) {
        	if(isDebugging)
        		System.out.println("Actor.StartNextState() - EndLoop");
            mapLocationPanel.ActorDoneAnimating(this);
            //if (effectController != null)
            //    effectController.StopEffect();
            if(isEffect) {
            	if(isDebugging)
            		System.out.println("Actor.StartNextState() - EFFECT - Done, setVisible(false)");
            	panel.setVisible(false);
            }
        }
    }
    
    private void WaitComplete()
    {
        isWaiting = false;
    }
    
    /**
     * Door Open/Close
     * @param doorTileGroup
     * @param open
     * @param frameInterval
     */
    private void StartCoroutine_AnimateDoorTile(DoorTile[] doorTileGroup, boolean open, float frameInterval)
    {
        for(DoorTile doorTile : doorTileGroup)
        	doorTile.StartAnim(open);
      
        mapLocationPanel.CreateUpdateTimer(mapLocationPanel.new UpdateTimer(Math.round(frameInterval * 1000), true, new RepeatingListener() {
    		@Override
    		public void actionPerformed(ActionEvent arg0) {
    			boolean isDoneAnimating = false;
				for(DoorTile doorTile : doorTileGroup)
					isDoneAnimating = doorTile.FrameStep();
				if(isDoneAnimating) {
					isDone = true;
					isWaitingOnDoor = false;
					if(isDebugging)
						System.out.println("Stop Door Anim");
				}
    		}

    		private boolean isDone;
			@Override
			public boolean IsDoneRepeating() {
				return isDone;
			}
        }));
        
        if(isDebugging)
        	System.out.println("Animate Door");
    }
    
    /**
     * Fade In/Out
     * @param duration
     * @param fadeIn
     */
    private void StartCoroutine_AnimateFade(float duration, boolean fadeIn)
    {
        Color startColor = new Color(originalColor.getRed(), originalColor.getGreen(), originalColor.getBlue(), !fadeIn ? originalColor.getAlpha() : 0);
        Color endColor = new Color(originalColor.getRed(), originalColor.getGreen(), originalColor.getBlue(), fadeIn ? originalColor.getAlpha() : 0);
        
        if(isDebugging)
        	System.out.println("AnimateFade - startColor: " + startColor + ", endColor: " + endColor);
        
        mapLocationPanel.CreateUpdateTimer(mapLocationPanel.new UpdateTimer(Math.round(fadeInterval * 1000), true, new RepeatingListener() {
        	float timer = 0f;
    		@Override
    		public void actionPerformed(ActionEvent arg0) {
    			float normTime = timer / duration;
				if(timer >= duration) {
					panel.SetTint(endColor, ColorBlend.Multiply);
					isDone = true;
				} else {
					panel.SetTint(ColorLerp(startColor, endColor, normTime), ColorBlend.Multiply);
				}
				
				if(isDebugging)
					System.out.println("AnimateFade - normTime: " + normTime + ", tint color alpha: " + panel.GetTintColor().getAlpha());
				
				timer += fadeInterval;
    		}

    		private boolean isDone;
			@Override
			public boolean IsDoneRepeating() {
				return isDone;
			}
        }));
    }
    
    /**
     * The dialography tells the actor when to halt their animation and animState progression.
     */
    public void EndStall()
    {
        endStall_flag = true;
    }
}
