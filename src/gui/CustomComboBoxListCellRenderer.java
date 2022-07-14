package gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

@SuppressWarnings("serial")
public class CustomComboBoxListCellRenderer extends DefaultListCellRenderer {
	
	public CustomComboBoxListCellRenderer() {
		super();
	}
	
	@Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (c instanceof JLabel) {
            JLabel l = (JLabel) c;
            //if (isSelected) {
                list.setSelectionBackground(Color.WHITE);
                list.setBackground(Color.LIGHT_GRAY);
            //} 
            return l;
        }
        return c;
    }
}