package gameLogic;

import javax.swing.SwingUtilities;

import gui.GUIManager;



public class Main_RAO {
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new Game();
				//new GUIManager();
				GUIManager.Initialize();
			}
		});
	}
}
