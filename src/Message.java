import java.io.Serializable;
import java.util.concurrent.atomic.*;

//messages are passed through the sockets
public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	int senderNode=-1;
	int receiverNode=-1;
	String text="";  
	String messageType="";  //REQUEST, GRANT, INQUIRE, FAILED, RELINQUISH, RELEASE  
							//(release indicates cs has been completed, relinquish temporarily gives up lock)
	CSRequest request;
	public Message(int from, int to, String m, String type,CSRequest r)
	{
		senderNode=from;
		receiverNode=to;
		text=m;
		messageType=type;
		request=r;
	}
	public String getText()
	{
		return text;
	}
	public int getSender()
	{
		return senderNode;
	}
	public int getReceiver()
	{
		return receiverNode;
	}
	public String getMessageType()
	{
		return messageType;
	}
	public CSRequest getRequest()
	{
		return request;
	}
}
