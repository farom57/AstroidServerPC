package farom.astroiddriver;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CmdMessage {
	public static final int MESSAGE_SIZE = 32;
	protected float speedHA;
	protected float speedDE;
	protected float powerDE;
	protected float powerHA;
	protected int powerAUX1=0;
	protected int powerAUX2=0;
	protected int powerAUX3=0;
	protected byte bulbState=0;
	protected float speedFOCUS=0;
	protected float powerFOCUS=0;

	
	public CmdMessage() {
		speedHA=0;
		speedDE=0;
		powerHA=1;
		powerDE=1;
		powerAUX1=0;
		powerAUX2=0;
		powerAUX3=0;
		bulbState=0;
		speedFOCUS=0;
		powerFOCUS=0;
		
	}
	
	public byte[] getBytes(){
		ByteBuffer buffer = ByteBuffer.allocate(MESSAGE_SIZE);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.putFloat(0, speedHA);
		buffer.putFloat(4, speedDE);
		buffer.putFloat(8, powerHA);
		buffer.putFloat(12, powerDE);
		buffer.put(16,(byte) ((powerAUX1/256) & 0xFF));
		buffer.put(17,(byte) (powerAUX1 & 0xFF));
		buffer.put(18,(byte) ((powerAUX2/256) & 0xFF));
		buffer.put(19,(byte) (powerAUX2 & 0xFF));
		buffer.put(20,(byte) ((powerAUX3/256) & 0xFF));
		buffer.put(21,(byte) (powerAUX3 & 0xFF));
		buffer.put(22,(byte) (bulbState & 0xFF));
		buffer.putFloat(23, speedFOCUS);
		buffer.putFloat(27, powerFOCUS);
		
		byte[] array = buffer.array();
		int sum = 0;
		for(int i=0; i<MESSAGE_SIZE-1; i++){
			sum+=array[i];
		}
		array[MESSAGE_SIZE-1]=(byte) (sum & 0xFF);
		
//		System.out.print("send: ");
//		for(int i =0; i<MESSAGE_SIZE; i++){
//			System.out.printf("%02X ", array[i]);
//		}
//		System.out.println("");
		return array;
	}

	/**
	 * @return the speedHA
	 */
	public float getSpeedHA() {
		return speedHA;
	}

	/**
	 * @param speedHA the speedHA to set
	 */
	public void setSpeedHA(float speedHA) {
		this.speedHA = speedHA;
	}

	/**
	 * @return the speedDE
	 */
	public float getSpeedDE() {
		return speedDE;
	}

	/**
	 * @param speedDE the speedDE to set
	 */
	public void setSpeedDE(float speedDE) {
		this.speedDE = speedDE;
	}

	public void setPowerHA(float powerHA) {
		this.powerHA = powerHA;
	}
	
	public void setPowerDE(float powerDE) {
		this.powerDE = powerDE;
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

	public float getSpeedFOCUS() {
		return speedFOCUS;
	}

	public void setSpeedFOCUS(float speedFOCUS) {
		this.speedFOCUS = speedFOCUS;
	}

	public float getPowerFOCUS() {
		return powerFOCUS;
	}

	public void setPowerFOCUS(float powerFOCUS) {
		this.powerFOCUS = powerFOCUS;
	}
	
	public void enableBulb() {
		this.bulbState=1;
	}
	
	public void disableBulb() {
		this.bulbState=0;
	}
	
	

}
