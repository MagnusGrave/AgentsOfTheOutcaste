package data;

import java.io.Serializable;

import enums.BattleItemType;
import enums.ElementType;
import enums.StatusType;

public abstract class CombatEffect implements Serializable {
	private static final long serialVersionUID = -907936339906982246L;

	public CombatEffect(BattleItemType battleItemTypeEffect, int effectTurnDuration) {
		this.battleItemTypeEffect = battleItemTypeEffect;
		this.effectTurnDuration = effectTurnDuration;
	}
	
	//Overarching Effect Behavior - Start
	
	/**
	 * This governs how the effect is handled. If this is null then it means we look to our
	 */
	public BattleItemType battleItemTypeEffect;
	/**
	 * Damage may be applied over time by setting a CombatEffect's effectTurnDuration > 0 accompanied by Damage info
	 */
	public int effectTurnDuration;
	
	//Combat mechanics
	public boolean meleeCounterAttack;
	public boolean rangedCounterAttack;
	public boolean autoDodgeProjectiles;
	public boolean autoDodgeBasicMelee;
	public boolean chanceToSurviveWith1Hp;
	public float chanceToSurvive_chance;
	
	/* Damage */
	//Base Damage
	public boolean useMainAttackAsBase;
	public int customBaseDamage;
	//Elemental Damage
	public ElementType elementalDamageType;

	/* Potion */
	//Healing (Can be offensive if healAlliesButDamageEnemies is true)
	public int customHealingPoints;
	public boolean healAlliesButDamageEnemies;
	
	/* Status */
	public StatusType statusEffect;
	
	/* Cure */
	public StatusType[] cures;
	
	/* Buff */
	public AttributeMod[] attributeMods_buffs;
	
	/* Debuff */
	public AttributeMod[] attributeMods_debuffs;
	
	/* SpiritTool */
	//May be moot, it'll depend on how the spirit tools are designed
	
	/* Revive */
	/**
	 * If this is non-zero then apply the revive mechanic.
	 */
	public float reviveHealthPercentage;
	
	//Overarching Effect Behavior - End
}
