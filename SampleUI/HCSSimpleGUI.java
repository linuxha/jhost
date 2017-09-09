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

// This class implements a simple GUI which displays four lights in
// a house. The image displayed is a simple rendering of a four room
// house with lights in each room which respond to X10 events.
// Much of what's in this file is hardcoded, but can easily be turned
// into a much more effective GUI. Enhancements include:
// - Creating a Light class to better encapsulate the lights.
// - Changing the Preferences dialog bos to allow a user to set the
//   houseCode and modules for each light.
// - Monitor and display other events (motion sensors, doors, windows, etc.)
// - Allow user to customize the floorplan of the house and the location
//   of devices.
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;

public class HCSSimpleGUI extends Frame
{
	// Menu labels.
	private static final String FILEMENULABEL = "File";
	private static final String EDITMENULABEL = "Edit";
	private static final String COMMANDSMENULABEL = "Commands";

	// Menu Item Labels
	private static final String EXITLABEL = "Exit";
	private static final String LOADLABEL = "Load";
	private static final String PREFERENCESLABEL = "Preferences...";
	private static final String SETTIMELABEL = "Set Time";

	private static final int windowWidth = 400;
	private static final int windowHeight = 400;

	// The root title of the application window.
	private static final String title = "HCS II Java Host - ";

	//
	// Instance variables.
	//

	// Which port have we opened?
	private String portName = "no port";

	// The Object with which we communicate with the port.
	private HCSPortManager portManager = null;

	// Lights can be generalized by creating a Light class which encompasss
	// the attributes of a light (location, state, houseCode, module, etc.).
	private static final int KITCHEN = 0;
	private static final int FAMILYROOM = 1;
	private static final int OFFICE = 2;
	private static final int BEDROOM = 3;

	private boolean[] lightIsOn = new boolean[] { false, false, false, false };
	private final int houseCode = 'A';
	private final int[] modules = new int[] { 4, 5, 6, 7 }; // X10 light modules

	public HCSSimpleGUI(HCSPortManager portManager)
	{
		super();

		this.portManager = portManager;

		setTitle(title + portName);
		setSize(windowWidth, windowHeight);

		// Make a top level menues.
		FileMenu fileMenu = new FileMenu();
		EditMenu editMenu = new EditMenu();
		CommandsMenu commandsMenu = new CommandsMenu();

		// Make a menu bar for this frame 
		// and add top level menues File and Edit.
		MenuBar mb = new MenuBar();
		mb.add(fileMenu);
		mb.add(editMenu);
		mb.add(commandsMenu);
		setMenuBar(mb);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});

		portManager.addEventListener(HCSEvent.X10MODULES, new HCSEventListener() {
			public void portEvent(HCSEvent event) {
				doX10(event);
			}
		});

		portManager.addEventListener(HCSEvent.TIME, new HCSEventListener() {
			public void portEvent(HCSEvent event) {
				doTime(event);
			}
		});

		show();
	}

	// Draw the house. Note that there are a lot of magic numbers in this
	// function. If this were a real UI these numbers should be replaced by
	// properly calculated values.
	public void paint(Graphics g)
	{
		Dimension d = getSize();
		final int xOffset = 50;	// Offset from the window edges to the
		final int yOffset = 50;	// building walls.
		final int width = Math.max(d.width, 4*xOffset) - 2*xOffset;
		final int height = Math.max(d.height, 4*yOffset) - 2*yOffset;
		final int xMax = xOffset + width;
		final int yMax = yOffset + height;
		final int halfWidth = width/2;
		final int halfHeight = height/2;
		final int xMid = xOffset + halfWidth;
		final int yMid = yOffset + halfHeight;
		final int radius = 10;

		// Draw a rectangle representing the walls of a house.
		g.setColor(Color.black);
		g.drawRect(xOffset, yOffset, width, height);

		// Draw the interior walls.
		g.drawLine(xOffset, yMid, xMax, yMid);
		g.drawLine(xMid, yOffset, xMid, yMax);

		// Label the rooms. (Note, the fudge factor (10) should really
		// be replaced by the height of the font, but then this is
		// just an example UI and something has to be left for the
		// reader to do.)
		g.setClip(xOffset, yOffset, halfWidth, halfHeight);
		g.drawString("Kitchen", xOffset + 10, yOffset + 10);
		g.setClip(xMid, yMid, halfWidth, halfHeight);
		g.drawString("Bedroom", xMid + 10, yMid + 10);
		g.setClip(xOffset, yMid, halfWidth, halfHeight);
		g.drawString("Office", xOffset + 10, yMid + 10);
		g.setClip(xMid, yOffset, halfWidth, halfHeight);
		g.drawString("Family Room", xMid + 10, yOffset + 10);
		g.setClip(0, 0, d.width, d.height);	// Disable the clip.

		// Draw the ceiling light. On or off depends on X10 control.
		g.setColor(lightIsOn[KITCHEN] ? Color.yellow : Color.yellow.darker());
		g.fillOval(xOffset+halfWidth/2-radius, yOffset+halfHeight/2-radius,
			2*radius, 2*radius);
		g.setColor(lightIsOn[BEDROOM] ? Color.yellow : Color.yellow.darker());
		g.fillOval(xMid+halfWidth/2-radius, yMid+halfHeight/2-radius,
			2*radius, 2*radius);
		g.setColor(lightIsOn[OFFICE] ? Color.yellow : Color.yellow.darker());
		g.fillOval(xOffset+halfWidth/2-radius, yMid+halfHeight/2-radius,
			2*radius, 2*radius);
		g.setColor(lightIsOn[FAMILYROOM]?Color.yellow : Color.yellow.darker());
		g.fillOval(xMid+halfWidth/2-radius, yOffset+halfHeight/2-radius,
			2*radius, 2*radius);
	}

	private void doX10(HCSEvent event)
	{
		if ( event.unit == houseCode )
		{
			boolean somethingChanged = false;
			for (int i = 0; i < 4; i++)
			{
				int houseUnit = 1<<(modules[i]-1);
				boolean state = ((event.state & houseUnit) == houseUnit);
				if ( state != lightIsOn[i] )
				{
					lightIsOn[i] = state;
					somethingChanged = true;
				}
			}

			repaint();
		}
	}

	private void doTime(HCSEvent event)
	{
		Calendar cal = Calendar.getInstance();
		cal.set( event.year, event.month, event.day, event.hour,
			event.minute, event.second );

		setTitle(title + portName + " - " +
			DateFormat.getDateTimeInstance().format(cal.getTime()));
	}

	private void exit()
	{
		setVisible(false);	// hide the Frame
		dispose();			// tell windowing system to free resources
		System.exit(0);		// exit
	}

	private void setPort(String newPortName)
	{
		if ( !portName.equals(newPortName) )
		{
			// The user has selected a new port.
			portName = newPortName;
			setTitle(title + portName);

			try
			{
				// Open the port to the controller.
				portManager.openPort(portName);

				// Request all information from the controller. This allows
				// us to set up initial states.
				portManager.getInfo();
			}
			catch (HCSException e)
			{
				portName = "no port";
			}
		}
	}

	// Encapsulate the look and behaviour of the File menu
	private class FileMenu extends Menu implements ActionListener
	{
		public FileMenu()
		{
			super(FILEMENULABEL);

			MenuItem mi;
			add(mi = new MenuItem(EXITLABEL));
			mi.addActionListener(this);
		}

		// respond to menu item selections
		public void actionPerformed(ActionEvent e)
		{
			String item = e.getActionCommand();
			if (item.equals(EXITLABEL)) 
				exit();
			else 
				System.out.println("Selected FileMenu " + item);
		}
	}

	// Encapsulate the look and behaviour of the Edit menu
	private class EditMenu extends Menu implements ActionListener
	{
		private Dialog dialogBox;
		private Choice choiceList;
		private	String selectedPort;

		public EditMenu()
		{
			super(EDITMENULABEL);

			MenuItem mi;
			add(mi = new MenuItem(PREFERENCESLABEL));
			mi.addActionListener(this);
		}

		// respond to a few menu items
		public void actionPerformed(ActionEvent e)
		{
			String item = e.getActionCommand();
			if (item.equals(PREFERENCESLABEL))
				preferencesDialog();
		}

		// A simple enhancement to this example which would make it far more
		// useful would be to enhance the dialog box to allow users to
		// set the house code and unit for the lights as well as for the
		// motion sensors and the digital inputs.
		private void preferencesDialog()
		{
			choiceList = new Choice();
			Vector portList = portManager.getPortList();
			for (int i = 0; i < portList.size(); i++)
				choiceList.add((String)portList.elementAt(i));

			// Search for the portName in the list.
			int index = 0;
			if ( portName != null )
			{
				for ( int i = 0; i < choiceList.getItemCount(); i++ )
				{
					if ( portName.equals( choiceList.getItem(i) ) )
					{
						index = i;
						break;
					}
				}
			}

			// We choose the first if one isn't set or we can't find the
			// one previously selected.
			choiceList.select(index);
			selectedPort = choiceList.getItem(index);

			// Create a main panel for all the dialog box components.
			Panel p = new Panel();
			p.setLayout(new FlowLayout(FlowLayout.RIGHT));

			// Create a panel containing the OK and Cancel buttons.
			Panel p2 = new Panel();
			Button okButton, cancelButton;

			p2.setLayout(new GridLayout(1,0,5,5));
			p2.add(okButton = new Button("OK"));
			p2.add(cancelButton = new Button("Cancel"));

			// Add the buttons to the main panel.
			p.add(p2, BorderLayout.EAST);

			// Create a panel containing the Port lister.
			Panel p3 = new Panel();
			p3.setLayout(new BorderLayout());
			p3.add(choiceList, BorderLayout.CENTER);
			p3.add(new Label("Port:"), BorderLayout.NORTH);

			// Create the dialog box and add the panels to it.
			dialogBox = new Dialog(HCSSimpleGUI.this, PREFERENCESLABEL, true);
			dialogBox.setSize(150,75);
			dialogBox.setResizable(true);
			dialogBox.setLayout(new BorderLayout());
			dialogBox.add(p, BorderLayout.SOUTH);
			dialogBox.add(p3, BorderLayout.CENTER);

			okButton.addActionListener( new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						dialogBox.setVisible(false);
						setPort(selectedPort);
					}
				});
			cancelButton.addActionListener( new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						dialogBox.setVisible(false);
					}
				});
			choiceList.addItemListener( new ItemListener()
				{
					public void itemStateChanged(ItemEvent e)
					{
						if (e.getStateChange() == ItemEvent.SELECTED)
						{
							selectedPort = choiceList.getSelectedItem();
						}
					}
				});

			dialogBox.pack();
			dialogBox.setVisible(true);

			dialogBox.dispose();
		}
	}

	// Encapsulate the look and behaviour of the Commands menu
	private class CommandsMenu extends Menu implements ActionListener
	{
		public CommandsMenu()
		{
			super(COMMANDSMENULABEL);

			MenuItem mi;
			add(mi = new MenuItem(LOADLABEL));
			mi.addActionListener(this);
			add(mi = new MenuItem(SETTIMELABEL));
			mi.addActionListener(this);
		}

		// respond to menu item selections
		public void actionPerformed(ActionEvent event)
		{
			String item = event.getActionCommand();
			if (item.equals(LOADLABEL))
			{
				try { portManager.load("events.bin"); }
				catch (HCSException e) { System.out.println("Load failed."); }
			}
			else if (item.equals(SETTIMELABEL))
			{
				try { portManager.setTime(); }
				catch (HCSException e) { System.out.println("SetTime failed."); }
			}
			else 
				System.out.println("Selected CommandsMenu " + item);
		}
	}
}

