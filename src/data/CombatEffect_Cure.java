package data;

import enums.BattleItemType;
import enums.StatusType;

public class CombatEffect_Cure extends CombatEffect {
	private static final long serialVersionUID = -3013532646223924840L;

	public CombatEffect_Cure(
			int effectTurnDuration,
			
			StatusType[] cures
	) {
		super(BattleItemType.Cure, effectTurnDuration);
		
		this.cures = cures;
	}
}