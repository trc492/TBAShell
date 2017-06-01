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

package webapi;

import java.util.ArrayList;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.json.JsonValue;

/**
 * This class extends the WebRequest class and provides the TBA specific web request methods.
 */
public class TbaApiV2 extends WebRequest
{
    private static final String TBA_API_BASE = "https://www.thebluealliance.com/api/v2/";
    private String header = null;

    /**
     * Constructor: Create an instance of the object.
     *
     * @param authorId specifies the author ID.
     * @param appId specifies the app ID.
     * @param appVersion specifies the app version.
     */
    public TbaApiV2(String authorId, String appId, String appVersion)
    {
        super(TBA_API_BASE);
        addRequestProperty("User-Agent", appId);
        addRequestProperty("X-TBA-App-Id", authorId + ":" + appId + ":" + appVersion);
    }   //TbaApiV2

    /**
     * This method prints the syntax of the API requests.
     */
    public void printApiHelp()
    {
        System.out.print(
            "V2 <Request>:\n" +
            "\tteams/<PageNum>\t\t\t\t- Team List Request.\n" +
            "\tteam/<TeamKey>\t\t\t\t- Single Team Request.\n" +
            "\tteam/<TeamKey>/events\t\t\t- Team Events Request.\n" +
            "\tteam/<TeamKey>/<Year>/events\t\t- Team Year Events Request.\n" +
            "\tteam/<TeamKey>/event/<EventKey>/awards\t- Team Event Awards Request.\n" +
            "\tteam/<TeamKey>/event/<EventKey>/matches\t- Team Event Matches Request.\n" +
            "\tteam/<TeamKey>/years_participated\t- Team Years Participated Request.\n" +
            "\tteam/<TeamKey>/media\t\t\t- Team Media Request.\n" +
            "\tteam/<TeamKey>/<Year>/media\t\t- Team Year Media Request.\n" +
            "\tteam/<TeamKey>/history/events\t\t- Team History Events Request.\n" +
            "\tteam/<TeamKey>/history/awards\t\t- Team Awards Request.\n" +
            "\tteam/<TeamKey>/history/robots\t\t- Team Robots Request.\n" +
            "\tteam/<TeamKey>/history/districts\t- Team Districts Request.\n" +
            "\tevents/<Year>\t\t\t\t- Event List Request.\n" +
            "\tevent/<EventKey>\t\t\t- Event Info Request.\n" +
            "\tevent/<EventKey>/teams\t\t\t- Event Teams Request.\n" +
            "\tevent/<EventKey>/matches\t\t- Event Matches Request.\n" +
            "\tevent/<EventKey>/stats\t\t\t- Event Stats Request.\n" +
            "\tevent/<EventKey>/rankings\t\t- Event Rankings Request.\n" +
            "\tevent/<EventKey>/awards\t\t\t- Event Awards Request.\n" +
            "\tevent/<EventKey>/district_points\t- Event District Points Request.\n" +
            "\tmatch/<MatchKey>\t\t\t- Match Request.\n" +
            "\tdistricts/<Year>\t\t\t- District List Request.\n" +
            "\tdistrict/<DistrictKey>/<Year>/events\t- District Events Request.\n" +
            "\tdistrict/<DistrictKey>/<Year>/rankings\t- District Rankings Request.\n" +
            "\tdistrict/<DistrictKey>/<Year>/teams\t- District Teams Request.\n");
    }   //printApiHelp

    //
    // TBA API v2.
    //

    /**
     * This method adds teams of the specified page to the teams array.
     *
     * @param teams specifies the array list to add the teams into.
     * @param pageNum specifies the page number.
     * @return array of teams.
     */
    private ArrayList<JsonObject> addTeams(ArrayList<JsonObject> teams, int pageNum)
    {
        JsonStructure data = get("teams/" + pageNum, header);

        if (data.getValueType() == JsonValue.ValueType.ARRAY && !((JsonArray)data).isEmpty())
        {
            for (JsonValue team: (JsonArray)data)
            {
                teams.add((JsonObject)team);
            }
        }
        else
        {
            teams = null;
        }

        return teams;
    }   //addTeams

    /**
     * This method sends a Team List Request.
     *
     * @return team list data.
     */
    public ArrayList<JsonObject> getTeams()
    {
        ArrayList<JsonObject> teams = new ArrayList<>();

        for (int page = 0; ; page++)
        {
            if (addTeams(teams, page) == null)
                break;
        }

        return teams;
    }   //getTeams

    /**
     * This method sends a Single Team Request.
     *
     * @param teamKey specifies the team key.
     * @return single team data.
     */
    public JsonStructure getTeam(String teamKey)
    {
        return get("team/" + teamKey, header);
    }   //getTeam

    /**
     * This method sends a Team Events Request.
     *
     * @param teamKey specifies the team key.
     * @return team events data.
     */
    public JsonStructure getTeamEvents(String teamKey)
    {
        return get("team/" + teamKey + "/events", header);
    }   //getTeamEvents

    /**
     * This method sends a Team Year Events Request.
     *
     * @param teamKey specifies the team key.
     * @param year specifies the year.
     * @return team year events data.
     */
    public JsonStructure getTeamEvents(String teamKey, String year)
    {
        return get("team/" + teamKey + "/" + year + "/events", header);
    }   //getTeamEvents

    /**
     * This method sends the Team Event Awards Request.
     *
     * @param teamKey specifies the team key.
     * @param eventKey specifies the event key.
     * @return team event awards data.
     */
    public JsonStructure getTeamEventAwards(String teamKey, String eventKey)
    {
        return get("team/" + teamKey + "/event/" + eventKey + "/awards", header);
    }   //getTeamEventAwards

    /**
     * This method sends the Team Event Matches Request.
     *
     * @param teamKey specifies the team key.
     * @param eventKey specifies the event key.
     * @return team event matches data.
     */
    public JsonStructure getTeamEventMatches(String teamKey, String eventKey)
    {
        return get("team/" + teamKey + "/event/" + eventKey + "/matches", header);
    }   //getTeamEventMatches

    /**
     * This method sends the Team Years Participated Request.
     *
     * @param teamKey specifies the team key.
     * @return team years participated data.
     */
    public JsonStructure getTeamYearsParticipated(String teamKey)
    {
        return get("team/" + teamKey + "/years_participated", header);
    }   //getTeamYearsParticipated

    /**
     * This method sends the Team Media Request.
     *
     * @param teamKey specifies the team key.
     * @return team media data.
     */
    public JsonStructure getTeamMedia(String teamKey)
    {
        return get("team/" + teamKey + "/media" + header);
    }   //getTeamMedia

    /**
     * This method sends the Team Year Media Request.
     *
     * @param teamKey specifies the team key.
     * @param year specifies the year.
     * @return team year media data.
     */
    public JsonStructure getTeamMedia(String teamKey, String year)
    {
        return get("team/" + teamKey + "/" + year + "/media", header);
    }   //getTeamMedia

    /**
     * This method sends a Team History Events Request.
     *
     * @param teamKey specifies the team key.
     * @return team history events data.
     */
    public JsonStructure getTeamHistoryEvents(String teamKey)
    {
        return get("team/" + teamKey + "/history/events", header);
    }   //getTeamHistoryEvents

    /**
     * This method sends a Team Awards Request.
     *
     * @param teamKey specifies the team key.
     * @return team awards data.
     */
    public JsonStructure getTeamAwards(String teamKey)
    {
        return get("team/" + teamKey + "/history/awards", header);
    }   //getTeamAwards

    /**
     * This method sends the Team Robots Request.
     *
     * @param teamKey specifies the team key.
     * @return team robots data.
     */
    public JsonStructure getTeamRobots(String teamKey)
    {
        return get("team/" + teamKey + "/history/robots", header);
    }   //getTeamRobots

    /**
     * This method sends the Team Districts Request.
     *
     * @param teamKey specifies the team key.
     * @return team districts data.
     */
    public JsonStructure getTeamDistricts(String teamKey)
    {
        return get("team/" + teamKey + "/history/districts", header);
    }   //getTeamDistricts

    /**
     * This methods sends the Event List Request.
     *
     * @param year specifies the year.
     * @return event list data.
     */
    public JsonStructure getEvents(String year)
    {
        return get("events/" + year, header);
    }   //getEvents

    /**
     * This method sends the Event Info Request.
     *
     * @param eventKey specifies the event key.
     * @return event info data.
     */
    public JsonStructure getEvent(String eventKey)
    {
        return get("event/" + eventKey, header);
    }   //getEvent 

    /**
     * This method sends the Event Teams Request.
     *
     * @param eventKey specifies the event key.
     * @return event teams data.
     */
    public JsonStructure getEventTeams(String eventKey)
    {
        return get("event/" + eventKey + "/teams", header);
    }   //getEventTeams

    /**
     * This method sends the Event Matches Request.
     *
     * @param eventKey specifies the event key.
     * @return event matches data.
     */
    public JsonStructure getEventMatches(String eventKey)
    {
        return get("event/" + eventKey + "/matches", header);
    }   //getEventMatches

    /**
     * This method sends the Event Stats Request.
     *
     * @param eventKey specifies the event key.
     * @return event stats data.
     */
    public JsonStructure getEventStats(String eventKey)
    {
        return get("event/" + eventKey + "/stats", header);
    }   //getEventStats

    /**
     * This method sends the Event Rankings Request.
     *
     * @param eventKey specifies the event key.
     * @return event rankings data.
     */
    public JsonStructure getEventRankings(String eventKey)
    {
        return get("event/" + eventKey + "/rankings", header);
    }   //getEventRankings

    /**
     * This method sends the Event Awards Request.
     *
     * @param eventKey specifies the event key.
     * @return event awards data.
     */
    public JsonStructure getEventAwards(String eventKey)
    {
        return get("event/" + eventKey + "/awards", header);
    }   //getEventAwards

    /**
     * This method sends the Event District Points Request.
     *
     * @param eventKey specifies the event key.
     * @return event district points data.
     */
    public JsonStructure getEventDistrictPoints(String eventKey)
    {
        return get("event/" + eventKey + "/district_points", header);
    }   //getEventDistrictPoints

    /**
     * This method sends the Match Request.
     *
     * @param matchKey specifies the match key.
     * @return match data.
     */
    public JsonStructure getMatch(String matchKey)
    {
        return get("match/" + matchKey, header);
    }   //getMatch

    /**
     * This method sends the District List Request.
     *
     * @param year specifies the year.
     * @return district list data.
     */
    public JsonStructure getDistricts(String year)
    {
        return get("districts/" + year, header);
    }   //getDistricts

    /**
     * This method sends the District Events Request.
     *
     * @param districtKey specifies the district key.
     * @param year specifies the year.
     * @return district events data.
     */
    public JsonStructure getDistrictEvents(String districtKey, String year)
    {
        return get("district/" + districtKey + "/" + year + "/events", header);
    }   //getDistrictEvents

    /**
     * This method sends the District Rankings Request.
     *
     * @param districtKey specifies the district key.
     * @param year specifies the year.
     * @return district rankings data.
     */
    public JsonStructure getDistrictRankings(String districtKey, String year)
    {
        return get("district/" + districtKey + "/" + year + "/rankings", header);
    }   //getDistrictRankings

    /**
     * This method sends the District Teams Request.
     *
     * @param districtKey specifies the district key.
     * @param year specifies the year.
     * @return district teams data.
     */
    public JsonStructure getDistrictTeams(String districtKey, String year)
    {
        return get("district/" + districtKey + "/" + year + "/teams", header);
    }   //getDistrictTeams

}   //class TbaApiV2
