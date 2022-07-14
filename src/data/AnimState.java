package data;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.Serializable;

import enums.Direction;
import enums.StateType;


public class AnimState implements Serializable {
	private static final long serialVersionUID = 7257554305022727004L;
	
	public void RebuildFromRaw() {
		stateType = StateType.fromInteger(stateTypeIndex);
		direction = Direction.fromInteger(directionIndex);
	}
	
	public StateType stateType;
	/**
	 * Used internally as a raw version of StateType
	 */
	private int stateTypeIndex;
	
    //Move state, used for Turn and Teleport states as well
    public Direction direction;
    /**
	 * Used internally as a raw version of Direction
	 */
    private int directionIndex;
    
    public int numberOfTiles;
    
    public float speedMod;
    
    //Wait State
    public float waitTime;
    //OpenDoor and CloseDoor 
    public Point doorCoord_min;
    public Point doorCoord_max;
    //Teleport
    //this may need to be a Point2D.Float
    public Point2D.Float teleportPosition;

    //Fading, can be applied to Move, Wait & EndLoop
    public float fadeDuration;
    public boolean fadeIn;
    
    //Anim State Override
    public boolean isLayingState;
}
