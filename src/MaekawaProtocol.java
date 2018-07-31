import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
public class MaekawaProtocol implements Runnable{
	Comparator<CSRequest> comparator;
	PriorityBlockingQueue<CSRequest> requestQueue; //requests will remain in queue until receive 
	static ArrayList<LinkedBlockingQueue<Message>> clientQueueList;
	static LinkedBlockingQueue<Message> serverQueue;
	static ArrayList<Integer> quorumIDList;
	static AtomicInteger csStatus;
	static boolean receivedFailed;
	static int lockedID; //id of node this node is locked by (or -1 if not locked)
	static int thisNodesID;
	static boolean[] quorumGrants;  //tracks whether have received grant from each node in quorum, find index using quorumIDList
	public MaekawaProtocol(ArrayList<LinkedBlockingQueue<Message>> cql,LinkedBlockingQueue<Message> sq,
			ArrayList<Integer> qidl, AtomicInteger status, int id)
	{
		lockedID=-1;
		receivedFailed=false;
		clientQueueList=cql;
		serverQueue=sq;
		comparator = new CSRequestComparator();
		requestQueue = new PriorityBlockingQueue<CSRequest>(1,comparator);
		quorumIDList=qidl;
		csStatus=status;
		thisNodesID=id;
		quorumGrants=new boolean[quorumIDList.size()];
	}
	
	public void run(){
		while(true)
		{
			Message newMessage=serverQueue.poll();
			
			checkForRequest(newMessage);

			checkForGrant(newMessage);
			
			checkForFailed(newMessage);
			
			checkForInquire(newMessage);
			
			checkForRelinquish(newMessage);
			
			checkForRelease(newMessage);
			
			checkCanEnterCS();
			
			//check whether this node just finished cs
			if(csStatus.get()==0&&!requestQueue.isEmpty()&&requestQueue.peek().getID()==thisNodesID)
			{
				System.out.println("broadcasting end of cs");	
				broadcastRelease();
			}
			
		}//end of while loop
		
	}//end of run method
	
	//if next message in queue is a new cs request handle it
	public void checkForRequest(Message newMessage)
	{
		if(newMessage!=null&&newMessage.getMessageType().equals("REQUEST"))
		{
			CSRequest newRequest=newMessage.getRequest();
			if(requestQueue.isEmpty())//no requests currently in queue
			{
				lockedID=newRequest.getID();
				Message grantMessage=new Message(thisNodesID,newRequest.getID(),"","GRANT",newRequest);
				clientQueueList.get(newRequest.getID()).add(grantMessage);
			}
			else if(comparator.compare(newRequest, requestQueue.peek())>0)  //new request has lower priority than request at head of queue
			{
				Message failedMessage=new Message(thisNodesID,newRequest.getID(),"","FAILED",newRequest);
				clientQueueList.get(newRequest.getID()).add(failedMessage);
			}
			else //new request has higher priority than request at head of queue, send inquire to node at head of queue
			{
				Message inquireMessage=new Message(thisNodesID,lockedID,"","INQUIRE",newRequest);
				clientQueueList.get(lockedID).add(inquireMessage);
			}
			 //if request came from this node, keep track of whether have received grants from all quorum nodes or received a failed message
			if(newRequest.getID()==thisNodesID)			
			{
				receivedFailed=false;
				for(int i=0;i<quorumGrants.length;i++)
				{
					quorumGrants[i]=false;
				}
			}		
			requestQueue.add(newRequest); //always add request to queue
		}
	}

	public void checkForGrant(Message newMessage)
	{
		if(newMessage!=null&&newMessage.getMessageType().equals("GRANT")&&csStatus.get()==1)
		{
			int index=-1;
			for(int i=0;i<quorumIDList.size();i++)
			{
				if(quorumIDList.get(i)==newMessage.getSender())
				{
					index=i;
				}
			}
			quorumGrants[index]=true;
		}
		if(newMessage!=null&&newMessage.getMessageType().equals("GRANT")&&csStatus.get()!=1)
		{
			System.out.println("ERROR, RECEIVED UNEXPECTED GRANT MESSAGE.");
		}
	}

	public void checkForFailed(Message newMessage)
	{
		if(newMessage!=null&&newMessage.getMessageType().equals("FAILED"))
		{
			receivedFailed=true;
		}
	}

	public void checkForInquire(Message newMessage)
	{
		if(newMessage!=null&&newMessage.getMessageType().equals("INQUIRE"))
		{
			CSRequest includedRequest=newMessage.getRequest();
			if(receivedFailed) //give up lock
			{
				int index=-1;
				for(int i=0;i<quorumIDList.size();i++)
				{
					if(quorumIDList.get(i)==newMessage.getSender())
					{
						index=i;
					}
				}
				quorumGrants[index]=false;
				Message relinquishMessage=new Message(thisNodesID,newMessage.getSender(),"","RELINQUISH",includedRequest);
				clientQueueList.get(newMessage.getSender()).add(relinquishMessage);
			}
		}
		
	}

	public void checkForRelinquish(Message newMessage)
	{
		if(newMessage!=null&&newMessage.getMessageType().equals("RELINQUISH"))
		{
				CSRequest topRequest=requestQueue.peek();
				lockedID=topRequest.getID();
				Message grantMessage= new Message(thisNodesID,topRequest.getID(),"","GRANT",topRequest);
				clientQueueList.get(topRequest.getID()).add(grantMessage);
		}
	}

	public void checkForRelease(Message newMessage)
	{
		if(newMessage!=null&&newMessage.getMessageType().equals("RELEASE"))
		{
			CSRequest removedRequest=requestQueue.poll();
			if(removedRequest.getID()!=newMessage.getSender())
			{
				System.out.println("ERROR, INCORRECT RELEASE.");
			}
			lockedID=-1;
			if(!requestQueue.isEmpty())	//if there's another request in the queue send them grant since last request has finished
			{
				CSRequest topRequest=requestQueue.peek();
				lockedID=topRequest.getID();
				Message grantMessage= new Message(thisNodesID,topRequest.getID(),"","GRANT",topRequest);
				clientQueueList.get(topRequest.getID()).add(grantMessage);
			}
		}
	}

	public void checkCanEnterCS()
	{
		if(csStatus.get()==1)
		{
			boolean enterCS=true;
			for(int i=0;i<quorumGrants.length;i++)
			{
				if(quorumGrants[i]==false)
				{
					enterCS=false;
				}
			}
			if(enterCS)
			{
				System.out.println("granting permission for node "+thisNodesID+" to enter cs.");	
				csStatus.set(2);
			}
		}
	}

	//sends release message to all nodes in quorum
	public static void broadcastRelease()
	{
		for(int i=0;i<quorumIDList.size();i++)
		{
			Message releaseMessage=new Message(thisNodesID,quorumIDList.get(i),"","RELEASE",null);
			clientQueueList.get(quorumIDList.get(i)).add(releaseMessage);
		}
	}
}
