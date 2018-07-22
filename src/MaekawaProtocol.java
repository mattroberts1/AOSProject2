import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
public class MaekawaProtocol implements Runnable{
	Comparator<CSRequest> comparator;
	PriorityBlockingQueue<CSRequest> requestQueue;
	static ArrayList<LinkedBlockingQueue<Message>> clientQueueList;
	static LinkedBlockingQueue<Message> serverQueue;
	static LinkedBlockingQueue<Message> interThreadComm;
	static ArrayList<Integer> quorumIDList;
	public MaekawaProtocol(ArrayList<LinkedBlockingQueue<Message>> cql,LinkedBlockingQueue<Message> sq,LinkedBlockingQueue<Message> itc,ArrayList<Integer> qidl )
	{
		clientQueueList=cql;
		serverQueue=sq;
		comparator = new CSRequestComparator();
		requestQueue = new PriorityBlockingQueue<CSRequest>(1,comparator);
		interThreadComm=itc;
		quorumIDList=qidl;
	}
	
	public void run(){

	}
}
