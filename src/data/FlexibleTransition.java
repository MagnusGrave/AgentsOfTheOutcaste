package data;

import java.io.Serializable;

import enums.EnvironmentType;

/**
 * This will serve as a sequential link from one mission to the next in a questline. These missions don't lead directly from one interaction to the next mission, they're exploration based.
 * They trigger after a minimum number of MapLocations visited and then only with a chance to occur. With the new caveat of needing to place MapLocation on the worldmap at the start of the game: 
 * these FlexibleTransitions can only continue to function in this manor as long as the nextMission uses a generic MapLocation EnvironmentType instead of a static unique Map Location.
 * @author Magnus
 *
 */
public class FlexibleTransition implements Serializable {
	private static final long serialVersionUID = -351756768749414718L;
	
	public FlexibleTransition(String prereqMissionId, int minimumLocationsVisited, float chanceToOccur, String nextMissionId, EnvironmentType nextMissionGenericLocationEnvironmentType) {
		this.prereqMissionId = prereqMissionId;
		this.minimumLocationsVisited = minimumLocationsVisited;
		this.chanceToOccur = chanceToOccur;
		this.nextMissionId = nextMissionId;
		
		if(nextMissionGenericLocationEnvironmentType == null)
			System.err.println("Missions.FlexibleTransition - NextMission: " + nextMissionId + " needs a non-null genericLocationEnvironmentType if it is to be used by a flexible transition.");
	}
	private String prereqMissionId;
	private int minimumLocationsVisited;
	private float chanceToOccur;
	private String nextMissionId;
	public String PrereqMissionId() { return prereqMissionId; }
	public int MinimumLocationsVisited() { return minimumLocationsVisited; }
	public float ChanceToOccur() { return chanceToOccur; }
	public String NextMissionId() { return nextMissionId; }
}