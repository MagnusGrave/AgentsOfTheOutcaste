package dataShared;

import java.io.Serializable;

public class ActorData implements Serializable
{
	private static final long serialVersionUID = -2876303994573749157L;
	
	public ActorData(String javaSheetFilePath, int nonActorFrames_startIndex, int nonActorFrames_endIndex, Util_ColorAdapter originalColor, float zRotation) {
		this.javaSheetFilePath = javaSheetFilePath;
		this.nonActorFrames_startIndex = nonActorFrames_startIndex;
		this.nonActorFrames_endIndex = nonActorFrames_endIndex;
		this.originalColor = originalColor;
		this.zRotation = zRotation;
	}
	
	public String actorId;
    public String characterDataId; //may be null if this actor is a non-character(an Effect) actor

    public String javaSheetFilePath;
    public int nonActorFrames_startIndex = -1;
    public int nonActorFrames_endIndex = -1;
    public float zRotation;

    public Util_ColorAdapter originalColor;
    
    
    /**
     * This is a bolt-on attribute of Actors to help differenciate nature layer actors from settlement layer actors.
     */
    private boolean isSettlementLayerActor;
    /**
     * This method sets isSettlementLayerActor equal to true and should be called on all settlement actors after their
     * instantiation.
     */
    public void SetIsSettlementLayerActor() {
    	isSettlementLayerActor = true;
    }
    /**
     * This is a bolt-on attribute of Actors to help differenciate nature layer actors from settlement layer actors.
     */
    public boolean IsSettlementLayerActor() { return isSettlementLayerActor; }
}
