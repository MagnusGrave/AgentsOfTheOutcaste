package data;

import java.io.Serializable;
import gameLogic.Interaction;

//[MISSION_FLOW_EDIT]
/**
 * Records the unresolved state of the current interaction so that upon exiting and loading the previous state of affairs can be restored correctly.
 *   -Tracks two of the three interaction states:
 *   	State 1: Chosen, pre test ... State inferred by unresolvedInteractionData
 *   	State 2: Mid Test (Mid Battle) ... State inferred by unresolvedInteractionData
 * @author Magnus
 *
 */
public class UnresolvedInteractionData implements Serializable {
	private static final long serialVersionUID = 3288292603095248138L;
	
	/*
	 * Used to set non battle states
	 */
	public UnresolvedInteractionData(Interaction interaction, boolean isPreTestNotMidTest) {
		this.interaction = interaction;
		this.isPreTestNotMidTest = isPreTestNotMidTest;
	}
	
	/*
	 * Used to overwrite a current state with current battle info
	 */
	public UnresolvedInteractionData(UnresolvedInteractionData other, BattleState currentBattleState) {
		this.interaction = other.interaction;
		this.isPreTestNotMidTest = other.isPreTestNotMidTest;
		this.currentBattleState = currentBattleState;
	}
	
	private Interaction interaction;
	public Interaction getInteraction() { return interaction; }
	private boolean isPreTestNotMidTest;
	public boolean IsPreTestNotMidTest() { return isPreTestNotMidTest; }
	//This data will be grabbed from BattlePanel upon serialization of the parent InteractionManager class, if we're Mid-Battle
	private BattleState currentBattleState;
	public BattleState getCurrentBattleState() { return currentBattleState; }
}
