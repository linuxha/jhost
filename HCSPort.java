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

// HCSPort is a base class from which actual port classes are built.
// For example HCSSerialPort is derived from HCSPort and implements
// access to a serial port.
import java.util.*;

public abstract class HCSPort
{
	public HCSPortListener listener = null;

	protected HCSPort(HCSPortListener newListener)
	{
		setPortListener(newListener);
	}

	public void setPortListener(HCSPortListener newListener)
	{
		listener = newListener;
	}

	protected HCSPortListener getPortListener()
	{
		return listener;
	}

	public abstract void open(String name) throws HCSException;
	public abstract void write(byte[] buf) throws HCSException;
	public abstract void write(byte chr) throws HCSException;
	public abstract byte[] read(int num) throws HCSException;
	public abstract void read(byte[] buf) throws HCSException;
	public abstract int read() throws HCSException;
	public abstract void setListenerState(boolean state);
}
