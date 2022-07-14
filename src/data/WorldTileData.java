package data;

import java.awt.geom.Point2D;
import java.io.Serializable;

import enums.EnvironmentType;
import enums.SettlementDesignation;
import enums.SettlementType;
import enums.WorldTileType;
import gameLogic.MapLocation;
import gui.WorldmapPanel.WorldTile;

public class WorldTileData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1029071741682739120L;

	public WorldTileData(WorldTile tile) {
		this.enviType = tile.getEnviType();
		this.tileType = tile.getTileType();
		this.settlementType = tile.GetSettlementType();
		this.settlementDesignation = tile.GetSettlementDesignation();
		this.blankTerrainImageIndex = tile.getBlankTerrainImageIndex();
		this.terrainImageIndex = tile.getTerrainImageIndex();
		this.position = tile.getPosition();
		this.isBlank = tile.IsBlank();
		this.isEpicenter = tile.IsEpicenter();
		this.isRiver = tile.IsRiver();
		this.isLake = tile.IsLake();
		this.isBay = tile.IsBay();
		this.isDiscovered = tile.isDiscovered();
		this.mapLocation = tile.GetMapLocation();
		this.isUniqueLocation = tile.IsUniqueLocation();
	}
	
	public EnvironmentType enviType;
	public WorldTileType tileType;
	public SettlementType settlementType;
	public SettlementDesignation settlementDesignation;
	
	public int blankTerrainImageIndex;
	public int terrainImageIndex;
	public Point2D position;
	
	public boolean isBlank;

	public boolean isEpicenter;

	public boolean isRiver;
	public boolean isLake;
	public boolean isBay;
	
	public boolean isDiscovered;
	
	public MapLocation mapLocation;
	public boolean isUniqueLocation;
}