package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class JFxLabel extends JLabel {
	public JFxLabel(String text, int swingConts_horizAlignment, Font font, Color color) {
		super(text, swingConts_horizAlignment);
		setFont(font);
		setForeground(color);
	}
	
	public JFxLabel(String text, int swingConts_horizAlignment, Font font, Color color, boolean debugComponent) {
		this(text, swingConts_horizAlignment, font, color);
		this.debugComponent = true;
	}
	
	public JFxLabel(String text, Icon icon, int swingConts_horizAlignment, Font font, Color color) {
		super(text, icon, swingConts_horizAlignment);
		setFont(font);
		setForeground(color);
	}
	
	public JFxLabel(String text, Icon icon, int swingConts_horizAlignment, Font font, Color color, boolean debugComponent) {
		this(text, icon, swingConts_horizAlignment, font, color);
		this.debugComponent = true;
	}
	
	private List<Integer> effects = new ArrayList<Integer>();
	
	private static final Integer STROKE = 0;
	private Color strokeColor;
	private int strokeThickness;
	private boolean isStrokingShadow;
	
	private static final Integer SHADOW = 1;
	private Color shadowColor;
	private Point shadowVector;
	
	private static final Integer HIGHLINE = 2;
	private Color highlineColor;
	private int highlineCenterOffset;
	private int highlineThickness;
	
	private static final Integer DROPSHADOW = 3;
	private final String dropShadowImagePath = "gui/dropShadow.png";
	private Ninecon dropShadowNinecon;
	
	
	public JFxLabel withStroke(Color strokeColor, int strokeThickness, boolean isStrokingShadow) {
		if(!effects.contains(STROKE)) {
			effects.add(STROKE);
			Collections.sort(effects);
		}
		this.strokeColor = strokeColor;
		this.strokeThickness = strokeThickness;
		this.isStrokingShadow = isStrokingShadow;
		
		//When this component's swingConts_horizAlignment is left or right it'll cut off the stroke on that side so add an inset necessary to offset the text from the each of the container
		if(this.getHorizontalAlignment() == SwingConstants.LEFT) {
			this.setBorder(BorderFactory.createEmptyBorder(0, strokeThickness, 0, 0));
		} else if(this.getHorizontalAlignment() == SwingConstants.RIGHT) {
			this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, strokeThickness));
		}
		
		return this;
	}
	
	public JFxLabel withShadow(Color shadowColor, Point shadowVector) {
		if(!effects.contains(SHADOW)) {
			effects.add(SHADOW);
			Collections.sort(effects);
		}
		this.shadowColor = shadowColor;
		this.shadowVector = shadowVector;
		return this;
	}
	
	//Pending implementation in paintComponent()
	/*public JFxLabel withHighline(Color highlineColor, int highlineCenterOffset, int highlineThickness) {
		if(!effects.contains(HIGHLINE)) {
			effects.add(HIGHLINE);
			Collections.sort(effects);
		}
		this.highlineColor = highlineColor;
		this.highlineCenterOffset = highlineCenterOffset;
		this.highlineThickness = highlineThickness;
		return this;
	}*/
	
	//Pending implementation in paintComponent()
	/*public JFxLabel withDropShadow(Color colorTint, ColorBlend colorBlend) {
		if(!effects.contains(DROPSHADOW)) {
			effects.add(DROPSHADOW);
			Collections.sort(effects);
			dropShadowNinecon = new Ninecon(GUIUtil.GetBuffedImage(dropShadowImagePath), 2, 2, 2, 2, 1f, colorTint, colorBlend);
		}
		return this;
	}*/
	
	
	
	public void SetColorScheme(JFxColorScheme colorScheme) {
		setForeground(colorScheme.textColor);
		this.strokeColor = colorScheme.strokeColor;
		this.shadowColor = colorScheme.shadowColor;
	}
	
	
	boolean debugComponent;
	boolean boundsDirtyFlag;
	int boundsCenterX;
	int boundsCenterY;
	int boundsMinX;
	int boundsMaxX;
	
	@Override
	public void setText(String text) {
		super.setText(text);
		boundsDirtyFlag = true;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		//Get some commonly used data for efficiency's sake
		FontMetrics metrics = g.getFontMetrics();
		int width = (int)(metrics.stringWidth(this.getText()) / 2);
		int height = (int)(metrics.getHeight() / 4);
		
		if(boundsCenterX == 0 || boundsDirtyFlag) {
			boundsDirtyFlag = false;
			
			//Rectangle2D bounds = g.getClip().getBounds2D();
			//The above method apparently was causing glitches when this logic was run as a result of SetText being called on this component, I wouldn't be surprised if the new approach breaks too
			Rectangle2D bounds = SwingUtilities.getLocalBounds(this);
			
			boundsCenterX = (int)bounds.getCenterX();
			boundsCenterY =  (int)bounds.getCenterY();
			
			boundsMinX = (int)bounds.getMinX();
			boundsMaxX = (int)bounds.getMaxX();
		}
		if(debugComponent)
			System.out.println("JFxLabel - width: " + width + ", height: " + height + ", boundsCenterX: " + boundsCenterX + ", boardsCenterY: " + boundsCenterY);
		int x = 0;
		switch(getHorizontalAlignment()) {
			case SwingConstants.LEFT:
				x = boundsMinX;
				break;
			case SwingConstants.CENTER:
				x = boundsCenterX - width;
				break;
			case SwingConstants.RIGHT:
				x = boundsMaxX - (width * 2);
				break;
			default:
				System.err.println("JFXLabel.paintComponent() - Add support for: " + getHorizontalAlignment());
		}
		int y = boundsCenterY + height;
		
		//Adjust to fix centering on the Y axis
		y -= 1;
		//System.out.println("JFxLabel - x: " + x + ", y: " + y);
		
		if(effects.contains(STROKE)) {
			g.setColor(strokeColor);
			
			//Accomodate shadow
			int shadowExtensionX = 0;
			int shadowExtensionY = 0;
			if(isStrokingShadow && effects.contains(SHADOW)) {
				if(shadowVector.x > 0)
					shadowExtensionX = shadowVector.x;
				if(shadowVector.y > 0)
					shadowExtensionY = shadowVector.y;
			}
			
			int horizontalShift = 0;
			if(this.getHorizontalAlignment() == SwingConstants.LEFT)
				horizontalShift = strokeThickness;
			else if(this.getHorizontalAlignment() == SwingConstants.RIGHT)
				horizontalShift = -strokeThickness;
			
			//Diagonal
			g.drawString(this.getText(), x+strokeThickness+shadowExtensionX+horizontalShift, y+strokeThickness+shadowExtensionY);
			g.drawString(this.getText(), x-strokeThickness+horizontalShift, y-strokeThickness);
			g.drawString(this.getText(), x+strokeThickness+shadowExtensionX+horizontalShift, y-strokeThickness);
			g.drawString(this.getText(), x-strokeThickness+horizontalShift, y+strokeThickness+shadowExtensionY);
			//Cartesian
			g.drawString(this.getText(), x+strokeThickness+shadowExtensionX+horizontalShift, y);
			g.drawString(this.getText(), x-strokeThickness+horizontalShift, y);
			g.drawString(this.getText(), x+horizontalShift, y-strokeThickness);
			g.drawString(this.getText(), x+horizontalShift, y+strokeThickness+shadowExtensionY);
		}
		
		if(effects.contains(SHADOW)) {
			int horizontalShift = 0;
			if(this.getHorizontalAlignment() == SwingConstants.LEFT)
				horizontalShift = strokeThickness;
			else if(this.getHorizontalAlignment() == SwingConstants.RIGHT)
				horizontalShift = -strokeThickness;
			
			g.setColor(shadowColor);
			g.drawString(this.getText(), x+shadowVector.x+horizontalShift, y+shadowVector.y);
		}
		
		if(effects.contains(HIGHLINE)) {
			
		}
		
		if(effects.contains(DROPSHADOW)) {
			
		}
		
		//Normal text painting procedure
		super.paintComponent(g);
	}
}
