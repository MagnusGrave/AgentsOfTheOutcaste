package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComboBoxUI;

import data.CharacterData;
import enums.ClassType;
import enums.StatType;


@SuppressWarnings("serial")
public class CreationPanel extends JPanel {
	JTextField nameField;
	final String defaultNameString = "NAME";
	JComboBox<ClassType> classPicker;
	ImagePanel avatarImage;
	CustomSpinner spinner_str;
	CustomSpinner spinner_int;
	CustomSpinner spinner_end;
	CustomSpinner spinner_spd;
	JLabel pointValueLabel;
	boolean isChangingStatsInternally;
	int distributionPoints = 5;
	final int STAT_BASE = 1;
	final int STAT_MIN = 1;
	final int STAT_MAX = 4;
	final int STAT_BONUS = 1; //Class-based bonus
	JLabel validationMessage;
	HashMap<StatType, StatData> statMap = new HashMap<StatType, StatData>();
	GUIManager manager;
	
	private final Color textColor = new Color(0.5f, 0.3f, 0.3f);
	private final Color subtleColor = Color.GRAY.darker();
	private final Color abilityWordColor = Color.green.darker();
	
	
	public void Initialize(GUIManager manager) {
		this.manager = manager;
		
		this.setOpaque(false);
		this.setBackground(new Color(0,0,0,0));
		
		//North Border Panel - Start
		
		//Use this to have a paper ninecon as BG and the UI panel as the foreground
		JLayeredPane layeredPane = new JLayeredPane();
		layeredPane.setOpaque(false);
		layeredPane.setBackground(new Color(0,0,0,0));
		layeredPane.setSize(GUIManager.GetContentSize());
		
		//Add the Background layer
		JLabel bgLabel = new JLabel(SpriteSheetUtility.PanelBG_PaperTiled());
		bgLabel.setSize(GUIUtil.GetRelativeSize(0.96f, 0.96f));
		bgLabel.setLocation(GUIUtil.GetRelativePoint(0.02f, 0.02f));
		layeredPane.add(bgLabel, 1);
		
		//Hold all ui in this layer
		//JPanel uiLayerPanel = new JPanel(new BorderLayout());
		JPanel uiLayerPanel = new JPanel();
		uiLayerPanel.setLayout(new BoxLayout(uiLayerPanel, BoxLayout.Y_AXIS));
		
		uiLayerPanel.setOpaque(false);
		uiLayerPanel.setBackground(new Color(0,0,0,0));
		uiLayerPanel.setSize(GUIUtil.GetRelativeSize(0.92f, 0.92f));
		uiLayerPanel.setLocation(GUIUtil.GetRelativePoint(0.04f, 0.04f));
		
		//Upper Section of menu containing: screen label, name entry, portrait selection and class type selection
		JPanel upperSection = new JPanel(new BorderLayout(1, 1));
		//labelBorder.setBackground(Color.GRAY);
		upperSection.setOpaque(false);
		upperSection.setBackground(new Color(0,0,0,0));
		
		JLabel screenTitle = new JFxLabel("Wanted!", SwingConstants.CENTER, GUIUtil.Title, textColor).withStroke(Color.BLACK, 3, false);
		upperSection.add(screenTitle, BorderLayout.NORTH);
		
		//Menu Top Section
		JPanel topSection = new JPanel();
		topSection.setOpaque(false);
		topSection.setBackground(new Color(0,0,0,0));
		topSection.setLayout(new GridLayout(1, 2, 6, 6));
		
		//Details (Name and Class)
		JPanel detailsGrid = new JPanel(new GridLayout(4, 1, 6, 6));
		detailsGrid.setOpaque(false);
		detailsGrid.setBackground(new Color(0,0,0,0));
		
		//Name Box
		JPanel nameLabelFlow = new JPanel(new FlowLayout(0));
		nameLabelFlow.setOpaque(false);
		nameLabelFlow.setBackground(new Color(0,0,0,0));
		JFxLabel nameSectionLabel = new JFxLabel("Name", SwingConstants.LEFT, GUIUtil.SubTitle, textColor).withStroke(Color.BLACK, 2, false);
		nameLabelFlow.add(nameSectionLabel);
		detailsGrid.add(nameLabelFlow);
		
		//General Text field variables
		int fieldIndent = GUIUtil.GetRelativeSize(0.02f, true).width;
		Dimension fieldPaneSize = GUIUtil.GetRelativeSize(0.6f, 0.1f);
		Dimension fieldBGSize = GUIUtil.GetRelativeSize(0.42f, 0.06f);
		Dimension fieldSize = GUIUtil.GetRelativeSize(0.402f, 0.04f);
		Point fieldOffset = GUIUtil.GetRelativePoint(0.01f, 0.012f);
		
		//Name Text Field
		Box nameoffsetBox = Box.createHorizontalBox();
		nameoffsetBox.add(Box.createHorizontalStrut(fieldIndent));
		
		JLayeredPane nameFieldLayeredPane = new JLayeredPane();
		nameFieldLayeredPane.setSize(fieldPaneSize);
		
		JLabel nameFieldBG = new JLabel(SpriteSheetUtility.FieldNinecon());
		nameFieldBG.setSize(fieldBGSize);
		nameFieldLayeredPane.add(nameFieldBG, 1);
		
		nameField = new JTextField(defaultNameString);
		nameField.setBorder(null);
		nameField.setFont(GUIUtil.Body);
		nameField.setSize(fieldSize);
		nameField.setLocation(fieldOffset);
		nameField.addKeyListener(new KeyAdapter() {
		    public void keyTyped(KeyEvent e) { 
		        if(nameField.getText().length() >= 20)
		            e.consume(); 
		    }  
		});
		nameFieldLayeredPane.add(nameField, 0);
		
		nameoffsetBox.add(nameFieldLayeredPane);
		detailsGrid.add(nameoffsetBox);
		
		//Class Box
		JPanel classLabelFlow = new JPanel(new FlowLayout(0));
		classLabelFlow.setOpaque(false);
		classLabelFlow.setBackground(new Color(0,0,0,0));
		JFxLabel classLabel = new JFxLabel("Class", SwingConstants.LEFT, GUIUtil.SubTitle, textColor).withStroke(Color.BLACK, 2, false);
		classLabelFlow.add(classLabel);
		detailsGrid.add(classLabelFlow);
		
		//Class picker ui element
		Box classOffsetBox = Box.createHorizontalBox();
		classOffsetBox.add(Box.createHorizontalStrut(fieldIndent));
		
		JLayeredPane classFieldLayeredPane = new JLayeredPane();
		classFieldLayeredPane.setSize(fieldPaneSize);
		
		JLabel classFieldBG = new JLabel(SpriteSheetUtility.FieldNinecon());
		classFieldBG.setSize(fieldBGSize);
		classFieldLayeredPane.add(classFieldBG, 1);
		
		classPicker = new JComboBox<ClassType>(new ClassType[] { ClassType.RONIN, ClassType.NINJA, ClassType.MONK, ClassType.BANDIT });
		classPicker.addActionListener(new ListenForCombo());
		classPicker.setBorder(null);
		classPicker.setFont(GUIUtil.Body);
		classPicker.setSize(fieldSize);
		classPicker.setLocation(fieldOffset);
		classPicker.setRenderer(new CustomComboBoxListCellRenderer());
		classPicker.setUI( (ComboBoxUI)CustomComboBoxUI.createUI(classPicker) );
		classPicker.setEditor(new CustomComboBoxEditor());
		classPicker.setEditable(true);
		classFieldLayeredPane.add(classPicker, 0);
		
		classOffsetBox.add(classFieldLayeredPane);
		
		detailsGrid.add(classOffsetBox);
		topSection.add(detailsGrid);
		
		//Avatar Image
		JPanel portraitSelectionContainer = new JPanel(new GridLayout(1,3));
		portraitSelectionContainer.setOpaque(false);
		portraitSelectionContainer.setBackground(new Color(0,0,0,0));
		
		//Place button in a grid layout
		JPanel leftGrid = new JPanel(new GridLayout(3, 3));
		leftGrid.setBackground(new Color(0,0,0,0));
		leftGrid.setOpaque(false);
		leftGrid.add(new JLabel()); //0
		leftGrid.add(new JLabel()); //1
		leftGrid.add(new JLabel()); //2
		leftGrid.add(new JLabel()); //3
		leftGrid.add(new JLabel()); //4
		
			//Cycle Portrait Left Button
			//JButton cyclePortraitLeftButton = new JButton("<");
			CustomButton cyclePortraitLeftButton = new CustomButton(SpriteSheetUtility.LeftArrowSymbol(), null, Color.GREEN, SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(), SpriteSheetUtility.PaperBGColor);
			cyclePortraitLeftButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					CyclePortrait(true);
				}
			});
			leftGrid.add(cyclePortraitLeftButton); //5
		
		leftGrid.add(new JLabel()); //6
		leftGrid.add(new JLabel()); //7
		leftGrid.add(new JLabel()); //8
		portraitSelectionContainer.add(leftGrid);
		
		JPanel imageContainer = new JPanel(new FlowLayout());
		imageContainer.setOpaque(false);
		imageContainer.setBackground(new Color(0,0,0,0));
		avatarImage = new ImagePanel(GetRandomPortraitPath());
		avatarImage.setPreferredSize(GUIUtil.GetRelativeSize(0.25f, false));
		avatarImage.ConformPreferredSizeToAspectRatio(false);
		//avatarImage.setMinimumSize(avatarImage.getPreferredSize());
		avatarImage.setMaximumSize(avatarImage.getPreferredSize());
		avatarImage.setBorder(BorderFactory.createLineBorder(Color.black, 3));
		imageContainer.add(avatarImage, SwingConstants.CENTER);
		portraitSelectionContainer.add(imageContainer);
		
		//Place button in a grid layout
		JPanel rightGrid = new JPanel(new GridLayout(3, 3));
		rightGrid.setBackground(new Color(0,0,0,0));
		rightGrid.setOpaque(false);
		rightGrid.add(new JLabel()); //0
		rightGrid.add(new JLabel()); //1
		rightGrid.add(new JLabel()); //2
		
		//Cycle Portrait Left Button
		//JButton cyclePortraitRightButton = new JButton(">");
		CustomButton cyclePortraitRightButton = new CustomButton(SpriteSheetUtility.RightArrowSymbol(), null, Color.GREEN, SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(), SpriteSheetUtility.PaperBGColor);
		cyclePortraitRightButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CyclePortrait(false);
			}
		});
		rightGrid.add(cyclePortraitRightButton); //3
				
		rightGrid.add(new JLabel()); //4
		rightGrid.add(new JLabel()); //5
		rightGrid.add(new JLabel()); //6
		rightGrid.add(new JLabel()); //7
		rightGrid.add(new JLabel()); //8
		portraitSelectionContainer.add(rightGrid);
		
		topSection.add(portraitSelectionContainer);
		//Add north border region to this panel
		//this.add(topSection, BorderLayout.NORTH);
		upperSection.add(topSection, BorderLayout.CENTER);
		//this.add(upperSection, BorderLayout.NORTH);
		//uiLayerPanel.add(upperSection, BorderLayout.NORTH);
		Dimension upperDimension = GUIUtil.GetRelativeSize(1f, 0.35f);
		//System.out.println("CreationPanel.Initialize() - upperDimension: " + upperDimension.toString());
		upperSection.setMinimumSize(upperDimension);
		upperSection.setPreferredSize(upperDimension);
		upperSection.setMaximumSize(upperDimension);
		uiLayerPanel.add(upperSection);
		
		//North Border Panel - End
		
		//Center Border Panel - Start
		
		//Stat Panel
		JPanel centerPanel = new JPanel();
		centerPanel.setOpaque(false);
		centerPanel.setBackground(new Color(0,0,0,0));
		centerPanel.setLayout(new BorderLayout(6, 6));
		//centerPanel.setBorder(BorderFactory.createEtchedBorder(Color.RED, Color.ORANGE));
		
		//Stat Header Panel
		JPanel statLabelFlow = new JPanel(new FlowLayout(0));
		statLabelFlow.setOpaque(false);
		statLabelFlow.setBackground(new Color(0,0,0,0));
		
		//Header Label
		JPanel statsLabelFlow = new JPanel(new FlowLayout(0));
		statsLabelFlow.setOpaque(false);
		statsLabelFlow.setBackground(new Color(0,0,0,0));
		JFxLabel statsSectionLabel = new JFxLabel("Stats", SwingConstants.LEFT, GUIUtil.SubTitle, textColor).withStroke(Color.BLACK, 2, false);
		statsLabelFlow.add(statsSectionLabel);
		statLabelFlow.add(statsLabelFlow);
		
		//Add our header to the Stat Panel
		centerPanel.add(statLabelFlow, BorderLayout.NORTH);
		
		//Stat Grid
		JPanel statGrid = new JPanel();
		statGrid.setOpaque(false);
		statGrid.setBackground(new Color(0,0,0,0));
		statGrid.setLayout(new GridLayout(0, 6, 2, 2));
		ListenForSpinner listener = new ListenForSpinner();
		
		//(Grid Row 0)
		//Available points Row
		JLabel pointsLabel = new JLabel("Available Points: ", SwingConstants.RIGHT);
		pointsLabel.setFont(GUIUtil.Body);
		statGrid.add(pointsLabel);
		
		//Point Value
		Dimension pointSize = GUIUtil.GetRelativeSize(0.152f, 0.067f);
		JLayeredPane pointsLayeredPane = new JLayeredPane();
		pointsLayeredPane.setSize(pointSize);
		
		JLabel pointBG = new JLabel(SpriteSheetUtility.ValueBGNinecon());
		pointBG.setSize(pointSize);
		pointsLayeredPane.add(pointBG, 1);
		
		String s = "" + distributionPoints;
		pointValueLabel = new JLabel(s, SwingConstants.CENTER);
		pointValueLabel.setFont(GUIUtil.Title);
		pointValueLabel.setSize(pointSize);
		pointsLayeredPane.add(pointValueLabel, 0);
		
		statGrid.add(pointsLayeredPane);
		
		statGrid.add(new JLabel(""));
		statGrid.add(new JLabel(""));
		statGrid.add(new JLabel(""));
		statGrid.add(new JLabel(""));
		
		//(Grid Row 1)
		int columnLabelAlignment = SwingConstants.CENTER;
		
		//Column Labels
		statGrid.add(new JLabel(""));
		JLabel attrLabel = new JLabel("Attribution", columnLabelAlignment);
		attrLabel.setFont(GUIUtil.ItalicHeader);
		attrLabel.setForeground(subtleColor);
		statGrid.add(attrLabel);
		JLabel classBonusLabel = new JLabel("Class Bonus", columnLabelAlignment);
		classBonusLabel.setFont(GUIUtil.ItalicHeader);
		classBonusLabel.setForeground(subtleColor);
		statGrid.add(classBonusLabel);
		JLabel extBonusLabel = new JLabel("External Bonus", columnLabelAlignment);
		extBonusLabel.setFont(GUIUtil.ItalicHeader);
		extBonusLabel.setForeground(subtleColor);
		statGrid.add(extBonusLabel);
		//JLabel resultLabel = new JLabel("Result", columnLabelAlignment);
		//resultLabel.setFont(GUIUtil.ItalicHeader);
		//resultLabel.setForeground(subtleColor);
		//statGrid.add(resultLabel);
		//This Result label looks a bit redundant
		statGrid.add(new JLabel(""));
		JLabel specLabel = new JLabel("Special Ability", columnLabelAlignment);
		specLabel.setFont(GUIUtil.ItalicHeader);
		specLabel.setForeground(subtleColor);
		statGrid.add(specLabel);
		
		//(Grid Row 2)
		int statLabelAlignment = SwingConstants.RIGHT;
		int valueAlignment = SwingConstants.CENTER;
		
		//Stat Label
		JLabel strLabel = new JLabel("Strength: ", statLabelAlignment);
		strLabel.setFont(GUIUtil.Header);
		statGrid.add(strLabel);
		//Spinner
		spinner_str = new CustomSpinner(new SpinnerNumberModel(1, 1, 4, 1), GUIUtil.SubTitle);
		spinner_str.addChangeListener(listener);
		statGrid.add(spinner_str.getWrapper());
		//Class Bonus
		JLabel classBonusLabel_str = new JLabel("+ 0", valueAlignment);
		classBonusLabel_str.setFont(GUIUtil.SubTitle);
		statGrid.add(classBonusLabel_str);
		//External Bonus
		JLabel extBonusLabel_str = new JLabel("+ 0", valueAlignment);
		extBonusLabel_str.setFont(GUIUtil.SubTitle);
		statGrid.add(extBonusLabel_str);
		//Result
		JLabel resultStatLabel_str = new JLabel("#", valueAlignment);
		resultStatLabel_str.setFont(GUIUtil.SubTitle);
		statGrid.add(resultStatLabel_str);
		//Ability
		JLabel abilityLabel_str = new JLabel("", valueAlignment);
		abilityLabel_str.setFont(GUIUtil.ItalicHeader);
		abilityLabel_str.setForeground(abilityWordColor);
		statGrid.add(abilityLabel_str);
		
		//statMap.put(StatType.STRENGTH, new StatData(StatType.STRENGTH, ClassType.RONIN, abilityLabel_str, spinner_str, classBonusLabel_str, extBonusLabel_str, resultStatLabel_str, "Berserk"));
		statMap.put(StatType.STRENGTH, new StatData(StatType.STRENGTH, ClassType.RONIN, abilityLabel_str, (JSpinner)spinner_str, classBonusLabel_str, extBonusLabel_str, resultStatLabel_str, "Berserk"));
		
		//(Grid Row 3)
		//Stat Label
		JLabel intLabel = new JLabel("Intellect: ", statLabelAlignment);
		intLabel.setFont(GUIUtil.Header);
		statGrid.add(intLabel);
		//Spinner
		spinner_int = new CustomSpinner(new SpinnerNumberModel(1, 1, 4, 1), GUIUtil.SubTitle);
		spinner_int.addChangeListener(listener);
		statGrid.add(spinner_int.getWrapper());
		//Class Bonus
		JLabel classBonusLabel_int = new JLabel("+ 0", valueAlignment);
		classBonusLabel_int.setFont(GUIUtil.SubTitle);
		statGrid.add(classBonusLabel_int);
		//External Bonus
		JLabel extBonusLabel_int = new JLabel("+ 0", valueAlignment);
		extBonusLabel_int.setFont(GUIUtil.SubTitle);
		statGrid.add(extBonusLabel_int);
		//Result
		JLabel resultStatLabel_int = new JLabel("#", valueAlignment);
		resultStatLabel_int.setFont(GUIUtil.SubTitle);
		statGrid.add(resultStatLabel_int);
		//Ability
		JLabel abilityLabel_int = new JLabel("", valueAlignment);
		abilityLabel_int.setFont(GUIUtil.ItalicHeader);
		abilityLabel_int.setForeground(abilityWordColor);
		statGrid.add(abilityLabel_int);
		
		statMap.put(StatType.INTELLECT, new StatData(StatType.INTELLECT, ClassType.NINJA, abilityLabel_int, spinner_int, classBonusLabel_int, extBonusLabel_int, resultStatLabel_int, "Hypnosis"));
		
		//(Grid Row 4)
		//Stat Label
		JLabel endLabel = new JLabel("Endurance: ", statLabelAlignment);
		endLabel.setFont(GUIUtil.Header);
		statGrid.add(endLabel);
		//Spinner
		spinner_end = new CustomSpinner(new SpinnerNumberModel(1, 1, 4, 1), GUIUtil.SubTitle);
		spinner_end.addChangeListener(listener);
		statGrid.add(spinner_end.getWrapper());
		//Class Bonus
		JLabel classBonusLabel_end = new JLabel("+ 0", valueAlignment);
		classBonusLabel_end.setFont(GUIUtil.SubTitle);
		statGrid.add(classBonusLabel_end);
		//External Bonus
		JLabel extBonusLabel_end = new JLabel("+ 0", valueAlignment);
		extBonusLabel_end.setFont(GUIUtil.SubTitle);
		statGrid.add(extBonusLabel_end);
		//Result
		JLabel resultStatLabel_end = new JLabel("#", valueAlignment);
		resultStatLabel_end.setFont(GUIUtil.SubTitle);
		statGrid.add(resultStatLabel_end);
		//Ability
		JLabel abilityLabel_end = new JLabel("", valueAlignment);
		abilityLabel_end.setFont(GUIUtil.ItalicHeader);
		abilityLabel_end.setForeground(abilityWordColor);
		statGrid.add(abilityLabel_end);
		
		statMap.put(StatType.ENDURANCE, new StatData(StatType.ENDURANCE, ClassType.MONK, abilityLabel_end, spinner_end, classBonusLabel_end, extBonusLabel_end, resultStatLabel_end, "Temple Bell"));
		
		//(Grid Row 5)
		//Stat Label
		JLabel spdLabel = new JLabel("Speed: ", statLabelAlignment);
		spdLabel.setFont(GUIUtil.Header);
		statGrid.add(spdLabel);
		//Spinner
		spinner_spd = new CustomSpinner(new SpinnerNumberModel(1, 1, 4, 1), GUIUtil.SubTitle);
		spinner_spd.addChangeListener(listener);
		statGrid.add(spinner_spd.getWrapper());
		//Class Bonus
		JLabel classBonusLabel_spd = new JLabel("+ 0", valueAlignment);
		classBonusLabel_spd.setFont(GUIUtil.SubTitle);
		statGrid.add(classBonusLabel_spd);
		//External Bonus
		JLabel extBonusLabel_spd = new JLabel("+ 0", valueAlignment);
		extBonusLabel_spd.setFont(GUIUtil.SubTitle);
		statGrid.add(extBonusLabel_spd);
		//Result
		JLabel resultStatLabel_spd = new JLabel("#", valueAlignment);
		resultStatLabel_spd.setFont(GUIUtil.SubTitle);
		statGrid.add(resultStatLabel_spd);
		//Ability
		JLabel abilityLabel_spd = new JLabel("", valueAlignment);
		abilityLabel_spd.setFont(GUIUtil.ItalicHeader);
		abilityLabel_spd.setForeground(abilityWordColor);
		statGrid.add(abilityLabel_spd);
		
		statMap.put(StatType.SPEED, new StatData(StatType.SPEED, ClassType.BANDIT, abilityLabel_spd, spinner_spd, classBonusLabel_spd, extBonusLabel_spd, resultStatLabel_spd, "Cloak and Dagger"));
		
		//Prepare StatMap now that we have all our stat components created
		SetupStatDatas();
		
		//Add Grid to center panel
		centerPanel.add(statGrid, BorderLayout.CENTER);
		//Add Center Panel to this panel
		//this.add(centerPanel);
		//uiLayerPanel.add(centerPanel, BorderLayout.CENTER);
		Dimension centerDimension = GUIUtil.GetRelativeSize(1f, 0.5f);
		//System.out.println("CreationPanel.Initialize() - centerDimension: " + centerDimension.toString());
		centerPanel.setMinimumSize(centerDimension);
		centerPanel.setPreferredSize(centerDimension);
		centerPanel.setMaximumSize(centerDimension);
		uiLayerPanel.add(centerPanel);
		
		//Center Border Panel - End
		
		//Bottom Border Panel - Start
		
		//Make panel to hold a message and a button
		JPanel bottomBox = new JPanel();
		bottomBox.setOpaque(false);
		bottomBox.setBackground(new Color(0,0,0,0));
		bottomBox.setLayout(new BoxLayout(bottomBox, BoxLayout.X_AXIS));
		//bottomBox.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
		
		bottomBox.setMinimumSize(GUIUtil.GetRelativeSize(1f, 0.07f));
		bottomBox.setPreferredSize(GUIUtil.GetRelativeSize(1f, 0.07f));
		bottomBox.setMaximumSize(GUIUtil.GetRelativeSize(1f, 0.07f));
		
		//Encapsulate the label in a border panel so it takes up maximum space
		JPanel labelContainer = new JPanel(new BorderLayout());
		labelContainer.setOpaque(false);
		labelContainer.setBackground(new Color(0,0,0,0));
		//labelContainer.setBorder(BorderFactory.createLineBorder(Color.RED,  1));
		
		//Add the message to the bottom panel
		validationMessage = new JLabel("Press the confirm button when you've completed your character sheet.", SwingConstants.LEFT);
		validationMessage.setFont(GUIUtil.Body);
		validationMessage.setForeground(Color.GRAY);
		validationMessage.setHorizontalAlignment(SwingConstants.RIGHT);
		labelContainer.add(validationMessage, BorderLayout.CENTER);
		bottomBox.add(labelContainer);
		
		//Put space between text and button
		bottomBox.add( Box.createHorizontalStrut(GUIUtil.GetRelativeSize(0.03f, true).width) );
		
		//Make the nested box for our button and Glue to space the bottom to the right hand side
		Box buttonBox = Box.createHorizontalBox();
		CustomButton doneButton = new CustomButton(SpriteSheetUtility.CheckmarkSymbol(),
													null, Color.GREEN,
													SpriteSheetUtility.buttonBG_BevelBoxUp(), SpriteSheetUtility.buttonBG_BevelBoxDown(),
													SpriteSheetUtility.PaperBGColor);
		Dimension doneButtonSize = GUIUtil.GetRelativeSize(0.07f, false);
		doneButton.setMinimumSize(doneButtonSize);
		doneButton.setPreferredSize(doneButtonSize);
		doneButton.setMaximumSize(doneButtonSize);
		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.add(doneButton);
		//Add the button box to the bottom panel
		bottomBox.add(buttonBox);
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ValidateCharacterSheet();
			}
		});
		//Add our Bottom Panel to this panel
		//this.add(bottomBox, BorderLayout.SOUTH);
		//uiLayerPanel.add(bottomBox, BorderLayout.SOUTH);
		bottomBox.setSize(GUIUtil.GetRelativeSize(0.1f, 1f));
		uiLayerPanel.add(bottomBox);
		
		//Bottom Border Panel - End
		
		layeredPane.add(uiLayerPanel, 0);
		this.add(layeredPane, BorderLayout.CENTER);
	}
	
	private final String portraitOptionsPathPrefix = "portraits/playerOptions/Option_";
	private final int portraitOptionCount = 6;
	private int portraitIndex = 1; //Use natural list numbering scheme, i.e. 1-n
	
	private String GetRandomPortraitPath() {
		//randomize the portrait that displays first
		portraitIndex = new Random().nextInt(portraitOptionCount) + 1;
		
		return GetPortraitPath();
	}
	
	private String GetPortraitPath() {
		return portraitOptionsPathPrefix + portraitIndex + ".png";
	}
	
	private void CyclePortrait(boolean cycleLeft) {
		if(cycleLeft) {
			if(portraitIndex == 1)
				portraitIndex = portraitOptionCount;
			else
				portraitIndex--;
		} else {
			if(portraitIndex == portraitOptionCount)
				portraitIndex = 1;
			else
				portraitIndex++;
		}
		
		avatarImage.SetNewImage(GetPortraitPath());
	}
	
	//Helper class that allows each stat to manage itself
	public class StatData {
		public StatData(StatType statType, ClassType governingClass, JLabel abilityLabel, JSpinner spinner, JLabel classBonusLabel, JLabel externalLabel, JLabel resultLabel, String abilityText) {
			this.statType = statType;
			this.governingClass = governingClass;
			this.abilityLabel = abilityLabel;
			this.spinner = spinner;
			this.classBonusLabel = classBonusLabel;
			this.externalLabel = externalLabel;
			this.resultLabel = resultLabel;
			this.abilityText = abilityText;
			
			attribution = STAT_BASE;
			classBonus = 0;
			externalBonus = 0;

			UpdateLabels();
		}
		private StatType statType;
		public StatType getStatType() { return statType; }
		private ClassType governingClass;
		public ClassType GoverningClass() { return governingClass; }
		//GUI
		private JLabel abilityLabel;
		private JSpinner spinner;
		private JLabel classBonusLabel;
		private JLabel externalLabel;
		private JLabel resultLabel;
		//Members
		private String abilityText = "(ABILITY EXAMPLE)";
		private String currentAbilityText;
		private int attribution;
		private int classBonus;
		private int externalBonus;
		
		//Check new spinner value and either allow it to be changed or change it back to what it was
		public void OnSpinnerChange() {
			int spinnerValue = (int)spinner.getValue();
			
			if(distributionPoints == 0 && attribution < spinnerValue) {
				isChangingStatsInternally = true;
				spinner.setValue(attribution);
			} else {
				if(spinnerValue > STAT_MAX) {
					isChangingStatsInternally = true;
					spinner.setValue(STAT_MAX);
				} else if(spinnerValue < STAT_MIN) {
					isChangingStatsInternally = true;
					spinner.setValue(STAT_MIN);
				} else {
					UpdatePoints(spinnerValue - attribution);
					attribution = spinnerValue;
					
					int resultingValue = Result();
					ClassType currentClassType = (ClassType)classPicker.getSelectedItem();
					if(currentClassType == governingClass && resultingValue >= STAT_MAX)
						currentAbilityText = abilityText;
					else
						currentAbilityText = " ";
				}
			}
			UpdateLabels();
		}
		
		//Update this stat for our new governing class
		public void ApplyClassBonus() {
			classBonus = STAT_BONUS;
			if(Result() >= STAT_MAX)
				currentAbilityText = abilityText;
			UpdateLabels();
		}
		
		//Get the final stat result after all points and bonuses
		public int Result() {
			return attribution + classBonus + externalBonus;
		}
		
		//Same as the result but as a string
		private String GetResultString() {
			return "= " + Result();
		}
		
		//Update all GUI components
		private void UpdateLabels() {
			abilityLabel.setText(currentAbilityText);
			classBonusLabel.setText("+ " + classBonus);
			externalLabel.setText("+ " + externalBonus);
			resultLabel.setText(GetResultString());
		}
		
		//Clear all the bonuses from components
		public void ResetBonuses() {
			currentAbilityText = " ";
			classBonus = 0;
			externalBonus = 0;
			
			UpdateLabels();
		}
	}
	
	//Prepares our StatData classes
	private void SetupStatDatas() {
		ClassType startingClass = (ClassType)classPicker.getSelectedItem();
		for(StatType stat : StatType.values()) {
			if(statMap.get(stat).GoverningClass() == startingClass) {
				statMap.get(stat).ApplyClassBonus();
				break;
			}
		}
	}
	
	//Handle the events of any Spinner changes
	private class ListenForSpinner implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			//Ignore this event if it was triggered by us setting spinner values manually
			if(isChangingStatsInternally) {
				isChangingStatsInternally = false;
				return;
			}
			
			//Figure out switch Spinner triggered this event
			StatType statType = StatType.values()[0];
			if(e.getSource() == spinner_str) {
				statType = StatType.STRENGTH;
			} else if(e.getSource() == spinner_int) {
				statType = StatType.INTELLECT;
			} else if(e.getSource() == spinner_end) {
				statType = StatType.ENDURANCE;
			} else if(e.getSource() == spinner_spd) {
				statType = StatType.SPEED;
			} else {
				System.err.println("Add support for a new stat point spinner");
				return;
			}
			//Handle spinner value change
			statMap.get(statType).OnSpinnerChange();
		}
	}
	
	//Handle the events of any Class changes  
	private class ListenForCombo implements ActionListener {
	    public void actionPerformed(ActionEvent e) {
	    	//Check that our classPicker is the component calling this event
			if(e.getSource() == classPicker) {
				ClassType newClassType = (ClassType)classPicker.getSelectedItem();
				
				StatData associatedStatData = null;
				//Reset all
				for(StatType stat : StatType.values()) {
					statMap.get(stat).ResetBonuses();
					if(statMap.get(stat).GoverningClass() == newClassType)
						associatedStatData = statMap.get(stat);
				}
				//Notify newly bolstered stat
				associatedStatData.ApplyClassBonus();
			} else {
				System.err.println("Add support for new combo box!");
			}
		}
	}
	
	//Update AvailablePoints text
	private void UpdatePoints(int diff) {
		//System.out.println("Distribution Points Change, diff: " + diff);
		if(diff > 0 && distributionPoints > 0)
			distributionPoints--;
		else if(diff < 0)
			distributionPoints++;
		
		String pointString = "" + distributionPoints;
		pointValueLabel.setText(pointString);
	}
	
	//Ensure the user has entered a name and alloted all available stat points before leaving the character sheet
	private void ValidateCharacterSheet() {
		//System.out.println("ValidateCharacterSheet - Stub");
		//Decide whether the sheet is complete and either move to the next page or set message to lead user to form completion
		
		//Check name
		String text = nameField.getText();
		text = text.replaceAll("\\s", "");
		//System.out.println("ValidateCharacterSheet - Name: " + text);
		if(text.compareTo(defaultNameString) == 0 || text.compareTo("") == 0) {
			//System.out.println("Show name message");
			validationMessage.setText("Name your character.");
			validationMessage.setForeground(Color.RED);
			return;
		}
		
		//Check if all points are used
		if(distributionPoints > 0) {
			validationMessage.setText("Assign all the available points to your character.");
			validationMessage.setForeground(Color.RED);
			return;
		}
		
		manager.SubmitPlayerDataAndResetSaveFile(new CharacterData(
			nameField.getText(),
			(ClassType)classPicker.getSelectedItem(),
			statMap.get(StatType.STRENGTH).Result(),
			statMap.get(StatType.INTELLECT).Result(),
			statMap.get(StatType.ENDURANCE).Result(),
			statMap.get(StatType.SPEED).Result(),
			0,
			GetPortraitPath()
		));
	}
}
