package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import data.CharacterData;
import data.ItemData;
import enums.EquipmentType;
import gameLogic.CharacterBase;

/**
 * Used to display info about characters during the course of Battle.
 * @author Magnus
 *
 */
@SuppressWarnings("serial")
public class CharacterCard extends JLayeredPane {
	ImagePanel headshot;
	
	JFxLabel nameLabel;
	
	JFxLabel classLabel;
	JFxLabel lvlLabel;
	
	JFxLabel weaponTypeLabel;
	JFxLabel armorSetLabel;
	
	Dimension hpBarFillSize;
	JFxLabel hpLabel;
	JLabel hpBar;

	CharacterData currentCharacter;
	
	
	
	public CharacterCard(Point cardLocation, Dimension cardSize) {
		//Implied construction of this JLayeredPane
		super();
		
			JLabel bgImage = new JLabel(SpriteSheetUtility.HighlightBGNinecon());
			bgImage.setPreferredSize(cardSize);
			bgImage.setBounds(0, 0, cardSize.width, cardSize.height);
		
		this.add(bgImage, 0, 0);
		
			int insetPixels = 12;
			Dimension contentSize = new Dimension(cardSize.width - (insetPixels * 2), cardSize.height - (insetPixels * 2));
			
			Dimension hpBarSize = new Dimension(cardSize.width, 18);
		
			JPanel infoPanel = new JPanel();
			infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
			infoPanel.setBackground(Color.WHITE);
			
				JPanel info = new JPanel();
				info.setLayout(new BoxLayout(info, BoxLayout.X_AXIS));
				info.setBackground(Color.WHITE);
					
					int portraitHeight = contentSize.height - hpBarSize.height;
					Dimension portraitSize = new Dimension(Math.round(portraitHeight * (63f/79f)), portraitHeight);
					headshot = new ImagePanel("portraits/Guard.png");
					headshot.setBackground(Color.LIGHT_GRAY);
					headshot.setPreferredSize(portraitSize);
					headshot.setMaximumSize(portraitSize);
					headshot.ConformPreferredSizeToAspectRatio(false);
					headshot.SetPaintInsideInsets(true);
					headshot.setBorder(BorderFactory.createLineBorder(Color.GRAY, 4));
					
				info.add(headshot);
				info.add(Box.createHorizontalStrut(6));
				
					JPanel infoGrid = new JPanel(new GridLayout(4, 1));
					infoGrid.setBackground(Color.WHITE);
						
						nameLabel = new JFxLabel("Name", null, SwingConstants.LEFT, GUIUtil.Header, Color.BLACK);
						infoGrid.add(nameLabel);
						
						Box classAndLevelBox = Box.createHorizontalBox();
							classLabel = new JFxLabel("Class", null, SwingConstants.LEFT, GUIUtil.Body, Color.BLACK);
							classAndLevelBox.add(classLabel);
							classAndLevelBox.add(Box.createHorizontalGlue());
							lvlLabel = new JFxLabel("Lvl. X", null, SwingConstants.LEFT, GUIUtil.Body, Color.BLACK);
							classAndLevelBox.add(lvlLabel);
							classAndLevelBox.add(Box.createHorizontalStrut(10));
						infoGrid.add(classAndLevelBox);
						
						weaponTypeLabel = new JFxLabel("Weapon Type", null, SwingConstants.LEFT, GUIUtil.Body, Color.BLACK);
						infoGrid.add(weaponTypeLabel);
						
						armorSetLabel = new JFxLabel("Armor Set", null, SwingConstants.LEFT, GUIUtil.Body, Color.BLACK);
						
					infoGrid.add(armorSetLabel);
					
				info.add(infoGrid);
				info.setPreferredSize(new Dimension(-1, 105));
				
			infoPanel.add(info);
			infoPanel.add(Box.createVerticalStrut(4));
			
				JLayeredPane hpLayeredPane = new JLayeredPane();
				hpLayeredPane.setSize(hpBarSize);
				hpLayeredPane.setPreferredSize(hpBarSize);
				hpLayeredPane.setMinimumSize(hpBarSize);
				hpLayeredPane.setMaximumSize(hpBarSize);
				hpLayeredPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
					
					//HP Bar
					hpBarFillSize = new Dimension(hpBarSize.width-2, hpBarSize.height-2);
					hpBar = new JLabel();
					hpBar.setLocation(1, 1);
					hpBar.setSize(hpBarFillSize);
					hpBar.setMaximumSize(hpBarFillSize);
					hpBar.setBackground(Color.RED);
					hpBar.setOpaque(true);
				hpLayeredPane.add(hpBar, 0, 0);
				
					//HP / Max HP numbers
					hpLabel = new JFxLabel("X / X", null, SwingConstants.LEFT, GUIUtil.Body, Color.BLACK);
					hpLabel.setSize(hpBarSize);
					hpLabel.setMinimumSize(hpBarSize);
					hpLabel.setPreferredSize(hpBarSize);
					hpLabel.setMaximumSize(hpBarSize);
					hpLabel.setBackground(new Color(0,0,0,0));
					hpLabel.setOpaque(false);
					hpLabel.setHorizontalAlignment(SwingConstants.CENTER);
				hpLayeredPane.add(hpLabel, 0, 0);
			
			infoPanel.add(hpLayeredPane);
			
			infoPanel.setPreferredSize(contentSize);
			infoPanel.setBounds(insetPixels, insetPixels, contentSize.width, contentSize.height);
			
		this.add(infoPanel, 0, 0);
		
		this.setPreferredSize(cardSize);
		this.setBounds(cardLocation.x, cardLocation.y, cardSize.width, cardSize.height);
	}
	
	public void DisplayCharacter(CharacterData charData) {
		currentCharacter = charData;
		
		if(charData.getPortraitPath() == null || charData.getPortraitPath().equals(""))
			headshot.SetNewImage(SpriteSheetUtility.GetWalkSheet(charData.getType()).GetSprite(1, 1, 1));
		else
			headshot.SetNewImage(charData.getPortraitPath());
		nameLabel.setText(charData.getName());
		classLabel.setText(charData.getType().toString());
		
		lvlLabel.setText("Lvl " + charData.Level());
		
		weaponTypeLabel.setText(GetWeaponTypeText(charData));
		armorSetLabel.setText(charData.GetArmorClassification());
		
		hpLabel.setText("" + charData.GetHp() + " / " + charData.GetHp());
		float hpPercentage = (float)charData.GetHp() / charData.GetHp();
		hpBar.setSize(Math.round(hpBarFillSize.width * hpPercentage), hpBarFillSize.height);
	}
	
	//Alternate version used during combat to show current HP and stuff
	public void DisplayCharacter(CharacterBase charBase) {
		currentCharacter = charBase.GetData();
		
		if(currentCharacter.getPortraitPath() == null || currentCharacter.getPortraitPath().equals(""))
			headshot.SetNewImage(SpriteSheetUtility.GetWalkSheet(currentCharacter.getType()).GetSprite(1, 1, 1));
		else
			headshot.SetNewImage(currentCharacter.getPortraitPath());
		nameLabel.setText(currentCharacter.getName());
		classLabel.setText(currentCharacter.getType().toString());
		
		lvlLabel.setText("Lvl " + charBase.GetData().Level());
		
		weaponTypeLabel.setText(GetWeaponTypeText(charBase.GetData()));
		armorSetLabel.setText(charBase.GetData().GetArmorClassification());

		hpLabel.setText("" + charBase.GetHp() + " / " + charBase.getMaxHp());
		float hpPercentage = (float)charBase.GetHp() / charBase.getMaxHp();
		hpBar.setSize(Math.round(hpBarFillSize.width * hpPercentage), hpBarFillSize.height);
	}
	
	private String GetWeaponTypeText(CharacterData charData) {
		ItemData rightHandWeapon = charData.GetAt(EquipmentType.RightHand.getValue());
		if(rightHandWeapon == null)
			return "Unarmed";
		else
			return rightHandWeapon.getStats().GetBattleToolTraits().weaponTraits.weaponType.toString();
	}
}
