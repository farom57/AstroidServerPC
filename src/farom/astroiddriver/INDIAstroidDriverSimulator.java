/**
 * 
 */
package farom.astroiddriver;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import laazotea.indi.INDIException;

/**
 * Simulates the arduino board software
 * @author farom
 *
 */
public class INDIAstroidDriverSimulator extends INDIAstroidDriver {

	private static float SIDERAL_RATE=86400.f/86164.f/4.f;
	private static int UPDATE_TIME_MS = 2;
	private static float UPDATE_TIME = UPDATE_TIME_MS/1000.f;
	private static int TIMEOUT_STATUS = 200;
	
	private int ms_count=0;
	private int step_ha=0;
	private int step_de=0;
	private int step_focus=0;
	private float ustep_ha=0;
	private float ustep_de=0;
	private float ustep_focus=0;
	private float move_speed_ha=1;
	private float move_speed_de=0;
	private float move_speed_focus=0;
	private int power_aux1=0;
	private int power_aux2=0;
	private int power_aux3=0;
	private byte bulb_state=0;
	
	

	
	private boolean connected = false;
	
	private Timer timer1ms;
	private Timer timer1s;
	private TimerTask task1ms;
	private TimerTask task1s;
	
	public INDIAstroidDriverSimulator(InputStream inputStream, OutputStream outputStream) {
		super(inputStream, outputStream);
		
		task1ms = new TimerTask(){
			@Override
			public void run() {
				updateStep();
			}			
		};
		timer1ms = new Timer();
		
		
		task1s = new TimerTask(){
			@Override
			public void run() {
				sendStatus();
			}			
		};
		timer1s = new Timer();
		
	}

	protected void sendStatus() {
		lastStatusMessage = new StatusMessage(ms_count, step_ha, step_de, ustep_ha, ustep_de, move_speed_ha, move_speed_de, 1.f, 1.f, power_aux1, power_aux2, power_aux3, bulb_state, step_focus, ustep_focus, move_speed_focus);
		//printMessage(lastStatusMessage.toString());
		updateStatus();
		
	}

	protected void updateStep() {
	    float speed_ha=SIDERAL_RATE*move_speed_ha;
	    float speed_de=SIDERAL_RATE*move_speed_de;
	    float speed_focus=move_speed_focus;
	    
	    ustep_ha+=speed_ha*UPDATE_TIME*1024.;
	    ustep_de+=speed_de*UPDATE_TIME*1024.;
	    ustep_focus+=speed_focus*UPDATE_TIME*1024.;

	    if(ustep_ha>=1024.){
	        ustep_ha-=1024.;
	        step_ha++;
	    }
	    if(ustep_ha<0.){
	        ustep_ha+=1024.;
	        step_ha--;
	    }
	    if(ustep_de>=1024.){
	        ustep_de-=1024.;
	        step_de++;
	    }
	    if(ustep_de<0.){
	        ustep_de+=1024.;
	        step_de--;
	    }
	    if(ustep_focus>=1024.){
	        ustep_focus-=1024.;
	        step_focus++;
	    }
	    if(ustep_focus<0.){
	        ustep_focus+=1024.;
	        step_focus--;
	    }
	    
		
	    ms_count+=UPDATE_TIME_MS;
	}

	@Override
	public void driverConnect(Date timestamp) throws INDIException {
		onConnected();
		timer1ms.scheduleAtFixedRate(task1ms, UPDATE_TIME_MS,UPDATE_TIME_MS);
		timer1s.scheduleAtFixedRate(task1s, TIMEOUT_STATUS, TIMEOUT_STATUS);
	}

	@Override
	public void driverDisconnect(Date timestamp) throws INDIException {
		onDisconnected();
		timer1ms.cancel();
		timer1s.cancel();
	}

	@Override
	protected void sendCommand() {
		move_speed_ha = command.getSpeedHA();
		move_speed_de = command.getSpeedDE();
		move_speed_focus = command.getSpeedFOCUS();
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see laazotea.indi.driver.INDIDriver#getName()
	 */
	@Override
	public String getName() {
		return "Astroid Simulator";
	}

}
