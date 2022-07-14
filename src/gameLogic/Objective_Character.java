package gameLogic;

import data.CharacterData;

public class Objective_Character extends Objective {
	public Objective_Character(TargetType objectiveType, int maxNumberOfExclusivePersonnelAssignments, CharacterData targetCharacterData, CharacterTask characterTask) {
		super(objectiveType, maxNumberOfExclusivePersonnelAssignments);
		this.targetCharacterData = targetCharacterData;
		this.characterTask = characterTask;
	}
	//Character related objectives
	private CharacterData targetCharacterData;
	private CharacterTask characterTask;
	@Override
	public Object GetTarget() {
		return (Object)targetCharacterData;
	}
	@Override
	public Object GetTask() {
		return (Object)characterTask;
	}
}
