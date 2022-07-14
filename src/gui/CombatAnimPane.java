package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLayeredPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import data.AnimSocketsPack;
import data.AttributeMod;
import data.BattleItemTraits;
import data.HealthModInfo;
import data.ItemData;
import dataShared.ActorData;
import enums.AttributeModType;
import enums.BattleItemType;
import enums.ClassType;
import enums.ColorBlend;
import enums.EquipmentType;
import enums.ItemType;
import enums.StatusType;
import enums.WeaponType;
import gameLogic.AbilityManager;
import gameLogic.AbilityManager.Ability;
import gameLogic.Board.TerrainType;
import gameLogic.CharacterBase;
import gameLogic.Game;
import gameLogic.ItemDepot;
import gameLogic.Items;
import gui.SpriteSheetUtility.AnimSubset;
import gui.SpriteSheetUtility.AnimType;
import gui.BattleCharacterController;

@SuppressWarnings("serial")
public class CombatAnimPane extends JLayeredPane implements ITransitionListener {
	BattlePanel battlePanel;
	
	//BG
	FadeTransitionPanel lightbox;
	
	//Terrain layers
	int startFadeEdge_left;
	int startFadeEdge_right;
	
	//Characetr/Tile Carriages
	JLayeredPane carriage_left;
	ImagePanel terrainTile_left;
	BattleCharacterController characterController_left;
	Point characterStartPos_left;
	BattleEffectController[] particleFXs_left;
	JFxLabel feedbackLabel_left;
	ImagePanel statusPanel_left;
	
	JLayeredPane carriage_right;
	ImagePanel terrainTile_right;
	BattleCharacterController characterController_right;
	Point characterStartPos_right;
	BattleEffectController[] particleFXs_right;
	JFxLabel feedbackLabel_right;
	ImagePanel statusPanel_right;
	
	//Each battle character can have only 3 particles effects at a time(during a single clip)
	private final int maxParticleCount = 3;

	//FG
	ImagePanel fadeOutOverlay_left;
	ImagePanel fadeOutOverlay_right;
	
	
	int timerDelay = 8;
	int alphaInterval = 8;
	int fadeOutEdgeSpeed = 16;
	int terrainSpeed = 32;
	int combatInterval = 15;
	Timer enterAnim;
	Timer combatTimer;
	Timer exitAnim;
	boolean isAnimating;
	
	Dimension dimension;
	Dimension carriageDimension;
	int startCarriageHeight;
	int targetCarriageHeight;
	int fadeOutWidth;
	Dimension characterSize;
	
	Dimension terrainSize;
	Point terrainCarriageOffset;
	
	boolean isFadingIn;
	CharacterBase attacker;
	CharacterBase defender;
	boolean didAttackHit;
	
	boolean hasFadeFinished;
	boolean hasExitAnimFinished;
	
	int carriageCenterOffset;
	Point carriagePosition_left;
	Point carriagePosition_right;
	
	/**
	 * Use this data structure to associate an EffectClip instance and its startLocation.
	 * @author Magnus
	 *
	 */
	public class EffectNode {
		public EffectNode(EffectClip effectClip, Point startLocation) {
			this.effectClip = effectClip;
			this.startLocation = startLocation;
		}
		private EffectClip effectClip;
		private Point startLocation;
		public EffectClip getEffectClip() { return effectClip; }
		public Point getStartLocation() { return startLocation; }
	}
	
	List<EffectNode> effectNodeList_left = new ArrayList<EffectNode>();
	List<EffectNode> effectNodeList_right = new ArrayList<EffectNode>();
	
	
	public class FeedbackMessage {
		public FeedbackMessage(String message, JFxColorScheme colorScheme) {
			this.message = message;
			this.colorScheme = colorScheme;
		}
		public FeedbackMessage(StatusType statusType) {
			this.statusType = statusType;
		}
		public String message;
		public StatusType statusType;
		
		//Styling
		public JFxColorScheme colorScheme;
	}
	
	private final int floatUpAnimIntervals = 5;
	private final int lingerIntervals = 20;
	private final JFxColorScheme positiveScheme = new JFxColorScheme(Color.WHITE, Color.BLUE.brighter(), Color.BLUE.darker().darker());
	private final JFxColorScheme negativeScheme = new JFxColorScheme(Color.WHITE, Color.RED.brighter(), Color.RED.darker().darker());
	private final JFxColorScheme dodgeScheme =  new JFxColorScheme(Color.WHITE, new Color(0.5f, 0f, 0.5f).brighter(), new Color(0.5f, 0f, 0.5f).darker().darker());
	
	//attacker/defender mapping
	boolean isRightCharacterTheAttacker;
	boolean isRightCharacterHidden;
	boolean isLeftCharacterHidden;
	//Generic attacker/defender adapters
	boolean isAttackersFeedbackAnimPlaying;
	boolean isDefendersFeedbackAnimPlaying;
	
	List<FeedbackMessage> feedbackQueue_left = new ArrayList<FeedbackMessage>();
	//boolean isFeedbackAnimPlaying_left;
	Timer feedbackLabelTimer_left;
	Timer statusPanelTimer_left;
	int feedbackIndex_left = 0;
	BufferedImage[] statusAnim_left;
	
	List<FeedbackMessage> feedbackQueue_right = new ArrayList<FeedbackMessage>();
	//boolean isFeedbackAnimPlaying_right;
	Timer feedbackLabelTimer_right;
	Timer statusPanelTimer_right;
	int feedbackIndex_right = 0;
	BufferedImage[] statusAnim_right;
	
	private void HandleFeedbackQueue(boolean handleDefendersFeedback, boolean didAttackHit, HealthModInfo info) {
		
		System.out.println("CombatAnimPane.HandleFeedbackQueue()");
		
		List<FeedbackMessage> feedbackQueue = new ArrayList<FeedbackMessage>();
		if(didAttackHit) {
			if(info == null)
				System.err.println("CombatAnimPane Constructor - healthModInfo == null! It should never be null.");
			
			if(info.isRevive)
				feedbackQueue.add(new FeedbackMessage("Revive", positiveScheme));
			
			//Queue the damage number
			if(info.amount != 0) {
				//This could be damage, healing or revive healing
				if(info.isHealing)
					feedbackQueue.add(new FeedbackMessage("" + info.amount, positiveScheme));
				else
					feedbackQueue.add(new FeedbackMessage("" + info.amount, negativeScheme));
			}
			
			//Queue a single message if there are cures
			if(info.appliedCures != null && info.appliedCures.size() > 0)
				feedbackQueue.add(new FeedbackMessage("Cure", positiveScheme));
			
			//Queue the Statuses
			if(info.appliedStatuses != null && info.appliedStatuses.size() > 0) {
				feedbackQueue.add(new FeedbackMessage("Status", negativeScheme));
				for(StatusType statusType : info.appliedStatuses)
					feedbackQueue.add(new FeedbackMessage(statusType));
			}
			
			//Queue the Buffs
			if(info.buffs != null && info.buffs.size() > 0)
				for(AttributeMod attriMod : info.buffs)
					feedbackQueue.add(new FeedbackMessage(AttributeModType.GetDisplayNameWithValue(true, attriMod), positiveScheme));
			
			//Queue the Debuffs
			if(info.debuffs != null && info.debuffs.size() > 0)
				for(AttributeMod attriMod : info.debuffs)
					feedbackQueue.add(new FeedbackMessage(AttributeModType.GetDisplayNameWithValue(false, attriMod), negativeScheme));
			
			if(feedbackQueue.size() == 0)
				feedbackQueue.add(new FeedbackMessage("[UNKNOWN]", negativeScheme));
		} else
			feedbackQueue.add(new FeedbackMessage("Dodge", dodgeScheme));
		
		if(handleDefendersFeedback && !isRightCharacterTheAttacker) {
			feedbackQueue_right.clear();
			feedbackQueue_right.addAll(feedbackQueue);
			feedbackIndex_right = 0;
		} else {
			feedbackQueue_left.clear();
			feedbackQueue_left.addAll(feedbackQueue);
			feedbackIndex_left = 0;
		}
		
		PlayNextQueuedFeedback(handleDefendersFeedback);
	}
	
	private void PlayNextQueuedFeedback(boolean updateDefendersFeedbackLabel) {
		List<FeedbackMessage> feedbackQueue = null;
		JFxLabel feedbackLabel = null;
		Timer messageTimer = null;
		Timer statusTimer = null;
		int index = 0;
		if(updateDefendersFeedbackLabel)
			isDefendersFeedbackAnimPlaying = true;
		else
			isAttackersFeedbackAnimPlaying = true;

		if(updateDefendersFeedbackLabel && !isRightCharacterTheAttacker) {
			feedbackQueue = feedbackQueue_right;
			feedbackLabel = feedbackLabel_right;
			messageTimer = feedbackLabelTimer_right;
			statusTimer = statusPanelTimer_right;
			index = feedbackIndex_right;
		} else {
			feedbackQueue = feedbackQueue_left;
			feedbackLabel = feedbackLabel_left;
			messageTimer = feedbackLabelTimer_left;
			statusTimer = statusPanelTimer_left;
			index = feedbackIndex_left;
		}
		
		System.out.println("PlayNextQueuedFeedback() - updateDefendersFeedbackLabel: " + updateDefendersFeedbackLabel);
		
		FeedbackMessage feedbackMessage = feedbackQueue.get(index);
		if(feedbackMessage.statusType == null) {
			feedbackLabel.setText(feedbackMessage.message);
			feedbackLabel.SetColorScheme(feedbackMessage.colorScheme);
			messageTimer.restart();
		} else {
			ActorData effectActor = GetStatusActorData(feedbackMessage.statusType);	
			if(updateDefendersFeedbackLabel && !isRightCharacterTheAttacker)
				statusAnim_right = SpriteSheetUtility.GetRangedEffectSheetFrames(effectActor.javaSheetFilePath, effectActor.nonActorFrames_startIndex, effectActor.nonActorFrames_endIndex);
			else
				statusAnim_left = SpriteSheetUtility.GetRangedEffectSheetFrames(effectActor.javaSheetFilePath, effectActor.nonActorFrames_startIndex, effectActor.nonActorFrames_endIndex);
			
			statusTimer.restart();
		}
	}
	
	/**
	 * Use this to get the associated status effect anims by StatusType.
	 * @param statusType
	 * @return
	 */
	public ActorData GetStatusActorData(StatusType statusType) {
		ActorData effectActor = null;
		switch(statusType) {
			case Blind:
				effectActor = effect_status_1_2;
				break;
			case Cripple:
				effectActor = effect_status_2_3;
				break;
			case Silence:
				effectActor = effect_status_1_3;
				break;
			case Daze:
				effectActor = effect_status_1_5;
				break;
			case Charmed:
				effectActor = effect_status_2_1;
				break;
			case Fear:
				effectActor = effect_status_2_4;
				break;
			case Goad:
				effectActor = effect_status_1_4;
				break;
			case Accelerated:
				effectActor = effect_status_2_5;
				break;
			default:
				System.err.println("CombatAnimPane.PlayNextQueuedFeedback() - Add support for StatusType: " + statusType);
				break;
		}
		return effectActor;
	}
	
	private void OnFeedbackMessageDone(boolean isFromRightLabel) {
		boolean canPlayNext = false;
		if(isFromRightLabel) {
			feedbackIndex_right++;
			if(feedbackIndex_right < feedbackQueue_right.size())
				canPlayNext = true;
		} else {
			feedbackIndex_left++;
			if(feedbackIndex_left < feedbackQueue_left.size())
				canPlayNext = true;
		}
		boolean isDefendersMessageDone = isFromRightLabel && !isRightCharacterTheAttacker;
		if(canPlayNext)
			PlayNextQueuedFeedback(isDefendersMessageDone);
		else {
			if(isDefendersMessageDone)
				isDefendersFeedbackAnimPlaying = false;
			else
				isAttackersFeedbackAnimPlaying = false;
		}
		
		//If EndCombatAnim had been stalled while waiting for the feedback anims to finish then call it now
		if(isFadingIn && !isDefendersFeedbackAnimPlaying && !isAttackersFeedbackAnimPlaying)
			EndCombatAnim();
	}
	
	
	public CombatAnimPane(BattlePanel battlePanel, Dimension dimension) {
		this.battlePanel = battlePanel;
		
		CreateEffectData();
		
		this.dimension = dimension;
		setSize(dimension);
		setLocation(0,0);
		setOpaque(false);
		setBackground(new Color(0,0,0,0));
		int paneIndex = 0;
		
		//setup fade out overlay
		fadeOutWidth = Math.round(dimension.width / 3f);
		fadeOutOverlay_left = new ImagePanel("worldmap/TravelAnim_EdgeFadeOut_Small_Left.png");
		fadeOutOverlay_left.setOpaque(false);
		fadeOutOverlay_left.setBackground(new Color(0,0,0,0));
		fadeOutOverlay_left.setSize(new Dimension(fadeOutWidth, dimension.height));
		startFadeEdge_left = -fadeOutWidth * 25/32;
		fadeOutOverlay_left.setLocation(startFadeEdge_left, 0);
		add(fadeOutOverlay_left, paneIndex);
		paneIndex++;
		fadeOutOverlay_right = new ImagePanel("worldmap/TravelAnim_EdgeFadeOut_Small_Right.png");
		fadeOutOverlay_right.setOpaque(false);
		fadeOutOverlay_right.setBackground(new Color(0,0,0,0));
		fadeOutOverlay_right.setSize(new Dimension(dimension.width / 2, dimension.height));
		startFadeEdge_right = dimension.width - (dimension.width / 2 * 7/32);
		fadeOutOverlay_right.setLocation(startFadeEdge_right, 0);
		add(fadeOutOverlay_right, paneIndex);
		paneIndex++;
		
		
		
		//Interrelated sizing and positioning
		//Carriage
		float carriageNormWidth = 0.26f;
		carriageDimension = GUIUtil.GetRelativeSize(carriageNormWidth, true);
		carriageDimension.height = Math.round(carriageDimension.width * 1.1f);
		targetCarriageHeight = dimension.height / 2 - (carriageDimension.height / 2);
		startCarriageHeight = dimension.height + targetCarriageHeight;

		//Terrain Tiles
		float tileNormWidth = 0.14f;
		terrainSize = GUIUtil.GetRelativeSize(tileNormWidth, true);
		terrainSize.height = Math.round(terrainSize.height * (3f/2));
		
		//Carriages
		carriageCenterOffset = GUIUtil.GetRelativeSize((carriageNormWidth - tileNormWidth) / 2f, true).width;
		carriagePosition_left = new Point(dimension.width / 2 - carriageDimension.width + carriageCenterOffset, startCarriageHeight);
		carriagePosition_right = new Point(dimension.width / 2 - carriageCenterOffset, startCarriageHeight);
		
		//Setup carriage on left
		int carriageIndex_left = 0;
		carriage_left = new JLayeredPane();
		carriage_left.setOpaque(false);
		carriage_left.setBackground(new Color(0,0,0,0));
		carriage_left.setBounds(carriagePosition_left.x, carriagePosition_left.y, carriageDimension.width, carriageDimension.height);
		//carriage_left.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
		
		//Setup carriage on right
		int carriageIndex_right = 0;
		carriage_right = new JLayeredPane();
		carriage_right.setOpaque(false);
		carriage_right.setBackground(new Color(0,0,0,0));
		carriage_right.setBounds(carriagePosition_right.x, carriagePosition_right.y, carriageDimension.width, carriageDimension.height);
		//carriage_right.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
		
		
		characterSize = GUIUtil.GetRelativeSize(0.16f, true);
		characterStartPos_left = new Point((carriageDimension.width/2) - (characterSize.width/2), 0);
		characterStartPos_right = new Point((carriageDimension.width/2) - (characterSize.width/2), 0);
		
		
		//Dimension feedbackLabelSize = GUIUtil.GetRelativeSize(0.16f, 0.08f);
		//Point feedbackLabelPos = new Point((carriageDimension.width/2) - (feedbackLabelSize.width/2), 0);
		Dimension feedbackLabelSize = GUIUtil.GetRelativeSize(carriageNormWidth, 0.08f);
		Point feedbackLabelPos = new Point(0, 0);
		
		//Combat Feedback Left
		feedbackLabel_left = new JFxLabel("9999", SwingConstants.CENTER, GUIUtil.CombatFeedback, Color.WHITE).withShadow(Color.RED, new Point(2, 2)).withStroke(Color.RED.darker().darker(), 4, true);
		feedbackLabel_left.setBounds(feedbackLabelPos.x, feedbackLabelPos.y, feedbackLabelSize.width, feedbackLabelSize.height);
		carriage_left.add(feedbackLabel_left, carriageIndex_left);
		carriageIndex_left++;
		feedbackLabel_left.setVisible(false);
		//feedbackLabel_left.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
		
		//Combat Feedback Right
		feedbackLabel_right = new JFxLabel("9999", SwingConstants.CENTER, GUIUtil.CombatFeedback, Color.WHITE).withShadow(Color.RED, new Point(2, 2)).withStroke(Color.RED.darker().darker(), 4, true);
		feedbackLabel_right.setBounds(feedbackLabelPos.x, feedbackLabelPos.y, feedbackLabelSize.width, feedbackLabelSize.height);
		carriage_right.add(feedbackLabel_right, carriageIndex_right);
		carriageIndex_right++;
		feedbackLabel_right.setVisible(false);
		//feedbackLabel_right.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
		
		Dimension statusPanelSize = GUIUtil.GetRelativeSize(0.12f, true);
		Point statusPanelPos = new Point((carriageDimension.width/2) - (statusPanelSize.width/2), -Math.round(statusPanelSize.height/3f));
		
		statusPanel_left = new ImagePanel(GUIUtil.GetBuffedImage("Question.png"));
		statusPanel_left.setOpaque(false);
		statusPanel_left.setBackground(new Color(0,0,0,0));
		statusPanel_left.setBounds(statusPanelPos.x, statusPanelPos.y, statusPanelSize.width, statusPanelSize.height);
		carriage_left.add(statusPanel_left, carriageIndex_left);
		carriageIndex_left++;
		statusPanel_left.setVisible(false);
		//statusPanel_left.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
		
		statusPanel_right = new ImagePanel(GUIUtil.GetBuffedImage("Question.png"));
		statusPanel_right.setOpaque(false);
		statusPanel_right.setBackground(new Color(0,0,0,0));
		statusPanel_right.setBounds(statusPanelPos.x, statusPanelPos.y, statusPanelSize.width, statusPanelSize.height);
		carriage_right.add(statusPanel_right, carriageIndex_right);
		carriageIndex_right++;
		statusPanel_right.setVisible(false);
		//statusPanel_right.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
		
		feedbackLabelTimer_left = new Timer(50, new ActionListener() {
			final int totalTickCount = floatUpAnimIntervals + lingerIntervals;
			final int originalY = feedbackLabelPos.y;
			final int offsetYDistance =  GUIUtil.GetRelativeSize(0.035f, true).height;
			private int tickCount = totalTickCount;
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(tickCount > lingerIntervals) {
					//int offsetY = Math.round(offsetYDistance * ((float)tickCount / totalTickCount) );
					int offsetY = Math.round(offsetYDistance * ((float)(tickCount - lingerIntervals) / floatUpAnimIntervals) );
					feedbackLabel_left.setLocation(feedbackLabel_left.getLocation().x, originalY + offsetY);
				}
				if(tickCount <= 0) {
					tickCount = totalTickCount;
					feedbackLabel_left.setLocation(feedbackLabel_left.getLocation().x, originalY);
					feedbackLabelTimer_left.stop();
					feedbackLabel_left.setVisible(false);
					OnFeedbackMessageDone(false);
				} else {
					tickCount--;
					if(!feedbackLabel_left.isVisible())
						feedbackLabel_left.setVisible(true);
				}
			}
		});
		feedbackLabelTimer_left.setInitialDelay(0);
		feedbackLabelTimer_left.setRepeats(true);
		
		feedbackLabelTimer_right = new Timer(50, new ActionListener() {
			final int totalTickCount = floatUpAnimIntervals + lingerIntervals;
			final int originalY = feedbackLabelPos.y;
			final int offsetYDistance =  GUIUtil.GetRelativeSize(0.035f, true).height;
			private int tickCount = totalTickCount;
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(tickCount > lingerIntervals) {
					int offsetY = Math.round(offsetYDistance * ((float)(tickCount - lingerIntervals) / floatUpAnimIntervals) );
					feedbackLabel_right.setLocation(feedbackLabel_right.getLocation().x, originalY + offsetY);
				}
				if(tickCount <= 0) {
					tickCount = totalTickCount;
					feedbackLabel_right.setLocation(feedbackLabel_right.getLocation().x, originalY);
					feedbackLabelTimer_right.stop();
					feedbackLabel_right.setVisible(false);
					OnFeedbackMessageDone(true);
				} else {
					tickCount--;
					if(!feedbackLabel_right.isVisible())
						feedbackLabel_right.setVisible(true);
				}
			}
		});
		feedbackLabelTimer_right.setInitialDelay(0);
		feedbackLabelTimer_right.setRepeats(true);
		
		
		statusPanelTimer_left = new Timer(50, new ActionListener() {
			final int originalY = statusPanelPos.y;
			final int offsetYDistance =  GUIUtil.GetRelativeSize(0.035f, true).height;
			final int totalTickCount = floatUpAnimIntervals + lingerIntervals;
			private int tickCount = totalTickCount;
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(tickCount > lingerIntervals) {
					int offsetY = Math.round(offsetYDistance * ((float)(tickCount - lingerIntervals) / floatUpAnimIntervals) );
					statusPanel_left.setLocation(statusPanel_left.getLocation().x, originalY + offsetY);
				}
				int frameIndex = totalTickCount - tickCount;
				frameIndex = Math.floorDiv(frameIndex, 2);
				if(frameIndex >= statusAnim_left.length)
					frameIndex = frameIndex % statusAnim_left.length;
				statusPanel_left.SetNewImage(statusAnim_left[frameIndex]);
				statusPanel_left.repaint(40);
				
				if(tickCount <= 0) {
					tickCount = totalTickCount;
					statusPanel_left.setLocation(statusPanel_left.getLocation().x, originalY);
					statusPanelTimer_left.stop();
					statusPanel_left.setVisible(false);
					OnFeedbackMessageDone(false);
				} else {
					tickCount--;
					if(!statusPanel_left.isVisible())
						statusPanel_left.setVisible(true);
				}
			}
		});
		statusPanelTimer_left.setInitialDelay(0);
		statusPanelTimer_left.setRepeats(true);
		
		statusPanelTimer_right = new Timer(50, new ActionListener() {
			final int originalY = statusPanelPos.y;
			final int offsetYDistance =  GUIUtil.GetRelativeSize(0.035f, true).height;
			final int totalTickCount = floatUpAnimIntervals + lingerIntervals;
			private int tickCount = totalTickCount;
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(tickCount > lingerIntervals) {
					int offsetY = Math.round(offsetYDistance * ((float)(tickCount - lingerIntervals) / floatUpAnimIntervals) );
					statusPanel_right.setLocation(statusPanel_right.getLocation().x, originalY + offsetY);
				}
				int frameIndex = totalTickCount - tickCount;
				frameIndex = Math.floorDiv(frameIndex, 2);
				if(frameIndex >= statusAnim_right.length)
					frameIndex = frameIndex % statusAnim_right.length;
				statusPanel_right.SetNewImage(statusAnim_right[frameIndex]);
				statusPanel_right.repaint(40);
				
				if(tickCount <= 0) {
					tickCount = totalTickCount;
					statusPanel_right.setLocation(statusPanel_right.getLocation().x, originalY);
					statusPanelTimer_right.stop();
					statusPanel_right.setVisible(false);
					OnFeedbackMessageDone(true);
				} else {
					tickCount--;
					if(!statusPanel_right.isVisible())
						statusPanel_right.setVisible(true);
				}
			}
		});
		statusPanelTimer_right.setInitialDelay(0);
		statusPanelTimer_right.setRepeats(true);
		
		
		//Wait to get these until we know the size of the effect so we can get the desired position based on the Characters position and size vs the effects size
		Point particlePosition_left = null;
		Point particlePosition_right = null;
		//Way too big, it's 11.807693 in actuality
		//float characterScaleRatio = (float)characterSize.width / SpriteSheetUtility.GetWalkSheet(ClassType.RONIN).GetSprite(0, 1, 1).getTileWidth();
		float characterScaleRatio = 6f;
		
		particleFXs_left = new BattleEffectController[maxParticleCount];
		particleFXs_right = new BattleEffectController[maxParticleCount];
		for(int i = 0; i < maxParticleCount; i++) {
			//Create and add PFX for Left
			particleFXs_left[i] = new BattleEffectController(effect_weapons_1_4.javaSheetFilePath, effect_weapons_1_4.nonActorFrames_startIndex, effect_weapons_1_4.nonActorFrames_endIndex,
					true, 0, 0, characterScaleRatio);
			if(particlePosition_left == null)
				particlePosition_left = GetAnchorPoint(true, AnchorType.FullyCocked, particleFXs_left[i].getEffectSize());
			particleFXs_left[i].GetImagePanel().setBounds(particlePosition_left.x, particlePosition_left.y, particleFXs_left[i].getEffectSize().width, particleFXs_left[i].getEffectSize().height);
			add(particleFXs_left[i].GetImagePanel(), paneIndex);
			paneIndex++;
			//particleFXs_left[i].GetImagePanel().setBorder(BorderFactory.createLineBorder(Color.ORANGE, 1));
			//if(i > 0) //TODO remove after debugging
				particleFXs_left[i].GetImagePanel().setVisible(false);
			
			//Create and add PFX for Right
			particleFXs_right[i] = new BattleEffectController(effect_weapons_1_4.javaSheetFilePath, effect_weapons_1_4.nonActorFrames_startIndex, effect_weapons_1_4.nonActorFrames_endIndex,
					true, 0, 0, characterScaleRatio);
			if(particlePosition_right == null)
				particlePosition_right = GetAnchorPoint(false, AnchorType.FullyCocked, particleFXs_right[i].getEffectSize());
			particleFXs_right[i].GetImagePanel().setBounds(particlePosition_right.x, particlePosition_right.y, particleFXs_right[i].getEffectSize().width, particleFXs_right[i].getEffectSize().height);
			add(particleFXs_right[i].GetImagePanel(), paneIndex);
			paneIndex++;
			//particleFXs_right[i].GetImagePanel().setBorder(BorderFactory.createLineBorder(Color.ORANGE, 1));
			//if(i > 0) //TODO remove after debugging
				particleFXs_right[i].GetImagePanel().setVisible(false);
		}
		
		//setup character on left
		//characterController_left = new BattleCharacterController(ClassType.BANDIT, WeaponType.Katana, null, true);
		characterController_left = new BattleCharacterController(null, null, true);
		
		characterController_left.GetImagePanel().setOpaque(false);
		characterController_left.GetImagePanel().setBackground(new Color(0f,0f,0f,0f));
		characterController_left.GetImagePanel().setBounds(characterStartPos_left.x, characterStartPos_left.y, characterSize.width, characterSize.height);
		//characterController_left.GetImagePanel().setBorder(BorderFactory.createLineBorder(Color.ORANGE, 1));
		carriage_left.add(characterController_left.GetImagePanel(), carriageIndex_left);
		carriageIndex_left++;
		
		//setup character on right
		//characterController_right = new BattleCharacterController(ClassType.BANDIT, WeaponType.Katana, null, false);
		characterController_right = new BattleCharacterController(null, null, false);
		
		characterController_right.GetImagePanel().setOpaque(false);
		characterController_right.GetImagePanel().setBackground(new Color(0f,0f,0f,0f));
		characterController_right.GetImagePanel().setBounds(characterStartPos_right.x, characterStartPos_right.y, characterSize.width, characterSize.height);
		//characterController_right.GetImagePanel().setBorder(BorderFactory.createLineBorder(Color.ORANGE, 1));
		carriage_right.add(characterController_right.GetImagePanel(), carriageIndex_right);
		carriageIndex_right++;
		
		
		//Weapon sprites
		//NOTE: This Weapon is being added to the the CombatAnimPane, not the carrage, so the weapon can travel the screen freely(necessary for throwing weapon anims)
		characterController_left.GetWeaponImagePanel().setOpaque(false);
		characterController_left.GetWeaponImagePanel().setBackground(new Color(0f,0f,0f,0f));
		characterController_left.GetWeaponImagePanel().setBounds(particlePosition_left.x, particlePosition_left.y, (characterSize.width / 3), (characterSize.height / 3));
		//characterController_left.GetWeaponImagePanel().setBorder(BorderFactory.createLineBorder(Color.RED, 1));
		add(characterController_left.GetWeaponImagePanel(), paneIndex);
		paneIndex++;
		
		//NOTE: This Weapon is being added to the the CombatAnimPane, not the carrage, so the weapon can travel the screen freely(necessary for throwing weapon anims)
		characterController_right.GetWeaponImagePanel().setOpaque(false);
		characterController_right.GetWeaponImagePanel().setBackground(new Color(0f,0f,0f,0f));
		characterController_right.GetWeaponImagePanel().setBounds(particlePosition_right.x, particlePosition_right.y, (characterSize.width / 3), (characterSize.height / 3));
		//characterController_right.GetWeaponImagePanel().setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));
		add(characterController_right.GetWeaponImagePanel(), paneIndex);
		paneIndex++;
		
		
		add(carriage_left, paneIndex);
		paneIndex++;
		
		add(carriage_right, paneIndex);
		paneIndex++;
		
		
		//Terrain Tiles
		terrainCarriageOffset = new Point((carriageDimension.width/2) - (terrainSize.width/2), carriageDimension.height - terrainSize.height);
		//setup tile on left
		terrainTile_left = new ImagePanel(GUIUtil.GetCombatTile(TerrainType.GRASS));
		terrainTile_left.setOpaque(false);
		terrainTile_left.setBackground(new Color(0,0,0,0));
		terrainTile_left.setBounds(carriagePosition_left.x + terrainCarriageOffset.x, carriagePosition_left.y + terrainCarriageOffset.y, terrainSize.width, terrainSize.height);
		add(terrainTile_left, paneIndex);
		paneIndex++;
		//setup tile on right
		terrainTile_right = new ImagePanel(GUIUtil.GetCombatTile(TerrainType.GRASS));
		terrainTile_right.setOpaque(false);
		terrainTile_right.setBackground(new Color(0,0,0,0));
		terrainTile_right.setBounds(carriagePosition_right.x + terrainCarriageOffset.x, carriagePosition_right.y + terrainCarriageOffset.y, terrainSize.width, terrainSize.height);
		add(terrainTile_right, paneIndex);
		paneIndex++;
		
		
		//Setup lightbox
		lightbox = new FadeTransitionPanel(new Color(0.02f, 0.02f, 0.03f, 0.8f));
		lightbox.setSize(dimension);
		add(lightbox, paneIndex);
		paneIndex++;
		
		
		enterAnim = new Timer(timerDelay, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int nextLeftX = Math.min(0, fadeOutOverlay_left.getLocation().x + fadeOutEdgeSpeed);
				fadeOutOverlay_left.setBounds(nextLeftX, fadeOutOverlay_left.getLocation().y,
						  					  fadeOutOverlay_left.getSize().width, fadeOutOverlay_left.getSize().height);

				int nextRightX = Math.max(dimension.width - fadeOutWidth, fadeOutOverlay_right.getLocation().x - fadeOutEdgeSpeed);
				fadeOutOverlay_right.setBounds(nextRightX, fadeOutOverlay_right.getLocation().y,
											   fadeOutOverlay_right.getSize().width, fadeOutOverlay_right.getSize().height);
				
				int nextTerrainY = Math.max(targetCarriageHeight, carriage_left.getLocation().y - terrainSpeed);
				int diffY = carriage_left.getLocation().y - nextTerrainY;
				
				//carriage_left.setBounds(dimension.width / 2 - carriageDimension.width + carriageCenterOffset, nextTerrainY, carriageDimension.width, carriageDimension.height);
				//carriage_right.setBounds(dimension.width / 2 - carriageCenterOffset, nextTerrainY, carriageDimension.width, carriageDimension.height);
				//carriage_left.setBounds(dimension.width / 2 - carriageDimension.width + carriageCenterOffset + AttackRangeOffset(false), nextTerrainY, carriageDimension.width, carriageDimension.height);
				//carriage_right.setBounds(dimension.width / 2 - carriageCenterOffset + AttackRangeOffset(true), nextTerrainY, carriageDimension.width, carriageDimension.height);
				carriage_left.setBounds(carriage_left.getLocation().x, nextTerrainY, carriageDimension.width, carriageDimension.height);
				carriage_right.setBounds(carriage_right.getLocation().x, nextTerrainY, carriageDimension.width, carriageDimension.height);
				
				terrainTile_left.setBounds(carriage_left.getLocation().x + terrainCarriageOffset.x, carriage_left.getLocation().y + terrainCarriageOffset.y, terrainSize.width, terrainSize.height);
				terrainTile_right.setBounds(carriage_right.getLocation().x + terrainCarriageOffset.x, carriage_right.getLocation().y + terrainCarriageOffset.y, terrainSize.width, terrainSize.height);
				
				characterController_left.GetWeaponImagePanel().setBounds(
						characterController_left.GetWeaponImagePanel().getLocation().x,
						characterController_left.GetWeaponImagePanel().getLocation().y - diffY,
						characterController_left.GetWeaponImagePanel().getSize().width, characterController_left.GetWeaponImagePanel().getSize().height);
		
				characterController_right.GetWeaponImagePanel().setBounds(
						characterController_right.GetWeaponImagePanel().getLocation().x,
						characterController_right.GetWeaponImagePanel().getLocation().y - diffY,
						characterController_right.GetWeaponImagePanel().getSize().width, characterController_right.GetWeaponImagePanel().getSize().height);
				
				if(nextLeftX == 0
						&& nextRightX == dimension.width - fadeOutWidth
						&& nextTerrainY == targetCarriageHeight) {
					
					enterAnim.stop();
				}
			}
		});
		enterAnim.setInitialDelay(0);
		
		/*combatTimer = new Timer(combatInterval, new ActionListener() {
			int attackersSequenceIndex;
			int attackersIntervalsPast;
			Point attackersStartLocation = null;
			Point attackersEndLocation;
			
			int defendersSequenceIndex;
			boolean isDefendersSequenceIndependant;
			int defendersIntervalsPast;
			Point defendersStartLocation = null;
			Point defendersEndLocation;
			
			private void ResetVariables() {
				attackersSequenceIndex = 0;
				attackersIntervalsPast = 0;
				attackersStartLocation = null;
				attackersEndLocation = null;
				defendersSequenceIndex = 0;
				isDefendersSequenceIndependant = false;
				defendersIntervalsPast = 0;
				defendersStartLocation = null;
				defendersEndLocation = null;
			}
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(attackersStartLocation == null) {
					attackersStartLocation = characterController_left.GetImagePanel().getLocation();
					defendersStartLocation = characterController_right.GetImagePanel().getLocation();
				}
				
				boolean startNextClip = false;
				if(
					attackersSequenceIndex == 0
					||
					(attackersSequence.GetClip(attackersSequenceIndex) != null && attackersIntervalsPast >= attackersSequence.GetClip(attackersSequenceIndex).duration_combatIntervals)
				) {
					startNextClip = true;
					attackersSequenceIndex++;
					
					attackersIntervalsPast = 0;
				}
				Clip attackersClip = attackersSequence.GetClip(attackersSequenceIndex);
				
				//If the upcoming defender clip is null then increment in parrallel with the attacker sequence clip index
				boolean startNextDefenderClip = false;
				Clip currentDefendersClip = defenderSequence.GetClip(defendersSequenceIndex);
				Clip nextDefendersClip = defenderSequence.GetClip(defendersSequenceIndex + 1);
				boolean isStartingDependantClip = startNextClip && !isDefendersSequenceIndependant && currentDefendersClip == null;
				
				//Now all defender sequences will become independant upon reaching their first clip. This will leave the term "dependant" meaning soley that the defender's sequence is null up to that point
				boolean isStartOfIndependantDefendersSequence = isStartingDependantClip && nextDefendersClip != null;
				if(isStartOfIndependantDefendersSequence)
					isStartingDependantClip = false;
				
				boolean isNextIndependantClip = currentDefendersClip != null && defendersIntervalsPast >= currentDefendersClip.duration_combatIntervals && nextDefendersClip != null;
				if(isStartingDependantClip || isStartOfIndependantDefendersSequence || isNextIndependantClip) {
					if(isStartOfIndependantDefendersSequence)
						isDefendersSequenceIndependant = true;
					
					startNextDefenderClip = true;
					defendersSequenceIndex++;
					
					defendersIntervalsPast = 0;
				}
				Clip defendersClip = defenderSequence.GetClip(defendersSequenceIndex);
				
				//and then when we're done
				boolean isAttackerDone = attackersClip == null;
				boolean isDefenderDone = currentDefendersClip != null && defendersIntervalsPast >= currentDefendersClip.duration_combatIntervals && nextDefendersClip == null;
				if(isAttackerDone && isDefenderDone) {
					
					for(int i = 0; i < maxParticleCount; i++) {
						if(particleFXs_left[i].getAnimTimer().isRunning())
							System.err.println("Combat is ending before the previous left effect is finished.");
						if(particleFXs_right[i].getAnimTimer().isRunning())
							System.err.println("Combat is ending before the previous right effect is finished.");
					}
					
					if(!isFeedbackAnimPlaying_left && !isFeedbackAnimPlaying_right)
						EndCombatAnim();
					//These might need to also be flow controlled to wait for the feedback anims to end
					combatTimer.stop();
					ResetVariables();
					return;
				}
				
				
				//Handle stuff at the beginning of a Clip for attacker
				if(startNextClip && !isAttackerDone) {
					//System.out.println("Starting attackers clip: " + attackersSequenceIndex);
					
					effectNodeList_left.clear();
					//Get new bois
					EffectSocket[] attackersEffectSockets = null;
					
					
					//Make adjustments if we're targeting ourselves
					if(attacker == defender) {
						Clip clip = defenderSequence.GetClip(attackersSequenceIndex);
						
						if(clip != null) {
							if(clip.socketAddressArray != null && clip.socketAddressArray.length > 0) {
								characterController_left.SetStillFrame(clip.genericAnimType, clip.socketAddressArray, isRangedAttack_attacker);
							} else if(clip.genericAnimType != null) {
								if(clip.playAnimAtDefaultSpeed)
									characterController_left.PlayAnim(clip.genericAnimType, clip.loopAnim, isRangedAttack_attacker);
								else
									characterController_left.PlayAnim(clip.genericAnimType, clip.duration_combatIntervals * combatInterval, isRangedAttack_attacker);
							}
							//Get movement endPosition
							if(clip.xMovement != 0)
								attackersEndLocation = new Point(attackersStartLocation.x + clip.xMovement, attackersStartLocation.y);
							else
								attackersEndLocation = null;
						}
						
						//Show our own damage number when targeting ourselves
						if(attackersSequenceIndex == attackersSequence.clips.length && !isFeedbackAnimPlaying_left)
							HandleFeedbackQueue(false, didAttackHit, healthModInfo);
					} else {
					
						
						if(attackersClip.socketAddressArray != null && attackersClip.socketAddressArray.length > 0) {
							characterController_left.SetStillFrame(attackersClip.genericAnimType, attackersClip.socketAddressArray, isRangedAttack_attacker);
						} else if(attackersClip.genericAnimType != null) {
							if(attackersClip.playAnimAtDefaultSpeed)
								characterController_left.PlayAnim(attackersClip.genericAnimType, attackersClip.loopAnim, isRangedAttack_attacker);
							else
								characterController_left.PlayAnim(attackersClip.genericAnimType, attackersClip.duration_combatIntervals * combatInterval, isRangedAttack_attacker);
						}
						//Get movement endPosition
						attackersEndLocation = new Point(attackersStartLocation.x + attackersClip.xMovement, attackersStartLocation.y);
						attackersEffectSockets = attackersSequence.GetEffectSockets(attackersSequenceIndex);
						
						
					}
					
					
					List<EffectClip> attackersEffectClips = new ArrayList<EffectClip>();
					if(attackersEffectSockets != null) {
						//Making slight adaptations for unarmed logic
						AnimSubset attackersOverridingSubset = null;
						if(attackersClip.genericAnimType != null) {
							AnimSocketsPack attackersAnimSocketsPack = SpriteSheetUtility.GetHandSocketsForAnim(attackersClip.genericAnimType, characterController_left.GetWeaponType(), isRangedAttack_attacker);
							if(attackersAnimSocketsPack != null)
								attackersOverridingSubset = attackersAnimSocketsPack.animSubset;
						}
						
						attackersEffectClips = GetEffectClipsForChar(true, attackersEffectSockets, attackersOverridingSubset, characterController_left.GetWeaponType());
					}
					if(attackersEffectClips != null) {
						for(int i = 0; i < attackersEffectClips.size(); i++) {
							EffectClip effectClip = attackersEffectClips.get(i);
							
							if(effectClip == null)
								continue;
							
							if(particleFXs_left[i].getAnimTimer().isRunning())
								System.err.println("A new left effect is starting before the previous one is finished");
							
							if(effectClip.effectData == null)
								System.err.println("CombatAnimPane - effectClip.effectData == null, if this is an ability or item use then the overriding effect didnt get set properly on attackersSequence!");
							
							//Setup particleFX for this effect
							particleFXs_left[i].SetToNewEffect(effectClip.effectData.javaSheetFilePath, effectClip.effectData.nonActorFrames_startIndex,
									effectClip.effectData.nonActorFrames_endIndex, true, effectClip.initialDelay_ms, effectClip.animSpeedOverride, characterScaleRatio);
							//Position particleFX to target location relative to the character
							Point position = GetAnchorPoint(true, effectClip.startingAnchorPosition, particleFXs_left[i].getEffectSize());
							//System.out.println("Attackers Anchor Pos: " + position + ", characterSize: " + characterController_left.GetImagePanel().getWidth()
							//		+ ", right carriage x: " + carriage_right.getLocation().x);
							
							particleFXs_left[i].GetImagePanel().setBounds(position.x, position.y, particleFXs_left[i].getEffectSize().width, particleFXs_left[i].getEffectSize().height);
							
							//effectClip.startLocation = position; //This was getting used by both left and right and the right would always overwrite left's values during update
							//Store these in our member structure used to track effect and their states.
							effectNodeList_left.add(new EffectNode(effectClip, position));
							
							//Show the effect frame or play the effect anim
							if(effectClip.animFrameIndex > -1) {
								particleFXs_left[i].SetStillFrame(effectClip.animFrameIndex);
							} else {
								if(effectClip.useControllersAnimSpeed)
									particleFXs_left[i].PlayAnim(effectClip.loopAnim);
								else
									particleFXs_left[i].PlayAnim(effectClip.duration_combatIntervals * combatInterval);
							}
						}
					}
				}
				
				
				
				//Handle stuff at the beginning of a Clip for defender
				if(startNextDefenderClip) {
					//System.out.println("Starting defenders clip: " + defendersSequenceIndex + ", isStartingDependantClip: " + isStartingDependantClip +
					//																		  ", isStartOfIndependantDefendersSequence: " + isStartOfIndependantDefendersSequence +
					//																		  ", isNextIndependantClip: " + isNextIndependantClip);
					int movementX = 0;
					if(defendersClip != null) {
						movementX = defendersClip.xMovement;
						
						//if(defendersClip.socketAddressArray != null) {
						if(defendersClip.socketAddressArray != null && defendersClip.socketAddressArray.length > 0) { //This needed a bit more spice
							
							characterController_right.SetStillFrame(defendersClip.genericAnimType, defendersClip.socketAddressArray, isRangedAttack_defender);
						} else if(defendersClip.genericAnimType != null) {
							if(defendersClip.playAnimAtDefaultSpeed)
								characterController_right.PlayAnim(defendersClip.genericAnimType, defendersClip.loopAnim, isRangedAttack_defender);
							else
								characterController_right.PlayAnim(defendersClip.genericAnimType, defendersClip.duration_combatIntervals * combatInterval, isRangedAttack_defender);
						}
						
						//Show Damage number
						//if(defendersSequenceIndex == defenderSequence.clips.length && !isFeedbackAnimPlaying_right) {
						//I think should need to keep this from running when targeting ourselves, the right side stuff is hidden anyways, there's no point in updating it
						if(defendersSequenceIndex == defenderSequence.clips.length && !isFeedbackAnimPlaying_right && attacker != defender)
							HandleFeedbackQueue(true, didAttackHit, healthModInfo);
					}
					
					//Get movement endPosition
					defendersEndLocation = new Point(defendersStartLocation.x - movementX, defendersStartLocation.y);
					
					//System.out.println("Defenders move distance: " + (defendersEndLocation.x - defendersStartLocation.x));
					
					effectNodeList_right.clear();
					//Get new bois for the right side
					EffectSocket[] defendersEffectSockets = defenderSequence.GetEffectSockets(defendersSequenceIndex);
					AnimSubset defendersOverridingSubset = null;
					
					if(defendersClip != null) { //I think we should skip this whole block if there is no clip for the defender
						
						if(defendersClip.genericAnimType != null) {
							AnimSocketsPack defendersAnimSocketsPack =
							SpriteSheetUtility.GetHandSocketsForAnim(defendersClip.genericAnimType, characterController_right.GetWeaponType(), isRangedAttack_defender);
							defendersOverridingSubset = defendersAnimSocketsPack.animSubset;
						}
						List<EffectClip> defendersEffectClips = new ArrayList<EffectClip>();
						
						//if(defendersEffectSockets != null) {
							//Sometimes we may not have a subset either. This may be a bug, though.
						if(defendersEffectSockets != null && defendersOverridingSubset != null) {
							
							defendersEffectClips = GetEffectClipsForChar(false, defendersEffectSockets, defendersOverridingSubset, characterController_right.GetWeaponType());
						}
						if(defendersEffectClips != null) {
							for(int i = 0; i < defendersEffectClips.size(); i++) {
								EffectClip effectClip = defendersEffectClips.get(i);
							
								if(effectClip == null)
									continue;
								
								if(particleFXs_right[i].getAnimTimer().isRunning())
									System.err.println("A new right effect is starting before the previous one is finished");
								
								if(effectClip.effectData == null)
									System.err.println("CombatAnimPane - effectClip.effectData == null, if this is an ability or item use then the overriding effect didnt get set properly" + 
									"on defenderSequence!");
								
								//Setup particleFX for this effect
								particleFXs_right[i].SetToNewEffect(effectClip.effectData.javaSheetFilePath, effectClip.effectData.nonActorFrames_startIndex, effectClip.effectData.nonActorFrames_endIndex,
										false, effectClip.initialDelay_ms, effectClip.animSpeedOverride, characterScaleRatio);
								//Position particleFX to target location relative to the character
								Point position = GetAnchorPoint(false, effectClip.startingAnchorPosition, particleFXs_right[i].getEffectSize());
								//System.out.println("Defenders Anchor Pos: " + position);
								
								particleFXs_right[i].GetImagePanel().setBounds(position.x, position.y, particleFXs_right[i].getEffectSize().width, particleFXs_right[i].getEffectSize().height);
								
								//Store these in our member structure used to track effect and their states.
								effectNodeList_right.add(new EffectNode(effectClip, position));
								
								//Show the effect frame or play the effect anim
								if(effectClip.animFrameIndex > -1) {
									particleFXs_right[i].SetStillFrame(effectClip.animFrameIndex);
								} else {
									if(effectClip.useControllersAnimSpeed)
										particleFXs_right[i].PlayAnim(effectClip.loopAnim);
									else
										particleFXs_right[i].PlayAnim(effectClip.duration_combatIntervals * combatInterval);
								}
							}
						}
						
					}
					
				}
				
				//Move the attacker and their effects
				if(!isAttackerDone) {
					
					//These should be more generic, so that we can use the variables collected early in this block instead of specific logic(otherwise our self-target spoofing doesn't work)
					//if(attackersClip.xMovement != 0) {
					//Be prepared for a null attackersEndLocation during movementless Clips
					if(
							(attacker != defender && attackersClip.xMovement != 0)
							||
							(attacker == defender && attackersEndLocation != null && attackersStartLocation != attackersEndLocation)
					) {
					
						float normProgress = (float)attackersIntervalsPast / attackersClip.duration_combatIntervals;
						int targetX = lerp(attackersStartLocation.x, attackersEndLocation.x, normProgress);
						if(characterController_left.GetImagePanel().getBounds().x != targetX) {
							//This probably isn't necessary but we'll do it to ensure the weapon positioning is happening in an identical manner for both sides
							int diffX = targetX - characterController_left.GetImagePanel().getLocation().x;
							characterController_left.GetImagePanel().setBounds(targetX, characterController_left.GetImagePanel().getLocation().y,
									characterController_left.GetImagePanel().getSize().width, characterController_left.GetImagePanel().getSize().height);
							
							//Weapon Movement
							ImagePanel weaponPanel = characterController_left.GetWeaponImagePanel();
							weaponPanel.setBounds(weaponPanel.getLocation().x + diffX, weaponPanel.getLocation().y,
									weaponPanel.getSize().width, weaponPanel.getSize().height);
						}
					} else {
						//Apparently if the character isn't moving it doesn't update itself during non-moving Anims
						characterController_left.GetImagePanel().repaint();
					}
				}
				if(effectNodeList_left != null) {
					for(int i = 0; i < effectNodeList_left.size(); i++) {
						EffectNode effectNode = effectNodeList_left.get(i);
						if(effectNode.getEffectClip() == null)
							continue;
						int xEffectTarget = effectNode.getEffectClip().xMovement;
						if(xEffectTarget != 0) {
							float normProgress = (float)attackersIntervalsPast / effectNode.getEffectClip().duration_combatIntervals;
							int currentX = effectNode.getStartLocation().x + Math.round(xEffectTarget * normProgress);
							if(particleFXs_left[i].GetImagePanel().getBounds().x != currentX)
								particleFXs_left[i].GetImagePanel().setBounds(currentX, particleFXs_left[i].GetImagePanel().getLocation().y,
										particleFXs_left[i].GetImagePanel().getSize().width, particleFXs_left[i].GetImagePanel().getSize().height);
						}
					}
				}
				
				//Move the defender and their effects
				if(defendersClip != null && defendersIntervalsPast < defendersClip.duration_combatIntervals) {
					if(defendersClip.xMovement != 0) {
						float normProgress = (float)defendersIntervalsPast / defendersClip.duration_combatIntervals;
						//The defenders movement should be applied in the opposite direction so that movement patterns are maintained and its the anims xMovement positivity/negativity
						//that determines direction
						int targetX = lerp(defendersStartLocation.x, defendersEndLocation.x, normProgress);
						if(characterController_right.GetImagePanel().getBounds().x != targetX) {
							//int diffX = currentX - characterController_right.GetImagePanel().getBounds().x;
							//My theory is that Bounds.x is being capped once the character image starts moving beyond the bounds of the panel, use Location instead which should provide unmodified info
							int diffX = targetX - characterController_right.GetImagePanel().getLocation().x;
							characterController_right.GetImagePanel().setBounds(targetX, characterController_right.GetImagePanel().getLocation().y,
									characterController_right.GetImagePanel().getSize().width, characterController_right.GetImagePanel().getSize().height);
							
							//Weapon Movement Shit
							ImagePanel weaponPanel = characterController_right.GetWeaponImagePanel();
							weaponPanel.setBounds(weaponPanel.getLocation().x + diffX, weaponPanel.getLocation().y,
									weaponPanel.getSize().width, weaponPanel.getSize().height);
						}
					} else {
						//Apparently if the character isn't moving it doesn't update itself during non-moving Anims
						characterController_right.GetImagePanel().repaint();
					}
				}
				if(effectNodeList_right != null) {
					for(int i = 0; i < effectNodeList_right.size(); i++) {
						EffectNode effectNode = effectNodeList_right.get(i);
						if(effectNode.getEffectClip() == null)
							continue;
						int xEffectTarget = effectNode.getEffectClip().xMovement;
						if(xEffectTarget != 0) {
							float normProgress = (float)attackersIntervalsPast / effectNode.getEffectClip().duration_combatIntervals;
							
							//int currentX = effectClip.startLocation.x + Math.round(xEffectTarget * normProgress);
							int currentX = effectNode.getStartLocation().x - Math.round(xEffectTarget * normProgress);
							
							if(particleFXs_right[i].GetImagePanel().getBounds().x != currentX)
								particleFXs_right[i].GetImagePanel().setBounds(currentX, particleFXs_right[i].GetImagePanel().getLocation().y,
										particleFXs_right[i].GetImagePanel().getSize().width, particleFXs_right[i].GetImagePanel().getSize().height);
						}
					}
				}
				
				
				attackersIntervalsPast++;
				defendersIntervalsPast++;
			}
		});*/
		//Converting this logic into an a more generic version which doesn't set any left or right variables explicitly, only "attacker" and "defender" variables. Assignments will be handled at the beginning
		// of the actionPerformed method in a conditional that only gets run once during the first iteration of the combatTimer. This allows the left side to be either attacker or defender without messy
		// conditionals and workarounds bloating the code.
		combatTimer = new Timer(combatInterval, new ActionListener() {
			//New Generic Approach
			BattleCharacterController attackerController;
			BattleCharacterController defenderController;
			BattleEffectController[] attackersParticleFXs;
			BattleEffectController[] defendersParticleFXs;
			List<EffectNode> attackersEffectNodeList = new ArrayList<EffectNode>();
			List<EffectNode> defendersEffectNodeList = new ArrayList<EffectNode>();
			
			int attackersSequenceIndex;
			int attackersIntervalsPast;
			Point attackersStartLocation = null;
			Point attackersEndLocation;
			
			int defendersSequenceIndex;
			boolean isDefendersSequenceIndependant;
			int defendersIntervalsPast;
			Point defendersStartLocation = null;
			Point defendersEndLocation;
			
			private void ResetVariables() {
				attackersSequenceIndex = 0;
				attackersIntervalsPast = 0;
				attackersStartLocation = null;
				attackersEndLocation = null;
				defendersSequenceIndex = 0;
				isDefendersSequenceIndependant = false;
				defendersIntervalsPast = 0;
				defendersStartLocation = null;
				defendersEndLocation = null;
			}
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(attackersStartLocation == null) {
					
					//Setup new generic variables here. This will need to rely on member variable instructions at some point to determine who is what
					if(!isRightCharacterTheAttacker) {
						attackerController = characterController_left;
						defenderController = characterController_right;
						attackersParticleFXs = particleFXs_left;
						defendersParticleFXs = particleFXs_right;
					} else {
						attackerController = characterController_right;
						defenderController = characterController_left;
						attackersParticleFXs = particleFXs_right;
						defendersParticleFXs = particleFXs_left;
					}
					
					attackersStartLocation = attackerController.GetImagePanel().getLocation();
					defendersStartLocation = defenderController.GetImagePanel().getLocation();
				}
				
				boolean startNextClip = false;
				if(
					attackersSequenceIndex == 0
					||
					(attackersSequence.GetClip(attackersSequenceIndex) != null && attackersIntervalsPast >= attackersSequence.GetClip(attackersSequenceIndex).duration_combatIntervals)
				) {
					startNextClip = true;
					attackersSequenceIndex++;
					
					attackersIntervalsPast = 0;
				}
				Clip attackersClip = attackersSequence.GetClip(attackersSequenceIndex);
				
				//If the upcoming defender clip is null then increment in parrallel with the attacker sequence clip index
				boolean startNextDefenderClip = false;
				Clip currentDefendersClip = defenderSequence.GetClip(defendersSequenceIndex);
				Clip nextDefendersClip = defenderSequence.GetClip(defendersSequenceIndex + 1);
				boolean isStartingDependantClip = startNextClip && !isDefendersSequenceIndependant && currentDefendersClip == null;
				
				//Now all defender sequences will become independant upon reaching their first clip. This will leave the term "dependant" meaning soley that the defender's sequence is null up to that point
				boolean isStartOfIndependantDefendersSequence = isStartingDependantClip && nextDefendersClip != null;
				if(isStartOfIndependantDefendersSequence)
					isStartingDependantClip = false;
				
				boolean isNextIndependantClip = currentDefendersClip != null && defendersIntervalsPast >= currentDefendersClip.duration_combatIntervals && nextDefendersClip != null;
				if(isStartingDependantClip || isStartOfIndependantDefendersSequence || isNextIndependantClip) {
					if(isStartOfIndependantDefendersSequence)
						isDefendersSequenceIndependant = true;
					
					startNextDefenderClip = true;
					defendersSequenceIndex++;
					
					defendersIntervalsPast = 0;
				}
				Clip defendersClip = defenderSequence.GetClip(defendersSequenceIndex);
				
				//and then when we're done
				boolean isAttackerDone = attackersClip == null;
				boolean isDefenderDone = currentDefendersClip != null && defendersIntervalsPast >= currentDefendersClip.duration_combatIntervals && nextDefendersClip == null;
				if(isAttackerDone && isDefenderDone) {
					
					for(int i = 0; i < maxParticleCount; i++) {
						if(attackersParticleFXs[i].getAnimTimer().isRunning())
							System.err.println("Combat is ending before the previous left effect is finished.");
						if(defendersParticleFXs[i].getAnimTimer().isRunning())
							System.err.println("Combat is ending before the previous right effect is finished.");
					}
					
					if(!isAttackersFeedbackAnimPlaying && !isDefendersFeedbackAnimPlaying)
						EndCombatAnim();
					//These might need to also be flow controlled to wait for the feedback anims to end
					combatTimer.stop();
					ResetVariables();
					return;
				}
				
				
				//Handle stuff at the beginning of a Clip for attacker
				if(startNextClip && !isAttackerDone) {
					//System.out.println("Starting attackers clip: " + attackersSequenceIndex);
					
					attackersEffectNodeList.clear();
					
					if(attackersClip.socketAddressArray != null && attackersClip.socketAddressArray.length > 0) {
						attackerController.SetStillFrame(attackersClip.genericAnimType, attackersClip.socketAddressArray, isRangedAttack_attacker);
					} else if(attackersClip.genericAnimType != null) {
						if(attackersClip.playAnimAtDefaultSpeed)
							attackerController.PlayAnim(attackersClip.genericAnimType, attackersClip.loopAnim, isRangedAttack_attacker);
						else
							attackerController.PlayAnim(attackersClip.genericAnimType, attackersClip.duration_combatIntervals * combatInterval, isRangedAttack_attacker);
					}
					//Get movement endPosition
					
					//attackersEndLocation = new Point(attackersStartLocation.x + attackersClip.xMovement, attackersStartLocation.y);
					int xPos = 0;
					if(!isRightCharacterTheAttacker)
						xPos = attackersStartLocation.x + attackersClip.xMovement;
					else
						xPos = attackersStartLocation.x - attackersClip.xMovement;
					attackersEndLocation = new Point(xPos, attackersStartLocation.y);
					
					EffectSocket[] attackersEffectSockets = attackersSequence.GetEffectSockets(attackersSequenceIndex);
					
					List<EffectClip> attackersEffectClips = new ArrayList<EffectClip>();
					if(attackersEffectSockets != null) {
						//Making slight adaptations for unarmed logic
						AnimSubset attackersOverridingSubset = null;
						if(attackersClip.genericAnimType != null) {
							AnimSocketsPack attackersAnimSocketsPack = SpriteSheetUtility.GetHandSocketsForAnim(attackersClip.genericAnimType, attackerController.GetWeaponType(), isRangedAttack_attacker);
							if(attackersAnimSocketsPack != null)
								attackersOverridingSubset = attackersAnimSocketsPack.animSubset;
						}
						
						attackersEffectClips = GetEffectClipsForChar(attackersEffectSockets, attackersOverridingSubset, attackerController.GetWeaponType());
					}
					if(attackersEffectClips != null) {
						for(int i = 0; i < attackersEffectClips.size(); i++) {
							EffectClip effectClip = attackersEffectClips.get(i);
							
							if(effectClip == null)
								continue;
							
							if(attackersParticleFXs[i].getAnimTimer().isRunning())
								System.err.println("A new left effect is starting before the previous one is finished");
							
							if(effectClip.effectData == null)
								System.err.println("CombatAnimPane - effectClip.effectData == null, if this is an ability or item use then the overriding effect didnt get set properly on attackersSequence!");
							
							//Setup particleFX for this effect
							attackersParticleFXs[i].SetToNewEffect(effectClip.effectData.javaSheetFilePath, effectClip.effectData.nonActorFrames_startIndex,
									effectClip.effectData.nonActorFrames_endIndex, !isRightCharacterTheAttacker, effectClip.initialDelay_ms, effectClip.animSpeedOverride, characterScaleRatio);
							//Position particleFX to target location relative to the character
							Point position = GetAnchorPoint(!isRightCharacterTheAttacker, effectClip.startingAnchorPosition, attackersParticleFXs[i].getEffectSize());
							System.out.println("Attackers Anchor Pos: " + position + ", characterSize: " + characterController_left.GetImagePanel().getWidth()
									+ ", right carriage x: " + carriage_right.getLocation().x);
							
							attackersParticleFXs[i].GetImagePanel().setBounds(position.x, position.y, attackersParticleFXs[i].getEffectSize().width, attackersParticleFXs[i].getEffectSize().height);
							
							//effectClip.startLocation = position; //This was getting used by both left and right and the right would always overwrite left's values during update
							//Store these in our member structure used to track effect and their states.
							attackersEffectNodeList.add(new EffectNode(effectClip, position));
							
							//Show the effect frame or play the effect anim
							if(effectClip.animFrameIndex > -1) {
								attackersParticleFXs[i].SetStillFrame(effectClip.animFrameIndex);
							} else {
								if(effectClip.useControllersAnimSpeed)
									attackersParticleFXs[i].PlayAnim(effectClip.loopAnim);
								else
									attackersParticleFXs[i].PlayAnim(effectClip.duration_combatIntervals * combatInterval);
							}
						}
					}
				}
				
				
				
				//Handle stuff at the beginning of a Clip for defender
				if(startNextDefenderClip) {
					//System.out.println("Starting defenders clip: " + defendersSequenceIndex + ", isStartingDependantClip: " + isStartingDependantClip +
					//																		  ", isStartOfIndependantDefendersSequence: " + isStartOfIndependantDefendersSequence +
					//																		  ", isNextIndependantClip: " + isNextIndependantClip);
					int movementX = 0;
					if(defendersClip != null) {
						movementX = defendersClip.xMovement;
						
						//if(defendersClip.socketAddressArray != null) {
						if(defendersClip.socketAddressArray != null && defendersClip.socketAddressArray.length > 0) { //This needed a bit more spice
							
							defenderController.SetStillFrame(defendersClip.genericAnimType, defendersClip.socketAddressArray, isRangedAttack_defender);
						} else if(defendersClip.genericAnimType != null) {
							if(defendersClip.playAnimAtDefaultSpeed)
								defenderController.PlayAnim(defendersClip.genericAnimType, defendersClip.loopAnim, isRangedAttack_defender);
							else
								defenderController.PlayAnim(defendersClip.genericAnimType, defendersClip.duration_combatIntervals * combatInterval, isRangedAttack_defender);
						}
						
						//Show Damage number
						if(defendersSequenceIndex == defenderSequence.clips.length && !isDefendersFeedbackAnimPlaying) { //&& attacker != defender) //I dont think we want this anymore cause its already handled
							//HandleFeedbackQueue(!isRightCharacterTheAttacker, didAttackHit, healthModInfo);
							boolean playOnRight = isSingleSelfTarget ? false : !isRightCharacterTheAttacker;
							HandleFeedbackQueue(playOnRight, didAttackHit, healthModInfo);	
						}
					}
					
					//Get movement endPosition
					//defendersEndLocation = new Point(defendersStartLocation.x - movementX, defendersStartLocation.y);
					int xPos = 0;
					if(!isRightCharacterTheAttacker)
						xPos = defendersStartLocation.x - movementX;
					else
						xPos = defendersStartLocation.x + movementX;
					defendersEndLocation = new Point(xPos, defendersStartLocation.y);
					
					//System.out.println("Defenders move distance: " + (defendersEndLocation.x - defendersStartLocation.x));
					
					defendersEffectNodeList.clear();
					//Get new bois for the right side
					EffectSocket[] defendersEffectSockets = defenderSequence.GetEffectSockets(defendersSequenceIndex);
					AnimSubset defendersOverridingSubset = null;
					
					if(defendersClip != null) { //I think we should skip this whole block if there is no clip for the defender
						
						if(defendersClip.genericAnimType != null) {
							AnimSocketsPack defendersAnimSocketsPack = SpriteSheetUtility.GetHandSocketsForAnim(defendersClip.genericAnimType, defenderController.GetWeaponType(), isRangedAttack_defender);
							defendersOverridingSubset = defendersAnimSocketsPack.animSubset;
						}
						List<EffectClip> defendersEffectClips = new ArrayList<EffectClip>();
						
						//if(defendersEffectSockets != null) {
							//Sometimes we may not have a subset either. This may be a bug, though.
						if(defendersEffectSockets != null && defendersOverridingSubset != null) {
							
							defendersEffectClips = GetEffectClipsForChar(defendersEffectSockets, defendersOverridingSubset, defenderController.GetWeaponType());
						}
						if(defendersEffectClips != null) {
							for(int i = 0; i < defendersEffectClips.size(); i++) {
								EffectClip effectClip = defendersEffectClips.get(i);
							
								if(effectClip == null)
									continue;
								
								if(defendersParticleFXs[i].getAnimTimer().isRunning())
									System.err.println("A new right effect is starting before the previous one is finished");
								
								if(effectClip.effectData == null)
									System.err.println("CombatAnimPane - effectClip.effectData == null, if this is an ability or item use then the overriding effect didnt get set properly"
											+ "on defenderSequence!");
								
								//Setup particleFX for this effect
								defendersParticleFXs[i].SetToNewEffect(effectClip.effectData.javaSheetFilePath, effectClip.effectData.nonActorFrames_startIndex, effectClip.effectData.nonActorFrames_endIndex,
										isRightCharacterTheAttacker, effectClip.initialDelay_ms, effectClip.animSpeedOverride, characterScaleRatio);
								//Position particleFX to target location relative to the character
								Point position = GetAnchorPoint(isRightCharacterTheAttacker, effectClip.startingAnchorPosition, defendersParticleFXs[i].getEffectSize());
								//System.out.println("Defenders Anchor Pos: " + position);
								
								defendersParticleFXs[i].GetImagePanel().setBounds(position.x, position.y, defendersParticleFXs[i].getEffectSize().width, defendersParticleFXs[i].getEffectSize().height);
								
								//Store these in our member structure used to track effect and their states.
								defendersEffectNodeList.add(new EffectNode(effectClip, position));
								
								//Show the effect frame or play the effect anim
								if(effectClip.animFrameIndex > -1) {
									defendersParticleFXs[i].SetStillFrame(effectClip.animFrameIndex);
								} else {
									if(effectClip.useControllersAnimSpeed)
										defendersParticleFXs[i].PlayAnim(effectClip.loopAnim);
									else
										defendersParticleFXs[i].PlayAnim(effectClip.duration_combatIntervals * combatInterval);
								}
							}
						}
						
					}
					
				}
				
				//Move the attacker and their effects
				if(!isAttackerDone) {
					if(attackersClip.xMovement != 0) {
						float normProgress = (float)attackersIntervalsPast / attackersClip.duration_combatIntervals;
						int targetX = lerp(attackersStartLocation.x, attackersEndLocation.x, normProgress);
						if(attackerController.GetImagePanel().getBounds().x != targetX) {
							//This probably isn't necessary but we'll do it to ensure the weapon positioning is happening in an identical manner for both sides
							int diffX = targetX - attackerController.GetImagePanel().getLocation().x;
							attackerController.GetImagePanel().setBounds(targetX, attackerController.GetImagePanel().getLocation().y,
									attackerController.GetImagePanel().getSize().width, attackerController.GetImagePanel().getSize().height);
							
							//Weapon Movement
							ImagePanel weaponPanel = attackerController.GetWeaponImagePanel();
							weaponPanel.setBounds(weaponPanel.getLocation().x + diffX, weaponPanel.getLocation().y,
									weaponPanel.getSize().width, weaponPanel.getSize().height);
						}
					} else {
						//Apparently if the character isn't moving it doesn't update itself during non-moving Anims
						attackerController.GetImagePanel().repaint();
					}
				}
				if(attackersEffectNodeList != null) {
					for(int i = 0; i < attackersEffectNodeList.size(); i++) {
						EffectNode effectNode = attackersEffectNodeList.get(i);
						if(effectNode.getEffectClip() == null)
							continue;
						int effectXMovement = effectNode.getEffectClip().xMovement;
						if(effectXMovement != 0) {
							float normProgress = (float)attackersIntervalsPast / effectNode.getEffectClip().duration_combatIntervals;
							
							
							//int currentX = effectNode.getStartLocation().x + Math.round(effectXMovement * normProgress);
							int currentX = 0;
							if(!isRightCharacterTheAttacker)
								currentX = effectNode.getStartLocation().x + Math.round(effectXMovement * normProgress);
							else
								currentX = effectNode.getStartLocation().x - Math.round(effectXMovement * normProgress);
							
							
							if(attackersParticleFXs[i].GetImagePanel().getBounds().x != currentX)
								attackersParticleFXs[i].GetImagePanel().setBounds(currentX, attackersParticleFXs[i].GetImagePanel().getLocation().y,
										attackersParticleFXs[i].GetImagePanel().getSize().width, attackersParticleFXs[i].GetImagePanel().getSize().height);
						}
					}
				}
				
				//Move the defender and their effects
				if(defendersClip != null && defendersIntervalsPast < defendersClip.duration_combatIntervals) {
					if(defendersClip.xMovement != 0) {
						float normProgress = (float)defendersIntervalsPast / defendersClip.duration_combatIntervals;
						//The defenders movement should be applied in the opposite direction so that movement patterns are maintained and its the anims xMovement positivity/negativity
						//that determines direction
						int targetX = lerp(defendersStartLocation.x, defendersEndLocation.x, normProgress);
						if(defenderController.GetImagePanel().getBounds().x != targetX) {
							//int diffX = currentX - characterController_right.GetImagePanel().getBounds().x;
							//My theory is that Bounds.x is being capped once the character image starts moving beyond the bounds of the panel, use Location instead which should provide unmodified info
							int diffX = targetX - defenderController.GetImagePanel().getLocation().x;
							defenderController.GetImagePanel().setBounds(targetX, defenderController.GetImagePanel().getLocation().y,
									defenderController.GetImagePanel().getSize().width, defenderController.GetImagePanel().getSize().height);
							
							//Weapon Movement Shit
							ImagePanel weaponPanel = defenderController.GetWeaponImagePanel();
							weaponPanel.setBounds(weaponPanel.getLocation().x + diffX, weaponPanel.getLocation().y,
									weaponPanel.getSize().width, weaponPanel.getSize().height);
						}
					} else {
						//Apparently if the character isn't moving it doesn't update itself during non-moving Anims
						defenderController.GetImagePanel().repaint();
					}
				}
				if(defendersEffectNodeList != null) {
					for(int i = 0; i < defendersEffectNodeList.size(); i++) {
						EffectNode effectNode = defendersEffectNodeList.get(i);
						if(effectNode.getEffectClip() == null)
							continue;
						int effectXMovement = effectNode.getEffectClip().xMovement;
						if(effectXMovement != 0) {
							float normProgress = (float)attackersIntervalsPast / effectNode.getEffectClip().duration_combatIntervals;
							
							
							//int currentX = effectNode.getStartLocation().x - Math.round(effectXMovement * normProgress);
							int currentX = 0;
							if(!isRightCharacterTheAttacker)
								currentX = effectNode.getStartLocation().x - Math.round(effectXMovement * normProgress);
							else
								currentX = effectNode.getStartLocation().x + Math.round(effectXMovement * normProgress);
							
							
							if(defendersParticleFXs[i].GetImagePanel().getBounds().x != currentX)
								defendersParticleFXs[i].GetImagePanel().setBounds(currentX, defendersParticleFXs[i].GetImagePanel().getLocation().y,
										defendersParticleFXs[i].GetImagePanel().getSize().width, defendersParticleFXs[i].GetImagePanel().getSize().height);
						}
					}
				}
				
				
				attackersIntervalsPast++;
				defendersIntervalsPast++;
			}
		});
		
		combatTimer.setInitialDelay(0);
		
		exitAnim = new Timer(timerDelay, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//Hide these as we start the exit anim
				if(feedbackLabel_left.isVisible())
					feedbackLabel_left.setVisible(false);
				if(feedbackLabel_right.isVisible())
					feedbackLabel_right.setVisible(false);
				
				int nextLeftX = Math.max(startFadeEdge_left, fadeOutOverlay_left.getLocation().x - fadeOutEdgeSpeed);
				fadeOutOverlay_left.setBounds(nextLeftX, fadeOutOverlay_left.getLocation().y,
						  					  fadeOutOverlay_left.getSize().width, fadeOutOverlay_left.getSize().height);

				int nextRightX = Math.min(startFadeEdge_right, fadeOutOverlay_right.getLocation().x + fadeOutEdgeSpeed);
				fadeOutOverlay_right.setBounds(nextRightX, fadeOutOverlay_right.getLocation().y,
											   fadeOutOverlay_right.getSize().width, fadeOutOverlay_right.getSize().height);
				
				int nextTerrainY = Math.min(startCarriageHeight, carriage_left.getLocation().y + terrainSpeed);
				int diffY = nextTerrainY - carriage_left.getLocation().y;
				
				//carriage_left.setBounds(dimension.width / 2 - carriageDimension.width + carriageCenterOffset, nextTerrainY, carriageDimension.width, carriageDimension.height);
				//carriage_right.setBounds(dimension.width / 2 - carriageCenterOffset, nextTerrainY, carriageDimension.width, carriageDimension.height);
				//carriage_left.setBounds(dimension.width / 2 - carriageDimension.width + carriageCenterOffset + AttackRangeOffset(false), nextTerrainY, carriageDimension.width, carriageDimension.height);
				//carriage_right.setBounds(dimension.width / 2 - carriageCenterOffset + AttackRangeOffset(true), nextTerrainY, carriageDimension.width, carriageDimension.height);
				carriage_left.setBounds(carriage_left.getLocation().x, nextTerrainY, carriageDimension.width, carriageDimension.height);
				carriage_right.setBounds(carriage_right.getLocation().x, nextTerrainY, carriageDimension.width, carriageDimension.height);
				
				terrainTile_left.setBounds(carriage_left.getLocation().x + terrainCarriageOffset.x, carriage_left.getLocation().y + terrainCarriageOffset.y, terrainSize.width, terrainSize.height);
				terrainTile_right.setBounds(carriage_right.getLocation().x + terrainCarriageOffset.x, carriage_right.getLocation().y + terrainCarriageOffset.y, terrainSize.width, terrainSize.height);
				
				characterController_left.GetWeaponImagePanel().setBounds(
						characterController_left.GetWeaponImagePanel().getLocation().x,
						characterController_left.GetWeaponImagePanel().getLocation().y + diffY,
						characterController_left.GetWeaponImagePanel().getSize().width, characterController_left.GetWeaponImagePanel().getSize().height);
		
				characterController_right.GetWeaponImagePanel().setBounds(
						characterController_right.GetWeaponImagePanel().getLocation().x,
						characterController_right.GetWeaponImagePanel().getLocation().y + diffY,
						characterController_right.GetWeaponImagePanel().getSize().width, characterController_right.GetWeaponImagePanel().getSize().height);
				
				if(nextLeftX == startFadeEdge_left
						&& nextRightX == startFadeEdge_right
						&& nextTerrainY == startCarriageHeight) {
					CompleteExitAnim();
					exitAnim.stop();
				}
			}
		});
		exitAnim.setInitialDelay(1200);
		
		//System.err.println("CombatAnimPane.Constructor() - Re-enable hiding of components.");
		SetComponentsVisible(false);
		
		CreateAnimSequences();
	}
	
	int lerp(int a, int b, float f)
	{
	    return Math.round( (float)a + f * (b - a) );
	}
	
	private List<EffectClip> GetEffectClipsForChar(EffectSocket[] availableEffectSockets, AnimSubset overridingSubset, WeaponType charsWeaponType) {
		List<EffectClip> selectedEffectClips = new ArrayList<EffectClip>();
		for(EffectSocket effectSocket : availableEffectSockets) {
			if(overridingSubset == null) { //Getting the effect for the unarmed shenanigens
				selectedEffectClips.add(effectSocket.filteredEffects.get(0).effectClip);
			} else if(
					
				(overridingSubset.overridingAnimType != null && effectSocket.animType == overridingSubset.overridingAnimType)
				||
				(overridingSubset.overridingAnimType == null && effectSocket.animType == overridingSubset.animType)
			) {
				FilteredEffect[] matchingFilteredEffects = effectSocket.filteredEffects.stream().filter(x -> x.weaponTypeFilters.contains(charsWeaponType)).toArray(FilteredEffect[]::new);
				if(matchingFilteredEffects.length > 1)
					System.err.println("CombatAnimPane.GetEffectClipsForChar() - There were more than one matching FilteredEffects. This means there are overlaped weaponTypes in one or more"
							+ " of the FilterEffect structures. Check for duplicates of WeaponType: " + charsWeaponType);
				else if(matchingFilteredEffects.length == 0)
					System.err.println("CombatAnimPane.GetEffectClipsForChar() - There are no matchingFilteredEffects!");
				else
					selectedEffectClips.add(matchingFilteredEffects[0].effectClip);
			}
		}
		return selectedEffectClips;
	}
	
	
	public class Clip {
		protected Clip(AnimType genericAnimType, int duration_combatIntervals, int xMovement, boolean playAnimAtDefaultSpeed, boolean loopAnim) {
			this.duration_combatIntervals = duration_combatIntervals;
			this.xMovement = xMovement;
			this.playAnimAtDefaultSpeed = playAnimAtDefaultSpeed;
			this.loopAnim = loopAnim;
			
			//this.animFrameIndex = animFrameIndex;
			//this.anim = anim;
			this.genericAnimType = genericAnimType;
			this.socketAddressArray = null;
		}
		protected Clip(int duration_combatIntervals, int xMovement, boolean playAnimAtDefaultSpeed, boolean loopAnim, AnimType genericAnimType, int sheetFrameIndex) {
			this.duration_combatIntervals = duration_combatIntervals;
			this.xMovement = xMovement;
			this.playAnimAtDefaultSpeed = playAnimAtDefaultSpeed;
			this.loopAnim = loopAnim;
			
			//this.animFrameIndex = animFrameIndex;
			//this.anim = anim;
			this.genericAnimType = genericAnimType;
			this.socketAddressArray = new SocketAddress[] { new SocketAddress(genericAnimType, sheetFrameIndex) };
		}
		protected Clip(int duration_combatIntervals, int xMovement, boolean playAnimAtDefaultSpeed, boolean loopAnim, AnimType genericAnimType, SocketAddress[] socketAddressArray) {
			this.duration_combatIntervals = duration_combatIntervals;
			this.xMovement = xMovement;
			this.playAnimAtDefaultSpeed = playAnimAtDefaultSpeed;
			this.loopAnim = loopAnim;
			
			//this.animFrameIndex = -1;
			//this.anim = null;
			this.genericAnimType = genericAnimType;
			this.socketAddressArray = socketAddressArray.clone();
		}
		
		int duration_combatIntervals;
		int xMovement;
		boolean playAnimAtDefaultSpeed;
		boolean loopAnim;
		
		//You'll always have a genericAnimType representing the regular generic animType(Idle, MainAttack, Run, etc). With an array of SocketAddress' that may contain only one Address for a generic address
		//or one generic address followed by a number of specific varient addresses.
		AnimType genericAnimType;
		//These represent a set of unique frames from a variety of anims, this gets referenced directly in the CombatAnimPane scope
		private SocketAddress[] socketAddressArray;
	}
	
	public class Clip_Wait extends Clip {
		public Clip_Wait(int duration_combatIntervals) {
			super(null, duration_combatIntervals, 0, true, true);
		}
	}
	
	public class Clip_Frame extends Clip {
		public Clip_Frame(int duration_combatIntervals, AnimType genericAnimType, int sheetFrameIndex) {
			super(duration_combatIntervals, 0, true, true, genericAnimType, sheetFrameIndex);
		}
		public Clip_Frame(int duration_combatIntervals, AnimType genericAnimType, SocketAddress[] socketAddressArray) {
			super(duration_combatIntervals, 0, true, true, genericAnimType, socketAddressArray);
		}
	}
	
	public class Clip_Anim extends Clip {
		public Clip_Anim(int duration_combatIntervals, AnimType genericAnimType, boolean playAnimAtDefaultSpeed, boolean loopAnim) {
			super(genericAnimType, duration_combatIntervals, 0, playAnimAtDefaultSpeed, loopAnim);
		}
	}
	
	public class Clip_Move extends Clip {
		public Clip_Move(int duration_combatIntervals, int xMovement) {
			super(null, duration_combatIntervals, xMovement, true, true);
		}
	}
	
	public class Clip_MovingFrame extends Clip {
		public Clip_MovingFrame(int duration_combatIntervals, int xMovement, AnimType genericAnimType, int sheetFrameIndex) {
			super(duration_combatIntervals, xMovement, true, true, genericAnimType, sheetFrameIndex);
		}
		public Clip_MovingFrame(int duration_combatIntervals, int xMovement, AnimType genericAnimType, SocketAddress[] socketAddressArray) {
			super(duration_combatIntervals, xMovement, true, true, genericAnimType, socketAddressArray);
		}
		
	}
	
	public class Clip_MovingAnim extends Clip {
		public Clip_MovingAnim(int duration_combatIntervals, int xMovement, AnimType genericAnimType, boolean playAnimAtDefaultSpeed, boolean loopAnim) {
			super(genericAnimType, duration_combatIntervals, xMovement, playAnimAtDefaultSpeed, loopAnim);
		}
	}
	
	/**
	 * An imitation of Clip, with less child classes.
	 * @author Magnus
	 */
	public class EffectClip {
		protected EffectClip(int duration_combatIntervals, int initialDelay_ms, int xMovement, int animFrameIndex, ActorData effectData, AnchorType startingAnchorPosition,
				int animSpeedOverride, boolean loopAnim) {
			this.duration_combatIntervals = duration_combatIntervals;
			this.initialDelay_ms = initialDelay_ms;
			this.xMovement = xMovement;
			this.animFrameIndex = animFrameIndex;
			this.effectData = effectData;
			this.startingAnchorPosition = startingAnchorPosition;
			this.useControllersAnimSpeed = animSpeedOverride > 0;
			this.animSpeedOverride = animSpeedOverride;
			this.loopAnim = loopAnim;
		}
		
		int duration_combatIntervals;
		int initialDelay_ms;
		int xMovement;
		int animFrameIndex;
		ActorData effectData;
		AnchorType startingAnchorPosition;
		boolean useControllersAnimSpeed;
		int animSpeedOverride;
		boolean loopAnim;
		
		//This is not set during construction but at the start of the clip and used throughout an animation involving movement
		Point startLocation;
	}
	
	
	class AnimSequence {
		//Each clip in the sequence will run for this long
		private Clip[] clips;
		/**
		 * The reason for a separate effectClips jagged array is the disjointing of the Clip and EffectClip. In this way, an animSequence index can contain a null Clip AND a non-null EffectClip,
		 * and vice versa. So when preparing to play to a sequence's next clip, both the Clip and EffectClip need to be queried.
		 * The first dimension of the jagged array is for clipIndex and the second dimension is for the multiple effects that could take place during the clip.
		 */
		//private EffectClip[][] effectClips; //This is being refactored
		
		public AnimSequence(int sequenceCount) {
			clips = new Clip[sequenceCount];
			//effectClips = new EffectClip[sequenceCount][maxParticleCount];
			effectSockets = new HashMap<Integer,List<EffectSocket>>();
		}
		
		public AnimSequence(AnimSequence sequence) {
			clips = sequence.clips.clone();
			
			//effectSockets = new HashMap<Integer,List<EffectSocket>>( sequence.effectSockets );
			effectSockets = new HashMap<Integer,List<EffectSocket>>();
			for(Integer key : sequence.effectSockets.keySet()) {
				List<EffectSocket> copiedSockets = new ArrayList<EffectSocket>();
				for(EffectSocket effectSocket : sequence.effectSockets.get(key)) {
					copiedSockets.add(new EffectSocket(effectSocket));
				}
				effectSockets.put(key, copiedSockets);
			}
		}
		
		public void AddClip(int clipIndex, Clip clip) {
			if(clipIndex >= clips.length) {
				Clip[] tempClips = clips.clone();
				clips = new Clip[clipIndex];
				for(int i = 0; i < tempClips.length; i++) {
					clips[i] = tempClips[i];
				}
			}
			clips[clipIndex - 1] = clip;
		}
		
		
		//Record the various motion settings for the Clip slot
		public void AddClip(int clipIndex, Clip clip, EffectSocket[] effectSocketsArray) {
			if(effectSocketsArray == null || effectSocketsArray.length == 0)
				System.err.println("CombatAnimPane.AnimSequence.AddClip() - Parameter called effectClips is null or empty. Use base version of AddClip that doesn't take an EffectClip argument instead.");
			
			AddClip(clipIndex, clip);
			AddEffectSockets(clipIndex, effectSocketsArray);
		}
		
		//Replaces effectClips member variable
		//A Map works rather well for this data structure due to the fact we'll always be refering to a set of EffectSockets by an explicit clipIndex.
		private Map<Integer,List<EffectSocket>> effectSockets;
		
		private void AddEffectSockets(int clipIndex, EffectSocket[] effectSocketsArray) {
			List<EffectSocket> list = (List<EffectSocket>)Arrays.asList(effectSocketsArray);
			
			if(!this.effectSockets.containsKey(clipIndex))
				this.effectSockets.put(clipIndex, list);
			else {
				list.addAll(0, this.effectSockets.get(clipIndex));
				this.effectSockets.put(clipIndex, list);
			}
		}
		
		private void ReplaceEffectClipsAt(int clipIndex, EffectClip replacementEffect) {
			for(EffectSocket effectSocket : effectSockets.get(clipIndex)) {
				for(FilteredEffect filteredEffect : effectSocket.filteredEffects) {
					filteredEffect.effectClip = replacementEffect;
				}
			}
			
			System.err.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ ReplaceEffectClipsAt() - Did update EffectSocket correctly: " 
								+ (effectSockets.get(clipIndex).get(0).filteredEffects.get(0).effectClip.effectData != null));
		}
		
		
		public Clip GetClip(int clipIndex) {
			int targetIndex = clipIndex - 1;
			if(targetIndex < 0 || targetIndex >= clips.length)
				return null;
			else
				return clips[targetIndex];
		}
		
		public EffectSocket[] GetEffectSockets(int clipIndex) {
			if(!this.effectSockets.containsKey(clipIndex))
				return null;
			else
				return effectSockets.get(clipIndex).stream().toArray(EffectSocket[]::new);
		}
	}
	
	private AnimSequence punchSeq;
	private AnimSequence meleeSwingSeq;
	private AnimSequence meleeSpinSeq;
	private AnimSequence rangedBowSeq;
	private AnimSequence rangedShootSeq;
	private AnimSequence rangedThrowSeq;
	
	private AnimSequence castSeq;
	private AnimSequence useItemSeq;
	
	private AnimSequence flinchSeq;
	private AnimSequence dodgeSeq;
	private AnimSequence positiveEffectSeq;
	
	Map<BattleItemType,EffectClip> itemEffects = new HashMap<BattleItemType,EffectClip>();
	
	//record and possible add-on to the sequences we learn about during the call to StartCombatAnim()
	AnimSequence attackersSequence;
	AnimSequence defenderSequence;
	
	
	private enum AnchorType {
		//These are refering to the BattleCharacter causing the effect
		Waist, Feet, Head, FrontHand, FrontShoulder, WeaponSlash, FarSlash, FrontGround, BackHand, FullyCocked,
		//These are refering to the BattleCharacter we're facing, these should be semetrically named so that they can be used synonymously once their "Others_" is trimmed out
		Others_Waist, Others_Head, Others_FrontShoulder, Others_Feet
	};
	private final String AnchorType_othersQualifier = "Others_";
	private Point GetAnchorPoint(boolean leftSide, AnchorType anchorType, Dimension effectSize) {
		//If this AnchorType is referring to the other BattleCharacter we're facing then reference their position instead of our own
		if(anchorType.toString().contains(AnchorType_othersQualifier)) {
			leftSide = !leftSide;
			String semetricalAnchorTypeString = anchorType.toString().split("_")[1];
			anchorType = AnchorType.valueOf(semetricalAnchorTypeString);
		}
		
		//Point point = leftSide ? new Point(carriagePosition_left.x + characterStartPos_left.x, targetCarriageHeight + characterStartPos_left.y)
		//		: new Point(carriagePosition_right.x + characterStartPos_right.x, targetCarriageHeight + characterStartPos_right.y);
		//The above was using Point members that dont update along with the carriages
		Point point = leftSide ? new Point(carriage_left.getLocation().x + characterStartPos_left.x, targetCarriageHeight + characterStartPos_left.y)
				: new Point(carriage_right.getLocation().x + characterStartPos_right.x, targetCarriageHeight + characterStartPos_right.y);
		
		point = new Point(point.x + (characterSize.width/2) - (effectSize.width/2), point.y + (characterSize.height/2) - (effectSize.height/2));
		int faceXFactor = leftSide ? 1 : -1;
		
		Point vector = new Point(0, 0);
		switch(anchorType) {
			case Waist:
				Dimension dim = GUIUtil.GetRelativeSize(0.025f, true);
				vector = new Point(0, dim.height);
				break;
			case Feet:
				dim = GUIUtil.GetRelativeSize(0.045f, true);
				vector = new Point(0, dim.height);
				break;
			case FrontGround:
				dim = GUIUtil.GetRelativeSize(0.045f, true);
				vector = new Point(Math.round(dim.width * 0.5f) * faceXFactor, dim.height);
				break;
			case Head:
				dim = GUIUtil.GetRelativeSize(0.025f, true);
				vector = new Point(0, Math.round(dim.height*0.4f));
				break;
			case FrontHand:
				dim = GUIUtil.GetRelativeSize(0.025f, true);
				vector = new Point(dim.width * faceXFactor, dim.height);
				break;
			case FrontShoulder:
				dim = GUIUtil.GetRelativeSize(0.025f, true);
				vector = new Point(dim.width*2 * faceXFactor, Math.round(dim.height*0.6f));
				break;
			case WeaponSlash:
				dim = GUIUtil.GetRelativeSize(0.025f, true);
				vector = new Point((dim.width/2) * faceXFactor, Math.round(dim.height*1.5f));
				break;
			case FarSlash:
				dim = GUIUtil.GetRelativeSize(0.025f, true);
				vector = new Point(Math.round(dim.width*1.5f) * faceXFactor, Math.round(dim.height*1.5f));
				break;
			case BackHand:
				break;
			case FullyCocked: //(shoulder height above BackHand)
				dim = GUIUtil.GetRelativeSize(0.04f, true);
				vector = new Point(dim.width * -faceXFactor, dim.height/2);
				break;
			default:
				System.err.println("Add support for AnchorType: " + anchorType);
				break;
		}
		return new Point(point.x + vector.x, point.y + vector.y);
	}
	
	ActorData effect_arrow_1;
	ActorData effect_arrow_2;
	ActorData effect_arrow_3;
	
	ActorData effect_chests_1;
	ActorData effect_chests_2;
	ActorData effect_chests_3;
	ActorData effect_chests_4;
	ActorData effect_chests_5;
	ActorData effect_chests_6;
	ActorData effect_chests_7;
	ActorData effect_chests_8;
	
	ActorData effect_claw_bite_1;
	ActorData effect_claw_bite_2;
	ActorData effect_claw_bite_3;
	
	ActorData effect_clock_1;
	ActorData effect_clock_2;
	
	ActorData effect_darkness_1;
	ActorData effect_darkness_2;
	ActorData effect_darkness_3;
	
	ActorData effect_diamond_1;
	ActorData effect_diamond_2;
	ActorData effect_diamond_3;
	
	ActorData effect_dust_1;
	ActorData effect_dust_2;
	ActorData effect_dust_3;
	
	ActorData effect_earth1_1;
	ActorData effect_earth1_2;
	
	ActorData effect_earth2_1;
	ActorData effect_earth2_2;
	
	ActorData effect_explosion_1;
	ActorData effect_explosion_2;
	
	ActorData effect_fire_1;
	ActorData effect_fire_2;
	ActorData effect_fire_3;
	ActorData effect_fire_4;
	
	ActorData effect_fireplace_1;
	
	ActorData effect_heal_1;
	ActorData effect_heal_2;
	ActorData effect_heal_3;
	ActorData effect_heal_4;
	ActorData effect_heal_5;
	
	ActorData effect_holy_1;
	ActorData effect_holy_2;
	ActorData effect_holy_3;
	
	ActorData effect_ice_1;
	ActorData effect_ice_2;
	ActorData effect_ice_3;
	ActorData effect_ice_4;
	
	ActorData effect_impact1_1;
	ActorData effect_impact1_2;
	ActorData effect_impact1_3;
	ActorData effect_impact1_4;
	ActorData effect_impact1_5;
	
	ActorData effect_impact2_1;
	ActorData effect_impact2_2;
	
	ActorData effect_impact3_1;
	ActorData effect_impact3_2;
	ActorData effect_impact3_3;
	
	ActorData effect_lightning_1;
	ActorData effect_lightning_2;
	ActorData effect_lightning_3;
	ActorData effect_lightning_4;
	
	ActorData effect_smoke_1;
	ActorData effect_smoke_2;
	ActorData effect_smoke_3;

	ActorData effect_spikeTrapAnim_1;

	ActorData effect_status_1_1;
	ActorData effect_status_1_2;
	ActorData effect_status_1_3;
	ActorData effect_status_1_4;
	ActorData effect_status_1_5;
	
	ActorData effect_status_2_1;
	ActorData effect_status_2_2;
	ActorData effect_status_2_3;
	ActorData effect_status_2_4;
	ActorData effect_status_2_5;
	
	ActorData effect_torch_1;
	ActorData effect_torch_2;
	ActorData effect_torch_3;
	
	ActorData effect_water_1;
	ActorData effect_water_2;
	ActorData effect_water_3;
	ActorData effect_water_4;
	
	ActorData effect_weapons_1_1;
	ActorData effect_weapons_1_2;
	ActorData effect_weapons_1_3;
	ActorData effect_weapons_1_4;
	ActorData effect_weapons_1_5;
	
	ActorData effect_weapons_2_1;
	ActorData effect_weapons_2_2;
	ActorData effect_weapons_2_3;
	ActorData effect_weapons_2_4;
	ActorData effect_weapons_2_5;
	
	ActorData effect_weapons_3_1;
	ActorData effect_weapons_3_2;
	ActorData effect_weapons_3_3;
	
	ActorData effect_wind_1;
	ActorData effect_wind_2;
	ActorData effect_wind_3;
	ActorData effect_wind_4;
	
	private void CreateEffectData() {
		effect_arrow_1 = new ActorData("effects/arrow.png", 0, 3,  null, 0f);
		effect_arrow_2 = new ActorData("effects/arrow.png", 4, 10,  null, 0f);
		effect_arrow_3 = new ActorData("effects/arrow.png", 11, 18,  null, 0f);
		
		effect_chests_1 = new ActorData("effects/chests.png", 0, 3,  null, 0f);
		effect_chests_2 = new ActorData("effects/chests.png", 4, 7,  null, 0f);
		effect_chests_3 = new ActorData("effects/chests.png", 8, 11,  null, 0f);
		effect_chests_4 = new ActorData("effects/chests.png", 12, 15,  null, 0f);
		effect_chests_5 = new ActorData("effects/chests.png", 16, 19,  null, 0f);
		effect_chests_6 = new ActorData("effects/chests.png", 20, 23,  null, 0f);
		effect_chests_7 = new ActorData("effects/chests.png", 24, 27,  null, 0f);
		effect_chests_8 = new ActorData("effects/chests.png", 28, 31,  null, 0f);
		
		effect_claw_bite_1 = new ActorData("effects/claw_bite.png", 0, 7,  null, 0f);
		effect_claw_bite_2 = new ActorData("effects/claw_bite.png", 8, 15,  null, 0f);
		effect_claw_bite_3 = new ActorData("effects/claw_bite.png", 16, 21,  null, 0f);
		
		effect_clock_1 = new ActorData("effects/clock.png", 0, 9,  null, 0f);
		effect_clock_2 = new ActorData("effects/clock.png", 10, 18,  null, 0f);
		
		effect_darkness_1 = new ActorData("effects/darkness.png", 0, 2,  null, 0f);
		effect_darkness_2 = new ActorData("effects/darkness.png", 3, 12,  null, 0f);
		effect_darkness_3 = new ActorData("effects/darkness.png", 13, 22,  null, 0f);
		
		effect_diamond_1 = new ActorData("effects/diamond.png", 0, 7,  null, 0f);
		effect_diamond_2 = new ActorData("effects/diamond.png", 8, 15,  null, 0f);
		effect_diamond_3 = new ActorData("effects/diamond.png", 16, 23,  null, 0f);
		
		effect_dust_1 = new ActorData("effects/dust.png", 0, 3,  null, 0f);
		effect_dust_2 = new ActorData("effects/dust.png", 4, 7,  null, 0f);
		effect_dust_3 = new ActorData("effects/dust.png", 8, 11,  null, 0f);
	
		effect_earth1_1 = new ActorData("effects/earth1.png", 0, 11,  null, 0f);
		effect_earth1_2 = new ActorData("effects/earth1.png", 12, 26,  null, 0f);
		
		effect_earth2_1 = new ActorData("effects/earth2.png", 0, 13,  null, 0f);
		effect_earth2_2 = new ActorData("effects/earth2.png", 14, 19,  null, 0f);
		
		effect_explosion_1 = new ActorData("effects/explosion.png", 0, 9,  null, 0f);
		effect_explosion_2 = new ActorData("effects/explosion.png", 10, 17,  null, 0f);
		
		effect_fire_1 = new ActorData("effects/fire.png", 0, 3,  null, 0f);
		effect_fire_2 = new ActorData("effects/fire.png", 4, 9,  null, 0f);
		effect_fire_3 = new ActorData("effects/fire.png", 10, 15,  null, 0f);
		effect_fire_4 = new ActorData("effects/fire.png", 16, 26,  null, 0f);
		
		effect_fireplace_1 = new ActorData("effects/fireplace.png", 0, 3,  null, 0f);
		
		effect_heal_1 = new ActorData("effects/heal.png", 0, 8,  null, 0f);
		effect_heal_2 = new ActorData("effects/heal.png", 9, 21,  null, 0f);
		effect_heal_3 = new ActorData("effects/heal.png", 22, 33,  null, 0f);
		effect_heal_4 = new ActorData("effects/heal.png", 34, 39,  null, 0f);
		effect_heal_5 = new ActorData("effects/heal.png", 40, 49,  null, 0f);
		
		effect_holy_1 = new ActorData("effects/holy.png", 0, 8,  null, 0f);
		effect_holy_2 = new ActorData("effects/holy.png", 9, 14,  null, 0f);
		effect_holy_3 = new ActorData("effects/holy.png", 15, 22,  null, 0f);
		
		effect_ice_1 = new ActorData("effects/ice.png", 0, 10,  null, 0f);
		effect_ice_2 = new ActorData("effects/ice.png", 11, 23,  null, 0f);
		effect_ice_3 = new ActorData("effects/ice.png", 24, 28,  null, 0f);
		effect_ice_4 = new ActorData("effects/ice.png", 29, 36,  null, 0f);
		
		effect_impact1_1 = new ActorData("effects/impact1.png", 0, 5,  null, 0f);
		effect_impact1_2 = new ActorData("effects/impact1.png", 6, 10,  null, 0f);
		effect_impact1_3 = new ActorData("effects/impact1.png", 11, 16,  null, 0f);
		effect_impact1_4 = new ActorData("effects/impact1.png", 17, 21,  null, 0f);
		effect_impact1_5 = new ActorData("effects/impact1.png", 22, 27,  null, 0f);
		
		effect_impact2_1 = new ActorData("effects/impact2.png", 0, 3,  null, 0f);
		effect_impact2_2 = new ActorData("effects/impact2.png", 4, 11,  null, 0f);
		
		effect_impact3_1 = new ActorData("effects/impact3.png",  0, 5,  null, 0f);
		effect_impact3_2 = new ActorData("effects/impact3.png",  6, 10,  null, 0f);
		effect_impact3_3 = new ActorData("effects/impact3.png",  11, 16,  null, 0f);
		
		effect_lightning_1 = new ActorData("effects/lightning.png",  0, 5,  null, 0f);
		effect_lightning_2 = new ActorData("effects/lightning.png",  6, 11,  null, 0f);
		effect_lightning_3 = new ActorData("effects/lightning.png",  12, 19,  null, 0f);
		effect_lightning_4 = new ActorData("effects/lightning.png",  20, 27,  null, 0f);
		
		effect_smoke_1 = new ActorData("effects/smoke.png",  0, 5,  null, 0f);
		effect_smoke_2 = new ActorData("effects/smoke.png",  6, 9,  null, 0f);
		effect_smoke_3 = new ActorData("effects/smoke.png",  10, 15,  null, 0f);
		
		effect_spikeTrapAnim_1 = new ActorData("effects/spikeTrapAnim.png",  0, 12, null, 0f);
		
		effect_status_1_1 = new ActorData("effects/status_1.png",  0, 7, null, 0f);
		effect_status_1_2 = new ActorData("effects/status_1.png",  8, 13, null, 0f);
		effect_status_1_3 = new ActorData("effects/status_1.png",  14, 19, null, 0f);
		effect_status_1_4 = new ActorData("effects/status_1.png",  20, 23, null, 0f);
		effect_status_1_5 = new ActorData("effects/status_1.png",  24, 29, null, 0f);
		
		effect_status_2_1 = new ActorData("effects/status_2.png",  0, 7, null, 0f);
		effect_status_2_2 = new ActorData("effects/status_2.png",  8, 15, null, 0f);
		effect_status_2_3 = new ActorData("effects/status_2.png",  16, 20, null, 0f);
		effect_status_2_4 = new ActorData("effects/status_2.png",  21, 28, null, 0f);
		effect_status_2_5 = new ActorData("effects/status_2.png",  29, 36, null, 0f);
		
		effect_torch_1 = new ActorData("effects/torch.png",  0, 3, null, 0f);
		effect_torch_2 = new ActorData("effects/torch.png",  4, 7, null, 0f);
		effect_torch_3 = new ActorData("effects/torch.png",  8, 11, null, 0f);
		
		effect_water_1 = new ActorData("effects/water.png",  0, 10, null, 0f);
		effect_water_2 = new ActorData("effects/water.png",  11, 21, null, 0f);
		effect_water_3 = new ActorData("effects/water.png",  22, 33, null, 0f);
		effect_water_4 = new ActorData("effects/water.png",  34, 44, null, 0f);
		
		effect_weapons_1_1 = new ActorData("effects/weapons_1.png",  0, 5,  null, 0f);
		effect_weapons_1_2 = new ActorData("effects/weapons_1.png",  6, 11,  null, 0f);
		effect_weapons_1_3 = new ActorData("effects/weapons_1.png",  12, 17,  null, 0f);
		effect_weapons_1_4 = new ActorData("effects/weapons_1.png",  18, 23,  null, 0f);
		effect_weapons_1_5 = new ActorData("effects/weapons_1.png",  24, 31,  null, 0f);

		effect_weapons_2_1 = new ActorData("effects/weapons_2.png",  0, 5,  null, 0f);
		effect_weapons_2_2 = new ActorData("effects/weapons_2.png",  6, 11,  null, 0f);
		effect_weapons_2_3 = new ActorData("effects/weapons_2.png",  12, 15,  null, 0f);
		effect_weapons_2_4 = new ActorData("effects/weapons_2.png",  16, 21,  null, 0f);
		effect_weapons_2_5 = new ActorData("effects/weapons_2.png",  22, 27,  null, 0f);
		
		effect_weapons_3_1 = new ActorData("effects/weapons_3.png",  0, 5,  null, 0f);
		effect_weapons_3_2 = new ActorData("effects/weapons_3.png",  6, 10,  null, 0f);
		effect_weapons_3_3 = new ActorData("effects/weapons_3.png",  11, 12,  null, 0f);
		
		effect_wind_1 = new ActorData("effects/wind.png", 0, 3,  null, 0f);
		effect_wind_2 = new ActorData("effects/wind.png", 4, 9,  null, 0f);
		effect_wind_3 = new ActorData("effects/wind.png", 10, 18,  null, 0f);
		effect_wind_4 = new ActorData("effects/wind.png", 19, 24,  null, 0f);
	}
	
	/**
	 * Used to refer to a single frame to use in BattleCharacterController.SetStillFrame(). Clips may take an array of SocketAddress', instead of one set of AnimType and frame index, to support
	 * all varient handsocket/weaponsocket configurations.
	 * @author Magnus
	 *
	 */
	public class SocketAddress {
		public SocketAddress(AnimType animType, int sheetFrameIndex) {
			this.animType = animType;
			this.sheetFrameIndex = sheetFrameIndex;
		}
		
		//For weapons that can participate in multiple AnimSequences
		/*public SocketAddress(WeaponType[] versitileWeaponTypes, AnimType animType, int sheetFrameIndex) {
			this(animType, sheetFrameIndex);
			
			if(versitileWeaponTypes == null)
				System.err.println("CombatAnimPane.SocketAddress.Contructor - versitileWeaponTypes parameter can't ne null!");
			this.versitileWeaponTypes = (List<WeaponType>)Arrays.asList(versitileWeaponTypes);
		}*/
		//This approach doesn't work
		
		AnimType animType;
		int sheetFrameIndex;
		
		//Use this to grab different "still frame" sockets
		//List<WeaponType> versitileWeaponTypes;
		//doesn't work
	}
	
	public class EffectSocket {
		public EffectSocket(AnimType animType, EffectClip effectClip) {
			this.animType = animType;
			//this.effectClip = effectClip;
			//When we call this Constructor we know that we only have one effect to use across all WeaponTypes
			WeaponType[] allWeaponTypesIncludingNull = new WeaponType[WeaponType.values().length + 1];
			for(int i = 0; i < allWeaponTypesIncludingNull.length; i++) {
				if(i < allWeaponTypesIncludingNull.length - 1)
					allWeaponTypesIncludingNull[i] = WeaponType.values()[i];
				else
					allWeaponTypesIncludingNull[i] = null;
			}
			this.filteredEffects = new ArrayList<FilteredEffect>();
			this.filteredEffects.add(new FilteredEffect(effectClip, allWeaponTypesIncludingNull));
		}
		public EffectSocket(AnimType animType, FilteredEffect[] filteredEffects) {
			this.animType = animType;
			//this.effectClip = effectClip;
			this.filteredEffects = new ArrayList<FilteredEffect>();
			this.filteredEffects = (List<FilteredEffect>)Arrays.asList(filteredEffects);
		}
		/**
		 * Make a unique copy of the EffectSocket so that the original isn't overwritten.
		 * @param animType
		 * @param filteredEffects
		 */
		public EffectSocket(EffectSocket effectSocket) {
			this.animType = effectSocket.animType;
			this.filteredEffects = new ArrayList<FilteredEffect>();
			for(FilteredEffect filteredEffect : effectSocket.filteredEffects ) {
				this.filteredEffects.add(new FilteredEffect(filteredEffect));
			}
		}
		
		public AnimType animType;
		//public EffectClip effectClip;
		public List<FilteredEffect> filteredEffects;
	}
	
	public class FilteredEffect {
		public FilteredEffect(EffectClip effectClip, WeaponType[] weaponTypeFilters) {
			this.effectClip = effectClip;
			this.weaponTypeFilters = (List<WeaponType>)Arrays.asList(weaponTypeFilters);
		}
		public FilteredEffect(FilteredEffect filteredEffect) {
			this.effectClip = filteredEffect.effectClip;
			this.weaponTypeFilters = new ArrayList<WeaponType>(filteredEffect.weaponTypeFilters);
		}
		
		public EffectClip effectClip;
		//This is a filter for effects NOT being overridden by overridingAnimTypes. It'll spice up generic anims by using different effects depending on equipped weaponType.
		public List<WeaponType> weaponTypeFilters;
	}
	
	private void CreateAnimSequences() {
		//General Values used by various EffectClips
		int g_dur = 24;
		int g_delay = 18;
		int g_xMov = 240;
		int g_spOvr = 0;
		
		
		meleeSwingSeq = new AnimSequence(5);
		
		meleeSwingSeq.AddClip(1, new Clip_Move(4, -40));
		
		meleeSwingSeq.AddClip(2, new Clip_MovingFrame(4, -40, AnimType.MainAttack, new SocketAddress[] {
				new SocketAddress(AnimType.MainAttack, 3) //,
				//Varient SocketAddresses
		}));
		
		meleeSwingSeq.AddClip(3, new Clip_Wait(30));
		
		meleeSwingSeq.AddClip(4, new Clip_MovingAnim(24, 150, AnimType.MainAttack, false, false), new EffectSocket[] {
			//Effects for generic melee attacks
			new EffectSocket(AnimType.MainAttack, new FilteredEffect[] {
			//Melee attacks for Close-range Weapons
			new FilteredEffect(new EffectClip(g_dur, g_delay, g_xMov, -1, effect_weapons_1_1, AnchorType.WeaponSlash, g_spOvr, false), new WeaponType[] {
					WeaponType.Tanto }),
			new FilteredEffect(new EffectClip(g_dur, g_delay, g_xMov, -1, effect_weapons_1_2, AnchorType.FarSlash, g_spOvr, false), new WeaponType[] {
					WeaponType.Jitte }),
			new FilteredEffect(new EffectClip(g_dur, g_delay, g_xMov, -1, effect_weapons_1_3, AnchorType.WeaponSlash, g_spOvr, false), new WeaponType[] {
					WeaponType.Kusarigama }),
			new FilteredEffect(new EffectClip(18, 6, 150, -1, effect_weapons_1_4, AnchorType.Waist, g_spOvr, false), new WeaponType[] {
					WeaponType.Katana, WeaponType.Kodachi, WeaponType.Ninjato }),
			new FilteredEffect(new EffectClip(g_dur, g_delay, g_xMov, -1, effect_weapons_2_1, AnchorType.WeaponSlash, g_spOvr, false), new WeaponType[] {
					WeaponType.WoodenSword, WeaponType.Club }),
			new FilteredEffect(new EffectClip(g_dur, g_delay, g_xMov, -1, effect_weapons_2_2, AnchorType.FarSlash, g_spOvr, false), new WeaponType[] {
					WeaponType.Fan, WeaponType.Stave }),
			new FilteredEffect(new EffectClip(g_dur, g_delay, g_xMov, -1, effect_weapons_2_4, AnchorType.FarSlash, g_spOvr, false), new WeaponType[] {
					WeaponType.Spear }),
			new FilteredEffect(new EffectClip(g_dur, g_delay, g_xMov, -1, effect_weapons_3_1, AnchorType.FarSlash, g_spOvr, false), new WeaponType[] {
					WeaponType.BranchSword }),
			new FilteredEffect(new EffectClip(g_dur, g_delay, g_xMov, -1, effect_weapons_3_2, AnchorType.FarSlash, g_spOvr, false), new WeaponType[] {
					WeaponType.Naginata }),
			
			//Melee attacks for Versitile Weapons
			new FilteredEffect(new EffectClip(g_dur, g_delay, g_xMov, -1, effect_weapons_2_3, AnchorType.FarSlash, g_spOvr, false), new WeaponType[] {
					WeaponType.Talisman }),
			new FilteredEffect(new EffectClip(g_dur, g_delay, g_xMov, -1, effect_weapons_1_1, AnchorType.FarSlash, g_spOvr, false), new WeaponType[] {
					WeaponType.ThrowingKnife, WeaponType.Kunai })
			}) //,
			//Varient EffectSockets
		});
		
		
		meleeSwingSeq.AddClip(5, new Clip_Wait(10));
		
		//Melee Swing Sequence - End
		
		//Melee Spin Sequence - Start
		
		meleeSpinSeq = new AnimSequence(5);
		
		meleeSpinSeq.AddClip(1, new Clip_Move(4, -40));
		
		meleeSpinSeq.AddClip(2, new Clip_MovingFrame(4, -40, AnimType.MainAttack, new SocketAddress[] {
																							new SocketAddress(AnimType.MainAttack_Vr1, 15)
		}));
		
		meleeSpinSeq.AddClip(3, new Clip_Wait(30));
		
		meleeSpinSeq.AddClip(4, new Clip_MovingAnim(36, 150, AnimType.MainAttack, false, false), new EffectSocket[] {
				//Dai Katana varient melee attack
				new EffectSocket(AnimType.MainAttack_Vr1,
						new EffectClip(30, 6, 150, -1, effect_weapons_1_5, AnchorType.Waist, 0, false)),
		});
		
		meleeSpinSeq.AddClip(5, new Clip_Wait(10));
		
		//Melee Spin Sequence - End
		
		//Ranged(r) attack standard values
		int r_dur = 12;
		
		//Ranged Bow Sequence - Start
		
		rangedBowSeq = new AnimSequence(5);
		
		rangedBowSeq.AddClip(1, new Clip_Move(4, -40));

		rangedBowSeq.AddClip(2, new Clip_MovingFrame(4, -40, AnimType.MainAttack, new SocketAddress[] {
																							new SocketAddress(AnimType.Bow, 21)
		}));
		
		rangedBowSeq.AddClip(3, new Clip_Wait(30));
		
		rangedBowSeq.AddClip(4, new Clip_MovingAnim(12, -10, AnimType.MainAttack, false, false), new EffectSocket[] {
				//Bow ranged attack
				new EffectSocket(AnimType.Bow,
						new EffectClip(r_dur, 0, 250, -1, effect_weapons_2_5, AnchorType.FrontShoulder, 0, false)),
		});
		
		rangedBowSeq.AddClip(5, new Clip_Wait(10));
		
		//Ranged Bow Sequence - End

		
		//Ranged Shoot Sequence - Start
		
		rangedShootSeq = new AnimSequence(5);
		
		rangedShootSeq.AddClip(1, new Clip_Move(4, -40));
		
		rangedShootSeq.AddClip(2, new Clip_MovingFrame(4, -40, AnimType.MainAttack, new SocketAddress[] {
																							new SocketAddress(AnimType.Shoot, 8)
		}));
		
		rangedShootSeq.AddClip(3, new Clip_Wait(30));
		
		rangedShootSeq.AddClip(4, new Clip_MovingAnim(30, -30, AnimType.MainAttack, false, false), new EffectSocket[] {
				//Gun ranged attack
				new EffectSocket(AnimType.Shoot,
						new EffectClip(r_dur, 24, 250, -1, effect_weapons_2_4, AnchorType.FarSlash, 0, false)),
		});
		
		rangedShootSeq.AddClip(5, new Clip_Wait(10));
		
		//Ranged Shoot Sequence - End
		
		
		//Ranged Throw Sequence - Start
		
		rangedThrowSeq = new AnimSequence(5);
		
		rangedThrowSeq.AddClip(1, new Clip_Move(4, -40));
		
		rangedThrowSeq.AddClip(2, new Clip_MovingFrame(4, -40, AnimType.MainAttack, new SocketAddress[] {
																							new SocketAddress(AnimType.Throw, 23)
		}));
		
		rangedThrowSeq.AddClip(3, new Clip_Wait(30));
		
		rangedThrowSeq.AddClip(4, new Clip_MovingAnim(12, 40, AnimType.MainAttack, false, false), new EffectSocket[] {
				//Throwing weapon long-range attacks
				new EffectSocket(AnimType.Throw, new FilteredEffect[] {
					//Versitile ranged attacks
					new FilteredEffect(new EffectClip(r_dur, 0, 250, -1, effect_weapons_3_2, AnchorType.FrontShoulder, 0, false), new WeaponType[] {
							 WeaponType.Kusarigama }),
					new FilteredEffect(new EffectClip(r_dur, 0, 250, -1, effect_weapons_2_5, AnchorType.FrontShoulder, 0, false), new WeaponType[] {
							 WeaponType.ThrowingKnife, WeaponType.Kunai }),
					new FilteredEffect(new EffectClip(r_dur, 0, 250, -1, effect_weapons_2_3, AnchorType.FrontShoulder, 0, false), new WeaponType[] {
							 WeaponType.Talisman }),
					
					//Ranged Only attacks
					new FilteredEffect(new EffectClip(r_dur, 0, 250, -1, effect_weapons_3_3, AnchorType.FrontShoulder, 0, false), new WeaponType[] {
							 WeaponType.Shuriken }),
				})
		});
		
		//rangedThrowSeq.AddClip(5, new Clip_Wait(10));
		//We need the Atack to end so the projectile disappears
		rangedThrowSeq.AddClip(5, new Clip_Anim(10, AnimType.Idle, true, true));
		
		//Ranged Throw Sequence - End
		
		//Punch Sequence - Start
		
		punchSeq = new AnimSequence(5);
		
		punchSeq.AddClip(1, new Clip_Move(4, -40));
		
		punchSeq.AddClip(2, new Clip_MovingFrame(4, -40, AnimType.Punch, new SocketAddress[] {
																							new SocketAddress(AnimType.Punch, 8)
		}));
		
		punchSeq.AddClip(3, new Clip_Wait(30));
		
		punchSeq.AddClip(4, new Clip_MovingAnim(24, 150, AnimType.Punch, false, false), new EffectSocket[] {
				new EffectSocket(AnimType.Punch,
						new EffectClip(16, 8, 60, -1, effect_impact1_4, AnchorType.FrontShoulder, 0, false)),
		});
		
		punchSeq.AddClip(5, new Clip_Wait(10));
		
		//Punch Sequence - End
		
		//Cast Sequence - Start
		
		castSeq = new AnimSequence(5);
		
		castSeq.AddClip(1, new Clip_Move(4, -5));
		
		/*castSeq.AddClip(2, new Clip_MovingFrame(4, -5, AnimType.Cast, new SocketAddress[] {
																							new SocketAddress(AnimType.Cast, 15)
		}));
		castSeq.AddClip(3, new Clip_Wait(30));
		castSeq.AddClip(4, new Clip_Anim(24, AnimType.Cast, false, false), new EffectSocket[] {
				new EffectSocket(AnimType.Cast,
						new EffectClip(0, 0, 0, -1, null, null, 0, false)), //This is a placeholder for the effect from AbilityAction
		});
		punchSeq.AddClip(5, new Clip_Wait(10));*/
		castSeq.AddClip(2, new Clip_Anim(4, AnimType.Idle, false, false));
		castSeq.AddClip(3, new Clip_Anim(30, AnimType.Cast, false, false));
		castSeq.AddClip(4, new Clip_Wait(24), new EffectSocket[] {
				new EffectSocket(AnimType.Cast,
						new EffectClip(0, 0, 0, -1, null, null, 0, false)), //This is a placeholder for the effect from AbilityAction
		});
		castSeq.AddClip(5, new Clip_Anim(10, AnimType.Idle, true, true));
		
		//Cast Sequence - End
		
		//UseItem Sequence - Start
		
		useItemSeq = new AnimSequence(5);
		
		useItemSeq.AddClip(1, new Clip_Move(4, -5));
		
		useItemSeq.AddClip(2, new Clip_MovingFrame(4, -5, AnimType.UseItem, new SocketAddress[] {
				new SocketAddress(AnimType.UseItem, 48)
		}));
		
		useItemSeq.AddClip(3, new Clip_Wait(30));
		
		useItemSeq.AddClip(4, new Clip_Anim(24, AnimType.UseItem, false, false), new EffectSocket[] {
				new EffectSocket(AnimType.UseItem,
						new EffectClip(0, 0, 0, -1, null, null, 0, false)), //This is a placeholder for the effect that'll be inferred from BattleItemType
		});
		
		useItemSeq.AddClip(5, new Clip_Wait(10));
		
		//UseItem Sequence - End
		
		//Flinch - Start
		
		flinchSeq = new AnimSequence(5);

		flinchSeq.AddClip(3, new Clip_Wait(38)); //38));
		
		flinchSeq.AddClip(4, new Clip_MovingAnim(10, -60, AnimType.Flinch, true, false), new EffectSocket[] {
				new EffectSocket(AnimType.Flinch, new EffectClip(g_dur, 0, -30, -1, effect_impact3_1, AnchorType.Waist, 0, false))
		});
		
		flinchSeq.AddClip(5, new Clip_Anim(8, AnimType.Idle, true, true));
		
		//Flinch - End
		
		//Dodge - Start
		
		dodgeSeq = new AnimSequence(5);
		
		/*dodgeSeq.AddClip(4, new Clip_Wait(1));
		
		dodgeSeq.AddClip(5, new Clip_MovingAnim(4, -80, AnimType.Crouch, true, false), new EffectSocket[] {
				new EffectSocket(AnimType.Crouch, new EffectClip(4, 0, -10, -1, effect_dust_1, AnchorType.Feet, 0, false))
		});*/
		
		//MeleeSwingSeq Times
		// 3 - Wait 	- 30
		// 4 - MoveAnim - 24
		// 5 - Wait		- 10
		
		dodgeSeq.AddClip(4, new Clip_MovingAnim(4, -80, AnimType.Crouch, true, false), new EffectSocket[] {
				new EffectSocket(AnimType.Crouch, new EffectClip(24, 0, 30, -1, effect_dust_1, AnchorType.FrontGround, 0, false))
		});
		
		dodgeSeq.AddClip(5, new Clip_Anim(24, AnimType.Crouch, true, false));
		
		//Dodge - End
		
		//Positive Effect - Start
		
		positiveEffectSeq = new AnimSequence(5);
		
		positiveEffectSeq.AddClip(4, new Clip_Anim(4, AnimType.Idle, true, true), new EffectSocket[] {
				new EffectSocket(AnimType.Idle, new EffectClip(24, 0, 30, -1, effect_heal_1, AnchorType.Waist, 0, false))
		});
		
		positiveEffectSeq.AddClip(5, new Clip_Anim(24, AnimType.Idle, true, true));
		
		//Positive Effect - End
		
		//Build AbilityAction Structures - Start
		
		ClassType classType = ClassType.RONIN;
		List<AbilityAction> abilityActions = new ArrayList<AbilityAction>();
		
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_counterattack(), null, null));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_baseDamage(), null, null));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_crippleSlash(), meleeSwingSeq, new EffectClip(18, 6, 150, -1, effect_lightning_2, AnchorType.FarSlash, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_waterWheel(), meleeSwingSeq, new EffectClip(18, 6, 150, -1, effect_water_3, AnchorType.FarSlash, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_trainingBoulder(), castSeq, new EffectClip(24, 0, 0, -1, effect_earth1_1, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_hunkerDown(), castSeq, new EffectClip(18, 6, 150, -1, effect_diamond_2, AnchorType.Head, g_spOvr, false)));
		
		//DEBUGGING - All other classes' abilities will be applied to Ronin for testing
		System.err.println("DEBUGGING @ CombatAnimPane.CreateAnimSequence() - All abilities belong to the Ronin class.");
		
		//classAbilityActionMap.put(classType, new ArrayList<AbilityAction>(abilityActions));
		
		//classType = ClassType.NINJA;
		//abilityActions.clear();
		
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_tilePenaltyRedux(), null, null));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_blind(), castSeq, new EffectClip(24, 0, 0, -1, effect_smoke_2, AnchorType.Others_Head, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_autoDodgeProj(), null, new EffectClip(24, 0, 0, -1, effect_smoke_1, AnchorType.Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_darkness1(), castSeq, new EffectClip(24, 0, 0, -1, effect_darkness_1, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_darkness2(), castSeq, new EffectClip(24, 0, 0, -1, effect_darkness_2, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_water2(), castSeq, new EffectClip(24, 0, 0, -1, effect_water_2, AnchorType.Others_Waist, g_spOvr, false)));
		
		//classAbilityActionMap.put(classType, new ArrayList<AbilityAction>(abilityActions));
		
		//classType = ClassType.BANDIT;
		//abilityActions.clear();
		
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_daze(), rangedThrowSeq, new EffectClip(24, 0, 0, -1, effect_explosion_2, AnchorType.Others_Feet, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_rangedCounter(), null, null));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_chanceToHitBuff(), null, null));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_water1(), castSeq, new EffectClip(24, 0, 0, -1, effect_water_1, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_wind1(), castSeq, new EffectClip(24, 0, 0, -1, effect_wind_1, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_wind3(), castSeq, new EffectClip(24, 0, 0, -1, effect_wind_3, AnchorType.Others_Waist, g_spOvr, false)));
		
		//classAbilityActionMap.put(classType, new ArrayList<AbilityAction>(abilityActions));
		
		//classType = ClassType.MONK;
		//abilityActions.clear();
		
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_silence(), castSeq, new EffectClip(24, 0, 0, -1, effect_impact1_5, AnchorType.Others_Head, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_autoDodgeMelee(), null, new EffectClip(24, 0, 0, -1, effect_smoke_3, AnchorType.Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_statusResistence(), null, null));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_heal2(), castSeq, new EffectClip(24, 0, 0, -1, effect_heal_2, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_holy1(), castSeq, new EffectClip(24, 0, 0, -1, effect_holy_1, AnchorType.Others_Feet, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_holy2(), castSeq, new EffectClip(24, 0, 0, -1, effect_holy_2, AnchorType.Others_Feet, g_spOvr, false)));
		
		//classAbilityActionMap.put(classType, new ArrayList<AbilityAction>(abilityActions));
		
		//classType = ClassType.PRIEST;
		//abilityActions.clear();
		
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_abilityPotency(), null, null));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_heal1(), castSeq, new EffectClip(24, 0, 0, -1, effect_heal_1, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_heal5(), castSeq, new EffectClip(24, 0, 0, -1, effect_heal_5, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_lightning3(), castSeq, new EffectClip(24, 0, 0, -1, effect_lightning_3, AnchorType.Others_Feet, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_ice1(), castSeq, new EffectClip(24, 0, 0, -1, effect_ice_1, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_ice3(), castSeq, new EffectClip(24, 0, 0, -1, effect_ice_3, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_fire3(), castSeq, new EffectClip(24, 0, 0, -1, effect_fire_3, AnchorType.Others_Waist, g_spOvr, false)));
		
		//classAbilityActionMap.put(classType, new ArrayList<AbilityAction>(abilityActions));
		
		//classType = ClassType.KAMI_AR;
		//abilityActions.clear();
		
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_lightning1(), null, new EffectClip(24, 0, 0, -1, effect_lightning_1, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_lightning2(), null, new EffectClip(24, 0, 0, -1, effect_lightning_2, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_lightning3(), null, new EffectClip(24, 0, 0, -1, effect_lightning_3, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_lightning4(), null, new EffectClip(24, 0, 0, -1, effect_lightning_4, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_daze(), null, new EffectClip(24, 0, 0, -1, effect_status_1_5, AnchorType.Others_FrontShoulder, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_chanceToDodge(), null, null));
		
		//classAbilityActionMap.put(classType, new ArrayList<AbilityAction>(abilityActions));
		
		//classType = ClassType.KAMI_ER;
		//abilityActions.clear();
		
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_heal1(), null, new EffectClip(24, 0, 0, -1, effect_heal_1, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_heal2(), null, new EffectClip(24, 0, 0, -1, effect_heal_2, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_holy1(), null, new EffectClip(24, 0, 0, -1, effect_holy_1, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_heal3(), null, new EffectClip(24, 0, 0, -1, effect_heal_3, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_holy2(), null, new EffectClip(24, 0, 0, -1, effect_holy_2, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_heal4(), null, new EffectClip(24, 0, 0, -1, effect_heal_4, AnchorType.Others_Waist, g_spOvr, false)));
		
		//classAbilityActionMap.put(classType, new ArrayList<AbilityAction>(abilityActions));
		
		//classType = ClassType.KAMI_ER;
		//abilityActions.clear();
		
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_darkness1(), null, new EffectClip(24, 0, 0, -1, effect_darkness_1, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_darkness2(), null, new EffectClip(24, 0, 0, -1, effect_darkness_2, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_darkness3(), null, new EffectClip(24, 0, 0, -1, effect_darkness_3, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_blind(), null, new EffectClip(24, 0, 0, -1, effect_smoke_2, AnchorType.Others_Head, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_chanceToSurvive(), null, new EffectClip(24, 0, 0, -1, effect_darkness_3, AnchorType.Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_fear(), null, new EffectClip(24, 0, 0, -1, effect_status_2_4, AnchorType.Others_Head, g_spOvr, false)));
		
		//classAbilityActionMap.put(classType, new ArrayList<AbilityAction>(abilityActions));
		
		//classType = ClassType.KAMI_IN;
		//abilityActions.clear();
		
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_clawBite1(), null, new EffectClip(24, 0, 0, -1, effect_claw_bite_1, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_earth1_1(), null, new EffectClip(24, 0, 0, -1, effect_earth1_1, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_diamond1(), null, new EffectClip(24, 0, 0, -1, effect_diamond_1, AnchorType.Head, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_diamond2(), null, new EffectClip(24, 0, 0, -1, effect_diamond_2, AnchorType.Head, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_earth1_2(), null, new EffectClip(24, 0, 0, -1, effect_earth1_2, AnchorType.Others_Feet, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_diamond3(), null, new EffectClip(24, 0, 0, -1, effect_diamond_3, AnchorType.Head, g_spOvr, false)));
		
		//classAbilityActionMap.put(classType, new ArrayList<AbilityAction>(abilityActions));
		
		//classType = ClassType.KAMI_KA;
		//abilityActions.clear();
		
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_water1(), null, new EffectClip(24, 0, 0, -1, effect_water_1, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_water2(), null, new EffectClip(24, 0, 0, -1, effect_water_2, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_heal2(), null, new EffectClip(24, 0, 0, -1, effect_heal_2, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_water3(), null, new EffectClip(24, 0, 0, -1, effect_water_3, AnchorType.Others_Feet, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_heal3(), null, new EffectClip(24, 0, 0, -1, effect_heal_3, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_water4(), null, new EffectClip(24, 0, 0, -1, effect_water_4, AnchorType.Others_Waist, g_spOvr, false)));
		
		//classAbilityActionMap.put(classType, new ArrayList<AbilityAction>(abilityActions));
		
		//classType = ClassType.KAMI_KO;
		//abilityActions.clear();
		
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_plants1(), null, new EffectClip(24, 0, 0, -1, effect_earth2_1, AnchorType.Others_Feet, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_charm(), null, new EffectClip(24, 0, 0, -1, effect_status_2_1, AnchorType.Others_Head, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_holy1(), null, new EffectClip(24, 0, 0, -1, effect_holy_1, AnchorType.Others_Feet, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_tilePenaltyRedux(), null, null));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_holy3(), null, new EffectClip(24, 0, 0, -1, effect_holy_3, AnchorType.Others_Feet, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_plants2(), null, new EffectClip(24, 0, 0, -1, effect_earth2_2, AnchorType.Others_Feet, g_spOvr, false)));
		
		//classAbilityActionMap.put(classType, new ArrayList<AbilityAction>(abilityActions));
		
		//classType = ClassType.KAMI_KY;
		//abilityActions.clear();
		
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_fire1(), null, new EffectClip(24, 0, 0, -1, effect_fire_1, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_fire2(), null, new EffectClip(24, 0, 0, -1, effect_fire_2, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_explosion1(), null, new EffectClip(24, 0, 0, -1, effect_explosion_1, AnchorType.Others_Feet, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_fire4(), null, new EffectClip(24, 0, 0, -1, effect_fire_4, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_fire3(), null, new EffectClip(24, 0, 0, -1, effect_fire_3, AnchorType.Others_Feet, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_explosion2(), null, new EffectClip(24, 0, 0, -1, effect_explosion_2, AnchorType.Others_Waist, g_spOvr, false)));
		
		//classAbilityActionMap.put(classType, new ArrayList<AbilityAction>(abilityActions));
		
		//classType = ClassType.KAMI_OI;
		//abilityActions.clear();
		
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_clawBite1(), null, new EffectClip(24, 0, 0, -1, effect_claw_bite_1, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_clawBite2(), null, new EffectClip(24, 0, 0, -1, effect_claw_bite_2, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_clawBite3(), null, new EffectClip(24, 0, 0, -1, effect_claw_bite_3, AnchorType.Others_Feet, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_timeSpeedUp(), null, new EffectClip(24, 0, 0, -1, effect_clock_1, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_goad(), null, new EffectClip(24, 0, 0, -1, effect_status_1_4, AnchorType.Others_Feet, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_counterattack(), null, null));
		
		//classAbilityActionMap.put(classType, new ArrayList<AbilityAction>(abilityActions));
		
		//classType = ClassType.KAMI_OK;
		//abilityActions.clear();
		
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_ice1(), null, new EffectClip(24, 0, 0, -1, effect_ice_1, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_wind1(), null, new EffectClip(24, 0, 0, -1, effect_wind_1, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_ice2(), null, new EffectClip(24, 0, 0, -1, effect_ice_2, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_ice3(), null, new EffectClip(24, 0, 0, -1, effect_ice_3, AnchorType.Others_Feet, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_wind3(), null, new EffectClip(24, 0, 0, -1, effect_wind_3, AnchorType.Others_Feet, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_ice4(), null, new EffectClip(24, 0, 0, -1, effect_ice_4, AnchorType.Others_Feet, g_spOvr, false)));
		
		//classAbilityActionMap.put(classType, new ArrayList<AbilityAction>(abilityActions));
		
		//classType = ClassType.KAMI_WA;
		//abilityActions.clear();
	
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_wind1(), null, new EffectClip(24, 0, 0, -1, effect_wind_1, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_wind2(), null, new EffectClip(24, 0, 0, -1, effect_wind_2, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_autoDodgeProj(), null, new EffectClip(24, 0, 0, -1, effect_smoke_1, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_clawBite3(), null, new EffectClip(24, 0, 0, -1, effect_claw_bite_3, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_wind3(), null, new EffectClip(24, 0, 0, -1, effect_wind_3, AnchorType.Others_Feet, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_wind4(), null, new EffectClip(24, 0, 0, -1, effect_wind_4, AnchorType.Others_Waist, g_spOvr, false)));
		
		//classAbilityActionMap.put(classType, new ArrayList<AbilityAction>(abilityActions));
		
		//classType = ClassType.SURF;
		//abilityActions.clear();
		
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_chanceToSurvive(), null, new EffectClip(24, 0, 0, -1, effect_holy_3, AnchorType.Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_increaseItemPotency(), null, null));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_fire1(), null, new EffectClip(24, 0, 0, -1, effect_fire_1, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_barrage(), null, new EffectClip(24, 0, 0, -1, effect_arrow_2, AnchorType.Others_Head, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_diamond1(), null, new EffectClip(24, 0, 0, -1, effect_diamond_1, AnchorType.Head, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_cannonFire(), null, new EffectClip(24, 0, 0, -1, effect_explosion_2, AnchorType.Others_Head, g_spOvr, false)));
		
		//classAbilityActionMap.put(classType, new ArrayList<AbilityAction>(abilityActions));
		
		//classType = ClassType.DIAMYO;
		//abilityActions.clear();
		
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_goad(), null, new EffectClip(24, 0, 0, -1, effect_status_1_4, AnchorType.Others_Head, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_baseArmor(), null, null));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_lightning1(), null, new EffectClip(24, 0, 0, -1, effect_lightning_1, AnchorType.Others_Feet, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_wind4(), null, new EffectClip(24, 0, 0, -1, effect_wind_4, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_commendation(), null, new EffectClip(24, 0, 0, -1, effect_clock_1, AnchorType.Others_Head, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_diamond3(), null, new EffectClip(24, 0, 0, -1, effect_diamond_3, AnchorType.Head, g_spOvr, false)));
		
		//classAbilityActionMap.put(classType, new ArrayList<AbilityAction>(abilityActions));
		
		//classType = ClassType.ONI;
		//abilityActions.clear();
		
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_fear(), null, new EffectClip(24, 0, 0, -1, effect_status_2_4, AnchorType.Others_Head, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_baseHp(), null, null));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_darkness3(), null, new EffectClip(24, 0, 0, -1, effect_darkness_3, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_fire2(), null, new EffectClip(24, 0, 0, -1, effect_fire_2, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_fire4(), null, new EffectClip(24, 0, 0, -1, effect_fire_4, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_earth1_2(), null, new EffectClip(24, 0, 0, -1, effect_earth1_2, AnchorType.Others_Feet, g_spOvr, false)));
		
		//classAbilityActionMap.put(classType, new ArrayList<AbilityAction>(abilityActions));
		
		//classType = ClassType.NEKOMATA;
		//abilityActions.clear();
		
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_charm(), null, new EffectClip(24, 0, 0, -1, effect_status_2_1, AnchorType.Others_Head, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_chanceToDodge(), null, null));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_heal3(), null, new EffectClip(24, 0, 0, -1, effect_heal_3, AnchorType.Others_Waist, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_plants1(), null, new EffectClip(24, 0, 0, -1, effect_earth2_1, AnchorType.Others_Feet, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_plants2(), null, new EffectClip(24, 0, 0, -1, effect_earth2_2, AnchorType.Others_Feet, g_spOvr, false)));
		abilityActions.add(new AbilityAction(AbilityManager.getAbility_clawBite2(), null, new EffectClip(24, 0, 0, -1, effect_claw_bite_2, AnchorType.FarSlash, g_spOvr, false)));
		
		//classAbilityActionMap.put(classType, new ArrayList<AbilityAction>(abilityActions));
		
		//TODO DEBUGGING
		System.err.println("CombatAnimPane.CreateAnimSequences() - Adding every Ability to the RONIN class for unfettered access.");
		classAbilityActionMap.put(ClassType.RONIN, new ArrayList<AbilityAction>(abilityActions));
		
		//Build AbilityAction Structures - End
		
		//Build Item Effects - Start
		
		itemEffects.put(BattleItemType.Accelerant, 	new EffectClip(g_dur, 0, 0, -1, this.effect_fire_1, AnchorType.Others_Head, 0, false));
		itemEffects.put(BattleItemType.Buff, 		new EffectClip(g_dur, 0, 0, -1, this.effect_holy_1, AnchorType.Others_Head, 0, false));
		itemEffects.put(BattleItemType.Cure, 		new EffectClip(g_dur, 0, 0, -1, this.effect_heal_2, AnchorType.Others_Head, 0, false));
		itemEffects.put(BattleItemType.Damage, 		new EffectClip(g_dur, 0, 0, -1, this.effect_explosion_1, AnchorType.Others_Head, 0, false));
		itemEffects.put(BattleItemType.Debuff, 		new EffectClip(g_dur, 0, 0, -1, this.effect_darkness_3, AnchorType.Others_Head, 0, false));
		itemEffects.put(BattleItemType.Potion, 		new EffectClip(g_dur, 0, 0, -1, this.effect_heal_1, AnchorType.Others_Head, 0, false));
		itemEffects.put(BattleItemType.Revive, 		new EffectClip(g_dur, 0, 0, -1, this.effect_holy_3, AnchorType.Others_Head, 0, false));
		itemEffects.put(BattleItemType.SpiritTool, 	new EffectClip(g_dur, 0, 0, -1, this.effect_holy_2, AnchorType.Others_Head, 0, false));
		itemEffects.put(BattleItemType.Status, 		new EffectClip(g_dur, 0, 0, -1, this.effect_status_1_1, AnchorType.Others_Head, 0, false));
		
		//Build Item Effects - End
	}
	
	public List<Ability> DEBUG_GetEveryActiveAbility() {
		List<Ability> everyAbility = new ArrayList<Ability>();
		for(AbilityAction abilityAction : classAbilityActionMap.get(ClassType.RONIN)) {
			if(abilityAction.ability.isActiveAbility)
				everyAbility.add( abilityAction.ability );
		}
		return everyAbility;
	}
	
	public class AbilityAction {
		public AbilityAction(Ability ability, AnimSequence animSeq, EffectClip overridingEffect) {
			this.ability = ability;
			this.animSeq = animSeq;
			this.overridingEffect = overridingEffect;
		}
		
		//For Identification and use in hit/miss, damage/cure, etc
		public Ability ability;
		
		//For use in combat anim
		public AnimSequence animSeq;
		public EffectClip overridingEffect;
	}
	
	public Map<ClassType, List<AbilityAction>> classAbilityActionMap = new HashMap<ClassType, List<AbilityAction>>();
	
	
	private boolean isRangedAttack_attacker;
	private boolean isRangedAttack_defender;
	private HealthModInfo healthModInfo;
	
	/**
	 * Used to know when to play the feedback message for ourselves as the "attacker"(the one preforming the animatiuon instead of the typical situation; the one reacting to it).
	 */
	private boolean isSingleSelfTarget;
	
	public void StartCombatAnim(CharacterBase attacker, TerrainType attackerTerrainType, CharacterBase defender, TerrainType defenderTerrainType, boolean didAttackHit,
								Ability chosenAbility, ItemData usedItem, HealthModInfo healthModInfo,
								boolean isRightCharacterTheAttacker, boolean isLeftCharacterHidden, boolean isRightCharacterHidden,
								boolean isLingeringAnimOrAutoRevive, boolean isSingleSelfTarget
	) {
		isAnimating = true;
		
		isFadingIn = true;
		
		hasFadeFinished = false;
		hasExitAnimFinished = false;
		this.attacker = attacker;
		this.defender = defender;
		this.didAttackHit = didAttackHit;
		//healthModInfo will be null for actions that missed
		this.healthModInfo = healthModInfo;
		if(didAttackHit && healthModInfo == null)
			System.err.println("CombatAnimPane.StartCombatAnim() - The healthModInfo can't be null for an actions that hit! Chosen ability: "
					+ (chosenAbility == null ? "null" : chosenAbility.name)
					+ ", chosen Item: " + (usedItem == null ? "null" : usedItem.getName()));
		
		this.isRightCharacterTheAttacker = isRightCharacterTheAttacker;
		this.isLeftCharacterHidden = isLeftCharacterHidden;
		this.isRightCharacterHidden = isRightCharacterHidden;
		
		//Used to know when to play the feedback message for ourselves as the "attacker"(the one preforming the animatiuon instead of the typical situation; the one reacting to it).
		this.isSingleSelfTarget = isSingleSelfTarget;
		
		boolean isSelfTargeting = attacker == defender;
		
		//Repositional the carriages
		characterController_left.GetImagePanel().setBounds(characterStartPos_left.x, characterStartPos_left.y, characterSize.width, characterSize.height);
		characterController_right.GetImagePanel().setBounds(characterStartPos_right.x, characterStartPos_right.y, characterSize.width, characterSize.height);
		
		//Set tints if the characters have paired actors in the current dialography
		ActorData attackerActorData = Game.Instance().GetBattlePanel().GetPossiblePairedActorDataBy(attacker.GetData().getId());
		ActorData defenderActorData = Game.Instance().GetBattlePanel().GetPossiblePairedActorDataBy(defender.GetData().getId());
		
		ActorData leftActorData = this.isRightCharacterTheAttacker ? defenderActorData : attackerActorData;
		if(leftActorData != null)
			characterController_left.GetImagePanel().SetTint(leftActorData.originalColor.getColor(), ColorBlend.Multiply);
		else
			characterController_left.GetImagePanel().ClearTint();
		
		ActorData rightActorData = this.isRightCharacterTheAttacker ? attackerActorData : defenderActorData;
		if(rightActorData != null)
			characterController_right.GetImagePanel().SetTint(rightActorData.originalColor.getColor(), ColorBlend.Multiply);
		else
			characterController_right.GetImagePanel().ClearTint();
		
		
		WeaponType attackerWeaponType = GetCombatantsWeaponType(attacker);
		//DEBUGGING - Override the WeaponType values to the debug value
		if(CombatAnimPane.DEBUG_WEAPONTYPE() != null)
			attackerWeaponType = CombatAnimPane.DEBUG_WEAPONTYPE();
				
		WeaponType defenderWeaponType = GetCombatantsWeaponType(defender);
		
		isRangedAttack_attacker = Point.distance(attacker.getLocation().getX(), attacker.getLocation().getY(), defender.getLocation().getX(), defender.getLocation().getY()) > 1.0;
		//TODO Revert after DEBUGGING
		//System.err.println("DEBUGGING @ CombatAnimPane.StartCombatAnim() - isRangedAttack is always true");
		//isRangedAttack_attacker = true;
		
		//This will always be false unless theres some kind of counterattack
		isRangedAttack_defender = false;

		
		//Implement abilityAction
		AbilityAction abilityAction = null;
		if(chosenAbility != null) {
			
			//abilityAction = classAbilityActionMap.get(attacker.GetData().getType()).stream().filter(x -> x.ability.name.equals(chosenAbility.name)).findFirst().get();
			//TODO DEBUGGING
			System.err.println("DEBUGGING @ CombatAnimPane.StartCombatAnim() - Using RONIN class as key to classAbilityActionMap instead of the character's actual class."
					+ "This allows us to get every ability.");
			abilityAction = classAbilityActionMap.get(ClassType.RONIN).stream().filter(x -> x.ability.name.equals(chosenAbility.name)).findFirst().get();
			
		} else if(usedItem != null) { //if usedItem
			//nothing needed here yet
		}
		
		//Implement healthModInfo
		boolean isActionNegative = false;
		if(healthModInfo != null) {
			for(int i = 0; i < 2; i++) {
				if(i == 0)
					isActionNegative = !healthModInfo.isHealing && healthModInfo.amount > 0;
				else if(i == 1 && !isActionNegative)
					isActionNegative = healthModInfo.appliedStatuses.size() > 0;
			}
		}
		
		//Set the attacks sequence to the currently used weapon attack
		AnimSequence selectedSeq = null;
		EffectClip overridingEffectClip = null;
		
		//Override with the proper effect according to the self-targeting anim.
		//if(isLeftCharacterHidden || isRightCharacterHidden) { //I think the only time we'll use this condition block is for lingering anims so make that 100% direct and clear
		if(isLingeringAnimOrAutoRevive) {
			
			if(healthModInfo.isRevive) {
				overridingEffectClip = new EffectClip(24, 0, 0, -1, effect_holy_3, AnchorType.Head, 0, false);
			} else {
				if(healthModInfo.isHealing)
					overridingEffectClip = new EffectClip(24, 0, 0, -1, effect_heal_2, AnchorType.Head, 0, false);
				else
					overridingEffectClip = new EffectClip(24, 0, 0, -1, effect_status_1_1, AnchorType.Head, 0, false);
			}
			//The attacker's sequence doesn't matter here cause they'll be hidden, only their sequence's effect matters because it will be shown
			selectedSeq = meleeSwingSeq;
		} else if(abilityAction != null && abilityAction.animSeq != null) {
			if(abilityAction.overridingEffect != null)
				overridingEffectClip = abilityAction.overridingEffect;
			selectedSeq = abilityAction.animSeq;
		} else if(usedItem != null) {
			BattleItemType firstBattleItemType = BattleItemTraits.GetAllBattleItemTypes(usedItem).get(0);
			System.err.println("CombatAnimPane.StartCombatAnim() - Stub - Picking Item's first BattleItemType. The user may need to choose from all BattleItemTypes available to decide how the item is used.");
			overridingEffectClip = itemEffects.get(firstBattleItemType);
			selectedSeq = useItemSeq;
			//selectedSeq = rangedThrowSeq; //I think the throw anim is more suited to item use but unfortunately it isnt supported due to some issue with socket overrides and still frames.
		} else if(attackerWeaponType != null) {
			if(abilityAction != null && abilityAction.overridingEffect != null)
				overridingEffectClip = abilityAction.overridingEffect;
			switch(attackerWeaponType) {
				case DaiKatana:
					selectedSeq = meleeSpinSeq;
					break;
				case Bow:
					selectedSeq = rangedBowSeq;
					break;
				case Gun:
					selectedSeq = rangedShootSeq;
					break;
				case Shuriken:
					selectedSeq = rangedThrowSeq;
					break;
				case Kusarigama: case Talisman: case ThrowingKnife: case Kunai:
					selectedSeq = isRangedAttack_attacker ? rangedThrowSeq : meleeSwingSeq;
					break;
				default:
					selectedSeq = meleeSwingSeq;
					break;
			}
		//Else set the sequence to the generic unarmed attack
		} else {
			if(abilityAction != null && abilityAction.overridingEffect != null)
				overridingEffectClip = abilityAction.overridingEffect;
			selectedSeq = punchSeq;
		}
		
		System.out.println("CombatAnimPane.StartCombatAnim() - overridingEffectClip exists: " + (overridingEffectClip != null) + ", abilityAction exists: " + (abilityAction != null));
		
		//Finish up the attackersSequence logic by duplicating it and possibly overriding some data
		if(overridingEffectClip != null) {
			//Make a duplicate so that our AnimSequence data structures dont get corrupted
			attackersSequence = new AnimSequence( selectedSeq );
			//swap out the 4th clip's effect with the overrideEffect
			attackersSequence.ReplaceEffectClipsAt(4, overridingEffectClip);
		} else {
			attackersSequence = new AnimSequence( selectedSeq );
		}
		
		//Create the defender's sequence based on the hit success and remaining health of the defender and possibly reactive combat abilities
		if(didAttackHit) { //The hit landed
			if(isActionNegative) {
				defenderSequence = new AnimSequence(flinchSeq);
				
				//Modify the flinch for alternate, indicating idle states(CriticallyWounded, Dead)
				if(defender.GetHp() <= 0) {
					//Add dead anim at end
					defenderSequence.AddClip(6, new Clip_Anim(20, AnimType.Dead, true, false));
					defenderSequence.AddClip(7, new Clip_Wait(30));
				} else if(defender.IsHpCritical()) {
					//Add crouch anim at end
					defenderSequence.AddClip(6, new Clip_Anim(20, AnimType.Crouch, true, true));
				}
			} else {
				//Use a sequence suitable for receiving positive combatEffect/s, like an idle with a shimmer particle effect or something
				defenderSequence = new AnimSequence(positiveEffectSeq);
			}
		} else { //They dodged
			defenderSequence = new AnimSequence(dodgeSeq);
		}
		/* Use this later if we add counter attacking or another scenario in which the defender retaliates
		if(CombatAnimPane.DEBUG_WEAPONTYPE() != null)
			defenderWeaponType = CombatAnimPane.DEBUG_WEAPONTYPE();
		switch(defenderWeaponType) {
			case Bow:
				defenderSequence = new AnimSequence( rangedBowSeq );
				break;
			case Gun:
				defenderSequence = new AnimSequence( rangedShootSeq );
				break;
			case Kusarigama: case Shuriken: case Talisman: case ThrowingKnife:
				defenderSequence = new AnimSequence( rangedThrowSeq );
				break;
			default:
				defenderSequence = new AnimSequence( meleeSwingSeq );
				break;
		}*/
		
		//System.out.println("attackerWeaponType: " + attackerWeaponType + ", defenderWeaponType: " + defenderWeaponType);
		
		fadeOutOverlay_left.setVisible(true);
		fadeOutOverlay_right.setVisible(true);
		
		//terrainTile_left.SetNewImage(GUIUtil.GetCombatTile(attackerTerrainType));
		terrainTile_left.SetNewImage(GUIUtil.GetCombatTile(this.isRightCharacterTheAttacker ? defenderTerrainType : attackerTerrainType));
		
		terrainTile_left.setVisible(!this.isLeftCharacterHidden);
		
		int leftXPos = !isSelfTargeting ? (dimension.width / 2 - carriageDimension.width + carriageCenterOffset + AttackRangeOffset(false)) : (dimension.width / 2 - (carriageDimension.width / 2));
		carriage_left.setLocation(leftXPos, carriage_left.getLocation().y);
		carriage_left.setVisible(!this.isLeftCharacterHidden);
		
		//terrainTile_right.SetNewImage(GUIUtil.GetCombatTile(defenderTerrainType));
		terrainTile_right.SetNewImage(GUIUtil.GetCombatTile(this.isRightCharacterTheAttacker ? defenderTerrainType : attackerTerrainType));
		
		terrainTile_right.setVisible(!this.isRightCharacterHidden);
		
		int rightXPos = !isSelfTargeting ? (dimension.width / 2 - carriageCenterOffset + AttackRangeOffset(true)) : (dimension.width / 2 - (carriageDimension.width / 2));
		carriage_right.setLocation(rightXPos, carriage_right.getLocation().y);
		carriage_right.setVisible(!this.isRightCharacterHidden);
		
		terrainTile_left.setBounds(carriage_left.getLocation().x + terrainCarriageOffset.x, carriage_left.getLocation().y + terrainCarriageOffset.y, terrainSize.width, terrainSize.height);
		terrainTile_right.setBounds(carriage_right.getLocation().x + terrainCarriageOffset.x, carriage_right.getLocation().y + terrainCarriageOffset.y, terrainSize.width, terrainSize.height);
		

		CharacterBase leftCharacter = null;
		AnimType leftStartAnim = AnimType.Idle;
		CharacterBase rightCharacter = null;
		AnimType rightStartAnim = AnimType.Idle;
		if(this.isRightCharacterTheAttacker) {
			leftCharacter = defender;
			rightCharacter = attacker;
			if(healthModInfo != null && healthModInfo.isRevive)
				leftStartAnim = AnimType.DeadIdle;
		} else {
			leftCharacter = attacker;
			rightCharacter = defender;
			if(healthModInfo != null && healthModInfo.isRevive)
				rightStartAnim = AnimType.DeadIdle;
		}
		characterController_left.SetNewCharacterClass(leftCharacter, leftStartAnim);
		characterController_left.GetImagePanel().setVisible(!this.isLeftCharacterHidden);
		
		characterController_right.SetNewCharacterClass(rightCharacter, rightStartAnim);
		characterController_right.GetImagePanel().setVisible(!this.isRightCharacterHidden);
		
		enterAnim.start();
		
		//Hide the defenders weapon sprite and keep it from updating during self targeting anims
		characterController_left.SetIsSelfTargeting(this.isLeftCharacterHidden);
		//These calls should get the up-to-date weaponType info from the GetHandSockets... method. Since the WeaponType could have changed from the time the BattleCharacter was created and anim start time.
		characterController_left.PlayAnim(characterController_left.getStartingAnim(), true, false);
		
		characterController_right.SetIsSelfTargeting(this.isRightCharacterHidden);
		characterController_right.PlayAnim(characterController_right.getStartingAnim(), true, false);
		
		lightbox.Fade(true, 1400, timerDelay, alphaInterval, 10, this);
	}
	
	private int AttackRangeOffset(boolean isRightCarriage) {
		//The ranging is determined by the attacker, so this implies that a ranged attacker cannot be countered by a melee attack. Once the range is established it cannot be changed by the defender.
		if(!this.isRangedAttack_attacker)
			return 0;
		else {
			int xOffset = -GUIUtil.GetRelativeSize(0.11f, true).width;
			if(isRightCarriage)
				xOffset *= -1;
			return xOffset;
		}
	}
	
	// <- DEBUGGING HELPERS : WeaponType -
	//Sets the currently equipped WeaponType for all characters regardless of their equipment
	
	/**
	 * Use this to replace the multi-referenced value for the desired test WeaponType. It'll be used for both attacker and defender. This member is being cycled during every attack
	 * from TransitionComplete().
	 */
	private static WeaponType debug_weaponType = null;
	/**
	 * Use this to replace the multi-referenced value for the desired test WeaponType. It'll be used for both attacker and defender.
	 */
	public static WeaponType DEBUG_WEAPONTYPE() { return debug_weaponType; }
	/**
	 * Control the cycling of weaponTypes on each attack. Used to debug all the weapon anims and effects at once.
	 */
	private final boolean debug_cycleWeaponsOnAttack = false;
	private int debug_weaponTypeIndex = debug_indexOfWeaponType();
	
	private int debug_indexOfWeaponType() {
		int index = 0;
		WeaponType[] types = WeaponType.values();
		for(int i = 0; i < types.length; i++) {
			if(debug_weaponType == types[i]) {
				index = i;
				break;
			}
		}
		return index;
	}
	
	// - DEBUGGING HELPERS : WeaponType ->
	
	//DEBUG WITH EITHER ABOVE -OR- BELOW
	
	// <- DEBUGGING HELPERS : Interaction's Manditory Equipment -
		//Sets the currently equipped WeaponType for all characters regardless of their equipment
	
	private static ItemData[] debug_manditoryEquipment = new ItemData[] { Items.getById( ItemDepot.Kusarigama.getId() ) };
	public static ItemData[] DEBUG_manditoryEquipment() { return debug_manditoryEquipment; }
	
	// - DEBUGGING HELPERS : Interaction's Manditory Equipment ->
	
	
	public static WeaponType GetCombatantsWeaponType(CharacterBase charBase) {
		//TESTING
		if(CombatAnimPane.DEBUG_WEAPONTYPE() == null) {
			
			//There is yet no procedure for chosing which weapon to attack with, in the instance that both characters hands are holding a weapon.
			//For now just use the weapon in the characters RightHand
			//TODO Add a more indepth method or choosing or allowing the player to choose which weapon they want to attack with(if they've got two equipped)
			ItemData rightHandItem = null;
			if(charBase.GetData().IsItemExistingAt(EquipmentType.RightHand.getValue()))
				rightHandItem = charBase.GetData().GetAt(EquipmentType.RightHand.getValue());
			WeaponType rightHandWeaponType = null;
			if(rightHandItem != null && rightHandItem.getType() == ItemType.Weapon)
				rightHandWeaponType = rightHandItem.getStats().GetBattleToolTraits().weaponTraits.weaponType;
			
			//System.out.println("CombatAnimPane.GetCombatantsWeaponType() - rightHandWeaponType: " + rightHandWeaponType);
			
			return rightHandWeaponType;
			
		} else {
			System.err.println("DEBUGGING @ CombatAnimPane.GetCombatantsWeaponType() - Returning CombatAnimPane.DEBUG_WEAPONTYPE() cause its non-null.");
			return CombatAnimPane.DEBUG_WEAPONTYPE();
		}
	}
	
	public boolean IsAnimating() {
		return isAnimating;
	}
	
	//Called at the end of the anim
	private void EndCombatAnim() {
		isFadingIn = false;
		
		battlePanel.ToggleActionLabel(null);
		
		exitAnim.start();
		
		lightbox.Fade(false, 1000, timerDelay, alphaInterval, 10, this);
		if(defender.GetHp() <= 0)
			battlePanel.SetDeadState(defender, true);
		else if(battlePanel.GetDeadState(defender))
			battlePanel.SetDeadState(defender, false);
	}
	
	@Override
	public void TransitionComplete() {
		if(isFadingIn) {
			SetComponentsVisible(true);
			combatTimer.start();
			
			//DEBUGGING - Cycle the debug weaponType during each attack
			if(debug_cycleWeaponsOnAttack) {
				debug_weaponTypeIndex++;
				if(debug_weaponTypeIndex >= WeaponType.values().length)
					debug_weaponTypeIndex = 0;
				debug_weaponType = WeaponType.values()[debug_weaponTypeIndex];
			}
		} else {
			hasFadeFinished = true;
			if(hasExitAnimFinished) {
				System.out.println("Fade initiating FinalizeTransition");
				FinalizeTransition();
			}
		}
	}
	
	//Wait till both the exit anim and the fade anim are complete before ending the transition
	public void CompleteExitAnim() {
		hasExitAnimFinished = true;
		if(hasFadeFinished) {
			System.out.println("Exit Anim initiating FinalizeTransition");
			FinalizeTransition();
		}
	}
	
	private void FinalizeTransition() {
		isAnimating = false;
		
		SetComponentsVisible(false);
		
		//Do cleanup on BattleCharacterControllers
		characterController_left.OnCombatAnimEnd();
		characterController_right.OnCombatAnimEnd();
		
		//tell battlePanel we're done
		//battlePanel.CombatAnimDone();
		//This flow control is being moved to game since it can now fire multiple sequencial combat anims
		Game.Instance().OnCombatAnimComplete();
	}
	
	private void SetComponentsVisible(boolean visible) {
		//Only hide them when ordered, showing them is a conditional matter handled solely by StartTravelAnim
		if(!visible) {
			terrainTile_left.setVisible(false);
			terrainTile_right.setVisible(false);
		}
		
		characterController_left.GetImagePanel().setVisible(visible);
		characterController_right.GetImagePanel().setVisible(visible);
		
		fadeOutOverlay_left.setVisible(visible);
		fadeOutOverlay_right.setVisible(visible);
	}
}
