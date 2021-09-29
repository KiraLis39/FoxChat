package subGUI;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import door.Exit;
import door.MainClass;
import door.Message.MessageDTO;
import door.Message.MessageDTO.GlobalMessageType;
import fox.adds.IOM;
import fox.adds.Out;
import fox.builders.FoxFontBuilder;
import fox.builders.ResManager;
import gui.ChatFrame;
import media.Media;
import net.NetConnector;
import registry.IOMs;
import registry.Registry;


@SuppressWarnings("serial")
public class LoginFrame extends JDialog implements MouseListener, MouseMotionListener {
	private static LoginFrame loginFrame;
	private static JPasswordField passField, repassField;
	private static JTextField loginField;
	private Graphics2D g2D;
	
	private static int showMode;
	
	private Point frameWas, mouseWasOnScreen;
	private Color optionButColor = Color.GRAY.brighter(), optionButColor2 = Color.GRAY.brighter();
	private static JButton option, option2;
	private static JPanel buttonsPane;
	
	
	@Override
	public void paint(Graphics g) {
//		g2D = (Graphics2D) g;
		
//		g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.0f));
		super.paint(g);
//		g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1.0f));
		
//		ResManager.getFilesLink("requestImage").getPath() // KiraLis39
	}
	
	public LoginFrame(int _showMode) {
		loginFrame = this;
		showMode = _showMode; // 0 - reg, 1 - login

		try{UIManager.setLookAndFeel(MainClass.getDefaultLaF());
		} catch (Exception e3){Out.Print(MainClass.class, 2, "Setup the UIManagers L&F-style was failed. Cause: " + e3.getCause());}
		
		setUndecorated(true);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setBackground(new Color(0, 0, 0, 0));
		setOpacity(0.96f);
		if (showMode == 1) {setPreferredSize(new Dimension(400, 260));
		} else {setPreferredSize(new Dimension(400, 360));}
		
		JPanel basePane = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				g2D = (Graphics2D) g;
				Registry.render(g2D, true);
				
				g2D.setColor(Color.DARK_GRAY);
				g2D.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 19, 19);
				
				g2D.setStroke(new BasicStroke(2));
				g2D.setColor(Color.WHITE);
				g2D.drawRoundRect(3, 4, getWidth() - 8, getHeight() - 8, 16, 16);
				g2D.setColor(Color.GRAY);
				g2D.drawRoundRect(4, 4, getWidth() - 8, getHeight() - 9, 16, 16);
				
				
				String title = showMode == 1 ? "-= LOG-IN =-" : "-= REGISTER =-";
				g2D.setFont(Registry.fBigSphere);
				g2D.setColor(Color.BLACK);
				g2D.drawString(title, (int) (getWidth() / 2 - FoxFontBuilder.getStringCenterX(g2D, title)) - 1, 29);
				g2D.setColor(Color.GRAY);
				g2D.drawString(title, (int) (getWidth() / 2 - FoxFontBuilder.getStringCenterX(g2D, title)), 28);
			}			
			
			{
				if (showMode == 1) {setLayout(new GridLayout(3, 0, 0, 6));
				} else {setLayout(new GridLayout(4, 0, 0, 6));}
				
				setBorder(new EmptyBorder(26, 3, 3, 3));
				
				
				JPanel loginPane = new JPanel(new BorderLayout()) {
					@Override
					public void paintComponent(Graphics g) {
						g2D = (Graphics2D) g;
						g2D.setColor(Color.DARK_GRAY);
						g2D.fillRoundRect(32, 0, 70, 15, 3, 3);
												
						g2D.setFont(Registry.fMessage);
						g2D.setColor(Color.GRAY.brighter());
						g2D.drawString("login:", 28, 27);
					}
					
					{
						setOpaque(false);
						setBorder(new EmptyBorder(9, 18, 3, 18));
						
						loginField = new JTextField() {
							@Override
							public void paintComponent(Graphics g) {
								super.paintComponent(g);
								
								g2D = (Graphics2D) g;
								
								g2D.setColor(new Color(0.0f, 0.0f, 0.0f, 0.5f));
								g2D.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 19, 19);
								
								g2D.setStroke(new BasicStroke(1));
								g2D.setColor(Color.WHITE);
								g2D.drawRoundRect(2, 2, getWidth() - 6, getHeight() - 5, 16, 16);
								g2D.setColor(Color.GRAY);
								g2D.drawRoundRect(3, 2, getWidth() - 6, getHeight() - 6, 16, 16);
							}
							
							{
								setOpaque(false);
								setBorder(new EmptyBorder(9, 12, 1, 0));
								setForeground(Color.WHITE);
								setFont(Registry.fMessage);
								setCaretColor(Color.GREEN);
								addMouseListener(new MouseAdapter() {
									@Override
									public void mousePressed(MouseEvent e) {selectAll();}

									@Override
									public void mouseClicked(MouseEvent e) {selectAll();}
								});
							}
						};
						
						add(loginField);
					}
				};
				
				JPanel passPane = new JPanel(new BorderLayout()) {
					@Override
					public void paintComponent(Graphics g) {
						g2D = (Graphics2D) g;
						g2D.setColor(Color.DARK_GRAY);
						g2D.fillRoundRect(32, 0, 70, 15, 3, 3);
												
						g2D.setFont(Registry.fMessage);
						g2D.setColor(Color.GRAY.brighter());
						g2D.drawString("password:", 28, 22);
					}
					
					{
						setOpaque(false);
						setBorder(new EmptyBorder(3, 18, 9, 18));
//						setBorder(BorderFactory.createCompoundBorder(
//								new EmptyBorder(3, 18, 9, 18),
//								BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true), "Пароль:", 1, 2, Registry.fMessage, Color.GRAY.brighter())
//						));
						
						passField = new JPasswordField() {
							@Override
							public void paintComponent(Graphics g) {
								super.paintComponent(g);
								
								Graphics2D g2D = (Graphics2D) g;

//								Area area = new Area(new Rectangle(1, 1, getWidth() - 2, getHeight() - 2));
//								area.subtract(new Area(new Rectangle(12, 0, 70, 15)));
//								g2D.draw(area);
								
								g2D.setColor(new Color(0.0f, 0.0f, 0.0f, 0.5f));
								g2D.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 19, 19);
								
								g2D.setStroke(new BasicStroke(1));
								g2D.setColor(Color.WHITE);
								g2D.drawRoundRect(2, 2, getWidth() - 6, getHeight() - 5, 16, 16);
								g2D.setColor(Color.GRAY);
								g2D.drawRoundRect(3, 2, getWidth() - 6, getHeight() - 6, 16, 16);
							}
							
							{
								setOpaque(false);
//								setBackground(new Color(0.0f, 0.0f, 0.0f, 0.3f));
								setBorder(new EmptyBorder(9, 12, 1, 0));
								setForeground(Color.WHITE);
								setFont(Registry.fMessage);
								setCaretColor(Color.GREEN);
							}
						};
						
						add(passField);
					}
				};
				
				JPanel rePassPane = new JPanel(new BorderLayout()) {
					@Override
					public void paintComponent(Graphics g) {
						g2D = (Graphics2D) g;
						g2D.setColor(Color.DARK_GRAY);
						g2D.fillRoundRect(32, 0, 70, 15, 3, 3);
												
						g2D.setFont(Registry.fMessage);
						g2D.setColor(Color.GRAY.brighter());
						g2D.drawString("repeat password:", 28, 22);
					}
					
					{
						setOpaque(false);
						setBorder(new EmptyBorder(3, 18, 3, 18));
						
						repassField = new JPasswordField() {
							@Override
							public void paintComponent(Graphics g) {
								super.paintComponent(g);
								
								Graphics2D g2D = (Graphics2D) g;
								
								g2D.setColor(new Color(0.0f, 0.0f, 0.0f, 0.5f));
								g2D.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 19, 19);
								
								g2D.setStroke(new BasicStroke(1));
								g2D.setColor(Color.WHITE);
								g2D.drawRoundRect(2, 2, getWidth() - 6, getHeight() - 5, 16, 16);
								g2D.setColor(Color.GRAY);
								g2D.drawRoundRect(3, 2, getWidth() - 6, getHeight() - 6, 16, 16);
							}
							
							{
								setOpaque(false);
//								setBackground(new Color(0.0f, 0.0f, 0.0f, 0.3f));
								setBorder(new EmptyBorder(9, 12, 1, 0));
								setForeground(Color.WHITE);
								setFont(Registry.fMessage);
								setCaretColor(Color.GREEN);
							}
						};
						
						add(repassField);
					}
				};
				
				
				buttonsPane = new JPanel(new BorderLayout(6, 6)) {
					{
						setOpaque(false);
						setBorder(new EmptyBorder(0, 24, 21, 24));
						
						JPanel options = new JPanel(new BorderLayout(6, 6)) {
							{
								setOpaque(false);
								
								option = new JButton("") {
									@Override
									public void paintComponent(Graphics g) {
//										super.paintComponent(g);
										
										g2D = (Graphics2D) g;
										g2D.setColor(optionButColor);
										g2D.drawRoundRect(3, 0, 12, 12, 6, 6);

										if (IOM.getBoolean(IOM.HEADERS.LAST_USER, IOMs.LUSER.KEEP_PASS)) { // if (keepPass) { ...
											g2D.setColor(Color.ORANGE);
											g2D.drawRoundRect(6, 3, 6, 6, 3, 3);
										}
										
										g2D.drawString("Сохранить пароль", 21, 11);
									}
									
									{
										setBorder(null);
										setFocusPainted(false);
										setPreferredSize(new Dimension(150, 0));
										setBackground(new Color(0.0f, 0.0f, 0.0f, 0.0f));
										
										addActionListener(new ActionListener() {
											@Override	public void actionPerformed(ActionEvent e) {
												executeAltOption();
												buttonsPane.repaint();
											}
										});
										
										addMouseListener(new MouseAdapter() {
											@Override
											public void mouseExited(MouseEvent e) {
												optionButColor = Color.GRAY.brighter();
												buttonsPane.repaint();
											}

											@Override
											public void mouseEntered(MouseEvent e) {
												optionButColor = Color.ORANGE;
												buttonsPane.repaint();
											}
										});
									}
								};
								
								option2 = new JButton("") {
									@Override
									public void paintComponent(Graphics g) {
//										super.paintComponent(g);
										
										g2D = (Graphics2D) g;
//										g2D.setColor(Color.ORANGE);
										g2D.setColor(optionButColor2);
										
										g2D.drawString("Изменить никнейм", 3, 11);
									}
									
									{
										setBorder(null);
										setFocusPainted(false);
										setPreferredSize(new Dimension(120, 20));
										setBackground(new Color(0.0f, 0.0f, 0.0f, 0.0f));
										
										addActionListener(new ActionListener() {
											@Override	public void actionPerformed(ActionEvent e) {
												setNewNickName();
												buttonsPane.repaint();
											}
										});
										
										addMouseListener(new MouseAdapter() {
											@Override
											public void mouseExited(MouseEvent e) {
												optionButColor2 = Color.GRAY.brighter();
												buttonsPane.repaint();
											}

											@Override
											public void mouseEntered(MouseEvent e) {
												optionButColor2 = Color.WHITE;
												buttonsPane.repaint();
											}
										});
									}
								};
								
								add(option, BorderLayout.WEST);
								add(option2, BorderLayout.EAST);
							}
						};
						
						JPanel buttons = new JPanel(new GridLayout(0, 2, 6, 6)) {
							{
								setOpaque(false);
								
								JButton okButton = new JButton("=OK=") {
									{
										setBackground(Color.DARK_GRAY.darker());
										setForeground(Color.WHITE);
										setFocusPainted(false);
										setFont(Registry.fMenuBarBig);
										addActionListener(new ActionListener() {									
											@Override
											public void actionPerformed(ActionEvent e) {onOkButtonClick();}
										});
									}
								};
								
								JButton cancelButton = new JButton("CANCEL") {
									{
										setBackground(Color.DARK_GRAY.darker());
										setForeground(Color.WHITE);
										setFocusPainted(false);
										setFont(Registry.fMenuBarBig);
										addActionListener(new ActionListener() {									
											@Override
											public void actionPerformed(ActionEvent e) {onExitButtonClick();}
										});
									}
								};
								
								add(okButton);
								add(cancelButton);
							}
						};
						
						add(options, BorderLayout.NORTH);
						add(buttons, BorderLayout.CENTER);
					}
				};
				
				
				add(loginPane);
				add(passPane);
				if (showMode == 0) {add(rePassPane);}
				add(buttonsPane);
			}
		};
						
		add(basePane);
		
		addMouseListener(this);
		addMouseMotionListener(this);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		
		loadResources();
		
		if (showMode == 1) {
			loginField.setText(Registry.login == null ? IOM.getString(IOM.HEADERS.LAST_USER, IOMs.LUSER.LAST_USER) : Registry.login);
			loginField.setEditable(Registry.login == null ? true : false);
			
			if (IOM.getBoolean(IOM.HEADERS.LAST_USER, IOMs.LUSER.KEEP_PASS)) {
				passField.setText(IOM.getString(IOM.HEADERS.LAST_USER, IOMs.LUSER.LAST_PASSWORD));
			}
		}
	}

	private static void executeAltOption() {
		IOM.set(IOM.HEADERS.LAST_USER, IOMs.LUSER.KEEP_PASS, !IOM.getBoolean(IOM.HEADERS.LAST_USER, IOMs.LUSER.KEEP_PASS));
		
		buttonsPane.repaint();
		option.repaint();
	}
	
	private void setNewNickName() {
		if (loginField.getText().isBlank()) {return;}
		
		String newName = 
				(String) JOptionPane.showInputDialog(
						LoginFrame.this, "Новый никнейм:", "Смена ника:",
						JOptionPane.PLAIN_MESSAGE, null, 
						null, loginField.getText());
		
		try {
			Files.copy(Paths.get("./resources/user/" + loginField.getText()), Paths.get("./resources/user/" + newName), StandardCopyOption.REPLACE_EXISTING);
			IOM.set(IOM.HEADERS.LAST_USER, IOMs.LUSER.LAST_USER, newName);
			loginField.setText(newName);
		} catch (IOException e) {e.printStackTrace();}
	}
	
	private static void loadResources() {
		try {
			ResManager.add("requestImage", new File("./resources/images/requestImage.png"));
			
			ResManager.add("cur_0", new File("./resources/images/0.png"));
			ResManager.add("cur_1", new File("./resources/images/1.png"));
			
			ResManager.add("grass", new File("./resources/images/grass.png"));
			ResManager.add("userListEdge", new File("./resources/images/userListEdge.png"));
			
			ResManager.add("onlineImage", new File("./resources/images/onlineImage.png"));
			ResManager.add("offlineImage", new File("./resources/images/offlineImage.png"));
			ResManager.add("afkImage", new File("./resources/images/afkImage.png"));
			
			ResManager.add("resetIPButtonImage", new File("./resources/images/resetIPButtonImage.png"));
			ResManager.add("sendButtonImage", new File("./resources/images/DEFAULT/btn.png"));
			
			ResManager.add("switchOffImage", new File("./resources/images/switchOff.png"));
			ResManager.add("switchOffoverImage", new File("./resources/images/switchOffover.png"));
			ResManager.add("switchOnImage", new File("./resources/images/switchOn.png"));
			ResManager.add("switchOnoverImage", new File("./resources/images/switchOnover.png"));
			
			ResManager.add("sysImageBkg", new File("./resources/images/sysImageBkg.png"));
		} catch (Exception e) {regResourcesLoadErrorAndExit(e);}
		
		try {
			File[] sounds = new File("./resources/sounds/").listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					if (pathname.isFile() && pathname.getName().endsWith(".mp3")) {return true;}
					return false;
				}
			});
			for (int i = 0; i < sounds.length; i++) {
				Media.addSound(sounds[i].getName().substring(0, sounds[i].getName().length() - 4), sounds[i]);				
			}
		} catch (Exception e) {regResourcesLoadErrorAndExit(e);}
	}
	
	public static void setUIStyleView() {
		if (IOM.getBoolean(IOM.HEADERS.CONFIG, IOMs.CONFIG.USE_UI_STYLE)) {
			try {UIManager.setLookAndFeel(new NimbusLookAndFeel());
		    } catch (Exception e) {
		    	try{UIManager.setLookAndFeel("com.jgoodies.plaf.plastic.PlasticXPLookAndFeel");
				} catch (Exception e2){
					try{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					} catch (Exception e3){
						System.err.println("Couldn't get specified look and feel, for some reason: " + e3.getMessage());
						Out.Print(MainClass.class, 2, "Setup the UIManagers L&F-style was failed. Cause: " + e3.getCause());
					}
				}
			}
		}
	}
	
	private static void regResourcesLoadErrorAndExit(Exception e) {
		JOptionPane.showConfirmDialog(null, 
				"<HTML>Произошла ошибка<br>при загрузке ресурсов!<br>", e.getMessage(), 
				JOptionPane.PLAIN_MESSAGE, JOptionPane.WARNING_MESSAGE);
		
		Exit.exit(Registry.RESOURCES_LOAD_FAIL);
	}
	
	
	private static void onOkButtonClick() {
		if (loginField.getText().isBlank() || new String(passField.getPassword()).isBlank()) {
			showInfoDialog();
			return;
		}
		
		if (showMode == 1) {
			Out.Print(LoginFrame.class, 1, "Try to login user '" + loginField.getText() + "' with password '" + new String(passField.getPassword()) + "'...");
		} else {
			Out.Print(LoginFrame.class, 1, "Try to create new user '" + loginField.getText() + "' with password '" + new String(passField.getPassword()) + "'...");
			if (new String(repassField.getPassword()).isBlank()) {
				showInfoDialog();
				return;
			}
			
			if (!new String(repassField.getPassword()).equals(new String(passField.getPassword()))) {
				showPNEDialog();
				return;
			}
		}
		
		MessageDTO ndpm = new MessageDTO(GlobalMessageType.PASS_REQUEST, loginField.getText(), "SERVER");
		ndpm.setUid(IOM.getString(IOM.HEADERS.SECURE, "UID"));
		ndpm.setPassword(new String(passField.getPassword()));
		NetConnector.writeMessage(ndpm);
		return;
	 }

	
	private static void buildIOM() {
		IOM.add(IOM.HEADERS.CONFIG, new File("./resources/user/" + IOM.getString(IOM.HEADERS.LAST_USER, IOMs.LUSER.LAST_USER) + "/config.cfg"));		
		IOM.setIfNotExist(IOM.HEADERS.CONFIG, IOMs.CONFIG.RENDER_ON, true);
		IOM.setIfNotExist(IOM.HEADERS.CONFIG, IOMs.CONFIG.USE_UI_STYLE, true);
		IOM.setIfNotExist(IOM.HEADERS.CONFIG, IOMs.CONFIG.UI_STYLE, 2);
		
		IOM.setIfNotExist(IOM.HEADERS.CONFIG, IOMs.CONFIG.ANIMATION_ENABLED, true);
		IOM.setIfNotExist(IOM.HEADERS.CONFIG, IOMs.CONFIG.SOUNDS_ENABLED, true);
		
		IOM.setIfNotExist(IOM.HEADERS.CONFIG, IOMs.CONFIG.USE_DIALOGPANE_OPACITY, true);		
		IOM.setIfNotExist(IOM.HEADERS.CONFIG, IOMs.CONFIG.SHOW_USERS_PANEL, true);
		IOM.setIfNotExist(IOM.HEADERS.CONFIG, IOMs.CONFIG.SHOW_LEFT_PANEL, false);
		
		IOM.setIfNotExist(IOM.HEADERS.CONFIG, IOMs.CONFIG.AFK_TIME_SEC, 600); // 10 min before AKF
		IOM.setIfNotExist(IOM.HEADERS.CONFIG, IOMs.CONFIG.LOAD_HISTORY_LINES, 30);
		
		Media.setSoundEnabled(IOM.getBoolean(IOM.HEADERS.CONFIG, IOMs.CONFIG.SOUNDS_ENABLED));
	}

	private static void closeLoginFrame() {
		loginFrame.dispose();
		setUIStyleView();
//		if (!ChatFrame.showFrame()) {new ChatFrame();}
	}

	private static void onExitButtonClick() {
		NetConnector.disconnect();
		Exit.exit(0);
	 }

	
	private static void showInfoDialog() {
		JOptionPane.showConfirmDialog(null, 
				"<html>Не достаточно данных!<hr>Необходимо заполнить все поля.", "Какие-то проблемы?", 
				JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE);
	}
	
	private static void showPNEDialog() {
		JOptionPane.showConfirmDialog(null, 
				"<html>Ошибка!<hr>Пароли не совпадают.", "Разные данные:", 
				JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
	}
	
	public static void showDeniedDialog(String cause) {
		JOptionPane.showConfirmDialog(null, 
				"<html>Отказ в доступе!<hr>Сервер не доступен, либо<br>логин-пароль не верны.", cause, 
				JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
	}
	
	public static void readyChatCreate() {
		IOM.set(IOM.HEADERS.LAST_USER, IOMs.LUSER.LAST_USER, loginField.getText());
		IOM.set(IOM.HEADERS.LAST_USER, IOMs.LUSER.LAST_PASSWORD, new String(passField.getPassword()));
		
		buildIOM();				
		IOM.saveAll();				
		closeLoginFrame();
		new ChatFrame();
	}

	
	@Override
	public void mouseDragged(MouseEvent e) {
		try {
			LoginFrame.this.setLocation(
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
}