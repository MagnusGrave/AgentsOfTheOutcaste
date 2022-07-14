package data;

import enums.BattleItemType;

public class CombatEffect_Debuff extends CombatEffect {
	private static final long serialVersionUID = 3650982590323944283L;

	public CombatEffect_Debuff(
			int effectTurnDuration,
			
			AttributeMod[] attributeMods_debuffs
	) {
		super(BattleItemType.Debuff, effectTurnDuration);
		
		this.attributeMods_debuffs = attributeMods_debuffs;
	}
}