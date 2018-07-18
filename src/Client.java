import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.ConnectException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicIntegerArray;


public class Client implements Runnable{

	int ID;
	String hostName;
	String hostAddr;
	int listenPort;
	AtomicIntegerArray connectionEstablished; //stores the status of the connections (1 if connection is established, 0 otherwise)
	int targetID;
	LinkedBlockingQueue<Message> clientQueue;
	public Client(int IDarg, String hostNamearg, int listenPortarg, LinkedBlockingQueue<Message> q, AtomicIntegerArray connArray, int tID)  //info for node being connected to
	{
		ID=IDarg;
		hostName=hostNamearg;
		listenPort=listenPortarg;	
		hostAddr=hostName+".utdallas.edu";
		clientQueue = q;
		connectionEstablished=connArray;
		targetID=tID;
	}
	
    public void run(){
    	
    	Socket socket=null;
    	boolean retry = true;
    	while(retry) //keep trying to connect to server until it comes online
    	{
    		
    		try {
    			socket = new Socket(hostAddr,listenPort);
    			retry=false;
    			System.out.println("connected to "+hostAddr);
    			connectionEstablished.set(targetID, 1);
    		}
    		catch(ConnectException e)
    		{
    			try{
    			Thread.sleep(100);
    			}
    			catch(Exception x){}
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    		}
    	}

    	try 
    	{
    		//loop for sending out messages received from controller
    		while(true)
    		{
    			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
    			Message m = clientQueue.take();
    			oos.writeObject(m);			
    		}
    	}
    	catch(Exception e){}
    }


}