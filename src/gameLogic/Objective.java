package gameLogic;

import java.util.ArrayList;
import java.util.List;


public abstract class Objective {
	public enum TargetType {
		Character,
		Tile
	};
	public enum CharacterTask {
		Kill, //Kill target, get within range to do so
		Heal, //Heal target, get within range to do so
		Surround, //Surround target in all four directions
		Cover //Move to the tile behind the target to cover their back
	};
	public enum TileTask {
		Surround, //Surround in all four directrions of target
		Occupy //Move to the tile
	};
	
	public Objective(TargetType objectiveType, int maxNumberOfExclusivePersonnelAssignments) {
		this.objectiveType = objectiveType;
		this.maxNumberOfExclusivePersonnelAssignments = maxNumberOfExclusivePersonnelAssignments;
	}
	//General objective properties
	protected TargetType objectiveType;
	public TargetType ObjectiveType() { return objectiveType; }
	protected int maxNumberOfExclusivePersonnelAssignments;
	public int getMaxNumberOfExclusivePersonnelAssignments() { return maxNumberOfExclusivePersonnelAssignments; }
	private List<CharacterBase> assignedExclusivePersonnel = new ArrayList<CharacterBase>();
	public List<CharacterBase> getAssignedExclusivePersonnel() { return assignedExclusivePersonnel; }
	public void AssignExclusivePersonnel(CharacterBase assignee) {
		assignedExclusivePersonnel.add(assignee);
	}
	public void RemoveExclusivePersonnel(CharacterBase assignee) {
		assignedExclusivePersonnel.remove(assignee);
	}
	
	public abstract Object GetTarget();
	public abstract Object GetTask();
}
