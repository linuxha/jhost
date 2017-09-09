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

public interface HCSPortCommunicator
{
	// Note that all data in and out of this class should be formatted
	// as required by the controller. No conversions are done when data
	// is sent to or received from he controller.

	// Message headers.
	public static final int ALERT						= '!';
	public static final int RESPONSE					= '$';

	// Responses from the HCS controller.
	public static final int ACKNOWLEDGE				= '^';
	public static final int ERRORMISMATCH				= '#';
	public static final int ERRORTOOBIG				= '@';

	// Commands sent to the HCS (and returned in responses).
	public static final byte CMDRESET					= 0x00;
	public static final byte CMDPAUSE					= 0x01;
	public static final byte CMDINFO					= 0x02;
	public static final byte CMDSETTIMEFREQUENCY		= 0x03;
	public static final byte CMDGETTIME				= 0x04;
	public static final byte CMDSETTIME				= 0x05;
	public static final byte CMDLOAD					= 0x06;
	public static final byte CMDCLEARLOG				= 0x07;
	public static final byte CMDGETLOGSIZE				= 0x08;
	public static final byte CMDGETLOG					= 0x09;
	public static final byte CMDSETLOG					= 0x0A;
	public static final byte CMDSETX10DISPLAY			= 0x10;
	public static final byte CMDGETX10STATUS			= 0x11;
	public static final byte CMDSETX10MODULE			= 0x12;
	public static final byte CMDSETDIGINDISPLAY		= 0x13;
	public static final byte CMDGETDIGINSTATUS			= 0x14;
	public static final byte CMDSETDIGINSTATE			= 0x15;
	public static final byte CMDSETDIGOUTDISPLAY		= 0x16;
	public static final byte CMDGETDIGOUTSTATUS		= 0x17;
	public static final byte CMDSETDIGOUTSTATE			= 0x18;
	public static final byte CMDSETADCDISPLAY			= 0x19;
	public static final byte CMDGETADCVALUE			= 0x1A;
	public static final byte CMDSETADCVALUE			= 0x1B;
	public static final byte CMDSETDACDISPLAY			= 0x1C;
	public static final byte CMDGETDACVALUE			= 0x1D;
	public static final byte CMDSETDACVALUE			= 0x1E;
	public static final byte CMDSETNETMODULEDISPLAY	= 0x1F;
	public static final byte CMDGETNETMODULESTATUS		= 0x20;
	public static final byte CMDSETNETBITDISPLAY		= 0x21;
	public static final byte CMDGETNETBITSTATUS		= 0x22;
	public static final byte CMDSETNETBITSTATE			= 0x23;
	public static final byte CMDGETVARIABLEVALUE		= 0x24;
	public static final byte CMDSETVARIABLEVALUE		= 0x25;
	public static final byte CMDSETNETWORKSTRING		= 0x30;
	public static final byte CMDSETVOICESTRING			= 0x31;
	public static final byte MAXCMD						= 0x31;

	// Responses from the HCS.
	public static final int DATATIME					= 0x80;
	public static final int DATAX10MODULES				= 0x81;
	public static final int DATADIGITALINPUTS			= 0x82;
	public static final int DATADIGITALOUTPUTS			= 0x83;
	public static final int DATAANALOGINPUTS			= 0x84;
	public static final int DATAANALOGOUTPUTS			= 0x85;
	public static final int DATANETWORKMODULES			= 0x86;
	public static final int DATANETBITS				= 0x87;
	public static final int DATACONSOLEMESSAGE			= 0x88;

	// Events for communicating with the UI Object.
	public static final int NEWTIME						= 1;
	public static final int NEWX10MODULES				= 2;
	public static final int NEWDIGITALINPUTS			= 3;
	public static final int NEWDIGITALOUTPUTS			= 4;
	public static final int NEWANALOGINPUTS				= 5;
	public static final int NEWANALOGOUTPUTS			= 6;
	public static final int NEWNETWORKMODULES			= 7;
	public static final int NEWNETBITS					= 8;
	public static final int NEWCONSOLEMESSAGE			= 9;

	public void setPort(HCSPort newPort);
}

