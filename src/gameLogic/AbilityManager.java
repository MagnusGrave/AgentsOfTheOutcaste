package gameLogic;

import java.util.Arrays;
import java.util.List;

import data.AttributeMod;
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
import dataShared.ActorData;
import enums.AttributeModType;
import enums.BattleItemType;
import enums.ClassType;
import enums.ElementType;
import enums.HitSelectionType;
import enums.StatusType;

public final class AbilityManager {

	//Ability Definition - Start
	
	public class Ability {
		private Ability(
			String name,
			String description,
			CombatEffect[] combatEffects
		) {
			this.name = name;
			this.description = description;
			this.combatEffects = (List<CombatEffect>)Arrays.asList(combatEffects);
			this.isActiveAbility = false;
		}
		private Ability(
			String name,
			String description,
			CombatEffect[] combatEffects,
			float chanceToHitFactor,
			HitSelectionType hitSelection, int hitRadius, int range_min, int range_max
		) {
			this.name = name;
			this.description = description;
			
			this.combatEffects = (List<CombatEffect>)Arrays.asList(combatEffects);
			
			this.isActiveAbility = true;
			
			this.chanceToHitFactor = chanceToHitFactor;
			
			this.hitSelection = hitSelection;
			this.hitRadius = hitRadius;
			this.range_min = range_min;
			this.range_max = range_max;
		}
		
		//informational
		public String name;
		public String description;
		
		/**
		 * Passive and active abilities are applied the same way via combatEffects. The only difference between an active Buff
		 * and a Permenant Passive is that the active buff is only applied during an effectTurnDuration while the Permanent
		 * Passive is applied every turn.
		 */
		public List<CombatEffect> combatEffects;
		
		
		//mark whether this is a passive or active ability
		public boolean isActiveAbility;
		
		
		//Hit/Miss
		float chanceToHitFactor;
		
		//Targeting
		public HitSelectionType hitSelection;
		public int hitRadius;
		public int range_min;
		public int range_max;
	}
	
	//Ability Definition - End
	
	public class AbilitiesNode {
		public AbilitiesNode(Ability[] abilities) {
			this.abilities = abilities;
		}
		public Ability[] abilities;
	}
	
	//Hard-coded Data - Start
	
	private static Ability ability_counterattack;
	private static Ability ability_baseDamage;
	private static Ability ability_crippleSlash;
	private static Ability ability_waterWheel;
	private static Ability ability_trainingBoulder;
	private static Ability ability_hunkerDown;
	
	private static AbilitiesNode[] abilityTree_ronin;
	
	
	private static Ability ability_tilePenaltyRedux;
	private static Ability ability_blind;
	private static Ability ability_autoDodgeProj;
	private static Ability ability_darkness1;
	private static Ability ability_darkness2;
	private static Ability ability_water2;
		
	private static AbilitiesNode[] abilityTree_ninja;
	
	
	private static Ability ability_daze;
	private static Ability ability_rangedCounter;
	private static Ability ability_chanceToHitBuff;
	private static Ability ability_water1;
	private static Ability ability_wind1;
	private static Ability ability_wind3;
	
	private static AbilitiesNode[] abilityTree_bandit;
	
	
	private static Ability ability_silence;
	private static Ability ability_autoDodgeMelee;
	private static Ability ability_statusResistence;
	private static Ability ability_heal2;
	private static Ability ability_holy1;
	private static Ability ability_holy2;
	
	private static AbilitiesNode[] abilityTree_monk;
	
	
	private static Ability ability_abilityPotency;
	private static Ability ability_heal1;
	private static Ability ability_heal5;
	private static Ability ability_lightning3;
	private static Ability ability_ice1;
	private static Ability ability_ice3;
	private static Ability ability_fire3;
	
	private static AbilitiesNode[] abilityTree_priest;
	
	
	private static Ability ability_heal4;
	private static Ability ability_holy3;
	private static Ability ability_lightning2;
	private static Ability ability_lightning4;
	private static Ability ability_ice2;
	private static Ability ability_ice4;
	private static Ability ability_wind2;
	private static Ability ability_water3;
	private static Ability ability_water4;
	private static Ability ability_clawBite1;
	private static Ability ability_clawBite3;
	private static Ability ability_timeSpeedUp;
	private static Ability ability_timeSlowDown;
	private static Ability ability_earth1_1;
	private static Ability ability_diamond2;
	private static Ability ability_explosion1;
	private static Ability ability_explosion2;
	
	private static AbilitiesNode[] abilityTree_kami_ar;
	private static AbilitiesNode[] abilityTree_kami_er;
	private static AbilitiesNode[] abilityTree_kami_ey;
	private static AbilitiesNode[] abilityTree_kami_in;
	private static AbilitiesNode[] abilityTree_kami_ka;
	private static AbilitiesNode[] abilityTree_kami_ko;
	private static AbilitiesNode[] abilityTree_kami_ky;
	private static AbilitiesNode[] abilityTree_kami_oi;
	private static AbilitiesNode[] abilityTree_kami_ok;
	private static AbilitiesNode[] abilityTree_kami_wa;
	
	
	private static Ability ability_chanceToSurvive;
	private static Ability ability_increaseItemPotency;
	private static Ability ability_fire1;
	private static Ability ability_barrage;
	private static Ability ability_diamond1;
	private static Ability ability_cannonFire;
	
	private static AbilitiesNode[] abilityTree_surf;
	
	
	private static Ability ability_goad;
	private static Ability ability_baseArmor;
	private static Ability ability_lightning1;
	private static Ability ability_wind4;
	private static Ability ability_commendation;
	private static Ability ability_diamond3;
	
	private static AbilitiesNode[] abilityTree_diamyo;
	
	
	private static Ability ability_fear;
	private static Ability ability_baseHp;
	private static Ability ability_darkness3;
	private static Ability ability_fire2;
	private static Ability ability_fire4;
	private static Ability ability_earth1_2;
	
	private static AbilitiesNode[] abilityTree_oni;
	
	
	private static Ability ability_charm;
	private static Ability ability_chanceToDodge;
	private static Ability ability_heal3;
	private static Ability ability_plants1;
	private static Ability ability_plants2;
	private static Ability ability_clawBite2;
	
	private static AbilitiesNode[] abilityTree_nekomata;
	
	public static Ability getAbility_counterattack() {
		return ability_counterattack;
	}

	public static Ability getAbility_baseDamage() {
		return ability_baseDamage;
	}

	public static Ability getAbility_crippleSlash() {
		return ability_crippleSlash;
	}

	public static Ability getAbility_waterWheel() {
		return ability_waterWheel;
	}

	public static Ability getAbility_trainingBoulder() {
		return ability_trainingBoulder;
	}

	public static Ability getAbility_hunkerDown() {
		return ability_hunkerDown;
	}

	public static Ability getAbility_tilePenaltyRedux() {
		return ability_tilePenaltyRedux;
	}

	public static Ability getAbility_blind() {
		return ability_blind;
	}

	public static Ability getAbility_autoDodgeProj() {
		return ability_autoDodgeProj;
	}

	public static Ability getAbility_darkness1() {
		return ability_darkness1;
	}

	public static Ability getAbility_darkness2() {
		return ability_darkness2;
	}

	public static Ability getAbility_water2() {
		return ability_water2;
	}

	public static Ability getAbility_daze() {
		return ability_daze;
	}

	public static Ability getAbility_rangedCounter() {
		return ability_rangedCounter;
	}

	public static Ability getAbility_chanceToHitBuff() {
		return ability_chanceToHitBuff;
	}

	public static Ability getAbility_water1() {
		return ability_water1;
	}

	public static Ability getAbility_wind1() {
		return ability_wind1;
	}

	public static Ability getAbility_wind3() {
		return ability_wind3;
	}

	public static Ability getAbility_silence() {
		return ability_silence;
	}

	public static Ability getAbility_autoDodgeMelee() {
		return ability_autoDodgeMelee;
	}

	public static Ability getAbility_statusResistence() {
		return ability_statusResistence;
	}

	public static Ability getAbility_heal2() {
		return ability_heal2;
	}

	public static Ability getAbility_holy1() {
		return ability_holy1;
	}

	public static Ability getAbility_holy2() {
		return ability_holy2;
	}

	public static Ability getAbility_abilityPotency() {
		return ability_abilityPotency;
	}

	public static Ability getAbility_heal1() {
		return ability_heal1;
	}

	public static Ability getAbility_heal5() {
		return ability_heal5;
	}

	public static Ability getAbility_lightning3() {
		return ability_lightning3;
	}

	public static Ability getAbility_ice1() {
		return ability_ice1;
	}

	public static Ability getAbility_ice3() {
		return ability_ice3;
	}

	public static Ability getAbility_fire3() {
		return ability_fire3;
	}

	public static Ability getAbility_heal4() {
		return ability_heal4;
	}

	public static Ability getAbility_holy3() {
		return ability_holy3;
	}
	
	public static Ability getAbility_lightning2() {
		return ability_lightning2;
	}

	public static Ability getAbility_lightning4() {
		return ability_lightning4;
	}

	public static Ability getAbility_ice2() {
		return ability_ice2;
	}
	
	public static Ability getAbility_ice4() {
		return ability_ice4;
	}

	public static Ability getAbility_wind2() {
		return ability_wind2;
	}
	
	public static Ability getAbility_water3() {
		return ability_water3;
	}

	public static Ability getAbility_water4() {
		return ability_water4;
	}

	public static Ability getAbility_clawBite1() {
		return ability_clawBite1;
	}

	public static Ability getAbility_clawBite3() {
		return ability_clawBite3;
	}
	
	public static Ability getAbility_timeSpeedUp() {
		return ability_timeSpeedUp;
	}
	
	public static Ability getAbility_timeSlowDown() {
		return ability_timeSlowDown;
	}
	
	public static Ability getAbility_earth1_1() {
		return ability_earth1_1;
	}
	
	public static Ability getAbility_diamond2() {
		return ability_diamond2;
	}
	
	public static Ability getAbility_explosion1() {
		return ability_explosion1;
	}
	
	public static Ability getAbility_explosion2() {
		return ability_explosion2;
	}
	
	public static Ability getAbility_chanceToSurvive() {
		return ability_chanceToSurvive;
	}

	public static Ability getAbility_increaseItemPotency() {
		return ability_increaseItemPotency;
	}

	public static Ability getAbility_fire1() {
		return ability_fire1;
	}

	public static Ability getAbility_barrage() {
		return ability_barrage;
	}

	public static Ability getAbility_diamond1() {
		return ability_diamond1;
	}

	public static Ability getAbility_cannonFire() {
		return ability_cannonFire;
	}

	public static Ability getAbility_goad() {
		return ability_goad;
	}

	public static Ability getAbility_baseArmor() {
		return ability_baseArmor;
	}

	public static Ability getAbility_lightning1() {
		return ability_lightning1;
	}

	public static Ability getAbility_wind4() {
		return ability_wind4;
	}

	public static Ability getAbility_commendation() {
		return ability_commendation;
	}

	public static Ability getAbility_diamond3() {
		return ability_diamond3;
	}

	public static Ability getAbility_fear() {
		return ability_fear;
	}

	public static Ability getAbility_baseHp() {
		return ability_baseHp;
	}

	public static Ability getAbility_darkness3() {
		return ability_darkness3;
	}

	public static Ability getAbility_fire2() {
		return ability_fire2;
	}

	public static Ability getAbility_fire4() {
		return ability_fire4;
	}

	public static Ability getAbility_earth1_2() {
		return ability_earth1_2;
	}

	public static Ability getAbility_charm() {
		return ability_charm;
	}

	public static Ability getAbility_chanceToDodge() {
		return ability_chanceToDodge;
	}

	public static Ability getAbility_heal3() {
		return ability_heal3;
	}

	public static Ability getAbility_plants1() {
		return ability_plants1;
	}

	public static Ability getAbility_plants2() {
		return ability_plants2;
	}

	public static Ability getAbility_clawBite2() {
		return ability_clawBite2;
	}
	
	
	static {
		AbilityManager instance = new AbilityManager();
		
		//RONIN
		
		ability_counterattack = instance.new Ability(
			"Melee Counterattack",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_MeleeCounter()
			}
		);
		
		ability_baseDamage = instance.new Ability(
			"PP: Base Damage",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Buff(-1, new AttributeMod[]{
						new AttributeMod(AttributeModType.BaseDamage, 25)
				})
			}
		);
		
		ability_crippleSlash = instance.new Ability(
			"Lightning Slash",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 15, ElementType.Lightning),
				new CombatEffect_Status(3, StatusType.Cripple)
			},
			0.9f,
			HitSelectionType.HitAll, 0, 1, 1
		);
		
		//Melee in all four directions
		ability_waterWheel = instance.new Ability(
			"Water Wheel",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, true, 0, ElementType.Water)
			},
			1f,
			HitSelectionType.HitAll, 1, 0, 0
		);
		
		ability_trainingBoulder = instance.new Ability(
			"Training Boulder",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 30, ElementType.Earth)
			},
			1.25f,
			HitSelectionType.HitAll, 0, 1, 2
		);
		
		ability_hunkerDown = instance.new Ability(
			"Hunker Down",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Buff(4, new AttributeMod[]{
					new AttributeMod(AttributeModType.BaseArmor, 25)
				}),
				new CombatEffect_Debuff(4, new AttributeMod[]{
					new AttributeMod(AttributeModType.TilePenalty, 1)
				})
			},
			2f,
			HitSelectionType.HitOnlySelf, 0, 0, 0
		);
		
		
		//NINJA
		
		ability_tilePenaltyRedux = instance.new Ability(
			"Quick Bootsies",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Buff(-1, new AttributeMod[]{
					new AttributeMod(AttributeModType.TilePenalty, -1)
				})
			}
		);
		
		ability_blind = instance.new Ability(
			"Pocket Sand",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Status(4, StatusType.Blind)
			},
			1f,
			HitSelectionType.HitAll, 0, 1, 1
		);
		
		ability_autoDodgeProj = instance.new Ability(
			"6th Sense",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_AutoDodgeProj()
			}
		);
		
		ability_darkness1 = instance.new Ability(
			"Leaping Shadow",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 20, ElementType.Darkness)
			},
			1f,
			HitSelectionType.HitOnlyEnemy, 1, 0, 3
		);
		
		//Use poison status
		ability_darkness2 = instance.new Ability(
			"Noxious Aura",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(2, true, 0, ElementType.Darkness)
			},
			1f,
			HitSelectionType.HitAll, 1, 0, 1
		);
		
		ability_water2 = instance.new Ability(
			"Water Jet",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 25, ElementType.Water)
			},
			2f,
			HitSelectionType.HitAll, 0, 1, 2
		);
		
		
		//BANDIT

		ability_daze = instance.new Ability(
			"Concussion Charge",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Status(2, StatusType.Daze)
			},
			1f,
			HitSelectionType.HitAll, 0, 2, 2
		);
		
		ability_rangedCounter = instance.new Ability(
			"Spiteful Aim",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_RangedCounter()
			}
		);
		
		ability_chanceToHitBuff = instance.new Ability(
			"Eagle Eye",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Buff(-1, new AttributeMod[]{
					new AttributeMod(AttributeModType.ChanceToHit, 0.3f)
				})
			}
		);
		
		ability_water1 = instance.new Ability(
			"Stale Canteen",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 10, ElementType.Water)
			},
			1f,
			HitSelectionType.HitAll, 0, 1, 2
		);
		
		ability_wind1 = instance.new Ability(
			"Sailor's Song",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, true, 0, ElementType.Wind)
			},
			1f,
			HitSelectionType.HitAll, 0, 1, 2
		);
		
		ability_wind3 = instance.new Ability(
			"Lacerating Gust",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 30, ElementType.Wind)
			},
			1f,
			HitSelectionType.HitAll, 0, 1, 1
		);
		
		
		//MONK
		
		ability_silence = instance.new Ability(
			"Harmonic Disturbance",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Status(2, StatusType.Silence)
			},
			1f,
			HitSelectionType.HitAll, 0, 1, 3
		);
		
		ability_autoDodgeMelee = instance.new Ability(
			"Auto Dodge Melee",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_AutoDodgeMelee()
			}
		);
		
		ability_statusResistence = instance.new Ability(
			"Charkra Bolster",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Buff(-1, new AttributeMod[]{
					new AttributeMod(AttributeModType.StatusResistance, 0.4f)
				})
			}
		);
		
		ability_heal2 = instance.new Ability(
			"Vital Mantra",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Potion(0, 40, false)
			},
			2f,
			HitSelectionType.HitOnlySelf, 0, 0, 0
		);
		
		ability_holy1 = instance.new Ability(
			"Buddha's Gaze",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, true, 20, ElementType.Holy)
			},
			1.2f,
			HitSelectionType.HitOnlyEnemy, 0, 1, 2
		);
		
		ability_holy2 = instance.new Ability(
			"Mahakala's Wrath",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, true, 40, ElementType.Holy)
			},
			1f,
			HitSelectionType.HitOnlyEnemy, 1, 1, 2
		);
		
		
		//PRIEST
		
		ability_abilityPotency = instance.new Ability(
			"Elemental Tap",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Buff(-1, new AttributeMod[]{
					new AttributeMod(AttributeModType.AbilityPotency, 0.5f)
				})
			}
		);
		
		ability_heal1 = instance.new Ability(
			"Musubi Infusion",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Potion(0, 20, false)
			},
			2f,
			HitSelectionType.HitOnlySelfAndAlly, 1, 0, 2
		);
		
		ability_heal5 = instance.new Ability(
			"Kami Blessing",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Potion(0, 60, false),
				new CombatEffect_Cure(0, StatusType.values())
			},
			1.5f,
			HitSelectionType.HitOnlySelfAndAlly, 0, 1, 3
		);
		
		ability_lightning3 = instance.new Ability(
			"Raijin's Lance",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 35, ElementType.Lightning)
			},
			1f,
			HitSelectionType.HitOnlyEnemy, 1, 0, 3
		);
		
		ability_ice1 = instance.new Ability(
			"Okami Scales",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 20, ElementType.Ice)
			},
			1f,
			HitSelectionType.HitOnlyEnemy, 1, 0, 3
		);
		
		ability_ice3 = instance.new Ability(
			"Divine Avalanche",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 45, ElementType.Ice)
			},
			0.8f,
			HitSelectionType.HitOnlyEnemy, 1, 0, 3
		);
		
		ability_fire3 = instance.new Ability(
			"Kagutsuchi's Rage",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 30, ElementType.Fire)
			},
			1.75f,
			HitSelectionType.HitOnlyEnemy, 2, 0, 3
		);
		
		
		//KAMI - They'll share the same set of abilities for now
		
		ability_heal4 = instance.new Ability(
			"Divine Snoot Boop",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Revive(0, 0.5f)
			},
			2f,
			HitSelectionType.HitOnlySelfAndAlly, 1, 0, 0
		);
		
		ability_holy3 = instance.new Ability(
			"Obliteration",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 60, ElementType.Holy)
			},
			1.2f,
			HitSelectionType.HitAll, 0, 1, 2
		);
		
		ability_lightning2 = instance.new Ability(
			"Heart Shock",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 40, ElementType.Lightning)
			},
			1f,
			HitSelectionType.HitAll, 1, 1, 2
		);
		
		ability_lightning4 = instance.new Ability(
			"Cruel Surge",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 80, ElementType.Lightning)
			},
			1f,
			HitSelectionType.HitAll, 1, 1, 3
		);
		
		ability_ice2 = instance.new Ability(
			"Frost Breath",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 35, ElementType.Ice)
			},
			1f,
			HitSelectionType.HitAll, 1, 1, 3
		);
		
		ability_ice4 = instance.new Ability(
			"Snow Age",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 70, ElementType.Ice)
			},
			1f,
			HitSelectionType.HitAll, 1, 1, 3
		);
		
		//Use poison status
		ability_wind2 = instance.new Ability(
			"Wing's Torrent",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(2, true, 30, ElementType.Wind)
			},
			1f,
			HitSelectionType.HitAll, 1, 1, 3
		);
		
		ability_water3 = instance.new Ability(
			"Geiser",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 50, ElementType.Water)
			},
			1f,
			HitSelectionType.HitAll, 1, 1, 3
		);
		
		ability_water4 = instance.new Ability(
			"Water Bullet",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 75, ElementType.Water)
			},
			1f,
			HitSelectionType.HitAll, 1, 1, 3
		);
		
		ability_clawBite1 = instance.new Ability(
			"Maul",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 30, null)
			},
			1.5f,
			HitSelectionType.HitAll, 0, 1, 1
		);
		
		ability_clawBite3 = instance.new Ability(
			"Gore",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 50, null)
			},
			1.5f,
			HitSelectionType.HitAll, 0, 1, 1
		);
		
		ability_timeSpeedUp = instance.new Ability(
			"Rush",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Buff(0, new AttributeMod[] {
					new AttributeMod(AttributeModType.SequenceShift, -3)
				})
			},
			1.5f,
			HitSelectionType.HitOnlySelf, 0, 0, 0
		);
		
		ability_timeSlowDown = instance.new Ability(
			"Time Vortex",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Buff(0, new AttributeMod[] {
					new AttributeMod(AttributeModType.SequenceShift, 5)
				})
			},
			0.8f,
			HitSelectionType.HitAll, 0, 1, 2
		);
		
		ability_earth1_1 = instance.new Ability(
			"Fling Rock",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 20, ElementType.Earth)
			},
			1f,
			HitSelectionType.HitAll, 0, 1, 2
		);
		
		ability_diamond2 = instance.new Ability(
			"Crystal Shell",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Buff(0, new AttributeMod[] {
					new AttributeMod(AttributeModType.BaseArmor, 20)
				})
			},
			1.5f,
			HitSelectionType.HitOnlySelf, 0, 0, 0
		);
		
		ability_explosion1 = instance.new Ability(
			"Super Heat",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 30, ElementType.Fire)
			},
			1f,
			HitSelectionType.HitAll, 0, 1, 2
		);
		
		ability_explosion2 = instance.new Ability(
			"Sulfer Cloud",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 60, ElementType.Fire)
			},
			0.9f,
			HitSelectionType.HitOnlyEnemy, 1, 2, 3
		);
		
		
		//SURF
		
		ability_chanceToSurvive = instance.new Ability(
			"Soldier's Duty",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_ChanceToSurvive(0.4f)
			}
		);
		
		ability_increaseItemPotency = instance.new Ability(
			"Battlefield Veteran",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Buff(-1, new AttributeMod[] {
					new AttributeMod(AttributeModType.ItemPotency, 0.5f)
				})
			}
		);
		
		ability_fire1 = instance.new Ability(
			"Set Flame",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 15, ElementType.Fire)
			},
			1f,
			HitSelectionType.HitAll, 0, 1, 2
		);
		
		ability_barrage = instance.new Ability(
			"Barrage",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 20, null)
			},
			0.9f,
			HitSelectionType.HitOnlyEnemy, 1, 2, 3
		);
		
		ability_diamond1 = instance.new Ability(
			"Guard",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Buff(3, new AttributeMod[] {
					new AttributeMod(AttributeModType.BaseArmor, 15)
				})
			},
			2f,
			HitSelectionType.HitOnlySelf, 0, 0, 0
		);
		
		ability_cannonFire = instance.new Ability(
			"Cannon Fire",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 40, null)
			},
			0.6f,
			HitSelectionType.HitAll, 2, 3, 4
		);
		
		
		//DIAMYO
		
		ability_goad = instance.new Ability(
			"Insult",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Status(4, StatusType.Goad)
			},
			1.6f,
			HitSelectionType.HitOnlyEnemy, 0, 1, 2
		);
		
		ability_baseArmor = instance.new Ability(
			"Equisite Armaments",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Buff(-1, new AttributeMod[]{
					new AttributeMod(AttributeModType.BaseArmor, 50)
				})
			}
		);
		
		ability_lightning1 = instance.new Ability(
			"Cloud Command",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 20, ElementType.Lightning)
			},
			1.3f,
			HitSelectionType.HitAll, 1, 1, 2
		);
		
		ability_wind4 = instance.new Ability(
			"Captive Kami",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 70, ElementType.Wind)
			},
			1f,
			HitSelectionType.HitOnlyEnemy, 1, 0, 3
		);
		
		ability_commendation = instance.new Ability(
			"Commendation",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Buff(0, new AttributeMod[]{
					new AttributeMod(AttributeModType.SequenceShift, -3)
				})
			},
			2f,
			HitSelectionType.HitOnlySelfAndAlly, 0, 1, 1
		);
		
		ability_diamond3 = instance.new Ability(
			"Ruler's Resources",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Buff(3, new AttributeMod[]{
					new AttributeMod(AttributeModType.BaseArmor, 60)
				})
			},
			2f,
			HitSelectionType.HitOnlySelf, 0, 0, 0
		);
		
		
		//ONI
		
		ability_fear = instance.new Ability(
			"Demoralize",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Status(3, StatusType.Fear)
			},
			0.7f,
			HitSelectionType.HitOnlyEnemy, 0, 1, 1
		);
		
		ability_baseHp = instance.new Ability(
			"Otherworldly Vigor",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Buff(3, new AttributeMod[]{
					new AttributeMod(AttributeModType.BaseDamage, 40)
				})
			},
			2f,
			HitSelectionType.HitOnlySelf, 0, 0, 0
		);
		
		ability_darkness3 = instance.new Ability(
			"Hell's Gate",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 60, ElementType.Darkness)
			},
			1f,
			HitSelectionType.HitOnlyEnemy, 1, 0, 0
		);
		
		ability_fire2 = instance.new Ability(
			"Forge Bellow",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 25, ElementType.Fire)
			},
			1f,
			HitSelectionType.HitAll, 1, 2, 2
		);
		
		//Use poison status
		ability_fire4 = instance.new Ability(
			"Volanic Summoning",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 80, ElementType.Fire)
			},
			0.8f,
			HitSelectionType.HitAll, 2, 3, 4
		);
		
		ability_earth1_2 = instance.new Ability(
			"Exploit Faultline",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 40, ElementType.Earth)
			},
			1f,
			HitSelectionType.HitAll, 0, 1, 2
		);
		
		
		//Nekomata
		
		ability_charm = instance.new Ability(
			"Kitty Cute Cutes",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Status(2, StatusType.Charmed)
			},
			1.1f,
			HitSelectionType.HitOnlyEnemy, 0, 1, 1
		);
		
		ability_chanceToDodge = instance.new Ability(
			"Lithe",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Buff(-1, new AttributeMod[]{
					new AttributeMod(AttributeModType.ChanceToDodge, 0.3f)
				})
			}
		);
		
		ability_heal3 = instance.new Ability(
			"Ancient Medicine",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Potion(0, 50, false)
			},
			2f,
			HitSelectionType.HitAll, 0, 1, 2
		);
		
		ability_plants1 = instance.new Ability(
			"Cultivate",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(2, false, 30, ElementType.Earth)
			},
			1f,
			HitSelectionType.HitOnlyEnemy, 1, 1, 2
		);
		
		//Use poison status
		ability_plants2 = instance.new Ability(
			"Hell Seeds",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, false, 65, ElementType.Earth)
			},
			1.2f,
			HitSelectionType.HitOnlyEnemy, 1, 1, 2
		);
		
		ability_clawBite2 = instance.new Ability(
			"Ferocity",
			"[BLANK]",
			new CombatEffect[] {
				new CombatEffect_Damage(0, true, 0, null)
			},
			2f,
			HitSelectionType.HitOnlyEnemy, 1, 0, 0
		);
		
		
		
		
		abilityTree_ronin = new AbilitiesNode[] {
			instance.new AbilitiesNode(new Ability[] { ability_counterattack }),
			instance.new AbilitiesNode(new Ability[] { ability_baseDamage }),
			instance.new AbilitiesNode(new Ability[] { ability_crippleSlash }),
			instance.new AbilitiesNode(new Ability[] { ability_waterWheel }),
			instance.new AbilitiesNode(new Ability[] { ability_trainingBoulder }),
			instance.new AbilitiesNode(new Ability[] { ability_hunkerDown })
		};
		
		abilityTree_ninja = new AbilitiesNode[] {
			instance.new AbilitiesNode(new Ability[] { ability_tilePenaltyRedux }),
			instance.new AbilitiesNode(new Ability[] { ability_blind }),
			instance.new AbilitiesNode(new Ability[] { ability_autoDodgeProj }),
			instance.new AbilitiesNode(new Ability[] { ability_darkness1 }),
			instance.new AbilitiesNode(new Ability[] { ability_darkness2 }),
			instance.new AbilitiesNode(new Ability[] { ability_water2 })
		};
		
		abilityTree_bandit = new AbilitiesNode[] {
			instance.new AbilitiesNode(new Ability[] { ability_daze }),
			instance.new AbilitiesNode(new Ability[] { ability_rangedCounter }),
			instance.new AbilitiesNode(new Ability[] { ability_chanceToHitBuff }),
			instance.new AbilitiesNode(new Ability[] { ability_water1 }),
			instance.new AbilitiesNode(new Ability[] { ability_wind1 }),
			instance.new AbilitiesNode(new Ability[] { ability_wind3 })
		};
		
		abilityTree_monk = new AbilitiesNode[] {
			instance.new AbilitiesNode(new Ability[] { ability_silence }),
			instance.new AbilitiesNode(new Ability[] { ability_autoDodgeMelee }),
			instance.new AbilitiesNode(new Ability[] { ability_statusResistence }),
			instance.new AbilitiesNode(new Ability[] { ability_heal2 }),
			instance.new AbilitiesNode(new Ability[] { ability_holy1 }),
			instance.new AbilitiesNode(new Ability[] { ability_holy2 })
		};
		
		abilityTree_priest = new AbilitiesNode[] {
			instance.new AbilitiesNode(new Ability[] { ability_abilityPotency }),
			instance.new AbilitiesNode(new Ability[] { ability_ice1 }),
			instance.new AbilitiesNode(new Ability[] { ability_heal1 }),
			instance.new AbilitiesNode(new Ability[] { ability_wind2, ability_fire3 }),
			instance.new AbilitiesNode(new Ability[] { ability_lightning3, ability_ice3 }),
			instance.new AbilitiesNode(new Ability[] { ability_heal5 })
		};
		
		
		//Lightning
		abilityTree_kami_ar = new AbilitiesNode[] {
			instance.new AbilitiesNode(new Ability[] { ability_lightning1 }),
			instance.new AbilitiesNode(new Ability[] { ability_lightning2 }),
			instance.new AbilitiesNode(new Ability[] { ability_lightning3 }),
			instance.new AbilitiesNode(new Ability[] { ability_lightning4 }),
			instance.new AbilitiesNode(new Ability[] { ability_daze }),
			instance.new AbilitiesNode(new Ability[] { ability_chanceToDodge }),
		};
		//Holy Kami
		abilityTree_kami_er = new AbilitiesNode[] {
			instance.new AbilitiesNode(new Ability[] { ability_heal1 }),
			instance.new AbilitiesNode(new Ability[] { ability_heal2 }),
			instance.new AbilitiesNode(new Ability[] { ability_holy1 }),
			instance.new AbilitiesNode(new Ability[] { ability_heal3 }),
			instance.new AbilitiesNode(new Ability[] { ability_holy2 }),
			instance.new AbilitiesNode(new Ability[] { ability_heal4 })
		};
		//Darkness Kami
		abilityTree_kami_ey = new AbilitiesNode[] {
			instance.new AbilitiesNode(new Ability[] { ability_darkness1 }),
			instance.new AbilitiesNode(new Ability[] { ability_darkness2 }),
			instance.new AbilitiesNode(new Ability[] { ability_darkness3 }),
			instance.new AbilitiesNode(new Ability[] { ability_blind }),
			instance.new AbilitiesNode(new Ability[] { ability_chanceToSurvive }),
			instance.new AbilitiesNode(new Ability[] { ability_fear })
		};
		//Earth
		abilityTree_kami_in = new AbilitiesNode[] {
			instance.new AbilitiesNode(new Ability[] { ability_clawBite1 }),
			instance.new AbilitiesNode(new Ability[] { ability_earth1_1 }),
			instance.new AbilitiesNode(new Ability[] { ability_diamond1 }),
			instance.new AbilitiesNode(new Ability[] { ability_diamond2 }),
			instance.new AbilitiesNode(new Ability[] { ability_earth1_2 }),
			instance.new AbilitiesNode(new Ability[] { ability_diamond3 }),
		};
		//Water
		abilityTree_kami_ka = new AbilitiesNode[] {
			instance.new AbilitiesNode(new Ability[] { ability_water1 }),
			instance.new AbilitiesNode(new Ability[] { ability_water2 }),
			instance.new AbilitiesNode(new Ability[] { ability_heal2 }),
			instance.new AbilitiesNode(new Ability[] { ability_water3 }),
			instance.new AbilitiesNode(new Ability[] { ability_heal3 }),
			instance.new AbilitiesNode(new Ability[] { ability_water4 }),
		};
		//Earth2 (Plants)
		abilityTree_kami_ko = new AbilitiesNode[] {
			instance.new AbilitiesNode(new Ability[] { ability_plants1 }),
			instance.new AbilitiesNode(new Ability[] { ability_charm }),
			instance.new AbilitiesNode(new Ability[] { ability_holy1 }),
			instance.new AbilitiesNode(new Ability[] { ability_tilePenaltyRedux }),
			instance.new AbilitiesNode(new Ability[] { ability_holy3 }),
			instance.new AbilitiesNode(new Ability[] { ability_plants2 }),
		};
		//Fire
		abilityTree_kami_ky = new AbilitiesNode[] {
			instance.new AbilitiesNode(new Ability[] { ability_fire1 }),
			instance.new AbilitiesNode(new Ability[] { ability_fire2 }),
			instance.new AbilitiesNode(new Ability[] { ability_explosion1 }),
			instance.new AbilitiesNode(new Ability[] { ability_fire4 }),
			instance.new AbilitiesNode(new Ability[] { ability_fire3 }),
			instance.new AbilitiesNode(new Ability[] { ability_explosion2 }),
		};
		//"Physical Damage" Kami
		abilityTree_kami_oi = new AbilitiesNode[] {
			instance.new AbilitiesNode(new Ability[] { ability_clawBite1 }),
			instance.new AbilitiesNode(new Ability[] { ability_clawBite2 }),
			instance.new AbilitiesNode(new Ability[] { ability_clawBite3 }),
			instance.new AbilitiesNode(new Ability[] { ability_timeSpeedUp }),
			instance.new AbilitiesNode(new Ability[] { ability_goad }),
			instance.new AbilitiesNode(new Ability[] { ability_counterattack }),
		};
		//Ice
		abilityTree_kami_ok = new AbilitiesNode[] {
			instance.new AbilitiesNode(new Ability[] { ability_ice1 }),
			instance.new AbilitiesNode(new Ability[] { ability_wind1 }),
			instance.new AbilitiesNode(new Ability[] { ability_ice2 }),
			instance.new AbilitiesNode(new Ability[] { ability_ice3 }),
			instance.new AbilitiesNode(new Ability[] { ability_wind3 }),
			instance.new AbilitiesNode(new Ability[] { ability_ice4 }),
		};
		//Wind
		abilityTree_kami_wa = new AbilitiesNode[] {
			instance.new AbilitiesNode(new Ability[] { ability_wind1 }),
			instance.new AbilitiesNode(new Ability[] { ability_wind2 }),
			instance.new AbilitiesNode(new Ability[] { ability_autoDodgeProj }),
			instance.new AbilitiesNode(new Ability[] { ability_clawBite3 }),
			instance.new AbilitiesNode(new Ability[] { ability_wind3 }),
			instance.new AbilitiesNode(new Ability[] { ability_wind4 }),
		};
		
		
		abilityTree_surf = new AbilitiesNode[] {
			instance.new AbilitiesNode(new Ability[] { ability_chanceToSurvive }),
			instance.new AbilitiesNode(new Ability[] { ability_increaseItemPotency }),
			instance.new AbilitiesNode(new Ability[] { ability_fire1 }),
			instance.new AbilitiesNode(new Ability[] { ability_barrage }),
			instance.new AbilitiesNode(new Ability[] { ability_diamond1 }),
			instance.new AbilitiesNode(new Ability[] { ability_cannonFire })
		};
		
		abilityTree_diamyo = new AbilitiesNode[] {
			instance.new AbilitiesNode(new Ability[] { ability_goad }),
			instance.new AbilitiesNode(new Ability[] { ability_baseArmor }),
			instance.new AbilitiesNode(new Ability[] { ability_lightning1 }),
			instance.new AbilitiesNode(new Ability[] { ability_wind4 }),
			instance.new AbilitiesNode(new Ability[] { ability_commendation }),
			instance.new AbilitiesNode(new Ability[] { ability_diamond3 })
		};
		
		abilityTree_oni = new AbilitiesNode[] {
			instance.new AbilitiesNode(new Ability[] { ability_fear }),
			instance.new AbilitiesNode(new Ability[] { ability_baseHp }),
			instance.new AbilitiesNode(new Ability[] { ability_darkness3 }),
			instance.new AbilitiesNode(new Ability[] { ability_fire2 }),
			instance.new AbilitiesNode(new Ability[] { ability_fire4 }),
			instance.new AbilitiesNode(new Ability[] { ability_earth1_2 })
		};
		
		abilityTree_nekomata = new AbilitiesNode[] {
			instance.new AbilitiesNode(new Ability[] { ability_charm }),
			instance.new AbilitiesNode(new Ability[] { ability_chanceToDodge }),
			instance.new AbilitiesNode(new Ability[] { ability_heal3 }),
			instance.new AbilitiesNode(new Ability[] { ability_plants1 }),
			instance.new AbilitiesNode(new Ability[] { ability_plants2 }),
			instance.new AbilitiesNode(new Ability[] { ability_clawBite2 })
		};
	}
	
	//Hard-coded Data - End
	
	//Runtime Referencing - Start
	
	public static AbilitiesNode[] GetAbilitiesNodeTree(ClassType classType) {
		AbilitiesNode[] tree = null;
		switch(classType) {
			case RONIN:
				tree = abilityTree_ronin;
				break;
			case NINJA:
				tree = abilityTree_ninja;
				break;
			case MONK:
				tree = abilityTree_monk;
				break;
			case BANDIT:
				tree = abilityTree_bandit;
				break;
			case PRIEST:
				tree = abilityTree_priest;
				break;
			case KAMI_AR:
				tree = abilityTree_kami_ar;
				break;
			case KAMI_ER:
				tree = abilityTree_kami_er;
				break;
			case KAMI_EY:
				tree = abilityTree_kami_ey;
				break;
			case KAMI_IN:
				tree = abilityTree_kami_in;
				break;
			case KAMI_KA:
				tree = abilityTree_kami_ka;
				break;
			case KAMI_KO:
				tree = abilityTree_kami_ko;
				break;
			case KAMI_KY:
				tree = abilityTree_kami_ky;
				break;
			case KAMI_OI:
				tree = abilityTree_kami_oi;
				break;
			case KAMI_OK:
				tree = abilityTree_kami_ok;
				break;
			case KAMI_WA:
				tree = abilityTree_kami_wa;
				break;
			case SURF:
				tree = abilityTree_surf;
				break;
			case DIAMYO:
				tree = abilityTree_diamyo;
				break;
			case ONI:
				tree = abilityTree_oni;
				break;
			case NEKOMATA:
				tree = abilityTree_nekomata;
				break;
			default:
				System.err.println("ClassType.GetAbilityTree() - Add support for: " + classType);
				break;
		}
		return tree;
	}
	
	//Runtime Referencing - End
}
