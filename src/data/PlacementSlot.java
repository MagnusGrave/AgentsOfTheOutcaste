package data;

import java.awt.Point;
import java.io.Serializable;

import enums.SlotType;


public class PlacementSlot implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2324821805142885829L;
	
	public PlacementSlot(SlotType slotType, Point point, Point suggestedDirection) {
		this.slotType = slotType;
		this.point = point;
		this.suggestedDirection = suggestedDirection;
	}
	public SlotType slotType;
	public Point point;
	public Point suggestedDirection;
}
