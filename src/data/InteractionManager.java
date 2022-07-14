package data;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import enums.InteractionType;
import enums.MenuType;
import gameLogic.Game;
import gameLogic.Interaction;
import gameLogic.Mission;
import gameLogic.Missions;
import gui.GUIManager;

/**
 * This class stores and handles interactions for both MapLocation and Mission. Its a layer of abstraction to assure that the interaction system stays consistent. 
 * @author Magnus
 *
 */
public class InteractionManager implements Serializable {
	private static final long serialVersionUID = -2492973276593087984L;

	public InteractionManager(Interaction[] interactions) {
		this.interactions = interactions;
		if(interactions == null)
			interList = new ArrayList<Interaction>();
		else
			interList = new ArrayList<Interaction>(Arrays.asList(interactions));
		
		BuildInteractionHandlers();
		
		graphPath = new ArrayList<GraphPathNode>();
	}
	
	/**
	 * Handle any setup necessary non-serialable properties during deserialization-based instantiation. Since InteractionManagers will only ever be initialized once at the
	 * start of a NewGame(and subsequently deserialized on start during all other occasions) we need to instantiate non-serializable elements here before they get used.
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		 
		//System.out.println("InteractionManager.readObject() - (Equivalent to OnDeserialize)");
		
		if(graphPath != null) {
			for(GraphPathNode node : graphPath)
				System.out.println("InteractionManager.readObject() - graphPath choice: " + node.choiceType);
		}
		
		if(interactionHandlers == null)
			BuildInteractionHandlers();
		
		if(unresolvedInteractionData != null)
			System.out.println("There is an existing unresolvedInteractionData");
	}
	
	/*
	 * Gets called during SaveData serialization
	 */
	private void writeObject(java.io.ObjectOutputStream oos) throws Exception {
		if(
		   GUIManager.getCurrentMenuType() != MenuType.MAINMENU //Dont record this if we haven't started or loaded into a game yet
		   &&
		   unresolvedInteractionData != null && !unresolvedInteractionData.IsPreTestNotMidTest()
		)
			unresolvedInteractionData = new UnresolvedInteractionData(unresolvedInteractionData, Game.Instance().GetBattlePanel().CollectBattleState());
		
		oos.defaultWriteObject();
	}
	
	//Data Store Members - Start
	
	private Interaction[] interactions;
	public Interaction[] getInteractions() { return interactions; }
	private List<Interaction> interList;
	public Interaction GetInteraction(InteractionType type) {
		Interaction matchingInteraction = interList.stream()
		  .filter(x -> x.Type() == type)
		  .findAny()
		  .orElse(null);
		if(matchingInteraction == null) {
			System.err.println("InteractionManager.GetInteraction() - Couldn't find interaction type: " + type.toString());
			return null;
		} else {
			return matchingInteraction;
		}
	}
	
	//Data Store Members - End
	
	//Runtime, Setup and Helper Stuff - Start
	
	//Once built, remembers the interaction states
	//private InteractionState[][] interactionLayeredStates;
	//public InteractionState[][] getInteractionLayeredStates() { return interactionLayeredStates; }
	//public void setInteractionLayeredStates(InteractionState[][] interactionLayeredStates) {
	//	this.interactionLayeredStates = interactionLayeredStates;
	//}
	//I think interactionLayeredStates logic is a remnant of outdated Interaction System data structuring
	
	public class InteractionHandler {
		public InteractionHandler(Interaction intr, InteractionState state, int layerDepth, InteractionType type, int elementIndex) {
			this.intr = intr;
			this.state = state;
			this.layerDepth = layerDepth;
			this.type = type;
			this.elementIndex = elementIndex;
		}
		private Interaction intr;
		public Interaction intr() { return intr; }
		private InteractionState state;
		public InteractionState state() { return state; }
		//layer-based address
		private int layerDepth; //serves both addresses
		public int layerDepth() { return layerDepth; }
		private InteractionType type;
		public InteractionType type() {  return type; }
		//jagged array address
		private int elementIndex;
		public int elementIndex() { return elementIndex; }
	}
	//private List<InteractionHandler> interactionHandlers = new ArrayList<InteractionHandler>();
	//Prevent this field from being serialized by using the transient keyword
	private transient List<InteractionHandler> interactionHandlers;
	public List<InteractionHandler> getInteractionHandlers() { return interactionHandlers; }
	
	/**
	 * Describes the sequence of interactions the user chose and the outcome of those interactions(whether or not they succeeded, if there was a test)
	 */
	private List<GraphPathNode> graphPath;
	public List<GraphPathNode> getGraphPath() { return graphPath; }
	
	
	//[MISSION_FLOW_EDIT]
	//There are three states an interaction can be in:
			//	State 1: Chosen, pre test ... State inferred by unresolvedInteractionData
			//	State 2: Mid Test (Mid Battle) ... State inferred by unresolvedInteractionData
			//	State 3: Post test but not yet resolved ... State inferred by the graphPath record and the active mission's incomplete status
	//Track the intermitant staes of current, unresolved interactions
	private UnresolvedInteractionData unresolvedInteractionData;
	public UnresolvedInteractionData getUnresolvedInteractionData() { return unresolvedInteractionData; }
	/*
	 * MapLocationPanel will set this every step of the way: At Intr choice, at battle start and at postTest start
	 */
	public void RecordUnresolvedInteractionState(Interaction interaction, boolean isPreTestNotMidTest) {
		System.out.println("InteractionManager.RecordUnresolvedInteractionState() - isPreTestNotMidTest: " + isPreTestNotMidTest);
		unresolvedInteractionData = new UnresolvedInteractionData(interaction, isPreTestNotMidTest);
	}
	/*
	 * Clear the unresolvedInteractionData when an interaction is completed applied/recorded
	 */
	public void ClearUnresolvedInteractionState() {
		System.out.println("InteractionManager.ClearInteractionState()");
		unresolvedInteractionData = null;
	}
	
	
	//Track the users choices
	public void RecordGraphPathNode(GraphPathNode graphPathNode) {
		graphPath.add(graphPathNode);
	}
	
	
	private void BuildInteractionHandlers() {
		//System.out.println("InteractionManager.BuildInteractionHandlers()");
		
		//Clear the handlers from the last MapLocation, if this is being called during travel gameplay
		if(interactionHandlers == null)
			interactionHandlers = new ArrayList<InteractionHandler>();
		else
			interactionHandlers.clear();
		
		List<List<Interaction>> interactionGraph = new ArrayList<List<Interaction>>();
		int depth = 0;
		List<Interaction> layerList = new ArrayList<Interaction>();
		
		//Collections.addAll(layerList, currentLocation.getInteractions());
		if(interactions != null)
			Collections.addAll(layerList, interactions);
		
		//InteractionState[][] layeredStates = interactionLayeredStates;
		while(true) {
			//System.out.println("InteractionManager.BuildInteractionHandlers() - Building Layer: " + depth);
			
			//TODO record each Tree intr and each Persistent intr(at its origin layer)
			//I think the list is being wiped from the interactionGraph when we .clear the layerList
			//interactionGraph.add(layerList);
			interactionGraph.add(new ArrayList<Interaction>(layerList));
			
			//get next layer list and overwrite layerList with it
			List<Interaction> nextLayerList = new ArrayList<Interaction>();
			int elementIndex = 0;
			for(Interaction intr : layerList) {
				//InteractionState intrState = layeredStates == null ? new InteractionState(depth, intr.Type()) : layeredStates[depth][elementIndex];
				InteractionState intrState = new InteractionState(depth, intr.Type());
				
				interactionHandlers.add(new InteractionHandler(intr, intrState, depth, intr.Type(), elementIndex));
				
				Interaction[] successIntrs = intr.NextInteractions_OnSuccess();
				if(successIntrs != null && successIntrs.length > 0)
					nextLayerList.addAll( (List<Interaction>)Arrays.asList(successIntrs) );
				Interaction[] failureIntrs = intr.NextInteractions_OnFailure();
				if(failureIntrs != null && failureIntrs.length > 0)
					nextLayerList.addAll( (List<Interaction>)Arrays.asList(failureIntrs) );
				
				System.out.println("InteractionManager.BuildInteractionHandlers() - Rebuilding Interaction element: " + elementIndex);
				
				elementIndex++;
			}
			layerList.clear();
			//System.out.println("InteractionManager.BuildInteractionHandlers() - Is the graph's layerList still intact? Size:" + interactionGraph.get(interactionGraph.size()-1).size());
			
			if(nextLayerList.size() == 0)
				break;
			layerList.addAll(nextLayerList);
			depth++;
		}
		
		//Populate the states if they havent been created yet
		/*if(layeredStates == null) {
			InteractionState[][] arrayRef = new InteractionState[interactionGraph.size()][];
			for(int i = 0; i < interactionGraph.size(); i++) {
				InteractionState[] layerArray = new InteractionState[interactionGraph.get(i).size()];
				int finalI = i;
				for(int e = 0; e < interactionGraph.get(i).size(); e++) {
					int finalE = e;
					layerArray[e] = interactionHandlers.stream().filter(x -> x.layerDepth == finalI && x.elementIndex == finalE).findFirst().get().state;
				}
				arrayRef[i] = layerArray;
			}
			setInteractionLayeredStates(arrayRef);
		}*/
		//this is most likely a remnant of outdated logic
	}
	
	public List<Interaction> GetActiveInteractions(int interactionLayerDepth) {
		System.out.println("InteractionManager.GetActiveInteractions() - graphPath.size(): " + graphPath.size());
		
		List<Interaction> actives = new ArrayList<Interaction>();
		//Scan the Interaction graph from start to the current layer depth, scan should include root Persistent Intr's
		
		int depth = 0;
		List<Interaction> layerList = new ArrayList<Interaction>();
		
		//Collections.addAll(layerList, currentLocation.getInteractions());
		if(interactions != null)
			Collections.addAll(layerList, interactions);
		//else
		//	return null;
		
		while(true) {
			//get next layer list and overwrite layerList with it
			List<Interaction> nextLayerList = new ArrayList<Interaction>();
			int finalDepth = depth;
			int elementIndex = 0;
			//See if this layer has been moved thru
			GraphPathNode graphNode = null;
			if(depth < graphPath.size())
				graphNode = graphPath.get(depth);
			
			for(Interaction intr : layerList) {
				//Get the Handler for this Intr
				int elementFinal = elementIndex;
				InteractionHandler intrHandler = interactionHandlers.stream().filter(x -> x.layerDepth == finalDepth && x.elementIndex == elementFinal).findFirst().get();
				
				//If this is the intr we chose then add the resulting intr to be the next set of choices to scan next iteration
				if(graphNode != null && graphNode.choiceType == intr.Type()) {
					if(graphNode.wasSuccessfulOutcome) {
						if(intr.NextInteractions_OnSuccess() != null)
							nextLayerList.addAll( (List<Interaction>)Arrays.asList(intr.NextInteractions_OnSuccess()) );
					} else {
						if(intr.NextInteractions_OnFailure() != null)
							nextLayerList.addAll( (List<Interaction>)Arrays.asList(intr.NextInteractions_OnFailure()) );
					}
					
					//Use the cancel list to remove active choices
					InteractionType[] cancels = intrHandler.intr.getCancelPersistentIntrTypes();
					if(cancels != null && cancels.length > 0) {
						List<InteractionType> cancelList = (List<InteractionType>)Arrays.asList(cancels);
						actives.removeIf(x -> cancelList.contains(x.Type()));
					}
				} else if(!intrHandler.state.isUsed && (intrHandler.intr.getIsPersistentIntr() || graphNode == null)) {
					//If we aren't choosing this Intr at this depth and we know this isn't a Tree choice thats about to be passed over then add it to the choices
					actives.add(intr);
				}
				
				elementIndex++;
			}
			layerList.clear();
			layerList.addAll(nextLayerList);
			
			if(depth > interactionLayerDepth)
				break;
			
			depth++;
		}
		
		return actives;
	}
	
	public Interaction GetInteractionAt(int targetLayerDepth, InteractionType type) {
		System.out.println("InteractionManager.GetInteractionAt() - targetLayerDepth: " + targetLayerDepth + ", type: " + type);
		
		//Use the simplicity and beauty of lambdas :D
		Optional<InteractionHandler> intrOptional = interactionHandlers.stream().filter(x -> x.layerDepth() == targetLayerDepth && x.type() == type).findFirst();
		if(!intrOptional.isPresent()) {
			intrOptional = interactionHandlers.stream().filter(x -> !x.state().isUsed && x.type() == type).findFirst();
			if(!intrOptional.isPresent()) {
				System.err.println("InteractionManager.GetInteractionAt() - Interaction doesnt exist with depth: " + targetLayerDepth + " and type: " + type + " and it isn't an active persistent intr either.");
				return null;
			} else {
				return intrOptional.get().intr();
			}
		} else {
			return intrOptional.get().intr();
		}
	}
	
	public Interaction[] GetCurrentInteractionLayers() {
		if(graphPath.size() > 0) {
			//use a consistent method of searching the interaction tree
			int secondToLastIndex = graphPath.size()-1;
			GraphPathNode secondToLastNode = graphPath.get(secondToLastIndex);
			Interaction secondToLastIntr = GetInteractionAt(secondToLastIndex, secondToLastNode.choiceType);
			return graphPath.get(graphPath.size()-1).wasSuccessfulOutcome ? secondToLastIntr.NextInteractions_OnSuccess() : secondToLastIntr.NextInteractions_OnFailure();
		} else {
			return interList.stream().filter(x -> x.IsRevealed()).toArray(Interaction[]::new);
		}
	}
	
	public Mission GetUsersNextDirectMission() {
		//if(graphPath.size() == 0)
		//	System.out.println("InteractionManager.GetUsersNextDirectMission() - graphPath is empty so returned Mission will be null.");
		//this was an incorrect assumption, just because the graphPath is empty doesn't mean that there isn't an active mission, it only means that the user hasn't take any action for the current mission
		for(GraphPathNode node : graphPath)
			System.out.println("InteractionManager.GetUsersNextDirectMission() - graphPath node: " + node.choiceType + ", wasSuccessful: " + node.wasSuccessfulOutcome);
		
		//Check the last graphPath
		int deepestDepth = graphPath.size() - 1;
		InteractionHandler intrHandler = interactionHandlers.stream().filter(x -> x.layerDepth() == deepestDepth && x.type() == graphPath.get(deepestDepth).choiceType).findFirst().orElse(null);
		if(intrHandler != null && intrHandler.intr().GotoMissionId() != null && !intrHandler.intr().GotoMissionId().isEmpty())
			return Missions.getById(intrHandler.intr().GotoMissionId());
		else
			return null;
	}
	
	//Runtime, Setup and Helper Stuff - End
}
