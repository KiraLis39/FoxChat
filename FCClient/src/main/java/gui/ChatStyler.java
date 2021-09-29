package gui;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import fox.adds.IOM;
import fox.adds.Out;
import fox.builders.ResManager;
import registry.IOMs;
import registry.Registry;
import subGUI.MenuBar;


public class ChatStyler {
	public enum uiStyleType {DEFAULT, GOLD, DARK}
	private static uiStyleType uiStyle = uiStyleType.DARK;
	public enum backgroundFillType {STRETCH, FILL, ASIS, PROPORTIONAL}
	private static backgroundFillType bFillType = backgroundFillType.STRETCH;
	private static float sidePanelsOpasity = 0.75f;
	
	
	public static void setBackgroundFillStyle(int bkgStyleIndex) {
		bFillType = backgroundFillType.values()[bkgStyleIndex];
		IOM.set(IOM.HEADERS.CONFIG, IOMs.CONFIG.BKG_DRAW_STYLE, bFillType.ordinal());
		ChatFrame.updateBackgroundImage();
	}

	public static void setUIStyle(uiStyleType style) {setUIStyle(style.ordinal());}

	public static void setUIStyle(int styleIndex) {
		uiStyle = uiStyleType.values()[styleIndex];
		
		String themeDirName = uiStyleType.values()[styleIndex].name();
		System.out.println("Setts up ui_style: '" + themeDirName + "'");
		try {
			ResManager.add("menuBarImage", new File("./resources/images/" + themeDirName + "/menuBarImage.png"), true);
			ResManager.add("downBarImage", new File("./resources/images/" + themeDirName + "/downBarImage.png"), true);	
			ResManager.add("sendButtonImage", new File("./resources/images/" + themeDirName + "/btn.png"), true);
			ResManager.add("pod_0", new File("./resources/images/" + themeDirName + "/pod_0.png"), true);
			ResManager.add("pod_1", new File("./resources/images/" + themeDirName + "/pod_1.png"), true);
		} catch (Exception e) {
			Out.Print(ChatFrame.class, 3, "UI_STYLE is not correct: " + uiStyle);
			e.printStackTrace();
		}
		
		File f;
		try {
			f = new File(IOM.getString(IOM.HEADERS.CONFIG, IOMs.CONFIG.BKG_PATH));
			ChatFrame.setBackgroundImage(ImageIO.read(f), f.getCanonicalPath());
		} catch (IOException e) {
			try {ChatFrame.setBackgroundImage(ImageIO.read(new File("./resources/images/backgrounds/bkgDefault.png")), "./resources/images/backgrounds/bkgDefault.png");
			} catch (IOException e1) {e1.printStackTrace();};
		}
		
		uiStyle = uiStyleType.values()[uiStyle.ordinal()];
		IOM.set(IOM.HEADERS.CONFIG, IOMs.CONFIG.UI_STYLE, uiStyle.ordinal());
		
		ChatFrame.setupMenuBar(null);
		if (uiStyle == uiStyleType.DEFAULT) {
			new MenuBar(Color.BLACK);
			ChatFrame.setSidePanelsBkg(new Color(0.45f, 0.5f, 0.55f, sidePanelsOpasity));
		} else {
			new MenuBar(Color.WHITE);
			ChatFrame.setSidePanelsBkg(new Color(0.1f, 0.1f, 0.1f, sidePanelsOpasity));
		}
		
		ChatFrame.setupMenuBar(MenuBar.getMenu());
		ChatFrame.setSendButtonSprite(Registry.fsc.addSpritelist("sendButtonSprite", ResManager.getBImage("sendButtonImage"), 1, 3));
	}

	public static backgroundFillType getFillType() {return bFillType;}

	public static uiStyleType getCurrentStyle() {return uiStyle;}	
}