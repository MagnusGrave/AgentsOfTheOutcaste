package data;

import enums.BattleItemType;
import enums.StatusType;

public class CombatEffect_Status extends CombatEffect {
	private static final long serialVersionUID = -1288017920991998230L;

	public CombatEffect_Status(
			int effectTurnDuration,
			
			StatusType statusEffect
	) {
		super(BattleItemType.Status, effectTurnDuration);
		
		this.statusEffect = statusEffect;
	}
}
