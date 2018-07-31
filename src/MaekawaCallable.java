import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MaekawaCallable{
	static ArrayList<LinkedBlockingQueue<Message>> clientQueueList;
	static ArrayList<Integer> quorumIDList;
	static AtomicInteger csStatus;
	//pass way to communicate with MaekawaProtocol thread
	public MaekawaCallable(ArrayList<LinkedBlockingQueue<Message>> cql, ArrayList<Integer> qidl, AtomicInteger status)
	{
		clientQueueList=cql;
		quorumIDList=qidl;
		csStatus=status;
	}

	//block until ok to enter cs then return
	public void enterCS(int id, AtomicInteger ts)
	{
		CSRequest newRequest=new CSRequest(id,ts);
		for(int i=0;i<quorumIDList.size();i++) //send request to all nodes in quorum (including self)
		{
			Message requestMessage=new Message(id,quorumIDList.get(i),"","REQUEST",newRequest);
			clientQueueList.get(quorumIDList.get(i)).add(requestMessage);
		}
		csStatus.set(1);
		while(csStatus.get()!=2)  //loop until maekawaprotocol sets csStatus to 2 meaning this node can enter cs then return
		{
			try {Thread.sleep(10);}
				catch(Exception e) {}  
		}
	}

	//let MaekawaProtocol know application has left cs
	public void leaveCS()
	{
		csStatus.set(0);		
	}
}
