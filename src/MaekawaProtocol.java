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
	static CSRequest lockingRequest; //the request from the node that this node is locked by
	static int thisNodesID;
	static AtomicInteger clock;
	static boolean[] quorumGrants;  //tracks whether have received grant from each node in quorum, find index using quorumIDList
	public MaekawaProtocol(ArrayList<LinkedBlockingQueue<Message>> cql,LinkedBlockingQueue<Message> sq,
			ArrayList<Integer> qidl, AtomicInteger status, int id, AtomicInteger c)
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
		clock=c;
		quorumGrants=new boolean[quorumIDList.size()];
	}
	
	public void run(){
		while(true)
		{

			//check whether this node is just submitted a cs request
			if(csStatus.get()==4)
			{
				csStatus.set(1);
				sendRequests();
			}
			//check whether this node just finished cs
			if(csStatus.get()==3)
			{
				csStatus.set(0);
				receivedFailed=false;
				for(int i=0;i<quorumGrants.length;i++)
				{
					quorumGrants[i]=false;
				}
				System.out.println("broadcasting end of cs");	
				broadcastRelease();
			}
			
			Message newMessage=serverQueue.poll();
			
			checkForRequest(newMessage);

			checkForGrant(newMessage);
			
			checkForFailed(newMessage);
			
			checkForInquire(newMessage);
			
			checkForRelinquish(newMessage);
			
			checkForRelease(newMessage);
			
			checkCanEnterCS();
			
			
			
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
				lockingRequest=newRequest;
				Message grantMessage=new Message(thisNodesID,newRequest.getID(),"","GRANT",newRequest);
				
				clientQueueList.get(newRequest.getID()).add(grantMessage);
			}
			else if(lockedID!=-1&&comparator.compare(newRequest, lockingRequest)>0)  //new request has lower priority than request that holds lock
			{
				Message failedMessage=new Message(thisNodesID,newRequest.getID(),"","FAILED",newRequest);
				clientQueueList.get(newRequest.getID()).add(failedMessage);
			}
			else //new request has higher priority than request that holds lock, send inquire to the node holding lock
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
			if(newMessage.getRequest().getID()!=lockingRequest.getID())
			{
				System.out.println("ERROR, INVALID RELINQUISH.");
			}
				CSRequest topRequest=requestQueue.peek();
				lockedID=topRequest.getID();
				lockingRequest=topRequest;
				Message grantMessage= new Message(thisNodesID,topRequest.getID(),"","GRANT",topRequest);
				clientQueueList.get(topRequest.getID()).add(grantMessage);
		}
	}

	public void checkForRelease(Message newMessage)
	{
		if(newMessage!=null&&newMessage.getMessageType().equals("RELEASE"))
		{
			boolean requestFound;
			requestFound=requestQueue.remove(lockingRequest);		
			if(!requestFound)
			{
				System.out.println("ERROR, INCORRECT RELEASE.  Expected request not found.  ");
			}
			lockedID=-1;
			lockingRequest=null;
			if(!requestQueue.isEmpty())	//if there's another request in the queue send them grant since last request has finished
			{
				CSRequest topRequest=requestQueue.peek();
				lockedID=topRequest.getID();
				lockingRequest=topRequest;
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

	public void sendRequests()
	{
		CSRequest newRequest=new CSRequest(thisNodesID,clock);
		for(int i=0;i<quorumIDList.size();i++) //send request to all nodes in quorum (including self)
		{
			Message requestMessage=new Message(thisNodesID,quorumIDList.get(i),"","REQUEST",newRequest);
			clientQueueList.get(quorumIDList.get(i)).add(requestMessage);
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
