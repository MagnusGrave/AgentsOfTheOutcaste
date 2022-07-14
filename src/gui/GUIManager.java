package gui;

import javax.imageio.ImageIO;
import javax.swing.*;

import data.CharacterData;
import enums.MenuType;
import gameLogic.Game;
import gameLogic.Missions;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class GUIManager implements ITransitionListener {
	private static GUIManager instance;
	//Called from Main in place of a constructor
	public static void Initialize() {
		 new GUIManager();
	}
	
	//private static JFrame frame;
	private MyFrame frame;
	/**
	 * The Main Container's root component. This root layeredPane facilitates the layering of persistent GUI buttons(Settings & Close) and the settings menu GUI over the gamePanel
	 */
	JLayeredPane layeredPane;
	private CardLayout cardLayout = new CardLayout();
	private JPanel gamePanel;
	private FadeTransitionPanel fadeTransPanel;
	public static FadeTransitionPanel GetFadeTransitionPanel() { return instance.fadeTransPanel; }
	
	private MapLocationPanel mapLocationPanel;
	public static MapLocationPanel MapLocationPanel() { return instance.mapLocationPanel; }
	
	private final int dragEventThreshold = 10;
	public static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	public static Dimension minimumWindowSize = new Dimension(800, 800);
	public static Dimension innerDimension = new Dimension(minimumWindowSize.width - 16, minimumWindowSize.height - 38);
	
	public static Dimension GetWindowSize() {
		return instance.frame.getBounds().getSize();
	}
	public static Dimension GetContentSize() {
		return isWindowed ? innerDimension : screenSize;
	}
	private static Dimension GetWindowFrameSize() {
		return isWindowed ? minimumWindowSize : screenSize;
	}
	private static boolean isWindowed;
	
	public enum BgType { Mountains, DemonWoods };
	public class ParallaxBg {
		public List<ParallaxPanel> parallaxPanels = new ArrayList<ParallaxPanel>();
		private BgType currentBgType = BgType.Mountains;
		public BgType getCurrentBgType() { return currentBgType; }
		
		public void SetBg(BgType type) {
			if(currentBgType == type)
				return;
			currentBgType = type;
			switch(currentBgType) {
				case Mountains:
					for(int i = 0; i < parallaxPanels.size(); i++) {
						ParallaxPanel parallaxPanel = parallaxPanels.get(i);
						int reverseI = parallaxPanels.size() - 1 - i;
						BufferedImage image = GUIUtil.GetBuffedImage("titleScreen/Mountains/parallax-mountain_" + reverseI + ".png");
						parallaxPanel.parallaxImages[0].SetNewImage(image);
						if(parallaxPanel.parallaxImages.length > 1)
							parallaxPanel.parallaxImages[1].SetNewImage(image);
					}
					break;
				case DemonWoods:
					for(int i = 0; i < parallaxPanels.size(); i++) {
						ParallaxPanel parallaxPanel = parallaxPanels.get(i);
						int reverseI = parallaxPanels.size() - 1 - i;
						BufferedImage image = GUIUtil.GetBuffedImage("titleScreen/DemonWoods/demonWoods_" + reverseI + ".png");
						parallaxPanel.parallaxImages[0].SetNewImage(image);
						if(parallaxPanel.parallaxImages.length > 1)
							parallaxPanel.parallaxImages[1].SetNewImage(image);
					}
					break;
				default:
					System.out.println("GUIMNanager.ParallaxBg.SetBG() - Add support for: " + currentBgType);
					break;
			}
		}
	}
	private ParallaxBg parallaxBg;
	public class ParallaxPanel {
		public ParallaxPanel(ImagePanel panel, ImagePanel repeatPanel) {
			parallaxImages = new ImagePanel[repeatPanel == null ? 1 : 2];
			parallaxImages[0] = panel;
			if(repeatPanel != null)
				parallaxImages[1] = repeatPanel;
		}
		ImagePanel[] parallaxImages;
	}
	List<Timer> parallaxTimers = new ArrayList<Timer>();
	
	
	public GUIManager() {
		instance = this;
		
		//Stuff that needs to be created for us in other areas
		worldmapPanel = new WorldmapPanel();
		
		//Setup frame
		//frame = new JFrame("Republics and Overlords");
		frame = new MyFrame("Agents of the Outcaste");
		if(isWindowed) {
			frame.setSize(GetWindowFrameSize());
			frame.setMinimumSize(GetWindowFrameSize());
			frame.setLocation(500, 10);
		} else {
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
			frame.setUndecorated(true);
			frame.setAlwaysOnTop(true);
			frame.setAutoRequestFocus(true);
		}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        Game.Instance().SaveData();
				System.exit(0);
		    }
		});
		
		
		//Debug the unianimous visual artifacts that appear after a period of inactivity
		int inactivityTimer_minutes = 5;
		@SuppressWarnings("serial")
		Action debugInactivity = new AbstractAction()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        System.err.println("GUIManager.InactivityListener.AbstractAction - Frame has been inactive for minutes: "+ inactivityTimer_minutes +". Are their visual artifacts?");
		    }
		};
		InactivityListener listener = new InactivityListener(frame, debugInactivity, inactivityTimer_minutes);
		listener.start();
		
		
		//Facilitate Settings button and panel overlays 
		layeredPane = new JLayeredPane();
		layeredPane.setPreferredSize(GetWindowFrameSize());
		layeredPane.setOpaque(false);
		layeredPane.setBackground(new Color(0,0,0,0));
		
		
		//Setup frame container
		Container mC = frame.getContentPane();
		mC.setBackground(Color.DARK_GRAY);
		mC.add(layeredPane);
		
		//Parallax Layers
		boolean debugParallax = false;
		
		JLayeredPane parallaxLayeredPane = new JLayeredPane();
		parallaxLayeredPane.setSize(GetContentSize());
		parallaxLayeredPane.setPreferredSize(GetContentSize());
		parallaxLayeredPane.setOpaque(false);
		
		parallaxBg = new ParallaxBg();
		
		for(int i = 0; i < 5; i++) {
			JPanel bgPanel = new JPanel(new GridLayout(1, 2));
			if(debugParallax)
				bgPanel.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
			
			ImagePanel parallaxImage = new ImagePanel("titleScreen/Mountains/parallax-mountain_" + (4 - i) + ".png");
			parallaxImage.setPreferredSize(GetContentSize());
			if(i < 4)
				parallaxImage.ConformPreferredSizeToAspectRatio(false);
			else
				parallaxImage.ConformPreferredSizeToAspectRatio(true);
			parallaxImage.setMinimumSize(parallaxImage.getPreferredSize());
			parallaxImage.setOpaque(false);
			if(debugParallax)
				parallaxImage.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
			
			bgPanel.setSize(parallaxImage.getPreferredSize());
			bgPanel.add(parallaxImage);
			
			if(i < 4) {
				bgPanel.setSize(new Dimension(parallaxImage.getPreferredSize().width*2, parallaxImage.getPreferredSize().height));
				//make on the parallaxing layers transparent
				bgPanel.setOpaque(false);
				bgPanel.setBackground(new Color(0,0,0,0));
				
				ImagePanel repeatImage = new ImagePanel("titleScreen/Mountains/parallax-mountain_" + (4 - i) + ".png");
				repeatImage.setPreferredSize(parallaxImage.getPreferredSize());
				//repeatImage.ConformPreferredSizeToAspectRatio(false);
				repeatImage.setOpaque(false);
				bgPanel.add(repeatImage);
				if(debugParallax)
					repeatImage.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
				
				parallaxBg.parallaxPanels.add(new ParallaxPanel(parallaxImage, repeatImage));
				
				final boolean debuggingFirstLayer = i == 0;
				int milliseconds = (int)Math.pow(4.0, (double)i) * 10;
				final int negativeTileWidth = -parallaxImage.getPreferredSize().width;
				if(debugParallax && debuggingFirstLayer) {
					System.out.println("GUIManager - negativeTileWidth: " + negativeTileWidth);
					System.out.println("GUIManager - repeatImage.localX: " + repeatImage.getLocation().x);	
				}
				
				Timer animTimer = new Timer(milliseconds, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						//reset to start
						if(bgPanel.getLocation().x <= negativeTileWidth)
							bgPanel.setLocation(0, bgPanel.getLocation().y);
						
						bgPanel.setLocation(bgPanel.getLocation().x - 1, bgPanel.getLocation().y);
						
						if(debugParallax && debuggingFirstLayer)
							System.out.println("GUIManager - First Layer Location: " + bgPanel.getLocation().x + " <= " + negativeTileWidth);
					}
				});
				parallaxTimers.add(animTimer);
				animTimer.start();
			}
			
			parallaxLayeredPane.add(bgPanel, i);
		}
		layeredPane.add(parallaxLayeredPane, 1);
		

		//BG Panel Test
		/*JPanel bgPanel = new JPanel(new BorderLayout());
		bgPanel.setLocation(100, 300);
		bgPanel.setSize((int)(GetTargetSize().width/1.5f), GetTargetSize().height/4);
		bgPanel.setOpaque(false);
		bgPanel.setBackground(new Color(0,0,0,0));
		JLabel bgContainer = new JLabel(SpriteSheetUtility.PanelBG_Paper());
		bgPanel.add(bgContainer, BorderLayout.CENTER);
		layeredPane.add(bgPanel, 1);*/
		
		gamePanel = new JPanel();
		gamePanel.setSize(GetContentSize());
		gamePanel.setMinimumSize(GetContentSize());
		//Try doing these two lines later
		gamePanel.setLayout(cardLayout);
		//Reveal the layered pane behind this one
		gamePanel.setOpaque(false);
		gamePanel.setBackground(new Color(0,0,0,0));
		//Set game panel as the first
		//layeredPane.add(gamePanel, new Integer(1), 1);
		layeredPane.add(gamePanel, 0);
		
		JPanel menuOverlay = new JPanel(new BorderLayout());
		
		//Add Settings button as second layer
		//Icon icon = null;
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("resources/gui/SettingsSymbol.png");
		BufferedImage imge = null;
		try {
			imge = ImageIO.read(is);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//icon = new ImageIcon(imge);
		
		//Overlay Buttons
		float buttonScale = 0.04f;
		//float buttonSpacing = 0.02f;
		
		//JButton settingsButton = new JButton(icon);
		CustomButton settingsButton = new CustomButton(imge, 
													null, Color.BLUE,
													SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(),
													Color.DARK_GRAY);
		Dimension buttonSize = GUIUtil.GetRelativeSize(buttonScale, true);
		settingsButton.setSize(buttonSize);
		//Dimension buttonInset = GUIUtil.GetRelativeSize(buttonSpacing, true);
		Dimension buttonInset = new Dimension(0, 0);
		settingsButton.setLocation(GUIUtil.GetCornerWithInset(new Point(1,0), new Point((-buttonSize.width - buttonInset.width)*2, buttonInset.height)));
		settingsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				menuOverlay.setVisible(true);
				settingsButton.setVisible(false);
			}
		});
		layeredPane.add(settingsButton, new Integer(2), 2);
		
		//Close button
		if(!isWindowed) {
			//JButton closeButton = new JButton(SpriteSheetUtility.CrossIcon());
			CustomButton closeButton = new CustomButton(SpriteSheetUtility.CrossIcon(), 
														null, Color.RED,
														SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(),
														Color.DARK_GRAY);
			buttonSize = GUIUtil.GetRelativeSize(buttonScale, true);
			closeButton.setSize(buttonSize);
			//buttonInset = GUIUtil.GetRelativeSize(buttonSpacing, true);
			buttonInset = new Dimension(0, 0);
			closeButton.setLocation(GUIUtil.GetCornerWithInset(new Point(1,0), new Point(-buttonSize.width - buttonInset.width, buttonInset.height)));
			closeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					Game.Instance().SaveData();
					System.exit(0);
				}
			});
			layeredPane.add(closeButton, new Integer(2), 2);
		}
		
		//Add Menu Overlay as third layer
		menuOverlay.setSize(GetContentSize());
		//menuOverlay.setBackground(new Color(0f, 0f, 0f, 0.5f));
		//menuOverlay.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		menuOverlay.setVisible(false);
		
			JPanel buttonBox = new JPanel();
			buttonBox.setLayout(new BoxLayout(buttonBox, BoxLayout.Y_AXIS));
			buttonBox.setPreferredSize(new Dimension(300, 400));
			buttonBox.setMaximumSize(new Dimension(300, 400));
			//buttonBox.setBorder(BorderFactory.createLineBorder(Color.PINK));
			
					JPanel audioPanel = new JPanel(new BorderLayout());
					audioPanel.setSize(GetContentSize());
					audioPanel.setVisible(false);
					
						JPanel audioComponentBox = new JPanel();
						audioComponentBox.setLayout(new BoxLayout(audioComponentBox, BoxLayout.Y_AXIS));

							JLabel musicLabel = new JLabel("Background Music Volume");
							audioComponentBox.add(musicLabel);
						
							JSlider musicSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, Game.Instance().GetBGMusicVolume());
							musicSlider.addChangeListener(worldmapPanel.getBgMusicThread());
							musicSlider.setMajorTickSpacing(50);
							musicSlider.setMinorTickSpacing(1);
							musicSlider.setPaintTicks(false);
							musicSlider.setPaintLabels(true);
							audioComponentBox.add(musicSlider);
						
						audioPanel.add(audioComponentBox, BorderLayout.CENTER);
						
						JButton closeAudioButton = new JButton("Done");
						closeAudioButton.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent arg0) {
								audioPanel.setVisible(false);
								Game.Instance().SetBGMusicVolume(musicSlider.getValue());
							}
						});
						audioPanel.add(closeAudioButton, BorderLayout.SOUTH);
					
					layeredPane.add(audioPanel, new Integer(2), 4);	
			
				JButton audioSettings = new JButton("Audio Settings");
				audioSettings.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						audioPanel.setVisible(true);
					}
				});
				buttonBox.add(audioSettings);
				
				JButton closeButton = new JButton("Back");
				closeButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						menuOverlay.setVisible(false);
						settingsButton.setVisible(true);
					}
				});
				buttonBox.add(closeButton);
			
			menuOverlay.add(buttonBox, BorderLayout.CENTER);
			
		layeredPane.add(menuOverlay, new Integer(2), 3);
		
		
		//Create the FadTransitionPanel to layer over everything else
		fadeTransPanel = new FadeTransitionPanel(Color.BLACK);
		fadeTransPanel.setSize(GetContentSize());
		fadeTransPanel.setMinimumSize(GetContentSize());
		layeredPane.add(fadeTransPanel, new Integer(3), 4);	
		
		
		//Setup Card Panels - Start
		
		//Welcome Screen
		JPanel mainMenuPanel = new JPanel(new BorderLayout(1, 1));
		//mainMenuPanel.setBorder(BorderFactory.createMatteBorder(2,2,2,2,Color.RED));
		mainMenuPanel.setOpaque(false);
		mainMenuPanel.setBackground(new Color(0,0,0,0));
		
		//Title Name
		JPanel titleSpacerPanel = new JPanel(new BorderLayout());
		titleSpacerPanel.setOpaque(false);
		titleSpacerPanel.setBackground(new Color(0,0,0,0));
		JFxLabel titleLabel = new JFxLabel("Agents of the Outcaste", SwingConstants.CENTER, GUIUtil.Title, Color.WHITE)
				.withStroke(Color.BLACK, 4, true)
				.withShadow(Color.GRAY, new Point(2, 2));
		titleSpacerPanel.add(titleLabel, BorderLayout.CENTER);
		titleSpacerPanel.add(Box.createVerticalStrut(290), BorderLayout.SOUTH);
		mainMenuPanel.add(titleSpacerPanel, BorderLayout.CENTER);
		
		//Options
		JPanel buttonGrid = new JPanel(new GridLayout(1, 1, 0, 0));
		if(Game.Instance().DoesPlayerDataExist())
			buttonGrid = new JPanel(new GridLayout(1, 2, 0, 0));
		buttonGrid.setPreferredSize(GUIUtil.GetRelativeSize(1f, 0.1f));
		buttonGrid.setOpaque(false);
		buttonGrid.setBackground(new Color(0,0,0,0));
		
		List<CustomButtonUltra> ultraButtonGroup = new ArrayList<CustomButtonUltra>(); 
		//JButton startButton = new JButton("New Game");
		CustomButtonUltra newGameButton = new CustomButtonUltra(new JFxLabel("New Game", SwingConstants.CENTER, GUIUtil.ItalicHeader, Color.WHITE)
																	.withStroke(Color.BLACK, 2, false),
																null, Color.green.darker(),
																SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(),
																Color.DARK_GRAY);
		ultraButtonGroup.add(newGameButton);
		
		newGameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cardLayout.show(gamePanel, MenuType.CHARACTER.toString());
				currentMenuType = MenuType.CHARACTER;
				
				//Clear the previous game data here cause if the player exits on the character creation screen then there will be an error due to a false positive on the game load state
				Game.Instance().ResetSaveData();
			}
		});
		buttonGrid.add(newGameButton);
		
		//Store the GUI componets that'll need killing after the game gens or loads
		parallaxLayeredPane_killTarget = parallaxLayeredPane;
		mainMenuPanel_killTarget = mainMenuPanel;
		
		CustomButtonUltra loadGameButton = null;
		if(Game.Instance().DoesPlayerDataExist()) {
			//JButton loadButton = new JButton("Load Game");
			loadGameButton = new CustomButtonUltra(new JFxLabel("Load Game", SwingConstants.CENTER, GUIUtil.ItalicHeader, Color.WHITE).withStroke(Color.BLACK, 2, false),
																	null, Color.green.darker(),
																	SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(),
																	Color.DARK_GRAY);
			ultraButtonGroup.add(loadGameButton);
			
			/*loadGameButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					worldmapPanel.LoadWorld();
					ShowScreen(MenuType.WORLDMAP);
				}
			});*/
			GUIManager thisClass = this;
			CustomButtonUltra finalLoadGameButton = loadGameButton;
			loadGameButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					fadeTransPanel.Fade(true, thisClass);
					pendingAction = new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							worldmapPanel.LoadWorld();
							
							//ShowScreen(MenuType.WORLDMAP);
							//if(Game.Instance().GetCurrentGraphPath() == null)
							//This is the new mechanism to track the game focus
							if(Game.Instance().IsInMapLocation())
								ShowScreen(MenuType.LOCATION);
							else
								ShowScreen(MenuType.WORLDMAP);
							
							KillUnusedGUI();
							
							//If LoadWorld() was a Runnable then we'd wait till the end of its process to callback to this class
							//but since LoadWorld() should stall further execution until its complete do this now
							fadeTransPanel.Fade(false, 120);
						}
					};
					newGameButton.setEnabled(false);
					finalLoadGameButton.setEnabled(false);
				}
			});
			
			buttonGrid.add(loadGameButton);
		}
		
		mainMenuPanel.add(buttonGrid, BorderLayout.SOUTH);
		
		gamePanel.add(mainMenuPanel, MenuType.MAINMENU.toString());
		
		
		//Add all buttons to a group so that they can elimate eachothers artifacting
		for(CustomButtonUltra ultra : ultraButtonGroup)
			ultra.AddGroupList(ultraButtonGroup);
		
		
		//Character Creation Panel
		//CreationPanel characterPanel = new CreationPanel();
		creationPanel = new CreationPanel();
		creationPanel.setLayout(new BorderLayout(1, 1));
		creationPanel.setBackground(Color.GRAY);
		//characterPanel.setBorder(BorderFactory.createMatteBorder(2,2,2,2,Color.BLUE));
		//The internal setup must occur after the panel has been prepared
		creationPanel.Initialize(this);
		gamePanel.add(creationPanel, MenuType.CHARACTER.toString());
		
		//Setup MapLocation Panel
		mapLocationPanel = new MapLocationPanel(worldmapPanel);
		gamePanel.add(mapLocationPanel, MenuType.LOCATION.toString());
		
		//Setup Worldmap
		worldmapPanel.setLayout(new BorderLayout(1, 1));
		worldmapPanel.setBackground(Color.GRAY);
		//worldmapPanel.setBorder(BorderFactory.createMatteBorder(2,2,2,2,Color.GREEN));
		//The internal setup must occur after the panel has been prepared
		worldmapPanel.Initialize(this, mapLocationPanel);
		gamePanel.add(worldmapPanel, MenuType.WORLDMAP.toString());
		
		//Setup Battle Panel
		BattlePanel battlePanel = new BattlePanel();
		battlePanel.setLayout(new BorderLayout(1, 1));
		//battlePanel.setBackground(Color.GRAY);
		//battlePanel.setBorder(BorderFactory.createMatteBorder(2,2,2,2,Color.BLUE));
		//Wrap the BattlePanel with the Board class, this adds a grid panel to the central area of the Battle Panel and then initializes it
		//need to wait to setup the battlefield until we have our BattleData
		Game.Instance().SetBattlePanel(battlePanel);
		
		//gamePanel.add(battlePanel, MenuType.BATTLE.toString());
		
		//Setup Continue Screen (Game Over)
		ContinuePanel continuePanel = new ContinuePanel(worldmapPanel);
		gamePanel.add(continuePanel, MenuType.GAMEOVER.toString());
		
		//Setup Card Panels - End
		
		//Finalize GUI setup
		cardLayout.show(gamePanel, MenuType.MAINMENU.toString());
		currentMenuType = MenuType.MAINMENU;
		//frame.pack(); //Condenses everything down to minimum space
		frame.setVisible(true);
		
		System.setProperty("awt.dnd.drag.threshold", "" + dragEventThreshold);
		
		currentScreen = showOnSecondaryScreen ? leftScreen : rightScreen;
		//Set full screen mode to enable graphics hardware acceleration
		if(!DEBUG_allowDevHotkeys) { //Disable this mode for development so that the app can be toggled between monitors
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			if(!showOnSecondaryScreen) {
				GraphicsDevice gd = ge.getDefaultScreenDevice();
				
				//This may be harming more than its helping
				//strangely with it disabled the screen is no longer minimizable which is kind of good but the fadeTransition is nonfunctional and framerate is too fast
				gd.setFullScreenWindow(frame);
				
				//investigate the display setting to try and stabilize the artifacting and timing issues
				//Print info for tracking which settings yield the best results
				/*String idName = gd.getType() == GraphicsDevice.TYPE_IMAGE_BUFFER ? "TYPE_IMAGE_BUFFER" :
					(gd.getType() == GraphicsDevice.TYPE_PRINTER ? "TYPE_PRINTER" : "TYPE_RASTER_SCREEN");
				System.out.println(
				"_|_|_ -.- CURRENT SETTINGS -.- _|_|_" + "\n" +
				"AvailableAcceleratedMemory: " + gd.getAvailableAcceleratedMemory() + "\n" +
				"Type: " + idName + "\n" +
				"DisplayMode.BitDepth: " + gd.getDisplayMode().getBitDepth() + "\n" +
				"DisplayMode.RefreshRate: " + gd.getDisplayMode().getRefreshRate() + "\n" +
				"Frame.isAutoRequestFocus: " + frame.isAutoRequestFocus() + "\n" +
				"_|_|_ -.- CURRENT SETTINGS -.- _|_|_"
				);*/
				
				//DisplayMode[] displayModes =  gd.getDisplayModes();
				//for(DisplayMode mode : displayModes) {
				//	System.out.println("mode: " + mode.getRefreshRate());
				//}
			} else {
				GraphicsDevice[] gd = ge.getScreenDevices();
				gd[currentScreen].setFullScreenWindow(frame);
			}
		} else {
			showOnScreen(currentScreen, frame);
		}
	}
	
	//Animation stuff - Start
	ActionListener pendingAction;
	
	//FadeTRansitionPanel callback
	public void TransitionComplete() {
		pendingAction.actionPerformed(null);
	}
	
	// - Animation Stuff ->
	
	// <- Attempted Preformance Optimizations -
	
	JLayeredPane parallaxLayeredPane_killTarget;
	JPanel mainMenuPanel_killTarget;
	
	private void KillUnusedGUI() {
		//Kill menu parallax
		for(Timer laxTimer : parallaxTimers)
			laxTimer.stop();
		parallaxTimers.clear();
		parallaxBg = null;
		layeredPane.remove(parallaxLayeredPane_killTarget);
		
		//Kill main menu
		gamePanel.remove(mainMenuPanel_killTarget);
	}
	
	// - Attempted Preformance Optimizations ->
	
	//For performance reasons these bools should be set to false prior to final launch
	//Dev Hotkeys
	//	Alt+P - Screen Cap
	//	Alt+[ - Switch Screens
	public static final boolean DEBUG_allowDevHotkeys = false;
	private final boolean showOnSecondaryScreen = false;
	
	//Choose which screen the window displays in, any screen expect the main one will impede GUIUtil.CaptureScreenshot()
	private final int leftScreen = 0;
	private final int rightScreen = 1;
	private int currentScreen;
	public static void showOnScreen( int screen, JFrame frame ) {
	    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    GraphicsDevice[] gd = ge.getScreenDevices();
	    if( screen > -1 && screen < gd.length ) {
	    	//Point newFrameLoc = new Point(gd[screen].getDefaultConfiguration().getBounds().x, frame.getY());
	    	Point newFrameLoc = new Point(gd[screen].getConfigurations()[0].getBounds().x, frame.getY());
	    	
	    	System.out.println("newFrameLoc: " + newFrameLoc);
	        frame.setLocation(newFrameLoc);
	    } else {
	        throw new RuntimeException( "No Screens Found" );
	    }
	    
	    instance.currentScreen = screen;
	}
	
	public static void CycleScreens() {
		showOnScreen(instance.currentScreen == instance.leftScreen ? instance.rightScreen : instance.leftScreen, instance.frame);
	}
	
	CreationPanel creationPanel;
	WorldmapPanel worldmapPanel;
	public static WorldmapPanel WorldmapPanel() {
		if(instance == null)
			return null;
		else
			return instance.worldmapPanel;
	}
	
	public void SubmitPlayerDataAndResetSaveFile(CharacterData data) {
		//Disable CreationPanel
		for(Component cmp : creationPanel.getComponents()) {
			cmp.setEnabled(false);
		}
		
		//handle fade out 
		fadeTransPanel.Fade(true, this);
		pendingAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Stop parallax timers to save on resources
				/*for(Timer laxTimer : parallaxTimers) {
					laxTimer.stop();
					System.err.println("GUIManager.SubmitPlayerDataAndResetSaveFile(CharacterData data) - "
							+ "Also remove all the parallax components so they can be garbage collected.");
				}*/
				KillUnusedGUI();
				
				//Reset
				Game.Instance().ResetSaveData();
				Game.Instance().SetPlayerData(data);
				
				Missions.RebuildMissionsAndMapLocations();
				
				worldmapPanel.GenerateWorld();
				
				ShowScreen(MenuType.WORLDMAP);
				//in a new game always start in the MapLocation panel
				//ShowScreen(MenuType.LOCATION);
				//Nevermind, i think because of the world generation stuff we need to make the Worlmap panel active first, while leaving the fadeTransition at opaque black
				//and then showing the map location screen upon completion of worldmap setup
				
				//fadeTransPanel.Fade(false, 120);
			}
		};
	}
	
	private MenuType currentMenuType;
	public static MenuType getCurrentMenuType() { return instance.currentMenuType; }
	
	public static void ShowScreen(MenuType menuType) {
		instance.currentMenuType = menuType;
		instance.cardLayout.show(instance.gamePanel, menuType.toString());
		if(menuType == MenuType.WORLDMAP)
			instance.worldmapPanel.OnPanelShown();
		else if(menuType == MenuType.LOCATION)
			instance.mapLocationPanel.OnPanelShown();
		else if(menuType == MenuType.GAMEOVER) {
			//switch the parallax panel to the dead forest images
			instance.parallaxBg.SetBg(BgType.DemonWoods);
		}
		
		if(menuType != MenuType.LOCATION)
			instance.mapLocationPanel.StopUpdateLoop();
	}
 }
