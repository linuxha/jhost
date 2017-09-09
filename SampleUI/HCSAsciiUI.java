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

import java.io.*;
import java.text.*;
import java.util.*;

public class HCSAsciiUI implements HCSEventListener
{
	protected HCSPortManager portManager = null;
	private boolean hideUpdates = false;

	// Which port have we opened?
	private String portName = "no port";

	public HCSAsciiUI(HCSPortManager portManager)
	{
		if ( null == portManager )
			return;

		this.portManager = portManager;
		portManager.addEventListener(HCSEvent.ALL, this);

		try { PortMenu(); }
		catch (HCSException e)
		{
			System.out.println("Failed (" + e.getMessage() + ").");
		}

		while (true)
		{
			try
			{
				System.in.read();
			}
			catch (IOException e) {}

			mainMenu();
		}
	}

	private void mainMenu()
	{
		hideUpdates = true;

		boolean success = false;
		do
		{
			System.out.println("\nHCS II - Java Host - " + portName );
			System.out.println("Please select a command from the following:");
			System.out.println("1) Port Menu");
			System.out.println("2) Time Menu");
			System.out.println("3) Miscellaneous Menu");
			System.out.println("4) X10 Menu");
			System.out.println("5) Log Menu");
			System.out.println("6) String Menu");
			System.out.println("7) IO Menu");
			System.out.println("8) Variable Menu");
			System.out.println("9) Done - Return to normal updates");
			System.out.println("0) EXIT");

			try
			{
				switch ( inputInteger("Selection? ", 0, 9) )
				{
					case 1: PortMenu(); break;
					case 2: TimeMenu(); break;
					case 3: MiscMenu(); break;
					case 4: X10Menu(); break;
					case 5: LogMenu(); break;
					case 6: StringMenu(); break;
					case 7: IOMenu(); break;
					case 8: VariableMenu(); break;
					case 9: success = true; break;
					case 0:
					{
						String response = input("Exit? ").toLowerCase();
						if (response != null && response.charAt(0) == 'y')
							exit();
						break;
					}
				}
			}
			catch (HCSException e)
			{
				System.out.println("Failed (" + e.getMessage() + ").");
			}
		} while (!success);

		hideUpdates = false;
	}

	private void PortMenu() throws HCSException
	{
		Vector portList = portManager.getPortList();

		System.out.println("\nHCS II - Java Host - " + portName );
		System.out.println("Please select a port from the following list:");
		for (int i = 0; i < portList.size(); i++)
			System.out.println(i+1 + ") " + (String)portList.elementAt(i));

		int selection = inputInteger("Selection? ", 1, portList.size());
		if ( selection < 1 )
		{
			System.out.println("Failed.");
			return;
		}

		setPort((String)portList.elementAt(selection-1));
	}

	private void IOMenu() throws HCSException
	{
		System.out.println("\nPlease select a command from the following:");
		System.out.println("1) Digital Input Menu");
		System.out.println("2) Digital Output Menu");
		System.out.println("3) Analog Input Menu");
		System.out.println("4) Analog Output Menu");
		System.out.println("5) Netbit Menu");
		System.out.println("4) Network Module Menu");
		System.out.println("0) Main menu");

		switch ( inputInteger("Selection? ", 0, 4) )
		{
			case 1: DigitalInputMenu(); break;
			case 2: DigitalOutputMenu(); break;
			case 3: ADCMenu(); break;
			case 4: DACMenu(); break;
			case 5: NetBitMenu(); break;
			case 6: NetModuleMenu(); break;
		}
	}

	private void MiscMenu() throws HCSException
	{
		System.out.println("\nPlease select a command from the following:");
		System.out.println("1) Load");
		System.out.println("2) Info");
		System.out.println("3) Reset");
		System.out.println("0) Main menu");

		switch ( inputInteger("Selection? ", 0, 3) )
		{
			case 1: load(); break;
			case 2: portManager.getInfo(); break;
			case 3: portManager.doReset(); break;
		}
	}

	private void X10Menu() throws HCSException
	{
		System.out.println("\nPlease select a command from the following:");
		System.out.println("1) Select X10 Display");
		System.out.println("2) Get X10 Status");
		System.out.println("3) Set X10 Module");
		System.out.println("0) Main menu");

		switch ( inputInteger("Selection? ", 0, 3) )
		{
			case 1: selectX10Display(); break;
			case 2: getX10Status(); break;
			case 3: setX10Module(); break;
		}
	}

	private void TimeMenu() throws HCSException
	{
		System.out.println("\nPlease select a command from the following:");
		System.out.println("1) Set Time");
		System.out.println("2) Set Time Frequency");
		System.out.println("3) Get Time");
		System.out.println("0) Main menu");

		switch ( inputInteger("Selection? ", 0, 3) )
		{
			case 1: setTime(); break;
			case 2: setTimeFrequency(); break;
			case 3:
				Calendar cal = portManager.getTime();
				System.out.println(
					DateFormat.getDateTimeInstance().format(cal.getTime()));
				break;
		}
	}

	private void LogMenu() throws HCSException
	{
		System.out.println("\nPlease select a command from the following:");
		System.out.println("1) Clear Log");
		System.out.println("2) Get Log Size");
		System.out.println("3) Get Log");
		System.out.println("4) Set Log");
		System.out.println("0) Main menu");

		switch ( inputInteger("Selection? ", 0, 2) )
		{
			case 1: portManager.clearLog(); break;
			case 2:
				System.out.println("Log size = " + portManager.getLogSize() +
					"bytes");
				break;
			case 3: getLog(); break;
			case 4: setLog(); break;
		}
	}

	private void getLog() throws HCSException
	{
		String filename = input("Log file name? ");
		if ( filename.equals("") )
			return;

		FileOutputStream fos = null;

		try
		{
			fos = new FileOutputStream(filename);
			fos.write(portManager.getLog());
		}
		catch (FileNotFoundException f)
		{
			throw new HCSException(HCSException.BADOPEN);
		}
		catch (IOException e)
		{
			throw new HCSException(HCSException.BADWRITE);
		}

		try
		{
			fos.close();
		}
		catch (IOException e2)
		{
			throw new HCSException(HCSException.BADCLOSE);
		}
	}

	private void StringMenu() throws HCSException
	{
		System.out.println("\nPlease select a command from the following:");
		System.out.println("1) Send Network String");
		System.out.println("2) Send Voice String");
		System.out.println("0) Main menu");

		switch ( inputInteger("Selection? ", 0, 2) )
		{
			case 1: sendNetworkString(); break;
			case 2: sendVoiceString(); break;
		}
	}

	private void VariableMenu() throws HCSException
	{
		System.out.println("\nPlease select a command from the following:");
		System.out.println("1) Set Variable");
		System.out.println("2) Get Variable");
		System.out.println("0) Main menu");

		switch ( inputInteger("Selection? ", 0, 2) )
		{
			case 1: setVariable(); break;
			case 2: getVariable(); break;
		}
	}

	private void NetModuleMenu() throws HCSException
	{
		System.out.println("\nPlease select a command from the following:");
		System.out.println("1) Select Network Module Display");
		System.out.println("2) Get Network Module Status");
		System.out.println("0) Main menu");

		switch ( inputInteger("Selection? ", 0, 2) )
		{
			case 1: selectNetModulesToDisplay(); break;
			case 2: getNetModuleStatus(); break;
		}
	}

	private void DigitalOutputMenu() throws HCSException
	{
		System.out.println("\nPlease select a command from the following:");
		System.out.println("1) Select Digital Output Display");
		System.out.println("2) Get Digital Output Status");
		System.out.println("3) Set Digital Output State");
		System.out.println("0) Main menu");

		switch ( inputInteger("Selection? ", 0, 3) )
		{
			case 1: selectDigitalOutputsToDisplay(); break;
			case 2: getDigitalOutputState(); break;
			case 3: setDigitalOutputState(); break;
		}
	}

	private void DigitalInputMenu() throws HCSException
	{
		System.out.println("\nPlease select a command from the following:");
		System.out.println("1) Select Digital Input Display");
		System.out.println("2) Get Digital Input Status");
		System.out.println("3) Set Digital Input State");
		System.out.println("0) Main menu");

		switch ( inputInteger("Selection? ", 0, 3) )
		{
			case 1: selectDigitalInputsToDisplay(); break;
			case 2: getDigitalInputState(); break;
			case 3: setDigitalInputState(); break;
		}
	}

	private void DACMenu() throws HCSException
	{
		System.out.println("\nPlease select a command from the following:");
		System.out.println("1) Select DAC Display");
		System.out.println("2) Get DAC Status");
		System.out.println("3) Set DAC State");
		System.out.println("0) Main menu");

		switch ( inputInteger("Selection? ", 0, 3) )
		{
			case 1: selectAnalogOutputsToDisplay(); break;
			case 2: getAnalogOutputValue(); break;
			case 3: setAnalogOutputValue(); break;
		}
	}

	private void ADCMenu() throws HCSException
	{
		System.out.println("\nPlease select a command from the following:");
		System.out.println("1) Select ADC Display");
		System.out.println("2) Get ADC Status");
		System.out.println("3) Set ADC State");
		System.out.println("0) Main menu");

		switch ( inputInteger("Selection? ", 0, 3) )
		{
			case 1: selectAnalogInputsToDisplay(); break;
			case 2: getAnalogInputValue(); break;
			case 3: setAnalogInputValue(); break;
		}
	}

	private void NetBitMenu() throws HCSException
	{
		System.out.println("\nPlease select a command from the following:");
		System.out.println("1) Select Netbit Display");
		System.out.println("2) Get Netbit Status");
		System.out.println("3) Set Netbit State");
		System.out.println("0) Main menu");

		switch ( inputInteger("Selection? ", 0, 3) )
		{
			case 1: selectNetbitsToDisplay(); break;
			case 2: getNetbitState(); break;
			case 3: setNetbitState(); break;
		}
	}

	private void setNetbitState() throws HCSException
	{
		int input = inputInteger("Netbit (0-319)? ", 0, 319);
		int state = inputInteger("State (0:OFF, 1:ON)? ", 0, 1);

		portManager.setNetbitState( input, state == 0 ? false : true );
	}

	private void getNetbitState() throws HCSException
	{
		int netbit = inputInteger("Netbit (0-319)? ", 0, 319);
		boolean state = portManager.getNetbitState(netbit);
		System.out.println("Netbit " + netbit + " = " + (state ? "ON" : "OFF"));
	}

	private void selectNetbitsToDisplay() throws HCSException
	{
		int input;
		long bits = 0;	// longs are always 64 bits in length.

		do
		{
			input = inputInteger("Select netbit banks to display (0-39, -1 when done): ", -1, 39);
			if ( input != -1 ) bits |= (1<<input);
		}
		while (input != -1);

		portManager.selectNetbitsToDisplay(bits);
	}

	private void getNetModuleStatus() throws HCSException
	{
		System.out.println("\nPlease select a network module from the following:");
		System.out.println("0) PL");
		System.out.println("1) MCIR");
		System.out.println("2) LCD");
		System.out.println("3) DIO");
		System.out.println("4) ADIO");
		System.out.println("5) DIO+");
		int module = inputInteger("Choice? (0-5) ", 0, 5);
		System.out.println("\nPlease enter a unit:");
		int unit = inputInteger("Unit? (0-7) ", 0, 7);

		int result = portManager.getNetModuleStatus(module, unit);
		switch (module)
		{
			case 0: System.out.print("PL "); break;
			case 1: System.out.print("MCIR "); break;
			case 2: System.out.print("LCD "); break;
			case 3: System.out.print("DIO "); break;
			case 4: System.out.print("ADIO "); break;
			case 5: System.out.print("DIO+ "); break;
			default:
				System.out.print("UNKNOWN "); break;
		}

		System.out.println( unit + " reports " +
			(result==0 ? "timeout" :
			(result==1 ? "active" : "error")));
	}

	private void selectNetModulesToDisplay() throws HCSException
	{
		System.out.println("\nPlease select network modules from the following:");
		System.out.println("0) PL");
		System.out.println("1) MCIR");
		System.out.println("2) LCD");
		System.out.println("3) DIO");
		System.out.println("4) ADIO");
		System.out.println("5) DIO+");
		System.out.println("6) Done.");

		int input;
		int modules = 0;

		do
		{
			input = inputInteger("Choice? (0-6) ", 0, 6);
			if ( 6 != input )
				modules |= (byte)(1<<input);
		} while (input != 6);

		portManager.selectNetModulesToDisplay(modules);
	}

	private void setAnalogOutputValue() throws HCSException
	{
		int dac = inputInteger("DAC (0-31)? ", 0, 31);
		int value = inputInteger("Value (0-255)? ", 0, 255);

		portManager.setAnalogOutputValue(dac, value);
	}

	private void getAnalogOutputValue() throws HCSException
	{
		int dac = inputInteger("DAC (0-31)? ", 0, 31);

		int value = portManager.getAnalogOutputValue(dac);
		System.out.println("DAC " + dac + " = " + value );
	}

	private void selectAnalogOutputsToDisplay() throws HCSException
	{
		int input;
		int bits = 0;	// ints are always 32 bits in length.

		do
		{
			input = inputInteger("Select DAC banks to display (0-3, -1 when done): ", -1, 3);
			if ( input != -1 ) bits |= (1<<input);
		}
		while (input != -1);

		portManager.selectAnalogOutputsToDisplay(bits);
	}

	private void setAnalogInputValue() throws HCSException
	{
		int input = inputInteger("ADC (0-135)? ", 0, 135);
		int value = inputInteger("Value (0-4095, 4096:TRANSPARENT)? ", 0, 4096);

		portManager.setAnalogInputValue(input, value);
	}

	private void getAnalogInputValue() throws HCSException
	{
		int input = inputInteger("ADC (0-135)? ", 0, 135);
		int value = portManager.getAnalogInputValue(input);

		System.out.println("ADC " + input + " = " + value);
	}

	private void selectAnalogInputsToDisplay() throws HCSException
	{
		int input;
		int bits = 0;	// ints are always 32 bits in length.

		do
		{
			input = inputInteger("Select input banks to display (0-16, -1 when done): ", -1, 16);
			if ( input != -1 ) bits |= (1<<input);
		}
		while (input != -1);

		portManager.selectAnalogInputsToDisplay(bits);
	}

	private void setDigitalOutputState() throws HCSException
	{
		int output = inputInteger("Output (0-255)? ", 0, 255);
		int state = inputInteger("State (0:OFF, 1:ON)? ", 0, 1);
		portManager.setDigitalOutputState(output, state==1);
	}

	private void getDigitalOutputState() throws HCSException
	{
		int output = inputInteger("Output (0-255)? ", 0, 255);
		boolean state = portManager.getDigitalOutputState(output);
		System.out.println("Output " + output + " = " + (state ? "ON" : "OFF"));
	}

	private void selectDigitalOutputsToDisplay() throws HCSException
	{
		int output;
		int bits = 0;	// ints are always 32 bits in length.

		do
		{
			output = inputInteger("Select output banks to display (0-31, -1 when done): ", -1, 31);
			if ( output != -1 ) bits |= (1<<output);
		}
		while (output != -1);

		portManager.selectDigitalOutputsToDisplay(bits);
	}

	private void setDigitalInputState() throws HCSException
	{
		int input = inputInteger("Input (0-255)? ", 0, 255);
		int state = inputInteger("State (0:OFF, 1:ON, 2:TRANSPARENT)? ", 0, 2);
		portManager.setDigitalInputState(input, state);
	}

	private void getDigitalInputState() throws HCSException
	{
		int input = inputInteger("Input (0-255)? ", 0, 255);
		int state = portManager.getDigitalInputState(input);
		System.out.println("Input " + input + " = " + (state == 0 ? "OFF" : (state == 1 ? "ON" : "TRANSPARENT")));
	}

	private void selectDigitalInputsToDisplay() throws HCSException
	{
		int input;
		int bits = 0;	// ints are always 32 bits in length.

		do
		{
			input = inputInteger("Select input banks to display (0-31, -1 when done): ", -1, 31);
			if ( input != -1 ) bits |= (1<<input);
		}
		while (input != -1);

		portManager.selectDigitalInputsToDisplay(bits);
	}

	private void getVariable() throws HCSException
	{
		int variable = inputInteger("Variable? (0-127) ", 0, 127);
		int value = portManager.getVariable(variable);
		System.out.println("Variable " + variable + " = " + value);
	}

	private void setVariable() throws HCSException
	{
		int variable = inputInteger("Variable? (0-127) ", 0, 127);
		int value = inputInteger("Value? (0-65535) ", 0, 65535);
		portManager.setVariable(variable, value);
	}

	private void sendNetworkString() throws HCSException
	{
		String string = input("String? ");
		portManager.sendNetworkString(string);
	}

	private void sendVoiceString() throws HCSException
	{
		String string = input("String? ");
		portManager.sendVoiceString(string);
	}

	private void setX10Module() throws HCSException
	{
		char chr;
		String response = null;
		do
		{
			try
			{
				response = input("Select X10 House Code (A-P): ").toLowerCase();
				chr = response.charAt(0);
			}
			catch (Exception e) { chr = '\0'; }
		} while (response.length() != 1 || chr < 'a' || chr > 'p');

		int unit = inputInteger("Select X10 Unit (1-16): ", 1, 16);
		if ( unit < 1 )
		{
			System.out.println("Failed");
			return;
		}

		System.out.println("\nPlease select an X10 command from the following: ");
		System.out.println("1) All Off");
		System.out.println("2) All On");
		System.out.println("3) On");
		System.out.println("4) Off");
		System.out.println("5) Dim");
		System.out.println("6) Brighten");
		int command = inputInteger("Command? ", 1, 6);
		if ( command < 1 )
		{
			System.out.println("Failed");
			return;
		}
		command--;

		int level = 0;
		if (command > 3)
		{
			level = inputInteger("Please choose a dim/brightness level (1-31) ", 1, 31);
			if ( level < 1 )
			{
				System.out.println("Failed");
				return;
			}
		}

		portManager.setX10Module(chr - 'a', unit, command, level);
	}

	private void getX10Status() throws HCSException
	{
		char chr;
		String response = null;
		do
		{
			try
			{
				response = input("Select X10 House Code (A-P) ").toLowerCase();
				chr = response.charAt(0);
			}
			catch (Exception e) { chr = '\0'; }
		} while (response.length() != 1 || chr < 'a' || chr > 'p');

		int unit = inputInteger("Select X10 Unit (1-16) ", 1, 16);
		if ( unit < 1 )
		{
			System.out.println("Failed");
			return;
		}

		boolean status = portManager.getX10Status(chr - 'a' + 1, unit);

		System.out.println("Unit " + chr + unit + (status ? " ON" : "OFF"));
	}

	private void selectX10Display() throws HCSException
	{
		char chr;
		String response = null;
		do
		{
			try
			{
				response = input("Select X10 House Code (A-P): ").toLowerCase();
				chr = response.charAt(0);
			}
			catch (Exception e) { chr = '\0'; }
		} while (response.length() != 1 || chr < 'a' || chr > 'p');

		int bit = 16 - (chr - 'a');	// a=16, p=1
		portManager.selectX10Display(chr - 'a');
	}

	private void load() throws HCSException
	{
		portManager.load("events.bin");
	}

	private void setLog() throws HCSException
	{
		int location = inputInteger("Memory location (0-65535) ", 0, 65535);
		int value = inputInteger("Memory value (0-255): ", 0, 255);
		portManager.setLog(location, value);
	}

	private void setTime() throws HCSException
	{
		Calendar date   = Calendar.getInstance();
		int millisecond = date.get(Calendar.MILLISECOND);
		int second      = date.get(Calendar.SECOND);
		int minute      = date.get(Calendar.MINUTE);
		int hour        = date.get(Calendar.HOUR_OF_DAY);
		int dow         = date.get(Calendar.DAY_OF_WEEK);
		int day         = date.get(Calendar.DAY_OF_MONTH);
		int month       = date.get(Calendar.MONTH);
		int year        = date.get(Calendar.YEAR);

		portManager.setTime(year, month, day, dow, hour, minute, second,
			millisecond);
	}

	private void setTimeFrequency() throws HCSException
	{
		System.out.println("\nPlease select a frequency from the following list:");
		System.out.println("0) off");
		System.out.println("1) Once a second");
		System.out.println("2) Once a minute");

		int selection = inputInteger("Selection? ", 0, 2);
		if ( selection < 1 )
		{
			System.out.println("Failed.");
			return;
		}
		portManager.setTimeFrequency(selection);
	}

	private void exit()
	{
		System.exit(0);		// We won't get here, but for completeness.
	}

	private int inputInteger(String prompt, int min, int max)
	{
		int val = min-1;
		while (val < min || val > max )
		{
			String response = input(prompt);
			try { val = Integer.parseInt(response); }
			catch (Exception e) { val = min-1; break; }
		}
		return val;
	}

	private String input(String prompt)
	{
		System.out.print(prompt);

		// Clear out any old input.
		try { while ( System.in.available() > 0 ) System.in.read(); }
		catch (IOException e) { return ""; }

		int chr;
		StringBuffer buf = new StringBuffer();
		while ( true )
		{
			try { chr = System.in.read(); }
			catch (IOException e) { return new String(buf); }

			if ( chr == 0x0D || chr == 0x0A )
				break;
			if ( chr != -1 )
				buf.append( (char)chr );
		}
		return new String(buf);
	}

	private void setPort(String newPortName) throws HCSException
	{
		if ( !portName.equals(newPortName) )
		{
			// The user has selected a new port.
			portName = newPortName;

			// We're changing the port so we must ask the port manager
			// to make the change.
			portManager.openPort(portName);
		}
	}

	//
	// Below is code which gets called by the IOObject when new data has come
	// in from the port.
	//

	private int decodeBCD(byte bcd)
	{
		int high = (int)(bcd & 0xf0);
		int low = (int)(bcd & 0x0f);
		high = (high >> 4)*10;
		return low + high;
	}

	// In the decode...() methods below each method is printing to stdout.
	// In general this is a BAD idea (but this is meant to be a simple example).
	// These methods are invoked by threads in the HCSPortManager class.
	// The methods should execute as quickly as possible, but in this
	// case they do not. A more appropriate way to do this is have a main UI
	// thread do the outputting, and these decode...(). The decode...()
	// methods would just fill in common datastructures which would be output
	// by the main UI thread. Other example UIs demonstrate this.

	private void decodeTime(byte[] time)
	{
		if ( time.length != 8 )
		{
			System.out.println("time length wrong.");
			return;
		}

		Calendar cal = Calendar.getInstance();
		cal.set(
			decodeBCD(time[7])+2000,	// year
			decodeBCD(time[6])-1,		// month
			decodeBCD(time[5]),			// day
			decodeBCD(time[3]),			// hours
			decodeBCD(time[2]),			// minutes
			decodeBCD(time[1]) );		// seconds

		System.out.println( "\n" +
			DateFormat.getDateTimeInstance().format(cal.getTime()));
	}

	private void decodeDigitalInputs(byte[] inputs)
	{
		if ( inputs.length != 2 )
		{
			System.out.println("inputs length wrong.");
			return;
		}

		int group = (int)inputs[0];
		int state = (int)inputs[1];
		System.out.print("Inputs " + (group*8) + "-" + (group*8+7) + ": ");
		for (int i = 7; i >= 0 ; i--)
			System.out.print( ( (state & (1<<i)) == 0 ? "0" : "1" ) );
		System.out.println("");
	}

	private void decodeDigitalOutputs(byte[] outputs)
	{
		if ( outputs.length != 2 )
		{
			System.out.println("outputs length wrong.");
			return;
		}

		int group = (int)outputs[0];
		int state = (int)outputs[1];
		System.out.print("Outputs " + (group*8) + "-" + (group*8+7) + ": ");
		for (int i = 7; i >= 0 ; i--)
			System.out.print( ( (state & (1<<i)) == 0 ? "0" : "1" ) );
		System.out.println("");
	}

	private void decodeX10(byte[] x10)
	{
		if ( x10.length != 3 )
		{
			System.out.println("x10 length wrong");
			return;
		}

		char houseCode = (char)x10[0];
		int state = ( (int)x10[1] + ((int)x10[2])<<8 );
		System.out.print(houseCode + ": ");
		for (int i = 15; i >= 0 ; i--)
			System.out.print( ( (state & (1<<i)) == 0 ? "0" : "1" ) );
		System.out.println("");
	}

	private void decodeAnalogOutputs(byte[] data)
	{
		if ( data.length != 5 )
		{
			System.out.println("Analog output length incorrect.");
			return;
		}
		int group = (int)data[0] * 4;
		for (int i = 0; i < 4; i += 2)
		{
			int bits = data[i] + (data[i+1]<<8);
			System.out.println("DAC " + (group+i) + " =\t" +
				Integer.toBinaryString(bits));
		}
	}

	private void decodeAnalogInputs(byte[] data)
	{
		if ( data.length != 17 )
		{
			System.out.println("Analog input length incorrect.");
			return;
		}
		int group = (int)data[0] * 8;
		for (int i = 0; i < 16; i += 2)
		{
			int bits = data[i] + (data[i+1]<<8);
			System.out.println("ADC " + (group+i) + " =\t" +
				Integer.toBinaryString(bits));
		}
	}

	private void decodeNetBits(byte[] data)
	{
		if ( data.length != 2 )
		{
			System.out.println("NetBit length incorrect.");
			return;
		}

		int group = (int)data[0];
		int state = (int)data[1];
		System.out.print("NetBits " + (group*8) + "-" + (group*8+7) + ": ");
		for (int i = 7; i >= 0 ; i--)
			System.out.print( ( (state & (1<<i)) == 0 ? "0" : "1" ) );
		System.out.println("");
	}

	private void decodeNetworkModules(byte[] data)
	{
		if ( data.length == 2 )
		{
			switch (data[0]&0xF0)
			{
				case 0: System.out.print("PL "); break;
				case 1: System.out.print("MCIR "); break;
				case 2: System.out.print("LCD "); break;
				case 3: System.out.print("DIO "); break;
				case 4: System.out.print("ADIO "); break;
				case 5: System.out.print("DIO+ "); break;
				default:
					System.out.print("UNKNOWN "); break;
			}

			System.out.println( (data[0]&0x0F) + " reports " +
				(data[1]==0 ? "timeout" :
				(data[1]==1 ? "active" : "error")));
		}
	}

	private void decodeConsoleMessage(byte[] data)
	{
		String str = new String(data);

		System.out.println("Message: " + str);
	}

	private void handleOverrun()
	{
		// For some reason the serial port has been overrun with data. If
		// we don't fix what happened we will continue to lose data. So we
		// handle the problem here.

		try
		{
			// First we stop the controller from sending any more data.
			// We might want to add a boolean here so that the user
			// cannot try running a command while the problem is being
			// corrected. The underlying HCSPortManager code will throw
			// an exception if a command is attempted while the controller
			// is paused.
			portManager.pause();

			// Then we fix problems. In this case we'll assume that the
			// problem is that memory cleanup is an issue.
			System.gc();

			// Then we restart the controller.
			portManager.resume();
		}
		catch (HCSException e)
		{
			System.out.println("Failed (" + e.getMessage() + ").");
		}
	}

	public void portEvent(HCSEvent event)
	{
		// Getting input from user from the keyboard so we don't want
		// to confuse things with updates at the same time.
		if ( hideUpdates ) return;

		switch (event.type & (~HCSEvent.RAW))
		{
			case HCSEvent.TIME:
				decodeTime(event.rawdata); break;
			case HCSEvent.X10MODULES:
				decodeX10(event.rawdata); break;
			case HCSEvent.DIGITALINPUTS:
				decodeDigitalInputs(event.rawdata); break;
			case HCSEvent.DIGITALOUTPUTS:
				decodeDigitalOutputs(event.rawdata); break;
			case HCSEvent.ANALOGINPUTS:
				decodeAnalogInputs(event.rawdata); break;
			case HCSEvent.ANALOGOUTPUTS:
				decodeAnalogOutputs(event.rawdata); break;
			case HCSEvent.NETWORKMODULES:
				decodeNetworkModules(event.rawdata); break;
			 case HCSEvent.NETBITS:
				decodeNetBits(event.rawdata); break;
			case HCSEvent.CONSOLEMESSAGE:
				decodeConsoleMessage(event.rawdata); break;
			case HCSEvent.UNKNOWN:
				// This indicates that something bad has happened at the
				// port. Likely an OVERRUN condition.
				System.out.println("Synch'ing..."); break;
			case HCSEvent.INPUTOVERRUN:
				System.out.println("OVERRUN...");
				handleOverrun();
				break;
		}
	}
}

