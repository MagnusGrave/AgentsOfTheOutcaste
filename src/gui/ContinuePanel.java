package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import enums.MenuType;
import gameLogic.Game;

@SuppressWarnings("serial")
public class ContinuePanel extends JPanel {
	//private GUIManager guiManager;
	
	ActionListener pendingAction;
	
	public ContinuePanel(WorldmapPanel worldmapPanel) {
		super(new BorderLayout(1, 1));
		//this.guiManager = guiManager;
		//mainMenuPanel.setBorder(BorderFactory.createMatteBorder(2,2,2,2,Color.RED));
		this.setOpaque(false);
		this.setBackground(new Color(0,0,0,0));
		
		//Title Name
		JPanel titleSpacerPanel = new JPanel(new BorderLayout());
		titleSpacerPanel.setOpaque(false);
		titleSpacerPanel.setBackground(new Color(0,0,0,0));
		JFxLabel titleLabel = new JFxLabel("Game Over", SwingConstants.CENTER, GUIUtil.Title, Color.RED.darker())
				.withStroke(Color.BLACK, 4, true)
				.withShadow(Color.GRAY, new Point(2, 2));
		titleSpacerPanel.add(titleLabel, BorderLayout.CENTER);
		titleSpacerPanel.add(Box.createVerticalStrut(290), BorderLayout.SOUTH);
		this.add(titleSpacerPanel, BorderLayout.CENTER);
		
		//Options
		JPanel buttonGrid = new JPanel(new GridLayout(1, 1, 0, 0));
		if(Game.Instance().DoesPlayerDataExist())
			buttonGrid = new JPanel(new GridLayout(1, 2, 0, 0));
		buttonGrid.setPreferredSize(GUIUtil.GetRelativeSize(1f, 0.1f));
		buttonGrid.setOpaque(false);
		buttonGrid.setBackground(new Color(0,0,0,0));
		
		List<CustomButtonUltra> ultraButtonGroup = new ArrayList<CustomButtonUltra>(); 
		//JButton startButton = new JButton("New Game");
		CustomButtonUltra continueButton = new CustomButtonUltra(new JFxLabel("Continue", SwingConstants.CENTER, GUIUtil.ItalicHeader, Color.WHITE).withStroke(Color.BLACK, 2, false),
																null, Color.green.darker(),
																SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(),
																Color.DARK_GRAY);
		ultraButtonGroup.add(continueButton);
		
		//we dont want a load button, actually. We want to save a game over to the saveData before the player has a chance to load-save, this will prevent unlimited retries via quiting and loading
		/*CustomButtonUltra loadButton = new CustomButtonUltra(new JFxLabel("Load Game", SwingConstants.CENTER, GUIUtil.ItalicHeader, Color.WHITE).withStroke(Color.BLACK, 2, false),
				null, Color.green.darker(),
				SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(),
				Color.DARK_GRAY);
		ultraButtonGroup.add(loadButton);*/
		
		
		//ContinuePanel thisPanel = this;
		continueButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GUIManager.GetFadeTransitionPanel().Fade(true, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						//worldmapPanel.LoadWorld();
						worldmapPanel.ReloadWorldFromContinue();
						GUIManager.ShowScreen(MenuType.WORLDMAP);
						
						//If LoadWorld() was a Runnable then we'd wait till the end of its process to callback to this class
						//but, since LoadWorld() should stall further execution until its complete, do this now
						GUIManager.GetFadeTransitionPanel().Fade(false, 120);
					}
				});
				continueButton.setEnabled(false);
				//loadButton.setEnabled(false);
			}
		});
		buttonGrid.add(continueButton);
		
		
		/*loadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
			}
		});
		buttonGrid.add(loadButton);*/
		
		this.add(buttonGrid, BorderLayout.SOUTH);
		
		
		//Add all buttons to a group so that they can elimate eachothers artifacting
		for(CustomButtonUltra ultra : ultraButtonGroup)
			ultra.AddGroupList(ultraButtonGroup);
	}
}
