package gui;

import java.awt.Graphics;

import javax.swing.JButton;

@SuppressWarnings("serial")
public class ButtonCover extends JButton {
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		//Cover up default button display
		g.setColor(getBackground());
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
	}
}
