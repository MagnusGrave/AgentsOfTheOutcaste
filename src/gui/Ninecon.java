package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JComponent;

import enums.ColorBlend;

//Nine-slice Scaling Icon
public class Ninecon implements Icon {
	private BufferedImage image;
	private int leftw;
	private int rightw;
	private int toph;
	private int bottomh;
	private int width;
	private int height;
	
	
	protected Ninecon(String path, int leftw, int rightw, int toph, int bottomh, float scale, Color tintColor, ColorBlend colorBlend) {
		this.image = GetBufferedImage(path);
	    this.leftw = leftw;
	    this.rightw = rightw;
	    this.toph = toph;
	    this.bottomh = bottomh;
	    ApplyScale(scale);
	    if(tintColor != null)
	    	SetTint(tintColor, colorBlend);
	}
	
	protected Ninecon(BufferedImage image, int leftw, int rightw, int toph, int bottomh, float scale, Color tintColor, ColorBlend colorBlend) {
		this.image = image;
	    this.leftw = leftw;
	    this.rightw = rightw;
	    this.toph = toph;
	    this.bottomh = bottomh;
	    ApplyScale(scale);
	    if(tintColor != null)
	    	SetTint(tintColor, colorBlend);
	}
	
	private BufferedImage GetBufferedImage(String path) {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("resources/" + path);
		if(is == null) {
			System.err.println("File not found at: " + "resources/" + path);
			return null;
		}
		BufferedImage imge = null;
		try {
			imge = ImageIO.read(is);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return imge;
	}
	
	private final int BufferedType = BufferedImage.TYPE_INT_ARGB;
	
	private void ApplyScale(float scale) {
		Image img = image.getScaledInstance(Math.round(image.getWidth()*scale), Math.round(image.getHeight()*scale), Image.SCALE_DEFAULT);
		image = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedType);
	    // Draw the image on to the buffered image
	    Graphics2D bGr = image.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();
		
		leftw = Math.round(leftw*scale);
	    rightw = Math.round(rightw*scale);
	    toph = Math.round(toph*scale);
	    bottomh = Math.round(bottomh*scale);
	}
	
	
	//An alternate version of a ninecon that tiles its N,E,S,W sides instead of stretching them
	protected Ninecon(BufferedImage NW, BufferedImage N, BufferedImage NE, BufferedImage E, BufferedImage SE, BufferedImage S, BufferedImage SW, BufferedImage W,
					  Color bgColor, float scale, Color tintColor, ColorBlend colorBlend) {
		this.NW = NW;
	    this.N = N;
	    this.NE = NE;
	    this.E = E;
	    this.SE = SE;
	    this.S = S;
	    this.SW = SW;
	    this.W = W;
	    this.bgColor = bgColor;
	    tileScale = scale;
	    ApplyTiledScale(scale);
	    if(tintColor != null)
	    	SetTint(tintColor, colorBlend);
	}
	
	BufferedImage NW;
	BufferedImage N;
	BufferedImage NE;
	BufferedImage E;
	BufferedImage SE;
	BufferedImage S;
	BufferedImage SW;
	BufferedImage W;
	Color bgColor;
	float tileScale;
	
	private void ApplyTiledScale(float scale) {
		ScaleImage(NW, scale);
		ScaleImage(N, scale);
		ScaleImage(NE, scale);
		ScaleImage(E, scale);
		ScaleImage(SE, scale);
		ScaleImage(S, scale);
		ScaleImage(SW, scale);
		ScaleImage(W, scale);
	}
	
	private void ScaleImage(BufferedImage tileImage, float scale) {
		Image img = tileImage.getScaledInstance(Math.round(tileImage.getWidth()*scale), Math.round(tileImage.getHeight()*scale), Image.SCALE_FAST);
		tileImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedType);
	    Graphics2D bGr = tileImage.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();
	}
	
	//this will be used by SpriteSheetUtility when generating a new instance of the ninecon instead of reusing the same image
	private void SetTint(Color tintColor, ColorBlend colorBlend) {
		if(image != null)
			image = GUIUtil.GetTintedImage(image, tintColor, colorBlend);
		else {
			NW = GUIUtil.GetTintedImage(NW, tintColor, colorBlend);
			N = GUIUtil.GetTintedImage(N, tintColor, colorBlend);
			NE = GUIUtil.GetTintedImage(NE, tintColor, colorBlend);
			E = GUIUtil.GetTintedImage(E, tintColor, colorBlend);
			SE = GUIUtil.GetTintedImage(SE, tintColor, colorBlend);
			S = GUIUtil.GetTintedImage(S, tintColor, colorBlend);
			SW = GUIUtil.GetTintedImage(SW, tintColor, colorBlend);
			W = GUIUtil.GetTintedImage(W, tintColor, colorBlend);
		}
	}
	
	@Override public int getIconWidth() {
		return width; // Math.max(image.getWidth(null), width);
	}
	
	@Override public int getIconHeight() {
		if(image != null)
			return Math.max(image.getHeight(null), height);
		else
			return height;
	}
	
	@Override public void paintIcon(Component cmp, Graphics g, int x, int y) {
		//System.out.println("PaintIcon - x: " + x + ", Has Focus: " + cmp.hasFocus());
		
	    Graphics2D g2 = (Graphics2D) g.create();
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	                        RenderingHints.VALUE_ANTIALIAS_OFF);
	    Insets i = new Insets(0, 0, 0, 0);
	    if (cmp instanceof JComponent) {
	    	JComponent jCom = (JComponent)cmp;
	    	if(jCom.getBorder() != null)
	    		i = jCom.getBorder().getBorderInsets(cmp);
	    }
	    
	    width  = cmp.getWidth() - i.left - i.right;
	    height = cmp.getHeight() - i.top - i.bottom;
	    
	    //Do normal painting
	    if(image != null) {
		    int iw = image.getWidth(cmp);
		    int ih = image.getHeight(cmp);
		
		    g2.drawImage(
		    		image.getSubimage(leftw, toph, iw - leftw - rightw, ih - toph - bottomh),
		        leftw, toph, width - leftw - rightw, height - toph - bottomh, cmp);
		    if (leftw > 0 && rightw > 0 && toph > 0 && bottomh > 0) {
		      g2.drawImage(image.getSubimage(leftw, 0, iw - leftw - rightw, toph),
		                   leftw, 0, width - leftw - rightw, toph, cmp);
		      g2.drawImage(image.getSubimage(leftw, ih - bottomh, iw - leftw - rightw, bottomh),
		                   leftw, height - bottomh, width - leftw - rightw, bottomh, cmp);
		      g2.drawImage(image.getSubimage(0, toph, leftw, ih - toph - bottomh),
		                   0, toph, leftw, height - toph - bottomh, cmp);
		      g2.drawImage(image.getSubimage(iw - rightw, toph, rightw, ih - toph - bottomh),
		                   width - rightw, toph, rightw, height - toph - bottomh, cmp);
		
		      g2.drawImage(image.getSubimage(0, 0, leftw, toph),
		                   0, 0, cmp);
		      g2.drawImage(image.getSubimage(iw - rightw, 0, rightw, toph),
		                   width - rightw, 0, cmp);
		      g2.drawImage(image.getSubimage(0, ih - bottomh, leftw, bottomh),
		                   0, height - bottomh, cmp);
		      g2.drawImage(image.getSubimage(iw - rightw, ih - bottomh, rightw, bottomh),
		                   width - rightw, height - bottomh, cmp);
		    }
	    }
	    //Do tiled painting
	    else
	    {
	    	int tileWidth = NE.getWidth(null);
	    	int tileHeight = NE.getHeight(null);
	    	int scaledTileWidth = (int)Math.round(NE.getWidth(null) * tileScale);
	    	int scaledTileHeight = (int)Math.round(NE.getHeight(null) * tileScale);
	    	
	    	//Draw four corners
	    	g2.drawImage(NW, 0, 0, scaledTileWidth, scaledTileHeight, cmp);
	    	g2.drawImage(NE, width - scaledTileWidth, 0, scaledTileWidth, scaledTileHeight, cmp);
	    	g2.drawImage(SW, 0, height - scaledTileHeight, scaledTileWidth, scaledTileHeight, cmp);
	    	g2.drawImage(SE, width - scaledTileWidth, height - scaledTileHeight, scaledTileWidth, scaledTileHeight, cmp);
	    	
	    	//iterate the drawing of the sides for each image that'll fit within the width and height
	    	int horizontalGap = width - (scaledTileWidth * 2);
	    	int horiTileCount = horizontalGap / scaledTileWidth;
	    	int scaledHoriRemainder = horizontalGap % scaledTileWidth;
	    	int horiRemainder = (int)Math.round(tileWidth * (scaledHoriRemainder / (float)scaledTileWidth));
	    	
	    	int vertGap = height - (scaledTileHeight * 2);
	    	int vertTileCount = vertGap / scaledTileHeight;
	    	int scaledVertRemainder = vertGap % scaledTileHeight;
	    	int vertRemainder = (int)Math.round(tileHeight * (scaledVertRemainder / (float)scaledTileHeight));
	    	
	    	for(int h = 1; h <= horiTileCount; h++) {
	    		g2.drawImage(N, scaledTileWidth * h, 0, scaledTileWidth, scaledTileHeight, cmp);
	    		g2.drawImage(S, scaledTileWidth * h, height - scaledTileWidth, scaledTileWidth, scaledTileHeight, cmp);
	    	}
	    	if(scaledHoriRemainder > 0) {
	    		g2.drawImage(N.getSubimage(0, 0, horiRemainder, tileHeight), scaledTileWidth * (horiTileCount + 1), 0, scaledHoriRemainder, scaledTileHeight, cmp);
	    		g2.drawImage(S.getSubimage(0, 0, horiRemainder, tileHeight), scaledTileWidth * (horiTileCount + 1), height - scaledTileHeight, scaledHoriRemainder, scaledTileHeight, cmp);
	    	}
	    	
	    	for(int v = 1; v <= vertTileCount; v++) {
	    		g2.drawImage(W, 0, scaledTileHeight * v, scaledTileWidth, scaledTileHeight, cmp);
	    		g2.drawImage(E, width - scaledTileWidth, scaledTileHeight * v, scaledTileWidth, scaledTileHeight, cmp);
	    	}
	    	if(scaledVertRemainder > 0) {
	    		g2.drawImage(W.getSubimage(0, 0, tileWidth, vertRemainder), 0, scaledTileHeight * (vertTileCount + 1), scaledTileWidth, scaledVertRemainder, cmp);
	    		g2.drawImage(E.getSubimage(0, 0, tileWidth, vertRemainder), width - scaledTileWidth, scaledTileHeight * (vertTileCount + 1), scaledTileWidth, scaledVertRemainder, cmp);
	    	}
	    	
	    	//fill center
	    	g2.setColor(bgColor);
	    	g2.fillRect(scaledTileWidth, scaledTileWidth, width - (scaledTileWidth * 2), height - (scaledTileWidth * 2));
	    }
	    
	    g2.dispose();
	}
	
	//A special use of Ninecon that do not join the component heirarchy, this method is used to manually paint this ninecon from inside another component's paint method
	public void paintIconWithoutComponent(Insets i, Graphics g, int x, int y, int width, int height) {
		//System.out.println("PaintIcon - x: " + x + ", Has Focus: " + cmp.hasFocus());
		
	    Graphics2D g2 = (Graphics2D) g.create();
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	                        RenderingHints.VALUE_ANTIALIAS_OFF);
	
	    int iw = image.getWidth(null);
	    int ih = image.getHeight(null);
	    //hide the members for width and height with the specified parameters
	    width  = width - i.left - i.right;
	    height = height - i.top - i.bottom;
	    
	    g2.drawImage(image.getSubimage(leftw, toph, iw - leftw - rightw, ih - toph - bottomh),
	    			x + leftw,
	    			y + toph,
	    			width - leftw - rightw, height - toph - bottomh, null);
	    if (leftw > 0 && rightw > 0 && toph > 0 && bottomh > 0) {
	      g2.drawImage(image.getSubimage(leftw, 0, iw - leftw - rightw, toph),
	                   x + leftw,
	                   y,
	                   width - leftw - rightw, toph, null);
	      g2.drawImage(image.getSubimage(leftw, ih - bottomh, iw - leftw - rightw, bottomh),
	                   x + leftw,
	                   y + height - bottomh,
	                   width - leftw - rightw, bottomh, null);
	      g2.drawImage(image.getSubimage(0, toph, leftw, ih - toph - bottomh),
	                   x,
	                   y + toph,
	                   leftw, height - toph - bottomh, null);
	      g2.drawImage(image.getSubimage(iw - rightw, toph, rightw, ih - toph - bottomh),
	                   x + width - rightw,
	                   y + toph,
	                   rightw, height - toph - bottomh, null);
	
	      g2.drawImage(image.getSubimage(0, 0, leftw, toph),
	                   x,
	                   y,
	                   null);
	      g2.drawImage(image.getSubimage(iw - rightw, 0, rightw, toph),
	                   x + width - rightw,
	                   y,
	                   null);
	      g2.drawImage(image.getSubimage(0, ih - bottomh, leftw, bottomh),
	                   x,
	                   y + height - bottomh,
	                   null);
	      g2.drawImage(image.getSubimage(iw - rightw, ih - bottomh, rightw, bottomh),
	                   x + width - rightw,
	                   y + height - bottomh,
	                   null);
	    }
	    g2.dispose();
	}
}
