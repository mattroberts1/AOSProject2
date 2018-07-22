import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.PriorityBlockingQueue;
public class MaekawaProtocol implements Runnable{
	Comparator<CSRequest> comparator;
	PriorityBlockingQueue<CSRequest> requestQueue;
	public MaekawaProtocol()
	{
		comparator = new CSRequestComparator();
		requestQueue = new PriorityBlockingQueue<CSRequest>(1,comparator);
	}
	
	public void run(){
		/* Test code for priorityqueue and comparator
		AtomicInteger tsmin= new AtomicInteger(5);
		AtomicInteger tsmid= new AtomicInteger(7);
		AtomicInteger tsmax= new AtomicInteger(9);
		CSRequest r1=new CSRequest(7,tsmin);
		CSRequest r2=new CSRequest(7,tsmid);
		CSRequest r3=new CSRequest(9,tsmid);
		CSRequest r4=new CSRequest(7,tsmax);
		requestQueue.add(r4);
		requestQueue.add(r3);
		requestQueue.add(r2);
		requestQueue.add(r1);
		
		while(!requestQueue.isEmpty())
		{
			CSRequest r=requestQueue.poll();
			System.out.println(r.getID()+" "+r.getTimestamp());
			
		}
		
		*/
	}
}
