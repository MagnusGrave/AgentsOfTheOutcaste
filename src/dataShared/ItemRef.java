package dataShared;

import java.io.Serializable;

public class ItemRef implements Serializable
{
	private static final long serialVersionUID = -5217841376733307815L;
	
	public ItemRef(String itemId, int quantity) {
		this.itemId = itemId;
		this.quantity = quantity;
	}
	
	public String itemId;
    public int quantity;
}
