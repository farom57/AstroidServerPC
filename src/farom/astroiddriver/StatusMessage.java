package farom.astroiddriver;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

/**
 * hold the status message from the device
 * @author farom
 */
public class StatusMessage{
	public static final int MESSAGE_SIZE = 56;
	protected long time;
	protected int msCount;
	protected int stepHA;
	protected int stepDE;
	protected float uStepHA;
	protected float uStepDE;
	protected float moveSpeedHA;
	protected float moveSpeedDE;
	protected float powerHA;
	protected float powerDE;
	protected int powerAUX1;
	protected int powerAUX2;
	protected int powerAUX3;
	protected byte bulbState;
	protected int stepFOCUS;
	protected float uStepFOCUS;
	protected float moveSpeedFOCUS;
	
	/**
	 * Create the StatusMessage from the buffer
	 * @param buffer
	 */
	public StatusMessage(byte buffer[]){
		time = (new Date()).getTime();
		msCount = ByteBuffer.wrap(buffer,0,4).order(ByteOrder.BIG_ENDIAN).getInt();
        stepHA = ByteBuffer.wrap(buffer,4,4).order(ByteOrder.BIG_ENDIAN).getInt();
        stepDE = ByteBuffer.wrap(buffer,8,4).order(ByteOrder.BIG_ENDIAN).getInt();
        uStepHA = ByteBuffer.wrap(buffer,12,4).order(ByteOrder.BIG_ENDIAN).getFloat();
        uStepDE = ByteBuffer.wrap(buffer,16,4).order(ByteOrder.BIG_ENDIAN).getFloat();
        moveSpeedHA = ByteBuffer.wrap(buffer,20,4).order(ByteOrder.BIG_ENDIAN).getFloat();
        moveSpeedDE = ByteBuffer.wrap(buffer,24,4).order(ByteOrder.BIG_ENDIAN).getFloat();
        powerHA = ByteBuffer.wrap(buffer,28,4).order(ByteOrder.BIG_ENDIAN).getFloat();
        powerDE = ByteBuffer.wrap(buffer,32,4).order(ByteOrder.BIG_ENDIAN).getFloat();
        powerAUX1 = parseInt(buffer[36],buffer[37]);
        powerAUX2 = parseInt(buffer[38],buffer[39]);
        powerAUX3 = parseInt(buffer[40],buffer[41]);
        bulbState = buffer[42];
        stepFOCUS = ByteBuffer.wrap(buffer,43,4).order(ByteOrder.BIG_ENDIAN).getInt();
        uStepFOCUS = ByteBuffer.wrap(buffer,47,4).order(ByteOrder.BIG_ENDIAN).getFloat();
        moveSpeedFOCUS = ByteBuffer.wrap(buffer,51,4).order(ByteOrder.BIG_ENDIAN).getFloat();
	}
	
	public StatusMessage(int ms, int HA, int DE, float uHA, float uDE, float mHA, float mDE, float pHA, float pDE, int pA1, int pA2,  int pA3, byte bulb, int FO, float uFO, float mFO){
		time = (new Date()).getTime();
		msCount = ms;
		stepHA= HA;
		stepDE = DE;
		uStepHA = uHA;
		uStepDE = uDE;
		moveSpeedHA = mHA;
		moveSpeedDE = mDE;
		powerHA = pHA;
		powerDE = pDE;
		powerAUX1 = pA1;
		powerAUX2 = pA2;
		powerAUX3 = pA3;
		bulbState = bulb;
		stepFOCUS = FO;
		uStepFOCUS = uFO;
		moveSpeedFOCUS = mFO;
	}
	
	/**
	 * Form a positive integer from two bytes
	 * @param high most significant byte
	 * @param low least significant byte
	 */
	private int parseInt(byte high, byte low){
		int result;
		if(high>=0){
			result = high * 256;
		}else{
			result = (high+256) * 256;
		}
		if(low>=0){
			result += low;
		}else{
			result += low + 256;
		}
		return result;
	}
	
	/**
	 * Validate the message according to the checksum 
	 * @param buffer
	 * @return true if the checksum is valid
	 */
	public static boolean verify(byte buffer[]){
		byte sum=0;
		for(int i=0; i<MESSAGE_SIZE-1; i++){
			sum+=buffer[i];
		}
		//System.out.println("sum:"+sum+" buffer[BUFFER_SIZE-1]:"+buffer[BUFFER_SIZE-1]);
		return (sum == buffer[MESSAGE_SIZE-1]);		
	}
	
	/**
	 * Empty StatusMessage
	 */
	StatusMessage(){
		time = 0;
		msCount = 0;
        stepHA = 0;
        stepDE = 0;
        uStepHA = 0;
        uStepDE = 0;
        moveSpeedHA=0;
        moveSpeedDE=0;
        powerHA = 0;
        powerDE= 0;
	}
	
	@Override
	public String toString(){
		return "recieved: "+(new Date(time))+"\nmsCount: "+msCount+"\nstepHA: "+stepHA+"\nstepDE: "+stepDE+"\nuStepHA: "+uStepHA+"\nuStepDE: "+uStepDE+"\nmoveSpeedHA: "+moveSpeedHA+"\nmoveSpeedDE: "+moveSpeedDE+"\npowerHA:"+powerHA+"\npowerDE:"+powerDE+"\npowerAux1:"+powerAUX1+"\npowerAux2:"+powerAUX2+"\npowerAux3:"+powerAUX3+"\nuStepFOCUS:"+uStepFOCUS+"\nstepFOCUS:"+stepFOCUS+"\nspeedFOCUS:"+moveSpeedFOCUS+"\n";			
	}


	/**
	 * @return the time
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @return the msCount
	 */
	public int getMsCount() {
		return msCount;
	}

	/**
	 * @return the stepHA
	 */
	public int getStepHA() {
		return stepHA;
	}

	/**
	 * @return the stepDE
	 */
	public int getStepDE() {
		return stepDE;
	}

	/**
	 * @return the uStepHA
	 */
	public float getuStepHA() {
		return uStepHA;
	}

	/**
	 * @return the uStepDE
	 */
	public float getuStepDE() {
		return uStepDE;
	}

	/**
	 * @return the moveSpeedHA
	 */
	public float getMoveSpeedHA() {
		return moveSpeedHA;
	}

	/**
	 * @return the moveSpeedDE
	 */
	public float getMoveSpeedDE() {
		return moveSpeedDE;
	}
	
	/**
	 * @return stepHA + uStepHA/1024
	 */
	public double getHA() {
		return (double)stepHA + (double)uStepHA/1024.;
	}

	/**
	 * @return stepDE + uStepDE/1024
	 */
	public double getDE() {
		return (double)stepDE + (double)uStepDE/1024.;
	}
	
	/**
	 * @return the powerHA
	 */
	public float getPowerHA() {
		return powerHA;
	}

	/**
	 * @return the powerDE
	 */
	public float getPowerDE() {
		return powerDE;
	}

	public int getPowerAUX1() {
		return powerAUX1;
	}

	public void setPowerAUX1(int powerAUX1) {
		this.powerAUX1 = powerAUX1;
	}

	public int getPowerAUX2() {
		return powerAUX2;
	}

	public void setPowerAUX2(int powerAUX2) {
		this.powerAUX2 = powerAUX2;
	}

	public int getPowerAUX3() {
		return powerAUX3;
	}

	public void setPowerAUX3(int powerAUX3) {
		this.powerAUX3 = powerAUX3;
	}

	public byte getBulbState() {
		return bulbState;
	}

	public void setBulbState(byte bulbState) {
		this.bulbState = bulbState;
	}

	public int getStepFOCUS() {
		return stepFOCUS;
	}

	public void setStepFOCUS(int stepFOCUS) {
		this.stepFOCUS = stepFOCUS;
	}

	public float getuStepFOCUS() {
		return uStepFOCUS;
	}

	public void setuStepFOCUS(float uStepFOCUS) {
		this.uStepFOCUS = uStepFOCUS;
	}

	public float getMoveSpeedFOCUS() {
		return moveSpeedFOCUS;
	}

	public void setMoveSpeedFOCUS(float moveSpeedFOCUS) {
		this.moveSpeedFOCUS = moveSpeedFOCUS;
	}
	
	public double getFOCUS() {
		return (double)stepFOCUS + (double)uStepFOCUS/1024.;
	}
	
}