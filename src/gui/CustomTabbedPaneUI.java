package gui;

import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.plaf.basic.BasicTabbedPaneUI;

public class CustomTabbedPaneUI extends BasicTabbedPaneUI {
	//public static ComponentUI createUI(JComponent c) {
	//	return new CustomTabbedPaneUI();
    //}
	//we can't really do anything with this cause we dont know what arguments to pass until we manually instantiate this class
	
	public CustomTabbedPaneUI(Ninecon selected, Ninecon unselected) {
		super();
		
		selectedNinecon = selected;
		unselectedNinecon = unselected;
	}
	
	/**
	* This method installs defaults for the Look and Feel.
	*/
	/*@Override
	protected void installDefaults()
	{
		LookAndFeel.installColorsAndFont(tabPane, "TabbedPane.background", "TabbedPane.foreground", "TabbedPane.font");
		tabPane.setOpaque(false);
		
		lightHighlight = UIManager.getColor("TabbedPane.highlight");
		highlight = UIManager.getColor("TabbedPane.light");
		
		shadow = UIManager.getColor("TabbedPane.shadow");
		darkShadow = UIManager.getColor("TabbedPane.darkShadow");
		
		focus = UIManager.getColor("TabbedPane.focus");
		
		textIconGap = UIManager.getInt("TabbedPane.textIconGap");
		tabRunOverlay = UIManager.getInt("TabbedPane.tabRunOverlay");
		
		tabInsets = UIManager.getInsets("TabbedPane.tabInsets");
		selectedTabPadInsets = UIManager.getInsets("TabbedPane.selectedTabPadInsets");
		tabAreaInsets = UIManager.getInsets("TabbedPane.tabAreaInsets");
		contentBorderInsets = UIManager.getInsets("TabbedPane.contentBorderInsets");
		tabsOpaque = UIManager.getBoolean("TabbedPane.tabsOpaque");
		
		// Although 'TabbedPane.contentAreaColor' is not defined in the defaults
		// of BasicLookAndFeel it is used by this class.
		selectedColor = UIManager.getColor("TabbedPane.contentAreaColor");
		if (selectedColor == null)
			selectedColor = UIManager.getColor("control");
			
		calcRect = new Rectangle();
		tabRuns = new int[10];
		tabAreaRect = new Rectangle();
		contentRect = new Rectangle();
	}*/

	private Ninecon selectedNinecon;
	private Ninecon unselectedNinecon;
	
	/**
	* This method paints the background for an individual tab.
	*
	* @param g The Graphics object to paint with.
	* @param tabPlacement The JTabbedPane's tab placement.
	* @param tabIndex The tab index.
	* @param x The x position of the tab.
	* @param y The y position of the tab.
	* @param w The width of the tab.
	* @param h The height of the tab.
	* @param isSelected Whether the tab is selected.
	*/
	@Override
	protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected)
	{
		Ninecon currentNinecon = null;
		//Insets currentInsets = this.getTabInsets(tabPlacement, tabIndex);
		Insets currentInsets = new Insets(0,0,0,0);
		//Draw "Text Bubble" image thats been modified to open downward
		if (isSelected) {
			currentNinecon = selectedNinecon;
			//Shift the image downward to make it up to the content panel below
			y += 10;
		}
		else //Draw the ordinary "Text Bubble"
		{
			currentNinecon = unselectedNinecon;
		}
		currentNinecon.paintIconWithoutComponent(currentInsets, g, x, y, w, h);
	}
}
