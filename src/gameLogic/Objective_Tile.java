package gameLogic;

import gameLogic.Board.Tile;


public class Objective_Tile extends Objective {
	public Objective_Tile(TargetType objectiveType, int maxNumberOfExclusivePersonnelAssignments, Tile targetTile, TileTask tileTask) {
		super(objectiveType, maxNumberOfExclusivePersonnelAssignments);
		this.targetTile = targetTile;
		this.tileTask = tileTask;
	}
	//Tile based objectives
	private Tile targetTile;
	private TileTask tileTask;
	@Override
	public Object GetTarget() {
		return (Object)targetTile;
	}
	@Override
	public Object GetTask() {
		return (Object)tileTask;
	}
}
