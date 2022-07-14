package gameLogic;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import data.AttributeMod;
import data.BattleItemTraits;
import data.CharacterBaseData;
import data.CharacterData;
import data.CombatEffect;
import data.CombatEffect_AutoDodgeMelee;
import data.CombatEffect_AutoDodgeProj;
import data.CombatEffect_Buff;
import data.CombatEffect_ChanceToSurvive;
import data.CombatEffect_Cure;
import data.CombatEffect_Damage;
import data.CombatEffect_Debuff;
import data.CombatEffect_MeleeCounter;
import data.CombatEffect_Potion;
import data.CombatEffect_RangedCounter;
import data.CombatEffect_Revive;
import data.CombatEffect_Status;
import data.HealthModInfo;
import data.ItemData;
import data.LingeringEffect;
import enums.AttributeModType;
import enums.BattleItemType;
import enums.ClassType;
import enums.ElementType;
import enums.EquipmentType;
import enums.ItemType;
import enums.StatusType;
import gameLogic.AbilityManager.Ability;
import gameLogic.Board.Tile;
import gameLogic.Game.InteractiveActionType;
import gameLogic.Game.ObjectiveType;

public class CharacterBase {
	public static final int ExpPointsPerLevel = 100; 
	public static final int LevelCap = 100;
	
	private CharacterData data;
	public CharacterData GetData() { return data; }
	public ClassType GetCharacterType() { return data.getType(); }
	
	private int maxHp, hp;//same as hit points, when value = 0 character is dead
	public int getMaxHp() { return maxHp; }
	public int GetHp() { return hp; }
	private static final float CriticalHealthPercent = 0.2f;
	public boolean IsHpCritical() {
		return ((float)hp / maxHp) <= CriticalHealthPercent;
	}
	private int maxArmor, armor;//absorbs damage
	public int getMaxArmor() { return maxArmor; }
	public int getArmor() { return armor; }
	private int maxAp, ap;//points for turn
	public int getMaxAp() { return maxAp; }
	public int GetAp() { return ap; }
	private int exp;//this attribute affects various abilities
	public int GetExp() { return exp; }
	
	private double experienceFactor;
	public double GetExperienceFactor() { return experienceFactor; }
	
	private final double baseChance_hit = 0.5;
	private double CalcHitChance(double baseChance) {
		double chanceRemainder = 1.0 - baseChance;
		double chance = baseChance + (((data.getIntellect() - 1) / 4.0) * chanceRemainder);
		return chance;
	}
	/**
	 * Provides a range from baseChance_hit - 1.0 based on this character's Intellect.
	 * @return
	 */
	public double GetChance_Hit() {
		return CalcHitChance(baseChance_hit);
	}
	public double GetTalliedChance_Hit(float talliedChanceToHit) {
		return CalcHitChance((double)talliedChanceToHit);
	}
	
	private final double baseChance_dodge = 0.3;
	private double CalcDodgeChance(double baseChance) {
		double chanceRemainder = 1.0 - baseChance;
		double chance = baseChance + (((data.getSpeed() - 1) / 4.0) * chanceRemainder);
		return chance;
	}
	/**
	 * Provides a range from baseChance_dodge - 1.0 based on this character's Speed.
	 * @return
	 */
	public double GetChance_Dodge() {
		return CalcDodgeChance(baseChance_dodge);
	}
	/**
	 * Provides a range from baseChance_dodge - 1.0 based on this character's Speed.
	 * @return
	 */
	public double GetTalliedChance_Dodge(float talliedBaseChance_dodge) {
		return CalcDodgeChance((double)talliedBaseChance_dodge);
	}
	
	
	//Battle info
	private boolean hasMoved;
	public boolean HasMoved() { return hasMoved; }
	private boolean hasUsedAction;
	public boolean HasUsedAction() { return hasUsedAction; }
	private Point location;
	public Point getLocation() { return location; }
	private Point direction;
	public void SetDirection(Point newDirection) {
		direction = newDirection;
	}
	public Point getDirection() { return direction; }
	private Map<Tile, List<Tile>> paths;
	public void SetPaths(Map<Tile, List<Tile>> newPaths) {
		paths = new HashMap<Tile, List<Tile>>(newPaths);
	}
	public Map<Tile, List<Tile>> GetPaths() { return paths; }

	
	
	public CharacterBase(CharacterData data, Point initialLocation) {
		this.data = data;
		
		this.maxHp = data.GetHp();
		//TODO DEBUGGING - Comment and restore above
		//System.err.println("DEBUGGING @ CharacterBase Constructor - Setting maxHp of every character to 1000.");
		//this.maxHp = 1000;
		
		this.hp = this.maxHp;
		
		this.maxArmor = data.GetBuff(ItemType.Armor);
		this.armor = this.maxArmor;
		
		this.maxAp = data.GetAp();
		this.ap = this.maxAp;
		
		this.exp = data.GetExp();
		this.CalcExperienceFactor();
		
		GetPassiveCombatEffects();
		
		location = initialLocation;
	}
	
	/**
	 * An overload constructor for restoring a CharacterBase to its previous state in battle, called when restoring a battleState from load game.
	 * @param characterBaseData
	 */
	public CharacterBase(CharacterBaseData characterBaseData) {
		this.data = characterBaseData.data;
		
		this.maxHp = characterBaseData.maxHp;
		this.hp = characterBaseData.hp;
		
		this.maxArmor = characterBaseData.maxArmor;
		this.armor = characterBaseData.armor;
		
		this.maxAp = characterBaseData.maxAp;
		this.ap = characterBaseData.ap;
		
		this.exp = characterBaseData.exp;
		this.experienceFactor = characterBaseData.experienceFactor;
		
		this.location = characterBaseData.location;
		this.direction = characterBaseData.direction;
		
		this.hasMoved = characterBaseData.hasMoved;
		this.hasUsedAction = characterBaseData.hasUsedAction;
		
		this.activeStatuses = characterBaseData.activeStatuses;
		this.lingeringEffects = characterBaseData.lingeringEffects;
		
		GetPassiveCombatEffects();
	}
	
	/**
	 * This gets called at the creation of this CharacterBase since learned ability are a snapshot taken at the beginning of combat
	 */
	private void GetPassiveCombatEffects() {
		for(Ability learnedAbility : data.GetLearnedAbilities()) {
			if(!learnedAbility.isActiveAbility) {
				passiveCombatEffects.addAll(learnedAbility.combatEffects);
			}
		}
	}
	
	private void CalcExperienceFactor() {
		//Scale by a factor from 1.0-2.0 over the range of level 1-max
		//The intended use of this 1.0-2.0 range is to improve chance values from 100% at Level 1 to a max potential of 200% at max level
		double currentLevel = Math.floor((double)exp / ExpPointsPerLevel); //Let this be truncated
		experienceFactor = 1.0 + (currentLevel / LevelCap);
	}
	
	public void AddExp(int amount) {
		exp += amount;
		CalcExperienceFactor();
	}
	
	public void Move(Point newLoc) {
		hasMoved = true;
		ap -= 1;
		location = newLoc;
	}
	
	
	Random weaponElementRandom = new Random();
	public ElementType getRandomWeaponElement() {
		List<ElementType> elementTypes = new ArrayList<ElementType>();
		for(ItemData item : data.GetData()) {
			if(item != null && item.getStats().getEquipmentType() == EquipmentType.RightHand && item.getStats().getElements() != null && item.getStats().getElements().length > 0) {
				elementTypes.addAll( (List<ElementType>)Arrays.asList(item.getStats().getElements()) );
				break;
			}
		}
		if(elementTypes.size() == 0)
			return null;
		else
			return elementTypes.get( weaponElementRandom.nextInt(elementTypes.size()) );
	}
	
	
	public void DoAttack() {
		hasUsedAction = true;
		ap -= 1;
	}
	
	//The damage calc logic was moved to ApplyCombatEffects()
	public void DoAbility() {
		hasUsedAction = true;
		ap -= 1;
	}
	
	public void DoItemAction() {
		hasUsedAction = true;
		ap -= 1;
	}
	
	/**
	 * This alternate version is used by ApplyCombatEffects and takes our passives and lingerers into consideration
	 * @param healthModInfo
	 * @param defenders_TalliedArmor
	 */
	public void TalliedTakeDamage(HealthModInfo healthModInfo, int defenders_TalliedArmor) {
		//The reduced Damage needs to be built into HealthModInfo.amount so that the final outcome will be represented properly to the user
		//int reducedDamage = Math.max(0, healthModInfo.amount - defenders_TalliedArmor);
		//hp = Math.max(0, hp - reducedDamage);
		hp = Math.max(0, hp - healthModInfo.amount);
		
		//TODO Factor in elemental damage types from damageInfo.elementTypes
		System.err.println("CharacterBase.TalliedTakeDamage() - Element Damage Stub");
		
		
		//TODO Comment after DEBUGGING
		//System.err.println("DEBUGGING @ CharacterBase.TalliedTakeDamage() - Auto reviving character.");
		//if(hp == 0)
		//	hp = this.maxHp;
		
		if(hp == 0)
			WipeCharacterStateOnDeath();
	}
	
	private void WipeCharacterStateOnDeath() {
		lingeringEffects.clear();
		activeStatuses.clear();
		
		Game.Instance().GetBattlePanel().OnCharacterDeath(this);
	}
	
	public void TakeHeal(HealthModInfo healthModInfo) {
		hp = Math.min(hp + healthModInfo.amount, this.maxHp);
	}
	
	//This logic could be absorbed into TakeHeal() if, in the future, there still isn't anything differenciating this and it
	public void Revive(float reviveHpPercentage) {
		hp = Math.min(Math.round(maxHp * reviveHpPercentage), this.maxHp);
	}
	
	public List<CombatEffect> passiveCombatEffects = new ArrayList<CombatEffect>();
	
	public List<LingeringEffect> lingeringEffects = new ArrayList<LingeringEffect>();
	
	public enum SpecificActionType { BasicAttack, Ability, Item }
	
	//This gets set by TallyTilePenaltyMod and eventually by ApplyCombatEffects once we add support for targeting ourselves
	private int tilePenaltyMod;
	public int getTilePenaltyMod() { return tilePenaltyMod; }
	
	/**
	 * This gets called at the start of the turn.
	 * @return
	 */
	private int TallyTilePenaltyMod() {
		int talliedTilePenaltyMod = 0;
		
		for(CombatEffect combatEffect : passiveCombatEffects) {
			if(combatEffect instanceof CombatEffect_Buff) {
				for(AttributeMod attributeMod : combatEffect.attributeMods_buffs) {
					switch(attributeMod.attributeModType) {
						case ChanceToHit: break; //attacker hit calc
						case ChanceToDodge: break; //defender dodge chance
						case BaseDamage: break; //attacker
						case BaseArmor: break; //defender
						case AbilityPotency: break; //attacker
						case ItemPotency: break; //attacker
						case StatusResistance: break; //defender status chance
						case TilePenalty: 
							talliedTilePenaltyMod += attributeMod.pointOffset;
							break;
						case SequenceShift: break; //for editing turn order
						default: System.err.println("CharacterBase.GetTalliedTilePenaltyMod() - Add support for AttributeMod: " + attributeMod.attributeModType); break;
					}
				}
			}
			
			else if(combatEffect instanceof CombatEffect_MeleeCounter || combatEffect instanceof CombatEffect_RangedCounter || combatEffect instanceof CombatEffect_AutoDodgeProj ||
					combatEffect instanceof CombatEffect_AutoDodgeMelee || combatEffect instanceof CombatEffect_ChanceToSurvive
			) {
				//These dont affect the defender's ability to avoid the attack
			}
			else if(combatEffect instanceof CombatEffect_Damage || combatEffect instanceof CombatEffect_Potion || combatEffect instanceof CombatEffect_Cure || combatEffect instanceof CombatEffect_Revive ||
					combatEffect instanceof CombatEffect_Status || combatEffect instanceof CombatEffect_Debuff
			) {
				//Nothing needed here, these aren't applicable to passive effects
				System.err.println("CharacterBase.GetModifiedChanceToHit() - Non-Passive type effects should never end up as something you'd have stored in passiveCombatEffects member list?!?"
						+ " Those sorts of things should be stored in the lingeringEffects member list.");
			}
			else {
				System.err.println("CharacterBase.GetModifiedChanceToHit() - Add support for CombatEffect child type: " + combatEffect.getClass().getName());
			}
		}
		
		for(LingeringEffect lingeringEffect : lingeringEffects) {
			CombatEffect combatEffect = lingeringEffect.combatEffect;
			
			if(combatEffect instanceof CombatEffect_Buff) {
				for(AttributeMod attributeMod : combatEffect.attributeMods_buffs) {
					switch(attributeMod.attributeModType) {
						case ChanceToHit: break; //hit calc
						case ChanceToDodge: break;
						case BaseDamage: break; //attacker
						case BaseArmor: break;
						case AbilityPotency: break; //attacker
						case ItemPotency: break; //attacker
						case StatusResistance: break;
						case TilePenalty:
							talliedTilePenaltyMod += attributeMod.pointOffset;
							break;
						case SequenceShift: break; //for editing turn order
						default: System.err.println("CharacterBase.GetTalliedTilePenaltyMod() - Add support for AttributeMod: " + attributeMod.attributeModType); break;
					}
				}
			}
			else if(combatEffect instanceof CombatEffect_Debuff) {
				//These could make vulnerabilities
				for(AttributeMod attributeMod : combatEffect.attributeMods_debuffs) {
					switch(attributeMod.attributeModType) {
						case ChanceToHit: break;
						case ChanceToDodge: break;
						case BaseDamage: break;
						case BaseArmor: break;
						case AbilityPotency: break;
						case ItemPotency: break;
						case StatusResistance: break;
						case TilePenalty:
							talliedTilePenaltyMod += attributeMod.pointOffset;
							break;
						case SequenceShift: break;
						default: System.err.println("CharacterBase.GetTalliedTilePenaltyMod() - Add support for AttributeMod: " + attributeMod.attributeModType); break;
					}
				}
			}
			
			else if(combatEffect instanceof CombatEffect_Status || combatEffect instanceof CombatEffect_Cure ||
					combatEffect instanceof CombatEffect_Damage || combatEffect instanceof CombatEffect_Potion  || combatEffect instanceof CombatEffect_Revive
			) {
				//These dont affect the defender's ability to avoid the attack
			}
			else if(combatEffect instanceof CombatEffect_MeleeCounter || combatEffect instanceof CombatEffect_RangedCounter || combatEffect instanceof CombatEffect_AutoDodgeProj ||
					combatEffect instanceof CombatEffect_AutoDodgeMelee || combatEffect instanceof CombatEffect_ChanceToSurvive
			) {
				System.err.println("CharacterBase.ApplyCombatEffects() - Passive type effects should never end up as something you'd have stored in lingeringEffects member list?!?"
						+ " Those sorts of things should be stored in the passiveCombatEffects member list.");
			}
			else {
				System.err.println("CharacterBase.GetModifiedChanceToHit() - Add support for CombatEffect child type: " + combatEffect.getClass().getName());
			}
		}
		
		return talliedTilePenaltyMod;
	}
	
	/**
	 * This is disected from the GetModifiedHitOutcome method below. It will calc the hit chance for a target, when the user's hovered tile is in range of valid targets, and this info will be stored and feed
	 * to the other remaining half of the GetModifiedHitOutcome disection to actually see if the action hits.
	 * @param specificActionType
	 * @param abilityChanceToHitFactor
	 * @param isRangedAttack
	 * @param defenderBase
	 * @return
	 */
	public double GetModifiedHitChance(SpecificActionType specificActionType, float abilityChanceToHitFactor, boolean isRangedAttack, CharacterBase defenderBase) {
		float attacks_talliedChanceToHit = (float)baseChance_hit;
		
		float defenders_talliedChanceToDodge = (float)baseChance_dodge;
		
		
		//Early out for defenders who will auto-dodge this attack
		for(CombatEffect combatEffect : defenderBase.passiveCombatEffects) {
			if(combatEffect instanceof CombatEffect_AutoDodgeProj) {
				if(specificActionType == SpecificActionType.BasicAttack && isRangedAttack)
					return 0f;
			}
			else if(combatEffect instanceof CombatEffect_AutoDodgeMelee) {
				if(specificActionType == SpecificActionType.BasicAttack && !isRangedAttack)
					return 0f;
			}
			else if(combatEffect instanceof CombatEffect_Buff) {
				for(AttributeMod attributeMod : combatEffect.attributeMods_buffs) {
					switch(attributeMod.attributeModType) {
						case ChanceToHit: break; //attacker hit calc
						case ChanceToDodge:
							defenders_talliedChanceToDodge += attributeMod.chanceOffset;
							break;
						case BaseDamage: break; //attacker
						case BaseArmor: break; //defender
						case AbilityPotency: break; //attacker
						case ItemPotency: break; //attacker
						case StatusResistance: break; //defender status chance
						case TilePenalty: break; //for movement
						case SequenceShift: break; //for editing turn order
						default: System.err.println("CharacterBase.GetModifiedHitOutcome() - Add support for AttributeMod: " + attributeMod.attributeModType); break;
					}
				}
			}
			
			else if(combatEffect instanceof CombatEffect_MeleeCounter || combatEffect instanceof CombatEffect_RangedCounter || combatEffect instanceof CombatEffect_ChanceToSurvive
			) {
				//These dont affect the defender's ability to avoid the attack
			}
			else if(combatEffect instanceof CombatEffect_Damage || combatEffect instanceof CombatEffect_Potion || combatEffect instanceof CombatEffect_Cure || combatEffect instanceof CombatEffect_Revive ||
					combatEffect instanceof CombatEffect_Status || combatEffect instanceof CombatEffect_Debuff
			) {
				//Nothing needed here, these aren't applicable to passive effects
				System.err.println("CharacterBase.GetModifiedChanceToHit() - Non-Passive type effects should never end up as something you'd have stored in passiveCombatEffects member list?!?"
						+ " Those sorts of things should be stored in the lingeringEffects member list.");
			}
			else {
				System.err.println("CharacterBase.GetModifiedChanceToHit() - Add support for CombatEffect child type: " + combatEffect.getClass().getName());
			}
		}
		
		for(LingeringEffect lingeringEffect : defenderBase.lingeringEffects) {
			CombatEffect combatEffect = lingeringEffect.combatEffect;
			
			if(combatEffect instanceof CombatEffect_Buff) {
				for(AttributeMod attributeMod : combatEffect.attributeMods_buffs) {
					switch(attributeMod.attributeModType) {
						case ChanceToHit: break; //hit calc
						case ChanceToDodge:
							defenders_talliedChanceToDodge += attributeMod.chanceOffset;
							break;
						case BaseDamage: break; //attacker
						case BaseArmor: break;
						case AbilityPotency: break; //attacker
						case ItemPotency: break; //attacker
						case StatusResistance: break;
						case TilePenalty: break; //for movement
						case SequenceShift: break; //for editing turn order
						default: System.err.println("CharacterBase.GetModifiedHitOutcome() - Add support for AttributeMod: " + attributeMod.attributeModType); break;
					}
				}
			}
			else if(combatEffect instanceof CombatEffect_Debuff) {
				//These could make vulnerabilities
				for(AttributeMod attributeMod : combatEffect.attributeMods_debuffs) {
					switch(attributeMod.attributeModType) {
						case ChanceToHit: break;
						case ChanceToDodge:
							defenders_talliedChanceToDodge = Math.max(0f, defenders_talliedChanceToDodge - attributeMod.chanceOffset);
							break;
						case BaseDamage: break;
						case BaseArmor: break;
						case AbilityPotency: break;
						case ItemPotency: break;
						case StatusResistance: break;
						case TilePenalty: break;
						case SequenceShift: break;
						default: System.err.println("CharacterBase.GetModifiedHitOutcome() - Add support for AttributeMod: " + attributeMod.attributeModType); break;
					}
				}
			}
			
			else if(combatEffect instanceof CombatEffect_Status || combatEffect instanceof CombatEffect_Cure ||
					combatEffect instanceof CombatEffect_Damage || combatEffect instanceof CombatEffect_Potion  || combatEffect instanceof CombatEffect_Revive
			) {
				//These dont affect the defender's ability to avoid the attack
			}
			else if(combatEffect instanceof CombatEffect_MeleeCounter || combatEffect instanceof CombatEffect_RangedCounter || combatEffect instanceof CombatEffect_AutoDodgeProj ||
					combatEffect instanceof CombatEffect_AutoDodgeMelee || combatEffect instanceof CombatEffect_ChanceToSurvive
			) {
				System.err.println("CharacterBase.ApplyCombatEffects() - Passive type effects should never end up as something you'd have stored in lingeringEffects member list?!?"
						+ " Those sorts of things should be stored in the passiveCombatEffects member list.");
			}
			else {
				System.err.println("CharacterBase.GetModifiedChanceToHit() - Add support for CombatEffect child type: " + combatEffect.getClass().getName());
			}
		}
		
		
		for(CombatEffect combatEffect : passiveCombatEffects) {
			if(combatEffect instanceof CombatEffect_Buff) {
				for(AttributeMod attributeMod : combatEffect.attributeMods_buffs) {
					switch(attributeMod.attributeModType) {
						case ChanceToHit:
							attacks_talliedChanceToHit += attributeMod.chanceOffset;
							break;
						case ChanceToDodge: break; //hit calc
						case BaseDamage: break; //attacker
						case BaseArmor: break;
						case AbilityPotency: break; //attacker
						case ItemPotency: break; //attacker
						case StatusResistance: break;
						case TilePenalty: break; //for movement
						case SequenceShift: break; //for editing turn order
						default: System.err.println("CharacterBase.GetModifiedHitOutcome() - Add support for AttributeMod: " + attributeMod.attributeModType); break;
					}
				}
			}
			
			else if(combatEffect instanceof CombatEffect_MeleeCounter || combatEffect instanceof CombatEffect_RangedCounter || combatEffect instanceof CombatEffect_AutoDodgeProj ||
					combatEffect instanceof CombatEffect_AutoDodgeMelee || combatEffect instanceof CombatEffect_ChanceToSurvive
			) {
				//These passives dont affect our chanceToHit
			}
			else if(combatEffect instanceof CombatEffect_Damage || combatEffect instanceof CombatEffect_Potion || combatEffect instanceof CombatEffect_Cure || combatEffect instanceof CombatEffect_Revive ||
					combatEffect instanceof CombatEffect_Status || combatEffect instanceof CombatEffect_Debuff
			) {
				//Nothing needed here, these aren't applicable to passive effects
				System.err.println("CharacterBase.GetModifiedChanceToHit() - Non-Passive type effects should never end up as something you'd have stored in passiveCombatEffects member list?!?"
						+ " Those sorts of things should be stored in the lingeringEffects member list.");
			}
			else {
				System.err.println("CharacterBase.GetModifiedChanceToHit() - Add support for CombatEffect child type: " + combatEffect.getClass().getName());
			}
		}
		
		//Check current lingering effects, skip this if we've already automissed
		boolean isBlind = false;
		for(LingeringEffect lingeringEffect : lingeringEffects) {
			CombatEffect combatEffect = lingeringEffect.combatEffect;
			
			if(combatEffect instanceof CombatEffect_Status) {
				//Could decrease chanceToHit
				if(combatEffect.statusEffect == StatusType.Blind)
					isBlind = true;
			}
			else if(combatEffect instanceof CombatEffect_Buff) {
				//Could improve chanceToHit
				for(AttributeMod attributeMod : combatEffect.attributeMods_buffs) {
					switch(attributeMod.attributeModType) {
						case ChanceToHit:
							attacks_talliedChanceToHit += attributeMod.chanceOffset;
							break;
						case ChanceToDodge: break; //hit calc
						case BaseDamage: break; //attacker
						case BaseArmor: break;
						case AbilityPotency: break; //attacker
						case ItemPotency: break; //attacker
						case StatusResistance: break;
						case TilePenalty: break; //for movement
						case SequenceShift: break; //for editing turn order
						default: System.err.println("CharacterBase.GetModifiedHitOutcome() - Add support for AttributeMod: " + attributeMod.attributeModType); break;
					}
				}
			}
			else if(combatEffect instanceof CombatEffect_Debuff) {
				//Could decrease chanceToHit
				for(AttributeMod attributeMod : combatEffect.attributeMods_debuffs) {
					switch(attributeMod.attributeModType) {
						case ChanceToHit:
							attacks_talliedChanceToHit = Math.max(0f, attacks_talliedChanceToHit - attributeMod.chanceOffset);
							break;
						case ChanceToDodge: break;
						case BaseDamage: break;
						case BaseArmor: break;
						case AbilityPotency: break;
						case ItemPotency: break;
						case StatusResistance: break;
						case TilePenalty: break;
						case SequenceShift: break;
						default: System.err.println("CharacterBase.GetModifiedHitOutcome() - Add support for AttributeMod: " + attributeMod.attributeModType); break;
					}
				}
			}
			
			else if(combatEffect instanceof CombatEffect_Damage || combatEffect instanceof CombatEffect_Potion || combatEffect instanceof CombatEffect_Cure || combatEffect instanceof CombatEffect_Revive) {
				//These are ineffectual. Some are so in the sense that they're separate mechanisms that are only accounted for during the start of a character's turn. Others dont apply to chanceToHit
			}
			
			else if(combatEffect instanceof CombatEffect_MeleeCounter || combatEffect instanceof CombatEffect_RangedCounter || combatEffect instanceof CombatEffect_AutoDodgeProj ||
					combatEffect instanceof CombatEffect_AutoDodgeMelee || combatEffect instanceof CombatEffect_ChanceToSurvive
			) {
				System.err.println("CharacterBase.GetModifiedChanceToHit() - Passive type effects should never end up as something you'd have stored in lingeringEffects member list?!?"
						+ " Those sorts of things should be stored in the passiveCombatEffects member list.");
			}
			
			else {
				System.err.println("CharacterBase.GetModifiedChanceToHit() - Add support for CombatEffect child type: " + combatEffect.getClass().getName());
			}
		}
		
		float clampedTalliedChanceToHit = Math.max(0f, Math.min(attacks_talliedChanceToHit, 1f) );
		double talliedHitChance = GetTalliedChance_Hit(clampedTalliedChanceToHit);
		
		double finalChance = talliedHitChance * (double)abilityChanceToHitFactor;
		if(isBlind)
			finalChance = finalChance / 2.0;
		
		//Hit calc adaptation
		double defendersChanceToGetHit = 1.0 - defenderBase.GetTalliedChance_Dodge(defenders_talliedChanceToDodge);
		//Factor in the ability's chanceToHitFactor
		double attackersHitChance = Math.min(finalChance, 1.0);
		double averageChance = (attackersHitChance + defendersChanceToGetHit) / 2.0;

		return averageChance;
	}
	
	public boolean GetModifiedHitOutcome(double chance) {
		double nextRandom = Game.Instance().GetCharacterActionRandom().nextDouble();
		boolean didAttackHit = nextRandom <= chance;
		return didAttackHit;
	}
	
	public enum PostBoutInstructionType { DefenderDoesCounterAttack, DefenderDoesRevive }
	
	public CombatCalcInfo GetCombatCalcInfo(List<CombatEffect> combatEffects, SpecificActionType specificActionType, boolean isRangedAttack, CharacterBase defenderBase) {
		if(defenderBase == this)
			System.err.println("CharacterBase.GetCombatCalcInfo() - This method could have potential problems handling ourselves as the target"
					+ "(both the attackerBase this method is called on and the defenderBase argument).");
		
		//Things to tally for the attacker in this method
		int attackers_talliedBaseDamage = data.GetBuff(ItemType.Weapon);
		float attackers_talliedAbilityPotency = 1f;
		float attackers_talliedItemPotency = 1f;
		
		//Things to tally for the attacker in this method
		int defenders_talliedArmor = defenderBase.data.GetArmor();
		float defenders_talliedStatusResistance = 0f;
		
		//Defenders cumulative state properties
		
		//Check Passives
		boolean hasChanceToSurvive = false;
		float chanceToSurvive = 0f;
		//Determine if there are extra combat steps to perform after the first initial bout
		
		PostBoutInstructionType postBoutInstruction = null;
		//TODO Comment and restore above after DEBUGGING
		//System.err.println("DEBUGGING @ CharacterBase.GetCombatCalcInfo() - Defender always applies PostBoutInstructionType.DefenderDoesCounterAttack.");
		//PostBoutInstructionType postBoutInstruction = PostBoutInstructionType.DefenderDoesRevive;
		
		int distanceToTarget = Game.GetDistance(getLocation(), defenderBase.getLocation());
		boolean areWeWithinDefendersWeaponRange = defenderBase.GetMinWeaponRange() <= distanceToTarget && defenderBase.GetMaxWeaponRange() >= distanceToTarget;
		
		for(CombatEffect combatEffect : defenderBase.passiveCombatEffects) {
			if(combatEffect instanceof CombatEffect_ChanceToSurvive) {
				hasChanceToSurvive = true;
				chanceToSurvive = combatEffect.chanceToSurvive_chance;
			}
			else if(combatEffect instanceof CombatEffect_MeleeCounter) {
				//TODO Uncomment once done DEBUGGING
				if(specificActionType == SpecificActionType.BasicAttack && !isRangedAttack && areWeWithinDefendersWeaponRange)
					postBoutInstruction = PostBoutInstructionType.DefenderDoesCounterAttack;
			}
			else if(combatEffect instanceof CombatEffect_RangedCounter) {
				//TODO Uncomment once done DEBUGGING
				if(specificActionType == SpecificActionType.BasicAttack && isRangedAttack && areWeWithinDefendersWeaponRange)
					postBoutInstruction = PostBoutInstructionType.DefenderDoesCounterAttack;
			}
			else if(combatEffect instanceof CombatEffect_Buff) {
				//This could bolster the defender in multiple ways
				for(AttributeMod attributeMod : combatEffect.attributeMods_buffs) {
					switch(attributeMod.attributeModType) {
						case ChanceToHit: break; //hit calc
						case ChanceToDodge: break; //hit calc
						case BaseDamage: break; //attacker
						case BaseArmor:
							defenders_talliedArmor += attributeMod.pointOffset;
							break;
						case AbilityPotency: break; //attacker
						case ItemPotency: break; //attacker
						case StatusResistance:
							defenders_talliedStatusResistance += attributeMod.chanceOffset;
							break;
						case TilePenalty: break; //for movement
						case SequenceShift: break; //for editing turn order
						default: System.err.println("CharacterBase.GetCombatCalcInfo() - Add support for AttributeMod: " + attributeMod.attributeModType); break;
					}
				}
			}
			
			else if(combatEffect instanceof CombatEffect_AutoDodgeProj || combatEffect instanceof CombatEffect_AutoDodgeMelee ||
					combatEffect instanceof CombatEffect_Status || combatEffect instanceof CombatEffect_Debuff || 
					combatEffect instanceof CombatEffect_Damage || combatEffect instanceof CombatEffect_Potion || combatEffect instanceof CombatEffect_Cure || combatEffect instanceof CombatEffect_Revive
			) {
				//These are either unapplicable passives or non-passive types
			}
			
			else {
				System.err.println("CharacterBase.GetCombatCalcInfo() - Add support for CombatEffect child type: " + combatEffect.getClass().getName());
			}
		}
		
		//Check the defenders lingering effects
		//These active cure effects could cancel out matching Status effects that are being applied later in this method
		List<LingeringEffect> currentLingeringCureEffects = new ArrayList<LingeringEffect>();
		boolean overrideIntructionWithRevive = false;
		for(LingeringEffect lingeringEffect : defenderBase.lingeringEffects) {
			CombatEffect combatEffect = lingeringEffect.combatEffect;
			if(combatEffect instanceof CombatEffect_Cure) {
				currentLingeringCureEffects.add(lingeringEffect);
			}
			else if(combatEffect instanceof CombatEffect_Buff) {
				//These could do all sorts of bolstering
				for(AttributeMod attributeMod : combatEffect.attributeMods_buffs) {
					switch(attributeMod.attributeModType) {
						case ChanceToHit: break;
						case ChanceToDodge: break;
						case BaseDamage: break;
						case BaseArmor:
							defenders_talliedArmor += attributeMod.pointOffset;
							break;
						case AbilityPotency: break;
						case ItemPotency: break;
						case StatusResistance:
							defenders_talliedStatusResistance += attributeMod.chanceOffset;
							break;
						case TilePenalty: break;
						case SequenceShift: break;
						default: System.err.println("CharacterBase.GetCombatCalcInfo() - Add support for AttributeMod: " + attributeMod.attributeModType); break;
					}
				}
			}
			else if(combatEffect instanceof CombatEffect_Debuff) {
				//These could make vulnerabilities
				for(AttributeMod attributeMod : combatEffect.attributeMods_debuffs) {
					switch(attributeMod.attributeModType) {
						case ChanceToHit: break;
						case ChanceToDodge: break;
						case BaseDamage: break;
						case BaseArmor:
							defenders_talliedArmor = Math.max(0, defenders_talliedArmor - attributeMod.pointOffset);
							break;
						case AbilityPotency: break;
						case ItemPotency: break;
						case StatusResistance:
							defenders_talliedStatusResistance = Math.max(0f, defenders_talliedStatusResistance - attributeMod.chanceOffset);
							break;
						case TilePenalty: break;
						case SequenceShift: break;
						default: System.err.println("CharacterBase.GetCombatCalcInfo() - Add support for AttributeMod: " + attributeMod.attributeModType); break;
					}
				}
			}
			else if(combatEffect instanceof CombatEffect_Revive) {
				//If someone has a lingering revive(a pre-vive) then it could subsequently trigger regen
				//This is intended to override the CounterAttack cause how're you gunna counterattack if you're busy dying
				overrideIntructionWithRevive = true;
			}
			
			else if(combatEffect instanceof CombatEffect_Damage || combatEffect instanceof CombatEffect_Potion || combatEffect instanceof CombatEffect_Status) {
				//These are ineffectual in the sense that they're separate mechanisms that are only accounted for during the start of a character's turn
			}
			
			else if(combatEffect instanceof CombatEffect_MeleeCounter || combatEffect instanceof CombatEffect_RangedCounter || combatEffect instanceof CombatEffect_AutoDodgeProj ||
					combatEffect instanceof CombatEffect_AutoDodgeMelee || combatEffect instanceof CombatEffect_ChanceToSurvive
			) {
				System.err.println("CharacterBase.GetCombatCalcInfo() - Passive type effects should never end up as something you'd have stored in lingeringEffects member list?!?"
						+ " Those sorts of things should be stored in the passiveCombatEffects member list.");
			}
			
			else {
				System.err.println("CharacterBase.GetCombatCalcInfo() - Add support for CombatEffect child type: " + combatEffect.getClass().getName());
			}
		}
		
		
		//Attackers cumulative state properties
		
		//Tally Attackers passive effects
		for(CombatEffect combatEffect : passiveCombatEffects) {
			if(combatEffect instanceof CombatEffect_Buff) {
				//These will help attacker
				for(AttributeMod attributeMod : combatEffect.attributeMods_buffs) {
					switch(attributeMod.attributeModType) {
						case ChanceToHit: break;
						case ChanceToDodge: break;
						case BaseDamage:
							attackers_talliedBaseDamage += attributeMod.pointOffset;
							break;
						case BaseArmor: break;
						case AbilityPotency:
							attackers_talliedAbilityPotency += attributeMod.chanceOffset;
							break;
						case ItemPotency:
							attackers_talliedItemPotency += attributeMod.chanceOffset;
							break;
						case StatusResistance: break;
						case TilePenalty: break;
						case SequenceShift: break;
						default: System.err.println("CharacterBase.GetCombatCalcInfo() - Add support for AttributeMod: " + attributeMod.attributeModType); break;
					}
				}
			}
			
			else if(combatEffect instanceof CombatEffect_MeleeCounter || combatEffect instanceof CombatEffect_RangedCounter || combatEffect instanceof CombatEffect_AutoDodgeProj ||
					combatEffect instanceof CombatEffect_AutoDodgeMelee || combatEffect instanceof CombatEffect_ChanceToSurvive) {
				//Ineffectual
			}
			
			else if(combatEffect instanceof CombatEffect_Damage || combatEffect instanceof CombatEffect_Potion || combatEffect instanceof CombatEffect_Cure || combatEffect instanceof CombatEffect_Revive ||
					combatEffect instanceof CombatEffect_Status || combatEffect instanceof CombatEffect_Debuff) {
				//Nothing needed here, these aren't applicable to passive effects
				System.err.println("CharacterBase.GetCombatCalcInfo() - Non-Passive type effects should never end up as something you'd have stored in passiveCombatEffects member list?!?"
						+ " Those sorts of things should be stored in the lingeringEffects member list.");
			}

			else {
				System.err.println("CharacterBase.GetCombatCalcInfo() - Add support for CombatEffect child type: " + combatEffect.getClass().getName());
			}
		}
		
		//Tally Attackers lingering effects
		for(LingeringEffect lingeringEffect : lingeringEffects) {
			CombatEffect combatEffect = lingeringEffect.combatEffect;
			if(combatEffect instanceof CombatEffect_Buff) {
				//Will help tally
				for(AttributeMod attributeMod : combatEffect.attributeMods_buffs) {
					switch(attributeMod.attributeModType) {
						case ChanceToHit: break;
						case ChanceToDodge: break;
						case BaseDamage:
							attackers_talliedBaseDamage += attributeMod.pointOffset;
							break;
						case BaseArmor: break;
						case AbilityPotency:
							attackers_talliedAbilityPotency += attributeMod.chanceOffset;
							break;
						case ItemPotency:
							attackers_talliedItemPotency += attributeMod.chanceOffset;
							break;
						case StatusResistance: break;
						case TilePenalty: break;
						case SequenceShift: break;
						default: System.err.println("CharacterBase.GetCombatCalcInfo() - Add support for AttributeMod: " + attributeMod.attributeModType); break;
					}
				}
			}
			else if(combatEffect instanceof CombatEffect_Debuff) {
				//Will hurt tally
				for(AttributeMod attributeMod : combatEffect.attributeMods_debuffs) {
					switch(attributeMod.attributeModType) {
						case ChanceToHit: break;
						case ChanceToDodge: break;
						case BaseDamage:
							attackers_talliedBaseDamage = Math.max(0, attackers_talliedBaseDamage - attributeMod.pointOffset);
							break;
						case BaseArmor: break;
						case AbilityPotency:
							attackers_talliedAbilityPotency = Math.max(0f, attackers_talliedAbilityPotency - attributeMod.chanceOffset);
							break;
						case ItemPotency:
							attackers_talliedItemPotency = Math.max(0f, attackers_talliedItemPotency - attributeMod.chanceOffset);
							break;
						case StatusResistance: break;
						case TilePenalty: break;
						case SequenceShift: break;
						default: System.err.println("CharacterBase.GetCombatCalcInfo() - Add support for AttributeMod: " + attributeMod.attributeModType); break;
					}
				}
			}
			
			else if(combatEffect instanceof CombatEffect_Damage || combatEffect instanceof CombatEffect_Potion || combatEffect instanceof CombatEffect_Revive || combatEffect instanceof CombatEffect_Cure ||
					combatEffect instanceof CombatEffect_Status) {
				//These are ineffectual in the sense that they're either: separate mechanisms that are only accounted for during the start of a character's turn OR ineffectual
			}
			
			else if(combatEffect instanceof CombatEffect_MeleeCounter || combatEffect instanceof CombatEffect_RangedCounter || combatEffect instanceof CombatEffect_AutoDodgeProj ||
					combatEffect instanceof CombatEffect_AutoDodgeMelee || combatEffect instanceof CombatEffect_ChanceToSurvive
			) {
				System.err.println("CharacterBase.GetCombatCalcInfo() - Passive type effects should never end up as something you'd have stored in lingeringEffects member list?!?"
						+ " Those sorts of things should be stored in the passiveCombatEffects member list.");
			}
			
			else {
				System.err.println("CharacterBase.GetCombatCalcInfo() - Add support for CombatEffect child type: " + combatEffect.getClass().getName());
			}
		}
		
		
		//Tally up or directly apply the incoming effects against/to the defender
		
		CombatEffect_Damage damageEffect = null;
		CombatEffect_Potion potionEffect = null;
		CombatEffect_Revive reviveEffect = null;
		
		//Subtle effects
		List<StatusType> newlyAppliedStatuses = new ArrayList<StatusType>();
		CombatEffect_Cure cureEffect = null;
		CombatEffect_Buff buffEffect = null;
		CombatEffect_Debuff debuffEffect = null;
		
		for(CombatEffect combatEffect : combatEffects) {
			if(combatEffect instanceof CombatEffect_Damage) {
				if(damageEffect != null)
					System.err.println("CharacterBase.GetCombatCalcInfo() - There are more than one CombatEffect_Damage, there should never be more than any one type of BattleItemTypes in an Ability!");
				damageEffect = (CombatEffect_Damage)combatEffect;
			}
			else if(combatEffect instanceof CombatEffect_Potion) {
				if(potionEffect != null)
					System.err.println("CharacterBase.GetCombatCalcInfo() - There are more than one CombatEffect_Potion, there should never be more than any one type of BattleItemTypes in an Ability!");
				potionEffect = (CombatEffect_Potion)combatEffect;
			}
			else if(combatEffect instanceof CombatEffect_Status) {
				//Make Status effects less likely to occur during a hitting attack via StatusResistance
				if(defenders_talliedStatusResistance > 0f && Game.Instance().GetCharacterActionRandom().nextFloat() <= defenders_talliedStatusResistance)
					continue;
				
				//If we've got a pre-cure for it then cancel out this CombatEffect
				LingeringEffect preCureEffect = null;
				for(LingeringEffect lingEffect : currentLingeringCureEffects) {
					for(StatusType cureType : lingEffect.combatEffect.cures) {
						if(cureType == combatEffect.statusEffect) {
							preCureEffect = lingEffect;
							break;
						}
					}
					if(preCureEffect != null)
						break;
				}
				if(preCureEffect != null) {
					currentLingeringCureEffects.remove(preCureEffect);
				} else {
					newlyAppliedStatuses.add(combatEffect.statusEffect);
				}
			}
			else if(combatEffect instanceof CombatEffect_Cure) {
				cureEffect = (CombatEffect_Cure)combatEffect;
			}
			else if(combatEffect instanceof CombatEffect_Buff) {
				buffEffect = (CombatEffect_Buff)combatEffect;
			}
			else if(combatEffect instanceof CombatEffect_Debuff) {
				debuffEffect = (CombatEffect_Debuff)combatEffect;
			}
			else if(combatEffect instanceof CombatEffect_Revive) {
				reviveEffect = (CombatEffect_Revive) combatEffect;
			}
			
			else if(combatEffect instanceof CombatEffect_MeleeCounter || combatEffect instanceof CombatEffect_RangedCounter || combatEffect instanceof CombatEffect_AutoDodgeProj ||
					combatEffect instanceof CombatEffect_AutoDodgeMelee || combatEffect instanceof CombatEffect_ChanceToSurvive
			) {
				System.err.println("CharacterBase.GetCombatCalcInfo() - Passive type effects should never end up as something you'd send to the ApplyCombatEffects?!?");
			}
			else {
				System.err.println("CharacterBase.GetCombatCalcInfo() - Add support for CombatEffect child type: " + combatEffect.getClass().getName());
			}
		}
		
		
		//Do all the number crunching of the defender's and attacker's tallied effects
		
		HealthModInfo healthModInfo = null;
		
		List<StatusType> cures = null;
		if(cureEffect != null)
			cures = (List<StatusType>)Arrays.asList( cureEffect.cures );
		List<AttributeMod> attributeMods_buffs = null;
		if(buffEffect != null)
			attributeMods_buffs = (List<AttributeMod>)Arrays.asList( buffEffect.attributeMods_buffs );
		List<AttributeMod> attributeMods_debuffs = null;
		if(debuffEffect != null)
			attributeMods_debuffs = (List<AttributeMod>)Arrays.asList( debuffEffect.attributeMods_debuffs );
		
		//Reconcile the defender's relavent passive and lingering effects, the attacker's relavent passive and lingering effects AND the applied effects
		if(damageEffect != null) {
			//Damage Calc
			int damage = data.GetTalliedAttack(attackers_talliedBaseDamage);
			if(!damageEffect.useMainAttackAsBase)
				damage = damageEffect.customBaseDamage;
			
			if(specificActionType == SpecificActionType.Ability) {
				if(attackers_talliedAbilityPotency > 0f)
					damage = Math.round(damage * attackers_talliedAbilityPotency);
			} else if(specificActionType == SpecificActionType.Item) {
				if(attackers_talliedItemPotency > 0f)
					damage = Math.round(damage * attackers_talliedItemPotency);
			}
			
			ElementType elementType = getRandomWeaponElement();
			//Elemental override
			if(damageEffect.elementalDamageType != null) 
				elementType = damageEffect.elementalDamageType;
			
			if(damageEffect.effectTurnDuration > 0) {
				//Divide damage across turns
				damage = damage / damageEffect.effectTurnDuration;
			}
			
			/*
			if(damage >= defenderBase.hp) {
				if(hasChanceToSurvive && Game.Instance().GetCharacterActionRandom().nextFloat() <= chanceToSurvive)
					if(defenderBase.hp == 1)
						damage = 0;
					else
						damage = defenderBase.hp - 1;
				else if(overrideIntructionWithRevive) //If they were indeed dealt a lethal blow then do our revive, if its available
					postBoutInstruction = PostBoutInstructionType.DefenderDoesRevive;
			}
			healthModInfo = new HealthModInfo(false, false, damage, elementType, (damage == 0), cures, newlyAppliedStatuses, attributeMods_buffs, attributeMods_debuffs, postBoutInstruction);
			 */
			float damageArmorRatio = (float)damage / Math.max(defenders_talliedArmor, 1);
			int remainingDamage = 0;
			//Do 5%-20% damage
			if(damageArmorRatio < 1f)
				remainingDamage = Math.round(damage * (0.05f + (0.15f * damageArmorRatio)));
			//20%-80%
			else if(damageArmorRatio >= 1f && damageArmorRatio < 2f)
				remainingDamage = Math.round(damage * (0.2f + (0.6f * (damageArmorRatio - 1f))));
			//80%-100%
			else
				remainingDamage = Math.round(damage * (0.8f + Math.min(0.2f, (0.2f * (damageArmorRatio / 10f)))));
			int reducedDamage = 1 + remainingDamage;
			
			if(reducedDamage >= defenderBase.hp) {
				if(hasChanceToSurvive && Game.Instance().GetCharacterActionRandom().nextFloat() <= chanceToSurvive)
					if(defenderBase.hp == 1)
						reducedDamage = 0;
					else
						reducedDamage = defenderBase.hp - 1;
				else if(overrideIntructionWithRevive) //If they were indeed dealt a lethal blow then do our revive, if its available
					postBoutInstruction = PostBoutInstructionType.DefenderDoesRevive;
			}
			healthModInfo = new HealthModInfo(false, false, reducedDamage, elementType, cures, newlyAppliedStatuses,
					attributeMods_buffs, attributeMods_debuffs, postBoutInstruction);
			
		} else if(potionEffect != null) {
			int healingPoints = potionEffect.customHealingPoints;
			
			if(specificActionType == SpecificActionType.Ability) {
				if(attackers_talliedAbilityPotency > 0f)
					healingPoints = Math.round(healingPoints * attackers_talliedAbilityPotency);
			} else if(specificActionType == SpecificActionType.Item) {
				if(attackers_talliedItemPotency > 0f)
					healingPoints = Math.round(healingPoints * attackers_talliedItemPotency);
			}
			
			if(potionEffect.effectTurnDuration > 0) {
				//Divide damage across turns
				healingPoints = healingPoints / potionEffect.effectTurnDuration;
			}
			
			healthModInfo = new HealthModInfo(true, false, healingPoints, null, cures, newlyAppliedStatuses, attributeMods_buffs, attributeMods_debuffs, postBoutInstruction);
		} else if(reviveEffect != null) {
			float revivePercentage = reviveEffect.reviveHealthPercentage;
			
			if(specificActionType == SpecificActionType.Ability) {
				if(attackers_talliedAbilityPotency > 0f)
					revivePercentage *= attackers_talliedAbilityPotency;
			} else if(specificActionType == SpecificActionType.Item) {
				if(attackers_talliedItemPotency > 0f)
					revivePercentage *= attackers_talliedItemPotency;
			}
			
			int reviveHealAmount = Math.round(defenderBase.getMaxHp() * revivePercentage);
			healthModInfo = new HealthModInfo(true, true, reviveHealAmount, null, cures, newlyAppliedStatuses, attributeMods_buffs, attributeMods_debuffs, postBoutInstruction);
			
			System.out.println(
					"REVIVE EFFECT != NULL, revivePercentage: " + revivePercentage +
					" * attackers_talliedAbilityPotency: " + attackers_talliedAbilityPotency +
					" OR * attackers_talliedItemPotency: " + attackers_talliedItemPotency +
					" * defenderBase.getMaxHp(): " + defenderBase.getMaxHp() +
					" = reviveHealAmount: " + reviveHealAmount
			);
		} else {
			System.err.println("CharacterBase.GetCombatCalcInfo() - Can't determine what kind of effect this action is, apparently its not a damageEffect, potionEffect or reviveEffect.");
			
			//This is an overflow for sublte combatEffects like a debuff without damage or a cure without healing or a spiritTool. Assemble these possible pieces into a healthModInfo.
			healthModInfo = new HealthModInfo((cures != null || attributeMods_buffs != null), false, 0, null, cures, newlyAppliedStatuses, attributeMods_buffs, attributeMods_debuffs, null);
		}
		
		CombatCalcInfo combatCalcInfo = new CombatCalcInfo(
				combatEffects,
				specificActionType,
				isRangedAttack,
				damageEffect,
				potionEffect,
				reviveEffect,
				defenders_talliedStatusResistance,
				defenders_talliedArmor,
				attackers_talliedBaseDamage,
				attackers_talliedAbilityPotency,
				attackers_talliedItemPotency,
				currentLingeringCureEffects,
				newlyAppliedStatuses,
				hasChanceToSurvive,
				chanceToSurvive,
				overrideIntructionWithRevive,
				postBoutInstruction,
				healthModInfo
		);
		
		return combatCalcInfo;
	}
	
	public class CombatCalcInfo {
		public CombatCalcInfo(
				//Inputs
				List<CombatEffect> combatEffects,
				SpecificActionType specificActionType,
				boolean isRangedAttack,
				
				//Calced stuff
				CombatEffect damageEffect,
				CombatEffect potionEffect,
				CombatEffect reviveEffect,
				float defenders_talliedStatusResistance,
				int defenders_talliedArmor,
				int attackers_talliedBaseDamage,
				float attackers_talliedAbilityPotency,
				float attackers_talliedItemPotency,
				List<LingeringEffect> currentLingeringCureEffects,
				List<StatusType> newlyAppliedStatuses,
				boolean hasChanceToSurvive,
				float chanceToSurvive,
				boolean overrideIntructionWithRevive,
				PostBoutInstructionType postBoutInstruction,
				
				//Outputs
				HealthModInfo healthModInfo
		) {
			this.combatEffects = combatEffects;
			this.specificActionType = specificActionType;
			this.isRangedAttack = isRangedAttack;
			this.damageEffect = damageEffect;
			this.potionEffect = potionEffect;
			this.reviveEffect = reviveEffect;
			this.defenders_talliedStatusResistance = defenders_talliedStatusResistance;
			this.defenders_talliedArmor = defenders_talliedArmor;
			this.attackers_talliedBaseDamage = attackers_talliedBaseDamage;
			this.attackers_talliedAbilityPotency = attackers_talliedAbilityPotency;
			this.attackers_talliedItemPotency = attackers_talliedItemPotency;
			this.currentLingeringCureEffects = currentLingeringCureEffects;
			this.newlyAppliedStatuses = newlyAppliedStatuses;
			this.hasChanceToSurvive = hasChanceToSurvive;
			this.chanceToSurvive = chanceToSurvive;
			this.overrideIntructionWithRevive = overrideIntructionWithRevive;
			this.postBoutInstruction = postBoutInstruction;
			this.healthModInfo = healthModInfo;
		}
		
		//Inputs
		List<CombatEffect> combatEffects;
		SpecificActionType specificActionType;
		boolean isRangedAttack;
		
		//Calced stuff
		CombatEffect damageEffect;
		CombatEffect potionEffect;
		CombatEffect reviveEffect;
		float defenders_talliedStatusResistance;
		int defenders_talliedArmor;
		int attackers_talliedBaseDamage;
		float attackers_talliedAbilityPotency;
		float attackers_talliedItemPotency;
		List<LingeringEffect> currentLingeringCureEffects;
		List<StatusType> newlyAppliedStatuses;
		boolean hasChanceToSurvive;
		float chanceToSurvive;
		boolean overrideIntructionWithRevive;
		PostBoutInstructionType postBoutInstruction;
		
		//Outputs
		public HealthModInfo healthModInfo;
	}
	
	public void ApplyCombatEffects(CombatCalcInfo calcInfo, CharacterBase defenderBase) {
		if(defenderBase == this)
			System.err.println("CharacterBase.ApplyCombatEffects() - This method could have potential problems handling ourselves as the target"
					+ "(both the attackerBase this method is called on and the defenderBase argument).");
		
		//Remove or reapply statuses
		for(CombatEffect combatEffect : calcInfo.combatEffects) {
			if(combatEffect instanceof CombatEffect_Status) {
				//Make Status effects less likely to occur during a hitting attack via StatusResistance
				if(calcInfo.defenders_talliedStatusResistance > 0f && Game.Instance().GetCharacterActionRandom().nextFloat() <= calcInfo.defenders_talliedStatusResistance)
					continue;
				
				//If we've got a pre-cure for it then cancel out this CombatEffect
				LingeringEffect preCureEffect = null;
				for(LingeringEffect lingEffect : calcInfo.currentLingeringCureEffects) {
					for(StatusType cureType : lingEffect.combatEffect.cures) {
						if(cureType == combatEffect.statusEffect) {
							preCureEffect = lingEffect;
							break;
						}
					}
					if(preCureEffect != null)
						break;
				}
				if(preCureEffect != null) {
					defenderBase.lingeringEffects.remove(preCureEffect);
				} else {
					//If theres a matching lingering effect then remove it
					LingeringEffect matchingStatusLingEffect = defenderBase.lingeringEffects.stream().filter(x -> x.combatEffect.statusEffect == combatEffect.statusEffect).findFirst().orElse(null);
					if(matchingStatusLingEffect != null)
						defenderBase.lingeringEffects.remove(matchingStatusLingEffect);
					//add this fresh one
					defenderBase.lingeringEffects.add(new LingeringEffect(combatEffect));
				}
			}
			else if(combatEffect instanceof CombatEffect_Cure) {
				boolean wereAnyStatusesCured = false;
				for(StatusType cureType : combatEffect.cures) {
					//check if there is a matching status to cure
					LingeringEffect curedLingeringEffect = defenderBase.lingeringEffects.stream().filter(x -> x.combatEffect.statusEffect == cureType).findFirst().orElse(null);
					if(curedLingeringEffect != null) {
						wereAnyStatusesCured = true;
						defenderBase.lingeringEffects.remove(curedLingeringEffect);
					}
				}
				if(!wereAnyStatusesCured && combatEffect.effectTurnDuration > 0)
					defenderBase.lingeringEffects.add(new LingeringEffect(combatEffect));
			}
			else if(combatEffect instanceof CombatEffect_Buff) {
				//If this is a TurnSequence buff then apply the new turn order
				AttributeMod sequenceMod = null;
				for(AttributeMod attributeMod : combatEffect.attributeMods_buffs) {
					if(attributeMod.attributeModType == AttributeModType.SequenceShift) {
						sequenceMod = attributeMod;
						break;
					}
				}
				if(sequenceMod != null)
					Game.Instance().ShiftTurnOrderForCharacter(defenderBase, sequenceMod.pointOffset);
				defenderBase.lingeringEffects.add(new LingeringEffect(combatEffect));
			}
			else if(combatEffect instanceof CombatEffect_Debuff) {
				defenderBase.lingeringEffects.add(new LingeringEffect(combatEffect));
			}
			else if(combatEffect instanceof CombatEffect_Revive) {
				if(defenderBase.GetHp() > 0 && combatEffect.effectTurnDuration > 0)
					defenderBase.lingeringEffects.add(new LingeringEffect(combatEffect));
			}
			
			else if(combatEffect instanceof CombatEffect_Damage || combatEffect instanceof CombatEffect_Potion) {
				//Nothing needed here
			}
			
			else if(combatEffect instanceof CombatEffect_MeleeCounter || combatEffect instanceof CombatEffect_RangedCounter || combatEffect instanceof CombatEffect_AutoDodgeProj ||
					combatEffect instanceof CombatEffect_AutoDodgeMelee || combatEffect instanceof CombatEffect_ChanceToSurvive
			) {
				System.err.println("CharacterBase.ApplyCombatEffects() - Passive type effects should never end up as something you'd send to the ApplyCombatEffects?!?");
			}
			else {
				System.err.println("CharacterBase.ApplyCombatEffects() - Add support for CombatEffect child type: " + combatEffect.getClass().getName());
			}
		}
		
		//Do all the number crunching of the defender's and attacker's tallied effects
		
		//Reconcile the defender's relavent passive and lingering effects, the attacker's relavent passive and lingering effects AND the applied effects
		if(calcInfo.damageEffect != null) {
			//Damage Calc
			int damage = data.GetTalliedAttack(calcInfo.attackers_talliedBaseDamage);
			if(!calcInfo.damageEffect.useMainAttackAsBase)
				damage = calcInfo.damageEffect.customBaseDamage;
			
			if(calcInfo.specificActionType == SpecificActionType.Ability) {
				if(calcInfo.attackers_talliedAbilityPotency > 0f)
					damage = Math.round(damage * calcInfo.attackers_talliedAbilityPotency);
			} else if(calcInfo.specificActionType == SpecificActionType.Item) {
				if(calcInfo.attackers_talliedItemPotency > 0f)
					damage = Math.round(damage * calcInfo.attackers_talliedItemPotency);
			}
			
			ElementType elementType = getRandomWeaponElement();
			//Elemental override
			if(calcInfo.damageEffect.elementalDamageType != null) 
				elementType = calcInfo.damageEffect.elementalDamageType;
			
			if(damage >= defenderBase.hp) {
				if(calcInfo.hasChanceToSurvive && Game.Instance().GetCharacterActionRandom().nextFloat() <= calcInfo.chanceToSurvive)
					if(defenderBase.hp == 1)
						damage = 0;
					else
						damage = defenderBase.hp - 1;
			}
			
			if(calcInfo.damageEffect.effectTurnDuration > 0) {
				//Divide damage across turns
				damage = damage / calcInfo.damageEffect.effectTurnDuration;
				//add to lingerers
				defenderBase.lingeringEffects.add(new LingeringEffect(calcInfo.damageEffect, damage));
			}
			
			defenderBase.TalliedTakeDamage(calcInfo.healthModInfo, calcInfo.defenders_talliedArmor);
		} else if(calcInfo.potionEffect != null) {
			int healingPoints = calcInfo.potionEffect.customHealingPoints;
			
			if(calcInfo.specificActionType == SpecificActionType.Ability) {
				if(calcInfo.attackers_talliedAbilityPotency > 0f)
					healingPoints = Math.round(healingPoints * calcInfo.attackers_talliedAbilityPotency);
			} else if(calcInfo.specificActionType == SpecificActionType.Item) {
				if(calcInfo.attackers_talliedItemPotency > 0f)
					healingPoints = Math.round(healingPoints * calcInfo.attackers_talliedItemPotency);
			}
			
			if(calcInfo.potionEffect.effectTurnDuration > 0) {
				//Divide damage across turns
				healingPoints = healingPoints / calcInfo.potionEffect.effectTurnDuration;
				//add to lingerers
				defenderBase.lingeringEffects.add(new LingeringEffect(calcInfo.potionEffect, healingPoints));
			}
			
			defenderBase.TakeHeal(calcInfo.healthModInfo);
		} else if(calcInfo.reviveEffect != null) {
			float revivePercentage = calcInfo.reviveEffect.reviveHealthPercentage;
			
			if(calcInfo.specificActionType == SpecificActionType.Ability) {
				if(calcInfo.attackers_talliedAbilityPotency > 0f)
					revivePercentage *= calcInfo.attackers_talliedAbilityPotency;
			} else if(calcInfo.specificActionType == SpecificActionType.Item) {
				if(calcInfo.attackers_talliedItemPotency > 0f)
					revivePercentage *= calcInfo.attackers_talliedItemPotency;
			}
			
			defenderBase.Revive(revivePercentage);
		}
		
		//Update our convenience members if we're targeting ourselves, otherwise update the defender
		if(defenderBase == this) {
			//This method sets the new status state to display, if this isn't a lingering effect happening on turn start. Lingering actions will set calcInfo.specificActionType to null.
			if(calcInfo.specificActionType != null) {
				System.out.println("ApplyEffects to ourselves: " + calcInfo.specificActionType);
				
				ApplyLingeringEffectsDuringCombat();
			}
			
			tilePenaltyMod = TallyTilePenaltyMod();
		}
		//This else block is new. It's being added because, prior to this, the defender wasn't having their lingering effects applied until the start of their turn; which was insufficient.
		else {
			if(calcInfo.specificActionType != null) {
				System.out.println("ApplyEffects to defender: " + calcInfo.specificActionType);
				
				defenderBase.ApplyLingeringEffectsDuringCombat();
			}
			
			defenderBase.tilePenaltyMod = defenderBase.TallyTilePenaltyMod();
		}
	}
	
	public boolean CanMove() {
		return !activeStatuses.contains(StatusType.Cripple);
	}
	
	public boolean CanPerformAttack() {
		return !activeStatuses.contains(StatusType.Daze);
	}
	
	public boolean CanPerformAbility() {
		return !activeStatuses.contains(StatusType.Silence);
	}
	
	private boolean CanPerformDamagingAbility() {
		if(!CanPerformAbility())
			return false;
		List<Ability> abilities = data.GetLearnedAbilities();
		return abilities.stream().anyMatch(x -> x.combatEffects.stream().anyMatch(e ->
				e.battleItemTypeEffect == BattleItemType.Damage
				||
				e.battleItemTypeEffect == BattleItemType.Debuff
				||
				e.battleItemTypeEffect == BattleItemType.Status
		));
	}
	
	private boolean HasAnyHealingAbilities() {
		List<Ability> abilities = data.GetLearnedAbilities();
		return abilities.stream().anyMatch(x -> IsHealingAbility(x));
	}
	
	private boolean IsHealingAbility(Ability ability) {
		return ability.combatEffects.stream().anyMatch(e -> e.battleItemTypeEffect == BattleItemType.Potion);
	}
	
	private boolean CanPerformHeal() {
		boolean hasHealingItem = Game.Instance().GetInventory().stream().anyMatch(x -> x.getType() == ItemType.BattleItem);
		//TODO filter items further by whether they have healing properties
		
		
		boolean hasHealingAbility = HasAnyHealingAbilities();
		return hasHealingItem || hasHealingAbility;
	}
	
	public ItemData[] GetAccessableBattleItems(boolean isAI) {
		if(isAI)
			System.err.println("CharacterBase.GetAccessableBattleItems() - Giving AI Access to every possible item. At some point each Battle should be retrofitted with its own repository of"
					+ "BattleItems for the AI to use.");
		return isAI ?
				Items.itemList.stream().filter(x -> x.getType() == ItemType.BattleItem).toArray(ItemData[]::new)
				:
				Game.Instance().GetBattleItemsFromInventory().stream().toArray(ItemData[]::new);
	}
	
	/**
	 * Filter inventory items or the items the AI gets access to in order to determine whether they have access to any items with damaging properties.
	 * @param isAI - This determines whether the player inventory or some other collection of items will be searched.
	 * @return
	 */
	private boolean CanPerformDamagingItem(boolean isAI) {
		ItemData[] playerOrAIInventory = GetAccessableBattleItems(isAI);
		boolean hasAccessToDamagingItem = false;
		for(ItemData itemData : playerOrAIInventory) {
			for(BattleItemType battleItemType : BattleItemTraits.GetAllBattleItemTypes(itemData)) {
				if(battleItemType == BattleItemType.Damage || battleItemType == BattleItemType.Debuff || battleItemType == BattleItemType.Status) {
					hasAccessToDamagingItem = true;
					break;
				}
			}
			if(hasAccessToDamagingItem)
				break;
		}
		return hasAccessToDamagingItem;
	}
	
	public boolean CanPerformAction(ObjectiveType actionType, boolean isAI) {
		switch(actionType) {
			case Attack:
				return CanPerformAttack() || CanPerformDamagingAbility() || CanPerformDamagingItem(isAI);
			case Heal:
				return CanPerformHeal();
			case Wait:
				return true;
			default:
				System.err.println("CharacterBase.CanPreformAction() - Add support for ActionType: " + actionType);
				return false;
		}
	}
	
	public int GetMinWeaponRange() {
		int minestRange = 1;
		for(ItemData item : data.GetData()) {
			if(item != null && (item.getStats().getEquipmentType() == EquipmentType.RightHand || item.getStats().getEquipmentType() == EquipmentType.LeftHand)) {
				if(item.getStats().GetBattleToolTraits().minRange < minestRange)
					minestRange = item.getStats().GetBattleToolTraits().minRange;
			}
		}
		return minestRange;
	}
	
	public int GetMaxWeaponRange() {
		int standardAttackRange = 1;
		for(ItemData item : data.GetData()) {
			if(item != null && (item.getStats().getEquipmentType() == EquipmentType.RightHand || item.getStats().getEquipmentType() == EquipmentType.LeftHand)) {
				if(item.getStats().GetBattleToolTraits().maxRange > standardAttackRange)
					standardAttackRange = item.getStats().GetBattleToolTraits().maxRange;
			}
		}
		return standardAttackRange;
	}
	
	public int GetMaxRangeForAction(ObjectiveType actionType) {
		List<Ability> abilities = data.GetLearnedAbilities();
		switch(actionType) {
			case Attack:
				int standardAttackRange = GetMaxWeaponRange();
				
				int abilityRange = 0;
				for(Ability ability : abilities) {
					if(ability.range_max > abilityRange)
						abilityRange = ability.range_max;
				}
				
				int attackMaxRange = Math.max(standardAttackRange, abilityRange);
				return attackMaxRange;
			case Heal:
				int potionRange = 0;
				for(ItemData item : Game.Instance().GetInventory().stream().filter(x -> x.getType() == ItemType.BattleItem && x.getStats().getHp() > 0).toArray(ItemData[]::new)) {
					if(item.getStats().GetBattleToolTraits().maxRange > potionRange)
						potionRange = item.getStats().GetBattleToolTraits().maxRange;
				}
				int healAbilityRange = 0;
				for(Ability ability : abilities) {
					if(!IsHealingAbility(ability))
						continue;
					if(ability.range_max > healAbilityRange)
						healAbilityRange = ability.range_max;
				}
				
				int healMaxRange = Math.max(potionRange, healAbilityRange);
				return healMaxRange;
			case Wait:
				return -1;
			default:
				System.err.println("CharacterBase.GetMaxRangeFoAction() - Add support for ActionType: " + actionType);
				return -1;
		}
	}
	
	public int GetMaxWeaponAOE() {
		int standardAttackAOE = 0;
		for(ItemData item : data.GetData()) {
			if(item != null && (item.getStats().getEquipmentType() == EquipmentType.RightHand || item.getStats().getEquipmentType() == EquipmentType.LeftHand)) {
				int weaponAOERange = item.getStats().GetBattleToolTraits().aoeRange;
				if(weaponAOERange > standardAttackAOE)
					standardAttackAOE = weaponAOERange;
			}
		}
		return standardAttackAOE;
	}
	
	//Check the units ability to reach distant tiles relative to the action type
	public int GetMaxAOERangeForAction(ObjectiveType actionType) {
		List<Ability> abilities = data.GetLearnedAbilities();
		switch(actionType) {
			case Attack:
				int standardAttackAOE = GetMaxWeaponAOE();
				
				int abilityAOERange = 0;
				for(Ability ability : abilities) {
					int thisAOERange = ability.hitRadius;
					if(thisAOERange > abilityAOERange)
						abilityAOERange = thisAOERange;
				}
				
				int attackMaxAOE = Math.max(standardAttackAOE, abilityAOERange);
				return attackMaxAOE;
			case Heal:
				int potionAOE = 0;
				for(ItemData item : Game.Instance().GetInventory().stream().filter(x -> x.getType() == ItemType.BattleItem && x.getStats().getHp() > 0).toArray(ItemData[]::new)) {
					if(item.getStats().GetBattleToolTraits().aoeRange > potionAOE)
						potionAOE = item.getStats().GetBattleToolTraits().aoeRange;
				}
				int healAbilityAOE = 0;
				for(Ability ability : abilities) {
					if(!IsHealingAbility(ability))
						continue;
					if(ability.hitRadius > healAbilityAOE)
						healAbilityAOE = ability.hitRadius;
				}
				
				int healMaxAOE = Math.max(potionAOE, healAbilityAOE);
				return healMaxAOE;
			case Wait:
				return -1;
			default:
				System.err.println("CharacterBase.GetMaxRangeFoAction() - Add support for ActionType: " + actionType);
				return -1;
		}
	}
	
	public class ActionOptionInfo {
		public ActionOptionInfo(Ability ability, ItemData itemData, int rangeMin, int rangeMax, int aoe, List<Tile> possibleMoveLocations) {
			this.ability = ability;	
			this.itemData = itemData;
			this.rangeMin = rangeMin;
			this.rangeMax = rangeMax;
			this.aoe = aoe;
			this.possibleMoveLocations = possibleMoveLocations;
		}
		
		//If Ability and ItemData are null then we know this is the ActionInfo for MainAttack
		public Ability ability;
		public ItemData itemData;
		public int rangeMin;
		public int rangeMax;
		public int aoe;
		public List<Tile> possibleMoveLocations;
	}
	
	private List<Tile> GetPossiblePositionsToAttackFrom(int minWeaponRange, int maxWeaponRange, int maxWeaponAOE, List<Tile> checkFromLocations, Point targetLoc) {
		boolean canMainAttackReach = false;
		List<Tile> possibleMoveLocations = new ArrayList<Tile>();
		for(Tile tile : checkFromLocations) {
			int distanceToTarget = Game.GetDistance(tile.Location(), targetLoc.getLocation());
			if(maxWeaponRange >= distanceToTarget && distanceToTarget > minWeaponRange)
				canMainAttackReach = true;
			else {
				int outOfRangeDistance = maxWeaponRange >= distanceToTarget ? distanceToTarget - maxWeaponRange : Math.abs(distanceToTarget - minWeaponRange);
				if(maxWeaponAOE >= outOfRangeDistance)
					canMainAttackReach = true;
			}
			//If the MainAttack is in range then add it to our options
			if(canMainAttackReach)
				possibleMoveLocations.add(tile);
		}
		return possibleMoveLocations;
	}
	
	/**
	 * Uses most the other convenience methods in this class to wrap up a nice and neat package for the Game's AI logic to analysis and select an option from.
	 * @return
	 */
	public List<ActionOptionInfo> GetAttackOptionInfos(Tile currentLocation, List<Tile> availableMoves, Point targetLoc, boolean isAI) {
		System.out.println("CHARACTER_BASE.GET_ATTACK_OPTION_INFOS()");
		
		List<ActionOptionInfo> actionOptionInfos = new ArrayList<ActionOptionInfo>();
		
		List<Tile> checkFromLocations = new ArrayList<Tile>( availableMoves );
		checkFromLocations.add(currentLocation);
		
		//TODO DEBUGGING
		//System.err.println("DEBUGGING @ CharacterBase.GetAttackOptionInfos() - Disabling MainAttack and Item options.");
		//boolean disableOption = false;
		
		//Determine if MainAttack is an option
		if(CanPerformAttack()) {
		//TODO DEBUGGING
		//if(disableOption) {
			
			int minWeaponRange = GetMinWeaponRange();
			int maxWeaponRange = GetMaxWeaponRange();
			int maxWeaponAOE = GetMaxWeaponAOE();
			List<Tile> possibleMoveLocations = GetPossiblePositionsToAttackFrom(minWeaponRange, maxWeaponRange, maxWeaponAOE, checkFromLocations, targetLoc);
			if(possibleMoveLocations.size() > 0)
				actionOptionInfos.add(new ActionOptionInfo(null, null, minWeaponRange, maxWeaponRange, maxWeaponAOE, possibleMoveLocations));
			
			System.out.println("	-Can preform MainAttack with possible Moves: " + possibleMoveLocations.size());
		}
		
		//Determine if damaging abilities are an option
		if(CanPerformAbility()) {
			List<Ability> abilities = data.GetLearnedAbilities();
			Ability[] damagingAbilities = abilities.stream().filter(x -> x.combatEffects.stream().anyMatch(e ->
					e.battleItemTypeEffect == BattleItemType.Damage
					||
					e.battleItemTypeEffect == BattleItemType.Debuff
					||
					e.battleItemTypeEffect == BattleItemType.Status
			)).toArray(Ability[]::new);
			if(damagingAbilities != null && damagingAbilities.length > 0) {
				for(Ability ability : damagingAbilities) {
					//If the Ability is in range then add it to our options
					int minRange = ability.range_min;
					int maxRange = ability.range_max;
					int aoe = ability.hitRadius;
					List<Tile> possibleMoveLocations = GetPossiblePositionsToAttackFrom(minRange, maxRange, aoe, checkFromLocations, targetLoc);
					if(possibleMoveLocations.size() > 0) {
						actionOptionInfos.add(new ActionOptionInfo(ability, null, minRange, maxRange, aoe, possibleMoveLocations));
					
						System.out.println("	-Can preform Ability: " + ability.name + ", with possible Moves: " + possibleMoveLocations.size());
					}
				}
			}
		}
		
		//Determine if there are damaging BattleItems we could use
		ItemData[] accessableBattleItems = GetAccessableBattleItems(isAI);
		if(accessableBattleItems.length > 0) {
		//TODO DEBUGGING
		//if(disableOption) {
		
			//determine if there are any damaging items
			for(ItemData itemData : accessableBattleItems) {
				List<BattleItemType> types = BattleItemTraits.GetAllBattleItemTypes(itemData);
				if(types.contains(BattleItemType.Accelerant) || types.contains(BattleItemType.Damage) || types.contains(BattleItemType.Debuff) || types.contains(BattleItemType.Status)) {
					//check if those items are capable of reaching the target
					//If the Item is in range then add it to our options
					int minRange = itemData.getStats().GetBattleToolTraits().minRange;
					int maxRange = itemData.getStats().GetBattleToolTraits().maxRange;
					int aoe = itemData.getStats().GetBattleToolTraits().aoeRange;
					List<Tile> possibleMoveLocations = GetPossiblePositionsToAttackFrom(minRange, maxRange, aoe, checkFromLocations, targetLoc);
					if(possibleMoveLocations.size() > 0) {
						actionOptionInfos.add(new ActionOptionInfo(null, itemData, minRange, maxRange, aoe, possibleMoveLocations));
						
						System.out.println("	-Can preform Item: " + itemData.getName() + ", with possible Moves: " + possibleMoveLocations.size());
					}
				}
			}
		}
		
		return actionOptionInfos;
	}
	
	public String toString() {
		return "Name: "+ data.getName() +"\nHealth: "+ hp +"\nArmor: "+ armor;
	}
	
	public void ClearPaths() {
		if(paths != null)
			paths.clear();
	}
	
	boolean debug_applyAllStatusesOnFirstTurn;
	
	/**
	 * Called by Game at the start of this character's turn. This method will be skipped when the BattleState is being restored. Game will handle the turnTaker's remaining lingeringEffects that
	 * didn't finish in the last session.
	 */
	public void OnTurnStart() {
		//System.out.println("CharacterBase.OnTurnStart() for: " + this.data.getName());
		
		
		//TODO DEBUGGING
		/*if(!debug_applyAllStatusesOnFirstTurn && (lingeringEffects == null || lingeringEffects.size() == 0)) {
			debug_applyAllStatusesOnFirstTurn = true;
			System.err.println("DEBUGGING @ CharacterBase.OnTurnStart() - Applying all Status LingeringEffects on this Character's first turn start.");
			this.lingeringEffects.clear();
			lingeringEffects.add(new LingeringEffect(new CombatEffect_Status(1, StatusType.Accelerated)));
			lingeringEffects.add(new LingeringEffect(new CombatEffect_Status(1, StatusType.Blind)));
			lingeringEffects.add(new LingeringEffect(new CombatEffect_Status(1, StatusType.Charmed)));
			lingeringEffects.add(new LingeringEffect(new CombatEffect_Status(1, StatusType.Cripple)));
			lingeringEffects.add(new LingeringEffect(new CombatEffect_Status(1, StatusType.Daze)));
			lingeringEffects.add(new LingeringEffect(new CombatEffect_Status(1, StatusType.Fear)));
			lingeringEffects.add(new LingeringEffect(new CombatEffect_Status(1, StatusType.Goad)));
			lingeringEffects.add(new LingeringEffect(new CombatEffect_Status(1, StatusType.Silence)));
		}*/
		
		
		//This method sets our activeStatuses member
		LingeringAnimsInfo animsInfo = ApplyLingeringEffectsOnTurnStartAndGetInfo();
		
		tilePenaltyMod = TallyTilePenaltyMod();
		
		ap = maxAp;
		hasMoved = false;
		hasUsedAction = false;
		
		if(animsInfo != null)
			Game.Instance().HandleLingeringEffectAnims(animsInfo.doHealingFirst, animsInfo.hpLossThisTurn, animsInfo.hpGainThisTurn, animsInfo.doRevive, animsInfo.revivePercentage);
		else
			Game.Instance().OnLingeringAnimsDone();
	}
	
	/**
	 * Called by Game when a characters turn is done.
	 */
	public void OnTurnEnd() {
		//Counts down effect's remaining turns count while also flagging completed ones
		LingeringEffect[] effectsToRemove = lingeringEffects.stream().filter(x -> x.DecrementAndCheckDone()).toArray(LingeringEffect[]::new);
		if(effectsToRemove != null && effectsToRemove.length > 0) {
			for(LingeringEffect removedEffect : effectsToRemove) {
				lingeringEffects.remove(removedEffect);
			}
		}
	}
	
	//Status switch
	/*
	switch(combatEffect.statusEffect) {
		case Accelerated: break;
		case Blind: break;
		case Charmed: break;
		case Cripple: break;
		case Daze: break;
		case Fear: break;
		case Goad: break;
		case Poisoned: break;
		case Silence: break;
		default: System.err.println("StatusEffect Switch - Add support for: " + combatEffect.statusEffect); break;
	}
	*/
	
	/**
	 * List is updated at the start of this characters turn and maintained during ApplyCombatEffect method. Its a convinence for other classes when they need to handle this character's status effects.
	 */
	public List<StatusType> activeStatuses = new ArrayList<StatusType>();
	public List<StatusType> GetActiveStatuses() { return activeStatuses; }
	
	private void ApplyLingeringEffectsDuringCombat() {
		activeStatuses.clear();
		boolean hasAnyBuffs = false;
		boolean hasAnyDebuffs = false;
		boolean hasAnyCures = false;
		for(LingeringEffect lingeringEffect : lingeringEffects) {
			CombatEffect combatEffect = lingeringEffect.combatEffect;
			
			if(combatEffect instanceof CombatEffect_Damage) {
			}
			else if(combatEffect instanceof CombatEffect_Potion) {
			}
			else if(combatEffect instanceof CombatEffect_Revive) {
			}
			else if(combatEffect instanceof CombatEffect_Status) {
				activeStatuses.add(combatEffect.statusEffect);
			}
			else if(combatEffect instanceof CombatEffect_Buff) {
				hasAnyBuffs = true;
			}
			else if(combatEffect instanceof CombatEffect_Debuff) {
				hasAnyDebuffs = true;
			}
			else if(combatEffect instanceof CombatEffect_Cure) {
				hasAnyCures = true;
			}
			
			else if(combatEffect instanceof CombatEffect_MeleeCounter || combatEffect instanceof CombatEffect_RangedCounter || combatEffect instanceof CombatEffect_AutoDodgeProj ||
					combatEffect instanceof CombatEffect_AutoDodgeMelee || combatEffect instanceof CombatEffect_ChanceToSurvive
			) {
				System.err.println("CharacterBase.ApplyLingeringEffectsDuringCombat() - Passive type effects should never end up as something you'd have stored in lingeringEffects member list?!?"
						+ " Those sorts of things should be stored in the passiveCombatEffects member list.");
			}
			else {
				System.err.println("CharacterBase.ApplyLingeringEffectsDuringCombat() - Add support for CombatEffect child type: " + combatEffect.getClass().getName());
			}
		}
		Game.Instance().GetBattlePanel().UpdateMiniEffectFeedback(this, activeStatuses, hasAnyBuffs, hasAnyDebuffs, hasAnyCures);
	}
	
	private LingeringAnimsInfo ApplyLingeringEffectsOnTurnStartAndGetInfo() {	
		int indexOfDamage = 0;
		int hpLossThisTurn = 0;
		int indexOfPotion = 0;
		int hpGainThisTurn = 0;
		LingeringEffect reviveLingEffect = null;
		activeStatuses.clear();
		boolean hasAnyBuffs = false;
		boolean hasAnyDebuffs = false;
		boolean hasAnyCures = false;
		
		int index = 0;
		for(LingeringEffect lingeringEffect : lingeringEffects) {
			CombatEffect combatEffect = lingeringEffect.combatEffect;
			
			if(combatEffect instanceof CombatEffect_Damage) {
				indexOfDamage = index;
				hpLossThisTurn += lingeringEffect.hpChangePerTurn;
			}
			else if(combatEffect instanceof CombatEffect_Potion) {
				indexOfPotion = index;
				hpGainThisTurn += lingeringEffect.hpChangePerTurn;
			}
			else if(combatEffect instanceof CombatEffect_Revive) {
				reviveLingEffect = lingeringEffect;
			}
			else if(combatEffect instanceof CombatEffect_Status) {
				activeStatuses.add(combatEffect.statusEffect);
			}
			else if(combatEffect instanceof CombatEffect_Buff) {
				hasAnyBuffs = true;
			}
			else if(combatEffect instanceof CombatEffect_Debuff) {
				hasAnyDebuffs = true;
			}
			else if(combatEffect instanceof CombatEffect_Cure) {
				hasAnyCures = true;
			}
			
			else if(combatEffect instanceof CombatEffect_MeleeCounter || combatEffect instanceof CombatEffect_RangedCounter || combatEffect instanceof CombatEffect_AutoDodgeProj ||
					combatEffect instanceof CombatEffect_AutoDodgeMelee || combatEffect instanceof CombatEffect_ChanceToSurvive
			) {
				System.err.println("CharacterBase.ApplyLingeringEffectsOnTurnStart() - Passive type effects should never end up as something you'd have stored in lingeringEffects member list?!?"
						+ " Those sorts of things should be stored in the passiveCombatEffects member list.");
			}
			else {
				System.err.println("CharacterBase.ApplyLingeringEffectsOnTurnStart() - Add support for CombatEffect child type: " + combatEffect.getClass().getName());
			}
			
			index++;
		}
		
		Game.Instance().GetBattlePanel().UpdateMiniEffectFeedback(this, activeStatuses, hasAnyBuffs, hasAnyDebuffs, hasAnyCures);
		
		
		//TODO DEBUGGING Comment this out once finished debugging
		/*System.err.println("DEBUGGING @ CharacterBase.ApplyLingeringEffectsOnTurnStart() - Applying Lingering Healing, Damage and Revive.");
		indexOfDamage = 1;
		hpLossThisTurn = 99;
		indexOfPotion = 0;
		hpGainThisTurn = 1;
		reviveLingEffect = new LingeringEffect(new CombatEffect_Revive(0, 1f));*/
		
		//Send these happenings thru the CombatAnimPane system so we can see the lingering damage and/or healing and possibly even a revive occur at the start of this character's turn.
		//Possible first and second anims
		List<CharacterBase> targets = new ArrayList<CharacterBase>();
		targets.add(this);
		boolean doHealingFirst = false;
		boolean doRevive = false;
		float revivePercentage = 0f;
		int simulatedHP = this.hp;
		boolean didHpReachZero = false;
		if(indexOfDamage <= indexOfPotion) {
			if(hpLossThisTurn > 0) {
				simulatedHP -= hpLossThisTurn;
				if(simulatedHP <= 0) {
					didHpReachZero = true;
					hpGainThisTurn = 0;
				}
			}
			doHealingFirst = false;
		} else {
			if(hpGainThisTurn > 0)
				simulatedHP += hpGainThisTurn;
			if(hpLossThisTurn > 0) {
				simulatedHP -= hpLossThisTurn;
				if(simulatedHP <= 0)
					didHpReachZero = true;
			}
			doHealingFirst = true;
		}
		//Another possible anim to play after TakeDamage and/or TakeHeal
		if(didHpReachZero && reviveLingEffect != null) {
			lingeringEffects.remove(reviveLingEffect);
			doRevive = true;
			revivePercentage = reviveLingEffect.combatEffect.reviveHealthPercentage;
		}
		//This will send all necessary info to the game so it can prepare the infos and route them to the COmbatAnimPane one after the other
		if(hpLossThisTurn > 0 || hpGainThisTurn > 0 || doRevive)
			return new LingeringAnimsInfo(doHealingFirst, hpLossThisTurn, hpGainThisTurn, doRevive, revivePercentage);
		else
			return null;
	}
	
	public class LingeringAnimsInfo {
		public LingeringAnimsInfo(boolean doHealingFirst, int hpLossThisTurn, int hpGainThisTurn, boolean doRevive, float revivePercentage) {
			this.doHealingFirst = doHealingFirst;
			this.hpLossThisTurn = hpLossThisTurn;
			this.hpGainThisTurn = hpGainThisTurn;
			this.doRevive = doRevive;
			this.revivePercentage = revivePercentage;
		}
		boolean doHealingFirst;
		int hpLossThisTurn;
		int hpGainThisTurn;
		boolean doRevive;
		float revivePercentage;
	}
}
