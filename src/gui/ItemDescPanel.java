package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import data.ItemData;
import gameLogic.Mission;
import gameLogic.Missions;

@SuppressWarnings("serial")
public class ItemDescPanel extends JPanel {
	//Character Card
	ImagePanel headshot;
	JLabel nameLabel;
	JLabel classLabel;
	JLabel equipmentLabel;
	JLabel valueLabel;
	JLabel quantityLabel;
	JLabel statLabel_attack;
	JLabel statLabel_armor;
	JLabel statLabel_hp;
	
	//Description
	JTextArea descriptionLabel;
	
	//Related Mission
	JLabel missionLabel;
	
	IMissionViewer missionViewer;
	ItemData itemData;
	
	
	
	public ItemDescPanel(IMissionViewer missionViewer) {
		this.missionViewer = missionViewer;
		
		//Setup Left Panel
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JPanel characterBox = new JPanel();
		characterBox.setLayout(new BoxLayout(characterBox, BoxLayout.Y_AXIS));
		
			JPanel infoPanel = new JPanel();
			infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
			
				JPanel info = new JPanel();
				info.setLayout(new BoxLayout(info, BoxLayout.X_AXIS));
				
					headshot = new ImagePanel("itemImages/stone02_02.png");
					info.add(headshot);
					
					JPanel infoGrid = new JPanel(new GridLayout(5, 1));
					
						Box labelBox = Box.createHorizontalBox();
							labelBox.add(Box.createHorizontalStrut(40));
							JLabel nameLabelLabel = new JLabel("Name: ");
							labelBox.add(nameLabelLabel);
							nameLabel = new JLabel();
							labelBox.add(nameLabel);
						infoGrid.add(labelBox);
						
						Box labelBox_class = Box.createHorizontalBox();
							labelBox_class.add(Box.createHorizontalStrut(40));
							JLabel classLabelLabel = new JLabel("Type: ");
							labelBox_class.add(classLabelLabel);
							classLabel = new JLabel();
							labelBox_class.add(classLabel);
						infoGrid.add(labelBox_class);
						
						Box labelBox_equipType = Box.createHorizontalBox();
							labelBox_equipType.add(Box.createHorizontalStrut(40));
							JLabel equipLabelLabel = new JLabel("Equip Type: ");
							labelBox_equipType.add(equipLabelLabel);
							equipmentLabel = new JLabel();
							labelBox_equipType.add(equipmentLabel);
						infoGrid.add(labelBox_equipType);
						
						Box labelBox_hp = Box.createHorizontalBox();
							labelBox_hp.add(Box.createHorizontalStrut(40));
							JLabel hpLabelLabel = new JLabel("Value(Individual): ");
							labelBox_hp.add(hpLabelLabel);
							valueLabel = new JLabel();
							labelBox_hp.add(valueLabel);
						infoGrid.add(labelBox_hp);
						
						Box labelBox_exp = Box.createHorizontalBox();
							labelBox_exp.add(Box.createHorizontalStrut(40));
							JLabel expLabelLabel = new JLabel("Quantity: ");
							labelBox_exp.add(expLabelLabel);
							quantityLabel = new JLabel();
							labelBox_exp.add(quantityLabel);
						infoGrid.add(labelBox_exp);
					
					info.add(infoGrid);
					
				infoPanel.add(info);
				
				JPanel stats = new JPanel();
				stats.setLayout(new BoxLayout(stats, BoxLayout.X_AXIS));
	
					Border statBorder = BorderFactory.createLineBorder(Color.BLACK, 1);
					JPanel flow_str = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
					flow_str.setBorder(statBorder);
					JLabel statLabel_str = new JLabel("Attack");
					flow_str.add(statLabel_str);
					statLabel_attack = new JLabel();
					flow_str.add(statLabel_attack);
					stats.add(flow_str);
					
					JPanel flow_int = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
					flow_int.setBorder(statBorder);
					JLabel statLabel_int = new JLabel("Armor");
					flow_int.add(statLabel_int);
					statLabel_armor = new JLabel();
					flow_int.add(statLabel_armor);
					stats.add(flow_int);
		
					JPanel flow_end = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
					flow_end.setBorder(statBorder);
					JLabel statLabel_end = new JLabel("HP");
					flow_end.add(statLabel_end);
					statLabel_hp = new JLabel();
					flow_end.add(statLabel_hp);
					stats.add(flow_end);
				
				infoPanel.add(stats);
			
			infoPanel.setPreferredSize(new Dimension(800, 220));
			characterBox.add(infoPanel);
			
			//Description
			JPanel descPanel = new JPanel(new BorderLayout());
			descPanel.setPreferredSize(new Dimension(800, 400));
			descPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 1), "Description"));
			
				descriptionLabel = new JTextArea("", 16, 30);
				descriptionLabel.setEditable(false);
				descriptionLabel.setFocusable(false);
				descriptionLabel.setLineWrap(true);
				descriptionLabel.setWrapStyleWord(true);
				descPanel.add(descriptionLabel);
				
			characterBox.add(descPanel);
			
			JPanel missionPanel = new JPanel();
			missionPanel.setPreferredSize(new Dimension(800, 200));
			missionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 1), "Related Mission"));
			
				missionLabel = new JLabel("-");
				MouseListener mouseListener = new MouseAdapter() {
					@Override
					public void mouseEntered(MouseEvent event) {				
			        	ToggleViewersMissionDesc(true);
				    }
					@Override
					public void mouseExited(MouseEvent event) {
			        	ToggleViewersMissionDesc(false);
					}
				};
				missionLabel.addMouseListener(mouseListener);
				missionPanel.add(missionLabel);
				
			characterBox.add(missionPanel);
			
		this.add(characterBox);
	}
	
	private void ToggleViewersMissionDesc(boolean enabled) {
		if(missionViewer == null)
			return;
		
		//System.out.println("ToggleViewersItemDesc()");
		
		missionViewer.ToggleMissionDesc(enabled, itemData.getRelatedMissionId());
	}
	
	public void DisplayItem(ItemData itemData) {
		this.itemData = itemData;
		
		//Update all UI components for the newly selected character
		headshot.SetNewImage(itemData.GetFilePath());
		nameLabel.setText(itemData.getName());
		classLabel.setText(itemData.getType().toString());
		equipmentLabel.setText(itemData.getStats() == null ?
			"N/A"
			:
			(itemData.getStats().getEquipmentType() == null ?
				"N/A"
				:
				itemData.getStats().getEquipmentType().toString()
			)
		);
		valueLabel.setText("" + itemData.getValue());
		quantityLabel.setText("" + itemData.getQuantity());
		
		statLabel_attack.setText("" + itemData.getStats().getAttack());
		statLabel_armor.setText("" + itemData.getStats().getArmor());
		statLabel_hp.setText("" + itemData.getStats().getHp());
		
		descriptionLabel.setText("" + itemData.getDescription());
		
		if(itemData.getRelatedMissionId() == null) {
			missionLabel.setText("-");
			missionLabel.setBorder(null);
		} else {
			Mission mission = Missions.getById(itemData.getRelatedMissionId());
			missionLabel.setText(mission.getName());
			missionLabel.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
		}
	}
}
