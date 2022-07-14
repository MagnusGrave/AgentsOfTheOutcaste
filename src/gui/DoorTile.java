package gui;

import data.SceneData.VisualTileData;

public class DoorTile {
	public DoorTile(VisualTileData visualTileData, SpriteSheet spriteSheet, ImagePanel tilePanel) {
		this.visualTileData = visualTileData;
		this.spriteSheet = spriteSheet;
		this.tilePanel = tilePanel;
	}
	
	private VisualTileData visualTileData;
	public VisualTileData GetVisualTileData() { return visualTileData; }
	private SpriteSheet spriteSheet;
	private ImagePanel tilePanel;

    //Runtime variables
    private boolean isAssetAnimating;
    public boolean IsAssetAnimating() { return isAssetAnimating; }
    private boolean isAnimatingClosed;
    private int frameIndex;

    public void StartAnim(boolean animateOpen)
    {
        isAssetAnimating = true;
        isAnimatingClosed = !animateOpen;
        frameIndex = 0;
    }
    
    public void Reset() {
		tilePanel.SetNewImage(spriteSheet.GetSprite(visualTileData.m_AnimatedSprites_names[0]), MapLocationPanel.getUpdateInterval_ms());
		isAssetAnimating = false;
		isAnimatingClosed = false;
		frameIndex = 0;
	}

    public boolean FrameStep()
    {
        frameIndex++;

        int frameSetCount = isAnimatingClosed ? visualTileData.m_DoorClosedSprites_names.length : visualTileData.m_AnimatedSprites_names.length;
        
        UpdateTileImage();
        
        if (frameIndex < frameSetCount - 1)
        {
        	return false;
        }
        else
        {
        	EndAnim();
            return true;
        }
    }
    
    public void UpdateTileImage()
    {
        if(
           (isAnimatingClosed && frameIndex < visualTileData.m_DoorClosedSprites_names.length)
           ||
           (!isAnimatingClosed && frameIndex < visualTileData.m_AnimatedSprites_names.length)
        )
        	tilePanel.SetNewImage(
        			spriteSheet.GetSprite( isAnimatingClosed ? visualTileData.m_DoorClosedSprites_names[frameIndex] : visualTileData.m_AnimatedSprites_names[frameIndex] ),
        			MapLocationPanel.getUpdateInterval_ms()
        			);
    }
    
    public void EndAnim()
    {
        isAssetAnimating = false;
        isAnimatingClosed = false;
        frameIndex = 0;
    }
}
