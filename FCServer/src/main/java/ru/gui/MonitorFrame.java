package gui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import door.Message.MessageDTO;
import door.Message.MessageDTO.GlobalMessageType;
import fox.builders.FoxFontBuilder;
import fox.builders.FoxFontBuilder.FONT;
import server.ClientHandler;
import server.Server;


@SuppressWarnings("serial")
public class MonitorFrame extends JFrame {		
	private static TrayIcon trayIcon;
	private static SystemTray systemTray = SystemTray.getSystemTray();
	static LinkedList<String> messageHistory = new LinkedList<String>();
	
	static JLabel connectsLabel, naClientsLabel;
	static JLabel statusLabel;
	static JLabel onlineLabel;
	static JTextArea console;
	static JScrollPane conScroll;
	static JTextField inputField;
	
	Font consoleFont = FoxFontBuilder.setFoxFont(FONT.CONSOLAS, 14, false);
	int historyMarker = 0;
	
	
	public MonitorFrame() {
		setTitle("FChat server monitor:");
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setAlwaysOnTop(true);
		setMinimumSize(new Dimension(360, 130));
		
		JPanel upPane = new JPanel(new BorderLayout()) {
			{

				JPanel infoPane = new JPanel(new GridLayout(1, 2, 3, 0)) {
					{
						setBorder(new EmptyBorder(3, 0, 3, 0));
						setBackground(Color.BLACK);
						
						JPanel infoPaneStatus = new JPanel(new GridLayout(2, 2, 3, 3)) {
							{
								setOpaque(false);
								setBorder(BorderFactory.createCompoundBorder(
										BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1, true), "Status:", 1, 2, consoleFont, Color.GRAY),
										new EmptyBorder(-6, 3, 6, 0)));
								
								add(new JLabel("Active:") {{setForeground(Color.WHITE);}});
								statusLabel = new JLabel("n/a") {{setHorizontalAlignment(SwingConstants.LEFT); setForeground(Color.WHITE);}};
								add(statusLabel);
								
								add(new JLabel("On-Line:") {{setForeground(Color.WHITE);}});
								Server.getAccess();
								onlineLabel = new JLabel("" + Server.isNetAccessible()) {{setHorizontalAlignment(SwingConstants.LEFT);}};
								add(onlineLabel);
							}
						};
						
						JPanel infoPaneData = new JPanel(new GridLayout(2, 2, 3, 3)) {
							{
								setOpaque(false);
								setBorder(BorderFactory.createCompoundBorder(
										BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1, true), "Data:", 1, 2, consoleFont, Color.GRAY),
										new EmptyBorder(-6, 3, 6, 0)));
								
								add(new JLabel("Connections:") {{setForeground(Color.WHITE);}});
								connectsLabel = new JLabel("" + Server.getAccess().getConnectionsCount()) {{setHorizontalAlignment(SwingConstants.LEFT);}};
								add(connectsLabel);
								
								add(new JLabel("Not autorized:") {{setForeground(Color.WHITE);}});
								naClientsLabel = new JLabel("" + Server.getAccess().getNAConnectionsCount()) {{setHorizontalAlignment(SwingConstants.LEFT);}};
								add(naClientsLabel);
							}
						};
						
						add(infoPaneStatus);
						add(infoPaneData);
					}
				};
				
				JPanel downButtonsPane = new JPanel(new BorderLayout(0, 0)) {
					{
						setBackground(Color.BLACK);
						setBorder(new EmptyBorder(0, 1, 0, 1));
						
						JButton resetLineBtn = new JButton("Отключить всех!") {
							{
								setFocusPainted(false);
								setBackground(new Color(0.0f, 0.25f, 0.75f, 1.0f));
								setForeground(Color.WHITE);
								addActionListener(new ActionListener() {
									@Override	
									public void actionPerformed(ActionEvent e) {resetRequest();}
								});
							}
						};
						
						JButton switchStateBtn = new JButton("О/I") {
							{
								setFocusPainted(false);
								setBackground(new Color(0.5f, 0.0f, 0.0f, 1.0f));
								setForeground(Color.WHITE);
								setToolTipText("<HTML>Запустить или остановить сервер.<br>Shift+click - завершение работы.");
								addActionListener(new ActionListener() {
									@Override	
									public void actionPerformed(ActionEvent e) {
										if (e.getModifiers() == 17) {exitRequest();
										} else {
											if (Server.isConnectionAlive()) {stopRequest();											
											} else {Server.getAccess().start();}
										}
									}
								});
							}
						};
						
						JButton connViewBtn = new JButton("VIEW") {
							{
								setFocusPainted(false);
								setBackground(new Color(0.25f, 0.5f, 0.1f, 1.0f));
								setForeground(Color.WHITE);
								
								addActionListener(new ActionListener() {
									@Override	
									public void actionPerformed(ActionEvent e) {printClientsList();}
								});
							}
						};
						
						add(connViewBtn, BorderLayout.WEST);
						add(resetLineBtn, BorderLayout.CENTER);
						add(switchStateBtn, BorderLayout.EAST);
					}
				};
					
				add(infoPane, BorderLayout.CENTER);
				add(downButtonsPane, BorderLayout.SOUTH);
			}
		};
		
		console = new JTextArea() {
			{
				setBorder(new EmptyBorder(3,3,3,3));
				setBackground(Color.BLACK);
				setForeground(Color.GREEN);
				setFont(consoleFont);
				setLineWrap(true);
				setWrapStyleWord(true);
				setCaretColor(Color.YELLOW);
				getCaret().setBlinkRate(250);
				setEditable(false);
				addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if (!e.isControlDown()) {
							inputField.requestFocusInWindow();
							inputField.setText(inputField.getText() + e.getKeyChar());
						}
					}
				});
			}
		};
		
		conScroll = new JScrollPane(console) {
			{
				setBorder(null);
				setPreferredSize(new Dimension(450, 300));
				setAutoscrolls(true);
			}
		};
		
		JPanel inputPane = new JPanel(new BorderLayout()) {
			{
				setBorder(new EmptyBorder(1,1,1,1));
				setBackground(Color.DARK_GRAY);
				
				inputField = new JTextField() {
					{
						setBorder(new EmptyBorder(1,3,0,3));
						setBackground(Color.BLACK);
						setForeground(Color.GREEN);
						setFont(consoleFont);
						setPreferredSize(new Dimension(300, 30));
						
						addKeyListener(new KeyAdapter() {
							@Override
							public void keyPressed(KeyEvent e) {
								switch (e.getKeyCode()) {
									case KeyEvent.VK_ENTER:
										String cmd = getText();
										if (cmd.startsWith("/")) {
											updateHistoryArray(cmd);
											cmd = cmdEngine(cmd);
											if (cmd != null) {toConsole(cmd);}											
											setText(null);
										}
									break;
										
									case KeyEvent.VK_UP: 
										if (messageHistory.size() > 0) {setText(messageHistory.get(historyMarker));}
										if (historyMarker > 0) {historyMarker--;}
									break;
										
									case KeyEvent.VK_DOWN: 
										if (messageHistory.size() > 0) {setText(messageHistory.get(historyMarker));}
										if (historyMarker < messageHistory.size() - 1) {historyMarker++;}
									break;
										
									default:
								}
							}
							
							private void updateHistoryArray(String cmd) {
								messageHistory.add(cmd);
								if (messageHistory.size() > 64) {System.out.println("Removed from history by max size (64): " + messageHistory.removeFirst());}
								historyMarker = messageHistory.size() - 1;
							}

							private String cmdEngine(String cmd) {
								if (cmd.equalsIgnoreCase("/help") || cmd.equalsIgnoreCase("/?")) {
									printCommandsList();
									return null;
								} else if (cmd.equalsIgnoreCase("/stop")) {
									if (Server.isConnectionAlive()) {stopRequest();
									} else {return "Сервер уже остановлен!";}
									return null;
								} else if (cmd.equalsIgnoreCase("/exit")) {
									exitRequest();
									return null;
								} else if (cmd.equalsIgnoreCase("/view") || cmd.equalsIgnoreCase("/show")) {
									printClientsList();
									return null;
								} else if (cmd.equalsIgnoreCase("/reset")) {
									resetRequest();
									return null;
								} else if (cmd.startsWith("/bc ")) {
									Server.getAccess().broadcast(GlobalMessageType.PUBLIC_MESSAGE, null, cmd.replace("/bc ", ""), false);
								} else if (cmd.startsWith("/say ")) {
									cmd = cmd.replace("/say ", "");
									final String to = cmd.substring(0, cmd.indexOf(" "));
									final String message = cmd.substring(to.length() + 1, cmd.length());
									Server.getAccess().getClient(to).say(new MessageDTO(GlobalMessageType.PRIVATE_MESSAGE, "SERVER", to, message));
								} else if (cmd.equalsIgnoreCase("/start")) {
									if (!Server.isConnectionAlive()) {Server.getAccess().start();
									} else {return "Сервер уже запущен!";}
									return null;
								} else if (cmd.equalsIgnoreCase("/info")) {
									printInfo();
									return null;
								} else {return "Команда " + cmd + " не зарегистрирована.";}
								
								return cmd;
							}

							private void printInfo() {
								toConsole("\n*** *** *** ***");
								toConsole("Server FoxyChat:");
								
								toConsole("\t* * * * * Server IP:\t\t" + Server.getIP());
								toConsole("\t* * * * * Server port:\t\t" + Server.getPort());
								toConsole("\t* * * * * Host name:\t\t" + Server.getHostName());
								toConsole("\t* * * * * Net access:\t\t" + Server.isNetAccessible());
								toConsole("\t* * * * * Clients count:\t" + Server.getAccess().getConnectionsCount());
								
								for (Entry<String, ClientHandler> client : Server.getAccess().getConnections()) {
									toConsole("\t " + client.getKey() + ": " + client.getValue().toString());
								}
								
								toConsole("*** *** *** ***\n");
							}

							private void printCommandsList() {
								toConsole("\n*** *** *** ***");
								toConsole("COMMANDS LISTING:");
								for (Entry<String, String> comItem : Server.getCommandsMapSet()) {
									toConsole(comItem.getKey() + "\t (" + comItem.getValue() + ");");
								}
								toConsole("*** *** *** ***\n");
							}
						});
					}
				};
				
				add(inputField);
			}
		};
		
		add(upPane, BorderLayout.NORTH);
		add(conScroll, BorderLayout.CENTER);
		add(inputPane, BorderLayout.SOUTH);
		
		
		try {
			BufferedImage trayIconImage = ImageIO.read(new File("tray.png"));
			trayIcon = new TrayIcon(trayIconImage, "FCServer");
			trayIcon.setImageAutoSize(true);
			trayIcon.setPopupMenu(new PopupMenu() {
				{
					add(new MenuItem("Start") {
				    	{
				    		addActionListener(new ActionListener() {
						    	public void actionPerformed(ActionEvent e) {Server.getAccess().start();}
						    });
				    	}
					});
					add(new MenuItem("Reset") {
				    	{
				    		addActionListener(new ActionListener() {
						    	public void actionPerformed(ActionEvent e) {resetRequest();}
						    });
				    	}
					});
					add(new MenuItem("Stop") {
				    	{
				    		addActionListener(new ActionListener() {
						    	public void actionPerformed(ActionEvent e) {stopRequest();}
						    });
				    	}
					});
					add(new MenuItem("Close") {
				    	{
				    		addActionListener(new ActionListener() {
						    	public void actionPerformed(ActionEvent e) {exitRequest();}
						    });
				    	}
					});
				}
			});
			trayIcon.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() >= 2) {traying(false);}
				}

				public void mouseEntered(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
			});
		} catch (IOException e) {e.printStackTrace();}
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowIconified(WindowEvent e) {traying(true);}
			@Override
			public void windowClosing(WindowEvent e) {traying(true);}
		});
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		
		Server.getAccess().start();
		
		ExecutorService updateEx = Executors.newSingleThreadExecutor();
		updateEx.execute(new Runnable() {
			@Override	public void run() {
				while (true) {
					updateOnlineStatus();
					updateConnectionsCount();
					updateActiveStatus();
					try {	Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}
				}
			}
		});
		updateEx.shutdown();
	}
	
	void exitRequest() {
		Object[] options = { "Да", "Нет!" };
		int n = JOptionPane.showOptionDialog(this, 
						"<HTML>Завершить работу сервера,<br>разорвав все активные соединения?<br>(активных: " + Server.getAccess().getConnectionsCount() + ")", 
						"Внимание!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);				
		if (n == 0) {
			Server.getAccess().close();
			System.exit(0);
		}
	}
	
	void stopRequest() {
		Object[] options = {"Да", "Нет!"};
		int n = JOptionPane.showOptionDialog(MonitorFrame.this, 
						"<HTML>Остановить работу сервера?<br>(активных: " + Server.getAccess().getConnectionsCount() + ")", 
						"Внимание!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);	
		
		if (n == 0) {Server.getAccess().stop();}
	}
	
	void resetRequest() {
		Object[] options = {"Да", "Нет!"};
		int n = JOptionPane.showOptionDialog(MonitorFrame.this, 
						"<HTML>Разорвать все активные соединения?<br>(активных: " + Server.getAccess().getConnectionsCount() + ")", 
						"Внимание!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);	
		
		if (n == 0) {Server.getAccess().resetConnections();}
	}
	
	
	static void printClientsList() {
		toConsole("Clients on-line:");
		for (Entry<String, ClientHandler> client : Server.getAccess().getConnections()) {
			toConsole(client.getKey() + ": " + client.getValue().toString());
		}
		toConsole("*** *** ***");
	}
	
	public synchronized static void toConsole(String string) {
		console.append("> " + string + "\n");
		scrollDown();
	}

	private static void scrollDown() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {Thread.sleep(200);} catch (Exception e) {/* SLEEP IGNORE */}
				int current = conScroll.getVerticalScrollBar().getValue();
				int max = conScroll.getVerticalScrollBar().getMaximum();
				if (current < max) {conScroll.getVerticalScrollBar().setValue(max);}
				console.repaint();
			}
		}).start();
	}
	
	public static void updateActiveStatus() {
		statusLabel.setText("" + Server.isConnectionAlive());
		statusLabel.setForeground(Server.isConnectionAlive() ? Color.GREEN : Color.RED);
	}
	public static void updateOnlineStatus() {
		onlineLabel.setText("" + Server.isNetAccessible());
		onlineLabel.setForeground(Server.isNetAccessible() ? Color.GREEN : Color.RED);
	}
	public static void updateConnectionsCount() {
		connectsLabel.setText("" + Server.getAccess().getConnectionsCount());
		naClientsLabel.setText("" + Server.getAccess().getNAConnectionsCount());
		connectsLabel.setForeground(Server.getAccess().getConnectionsCount() < Server.getMaxClientsAllowed() ? Color.GREEN : Color.RED);
		naClientsLabel.setForeground(Server.getAccess().getConnectionsCount() + Server.getAccess().getNAConnectionsCount() < Server.getMaxClientsAllowed() ? Color.WHITE : Color.RED);
	}

	void traying(boolean hide) {
		if (hide) {
			try {
				systemTray.add(trayIcon);
				setVisible(false);
				trayIcon.displayMessage("FCServer:", "I`am here! (click to restore)", TrayIcon.MessageType.INFO);
			} catch (AWTException e1) {e1.printStackTrace();}
		} else {
			setVisible(true);
			systemTray.remove(trayIcon);
		}
	}
}