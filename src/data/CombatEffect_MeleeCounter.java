package data;

public class CombatEffect_MeleeCounter extends CombatEffect {
	private static final long serialVersionUID = -1984123056096990027L;

	public CombatEffect_MeleeCounter() {
		super(null, -1);
		
		this.meleeCounterAttack = true;
	}
}