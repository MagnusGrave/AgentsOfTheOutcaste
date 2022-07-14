package enums;

public enum StatusType {
	Blind(0), Cripple(1), Silence(2), Daze(3),
	
	Charmed(4), Fear(5), Goad(6),
	
	Accelerated(7);
	
	//, Poisoned(8); //Poison has been deemed unnecessary as a status effect. Its only damage-over-time and the other two time based hp/state modifiers(Lingering Potion and Cures) dont use a StatusType anyways.
	
	
	private final int value;
    private StatusType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}