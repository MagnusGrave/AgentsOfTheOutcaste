package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;

import javax.swing.JLayeredPane;
import javax.swing.SwingConstants;

import data.ItemData;

/**
 * The representation of a single item or stack of the same items.
 * @author Magnus
 *
 */
@SuppressWarnings("serial")
public class ItemBlock extends JLayeredPane {
	private Point2D staticSize = new Point2D.Float(0.05f, 0.05f);
	
	public ItemBlock(ItemData data) {
		super();
		this.data = data;
	}
	public void Initialize() {
		//this.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 1));
		itemImage = new ImagePanel(GUIUtil.GetBuffedImage(data.GetFilePath()));
		itemImage.setBackground(new Color(0,0,0,0));
		itemImage.setOpaque(false);
		Point2D imageNormSize = new Point2D.Float(0.6f, 0.6f);
		Point imageLoc = GUIUtil.GetRelativePoint(0.5f - ((float)imageNormSize.getX() / 2f), 0.1f); //0.5f - ((float)imageNormSize.getY() / 2f));
		Dimension imageSize = GUIUtil.GetRelativeSize((float)imageNormSize.getX(), (float)imageNormSize.getY());
		Dimension scaledImageSize = new Dimension( (int)Math.round(imageSize.width * staticSize.getX()), (int)Math.round(imageSize.height * staticSize.getY()) );
		//itemImage.setBounds((int)Math.round(imageLoc.x * staticSize.getX()), (int)Math.round(imageLoc.y * staticSize.getY()),
		Point2D staticImageSize = new Point2D.Float(0.06f, 0.06f);
		itemImage.setBounds((int)Math.round(imageLoc.x * staticImageSize.getX()), (int)Math.round(imageLoc.y * staticImageSize.getY()),
							scaledImageSize.width, scaledImageSize.height);
		itemImage.ConformSizeToAspectRatio(true);
		if((float)itemImage.getImageWidth() / itemImage.getImageHeight() < 0.7f) {
			System.out.println("Switch aspect");
			itemImage.setSize(scaledImageSize.height, scaledImageSize.width);
			itemImage.ConformSizeToAspectRatio(false);
			int sizeOffset = (int)Math.round( GUIUtil.GetRelativeSize((float)imageNormSize.getX() / 4f, true).width * (float)staticSize.getX() );
			itemImage.setLocation( GUIUtil.GetRelativeSize((float)staticSize.getX() / 2f, true).width - sizeOffset, 0);
		}
		
		quantityLabel = new JFxLabel("x" + data.getQuantity(), SwingConstants.CENTER, GUIUtil.Body, Color.BLACK).withStroke(Color.WHITE, 1, true);
		Point2D quantityNormSize = new Point2D.Float(0.35f, 0.35f);
		Point quantityLoc = GUIUtil.GetRelativePoint(0.65f, 0.65f);
		Dimension quantitySize = GUIUtil.GetRelativeSize((float)quantityNormSize.getX(), (float)quantityNormSize.getY());
		quantityLabel.setBounds((int)Math.round(quantityLoc.x * staticSize.getX()), (int)Math.round(quantityLoc.y * staticSize.getY()),
							   (int)Math.round(quantitySize.width * staticSize.getX()), (int)Math.round(quantitySize.height * staticSize.getY()));
		
		this.add(itemImage, 0, 0);
		
		this.add(quantityLabel, 0, 0);
		
		ImagePanel bg = new ImagePanel( SpriteSheetUtility.CloudImage_Up() );
		//bg.setBorder(BorderFactory.createLineBorder(Color.CYAN, 1));
		bg.setBackground(new Color(0,0,0,0));
		bg.setOpaque(false);
		Point2D bgNormSize = new Point2D.Float(1f, 1f);
		Point bgLoc = GUIUtil.GetRelativePoint(0.5f - ((float)bgNormSize.getX() / 2f), 0.4f );//- ((float)bgNormSize.getY() / 4f));
		Dimension bgSize = GUIUtil.GetRelativeSize((float)bgNormSize.getX(), (float)bgNormSize.getY());
		bg.setBounds((int)Math.round(bgLoc.x * staticSize.getX()), (int)Math.round(bgLoc.y * staticSize.getY()),
				(int)Math.round(bgSize.width * staticSize.getX()), (int)Math.round(bgSize.height * staticSize.getY()));
		bg.ConformSizeToAspectRatio(false);
		this.add(bg, 0, -1);
	}
	ImagePanel itemImage;
	JFxLabel quantityLabel;
	ItemData data;
}
