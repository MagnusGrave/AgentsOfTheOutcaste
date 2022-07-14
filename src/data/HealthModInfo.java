package data;

import java.util.List;

import enums.ElementType;
import enums.StatusType;
import gameLogic.CharacterBase.PostBoutInstructionType;

/**
 * This may represent either: Non-Elemental Damage, Elemental Damage or Healing(Non-Elemental)
 * @author Magnus
 *
 */
public class HealthModInfo {
	public HealthModInfo(
			//Damage or healing
			boolean isHealing, boolean isRevive, int amount, ElementType elementType,
			
			//Cures or statuses
			List<StatusType> appliedCures, List<StatusType> appliedStatuses,
			
			//Buffs and Debuffs
			List<AttributeMod> buffs, List<AttributeMod> debuffs,
			
			PostBoutInstructionType postBoutInstruction
	) {
		this.isHealing = isHealing;
		this.isRevive = isRevive;
		this.amount = amount;
		this.elementType = elementType;
		
		this.appliedCures = appliedCures;
		this.appliedStatuses = appliedStatuses;
		
		this.buffs = buffs;
		this.debuffs = debuffs;
		
		this.postBoutInstruction = postBoutInstruction;
	}
	public boolean isHealing;
	public boolean isRevive;
	public int amount;
	public ElementType elementType;
	
	//The statuses to visualize during this combat anim
	public List<StatusType> appliedCures;
	public List<StatusType> appliedStatuses;
	
	public List<AttributeMod> buffs;
	public List<AttributeMod> debuffs;
	
	public PostBoutInstructionType postBoutInstruction;
}
