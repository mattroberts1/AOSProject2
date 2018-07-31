
import java.util.concurrent.atomic.AtomicInteger;
import java.io.Serializable;
public class CSRequest implements Serializable{
	private static final long serialVersionUID = 1L;

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
