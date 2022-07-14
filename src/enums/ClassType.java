package enums;

public enum ClassType {
	//Rebels
	RONIN(0), NINJA(1), MONK(2), BANDIT(3), PRIEST(4),
	KAMI_AR(5), KAMI_ER(6), KAMI_EY(7), KAMI_IN(8), KAMI_KA(9),
	KAMI_KO(10), KAMI_KY(11), KAMI_OI(12), KAMI_OK(13), KAMI_WA(14),
	//Feudalists
	SURF(15), DIAMYO(16),
	NEKOMATA(17), ONI(18);
	
	private final int value;
    private ClassType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
