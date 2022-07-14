package data;

public class CombatEffect_AutoDodgeMelee extends CombatEffect {
	private static final long serialVersionUID = -1328925981435974359L;

	public CombatEffect_AutoDodgeMelee() {
		super(null, -1);
		
		this.autoDodgeBasicMelee = true;
	}
}