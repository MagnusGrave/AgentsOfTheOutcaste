package gui;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;

import enums.EnvironmentType;
import enums.SettlementType;
import gameLogic.Game;
import gui.WorldmapPanel.WorldTile;


@SuppressWarnings("serial")
public class MyFrame extends JFrame {
	private class MyDispatcher implements KeyEventDispatcher {
		boolean isHoldingAlt;
		
		//Developer Helper Variables
		private int forestTeleportEpicenterIndex;
		private int gardenTeleportIndex;
		
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
        	boolean isAlt = KeyEvent.VK_ALT == e.getKeyCode();
        	
            if (e.getID() == KeyEvent.KEY_PRESSED) {
            	//For featured gameplay commands
            	
            	
        		if(isAlt)
        			isHoldingAlt = true;
        		if(isHoldingAlt) {
        			//For developer commands that can execute regardless of the DEBUG_allowDevHotkeys setting
    				if(e.getKeyChar() == 's') {
    					System.out.println("Developer Helper Command - ALT + s - Regen Stamina");
    					Game.Instance().SetPartyStamina(WorldmapPanel.partyStaminaMax);
    					GUIManager.WorldmapPanel().RefreshStaminaBar();
    				} else if(e.getKeyChar() == 'f') {
    					WorldTile[] forestEpicenters = GUIManager.WorldmapPanel().worldMap.values().stream().filter(x -> x.getEnviType() == EnvironmentType.Forest && x.IsEpicenter()).toArray(WorldTile[]::new);
    					if(forestTeleportEpicenterIndex >= forestEpicenters.length)
    						forestTeleportEpicenterIndex = 0;
    					System.out.println("Developer Helper Command - ALT + f - Teleport to next forest epicenter, sceneDir: " + forestEpicenters[forestTeleportEpicenterIndex].GetMapLocation().getSceneDirectory()
    							+ ", Combo Nature Directory: " + forestEpicenters[forestTeleportEpicenterIndex].GetMapLocation().getComboNatureSceneDirectory()
    							+ ", Combo Settlement Directory: " + forestEpicenters[forestTeleportEpicenterIndex].GetMapLocation().getComboSettlementSceneDirectory());
    					GUIManager.WorldmapPanel().MoveToNewTile(forestEpicenters[forestTeleportEpicenterIndex].GetMapLocation());
    					forestTeleportEpicenterIndex++;
    				} else if(e.getKeyChar() == 'g') {
    					WorldTile[] gardens = GUIManager.WorldmapPanel().worldMap.values().stream().filter(x -> x.GetSettlementType() == SettlementType.Garden).toArray(WorldTile[]::new);
    					if(gardenTeleportIndex >= gardens.length)
    						gardenTeleportIndex = 0;
    					System.out.println("Developer Helper Command - ALT + g - Teleport to next gardens, sceneDir: " + gardens[gardenTeleportIndex].GetMapLocation().getSceneDirectory()
    							+ ", Combo Nature Directory: " + gardens[gardenTeleportIndex].GetMapLocation().getComboNatureSceneDirectory()
    							+ ", Combo Settlement Directory: " + gardens[gardenTeleportIndex].GetMapLocation().getComboSettlementSceneDirectory());
    					GUIManager.WorldmapPanel().MoveToNewTile(gardens[gardenTeleportIndex].GetMapLocation());
    					gardenTeleportIndex++;
    				}
        			
    				//For commands that only function when facilitated by special screen settings that're enacted only when DEBUG_allowDevHotkeys is equal to true
        			if(GUIManager.DEBUG_allowDevHotkeys) {
	        			if(e.getKeyChar() == 'p')
	        				GUIUtil.CaptureScreenshot();
	        			else if(e.getKeyChar() == '[')
	        				GUIManager.CycleScreens();
        			}
        		}
            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
            	if(isAlt)
            		isHoldingAlt = false;
            }
            return false;
        }
    }
    
    public MyFrame(String title) {
        super(title);
        	
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new MyDispatcher());
        
        //It is advised to disable resizing for full screen exclusive mode applications
        this.setResizable(false);
    }
}
