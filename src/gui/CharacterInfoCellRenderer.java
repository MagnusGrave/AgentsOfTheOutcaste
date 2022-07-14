package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import gameLogic.Game.TargetInfo;


@SuppressWarnings({ "serial" })
public class CharacterInfoCellRenderer extends JPanel implements ListCellRenderer<Object> {
	private final static Color bgColor = SpriteSheetUtility.ValueBGColor;
	private final static Dimension cellSize = GUIUtil.GetRelativeSize(0.2f, 0.2f);
	private final static Dimension cardSize = GUIUtil.GetRelativeSize(0.2f, 0.15f);
	
	CharacterCard characterCard;
	TargetInfoPanel targetInfoPanel;
	
	public CharacterInfoCellRenderer() {
		super();
		setOpaque(true);
		setBackground(bgColor);
		setSize(cellSize);
		setPreferredSize(cellSize);
		
		this.setLayout( new BoxLayout(this, BoxLayout.Y_AXIS) );
		
		characterCard = new CharacterCard(new Point(0, 0), cardSize);
		characterCard.setSize(cardSize);
		characterCard.setPreferredSize(cardSize);
		characterCard.setVisible(true);
		add(characterCard);
		
		Dimension infoPanelSize = new Dimension(cardSize.width, cellSize.height - cardSize.height);
		targetInfoPanel = new TargetInfoPanel(infoPanelSize);
		targetInfoPanel.setSize(infoPanelSize);
		targetInfoPanel.setPreferredSize(infoPanelSize);
		targetInfoPanel.setVisible(true);
		//targetInfoPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
		add(targetInfoPanel);
	}
	
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        TargetInfo targetInfo = (TargetInfo)value;
        characterCard.DisplayCharacter(targetInfo.target);
        targetInfoPanel.DisplayInfo(targetInfo);
        return this;
    }
}
