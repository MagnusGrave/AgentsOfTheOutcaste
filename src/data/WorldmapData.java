package data;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class WorldmapData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 270917473468346228L;
	
	public WorldmapData(Dimension worldGridDimension, Point startPoint, float worldMapWidth, float worldMapHeight, int tileWidth, int tileHeight, int rowCount, int columnCount, int heightInterval, Map<Point2D, WorldTileData> worldMapDatas) {
		this.worldGridDimension = worldGridDimension;
		this.startPoint = startPoint;
		this.worldMapWidth = worldMapWidth;
		this.worldMapHeight = worldMapHeight;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		this.rowCount = rowCount;
		this.columnCount = columnCount;
		this.heightInterval = heightInterval;
		this.worldMapDatas = worldMapDatas;
	}
	public Dimension worldGridDimension;
	public Point startPoint;
	public float worldMapWidth;
	public float worldMapHeight;
	public int tileWidth;
	public int tileHeight;
	public int rowCount;
	public int columnCount;
	public int heightInterval;
	private Map<Point2D, WorldTileData> worldMapDatas = new HashMap<Point2D, WorldTileData>();
	public Map<Point2D, WorldTileData> GetWorldMapDatas() {
		return worldMapDatas;
	}
	public void SetWorldMapDatas(Map<Point2D, WorldTileData> worldMapDatas) {
		this.worldMapDatas = worldMapDatas;
	}
	
	public void print() {
		System.out.println("_ WorldmapData _");
		System.out.println("worldMapWidth: " + worldMapWidth);
		System.out.println("worldMapHeight: " + worldMapHeight);
		System.out.println("tileWidth: " + tileWidth);
		System.out.println("tileHeight: " + tileHeight);
		System.out.println("rowCount: " + rowCount);
		System.out.println("columnCount: " + columnCount);
		System.out.println("heightInterval: " + heightInterval);
		System.out.println("worldMapDatas.count: " + worldMapDatas.size());
	}
}
