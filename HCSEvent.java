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

// A simple class which is used in communicating port events.

public class HCSEvent
{
	public static final int ALL						= 0;
	public static final int TIME					= 1;
	public static final int X10MODULES				= 2;
	public static final int DIGITALINPUTS			= 3;
	public static final int DIGITALOUTPUTS			= 4;
	public static final int ANALOGINPUTS			= 5;
	public static final int ANALOGOUTPUTS			= 6;
	public static final int NETWORKMODULES			= 7;
	public static final int NETBITS					= 8;
	public static final int CONSOLEMESSAGE			= 9;
	// public static final int OVERFLOW				= 10;
	public static final int UNKNOWN					= 10;
	public static final int BADREAD					= 11;
	// public static final int BADWRITE				= 13;
	public static final int INPUTOVERRUN			= 12;

	// NUMEVENTTYPES is one greater than the last event type.
	public static final int NUMEVENTTYPES			= 13;

	// A flag to indicate the data is unmodified as returned by the port.
	public static final int RAW						= 0x8000;

	public HCSEvent(int type, byte[] data)
	{
		this.type = type | RAW;	// Mark the data as being unmodified.
		this.rawdata = data;
	};

	public int type;
	public byte[] rawdata;

	// Need two types. One which is the massaged type and set to
	// RAW initially, and the second is the detailed event.

	// Can also reuse these objects. This will save memory and reduce
	// fragmentation. When the thread returns from the user code it can
	// mark the event as unused.
	public int year, month, day, dow, hour, minute, second, millisecond;	// TIME
	public int module, unit, state;
	public int[] values;
	public String message;	// CONSOLEMESSAGE
}
