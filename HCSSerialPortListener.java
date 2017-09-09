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

import java.util.*;

public class HCSSerialPortListener
	implements HCSPortListener, HCSPortCommunicator
{
	private Object key = null;
	private HCSSerialPort port = null;
	private HCSEventListener eventListener = null;
	private boolean releasing = false;

	public HCSSerialPortListener(HCSSerialPort port,
		HCSEventListener eventListener)
	{
		this.port = port;
		this.eventListener = eventListener;
	}

	public void setPort(HCSPort newPort)
	{
		port = (HCSSerialPort)newPort;
	}

	public void acquire()
	{
		// Enable listening on the port by calling port.setListenerState(true)
		// Send a setTimeCommand() to disable any pauses.
		if ( null != port )
		{
			key = port.acquire();
			port.setListenerState(true);
		}
	}

	public void release() throws HCSException
	{
		// Send a pause command and flag that we want to release the port
		// when we get a response to the pause request. We don't want to
		// immediately stop watching the port because update data is
		// still coming in for a while and we don't want to throw it
		// away. Instead we flag that we want to release and wait for
		// a response to the request in the portEvent() method. When
		// we get the response we stop listening to the port and release
		// it properly.
		if ( null != port )
		{
			try
			{
				port.write(key, (byte)HCSPortCommunicator.ALERT);
				port.write(key, (byte)HCSPortCommunicator.CMDPAUSE);
			}
			catch (HCSException e)
			{
				throw new HCSException(HCSException.BADRELEASE);
			}
			releasing = true;
		}
	}

	public void portEvent()
	{
		if ( null == port || null == eventListener )
			return;

		// We don't want the port to listen to events while we're reading them.
		// Otherwise we'd just find ourselves right back here.
		// port.setListenerState(false);

		// Expect to see <RESPONSE> <cmd> [<data>]
		boolean released = false;
		int b;
		try { b = port.read(key); }
		catch (HCSException e)
		{
			eventListener.portEvent(new HCSEvent(HCSEvent.BADREAD, null));
			return;
		}
		if ( RESPONSE != b )
		{
			byte[] array = new byte[1];
			array[0] = (byte)b;
			eventListener.portEvent(new HCSEvent(HCSEvent.UNKNOWN, array));
		}
		else
		{
			try
			{
				b = port.read(key);
				switch (b)
				{
					case HCSPortCommunicator.DATATIME:
						eventListener.portEvent(new
							HCSEvent(HCSEvent.TIME, port.read(key, 8)));
						break;
					case HCSPortCommunicator.DATAX10MODULES:
						eventListener.portEvent(new
							HCSEvent(HCSEvent.X10MODULES, port.read(key, 3)));
						break;
					case HCSPortCommunicator.DATADIGITALINPUTS:
						eventListener.portEvent(new
							HCSEvent(HCSEvent.DIGITALINPUTS,
								port.read(key, 2)));
						break;
					case HCSPortCommunicator.DATADIGITALOUTPUTS:
						eventListener.portEvent(new
							HCSEvent(HCSEvent.DIGITALOUTPUTS,
								port.read(key, 2)));
						break;
					case HCSPortCommunicator.DATAANALOGINPUTS:
						eventListener.portEvent(new
							HCSEvent(HCSEvent.ANALOGINPUTS,
								port.read(key, 17)));
						break;
					case HCSPortCommunicator.DATAANALOGOUTPUTS:
						eventListener.portEvent(new
							HCSEvent(HCSEvent.ANALOGOUTPUTS,
								port.read(key, 5)));
						break;
					case HCSPortCommunicator.DATANETWORKMODULES:
						eventListener.portEvent(new
							HCSEvent(HCSEvent.NETWORKMODULES,
								port.read(key, 2)));
						break;
					case HCSPortCommunicator.DATANETBITS:
						eventListener.portEvent(new
							HCSEvent(HCSEvent.NETBITS, port.read(key, 2)));
						break;
					case HCSPortCommunicator.DATACONSOLEMESSAGE:
						eventListener.portEvent(new
							HCSEvent(HCSEvent.CONSOLEMESSAGE,
								getConsoleMessageData()));
						break;
					case HCSPortCommunicator.CMDPAUSE:
						if ( releasing )
						{
							// We've finally received a response to the
							// pause request so we can complete releasing
							// the port.
							released = true;
							releasing = false;
							port.release(key);
						}
						break;
					default:
					{
						byte[] array = new byte[1];
						array[0] = (byte)b;
						eventListener.portEvent(new
							HCSEvent(HCSEvent.UNKNOWN, array));
					}
				}
			}
			catch (HCSException e)
			{
				eventListener.portEvent(new HCSEvent(HCSEvent.BADREAD, null));
				return;
			}
		}

		// We're done so re-enable the port listener unless we just released
		// it in which case we just don't take it back.
		// The port has stopped listening for DATA_AVAILABLE. If we still
		// have the port tell it to start listenining again.
		if ( !released)
			port.setListenerState(true);
	}

	public void portErrorEvent(int errorType)
	{
		eventListener.portEvent(new HCSEvent(errorType, null));
	}

	private byte[] getConsoleMessageData() throws HCSException
	{
		Vector vector = new Vector();
		byte[] consoleMessage = null;
		int b = port.read(key);
		while(0x00 != b)
		{
			vector.addElement(new Byte((byte)b));
			b = port.read(key);
		}

		// Now convert the Vector into an array of bytes.
		consoleMessage = new byte[vector.size()];
		for (int i = 0; i < vector.size(); i++ )
			consoleMessage[i] = ((Byte)vector.elementAt(i)).byteValue();

		return consoleMessage;
	}
}

