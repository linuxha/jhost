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

// This is the entry class for the java host application. It initializes
// the port manager (the class that sets up and communicates with whatever
// port the HCS II controller is attached to). It then loads the UI class
// specified on the command line, then instantiates it. This class is
// undergoing extensive enhancements between the beta releases.
import java.lang.reflect.*;

public class javahost
{
	public static int VERSION = 0;
	public static int REVISION = 92;

	public static void main(String args[])
	{
		boolean useDefaultUI = (args.length == 0 ||
		   (args.length > 0 && args[0].equals("-d")));

		if (args.length == 1 && !useDefaultUI)
		{
			System.out.println("\nUsage: javahost [-d] [dir UI [UI]*]");
			System.out.println("\t-d  - load the default UI");
			System.out.println("\tdir - directory in which UIs are located");
			System.out.println("\tUI  - the name of a UI class to load");
			System.exit(0);
		}
		else if (!useDefaultUI)
		{
			// If the user is using their own UI, then output this otherwise
			// they will see the UI's version.
			System.out.println("\nHCS II Java Host, version " + VERSION + "." +
				REVISION + " (BETA)");
			System.out.println(" Copyright (c) 2001, Paul Tokarchuk");
			System.out.println(" This software comes with ABSOLUTELY NO" +
				" WARRANTY.");
			System.out.println(" See the included LICENSE file for details.");
		}

		HCSPortManager portManager = null;
		try { portManager = new HCSPortManager(null); }
		catch (HCSException e)
		{
			System.out.println("Initialization failed because \"" +
				e.getMessage() + "\"");
			return;
		}

		HCSSwingGUI ui = null;
		if (useDefaultUI)
			ui = new HCSSwingGUI(portManager);
		if (args.length > 0)
		{
			// Load the user's UI objects.
			int startIndex = (args[0].equals("-d") ? 1 : 0);
			for (int i = startIndex+1; i < args.length; i++)
				loadUI(portManager, args[startIndex], args[i]);
		}
	}

	private static void loadUI(HCSPortManager portManager, String dirName,
		String className)
	{
		// Load the class specified.
		try
		{
			portManager.loadUI(dirName, className);
		}
		catch (HCSException e)
		{
			System.out.println("Failed to load " + className + ". (" +
				e.getMessage() + ")");
			return;
		}
		catch (NoSuchMethodException e)
		{
			System.out.println(className + ": Failed to find UI constructor. ("
				+ e.getMessage() + ")");
			return;
		}
		catch (InstantiationException e)
		{
			System.out.println(className +
				": Failed to instantiate UI constructor. (" + e.getMessage() +
				")");
			return;
		}
		catch (IllegalAccessException f)
		{
			System.out.println(className +
				": Access to UI constructor is priviledged. (" +
				f.getMessage() + ")");
			return;
		}
		catch (InvocationTargetException g)
		{
			Throwable h = g.getTargetException();
			System.out.println(className +
				": UI constructor threw an exception. (" +
				h.getMessage() + ")");
			h.printStackTrace();
			return;
		}
	}
}

