package enums;

public enum TestType {
	None(0),
	StatTest(1),
	Random(2),
	BattleOutcome(3),
	ItemPossession(4),
	SkillPossession(5),
	ClassPossession(6);
	
	private final int value;
    private TestType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}