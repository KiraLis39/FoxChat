package server;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

import door.Message.MessageDTO;
import door.Message.MessageDTO.GlobalMessageType;
import gui.MonitorFrame;


public class ClientHandler implements Runnable {
	public static enum clientState {UNAUTORISED, AUTORIZED}
	private clientState autorizedState = clientState.UNAUTORISED;
	
	private final String userExistsMessage = "Пользователь с таким именем уже в системе!";
	private final String welcomeMessage = "Добро пожаловать на Сервер!";
	private final String wrongPassMessage = "Был введен не верный пароль!";
	
	private MessageDTO autorizeRequest;
	
	private final Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	
	private Thread cHandThread;
	private String clientName;
	
	private boolean isClientAFK, isPassRequared = true;
	
	
	public ClientHandler(Socket _socket) {
		this.socket = _socket;
		
		this.cHandThread = new Thread(this);
		this.cHandThread.setDaemon(true);
		this.cHandThread.start();
	}
	
	@Override
	public void run() {
		try {
			this.dis = new DataInputStream(socket.getInputStream());
			this.dos = new DataOutputStream(socket.getOutputStream());
			
			MonitorFrame.toConsole("ClientHandler created. Sending a autorizeRequest...");
			autorizeRequest = new MessageDTO(GlobalMessageType.AUTH_REQUEST, "SERVER", "client", "Первичный запрос клиенту на UID");
			say(autorizeRequest);
			
			String income;
			while (true) {
				income = dis.readUTF();
				MonitorFrame.toConsole("ClientHandler resived: " + income);
				onRecieveMessage(MessageDTO.convertFromJson(income));
			}
		} catch (SocketException e) {
			MonitorFrame.toConsole("\nПохоже, соединение было внезапно сброшено: " + e.getMessage() + "." + clientName + " will be kicked than.");
			kick();
//			e.printStackTrace();
		} catch (EOFException e) {
			MonitorFrame.toConsole("\nEOFException на сервере. Причина: " + e.getMessage() + ".\nClient " + clientName + " was disconnected and will be kicked than.");
			kick();
//			e.printStackTrace();
		} catch (IOException e) {
			MonitorFrame.toConsole("\nIOException на сервере. Причина: " + e.getMessage());
			e.printStackTrace();
		}
	}


	private void onRecieveMessage(MessageDTO incomeDTO) {
		if (incomeDTO == null) {throw new RuntimeException("ClientHandler.onRecieveMessage(): incomeDTO cant be NULL!");}
		if (clientName == null) {clientName = incomeDTO.getFrom();}
		
		if (incomeDTO.getMessageType() == GlobalMessageType.AUTH_REQUEST) {autorizationCheck(incomeDTO);
		} else if (incomeDTO.getMessageType() == GlobalMessageType.PASS_REQUEST) {passCheck(incomeDTO);
		} else {
			MonitorFrame.toConsole(Server.getFormatTime(incomeDTO.getTimestamp()) + " (" +  getUserName() + " -> " + incomeDTO.getTo() + ") " + incomeDTO.getBody());
			
			switch (incomeDTO.getMessageType()) {
				case PUBLIC_MESSAGE: 
					Server.getAccess().broadcast(GlobalMessageType.PUBLIC_MESSAGE, this, incomeDTO.getBody(), true);
					break;
					
				case PRIVATE_MESSAGE: 
					if (Server.containsClient(incomeDTO.getTo())) {Server.getAccess().getClient(incomeDTO.getTo()).say(incomeDTO);				
					} else {say(new MessageDTO(GlobalMessageType.PRIVATE_MESSAGE, "SERVER", clientName, "Получатель не в сети. Повторите позже."));}
					break;
					
				case SYSINFO_MESSAGE: 
					if (incomeDTO.getBody().equals("AFK=true")) {isClientAFK = true;
					} else if (incomeDTO.getBody().equals("AFK=false")) {isClientAFK = false;}
					break;
					
				case USERLIST_MESSAGE: 					
					say(new MessageDTO(GlobalMessageType.USERLIST_MESSAGE, "SERVER", clientName, Arrays.toString(Server.getAccess().getClientsNameSet().toArray())));
					break;
					
				default: System.err.println("ClientHandler: onRecieveMessage(): Unknown message type income: " + incomeDTO.getMessageType() + ".");
			}
		}
	}

	private void autorizationCheck(MessageDTO incomeDTO) {
		MonitorFrame.toConsole("autorizationCheck(): Income unatorized message from UID: '" + incomeDTO.getUid() + "'.");
		
		if (incomeDTO.getMessageType() == GlobalMessageType.AUTH_REQUEST) {
			// client send me UID. Than check it:			
			if (DataBase.containsUID(incomeDTO.getUid())) {
				MonitorFrame.toConsole("Это зарегистрированный клиент. Сообщаем о готовности принять логин и пароль.");
				say(new MessageDTO(GlobalMessageType.CONFIRM_AUTH_MESSAGE, "SERVER", clientName, DataBase.getClientNameByUID(incomeDTO.getUid())));
			} else {
				MonitorFrame.toConsole("Это новый клиент. Пусть шлет свои данные для регистрации.");
				say(new MessageDTO(GlobalMessageType.REJECT_AUTH_MESSAGE, "SERVER", clientName));
			}
		} else {
			MonitorFrame.toConsole("Клиент не понял, что от него требуется? Повторяем запрос авторизации.");			
			say(autorizeRequest);
		}
	}

	private void passCheck(MessageDTO incomeDTO) {
		clientName = incomeDTO.getFrom();
		MonitorFrame.toConsole("passCheck(): Income password from '" + clientName + 
				"'. UID: '" + incomeDTO.getUid() + 
				"', LOGIN: '" + incomeDTO.getFrom() + 
				"', PASSWORD: '" + incomeDTO.getPassword() + "'.");
			
		if (Server.containsClient(clientName)) {
			MonitorFrame.toConsole("Такой клиент уже авторизирован! Отказ.");
			setAccessGranted(false, userExistsMessage);
		} else {
			if (DataBase.containsUID(incomeDTO.getUid())) {MonitorFrame.toConsole("Запрос на вход клиента '" + clientName + "'. UID: '" + incomeDTO.getBody() + "'.");
			} else {
				MonitorFrame.toConsole("Запрос на регистрацию нового клиента '" + clientName + "'. UID: '" + incomeDTO.getBody() + "'.");
				
				if (DataBase.addClient(incomeDTO)) {MonitorFrame.toConsole("Клиент '" + incomeDTO.getFrom() + "' добавлен в базу данных успешно.");
				} else {
					MonitorFrame.toConsole("Клиент '" + incomeDTO.getFrom() + "' не был добавлен в базу данных.");
					say(new MessageDTO(GlobalMessageType.REJECT_PASS_MESSAGE, "SERVER", clientName, "Something wrong with adding a client to DB on the Server. Method: passCheck()"));
					return;
				}
			}
			
			if (isPassRequared) {
				if (DataBase.checkPassword(clientName, incomeDTO.getUid(), incomeDTO.getPassword())) {
					setAccessGranted(true, welcomeMessage);
				} else {
					MonitorFrame.toConsole("Пароль не верен! Отказ в доступе '" + clientName + "'!");
					setAccessGranted(false, wrongPassMessage);
				}
			} else {
				MonitorFrame.toConsole("Пароль не требуется. Предоставление входа '" + clientName + "'.");
				setAccessGranted(true, welcomeMessage);
			}
		}
	}
	

	public void say(MessageDTO mesDTO) {
		String says = mesDTO.convertToJson();
		MonitorFrame.toConsole("Server say " + says);
		try {
			dos.writeUTF(says);
			dos.flush();
		} catch (IOException e) {e.printStackTrace();}
	}
	
	public void kick() {
		if (socket == null || socket.isClosed()) {return;}
		
		try {
			socket.shutdownInput();
			socket.shutdownOutput();
			socket.close();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Server.getAccess().onClientDisconnect(clientName);
			cHandThread.interrupt();
		}		
	}
	

	public DataInput getInputStream() {return dis;}

	public void setAccessGranted(boolean access, final String message) {
		if (message == null) {throw new RuntimeException("Access grants message is NULL");}
		
		if (access) {
			if (autorizedState != clientState.AUTORIZED) {autorizedState = clientState.AUTORIZED;}
			say(new MessageDTO(GlobalMessageType.CONFIRM_PASS_MESSAGE, "SERVER", clientName, message));
			Server.getAccess().addClientToArray(this);
		} else {
			say(new MessageDTO(GlobalMessageType.REJECT_PASS_MESSAGE, "SERVER", clientName, message));
		}
	}

	public String getUserName() {return this.clientName;}

	public boolean isConnected() {return socket != null && cHandThread != null && !socket.isClosed() && !cHandThread.isInterrupted();}
		
	public boolean isClientAFK() {return isClientAFK;}
	
	@Override
	public String toString() {return socket.toString();}
}