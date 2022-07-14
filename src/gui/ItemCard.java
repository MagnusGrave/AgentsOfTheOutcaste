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

import data.BattleToolTraits;
import data.ItemData;
import enums.HitSelectionType;
import gameLogic.Game;

@SuppressWarnings("serial")
public class ItemCard extends JLayeredPane {
	ImagePanel itemImage;
	JLabel nameLabel;
	JLabel descLabel;
	JLabel effectsBlobLabel;
	JLabel rangeNotationLabel;
	
	ItemData currentItem;
	
	private final Color positiveColor = Color.GREEN;
	private final Color negativeColor = Color.RED;
	
	
	
	public ItemCard(Point panePos, Dimension paneSize) {
		super();
		setOpaque(false);
		setBackground(new Color(0,0,0,0));
		setBounds(panePos.x, panePos.y, paneSize.width, paneSize.height);
		
		JLabel cardBG = new JLabel( SpriteSheetUtility.HighlightBGNinecon() );
		cardBG.setBounds(0, 0, paneSize.width, paneSize.height);
		add(cardBG, 0, 0);
		
		int insetWidth = Math.round(paneSize.width * 0.04f);
		int insetHeight = Math.round(paneSize.height * 0.02f);
		Dimension textGridPanelSize = new Dimension( paneSize.width - (insetWidth*2), paneSize.height - (insetHeight*2));
		Point textGridPanelPos = new Point(insetWidth, insetHeight);
		
		JPanel textGridPanel = new JPanel(new GridLayout(4, 1));
		textGridPanel.setBackground(Color.WHITE);
		textGridPanel.setBounds(textGridPanelPos.x, textGridPanelPos.y, textGridPanelSize.width, textGridPanelSize.height);
		
			/*
			nameLabel = new JFxLabel("Name", SwingConstants.LEFT, GUIUtil.Header, Color.BLACK).withShadow(Color.LIGHT_GRAY, new Point(2,2));
			nameLabel.setVerticalAlignment(SwingConstants.CENTER);
			nameLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
			textGridPanel.add(nameLabel);
			*/
			
			JPanel imageAndNamePanel = new JPanel();
			imageAndNamePanel.setBackground(Color.WHITE);
			imageAndNamePanel.setLayout(new BoxLayout(imageAndNamePanel, BoxLayout.X_AXIS));
			imageAndNamePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
			itemImage = new ImagePanel("Question.png");
			itemImage.setBackground(Color.WHITE);
			itemImage.setMaximumSize(new Dimension(textGridPanelSize.height/4, textGridPanelSize.height/4));
			imageAndNamePanel.add(itemImage);
			nameLabel = new JFxLabel("Name", SwingConstants.LEFT, GUIUtil.Header, Color.BLACK).withShadow(Color.LIGHT_GRAY, new Point(2,2));
			nameLabel.setVerticalAlignment(SwingConstants.CENTER);
			imageAndNamePanel.add(nameLabel);
			textGridPanel.add(imageAndNamePanel);
			
			
			descLabel = new JFxLabel("Description", SwingConstants.LEFT, GUIUtil.Body_2, Color.BLACK);
			descLabel.setVerticalAlignment(SwingConstants.TOP);
			descLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
			textGridPanel.add(descLabel);
			
			effectsBlobLabel = new JFxLabel("EffectsBlob", SwingConstants.LEFT, GUIUtil.Body_2_I, Color.BLACK);
			effectsBlobLabel.setVerticalAlignment(SwingConstants.TOP);
			effectsBlobLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
			textGridPanel.add(effectsBlobLabel);
			
			rangeNotationLabel = new JFxLabel("RangeNoationBlob", SwingConstants.LEFT, GUIUtil.Body_2, Color.BLACK);
			rangeNotationLabel.setVerticalAlignment(SwingConstants.TOP);
			textGridPanel.add(rangeNotationLabel);
		
		add(textGridPanel, 0, 0);
	}
	
	public void DisplayItem(ItemData item) {
		this.currentItem = item;
		
		itemImage.SetNewImage(item.GetFilePath());
		itemImage.ConformPreferredSizeToAspectRatio(false);
		itemImage.repaint(1000);
		System.out.println("Showing ItemCard with imagePath: " + item.GetFilePath());
		
		nameLabel.setText(currentItem.getName());
		descLabel.setText("<html>"+ currentItem.getDescription() +"</html>");
		effectsBlobLabel.setText("<html>"+ AbilityCard.GetCombatEffectsBlob( Game.Instance().GetItemsCombatProperites(currentItem).combatEffects ) +"</html>");
		rangeNotationLabel.setText("<html>"+  GetRangeNotationBlob(currentItem.getStats().GetBattleToolTraits())  +"</html>");
	}
	
	public String GetRangeNotationBlob(BattleToolTraits battleToolTraits) {
		return "Range: " + battleToolTraits.minRange + "-" + battleToolTraits.maxRange + ", AOE: " + battleToolTraits.aoeRange +
				"<br>Targeting: " + HitSelectionType.HitAll;
	}
}
