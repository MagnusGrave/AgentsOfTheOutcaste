package enums;

public enum WeaponGroup {
	Blunt(0),
	Edged(1),
	Pole(2),
	Versitile(3),
	Ranged(4);
	
	private final int value;
    private WeaponGroup(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
