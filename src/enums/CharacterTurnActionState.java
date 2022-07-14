package enums;

public enum CharacterTurnActionState {
	ActionMenu,
	
	//Picka a tile to move to
	MoveSelection,
	
	//Picka atile to attack
	AttackSelection,
	
	//Choose an ability from the list
	AbilityMenu,
	//Pick a tile at which to apply the ability
	AbilitySelection,
	
	//Choose an item from the list
	ItemMenu,
	//Pick a tile at which to apply the item
	ItemSelection,
	
	//Pick a tile to face
	WaitSelection
}
