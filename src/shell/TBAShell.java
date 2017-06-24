/*
 * Copyright (c) 2017 Titan Robotics Club (http://www.titanrobotics.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package shell;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class TBAShell
{
    private static final String AUTHOR_ID = "frc492";
    private static final String APP_NAME = "TBAShell";
    private static final String APP_VERSION = "v0.1";
    private static final InputStream consoleIn = System.in;
    private static final PrintStream consoleOut = System.out;
    private static final PrintStream statusOut = System.out;
    private static final PrintStream dataOut = System.out;

    private static TBACommand parser = new TBACommand(AUTHOR_ID, APP_NAME, APP_VERSION);

    /**
     * Main entry point of the program.
     *
     * If program arguments are provided, they will be treated as a web request command and will be processed and
     * the program will terminate. If no program argument provided, the program goes into interactive mode prompting
     * the user using the console for the command. The command is processed and the user is prompted for the next
     * command until the user issues a "quit" command.
     *
     * @param args specifies the program arguments.
     */
    public static void main(String[] args)
    {
        Scanner console = args.length == 0? new Scanner(consoleIn): null;
        boolean done = false;

        while (!done)
        {
            if (args.length == 0)
            {
                //
                // Interactive mode: prompt the user for the command, process it and repeat until quit.
                //
                consoleOut.print("\nTBA Command (? for help): ");
                String command = console.nextLine().trim();
                if (command.equals("quit") || command.equals("exit"))
                {
                    consoleOut.println("Program terminated.");
                    done = true;
                }
                else if (command.equals("?"))
                {
                    printHelpMsg(false);
                }
                else if (command.equals("help"))
                {
                    printHelpMsg(true);
                }
                else if (parser.processCommand(command.split("\\s+"), dataOut, statusOut) == null)
                {
                    printErrorMsg();
                }
            }
            else
            {
                //
                // Batch mode: process the command and exit.
                //
                if (parser.processCommand(args, dataOut, statusOut) == null)
                {
                    printErrorMsg();
                }
                done = true;
            }
        }

        if (console != null)
        {
            console.close();
        }
    }   //main

    /**
     * This method prints the help message.
     *
     * @param longVersion specifies true to print the long version of the message, false to print the short version.
     */
    private static void printHelpMsg(boolean longVersion)
    {
        consoleOut.print(
            "\nSyntax: <Command>\n" +
            "<Command>:\n" +
            "\t?\t\t\t\t- Print the short help message.\n" +
            "\thelp\t\t\t\t- Print the long help message (with raw request syntax).\n" +
            "\tquit\t\t\t\t- Exit this program.\n" +
            "\texit\t\t\t\t- Exit this program.\n" +
            "\tlist [<Options>] <Model>\t- Retrieve and list model data.\n" +
            "\tget <Request>\t\t\t- Send raw <Request> to the web server.\n");
        parser.printCommandHelp(longVersion, consoleOut);
    }   //printHelpMsg

    /**
     * This method retrieves the error message of the last request. If there is one, the message is printed to
     * the console.
     */
    private static void printErrorMsg()
    {
        String errorMsg = parser.getErrorMessage();

        if (errorMsg != null)
        {
            consoleOut.println(errorMsg);
        }
    }   //printErrorMsg

}   //class TBAShell
