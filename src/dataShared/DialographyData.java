package dataShared;

import java.io.Serializable;
import java.util.List;

public class DialographyData implements Serializable {
	private static final long serialVersionUID = 7122577220294237293L;
	
    public String dialographyName;
    public DialogLine[] dialogLines;
    public String locationDescSummary;
    public List<ActorPathData> actorPathDatas;
}
