package door;

import fox.adds.IOM;
import fox.adds.Out;
import net.NetConnector;


public class Exit {

	public static void exit() {exit(0);}
	
	public static void exit(int errCode) {
		if (errCode != 0) {
			Out.Print(Exit.class, 3, "Warning! Program has closed with code #" + errCode + ". Check it please.");
		}
		
		NetConnector.disconnect();
		IOM.saveAll();
		System.exit(errCode);
	}
}
