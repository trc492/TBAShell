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

import java.util.ArrayList;
import java.util.Scanner;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.json.JsonValue;

import webapi.TbaApiV3;

public class TBAShell
{
    private static final String AUTHOR_ID = "frc492";
    private static final String APP_NAME = "TBAShell";
    private static final String APP_VERSION = "v0.1";

    private static TbaApiV3 tbaApi = new TbaApiV3(AUTHOR_ID, APP_NAME, APP_VERSION);

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
        Scanner console = args.length == 0? new Scanner(System.in): null;
        boolean done = false;

        while (!done)
        {
            if (args.length == 0)
            {
                System.out.print("\n[TBA] Command (? for help): ");
                String command = console.nextLine().trim();
                if (command.equals("quit") || command.equals("exit"))
                {
                    System.out.println("Program terminated.");
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
                else
                {
                    processCommand(command.split("\\s+"));
                }
            }
            else
            {
                processCommand(args);
                done = true;
            }
        }

        if (console != null)
        {
            console.close();
        }
    }   //main

    /**
     * This method processes a web request command.
     *
     * @param tokens specifies the tokens split from the command line.
     */
    private static void processCommand(String[] tokens)
    {
        String errorMsg = null;

        if (tokens[0].equals("get") && tokens.length == 2)
        {
            tbaApi.printData(tbaApi.get(tokens[1]), null, null);
        }
        else if (tokens[0].equals("list") && (tokens.length == 2 || tokens.length == 3))
        {
            String request;
            int verboseLevel = 1;

            if (tokens.length == 2)
            {
                request = tokens[1];
            }
            else
            {
                request = tokens[2];
                if (tokens[1].startsWith("-verbose="))
                {
                    try
                    {
                        verboseLevel = Integer.parseInt(tokens[1].substring(9));
                    }
                    catch (NumberFormatException e)
                    {
                        errorMsg = "verbose must be an integer: " + e.getMessage();
                    }
                }
                else
                {
                    errorMsg = "Invalid request option, expecting \"-verbose=\".";
                }
            }

            if (errorMsg == null)
            {
                String[] params = request.split("/");
                if (params[0].equals("status"))
                {
                    errorMsg = processStatusRequest(params);
                }
                else if (params[0].equals("teams"))
                {
                    errorMsg = processTeamsRequest(params, verboseLevel);
                }
                else if (params[0].equals("events"))
                {
                    errorMsg = processEventsRequest(params, verboseLevel);
                }
                else if (params[0].equals("districts"))
                {
                    errorMsg = processDistrictsRequest(params, verboseLevel);
                }
                else if (params[0].equals("matches"))
                {
                    errorMsg = processMatchesRequest(params, verboseLevel);
                }
                else if (params[0].equals("awards"))
                {
                    errorMsg = processAwardsRequest(params, verboseLevel);
                }
                else if (params[0].equals("rankings"))
                {
                    errorMsg = processRankingsRequest(params, verboseLevel);
                }
                else if (params[0].equals("oprs"))
                {
                    errorMsg = processOprsRequest(params, verboseLevel);
                }
                else if (params[0].equals("district_points"))
                {
                    errorMsg = processDistrictPointsRequest(params, verboseLevel);
                }
                else if (params[0].equals("insights"))
                {
                    errorMsg = processInsightsRequest(params);
                }
                else if (params[0].equals("predictions"))
                {
                    errorMsg = processPredictionsRequest(params);
                }
                else if (params[0].equals("alliances"))
                {
                    errorMsg = processAlliancesRequest(params);
                }
                else if (params[0].equals("years_participated"))
                {
                    errorMsg = processYearsParticipatedRequest(params);
                }
                else if (params[0].equals("robots"))
                {
                    errorMsg = processRobotsRequest(params, verboseLevel);
                }
                else if (params[0].equals("media"))
                {
                    errorMsg = processMediaRequest(params, verboseLevel);
                }
                else if (params[0].equals("social_media"))
                {
                    errorMsg = processSocialMediaRequest(params, verboseLevel);
                }
                else
                {
                    errorMsg = "Invalid request \"" + params[0] + "\"";
                }
            }
        }
        else
        {
            //
            // No specific error message but still an error.
            //
            errorMsg = "";
        }

        if (errorMsg != null)
        {
            if (errorMsg.length() > 0) errorMsg += "\n";
            System.out.println(errorMsg + "Invalid command syntax, type help for correct syntax.");
        }
    }   //processCommand

    private static String processStatusRequest(String[] params)
    {
        String errorMsg = null;

        if (params.length == 1)
        {
            JsonStructure status = tbaApi.getStatus();
            tbaApi.printData(status, null, null);
        }
        else if (params.length == 2)
        {
            String[] keys = params[1].split("&");
            JsonStructure status = tbaApi.getTeamEventStatus(keys[0], keys[1]);
            tbaApi.printData(status, null, null);
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processStatusRequest

    private static String processTeamsRequest(String[] params, int verboseLevel)
    {
        String errorMsg = null;

        if (params.length == 1)
        {
            ArrayList<JsonValue> teams = tbaApi.getTeams(
                null, verboseLevel == 0? "keys": verboseLevel == 1? "simple": null);
            printTeamsArray(teams, verboseLevel);
        }
        else if (params.length == 2)
        {
            String[] filter = params[1].split("=");
            if (filter.length == 2)
            {
                if (filter[0].equals("year"))
                {
                    ArrayList<JsonValue> teams = tbaApi.getTeams(
                        filter[1], verboseLevel == 0? "keys": verboseLevel == 1? "simple": null);
                    printTeamsArray(teams, verboseLevel);
                }
                else
                {
                    JsonStructure data = null;
                    String verbosity = verboseLevel == 0? "keys": verboseLevel == 1? "simple": null;
                    String key1 = verboseLevel > 1? null: "key";
                    String key2 = verboseLevel == 1? "nickname": null;

                    if (filter[0].equals("team"))
                    {
                        data = tbaApi.getTeam(filter[1], verboseLevel > 1? null: "simple");
                        tbaApi.printData(data, key1, key2);
                    }
                    else if (filter[0].equals("event"))
                    {
                        data = tbaApi.getEventTeams(filter[1], verbosity);
                        tbaApi.printData(data, key1, key2);
                    }
                    else if (filter[0].equals("district"))
                    {
                        data = tbaApi.getDistrictTeams(filter[1], verbosity);
                        tbaApi.printData(data, key1, key2);
                    }
                    else
                    {
                        errorMsg = "Invalid filter \"" + filter[0] + "\".";
                    }
                }
            }
            else
            {
                errorMsg = "Invalid filter syntax.";
            }
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processTeamsRequest

    private static void printTeamsArray(ArrayList<JsonValue> teams, int verboseLevel)
    {
        for (JsonValue team: teams)
        {
            switch (verboseLevel)
            {
                case 0:
                    System.out.println(team);
                    break;

                case 1:
                    tbaApi.printData((JsonStructure)team, "key", "nickname");
                    break;

                case 2:
                    tbaApi.printData((JsonStructure)team, null, null);
                    break;
            }
        }
    }   //printTeamsArray

    private static String processEventsRequest(String[] params, int verboseLevel)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonStructure data = null;
            String verbosity = verboseLevel == 0? "keys": verboseLevel == 1? "simple": null;
            String key1 = verboseLevel > 1? null: "key";
            String key2 = verboseLevel == 1? "name": null;
            String[] filter = params[1].split("=");

            if (filter.length == 2)
            {
                if (filter[0].equals("year"))
                {
                    data = tbaApi.getEvents(filter[1], verbosity);
                    tbaApi.printData(data, key1, key2);
                }
                else if (filter[0].equals("event"))
                {
                    data = tbaApi.getEvent(filter[1], verboseLevel > 1? null: "simple");
                    tbaApi.printData(data, key1, key2);
                }
                else if (filter[0].equals("district"))
                {
                    data = tbaApi.getDistrictEvents(filter[1], verbosity);
                    tbaApi.printData(data, key1, key2);
                }
                else if (filter[0].equals("team"))
                {
                    String[] filterKeys = filter[1].split("&");
                    if (filterKeys.length == 2)
                    {
                        data = tbaApi.getTeamEvents(filterKeys[0], filterKeys[1], verbosity);
                        tbaApi.printData(data, key1, key2);
                    }
                    else
                    {
                        errorMsg = "Invalid team filter, expecting team=<TeamKey>&<Year>.";
                    }
                }
                else
                {
                    errorMsg = "Invalid filter \"" + filter[0] + "\".";
                }
            }
            else
            {
                errorMsg = "Invalid filter syntax.";
            }
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processEventsRequest

    private static String processDistrictsRequest(String[] params, int verboseLevel)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonStructure data = null;
            String key1 = verboseLevel > 1? null: "key";
            String key2 = verboseLevel == 1? "display_name": null;
            String[] filter = params[1].split("=");

            if (filter.length == 2)
            {
                if (filter[0].equals("year"))
                {
                    data = tbaApi.getDistricts(filter[1]);
                    tbaApi.printData(data, key1, key2);
                }
                else if (filter[0].equals("team"))
                {
                    data = tbaApi.getTeamDistricts(filter[1]);
                    tbaApi.printData(data, key1, key2);
                }
                else
                {
                    errorMsg = "Invalid filter \"" + filter[0] + "\".";
                }
            }
            else
            {
                errorMsg = "Invalid filter syntax.";
            }
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processDistrictsRequest

    private static String processMatchesRequest(String[] params, int verboseLevel)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonStructure data = null;
            String verbosity = verboseLevel > 1? null: "keys";
            String key1 = verboseLevel > 1? null: "key";
            String[] filter = params[1].split("=");

            if (filter.length == 2)
            {
                if (filter[0].equals("match"))
                {
                    data = tbaApi.getMatch(filter[1], verboseLevel > 1? null: "simple");
                    tbaApi.printData(data, key1, null);
                }
                else if (filter[0].equals("event"))
                {
                    String[] filterKeys = filter[1].split("&");
                    if (filterKeys.length == 1)
                    {
                        data = tbaApi.getEventMatches(filterKeys[0], verbosity);
                        tbaApi.printData(data, key1, null);
                    }
                    else if (filterKeys.length == 2)
                    {
                        data = tbaApi.getTeamEventMatches(filterKeys[1], filterKeys[0], verbosity);
                        tbaApi.printData(data, key1, null);
                    }
                    else
                    {
                        errorMsg = "Invalid event filter, expecting event=<EventKey>[&<TeamKey>].";
                    }
                }
                else if (filter[0].equals("team"))
                {
                    String[] filterKeys = filter[1].split("&");
                    if (filterKeys.length == 2)
                    {
                        data = tbaApi.getTeamMatches(filterKeys[0], filterKeys[1], verbosity);
                        tbaApi.printData(data, key1, null);
                    }
                    else
                    {
                        errorMsg = "Invalid team filter, expecting team=<TeamKey>&<Year>.";
                    }
                }
                else
                {
                    errorMsg = "Invalid filter \"" + filter[0] + "\".";
                }
            }
            else
            {
                errorMsg = "Invalid filter syntax.";
            }
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processMatchesRequest

    private static String processAwardsRequest(String[] params, int verboseLevel)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonStructure data = null;
            String key1 = verboseLevel > 1? null: "name";
            String key2 = verboseLevel == 1? "event_key": null;
            String[] filter = params[1].split("=");

            if (filter.length == 2)
            {
                if (filter[0].equals("event"))
                {
                    String[] filterKeys = filter[1].split("&");
                    if (filterKeys.length == 1)
                    {
                        data = tbaApi.getEventAwards(filterKeys[0]);
                        tbaApi.printData(data, key1, key2);
                    }
                    else if (filterKeys.length == 2)
                    {
                        data = tbaApi.getTeamEventAwards(filterKeys[1], filterKeys[0]);
                        tbaApi.printData(data, key1, key2);
                    }
                    else
                    {
                        errorMsg = "Invalid event filter, expecting event=<EventKey>[&<TeamKey>].";
                    }
                }
                else if (filter[0].equals("team"))
                {
                    String[] filterKeys = filter[1].split("&");
                    if (filterKeys.length == 1)
                    {
                        data = tbaApi.getTeamAwards(filterKeys[0], null);
                        tbaApi.printData(data, key1, key2);
                    }
                    else if (filterKeys.length == 2)
                    {
                        data = tbaApi.getTeamAwards(filterKeys[0], filterKeys[1]);
                        tbaApi.printData(data, key1, key2);
                    }
                    else
                    {
                        errorMsg = "Invalid event filter, expecting team=<TeamKey>[&<Year>].";
                    }
                }
                else
                {
                    errorMsg = "Invalid filter \"" + filter[0] + "\".";
                }
            }
            else
            {
                errorMsg = "Invalid filter syntax.";
            }
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processAwardsRequest

    private static String processRankingsRequest(String[] params, int verboseLevel)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonStructure data = null;
            String key1 = verboseLevel > 1? null: "rank";
            String key2 = verboseLevel == 1? "team_key": null;
            String[] filter = params[1].split("=");

            if (filter.length == 2)
            {
                if (filter[0].equals("district"))
                {
                    data = tbaApi.getDistrictRankings(filter[1]);
                    tbaApi.printData(data, key1, key2);
                }
                else if (filter[0].equals("event"))
                {
                    data = tbaApi.getEventRankings(filter[1], verboseLevel);
                    tbaApi.printData(data, key1, key2);
                }
                else
                {
                    errorMsg = "Invalid filter \"" + filter[0] + "\".";
                }
            }
            else
            {
                errorMsg = "Invalid filter syntax.";
            }
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processRankingsRequest

    private static String processOprsRequest(String[] params, int verboseLevel)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonStructure data = tbaApi.getEventOprs(params[1], verboseLevel);
            tbaApi.printData(data, null, null);
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processOprsRequest

    private static String processDistrictPointsRequest(String[] params, int verboseLevel)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonStructure data = tbaApi.getEventDistrictPoints(params[1], verboseLevel);
            tbaApi.printData(data, null, null);
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processDistrictPointsRequest

    private static String processInsightsRequest(String[] params)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonStructure data = tbaApi.getEventInsights(params[1]);
            tbaApi.printData(data, null, null);
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processInsightsRequest

    private static String processPredictionsRequest(String[] params)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonStructure data = tbaApi.getEventPredictions(params[1]);
            tbaApi.printData(data, null, null);
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processPredictionsRequest

    private static String processAlliancesRequest(String[] params)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonStructure data = tbaApi.getEventAlliances(params[1]);
            tbaApi.printData(data, null, null);
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processAlliancesRequest

    private static String processYearsParticipatedRequest(String[] params)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonStructure data = tbaApi.getTeamYearsParticipated(params[1]);
            tbaApi.printData(data, null, null);
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processYearsParticipatedRequest

    private static String processRobotsRequest(String[] params, int verboseLevel)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonArray data = (JsonArray)tbaApi.getTeamRobots(params[1]);
            for (JsonValue robot: data)
            {
                switch (verboseLevel)
                {
                    case 0:
                    case 1:
                        tbaApi.printData((JsonStructure)robot, "key", "robot_name");
                        break;

                    case 2:
                        tbaApi.printData((JsonStructure)robot, null, null);
                        break;
                }
            }
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processRobotsRequest

    private static String processMediaRequest(String[] params, int verboseLevel)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            String[] filterKeys = params[1].split("&");
            if (filterKeys.length == 2)
            {
                JsonArray data = (JsonArray)tbaApi.getTeamMedia(filterKeys[0], filterKeys[1]);
                for (JsonValue media: data)
                {
                    switch (verboseLevel)
                    {
                        case 0:
                        case 1:
                            JsonObject obj = (JsonObject)media;
                            String site = obj.getString("type");
                            System.out.println(
                                "http://" + site + ".com/" +
                                (site.equalsIgnoreCase("youtube")? "watch?v=": "") +
                                obj.getString("foreign_key"));
                            break;

                        case 2:
                            tbaApi.printData((JsonStructure)media, null, null);
                            break;
                    }
                }
                
            }
            else
            {
                errorMsg = "Invalid filter, expecting <TeamKey>&<Year>.";
            }
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processMediaRequest

    private static String processSocialMediaRequest(String[] params, int verboseLevel)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonArray data = (JsonArray)tbaApi.getTeamSocialMedia(params[1]);
            for (JsonValue socialMedia: data)
            {
                switch (verboseLevel)
                {
                    case 0:
                    case 1:
                        JsonObject obj = (JsonObject)socialMedia;
                        String type = obj.getString("type");
                        int index = type.indexOf('-');
                        System.out.println(
                            "http://" + (index == -1? type: type.substring(0, index)) + ".com/" +
                            obj.getString("foreign_key"));
                        break;

                    case 2:
                        tbaApi.printData((JsonStructure)socialMedia, null, null);
                        break;
                }
            }
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processSocialMediaRequest

    /**
     * This method prints the help message.
     *
     * @param longVersion specifies true to print the long version of the message, false to print the short version.
     */
    private static void printHelpMsg(boolean longVersion)
    {
        System.out.print(
            "\nSyntax: <Command>\n" +
            "<Command>:\n" +
            "\t?\t\t\t\t- Print the short help message.\n" +
            "\thelp\t\t\t\t- Print the long help message.\n" +
            "\tquit\t\t\t\t- Exit this program.\n" +
            "\texit\t\t\t\t- Exit this program.\n" +
            "\tlist [<Options>] <Model>\t- Retrieve and list model data.\n" +
            "\tget <Request>\t\t\t- Send raw <Request> to the web server.\n");
        if (longVersion)
        {
            System.out.print(
                "<Options>:\n" +
                "\t-verbose={0|1|2}\t\t- Specifies output verbosity (0: minimum, 1: medium, 2: maximum).\n" +
                "<Model>:\n" +
                "\tstatus[/<TeamKey>&<EventKey>]\n" +
                "\tteams/[{year=<Year>|team=<TeamKey>|district=<DistrictKey>|event=<EventKey>}]\n" +
                "\tevents/{year=<Year>|event=<EventKey>|district=<DistrictKey>|team=<TeamKey>[&<Year>]}\n" +
                "\tdistricts/{year=<Year>|team=<TeamKey>}\n" +
                "\tmatches/{match=<MatchKey>|event=<EventKey>[&<TeamKey>]|team=<TeamKey>&<Year>}\n" +
                "\tawards/{event=<EventKey>[&<TeamKey>]|team=<TeamKey>[&<Year>]}\n" +
                "\trankings/{district=<DistrictKey>|event=<EventKey>}\n" +
                "\toprs/<EventKey>\n" +
                "\tdistrict_points/<EventKey>\n" +
                "\tinsights/<EventKey>\n" +
                "\tpredictions/<EventKey>\n" +
                "\talliances/<EventKey>\n" +
                "\tyears_participated/<TeamKey>\n" +
                "\trobots/<TeamKey>\n" +
                "\tmedia/<TeamKey>&<Year>\n" +
                "\tsocial_media/<TeamKey>\n");
            tbaApi.printApiHelp();
        }
    }   //printHelpMsg

}   //class TBAShell
