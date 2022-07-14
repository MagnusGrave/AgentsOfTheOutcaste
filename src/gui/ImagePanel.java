package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import enums.ColorBlend;


@SuppressWarnings("serial")
public class ImagePanel extends JPanel {	
	private BufferedImage image;
	
	/**
	 * Create a panel that draws a BufferedImage on every paintComponent() call.
	 * 
	 * @param path - Describes a path with root at PROJECT_DIRECTORY/resources/ ...path
	 * Example: "character_TEST/Front@2x.png"
	 */
    public ImagePanel(String path) {
    	image = GUIUtil.GetBuffedImage(path);
    }
    
    public ImagePanel(BufferedImage image) {
    	this.image = image;
    }
    
    public ImagePanel(BufferedImage image, Color tintColor) {
    	this.image = image;
    	SetTint(tintColor);
    }
    
    public ImagePanel() {
    	//This is a placeholder for ImagePanels that are blank but may be set to an image at some point
    }
    
    
    public BufferedImage getImage() {
    	return image;
    }
    
    public int getImageWidth() {
    	return image.getWidth();
    }
    
    public int getImageHeight() {
    	return image.getHeight();
    }
    
    public void SetNewImage(String path) {
    	image = GUIUtil.GetBuffedImage(path);
    	
    	//repaint(); //This may be unnecessarily bogging down the Event Dispatch Thread, though an exhaustive, application-wide test would be required to find out.
    }
    public void SetNewImage(BufferedImage newImage) {
    	this.image = newImage;
    	
    	if(isTinted)
    		SetTint(currentTintColor, currentColorBlend);
    	
    	//repaint(); //This may be unnecessarily bogging down the Event Dispatch Thread, though an exhaustive, application-wide test would be required to find out.
    }
    /**
     * Resource management for the MapLocationPanel system.
     * @param newImage - New image to paint.
     * @param gracePeriod_ms - Provided to the repaint method as "tm - maximum time in milliseconds before update".
     */
    public void SetNewImage(BufferedImage newImage, int gracePeriod_ms) {
    	this.image = newImage;
    	
    	if(isTinted)
    		SetTint(currentTintColor, currentColorBlend);
    	
    	//repaint(gracePeriod_ms);
    	//Well fuck me running...repaint is seemingly useless at this early point of MapLocationPanel Update animation system
    	//It was actually doing harm by unnecessarily bogging down the Event Dispatch Thread. Fuck. Fuck. Fuck.
    }
    
    //Tinting - Start
    
    private boolean isTinted;
    private BufferedImage tintedImage;
    private Color currentTintColor;
    private ColorBlend currentColorBlend = ColorBlend.Screen;
    public Color GetTintColor() { return currentTintColor; }
    
    public void SetTint(Color tintColor) {
    	SetTint(tintColor, ColorBlend.Screen);
	}
    
    public void SetTint(Color tintColor, ColorBlend colorBlend) {
    	isTinted = true;
    	currentTintColor = tintColor;
    	currentColorBlend = colorBlend;
    	
    	tintedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TRANSLUCENT);
		Graphics2D graphics = tintedImage.createGraphics();
		graphics.drawImage(image, 0, 0, null);
		graphics.dispose();
		
		switch(colorBlend) {
			case Multiply:
				float r = (float)tintColor.getRed() / 255f;
		    	float g = (float)tintColor.getGreen() / 255f;
		    	float b = (float)tintColor.getBlue() / 255f;
		    	float a = (float)tintColor.getAlpha() / 255f;
				
				for (int i = 0; i < image.getWidth(); i++) {
					for (int j = 0; j < image.getHeight(); j++) {
						int ax = tintedImage.getColorModel().getAlpha(tintedImage.getRaster().getDataElements(i, j, null));
						int rx = tintedImage.getColorModel().getRed(tintedImage.getRaster().getDataElements(i, j, null));
						int gx = tintedImage.getColorModel().getGreen(tintedImage.getRaster().getDataElements(i, j, null));
						int bx = tintedImage.getColorModel().getBlue(tintedImage.getRaster().getDataElements(i, j, null));
						rx *= r;
						gx *= g;
						bx *= b;
						ax *= a;
						int rgb = (ax << 24) | (rx << 16) | (gx << 8) | (bx);
						
						tintedImage.setRGB(i, j, rgb);
						
						//System.out.println("RGB: " + rgb);
					}
				}
				break;
			case Screen:
				for (int i = 0; i < image.getWidth(); i++) {
					for (int j = 0; j < image.getHeight(); j++) {
						int pixel = image.getRGB(i, j);
						Color pixelColor = new Color(pixel);
						int alpha = (pixel >> 24) & 0xff;
						int rx = Math.min(255, pixelColor.getRed() + tintColor.getRed());
						int gx = Math.min(255, pixelColor.getGreen() + tintColor.getGreen());
						int bx = Math.min(255, pixelColor.getBlue() + tintColor.getBlue());
						
						int colorInt = (alpha << 24) | (rx << 16) | (gx << 8) | bx;
						tintedImage.setRGB(i, j, colorInt);
					}
				}
				break;
			default:
				System.err.println("ImagePanel.GetTintedImage() - Add support for: " + colorBlend);
				break;
		}
	}
    
    public void ClearTint() {
    	isTinted = false;
    	currentTintColor = null;
    	currentColorBlend = ColorBlend.Screen;
    }
    
    //Tinting - End
    
    //Transparency - Start

    private int currentAlphaValue;
    public int GetTransparency() { return currentAlphaValue; }
    
    public void SetTransparency(int alphaValue) {
    	/*isTinted = true;
    	currentTintColor = tintColor;
    	
    	tintedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TRANSLUCENT);
		Graphics2D graphics = tintedImage.createGraphics();
		graphics.drawImage(image, 0, 0, null);
		graphics.dispose();
		
		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				int pixel = image.getRGB(i, j);
				Color pixelColor = new Color(pixel);
				int alpha = (pixel >> 24) & 0xff;
				int rx = Math.min(255, pixelColor.getRed() + tintColor.getRed());
				int gx = Math.min(255, pixelColor.getGreen() + tintColor.getGreen());
				int bx = Math.min(255, pixelColor.getBlue() + tintColor.getBlue());
				
				int colorInt = (alpha << 24) | (rx << 16) | (gx << 8) | bx;
				tintedImage.setRGB(i, j, colorInt);
			}
		}*/
    	currentAlphaValue = alphaValue;
    	//piggyback Tint logic
    	if(currentTintColor == null)
    		currentTintColor = new Color(255, 255, 255, alphaValue);
    	else
    		currentTintColor = new Color(currentTintColor.getRed(), currentTintColor.getGreen(), currentTintColor.getBlue(), alphaValue);
    	SetTint(currentTintColor);
	}
    
    public void ClearTransparency() {
    	currentAlphaValue = 255;
    	SetTint(currentTintColor);
    }
    
    //Transparency - End

    private boolean paintInsideInsets;
    public void SetPaintInsideInsets(boolean paintInsideInsets) {
    	this.paintInsideInsets = paintInsideInsets;
    }
    
    //Rotate Image
    private boolean usingRotation;
    private float zRotation;
    public float getZRotation() { return zRotation; }
    private Point2D normRotationOrigin;
    private Point2D rotatedOrigin;
    public Point2D getRotatedOrigin() { return rotatedOrigin; }
    /**
     * The image holding a render of the rotated image
     */
    private BufferedImage rotatedImage;
    public void SetZRotation(float newRot) {
    	if(newRot != 0f)
    		usingRotation = true;
    	zRotation = newRot;
    	normRotationOrigin = null;
    	RescaleBoundsForRotation();
    }
    //If we don't send this frames intended flipStatus then the rotation calculations are going to use the previous zRotation value and produce erroneous results
    public void SetZRotation(float newRot, Point2D normRotationOrigin) {
    	usingRotation = true;
    	zRotation = newRot;
    	this.normRotationOrigin = normRotationOrigin;
    	
    	RescaleBoundsForRotation();
    	
    	rotatedOrigin = GetRotatedOrigin(new Dimension(this.getWidth(), this.getHeight()), this.xFlipStatus, newRot, normRotationOrigin);
    	
    	//Create a rotated version of our image
    	GraphicsConfiguration gc = getDefaultConfiguration();
        rotatedImage = tilt(this.xFlipStatus ? mirrorXImage : image, Math.toRadians((double)zRotation), gc);
    }
    
    
    public static GraphicsConfiguration getDefaultConfiguration() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        return gd.getDefaultConfiguration();
    }
    
    //TODO This method should resize our panel for a rotated image that's size will be larger than our current bounds(i.e. corners sticking out of bounds) but current its not doing anything
    /**
     * The intention of this method is to resize this panel's bounds to accomodate the newly rotated image. This will allow the weapon sprite to maintain a consistent scale at any rotation.
     * Otherwise images whose rotated bounds are larger than this panel's bounds will be shrunken so that it fits within.
     */
    private void RescaleBoundsForRotation() {
    	/*
    	int extentX = this.getBounds().width / 2;
    	int extentY = this.getBounds().height / 2;
    	
    	Rectangle visibleRect = new Rectangle();
    	this.computeVisibleRect(visibleRect);
    	if(this.getGraphics() != null && this.getGraphics().getClipBounds() != null)
    		System.err.println("ImagePanel.RescaleBoundsForRotation() - this.getGraphics().getClipBounds().x: " + this.getGraphics().getClipBounds().x
    			+ ", this.getGraphics().getClipBounds().width: " + this.getGraphics().getClipBounds().width);
    	
    	this.setBounds(this.getBounds().x + extentX, this.getBounds().y + extentY, this.getBounds().width + extentX, this.getBounds().height + extentY);
    	*/
    }
    
    public static BufferedImage tilt(BufferedImage image, double angle, GraphicsConfiguration gc) {
        double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
        int w = image.getWidth(), h = image.getHeight();
        int neww = (int)Math.floor(w*cos+h*sin), newh = (int)Math.floor(h*cos+w*sin);
        BufferedImage result = gc.createCompatibleImage(neww, newh, image.getColorModel().getTransparency());
        Graphics2D g = result.createGraphics();
        g.translate((neww-w)/2, (newh-h)/2);
        g.rotate(angle, w/2, h/2);
        g.drawRenderedImage(image, null);
        return result;
    }
    
    public static Point2D GetRotatedOrigin(Dimension panelSize, boolean xFlipStatus, float zRot, Point2D normRotOrigin) {
    	//Pivot from the center
    	Point pivotPoint = new Point(Math.round(panelSize.width / 2f), Math.round(panelSize.height / 2f));
    	Point naturalOrigin = new Point(Math.round(panelSize.width * (float)normRotOrigin.getX()), Math.round(panelSize.height * (float)normRotOrigin.getY()));
    	
    	//I think this is the correct way to calc mirrored origins
    	if(xFlipStatus)
    		naturalOrigin.x = panelSize.width - naturalOrigin.x;
    	
    	double radianRot = Math.toRadians((double)zRot);
    	
    	float rotatedX = (float)( (double)pivotPoint.x + (naturalOrigin.x-pivotPoint.x)*Math.cos(radianRot) - (naturalOrigin.y-pivotPoint.y)*Math.sin(radianRot) );
    	float rotatedY = (float)( (double)pivotPoint.y + (naturalOrigin.x-pivotPoint.x)*Math.sin(radianRot) + (naturalOrigin.y-pivotPoint.y)*Math.cos(radianRot) );
    	Point2D rotatedOrigin = new Point2D.Float( rotatedX / panelSize.width, rotatedY / panelSize.height);
    	
    	//System.err.println("ImagePanel.GetRotatedOrigin() - xFlipStatus: " + xFlipStatus + " + zRot: " + zRot + " + normRotOrigin: " + normRotOrigin + " = rotatedOrigin: " + rotatedOrigin);
    	
    	return rotatedOrigin;
    }
    
    
    public void ClearRotation() {
    	usingRotation = false;
    	zRotation = 0f;
    	normRotationOrigin = null;
    }
    
    //For Flipping awareness
    /**
     * Use this methd to prepare an ImagePanel for the use of its MirrorX display.
     */
    public void CreateMirrorXImage() {
    	this.mirrorXImage = GUIUtil.Mirror(this.image);
    }
    private boolean xFlipStatus;
    public boolean getXFlipStatus() { return xFlipStatus; }
    /**
     * This method mirrors the panel's image. Because it was clashing with the Order of Operations relative to SetZRotation being called sequencially on the same instance it has become secondary in its
     * control of the displayed image. It now sets this instance's xFlipStatus member variable and then tells the SetZRotation method to adjust the display image. The SetZRotation method will then
     * decide whether to use the image or the mirroredImage and rotate that image when necessary.
     * _____-
     * As of right now, this method is only being used in one place in BattleCharacterController.UpdateWeaponSprite() preceding a call to SetZRotation. THis method's independant use has not been tested.
     * 
     * @param newStatus
     */
    public void SetXFlipStatus(boolean precedesSetZRotationCall, boolean newStatus) {
    	boolean previousStatus = xFlipStatus;
    	
    	xFlipStatus = newStatus;
    	
    	//This this method call happens before a guaranteed call to SetZRotation then we don't need to call it here AND again later this frame
    	if(!precedesSetZRotationCall && rotatedImage != null && xFlipStatus != previousStatus)
    		this.SetZRotation(this.zRotation, this.normRotationOrigin);
    }
    private BufferedImage mirrorXImage;
    //Now this is only used internally
    //public BufferedImage getMirrorXImage() { return mirrorXImage; }
    
    
    @Override
    protected void paintComponent(Graphics graphics) {
    	//Rotate image
    	Graphics2D g = (Graphics2D) graphics;
    	
        super.paintComponent(g);
        
        //For blank images
        if(image == null)
        	return;
        
        
        //Alternate Draw Method - For rotated images
        //Flipping and roatating doesn not yet suppport paintingInsideInsets or Tint yet
        if(usingRotation) {
        	g.drawImage(rotatedImage, 0, 0, this.getWidth(), this.getHeight(), this);       
        	
        	//TODO Add support for missing features instead of returning here
        	return;
        }
        //RETURNING HERE
        
        
        final BufferedImage displayImage = xFlipStatus ? mirrorXImage : image;
        
    	if(!paintInsideInsets) {
	        if(isTinted)
	        	g.drawImage(tintedImage, 0, 0, this.getWidth(), this.getHeight(), this);
	        else
	        	g.drawImage(displayImage, 0, 0, this.getWidth(), this.getHeight(), this);
        } else {
        	Image imageToDraw = isTinted ? tintedImage : displayImage;
	        Insets insets = this.getInsets();
	        g.drawImage(imageToDraw, insets.left, insets.top, this.getWidth() - insets.left - insets.right, this.getHeight() - insets.top - insets.bottom, this);
        }
    }
    	
    //A layout helper method to be used after setting a preferred size for the ImagePanel
    public void ConformPreferredSizeToAspectRatio(boolean prioritizeWidth) {
    	float ratio = prioritizeWidth ? (float)image.getHeight() / (float)image.getWidth() : (float)image.getWidth() / (float)image.getHeight();
    	
    	//Resize to fit the aspect ratio
    	int newWidth = prioritizeWidth ? getPreferredSize().width : Math.round(getPreferredSize().height * ratio);
    	int newHeight = prioritizeWidth ? Math.round(getPreferredSize().width * ratio) : getPreferredSize().height;
    	setPreferredSize(new Dimension(newWidth, newHeight));
    	
    	//System.out.println("ImagePanel - Ratio: " + ratio + ", newWidth: " + newWidth + ", newHeight: " + newHeight);
    }
    
    public void ConformSizeToAspectRatio(boolean prioritizeWidth) {
    	float ratio = prioritizeWidth ? (float)image.getHeight() / (float)image.getWidth() : (float)image.getWidth() / (float)image.getHeight();
    	int newWidth = prioritizeWidth ? getSize().width : Math.round(getSize().height * ratio);
    	int newHeight = prioritizeWidth ? Math.round(getSize().width * ratio) : getSize().height;
    	setSize(new Dimension(newWidth, newHeight));
    }
}
