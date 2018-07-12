import java.io.Serializable;
import java.util.concurrent.atomic.*;

//messages are passed through the sockets
public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	int senderNode=-1;
	int receiverNode=-1;
	String text="";  

	String messageType="";  

	public Message(int from, int to, String m, String type)
	{
		senderNode=from;
		receiverNode=to;
		text=m;
		messageType=type;
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
}
