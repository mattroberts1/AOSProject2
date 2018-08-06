import java.util.Scanner;
import java.io.File;


public class Logchecker {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Scanner sc=null;
		try {
		sc = new Scanner(new File (args[0]));
		}
		catch(Exception e)
		{
			System.out.println("log file not found.");
		}
		int errorIndex=0;
		boolean isValid=true;
		String line1;
		String line2;

		while (sc.hasNextLine())
		{
			line1=sc.nextLine();
			line2=sc.nextLine();
			if(line1!=null&&line2!=null)
			{
				String[] split1=line1.split("-");
				String[] split2=line2.split("-");
				if(!split1[0].equals(split2[0]))
				{
					isValid=false;
					errorIndex+=2;
					break;
				}
				else if(!split1[1].equals("entercs")||!split2[1].equals("leavecs"))
				{
					isValid=false;
					errorIndex+=2;
					break;
				}	
			errorIndex+=2;
			}
		}
		
		if(isValid)
		{System.out.println("Output was valid");}
		else
			System.out.println("Output not valid, error detected near line " +errorIndex);
	}
}
