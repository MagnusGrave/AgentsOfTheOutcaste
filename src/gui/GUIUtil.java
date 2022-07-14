package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import enums.ColorBlend;
import enums.MissionIndicatorType;
import enums.SettlementDesignation;
import enums.SettlementType;

import gameLogic.Board.TerrainType;


public final class GUIUtil {
	private final static GUIUtil instance = new GUIUtil();

	GUIUtil() {
		CreateFonts();
	}
	
	static {
		GetSettlementIcons();
		GetIndicatorOverlayIcons();
	}
	
	//Commonly used helper methods
	
	public static BufferedImage GetBuffedImage(String path) {
		InputStream is = instance.getClass().getClassLoader().getResourceAsStream("resources/" + path);
		if(is == null) {
			System.err.println("File not found at: " + "resources/" + path + " ... Dont forget to Refresh the Java project after adding new files.");
			Thread.dumpStack();
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
	
	public static BufferedImage GetBuffedImageFromAbsolutePath(String absolutePath) {
		File file = new File(absolutePath);
		if(!file.exists()) {
			System.err.println("File not found at absolute path: " + absolutePath);
			Thread.dumpStack();
			return null;
		}
		BufferedImage imge = null;
		try {
			imge = ImageIO.read(file);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return imge;
	}
	
	public static BufferedImage GetTintedImage(BufferedImage original, Color tintColor) {	
		//BufferedImage tintedImage = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TRANSLUCENT);
		BufferedImage tintedImage = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = tintedImage.createGraphics();
		graphics.drawImage(original, 0, 0, null);
		graphics.dispose();
		
		for (int i = 0; i < original.getWidth(); i++) {
			for (int j = 0; j < original.getHeight(); j++) {
				Color pixelColor = new Color(original.getRGB(i, j), true);
				int rx = Math.min(255, pixelColor.getRed() + tintColor.getRed());
				int gx = Math.min(255, pixelColor.getGreen() + tintColor.getGreen());
				int bx = Math.min(255, pixelColor.getBlue() + tintColor.getBlue());
				int ax = pixelColor.getAlpha();
				
				Color finalColor = new Color(rx, gx, bx, ax);
				tintedImage.setRGB(i, j, finalColor.getRGB());
			}
		}
		return tintedImage;
	}
	
	public static BufferedImage GetTintedImage(BufferedImage original, Color tintColor, ColorBlend colorBlend) {
		//BufferedImage tintedImage = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TRANSLUCENT);
		BufferedImage tintedImage = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = tintedImage.createGraphics();
		graphics.drawImage(original, 0, 0, null);
		graphics.dispose();
		
		switch(colorBlend) {
			case Screen:
				for (int i = 0; i < original.getWidth(); i++) {
					for (int j = 0; j < original.getHeight(); j++) {
						Color pixelColor = new Color(original.getRGB(i, j), true);
						int rx = Math.min(255, pixelColor.getRed() + tintColor.getRed());
						int gx = Math.min(255, pixelColor.getGreen() + tintColor.getGreen());
						int bx = Math.min(255, pixelColor.getBlue() + tintColor.getBlue());
						int ax = pixelColor.getAlpha();
						
						Color finalColor = new Color(rx, gx, bx, ax);
						tintedImage.setRGB(i, j, finalColor.getRGB());
					}
				}
				break;
			case Multiply:
				float r = (float)tintColor.getRed() / 255f;
		    	float g = (float)tintColor.getGreen() / 255f;
		    	float b = (float)tintColor.getBlue() / 255f;
		    	float a = (float)tintColor.getAlpha() / 255f;
				
				for (int i = 0; i < original.getWidth(); i++) {
					for (int j = 0; j < original.getHeight(); j++) {
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
					}
				}
				break;
			default:
				System.err.println("GUIUtil.GetTintedImage() - Add support for: " + colorBlend.toString());
				break;
		}
		return tintedImage;
	}
	
	public static BufferedImage Mirror(BufferedImage simg) {
		//This method is premitted to receive and return null images because of its general purpose and multifaceted use. A null image doesn't necessarily indicate an error; nulls are used for weapon sockets.
		if(simg == null)
			return null;
		
		//get source image dimension
		int width = simg.getWidth();
		int height = simg.getHeight();
		//BufferedImage for mirror image
		BufferedImage mimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		//create mirror image pixel by pixel
		for(int y = 0; y < height; y++) {
			for(int lx = 0, rx = width - 1; lx < width; lx++, rx--) {
				//get source pixel value
				int p = simg.getRGB(lx, y);
				//set mirror image pixel value
				mimg.setRGB(rx, y, p);
			}
		}
		return mimg;
	}
	
	public static BufferedImage MirrorOnY(BufferedImage simg) {
		//get source image dimension
		int width = simg.getWidth();
		int height = simg.getHeight();
		//BufferedImage for mirror image
		BufferedImage mimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		//create mirror image pixel by pixel
		for(int x = 0; x < width; x++) {
			for(int ly = 0, ry = height - 1; ly < height; ly++, ry--) {
				//get source pixel value
				int p = simg.getRGB(x, ly);
				//set mirror image pixel value
				mimg.setRGB(x, ry, p);
			}
		}
		return mimg;
	}
	
	public static Point GetCornerWithInset(Point cornerDirection, Point inset) {
		Point point = new Point(Math.round(GUIManager.GetContentSize().width * (float)cornerDirection.x) + inset.x, GUIManager.GetContentSize().height * cornerDirection.y + inset.y);
		//System.out.println("GUIUtil.GetCornerWithInset() - point: " + point.toString());
		return point;
	}
	
	public static Point GetRelativePoint(float xPortion, float yPortion) {
		return new Point(Math.round(GUIManager.GetContentSize().width * xPortion), Math.round(GUIManager.GetContentSize().height * yPortion));
	}
	
	public static Dimension GetRelativeSize(float xPortion, float yPortion) {
		return new Dimension(Math.round(GUIManager.GetContentSize().width * xPortion), Math.round(GUIManager.GetContentSize().height * yPortion));
	}
	
	public static Dimension GetRelativeSize(float squarePortion, boolean baseOnWidth) {
		int size = Math.round((baseOnWidth ? GUIManager.GetContentSize().width : GUIManager.GetContentSize().height) * squarePortion);
		return new Dimension(size, size);
	}
	
	//Font Presents
	private final String fontAddress_Thaleah = "gui/ThaleahFat.ttf";
	private final String fontAddress_1980 = "gui/1980v202005.ttf";
	
	/**
	    * General purpose setting - Thaleah Fat : @100, Bold
	    * @return Font
	    */
	public static Font Title;
	/**
	    * General purpose setting - Thaleah Fat : @75
	    * @return Font
	    */
	public static Font LocationLabel;
	/**
	    * General purpose setting - Thaleah Fat : @60, Bold
	    * @return Font
	    */
	public static Font SubTitle;
	/**
	    * General purpose setting - Thaleah Fat : @60
	    * @return Font
	    */
	public static Font CombatFeedback;
	
	/**
	    * General purpose setting - Thaleah Fat : @50, Italic
	    * @return Font
	    */
	public static Font SublocationHeader;
	/**
	    * General purpose setting - Thaleah Fat : @45, Italic
	    * @return Font
	    */
	public static Font ItalicHeader_L;
	/**
	    * General purpose setting - Thaleah Fat : @38, Italic
	    * @return Font
	    */
	public static Font ItalicHeader;
	/**
	    * General purpose setting - Thaleah Fat : @38
	    * @return Font
	    */
	public static Font Header;
	
	/**
	    * General purpose setting - 1980 : @30, Italic
	    * @return Font
	    */
	public static Font Body_2_I;
	/**
	    * General purpose setting - 1980 : @30
	    * @return Font
	    */
	public static Font Body_2;
	/**
	    * General purpose setting - 1980 : @24
	    * @return Font
	    */
	public static Font Body;
	
	
	private void CreateFonts() {
		Title = CreateCustomFont(fontAddress_Thaleah, Font.BOLD, 100f);
		LocationLabel = CreateCustomFont(fontAddress_Thaleah, Font.PLAIN, 75f);
		SubTitle = CreateCustomFont(fontAddress_Thaleah, Font.BOLD, 60f);
		CombatFeedback = CreateCustomFont(fontAddress_Thaleah, Font.PLAIN, 60f);
		
		SublocationHeader = CreateCustomFont(fontAddress_Thaleah, Font.ITALIC, 50f);
		ItalicHeader_L = CreateCustomFont(fontAddress_Thaleah, Font.ITALIC, 45f);
		ItalicHeader = CreateCustomFont(fontAddress_Thaleah, Font.ITALIC, 38f);
		Header = CreateCustomFont(fontAddress_Thaleah, Font.PLAIN, 38f);
		
		Body_2_I = CreateCustomFont(fontAddress_1980, Font.ITALIC, 30f);
		Body_2 = CreateCustomFont(fontAddress_1980, Font.PLAIN, 30f);
		Body = CreateCustomFont(fontAddress_1980, Font.PLAIN, 24f);
	}
	
	private Font CreateCustomFont(String path, int style, float fontSize) {
		Font customFont = null;
		try {
		    //Create the font with a specified size
			InputStream fontStream = this.getClass().getClassLoader().getResourceAsStream("resources/" + path);
			if(fontStream == null) {
				System.err.println("File not found at: " + "resources/" + path);
				return null;
			}
			customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(style, fontSize);
		    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		    //register the font
		    ge.registerFont(customFont);
		} catch (IOException e) {
		    e.printStackTrace();
		} catch(FontFormatException e) {
		    e.printStackTrace();
		}
		return customFont;
	}
	
	//Other reused UI assets
	
	public static BufferedImage WorldmapHighlightIcon = GetBuffedImage("worldmap/Highlight.png");
	public static BufferedImage WorldmapEpicenterIcon = GetBuffedImage("worldmap/Epicenter.png");
	public static BufferedImage WorldmapCloudIcon = GetBuffedImage("worldmap/BgCloud.png");
	
	public static BufferedImage TravelArrow = GetBuffedImage("gui/TravelArrow.png");
	public static BufferedImage TravelPathArrow = GetBuffedImage("gui/TravelPathArrow.png");
	public static BufferedImage LeaveArrow = GetBuffedImage("gui/LeaveArrow.png");
	public static BufferedImage TravelOnFootSymbol = GetBuffedImage("gui/travelSymbol_onFoot.png");
	
	//Screen Capping Application
	
	//In GUIManager this is subscribed to a KeyListener event for ALT+P
	public static final void CaptureScreenshot() {
		try {
			Robot rbt = new Robot();
		    Toolkit tk = Toolkit.getDefaultToolkit();
		    Dimension dim = tk.getScreenSize();
		    BufferedImage background = rbt.createScreenCapture(new Rectangle(0, 0, (int) dim.getWidth(), (int) dim.getHeight()));
		    
		    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh;mm;ss");
		    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		    
		    File outputfile = new File("screenshots/screenshot_" + sdf.format(timestamp) + ".png");
		    outputfile.mkdirs();
		    
		    ImageIO.write(background, "png", outputfile);
		    
		    System.out.println("GUIUtil.CaptureScreenshot()");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	// <- Static & Roaming Settlement Icons -
	
	private static Map<String, BufferedImage> settlementIconPath;
	public static BufferedImage GetSettlementImage(SettlementType type, SettlementDesignation designation) {
		if(type == null) {
			System.err.println("GUIUtil.GetSettlementImage() - type cannot be equal to null. Returning null image.");
			return null;
		}
		
		String comboString = type.toString() + (designation != null ? ("_" + designation.toString()) : "");
		BufferedImage image = null;
		//System.out.println("GUIUtil.GetSettlementImage() - comboString: " + comboString);
		if(settlementIconPath.containsKey(comboString)) {
			image = settlementIconPath.get(comboString);
		} else {
			System.err.println("GUIUtil.GetSettlementImage() - SettlementDesignation doesn't exist for SettlementType: " + type.toString() + ". Defaulting to designation-less settlement image.");
			image = settlementIconPath.get(type.toString());
			if(image == null)
				System.err.println("GUIUtil.GetSettlementImage() - There is no image for SettlementType: " + type.toString() + ", image will be null!");
		}
		return image;
	}
	private static void GetSettlementIcons() {
		AddSettlementPath(SettlementType.Campsite, false);
		AddSettlementPath(SettlementType.Teahouse, false);
		
		AddSettlementPath(SettlementType.Market, true);
		
		AddSettlementPath(SettlementType.Blacksmith, false);
		AddSettlementPath(SettlementType.Doctor, false);
		AddSettlementPath(SettlementType.Crossroads, false);
		AddSettlementPath(SettlementType.NotableLocation, false);
		AddSettlementPath(SettlementType.QuestBoard, false);
		AddSettlementPath(SettlementType.Estate, false);
		
		AddSettlementPath(SettlementType.Village, true);
		
		AddSettlementPath(SettlementType.Garden, false);
		AddSettlementPath(SettlementType.Shrine, false);
		AddSettlementPath(SettlementType.Graveyard, false);
		
		AddSettlementPath(SettlementType.Castle, true);
		AddSettlementPath(SettlementType.MilitaryEncampment, true);
		AddSettlementPath(SettlementType.Battle, true);
		
		AddSettlementPath(SettlementType.AssassinationTarget, false);
		AddSettlementPath(SettlementType.ElementalDisturbance, false);
		AddSettlementPath(SettlementType.Kami, false);
		AddSettlementPath(SettlementType.YokaiActivity, false);
		AddSettlementPath(SettlementType.YokaiAttack, false);
	}
	
	private static void AddSettlementPath(SettlementType type, boolean createEachDesignation) {
		if(settlementIconPath == null)
			settlementIconPath = new HashMap<String, BufferedImage>();
		
		String prefix = null;
		if(SettlementType.IsRoamingSettlement(type))
			prefix = "worldmap/mapLocationIcons/roamingSettlements/";
		else
			prefix = "worldmap/mapLocationIcons/staticSettlements/";
		String suffix = ".png";
		
		if(!createEachDesignation) {
			String address = prefix + type.toString() + suffix;
			settlementIconPath.put(type.toString(), GetBuffedImage(address));
		} else {
			String comboString = type.toString() + "_" + SettlementDesignation.Outcaste.toString();
			settlementIconPath.put(comboString, GetBuffedImage(prefix + comboString + suffix));
			
			comboString = type.toString() + "_" + SettlementDesignation.Small.toString();
			settlementIconPath.put(comboString, GetBuffedImage(prefix + comboString + suffix));
			
			comboString = type.toString() + "_" + SettlementDesignation.Medium.toString();
			settlementIconPath.put(comboString, GetBuffedImage(prefix + comboString + suffix));
			
			comboString = type.toString() + "_" + SettlementDesignation.Large.toString();
			settlementIconPath.put(comboString, GetBuffedImage(prefix + comboString + suffix));
		}
	}
	
	// - Static & Roaming Settlement Icons ->
	
	// <- Indicator Overlay Icons -
	
	private static Map<MissionIndicatorType, BufferedImage> indicatorOverlayIconPath;
	
	private static void GetIndicatorOverlayIcons() {
		for(MissionIndicatorType indicatorType : MissionIndicatorType.values())
			AddIndicatorOverlayPath(indicatorType);
	}
	
	private static void AddIndicatorOverlayPath(MissionIndicatorType indicatorType) {
		if(indicatorOverlayIconPath == null)
			indicatorOverlayIconPath = new HashMap<MissionIndicatorType, BufferedImage>();
		
		String prefix = "worldmap/mapLocationIcons/indicatorOverlays/";
		String suffix = ".png";
		
		String address = prefix + indicatorType.toString() + "Indicator" + suffix;
		indicatorOverlayIconPath.put(indicatorType, GetBuffedImage(address));
	}
	
	public static BufferedImage GetIndicatorOverlayImage(MissionIndicatorType indicatorType) {
		if(indicatorType == null) {
			System.err.println("GUIUtil.GetIndicatorOverlayImage() - indicatorType parameter cannot be null.");
			return null;
		}
		
		BufferedImage image = null;
		if(indicatorOverlayIconPath.containsKey(indicatorType)) {
			image = indicatorOverlayIconPath.get(indicatorType);
			if(image == null)
				System.err.println("GUIUtil.GetSettlementImage() - IndicatorOverlayType: " + indicatorType + " is a null value in GUIUtil.indicatorOverlayIconPath map.");
		} else
			System.err.println("GUIUtil.GetSettlementImage() - There is no image for IndicatorOverlayType: " + indicatorType + ", image will be null!");
		return image;
	}
	
	// - Indicator Overlay Icons ->
	
	public static BufferedImage RenderPanelToPath(JComponent panel, String path)
    {
        Dimension size = panel.getSize();
        //BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
        BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        panel.paint(g2);
        try
        {
            File file = new File(path);
            file.mkdirs(); //Creates any missing directories for the file
        	ImageIO.write(image, "png", file);
            System.out.println("Panel saved as Image.");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return image;
    }
	
	//Combat Imagery Helpers
	public static BufferedImage GetCombatTile(TerrainType terrainType) {
		return GetBuffedImage("battleTiles/combatAnim/" + terrainType.toString().toLowerCase() + ".png");
	}
}
