package gui;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import data.ItemData;
import enums.PanelTemplateType;
import gameLogic.Game;
import gameLogic.Mission;
import gameLogic.Missions;
import gameLogic.Mission.MissionStatusType;

@SuppressWarnings("serial")
public class MissionPanel extends JPanel implements IItemViewer, IRefreshable {
	MissionDescPanel missionDesc;
	ItemDescPanel itemDesc;
	
	CardLayout rightCardLayout = new CardLayout();
	JPanel rightContainer = new JPanel();
	
	JList<Mission> missionList;

	public MissionPanel() {
		super(new GridLayout(1, 2, 2, 2));
		
		//Create left panel with mission desc
		missionDesc = new MissionDescPanel(this);
		this.add(missionDesc);
		
		//Right CardLayout Panel
		rightContainer.setLayout(rightCardLayout);
		
		//Item List
		missionList = new JList<Mission>(); //data has type Object[]
		missionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		missionList.setLayoutOrientation(JList.VERTICAL);
		missionList.setVisibleRowCount(-1);
		missionList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
			    if (e.getValueIsAdjusting() == false) {
			    	if (missionList.getSelectedIndex() != -1)
			        	OnElementSelection(missionList.getSelectedIndex());
			    }
			}
		});
		
		JScrollPane listScroller = new JScrollPane(missionList);
		listScroller.setPreferredSize(new Dimension(250, 80));
		
		rightContainer.add(listScroller, PanelTemplateType.MissionList.toString());

		//Mission Description Panel
		itemDesc = new ItemDescPanel(null);
		rightContainer.add(itemDesc, PanelTemplateType.ItemDesc.toString());
		
		this.add(rightContainer);
		
		rightCardLayout.show(rightContainer, PanelTemplateType.MissionList.toString());
	}
	
	public void Refresh() {
		System.out.println("MissionPanel - Refresh");
		
		List<Mission> refreshMissionList = new ArrayList<Mission>();
		Mission[] completedMissionsArray = Game.Instance().getMissions().stream().filter(x -> x.getMissionStatus() == MissionStatusType.Concluded).toArray(Mission[]::new);
		if(completedMissionsArray != null) {
			for(Mission completedMission : completedMissionsArray)
				refreshMissionList.add(completedMission);
		}
		
		Mission[] nextAvailableMissions = Missions.GetNextAvailableMissions();
		if(nextAvailableMissions != null) {
			for(Mission nextMission : nextAvailableMissions) {
				if(!refreshMissionList.contains(nextMission))
					refreshMissionList.add(nextMission);
			}
		}
		
		if(refreshMissionList != null && refreshMissionList.size() > 0) {
			missionList.setListData(refreshMissionList.stream().toArray(Mission[]::new));
			missionList.updateUI();
			missionList.setSelectedIndex(0);
		}
	}
	
	private void OnElementSelection(int index) {
		missionDesc.DisplayMission(missionList.getModel().getElementAt(index));
	}

	public void ToggleItemDesc(boolean enabled, ItemData item) {
		//System.out.println("ToggleQuestDescription() - Stub");
		
		if(enabled)
			itemDesc.DisplayItem(item);
			
		rightCardLayout.show(rightContainer, enabled ? PanelTemplateType.ItemDesc.toString() : PanelTemplateType.MissionList.toString());
	}
}
