import java.util.Comparator;

public class CSRequestComparator implements Comparator<CSRequest>{

	//returns positive if r1 has lower priority than r2 (r1 should be put after r2 in queue)
	@Override
	public int compare(CSRequest r1, CSRequest r2)
	{
		if(r1.getTimestamp().get()>r2.getTimestamp().get())
			return 1;
		if(r1.getTimestamp().get()==r2.getTimestamp().get()&&r1.getID()>r2.getID())
			return 1;
		if(r1.getTimestamp().get()<r2.getTimestamp().get())
			return -1;
		if(r1.getTimestamp().get()==r2.getTimestamp().get()&&r1.getID()<r2.getID())
			return -1;
		return 0;
	}
}
