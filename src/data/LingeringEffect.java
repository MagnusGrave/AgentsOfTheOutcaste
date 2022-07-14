package data;

import java.io.Serializable;

public class LingeringEffect implements Serializable {
	private static final long serialVersionUID = -4935683336876939886L;

	public LingeringEffect(CombatEffect combatEffect) {
		this.combatEffect = combatEffect;
		this.turnsRemaining = combatEffect.effectTurnDuration;
	}
	public LingeringEffect(CombatEffect combatEffect, int hpChangePerTurn) {
		this(combatEffect);
		
		if(combatEffect instanceof CombatEffect_Damage || combatEffect instanceof CombatEffect_Potion) {
			this.hpChangePerTurn = hpChangePerTurn;
		} else {
			System.err.println("CharacterBase.LingeringEffect Constructor - Only CombatEffect_Damage and CombatEffect_Potion should use the constructor with the hpChangePerTurn parameter!");
		}
	}
	
	public CombatEffect combatEffect;
	public int turnsRemaining;
	public int hpChangePerTurn;
	
	/**
	 * Call this at the end of this Character's turn
	 */
	public boolean DecrementAndCheckDone() {
		turnsRemaining--;
		return turnsRemaining < 0;
	}
}
