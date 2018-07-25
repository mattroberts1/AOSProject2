import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MaekawaCallable{
	static LinkedBlockingQueue<CSRequest> interThreadComm;
	static AtomicInteger csStatus;
	//pass way to communicate with MaekawaProtocol thread
	public MaekawaCallable(LinkedBlockingQueue<CSRequest> itc, AtomicInteger status)
	{
		interThreadComm=itc;
		csStatus=status;
	}

	//block until ok to enter cs then return
	public void enterCS(int id, AtomicInteger ts)
	{
		CSRequest newRequest=new CSRequest(id,ts);
		interThreadComm.add(newRequest);
		csStatus.set(1);
		while(csStatus.get()!=2)  //loop until maekawaprotocol sets csStatus to 2 meaning this node can enter cs then return
		{
			try {wait();}
				catch(Exception e) {}  
		}
	}

	//let MaekawaProtocol know application has left cs
	public void leaveCS()
	{
		csStatus.set(0);		
	}
}
