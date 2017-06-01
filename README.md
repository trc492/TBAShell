# TBAShell
A shell interface to TheBlueAlliance.com.

Syntax: \<Command\> [\<ParamList\>]
<Command>:
        ?                                       - Print the short help message.
        help                                    - Print the long help message.
        quit                                    - Exit this program.
        exit                                    - Exit this program.
        show [<Options>] <Model>[<params>]      - Retrieve and show model data.
        get <Request>                           - Send raw <Request> to the web server.
<Options>:
        -verbose={0|1|2}                        - Specifies output verbosity (0: minimum, 1: medium, 2: maximum).
<Model>:
        status[/<TeamKey>&<EventKey>]
        teams/[{year=<Year>|team=<TeamKey>|district=<DistrictKey>|event=<EventKey>}]
        events/{year=<Year>|event=<EventKey>|district=<DistrictKey>|team=<TeamKey>[&<Year>]}
        districts/{year=<Year>|team=<TeamKey>}
        matches/{match=<MatchKey>|event=<EventKey>[&<TeamKey>]|team=<TeamKey>&<Year>}
        awards/{event=<EventKey>[&<TeamKey>]|team=<TeamKey>[&<Year>]}
        rankings/{district=<DistrictKey>|event=<EventKey>}
        oprs/<EventKey>
        district_points/<EventKey>
        insights/<EventKey>
        predictions/<EventKey>
        alliances/<EventKey>
        years_participated/<TeamKey>
        robots/<TeamKey>
        media/<TeamKey>&<Year>
        social_media/<TeamKey>
V3 <Request>:
        status                                                  - TBA Status request.
        teams[/<Year>]/<PageNum>[/simple|keys]                  - Team List Request with optional year and verbosity.
        team/<TeamKey>[/simple]                                 - Single Team Request with optional verbosity.
        team/<TeamKey>/years_participated                       - Team Years Participated Request.
        team/<TeamKey>/districts                                - Team Districts Request.
        team/<TeamKey>/robots                                   - Team Robots Request.
        team/<TeamKey>/events[/<Year>][/simple|keys]            - Team Events Request with optional year and verbosity.
        team/<TeamKey>/event/<EventKey>/matches[/simple|key]    - Team Event Matches Request with optional verbosity.
        team/<TeamKey>/event/<EventKey>/awards                  - Team Event Awards Request.
        team/<TeamKey>/event/<EventKey>/status                  - Team Event Status Request.
        team/<TeamKey>/awards[/<Year>]                          - Team Awards Request with optional year.
        team/<TeamKey>/matches[/<Year>][/simple|key]            - Team Matches Request with optional year and verbosity.
        team/<TeamKey>/media/<Year>                             - Team Media Request.
        team/<TeamKey>/social_media                             - Team Social Media Request.
        events/<Year>[/simple|keys]                             - Event List Request with optional verbosity.
        event/<EventKey>[/simple]                               - Single Event Request with optional verbosity.
        event/<EventKey>/teams[/simple|keys]                    - Event Teams Request with optional verbosity.
        event/<EventKey>/alliances                              - Event Alliances Request.
        event/<EventKey>/insights                               - Event Insights Request.
        event/<EventKey>/oprs                                   - Event OPR Request.
        event/<EventKey>/predictions                            - Event Rankings Request.
        event/<EventKey>/rankings                               - Event Rankings Request.
        event/<EventKey>/district_points                        - Event District Points Request.
        event/<EventKey>/matches[/simple|keys]                  - Event Matches Request with optional verbosity.
        event/<EventKey>/awards                                 - Event Awards Request.
        districts/<Year>                                        - District List Request.
        district/<DistrictKey>/teams[/simple|keys]              - District Teams Request with optional verbosity.
        district/<DistrictKey>/rankings                         - District Rankings Request.
        district/<DistrictKey>/events[/simple|keys]             - District Events Request with optional verbosity.
        match/<MatchKey>[/simple]                               - Match Request with optional verbosity.
