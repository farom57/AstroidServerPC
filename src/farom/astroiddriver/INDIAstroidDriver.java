package farom.astroiddriver;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


import laazotea.indi.Constants;
import laazotea.indi.Constants.LightStates;
import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.Constants.SwitchStatus;
import laazotea.indi.INDIException;
import laazotea.indi.driver.*;

/**
 * A class representing a INDI Driver for the Astroid device.
 * 
 * @author farom
 */
public abstract class INDIAstroidDriver extends INDIDriver implements INDIConnectionHandler {

	/**
	 * number of steps per turn
	 */
	private static final int STEP_BY_TURN = 50 * 3 * 144;


	/**
	 * status turns red if no StatusMessage is received for
	 * CONNECTION_TIMEOUT_ALERT milliseconds
	 */
	private static final long CONNECTION_TIMEOUT_ALERT = 1000; // the link

	private static final double GOTO_STOP_DISTANCE = 1. / 60.;
	private static final double GOTO_SLOW_DISTANCE = 15. / 60.;
	private static final float MAX_SPEED = 623*2;
	private static final float GOTO_SPEED = 400;
	private static final float GOTO_ACC_T = 5;
	private static final float GOTO_SLOW_SPEED = GOTO_SPEED/10;

	/**
	 * sideral rate in arcmin/sec
	 */
	private static final double SIDERAL_RATE = 360. * 60. / 86164.09053;



	private INDINumberProperty geographicCoordP; // GEOGRAPHIC_COORD
	private INDINumberElement geographicCoordLatE; // LAT
	private INDINumberElement geographicCoordLongE; // LONG
	private INDINumberElement geographicCoordElevE; // ELEV

	private INDINumberProperty eqCoordP; // EQUATORIAL_EOD_COORD
	private INDINumberElement eqCoordRAE; // RA
	private INDINumberElement eqCoordDEE; // DEC

	private INDISwitchProperty onCoordSetP; // ON_COORD_SET
	private INDISwitchElement onCoordSetSlewE; // SLEW
	private INDISwitchElement onCoordSetTrackE; // TRACK
	private INDISwitchElement onCoordSetSyncE; // SYNC

	private INDISwitchProperty telescopeMotionNSP; // TELESCOPE_MOTION_NS
	private INDISwitchElement motionNE; // MOTION_NORTH
	private INDISwitchElement motionSE; // MOTION_SOUTH

	private INDISwitchProperty telescopeMotionWEP; // TELESCOPE_MOTION_WE
	private INDISwitchElement motionWE; // MOTION_WEST
	private INDISwitchElement motionEE; // MOTION_EAST

	private INDISwitchProperty abortMotionP; // TELESCOPE_ABORT_MOTION
	private INDISwitchElement abortMotionE; // ABORT_MOTION

	private INDINumberProperty timedGuideNSP; // TELESCOPE_TIMED_GUIDE_NS
	private INDINumberElement timedGuideNE; // TIMED_GUIDE_N
	private INDINumberElement timedGuideSE; // TIMED_GUIDE_S
	
	private INDINumberProperty timedGuideWEP; // TELESCOPE_TIMED_GUIDE_WE
	private INDINumberElement timedGuideWE; // TIMED_GUIDE_W
	private INDINumberElement timedGuideEE; // TIMED_GUIDE_E
	
	private INDINumberProperty telescopeInfoP; // TELESCOPE_INFO
	private INDINumberElement telescopeApertureE ; // TELESCOPE_APERTURE
	private INDINumberElement telescopeFocalLengthE ; // TELESCOPE_FOCAL_LENGTH 	
	private INDINumberElement guiderApertureE ; // GUIDER_APERTURE
	private INDINumberElement guiderFocalLengthE ; // GUIDER_FOCAL_LENGTH
	
	private INDINumberProperty intervalometerSettingsP;
	private INDINumberElement exposureTimeE;
	private INDINumberElement delayTimeE;
	private INDINumberElement exposureNumberE;
	
	private INDINumberProperty focusSpeedP; // FOCUS_SPEED
	private INDINumberElement focusSpeedE ; // FOCUS_SPEED_VALUE
	
	private INDINumberProperty focusTimerP; // FOCUS_TIMER
	private INDINumberElement focusTimerE ; // FOCUS_TIMER_VALUE
	
	private INDISwitchProperty focusMotionP; // FOCUS_MOTION
	private INDISwitchElement focusInwardE; // FOCUS_INWARD
	private INDISwitchElement focusOutwardE; // FOCUS_OUTWARD
	
	private INDISwitchProperty focusAbortMotionP; // FOCUS_ABORT_MOTION
	private INDISwitchElement focusAbortE; // ABORT
	
	private INDINumberProperty relFocusPosP; // REL_FOCUS_POSITION
	private INDINumberElement relFocusPosE ; // FOCUS_RELATIVE_POSITION
	
	private INDINumberProperty absFocusPosP; // ABS_FOCUS_POSITION
	private INDINumberElement absFocusPosE ; // FOCUS_ABSOLUTE_POSITION
	
	private INDISwitchProperty trackRateP; // TELESCOPE_TRACK_RATE
	private INDISwitchElement trackSideralE; // TRACK_SIDEREAL
	private INDISwitchElement trackSolarE; // TRACK_SOLAR
	private INDISwitchElement trackLunarE; // TRACK_LUNAR
	private INDISwitchElement trackCustomE; // TRACK_CUSTOM
	

	
	/**
	 * On German equatorial mounts, a given celestial position can be pointed in
	 * two ways. The counter-weight is generally down and the telescope up. -
	 * East means that the telescope is pointing toward the east when the
	 * counter-weight is down (and west when it is up). - West means that the
	 * telescope is pointing toward the west when the counter-weight is down
	 * (and east when it is up). When INVERT_DE = false and side = East, the
	 * declination increase when the DE speed is positive
	 */
	private INDISwitchProperty sideP; // side
	private INDISwitchElement sideEastE; // east
	private INDISwitchElement sideWestE; // west

	private INDINumberProperty timeLstP; // TIME_LST
	private INDINumberElement lstE; // LST

	// private INDISwitchProperty enableAxisP;
	// private INDISwitchElement enableDecE;
	// private INDISwitchElement enableRaE;
	//
	// private INDITextProperty commandP;
	// private INDITextElement commandE;

	private INDILightProperty linkStatusP;
	private INDILightElement linkStatusE;

	private INDINumberProperty motionRateP;
	private INDINumberElement motionRateE;
	
	private INDINumberProperty currentRateP;
	private INDINumberElement currentDERateE;
	private INDINumberElement currentRARateE;
	private INDINumberElement trackingRateE;
	
	private INDINumberProperty powerP;
	private INDINumberElement powerRAE;
	private INDINumberElement powerDEE;
	
	private INDINumberProperty powerAuxP;
	private INDINumberElement powerAux1E;
	private INDINumberElement powerAux2E;
	private INDINumberElement powerAux3E;

	
	protected StatusMessage lastStatusMessage;
	protected CmdMessage command;

	private double syncCoordHA;
	private double syncStepHA;
	private double syncCoordDE;
	private double syncStepDE;
	private double gotoTargetRA;
	private double gotoTargetDE;
	private boolean gotoActive;
	
	private double motionSpeed;
	private double trackSpeed = 1;
	private double slewDESpeed = 0;
	private double slewRASpeed = 0;
	private double slewFOCUSSpeed = 0;
	private double powerHA = 1;
	private double powerDE = 1;
	private double powerFOCUS = 1;
	private Date lastGotoUpdate;
	
	Timer intervalometer;
	TimerTask currentTask;	
	

	/**
	 * Constructs a INDIAstroidDriver with a particular
	 * <code>inputStream<code> from which to read the incoming messages (from clients) and a
	 * <code>outputStream</code> to write the messages to the clients.
	 * 
	 * @param inputStream
	 *            The stream from which to read messages
	 * @param outputStream
	 *            The stream to which to write the messages
	 */
	public INDIAstroidDriver(InputStream inputStream, OutputStream outputStream) {
		super(inputStream, outputStream);
		

		// ------------------------------------------
		// --- Setup INDI properties and elements ---
		// ------------------------------------------

		linkStatusP = new INDILightProperty(this, "link_status", "Link status", "Main Control", PropertyStates.IDLE);
		linkStatusE = new INDILightElement(linkStatusP, "USB/Serial", LightStates.ALERT);

		
		geographicCoordP = new INDINumberProperty(this, "GEOGRAPHIC_COORD", "Scope Location", "Scope Location",
				Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW); // GEOGRAPHIC_COORD
		geographicCoordLatE = new INDINumberElement(geographicCoordP, "LAT", "Lat (dd:mm:ss)", 0., -90, 90, 0.,
				"%010.6m");
		geographicCoordLongE = new INDINumberElement(geographicCoordP, "LONG", "Lon (dd:mm:ss)", 0., 0, 360, 0.,
				"%010.6m");
		geographicCoordElevE = new INDINumberElement(geographicCoordP, "ELEV", "Elevation (m)", 0., -1000, 100000, 0.,
				"%g");
		addProperty(geographicCoordP);

		
		timeLstP = new INDINumberProperty(this, "TIME_LST", "Local sidereal time", "Scope Location",
				Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RO);
		lstE = new INDINumberElement(timeLstP, "LST", "Local sidereal time", 0, 0, 24, 0, "%010.6m");
		addProperty(timeLstP);

		
		eqCoordP = new INDINumberProperty(this, "EQUATORIAL_EOD_COORD", "Eq. Coordinates", "Main Control",
				Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW); // EQUATORIAL_EOD_COORD
		eqCoordRAE = new INDINumberElement(eqCoordP, "RA", "RA (hh:mm:ss)", 0., 0, 24, 0, "%010.6m"); // RA
		eqCoordDEE = new INDINumberElement(eqCoordP, "DEC", "DEC (dd:mm:ss)", 0., -180, 180, 0, "%010.6m"); // DEC

		
		sideP = new INDISwitchProperty(this, "TELESCOPE_PIER_SIDE", "Telescope side", "Main Control",
				Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW, Constants.SwitchRules.ONE_OF_MANY); // TELESCOPE_MOTION_WE
		sideWestE = new INDISwitchElement(sideP, "PIER_EAST", "Pointing West", Constants.SwitchStatus.OFF);//Mount on the East side of pier (Pointing West).
		sideEastE = new INDISwitchElement(sideP, "PIER_WEST", "Pointing East", Constants.SwitchStatus.ON);// Mount on the West side of pier (Pointing East).

		
		onCoordSetP = new INDISwitchProperty(this, "ON_COORD_SET", "On Set", "Main Control",
				Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW, Constants.SwitchRules.ONE_OF_MANY); // ON_COORD_SET
		onCoordSetSlewE = new INDISwitchElement(onCoordSetP, "SLEW", "Slew", Constants.SwitchStatus.OFF); // SLEW
		onCoordSetTrackE = new INDISwitchElement(onCoordSetP, "TRACK", "Track", Constants.SwitchStatus.OFF); // TRACK
		onCoordSetSyncE = new INDISwitchElement(onCoordSetP, "SYNC", "Sync", Constants.SwitchStatus.ON); // SYNC

		
		telescopeMotionNSP = new INDISwitchProperty(this, "TELESCOPE_MOTION_NS", "North/South", "Motion Control",
				Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW, Constants.SwitchRules.AT_MOST_ONE); // TELESCOPE_MOTION_NS
		motionNE = new INDISwitchElement(telescopeMotionNSP, "MOTION_NORTH", "North", Constants.SwitchStatus.OFF); // MOTION_NORTH
		motionSE = new INDISwitchElement(telescopeMotionNSP, "MOTION_SOUTH", "South", Constants.SwitchStatus.OFF); // MOTION_SOUTH

		
		telescopeMotionWEP = new INDISwitchProperty(this, "TELESCOPE_MOTION_WE", "West/East", "Motion Control",
				Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW, Constants.SwitchRules.AT_MOST_ONE); // TELESCOPE_MOTION_WE
		motionWE = new INDISwitchElement(telescopeMotionWEP, "MOTION_WEST", "West", Constants.SwitchStatus.OFF); // MOTION_WEST
		motionEE = new INDISwitchElement(telescopeMotionWEP, "MOTION_EAST", "East", Constants.SwitchStatus.OFF); // MOTION_EAST

		
		abortMotionP = new INDISwitchProperty(this, "TELESCOPE_ABORT_MOTION", "Abort Motion", "Motion Control",
				Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW, Constants.SwitchRules.AT_MOST_ONE); // TELESCOPE_ABORT_MOTION
		abortMotionE = new INDISwitchElement(abortMotionP, "ABORT_MOTION", "Abort", Constants.SwitchStatus.OFF); // ABORT_MOTION

		
		motionRateP = new INDINumberProperty(this, "TELESCOPE_MOTION_RATE", "Motion rate", "Motion Control",
				Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW);
		motionRateE = new INDINumberElement(motionRateP, "MOTION_RATE", "Motion rate (X)", GOTO_SPEED, 0., MAX_SPEED, 0,
				"%7.2f");
		motionSpeed = motionRateE.getValue();
		
		currentRateP = new INDINumberProperty(this, "TELESCOPE_CURRENT_RATE", "Current rate", "Motion Control",
				Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW);		
		currentRARateE = new INDINumberElement(currentRateP, "CURRENT_RA_RATE", "RA rate (X)", 0, -MAX_SPEED, MAX_SPEED, 0,
				"%7.2f");
		currentDERateE = new INDINumberElement(currentRateP, "CURRENT_DE_RATE", "DE rate (X)", 0, -MAX_SPEED, MAX_SPEED, 0,
				"%7.2f");
		trackingRateE = new INDINumberElement(currentRateP, "TRACKING_RATE", "Tracking rate (X)", 1, -MAX_SPEED, MAX_SPEED, 0,
				"%7.2f");
		

		
		telescopeInfoP = INDINumberProperty.createSaveableNumberProperty(this, "TELESCOPE_INFO", "Telescope info", "Info",
				Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW); // TELESCOPE_INFO
		telescopeApertureE = new INDINumberElement(telescopeInfoP, "TELESCOPE_APERTURE", "Telescope aperture (mm)", 203, 1, 10000, 0.,"%7.2f"); // TELESCOPE_APERTURE
		telescopeFocalLengthE  = new INDINumberElement(telescopeInfoP, "TELESCOPE_FOCAL_LENGTH 	", "Telescope focal length (mm)", 1000, 1, 10000, 0.,"%7.2f"); // TELESCOPE_FOCAL_LENGTH 	
		guiderApertureE = new INDINumberElement(telescopeInfoP, "GUIDER_APERTURE", "Guider aperture (mm)", 50, 1, 10000, 0.,"%7.2f"); // GUIDER_APERTURE
		guiderFocalLengthE = new INDINumberElement(telescopeInfoP, "GUIDER_FOCAL_LENGTH", "Guider focal lenth (mm)", 162, 1, 10000, 0.,"%7.2f"); // GUIDER_FOCAL_LENGTH
		addProperty(telescopeInfoP);
		
		
		timedGuideNSP = new INDINumberProperty(this, "TELESCOPE_TIMED_GUIDE_NS", "Guide pulse N/S", "Motion Control",
				Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW); // TELESCOPE_TIMED_GUIDE_NS
		timedGuideNE = new INDINumberElement(timedGuideNSP, "TIMED_GUIDE_N", "North (ms)", 0, 0., 5000, 0,"%4.0f"); // TIMED_GUIDE_N
		timedGuideSE = new INDINumberElement(timedGuideNSP, "TIMED_GUIDE_S", "South (ms)", 0, 0., 5000, 0,"%4.0f"); // TIMED_GUIDE_S
		timedGuideWEP = new INDINumberProperty(this, "TELESCOPE_TIMED_GUIDE_WE", "Guide pulse W/E", "Motion Control",
				Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW); // TELESCOPE_TIMED_GUIDE_WE
		timedGuideWE = new INDINumberElement(timedGuideWEP, "TIMED_GUIDE_W", "West (ms)", 0, 0., 5000, 0,"%4.0f"); // TIMED_GUIDE_W
		timedGuideEE = new INDINumberElement(timedGuideWEP, "TIMED_GUIDE_E", "East (ms)", 0, 0., 5000, 0,"%4.0f"); // TIMED_GUIDE_E
		
		intervalometerSettingsP = new INDINumberProperty(this, "INTERVALOMETER_SETTINGS", "Intervalometer settings", "Auxiliary",
				Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW);
		exposureTimeE = new INDINumberElement(intervalometerSettingsP, "EXPOSURE_TIME", "Exposure time", 30, 0.001, 3600, 0,"%7.2f");
		delayTimeE = new INDINumberElement(intervalometerSettingsP, "DELAY_TIME", "Delay", 1, 0.001, 3600, 0,"%7.2f");
		exposureNumberE = new INDINumberElement(intervalometerSettingsP, "EXPOSURE_NUMBER", "Exposure number", 999, 0, 999999, 1,"%6.0f");
		
		focusSpeedP = new INDINumberProperty(this, "FOCUS_SPEED", "Focus speed", "Auxiliary",
				Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW); // FOCUS_SPEED
		focusSpeedE = new INDINumberElement(focusSpeedP, "FOCUS_SPEED_VALUE", "Speed", 1, -2000, 2000, 1,"%7.2f"); // FOCUS_SPEED_VALUE
		
		focusTimerP = new INDINumberProperty(this, "FOCUS_TIMER", "Focus timer", "Auxiliary",
				Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW); // FOCUS_TIMER
		focusTimerE = new INDINumberElement(focusTimerP, "FOCUS_TIMER_VALUE", "Duration", 1, 0, 1e9, 0,"%5.2f"); // FOCUS_TIMER_VALUE

		focusMotionP = new INDISwitchProperty(this, "FOCUS_MOTION", "Focus motion", "Auxiliary",
				Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW, Constants.SwitchRules.ONE_OF_MANY); // FOCUS_MOTION
		focusInwardE = new INDISwitchElement(focusMotionP, "FOCUS_INWARD", "Inward", Constants.SwitchStatus.ON); // FOCUS_INWARD
		focusOutwardE = new INDISwitchElement(focusMotionP, "FOCUS_OUTWARD", "Outward", Constants.SwitchStatus.OFF); // FOCUS_OUTWARD
		
		focusAbortMotionP = new INDISwitchProperty(this, "FOCUS_ABORT_MOTION", "Focus abort", "Auxiliary",
				Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW, Constants.SwitchRules.ANY_OF_MANY); // FOCUS_ABORT_MOTION
		focusAbortE = new INDISwitchElement(focusAbortMotionP, "ABORT", "Abort", Constants.SwitchStatus.OFF); // ABORT
		
		relFocusPosP = new INDINumberProperty(this, "REL_FOCUS_POSITION", "Relative position", "Auxiliary",
				Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW); // REL_FOCUS_POSITION
		relFocusPosE = new INDINumberElement(relFocusPosP, "FOCUS_RELATIVE_POSITION", "Rel position", 0, -1e9, 1e9, 0,"%7.2f"); // FOCUS_RELATIVE_POSITION
		
		absFocusPosP = new INDINumberProperty(this, "ABS_FOCUS_POSITION", "Absolute position", "Auxiliary",
				Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW); // ABS_FOCUS_POSITION
		absFocusPosE = new INDINumberElement(absFocusPosP, "FOCUS_ABSOLUTE_POSITION", "Abs position", 0, -1e9, 1e9, 0,"%7.2f"); // FOCUS_ABSOLUTE_POSITION
		
		trackRateP = new INDISwitchProperty(this, "TELESCOPE_TRACK_RATE", "Track rate", "Motion Control",
				Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW, Constants.SwitchRules.ONE_OF_MANY); // TELESCOPE_TRACK_RATE
		trackSideralE = new INDISwitchElement(trackRateP, "TRACK_SIDEREAL", "Sidereal", Constants.SwitchStatus.ON); // TRACK_SIDEREAL
		trackSolarE = new INDISwitchElement(trackRateP, "TRACK_SOLAR", "Solar", Constants.SwitchStatus.OFF); // TRACK_SOLAR
		trackLunarE = new INDISwitchElement(trackRateP, "TRACK_LUNAR", "Lunar", Constants.SwitchStatus.OFF); // TRACK_LUNAR
		trackCustomE = new INDISwitchElement(trackRateP, "TRACK_CUSTOM", "Off", Constants.SwitchStatus.OFF); // TRACK_CUSTOM
		trackSpeed = 1;
		
		powerP = new INDINumberProperty(this, "MOTOR_POWER", "Motor power", "Motion Control", Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW);		
		powerRAE = new INDINumberElement(powerP, "POWER_RA", "RA power", 1, -9.99, 9.99, 0,"%7.2f");
		powerDEE = new INDINumberElement(powerP, "POWER_DE", "DE power", 1, -9.99, 9.99, 0,"%7.2f");
		
		powerAuxP = new INDINumberProperty(this, "AUX_POWER", "Auxiliary supplies power", "Auxiliary", Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW);		
		powerAux1E = new INDINumberElement(powerAuxP, "POWER_AUX1", "Aux1 Power", 0, 0, 100, 1,"%3.0f");
		powerAux2E = new INDINumberElement(powerAuxP, "POWER_AUX2", "Aux2 Power", 0, 0, 100, 1,"%3.0f");
		powerAux3E = new INDINumberElement(powerAuxP, "POWER_AUX3", "Aux3 Power", 0, 0, 100, 1,"%3.0f");
		
		
		// --- Remaining initializations ---

		lastStatusMessage = new StatusMessage();
		command = new CmdMessage();

		// --- Setup 1s timer (sidereal time update & link status verification)
		// ---
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				lstE.setValue(getSiderealTime());
				if (isConnected()) {
					if ((new Date()).getTime() - lastStatusMessage.time > CONNECTION_TIMEOUT_ALERT) {
						linkStatusE.setValue(LightStates.ALERT);
					} else {
						linkStatusE.setValue(LightStates.OK);
					}

					try {
						updateProperty(timeLstP);
						updateProperty(linkStatusP);
					} catch (INDIException e) {
						e.printStackTrace();
					}
				}
			}
		};
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(task, 0, 1000);


	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laazotea.indi.driver.INDIDriver#getName()
	 */
	@Override
	public String getName() {
		return "Astroid";
	}

	/**
	 * Called when a new BLOB Vector message has been received from a Client.
	 * 
	 * @param property
	 *            The BLOB Property asked to change.
	 * @param timestamp
	 *            The timestamp of the received message
	 * @param elementsAndValues
	 *            An array of pairs of BLOL Elements and its requested values to
	 *            be parsed.
	 */
	@Override
	public void processNewBLOBValue(INDIBLOBProperty property, Date date, INDIBLOBElementAndValue[] elementsAndValues) {

	}

	/**
	 * Called when a new Number Vector message has been received from a Client.
	 * 
	 * @param property
	 *            The Number Property asked to change.
	 * @param timestamp
	 *            The timestamp of the received message
	 * @param elementsAndValues
	 *            An array of pairs of Number Elements and its requested values
	 *            to be parsed.
	 */
	@Override
	public void processNewNumberValue(INDINumberProperty property, Date date,
			INDINumberElementAndValue[] elementsAndValues) {

		
		
		try{
			// Avoid crash when empty property
			if(elementsAndValues == null){			
				try {
					printMessage("elementsAndValues == null");
					property.setState(PropertyStates.ALERT);
					updateProperty(property, "Empty property: you may have enter an invalid value");
				} catch (INDIException e) {
					e.printStackTrace();
				}
				return;
			}
			if(elementsAndValues.length <= 0){
				try {
					printMessage("elementsAndValues <= 0");
					property.setState(PropertyStates.ALERT);
					updateProperty(property, "Empty property: you may have enter an invalid value");
				} catch (INDIException e) {
					e.printStackTrace();
				}
				return;
			}
			// debug
			//printMessage("Prop: "+property.getName()+"\n" + elementsAndValues.length + " element(s):\n 1) " + elementsAndValues[0].getElement().getNameAndValueAsString());
			
			
			
			
			// --- Geographic coordinates ---
			if (property == geographicCoordP) {
				for (int i = 0; i < elementsAndValues.length; i++) {
					INDINumberElement el = elementsAndValues[i].getElement();
					double val = elementsAndValues[i].getValue();
					if (el == geographicCoordLatE) {
						geographicCoordLatE.setValue(val);
					}
					if (el == geographicCoordLongE) {
						geographicCoordLongE.setValue(val);
					}
					if (el == geographicCoordElevE) {
						geographicCoordElevE.setValue(val);
					}
					geographicCoordP.setState(PropertyStates.OK);
	
				}
				try {
					updateProperty(geographicCoordP);
				} catch (INDIException e) {
					e.printStackTrace();
				}
			}
	
			// --- Equatorial coordinates ---
			if (property == eqCoordP) {
				double newRA = 0;
				double newDE = 0;
				eqCoordP.setState(PropertyStates.BUSY);
				for (int i = 0; i < elementsAndValues.length; i++) {
					INDINumberElement el = elementsAndValues[i].getElement();
					double val = elementsAndValues[i].getValue();
					if (el == eqCoordDEE) {
						newDE = mod360(val+180)-180;
					} else {
						newRA = mod24(val);
					}
				}
	
				if (onCoordSetSyncE.getValue() == SwitchStatus.ON) {
					syncCoordinates(newRA, newDE);
				} else {
					gotoCoordinates(newRA, newDE);
				}
	
			}
	
			// --- Motion rate ---
			if (property == motionRateP) {
				double val = elementsAndValues[0].getValue();
				motionRateE.setValue(val);
				motionSpeed = val;
	
				if (motionNE.getValue() == SwitchStatus.ON) {
					slewDESpeed = motionSpeed;
					updateSpeed();
					telescopeMotionNSP.setState(PropertyStates.OK);
				} else if (motionSE.getValue() == SwitchStatus.ON) {
					slewDESpeed = -motionSpeed;
					updateSpeed();
					telescopeMotionNSP.setState(PropertyStates.OK);
				}
				if (motionWE.getValue() == SwitchStatus.ON) {
					slewRASpeed = -motionSpeed;
					updateSpeed();
					telescopeMotionWEP.setState(PropertyStates.OK);
				} else if (motionEE.getValue() == SwitchStatus.ON) {
					slewRASpeed = motionSpeed;
					updateSpeed();
					telescopeMotionWEP.setState(PropertyStates.OK);
				}
				motionRateP.setState(PropertyStates.OK);
	
				try {
					updateProperty(motionRateP);
				} catch (INDIException e) {
					e.printStackTrace();
				}

			}
			
			// --- Current rate ---
			if (property == currentRateP) {
				currentRateP.setState(PropertyStates.BUSY);
				for (int i = 0; i < elementsAndValues.length; i++) {
					INDINumberElement el = elementsAndValues[i].getElement();
					double val = elementsAndValues[i].getValue();
					if (el == currentRARateE) {
						slewRASpeed = val;
					} else if (el == currentDERateE) {
						slewDESpeed = val;
					} else if (el == trackingRateE) {
						trackSpeed = val;
					}
					
				}
				currentRateP.setState(PropertyStates.OK);
				updateSpeed();
				// "updateProperty" already performed in updateSpeed()

			}
			
			// --- Power ---
			if (property == powerP) {
				powerP.setState(PropertyStates.BUSY);
				for (int i = 0; i < elementsAndValues.length; i++) {
					INDINumberElement el = elementsAndValues[i].getElement();
					double val = elementsAndValues[i].getValue();
					if (el == powerRAE) {
						powerHA = val;
					} else if (el == powerDEE) {
						powerDE = val;
					}
					
				}
				powerP.setState(PropertyStates.OK);
				updateSpeed();
				// "updateProperty" already performed in updateSpeed()

			}
			
			// --- Telescope info ---
			if (property == telescopeInfoP) {
				for (int i = 0; i < elementsAndValues.length; i++) {
					INDINumberElement el = elementsAndValues[i].getElement();
					double val = elementsAndValues[i].getValue();
					el.setValue(val);
					telescopeInfoP.setState(PropertyStates.OK);
				}
				try {
					updateProperty(telescopeInfoP);
				} catch (INDIException e) {
					e.printStackTrace();
				}
			}
			
			
			// --- Intervalometer ---
			if (property == intervalometerSettingsP) {
				for (int i = 0; i < elementsAndValues.length; i++) {
					
					
					INDINumberElement el = elementsAndValues[i].getElement();
					double val = elementsAndValues[i].getValue();
					el.setValue(val);
					
					if(el == exposureNumberE){
						startIntervalometer();
					}
				}
				try {
					updateProperty(intervalometerSettingsP);
				} catch (INDIException e) {
					e.printStackTrace();
				}
			}
			
			// --- Guide ---
			if (property == timedGuideNSP) {
				double val = elementsAndValues[0].getValue();
				INDINumberElement el = elementsAndValues[0].getElement();
				if(val<=0.){
					if(elementsAndValues.length>=2){
						val = elementsAndValues[1].getValue();
						el = elementsAndValues[1].getElement();
					}else{
						timedGuideNSP.setState(PropertyStates.ALERT);
						try {
							updateProperty(timedGuideNSP,"0ms pulse error");
						} catch (INDIException e) {
							e.printStackTrace();
						}
						return;
					}
				}
				if(val<=0.){
					timedGuideNSP.setState(PropertyStates.ALERT);
					try {
						updateProperty(timedGuideNSP,"0ms pulse error");
					} catch (INDIException e) {
						e.printStackTrace();
					}
					return;
				}
				
				if(el==timedGuideNE){
					timedGuideNSP.setState(PropertyStates.BUSY);
					motionNE.setValue(SwitchStatus.ON);
					try {
						updateProperty(timedGuideNSP);
						updateProperty(telescopeMotionNSP);
					} catch (INDIException e) {
						e.printStackTrace();
					}
					
					slewDESpeed = motionSpeed;
					updateSpeed();
					
					TimerTask task = new TimerTask() {					
						@Override
						public void run() {
							timedGuideNSP.setState(PropertyStates.OK);
							motionNE.setValue(SwitchStatus.OFF);
							try {
								updateProperty(timedGuideNSP);
								updateProperty(telescopeMotionNSP);
							} catch (INDIException e) {
								e.printStackTrace();
							}
							slewDESpeed = 0;
							updateSpeed();
						}
					};
					Timer timer = new Timer();
					timer.schedule(task, (long)val);
				}			
				if(el==timedGuideSE){
					timedGuideNSP.setState(PropertyStates.BUSY);				
					motionSE.setValue(SwitchStatus.ON);
					try {
						updateProperty(timedGuideNSP);
						updateProperty(telescopeMotionNSP);
					} catch (INDIException e) {
						e.printStackTrace();
					}
					slewDESpeed = -motionSpeed;
					updateSpeed();
					TimerTask task = new TimerTask() {					
						@Override
						public void run() {
							timedGuideNSP.setState(PropertyStates.OK);	
							motionSE.setValue(SwitchStatus.OFF);
							try {
								updateProperty(timedGuideNSP);
								updateProperty(telescopeMotionNSP);
							} catch (INDIException e) {
								e.printStackTrace();
							}
							slewDESpeed = 0;
							updateSpeed();
						}
					};
					Timer timer = new Timer();
					timer.schedule(task, (long)val);
				}			
			}
			if (property == timedGuideWEP) {
				double val = elementsAndValues[0].getValue();
				INDINumberElement el = elementsAndValues[0].getElement();
				if(val<=0.){
					if(elementsAndValues.length>=2){
						val = elementsAndValues[1].getValue();
						el = elementsAndValues[1].getElement();
					}else{
						timedGuideWEP.setState(PropertyStates.ALERT);
						try {
							updateProperty(timedGuideWEP,"0ms pulse error");
						} catch (INDIException e) {
							e.printStackTrace();
						}
						return;
					}
				}
				if(val<=0.){
					timedGuideWEP.setState(PropertyStates.ALERT);
					try {
						updateProperty(timedGuideWEP,"0ms pulse error");
					} catch (INDIException e) {
						e.printStackTrace();
					}
					return;
				}
				
				if(el==timedGuideWE){
					timedGuideWEP.setState(PropertyStates.BUSY);
					motionWE.setValue(SwitchStatus.ON);
					try {
						updateProperty(timedGuideWEP);
						updateProperty(telescopeMotionWEP);
					} catch (INDIException e) {
						e.printStackTrace();
					}
					slewRASpeed = -motionSpeed;
					updateSpeed();
					TimerTask task = new TimerTask() {					
						@Override
						public void run() {
							timedGuideWEP.setState(PropertyStates.OK);
							motionWE.setValue(SwitchStatus.OFF);
							try {
								updateProperty(timedGuideWEP);
								updateProperty(telescopeMotionWEP);
							} catch (INDIException e) {
								e.printStackTrace();
							}
							slewRASpeed = 0;
							updateSpeed();
						}
					};
					Timer timer = new Timer();
					timer.schedule(task, (long)val);
				}			
				if(el==timedGuideEE){
					timedGuideWEP.setState(PropertyStates.BUSY);				
					motionEE.setValue(SwitchStatus.ON);
					try {
						updateProperty(timedGuideWEP);
						updateProperty(telescopeMotionWEP);
					} catch (INDIException e) {
						e.printStackTrace();
					}
					slewRASpeed = motionSpeed;
					updateSpeed();
					TimerTask task = new TimerTask() {					
						@Override
						public void run() {
							timedGuideWEP.setState(PropertyStates.OK);	
							motionEE.setValue(SwitchStatus.OFF);
							try {
								updateProperty(timedGuideWEP);
								updateProperty(telescopeMotionWEP);
							} catch (INDIException e) {
								e.printStackTrace();
							}
							slewRASpeed = 0;
							updateSpeed();
						}
					};
					Timer timer = new Timer();
					timer.schedule(task, (long)val);
				}			
			}
			 // --- Focus ---
			if (property == focusSpeedP) {
				for (int i = 0; i < elementsAndValues.length; i++) {
					INDINumberElement el = elementsAndValues[i].getElement();
					double val = elementsAndValues[i].getValue();
					if (el == focusSpeedE) {
						el.setValue(val);
						focusSpeedP.setState(PropertyStates.OK);
					}
				}
				try {
					updateProperty(focusSpeedP);
				} catch (INDIException e) {
					e.printStackTrace();
				}
			}
			
			if (property == focusTimerP) {
				for (int i = 0; i < elementsAndValues.length; i++) {
					INDINumberElement el = elementsAndValues[i].getElement();
					double val = elementsAndValues[i].getValue();
					if (el == focusTimerE) {
						el.setValue(val);
						moveFocus(val, (int) (focusSpeedE.getValue() * (focusOutwardE.getValue()==SwitchStatus.ON ? 1 : -1)),focusTimerP);
					}
				}
				try {
					updateProperty(focusTimerP);
				} catch (INDIException e) {
					e.printStackTrace();
				}
			}
			
			if (property == relFocusPosP) {
				for (int i = 0; i < elementsAndValues.length; i++) {
					INDINumberElement el = elementsAndValues[i].getElement();
					double val = elementsAndValues[i].getValue();
					if (el == relFocusPosE) {
						el.setValue(val);
						
						double duration = val / focusSpeedE.getValue();
						focusTimerE.setValue(Math.abs(duration));
						moveFocus(Math.abs(duration), (int) (focusSpeedE.getValue() * (duration > 0 ? 1 : -1)),relFocusPosP);
					}
				}
				try {
					updateProperty(relFocusPosP);
					updateProperty(focusTimerP);
				} catch (INDIException e) {
					e.printStackTrace();
				}
			}
			
			if (property == absFocusPosP) {
				for (int i = 0; i < elementsAndValues.length; i++) {
					INDINumberElement el = elementsAndValues[i].getElement();
					double val = elementsAndValues[i].getValue();
					if (el == absFocusPosE) {					
						double duration = (val-el.getValue()) / focusSpeedE.getValue();
						focusTimerE.setValue(Math.abs(duration));
						moveFocus(Math.abs(duration), (int) (focusSpeedE.getValue() * (duration > 0 ? 1 : -1)),absFocusPosP);
					}
				}
				try {
					updateProperty(absFocusPosP);
					updateProperty(focusTimerP);
				} catch (INDIException e) {
					e.printStackTrace();
				}
			}
			
			// --- Aux power supply ---
			if (property == powerAuxP) {
				for (int i = 0; i < elementsAndValues.length; i++) {
					INDINumberElement el = elementsAndValues[i].getElement();
					double val = elementsAndValues[i].getValue();
					el.setValue(val);
				}
				command.setPowerAUX1((int) Math.round(powerAux1E.getValue()*256./100.));
				command.setPowerAUX2((int) Math.round(powerAux2E.getValue()*256./100.));
				command.setPowerAUX3((int) Math.round(powerAux3E.getValue()*256./100.));
				sendCommand();
				powerAuxP.setState(PropertyStates.OK);
				try {
					updateProperty(powerAuxP);
				} catch (INDIException e) {
					e.printStackTrace();
				}
			}
		
		}catch(IllegalArgumentException e){
			printMessage(e.getMessage());
			property.setState(PropertyStates.ALERT);
			try {
				updateProperty(property,e.getMessage());
			} catch (INDIException e1) {
				e1.printStackTrace();
			}
		}

		
		
	}

	/**
	 * Called when a new Switch Vector message has been received from a Client.
	 * 
	 * @param property
	 *            The Switch Property asked to change.
	 * @param timestamp
	 *            The timestamp of the received message
	 * @param elementsAndValues
	 *            An array of pairs of Switch Elements and its requested values
	 *            to be parsed.
	 */
	@Override
	public void processNewSwitchValue(INDISwitchProperty property, Date date,
			INDISwitchElementAndValue[] elementsAndValues) {
		
		try{
			// Avoid crash when empty property
			if(elementsAndValues == null){			
				try {
					printMessage("elementsAndValues == null");
					property.setState(PropertyStates.ALERT);
					updateProperty(property, "Empty property: you may have enter an invalid value");
				} catch (INDIException e) {
					e.printStackTrace();
				}
				return;
			}
			if(elementsAndValues.length <= 0){
				try {
					printMessage("elementsAndValues <= 0");
					property.setState(PropertyStates.ALERT);
					updateProperty(property, "Empty property: you may have enter an invalid value");
				} catch (INDIException e) {
					e.printStackTrace();
				}
				return;
			}
			// debug
			//printMessage("Prop: "+property.getName()+"\n" + elementsAndValues.length + " element(s):\n 1) " + elementsAndValues[0].getElement().getNameAndValueAsString());
			
	
			if (property == onCoordSetP) {
				onCoordSetP.setState(PropertyStates.IDLE);
				for (int i = 0; i < elementsAndValues.length; i++) {
					INDISwitchElement el = elementsAndValues[i].getElement();
					SwitchStatus val = elementsAndValues[i].getValue();
					if (val == SwitchStatus.ON) {
						onCoordSetSlewE.setValue(SwitchStatus.OFF);
						onCoordSetTrackE.setValue(SwitchStatus.OFF);
						onCoordSetSyncE.setValue(SwitchStatus.OFF);
						el.setValue(SwitchStatus.ON);
						onCoordSetP.setState(PropertyStates.OK);
	
					}
	
				}
				try {
					updateProperty(onCoordSetP);
				} catch (INDIException e) {
					e.printStackTrace();
				}
			}
	
			if (property == sideP) {
				sideP.setState(PropertyStates.IDLE);
				boolean hasChanged = false;
				for (int i = 0; i < elementsAndValues.length; i++) {
					INDISwitchElement el = elementsAndValues[i].getElement();
					SwitchStatus val = elementsAndValues[i].getValue();
					if (val != el.getValue()) {
						hasChanged = true;
					}
				}
				if (hasChanged) {
					if (sideWestE.getValue() == SwitchStatus.ON) {
						sideWestE.setValue(SwitchStatus.OFF);
						sideEastE.setValue(SwitchStatus.ON);
					} else {
						sideWestE.setValue(SwitchStatus.ON);
						sideEastE.setValue(SwitchStatus.OFF);
					}
					sideP.setState(PropertyStates.OK);
	
					syncCoordHA = syncCoordHA + 12;
					syncCoordDE = 180 - syncCoordDE;
				}
				try {
					updateProperty(sideP);
				} catch (INDIException e) {
					e.printStackTrace();
				}
			}
	
			if (property == telescopeMotionNSP) {
	
				if (elementsAndValues.length != 2) {
					printMessage("elementsAndValues.length!=2");
					return;
				}
	
				if (motionNE.getValue() == SwitchStatus.ON) {
					if ((elementsAndValues[0].getElement() == motionSE && elementsAndValues[0].getValue() == SwitchStatus.ON)
							|| (elementsAndValues[1].getElement() == motionSE && elementsAndValues[1].getValue() == SwitchStatus.ON)) {
						motionNE.setValue(SwitchStatus.OFF);
						motionSE.setValue(SwitchStatus.ON);
					} else if ((elementsAndValues[0].getElement() == motionNE && elementsAndValues[0].getValue() == SwitchStatus.OFF)
							|| (elementsAndValues[1].getElement() == motionNE && elementsAndValues[1].getValue() == SwitchStatus.OFF)) {
						motionNE.setValue(SwitchStatus.OFF);
					}
				} else if (motionSE.getValue() == SwitchStatus.ON) {
					if ((elementsAndValues[0].getElement() == motionNE && elementsAndValues[0].getValue() == SwitchStatus.ON)
							|| (elementsAndValues[1].getElement() == motionNE && elementsAndValues[1].getValue() == SwitchStatus.ON)) {
						motionSE.setValue(SwitchStatus.OFF);
						motionNE.setValue(SwitchStatus.ON);
					} else if ((elementsAndValues[0].getElement() == motionSE && elementsAndValues[0].getValue() == SwitchStatus.OFF)
							|| (elementsAndValues[1].getElement() == motionSE && elementsAndValues[1].getValue() == SwitchStatus.OFF)) {
						motionSE.setValue(SwitchStatus.OFF);
					}
				} else {
					if (elementsAndValues[0].getValue() == SwitchStatus.ON) {
						elementsAndValues[0].getElement().setValue(SwitchStatus.ON);
					} else if (elementsAndValues[1].getValue() == SwitchStatus.ON) {
						elementsAndValues[1].getElement().setValue(SwitchStatus.ON);
					}
				}
	
				if (motionNE.getValue() == SwitchStatus.ON) {
					slewDESpeed = motionSpeed;
					updateSpeed();
					telescopeMotionNSP.setState(PropertyStates.OK);
				} else if (motionSE.getValue() == SwitchStatus.ON) {
					slewDESpeed = -motionSpeed;
					updateSpeed();
					telescopeMotionNSP.setState(PropertyStates.OK);
				} else {
					slewDESpeed = 0;
					updateSpeed();
					telescopeMotionNSP.setState(PropertyStates.IDLE);
				}
				
	
				try {
					updateProperty(telescopeMotionNSP);
				} catch (INDIException e) {
					e.printStackTrace();
				}
			}
	
			if (property == telescopeMotionWEP) {
	
				if (elementsAndValues.length != 2) {
					printMessage("elementsAndValues.length!=2");
					return;
				}
	
				if (motionWE.getValue() == SwitchStatus.ON) {
					if ((elementsAndValues[0].getElement() == motionEE && elementsAndValues[0].getValue() == SwitchStatus.ON)
							|| (elementsAndValues[1].getElement() == motionEE && elementsAndValues[1].getValue() == SwitchStatus.ON)) {
						motionWE.setValue(SwitchStatus.OFF);
						motionEE.setValue(SwitchStatus.ON);
					} else if ((elementsAndValues[0].getElement() == motionWE && elementsAndValues[0].getValue() == SwitchStatus.OFF)
							|| (elementsAndValues[1].getElement() == motionWE && elementsAndValues[1].getValue() == SwitchStatus.OFF)) {
						motionWE.setValue(SwitchStatus.OFF);
					}
				} else if (motionEE.getValue() == SwitchStatus.ON) {
					if ((elementsAndValues[0].getElement() == motionWE && elementsAndValues[0].getValue() == SwitchStatus.ON)
							|| (elementsAndValues[1].getElement() == motionWE && elementsAndValues[1].getValue() == SwitchStatus.ON)) {
						motionEE.setValue(SwitchStatus.OFF);
						motionWE.setValue(SwitchStatus.ON);
					} else if ((elementsAndValues[0].getElement() == motionEE && elementsAndValues[0].getValue() == SwitchStatus.OFF)
							|| (elementsAndValues[1].getElement() == motionEE && elementsAndValues[1].getValue() == SwitchStatus.OFF)) {
						motionEE.setValue(SwitchStatus.OFF);
					}
				} else {
					if (elementsAndValues[0].getValue() == SwitchStatus.ON) {
						elementsAndValues[0].getElement().setValue(SwitchStatus.ON);
					} else if (elementsAndValues[1].getValue() == SwitchStatus.ON) {
						elementsAndValues[1].getElement().setValue(SwitchStatus.ON);
					}
				}
	
				if (motionWE.getValue() == SwitchStatus.ON) {
					slewRASpeed = -motionSpeed;
					telescopeMotionWEP.setState(PropertyStates.OK);
				} else if (motionEE.getValue() == SwitchStatus.ON) {
					slewRASpeed = motionSpeed;
					telescopeMotionWEP.setState(PropertyStates.OK);
				} else {
					slewRASpeed = 0;
					telescopeMotionWEP.setState(PropertyStates.IDLE);
				}
				updateSpeed();
	
				try {
					updateProperty(telescopeMotionWEP);
				} catch (INDIException e) {
					e.printStackTrace();
				}
			}
	
			
			
			if (property == trackRateP) {
				trackRateP.setState(PropertyStates.IDLE);
				for (int i = 0; i < elementsAndValues.length; i++) {
					INDISwitchElement el = elementsAndValues[i].getElement();
					SwitchStatus val = elementsAndValues[i].getValue();
					if (val == SwitchStatus.ON) {
						trackSideralE.setValue(SwitchStatus.OFF);
						trackSolarE.setValue(SwitchStatus.OFF);
						trackLunarE.setValue(SwitchStatus.OFF);
						trackCustomE.setValue(SwitchStatus.OFF);
						el.setValue(SwitchStatus.ON);
						trackRateP.setState(PropertyStates.OK);		
					}		
				}

				
				if(trackSideralE.getValue()==SwitchStatus.ON){
					trackSpeed = 1;
				}else if(trackSolarE.getValue()==SwitchStatus.ON){
					trackSpeed = 86164.0/86400.0;
				}else if(trackLunarE.getValue()==SwitchStatus.ON){
					trackSpeed = 27.0/28.0;
				}else{
					trackSpeed = 0;
				}
				updateSpeed();
				
				try {
					updateProperty(trackRateP);
				} catch (INDIException e) {
					e.printStackTrace();
				}
			}
			
			
			if (property == abortMotionP) {
				if (elementsAndValues.length > 0) {
					if (elementsAndValues[0].getValue() == SwitchStatus.ON) {
						abortMotionP.setState(PropertyStates.OK);
						gotoActive = false;
						slewDESpeed = 0;
						slewRASpeed = 0;
						updateSpeed();
	
						motionEE.setValue(SwitchStatus.OFF);
						motionWE.setValue(SwitchStatus.OFF);
						motionNE.setValue(SwitchStatus.OFF);
						motionSE.setValue(SwitchStatus.OFF);
					}
					try {
						updateProperty(abortMotionP);
						updateProperty(telescopeMotionNSP);
						updateProperty(telescopeMotionWEP);
					} catch (INDIException e) {
						e.printStackTrace();
					}
				}
			}
			
			if (property == focusMotionP) {
				focusMotionP.setState(PropertyStates.IDLE);
				for (int i = 0; i < elementsAndValues.length; i++) {
					INDISwitchElement el = elementsAndValues[i].getElement();
					SwitchStatus val = elementsAndValues[i].getValue();
					if (val == SwitchStatus.ON) {
						focusInwardE.setValue(SwitchStatus.OFF);
						focusOutwardE.setValue(SwitchStatus.OFF);
						el.setValue(SwitchStatus.ON);
						focusMotionP.setState(PropertyStates.OK);
					}
	
				}
				try {
					updateProperty(focusMotionP);
				} catch (INDIException e) {
					e.printStackTrace();
				}
			}
			
			if (property == focusAbortMotionP) {
				focusMotionP.setState(PropertyStates.IDLE);
				for (int i = 0; i < elementsAndValues.length; i++) {
					INDISwitchElement el = elementsAndValues[i].getElement();
					SwitchStatus val = elementsAndValues[i].getValue();
					if (val == SwitchStatus.ON) {
						command.setSpeedFOCUS(0.f);
						command.setPowerFOCUS(0.f);
						sendCommand();
						focusAbortMotionP.setState(PropertyStates.OK);
					}

				}
				try {
					updateProperty(focusMotionP);
					updateProperty(focusAbortMotionP);
				} catch (INDIException e) {
					e.printStackTrace();
				}
			}
			
		}catch(IllegalArgumentException e){
			printMessage(e.getMessage());
			property.setState(PropertyStates.ALERT);
			try {
				updateProperty(property,e.getMessage());
			} catch (INDIException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Called when a new Text Vector message has been received from a Client.
	 * 
	 * @param property
	 *            The Text Property asked to change.
	 * @param timestamp
	 *            The timestamp of the received message
	 * @param elementsAndValues
	 *            An array of pairs of Text Elements and its requested values to
	 *            be parsed.
	 */
	@Override
	public void processNewTextValue(INDITextProperty property, Date date, INDITextElementAndValue[] elementsAndValues) {
		
		try{
			// Avoid crash when empty property
			if(elementsAndValues == null){			
				try {
					printMessage("elementsAndValues == null");
					property.setState(PropertyStates.ALERT);
					updateProperty(property, "Empty property: you may have enter an invalid value");
				} catch (INDIException e) {
					e.printStackTrace();
				}
				return;
			}
			if(elementsAndValues.length <= 0){
				try {
					printMessage("elementsAndValues <= 0");
					property.setState(PropertyStates.ALERT);
					updateProperty(property, "Empty property: you may have enter an invalid value");
				} catch (INDIException e) {
					e.printStackTrace();
				}
				return;
			}
		}catch(IllegalArgumentException e){
			printMessage(e.getMessage());
			property.setState(PropertyStates.ALERT);
			try {
				updateProperty(property,e.getMessage());
			} catch (INDIException e1) {
				e1.printStackTrace();
			}
		}
		
		// No properties
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * laazotea.indi.driver.INDIConnectionHandler#driverConnect(java.util.Date)
	 */
	@Override
	public abstract void driverConnect(Date timestamp) throws INDIException;
	

	/**
	 * Called when the device is just connected
	 */
	protected void onConnected(){
		printMessage("Driver connected");
		addProperty(linkStatusP);
		addProperty(eqCoordP, "Driver connected");
		addProperty(sideP);
		addProperty(onCoordSetP);
		addProperty(telescopeMotionNSP);
		addProperty(telescopeMotionWEP);
		addProperty(abortMotionP);
		addProperty(motionRateP);
		addProperty(currentRateP);
		addProperty(timedGuideNSP);
		addProperty(timedGuideWEP);
		addProperty(trackRateP);
		addProperty(powerP);
		addProperty(focusMotionP);
		addProperty(focusAbortMotionP);
		addProperty(focusSpeedP);
		addProperty(focusTimerP);
		addProperty(relFocusPosP);
		addProperty(absFocusPosP);
		addProperty(intervalometerSettingsP);
		addProperty(powerAuxP);
		initIntervalometer();
		
		syncCoordHA = getSiderealTime();
		syncStepHA = 0;
		syncCoordDE = 0;
		syncStepDE = 0;
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * laazotea.indi.driver.INDIConnectionHandler#driverDisconnect(java.util
	 * .Date)
	 */
	@Override
	public abstract void driverDisconnect(Date timestamp) throws INDIException;
	
	/**
	 * Called when the device is just disconnected
	 */
	protected void onDisconnected(){
		printMessage("Driver disconnect");
		removeProperty(intervalometerSettingsP);
		removeProperty(linkStatusP);
		removeProperty(eqCoordP);
		removeProperty(sideP);
		removeProperty(onCoordSetP);
		removeProperty(telescopeMotionNSP);
		removeProperty(telescopeMotionWEP);
		removeProperty(abortMotionP);
		removeProperty(motionRateP);
		removeProperty(currentRateP);
		removeProperty(timedGuideNSP);
		removeProperty(timedGuideWEP);
		removeProperty(focusMotionP);
		removeProperty(focusSpeedP);
		removeProperty(focusTimerP);
		removeProperty(relFocusPosP);
		removeProperty(absFocusPosP);
		removeProperty(trackRateP);
		removeProperty(powerP);
		removeProperty(focusMotionP);
		removeProperty(focusAbortMotionP);
		removeProperty(focusSpeedP);
		removeProperty(focusTimerP);
		removeProperty(relFocusPosP);
		removeProperty(absFocusPosP);
		removeProperty(intervalometerSettingsP);
		removeProperty(powerAuxP);
	}

	/**
	 * Computes the sidereal time
	 * 
	 * @return the sidereal time in hours
	 */
	public double getSiderealTime() {
		long now = (new Date()).getTime();
		double j2000 = 10957.5 * 3600 * 24 * 1e3;
		double D = (now - j2000) / 86400.0e3;
		double GMST = 18.697374558 + 24.06570982441908 * D;
		double lon = geographicCoordLongE.getValue();
		lon = lon / 360 * 24;
		double LST = GMST + lon;
		LST = LST % 24;
		if(LST<0){LST+=24;} // error with years before 2000
		return LST;
	}

	/**
	 * Send the current command message to the device
	 */
	protected abstract void sendCommand();

	/**
	 * @return the declination in deg (between -90deg and 270deg)
	 */
	protected double getDE(){
		return mod360((lastStatusMessage.getDE() - syncStepDE) / STEP_BY_TURN * 360 * (sideEastE.getValue() == SwitchStatus.ON ? 1 : -1) + syncCoordDE +90)-90;
	}
	
	/**
	 * @return the declination in deg (between -90deg and 90deg)
	 */
	protected double getDE2(){
		double DE = getDE();
		if (DE > 90) {
			DE = 180 - DE;
		}
		return DE;
	}
	
	/**
	 * @return the right ascension in hours (positive to the east, 12h bias if the dec is between 90 and 270)
	 */
	protected double getRA(){
		return mod24(getSiderealTime() - getHA());
	}
	
	/**
	 * @return the right ascension in hours (positive to the east))
	 */
	protected double getRA2(){
		double RA = getRA();
		double DE = getDE();		
		if (DE > 90) {
			RA = mod24(RA + 12);
		}
		return RA;
	}
	
	/**
	 * @return the hour angle in hours
	 */
	protected double getHA(){
		return (lastStatusMessage.getHA()- syncStepHA) / STEP_BY_TURN * 24 + syncCoordHA;
	}
	
	/**
	 * update the position properties from the status message
	 */
	protected void updateStatus() {


		eqCoordRAE.setValue(getRA2());
		//System.out.println("getRA2()=" + getRA2());
		eqCoordDEE.setValue(getDE2());
		//System.out.println("getDE2()=" + getDE2());
		absFocusPosE.setValue(lastStatusMessage.getFOCUS());

		
		gotoUpdate();

		try {
			updateProperty(eqCoordP);
			updateProperty(absFocusPosP);
		} catch (INDIException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sync with the specified coordinates
	 * 
	 * @param RA
	 * @param DE
	 */
	private void syncCoordinates(double RA, double DE) {
		syncCoordDE = DE;
		syncStepDE = lastStatusMessage.getDE();
		syncCoordHA = getSiderealTime() - RA;
		syncStepHA = lastStatusMessage.getHA();
		eqCoordP.setState(PropertyStates.OK);
		updateStatus();
	}

	/**
	 * Go to the specified coordinates
	 * 
	 * @param RA
	 * @param DE
	 */
	private void gotoCoordinates(double RA, double DE) {
		gotoTargetRA = RA;
		gotoTargetDE = DE;
		gotoActive = true;
		lastGotoUpdate = new Date();
	}

	private void gotoUpdate() {
		if (gotoActive) {

			double dt = ((new Date()).getTime()-lastGotoUpdate.getTime())/1000.;
			lastGotoUpdate = new Date();

			
			// DE
			double distanceDE = gotoTargetDE - getDE();
			if (Math.abs(distanceDE) < GOTO_STOP_DISTANCE){
				slewDESpeed = 0;
			}else if(GOTO_ACC_T>0){
				if (2*Math.abs(distanceDE)*(GOTO_SPEED*15/3600/GOTO_ACC_T)>(slewDESpeed*360/86400)*(slewDESpeed*360/86400)){
					slewDESpeed += GOTO_SPEED/GOTO_ACC_T *dt* (distanceDE > 0 ? 1 : -1);				
				}else{
					slewDESpeed = 86400/360*Math.sqrt(2*(Math.abs(distanceDE))*(GOTO_SPEED*15/3600/GOTO_ACC_T))*(distanceDE > 0 ? 1 : -1);
				}
				slewDESpeed = Math.min(Math.max(slewDESpeed,-GOTO_SPEED),GOTO_SPEED);
			}else{
				slewDESpeed = GOTO_SPEED*(distanceDE > 0 ? 1 : -1);
			}
			
			// RA
			double distanceRA = (mod24(gotoTargetRA - getRA() + 12) - 12)*15; // between -180 and 180
			if (Math.abs(distanceRA) < GOTO_STOP_DISTANCE){
				slewRASpeed = 0;
			}else if(GOTO_ACC_T>0){
				if (2*Math.abs(distanceRA)*(GOTO_SPEED*15/3600/GOTO_ACC_T)>(slewRASpeed*360/86400)*(slewRASpeed*360/86400)){
					slewRASpeed += GOTO_SPEED/GOTO_ACC_T *dt* (distanceRA > 0 ? 1 : -1);				
				}else{
					slewRASpeed = 86400/360*Math.sqrt(2*(Math.abs(distanceRA))*(GOTO_SPEED*15/3600/GOTO_ACC_T))*(distanceRA > 0 ? 1 : -1);
				}
				slewRASpeed = Math.min(Math.max(slewRASpeed,-GOTO_SPEED),GOTO_SPEED);
			}else{
				slewRASpeed = GOTO_SPEED*(distanceRA > 0 ? 1 : -1);
			}
			
//			printMessage("distanceDE="+distanceDE);
//			printMessage("slewDESpeed="+slewDESpeed);
//			printMessage("distanceRA="+distanceRA);
//			printMessage("slewRASpeed="+slewRASpeed);
//			printMessage("GOTO_STOP_DISTANCE="+GOTO_STOP_DISTANCE);

			
			updateSpeed();

			if (slewRASpeed == 0 && slewDESpeed == 0) {
				gotoActive = false;
				eqCoordP.setState(PropertyStates.OK);
				try {
					updateProperty(eqCoordP);
				} catch (INDIException e) {
					e.printStackTrace();
				}
			}



		}
	}

	/**
	 * returns the modulus 24 in [0, 24[
	 * 
	 * @param x
	 * @return x modulus 24 in [0, 24[
	 */
	private double mod24(double x) {
		double res = x % 24;
		if (res >= 0) {
			return res;
		} else {
			return res + 24;
		}
	}

	/**
	 * returns the modulus 360 in [0, 360[
	 * 
	 * @param x
	 * @return x modulus 360 in [0, 360[
	 */
	private double mod360(double x) {
		double res = x % 360;
		res += (res < 0 ? 360 : 0);
		return res;
	}
	
	/**
	 * Execute the focus servo move
	 * @param time duration of the move in s
	 * @param speed negative or positive speed
	 */
	private void moveFocus(final double duration, final float speed, final INDIProperty prop){
		command.setSpeedFOCUS(speed);
		command.setPowerFOCUS(1.f);
		sendCommand();
		TimerTask task = new TimerTask(){
			@Override
			public void run() {
				command.setSpeedFOCUS(0.f);
				command.setPowerFOCUS(0.f);
				sendCommand();
				prop.setState(PropertyStates.OK);
				try {
					updateProperty(prop);
				} catch (INDIException e) {
					e.printStackTrace();
				}
			}			
		};
		Timer timer = new Timer();
		timer.schedule(task, (long) (duration*1000));
		
		prop.setState(PropertyStates.BUSY);
		try {
			updateProperty(prop);
		} catch (INDIException e) {
			e.printStackTrace();
		}
		
	}
	
	


	

	
	private void initIntervalometer(){
		intervalometer = new Timer();
	}
	
	private void startIntervalometer(){
		intervalometer.cancel();
		intervalometer = new Timer();
		command.disableBulb();
		sendCommand();

		double n = exposureNumberE.getValue();
		if(n>0){
			intervalometerSettingsP.setState(PropertyStates.BUSY);
			try {
				updateProperty(intervalometerSettingsP);
			} catch (INDIException e) {
				e.printStackTrace();
			}
			currentTask = new CompleteTask();
			intervalometer.schedule(new ExposeTask(), 100);
		}
	}
	
	
    

	
	/**
	 * On German equatorial mounts, a given celestial position can be pointed in
	 * two ways. The counter-weight is generally down and the telescope up. -
	 * East means that the telescope is pointing toward the east when the
	 * counter-weight is down (and west when it is up). - West means that the
	 * telescope is pointing toward the west when the counter-weight is down
	 * (and east when it is up). When INVERT_DE = false and side = East, the
	 * declination increase when the DE speed is positive
	 */
	private void updateSpeed(){
		double speedDE, speedHA;
		
		speedDE = slewDESpeed * (sideEastE.getValue() == SwitchStatus.ON ? 1 : -1);
		speedHA = trackSpeed-slewRASpeed; // because HA = LST-RA
		
		command.setSpeedDE((float)speedDE);
		command.setSpeedHA((float)speedHA);
		command.setPowerHA((float)powerHA);
		command.setPowerDE((float)powerDE);
		sendCommand();
		
		currentRARateE.setValue(slewRASpeed);
		currentDERateE.setValue(slewDESpeed);
		trackingRateE.setValue(trackSpeed);
		currentRateP.setState(PropertyStates.OK);
		try {
			updateProperty(currentRateP);
		} catch (INDIException e) {
			e.printStackTrace();
		}
	}
	
	private class ExposeTask extends TimerTask{
		@Override
		public void run() {
			printMessage("Expose");
			currentTask = this;
			
			command.enableBulb();
			sendCommand();

			intervalometer.schedule(new CompleteTask(), (long)(exposureTimeE.getValue()*1000));
			
			intervalometerSettingsP.setState(PropertyStates.ALERT);
			try {
				updateProperty(intervalometerSettingsP);
			} catch (INDIException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private class CompleteTask extends TimerTask{
		@Override
		public void run() {
			printMessage("Complete");
			currentTask = this;
			command.disableBulb();
			sendCommand();			
			
			double n = exposureNumberE.getValue();
			n=n-1;
			exposureNumberE.setValue(n);
			if(n>0){
				intervalometer.schedule(new ExposeTask(),  (long)(delayTimeE.getValue()*1000));
				intervalometerSettingsP.setState(PropertyStates.BUSY);
			}else{
				intervalometerSettingsP.setState(PropertyStates.OK);
			}
			try {
				updateProperty(intervalometerSettingsP);
			} catch (INDIException e) {
				e.printStackTrace();
			}
		}
		
	};
	

}