package data;

import java.io.Serializable;

import enums.SettlementType;

public class JourneyConsumableTraits implements Serializable {
	/**
	 * Auto-generated serial id
	 */
	private static final long serialVersionUID = -6139331093888821772L;
	
	public JourneyConsumableTraits(int duration_days, int staminaMod, int staminaRegenRateMod, int terrainRiskMod, int terrainStaminaMod, int settlementRiskMod, int settlementStaminaMod,
			SettlementType[] encounterProtections, SettlementType[] encounterAttractants)
	{
		this.duration_days = duration_days;
		this.staminaMod = staminaMod;
		this.staminaRegenRateMod = staminaRegenRateMod;
		
		this.terrainRiskMod = terrainRiskMod;
		this.terrainStaminaMod = terrainStaminaMod;
		this.settlementRiskMod = settlementRiskMod;
		this.settlementStaminaMod = settlementStaminaMod;
		
		this.encounterProtections = encounterProtections;
		this.encounterAttractants = encounterAttractants;
	}
	
	public int duration_days;
	
	public int staminaMod;
	public int staminaRegenRateMod;
	
	//Travel Performance
	public int terrainRiskMod;
	public int terrainStaminaMod;
	public int settlementRiskMod;
	public int settlementStaminaMod;
	
	//Only Fleeting Types: MilitaryEncampment, Battle, AssassinationTarget, ElementalDisturbance, Kami, YokaiActivity, YokaiAttack
	public SettlementType[] encounterProtections;
	public SettlementType[] encounterAttractants;
}
