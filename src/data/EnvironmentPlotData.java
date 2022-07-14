package data;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import enums.EnvironmentType;
import gui.WorldmapPanel.EnvironmentPlot;
import gui.WorldmapPanel.WorldTile;

public class EnvironmentPlotData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2958677795095143738L;
	
	public EnvironmentPlotData(EnvironmentPlot plot) {
		enviType = plot.getEnviType();
		epicenter = plot.getEpicenter();
		range = plot.getRange();
		for(WorldTile tile : plot.getTiles())
			tileDatas.add(tile.getData());
	}
	public EnvironmentType enviType;
	public Point2D epicenter;
	public int range;
	public List<WorldTileData> tileDatas = new ArrayList<WorldTileData>();
}
