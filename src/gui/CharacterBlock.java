package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;

import javax.swing.JLayeredPane;
import javax.swing.SwingConstants;

import data.CharacterData;


@SuppressWarnings("serial")
public class CharacterBlock extends JLayeredPane {
	private Point2D staticSize = new Point2D.Float(0.15f, 0.2f);
	
	public CharacterBlock(CharacterData data) {
		super();
		this.data = data;
	}
	public void Initialize() {
		//this.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 1));
		
		portraitImage = new ImagePanel(GUIUtil.GetBuffedImage(data.getPortraitPath()));
		//portraitImage.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
		portraitImage.setBackground(new Color(0,0,0,0));
		portraitImage.setOpaque(false);
		Point2D imageNormSize = new Point2D.Float(0.41f, 0.41f);
		Point imageLoc = GUIUtil.GetRelativePoint(0.045f, 0.03f);
		Dimension imageSize = GUIUtil.GetRelativeSize((float)imageNormSize.getX(), (float)imageNormSize.getY());
		portraitImage.setBounds((int)Math.round(imageLoc.x * staticSize.getX()), (int)Math.round(imageLoc.y * staticSize.getY()),
								(int)Math.round(imageSize.width * staticSize.getX()), (int)Math.round(imageSize.height * staticSize.getY()));
		portraitImage.ConformSizeToAspectRatio(true);
		
		
		nameLabel = new JFxLabel(data.getName(), SwingConstants.CENTER, GUIUtil.Body_2, Color.WHITE);
		//nameLabel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
		Point2D quantityNormSize = new Point2D.Float(0.44f, 0.15f);
		Point quantityLoc = GUIUtil.GetRelativePoint(0.03f, 0.58f);
		Dimension quantitySize = GUIUtil.GetRelativeSize((float)quantityNormSize.getX(), (float)quantityNormSize.getY());
		nameLabel.setBounds((int)Math.round(quantityLoc.x * staticSize.getX()), (int)Math.round(quantityLoc.y * staticSize.getY()),
							   (int)Math.round(quantitySize.width * staticSize.getX()), (int)Math.round(quantitySize.height * staticSize.getY()));
		this.add(nameLabel, 0, 0);
		
		this.add(portraitImage, 0, -1);
		
		ImagePanel bg = new ImagePanel( SpriteSheetUtility.PanelBG_Stone() );
		//bg.setBorder(BorderFactory.createLineBorder(Color.CYAN, 1));
		bg.setBackground(new Color(0,0,0,0));
		bg.setOpaque(false);
		Point2D bgNormSize = new Point2D.Float(0.46f, 0.75f);
		Point bgLoc = GUIUtil.GetRelativePoint(0.02f, 0f);
		Dimension bgSize = GUIUtil.GetRelativeSize((float)bgNormSize.getX(), (float)bgNormSize.getY());
		bg.setBounds((int)Math.round(bgLoc.x * staticSize.getX()), (int)Math.round(bgLoc.y * staticSize.getY()),
				(int)Math.round(bgSize.width * staticSize.getX()), (int)Math.round(bgSize.height * staticSize.getY()));
		this.add(bg, 0, -1);
	}
	ImagePanel portraitImage;
	JFxLabel nameLabel;
	CharacterData data;
}
