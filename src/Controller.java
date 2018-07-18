import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class Controller {
	static AtomicIntegerArray connectionEstablished;
	static ArrayList<LinkedBlockingQueue<Message>> clientQueueList;
	static LinkedBlockingQueue<Message> serverQueue;
	static int thisNodesID;
	static String thisNodesName;
	static Config conf;
	
	public static void main(String[] args) {
//TODO: remove args override
		args =new String[1];
		args[0]="src\\config.txt";
		conf=new Config(args[0]);
		//find id of this node
		thisNodesName=getdcxxName();
		for(int i=0;i<conf.getNodeIDList().length;i++)
		{
			if(thisNodesName.equals(conf.getNodeIDList()[i][1]))
			{
				thisNodesID=i;
			}
		}
		connectionEstablished = new AtomicIntegerArray(conf.getNumNodes());
		
//set up server and clients
		//create server thread
		Server s = new Server(Integer.parseInt(conf.getNodeIDList()[thisNodesID][2]), serverQueue);
		Thread serverThread = new Thread(s);
		serverThread.start();
		//create client threads
		for(int i=0;i<conf.getNumNodes();i++)
		{
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
		System.out.println("all nodes are online");
		
		

	}

	
	
	
	
	
	//returns dcxx part of host name
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
}
