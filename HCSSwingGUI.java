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

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.io.*;
import java.beans.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

public class HCSSwingGUI
{
	protected static final String PROPFILENAME = "hcs.properies";
	protected static final String PROPHEADER   = "Properties for HCSSwingGUI";
	protected static final String SOURCEFILE = "SOURCEFILE";
	protected static final String COMPILERLOCATION = "COMPILERLOCATION";
	protected static final String PORT = "PORT";
	protected static final String CWINDOW_POSX = "COMPILER.WINDOW.X";
	protected static final String CWINDOW_POSY = "COMPILER.WINDOW.Y";
	protected static final String CWINDOW_WIDTH = "COMPILER.WINDOW.WIDTH";
	protected static final String CWINDOW_HEIGHT = "COMPILER.WINDOW.HEIGHT";
	protected static final String CAREA_ROWS = "COMPILER.WINDOW.CODEAREA.ROWS";
	protected static final String CAREA_COLS = "COMPILER.WINDOW.CODEAREA.COLUMNS";
	protected static final String MAREA_ROWS = "COMPILER.WINDOW.MESSAGEAREA.ROWS";
	protected static final String MAREA_COLS = "COMPILER.WINDOW.MESSAGEAREA.COLUMNS";
	protected static final String DIVIDER_LOC = "COMPILER.WINDOW.DIVIDER.LOCATION";
	protected static final String CWINDOW_VIS = "COMPILER.WINDOW.VISIBILITY";
	protected static final String CONSOLE_POSX = "CONSOLE.X";
	protected static final String CONSOLE_POSY = "CONSOLE.Y";
	protected static final String CONSOLE_WIDTH = "CONSOLE.WIDTH";
	protected static final String CONSOLE_HEIGHT = "CONSOLE.HEIGHT";
	protected static final String CONSOLE_TIME = "CONSOLE.TIME";
	protected static final String X10WINDOW_VIS = "X10WINDOW.VISIBILITY";
	protected static final String X10WINDOW_POSX = "X10WINDOW.X";
	protected static final String X10WINDOW_POSY = "X10WINDOW.Y";
	protected static final String X10WINDOW_WIDTH = "X10WINDOW.WIDTH";
	protected static final String X10WINDOW_HEIGHT = "X10WINDOW.HEIGHT";
	protected static final String X10HOUSECODES = "X10CODES";
	protected static final String NETWORKMODULES_VIS = "NETWORKMODULES.VISIBILITY";
	protected static final String NETWORKMODULES_POSX = "NETWORKMODULES.X";
	protected static final String NETWORKMODULES_POSY = "NETWORKMODULES.Y";
	protected static final String NETWORKMODULES_WIDTH = "NETWORKMODULES.WIDTH";
	protected static final String NETWORKMODULES_HEIGHT = "NETWORKMODULES.HEIGHT";
	protected static final String NETWORKMODULES = "NETWORKMODULES";
	protected static final String PREFS_POSX = "PREFS.X";
	protected static final String PREFS_POSY = "PREFS.Y";
	protected static final String PREFS_WIDTH = "PREFS.WIDTH";
	protected static final String PREFS_HEIGHT = "PREFS.HEIGHT";
	protected static final String COMMAND_POSX = "COMMAND.X";
	protected static final String COMMAND_POSY = "COMMAND.Y";
	protected static final String COMMAND_WIDTH = "COMMAND.WIDTH";
	protected static final String COMMAND_HEIGHT = "COMMAND.HEIGHT";
	protected static final String UI_FILE = "UI.FILE";

	protected static final String AtoP = "ABCDEFGHIJKLMNOP";
	protected static final String NOPORT = "No Port Selected";
	protected static final String TITLEBASE = "JavaHost - ";
	protected static final String DEFAULTDIGINOUT = "11111111111111111111111111111111";
	protected static final String DEFAULTNETBITS = "1111111111111111111111111111111111111111";

	protected HCSPortManager hcs = null;
	protected String port = NOPORT;
	protected HCSProperties props = new HCSProperties();

	protected JFileChooser fileChooser = new JFileChooser();
	protected XpressFilter xpressFilter = new XpressFilter();
	protected ClassFilter classFilter = new ClassFilter();


	// Windows.
	protected X10Window x10Window = null;
	protected CompilerWindow compilerWindow = null;
	protected PreferencesWindow prefsWindow = null;
	protected ConsoleWindow consoleWindow = null;
	protected CommandWindow commandWindow = null;
	protected DigitalInputWindow digitalInputWindow = null;
	protected DigitalOutputWindow digitalOutputWindow = null;
	protected NetbitsWindow netbitsWindow = null;
	protected AnalogInputWindow analogInputWindow = null;
	protected AnalogOutputWindow analogOutputWindow = null;
	protected NetworkModulesWindow networkModulesWindow = null;

	private boolean initializing = true;

	public HCSSwingGUI(HCSPortManager pm)
	{
		hcs = pm;

		AboutWindow aboutWindow = new AboutWindow();

		// Load the properties file.
		File propsFile = new File(PROPFILENAME);
		if ( propsFile.exists() && propsFile.canRead() )
		{
			try
			{
				FileInputStream fis = new FileInputStream(propsFile);
				props.load(fis);
				fis.close();
			}
			// These two exceptions should not happen since we already
			// checked for the existence and ability to read the property
			// file. So we output an internal error if they do occur.
			catch (FileNotFoundException e)
			{
				System.out.println("Internal Error: Failed to find " +
					PROPFILENAME + ".");
			}
			catch (IOException e)
			{
				System.out.println("Internal Error: Failed to load "
					+ PROPFILENAME + ".");
			}
		}

		// Open the port.
		try
		{
			openPort(props.getProperty(PORT, NOPORT));
		}
		catch (HCSException e)
		{
			Error("Failed to open port.\n" + e.getMessage());
		}

		initializing = true;

		if (props.getProperty(X10WINDOW_VIS, "false").equals("true"))
			x10Window = new X10Window();

		if (props.getProperty(CWINDOW_VIS, "false").equals("true"))
			compilerWindow = new CompilerWindow();

		if (props.getProperty("DIGITALINPUT.VISIBILITY", "false").equals("true"))
			digitalInputWindow = new DigitalInputWindow();

		if (props.getProperty("DIGITALOUTPUT.VISIBILITY", "false").equals("true"))
			digitalOutputWindow = new DigitalOutputWindow();

		if (props.getProperty("NETBITS.VISIBILITY", "false").equals("true"))
			netbitsWindow = new NetbitsWindow();

		if (props.getProperty("ANALOGINPUT.VISIBILITY", "false").equals("true"))
			analogInputWindow = new AnalogInputWindow();

		if (props.getProperty("ANALOGOUTPUT.VISIBILITY", "false").equals("true"))
			analogOutputWindow = new AnalogOutputWindow();

		if (props.getProperty(NETWORKMODULES_VIS, "false").equals("true"))
			networkModulesWindow = new NetworkModulesWindow();

		// Open the console window last so that it gets focus.
		consoleWindow = new ConsoleWindow();

		fileChooser.addChoosableFileFilter(xpressFilter);
		fileChooser.addChoosableFileFilter(classFilter);

		initializing = false;

		// Start things off by getting the current state.
		if (!port.equals(NOPORT))
			getInfo();

		// Really I'd like the aboutWindow to close through the use of a
		// Timer, but I get a compiler error when I use Timers, so I can't
		// do that. Instead I close the about window when all other windows
		// have finished rendering.
		aboutWindow.setVisible(false);
		aboutWindow.dispose();
		aboutWindow = null;
	}

	protected void getInfo()
	{
		// Whenever a window is made visible we need to get its state. The best
		// way to do this is to request a full information dump from the
		// controller. However we don't want to be doing this for every window
		// during initialization, only when the window is opened manually.
		if (initializing) return;

		try
		{
			hcs.getInfo();
		}
		catch (HCSException e)
		{
			Error("Failed to get update from\nthe controller.\n" +
				e.getMessage());
		}

	}

	protected void openPort(String newPort) throws HCSException
	{
		if (newPort.equals(port) || newPort.equals(NOPORT))
			return;

		port = newPort;
		hcs.openPort(port);
		props.put(PORT, port);
	}

	protected class HCSProperties extends Properties
	{
		public void save()
		{
			try
			{
				FileOutputStream fos =
					new FileOutputStream(new File(PROPFILENAME));
				props.save(fos, PROPHEADER);
				fos.close();
			}
			catch (IOException e)
			{
				Error("Unable to save " + PROPFILENAME + ".");
			}
		}

		public int getProperty(String key, int val)
		{
			String property = props.getProperty(key);
			if (property != null)
				return parseInt(property);
			else
				return val;
		}

		public void put(String key, int val)
		{
			props.put(key, Integer.toString(val));
		}

	}

	private class ClassFilter extends FileFilter
	{
		public String getDescription()
		{
			return "Class Files (*.class)";
		}

		public boolean accept(File f)
		{
			if (f.isDirectory()) return true;

			String ext = null;
			String s = f.getName();
			int i = s.lastIndexOf('.');
			if (i > 0 && i < s.length()-1)
				ext = s.substring(i+1).toLowerCase();

			if (ext != null && ext.equals("class"))
				return true;
			return false;
		}
	}

	private class XpressFilter extends FileFilter
	{
		public String getDescription()
		{
			return "XPRESS Files (*.hcs)";
		}

		public boolean accept(File f)
		{
			if (f.isDirectory()) return true;

			String ext = null;
			String s = f.getName();
			int i = s.lastIndexOf('.');
			if (i > 0 && i < s.length()-1)
				ext = s.substring(i+1).toLowerCase();

			if (ext != null && ext.equals("hcs"))
				return true;
			return false;
		}
	}

	private class StatusWindow extends JDialog
	{
		public StatusWindow(String title, String message, int type)
		{
			final JOptionPane optionPane = new JOptionPane(message,
				type, JOptionPane.DEFAULT_OPTION);

			setTitle(title);
			setContentPane(optionPane);
			setModal(false);

			optionPane.addPropertyChangeListener( new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent e)
				{
					String prop = e.getPropertyName();
					if (isVisible()
						&& e.getSource() == optionPane
						&& (prop.equals(JOptionPane.VALUE_PROPERTY)
						 || prop.equals(JOptionPane.INPUT_VALUE_PROPERTY)))
					{
						setVisible(false);
						dispose();
					}
				}
			});

			pack();
			setVisible(true);
		}
	}

	private class ErrorWindow extends StatusWindow
	{
		public ErrorWindow(String msg)
		{
			super("Error", msg, JOptionPane.ERROR_MESSAGE);
		}
	}

	private void Error(final String msg)
	{
		// This function can be called during an ActionEvent so we put up
		// the error window when it is next convenient.
		Runnable update = new Runnable() {
			public void run()
			{
				ErrorWindow w = new ErrorWindow(msg);
				Toolkit.getDefaultToolkit().beep();
			}
		};
		SwingUtilities.invokeLater(update);
	}

	private class AboutWindow extends StatusWindow
	{
		public AboutWindow()
		{
			super("About...",
			"HCS II Java Host, version " + javahost.VERSION + "." +
			javahost.REVISION + " (BETA)\n" +
			"Copyright (c) 2001, Paul Tokarchuk\n" +
			" This software comes with ABSOLUTELY NO\n" +
			" WARRANTY. See the included LICENSE file\n" +
			" for details.", JOptionPane.PLAIN_MESSAGE);
		}
	}

	private class CompilerWindow extends JFrame
	{
		// This will maintain a reference to the main() for the compiler so
		// that we can invoke it from this code. As well as allow the UI to
		// register callbacks for displaying compiler output and error
		// messages.
		protected Method compile = null;
		protected Method registerOutput = null;
		protected Method registerError = null;

		protected JTextArea codeArea = null;
		protected JTextArea messagesArea = null;
		protected PrintWriter writer = new PrintWriter(new CompilerWriter());

		public CompilerWindow()
		{
			// Create a frame into which the Compiler UI will be placed.
			setTitle("Compiler Window");
		
			// Allow the frame to respond to window close events.
			addWindowListener( new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					setVisible(false);
				}
			});

			// Build the menues.
			JMenuBar menuBar = new JMenuBar();
			setJMenuBar(menuBar);
			JMenu menu = new JMenu("Compiler");
			menu.setMnemonic('c');
			menu.getAccessibleContext().setAccessibleDescription(
				"Compiler options");
			menuBar.add(menu);

			OpenListener openListener = new OpenListener();
			JMenuItem menuItem = new JMenuItem("Open...", KeyEvent.VK_O);
			menuItem.getAccessibleContext().setAccessibleDescription(
				"Open a file for compiling.");
			menuItem.setActionCommand("Open");
			menuItem.addActionListener(openListener);
			menu.add(menuItem);

			SaveListener saveListener = new SaveListener();
			menuItem = new JMenuItem("Save", KeyEvent.VK_S);
			menuItem.getAccessibleContext().setAccessibleDescription(
				"Save an edited file.");
			menuItem.setActionCommand("Save");
			menuItem.addActionListener(saveListener);
			menu.add(menuItem);

			menuItem = new JMenuItem("Save As...", KeyEvent.VK_A);
			menuItem.getAccessibleContext().setAccessibleDescription(
				"Save an editted file.");
			menuItem.setActionCommand("Save as");
			menuItem.addActionListener(saveListener);
			menu.add(menuItem);

			CompileListener compileListener = new CompileListener();
			menuItem = new JMenuItem("Compile", KeyEvent.VK_C);
			menuItem.getAccessibleContext().setAccessibleDescription(
				"Compile the opened file.");
			menuItem.setActionCommand("Compile");
			menuItem.addActionListener(compileListener);
			menu.add(menuItem);

			// Create the text areas with a default size.
			codeArea = new JTextArea(20, 60);
			messagesArea = new JTextArea(8, 60);
			messagesArea.setEditable(false);

			JScrollPane codeScrollPane = new JScrollPane(codeArea);
			JScrollPane messagesScrollPane = new JScrollPane(messagesArea);

			final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				codeScrollPane, messagesScrollPane);
			splitPane.setOneTouchExpandable(true);

			Container pane = getContentPane();
			pane.add(splitPane);

			pack();

			// Change the size if necessary. And set the location while we're
			// here.
			codeArea.setRows(props.getProperty(CAREA_ROWS, 20));
			codeArea.setColumns(props.getProperty(CAREA_COLS, 60));
			codeArea.setTabSize(4);	// Doesn't seem to work.

			messagesArea.setRows(props.getProperty(MAREA_ROWS, 8));
			messagesArea.setColumns(props.getProperty(MAREA_COLS, 60));

			// If we've not saved the divider location, use the one that's
			// calculated for us.
			int tmp = props.getProperty(DIVIDER_LOC, 0);
			if (tmp != 0) splitPane.setDividerLocation(tmp);

			setLocation(
				props.getProperty(CWINDOW_POSX, 0),
				props.getProperty(CWINDOW_POSY, 0));

			// If either dimension is 0, then use the calculated values.
			int x = props.getProperty(CWINDOW_WIDTH, 0);
			int y = props.getProperty(CWINDOW_HEIGHT, 0);
			if (x != 0 && y != 0) setSize( x, y );

			// Listen for resize and repositioning events so that we can
			// save this information for the next time the application is
			// run.
			addComponentListener( new ComponentListener() {
				public void componentMoved(ComponentEvent e) {
					Point pos = getLocation();
					props.put(CWINDOW_POSX, pos.x);
					props.put(CWINDOW_POSY, pos.y);
					props.put(DIVIDER_LOC, splitPane.getDividerLocation());
				}
				public void componentResized(ComponentEvent e) {
					Dimension size = getSize();
					props.put(CWINDOW_WIDTH, size.width);
					props.put(CWINDOW_HEIGHT, size.height);
					props.put(CAREA_ROWS, codeArea.getRows());
					props.put(CAREA_COLS, codeArea.getColumns());
					props.put(MAREA_ROWS, messagesArea.getRows());
					props.put(MAREA_COLS, messagesArea.getColumns());
					props.put(DIVIDER_LOC, splitPane.getDividerLocation());
				}
				public void componentShown(ComponentEvent e) {}
				public void componentHidden(ComponentEvent e) {}
			});

			// Always make the window visible when first opened.
			setVisible(true);
		}

		public void setVisible(boolean state)
		{
			props.put(CWINDOW_VIS, (state ? "true" : "false"));
			super.setVisible(state);
		}

		private class OpenListener implements ActionListener
		{
			public void actionPerformed(ActionEvent e)
			{
				String sourceFile = props.getProperty(SOURCEFILE);

				if (sourceFile != null)
				{
					File file = new File(sourceFile);
					fileChooser.setSelectedFile(file);
					fileChooser.setCurrentDirectory(file);
				}

				fileChooser.setFileFilter(xpressFilter);

				int returnVal = fileChooser.showDialog(CompilerWindow.this,
					"Open");
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					File file = fileChooser.getSelectedFile();
					props.put(SOURCEFILE, file.getAbsolutePath());

					FileReader reader = null;
					try
					{
						reader = new FileReader(file);
						codeArea.read(reader, file);
						reader.close();

						String str = codeArea.getText();
						if ( str.indexOf('\n') < 0)
						{
							// On Macs \r is used as the new line character.
							// On windows its \n\r, on Unix its \n. Java
							// also uses \n. So if there are no \n's in the
							// file it is a Mac file so we replace all \r's
							// with \n's.
							str = codeArea.getText().replace('\r', '\n');
							codeArea.setText(str);
						}
						// Must set position at the top of the file.
						codeArea.moveCaretPosition(0);
					}
					catch (FileNotFoundException g)
					{
						writer.println("Failed to find file \"" +
							file.getName() + "\".");
					}
					catch (IOException f)
					{
						writer.println("Failed to close file \"" +
							file.getName() + "\".");
					}
				}
			}
		}

		private class SaveListener implements ActionListener
		{
			public void actionPerformed(ActionEvent e)
			{
				String cmd = e.getActionCommand();
				String sourceFile = props.getProperty(SOURCEFILE);

				if (sourceFile == null || !cmd.equals("Save"))
					saveAs(sourceFile, cmd);
				else
					save(sourceFile);
			}
		}

		private class CompileListener implements ActionListener
		{
			public void actionPerformed(ActionEvent event)
			{
				if (compile == null)
				{
					// To save memory do a load-on-demand load of the compiler.
					// This also decreases start up time.
					try
					{
						String location = props.getProperty(COMPILERLOCATION);

						if (location == null)
						{
							fileChooser.setSelectedFile(new File("Compile.class"));
							fileChooser.setFileFilter(classFilter);
							int returnVal = fileChooser.showDialog(
								CompilerWindow.this, "Select Compiler");
							if (returnVal != JFileChooser.APPROVE_OPTION)
								return;
							location =
								fileChooser.getSelectedFile().getParent();
							props.put(COMPILERLOCATION, location);
						}
						loadCompiler(location);

						// Register the write methods so we have somewhere to
						// write to.
						Object invokeArgs[] = new Object[1];
						invokeArgs[0] = writer;
						if (registerOutput != null)
							registerOutput.invoke(null, invokeArgs);
						if (registerError != null)
							registerError.invoke(null, invokeArgs);
					}
					catch (HCSException e)
					{
						writer.println("Failed to load compiler. " +
							"Unable to compile.");
						writer.println(e.getMessage());
						return;
					}
					catch (IllegalAccessException e)
					{
						// Registration functions are not accessible.
						writer.println("Unable to access compiler text output "
							+ "registration methods.");
						return;
					}
					catch (InvocationTargetException e)
					{
						// Registration methods threw an exception.
						writer.println("Compiler print registration method " +
							"threw an exception.");
						writer.println(e.getTargetException().getMessage());
						e.getTargetException().printStackTrace(writer);
						return;
					}

				}

				// Clear out any previous messages.
				messagesArea.setText("");

				// Run the compiler in a separate thread from the UI.
				Runnable update = new Runnable() {
					public void run()
					{
						try
						{
							String tmpname = "__tmp.hcs";
							save(tmpname);
							runCompiler(tmpname, "events.bin");
							writer.println("Started upload");
							hcs.load("events.bin");
							writer.println("Completed upload");
						}
						catch (Exception e)
						{
							writer.println(e.getMessage());
							e.printStackTrace(writer);
							return;
						}
					}
				};
				SwingUtilities.invokeLater(update);
			}
		}

		private class CompilerWriter extends Writer
		{
			public CompilerWriter()
			{
				super();
			}

			public void close() {}
			public void flush() {}

			public void write(char[] buf, int off, int len)
			{
				if (messagesArea != null)
				{
					String str = new String(buf, off, len);
					messagesArea.setText(messagesArea.getText().trim() + "\n" + str.trim());
				}
			}
		}

		protected void save(String filename)
		{
			if (filename == null)
				return;

			FileWriter writer = null;
			try
			{
				writer = new FileWriter(filename);
				codeArea.write(writer);
				writer.close();
			}
			catch (IOException e)
			{
				Error("Failed to write " + filename + ".\n" + e.getMessage());
			}
		}

		protected void saveAs(String filename, String action)
		{
			if (filename != null)
			{
				File file = new File(filename);
				fileChooser.setSelectedFile(file);
				fileChooser.setCurrentDirectory(file);
			}

			fileChooser.setFileFilter(xpressFilter);
			int returnVal = fileChooser.showDialog(CompilerWindow.this, action);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				filename = fileChooser.getSelectedFile().getAbsolutePath();
				save(filename);
				props.put(SOURCEFILE, filename);
			}
		}

		private void loadCompiler(String path) throws HCSException
		{
			String name = "Compile";
			Class c = hcs.loadClass(path, name);

			// Define the argument types for the constructor we want.
			Class[] paramTypes = new Class[1];
			paramTypes[0] = String[].class;

			// Get the main method for the Compiler. The main takes
			// two strings an input file name and an output file name.
			try
			{
				compile = c.getDeclaredMethod("main", paramTypes);
				if ( null == compile )
					throw new HCSException(HCSException.BADCLASS);
			}
			catch (NoSuchMethodException e)
			{
				throw new HCSException(HCSException.BADMETHOD);
			}
			catch (SecurityException s)
			{
				throw new HCSException(HCSException.BADSECURITY);
			}

			// Also need to get references to the the register functions.
			paramTypes[0] = PrintWriter.class;

			// Get the output registration method.
			try
			{
				registerOutput = c.getDeclaredMethod("registerOutputWriter",
					paramTypes);
				registerError = c.getDeclaredMethod("registerErrorWriter",
					paramTypes);
				if ( null == registerOutput || null == registerError )
					throw new HCSException(HCSException.BADCLASS);
			}
			catch (NoSuchMethodException e)
			{
				throw new HCSException(HCSException.BADMETHOD);
			}
			catch (SecurityException s)
			{
				throw new HCSException(HCSException.BADSECURITY);
			}
		}

		protected void runCompiler(String inputFile, String outputFile)
			throws Exception
		{
			if (compile == null)
				throw new HCSException(HCSException.BADMETHOD);

			// Create the argument array for the compiler main() method.
			String compilerArgs[] = new String[2];
			compilerArgs[0] = inputFile;
			compilerArgs[1] = outputFile;

			// Turn these arguments into an Object array to pass to the
			// Method.invoke() method.
			Object invokeArgs[] = new Object[1];
			invokeArgs[0] = compilerArgs;

			// The compiler can throw exceptions, but we leave catching
			// them to the calling function.
			compile.invoke(null, invokeArgs);
		}
	}

	private class X10Window extends JFrame
	{
		protected X10Button x10Buttons[][] = new X10Button[16][16];
		protected boolean houseCodes[] = new boolean[16];

		public X10Window()
		{
			int i, j;

			setTitle("X10 Modules");
		
			// Allow the frame to respond to window close events.
			addWindowListener( new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					setVisible(false);
				}
			});

			Container x10Pane = getContentPane();

			for (i = 0; i < 16; i++) houseCodes[i] = false;
			char[] chars = props.getProperty(X10HOUSECODES, AtoP)
				.toCharArray();
			int numHouseCodes = 0;
			for (i = 0; i < chars.length; i++)
			{
				if (chars[i] >= 'A' && chars[i] <= 'P')
				{
					int index = (int)(chars[i]-'A');
					if (!houseCodes[index])
					{
						houseCodes[index] = true;
						numHouseCodes++;
					}
				}
			}

			// There are 16 house codes by 16 units in X10 so we create an
			// at most 17 x 17 grid layout. (Add one for the labels.)
			x10Pane.setLayout(new GridLayout(numHouseCodes+1, 17));

			// The top left corner is blank.
			x10Pane.add(new JLabel(""));

			// Number the units across the top.
			JLabel label = null;
			for (i = 0; i < 16; i++)
			{
				label = new JLabel(new Integer(i+1).toString());
				label.setForeground(Color.black);
				x10Pane.add(label);
			}

			// All X10 buttons will share the same listener.
			X10Listener x10Listener = new X10Listener();

			// Create the buttons for X10.
			for (i = 0; i < 16; i++)
			{
				// Don't create a houseCodes list if the user doesn't want it.
				if (!houseCodes[i]) continue;

				// Create and output the house code label.
				Character chr = new Character((char)('A' + i));
				String module = chr.toString();
				label = new JLabel(module);
				label.setForeground(Color.black);
				x10Pane.add(label);

				// Create the 16 buttons for this house code.
				for (j = 0; j < 16; j++)
				{
					X10Button tmp = new X10Button(i+1, j+1);
					tmp.addActionListener(x10Listener);
					x10Pane.add(tmp);
					x10Buttons[i][j] = tmp;
				}
			}
		
			// Pack the frame then display it.
			pack();

			setLocation(
				props.getProperty(X10WINDOW_POSX, 0),
				props.getProperty(X10WINDOW_POSY, 0));

			// If either dimension is 0, then use the calculated values.
			int x = props.getProperty(X10WINDOW_WIDTH, 0);
			int y = props.getProperty(X10WINDOW_HEIGHT, 0);
			if (x != 0 && y != 0) setSize( x, y );

			// Listen for resize and repositioning events so that we can
			// save this information for the next time the application is
			// run.
			addComponentListener( new ComponentListener() {
				public void componentMoved(ComponentEvent e) {
					Point pos = getLocation();
					props.put(X10WINDOW_POSX, pos.x);
					props.put(X10WINDOW_POSY, pos.y);
				}
				public void componentResized(ComponentEvent e) {
					Dimension size = getSize();
					props.put(X10WINDOW_WIDTH, size.width);
					props.put(X10WINDOW_HEIGHT, size.height);
				}
				public void componentShown(ComponentEvent e) {}
				public void componentHidden(ComponentEvent e) {}
			});

			setVisible(true);
		}

		private HCSEventListener eventListener = new HCSEventListener() {
			public void portEvent(HCSEvent event) {
				// The event comes in with the housecode as an ASCII letter
				// and the state of all units in that housecode. We can't learn
				// which unit caused the event so we update all units for the
				// housecode.
				final int house = event.unit - 'A';
				final int houseState = event.state;

				// If the user doesn't want this house code displayed, ignore
				// it.
				if (!houseCodes[house]) return;

				// This listener exists in a separate thread than the Swing
				// event dispatcher, so we're unable to access the Swing
				// components from this thread. Instead we need to create
				// a new one and let Swing handle the update itself.
				Runnable update = new Runnable() {
					public void run()
					{
						int bit;
						boolean state;
						for (int n = 0; n < 16; n++)
						{
							bit = 1<<n;
							state = ((houseState & bit) == bit);
							x10Buttons[house][n].setSelected(state);
						}
					}
				};
				SwingUtilities.invokeLater(update);
			}
		};

		public void setVisible(boolean state)
		{
			props.put(X10WINDOW_VIS, (state ? "true" : "false"));
			super.setVisible(state);

			// Remove the event listener if the window is invisible. This
			// will improve the responsiveness of the application in general.
			if (state)
			{
				hcs.addEventListener(HCSEvent.X10MODULES,
					eventListener);
				// Get a full update from the controller. We could be smarter
				// about this by getting an update on only the X10 modules of
				// interest but that would be more time consuming.
				getInfo();
			}
			else
				hcs.removeEventListener(HCSEvent.X10MODULES, eventListener);
		}

		// Create our own radio button which remembers its location in the grid.
		// This simplifies sending out X10 events when a button is clicked. This
		// class cannot be a local class as others need to reference a variable
		// of this type.
		private class X10Button extends JRadioButton
		{
			public X10Button(int house, int unit)
			{
				super("");
				this.house = house;
				this.unit = unit;
			}

			public int house;
			public int unit;
		}

		// Create a member class to serve as the action listener for the
		// buttons. (I'd prefer to use this as a local class above, but
		// the Java compiler I'm using doesn't allow local classes in
		// member classes.)
		private class X10Listener implements ActionListener
		{
			public void actionPerformed(ActionEvent e)
			{
				X10Button tmp = (X10Button)e.getSource();
				try
				{
					hcs.setX10Module(tmp.house,
						tmp.unit, (tmp.isSelected() ? 2: 3), 0);
				}
				catch (HCSException f)
				{
					Error("Failed to set X10 module.\n" + f.getMessage());
				}
			}
		}
	}

	// Create our own radio button which remembers its location in a grid.
	// This simplifies sending out io events when a button is clicked.
	// This is shared by all the derivations of DigitalWindow.
	private class DigitalButton extends JRadioButton
	{
		public DigitalButton(int io)
		{
			super("");
			this.io = io;
		}

		public int io;
	}

	private class DigitalWindow extends JFrame
	{
		protected DigitalButton digitalButtons[] = null;
		protected boolean groups[] = null;
		protected String ioType = null;
		protected int eventType = -1;

		public DigitalWindow(String type, int numberOf, int eventType)
		{
			int i, j;

			ioType = type;
			this.eventType = eventType;
			int numBitMaps = numberOf>>>3;
			int units[] = new int[numBitMaps];
			groups = new boolean[numBitMaps];
			digitalButtons = new DigitalButton[numberOf];

			// Allow the frame to respond to window close events.
			addWindowListener( new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					setVisible(false);
				}
			});

			// Build the list of units the user would like to see.
			for (i = 0; i < numBitMaps; i++)
			{
				units[i] = -1;
				groups[i] = false;
			}

			StringBuffer buf = new StringBuffer();
			for (i = 0; i < numBitMaps; i++) buf.append('1');
			String defaults = new String(buf);
			char[] chars = props.getProperty(ioType+".UNITS", defaults)
				.toCharArray();
			int numUnits = 0;
			for (i = 0; i < chars.length; i++)
			{
				if (chars[i] == '1')
				{
					units[numUnits++] = i<<3;	// i.e. i*8
					groups[i] = true;
				}
			}

			Container pane = getContentPane();

			// Create 1 row for every 2 input groups. Each row has 16
			// buttons and two labels.
			if (numUnits > 1)
				pane.setLayout(new GridLayout(numUnits>>>1, 18));
			else
				pane.setLayout(new GridLayout(1, 9));

			// Create the buttons for inputs/outputs.
			JLabel label = null;
			for (i = 0; i < numUnits; i++)
			{
				// Create and output the house code label.
				label = new JLabel(String.valueOf(units[i]));
				label.setForeground(Color.black);
				pane.add(label);

				// Create the 8 buttons for this set of inputs/outputs.
				// We wind up wasting some space since the digitalButtons
				// array will be sparsely populated in many cases. But the
				// waste is only a K or so. We'd use more space trying to be
				// smart about this.
				for (j = 0; j < 8; j++)
				{
					DigitalButton tmp = new DigitalButton(units[i]+j);
					tmp.addActionListener( new ActionListener() {
						public void actionPerformed(ActionEvent e)
						{
							DigitalButton db = (DigitalButton)e.getSource();
							buttonEvent(db.io, (db.isSelected() ? 1: 0));
						}
					});
					digitalButtons[units[i]+j] = tmp;
					pane.add(tmp);
				}
			}
		
			// Pack the frame then display it.
			pack();

			setLocation(
				props.getProperty(ioType+".POSX", 0),
				props.getProperty(ioType+".POSY", 0));

			// If either dimension is 0, then use the calculated values.
			int x = props.getProperty(ioType+".WIDTH", 0);
			int y = props.getProperty(ioType+".HEIGHT", 0);
			if (x != 0 && y != 0) setSize( x, y );

			// Listen for resize and repositioning events so that we can
			// save this information for the next time the application is
			// run.
			addComponentListener( new ComponentListener() {
				public void componentMoved(ComponentEvent e) {
					Point pos = getLocation();
					props.put(ioType+".POSX", pos.x);
					props.put(ioType+".POSY", pos.y);
				}
				public void componentResized(ComponentEvent e) {
					Dimension size = getSize();
					props.put(ioType+".WIDTH", size.width);
					props.put(ioType+".HEIGHT", size.height);
				}
				public void componentShown(ComponentEvent e) {}
				public void componentHidden(ComponentEvent e) {}
			});

			setVisible(true);
		}

		private HCSEventListener eventListener = new HCSEventListener() {
			public synchronized void portEvent(HCSEvent event) {
				// If the user doesn't want this group displayed, ignore
				// it.
				if (!groups[event.unit]) return;
				final int groupBase = event.unit << 3;
				final int groupState = event.state;
				Runnable update = new Runnable() {
					public void run()
					{
						int bit;
						boolean state;
						for (int n = 0; n < 8; n++)
						{
							bit = 1<<n;
							state = ((groupState & bit) == bit);
							digitalButtons[groupBase + n].setSelected(state);
						}
					}
				};
				SwingUtilities.invokeLater(update);
			}
		};

		public void setVisible(boolean state)
		{
			props.put(ioType+".VISIBILITY", (state ? "true" : "false"));
			super.setVisible(state);

			// Remove the event listener if the window is invisible. This
			// will improve the responsiveness of the application in general.
			if (state)
			{
				hcs.addEventListener(eventType, eventListener);
				getInfo();
			}
			else
				hcs.removeEventListener(eventType, eventListener);
		}

		protected void buttonEvent(int thing, int state)
		{
			System.out.println("Internal Error: called base class buttonEvent.");
		}
	}

	private class DigitalInputWindow extends DigitalWindow
	{
		public DigitalInputWindow()
		{
			super("DIGITALINPUT", 256, HCSEvent.DIGITALINPUTS);
			setTitle("Digital Inputs");
		}

		protected void buttonEvent(int input, int state)
		{
			try
			{
				hcs.setDigitalInputState(input, state);
			}
			catch (HCSException f)
			{
				Error("Failed to set digital input.\n" + f.getMessage());
			}
		}
	}

	private class DigitalOutputWindow extends DigitalWindow
	{
		public DigitalOutputWindow()
		{
			super("DIGITALOUTPUT", 256, HCSEvent.DIGITALOUTPUTS);
			setTitle("Digital Outputs");
		}

		protected void buttonEvent(int input, int state)
		{
			try
			{
				hcs.setDigitalOutputState(input, state>0);
			}
			catch (HCSException f)
			{
				Error("Failed to set digital output.\n" + f.getMessage());
			}
		}
	}

	private class NetbitsWindow extends DigitalWindow
	{
		public NetbitsWindow()
		{
			super("NETBITS", 320, HCSEvent.NETBITS);
			setTitle("Netbits");
		}

		protected void buttonEvent(int input, int state)
		{
			try
			{
				hcs.setNetbitState(input, state==1);
			}
			catch (HCSException f)
			{
				Error("Failed to set netbit.\n" + f.getMessage());
			}
		}
	}

	private class AnalogWindow extends JFrame
	{
		protected boolean groups[] = null;
		protected String ioType = null;
		protected int eventType = -1;
		protected JLabel values[] = null;
		protected int numberInUpdate;

		public AnalogWindow(String type, int numberOf, int numberInUpdate,
			int eventType)
		{
			int i, j;

			ioType = type;
			this.eventType = eventType;
			this.numberInUpdate = numberInUpdate;
			int numBitMaps = numberOf/numberInUpdate;
			int units[] = new int[numBitMaps];
			groups = new boolean[numBitMaps];
			values = new JLabel[numberOf];

			// Allow the frame to respond to window close events.
			addWindowListener( new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					setVisible(false);
				}
			});

			// Build the list of units the user would like to see.
			for (i = 0; i < numBitMaps; i++)
			{
				units[i] = -1;
				groups[i] = false;
			}

			StringBuffer buf = new StringBuffer();
			for (i = 0; i < numBitMaps; i++) buf.append('1');
			String defaults = new String(buf);
			char[] chars = props.getProperty(ioType+".UNITS", defaults)
				.toCharArray();
			int numUnits = 0;
			for (i = 0; i < chars.length; i++)
			{
				if (chars[i] == '1')
				{
					units[numUnits++] = i*numberInUpdate;
					groups[i] = true;
				}
			}

			Container pane = getContentPane();

			// Create 1 row for every 2 input groups. Each row has 16
			// values and two labels.
			if (numUnits > 1)
				pane.setLayout(new GridLayout(numUnits>>>1, numberInUpdate*2+2,
					5, 2));
			else
				pane.setLayout(new GridLayout(1, numberInUpdate+1, 5, 0));

			// Create the value labels for inputs/outputs.
			JLabel label = null;
			for (i = 0; i < numUnits; i++)
			{
				// Create and output the label.
				label = new JLabel(String.valueOf(units[i]) + " ");
				label.setForeground(Color.black);
				pane.add(label);

				// Create the 8 labels for this set of inputs/outputs.
				// We wind up wasting some space since the digitalButtons
				// array will be sparsely populated in many cases. But the
				// waste is only a K or so. We'd use more space trying to be
				// smart about this.
				for (j = 0; j < numberInUpdate; j++)
				{
					values[units[i]+j] = new JLabel("????");
					pane.add(values[units[i]+j]);
				}
			}
		
			// Pack the frame then display it.
			pack();

			setLocation(
				props.getProperty(ioType+".POSX", 0),
				props.getProperty(ioType+".POSY", 0));

			// If either dimension is 0, then use the calculated values.
			int x = props.getProperty(ioType+".WIDTH", 0);
			int y = props.getProperty(ioType+".HEIGHT", 0);
			if (x != 0 && y != 0) setSize( x, y );

			// Listen for resize and repositioning events so that we can
			// save this information for the next time the application is
			// run.
			addComponentListener( new ComponentListener() {
				public void componentMoved(ComponentEvent e) {
					Point pos = getLocation();
					props.put(ioType+".POSX", pos.x);
					props.put(ioType+".POSY", pos.y);
				}
				public void componentResized(ComponentEvent e) {
					Dimension size = getSize();
					props.put(ioType+".WIDTH", size.width);
					props.put(ioType+".HEIGHT", size.height);
				}
				public void componentShown(ComponentEvent e) {}
				public void componentHidden(ComponentEvent e) {}
			});

			setVisible(true);
		}

		private HCSEventListener eventListener = new HCSEventListener() {
			public synchronized void portEvent(HCSEvent event) {
				// If the user doesn't want this group displayed, ignore
				// it.
				if (!groups[event.unit]) return;
				final int groupBase = event.unit * numberInUpdate;
				final int groupValues[] = event.values;
				Runnable update = new Runnable() {
					public void run()
					{
						for (int n = 0; n < numberInUpdate; n++)
							values[groupBase + n].setText(
								String.valueOf(groupValues[n]));
					}
				};
				SwingUtilities.invokeLater(update);
			}
		};

		public void setVisible(boolean state)
		{
			props.put(ioType+".VISIBILITY", (state ? "true" : "false"));
			super.setVisible(state);

			// Remove the event listener if the window is invisible. This
			// will improve the responsiveness of the application in general.
			if (state)
			{
				hcs.addEventListener(eventType, eventListener);
				getInfo();
			}
			else
				hcs.removeEventListener(eventType, eventListener);
		}
	}

	private class AnalogInputWindow extends AnalogWindow
	{
		public AnalogInputWindow()
		{
			super("ANALOGINPUT", 192, 8, HCSEvent.ANALOGINPUTS);
			setTitle("Analog Inputs");
		}
	}

	private class AnalogOutputWindow extends AnalogWindow
	{
		public AnalogOutputWindow()
		{
			super("ANALOGOUTPUT", 64, 2, HCSEvent.ANALOGOUTPUTS);
			setTitle("Analog Outputs");
		}
	}

	private class CircleIcon implements Icon
	{
		private Color colour;
		private int width = 10;
		private int height = 10;

		public CircleIcon(Color c) { colour = c; }
		public int getIconWidth() { return width; }
		public int getIconHeight() { return height; }
		public void paintIcon(Component c, Graphics g, int x, int y)
		{
			g.setColor(colour);
			g.fillOval(x, y, width, height);
		}
	}

	private class NetworkModulesWindow extends JFrame
	{
		protected JLabel labels[][] = new JLabel[6][8];
		protected boolean modules[] = new boolean[6];
		protected Icon redIcon = new CircleIcon(Color.red);
		protected Icon yellowIcon = new CircleIcon(Color.yellow);
		protected Icon greenIcon = new CircleIcon(Color.green);

		public NetworkModulesWindow()
		{
			int i, j;

			setTitle("Network Modules");
		
			// Allow the frame to respond to window close events.
			addWindowListener( new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					setVisible(false);
				}
			});

			Container pane = getContentPane();

			for (i = 0; i < 6; i++) modules[i] = false;
			char[] chars = props.getProperty(NETWORKMODULES, "111111")
				.toCharArray();
			int numModules = 0;
			for (i = 0; i < chars.length; i++)
			{
				if (chars[i] == '1')
				{
					modules[i] = true;
					numModules++;
				}
			}

			pane.setLayout(new GridLayout(numModules+1, 9));

			// The top left corner is blank.
			pane.add(new JLabel(""));

			// Number the units across the top.
			JLabel label = null;
			for (i = 0; i < 8; i++)
			{
				label = new JLabel(new Integer(i+1).toString());
				label.setForeground(Color.black);
				pane.add(label);
			}

			// Create the buttons for X10.
			for (i = 0; i < 6; i++)
			{
				// Don't create a modules list if the user doesn't want it.
				if (!modules[i]) continue;

				switch (i)
				{
					case 0: label = new JLabel("PL"); break;
					case 1: label = new JLabel("MCIR"); break;
					case 2: label = new JLabel("LCD"); break;
					case 3: label = new JLabel("DIO"); break;
					case 4: label = new JLabel("ADIO"); break;
					case 5: label = new JLabel("DIO+"); break;
				}
				label.setForeground(Color.black);
				pane.add(label);

				// Create the 8 labels for this house code.
				for (j = 0; j < 8; j++)
				{
					JLabel tmp = new JLabel(redIcon);
					labels[i][j] = tmp;
					pane.add(tmp);
				}
			}
		
			// Pack the frame then display it.
			pack();

			setLocation(
				props.getProperty(NETWORKMODULES_POSX, 0),
				props.getProperty(NETWORKMODULES_POSY, 0));

			// If either dimension is 0, then use the calculated values.
			int x = props.getProperty(NETWORKMODULES_WIDTH, 0);
			int y = props.getProperty(NETWORKMODULES_HEIGHT, 0);
			if (x != 0 && y != 0) setSize( x, y );

			// Listen for resize and repositioning events so that we can
			// save this information for the next time the application is
			// run.
			addComponentListener( new ComponentListener() {
				public void componentMoved(ComponentEvent e) {
					Point pos = getLocation();
					props.put(NETWORKMODULES_POSX, pos.x);
					props.put(NETWORKMODULES_POSY, pos.y);
				}
				public void componentResized(ComponentEvent e) {
					Dimension size = getSize();
					props.put(NETWORKMODULES_WIDTH, size.width);
					props.put(NETWORKMODULES_HEIGHT, size.height);
				}
				public void componentShown(ComponentEvent e) {}
				public void componentHidden(ComponentEvent e) {}
			});

			setVisible(true);
		}

		private HCSEventListener eventListener = new HCSEventListener() {
			public void portEvent(HCSEvent event) {
				// The event comes in with the housecode as an ASCII letter
				// and the state of all units in that housecode. We can't learn
				// which unit caused the event so we update all units for the
				// housecode.
				final int module = event.module;
				final int unit = event.unit;
				final int state = event.state;

				// If the user doesn't want this house code displayed, ignore
				// it.
				if (!modules[module]) return;

				// This listener exists in a separate thread than the Swing
				// event dispatcher, so we're unable to access the Swing
				// components from this thread. Instead we need to create
				// a new one and let Swing handle the update itself.
				Runnable update = new Runnable() {
					public void run()
					{
						labels[module][unit].setIcon(
							(state == 0 ? yellowIcon :
							(state == 1 ? greenIcon : redIcon)));
					}
				};
				SwingUtilities.invokeLater(update);
			}
		};

		public void setVisible(boolean state)
		{
			props.put(NETWORKMODULES_VIS, (state ? "true" : "false"));
			super.setVisible(state);

			// Remove the event listener if the window is invisible. This
			// will improve the responsiveness of the application in general.
			if (state)
			{
				hcs.addEventListener(HCSEvent.NETWORKMODULES,
					eventListener);
				getInfo();
			}
			else
				hcs.removeEventListener(HCSEvent.NETWORKMODULES,
					eventListener);
		}

	}

	private class ConsoleWindow extends JFrame
	{
		public ConsoleWindow()
		{
			// Build the menues.
			JMenuBar menuBar = new JMenuBar();
			setJMenuBar(menuBar);
			hcs.registerMenuBar(menuBar);

			// Create the File menu.
			JMenu menu = new JMenu("File");
			menu.setMnemonic('f');
			menu.getAccessibleContext().setAccessibleDescription(
				"File menu");
			menuBar.add(menu);

			// Create File->About
			JMenuItem menuItem = new JMenuItem("About...", KeyEvent.VK_A);
			menuItem.getAccessibleContext().setAccessibleDescription(
				"Information about the application");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Runnable update = new Runnable() {
						public void run()
						{
							AboutWindow aboutWindow = new AboutWindow();
						}
					};
					SwingUtilities.invokeLater(update);
				}
			});
			menu.add(menuItem);

			// Create File->Open UI
			menuItem = new JMenuItem("Open UI...", KeyEvent.VK_O);
			menuItem.getAccessibleContext().setAccessibleDescription(
				"Open UI Class File");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Runnable update = new Runnable() {
						public void run()
						{
							openUI();
						}
					};
					SwingUtilities.invokeLater(update);
				}
			});
			menu.add(menuItem);

			// Create File->Save Properties
			menuItem = new JMenuItem("Save Properties", KeyEvent.VK_S);
			menuItem.getAccessibleContext().setAccessibleDescription(
				"Save Application Properties");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					props.save();
				}
			});
			menu.add(menuItem);

			// Create File->Exit
			menuItem = new JMenuItem("Exit", KeyEvent.VK_E);
			menuItem.getAccessibleContext().setAccessibleDescription(
				"Exit the application");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					props.save();
					System.exit(0);
				}
			});
			menu.add(menuItem);

			// Create the Edit menu.
			menu = new JMenu("Edit");
			menu.setMnemonic('e');
			menu.getAccessibleContext().setAccessibleDescription(
				"Edit menu");
			menuBar.add(menu);

			// Create Edit->Preferences...
			menuItem = new JMenuItem("Preferences...", KeyEvent.VK_P);
			menuItem.getAccessibleContext().setAccessibleDescription(
				"Open the Preferences Dialog Box.");
			menuItem.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (prefsWindow == null)
						prefsWindow = new PreferencesWindow();
					else prefsWindow.setVisible(!prefsWindow.isVisible());
				}
			});
			menu.add(menuItem);

			// Create the Commands menu.
			menu = new JMenu("Commands");
			menu.setMnemonic('c');
			menu.getAccessibleContext().setAccessibleDescription(
				"Commands menu");
			menuBar.add(menu);

			// Create Commands->command...
			menuItem = new JMenuItem("Send Command...", KeyEvent.VK_P);
			menuItem.getAccessibleContext().setAccessibleDescription(
				"Send a command to the controller.");
			menuItem.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (commandWindow == null)
						commandWindow = new CommandWindow();
					else commandWindow.setVisible(!commandWindow.isVisible());
				}
			});
			menu.add(menuItem);

			// Create the Windows menu.
			menu = new JMenu("Windows");
			menu.setMnemonic('w');
			menu.getAccessibleContext().setAccessibleDescription(
				"Windows menu");
			menuBar.add(menu);

			// Create Windows->Compiler
			menuItem = new JMenuItem("Compiler", KeyEvent.VK_X);
			menuItem.getAccessibleContext().setAccessibleDescription(
				"Toggle the Compiler Window.");
			menuItem.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (compilerWindow == null)	
						compilerWindow = new CompilerWindow();
					else
						compilerWindow.setVisible(!compilerWindow.isVisible());
				}
			});
			menu.add(menuItem);

			menuItem = new JMenuItem("Digital Inputs", KeyEvent.VK_I);
			menuItem.getAccessibleContext().setAccessibleDescription(
				"Toggle the Digital Inputs Window.");
			menuItem.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (digitalInputWindow == null)
						digitalInputWindow = new DigitalInputWindow();
					else
						digitalInputWindow.setVisible(
							!digitalInputWindow.isVisible());
				}
			});
			menu.add(menuItem);

			menuItem = new JMenuItem("Digital Outputs", KeyEvent.VK_O);
			menuItem.getAccessibleContext().setAccessibleDescription(
				"Toggle the Digital Outputs Window.");
			menuItem.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (digitalOutputWindow == null)
						digitalOutputWindow = new DigitalOutputWindow();
					else
						digitalOutputWindow.setVisible(
							!digitalOutputWindow.isVisible());
				}
			});
			menu.add(menuItem);

			menuItem = new JMenuItem("Netbits", KeyEvent.VK_I);
			menuItem.getAccessibleContext().setAccessibleDescription(
				"Toggle the Netbits Window.");
			menuItem.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (netbitsWindow == null)
						netbitsWindow = new NetbitsWindow();
					else
						netbitsWindow.setVisible(
							!netbitsWindow.isVisible());
				}
			});
			menu.add(menuItem);

			menuItem = new JMenuItem("X10 Modules", KeyEvent.VK_X);
			menuItem.getAccessibleContext().setAccessibleDescription(
				"Toggle the X10 Window.");
			menuItem.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (x10Window == null) x10Window = new X10Window();
					else x10Window.setVisible(!x10Window.isVisible());
				}
			});
			menu.add(menuItem);

			menuItem = new JMenuItem("Analog Inputs");
			menuItem.getAccessibleContext().setAccessibleDescription(
				"Toggle the Analog Inputs Window.");
			menuItem.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (analogInputWindow == null)
						analogInputWindow = new AnalogInputWindow();
					else
						analogInputWindow.setVisible(
							!analogInputWindow.isVisible());
				}
			});
			menu.add(menuItem);

			menuItem = new JMenuItem("Analog Outputs");
			menuItem.getAccessibleContext().setAccessibleDescription(
				"Toggle the Analog Outputs Window.");
			menuItem.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (analogOutputWindow == null)
						analogOutputWindow = new AnalogOutputWindow();
					else
						analogOutputWindow.setVisible(
							!analogOutputWindow.isVisible());
				}
			});
			menu.add(menuItem);

			menuItem = new JMenuItem("Network Modules");
			menuItem.getAccessibleContext().setAccessibleDescription(
				"Toggle the Network Modules Window.");
			menuItem.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (networkModulesWindow == null)
						networkModulesWindow = new NetworkModulesWindow();
					else
						networkModulesWindow.setVisible(
							!networkModulesWindow.isVisible());
				}
			});
			menu.add(menuItem);

			// We don't want the window to ever go away since it has the
			// main menues.
			setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

			final Container pane = getContentPane();
			final JLabel label = new JLabel();
			pane.add(label);

			setLocation(
				props.getProperty(CONSOLE_POSX, 0),
				props.getProperty(CONSOLE_POSY, 0));
			setSize(
				props.getProperty(CONSOLE_WIDTH, 300),
				props.getProperty(CONSOLE_HEIGHT, 100));

			// Pack the frame then display it.
			addComponentListener( new ComponentListener() {
				public void componentMoved(ComponentEvent e) {
					Point pos = getLocation();
					props.put(CONSOLE_POSX, pos.x);
					props.put(CONSOLE_POSY, pos.y);
				}
				public void componentResized(ComponentEvent e) {
					Dimension size = getSize();
					props.put(CONSOLE_WIDTH, size.width);
					props.put(CONSOLE_HEIGHT, size.height);
				}
				public void componentShown(ComponentEvent e)  {}
				public void componentHidden(ComponentEvent e) {}
			});
			String timeProp = props.getProperty(CONSOLE_TIME, "Off");
			enableTime(!timeProp.equals("Off"), timeProp.endsWith("Second"));
			setVisible(true);

			// Tell the port manager that this anonymous class will be
			// listening to message events.
			hcs.addEventListener(HCSEvent.CONSOLEMESSAGE,
					new HCSEventListener() {
				public void portEvent(HCSEvent event) {
					final String msg = event.message;
					Runnable update = new Runnable() {
						public void run()
						{
							label.setText(msg.trim());
						}
					};
					SwingUtilities.invokeLater(update);
				}
			});
		}

		private boolean updatingTime = false;
		private boolean updatingSeconds = false;
		private GregorianCalendar time = new GregorianCalendar();

		private HCSEventListener eventListener = new HCSEventListener() {
			public void portEvent(HCSEvent event) {
				time.set(event.year, event.month, event.day, event.hour,
					event.minute, event.second);
				Runnable update = new Runnable() {
					public void run()
					{
						setTitle();
					}
				};
				SwingUtilities.invokeLater(update);
			}
		};

		public void enableTime(boolean enableTime, boolean showSeconds)
		{
			if (enableTime && !updatingTime)
				hcs.addEventListener(HCSEvent.TIME, eventListener);
			else if (!enableTime && updatingTime)
				hcs.removeEventListener(HCSEvent.TIME, eventListener);

			updatingTime = enableTime;
			updatingSeconds = showSeconds;
			setTitle();
		}

		private void setTitle()
		{
			String title = new String(TITLEBASE + port);
			if (updatingTime)
			{
				if (updatingSeconds)
					title += " - " + 
						DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
							DateFormat.LONG).format(time.getTime());
				else
					title += " - " + 
						DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
							DateFormat.SHORT).format(time.getTime());
			}

			setTitle(title);
		}

		// Allow the user to select a class file to open.
		protected void openUI()
		{
			File file = new File(props.getProperty(UI_FILE), "");
			fileChooser.setSelectedFile(file);
			fileChooser.setCurrentDirectory(file);

			fileChooser.setFileFilter(classFilter);
			int returnVal = fileChooser.showDialog(ConsoleWindow.this, "Open");
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				file = fileChooser.getSelectedFile();
				try
				{
					hcs.loadUI(file.getParent(), file.getName());
					props.put(UI_FILE, file.getAbsolutePath());
				}
				catch (Exception e)
				{
					Error("Unable to load class.\n" + e.getMessage());
				}
			}
		}
	}

	private class PreferencesWindow extends JFrame
	{
		private JTextField x10Field = null;
		protected boolean diginGroups[] = new boolean[32];
		protected boolean digoutGroups[] = new boolean[32];
		protected boolean netbitGroups[] = new boolean[40];
		protected boolean analogoutGroups[] = new boolean[8];
		protected boolean analoginGroups[] = new boolean[24];
		protected boolean networkModules[] = new boolean[6];

		public PreferencesWindow()
		{
			setTitle("Preferences");

			JTabbedPane pane = new JTabbedPane();
			GridBagConstraints c = new GridBagConstraints();

			// Create the Ports Pane.
			GridBagLayout gridbag = new GridBagLayout();
			JPanel panel = new JPanel(gridbag);
			JLabel label = new JLabel("Please select the port the");
			c.gridx = 0; c.gridy = 0; c.gridwidth = 1; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0; // c.insets = new Insets(5,5,5,5);
			gridbag.setConstraints(label, c);
			panel.add(label);
			label = new JLabel("controller is connected to.");
			c.gridx = 0; c.gridy = 1; c.gridwidth = 1; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0; // c.insets = new Insets(5,5,5,5);
			gridbag.setConstraints(label, c);
			panel.add(label);

			// Need an actionListener on the combobox. Also need to set the
			// previously selected port.
			JComboBox comboBox = new JComboBox(hcs.getPortList());
			comboBox.setSelectedItem(port);
			comboBox.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					JComboBox cb = (JComboBox)e.getSource();
					try
					{
						openPort((String)cb.getSelectedItem());
						consoleWindow.setTitle();
					}
					catch (HCSException ex)
					{
						Error("Failed to open port.\n" + ex.getMessage());
					}
				}
			});
			c.gridx = 0; c.gridy = 2; c.gridwidth = 1; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0; // c.insets = new Insets(5,5,5,5);
			gridbag.setConstraints(comboBox, c);
			panel.add(comboBox);
			pane.addTab("Port", null, panel, "Select Controller Port");

			// Create the X10 Pane.
			gridbag = new GridBagLayout();
			panel = new JPanel(gridbag);
			label = new JLabel("Please enter the X10 units");
			c.gridx = 0; c.gridy = 0; c.gridwidth = 1; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0; // c.insets = new Insets(5,5,5,5);
			gridbag.setConstraints(label, c);
			panel.add(label);
			label = new JLabel("to display. i.e. ABEFG");
			c.gridx = 0; c.gridy = 1; c.gridwidth = 1; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0; // c.insets = new Insets(5,5,5,5);
			gridbag.setConstraints(label, c);
			panel.add(label);

			x10Field = new JTextField(16);
			x10Field.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JTextField tf = (JTextField)e.getSource();
					String str = tf.getText().toUpperCase();
					char[] chars = str.toCharArray();
					boolean error = false;
					int units = 0;
					int bit;
					for (int i = 0; i < chars.length; i++)
					{
						if (chars[i] < 'A' && chars[i] > 'P')
						{
							error = true;
							break;
						}
						else
						{
							bit = chars[i] - 'A';	// a=0, p=15
							units |= (1<<bit);
						}
					}

					if (error)
						Error("Invalid X10 specification.\n\t\""+str+"\"");
					else if (!str.equals(props.getProperty(X10HOUSECODES,AtoP)))
					{
						try
						{
							hcs.selectX10Display(units);
							// The window height will change with new units.
							props.put(X10WINDOW_HEIGHT, 0);
							props.put(X10HOUSECODES, str);
							if (x10Window != null)
							{
								boolean wasVisible = x10Window.isVisible();
								x10Window.dispose();
								x10Window = null;
								if (wasVisible) x10Window = new X10Window();
							}
						}
						catch (HCSException ex)
						{
							Error("Failed to set X10 modules for display.\n" +
								ex.getMessage());
							x10Field.setText(props.getProperty(X10HOUSECODES,
								AtoP));
						}
					}
				}
			});
			c.gridx = 0; c.gridy = 2; c.gridwidth = 1; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0; // c.insets = new Insets(5,5,5,5);
			gridbag.setConstraints(x10Field, c);
			panel.add(x10Field);
			pane.addTab("X10", null, panel, "Select X10 Modules");

			// Create the digital inputs pane.
			gridbag = new GridBagLayout();
			panel = new JPanel(gridbag);
			JRadioButton radioButton = null;
			ActionListener listener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// Toggle the bit appropriately.
					JRadioButton b = (JRadioButton)e.getSource();
					diginGroups[parseInt(e.getActionCommand())]
						= b.isSelected() ? true : false;
				}
			};
			char[] chars = props.getProperty("DIGITALINPUT.UNITS", DEFAULTDIGINOUT)
				.toCharArray();

			// Initialize the radio buttons.
			c.anchor = GridBagConstraints.WEST;
			for (int i = 0; i < 32; i++)
			{
				radioButton = new JRadioButton(8*i + "-" + (8*i+7)); 
				radioButton.setActionCommand(String.valueOf(i));
				radioButton.addActionListener(listener);
				if (chars[i] == '1')
				{
					radioButton.setSelected(true);
					diginGroups[i] = true;
				}
				else
					diginGroups[i] = false;
				c.gridx = (i&3); c.gridy = (i>>2);
				c.gridwidth = 1; c.gridheight = 1;
				c.weightx = 0.0; c.weighty = 0.0;
				gridbag.setConstraints(radioButton, c);
				panel.add(radioButton);
			}
			JButton button = new JButton("Select");
			button.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					String property = new String("");
					int bits = 0;
					for (int i = 0; i < 32; i++)
					{
						if (diginGroups[i])
						{
							bits |= (1<<i);
							property += "1";
						}
						else
							property += "0";
					}

					if (!property.equals(props.getProperty("DIGITALINPUT.UNITS")))
					{
						props.put("DIGITALINPUT.UNITS", property);
						props.put("DIGITALINPUT.HEIGHT", 0);
						props.put("DIGITALINPUT.WIDTH", 0);
						if (digitalInputWindow != null)
						{
							boolean wasVisible = digitalInputWindow.isVisible();
							digitalInputWindow.dispose();
							digitalInputWindow = null;
							if (wasVisible)
								digitalInputWindow = new DigitalInputWindow();
						}
						try
						{
							hcs.selectDigitalInputsToDisplay(bits);
						}
						catch (HCSException ex)
						{
							Error("Failed to select digital input groups\n" +
								"for display.\n" + ex.getMessage());
						}
					}
				}
			});
			c.gridx = 1; c.gridy = 9;
			c.gridwidth = 1; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			gridbag.setConstraints(button, c);
			panel.add(button);
			pane.addTab("DigIn", null, panel, "Select Digital Input Groups");

			// Create the digital outputs pane.
			gridbag = new GridBagLayout();
			panel = new JPanel(gridbag);
			ActionListener digOutListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// Toggle the bit appropriately.
					JRadioButton b = (JRadioButton)e.getSource();
					digoutGroups[parseInt(e.getActionCommand())] = b.isSelected() ? true : false;
				}
			};
			chars = props.getProperty("DIGITALOUTPUT.UNITS",
				DEFAULTDIGINOUT) .toCharArray();

			// Initialize the radio buttons.
			c.anchor = GridBagConstraints.WEST;
			for (int i = 0; i < 32; i++)
			{
				radioButton = new JRadioButton(8*i + "-" + (8*i+7)); 
				radioButton.setActionCommand(String.valueOf(i));
				radioButton.addActionListener(digOutListener);
				if (chars[i] == '1')
				{
					radioButton.setSelected(true);
					digoutGroups[i] = true;
				}
				else
					digoutGroups[i] = false;
				c.gridx = (i&3); c.gridy = (i>>2);
				c.gridwidth = 1; c.gridheight = 1;
				c.weightx = 0.0; c.weighty = 0.0;
				gridbag.setConstraints(radioButton, c);
				panel.add(radioButton);
			}

			button = new JButton("Select");
			button.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					String property = new String("");
					int bits = 0;
					for (int i = 0; i < 32; i++)
					{
						if (digoutGroups[i])
						{
							bits |= (1<<i);
							property += "1";
						}
						else
							property += "0";
					}

					if (!property.equals(props.getProperty("DIGITALOUTPUT.UNITS")))
					{
						props.put("DIGITALOUTPUT.UNITS", property);
						props.put("DIGITALOUTPUT.HEIGHT", 0);
						props.put("DIGITALOUTPUT.WIDTH", 0);
						if (digitalOutputWindow != null)
						{
							boolean wasVisible = digitalOutputWindow.isVisible();
							digitalOutputWindow.dispose();
							digitalOutputWindow = null;
							if (wasVisible)
								digitalOutputWindow = new DigitalOutputWindow();
						}
						try
						{
							hcs.selectDigitalOutputsToDisplay(bits);
						}
						catch (HCSException ex)
						{
							Error("Failed to select digital output groups\n" +
								"for display.\n" + ex.getMessage());
						}
					}
				}
			});
			c.gridx = 1; c.gridy = 9;
			c.gridwidth = 1; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			gridbag.setConstraints(button, c);
			panel.add(button);
			pane.addTab("DigOut", null, panel, "Select Digital Output Groups");

			// Create the netbits pane.
			gridbag = new GridBagLayout();
			panel = new JPanel(gridbag);
			ActionListener netbitsListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// Toggle the bit appropriately.
					JRadioButton b = (JRadioButton)e.getSource();
					netbitGroups[parseInt(e.getActionCommand())] = b.isSelected() ? true : false;
				}
			};
			chars = props.getProperty("NETBITS.UNITS",
				DEFAULTNETBITS) .toCharArray();

			// Initialize the radio buttons.
			c.anchor = GridBagConstraints.WEST;
			for (int i = 0; i < 40; i++)
			{
				radioButton = new JRadioButton(8*i + "-" + (8*i+7)); 
				radioButton.setActionCommand(String.valueOf(i));
				radioButton.addActionListener(netbitsListener);
				if (chars[i] == '1')
				{
					radioButton.setSelected(true);
					netbitGroups[i] = true;
				}
				else
					netbitGroups[i] = false;
				c.gridx = (i&3); c.gridy = (i>>2);
				c.gridwidth = 1; c.gridheight = 1;
				c.weightx = 0.0; c.weighty = 0.0;
				gridbag.setConstraints(radioButton, c);
				panel.add(radioButton);
			}

			button = new JButton("Select");
			button.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					String property = new String("");
					long bits = 0;
					for (int i = 0; i < 40; i++)
					{
						if (netbitGroups[i])
						{
							bits |= (1<<i);
							property += "1";
						}
						else
							property += "0";
					}

					if (!property.equals(props.getProperty("NETBITS.UNITS")))
					{
						props.put("NETBITS.UNITS", property);
						props.put("NETBITS.HEIGHT", 0);
						props.put("NETBITS.WIDTH", 0);
						if (netbitsWindow != null)
						{
							boolean wasVisible = netbitsWindow.isVisible();
							netbitsWindow.dispose();
							netbitsWindow = null;
							if (wasVisible)
								netbitsWindow = new NetbitsWindow();
						}
						try
						{
							hcs.selectNetbitsToDisplay(bits);
						}
						catch (HCSException ex)
						{
							Error("Failed to select netbit groups\n" +
								"for display.\n" + ex.getMessage());
						}
					}
				}
			});
			c.gridx = 1; c.gridy = 11;
			c.gridwidth = 1; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			gridbag.setConstraints(button, c);
			panel.add(button);
			pane.addTab("Netbit", null, panel, "Select Netbits Groups to Display");

			// Create the analog inputs pane.
			gridbag = new GridBagLayout();
			panel = new JPanel(gridbag);
			ActionListener analogInListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// Toggle the bit appropriately.
					JRadioButton b = (JRadioButton)e.getSource();
					analoginGroups[parseInt(e.getActionCommand())] = b.isSelected() ? true : false;
				}
			};
			chars = props.getProperty("ANALOGINPUT.UNITS",
				"111111111111111110000000") .toCharArray();

			// Initialize the radio buttons.
			c.anchor = GridBagConstraints.WEST;
			for (int i = 0; i < 24; i++)
			{
				radioButton = new JRadioButton(8*i + "-" + (8*i+7)); 
				radioButton.setActionCommand(String.valueOf(i));
				radioButton.addActionListener(analogInListener);
				if (chars[i] == '1')
				{
					radioButton.setSelected(true);
					analoginGroups[i] = true;
				}
				else
					analoginGroups[i] = false;
				c.gridx = (i&3); c.gridy = (i>>2);
				c.gridwidth = 1; c.gridheight = 1;
				c.weightx = 0.0; c.weighty = 0.0;
				gridbag.setConstraints(radioButton, c);
				panel.add(radioButton);
			}

			button = new JButton("Select");
			button.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					String property = new String("");
					int bits = 0;
					for (int i = 0; i < 24; i++)
					{
						if (analoginGroups[i])
						{
							bits |= (1<<i);
							property += "1";
						}
						else
							property += "0";
					}

					if (!property.equals(props.getProperty("ANALOGINPUT.UNITS")))
					{
						props.put("ANALOGINPUT.UNITS", property);
						props.put("ANALOGINPUT.HEIGHT", 0);
						props.put("ANALOGINPUT.WIDTH", 0);
						if (analogInputWindow != null)
						{
							boolean wasVisible = analogInputWindow.isVisible();
							analogInputWindow.dispose();
							analogInputWindow = null;
							if (wasVisible)
								analogInputWindow = new AnalogInputWindow();
						}
						try
						{
							hcs.selectAnalogInputsToDisplay(bits);
						}
						catch (HCSException ex)
						{
							Error("Failed to select analog input groups\n" +
								"for display.\n" + ex.getMessage());
						}
					}
				}
			});
			c.gridx = 1; c.gridy = 7;
			c.gridwidth = 1; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			gridbag.setConstraints(button, c);
			panel.add(button);
			pane.addTab("AnalogIn", null, panel, "Select Analog Input Groups");

			// Create the analog outputs pane.
			gridbag = new GridBagLayout();
			panel = new JPanel(gridbag);
			ActionListener analogOutListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// Toggle the bit appropriately.
					JRadioButton b = (JRadioButton)e.getSource();
					analogoutGroups[parseInt(e.getActionCommand())] =
						b.isSelected() ? true : false;
				}
			};
			chars = props.getProperty("ANALOGOUTPUT.UNITS",
				"11111111") .toCharArray();

			// Initialize the radio buttons.
			c.anchor = GridBagConstraints.WEST;
			for (int i = 0; i < 8; i++)
			{
				radioButton = new JRadioButton(8*i + "-" + (8*i+7)); 
				radioButton.setActionCommand(String.valueOf(i));
				radioButton.addActionListener(analogOutListener);
				if (chars[i] == '1')
				{
					radioButton.setSelected(true);
					analogoutGroups[i] = true;
				}
				else
					analogoutGroups[i] = false;
				c.gridx = (i&3); c.gridy = (i>>2);
				c.gridwidth = 1; c.gridheight = 1;
				c.weightx = 0.0; c.weighty = 0.0;
				gridbag.setConstraints(radioButton, c);
				panel.add(radioButton);
			}

			button = new JButton("Select");
			button.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					String property = new String("");
					int bits = 0;
					for (int i = 0; i < 8; i++)
					{
						if (analogoutGroups[i])
						{
							bits |= (1<<i);
							property += "1";
						}
						else
							property += "0";
					}

					if (!property.equals(props.getProperty("ANALOGOUTPUT.UNITS")))
					{
						props.put("ANALOGOUTPUT.UNITS", property);
						props.put("ANALOGOUTPUT.HEIGHT", 0);
						props.put("ANALOGOUTPUT.WIDTH", 0);
						if (analogOutputWindow != null)
						{
							boolean wasVisible = analogOutputWindow.isVisible();
							analogOutputWindow.dispose();
							analogOutputWindow = null;
							if (wasVisible)
								analogOutputWindow = new AnalogOutputWindow();
						}
						try
						{
							hcs.selectAnalogOutputsToDisplay(bits);
						}
						catch (HCSException ex)
						{
							Error("Failed to select analog output groups\n" +
								"for display.\n" + ex.getMessage());
						}
					}
				}
			});
			c.gridx = 1; c.gridy = 3;
			c.gridwidth = 1; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			gridbag.setConstraints(button, c);
			panel.add(button);
			pane.addTab("AnalogOut", null, panel, "Select Analog Output Groups");

			// Create the network modules pane.
			gridbag = new GridBagLayout();
			panel = new JPanel(gridbag);
			ActionListener networkListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// Toggle the bit appropriately.
					JRadioButton b = (JRadioButton)e.getSource();
					networkModules[parseInt(e.getActionCommand())]
						= b.isSelected() ? true : false;
				}
			};
			chars = props.getProperty(NETWORKMODULES, "111111")
				.toCharArray();

			// Initialize the radio buttons.
			radioButton = new JRadioButton("PL");
			radioButton.setActionCommand("0");
			radioButton.addActionListener(networkListener);
			if (chars[0] == '1')
			{
				radioButton.setSelected(true);
				networkModules[0] = true;
			}
			else
				networkModules[0] = false;
			c.gridx = 0; c.gridy = 0;
			c.gridwidth = 1; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			gridbag.setConstraints(radioButton, c);
			panel.add(radioButton);
			radioButton = new JRadioButton("MCIR");
			radioButton.setActionCommand("1");
			radioButton.addActionListener(networkListener);
			if (chars[1] == '1')
			{
				radioButton.setSelected(true);
				networkModules[1] = true;
			}
			else
				networkModules[1] = false;
			c.gridx = 0; c.gridy = 1;
			gridbag.setConstraints(radioButton, c);
			panel.add(radioButton);
			radioButton = new JRadioButton("LCD");
			radioButton.setActionCommand("2");
			radioButton.addActionListener(networkListener);
			if (chars[2] == '1')
			{
				radioButton.setSelected(true);
				networkModules[2] = true;
			}
			else
				networkModules[2] = false;
			c.gridx = 0; c.gridy = 2;
			gridbag.setConstraints(radioButton, c);
			panel.add(radioButton);
			radioButton = new JRadioButton("DIO");
			radioButton.setActionCommand("3");
			radioButton.addActionListener(networkListener);
			if (chars[3] == '1')
			{
				radioButton.setSelected(true);
				networkModules[3] = true;
			}
			else
				networkModules[3] = false;
			c.gridx = 0; c.gridy = 3;
			gridbag.setConstraints(radioButton, c);
			panel.add(radioButton);
			radioButton = new JRadioButton("ADIO");
			radioButton.setActionCommand("4");
			radioButton.addActionListener(networkListener);
			if (chars[4] == '1')
			{
				radioButton.setSelected(true);
				networkModules[4] = true;
			}
			else
				networkModules[4] = false;
			c.gridx = 0; c.gridy = 4;
			gridbag.setConstraints(radioButton, c);
			panel.add(radioButton);
			radioButton = new JRadioButton("DIO+");
			radioButton.setActionCommand("5");
			radioButton.addActionListener(networkListener);
			if (chars[5] == '1')
			{
				radioButton.setSelected(true);
				networkModules[5] = true;
			}
			else
				networkModules[5] = false;
			c.gridx = 0; c.gridy = 5;
			gridbag.setConstraints(radioButton, c);
			panel.add(radioButton);

			button = new JButton("Select");
			button.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					String property = new String("");
					int bits = 0;
					for (int i = 0; i < 6; i++)
					{
						if (networkModules[i])
						{
							bits |= (1<<i);
							property += "1";
						}
						else
							property += "0";
					}

					if (!property.equals(props.getProperty(NETWORKMODULES, "11111")))
					{
						props.put(NETWORKMODULES, property);
						props.put(NETWORKMODULES_HEIGHT, 0);
						props.put(NETWORKMODULES_WIDTH, 0);
						if (networkModulesWindow != null)
						{
							boolean wasVisible = networkModulesWindow.isVisible();
							networkModulesWindow.dispose();
							networkModulesWindow = null;
							if (wasVisible)
								networkModulesWindow = new NetworkModulesWindow();
						}
						try
						{
							hcs.selectNetModulesToDisplay(bits);
						}
						catch (HCSException ex)
						{
							Error("Failed to select network modules\n" +
								"for display.\n" + ex.getMessage());
						}
					}
				}
			});
			c.gridx = 0; c.gridy = 6;
			gridbag.setConstraints(button, c);
			panel.add(button);
			pane.addTab("NetMods", null, panel, "Select Network Modules");

			// Add panel for setting time.
			gridbag = new GridBagLayout();
			panel = new JPanel(gridbag);
			label = new JLabel("Time");
			c.gridx = 0; c.gridy = 0; c.gridwidth = 1; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0; c.insets = new Insets(5,5,5,5);
			gridbag.setConstraints(label, c);
			panel.add(label);

			final JComboBox timeFrequency = new JComboBox();
			timeFrequency.addItem("Off");
			timeFrequency.addItem("Every Second");
			timeFrequency.addItem("Every Minute");
			timeFrequency.setSelectedItem(props.getProperty(CONSOLE_TIME, "Off"));
			timeFrequency.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					JComboBox cb = (JComboBox)e.getSource();
					String selection = (String)cb.getSelectedItem();
					int freq = 2;
					if (selection.equals("Off")) freq = 0;
					else if (selection.equals("Every Second")) freq = 1;
					try
					{
						hcs.setTimeFrequency(freq);
						props.put(CONSOLE_TIME, selection);
						consoleWindow.enableTime(freq!=0, freq==1);
					}
					catch (HCSException ex)
					{
						Error("Failed to set time update frequency.\n" +
							ex.getMessage());
					}
				}
			});
			c.gridx = 1; c.gridy = 0; c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			gridbag.setConstraints(timeFrequency, c);
			panel.add(timeFrequency);
			pane.addTab("Time", null, panel, "Set Time Frequency");

			pane.setSelectedIndex(0);
			Container contentPane = getContentPane();
			contentPane.add(pane, BorderLayout.CENTER);
			pack();

			setLocation(
				props.getProperty(PREFS_POSX, 0),
				props.getProperty(PREFS_POSY, 0));
			setSize(
				props.getProperty(PREFS_WIDTH, 325),
				props.getProperty(PREFS_HEIGHT, 300));

			// Pack the frame then display it.
			addComponentListener( new ComponentListener() {
				public void componentMoved(ComponentEvent e) {
					Point pos = getLocation();
					props.put(PREFS_POSX, pos.x);
					props.put(PREFS_POSY, pos.y);
				}
				public void componentResized(ComponentEvent e) {
					Dimension size = getSize();
					props.put(PREFS_WIDTH, size.width);
					props.put(PREFS_HEIGHT, size.height);
				}
				public void componentShown(ComponentEvent e)  {}
				public void componentHidden(ComponentEvent e) {}
			});
			setVisible(true);
		}
		public void setVisible(boolean state)
		{
			// The user may input some invalid data then close the window
			// without hitting return. This information would be maintained
			// so instead when making the window visible we set the contents
			// to something valid.
			if (state)
				x10Field.setText(props.getProperty(X10HOUSECODES, AtoP));
			super.setVisible(state);
		}
	}

	private class CommandWindow extends JFrame
	{
		public CommandWindow()
		{
			// Some variables shared by the various panels.
			int i;
			GridBagConstraints c = new GridBagConstraints();
			JPanel panel = null;
			JLabel label = null;

			// Create the main pane.
			JTabbedPane pane = new JTabbedPane(JTabbedPane.TOP);

			// Create the Ports Pane.
			GridBagLayout gridbag = new GridBagLayout();
			panel = new JPanel();
			panel.setLayout(gridbag);

			label = new JLabel("House Code ");
			c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.2; c.weighty = 0.2;
			c.anchor = GridBagConstraints.WEST;
			gridbag.setConstraints(label, c);
			panel.add(label);
			final JComboBox x10HouseCode = new JComboBox();
			for (i = 0; i < 16; i++)
				x10HouseCode.addItem(String.valueOf((char)('A'+i)));
			c.gridx = 2; c.gridy = 0; c.gridwidth = 1; c.gridheight = 1;
			c.weightx = 0.2; c.weighty = 0.2;
			gridbag.setConstraints(x10HouseCode, c);
			panel.add(x10HouseCode);

			label = new JLabel("Module");
			c.gridx = 0; c.gridy = 1; c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.2; c.weighty = 0.2;
			gridbag.setConstraints(label, c);
			panel.add(label);
			final JComboBox x10Module = new JComboBox();
			for (i = 1; i <= 16; i++)
				x10Module.addItem(String.valueOf(i));
			c.gridx = 2; c.gridy = 1; c.gridwidth = 1; c.gridheight = 1;
			c.weightx = 0.2; c.weighty = 0.2;
			gridbag.setConstraints(x10Module, c);
			panel.add(x10Module);

			label = new JLabel("Level");
			c.gridx = 0; c.gridy = 3; c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			gridbag.setConstraints(label, c);
			panel.add(label);
			final JSlider x10Slider = new JSlider(JSlider.HORIZONTAL,1,31,31);
			x10Slider.setMajorTickSpacing(10);
			x10Slider.setMinorTickSpacing(1);
			x10Slider.setPaintTicks(true);
			x10Slider.setPaintLabels(true);
			x10Slider.setEnabled(false);
			// Set minimum size. Necessary because of an apparent bug in
			// tabbed panes which improperly resizes panes if there are
			// two rows of tabs.
			x10Slider.setMinimumSize(x10Slider.getPreferredSize());
			c.gridx = 2; c.gridy = 3; c.gridwidth = 4; c.gridheight = 3;
			c.weightx = 0.0; c.weighty = 0.0;
			gridbag.setConstraints(x10Slider, c);
			panel.add(x10Slider);

			label = new JLabel("Command");
			c.gridx = 0; c.gridy = 2; c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.2; c.weighty = 0.2;
			gridbag.setConstraints(label, c);
			panel.add(label);
			final JComboBox x10Command = new JComboBox();
			x10Command.addItem("All Units Off");
			x10Command.addItem("All Lights On");
			x10Command.addItem("On");
			x10Command.addItem("Off");
			x10Command.addItem("Dim");
			x10Command.addItem("Bright");
			x10Command.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					JComboBox cb = (JComboBox)e.getSource();
					String selection = (String)cb.getSelectedItem();
					selection = selection.toLowerCase();
					if (selection.equals("dim") || selection.equals("bright"))
						x10Slider.setEnabled(true);
					else
						x10Slider.setEnabled(false);
				}
			});
			x10Command.setSelectedItem("On");
			c.gridx = 2; c.gridy = 2; c.gridwidth = 3; c.gridheight = 1;
			c.weightx = 0.2; c.weighty = 0.2;
			gridbag.setConstraints(x10Command, c);
			panel.add(x10Command);

			JButton button = new JButton("Go!");
			button.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						String ts = (String)x10HouseCode.getSelectedItem();
						int houseCode = (int)(ts.charAt(0) - 'A' + 1);
						ts = (String)x10Module.getSelectedItem();
						int module = parseInt(ts);
						int level = (int)x10Slider.getValue();
						int command;
						ts = (String)x10Command.getSelectedItem();
						ts = ts.toLowerCase();
						if (ts.indexOf("units") > 0) command = 0;
						else if (ts.indexOf("lights") > 0) command = 1;
						else if (ts.equals("on")) command = 2;
						else if (ts.equals("off")) command = 3;
						else if (ts.equals("dim")) command = 4;
						else command = 5;
						hcs.setX10Module(houseCode, module, command,
							level);
					}
					catch (HCSException ex)
					{
						Error("Failed to send X10 command.\n" +
							ex.getMessage());
					}
				}
			});
			c.gridx = 2; c.gridy = 6; c.gridwidth = 1; c.gridheight = 2;
			c.weightx = 0.2; c.weighty = 0.2;
			gridbag.setConstraints(button, c);
			panel.add(button);

			// Set minimum size. Necessary because of an apparent bug in
			// tabbed panes which improperly resizes panes if there are
			// two rows of tabs. This affects the rendering of sliders.
			Dimension min = panel.getMinimumSize();
			min.height += (min.height/4);	// Add 25%
			panel.setMinimumSize(min);
			panel.setPreferredSize(min);
			pane.addTab("X10", null, panel, "Send X10 Commands");

			// Create the message command pane.
			panel = new JPanel(new BorderLayout());
			label = new JLabel("Please enter a message string:");
			label.setHorizontalAlignment(JLabel.LEFT);
			panel.add(label, BorderLayout.NORTH);

			final JTextArea networkString = new JTextArea();
			panel.add(networkString, BorderLayout.CENTER);

			button = new JButton("Go!");
			button.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						hcs.sendNetworkString(networkString.getText());
					}
					catch (HCSException ex)
					{
						Error("Failed to send network string.\n" +
							ex.getMessage());
					}
				}
			});
			panel.add(button, BorderLayout.SOUTH);
			pane.addTab("Message", null, panel, "Send a Network String");
			
			// Create the voice command pane.
			panel = new JPanel(new BorderLayout());
			label = new JLabel("Please enter a voice string:");
			label.setHorizontalAlignment(JLabel.LEFT);
			panel.add(label, BorderLayout.NORTH);

			final JTextArea voiceString = new JTextArea();
			panel.add(voiceString, BorderLayout.CENTER);

			button = new JButton("Go!");
			button.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						hcs.sendVoiceString(voiceString.getText());
					}
					catch (HCSException ex)
					{
						Error("Failed to send voice string.\n" +
							ex.getMessage());
					}
				}
			});
			panel.add(button, BorderLayout.SOUTH);
			pane.addTab("Voice", null, panel, "Send a Voice String");

			// Create the Digital Input command pane.
			gridbag = new GridBagLayout();
			panel = new JPanel();
			panel.setLayout(gridbag);

			label = new JLabel("Input (0-255)");
			c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			c.anchor = GridBagConstraints.WEST;
			gridbag.setConstraints(label, c);
			panel.add(label);
			label = new JLabel("State");
			c.gridx = 0; c.gridy = 1; c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			gridbag.setConstraints(label, c);
			panel.add(label);

			final JTextField inputField = new JTextField(4);
			c.gridx = 2; c.gridy = 0; c.gridwidth = 1; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			gridbag.setConstraints(inputField, c);
			panel.add(inputField);

			final JComboBox inputState = new JComboBox();
			inputState.addItem("On");
			inputState.addItem("Off");
			inputState.addItem("Transparent");
			inputState.setSelectedItem("On");
			c.gridx = 2; c.gridy = 1; c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			gridbag.setConstraints(inputState, c);
			panel.add(inputState);

			button = new JButton("Go!");
			button.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						int input = parseInt(inputField.getText());
						String ts = (String)inputState.getSelectedItem();
						int state;
						if (ts.equals("Off")) state = 0;
						else if (ts.equals("On")) state = 1;
						else state = 2;
						hcs.setDigitalInputState(input, state);
					}
					catch (HCSException ex)
					{
						Error("Failed to set digital input.\n" +
							ex.getMessage());
					}
				}
			});
			c.gridx = 2; c.gridy = 2; c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			c.anchor = GridBagConstraints.NORTHWEST;
			gridbag.setConstraints(button, c);
			panel.add(button);
			pane.addTab("DigIn", null, panel, "Set a Digital Input");

			// Create the Digital Output command pane.
			gridbag = new GridBagLayout();
			panel = new JPanel();
			panel.setLayout(gridbag);

			label = new JLabel("Output (0-255)");
			c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			c.anchor = GridBagConstraints.WEST;
			gridbag.setConstraints(label, c);
			panel.add(label);
			label = new JLabel("State");
			c.gridx = 0; c.gridy = 1; c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			gridbag.setConstraints(label, c);
			panel.add(label);

			final JTextField outputField = new JTextField(4);
			c.gridx = 2; c.gridy = 0; c.gridwidth = 1; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			gridbag.setConstraints(outputField, c);
			panel.add(outputField);

			final JComboBox outputState = new JComboBox();
			outputState.addItem("On");
			outputState.addItem("Off");
			outputState.setSelectedItem("On");
			c.gridx = 2; c.gridy = 1; c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			gridbag.setConstraints(outputState, c);
			panel.add(outputState);

			button = new JButton("Go!");
			button.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						int output = parseInt(outputField.getText());
						String ts = (String)outputState.getSelectedItem();
						hcs.setDigitalOutputState(output,
							ts.equals("On"));
					}
					catch (HCSException ex)
					{
						Error("Failed to set digital output.\n" +
							ex.getMessage());
					}
				}
			});
			c.gridx = 2; c.gridy = 2; c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			c.anchor = GridBagConstraints.NORTHWEST;
			gridbag.setConstraints(button, c);
			panel.add(button);
			pane.addTab("DigOut", null, panel, "Set a Digital Output");

			// Create the Netbit command pane.
			gridbag = new GridBagLayout();
			panel = new JPanel();
			panel.setLayout(gridbag);

			label = new JLabel("Netbit (0-319)");
			c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			c.anchor = GridBagConstraints.WEST;
			gridbag.setConstraints(label, c);
			panel.add(label);
			label = new JLabel("State");
			c.gridx = 0; c.gridy = 1; c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			gridbag.setConstraints(label, c);
			panel.add(label);

			final JTextField netbitField = new JTextField(4);
			c.gridx = 2; c.gridy = 0; c.gridwidth = 1; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			gridbag.setConstraints(netbitField, c);
			panel.add(netbitField);

			final JComboBox netbitState = new JComboBox();
			netbitState.addItem("On");
			netbitState.addItem("Off");
			netbitState.setSelectedItem("On");
			c.gridx = 2; c.gridy = 1; c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			gridbag.setConstraints(netbitState, c);
			panel.add(netbitState);

			button = new JButton("Go!");
			button.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						int netbit = parseInt(netbitField.getText());
						String ts = (String)netbitState.getSelectedItem();
						boolean state = false;
						if (ts.equals("On")) state = true;
						hcs.setNetbitState(netbit, state);
					}
					catch (HCSException ex)
					{
						Error("Failed to set netbit.\n" +
							ex.getMessage());
					}
				}
			});
			c.gridx = 2; c.gridy = 2; c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			c.anchor = GridBagConstraints.NORTHWEST;
			gridbag.setConstraints(button, c);
			panel.add(button);
			pane.addTab("Netbit", null, panel, "Set a Netbit");

			// Create the ADC command pane.
			gridbag = new GridBagLayout();
			panel = new JPanel();
			panel.setLayout(gridbag);

			label = new JLabel("Number (0-135)");
			c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			c.anchor = GridBagConstraints.WEST;
			gridbag.setConstraints(label, c);
			panel.add(label);
			label = new JLabel("Value (0-4095)");
			c.gridx = 0; c.gridy = 1; c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			gridbag.setConstraints(label, c);
			panel.add(label);

			final JTextField adcNumber = new JTextField(4);
			c.gridx = 2; c.gridy = 0; c.gridwidth = 1; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			gridbag.setConstraints(adcNumber, c);
			panel.add(adcNumber);
			final JTextField adcValue = new JTextField(4);
			// ADCvalueField.setText(String.valueOf(ADCvalue));
			c.gridx = 2; c.gridy = 1; c.gridwidth = 1; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			gridbag.setConstraints(adcValue, c);
			panel.add(adcValue);

			JComboBox adcState = new JComboBox();
			adcState.addItem("Active");
			adcState.addItem("Transparent");
			adcState.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					JComboBox cb = (JComboBox)e.getSource();
					String selection = (String)cb.getSelectedItem();
					selection = selection.toLowerCase();
					adcValue.setEnabled(selection.equals("active"));
				}
			});
			adcState.setSelectedItem("Active");
			c.gridx = 3; c.gridy = 1; c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			gridbag.setConstraints(adcState, c);
			panel.add(adcState);

			button = new JButton("Go!");
			button.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						int adc = parseInt(adcNumber.getText());
						int value = 4096;
						if (adcValue.isEnabled())
							value = parseInt(adcValue.getText());
						hcs.setAnalogInputValue(adc, value);
					}
					catch (HCSException ex)
					{
						Error("Failed to set ADC.\n" + ex.getMessage());
					}
				}
			});
			c.gridx = 2; c.gridy = 2; c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			c.anchor = GridBagConstraints.NORTHWEST;
			gridbag.setConstraints(button, c);
			panel.add(button);
			pane.addTab("ADC", null, panel, "Set an ADC");

			// Create the DAC command pane.
			gridbag = new GridBagLayout();
			panel = new JPanel();
			panel.setLayout(gridbag);

			label = new JLabel("Number (0-31)");
			c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			c.anchor = GridBagConstraints.WEST;
			gridbag.setConstraints(label, c);
			panel.add(label);
			label = new JLabel("Value (0-4095)");
			c.gridx = 0; c.gridy = 1; c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			gridbag.setConstraints(label, c);
			panel.add(label);

			final JTextField dacNumber = new JTextField(4);
			c.gridx = 2; c.gridy = 0; c.gridwidth = 1; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			gridbag.setConstraints(dacNumber, c);
			panel.add(dacNumber);
			final JTextField dacValue = new JTextField(4);
			c.gridx = 2; c.gridy = 1; c.gridwidth = 1; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			gridbag.setConstraints(dacValue, c);
			panel.add(dacValue);

			button = new JButton("Go!");
			button.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						int dac = parseInt(dacNumber.getText());
						int value = parseInt(dacValue.getText());
						hcs.setAnalogOutputValue(dac, value);
					}
					catch (HCSException ex)
					{
						Error("Failed to set DAC.\n" + ex.getMessage());
					}
				}
			});
			c.gridx = 2; c.gridy = 2; c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			c.anchor = GridBagConstraints.NORTHWEST;
			gridbag.setConstraints(button, c);
			panel.add(button);
			pane.addTab("DAC", null, panel, "Set a DAC");

			// Add Set Time command tab.
			gridbag = new GridBagLayout();
			panel = new JPanel();
			panel.setLayout(gridbag);

			button = new JButton("Set Time");
			button.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						hcs.setTime();
					}
					catch (HCSException ex)
					{
						Error("Failed to set time.\n" + ex.getMessage());
					}
				}
			});
			c.gridx = 0; c.gridy = 0; c.gridwidth = 1; c.gridheight = 1;
			c.weightx = 0.0; c.weighty = 0.0;
			c.anchor = GridBagConstraints.CENTER;
			gridbag.setConstraints(button, c);
			panel.add(button);
			pane.addTab("Time", null, panel, "Set Time");

			pane.setSelectedIndex(0);

			Container contentPane = getContentPane();
			contentPane.add(pane, BorderLayout.CENTER);
			pack();

			setLocation(
				props.getProperty(COMMAND_POSX, 0),
				props.getProperty(COMMAND_POSY, 0));
			setSize(
				props.getProperty(COMMAND_WIDTH, 275),
				props.getProperty(COMMAND_HEIGHT, 190));

			// Pack the frame then display it.
			addComponentListener( new ComponentListener() {
				public void componentMoved(ComponentEvent e) {
					Point pos = getLocation();
					props.put(COMMAND_POSX, pos.x);
					props.put(COMMAND_POSY, pos.y);
				}
				public void componentResized(ComponentEvent e) {
					Dimension size = getSize();
					props.put(COMMAND_WIDTH, size.width);
					props.put(COMMAND_HEIGHT, size.height);
				}
				public void componentShown(ComponentEvent e)  {}
				public void componentHidden(ComponentEvent e) {}
			});
			setVisible(true);
		}
	}

	// Some useful utility functions.
	protected int parseInt(String str)
	{
		// Some of the classes and functions above need to convert a string to
		// an int, knowing that the string really is an int, so we ignore
		// the exception, which should never happen in these cases.
		int val = -1;
		try
		{
			val = Integer.parseInt(str);
		}
		catch(Exception e) {}
		return val;
	}
}

