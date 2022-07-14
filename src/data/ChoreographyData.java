package data;

import java.io.Serializable;

import dataShared.ActorData;

public class ChoreographyData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6625259135763700101L;
	
	/**
	 * Will be used as the prefix when searching for the choreo file, like: "[mapLocationName]_[choreographyName]"
	 */
	public String mapLocationName;
	/**
	 * Will be used as the suffix when searching for the choreo file, like: "[mapLocationName]_[choreographyName]"
	 */
	public String choreographyName;
	
	//All the included ActorData
	public ActorData[] actorData;
}
