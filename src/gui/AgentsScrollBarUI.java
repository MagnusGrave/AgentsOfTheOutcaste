package gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class AgentsScrollBarUI extends BasicScrollBarUI {
	public static ComponentUI createUI(JComponent c) {
        return new AgentsScrollBarUI();
    }
	
	//Hide Arrow Buttons
	@Override
    protected JButton createDecreaseButton(int orientation) {
        return createZeroButton();
    }
    @Override    
    protected JButton createIncreaseButton(int orientation) {
        return createZeroButton();
    }
    private JButton createZeroButton() {
        JButton jbutton = new JButton();
        jbutton.setPreferredSize(new Dimension(0, 0));
        jbutton.setMinimumSize(new Dimension(0, 0));
        jbutton.setMaximumSize(new Dimension(0, 0));
        return jbutton;
    }
	
    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
    	//g.drawImage(SpriteSheetUtility.Scroll_Track(), trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height, null);
    	SpriteSheetUtility.Scroll_Track().paintIconWithoutComponent(new Insets(0, 0, 0, 0), g, trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
    }
    
    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
    	//g.drawImage(SpriteSheetUtility.Scroll_Thumb(), thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, null);
    	SpriteSheetUtility.Scroll_Thumb().paintIconWithoutComponent(new Insets(0, 1, 0, 1), g, thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height);
    }
}