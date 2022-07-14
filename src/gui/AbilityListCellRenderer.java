package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import gameLogic.AbilityManager.Ability;

@SuppressWarnings({ "serial" })
public class AbilityListCellRenderer extends JFxLabel implements ListCellRenderer<Object> {
	private final static int swingConts_horizAlignment = SwingConstants.LEFT;
	private final static Font font = GUIUtil.Body_2;
	private final static Color color = Color.BLACK;
	private final static Color bgColor = SpriteSheetUtility.ValueBGColor;
	
	private final Color selectedColor = Color.LIGHT_GRAY;
	private Color originalBgColor;
	
	//public AbilityListCellRenderer(int swingConts_horizAlignment, Font font, Color color, Color bgColor) {
	public AbilityListCellRenderer() {
		
		super("", swingConts_horizAlignment, font, color);
		setOpaque(true);
		originalBgColor = bgColor;
		setBackground(bgColor);
	}
	
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Ability ability = (Ability)value;
        setText(ability.name);
        if(isSelected) {
        	setBackground(selectedColor);
        } else {
        	setBackground(originalBgColor);
        }
        return this;
    }
}