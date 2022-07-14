package data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import data.BattleData;

public class BattleState implements Serializable {
	private static final long serialVersionUID = -421213070713430596L;

	//Unchanging elements
	public BattleData battleData;
	
	//Changing Elements
	public boolean isPlacementPhase;
	public int turnCount;
	public int turnPhases;
	public int turnOrderIndex;
	public List<CombatEffect> remainingLingeringAnimCombatEffects;
	public Map<Integer, CharacterBaseData> enemyBaseDataMap;
	public Map<Integer, CharacterBaseData> allyBaseDataMap;
	//TODO add support for this
	public Map<Integer, CharacterBaseData> npcAllyBaseDataMap;
}
