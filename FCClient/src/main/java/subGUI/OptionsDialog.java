package subGUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import fox.adds.IOM;
import fox.builders.ResManager;
import media.Media;
import registry.IOMs;
import registry.Registry;


@SuppressWarnings("serial")
public class OptionsDialog extends JDialog implements ActionListener {
	private static Icon resetIPButtonIcon;
	private static Icon switchOnIcon, switchOnOverIcon;
	private static Icon switchOffIcon, switchOffOverIcon;
	
	private Color baseColor = Color.DARK_GRAY;
	private Color secondColor = Color.GRAY;
	private Color labelsColor = Color.WHITE;
	
	private static JTextField fieldIP, fieldPort;
	private static JCheckBox box1, box2, box3;
	private JLabel sml1, sml2;
	
	
	public OptionsDialog() {
		setTitle("Настройки чата:");
//		setAlwaysOnTop(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setMinimumSize(new Dimension(440, 260));
		
		init();
		
		JPanel connectPane = new JPanel(new BorderLayout(3, 3)) {
			{
				setBackground(Color.DARK_GRAY);
				setBorder(new EmptyBorder(3, 3, 3, 3));
				
				JPanel ipAndPortPane = new JPanel(new FlowLayout(1, 9, 0)) {
					{
						setOpaque(false);
						setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(
								BorderFactory.createLineBorder(secondColor, 1, true), "Connection:", 0, 2, Registry.fMenuBar, labelsColor), 
								new EmptyBorder(0, 0, 0, 0)));
						

						fieldIP = new JTextField("127.0.0.1", 10) {
							{
								setFont(Registry.fBigSphere);
								setBackground(Color.BLACK);
								setForeground(Color.GREEN);
								setCaretColor(Color.YELLOW);
								setHorizontalAlignment(0);
								setBorder(null);
								addFocusListener(new FocusAdapter() {
									@Override
									public void focusGained(FocusEvent e) {selectAll();}
								});
								addKeyListener(new KeyAdapter() {
									@Override
									public void keyReleased(KeyEvent e) {
										if (e.getKeyCode() != KeyEvent.VK_BACK_SPACE) {
											if (getText().length() == 3 || getText().length() == 7) {setText(getText() + ".");}
										}
									}
								});
							}
						};
						
						fieldPort = new JTextField("13900", 5) {
							{
								setFont(Registry.fBigSphere);
								setBackground(Color.BLACK);
								setForeground(new Color(0, 127, 255));
								setCaretColor(Color.YELLOW);
								setBorder(null);
								setHorizontalAlignment(0);
								addFocusListener(new FocusAdapter() {										
									@Override
									public void focusGained(FocusEvent e) {selectAll();}
								});
							}
						};
						
						JPanel ipPane = new JPanel(new BorderLayout(3, 3)) {
							{
								setOpaque(false);
								
								JLabel ipLabel = new JLabel("IP: ") {
									{
										setFont(Registry.fMenuBarBig);
										setForeground(labelsColor);
										setHorizontalAlignment(SwingConstants.RIGHT);
									}
								};
								
								JButton resetIPButton = new JButton(resetIPButtonIcon) {
									{
										setBackground(baseColor);
										setActionCommand("resetIP");
										setToolTipText("Reset to localhost");
										setPreferredSize(new Dimension(32, 32));
										setFocusPainted(false);
//										setBorderPainted(false);
//										setBorder(BorderFactory.createRaisedBevelBorder());
										addActionListener(OptionsDialog.this);
									}
								};
								
								add(ipLabel, BorderLayout.WEST);
								add(fieldIP, BorderLayout.CENTER);
								add(resetIPButton, BorderLayout.EAST);
							}
						};
						
						JPanel portPane = new JPanel(new BorderLayout(3, 3)) {
							{
								setOpaque(false);
								
								JLabel portLabel = new JLabel("PORT: ") {
									{
										setFont(Registry.fMenuBarBig);
										setForeground(labelsColor);
										setHorizontalAlignment(SwingConstants.RIGHT);
									}
								};
								JButton resetPortButton = new JButton(resetIPButtonIcon) {
									{
										setBackground(baseColor);
										setActionCommand("resetPort");
										setToolTipText("Reset to default port");
										setPreferredSize(new Dimension(32, 32));
										setFocusPainted(false);
//										setBorderPainted(false);
//										setBorder(BorderFactory.createRaisedBevelBorder());
										addActionListener(OptionsDialog.this);
									}
								};

								add(portLabel, BorderLayout.WEST);
								add(fieldPort, BorderLayout.CENTER);
								add(resetPortButton, BorderLayout.EAST);
							}
						};
						
						add(ipPane);
						add(portPane);
					}
				};

				JPanel switchPane = new JPanel(new GridLayout(2, 1)) {
					{
						setOpaque(false);
						setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(
								BorderFactory.createLineBorder(secondColor, 1, true), "Multimedia:", 0, 2, Registry.fMenuBar, labelsColor), 
								new EmptyBorder(0, 0, 0, 0)));
						
						box1 = new JCheckBox("Звуковые оповещения", switchOffIcon, false) {
							{
								setOpaque(false);
								setFocusPainted(false);
								setForeground(Color.WHITE);
								setFont(Registry.fLabels);
								setSelectedIcon(switchOffOverIcon);
								addItemListener(new ItemListener() {
									@Override
									public void itemStateChanged(ItemEvent e) {
										IOM.set(IOM.HEADERS.CONFIG, IOMs.CONFIG.SOUNDS_ENABLED, isSelected());
										Media.setSoundEnabled(isSelected());
										if (isSelected()) {setSelectedIcon(switchOnIcon);} else {setSelectedIcon(switchOffIcon);}
									}
								});
								addMouseListener(new MouseAdapter() {
									@Override
									public void mouseEntered(MouseEvent e) {
										if (isSelected()) {setSelectedIcon(switchOnOverIcon);
										} else {setSelectedIcon(switchOffOverIcon);}
									}
									
									@Override
									public void mouseExited(MouseEvent e) {
										if (isSelected()) {setSelectedIcon(switchOnIcon);
										} else {setSelectedIcon(switchOffIcon);}
									}
								});
							}
						};
						
						box2 = new JCheckBox("Разрешить анимацию", switchOffIcon, false) {
							{
								setOpaque(false);
								setFocusPainted(false);
								setForeground(Color.WHITE);
								setFont(Registry.fLabels);
								
								addItemListener(new ItemListener() {
									@Override
									public void itemStateChanged(ItemEvent e) {
										IOM.set(IOM.HEADERS.CONFIG, IOMs.CONFIG.ANIMATION_ENABLED, isSelected());
										if (isSelected()) {setSelectedIcon(switchOnIcon);} else {setSelectedIcon(switchOffIcon);}
//										if (isSelected()) {ChatFrame.zaglushko();}
									}
								});
								addMouseListener(new MouseAdapter() {
									@Override
									public void mouseEntered(MouseEvent e) {
										if (isSelected()) {setSelectedIcon(switchOnOverIcon);
										} else {setSelectedIcon(switchOffOverIcon);}
									}
									
									@Override
									public void mouseExited(MouseEvent e) {
										if (isSelected()) {setSelectedIcon(switchOnIcon);
										} else {setSelectedIcon(switchOffIcon);}
									}
								});
							}
						};
						
						add(box1);
						add(box2);
					}
				};
				
				JPanel downPane = new JPanel(new BorderLayout(3, 3)) {
					{
						setOpaque(false);
						setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(
								BorderFactory.createLineBorder(secondColor, 1, true), "Messages:", 0, 2, Registry.fMenuBar, labelsColor), 
								new EmptyBorder(0, 3, 3, 0)));
						
						JPanel otherPane = new JPanel(new GridLayout(2, 2)) {
							{
								setOpaque(false);
								
								
								JPanel sendMessagesType = new JPanel(new BorderLayout(3, 3)) {
									{
										setOpaque(false);
										
										add(new JLabel("Отправка сообщений по ") {{setForeground(Color.WHITE);}});
									}
								};
								
								JPanel sendMessagesType2 = new JPanel(new BorderLayout(3, 3)) {
									{
										setOpaque(false);
										
										add(new JLabel("Перенос строки по ") {{setForeground(Color.WHITE);}});
									}
								};
								
								sml1 = new JLabel() {{setForeground(Color.WHITE);}};
								sml2 = new JLabel() {{setForeground(Color.WHITE);}};
								
								JPanel sendMessagesLabel = new JPanel(new BorderLayout(3, 3)) {
									{
										setOpaque(false);
										
										add(sml1);
									}
								};
								
								JPanel sendMessagesLabel2 = new JPanel(new BorderLayout(3, 3)) {
									{
										setOpaque(false);
										
										add(sml2);
									}
								};
								
								add(sendMessagesType);
								add(sendMessagesLabel);
								
								add(sendMessagesType2);						
								add(sendMessagesLabel2);
							}
						};
				
						box3 = new JCheckBox(switchOffIcon, false) {
							{
								setOpaque(false);
								setFocusPainted(false);
								setForeground(Color.WHITE);
								setFont(Registry.fLabels);
								setSelectedIcon(switchOffOverIcon);
								addItemListener(new ItemListener() {
									@Override
									public void itemStateChanged(ItemEvent e) {
										IOM.set(IOM.HEADERS.CONFIG, IOMs.CONFIG.MSG_SEND_TYPE, isSelected() ? 1 : 0);
										if (isSelected()) {setSelectedIcon(switchOnIcon);} else {setSelectedIcon(switchOffIcon);}
										sml1.setText(box3.isSelected() ? "ENTER" : "CTRL+ENTER");
										sml2.setText(box3.isSelected() ? "CTRL+ENTER" : "ENTER");
									}
								});
								addMouseListener(new MouseAdapter() {
									@Override
									public void mouseEntered(MouseEvent e) {
										if (isSelected()) {setSelectedIcon(switchOnOverIcon);
										} else {setSelectedIcon(switchOffOverIcon);}
									}
									
									@Override
									public void mouseExited(MouseEvent e) {
										if (isSelected()) {setSelectedIcon(switchOnIcon);
										} else {setSelectedIcon(switchOffIcon);}
									}
								});
							}
						};
						
						add(otherPane, BorderLayout.CENTER);
						add(box3, BorderLayout.EAST);
					}
				};
				
				add(ipAndPortPane, BorderLayout.NORTH);
				add(switchPane, BorderLayout.CENTER);
				add(downPane, BorderLayout.SOUTH);
			}
		};
		
		add(connectPane);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				IOM.set(IOM.HEADERS.LAST_USER, IOMs.LUSER.LAST_IP, fieldIP.getText());
				IOM.set(IOM.HEADERS.LAST_USER, IOMs.LUSER.LAST_PORT, fieldPort.getText());
				IOM.save(IOM.HEADERS.CONFIG.name());
				
				dispose();
			}
		});
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		
		fieldIP.setText(IOM.getString(IOM.HEADERS.LAST_USER, IOMs.LUSER.LAST_IP));
		fieldPort.setText(IOM.getString(IOM.HEADERS.LAST_USER, IOMs.LUSER.LAST_PORT));
		
		box1.setSelected(IOM.getBoolean(IOM.HEADERS.CONFIG, IOMs.CONFIG.SOUNDS_ENABLED));
		box2.setSelected(IOM.getBoolean(IOM.HEADERS.CONFIG, IOMs.CONFIG.ANIMATION_ENABLED));
		box3.setSelected(IOM.getInt(IOM.HEADERS.CONFIG, IOMs.CONFIG.MSG_SEND_TYPE) == 1 ? true : false);
		
		sml1.setText(box3.isSelected() ? "ENTER" : "CTRL+ENTER");
		sml2.setText(box3.isSelected() ? "CTRL+ENTER" : "ENTER");
	}
	
	private static void init() {
		resetIPButtonIcon = new ImageIcon(ResManager.getFilesLink("resetIPButtonImage").getPath());
		switchOnIcon = new ImageIcon(ResManager.getFilesLink("switchOnImage").getPath());
		switchOnOverIcon = new ImageIcon(ResManager.getFilesLink("switchOnoverImage").getPath());
		switchOffIcon = new ImageIcon(ResManager.getFilesLink("switchOffImage").getPath());
		switchOffOverIcon = new ImageIcon(ResManager.getFilesLink("switchOffoverImage").getPath());
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
			case "resetIP": fieldIP.setText("localhost");
				break;
			case "resetPort": fieldPort.setText("13900");
				break;
			default:
		}
	}
}

//		Choice choice = new Choice();
//		choice.addItem("First");
//		choice.addItem("Second");
//		choice.addItem("Third");

//		Полезные методы класса Choice:
//		countItems() - считать количество пунктов в списке; 
//		getItem(int) - возвратить строку с определенным номером в списке; 
//		select(int) - выбрать строку с определенным номером; 
//		select(String) - выбрать определенную строку текста из списка. 