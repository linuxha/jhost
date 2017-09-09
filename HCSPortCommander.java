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

// This is a simple interface which is implemented by classes which
// sends commands to HCSPorts. The sendCommand() method is called whenever
// some data should be sent to the port. HCSPortCommander classes must be
// matched with an appropriate HCSPort class.
public interface HCSPortCommander
{
	public byte[] sendCommand(byte cmd, byte[] data) throws HCSException;
	public void setPort(HCSPort port);
}

