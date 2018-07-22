import java.util.Scanner;
import java.io.File;
import java.util.ArrayList;
public class Config {
	int numNodes;
	int interRequestDelay;
	int csExecutionTime;
	int numRequests;
	String [][] nodeIDList;
	ArrayList<ArrayList<Integer>> quorumList;
	
	Config(String filepath)
	{ 
		Scanner sc=null;
		try {
		sc = new Scanner(new File (filepath));
		}
		catch(Exception e)
		{
			System.out.println("Config file not found.");
		}
		String line="";
		int counter1=0;
		int counter2=0;
		boolean readFirstLine=false;
		//iterates through every line in configuration file
		while (sc.hasNextLine())
		{
			line=sc.nextLine();
			//if line is empty read next line
			if(line.length()==0)
			{
				continue;
			}
			char a = line.charAt(0);
			//check if first char in line is an integer, if not skip line
			if(a!='0'&&a!='1'&&a!='2'&&a!='3'&&a!='4'&&a!='5'&&a!='6'&&a!='7'&&a!='8'&&a!='9')
			{
				continue;
			}
			
			if(readFirstLine==false)
			{
				String[] tokens = line.split("\\s+");
				numNodes=Integer.parseInt(tokens[0]);
				interRequestDelay=Integer.parseInt(tokens[1]);
				csExecutionTime=Integer.parseInt(tokens[2]);
				numRequests=Integer.parseInt(tokens[3]);
				readFirstLine=true;
				nodeIDList=new String [numNodes][3];
				quorumList=new ArrayList<ArrayList<Integer>>(numNodes);
				continue;
			}
			if(counter1<numNodes) 
			{
				String[] tokens = line.split("\\s+");
				nodeIDList[counter1][0]=tokens[0];
				nodeIDList[counter1][1]=tokens[1];
				nodeIDList[counter1][2]=tokens[2];
				counter1++;
				continue;
			}
			
			if(counter2<numNodes)
			{
				quorumList.add(new ArrayList<Integer>());
				String[] tokens = line.split("\\s+");
				for(int i=0;i<tokens.length;i++)
				{
					if(tokens[i].charAt(0)=='#')  //skip rest of line if comment symbol is found
					{
						break;
					}
					quorumList.get(counter2).add(Integer.parseInt(tokens[i]));
				}
				counter2++;
			}	
		}
	}

	public int getNumNodes()
	{
		return numNodes;
	}
	public int getInterRequestDelay()
	{
		return interRequestDelay;
	}
	public int getCSExecutionTime()
	{
		return csExecutionTime;
	}
	public int getNumRequests()
	{
		return numRequests;
	}
	public String[][] getNodeIDList()
	{
		return nodeIDList;
	}
	public ArrayList<ArrayList<Integer>> getQuorumList()
	{
		return quorumList;
	}

}
