package gui;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicComboBoxUI;

public class CustomComboBoxUI extends BasicComboBoxUI {
	public static ComponentUI createUI(JComponent c) {
		return new CustomComboBoxUI();
    }
	
	public CustomComboBoxUI() {
		super();
	}

	@Override
    protected JButton createArrowButton() {
    	JButton button = new CustomButton(SpriteSheetUtility.DownArrowSymbol(), null, Color.GREEN, SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(), Color.WHITE);
    	return button;
    }
}
