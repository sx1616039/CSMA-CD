package wxq.csma_cd;

public class VirtualHost extends Thread{

	public final static int STATE_QUIET = 0;
	public final static int STATE_SEND = 1;
	public final static int STATE_COLLISION = 2;
	
	public static int scale;// 时间的放大系数
	/** the span of send a frame is 500 millisecond */
	private final int FRAME_SPAN = 500;
	/** suppose that a frame need 1000 milliseconds go and back */
	private final int SLOT_TIME = 2*FRAME_SPAN;
	/** the span of send a msg is 3000 millisecond */
	private final int MSG_SPAN = 3000;
	
	private String name;// host number
	private String dest;// dest number
	private String msg;
	
	private int id;//  host id
	private int collisions;// the number of collisions
	private int timer;// timer count down
	private int state;// 0: not idle, 1: send, 2: collision
	 
	private boolean isSent;
	private boolean isSuspend;
	
	private int slices;// 1秒钟分多次分片。
	
	public VirtualHost(String name, int id){
		this.id = id;
		this.name = name;
		dest = "";
		msg = "";
		state = STATE_QUIET;
		collisions = 0;
		timer = 0;
		isSent = false;
		isSuspend = false;
		scale = 1;
		slices = 0;
	}
	public void setHostName(String name){
		this.name = name;
	}
	public void setmsg(String msg){
		this.msg = name+":"+dest+":"+msg;
	}
	public void setDest(String dest){
		this.dest = dest;
	}
	public void setState(int state){
		this.state = state;
	}
	public void resetSendState(){
		this.isSent = false;
	}
	public void setScale(int scale){
		this.scale = scale;
	}
	public void setIsSuspend(boolean isSuspend){
		this.isSuspend = isSuspend;
	}
	// on button send
	public void reStart(){
		collisions = 0;
		timer = 0;
		isSent = false;
		state = STATE_QUIET;
	}
	public int getHostState(){
		return state;
	}
	public int getTimer(){
		return timer;
	}
	public String getDest(){
		return dest;
	}
	public int getCollisions(){
		return collisions;
	}
	public String getMsg(){
		return msg;
	}
	public boolean hasBeenSent(){
		return isSent;
	}
	public void run() {
		while (true) {
			// try to send: 16 times..
			while (collisions < 16) {
				if (isSuspend){
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
					continue;
				}
				// state = STATE_QUIET means:bus is not idle
				if (state == STATE_QUIET) {
//					System.out.println("host[" + id
//							+ "]bus is non-idle!sleep 1000 milliseconds");
					try {
						Thread.sleep(100*scale);
					} catch (InterruptedException e) {
						System.err.println("Inturrupted: Interrupt exception");
					}
				} else if (state == STATE_SEND && !isSent) {
					// state == STATE_SEND means: bus empty, packet can sent to
					// destination
					slices++;
					try {
						Thread.sleep(50*scale);
					} catch (InterruptedException e) {
						System.err.println("Inturrupted: Interrupt exception");
					}
					if (slices > SLOT_TIME / 50) {
						System.out.println("host[" + id
								+ "] send the msg without collisions!");
						
						try {
							Thread.sleep(2000*scale);// 前后共花费2秒钟
						} catch (InterruptedException e) {
							System.err.println("Inturrupted: Interrupt exception");
						}
						System.out.println("host[" + id
								+ "] has sent the msg successfully!");
						isSent = true;					
						slices = 0;
						
					}
				} else if (state == STATE_COLLISION){
					// state == STATE_COLLISION means:: collision between
					// packets
					slices = 0;
					// increase idle time after each failed transmission
					try {
						collisions++;
						BackOffTimer backofftimer = new BackOffTimer(collisions);
						int len = backofftimer.getTimer();
						timer = len;
						for (int i = 0; i < len; i++) {
							Thread.sleep(SLOT_TIME*scale);
							timer--;
						}
						state = STATE_SEND;
					} catch (InterruptedException e) {
						System.err.println("Inturrupted: Interrupt exception");
					}				
				}
			}
			// Retransmission failed for last 16 times? Transmission failure.
			if (collisions == 16){
				System.out.println("Host [" + name + "]has given up this frame!");
				collisions++;
			}
			
			if (collisions >= 16) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}
		}
	}
}
