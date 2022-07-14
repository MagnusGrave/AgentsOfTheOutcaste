package enums;

public enum MissionIndicatorType {
	MissionOrange(0),
	MissionBlue(1),
	MissionRed(2),
	MissionBlack(3),
	MissionQuestionOrange(4),
	MissionQuestionBlue(5),
	MissionQuestionFloral(6),
	MissionQuestionBlack(7);
	
	private final int value;
    private MissionIndicatorType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
