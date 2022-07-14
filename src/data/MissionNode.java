package data;

import java.io.Serializable;

import enums.TransitionType;


public class MissionNode implements Serializable {
	private static final long serialVersionUID = 496451255064622762L;
	
	public MissionNode(String previousMissionId, TransitionType transitionType, String nextMissionId) {
		this.previousMissionId = previousMissionId;
		this.transitionType = transitionType;
		this.nextMissionId = nextMissionId;
	}
	public String previousMissionId;
	public TransitionType transitionType;
	public String nextMissionId;
}
