package data;

import java.io.Serializable;

import enums.InteractionType;

public class GraphPathNode implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3448105765408991984L;
	
	public GraphPathNode(InteractionType choiceType, boolean wasSuccessfulOutcome) {
		this.choiceType = choiceType;
		this.wasSuccessfulOutcome = wasSuccessfulOutcome;
	}
	public InteractionType choiceType;
	public boolean wasSuccessfulOutcome;
}
