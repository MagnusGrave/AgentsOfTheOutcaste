package enums;

/**
 * C# data structure mimic for AnimState enum.
 * @author Magnus
 *
 */
public enum Direction {
	Down(0),
	Left(1),
	Right(2),
	Up(3);
	
	private final int value;
    private Direction(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }

    public static Direction fromInteger(int x) {
        switch(x) {
	        case 0:
	            return Down;
	        case 1:
	            return Left;
	        case 2:
	            return Right;
	        case 3:
	            return Up;
	        default:
	        	System.err.println("ActorPath.Direction - Add support for int-to-Direction: " + x);
	        	return null;
        }
    }
}