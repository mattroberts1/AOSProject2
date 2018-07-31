import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;


public class ListenerSocket implements Runnable {
	Socket socket; //socket connected to client of other process
	LinkedBlockingQueue<Message> serverQueue; //queue for received messages messages

	public ListenerSocket(Socket s, LinkedBlockingQueue<Message> sq)
	{
		socket=s;
		serverQueue=sq;
	}
	public void run()
	{
		try {
			while(true)
			{
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
				Message m = (Message) ois.readObject();
						serverQueue.put(m);
						System.out.println("Received message of type "+m.getMessageType()+" from node "+m.getSender());
			}
		}
    	catch(Exception e){}
	}

}
