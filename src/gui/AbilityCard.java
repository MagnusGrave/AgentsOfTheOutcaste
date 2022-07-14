package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import data.CombatEffect;
import enums.StatusType;
import gameLogic.AbilityManager.Ability;

@SuppressWarnings("serial")
public class AbilityCard extends JLayeredPane {
	JLabel nameLabel;
	JLabel descLabel;
	JLabel effectsBlobLabel;
	JLabel rangeNotationLabel;
	
	Ability currentAbility;
	
	private final Color positiveColor = Color.GREEN;
	private final Color negativeColor = Color.RED;
	
	
	
	public AbilityCard(Point panePos, Dimension paneSize) {
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
		
			nameLabel = new JFxLabel("Name", SwingConstants.LEFT, GUIUtil.Header, Color.BLACK).withShadow(Color.LIGHT_GRAY, new Point(2,2));
			nameLabel.setVerticalAlignment(SwingConstants.CENTER);
			nameLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
			textGridPanel.add(nameLabel);
			
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
	
	public void DisplayAbility(Ability ability) {
		this.currentAbility = ability;

		nameLabel.setText(currentAbility.name);
		descLabel.setText("<html>"+ (currentAbility.isActiveAbility ? "(Active) " : "(Passive) ") + currentAbility.description +"</html>");
		effectsBlobLabel.setText("<html>"+ GetCombatEffectsBlob( currentAbility.combatEffects ) +"</html>");
		if(currentAbility.isActiveAbility)
			rangeNotationLabel.setText("<html>"+  GetRangeNotationBlob(currentAbility)  +"</html>");
		else
			rangeNotationLabel.setText("");
	}
	
	/**
	 * Use this for any UI components that need it. It's general purpose use of combatEffects will support any Ability, ItemData or new data structure.
	 * @param combatEffects
	 * @return
	 */
	public static String GetCombatEffectsBlob(List<CombatEffect> combatEffects) {
		String blob = "";

		List<String> statusNames = new ArrayList<String>();
		for(CombatEffect combatEffect : combatEffects) {
			switch(combatEffect.battleItemTypeEffect) {
				case Accelerant:
					statusNames.add("Accelerated");
					break;
				case Damage:
					blob += "Applies Damage. ";
					break;
				case Potion:
					blob += "Applies Healing. ";
					break;
				case Status:
					statusNames.add(combatEffect.statusEffect.toString());
					break;
				case Cure:
					String cureArray = "Applies Cures: ";
					for(int i = 0; i < combatEffect.cures.length; i++)
						cureArray += (i == 0 ? "" : ", ") + combatEffect.cures[i];
					cureArray += ". ";
					blob += cureArray;
					break;
				case Buff:
					String buffArray = "Applies Buffs: ";
					for(int i = 0; i < combatEffect.attributeMods_buffs.length; i++)
						buffArray += (i == 0 ? "" : ", ") + combatEffect.attributeMods_buffs[i].attributeModType;
					buffArray += ". ";
					blob += buffArray;
					break;
				case Debuff:
					String debuffArray = "Applies Debuffs: ";
					for(int i = 0; i < combatEffect.attributeMods_debuffs.length; i++)
						debuffArray += (i == 0 ? "" : ", ") + combatEffect.attributeMods_debuffs[i].attributeModType;
					debuffArray += ". ";
					blob += debuffArray;
					break;
				case SpiritTool:
					blob += "Interacts with Spirits. ";
					break;
				case Revive:
					blob += "Revives the fallen. ";
					break;
				default:
					System.err.println("AbilityCard.GetCombatEffectsBlob() - Add support for: " + combatEffect.battleItemTypeEffect);
					break;
			}
		}
		
		if(statusNames.size() > 0) {
			String statusArray = "Applies Statuses: ";
			for(int i = 0; i < statusNames.size(); i++) {
				if(i == 0)
					statusArray += statusNames.get(0);
				else
					statusArray += ", " + statusNames.get(i);
			}
			statusArray += ". ";
			
			blob += statusArray;
		}
		
		return blob;
	}
	
	public String GetRangeNotationBlob(Ability ability) {
		return "Range: " + ability.range_min + "-" + ability.range_max + ", AOE: " + ability.hitRadius +
				"<br>Targeting: " + ability.hitSelection;
	}
}
