package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSpinnerUI;

public class CustomSpinnerUI extends BasicSpinnerUI {
	public static ComponentUI createUI(JComponent c) {
		return new CustomSpinnerUI();
    }
	
	@Override
	protected Component createNextButton() {
		CustomButton customButton = new CustomButton(SpriteSheetUtility.UpArrowSymbol(), null, Color.GREEN, SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(), Color.WHITE);
		
		Dimension buttSize = GUIUtil.GetRelativeSize(0.015f, true);
		customButton.setSize(buttSize);
		customButton.setPreferredSize(buttSize);
		
		installNextButtonListeners(customButton);
		
		return customButton;
	}
	
	@Override
	protected Component createPreviousButton() {
		CustomButton customButton = new CustomButton(SpriteSheetUtility.DownArrowSymbol(), null, Color.GREEN, SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(), Color.WHITE);
		
		Dimension buttSize = GUIUtil.GetRelativeSize(0.015f, true);
		customButton.setSize(buttSize);
		customButton.setPreferredSize(buttSize);
		
		installPreviousButtonListeners(customButton);
		
		return customButton;
	}
}
