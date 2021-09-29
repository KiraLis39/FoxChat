package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import interfaces.iServerConnector;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import door.MainClass;
import door.Message.MessageDTO;
import door.Message.MessageDTO.GlobalMessageType;
import gui.MonitorFrame;


@SuppressWarnings("serial")
public class Server implements iServerConnector, Runnable {
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("<dd.MM.yyyy HH:mm:ss>");
	private static Logger log;
	
	private static List<ClientHandler> nonAutorizedClients = new ArrayList<ClientHandler>();
	private static Map<String, ClientHandler> clientsMap = new LinkedHashMap<String, ClientHandler>();
	
	private static LinkedHashMap<String, String> comsMap = new LinkedHashMap<String, String> () {
		{
			put("/?", "Выводит список всех доступных в консоли команд (см. 'help')");
			put("/help", "Выводит список всех доступных в консоли команд (см. '?')");	
			
			put("/view", "Выводит список всех активных подлючений (клиентов) (см. 'show')");
			put("/show", "Выводит список всех активных подлючений (клиентов) (см. 'view')");
			
			put("/reset", "Отключает всех клиентов и очищает список их подключений");
			put("/exit", "Полностью останавливает и закрывает приложение сервера");
			put("/stop", "Отключает всех клиентов и останавливает выполнение сервера (не закрывая его)");
			put("/start", "Запускает сервер (если он был остановлен)");
			
			put("/bc <MESSAGE>", "Отправка глобального сообщения");	
			put("/say <MESSAGE>", "Отправка сообщения пользователю (клиенту)");	
			
			put("/info", "Выводит информацию о сервере и подключении");	
		}
	};

	private static ExecutorService serverEx;
	private static ServerSocket sSocket;	
	private static Server server;
	
	private static final int PORT = 13900;
	private static final int MAX_CLIENTS = 32;
	
	
	public Server() {
		server = this;
		log = LogManager.getLogger(Server.class);
	}

	public static Server getAccess() {return server;}

	
	public synchronized void resetConnections() {
		MonitorFrame.toConsole("Kick all clients...");
		log.warn("All clients was kicked.");
		
		for (Entry<String, ClientHandler> entry : clientsMap.entrySet()) {entry.getValue().kick();}
		clientsMap.clear();		
		
		for (ClientHandler nac : nonAutorizedClients) {nac.kick();}
		nonAutorizedClients.clear();
	}
	
	public synchronized void broadcast(GlobalMessageType type, final ClientHandler srcClient, final String brdcstmsg, boolean isSelfExclude) {
		/*
		 * isSelfExclude = true (Сообщения транслируются всем, кроме самого отправителя).
		 * isSelfExclude = false (Сообщение оправляется лишь самому отправителю).
		 */
		if (clientsMap.size() > 0) {
			for (ClientHandler handler : clientsMap.values()) {
				if (handler.equals(srcClient) && isSelfExclude) {continue;
				} else {handler.say(new MessageDTO(type, srcClient == null ? "SERVER" : srcClient.getUserName(), handler.getUserName(), brdcstmsg));}
			}
		}
	}
		
	
	@Override
	public void run() {
		try {
			sSocket = new ServerSocket(PORT);
			MonitorFrame.toConsole("Server up on " + sSocket.getInetAddress() + "/" + sSocket.getLocalPort());
	
			while (!sSocket.isClosed()) {
				MonitorFrame.toConsole("\nServer awaits for a new connection...");
				onClientConnection(new ClientHandler(sSocket.accept()));
			}
		} catch (UnknownHostException e) {onServerException(e);
		} catch (SocketException e) {onServerException(e);
		} catch (IOException e) {onServerException(e);
		} finally {
			log.warn("Server is shutdown.");
			MonitorFrame.toConsole("Server.run(): Server thread is shut down.");
		}
	}
	
	@Override
	public synchronized void start() {
		DataBase.loadDataBase();
		
		serverEx = Executors.newSingleThreadExecutor();
		log.info("Start the clients-income reading thread..");
		serverEx.execute(this);
		serverEx.shutdown();
	}

	@Override
	public synchronized void stop() {
		log.warn("Server shutting down...");
		resetConnections();
		MonitorFrame.toConsole("Closing server socket...");
		if (sSocket != null) {
			try {sSocket.close();
			} catch (IOException e) {e.printStackTrace();}
		}
		serverEx.shutdown();
		MonitorFrame.toConsole("Server has shutting down...");
	}
	
	@Override
	public void close() {
		serverEx.shutdownNow();
		sSocket = null;
		clientsMap.clear();
		DataBase.closeDB();
	}

	@Override
	public synchronized void onClientConnection(ClientHandler ch) {
		MonitorFrame.toConsole("Новый клиент пытается выполнить подключение...");
		nonAutorizedClients.add(ch);
		log.trace("A new client connected.");
	}

	@Override
	public synchronized void onClientDisconnect(String clientName) {
		MonitorFrame.toConsole("Server.onClientDisconnect(): Client '" + clientName + "' disconnected.");
		if (clientsMap.containsKey(clientName)) {clientsMap.remove(clientName);}
		
		for (ClientHandler ch : nonAutorizedClients) {
			String uName = ch.getUserName();
				if (uName == null || uName.equals(clientName)) {
					nonAutorizedClients.remove(ch);
					break;
				}
		}
		
		log.trace("Client " + clientName + " was disconnected.");
	}

	@Override
	public synchronized void onServerException(Exception e) {
		if (!e.getMessage().equals("Socket closed")) {
			log.error("Server has Exception!", e);
			MonitorFrame.toConsole("Server has Exception: '" + e.getMessage() + "' (" + e.getCause() + ").");
			e.printStackTrace();
		}
	}

	
	public static synchronized String getIP() {
		try {return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {return null;}
	}
	
	public static synchronized String getHostName() {
		try {return InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {return null;}
	}
	
	public static synchronized int getPort() {return sSocket.getLocalPort();}
	

	public static synchronized boolean isNetAccessible() {
		try(Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("google.com", 80), 3000);
            return true;
        } catch(UnknownHostException unknownHost) {return false;
        } catch (IOException e) {return false;}
	}
	
	public static synchronized int getMaxClientsAllowed() {return MAX_CLIENTS;}

	public static synchronized String getFormatTime(long millis) {return dateFormat.format(millis);}

	public static boolean isConnectionAlive() {return serverEx != null && !serverEx.isTerminated();}

	public static Set<Entry<String, String>> getCommandsMapSet() {return comsMap.entrySet();}
	
	public synchronized int getNAConnectionsCount() {return nonAutorizedClients.size();}
	
	public synchronized Set<Entry<String, ClientHandler>> getConnections() {return clientsMap.entrySet();}
	public synchronized Set<String> getClientsNameSet() {return clientsMap.keySet();}
	public synchronized int getConnectionsCount() {return clientsMap.size();}
	public synchronized void addClientToArray(ClientHandler handler) {
		clientsMap.put(handler.getUserName(), handler);
		nonAutorizedClients.remove(handler);
//		handler.setAccessGranted(true, welc);
	}
	public synchronized ClientHandler getClient(String clientName) {return clientsMap.get(clientName);}
	public static boolean containsClient(String to) {return clientsMap.containsKey(to);}
}