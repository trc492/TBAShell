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
                String[] params = request.split("\\?");
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

    /**
     * This method matches a filter pair with the specified key and returns the filter value if it matches.
     *
     * @param filterPair specifies the filter key/value pair.
     * @param key specifies the filter key to match for.
     * @return filter value if the key matches, null otherwise.
     */
    private static String getFilterValue(String[] filterPair, String key)
    {
        String value = null;

        if (filterPair.length == 2 && filterPair[0].equals(key))
        {
            value = filterPair[1];
        }

        return value;
    }   //getFilterValue

    /**
     * This method processes the Status request.
     * Syntax: status
     *         status?team=<TeamKey>&event=<EventKey>
     *
     * @param params specifies the parameters to be parsed.
     * @return error message string if failed, null if successful.
     */
    private static String processStatusRequest(String[] params)
    {
        String errorMsg = null;
        JsonStructure data = null;

        if (params.length == 1)
        {
            //
            // Get TBA status.
            //
            data = tbaApi.getStatus();
        }
        else if (params.length == 2)
        {
            //
            // Get team status for the specified event.
            //
            String[] filters = params[1].split("&");
            String team, event;
            if (filters.length == 2 &&
                (team = getFilterValue(filters[0].split("="), "team")) != null &&
                (event = getFilterValue(filters[1].split("="), "event")) != null)
            {
                data = tbaApi.getTeamEventStatus(team, event);
            }
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        if (data != null)
        {
            tbaApi.printData(data, null, null);
        }
        else if (errorMsg == null)
        {
            errorMsg = "Invalid filter, expecting \"team=<TeamKey>&event=<EventKey>\".";
        }

        return errorMsg;
    }   //processStatusRequest

    /**
     * This method processes the Teams request.
     * Syntax: teams
     *         teams?year=<Year>
     *         teams?team=<TeamKey>
     *         teams?event=<EventKey>
     *         teams?district=<DistrictKey>
     *
     * @param params specifies the parameters to be parsed.
     * @param verboseLevel specifies the print verbose level.
     * @return error message string if failed, null if successful.
     */
    private static String processTeamsRequest(String[] params, int verboseLevel)
    {
        String errorMsg = null;
        boolean syntaxError = false;

        if (params.length == 1)
        {
            //
            // Get all teams.
            //
            ArrayList<JsonValue> teams = tbaApi.getTeams(
                null, verboseLevel == 0? "keys": verboseLevel == 1? "simple": null);
            if (teams != null)
            {
                printTeamsArray(teams, verboseLevel);
            }
            else
            {
                syntaxError = true;
            }
        }
        else if (params.length == 2)
        {
            String[] filterPair = params[1].split("=");
            String value;

            if ((value = getFilterValue(filterPair, "year")) != null)
            {
                //
                // Get teams for the specified year.
                //
                ArrayList<JsonValue> teams = tbaApi.getTeams(
                    value, verboseLevel == 0? "keys": verboseLevel == 1? "simple": null);
                if (teams != null)
                {
                    printTeamsArray(teams, verboseLevel);
                }
                else
                {
                    syntaxError = true;
                }
            }
            else
            {
                JsonStructure data = null;
                String verbosity = verboseLevel == 0? "keys": verboseLevel == 1? "simple": null;
                String key1 = verboseLevel > 1? null: "key";
                String key2 = verboseLevel == 1? "nickname": null;

                if ((value = getFilterValue(filterPair, "team")) != null)
                {
                    //
                    // Get info for the specified team.
                    //
                    data = tbaApi.getTeam(value, verboseLevel > 1? null: "simple");
                }
                else if ((value = getFilterValue(filterPair, "event")) != null)
                {
                    //
                    // Get teams for the specified event.
                    //
                    data = tbaApi.getEventTeams(value, verbosity);
                }
                else if ((value = getFilterValue(filterPair, "district")) != null)
                {
                    //
                    // Get teams for the specified district.
                    //
                    data = tbaApi.getDistrictTeams(value, verbosity);
                }

                if (data != null)
                {
                    tbaApi.printData(data, key1, key2);
                }
                else
                {
                    syntaxError = true;
                }
            }

            if (syntaxError)
            {
                errorMsg = "Invalid filter, expecting \"year=<Year>\" or \"team=<TeamKey>\" or " +
                    "\"event=<EventKey>\" or \"district=<DistrictKey>\".";
            }
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processTeamsRequest

    /**
     * This method prints the info of the array list of teams with the specified verbose level.
     *
     * @param teams specifies the array list of team objects.
     * @param verboseLevel specifies the print verbose level.
     */
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

    /**
     * This method processes the Events request.
     * Syntax: events?year=<Year>
     *         events?team=<TeamKey>
     *         events?team=<TeamKey>&year=<Year>
     *         events?event=<EventKey>
     *         events?district=<DistrictKey>
     *
     * @param params specifies the parameters to be parsed.
     * @param verboseLevel specifies the print verbose level.
     * @return error message string if failed, null if successful.
     */
    private static String processEventsRequest(String[] params, int verboseLevel)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonStructure data = null;
            String verbosity = verboseLevel == 0? "keys": verboseLevel == 1? "simple": null;
            String key1 = verboseLevel > 1? null: "key";
            String key2 = verboseLevel == 1? "name": null;
            String[] filters = params[1].split("&");

            if (filters.length == 1)
            {
                String[] filterPair = filters[0].split("=");
                String value;

                if ((value = getFilterValue(filterPair, "year")) != null)
                {
                    //
                    // Get events for the specified year.
                    //
                    data = tbaApi.getEvents(value, verbosity);
                }
                else if ((value = getFilterValue(filterPair, "team")) != null)
                {
                    //
                    // Get events for the specified team.
                    //
                    data = tbaApi.getTeamEvents(value, null, verbosity);
                }
                else if ((value = getFilterValue(filterPair, "event")) != null)
                {
                    //
                    // Get info for specified event.
                    //
                    data = tbaApi.getEvent(value, verboseLevel > 1? null: "simple");
                }
                else if ((value = getFilterValue(filterPair, "district")) != null)
                {
                    //
                    // Get events for the specified district.
                    //
                    data = tbaApi.getDistrictEvents(value, verbosity);
                }
            }
            else if (filters.length == 2)
            {
                String[] filter1Pair = filters[0].split("=");
                String[] filter2Pair = filters[1].split("=");
                String value1 = getFilterValue(filter1Pair, "team");
                String value2 = getFilterValue(filter2Pair,"year");

                if (value1 != null && value2 != null)
                {
                    //
                    // Get events for the specified team and year.
                    //
                    data = tbaApi.getTeamEvents(value1, value2, verbosity);
                }
            }

            if (data != null)
            {
                tbaApi.printData(data, key1, key2);
            }
            else
            {
                errorMsg = "Invalid filter, expecting \"year=<Year>\" or \"team=<TeamKey>\" or " +
                    "\"team=<TeamKey>&year=<Year>\" or " + "\"event=<EventKey>\" or \"district=<DistrictKey>\".";
            }
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processEventsRequest

    /**
     * This method processes the Districts request.
     * Syntax: districts?year=<Year>
     *         districts?team=<TeamKey>
     *
     * @param params specifies the parameters to be parsed.
     * @param verboseLevel specifies the print verbose level.
     * @return error message string if failed, null if successful.
     */
    private static String processDistrictsRequest(String[] params, int verboseLevel)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonStructure data = null;
            String key1 = verboseLevel > 1? null: "key";
            String key2 = verboseLevel == 1? "display_name": null;
            String[] filterPair = params[1].split("=");
            String value;

            if ((value = getFilterValue(filterPair, "year")) != null)
            {
                //
                // Get districts for the specified year.
                //
                data = tbaApi.getDistricts(value);
            }
            else if ((value = getFilterValue(filterPair, "team")) != null)
            {
                //
                // Get districts for the specified team.
                //
                data = tbaApi.getTeamDistricts(value);
            }

            if (data != null)
            {
                tbaApi.printData(data, key1, key2);
            }
            else
            {
                errorMsg = "Invalid filter, expecting \"year=<Year>\" or \"team=<TeamKey>\".";
            }
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processDistrictsRequest

    /**
     * This method processes the Matches request.
     * Syntax: matches?team=<TeamKey>&year=<Year>
     *         matches?event=<EventKey>
     *         matches?event=<EventKey>&team=<TeamKey>
     *         matches?match=<MatchKey>
     *
     * @param params specifies the parameters to be parsed.
     * @param verboseLevel specifies the print verbose level.
     * @return error message string if failed, null if successful.
     */
    private static String processMatchesRequest(String[] params, int verboseLevel)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonStructure data = null;
            String verbosity = verboseLevel > 0? null: "keys";
            String key1 = verboseLevel > 0? null: "key";
            String[] filters = params[1].split("&");

            if (filters.length == 1)
            {
                String[] filterPair = filters[0].split("=");
                String value;

                if ((value = getFilterValue(filterPair, "event")) != null)
                {
                    //
                    // Get matches for the specified event.
                    //
                    data = tbaApi.getEventMatches(value, verbosity);
                }
                else if ((value = getFilterValue(filterPair, "match")) != null)
                {
                    //
                    // Get info for the specified match.
                    //
                    data = tbaApi.getMatch(value, verboseLevel > 1? null: "simple");
                }
            }
            else if (filters.length == 2)
            {
                String[] filter1Pair = filters[0].split("=");
                String[] filter2Pair = filters[1].split("=");
                String value1, value2;

                if ((value1 = getFilterValue(filter1Pair, "team")) != null &&
                    (value2 = getFilterValue(filter2Pair, "year")) != null)
                {
                    //
                    // Get matches for the specified team and year.
                    //
                    data = tbaApi.getTeamMatches(value1, value2, verbosity);
                }
                else if ((value1 = getFilterValue(filter1Pair, "event")) != null &&
                         (value2 = getFilterValue(filter2Pair, "team")) != null)
                {
                    //
                    // Get matches for the specified event and team.
                    //
                    data = tbaApi.getTeamEventMatches(value2, value1, verbosity);
                }
            }

            if (data != null)
            {
                tbaApi.printData(data, key1, null);
            }
            else
            {
                errorMsg = "Invalid filter, expecting \"team=<TeamKey>&year=<Year>\" or \"event=<EventKey>\" or " +
                    "\"event=<EventKey>&team=<TeamKey>\" or \"match=<MatchKey>\".";
            }
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processMatchesRequest

    /**
     * This method processes the Awards request.
     * Syntax: awards?team=<TeamKey>
     *         awards?team=<TeamKey>&year=<Year>
     *         awards?event=<EventKey>
     *         awards?event=<EventKey>&team=<TeamKey>
     *
     * @param params specifies the parameters to be parsed.
     * @param verboseLevel specifies the print verbose level.
     * @return error message string if failed, null if successful.
     */
    private static String processAwardsRequest(String[] params, int verboseLevel)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonStructure data = null;
            String key1 = verboseLevel > 1? null: "name";
            String key2 = verboseLevel == 1? "event_key": null;
            String[] filters = params[1].split("&");

            if (filters.length == 1)
            {
                String[] filterPair = filters[0].split("=");
                String value;

                if ((value = getFilterValue(filterPair, "team")) != null)
                {
                    //
                    // Get awards for the specified team.
                    //
                    data = tbaApi.getTeamAwards(value, null);
                }
                else if ((value = getFilterValue(filterPair, "event")) != null)
                {
                    //
                    // Get awards for the specified event.
                    //
                    data = tbaApi.getEventAwards(value);
                }
            }
            else if (filters.length == 2)
            {
                String[] filter1Pair = filters[0].split("=");
                String[] filter2Pair = filters[1].split("=");
                String value1, value2;

                if ((value1 = getFilterValue(filter1Pair, "team")) != null &&
                    (value2 = getFilterValue(filter2Pair, "year")) != null)
                {
                    //
                    // Get awards for the specified team and year.
                    //
                    data = tbaApi.getTeamAwards(value1, value2);
                }
                else if ((value1 = getFilterValue(filter1Pair, "event")) != null &&
                         (value2 = getFilterValue(filter2Pair, "team")) != null)
                {
                    //
                    // Get awards for the specified event and team.
                    //
                    data = tbaApi.getTeamEventAwards(value2, value1);
                }
            }

            if (data != null)
            {
                tbaApi.printData(data, key1, key2);
            }
            else
            {
                errorMsg = "Invalid filter, expecting \"team=<TeamKey>\" or \"team=<TeamKey>&year=<Year>\" or " +
                    "\"event=<EventKey>\" or \"event=<EventKey>&team=<TeamKey>\".";
            }
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processAwardsRequest

    /**
     * This method processes the Rankings request.
     * Syntax: rankings?event=<EventKey>
     *         rankings?district=<DistrictKey>
     *
     * @param params specifies the parameters to be parsed.
     * @param verboseLevel specifies the print verbose level.
     * @return error message string if failed, null if successful.
     */
    private static String processRankingsRequest(String[] params, int verboseLevel)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonStructure data = null;
            String key1 = verboseLevel > 1? null: "rank";
            String key2 = verboseLevel == 1? "team_key": null;
            String[] filterPair = params[1].split("=");
            String value;

            if ((value = getFilterValue(filterPair, "event")) != null)
            {
                //
                // Get rankings for the specified event.
                //
                data = tbaApi.getEventRankings(value, verboseLevel);
            }
            else if ((value = getFilterValue(filterPair, "district")) != null)
            {
                //
                // Get rankings for the specified district.
                //
                data = tbaApi.getDistrictRankings(value);
            }

            if (data != null)
            {
                tbaApi.printData(data, key1, key2);
            }
            else
            {
                errorMsg = "Invalid filter, expecting \"event=<EventKey>\" or \"district=<DistrictKey>\".";
            }
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processRankingsRequest

    /**
     * This method processes the OPRs request.
     * Syntax: oprs?event=<EventKey>
     *
     * @param params specifies the parameters to be parsed.
     * @param verboseLevel specifies the print verbose level.
     * @return error message string if failed, null if successful.
     */
    private static String processOprsRequest(String[] params, int verboseLevel)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonStructure data = null;
            String value = getFilterValue(params[1].split("="), "event");

            if (value != null)
            {
                //
                // Get oprs for the specified event.
                //
                data = tbaApi.getEventOprs(value, verboseLevel);
            }

            if (data != null)
            {
                tbaApi.printData(data);
            }
            else
            {
                errorMsg = "Invalid filter, expecting \"event=<EventKey>\".";
            }
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processOprsRequest

    /**
     * This method processes the District Points request.
     * Syntax: district_points?event=<EventKey>
     *
     * @param params specifies the parameters to be parsed.
     * @param verboseLevel specifies the print verbose level.
     * @return error message string if failed, null if successful.
     */
    private static String processDistrictPointsRequest(String[] params, int verboseLevel)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonStructure data = null;
            String value = getFilterValue(params[1].split("="), "event");

            if (value != null)
            {
                //
                // Get district points for the specified event.
                //
                data = tbaApi.getEventDistrictPoints(value, verboseLevel);
            }

            if (data != null)
            {
                tbaApi.printData(data);
            }
            else
            {
                errorMsg = "Invalid filter, expecting \"event=<EventKey>\".";
            }
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processDistrictPointsRequest

    /**
     * This method processes the Insights request.
     * Syntax: insights?event=<EventKey>
     *
     * @param params specifies the parameters to be parsed.
     * @return error message string if failed, null if successful.
     */
    private static String processInsightsRequest(String[] params)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonStructure data = null;
            String value = getFilterValue(params[1].split("="), "event");

            if (value != null)
            {
                //
                // Get insights for the specified event.
                //
                data = tbaApi.getEventInsights(value);
            }

            if (data != null)
            {
                tbaApi.printData(data);
            }
            else
            {
                errorMsg = "Invalid filter, expecting \"event=<EventKey>\".";
            }
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processInsightsRequest

    /**
     * This method processes the Predictions request.
     * Syntax: predictions?event=<EventKey>
     *
     * @param params specifies the parameters to be parsed.
     * @return error message string if failed, null if successful.
     */
    private static String processPredictionsRequest(String[] params)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonStructure data = null;
            String value = getFilterValue(params[1].split("="), "event");

            if (value != null)
            {
                //
                // Get predictions for the specified event.
                //
                data = tbaApi.getEventPredictions(value);
            }

            if (data != null)
            {
                tbaApi.printData(data);
            }
            else
            {
                errorMsg = "Invalid filter, expecting \"event=<EventKey>\".";
            }
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processPredictionsRequest

    /**
     * This method processes the Alliances request.
     * Syntax: alliances?event=<EventKey>
     *
     * @param params specifies the parameters to be parsed.
     * @return error message string if failed, null if successful.
     */
    private static String processAlliancesRequest(String[] params)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonStructure data = null;
            String value = getFilterValue(params[1].split("="), "event");

            if (value != null)
            {
                //
                // Get alliances for the specified event.
                //
                data = tbaApi.getEventAlliances(value);
            }

            if (data != null)
            {
                tbaApi.printData(data);
            }
            else
            {
                errorMsg = "Invalid filter, expecting \"event=<EventKey>\".";
            }
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processAlliancesRequest

    /**
     * This method processes the YearsParticipated request.
     * Syntax: years_participated?team=<TeamKey>
     *
     * @param params specifies the parameters to be parsed.
     * @return error message string if failed, null if successful.
     */
    private static String processYearsParticipatedRequest(String[] params)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonStructure data = null;
            String value = getFilterValue(params[1].split("="), "team");

            if (value != null)
            {
                //
                // Get years participated for the specified team.
                //
                data = tbaApi.getTeamYearsParticipated(value);
            }

            if (data != null)
            {
                tbaApi.printData(data);
            }
            else
            {
                errorMsg = "Invalid filter, expecting \"team=<TeamKey>\".";
            }
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processYearsParticipatedRequest

    /**
     * This method processes the Robots request.
     * Syntax: robots?team=<TeamKey>
     *
     * @param params specifies the parameters to be parsed.
     * @param verboseLevel specifies the print verbose level.
     * @return error message string if failed, null if successful.
     */
    private static String processRobotsRequest(String[] params, int verboseLevel)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonArray data = null;
            String value = getFilterValue(params[1].split("="), "team");

            if (value != null)
            {
                //
                // Get robots for the specified team.
                //
                data = (JsonArray)tbaApi.getTeamRobots(value);
                if (data != null)
                {
                    for (JsonValue robot: data)
                    {
                        switch (verboseLevel)
                        {
                            case 0:
                            case 1:
                                tbaApi.printData((JsonStructure)robot, "key", "robot_name");
                                break;
    
                            case 2:
                                tbaApi.printData((JsonStructure)robot);
                                break;
                        }
                    }
                }
            }

            if (data == null)
            {
                errorMsg = "Invalid filter, expecting \"team=<TeamKey>\".";
            }
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processRobotsRequest

    /**
     * This method processes the Media request.
     * Syntax: media?team=<TeamKey>&year=<Year>
     *
     * @param params specifies the parameters to be parsed.
     * @param verboseLevel specifies the print verbose level.
     * @return error message string if failed, null if successful.
     */
    private static String processMediaRequest(String[] params, int verboseLevel)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonArray data = null;
            String[] filters = params[1].split("&");

            if (filters.length == 2)
            {
                String[] filter1Pair = filters[0].split("=");
                String[] filter2Pair = filters[1].split("=");
                String value1 = getFilterValue(filter1Pair, "team");
                String value2 = getFilterValue(filter2Pair, "year");

                if (value1 != null && value2 != null)
                {
                    //
                    // Get media for the specified team.
                    //
                    data = (JsonArray)tbaApi.getTeamMedia(value1, value2);
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
            }

            if (data == null)
            {
                errorMsg = "Invalid filter, expecting \"team=<TeamKey>&year=<Year>\".";
            }
        }
        else
        {
            errorMsg = "Invalid number of arguments.";
        }

        return errorMsg;
    }   //processMediaRequest

    /**
     * This method processes the SocialMedia request.
     * Syntax: social_media?team=<TeamKey>
     *
     * @param params specifies the parameters to be parsed.
     * @param verboseLevel specifies the print verbose level.
     * @return error message string if failed, null if successful.
     */
    private static String processSocialMediaRequest(String[] params, int verboseLevel)
    {
        String errorMsg = null;

        if (params.length == 2)
        {
            JsonArray data = null;
            String value = getFilterValue(params[1].split("="), "team");

            if (value != null)
            {
                //
                // Get social media for the specified team.
                //
                data = (JsonArray)tbaApi.getTeamSocialMedia(value);
                if (data != null)
                {
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
            }

            if (data == null)
            {
                errorMsg = "Invalid filter, expecting \"team=<TeamKey>\".";
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
                "\tstatus[?team=<TeamKey>&event=<EventKey>]\n" +
                "\tteams?[{year=<Year>|team=<TeamKey>|event=<EventKey>|district=<DistrictKey>}]\n" +
                "\tevents?{year=<Year>|team=<TeamKey>[&year=<Year>]|event=<EventKey>|district=<DistrictKey>}\n" +
                "\tdistricts?{year=<Year>|team=<TeamKey>}\n" +
                "\tmatches?{team=<TeamKey>&year=<Year>|event=<EventKey>[&team=<TeamKey>]|match=<MatchKey>}\n" +
                "\tawards?{team=<TeamKey>[&year=<Year>]|event=<EventKey>[&team=<TeamKey>]}\n" +
                "\trankings?{event=<EventKey>|district=<DistrictKey>}\n" +
                "\toprs?event=<EventKey>\n" +
                "\tdistrict_points?event=<EventKey>\n" +
                "\tinsights?event=<EventKey>\n" +
                "\tpredictions?event=<EventKey>\n" +
                "\talliances?event=<EventKey>\n" +
                "\tyears_participated?team=<TeamKey>\n" +
                "\trobots?team=<TeamKey>\n" +
                "\tmedia?team=<TeamKey>&year=<Year>\n" +
                "\tsocial_media?team=<TeamKey>\n");
            tbaApi.printApiHelp();
        }
    }   //printHelpMsg

}   //class TBAShell
