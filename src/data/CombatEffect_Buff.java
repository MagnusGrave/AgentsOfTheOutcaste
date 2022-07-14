package data;

import enums.BattleItemType;

public class CombatEffect_Buff extends CombatEffect {
	private static final long serialVersionUID = -6031291574235223198L;

	public CombatEffect_Buff(
			int effectTurnDuration,

			AttributeMod[] attributeMods_buffs
	) {
		super(BattleItemType.Buff, effectTurnDuration);
		
		this.attributeMods_buffs = attributeMods_buffs;
	}
}