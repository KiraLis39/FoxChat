package net;

import java.awt.Color;

import fox.adds.IOM;
import registry.IOMs;
import subGUI.MenuBar;


public class SubController implements Runnable {

	private static long afkTimeLast, afkTimeLimit, sleepTime = 1000;


	@Override
	public void run() {
		afkTimeLimit = IOM.getInt(IOM.HEADERS.CONFIG, IOMs.CONFIG.AFK_TIME_SEC) * 1000;
		afkTimeLast = System.currentTimeMillis();
		
		// прочие полезные, фоновые методы. Не критические и не обязательные.
		while (true) {
			checkConnectStatus();			
			
			try {Thread.sleep(sleepTime);} catch (InterruptedException e) {Thread.currentThread().interrupt();}
		}
	}
	
	private static void checkConnectStatus() {
		if (NetConnector.getConnectState() == NetConnector.connStates.CONNECTED) {
			if (System.currentTimeMillis() - afkTimeLast > afkTimeLimit) {
				NetConnector.setAfk(true);
				resetAfkTime();
			}
			
			if (!NetConnector.isAfk()) {	MenuBar.updateConnectLabel(MenuBar.getCurrentTextColor() == Color.BLACK ? new Color(0.25f, 0.5f, 0.5f) : Color.BLACK, Color.GREEN, "On-Line");}
		} else if (NetConnector.getConnectState() == NetConnector.connStates.CONNECTING) {
			MenuBar.updateConnectLabel(MenuBar.textColor == Color.BLACK ? new Color(0.75f, 0.25f, 0.0f) : Color.DARK_GRAY, Color.BLACK, "Connect..");
		} else {MenuBar.updateConnectLabel(MenuBar.textColor == Color.BLACK ? null : new Color(0.45f, 0.2f, 0.2f), Color.RED, "Off-Line");}
	}

	public static void resetAfkTime() {afkTimeLast = System.currentTimeMillis();}
}
