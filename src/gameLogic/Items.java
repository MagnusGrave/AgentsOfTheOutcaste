package gameLogic;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import data.ItemData;
import data.ItemDataStorage;
import dataShared.ItemRef;


public class Items {
	private static Items instance;
	//private static final String directory = "itemData/";
	public static final String itemDataStoragePath = "resources/itemData/ItemDataStorage.obj";
	
	/*private static final String weaponsJsonPath = "weapons.json";
	private static final String armorJsonPath = "armor.json";
	private static final String battleItemsJsonPath = "battleItems.json";
	private static final String journeyConsumablesJsonPath = "journeyConsumables.json";
	private static final String miscJsonPath = "misc.json";*/
	
	public static List<ItemData> itemList;
	
	//Called by Missions when its loaded the GranularData.json file
	public static void ReceiveCustomItemData(ItemData[] itemDatas) {
		itemList.addAll( new ArrayList<>(Arrays.asList(itemDatas)) );
	}
	
	
	public static void Initalize() {
		instance = new Items();
		itemList = new ArrayList<ItemData>();
		
		//instead, populate ItemList using the binary itemData file created by DevTools ItemSheetAdapter
		ItemDataStorage storage = null;
		/*try {
			URL url = instance.getClass().getClassLoader().getResource(itemDataStoragePath);
			File file = new File(url.getPath());
			
			FileInputStream fi = new FileInputStream(file);
			ObjectInputStream oi = new ObjectInputStream(fi);

			// Read objects
			storage = (ItemDataStorage) oi.readObject();

			//Uncomment this to debug SaveData
			//saveData.print();

			oi.close();
			fi.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found for ItemDataStorage");
		} catch (IOException e) {
			System.out.println("Error initializing stream to load ItemDataStorage");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}*/
		//The above approach doesn't work for game builds
		try {
			InputStream fi = instance.getClass().getClassLoader().getResourceAsStream(itemDataStoragePath);
			ObjectInputStream oi = new ObjectInputStream(fi);

			// Read objects
			storage = (ItemDataStorage) oi.readObject();

			oi.close();
			fi.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("Error initializing stream");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		
		//Populate itemList
		if(storage == null) {
			System.err.println("ItemDataStorage didn't exist or wasn't successfully loaded. If it exists then try generating a fresh version via the Dev project. itemList will remain empty.");
		} else {
			for(ItemData item : storage.itemDataArray)
				itemList.add(item);
		}
		
		
		//this is being moved to DevTools project into class ItemSheetAdapter
		/*Gson gson = new Gson();
		
		//WEAPONS
		String jsonString = LoadJson(weaponsJsonPath);
		if(jsonString != null) {
			//System.out.println("Reading weapon json");
			
			JsonArray jsonArray = gson.fromJson(jsonString, JsonArray.class);
			for(JsonElement element : jsonArray) {
				//System.out.println("Reading weaponElement");
				
				JsonObject jsonObject = element.getAsJsonObject();
	
				//Item variables
				String fileName = null, id = null, name = null, description = null;
				int quantity = 1;
				String valueTier = null;
				
				//Stats variables
				//Equipment Type for weapons is always RightHand(Should be called MainHand and if two handed it'll take the LeftHand slot too)
				//EquipmentType equipmentType = null;
				WeightType weight = null;
				KarmaType karma = null;
				ClassType[] usableClasses = null;
				int attack = 0, spirit = 0, defense = 0, vitality = 0;
				ElementType[] elements = null;
				
				//WeaponTraits variables
				int minRange = 1, maxRange = 1, aoeRange = 0;
				WeaponGroup weaponGroup = null;
				WeaponType weaponType = null;
				boolean twoHandsRequired = false;
				boolean canBeProjectile = false;
				
				for(String key : jsonObject.keySet()) {
					if(key.startsWith("Weapon Group")) {
						String allCaps = jsonObject.get(key).getAsString();
						String firstCap = allCaps.substring(0, 1) + allCaps.substring(1).toLowerCase();
						weaponGroup = WeaponGroup.valueOf(firstCap);
					} else if(key.startsWith("Weapon Type")) {
						weaponType = WeaponType.valueOf(jsonObject.get(key).getAsString());
					} else if(key.startsWith("File Name")) {
						fileName = jsonObject.get(key).getAsString();
					} else if(key.startsWith("Id")) {
						id = jsonObject.get(key).getAsString();
					} else if(key.startsWith("Name")) {
						name = jsonObject.get(key).getAsString();
					} else if(key.startsWith("Description")) {
						String desc = "";
						if(jsonObject.get(key).isJsonArray()) {
							JsonArray descChunkElements = jsonObject.get(key).getAsJsonArray();
							for(int i = 0; i < descChunkElements.size(); i++) {
								desc = descChunkElements.get(i) .getAsString() + (i < descChunkElements.size() - 1 ? ", " : "");
							}
						} else {
							desc = jsonObject.get(key).getAsString();
						}
						description = desc;
					} else if(key.startsWith("Classes")) {
						List<ClassType> classes = new ArrayList<ClassType>();
						if(jsonObject.get(key).isJsonArray()) {
							for(JsonElement classLetterElement : jsonObject.get(key).getAsJsonArray()) {
								ClassType classType = getClassType(classLetterElement.getAsString());
								classes.add(classType);
							}
						} else {
							ClassType classType = getClassType(jsonObject.get(key).getAsString());
							classes.add(classType);
						}
						usableClasses = classes.stream().toArray(ClassType[]::new);
					} else if(key.startsWith("Weight")) {
						switch(jsonObject.get(key).getAsString()) {
							case "L":
								weight = WeightType.Light;
								break;
							case "M":
								weight = WeightType.Medium;
								break;
							case "H":
								weight = WeightType.Heavy;
								break;
							default:
								System.err.println("Items.Initialize() - Add support for WeightType: " + jsonObject.get(key).getAsString());
								break;
						}
					} else if(key.startsWith("Two Hands")) {
						twoHandsRequired = jsonObject.get(key).getAsString().equals("T") ? true : false;
					} else if(key.startsWith("Can Be P")) {
						canBeProjectile = jsonObject.get(key).getAsString().equals("T") ? true : false;
					} else if(key.startsWith("Range")) {
						String rangeString = jsonObject.get(key).getAsString();
						if(rangeString.length() == 1) {
							minRange = 1;
							maxRange = jsonObject.get(key).getAsInt();
						} else {
							minRange = Integer.parseInt(rangeString.substring(0, 1));
							maxRange = Integer.parseInt(rangeString.substring(4));
						}
					} else if(key.startsWith("Attack")) {
						attack = jsonObject.get(key).getAsInt();
					} else if(key.startsWith("Spirit")) {
						spirit = jsonObject.get(key).getAsInt();
					} else if(key.startsWith("Defense")) {
						defense = jsonObject.get(key).getAsInt();
					} else if(key.startsWith("HP")) {
						vitality = jsonObject.get(key).getAsInt();
					} else if(key.startsWith("Karma")) {
						switch(jsonObject.get(key).getAsString()) {
							case "C":
								karma = KarmaType.Cursed;
								break;
							case "N":
								karma = KarmaType.Neutral;
								break;
							case "D":
								karma = KarmaType.Divine;
								break;
							default:
								System.err.println("Items.Initialize() - Add support for KarmaType: " + jsonObject.get(key).getAsString());
								break;
						}
					} else if(key.startsWith("Elements")) {
						List<ElementType> elementList = new ArrayList<ElementType>();
						if(jsonObject.get(key).isJsonArray()) {
							for(JsonElement elementElement : jsonObject.get(key).getAsJsonArray()) {
								elementList.add( ElementType.valueOf(elementElement.getAsString()) );
							}
						} else if(!jsonObject.get(key).getAsString().equals("NA")) {
							elementList.add( ElementType.valueOf(jsonObject.get(key).getAsString()) );
						}
						elements = elementList.stream().toArray(ElementType[]::new);
					} else if(key.startsWith("Value")) {
						valueTier = jsonObject.get(key).getAsString();
					}
				}
				WeaponTraits weaponTraits = new WeaponTraits(minRange, maxRange, aoeRange, weaponGroup, weaponType, twoHandsRequired, canBeProjectile);
				Stats stats = new Stats(EquipmentType.RightHand, weight, karma, usableClasses, attack, spirit, defense, vitality, elements, weaponTraits);
				ItemData itemData = new ItemData(fileName, id, name, description, ItemType.Weapon, stats, quantity, valueTier, null);
				
				//if(itemData.getName().equals("Old Bokuto"))
				//	System.out.println(itemData.toString());
				//else
				//	System.out.println("Loaded Item: " + itemData.getName());
				itemList.add(itemData);
			}
			System.out.println("Items - Weapons loaded");
		}
		
		//ARMOR
		jsonString = LoadJson(armorJsonPath);
		if(jsonString != null) {
			//System.out.println("Reading armor json");
			
			JsonArray jsonArray = gson.fromJson(jsonString, JsonArray.class);
			for(JsonElement element : jsonArray) {
				//System.out.println("Reading armor element");
				
				JsonObject jsonObject = element.getAsJsonObject();
	
				//Item variables
				String fileName = null, id = null, name = null, description = null;
				int quantity = 1;
				String valueTier = null;
				
				//Stats variables
				//Equipment Type for weapons is always RightHand(Should be called MainHand and if two handed it'll take the LeftHand slot too)
				EquipmentType equipmentType = null;
				WeightType weight = null;
				KarmaType karma = null;
				ClassType[] usableClasses = null;
				int attack = 0, spirit = 0, defense = 0, vitality = 0;
				ElementType[] elements = null;
				
				for(String key : jsonObject.keySet()) {
					if(key.startsWith("Classes")) {
						List<ClassType> classes = new ArrayList<ClassType>();
						if(jsonObject.get(key).isJsonArray()) {
							for(JsonElement classLetterElement : jsonObject.get(key).getAsJsonArray()) {
								ClassType classType = getClassType(classLetterElement.getAsString());
								classes.add(classType);
							}
						} else {
							ClassType classType = getClassType(jsonObject.get(key).getAsString());
							classes.add(classType);
						}
						usableClasses = classes.stream().toArray(ClassType[]::new);
					} else if(key.startsWith("Equipment Type")) {
						equipmentType = EquipmentType.valueOf(jsonObject.get(key).getAsString());
					} else if(key.startsWith("File Name")) {
						fileName = jsonObject.get(key).getAsString();
					} else if(key.startsWith("Id")) {
						id = jsonObject.get(key).getAsString();
					} else if(key.startsWith("Name")) {
						name = jsonObject.get(key).getAsString();
					} else if(key.startsWith("Description")) {
						String desc = "";
						if(jsonObject.get(key).isJsonArray()) {
							JsonArray descChunkElements = jsonObject.get(key).getAsJsonArray();
							for(int i = 0; i < descChunkElements.size(); i++) {
								desc = descChunkElements.get(i) .getAsString() + (i < descChunkElements.size() - 1 ? ", " : "");
							}
						} else {
							desc = jsonObject.get(key).getAsString();
						}
						description = desc;
					} else if(key.startsWith("Weight")) {
						switch(jsonObject.get(key).getAsString()) {
							case "L":
								weight = WeightType.Light;
								break;
							case "M":
								weight = WeightType.Medium;
								break;
							case "H":
								weight = WeightType.Heavy;
								break;
							default:
								System.err.println("Items.Initialize() - Add support for WeightType: " + jsonObject.get(key).getAsString());
								break;
						}
					} else if(key.startsWith("Attack")) {
						attack = jsonObject.get(key).getAsInt();
					} else if(key.startsWith("Spirit")) {
						spirit = jsonObject.get(key).getAsInt();
					} else if(key.startsWith("Defense")) {
						defense = jsonObject.get(key).getAsInt();
					} else if(key.startsWith("HP")) {
						vitality = jsonObject.get(key).getAsInt();
					} else if(key.startsWith("Karma")) {
						switch(jsonObject.get(key).getAsString()) {
							case "C":
								karma = KarmaType.Cursed;
								break;
							case "N":
								karma = KarmaType.Neutral;
								break;
							case "D":
								karma = KarmaType.Divine;
								break;
							default:
								System.err.println("Items.Initialize() - Add support for KarmaType: " + jsonObject.get(key).getAsString());
								break;
						}
					} else if(key.startsWith("Elements")) {
						List<ElementType> elementList = new ArrayList<ElementType>();
						if(jsonObject.get(key).isJsonArray()) {
							for(JsonElement elementElement : jsonObject.get(key).getAsJsonArray()) {
								elementList.add( ElementType.valueOf(elementElement.getAsString()) );
							}
						} else if(!jsonObject.get(key).getAsString().equals("NA")) {
							elementList.add( ElementType.valueOf(jsonObject.get(key).getAsString()) );
						}
						elements = elementList.stream().toArray(ElementType[]::new);
					} else if(key.startsWith("Value")) {
						valueTier = jsonObject.get(key).getAsString();
					}
				}
				Stats stats = new Stats(equipmentType, weight, karma, usableClasses, attack, spirit, defense, vitality, elements, null);
				ItemData itemData = new ItemData(fileName, id, name, description, ItemType.Armor, stats, quantity, valueTier, null);
				itemList.add(itemData);
			}
			System.out.println("Items - Armor loaded");
		}
		
		//BATTLEITEMS
		jsonString = LoadJson(battleItemsJsonPath);
		if(jsonString != null) {
			//System.out.println("Reading battleItems json");
			
			JsonArray jsonArray = gson.fromJson(jsonString, JsonArray.class);
			for(JsonElement element : jsonArray) {
				//System.out.println("Reading BattleItem element");
				
				JsonObject jsonObject = element.getAsJsonObject();
	
				//Item variables
				String fileName = null, id = null, name = null, description = null;
				int quantity = 1;
				String valueTier = null;
				
				//Stats variables
				//Equipment Type for weapons is always RightHand(Should be called MainHand and if two handed it'll take the LeftHand slot too)
				//EquipmentType equipmentType = null;
				WeightType weight = null;
				KarmaType karma = null;
				ClassType[] usableClasses = null;
				int attack = 0, spirit = 0, defense = 0, vitality = 0;
				ElementType[] elements = null;
				
				//BattleToolTraits
				int minRange = 1, maxRange = 1, aoeRange = 0;
				
				//BattleItemTraits
				int aoe = 0;
				int effectDuration = 0;
				StatusType status = null;
				//For types that are occuring in unison, i.e. if all their types are separated by a comma
				List<BattleItemType> multifariousTypes = new ArrayList<BattleItemType>();
				//For BattleItems with a variety of uses, i.e. if their types are described using "or"
				List<BattleItemType[]> splitBehaviorTypeGroups = new ArrayList<BattleItemType[]>();
				
				for(String key : jsonObject.keySet()) {
					if(key.startsWith("Battle Item Type")) {
						//If array then handle one element at a time, while checking for an element chunk consisting of "[TYPE] or [TYPE]", which needs to be handled
						if(jsonObject.get(key).isJsonArray()) {
							for(JsonElement typeElement : jsonObject.get(key).getAsJsonArray()) {
								if(typeElement.getAsString().contains(" or ")) {
									String[] endAndStartTypes = jsonObject.get(key).getAsString().split(" or ");
									
									//add last element and save to groups
									multifariousTypes.add( BattleItemType.valueOf(endAndStartTypes[0]));
									splitBehaviorTypeGroups.add(multifariousTypes.stream().toArray(BattleItemType[]::new));
									
									//Start a fresh list with the start element
									multifariousTypes.clear();
									multifariousTypes.add( BattleItemType.valueOf(endAndStartTypes[1]));
									
								} else {
									multifariousTypes.add( BattleItemType.valueOf(typeElement.getAsString()));
								}
							}
						} else {
							if(jsonObject.get(key).getAsString().contains(" or ")) {
								String[] groups = jsonObject.get(key).getAsString().split(" or ");
								for(String group : groups) {
									multifariousTypes.add( BattleItemType.valueOf(group));
									splitBehaviorTypeGroups.add(multifariousTypes.stream().toArray(BattleItemType[]::new));
									multifariousTypes.clear();
								}
							} else {
								multifariousTypes.add( BattleItemType.valueOf(jsonObject.get(key).getAsString()));
							}
						}
						//clear the multifariousTypes after the last element in the "or" series so that the correct constructor is called below when building classes
						if(splitBehaviorTypeGroups.size() > 0)
							multifariousTypes.clear();
					} else if(key.startsWith("File Name")) {
						fileName = jsonObject.get(key).getAsString();
					} else if(key.startsWith("Id")) {
						id = jsonObject.get(key).getAsString();
					} else if(key.startsWith("Name")) {
						name = jsonObject.get(key).getAsString();
					} else if(key.startsWith("Description")) {
						String desc = "";
						if(jsonObject.get(key).isJsonArray()) {
							JsonArray descChunkElements = jsonObject.get(key).getAsJsonArray();
							for(int i = 0; i < descChunkElements.size(); i++) {
								desc = descChunkElements.get(i) .getAsString() + (i < descChunkElements.size() - 1 ? ", " : "");
							}
						} else {
							desc = jsonObject.get(key).getAsString();
						}
						description = desc;
					} else if(key.startsWith("Classes")) {
						List<ClassType> classes = new ArrayList<ClassType>();
						if(jsonObject.get(key).isJsonArray()) {
							for(JsonElement classLetterElement : jsonObject.get(key).getAsJsonArray()) {
								ClassType classType = getClassType(classLetterElement.getAsString());
								classes.add(classType);
							}
						} else {
							ClassType classType = getClassType(jsonObject.get(key).getAsString());
							classes.add(classType);
						}
						usableClasses = classes.stream().toArray(ClassType[]::new);
					} else if(key.startsWith("Weight")) {
						switch(jsonObject.get(key).getAsString()) {
							case "L":
								weight = WeightType.Light;
								break;
							case "M":
								weight = WeightType.Medium;
								break;
							case "H":
								weight = WeightType.Heavy;
								break;
							default:
								System.err.println("Items.Initialize() - Add support for WeightType: " + jsonObject.get(key).getAsString());
								break;
						}
					} else if(key.startsWith("Range")) {
						String rangeString = jsonObject.get(key).getAsString();
						if(rangeString.length() == 1) {
							minRange = 1;
							maxRange = jsonObject.get(key).getAsInt();
						} else {
							minRange = Integer.parseInt(rangeString.substring(0, 1));
							maxRange = Integer.parseInt(rangeString.substring(4));
						}
					} else if(key.startsWith("AOE")) {
						aoe = jsonObject.get(key).getAsInt();
					} else if(key.startsWith("Effect Duration")) {
						effectDuration = jsonObject.get(key).getAsInt();
					} else if(key.startsWith("Status")) {
						String value = jsonObject.get(key).getAsString();
						if(!value.equals("NA"))
							status = StatusType.valueOf(value);
					} else if(key.startsWith("Attack")) {
						attack = jsonObject.get(key).getAsInt();
					} else if(key.startsWith("Spirit")) {
						spirit = jsonObject.get(key).getAsInt();
					} else if(key.startsWith("Defense")) {
						defense = jsonObject.get(key).getAsInt();
					} else if(key.startsWith("HP")) {
						vitality = jsonObject.get(key).getAsInt();
					} else if(key.startsWith("Karma")) {
						switch(jsonObject.get(key).getAsString()) {
							case "C":
								karma = KarmaType.Cursed;
								break;
							case "N":
								karma = KarmaType.Neutral;
								break;
							case "D":
								karma = KarmaType.Divine;
								break;
							default:
								System.err.println("Items.Initialize() - Add support for KarmaType: " + jsonObject.get(key).getAsString());
								break;
						}
					} else if(key.startsWith("Elements")) {
						List<ElementType> elementList = new ArrayList<ElementType>();
						if(jsonObject.get(key).isJsonArray()) {
							for(JsonElement elementElement : jsonObject.get(key).getAsJsonArray()) {
								elementList.add( ElementType.valueOf(elementElement.getAsString()) );
							}
						} else if(!jsonObject.get(key).getAsString().equals("NA")) {
							elementList.add( ElementType.valueOf(jsonObject.get(key).getAsString()) );
						}
						elements = elementList.stream().toArray(ElementType[]::new);
					} else if(key.startsWith("Value")) {
						valueTier = jsonObject.get(key).getAsString();
					}
				}
				BattleItemTraits battleItemTraits = null;
				if(multifariousTypes.size() > 0) 
					battleItemTraits = new BattleItemTraits(minRange, maxRange, aoeRange, aoe, effectDuration, status,
							multifariousTypes.stream().toArray(BattleItemType[]::new)
					);
				else
					battleItemTraits = new BattleItemTraits(minRange, maxRange, aoeRange, aoe, effectDuration, status,
							splitBehaviorTypeGroups.stream().toArray(BattleItemType[][]::new)
					);
				
				Stats stats = new Stats(null, weight, karma, usableClasses, attack, spirit, defense, vitality, elements, battleItemTraits);
				ItemData itemData = new ItemData(fileName, id, name, description, ItemType.BattleItem, stats, quantity, valueTier, null);
				itemList.add(itemData);
			}
			System.out.println("Items - BattleItems loaded");
		}
		
		//JourneyConsumables
		jsonString = LoadJson(journeyConsumablesJsonPath);
		if(jsonString != null) {
			//System.out.println("Reading JourneyConsumables json");
			
			JsonArray jsonArray = gson.fromJson(jsonString, JsonArray.class);
			for(JsonElement element : jsonArray) {
				//System.out.println("Reading  journeyConsumable element");
				
				JsonObject jsonObject = element.getAsJsonObject();
	
				//Item variables
				String fileName = null, id = null, name = null, description = null;
				int quantity = 1;
				String valueTier = null;
				
				//JourneyConsumableTraits
				int duration_days = 0;
				int staminaMod = 0;
				int staminaRegenRateMod = 0;
				int terrainRiskMod = 0;
				int terrainStaminaMod = 0;
				int settlementRiskMod = 0;
				int settlementStaminaMod = 0;
				List<SettlementType> encounterProtections = new ArrayList<SettlementType>();
				List<SettlementType> encounterAttractants = new ArrayList<SettlementType>();
				
				for(String key : jsonObject.keySet()) {
					if(key.startsWith("File Name")) {
						fileName = jsonObject.get(key).getAsString();
					} else if(key.startsWith("Id")) {
						id = jsonObject.get(key).getAsString();
					} else if(key.startsWith("Name")) {
						name = jsonObject.get(key).getAsString();
					} else if(key.startsWith("Description")) {
						String desc = "";
						if(jsonObject.get(key).isJsonArray()) {
							JsonArray descChunkElements = jsonObject.get(key).getAsJsonArray();
							for(int i = 0; i < descChunkElements.size(); i++) {
								desc = descChunkElements.get(i) .getAsString() + (i < descChunkElements.size() - 1 ? ", " : "");
							}
						} else {
							desc = jsonObject.get(key).getAsString();
						}
						description = desc;
					} else if(key.startsWith("Duration")) {
						duration_days = jsonObject.get(key).getAsInt();
					} else if(key.startsWith("Stamina Mod")) {
						staminaMod = jsonObject.get(key).getAsInt();
					} else if(key.startsWith("Stamina Regen")) {
						staminaRegenRateMod = jsonObject.get(key).getAsInt();
					} else if(key.startsWith("Terrain Risk")) {
						terrainRiskMod = jsonObject.get(key).getAsInt();
					} else if(key.startsWith("Terrain Stamina")) {
						terrainStaminaMod = jsonObject.get(key).getAsInt();
					} else if(key.startsWith("Settlement Risk")) {
						settlementRiskMod = jsonObject.get(key).getAsInt();
					} else if(key.startsWith("Settlement Stamina")) {
						settlementStaminaMod = jsonObject.get(key).getAsInt();
					} else if(key.startsWith("Encounter Protections")) {
						if(jsonObject.get(key).isJsonArray()) {
							for(JsonElement encounterElement : jsonObject.get(key).getAsJsonArray()) {
								encounterProtections.add( SettlementType.valueOf(SettlementType.class, encounterElement.getAsString()) );
							}
						} else if(!jsonObject.get(key).getAsString().equals("NA")) {
							encounterProtections.add( SettlementType.valueOf(SettlementType.class, jsonObject.get(key).getAsString()) );
						}
					} else if(key.startsWith("Encounter Attractants")) {
						if(jsonObject.get(key).isJsonArray()) {
							for(JsonElement encounterElement : jsonObject.get(key).getAsJsonArray()) {
								encounterAttractants.add( SettlementType.valueOf(SettlementType.class, encounterElement.getAsString()) );
							}
						} else if(!jsonObject.get(key).getAsString().equals("NA")) {
							encounterAttractants.add( SettlementType.valueOf(SettlementType.class, jsonObject.get(key).getAsString()) );
						}
					} else if(key.startsWith("Value")) {
						valueTier = jsonObject.get(key).getAsString();
					}
				}
				SettlementType[] protections = encounterProtections.stream().toArray(SettlementType[]::new);
				SettlementType[] attractants = encounterAttractants.stream().toArray(SettlementType[]::new);
				JourneyConsumableTraits journeyConsumableTraits = new JourneyConsumableTraits(
						duration_days, staminaMod, staminaRegenRateMod,
						terrainRiskMod, terrainStaminaMod,
						settlementRiskMod, settlementStaminaMod,
						protections, attractants);
				ItemData itemData = new ItemData(fileName, id, name, description, ItemType.JourneyConsumable, journeyConsumableTraits, quantity, valueTier, null);
				itemList.add(itemData);
			}
			System.out.println("Items - JourneyConsumables loaded");
		}
		
		//MISC
		jsonString = LoadJson(miscJsonPath);
		if(jsonString != null) {
			//System.out.println("Reading Misc json");
			
			JsonArray jsonArray = gson.fromJson(jsonString, JsonArray.class);
			for(JsonElement element : jsonArray) {
				//System.out.println("Reading misc element");
				
				JsonObject jsonObject = element.getAsJsonObject();
	
				//Item variables
				String fileName = null, id = null, name = null, description = null;
				int quantity = 1;
				String valueTier = null;
				
				for(String key : jsonObject.keySet()) {
					if(key.startsWith("File Name")) {
						fileName = jsonObject.get(key).getAsString();
					} else if(key.startsWith("Id")) {
						id = jsonObject.get(key).getAsString();
					} else if(key.startsWith("Name")) {
						name = jsonObject.get(key).getAsString();
					} else if(key.startsWith("Description")) {
						String desc = "";
						if(jsonObject.get(key).isJsonArray()) {
							JsonArray descChunkElements = jsonObject.get(key).getAsJsonArray();
							for(int i = 0; i < descChunkElements.size(); i++) {
								desc = descChunkElements.get(i) .getAsString() + (i < descChunkElements.size() - 1 ? ", " : "");
							}
						} else {
							desc = jsonObject.get(key).getAsString();
						}
						description = desc;
					} else if(key.startsWith("Value")) {
						valueTier = jsonObject.get(key).getAsString();
					}
				}
				ItemData itemData = new ItemData(fileName, id, name, description, ItemType.Misc, quantity, valueTier, null);
				itemList.add(itemData);
			}
			System.out.println("Items - Misc loaded");
		}
		
		//Merge multi-use items(BattleItem and JourneyConsumable) that live in multiple sheets, each set combination has discrete unique properties
		for(ItemData data : itemList) {
			ItemData[] matchingData = itemList.stream().filter(x -> data.getId().equals(x.getId())).toArray(ItemData[]::new);
			if(matchingData.length > 1) {
				List<ItemType> multifariousTypes = new ArrayList<ItemType>();
				Stats stats = null;
				JourneyConsumableTraits journeyConsumableTraits = null;
				//Build a new ItemData piece by piece
				for(ItemData itemData : matchingData) {
					multifariousTypes.add(itemData.getType());
					switch(data.getType()) {
						case Weapon:
						case Armor:
						case Misc:
							System.err.println("Weapon, Armor and Misc type items can't be multifarious, only BattleItems and JourneyConsumables.");
							break;
						case BattleItem:
							stats = itemData.getStats();
							break;
						case JourneyConsumable:
							journeyConsumableTraits = itemData.getJourneyConsumableTraits();
							break;
						default:
							System.out.println("Items.Initialize() - Add support for: " + data.getType());
							break;
					}
					itemList.remove(itemData);
				}
				ItemData template = matchingData[0];
				ItemData mergedItemData = new ItemData(multifariousTypes.stream().toArray(ItemType[]::new), template, stats, journeyConsumableTraits);
				
				itemList.add(mergedItemData);
			}
		}*/
	}
	
	//Moved to DevTools class ItemSheetAdapter
	/*
	private static ClassType getClassType(String firstInitial) {
		ClassType classType = null;
		switch(firstInitial) {
			case "B":
				classType = ClassType.BANDIT;
				break;
			case "N":
				classType = ClassType.NINJA;
				break;
			case "P":
				classType = ClassType.PRIEST;
				break;
			case "R":
				classType = ClassType.RONIN;
				break;
			default:
				System.err.println("Items.getClassType() - Add support for classType: " + firstInitial);
				break;
		}
		return classType;
	}
	
	//get load method from battle scene usage
	private static String LoadJson(String fileName) {
		String jsonPath = directory + fileName;
		System.out.println("Items.LoadJson() - Loading from: " + jsonPath);
		
		InputStream is = instance.getClass().getClassLoader().getResourceAsStream("resources/" + jsonPath);
		if(is == null) {
			System.err.println("File not found at: " + "resources/" + jsonPath + " ... Dont forget to Refresh the Java project after adding new files.");
			Thread.dumpStack();
			return null;
		}
		String jsonString = null;
		try {
			InputStreamReader isReader = new InputStreamReader(is);
			BufferedReader reader = new BufferedReader(isReader);
			StringBuffer sb = new StringBuffer();
			String str;
			while((str = reader.readLine()) != null){
				sb.append(str);
			}
			jsonString = sb.toString();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return jsonString;
	}
	*/
	
	public static ItemData getById(String id) {
		//System.out.println("ItemList.size: " + itemList.size() + ", Name: " + itemList.get(0).getName() + ", Id: " + itemList.get(0).getId());
		Optional<ItemData> possibleItemData = itemList.stream().filter(x -> x.getId().equals(id)).findFirst();
		if(!possibleItemData.isPresent()) {
			System.err.println("Items.getById() - Can't find itemData with id: " + id);
			return null;
		} else {
			return possibleItemData.get();
		}
	}
	
	public static ItemData[] GetItemDatas(ItemRef[] itemRefs) {
		if(itemRefs == null)
			return new ItemData[]{};
					
		ItemData[] itemDatas = new ItemData[itemRefs.length];
		for(int i = 0; i < itemRefs.length; i++) {
			ItemData itemData = getById(itemRefs[i].itemId);
			if(itemData == null) {
				itemDatas[i] = null;
				continue;
			}
			itemDatas[i] = itemData.GetItemWithQuantity(itemRefs[i].quantity);
		}
		return itemDatas;
	}
}
