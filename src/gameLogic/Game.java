package gameLogic;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import javax.swing.Timer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import data.CharacterData;
import data.CombatEffect;
import data.CombatEffect_Buff;
import data.CombatEffect_Cure;
import data.CombatEffect_Damage;
import data.CombatEffect_Debuff;
import data.CombatEffect_Potion;
import data.CombatEffect_Revive;
import data.CombatEffect_Status;
import data.HealthModInfo;
import data.FlexibleTransition;
import data.ItemData;
import data.LingeringEffect;
import data.MissionNode;
import data.SaveData;
import data.SceneData;
import data.SceneData.Breakaway;
import data.SceneData.Row;
import data.SceneData.Row.ImageLayer;
import data.SceneData.Row.TileData;
import data.SceneData.VisualTileData;
import data.WorldTileData;
import data.WorldmapData;
import data.AttributeMod;
import data.BattleData;
import data.BattleItemTraits;
import data.BattleState;
import data.CharacterBaseData;
import enums.AttributeModType;
import enums.BattleItemType;
import enums.ItemType;
import enums.MenuType;
import enums.SceneLayeringType;
import enums.StatusType;
import gameLogic.AbilityManager.Ability;
import gameLogic.Board.Tile;
import gameLogic.CharacterBase.ActionOptionInfo;
import gameLogic.CharacterBase.CombatCalcInfo;
import gameLogic.CharacterBase.SpecificActionType;
import gameLogic.Mission.MissionStatusType;
import gameLogic.Objective.TargetType;

import gui.BattlePanel;
import gui.GUIManager;


public class Game {
	private static Game instance;
	public static Game Instance() { return instance; }
	
	private boolean isDataLoaded;
	public boolean IsDataLoaded() { return isDataLoaded; }
	private SaveData saveData;
	public boolean DoesPlayerDataExist() { return saveData.playerData != null; }

	
	public Game() {
		instance = this;
		
		//Try to load character data
		TryLoadData();
		
		//Now that we've loaded our saveData we can access worldTileDatas contained within
		Missions.FindDirectMissionExtents();
		
		
		//Test the ImagePanel.GetRotationOrigin() output
		/*System.err.println("-= Natural =-");
		ImagePanel.GetRotatedOrigin(new Dimension(100, 100), false, 0f, new Point2D.Float(0.25f, 0.25f));
		System.err.println("	Should be: top left (0.25, 0.25)");
		ImagePanel.GetRotatedOrigin(new Dimension(100, 100), false, 0f, new Point2D.Float(0.5f, 0.5f));
		System.err.println("	Should be: center (0.5, 0.5)");
		ImagePanel.GetRotatedOrigin(new Dimension(100, 100), false, 0f, new Point2D.Float(0.75f, 0.75f));
		System.err.println("	Should be: bottom right (0.75, 0.75)");
		ImagePanel.GetRotatedOrigin(new Dimension(100, 100), false, 0f, new Point2D.Float(0.75f, 0.25f));
		System.err.println("	Should be: top right (0.75, 0.25)");
		
		System.err.println("-= Natural, Rot 90 =-");
		ImagePanel.GetRotatedOrigin(new Dimension(100, 100), false, 90f, new Point2D.Float(0.25f, 0.25f));
		System.err.println("	Should be: top right (0.75, 0.25)");
		ImagePanel.GetRotatedOrigin(new Dimension(100, 100), false, 90f, new Point2D.Float(0.5f, 0.5f));
		System.err.println("	Should be: center (0.5, 0.5)");
		ImagePanel.GetRotatedOrigin(new Dimension(100, 100), false, 90f, new Point2D.Float(0.75f, 0.75f));
		System.err.println("	Should be: bottom left (0.25, 0.75)");
		ImagePanel.GetRotatedOrigin(new Dimension(100, 100), false, 90f, new Point2D.Float(0.75f, 0.25f));
		System.err.println("	Should be: bottom right (0.75, 0.75)");
		
		System.err.println("-= Mirrored =-");
		ImagePanel.GetRotatedOrigin(new Dimension(100, 100), true, 0f, new Point2D.Float(0.25f, 0.25f));
		System.err.println("	Should be: top right (0.75, 0.25)");
		ImagePanel.GetRotatedOrigin(new Dimension(100, 100), true, 0f, new Point2D.Float(0.5f, 0.5f));
		System.err.println("	Should be: center (0.5, 0.5)");
		ImagePanel.GetRotatedOrigin(new Dimension(100, 100), true, 0f, new Point2D.Float(0.75f, 0.75f));
		System.err.println("	Should be: bottom left (0.25, 0.75)");
		ImagePanel.GetRotatedOrigin(new Dimension(100, 100), true, 0f, new Point2D.Float(0.75f, 0.25f));
		System.err.println("	Should be: top left (0.25, 0.25)");
		
		System.err.println("-= Mirrored, Rot 90 =-");
		//Even though we intend to apply a 90 degree rotation here, because its mirrored(Right Facing), the rotation must be supplied as the inverse angle instead.
		ImagePanel.GetRotatedOrigin(new Dimension(100, 100), true, 270f, new Point2D.Float(0.25f, 0.25f));
		System.err.println("	Should be: top left (0.25, 0.25)");
		ImagePanel.GetRotatedOrigin(new Dimension(100, 100), true, 270f, new Point2D.Float(0.5f, 0.5f));
		System.err.println("	Should be: center (0.5, 0.5)");
		ImagePanel.GetRotatedOrigin(new Dimension(100, 100), true, 270f, new Point2D.Float(0.75f, 0.75f));
		System.err.println("	Should be: bottom right (0.75, 0.75)");
		ImagePanel.GetRotatedOrigin(new Dimension(100, 100), true, 270f, new Point2D.Float(0.75f, 0.25f));
		System.err.println("	Should be: bottom left (0.25, 0.75)");
		
		System.err.println("-= Left Facing Char - Attack Frame 0 =-");
		System.err.println("-= Mirrored, Rot -45 =-");
		ImagePanel.GetRotatedOrigin(new Dimension(100, 100), true, 315f, new Point2D.Float(0.25f, 0.75f));
		System.err.println("	Should be: top left (0.85, 0.5)");
		
		System.err.println("-= Right Facing Char - Attack Frame 0 =-");
		System.err.println("-= Natural, Rot -45 =-");
		ImagePanel.GetRotatedOrigin(new Dimension(100, 100), false, 45f, new Point2D.Float(0.25f, 0.75f));
		System.err.println("	Should be: top left (0.15, 0.5)");*/
	}
	
	//private static final String dataPath = System.getProperty("user.home") + "\\RepublicsAndOverlords\\";
	private static final String dataPath = System.getProperty("user.home") + "\\AgentsOfTheOutcaste\\";
	private final String dataFileName = "AO_saveFile.obj";
	
	public static String SetupOrGetDataPathDirectory() {
		File tempDirectory = new File(dataPath);
		//Creates any missing directories for the path
		if(!tempDirectory.exists())
			tempDirectory.mkdirs();
		return dataPath;
	}
	
	public File GetSaveFile() {
		File file = null;
		
		file = new File(SetupOrGetDataPathDirectory() + dataFileName);
		 
		return file;
	}
	
	private void TryLoadData() {
		isDataLoaded = true;
		try {
			FileInputStream fi = new FileInputStream(GetSaveFile());
			ObjectInputStream oi = new ObjectInputStream(fi);

			// Read objects
			saveData = (SaveData) oi.readObject();

			//Uncomment this to debug SaveData
			//saveData.print();

			oi.close();
			fi.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			isDataLoaded = false;
		} catch (IOException e) {
			System.out.println("Error initializing stream");
			isDataLoaded = false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			isDataLoaded = false;
		} finally {
			if(saveData == null) {
				System.out.println("SaveData didn't exist or wasn't successfully loaded. Creating new SaveData.");
				saveData = new SaveData();
			}
			
			/*if(saveData.teamDatas != null) {
				for(CharacterData teammate : saveData.teamDatas)
					System.out.println("Loaded teammate: " + teammate.getName());
			}*/
		}
		
		//Load Scene Data file
		if(isDataLoaded && saveData.worldmapData != null && saveData.worldmapData.GetWorldMapDatas() != null) {
			WorldTileData worldTileData = saveData.worldmapData.GetWorldMapDatas().get(saveData.currentWorldmapLocation);
			MapLocation startLocation = worldTileData.mapLocation;
			LoadSceneData(startLocation);
			
			Missions.SetFlexibleTransitionsLoadedFromSaveData(saveData.missionTree, saveData.flexibleMissionTransitions);
		} else {
			System.out.println("Game.TryLoadData() - Failed to load data. isDataLoaded: " + isDataLoaded + ", saveData.worldmapData != null: " + (saveData.worldmapData != null) +
					", saveData.worldmapData.GetWorldMapDatas() != null: " + (saveData.worldmapData != null && saveData.worldmapData.GetWorldMapDatas() != null));
		}
	}
	
	public void SaveMissionTransitions(List<MissionNode> missionTree, List<FlexibleTransition> flexibleMissionTransitions) {
		saveData.missionTree = missionTree;
		saveData.flexibleMissionTransitions = flexibleMissionTransitions;
	}
	
	public void SaveData() {
		//Collect this sessions data from the WorldmapData if we're not on the title screen
		if(GUIManager.WorldmapPanel().GetWorldTileDataMap() != null)
			saveData.worldmapData.SetWorldMapDatas(GUIManager.WorldmapPanel().GetWorldTileDataMap());
		
		try {
			FileOutputStream f = new FileOutputStream(GetSaveFile());
			ObjectOutputStream o = new ObjectOutputStream(f);
			
			// Write objects to file
			o.writeObject(saveData);

			o.close();
			f.close();
			
			System.out.println("Save Complete");
			//saveData.print();
		} catch (FileNotFoundException e) {
			System.err.println("Game.SaveData() - File not found");
		} catch (IOException e) {
			System.err.println("Game.SaveData() - Error initializing stream");
			e.printStackTrace();
		}
	}
	
	public void ResetSaveData() {
		System.out.println("Game.ResetSaveData()");
		saveData = new SaveData(saveData);
	}
	
	public void SetPlayerData(CharacterData playerData) {
		saveData.playerData = playerData;
	}
	public CharacterData GetPlayerData() {
		return saveData.playerData;
	}
	
	//Board/Battle related stuff - Start
	
	private BattlePanel battlePanel;
	public BattlePanel GetBattlePanel() { return battlePanel; }
	private Board board;
	//public Board getBoard() { return board; }
	private BattleData currentBattleData;
	public BattleData GetBattleData() { return currentBattleData; }
	private boolean isPlacementPhase;
	public boolean IsPlacementPhase() { return isPlacementPhase; }
	
	
	public void SetBattlePanel(BattlePanel battlePanel) {
		this.battlePanel = battlePanel;
	}
	
	/**
	 * Called by WorldmapPanel on a fight interaction, or any other interaction with BattleData.
	 * @param battleData
	 */
	public void StartBattle(BattleData battleData) {
		//See the method's summary for an explanation
		ClearOldBattleData();
		
		isPlacementPhase = true;
		
		currentBattleData = battleData;
		System.out.println("Game.StartBattle() - Is Wincon null: " + (currentBattleData.WinCondition() == null));
		
		System.err.println("Game.StartBattle() - Write Board an Initialize method to facilitate setting up the Board and BattlePanel after the first battle of this application session.");
		//if(board == null)
			board = new Board(battlePanel, null);
		//else
			//board.Initialize();
	}
	/**
	 * Called by WorldmapPanel on load to restore a battle suspended by exiting the game.
	 * @param battleStateToRestore - This should hold all the information necessary to reconstruct the battle to its previous state.
	 */
	public void StartBattle(BattleState battleStateToRestore) {
		//See the method's summary for an explanation
		ClearOldBattleData();
		
		//Extract all the info we need from battleStateToRestore to set our local variables
		isPlacementPhase = battleStateToRestore.isPlacementPhase;
		turnCount = battleStateToRestore.turnCount;
		turnPhases = battleStateToRestore.turnPhases;
		//This variable is going to modulate all the relevant battle data being retrieved from Game by Board and BattlePanel, etc
		currentBattleData = battleStateToRestore.battleData;
		System.out.println("Game.StartBattle() - Is Wincon null: " + (currentBattleData.WinCondition() == null));
		
		//The board will never be constructed before this point because this logic flow has only one chance to occur at the start of the application session, like the expectation in this methods overload
		board = new Board(battlePanel, battleStateToRestore);
	}
	
	private List<CharacterBase> enemyCharacterList = new ArrayList<CharacterBase>();
	public List<CharacterBase> GetEnemyCharacterList() { return enemyCharacterList; }
	
	//Called by CharacterOverlays during setup
	public CharacterBase CreateCharacter_Enemy(CharacterData characterData, Point location) {
		CharacterBase charBase = new CharacterBase(characterData, location);
		enemyCharacterList.add(charBase);
		return charBase;
	}
	/**
	 * Overload for restoring a battleState from game load.
	 * @param characterBaseData - All the info needed to restore a CharacterBase back to its previous state.
	 * @return
	 */
	public CharacterBase CreateCharacter_Enemy(CharacterBaseData characterBaseData) {
		CharacterBase charBase = new CharacterBase(characterBaseData);
		enemyCharacterList.add(charBase);
		return charBase;
	}
	
	private List<CharacterBase> allyCharacterList = new ArrayList<CharacterBase>();
	public List<CharacterBase> GetAllyCharacterList() { return allyCharacterList; }
	
	public CharacterBase CreateCharacter_Ally(CharacterData characterData, Point location) {
		CharacterBase charBase = new CharacterBase(characterData, location);
		allyCharacterList.add(charBase);
		return charBase;
	}
	/**
	 * Overload for restoring a battleState from game load.
	 * @param characterBaseData - All the info needed to restore a CharacterBase back to its previous state.
	 * @return
	 */
	public CharacterBase CreateCharacter_Ally(CharacterBaseData characterBaseData) {
		CharacterBase charBase = new CharacterBase(characterBaseData);
		allyCharacterList.add(charBase);
		return charBase;
	}
	
	//May need an UpdateCharacter method when selecting a placed ally and swapping them for a different character
	//public void UpdateCharacter() { }
	
	public void RemoveCharacter_Ally(String id) {
		allyCharacterList.removeIf(x -> x.GetData().getId().equals(id));
	}
	
	private List<CharacterBase> npcAllyCharacterList = new ArrayList<CharacterBase>();
	public List<CharacterBase> GetNpcAllyCharacterList() { return npcAllyCharacterList; }
	
	public CharacterBase CreateCharacter_NpcAlly(CharacterData characterData, Point location) {
		CharacterBase charBase = new CharacterBase(characterData, location);
		npcAllyCharacterList.add(charBase);
		return charBase;
	}
	/**
	 * Overload for restoring a battleState from game load.
	 * @param characterBaseData - All the info needed to restore a CharacterBase back to its previous state.
	 * @return
	 */
	public CharacterBase CreateCharacter_NpcAlly(CharacterBaseData characterBaseData) {
		CharacterBase charBase = new CharacterBase(characterBaseData);
		npcAllyCharacterList.add(charBase);
		return charBase;
	}
	
	public CharacterData[] GetAvailableBattleRoster() {
		List<CharacterData> availableDatas = new ArrayList<CharacterData>();
		
		List<CharacterBase> placedCharBases = new ArrayList<CharacterBase>( allyCharacterList );
		for(CharacterData charData : GetPartyData()) {
			if(charData == null) {
				System.err.println("CharData == null!!!");
				continue;
			} else {
				System.out.println("Getting CharacterData for: " + charData.getName());
			}
			if(!placedCharBases.stream().anyMatch(x -> x.GetData().getId() == charData.getId()))
				availableDatas.add(charData);
		}
		
		return availableDatas.stream().toArray(CharacterData[]::new);
	}
	
	//Used for Calcing moves so that they can be displayed during the placement phase, for allies and enemies alike
	public List<CharacterBase> GetAllPlacedCharBases() {
		List<CharacterBase> charBases = new ArrayList<CharacterBase>();
		charBases.addAll(allyCharacterList);
		charBases.addAll(enemyCharacterList);
		charBases.addAll(npcAllyCharacterList);
		return charBases;
	}
	
	private List<CharacterBase> turnOrderedCharBases = new ArrayList<CharacterBase>();
	public List<CharacterBase> GetTurnOrderedCharBases() { return turnOrderedCharBases; }
	private int turnOrderIndex;
	public int getTurnOrderIndex() { return turnOrderIndex; }
	public CharacterBase GetActiveBattleCharacter() {
		if(turnOrderedCharBases.size() == 0) {
			//System.err.println("ERROR : Game.GetActiveCharacter() - turnOrderedCharBases is empty!");
			//This debug isn't useful, its better to let all processes who need the active player check its null status
			return null;
		} else {
			return turnOrderedCharBases.get(turnOrderIndex);
		}
	}
	
	public void CompletePlacementPhase() {
		isPlacementPhase = false;
		
		//Determine turn order
		Map<Integer, ArrayList<CharacterBase>> charsBySpeed = new HashMap<Integer, ArrayList<CharacterBase>>();
		turnOrderedCharBases.addAll(enemyCharacterList);
		turnOrderedCharBases.addAll(npcAllyCharacterList);
		turnOrderedCharBases.addAll(allyCharacterList);
		for(CharacterBase charBase : turnOrderedCharBases) {
			Integer speedInteger = new Integer(charBase.GetData().getSpeed());
			if(!charsBySpeed.containsKey(speedInteger))
				charsBySpeed.put(speedInteger, new ArrayList<CharacterBase>());
			charsBySpeed.get(speedInteger).add(charBase);
		}
		turnOrderedCharBases.clear();
		
		Integer[] keys = charsBySpeed.keySet().stream().sorted().toArray(Integer[]::new);
		for(Integer integer : keys) {
			//System.out.println("Integer Order: " + integer.toString());
			Collections.shuffle(charsBySpeed.get(integer));
			turnOrderedCharBases.addAll(0, charsBySpeed.get(integer));
		}
		//Testing
		//turnOrderedCharBases.addAll(allyCharacterList);
		//turnOrderedCharBases.addAll(enemyCharacterList);
		//turnOrderedCharBases.addAll(npcAllyCharacterList);
		
		//I'm pretty sure we'll need this here
		//get paths for charaters, now that we've got turnOrderedCharBases populated
		board.CalcAllMoves();
		
		//Start first turn
		StartNextCharacterTurn();
	}
	
	public void StartRestoredBattle(BattleState battleStateToRestore, Board liveBoardInstance) {
		isPlacementPhase = false;
		turnOrderIndex = battleStateToRestore.turnOrderIndex;
		
		//Recreate turn order
		for(int i = 0; i < battleStateToRestore.enemyBaseDataMap.size() + battleStateToRestore.allyBaseDataMap.size() + battleStateToRestore.npcAllyBaseDataMap.size(); i++) {
			CharacterBase matchingBase = null;
			if(battleStateToRestore.enemyBaseDataMap.keySet().contains(i)) {
				final CharacterBaseData baseDataToMatch = battleStateToRestore.enemyBaseDataMap.get(i);
				matchingBase = enemyCharacterList.stream().filter(x -> x.GetData().getId().equals(baseDataToMatch.data.getId())).findFirst().get();
			} else if(battleStateToRestore.allyBaseDataMap.keySet().contains(i)) {
				final CharacterBaseData baseDataToMatch = battleStateToRestore.allyBaseDataMap.get(i);
				matchingBase = allyCharacterList.stream().filter(x -> x.GetData().getId().equals(baseDataToMatch.data.getId())).findFirst().get();
			} else if(battleStateToRestore.npcAllyBaseDataMap.keySet().contains(i)) {
				final CharacterBaseData baseDataToMatch = battleStateToRestore.npcAllyBaseDataMap.get(i);
				matchingBase = npcAllyCharacterList.stream().filter(x -> x.GetData().getId().equals(baseDataToMatch.data.getId())).findFirst().get();
			} else {
				System.err.println("Game.StartRestoredBattle() - There is no battleStateToRestore.(...)BaseDataMap that contains the turn index: " + i);
			}
			turnOrderedCharBases.add( matchingBase );
		}
		
		//get paths for charaters, now that we've got turnOrderedCharBases populated
		liveBoardInstance.CalcAllMoves();
		
		//Resume turnTaker's turn
		CharacterBase character = turnOrderedCharBases.get(turnOrderIndex);
		System.out.println("Game.StartRestoredBattle() - Resuming turn for: " + character.GetData().getName());
		battlePanel.HideActionPanel();
		battlePanel.ShowCharacterCard(character.GetData(), character.getLocation());
		
		//Use this to start the turnTakers lingering effects anims where we left off OR ignore them if they already played or they didnt have any to begin with
		if(battleStateToRestore.remainingLingeringAnimCombatEffects != null && battleStateToRestore.remainingLingeringAnimCombatEffects.size() > 0)
			ResumeLingeringEffectAnims(battleStateToRestore.remainingLingeringAnimCombatEffects);
		else
			OnLingeringAnimsDone();
	}
	
	/**
	 * Guides logic flow as to whether we treat the turnTsaker like a user controller character or an AI
	 */
	boolean isAStatusControllingOurAlly;
	/**
	 * The objectives for the current turnTaker.
	 */
	List<Objective> currentObjectives = new ArrayList<Objective>();
	
	/**
	 * Called internally upon the start of a turn, unless the battleState is being restored
	 */
	private void StartNextCharacterTurn() {
		//Do this initially whether its a user-controlled character or an AI
		CharacterBase character = turnOrderedCharBases.get(turnOrderIndex);
		System.out.println("Game.StartNextCharacterTurn() - For: " + character.GetData().getName());
		battlePanel.HideActionPanel();
		battlePanel.ShowCharacterCard(character.GetData(), character.getLocation());
		
		character.OnTurnStart();
	}
	
	/**
	 * This is the callback that officually starts the characters turn. It's called either by CharacterBase.OnTurnStart() if there are no lingering anims or
	 * by this.OnCombatAnimComplete() when ending the lingering anims.
	 */
	public void OnLingeringAnimsDone() {
		CharacterBase character = turnOrderedCharBases.get(turnOrderIndex);
		
		BattleOutcome battleOutcome = AssessObjectives();
		boolean didWin = false;
		switch(battleOutcome) {
			case Pending:
				break;
			case BattleWon:
				didWin = true;
				//Fallthrough to BattleLost
			case BattleLost:
				EndBattle(didWin);
				break;
			default:
				System.err.println("Game.StartNextCharacterTurn() - Add support for: " + battleOutcome);
		}
		if(battleOutcome != BattleOutcome.Pending)
			return;
		
		//Update NpcAlly Objectives
		if(npcAllyCharacterList.size() > 0) {
			UpdateObjectiveListForCharacters(currentObjectives_npcAllyAI, enemyCharacterList, Objective.CharacterTask.Kill);
			UpdateObjectiveListForCharacters(currentObjectives_npcAllyAI, allyCharacterList, Objective.CharacterTask.Heal);
		}
		
		
		if(character.GetHp() <= 0) {
			System.out.println("Game.OnLingeringAnimsDone() - Ending turn for turnTaker prematurely, they were killed and not revived during the application of the lingering effects.");
			EndTurn();
			return;
		}
		
		
		//Figure out objectives for this characters turn
		isAStatusControllingOurAlly = false;
		currentObjectives.clear();
		List<CharacterBase> opposingCharacters = new ArrayList<CharacterBase>();
		if(enemyCharacterList.contains(character)) {
			opposingCharacters.addAll(allyCharacterList);
			opposingCharacters.addAll(npcAllyCharacterList);
			if(!character.GetActiveStatuses().contains(StatusType.Charmed))
				currentObjectives = new ArrayList<Objective>(currentObjectives_enemyAI);
			else {
				//Create objectives for this charmed enemy as-if they were an ally
				List<CharacterBase> enemiesExcludingSelf = new ArrayList<CharacterBase>(enemyCharacterList);
				enemiesExcludingSelf.remove(character);
				if(enemiesExcludingSelf.size() > 0)
					UpdateObjectiveListForCharacters(currentObjectives, enemiesExcludingSelf, Objective.CharacterTask.Kill);
				else
					UpdateObjectiveListForCharacters(currentObjectives, allyCharacterList, Objective.CharacterTask.Heal);
			}
		} else { //They're an ally or allyNPC
			opposingCharacters.addAll(enemyCharacterList);
			if(character.GetActiveStatuses().contains(StatusType.Charmed)) {
				if(allyCharacterList.contains(character))
					isAStatusControllingOurAlly = true;
				List<CharacterBase> alliesExcludingSelf = new ArrayList<CharacterBase>(allyCharacterList);
				alliesExcludingSelf.remove(character);
				if(alliesExcludingSelf.size() > 0)
					UpdateObjectiveListForCharacters(currentObjectives, alliesExcludingSelf, Objective.CharacterTask.Kill);
				else
					UpdateObjectiveListForCharacters(currentObjectives, enemyCharacterList, Objective.CharacterTask.Heal);
			} else if(npcAllyCharacterList.contains(character))
				currentObjectives = new ArrayList<Objective>(currentObjectives_npcAllyAI);
		}
		//Hijack the objectives if they've got either of these status effects
		if(character.GetActiveStatuses().contains(StatusType.Fear)) {
			if(allyCharacterList.contains(character))
				isAStatusControllingOurAlly = true;
			currentObjectives.clear();
			//Find the possible move tile that's furthest from all opposing characters
			if(character.GetPaths().size() > 0) {
				Tile targetTile = null;
				int furthestOverall = 0;
				for(Tile moveTile : character.GetPaths().keySet()) {
					int shortestDistanceToOpposition = Integer.MAX_VALUE;
					for(CharacterBase opposingChar : opposingCharacters) {
						int distance = GetDistance(moveTile.Location(), opposingChar.getLocation());
						if(distance < shortestDistanceToOpposition)
							shortestDistanceToOpposition = distance;
					}
					if(shortestDistanceToOpposition > furthestOverall) {
						furthestOverall = shortestDistanceToOpposition;
						targetTile = moveTile;
					}
				}
				Objective objective = new Objective_Tile(Objective.TargetType.Tile, -1, targetTile, Objective.TileTask.Occupy);
				currentObjectives.add(objective);
			}
		} else if(character.GetActiveStatuses().contains(StatusType.Goad)) {
			if(allyCharacterList.contains(character))
				isAStatusControllingOurAlly = true;
			currentObjectives.clear();
			//try to melee the closest opposing character
			CharacterBase target = null;
			int shortestDistanceToOpposition = Integer.MAX_VALUE;
			for(CharacterBase opposingChar : opposingCharacters) {
				int distance = GetDistance(character.getLocation(), opposingChar.getLocation());
				if(distance < shortestDistanceToOpposition) {
					shortestDistanceToOpposition = distance;
					target = opposingChar;
				}
			}
			Objective objective = new Objective_Character(Objective.TargetType.Character, -1, target.GetData(), Objective.CharacterTask.Kill);
			currentObjectives.add(objective);
		}
		
		
		System.out.println("Game.OnLingeringAnimsDone() - StartNextCharacterTurn for: " + character.GetData().getName());
		
		if(allyCharacterList.contains(character) && !isAStatusControllingOurAlly) {
			//If the turn taker is a non-npc ally then display turn options
			battlePanel.ShowActionPanel(character);
			battlePanel.ToggleIgnoreMouseEvents(false);
		} else {
			Timer thinkingTimer = new Timer(AIThinkTime, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					RunAILoop(character);
				}
			});
			thinkingTimer.setRepeats(false);
			thinkingTimer.start();
		}
	}
	
	public void EndTurn() {
		//This is new-er but I think its necessary, I couldn't find anywhere else that the input gets disabled, expect from natural or AI choices involving completed actions
		battlePanel.ToggleIgnoreMouseEvents(true);
		
		//Handle any clean up
		GetActiveBattleCharacter().OnTurnEnd();
		
		battlePanel.EndTurnCleanup();
		
		//If the turntaker shifted themselves during their turn then we wait till their turn is done and then we fuck with the turnOrderList
		if(this.pendingCharacterBaseToShift != null)
			ShiftPendingCharacterBeforeNextTurn();
		
		//Unhighlight old turn taker 
		board.GetTileAt(GetActiveBattleCharacter().getLocation()).ToggleTurnTaker(false);
		turnOrderIndex++;
		if(turnOrderIndex >= turnOrderedCharBases.size()) {
			turnOrderIndex = 0;
			turnPhases++;	
		}
		//Highlight current turn taker 
		board.GetTileAt(GetActiveBattleCharacter().getLocation()).ToggleTurnTaker(true);
		
		turnCount++;
		
		StartNextCharacterTurn();
	}
	
	//AI - Start
	
	private void EndBattle(boolean didWin) {
		System.out.println("Game.EndBattle() - didWin:" + didWin);
		
		deadCharDatas.clear();
		CharacterBase playerBase = this.FindTargetCharacterBase(saveData.playerData);
		
		if( playerBase.GetHp() <= 0
			&&
			!didWin
			&&
			GetBattleData().IsLossGameover()
		) {
			saveData.gameOvers += 1;
			
			//Show battle outcome message and then chain into a transition
			battlePanel.AnimateBattleMessageRibbon("Battle Lost", true, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					GUIManager.GetFadeTransitionPanel().Fade(true, new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							GUIManager.ShowScreen(MenuType.GAMEOVER);
							GUIManager.GetFadeTransitionPanel().Fade(false, 120);
						}
					});
				}
			});
		} else {
			//Update the ally character data with the hp and other values stored in the characterBases before they're discarded
			//also award general things, like exp
			
			//TODO Determine how much Exp each character gets
			System.err.println("Game.EndBattle() - STUB - Exp Allotment");
			float perCharExpNorm = 0.4f;
			
			//Apply player character exp
			saveData.playerData.AddExp(perCharExpNorm);
			if(playerBase.GetHp() <= 0)
				deadCharDatas.add(saveData.playerData);
			
			List<CharacterData> survivingCharDatas = Arrays.asList(saveData.teamDatas);
			for(CharacterBase charBase : allyCharacterList) {
				if(charBase == playerBase)
					continue;
				CharacterData deadCharData = null;
				for(CharacterData data : survivingCharDatas) {
					if(charBase.GetData().getId() == data.getId()) {
						if(charBase.GetHp() <= 0)
							deadCharData = data;
						else
							data.AddExp(perCharExpNorm);
						break;
					}
				}
				if(deadCharData != null) {
					survivingCharDatas.remove(deadCharData);
					deadCharDatas.add(deadCharData);	
				}
			}
			saveData.teamDatas = survivingCharDatas.stream().toArray(CharacterData[]::new);
			
			
			//Notify MapLocationPanel
			final boolean didWin_final = didWin;
			String message = didWin ? "Battle Won!" : "Battle Concluded";
			battlePanel.AnimateBattleMessageRibbon(message, false, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(didWin_final)
						battlePanel.ShowBattleResults(didWin_final);
					else
						GUIManager.MapLocationPanel().ApplyInteraction(didWin_final);
				}
			});
		}
		
		//Tell the battlePanel so it can clean up its persistent objects, like timers
		battlePanel.EndBattle();
	}
	
	/**
	 * Wait till the next battle to clear this info cause the BattleState data structure still needs the battle info during the awkward transition from End Of Battle to Start Of Next Dialography.
	 * EndBattle method is the last natural hook to clear the battle info for the battle phase ends and logic flow moves elsewhere but ApplyInteraction doesn't get called till after the FadeTransitionTimer
	 * which creates a gap in which the battle is over but hasn't been recorded yet so we'll still need our unresolvedInteractionData.BattleState to restore it but the battle data has already been purged.
	 */
	private void ClearOldBattleData() {
		allyCharacterList.clear();
		enemyCharacterList.clear();
		npcAllyCharacterList.clear();
		turnOrderedCharBases.clear();
		turnCount = 0;
		turnPhases = 0;
		turnOrderIndex = 0;
		lingeringAnimCombatEffects.clear();
	}
	
	//Characters that died in the last battle(can include the player character), the non-player charDatas in this list no longer exist in the saveData.teamDatas
	//TODO Do something with dead teammates, like showing notifcations of their deaths upon arrival to the MapLocationScreen
	List<CharacterData> deadCharDatas = new ArrayList<CharacterData>();
	public List<CharacterData> getDeadCharacters() { return deadCharDatas; }
	
	enum BattleOutcome { Pending, BattleWon, BattleLost };
	/**
	 * Tracks how many individual character turns have passed.
	 */
	private int turnCount;
	/**
	 * Gets the value that tracks how many individual character turns have passed.
	 * @return Game.turnCount
	 */
	public int getTurnCount() { return turnCount; }
	/**
	 * This value tracks how many times the turn order has returned to the beginning, i.e. when all the battle characters have taken their turns.
	 */
	private int turnPhases;
	/**
	 * Get the value that tracks how many times the turn order has returned to the beginning, i.e. when all the battle characters have taken their turns.
	 * @return Game.turnPhases
	 */
	public int getTurnPhases() { return turnPhases; }
	
	public BattleOutcome AssessObjectives() {
		BattleOutcome outcome = BattleOutcome.Pending;
		boolean areAllEnemiesDead = true;
		for(CharacterBase enemyChar : enemyCharacterList) {
			if(enemyChar.GetHp() > 0) {
				areAllEnemiesDead = false;
				break;
			}
		}
		//Create Objectives based on the battleData, if they don't exist already
		switch(currentBattleData.WinCondition().WinConditionType()) {
			case DeathMatch:
				UpdateObjectiveListForCharacters(currentObjectives_enemyAI, allyCharacterList, Objective.CharacterTask.Kill);
				
				if(areAllEnemiesDead)
					outcome = BattleOutcome.BattleWon;
				break;
			case Assassination:
				System.err.println("Stub for Game.AssessObjectives() - STUB - Assassination condition");
				break;
			case ProtectAllies:
				System.err.println("Stub for Game.AssessObjectives() - STUB - ProtectAllies condition");
				break;
			case OccupyTile:
				System.err.println("Stub for Game.AssessObjectives() - STUB - OccupyTile condition");
				break;
			case SurviveForTime:
				UpdateObjectiveListForCharacters(currentObjectives_enemyAI, allyCharacterList, Objective.CharacterTask.Kill);
				
				if(areAllEnemiesDead || (currentObjectives_enemyAI.size() > 0 && turnPhases >= currentBattleData.WinCondition().TurnsToSurvive()))
					outcome = BattleOutcome.BattleWon;
				break;
			default:
				System.err.println("Game.StartNextCharacterTurn() - Add support for: WinConditionType." + currentBattleData.WinCondition().WinConditionType().toString());
				break;
		}
		
		//This conditional allows a technical draw to be considered a win, if that scenario ever occurs
		if(outcome != BattleOutcome.BattleWon && currentObjectives_enemyAI.size() == 0)
			outcome = BattleOutcome.BattleLost;
		
		return outcome;
	}
	
	private void UpdateObjectiveListForCharacters(List<Objective> listToUpdate, List<CharacterBase> characterTargets, Objective.CharacterTask characterTask) {
		for(CharacterBase target : characterTargets) {
			//If we already have an objective to kill this ally then continue
			Objective[] matchingObjectives = listToUpdate.stream().filter(x ->
					((CharacterData)x.GetTarget()).getId() == target.GetData().getId()
					&&
					(Objective.CharacterTask)x.GetTask() == characterTask)
			.toArray(Objective[]::new);
			switch(characterTask) {
				case Kill:
					for(Objective objective : matchingObjectives) {
						if(target.GetHp() <= 0)
							listToUpdate.remove(objective);
					}
					if(matchingObjectives.length > 0)
						continue;
					
					if(target.GetHp() > 0) {
						System.out.println("Game.StartNextCharacterTurn() : (WinCondition: SurviveForTime) - Creating Kill Objective for: " + target.GetData().getName());
						Objective objective = new Objective_Character(Objective.TargetType.Character, -1, target.GetData(), Objective.CharacterTask.Kill);
						listToUpdate.add(objective);
					}
					break;
				case Heal:
					System.err.println("Game.UpdateObjectiveListForCharacters() - STUB - Handle Heal CharacterTask.");
					break;
				case Surround:
					System.err.println("Game.UpdateObjectiveListForCharacters() - STUB - Handle Surround CharacterTask.");	
					break;
				case Cover:
					System.err.println("Game.UpdateObjectiveListForCharacters() - STUB - Handle Cover CharacterTask.");
					break;
				default:
					System.err.println("Game.UpdateObjectiveListForCharacters() - Add support for Objective.CharacterTask: " + characterTask);
					break;
			}
		}
	}
	
	private void UpdateObjectiveListForTiles(List<Objective> listToUpdate, List<Tile> tileTargets, Objective.TileTask tileTask) {
		for(Tile target : tileTargets) {
			//If we already have an objective to kill this ally then continue
			Objective[] matchingObjectives = listToUpdate.stream().filter(x ->
					((Tile)x.GetTarget()).Location() == target.Location()
					&&
					(Objective.TileTask)x.GetTask() == tileTask)
			.toArray(Objective[]::new);
			switch(tileTask) {
				case Surround:
					System.err.println("Game.UpdateObjectiveListForTiles() - STUB - Handle Surround TileTask.");
					break;
				case Occupy:
					System.err.println("Game.UpdateObjectiveListForTiles() - STUB - Handle Occupy TileTask.");			
					break;
				default:
					System.err.println("Game.UpdateObjectiveListForTiles() - Add support for Objective.TileTask: " + tileTask);
					break;
			}
		}
	}
	
	//Store objective data for use across mutliple methods separated by animation and periods of time the AI is simulating the act of "thinking"
	List<ObjectiveAnalysis> analysisList = new ArrayList<ObjectiveAnalysis>();
	
	//DEBUGGING
	private boolean DEBUG_makeAILifeless = true;
	
	private void RunAILoop(CharacterBase turnTaker) {
		//DEBUGGING
		if(DEBUG_makeAILifeless) {
			System.err.println("DEBUGGING @ Game.RunAILoop() - AI is lifeless.");
			chosenAction = new AIAction(null, ObjectiveType.Wait, null, null, null);
			DoNextAIAction();
			return;
		}
		
		
		//This bool is necessary because the AI logic will most likely be run for ally characters with control-inhibiting status effects such as Fear, Goad and Charm.
		boolean isTheTurnTakerAnAI = !allyCharacterList.contains(turnTaker);
		
		//if(turnTaker.HasMoved() && turnTaker.HasUsedAction()) {
		//I dont think its helpful to ALWAYS make the AI move, they should only do so if theres an advantage to doing so or if they're complied by a Status effect.
		if(turnTaker.HasUsedAction() || (!turnTaker.CanPerformAction(ObjectiveType.Attack, isTheTurnTakerAnAI) && !turnTaker.CanPerformAction(ObjectiveType.Heal, isTheTurnTakerAnAI)) ) {
			
			chosenAction = new AIAction(null, ObjectiveType.Wait, null, null, null);
			DoNextAIAction();
			return;
		}
		
		
		//Carry out the objectives
		analysisList.clear();
		//Is this character assigned to an objective exclusively
		if(currentObjectives.stream().anyMatch(x -> x.getAssignedExclusivePersonnel().contains(turnTaker))) {
			//Only carry out objectives exlusive to this character
			System.err.println("Game.RunAILoop() - STUB - Handle exclusive objectives.");
			
			
			return;
		}
		
		//Otherwise carry out objectives generally, with a priority according to the order of currentObjectives, index 0 being the highest priority
			
		//Collect data about the first objective and then compare that data with the following objectives in order to cross reference aspects of them
		//like if the first objective is to attack an enemy then record the reachable tiles that'll be in range of the target and then figure our where you'd need to move
		//in order to accomplish the second objective, then if those were the only objectives move on to cross referencing the target move locations for objective #1
		//with the target move locations for objective #2. If there is a shared tile among them then move to that tile so that both objectives are satisfied with a single action
		
		for(Objective objective : currentObjectives) {
			System.out.println("Game.RunAILoop() - Looping over Objective: " +
					(objective.ObjectiveType() == TargetType.Character ? ((Objective.CharacterTask)objective.GetTask()).toString() : ((Objective.TileTask)objective.GetTask()).toString()));
			
			ObjectiveAnalysis analysis = new ObjectiveAnalysis();
			switch(objective.objectiveType) {
				case Character:
					CharacterBase target = FindTargetCharacterBase((CharacterData)objective.GetTarget());
					
					switch((Objective.CharacterTask)objective.GetTask()) {
						case Kill:
							
							//if(!turnTaker.HasMoved()) {
							if(!turnTaker.HasMoved() && turnTaker.CanMove()) {
								
								List<Tile> adjacentOrClosestMoves = GetAdjacentOrClosestTiles(turnTaker, target.getLocation());
								for(Tile adjacentOrClosestMove : adjacentOrClosestMoves)
									System.out.println("Game.RunAILoop() - adjacentOrClosestMove: " + adjacentOrClosestMove.Location());
								analysis.objectiveMoves.addAll(adjacentOrClosestMoves);
								
								if(analysis.objectiveMoves.size() == 0) {
									System.err.println("Game.RunAILoop() - Failed to find any Adjacent or close tiles!");
									break;
								}
							}
							
							//Decide action based on distance to target and available attack actions(MainAttack, Ability or damaging BattleItem)
							/*if(turnTaker.GetMaxRangeForAction(ObjectiveType.Attack) >= distanceToTarget) {
								System.out.println("Game.RunAILoop(CharacterBase turnTaker) - turnTaker.GetMaxRangeForAction(ActionType.Attack): " + turnTaker.GetMaxRangeForAction(ObjectiveType.Attack) +
												   " >= distanceToTarget: " + distanceToTarget);
								
								analysis.actionTargetTile = board.GetTileAt(target.getLocation());
								analysis.objectiveType = ObjectiveType.Attack;
							} else {
								analysis.objectiveType = ObjectiveType.Wait;
							}*/
							if(turnTaker.CanPerformAction(ObjectiveType.Attack, isTheTurnTakerAnAI)) {
								
								System.out.println("((((( )))))) CAN Perform Attack Action");
								
								List<Tile> availableMoves = new ArrayList<Tile>();
								
								//if(!turnTaker.HasMoved())
								if(!turnTaker.HasMoved() && turnTaker.CanMove())
									
									availableMoves.addAll( turnTaker.GetPaths().keySet() );
								
								List<ActionOptionInfo> attackOptionInfos = turnTaker.GetAttackOptionInfos(board.GetTileAt(turnTaker.getLocation()), availableMoves, target.getLocation(), isTheTurnTakerAnAI);
								if(attackOptionInfos.size() > 0) {
									analysis.attackOptionInfos = attackOptionInfos;
									analysis.actionTargetTile = board.GetTileAt(target.getLocation());
									analysis.objectiveType = ObjectiveType.Attack;
								} else {
									analysis.objectiveType = ObjectiveType.Wait;
								}
							} else {
								
								System.out.println("))))) ((((( CANNOT Perform Attack Action");
								
								analysis.objectiveType = ObjectiveType.Wait;
							}
							
							break;
						case Heal:
							System.err.println("Game.RunAILoop() - STUB - Write logic for Heal objective.");
							break;
						case Surround:
							System.err.println("Game.RunAILoop() - STUB - Write logic for Surround objective.");
							break;
						case Cover:
							System.err.println("Game.RunAILoop() - STUB - Write logic for Cover objective.");
							break;
						default:
							System.err.println("Game.RunAILoop() - Add support for: Objective.CharacterTask." + ((Objective.CharacterTask)objective.GetTask()).toString());
							break;
					}
					
					break;
				case Tile:
					Tile targetTile = (Tile)objective.GetTarget();
					
					switch((Objective.TileTask)objective.GetTask()) {
						case Surround:
							List<Tile> adjacentOrClosestMoves = GetAdjacentOrClosestTiles(turnTaker, targetTile.Location());
							analysis.objectiveMoves.addAll(adjacentOrClosestMoves);
							analysis.objectiveType = ObjectiveType.Wait;
							break;
						case Occupy:
							analysis.objectiveMoves.add(targetTile);
							analysis.objectiveType = ObjectiveType.Wait;
							break;
						default:
							System.err.println("Game.RunAILoop() - Add support for: Objective.TileTask." + ((Objective.TileTask)objective.GetTask()).toString());
							break;
					}
					
					break;
				default:
					System.err.println("Game.RunAILoop() - Add support for: objective.objectiveType." + objective.objectiveType.toString());
					break;
			}
			analysisList.add(analysis);
			//end of objective for loop
		}
		//If there are no objectives then just wait
		if(currentObjectives.size() == 0) {
			System.out.println("Game.RunAILoop() - Character has no objectives.");
			ObjectiveAnalysis blankObjective = new ObjectiveAnalysis();
			blankObjective.objectiveType = ObjectiveType.Wait;
			if(analysisList.size() == 0)
				analysisList.add(blankObjective);
		}
		
		//Debug the AI moves
		for(ObjectiveAnalysis obAn : analysisList) {
			for(Tile tile : obAn.objectiveMoves) {
				tile.LayerAIObjectiveMoves(true);
			}
		}
		
		
		ObjectiveAnalysis plannedAction = new ObjectiveAnalysis();
		if(!turnTaker.HasUsedAction()) {
			//Find synonmous action and shared target
			//Compare different objective data, stores tiles and their associated analysisList indices
			class CombinedAction {
				public CombinedAction(ObjectiveAnalysis accomodatingAnalysis, ActionOptionInfo actionOptionInfo, ObjectiveAnalysis[] constituentAnalyses) {
					accomodatingAction = accomodatingAnalysis;
					this.actionOptionInfo = actionOptionInfo;
					for(ObjectiveAnalysis analysis : constituentAnalyses)
						constituentActions.add(analysis);
				}
				//This is the version of the action that satisfies all constituent actions
				public ObjectiveAnalysis accomodatingAction;
				public ActionOptionInfo actionOptionInfo;
				public List<ObjectiveAnalysis> constituentActions = new ArrayList<ObjectiveAnalysis>();
			}
			List<CombinedAction> combinableActions = new ArrayList<CombinedAction>();
			
			/*for(int i = 0; i < analysisList.size(); i++) {
				ObjectiveType examinedActionType = analysisList.get(i).objectiveType;
				
				//is this action a singularly targeted one or can this unit affect multiple tiles/targets
				//the ActionType is also indirectly addressed here as CharacterBase.GetMaxAOERangeForAction() will return -1 for ActionTypes that aren't relevant to the idea of combinable objectives
				int maxAOERange = turnTaker.GetMaxAOERangeForAction(examinedActionType);
				if(maxAOERange > 0) {
					//they can hit multiple tiles, check all possible target arrangements for combinable actions
					//get all the possible target tiles
					Point targetLocation = analysisList.get(i).actionTargetTile.Location();
					List<Tile> possibleTargetTiles = new ArrayList<Tile>();
					for(int y = targetLocation.y - maxAOERange; y <= targetLocation.y + maxAOERange; y++) {
						for(int x = targetLocation.x - maxAOERange; x <= targetLocation.x + maxAOERange; x++) {
							Point loc = new Point(x, y);
							if(GetDistance(loc, targetLocation) > maxAOERange)
								continue;
							Tile possibleTile = board.GetTileAt(loc);
							if(possibleTile == null)
								continue;
							possibleTargetTiles.add(possibleTile);
						}
					}
					final int finalI = i;
					for(Tile targetTile : possibleTargetTiles) {
						//check all the affected tiles and see if these affected tiles satisfy any of the other analyses
						int distanceToTarget = GetDistance(targetTile.Location(), turnTaker.getLocation());
						int attackRange = turnTaker.GetMaxRangeForAction(examinedActionType) + maxAOERange;
						List<Tile> moveOptions = new ArrayList<Tile>();
						if(!turnTaker.HasMoved()) {
							for(Tile moveTile : turnTaker.GetPaths().keySet()) {
								int otherDistance = GetDistance(moveTile.Location(), targetTile.Location());
								if(otherDistance <= attackRange)
									moveOptions.add(moveTile);
							}
						}
						if(distanceToTarget > attackRange && moveOptions.size() == 0)
							continue;
						
						//Investigate other possible analyses in AOE
						for(int y = targetTile.Location().y - maxAOERange; y <= targetTile.Location().y + maxAOERange; y++) {
							for(int x = targetTile.Location().x - maxAOERange; x <= targetTile.Location().x + maxAOERange; x++) {
								Point loc = new Point(x, y);
								if(GetDistance(loc, targetTile.Location()) > maxAOERange)
									continue;
								Tile possibleTile = board.GetTileAt(loc);
								if(possibleTile == null)
									continue;

								ObjectiveAnalysis[] otherSatisfiedAnalyses = analysisList.stream().filter(a ->
										a.actionTargetTile.Location().equals(loc)
										&&
										a.objectiveType == examinedActionType
										&&
										analysisList.indexOf(a) != finalI
									).toArray(ObjectiveAnalysis[]::new);
								if(otherSatisfiedAnalyses.length > 0) {
									ObjectiveAnalysis accomodatingAction = new ObjectiveAnalysis(moveOptions, examinedActionType, targetTile);
									combinableActions.add(new CombinedAction(accomodatingAction, otherSatisfiedAnalyses));
								}
							}
						}
					}
				}
			}*/
			
			class TargetingInfo {
				public TargetingInfo(Point targetPoint, List<Point> targetArea, List<Tile> applicableMoveTiles) {
					this.targetPoint = targetPoint;
					this.targetArea = targetArea;
					this.applicableMoveTiles = applicableMoveTiles;
				}
				Point targetPoint;
				List<Point> targetArea;
				List<Tile> applicableMoveTiles;
			}
			
			//This block has been revamped to more specifically analyze the combinability of specific actions. 
			//Is this action a singularly targeted one or can this unit affect multiple tiles/targets.
			for(int i = 0; i < analysisList.size(); i++) {
				ObjectiveType examinedActionType = analysisList.get(i).objectiveType;
				
				//Because statuses may prevent attack options altogether we must ignore these analyses
				if(analysisList.get(i).attackOptionInfos == null)
					continue;
				
				final int finalI = i;
				for(ActionOptionInfo actionOptionInfo : analysisList.get(i).attackOptionInfos) {
					if(actionOptionInfo.aoe <= 0)
						continue;
					
					List<TargetingInfo> targetingInfos = new ArrayList<TargetingInfo>();
					//Find all tiles surrounding the objective that are within aoe range of it
					Point objectiveLocation = analysisList.get(i).actionTargetTile.Location();
					for(int y = objectiveLocation.y - actionOptionInfo.aoe; y <= objectiveLocation.y + actionOptionInfo.aoe; y++) {
						for(int x = objectiveLocation.x - actionOptionInfo.aoe; x <= objectiveLocation.x + actionOptionInfo.aoe; x++) {
							Point loc = new Point(x, y);
							if(GetDistance(loc, objectiveLocation) > actionOptionInfo.aoe)
								continue;
							Tile possibleTile = board.GetTileAt(loc);
							if(possibleTile == null)
								continue;
							
							//Get all tiles in range
							List<Point> pointsInRange = new ArrayList<Point>();
							pointsInRange.add(loc);
							int remainingRadiusCycles = actionOptionInfo.aoe;
							while(remainingRadiusCycles > 0) {
								List<Point> newPoints = new ArrayList<Point>();
								for(Point pointInRange : pointsInRange) {
									for(Tile surroundingTile : board.GetSurroundingTiles(pointInRange)) {
										if(!pointsInRange.contains(surroundingTile.Location()))
											newPoints.add(surroundingTile.Location());
									}
								}
								pointsInRange.addAll(newPoints);
								remainingRadiusCycles--;
							}
							
							//Get all applicableMoves
							List<Tile> applicableMoves = new ArrayList<Tile>();
							for(Tile moveTile : actionOptionInfo.possibleMoveLocations) {
								int distanceBetweenMoveAndTarget = GetDistance(loc, moveTile.Location());
								if(distanceBetweenMoveAndTarget <= actionOptionInfo.rangeMax && distanceBetweenMoveAndTarget >= actionOptionInfo.rangeMin)
									applicableMoves.add(moveTile);
							}
							
							targetingInfos.add(new TargetingInfo(loc, pointsInRange, applicableMoves));
						}
					}
					
					for(TargetingInfo targetingInfo : targetingInfos) {
						//Investigate other possible analyses in AOE
						for(Point radiusPoint : targetingInfo.targetArea) {
							ObjectiveAnalysis[] otherSatisfiedAnalyses = analysisList.stream().filter(a ->
									a.actionTargetTile.Location().equals(radiusPoint)
									&&
									a.objectiveType == examinedActionType
									&&
									analysisList.indexOf(a) != finalI
								).toArray(ObjectiveAnalysis[]::new);
							if(otherSatisfiedAnalyses.length > 0) {
								ObjectiveAnalysis accomodatingAction = new ObjectiveAnalysis(
									targetingInfo.applicableMoveTiles,
									examinedActionType,
									board.GetTileAt(targetingInfo.targetPoint),
									targetingInfo.targetArea
								);
								combinableActions.add(new CombinedAction(accomodatingAction, actionOptionInfo, otherSatisfiedAnalyses));
							}
						}
					}
				}
			}
			
			if(combinableActions.size() > 0) {
				//Pick the combinable action that satisfies greatest number of objectives
				int greatestUtility = 0;
				CombinedAction mostUtilitarianCombinedAction = null;
				for(CombinedAction combinedAction : combinableActions) {
					int listSize = combinedAction.constituentActions.size();
					if(listSize > greatestUtility) {
						greatestUtility = listSize;
						mostUtilitarianCombinedAction = combinedAction;
					}
				}
				plannedAction = mostUtilitarianCombinedAction.accomodatingAction;
				System.out.println("Game.RunAILoop() : (Action Block) - COMBINED action choice: " + plannedAction.objectiveType);
				
				
				//Picking the most utilitarian attackOption from the most UtilitarianCombinedAction
				ActionOptionInfo chosenAttackAction = mostUtilitarianCombinedAction.actionOptionInfo;
				String actionDesc = "MainAttack Action";
				if(chosenAttackAction.ability != null)
					actionDesc = chosenAttackAction.ability.name + " Ability Action";
				else if(chosenAttackAction.itemData != null)
					actionDesc = chosenAttackAction.itemData.getName() + " Item Action";
				System.out.println("Game.RunAILoop() - Chosen Attack Action: " + actionDesc);
				plannedAction.AIsChosenAttackAction = chosenAttackAction;
				
				
			} else {
				plannedAction = analysisList.get(0);
				System.out.println("Game.RunAILoop() : (Action Block) - SINGULAR action choice: " + plannedAction.objectiveType);
				
				
				//Chose the attackOption for the singular plannedAction
				ActionOptionInfo chosenAttackAction = null;
				if(plannedAction.attackOptionInfos != null) {
					System.err.println("Game.RunAILoop() - (Non-Combinable Action) AI is choosing an attack option at random. Apply more intelligent logic to this decision; at some point.");
					chosenAttackAction = plannedAction.attackOptionInfos.get( this.GetCharacterActionRandom().nextInt(plannedAction.attackOptionInfos.size()) );
					String actionDesc = "MainAttack Action";
					if(chosenAttackAction.ability != null)
						actionDesc = chosenAttackAction.ability.name + " Ability Action";
					else if(chosenAttackAction.itemData != null)
						actionDesc = chosenAttackAction.itemData.getName() + " Item Action";
					System.out.println("Game.RunAILoop() - Chosen Attack Action: " + actionDesc);
				}
				plannedAction.AIsChosenAttackAction = chosenAttackAction;
			}
		}
		
		Tile moveTile = null;
		
		//if(!turnTaker.HasMoved()) {
		if(!turnTaker.HasMoved() && turnTaker.CanMove()) {
			
			//check
			if(plannedAction.objectiveMoves != null && plannedAction.objectiveMoves.size() > 0) {
				//1. if we need to move first to achieve our action goal
				//if so then look at our moveTiles in the plannedAction and see if there are any other objectives we can milk out of them or tactics we can apply to them and then pick one
				
				//re-iterate thru the analyses and cross-reference wait action's objectiveMoves with our plannedAction.objectiveMoves
				Map<Tile, List<ObjectiveAnalysis>> movesWithMultipleObjectives = new HashMap<Tile, List<ObjectiveAnalysis>>();
				for(Tile plannedMove : plannedAction.objectiveMoves) {
					for(int i = 0; i < analysisList.size(); i++) {
						List<Tile> otherMoves = analysisList.get(i).objectiveMoves;
						switch(analysisList.get(i).objectiveType) {
							case Attack: case Heal:
								//ignore these types because they've already been analyzed above
								break;
							case Wait:
								if(otherMoves != null && otherMoves.size() > 0 && otherMoves.contains(plannedMove)) {
									List<ObjectiveAnalysis> objectiveList = new ArrayList<ObjectiveAnalysis>();
									if(movesWithMultipleObjectives.containsKey(plannedMove))
										objectiveList = movesWithMultipleObjectives.get(plannedMove);
									objectiveList.add(analysisList.get(i));
									movesWithMultipleObjectives.put(plannedMove, objectiveList);
								}
								break;
							default:
								System.err.println("Game.RunAILoop() - Add support for: " + analysisList.get(i).objectiveType);
								break;
						}
					}
				}
				//iterate movesByObjectives and count up how many objectives are involved for each tile by iterating the analysesWithMatchingMoves
				//and choose the moveTile that satisfies the greatest number of objectives
				int largestObjectiveCount = 0;
				Tile bestMultiMove = null;
				for(Tile key : movesWithMultipleObjectives.keySet()) {
					int count = movesWithMultipleObjectives.get(key).size();
					if(count > largestObjectiveCount) {
						largestObjectiveCount = count;
						bestMultiMove = key;
					}
				}
				//If there is a move choice that satifies multiple objectives then choose that one
				if(bestMultiMove != null)
					moveTile = bestMultiMove;
				else
					moveTile = AIPickTileClosestToTargetsBackfacingOrFirstMove(turnTaker, analysisList.get(0));
			} else {
				//or
				//2. if we've already used our action this turn or didn't find a reachable action
				//if so then we should be able to use the below algorithym to choose a move
			
				//Compare different objective data, stores tiles and their associated analysisList indices
				Map<Tile, List<Integer>> synonomousMoveMap = new HashMap<Tile, List<Integer>>();
				for(int tier = 0; tier < analysisList.size() - 1; tier++) {
					for(int i = tier + 1; i < analysisList.size(); i++) {
						int other = i;
						Tile[] matchingTiles = analysisList.get(tier).objectiveMoves.stream().filter(x -> analysisList.get(other).objectiveMoves.contains(x)).toArray(Tile[]::new);
						if(matchingTiles != null && matchingTiles.length > 0) {
							for(Tile matchingTile : matchingTiles) {
								List<Integer> indices = new ArrayList<Integer>(synonomousMoveMap.get(matchingTile));
								if(!indices.contains((Integer)tier))
									indices.add((Integer)tier);
								if(!indices.contains((Integer)other))
									indices.add((Integer)other);
								synonomousMoveMap.put(matchingTile, indices);
							}
							
						}
					}
				}
				
				if(synonomousMoveMap.size() > 0) {
					//Move to the tile that satisfies greatest number of objectives
					int greatestUtility = 0;
					Tile mostUtilitarianMove = null;
					for(Tile keyTile : synonomousMoveMap.keySet()) {
						int listSize = synonomousMoveMap.get(keyTile).size();
						if(listSize > greatestUtility) {
							greatestUtility = listSize;
							mostUtilitarianMove = keyTile;
						}
					}
					moveTile = mostUtilitarianMove;
				} else {
					//Move to the first tile for the first objective
					if(analysisList.size() == 0 || analysisList.get(0).objectiveMoves.size() == 0) {
						System.out.println("Game.RunAILoop() - There are no objective moves.");
					} else {
						
						for(Tile objectiveMove : analysisList.get(0).objectiveMoves)
							System.out.println("Game.RunAILoop() - objectiveMove.Loc: " + objectiveMove.Location());
						
						moveTile = AIPickTileClosestToTargetsBackfacingOrFirstMove(turnTaker, analysisList.get(0));
					}
				}
			}
		}
		
		
		//Think about it
		System.out.println("Game.RunAILoop() - Ending with moveTile: " + moveTile);
		chosenAction = new AIAction(moveTile, plannedAction.objectiveType, plannedAction.actionTargetTile, plannedAction.targetArea, plannedAction.AIsChosenAttackAction);
		DoNextAIAction();
	}
	
	/**
	 * Instead of just picking the first element of the list lets pick the tile at the target enemy's back first or the sides or finally the front, if this objective is for an enemy. If it isn't an
	 * enemy then the first objectiveMove in the list is chosen.
	 * @return Tile
	 */
	private Tile AIPickTileClosestToTargetsBackfacingOrFirstMove(CharacterBase turnTaker, ObjectiveAnalysis chosenAnalysis) {
		//String debugMessage = null;
		//Point debugTargetsDirection = null;
		//String moves = "";
		//for(Tile tile : chosenAnalysis.objectiveMoves)
		//	moves += tile.Location() + ", ";
		//System.out.println("AI ** available moves: " + moves);
		
		Tile moveTile = null;
		switch(chosenAnalysis.objectiveType) {
			case Attack:
				CharacterBase targetChar = chosenAnalysis.actionTargetTile.Occupant();
				Point locationAtBack = new Point(targetChar.getLocation().x - targetChar.getDirection().x, targetChar.getLocation().y + targetChar.getDirection().y);
				
				if(turnTaker.getLocation().equals(locationAtBack)) {
					moveTile = null;
					break;
				}
				
				boolean isFacingXAxis = targetChar.getDirection().x != 0;
				Point sideLoc1 = new Point(targetChar.getLocation().x + (!isFacingXAxis ? 1 : 0), targetChar.getLocation().y + (isFacingXAxis ? 1 : 0));
				Point sideLoc2 = new Point(targetChar.getLocation().x - (!isFacingXAxis ? 1 : 0), targetChar.getLocation().y - (isFacingXAxis ? 1 : 0));
				if(chosenAnalysis.objectiveMoves.stream().anyMatch(x -> x.Location().equals(locationAtBack))) {
					moveTile = board.GetTileAt(locationAtBack);
					//debugMessage = "At Back";
				} else if(chosenAnalysis.objectiveMoves.stream().anyMatch(x -> x.Location().equals(sideLoc1))) {
					moveTile = board.GetTileAt(sideLoc1);
					//debugMessage = "At Left Side";
				} else if(chosenAnalysis.objectiveMoves.stream().anyMatch(x -> x.Location().equals(sideLoc2))) {
					moveTile = board.GetTileAt(sideLoc2);
					//debugMessage = "At Right Side";
				} else {
					moveTile = chosenAnalysis.objectiveMoves.get(0);
					//debugMessage = "At Front";
				}
				//debugTargetsDirection = targetChar.getDirection();
				
				if(turnTaker.getLocation().equals(moveTile.Location()))
					moveTile = null;
				
				break;
			default:
				moveTile = chosenAnalysis.objectiveMoves.get(0);
				break;
		}
		
		//System.out.println("AI ** Game.AIPickTileClosestToTargetsBackfacingOrFirstMove() - chosenTile: " + moveTile.Location().toString() +
		//		", target's direction: " + debugTargetsDirection +
		//		", chosen position: " + debugMessage);
		
		return moveTile;
	}
	
	public List<ItemData> GetBattleItemsFromInventory() {
		List<ItemData> relevantInventory = new ArrayList<ItemData>();
		for(ItemData itemData : Game.Instance().GetInventory()) {
			boolean isItemUsable = false;
			if(itemData.itemUseTypes() != null) {
				for(ItemType itemType : itemData.itemUseTypes()) {
					if(itemType == ItemType.BattleItem) {
						isItemUsable = true;
						break;
					}
				}
			} else if(itemData.getType() == ItemType.BattleItem) {
				isItemUsable = true;
			}
			if(isItemUsable)
				relevantInventory.add(itemData);
		}
		return relevantInventory;
	}
	
	
	public CharacterBase FindTargetCharacterBase(CharacterData characterData) {
		CharacterBase target = null;
		String targetID = characterData.getId();
		//Search thru all the characterBase lists until the matching characterData is found
		List<CharacterBase> allBases =  new ArrayList<CharacterBase>();
		allBases.addAll(allyCharacterList);
		allBases.addAll(enemyCharacterList);
		allBases.addAll(npcAllyCharacterList);
		for(CharacterBase base : allBases) {
			if(base.GetData().getId().equals(targetID)) {
				target = base;
				break;
			}
		}
		return target;
	}
	
	public CharacterBase FindTargetCharacterBase(String ID) {
		CharacterBase target = null;
		//Search thru all the characterBase lists until the matching characterData is found
		List<CharacterBase> allBases =  new ArrayList<CharacterBase>();
		allBases.addAll(allyCharacterList);
		allBases.addAll(enemyCharacterList);
		allBases.addAll(npcAllyCharacterList);
		for(CharacterBase base : allBases) {
			if(base.GetData().getId().equals(ID)) {
				target = base;
				break;
			}
		}
		return target;
	}
	
	private int AIThinkTime = 1000;
	
	private void AIMoveDoneCallback() {	
		System.out.println("Game.AIMoveDoneCallback()");
		
		//reassess objectives and decide next action
		RunAILoop(GetActiveBattleCharacter());
	}
	
	public void AIAttackDoneCallback() {	
		System.out.println("Game.AIAttackDoneCallback()");
		
		//reassess objectives and decide next action
		RunAILoop(GetActiveBattleCharacter());
	}
	
	public void AIItemDoneCallback() {	
		System.out.println("Game.AIItemDoneCallback()");
		
		//reassess objectives and decide next action
		RunAILoop(GetActiveBattleCharacter());
	}
	
	
	
	private void DoNextAIAction() {
		CharacterBase turnTaker = GetActiveBattleCharacter();
		
		Timer actionTimer = new Timer(AIThinkTime, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(chosenAction.moveTile != null && !turnTaker.HasMoved()) {
					board.MoveAI(turnTaker, chosenAction.moveTile);
				} else {
					//Carry out actions decribed by 
					switch(chosenAction.objectiveType) {
						case Attack:
							List<CharacterBase> targets = new ArrayList<CharacterBase>();
							if(chosenAction.targetArea != null) {
								for(Point areaPoint : chosenAction.targetArea) {
									if(board.GetTileAt(areaPoint).Occupant() != null)
										targets.add(board.GetTileAt(areaPoint).Occupant());
								}
							} else
								targets.add(chosenAction.actionTargetTile.Occupant());
							if(chosenAction.actionOptionInfo.ability != null) {
								CreateTargetInfos(InteractiveActionType.Ability, chosenAction.actionOptionInfo.ability, null, targets);
								DoAbility(turnTaker, chosenAction.actionOptionInfo.ability, targets);
							} else if(chosenAction.actionOptionInfo.itemData != null) {
								CreateTargetInfos(InteractiveActionType.UseItem, null, chosenAction.actionOptionInfo.itemData, targets);
								DoItem(turnTaker, chosenAction.actionOptionInfo.itemData, targets);
							} else {
								CreateTargetInfos(InteractiveActionType.MainAttack, null, null, targets);
								DoAttack(turnTaker, targets);
							}
							break;
						case Heal:
							//This should be synonomous with Healing logic
							targets = new ArrayList<CharacterBase>();
							if(chosenAction.targetArea != null) {
								for(Point areaPoint : chosenAction.targetArea) {
									if(board.GetTileAt(areaPoint).Occupant() != null)
										targets.add(board.GetTileAt(areaPoint).Occupant());
								}
							} else
								targets.add(chosenAction.actionTargetTile.Occupant());
							if(chosenAction.actionOptionInfo.ability != null) {
								CreateTargetInfos(InteractiveActionType.Ability, chosenAction.actionOptionInfo.ability, null, targets);
								DoAbility(turnTaker, chosenAction.actionOptionInfo.ability, targets);
							} else if(chosenAction.actionOptionInfo.itemData != null) {
								CreateTargetInfos(InteractiveActionType.UseItem, null, chosenAction.actionOptionInfo.itemData, targets);
								DoItem(turnTaker, chosenAction.actionOptionInfo.itemData, targets);
							} else {
								System.err.println("Game.DoNextAIAction() - Somehow we're being expected to heal without any Ability or ItemData to use?!");
							}
							break;
						case Wait:
							PickAIDirection(turnTaker);
							EndTurn();
							break;
						default:
							System.err.println("DoNextAIAction().actionTimer - Add support for: " + chosenAction.objectiveType);
							break;
					}
				}
			}
		});
		actionTimer.setRepeats(false);
		actionTimer.start();
	}
	
	private void PickAIDirection(CharacterBase turnTaker) {
		//Pick direction facing the closest to the first objective target
		Point targetLocation = null;
		switch(currentObjectives_enemyAI.get(0).ObjectiveType()) {
			case Character:
				targetLocation = FindTargetCharacterBase((CharacterData)currentObjectives_enemyAI.get(0).GetTarget()).getLocation();
				break;
			case Tile:
				targetLocation = ((Tile)currentObjectives_enemyAI.get(0).GetTarget()).Location();
				break;
			default:
				System.out.println("Game.AISetDirection() - Add support for: " + currentObjectives_enemyAI.get(0).ObjectiveType());
				break;
		}
		Point targetDirection = GetDirection(turnTaker.getLocation(), targetLocation);
		Point bestDirection = null;
		int smallestOffset = 4;
		List<Tile> surroundingTiles = board.GetSurroundingTiles(turnTaker.getLocation());
		for(int i = 0; i < surroundingTiles.size(); i++) {
			Point directionToOption = GetDirection(turnTaker.getLocation(), surroundingTiles.get(i).Location());
			int offset = GetAlignmentOffset(targetDirection, directionToOption);
			if(offset < smallestOffset) {
				bestDirection = directionToOption;
				smallestOffset = offset;
			}
		}
		board.SetAIDirection(bestDirection);
	}
	
	Random characterActionRand = new Random();
	public Random GetCharacterActionRandom() { return characterActionRand; }
	
	public class TargetInfo {
		public TargetInfo(CharacterBase target, double chanceToHit, CombatCalcInfo combatCalcInfo) {
			this.target = target;
			this.chanceToHit = chanceToHit;
			this.combatCalcInfo = combatCalcInfo;
		}
		public CharacterBase target;
		public double chanceToHit;
		public CombatCalcInfo combatCalcInfo;
	}
	/**
	 * The list created when OnFocusTile is triggered on a tile from which there are valid targets in range of the current action.
	 */
	List<TargetInfo> currentTargetInfos = new ArrayList<TargetInfo>();
	public List<TargetInfo> getCurrentTargetInfos() { return currentTargetInfos; }
	
	/**
	 * This is now a convenience method that leverages GetTargetInfoInstances to populate our member list currentTargetInfos, which is used throughout the battle UI and combat anim logic.
	 * @param actionType
	 * @param ability
	 * @param itemData
	 * @param newTargets
	 */
	public void CreateTargetInfos(InteractiveActionType actionType, Ability ability, ItemData itemData, List<CharacterBase> newTargets) {
		currentTargetInfos.clear();
		currentTargetInfos = GetTargetInfoInstances(GetActiveBattleCharacter(), actionType, ability, itemData, newTargets);
	}
	
	/**
	 * This returns a TargetInfo for each of the CharacterBases in newTargets.
	 */
	/*public List<TargetInfo> GetTargetInfoInstances(CharacterBase attacker, InteractiveActionType actionType, Ability ability, ItemData itemData, List<CharacterBase> newTargets) {
		List<TargetInfo> targetInfos = new ArrayList<TargetInfo>();
		
		for(CharacterBase defenderBase : newTargets) {
			CharacterBase defender = defenderBase;
			CombatCalcInfo combatCalcInfo = null;
			double hitChance = 0f;
			AbilityManager AMI = new AbilityManager();
			boolean isRangedAttack = Point.distance(attacker.getLocation().getX(), attacker.getLocation().getY(), defender.getLocation().getX(), defender.getLocation().getY()) > 1.0;
			List<CombatEffect> combatEffects = new ArrayList<CombatEffect>();
			switch(actionType) {
				case MainAttack:
					hitChance = attacker.GetModifiedHitChance(SpecificActionType.BasicAttack, 1f, isRangedAttack, defender);
					
					//TODO Remove after DEBUGGING
					//System.err.println("DEBUGGING @ Game.CreateTargetInfos() - Player always hits, non-players always miss.");
					//didAttackHit = attacker.GetData() == this.GetPlayerData();
	
					combatEffects.clear();
					combatEffects.add(AMI.new CombatEffect_Damage(0, true, 0, attacker.getRandomWeaponElement()));
					combatCalcInfo = attacker.GetCombatCalcInfo(combatEffects, SpecificActionType.BasicAttack, isRangedAttack, defender);
					break;
				case Ability:
					hitChance = attacker.GetModifiedHitChance(SpecificActionType.Ability, ability.chanceToHitFactor, isRangedAttack, defender);
					
					combatCalcInfo = attacker.GetCombatCalcInfo(ability.combatEffects, SpecificActionType.Ability, isRangedAttack, defender);
					break;
				case UseItem:
					//Build the corresponding CombatEffects using the ItemData
					ItemCombatProperties itemProps = GetItemsCombatProperites(itemData);
					if(itemProps.combatEffects.size() == 0)
						System.err.println("Game.GetTargetInfoInstances() - There weren't any combatEffects found for the Item action: " + itemData.getName());
					
					if(itemProps.isGuaranteedItemHit)
						hitChance = 1f;
					else
						//Base non-recovery items hit factor on attacker's tallied hit chance & defender's tallied dodge chance
						hitChance = attacker.GetModifiedHitChance(SpecificActionType.Item, 1f, isRangedAttack, defender);
					
					//TODO Remove after DEBUGGING
					//System.err.println("DEBUGGING @ Game.DoAbility() - Player always hits, non-players always miss.");
					//didAttackHit = attacker.GetData() == this.GetPlayerData();
					
					combatCalcInfo = attacker.GetCombatCalcInfo(itemProps.combatEffects, SpecificActionType.Item, isRangedAttack, defender);
					break;
				default:
					System.err.println("Game.GetTargetInfoInstances() - Add support for Game.ActionType: " + actionType);
					break;
			}
			targetInfos.add(new TargetInfo(defender, hitChance, combatCalcInfo));
		}
		return targetInfos;
	}*/
	public List<TargetInfo> GetTargetInfoInstances(CharacterBase attacker, InteractiveActionType actionType, Ability ability, ItemData itemData, List<CharacterBase> newTargets) {
		List<TargetInfo> targetInfos = new ArrayList<TargetInfo>();
		
		List<CombatEffect> combatEffects = new ArrayList<CombatEffect>();
		ItemCombatProperties itemProps = null;
		switch(actionType) {
			case MainAttack:
				combatEffects.add(new CombatEffect_Damage(0, true, 0, attacker.getRandomWeaponElement()));
				break;
			case Ability:
				break;
			case UseItem:
				//Build the corresponding CombatEffects using the ItemData
				itemProps = GetItemsCombatProperites(itemData);
				if(itemProps.combatEffects.size() == 0)
					System.err.println("Game.GetTargetInfoInstances() - There weren't any combatEffects found for the Item action: " + itemData.getName());
				break;
			default:
				System.err.println("Game.GetTargetInfoInstances() - Add support for Game.ActionType: " + actionType);
				break;
		}
		
		for(CharacterBase defenderBase : newTargets) {
			CharacterBase defender = defenderBase;
			CombatCalcInfo combatCalcInfo = null;
			double hitChance = 0f;
			
			boolean isRangedAttack = Point.distance(attacker.getLocation().getX(), attacker.getLocation().getY(), defender.getLocation().getX(), defender.getLocation().getY()) > 1.0;
			
			switch(actionType) {
				case MainAttack:
					hitChance = attacker.GetModifiedHitChance(SpecificActionType.BasicAttack, 1f, isRangedAttack, defender);
					
					//TODO Remove after DEBUGGING
					//System.err.println("DEBUGGING @ Game.CreateTargetInfos() - Player always hits, non-players always miss.");
					//didAttackHit = attacker.GetData() == this.GetPlayerData();

					combatCalcInfo = attacker.GetCombatCalcInfo(combatEffects, SpecificActionType.BasicAttack, isRangedAttack, defender);
					break;
				case Ability:
					hitChance = attacker.GetModifiedHitChance(SpecificActionType.Ability, ability.chanceToHitFactor, isRangedAttack, defender);
					
					combatCalcInfo = attacker.GetCombatCalcInfo(ability.combatEffects, SpecificActionType.Ability, isRangedAttack, defender);
					break;
				case UseItem:
					if(itemProps.isGuaranteedItemHit)
						hitChance = 1f;
					else
						//Base non-recovery items hit factor on attacker's tallied hit chance & defender's tallied dodge chance
						hitChance = attacker.GetModifiedHitChance(SpecificActionType.Item, 1f, isRangedAttack, defender);
					
					//TODO Remove after DEBUGGING
					//System.err.println("DEBUGGING @ Game.DoAbility() - Player always hits, non-players always miss.");
					//didAttackHit = attacker.GetData() == this.GetPlayerData();
					
					combatCalcInfo = attacker.GetCombatCalcInfo(itemProps.combatEffects, SpecificActionType.Item, isRangedAttack, defender);
					break;
				default:
					System.err.println("Game.GetTargetInfoInstances() - Add support for Game.ActionType: " + actionType);
					break;
			}
			targetInfos.add(new TargetInfo(defender, hitChance, combatCalcInfo));
		}
		
		return targetInfos;
	}
	
	
	//Lingering CombatAnim Adaptation - Start
	
	/**
	 * Track consecutive lingering anims by filling this list in the intended order.
	 */
	List<CombatEffect> lingeringAnimCombatEffects = new ArrayList<CombatEffect>();
	public List<CombatEffect> GetLingeringAnimCombatEffects() { return lingeringAnimCombatEffects; }
	/**
	 * This is how we show lingering effects happening to characters at the start of their turn. In its entirety this method, when called, can result in 1-3 combat anims: lingering damage, lingering healing
	 * and lingering revive. The CharacterBase decides which ones will happen and in what order. The first two possible anims are healing and/or damage and is/are always proceded by a possible revive.
	 * Additionally, the revive anim will never play by itself, it is only played if lingering damage kills the character this turn.
	 * @param doHealingFirst
	 * @param hpLoss
	 * @param hpGain
	 * @param doRevive
	 */
	public void HandleLingeringEffectAnims(boolean doHealingFirst, int hpLoss, int hpGain, boolean doRevive, float revivePercentage) {
		currentActionType = InteractiveActionType.Lingering;
		lingeringAnimCombatEffects.clear();
		if(!doHealingFirst) {
			if(hpLoss > 0)
				lingeringAnimCombatEffects.add(new CombatEffect_Damage(0, false, hpLoss, null));
			if(hpGain > 0)
				lingeringAnimCombatEffects.add(new CombatEffect_Potion(0, hpGain, false));
		} else if(doHealingFirst) {
			if(hpGain > 0)
				lingeringAnimCombatEffects.add(new CombatEffect_Potion(0, hpGain, false));
			if(hpLoss > 0)
				lingeringAnimCombatEffects.add(new CombatEffect_Damage(0, false, hpLoss, null));
		}
		
		if(doRevive)
			lingeringAnimCombatEffects.add(new CombatEffect_Revive(0, revivePercentage));
		
		System.out.println("Game.HandleLingeringEffectAnims() - list size: " + lingeringAnimCombatEffects.size());
		
		PlayNextLingeringAnim();
	}
	
	/**
	 * This gets called for the turnTaker when the BattleState is restored, if they've got remaining lingeringEffect, since the character's OnTurnStart method won't be called.
	 * @param remainingLingeringAnimCombatEffects
	 */
	public void ResumeLingeringEffectAnims(List<CombatEffect> remainingLingeringAnimCombatEffects) {
		currentActionType = InteractiveActionType.Lingering;
		lingeringAnimCombatEffects = remainingLingeringAnimCombatEffects;
		
		System.out.println("Game.ResumeLingeringEffectAnims() - list size: " + lingeringAnimCombatEffects.size());
		
		PlayNextLingeringAnim();
	}
	
	/**
	 * This actually initiates the playing of the first lingering anim in our list, which gets smaller everytime we play one.
	 */
	private void PlayNextLingeringAnim() {
		currentTargetInfos.clear();
		CharacterBase attacker = GetActiveBattleCharacter();
		CharacterBase defender = attacker;
		List<CombatEffect> listWithSingleEffect = new ArrayList<CombatEffect>();
		listWithSingleEffect.add(lingeringAnimCombatEffects.get(0));
		CombatCalcInfo combatCalcInfo = attacker.GetCombatCalcInfo(listWithSingleEffect, null, false, defender);
		currentTargetInfos.add(new TargetInfo(defender, 1f, combatCalcInfo));
		
		List<CharacterBase> listWithSingleDefender = new ArrayList<CharacterBase>();
		listWithSingleDefender.add(defender);
		PlayCombatAnimSeries(InteractiveActionType.Lingering, null, null, listWithSingleDefender);
	}
	
	/**
	 * This needs to be called like a callback from a hook equating to "On CombatAnim End"
	 * @return
	 */
	public boolean TryPlayAnotherLingeringAnim() {
		if(lingeringAnimCombatEffects.size() > 0)
			lingeringAnimCombatEffects.remove(0);
		
		if(lingeringAnimCombatEffects.size() == 0) {
			//If we're ending the last Lingering Anim then clear currentTargetInfos so that we don't cross contaminate the next conditional being called after this: TryPlayNextDefenderAnim(), which also
			//uses currentTargetInfos.
			if(currentActionType == InteractiveActionType.Lingering) {
				currentActionType = null;
				currentTargetInfos.clear();
				OnLingeringAnimsDone();
			}
			return false;
		} else {
			PlayNextLingeringAnim();
			return true;
		}
	}
	
	//Lingering CombatAnim Adaptation - End
	
	
	public void DoAttack(CharacterBase attacker, List<CharacterBase> defenders) {
		attacker.DoAttack();
		
		PlayCombatAnimSeries(InteractiveActionType.MainAttack, null, null, defenders);
	}
	
	public void DoAbility(CharacterBase attacker, Ability ability, List<CharacterBase> defenders) {
		attacker.DoAbility();
		
		PlayCombatAnimSeries(InteractiveActionType.Ability, ability, null, defenders);
	}
	
	public void DoItem(CharacterBase attacker, ItemData itemData, List<CharacterBase> defenders) {
		attacker.DoItemAction();
		
		PlayCombatAnimSeries(InteractiveActionType.UseItem, null, itemData, defenders);
	}
	
	
	public enum InteractiveActionType { MainAttack, Ability, UseItem, Lingering };
	InteractiveActionType currentActionType;
	List<CharacterBase> targetedDefenders;
	//These get set for user-controllered characters and AI alike in the PlayCombatAnimSeries method and are then used in the follwoing TryPlayNextDefenderAnim method calls.
	private Ability currentAbility;
	private ItemData currentItemData;
	
	private void PlayCombatAnimSeries(InteractiveActionType actionType, Ability currentAbility, ItemData currentItemData, List<CharacterBase> defenders) {
		this.currentActionType = actionType;
		this.targetedDefenders = defenders;
		this.currentAbility = currentAbility;
		this.currentItemData = currentItemData;
		
		battlePanel.PrepareUIForCombatAnim();
		
		TryPlayNextDefenderAnim();
	}
	
	/**
	 * Called by CombatAnimPane. Instead of routing control from Game > CombatAnimPane > BattlePanel we need to go Game > CombatAnimPane > ?(Game > CombatAnimPane >) Game > BattlePanel.
	 */
	public void OnCombatAnimComplete() {
		//FIRST Check if this CombatAnim is the result of a lingering effect anim occuring at the start of the turnTaker's turn, if it is and its ending then it'll clear currentTargetInfos so that lists value
		//	wont cross contaminate the TryPlayNextDefenderAnim() method, which should only occur during animations.
		//THEN Check if the current combat action has more defenders to animate.
		//FINALLY check if there are any post bout instructions to carry out now that all combat anims have finished.
		if(!TryPlayAnotherLingeringAnim() && !TryPlayNextDefenderAnim() && !TryDoPostBoutInstruction()) {
			//Move on with the turn
			battlePanel.CombatAnimDone();
		}
	}
	
	//PostBoutInstruction Stuff - Start
	
	/**
	 * Keeps track of which currentTargetInfos we're doing a Post Bout for. Its default should always be -1 so that it can properly sequence all currentTargetInfos with posBoutInstructions
	 */
	private int postBoutInstructionIndex = -1;
	
	private boolean TryDoPostBoutInstruction() {
		TargetInfo targetInfo = null;
		
		//Check if there are any PostCoytusInstructions to handle in any of the currentTargetInfos
		for(int i = 0; i < currentTargetInfos.size(); i++) {
			TargetInfo thisInfo = currentTargetInfos.get(i);
			if(thisInfo.combatCalcInfo.postBoutInstruction != null && i > postBoutInstructionIndex) {
				postBoutInstructionIndex = i;
				targetInfo = thisInfo;
				break;
			}
		}
		if(targetInfo == null) {
			postBoutInstructionIndex = -1;
			return false;
		}
		
		switch(targetInfo.combatCalcInfo.postBoutInstruction) {
			case DefenderDoesCounterAttack:
				CharacterBase attacker = targetInfo.target;
				CharacterBase defender = GetActiveBattleCharacter();
				
				String actiontext = GetDistance(attacker.getLocation(), defender.getLocation()) > 1 ? "Ranged Counterattack" : "Melee Counterattack";
				battlePanel.ToggleActionLabel(actiontext);
				
				//Create a new TargetInfo for the brand new attack
				List<CharacterBase> targets = new ArrayList<CharacterBase>();
				targets.add(defender);
				TargetInfo newTargetInfo = GetTargetInfoInstances(attacker, InteractiveActionType.MainAttack, null, null, targets).get(0);
				
				boolean didAttackHit = attacker.GetModifiedHitOutcome(newTargetInfo.chanceToHit);
				
				//TODO Comment after DEBUGGING
				//System.err.println("DEBUGGING @ Game.DoAttack() - Player always hits, non-players always miss.");
				//didAttackHit = attacker.GetData() == this.GetPlayerData();
				
				if(didAttackHit) {
					attacker.ApplyCombatEffects(newTargetInfo.combatCalcInfo, defender);
					battlePanel.PlayCombatAnim(attacker, defender, true, newTargetInfo.combatCalcInfo.healthModInfo, null, null, true, false, false, false, false);
				} else
					battlePanel.PlayCombatAnim(attacker, defender, false, null, null, null, true, false, false, false, false);
				break;
			case DefenderDoesRevive:
				battlePanel.ToggleActionLabel("Auto-Revive");
				
				defender = targetInfo.target;
				List<CombatEffect> listWithSingleEffect = new ArrayList<CombatEffect>();
				
				LingeringEffect lingeringRevive = defender.lingeringEffects.stream().filter(x -> x.combatEffect.reviveHealthPercentage != 0f).findFirst().get();
				//TODO Comment and restore above after DEBUGGING
				//System.err.println("DEBUGGING @ Game.TryDoPostBoutInstruction() - A Revive LingeringEffect will always be created for the defender.");
				//LingeringEffect lingeringRevive = new LingeringEffect(new CombatEffect_Revive(0, 0.5f));
				
				listWithSingleEffect.add(new CombatEffect_Revive(0, lingeringRevive.combatEffect.reviveHealthPercentage));
				CombatCalcInfo combatCalcInfo = defender.GetCombatCalcInfo(listWithSingleEffect, null, false, defender);
				defender.ApplyCombatEffects(combatCalcInfo, defender);
				battlePanel.PlayCombatAnim(defender, defender, true, combatCalcInfo.healthModInfo, null, null, false, true, false, true, false);
				break;
			default:
				System.err.println("Game.DoPostBoutInstruction() - Add support for: " + targetInfo.combatCalcInfo.postBoutInstruction);
				break;
		}
		return true;
	}
	
	//PostBoutInstruction Stuff - End
	
	public class ItemCombatProperties {
		public ItemCombatProperties(boolean isGuaranteedItemHit, List<CombatEffect> combatEffects) {
			this.isGuaranteedItemHit = isGuaranteedItemHit;
			this.combatEffects = combatEffects;
		}
		public boolean isGuaranteedItemHit;
		public List<CombatEffect> combatEffects;
	}
	
	public ItemCombatProperties GetItemsCombatProperites(ItemData itemData) {
		boolean isGuaranteedItemHit = false;
		List<CombatEffect> combatEffects = new ArrayList<CombatEffect>();
		
		for(BattleItemType battleItemType : BattleItemTraits.GetAllBattleItemTypes(itemData)) {
			List<AttributeMod> mods = new ArrayList<AttributeMod>();
			switch(battleItemType) {
				case Accelerant:
					combatEffects.add(new CombatEffect_Status(itemData.getStats().GetBattleToolTraits().battleItemTraits.effectDuration, StatusType.Accelerated));
					break;
				case Status:
					combatEffects.add(new CombatEffect_Status(itemData.getStats().GetBattleToolTraits().battleItemTraits.effectDuration,
									  itemData.getStats().GetBattleToolTraits().battleItemTraits.status));
					break;
				case Cure:
					isGuaranteedItemHit = true;
					combatEffects.add(new CombatEffect_Cure(0, new StatusType[] { itemData.getStats().GetBattleToolTraits().battleItemTraits.status } ));
					break;
				case Damage:
					combatEffects.add(new CombatEffect_Damage(0, false, itemData.getStats().getAttack(), null));
					break;
				case Potion:
					isGuaranteedItemHit = true;
					combatEffects.add(new CombatEffect_Potion(0, itemData.getStats().getHp(), false));
					break;
				case Revive:
					isGuaranteedItemHit = true;
					combatEffects.add(new CombatEffect_Revive(0, 0.5f));
					break;
				case Buff:
					isGuaranteedItemHit = true;
					if(itemData.getStats().getArmor() != 0)
						mods.add(new AttributeMod(AttributeModType.BaseArmor, itemData.getStats().getArmor()));
					if(itemData.getStats().getAttack() != 0)
						mods.add(new AttributeMod(AttributeModType.BaseDamage, itemData.getStats().getAttack()));
					if(itemData.getStats().getHp() != 0)
						mods.add(new AttributeMod(AttributeModType.StatusResistance, itemData.getStats().getHp() / 100f));
					if(itemData.getStats().getSpirit() != 0)
						mods.add(new AttributeMod(AttributeModType.AbilityPotency, itemData.getStats().getSpirit() / 100f));
					CombatEffect buffEffect = new CombatEffect_Buff(itemData.getStats().GetBattleToolTraits().battleItemTraits.effectDuration, mods.stream().toArray(AttributeMod[]::new));
					combatEffects.add(buffEffect);
					break;
				case Debuff:
					if(itemData.getStats().getArmor() != 0)
						mods.add(new AttributeMod(AttributeModType.BaseArmor, itemData.getStats().getArmor()));
					if(itemData.getStats().getAttack() != 0)
						mods.add(new AttributeMod(AttributeModType.BaseDamage, itemData.getStats().getAttack()));
					if(itemData.getStats().getHp() != 0)
						mods.add(new AttributeMod(AttributeModType.StatusResistance, itemData.getStats().getHp() / 100f));
					if(itemData.getStats().getSpirit() != 0)
						mods.add(new AttributeMod(AttributeModType.AbilityPotency, itemData.getStats().getSpirit() / 100f));
					CombatEffect debuffEffect = new CombatEffect_Debuff(itemData.getStats().GetBattleToolTraits().battleItemTraits.effectDuration, mods.stream().toArray(AttributeMod[]::new));
					combatEffects.add(debuffEffect);
					break;
				case SpiritTool:
					isGuaranteedItemHit = true;
					//We're not sure what SpiritTools will need for their implementation but we know it probably wont be combatEffects
					break;
				default:
					System.err.println("Game.GetTargetInfoInstances() - Add support for: " + battleItemType);
					break;
			}
		}
		
		return new ItemCombatProperties(isGuaranteedItemHit, combatEffects);
	}
	
	/**
	 * This gets called by PlayCombatAnimSeries initially and then again by EndCombatAnim for each remaining defender.
	 * @return This returns true if there's another defender anim to play, otherwise it'll return false and signify that this series of combat anims has come to an end.
	 */
	private boolean TryPlayNextDefenderAnim() {
		if(targetedDefenders.size() == 0)
			return false;
		
		CharacterBase attacker = GetActiveBattleCharacter();
		CharacterBase defender = targetedDefenders.get(0);
		targetedDefenders.remove(0);
		boolean isSelfTargeting = attacker == defender;
		boolean isSingleSelfTarget = isSelfTargeting && currentTargetInfos.size() == 1;
		boolean isMultiTargetActionCurrentlyHittingSelf = isSelfTargeting && currentTargetInfos.size() > 1;
		
		TargetInfo targetInfo = currentTargetInfos.stream().filter(x -> x.target == defender).findFirst().get();
		switch(currentActionType) {
			case MainAttack:
				String actiontext = GetDistance(attacker.getLocation(), defender.getLocation()) > 1 ? "Ranged Attack" : "Melee Attack";
				battlePanel.ToggleActionLabel(actiontext);
				
				boolean didAttackHit = attacker.GetModifiedHitOutcome(targetInfo.chanceToHit);
				
				//TODO Remove after DEBUGGING
				//System.err.println("DEBUGGING @ Game.DoAttack() - Player always hits, non-players always miss.");
				//didAttackHit = attacker.GetData() == this.GetPlayerData();
				
				if(didAttackHit) {
					//List<CombatEffect> combatEffects = new ArrayList<CombatEffect>();
					//combatEffects.add(AMI.new CombatEffect_Damage(0, true, 0, attacker.getRandomWeaponElement()));
					attacker.ApplyCombatEffects(targetInfo.combatCalcInfo, defender);
					//defender.TakeDamage(targetInfo.combatCalcInfo.healthModInfo);
					battlePanel.PlayCombatAnim(attacker, defender, didAttackHit, targetInfo.combatCalcInfo.healthModInfo, null, null, isMultiTargetActionCurrentlyHittingSelf, false, isSelfTargeting, false, false);
				} else
					battlePanel.PlayCombatAnim(attacker, defender, false, null, null, null, isMultiTargetActionCurrentlyHittingSelf, false, isSelfTargeting, false, false);
				break;
			case Ability:
				battlePanel.ToggleActionLabel("Ability: " + currentAbility.name);
				
				didAttackHit = attacker.GetModifiedHitOutcome(targetInfo.chanceToHit);
				
				if(didAttackHit) {
					attacker.ApplyCombatEffects(targetInfo.combatCalcInfo, defender);
					battlePanel.PlayCombatAnim(attacker, defender, true, targetInfo.combatCalcInfo.healthModInfo, currentAbility, null, isMultiTargetActionCurrentlyHittingSelf, false, isSelfTargeting, false, isSingleSelfTarget);
				} else
					battlePanel.PlayCombatAnim(attacker, defender, false, null, currentAbility, null, isMultiTargetActionCurrentlyHittingSelf, false, isSelfTargeting, false, isSingleSelfTarget);
				break;
			case UseItem:
				battlePanel.ToggleActionLabel("Item: " + currentItemData.getName());
				
				//Build the corresponding CombatEffects using the ItemData
				boolean isGuaranteedItemHit = GetItemsCombatProperites(currentItemData).isGuaranteedItemHit;
				
				didAttackHit = false;
				if(isGuaranteedItemHit)
					didAttackHit = true;
				else {
					//Base non-recovery items hit factor on attacker's tallied hit chance & defender's tallied dodge chance
					didAttackHit = attacker.GetModifiedHitOutcome(targetInfo.chanceToHit);
				}
				
				//TODO Remove after DEBUGGING
				//System.err.println("DEBUGGING @ Game.DoAbility() - Player always hits, non-players always miss.");
				//didAttackHit = attacker.GetData() == this.GetPlayerData();
				
				if(didAttackHit) {
					attacker.ApplyCombatEffects(targetInfo.combatCalcInfo, defender);
					battlePanel.PlayCombatAnim(attacker, defender, true, targetInfo.combatCalcInfo.healthModInfo, null, currentItemData, isMultiTargetActionCurrentlyHittingSelf, false, isSelfTargeting, false, isSingleSelfTarget);
				} else
					battlePanel.PlayCombatAnim(attacker, defender, false, null, null, currentItemData, isMultiTargetActionCurrentlyHittingSelf, false, isSelfTargeting, false, isSingleSelfTarget);
				
				//Remove this item from the party's inventory
				TakeSingleItemFromInventoryAt(currentItemData.getName());
				break;
			case Lingering:
				String lingeringEffectText = "[UNKNOWN]";
				if(targetInfo.combatCalcInfo.damageEffect != null)
					lingeringEffectText = "Poison Damage";
				else if(targetInfo.combatCalcInfo.potionEffect != null)
					lingeringEffectText = "Healing Over Time";
				else if(targetInfo.combatCalcInfo.reviveEffect != null)
					lingeringEffectText = "Auto-Revive";
				battlePanel.ToggleActionLabel(lingeringEffectText);
				
				attacker.ApplyCombatEffects(targetInfo.combatCalcInfo, defender);
				battlePanel.PlayCombatAnim(attacker, defender, true, targetInfo.combatCalcInfo.healthModInfo, null, null, true, false, true, true, false);
				break;
			default:
				System.err.println("Game.TryPlayNextDefenderAnim() - Add support for Game.ActionType: " + currentActionType);
				break;
		}
		
		return true;
	}
	
	
	/**
	 * This will protect from the CharBase.ApplyCombatEffects() method calling this and fucking with the turntaker's turn order in the middle of their turn
	 * @param characterBase
	 * @param sequenceShift
	 */
	public void ShiftTurnOrderForCharacter(CharacterBase characterBase, int sequenceShift) {
		//If the current turntaker is speeding themselves up we've gotta wait till the turns over to fuck with the turn order otherwise we'll desync the current turntaker from the turn order list
		if(characterBase == this.GetActiveBattleCharacter()) {
			pendingCharacterBaseToShift = characterBase;
			pendingSequenceShift = sequenceShift;
			return;
		} else {
			UnsafeShiftTurnOrderForCharacter(characterBase, sequenceShift);
		}
	}
	
	/**
	 * This will either alter turn order of a non-turnTaker in the middle of a turn or it'll wait till after the turntaker's turn to change their order.
	 * @param characterBase
	 * @param sequenceShift
	 */
	private void UnsafeShiftTurnOrderForCharacter(CharacterBase characterBase, int sequenceShift) {
		//If the current turntaker is speeding themselves up we've gotta wait till the turns over to fuck with the turn order otherwise we'll desync the current turntaker from the turn order list
		/*if(characterBase == this.GetActiveBattleCharacter()) {
			pendingCharacterBaseToShift = characterBase;
			pendingSequenceShift = sequenceShift;
			return;
		}*/
		//Moving this into a safety method, which will protect from the CharBase.ApplyCombatEffects() method calling this and fucking with the turntaker's turn order in the middle of their turn
		
		int previousIndex = turnOrderedCharBases.indexOf(characterBase);
		
		int newIndex = previousIndex + sequenceShift;
		if(newIndex < 0)
			newIndex = newIndex + turnOrderedCharBases.size();
		else if(newIndex >= turnOrderedCharBases.size())
			newIndex -= turnOrderedCharBases.size();
		//Don't let a "sped up" unit put itself before the current turntaker, otherwise its like we're majorly slowing them down
		//The fastest a unit could be sped up is for their turn to be next
		if(sequenceShift < 0 && newIndex <= turnOrderIndex) {
			newIndex = turnOrderIndex + 1;
			if(newIndex == turnOrderedCharBases.size())
				newIndex = 0;
		}
		
		//If the turnOrder playhead is occupying a chunk of players shifting upwards in the turn order list then we need the playhead to follow
		if(previousIndex < turnOrderIndex && newIndex > turnOrderIndex)
			turnOrderIndex--;
		
		turnOrderedCharBases.remove(previousIndex);
		turnOrderedCharBases.add(newIndex, characterBase);
	}
	
	private CharacterBase pendingCharacterBaseToShift;
	private int pendingSequenceShift;
	
	/**
	 * This gets called after the turntaker's turn is over so that we can their turn order before the start of the next turn.
	 */
	private void ShiftPendingCharacterBeforeNextTurn() {
		UnsafeShiftTurnOrderForCharacter(pendingCharacterBaseToShift, pendingSequenceShift);
		pendingCharacterBaseToShift = null;
		pendingSequenceShift = 0;
	}
	
	
	public enum ObjectiveType {
		Attack,
		Heal,
		Wait
	};
	//Used to track data about each objective so that the data may be compared later
	public class ObjectiveAnalysis {
		public ObjectiveAnalysis() {}
		public ObjectiveAnalysis(List<Tile> objectiveMoves, ObjectiveType objectiveType, Tile actionTargetTile, List<Point> targetArea) {
			this.objectiveMoves = objectiveMoves;
			this.objectiveType = objectiveType;
			this.actionTargetTile = actionTargetTile;
		}
		public List<Tile> objectiveMoves = new ArrayList<Tile>();
		public ObjectiveType objectiveType;
		public Tile actionTargetTile;
		/**
		 * Stores the tiles with aoe range of the actionTargetTile, this will be null for non-aoe actions.
		 */
		public List<Point> targetArea;
		
		/**
		 * This stores all the possible attackOptions that achieve the objective. Used to refine the combined decision analysis on a deeper level. The the selected attack option is then stored in
		 * the AIsChosenAttackAction.
		 */
		public List<ActionOptionInfo> attackOptionInfos;
		/**
		 * This is the final attack decision stored for reference later when the attack occurs(the character may need to move before attacking, etc)
		 */
		public ActionOptionInfo AIsChosenAttackAction;
	}
	public class AIAction {
		public AIAction(Tile moveTile, ObjectiveType objectiveType, Tile actionTargetTile, List<Point> targetArea, ActionOptionInfo actionOptionInfo) {
			this.moveTile = moveTile;
			this.objectiveType = objectiveType;
			this.actionTargetTile = actionTargetTile;
			this.targetArea = targetArea;
			this.actionOptionInfo = actionOptionInfo;
		}
		public Tile moveTile;
		public ObjectiveType objectiveType;
		public Tile actionTargetTile;
		public List<Point> targetArea;
		public ActionOptionInfo actionOptionInfo;
	}
	AIAction chosenAction;
	
	public static int GetDistance(Point p1, Point p2) {
		return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
	}
	
	private Point GetDirection(Point source, Point target) {
		return new Point(Math.max(-1, Math.min(target.x - source.x, 1)) , Math.max(-1, Math.min(target.y - source.y, 1)));
	}
	
	//Returns a number between 0 and 4, 0 meaning the directions are aligned and 4 meaning they're opposite
	private int GetAlignmentOffset(Point sourceDir, Point targetDir) {
		int sourceAngle = GetAngle(sourceDir);
		int targetAngle = GetAngle(targetDir);
		int difAngle = Math.abs(sourceAngle - targetAngle);
		
		return difAngle / 45;
	}
	
	private int GetAngle(Point coord) {
		double deltaX = coord.x;
		double deltaY = coord.y;
		double radianAngle = Math.atan2(deltaY, deltaX);
		int degreeAngle = (int)Math.round( radianAngle * ((double)180/Math.PI) );
		if(degreeAngle < 0)
			degreeAngle += 180;
		return degreeAngle;
	}
	
	private List<Tile> GetAdjacentOrClosestTiles(CharacterBase turnTaker, Point targetLocation) {
		System.out.println("Game.GetAdjacentOrClosestTiles()");
		
		List<Tile> targetTiles = new ArrayList<Tile>();
		
		List<Tile> adjacentTiles = new ArrayList<Tile>();
		adjacentTiles.add(board.GetTileAt(new Point(targetLocation.x - 1, targetLocation.y)));
		adjacentTiles.add(board.GetTileAt(new Point(targetLocation.x + 1, targetLocation.y)));
		adjacentTiles.add(board.GetTileAt(new Point(targetLocation.x, targetLocation.y - 1)));
		adjacentTiles.add(board.GetTileAt(new Point(targetLocation.x, targetLocation.y + 1)));
		
		for(Tile adjacentTile : adjacentTiles) {
			//If within range of adjacent target and is considered a move option
			if(turnTaker.GetPaths().keySet().stream().anyMatch(x -> x.Location() == adjacentTile.Location())) {
				System.out.println("- If within range of adjacent target and is considered a move option");
				targetTiles.add(adjacentTile);
			}
		}
		
		//if all adjacentTiles are out of range then pathfind toward them
		if(targetTiles.size() == 0) {
			System.out.println("-All adjacentTiles are out of range");
			for(Tile adjacentTile : adjacentTiles) {
				Point direction = GetDirection(turnTaker.getLocation(), adjacentTile.Location());
				//System.out.println("Direction: " + direction.toString());
				
				//Sus out which are the farthest available move tiles in that direction and add them to targetTiles
				Map<Integer, List<Tile>> rangeBuckets = new HashMap<Integer, List<Tile>>();
				if(turnTaker.GetPaths().keySet().size() > 0) {
					for(Tile moveTile : turnTaker.GetPaths().keySet()) {
						System.out.println("    - Inspecting moveTile at: " + moveTile.Location().x + ", " + moveTile.Location().y);
						Point moveDirection = GetDirection(turnTaker.getLocation(), moveTile.Location());
						if(GetAlignmentOffset(direction, moveDirection) > 2) {
							System.out.println("    - tile is out of acceptable target direction alignment range. continuing.");
							continue;
						}
						
						//Get distance to target adjacent tile
						int distance = GetDistance(adjacentTile.Location(), moveTile.Location());
						if(!rangeBuckets.containsKey(distance))
							rangeBuckets.put(distance, new ArrayList<Tile>());
						rangeBuckets.get(distance).add(moveTile);
					}
				} else {
					System.err.println("turnTaker.GetPaths().keySet().size() == 0");
				}
				
				if(rangeBuckets.size() > 0) {
					System.out.println("    - created rangeBuckets.size(): " + rangeBuckets.size());
					List<Integer> ranges = new ArrayList<Integer>( rangeBuckets.keySet() );
					Collections.sort(ranges);
					//Add the tiles that are closest to the target adjacent tile
					targetTiles.addAll(rangeBuckets.get(ranges.get(0)));
				} else {
					System.out.println("    - Could not create rangeBuckets.");
				}
			}
		}
		
		
		return targetTiles;
	}
	
	//Enemy AI
	private List<Objective> currentObjectives_enemyAI = new ArrayList<Objective>();
	
	//Objectives Npc Ally AI
	private List<Objective> currentObjectives_npcAllyAI = new ArrayList<Objective>();
	
	//AI - End
	
	//Update character battle board position
	public void SetCharacterMovement(Point newLocation) {
		System.out.println("SetCharacterMovement(" + newLocation + ")");
		
		CharacterBase character = turnOrderedCharBases.get(turnOrderIndex);
		character.Move(newLocation);
		
		board.CalcAllMoves();
		
		if(!allyCharacterList.contains(character))
			AIMoveDoneCallback();
		else
			battlePanel.ShowActionPanel(character);
	}
	
	//Board related stuff - End
	
	//SaveData Tie-ins - Start
	
	public List<Mission> getMissions() {
		if(saveData.missions != null && saveData.missions.size() > 0)
			return saveData.missions;
		else
			return null;
	}
	public void RegisterMission(Mission mission) {
		saveData.missions.add(mission);
	}
	
	//Called by WorldmapPanel
	public void CompleteMission(Mission mission) {
		//Missions.CompleteMission(mission);
		//this now needs to be called after saveData.completedMissionIds is updated because Missions.CompleteMission() does operations with it and needs the most up-to-date data
		
		//Update completedMissions
		//List<String> completedMissionIdList = new ArrayList<>(Arrays.asList(saveData.completedMissionIds));
		//completedMissionIdList.add(mission.getId());
		//saveData.completedMissionIds = completedMissionIdList.stream().toArray(String[]::new);
		mission.SetMissionStatus(MissionStatusType.Concluded);
		
		//This can be called now that saveData.completedMissionIds has been updated
		Missions.OnGameCompleteMission(mission);
		
		ReceiveItems(mission.getRewards());
		
		GUIManager.WorldmapPanel().HandleMissionIndicator(mission);
	}
	
	public void IterateVisitedLocationsSinceLastMissionCounter() {
		saveData.locationsVisitedSinceLastMissionCounter++;
	}
	
	public int GetLocationsVisitedSinceLastMissionCounter() {
		return saveData.locationsVisitedSinceLastMissionCounter;
	}
	
	public void ResetLocationsVisitedSinceLastMissionCounter() {
		saveData.locationsVisitedSinceLastMissionCounter = 0;
	}
	
	//Map stuff
	public void SetWorldmapData(WorldmapData worldmapData) {
		saveData.worldmapData = worldmapData;
	}
	public WorldmapData GetWorldmapData() {
		return saveData.worldmapData;
	}
	
	public Point2D GetWorldmapLocation() {
		return saveData.currentWorldmapLocation;
	}
	//Called by WorldmapPanel when entering a new area
	public void SetWorldmapLocation(Point2D currentWorldmapLocation, MapLocation currentLocation) {
		saveData.currentWorldmapLocation = currentWorldmapLocation;
		LoadSceneData(currentLocation);
	}
	
	/**
	 * Recall if the player was in a mapLocation or on the Worldmap.
	 * @return
	 */
	public boolean IsInMapLocation() {
		return saveData.isInMapLocation;
	}
	/**
	 * Set the game focus to be in a MapLocation or on the Worldmap.
	 * @param isInMapLocation
	 */
	public void SetIsInMapLocation(boolean isInMapLocation) {
		saveData.isInMapLocation = isInMapLocation;
	}
	
	public int GetPartyStamina() {
		return saveData.partyStamina;
	}
	//Called by WorldmapPanel when entering a new area
	public void SetPartyStamina(int partyStamina) {
		saveData.partyStamina = partyStamina;
	}
	
	private final static String scenesDirectoryRoot = "mapLocationScenes/";
	public static String SceneDirectoryRoot() { return scenesDirectoryRoot; }
	private final String jsonFileName = "SceneData.json";
	private SceneData currentSceneData;
	public SceneData GetSceneData() { return currentSceneData; }
	private boolean isComboScene;
	public boolean IsComboScene() { return isComboScene; }
	
	private void LoadSceneData(MapLocation currentLocation) {
		System.out.println("Game.LoadSceneData() - currentLocation: " + currentLocation.getName() + ", sceneDirectory: " + currentLocation.getSceneDirectory()
				+ ", Combo Nature Directory: " + currentLocation.getComboNatureSceneDirectory() + ", Combo Settlement Directory: " + currentLocation.getComboSettlementSceneDirectory());
		if(currentLocation.getSceneDirectory() != null)
			currentSceneData = ReadSceneData(currentLocation.getSceneDirectory());
		else
			currentSceneData = MergeSceneDatas(currentLocation.getComboNatureSceneDirectory(), currentLocation.getComboSettlementSceneDirectory());
	}
	
	public SceneData ReadSceneData(String sceneDirectory) {
		System.out.println("Game.ReadSceneData() - sceneDirectory: " + sceneDirectory);
		
		isComboScene = false;
		
		String jsonPath = sceneDirectory + jsonFileName;
		
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("resources/" + jsonPath);
		if(is == null) {
			System.err.println("Game.ReadSceneData() - File not found at: " + "resources/" + jsonPath + " ... Dont forget to Refresh the Java project after adding new files.");
			Thread.dumpStack();
			return null;
		}
		String jsonString = null;
		try {
			InputStreamReader isReader = new InputStreamReader(is);
			BufferedReader reader = new BufferedReader(isReader);
			StringBuffer sb = new StringBuffer();
			String str;
			while((str = reader.readLine())!= null){
				sb.append(str);
			}
			jsonString = sb.toString();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	
		//convert json into sceneData class by using gson with our custom adapters
		GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
		devParallel.UnitySerializationUtility.AttachUnityAdapters(gsonBuilder);
		Gson gson = gsonBuilder.create();
		
		SceneData sceneData = gson.fromJson(jsonString, SceneData.class);
		
		if(sceneData == null)
			System.err.println("Game.ReadSceneData() - The scene could not be loaded from the json file.");
		
		return sceneData;
	}
	
	private SceneData MergeSceneDatas(String natureSceneDirectory, String settlementSceneDirectory) {
		isComboScene = true;
		SceneData natureSceneData = ReadSceneData(natureSceneDirectory);
		SceneData settlementSceneData = ReadSceneData(settlementSceneDirectory);
		//Suture the scenes together
		SceneData comboSceneData = new SceneData();
		comboSceneData.sceneWidth = natureSceneData.sceneWidth;
		comboSceneData.sceneHeight = natureSceneData.sceneHeight;
		comboSceneData.sceneLayeringType = SceneLayeringType.BothLayers;
		comboSceneData.breakaways = new ArrayList<Breakaway>();
		comboSceneData.visualTileDatas = new ArrayList<VisualTileData>();
		comboSceneData.rows =  new ArrayList<Row>();
		
		Dimension settlementSceneOffset = new Dimension(
				(natureSceneData.sceneWidth - settlementSceneData.sceneWidth) / 2, 
				(natureSceneData.sceneHeight - settlementSceneData.sceneHeight) / 2
		);
		
		
		//For now we're going to try to roll with unrestricted scene dimensions and to support this we need to record how the settlement will be placed over the nature scene
		comboSceneData.settlementSceneOffsetX = settlementSceneOffset.width;
		comboSceneData.settlementSceneOffsetY = settlementSceneOffset.height;
		comboSceneData.settlementSceneWidth = settlementSceneData.sceneWidth;
		comboSceneData.settlementSceneHeight = settlementSceneData.sceneHeight;
		//Set all of these values for the settlements ImageLayers and Breakaways so they can be properly setup during MapLocationPanel.AssembleScene()
		for(Row settlementRow : settlementSceneData.rows) {
			for(ImageLayer settlementImageLayer : settlementRow.imageLayers) {
				settlementImageLayer.belongsToSettlement = true;
			}
		}
		for(Breakaway settlementBreakaway : settlementSceneData.breakaways) {
			settlementBreakaway.belongsToSettlement = true;
		}
		for(VisualTileData visTileData : settlementSceneData.visualTileDatas) {
			visTileData.belongsToSettlement = true;
		}
		
		
		for(int row = 0; row < natureSceneData.rows.size(); row++) {
			final int rowFinal = row;
			
			final int settlementRow = row - settlementSceneOffset.height;
			//I have a theory that when this becomes positive, in the case of a settlement that's taller than the nature layer,
			//it'll push back all the settlement row layers underneath the next nature row layer.
			//final int settlementRow = Math.min(row, row - settlementSceneOffset.height);
			//Nevermind, that theory was wrong. All it did was shift up the placement of settlements on nature layers shorter than them.
			
			final boolean isWithinSettlementRowBounds = settlementRow >= 0 && settlementRow < settlementSceneData.sceneHeight;
			
			//Collect the breakaways from both scenes, in sequencial order
			Breakaway[] breakaways = natureSceneData.breakaways.stream().filter(x -> x.correspondingRowIndex == rowFinal).toArray(Breakaway[]::new);
			if(breakaways != null)
				comboSceneData.breakaways.addAll( Arrays.asList(breakaways) );
			if(isWithinSettlementRowBounds) {
				Breakaway[] settlementBreakaways = settlementSceneData.breakaways.stream().filter(x -> x.correspondingRowIndex == settlementRow).toArray(Breakaway[]::new);
				if(settlementBreakaways != null)
					comboSceneData.breakaways.addAll( Arrays.asList(settlementBreakaways) );
			}
			
			Row newRow = new SceneData().new Row();
			newRow.imageLayers = new ArrayList<ImageLayer>();
			newRow.tileDatas = new ArrayList<TileData>();
			
			//Use the imageLayers in rows to spoof the layering of combined scenes, like: Layer1 = Base nature layer image, Layer2 = Base settlement layer image, Layer3 = Nature Breakaway Image 1,
			//Layer4 = Settlement Breakaway Image 1, etc
			//Get all the image layers for this row
			newRow.imageLayers.addAll(natureSceneData.rows.get(row).imageLayers);
			//Get all the image layers for the adjust row
			if(isWithinSettlementRowBounds)
				newRow.imageLayers.addAll(settlementSceneData.rows.get(settlementRow).imageLayers);
			
			
			for(int column = 0; column < natureSceneData.rows.get(row).tileDatas.size(); column++) {
				final int columnFinal = column;
				final int settlementColumn = column - settlementSceneOffset.width;
				final boolean isWithinSettlementColumnBounds = settlementColumn >= 0 && settlementColumn < settlementSceneData.sceneWidth;
				
				//Use the nature tileData by default or overwrite it with the settlement tileData if it exists at this location
				TileData tileData = natureSceneData.rows.get(row).tileDatas.get(column);
				//Setting a special JAVA ONLY set of variables used only for display purposes.
				tileData.comboSceneOffsetLocX = tileData.gridLocationX;
				tileData.comboSceneOffsetLocY = tileData.gridLocationY;
				if(isWithinSettlementRowBounds && isWithinSettlementColumnBounds) {
					TileData settlementTileData = settlementSceneData.rows.get(settlementRow).tileDatas.stream().filter(x -> x.gridLocationX == settlementColumn).findFirst().orElse(null);
					if(settlementTileData != null) {
						tileData = settlementTileData;
						//Setting a special JAVA ONLY set of variables used only for display purposes.
						tileData.comboSceneOffsetLocX = tileData.gridLocationX + settlementSceneOffset.width;
						tileData.comboSceneOffsetLocY = tileData.gridLocationY + settlementSceneOffset.height;
					}
				}
				newRow.tileDatas.add(tileData);
				
				//Use the nature visualTile by default or overwrite it with the settlement visualTile if it exists at this location
				VisualTileData visualTileData = natureSceneData.visualTileDatas.stream().filter(x -> x.gridLocationX == columnFinal && x.gridLocationY == rowFinal).findFirst().orElse(null);
				if(isWithinSettlementRowBounds && isWithinSettlementColumnBounds) {
					VisualTileData settlementVisualTileData = settlementSceneData.visualTileDatas.stream().filter(x -> x.gridLocationX == settlementColumn && x.gridLocationY == settlementRow)
							.findFirst().orElse(null);
					if(settlementVisualTileData != null) //a settlement visual tile exists then overwrite the nature's visual tile
						visualTileData = settlementVisualTileData;
				}
				if(visualTileData != null)
					comboSceneData.visualTileDatas.add(visualTileData);
			}
			comboSceneData.rows.add(newRow);
		}
		
		return comboSceneData;
	}
	
	
	//This needs to be set on death to "Respawn", thats the only SpecialCase planned currently
	private static String currentDialographySuffix;
	public static String ConsumeSpecialCase_DialographySuffix() {
		if(currentDialographySuffix == null)
			return null;
		else {
			String temp = currentDialographySuffix;
			currentDialographySuffix = null;
			return temp;
		}
	}
	
	
	//Inventory
	public List<ItemData> GetInventory() {
		//TODO restore after debugging
		//return new ArrayList<>(Arrays.asList(saveData.inventory));
		
		//DEBUGGING
		System.err.println("DEBUGGING @ Game.GetInventory() - Inventory consists of all BattleItems.");
		List<ItemData> battleItems = new ArrayList<ItemData>();
		for(ItemData itemData : Items.itemList) {
			if(itemData.itemUseTypes() != null) {
				for(ItemType itemType : itemData.itemUseTypes()) {
					if(itemType == ItemType.BattleItem) {
						battleItems.add(itemData);
						continue;
					}
				}
			} else if(itemData.getType() == ItemType.BattleItem) {
				battleItems.add(itemData);
			}
		}
		return battleItems;
	}
	
	//Called by WorldmapPanel
	public void ReceiveItems(ItemData[] items) {
		//System.err.println("Check if items are existing in the inventory and condense items using durability/quantity");
		
		//Update inventory 
		List<ItemData> itemList = GetInventory();
		for(ItemData newItem : items) {
			//Condense items
			if(itemList.stream().anyMatch(x -> x.getName().equals(newItem.getName()))) {
				Optional<ItemData> itemOp = itemList.stream().filter(x -> x.getName().equals(newItem.getName())).findFirst();
				if(!itemOp.isPresent()) {
					System.err.println("Game.ReceiveItems() - Couldn't find item by name: " + newItem.getName());
				} else {
					System.out.println("Game.ReceiveItems() - Add to existing item quantity");
					ItemData item = itemOp.get();
					itemList.set(itemList.indexOf((Object)item), item.GetItemWithQuantity(item.getQuantity() + newItem.getQuantity()));
				}
			} else {
				System.out.println("Game.ReceiveItems() - Add new item");
				itemList.add(newItem);
			}
		}
		saveData.inventory = itemList.stream().toArray(ItemData[]::new);
	}
	
	public ItemData TakeSingleItemFromInventoryAt(String name) {
		List<ItemData> inventoryList = new ArrayList<>(Arrays.asList(saveData.inventory));
		//ItemData item = inventoryList.remove(inventoryIndex);
		//return item;
		Optional<ItemData> itemOp = inventoryList.stream().filter(x -> x.getName().equals(name)).findFirst();
		if(!itemOp.isPresent()) {
			System.err.println("Game.TakeItemFromInventoryAt() - Couldn't find item by name: " + name);
			return null;
		}
		ItemData item = itemOp.get();
		Object itemObject = (Object)item;
		if(item.getQuantity() > 1)
			inventoryList.set(inventoryList.indexOf(itemObject), item.GetItemWithQuantity(item.getQuantity() - 1));
		else
			inventoryList.remove(itemObject);
		
		saveData.inventory = inventoryList.stream().toArray(ItemData[]::new);
		
		//Reduce quantity to single one
		item = item.GetItemWithQuantity(1);
		
		return item;
	}
	
	public boolean TryConsumeInventoryItems(ItemData[] requiredItemArray) {
		boolean canConsume = false;
		Stream<ItemData> requiredItems = new ArrayList<>(Arrays.asList(requiredItemArray)).stream();
		List<ItemData> inventoryList = GetInventory();
		ItemData[] matchingItems = inventoryList.stream().filter(x -> requiredItems.anyMatch(y -> y.getName().equals(x.getName()) && y.getQuantity() <= x.getQuantity())).toArray(ItemData[]::new);
		if(matchingItems != null && matchingItems.length == requiredItemArray.length) {
			canConsume = true;
			//Remove items or quantities of items from our inventory
			for(ItemData matchingInventoryItem : matchingItems) {
				ItemData requirement = (ItemData)requiredItems.filter(x -> x.getName().equals(matchingInventoryItem.getName())).findFirst().get();
				int remainder = matchingInventoryItem.getQuantity() - requirement.getQuantity();
				if(remainder > 0)
					inventoryList.set(inventoryList.indexOf((Object)matchingInventoryItem), matchingInventoryItem.GetItemWithQuantity(remainder));
				else
					inventoryList.remove(matchingInventoryItem);
			}
			saveData.inventory = inventoryList.stream().toArray(ItemData[]::new);
		}
			
		return canConsume;
	}
	
	//Characters
	public CharacterData[] GetPartyData() {
		List<CharacterData> charData = new ArrayList<CharacterData>();
		charData.add(saveData.playerData);
		if(saveData.teamDatas != null) {
			for(CharacterData member : saveData.teamDatas)
				charData.add(member);
		}
		System.out.println("Game.GetPartyData() - size: " + charData.size());
		return charData.stream().toArray(CharacterData[]::new);
	}
	
	public void AddTeammate(CharacterData newCharacterData) {
		List<CharacterData> teamList = new ArrayList<CharacterData>();
		if(saveData.teamDatas != null) {
			for(CharacterData data : saveData.teamDatas)
				teamList.add(data);
		}
		teamList.add(newCharacterData);
		saveData.teamDatas = teamList.stream().toArray(CharacterData[]::new);
	}
	
	public void RemoveTeammate(CharacterData characterData) {
		List<CharacterData> teamList = new ArrayList<CharacterData>();
		for(CharacterData data : saveData.teamDatas)
			teamList.add(data);
		teamList.remove(characterData);
		saveData.teamDatas = teamList.stream().toArray(CharacterData[]::new);
	}
	
	public int GetBGMusicVolume() {
		return saveData.setting_bgVolume;
	}
	
	public void SetBGMusicVolume(int newNormVolume) {
		saveData.setting_bgVolume = newNormVolume;
	}
	
	//SaveData Tie-ins - End
	
	//DEBUG
	public List<String> DEBUG_GetSaveDatasSceneSelections() {
		return saveData.DEBUG_sceneSelections;
	}
}
