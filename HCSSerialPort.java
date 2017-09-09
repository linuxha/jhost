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

// HCSSerialPort is derived from HCSPort and implements access to a physical
// serial port.
//import java.io.*;
import java.util.*;
//import javax.comm.*;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class HCSSerialPort extends HCSPort implements SerialPortEventListener
{
	// Lock - A trivial class used to implement a mutex lock for HCSSerialPort.
	// This class is necessary because in normal operation two separate
	// processes (in the generic sense) are trying to use the port.
	// One process wants to send commands to the controller, the other wants
	// to receive updates from the controller. So we permit each process
	// to lock the port for it's exclusive use. This is a bit of a kludge but
	// ensures that we don't lose any update data when we send commands.
	public class Lock
	{
		public boolean inUse = false;
		public Integer key = null;
		public int count = 0;
	}

	private Lock lock;

	// Class private constants.
	private static final int parity = SerialPort.PARITY_NONE;
	private static final int databits = SerialPort.DATABITS_8;
	private static final int stopbits = SerialPort.STOPBITS_1;
	private static final int baudrate = 9600;
	private static final int timeout = 2000;	// milliseconds

	// Instance variables describing an HCS serial port.
	private CommPortIdentifier portId = null;
	private SerialPort serialPort = null;
	private InputStream inputStream = null;
	private OutputStream outputStream = null;
	private String portName = null;

	// Remember the parent object so that we can call back to it.
	// private HCSPortListener listener = null;

	HCSSerialPort(String name, HCSPortListener newListener) throws HCSException
	{
		super(newListener);
		lock = new Lock();

		if (null != name) open(name);
	}

	// Acquire sole access to this class. This method will block if it
	// is unable to acquire the lock.
	public Object acquire()
	{
		synchronized(lock)
		{
			if ( lock.inUse )
			{
				try { lock.wait(); }
				catch (InterruptedException e) {}
			}

			lock.inUse = true;
			lock.key = new Integer(++lock.count);
			return lock.key;
		}
	}

	public void release(Object obj)
	{
		synchronized(lock)
		{
			if ( lock.key != null && lock.key.equals(obj) )
			{
				lock.inUse = false;
				lock.notify();
			}
		}
	}

	// Return a Vector of Strings containing names of available ports.
	static public Vector getPortList()
	{
		Vector ports = new Vector();
		CommPortIdentifier portId;

		// Get an enumeration of the javax.comm ports available on
		// this machine.
		Enumeration portList = CommPortIdentifier.getPortIdentifiers();

		// Walk through the enumeration looking for all serial ports.
		// Add the name of each serail port to our Vector.
		while (portList.hasMoreElements())
		{
			portId = (CommPortIdentifier)portList.nextElement();
			if ( portId.getPortType() == CommPortIdentifier.PORT_SERIAL )
				ports.addElement(portId.getName());
		}
		return ports;
	}

	public void open(String name) throws HCSException
	{
		if ( name.equals(portName) )
			return;	// Not opening a new port.

		try
		{
			portId = CommPortIdentifier.getPortIdentifier(name);
		}
		catch (Exception e)
		{
			throw new HCSException(HCSException.INVALIDARGUMENT);
		}

		if ( serialPort != null )
			serialPort.close();	// We previously opened a port so close it.

		try
		{
			serialPort = (SerialPort)portId.open("JavaHost", timeout);
		}
		catch (Exception f)
		{
			throw new HCSException(HCSException.BADOPEN);
		}
		try
		{
			portName = name;
			inputStream = serialPort.getInputStream();
			outputStream = serialPort.getOutputStream();
			serialPort.setSerialPortParams( baudrate, databits, stopbits, parity );
			serialPort.enableReceiveTimeout(timeout);
			serialPort.addEventListener(this);
		}
		catch (Exception e)
		{
			throw new HCSException(HCSException.BADINITIALIZATION);
		}
	}

	//
	// HCSSerialPort doesn't implement these functions from HCSPort.
	// Instead it implements ones which ensure that the caller currently
	// has exclusive access to the port.
	//
	public void write(byte[] buf) throws HCSException
	{
		throw new HCSException(HCSException.BADFUNCTION);
	}

	public void write(byte chr) throws HCSException
	{
		throw new HCSException(HCSException.BADFUNCTION);
	}

	public byte[] read(int num) throws HCSException
	{
		throw new HCSException(HCSException.BADFUNCTION);
	}

	public void read(byte[] buf) throws HCSException
	{
		throw new HCSException(HCSException.BADFUNCTION);
	}

	public int read() throws HCSException
	{
		throw new HCSException(HCSException.BADFUNCTION);
	}

	public void write( Object key, byte[] buf ) throws HCSException
	{
		if ( !lock.inUse || lock.key == null || !lock.key.equals(key) )
			throw new HCSException(HCSException.BADOWNER);

		try
		{
			if ( outputStream != null )
				outputStream.write(buf);
		}
		catch (IOException e)
		{
			throw new HCSException(HCSException.BADWRITE);
		}
	}

	public void write( Object key, byte chr ) throws HCSException
	{
		if ( !lock.inUse || lock.key == null || !lock.key.equals(key) )
			throw new HCSException(HCSException.BADOWNER);

		try
		{
			if ( outputStream != null )
				outputStream.write(chr);
		}
		catch (IOException e)
		{
			throw new HCSException(HCSException.BADWRITE);
		}
	}

	public void read( Object key, byte[] buf ) throws HCSException
	{
		if ( !lock.inUse || lock.key == null || !lock.key.equals(key) )
			throw new HCSException(HCSException.BADOWNER);

		try
		{
			if ( inputStream != null )
				inputStream.read(buf);
		}
		catch (IOException e)
		{
			throw new HCSException(HCSException.BADREAD);
		}
	}

	public byte[] read( Object key, int num ) throws HCSException
	{
		if ( !lock.inUse || lock.key == null || !lock.key.equals(key) )
			throw new HCSException(HCSException.BADOWNER);

		byte[] buf = null;
		try
		{
			if ( inputStream != null )
			{
				buf = new byte[num];
				inputStream.read(buf);
			}
		}
		catch (IOException e)
		{
			throw new HCSException(HCSException.BADREAD);
		}
		return buf;
	}

	public int read(Object key) throws HCSException
	{
		if ( !lock.inUse || lock.key == null || !lock.key.equals(key) )
			throw new HCSException(HCSException.BADOWNER);

		int chr = -1;
		try
		{
			if ( inputStream != null )
				chr = inputStream.read();
		}
		catch (IOException e)
		{
			throw new HCSException(HCSException.BADREAD);
		}
		return chr;
	}

	// The communication with the controller happens in two ways. Either
	// a request is made to it which it responds to immediately, or it
	// can be sending back periodic reports automatically. Generally
	// we just want to listen to it, in which case setListenerState(true)
	// is used. However if we are sending a command we want to disable
	// listening and let the objects read the response directly.
	public void setListenerState( boolean enable )
	{
		if ( null != serialPort )
		{
			serialPort.notifyOnDataAvailable(enable);
			serialPort.notifyOnOverrunError(enable);
		}
	}

	// When we are notified that there is data on the serial port let
	// the HCSIOObject read it.
	public void serialEvent(SerialPortEvent event)
	{
		if ( null != listener)
		{
			int type = event.getEventType();
			if (type == SerialPortEvent.DATA_AVAILABLE )
			{
				// Immediately stop listening for DATA_AVAILABLE messages.
				// We'll read data until there is no more to read.
				setListenerState(false);
				try
				{
					while (inputStream.available() > 0)
						listener.portEvent();
				}
				catch (IOException e)
				{
					// InputStream.available() can throw an IOException, but
					// I believe it is unlikely (and more likely elsewhere where
					// it can be better handled, so we'll ignore it here.
				}
			}
			else if (type == SerialPortEvent.OE)
			{
				listener.portErrorEvent(HCSEvent.INPUTOVERRUN);
			}
		}
	}

	protected void finalize()
	{
		if (serialPort != null)
			serialPort.close();
	}

}

