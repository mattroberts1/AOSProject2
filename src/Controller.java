import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicInteger;
public class Controller {
	static AtomicIntegerArray connectionEstablished;
	static ArrayList<LinkedBlockingQueue<Message>> clientQueueList;
	static LinkedBlockingQueue<Message> serverQueue;
	static int thisNodesID;
	static String thisNodesName;
	static Config conf;
	static ArrayList<Integer> quorumIDList;
	static AtomicInteger clock;
	static AtomicInteger csStatus; //0 for not waiting on cs, 1 for waiting, 2 for in cs
	public static void main(String[] args) {
		
		
		
		/*test code for priorityqueue and comparator
		MaekawaProtocol maekawaProtocol = new MaekawaProtocol();
		Thread mpThread = new Thread(maekawaProtocol);
		mpThread.start();
		try{Thread.sleep(10000);}catch(Exception e) {}
		*/		
		
		
		
		clock=new AtomicInteger(0);
		conf=new Config(args[0]);
		//find id of this node
		thisNodesName=getdcxxName();
		thisNodesID=getNodeID(thisNodesName);

		//create all the socket connections
		setupConnections();
		System.out.println("all nodes are online");
		
		//set up maekawa stuff
		quorumIDList=conf.getQuorumList().get(thisNodesID);
		
		
	}

	
	
	
	//establishes connections to all other nodes, blocks until all other nodes are online
	public static void setupConnections()
	{
		connectionEstablished = new AtomicIntegerArray(conf.getNumNodes());
		serverQueue=new LinkedBlockingQueue<Message>();
		//create server thread
		Server s = new Server(Integer.parseInt(conf.getNodeIDList()[thisNodesID][2]), serverQueue);
		Thread serverThread = new Thread(s);
		serverThread.start();
		//create client threads
		clientQueueList = new ArrayList<LinkedBlockingQueue<Message>>();
		for(int i=0;i<conf.getNumNodes();i++)
		{
			clientQueueList.add(new LinkedBlockingQueue<Message>());
			if(i!=thisNodesID)
			{
				String hostNamearg=conf.getNodeIDList()[i][1];
				int listenPortarg=Integer.parseInt(conf.getNodeIDList()[i][2]);
				Client c = new Client(i, hostNamearg, listenPortarg, clientQueueList.get(i), connectionEstablished, i);
				Thread clientThread= new Thread(c);
				clientThread.start();
			}
		}
		//wait for all nodes to come online
		boolean allConnected=false;
		while(!allConnected)
		{
			allConnected=true;
			for(int i=0;i<connectionEstablished.length();i++)
			{
				if(i!=thisNodesID) //don't connect node to itself
				{
					if(connectionEstablished.get(i)==0)
					{
						allConnected=false;
					}
				}
			}
		}
	}
	
	
	public static void makeCSRequest()
	{
	
		clock.incrementAndGet();
	}
	
	
	//returns dcxx part of host name for this node
	public static String getdcxxName()
	{
		String hostName="";
		try {
		hostName=InetAddress.getLocalHost().getHostName();
		}
		catch(Exception e)
		{	
			e.printStackTrace();
		}
		String[] temp = hostName.split("\\.");
		return temp[0];
	}
	
	//returns node id given name (dcxx)
	public static int getNodeID(String name)
	{
		int id=-1;
		for(int i=0;i<conf.getNodeIDList().length;i++)
		{
			if(name.equals(conf.getNodeIDList()[i][1]))
			{
				id=i;
			}
		}
		return id;
	}
}
