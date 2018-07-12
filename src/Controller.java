import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class Controller {
	static AtomicIntegerArray connectionEstablished;
	static ArrayList<LinkedBlockingQueue<Message>> clientQueueList;
	static LinkedBlockingQueue<Message> serverQueue;
	static int thisNodesID;
	
	
	
	public static void main(String[] args) {
//TODO: remove args override
		args =new String[1];
		args[0]="src\\config.txt";
		

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
