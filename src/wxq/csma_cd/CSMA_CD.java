package wxq.csma_cd;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JLabel;

public class CSMA_CD extends JFrame {
	
	private JPanel contentPane;
	
	private JPanel hostPanel[];// hosts
	private JButton btnSend[];// send button
	private JTextField textField[];
	private JComboBox<String> comboBox[];// hosts number
	private JComboBox<Integer> cbbScale;// 时间的放慢倍数
	private DrawMsg drawPanel;
	
	private JButton btnSuspend;
	
	VirtualHost[] virtualHosts;//Array container of virtual hosts
	/**
	 * position x of 8 nodes(0-7).
	 */
	private final int nodesX[]={52,182,312,442,117,247,377,507,558};
	private final Integer[] scales={1,2,5,10,20,50,100};
	private String []hostsName;
	
	private ArrayList<Point> list;// span of host nodes in use 
	private ArrayList<Point> redline;// collision length of two host nodes
	private int []coStep;// the step of nodei that collision occurs
	private int []isCol;// 0: no collision,1:left collision,2: right collision,3: both side
	private int []idest;// the ith host's dest number
	private boolean isSuspend; 
	/**
	 * Launch the application.
	 */
 	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CSMA_CD frame = new CSMA_CD();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public CSMA_CD() {
		setTitle("CSMA-CD");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 660, 450);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		setContents();
		startThread();
	}
	
	protected void setContents() {
		hostPanel = new JPanel[8];
		btnSend = new JButton[8];
		textField = new JTextField[32];
		comboBox = new JComboBox[8];
		cbbScale = new JComboBox<Integer>();
		cbbScale.setBounds(561, 45, 70, 25);
		cbbScale.setModel(new DefaultComboBoxModel<Integer>(scales));
		contentPane.add(cbbScale);
		for (int k=0; k<8; k++){
			comboBox[k] = new JComboBox<String>();
		}
		list  = new ArrayList<Point>();	
		redline = new ArrayList<Point>();	
		coStep = new int[8];
		isCol = new int[8];
		idest = new int[8];
		
		virtualHosts=new VirtualHost[8];
		hostsName = new String[]{"1","2","3","4","5","6","7","8"};
		for (int i=0; i<8;i++){
			hostPanel[i] = new JPanel();
			btnSend[i] = new JButton();
			hostPanel[i].setBounds(37+i/4*67+(i%4)*130,20+i/4*205, 104, 105);
			hostPanel[i].setLayout(null);
			hostPanel[i].add(btnSend[i]);
			btnSend[i].setBounds(0, 75, 104, 30);
			btnSend[i].setText("send");
			contentPane.add(hostPanel[i]);
			for (int j=0; j<4; j++){
				textField[i*4+j] = new JTextField();
				textField[i*4+j].setColumns(10);
				hostPanel[i].add(textField[i*4+j]);
			}
			textField[i*4+0].setBounds(0, 0, 104, 25);// input data area
			textField[i*4+0].setText("Hello Baby!");
			textField[i*4+0].getDocument().addDocumentListener(new InputDataDocListener(i));
			textField[i*4+1].setBounds(0, 25, 52, 25);// src host number
			textField[i*4+1].getDocument().addDocumentListener(new DocListener(i));
			textField[i*4+2].setBounds(0, 50, 52, 25);
			textField[i*4+3].setBounds(52, 50, 52, 25);
	
			
			textField[i*4+2].setEditable(false);// timer value
			textField[i*4+2].setText("0");
			textField[i*4+3].setEditable(false);// cnt of retreat
			textField[i*4+3].setText("0");
			
			Point p = new Point(nodesX[i],nodesX[i]);
			list.add(p);
		
			btnSend[i].addActionListener(new BtnAction(i));
			
			
			virtualHosts[i]=new VirtualHost(hostsName[i],i);
			virtualHosts[i].start();
			textField[i*4+1].setText(hostsName[i]);// show host name
			
			comboBox[i].setBounds(52, 25, 52, 25);// dest host number
			comboBox[i].setModel(new DefaultComboBoxModel<String>(hostsName));
		
			hostPanel[i].add(comboBox[i]);
			
			coStep[i] = 0;
			isCol[i] = 0;// collisions occurred at right as default
			idest[i] = -1;// with no dest
		}
		isSuspend = false;
		btnSuspend = new JButton();
		btnSuspend.setBounds(37, 360, 104, 30);
		btnSuspend.setText("suspend");
		btnSuspend.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO 自动生成的方法存根
				if (!isSuspend){
					isSuspend = true;
					btnSuspend.setText("continue");			
				}else{
					isSuspend = false;
					btnSuspend.setText("suspend");
					
				}
				for (int i=0; i<8; i++){						
					virtualHosts[i].setIsSuspend(isSuspend);					
				}
			}
			
		});
		contentPane.add(btnSuspend);
		
		
		drawPanel = new DrawMsg(561, 100);
		drawPanel.setBounds(37, 125, 561, 100);
		contentPane.add(drawPanel);
		
		JLabel lblNewLabel = new JLabel("slow scale");
		lblNewLabel.setBounds(561, 20, 70, 20);
		contentPane.add(lblNewLabel);
	}
	public class DocListener implements DocumentListener
	{
		int texti;
		public DocListener(int i){
			texti = i;
		}
		@Override
		public void insertUpdate(DocumentEvent e) {
			System.out.println("insert update");
			int []index={0,0,0,0,0,0,0,0};
			for (int i=0; i<8; i++){
				index[i]= comboBox[i].getSelectedIndex();
			}
			hostsName[texti] = textField[texti*4+1].getText().trim();
			for (int i=0; i<8; i++){
				comboBox[i].removeAllItems();
				for (int j=0; j<8; j++){
					comboBox[i].addItem(hostsName[j]);
				}
				comboBox[i].setSelectedIndex(index[i]);
			}
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			// TODO 自动生成的方法存根
			System.out.println("remove update");
			int []index={0,0,0,0,0,0,0,0};
			for (int i=0; i<8; i++){
				index[i]= comboBox[i].getSelectedIndex();
			}
			hostsName[texti] = textField[texti*4+1].getText().trim();
			for (int i=0; i<8; i++){
				comboBox[i].removeAllItems();
				for (int j=0; j<8; j++){
					comboBox[i].addItem(hostsName[j]);
				}
				comboBox[i].setSelectedIndex(index[i]);
			}
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			System.out.println("changed update");
		}
		
	}     
	
	public class InputDataDocListener implements DocumentListener
	{
		int texti;
		public InputDataDocListener(int i){
			texti = i;
		}
		@Override
		public void insertUpdate(DocumentEvent e) {
			System.out.println("insert update");
			String s = textField[texti*4+0].getText().trim();
			System.out.println("==="+s+"-----------");
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			// TODO 自动生成的方法存根
			System.out.println("remove update");
			String s = textField[texti*4+0].getText().trim();
			System.out.println("==="+s+"-----------");
			
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			System.out.println("changed update");
			
		}
		
	}
	public class BtnAction implements ActionListener{
		private int hosti;
		public BtnAction(int i){
			hosti = i;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			// no reply if host is sending msg or there is a collision
			if (virtualHosts[hosti].getHostState()!=VirtualHost.STATE_QUIET){
				return;
			}
			// 先判断主机名是否为空
			if (textField[hosti*4+0].getText().equals("")// input message
					&& textField[hosti*4+1].getText().equals("")){// src host name
				JOptionPane.showMessageDialog(null,
						"input message or host name is null", "host",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			boolean canSend=true;
			// listening channel state
			for (int i=0; i<8; i++){
				if (i!=hosti && virtualHosts[i].getHostState()==VirtualHost.STATE_SEND){
					if (nodesX[hosti]<list.get(i).y 
							&& nodesX[hosti]>list.get(i).x){
						canSend = false;
						break;
					}
				}
			}
			// when collisions judge the red line
			if (canSend) {
				for (int i = 0; i < redline.size(); i++) {
					if (nodesX[hosti] < list.get(redline.get(i).y).x
							&& nodesX[hosti] > list.get(redline.get(i).x).y) {
						canSend = false;
						break;
					}
				}
			}
			if (canSend){
				virtualHosts[hosti].reStart();// reset collision and timer
				virtualHosts[hosti].setState(VirtualHost.STATE_SEND);				
				String destName = (String)comboBox[hosti].getSelectedItem();
				idest[hosti] = comboBox[hosti].getSelectedIndex();
				virtualHosts[hosti].setDest(destName);
				virtualHosts[hosti].setmsg(textField[hosti*4+0].getText().trim());
				System.out.println("btnSend-----send----"+destName);
				System.out.println("btnSend-----send----"+virtualHosts[hosti].getMsg());
			}
		}
		
	}
	private void startThread(){
		// 创建一个线程
		new Thread(new Runnable() {
			public void run() {
				try {
					while(true){
						if (isSuspend){
							Thread.sleep(50);
							continue;
						}
						// check collisions
						for (int i = 0; i < 8; i++) {
							for (int j = 0; j < 8; j++) {
								if (virtualHosts[i].getHostState() == VirtualHost.STATE_SEND
										&& virtualHosts[j].getHostState() == VirtualHost.STATE_SEND) {
									if (nodesX[i] < nodesX[j]
											&& list.get(i).y >= list.get(j).x) {
										if (isCol[i]==1){// left before
											isCol[i] = 3;// both side
											Point p = new Point(i,j);
											redline.add(p);
										}else if (isCol[i]==0){
											isCol[i] = 2;// right collision
											Point p = new Point(i,j);
											redline.add(p);
										}
										
									} else if (nodesX[i] > nodesX[j]
											&& list.get(i).x <= list.get(j).y) {
										if (isCol[i]==2){// right before
											isCol[i] = 3;// both side
											Point p = new Point(j,i);
											redline.add(p);
										}else if (isCol[i]==0){
											isCol[i] = 1;// left collision
											Point p = new Point(j,i);
											redline.add(p);
										}
										
									}
								}
							}
						}
						
						for (int i=0; i<8; i++){
							textField[i*4+2].setText(String.valueOf(virtualHosts[i].getTimer()));
							textField[i*4+3].setText(String.valueOf(virtualHosts[i].getCollisions()));
						}
						repaint();
						int scale = (int)cbbScale.getSelectedItem();
						VirtualHost.scale = scale;
						Thread.sleep(10*scale);
						
						System.out.println("====");
						// 判断是否发送成功，
					}
				} catch (InterruptedException b) {
					b.printStackTrace();
				}
			}
		}).start();
		
	}
	
	public class DrawMsg extends JPanel
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 4361447632334649859L;
		private int width;
		private int height;
		private Graphics2D g2d;
		public DrawMsg(int width,int height)
		{
			setSize(width, height);	
			this.width = width;
			this.height = height;
		}
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			g2d = (Graphics2D) g;
			g2d.setColor(Color.GREEN);
			g2d.fillRect(0, height/2, width, 3);// 画横轴
			for (int i=0; i<8; i++){// 画纵轴
				g2d.fillRect(52+(i%4)*130+(i/4)*65, (i/4)*height/2, 3, height/2);
			}
			
			final Color[] color={Color.gray,Color.yellow,Color.blue,Color.orange,
					Color.cyan,Color.orange,Color.gray,Color.yellow};
			int sendCnt = 0;
			for (int k=0; k<8; k++){
				if (virtualHosts[k].getHostState()==VirtualHost.STATE_SEND){
					g2d.setColor(color[k]);
					// hostk access channel
					if (isCol[k]==0){// no collision
						list.get(k).x -= 5;
						list.get(k).y += 5;
						g2d.fillRect(nodesX[k], (k/4)*height/2, 4, height/2);
					}else if (isCol[k]==1){// left collision
						list.get(k).x += 5;
						list.get(k).y += 5;
						if (nodesX[k]-list.get(k).x>=5){
							g2d.fillRect(nodesX[k], (k/4)*height/2, 4, height/2);
						}else if (virtualHosts[k].getHostState()!=VirtualHost.STATE_COLLISION){
							virtualHosts[k].setState(VirtualHost.STATE_COLLISION);
							System.out.println("==set state collision1==");
						}
						
					}else if (isCol[k]==2){// right collision
						list.get(k).x -= 5;
						list.get(k).y -= 5;
						if (list.get(k).y-nodesX[k]>=5){
							g2d.fillRect(nodesX[k], (k/4)*height/2, 4, height/2);
						}else if (virtualHosts[k].getHostState()!=VirtualHost.STATE_COLLISION){
							virtualHosts[k].setState(VirtualHost.STATE_COLLISION);
							System.out.println("==set state collision2==");
						}
					}else if (isCol[k]==3){ //both side collision
						list.get(k).x += 5;
						list.get(k).y -= 5;
						if (list.get(k).y-nodesX[k]>=5 && nodesX[k]-list.get(k).x>=5){
							g2d.fillRect(nodesX[k], (k/4)*height/2, 4, height/2);
						}else if (virtualHosts[k].getHostState()!=VirtualHost.STATE_COLLISION){
							virtualHosts[k].setState(VirtualHost.STATE_COLLISION);
							System.out.println("==set state collision3==");
						}
					}
					// 发送成功，通知消息
					if (virtualHosts[k].getHostState()==VirtualHost.STATE_SEND){
						if (nodesX[idest[k]] < nodesX[k] && list.get(k).x-nodesX[idest[k]]<=5
								|| nodesX[idest[k]] > nodesX[k] && nodesX[idest[k]]-list.get(k).y<=5){
							g2d.fillRect(nodesX[idest[k]], (idest[k]/4)*height/2, 4, height/2);
							System.out.println("has been sent:"+virtualHosts[k].hasBeenSent());
							
							if (virtualHosts[k].hasBeenSent()){
								textField[idest[k]*4+0].setText(virtualHosts[k].getMsg());
								virtualHosts[k].reStart();
							}							
						}
					}							
						
					if (list.get(k).x<0){
						list.get(k).x = 0;
					}
					if (list.get(k).y>width){
						list.get(k).y = width;
					}
					// 画横轴
					g2d.fillRect(list.get(k).x, height/2, list.get(k).y-list.get(k).x, 4);
					
					sendCnt++;
				}
			}
			// draw red lines to present collisions
			if (sendCnt == 0) {// channel is idle
				g2d.fillRect(0, height/2, width, 3);// 画横轴
				for (int i=0; i<8; i++){
					isCol[i] = 0;	
					list.get(i).x = nodesX[i];
					list.get(i).y = nodesX[i];
				}
				redline.clear();

			}else{
				for (int i = 0; i < redline.size(); i++) {
					g2d.setColor(Color.red);
					g2d.fillRect(
							list.get(redline.get(i).x).y,
							height / 2,
							list.get(redline.get(i).y).x
									- list.get(redline.get(i).x).y, 4);
				}
			}
		}
		
	}
}
