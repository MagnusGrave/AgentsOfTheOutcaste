package gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import data.CharacterData;
import gameLogic.AbilityManager;
import gameLogic.AbilityManager.Ability;


@SuppressWarnings("serial")
public class CharacterDescPanel extends JPanel {
	private CharacterData characterData;
	//Left Panel Dynamic Elements
	private CharacterDetailCard card;
	public CharacterDetailCard Card() { return card; }
	
	private IItemSocketViewer socketViewer;
	
	//Equipment Tab
	private ArrayList<JButton> equipmentButtons = new ArrayList<JButton>(); //Right Hand, Left Hand, Head, Accessory, Armor, Feet
	//Skill Tree Tab
	private ArrayList<JButton> abilityTreeButtons = new ArrayList<JButton>();
	
	private int openItemSlot;
	
	
	
	public CharacterDescPanel(IItemSocketViewer socketViewer) {
		this.socketViewer = socketViewer;
		
		//Setup Left Panel
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		Dimension cardSize = GUIUtil.GetRelativeSize(0.2f, 0.15f);
		card = new CharacterDetailCard(cardSize);
		this.add(card);
		
		JTabbedPane managementTabs = new JTabbedPane();
		
			//Right Hand, Left Hand, Head, Accessory, Armor, Feet
			JPanel equipmentGridBag = new JPanel(new GridBagLayout());
			GridBagConstraints eqC = new GridBagConstraints();
			eqC.fill = GridBagConstraints.HORIZONTAL;
			
				Box slotBox = Box.createVerticalBox();
					JLabel slotLabel = new JLabel("R Hand", SwingConstants.CENTER);
					slotBox.add(slotLabel);
					JButton rightHandButton = new JButton();
					eqC.weightx = 1;
					eqC.weighty = 1;
					eqC.gridx = 0;
					eqC.gridy = 3;
					rightHandButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							ChooseItem(rightHandButton, 0);
						}
					});
					equipmentButtons.add(rightHandButton);
					slotBox.add(rightHandButton);
				equipmentGridBag.add(slotBox, eqC);

				eqC.gridx = 1;
				eqC.gridy = 3;
				equipmentGridBag.add(Box.createRigidArea(new Dimension(50, 50)), eqC);
				
				slotBox = Box.createVerticalBox();
					slotLabel = new JLabel("Headware", SwingConstants.CENTER);
					slotBox.add(slotLabel);
					JButton headButton = new JButton();
					eqC.gridx = 2;
					eqC.gridy = 0;
					headButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							ChooseItem(headButton, 1);
						}
					});
					equipmentButtons.add(headButton);
					slotBox.add(headButton);
				equipmentGridBag.add(slotBox, eqC);
				
				eqC.gridx = 2;
				eqC.gridy = 1;
				equipmentGridBag.add(Box.createRigidArea(new Dimension(50, 50)), eqC);
				
				slotBox = Box.createVerticalBox();
					slotLabel = new JLabel("Accessory", SwingConstants.CENTER);
					slotBox.add(slotLabel);
					JButton accessoryButton = new JButton();
					eqC.gridx = 2;
					eqC.gridy = 2;
					accessoryButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							ChooseItem(accessoryButton, 2);
						}
					});
					equipmentButtons.add(accessoryButton);
					slotBox.add(accessoryButton);
				equipmentGridBag.add(slotBox, eqC);
				
				slotBox = Box.createVerticalBox();
					slotLabel = new JLabel("Clothing", SwingConstants.CENTER);
					slotBox.add(slotLabel);
					JButton clothingButton = new JButton();
					eqC.gridx = 2;
					eqC.gridy = 4;
					clothingButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							ChooseItem(clothingButton, 3);
						}
					});
					equipmentButtons.add(clothingButton);
					slotBox.add(clothingButton);
				equipmentGridBag.add(slotBox, eqC);
				
				eqC.gridx = 2;
				eqC.gridy = 5;
				equipmentGridBag.add(Box.createRigidArea(new Dimension(50, 50)), eqC);
				
				slotBox = Box.createVerticalBox();
					slotLabel = new JLabel("Footware", SwingConstants.CENTER);
					slotBox.add(slotLabel);
					JButton feetButton = new JButton();
					eqC.gridx = 2;
					eqC.gridy = 6;
					feetButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							ChooseItem(feetButton, 4);
						}
					});
					equipmentButtons.add(feetButton);
					slotBox.add(feetButton);
				equipmentGridBag.add(slotBox, eqC);
				
				eqC.gridx = 3;
				eqC.gridy = 3;
				equipmentGridBag.add(Box.createRigidArea(new Dimension(50, 50)), eqC);
				
				slotBox = Box.createVerticalBox();
					slotLabel = new JLabel("Left Hand", SwingConstants.CENTER);
					slotBox.add(slotLabel);
					JButton leftHandButton = new JButton();
					eqC.gridx = 4;
					eqC.gridy = 3;
					leftHandButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							ChooseItem(leftHandButton, 5);
						}
					});
					equipmentButtons.add(leftHandButton);
					slotBox.add(leftHandButton);
				equipmentGridBag.add(slotBox, eqC);
			
			managementTabs.addTab("Equipment", null, equipmentGridBag, "Customize this character's equipment.");
		
			JPanel skillTreeGridBag = new JPanel(new GridBagLayout());
			GridBagConstraints skC = new GridBagConstraints();
			skC.fill = GridBagConstraints.HORIZONTAL;
			
				JButton path1 = new JButton("P1.0");
				skC.gridx = 2;
				skC.gridy = 1;
				abilityTreeButtons.add(path1);
				skillTreeGridBag.add(path1, skC);
				
				JButton path1_2a = new JButton("P1.A1");
				skC.gridx = 1;
				skC.gridy = 3;
				abilityTreeButtons.add(path1_2a);
				skillTreeGridBag.add(path1_2a, skC);
				
				JButton path1_3a = new JButton("P1.A2");
				skC.gridx = 1;
				skC.gridy = 5;
				abilityTreeButtons.add(path1_3a);
				skillTreeGridBag.add(path1_3a, skC);
				
				JButton path1_2b = new JButton("P1.B1");
				skC.gridx = 3;
				skC.gridy = 3;
				abilityTreeButtons.add(path1_2b);
				skillTreeGridBag.add(path1_2b, skC);
				
				JButton path1_3b = new JButton("P1.B2");
				skC.gridx = 3;
				skC.gridy = 5;
				abilityTreeButtons.add(path1_3b);
				skillTreeGridBag.add(path1_3b, skC);
			
			managementTabs.addTab("Skill Tree", null, skillTreeGridBag, "Choose this character's skills.");
			
		managementTabs.setPreferredSize(new Dimension(800, 600));
		this.add(managementTabs);
	}
	
	public void DisplayCharacter(CharacterData characterData) {
		this.characterData = characterData;
		
		//Update all UI components for the newly selected character
		card.DisplayCharacter(characterData);
		
		for(int i = 0; i < equipmentButtons.size(); i++) {
			String text = "[NONE]";
			if(characterData.IsItemExistingAt(i))
				text = characterData.GetAt(i).getName();
			equipmentButtons.get(i).setText(text);
		}
		
		/*String[] abilityTree = characterData.getAbilityTree();
		for(int i = 0; i < abilityTreeButtons.size(); i++) {
			String text = ClassType.GetAbilityTree(characterData.getType())[i];
			if(abilityTree != null && i < abilityTree.length && abilityTree[i] != null)
				text += " (L)";
			abilityTreeButtons.get(i).setText(text);
		}*/
		List<Boolean> abilityTree = characterData.getAbilitiesNodeLearnedStatusTree();
		for(int i = 0; i < abilityTreeButtons.size(); i++) {
			String text = "";
			for(Ability ability : AbilityManager.GetAbilitiesNodeTree(characterData.getType())[i].abilities) {
				text += (text == "" ? "" : ", ") + ability.name;
			}
			if(abilityTree.get(i))
				text += " (L)";
			else {
				final int finalI = i;
				abilityTreeButtons.get(i).addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						characterData.LearnAbilitiesNode(finalI);
						abilityTreeButtons.get(finalI).removeActionListener(this);
					}
				});
			}
			abilityTreeButtons.get(i).setText(text);
		}
	}
	
	private void ChooseItem(JButton button, int index) {
		if(openItemSlot > -1)
			SumbitEquipmentChoice(index);
		
		openItemSlot = index;
		socketViewer.StartEquipmentSelection(openItemSlot);
		button.setEnabled(false);
	}
	
	//Called by PartyPanel once a new equipment is chosen
	public void SumbitEquipmentChoice(int slotIndex) {
		if(characterData.IsItemExistingAt(slotIndex))
			equipmentButtons.get(slotIndex).setText( characterData.GetAt(slotIndex).getName() );
		else
			equipmentButtons.get(slotIndex).setText( "[NONE]" );
		equipmentButtons.get(slotIndex).setEnabled(true);
		openItemSlot = -1;
		
		card.DisplayCharacter(this.characterData);
	}
}
