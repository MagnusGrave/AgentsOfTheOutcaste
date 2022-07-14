package data;

import enums.BattleItemType;

public class CombatEffect_Potion extends CombatEffect {
	private static final long serialVersionUID = 6603587016235672300L;

	public CombatEffect_Potion(
			int effectTurnDuration,
			
			int customHealingPoints, boolean healAlliesButDamageEnemies
	) {
		super(BattleItemType.Potion, effectTurnDuration);
		
		this.customHealingPoints = customHealingPoints;
		this.healAlliesButDamageEnemies = healAlliesButDamageEnemies;
	}
}