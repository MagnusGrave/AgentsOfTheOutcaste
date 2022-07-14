package data;


/**
 * Interweave an anim trigger type system that carries commands to the BattleCharacterController to do things at the start or end of a frame.
 * Things like Swapping images on the weapon sprite, or hiding/showing it.
 * @author Magnus
 */
public class AnimCommand {
	public enum CommandType { SwapImage, Show, Hide }
	
	/**
	 * For Show and Hide CommandTypes
	 * @param command
	 */
	public AnimCommand(CommandType command) {
		this.command = command;
	}
	
	/**
	 * For SwapImage CommandType.
	 * @param command
	 * @param swapImagePath
	 */
	public AnimCommand(String swapImagePath) {
		this.command = CommandType.SwapImage;
		this.swapImagePath = swapImagePath;
	}
	
	public CommandType command;
	public String swapImagePath;
}