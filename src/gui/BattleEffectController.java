package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.Timer;

public class BattleEffectController {
	private ImagePanel imagePanel;
	public ImagePanel GetImagePanel() { return imagePanel; }
	
	//Fallback value if the speed isn't overridden during construction
	public static final int defaultAnimSpeed = 250;
	//May be overridden at construction
	private int animSpeed;
	private Timer animTimer;
	public Timer getAnimTimer() { return animTimer; }
	private BufferedImage[] currentFrames;
	private int currentFrameIndex;
	private boolean isFacingRight;
	private boolean loopAnim;
	//Effect's additional and most likely unnecessary variables
	private Dimension effectSize;
	public Dimension getEffectSize() { return effectSize; }
	private int initialDelay_ms;
	
	
	
	public BattleEffectController(String sheetFilePath, int frameStartIndex, int frameEndIndex, boolean startFacingRight, int initialDelay_ms, int animSpeedOverride, float characterScaleRatio) {
		//Example parameter values for these adapted variables
		//sheetFilePath = "effects/claw_bite.png";
		//frameStartIndex = 0;
		//frameEndIndex = 10;
		//startFacingRight = false;
		//playAtStart = false;
		//animSpeedOverride = -1;
		
		
		isFacingRight = startFacingRight;
		
		System.out.println("BattleEffectController Contructor - About to attempt to get Effect Sheet at: " + sheetFilePath);
		/*SpriteSheet spriteSheet = SpriteSheetUtility.GetEffectSheet(sheetFilePath);
		String[] splits = sheetFilePath.split("/");
		currentFrames = new BufferedImage[frameEndIndex - frameStartIndex + 1];
		for(int i = frameStartIndex; i <= frameEndIndex; i++) {
			String frameName = splits[splits.length-1].split("\\.")[0] + "_" + i;
			currentFrames[i-frameStartIndex] = spriteSheet.GetSprite(frameName);
		}*/
		currentFrames = SpriteSheetUtility.GetRangedEffectSheetFrames(sheetFilePath, frameStartIndex, frameEndIndex);
		
		BufferedImage firstFrame = currentFrames[0];
		effectSize = new Dimension(
			Math.round(firstFrame.getTileWidth() * characterScaleRatio), 
			Math.round(firstFrame.getTileHeight() * characterScaleRatio)
		);
		imagePanel = new ImagePanel(firstFrame);
		imagePanel.setOpaque(false);
		imagePanel.setBackground(new Color(0f,0f,0f,0f));
		imagePanel.setVisible(false);
		
		animSpeed = animSpeedOverride > 0 ? animSpeedOverride : defaultAnimSpeed;
		animTimer = new Timer(animSpeed, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				BufferedImage frame = currentFrames[currentFrameIndex];
				if(isFacingRight)
					frame = GUIUtil.Mirror(frame);
				imagePanel.SetNewImage(frame);
				
				//This is used to keep the original sprite from showing during the period between the call to PlayAnim and the first execution of this ActionListener
				if(!imagePanel.isVisible())
					imagePanel.setVisible(true);
				
				currentFrameIndex++;
				if(currentFrameIndex >= currentFrames.length) {
					currentFrameIndex = 0;
					if(!loopAnim) {
						animTimer.stop();
						imagePanel.setVisible(false);
					}
				}
				
				//This is necessary for Effects that don't move. If they don't move then they won't get repainted for some reason.
				imagePanel.repaint();
			}
		});
		this.initialDelay_ms = Math.max(0, initialDelay_ms);
		animTimer.setInitialDelay(this.initialDelay_ms);
	}
	
	public void SetToNewEffect(String sheetFilePath, int frameStartIndex, int frameEndIndex, boolean startFacingRight, int initialDelay_ms, int animSpeedOverride, float characterScaleRatio) {
		isFacingRight = startFacingRight;
		
		SpriteSheet spriteSheet = SpriteSheetUtility.GetEffectSheet(sheetFilePath);
		String[] splits = sheetFilePath.split("/");
		currentFrames = new BufferedImage[frameEndIndex - frameStartIndex + 1];
		for(int i = frameStartIndex; i <= frameEndIndex; i++) {
			String frameName = splits[splits.length-1].split("\\.")[0] + "_" + i;
			currentFrames[i-frameStartIndex] = spriteSheet.GetSprite(frameName);
		}
		
		effectSize = new Dimension(
			Math.round(currentFrames[0].getTileWidth() * characterScaleRatio), 
			Math.round(currentFrames[0].getTileHeight() * characterScaleRatio)
		);
			
		animSpeed = animSpeedOverride > 0 ? animSpeedOverride : defaultAnimSpeed;
		this.initialDelay_ms = Math.max(0, initialDelay_ms);
		animTimer.setInitialDelay(this.initialDelay_ms);
	}
	
	public void SetStillFrame(int sheetIndex) {
		if(animTimer.isRunning())
			animTimer.stop();
		
		BufferedImage frame = currentFrames[sheetIndex];
		if(isFacingRight)
			frame = GUIUtil.Mirror(frame);
		
		imagePanel.SetNewImage(frame);
		imagePanel.setVisible(true);
	}
	
	/**
	 * The play method. Used for playing an anim at controller's animSpeed or for beginning looping animations.
	 * @param loopAnim
	 */
	public void PlayAnim(boolean loopAnim) {
		System.out.println("BattleEffectController.PlayAnim(AnimType animType, boolean loopAnim)");
		
		if(animTimer.isRunning())
			animTimer.stop();
		
		this.loopAnim = loopAnim;
		//set timing back to default
		animTimer.setDelay(animSpeed);
		
		currentFrameIndex = 0;
		animTimer.restart();
	}
	
	/**
	 * Used to end looping animations. A counterpart of the PlayAnim(boolean loopAnim) method.
	 */
	public void StopAnim() {
		if(animTimer.isRunning())
			animTimer.stop();
		
		imagePanel.setVisible(false);
	}
	
	/**
	 * This method defys the Effect's animSpeed(which was set during contsruction). It will adjust the animSpeed so that the entire animation plays within the duration_milliseconds. This is the goto method for 
	 * one-shot effects.
	 * @param duration_milliseconds - The duration of the animation.
	 */
	public void PlayAnim(int duration_milliseconds) {
		System.out.println("BattleEffectController.PlayAnim(int duration_milliseconds)");
		
		if(animTimer.isRunning())
			animTimer.stop();
		
		loopAnim = false;
		//edit timing so that the full anim plays in the duraction of the CombatAnim clip
		animTimer.setDelay(Math.round((float)duration_milliseconds / currentFrames.length));
		
		currentFrameIndex = 0;
		animTimer.restart();
	}
	
	public void ChangeDirection(boolean faceRight) {
		isFacingRight = faceRight;
	}
}
