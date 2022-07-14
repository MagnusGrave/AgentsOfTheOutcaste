package data;

import java.io.Serializable;
import java.util.List;

public class ItemDataStorage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4215231092590273782L;
	
	public ItemDataStorage(List<ItemData> itemDataArray) {
		this.itemDataArray = itemDataArray.stream().toArray(ItemData[]::new);
	}
	
	public ItemData[] itemDataArray;
}
