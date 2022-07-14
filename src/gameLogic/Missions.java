package gameLogic;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import data.CharacterData;
import data.FlexibleTransition;
import data.ItemData;
import data.MissionNode;

import dataShared.ActorData;
import dataShared.InteractionData;
import dataShared.LocationContentData;
import dataShared.LocationContentData.MapLocationStructure;
import dataShared.LocationContentData.MissionStructure;
import dataShared.MapLocationData;

import enums.EnvironmentType;
import enums.InteractionType;
import enums.SceneLayeringType;
import enums.WorldTileType;
import enums.SettlementType;
import enums.TransitionType;
import enums.SettlementDesignation;

import gameLogic.Mission.MissionStatusType;
import gui.GUIManager;


public class Missions {
	//Initialization - Start
	private static Missions instance;
	
	//Missions - Start
	
	//Dark forest - fight or flee
	private static Mission m_0;
	//public static Mission M_0() { return m_0; }
	
	//Cliffside, kicked off
	private static Mission m_1_a;
	//public static Mission M_1_a() { return m_1_a; }
	
	//River, exploration starts
	private static Mission m_2_a;
	//public static Mission M_2_a() { return m_2_a; }
	
	//Grassland, escaped pursuer
	private static Mission m_1_b;
	//public static Mission M_1_b() { return m_1_b; }
	
	//Magic Forest, get Arbor Sigil and start exploration
	private static Mission m_2_b;
	//public static Mission M_2_b() { return m_2_b; }
	
	//Town/city
	private static Mission m_3;
	//public static Mission M_3() { return m_3; }
	
	//public List<Mission> missionList;
	//This is a convenience method called by Mission from all of its constructors except for the constructor used to load MissionDatas from json files
	//its to avoid having to manually type missionList.add(...); everytime a mission is instantiated
	
	//public static void OnMissionConstruction(Mission constructedMission) {
	//	instance.missionList.add(constructedMission);
	//}
	//Since we're not hardcoding declared missions anymore we don't need this
	
	private List<FlexibleTransition> flexibleTransitions;
	/**
	 * This is called on start during Game.TryLoadData() if save data exists. This list will be overwritten if the player starts a new game
	 * @param transitions
	 */
	public static void SetFlexibleTransitionsLoadedFromSaveData(List<MissionNode> missionTree, List<FlexibleTransition> flexibleTransitions) {
		System.out.println("Missions.SetFlexibleTransitionsLoadedFromSaveData() - missionTree exists: " + (missionTree != null));
		instance.missionTree = missionTree;
		instance.flexibleTransitions = flexibleTransitions;
	}
	
	private static Mission s_1;
	public static Mission S_1() { return s_1; }
	
	//Missions - End
	
	//Static MapLocations - Start
	
	//private static MapLocation kogaKeep;
	//public static MapLocation KogaKeep() {return kogaKeep;}
	//this is not something we do anymore
	
	//Static MapLocations - End
	
	//WorldMap Association - Start
	
	//Instruction cluster to tell the Worldmap generator that a cluster of worldTile need to be found at certain proximity of eachother
		//for example: The "Dark Forest" MapLocation for m_0 should be a forest tile with the ClosestPossible proximity to the tiles required for its children
			//Path m_1_a: m_0.forest -> m_1_a.mountain -> m_2_a.river
			// &
			//Path m_1_b: m_0.forest -> m_1_b.grassland
		//for this we'd need the forest plot thats closest to a mountainPlot.(with a river) and a grasslandPlot
	//use a Map<Mission, List<MissionPathInstruction>>
	public class InstructionCluster {
		public InstructionCluster(Mission centralMission) {
			this.centralMission = centralMission;
		}
		private Mission centralMission;
		public Mission GetCentralMission() {
			return centralMission;
		}
		private List<ClusterLink> clusterChains = new ArrayList<ClusterLink>();
		public List<ClusterLink> GetClusterChains() {
			return clusterChains;
		}
		public void AddClusterChain(ClusterLink clusterLayer) {
			clusterChains.add(clusterLayer);
		}
	}
	
	public class ClusterLink {
		public ClusterLink(Mission layersMission, ClusterLink nextLink) {
			this.layersMission = layersMission;
			this.nextLink = nextLink;
		}
		private Mission layersMission;
		public Mission GetLayersMission() {
			return layersMission;
		}
		private ClusterLink nextLink;
		public ClusterLink NextLink() {
			return nextLink;
		}
	}
	
	//Critical Mission Path, if followed it'd yield the most efficient run of the game. It serves as the procedure for placing missions on the worldMap in a fluid way compatible with procedure map generation
	public enum ProximityType { ClosestTypedTile, FarthestTypedTile, AnyTypedTile,
								ClosestTypedPlot, FarthestTypedPlot, AnyTypedPlot };
	public class MissionPathInstruction {
		public MissionPathInstruction(Mission startMission, ProximityType proximityOfStartToNext, Mission nextMission) {
			this.startMission = startMission;
			this.proximityOfStartToNext = proximityOfStartToNext;
			this.nextMission = nextMission;
		}
		private Mission startMission;
		public Mission getStartMission() { return startMission; }
		private ProximityType proximityOfStartToNext;
		public ProximityType getProximityOfStartToNext() { return proximityOfStartToNext; }
		private Mission nextMission;
		public Mission getNextMission() { return nextMission; }
	}
	//private static List<MissionPathInstruction> missionPathInstructions = new ArrayList<MissionPathInstruction>();
	//public static List<MissionPathInstruction> GetMissionPathInstructions() {
	//	return missionPathInstructions;
	//}
	//This is the sequential collection of all the instructions, reading the InstructionSets lsit we can move from an instruction cluster to a series of instructions to another cluster to etc in a fluid way
	public class InstructionSet {
		public InstructionSet(InstructionCluster instructionCluster) {
			this.instructionCluster = instructionCluster;
		}
		public InstructionSet(List<MissionPathInstruction> instructionSeries) {
			this.instructionSeries = instructionSeries;
		}
		private InstructionCluster instructionCluster;
		public InstructionCluster GetInstructionCluster() {
			return instructionCluster;
		}
		//or
		private List<MissionPathInstruction> instructionSeries;
		public List<MissionPathInstruction> GetInstructionSeries() {
			return instructionSeries;
		}
	}
	private static List<InstructionSet> instructionSets = new ArrayList<InstructionSet>();
	public static List<InstructionSet> GetInstructionSets() {
		return instructionSets;
	}
	
	//Static non-active or conditional locations
	//private static List<MapLocation> staticMapLocations = new ArrayList<MapLocation>();
	//public static List<MapLocation> GetAllStaticMapLocations() {
	//	return staticMapLocations;
	//}
	
	//WorldMap Association - End
	
	//Runtime helper members
	//private static int locationsVisitedSinceLastMission;
	private static Random r;
	
	//Mission Naming Conventions
	//[M for Main Quest or S for Side Quest]_[Index]_[Multi-pathquestline]
	//[G for Generic Location]_[Theme: forest, riverBank, bridge, mountianPass, etc]
	
	public Map<String, List<ActorData>> sceneActorsMap;
	
	public static CharacterData GetActorCharacter(MapLocation mapLocation, String actorId) {
		List<ActorData> actorDatas = null;
		
		String relativeSceneDirectory = mapLocation.getRelativeSceneDirectory();
		if(relativeSceneDirectory == null)
			relativeSceneDirectory = mapLocation.getRelativeComboSettlementSceneDirectory();
		if(relativeSceneDirectory == null) {
			System.err.println("Missions.GetActorCharacter() - For MapLocation: " + mapLocation.getName() + ", both its RelativeSceneDirectory and RelativeComboSettlementSceneDirectory are null."
					+ "How else is its relative scene directory being stored? Check the scene's json for missing data. If there's a new directory variable then add support for it here.");
		} else {
			actorDatas = instance.sceneActorsMap.get(relativeSceneDirectory);
			if(actorDatas == null) {
				System.err.println("Missions.GetActorCharacter() - sceneActorsMap doesn't contain an entry for relativeSceneDirectory: " + relativeSceneDirectory +
					". Here are the existing scene directories:");
				for(String key : instance.sceneActorsMap.keySet()) {
					System.out.println("  sceneActorsMap key: " + key);
				}
			}
		}
		
		ActorData actorData = actorDatas.stream().filter(x -> x.actorId.equals(actorId)).findFirst().orElse(null);
		if(actorData == null) {
			System.err.println("Missions.GetActorCharacter() - There is no actorData for sceneDirectory: " + relativeSceneDirectory + " and actorId: " + actorId);
			return null;
		}
		String characterId = actorData.characterDataId;
		if(characterId == null || characterId.isEmpty())
			return null;
		else
			return instance.characterDataList.stream().filter(x -> x.getId().equals(characterId)).findFirst().get();
	}
	
	public static List<ActorData> GetAllLocationActors(String relativeSceneDirectory) {
		if(!instance.sceneActorsMap.containsKey(relativeSceneDirectory))
			System.err.println("Missions.GetAllLocationActors() - Can't find a sceneActorsMap entry for scene directory key: " + relativeSceneDirectory + 
					", instance.sceneActorsMap.size(): " + instance.sceneActorsMap.size());
		return instance.sceneActorsMap.get(relativeSceneDirectory);
	}
	
	private static final String locationContentDataPathRoot_EDITOR = "C:/Users/Magnus/Desktop/Java/Eclipse_Workspace_V2019/AgentsOfTheOutcaste/src/resources/mapLocationScenes";
	public static final String locationContentDataPathRoot = "resources/mapLocationScenes/";
	public List<LocationContentData> locationContentDatas;
	public static void LoadLocationContentData(Missions instance, Gson gson) {
		instance.locationContentDatas = new ArrayList<LocationContentData>();
		
		//do a forloop for all the text files in the root directory
		//locationContentDataPathRoot
		List<String> fileNames = new ArrayList<String>();
		
		//Blindly grabbing file names has to be done differently when running the project via the editor or via a jar file
		boolean isRunningInEditor = Missions.class.getResource("Missions.class").toString().startsWith("file:");
		if(isRunningInEditor) {
			System.out.println("Missions.LoadLocationContentData() - Grabbing filenames from project directory");
			//do windows IO type filename grab
			final File projectRelativeFolder = new File(locationContentDataPathRoot_EDITOR);
			for (final File fileEntry : projectRelativeFolder.listFiles()) {
		        if (fileEntry.isDirectory()) {
		        	//System.out.println("Directory: " + fileEntry.getName());
		        } else {
		        	if(fileEntry.getName().startsWith("LocationContentData_")) {
		        		System.out.println("Missions.LoadLocationContentData() - Found LocationContentData file: " + fileEntry.getName());
		        		fileNames.add(fileEntry.getName());
		        	} //else
		        		//System.out.println("File: " + fileEntry.getName());
		        }
		    }
		} else {
			String root = "/" + locationContentDataPathRoot;
			URI uri = null;
			try {
				uri = instance.getClass().getResource(root).toURI();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
	        Path myPath = null;
	        FileSystem fileSystem = null;
	        if (uri.getScheme().equals("jar")) {
				try {
					fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
					myPath = fileSystem.getPath(root);
				} catch (IOException e) {
					e.printStackTrace();
				}
	        } else {
	            myPath = Paths.get(uri);
	        }
	        Stream<Path> walk = null;
			try {
				walk = Files.walk(myPath, 1);
			} catch (IOException e) {
				e.printStackTrace();
			}
	        for (Iterator<Path> it = walk.iterator(); it.hasNext();) {
	            String name = it.next().toString();
				//System.err.println(name);
				if (name.endsWith(".json")) {
					String[] fileNameSplits = name.split("/");
					String fileName = fileNameSplits[fileNameSplits.length-1];
					if(fileName.startsWith("LocationContentData_")) {
						fileNames.add(fileName);
						//System.err.println("saved filename: " + fileName);
					}
				}
	        }
	        walk.close();
	        if(fileSystem != null) {
		        try {
					fileSystem.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
		}
		
		for(String fileName : fileNames) {
			
			String fullPath = locationContentDataPathRoot + fileName;
			System.out.println("Missions.LoadLocationContentData() - Attempting to get resource at: " + fullPath);
			
			InputStream is = instance.getClass().getClassLoader().getResourceAsStream(fullPath);
			if(is == null) {
				System.err.println("Missions.LoadLocationContentData() - File not found at: " + fullPath + " ... Dont forget to Refresh the Java project after adding new files.");
				Thread.dumpStack();
				return;
			}
			String jsonString = null;
			try {
				InputStreamReader isReader = new InputStreamReader(is);
				BufferedReader reader = new BufferedReader(isReader);
				StringBuffer sb = new StringBuffer();
				String str;
				while((str = reader.readLine())!= null){
					sb.append(str);
				}
				jsonString = sb.toString();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			LocationContentData locationContentData = gson.fromJson(jsonString, LocationContentData.class);
			
			//Populate list
			if(locationContentData == null) {
				System.err.println("Missions.LoadLocationContentData() - LocationContentData didn't exist or wasn't successfully loaded. The mapLocationList and missionList will remain empty.");
			} else {
				instance.locationContentDatas.add(locationContentData);
				
				if(locationContentData.mapLocationStructures.size() == 0)
					System.err.println("Missions.LoadLocationContentData() - locationContentData has no mapLocationStructures, fileName: " + fileName + ", fullPath: " + fullPath);
				else {
					for(MapLocationStructure locStruct : locationContentData.mapLocationStructures) {
						System.out.println("Missions.LoadLocationContentData() - Adding mapLocationStructure: " + locStruct.mapLocationData.name + ", from contents: " + fullPath);
					}
				}
			}
		}
		
		//This belongs here so that the sceneActorsMap will be populated whether we're starting a new game or loading one
		if(instance.sceneActorsMap == null)
			instance.sceneActorsMap = new HashMap<String, List<ActorData>>();
		else
			instance.sceneActorsMap.clear();
		for(LocationContentData locationContentDatum : instance.locationContentDatas) {
			List<ActorData> actorDataList = new ArrayList<>(Arrays.asList(locationContentDatum.actorDatas)); 
			//System.out.println("Missions.BuildMissionsAndMapLocationsFromLocationContentData() - ActorDatas.length: " + locationContentDatum.actorDatas.length + ", actorDataList.size(): " + actorDataList.size());
			instance.sceneActorsMap.put(locationContentDatum.mapLocationStructures.get(0).mapLocationData.sceneDirectory, actorDataList);	
		}
	}
	
	private void BuildMissionsAndMapLocationsFromLocationContentData() {
		//Clear all preexisting stuff
		//if(sceneActorsMap == null)
		//	sceneActorsMap = new HashMap<String, List<ActorData>>();
		//else
		//	sceneActorsMap.clear();
		
		//instance.missionList.clear();
		mapLocationTemplates = new ArrayList<MapLocation>();
		uniqueMapLocations = new ArrayList<MapLocation>();
		List<Mission> missionList = new ArrayList<Mission>();
		
		System.out.println("Missions.BuildMissionsAndMapLocationsFromLocationContentData()");
		
		for(LocationContentData locationContentDatum : locationContentDatas) {
			//List<ActorData> actorDataList = new ArrayList<>(Arrays.asList(locationContentDatum.actorDatas)); 
			//System.out.println("Missions.BuildMissionsAndMapLocationsFromLocationContentData() - ActorDatas.length: " + locationContentDatum.actorDatas.length + ", actorDataList.size(): " + actorDataList.size());
			//sceneActorsMap.put(locationContentDatum.mapLocationStructures.get(0).mapLocationData.sceneDirectory, actorDataList);
			
			for(MapLocationStructure mapLocStruct : locationContentDatum.mapLocationStructures) {
				
				List<Interaction> interactionList = new ArrayList<Interaction>();
				for(int i = 0; i < mapLocStruct.interactionDatas.size(); i++) {
					InteractionData intrSData = mapLocStruct.interactionDatas.get(i);
					interactionList.add( new Interaction(intrSData) );
				}
				//populate all the references for each interaction's next successful interactions
				for(int i = 0; i < mapLocStruct.interactionDatas.size(); i++) {
					//Success
					Interaction[] nextIntrs_success = null;
					String[] intrIds_success = mapLocStruct.interactionDatas.get(i).nextInteractions_onSuccess_ids;
					if(intrIds_success != null && intrIds_success.length > 0) {
						nextIntrs_success = new Interaction[intrIds_success.length];
						final int finalI = i;
						for(int n = 0; n < intrIds_success.length; n++) {
							final int finalN = n;
							nextIntrs_success[n] = interactionList.stream().filter(x -> x.getId().equals( mapLocStruct.interactionDatas.get(finalI).nextInteractions_onSuccess_ids[finalN] )).findFirst().get();
						}
					}
					interactionList.get(i).SetNextInteractions_OnSuccess(nextIntrs_success);
					//Failure
					Interaction[] nextIntrs_failure = null;
					String[] intrIds_failure = mapLocStruct.interactionDatas.get(i).nextInteractions_onFailure_ids;
					if(intrIds_failure != null && intrIds_failure.length > 0) {
						nextIntrs_failure = new Interaction[intrIds_failure.length];
						final int finalI = i;
						for(int n = 0; n < intrIds_failure.length; n++) {
							final int finalN = n;
							nextIntrs_failure[n] = interactionList.stream().filter(x -> x.getId().equals( mapLocStruct.interactionDatas.get(finalI).nextInteractions_onFailure_ids[finalN] )).findFirst().get();
						}
					}
					interactionList.get(i).SetNextInteractions_OnFailure(nextIntrs_failure);
				}
				Interaction[] interactions = interactionList.stream().toArray(Interaction[]::new);
				
				/*if(mapLocStruct.missionStructures != null && mapLocStruct.missionStructures.size() > 0) {
					//We either create a new instance of the MapLocation from its LocationContentData file or grab its preexisting loaded instance from the SaveData.WorldTileData structure
					MapLocationData sData = mapLocStruct.mapLocationData;
					SceneLayeringType sceneLayeringType = Game.Instance().ReadSceneData(sData.name, Game.SceneDirectoryRoot() + sData.sceneDirectory).sceneLayeringType;
					MapLocation newMapLocationInstance = new MapLocation(sData.id, sData.enviType, sData.tileType, sData.settlementType, sData.settlementDesignation, sData.name, sData.description,
							sData.sceneDirectory, sData.customBGImagePath, interactions, mapLocStruct.interactionlessDialographyDatas, sceneLayeringType, mapLocStruct);
					MapLocation mapLocation = null;
					//Use blank new template for MapLocation on NewGame
					if(Game.Instance().GetWorldmapData() == null) {
						//instance.mapLocations.add(newMapLocationInstance);
						mapLocation = newMapLocationInstance;
					} else {
						WorldTileData matchingWorldTileData = Game.Instance().GetWorldmapData().GetWorldMapDatas().values().stream().filter(
								x -> x.mapLocation != null && x.mapLocation.getId().equals(sData.id)).findFirst().orElse(null);
						if(matchingWorldTileData == null) {
							//Determine if this is a flexible mission that hasn't been started yet, in which case its MapLocation will remain null
							boolean areAllMissionsAtThisLocationFlexible = true;
							for(String missionSeqName : flexibleTransitionMissions) {
								String flexMissionId = missionSequenceIdentifiers.stream().filter(x -> x.identifier.equals(missionSeqName)).findFirst().orElse(null).missionId;
								if(!mapLocStruct.missionStructures.stream().anyMatch(x -> x.missionData.id.equals(flexMissionId))) {
									areAllMissionsAtThisLocationFlexible = false;
									break;
								}
							}
							if(areAllMissionsAtThisLocationFlexible) {
								for(Mission mission : Game.Instance().getMissions()) {
									if(mission.getMissionStatus() != Mission.MissionStatusType.Concluded)
										continue;
									String completedMissionId = mission.getId();
									if(mapLocStruct.missionStructures.stream().anyMatch(x -> x.missionData.id.equals(completedMissionId)))
										System.err.println("Missions.BuildMissionsAndMapLocationsFromLocationContentData() - Can't find the MapLocation for the previously completed FlexibleMission: " + 
												completedMissionId);
								}
							} else {
								System.err.println("Missions.BuildMissionsAndMapLocationsFromLocationContentData() - Can't load a newly designed MapLocation into a world that's already been generated"
										+ " -OR- Can't find the MapLocation for a mission."
										+ " Start a New Game for this new MapLocation to be included in the game. ... Fallingback to a blank new instance of this MapLocation.");
								//instance.mapLocations.add(newMapLocationInstance);
								mapLocation = newMapLocationInstance;
							}
						} else {
							System.out.println("Missions.BuildMissionsAndMapLocationsFromLocationContentData() - Found an existing MapLocation for: " + matchingWorldTileData.mapLocation.getName());
							//instance.mapLocations.add(matchingWorldTileData.mapLocation);
							mapLocation = matchingWorldTileData.mapLocation;
						}
					}*/
				MapLocationData sData = mapLocStruct.mapLocationData;
				SceneLayeringType sceneLayeringType = Game.Instance().ReadSceneData(Game.SceneDirectoryRoot() + sData.sceneDirectory).sceneLayeringType;
				System.out.println("Missions.BuildShit... - MapLocationData.id: " + sData.id);
				if(sData.id == null || sData.id.isEmpty()) {
					MapLocation templateInstance = new MapLocation(null, sData.enviType, sData.tileType, sData.settlementType, sData.settlementDesignation, sData.name, sData.description,
							sData.sceneDirectory, sData.customBGImagePath, interactions, mapLocStruct.interactionlessDialographyDatas, sceneLayeringType, mapLocStruct);
					mapLocationTemplates.add(templateInstance);
				} else {
					MapLocation uniqueInstance = new MapLocation(sData.id, sData.enviType, sData.tileType, sData.settlementType, sData.settlementDesignation, sData.name, sData.description,
							sData.sceneDirectory, sData.customBGImagePath, interactions, mapLocStruct.interactionlessDialographyDatas, sceneLayeringType, mapLocStruct);
					uniqueMapLocations.add(uniqueInstance);
				}
				
				if(mapLocStruct.missionStructures != null && mapLocStruct.missionStructures.size() > 0) {
					
					
					for(MissionStructure missionStructure : mapLocStruct.missionStructures) {
						System.out.println("Construct mission: " + missionStructure.missionData.name);
						
						
						//Get instantiate mission's interactions
						List<Interaction> missionInterList = new ArrayList<Interaction>();
						for(int i = 0; i < missionStructure.interactionDatas.size(); i++) {
							InteractionData intrSData = missionStructure.interactionDatas.get(i);
							if(intrSData.battleData != null && (intrSData.battleData.name == null || intrSData.battleData.name.isEmpty())) {
								//System.out.println("BATTLE IS EMPTY, SETTING TO NULL");
								intrSData.battleData = null;
							}
							missionInterList.add( new Interaction(intrSData) );
						}
						//populate all the references for each interaction's next successful interactions
						for(int i = 0; i < missionStructure.interactionDatas.size(); i++) {
							//Success
							Interaction[] nextIntrs_success = null;
							String[] intrIds_success = missionStructure.interactionDatas.get(i).nextInteractions_onSuccess_ids;
							if(intrIds_success != null && intrIds_success.length > 0) {
								nextIntrs_success = new Interaction[intrIds_success.length];
								final int finalI = i;
								for(int n = 0; n < intrIds_success.length; n++) {
									final int finalN = n;
									nextIntrs_success[n] = missionInterList.stream().filter(x -> x.getId().equals( missionStructure.interactionDatas.get(finalI).nextInteractions_onSuccess_ids[finalN] )).findFirst().get();
								}
							}
							missionInterList.get(i).SetNextInteractions_OnSuccess(nextIntrs_success);
							//Failure
							Interaction[] nextIntrs_failure = null;
							String[] intrIds_failure = missionStructure.interactionDatas.get(i).nextInteractions_onFailure_ids;
							if(intrIds_failure != null && intrIds_failure.length > 0) {
								nextIntrs_failure = new Interaction[intrIds_failure.length];
								final int finalI = i;
								for(int n = 0; n < intrIds_failure.length; n++) {
									final int finalN = n;
									nextIntrs_failure[n] = missionInterList.stream().filter(x -> x.getId().equals( missionStructure.interactionDatas.get(finalI).nextInteractions_onFailure_ids[finalN] )).findFirst().get();
								}
							}
							missionInterList.get(i).SetNextInteractions_OnFailure(nextIntrs_failure);
						}
						Interaction[] missionInteractions = missionInterList.stream().toArray(Interaction[]::new);
						
						
						//Mission mission = new Mission(missionStructure.missionData, mapLocation, missionInteractions, missionStructure.interactionlessDialographyDatas.get(0));
						Mission mission = new Mission(missionStructure.missionData, missionInteractions, missionStructure.interactionlessDialographyDatas.get(0), missionStructure, mapLocStruct.mapLocationData.tileType);
						//If this MapLocation is a unique one then we can set this mission's mapLocationId here, it'll be needed during (WorldGenerationProcess).PlaceLocations()
						if(sData.id != null && !sData.id.isEmpty())
							mission.setMapLocationId(sData.id);
						
						missionList.add(mission);
						//Store the mission in saveData
						Game.Instance().RegisterMission(mission);
					}
				}
			}
		}
		
		
		//Create direct mission references and all associated instructions for mission placement during world generation
		
		//Static Non-Unity-Designed MapLocations - Start
		
		//staticMapLocations = new ArrayList<MapLocation>();
		
		//TODO
		//This approach of doing a fresh instantiation won't work with the new way we load mission/mapLocation records from SaveData so load the InteractionManager from the matching MapLocation record.
		/*kogaKeep = new MapLocation(
			EnvironmentType.Forest,
			WorldTileType.forest,
			SettlementType.Castle,
			SettlementDesignation.Outcaste,
			//Location Name
			"Koga Keep",
			//Location Description
			"One of two castles ruled by the notorious Koga clan.",
			//Scene Directory Name + /
			"Castle/",
			//bgImagePath
			null,
			new Interaction[] {
			},
			SceneLayeringType.BothLayers
		);
		staticMapLocations.add(kogaKeep);*/
		
		//Static Non-Unity-Designed MapLocations - End
		
		
		//Connect Mission instances to their Sequence Identifier - Start
		
		final String missionId1 = missionSequenceIdentifiers.stream().filter(x -> x.identifier.equals("m_0")).findFirst().get().missionId;
		m_0 = missionList.stream().filter(x -> x.getId().equals(missionId1)).findFirst().get();
		
		final String missionId2 = missionSequenceIdentifiers.stream().filter(x -> x.identifier == "m_1_a").findFirst().get().missionId;
		m_1_a = missionList.stream().filter(x -> x.getId().equals(missionId2)).findFirst().get();
		
		final String missionId3 = missionSequenceIdentifiers.stream().filter(x -> x.identifier == "m_2_a").findFirst().get().missionId;
		m_2_a = missionList.stream().filter(x -> x.getId().equals(missionId3)).findFirst().get();
		
		final String missionId4 = missionSequenceIdentifiers.stream().filter(x -> x.identifier == "m_1_b").findFirst().get().missionId;
		m_1_b = missionList.stream().filter(x -> x.getId().equals(missionId4)).findFirst().get();
		
		final String missionId5 = missionSequenceIdentifiers.stream().filter(x -> x.identifier == "m_2_b").findFirst().get().missionId;
		m_2_b = missionList.stream().filter(x -> x.getId().equals(missionId5)).findFirst().get();
		
		final String missionId6 = missionSequenceIdentifiers.stream().filter(x -> x.identifier == "m_3").findFirst().get().missionId;
		m_3 = missionList.stream().filter(x -> x.getId().equals(missionId6)).findFirst().get();
		
		//Connect Missions to their Sequence Identifier - End
		
		//Mission Tree Setup - Start
		//Create the missionTree for use by the mission system to easily track mission sequence
		
		missionTree = new ArrayList<MissionNode>();
		
		missionTree.add(new MissionNode(null, null, m_0.getId()));
		
		missionTree.add(new MissionNode(m_0.getId(), TransitionType.Direct, m_1_a.getId()));
		missionTree.add(new MissionNode(m_0.getId(), TransitionType.Direct, m_1_b.getId()));
		
		missionTree.add(new MissionNode(m_1_a.getId(), TransitionType.Direct, m_2_a.getId()));
		missionTree.add(new MissionNode(m_1_b.getId(), TransitionType.Flexible, m_2_b.getId()));
		
		missionTree.add(new MissionNode(m_2_a.getId(), TransitionType.Direct, m_3.getId()));
		missionTree.add(new MissionNode(m_2_b.getId(), TransitionType.Direct, m_3.getId()));
		
		missionTree.add(new MissionNode(m_3.getId(), null, null));
		
		//Mission Tree Setup - End
		
		//Post-constuction setup
		flexibleTransitions = new ArrayList<FlexibleTransition>();
		instructionSets = new ArrayList<InstructionSet>();
		
		//Mission Linking and Plotting Procedure
		
		//First Instruction Set - Cluster Type
		InstructionCluster instructionCluster_0 = new InstructionCluster(m_0);
		
		instructionCluster_0.AddClusterChain(new ClusterLink(m_1_a, new ClusterLink(m_2_a, null)));
		m_1_a.LinkMission_OnInteraction(InteractionType.Talk, m_2_a);
		
		instructionCluster_0.AddClusterChain(new ClusterLink(m_1_b, null));
		//Add Flexible path for end of b branch
		flexibleTransitions.add(new FlexibleTransition(m_1_b.getId(), 2, 0.5f, m_2_b.getId(), m_2_b.getGenericLocationEnvironmentType()));
		
		instructionSets.add(new InstructionSet(instructionCluster_0));
		
		//Second Instruction Set - Series Type
		List<MissionPathInstruction> instructionSeries_1 = new ArrayList<MissionPathInstruction>();
		
		//Add missions in a series
		instructionSeries_1.add(new MissionPathInstruction(m_0, ProximityType.ClosestTypedTile, m_3));
		
		instructionSets.add(new InstructionSet(instructionSeries_1));
		
		//Send all persistent mission sequence building data to game for saving
		Game.Instance().SaveMissionTransitions(missionTree, flexibleTransitions);
		
		System.out.println("Missions.BuildMissionsAndMapLocationsFromLocationContentData() - Build process is complete.");
	}
	
	/**
	 * Reload everything from the LocationContentData files on NewGame
	 */
	public static void RebuildMissionsAndMapLocations() {
		instance.BuildMissionsAndMapLocationsFromLocationContentData();
	}
	
	/**
	 * Holds all the references to generic MapLocation Templates that were created during initialization.
	 */
	private List<MapLocation> mapLocationTemplates;
	
	/**
	 * Used during world generation to provide copies of all generic MapLocations templates that only constitute a Nature Layer. Not used to generate unique locations with built-in MapLocationIds.
	 * @param enviType
	 * @param worldType
	 * @return
	 */
	public static MapLocation GetMapLocationTemplateCopy(EnvironmentType enviType, WorldTileType worldType) {
		MapLocation mapLocationTemplate = instance.mapLocationTemplates.stream().filter(m ->
			m.getEnviType() == enviType
			&&
			m.getTileType() == worldType
			&&
			m.getSettlementType() == null
			&&
			m.getSettlementDesignation() == null
			).findFirst().orElse(null);
		if(mapLocationTemplate == null)
			System.err.println("Missions.GetMapLocationTemplate(NATURE LAYER) - Couldn't find a template with parameters: " + enviType +", "+ worldType);
		return new MapLocation( mapLocationTemplate );
	}
	
	/**
	 * Used to during world generation to provide copies of all generic MapLocations that are
	 * 	1. a generic MapLocation that fulfills BOTH the Nature and Settlement layers.
	 *  -OR- (if one does not exist)
	 *  2. a new MapLocation instance created by combining the data of the matching Nature layer MapLocation and the matching Settlement layer MapLocation.
	 * Not used to retrieve unique locations with built-in MapLocationIds.
	 * @param enviType
	 * @param worldType
	 * @param settlementType
	 * @param settlementDesignation
	 * @return
	 */
	public static MapLocation GetMapLocationTemplateCopy(EnvironmentType enviType, WorldTileType worldType, SettlementType settlementType, SettlementDesignation settlementDesignation) {
		MapLocation mapLocationTemplate = instance.mapLocationTemplates.stream().filter(m ->
			m.getEnviType() == enviType
			&&
			m.getTileType() == worldType
			&&
			m.getSettlementType() == settlementType
			&&
			m.getSettlementDesignation() == settlementDesignation
			).findFirst().orElse(null);
		if(mapLocationTemplate == null) {
			//System.err.println("Missions.GetMapLocationTemplate() - Couldn't find a template with parameters: " + enviType +", "+ worldType +", "+ settlementType +", "+ settlementDesignation);
			//This means that there is no existing generic location that matches both the nature and settlement layers, which is fine. It means we must assemble a MapLocation by combining a Nature layer
			//and Settlement layer into one MapLocation.
			
			//Get nature layer location
			MapLocation natureLocationTemplate = instance.mapLocationTemplates.stream().filter(m ->
			m.getEnviType() == enviType
			&&
			m.getTileType() == worldType
			&&
			m.getSettlementType() == null
			&&
			m.getSettlementDesignation() == null
			).findFirst().orElse(null);
			if(natureLocationTemplate == null) {
				System.err.println("Missions.GetMapLocationTemplate() - Couldn't find a template with parameters: " + enviType +", "+ worldType);
				return null;
			}
			
			MapLocation settlementLocationTemplate = instance.mapLocationTemplates.stream().filter(m ->
			m.getSettlementType() == settlementType
			&&
			m.getSettlementDesignation() == settlementDesignation
			).findFirst().orElse(null);
			if(settlementLocationTemplate == null) {
				System.err.println("Missions.GetMapLocationTemplate() - Couldn't find a template with parameters: " + settlementType +", "+ settlementDesignation);
				return null;
			}
			MapLocationStructure selectStructure = GetMapLocationStructures(null, null, settlementType, settlementDesignation, SceneLayeringType.SettlementLayer).get(0);
			
			mapLocationTemplate = new MapLocation(
					enviType, worldType, settlementType, settlementDesignation,
					settlementLocationTemplate.getName(),
					settlementLocationTemplate.getDescription(),
					new String[] { natureLocationTemplate.getRelativeSceneDirectory(), settlementLocationTemplate.getRelativeSceneDirectory() },
					natureLocationTemplate.getCustomBgImagePath(),
					null,
					selectStructure.interactionlessDialographyDatas,
					SceneLayeringType.BothLayers,
					selectStructure
			);
		} else {
			System.out.println("Missions.GetMapLocationTemplateCopy() - Found unmerged template with enviType: " + enviType + ", worldType: " + worldType + ", settlementType: " + settlementType +
					", settlementDesignation: " + settlementDesignation);
		}
		
		if(mapLocationTemplate.getName().equals("Vassal's Castle"))
			System.out.println("Missions.GetMapLocationTemplateCopy() - Vassal's Castle with enviType: " + enviType + ", worldType: " + worldType + ", settlementType: " + settlementType +
				", settlementDesignation: " + settlementDesignation + ", sceneDirectory: " + mapLocationTemplate.getSceneDirectory() + ", comboNatureDir:" + mapLocationTemplate.getComboNatureSceneDirectory()
				+ ", comboSettlementDir: " + mapLocationTemplate.getComboSettlementSceneDirectory());
		
		return new MapLocation( mapLocationTemplate );
	}

	/**
	 * Collected upon loading of LocationContentData, MapLocationData with existing ids get added to this list, otherwise MapLocations with no ids get added to the mapLocationTemplates list.
	 */
	private List<MapLocation> uniqueMapLocations;
	
	public static MapLocation GetUniqueMapLocation(String mapLocationId) {
		MapLocation matchingLoc = instance.uniqueMapLocations.stream().filter(x -> x.getId().equals(mapLocationId)).findFirst().orElse(null);
		if(matchingLoc == null)
			System.err.println("MissionsGetUniqueMapLocation() - Couldn't find a unique MapLocation with id: " + mapLocationId + ". Returning null.");
		return matchingLoc;
	}
	
	/*
	 * Return only the MapLocation templates of SceneLayeringType.SettlementLayer
	 */
	public static MapLocation[] GetAllSettlementTemplateCopys() {
		MapLocation[] settlementLocations = instance.mapLocationTemplates.stream().filter(x -> x.getTileType() == null).toArray(MapLocation[]::new);
		MapLocation[] copies = new MapLocation[settlementLocations.length];
		for(int i = 0; i < settlementLocations.length; i++)
			copies[i] = new MapLocation( settlementLocations[i] );
		return copies;
	}
	
	//This has been replaced by the MapLocationStructure variable stored directly within MapLocation
	/*public static MapLocationStructure GetMapLocationStructure(String mapLocationId) {
		MapLocationStructure mapLocStruct = null;
		//First search for a unique instance of the target MapLocation
		LocationContentData locContentWithMatchingId = instance.locationContentDatas.stream().filter(x -> x.mapLocationStructures.stream().anyMatch(m -> m.mapLocationData.id.equals(mapLocationId))).findFirst().orElse(null);
		if(locContentWithMatchingId == null) {
			System.err.println("Missions.GetMapLocationStructure(String mapLocationId) - Couldn't find a unique mapLocationStruct with the id: " + mapLocationId);
			return null;
		}
		//Now that we know the target MapLocationStructure exists inside a LocationContentData, retrieve it
		mapLocStruct = locContentWithMatchingId.mapLocationStructures.stream().filter(m -> m.mapLocationData.id.equals(mapLocationId)).findFirst().orElse(null);
		return mapLocStruct;
	}*/
	
	public static List<MapLocationStructure> GetMapLocationStructures(EnvironmentType enviType, WorldTileType tileType, SettlementType settlementType, SettlementDesignation settlementDesig, SceneLayeringType sceneLayeringType) {
		List<MapLocationStructure> mapLocStructs = new ArrayList<MapLocationStructure>();
		//If theres no unique instance then this is a generic mapLocation class with an id that was randomly generated during worldmap creation
		//search the locationContentDatas another way using the supplied mapLocation details
		LocationContentData locContentWithMatchingTypes = instance.locationContentDatas.stream().filter(x -> x.mapLocationStructures.stream().anyMatch(m ->
		m.mapLocationData.enviType == enviType
		&&
		m.mapLocationData.tileType == tileType
		&&
		m.mapLocationData.settlementType == settlementType
		&&
		m.mapLocationData.settlementDesignation == settlementDesig
		)).findFirst().orElse(null);
		
		if(locContentWithMatchingTypes == null) {
			if(sceneLayeringType != SceneLayeringType.BothLayers) {
				System.err.println("Missions.GetMapLocationStructure() - The map with the desired types doesn't exist (in the java directory or it hasn't been designed yet). Returning null.");
				return null;
			} else {
				//If we've made it this far it means that -EITHER- we have a Nature & Settlement map pairing
				MapLocationStructure natureChoice = null;
				LocationContentData[] locContentsWithMatchingNature = instance.locationContentDatas.stream().filter(x -> x.mapLocationStructures.stream().anyMatch(m ->
				m.mapLocationData.enviType == enviType
				&&
				m.mapLocationData.tileType == tileType
				&&
				m.mapLocationData.settlementType == null
				&&
				m.mapLocationData.settlementDesignation == null
				)).toArray(LocationContentData[]::new);
				if(locContentsWithMatchingNature.length == 0) {
					System.err.println("Missions.GetMapLocationStructure() - The Nature counterpart for the SceneLayeringType.BothLayers map doesn't exist. Returning null.");
					return null;
				} else {
					natureChoice = locContentsWithMatchingNature[0].mapLocationStructures.get(0);
				}
				
				MapLocationStructure settlementChoice = null;
				LocationContentData[] locContentsWithMatchingSettlement = instance.locationContentDatas.stream().filter(x -> x.mapLocationStructures.stream().anyMatch(m ->
				m.mapLocationData.enviType == null
				&&
				m.mapLocationData.tileType == null
				&&
				m.mapLocationData.settlementType == settlementType
				&&
				m.mapLocationData.settlementDesignation == settlementDesig
				)).toArray(LocationContentData[]::new);
				if(locContentsWithMatchingSettlement.length == 0) {
					System.err.println("Missions.GetMapLocationStructure() - The Settlement counterpart for the SceneLayeringType.BothLayers map doesn't exist. Returning null.");
					return null;
				} else {
					settlementChoice = locContentsWithMatchingSettlement[0].mapLocationStructures.get(0);
				}
				
				mapLocStructs.add(natureChoice);
				mapLocStructs.add(settlementChoice);
			}
		} else {
			//Now that we know the target MapLocationStructure exists inside a LocationContentData, retrieve it
			mapLocStructs.add(
				locContentWithMatchingTypes.mapLocationStructures.stream().filter(m ->
				m.mapLocationData.enviType == enviType
				&&
				m.mapLocationData.tileType == tileType
				&&
				m.mapLocationData.settlementType == settlementType
				&&
				m.mapLocationData.settlementDesignation == settlementDesig
				).findFirst().orElse(null)
			);
		}
		return mapLocStructs;
	}
	
	//This method is completely useless now that we're storing the mission structure inside of Mission
	//public static MissionStructure[] GetActiveMissionStructures(String mapLocationId) {
	/*public static MissionStructure[] GetActiveMissionStructures(MapLocation mapLocation) {
		System.err.println("Missions.GetActiveMissionStructures() - Fix this method; the way in which we retrieve the MapLocation's MapLocationStructure needs to be updated.");
		//TODO Fix this method: The lines have been blurred between a map and its original structure. its almost seeming like the structure should be captured within each MapLocation upon
		//its instantiation
		
		//This is the old way
		//MapLocationStructure locStructure = GetMapLocationStructure(mapLocationId);
		//Now the Structure is stored directly within the MapLocation
		MapLocationStructure locStructure = mapLocation.getMapLocationStructure();
		
		List<Mission> activeMissions = new ArrayList<>(Arrays.asList(GetNextAvailableMissions()));
		MissionStructure[] missionStructures = locStructure.missionStructures.stream().filter(
				x -> activeMissions.stream().anyMatch(
						a -> a.getId().equals(x.missionData.id))
			).toArray(MissionStructure[]::new);
		System.out.println("Missions.GetActiveMissionStructures() - Found active missions structures totaling to: " + missionStructures.length);
		return missionStructures;
	}*/
	
	
	public class GranularData implements Serializable {
		private static final long serialVersionUID = 7650958004451055292L;
		
		public ItemData[] customItemDatas;
		public CharacterData[] characterDatas;
	}
	private GranularData granularData;
	
	public List<CharacterData> characterDataList;
	
	public static CharacterData GetCharacterById(String characterId) {
		//Because of the way that Unity's Json serializer converts null classes to empty instances, we have to filter out blank CharacterDatas
		if(characterId == null || characterId.equals(""))
			return null;
		System.out.println("Missions.GetCharacterById() - searching for: " + characterId);
		return instance.characterDataList.stream().filter(x -> x.getId().equals(characterId)).findFirst().get();
	}
	
	public static void LoadGranularData(Missions instance, Gson gson) {
		instance.characterDataList = new ArrayList<CharacterData>();
		
		String fullPath = "resources/mapLocationScenes/GranularData.json";
		System.out.println("Missions.LoadGranularData() - Attempting to get resource at: " + fullPath);
		
		InputStream is = instance.getClass().getClassLoader().getResourceAsStream(fullPath);
		if(is == null) {
			System.err.println("File not found at: " + fullPath + " ... Dont forget to Refresh the Java project after adding new files.");
			Thread.dumpStack();
			return;
		}
		String jsonString = null;
		try {
			InputStreamReader isReader = new InputStreamReader(is);
			BufferedReader reader = new BufferedReader(isReader);
			StringBuffer sb = new StringBuffer();
			String str;
			while((str = reader.readLine())!= null){
				sb.append(str);
			}
			jsonString = sb.toString();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//Gson gson = new Gson();
		instance.granularData = gson.fromJson(jsonString, GranularData.class);
		
		//Populate list
		if(instance.granularData == null) {
			System.err.println("GranularData didn't exist or wasn't successfully loaded. The characterDataList and customItemDataList will remain empty.");
		} else {
			//Add the array into our utility list
			instance.characterDataList = new ArrayList<>(Arrays.asList(instance.granularData.characterDatas));
			System.out.println("GranularData is loaded successfully.");
		}
	}
	
	
	public class SequenceIdentifier {
		public SequenceIdentifier(String identifier, String missionId) {
			this.identifier = identifier;
			this.missionId = missionId;
		}
		public String identifier;
		public String missionId;
	}
	public static List<SequenceIdentifier> missionSequenceIdentifiers;
	
	/**
	 * Use this to foretell flexible transtions so that we know we dont need a MapLocation when building MapLocations (cause its impossible to know what MapLocation the mission will occur at)
	 */
	private static List<String> flexibleTransitionMissions;
	
	//Setup up all members by first constructing them and then by linking them to eachother
	static {
		instance = new Missions();
		
		//Misc member construction
		r = new Random();
		
		GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
		devParallel.UnitySerializationUtility.AttachUnityAdapters(gsonBuilder);
		Gson gson = gsonBuilder.create();
		
		// <- Item Setup -
		Items.Initalize();
		//boolean wereItemsInitialized = Items.Initalize();
		//System.out.println("wereItemsInitialized: " + wereItemsInitialized);
		
		//Item declaration - Option 1 : Fully code created
		//ItemData coin = new ItemData("Question.png", null, "Coin", "The currency of the land.", ItemType.Misc, 1, "T1-C", null);
		//Item declaration - Option 2 : Proxy Reference Class by Name
		//new ItemData(ItemDepot.OldBokuto) || new ItemData(ItemDepot.OldBokuto.getId(), 1)
		//Item declaration - Option 3 : Data driven search
		//ItemData[] desiredItems = Items.itemList.stream().filter(x -> x.someProperty == desiredProperty).toArray(ItemData[]::new);
		
		LoadGranularData(instance, gson);
		
		Items.ReceiveCustomItemData(instance.granularData.customItemDatas);
		
		// - Item Setup ->
		
		// <- Character Setup -
		
		//CharacterData darkEntity = new CharacterData("Dark Entity", ClassType.YOKAI, 2, 5, 4, 1, 8.0f, "portraits/Yurai.png");
		
		//Define all character architypes for use in missions
		//CharacterData playerProxy = new CharacterData("[PLAYER]", null, 0, 0, 0, 0, 0f, "");
		//CharacterData randomThug = new CharacterData("?", ClassType.BANDIT, 1, 1, 1, 2, 2f, "portraits/Thug.png");
		//CharacterData randomGuard = new CharacterData("Guard", ClassType.SURF, 2, 1, 1, 1, 5f, "portraits/Guard.png");
		//CharacterData randomAttendant = new CharacterData("Attendant", ClassType.SURF, 1, 1, 1, 1, 1f, "portraits/Attendant.png");
		
		
		
		// - Character Setup ->
		
		// <- BattleData Setup -
		
		/*BattleData battle0_darkForest = new BattleData(
			"Iron Awakening",
			new PlacementSlot[] {
				new PlacementSlot(SlotType.ForcePlayerChar, new Point(14, 9), new Point(1, 0))
			},
			null,
			new CharacterPlan[] {
				new BattleData().new CharacterPlan(darkEntity, new Point(17, 9), new Point(-1, 0))
			},
			new BattleData().new WinCondition(WinConditionType.SurviveForTime, null, null, 3),
			false
		);*/
		//System.out.println("Wincon type: " + battle0.WinCondition().WinConditionType());
		
		// - BattleData Setup ->
		
		// <- Load MapLocationDatas and MissionDatas -
		
		// <- Setup Sequence Structures -
		//These foretell missions sequencing so that they can be used when building MapLocations AND also directly when using them to do the actual sequencing
		
		//use a hardcoded dictionary to associate a mission to the Sequence Nomenclature: "m_0", "m_1_a", "m_2_a", "m_1_b", "m_2_b", "m_3", ect
		missionSequenceIdentifiers = new ArrayList<SequenceIdentifier>();
		missionSequenceIdentifiers.add(instance.new SequenceIdentifier("m_0", "e2f24e46-3a31-4bee-b82d-da60e726c85a")); 
		missionSequenceIdentifiers.add(instance.new SequenceIdentifier("m_1_a", "ac50d058-3362-462d-8037-f990c1ec0981")); 
		missionSequenceIdentifiers.add(instance.new SequenceIdentifier("m_2_a", "d625a4d4-14dd-47ea-a8b2-cfa4d4d44218")); 
		missionSequenceIdentifiers.add(instance.new SequenceIdentifier("m_1_b", "eda582b2-65c2-4cca-ac25-7b21666b83ce")); 
		missionSequenceIdentifiers.add(instance.new SequenceIdentifier("m_2_b", "62376780-e0b0-47da-921d-caa6895e5fa6")); 
		missionSequenceIdentifiers.add(instance.new SequenceIdentifier("m_3", "e1d1b419-1ed1-4426-b803-fee3390c810c"));
		
		flexibleTransitionMissions = new ArrayList<String>();
		flexibleTransitionMissions.add("m_2_b");
		
		// - Setup Sequence Structures ->
		
		//Load these in preparation for a NewGame if the users chooses to do that
		LoadLocationContentData(instance, gson);
		
		if(Game.Instance().GetWorldmapData() == null) {
			//Reload and reinstantiate everything from LocationContentData
			instance.BuildMissionsAndMapLocationsFromLocationContentData();
		} else {
			//Shouldn't need to do anything here when we're loading everything from SaveData; MapLocations and Missions
			//If the player chooses to start a new game later then the BuildMissionsAndMapLocationsFromLocationContentData() method will be called at that time
		}
		
		// - Load MapLocationDatas and MissionDatas ->
	}
	
	//Initialization - End
	
	
	List<SettlementType> designatedTypes = new ArrayList<SettlementType>(Arrays.asList(new SettlementType[] {
		SettlementType.Market,
		SettlementType.Village,
		SettlementType.Castle,
		SettlementType.MilitaryEncampment,
		SettlementType.Battle
	}));
	public List<SettlementType> GetDesignatedTypes() { return designatedTypes; }
	
	
	
	//Runtime Activities - Start
	
	/**
	 * This is meant to be called by the Game only.
	 * @param completedMission
	 */
	public static void OnGameCompleteMission(Mission completedMission) {
		System.out.println("Missions.OnGameCompleteMission() - completedMission: " + completedMission.getName());
		
		//Reset FlexibleTransition tracking
		Game.Instance().ResetLocationsVisitedSinceLastMissionCounter();
		
		//update the direct mission sequence tracker
		FindDirectMissionExtents();
		
		//Reassessment for next available missions for internal and external referencing
		ReassessNextAvailableMissions();
	}
	
	/**
	 * At the start of the game determine which direct sequencial missions are available.
	 * Note: This excludes FlexibleTransitions. Those are easily determined on the fly in GetNextAvailableMissions().
	 */
	private List<Mission> nextDirectMissions;
	
	/**
	 * Call this at the beginning of the game to populate missionBranchExtents will all currently available missions.
	 */
	public static void FindDirectMissionExtents() {
		//Initialize or re-initialize the nextDirectMissions list
		instance.nextDirectMissions = new ArrayList<Mission>();
		
		//recursively follow the single path to the end
		Mission missionSubject = null;
		if(m_0 != null)
			missionSubject = m_0;
		else //if m_0 is null it means we're loading from a save file so get the matching mission from Game with the register id for m_0
			missionSubject = getById(missionSequenceIdentifiers.stream().filter(x -> x.identifier.equals("m_0")).findFirst().get().missionId);
		
		//use our list of completedMissions to jump the direct mission gaps created by FlexibleTransitions
		List<Mission> readonlyCompletedMissions;
		List<Mission> unexaminedCompletedMissions;
		Mission[] completedMissions = Game.Instance().getMissions().stream().filter(x -> x.getMissionStatus() == MissionStatusType.Concluded).toArray(Mission[]::new);
		if(completedMissions != null && completedMissions.length > 0) {
			readonlyCompletedMissions = new ArrayList<Mission>( Arrays.asList(completedMissions) );
			unexaminedCompletedMissions = new ArrayList<Mission>( readonlyCompletedMissions );
		} else {
			readonlyCompletedMissions = new ArrayList<Mission>();
			unexaminedCompletedMissions = new ArrayList<Mission>();
		}
		
		for(Mission mission : unexaminedCompletedMissions)
			System.out.println("Missions.FindDirectMissionExtents() - (while loop) Completed Mission: " + mission.getName());
		
		List<Mission> possibleContenders = new ArrayList<Mission>();
		while(true) {
			System.out.println("Missions.FindDirectMissionExtents() - (while loop) missionSubject: " + missionSubject.getName());
			
			Mission userReachedNextMission = missionSubject.GetInteractionManager().GetUsersNextDirectMission();
			System.out.println("Missions.FindDirectMissionExtents() - (while loop) userReachedNextMission: " + (userReachedNextMission == null ? "null" : userReachedNextMission.getName()) + ", unexaminedCompletedMissions: " + unexaminedCompletedMissions.size());
			
			//Remove missionSubject if its been completed
			final String subjectId = missionSubject.getId();
			boolean wasSubjectRemovedFromCompletedMissions = unexaminedCompletedMissions.removeIf(x -> x.getId().equals(subjectId));
			
			//If theres a next mission and we've already completed it then go a level deeper
			boolean inspectACompletedMissionNext = false;
			if(userReachedNextMission != null) {
				boolean hasNextMissionBeenCompleted = unexaminedCompletedMissions.stream().anyMatch(x -> x.getId().equals(userReachedNextMission.getId()));
				if(!hasNextMissionBeenCompleted) {
					if(unexaminedCompletedMissions.size() > 0) {
						missionSubject = userReachedNextMission;
					} else {
						possibleContenders.add(userReachedNextMission);
						System.out.println("Missions.FindDirectMissionExtents() - (while loop) Adding nextMission as possibleContender: " + (userReachedNextMission == null ? "null" : userReachedNextMission.getName()));
						break;
					}
				} else {
					System.out.println("Missions.FindDirectMissionExtents() - (while loop) Ignoring completedMission: " + userReachedNextMission.getName() + ", unexaminedCompletedMissions remaining: " + unexaminedCompletedMissions.size());
					inspectACompletedMissionNext = true;
				}
			
			//Otherwise record this extent subject or FlexibleTransition-predecessor subject and if there are still completed mission branches we haven't searched then search one of those next iteration
			} else if(!wasSubjectRemovedFromCompletedMissions) { //else if(nextMission == null)
				possibleContenders.add(missionSubject);
				System.out.println("Missions.FindDirectMissionExtents() - (while loop) Adding missionSubject as possibleContender: " + (missionSubject == null ? "null" : missionSubject.getName()));
				inspectACompletedMissionNext = true;
			} else {
				System.out.println("Missions.FindDirectMissionExtents() - (while loop) nextMission is null and the subject was a completed mission. Moving on to inspect the next completedMission(if there are any left).");
				inspectACompletedMissionNext = true;
			}
			
			if(inspectACompletedMissionNext) {
				if(unexaminedCompletedMissions.size() > 0) {
					missionSubject = Missions.getById( unexaminedCompletedMissions.get(0).getId() );
					unexaminedCompletedMissions.remove(0);
					
					System.out.println("Missions.FindDirectMissionExtents() - (while loop) Preparing to examine the next unexamined completed mission: " + missionSubject.getName());
				} else {
					break;
				}
			}
		}
		
		//We do need to rely on the missionTree, which is now persistent
		for(Mission possibleContender : possibleContenders) {
			//Skip missions that havent been completed yet
			if(!readonlyCompletedMissions.contains(possibleContender))
				continue;
			//now use the missionTree to determine the next mission
			MissionNode[] nextNodes = 
			instance.missionTree.stream().filter(
				x -> x.previousMissionId != null
				&&
				x.previousMissionId.equals(possibleContender.getId())
				&&
				x.transitionType == TransitionType.Direct
			).toArray(MissionNode[]::new);
			for(MissionNode node : nextNodes) {
				Mission nextMission = getById(node.nextMissionId);
				instance.nextDirectMissions.add(nextMission);
				System.out.println("Missions.FindDirectMissionExtents() - nextDirectMission: " + nextMission.getName());
			}
		}
		
		for(int i = possibleContenders.size()-1; i > -1; i--) {
			Mission remainingContender = possibleContenders.get(i);
			if(readonlyCompletedMissions.contains(remainingContender))
				continue;
			instance.nextDirectMissions.add(0, remainingContender);
			System.out.println("Missions.FindDirectMissionExtents() - nextDirectMission: " + remainingContender);
		}
		
		//Now that we've done all that conveluded logic to determine which pending missions are up next, just simply add the missions in the Active MissionState
		Mission[] activeMissions = Game.Instance().getMissions().stream().filter(x -> x.getMissionStatus() == MissionStatusType.Active).toArray(Mission[]::new);
		for(Mission activeMission : activeMissions) {
			
			//[MISSION_FLOW_EDIT]
			//Due to the slightly different timing of Mission Completion, we need to safety check against adding duplicate missions because of unfinished mission being supplied from multiple analytical sources
			if(instance.nextDirectMissions.contains(activeMission))
				continue;
			
			instance.nextDirectMissions.add(activeMission);
			System.out.println("Missions.FindDirectMissionExtents() - adding active mission: " + activeMission);
		}
		
		
		//Set Worldmap indicators for active mission if their properties dictate it
		if(GUIManager.WorldmapPanel() != null) {
			for(Mission nextDirectMission : instance.nextDirectMissions)
				GUIManager.WorldmapPanel().HandleMissionIndicator(nextDirectMission);
		}
	}
	
	/**
	 * This represents all the possible mission paths arranged in a node graph. Its quite simple yet effective with the only transitions being direct or flexible sequencing.
	 * A Direct Transition is one that either happens one mission after another:
	 *   1. in which the player fast-travels to the next mission location as the previous mission concludes
	 *   2. the mission location was planned and plotted during world generation.
	 *   *The sequencing of this transition type is such that once a mission is complete the next mission or missions become available at their predetermined locations and occur at the moment 
	 *    the player enter's their location. (The exception being the coincidence of multiple active mission at the same location at the same time, at which time missions will occur one at
	 *    a time until in a non-arbitrary order everytime the player "Enters" the location from the worldmap)
	 * A Flexible Transition is one that is not predefined but occurs conditionally at a suitable location.
	 *  *The suitability of which is determined by the genericLocationEnvironmentType of the mission.
	 *  *The conditionality of the occurance is defined by the properties of the governing FlexibleTransition.
	 * @author Magnus
	 *
	 */
	/*public class MissionNode {
		public MissionNode(Mission previousMission, TransitionType transitionType, Mission nextMission) {
			this.previousMission = previousMission;
			this.transitionType = transitionType;
			this.nextMission = nextMission;
		}
		public Mission previousMission;
		public TransitionType transitionType;
		public Mission nextMission;
	}*/
	private List<MissionNode> missionTree;
	
	
	//Will be called by WorldmapPanel at every game launch and every time we enter a MapLocation
	private Mission[] availableMissions;
	public static Mission[] GetNextAvailableMissions() {
		if(instance.availableMissions == null)
			instance.availableMissions = DetermineNextAvailableMissions();
		
		return instance.availableMissions;
	}
	
	//public static void ClearNextAvailableMissions() {
	//	instance.availableMissions = null;
	//}
	
	public static void ReassessNextAvailableMissions() {
		instance.availableMissions = DetermineNextAvailableMissions();
	}
	
	private static Mission[] DetermineNextAvailableMissions() {
		Mission[] completedMissionsArray = Game.Instance().getMissions().stream().filter(x -> x.getMissionStatus() == MissionStatusType.Concluded).toArray(Mission[]::new);
		
		if(completedMissionsArray == null || (completedMissionsArray != null && completedMissionsArray.length == 0)) {
			System.out.println("Missions.DetermineNextAvailableMissions() - there are no completed missions. Returning first mission.");
			Mission firstMission = m_0 != null ? m_0 : getById(missionSequenceIdentifiers.stream().filter(x -> x.identifier.equals("m_0")).findFirst().get().missionId);
			return new Mission[] { firstMission };
		}
		
		//Determine which FlexibleTransitions are available
		List<Mission> completedMissions = new ArrayList<Mission>( Arrays.asList(completedMissionsArray) );
		System.out.println("Missions.DetermineNextAvailableMissions() - completedMissions length: " + completedMissions.size());
		List<FlexibleTransition> readyTransitions = new ArrayList<FlexibleTransition>();
		List<Mission> availableMissions = new ArrayList<Mission>();
		for(Mission mission : completedMissions) {
			FlexibleTransition trans = instance.flexibleTransitions.stream().filter(t -> t.PrereqMissionId().equals(mission.getId()) && getById(t.NextMissionId()).getMissionStatus() == MissionStatusType.Pending)
				.findFirst().orElse(null);
			if(trans != null)
				readyTransitions.add(trans);
		}
		System.out.println("Missions.DetermineNextAvailableMissions() - Found potential FlexibleTransitions: " + readyTransitions.size());
		//This only find the next available FlexibleTransition missions, not the direct decending missions
		int maxMissions = 3;
		for(int i = 0; i < readyTransitions.size(); i++) {
			boolean isAvailable = false;
			if(Game.Instance().GetLocationsVisitedSinceLastMissionCounter() >= readyTransitions.get(i).MinimumLocationsVisited())
				isAvailable = ((float)r.nextInt(100) / 99) < readyTransitions.get(i).ChanceToOccur();
			
			if(isAvailable) {
				//availableMissions.add(readyTransitions.get(i).NextMission());
				final int finalI = i;
				Optional<Mission> missionOp = Game.Instance().getMissions().stream().filter(x -> x.getId().equals(readyTransitions.get(finalI).NextMissionId())).findFirst();
				availableMissions.add(missionOp.get());
			}
			
			if(availableMissions.size() == maxMissions)
				break;
		}
		
		//Get the currently available direct sequential missions
		availableMissions.addAll(instance.nextDirectMissions);
		
		System.out.println("Missions.DetermineNextAvailableMissions() - availableMissions.length: " + availableMissions.size());
		
		return availableMissions.stream().toArray(Mission[]::new);
	}
	
	public static Mission getById(String missionId) {
		Optional<Mission> possibleMission = Game.Instance().getMissions().stream().filter(x -> x.getId().equals(missionId)).findFirst();
		if(possibleMission.isPresent())
			return possibleMission.get();
		else {
			System.err.println("Missions.getById() - Couldn't find mission with id: " + missionId);
			return null;
		}
	}
	
	public static Mission[] getAllByIds(String[] missionIds) {
		List<String> missionIdList = Arrays.asList(missionIds);
		Mission[] matchingMissions = Game.Instance().getMissions().stream().filter(x -> missionIdList.contains(x.getId())).toArray(Mission[]::new);
		if(matchingMissions.length > 0)
			return matchingMissions;
		else {
			System.err.println("Missions.getById() - Couldn't find any missions with ids array.");
			return null;
		}
	}
	
	//Runtime Activities - End
	
	
	//static {
		//Hardcoded Instantiation (OUTDATED)
	
		//Main Missions - Start
		
		//Overwrite m_0 interaction with test interactions
		/*CharacterData testChar1 = CharacterData.CreateRandom();
		Interaction[] testInteractions = new Interaction[] {
			new Interaction(
				//Type
				InteractionType.Camp,
				//Dialog
				new DialogLine[] {
					new DialogLine(playerProxy, true, "You find a semi-obscured area in the brush. This will make for a good campground.")
				},
				//Granted Items
				new ItemData[] {
				},
				//Is Revealed?
				true,
				//Does Block Map Movement?
				false,
				//Test Type
				Interaction.TestType.None,
				//testStat
				null,
				//passingStatValue
				0,
				//Chance To Occur
				1f,
				//requiredItem
				null,
				//requiredClass
				null,
				//Battle Data
				null,
				//-Interaction Graph-
				//isPersistentIntr
				true,
				//isUniqueTreeIntr
				false,
				//virginRootEnduresReturnToBase
				false,
				//instanceRefreshTimer_days
				1,
				//cancelPersistentIntrTypes (InteractionType[])
				null,
				//Resulting Dialog _OnSuccess
				null,
				//Result Summary_onSuccess
				null,
				//Next Interactions _OnSuccess
				null,
				//Resulting Dialog _OnFailure,
				null,
				//Result Summary_onFailure
				null,
				//Next Interactions _OnFailure,
				null
			),
			new Interaction(
				//Type
				InteractionType.Explore,
				//Dialog
				new DialogLine[] {
					new DialogLine(playerProxy, true, "You begin exploring the area.")
				},
				//Granted Items
				null,
				//Is Revealed?
				true,
				//Does Block Map Movement?
				false,
				//Test Type
				Interaction.TestType.Random,
				//testStat
				null,
				//passingStatValue
				0,
				//Chance To Occur
				1f,
				//requiredItem
				null,
				//requiredClass
				null,
				//Battle Data
				null,
				//-Interaction Graph-
				//isPersistentIntr
				false,
				//isUniqueTreeIntr
				false,
				//virginRootEnduresReturnToBase
				false,
				//instanceRefreshTimer_days
				0,
				//cancelPersistentIntrTypes
				null,
				//Resulting Dialog _OnSuccess
				new DialogLine[] {
					new DialogLine(playerProxy, true, "You notice a merchant standing on a roadside nearby.")
				},
				//Result Summary_onSuccess
				"You notice a nearby merchant and begin approaching them.",
				//Next Interactions _OnSuccess
				new Interaction[] {
					new Interaction(
						//Type
						InteractionType.Talk,
						//Dialog
						new DialogLine[] {
							new DialogLine(testChar1, false, "\"Greetings, traveler.\""),
							new DialogLine(playerProxy, true, "..."),
							new DialogLine(testChar1, false, "\"If you don't want to trade then what do you want? Are you lost?\""),
							new DialogLine(playerProxy, true, "\"Do you know the way to San Hose?\""),
							new DialogLine(testChar1, false, "\"I do in fact, just came from that direction.\""),
						},
						//Granted Items
						new ItemData[] {
						},
						//Is Revealed?
						false,
						//Does Block Map Movement?
						false,
						//Test Type
						Interaction.TestType.None,
						//testStat
						null,
						//passingStatValue
						0,
						//Chance To Occur
						1f,
						//requiredItem
						null,
						//requiredClass
						null,
						//Battle Data
						null,
						//-Interaction Graph-
						//isPersistentIntr
						false,
						//isUniqueTreeIntr
						false,
						//virginRootEnduresReturnToBase
						false,
						//instanceRefreshTimer_days
						0,
						//cancelPersistentIntrTypes (InteractionType[])
						null,
						//Resulting Dialog _OnSuccess
						null,
						//Result Summary_onSuccess
						"The merchant told you the way to [MapLocation.Name].",
						//Next Interactions _OnSuccess
						new Interaction[] {
							new Interaction(
								//Type
								InteractionType.Travel,
								//Dialog
								new DialogLine[] {
									new DialogLine(playerProxy, true, "\"Thank ya kindly. I'll be on my way then.\""),
								},
								//Granted Items
								new ItemData[] {
								},
								//Is Revealed?
								false,
								//Does Block Map Movement?
								false,
								//Test Type
								Interaction.TestType.None,
								//testStat
								null,
								//passingStatValue
								0,
								//Chance To Occur
								1f,
								//requiredItem
								null,
								//requiredClass
								null,
								//Battle Data
								null,
								//-Interaction Graph-
								//isPersistentIntr
								false,
								//isUniqueTreeIntr
								false,
								//virginRootEnduresReturnToBase
								false,
								//instanceRefreshTimer_days
								0,
								//cancelPersistentIntrTypes (InteractionType[])
								null,
								//Resulting Dialog _OnSuccess
								null,
								//Result Summary_onSuccess
								null,
								//Next Interactions _OnSuccess
								null,
								//Resulting Dialog _OnFailure,
								null,
								//Result Summary_onFailure
								null,
								//Next Interactions _OnFailure,
								null
							).WithGotoLocation(kogaKeep),
						},
						//Resulting Dialog _OnFailure,
						null,
						//Result Summary_onFailure
						null,
						//Next Interactions _OnFailure,
						null
					),
					new Interaction(
						//Type
						InteractionType.Trade,
						//Dialog
						new DialogLine[] {
							new DialogLine(testChar1, false, "Take a look at my wares."),
							new DialogLine(playerProxy, true, "Will do!")
						},
						//Granted Items
						new ItemData[] {
						},
						//Is Revealed?
						false,
						//Does Block Map Movement?
						false,
						//Test Type
						Interaction.TestType.None,
						//testStat
						null,
						//passingStatValue
						0,
						//Chance To Occur
						1f,
						//requiredItem
						null,
						//requiredClass
						null,
						//Battle Data
						null,
						//-Interaction Graph-
						//isPersistentIntr
						true,
						//isUniqueTreeIntr
						false,
						//virginRootEnduresReturnToBase
						false,
						//instanceRefreshTimer_days
						0,
						//cancelPersistentIntrTypes (InteractionType[])
						null,
						//Resulting Dialog _OnSuccess
						null,
						//Result Summary_onSuccess
						null,
						//Next Interactions _OnSuccess
						null,
						//Resulting Dialog _OnFailure,
						null,
						//Result Summary_onFailure
						null,
						//Next Interactions _OnFailure,
						null
					),
					new Interaction(
						//Type
						InteractionType.Steal,
						//Dialog
						new DialogLine[] {
							new DialogLine(testChar1, false, testChar1.getName() + " rifles through one of his travel bags at the edge of the path. "
									+ "Every so often they glance around, checking there surroundings."),
							new DialogLine(playerProxy, true, "As you approach you decide to attempt to steal from one of their unatttended bags while passing by.")
						},
						//Granted Items
						new ItemData[] {
							ItemData.CreateRandom(null)
						},
						//Is Revealed?
						false,
						//Does Block Map Movement?
						false,
						//Test Type
						Interaction.TestType.StatTest,
						//testStat
						StatType.SPEED,
						//passingStatValue
						3,
						//Chance To Occur
						1f,
						//requiredItem
						null,
						//requiredClass
						null,
						//Battle Data
						null,
						//-Interaction Graph-
						//isPersistentIntr
						false,
						//isUniqueTreeIntr
						false,
						//virginRootEnduresReturnToBase
						false,
						//instanceRefreshTimer_days
						0,
						//cancelPersistentIntrTypes (InteractionType[])
						new InteractionType[] { InteractionType.Camp, InteractionType.Trade },
						//Resulting Dialog _OnSuccess
						new DialogLine[] {
							new DialogLine(playerProxy, true, "Your nimble hand finds purchase as you pilfer an item from their bag and continue walking.")
						},
						//Result Summary_onSuccess
						"You stole an item from the merchant without them noticing.",
						//Next Interactions _OnSuccess
						null,
						//Resulting Dialog _OnFailure,
						new DialogLine[] {
							new DialogLine(testChar1, false, "\"Okay. Really? You're trying to steal from me in such a blatant manner?!\""),
							new DialogLine(playerProxy, true, "\"NO!...I'm not defensive!\"")
						},
						//Result Summary_onFailure
						"The merchant caught you stealing. Now you must face them or escape to avoid the local authorities!",
						//Next Interactions _OnFailure,
						new Interaction[] {
							new Interaction(
								//Type
								InteractionType.Fight,
								//Dialog
								new DialogLine[] {
									new DialogLine(playerProxy, true, "\"The dead tell no tales.\""),
									new DialogLine(testChar1, false, "\"You scoundrel!\""),
								},
								//Granted Items
								new ItemData[] {
								},
								//Is Revealed?
								false,
								//Does Block Map Movement?
								true,
								//Test Type
								Interaction.TestType.None,
								//testStat
								null,
								//passingStatValue
								0,
								//Chance To Occur
								1f,
								//requiredItem
								null,
								//requiredClass
								null,
								//Battle Data
								battle0_darkForest,
								//-Interaction Graph-
								//isPersistentIntr
								false,
								//isUniqueTreeIntr
								false,
								//virginRootEnduresReturnToBase
								false,
								//instanceRefreshTimer_days
								0,
								//cancelPersistentIntrTypes (InteractionType[])
								null,
								//Resulting Dialog _OnSuccess
								new DialogLine[] {
									new DialogLine(testChar1, false, "The dead merchant and their wares lay strewn across the road way.")
								},
								//Result Summary _onSuccess
								"You've dispatched the poor innocent merchant. Their wares lay strewn across the road way.",
								//Next Interactions _OnSuccess
								new Interaction[] {
									new Interaction(
										//Type
										InteractionType.Search,
										//Dialog
										new DialogLine[] {
											new DialogLine(playerProxy, true, "You quickly sift through the wares looking for the most valuable pieces.")
										},
										//Granted Items
										new ItemData[] {
											ItemData.CreateRandom(null),
											ItemData.CreateRandom(null),
											ItemData.CreateRandom(null),
										},
										//Is Revealed?
										false,
										//Does Block Map Movement?
										false,
										//Test Type
										Interaction.TestType.None,
										//testStat
										null,
										//passingStatValue
										0,
										//Chance To Occur
										1f,
										//requiredItem
										null,
										//requiredClass
										null,
										//Battle Data
										null,
										//-Interaction Graph-
										//isPersistentIntr
										false,
										//isUniqueTreeIntr
										false,
										//virginRootEnduresReturnToBase
										false,
										//instanceRefreshTimer_days
										0,
										//cancelPersistentIntrTypes (InteractionType[])
										null,
										//Resulting Dialog _OnSuccess
										null,
										//Result Summary _onSuccess
										"You took an item from the merchant's things.",
										//Next Interactions _OnSuccess
										null,
										//Resulting Dialog _OnFailure,
										null,
										//Result Summary _onFailure
										null,
										//Next Interactions _OnFailure,
										null
									),
								},
								//Resulting Dialog _OnFailure,
								null,
								//Result Summary _onFailure
								null,
								//Next Interactions _OnFailure,
								null
							),
							new Interaction(
								//Type
								InteractionType.Flee,
								//Dialog
								new DialogLine[] {
									new DialogLine(playerProxy, true, "\"Try to catch me!\""),
									new DialogLine(testChar1, false, "\"Get back here, thief!\""),
								},
								//Granted Items
								new ItemData[] {
								},
								//Is Revealed?
								false,
								//Does Block Map Movement?
								false,
								//Test Type
								Interaction.TestType.ClassPossession,
								//testStat
								null,
								//passingStatValue
								0,
								//Chance To Occur
								1f,
								//requiredItem
								null,
								//requiredClass
								ClassType.BANDIT,
								//Battle Data
								null,
								//-Interaction Graph-
								//isPersistentIntr
								false,
								//isUniqueTreeIntr
								false,
								//virginRootEnduresReturnToBase
								false,
								//instanceRefreshTimer_days
								0,
								//cancelPersistentIntrTypes (InteractionType[])
								null,
								//Resulting Dialog _OnSuccess
								new DialogLine[] {
									new DialogLine(playerProxy, true, "You bolt away from the road and disappear into the wilds. You're experience as a bandit helps you escape this ordeal.")
								},
								//Result Summary _onSuccess
								null,
								//Next Interactions _OnSuccess
								null,
								//Resulting Dialog _OnFailure,
								new DialogLine[] {
									new DialogLine(playerProxy, true, "A rock trips you as you run and you land face first in the dirt."),
									new DialogLine(testChar1, false, "\"A samurai will take your hands for this crime!\" They shout as they hurriedly gather their wares."),
									new DialogLine(testChar1, false, "The merchant makes a rude gesture at you and proceeds to run away."),
								},
								//Result Summary _onFailure
								"You failed to escape the merchant in time; allowing them to flee to the local authorities and notify them of your crime.",
								//Next Interactions _OnFailure,
								null
							),
						}
					),
				},
				//Resulting Dialog _OnFailure,
				new DialogLine[] {
					new DialogLine(playerProxy, true, "You find nothing of interest."),
				},
				//Result Summary_onFailure
				"You didn't find anything of interest.",
				//Next Interactions _OnFailure,
				null
			)
		};
		
		
		m_0 = new Mission(
			//Id
			"e2f24e46-3a31-4bee-b82d-da60e726c85a",
			//Mission Name
			"Sleeping Fury",
			//Mission Description
			"An encounter with a mysterious being. You must carry on through this unfamilier land in search of answers.",
			new MapLocation(
				EnvironmentType.Farmland,
				WorldTileType.farmland,
				null,
				null,
				"Dark Forest",
				"You awaken suddenly, instinctively springing to your feet. As you frantically glance around your dark surrounds, you make out a "
					+ "figure lingering across the overgrown clearing. It stands as still as the surrounding trees yet you can sense it watching you. "
					+ "What will you do now?",
				"DarkForest/",
				//"Farmland/",
				//"mapLocationBG/bg_forest-forest.png",
				null,
				new Interaction[] {
					new Interaction(
						//Type
						InteractionType.Fight,
						//Dialog
						new DialogLine[] {
							new DialogLine(playerProxy, true,
									"You charge at the ominous figure. Your hand naturally reaches for a sword at your hip and finds purchase as you draw forth a black blade."),
							new DialogLine(playerProxy, true,
									"The dim light seeping through the surrounding forest dances strangely along the edge of the blade."),
							new DialogLine(darkEntity, false,
									"The figure shifts at the sight of your weapon, preparing itself for your impending attack."),
							new DialogLine(playerProxy, true,
									"You're nearly in range. You raise the sword above your head and utter a fierce warcry."),
						},
						//Granted Items
						new ItemData[] {
						},
						//Is Revealed?
						true,
						//Does Block Map Movement?
						true,
						//Test Type
						Interaction.TestType.None,
						//testStat
						null,
						//passingStatValue
						0,
						//Chance To Occur
						1f,
						//requiredItem
						null,
						//requiredClass
						null,
						//Battle Data
						battle0_darkForest,
						//-Interaction Graph-
						//isPersistentIntr
						false,
						//isUniqueTreeIntr
						false,
						//virginRootEnduresReturnToBase
						false,
						//instanceRefreshTimer_days
						0,
						//cancelPersistentIntrTypes (InteractionType[])
						null,
						//Resulting Dialog _OnSuccess
						new DialogLine[] {
							new DialogLine(darkEntity, false,
									"The dark entity jumps back and relaxes its stance."),
							new DialogLine(darkEntity, false,
									"It begins to speak in a harsh whisper. \"Well, I can see there's no need to test your mettle.\""),
							new DialogLine(darkEntity, false,
									"\"You'll do nicely.\" It's mouth curling into something like an unnervingly wide smile."),
							new DialogLine(playerProxy, true,
									"\"Do nicely for what, fiend?\" you shout. Its mocking demeanor angering you."),
							new DialogLine(darkEntity, false,
									"It chuckles, turns around and begins walking toward a trail leading out of the clearing."),
							new DialogLine(darkEntity, false,
									"It pauses and looks back at you. \"Well?\" it mutters in an unamused tone of voice. \"Are you coming or not?\""),
							new DialogLine(playerProxy, true,
									"Standing there, dumbfonded, you stare at it. It truely didn't seem interested in fighting anymore. You cast a few cautious glaces around the area but there doesn't seem to be any indication of treachery."),
							new DialogLine(darkEntity, false,
									"It nods and proceeds down the trail."),
							new DialogLine(playerProxy, true,
									"You loosen your grip on the sword and take a deep breath. A grubble escaping your lips as you bitterly decide it's better to keep this creature in sight then to let it vanish into the dark forest. You follow after it."),
						},
						//Result Summary _onSuccess
						null,
						//Next Interactions _OnSuccess
						null,
						//Resulting Dialog _OnFailure,
						new DialogLine[] {
							new DialogLine(playerProxy, true,
									"Squinting up at the forest canopy you struggle to breath with the pain searing through your body."),
							new DialogLine(playerProxy, true,
									"This dark being had mortally wounded you in combat. You're eyes widen as you feel the blood leaving your body and sense yourself slipping away."),
							new DialogLine(playerProxy, true,
									"As your vision fades, you begin to notice bright lights forming above you. They slowly float down from above and surround you."),
							new DialogLine(playerProxy, true,
									"A bolt of energy shoots through your body. You sit up coughing and sputtering on this newfound breath in your lungs."),
							new DialogLine(playerProxy, true,
									"The lights shrink and disappear as you sit hunched over, attempting to get your bearings."),
							new DialogLine(playerProxy, true,
									"You look down at your wounds but they're no longer there. Dried blood and torn cloth are the only indication of any wounds existing there."),
							new DialogLine(playerProxy, true,
									"You glance up at the dark being who had struck you down only minutes ago. \"How?\""),
							new DialogLine(darkEntity, false,
									"It stood there, staring back at you with its hands causally held at its back."),
							new DialogLine(darkEntity, false,
									"\"Only few know of such ancient mysteries. What is know is your special connection with this place.\""),
							new DialogLine(darkEntity, false,
									"\"If you perish somewhere out there, beyond this forest, you will awaken here.\""),
							new DialogLine(playerProxy, true,
									"\"What is happening here? Why am I bound to this dark forest?\""),
							new DialogLine(darkEntity, false,
									"\"Come. I have more to show you.\" It said dismissively while walking away."),
							new DialogLine(playerProxy, true,
									"Climbing to your feet you dust yourself off. Stupefied by the strange event, you stumble after it."),
						},
						//Result Summary _onFailure
						null,
						//Next Interactions _OnFailure,
						null
					),
					new Interaction(
						//Type
						InteractionType.Flee,
						//Dialog
						new DialogLine[] {
							new DialogLine(playerProxy, true,
									"You turn and run as fast as you can. The threating aura of that figure still lingering in your mind's eye."),
							new DialogLine(playerProxy, true,
									"Fear twists up your spine as you hear something rapidly stomping after you."),
						},
						//Granted Items
						null,
						//Is Revealed?
						true,
						//Does Block Map Movement?
						false,
						//Test Type
						Interaction.TestType.None,
						//testStat
						StatType.values()[0],
						//passingStatValue
						0,
						//Chance To Occur
						1f,
						//requiredItem
						null,
						//requiredClass
						ClassType.values()[0],
						//Battle Data
						null,
						//-Interaction Graph-
						//isPersistentIntr
						false,
						//isUniqueTreeIntr
						false,
						//virginRootEnduresReturnToBase
						false,
						//instanceRefreshTimer_days
						0,
						//cancelPersistentIntrTypes (InteractionType[])
						null,
						//Resulting Dialog _On Success
						null,
						//Result Summary _onSuccess
						null,
						//Next Interactions _OnSuccess
						null,
						//resultingDialog_onFailure,
						null,
						//Result Summary _onFailure
						null,
						//nextInteractions_onFailure,
						null
					),
				}
				//testInteractions
			),
			//Mission Interaction Requirements (InteractionType[])
			null,
			//Mission Rewards (ItemData[])
			//new ItemData[] { coin, bearPelt }
			new ItemData[] { coin, new ItemData(ItemDepot.OldBokuto.getId(), 1) }
		);
		ItemData blackBlade = 
		new ItemData(
			"Question.png",
			"58bd9b1d-ad67-4917-84b3-761f9bfb8a18", 
			"Black Blade", 
			"A mysterious sword. It shimmers like obsidian but with a surface uniform as steel.", 
			ItemType.Weapon,
			new Stats(
				EquipmentType.RightHand, 
				WeightType.Medium, 
				KarmaType.Cursed, 
				new ClassType[]{ ClassType.BANDIT, ClassType.MONK, ClassType.NINJA, ClassType.PRIEST, ClassType.RONIN }, 
				2,
				2,
				0,
				0, 
				null,
				new BattleToolTraits(
					1, 
					1, 
					0, 
					new WeaponTraits(WeaponGroup.Edged, WeaponType.Kodachi, false, false), 
					null
				)
			),
			1,
			null,
			m_0.getId()	
		);
		//m_0.AddGrantedItemsForInteraction(InteractionType.Fight, new ItemData[] { blackBlade });
		
		
		m_1_a = new Mission(
			"50ea3bcf-4f00-484b-bc94-c555a0a066e0",
			//Mission Name
			"Forged in Fire",
			//Mission Description
			"After battling the mysterous being for a time, it called for a truse and led you to a cliff overlooking a large city. "
				+ "It spoke of wicked ambitions and of cryptic destinies. And then without warning, it cast you off the cliff face."
				+ "You landed in a body of water far below, leaving you to your own devices.",
			new MapLocation(
				EnvironmentType.Mountainous,
				WorldTileType.peak,
				null,
				null,
				//Location Name
				"Cliffside Outlook",
				//Description
				"The dark figure leads you out to the edge of a cliffside.",
				//Scene Directory Name + /
				"Peak/",
				//BG Image Address
				"mapLocationBG/Cliffside.png",
				//All Possible Interactions
				new Interaction[] {
					new Interaction(
						//Type
						InteractionType.Talk,
						//Dialog
						new DialogLine[] {
							new DialogLine(darkEntity, false,
								"Do you see that city in the distance there?"),
							new DialogLine(darkEntity, false,
								"You are akin to that place, embedded and glowing throughout the night of this great woodland."),
							new DialogLine(darkEntity, false,
								"It is your right to grow stronger and seize your true destiny, like the city consuming the wilds to build itself higher."),
							new DialogLine(darkEntity, false,
								"Take that thirsty gift of war and use it to cleave such a path through this land that all will know your reign."),
							new DialogLine(darkEntity, false,
								"The figure steps back and, as you turn to face them, they swiftly kick you off the cliff's edge!!!"),
						},
						//Granted Items
						null,
						//Is Revealed?
						true,
						//Does Block Map Movement?
						true,
						//Test Type
						Interaction.TestType.None,
						//testStat
						StatType.values()[0],
						//passingStatValue
						0,
						//Chance To Occur
						1f,
						//requiredItem
						null,
						//requiredClass
						ClassType.values()[0],
						//Battle Data
						null,
						//-Interaction Graph-
						//isPersistentIntr
						false,
						//isUniqueTreeIntr
						false,
						//virginRootEnduresReturnToBase
						false,
						//instanceRefreshTimer_days
						0,
						//cancelPersistentIntrTypes (InteractionType[])
						null,
						//Resulting Dialog _On Success
						null,
						//Result Summary _onSuccess
						null,
						//Next Interactions _OnSuccess
						null,
						//Resulting Dialog _OnFailure,
						null,
						//Result Summary _onFailure
						null,
						//Next Interactions _OnFailure,
						null
					),
				}
			),
			//Mission Interaction Requirements (InteractionType[])
			null,
			//Rewards (ItemData[])
			null
		);
		
		m_2_a = new Mission(
			"23be54fe-0fdd-4934-8c32-70437e16ce34",
			"Waterlogged",
			"You wake in a start; coughing water from your lungs."
				+ "You push yourself up from the shallows of a shore."
				+ "The stupper wears off quicking as the anger whells up inside you."
				+ "\"Who was that entity and why did it try to dispatch me in such an ill manor?\""
				+ "You swear to get your revenge on that pitiless creature if the opportunity every presents itself.",
			EnvironmentType.Water,
			//Mission Interaction Requirements (InteractionType[])
			null,
			//Rewards (ItemData[])
			null
		);
		
		m_1_b = new Mission(
			"1c8f8375-03de-4b53-99a9-0c192a391297",
			//Mission Name
			"Panicked Escape",
			//Mission Description
			"Having run for your life through the wilderness you begin to wonder where you are and how you'll be able to find food and shelter.",
			new MapLocation(
				EnvironmentType.Grassland,
				WorldTileType.field,
				null,
				null,
				//Location Name
				"Quiet Valley",
				//Location Description
				"Collapsing to your knees you gasp for breath. Having seemingly outrun whatever was making that horrible stomping noise, "
					+ "you take a moment to recover and attempt to find a bearing in this massive forest. As far as you can tell, you're heading "
					+ "downhill but there aren't any apparent landmarks in sight.",
				//Scene Directory Name + /
				"Field2/",
				//bgImagePath
				"mapLocationBG/abstractBG_forest-forest.png",
				new Interaction[] {
				}
			),
			//Mission Interaction Requirements (InteractionType[])
			null,
			//Rewards (ItemData[])
			null
		);
		
		m_2_b = new Mission(
			"e7d44f6a-f818-4d46-a2d9-c931db0398d7",
			"Resounding Aura",
			"As you make your way down the mountainside you begin to sense curious aura eminating from somewhere nearby.",
			EnvironmentType.Forest,
			new MapLocation(
				EnvironmentType.Forest,
				WorldTileType.forestEdge,
				null,
				null,
				"Ancient Grove",
				"Those who roam this land may be richer than the Shogun. Some hearts are drawn to the solace of a journey through quite forests and high peaks."
					+ "To those wandering souls they say: May you all find your true paths among the many roads.",
				//Scene Directory Name + /
				"",
				"mapLocationBG/AncientTree.png",
				new Interaction[] {
					//have interactions leading to the appearance of the Daddy Woodpecker
				}
			),
			//Mission Interaction Requirements (InteractionType[])
			new InteractionType[] { InteractionType.Talk },
			//Rewards (ItemData[])
			new ItemData[] {
			}
		);
		ItemData arborSigil = new ItemData("Question.png", "335a6e9e-52b2-4cb2-a9df-e7be71d66dd0", "Arbor Sigil", "This large pendant carved from wood glows with life.", ItemType.Armor,
				new Stats(EquipmentType.Accessory, WeightType.Light, KarmaType.Divine, new ClassType[]{ ClassType.BANDIT, ClassType.MONK, ClassType.NINJA, ClassType.PRIEST, ClassType.RONIN }, 0,0,2,2, null, null), 1, null, m_2_b.toString());
		m_2_b.AddRewardsItems(new ItemData[] { arborSigil });
		
		
		m_3 = new Mission(
			"81ac64ea-580c-4e63-9733-17933dc89ff3",
			//Mission Name
			"Fate of the Outcaste",
			//Mission Description
			"Travel to the city shown to you by the dark entity while atop the cliffside lookout.",
			//GenericLocationType
			new MapLocation(
				//EnvironmentType
				null,
				//WorldTileType
				null,
				//SettlementType
				SettlementType.Village,
				SettlementDesignation.Small,
				//Location Name
				"Sota City",
				//Location Description
				"This is the village mentioned by dark entity. It's many streets and bustling crowds stretch out before you as you stand on the outskirts.",
				//Scene Directory Name + / ("DarkForest/")
				"",
				//bgImagePath
				null,
				new Interaction[] {
					new Interaction(
						//Type
						InteractionType.Explore,
						//Dialog
						new DialogLine[] {
							new DialogLine(playerProxy, true,
								"You walk onto the main street of Sota Village. You begin to notice people along the street glaring distainfully at you as you make your way."),
							new DialogLine(null, true,
								"A humongous cart barely misses you as it rolls swiftly past and kicks up large chunks of mud at you as it does."),
							new DialogLine(playerProxy, true,
								"\"Fools!?\" You curse at the drivers in surprise; flinching from the cold sensation of the mud absorbing into your cloths."),
							new DialogLine(playerProxy, true,
								"Brushing off the mess you realize that your cloths and skin were already filthy."),
							new DialogLine(playerProxy, true,
								"You look around to see if anyone had witnessed this rude exchange. No one seems to care."),
							new DialogLine(randomThug, false,
								"Then you notice two men down the way. They brandish clubs as they mumble to one another and begin walking toward you."),
						},
						//Granted Items
						new ItemData[] {
						},
						//Is Revealed?
						true,
						//Does Block Map Movement?
						true,
						//Test Type
						Interaction.TestType.None,
						//testStat
						StatType.values()[0],
						//passingStatValue
						0,
						//Chance To Occur
						1f,
						//requiredItem
						null,
						//requiredClass
						ClassType.values()[0],
						//Battle Data
						null,
						//-Interaction Graph-
						//isPersistentIntr
						false,
						//isUniqueTreeIntr
						false,
						//virginRootEnduresReturnToBase
						false,
						//instanceRefreshTimer_days
						0,
						//cancelPersistentIntrTypes (InteractionType[])
						null,
						//Resulting Dialog _OnSuccess
						null,
						//Result Summary _onSuccess
						null,
						//Next Interactions _OnSuccess
						null,
						//Resulting Dialog _OnFailure,
						null,
						//Result Summary _onSuccess
						null,
						//Next Interactions _OnFailure,
						null
					),
				}
			),
			//Mission Interaction Requirements (InteractionType[])
			null,
			//Rewards (ItemData[])
			null
		);
		
		//Main Missions - End
		
		//Side Missions - Start
		
		s_1 = new Mission(
			"ca4ae30f-200f-43e4-9f55-991767172a05",
			"Pelts Needed",
			"A local contract for 5 bear pelts.",
			new MapLocation(
				EnvironmentType.Farmland,
				WorldTileType.farmland,
				SettlementType.Estate,
				null,
				"Shutenzo Estate",
				"A large grounds with a modest Manor and adjoining temple. The estate being tucked into a dense forest valley made it's appeal all the more wholesome.",
				//Scene Directory Name + /
				"",
				"mapLocationBG/Estate.png",
				new Interaction[] {
					//Create Talk interaction that requires the bear pelt items.
					new Interaction(
						//Type
						InteractionType.Talk,
						//Dialog
						new DialogLine[] {
							new DialogLine(playerProxy, true,
								"You approach the gate and pause outside its doors."),
							new DialogLine(randomGuard, false,
								"A door swings open and two guards lumber out."),
							new DialogLine(randomGuard, false,
								"\"Whats your business here?\""),
						},
						//Granted Items
						new ItemData[] {
						},
						//Is Revealed?
						true,
						//Does Block Map Movement?
						false,
						//Test Type
						Interaction.TestType.ItemPossession,
						//testStat
						StatType.values()[0],
						//passingStatValue
						0,
						//Chance To Occur
						1f,
						//requiredItems
						new ItemData[] {
							new ItemData(ItemDepot.OldBokuto.getId(), 5),
						},
						//requiredClass
						ClassType.values()[0],
						//Battle Data
						null,
						//-Interaction Graph-
						//isPersistentIntr
						false,
						//isUniqueTreeIntr
						false,
						//virginRootEnduresReturnToBase
						false,
						//instanceRefreshTimer_days
						0,
						//cancelPersistentIntrTypes (InteractionType[])
						null,
						//Resulting Dialog _OnSuccess
						new DialogLine[] {
							new DialogLine(playerProxy, true,
								"You pull the bear pelts off your back and hold them out."),
							new DialogLine(randomGuard, false,
								"The first guard nods. \"One moment.\""),
							new DialogLine(randomGuard, false,
								"The remaining guard glares at you as you both wait."),
							new DialogLine(playerProxy, true,
								"You begin to notice another presence nearby. You look to the right; down along the fence."),
							new DialogLine(null, true,
								"As you peer into the darkness created amoungst the foliage you see two dim lights like the eyes of an animal. Their pale red glow make the hairs on the back of your neck stand."),
							new DialogLine(randomGuard, false,
								"The guard shifts awkwardly. \"Hey, what're you looking at?\""),
							new DialogLine(randomAttendant, false,
								"Suddenly a short man stumbles over the gate threshold. Both you and the guard start at the loud commotion."),
							new DialogLine(randomAttendant, false,
								"This man grunts and shuffles over to you, untying the purse strings of his wallet."),
							new DialogLine(randomAttendant, false,
								"He pauses, starring at you. \"The pelts then, sir?\""),
							new DialogLine(playerProxy, true,
								"You trade him the pelts for coin and they all fold back into the threshold. As the door creaks shut you glance back into the brush but don't see anything unusual."),
						},
						//Result Summary _onSuccess
						"You exchanged your bear pelts for coin at the estate's front gate.",
						//Next Interactions _OnSuccess
						new Interaction[] {
							//Create search interaction to search for spookies, it'll end in the guards yelling at you
							new Interaction(
								//Type
								InteractionType.Search,
								//Dialog
								new DialogLine[] {
									new DialogLine(playerProxy, true,
										"You walk a ways away from the gate and then drift off into the woods and circle back to the area near the fence."),
									new DialogLine(playerProxy, true,
										"As you pace through the brush and fallen trees you search the area for anything suspicious."),
									new DialogLine(playerProxy, true,
										"You reach a small creek and hear the banter of approaching attendents."),
									new DialogLine(playerProxy, true,
										"You crouch down behind an maple tree as the attendents appear from behind the high bank."),
									new DialogLine(randomAttendant, false,
										"They saunter over to the washing station on the edge of the dammed creek."),
									new DialogLine(randomAttendant, true,
										"In hushed voices, the two carry on. \"Did you see it again lastnight?\""),
									new DialogLine(randomAttendant, false,
										"\"No but my wife said she saw a strange glow coming from the blacksmith's.\""),
									new DialogLine(randomGuard, true,
										"\"Hey, you there!\" rings out a voice from behind you. You spin around and see a guard standing at the fence a ways away."),
									new DialogLine(randomAttendant, false,
										"The attendents hurry back toward the estate."),
									new DialogLine(playerProxy, true,
										"Slinking away into the forest you lose yourself in thought; wondering what strange events have befallen this region."),
								},
								//Granted Items
								new ItemData[] {
								},
								//Is Revealed?
								true,
								//Does Block Map Movement?
								false,
								//Test Type
								Interaction.TestType.None,
								//testStat
								StatType.values()[0],
								//passingStatValue
								0,
								//Chance To Occur
								1f,
								//requiredItems
								null,
								//requiredClass
								ClassType.values()[0],
								//Battle Data
								null,
								//-Interaction Graph-
								//isPersistentIntr
								false,
								//isUniqueTreeIntr
								false,
								//virginRootEnduresReturnToBase
								false,
								//instanceRefreshTimer_days
								0,
								//cancelPersistentIntrTypes (InteractionType[])
								null,
								//Resulting Dialog _OnSuccess
								null,
								//Result Summary _onSuccess
								null,
								//Next Interactions _OnSuccess
								null,
								//Resulting Dialog _OnFailure,
								null,
								//Result SUmmary _onFailure
								null,
								//Next Interactions _OnFailure,
								null
							),
						},
						//Resulting Dialog _OnFailure,
						new DialogLine[] {
							new DialogLine(randomGuard, false,
								"\"If you're a hunter asking for more information about the job posting then come back once you've got 5 bear pelts to sell\" utters one of the guards."),
							new DialogLine(randomGuard, false,
								"They return to their posts and slam the gate shut."),
						},
						//Result Summary _onFailure
						"You've been informed that you must return once you've collected 5 bear pelts to sell.",
						//Next Interactions _OnFailure,
						null
					),
				}
			),
			//Mission Interaction Requirements (InteractionType[])
			new InteractionType[] { InteractionType.Talk },
			//Rewards (ItemData[])
			new ItemData[] {
				coin.GetItemWithQuantity(50),
			}
		);*/

		//Side Missions - End
	//}
}
