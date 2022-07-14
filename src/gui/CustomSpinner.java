package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.plaf.SpinnerUI;


@SuppressWarnings("serial")
public class CustomSpinner extends JSpinner {
	//private JPanel expansionPanel;
	//This is what subscribers should add to the UI heirarchy, instead of the actual CustomSpinner component itself
	//public JPanel getWrapper() { return expansionPanel; }
	private JLayeredPane layeredPane;
	public JLayeredPane getWrapper() { return layeredPane; }
	
	private Font font;
	public Font getFont() { return font; }

	CustomSpinner(SpinnerNumberModel numberModel, Font font) {
		super(numberModel);
		
		layeredPane = new JLayeredPane();
		layeredPane.setSize(GUIUtil.GetRelativeSize(0.16f, 0.08f));
		
		this.setSize(GUIUtil.GetRelativeSize(0.141f, 0.049f));
		Point location = GUIUtil.GetRelativePoint(0.006f, 0.012f);
		this.setLocation(location);
		
		//In this case, replacing the editor was ineffective
		
		this.setUI( (SpinnerUI)CustomSpinnerUI.createUI(this) );
		
		this.setBorder(null);
		this.setBackground(Color.WHITE);
		
		JFormattedTextField field = ((JSpinner.DefaultEditor)this.getEditor()).getTextField();
		field.setEditable(false);
		field.setHorizontalAlignment(JTextField.CENTER);
		field.setFont(font);
		field.setBackground(Color.WHITE);
		
		layeredPane.add(this, 0);
		
		
		//add bg layer
		JLabel BG = new JLabel(SpriteSheetUtility.FieldNinecon());
		BG.setSize(GUIUtil.GetRelativeSize(0.154f, 0.07f));
		layeredPane.add(BG, 1);
	}
}
