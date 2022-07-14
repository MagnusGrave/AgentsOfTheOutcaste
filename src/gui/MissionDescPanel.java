package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

import data.ItemData;
import enums.InteractionType;
import gameLogic.Game;
import gameLogic.MapLocation;
import gameLogic.Mission;
import gameLogic.Mission.MissionStatusType;

@SuppressWarnings("serial")
public class MissionDescPanel extends JPanel {	
	//Character Card
	ImagePanel headshot;
	JLabel nameLabel;
	JLabel classLabel;
	JLabel valueLabel;
	JLabel quantityLabel;
	JLabel statLabel_attack;
	JLabel statLabel_armor;
	JLabel statLabel_hp;
	
	//Description
	JTextArea descriptionLabel;
	
	//Mission Rewards
	JList<ItemData> itemList;
	
	IItemViewer itemViewer;
	Mission mission;

	
	public MissionDescPanel(IItemViewer itemViewer) {
		this.itemViewer = itemViewer;
		
		//Setup Left Panel
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JPanel characterBox = new JPanel();
		characterBox.setLayout(new BoxLayout(characterBox, BoxLayout.Y_AXIS));
		
			JPanel charInfoPanel = new JPanel();
			charInfoPanel.setLayout(new BoxLayout(charInfoPanel, BoxLayout.Y_AXIS));
			
				JPanel info = new JPanel();
				info.setLayout(new BoxLayout(info, BoxLayout.X_AXIS));
				
					headshot = new ImagePanel("missionEmblems/mission_nondescript.png");
					info.add(headshot);
					
					JPanel infoGrid = new JPanel(new GridLayout(4, 1));
					
						Box labelBox = Box.createHorizontalBox();
							labelBox.add(Box.createHorizontalStrut(40));
							JLabel nameLabelLabel = new JLabel("Name: ");
							labelBox.add(nameLabelLabel);
							nameLabel = new JLabel();
							labelBox.add(nameLabel);
						infoGrid.add(labelBox);
						
						Box labelBox_class = Box.createHorizontalBox();
							labelBox_class.add(Box.createHorizontalStrut(40));
							JLabel classLabelLabel = new JLabel("Location: ");
							labelBox_class.add(classLabelLabel);
							classLabel = new JLabel();
							labelBox_class.add(classLabel);
						infoGrid.add(labelBox_class);
						
						Box labelBox_hp = Box.createHorizontalBox();
							labelBox_hp.add(Box.createHorizontalStrut(40));
							JLabel hpLabelLabel = new JLabel("Objectives: ");
							labelBox_hp.add(hpLabelLabel);
							valueLabel = new JLabel();
							labelBox_hp.add(valueLabel);
						infoGrid.add(labelBox_hp);
						
						Box labelBox_exp = Box.createHorizontalBox();
							labelBox_exp.add(Box.createHorizontalStrut(40));
							JLabel expLabelLabel = new JLabel("Activity: ");
							labelBox_exp.add(expLabelLabel);
							quantityLabel = new JLabel();
							labelBox_exp.add(quantityLabel);
						infoGrid.add(labelBox_exp);
					
					info.add(infoGrid);
					
				charInfoPanel.add(info);
				
				/*JPanel stats = new JPanel();
				stats.setLayout(new BoxLayout(stats, BoxLayout.X_AXIS));
	
					Border statBorder = BorderFactory.createLineBorder(Color.BLACK, 1);
					JPanel flow_str = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
					flow_str.setBorder(statBorder);
					JLabel statLabel_str = new JLabel("Attack");
					flow_str.add(statLabel_str);
					statLabel_attack = new JLabel("+ " + item.getStats().getAttack());
					flow_str.add(statLabel_attack);
					stats.add(flow_str);
					
					JPanel flow_int = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
					flow_int.setBorder(statBorder);
					JLabel statLabel_int = new JLabel("Armor");
					flow_int.add(statLabel_int);
					statLabel_armor = new JLabel("+ " + item.getStats().getArmor());
					flow_int.add(statLabel_armor);
					stats.add(flow_int);
		
					JPanel flow_end = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
					flow_end.setBorder(statBorder);
					JLabel statLabel_end = new JLabel("HP");
					flow_end.add(statLabel_end);
					statLabel_hp = new JLabel("+ " + item.getStats().getHp());
					flow_end.add(statLabel_hp);
					stats.add(flow_end);
				
				charInfoPanel.add(stats);*/
			
			charInfoPanel.setPreferredSize(new Dimension(800, 200));
			characterBox.add(charInfoPanel);
			
			//Description
			JPanel descPanel = new JPanel(new BorderLayout());
			descPanel.setPreferredSize(new Dimension(800, 400));
			descPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 1), "Description"));
			
				descriptionLabel = new JTextArea("", 16, 30);
				descriptionLabel.setEditable(false);
				descriptionLabel.setFocusable(false);
				descriptionLabel.setLineWrap(true);
				descriptionLabel.setWrapStyleWord(true);
				descPanel.add(descriptionLabel, BorderLayout.CENTER);
				
			characterBox.add(descPanel);
			
			//Item List
			itemList = new JList<ItemData>();
			itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			itemList.setLayoutOrientation(JList.VERTICAL);
			itemList.setVisibleRowCount(-1);
			
			/*MouseListener mouseListener = new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent event) {				
					JList theList = (JList) event.getSource();
		        	int index = theList.locationToIndex(event.getPoint());
		        	if (index >= 0) {
		        		//Object o = theList.getModel().getElementAt(index);
		        		ToggleViewersItemDesc(true, index);
		        	}
			    }
				@Override
				public void mouseExited(MouseEvent event) {
					JList theList = (JList) event.getSource();
		        	int index = theList.locationToIndex(event.getPoint());
		        	if (index >= 0) {
		        		//Object o = theList.getModel().getElementAt(index);
		        		ToggleViewersItemDesc(false, index);
		        	}
				}
			};
			itemList.addMouseListener(mouseListener);*/
			itemList.addMouseListener(
				new MouseAdapter() {
					@Override
					public void mouseExited(MouseEvent event) {
			        	int index = itemList.locationToIndex(event.getPoint());
			        	if (index >= 0) {
			        		mHoveredJListIndex = -1;
			        		ToggleViewersItemDesc(false, index);
			        	}
					}
				}
			);
			itemList.addMouseMotionListener(
				new MouseAdapter() {
					public void mouseMoved(MouseEvent event) {
						int index = itemList.locationToIndex(event.getPoint());
						if(index != mHoveredJListIndex) {
							mHoveredJListIndex = index;
							ToggleViewersItemDesc(true, index);
						}
					}
				}
			);
			
			JScrollPane listScroller = new JScrollPane(itemList);
			listScroller.setPreferredSize(new Dimension(250, 200));
			listScroller.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 1), "Rewards"));
			
			characterBox.add(listScroller);
			
		this.add(characterBox);
	}
	
	private int mHoveredJListIndex = -1;
	
	private void ToggleViewersItemDesc(boolean enabled, int index) {
		if(itemViewer == null)
			return;
		
		//System.out.println("ToggleViewersItemDesc()");
		
		itemViewer.ToggleItemDesc(enabled, mission.getRewards()[index]);
	}
	
	public void DisplayMission(Mission mission) {
		this.mission = mission;
		
		//Update all UI components for the newly selected character
		InteractionType firstStipulation = null;
		if(mission.getMissionStipulations() != null && mission.getMissionStipulations().size() > 0)
			firstStipulation = mission.getMissionStipulations().get(0);
		MapLocation mapLocation = GUIManager.WorldmapPanel().GetMapLocationById(mission.getMapLocationId());
		headshot.SetNewImage(InteractionType.GetMissionIcon(firstStipulation, mapLocation.getSettlementType()));
		
		nameLabel.setText(mission.getName());
		
		classLabel.setText(mapLocation.getName());
		
		String objectives = "";
		if(mission.getMissionStipulations() != null) {
			for(InteractionType type : mission.getMissionStipulations())
				objectives += type.toString();
		}
		valueLabel.setText(objectives);
		
		Mission[] completedMissionsArray = Game.Instance().getMissions().stream().filter(x -> x.getMissionStatus() == MissionStatusType.Concluded).toArray(Mission[]::new);
		if(completedMissionsArray != null && completedMissionsArray.length > 0) {
			List<Mission> completedMissions =  new ArrayList<>(Arrays.asList(completedMissionsArray));
			quantityLabel.setText(completedMissions.contains(mission) ? "Complete" : "Active");
		} else {
			quantityLabel.setText("Active");
		}
		
		descriptionLabel.setText("" + mission.getDescription());
		
		if(mission.getRewards() != null && mission.getRewards().length > 0) {
			//System.out.println("Mission Rewards: " + mission.getRewards().length);
			itemList.setListData(mission.getRewards());
		} else {
			itemList.setListData(new ItemData[0]);
		}
		itemList.updateUI();
	}
}
