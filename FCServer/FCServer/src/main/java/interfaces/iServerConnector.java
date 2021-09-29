package interfaces;

import server.ClientHandler;

public interface iServerConnector {
	void start();
	void stop();
	void close();
	
	void onClientConnection(ClientHandler handler);
	void onClientDisconnect(String clientName);
	void onServerException(Exception e);
}
