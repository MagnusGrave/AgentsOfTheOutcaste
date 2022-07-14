package data;

import java.io.Serializable;

import enums.InteractionType;

public class InteractionState implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4782297990057757074L;
	
	public InteractionState(int layerIndex, InteractionType type) {
		this.layerIndex = layerIndex;
		this.type = type;
	}
	//Graph Address
	public int layerIndex;
	public InteractionType type;
	//State data
	public boolean isUsed;
	public int daysTillRefresh;
}
