package enums;

public enum WeaponType {
	//WeaponGroup.Blunt
	WoodenSword(0),
	Club(1),
	Jitte(2),
	Fan(3),
	Stave(4),
	
	//WeaponGroup.Edged
	Katana(5),
	DaiKatana(6),
	Kodachi(7),
	BranchSword(8),
	Tanto(9),
	Ninjato(10),
	
	//WeaponGroup.Pole
	Naginata(11),
	Spear(12),
	
	//WeaponGroup.Versitile
	ThrowingKnife(12),
	Kunai(13),
	Kusarigama(14),
	Talisman(15),
	
	//WeaponGroup.Ranged
	Bow(16),
	Gun(17),
	Shuriken(18);
	
	private final int value;
    private WeaponType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
