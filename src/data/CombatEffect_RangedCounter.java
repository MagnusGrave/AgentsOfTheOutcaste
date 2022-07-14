package data;

public class CombatEffect_RangedCounter extends CombatEffect {
	private static final long serialVersionUID = -828531106210724941L;

	public CombatEffect_RangedCounter() {
		super(null, -1);
		
		this.rangedCounterAttack = true;
	}
}
