package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import gameLogic.Game.TargetInfo;

@SuppressWarnings("serial")
public class TargetInfoPanel extends JLayeredPane {
	private JLabel targetInfoPanelBG;
	private JPanel targetInfoPanel;
	private JFxLabel targetInfoDamage;
	private JFxLabel targetInfoHitChance;
	
	public TargetInfoPanel(Dimension panelSize) {
		//Point targetInfoPanelBGLoc = GUIUtil.GetRelativePoint(0.015f, 0.17f);
		//Dimension targetInfoPanelBGSize = GUIUtil.GetRelativeSize(0.2f, 0.05f);
		//Point targetInfoPanelLoc = GUIUtil.GetRelativePoint(0.025f, 0.17f);
		//Dimension targetInfoPanelSize = GUIUtil.GetRelativeSize(0.18f, 0.05f);
		Point targetInfoPanelLoc = new Point(Math.round(panelSize.width * 0.05f), Math.round(panelSize.height * 0.05f));
		Dimension targetInfoPanelSize = new Dimension(Math.round(panelSize.width * 0.9f), Math.round(panelSize.height * 0.9f));
		
		
		targetInfoPanelBG = new JLabel(SpriteSheetUtility.ValueBGNinecon());
		//targetInfoPanelBG.setBounds(targetInfoPanelBGLoc.x, targetInfoPanelBGLoc.y, targetInfoPanelBGSize.width, targetInfoPanelBGSize.height);
		targetInfoPanelBG.setSize(panelSize);
		targetInfoPanelBG.setPreferredSize(panelSize);
		//targetInfoPanelBG.setVisible(false);
		add(targetInfoPanelBG, 0, 0);
		
		targetInfoPanel = new JPanel(new GridLayout(1, 2));
		targetInfoPanel.setOpaque(false);
		targetInfoPanel.setBackground(new Color(0,0,0,0));
		//targetInfoPanel.setBounds(targetInfoPanelLoc.x, targetInfoPanelLoc.y, targetInfoPanelSize.width, targetInfoPanelSize.height);
		targetInfoPanel.setLocation(targetInfoPanelLoc.x, targetInfoPanelLoc.y);
		targetInfoPanel.setSize(targetInfoPanelSize.width, targetInfoPanelSize.height);
		//targetInfoPanel.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
		
		//targetInfoPanel.setVisible(false);
	
			targetInfoDamage = new JFxLabel("Damage", null, SwingConstants.LEFT, GUIUtil.Body, Color.BLACK);
			targetInfoPanel.add(targetInfoDamage);
			//targetInfoDamage.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
	
			targetInfoHitChance = new JFxLabel("Chance", null, SwingConstants.LEFT, GUIUtil.Body, Color.BLACK);
			targetInfoPanel.add(targetInfoHitChance);
			//targetInfoHitChance.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
	
		add(targetInfoPanel, 0, 0);
	}
	
	public void DisplayInfo(TargetInfo targetInfo) {
		if(targetInfo.combatCalcInfo.healthModInfo.isHealing) {
			targetInfoDamage.setText("+ " + targetInfo.combatCalcInfo.healthModInfo.amount + " HP");
			targetInfoDamage.setForeground(Color.BLUE);
		} else {
			targetInfoDamage.setText("- " + targetInfo.combatCalcInfo.healthModInfo.amount + " HP");
			targetInfoDamage.setForeground(Color.RED);
		}
		targetInfoHitChance.setText(Math.round(targetInfo.chanceToHit * 100) + "% Chance");
		//Conform our 0%-100% to a 0.0-0.68 range. 0.68 is the point on the spectrum with the blue-est blue.
		double rangeAdjustedChance = Math.min(targetInfo.chanceToHit * 0.68, 0.68);
		targetInfoHitChance.setForeground(Color.getHSBColor((float)rangeAdjustedChance, 1f, 0.9f));
	}
}
