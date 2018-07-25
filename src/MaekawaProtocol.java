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
	static LinkedBlockingQueue<CSRequest> interThreadComm;
	static ArrayList<Integer> quorumIDList;
	static AtomicInteger csStatus;
	public MaekawaProtocol(ArrayList<LinkedBlockingQueue<Message>> cql,LinkedBlockingQueue<Message> sq,LinkedBlockingQueue<CSRequest> itc,
			ArrayList<Integer> qidl, AtomicInteger status)
	{
		clientQueueList=cql;
		serverQueue=sq;
		comparator = new CSRequestComparator();
		requestQueue = new PriorityBlockingQueue<CSRequest>(1,comparator);
		interThreadComm=itc;
		quorumIDList=qidl;
		csStatus=status;
	}
	
	public void run(){
		while(true)
		{
			//check if received request from this node
			CSRequest myRequest=interThreadComm.poll();
			if(myRequest!=null)
			{
				requestQueue.add(myRequest);
			}
			

			
			
			
			
		}

	}
}
