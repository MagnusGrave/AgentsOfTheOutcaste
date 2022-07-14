package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;

import enums.ColorBlend;

@SuppressWarnings("serial")
public class CustomButton extends JButton {
	public CustomButton(BufferedImage iconImage, Color normalColor, Color hoverColor, BufferedImage upImage, BufferedImage downImage, Color buttonBGColor) {
		super();
		this.iconImage = iconImage;
		
		//Allow image alpha
		this.setOpaque(true);
		this.setBackground(buttonBGColor);
		
		//Remove focus border
		this.setBorder(null);
		
		if(normalColor == null)
			idle = upImage;
		else
			idle = GUIUtil.GetTintedImage(upImage, normalColor);
		if(hoverColor == null) {
			hover = upImage;
			pressed = downImage;
		} else {
			hover = GUIUtil.GetTintedImage(upImage, hoverColor);
			pressed = GUIUtil.GetTintedImage(downImage, hoverColor);
		}

		addMouseListener(new MouseAdapter() {
	         public void mouseEntered(MouseEvent me) {
	        	 if(!isEnabled())
	        		 return;
	        	 
	        	 currentBGImage = hover;
	        	 revalidate();
	        	 repaint();
	         }
	         public void mouseExited(MouseEvent me) {
	        	 if(!isEnabled())
	        		 return;
	        	 
	        	 currentBGImage = idle;
	        	 isPressed = false;
	        	 revalidate();
	        	 repaint();
	         }
	         public void mousePressed(MouseEvent e) {
	        	 if(!isEnabled())
	        		 return;
	        	 
	        	 currentBGImage = pressed;
	        	 isPressed = true;
	        	 revalidate();
	        	 repaint();
	         }
	         public void mouseReleased(MouseEvent e) {
	        	 if(!isEnabled())
	        		 return;
	        	 
	        	 currentBGImage = idle;
	        	 isPressed = false;
	        	 revalidate();
	        	 repaint();
	         }
		});
		currentBGImage = idle;
		
		//Add support for disabled state
		disabled = GUIUtil.GetTintedImage(upImage, Color.GRAY, ColorBlend.Multiply);
		disabledIconImage = GUIUtil.GetTintedImage(SpriteSheetUtility.CrossIcon(), Color.LIGHT_GRAY);
		addPropertyChangeListener("enabled", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				currentBGImage = (boolean)evt.getNewValue() ? idle : disabled;
				isPressed = false;
				revalidate();
				repaint();
			}
		});
		
		revalidate();
		repaint();
	}
	
	private BufferedImage iconImage;
	private BufferedImage currentBGImage;
	private BufferedImage idle;
	private BufferedImage hover;
	private BufferedImage pressed;
	private BufferedImage disabled;
	private BufferedImage disabledIconImage;
	private boolean isPressed;
	//Icon placement magic numbers
	private final float iconScale = 0.6f;
	private final float iconPos = (1f - iconScale) / 2; //0.1f;
	private final float iconUpY = 0.06f;
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		//Cover up bullshit default button display
		g.setColor(getBackground());
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		//Change images for different states
		g.drawImage(currentBGImage, 0, 0, this.getWidth(), this.getHeight(), this);

		int xPos = Math.round(this.getWidth() * iconPos);
		Point iconLocation = isPressed ? new Point(xPos, Math.round(this.getHeight() * iconPos)) : new Point(xPos, Math.round(this.getHeight() * iconUpY));
		
		g.drawImage(this.isEnabled() ? iconImage : disabledIconImage, iconLocation.x, iconLocation.y, Math.round(this.getWidth() * iconScale), Math.round(this.getHeight() * iconScale), this);
	}
}
