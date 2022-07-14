package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.Random;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import data.BattleData;
import data.ItemData;
import enums.ColorBlend;
import data.CharacterData;

import gameLogic.ItemDepot;
import gameLogic.Items;
import gameLogic.Missions;

/**
 * This is a notification panel used to display a group of items that've been recieved as a result of something resolving; an interaction, mission, battle, etc
 * @author Magnus
 *
 */
@SuppressWarnings("serial")
public class ResultsPane extends JLayeredPane {
	public ResultsPane(Point2D paneNormSize, ActionListener okButtonListener) {
		super();
		this.setBackground(Color.DARK_GRAY);
		this.setOpaque(true);
		Dimension paneSize = GUIUtil.GetRelativeSize((float)paneNormSize.getX(), (float)paneNormSize.getY());
		Point paneLoc = GUIUtil.GetRelativePoint(0.5f - ((float)paneNormSize.getX() / 2f), 0.5f - ((float)paneNormSize.getY() / 2f));
		this.setBounds(paneLoc.x, paneLoc.y, paneSize.width, paneSize.height);
		
		
		//Upper Section
		titleLabel = new JFxLabel("Event", SwingConstants.LEFT, GUIUtil.LocationLabel, Color.WHITE)
				.withStroke(Color.BLACK, 3, true)
				.withShadow(Color.LIGHT_GRAY, new Point(2, 2));
		Point2D missionLabelNormSize = new Point2D.Float(0.8f, 0.1f);
		Point missionLabelLoc = GUIUtil.GetRelativePoint(0.5f - ((float)missionLabelNormSize.getX() / 2f), 0.11f);
		Dimension missionLabelSize = GUIUtil.GetRelativeSize((float)missionLabelNormSize.getX(), (float)missionLabelNormSize.getY());
		titleLabel.setBounds((int)Math.round(missionLabelLoc.x * paneNormSize.getX()), (int)Math.round(missionLabelLoc.y * paneNormSize.getY()),
							   (int)Math.round(missionLabelSize.width * paneNormSize.getX()), (int)Math.round(missionLabelSize.height * paneNormSize.getY()));
		this.add(titleLabel, 0, 0);
		
		resultLabel = new JFxLabel("Result", SwingConstants.LEFT, GUIUtil.ItalicHeader_L, Color.BLACK)
				.withShadow(Color.DARK_GRAY, new Point(2, 2));
		Point2D resultsLabelNormSize = new Point2D.Float(0.76f, 0.1f);
		Point resultsLabelLoc = GUIUtil.GetRelativePoint(0.5f - ((float)resultsLabelNormSize.getX() / 2f), 0.19f);
		Dimension resultsLabelSize = GUIUtil.GetRelativeSize((float)resultsLabelNormSize.getX(), (float)resultsLabelNormSize.getY());
		resultLabel.setBounds((int)Math.round(resultsLabelLoc.x * paneNormSize.getX()), (int)Math.round(resultsLabelLoc.y * paneNormSize.getY()),
				   (int)Math.round(resultsLabelSize.width * paneNormSize.getX()), (int)Math.round(resultsLabelSize.height * paneNormSize.getY()));
		this.add(resultLabel, 0, 0);
		
		//Lower Section
		
		float recruitNormSizeX = 0.6f;
		JFxLabel recruitLabel = new JFxLabel("Recruits:", SwingConstants.LEFT, GUIUtil.ItalicHeader, Color.DARK_GRAY);
		Point2D recruitLabelNormSize = new Point2D.Float(recruitNormSizeX, 0.05f);
		Point recruitLabelLoc = GUIUtil.GetRelativePoint(0.5f - ((float)recruitLabelNormSize.getX() / 2f), 0.29f);
		Dimension recruitLabelSize = GUIUtil.GetRelativeSize((float)recruitLabelNormSize.getX(), (float)recruitLabelNormSize.getY());
		recruitLabel.setBounds((int)Math.round(recruitLabelLoc.x * paneNormSize.getX()), (int)Math.round(recruitLabelLoc.y * paneNormSize.getY()),
				   (int)Math.round(recruitLabelSize.width * paneNormSize.getX()), (int)Math.round(recruitLabelSize.height * paneNormSize.getY()));
		this.add(recruitLabel, 0, 0);
		
		Color recruitBgColor = Color.LIGHT_GRAY;
		recruitGrid = new JPanel(new GridLayout(0, 6)); //2, 5
		recruitGrid.setBackground(recruitBgColor);
		Point2D recruitGridNormSize = new Point2D.Float(recruitNormSizeX, 0.2f); //0.72f, 0.36f);
		Point2D recruitGridNormLoc = new Point2D.Float(0.5f - ((float)recruitGridNormSize.getX() / 2f), 0.34f);
		Point recruitGridLoc = GUIUtil.GetRelativePoint((float)recruitGridNormLoc.getX(), (float)recruitGridNormLoc.getY());
		Dimension recruitGridSize = GUIUtil.GetRelativeSize((float)recruitGridNormSize.getX(), (float)recruitGridNormSize.getY());
		recruitGrid.setBounds((int)Math.round(recruitGridLoc.x * paneNormSize.getX()), (int)Math.round(recruitGridLoc.y * paneNormSize.getY()),
				    (int)Math.round(recruitGridSize.width * paneNormSize.getX()), (int)Math.round(recruitGridSize.height * paneNormSize.getY()));
		this.add(recruitGrid, 0, 0);
		
		JLabel recruitBg = new JLabel(SpriteSheetUtility.HighlightBGNinecon(recruitBgColor, ColorBlend.Multiply));
		Point2D recruitBgNormSize = new Point2D.Float((float)recruitGridNormSize.getX() + 0.02f, (float)recruitGridNormSize.getY() + 0.0225f);
		Point recruitBgLoc = GUIUtil.GetRelativePoint((float)recruitGridNormLoc.getX() - 0.01f, (float)recruitGridNormLoc.getY() - 0.01125f);
		Dimension recruitBgSize = GUIUtil.GetRelativeSize((float)recruitBgNormSize.getX(), (float)recruitBgNormSize.getY());
		recruitBg.setBounds((int)Math.round(recruitBgLoc.x * paneNormSize.getX()), (int)Math.round(recruitBgLoc.y * paneNormSize.getY()),
				   (int)Math.round(recruitBgSize.width * paneNormSize.getX()), (int)Math.round(recruitBgSize.height * paneNormSize.getY()));
		this.add(recruitBg, 0, -1);
		
		
		float rewardsNormLocY = 0.59f; //0.29f
		JFxLabel rewardsLabel = new JFxLabel("Rewards:", SwingConstants.LEFT, GUIUtil.ItalicHeader, Color.DARK_GRAY);
		Point2D rewardsLabelNormSize = new Point2D.Float(0.35f, 0.05f);
		Point rewardsLabelLoc = GUIUtil.GetRelativePoint(0.5f - ((float)rewardsLabelNormSize.getX() / 2f), rewardsNormLocY);
		Dimension rewardsLabelSize = GUIUtil.GetRelativeSize((float)rewardsLabelNormSize.getX(), (float)rewardsLabelNormSize.getY());
		rewardsLabel.setBounds((int)Math.round(rewardsLabelLoc.x * paneNormSize.getX()), (int)Math.round(rewardsLabelLoc.y * paneNormSize.getY()),
				   (int)Math.round(rewardsLabelSize.width * paneNormSize.getX()), (int)Math.round(rewardsLabelSize.height * paneNormSize.getY()));
		this.add(rewardsLabel, 0, 0);
		
		Color itemBgColor = Color.LIGHT_GRAY;
		itemGrid = new JPanel(new GridLayout(0, 5)); //2, 5
		itemGrid.setBackground(itemBgColor);
		Point2D itemGridNormSize = new Point2D.Float(0.35f, 0.2f); //0.72f, 0.36f);
		Point2D itemGridNormLoc = new Point2D.Float(0.5f - ((float)itemGridNormSize.getX() / 2f), rewardsNormLocY + 0.05f);
		Point itemGridLoc = GUIUtil.GetRelativePoint((float)itemGridNormLoc.getX(), (float)itemGridNormLoc.getY());
		Dimension itemGridSize = GUIUtil.GetRelativeSize((float)itemGridNormSize.getX(), (float)itemGridNormSize.getY());
		itemGrid.setBounds((int)Math.round(itemGridLoc.x * paneNormSize.getX()), (int)Math.round(itemGridLoc.y * paneNormSize.getY()),
				    (int)Math.round(itemGridSize.width * paneNormSize.getX()), (int)Math.round(itemGridSize.height * paneNormSize.getY()));
		this.add(itemGrid, 0, 0);
		
		JLabel itemsBg = new JLabel(SpriteSheetUtility.HighlightBGNinecon(itemBgColor, ColorBlend.Multiply));
		Point2D itemsBgNormSize = new Point2D.Float((float)itemGridNormSize.getX() + 0.02f, (float)itemGridNormSize.getY() + 0.0225f);
		Point itemsBgLoc = GUIUtil.GetRelativePoint((float)itemGridNormLoc.getX() - 0.01f, (float)itemGridNormLoc.getY() - 0.01125f);
		Dimension itemsBgSize = GUIUtil.GetRelativeSize((float)itemsBgNormSize.getX(), (float)itemsBgNormSize.getY());
		itemsBg.setBounds((int)Math.round(itemsBgLoc.x * paneNormSize.getX()), (int)Math.round(itemsBgLoc.y * paneNormSize.getY()),
				   (int)Math.round(itemsBgSize.width * paneNormSize.getX()), (int)Math.round(itemsBgSize.height * paneNormSize.getY()));
		this.add(itemsBg, 0, -1);
		
		
		CustomButton okButton = new CustomButton(SpriteSheetUtility.CheckmarkSymbol(), null, Color.GREEN, SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(), SpriteSheetUtility.PaperBGColor);
		Point2D okButtonNormSize = new Point2D.Float(0.06f, 0.06f);
		Point okButtonLoc = GUIUtil.GetRelativePoint(0.88f, 0.8f);
		Dimension okButtonSize = GUIUtil.GetRelativeSize((float)okButtonNormSize.getX(), true);
		okButton.setBounds((int)Math.round(okButtonLoc.x * paneNormSize.getX()), (int)Math.round(okButtonLoc.y * paneNormSize.getY()),
				   (int)Math.round(okButtonSize.width * paneNormSize.getX()), (int)Math.round(okButtonSize.height * paneNormSize.getX()));
		okButton.addActionListener(okButtonListener);
		this.add(okButton, 0, 0);
		
		
		//Misc
		JLabel bg = new JLabel(SpriteSheetUtility.PanelBG_PaperTiled());
		Point2D bgNormSize = new Point2D.Float(1f, 1f);
		Point bgLoc = GUIUtil.GetRelativePoint(0f, 0f);
		Dimension bgSize = GUIUtil.GetRelativeSize((float)bgNormSize.getX(), (float)bgNormSize.getY());
		bg.setBounds((int)Math.round(bgLoc.x * paneNormSize.getX()), (int)Math.round(bgLoc.y * paneNormSize.getY()),
				   (int)Math.round(bgSize.width * paneNormSize.getX()), (int)Math.round(bgSize.height * paneNormSize.getY()));
		this.add(bg, 0, -1);
		
		
		//test ui values
		int testRewardCount = 7;
		ItemData[] testRewards = new ItemData[testRewardCount];
		Random itemRandom = new Random();
		for(int i = 0; i < testRewardCount; i++) {
			if(i == 0)
				testRewards[i] = new ItemData(ItemDepot.FireSeal.getId(), 3);
			else if(i == 1)
				testRewards[i] = new ItemData(ItemDepot.ShrapnelBomb.getId(), 15);
			else if(i == 2)
				testRewards[i] = new ItemData(ItemDepot.ExquisiteSlatFan.getId(), 1);
			else
				testRewards[i] = Items.itemList.get(itemRandom.nextInt(Items.itemList.size()));
		}
		BattleData exampleBattleData = BattleData.GetExample();
		String[] exampleRecruits = new String[] { "e0f327f5-bad5-48d2-8097-15325ed8a6d5" };
		UpdateResults("Battle: " + exampleBattleData.GetName(), true, exampleRecruits, testRewards);
	}
	//Upper Section
	JFxLabel titleLabel; //Reads the name of the resolved event
	JFxLabel resultLabel; //Reads "Success" or "Fail"
	//Lower Section
	JPanel recruitGrid;
	JPanel itemGrid;
	
	
	public void UpdateResults(String titleText, boolean wasSuccess, String[] recruitIds, ItemData[] itemDatas) {
		titleLabel.setText(titleText);
		resultLabel.setText(wasSuccess ? "Success" : "Fail");
		resultLabel.setForeground(wasSuccess ? Color.GREEN.darker() : Color.RED.darker());
		
		recruitGrid.removeAll();
		if(recruitIds != null) {
			for(String id : recruitIds) {
				CharacterData charData = Missions.GetCharacterById(id);
				System.out.println("ResultsPane.UpdateResults() - new recruit: " + charData.getName());
				CharacterBlock block = new CharacterBlock(charData);
				recruitGrid.add(block);
				block.Initialize();
			}
		}
		
		itemGrid.removeAll();
		if(itemDatas != null) {
			for(ItemData data : itemDatas) {
				System.out.println("ResultsPane.UpdateResults() - new item: " + data.getName());
				ItemBlock block = new ItemBlock(data);
				block.setToolTipText(data.getName());
				itemGrid.add(block);
				block.Initialize();
			}
		}
	}
}
