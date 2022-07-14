package dataShared;

import java.awt.geom.Point2D;
import java.io.Serializable;

import data.AnimState;

public class ActorPathData implements Serializable {
	private static final long serialVersionUID = 2961255522407206871L;
	
	public AnimState[] animStates;
    public String actorId;
    public String characterDataId; //may be null if this actor is a non-character(an Effect) actor
    public Point2D.Float startLocation;
    
    //this will track whether this AnimPath was a Looping type(which will disqualify it from dialog control interlinking and it will persist across all dialographies unless overridden)
    public boolean isLoopPath;
}
