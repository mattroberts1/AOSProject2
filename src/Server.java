import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
public class Server implements Runnable{   	
    private static ServerSocket server;
    private static int port;  //port server will listen on
    LinkedBlockingQueue<Message> serverQueue; //queue for passing application messages back to controller
    public Server(int p, LinkedBlockingQueue<Message> sq)  
    {
    	port=p;
    	serverQueue=sq;
    }
    public void run()
    {	
    	try {
    		server = new ServerSocket(port);
    		System.out.println("Server is listening for incoming connections");
            while(true)
            {
            	 Socket socket = server.accept();
            	 new Thread(new ListenerSocket(socket, serverQueue)).start();
            }
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();	
    	}
    }
}
