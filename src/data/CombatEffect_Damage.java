package data;

import enums.BattleItemType;
import enums.ElementType;

public class CombatEffect_Damage extends CombatEffect {
	private static final long serialVersionUID = -2890758197978696252L;

	public CombatEffect_Damage(
			int effectTurnDuration,
			
			boolean useMainAttackAsBase, int customBaseDamage,
			ElementType elementalDamageType
	) {
		super(BattleItemType.Damage, effectTurnDuration);

		this.useMainAttackAsBase = useMainAttackAsBase;
		this.customBaseDamage = customBaseDamage;
		this.elementalDamageType = elementalDamageType;
	}
}