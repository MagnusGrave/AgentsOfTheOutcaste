package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import enums.ColorBlend;

@SuppressWarnings("serial")
public class CustomButtonUltra extends JLayeredPane {
	//Top Layer
	private ImagePanel icon;
	private JFxLabel fxText;
	
	//Mid Layer
	private JLabel nineconContainer;
	private Ninecon idle;
	private Ninecon hover;
	private Ninecon pressed;
	
	private Ninecon disabled;
	private final Color disabledColor = Color.GRAY;
	
	//Bottom Layer
	private ButtonCover button;
	private boolean isPressed;
	private Color buttonBGColor;
	
	//Icon placement magic numbers
	//private final float iconScale = 0.6f;
	//private final float iconPos = (1f - iconScale) / 2; //0.1f;
	//private final float iconUpY = 0.06f;
	
	public CustomButtonUltra(BufferedImage icon, Color normalColor, Color hoverColor, BufferedImage upImage, BufferedImage downImage, Color buttonBGColor) {
		super();
		
		this.icon = new ImagePanel(icon);
		//this.icon.setSize(Math.round(this.getWidth() * iconScale), Math.round(this.getHeight() * iconScale));
		JPanel iconPanel = new JPanel(new BorderLayout());
		iconPanel.add(this.icon, BorderLayout.CENTER);
		this.add(iconPanel, -1);
		
		this.buttonBGColor = buttonBGColor;
		
		CreateLabelAndButton(normalColor, hoverColor, upImage, downImage);
	}
	
	public CustomButtonUltra(JFxLabel fxText,  Color normalColor, Color hoverColor, BufferedImage upImage, BufferedImage downImage, Color buttonBGColor) {
		super();
		
		this.fxText = fxText;
		//this.fxText.setSize(new Dimension(Math.round(this.getWidth() * iconScale), Math.round(this.getHeight() * iconScale)));
		this.add(this.fxText, 0);
		
		this.buttonBGColor = buttonBGColor;
		
		//For debugging
		//this.fxText.setBorder(BorderFactory.createLineBorder(Color.CYAN, 2));
		
		CreateLabelAndButton(normalColor, hoverColor, upImage, downImage);
	}
	
	private final int cornerSize = 12;
	
	private void CreateLabelAndButton(Color normalColor, Color hoverColor, BufferedImage upImage, BufferedImage downImage) {
		BufferedImage normalImage = null;
		originalIdleColor = normalColor;
		originalUpImage = upImage;
		if(normalColor == null) {
			normalImage = upImage;
			originalIdleColor = Color.WHITE;
		} else {
			normalImage = GUIUtil.GetTintedImage(upImage, normalColor);
		}
		idle = new Ninecon(normalImage, cornerSize, cornerSize, cornerSize, cornerSize, 4f, null, null);
		
		disabled = new Ninecon(upImage, cornerSize, cornerSize, cornerSize, cornerSize, 4f, disabledColor, ColorBlend.Multiply);
		
		BufferedImage hoverImage = null;
		BufferedImage pressedImage = null;
		if(hoverColor == null) {
			hoverImage = upImage;
			pressedImage = downImage;
		} else {
			hoverImage = GUIUtil.GetTintedImage(upImage, hoverColor);
			pressedImage = GUIUtil.GetTintedImage(downImage, hoverColor);
		}
		hover = new Ninecon(hoverImage, cornerSize, cornerSize, cornerSize, cornerSize, 4f, null, null);
		pressed = new Ninecon(pressedImage, cornerSize, cornerSize, cornerSize, cornerSize, 4f, null, null);
		
		this.nineconContainer = new JLabel(idle);
		this.add(this.nineconContainer, 1);
		
		button = new ButtonCover();
		button.addMouseListener(new MouseAdapter() {
	         public void mouseEntered(MouseEvent me) {
	        	 if(!isEnabled())
	        		 return;
	        	 
	        	 //System.out.println("mouseEntered: " + fxText.getText());
	        	 UpdateIcon(hover);

	        	 NotifyGroup();
	         }
	         public void mouseExited(MouseEvent me) {
	        	 if(!isEnabled())
	        		 return;
	        	 
	        	 //System.out.println("mouseExited: " + fxText.getText());
	        	 UpdateIcon(idle);

	        	 NotifyGroup();
	        	 
	        	 isPressed = false;
	         }
	         public void mousePressed(MouseEvent e) {
	        	 if(!isEnabled())
	        		 return;
	        	 
	        	 //System.out.println("mousePressed: " + fxText.getText());
	        	 UpdateIcon(pressed);
	        	 
	        	 //Hacky way to call repaint next frame
	        	 Timer nextFrameTimer = new Timer(1, new ActionListener() {
					@Override public void actionPerformed(ActionEvent arg0) {
						revalidate();
						repaint();
					}
	        	 });
	        	 nextFrameTimer.setRepeats(false);
	        	 nextFrameTimer.start();
	        	 
	        	 isPressed = true;
	         }
	         public void mouseReleased(MouseEvent e) {
	        	 if(!isEnabled())
	        		 return;
	        	 
	        	 //System.out.println("mouseReleased: " + fxText.getText());
	        	 UpdateIcon(idle);

	        	 NotifyGroup();
	        	 
	        	 isPressed = false;
	         }
		});
		
		//Remove focus border
		button.setBorder(null);
		//Disguise button
		button.setOpaque(true);
		button.setBackground(buttonBGColor);
		
		button.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {
				//System.out.println("Gained Focus: " + fxText.getText());
			}
			@Override
			public void focusLost(FocusEvent arg0) {
				//System.out.println("Lost Focus: " + fxText.getText());
				revalidate();
				repaint();
			}
		});
		
		this.add(button, 2);
	}

	private void UpdateIcon(Ninecon ncon) {
		//System.out.println("UpdateIcon: " + fxText.getText());
		
		this.nineconContainer.setIcon(ncon);
	}
	
	Color originalIdleColor;
	BufferedImage originalUpImage;
	public void SetIdleColor(Color newColor) {
		if(newColor == null)
			newColor = originalIdleColor;
		
		BufferedImage normalImage = GUIUtil.GetTintedImage(originalUpImage, newColor);
		idle = new Ninecon(normalImage, cornerSize, cornerSize, cornerSize, cornerSize, 4f, null, null);
		this.nineconContainer = new JLabel(idle);
	}
	
	//Group - Start
	CustomButtonUltra[] buttonGroup;
	//boolean isNotifier;
	
	//Called by the creator of the buttons once all buttons have been created and added to a list
	public void AddGroupList(List<CustomButtonUltra> newButtonGroup) {
		buttonGroup = newButtonGroup.stream().toArray(CustomButtonUltra[]::new);
	}
	
	//Called by mouse event delegates
	private void NotifyGroup() {
		for(CustomButtonUltra cusBut : buttonGroup) {
			cusBut.ResetFromGroup();
		}
	}
	
	//Called by other buttons in group
	public void ResetFromGroup() {
		//System.out.println("Resetting: " + fxText.getText());
		
		revalidate();
		repaint();
	}
	//Group - End
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		button.setEnabled(enabled);
		UpdateIcon(enabled ? idle : disabled);
	}
	
	//Emulate JButton action implementation
	public void addActionListener(ActionListener l) {
		button.addActionListener(l);
	}
	
	private final float textScale = 0.6f;
	private final float textPos = (1f - textScale) / 2;
	private final float textUpY = 0.06f;
	private final float textDownY = textScale / 2f;
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		//System.out.println("Painting: " + fxText.getText());
		
		//button.setLocation(this.getLocation());
		button.setSize(this.getSize());
		
		//nineconContainer.setLocation(this.getLocation());
		this.nineconContainer.setSize(this.getSize());

		if(icon != null) {
			icon.setLocation(this.getLocation());
			icon.setSize(this.getSize());
			
			//int xPos = Math.round(this.getWidth() * iconPos);
			//Point iconLocation = isPressed ? new Point(xPos, Math.round(this.getHeight() * iconPos)) : new Point(xPos, Math.round(this.getHeight() * iconUpY));
			//icon.setLocation(iconLocation.x, iconLocation.y);
			icon.setLocation(0, 0);
		} else {
			this.fxText.setSize(new Dimension(Math.round(this.getWidth() * textScale), Math.round(this.getHeight() * textScale)));
			
			//Set the location of the label
			//System.out.println("CustomButtonUltra.paintComponent() - Paint Height : " + fxText.getHeight());
			
			int xPos = Math.round(this.getWidth() * textPos);
			int yPos = isPressed ? Math.round(fxText.getHeight() * textDownY) : Math.round(fxText.getHeight() * textUpY);
			Point textLocation = new Point(xPos, yPos);
			this.fxText.setLocation(textLocation);
		}
	}
}
