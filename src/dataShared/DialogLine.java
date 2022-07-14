package dataShared;

import java.io.Serializable;

public class DialogLine implements Serializable {
	private static final long serialVersionUID = -1334613945533242570L;
	
	public String actorId;
	
    //Actor Control Props
    public boolean doesStallChoreo;
    public String[] ignoreActorIds;
    
    //Dialog props
    public boolean useLeftPortrait;
    public String dialogText;
}
