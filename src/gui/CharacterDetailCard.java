package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

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
 * Used to display info about characters in the Character Panel and possibly other places where it's helpful to see characters' finite stats.
 * @author Magnus
 *
 */
@SuppressWarnings("serial")
public class CharacterDetailCard extends JPanel {
	ImagePanel headshot;
	
	JFxLabel nameLabel;
	
	JFxLabel classLabel;
	JFxLabel lvlLabel;
	
	JFxLabel weaponTypeLabel;
	JFxLabel armorSetLabel;
	
	Dimension hpBarSize;
	JFxLabel hpLabel;
	JLabel hpBar;
	//JLabel expLabel;
	//JLabel strLabel;
	//JLabel intLabel;
	//JLabel endLabel;
	//JLabel spdLabel;
	
	//JLabel attLabel;
	//JLabel apLabel;
	//JLabel armorLabel;
	//JLabel moveLabel;
	
	
	public CharacterDetailCard(Dimension cardSize) {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JPanel info = new JPanel();
		info.setLayout(new BoxLayout(info, BoxLayout.X_AXIS));
			
			headshot = new ImagePanel("portraits/Guard.png");
			info.add(headshot);
			
			JPanel infoGrid = new JPanel(new GridLayout(4, 1));
				
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
			
		this.add(info);
		
		hpBarSize = new Dimension(cardSize.width, 15);
		JLayeredPane hpLayeredPane = new JLayeredPane();
		hpLayeredPane.setSize(hpBarSize);
		hpLayeredPane.setPreferredSize(hpBarSize);
		hpLayeredPane.setMinimumSize(hpBarSize);
		hpLayeredPane.setMaximumSize(hpBarSize);
		hpLayeredPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		
			//HP Bar
			hpBar = new JLabel(".");
			hpBar.setSize(hpBarSize.width, hpBarSize.height);
			hpBar.setMaximumSize(hpBar.getSize());
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
		
		this.add(hpLayeredPane);
		
		this.setPreferredSize(cardSize);
	}
	
	CharacterData currentCharacter;
	
	public void DisplayCharacter(CharacterData charData) {
		currentCharacter = charData;
		
		if(charData.getPortraitPath() == null || charData.getPortraitPath().equals(""))
			headshot.SetNewImage(SpriteSheetUtility.GetWalkSheet(charData.getType()).GetSprite(1, 1, 1));
		else
			headshot.SetNewImage(charData.getPortraitPath());
		nameLabel.setText(charData.getName());
		classLabel.setText(charData.getType().toString());
		
		/*hpLabel.setText("" + charData.GetHp());
		expLabel.setText("" + charData.GetExp());
		strLabel.setText("" + charData.getStrength());
		intLabel.setText("" + charData.getIntellect());
		endLabel.setText("" + charData.getEndurance());
		spdLabel.setText("" + charData.getSpeed());
		
		attLabel.setText("" + charData.GetAttack());
		attLabel.setForeground(Color.BLACK);
		
		apLabel.setText("" + charData.GetAp());
		
		armorLabel.setText("" + charData.GetArmor());
		armorLabel.setForeground(Color.BLACK);
		
		moveLabel.setText("" + charData.GetMoveRange());*/
		
		lvlLabel.setText("Lvl " + charData.Level());
		
		weaponTypeLabel.setText(GetWeaponTypeText(charData));
		armorSetLabel.setText(charData.GetArmorClassification());
		
		hpLabel.setText("" + charData.GetHp() + " / " + charData.GetHp());
		float hpPercentage = (float)charData.GetHp() / charData.GetHp();
		hpBar.setSize(Math.round(hpBarSize.width * hpPercentage), hpBarSize.height);
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
		/*hpLabel.setText("" + charBase.GetHp());
		expLabel.setText("" + charBase.GetExp());
		strLabel.setText("" + currentCharacter.getStrength());
		intLabel.setText("" + currentCharacter.getIntellect());
		endLabel.setText("" + currentCharacter.getEndurance());
		spdLabel.setText("" + currentCharacter.getSpeed());
		
		attLabel.setText("" + currentCharacter.GetAttack());
		attLabel.setForeground(Color.BLACK);
		
		apLabel.setText("" + charBase.GetAp());
		
		armorLabel.setText("" + currentCharacter.GetArmor());
		armorLabel.setForeground(Color.BLACK);
		
		moveLabel.setText("" + currentCharacter.GetMoveRange());*/
		
		lvlLabel.setText("Lvl " + charBase.GetData().Level());
		
		weaponTypeLabel.setText(GetWeaponTypeText(charBase.GetData()));
		armorSetLabel.setText(charBase.GetData().GetArmorClassification());

		hpLabel.setText("" + charBase.GetHp() + " / " + charBase.getMaxHp());
		float hpPercentage = (float)charBase.GetHp() / charBase.getMaxHp();
		hpBar.setSize(Math.round(hpBarSize.width * hpPercentage), hpBarSize.height);
	}
	
	private String GetWeaponTypeText(CharacterData charData) {
		ItemData rightHandWeapon = charData.GetAt(EquipmentType.RightHand.getValue());
		if(rightHandWeapon == null)
			return "Unarmed";
		else
			return rightHandWeapon.getStats().GetBattleToolTraits().weaponTraits.weaponType.toString();
	}
	
	private final Color positiveColor = Color.GREEN;
	private final Color negativeColor = Color.RED;
	
	public void SimulateEquipmentStats(int slotIndex, ItemData simItem) {
		CharacterData clone = new CharacterData(currentCharacter);
		
		if(clone.IsItemExistingAt(slotIndex))
			clone.ReturnItemAtIndex(slotIndex);
		clone.SetItemAtIndex(slotIndex, simItem);

		/*if(clone.GetAttack() > currentCharacter.GetAttack()) {
			attLabel.setForeground(positiveColor);
			attLabel.setText("" + clone.GetAttack() + " +");
		} else if (clone.GetAttack() < currentCharacter.GetAttack()) {
			attLabel.setForeground(negativeColor);
			attLabel.setText("" + clone.GetAttack() + " -");
		} else {
			attLabel.setForeground(Color.BLACK);
			attLabel.setText("" + clone.GetAttack());
		}
		
		if(clone.GetArmor() > currentCharacter.GetArmor()) {
			armorLabel.setForeground(positiveColor);
			armorLabel.setText("" + clone.GetArmor() + " +");
		} else if (clone.GetArmor() < currentCharacter.GetArmor()) {
			armorLabel.setForeground(negativeColor);
			armorLabel.setText("" + clone.GetArmor() + " -");
		} else {
			armorLabel.setForeground(Color.BLACK);
			armorLabel.setText("" + clone.GetArmor());
		}*/
		
		
	}
}