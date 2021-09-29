package door;

import java.io.File;
import java.util.TimeZone;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import fox.adds.IOM;
import fox.adds.Out;
import fox.builders.ResManager;
import net.NetConnector;
import registry.IOMs;


public class MainClass {
	private final static LookAndFeel defaultLaF = UIManager.getLookAndFeel();
	
	public static void main(String[] args) {
		// create UID:
		loadSecure();

		// set timezone:
		TimeZone.setDefault(TimeZone.getTimeZone("GMT+3"));
		
		// tune:
		Out.setEnabled(true);
		Out.setLogsCoutAllow(3);
		
		IOM.setDebugOn(false);
		IOM.setDefaultEmptyString("NONE");
		
		ResManager.setDebugOn(false);
		
		// prepare and launch:
		checkFilesExists();
		getLastData();
		NetConnector.reConnect();
	}

	private static void getLastData() {
		IOM.add(IOM.HEADERS.LAST_USER, new File("./resources/user/luser.dat"));
		IOM.setIfNotExist(IOM.HEADERS.LAST_USER, IOMs.LUSER.LAST_USER, "");
		IOM.setIfNotExist(IOM.HEADERS.LAST_USER, IOMs.LUSER.LAST_PASSWORD, "");	
		IOM.setIfNotExist(IOM.HEADERS.LAST_USER, IOMs.LUSER.LAST_IP, "localhost");
		IOM.setIfNotExist(IOM.HEADERS.LAST_USER, IOMs.LUSER.LAST_PORT, 13900);
	}

	private static void loadSecure() {
		IOM.add(IOM.HEADERS.SECURE, new File("./resources/secure.dat"));
		IOM.setIfNotExist(IOM.HEADERS.SECURE, "UID", fox.tools.SystemInfo.USER.getUSER_NAME() + "_" + fox.tools.SystemInfo.CPU.getCPU_MODEL() + "_" + fox.tools.SystemInfo.CPU.getCPU_NAME());
	}

	private static void checkFilesExists() {
		File[] ownDirectories = new File[] {
				new File("./resources/images/backgrounds/"),
				new File("./resources/images/DEFAULT/"),
				new File("./resources/sounds/"),
				new File("./resources/user/")
		};
		for (int i = 0; i < ownDirectories.length; i++) {
			if (!ownDirectories[i].exists()) {
				ownDirectories[i].mkdirs();
			}
		}
	}

	public static LookAndFeel getDefaultLaF() {return defaultLaF;}
}