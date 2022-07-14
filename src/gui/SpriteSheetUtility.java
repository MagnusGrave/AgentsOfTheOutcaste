package gui;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import data.AnimCommand;
import data.AnimCommand.CommandType;
import data.AnimSocketsPack;
import enums.ClassType;
import enums.ColorBlend;
import enums.StatusType;
import enums.WeaponType;
import enums.WorldTileType;

public final class SpriteSheetUtility {
	//Declare all the necessary sheets, whether used internally or externally
	private final static SpriteSheet uiIconSheet = new SpriteSheet(GUIUtil.GetBuffedImage("gui/uiIconsSheet.png"), 8, 4, 0);
	
	private final static SpriteSheet terrainSheet1 = new SpriteSheet(GUIUtil.GetBuffedImage("worldmap/terrainTiles256x384_1.png"), 8, 5, 0);
	private final static SpriteSheet terrainSheet2 = new SpriteSheet(GUIUtil.GetBuffedImage("worldmap/terrainTiles256x384_2.png"), 8, 5, 8);
	private final static SpriteSheet terrainSheetUnder = new SpriteSheet(GUIUtil.GetBuffedImage("worldmap/underTiles.png"), 6, 1, 0);
	
	//Get all separate sprites from sheets
	private final static Ninecon panelBG_paper = new Ninecon(uiIconSheet.GetSprite(0, 2, 2), 20, 20, 20, 20, 4f, null, null);
	public static Ninecon PanelBG_Paper() { return panelBG_paper; }
	public static Ninecon PanelBG_Paper(Color tintColor, ColorBlend colorBlend) { return new Ninecon(uiIconSheet.GetSprite(0, 2, 2), 20, 20, 20, 20, 4f, tintColor, colorBlend); }
	public static final Color PaperBGColor = new Color(236, 194, 139);
	
	private final static BufferedImage paperSprite_NW = uiIconSheet.GetSprite(0, 1, 1);
	private final static BufferedImage paperSprite_NE = uiIconSheet.GetSprite(1, 1, 1);
	private final static BufferedImage paperSprite_SW = uiIconSheet.GetSprite(8, 1, 1);
	private final static BufferedImage paperSprite_SE = uiIconSheet.GetSprite(9, 1, 1);
	private final static BufferedImage paperSprite_N = uiIconSheet.GetSprite(16, 1, 1);
	private final static BufferedImage paperSprite_E = uiIconSheet.GetSprite(17, 1, 1);
	private final static BufferedImage paperSprite_S = uiIconSheet.GetSprite(24, 1, 1);
	private final static BufferedImage paperSprite_W = uiIconSheet.GetSprite(25, 1, 1);
	private final static Ninecon panelBG_paperTiled = new Ninecon(paperSprite_NW, paperSprite_N, paperSprite_NE, paperSprite_E, paperSprite_SE, paperSprite_S, paperSprite_SW, paperSprite_W,
																  PaperBGColor, 6f, null, null);
	public static Ninecon PanelBG_PaperTiled() { return panelBG_paperTiled; }
	public static Ninecon PanelBG_PaperTiled(Color tintColor, ColorBlend colorBlend) {
		return new Ninecon(paperSprite_NW, paperSprite_N, paperSprite_NE, paperSprite_E, paperSprite_SE, paperSprite_S, paperSprite_SW, paperSprite_W,
			  PaperBGColor, 6f, tintColor, colorBlend);
	}
	
	private final static BufferedImage panelBG_stone = uiIconSheet.GetSprite(2, 2, 2);
	public static BufferedImage PanelBG_Stone() { return panelBG_stone; }
	
	private final static Ninecon fieldNinecon = new Ninecon(uiIconSheet.GetSprite(5, 1, 1), 10, 10, 10, 10, 2f, null, null);
	public static Ninecon FieldNinecon() { return fieldNinecon; }
	public static Ninecon FieldNinecon(Color tintColor, ColorBlend colorBlend) { return new Ninecon(uiIconSheet.GetSprite(5, 1, 1), 10, 10, 10, 10, 2f, tintColor, colorBlend); }
	
	private final static Ninecon valueBGNinecon = new Ninecon(uiIconSheet.GetSprite(4, 1, 1), 10, 10, 10, 10, 2f, null, null);
	public static Ninecon ValueBGNinecon() { return valueBGNinecon; }
	public static Ninecon ValueBGNinecon(Color tintColor, ColorBlend colorBlend) { return new Ninecon(uiIconSheet.GetSprite(4, 1, 1), 10, 10, 10, 10, 2f, tintColor, colorBlend); }
	public static final Color ValueBGColor = new Color(240, 240, 240);
	
	private final static Ninecon highlightBGNinecon = new Ninecon(uiIconSheet.GetSprite(20, 1, 1), 10, 10, 10, 10, 2f, null, null);
	public static Ninecon HighlightBGNinecon() { return highlightBGNinecon; }
	public static Ninecon HighlightBGNinecon(Color tintColor, ColorBlend colorBlend) { return new Ninecon(uiIconSheet.GetSprite(20, 1, 1), 10, 10, 10, 10, 2f, tintColor, colorBlend); }
	
	private final static BufferedImage circleBGImage = uiIconSheet.GetSprite(21, 1, 1);
	public static BufferedImage CircleBG() { return circleBGImage; }
	
	private final static Ninecon circularNinecon = new Ninecon(circleBGImage, 15, 15, 15, 15, 4f, null, null);
	public static Ninecon CircularNinecon() { return circularNinecon; }
	public static Ninecon CircularNinecon(Color tintColor, ColorBlend colorBlend) { return new Ninecon(circleBGImage, 15, 15, 15, 15, 4f, tintColor, colorBlend); }

	
	private final static BufferedImage buttonBG_bevelBoxUp = uiIconSheet.GetSprite(6, 1, 1);
	public static BufferedImage buttonBG_BevelBoxUp() { return buttonBG_bevelBoxUp; }
	private final static BufferedImage buttonBG_bevelBoxDown = uiIconSheet.GetSprite(7, 1, 1);
	public static BufferedImage buttonBG_BevelBoxDown() { return buttonBG_bevelBoxDown; }
	
	private final static BufferedImage cloudImage_up = uiIconSheet.GetSprite(30, 2, 1);
	public static BufferedImage CloudImage_Up() { return cloudImage_up; }
	
	private final static BufferedImage cloudImage_down = uiIconSheet.GetSprite(28, 2, 1);
	public static BufferedImage CloudImage_Down() { return cloudImage_down; }

	private final static BufferedImage crossSymbol = uiIconSheet.GetSprite(22, 1, 1);
	public static BufferedImage CrossIcon() { return crossSymbol; }
	
	private final static BufferedImage checkmarkSymbol = uiIconSheet.GetSprite(23, 1, 1);
	public static BufferedImage CheckmarkSymbol() { return checkmarkSymbol; }
	
	private final static BufferedImage leftArrowSymbol = uiIconSheet.GetSprite(12, 1, 1);
	public static BufferedImage LeftArrowSymbol() { return leftArrowSymbol; }
	
	private final static BufferedImage downArrowSymbol = uiIconSheet.GetSprite(13, 1, 1);
	public static BufferedImage DownArrowSymbol() { return downArrowSymbol; }
	
	private final static BufferedImage upArrowSymbol = uiIconSheet.GetSprite(14, 1, 1);
	public static BufferedImage UpArrowSymbol() { return upArrowSymbol; }
	
	private final static BufferedImage rightArrowSymbol = uiIconSheet.GetSprite(15, 1, 1);
	public static BufferedImage RightArrowSymbol() { return rightArrowSymbol; }
	
	
	//private final static BufferedImage scroll_track = SpriteSheetUtility.buttonBG_BevelBoxDown();
	private final static Ninecon scroll_trackNinecon = new Ninecon(uiIconSheet.GetSprite(4, 1, 1), 10, 10, 10, 10, 1f, null, null);
	public static Ninecon Scroll_Track() { return scroll_trackNinecon; }
	//private final static BufferedImage scroll_thumb = SpriteSheetUtility.buttonBG_BevelBoxUp();
	private final static Ninecon scroll_thumbNinecon = new Ninecon(uiIconSheet.GetSprite(20, 1, 1), 10, 10, 10, 10, 1f, null, null);
	public static Ninecon Scroll_Thumb() { return scroll_thumbNinecon; }
	
	//Worldmap Terrain - Start
	
	//terrain groups devided by type
	private final static BufferedImage[] terrainGroup_grass = GetSeries(terrainSheet1, 0, 1, 1, 4);
	private final static BufferedImage[] terrainGroup_water = GetSeries(terrainSheet1, 4, 1, 1, 4);
	private final static BufferedImage[] terrainGroup_mountain = GetSeries(terrainSheet1, 8, 1, 1, 4);
	private final static BufferedImage[] terrainGroup_dune = GetSeries(terrainSheet1, 12, 1, 1, 4);
	private final static BufferedImage[] terrainGroup_forest = GetSeries(terrainSheet1, 16, 1, 1, 4);
	private final static BufferedImage[] terrainGroup_marsh = GetSeries(terrainSheet1, 20, 1, 1, 4);
	private final static BufferedImage[] terrainGroup_barren = GetSeries(terrainSheet1, 24, 1, 1, 4);
	private final static BufferedImage[] terrainGroup_space = GetSeries(terrainSheet1, 28, 1, 1, 4);
	private final static BufferedImage[] terrainGroup_rockyLush = GetSeries(terrainSheet1, 32, 1, 1, 4);
	private final static BufferedImage[] terrainGroup_blank = GetSeries(terrainSheet1, 36, 1, 1, 4);
	
	private final static BufferedImage[] terrainGroup_rockyGrass = GetSeries(terrainSheet2, 0, 1, 1, 4);
	private final static BufferedImage[] terrainGroup_winter = GetSeries(terrainSheet2, 4, 1, 1, 4);
	private final static BufferedImage[] terrainGroup_prarie = GetSeries(terrainSheet2, 8, 1, 1, 4);
	private final static BufferedImage[] terrainGroup_tropical = GetSeries(terrainSheet2, 12, 1, 1, 4);
	private final static BufferedImage[] terrainGroup_bushland = GetSeries(terrainSheet2, 16, 1, 1, 4);
	private final static BufferedImage[] terrainGroup_volcanic = GetSeries(terrainSheet2, 20, 1, 1, 4);
	private final static BufferedImage[] terrainGroup_desert = GetSeries(terrainSheet2, 24, 1, 1, 7);
	
	//Single tiles
	private final static BufferedImage terrain_farmland = terrainSheet2.GetSprite(31, 1, 1);
	
	//Weapon Combat Sprites
	private static Map<AnimSubset, List<HandSocket>> animFrameSocketMap = new HashMap<AnimSubset, List<HandSocket>>();
	
	
	public class TypedTerrain {
		WorldTileType tileType;
		BufferedImage[] terrainGroup;
		public TypedTerrain(WorldTileType tileType, BufferedImage[]  terrainGroup) {
			this.tileType = tileType;
			this.terrainGroup = terrainGroup;
		}
	}
	private static List<TypedTerrain> typedTerrains;
	
	
	public enum StatusStateType {
		Status,
		Buff,
		Debuff,
		Cure
	}
	
	private final static SpriteSheet statusStatesSheet = new SpriteSheet(GUIUtil.GetBuffedImage("effects/statesV2.png"), 3, 12, 0);
	//StatusStateType.Status
	private static BufferedImage[] poisonStateFrames = GetSeries(statusStatesSheet, 0, 1, 1, 3);
	private static BufferedImage[] blindStateFrames = GetSeries(statusStatesSheet, 3, 1, 1, 3);
	private static BufferedImage[] silenceStateFrames = GetSeries(statusStatesSheet, 6, 1, 1, 3);
	private static BufferedImage[] goadStateFrames = GetSeries(statusStatesSheet, 9, 1, 1, 3);
	private static BufferedImage[] dazeStateFrames = GetSeries(statusStatesSheet, 12, 1, 1, 3);
	private static BufferedImage[] charmStateFrames = GetSeries(statusStatesSheet, 15, 1, 1, 3);
	private static BufferedImage[] crippleStateFrames = GetSeries(statusStatesSheet, 18, 1, 1, 3);
	private static BufferedImage[] fearStateFrames = GetSeries(statusStatesSheet, 21, 1, 1, 3);
	private static BufferedImage[] accelerateStateFrames = GetSeries(statusStatesSheet, 24, 1, 1, 3);
	private static BufferedImage[] buffStateFrames = GetSeries(statusStatesSheet, 27, 1, 1, 3);
	private static BufferedImage[] debuffStateFrames = GetSeries(statusStatesSheet, 30, 1, 1, 3);
	private static BufferedImage[] cureStateFrames = GetSeries(statusStatesSheet, 33, 1, 1, 3);
	
	public static BufferedImage[] GetEffectState(StatusStateType statusStateType, StatusType statusType) {
		BufferedImage[] stateFrames = null;
		switch(statusStateType) {
			case Status:
				if(statusType == null) {
					System.err.println("SpriteSheetUtility.GetEffectState() - When getting a StatusStateType.Status a StatusType argument must also be supplied! Returning null.");
					break;
				}
				switch(statusType) {
					case Blind:
						stateFrames = blindStateFrames;
						break;
					case Silence:
						stateFrames = silenceStateFrames;
						break;
					case Goad:
						stateFrames = goadStateFrames;
						break;
					case Daze:
						stateFrames = dazeStateFrames;
						break;
					case Charmed:
						stateFrames = charmStateFrames;
						break;
					case Cripple:
						stateFrames = crippleStateFrames;
						break;
					case Fear:
						stateFrames = fearStateFrames;
						break;
					case Accelerated:
						stateFrames = accelerateStateFrames;
						break;
					default:
						System.err.println("SpriteSheetUtility.GetEffectState() - Add support for StatusType: " + statusType);
						break;
				}
				break;
			case Buff:
				stateFrames = buffStateFrames;
				break;
			case Debuff:
				stateFrames = debuffStateFrames;
				break;
			case Cure:
				stateFrames = cureStateFrames;
				break;
			default:
				System.err.println("SpriteSheetUtility.GetEffectState() - Add support for StatusStateType: " + statusStateType);
				break;
		}
		return stateFrames;
	}
	
	static {
		SpriteSheetUtility instance = new SpriteSheetUtility();
		typedTerrains = new ArrayList<TypedTerrain>();
		//Add groups
		typedTerrains.add(instance.new TypedTerrain(WorldTileType.field, terrainGroup_grass));
		typedTerrains.add(instance.new TypedTerrain(WorldTileType.water, terrainGroup_water));
		typedTerrains.add(instance.new TypedTerrain(WorldTileType.peak, terrainGroup_mountain));
		typedTerrains.add(instance.new TypedTerrain(WorldTileType.dunes, terrainGroup_dune));
		typedTerrains.add(instance.new TypedTerrain(WorldTileType.forest, terrainGroup_forest));
		typedTerrains.add(instance.new TypedTerrain(WorldTileType.marsh, terrainGroup_marsh));
		typedTerrains.add(instance.new TypedTerrain(WorldTileType.barrens, terrainGroup_barren));
		typedTerrains.add(instance.new TypedTerrain(WorldTileType.foothills, terrainGroup_rockyLush));
		typedTerrains.add(instance.new TypedTerrain(WorldTileType.blank, terrainGroup_blank));
		typedTerrains.add(instance.new TypedTerrain(WorldTileType.plateau, terrainGroup_rockyGrass));
		typedTerrains.add(instance.new TypedTerrain(WorldTileType.prairie, terrainGroup_prarie));
		typedTerrains.add(instance.new TypedTerrain(WorldTileType.forestEdge, terrainGroup_bushland));
		//Add singles like groups
		typedTerrains.add(instance.new TypedTerrain(WorldTileType.farmland, new BufferedImage[] { terrain_farmland }));
		
		SetupAnimController(instance);
		
		SetupTileSheets();
		
		
		// <- Weapon Combat Sprites -
		
		AnimSubtypeManager.Initialize(instance);
		
		// <- Idle: Generic -
		
		List<HandSocket> socketFrames_idle_generic = new ArrayList<HandSocket>();
		
		
		HandSocket idle_generic_0 = new HandSocket(0, new Point(32, 35));
		
		idle_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), false, 90f));
		idle_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), false,  90f));
		idle_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), false, 90f,
				new AnimCommand[] { new AnimCommand("effects/bow_tucked.png") }		
		));
		idle_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 270f,
				new AnimCommand[] { new AnimCommand("effects/gun.png") }		
		));
		//For unarmed combat
		idle_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_idle_generic.add(idle_generic_0);
		
		
		HandSocket idle_generic_1 = new HandSocket(1, new Point(32, 36));
		
		idle_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), false, 90f));
		idle_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), false, 90f));
		idle_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), false, 90f,
				new AnimCommand[] { new AnimCommand("effects/bow_tucked.png") }		
				));
		idle_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 270f));
		//For unarmed combat
		idle_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_idle_generic.add(idle_generic_1);
		
		
		HandSocket idle_generic_2 = new HandSocket(2, new Point(32, 36));
		
		idle_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), false, 90f));
		idle_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), false, 90f));
		idle_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), false, 90f,
				new AnimCommand[] { new AnimCommand("effects/bow_tucked.png") }		
				));
		idle_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 270f));
		//For unarmed combat
		idle_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_idle_generic.add(idle_generic_2);
		
		
		AnimSubset matchingSubset = AnimSubtypeManager.getAnimSubsets().stream().filter(x -> x.animType == AnimType.Idle && x.animSubtype == AnimSubtype.Generic).findFirst().get();
		animFrameSocketMap.put(matchingSubset, socketFrames_idle_generic);
		
		// - Idle: Generic ->
		
		// <- MainAttack: Generic -
		
		List<HandSocket> socketFrames_mainAttack_generic = new ArrayList<HandSocket>();
		
		HandSocket mainAttack_generic_0 = new HandSocket(3, new Point(36, 31));
		
		mainAttack_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman },
				new Point(5, 11), true, 90f));
		
		socketFrames_mainAttack_generic.add(mainAttack_generic_0);
		
		
		HandSocket mainAttack_generic_1 = new HandSocket(4, new Point(18, 37));
		
		mainAttack_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman },
				new Point(5, 11), true, 315f));
		
		socketFrames_mainAttack_generic.add(mainAttack_generic_1);
		
		
		HandSocket mainAttack_generic_2 = new HandSocket(5, new Point(19, 38));
		
		mainAttack_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman },
				new Point(5, 11), true, 270f));
		
		socketFrames_mainAttack_generic.add(mainAttack_generic_2);
		
		
		matchingSubset = AnimSubtypeManager.getAnimSubsets().stream().filter(x -> x.animType == AnimType.MainAttack && x.animSubtype == AnimSubtype.Generic).findFirst().get();
		//System.out.println("SpriteSheetUtility Static Block - Generic MainAttack AnimSubset: " + matchingSubset);
		
		animFrameSocketMap.put(matchingSubset, socketFrames_mainAttack_generic);
		
		// - MainAttack: Generic ->
		
		// <- MainAttack: DaiKatana varient -
		
		List<WeaponType> weaponTypes = new ArrayList<WeaponType>();
		weaponTypes.add(WeaponType.DaiKatana);
		AnimSubset weaponSpecificVarientSubset1 = instance.new AnimSubset(AnimType.MainAttack, AnimType.MainAttack_Vr1, weaponTypes);
		AnimSubtypeManager.AddVarientSubset(weaponSpecificVarientSubset1);
		
		List<HandSocket> socketFrames_mainAttack_varient1 = new ArrayList<HandSocket>();
		
		
		HandSocket mainAttack_varient1_0 = new HandSocket(15, new Point(32, 28));
		
		mainAttack_varient1_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), false, 320f));
		
		socketFrames_mainAttack_varient1.add(mainAttack_varient1_0);
		
		
		HandSocket mainAttack_varient1_1 = new HandSocket(4, new Point(18, 36));
		
		mainAttack_varient1_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), true, 0f));
		
		socketFrames_mainAttack_varient1.add(mainAttack_varient1_1);
		
		
		HandSocket mainAttack_varient1_2 = new HandSocket(48, new Point(29, 36));
		
		mainAttack_varient1_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), true, 180f));
		
		socketFrames_mainAttack_varient1.add(mainAttack_varient1_2);
		
		
		HandSocket mainAttack_varient1_3 = new HandSocket(17, new Point(32, 22));
		
		mainAttack_varient1_3.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), true, 90f));
		
		socketFrames_mainAttack_varient1.add(mainAttack_varient1_3);
		
		
		HandSocket mainAttack_varient1_4 = new HandSocket(24, new Point(29, 38));
		
		mainAttack_varient1_4.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), true, 270f));
		
		socketFrames_mainAttack_varient1.add(mainAttack_varient1_4);
		
		
		animFrameSocketMap.put(weaponSpecificVarientSubset1, socketFrames_mainAttack_varient1);
		
		// - MainAttack: DaiKatana varient ->
		
		
		// <- MainAttack: Bow varient -
		
		weaponTypes = new ArrayList<WeaponType>();
		weaponTypes.add(WeaponType.Bow);
		AnimSubset weaponSpecificVarientSubset2 = instance.new AnimSubset(AnimType.MainAttack, AnimType.Bow, weaponTypes);
		AnimSubtypeManager.AddVarientSubset(weaponSpecificVarientSubset2);
		
		List<HandSocket> socketFrames_mainAttack_varient2 = new ArrayList<HandSocket>();
		
		
		HandSocket mainAttack_varient2_0 = new HandSocket(21, new Point(11, 28));
		
		mainAttack_varient2_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(10, 2), true, 315f,
				new AnimCommand[] { new AnimCommand("effects/bow_drawn.png") } //Commands: This frame need to use a custom sprite showing the bow string drawn back
		));
		
		socketFrames_mainAttack_varient2.add(mainAttack_varient2_0);
		
		
		HandSocket mainAttack_varient2_1 = new HandSocket(22, new Point(11, 28));
		
		mainAttack_varient2_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(10, 2), true, 315f,
				new AnimCommand[] { new AnimCommand("effects/bow_drawn.png") }
		));
		
		socketFrames_mainAttack_varient2.add(mainAttack_varient2_1);
		
		
		HandSocket mainAttack_varient2_2 = new HandSocket(23, new Point(12, 29));
		
		mainAttack_varient2_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(11, 2), true, 315f,
				new AnimCommand[] { new AnimCommand("effects/bow.png") }		
		));
		
		socketFrames_mainAttack_varient2.add(mainAttack_varient2_2);
		
		
		animFrameSocketMap.put(weaponSpecificVarientSubset2, socketFrames_mainAttack_varient2);
		
		// - MainAttack: Bow varient ->
		
		
		// <- MainAttack: Shoot varient -
		
		weaponTypes = new ArrayList<WeaponType>();
		weaponTypes.add(WeaponType.Gun);
		AnimSubset weaponSpecificVarientSubset3 = instance.new AnimSubset(AnimType.MainAttack, AnimType.Shoot, weaponTypes);
		AnimSubtypeManager.AddVarientSubset(weaponSpecificVarientSubset3);
		
		List<HandSocket> socketFrames_mainAttack_varient3 = new ArrayList<HandSocket>();
		
		
		HandSocket mainAttack_varient3_0 = new HandSocket(8, new Point(24, 35));
		
		mainAttack_varient3_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f,
				new AnimCommand[] { new AnimCommand("effects/gun_tucked.png") }		
		));
		
		socketFrames_mainAttack_varient3.add(mainAttack_varient3_0);
		
		
		HandSocket mainAttack_varient3_1 = new HandSocket(47, new Point(26, 33));
		
		mainAttack_varient3_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 334f,
				new AnimCommand[] { new AnimCommand("effects/gun_tucked.png") }		
		));
		
		socketFrames_mainAttack_varient3.add(mainAttack_varient3_1);
		
		
		HandSocket mainAttack_varient3_2 = new HandSocket(47, new Point(26, 33));
		
		mainAttack_varient3_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 334f,
				new AnimCommand[] { new AnimCommand("effects/gun_tucked.png") }		
		));
		
		socketFrames_mainAttack_varient3.add(mainAttack_varient3_2);
		
		HandSocket mainAttack_varient3_3 = new HandSocket(8, new Point(24, 35));
		
		mainAttack_varient3_3.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f,
				new AnimCommand[] { new AnimCommand("effects/gun_tucked.png") }		
		));
		
		socketFrames_mainAttack_varient3.add(mainAttack_varient3_3);
		
		
		animFrameSocketMap.put(weaponSpecificVarientSubset3, socketFrames_mainAttack_varient3);
		
		// - MainAttack: Shoot varient ->
		
		
		// <- MainAttack: Throw varient -
		
		weaponTypes = new ArrayList<WeaponType>();
		weaponTypes.add(WeaponType.ThrowingKnife);
		weaponTypes.add(WeaponType.Kunai);
		weaponTypes.add(WeaponType.Kusarigama);
		weaponTypes.add(WeaponType.Talisman);
		weaponTypes.add(WeaponType.Shuriken);
		AnimSubset weaponSpecificVarientSubset4 = instance.new AnimSubset(AnimType.MainAttack, AnimType.Throw, weaponTypes);
		AnimSubtypeManager.AddVarientSubset(weaponSpecificVarientSubset4);
		
		List<HandSocket> socketFrames_mainAttack_varient4 = new ArrayList<HandSocket>();
		
		
		HandSocket mainAttack_varient4_0 = new HandSocket(23, new Point(34, 25));
		
		mainAttack_varient4_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), false, 0f));
		
		socketFrames_mainAttack_varient4.add(mainAttack_varient4_0);
		
		
		HandSocket mainAttack_varient4_1 = new HandSocket(23, new Point(34, 25));
		
		mainAttack_varient4_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), false, 0f));
		
		socketFrames_mainAttack_varient4.add(mainAttack_varient4_1);
		
		
		HandSocket mainAttack_varient4_2 = new HandSocket(4, new Point(7, 29));
		
		mainAttack_varient4_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), false, 225f));
		
		socketFrames_mainAttack_varient4.add(mainAttack_varient4_2);
		
		//Testing projectile's travel to the defender
		HandSocket mainAttack_varient4_3 = new HandSocket(4, new Point(-20, 29));
		
		mainAttack_varient4_3.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), false, 225f));
		
		socketFrames_mainAttack_varient4.add(mainAttack_varient4_3);
		
		HandSocket mainAttack_varient4_4 = new HandSocket(4, new Point(-47, 29));
		
		mainAttack_varient4_4.weaponSockets.add(
			new WeaponSocket(
				new WeaponType[] {
					WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken
				},
				new Point(5, 11), false, 225f,
				new AnimCommand[] { new AnimCommand(CommandType.Hide) } //commands
			)
		);
		
		socketFrames_mainAttack_varient4.add(mainAttack_varient4_4);
		
		
		animFrameSocketMap.put(weaponSpecificVarientSubset4, socketFrames_mainAttack_varient4);
		
		// - MainAttack: Throw varient ->
		
		// <- Flinch: Generic -
		
		List<HandSocket> socketFrames_flinch_generic = new ArrayList<HandSocket>();
		
		
		HandSocket flinch_generic_0 = new HandSocket(36, new Point(34, 33));
		
		flinch_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), false, 0f));
		flinch_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), false,  0f));
		flinch_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), false, 90f,
				new AnimCommand[] { new AnimCommand("effects/bow_tucked.png") }		
		));
		flinch_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f));
		//For unarmed combat
		flinch_generic_0.weaponSockets.add(new WeaponSocket(
						new WeaponType[] { null },
						new Point(0, 0), false, 0f	
				)); 
		
		socketFrames_flinch_generic.add(flinch_generic_0);
		
		
		HandSocket flinch_generic_1 = new HandSocket(37, new Point(36, 32));
		
		flinch_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), false, 0f));
		flinch_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), false, 0f));
		flinch_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), false, 90f,
				new AnimCommand[] { new AnimCommand("effects/bow_tucked.png") }		
		));
		flinch_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f));
		//For unarmed combat
		flinch_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_flinch_generic.add(flinch_generic_1);
		
		
		HandSocket flinch_generic_2 = new HandSocket(38, new Point(31, 35));
		
		flinch_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), false, 0f));
		flinch_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), false, 0f));
		flinch_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), false, 90f,
				new AnimCommand[] { new AnimCommand("effects/bow_tucked.png") }		
		));
		flinch_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f));
		//For unarmed combat
		flinch_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_flinch_generic.add(flinch_generic_2);
		
		
		matchingSubset = AnimSubtypeManager.getAnimSubsets().stream().filter(x -> x.animType == AnimType.Flinch && x.animSubtype == AnimSubtype.Generic).findFirst().get();
		animFrameSocketMap.put(matchingSubset, socketFrames_flinch_generic);
		
		// - Flinch: Generic ->
		
		// <- Crouch: Generic -
		
		List<HandSocket> socketFrames_crouch_generic = new ArrayList<HandSocket>();
		
		
		HandSocket crouch_generic_0 = new HandSocket(24, new Point(31, 38));
		
		crouch_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), true, 225f));
		crouch_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), true,  225f));
		crouch_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), true, 225f,
				new AnimCommand[] { new AnimCommand("effects/bow_tucked.png") }
		));
		crouch_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f));
		//For unarmed combat
		crouch_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_crouch_generic.add(crouch_generic_0);
		
		
		HandSocket crouch_generic_1 = new HandSocket(25, new Point(31, 39));
		
		crouch_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), true, 270f));
		crouch_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), true, 270f));
		crouch_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), true, 225f));
		crouch_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f));
		//For unarmed combat
		crouch_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_crouch_generic.add(crouch_generic_1);
		
		
		HandSocket crouch_generic_2 = new HandSocket(26, new Point(31, 39));
		
		crouch_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), true, 270f));
		crouch_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), true, 270f));
		crouch_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), true, 225f));
		crouch_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f));
		//For unarmed combat
		crouch_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_crouch_generic.add(crouch_generic_2);
		
		
		matchingSubset = AnimSubtypeManager.getAnimSubsets().stream().filter(x -> x.animType == AnimType.Crouch && x.animSubtype == AnimSubtype.Generic).findFirst().get();
		animFrameSocketMap.put(matchingSubset, socketFrames_crouch_generic);
		
		// - Crouch: Generic ->
		
		// <- Dead: Generic -
		
		List<HandSocket> socketFrames_dead_generic = new ArrayList<HandSocket>();
		
		
		HandSocket dead_generic_0 = new HandSocket(48, new Point(30, 38));
		
		dead_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), false, 270f));
		dead_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), false,  270f));
		dead_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), true, 45f));
		dead_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f));
		//For unarmed combat
		dead_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_dead_generic.add(dead_generic_0);
		
		
		HandSocket dead_generic_1 = new HandSocket(49, new Point(29, 38));
		
		dead_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), false, 270f));
		dead_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), false, 270f));
		dead_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), true, 45f));
		dead_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f));
		//For unarmed combat
		dead_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_dead_generic.add(dead_generic_1);
		
		
		HandSocket dead_generic_2 = new HandSocket(43, new Point(31, 39));
		
		dead_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), false, 270f));
		dead_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), false, 270f));
		dead_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), true, 45f));
		dead_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f));
		//For unarmed combat
		dead_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_dead_generic.add(dead_generic_2);
		
		
		HandSocket dead_generic_3 = new HandSocket(51, new Point(32, 41));
		
		dead_generic_3.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), true, 125f));
		dead_generic_3.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), true, 125f));
		dead_generic_3.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), true, 45f));
		dead_generic_3.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f));
		//For unarmed combat
		dead_generic_3.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_dead_generic.add(dead_generic_3);
		
		HandSocket dead_generic_4 = new HandSocket(51, new Point(32, 41));
		
		dead_generic_4.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), true, 125f));
		dead_generic_4.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), true, 125f));
		dead_generic_4.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), true, 45f));
		dead_generic_4.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f));
		//For unarmed combat
		dead_generic_4.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_dead_generic.add(dead_generic_4);
		
		HandSocket dead_generic_5 = new HandSocket(52, new Point(32, 41));
		
		dead_generic_5.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), true, 125f));
		dead_generic_5.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), true, 125f));
		dead_generic_5.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), true, 45f));
		dead_generic_5.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f));
		//For unarmed combat
		dead_generic_5.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_dead_generic.add(dead_generic_5);
		
		HandSocket dead_generic_6 = new HandSocket(53, new Point(32, 41));
		
		dead_generic_6.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), true, 125f));
		dead_generic_6.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), true, 125f));
		dead_generic_6.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), true, 45f));
		dead_generic_6.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f));
		//For unarmed combat
		dead_generic_6.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_dead_generic.add(dead_generic_6);
		
		
		matchingSubset = AnimSubtypeManager.getAnimSubsets().stream().filter(x -> x.animType == AnimType.Dead && x.animSubtype == AnimSubtype.Generic).findFirst().get();
		animFrameSocketMap.put(matchingSubset, socketFrames_dead_generic);
		
		// - Dead: Generic ->
		
		// <- DeadIdle: Generic -
		
		List<HandSocket> socketFrames_deadIdle_generic = new ArrayList<HandSocket>();
		
		HandSocket deadIdle_generic_0 = new HandSocket(51, new Point(32, 41));
		
		deadIdle_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), true, 125f));
		deadIdle_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), true, 125f));
		deadIdle_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), true, 45f));
		deadIdle_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f));
		//For unarmed combat
		deadIdle_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_deadIdle_generic.add(deadIdle_generic_0);
		
		HandSocket deadIdle_generic_1 = new HandSocket(52, new Point(32, 41));
		
		deadIdle_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), true, 125f));
		deadIdle_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), true, 125f));
		deadIdle_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), true, 45f));
		deadIdle_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f));
		//For unarmed combat
		deadIdle_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_deadIdle_generic.add(deadIdle_generic_1);
		
		HandSocket deadIdle_generic_2 = new HandSocket(53, new Point(32, 41));
		
		deadIdle_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), true, 125f));
		deadIdle_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), true, 125f));
		deadIdle_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), true, 45f));
		deadIdle_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f));
		//For unarmed combat
		deadIdle_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_deadIdle_generic.add(deadIdle_generic_2);
		
		
		matchingSubset = AnimSubtypeManager.getAnimSubsets().stream().filter(x -> x.animType == AnimType.DeadIdle && x.animSubtype == AnimSubtype.Generic).findFirst().get();
		animFrameSocketMap.put(matchingSubset, socketFrames_deadIdle_generic);
		
		// - DeadIdle: Generic ->
		
		// <- Cast: Generic -
		
		List<HandSocket> socketFrames_cast_generic = new ArrayList<HandSocket>();
		
		
		HandSocket cast_generic_0 = new HandSocket(15, new Point(34, 33));
		
		cast_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), false, 0f,
				new AnimCommand[] { new AnimCommand(CommandType.Hide) }	));
		cast_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), false,  0f,
				new AnimCommand[] { new AnimCommand(CommandType.Hide) }	));
		cast_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), false, 90f,
				new AnimCommand[] { new AnimCommand(CommandType.Hide) }	));
		cast_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f,
				new AnimCommand[] { new AnimCommand(CommandType.Hide) }	));
		//For unarmed combat
		cast_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_cast_generic.add(cast_generic_0);
		
		
		HandSocket cast_generic_1 = new HandSocket(16, new Point(36, 32));
		
		cast_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), false, 0f));
		cast_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), false, 0f));
		cast_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), false, 90f));
		cast_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f));
		//For unarmed combat
		cast_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_cast_generic.add(cast_generic_1);
		
		
		HandSocket cast_generic_2 = new HandSocket(17, new Point(31, 35));
		
		cast_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), false, 0f));
		cast_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), false, 0f));
		cast_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), false, 90f));
		cast_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f));
		//For unarmed combat
		cast_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_cast_generic.add(cast_generic_2);
		
		
		HandSocket cast_generic_3 = new HandSocket(18, new Point(31, 35));
		
		cast_generic_3.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), false, 0f));
		cast_generic_3.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), false, 0f));
		cast_generic_3.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), false, 90f));
		cast_generic_3.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f));
		//For unarmed combat
		cast_generic_3.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_cast_generic.add(cast_generic_3);
		
		HandSocket cast_generic_4 = new HandSocket(19, new Point(31, 35));
		
		cast_generic_4.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), false, 0f));
		cast_generic_4.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), false, 0f));
		cast_generic_4.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), false, 90f));
		cast_generic_4.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f));
		//For unarmed combat
		cast_generic_4.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_cast_generic.add(cast_generic_4);
		
		
		HandSocket cast_generic_5 = new HandSocket(20, new Point(31, 35));
		
		cast_generic_5.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), false, 0f));
		cast_generic_5.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), false, 0f));
		cast_generic_5.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), false, 90f));
		cast_generic_5.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f));
		//For unarmed combat
		cast_generic_5.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_cast_generic.add(cast_generic_5);
		
		
		matchingSubset = AnimSubtypeManager.getAnimSubsets().stream().filter(x -> x.animType == AnimType.Cast && x.animSubtype == AnimSubtype.Generic).findFirst().get();
		animFrameSocketMap.put(matchingSubset, socketFrames_cast_generic);
		
		// - Cast: Generic ->
		
		// <- UseItem: Generic -
		
		List<HandSocket> socketFrames_useItem_generic = new ArrayList<HandSocket>();
		
		
		HandSocket useItem_generic_0 = new HandSocket(48, new Point(34, 33));
		
		useItem_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), false, 0f,
				new AnimCommand[] { new AnimCommand(CommandType.Hide) }	));
		useItem_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), false,  0f,
				new AnimCommand[] { new AnimCommand(CommandType.Hide) }	));
		useItem_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), false, 90f,
				new AnimCommand[] { new AnimCommand(CommandType.Hide) }	));
		useItem_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f,
				new AnimCommand[] { new AnimCommand(CommandType.Hide) }	));
		//For unarmed combat
		useItem_generic_0.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_useItem_generic.add(useItem_generic_0);
		
		
		HandSocket useItem_generic_1 = new HandSocket(49, new Point(36, 32));
		
		useItem_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), false, 0f));
		useItem_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), false, 0f));
		useItem_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), false, 90f));
		useItem_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f));
		//For unarmed combat
		useItem_generic_1.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_useItem_generic.add(useItem_generic_1);
		
		
		HandSocket useItem_generic_2 = new HandSocket(50, new Point(31, 35));
		
		useItem_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] {
						WeaponType.WoodenSword, WeaponType.Club, WeaponType.Jitte, WeaponType.Fan, WeaponType.Stave,
						WeaponType.Tanto, WeaponType.Kodachi, WeaponType.Katana, WeaponType.Ninjato,
						WeaponType.BranchSword,
						WeaponType.Spear, WeaponType.Naginata,
						WeaponType.ThrowingKnife, WeaponType.Kunai, WeaponType.Kusarigama, WeaponType.Talisman, WeaponType.Shuriken },
				new Point(5, 11), false, 0f));
		useItem_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.DaiKatana },
				new Point(5, 13), false, 0f));
		useItem_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Bow },
				new Point(12, 4), false, 90f));
		useItem_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { WeaponType.Gun },
				new Point(5, 12), true, 315f));
		//For unarmed combat
		useItem_generic_2.weaponSockets.add(new WeaponSocket(
				new WeaponType[] { null },
				new Point(0, 0), false, 0f	
		)); 
		
		socketFrames_useItem_generic.add(useItem_generic_2);
		
		
		matchingSubset = AnimSubtypeManager.getAnimSubsets().stream().filter(x -> x.animType == AnimType.UseItem && x.animSubtype == AnimSubtype.Generic).findFirst().get();
		animFrameSocketMap.put(matchingSubset, socketFrames_useItem_generic);
		
		// - UseItem: Generic ->
		
		
		//TODO FILL OUT THE REST OF THE ANIMS THAT NEED WEAPON SOCKETS
		
		
		
		// - Weapon Combat Sprites ->
	}
	
	//Tile underside
	private final static BufferedImage terrain_under_ground = terrainSheetUnder.GetSprite(0, 1, 1);
	private final static BufferedImage terrain_under_water = terrainSheetUnder.GetSprite(1, 1, 1);
	private final static BufferedImage[] terrainGroup_under_space = GetSeries(terrainSheetUnder, 2, 1, 1, 4);
	
	public static BufferedImage[] getTerraingroupGrass() {
		return terrainGroup_grass;
	}

	public static BufferedImage[] getTerraingroupWater() {
		return terrainGroup_water;
	}

	public static BufferedImage[] getTerraingroupMountain() {
		return terrainGroup_mountain;
	}

	public static BufferedImage[] getTerraingroupDune() {
		return terrainGroup_dune;
	}

	public static BufferedImage[] getTerraingroupForest() {
		return terrainGroup_forest;
	}

	public static BufferedImage[] getTerraingroupMarsh() {
		return terrainGroup_marsh;
	}

	public static BufferedImage[] getTerraingroupBarren() {
		return terrainGroup_barren;
	}

	public static BufferedImage[] getTerraingroupSpace() {
		return terrainGroup_space;
	}

	public static BufferedImage[] getTerraingroupRockylush() {
		return terrainGroup_rockyLush;
	}

	public static BufferedImage[] getTerraingroupBlank() {
		return terrainGroup_blank;
	}

	public static BufferedImage[] getTerraingroupRockygrass() {
		return terrainGroup_rockyGrass;
	}

	public static BufferedImage[] getTerraingroupWinter() {
		return terrainGroup_winter;
	}

	public static BufferedImage[] getTerraingroupPrarie() {
		return terrainGroup_prarie;
	}

	public static BufferedImage[] getTerraingroupTropical() {
		return terrainGroup_tropical;
	}

	public static BufferedImage[] getTerraingroupBushland() {
		return terrainGroup_bushland;
	}

	public static BufferedImage[] getTerraingroupVolcanic() {
		return terrainGroup_volcanic;
	}

	public static BufferedImage[] getTerraingroupDesert() {
		return terrainGroup_desert;
	}

	public static BufferedImage getTerrainFarmland() {
		return terrain_farmland;
	}

	public static BufferedImage getTerrainUnderGround() {
		return terrain_under_ground;
	}

	public static BufferedImage getTerrainUnderWater() {
		return terrain_under_water;
	}

	public static BufferedImage[] getTerraingroupUnderSpace() {
		return terrainGroup_under_space;
	}
	
	public static BufferedImage[] GetTerrainFromWorldTile(WorldTileType tileType) {
		Optional<TypedTerrain> opt = typedTerrains.stream().filter(x -> x.tileType == tileType).findFirst();
		TypedTerrain typedTerrain = opt.isPresent() ? opt.get() : null;
		if(typedTerrain != null) {
			return typedTerrain.terrainGroup;
		} else {
			System.err.println("SpriteSheetUtility.GetTerrainFromWorldTile() - Couldn't find TypedTerrain for WorldTileType: " + tileType.toString());
			return terrainGroup_space;
		}
	}
	
	private final static Random r = new Random();
	public static BufferedImage GetRandomTerrainFromWorldTile(WorldTileType tileType) {
		return GetTerrainFromWorldTile(tileType)[r.nextInt(GetTerrainFromWorldTile(tileType).length)];
	}
	
	//Worldmap Terrain - End
	
	private static BufferedImage[] GetSeries(SpriteSheet sheet, int startIndex, int cellCountWidth, int cellCountHeight, int seriesCount) {
		BufferedImage[] images = new BufferedImage[seriesCount];
		for(int i = 0; i < seriesCount; i++) {
			images[i] = sheet.GetSprite(startIndex + i, cellCountWidth, cellCountHeight);
		}
		return images;
	}
	
	public static BufferedImage[] GetRangedEffectSheetFrames(String sheetFilePath, int frameStartIndex, int frameEndIndex) {
		SpriteSheet spriteSheet = SpriteSheetUtility.GetEffectSheet(sheetFilePath);
		String[] splits = sheetFilePath.split("/");
		BufferedImage[] frames = new BufferedImage[frameEndIndex - frameStartIndex + 1];
		for(int i = frameStartIndex; i <= frameEndIndex; i++) {
			String frameName = splits[splits.length-1].split("\\.")[0] + "_" + i;
			frames[i-frameStartIndex] = spriteSheet.GetSprite(frameName);
		}
		return frames;
	}
	
	//Character Images - Start
	
	public class SequenceData {
		List<Integer> frameSequence = new ArrayList<Integer>();
		public SequenceData(int startIndex, int count) {
			for(int i = startIndex; i < startIndex + count; i++)
				frameSequence.add(new Integer(i));
		}
		public SequenceData(int[] sequenceArray) {
			for(int index : sequenceArray)
				frameSequence.add(new Integer(index));
		}
		public SequenceData(int frameIndex) {
			frameSequence.add(new Integer(frameIndex));
		}
		
		public void AddFrames(int startIndex, int count) {
			for(int i = startIndex; i < startIndex + count; i++)
				frameSequence.add(new Integer(i));
		}
		public void AddFrames(int[] sequenceArray) {
			for(int index : sequenceArray)
				frameSequence.add(new Integer(index));
		}
		public void AddFrame(int frameIndex) {
			frameSequence.add(new Integer(frameIndex));
		}
	}
	
	private static Map<ClassType, SpriteSheet> characterBattleSheets;
	public class AnimController {
		private ClassType classType;
		private Map<AnimType, SequenceData> anims = new HashMap<AnimType, SequenceData>();

		public AnimController(ClassType classType) {
			this.classType = classType;
		}
		private void AddAnim(AnimType animType, SequenceData sequenceData) {
			anims.put(animType, sequenceData);
		}
		
		public BufferedImage[] GetAnimFrames(AnimType animType) {
			List<BufferedImage> frames = new ArrayList<BufferedImage>();
			for(Integer integerIndex : anims.get(animType).frameSequence)
				frames.add( characterBattleSheets.get(classType).GetSprite((int)integerIndex, 1, 1) );
			return frames.stream().toArray(BufferedImage[]::new);
		}
	}
	
	public static BufferedImage GetSpriteByIndex(ClassType classType, int sheetIndex) {
		return characterBattleSheets.get(classType).GetSprite(sheetIndex, 1, 1);
	}
	
	private static List<AnimController> animControllers; //= new ArrayList<AnimController>();
	public static AnimController GetAnimController(ClassType classType) {
		Optional<AnimController> controllerOp = animControllers.stream().filter(x -> x.classType == classType).findFirst();
		if(controllerOp.isPresent())
			return controllerOp.get();
		else {
			System.err.println("Anim Controller - can't find AnimController for class: " + classType.toString());
			return null;
		}
	}
	
	private static Map<ClassType, SpriteSheet> characterWalkSheets;
	public static SpriteSheet GetWalkSheet(ClassType classType) {
		return characterWalkSheets.get(classType);
	}
	
	/**
	 * May contain any or all of the characterWalkSheets, the assortedSheets holds the SpriteSheet for every request made by ActorPaths being loaded into the scene
	 * which could contain classed and non-classed characters 
	 */
	private static Map<String, SpriteSheet> assortedActorSheets = new HashMap<String, SpriteSheet>();
	public static SpriteSheet GetActorSheet(String filePath) {
		//Try to the serve up the already-instantiated SpriteSheet to save on memory
		if(assortedActorSheets.containsKey(filePath))
			return assortedActorSheets.get(filePath);
		
		BufferedImage sheetImage = GUIUtil.GetBuffedImage(filePath);
		if(sheetImage != null) {
			SpriteSheet spriteSheet = new SpriteSheet(sheetImage, 3, 4, 0);
			assortedActorSheets.put(filePath, spriteSheet);	
			return spriteSheet;
		} else {
			System.err.println("SpriteSheetUtility.GetActorSheet() - Can't find a non-class Sheet with matching path: " + filePath);
			return null;
		}
	}
	
	private static Map<ClassType, BufferedImage> deadSpritesByClass;
	public static BufferedImage GetDeadStateSprite(ClassType classType) {
		return deadSpritesByClass.get(classType);
	}
	
	/*Battle Spritesheet Structure (every three frames)
		Idle, 		MainAttack, 	 Walk,
		IdleSword, 	SecondaryAttack, Cast,
		Praise, 	Bow, 			 Crouch,
		Idle2, 		MainAttack2, 	 Crouch2,
		Flinch, 	Praise2, 		 Crouch3,
		Flinch2, 	UseItem, 		 Dead
	*/
	public enum AnimType { 
		Idle, 		MainAttack, 	 Walk,
		 			SecondaryAttack, Cast,
		 			Bow, 			 Crouch,
		
		Flinch, 	
					UseItem, 		 Dead,
					
		//Custom Anims
		Punch,
		MainAttack_Vr1,
		Shoot, Throw,
		
		DeadIdle //a single frame of them laying on the ground
	};
	private static void SetupAnimController(SpriteSheetUtility instance) {
		characterWalkSheets = new HashMap<ClassType, SpriteSheet>();
		deadSpritesByClass = new HashMap<ClassType, BufferedImage>();
		characterBattleSheets = new HashMap<ClassType, SpriteSheet>();
		animControllers = new ArrayList<AnimController>();
		for(ClassType classType : ClassType.values()) {
			String capitalizedName = classType.toString().toUpperCase();
			
			BufferedImage walkImage = GUIUtil.GetBuffedImage("characters/"+ capitalizedName +"/walk.png");
			if(walkImage != null)
				characterWalkSheets.put(classType, new SpriteSheet(walkImage, 3, 4, 0));
			
			deadSpritesByClass.put(classType, GUIUtil.GetBuffedImage("characters/"+ capitalizedName +"/dead.png"));
			
			BufferedImage battleImage = GUIUtil.GetBuffedImage("characters/"+ capitalizedName +"/battle.png");
			if(battleImage != null) {
				characterBattleSheets.put(classType, new SpriteSheet(battleImage, 9, 6, 0));
				AnimController animController = instance.new AnimController(classType);
				animController.AddAnim(AnimType.Idle, instance.new SequenceData(0, 3));
				animController.AddAnim(AnimType.MainAttack, instance.new SequenceData(3, 3));
				SequenceData walkData = instance.new SequenceData(6, 3);
				walkData.AddFrame(7);
				animController.AddAnim(AnimType.Walk, walkData);
				animController.AddAnim(AnimType.SecondaryAttack, instance.new SequenceData(12, 3));
				animController.AddAnim(AnimType.Cast, instance.new SequenceData(15, 6));
				animController.AddAnim(AnimType.Crouch, instance.new SequenceData(24, 3));
				animController.AddAnim(AnimType.Flinch, instance.new SequenceData(36, 3));
				animController.AddAnim(AnimType.UseItem, instance.new SequenceData(48, 3));
				SequenceData deadData = instance.new SequenceData(48, 2);
				deadData.AddFrames(43, 2);
				deadData.AddFrames(51, 3);
				animController.AddAnim(AnimType.Dead, deadData);
				
				animController.AddAnim(AnimType.DeadIdle, instance.new SequenceData(51, 3));
				
				//MainAttack_Vr2
				animController.AddAnim(AnimType.Bow, instance.new SequenceData(21, 3));
				
				//Custom Anims - Start
				
				//MainAttack_Vr1
				SequenceData mainAttack_Vr1Data = instance.new SequenceData(15, 1);
				mainAttack_Vr1Data.AddFrames(4, 1);
				mainAttack_Vr1Data.AddFrames(48, 1);
				mainAttack_Vr1Data.AddFrames(17, 1);
				mainAttack_Vr1Data.AddFrames(24, 1);
				animController.AddAnim(AnimType.MainAttack_Vr1, mainAttack_Vr1Data);
				
				//MainAttack_Vr3
				SequenceData mainAttack_ShootData = instance.new SequenceData(8, 1);
				mainAttack_ShootData.AddFrames(8, 1);
				mainAttack_ShootData.AddFrames(47, 1);
				mainAttack_ShootData.AddFrames(8, 1);
				animController.AddAnim(AnimType.Shoot, mainAttack_ShootData);
				
				//MainAttack_Vr4
				SequenceData mainAttack_ThrowData = instance.new SequenceData(23, 1);
				mainAttack_ThrowData.AddFrames(23, 1);
				mainAttack_ThrowData.AddFrames(4, 1);
				//The projectile's journey to the defender
				mainAttack_ThrowData.AddFrames(4, 1);
				mainAttack_ThrowData.AddFrames(4, 1);
				animController.AddAnim(AnimType.Throw, mainAttack_ThrowData);
				
				//MainAttack_Vr5
				SequenceData mainAttack_punchData = instance.new SequenceData(8, 1);
				mainAttack_punchData.AddFrames(21, 1);
				mainAttack_punchData.AddFrames(21, 1);
				mainAttack_punchData.AddFrames(38, 1);
				animController.AddAnim(AnimType.Punch, mainAttack_punchData);
				
				//Custom Anims - End
				
				animControllers.add(animController);
			}
		}
	}
	
	//Character Images - End
	
	// <- Weapon Combat Sprites -
	
	public static class HandSocket {
		public HandSocket(int frameIndex, Point relativeHandPos) {
			this.frameIndex = frameIndex;
			this.relativeHandPos = relativeHandPos;
			this.normRelativeHandPos = new Point2D.Float((float)relativeHandPos.x / characterPixelResolution, (float)relativeHandPos.y / characterPixelResolution);
			
			this.weaponSockets = new ArrayList<WeaponSocket>();
		}
		public int frameIndex;
		public Point relativeHandPos;
		public Point2D normRelativeHandPos;
		
		public static final int characterPixelResolution = 48;
		
		private List<WeaponSocket> weaponSockets;
	}
	public static class WeaponSocket {
		public WeaponSocket(WeaponType[] weaponTypes, Point handlePos, boolean flipX, float zRot) {
			this.weaponTypes = new ArrayList<WeaponType>();
			this.weaponTypes.addAll( (List<WeaponType>)Arrays.asList(weaponTypes) );
			this.handlePos = handlePos;
			normHandlePos = new Point2D.Float((float)handlePos.x / weaponPixelResolution, (float)handlePos.y / weaponPixelResolution);
			this.flipX = flipX;
			this.zRot = zRot;
		}
		public WeaponSocket(WeaponType[] weaponTypes, Point handlePos, boolean flipX, float zRot, AnimCommand[] commands_onStart) {
			this(weaponTypes, handlePos, flipX, zRot);
			this.commands_onStart = commands_onStart;
		}
		
		public List<WeaponType> weaponTypes;
		public Point handlePos;
		public Point2D normHandlePos;
		public boolean flipX;
		public float zRot;
		
		public static final int weaponPixelResolution = 16;
		
		//Use these to embed commands that the BattleCharacterController will use to do things like image swapping, and hiding/showing
		public AnimCommand[] commands_onStart;
	}
	
	public static AnimSocketsPack GetHandSocketsForAnim(AnimType animType, WeaponType weaponType, boolean isRangedAttack) {
		if(animType == null) {
			System.err.println("SpriteSheetUtility.GetHandSocketsForAnim() - animType can't be null! This method will fail with an exception.");
			Thread.dumpStack();
		}
		
		//Return null for unarmed
		if(animType == AnimType.MainAttack && weaponType == null)
			return null;
		
		AnimSocketsPack animSocketsPack = new AnimSocketsPack();
		
		AnimSubset animSubset = AnimSubtypeManager.GetAnimSubset(animType, weaponType, isRangedAttack);
		
		System.out.println("SpriteSheetUtility.GetHandSocketsForAnim() - animSubset: " + animSubset.toString());
		
		if(animFrameSocketMap.containsKey(animSubset)) {
			List<HandSocket> refList = animFrameSocketMap.get(animSubset);

			//Callout missing hard-coded socket information
			List<HandSocket> frameSockets = new ArrayList<HandSocket>( refList );
			if(frameSockets != null) {		
				for(HandSocket handSocket : frameSockets) {
					WeaponSocket testWeaponSocket = GetWeaponSocketFrom(handSocket, weaponType);
					if(testWeaponSocket == null)
						System.err.println("SpriteSheetUtility.GetHandSocketsForAnim() - Anim: " + animSubset.animType + " is missing a WeaponSocket with type: " + weaponType
								+ ". Add it in SpriteSheetUtility static intialization block.");
				}
			}
			
			animSocketsPack.animSubset = animSubset;
			animSocketsPack.frameSockets = new ArrayList<HandSocket>( frameSockets );
			return animSocketsPack;
		} else {
			System.err.println("SpriteSheetUtility.GetHandSocketsForAnim() - The animFrameSocketMap does not contain an entry for animType: " + animType + ". This may be deliberate.");
			
			System.err.println("SpriteSheetUtility.GetHandSocketsForAnim() - Available AnimSubsets currently in animFrameSocketMap:");
			for(AnimSubset animSub : animFrameSocketMap.keySet()) {
				System.err.println("  -" + animSub.toString());
			}
			
			return null;
		}
	}
	
	public static WeaponSocket GetWeaponSocketFrom(HandSocket handSocket, WeaponType weaponType) {
		Optional<WeaponSocket> matchingWeaponSocket = handSocket.weaponSockets.stream().filter(x -> x.weaponTypes.contains(weaponType)).findFirst();
		if(matchingWeaponSocket.isPresent())
			return matchingWeaponSocket.get();
		else
			return null;
	}
	
	/**
	 * Use this to organize all the varient frameSockets needed for alternate anims.
	 * Generic: The base version of the AnimType.
	 * WeaponSpecific: A varient of an animType meant for certain WeaponTypes
	 * @author Magnus
	 */
	public enum AnimSubtype { Generic, WeaponSpecific };
	public class AnimSubset {
		/**
		 * For Generic subtype
		 * @param animType
		 */
		public AnimSubset(AnimType animType) {
			this.animType = animType;
			this.animSubtype = AnimSubtype.Generic;
		}
		/**
		 * For WeaponSpecific subtype
		 * @param animType
		 * @param weaponTypes
		 */
		/*public AnimSubset(AnimType animType, List<WeaponType> weaponTypes) {
			this.animType = animType;
			this.animSubtype = AnimSubtype.WeaponSpecific;
			this.WeaponSpecific_weaponTypes = weaponTypes;
		}*/
		/**
		 * For WeaponSpecific subtype, now with a parameter for the overridingAnimType which is the varient of the genericAnimType(i.e. as AnimType.MainAttack_Vr1 is to AnimType.MainAttack)
		 * @param genericAnimType
		 * @param overridingAnimType
		 * @param weaponTypes
		 */
		public AnimSubset(AnimType genericAnimType, AnimType overridingAnimType, List<WeaponType> weaponTypes) {
			this.animType = genericAnimType;
			this.overridingAnimType = overridingAnimType;
			
			this.animSubtype = AnimSubtype.WeaponSpecific;
			this.WeaponSpecific_weaponTypes = weaponTypes;
		}
		
		AnimType animType;
		//The varient AnimType thats standing in place on the generic one
		AnimType overridingAnimType;
		
		AnimSubtype animSubtype;
		//Subtype classifications
		List<WeaponType> WeaponSpecific_weaponTypes = new ArrayList<WeaponType>();
		
		@Override
		public String toString() {
			String weaponsTypesString = "";
			for(WeaponType type : WeaponSpecific_weaponTypes)
				weaponsTypesString += type.toString() + ", ";
			return
					"AnimSubset - animType: " + animType +
					", overridingAnimType: " + overridingAnimType +
					", animSubtype: " + animSubtype +
					", WeaponSpecific_weaponTypes: " + weaponsTypesString
			;
		}
	}
	private static class AnimSubtypeManager {
		private static List<AnimSubset> animSubsets;
		public static List<AnimSubset> getAnimSubsets() { return animSubsets; }
		
		private static void Initialize(SpriteSheetUtility instance) {
			//Populate the generic subtypes for every AnimType, these will be used to refer to the "base" animations
			animSubsets = new ArrayList<AnimSubset>();
			for(AnimType animType : AnimType.values())
				animSubsets.add(instance.new AnimSubset(animType));
		}
		public static void AddVarientSubset(AnimSubset animSubset) {
			if(animSubsets.contains(animSubset))
				System.err.println("BattleCharacterController.AnimSubtypeManager Constructor - An attempt is being made to add a duplicate AnimSubset: " + animSubset.animType + ", " + animSubset.animSubtype);
			else
				animSubsets.add(animSubset);
		}
		
		public static AnimSubset GetAnimSubset(AnimType animType, WeaponType weaponType, boolean isRangedAttack) {
			if(animType == null)
				System.err.println("SpriteSheetUtility.AnimSubtypeManager.GetAnimSubset() - animType can't be null! This method will fail with an exception.");
			
			AnimSubset chosenAnimSubset = null;
			List<AnimSubset> animTypeSubsets = new ArrayList<>( (List<AnimSubset>)Arrays.asList( animSubsets.stream().filter(x -> x.animType == animType).toArray(AnimSubset[]::new) ) );
			
			if(weaponType != null) {
				AnimSubset weaponSpecificSubset = animTypeSubsets.stream().filter(x -> x.animSubtype == AnimSubtype.WeaponSpecific && x.WeaponSpecific_weaponTypes.contains(weaponType)).findFirst().orElse(null);
				if(weaponSpecificSubset != null) {
					chosenAnimSubset = weaponSpecificSubset;
				}
			}
			
			if(chosenAnimSubset == null)
				chosenAnimSubset = animSubsets.stream().filter(x -> x.animType == animType && x.animSubtype == AnimSubtype.Generic).findFirst().get();
			
			
			//Override the animset to be a generic MainAttack if we're close range with a Versitile type weapon
			if(chosenAnimSubset.overridingAnimType == AnimType.Throw && !isRangedAttack && weaponType != WeaponType.Shuriken) {
				//System.out.println("	Correcting a Versitile weapon's anim to use the generic MainAttack when in melee range, flagged animSubset: " + chosenAnimSubset);
				//Dont create a create a new instance or try overriding the onld instance because that instance will still that the same object.ID and will still yield the
				//same return from animFrameSocketMap.get()
				chosenAnimSubset = animTypeSubsets.stream().filter(x -> x.animType == AnimType.MainAttack && x.animSubtype == AnimSubtype.Generic).findFirst().orElse(null);
			}
			
			
			return chosenAnimSubset;
		}
	}
	
	// - Weapon Combat Sprites ->
	
	//MapLocation Tile Spritesheets - Start
	
	private final static String tileSheetPath = "mapLocationScenes/_AnimatedSheets/";
	/**
	 * Add all required sheets to this list or they wont get loaded
	 */
	private static String[] tileSheetNames;
	private static Map<String, SpriteSheet> tileSheet;
	
	public static String GetSheetNameFromFrame(String frameName) {
		String[] splits = frameName.split("_");
		String sheetName = "";
		for(int i = 0; i < splits.length - 1; i++) {
			sheetName += splits[i];
			if(splits.length - 2 >= 0 && i < splits.length - 2)
				sheetName += "_";
		}
		return sheetName;
	}
	
	public static SpriteSheet GetTileSheet(String sheetName) {
		if(!tileSheet.containsKey(sheetName)) {
			System.err.println("SpriteSheetUtility.GetTileSheet(String sheetName) - Couldn't find sheet named: " + sheetName);
			return null;
		}
		return tileSheet.get(sheetName);
	}
	
	public static SpriteSheet GetEffectSheet(String sheetPath) {
		BufferedImage sheetImage = GUIUtil.GetBuffedImage(sheetPath);
		
		//String sheetPathWithoutExt = sheetPath.split("\\.")[0];
		//System.out.println("SpriteSheetUtility.GetEffectSheet() - sheetPathWithoutExt: " + sheetPathWithoutExt);
		//String fullMetaPath = "resources/" + sheetPathWithoutExt + ".meta";
		String fullMetaPath = "resources/" + sheetPath + ".meta";
		
		SpriteSheetUtility instance = new SpriteSheetUtility();
		InputStream is = instance.getClass().getClassLoader().getResourceAsStream(fullMetaPath);
		if(is == null) {
			System.err.println("File not found at: " + fullMetaPath + " ... Dont forget to Refresh the Java project after adding new files.");
			Thread.dumpStack();
			return null;
		}
		
		List<String> lines = new ArrayList<String>();
		try {
			InputStreamReader isReader = new InputStreamReader(is);
			BufferedReader reader = new BufferedReader(isReader);
			String str;
			while((str = reader.readLine()) != null) {
				StringBuffer sb = new StringBuffer();
				sb.append(str);
				lines.add(sb.toString());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//parse meta file
		List<SpriteMeta> spriteMetas = new ArrayList<SpriteMeta>();
		boolean hasReachedSpriteSection = false;
		SpriteMeta currentSpriteMeta = null;
		for(String str : lines) {
			//System.out.println("Line: " + str);
			
			if(hasReachedSpriteSection) {
				if(str.startsWith("    outline:")) {
					if(currentSpriteMeta != null)
						spriteMetas.add(currentSpriteMeta);
					break;
				}
				
				if(str.startsWith("      name:")) {
					if(currentSpriteMeta != null)
						spriteMetas.add(currentSpriteMeta);	
					currentSpriteMeta = new SpriteMeta();
					
					String value = str.split(": ")[1];
					if(value.startsWith("'")) {
						String[] splits = value.split("'");
						currentSpriteMeta.name = splits[1];
					} else {
						currentSpriteMeta.name = value;
					}
				} else if(str.startsWith("        x:")) {
					currentSpriteMeta.x = Integer.parseInt( str.split(": ")[1] );
				} else if(str.startsWith("        y:")) {
					currentSpriteMeta.y = Integer.parseInt( str.split(": ")[1] );
				} else if(str.startsWith("        width:")) {
					currentSpriteMeta.width = Integer.parseInt( str.split(": ")[1] );
				} else if(str.startsWith("        height:")) {
					currentSpriteMeta.height = Integer.parseInt( str.split(": ")[1] );
				} else {
					continue;
				}
			} else if(str.startsWith("    sprites:"))
				hasReachedSpriteSection = true;
		}
		
		return new SpriteSheet(sheetImage, spriteMetas);
	}
	
	private static void SetupTileSheets() {
		tileSheetNames = new String[] {
			"!doors1a",
			"!doors1b",
			"!doors2",
			"DunesTileset_V2",
			"tileA1_water"
		};
		tileSheet = new HashMap<String, SpriteSheet>();
		
		for(int i = 0; i < tileSheetNames.length; i++) {
			String fullImagePath = tileSheetPath + tileSheetNames[i] + ".png";
			BufferedImage sheetImage = GUIUtil.GetBuffedImage(fullImagePath);
			
			String fullMetaPath = "resources/" + fullImagePath + ".meta";
			SpriteSheetUtility instance = new SpriteSheetUtility();
			InputStream is = instance.getClass().getClassLoader().getResourceAsStream(fullMetaPath);
			if(is == null) {
				System.err.println("File not found at: " + fullMetaPath + " ... Dont forget to Refresh the Java project after adding new files.");
				Thread.dumpStack();
				return;
			}
			
			List<String> lines = new ArrayList<String>();
			try {
				InputStreamReader isReader = new InputStreamReader(is);
				BufferedReader reader = new BufferedReader(isReader);
				String str;
				while((str = reader.readLine()) != null) {
					StringBuffer sb = new StringBuffer();
					sb.append(str);
					lines.add(sb.toString());
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			//parse meta file
			List<SpriteMeta> spriteMetas = new ArrayList<SpriteMeta>();
			boolean hasReachedSpriteSection = false;
			SpriteMeta currentSpriteMeta = null;
			for(String str : lines) {
				//System.out.println("Line: " + str);
				
				if(hasReachedSpriteSection) {
					if(str.startsWith("    outline:")) {
						if(currentSpriteMeta != null)
							spriteMetas.add(currentSpriteMeta);
						break;
					}
					
					if(str.startsWith("      name:")) {
						if(currentSpriteMeta != null)
							spriteMetas.add(currentSpriteMeta);	
						currentSpriteMeta = new SpriteMeta();
						
						String value = str.split(": ")[1];
						if(value.startsWith("'")) {
							String[] splits = value.split("'");
							currentSpriteMeta.name = splits[1];
						} else {
							currentSpriteMeta.name = value;
						}
					} else if(str.startsWith("        x:")) {
						currentSpriteMeta.x = Integer.parseInt( str.split(": ")[1] );
					} else if(str.startsWith("        y:")) {
						currentSpriteMeta.y = Integer.parseInt( str.split(": ")[1] );
					} else if(str.startsWith("        width:")) {
						currentSpriteMeta.width = Integer.parseInt( str.split(": ")[1] );
					} else if(str.startsWith("        height:")) {
						currentSpriteMeta.height = Integer.parseInt( str.split(": ")[1] );
					} else {
						continue;
					}
				} else if(str.startsWith("    sprites:"))
					hasReachedSpriteSection = true;
			}
			
			SpriteSheet spriteSheet = new SpriteSheet(sheetImage, spriteMetas);
			tileSheet.put(tileSheetNames[i], spriteSheet);
		}
	}
	
	//MapLocation Tile Spritesheets - End
}
