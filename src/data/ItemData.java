package data;

import java.io.Serializable;
import java.util.Random;
import java.util.UUID;

import enums.ClassType;
import enums.EquipmentType;
import enums.ItemType;
import enums.KarmaType;
import enums.WeightType;
import gameLogic.Items;



public class ItemData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6784985570743277704L;
	
	//Direct from sheet - Column B
	private final String itemImageDirectory = "itemImages/";
	private String fileName;
	public String GetFilePath() {
		if(fileName.equals("Question.png"))
			return fileName;
		else
			return itemImageDirectory + fileName;
	}
	//Direct from sheet - Column C
	private String name;
	public String getName() { return name; }
	//Direct from sheet - Column D
	private String description;
	public String getDescription() { return description; }
	
	//From Sheet name: "Weapons", etc.
	private ItemType type;
	public ItemType getType() { return type; }
	//from combining items on multiple sheets
	private ItemType[] itemUseTypes;
	public ItemType[] itemUseTypes() { return itemUseTypes; }
	
	//A possible subtype for any kind of equipment or Battle Items
	private Stats stats;
	public Stats getStats() { return stats; }
	//-OR-
	//The other possible subtype for Journey Consumables
	private JourneyConsumableTraits journeyConsumableTraits;
	public JourneyConsumableTraits getJourneyConsumableTraits() { return journeyConsumableTraits; }
	
	//Directly from Column P
	//If this value is null it means that the value should always be zero
	private String valueTier;
	public String getValueTier() { return valueTier; }
	
	private void TryGetDirectValue(String valueString) {
		try {
			value = Integer.parseInt(valueString);
			this.valueTier = null;
		} catch(NumberFormatException e) {
			//do nothing, this will happen almost always
		}
	}
	
	//this will be the direct int value assigned in the spreadsheet or the resulting value price determined by the loot tables and price scale
	private int value;	
	public int getValue() { return value; }
	
	
	private int quantity;
	public int getQuantity() { return quantity; }
	
	private String relatedMissionId;
	public String getRelatedMissionId() { return relatedMissionId; }
	
	//This is neccessary because the user/designer-defined names could conflict with comparisons
	private String id;
	public String getId() { return id; }
	
	
	//public ItemData() {};
	//This is most likely an unnecessary relic of my novice java assumptions.
	
	/**
	 * Constructor for duplicating an item with a specified quantity. For custom objects that aren't identical to a base item.
	 * i.e. custom items that are created thru out gameplay: letters that have custom names and descriptions for storytelling and flavor
	 * ("Shogun's Letter To Emperor", etc).
	 * @param other
	 * @param quantity
	 */
	public ItemData(ItemData other, int quantity) {
		this.fileName = other.fileName;
		this.id = other.id;
		this.name = other.name;
		this.description = other.description;
		this.type = other.type;
		this.stats = other.stats;
		this.quantity = quantity;
		this.valueTier = other.valueTier;
		TryGetDirectValue(this.valueTier);
		this.relatedMissionId = other.relatedMissionId;
	}
	
	/**
	 * Constructor for duplicating an item with a specified quantity and a new or blank mission id. For custom objects that aren't identical to a base item.
	 * i.e. custom items that are created thru out gameplay: letters that have custom names and descriptions for storytelling and flavor
	 * ("Shogun's Letter To Emperor", etc).
	 * @param other
	 * @param quantity
	 */
	public ItemData(ItemData other, int quantity, String relatedMissionId) {
		this(other, quantity);
		this.relatedMissionId = relatedMissionId;
	}
	
	/**
	 * Constructor an item using only its reference id and a unique quantity. For items that are identical to a base item.
	 * @param itemId
	 * @param quantity
	 */
	public ItemData(String itemId, int quantity) {
		this(Items.getById(itemId), quantity);
	}
	
	/**
	 * Constructor an item using only its reference id and provide it with a new or blank relatedMissionId. For items that are identical to a base item.
	 * @param itemId
	 * @param quantity
	 */
	public ItemData(String itemId, int quantity, String relatedMissionId) {
		this(Items.getById(itemId), quantity);
		this.relatedMissionId = relatedMissionId;
	}
	
	/**
	 * Constructor for Weapons, Armor and BattleItem items.
	 * @param fileName
	 * @param id
	 * @param name
	 * @param description
	 * @param type
	 * @param stats
	 * @param quantity
	 * @param valueTier
	 * @param relatedMissionId
	 */
	public ItemData(String fileName, String id, String name, String description, ItemType type, Stats stats, int quantity, String valueTier, String relatedMissionId) {
		this(fileName, id, name, description, type, quantity, valueTier, relatedMissionId);
		this.stats = stats;
	}
	
	/**
	 * Constructor for JourneyConsumable items.
	 * @param fileName
	 * @param id
	 * @param name
	 * @param description
	 * @param type
	 * @param journeyConsumableTraits
	 * @param quantity
	 * @param valueTier
	 * @param relatedMissionId
	 */
	public ItemData(String fileName, String id, String name, String description, ItemType type, JourneyConsumableTraits journeyConsumableTraits,
			int quantity, String valueTier, String relatedMissionId)
	{
		this(fileName, id, name, description, type, quantity, valueTier, relatedMissionId);
		this.journeyConsumableTraits = journeyConsumableTraits;
	}
	
	/**
	 * Constructor for Misc items.
	 * @param fileName
	 * @param id
	 * @param name
	 * @param description
	 * @param type
	 * @param quantity
	 * @param valueTier
	 * @param relatedMissionId
	 */
	public ItemData(String fileName, String id, String name, String description, ItemType type, int quantity, String valueTier, String relatedMissionId) {
		this.fileName = fileName;
		if(id == null)
			this.id = UUID.randomUUID().toString();
		else
			this.id = id;
		this.name = name;
		this.description = description;
		this.type = type;
		this.quantity = quantity;
		this.valueTier = valueTier;
		TryGetDirectValue(this.valueTier);
		this.relatedMissionId = relatedMissionId;
	}
	
	/**
	 * For merging multi-use items(BattleItem and JourneyConsumable) into one ItemData
	 * @param other
	 * @param quantity
	 */
	public ItemData(ItemType[] multifariousTypes, ItemData other, Stats stats, JourneyConsumableTraits journeyConsumableTraits) {
		this(other, 1);
		this.itemUseTypes = multifariousTypes;
		this.stats = stats;
		this.journeyConsumableTraits = journeyConsumableTraits;
	}
	
	//used to creatue new instances for use inside of related mission so that the item can carry its relatedMissionId thru its lifetime
	/*public ItemData GetUniqueInstance(ItemData itemData, int quantity, String relatedMissionId) {
		return new ItemData(itemData.fileName, itemData.id, itemData.name, itemData.description, itemData.type,itemData.stats, quantity, itemData.valueTier, relatedMissionId);
	}*/
	//This is essentially a duplicate of the constructor above that serves as a means of creating a new instance of an item with a new relatedMissionId.
	
	public ItemData GetItemWithQuantity(int quantity) {
		return new ItemData(fileName, id, name, description, type, stats, quantity, valueTier, relatedMissionId);
	}
	
	public static ItemData CreateRandom(String optionalRelatedMissionId) {
		Random r = new Random();
		return new ItemData(
			"Question.png",
			null,
			"Item " + (int)Math.floor(Math.abs(r.nextDouble() * 10000)),
			"A randomly generated item.",
			ItemType.values()[r.nextInt(ItemType.values().length)],
			new Stats(
				EquipmentType.values()[r.nextInt(EquipmentType.values().length)],
				WeightType.values()[r.nextInt(WeightType.values().length)],
				KarmaType.values()[r.nextInt(KarmaType.values().length)],
				new ClassType[] { ClassType.BANDIT, ClassType.MONK, ClassType.NINJA, ClassType.RONIN, ClassType.PRIEST },
				r.nextInt(6), r.nextInt(6), r.nextInt(6), r.nextInt(6),
				null,
				null
			),
			1,
			"T1-C",
			optionalRelatedMissionId
			);
	}
	
	@Override
	public String toString() {
		return  "<--------------------------" + "\n" +
				"fileName: " + fileName + "\n" +
				"id: " + id + "\n" +
				"name: " + name + "\n" +
				"description: " + description + "\n" +
				"type: " + (type != null ? type.toString() : "null") + "\n" +
				"stats: " + (stats != null ? stats.toString() : "null") + "\n" +
				"quantity: " + quantity + "\n" +
				"valueTier: " + valueTier + "\n" +
				"relatedMissionId: " + relatedMissionId + "\n" +
				"------------------------->";
	}
}
