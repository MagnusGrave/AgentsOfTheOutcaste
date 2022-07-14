package dataShared;

import java.io.Serializable;
import java.util.List;

public class LocationContentData implements Serializable {
	private static final long serialVersionUID = 8004385503421210474L;

	public class MissionStructure implements Serializable {
		private static final long serialVersionUID = -3156965947857117148L;
		
		public MissionData missionData;
        public List<InteractionData> interactionDatas;
        public List<DialographyData> interactionlessDialographyDatas;
        public BattleData interactionlessBattleData;
    }
	
    //public MissionStructure[] missionStructures;
    
    //public MapLocationData mapLocationData;
    //public InteractionData[] mapLocation_interactionDatas;
    //public DialographyData[] mapLocation_interactionlessDialographyDatas;
    //public BattleData mapLocation_interactionlessBattleData;
    public class MapLocationStructure implements Serializable {
		private static final long serialVersionUID = -2870455632647376901L;
		
		public MapLocationData mapLocationData;
        public List<InteractionData> interactionDatas;
        public List<DialographyData> interactionlessDialographyDatas;
        public BattleData interactionlessBattleData;
        public List<MissionStructure> missionStructures;
    }
    public List<MapLocationStructure> mapLocationStructures;
	
    
    public ActorData[] actorDatas;
}

/*[System.Serializable]
public class LocationContentData
{
    [System.Serializable]
    public struct MissionStructure
    {
        public MissionStructure(MissionDataSlot missionDataSlot)
        {
            missionData = missionDataSlot.missionData.CollectExportData();
            interactionDatas = new List<InteractionDataSlot.InteractionData>();
            interactionlessDialographyDatas = new List<Dialography.DialographyData>();
            interactionlessBattleData = null;

            //Sort each child into the appropriate place
            for (int i = 0; i < missionDataSlot.transform.childCount; i++)
            {
                GameObject go = missionDataSlot.transform.GetChild(i).gameObject;
                InteractionDataSlot interSlot = go.GetComponent<InteractionDataSlot>();
                Dialography diaSlot = go.GetComponent<Dialography>();
                BattleDataSlot battleSlot = go.GetComponent<BattleDataSlot>();
                if (interSlot != null)
                    interactionDatas.Add(interSlot.data.CollectExportData());
                else if (diaSlot != null)
                    interactionlessDialographyDatas.Add(new Dialography.DialographyData(diaSlot));
                else if (battleSlot != null)
                    interactionlessBattleData = battleSlot.battleData.CollectExportData();
            }
        }
        public MissionDataSlot.MissionData missionData;
        public List<InteractionDataSlot.InteractionData> interactionDatas;
        public List<Dialography.DialographyData> interactionlessDialographyDatas;
        public BattleData interactionlessBattleData;
    }

    [System.Serializable]
    public struct MapLocationStructure
    {
        public MapLocationStructure(MapLocationSlot mapLocationSlot)
        {
            mapLocationData = mapLocationSlot.mapLocationData.CollectExportData();
            interactionDatas = new List<InteractionDataSlot.InteractionData>();
            interactionlessDialographyDatas = new List<Dialography.DialographyData>();
            interactionlessBattleData = null;
            missionStructures = new List<MissionStructure>();

            //Sort each child into the appropriate place
            for (int i = 0; i < mapLocationSlot.transform.childCount; i++)
            {
                GameObject go = mapLocationSlot.transform.GetChild(i).gameObject;
                InteractionDataSlot interSlot = go.GetComponent<InteractionDataSlot>();
                Dialography diaSlot = go.GetComponent<Dialography>();
                BattleDataSlot battleSlot = go.GetComponent<BattleDataSlot>();
                MissionDataSlot missionSlot = go.GetComponent<MissionDataSlot>();
                if (interSlot != null)
                    interactionDatas.Add(interSlot.data.CollectExportData());
                else if (diaSlot != null)
                    interactionlessDialographyDatas.Add(new Dialography.DialographyData(diaSlot));
                else if (battleSlot != null)
                    interactionlessBattleData = battleSlot.battleData.CollectExportData();
                else if(missionSlot != null)
                    missionStructures.Add(new MissionStructure(missionSlot));
            }
        }
        public MapLocationSlot.MapLocationData mapLocationData;
        public List<InteractionDataSlot.InteractionData> interactionDatas;
        public List<Dialography.DialographyData> interactionlessDialographyDatas;
        public BattleData interactionlessBattleData;
        public List<MissionStructure> missionStructures;
    }
    public List<MapLocationStructure> mapLocationStructures = new List<MapLocationStructure>();

    public ActorData[] actorDatas;
}*/
