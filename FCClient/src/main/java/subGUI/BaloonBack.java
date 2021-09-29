package subGUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import door.Message.MessageDTO;
import door.Message.MessageDTO.GlobalMessageType;
import fox.adds.IOM;
import fox.builders.FoxFontBuilder;
import fox.builders.ResManager;
import gui.ChatFrame;
import net.NetConnector.localMessageType;
import registry.IOMs;
import registry.Registry;


@SuppressWarnings("serial")
public class BaloonBack extends JPanel {
	private static SimpleDateFormat format = new SimpleDateFormat("(dd.MM HH:mm:ss)"); // "dd.MM.yyyy HH:mm:ss"
	private static BufferedImage sysBack = ResManager.getBImage("sysImageBkg");
	
	private JTextArea baloonTextArea;
	private Baloon baloon;
	private String header;
	private GridBagConstraints outGBC, incomeGBC, otherGBC;
	private boolean isSelected = false;

	private Color selectColor = new Color(0.0f, 0.0f, 0.1f, 0.5f);
	private Color selectColorB = new Color(0.1f, 0.1f, 0.2f, 0.25f);
	
	
	@Override
	public void paintComponent(Graphics g) {
		if (IOM.getBoolean(IOM.HEADERS.CONFIG, IOMs.CONFIG.DEBUG_GRAPHICS) || isSelected) {
			Graphics2D g2D = (Graphics2D) g;
			g2D.setColor(selectColor);
			g2D.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 6, 6);
			g2D.setColor(selectColorB);
			g2D.drawRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 6, 6);
			g2D.drawRoundRect(6, 6, getWidth() - 12, getHeight() - 12, 6, 6);
//			g2D.dispose();
		}
	}
	
	public BaloonBack(localMessageType inputOutput, GlobalMessageType globalType, String from, String to, String body,	String footer) {
		setLayout(new GridBagLayout());
		setBackground(selectColor);
		
		baloon = new Baloon(inputOutput, from, to, body, footer);
		

		outGBC = new GridBagConstraints() {
			{
				this.insets = new Insets(0, 0, 0, 60);
				this.anchor = GridBagConstraints.WEST;
				this.fill = GridBagConstraints.NONE;

				this.weightx = 1;	
			}
		};
		
		incomeGBC = new GridBagConstraints() {
			{
				this.anchor = GridBagConstraints.EAST;
				this.fill = GridBagConstraints.NONE;
				
				this.weightx = 1;
			}
		};
		
		otherGBC = new GridBagConstraints() {
			{
				this.anchor = GridBagConstraints.CENTER;
				this.fill = GridBagConstraints.BOTH;
				this.weightx = 1;
			}
		};
		
		if (inputOutput == localMessageType.OUTPUT) {add(baloon, outGBC);
		} else if (inputOutput == localMessageType.INPUT) {add(baloon, incomeGBC);
		} else {add(baloon, otherGBC);}
	}
	
	public BaloonBack(localMessageType inputOutput, MessageDTO mesDTO) {
		setLayout(new GridBagLayout());
		setBackground(selectColor);

		if (mesDTO.getTo() == null) {mesDTO.setTo("Всем");}
		
		baloon = new Baloon(inputOutput, mesDTO.getFrom(), mesDTO.getTo(), mesDTO.getBody());
		
		outGBC = new GridBagConstraints() {
			{
				this.insets = new Insets(0, 0, 0, 60);
				this.anchor = GridBagConstraints.WEST;
				this.fill = GridBagConstraints.NONE;

				this.weightx = 1;	
			}
		};
		
		incomeGBC = new GridBagConstraints() {
			{
				this.anchor = GridBagConstraints.EAST;
				this.fill = GridBagConstraints.NONE;
				
				this.weightx = 1;
			}
		};
		
		otherGBC = new GridBagConstraints() {
			{
				this.anchor = GridBagConstraints.CENTER;
				this.fill = GridBagConstraints.BOTH;
				this.weightx = 1;
			}
		};
		
		if (inputOutput == localMessageType.OUTPUT) {add(baloon, outGBC);
		} else if (inputOutput == localMessageType.INPUT) {add(baloon, incomeGBC);
		} else {add(baloon, otherGBC);}
	}

	public class Baloon extends JPanel {
		private final int LAYOUT_SPACING = 3;
		private final String from, body, to, strStart;
		private final JLabel downDataLabel;
		private final Color color;
		private final Color mesColSystem = new Color(0.075f, 0.1f, 0.125f, 0.6f);
		private final Color mesColOutput = new Color(0.0f, 0.75f, 0.75f, 0.6f);
		private final Color mesColInput = new Color(0.25f, 0.75f, 0.0f, 0.6f);
		private final Color mesColWarn = new Color(1.0f, 0.0f, 0.0f, 0.6f);
		private final localMessageType type;
		private final Baloon baloon;
		
		@Override
		public void paintComponent(Graphics g) {
			Graphics2D g2D = (Graphics2D) g.create();
			Registry.render(g2D);
			
			if (type == localMessageType.INFO && sysBack != null) {
				g2D.drawImage(sysBack, 0, 0, getWidth(), getHeight(), this);				
			} else {
				g2D.setColor(color);
				g2D.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
			}

			
			g2D.setColor(color);
			g2D.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 12, 12);

			g2D.setColor(type == localMessageType.INFO ? Color.RED : Color.BLACK);
			g2D.setFont(Registry.fLabels);
			g2D.drawString(strStart, 8, 16);
			
			g2D.setColor(Color.BLACK);
			g2D.drawString(to, (int) (10D + FoxFontBuilder.getStringWidth(g2D, strStart)) - 1, 17);
			g2D.setColor(Color.WHITE);
			g2D.drawString(to, (int) (10D + FoxFontBuilder.getStringWidth(g2D, strStart)), 16);
			
			g2D.dispose();
		}
		
		public Baloon(localMessageType type, String _from, String _to, String _body) {
			this(type, _from, _to, _body, null);
		}
		
		public Baloon(localMessageType type, String _from, String _to, String _body, String footer) {
			baloon = this;
			this.type = type;
			
			switch (type) {
				case OUTPUT: color = mesColOutput;
					break;					
				case INPUT: color = mesColInput;
					break;				
				case INFO: color = mesColSystem;
					break;				
				case WARN: color = mesColWarn;
					break;			
				default: color = Color.GRAY;
			}
			
			this.from = _from;
			this.body = _body;
			this.to = _to;
			
			this.strStart = " От: " + from + " кому ";
			header = strStart + to + " ";
			
			setOpaque(false);
			setBorder(new EmptyBorder(20, 9, 3, 9));
			
			setLayout(new BorderLayout(LAYOUT_SPACING, LAYOUT_SPACING));			
			
			baloonTextArea = new JTextArea(body) {
				@Override
				public void paintComponent(Graphics g) {
					Graphics2D g2D = (Graphics2D) g.create();
					Registry.render(g2D);
//					g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					
					g2D.setFont(getFont());
					g2D.setColor(new Color(0.0f, 0.0f, 0.0f, type == localMessageType.INFO ? 0.8f : 0.35f));
					g2D.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 6, 6);
					
					g2D.setColor(color);
					g2D.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 6, 6);
					
					g2D.setColor(Color.DARK_GRAY);
					g2D.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 6, 6);
					
					super.paintComponent(g2D);
					g2D.dispose();
				}
				
				{
					setBorder(new EmptyBorder(3, 6, 3, 6));
					setBackground(new Color(0,0,0,0));
					setForeground(Color.WHITE);
					
					setLineWrap(true);
					setWrapStyleWord(true);
				
					setEditable(false);
					setFocusable(false);
					setFont(Registry.fMessage);
					
					addMouseListener(new MouseAdapter() {						
						@Override
						public void mouseReleased(MouseEvent e) {
							ChatFrame.chatPanel.repaint();
//							baloon.repaint();
						}						
						
						@Override
						public void mousePressed(MouseEvent e) {
							ChatFrame.chatPanel.repaint();
							if (type != localMessageType.INFO) baloon.setSelected(!baloon.isSelected());
//							baloon.repaint();
						}						
						
						@Override
						public void mouseExited(MouseEvent e) {
//							baloon.repaint();
						}						
						
						@Override
						public void mouseEntered(MouseEvent e) {
//							baloon.repaint();
						}						
						
						@Override
						public void mouseClicked(MouseEvent e) {
//							baloon.repaint();
						}
					});
				}
			};
			
			downDataLabel = new JLabel(footer == null ? format.format(System.currentTimeMillis()) : footer) {
				{
					setHorizontalAlignment(RIGHT);
					setFont(Registry.fLabels);
					setForeground(type == localMessageType.INFO ? Color.RED : Color.BLACK);
				}
			};
			
			add(baloonTextArea, BorderLayout.CENTER);
			add(downDataLabel, BorderLayout.SOUTH);
		}

		protected void setSelected(boolean b) {isSelected  = b;}
		protected boolean isSelected() {	return isSelected;}

		public JTextArea getArea() {return baloonTextArea;}
		
		public String getHeaderText() {return header;}
		public String getAreaText() {return body;}
		public String getFooterText() {return downDataLabel.getText();}
		
		public JLabel getDataLabel() {return downDataLabel;}

		public int getVerticalShiftsSum() {
			return getBorder().getBorderInsets(baloon).top + getBorder().getBorderInsets(baloon).bottom 
					+ baloonTextArea.getBorder().getBorderInsets(baloonTextArea).top + baloonTextArea.getBorder().getBorderInsets(baloonTextArea).bottom
					+ LAYOUT_SPACING
					+ downDataLabel.getHeight();
		}
	}

	public Baloon getBaloon() {return baloon;}
}