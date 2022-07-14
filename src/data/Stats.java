package data;

import java.io.Serializable;

import enums.ClassType;
import enums.ElementType;
import enums.EquipmentType;
import enums.KarmaType;
import enums.WeightType;


public class Stats implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2959311220974373489L;
	public Stats() {};
	public Stats(EquipmentType equipmentType, WeightType weight, KarmaType karma, ClassType[] usableClasses,
				 int attack, int spirit, int armor, int vitality, ElementType[] elements, BattleToolTraits battleToolTraits)
	{
		this.equipmentType = equipmentType;
		this.weight = weight;
		this.karma = karma;
		this.usableClasses = usableClasses;
		
		this.attack = attack;
		this.spirit = spirit;
		this.armor = armor;
		this.vitality = vitality;
		
		this.battleToolTraits = battleToolTraits;
	}
	
	
	private EquipmentType equipmentType;
	public EquipmentType getEquipmentType() { return equipmentType; }
	
	private WeightType weight;
	public WeightType getWeight() { return weight; }
	
	//Column N as the first letter of each word
	private KarmaType karma;
	public KarmaType getKarma() { return karma; }
	
	//Direct from Sheet - Column E (Represented by the first letter of the class name, in a comma separated list like "N,P")
	private ClassType[] usableClasses;
	public ClassType[] GetUsableClasses() { return usableClasses; }
	
	
	//Column J
	private int attack;
	public int getAttack() { return attack; }
	//Column K
	private int spirit;
	public int getSpirit() { return spirit; }
	//Column L
	private int armor;
	public int getArmor() { return armor; }
	//Column M
	private int vitality;
	public int getHp() { return vitality; }
	
	
	private ElementType[] elements;
	public ElementType[] getElements() {return elements;}
	
	
	private BattleToolTraits battleToolTraits;
	public BattleToolTraits GetBattleToolTraits() { return battleToolTraits; }
}
