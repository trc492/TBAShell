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

import java.io.PrintStream;
import java.util.HashMap;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.json.JsonValue;

import webapi.TbaApiV3;

/**
 * This class implements a parser to parse and process TBA commands.
 */
public class TBACommand
{
    /**
     * This class parses the filter part of the command and create a set of <key>=<value> pairs.
     */
    private class FilterSet
    {
        private HashMap<String, String> filters = new HashMap<>();

        /**
         * Constructor: Create an instance of the object.
         * A filter string contains a set of filters separated by '&'. A basic filter syntax is <key>=<value>.
         *
         * @param filterCommand specifies the filter command string.
         */
        public FilterSet(String filterCommand)
        {
            for (String s: filterCommand.split("&"))
            {
                String[] pair = s.split("=");
                if (pair.length != 2)
                {
                    throw new IllegalArgumentException("Invalid filter syntax, expecting \"<key>=<value>\".");
                }
                else
                {
                    filters.put(pair[0], pair[1]);
                }
            }
        }   //FilterSet

        /**
         * This method finds the filter with the specified key and returns the filter value.
         *
         * @param key specifies the filter key.
         * @return corresponding filter value.
         */
        public String getValue(String key)
        {
            return filters.get(key);
        }   //getValue

        /**
         * This method returns the number of filters in the filter set.
         *
         * @return number of filters in the set.
         */
        int getNumFilters()
        {
            return filters.size();
        }   //getNumFilters

    }   //class FilterSet

    private TbaApiV3 tbaApi;
    private String errorMsg = null;

    /**
     * Constructor: Create an instance of the object.
     *
     * @param authorId specifies the author ID.
     * @param appName specifies the application name.
     * @param appVersion specifies the application version.
     */
    public TBACommand(String authorId, String appName, String appVersion)
    {
        tbaApi = new TbaApiV3(authorId, appName, appVersion);
    }   //TBACommand

    /**
     * This method returns the error message of the last request if any.
     *
     * @return error message of the last request, null if none.
     */
    public String getErrorMessage()
    {
        return errorMsg;
    }   //getErrorMessage

    /**
     * This method prints the command help message.
     *
     * @param longVersion specifies true to print the long version of the message, false to print the short version.
     * @param helpOut specifies standard output stream for the help message.
     */
    public void printCommandHelp(boolean longVersion, PrintStream helpOut)
    {
        helpOut.print(
            "<Options>:\n" +
            "\t-(0|1|2)\t\t\t- Specifies output verbose level (0: minimum, 1: medium, 2: maximum - default is 1).\n" +
            "<Model>:\n" +
            "\tstatus[?team=<TeamKey>&event=<EventKey>]\n" +
            "\tteams[?(year=<Year>|team=<TeamKey>|event=<EventKey>|district=<DistrictKey>)]\n" +
            "\tevents?(year=<Year>|team=<TeamKey>[&year=<Year>]|event=<EventKey>|district=<DistrictKey>)\n" +
            "\tdistricts?(year=<Year>|team=<TeamKey>)\n" +
            "\tmatches?(team=<TeamKey>&year=<Year>|event=<EventKey>[&team=<TeamKey>]|match=<MatchKey>)\n" +
            "\tawards?(team=<TeamKey>[&year=<Year>]|event=<EventKey>[&team=<TeamKey>])\n" +
            "\trankings?(event=<EventKey>|district=<DistrictKey>)\n" +
            "\toprs?event=<EventKey>\n" +
            "\tdistrict_points?event=<EventKey>\n" +
            "\tinsights?event=<EventKey>\n" +
            "\tpredictions?event=<EventKey>\n" +
            "\talliances?event=<EventKey>\n" +
            "\tyears_participated?team=<TeamKey>\n" +
            "\trobots?team=<TeamKey>\n" +
            "\tmedia?team=<TeamKey>&year=<Year>\n" +
            "\tsocial_media?team=<TeamKey>\n");

        if (longVersion)
        {
            tbaApi.printApiHelp(helpOut);
        }
    }   //printCommandHelp

    /**
     * This method processes a TBA request command.
     * The syntax of a basic TBA request command is:
     *  Command     ::= [-<Options>] <Model>?[<FilterSet>]
     *  Options     ::= Specifies command options such as verbose level.
     *  Model       ::= Specifies data model such as teams, events, districts, matches etc.
     *  FilterSet   ::= <Filter>{&<Filter>}
     *  Filter      ::= <Key>=<Value>
     *  Key         ::= Specifies the filter key.
     *  Value       ::= Specifies the filter value.
     *
     * @param tokens specifies the tokens split from the command line.
     * @param dataOut specifies the output stream to print the data, null if no data output required.
     * @param statusOut specifies standard output stream for command status, can be null for quiet mode.
     * @return resulting data of the command, null if command failed.
     */
    public JsonStructure processCommand(String[] tokens, PrintStream dataOut, PrintStream statusOut)
    {
        JsonStructure data = null;

        errorMsg = null;
        if (tokens[0].equals("get") && tokens.length == 2)
        {
            data = tbaApi.get(tokens[1], statusOut);
            if (data != null && dataOut != null)
            {
                tbaApi.printData(data, dataOut);
            }
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
                if (tokens[1].charAt(0) == '-')
                {
                    try
                    {
                        verboseLevel = Integer.parseInt(tokens[1].substring(1));
                    }
                    catch (NumberFormatException e)
                    {
                        errorMsg = "Verbose level must be an integer: " + e.getMessage();
                    }
                }
                else
                {
                    errorMsg = "Invalid request option, expecting \"-<VerboseLevel>\".";
                }
            }

            if (errorMsg == null)
            {
                String[] params = request.split("\\?");
                FilterSet filterSet = null;

                if (params.length > 2)
                {
                    errorMsg = "Invalid request syntax, expecting \"<Model>?<Filters>\".";
                }
                else if (params.length == 2)
                {
                    try
                    {
                        filterSet = new FilterSet(params[1]);
                    }
                    catch (IllegalArgumentException e)
                    {
                        errorMsg = e.getMessage();
                    }
                }

                if (errorMsg == null)
                {
                    if (params[0].equals("status"))
                    {
                        data = processStatusRequest(filterSet, dataOut, statusOut);
                    }
                    else if (params[0].equals("teams"))
                    {
                        data = processTeamsRequest(verboseLevel, filterSet, dataOut, statusOut);
                    }
                    else if (params[0].equals("events"))
                    {
                        data = processEventsRequest(verboseLevel, filterSet, dataOut, statusOut);
                    }
                    else if (params[0].equals("districts"))
                    {
                        data = processDistrictsRequest(verboseLevel, filterSet, dataOut, statusOut);
                    }
                    else if (params[0].equals("matches"))
                    {
                        data = processMatchesRequest(verboseLevel, filterSet, dataOut, statusOut);
                    }
                    else if (params[0].equals("awards"))
                    {
                        data = processAwardsRequest(verboseLevel, filterSet, dataOut, statusOut);
                    }
                    else if (params[0].equals("rankings"))
                    {
                        data = processRankingsRequest(verboseLevel, filterSet, dataOut, statusOut);
                    }
                    else if (params[0].equals("oprs"))
                    {
                        data = processOprsRequest(verboseLevel, filterSet, dataOut, statusOut);
                    }
                    else if (params[0].equals("district_points"))
                    {
                        data = processDistrictPointsRequest(verboseLevel, filterSet, dataOut, statusOut);
                    }
                    else if (params[0].equals("insights"))
                    {
                        data = processInsightsRequest(filterSet, dataOut, statusOut);
                    }
                    else if (params[0].equals("predictions"))
                    {
                        data = processPredictionsRequest(filterSet, dataOut, statusOut);
                    }
                    else if (params[0].equals("alliances"))
                    {
                        data = processAlliancesRequest(filterSet, dataOut, statusOut);
                    }
                    else if (params[0].equals("years_participated"))
                    {
                        data = processYearsParticipatedRequest(filterSet, dataOut, statusOut);
                    }
                    else if (params[0].equals("robots"))
                    {
                        data = processRobotsRequest(verboseLevel, filterSet, dataOut, statusOut);
                    }
                    else if (params[0].equals("media"))
                    {
                        data = processMediaRequest(verboseLevel, filterSet, dataOut, statusOut);
                    }
                    else if (params[0].equals("social_media"))
                    {
                        data = processSocialMediaRequest(verboseLevel, filterSet, dataOut, statusOut);
                    }
                    else
                    {
                        errorMsg = "Invalid request \"" + params[0] + "\"";
                    }
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
            errorMsg += "Invalid command syntax, type ? for help.";
        }

        return data;
    }   //processCommand

    //
    // Process TBA requests.
    //

    /**
     * This method processes the Status request.
     * Syntax: status
     *         status?team=<TeamKey>&event=<EventKey>
     *
     * @param filterSet specifies filter set, null if none.
     * @param dataOut specifies the output stream to print the data, null if no data output required.
     * @param statusOut specifies standard output stream for command status, can be null for quiet mode.
     * @return resulting data of the command, null if command failed.
     */
    private JsonStructure processStatusRequest(FilterSet filterSet, PrintStream dataOut, PrintStream statusOut)
    {
        JsonStructure data = null;
        int numFilters = filterSet != null? filterSet.getNumFilters(): 0;

        try
        {
            if (numFilters == 0)
            {
                //
                // Get TBA status.
                //
                data = tbaApi.getStatus(statusOut);
            }
            else if (numFilters == 2)
            {
                //
                // Get team status for the specified event.
                //
                String team = filterSet.getValue("team");
                String event = filterSet.getValue("event");

                if (team != null && event != null)
                {
                    data = tbaApi.getTeamEventStatus(team, event, statusOut);
                }
            }

            if (data == null)
            {
                errorMsg = "Invalid filter, expecting \"team=<TeamKey>&event=<EventKey>\".";
            }
            else if (dataOut != null)
            {
                tbaApi.printData(data, dataOut);
            }
        }
        catch (RuntimeException e)
        {
            errorMsg = e.getMessage();
        }

        return data;
    }   //processStatusRequest

    /**
     * This method processes the Teams request.
     * Syntax: teams
     *         teams?year=<Year>
     *         teams?team=<TeamKey>
     *         teams?event=<EventKey>
     *         teams?district=<DistrictKey>
     *
     * @param verboseLevel specifies the print verbose level.
     * @param filterSet specifies filter set, null if none.
     * @param dataOut specifies the output stream to print the data, null if no data output required.
     * @param statusOut specifies standard output stream for command status, can be null for quiet mode.
     * @return resulting data of the command, null if command failed.
     */
    private JsonStructure processTeamsRequest(
        int verboseLevel, FilterSet filterSet, PrintStream dataOut, PrintStream statusOut)
    {
        JsonStructure data = null;
        int numFilters = filterSet != null? filterSet.getNumFilters(): 0;
        String verbosity = verboseLevel == 0? "keys": verboseLevel == 1? "simple": null;

        try
        {
            if (numFilters == 0)
            {
                //
                // Get all teams.
                //
                data = tbaApi.getTeams(null, verbosity, statusOut);
            }
            else if (numFilters == 1)
            {
                String value;

                if ((value = filterSet.getValue("year")) != null)
                {
                    //
                    // Get teams for the specified year.
                    //
                    data = tbaApi.getTeams(value, verbosity, statusOut);
                }
                else
                {
                    if ((value = filterSet.getValue("team")) != null)
                    {
                        //
                        // Get info for the specified team.
                        //
                        data = tbaApi.getTeam(value, verboseLevel > 1? null: "simple", statusOut);
                    }
                    else if ((value = filterSet.getValue("event")) != null)
                    {
                        //
                        // Get teams for the specified event.
                        //
                        data = tbaApi.getEventTeams(value, verbosity, statusOut);
                    }
                    else if ((value = filterSet.getValue("district")) != null)
                    {
                        //
                        // Get teams for the specified district.
                        //
                        data = tbaApi.getDistrictTeams(value, verbosity, statusOut);
                    }
                }
            }

            if (data == null)
            {
                errorMsg = "Invalid filter, expecting \"year=<Year>\" or \"team=<TeamKey>\" or "
                    + "\"event=<EventKey>\" or \"district=<DistrictKey>\".";
            }
            else if (dataOut != null)
            {
                tbaApi.printData(data, verboseLevel > 1? null: "key", verboseLevel == 1? "nickname": null, dataOut);
            }
        }
        catch (RuntimeException e)
        {
            errorMsg = e.getMessage();
        }

        return data;
    }   //processTeamsRequest

    /**
     * This method processes the Events request.
     * Syntax: events?year=<Year>
     *         events?team=<TeamKey>
     *         events?team=<TeamKey>&year=<Year>
     *         events?event=<EventKey>
     *         events?district=<DistrictKey>
     *
     * @param verboseLevel specifies the print verbose level.
     * @param filterSet specifies filter set, null if none.
     * @param dataOut specifies the output stream to print the data, null if no data output required.
     * @param statusOut specifies standard output stream for command status, can be null for quiet mode.
     * @return resulting data of the command, null if command failed.
     */
    private JsonStructure processEventsRequest(
        int verboseLevel, FilterSet filterSet, PrintStream dataOut, PrintStream statusOut)
    {
        JsonStructure data = null;
        int numFilters = filterSet != null? filterSet.getNumFilters(): 0;
        String verbosity = verboseLevel == 0? "keys": verboseLevel == 1? "simple": null;

        try
        {
            if (numFilters == 1)
            {
                String value;

                if ((value = filterSet.getValue("year")) != null)
                {
                    //
                    // Get events for the specified year.
                    //
                    data = tbaApi.getEvents(value, verbosity, statusOut);
                }
                else if ((value = filterSet.getValue("team")) != null)
                {
                    //
                    // Get events for the specified team.
                    //
                    data = tbaApi.getTeamEvents(value, null, verbosity, statusOut);
                }
                else if ((value = filterSet.getValue("event")) != null)
                {
                    //
                    // Get info for specified event.
                    //
                    data = tbaApi.getEvent(value, verboseLevel > 1? null: "simple", statusOut);
                }
                else if ((value = filterSet.getValue("district")) != null)
                {
                    //
                    // Get events for the specified district.
                    //
                    data = tbaApi.getDistrictEvents(value, verbosity, statusOut);
                }
            }
            else if (numFilters == 2)
            {
                String value1, value2;

                if ((value1 = filterSet.getValue("team")) != null && (value2 = filterSet.getValue("year")) != null)
                {
                    //
                    // Get events for the specified team and year.
                    //
                    data = tbaApi.getTeamEvents(value1, value2, verbosity, statusOut);
                }
            }

            if (data == null)
            {
                errorMsg = "Invalid filter, expecting \"year=<Year>\" or \"team=<TeamKey>\" or "
                    + "\"team=<TeamKey>&year=<Year>\" or " + "\"event=<EventKey>\" or \"district=<DistrictKey>\".";
            }
            else if (dataOut != null)
            {
                tbaApi.printData(data, verboseLevel > 1? null: "key", verboseLevel == 1? "name": null, dataOut);
            }
        }
        catch (RuntimeException e)
        {
            errorMsg = e.getMessage();
        }

        return data;
    }   //processEventsRequest

    /**
     * This method processes the Districts request.
     * Syntax: districts?year=<Year>
     *         districts?team=<TeamKey>
     *
     * @param verboseLevel specifies the print verbose level.
     * @param filterSet specifies filter set, null if none.
     * @param dataOut specifies the output stream to print the data, null if no data output required.
     * @param statusOut specifies standard output stream for command status, can be null for quiet mode.
     * @return resulting data of the command, null if command failed.
     */
    private JsonStructure processDistrictsRequest(
        int verboseLevel, FilterSet filterSet, PrintStream dataOut, PrintStream statusOut)
    {
        JsonStructure data = null;
        int numFilters = filterSet != null? filterSet.getNumFilters(): 0;

        try
        {
            if (numFilters == 1)
            {
                String value;

                if ((value = filterSet.getValue("year")) != null)
                {
                    //
                    // Get districts for the specified year.
                    //
                    data = tbaApi.getDistricts(value, statusOut);
                }
                else if ((value = filterSet.getValue("team")) != null)
                {
                    //
                    // Get districts for the specified team.
                    //
                    data = tbaApi.getTeamDistricts(value, statusOut);
                }
            }

            if (data == null)
            {
                errorMsg = "Invalid filter, expecting \"year=<Year>\" or \"team=<TeamKey>\".";
            }
            else if (dataOut != null)
            {
                tbaApi.printData(data, verboseLevel > 1? null: "key", verboseLevel == 1? "display_name": null, dataOut);
            }
        }
        catch (RuntimeException e)
        {
            errorMsg = e.getMessage();
        }

        return data;
    }   //processDistrictsRequest

    /**
     * This method processes the Matches request.
     * Syntax: matches?team=<TeamKey>&year=<Year>
     *         matches?event=<EventKey>
     *         matches?event=<EventKey>&team=<TeamKey>
     *         matches?match=<MatchKey>
     *
     * @param verboseLevel specifies the print verbose level.
     * @param filterSet specifies filter set, null if none.
     * @param dataOut specifies the output stream to print the data, null if no data output required.
     * @param statusOut specifies standard output stream for command status, can be null for quiet mode.
     * @return resulting data of the command, null if command failed.
     */
    private JsonStructure processMatchesRequest(
        int verboseLevel, FilterSet filterSet, PrintStream dataOut, PrintStream statusOut)
    {
        JsonStructure data = null;
        int numFilters = filterSet != null? filterSet.getNumFilters(): 0;
        String verbosity = verboseLevel > 0? null: "keys";

        try
        {
            if (numFilters == 1)
            {
                String value;

                if ((value = filterSet.getValue("event")) != null)
                {
                    //
                    // Get matches for the specified event.
                    //
                    data = tbaApi.getEventMatches(value, verbosity, statusOut);
                }
                else if ((value = filterSet.getValue("match")) != null)
                {
                    //
                    // Get info for the specified match.
                    //
                    data = tbaApi.getMatch(value, verboseLevel > 1? null: "simple", statusOut);
                }
            }
            else if (numFilters == 2)
            {
                String value1, value2;

                if ((value1 = filterSet.getValue("team")) != null && (value2 = filterSet.getValue("year")) != null)
                {
                    //
                    // Get matches for the specified team and year.
                    //
                    data = tbaApi.getTeamMatches(value1, value2, verbosity, statusOut);
                }
                else if ((value1 = filterSet.getValue("team")) != null && (value2 = filterSet.getValue("event")) != null)
                {
                    //
                    // Get matches for the specified event and team.
                    //
                    data = tbaApi.getTeamEventMatches(value1, value2, verbosity, statusOut);
                }
            }

            if (data == null)
            {
                errorMsg = "Invalid filter, expecting \"team=<TeamKey>&year=<Year>\" or \"event=<EventKey>\" or "
                    + "\"event=<EventKey>&team=<TeamKey>\" or \"match=<MatchKey>\".";
            }
            else if (dataOut != null)
            {
                tbaApi.printData(data, verboseLevel > 0? null: "key", null, dataOut);
            }
        }
        catch (RuntimeException e)
        {
            errorMsg = e.getMessage();
        }

        return data;
    }   //processMatchesRequest

    /**
     * This method processes the Awards request.
     * Syntax: awards?team=<TeamKey>
     *         awards?team=<TeamKey>&year=<Year>
     *         awards?event=<EventKey>
     *         awards?event=<EventKey>&team=<TeamKey>
     *
     * @param verboseLevel specifies the print verbose level.
     * @param filterSet specifies filter set, null if none.
     * @param dataOut specifies the output stream to print the data, null if no data output required.
     * @param statusOut specifies standard output stream for command status, can be null for quiet mode.
     * @return resulting data of the command, null if command failed.
     */
    private JsonStructure processAwardsRequest(
        int verboseLevel, FilterSet filterSet, PrintStream dataOut, PrintStream statusOut)
    {
        JsonStructure data = null;
        int numFilters = filterSet != null? filterSet.getNumFilters(): 0;

        try
        {
            if (numFilters == 1)
            {
                String value;

                if ((value = filterSet.getValue("team")) != null)
                {
                    //
                    // Get awards for the specified team.
                    //
                    data = tbaApi.getTeamAwards(value, null, statusOut);
                }
                else if ((value = filterSet.getValue("event")) != null)
                {
                    //
                    // Get awards for the specified event.
                    //
                    data = tbaApi.getEventAwards(value, statusOut);
                }
            }
            else if (numFilters == 2)
            {
                String value1, value2;

                if ((value1 = filterSet.getValue("team")) != null && (value2 = filterSet.getValue("year")) != null)
                {
                    //
                    // Get awards for the specified team and year.
                    //
                    data = tbaApi.getTeamAwards(value1, value2, statusOut);
                }
                else if ((value1 = filterSet.getValue("team")) != null && (value2 = filterSet.getValue("event")) != null)
                {
                    //
                    // Get awards for the specified event and team.
                    //
                    data = tbaApi.getTeamEventAwards(value1, value2, statusOut);
                }
            }

            if (data == null)
            {
                errorMsg = "Invalid filter, expecting \"team=<TeamKey>\" or \"team=<TeamKey>&year=<Year>\" or "
                    + "\"event=<EventKey>\" or \"event=<EventKey>&team=<TeamKey>\".";
            }
            else if (dataOut != null)
            {
                tbaApi.printData(data, verboseLevel > 1? null: "name", verboseLevel == 1? "event_key": null, dataOut);
            }
        }
        catch (RuntimeException e)
        {
            errorMsg = e.getMessage();
        }

        return data;
    }   //processAwardsRequest

    /**
     * This method processes the Rankings request.
     * Syntax: rankings?event=<EventKey>
     *         rankings?district=<DistrictKey>
     *
     * @param verboseLevel specifies the print verbose level.
     * @param filterSet specifies filter set, null if none.
     * @param dataOut specifies the output stream to print the data, null if no data output required.
     * @param statusOut specifies standard output stream for command status, can be null for quiet mode.
     * @return resulting data of the command, null if command failed.
     */
    private JsonStructure processRankingsRequest(
        int verboseLevel, FilterSet filterSet, PrintStream dataOut, PrintStream statusOut)
    {
        JsonStructure data = null;
        int numFilters = filterSet != null? filterSet.getNumFilters(): 0;

        try
        {
            if (numFilters == 1)
            {
                String value;

                if ((value = filterSet.getValue("event")) != null)
                {
                    //
                    // Get rankings for the specified event.
                    //
                    data = tbaApi.getEventRankings(value, verboseLevel, statusOut);
                }
                else if ((value = filterSet.getValue("district")) != null)
                {
                    //
                    // Get rankings for the specified district.
                    //
                    data = tbaApi.getDistrictRankings(value, statusOut);
                }
            }

            if (data == null)
            {
                errorMsg = "Invalid filter, expecting \"event=<EventKey>\" or \"district=<DistrictKey>\".";
            }
            else if (dataOut != null)
            {
                tbaApi.printData(data, verboseLevel > 1? null: "rank", verboseLevel == 1? "team_key": null, dataOut);
            }
        }
        catch (RuntimeException e)
        {
            errorMsg = e.getMessage();
        }

        return data;
    }   //processRankingsRequest

    /**
     * This method processes the OPRs request.
     * Syntax: oprs?event=<EventKey>
     *
     * @param verboseLevel specifies the print verbose level.
     * @param filterSet specifies filter set, null if none.
     * @param dataOut specifies the output stream to print the data, null if no data output required.
     * @param statusOut specifies standard output stream for command status, can be null for quiet mode.
     * @return resulting data of the command, null if command failed.
     */
    private JsonStructure processOprsRequest(
        int verboseLevel, FilterSet filterSet, PrintStream dataOut, PrintStream statusOut)
    {
        JsonStructure data = null;
        int numFilters = filterSet != null? filterSet.getNumFilters(): 0;

        try
        {
            if (numFilters == 1)
            {
                String value;

                if ((value = filterSet.getValue("event")) != null)
                {
                    //
                    // Get oprs for the specified event.
                    //
                    data = tbaApi.getEventOprs(value, verboseLevel, statusOut);
                }
            }

            if (data == null)
            {
                errorMsg = "Invalid filter, expecting \"event=<EventKey>\".";
            }
            else if (dataOut != null)
            {
                tbaApi.printData(data, dataOut);
            }
        }
        catch (RuntimeException e)
        {
            errorMsg = e.getMessage();
        }

        return data;
    }   //processOprsRequest

    /**
     * This method processes the District Points request.
     * Syntax: district_points?event=<EventKey>
     *
     * @param verboseLevel specifies the print verbose level.
     * @param filterSet specifies filter set, null if none.
     * @param dataOut specifies the output stream to print the data, null if no data output required.
     * @param statusOut specifies standard output stream for command status, can be null for quiet mode.
     * @return resulting data of the command, null if command failed.
     */
    private JsonStructure processDistrictPointsRequest(
        int verboseLevel, FilterSet filterSet, PrintStream dataOut, PrintStream statusOut)
    {
        JsonStructure data = null;
        int numFilters = filterSet != null? filterSet.getNumFilters(): 0;

        try
        {
            if (numFilters == 1)
            {
                String value;

                if ((value = filterSet.getValue("event")) != null)
                {
                    //
                    // Get district points for the specified event.
                    //
                    data = tbaApi.getEventDistrictPoints(value, verboseLevel, statusOut);
                }
            }

            if (data == null)
            {
                errorMsg = "Invalid filter, expecting \"event=<EventKey>\".";
            }
            else if (dataOut != null)
            {
                tbaApi.printData(data, dataOut);
            }
        }
        catch (RuntimeException e)
        {
            errorMsg = e.getMessage();
        }

        return data;
    }   //processDistrictPointsRequest

    /**
     * This method processes the Insights request.
     * Syntax: insights?event=<EventKey>
     *
     * @param filterSet specifies filter set, null if none.
     * @param dataOut specifies the output stream to print the data, null if no data output required.
     * @param statusOut specifies standard output stream for command status, can be null for quiet mode.
     * @return resulting data of the command, null if command failed.
     */
    private JsonStructure processInsightsRequest(FilterSet filterSet, PrintStream dataOut, PrintStream statusOut)
    {
        JsonStructure data = null;
        int numFilters = filterSet != null? filterSet.getNumFilters(): 0;

        try
        {
            if (numFilters == 1)
            {
                String value;

                if ((value = filterSet.getValue("event")) != null)
                {
                    //
                    // Get insights for the specified event.
                    //
                    data = tbaApi.getEventInsights(value, statusOut);
                }
            }

            if (data == null)
            {
                errorMsg = "Invalid filter, expecting \"event=<EventKey>\".";
            }
            else if (dataOut != null)
            {
                tbaApi.printData(data, dataOut);
            }
        }
        catch (RuntimeException e)
        {
            errorMsg = e.getMessage();
        }

        return data;
    }   //processInsightsRequest

    /**
     * This method processes the Predictions request.
     * Syntax: predictions?event=<EventKey>
     *
     * @param filterSet specifies filter set, null if none.
     * @param dataOut specifies the output stream to print the data, null if no data output required.
     * @param statusOut specifies standard output stream for command status, can be null for quiet mode.
     * @return resulting data of the command, null if command failed.
     */
    private JsonStructure processPredictionsRequest(FilterSet filterSet, PrintStream dataOut, PrintStream statusOut)
    {
        JsonStructure data = null;
        int numFilters = filterSet != null? filterSet.getNumFilters(): 0;

        try
        {
            if (numFilters == 1)
            {
                String value;

                if ((value = filterSet.getValue("event")) != null)
                {
                    //
                    // Get predictions for the specified event.
                    //
                    data = tbaApi.getEventPredictions(value, statusOut);
                }
            }

            if (data == null)
            {
                errorMsg = "Invalid filter, expecting \"event=<EventKey>\".";
            }
            else if (dataOut != null)
            {
                tbaApi.printData(data, dataOut);
            }
        }
        catch (RuntimeException e)
        {
            errorMsg = e.getMessage();
        }

        return data;
    }   //processPredictionsRequest

    /**
     * This method processes the Alliances request.
     * Syntax: alliances?event=<EventKey>
     *
     * @param filterSet specifies filter set, null if none.
     * @param dataOut specifies the output stream to print the data, null if no data output required.
     * @param statusOut specifies standard output stream for command status, can be null for quiet mode.
     * @return resulting data of the command, null if command failed.
     */
    private JsonStructure processAlliancesRequest(FilterSet filterSet, PrintStream dataOut, PrintStream statusOut)
    {
        JsonStructure data = null;
        int numFilters = filterSet != null? filterSet.getNumFilters(): 0;

        try
        {
            if (numFilters == 1)
            {
                String value;

                if ((value = filterSet.getValue("event")) != null)
                {
                    //
                    // Get alliances for the specified event.
                    //
                    data = tbaApi.getEventAlliances(value, statusOut);
                }
            }

            if (data == null)
            {
                errorMsg = "Invalid filter, expecting \"event=<EventKey>\".";
            }
            else if (dataOut != null)
            {
                tbaApi.printData(data, dataOut);
            }
        }
        catch (RuntimeException e)
        {
            errorMsg = e.getMessage();
        }

        return data;
    }   //processAlliancesRequest

    /**
     * This method processes the YearsParticipated request.
     * Syntax: years_participated?team=<TeamKey>
     *
     * @param filterSet specifies filter set, null if none.
     * @param dataOut specifies the output stream to print the data, null if no data output required.
     * @param statusOut specifies standard output stream for command status, can be null for quiet mode.
     * @return resulting data of the command, null if command failed.
     */
    private JsonStructure processYearsParticipatedRequest(
        FilterSet filterSet, PrintStream dataOut, PrintStream statusOut)
    {
        JsonStructure data = null;
        int numFilters = filterSet != null? filterSet.getNumFilters(): 0;

        try
        {
            if (numFilters == 1)
            {
                String value;

                if ((value = filterSet.getValue("team")) != null)
                {
                    //
                    // Get years participated for the specified team.
                    //
                    data = tbaApi.getTeamYearsParticipated(value, statusOut);
                }
            }

            if (data == null)
            {
                errorMsg = "Invalid filter, expecting \"team=<TeamKey>\".";
            }
            else if (dataOut != null)
            {
                tbaApi.printData(data, dataOut);
            }
        }
        catch (RuntimeException e)
        {
            errorMsg = e.getMessage();
        }

        return data;
    }   //processYearsParticipatedRequest

    /**
     * This method processes the Robots request.
     * Syntax: robots?team=<TeamKey>
     *
     * @param verboseLevel specifies the print verbose level.
     * @param filterSet specifies filter set, null if none.
     * @param dataOut specifies the output stream to print the data, null if no data output required.
     * @param statusOut specifies standard output stream for command status, can be null for quiet mode.
     * @return resulting data of the command, null if command failed.
     */
    private JsonStructure processRobotsRequest(
        int verboseLevel, FilterSet filterSet, PrintStream dataOut, PrintStream statusOut)
    {
        JsonArray data = null;
        int numFilters = filterSet != null? filterSet.getNumFilters(): 0;

        try
        {
            if (numFilters == 1)
            {
                String value;

                if ((value = filterSet.getValue("team")) != null)
                {
                    //
                    // Get robots for the specified team.
                    //
                    data = (JsonArray)tbaApi.getTeamRobots(value, statusOut);
                    if (data != null && dataOut != null)
                    {
                        for (JsonValue robot: data)
                        {
                            switch (verboseLevel)
                            {
                                case 0:
                                case 1:
                                    tbaApi.printData((JsonStructure)robot, "key", "robot_name", dataOut);
                                    break;

                                case 2:
                                    tbaApi.printData((JsonStructure)robot, dataOut);
                                    break;
                            }
                        }
                    }
                }
            }

            errorMsg = data == null? "Invalid filter, expecting \"team=<TeamKey>\".": null;
        }
        catch (RuntimeException e)
        {
            errorMsg = e.getMessage();
        }

        return data;
    }   //processRobotsRequest

    /**
     * This method processes the Media request.
     * Syntax: media?team=<TeamKey>&year=<Year>
     *
     * @param verboseLevel specifies the print verbose level.
     * @param filterSet specifies filter set, null if none.
     * @param dataOut specifies the output stream to print the data, null if no data output required.
     * @param statusOut specifies standard output stream for command status, can be null for quiet mode.
     * @return resulting data of the command, null if command failed.
     */
    private JsonStructure processMediaRequest(
        int verboseLevel, FilterSet filterSet, PrintStream dataOut, PrintStream statusOut)
    {
        JsonArray data = null;
        int numFilters = filterSet != null? filterSet.getNumFilters(): 0;

        try
        {
            if (numFilters == 2)
            {
                String value1, value2;

                if ((value1 = filterSet.getValue("team")) != null && (value2 = filterSet.getValue("year")) != null)
                {
                    //
                    // Get media for the specified team.
                    //
                    data = (JsonArray)tbaApi.getTeamMedia(value1, value2, statusOut);
                    if (data != null && dataOut != null)
                    {
                        for (JsonValue media: data)
                        {
                            switch (verboseLevel)
                            {
                                case 0:
                                case 1:
                                    JsonObject obj = (JsonObject)media;
                                    String site = obj.getString("type");
                                    dataOut.println(
                                        "http://" + site + ".com/" +
                                        (site.equalsIgnoreCase("youtube")? "watch?v=": "") +
                                        obj.getString("foreign_key"));
                                    break;

                                case 2:
                                    tbaApi.printData((JsonStructure)media, dataOut);
                                    break;
                            }
                        }
                    }
                }
            }

            errorMsg = data == null? "Invalid filter, expecting \"team=<TeamKey>&year=<Year>\".": null;
        }
        catch (RuntimeException e)
        {
            errorMsg = e.getMessage();
        }

        return data;
    }   //processMediaRequest

    /**
     * This method processes the SocialMedia request.
     * Syntax: social_media?team=<TeamKey>
     *
     * @param verboseLevel specifies the print verbose level.
     * @param filterSet specifies filter set, null if none.
     * @param dataOut specifies the output stream to print the data, null if no data output required.
     * @param statusOut specifies standard output stream for command status, can be null for quiet mode.
     * @return resulting data of the command, null if command failed.
     */
    private JsonStructure processSocialMediaRequest(
        int verboseLevel, FilterSet filterSet, PrintStream dataOut, PrintStream statusOut)
    {
        JsonArray data = null;
        int numFilters = filterSet != null? filterSet.getNumFilters(): 0;

        try
        {
            if (numFilters == 1)
            {
                String value;

                if ((value = filterSet.getValue("team")) != null)
                {
                    //
                    // Get social media for the specified team.
                    //
                    data = (JsonArray)tbaApi.getTeamSocialMedia(value, statusOut);
                    if (data != null && dataOut != null)
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
                                    dataOut.println(
                                        "http://" + (index == -1? type: type.substring(0, index)) + ".com/" +
                                        obj.getString("foreign_key"));
                                    break;

                                case 2:
                                    tbaApi.printData((JsonStructure)socialMedia, dataOut);
                                    break;
                            }
                        }
                    }
                }
            }

            errorMsg = data == null? "Invalid filter, expecting \"team=<TeamKey>\".": null;
        }
        catch (RuntimeException e)
        {
            errorMsg = e.getMessage();
        }

        return data;
    }   //processSocialMediaRequest

}   //class TBACommand
