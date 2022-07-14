package gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import data.CharacterData;
import data.ItemData;
import enums.EquipmentType;
import enums.PanelTemplateType;
import gameLogic.*;


@SuppressWarnings("serial")
public class PartyPanel extends JPanel implements IItemSocketViewer, IRefreshable {
	int selectedIndex;
	CharacterDescPanel characterDesc;
	ItemDescPanel itemDesc;
	JList<CharacterData> charList;
	
	
	CardLayout rightCardLayout = new CardLayout();
	JPanel rightContainer = new JPanel();
	
	JButton dismissCharacterButton;
	
	//Testing - Add items to inventory manually
	CharacterData[] manualCharacters = new CharacterData[] {
		CharacterData.CreateRandom(),
		CharacterData.CreateRandom(),
		CharacterData.CreateRandom(),
		CharacterData.CreateRandom(),
		CharacterData.CreateRandom(),
		CharacterData.CreateRandom()
	};
	
	
	
	public PartyPanel() {
		super(new GridLayout(1, 2, 2, 2));
		
		//Left Panel
		characterDesc = new CharacterDescPanel(this);
		this.add(characterDesc);
		
		//Right CardLayout Panel
		rightContainer.setLayout(rightCardLayout);
		
			//Testing - Add items to inventory manually
			JPanel debugPanel = new JPanel(new BorderLayout());
			
				JButton debugButton = new JButton("Add Characters (TESTING)");
				debugButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						for(CharacterData newChar : manualCharacters)
							Game.Instance().AddTeammate(newChar);
						Refresh();
					}
				});
				debugPanel.add(debugButton, BorderLayout.NORTH);
			
				//Character List
				charList = new JList<CharacterData>();
					charList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					charList.setLayoutOrientation(JList.VERTICAL);
					charList.setVisibleRowCount(-1);
					charList.addListSelectionListener(new ListSelectionListener() {
						public void valueChanged(ListSelectionEvent e) {
						    if (e.getValueIsAdjusting() == false) {
						    	if (charList.getSelectedIndex() != -1)
						        	OnElementSelection(charList.getSelectedIndex());
						    	
						    	dismissCharacterButton.setVisible(charList.getSelectedIndex() > 0);
						    }
						}
					});
				JScrollPane listScroller = new JScrollPane(charList);
				listScroller.setPreferredSize(new Dimension(250, 80));
				debugPanel.add(listScroller, BorderLayout.CENTER);
				
				dismissCharacterButton = new JButton("Dismiss Character");
				dismissCharacterButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Game.Instance().RemoveTeammate(Game.Instance().GetPartyData()[selectedIndex]);

						Refresh();
						OnElementSelection(0);
					}
				});
				debugPanel.add(dismissCharacterButton, BorderLayout.SOUTH);
				dismissCharacterButton.setVisible(false);
			
			//rightContainer.add(listScroller, PanelTemplateType.CharacterList.toString());
			rightContainer.add(debugPanel, PanelTemplateType.CharacterList.toString());
		
			//Add other panels: item desc, skill desc, and item list
			ItemDescPanel itemDesc = new ItemDescPanel(null);
			rightContainer.add(itemDesc, PanelTemplateType.ItemDesc.toString());
			
			
			chooseButton = new JButton("Choose");
			
			//Item List
			itemList = new JList<ItemData>();
			itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			itemList.setLayoutOrientation(JList.VERTICAL);
			itemList.setVisibleRowCount(-1);
			itemList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
				    if (e.getValueIsAdjusting() == false) {
				    	if (itemList.getSelectedIndex() != -1) {
				    		chooseButton.setEnabled(true);
				    		OnItemHighlight(itemList.getSelectedIndex());
				    	}
				    }
				}
			});
			
			
			/*itemList.addMouseListener(
				new MouseAdapter() {
					@Override
					public void mouseExited(MouseEvent event) {
			        	int index = itemList.locationToIndex(event.getPoint());
			        	if (index >= 0) {
			        		mHoveredJListIndex = -1;
							OnItemExit();
			        	}
					}
				}
			);*/
			/*itemList.addMouseMotionListener(
				new MouseAdapter() {
					public void mouseMoved(MouseEvent event) {
						int index = itemList.locationToIndex(event.getPoint());
						if(index != mHoveredJListIndex) {
							mHoveredJListIndex = index;
							OnItemEnter(index);
						}
					}
				}
			);*/
			
			
			JPanel selectionPanel = new JPanel(new BorderLayout());
					
				JScrollPane listScroller_itemSelection = new JScrollPane(itemList);
				listScroller_itemSelection.setPreferredSize(new Dimension(250, 80));
				
				selectionPanel.add(listScroller_itemSelection, BorderLayout.CENTER);
				
				Box buttonBox = Box.createHorizontalBox();
				
					chooseButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							OnItemSelect(itemList.getModel().getElementAt(itemList.getSelectedIndex()).getName());
						}
					});
					chooseButton.setEnabled(false);
					buttonBox.add(chooseButton);
					
					JButton clearButton = new JButton("Clear");
					clearButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							OnItemSelect("");
						}
					});
					buttonBox.add(clearButton);
					
					buttonBox.add(Box.createHorizontalGlue());
					JButton cancelButton = new JButton("Cancel");
					cancelButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							characterDesc.SumbitEquipmentChoice(currentSlotIndex);
							rightCardLayout.show(rightContainer, PanelTemplateType.CharacterList.toString());
						}
					});
					buttonBox.add(cancelButton);
				
				selectionPanel.add(buttonBox, BorderLayout.SOUTH);
			
			rightContainer.add(selectionPanel, PanelTemplateType.InventoryList.toString());
		
		this.add(rightContainer);
		
		rightCardLayout.show(rightContainer, PanelTemplateType.CharacterList.toString());
	}
	
	//private int mHoveredJListIndex = -1;
	
	public void Refresh() {
		CharacterData[] dataArray = Game.Instance().GetPartyData();
		System.out.println("PartyPanel - Refresh, first: " + dataArray[0].toString());
		
		charList.setListData(dataArray);
		charList.updateUI();
		charList.setSelectedIndex(0);
	}
	
	//Local Methods
	//Character selection
	private void OnElementSelection(int index) {
		selectedIndex = index;
		
		
		System.out.println("OnElementSelection() - index: " + index + ", party.length: " + Game.Instance().GetPartyData().length);
		if(index == -1)
			return;
		
		if(Game.Instance().GetPartyData().length > index)
			characterDesc.DisplayCharacter(Game.Instance().GetPartyData()[index]);
		else
			System.err.println("PartyPanel.OnElementSelection() - Couldn't get element at index: " + index);
	}
	
	JList<ItemData> itemList;
	int currentSlotIndex;
	JButton chooseButton;
	
	//External Methods
	public void StartEquipmentSelection(int slotIndex) {
		currentSlotIndex = slotIndex;
		
		//String itemNameInSlot;
		//if(Game.Instance().GetPartyData()[selectedIndex].GetLiveInventory().IsItemExistingAt(currentSlotIndex))
		//	itemNameInSlot = Game.Instance().GetPartyData()[selectedIndex].GetLiveInventory().GetAt(currentSlotIndex).getName();
		//else
		//	itemNameInSlot = "";
		
		//Update item list with game.inventory and filter for slot's equipment type
		ItemData[] filteredItems = Game.Instance().GetInventory().stream()
				.filter(x -> x.getStats() != null && x.getStats().getEquipmentType() == EquipmentType.values()[slotIndex])
				//.filter(x -> x.getEquipmentType() == EquipmentType.values()[slotIndex] && !x.getName().equals(itemNameInSlot))
				.toArray(ItemData[]::new);
		itemList.setListData(filteredItems);
		itemList.updateUI();
		
		chooseButton.setEnabled(false);
		
		rightCardLayout.show(rightContainer, PanelTemplateType.InventoryList.toString());
	}
	
	//Equipment slot chooser
	public void OnItemHighlight(int index) {
		System.out.println("PartyPanel.OnItemEnter() - Show changes to characters stats with: " + itemList.getModel().getElementAt(index).getName());
		
		characterDesc.Card().SimulateEquipmentStats(currentSlotIndex, itemList.getModel().getElementAt(index));
	}
	
	//public void OnItemExit() {
	//	System.out.println("PartyPanel.OnItemExit() - Show character's base stats");
	//	
	//}
	
	public void OnItemSelect(String itemName) {
		//Update the CharacterData.Inventory
		CharacterData charData = Game.Instance().GetPartyData()[selectedIndex];
		if(charData.IsItemExistingAt(currentSlotIndex))
			Game.Instance().ReceiveItems(new ItemData[] { charData.ReturnItemAtIndex(currentSlotIndex) });
		
		if(itemName == "")
			charData.SetItemAtIndex(currentSlotIndex, null);
		else
			charData.SetItemAtIndex(currentSlotIndex, Game.Instance().TakeSingleItemFromInventoryAt(itemName));
		
		//Then Update UI
		characterDesc.SumbitEquipmentChoice(currentSlotIndex);
		
		rightCardLayout.show(rightContainer, PanelTemplateType.CharacterList.toString());
	}
}
