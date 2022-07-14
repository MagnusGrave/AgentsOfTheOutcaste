package data;

public class CombatEffect_ChanceToSurvive extends CombatEffect {
	private static final long serialVersionUID = -9146134660624837514L;

	public CombatEffect_ChanceToSurvive(float chanceToSurvive_chance) {
		super(null, -1);
		
		this.chanceToSurviveWith1Hp = true;
		this.chanceToSurvive_chance = chanceToSurvive_chance;
	}
}