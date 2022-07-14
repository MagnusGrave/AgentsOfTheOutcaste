package data;

import enums.BattleItemType;

public class CombatEffect_Revive extends CombatEffect {
	private static final long serialVersionUID = -8328140939604319112L;

	public CombatEffect_Revive(
			int effectTurnDuration,
			
			float reviveHealthPercentage
	) {
		super(BattleItemType.Revive, effectTurnDuration);
		
		this.reviveHealthPercentage = reviveHealthPercentage;
	}
}