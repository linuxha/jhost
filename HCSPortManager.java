// Copyright (C) 2001, Paul Tokarchuk
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
// For updates check out the Creative Control Concepts web page at:
// http://www.cc-concepts.com
//
// Or to contact the author send email to ptokarchuk@sympatico.ca
//

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

public class HCSPortManager implements HCSEventListener
{
	private class EventThread extends Thread
	{
		private EventThread next = null;
		private HCSEvent event = null;
		private HCSEventListener listener = null;
		private int type;

		public EventThread( HCSEventListener listener,
			int type, HCSEvent event, int id )
		{
			super( "EventThread "+id );
			setDaemon(true);
			setPriority(2);		// Set priority lower than the IO code.
								// It's more important that we get the
								// events than display them. Display events
								// can always be compressed if necessary.

			this.type = type;
			this.event = event;
			this.listener = listener;
		}

		public EventThread next() { return next; }
		public void next(EventThread thread) { next = thread; }

		public synchronized void setWorkUnit(HCSEventListener listener,
			int type, HCSEvent event)
		{
			this.type = type;	// Type may be HCSEvent.ALL.
			this.event = event;
			this.listener = listener;
			notify();
		}

		public synchronized void run()
		{
			while(true)
			{
				// We're awake that means we have an event to handle.
				if ( listener != null )
				{
					switch (type)
					{
						case HCSEvent.TIME:
							timeEvent(event);
							break;
						case HCSEvent.X10MODULES:
							x10Event(event);
							break;
						case HCSEvent.DIGITALINPUTS:
							digitalInputsEvent(event);
							break;
						case HCSEvent.DIGITALOUTPUTS:
							digitalOutputsEvent(event);
							break;
						case HCSEvent.ANALOGINPUTS:
							analogInputsEvent(event);
							break;
						case HCSEvent.ANALOGOUTPUTS:
							analogOutputsEvent(event);
							break;
						case HCSEvent.NETWORKMODULES:
							networkModulesEvent(event);
							break;
						 case HCSEvent.NETBITS:
							netbitsEvent(event);
							break;
						case HCSEvent.CONSOLEMESSAGE:
							event.message = new String(event.rawdata);
							event.type &= (~HCSEvent.RAW);
							listener.portEvent(event);
							break;
						case HCSEvent.INPUTOVERRUN:
							event.type &= (~HCSEvent.RAW);
							// Fall through.
						case HCSEvent.ALL:
						case HCSEvent.UNKNOWN:
						default:
							listener.portEvent(event);
							break;
					}
				}

				event = null;
				addAvailableThread(this);
				try { wait(); }	// Wait immediately after creation. 
				catch (InterruptedException e) { return; }

			}
		}

		private void timeEvent(HCSEvent event)
		{
			if ( event.rawdata.length != 8 ) return;

			event.type &= (~HCSEvent.RAW);
			event.year         = decodeBCD(event.rawdata[7])+2000;
			event.month        = decodeBCD(event.rawdata[6])-1;
			event.day          = decodeBCD(event.rawdata[5]);
			event.dow          = decodeBCD(event.rawdata[4]);
			event.hour         = decodeBCD(event.rawdata[3]);
			event.minute       = decodeBCD(event.rawdata[2]);
			event.second       = decodeBCD(event.rawdata[1]);
			event.millisecond  = decodeBCD(event.rawdata[0])*10;
			listener.portEvent(event);
		}

		private void digitalInputsEvent(HCSEvent event)
		{
			if ( event.rawdata.length != 2 ) return;
			event.type &= (~HCSEvent.RAW);
			event.unit = (int)event.rawdata[0];
			event.state = (int)event.rawdata[1];
			listener.portEvent(event);
		}

		private void digitalOutputsEvent(HCSEvent event)
		{
			if ( event.rawdata.length != 2 ) return;
			event.type &= (~HCSEvent.RAW);
			event.unit = (int)event.rawdata[0];
			event.state = (int)event.rawdata[1];
			listener.portEvent(event);
		}

		private void x10Event(HCSEvent event)
		{
			if ( event.rawdata.length != 3 ) return;

			event.type &= (~HCSEvent.RAW);
			event.unit = (int)event.rawdata[0];
			event.state = decodeLowHigh(event.rawdata[1], event.rawdata[2]);
			listener.portEvent(event);
		}

		private void analogOutputsEvent(HCSEvent event)
		{
			if ( event.rawdata.length != 5 ) return;
			event.type &= (~HCSEvent.RAW);
			event.unit = (int)event.rawdata[0];
			event.values = new int[2];
			event.values[0] = decodeLowHigh(event.rawdata[1], event.rawdata[2]);
			event.values[1] = decodeLowHigh(event.rawdata[3], event.rawdata[4]);

			listener.portEvent(event);
		}

		private void analogInputsEvent(HCSEvent event)
		{
			if ( event.rawdata.length != 17 ) return;
			event.type &= (~HCSEvent.RAW);
			event.unit = (int)event.rawdata[0];
			event.values = new int[8];
			for (int i = 1, j = 0; j < 8; i+=2, j++)
				event.values[j] = decodeLowHigh(event.rawdata[i],
					event.rawdata[i+1]);

			listener.portEvent(event);
		}

		private void netbitsEvent(HCSEvent event)
		{
			if ( event.rawdata.length != 2 ) return;
			event.type &= (~HCSEvent.RAW);
			event.unit = (int)event.rawdata[0];
			event.state = (int)event.rawdata[1];
			listener.portEvent(event);
		}

		private void networkModulesEvent(HCSEvent event)
		{
			if ( event.rawdata.length != 2 ) return;
			event.type &= (~HCSEvent.RAW);
			event.module = (int)((event.rawdata[0]&0xF0)>>>4);
			event.unit = (int)(event.rawdata[0]&0x0F);
			event.state = (int)event.rawdata[1];
			listener.portEvent(event);
		}
	}

	private HCSPort port = null;
	private HCSPortListener listener = null;
	private HCSPortCommander commander = null;
	private Object menuBar = null;

	private boolean	paused = true;

	// Create a dummy head of list to synchronize on.
	private EventThread availableThread = new EventThread(null, -1, null, -1);

	protected Vector[] listeners = new Vector[HCSEvent.NUMEVENTTYPES];

	public HCSPortManager(String name) throws HCSException
	{
		// These next three lines would have to changed for some other type
		// of HCSPort. Or better yet allowed to be set for a selection of
		// ports.
		if (name != null)
			port = new HCSSerialPort(name, null);
		listener = new HCSSerialPortListener((HCSSerialPort)port, this);
		commander = new HCSSerialPortCommander((HCSSerialPort)port);

		if (port != null)
			port.setPortListener(listener);

		for (int i = 0; i < HCSEvent.NUMEVENTTYPES; i++)
			listeners[i] = new Vector();
	}

	public Vector getPortList()
	{
		return HCSSerialPort.getPortList();
	}

	public void openPort(String portName) throws HCSException
	{
		if (port == null)
		{
			port = new HCSSerialPort(portName, null);
			listener.setPort(port);
			commander.setPort(port);
			port.setPortListener(listener);
		}
		else
			port.open(portName);
		listener.acquire();
		paused = false;
	}

	// A little utility function which loads classes. Java 2.0 has a
	// URLClassLoader class that could be used, but for now this code
	// is Java 1.1 compatible.
	public Class loadClass(String path, String name) throws HCSException
	{
		// Get a class loader.
		FileClassLoader loader = new FileClassLoader(path);

		// Load the class and return a pointer to it.
		Class c = null;

		try
		{
			c = loader.loadClass(name);
		}
		catch (ClassNotFoundException e)
		{
			throw new HCSException(HCSException.BADCLASS);
		}

		return c;
	}

	public void loadUI(String dirName, String className)
		throws HCSException, NoSuchMethodException, InstantiationException,
			IllegalAccessException, InvocationTargetException
		// Throws NoSuchMethodException if the constructor cannot be found
		// Throws InstantiationException if instantiation failed.
		// Throws IllegalAccessException if unable to access constructor.
		// Throws InvocationTargetException if the constructor throws an exception.
	{
		// Load the class specified.
		Class c = loadClass(dirName, className);

		// Define the argument types for the constructor we want.
		Class[] paramTypes = new Class[1];
		paramTypes[0] = HCSPortManager.class;

		// Get the class constructor. The constructor must take
		// an HCSPortManager as the single argument.
		Constructor con = c.getConstructor(paramTypes);

		if ( null == con )
			throw new NoSuchMethodException();

		// Bundle up the argument to pass to the constructor.
		Object[] argList = new Object[1];
		argList[0] = this;

		// Call the constructor to get a new object.
		Object obj = con.newInstance(argList);
	}

	// The next three methods sendCommand(), pause(), and resume() are
	// synchronized as they can be called by separate threads but all
	// control the state of the listener.

	// This method is the lowest level. It can be called with byte arrays
	// containing the HCS command. See examples below.
	public synchronized byte[] sendCommand(byte cmd, byte[] data)
		throws HCSException
	{
		if ( null == port )
			throw new HCSException(HCSException.NOTOPEN);

		if (paused == true)
			throw new HCSException( HCSException.PAUSED );

		listener.release();
		byte[] results = null;
		try
		{
			results = commander.sendCommand(cmd, data);
		}
		finally
		{
			// If sendCommand() generates an exception make sure that we
			// attempt to let the listener reacquire the port. On an
			// exception, since we have no catch block, the return statement
			// will be skipped.
			listener.acquire();
		}

		return results;
	}

	public synchronized void pause() throws HCSException
	{
		// We cannot just send a pause command to the controller using the
		// normal sendCommand() approach. Doing so would not have the desired
		// effect (it would in fact appear to do nothing). This is due to the
		// way the commander/listener pair work. When the commander wants to
		// send a command it asks the listener to release control of the
		// port. The listener does this in part by sending a pause command
		// to the controller. The listener then holds on to the port until
		// the controller acknowledges the pause command (and is therefore
		// pausing). This ensures that no data is coming in from the controller
		// when a command is going out to it (this in and of itself isn't
		// important, but allows the commander to easily check for an
		// acknowledgement from the controller to the command). Once the
		// commander sends a command, the controller will send the response
		// and then waken from the pause and start sending the normal
		// information. Given this cycle obviously having the commander
		// send a pause command wouldn't work. The listener would have paused
		// the output, the commander would send the pause command, which would
		// then wake up the controller.
		if (paused) return;

		if ( null == port )
			throw new HCSException(HCSException.NOTOPEN);

		listener.release();
		paused = true;
	}

	public synchronized void resume() throws HCSException
	{
		if (!paused) return;

		if (null == port)
			throw new HCSException(HCSException.NOTOPEN);

		// We request a full information dump from the controller on
		// returning from the pause so that we can learn of any changes
		// that might have occurred while paused.
		paused = false;
		listener.acquire();
		getInfo();
	}

	public void getInfo() throws HCSException
	{
		// This command requests a full update from the controller, but
		// doesn't expect a response. The data will be returned with the
		// next normal update from the controller. To actually return
		// the update data the listener cannot be allowed to reacquire
		// the port, and instead this function would have to wait until
		// a full update is sent from the controller. Unfortunately there's
		// no way to know when the full update has been completely sent
		// so rather than waiting for a response we leave this to the
		// normal event listening process.
		sendCommand(HCSPortCommunicator.CMDINFO, null);
	}

	public void doReset() throws HCSException
	{
		sendCommand(HCSPortCommunicator.CMDRESET, null);
	}

	public void clearLog() throws HCSException
	{
		sendCommand(HCSPortCommunicator.CMDCLEARLOG, null);
	}

	public int getLogSize() throws HCSException
	{
		byte[] data = sendCommand(HCSPortCommunicator.CMDGETLOGSIZE, null);
		return decodeLowHigh(data[0], data[1]);
	}

	public byte[] getLog() throws HCSException
	{
		return sendCommand(HCSPortCommunicator.CMDGETLOG, null);
	}

	public void setNetbitState( int netbit, boolean state ) throws HCSException
	{
		if ( netbit < 0 || netbit > 319)
			throw new HCSException(HCSException.INVALIDARGUMENT);

		byte[] data = new byte[3];
		data[0] = (byte)(netbit&0x00FF);
		data[1] = (byte)(netbit>>>8);
		data[2] = (byte)(state ? 1 : 0);
		sendCommand(HCSPortCommunicator.CMDSETNETBITSTATE, data);
	}

	public boolean getNetbitState(int netbit) throws HCSException
	{
		if ( netbit < 0 || netbit > 319)
			throw new HCSException(HCSException.INVALIDARGUMENT);

		byte[] data = new byte[2];
		data[0] = (byte)(netbit&0xFF);
		data[1] = (byte)(netbit>>>8);
		data = sendCommand(HCSPortCommunicator.CMDGETNETBITSTATUS, data);
		if ( 3 == data.length )
		{
			int returnedNetbit = decodeLowHigh(data[0], data[1]);
			if ( returnedNetbit == netbit )
				return (data[1] == 1);
			else
				throw new HCSException(HCSException.RESPONSEMISMATCH);
		}
		else
			throw new HCSException(HCSException.BADRESPONSESIZE);
	}

	public void selectNetbitsToDisplay(long bits) throws HCSException
	{
		// The compiler doesn't like a 40 bit constant (must treat them
		// all as ints to start) so can't do this test simply.
		// final long mask = 0xFFFFFFFFFF;		// 2^40-1
		// if ( (bits & mask) != mask)
			// throw new HCSException(HCSException.INVALIDARGUMENT);

		byte[] data = new byte[5];
		data[0] = (byte)(bits & 0xFF);
		data[1] = (byte)((bits >>> 8) & 0xFF);
		data[2] = (byte)((bits >>> 16) & 0xFF);
		data[3] = (byte)((bits >>> 24) & 0xFF);
		data[4] = (byte)((bits >>> 32) & 0xFF);
		sendCommand(HCSPortCommunicator.CMDSETNETBITDISPLAY, data);
	}

	public int getNetModuleStatus(int module, int unit) throws HCSException
	{
		if (module < 0 || module > 5 || unit < 0 || unit > 7)
			throw new HCSException(HCSException.INVALIDARGUMENT);

		int codedUnit = (module<<4)+unit;
		byte[] data = new byte[1];
		data[0] = (byte)(codedUnit);
		data = sendCommand(HCSPortCommunicator.CMDGETNETMODULESTATUS, data);
		if ( data.length == 2 )
		{
			if ((int)data[0] != codedUnit)
				throw new HCSException(HCSException.RESPONSEMISMATCH);
			else
				return (int)(data[1]);
		}
		else
			throw new HCSException(HCSException.BADRESPONSESIZE);
	}

	public void selectNetModulesToDisplay(int modules) throws HCSException
	{
		if ( modules < 0 || modules > 31)
			throw new HCSException(HCSException.INVALIDARGUMENT);

		byte[] data = new byte[1];
		data[0] = (byte)modules;
		sendCommand(HCSPortCommunicator.CMDSETNETMODULEDISPLAY, data);
	}

	public void setAnalogOutputValue(int dac, int value) throws HCSException
	{
		if (dac < 0 || dac > 31 || value < 0 || value > 4095 )
			throw new HCSException(HCSException.INVALIDARGUMENT);

		byte[] data = new byte[3];
		data[0] = (byte)dac;
		data[1] = (byte)(value & 0xFF);
		data[2] = (byte)(value >>> 8);
		sendCommand(HCSPortCommunicator.CMDSETDACVALUE, data);
	}

	public int getAnalogOutputValue(int dac) throws HCSException
	{
		if (dac < 0 || dac > 31)
			throw new HCSException(HCSException.INVALIDARGUMENT);

		byte[] data = new byte[1];
		data[0] = (byte)dac;
		data = sendCommand(HCSPortCommunicator.CMDGETDACVALUE, data);
		if ( 3 == data.length )
		{
			if ( dac != (int)data[0] )
				throw new HCSException(HCSException.RESPONSEMISMATCH);
			return decodeLowHigh(data[1], data[2]);
		}
		else
			throw new HCSException(HCSException.BADRESPONSESIZE);
	}

	public void selectAnalogOutputsToDisplay(int dacs) throws HCSException
	{
		if (dacs < 0 || dacs > 0x0F)
			throw new HCSException(HCSException.INVALIDARGUMENT);

		byte[] data = new byte[1];
		data[0] = (byte)dacs;
		sendCommand(HCSPortCommunicator.CMDSETDACDISPLAY, data);
	}

	public void setAnalogInputValue(int input, int value) throws HCSException
	{
		if ( input < 0 || input > 135 || value < 0 || value > 4096 )
			throw new HCSException(HCSException.INVALIDARGUMENT);

		byte[] data = new byte[3];
		data[0] = (byte)input;
		data[1] = (byte)(value & 0xFF);
		data[2] = (byte)(value >>> 8);
		sendCommand(HCSPortCommunicator.CMDSETADCVALUE, data);
	}

	public int getAnalogInputValue(int input) throws HCSException
	{
		if (input < 0 || input > 135)
			throw new HCSException(HCSException.INVALIDARGUMENT);

		byte[] data = new byte[1];
		data[0] = (byte)input;
		data = sendCommand(HCSPortCommunicator.CMDGETADCVALUE, data);
		if ( 3 == data.length )
		{
			if (input != (int)data[0])
				throw new HCSException(HCSException.RESPONSEMISMATCH);
			return decodeLowHigh(data[1], data[2]);
		}
		else
			throw new HCSException(HCSException.BADRESPONSESIZE);
	}

	public void selectAnalogInputsToDisplay(int inputs) throws HCSException
	{
		if ((inputs & 0xFFFFFF) != inputs)
			throw new HCSException(HCSException.INVALIDARGUMENT);

		byte[] data = new byte[3];
		data[0] = (byte)(inputs & 0xFF);
		data[1] = (byte)((inputs >>> 8) & 0xFF);
		data[2] = (byte)((inputs >>> 16) & 0xFF);
		sendCommand(HCSPortCommunicator.CMDSETADCDISPLAY, data);
	}

	public void setDigitalOutputState(int output, boolean state)
		throws HCSException
	{
		if (output < 0 || output > 255)
			throw new HCSException(HCSException.INVALIDARGUMENT);
		byte[] data = new byte[2];
		data[0] = (byte)output;
		data[1] = (byte)(state?1:0);
		sendCommand(HCSPortCommunicator.CMDSETDIGOUTSTATE, data);
	}

	public boolean getDigitalOutputState(int output) throws HCSException
	{
		if (output < 0 || output > 255)
			throw new HCSException(HCSException.INVALIDARGUMENT);

		byte[] data = new byte[1];
		data[0] = (byte)output;
		data = sendCommand(HCSPortCommunicator.CMDGETDIGOUTSTATUS, data);
		if ( 2 == data.length)
		{
			if (data[0] != output)
				throw new HCSException(HCSException.RESPONSEMISMATCH);
			return (data[1]==0?false:true);
		}
		else
			throw new HCSException(HCSException.BADRESPONSESIZE);
	}

	public void selectDigitalOutputsToDisplay(int outputs) throws HCSException
	{
		if ( (outputs & 0xFFFFFFFF) != outputs)
			throw new HCSException(HCSException.INVALIDARGUMENT);

		byte[] data = new byte[4];
		data[0] = (byte)(outputs & 0xFF);
		data[1] = (byte)((outputs >>> 8) & 0xFF);
		data[2] = (byte)((outputs >>> 16) & 0xFF);
		data[3] = (byte)((outputs >>> 24) & 0xFF);
		sendCommand(HCSPortCommunicator.CMDSETDIGOUTDISPLAY, data);
	}

	public void setDigitalInputState(int input, int state) throws HCSException
	{
		if (input < 0 || input > 255 || state < 0 || state > 2)
			throw new HCSException(HCSException.INVALIDARGUMENT);

		byte[] data = new byte[2];
		data[0] = (byte)input;
		data[1] = (byte)state;
		sendCommand(HCSPortCommunicator.CMDSETDIGINSTATE, data);
	}

	public int getDigitalInputState(int input) throws HCSException
	{
		if (input < 0 || input > 255)
			throw new HCSException(HCSException.INVALIDARGUMENT);

		byte[] data = new byte[1];
		data[0] = (byte)input;
		data = sendCommand(HCSPortCommunicator.CMDGETDIGINSTATUS, data);
		if ( 2 == data.length )
		{
			if ( (int)data[0] != input)
				throw new HCSException(HCSException.RESPONSEMISMATCH);
			return (int)data[1];
		}
		else
			throw new HCSException(HCSException.BADRESPONSESIZE);
	}

	public void selectDigitalInputsToDisplay(int inputs) throws HCSException
	{
		if ( (inputs & 0xFFFFFFFF) != inputs)
			throw new HCSException(HCSException.INVALIDARGUMENT);

		byte[] data = new byte[4];
		data[0] = (byte)(inputs & 0xFF);
		data[1] = (byte)((inputs >>> 8) & 0xFF);
		data[2] = (byte)((inputs >>> 16) & 0xFF);
		data[3] = (byte)((inputs >>> 24) & 0xFF);
		sendCommand(HCSPortCommunicator.CMDSETDIGINDISPLAY, data);
	}

	public int getVariable(int variable) throws HCSException
	{
		if ( variable < 0 || variable > 127 )
			throw new HCSException(HCSException.INVALIDARGUMENT);

		byte[] data = new byte[1];
		data[0] = (byte)variable;
		data = sendCommand(HCSPortCommunicator.CMDGETVARIABLEVALUE, data);
		if ( data.length == 3 )
		{
			if ( (int)data[0] != variable)
				throw new HCSException(HCSException.RESPONSEMISMATCH);
			return decodeLowHigh(data[1], data[2]);
		}
		else
			throw new HCSException(HCSException.BADRESPONSESIZE);
	}

	public void setVariable(int variable, int value) throws HCSException
	{
		if ( variable < 0 || variable > 127 || value < 0 || value > 65535)
			throw new HCSException(HCSException.INVALIDARGUMENT);

		byte[] data = new byte[3];
		data[0] = (byte)variable;
		data[1] = (byte)(value&0xFF);
		data[2] = (byte)(value >>> 8);
		sendCommand(HCSPortCommunicator.CMDSETVARIABLEVALUE, data);
	}

	public void sendNetworkString(String string) throws HCSException
	{
		if (string.length() > 0)
		{
			// The string must be null terminated when passed to the HCS.
			byte[] bytes = string.getBytes();
			byte[] data = new byte[bytes.length+1];
			for (int i = 0; i < bytes.length; i++) data[i] = bytes[i];
			data[bytes.length] = '\0';
			sendCommand(HCSPortCommunicator.CMDSETNETWORKSTRING,data);
		}
	}

	public void sendVoiceString(String string) throws HCSException
	{
		if (string.length() > 0)
		{
			// The string must be null terminated when passed to the HCS.
			byte[] bytes = string.getBytes();
			byte[] data = new byte[bytes.length+1];
			for (int i = 0; i < bytes.length; i++) data[i] = bytes[i];
			data[bytes.length] = '\0';
			sendCommand(HCSPortCommunicator.CMDSETVOICESTRING,data);
		}
	}

	public void setX10Module(int code, int unit, int command, int level)
		throws HCSException
	{
		if ( code < 1 || code > 16 || unit < 1 || unit > 16
				|| command < 0 || command > 5 || level < 0 || level > 31 )
			throw new HCSException(HCSException.INVALIDARGUMENT);

		// Controller wants units numbers from 0-15.
		code--; unit--;

		byte[] data = new byte[3];
		data[0] = (byte)( (code<<4) | unit );
		data[1] = (byte)command;
		data[2] = (byte)level;
		sendCommand(HCSPortCommunicator.CMDSETX10MODULE, data);
	}

	public boolean getX10Status(int code, int unit) throws HCSException
	{
		if ( code < 1 || code > 16 || unit < 1 || unit > 16 )
			throw new HCSException(HCSException.INVALIDARGUMENT);
	
		// Controller wants units numbers from 0-15.
		code--; unit--;

		int codedModule = (byte)( (code<<4) | unit );
		byte[] data = new byte[1];
		data[0] = (byte)codedModule;
		data = sendCommand(HCSPortCommunicator.CMDGETX10STATUS, data);
		if ( data.length == 2 )
		{
			if (codedModule != (int)data[0])
				throw new HCSException(HCSException.RESPONSEMISMATCH);
			return (data[1] == 1);
		}
		else
			throw new HCSException(HCSException.BADRESPONSESIZE);
	}

	public void selectX10Display(int codes) throws HCSException
	{
		if ( (codes & 0xFFFF) != codes )
			throw new HCSException(HCSException.INVALIDARGUMENT);
	
		byte[] data = new byte[2];
		data[0] = (byte)(codes & 0xFF);
		data[1] = (byte)(codes >>> 8);
		sendCommand(HCSPortCommunicator.CMDSETX10DISPLAY, data);
	}

	public void load(String filename) throws HCSException
	{
		byte[] buf = null; // new byte[64*1024];
		FileInputStream fis = null;
		File file = new File(filename);

		try
		{
			fis = new FileInputStream(file);
		}
		catch (FileNotFoundException f)
		{
			throw new HCSException(HCSException.BADOPEN);
		}

		if (file.length() > 65536)
			throw new HCSException(HCSException.PROGRAMSIZE);

		buf = new byte[(int)file.length()];

		try
		{
			int bytesRead = fis.read(buf);
			if ( fis.read() != -1 )
				throw new HCSException(HCSException.INCOMPLETEREAD);
		}
		catch (IOException e)
		{
			throw new HCSException(HCSException.BADREAD);
		}
		finally
		{
			try
			{
				fis.close();
			}
			catch (IOException e2)
			{
				throw new HCSException(HCSException.BADCLOSE);
			}
		}

		sendCommand(HCSPortCommunicator.CMDLOAD, buf);
	}

	public void setLog(int location, int value) throws HCSException
	{
		if (location < 0 || location > 65535 || value < 0 || value > 255)
			throw new HCSException(HCSException.INVALIDARGUMENT);

		byte[] data = new byte[3];
		data[0] = (byte)value;
		data[1] = (byte)(location&0xFF);
		data[2] = (byte)(location >>> 8);
		sendCommand(HCSPortCommunicator.CMDSETLOG, data);
	}

	public Calendar getTime() throws HCSException
	{
		byte[] time = sendCommand(HCSPortCommunicator.CMDGETTIME, null);
		Calendar cal = Calendar.getInstance();
		cal.set(
			decodeBCD(time[7])+2000,	// year
			decodeBCD(time[6])-1,		// month
			decodeBCD(time[5]),			// day
			decodeBCD(time[3]),			// hours
			decodeBCD(time[2]),			// minutes
			decodeBCD(time[1]) );		// seconds

		return cal;
	}

	public void setTime() throws HCSException
	{
		setTime(Calendar.getInstance());
	}

	public void setTime(Calendar cal) throws HCSException
	{
		int millisecond = cal.get(Calendar.MILLISECOND);
		int second      = cal.get(Calendar.SECOND);
		int minute      = cal.get(Calendar.MINUTE);
		int hour        = cal.get(Calendar.HOUR_OF_DAY);
		int dow         = cal.get(Calendar.DAY_OF_WEEK);
		int day         = cal.get(Calendar.DAY_OF_MONTH);
		int month       = cal.get(Calendar.MONTH);
		int year        = cal.get(Calendar.YEAR);

		setTime(year, month, day, dow, hour, minute, second, millisecond);
	}

	public void setTime(int year, int month, int day, int dow, int hour,
		int minute, int second, int millisecond) throws HCSException
	{
		byte[] time = new byte[8];
		time[0] = encodeBCD( millisecond/10 );
		time[1] = encodeBCD( second );
		time[2] = encodeBCD( minute );
		time[3] = encodeBCD( hour );
		time[4] = encodeBCD( dow );
		time[5] = encodeBCD( day );
		time[6] = encodeBCD( month+1 );
		time[7] = encodeBCD( year-2000 );
		sendCommand(HCSPortCommunicator.CMDSETTIME, time);
	}

	public void setTimeFrequency(int frequency) throws HCSException
	{
		if ( frequency < 0 || frequency > 2 )
			throw new HCSException(HCSException.INVALIDARGUMENT);

		byte[] data = new byte[1];
		data[0] = (byte)frequency;
		sendCommand(HCSPortCommunicator.CMDSETTIMEFREQUENCY, data);
	}

	protected static int decodeBCD(byte bcd)
	{
		int high = (int)(bcd & 0xf0);
		int low = (int)(bcd & 0x0f);
		high = (high >>> 4)*10;
		return low + high;
	}

	private static byte encodeBCD(int decimal)
	{
		int high = decimal/10;
		int low = decimal%10;
		return (byte)((high << 4) | low);
	}

	protected int decodeLowHigh(byte lo, byte hi)
	{
		int t = ((int)(hi<<8))&0xFF00;
		return t + (((int)lo)&0xFF);
	}

	protected void addAvailableThread(EventThread thread)
	{
		// There is a dummy element at the head of the list of available
		// threads. This allows us to avoid problems with synchronizing on
		// a null.
		synchronized(availableThread)
		{
			thread.next(availableThread.next());
			availableThread.next(thread);
		}
	}

	protected EventThread getAvailableThread()
	{
		EventThread thread = null;
		synchronized(availableThread)
		{
			thread = availableThread.next();
			// if ( thread == null ) availableThread.next(null);
			// else
			if (thread != null)
			{
				availableThread.next(thread.next());
				thread.next(null);
			}
		}
		return thread;
	}

	private int numThreads = 0;

	private void portEvent(Enumeration list, int type, HCSEvent event)
	{
		while (list.hasMoreElements())
		{
			HCSEventListener listener = (HCSEventListener)(list.nextElement());
			EventThread thread = getAvailableThread();
			if ( null != thread )
				thread.setWorkUnit(listener, type, event);
			else
			{
				// No threads are available, so we create one. An alternate
				// and faster (but less memory efficient) approach would
				// be to have one thread per listener and just notify
				// the thread when a new event has come in. This has the
				// added benefit of providing a simple solution to
				// event compression (ignore new events as long as the
				// thread is busy).
				thread = new EventThread(listener, type, event, numThreads++);
				thread.start();
			}
		}
	}

	public void portEvent(HCSEvent event)
	{
		// If there are listeners for this type of event handle them.
		int type = event.type & (~HCSEvent.RAW);
		if ( !listeners[type].isEmpty() )
			portEvent(listeners[type].elements(), type, event);

		// If someone is listening to all events, handle them too.
		if ( !listeners[HCSEvent.ALL].isEmpty() )
			portEvent(listeners[HCSEvent.ALL].elements(), HCSEvent.ALL, event);
	}

	public void addEventListener(int type, HCSEventListener listener)
	{
		if ( !listeners[type].contains(listener) )
			listeners[type].addElement(listener);
	}

	public void removeEventListener(int type, HCSEventListener listener)
	{
		if ( listeners[type].contains(listener) )
			listeners[type].removeElement(listener);
	}

	// A UI may choose to register its menuBar object with the portManager so
	// that other UI objects can choose to add themselves to an existing
	// menuBar. The menuBar though could be an AWT menuBar, or a Swing menuBar,
    // or any number of other possible UI toolkit menuBars. So we'll remember
    // that it is an object, and require that the UI object properly check the
	// type of menuBar object returned.
	public void registerMenuBar(Object menuBar)
	{
		this.menuBar = menuBar;
	}

	public Object getMenuBar()
	{
		return menuBar;
	}
}

