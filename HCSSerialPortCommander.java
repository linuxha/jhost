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

// This class with HCSListener form a pair of classes for operating with
// the HCS controller. HCSCommander is used for sending commands to the
// controller. HCSListener is used for listening to regular updates from the
// controller.
import java.util.*;

public class HCSSerialPortCommander
	implements HCSPortCommander, HCSPortCommunicator
{
	private Object key = null;
	private HCSSerialPort port = null;
	private byte[] responseByteCount = new byte[HCSPortCommunicator.MAXCMD+1];
	private byte[] inputByteCount = new byte[HCSPortCommunicator.MAXCMD+1];

	public HCSSerialPortCommander(HCSSerialPort newPort)
	{
		port = newPort;
		for (int i = 0; i <= HCSPortCommunicator.MAXCMD; i++)
			inputByteCount[i] = responseByteCount[i] = 0;

		// responseByteCount[HCSPortCommunicator.CMDCLEARLOG]			= 0;
		responseByteCount[HCSPortCommunicator.CMDGETADCVALUE]			= 3;
		responseByteCount[HCSPortCommunicator.CMDGETDACVALUE]			= 3;
		responseByteCount[HCSPortCommunicator.CMDGETDIGINSTATUS]		= 2;
		responseByteCount[HCSPortCommunicator.CMDGETDIGOUTSTATUS]		= 2;
		// responseByteCount[HCSPortCommunicator.CMDGETLOG]				= ?;
		responseByteCount[HCSPortCommunicator.CMDGETLOGSIZE]			= 2;
		responseByteCount[HCSPortCommunicator.CMDGETNETMODULESTATUS]	= 2;
		responseByteCount[HCSPortCommunicator.CMDGETNETBITSTATUS]		= 3;
		responseByteCount[HCSPortCommunicator.CMDGETTIME]				= 8;
		responseByteCount[HCSPortCommunicator.CMDGETX10STATUS]			= 2;
		responseByteCount[HCSPortCommunicator.CMDGETVARIABLEVALUE]		= 3;
		// responseByteCount[HCSPortCommunicator.CMDINFO]				= 0;
		// responseByteCount[HCSPortCommunicator.CMDLOAD]				= ?;
		// responseByteCount[HCSPortCommunicator.CMDPAUSE]				= 0;
		// responseByteCount[HCSPortCommunicator.CMDRESET]				= 0;
		// responseByteCount[HCSPortCommunicator.CMDSETADCDISPLAY]		= 0;
		// responseByteCount[HCSPortCommunicator.CMDSETADCVALUE]		= 0;
		// responseByteCount[HCSPortCommunicator.CMDSETDACDISPLAY]		= 0;
		// responseByteCount[HCSPortCommunicator.CMDSETDACVALUE]		= 0;
		// responseByteCount[HCSPortCommunicator.CMDSETDIGINDISPLAY]	= 0;
		// responseByteCount[HCSPortCommunicator.CMDSETDIGINSTATE]		= 0;
		// responseByteCount[HCSPortCommunicator.CMDSETDIGOUTDISPLAY]	= 0;
		// responseByteCount[HCSPortCommunicator.CMDSETDIGOUTSTATE]		= 0;
		// responseByteCount[HCSPortCommunicator.CMDSETLOG]				= 0;
		// responseByteCount[HCSPortCommunicator.CMDSETNETBITDISPLAY]	= 0;
		// responseByteCount[HCSPortCommunicator.CMDSETNETBITSTATE]		= 0;
		// responseByteCount[HCSPortCommunicator.CMDNETMODULEDISPLAY]	= 0;
		// responseByteCount[HCSPortCommunicator.CMDSETNETWORKSTRING]	= 0;
		// responseByteCount[HCSPortCommunicator.CMDSETTIME]			= 0;
		// responseByteCount[HCSPortCommunicator.CMDSETTIMEFREQUENCY]	= 0;
		// responseByteCount[HCSPortCommunicator.CMDSETVARIABLEVALUE]	= 0;
		// responseByteCount[HCSPortCommunicator.CMDSETVOICESTRING]		= 0;
		// responseByteCount[HCSPortCommunicator.CMDSETX10MODULE]		= 0;
		// responseByteCount[HCSPortCommunicator.CMDSETX10DISPLAY]		= 

		// inputByteCount[HCSPortCommunicator.CMDCLEARLOG]				= 0;
		inputByteCount[HCSPortCommunicator.CMDGETADCVALUE]				= 2;
		inputByteCount[HCSPortCommunicator.CMDGETDACVALUE]				= 2;
		inputByteCount[HCSPortCommunicator.CMDGETDIGINSTATUS]			= 1;
		inputByteCount[HCSPortCommunicator.CMDGETDIGOUTSTATUS]			= 1;
		// inputByteCount[HCSPortCommunicator.CMDGETLOG]				= ?;
		inputByteCount[HCSPortCommunicator.CMDGETLOGSIZE]				= 0;
		inputByteCount[HCSPortCommunicator.CMDGETNETMODULESTATUS]		= 1;
		inputByteCount[HCSPortCommunicator.CMDGETNETBITSTATUS]			= 2;
		// inputByteCount[HCSPortCommunicator.CMDGETTIME]				= 0;
		inputByteCount[HCSPortCommunicator.CMDGETX10STATUS]				= 1;
		inputByteCount[HCSPortCommunicator.CMDGETVARIABLEVALUE]			= 1;
		// inputByteCount[HCSPortCommunicator.CMDINFO]					= 0;
		// inputByteCount[HCSPortCommunicator.CMDLOAD]					= ?;
		// inputByteCount[HCSPortCommunicator.CMDPAUSE]					= 0;
		// inputByteCount[HCSPortCommunicator.CMDRESET]					= 0; 
		inputByteCount[HCSPortCommunicator.CMDSETADCDISPLAY]			= 3;
		inputByteCount[HCSPortCommunicator.CMDSETADCVALUE]				= 3;
		inputByteCount[HCSPortCommunicator.CMDSETDACDISPLAY]			= 1;
		inputByteCount[HCSPortCommunicator.CMDSETDACVALUE]				= 2;
		inputByteCount[HCSPortCommunicator.CMDSETDIGINDISPLAY]			= 4;
		inputByteCount[HCSPortCommunicator.CMDSETDIGINSTATE]			= 2;
		inputByteCount[HCSPortCommunicator.CMDSETDIGOUTDISPLAY]			= 4;
		inputByteCount[HCSPortCommunicator.CMDSETDIGOUTSTATE]			= 2;
		inputByteCount[HCSPortCommunicator.CMDSETLOG]					= 3;
		inputByteCount[HCSPortCommunicator.CMDSETNETMODULEDISPLAY]		= 1;
		inputByteCount[HCSPortCommunicator.CMDSETNETBITDISPLAY]			= 5;
		inputByteCount[HCSPortCommunicator.CMDSETNETBITSTATE]			= 3;
		// inputByteCount[HCSPortCommunicator.CMDSETNETWORKSTRING]		= ?;
		inputByteCount[HCSPortCommunicator.CMDSETTIME]					= 8;
		inputByteCount[HCSPortCommunicator.CMDSETTIMEFREQUENCY]			= 1;
		inputByteCount[HCSPortCommunicator.CMDSETVARIABLEVALUE]			= 3;
		// inputByteCount[HCSPortCommunicator.CMDSETVOICESTRING]		= ?;
		inputByteCount[HCSPortCommunicator.CMDSETX10MODULE]				= 3;
		inputByteCount[HCSPortCommunicator.CMDSETX10DISPLAY]			= 2;
	}

	public void setPort(HCSPort newPort)
	{
		port = (HCSSerialPort)newPort;
	}

	public byte[] sendCommand( byte cmd, byte[] data ) throws HCSException
	{
		if ( null != port )
		{
			switch (cmd)
			{
				case HCSPortCommunicator.CMDGETLOG:
					return getLog();
				case HCSPortCommunicator.CMDLOAD:
				{
					load(data);
					return null;
				}
				case HCSPortCommunicator.CMDINFO:
				case HCSPortCommunicator.CMDRESET:
				{
					if ( (data == null && 0 != inputByteCount[cmd] )
							|| (data != null && data.length != inputByteCount[cmd]) )
						throw new HCSException(HCSException.INVALIDARGUMENT);
					else
						doQuickCmd(cmd, data);
					return null;
				}
				case HCSPortCommunicator.CMDSETVOICESTRING:
				case HCSPortCommunicator.CMDSETNETWORKSTRING:
					return doCmd( cmd, data, responseByteCount[cmd]);
				default:
				{
					if ( (data == null && 0 != inputByteCount[cmd] ) ||
							(data != null && data.length != inputByteCount[cmd]) )
						throw new HCSException(HCSException.INVALIDARGUMENT);
					else
						return doCmd( cmd, data, responseByteCount[cmd] );
				}
			}
		}
		return null;
	}

	private byte[] getLog() throws HCSException
	{
		// We have to do special processing of the results so we send
		// a simple command first assuming we get no response.
		doQuickCmd(HCSPortCommunicator.CMDGETLOG, null);

		// Now we process the results. We will be getting back an
		// indeterminant number of bytes, terminated by 3 0xFF's.
		
		byte[] data = null;

		key = port.acquire();
		try
		{
			Vector log = new Vector();
			int count = 0;
			int b;
			while ( count != 3 )
			{
				b = port.read(key);
				log.addElement(new Byte((byte)b));
				if ( 0xFF == b ) count++;
				else count = 0;
			}

			// Now convert the Vector into an array of bytes.
			data = new byte[log.size()];
			for (int i = 0; i < log.size(); i++ )
			{
				data[i] = ((Byte)log.elementAt(i)).byteValue();
			}
		}
		finally
		{
			port.release(key);
		}

		return data;
	}

	private void load(byte[] buf) throws HCSException
	{
		key = port.acquire();
		try
		{
			// Alert the controller that we are about to attempt a load.
			port.write(key, (byte)ALERT);
			port.write(key, HCSPortCommunicator.CMDLOAD);

			// The controller will acknowledge the load when it has halted
			// the currently loaded program.
			int response;
			response = port.read(key);
			if ( response != ACKNOWLEDGE )
				throw new HCSException(HCSException.BADACK);

			// Write the file to the controller.
			port.write(key, buf);

			// The controller will respond appropriately.
			response = port.read(key);
			if ( response ==  ERRORMISMATCH )
				throw new HCSException(HCSException.BADFIRMWARE);
			else if ( response == ERRORTOOBIG )
				throw new HCSException(HCSException.PROGRAMSIZE);
			else if ( response != ACKNOWLEDGE )
				throw new HCSException(HCSException.LOADERROR,
					"Unknown error " + response + " loading program to controller");
		}
		finally
		{
			port.release(key);
		}
	}

	// Send a command to the controller and don't wait for a response.
	// currently only two commands use this, CMDRESET and CMDINFO,
	// neither of which has any data, but for future expandability
	// we include a data argument.
	private void doQuickCmd( byte cmd, byte[] data ) throws HCSException
	{
		// Ask the controller to stop sending updates.
		// Note: No need to disable the listener since we're not expecting
		// a response back.

		key = port.acquire();	// Acquire access to the port.
		try
		{
			// We send out ALERT cmd data, and expect nothing back.
			port.write(key, (byte)ALERT);
			port.write(key, cmd);
			if ( data != null ) port.write(key, data);
		}
		finally
		{
			port.release(key);
		}
	}

	// Send a command to the controller, and wait for a response.
	// The response may include data which we collect as well.
	private byte[] doCmd( byte cmd, byte[] data, int numBytes )
		throws HCSException
	{
		// We send out 'ALERT cmd data', and expect back 'RESPONSE cmd data'.
		// Either the output or input's data may not be present.
		byte[] response = null;

		key = port.acquire();
		try
		{
			port.write(key, (byte)ALERT);
			port.write(key, cmd);
			if ( data != null ) port.write(key, data);

			// We assume that no other data is coming back from the controller
			// but what we expect. (Because the listener has released the port
			// so no updates are being sent.)
			if ( port.read(key) != RESPONSE )
				throw new HCSException(HCSException.BADRESPONSE);
			if ( port.read(key) != (int)cmd )
				throw new HCSException(HCSException.BADCOMMAND);

			if ( numBytes > 0 )
			{
				response = new byte[numBytes];
				port.read(key, response);
			}
		}
		finally
		{
			port.release(key);
		}

		return response;
	}
}

