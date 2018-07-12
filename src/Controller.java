
public class Controller {

	public static void main(String[] args) {
//TODO: remove args override
		args =new String[1];
		args[0]="src\\config.txt";
		
		
		Config conf= new Config(args[0]);
		System.out.print(conf.getNumNodes()+" ");
		System.out.print(conf.getInterRequestDelay()+" ");
		System.out.print(conf.getCSExecutionTime()+" ");
		System.out.println(conf.getNumRequests()+" ");
		System.out.println("Node ID list:");
		for(int i=0;i<conf.getNodeIDList().length;i++)
		{
			for(int j=0;j<conf.getNodeIDList()[i].length;j++)
			{
				System.out.print(conf.getNodeIDList()[i][j]+" ");
			}
			System.out.println();
		}
		System.out.println("Quorum list:");
		for(int i=0;i<conf.getQuorumList().size();i++)
		{
			for(int j=0;j<conf.getQuorumList().get(i).size();j++)
			{
				System.out.print(conf.getQuorumList().get(i).get(j)+" ");
			}
			System.out.println();
		}
	}

}
