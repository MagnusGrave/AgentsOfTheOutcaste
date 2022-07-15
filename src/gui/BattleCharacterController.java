package gui;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.Timer;

import data.AnimCommand;
import data.AnimSocketsPack;
import enums.ClassType;
import enums.WeaponType;
import gameLogic.CharacterBase;
import gui.CombatAnimPane.SocketAddress;
import gui.SpriteSheetUtility.AnimController;
import gui.SpriteSheetUtility.AnimType;
import gui.SpriteSheetUtility.HandSocket;
import gui.SpriteSheetUtility.WeaponSocket;

public class BattleCharacterController {
	private ClassType classType;
	private CharacterBase characterBase;
	/** 
	 * This is a convenience method to grab the classType from the Game's CharacterData instance.
	 * @return
	 */
	private ClassType GetClassType() {
		if(characterBase != null) {
			ClassType classType = characterBase.GetData().getType();
			return classType;
		} else {
			return classType;
		}
	}
	/** 
	 * This is a convenience method to grab the classType from the Game's CharacterData instance.
	 * @return
	 */
	public WeaponType GetWeaponType() {
		if(characterBase != null) {
			WeaponType weaponType = CombatAnimPane.GetCombatantsWeaponType(characterBase);
			return weaponType;
		} else {
			System.err.println("BattleCharacterController.GetWeaponType() - characterBase == null!");
			//return WeaponType.Katana;
			//We no longer need to return a default value, that I'm aware of.
			return null;
		}
	}
	
	
	private AnimController animController;
	
	private ImagePanel imagePanel;
	public ImagePanel GetImagePanel() { return imagePanel; }
	private ImagePanel weaponImagePanel;
	public ImagePanel GetWeaponImagePanel() { return weaponImagePanel; }
	
	public static final int defaultAnimSpeed = 250;
	Timer animTimer;
	BufferedImage[] currentFrames;
	int currentFrameIndex;
	boolean isFacingRight;
	private boolean loopAnim;
	
	/**
	 * Use this to set at the start of a frame or anim and use it throughout the anim
	 */
	private List<HandSocket> currentHandSockets;
	
	/**
	 * Track the permanent state of Visibility as determined by the WeaponSocket.AnimCommands[].
	 * @param commands
	 */
	private boolean commandStatus_isVisible = true;
	/**
	 * Track this so we don't try applying the same image for no reason based on the WeaponSocket.AnimCommands[].
	 * @param commands
	 */
	private String commandStatus_swapImagePath = null;
	
	
	
	//Initialization - Start
	
	/**
	 * This is for constructing BattleCharacters to be used in the CombatAnimPane.
	 * @param charDataId
	 * @param startingAnim
	 * @param startFacingRight
	 */
	//public BattleCharacterController(String charDataId,  AnimType startingAnim, boolean startFacingRight) {
		//this.characterDataID = charDataId;
	public BattleCharacterController(CharacterBase characterBase,  AnimType startingAnim, boolean startFacingRight) {
		this.classType = characterBase.GetData().getType();
		this.characterBase = characterBase;
		
		ClassType classType = GetClassType();
		WeaponType weaponType = GetWeaponType();
		Instantiate(classType, weaponType, startingAnim, startFacingRight);
	}
	
	/**
	 * This is the manually controlled version of the BattleCharacter used by the WorldMapPanel.TravelAnimPane's animation
	 * @param classType
	 * @param weaponType
	 * @param startingAnim
	 * @param startFacingRight
	 */
	public BattleCharacterController(ClassType classType, WeaponType weaponType, AnimType startingAnim, boolean startFacingRight) {
		this.classType = classType;
		Instantiate(classType, weaponType, startingAnim, startFacingRight);
	}
	
	private void Instantiate(ClassType classType, WeaponType weaponType, AnimType startingAnim, boolean startFacingRight) {
		this.startingAnim = startingAnim;
		isFacingRight = startFacingRight;
		animController = SpriteSheetUtility.GetAnimController(classType);
		if(startingAnim != null)
			currentFrames = animController.GetAnimFrames(startingAnim);
		else //Set this as a default so the BattleCharacter's imagePanel can be setup properly
			currentFrames = animController.GetAnimFrames(AnimType.Idle);
		imagePanel = new ImagePanel(currentFrames[0]);
		if(weaponType != null)
			weaponImagePanel = new ImagePanel("effects/" + weaponType.toString().substring(0, 1).toLowerCase() + weaponType.toString().substring(1) + ".png");
		else //If they're fighting bare fisted
			weaponImagePanel = new ImagePanel("effects/bow.png");
		weaponImagePanel.CreateMirrorXImage();
		weaponImagePanel.setVisible(false);
		
		animTimer = new Timer(defaultAnimSpeed, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				BufferedImage frame = currentFrames[currentFrameIndex];
				if(isFacingRight)
					frame = GUIUtil.Mirror(frame);
				imagePanel.SetNewImage(frame);
				
				UpdateWeaponSprite();
				
				currentFrameIndex++;
				if(currentFrameIndex >= currentFrames.length) {
					currentFrameIndex = 0;
					if(!loopAnim)
						animTimer.stop();
				}
			}
		});
		animTimer.setInitialDelay(0);
		
		if(startingAnim != null)
			PlayAnim(startingAnim, true, false);
	}
	
	public void SetNewCharacterClass(CharacterBase characterBase, AnimType startingAnim) {
		this.classType = characterBase.GetData().getType();
		this.characterBase = characterBase;
		
		
		ClassType classType = GetClassType();
		WeaponType weaponType = GetWeaponType();
		
		
		this.startingAnim = startingAnim;
		animController = SpriteSheetUtility.GetAnimController(classType);
		currentFrames = animController.GetAnimFrames(startingAnim);
		if(weaponType != null) {
			weaponImagePanel.SetNewImage("effects/" + weaponType.toString().substring(0, 1).toLowerCase() + weaponType.toString().substring(1) + ".png");
			weaponImagePanel.CreateMirrorXImage();
			weaponImagePanel.setVisible(true);
		} else {
			//If they're fighting bare fisted
			weaponImagePanel.setVisible(false);
		}
		
		System.out.println("BattleCharacterController.SetNewCharacterClass() - isFacingRight: " + isFacingRight);
		
		//Maintain WeaponSocket AnimCommand system
		WipeCommandStatuses();

		PlayAnim(startingAnim, true, false);
	}
	
	//Initialization - End
	
	//Cleanup After CombatAnim - Start
	
	public void OnCombatAnimEnd() {
		animTimer.stop();
	}
	
	//Cleanup After CombatAnim - End
	
	//Character Animation - Start
	
	private AnimType startingAnim;
	public AnimType getStartingAnim() { return startingAnim; }
	private AnimType currentPlayingAnim;
	public AnimType getCurrentPlayingAnim() { return currentPlayingAnim; }
	public boolean doesAnimLoop() { return loopAnim; }
	
	public void SetStillFrame(AnimType genericAnimType, SocketAddress[] socketAddressArray, boolean isRangedAttack) {
		if(animTimer.isRunning())
			animTimer.stop();
		
		
		ClassType classType = GetClassType();
		WeaponType weaponType = GetWeaponType();
		
		
		currentPlayingAnim = genericAnimType;
		loopAnim = false;
		
		//Determine which SocketAddress to use
		AnimSocketsPack animSocketsPack = SpriteSheetUtility.GetHandSocketsForAnim(genericAnimType, weaponType, isRangedAttack);
		
		//This is for unarmed Punch anim
		if(animSocketsPack == null) {
			//Use the first and only element in the socketAddressArray which will know will only ever be our Punch address
			BufferedImage frame = SpriteSheetUtility.GetSpriteByIndex(classType, socketAddressArray[0].sheetFrameIndex);
			if(isFacingRight)
				frame = GUIUtil.Mirror(frame);
			imagePanel.SetNewImage(frame);
			//Clear this so we dont accidentally use it somewhere else
			currentHandSockets = null;
		} else {
		
			SocketAddress selectedSocketAddress = null;
			
			AnimType overrider = animSocketsPack.animSubset.overridingAnimType;
			for(SocketAddress socketAddress : socketAddressArray) {
				//System.out.println("SetStillFrame() - considering socketAddress.animType: " + socketAddress.animType);
				if( (overrider != null && socketAddress.animType == overrider) || (overrider == null && socketAddress.animType == genericAnimType) ) {
					selectedSocketAddress = socketAddress;
					//System.out.println("	Picked SocketAddress with animType: " + socketAddress.animType + ", " + socketAddress.sheetFrameIndex);
					break;
				}
			}
			if(selectedSocketAddress == null)
				//System.err.println("BattleCharacterController.SetStillFrame() - selectedSocketAddress wasnt set from an element in the socketAddressArray! overrider: " + overrider
				//					+ ", genericAnimType: " + genericAnimType);
				//I think this is the appropriate helper message
				System.err.println("BattleCharacterController.SetStillFrame() - selectedSocketAddress wasnt set from an element in the socketAddressArray! overrider: " + overrider
					+ ", genericAnimType: " + genericAnimType + ". Add support for this in the appropriate AnimSequence setup in CombatAnimPane.");
			
			BufferedImage frame = SpriteSheetUtility.GetSpriteByIndex(classType, selectedSocketAddress.sheetFrameIndex);
			if(isFacingRight)
				frame = GUIUtil.Mirror(frame);
			imagePanel.SetNewImage(frame);
			
			//System.out.println("BattleCharacterController.SetStillFrame() - told to play animType: " + genericAnimType + ", received back possibleVarientAnimType: " + selectedSocketAddress.animType);
			
			currentHandSockets = animSocketsPack.frameSockets;
			UpdateWeaponSprite(selectedSocketAddress.sheetFrameIndex, true);
		
		}
	}
	
	public void PlayAnim(AnimType animType, boolean loopAnim, boolean isRangedAttack) {
		//System.out.println("BattleCharacterController.PlayAnim(AnimType animType, boolean loopAnim)");
		
		if(animTimer.isRunning())
			animTimer.stop();
		
		
		WeaponType weaponType = GetWeaponType();
		
		
		currentPlayingAnim = animType;
		
		AnimSocketsPack animSocketsPack = SpriteSheetUtility.GetHandSocketsForAnim(animType, weaponType, isRangedAttack);
		
		
		//if(animSocketsPack == null)
		//		currentFrames = animController.GetAnimFrames(AnimType.Punch);
		//else if(animSocketsPack.animSubset.overridingAnimType == null)
		//	currentFrames = animController.GetAnimFrames(animSocketsPack.animSubset.animType);
		//else
		//	currentFrames = animController.GetAnimFrames(animSocketsPack.animSubset.overridingAnimType);
		//Bug Fix - Adding support for the Walk anim, currently this is only used by the traveling character
		if(animSocketsPack == null) {
			if(animType == AnimType.Walk)
				currentFrames = animController.GetAnimFrames(AnimType.Walk);
			else	
				currentFrames = animController.GetAnimFrames(AnimType.Punch);
		} else if(animSocketsPack.animSubset.overridingAnimType == null)
			currentFrames = animController.GetAnimFrames(animSocketsPack.animSubset.animType);
		else
			currentFrames = animController.GetAnimFrames(animSocketsPack.animSubset.overridingAnimType);
		
		
		this.loopAnim = loopAnim;
		//set timing back to default
		animTimer.setDelay(defaultAnimSpeed);
		
		currentFrameIndex = 0;
		
		if(animSocketsPack != null) {
			currentHandSockets = animSocketsPack.frameSockets;
			UpdateWeaponSprite();
		}
		
		animTimer.restart();
	}
	
	public void PlayAnim(AnimType animType, int duration_milliseconds, boolean isRangedAttack) {
		//System.out.println("BattleCharacterController.PlayAnim(AnimType animType, int duration_milliseconds)");
		
		if(animTimer.isRunning())
			animTimer.stop();
		

		WeaponType weaponType = GetWeaponType();
		
		
		currentPlayingAnim = animType;

		AnimSocketsPack animSocketsPack = SpriteSheetUtility.GetHandSocketsForAnim(animType, weaponType, isRangedAttack);
		
		//if(animSocketsPack == null)
		//	currentFrames = animController.GetAnimFrames(AnimType.Punch);
		//else if(animSocketsPack.animSubset.overridingAnimType == null)
		//	currentFrames = animController.GetAnimFrames(animSocketsPack.animSubset.animType);
		//else
		//	currentFrames = animController.GetAnimFrames(animSocketsPack.animSubset.overridingAnimType);
		//Bug Fix - Adding support for the Walk anim, currently this is only used by the traveling character
		if(animSocketsPack == null) {
			if(animType == AnimType.Walk)
				currentFrames = animController.GetAnimFrames(AnimType.Walk);
			else
				currentFrames = animController.GetAnimFrames(AnimType.Punch);
		} else if(animSocketsPack.animSubset.overridingAnimType == null)
			currentFrames = animController.GetAnimFrames(animSocketsPack.animSubset.animType);
		else
			currentFrames = animController.GetAnimFrames(animSocketsPack.animSubset.overridingAnimType);
		
		
		loopAnim = false;
		//edit timing so that the full anim plays in the duraction of the CombatAnim clip
		animTimer.setDelay(Math.round((float)duration_milliseconds / currentFrames.length));
		
		currentFrameIndex = 0;
		
		if(animSocketsPack != null) {
			currentHandSockets = animSocketsPack.frameSockets;
			UpdateWeaponSprite();
		}
		
		animTimer.restart();
	}
	
	//Character Animation - End
	
	//Weapon Sprite Management - Start	
	
	private void ApplyCommands(AnimCommand[] commands) {
		for(AnimCommand animCommand : commands) {
			switch(animCommand.command) {
				case Show:
					commandStatus_isVisible = true;
					weaponImagePanel.setVisible(true);
					break;
				case Hide:
					commandStatus_isVisible = false;
					weaponImagePanel.setVisible(false);
					break;
				case SwapImage:
					commandStatus_isVisible = true;
					
					boolean isNewImage = animCommand.swapImagePath != commandStatus_swapImagePath;
					commandStatus_swapImagePath = animCommand.swapImagePath;
					
					if(isNewImage) {
						weaponImagePanel.SetNewImage(animCommand.swapImagePath);
						weaponImagePanel.CreateMirrorXImage();	
					}
					weaponImagePanel.setVisible(true);
					break;
				default:
					System.err.println("BattleCharacterController.ApplyCommands() - Add support for CommandType: " + animCommand.command);
					break;
			}
		}
	}
	
	//Hides the weapon and keeps it from updating when self targeting combat anim is playing
	private boolean isSelfTargeting;
	public void SetIsSelfTargeting(boolean isSelfTargeting) {
		this.isSelfTargeting = isSelfTargeting;
		weaponImagePanel.setVisible(false);
	}
	
	/**
	 * Do this everytime we're being set to a new Battle Character
	 */
	private void WipeCommandStatuses() {
		commandStatus_isVisible = true;
		commandStatus_swapImagePath = null;
	}
	
	/**
	 * This overload assumes that we're using the current value of currentFrameIndex. 
	 */
	public void UpdateWeaponSprite() {
		UpdateWeaponSprite(currentFrameIndex, false);
	}
	/**
	 * This overload mehthod is the master and will accept both a sequencial frame index value to be expected from currentFrameIndex(0, 1, 2...etc) or an explicit sheet frame used during SetStillFrame().
	 * @param frameIndex - either the currentFrameIndex or a sheet index.
	 * @param useIndexAsExplicitSheetFrame - Instructions on which handSocket element to use.
	 */
	public void UpdateWeaponSprite(int frameIndex, boolean useIndexAsExplicitSheetFrame) {
		
		//This keeps the defender weapon from showing when supporting self targeting
		if(isSelfTargeting)
			return;

		WeaponType weaponType = GetWeaponType();
		
		
		//System.out.println("BattleCharacterController.UpdateWeaponSprite() - isFacingRight: " + isFacingRight
		//	+ ", weaponType exists: " + (weaponType != null)
		//	+ ", sockets exist: " + (currentHandSockets != null));
		
		//Handles weapon repositioning, mirroring and rotation for each frame, some anims dont have weapon frames
		if(weaponType != null && currentHandSockets != null) {
			HandSocket newSocket = null;
			
			if(useIndexAsExplicitSheetFrame && !currentHandSockets.stream().filter(x -> x.frameIndex == frameIndex).findFirst().isPresent()) {
				System.err.println("Get fucking bent you piece of human garbage, frameIndex: " + frameIndex);
				for(HandSocket socket : currentHandSockets) {
					System.err.println("	Available HandSocket, frameIndex: " + socket.frameIndex);
				}
			}
			
			if(useIndexAsExplicitSheetFrame)
				newSocket = currentHandSockets.stream().filter(x -> x.frameIndex == frameIndex).findFirst().get();	
			else
				newSocket = currentHandSockets.get(frameIndex);
			
			WeaponSocket currentWeaponSocket = SpriteSheetUtility.GetWeaponSocketFrom(newSocket, weaponType);
			
			//OnStart Commands -Do the commands for this WeaponSocket if its got any
			if(currentWeaponSocket.commands_onStart != null) {
				ApplyCommands(currentWeaponSocket.commands_onStart);
			} else if(commandStatus_isVisible) {
				//Normally we wait till the first Update before showing the weapon image, especially ones that've just been set to a new image
				if(!weaponImagePanel.isVisible())
					weaponImagePanel.setVisible(true);
			}
			
			
			//Mirroring - This also needs to happen above Repositioning block because it affects the RotatedOrigin caluclation
			boolean virtualFlipX = currentWeaponSocket.flipX;
			if(isFacingRight)
				virtualFlipX = !virtualFlipX;
			if(virtualFlipX != weaponImagePanel.getXFlipStatus())
				//ALWAYS CALL BEFORE CALLING SetZRotation(...), else if only calling SetXFlipStatus method then do SetXFlipStatus(false, ...)
				weaponImagePanel.SetXFlipStatus(true, virtualFlipX);
			
			
			//Rotation - This should be above the "Reposition" block, i believe. So that the new Rotation can be taken into account for calls to weaponImagePanel.getRotatedOrigin()
			float virtualRotation = currentWeaponSocket.zRot;
			if(isFacingRight)
				virtualRotation = 360f - virtualRotation;
			
			weaponImagePanel.SetZRotation(virtualRotation, currentWeaponSocket.normHandlePos);
			
			
			//Repositioning
			//The right-side character is naturally aligned with the weapon sprite position, the HandSocket.normRelativeHandPos values are based on this(the top left corner of the
			//left-facing charater sprites)
			int naturalHandPosX = Math.round((float)newSocket.normRelativeHandPos.getX() * imagePanel.getWidth());
			int relativeHandPosX = isFacingRight ? (imagePanel.getWidth() - naturalHandPosX) : naturalHandPosX;
			
			Point weaponOffset = null;
			if(weaponImagePanel.getZRotation() == 0f) {
				weaponOffset = new Point(
					Math.round((float)currentWeaponSocket.normHandlePos.getX() * weaponImagePanel.getWidth())
					,
					Math.round((float)currentWeaponSocket.normHandlePos.getY() * weaponImagePanel.getHeight())
				);
				System.out.println("BattleCharacterController.UpdateWeaponSprite() - isFacingRight: " + this.isFacingRight
						+ ", characterBase.name: " + (characterBase != null ? characterBase.GetData().getName() : "NULL")
						+ ", animType: " + this.currentPlayingAnim
						+ ", weapon rot: " + this.weaponImagePanel.getZRotation()
						+ ", weaponType: " + weaponType
						+ ",  [DEFAULT] weaponOffset: " + weaponOffset
				);
			} else {
				weaponOffset = new Point(
					Math.round((float)weaponImagePanel.getRotatedOrigin().getX() * weaponImagePanel.getWidth())
					,
					Math.round((float)weaponImagePanel.getRotatedOrigin().getY() * weaponImagePanel.getHeight())
				);
				System.out.println("BattleCharacterController.UpdateWeaponSprite() - isFacingRight: " + this.isFacingRight
						+ ", characterBase.name: " + (characterBase != null ? characterBase.GetData().getName() : "NULL")
						+ ", animType: " + this.currentPlayingAnim
						+ ", weapon rot: " + this.weaponImagePanel.getZRotation()
						+ ", weaponType: " + weaponType
						+ ", [WITH ROTATION] weaponOffset: " + weaponOffset + ", rotatedOrigin: " + weaponImagePanel.getRotatedOrigin());
			}
			int xValue =
					relativeHandPosX
					-
					weaponOffset.x;
			int yValue =
					Math.round((float)newSocket.normRelativeHandPos.getY() * imagePanel.getHeight())
					-
					weaponOffset.y;
			
			Point weaponPosOnChar = new Point(xValue, yValue);
			
			Point newPosition = new Point(
				imagePanel.getParent().getLocation().x + imagePanel.getLocation().x + weaponPosOnChar.x,
				imagePanel.getParent().getLocation().y + imagePanel.getLocation().y + weaponPosOnChar.y
			);
			
			weaponImagePanel.setBounds(newPosition.x, newPosition.y, weaponImagePanel.getWidth(), weaponImagePanel.getHeight());
		} else {
			//Reset all member variable data pretaining to the weapon since there will be no weapon sprite used during unarmed combat
			
		}
	}
	
	//Weapon Sprite Management - End
	
	//Misc - Start
	
	public void ChangeDirection(boolean faceRight) {
		isFacingRight = faceRight;
	}
	
	//Misc - End
}