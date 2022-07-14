package gui;

import javax.sound.sampled.*;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import gameLogic.Game;

import java.io.*;

public class BGMusicThread extends Thread implements ChangeListener {

	@Override
	public void run() {
		BufferedInputStream in = new BufferedInputStream(this.getClass().getClassLoader().getResourceAsStream("resources/Music/Capital_punishment_final.wav"));
		
		AudioInputStream as1;
		try {
			//as1 = AudioSystem.getAudioInputStream(file);
			as1 = AudioSystem.getAudioInputStream(in);
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
			return;
		}
		
		AudioFormat af = as1.getFormat();
		Clip clip1;
		try {
			clip1 = AudioSystem.getClip();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			return;
		}
		DataLine.Info info = new DataLine.Info(Clip.class, af);
		Line line1;
		try {
			line1 = AudioSystem.getLine(info);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			return;
		}
		
		//Uncomment once sound design process begins
		/*if (!line1.isOpen()) {
			try {
				clip1.open(as1);
			} catch (LineUnavailableException | IOException e) {
				e.printStackTrace();
			}
			clip1.loop(Clip.LOOP_CONTINUOUSLY);
			clip1.start();
			
			gainControl = (FloatControl) clip1.getControl(FloatControl.Type.MASTER_GAIN);
			//Load settings
			SetVolume(Game.Instance().GetBGMusicVolume());
		}*/
	}
	
	public void stateChanged(ChangeEvent e) {
	    JSlider source = (JSlider)e.getSource();
	    if (!source.getValueIsAdjusting()) {
	        SetVolume(source.getValue());
	    }
	}
	
	FloatControl gainControl;
	
	private void SetVolume(int newVolume) {
		if(gainControl == null)
			return;
		
		float normVolume = (float)newVolume / 100f;
		
		float range = gainControl.getMaximum() - gainControl.getMinimum();
		float gain = (range * normVolume) + gainControl.getMinimum();
		gainControl.setValue(gain);
	}
}
