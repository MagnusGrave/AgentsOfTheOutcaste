package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import data.CharacterData;
import data.HealthModInfo;
import data.ItemData;
import data.LingeringEffect;
import data.PlacementSlot;
import data.SceneData.Row.TileData;
import dataShared.ActorData;
import data.AttributeMod;
import data.BattleData.CharacterPlan;
import data.BattleState;
import data.CharacterBaseData;
import enums.AttributeModType;
import enums.CharacterTurnActionState;
import enums.ColorBlend;
import enums.Direction;
import enums.EquipmentType;
import enums.ItemType;
import enums.SlotType;
import enums.StatusType;
import gameLogic.AbilityManager.Ability;
import gameLogic.AbilityManager;
import gameLogic.Board;
import gameLogic.Board.Tile;
import gameLogic.CharacterBase;
import gameLogic.Game;
import gameLogic.Game.InteractiveActionType;
import gameLogic.Game.TargetInfo;
import gameLogic.ItemDepot;
import gameLogic.Items;
import gameLogic.Mission;
import gui.SpriteSheetUtility.StatusStateType;

@SuppressWarnings("serial")
public class BattlePanel extends JPanel {
	
	public static Point GetSubtractedPoint(Point a, Point b) {
		return new Point(a.x - b.x, a.y - b.y);
	}
	
	public static Point GetAddedPoint(Point a, Point b) {
		return new Point(a.x + b.x, a.y + b.y);
	}
	
	public class CharacterSocket {	
		/**
		 * Construct a character's image and its status state anim panel. This is the structure used to render characters on the battlefield.
		 * @param id
		 * @param comboPanel
		 * @param characterPanel
		 * @param feedbackPanel
		 * @param feedbackHeight
		 * @param possiblePairedActorData
		 */
		public CharacterSocket(String id, JPanel comboPanel, ImagePanel characterPanel, Box.Filler stateBlankFrame, ImagePanel feedbackPanel, ActorData possiblePairedActorData) {
			ID = id;
			this.comboPanel = comboPanel;
			simulatedLocation = new Point(this.comboPanel.getLocation().x, this.comboPanel.getLocation().y + feedbackPanelSize.height);
			this.characterPanel = characterPanel;
			this.stateBlankFrame = stateBlankFrame;
			stateContainer = this.stateBlankFrame.getParent();
			this.feedbackPanel = feedbackPanel;
			//record ActorData and do stuff with it
			this.possiblePairedActorData = possiblePairedActorData;
			if(possiblePairedActorData != null) {
				characterPanel.SetTint(possiblePairedActorData.originalColor.getColor(), ColorBlend.Multiply);
			}
		}
		public String ID;
		/*
		 * This is the main component for the character. This is the object that will be changing layers when the character moves. It contains the character's status state imagePanel on top and
		 * the character imagePanel on the bottom.
		 */
		public JPanel comboPanel;
		private ImagePanel characterPanel;
		public ActorData possiblePairedActorData;
		public boolean isInDeadState;
		
		public void SetDeadState(CharacterBase charBase, boolean isDead) {
			isInDeadState = isDead;
			BufferedImage newFrame = null;
			if(isDead)
				newFrame = SpriteSheetUtility.GetDeadStateSprite(charBase.GetCharacterType());
			else
				newFrame = SpriteSheetUtility.GetWalkSheet(charBase.GetData().getType()).GetSprite(GetAnimIndexFromDirection(charBase.getDirection()), 1, 1);
			characterPanel.SetNewImage(newFrame);
		}
		
		//Use this instead of directly setting the Panel's location so we can move our feedbackPanel along with the Panel. 
		public void SetAllBounds(Point newLocation, Dimension newSize) {
			//need to set the bounds relative to the scenePane which is now the container for character panels, instead of the battlePane
			//Point scenePaneLoc = GUIManager.MapLocationPanel().scenePane.getLocation();
			//newLocation = new Point(newLocation.x - scenePaneLoc.x, newLocation.y - scenePaneLoc.y);
			//I dont think is right, it seemed to create another bug. I think the real solution is updating simulatedLocation when the CharacterSockets get migrated to the scenePane
			
			simulatedLocation = newLocation;
			comboPanel.setBounds(newLocation.x, newLocation.y - feedbackPanelSize.height, newSize.width, newSize.height + feedbackPanelSize.height);
		}
		
		//Used to simulate the location of the top left corner of the characterPanel, even though the corner of the comboPanel is higher than that; by a distance of feedbackHeight
		private Point simulatedLocation;
		public Point getSimulatedLocation() { return simulatedLocation; }
		
		public ImagePanel feedbackPanel;
		private List<StatusType> activeStatuses;
		private boolean hasAnyBuffs;
		private boolean hasAnyDebuffs;
		private boolean hasAnyCures;
		public void SetFeedbackState(List<StatusType> activeStatuses, boolean hasAnyBuffs, boolean hasAnyDebuffs, boolean hasAnyCures) {
			this.activeStatuses = activeStatuses;
			this.hasAnyBuffs = hasAnyBuffs;
			this.hasAnyDebuffs = hasAnyDebuffs;
			this.hasAnyCures = hasAnyCures;
			
			String statuses = "";
			for(StatusType status : activeStatuses)
				statuses += status.toString() + ", ";
			System.out.println("BattlePanel.CharacterSocket.SetFeedbackState() - statuses: [" + statuses + "], hasAnyBuffs: "
				+ hasAnyBuffs + ", hasAnyDebuffs: " + hasAnyDebuffs + ", hasAnyCures: " + hasAnyCures);
			
			currentFeedbackStateIndex = 0;
			stateFrameIndex = 0;
			
			//Subscript and upsubscribe based on our this sockets needs
			if(activeStatuses.size() == 0 && !hasAnyBuffs && !hasAnyDebuffs && !hasAnyCures) {
				socketsNeedingFeedback.remove(this);
				EndFeedback();
			} else if(!socketsNeedingFeedback.contains(this)) //Don't add this CharacterSocket multiple times
				socketsNeedingFeedback.add(this);
		}
		private int currentFeedbackStateIndex;
		private final int stateFrameMax = 2;
		private int stateFrameIndex;
		private boolean skipNextFrame;
		
		private Container stateContainer;
		Box.Filler stateBlankFrame;
		private boolean isShowingFeedback;
		private void SwapFeedback(boolean showFeedbackImage) {
			isShowingFeedback = showFeedbackImage;
			stateContainer.remove(showFeedbackImage ? stateBlankFrame : feedbackPanel);
			stateContainer.add(showFeedbackImage ? feedbackPanel : stateBlankFrame, 0);
			stateContainer.repaint();
		}
		
		public void NextFeedbackFrame() {
			//Provides a 1 frame gap between status state anims
			if(skipNextFrame) {
				SwapFeedback(false);
				skipNextFrame = false;
				return;
			}
			
			StatusStateType statusStateType = null;
			StatusType statusType = null;
			
			if(currentFeedbackStateIndex < activeStatuses.size()) {
				statusStateType = StatusStateType.Status;
				statusType = activeStatuses.get(currentFeedbackStateIndex);
			} else if(currentFeedbackStateIndex - activeStatuses.size() == 0) { //Buff, Debuff or Cure status effect
				if(hasAnyBuffs)
					statusStateType = StatusStateType.Buff;
				else if(hasAnyDebuffs)
					statusStateType = StatusStateType.Debuff;
				else if(hasAnyCures)
					statusStateType = StatusStateType.Cure;
				else
					System.err.println("BattlePanel.CharacterSocket.NextFeedbackFrame() - Add support for StatusStateType thats not Buff, Debuff or Cure! activeStatuses.size(): " + activeStatuses.size());
			} else if(currentFeedbackStateIndex - activeStatuses.size() == 1) { //Debuff or Cure status effect
				if(hasAnyDebuffs)
					statusStateType = StatusStateType.Debuff;
				else if(hasAnyCures)
					statusStateType = StatusStateType.Cure;
				else
					System.err.println("BattlePanel.CharacterSocket.NextFeedbackFrame() - Add support for StatusStateType thats not Debuff or Cure!");
			} else if(currentFeedbackStateIndex - activeStatuses.size() == 2) { //Cure status effect
				if(hasAnyCures)
					statusStateType = StatusStateType.Cure;
				else
					System.err.println("BattlePanel.CharacterSocket.NextFeedbackFrame() - Add support for StatusStateType thats not Cure!");
			} else {
				System.err.println("BattlePanel.CharacterSocket.NextFeedbackFrame() - currentFeedbackStateIndex has beyond possible limits!");
			}
			
			//Only get the first 4 frames of the anim regardless of its total frame count.
			feedbackPanel.SetNewImage( SpriteSheetUtility.GetEffectState(statusStateType, statusType)[stateFrameIndex] );
			if(!isShowingFeedback)
				SwapFeedback(true);
			else
				feedbackPanel.repaint();
			
			stateFrameIndex++;
			if(stateFrameIndex > stateFrameMax) {
				stateFrameIndex = 0;
				currentFeedbackStateIndex++;
				skipNextFrame = true;
				int maxFeedbackCount = activeStatuses.size() + (hasAnyBuffs ? 1 : 0) + (hasAnyDebuffs ? 1 : 0) + (hasAnyCures ? 1 : 0);
				if(currentFeedbackStateIndex >= maxFeedbackCount)
					currentFeedbackStateIndex = 0;
			}
		}
		
		private void EndFeedback() {
			SwapFeedback(false);
			currentFeedbackStateIndex = 0;
			stateFrameIndex = 0;
			skipNextFrame = false;
		}
	}
	private List<CharacterSocket> characterSockets = new ArrayList<CharacterSocket>();
	
	private Dimension feedbackPanelSize;
	
	private Board board;
	private JLayeredPane battlePane;
	//Character Card
	private CharacterCard characterCard;
	private JLabel effectsReadoutBG;
	private JFxLabel effectsReadout;
	private void ToggleEffectReadout(boolean enabled, CharacterBase charBase) {
		if(enabled && charBase.lingeringEffects.size() > 0) {
			String effectsParagraph = "<html>Effects" + "<br>";
			
			for(LingeringEffect lingeringEffect : charBase.lingeringEffects) {
				switch(lingeringEffect.combatEffect.battleItemTypeEffect) {
					case Accelerant:
						effectsParagraph += StatusType.Accelerated.toString();
						break;
					case Damage:
						effectsParagraph += "Poisoned";
						break;
					case Potion:
						effectsParagraph += "Healing";
						break;
					case Status:
						effectsParagraph += lingeringEffect.combatEffect.statusEffect.toString();
						break;
					case Cure:
						effectsParagraph += "Prevent " + lingeringEffect.combatEffect.statusEffect.toString();
						break;
					case Buff:
						for(AttributeMod attriMod : lingeringEffect.combatEffect.attributeMods_buffs)
							effectsParagraph += AttributeModType.GetDisplayNameWithValue(true, attriMod) + "<br>";
						break;
					case Debuff:
						for(AttributeMod attriMod : lingeringEffect.combatEffect.attributeMods_debuffs)
							effectsParagraph += AttributeModType.GetDisplayNameWithValue(false, attriMod) + "<br>";
						break;
					case SpiritTool:
						effectsParagraph += "SpiritTool???";
						break;
					case Revive:
						effectsParagraph += "Auto-Revive";
						break;
					default:
						System.err.println("BattlePanel.ToggleEffectReadout() - Add support for BattleItemType: " + lingeringEffect.combatEffect.battleItemTypeEffect);
						break;
				}
				if(!effectsParagraph.endsWith("<br>"))
					effectsParagraph += "<br>";
			}
			effectsParagraph += "</html>";
			
			effectsReadout.setText(effectsParagraph);
			effectsReadout.setVisible(true);
			effectsReadoutBG.setVisible(true);
		} else {
			effectsReadout.setVisible(false);
			effectsReadoutBG.setVisible(false);
		}
	}
	//Ability Card
	private AbilityCard abilityCard;
	private ItemCard itemCard;
	//Character Placement
	private JPanel selectionPanel;
	private int selectionIndex;
	private Point selectionLocation;
	
	JPanel gridPanel;
	
	
	//CharacterSocket Feedback - Start
	
	public void UpdateMiniEffectFeedback(CharacterBase updatingChar, List<StatusType> activeStatuses, boolean hasAnyBuffs, boolean hasAnyDebuffs, boolean hasAnyCures) {
		CharacterSocket socket = characterSockets.stream().filter(x -> x.ID == updatingChar.GetData().getId()).findFirst().get();
		socket.SetFeedbackState(activeStatuses, hasAnyBuffs, hasAnyDebuffs, hasAnyCures);
	}
	
	private List<CharacterSocket> socketsNeedingFeedback = new ArrayList<CharacterSocket>();
	Timer battlefieldFeedbackTimer = new Timer(600, new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			for(CharacterSocket socket : socketsNeedingFeedback) {
				socket.NextFeedbackFrame();
			}
		}
	});
	
	/**
	 * Called by CharacterBase upon their death. This will probably have multiple uses eventually but for now its used to remove the dead character from the socketsNeedingFeedback list if they're in it.
	 * @param deadChar
	 */
	public void OnCharacterDeath(CharacterBase deadChar) {
		CharacterSocket socket = characterSockets.stream().filter(x -> x.ID == deadChar.GetData().getId()).findFirst().orElse(null);
		if(socket != null && socketsNeedingFeedback.contains(socket)) {
			socketsNeedingFeedback.remove(socket);
			socket.EndFeedback();
		}
	}
	
	//CharacterSocket Feedback - End
	
	
	public ActorData GetPossiblePairedActorDataBy(String charId) {
		CharacterSocket charSocket = characterSockets.stream().filter(x -> x.ID.equals(charId)).findFirst().orElse(null);
		ActorData actorData = null;
		if(charSocket == null)
			System.err.println("BattlePanel.GetPossiblePairedActorDataBy() - Couldn't find a CharacterSocket with id: " + charId);
		else
			actorData = charSocket.possiblePairedActorData;
		return actorData;
	}
	
	/**
	 * Use this to track which manditoryGear items have been equipped so far
	 */
	Map<String,String> manditoryGearIdCharIdAssignments = new HashMap<String,String>();
	
	/**
	 * Use this to reverse the forced displacement of preexisting gear so that it can be automatically reapplied if the non-player teammate who was forced
	 * to wear it is removed from the battle roster.
	 */
	Map<String,List<ItemData>> charIdPreviousGearMap = new HashMap<String,List<ItemData>>();
	
	/**
	 * Use this, first, if there's a forced Player Character and otherwise use it on the first member of the party to be chosen for the battle.
	 * @param charData
	 * @param itemData
	 */
	private void ForceGearEquip(CharacterData charData, ItemData[] itemData) {
		System.out.println("BattlePanel.ForceItemEquip() - force equipping items to: " + charData.getName() + ", item count: " + itemData.length);
		List<ItemData> previousGear = new ArrayList<ItemData>();
		for(ItemData itemDatum : itemData) {
			int index = itemDatum.getStats().getEquipmentType().getValue();
			
			if(charData.IsItemExistingAt(index)) {
				ItemData returnedItem = charData.ReturnItemAtIndex(index);
				previousGear.add(returnedItem);
				Game.Instance().ReceiveItems(new ItemData[] { returnedItem });
			}

			charData.SetItemAtIndex(index, itemDatum);
			
			//Record the item id to save with this player id in order to track who has what equipped with the Map
			manditoryGearIdCharIdAssignments.put(itemDatum.getId(), charData.getId());
		}
		
		charIdPreviousGearMap.put(charData.getId(), previousGear);
	}
	
	/**
	 * Call this when we remove a character from the battle roster
	 * @param charDataId
	 */
	private void TryReverseForcedEquip(CharacterData charData) {
		//If this char is one that has been force-equipped
		if(charIdPreviousGearMap.containsKey(charData.getId())) {
			List<ItemData> previousItems = charIdPreviousGearMap.get(charData.getId());
			//remove returned manditory items from the assignment map
			for(int i = 0; i < EquipmentType.values().length; i++) {
				if(charData.IsItemExistingAt(i)) {
					final ItemData currentlyEquippedItem = charData.GetAt(i);
					//Remove this item if its a manditory one
					if(manditoryGearIdCharIdAssignments.containsKey(currentlyEquippedItem.getId())) {
						manditoryGearIdCharIdAssignments.remove(currentlyEquippedItem.getId());
					
						//Find the previous item for the slot we're reversing
						ItemData previousItemForThisSlot = previousItems.get(i);
						
						//Remove it from the party inventory
						boolean didRemoveFromInventory = Game.Instance().TryConsumeInventoryItems(new ItemData[]{ previousItemForThisSlot });
						if(!didRemoveFromInventory)
							System.err.println("BattlePanel.TryReverseForcedEquip() - OOPS, couldn't remove item from inventory: "
									+ previousItemForThisSlot.getName());
						
						//Overwrite it with the previous gear
						charData.SetItemAtIndex(i, previousItemForThisSlot);
					}
				}
			}
			charIdPreviousGearMap.remove(charData.getId());
			System.out.println("BattlePanel.TryReverseForcedEquip() - Reversed the gear for CharData: " + charData.getName());
		}
	}
	
	
	public void Initialize(Board board, JPanel _gridPanel, BattleState battleStateToRestore) {
		this.board = board;
		this.gridPanel = _gridPanel;
		
		//Do any extra setup involving battleStateToRestore here
		
		
		List<CustomButtonUltra> ultraButtons = new ArrayList<CustomButtonUltra>();
		
		characterSockets.clear();
		
		//Setup our layered pane to hold all character panels
		battlePane = new JLayeredPane();
		//Dimension battlePaneSize = new Dimension(Board.ScaledTileSize() * Board.BoardDimensions().width, Board.ScaledTileSize() * Board.BoardDimensions().height);
		//Dimension battlePaneSize = new Dimension(MapLocationPanel.GetAdjustedTileSize() * Board.BoardDimensions().width, MapLocationPanel.GetAdjustedTileSize() * Board.BoardDimensions().height);
		
		battlePane.setOpaque(false);
		battlePane.setBackground(new Color(0,0,0,0));
		//battlePane.setSize(battlePaneSize);
		//battlePane.setPreferredSize(battlePaneSize);
		//battlePane.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 1));
		
		//Add grid as bottom layer
		GridLayout gridLayout = (GridLayout)gridPanel.getLayout();
		int blankUpperRows = Game.Instance().GetSceneData().sceneHeight - gridLayout.getRows();
		System.out.println("BattlePanel.Initialize() - gridLayout rows: " + gridLayout.getRows() + ", columns: " + gridLayout.getColumns() + ", blankUpperRows: " + blankUpperRows);
		
		gridPanel.setLocation(MapLocationPanel.GetSceneLoc().x, MapLocationPanel.GetSceneLoc().y + (MapLocationPanel.GetAdjustedTileSize() * blankUpperRows));
		gridPanel.setOpaque(false);
		gridPanel.setBackground(new Color(0,0,0,0));
		//gridPanel.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 1));
		
		System.out.println("BattlePanel.Initialize() - gridPanel.size: " + gridPanel.getSize());
		
		battlePane.add(gridPanel, new Integer(0), 0);
		
		//[MISSION_FLOW_EDIT] - Start
		
		feedbackPanelSize = new Dimension(GetCharacterSize().width, GetCharacterSize().width);
		int feedbackXCentering = Math.round(GetCharacterSize().width / 2f) - Math.round(feedbackPanelSize.width / 2f);
		
		if(battleStateToRestore == null || battleStateToRestore.isPlacementPhase) {
			
			//Create enemy character layered panels
			for(CharacterPlan characterPlan : Game.Instance().GetBattleData().EnemyCharacterPlans()) {
				Point location = new Point(characterPlan.Location().x, characterPlan.Location().y);
				
				System.out.println("BattlePanel.Initialize() - sceneHeight: " + Game.Instance().GetSceneData().sceneHeight + " - characterPlan.Location().y: " + characterPlan.Location().y);
				
				SpriteSheet walkSheet = SpriteSheetUtility.GetWalkSheet(characterPlan.Character().getType());
				ImagePanel characterPanel = new ImagePanel(walkSheet.GetSprite(GetAnimIndexFromDirection(characterPlan.Direction()), 1, 1));
				
				Point blankShiftLoc = GetTransformedCoordinate(location);
				
				//Point characterPlacementLoc = GetCharacterPlacementLocation(GetTransformedLocation(blankShiftLoc), GetCharacterSize().height);
				//Trying to shift up placement for settlement layers that've been shifted up during combo scene mergers
				Point transLoc = new Point(blankShiftLoc.x, blankShiftLoc.y - Game.Instance().GetSceneData().settlementSceneOffsetY);
				System.err.println("Adjusting Enemy Placement - blankShiftLoc: " + blankShiftLoc + " - offsetY: " + Game.Instance().GetSceneData().settlementSceneOffsetY + " = transLoc: " + transLoc);
				Point characterPlacementLoc = GetCharacterPlacementLocation(GetTransformedLocation(transLoc), GetCharacterSize().height);
				
				
				characterPanel.setSize(GetCharacterSize());
				characterPanel.setPreferredSize(GetCharacterSize());
				characterPanel.setOpaque(false);
				characterPanel.setBackground(new Color(0,0,0,0));
				//characterPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
				
				Box.Filler stateBlankFrame = new Box.Filler(feedbackPanelSize, feedbackPanelSize, feedbackPanelSize);
				//stateBlankFrame.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
				
				ImagePanel feedbackPanel = new ImagePanel("Question.png");
				feedbackPanel.setSize(feedbackPanelSize);
				feedbackPanel.setPreferredSize(feedbackPanelSize);
				feedbackPanel.setOpaque(false);
				feedbackPanel.setBackground(new Color(0,0,0,0));
				//feedbackPanel.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
				
				JPanel comboPanel = new JPanel();
				comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.Y_AXIS));
				comboPanel.setBounds(characterPlacementLoc.x, characterPlacementLoc.y - feedbackPanelSize.height, GetCharacterSize().width, GetCharacterSize().height + feedbackPanelSize.height);
				comboPanel.setOpaque(false);
				comboPanel.setBackground(new Color(0,0,0,0));
				comboPanel.add(stateBlankFrame);
				comboPanel.add(characterPanel);
				battlePane.add(comboPanel, new Integer(1), 0);
				
				ActorData possiblePairedActorData = GUIManager.MapLocationPanel().getActiveActorDatasBeforeBattle().stream().filter(
						x -> x.characterDataId.equals(characterPlan.Character().getId())).findFirst().orElse(null);
				if(possiblePairedActorData != null)
					System.out.println("BattlePanel.Initialize() - Found matching actor: " + possiblePairedActorData.actorId + ", storing its data in CharacterSocket.");
				
				characterSockets.add(new CharacterSocket(characterPlan.Character().getId(), comboPanel, characterPanel, stateBlankFrame, feedbackPanel, possiblePairedActorData));
				
				System.out.println("BattlePanel.Initialize() - Creating enemy character: " + characterPlan.Character().getName() + ", Location: " + location + ", blankShiftLoc: " + blankShiftLoc);
				CharacterBase charBase = Game.Instance().CreateCharacter_Enemy(characterPlan.Character(), blankShiftLoc);
				charBase.SetDirection(characterPlan.Direction());
				board.GetTileAt(blankShiftLoc).SetOccupant(charBase);
			}
			
			//Create all Placement Slots and forced Player panels
			for(PlacementSlot emptySlot :  Game.Instance().GetBattleData().EmptyAllySlots()) {
				Point blankShiftLoc = GetTransformedCoordinate(emptySlot.point);
				Point characterPlacementLoc = GetCharacterPlacementLocation(GetTransformedLocation(blankShiftLoc), GetCharacterSize().height);
				
				CharacterSocket socket = null;
				if(emptySlot.slotType == SlotType.ForcePlayerChar) {
					CharacterData playerData = Game.Instance().GetPlayerData();
					
					SpriteSheet walkSheet = SpriteSheetUtility.GetWalkSheet(playerData.getType());
					/*ImagePanel imagePanel = new ImagePanel(walkSheet.GetSprite(GetAnimIndexFromDirection(emptySlot.suggestedDirection), 1, 1));
					imagePanel.setBounds(characterPlacementLoc.x, characterPlacementLoc.y, GetCharacterSize().width, GetCharacterSize().height);
					imagePanel.setOpaque(false);
					imagePanel.setBackground(new Color(0,0,0,0));
					battlePane.add(imagePanel, new Integer(1), 0);*/
					ImagePanel characterPanel = new ImagePanel(walkSheet.GetSprite(GetAnimIndexFromDirection(emptySlot.suggestedDirection), 1, 1));
					characterPanel.setSize(GetCharacterSize());
					characterPanel.setPreferredSize(GetCharacterSize());
					characterPanel.setOpaque(false);
					characterPanel.setBackground(new Color(0,0,0,0));
					//characterPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
					
					Box.Filler stateBlankFrame = new Box.Filler(feedbackPanelSize, feedbackPanelSize, feedbackPanelSize);
					//stateBlankFrame.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
					
					ImagePanel feedbackPanel = new ImagePanel("Question.png");
					feedbackPanel.setSize(feedbackPanelSize);
					feedbackPanel.setPreferredSize(feedbackPanelSize);
					feedbackPanel.setOpaque(false);
					feedbackPanel.setBackground(new Color(0,0,0,0));
					//feedbackPanel.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
					
					JPanel comboPanel = new JPanel();
					comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.Y_AXIS));
					comboPanel.setBounds(characterPlacementLoc.x, characterPlacementLoc.y - feedbackPanelSize.height, GetCharacterSize().width, GetCharacterSize().height + feedbackPanelSize.height);
					comboPanel.setOpaque(false);
					comboPanel.setBackground(new Color(0,0,0,0));
					comboPanel.add(stateBlankFrame);
					comboPanel.add(characterPanel);
					battlePane.add(comboPanel, new Integer(1), 0);
					
					ActorData possiblePairedActorData = GUIManager.MapLocationPanel().getActiveActorDatasBeforeBattle().stream().filter(
							x -> x.characterDataId.equals(playerData.getId())).findFirst().orElse(null);
					if(possiblePairedActorData != null)
						System.out.println("BattlePanel.Initialize() - Found player's matching actor: " + possiblePairedActorData.actorId + ", storing its data in CharacterSocket.");
					
					//socket = new CharacterSocket(playerData.getId(), imagePanel, possiblePairedActorData);
					socket = new CharacterSocket(playerData.getId(), comboPanel, characterPanel, stateBlankFrame, feedbackPanel, possiblePairedActorData);
					
					//This is manditory gear management here and for chosen characters later
					if(GUIManager.MapLocationPanel().getManditoryEquipmentForBattle() != null) {
						System.out.println("BattlePanel.Initialize() - Applying all ManditoryGear to playerChar since their forced into this battle.");
						ForceGearEquip(playerData, GUIManager.MapLocationPanel().getManditoryEquipmentForBattle());
					}
					
					System.out.println("BattlePanel.Initialize() - Creating forced ally character: " + playerData.getName() + ", Location: " + characterPlacementLoc + ", blankShiftLoc: " + blankShiftLoc);
					CharacterBase charBase = Game.Instance().CreateCharacter_Ally(playerData, blankShiftLoc);
					charBase.SetDirection(emptySlot.suggestedDirection);
					board.GetTileAt(blankShiftLoc).SetOccupant(charBase);
				} else {
					/*ImagePanel imagePanel = new ImagePanel("character_" + "TEST" + "/Front@2x.png");
					imagePanel.setBounds(characterPlacementLoc.x, characterPlacementLoc.y, GetCharacterSize().width, GetCharacterSize().height);
					imagePanel.setOpaque(false);
					imagePanel.setBackground(new Color(0,0,0,0));
					imagePanel.setVisible(false);
					battlePane.add(imagePanel, new Integer(1), 0);
					socket = new CharacterSocket(null, imagePanel, null);*/
					ImagePanel characterPanel = new ImagePanel("character_" + "TEST" + "/Front@2x.png");
					characterPanel.setSize(GetCharacterSize());
					characterPanel.setPreferredSize(GetCharacterSize());
					characterPanel.setOpaque(false);
					characterPanel.setBackground(new Color(0,0,0,0));
					//characterPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
					
					Box.Filler stateBlankFrame = new Box.Filler(feedbackPanelSize, feedbackPanelSize, feedbackPanelSize);
					//stateBlankFrame.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
					
					ImagePanel feedbackPanel = new ImagePanel("Question.png");
					feedbackPanel.setSize(feedbackPanelSize);
					feedbackPanel.setPreferredSize(feedbackPanelSize);
					feedbackPanel.setOpaque(false);
					feedbackPanel.setBackground(new Color(0,0,0,0));
					//feedbackPanel.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
					
					JPanel comboPanel = new JPanel();
					comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.Y_AXIS));
					comboPanel.setBounds(characterPlacementLoc.x, characterPlacementLoc.y - feedbackPanelSize.height, GetCharacterSize().width, GetCharacterSize().height + feedbackPanelSize.height);
					comboPanel.setOpaque(false);
					comboPanel.setBackground(new Color(0,0,0,0));
					comboPanel.add(stateBlankFrame);
					comboPanel.add(characterPanel);
					battlePane.add(comboPanel, new Integer(1), 0);
					
					socket = new CharacterSocket(null, comboPanel, characterPanel, stateBlankFrame, feedbackPanel, null);
				}
				
				//battlePane.add(imagePanel, new Integer(1), characterIndex);
				//characterIndex++;
				//just lump them all into layer 1, they'll be rearranged after all have been created
				//battlePane.add(imagePanel, new Integer(1), 0);
				
				characterSockets.add(socket);
			}
			
			//Replace the placed ally units
			if(battleStateToRestore != null && battleStateToRestore.isPlacementPhase) {
				for(CharacterBaseData baseData : battleStateToRestore.allyBaseDataMap.values()) {
					Tile tile = board.GetTileAt(baseData.location);
					if(tile.Occupant() == null) {
						PlacementSlot placementSlot = new ArrayList<>( Arrays.asList(Game.Instance().GetBattleData().EmptyAllySlots()) ).stream()
								.filter(x -> x.point == baseData.location).findFirst().get();
						CharacterBase charBase = Game.Instance().CreateCharacter_Ally(baseData);
						charBase.SetDirection(placementSlot.suggestedDirection);
						tile.SetOccupant(charBase);
					}
				}
			}
			
		} else {
			//Create enemy character layered panels
			for(CharacterBaseData baseData : battleStateToRestore.enemyBaseDataMap.values()) {
				Point location = new Point(baseData.location.x, baseData.location.y);
				System.out.println("BattlePanel.Initialize() - sceneHeight: " + Game.Instance().GetSceneData().sceneHeight + " - characterPlan.Location().y: " + baseData.location.y);
				
				SpriteSheet walkSheet = SpriteSheetUtility.GetWalkSheet(baseData.data.getType());
				Point characterPlacementLoc = GetCharacterPlacementLocation(GetTransformedLocation(baseData.location), GetCharacterSize().height);
				
				/*ImagePanel imagePanel = new ImagePanel(walkSheet.GetSprite(GetAnimIndexFromDirection(baseData.direction), 1, 1));
				imagePanel.setBounds(characterPlacementLoc.x, characterPlacementLoc.y, GetCharacterSize().width, GetCharacterSize().height);
				//SetOpaque() has not been called on the character's imagePanel until 9-4-21, thats probably why its been really glitchy with overlapping components
				imagePanel.setOpaque(false);
				imagePanel.setBackground(new Color(0,0,0,0));
				
				//battlePane.add(imagePanel, new Integer(1), characterIndex);
				//characterIndex++;
				//just lump them all into layer 1, they'll be rearranged after all have been created
				battlePane.add(imagePanel, new Integer(1), 0);*/
				ImagePanel characterPanel = new ImagePanel(walkSheet.GetSprite(GetAnimIndexFromDirection(baseData.direction), 1, 1));
				characterPanel.setSize(GetCharacterSize());
				characterPanel.setPreferredSize(GetCharacterSize());
				characterPanel.setOpaque(false);
				characterPanel.setBackground(new Color(0,0,0,0));
				//characterPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
				
				Box.Filler stateBlankFrame = new Box.Filler(feedbackPanelSize, feedbackPanelSize, feedbackPanelSize);
				//stateBlankFrame.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
				
				ImagePanel feedbackPanel = new ImagePanel("Question.png");
				feedbackPanel.setSize(feedbackPanelSize);
				feedbackPanel.setPreferredSize(feedbackPanelSize);
				feedbackPanel.setOpaque(false);
				feedbackPanel.setBackground(new Color(0,0,0,0));
				//feedbackPanel.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
				
				JPanel comboPanel = new JPanel();
				comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.Y_AXIS));
				comboPanel.setBounds(characterPlacementLoc.x, characterPlacementLoc.y - feedbackPanelSize.height, GetCharacterSize().width, GetCharacterSize().height + feedbackPanelSize.height);
				comboPanel.setOpaque(false);
				comboPanel.setBackground(new Color(0,0,0,0));
				comboPanel.add(stateBlankFrame);
				comboPanel.add(characterPanel);
				battlePane.add(comboPanel, new Integer(1), 0);
				
				
				ActorData possiblePairedActorData = GUIManager.MapLocationPanel().getActiveActorDatasBeforeBattle().stream().filter(
						x -> x.characterDataId.equals(baseData.data.getId())).findFirst().orElse(null);
				if(possiblePairedActorData != null)
					System.out.println("BattlePanel.Initialize() - Found matching actor: " + possiblePairedActorData.actorId + ", storing its data in CharacterSocket.");
				
				//characterSockets.add(new CharacterSocket(baseData.data.getId(), imagePanel, possiblePairedActorData));
				characterSockets.add(new CharacterSocket(baseData.data.getId(), comboPanel, characterPanel, stateBlankFrame, feedbackPanel, possiblePairedActorData));
				
				System.out.println("BattlePanel.Initialize() - Creating enemy character: " + baseData.data.getName() + ", Location: " + location + ", transformedLocation: " + baseData.location);
				CharacterBase charBase = Game.Instance().CreateCharacter_Enemy(baseData);
				charBase.SetDirection(baseData.direction);
				board.GetTileAt(baseData.location).SetOccupant(charBase);
			}
			
			//Create all Ally Slots
			for(CharacterBaseData baseData : battleStateToRestore.allyBaseDataMap.values()) {
				Point characterPlacementLoc = GetCharacterPlacementLocation(GetTransformedLocation(baseData.location), GetCharacterSize().height);
				
				CharacterSocket socket = null;

				SpriteSheet walkSheet = SpriteSheetUtility.GetWalkSheet(baseData.data.getType());
				/*ImagePanel imagePanel = new ImagePanel(walkSheet.GetSprite(GetAnimIndexFromDirection(baseData.direction), 1, 1));
				imagePanel.setBounds(characterPlacementLoc.x, characterPlacementLoc.y, GetCharacterSize().width, GetCharacterSize().height);
				//SetOpaque() has not been called on the character's imagePanel until 9-4-21, thats probably why its been really glitchy with overlapping components
				imagePanel.setOpaque(false);
				imagePanel.setBackground(new Color(0,0,0,0));*/
				ImagePanel characterPanel = new ImagePanel(walkSheet.GetSprite(GetAnimIndexFromDirection(baseData.direction), 1, 1));
				characterPanel.setSize(GetCharacterSize());
				characterPanel.setPreferredSize(GetCharacterSize());
				characterPanel.setOpaque(false);
				characterPanel.setBackground(new Color(0,0,0,0));
				//characterPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
				
				Box.Filler stateBlankFrame = new Box.Filler(feedbackPanelSize, feedbackPanelSize, feedbackPanelSize);
				//stateBlankFrame.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
				
				ImagePanel feedbackPanel = new ImagePanel("Question.png");
				feedbackPanel.setSize(feedbackPanelSize);
				feedbackPanel.setPreferredSize(feedbackPanelSize);
				feedbackPanel.setOpaque(false);
				feedbackPanel.setBackground(new Color(0,0,0,0));
				//feedbackPanel.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
				
				JPanel comboPanel = new JPanel();
				comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.Y_AXIS));
				comboPanel.setBounds(characterPlacementLoc.x, characterPlacementLoc.y - feedbackPanelSize.height, GetCharacterSize().width, GetCharacterSize().height + feedbackPanelSize.height);
				comboPanel.setOpaque(false);
				comboPanel.setBackground(new Color(0,0,0,0));
				comboPanel.add(stateBlankFrame);
				comboPanel.add(characterPanel);
				battlePane.add(comboPanel, new Integer(1), 0);
				
				
				ActorData possiblePairedActorData = GUIManager.MapLocationPanel().getActiveActorDatasBeforeBattle().stream().filter(
						x -> x.characterDataId.equals(baseData.data.getId())).findFirst().orElse(null);
				if(possiblePairedActorData != null)
					System.out.println("BattlePanel.Initialize() - Found player's matching actor: " + possiblePairedActorData.actorId + ", storing its data in CharacterSocket.");
				
				//socket = new CharacterSocket(baseData.data.getId(), imagePanel, possiblePairedActorData);
				socket = new CharacterSocket(baseData.data.getId(), comboPanel, characterPanel, stateBlankFrame, feedbackPanel, possiblePairedActorData);
				
				System.out.println("BattlePanel.Initialize() - Creating forced ally character: " + baseData.data.getName() + ", Location: " + characterPlacementLoc + ", transformedLocation: " + baseData.location);
				CharacterBase charBase = Game.Instance().CreateCharacter_Ally(baseData);
				charBase.SetDirection(baseData.direction);
				board.GetTileAt(baseData.location).SetOccupant(charBase);
				
				//battlePane.add(imagePanel, new Integer(1), 0);
				
				characterSockets.add(socket);
			}
		}
		
		//[MISSION_FLOW_EDIT] - End
	
		System.err.println("Create all NPC Allies @ BattlePanel.Initialize()");
		//TODO
		
		//Layer all characters according to their row depth
		List<CharacterBase> allCharacters = new ArrayList<CharacterBase>();
		allCharacters.addAll( Game.Instance().GetAllyCharacterList() );
		allCharacters.addAll( Game.Instance().GetEnemyCharacterList() );
		Map<Integer, List<CharacterBase>> rowGroups = new HashMap<Integer, List<CharacterBase>>();
		for(CharacterBase charBase : allCharacters) {
			Integer row = new Integer(charBase.getLocation().y);
			if(rowGroups.containsKey(row)) {
				rowGroups.get(row).add(charBase);
			} else {
				List<CharacterBase> rowList = new ArrayList<CharacterBase>();
				rowList.add(charBase);
				rowGroups.put(row, rowList);
			}
			GUIManager.MapLocationPanel().HandleBreakawayForCharacterMove(null, charBase.getLocation());
		}
		//now that we've sorted all the characters into groups divided by row, we can set each row group's layer in battlePane
		for(Integer key : rowGroups.keySet()) {
			for(CharacterBase charBase : rowGroups.get(key)) {
				System.out.println("&&&#&&#&#  Row Layering for: " + charBase.GetData().getName());
				CharacterSocket socket = characterSockets.stream().filter(x -> x.ID == charBase.GetData().getId()).findFirst().get();
				battlePane.setLayer(socket.comboPanel, key + 1, 0);
			}
		}
		//reserve these layers
		//Layer 0 : Board
		//Layer 1 - MapHeight : character rows
		//Layer MapHeight+1 - n : gui panels and such
		Integer battlePaneGUILayer = new Integer(Board.BoardDimensions().height + 1);
		System.out.println("Setting Gui layer to: " + battlePaneGUILayer);
		
		
		board.CalcAllMoves();
		
		
		Point cardLocation = GUIUtil.GetRelativePoint(0.015f, 0.02f);
		Dimension cardSize = GUIUtil.GetRelativeSize(0.2f, 0.15f);
		
		Point abilityCardLocation = GUIUtil.GetRelativePoint(0.015f, 0.3f);
		Dimension abilityCardSize = GUIUtil.GetRelativeSize(0.2f, true);

		Point placementLocation = GUIUtil.GetRelativePoint(0.03f, 0.85f);
		
		Dimension effectsReadoutSize = GUIUtil.GetRelativeSize(0.2f, 0.3f);
		effectsReadoutBG = new JLabel(SpriteSheetUtility.ValueBGNinecon());
		effectsReadoutBG.setBounds(cardLocation.x, cardLocation.y + cardSize.height, effectsReadoutSize.width, effectsReadoutSize.height);
		effectsReadoutBG.setVisible(false);
		battlePane.add(effectsReadoutBG, battlePaneGUILayer, 0);
		Dimension effectsReadoutInset = GUIUtil.GetRelativeSize(0.01f, 0.01f);
		effectsReadout = new JFxLabel("Lingering Effects", null, SwingConstants.LEFT, GUIUtil.Body, Color.BLACK);
		effectsReadout.setVerticalAlignment(SwingConstants.TOP);
		effectsReadout.setBounds(cardLocation.x + effectsReadoutInset.width, cardLocation.y + cardSize.height + effectsReadoutInset.height,
				effectsReadoutSize.width - (effectsReadoutInset.width * 2), effectsReadoutSize.height - (effectsReadoutInset.height * 2));
		effectsReadout.setVisible(false);
		battlePane.add(effectsReadout, battlePaneGUILayer, 0);
		
		//Add menu type stuff to render over all units
		characterCard = new CharacterCard(cardLocation, cardSize);
		characterCard.setBounds(cardLocation.x, cardLocation.y, cardSize.width, cardSize.height);
		characterCard.setVisible(false);
		//characterCard.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		//battlePane.add(characterCard, new Integer(3), 13);
		battlePane.add(characterCard, battlePaneGUILayer, 0);
		
		
		abilityCard = new AbilityCard(abilityCardLocation, abilityCardSize);
		abilityCard.setVisible(false);
		battlePane.add(abilityCard, battlePaneGUILayer, 0);
		
		itemCard = new ItemCard(abilityCardLocation, abilityCardSize);
		itemCard.setVisible(false);
		battlePane.add(itemCard, battlePaneGUILayer, 0);
		
		
		selectionPanel = new JPanel(new GridLayout(1, 3));
		selectionPanel.setBounds(placementLocation.x, placementLocation.y, characterCard.getSize().width, GUIUtil.GetRelativeSize(0.08f, false).height);
		selectionPanel.setVisible(false);
		
			JButton cycleLeftButton = new JButton("<");
			cycleLeftButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					CycleSelection(false);
				}
			});
			selectionPanel.add(cycleLeftButton);
			
			JPanel selectionGridPanel = new JPanel(new GridLayout(2, 1));
			
				JButton selectButton = new JButton("Select");
				selectButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						//SelectCharacter();
						SetCharacterPlacement();
					}
				});
				selectionGridPanel.add(selectButton);
				
				JButton clearButton = new JButton("Clear");
				clearButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						ClearCharacter();
					}
				});
				selectionGridPanel.add(clearButton);
				
			selectionPanel.add(selectionGridPanel);
			
			JButton cycleRightButton = new JButton(">");
			cycleRightButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					CycleSelection(true);
				}
			});
			selectionPanel.add(cycleRightButton);
		
		//battlePane.add(selectionPanel, new Integer(3), 14);
		battlePane.add(selectionPanel, battlePaneGUILayer, 0);
		
		//Battle Placement Controls
		//startBattleButton = new JButton("Start Battle");
		JFxLabel startLabel = new JFxLabel("Start Battle", null, SwingConstants.CENTER, GUIUtil.Header, Color.BLACK);
		startBattleButton = new CustomButtonUltra(startLabel, null, Color.GREEN, SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(), Color.DARK_GRAY);
		ultraButtons.add(startBattleButton);
		Point startButLoc = GUIUtil.GetRelativePoint(0.4f, 0.04f);
		Dimension startButSize = GUIUtil.GetRelativeSize(0.2f, 0.12f);
		startBattleButton.setBounds(startButLoc.x, startButLoc.y, startButSize.width, startButSize.height);
		startBattleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				StartOpeningRibbonAnim();
			}
		});
		startBattleButton.setVisible(false);
		//battlePane.add(startBattleButton, new Integer(3), 15);
		battlePane.add(startBattleButton, battlePaneGUILayer, 0);
		
		//Action Panel setup
		
		actionPanel = new JPanel(new GridLayout(5,1));
		actionPanel.setSize(GUIUtil.GetRelativeSize(0.15f, 0.5f));
		actionPanel.setLocation(GUIUtil.GetRelativePoint(0.82f, 0.25f));
		actionPanel.setVisible(false);
			
			JFxLabel moveLabel = new JFxLabel("Move", null, SwingConstants.CENTER, GUIUtil.Header, Color.BLACK);
			moveButton = new CustomButtonUltra(moveLabel, null, Color.GREEN, SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(), Color.DARK_GRAY);
			ultraButtons.add(moveButton);
			moveButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					HideActionPanel();
					cancelButton.setVisible(true);
					
					currentTurnActionState = CharacterTurnActionState.MoveSelection;
					CharacterBase charBase = Game.Instance().GetActiveBattleCharacter();
					board.ShowMoves(charBase);
					board.SaveTurnTakersMoveTiles(charBase);
				}
			});
			actionPanel.add(moveButton);
			
			JFxLabel attackLabel = new JFxLabel("Attack", null, SwingConstants.CENTER, GUIUtil.Header, Color.BLACK);
			attackButton = new CustomButtonUltra(attackLabel, null, Color.GREEN, SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(), Color.DARK_GRAY);
			ultraButtons.add(attackButton);
			attackButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					HideActionPanel();
					cancelButton.setVisible(true);
					
					currentTurnActionState = CharacterTurnActionState.AttackSelection;
					CharacterBase charBase = Game.Instance().GetActiveBattleCharacter();
					board.ShowAttacks(charBase);
				}
			});
			actionPanel.add(attackButton);
			
			JFxLabel abilityLabel = new JFxLabel("Ability", null, SwingConstants.CENTER, GUIUtil.Header, Color.BLACK);
			abilityButton = new CustomButtonUltra(abilityLabel, null, Color.GREEN, SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(), Color.DARK_GRAY);
			ultraButtons.add(abilityButton);
			abilityButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					//They need to choose an ability from their available list
					HideActionPanel();
					//Show learned ability list
					currentTurnActionState = CharacterTurnActionState.AbilityMenu;
					UpdateAbilityPanel();
					
					abilityPanel.setVisible(true);
					abilityBG.setVisible(true);
					abilityPanel.setEnabled(true);
					cancelButton.setVisible(true);
				}
			});
			actionPanel.add(abilityButton);
			
			JFxLabel itemLabel = new JFxLabel("Item", null, SwingConstants.CENTER, GUIUtil.Header, Color.BLACK);
			itemButton = new CustomButtonUltra(itemLabel, null, Color.GREEN, SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(), Color.DARK_GRAY);
			ultraButtons.add(itemButton);
			itemButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					//They need to choose an ability from their available list
					HideActionPanel();
					//Show learned ability list
					currentTurnActionState = CharacterTurnActionState.ItemMenu;
					UpdateItemPanel();
					
					itemPanel.setVisible(true);
					itemBG.setVisible(true);
					itemPanel.setEnabled(true);
					cancelButton.setVisible(true);
				}
			});
			actionPanel.add(itemButton);
			
			JFxLabel waitLabel = new JFxLabel("Wait", null, SwingConstants.CENTER, GUIUtil.Header, Color.BLACK);
			CustomButtonUltra waitButton = new CustomButtonUltra(waitLabel, null, Color.GREEN, SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(), Color.DARK_GRAY);
			ultraButtons.add(waitButton);
			waitButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					HideActionPanel();
					cancelButton.setVisible(true);
					
					currentTurnActionState = CharacterTurnActionState.WaitSelection;
					ChooseDirection();
				}
			});
			actionPanel.add(waitButton);
		//battlePane.add(actionPanel, new Integer(3), 16);
		battlePane.add(actionPanel, battlePaneGUILayer, 0);
		
		JFxLabel confirmLabel = new JFxLabel("Confirm", null, SwingConstants.CENTER, GUIUtil.Header, Color.BLACK);
		confirmButton = new CustomButtonUltra(confirmLabel, null, Color.GREEN, SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(), Color.DARK_GRAY);
		ultraButtons.add(confirmButton);
		Point confirmPos = GUIUtil.GetRelativePoint(0.82f, 0.75f);
		Dimension confirmSize = GUIUtil.GetRelativeSize(0.15f, 0.1f);
		confirmButton.setBounds(confirmPos.x, confirmPos.y, confirmSize.width, confirmSize.height);
		confirmButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(userChosenAbility != null)
					ConfirmAbility();
				else
					ConfirmItem();
			}
		});
		//battlePane.add(cancelButton, new Integer(3), 17);
		battlePane.add(confirmButton, battlePaneGUILayer, 0);
		confirmButton.setVisible(false);
		
		JFxLabel cancelLabel = new JFxLabel("Cancel", null, SwingConstants.CENTER, GUIUtil.Header, Color.BLACK);
		cancelButton = new CustomButtonUltra(cancelLabel, null, Color.GREEN, SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(), Color.DARK_GRAY);
		ultraButtons.add(cancelButton);
		Point cancelPos = GUIUtil.GetRelativePoint(0.82f, 0.85f);
		Dimension cancelSize = GUIUtil.GetRelativeSize(0.15f, 0.1f);
		cancelButton.setBounds(cancelPos.x, cancelPos.y, cancelSize.width, cancelSize.height);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				CancelAction();
			}
		});
		//battlePane.add(cancelButton, new Integer(3), 17);
		battlePane.add(cancelButton, battlePaneGUILayer, 0);
		cancelButton.setVisible(false);
		
		
		Point terrainPanelBGLoc = GUIUtil.GetRelativePoint(0.38f, 0.82f);
		Dimension terrainPanelBGSize = GUIUtil.GetRelativeSize(0.22f, 0.14f);
		Point terrainPanelLoc = GUIUtil.GetRelativePoint(0.39f, 0.83f);
		Dimension terrainPanelSize = GUIUtil.GetRelativeSize(0.2f, 0.12f);
		
		terrainPanelBG = new JLabel(SpriteSheetUtility.ValueBGNinecon());
		terrainPanelBG.setBounds(terrainPanelBGLoc.x, terrainPanelBGLoc.y, terrainPanelBGSize.width, terrainPanelBGSize.height);
		terrainPanelBG.setVisible(false);
		//battlePane.add(terrainPanelBG, new Integer(3), 19);
		battlePane.add(terrainPanelBG, battlePaneGUILayer, 0);
		
		
		//Ability Menu
		abilityBG = new JLabel(SpriteSheetUtility.ValueBGNinecon());
		abilityBG.setOpaque(false);
		abilityBG.setBackground(new Color(0,0,0,0));
		abilityBG.setBounds(new Rectangle(GUIUtil.GetRelativePoint(0.82f, 0.25f), GUIUtil.GetRelativeSize(0.15f, 0.49f)));
		abilityBG.setVisible(false);
		battlePane.add(abilityBG, battlePaneGUILayer, 0);
		
		Ability[] abilityArray = new Ability[] { AbilityManager.getAbility_barrage(), AbilityManager.getAbility_baseArmor(), AbilityManager.getAbility_cannonFire() };
		abilityJList = new JList<Ability>(abilityArray);
		abilityJList.setCellRenderer(new AbilityListCellRenderer());
		abilityJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		abilityJList.setLayoutOrientation(JList.VERTICAL);
		abilityJList.setVisibleRowCount(-1);
		abilityJList.setBackground(SpriteSheetUtility.ValueBGColor);
		abilityJList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
			    if(e.getValueIsAdjusting() == false) {
			    	if(abilityJList.getSelectedIndex() != -1)
			    		SelectAbility(abilityJList.getSelectedIndex());
			    }
			}
		});
		
		abilityPanel = new JScrollPane(abilityJList);
		abilityPanel.setSize(GUIUtil.GetRelativeSize(0.13f, 0.46f));
		abilityPanel.setLocation(GUIUtil.GetRelativePoint(0.83f, 0.265f));
		abilityPanel.setVisible(false);
		abilityPanel.setEnabled(false);
		abilityPanel.setBorder(null);
		battlePane.add(abilityPanel, battlePaneGUILayer, 0);
		
		
		//Item Menu
		itemBG = new JLabel(SpriteSheetUtility.ValueBGNinecon());
		itemBG.setOpaque(false);
		itemBG.setBackground(new Color(0,0,0,0));
		itemBG.setBounds(new Rectangle(GUIUtil.GetRelativePoint(0.82f, 0.25f), GUIUtil.GetRelativeSize(0.15f, 0.49f)));
		itemBG.setVisible(false);
		battlePane.add(itemBG, battlePaneGUILayer, 0);
		
		ItemData[] itemDataArray = new ItemData[] { Items.getById(ItemDepot.AdamantBranch.getId()), Items.getById(ItemDepot.AmethystKunai.getId()), Items.getById(ItemDepot.BambooSpear.getId()) };
		itemDataJList = new JList<ItemData>(itemDataArray);
		itemDataJList.setCellRenderer(new ItemListCellRenderer());
		itemDataJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		itemDataJList.setLayoutOrientation(JList.VERTICAL);
		itemDataJList.setVisibleRowCount(-1);
		itemDataJList.setBackground(SpriteSheetUtility.ValueBGColor);
		itemDataJList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
			    if(e.getValueIsAdjusting() == false) {
			    	if(itemDataJList.getSelectedIndex() != -1)
			    		SelectItem(itemDataJList.getSelectedIndex());
			    }
			}
		});
		
		itemPanel = new JScrollPane(itemDataJList);
		itemPanel.setSize(GUIUtil.GetRelativeSize(0.13f, 0.46f));
		itemPanel.setLocation(GUIUtil.GetRelativePoint(0.83f, 0.265f));
		itemPanel.setVisible(false);
		itemPanel.setEnabled(false);
		itemPanel.setBorder(null);
		battlePane.add(itemPanel, battlePaneGUILayer, 0);
		
		
		terrainPanel = new JPanel(new GridLayout(2, 2));
		terrainPanel.setOpaque(false);
		terrainPanel.setBackground(new Color(0,0,0,0));
		terrainPanel.setBounds(terrainPanelLoc.x, terrainPanelLoc.y, terrainPanelSize.width, terrainPanelSize.height);
		//terrainPanel.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		terrainPanel.setVisible(false);
			//JLabel nameLabel = new JLabel("Terrain:");
			JFxLabel nameLabel = new JFxLabel("Location:", null, SwingConstants.LEFT, GUIUtil.Header, Color.BLACK);
			terrainPanel.add(nameLabel);
			//terrainName = new JLabel("?");
			terrainName = new JFxLabel("?", null, SwingConstants.LEFT, GUIUtil.Header, Color.BLACK);
			terrainPanel.add(terrainName);
			//JLabel penaltyLabel = new JLabel("Penalty:");
			JLabel penaltyLabel = new JFxLabel("Penalty:", null, SwingConstants.LEFT, GUIUtil.Header, Color.BLACK);
			terrainPanel.add(penaltyLabel);
			//terrainPenalty = new JLabel("?");
			terrainPenalty = new JFxLabel("?", null, SwingConstants.LEFT, GUIUtil.Header, Color.BLACK);
			terrainPanel.add(terrainPenalty);
		//battlePane.add(terrainPanel, new Integer(3), 18);
		battlePane.add(terrainPanel, battlePaneGUILayer, 0);
		
		Point targetInfoScrollPaneBGLoc = GUIUtil.GetRelativePoint(0f, 0f);
		Dimension targetInfoScrollPaneBGSize = GUIUtil.GetRelativeSize(0.24f, 0.267f);
		targetInfoScrollPaneBG = new JLabel(SpriteSheetUtility.HighlightBGNinecon());
		targetInfoScrollPaneBG.setBounds(targetInfoScrollPaneBGLoc.x, targetInfoScrollPaneBGLoc.y, targetInfoScrollPaneBGSize.width, targetInfoScrollPaneBGSize.height);
		targetInfoScrollPaneBG.setVisible(false);
		battlePane.add(targetInfoScrollPaneBG, battlePaneGUILayer, 0);
		
		Point targetsLabelLoc = GUIUtil.GetRelativePoint(0.01f, 0f);
		Dimension targetsLabelSize = GUIUtil.GetRelativeSize(0.22f, 0.05f);
		targetsLabel = new JFxLabel("Targets:", null, SwingConstants.LEFT, GUIUtil.Header, Color.BLACK);
		targetsLabel.setBounds(targetsLabelLoc.x, targetsLabelLoc.y, targetsLabelSize.width, targetsLabelSize.height);
		targetsLabel.setVisible(false);
		//targetsLabel.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
		battlePane.add(targetsLabel, battlePaneGUILayer, 0);
		
		Point targetInfoScrollPaneLoc = GUIUtil.GetRelativePoint(0.015f, 0.045f);
		Dimension targetInfoScrollPaneSize = GUIUtil.GetRelativeSize(0.21f, 0.205f);
		//TargetInfo[] targetInfoArray = new TargetInfo[] { };
		targetInfoJList = new JList<TargetInfo>();
		targetInfoJList.setCellRenderer(new CharacterInfoCellRenderer());
		targetInfoJList.setLayoutOrientation(JList.VERTICAL);
		targetInfoJList.setVisibleRowCount(-1);
		targetInfoJList.setBackground(SpriteSheetUtility.ValueBGColor);
		
		targetInfoScrollPane = new JScrollPane(targetInfoJList);
		targetInfoScrollPane.setSize(targetInfoScrollPaneSize);
		targetInfoScrollPane.setLocation(targetInfoScrollPaneLoc);
		
		targetInfoScrollPane.getVerticalScrollBar().setUI(new AgentsScrollBarUI());
		targetInfoScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		targetInfoScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		targetInfoScrollPane.setVisible(false);
		targetInfoScrollPane.setEnabled(false);
		targetInfoScrollPane.setBorder(null);
		//targetInfoScrollPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 3, true));
		
		battlePane.add(targetInfoScrollPane, battlePaneGUILayer, 0);
		
		battlePane.addMouseWheelListener(new MouseWheelListener() {
			int lastIndex = 0;
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				//ScrollBar vertical = targetInfoScrollPane.getVerticalScrollBar();
				//if(e.getWheelRotation() > 0)
				//	vertical.setValue( vertical.getMaximum() );
				//else
				//	vertical.setValue( vertical.getMinimum() );
				if(targetInfoJList.getModel().getSize() == 0)
					return;
				
				scrollTargetInfoIndex = Math.min(Math.max(0, scrollTargetInfoIndex + e.getWheelRotation()), targetInfoJList.getModel().getSize()-1);
				
				if(scrollTargetInfoIndex != lastIndex) {
					System.out.println("Mouse Wheel, scrollTargetInfoIndex: " + scrollTargetInfoIndex);
					targetInfoJList.ensureIndexIsVisible(scrollTargetInfoIndex);
				}
				lastIndex = scrollTargetInfoIndex;
			}
		});
		
		
		resultsPane = new ResultsPane(new Point2D.Float(0.75f, 0.75f),
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					resultsPane.setVisible(false);
					GUIManager.MapLocationPanel().ApplyInteraction(didWinBattle); //We know they've won if we're here
				}
			}
		);
		battlePane.add(resultsPane, battlePaneGUILayer, 0);
		resultsPane.setVisible(false);
		
		combatAnimPane = new CombatAnimPane(this, GUIUtil.GetRelativeSize(1f, 1f));
		//battlePane.add(combatAnimPane, new Integer(3), 20);
		battlePane.add(combatAnimPane, battlePaneGUILayer, 0);
		
		
		//Action Label
		float actionLabelWidth = 0.35f;
		Point actionLabelBGLoc = GUIUtil.GetRelativePoint(0.5f - (actionLabelWidth/2f), 0.05f);
		Dimension actionLabelBGSize = GUIUtil.GetRelativeSize(actionLabelWidth, 0.08f);
		
		actionLabelBG = new JLabel(SpriteSheetUtility.ValueBGNinecon());
		actionLabelBG.setBounds(actionLabelBGLoc.x, actionLabelBGLoc.y, actionLabelBGSize.width, actionLabelBGSize.height);
		actionLabelBG.setVisible(false);
		battlePane.add(actionLabelBG, battlePaneGUILayer, 0);
		
		actionLabel = new JFxLabel("Action Name", null, SwingConstants.CENTER, GUIUtil.ItalicHeader_L, Color.BLACK);
		actionLabel.setBounds(actionLabelBGLoc.x, actionLabelBGLoc.y, actionLabelBGSize.width, actionLabelBGSize.height);
		actionLabel.setVisible(false);
		battlePane.add(actionLabel, battlePaneGUILayer, 0);
		
		
		battleMessageRibbon = new JPanel(new GridLayout(1,1));
		Dimension size = GUIUtil.GetRelativeSize(0.2f, 0.12f);
		int xLoc = GUIUtil.GetRelativePoint(0.4f, 0f).x;
		battleMessageRibbon.setBounds(xLoc, battleMessageRibbonY, size.width, size.height);
		battleMessageRibbon.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		
			battleMessageLabel = new JFxLabel("Battle Message", SwingConstants.CENTER, GUIUtil.ItalicHeader, Color.BLACK);
			battleMessageRibbon.add(battleMessageLabel);
			
		battlePane.add(battleMessageRibbon, battlePaneGUILayer, 0);
		
		
		this.add(battlePane, BorderLayout.CENTER);
		
		
		//Make all the buttons aware of eachother
		for(CustomButtonUltra but : ultraButtons) {
			but.AddGroupList(ultraButtons);
		}
		
		//[MISSION_FLOW_EDIT]
		if(battleStateToRestore == null || battleStateToRestore.isPlacementPhase) {
		
			//Now that everything is initiated, check whether all the placement slots are already filled by Forced type slots
			if(Game.Instance().GetBattleData().EmptyAllySlots().length == Game.Instance().GetAllyCharacterList().size()) {
				//set any variables relating to character placement phase so that we dont get those kinds of interactions when triggering mouse events on tiles
				isSelectionMaxedOut = true;
				startBattleButton.setVisible(true);
			}
		
		//[MISSION_FLOW_EDIT]
		} else {
			StartBattlePhase();
			
			//Start the turn for the character who goes next
			Game.Instance().StartRestoredBattle(battleStateToRestore, board);
		}
		
		
		//DEBUGGING
		/*System.err.println("DEBUGGING @ BattlePanel.Initialize() - Displaying all StatusEffect Mini-Feedbacks on the first enemy. (This doesn't actually apply the LingeringEffects themselves)");
		List<StatusType> statusEffects = new ArrayList<StatusType>();
		statusEffects.add(StatusType.Blind);
		statusEffects.add(StatusType.Silence);
		statusEffects.add(StatusType.Goad);
		statusEffects.add(StatusType.Daze);
		statusEffects.add(StatusType.Charmed);
		statusEffects.add(StatusType.Cripple);
		statusEffects.add(StatusType.Fear);
		statusEffects.add(StatusType.Accelerated);
		UpdateMiniEffectFeedback(Game.Instance().GetEnemyCharacterList().get(0), statusEffects, true, true, true);*/
	}
	
	//Ability Menu - Start
	
	private CustomButtonUltra abilityButton;
	private JScrollPane abilityPanel;
	private JLabel abilityBG;
	
	//List<Ability> abilityChoices;
	JList<Ability> abilityJList;
	JList<ItemData> itemDataJList;
	
	/**
	 * This gets called when the user selects the "Ability" action button.
	 */
	private void UpdateAbilityPanel() {
		//System.out.println("BattlePanel.UpdateAbilityPanel()");
		
		
		//TODO restore after debugging
		//List<Ability> abilityChoices = Game.Instance().GetActiveBattleCharacter().GetData().GetLearnedAbilities();
		//abilityChoices.removeIf(x -> !x.isActiveAbility);
		//DEBUGGING
		System.err.println("DEBUGGING @ BattlePanel.UpdateAbilityPanel() - Makes available every ability in the game.");
		List<Ability> abilityChoices = combatAnimPane.DEBUG_GetEveryActiveAbility();
		
		
		//Update the ability scrollPanel UI
		abilityJList.setListData(abilityChoices.stream().toArray(Ability[]::new));
		abilityJList.updateUI();
		abilityJList.setSelectedIndex(0);
	}
	
	//Called by button
	private void SelectAbility(int index) {
		System.out.println("BattlePanel.SelectAbility() - index: " + index);
		
		userChosenAbility = abilityJList.getModel().getElementAt(index);
		
		confirmButton.setVisible(true);
		board.ShowAbilityAttacks(Game.Instance().GetActiveBattleCharacter(), userChosenAbility);
		
		abilityCard.DisplayAbility(userChosenAbility);
		abilityCard.setVisible(true);
	}
	
	private void ConfirmAbility() {
		//Close menus and stuff like we do when choosing attak
		abilityPanel.setVisible(false);
		abilityBG.setVisible(false);
		HideActionPanel();
		confirmButton.setVisible(false);
		
		cancelButton.setVisible(true);
		
		currentTurnActionState = CharacterTurnActionState.AbilitySelection;
		
		//abilityCard.setVisible(false);
	}
	
	//Ability Menu - End
	
	//Item Menu - Start
	
	private void UpdateItemPanel() {
		System.out.println("BattlePanel.UpdateItemPanel()");
	
		List<ItemData> relevantInventory = Game.Instance().GetBattleItemsFromInventory();
		
		//Update the ability scrollPanel UI
		itemDataJList.setListData(relevantInventory.stream().toArray(ItemData[]::new));
		itemDataJList.updateUI();
		itemDataJList.setSelectedIndex(0);
	}
	
	//Called by button
	private void SelectItem(int index) {
		System.out.println("BattlePanel.SelectItem() - index: " + index);
		
		userChosenItem = itemDataJList.getModel().getElementAt(index);
		
		confirmButton.setVisible(true);
		board.ShowItemTiles(Game.Instance().GetActiveBattleCharacter(), userChosenItem);
		
		itemCard.DisplayItem(userChosenItem);
		itemCard.setVisible(true);
	}
	
	private void ConfirmItem() {
		//Close menus and stuff like we do when choosing an item
		itemPanel.setVisible(false);
		itemBG.setVisible(false);
		HideActionPanel();
		confirmButton.setVisible(false);
		
		cancelButton.setVisible(true);
		
		currentTurnActionState = CharacterTurnActionState.ItemSelection;
		
		//itemCard.setVisible(false);
	}
	
	//Item Menu - End
	
	/**
	 * This is called from Game when starting combat anim/s.
	 */
	public void PrepareUIForCombatAnim() {
		abilityCard.setVisible(false);
		itemCard.setVisible(false);
	}
	
	/**
	 * Inverts and offsets the y position to have its origin at the bottom left, instead of Java's natural origin at the top left.
	 * @param naturalCoord
	 * @return
	 */
	public Point GetTransformedCoordinate(Point naturalCoord) {
		int blankUpperRows = Game.Instance().GetSceneData().sceneHeight - Board.BoardDimensions().height;
		return new Point(naturalCoord.x,  Game.Instance().GetSceneData().sceneHeight - blankUpperRows - 1 - naturalCoord.y);
	}
	
	public boolean IsPlacementTileAForcedType(Point location) {
		//get the flipped version of the location
		location = GetTransformedCoordinate(location);
		
		PlacementSlot matchingSlot = null;
		for(PlacementSlot slot : Game.Instance().GetBattleData().EmptyAllySlots()) {
			if(slot.point.equals(location)) {
				matchingSlot = slot;
				break;
			}
		}
		if(matchingSlot == null) {
			System.out.println("BattlePanel.IsPlacementTileAForcedType() - Didn't find a placement slot at: " + location);
			for(PlacementSlot slot : Game.Instance().GetBattleData().EmptyAllySlots()) {
				System.out.println("Slot Coord: " + slot.point);
			}
			return false;
		} else {
			return matchingSlot.slotType == SlotType.ForcePlayerChar;
		}
	}
	
	//Battle Panels and Grid Interaction - Start
	
	private JPanel terrainPanel;
	private JLabel terrainPanelBG;
	private JLabel terrainName;
	private JLabel terrainPenalty;
	
	private JLabel actionLabelBG;
	private JFxLabel actionLabel;
	
	public void ToggleActionLabel(String labelText) {
		if(labelText == null) {
			actionLabelBG.setVisible(false);
			actionLabel.setVisible(false);
		} else {
			actionLabelBG.setVisible(true);
			actionLabel.setText(labelText);
			actionLabel.setVisible(true);
		}
	}
	
	private boolean ignoreMouseEvents;
	public boolean IgnoringMouseEvents() { return ignoreMouseEvents; }
	public void ToggleIgnoreMouseEvents(boolean ignore) {
		ignoreMouseEvents = ignore;
	}
	
	//used to hide everything once an action is chosen
	private void HidePanels() {
		ToggleTerrainPanel(false, null, null);
		HideCharacterCard();
		HideActionPanel();
	}
	
	public void ToggleTerrainPanel(boolean enabled, TileData tileData, Point focusedTileLocation) {
		terrainPanel.setVisible(enabled);
		terrainPanelBG.setVisible(enabled);
		if(enabled) {
			
			//-Applying a workaround that displays the excepted sequencial grid coord while leaving the settlement coordinates alone so they can still
			//be within the data-bound ranges of scene information; like breakaway zones, etc. 
			//terrainName.setText(tileData.gridLocationX + ", " + tileData.gridLocationY);
			terrainName.setText(tileData.comboSceneOffsetLocX + ", " + tileData.comboSceneOffsetLocY);
			
			
			//terrainName.setText(tileData.gridLocationX + ", " + (Board.BoardDimensions().height - 1 - tileData.gridLocationY));
			terrainPenalty.setText("" + (tileData.isPassable ? tileData.penalty : "Impassable"));
		}
	}
	
	/**
	 * Called by Board.FocusTile() for an attackable tile that has any targets.
	 * @param targetedCharacters
	 */
	public void ShowTargets(InteractiveActionType actionType, List<CharacterBase> targetedCharacters) {
		System.out.println("BattlePanel.ShowTargets()");
		Game.Instance().CreateTargetInfos(actionType, userChosenAbility, userChosenItem, targetedCharacters);
		ToggleTargetInfoPanel(true, Game.Instance().getCurrentTargetInfos());
	}
	
	public void HideTargets() {
		ToggleTargetInfoPanel(false, null);
	}
	
	private JLabel targetInfoScrollPaneBG;
	private JFxLabel targetsLabel;
	private JScrollPane targetInfoScrollPane;
	//These are only setup to support a max of 5 targeted characters(1 highlight charCard + 4 targetCards) at once, will need additional setup to display more
	private JList<TargetInfo> targetInfoJList = new JList<TargetInfo>();
	int scrollTargetInfoIndex;
	
	private void ToggleTargetInfoPanel(boolean enabled, List<TargetInfo> targetInfos) {
		//Update this JList with targetInfos
		if(enabled) {
			scrollTargetInfoIndex = 0;
			targetInfoJList.setListData(targetInfos.stream().toArray(TargetInfo[]::new));
			targetInfoJList.updateUI();
			targetInfoJList.ensureIndexIsVisible(0);
			
			targetInfoScrollPaneBG.setVisible(true);
			targetsLabel.setVisible(true);
			targetInfoScrollPane.setVisible(true);
			
			//Hide the default selection characterCard if its showing
			characterCard.setVisible(false);
			ToggleEffectReadout(false, null);
		} else {
			targetInfoScrollPaneBG.setVisible(false);
			targetsLabel.setVisible(false);
			targetInfoScrollPane.setVisible(false);
		}
	}
	
	public void ShowCharacterCard(CharacterData charData, Point focusedTileLocation) {
		//Suppress the character card if the TargetInfos are showing
		if(targetInfoScrollPane.isVisible()) {
			characterCard.setVisible(false);
			ToggleEffectReadout(false, null);
			return;
		}
		
		CharacterBase existingCharBase = Game.Instance().FindTargetCharacterBase(charData);
		if(existingCharBase != null) {
			characterCard.DisplayCharacter(existingCharBase);
			ToggleEffectReadout(true, existingCharBase);
		} else
			characterCard.DisplayCharacter(charData);
		
		characterCard.setVisible(true);
	}
	
	public void HideCharacterCard() {
		characterCard.setVisible(false);
		ToggleEffectReadout(false, null);
	}
	
	private JPanel actionPanel;
	private CustomButtonUltra moveButton;
	private CustomButtonUltra attackButton;
	
	private CustomButtonUltra itemButton;
	private JScrollPane itemPanel;
	private JLabel itemBG;
	
	private CharacterTurnActionState currentTurnActionState = CharacterTurnActionState.ActionMenu;
	public CharacterTurnActionState GetCurrentTurnActionState() { return currentTurnActionState; }
	
	public void ShowActionPanel(CharacterBase charBase) {
		//Change our state because we're returning to the menu
		currentTurnActionState = CharacterTurnActionState.ActionMenu;
		
		boolean canMove = !charBase.HasMoved() && charBase.CanMove();
		boolean canAttack = !charBase.HasUsedAction() && charBase.CanPerformAttack();
		boolean canUseAbility = !charBase.HasUsedAction() && charBase.CanPerformAbility();
		boolean canUseItem = !charBase.HasUsedAction() && Game.Instance().GetInventory().stream().anyMatch(x -> x.getType() == ItemType.BattleItem);
		
		//Check if the character has no available actions left and if so then end turn
		if(!canMove && !canAttack && !canUseItem) {
			Game.Instance().EndTurn();
		} else {
			actionPanel.setVisible(true);
			
			moveButton.setEnabled(canMove);
			attackButton.setEnabled(canAttack);
			abilityButton.setEnabled(canUseAbility);
			itemButton.setEnabled(canUseItem);
		}
	}
	
	public void HideActionPanel() {
		actionPanel.setVisible(false);
	}
	
	ResultsPane resultsPane;
	boolean didWinBattle;
	
	public void ShowBattleResults(boolean didWin) {
		didWinBattle = didWin;
		System.out.println("BattlePanel.ShowBattleResults() - didWin: " + didWinBattle);
		
		//Update all necessary elements of the results panel
		//Mission governingMission = Missions.getById(MapLocationPanel.GetCurrentLocation().getGoverningMissionId());
		Mission activeMission = GUIManager.MapLocationPanel().GetActiveMission();
		ItemData[] battleRewards = null;
		if(activeMission != null)
			battleRewards = activeMission.getRewards();
		resultsPane.UpdateResults(Game.Instance().GetBattleData().GetName(), didWinBattle, null, battleRewards);
		//Show panel
		resultsPane.setVisible(true);
	}
	
	//Battle Panels and Grid Interactions - End
	
	//Character Placement - Start
	
	//private boolean isSelectingCharacter;
	private List<CharacterData> selectionChoices = new ArrayList<CharacterData>();
	private boolean isSelectionMaxedOut;
	public boolean IsSelectionMaxedOut() { return isSelectionMaxedOut; }
	
	public void ToggleCharacterSelection(boolean enabled, Point location) {
		//isSelectingCharacter = enabled;
		if(enabled) {
			selectionChoices.clear();
			if(board.GetTileAt(location).Occupant() != null)
				selectionChoices.add(board.GetTileAt(location).Occupant().GetData());
			
			for(CharacterData charData : Game.Instance().GetAvailableBattleRoster())
				selectionChoices.add(charData);
			
			selectionLocation = location;
			
			selectionIndex = 0;
			CharacterData charData = selectionChoices.get(selectionIndex);
			ShowCharacterCard(charData, location);
			
			selectionPanel.setVisible(true);
			
			startBattleButton.setVisible(false);
			//This feels awkward while placing units
			ToggleTerrainPanel(false, null, null);
			
			ShowCharacterPlacement();
		} else {
			HideCharacterCard();
			
			selectionPanel.setVisible(false);
			
			int manditorySlotCount = 0;
			for(PlacementSlot slot : Game.Instance().GetBattleData().EmptyAllySlots()) {
				if(slot.slotType != SlotType.AnyChar_Optional)
					manditorySlotCount++;
			}
			
			int allyFilledSocketCount = (int)characterSockets.stream().filter(x -> selectionChoices.stream().anyMatch(c -> c.getId() == x.ID) && x.ID != null).count();
			if(Game.Instance().GetAvailableBattleRoster().length == 0
			   || 
			   allyFilledSocketCount >= manditorySlotCount
			  )
			{
				//System.out.println("ToggleCharacterSelection() - allyFilledSocketCount: " + allyFilledSocketCount + " >= manditorySlotCount: " + manditorySlotCount + " ???");
				//if(allyFilledSocketCount > 0)
				//	System.out.println("	-First Socket occupied by ID: " + characterSockets.get(0).ID);
				
				isSelectionMaxedOut = true;
				startBattleButton.setVisible(true);
			}
		}
	}
	
	private void CycleSelection(boolean cycleRight) {
		//Cycling creates artifacts on the battle image when there is only one character in the party
		if(selectionChoices.size() == 1)
			return;
		
		if(cycleRight) {
			selectionIndex++;
			if(selectionIndex >= selectionChoices.size())
				selectionIndex = 0;
		} else {
			selectionIndex--;
			if(selectionIndex <= -1)
				selectionIndex = selectionChoices.size() - 1;
		}
		
		characterCard.DisplayCharacter(selectionChoices.get(selectionIndex));
		
		ShowCharacterPlacement();
	}
	
	private int GetAllySlotIndex() {
		int enemySocketCount = Game.Instance().GetEnemyCharacterList().size();
		int slotIndex = 0;
		PlacementSlot[] slots = Game.Instance().GetBattleData().EmptyAllySlots();
		for(int i = 0; i < slots.length; i++) {
			if(slots[i].point.equals(selectionLocation)) {
				slotIndex = i;
				break;
			}
		}
		return enemySocketCount + slotIndex;
	}
	
	private void ShowCharacterPlacement() {
		//Set character
		CharacterData selectedCharacter = selectionChoices.get(selectionIndex);
		
		CharacterSocket socket = characterSockets.get(GetAllySlotIndex());
		socket.ID = selectedCharacter.getId();
		//characterSockets.get(socketIndex).Panel.SetNewImage("character_" + selectedCharacter.getType().toString() + "/Front@2x.png");
		SpriteSheet walkSheet = SpriteSheetUtility.GetWalkSheet(selectedCharacter.getType());
		socket.characterPanel.SetNewImage(walkSheet.GetSprite(1, 1, 1));
		socket.characterPanel.setVisible(true);
		socket.characterPanel.setOpaque(true);
		
		//These must be set again after calling SetNewImage()
		//int size = MapLocationPanel.GetAdjustedTileSize();
		//socket.Panel.setSize(size, size);
		//socket.Panel.setLocation(selectionLocation.x * size, selectionLocation.y * size);
		Point characterPlacementLoc = GetCharacterPlacementLocation(GetTransformedLocation(selectionLocation), GetCharacterSize().height);
		socket.SetAllBounds(characterPlacementLoc, GetCharacterSize());
	}
	
	private Point GetTransformedLocation(Point javaNaturalCoord) {
		int sceneHeight = Game.Instance().GetSceneData().sceneHeight;
		GridLayout gridLayout = (GridLayout)gridPanel.getLayout();
		int blankUpperRows = sceneHeight - gridLayout.getRows();
		int size = MapLocationPanel.GetAdjustedTileSize();
		Point blankShiftLocation = new Point(MapLocationPanel.GetSceneLoc().x + (javaNaturalCoord.x * size), MapLocationPanel.GetSceneLoc().y + ((javaNaturalCoord.y + blankUpperRows) * size));
		return blankShiftLocation;
	}
	
	private static final float characterScaleFactor = 1.5f;
	public static float getCharacterScaleFactor() { return characterScaleFactor; }
	
	public static Dimension GetCharacterSize() {
		int width = Math.round(MapLocationPanel.GetAdjustedTileSize() * characterScaleFactor);
		int height = (int)(width * (36f / 26f));
		return new Dimension(width, height);
	}
	
	private Point2D tileOffsetNorm = new Point2D.Float(-0.22f, 0.75f);
	
	private Point GetCharacterPlacementLocation(Point tileCornerLocation, int characterImageHeight) {
		int tileSize = MapLocationPanel.GetAdjustedTileSize();
		return new Point(tileCornerLocation.x + Math.round(tileSize * (float)tileOffsetNorm.getX()), tileCornerLocation.y - characterImageHeight + Math.round(tileSize * (float)tileOffsetNorm.getY()));
	}
	
	private void SetCharacterPlacement() {
		System.out.println("SetCharacterAtSlot: " + selectionLocation.toString());
		
		System.err.println("BattlePanel.SetCharacterPlacement() - Remove existing character if one was already placed here");
	
		
		CharacterData selectedCharacter = selectionChoices.get(selectionIndex);
		board.GetTileAt(selectionLocation).SetOccupant( Game.Instance().CreateCharacter_Ally(selectedCharacter, selectionLocation) );
	
		board.CalcAllMoves();
	
		ItemData[] manditoryGear = GUIManager.MapLocationPanel().getManditoryEquipmentForBattle();
		if(manditoryGear != null && manditoryGearIdCharIdAssignments.size() == 0) {
			System.out.println("BattlePanel.SetCharacterPlacement() - Applying all ManditoryGear to placed char: " + selectedCharacter.getName());
			ForceGearEquip(selectedCharacter, manditoryGear);
		}
		
		//Conclude selection UI
		ToggleCharacterSelection(false, null);
	}
	
	private void ClearCharacter() {
		int slotIndex = GetAllySlotIndex();
		
		Game.Instance().RemoveCharacter_Ally(characterSockets.get(slotIndex).ID);
		
		TryReverseForcedEquip(board.GetTileAt(selectionLocation).Occupant().GetData());
		
		characterSockets.get(slotIndex).ID = null;
		characterSockets.get(slotIndex).characterPanel.setVisible(false);
		board.GetTileAt(selectionLocation).SetOccupant(null);
		
		//Conclude selection UI
		isSelectionMaxedOut = false;
		//startBattleButton.setVisible(false); //Should already be hidden when selecting character
		ToggleCharacterSelection(false, null);
	}
	
	//Character Placement - End
	
	//Battle Start Anim - Start
	
	private CustomButtonUltra startBattleButton;
	
	private void StartOpeningRibbonAnim() {
		System.out.println("Battle Start!");
		
		startBattleButton.setVisible(false);
		
		//Remove the unused ImagePanels
		for(CharacterSocket charSocket : characterSockets) {
			if(charSocket.ID == null)
				battlePane.remove(charSocket.comboPanel);
		}
		//Remove the unused sockets from the list
		characterSockets.removeIf(x -> x.ID == null);
		
		board.ResetHighlights();
		AnimateBattleMessageRibbon("Start Battle!", false, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				TransitionFromPlacementToBattlePhase();
			}
		});
	}
	
	//Battle Start Anim - End
	
	//Battle Message Ribbon - Start
	
	private JPanel battleMessageRibbon;
	private JFxLabel battleMessageLabel;
	private Timer startBattleTimer;
	private int battleMessageRibbonY = -GUIUtil.GetRelativePoint(0f,  0.12f).y;
	private boolean hasRibbonReachedCenter;
	private int shortPauseTime = 1400;
	private int longPauseTime = 5000;
	
	public void AnimateBattleMessageRibbon(String message, boolean useLongPause, ActionListener callback) {
		battleMessageLabel.setText(message);
		hasRibbonReachedCenter = false;
		battleMessageRibbonY = -GUIUtil.GetRelativePoint(0f,  0.12f).y;
		battleMessageRibbon.setBounds(battleMessageRibbon.getLocation().x, battleMessageRibbonY, battleMessageRibbon.getSize().width, battleMessageRibbon.getSize().height);
		final int yVel = 80;
		final int stopPos = GUIUtil.GetRelativePoint(0f,  0.3f).y - (battleMessageRibbon.getSize().height / 2);
		final int delay = 10;
		final int pauseDelay = useLongPause ? longPauseTime : shortPauseTime;
		
		startBattleTimer = new Timer(delay, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(battleMessageRibbonY >= stopPos && !hasRibbonReachedCenter) {
					hasRibbonReachedCenter = true;
					startBattleTimer.setDelay(pauseDelay);
				} else if(battleMessageRibbonY > stopPos && startBattleTimer.getDelay() > delay) {
					startBattleTimer.setDelay(delay);
				} else if(battleMessageRibbonY > GUIManager.GetWindowSize().height) {
					startBattleTimer.stop();
					if(callback != null)
						callback.actionPerformed(arg0);
					return;
				}
				//System.out.println("startBattleTimer tick");
				
				battleMessageRibbonY += yVel;
				battleMessageRibbon.setBounds(battleMessageRibbon.getLocation().x, battleMessageRibbonY, battleMessageRibbon.getSize().width, battleMessageRibbon.getSize().height);
			}
		});
		startBattleTimer.setInitialDelay(0);
		startBattleTimer.start();
	}
	
	//Battle Message Ribbon - End
	
	//[MISSION_FLOW_EDIT]
	/*public void RestoreBattleState(BattleState battleState) {
		System.err.println("BattlePanel.RestoreBattleState() - STUB");

	}*/
	//Since everything for the Battle is setup from Game we're just going to send the battleState there instead of preloading it here
	
	private void TransitionFromPlacementToBattlePhase() {
		Game.Instance().CompletePlacementPhase();
		StartBattlePhase();
	}
	
	/**
	 * Called internally either by the TransitionFromPlacementToBattlePhase method or at the end of the Initialize method when reloading a BattleState.
	 */
	public void StartBattlePhase() {
		//Migrate all the characterSockets from the battlePane to the scenePane because the battle characters need to participate in the row layering too.
		for(CharacterSocket charSocket : this.characterSockets) {
			battlePane.remove(charSocket.comboPanel);
			GUIManager.MapLocationPanel().MigrateBattleCharacterToScenePane(charSocket, charSocket.comboPanel.getLocation());
		}
		GUIManager.MapLocationPanel().DebugScenePaneLayers();
		
		battlefieldFeedbackTimer.setRepeats(true);
		battlefieldFeedbackTimer.setInitialDelay(0);
		battlefieldFeedbackTimer.start();
	}
	
	//[MISSION_FLOW_EDIT]
	public BattleState CollectBattleState() {
		BattleState battleState = new BattleState();
		battleState.isPlacementPhase = Game.Instance().IsPlacementPhase();
		battleState.turnCount = Game.Instance().getTurnCount();
		battleState.turnPhases = Game.Instance().getTurnPhases();
		battleState.turnOrderIndex = Game.Instance().getTurnOrderIndex();
		battleState.remainingLingeringAnimCombatEffects = Game.Instance().GetLingeringAnimCombatEffects();
		battleState.battleData = Game.Instance().GetBattleData();
		if(battleState.battleData == null)
			System.err.println("BattlePanel.CollectBattleState() - BattleData is null! This will bug the Restored Battle Logic.");
		Map<Integer,CharacterBaseData> charDataList_enemy = new HashMap<Integer,CharacterBaseData>();
		Map<Integer,CharacterBaseData> charDataList_ally = new HashMap<Integer,CharacterBaseData>();
		Map<Integer,CharacterBaseData> charDataList_npcAlly = new HashMap<Integer,CharacterBaseData>();
		for(int i = 0; i < Game.Instance().GetTurnOrderedCharBases().size(); i++) {
			CharacterBase charBase = Game.Instance().GetTurnOrderedCharBases().get(i);
			
			if(Game.Instance().GetEnemyCharacterList().contains(charBase))
				charDataList_enemy.put(i, new CharacterBaseData(charBase));
			else if(Game.Instance().GetAllyCharacterList().contains(charBase))
				charDataList_ally.put(i, new CharacterBaseData(charBase));
			else if(Game.Instance().GetNpcAllyCharacterList().contains(charBase))
				charDataList_npcAlly.put(i, new CharacterBaseData(charBase));
			else
				System.err.println("BattlePanel.CollectBattleState() - Couldn't find any CharacterList that holds this charBase: " + charBase.GetData().getName());
		}
		if(charDataList_enemy.size() == 0)
			System.err.println("BattlePanel.CollectBattleState() - There were no enemies characters found in this battle?! How can that be?");
		if(charDataList_ally.size() == 0)
			System.err.println("BattlePanel.CollectBattleState() - There were no ally characters found in this battle?! How can that be?");
		battleState.enemyBaseDataMap = charDataList_enemy;
		battleState.allyBaseDataMap = charDataList_ally;
		battleState.npcAllyBaseDataMap = charDataList_npcAlly;
		return battleState;
	}
	
	//Actions - Start
	
	//Move Action - Start
	
	private Timer moveTimer;
	private int moveVel = 3;
	private Point nextLoc;
	private Point targetLoc;
	private Point targetPoint;
	private int xDir;
	private int yDir;
	private int targetIndex;
	
	public void MoveAlongPath(List<Tile> path) {
		System.out.println("BattlePanel.MoveAlongPath()");
		
		for(Tile tile : path) {
			System.out.println("  -path tile.loc: " + tile.Location());
		}
		
		HidePanels();
		cancelButton.setVisible(false);
		
		CharacterBase characterBase = Game.Instance().GetActiveBattleCharacter();
		CharacterSocket socket = characterSockets.stream().filter(x -> x.ID == characterBase.GetData().getId()).findFirst().get();
		
		int tileSize = MapLocationPanel.GetAdjustedTileSize();
		
		targetIndex = 0;
		int frameDuration_ticks = 2;
		
		Point characterPanelLoc = socket.getSimulatedLocation();
		
		Point locationOnGrid = new Point(MapLocationPanel.GetSceneLoc().x + (characterBase.getLocation().x * tileSize), MapLocationPanel.GetSceneLoc().y + (characterBase.getLocation().y * tileSize));
		Point gridCornerOffset = new Point(characterPanelLoc.x - locationOnGrid.x, characterPanelLoc.y - locationOnGrid.y);
		System.out.println("gridCornerOffset: " + gridCornerOffset);
		
		nextLoc = new Point(characterPanelLoc);
		//targetLoc = new Point(path.get(targetIndex).Location().x * tileSize, path.get(targetIndex).Location().y * tileSize);
		targetLoc = new Point(MapLocationPanel.GetSceneLoc().x + (path.get(targetIndex).Location().x * tileSize) + gridCornerOffset.x,
							  MapLocationPanel.GetSceneLoc().y + (path.get(targetIndex).Location().y * tileSize) + gridCornerOffset.y);
		targetPoint = path.get(targetIndex).Location();
		
		xDir = Math.max(-1, Math.min(1, path.get(targetIndex).Location().x - characterBase.getLocation().x));
		yDir = Math.max(-1, Math.min(1, path.get(targetIndex).Location().y - characterBase.getLocation().y));
		
		SpriteSheet walkSheet = SpriteSheetUtility.GetWalkSheet(characterBase.GetData().getType());
		int animFrameCount = 4;
		int[] animFrames = new int[animFrameCount];
		int directionMod = GetAnimIndexFromDirection(new Point(xDir, -yDir));
		animFrames[0] = directionMod - 1;
		animFrames[1] = directionMod;
		animFrames[2] = directionMod + 1;
		animFrames[3] = directionMod;
		socket.characterPanel.SetNewImage(walkSheet.GetSprite(animFrames[0], 1, 1));
		
		
		moveTimer = new Timer(4, new ActionListener() {
			int animFrameTick;
			//This should start on the second element because we've already set our character's first frame at the start of the move
			int animIndex = 1;
			//This is a grid position, not a pixel position
			Point lastPoint = characterBase.getLocation();
			
			boolean hasChangedLayers = false;
			Direction direction = GetDirectionEnumFromDirection(new Point(xDir, -yDir));
			
			//Used for detecting erroneous grid locations, this value assumes there won't be any scenes created taller than this
			int ordinarySceneHeightMax = 50;
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//if(targetIndex < path.size())
				//	moveTimer.setDelay(delay);
				
				if(nextLoc.distance(targetLoc) <= moveVel * 2) {
					//Re-adjust the character's layer when they complete their upward movement to place them behind vertical elements of the lower layers.  
					if(direction == Direction.Up) {
						Point offsetLastPoint = new Point(lastPoint.x, lastPoint.y + 2);
						Point offsetNewPoint = new Point(targetPoint.x, targetPoint.y + 2);
						
						if(offsetNewPoint.y > ordinarySceneHeightMax)
							System.err.println("Path Location Anomaly Detected - Move Timer Part 1 - Location exceeds ordinary scene grid size of "+ordinarySceneHeightMax+" units! ... path location: " + targetPoint + ", targetIndex: " + targetIndex);
						
						System.out.println("Tile-Landing Relayering");
						SetPaneLayer(characterBase, offsetLastPoint, offsetNewPoint);
					}
					
					GUIManager.MapLocationPanel().HandleBreakawayForCharacterMove(lastPoint, path.get(targetIndex).Location());
					lastPoint = path.get(targetIndex).Location();
					
					nextLoc = targetLoc;
					targetIndex++;
					if(targetIndex < path.size()) {
						//targetLoc = new Point(path.get(targetIndex).Location().x * tileSize, path.get(targetIndex).Location().y * tileSize);
						targetLoc = new Point(MapLocationPanel.GetSceneLoc().x + (path.get(targetIndex).Location().x * tileSize) + gridCornerOffset.x,
											  MapLocationPanel.GetSceneLoc().y + (path.get(targetIndex).Location().y * tileSize) + gridCornerOffset.y);
						targetPoint = path.get(targetIndex).Location();
						
						xDir = Math.max(-1, Math.min(1, path.get(targetIndex).Location().x - path.get(targetIndex-1).Location().x));
						yDir = Math.max(-1, Math.min(1, path.get(targetIndex).Location().y - path.get(targetIndex-1).Location().y));
						
						int directionMod = GetAnimIndexFromDirection(new Point(xDir, -yDir));
						animFrames[0] = directionMod - 1;
						animFrames[1] = directionMod;
						animFrames[2] = directionMod + 1;
						animFrames[3] = directionMod;
						//animIndex = 0;
						//this shouldn't reset, it'll cause a stutterstep in the walk anim everytime a new tile is reached (unless its aligned perfectly with the distance and speed, doubtful)
						
						hasChangedLayers = false;
						direction = GetDirectionEnumFromDirection(new Point(xDir, -yDir));
					}
				} else {
					//This early relayering prevents the downward moving character from being overlapped by the row their walking down into and prevents the character walking upward from passing vertical
					//objects too late. A nearly identical mechanism is being used during dialographies for actor movements.
					if(!hasChangedLayers && (direction == Direction.Down || direction == Direction.Up) && nextLoc.distance(targetLoc) >= Board.ScaledTileSize() / 2) {
						hasChangedLayers = true;
						
						//For some reason characters need even more offset when moving up. This fixed the issue and it hasn't caused any negative effects that I know of
						Point offsetLastPoint = new Point(lastPoint.x, lastPoint.y + (direction == Direction.Down ? 2 : 3));
						
						//For some reason characters need even more offset when moving up. This fixed the issue and it hasn't caused any negative effects that I know of
						Point offsetNewPoint = new Point(targetPoint.x, targetPoint.y + (direction == Direction.Down ? 2 : 3));
						
						if(offsetNewPoint.y > ordinarySceneHeightMax)
							System.err.println("Path Location Anomaly Detected - Move Timer Part 2 - Location exceeds ordinary scene grid size of "+ordinarySceneHeightMax+" units! ... path location: " + offsetNewPoint);
	                	
						System.out.println("Tile-Takeoff Relayering");
						SetPaneLayer(characterBase, offsetLastPoint, offsetNewPoint);
					}
					
					nextLoc.translate(moveVel * xDir, moveVel * yDir);
				}
				socket.SetAllBounds(nextLoc, GetCharacterSize());
				
				//Animate
				if(animFrameTick >= frameDuration_ticks) {
					animFrameTick = 0;
					socket.characterPanel.SetNewImage(walkSheet.GetSprite(animFrames[animIndex], 1, 1));
					animIndex++;
					if(animIndex >= animFrameCount)
						animIndex = 0;
				} else
					animFrameTick++;
				
				//System.out.println("BattlePanel.MoveAlongPath() - nextLoc: " + nextLoc + ", targetLoc: " + targetLoc);
				
				if(targetIndex >= path.size()) {
					socket.characterPanel.SetNewImage(walkSheet.GetSprite(animFrames[1], 1, 1));
					EndMovement(path.get(path.size() - 1));
					moveTimer.stop();
					return;
				}
			}
		});
		moveTimer.start();
	}
	//DEBUGGING - 6/1/22 - Erroneous lasLoc and loc arguments are entering here, address this from the call sources
	private void SetPaneLayer(CharacterBase charBase, Point lastLoc, Point loc) {
		//Integer row = loc.y + 1;
		//for some reason adding two to the characters row makes then render properly, still not sure why that is...
		Integer row = loc.y + 2;
		//DEBUGGING - 6/1/22 - Dont know what else to try. Please look upon my codes with favor, Machine God
		//Integer row = loc.y;
		
		CharacterSocket socket = characterSockets.stream().filter(x -> x.ID == charBase.GetData().getId()).findFirst().get();
		
		//battlePane.setLayer(socket.comboPanel, row, 0);
		//The characters are part of the scenePane now, so call the appropriate method for that
		int oldRow = lastLoc.y + 2;
		//DEBUGGING - 6/1/22 - Dont know what else to try. Please look upon my codes with favor, Machine God
		//int oldRow = lastLoc.y;
		
		System.out.println("BattlePanel.SetPaneLayer() - " + charBase.GetData().getName() + " is moving layers from oldRow: "+ oldRow +" to row: " + row);
		
		GUIManager.MapLocationPanel().UpdateActorRow(socket.comboPanel, oldRow, row, true, charBase.GetData().getName());
	}
	
	private void EndMovement(Tile destination) {
		System.out.println("BattlePanel.EndMovement()");
		
		CharacterBase charBase = Game.Instance().GetActiveBattleCharacter();
		//CLear previous tile
		board.GetTileAt(charBase.getLocation()).SetOccupant(null);
		//Setup next tile
		Tile tile = board.GetTileAt(destination.Location());
		tile.SetOccupant(charBase);
		tile.ToggleTurnTaker(true);
		
		//board.ResetHighlights();
		//This method is really intensive now that the battle dimensions are much larger
		board.ResetDirtyHighlights();
		if(Game.Instance().GetAllyCharacterList().contains(charBase)) {
			ShowActionPanel(charBase);
			ToggleIgnoreMouseEvents(false);
		}
		
		Game.Instance().SetCharacterMovement(destination.Location());
	}
	
	//Move Action - End
	
	//Attack Action - Start
	
	CombatAnimPane combatAnimPane;
	
	//These variables get set when they're used in the GUI logic and they get cleared by menu navigation and/or at the end of the turn.
	private Ability userChosenAbility;
	public Ability getUserChosenAbility() { return userChosenAbility; }
	private ItemData userChosenItem;
	public ItemData getUserChosenItem() { return userChosenItem; }
	
	//Called by Game.AIMoveDoneCallback()
	public void PlayCombatAnim(CharacterBase attacker, CharacterBase defender, boolean didAttackHit, HealthModInfo healthModInfo, Ability abilityToUse, ItemData itemToUse,
			boolean isRightCharacterTheAttacker, boolean isLeftCharacterHidden, boolean isRightCharacterHidden, boolean isLingeringAnimOrAutoRevive, boolean isSingleSelfTarget
	) {
		cancelButton.setVisible(false);
		
		ToggleTargetInfoPanel(false, null);
		HideCharacterCard();
		ToggleTerrainPanel(false, null, null);
		
		//Pause this during combat anim
		battlefieldFeedbackTimer.stop();
		
		combatAnimPane.StartCombatAnim(attacker, board.GetTileAt(attacker.getLocation()).TerrainType(), defender, board.GetTileAt(defender.getLocation()).TerrainType(), didAttackHit,
				abilityToUse, itemToUse, healthModInfo, isRightCharacterTheAttacker, isLeftCharacterHidden, isRightCharacterHidden, isLingeringAnimOrAutoRevive, isSingleSelfTarget);
		//Then let the CombnatAnimPane handle the anim in stages:
		//setup the character image panels
		//run the combat overlay intro anim
		//animate the characters
		//run the combat overlay outro anim
	}
	
	public void CombatAnimDone() {
		//Unpause this now that combat anim is done
		battlefieldFeedbackTimer.restart();
		
		CharacterBase activeChar = Game.Instance().GetActiveBattleCharacter();
		
		//We may have killed ourselves with area damage of our action
		if(activeChar.GetHp() == 0) {
			Game.Instance().EndTurn();
		} else {
			if(Game.Instance().GetAllyCharacterList().contains(activeChar)) {
				ShowActionPanel(activeChar);
				ToggleIgnoreMouseEvents(false);
			} else {
				Game.Instance().AIAttackDoneCallback();
			}
		}
	}
	
	//Attack Action - End
	
	//End Turn Action - Start
	
	//Called from Game.SetCharacterMovement() if the active character is an ally
	public void ChooseDirection() {
		board.ShowDirections(Game.Instance().GetActiveBattleCharacter().getLocation());
	}
	
	/**
	 * Get an idex to be used for the character's Walk sheet.
	 * @param direction
	 * @return index == 1 -> down, index == 4 -> left, index == 7 -> right, index == 10 -> up
	 */
	public int GetAnimIndexFromDirection(Point direction) {
		//System.out.println("BattlePanel.GetAnimIndexFromDirection() - direction: " + direction);
		
		int index = 1;
		if(direction.x == 0 && direction.y == -1)
			index = 1;
		else if(direction.x == -1 && direction.y == 0)
			index = 4;
		else if(direction.x == 1 && direction.y == 0)
			index = 7;
		else if(direction.x == 0 && direction.y == 1)
			index = 10;
		return index;
	}
	
	public Direction GetDirectionEnumFromDirection(Point direction) {
		//System.out.println("BattlePanel.GetDirectionEnumFromDirection() - direction: " + direction);
		
		Direction dir = null;
		if(direction.x == 0 && direction.y == -1)
			dir = Direction.Down;
		else if(direction.x == -1 && direction.y == 0)
			dir = Direction.Left;
		else if(direction.x == 1 && direction.y == 0)
			dir = Direction.Right;
		else if(direction.x == 0 && direction.y == 1)
			dir = Direction.Up;
		return dir;
	}
	
	public void UpdateDirection(Point direction) {
		CharacterBase selectedCharacter = Game.Instance().GetActiveBattleCharacter();
		CharacterSocket socket = characterSockets.stream().filter(x -> x.ID == selectedCharacter.GetData().getId()).findFirst().get();
		SpriteSheet walkSheet = SpriteSheetUtility.GetWalkSheet(selectedCharacter.GetData().getType());
		socket.characterPanel.SetNewImage(walkSheet.GetSprite(GetAnimIndexFromDirection(direction), 1, 1));
		
		//System.out.println("BattlePanel.UpdateDirection()");
		
		selectedCharacter.SetDirection(direction);
	}
	
	/**
	 * This gets called by the Game at the end of each turn.
	 */
	public void EndTurnCleanup() {
		userChosenAbility = null;
		userChosenItem = null;
	}
	
	//used only by the ally turn logic
	public void CompleteDirectionSelection() {
		cancelButton.setVisible(false);
		
		board.ResetDirtyHighlights();
		
		board.CalcAllMoves();
		
		currentTurnActionState = CharacterTurnActionState.ActionMenu;
		
		Game.Instance().EndTurn();
	}
	
	//End Turn Action - End
	
	//Multipurpose Action Functionality - Start
	
	public CustomButtonUltra confirmButton;
	public CustomButtonUltra cancelButton;
	
	private void CancelAction() {
		switch(currentTurnActionState) {
			case MoveSelection:
				board.ClearTurnTakersMoveTiles();
				board.ResetDirtyHighlights();
				cancelButton.setVisible(false);
				ShowActionPanel(Game.Instance().GetActiveBattleCharacter());
				currentTurnActionState = CharacterTurnActionState.ActionMenu;
				break;
			case AttackSelection:
				board.ClearTargetTiles();
				board.ResetDirtyHighlights();
				cancelButton.setVisible(false);
				ShowActionPanel(Game.Instance().GetActiveBattleCharacter());
				currentTurnActionState = CharacterTurnActionState.ActionMenu;
				break;
			case AbilityMenu:
				board.ClearTargetTiles();
				board.ResetDirtyHighlights();
				abilityPanel.setVisible(false);
				abilityBG.setVisible(false);
				confirmButton.setVisible(false);
				cancelButton.setVisible(false);
				abilityCard.setVisible(false);
				ShowActionPanel(Game.Instance().GetActiveBattleCharacter());
				currentTurnActionState = CharacterTurnActionState.ActionMenu;
				userChosenAbility = null;
				break;
			case AbilitySelection:
				board.ResetDirtyHighlights();
				abilityPanel.setVisible(true);
				abilityBG.setVisible(true);
				confirmButton.setVisible(true);
				cancelButton.setVisible(true);
				currentTurnActionState = CharacterTurnActionState.AbilityMenu;
				break;
			case ItemMenu:
				board.ClearTargetTiles();
				board.ResetDirtyHighlights();
				itemPanel.setVisible(false);
				itemBG.setVisible(false);
				confirmButton.setVisible(false);
				cancelButton.setVisible(false);
				itemCard.setVisible(false);
				ShowActionPanel(Game.Instance().GetActiveBattleCharacter());
				currentTurnActionState = CharacterTurnActionState.ActionMenu;
				userChosenItem = null;
				break;
			case ItemSelection:
				board.ResetDirtyHighlights();
				itemPanel.setVisible(true);
				itemBG.setVisible(true);
				confirmButton.setVisible(true);
				cancelButton.setVisible(true);
				currentTurnActionState = CharacterTurnActionState.ItemMenu;
				break;
			case WaitSelection:
				board.ResetDirtyHighlights();
				cancelButton.setVisible(false);
				ShowActionPanel(Game.Instance().GetActiveBattleCharacter());
				currentTurnActionState = CharacterTurnActionState.ActionMenu;
				break;
			default:
				System.err.println("BattlePanel.CancelAction() - Add support for: " + currentTurnActionState);
				break;
		}
		
		ToggleIgnoreMouseEvents(false);
	}
	
	//Multipurpose Action Functionality - End
	
	//Actions - End
	
	//Character States - Start
	
	public void SetDeadState(CharacterBase charBase, boolean isDead) {
		System.out.println("BattlePanel.SetDeadState()");
		
		CharacterSocket socket = characterSockets.stream().filter(x -> x.ID == charBase.GetData().getId()).findFirst().get();
		socket.SetDeadState(charBase, isDead);
	}
	
	public boolean GetDeadState(CharacterBase charBase) {
		CharacterSocket socket = characterSockets.stream().filter(x -> x.ID == charBase.GetData().getId()).findFirst().get();
		return socket.isInDeadState;
	}
	
	//Character States - End
	
	public void EndBattle() {
		battlefieldFeedbackTimer.stop();
		//There may be over cleanup stuff eventually
	}
	
	//The MapLocationPanel class calls this from StartDialography() to purge its scenePane of battle-related UI elements without needing to remember what they are.
	public void RemoveBattleElementsForDialography() {
		//Remove the character panels from the scenePane. This is likely a more effecient way to hide them.
		for(CharacterSocket charSocket : this.characterSockets) {
			GUIManager.MapLocationPanel().RemoveBattleCharacterFromScenePane(charSocket);
		}
	}
}
