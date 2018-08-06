import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MaekawaCallable{
	static ArrayList<LinkedBlockingQueue<Message>> clientQueueList;
	static ArrayList<Integer> quorumIDList;
	static AtomicInteger csStatus;
	static AtomicInteger clock;
	//pass way to communicate with MaekawaProtocol thread
	public MaekawaCallable(ArrayList<LinkedBlockingQueue<Message>> cql, ArrayList<Integer> qidl, AtomicInteger status, AtomicInteger c)
	{
		clientQueueList=cql;
		quorumIDList=qidl;
		csStatus=status;
		clock=c;
	}

	//block until ok to enter cs then return
	public void enterCS()
	{
		while(csStatus.get()!=0) 
		{
			try {Thread.sleep(10);}
				catch(Exception e) {}  
		}
		csStatus.set(4);
		while(csStatus.get()!=2)  //loop until maekawaprotocol sets csStatus to 2 meaning this node can enter cs then return
		{
			try {Thread.sleep(10);}
				catch(Exception e) {}  
		}
	}

	//let MaekawaProtocol know application has left cs
	public void leaveCS()
	{
		csStatus.set(3);		
	}
}
