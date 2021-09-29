package subGUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import fox.adds.IOM;
import fox.builders.ResManager;
import gui.ChatFrame;
import gui.ChatStyler;
import net.NetConnector;
import registry.IOMs;


@SuppressWarnings("serial")
public class MenuBar extends JMenuBar implements ActionListener {
	private static Icon onlineIcon, offlineIcon, afkIcon;
	
	public static Color textColor;
	
	private static JLabel connectLabel;
	private JRadioButtonMenuItem styleDefault, styleGold, styleDark;
	private JCheckBoxMenuItem dpOpacityBox;
	
	
	@Override
	public void paintComponent(Graphics g) {
		g.drawImage(ResManager.getBImage("menuBarImage"), 0, 0, getWidth(), getHeight(), this);
	}
	
	public MenuBar(Color color) {
		{
			if (color != null) {textColor = color;}
			onlineIcon = new ImageIcon(ResManager.getFilesLink("onlineImage").getPath());
			offlineIcon = new ImageIcon(ResManager.getFilesLink("offlineImage").getPath());
			afkIcon = new ImageIcon(ResManager.getFilesLink("afkImage").getPath());
			
			setOpaque(false);
			setBorder(new EmptyBorder(9, 0, 9, 0));
			
			JMenu file = new JMenu("Чат") {
				{
					setOpaque(false);
					setIcon(new ImageIcon("./resources/images/file.png"));
					setBorder(new EmptyBorder(0, 3, 0, 6));
					setForeground(textColor);
					
					JMenuItem save = new JMenuItem("Сохранить в файл...") {
						{
							setOpaque(true);
							setActionCommand("save");
							addActionListener(MenuBar.this);
							setIcon(new ImageIcon("./resources/images/save.png"));
						}
					};
					JMenuItem logout = new JMenuItem("Отключиться") {
						{
							setOpaque(true);
							setActionCommand("logout");
							addActionListener(MenuBar.this);
						}
					};
					JMenuItem exit = new JMenuItem("Выход") {
						{
							setOpaque(true);
							setActionCommand("exit");
							addActionListener(MenuBar.this);
						}
					};
					
					add(save);
					add(logout);
					addSeparator();
					add(exit);
				}
			};
			
			JMenu viewMenu = new JMenu("Вид") {
				{
					setOpaque(false);
					setIcon(new ImageIcon("./resources/images/view.png"));
					setBorder(new EmptyBorder(0, 3, 0, 6));
					setForeground(textColor);

			        add(new JCheckBoxMenuItem("Левая панель", null, true) {
						{
							setOpaque(true);
							setActionCommand("leftPaneVis");
							addActionListener(MenuBar.this);
							setSelected(IOM.getBoolean(IOM.HEADERS.CONFIG, IOMs.CONFIG.SHOW_LEFT_PANEL));
						}
					});
			        add(new JCheckBoxMenuItem("Список юзеров", null, true) {
						{
							setOpaque(true);
							setActionCommand("rightPaneVis");
							addActionListener(MenuBar.this);
							setSelected(IOM.getBoolean(IOM.HEADERS.CONFIG, IOMs.CONFIG.SHOW_USERS_PANEL));
						}
					});
			        add(new JSeparator());
			        
			        add(new JMenuItem("Фон сменить") {
						{
							setActionCommand("bckChose");
							addActionListener(MenuBar.this);
						}
					});
			        
			        JMenu h1 = new JMenu("Фон:");
			        final JRadioButtonMenuItem bckBySize = new JRadioButtonMenuItem("Вписать") {
		        		{
		        			setActionCommand("bckBySize");
							addActionListener(MenuBar.this);
							setSelected(IOM.getInt(IOM.HEADERS.CONFIG, IOMs.CONFIG.BKG_DRAW_STYLE) == 0);
		        		}
		        	};
		        	final JRadioButtonMenuItem bkgFill = new JRadioButtonMenuItem("Замостить") {
		        		{
		        			setActionCommand("bkgFill");
							addActionListener(MenuBar.this);
							setSelected(IOM.getInt(IOM.HEADERS.CONFIG, IOMs.CONFIG.BKG_DRAW_STYLE) == 1);
		        		}
		        	};
		        	final JRadioButtonMenuItem bkgAsIs =  new JRadioButtonMenuItem("Как есть") {
		        		{
		        			setActionCommand("bkgAsIs");
							addActionListener(MenuBar.this);
							setSelected(IOM.getInt(IOM.HEADERS.CONFIG, IOMs.CONFIG.BKG_DRAW_STYLE) == 2);
		        		}
		        	};
		        	final JRadioButtonMenuItem bkgProp = new JRadioButtonMenuItem("По центру") {
		        		{
		        			setActionCommand("bkgProp");
							addActionListener(MenuBar.this);
							setSelected(IOM.getInt(IOM.HEADERS.CONFIG, IOMs.CONFIG.BKG_DRAW_STYLE) == 3);
		        		}
		        	};

			        new ButtonGroup() {
			        	{
			        		add(bckBySize);
					        add(bkgFill);
					        add(bkgAsIs);
					        add(bkgProp);
			        	}
			        };
			        
			        h1.add(bckBySize);
			        h1.add(bkgFill);
			        h1.add(bkgAsIs);
			        h1.add(bkgProp);
			        
			        add(h1);
			        
			        dpOpacityBox = new JCheckBoxMenuItem("Фильтр окна диалога") {
						{
							setOpaque(true);
							setActionCommand("dialogOpasity");
							addActionListener(MenuBar.this);
							setSelected(IOM.getBoolean(IOM.HEADERS.CONFIG, IOMs.CONFIG.USE_DIALOGPANE_OPACITY));
						}
					};
			        add(dpOpacityBox);
			        add(new JSeparator());
			        
			        styleDefault = new JRadioButtonMenuItem("Стиль Утро", null, IOM.getInt(IOM.HEADERS.CONFIG, IOMs.CONFIG.UI_STYLE) == 0) {
						{
							setActionCommand("stlDefault");
							addActionListener(MenuBar.this);
						}
					};
					styleGold = new JRadioButtonMenuItem("Смешанный", null, IOM.getInt(IOM.HEADERS.CONFIG, IOMs.CONFIG.UI_STYLE) == 1) {
						{
							setActionCommand("stlGold");
							addActionListener(MenuBar.this);
						}
					};
					styleDark = new JRadioButtonMenuItem("Стиль Вечер", null, IOM.getInt(IOM.HEADERS.CONFIG, IOMs.CONFIG.UI_STYLE) == 2) {
						{
							setActionCommand("stlEvening");
							addActionListener(MenuBar.this);
						}
					};
					 new ButtonGroup() {
				        	{
				        		add(styleDefault);
						        add(styleGold);
						        add(styleDark);
				        	}
				        };
				     add(styleDefault);
				     add(styleGold);
				     add(styleDark);
				}
			};
	        
	        JMenu optMenu = new JMenu("Настройки") {
				{
					setOpaque(false);
					setIcon(new ImageIcon("./resources/images/options.png"));
					setBorder(new EmptyBorder(0, 3, 0, 0));
					setForeground(textColor);
					
					add(new JMenuItem("Открыть опции") {
						{
							setActionCommand("tune");
							addActionListener(MenuBar.this);
						}
					});
				}
			};
	        
			add(file);
			add(viewMenu);
			add(optMenu);
			
//			add(Box.createHorizontalStrut(15));
			add(Box.createHorizontalGlue());
			
			connectLabel = new JLabel("Net state:", offlineIcon, SwingConstants.CENTER) {
				{
//					setFocusPainted(false);
					setPreferredSize(new Dimension(120, 24));
					setBackground(textColor == Color.BLACK ? Color.GRAY : Color.BLACK);
//					setActionCommand("connect");
//					addActionListener(MenuBar.this);
					setToolTipText("Double-click to AFK change");
					addMouseListener(new MouseAdapter() {						
						@Override
						public void mouseClicked(MouseEvent e) {
							if (e.getClickCount() >= 2) {
								NetConnector.setAfk(!NetConnector.isAfk());
								repaint();
							}
						}
					});
				}
			};
			add(connectLabel);
			
			add(Box.createHorizontalStrut(15));
//			add(Box.createHorizontalGlue());				
			
			 JMenu helpMenu = new JMenu("Help") {
					{
						setOpaque(false);
						setIcon(new ImageIcon("./resources/images/help.png"));
						setBorder(new EmptyBorder(0, 3, 0, 6));
						setForeground(textColor);
						
							JMenu h1 = new JMenu("Обратная связь:");
					        	JMenuItem h2 = new JMenuItem("AngelicaLis39@mail.ru");
					        	JMenuItem h3 = new JMenuItem("https://vk.com/anestorf");
				        	h1.add(h2);
				        	h1.add(h3);
				        	
		        		add(h1);
					}
				};
			add(helpMenu);
		}
	}

	public static JMenuBar getMenu() {return new MenuBar(null);}
	
	public static Color getCurrentTextColor() {return textColor;}
	
	private void choseNewBackgroundImage() {
		JFileChooser bkgImageChooser = new JFileChooser("./resources/images/backgrounds/") {
			{
				setDialogTitle("Новый бэкграунд:");
				setFileFilter(new FileNameExtensionFilter("Images", "PNG", "JPG"));
				setFileHidingEnabled(false);
				setFileSelectionMode(JFileChooser.FILES_ONLY);
				
			}
		};
		
		int result = bkgImageChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File newBkgFile = bkgImageChooser.getSelectedFile();
			BufferedImage newBkgImage;
			try {
				newBkgImage = ImageIO.read(newBkgFile);
				ChatFrame.setBackgroundImage(newBkgImage, newBkgFile.getCanonicalPath());
			} catch (IOException e) {
				JOptionPane.showConfirmDialog(this, "<HTML>Произошла ошибка<br>при открытии файла<br>" + newBkgFile, e.getMessage(), 
						JOptionPane.PLAIN_MESSAGE, JOptionPane.WARNING_MESSAGE);
				e.printStackTrace();
			}
		}		
	}
	
	public static void setConnLabelText(String text) {connectLabel.setText(text);}

	public static void updateConnectLabel(Color bkg, Color frg, String text) {
		if (connectLabel == null) {return;}
		
		connectLabel.setBackground(bkg);
		connectLabel.setForeground(frg);
		setConnLabelText(text);
		
		if (text.equals("On-Line")) {connectLabel.setIcon(onlineIcon);
		} else if (text.equals("On-Line (AFK)")) {connectLabel.setIcon(afkIcon);
		} else {connectLabel.setIcon(offlineIcon);}
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
			case "logout": 
				NetConnector.disconnect();
				if (!IOM.getBoolean(IOM.HEADERS.LAST_USER, IOMs.LUSER.KEEP_PASS)) {
					IOM.set(IOM.HEADERS.LAST_USER, IOMs.LUSER.LAST_PASSWORD, "");
					IOM.save(IOM.HEADERS.LAST_USER);
				}
				
				NetConnector.disconnect();
				ChatFrame.disposeFrame();
				new LoginFrame(1);			
				break;
			case "exit": ChatFrame.disconnectAndExit();
				break;
			case "save": ChatFrame.saveChatToFile();
				break;
				
			case "leftPaneVis": ChatFrame.switchLeftPaneVisible();
				break;
			case "rightPaneVis": ChatFrame.switchRightPaneVisible();
				break;
				
			case "bckChose": choseNewBackgroundImage();
				break;
			case "bckBySize": 	ChatStyler.setBackgroundFillStyle(0);
				break;
			case "bkgFill": 		ChatStyler.setBackgroundFillStyle(1);
				break;
			case "bkgAsIs": 	ChatStyler.setBackgroundFillStyle(2);
				break;
			case "bkgProp": 	ChatStyler.setBackgroundFillStyle(3);
				break;
			case "dialogOpasity": ChatFrame.setDialogOpacity(dpOpacityBox.isSelected());
				break;
				
			case "stlDefault": ChatStyler.setUIStyle(0);
				break;
			case "stlGold": 		ChatStyler.setUIStyle(1);
				break;
			case "stlEvening":ChatStyler.setUIStyle(2);
				break;
				
			case "tune": new OptionsDialog();
				break;
			default: 
		}
	}

}