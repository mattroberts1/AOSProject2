import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
public class CSRequest{

	int nodeID;
	AtomicInteger timestamp;
	
	public CSRequest(int id, AtomicInteger ts)
	{
		nodeID=id;
		timestamp=ts;
	}

	public int getID()
	{
		return nodeID;
	}
	public AtomicInteger getTimestamp()
	{
		return timestamp;
	}
}
