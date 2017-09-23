package farom.astroiddriver.jssc;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.Date;

import farom.astroiddriver.CmdMessage;
import farom.astroiddriver.INDIAstroidDriver;
import farom.astroiddriver.StatusMessage;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;
import jssc.SerialPortTimeoutException;
import laazotea.indi.Constants;
import laazotea.indi.INDIException;
import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.driver.INDITextElement;
import laazotea.indi.driver.INDITextElementAndValue;
import laazotea.indi.driver.INDITextProperty;

/**
 * @author farom
 *
 */
public class INDIAstroidDriverJSSC extends INDIAstroidDriver implements SerialPortEventListener {

	private SerialPort serialPort;
	private INDITextProperty devicePortP; // DEVICE_PORT
	private INDITextElement devicePortE; // PORT
	private ByteBuffer buffer;
	
	/**
	 * @param inputStream
	 * @param outputStream
	 */
	public INDIAstroidDriverJSSC(InputStream inputStream, OutputStream outputStream) {
		super(inputStream, outputStream);
		serialPort = null;
		
		devicePortP = new INDITextProperty(this, "DEVICE_PORT", "Port", "Main Control", Constants.PropertyStates.IDLE,
				Constants.PropertyPermissions.RW);
		String[] portNames = SerialPortList.getPortNames();
		if (portNames.length > 0) {
			devicePortE = new INDITextElement(devicePortP, "PORT", portNames[0]);
			devicePortP.setState(PropertyStates.OK);
			addProperty(devicePortP, "Serial port found : " + portNames[0]);
			printMessage("Serial port found : " + portNames[0]);
		} else {
			devicePortE = new INDITextElement(devicePortP, "PORT", "/dev/tty???");
			devicePortP.setState(PropertyStates.ALERT);
			addProperty(devicePortP, "Serial port not found");
			printMessage("Serial port not found");
		}
		
		buffer=ByteBuffer.allocate(255);
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
		super.processNewTextValue(property, date, elementsAndValues);
		if (property == devicePortP) {
			if (elementsAndValues.length > 0) {
				devicePortE.setValue(elementsAndValues[0].getValue());
				devicePortP.setState(PropertyStates.OK);
			}

			try {
				updateProperty(devicePortP);
			} catch (INDIException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void driverConnect(Date timestamp) throws INDIException {
		printMessage("try to open " + devicePortE.getValue());
		try {
			serialPort = new SerialPort(devicePortE.getValue());
			serialPort.openPort();
			serialPort.setParams(9600, 8, 1, 0);
			serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
			serialPort.addEventListener(this);

			onConnected();

		} catch (SerialPortException e) {
			printMessage("Connection failled");
			updateProperty(devicePortP, "Connection failled");
			throw new INDIException("SerialPortException : " + e.getMessage());
		}		
	}
	
	@Override
	public void driverDisconnect(Date timestamp) throws INDIException {
		try {
			serialPort.closePort();
			
			onDisconnected();
			
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send the current command message to the device
	 */
	@Override
	protected void sendCommand() {
		try {
			if (!serialPort.writeBytes(command.getBytes())) {
				printMessage("error while sending data to the device");
			}
			System.out.println(DateFormat.getTimeInstance().format(new Date())+" - Sent command:");
			for (int i = 0; i < CmdMessage.MESSAGE_SIZE; i++) {
				byte[] array = command.getBytes();
				System.out.printf("%02X ", array[i]);
			}
			System.out.println("");
		} catch (SerialPortException e) {
			printMessage("error while sending data to the device");
			e.printStackTrace();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see jssc.SerialPortEventListener#serialEvent(jssc.SerialPortEvent)
	 */
	@Override
	public void serialEvent(SerialPortEvent event) {
		if (event.isRXCHAR()) {

			byte[] localBuffer;
			byte[] localBuffer2 = new byte[StatusMessage.MESSAGE_SIZE];
			
			try {
				localBuffer = serialPort.readBytes();

				//System.out.println("Read " + localBuffer.length + "bytes");
				if(localBuffer.length>buffer.remaining()) {
					//System.out.println(""+localBuffer.length+"bytes droped");
					return;
				}
				buffer.put(localBuffer);
				//System.out.println("buffer.position() = " + buffer.position());
				if(buffer.position()>StatusMessage.MESSAGE_SIZE) {
					buffer.flip();
					byte startFlag;
					startFlag = buffer.get();
					while(startFlag!=0x55) { 
						if(buffer.remaining()>StatusMessage.MESSAGE_SIZE) {
							System.out.print("1 byte droped");
							System.out.printf("0x%02X ",startFlag);
							System.out.println("");
							startFlag = buffer.get();
						}else{
							System.out.print("1 byte droped");
							System.out.printf("0x%02X ",startFlag);
							System.out.println(", waiting for data");
							buffer.compact();
							return;
						}
					}

					buffer.get(localBuffer2, 0, StatusMessage.MESSAGE_SIZE);
					buffer.compact();
					if (StatusMessage.verify(localBuffer2)) {						
						lastStatusMessage = new StatusMessage(localBuffer2);
						System.out.println("Valid message:");
						for (int i = 0; i < StatusMessage.MESSAGE_SIZE; i++) {
							System.out.printf("%02X ", localBuffer2[i]);
						}
						System.out.println("");
						System.out.println(lastStatusMessage);
						updateStatus();
					}else {
						System.out.println("Invalid message:");
						for (int i = 0; i < StatusMessage.MESSAGE_SIZE; i++) {
							System.out.printf("%02X ", localBuffer2[i]);
						}
						System.out.println("");
					}
				}
			} catch (SerialPortException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (NullPointerException e) {
				
			}


		}


		
		/*System.out.println("SerialPortEvent type="+event.getEventType());
		if (event.isRXCHAR()) {// If data is available
			System.out.println("=RXCHAR");
			System.out.println("size="+event.getEventValue());
			//if (event.getEventValue() == StatusMessage.MESSAGE_SIZE) {
				try {
					byte buffer[] = serialPort.readBytes(StatusMessage.MESSAGE_SIZE, 100);
					//serialPort.readBytes(event.getEventValue() - StatusMessage.MESSAGE_SIZE);

					if (StatusMessage.verify(buffer)) {
						
						lastStatusMessage = new StatusMessage(buffer);
						System.out.println("valid message (" + event.getEventValue() + "/"
								+ StatusMessage.MESSAGE_SIZE + " bytes):");
						for (int i = 0; i < StatusMessage.MESSAGE_SIZE; i++) {
							System.out.printf("%02X ", buffer[i]);
						}
						System.out.println("");
						System.out.println(lastStatusMessage);
						
						
						updateStatus();
					} else {
						printMessage(DateFormat.getTimeInstance().format(new Date())+" - invalid message");
						System.out.println(DateFormat.getTimeInstance().format(new Date())+" - invalid message (" + event.getEventValue() + "/"
								+ StatusMessage.MESSAGE_SIZE + " bytes):");
						for (int i = 0; i < StatusMessage.MESSAGE_SIZE; i++) {
							System.out.printf("%02X ", buffer[i]);
						}
						System.out.println("");
						
					}
				} catch (SerialPortException e) {
					e.printStackTrace();
				} catch (SerialPortTimeoutException e) {
					System.out.println("timed out");
					try {
						serialPort.purgePort(SerialPort.PURGE_RXCLEAR);
					} catch (SerialPortException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}

			//}else{
			//	System.out.println("event: " + event.getEventValue());
			//	try {
			//		serialPort.readBytes(event.getEventValue());
			//	} catch (SerialPortException e) {
			//		e.printStackTrace();
			//	}
			//}

		}*/
	}


}
