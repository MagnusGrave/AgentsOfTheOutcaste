package enums;

/**
 * C# data structure mimic for AnimState enum.
 * @author Magnus
 *
 */
public enum StateType {
	Move(0),
	Turn(1),
	Wait(2),
	OpenDoor(3),
	CloseDoor(4),
	Teleport(5),
	EndLoop(8),
	D_StallPoint(9),
	D_Lock(10),
	D_Release(11);
	
	private final int value;
    private StateType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
	
	public static StateType fromInteger(int x) {
        switch(x) {
	        case 0:
	            return Move;
	        case 1:
	            return Turn;
	        case 2:
	            return Wait;
	        case 3:
	            return OpenDoor;
	        case 4:
	            return CloseDoor;
	        case 5:
	            return Teleport;
	        case 8:
	            return EndLoop;
	        case 9:
	            return D_StallPoint;
	        case 10:
	            return D_Lock;
	        case 11:
	            return D_Release;
	        default:
	        	System.err.println("ActorPath.Direction - Add support for int-to-Direction: " + x);
	        	return null;
        }
    }
}