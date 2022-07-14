package devParallel;

import java.lang.reflect.Type;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import enums.BattleItemType;
import enums.ClassType;
import enums.Direction;
import enums.ElementType;
import enums.EnvironmentType;
import enums.EquipmentType;
import enums.InteractionType;
import enums.ItemType;
import enums.KarmaType;
import enums.MissionIndicatorType;
import enums.SceneLayeringType;
import enums.WeaponGroup;
import enums.WeaponType;
import enums.WeightType;
import enums.WinConditionType;
import enums.WorldTileType;
import enums.SettlementType;
import enums.SlotType;
import enums.StatType;
import enums.StateType;
import enums.StatusType;
import enums.TestType;
import enums.TransitionType;
import enums.SettlementDesignation;

public class UnitySerializationUtility {

	public static GsonBuilder AttachUnityAdapters(GsonBuilder gsonBuilder) {
		
		gsonBuilder.registerTypeAdapter(BattleItemType.class, new BattleItemTypeSerializer() );
		gsonBuilder.registerTypeAdapter(ClassType.class, new ClassTypeSerializer() );
		gsonBuilder.registerTypeAdapter(Direction.class, new DirectionSerializer() );
		gsonBuilder.registerTypeAdapter(ElementType.class, new ElementTypeSerializer() );
		gsonBuilder.registerTypeAdapter(EnvironmentType.class, new EnvironmentTypeSerializer() );
		gsonBuilder.registerTypeAdapter(EquipmentType.class, new EquipmentTypeSerializer() );
		gsonBuilder.registerTypeAdapter(InteractionType.class, new InteractionTypeSerializer() );
		gsonBuilder.registerTypeAdapter(ItemType.class, new ItemTypeSerializer() );
		gsonBuilder.registerTypeAdapter(KarmaType.class, new KarmaTypeSerializer() );
		gsonBuilder.registerTypeAdapter(MissionIndicatorType.class, new MissionIndicatorTypeSerializer() );
		gsonBuilder.registerTypeAdapter(SceneLayeringType.class, new SceneLayeringTypeSerializer() );
		gsonBuilder.registerTypeAdapter(SettlementDesignation.class, new SettlementDesignationSerializer() );
		gsonBuilder.registerTypeAdapter(SettlementType.class, new SettlementTypeSerializer() );
		gsonBuilder.registerTypeAdapter(SlotType.class, new SlotTypeSerializer() );
		gsonBuilder.registerTypeAdapter(StateType.class, new StateTypeSerializer() );
		gsonBuilder.registerTypeAdapter(StatType.class, new StatTypeSerializer() );
		gsonBuilder.registerTypeAdapter(StatusType.class, new StatusTypeSerializer() );
		gsonBuilder.registerTypeAdapter(TestType.class, new TestTypeSerializer() );
		gsonBuilder.registerTypeAdapter(TransitionType.class, new TransitionTypeSerializer() );
		gsonBuilder.registerTypeAdapter(WeaponGroup.class, new WeaponGroupSerializer() );
		gsonBuilder.registerTypeAdapter(WeaponType.class, new WeaponTypeSerializer() );
	    gsonBuilder.registerTypeAdapter(WeightType.class, new WeightTypeSerializer() );
	    gsonBuilder.registerTypeAdapter(WinConditionType.class, new WinConditionTypeSerializer() );
	    gsonBuilder.registerTypeAdapter(WorldTileType.class, new WorldTileTypeSerializer() );
	    
	    gsonBuilder.registerTypeAdapter(BattleItemType.class, new BattleItemTypeDeserializer() );
	    gsonBuilder.registerTypeAdapter(ClassType.class, new ClassTypeDeserializer() );
	    gsonBuilder.registerTypeAdapter(Direction.class, new DirectionDeserializer() );
	    gsonBuilder.registerTypeAdapter(ElementType.class, new ElementTypeDeserializer() );
	    gsonBuilder.registerTypeAdapter(EnvironmentType.class, new EnvironmentTypeDeserializer() );
	    gsonBuilder.registerTypeAdapter(EquipmentType.class, new EquipmentTypeDeserializer() );
	    gsonBuilder.registerTypeAdapter(InteractionType.class, new InteractionTypeDeserializer() );
		gsonBuilder.registerTypeAdapter(ItemType.class, new ItemTypeDeserializer() );
	    gsonBuilder.registerTypeAdapter(KarmaType.class, new KarmaTypeDeserializer() );
	    gsonBuilder.registerTypeAdapter(MissionIndicatorType.class, new MissionIndicatorTypeDeserializer() );
	    gsonBuilder.registerTypeAdapter(SceneLayeringType.class, new SceneLayeringTypeDeserializer() );
	    gsonBuilder.registerTypeAdapter(SettlementDesignation.class, new SettlementDesignationDeserializer() );
	    gsonBuilder.registerTypeAdapter(SettlementType.class, new SettlementTypeDeserializer() );
	    gsonBuilder.registerTypeAdapter(SlotType.class, new SlotTypeDeserializer() );
	    gsonBuilder.registerTypeAdapter(StateType.class, new StateTypeDeserializer() );
	    gsonBuilder.registerTypeAdapter(StatType.class, new StatTypeDeserializer() );
	    gsonBuilder.registerTypeAdapter(StatusType.class, new StatusTypeDeserializer() );
	    gsonBuilder.registerTypeAdapter(TestType.class, new TestTypeDeserializer() );
	    gsonBuilder.registerTypeAdapter(TransitionType.class, new TransitionTypeDeserializer() );
	    gsonBuilder.registerTypeAdapter(WeaponGroup.class, new WeaponGroupDeserializer() );
	    gsonBuilder.registerTypeAdapter(WeaponType.class, new WeaponTypeDeserializer() );
	    gsonBuilder.registerTypeAdapter(WeightType.class, new WeightTypeDeserializer() );
	    gsonBuilder.registerTypeAdapter(WinConditionType.class, new WinConditionTypeDeserializer() );
	    gsonBuilder.registerTypeAdapter(WorldTileType.class, new WorldTileTypeDeserializer() );
	    
		return gsonBuilder;
	}
	
	//Serialization - Start
	
	//Turns this int value back to an enum
	private static class ItemTypeSerializer implements JsonSerializer<ItemType> {
		@Override
		public JsonElement serialize(ItemType enumSrc, Type arg1, JsonSerializationContext arg2) {
	        return arg2.serialize( (enumSrc == null ? -1 : enumSrc.getValue()) );
		}
	}
	//Turns this int value back to an enum
	private static class EquipmentTypeSerializer implements JsonSerializer<EquipmentType> {
		@Override
		public JsonElement serialize(EquipmentType enumSrc, Type arg1, JsonSerializationContext arg2) {
			return arg2.serialize( (enumSrc == null ? -1 : enumSrc.getValue()) );
		}
	}
	//Turns this int value back to an enum
	private static class WeightTypeSerializer implements JsonSerializer<WeightType> {
		@Override
		public JsonElement serialize(WeightType enumSrc, Type arg1, JsonSerializationContext arg2) {
			return arg2.serialize( (enumSrc == null ? -1 : enumSrc.getValue()) );
		}
	}
	//Turns this int value back to an enum
	private static class KarmaTypeSerializer implements JsonSerializer<KarmaType> {
		@Override
		public JsonElement serialize(KarmaType enumSrc, Type arg1, JsonSerializationContext arg2) {
			return arg2.serialize( (enumSrc == null ? -1 : enumSrc.getValue()) );
		}
	}
	//Turns this int value back to an enum
	private static class MissionIndicatorTypeSerializer implements JsonSerializer<MissionIndicatorType> {
		@Override
		public JsonElement serialize(MissionIndicatorType enumSrc, Type arg1, JsonSerializationContext arg2) {
			return arg2.serialize( (enumSrc == null ? -1 : enumSrc.getValue()) );
		}
	}
	//Turns this int value back to an enum
	private static class ClassTypeSerializer implements JsonSerializer<ClassType> {
		@Override
		public JsonElement serialize(ClassType enumSrc, Type arg1, JsonSerializationContext arg2) {
			return arg2.serialize( (enumSrc == null ? -1 : enumSrc.getValue()) );
		}
	}
	//Turns this int value back to an enum
	private static class ElementTypeSerializer implements JsonSerializer<ElementType> {
		@Override
		public JsonElement serialize(ElementType enumSrc, Type arg1, JsonSerializationContext arg2) {
			return arg2.serialize( (enumSrc == null ? -1 : enumSrc.getValue()) );
		}
	}
	//Turns this int value back to an enum
	private static class WeaponGroupSerializer implements JsonSerializer<WeaponGroup> {
		@Override
		public JsonElement serialize(WeaponGroup enumSrc, Type arg1, JsonSerializationContext arg2) {
			return arg2.serialize( (enumSrc == null ? -1 : enumSrc.getValue()) );
		}
	}
	//Turns this int value back to an enum
	private static class WeaponTypeSerializer implements JsonSerializer<WeaponType> {
		@Override
		public JsonElement serialize(WeaponType enumSrc, Type arg1, JsonSerializationContext arg2) {
			return arg2.serialize( (enumSrc == null ? -1 : enumSrc.getValue()) );
		}
	}
	//Turns this int value back to an enum
	private static class StatusTypeSerializer implements JsonSerializer<StatusType> {
		@Override
		public JsonElement serialize(StatusType enumSrc, Type arg1, JsonSerializationContext arg2) {
			return arg2.serialize( (enumSrc == null ? -1 : enumSrc.getValue()) );
		}
	}
	//Turns this int value back to an enum
	private static class BattleItemTypeSerializer implements JsonSerializer<BattleItemType> {
		@Override
		public JsonElement serialize(BattleItemType enumSrc, Type arg1, JsonSerializationContext arg2) {
			return arg2.serialize( (enumSrc == null ? -1 : enumSrc.getValue()) );
		}
	}
	//Turns this int value back to an enum
	private static class SettlementTypeSerializer implements JsonSerializer<SettlementType> {
		@Override
		public JsonElement serialize(SettlementType enumSrc, Type arg1, JsonSerializationContext arg2) {
			return arg2.serialize( (enumSrc == null ? -1 : enumSrc.getValue()) );
		}
	}
	//Turns this int value back to an enum
	private static class SettlementDesignationSerializer implements JsonSerializer<SettlementDesignation> {
		@Override
		public JsonElement serialize(SettlementDesignation enumSrc, Type arg1, JsonSerializationContext arg2) {
			return arg2.serialize( (enumSrc == null ? -1 : enumSrc.getValue()) );
		}
	}
	//Turns this int value back to an enum
	private static class WinConditionTypeSerializer implements JsonSerializer<WinConditionType> {
		@Override
		public JsonElement serialize(WinConditionType enumSrc, Type arg1, JsonSerializationContext arg2) {
			return arg2.serialize( (enumSrc == null ? -1 : enumSrc.getValue()) );
		}
	}
	//Turns this int value back to an enum
	private static class InteractionTypeSerializer implements JsonSerializer<InteractionType> {
		@Override
		public JsonElement serialize(InteractionType enumSrc, Type arg1, JsonSerializationContext arg2) {
			return arg2.serialize( (enumSrc == null ? -1 : enumSrc.getValue()) );
		}
	}
	//Turns this int value back to an enum
	private static class StatTypeSerializer implements JsonSerializer<StatType> {
		@Override
		public JsonElement serialize(StatType enumSrc, Type arg1, JsonSerializationContext arg2) {
			return arg2.serialize( (enumSrc == null ? -1 : enumSrc.getValue()) );
		}
	}
	//Turns this int value back to an enum
	private static class EnvironmentTypeSerializer implements JsonSerializer<EnvironmentType> {
		@Override
		public JsonElement serialize(EnvironmentType enumSrc, Type arg1, JsonSerializationContext arg2) {
			return arg2.serialize( (enumSrc == null ? -1 : enumSrc.getValue()) );
		}
	}
	//Turns this int value back to an enum
	private static class DirectionSerializer implements JsonSerializer<Direction> {
		@Override
		public JsonElement serialize(Direction enumSrc, Type arg1, JsonSerializationContext arg2) {
			return arg2.serialize( (enumSrc == null ? -1 : enumSrc.getValue()) );
		}
	}
	//Turns this int value back to an enum
	private static class StateTypeSerializer implements JsonSerializer<StateType> {
		@Override
		public JsonElement serialize(StateType enumSrc, Type arg1, JsonSerializationContext arg2) {
			return arg2.serialize( (enumSrc == null ? -1 : enumSrc.getValue()) );
		}
	}
	//Turns this int value back to an enum
	private static class SlotTypeSerializer implements JsonSerializer<SlotType> {
		@Override
		public JsonElement serialize(SlotType enumSrc, Type arg1, JsonSerializationContext arg2) {
			return arg2.serialize( (enumSrc == null ? -1 : enumSrc.getValue()) );
		}
	}
	//Turns this int value back to an enum
	private static class TestTypeSerializer implements JsonSerializer<TestType> {
		@Override
		public JsonElement serialize(TestType enumSrc, Type arg1, JsonSerializationContext arg2) {
			return arg2.serialize( (enumSrc == null ? -1 : enumSrc.getValue()) );
		}
	}
	//Turns this int value back to an enum
	private static class TransitionTypeSerializer implements JsonSerializer<TransitionType> {
		@Override
		public JsonElement serialize(TransitionType enumSrc, Type arg1, JsonSerializationContext arg2) {
			return arg2.serialize( (enumSrc == null ? -1 : enumSrc.getValue()) );
		}
	}
	//Turns this int value back to an enum
	private static class WorldTileTypeSerializer implements JsonSerializer<WorldTileType> {
		@Override
		public JsonElement serialize(WorldTileType enumSrc, Type arg1, JsonSerializationContext arg2) {
			return arg2.serialize( (enumSrc == null ? -1 : enumSrc.getValue()) );
		}
	}
	//Turns this int value back to an enum
	private static class SceneLayeringTypeSerializer implements JsonSerializer<SceneLayeringType> {
		@Override
		public JsonElement serialize(SceneLayeringType enumSrc, Type arg1, JsonSerializationContext arg2) {
			return arg2.serialize( (enumSrc == null ? -1 : enumSrc.getValue()) );
		}
	}
	
	//Serialization - End
	
	//Deserialization - Start
	
	//Turns this int value back to an enum
	private static class ItemTypeDeserializer implements JsonDeserializer<ItemType> {
		@Override
		public ItemType deserialize(JsonElement json, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
			int typeInt = json.getAsInt();
			if(typeInt == -1)
				return null;
			else
				return ItemType.values()[typeInt];
		}
	}
	//Turns this int value back to an enum
	private static class EquipmentTypeDeserializer implements JsonDeserializer<EquipmentType> {
		@Override
		public EquipmentType deserialize(JsonElement json, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
			int typeInt = json.getAsInt();
			if(typeInt == -1)
				return null;
			else
				return EquipmentType.values()[typeInt];
		}
	}
	//Turns this int value back to an enum
	private static class WeightTypeDeserializer implements JsonDeserializer<WeightType> {
		@Override
		public WeightType deserialize(JsonElement json, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
			int typeInt = json.getAsInt();
			if(typeInt == -1)
				return null;
			else
				return WeightType.values()[typeInt];
		}
	}
	//Turns this int value back to an enum
	private static class KarmaTypeDeserializer implements JsonDeserializer<KarmaType> {
		@Override
		public KarmaType deserialize(JsonElement json, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
			int typeInt = json.getAsInt();
			if(typeInt == -1)
				return null;
			else
				return KarmaType.values()[typeInt];
		}
	}
	//Turns this int value back to an enum
	private static class MissionIndicatorTypeDeserializer implements JsonDeserializer<MissionIndicatorType> {
		@Override
		public MissionIndicatorType deserialize(JsonElement json, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
			int typeInt = json.getAsInt();
			if(typeInt == -1)
				return null;
			else
				return MissionIndicatorType.values()[typeInt];
		}
	}
	//Turns this int value back to an enum
	private static class ClassTypeDeserializer implements JsonDeserializer<ClassType> {
		@Override
		public ClassType deserialize(JsonElement json, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
			int typeInt = json.getAsInt();
			if(typeInt == -1)
				return null;
			else
				return ClassType.values()[typeInt];
		}
	}
	//Turns this int value back to an enum
	private static class ElementTypeDeserializer implements JsonDeserializer<ElementType> {
		@Override
		public ElementType deserialize(JsonElement json, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
			int typeInt = json.getAsInt();
			if(typeInt == -1)
				return null;
			else
				return ElementType.values()[typeInt];
		}
	}
	//Turns this int value back to an enum
	private static class WeaponGroupDeserializer implements JsonDeserializer<WeaponGroup> {
		@Override
		public WeaponGroup deserialize(JsonElement json, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
			int typeInt = json.getAsInt();
			if(typeInt == -1)
				return null;
			else
				return WeaponGroup.values()[typeInt];
		}
	}
	//Turns this int value back to an enum
	private static class WeaponTypeDeserializer implements JsonDeserializer<WeaponType> {
		@Override
		public WeaponType deserialize(JsonElement json, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
			int typeInt = json.getAsInt();
			if(typeInt == -1)
				return null;
			else
				return WeaponType.values()[typeInt];
		}
	}
	//Turns this int value back to an enum
	private static class StatusTypeDeserializer implements JsonDeserializer<StatusType> {
		@Override
		public StatusType deserialize(JsonElement json, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
			int typeInt = json.getAsInt();
			if(typeInt == -1)
				return null;
			else
				return StatusType.values()[typeInt];
		}
	}
	//Turns this int value back to an enum
	private static class BattleItemTypeDeserializer implements JsonDeserializer<BattleItemType> {
		@Override
		public BattleItemType deserialize(JsonElement json, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
			int typeInt = json.getAsInt();
			if(typeInt == -1)
				return null;
			else
				return BattleItemType.values()[typeInt];
		}
	}
	//Turns this int value back to an enum
	private static class SettlementTypeDeserializer implements JsonDeserializer<SettlementType> {
		@Override
		public SettlementType deserialize(JsonElement json, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
			int typeInt = json.getAsInt();
			if(typeInt == -1)
				return null;
			else
				return SettlementType.values()[typeInt];
		}
	}
	//Turns this int value back to an enum
	private static class SettlementDesignationDeserializer implements JsonDeserializer<SettlementDesignation> {
		@Override
		public SettlementDesignation deserialize(JsonElement json, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
			int typeInt = json.getAsInt();
			if(typeInt == -1)
				return null;
			else
				return SettlementDesignation.values()[typeInt];
		}
	}
	//Turns this int value back to an enum
	private static class WinConditionTypeDeserializer implements JsonDeserializer<WinConditionType> {
		@Override
		public WinConditionType deserialize(JsonElement json, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
			int typeInt = json.getAsInt();
			if(typeInt == -1)
				return null;
			else
				return WinConditionType.values()[typeInt];
		}
	}
	//Turns this int value back to an enum
	private static class InteractionTypeDeserializer implements JsonDeserializer<InteractionType> {
		@Override
		public InteractionType deserialize(JsonElement json, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
			int typeInt = json.getAsInt();
			if(typeInt == -1)
				return null;
			else
				return InteractionType.values()[typeInt];
		}
	}
	//Turns this int value back to an enum
	private static class StatTypeDeserializer implements JsonDeserializer<StatType> {
		@Override
		public StatType deserialize(JsonElement json, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
			int typeInt = json.getAsInt();
			if(typeInt == -1)
				return null;
			else
				return StatType.values()[typeInt];
		}
	}
	//Turns this int value back to an enum
	private static class EnvironmentTypeDeserializer implements JsonDeserializer<EnvironmentType> {
		@Override
		public EnvironmentType deserialize(JsonElement json, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
			int typeInt = json.getAsInt();
			if(typeInt == -1)
				return null;
			else
				return EnvironmentType.values()[typeInt];
		}
	}
	//Turns this int value back to an enum
	private static class DirectionDeserializer implements JsonDeserializer<Direction> {
		@Override
		public Direction deserialize(JsonElement json, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
			int typeInt = json.getAsInt();
			if(typeInt == -1)
				return null;
			else
				return Direction.values()[typeInt];
		}
	}
	//Turns this int value back to an enum
	private static class StateTypeDeserializer implements JsonDeserializer<StateType> {
		@Override
		public StateType deserialize(JsonElement json, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
			int typeInt = json.getAsInt();
			if(typeInt == -1)
				return null;
			else
				return StateType.fromInteger(typeInt);
		}
	}
	//Turns this int value back to an enum
	private static class SlotTypeDeserializer implements JsonDeserializer<SlotType> {
		@Override
		public SlotType deserialize(JsonElement json, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
			int typeInt = json.getAsInt();
			if(typeInt == -1)
				return null;
			else
				return SlotType.values()[typeInt];
		}
	}
	//Turns this int value back to an enum
	private static class TestTypeDeserializer implements JsonDeserializer<TestType> {
		@Override
		public TestType deserialize(JsonElement json, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
			int typeInt = json.getAsInt();
			if(typeInt == -1)
				return null;
			else
				return TestType.values()[typeInt];
		}
	}
	//Turns this int value back to an enum
	private static class TransitionTypeDeserializer implements JsonDeserializer<TransitionType> {
		@Override
		public TransitionType deserialize(JsonElement json, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
			int typeInt = json.getAsInt();
			if(typeInt == -1)
				return null;
			else
				return TransitionType.values()[typeInt];
		}
	}
	//Turns this int value back to an enum
	private static class WorldTileTypeDeserializer implements JsonDeserializer<WorldTileType> {
		@Override
		public WorldTileType deserialize(JsonElement json, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
			int typeInt = json.getAsInt();
			if(typeInt == -1)
				return null;
			else
				return WorldTileType.values()[typeInt];
		}
	}
	//Turns this int value back to an enum
	private static class SceneLayeringTypeDeserializer implements JsonDeserializer<SceneLayeringType> {
		@Override
		public SceneLayeringType deserialize(JsonElement json, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
			int typeInt = json.getAsInt();
			if(typeInt == -1)
				return null;
			else
				return SceneLayeringType.values()[typeInt];
		}
	}
	
	//Deserialization - End
}