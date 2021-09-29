package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import door.Exit;
import door.Message.MessageDTO;
import door.Message.MessageDTO.GlobalMessageType;
import fox.adds.IOM;
import fox.adds.InputAction;
import fox.adds.Out;
import fox.builders.FoxFontBuilder;
import fox.builders.ResManager;
import fox.components.VerticalFlowLayout;
import fox.games.FoxCursor;
import gui.ChatStyler.backgroundFillType;
import gui.ChatStyler.uiStyleType;
import media.Media;
import net.NetConnector;
import net.NetConnector.localMessageType;
import net.SubController;
import registry.IOMs;
import registry.Registry;
import subGUI.BaloonBack;
import subGUI.BaloonBack.Baloon;


@SuppressWarnings("serial")
public class ChatFrame extends JFrame implements ActionListener, MouseListener, MouseMotionListener {
	private static Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	private static BufferedImage[] sendButtonSprite;

	private static File tmpHistoryFile = new File("./resources/user/" + IOM.getString(IOM.HEADERS.LAST_USER, IOMs.LUSER.LAST_USER) + "/clog");
	
	public static JPanel chatPanel;
	
	private static ChatFrame frame;
	private static JTextArea inputArea;
	private static JScrollPane inputScroll, msgsScroll;
	private static JPanel basePane;
	private static JPanel rightPane;
	private static JPanel leftPane;
	private static JPanel downPane;
	private static JPanel midPane;
	private static JPanel correctPane;
	private static JButton sendButton;
	
	private static DefaultListModel<String> usersListModel;	
	private static JList<String> usersList;

	private Point frameWas, mouseWasOnScreen;
	
	private static boolean needUpdate = true, isBusy, isFullscreen;

	private static Color cSidePanelsBkg = new Color(0.0f, 0.0f, 0.0f, 0.6f);
	
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2D = (Graphics2D) g;
		Registry.render(g2D);
		super.paintComponents(g2D);
		
//		g2D.drawImage(ResManager.getBImage("head"), 0, 0, getWidth(), 30, this);
//		g2D.setColor(Color.ORANGE);
//		g2D.drawString(Registry.name + " v." + Registry.verse, 10, 18);		
	}
	
	public ChatFrame() {
		frame = this;
		init();
		
		setTitle(Registry.name + " v." + Registry.verse);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setMinimumSize(new Dimension(600, 800));

		basePane = new JPanel(new BorderLayout()) {
			int bkgW, bkgH;
			
			@Override
			public void paintComponent(Graphics g) {
				Registry.render((Graphics2D) g);
				if (ResManager.getBImage("bkgDefault") == null) {return;}
				bkgW = ResManager.getBImage("bkgDefault").getWidth();
				bkgH = ResManager.getBImage("bkgDefault").getHeight();
				
				drawImageBackplace(g);
				
				if (ChatStyler.getFillType() == backgroundFillType.ASIS) {g.drawImage(ResManager.getBImage("bkgDefault"), 0, 0, bkgW, bkgH, null);
				} else if (ChatStyler.getFillType() == backgroundFillType.STRETCH) {g.drawImage(ResManager.getBImage("bkgDefault"), 0, 0, getWidth(), getHeight(), null);
				} else if (ChatStyler.getFillType() == backgroundFillType.PROPORTIONAL) {
					int w1 = getWidth() / 2 - bkgW / 2;
					int h1 = (getHeight() - 120) / 2 - bkgH / 2;
					
					g.drawImage(ResManager.getBImage("bkgDefault"), w1, h1, bkgW, bkgH, null);
				} else {
					int tmpx = getWidth() / bkgW;
					int tmpy = getHeight() / bkgH;
					
					for (int i = 0; i < tmpy + 1; i++) {
						for (int j = 0; j < tmpx + 1; j++) {
							g.drawImage(ResManager.getBImage("bkgDefault"), bkgW * j, bkgH * i, bkgW, bkgH, null);
						}
					}
				}
			}
			
			private void drawImageBackplace(Graphics g) {
				Color itemBackColor1 = new Color(
						ResManager.getBImage("bkgDefault").getColorModel().getRGB(
								ResManager.getBImage("bkgDefault").getRaster().getDataElements(
										bkgW - 3, bkgH / 2, null)));
				Color itemBackColor2 = new Color(
						ResManager.getBImage("bkgDefault").getColorModel().getRGB(
								ResManager.getBImage("bkgDefault").getRaster().getDataElements(
										3, bkgH / 2, null)));
				
				GradientPaint primary = new GradientPaint(0, 0, itemBackColor1, getWidth(), getHeight(), itemBackColor2);
				
				((Graphics2D) g).setPaint(primary);
				g.fillRect(0, 0, getWidth(), getHeight());				
			}

			{
				setCursor(FoxCursor.createCursor(ResManager.getBImage("cur_1"), "ansC"));
				setBorder(new EmptyBorder(1, 0, 0, 0));
				
				midPane = new JPanel(new BorderLayout()) {
					{
						setOpaque(false);
						setBorder(new EmptyBorder(0, 3, 1, 3));

						chatPanel = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.BOTTOM, 0, 0)) {
							@Override
							public void paintComponent(Graphics g) {
								if (IOM.getBoolean(IOM.HEADERS.CONFIG, IOMs.CONFIG.USE_DIALOGPANE_OPACITY)) {
									Registry.render((Graphics2D) g);
									g.setColor(new Color(0.4f, 0.4f, 0.5f, 0.5f));
									g.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
								}
							}
							
							{
//								setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
								setOpaque(false);
								setName("chatPane");
							}
						};
						
						msgsScroll = new JScrollPane(chatPanel) {
							{
								setViewportBorder(null);
								
								setBorder(null);
								setOpaque(false);
								getViewport().setOpaque(false);
								setAutoscrolls(true);
								
//								getViewport().setLayout(new ConstrainedViewPortLayout());
								getVerticalScrollBar().setUnitIncrement(14);
								getVerticalScrollBar().setAutoscrolls(true);
								setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
							}
						};

						add(msgsScroll);
					}
				};
				
				leftPane = new JPanel() {
					@Override
					public void paintComponent(Graphics g) {
						Registry.render((Graphics2D) g);
						g.setColor(cSidePanelsBkg);
						g.fillRoundRect(0, 0, getWidth(), getHeight() - 1, 6, 6);
						
						g.drawImage(ResManager.getBImage("pod_0"), 3, 3, 35, 35, this);
						g.drawImage(ResManager.getBImage("pod_1"), 3, 48, 35, 35, this);
					}
					
					{
						setOpaque(false);
						setPreferredSize(new Dimension(42, 0));
						setBorder(new EmptyBorder(0, 1, 1, 0));
						setVisible(IOM.getBoolean(IOM.HEADERS.CONFIG, IOMs.CONFIG.SHOW_LEFT_PANEL));
					}
				};
				
				rightPane = new JPanel(new BorderLayout()) {
					@Override
					public void paintComponent(Graphics g) {
						Registry.render((Graphics2D) g);
						g.setColor(cSidePanelsBkg);
						g.fillRoundRect(0, 0, getWidth(), getHeight() - 1, 6, 6);

						g.drawImage(ResManager.getBImage("userListEdge"), 0, 0, 16, getHeight(), this);
					}
					
					{
						setOpaque(false);
						setBorder(new EmptyBorder(3, 16, 1, 3));
						setVisible(IOM.getBoolean(IOM.HEADERS.CONFIG, IOMs.CONFIG.SHOW_USERS_PANEL));
						
						
						UIManager.put("List.dropCellBackground", UIManager.getColor(Color.GREEN));
						UIManager.put("List.background", UIManager.getColor(Color.GREEN));
						usersListModel = new DefaultListModel<String>();
						usersList = new JList<String>(usersListModel) {
							@Override
		                    public int locationToIndex(Point location) {
		                        int index = super.locationToIndex(location);
		                        if (index != -1 && !getCellBounds(index, index).contains(location)) {return -1;
		                        } else {return index;}		                        
		                    }
							
							{
								setOpaque(false);
								
								setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//								setBackground(new Color(0.7f, 0.8f, 0.85f, 0.1f));
								setSelectionBackground(new Color(0.7f, 0.8f, 0.85f, 0.1f));
								setSelectionForeground(new Color(1.0f, 1.0f, 1.0f));
								
								setForeground(Color.WHITE);
								setFont(Registry.fUsers);
//								setVisibleRowCount(5);
								setCellRenderer(new TransparentListCellRenderer());
								
								addMouseListener(new MouseAdapter() {
									@Override
									public void mouseReleased(MouseEvent e) {
										if (locationToIndex(e.getPoint()) != -1) {
											rightPane.repaint();
											inputArea.requestFocus();
											clearSelection();
										}
									}
									
									@Override
									public void mousePressed(MouseEvent e) {
										rightPane.repaint();
										if (getSelectedValue() == null) {return;}
										
										Out.Print(ChatFrame.class, 0, "Был выбран вариант " + getSelectedValue());
										
										if (inputArea.getText().startsWith("/to ")) {
											inputArea.setText(inputArea.getText().replace(inputArea.getText().split(": ")[0] + ": ", ""));
										}											
										inputArea.setText("/to " + getSelectedValue() + ": " + inputArea.getText());
									}
								});
							
								setFocusable(false);
								setCursor(FoxCursor.createCursor(ResManager.getBImage("cur_0"), "ansC"));
							}
						};
						
						add(usersList);
					}
				};
				
				downPane = new JPanel(new BorderLayout(3, 3)) {
					@Override
					public void paintComponent(Graphics g) {
						Registry.render((Graphics2D) g);
						super.paintComponent(g);
						
						g.drawImage(ResManager.getBImage("downBarImage"), 0, 0, getWidth(), getHeight(), this);
						g.drawImage(ResManager.getBImage("grass"), 0, getHeight() - 32, getWidth(), 32, this);
						
						g.setFont(Registry.fLabels);
						g.setColor(Color.BLACK);
						g.drawString(Registry.company, (int) (getWidth() / 2 - FoxFontBuilder.getStringCenterX(g, Registry.company)) - 1, getHeight() - 6);
						g.setColor(Color.WHITE);
						g.drawString(Registry.company, (int) (getWidth() / 2 - FoxFontBuilder.getStringCenterX(g, Registry.company)), getHeight() - 7);
						
						g.setFont(Registry.fLabels);
						
						g.setColor(Color.GRAY);
						g.drawString("'Ctrl+0' - anti-glitch;", 7, getHeight() - 9);
						g.setColor(Color.BLACK);
						g.drawString("'Ctrl+0' - anti-glitch;", 8, getHeight() - 8);
						
						if (isFullscreen()) {							
							g.setColor(Color.GRAY);
							g.drawString("'Ctrl+F' - switch fullscreen", FoxFontBuilder.getStringWidth(g, "'Ctrl+0' - anti-glitch;").intValue() + 15, getHeight() - 9);
							g.setColor(Color.BLACK);
							g.drawString("'Ctrl+F' - switch fullscreen", FoxFontBuilder.getStringWidth(g, "'Ctrl+0' - anti-glitch;").intValue() + 16, getHeight() - 8);
						}
					}
					
					{
						setOpaque(false);
						setBorder(BorderFactory.createCompoundBorder(
								new EmptyBorder(0, 0, 26, 0),
								BorderFactory.createCompoundBorder(
										BorderFactory.createTitledBorder(
												BorderFactory.createLineBorder(ChatStyler.getCurrentStyle() == uiStyleType.DARK ? Color.GRAY.darker() : Color.GRAY.brighter(), 1, true), 
												"- Foxy Chat -", 3, 2, Registry.fMessage, Color.GRAY.darker()),
										new EmptyBorder(-6, 0, 0, 0)
										)
								)
						);
//						setCursor(FoxCursor.createCursor(ResourceManager.getBufferedImage("cur_1"), "ansC"));
						
						correctPane = new JPanel(new FlowLayout(0, 3, 3)) {
							{
								setOpaque(false);
								setBorder(new EmptyBorder(-3, -3, -3, 0));
								setPreferredSize(new Dimension(0, 22));
								
								JButton music = new JButton("music") {
									{
										setBackground(Color.BLACK);
										setForeground(Color.WHITE);
										setPreferredSize(new Dimension(100, 24));
										setActionCommand("music");
										addActionListener(ChatFrame.this);
										setFocusPainted(false);
									}
								};
								JButton photo = new JButton("photo") {
									{
										setBackground(Color.BLACK);
										setForeground(Color.WHITE);
										setPreferredSize(new Dimension(100, 24));
										setActionCommand("photo");
										addActionListener(ChatFrame.this);
										setFocusPainted(false);
									}
								};
								JButton document = new JButton("document") {
									{
										setBackground(Color.BLACK);
										setForeground(Color.WHITE);
										setPreferredSize(new Dimension(100, 24));
										setActionCommand("document");
										addActionListener(ChatFrame.this);
										setFocusPainted(false);
									}
								};
								
								add(music);
								add(photo);
								add(document);
							}
						};
						
						inputArea = new JTextArea() {
							{
								setWrapStyleWord(true);
								setLineWrap(true);
								setFont(Registry.fMessage);
								setBorder(new EmptyBorder(0, 3, 0, 3));
								setRequestFocusEnabled(true);
//								setBackground(cSidePanelsBkg);
								
								addKeyListener(new KeyAdapter() {									
									@Override
									public void keyPressed(KeyEvent e) {
										if (e.getExtendedKeyCode() == KeyEvent.VK_ENTER) {
											if (e.getModifiersEx() == InputEvent.CTRL_DOWN_MASK) {enterActionVar(1);
											} else {enterActionVar(0);}
										}
									}
									
									@Override
									public void keyReleased(KeyEvent e) {
										if (e.getKeyCode() != KeyEvent.VK_ENTER) {return;}
										
										if (IOM.getInt(IOM.HEADERS.CONFIG, IOMs.CONFIG.MSG_SEND_TYPE) == 0) {
											if (e.getModifiersEx() == InputEvent.CTRL_DOWN_MASK) {
												inputArea.setText(null);
											}
										} else {
											if (e.getModifiersEx() != InputEvent.CTRL_DOWN_MASK) {
												inputArea.setText(null);
											}
										}
									};
								});
							}
						};
						
						inputScroll = new JScrollPane(inputArea) {
							{
								setViewportBorder(null);
								
								setOpaque(false);
								getViewport().setOpaque(false);

								setAutoscrolls(true);
								getVerticalScrollBar().setAutoscrolls(true);

								getVerticalScrollBar().setUnitIncrement(9);
								setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
								setPreferredSize(new Dimension(0, 45));
							}
						};
						
						sendButton = new JButton("Отправить") {
							BufferedImage btnImage = sendButtonSprite[0];
							
							@Override
							public void paintComponent(Graphics g) {
								Registry.render((Graphics2D) g);
								g.setFont(Registry.fLabels);
								g.setColor(Color.WHITE);

								g.drawImage(btnImage, 0, 0, getWidth(), getHeight(), this);
								g.drawString(getText(), 
										(int) (getWidth() / 2 - FoxFontBuilder.getStringCenterX(g, getText())), 
										(int) (getHeight() / 2 + FoxFontBuilder.getStringHeight(g) / 3) - (btnImage == sendButtonSprite[2] ? 1:0));
							}
							
							{
								setOpaque(false);
								setActionCommand("send");
								setBorderPainted(false);
								addActionListener(ChatFrame.this);
								addMouseListener(new MouseAdapter() {
							         public void mouseEntered(MouseEvent me) {
							        	 btnImage = sendButtonSprite[1];
							        	 repaint();
							         }
							         public void mouseExited(MouseEvent me) {
							        	 btnImage = sendButtonSprite[0];
							        	 repaint();
							         }
							         public void mousePressed(MouseEvent e) {
							        	 btnImage = sendButtonSprite[2];
							        	 repaint();
							         }
							         public void mouseReleased(MouseEvent e) {
							        	 btnImage = sendButtonSprite[1];
							        	 repaint();
							         }
							      });
							}
						};
						
						add(correctPane, BorderLayout.NORTH);
						add(inputScroll, BorderLayout.CENTER);
						add(sendButton, BorderLayout.EAST);
					}
				};
				
//				add(upPane, BorderLayout.NORTH);
				add(midPane, BorderLayout.CENTER);
				add(leftPane, BorderLayout.WEST);
				add(rightPane, BorderLayout.EAST);
				add(downPane, BorderLayout.SOUTH);
			}
		};
		
		add(basePane);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {exitRequest();}
		});
		addMouseListener(this);
		addMouseMotionListener(this);
		addComponentListener(new ComponentAdapter() {
			@Override	public void componentResized(ComponentEvent e) {needUpdate = true;}
		});
		addWindowStateListener(new WindowStateListener() {
			@Override
			public void windowStateChanged(WindowEvent e) {
				System.out.println("FRAME STATE: " + e.getOldState() + " -> " + e.getNewState());
				if (e.getNewState() == 0) {
					if (NetConnector.isAfk()) {NetConnector.setAfk(false);}
					isFullscreen = false;
				} else if (e.getNewState() == 1) {
					if (!NetConnector.isAfk()) {NetConnector.setAfk(true);}
				} else if (e.getNewState() == 6) {
					if (NetConnector.isAfk()) {NetConnector.setAfk(false);}
					isFullscreen = true;
				}
				
				switchFullscreen();
			}
		});

		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		
		// поток обновления UI:
		new Thread(new Runnable() {
			@Override
			public void run() {
				Media.playSound("launched");
				Thread t1 = null, t2 = null;
				
				while (true) {
					if (needUpdate) {
						needUpdate = false;
						if (isBusy) {
							try {t1.interrupt();} catch (Exception e) {}
							try {t2.interrupt();} catch (Exception e) {}
						}
						isBusy = true;
						
						rightPane.setPreferredSize(new Dimension(ChatFrame.this.getWidth() / 5, 0));
						rightPane.revalidate();
						rightPane.repaint();
						

						t1 = new Thread(() -> {revalidateChatBaloonsPanel();});
						t1.start();						
						try {t1.join();} catch (InterruptedException e1) {e1.printStackTrace();}
						
						try {Thread.sleep(200);} catch (InterruptedException e) {/* IGNORE SLEEP */}
						
						t2 = new Thread(() -> {correctsBaloonsGlitches();});
						t2.start();
						try {t2.join();} catch (InterruptedException e1) {e1.printStackTrace();}
						

						t1 = new Thread(() -> {revalidateChatBaloonsPanel();});
						t1.start();						
						try {t1.join();} catch (InterruptedException e1) {e1.printStackTrace();}
						
						try {Thread.sleep(200);} catch (InterruptedException e) {/* IGNORE SLEEP */}
						
						t2 = new Thread(() -> {correctsBaloonsGlitches();});
						t2.start();
						try {t2.join();} catch (InterruptedException e1) {e1.printStackTrace();}
						
						
						inputArea.requestFocusInWindow();
						usersList.clearSelection();
						
						isBusy = false;
						scrollDown();
					}
					try {Thread.sleep(100);} catch (InterruptedException e) {/* IGNORE SLEEP */}
				}
			}
		}) {{setDaemon(true); start();}};
	
		// поток фоновых фич:
		(new Thread(new SubController()){{setDaemon(true);}}).start();
		
		setupInAc();
		setSidePanelsBkg(cSidePanelsBkg);
		NetConnector.requestUserList();
		
		loadHistory(IOM.getInt(IOM.HEADERS.CONFIG, IOMs.CONFIG.LOAD_HISTORY_LINES));
	}

	private static void loadHistory(int messagesCount) {
		if (!tmpHistoryFile.exists()) {return;}
		
		StringBuilder sb = new StringBuilder();		
		try {
			for (String line : Files.readAllLines(tmpHistoryFile.toPath())) {sb.append(line + System.lineSeparator());}
		} catch (IOException e) {e.printStackTrace();}

		String[] lines = sb.toString().split("&>>");
		if (lines.length > messagesCount) {
			int trimsCount = lines.length - messagesCount;
			for (int i = 0; i < trimsCount; i++) {
				lines[i] = null;
			}
		}

		for (int i = 0; i < lines.length; i++) {
			if (lines[i] == null || lines[i].isBlank()) {continue;}
			
			String[] data = lines[i].split("&>");			
			localMessageType type = localMessageType.valueOf(data[1]);
			String fromTo = data[2];
			String body = data[3];
			String date = data[4];
			GlobalMessageType global = GlobalMessageType.valueOf(data[5]);
			addChatBaloon(	type, global, 
					fromTo.split("От: ")[1].split(" кому")[0], 
					fromTo.split("кому ")[1], 
					body, date);
		}
	}

	public static void setupInAc() {
		System.out.println("SETUP INAC: " + IOM.getInt(IOM.HEADERS.CONFIG, IOMs.CONFIG.MSG_SEND_TYPE));
		
		InputAction.add("chat", frame);
		InputAction.set("chat", "escape", KeyEvent.VK_ESCAPE, 0, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				usersList.clearSelection();
				if (inputArea.getText().startsWith("/to ")) {
					inputArea.setText(inputArea.getText().replace(inputArea.getText().split(": ")[0] + ": ", ""));
				} else {exitRequest();}
				
				inputArea.requestFocusInWindow();
			}
		});
		InputAction.set("chat", "fullscreen", KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				isFullscreen = !isFullscreen;
				switchFullscreen();
				inputArea.requestFocusInWindow();
			}
		});	
		InputAction.set("chat", "update", KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK, new AbstractAction() {
			@Override	public void actionPerformed(ActionEvent e) {needUpdate = true;}
		});
	}

	private static void enterActionVar(int var) {
		if (IOM.getInt(IOM.HEADERS.CONFIG, IOMs.CONFIG.MSG_SEND_TYPE) == 0) {
			inputArea.setToolTipText("<HTML>Enter - next line<br>Ctrl+Enter - send");
			if (var == 0) {
			} else {addMessage(inputArea.getText(), localMessageType.OUTPUT);}
		} else {
			inputArea.setToolTipText("<HTML>Ctrl+Enter - next line<br>Enter - send");
			if (var == 0) {addMessage(inputArea.getText(), localMessageType.OUTPUT);
			} else {nextInputLine();}
		}
	}
	
	private static void nextInputLine() {
		inputArea.requestFocusInWindow();
		inputArea.append("\n");
	}

	private static void init() {
		ChatStyler.setUIStyle(IOM.getInt(IOM.HEADERS.CONFIG, IOMs.CONFIG.UI_STYLE) == -1 ? 2 : IOM.getInt(IOM.HEADERS.CONFIG, IOMs.CONFIG.UI_STYLE));
		ChatStyler.setBackgroundFillStyle(IOM.getInt(IOM.HEADERS.CONFIG, IOMs.CONFIG.BKG_DRAW_STYLE) == -1 ? 0 : IOM.getInt(IOM.HEADERS.CONFIG, IOMs.CONFIG.BKG_DRAW_STYLE));
	}
	
	
	// MESSAGE SYSTEM:
	public synchronized static void addMessage(String message, localMessageType type) {
		addMessage(new MessageDTO(GlobalMessageType.SYSINFO_MESSAGE, "System", 
				(type == localMessageType.INFO || type == localMessageType.WARN) ? IOM.getString(IOM.HEADERS.LAST_USER, IOMs.LUSER.LAST_USER) : null, message), type);
	}
	
	public synchronized static void addMessage(MessageDTO messageDTO, localMessageType type) {
		if (messageDTO.getBody() == null || messageDTO.getBody().isBlank()) {return;}

		boolean successfulSended = false;
		
		if (messageDTO.getBody().startsWith("/to ")) {
			messageDTO.setTo(messageDTO.getBody().split(": ")[0].replace("/to ", ""));
			messageDTO.setBody(messageDTO.getBody().substring(messageDTO.getBody().indexOf(": ") + 2, messageDTO.getBody().length()));
		}
		
		if (messageDTO.getTo() == null) {
			if (type == localMessageType.INPUT) {
				messageDTO.setTo(IOM.getString(IOM.HEADERS.LAST_USER, IOMs.LUSER.LAST_USER));
				messageDTO.setMessageType(GlobalMessageType.PRIVATE_MESSAGE);
			} else {
				messageDTO.setTo("Всем");
				messageDTO.setMessageType(GlobalMessageType.PUBLIC_MESSAGE);
			}
		} else {messageDTO.setMessageType(GlobalMessageType.PRIVATE_MESSAGE);}
		
		
		if (type == localMessageType.OUTPUT) {
			if (NetConnector.isAfk()) {NetConnector.setAfk(false);}

			messageDTO.setFrom(IOM.getString(IOM.HEADERS.LAST_USER, IOMs.LUSER.LAST_USER));
			messageDTO.setTimestamp(System.currentTimeMillis());
			
			successfulSended = NetConnector.writeMessage(messageDTO);
			if (successfulSended) {
				Media.playSound("messageSend");
				inputArea.setText(null);
			} else {
				messageDTO.setBody("(Не отправлено) " + messageDTO.getBody());
				type = localMessageType.INFO;
				Media.playSound("systemError");
			}
		}
		
		switch (type) {
			case WARN: Media.playSound("systemError");
				break;
				
			case INFO: Media.playSound("systemError");
				break;
				
			case INPUT: 
//				if (!messageDTO.getFrom().equals("SERVER") && !usersListModel.contains(messageDTO.getFrom())) {addUserToList(messageDTO.getFrom());}
				Media.playSound("messageReceive");
				break;
				
			default:	if (type != localMessageType.OUTPUT) System.err.println("ChatFrame:addMessage(): Unknown type income: " + type);
		}

		addChatBaloon(type, messageDTO);
		inputArea.requestFocusInWindow();
	}
	
	private static void addChatBaloon(localMessageType inputOrOutput, MessageDTO mesDTO) {
		if (chatPanel == null) {return;}
		if (mesDTO.getBody() == null || mesDTO.getBody().isBlank()) {return;}
		
		BaloonBack newBaloonBack = new BaloonBack(inputOrOutput, mesDTO);
		chatPanel.add(newBaloonBack);
		chatPanel.add(Box.createVerticalStrut(3));
//		revalidateBaloon(newBaloonBack);
		
		String bHeader = newBaloonBack.getBaloon().getHeaderText();
		String bBody = newBaloonBack.getBaloon().getAreaText();
		String bFooter = newBaloonBack.getBaloon().getFooterText();
		String compoundString = "&>" + inputOrOutput + "&>" + bHeader + "&>" + bBody + "&>" + bFooter + "&>" + mesDTO.getMessageType() + "&>>";
		if (!bHeader.contains("System") && !bHeader.contains("INFO")) {writeHistory(compoundString);}
		
		scrollDown();
		needUpdate = true;
	}
	
	private static void addChatBaloon(localMessageType type, GlobalMessageType glType, String from, String to, String body, String footer) {
		BaloonBack newBaloonBack = new BaloonBack(type, glType, from, to, body, footer);
		chatPanel.add(newBaloonBack);
		chatPanel.add(Box.createVerticalStrut(3));
	}

	private static void writeHistory(String compoundString) {
		try {
			if (!tmpHistoryFile.exists()) {tmpHistoryFile.createNewFile();}
			Path chatLog = tmpHistoryFile.toPath();
			
			try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(chatLog.toFile(), true), StandardCharsets.UTF_8)) {
				osw.write(compoundString);
				osw.flush();
			} catch (Exception e) {e.printStackTrace();}
		} catch (Exception e) {
			JOptionPane.showConfirmDialog(frame, "<HTML>Произошла ошибка<br>при записи истории<br>" + tmpHistoryFile, e.getMessage(), 
					JOptionPane.PLAIN_MESSAGE, JOptionPane.WARNING_MESSAGE);
			e.printStackTrace();
		}
	}

	private synchronized static void revalidateChatBaloonsPanel() {
		System.out.println("\nrevalidateChatBaloonsPanel");
		for (Component bc : chatPanel.getComponents()) {
			if (bc instanceof BaloonBack) {calculateBaloonSize(((BaloonBack) bc).getBaloon());}
		}
	}
	
	private synchronized static void correctsBaloonsGlitches() {
		System.out.println("correctsBaloonsGlitches");
		for (Component bc : chatPanel.getComponents()) {
			if (bc instanceof BaloonBack) {
				((BaloonBack) bc).setPreferredSize(new Dimension((int) (msgsScroll.getSize().getWidth() - 19D), ((BaloonBack) bc).getBaloon().getPreferredSize().height));
			}
			bc.revalidate();
		}
	}
		
	private synchronized static void calculateBaloonSize(Baloon baloon) {
		if (baloon.getArea().getGraphics() == null) return;
		
		baloon.getDataLabel().setPreferredSize(new Dimension(FoxFontBuilder.getStringWidth(baloon.getGraphics(), baloon.getHeaderText()).intValue() + 9,	15));
		Double maxPlace = msgsScroll.getSize().getWidth() - 19D;
		
		int canPlaceColumn;	
		Double bodyWidth = FoxFontBuilder.getStringWidth(baloon.getArea().getGraphics(), baloon.getAreaText());
		Double headWidth = FoxFontBuilder.getStringWidth(baloon.getArea().getGraphics(), baloon.getHeaderText());
		if (bodyWidth > headWidth) {
			if (bodyWidth > maxPlace) {
				canPlaceColumn = (int) (maxPlace / FoxFontBuilder.getStringWidth(baloon.getArea().getGraphics(), "W")) - 6;
				baloon.getArea().setColumns(canPlaceColumn);
			} else {
				canPlaceColumn = (int) (bodyWidth / FoxFontBuilder.getStringWidth(baloon.getArea().getGraphics(), "W"));
				baloon.getArea().setColumns(canPlaceColumn);
			}
		}		
		baloon.revalidate();
	}
	
		
	// UTILITES:
	public static void addUserToList(String newUserName) {
		newUserName = newUserName.replace("[", "").replace("]", "");
		
		if (!usersListModel.contains(newUserName)) {
			usersListModel.addElement(newUserName);
			rightPane.repaint();
		}		
	}
	
	public static void updateUserList(String[] users) {
		if (usersListModel != null) {usersListModel.removeAllElements();} // or marked it off-line is better?..
		
		for (String userName : users) {
			if (userName.equals(IOM.getString(IOM.HEADERS.LAST_USER, IOMs.LUSER.LAST_USER))) {continue;}
			addUserToList(userName);
		}		
	}
	
	public static void saveChatToFile() {
		System.out.println("\nsaving chats messages");
		StringBuilder sb = new StringBuilder();
		for (Component bc : chatPanel.getComponents()) {
			if (bc instanceof BaloonBack) {
				Baloon nextBaloon = ((BaloonBack) bc).getBaloon();
				String bHeader = nextBaloon.getHeaderText();
				String bBody = nextBaloon.getAreaText();
				String bFooter = nextBaloon.getFooterText();
				String compoundString = "&>" + bHeader + "&>" + bBody + "&>" + bFooter + System.lineSeparator();
				
				sb.append(compoundString);
			}
		}
		
		JFileChooser chatSaveChooser = new JFileChooser(fox.tools.SystemInfo.USER.getUSER_HOME()) {
			{
				setDialogTitle("Куда сохраняем?");
//				setFileFilter(new FileNameExtensionFilter("Images", "PNG", "JPG"));
//				setFileHidingEnabled(false);
//				setFileSelectionMode(JFileChooser.FILES_ONLY);						
			}
		};
		
		int result = chatSaveChooser.showSaveDialog(frame);
		if (result == JFileChooser.APPROVE_OPTION) {
			File chatFile = new File(chatSaveChooser.getSelectedFile().getPath() + ".txt");
			try {
				Path chatLog = chatFile.toPath();
				try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(chatLog.toFile(), true), StandardCharsets.UTF_8)) {
					osw.write(sb.toString());
					osw.flush();
				} catch (Exception e) {e.printStackTrace();}
			} catch (Exception e) {
				JOptionPane.showConfirmDialog(frame, "<HTML>Произошла ошибка<br>при сохранении файла<br>" + chatFile, e.getMessage(), 
						JOptionPane.PLAIN_MESSAGE, JOptionPane.WARNING_MESSAGE);
				e.printStackTrace();
			}
		}	
	}

	public static void switchLeftPaneVisible() {
		leftPane.setVisible(!leftPane.isVisible());
		needUpdate = true;
		IOM.set(IOM.HEADERS.CONFIG, IOMs.CONFIG.SHOW_LEFT_PANEL, leftPane.isVisible());
	}
	
	public static void switchRightPaneVisible() {
		rightPane.setVisible(!rightPane.isVisible());
		needUpdate = true;
		IOM.set(IOM.HEADERS.CONFIG, IOMs.CONFIG.SHOW_USERS_PANEL, rightPane.isVisible());
	}

	public static void disposeFrame() {frame.dispose();}
	
	public static boolean showFrame() {
		if (frame != null) {
			frame.setVisible(true);
			return true;
		}
		return false;
	}
	
	private static void switchFullscreen() {
		if (isFullscreen) {
			if (frame.getSize().getWidth() >= screen.getWidth() && frame.isUndecorated()) {return;}
			
			isFullscreen = true;
			frame.dispose();
			frame.setUndecorated(true);
			frame.setState(MAXIMIZED_BOTH);
			frame.setSize(screen);
			
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			
			needUpdate = true;
		} else {
			if (frame.getSize().getWidth() < screen.getWidth() && !frame.isUndecorated()) {return;}
			
			isFullscreen = false;
			frame.dispose();
			frame.setUndecorated(false);
			frame.setState(NORMAL);
			
//			frame.pack();
			frame.setSize(frame.getMinimumSize());
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			
			needUpdate = true;
		}
	}
	
	private static void scrollDown() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {Thread.sleep(250);} catch (Exception e) {/* SLEEP IGNORE */}
				int current = msgsScroll.getVerticalScrollBar().getValue();
				int max = msgsScroll.getVerticalScrollBar().getMaximum();
				if (current < max) {msgsScroll.getVerticalScrollBar().setValue(max);}
			}
		}).start();
		msgsScroll.revalidate();
	}
	
	private static void choseMusicToSend() {zaglushko();}
	
	private static void chosePhotoToSend() {zaglushko();}
	
	private static void choseDocumentToSend() {zaglushko();}
	
	
	public static void zaglushko() {
		JOptionPane.showConfirmDialog(frame, "Еще не реализовано...", "Прастити", JOptionPane.PLAIN_MESSAGE, JOptionPane.INFORMATION_MESSAGE);
	}
	
	
	// EXIT:
	private static void exitRequest() {
		Object[] options = { "Да", "Нет!" };
		int n = JOptionPane.showOptionDialog(frame, "Закрыть окно?", "Подтверждение", 
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 	null, options, options[0]);		
		if (n == 0) {disconnectAndExit();}
	}
	
	public static void disconnectAndExit() {Exit.exit();}
	
	
	// GETS & SETS:
	public static void setSendButtonSprite(BufferedImage[] spritelist) {sendButtonSprite = spritelist;}
	public static void setBackgroundImage(BufferedImage bkgImage, String bkgPath) {
		try {
			ResManager.remove("bkgDefault");
			ResManager.add("bkgDefault", new File(bkgPath));
		} catch (Exception e) {e.printStackTrace();}
		
		IOM.set(IOM.HEADERS.CONFIG, IOMs.CONFIG.BKG_PATH, bkgPath);
		frame.repaint();
	}
	public static void setSidePanelsBkg(Color color) {
		cSidePanelsBkg = color;
		try {usersList.setBackground(color);			
		} catch (Exception e) {cSidePanelsBkg = color;}
	}
	public static void setupMenuBar(JMenuBar mBar) {frame.setJMenuBar(mBar);}
	public static void setDialogOpacity(boolean dpOpacity) {
		IOM.set(IOM.HEADERS.CONFIG, IOMs.CONFIG.USE_DIALOGPANE_OPACITY, dpOpacity);
		chatPanel.repaint();
	}
	public static void updateBackgroundImage() {if (basePane != null) basePane.repaint();}
	public static boolean isFullscreen() {	return isFullscreen;}
	public static boolean isChatShowing() {return frame != null && frame.isVisible();}
	
	
	// LISTENERS:
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
			case "send": addMessage(inputArea.getText(), localMessageType.OUTPUT);
				break;
				
			case "music": choseMusicToSend();
				break;
			case "photo": chosePhotoToSend();
				break;
			case "document": choseDocumentToSend();
				break;
			case "uCorrect": inputArea.replaceSelection("<u>" + inputArea.getSelectedText() + "</u>");
				break;
			default: 
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		try {
			ChatFrame.this.setLocation(
					(int) (frameWas.getX() - (mouseWasOnScreen.getX() - e.getXOnScreen())), 
					(int) (frameWas.getY() - (mouseWasOnScreen.getY() - e.getYOnScreen())));
		} catch (Exception e2) {/* IGNORE MOUSE DRAG */}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouseWasOnScreen = new Point(e.getXOnScreen(), e.getYOnScreen());
		frameWas = getLocation();
	}
	
	public void mouseMoved(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}

	
	// INNER CLASSES:
	public class TransparentListCellRenderer extends DefaultListCellRenderer {
	     @Override
	     public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
	         Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	         if (!isSelected) {c.setBackground(new Color(0.0f, 0.0f, 0.0f, 0.0f));}
	         return c;
	     }
	}
}