package data;

import java.awt.Point;
import java.io.Serializable;
import java.util.List;

import enums.StatusType;
import gameLogic.CharacterBase;

public class CharacterBaseData implements Serializable {
	private static final long serialVersionUID = -3915205127709234049L;

	public CharacterBaseData(CharacterBase charBase) {
		this.data = charBase.GetData();
		
		this.maxHp = charBase.getMaxHp();
		this.hp = charBase.GetHp();
		this.maxArmor = charBase.getMaxArmor();
		this.armor = charBase.getArmor();
		this.maxAp = charBase.getMaxAp();
		this.ap = charBase.GetAp();
		
		this.experienceFactor = charBase.GetExperienceFactor();
		this.exp = charBase.GetExp();
		
		this.hasMoved = charBase.HasMoved();
		this.hasUsedAction = charBase.HasUsedAction();
		this.location = charBase.getLocation();
		this.direction = charBase.getDirection();
		
		this.activeStatuses = charBase.GetActiveStatuses();
		this.lingeringEffects = charBase.lingeringEffects;
	}
	
	public CharacterData data;
	
	public int maxHp, hp;//same as hit points, when value = 0 character is dead
	public int maxArmor, armor;//absorbs damage
	public int maxAp, ap;//points for turn
	
	//such as attack and base chance,this factors in how much experience you give
	public double experienceFactor;//chance modifier based on progress to max level
	public int exp;//this attribute affects various abilities
	
	//Battle info
	public boolean hasMoved;
	public boolean hasUsedAction;
	public Point location;
	public Point direction;
	
	//Status Ailment
	public List<StatusType> activeStatuses;
	
	//Lingering Effects
	public List<LingeringEffect> lingeringEffects;
}
