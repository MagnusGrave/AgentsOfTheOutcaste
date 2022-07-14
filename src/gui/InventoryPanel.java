package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import data.ItemData;
import enums.PanelTemplateType;
import gameLogic.Game;
import gameLogic.ItemDepot;
import gameLogic.Items;
import gameLogic.Mission;
import gameLogic.Missions;


@SuppressWarnings("serial")
public class InventoryPanel extends JPanel implements IMissionViewer, IRefreshable {
	//Left Panel Dynamic Elements
	ItemDescPanel itemDesc;
	
	CardLayout rightCardLayout = new CardLayout();
	JPanel rightContainer = new JPanel();
	
	MissionDescPanel missionDescPanel;
	int selectedIndex;
	
	JList<ItemData> itemList;
	
	//Testing - Add items to inventory manually
	ItemData[] manualItems = new ItemData[] {
		Items.getById(ItemDepot.Katana.getId()),
		Items.getById(ItemDepot.AmethystKunai.getId())
	};
	
	
	
	public InventoryPanel() {
		super(new GridLayout(1, 2, 2, 2));
		
		//Setup Left Panel
		itemDesc = new ItemDescPanel(this);
		this.add(itemDesc);	
		
		
		//Right CardLayout Panel
		rightContainer.setLayout(rightCardLayout);
		
		//Testing - Add items to inventory manually
		JPanel debugPanel = new JPanel(new BorderLayout());
		
			JButton button = new JButton("Add Items (TESTING)");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Game.Instance().ReceiveItems(manualItems);
					Refresh();
				}
			});
			debugPanel.add(button, BorderLayout.NORTH);
		
			//Item List
			itemList = new JList<ItemData>();
			itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			itemList.setLayoutOrientation(JList.VERTICAL);
			itemList.setVisibleRowCount(-1);
			itemList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
				    if (e.getValueIsAdjusting() == false) {
				    	if (itemList.getSelectedIndex() != -1)
				        	OnElementSelection(itemList.getSelectedIndex());
				    }
				}
			});
			
			JScrollPane listScroller = new JScrollPane(itemList);
			listScroller.setPreferredSize(new Dimension(250, 80));
			
			debugPanel.add(listScroller, BorderLayout.CENTER);
		
		//rightContainer.add(listScroller, PanelTemplateType.InventoryList.toString());
		rightContainer.add(debugPanel, PanelTemplateType.InventoryList.toString());

		//Mission Description Panel
		missionDescPanel = new MissionDescPanel(null);
		rightContainer.add(missionDescPanel, PanelTemplateType.MissionDesc.toString());
		
		this.add(rightContainer);
		
		rightCardLayout.show(rightContainer, PanelTemplateType.InventoryList.toString());
	}
	
	public void Refresh() {
		System.out.println("InventoryPanel - Refresh");
		
		itemList.setListData(Game.Instance().GetInventory().stream().toArray(ItemData[]::new));
		itemList.updateUI();
		itemList.setSelectedIndex(0);
	}
	
	private void OnElementSelection(int index) {
		selectedIndex = index;
		if(Game.Instance().GetInventory().size() > index)
			itemDesc.DisplayItem(Game.Instance().GetInventory().get(index));
		else
			System.out.println(this.getName() + "Couldn't get element at index: " + index + " !!");
	}
	
	public void ToggleMissionDesc(boolean enabled, String missionId) {
		if(Game.Instance().GetInventory().get(selectedIndex).getRelatedMissionId() == null)
			return;
		
		Mission mission = Missions.getById(missionId);
		
		if(enabled)
			missionDescPanel.DisplayMission(mission);
			
		rightCardLayout.show(rightContainer, enabled ? PanelTemplateType.MissionDesc.toString() : PanelTemplateType.InventoryList.toString());
	}
}
