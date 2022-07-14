package data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import enums.ClassType;
import enums.EquipmentType;
import enums.ItemType;
import enums.StatType;
import gameLogic.AbilityManager.Ability;
import gameLogic.AbilityManager;
import gameLogic.CharacterBase;

public class CharacterData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8610326985096080372L;
	
	private String name;
	public String getName() { return name; }
	private ClassType type;
	public ClassType getType() { return type; }
	private int strength;
	public int getStrength() { return strength; }
	private int intellect;
	public int getIntellect() { return intellect; }
	private int endurance;
	public int getEndurance() { return endurance; }
	private int speed;
	public int getSpeed() { return speed; }
	//This is a normalized level representation, ei.  for expNorm 1.43 @ 100exp/Lvl = 143exp, Lvl 1 ... for expNorm 2.5 @ 50exp/Lvl = 125exp, Lvl 2
	private float expNorm;
	
	private List<Boolean> abilitiesNodeLearnedStatusTree = new ArrayList<Boolean>();
	public List<Boolean> getAbilitiesNodeLearnedStatusTree() {
		//return new ArrayList<Boolean>( abilitiesNodeTree );
		//TODO DEBUGGING
		System.err.println("DEBUGGING @ CharacterData.getAbilitiesNodeLearnedStatusTree() - Considered all AbilitiesNodes to be learned.");
		List<Boolean> debugTree = new ArrayList<Boolean>();
		debugTree.add(new Boolean(true));
		debugTree.add(new Boolean(true));
		debugTree.add(new Boolean(true));
		debugTree.add(new Boolean(true));
		debugTree.add(new Boolean(true));
		debugTree.add(new Boolean(true));
		return debugTree;
	}
	
	/**
	 * All this from the character description panel when the user chooses to learn an AbilitiesNode.
	 * @param abilityIndex
	 */
	public void LearnAbilitiesNode(int abilityIndex) {
		System.out.println("CharacterData.LearnAbilitiesNode() - Character learned AbilityNode at index: " + abilityIndex);
		
		abilitiesNodeLearnedStatusTree.remove(abilityIndex);
		abilitiesNodeLearnedStatusTree.add(abilityIndex, new Boolean(true));
	}
	
	/**
	 * Return the abilities contained in the learned Ability Nodes.
	 * @return
	 */
	public List<Ability> GetLearnedAbilities() {
		List<Ability> learnedAbilities = new ArrayList<Ability>();
		List<Boolean> abilityTree = getAbilitiesNodeLearnedStatusTree();
		for(int i = 0; i < abilityTree.size(); i++) {
			if(!abilityTree.get(i))
				continue;
			for(Ability ability : AbilityManager.GetAbilitiesNodeTree(getType())[i].abilities)
				learnedAbilities.add(ability);
		}
		return learnedAbilities;
	}
	
	//This is neccessary because the user/designer-defined names could conflict with comparisons
	private String id = UUID.randomUUID().toString();
	public String getId() { return id; }
	//The path to the image for a unique portrait
	private String portraitPath;
	public String getPortraitPath() {
		return portraitPath;
	}
	
	//Inventory - Start
	
	private List<ItemData> items = new ArrayList<ItemData>();
	
	public ItemData GetAt(int index) {
		if(items.size() <= index) {
			System.err.println("CharacterData.Inventory.GetAt() - Index out of Bounds!");
			return null;
		} else {
			return items.get(index);
		}
	}
	
	//Check for item before setting new one in a slot
	public boolean IsItemExistingAt(int index) {
		if(index >= items.size())
			return false;
		else
			return items.get(index) != null;
	}
	
	//Return the item to the party storage and clear it from characters inventory
	public ItemData ReturnItemAtIndex(int index) {
		ItemData data = items.get(index);
		items.set(index, null);
		return data;
	}
	
	//Equip a new item
	public void SetItemAtIndex(int index, ItemData newData) {
		if(IsItemExistingAt(index))
			System.err.println("An item already exists in slot:"+index+", ReturnItemAtIndex() before setting a new item or it'll be permanently overwritten!");
			
		items.set(index, newData);
	}
	
	//Get the cumulative buff value granted from all equipment
	public int GetBuff(ItemType itemType) {
		int buffValue = 0;
		for(ItemData item : items) {
			if(item == null)
				continue;
			
			if(item.getType() == itemType) {
				switch(itemType) {
					case Weapon:
						buffValue += item.getStats().getAttack();
						break;
					case Armor:
						buffValue += item.getStats().getArmor();
						break;
					case BattleItem:
						buffValue += item.getStats().getHp();
						break;
					case Misc:
						break;
					default:
						System.err.println("Add support for: " + itemType.toString());
						break;
				}
			}
		}
		return buffValue;
	}
	
	//For saving
	public ItemData[] GetData() {
		return items.stream().toArray(ItemData[]::new);
	}
	
	//Inventory - End
	
	public int GetExp() { return Math.round(CharacterBase.ExpPointsPerLevel * expNorm); }
	public int Level() { return 1 + Math.round( (float)Math.floor(expNorm) ); }
	
	public void AddExp(float expNormGained) {
		expNorm += expNormGained;
	}
	
	//Base Damage Calc Scheme
	/*
		Strength 1 = 100% Base Weapon Damage
		Strength 2 = 113% Base Weapon Damage
		Strength 3 = 125% Base Weapon Damage
		Strength 4 = 138% Base Weapon Damage
		Strength 5 = 150% Base Weapon Damage
	*/
	private int ApplyStrengthToBaseDamage(int baseDamage) {
		return baseDamage + Math.round( baseDamage * ((strength - 1) / 8f) );
	}
	public int GetAttack() {
		int baseWeaponDamage = GetBuff(ItemType.Weapon);
		return ApplyStrengthToBaseDamage(baseWeaponDamage);
	}
	public int GetTalliedAttack(int talliedBaseDamage) {
		return ApplyStrengthToBaseDamage(talliedBaseDamage);
	}
	
	
	public int GetAp() {
		return intellect <= 3 ? 2 : 3;
	}
	
	public int GetHp() {
		return endurance * 2 + 2;
	}
	
	public int GetArmor() {
		return GetBuff(ItemType.Armor);
	}
	
	private final String armorClassLight = "Light";
	private final String armorClassMedium = "Medium";
	private final String armorClassHeavy = "Heavy";
	
	public String GetArmorClassification() {
		int armorValue = GetBuff(ItemType.Armor);
		//Assume all equipment slots but mainhand can contain equipment with armor properties and then multiply that number by the typical max armor value
		int armorUpperLimit = (EquipmentType.values().length - 1) * 100;
		String classification = "[UNKNOWN]";
		if(armorValue <= armorUpperLimit / 3)
			classification = armorClassLight;
		else if(armorValue <= armorUpperLimit * (2/3))
			classification = armorClassMedium;
		else
			classification = armorClassHeavy;
		return classification + " Armor";
	}
	
	public int GetMoveRange() {
		if(endurance <= 2)
			return 2;
		else if(endurance < 5)
			return 3;
		else
			return 4;
	}
	//DEBUG
	/*public int GetMoveRange() {
		System.err.println("DEBUGGING @ CharacterData.GetMoveRange() - Spoofing move range to be a constant large value for easy scene traversal for testing.");
		return 5;
	}*/
	
	CharacterData() {
	};
	
	public CharacterData(String name, ClassType type, int strength, int intellect, int endurance, int speed, float expNorm, String portraitPath) {
		this.name = name;
		this.type = type;
		this.strength = strength;
		this.intellect = intellect;
		this.endurance = endurance;
		this.speed = speed;
		this.expNorm = expNorm;
		this.portraitPath = portraitPath;

		for(int i = 0; i < 6; i++)
			items.add(null);
	}
	
	public CharacterData(CharacterData cloneSource) {
		this.name = cloneSource.name;
		this.type = cloneSource.type;
		this.strength = cloneSource.strength;
		this.intellect = cloneSource.intellect;
		this.endurance = cloneSource.endurance;
		this.speed = cloneSource.speed;
		this.expNorm = cloneSource.expNorm;
		this.portraitPath = cloneSource.portraitPath;
		this.items = new ArrayList<ItemData>( cloneSource.items );
	}
	
	public static CharacterData CreateRandom() {
		Random r = new Random();
		return new CharacterData(
			"Random " + (int)Math.floor(Math.abs(r.nextDouble() * 10000)),
			ClassType.values()[r.nextInt(4)],
			1 + r.nextInt(4),
			1 + r.nextInt(4),
			1 + r.nextInt(4),
			1 + r.nextInt(4),
			50 * (1 + r.nextInt(7)),
			""
		);
	}
	
	public int GetStatValue(StatType statType) {
		int value = 1;
		switch(statType) {
			case STRENGTH:
				value = strength;
				break;
			case INTELLECT:
				value = intellect;
				break;
			case ENDURANCE:
				value = endurance;
				break;
			case SPEED:
				value = speed;
				break;
			default:
				System.err.println("Add support for: " + statType);
				break;
		}
		return value;
	}
	
	public StatType GetGoverningStat() {
		StatType statType = StatType.values()[0];
		switch(type) {
			case RONIN:
				statType = StatType.STRENGTH;
				break;
			case NINJA:
				statType = StatType.INTELLECT;
				break;
			case MONK:
				statType = StatType.ENDURANCE;
				break;
			case BANDIT:
				statType = StatType.SPEED;
				break;
			case KAMI_AR: case KAMI_ER: case KAMI_EY: case KAMI_IN: case KAMI_KA: case KAMI_KO: case KAMI_KY: case KAMI_OI: case KAMI_OK: case KAMI_WA:
				statType = StatType.STRENGTH;
				break;
			case SURF:
				statType = StatType.ENDURANCE;
				break;
			case DIAMYO:
				statType = StatType.INTELLECT;
				break;
			case NEKOMATA: case ONI:
				statType = StatType.SPEED;
				break;
			default:
				System.err.println("Add support for: " + type.toString());
				break;
		}
		return statType;
	}

	@Override
	public String toString() {
		//return "Name: " + name + ", Type: " + type + ", Strength: " + strength + ", Intellect: " + intellect + ", Endurance: " + endurance + ", Speed: " + speed + ", ExpNorm: " + expNorm;
		return name + " - " + (type != null ? type.toString() : "null") + ", ID: " + id + ", portraitPath: " + portraitPath;
	}
	
	public String toString(boolean extra) {
		String items = "";
		for(ItemData itemData : GetData()) {
			if(itemData == null)
				items += "null, ";
			else
				items += itemData.getName() + ", ";
		}
		return toString() + ", InventoryItems: " + items;
	}
}
