import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MaekawaCallable{
	static LinkedBlockingQueue<Message> interThreadComm;
	//pass way to communicate with MaekawaProtocol thread
	public MaekawaCallable(LinkedBlockingQueue<Message> itc)
	{
		interThreadComm=itc;
	}
	
	//block until ok to enter cs then return
	public void enterCS(int id, AtomicInteger ts)
	{
		CSRequest newRequest=new CSRequest(id,ts);
//TODO pass cs request to maekawa protocol
//TODO wait for response from maekawa protocol saying it's ok to enter cs

	}
	//let MaekawaProtocol know application has left cs
	public void leaveCS()
	{
//TODO tell maekawa protocol that node has left cs		
	}
}
