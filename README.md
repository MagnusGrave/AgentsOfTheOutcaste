# AgentsOfTheOutcaste

A tactics RPG game set in a mystical Sengoku Period Feudal Japan.

This game project is 2.5 years in the making. My life-long love of the Final Fantasy Tactics series has driven me to make my own tactics game. After many attempts I've finally built up all the skills necessary to pull off a project as ambitious as this one. 

The infustructure of this game is built entirely on the Swing UI framework. 

Nearly all graphics are free or paid assets from the Time Fantasy character and tile sets. Some may recognize these assets made popular by RPG Maker. Check out these all around fantastic assets: https://finalbossblues.itch.io/. All other graphics were acquired via Unity Asset Store or were made using Affinity Photo.

The soundtrack was out-sourced to a phenomenal composer: https://www.youtube.com/user/KydenXuD. Each track was tailor made to fit perfectly into the flow of the game and to meet any mood. This soundtrack may be available alongside the game's steam launch.

One of my favoite aspects of this project, a development endeavor that's completely new to me, is that it has a disjointed content creation engine. This "level editor" component is built using the Unity game engine. Leveraging Unity's built-in tile editor tools one can create levels. But that's not all. Making use of Unity's accesible Inspector and component based structure one can create any data class in the game; ranging from granular data, such as Weapon and Consumable Items, to more complex data, like dialogs and cut scenes, and even the game's narrative. All levels, or scenes as they're referred to in Unity, are exported from Unity as a combination of images and a scene data json file. The data file contains pertinent info about the scene, included information interpreted from many "helper" Tilemap layers; collision, bounds, breakaway scenery and depth information. All Granular data, characters and items are exported as one json data file. All other compresensive data such as dialogs, cutscene animations, events and missions are exported into groups determined by their host scene. Once this content is created it is added to specific project folders and read at the start of the game. When players make a new game an entire world is procedurally generated using the aforementioned modular game content. My hopes is that this game will stand apart due to its unconventional application of a linear story line in an procedural setting. During each play-through the storyline content will remain the same but where the events occur on the world map are unique. My hopes are that this synthesis of game genres will breath new life into linear RPGs.

All the item data in the game was written in Google Sheets, exported into an intermediate json file and then converted into json data structures suited to direct parsing into native item classes. The json parsing is done with a json tool available on the Eclipse Marketplace. Several google apps scripts were written to automate tasks such as populating each items image path and the assignment of a random GUID.


Currently, the main mechanics of this game are being buttoned up. The full game playloop is mostly functioning. My goal is to get a stable alpha by the end of this year. Once that's complete, I will move on to creating the actual characters, dialog, cut scenes and missions. I expect the early access beta to launch by 2024.
