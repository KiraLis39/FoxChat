package net;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JOptionPane;

import door.Message.MessageDTO;
import door.Message.MessageDTO.GlobalMessageType;
import fox.adds.IOM;
import fox.adds.Out;
import gui.ChatFrame;
import media.Media;
import registry.IOMs;
import registry.Registry;
import subGUI.LoginFrame;
import subGUI.MenuBar;


public class NetConnector extends Thread {
	public enum localMessageType {OUTPUT, INPUT, INFO, WARN}
	
	public enum connStates {DISCONNECTED, CONNECTING, CONNECTED}
	private static connStates connState = connStates.DISCONNECTED;
	
	public enum authStates {AUTORIZED, UNAUTORIZED}
	private static authStates authState = authStates.UNAUTORIZED;
	
	private static MessageDTO authAnswer;
	
	private static Thread self;
	private static Socket socket;
	private static DataInputStream dis;
	private static DataOutputStream dos;
	
	private static boolean isClientAFK;
	
	
	private NetConnector() {
		super(new Runnable() {
			@Override
			public void run() {
				try {
					setConnectState(connStates.CONNECTING);
					
					Out.Print(NetConnector.class, 0, "Try to create socket with data: " + IOM.getString(IOM.HEADERS.LAST_USER, IOMs.LUSER.LAST_IP) + ": " + Integer.parseInt(IOM.getString(IOM.HEADERS.LAST_USER, IOMs.LUSER.LAST_PORT)));
					socket = new Socket(IOM.getString(IOM.HEADERS.LAST_USER, IOMs.LUSER.LAST_IP), Integer.parseInt(IOM.getString(IOM.HEADERS.LAST_USER, IOMs.LUSER.LAST_PORT)));
					
					Out.Print(NetConnector.class, 0, "Try to create data IO-streams by clients socket...");
					dis = new DataInputStream(socket.getInputStream());
					dos = new DataOutputStream(socket.getOutputStream());
					
					// waiting for income message:
					Out.Print(NetConnector.class, 1, "Client net is ready to get messages.");
					while (true) {	onMessageRecieved(MessageDTO.convertFromJson(dis.readUTF()));}
				} catch (Exception e) {showServerLostMessage(e);}
			}
		});
	
		self = this;
	}
	
	public static void reConnect() {
		authAnswer = new MessageDTO(GlobalMessageType.AUTH_REQUEST, "unautorized client", "SERVER", "AUTH UID FOR SERVER");
		authAnswer.setUid(IOM.getString(IOM.HEADERS.SECURE, "UID"));
		
		new NetConnector().start();
	}
	
	private static void showServerLostMessage(Exception e) {
		System.err.println("Нет соединения либо отказ сервера. Причина: " + e.getMessage());
//		if (!e.getMessage().equals("Socket closed")) {e.printStackTrace();}
		
		if (!ChatFrame.isChatShowing()) {
			JOptionPane.showConfirmDialog(null, "<html>Нет соединения либо отказ сервера.<br>Причина: <font color='RED'>" + e.getMessage(), 
					"Error:", JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE);
		} else {ChatFrame.addMessage("Потеряно соединения с сервером!", localMessageType.WARN);}
		
		disconnect();
	}
	
	public static boolean writeMessage(MessageDTO message) {
		if (dos == null || socket == null || socket.isClosed()) {			
			try {reConnect();
			} catch (Exception e) {
				System.out.println("Не удалось отправить сообщение по причине: " + e.getMessage());
				return false;
			}
		}		
		if (dos == null || socket == null || socket.isClosed()) {return false;}
		
		try {
			System.out.println("Пишем серверу письмишко: " + message);
			dos.writeUTF(message.convertToJson());
			dos.flush();
			return true;
		} catch (Exception e) {
			showServerLostMessage(e);
			return false;
		}
	}
	
	private static void onMessageRecieved(MessageDTO incomeDTO) {
		if (incomeDTO.getTo() == null || incomeDTO.getTo().isBlank()) {incomeDTO.setTo(IOM.getString(IOM.HEADERS.LAST_USER, IOMs.LUSER.LAST_USER));}
		System.out.println("\nNetConnector.onMessageRecieved() >>> " + incomeDTO.toString());

		if (incomeDTO.getMessageType() == GlobalMessageType.PUBLIC_MESSAGE || incomeDTO.getMessageType() == GlobalMessageType.PRIVATE_MESSAGE) {
			ChatFrame.addMessage(incomeDTO, localMessageType.INPUT);
			return;
		}
		
		if (incomeDTO.getMessageType() == GlobalMessageType.USERLIST_MESSAGE) {
			ChatFrame.updateUserList(incomeDTO.getBody().split(","));
			return;
		}
		
		
		if (incomeDTO.getMessageType() == GlobalMessageType.AUTH_REQUEST) {
			writeMessage(authAnswer);
			return;
		} else if (incomeDTO.getMessageType() == GlobalMessageType.CONFIRM_AUTH_MESSAGE) {
			Registry.login = incomeDTO.getBody();
			setAuthState(authStates.AUTORIZED);
			return;
		} else if (incomeDTO.getMessageType() == GlobalMessageType.REJECT_AUTH_MESSAGE) {
			setAuthState(authStates.UNAUTORIZED);
			return;
		}
		
		if (incomeDTO.getMessageType() == GlobalMessageType.CONFIRM_PASS_MESSAGE) {
			setConnectState(connStates.CONNECTED);
			LoginFrame.readyChatCreate();
			return;
		} else if (incomeDTO.getMessageType() == GlobalMessageType.REJECT_PASS_MESSAGE) {
			setConnectState(connStates.DISCONNECTED);
			LoginFrame.showDeniedDialog(incomeDTO.getBody());
			return;
		}
	}

	public static void requestUserList() {
		MessageDTO reqList = new MessageDTO(GlobalMessageType.USERLIST_MESSAGE, null, "SERVER");
		writeMessage(reqList);
	}

	public static connStates getConnectState() {return connState;}
	private static void setConnectState(connStates _state) {
		connState = _state;
		
		if (connState == connStates.CONNECTED) {
			Media.playSound("connect");
//			addMessage("Соединение с сервером успешно установлено!", localMessageType.INFO);
		} else if (connState == connStates.DISCONNECTED) {
//			addMessage("Соединение с сервером отсутствует!", localMessageType.WARN);
		}
	}
	
	public static authStates getAuthState() {return authState;}
	private static void setAuthState(authStates _state) {
		authState = _state;
		if (ChatFrame.isChatShowing()) {ChatFrame.disposeFrame();}
		if (authState == authStates.UNAUTORIZED) {new LoginFrame(0);			
		} else {new LoginFrame(1);}
	}

	public static void disconnect() {
		Media.playSound("disconnect");
		
		try {
			if (socket != null && !socket.isClosed()) {
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();

				setConnectState(connStates.DISCONNECTED);
			}
			if (dis != null) {dis.close();}
			if (dos != null) {dos.close();}
		} catch (IOException e) {e.printStackTrace();}
		
		if (Thread.currentThread().isAlive()) {Thread.currentThread().interrupt();}
	}

	public static Thread getThread() {return self;}
	
	public static boolean isAfk() {return isClientAFK;}
	public static void setAfk(boolean afk) {
		if (getConnectState() == connStates.CONNECTED && getAuthState() == authStates.AUTORIZED) {
			isClientAFK = afk;
			MenuBar.updateConnectLabel(MenuBar.getCurrentTextColor() == Color.BLACK ? new Color(0.25f, 0.5f, 0.5f) : Color.BLACK, afk ? Color.YELLOW : Color.GREEN, afk ? "On-Line (AFK)" : "On-Line");
			ChatFrame.addMessage("*** AFK " + (afk ? "ON" : "OFF") + " ***", localMessageType.INFO);
			writeMessage(new MessageDTO(GlobalMessageType.SYSINFO_MESSAGE, null, "SERVER", "AFK=" + NetConnector.isAfk()));
		}
	}
}