import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.BufferedWriter;
public class Controller {
	static AtomicIntegerArray connectionEstablished;
	static long startTime;
	static ArrayList<LinkedBlockingQueue<Message>> clientQueueList;
	static LinkedBlockingQueue<Message> serverQueue;
	static int thisNodesID;
	static String thisNodesName;
	static Config conf;
	static ArrayList<Integer> quorumIDList;
	static AtomicInteger clock;
	static AtomicInteger messageCount;
	static AtomicInteger csStatus; //0 for not waiting on cs, 1 for waiting, 2 for in cs, 3 for just finished cs, 4 for just entered cs request
	static MaekawaCallable csGateway;
	static int requestsRemaining;
	public static void main(String[] args) {
		messageCount=new AtomicInteger(0);
		csStatus=new AtomicInteger(0);
		clock=new AtomicInteger(0);
		conf=new Config(args[0]);
		requestsRemaining=conf.getNumRequests();
		//find id of this node
		thisNodesName=getdcxxName();
		thisNodesID=getNodeID(thisNodesName);
		
		//create all the socket connections
		setupConnections();
		System.out.println("all nodes are online");
		
		checkQuorums();
		//set up maekawa stuff
		quorumIDList=conf.getQuorumList().get(thisNodesID);
		csGateway= new MaekawaCallable(clientQueueList, quorumIDList,csStatus,clock);
		MaekawaProtocol maekawaProtocol = new MaekawaProtocol(clientQueueList,serverQueue,quorumIDList,csStatus,thisNodesID,clock);
		Thread mpThread = new Thread(maekawaProtocol);
		mpThread.start();
		

		try {Thread.sleep(thisNodesID*500+1000);} //give some time to make sure all other nodes have maekawa stuff set up before starting communication
		catch(Exception e) {e.printStackTrace();}
		startTime=System.currentTimeMillis();
		
		//loop for application
		while(requestsRemaining>0)
		{
			
			doCSRequest();
			clock.incrementAndGet();
			requestsRemaining--;
			System.out.println("CS request executed.  Requests remaining: "+(int)requestsRemaining*100/conf.getNumRequests()+"%");
			try {Thread.sleep((long)getExpRandom(conf.getInterRequestDelay()));} //wait exp random time
			catch(Exception e) {e.printStackTrace();}
		}
		long runTime=System.currentTimeMillis()-startTime;
		try {Thread.sleep(5000);}
		catch(Exception e) {e.printStackTrace();}
		System.out.println("done.");
		System.out.println("Total messages sent by this node: "+messageCount.get());
		System.out.println("Process took "+runTime+"ms to complete");
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
			
				String hostNamearg=conf.getNodeIDList()[i][1];
				int listenPortarg=Integer.parseInt(conf.getNodeIDList()[i][2]);
				Client c = new Client(i, hostNamearg, listenPortarg, clientQueueList.get(i), connectionEstablished, i, messageCount);
				Thread clientThread= new Thread(c);
				clientThread.start();
			
		}
		//wait for all nodes to come online
		boolean allConnected=false;
		while(!allConnected)
		{
			allConnected=true;
			for(int i=0;i<connectionEstablished.length();i++)
			{
				
					if(connectionEstablished.get(i)==0)
					{
						allConnected=false;
					}
				
			}
		}
	}
	
	//asks maekawa protocol for permission to enter cs, blocks until permission granted, calls doCS, then tells mp node has left cs
	public static void doCSRequest()
	{
		csGateway.enterCS();
		doCS();
		csGateway.leaveCS();
	}
	
	//does printing to logfile and waiting for generated exponential random cs execution time
	public static void doCS()
	{
		try{FileWriter fw1 = new FileWriter("logfile", true);
			    BufferedWriter bw1 = new BufferedWriter(fw1);
			    PrintWriter out1 = new PrintWriter(bw1);
			    out1.println(thisNodesID+"-entercs");
			    out1.close();
		}
			 catch (Exception e) {e.printStackTrace();}
		System.out.println("Node "+thisNodesID+" doing CS.");
		try {Thread.sleep((long)getExpRandom(conf.getCSExecutionTime()));} //spend some time in cs
		catch(Exception e) {e.printStackTrace();}

		try{FileWriter fw2 = new FileWriter("logfile", true);
			BufferedWriter bw2 = new BufferedWriter(fw2);
			PrintWriter out2 = new PrintWriter(bw2);
			out2.println(thisNodesID+"-leavecs");
			out2.close();
			}
		catch (Exception e) {e.printStackTrace();}
	}
	
	//returns a random value with exponential distribution and mean of lampda
	public static double getExpRandom(int lampda)
	{
		return -lampda *Math.log(Math.random());

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
	public static void checkQuorums()
	{
		ArrayList<ArrayList<Integer>> quorumList=conf.getQuorumList();
		boolean validQuorum=true;
		for(int i=0;i<quorumList.size();i++)//for each node in system
		{
			for(int j=0;j<quorumList.size();j++)//compare to every other node in system
			{
				boolean pairOK=false;
				for(int iq=0;iq<quorumList.get(i).size();iq++)
				{
					for(int jq=0;jq<quorumList.get(j).size();jq++)
					{
						if(quorumList.get(i).get(iq)==quorumList.get(j).get(jq))
						{
							pairOK=true;
						}
					}
				}
				if(pairOK==false)
				{
					validQuorum=false;
				}
				
			}
		}
		if(validQuorum==false)
		{
		System.out.println("WARNING, INVALID QUORUM DETECTED.  ");
		}
	}
}
