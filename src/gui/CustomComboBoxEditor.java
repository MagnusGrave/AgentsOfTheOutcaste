package gui;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import enums.ClassType;

public class CustomComboBoxEditor extends BasicComboBoxEditor {
	private List<ClassType> classList = Arrays.asList(new ClassType[] { ClassType.RONIN, ClassType.NINJA, ClassType.MONK, ClassType.BANDIT });
	
	@Override
	protected JTextField createEditorComponent() {
		JTextField textField = super.createEditorComponent();
		
		//System.out.println("CustomComboBoxEditor.createEditorComponent()");
		
		//Change bg color
		textField.setBackground(Color.WHITE);
		
		//This allows the editor to be used for the combo box while also denying interference with the text via user input
		textField.setEditable(false);
		
		return textField;
	}
	
	@Override
    public void setItem(Object anObject) {
		if(classList.contains(anObject)) {
			//System.out.println("SetItem() - Setting Valid Item");
			super.setItem(anObject);
		} else {
			//System.out.println("SetItem() - Rejecting Invalid Item");
		}
    }
}
