package wxq.csma_cd;
import java.lang.Math;

/**
 * Backoff timer generator
 * @author Pranay Sarkar
 */
public class BackOffTimer {
	int timer;
	/**
	 *
	 * Randomly selected back-off time,
	 * Calculated according to the retransmission number
	 * random multiples by k times for k-th retransmission
	 * @param transNum  : number of rretransmission
	 * @return Random multiples
	 */
	public BackOffTimer(int transNum) { 
		int rndom;
		int temp;
		temp=Math.min(transNum,10);
		rndom=(int)(Math.random()*(Math.pow(2,temp)-1));
		timer = rndom; 
	}
	public int getTimer(){
		return timer;
	}
}
