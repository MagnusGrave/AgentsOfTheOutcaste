package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class FadeTransitionPanel extends JPanel {
	//private static FadeTransitionPanel instance;
	Timer timer;
	//private int timerDelay;
	//private int alphaInterval;
	private final int DEFAULT_timerDelay = 8;
	private final int DEFAULT_alphaInterval = 8;
	private Color targetColor = new Color(0, 0, 0, 0);
	private Color finalColor;
	private boolean isFadingOrBlackedOut;
	private boolean isFadingIn;
	
	private boolean hasActivatedCompletionBuffer;
	private int completion_frameBuffer; // = 20;
	private final int DEFAULT_completion_frameBuffer = 20;
	private int currentFrameBuffer;
	
	/*public FadeTransitionPanel() {
		instance = this;
		
		setOpaque(false);
		setBackground(targetColor);
	}
	
	public void Fade(boolean fadeIn, int alternateInitialDelay, ITransitionListener listener) {
		//instance.setOpaque(true);
		instance.isFadingOrBlackedOut = true;
		instance.hasActivatedCompletionBuffer = false;
		
		instance.timer = new Timer(instance.timerDelay, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Color bgC = instance.targetColor;
				int newAlpha = Math.max(0, Math.min(255, bgC.getAlpha() + (fadeIn ? instance.alphaInterval : -instance.alphaInterval)));
				Color newBGColor = new Color(bgC.getRed(), bgC.getGreen(), bgC.getBlue(), newAlpha);
				instance.targetColor = newBGColor;
				//System.out.println("newBGColor: " + newBGColor.getAlpha());
				if((fadeIn && newAlpha == 255) || (!fadeIn && newAlpha == 0)) {
					if(!instance.hasActivatedCompletionBuffer) {
						instance.hasActivatedCompletionBuffer = true;
						instance.currentFrameBuffer = instance.completion_frameBuffer;
					} else if(instance.currentFrameBuffer > 0) {
						instance.currentFrameBuffer--;
					} else { //Actually complete the damn thang
						instance.timer.stop();
						if(listener != null)
							listener.TransitionComplete();
						if(newAlpha == 0)
							instance.isFadingOrBlackedOut = false;
					}
				}
			}
		});
		if(alternateInitialDelay > 0)
			instance.timer.setInitialDelay(alternateInitialDelay);
		instance.timer.start();
	}*/
	public FadeTransitionPanel(Color finalColor) {	
		this.finalColor = finalColor;
		targetColor = new Color(finalColor.getRed(), finalColor.getGreen(), finalColor.getBlue(), 0);
		setOpaque(false);
		setBackground(targetColor);
	}
	
	public void Fade(boolean fadeIn) {
		Fade(fadeIn, 0, DEFAULT_timerDelay, DEFAULT_alphaInterval, DEFAULT_completion_frameBuffer, null, null);
	}
	public void Fade(boolean fadeIn, ITransitionListener listener) {
		Fade(fadeIn, 0, DEFAULT_timerDelay, DEFAULT_alphaInterval, DEFAULT_completion_frameBuffer, listener, null);
	}
	public void Fade(boolean fadeIn, ActionListener action) {
		Fade(fadeIn, 0, DEFAULT_timerDelay, DEFAULT_alphaInterval, DEFAULT_completion_frameBuffer, null, action);
	}
	
	public void Fade(boolean fadeIn, int alternateInitialDelay) {
		Fade(fadeIn, alternateInitialDelay, DEFAULT_timerDelay, DEFAULT_alphaInterval, DEFAULT_completion_frameBuffer, null, null);
	}
	public void Fade(boolean fadeIn, int alternateInitialDelay, ITransitionListener listener) {
		Fade(fadeIn, alternateInitialDelay, DEFAULT_timerDelay, DEFAULT_alphaInterval, DEFAULT_completion_frameBuffer, listener, null);
	}
	public void Fade(boolean fadeIn, int alternateInitialDelay, ActionListener action) {
		Fade(fadeIn, alternateInitialDelay, DEFAULT_timerDelay, DEFAULT_alphaInterval, DEFAULT_completion_frameBuffer, null, action);
	}
	
	public void Fade(boolean fadeIn, int alternateInitialDelay, int timerDelay, int alphaInterval, int completionBufferFrames) {
		Fade(fadeIn, alternateInitialDelay, timerDelay, alphaInterval, completionBufferFrames, null, null);
	}
	public void Fade(boolean fadeIn, int alternateInitialDelay, int timerDelay, int alphaInterval, int completionBufferFrames, ITransitionListener listener) {
		Fade(fadeIn, alternateInitialDelay, timerDelay, alphaInterval, completionBufferFrames, listener, null);
	}
	public void Fade(boolean fadeIn, int alternateInitialDelay, int timerDelay, int alphaInterval, int completionBufferFrames, ActionListener action) {
		Fade(fadeIn, alternateInitialDelay, timerDelay, alphaInterval, completionBufferFrames, null, action);
	}
	
	private void Fade(boolean fadeIn, int alternateInitialDelay, int timerDelay, int alphaInterval, int completionBufferFrames, ITransitionListener listener, ActionListener action) {
		if(isFadingOrBlackedOut && ( (!isFadingIn && !fadeIn) || (isFadingIn && fadeIn) ) ) {
			System.out.println("FadeTransitionPanel.Fade() - Ignoring duplicate fade call. Only fade calls that're contrary to the fade state may interupt a transition.");
			return;
		}
		
		if(!isFadingOrBlackedOut && !fadeIn) {
			System.out.println("FadeTransitionPanel.Fade() - Ignoring pointless call thats attempting to fade out a fade panel that's already faded out.");
			return;
		}
		
		System.out.println("FadeTransitionPanel.Fade() - fadeIn: " + fadeIn + ", alternateInitialDelay: " + alternateInitialDelay + ", timerDelay: " + timerDelay + ", alphaInterval: " + alphaInterval
				+ ", completionBufferFrames: " + completionBufferFrames);
		
		//instance.setOpaque(true);
		isFadingIn = fadeIn;
		isFadingOrBlackedOut = true;
		hasActivatedCompletionBuffer = false;
		completion_frameBuffer = completionBufferFrames;
		
		final JPanel thisPanel = this;
		timer = new Timer(timerDelay, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Color bgC = targetColor;
				int newAlpha = Math.max(0, Math.min(finalColor.getAlpha(), bgC.getAlpha() + (fadeIn ? alphaInterval : -alphaInterval)));
				Color newBGColor = new Color(bgC.getRed(), bgC.getGreen(), bgC.getBlue(), newAlpha);
				targetColor = newBGColor;
				//System.out.println("newBGColor: " + newBGColor.getAlpha());
				
				//Force this component to repaint itself to prevent artifacts during its fade animation
				thisPanel.repaint(20);
				
				if((fadeIn && newAlpha == finalColor.getAlpha()) || (!fadeIn && newAlpha == 0)) {
					if(!hasActivatedCompletionBuffer) {
						hasActivatedCompletionBuffer = true;
						currentFrameBuffer = completion_frameBuffer;
					} else if(currentFrameBuffer > 0) {
						currentFrameBuffer--;
					} else { //Actually complete the damn thang
						System.out.println("FadeTransitionPanel.Fade() - fade timer done.");
						
						timer.stop();
						if(listener != null)
							listener.TransitionComplete();
						else if(action != null)
							action.actionPerformed(null);
						if(newAlpha == 0)
							isFadingOrBlackedOut = false;
					}
				}
			}
		});
		if(alternateInitialDelay > 0)
			timer.setInitialDelay(alternateInitialDelay);
		timer.start();
	}
	
	
	@Override
	public void paintComponent(Graphics g) {
		if(isFadingOrBlackedOut) {
			g.setColor(targetColor);
			g.fillRect(0, 0, this.getSize().width, this.getSize().height); 
			
			//System.out.println("Fade paintComponent() - timestamp: " + System.nanoTime());
		}
	}
}
