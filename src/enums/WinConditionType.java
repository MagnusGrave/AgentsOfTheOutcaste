package enums;

public enum WinConditionType {
	DeathMatch(0),
	Assassination(1),
	ProtectAllies(2),
	OccupyTile(3),
	SurviveForTime(4);
	
	private final int value;
    private WinConditionType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}