package gui;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpriteSheet {
	public SpriteSheet(BufferedImage sheetImage, int columns, int rows, int deadFrames) {
		this.sheetImage = sheetImage;
		this.columns = columns;
		this.rows = rows;
		this.deadFrames = deadFrames;
	}
	private BufferedImage sheetImage;
	private int columns;
	private int rows;
	private int deadFrames;
	
	public int Width() { return sheetImage.getWidth() / columns; }
	public int Height() { return sheetImage.getHeight() / rows; }
	
	public BufferedImage GetSprite(int startIndex, int cellCountWidth, int cellCountHeight) {
		int modulo = startIndex % columns;
		int x = modulo * Width();
		int y = (startIndex / columns) * Height();
		int w = cellCountWidth * Width();
		int h = cellCountHeight * Height();
		//System.out.println("GetSprite - x:" + x + ", y:" + y + ", w:" + w + ", h:" + h);
		return sheetImage.getSubimage(x, y, w, h);
	}
	
	//This is an alternate usage of SpriteSheet which uses names to identify sprites, rather than indices, and also selectively slices sheets as
	//directed by specification contained in a text file.
	public SpriteSheet(BufferedImage sheetImage, List<SpriteMeta> spriteMetas) {
		this.sheetImage = sheetImage;
		this.columns = -1;
		this.rows = -1;
		this.deadFrames = -1;
		
		spriteMap = new HashMap<String, BufferedImage>();
		//use metas to get all sprites as subimages, Unity has the spritesheet origins at the bottom left corner for whatever reason
		for(SpriteMeta meta : spriteMetas)
			spriteMap.put(meta.name, sheetImage.getSubimage(meta.x, sheetImage.getHeight() - meta.height - meta.y, meta.width, meta.height));
	}
	private Map<String, BufferedImage> spriteMap;
	
	//public SpriteSheet(BufferedImage sheetImage, List<SpriteMeta> spriteMetas, Color sheetTint, ColorBlend colorBlend) {
	//	this(sheetImage, spriteMetas);
	//	SetTint(sheetTint, colorBlend);
	//}
	/*Color currentTintColor;
	public void SetTint_Permanent(Color tintColor, ColorBlend colorBlend) {
    	currentTintColor = tintColor;
    	BufferedImage tintedImage = new BufferedImage(sheetImage.getWidth(), sheetImage.getHeight(), BufferedImage.TRANSLUCENT);
		Graphics2D graphics = tintedImage.createGraphics();
		graphics.drawImage(sheetImage, 0, 0, null);
		graphics.dispose();
		
		switch(colorBlend) {
			case Multiply:
				float r = (float)tintColor.getRed() / 255f;
		    	float g = (float)tintColor.getGreen() / 255f;
		    	float b = (float)tintColor.getBlue() / 255f;
		    	float a = (float)tintColor.getAlpha() / 255f;
				
				for (int i = 0; i < sheetImage.getWidth(); i++) {
					for (int j = 0; j < sheetImage.getHeight(); j++) {
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
				for (int i = 0; i < sheetImage.getWidth(); i++) {
					for (int j = 0; j < sheetImage.getHeight(); j++) {
						int pixel = sheetImage.getRGB(i, j);
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
		sheetImage = tintedImage;
	}*/
	//this doesn't work, changing the source image will affect all other usercases of it
	
	public BufferedImage GetSprite(String name) {
		if(!spriteMap.containsKey(name)) {
			System.err.println("SpriteSheet.GetSprite(String name) - Doesn't contain image named: " + name);
			return null;
		}
		return spriteMap.get(name);
	}
}
