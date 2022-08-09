Logic Bugs
==

- [ x ] MapLocationPanel doesn't properly set the player actor's layer when they start off doing a non-vertical movement into the scene.
- [ X ] While traveling the worldmap the player character is playing an attack animation instead of a walk anim. This is because the only anims available to it are battle related ones. We need to, instead, load up the SpriteSheetUtility.animFrameSocketMap with walk anims. It's being addressed in: BattleCharacterController.PlayAnim().
- [ X ] When arriving at the destination on the Worldmap an error occurs due to a reference in the player character's weapon anim.
- [ X ] The travel character is facing the wrong direction while their idle before the travel anim starts.
- [ X ] Characters can become moved to erroneous layers when a new dialography starts. -Fixed by calling UpdateActorRow after setting the characters into the scene layers in MapLocationPanel.StarDialography.
- [  ] Start adding random interactions at every scene. This will kickoff the balancing of the grinding and farming mechanics.
- [  ] When loading a game and entering the Resounding Aura mission the MapLocationPanel.activeMissionis null.
- [  ] Have the first Flee mission drop off the player in a forest with radius 2 or larger, a forest tile that also meets the criteria of the dynamic transition.


Character Obscurity Solution
==
This solution is to removal all "hideable" vertical layers aside from buildings. The main strategy here is to go thru these scenes and either move non-building vertical elements to the margins(the sides and back of the scene) where characters cannot be obscured, by reducing the collision bounds or expanding the scene bounds to add space for the margins, or to remove the vert elements altogether. While doing this the faked vertical topology of scenes needs to be addressed and replaced using the new tile sets. The Dark Forest scene size is now the standard to follow.

- [ X ] This was solved by tinkering with the MapLocationPanel.MigrateBattleCharToScenePane(), BattlePanel.SetPaneLayer() and BattlePanel.MoveAlongPath() methods to start characters at the right height in the scenePane by applying a blankRowOffset, to add a 2-3 tile vertical offset in some places and to adjust the layer setting procedure during movement.
- [ X ] Test this solution in other scenes to make sure there aren't more caveats necessary to make these updates consistent across all scenes.


Scenes to Rework
--
- [ X ] Foothills: Topology and vert elements
- [ X ] Plateau: Topology, vert elements and ground tiles touch up
- [ X ] Peak: Complete remake
- [  ] Forest: Vert Elements
- [  ] Forest Edge: Shrink Collision bounds
- [  ] Marsh: Vert Elements
- [  ] Garden: Vert Elements
- [  ] Dark Forest: Vert Elements
- [  ] Farmland Estate: Vert Elements
- [  ] Flower Garden: Remove vert elements
- [  ] Masamune Burial Grounds: Vert elements
- [ X ] Omoi Falls: Topology and Vert Element
- [  ] Sacred Hollow: Topology
- [  ] Made all scenes conform to Dark Forest size


MapLocation Scene Issues
==

General Issues
--
- [ x ] Actors' anim locations need to be offset based on the Settlement Layer offset.
- [  ] Elevation changes in nature scenes need to be expressed with the rock wall/ledge ground tiles instead of vertical mounds.
- [  ] The MapLocation's ScenePane needs to get its location layers removed between scene loads because the number of layers keep stacking up each time a new scene is loaded.
- [ X ] Fix the issue with offsetNewPoint being a pixel position and not a grid index, in the call: "SetPaneLayer(characterBase, offsetLastPoint, offsetNewPoint);" in BattlePanel.MoveAlongPath().moveTimer.ActionPerformed().
- [ X ] Debug MapLocationPanel.HandleBreakawayForCharacterMove() method to determine why character movements aren't registering within the breakaway zones.
- [ X ] The Estate SceneData exported from Unity is missing the activeCoordinates array for the north-most breakZone. This may be because breakZones are only searched for using the multi-tilemap convention "breakZone_breakaway_X.X" instead of looking for both that AND the single-tilemap convention "breakZone_breakaway_X".
  - [ X ] Another bug was introduced while fixing the parent task. Now the Estate_Testing sceneData export is experiencing a rendering issue on the Java side: no settlement layers are visible asside from the door animated tiles, only the nature scene and the doors are visible.
- [ X ] DISREGARDED - It seems like the breakaway zone detection logic will need to invert newLocation's Y axis, as the SceneData activeCoordinates are recorded using the bottom-left origin orientation.
- [ X ] There's an issue with the bounds tiles grid size clashing with the collision tiles grid size. An attempted fix failed in Board's constructor during board tile creation; it only created more errors. Further understanding is required. Further understanding has been achieved and I now know the trick will be to expand the boardDimensions when the nature scene is bigger than the settlement scene but I still haven't figured out the right formula. It turned out to be the difference in even/odd widths and/or heights of the boardDimensioins and the settlementSize. So if they dont match we add 1 to the width and/or height.
- [ X ] On the battle panel Board all the settlement tiles have incorrect coordinates displayed when hovering over them, they need to be offset. It seems that it may be possible to edit the position variables of the TileData when the scene layers are being merged. I tried editing the TileData position variables but that desynced the relationships to other data-bound coords, like breakaway zones etc, so to fix this bug I made a new set of position variables in TileData to record the offset position and displayed that in the terrainPanel.
- [ X ] MISINTERPRETATION: The height placement of doors and battle characters at the start of the Dialography and BattlePanel needs to be shifted up along with the settlement layer in any combo scene. ACTUAL PROBLEM: The problem is that the placement of the settlement image components is unaligned with everything else. The actors and visual tiles, settlement scene collision tiles and nature scene are all in the correct locations. This bug was fixed by editing the settlement component placement in MapLocationPanel.AssembleScene().
- [  ] Tall scenes(primarially the Mountain scene) seem to misalign the Breakaway Zones and also wreak havok on character movement calculation.
- [ X ] Battle characters UI elements need to be destroyed or hidden when the battle ends.
- [  ] Dialographies don't seem to put characters on the correct layers. Deeper characters render in front of more shallow characters. Fortunately though, both seem to get layered correctly amongst the scene elements.

Scene Specific Issues
--
- [ x ] Castle Scene(With any Nature Layer): Only the breakaway roof is appearing.
- [ x ] Farmland Estate Combo Scene: Needs to be re-exported so the animated tiles will be shown.
- [ x ] Peak + (Buddha) Shrine: The shrine's animated tiles don't appear.
- [ x ] Market_Outcaste(With any Nature Layer): The actors are incorrectly offset to the right by 1 or 2 tiles.
- [  ] Market_Outcaste(With any Nature Layer): The actors are being put on too shallow of a layer; rendering them in front of their stalls.
- [  ] Make a new custom scene for Clifftop Outlook that facilitates being kicked off a cliff into a river below.