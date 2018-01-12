package com.jpruim.vpnreport;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
/*
Work Timeline
Saturday, December 23 - 2 hours.
Implemented loading and sorting. It can read the file and put it into a useful form.

Sunday, December 24 - 4 hours.
Implemented time tallying, export text report.
Monday, December 25 - 3 hours.
Added HTML Templating. Can't do PDF coz that needs a native binary.
Total Time - 9 hours.
 */
public class Main {
    public static void main(String[] args) throws Exception {

        if(args.length < 1){
            System.err.println("ERR: No file specified as argument. Exiting");
            System.exit(1);
        }else{
            System.out.println("Working on file " + args[0]);
        }
        File csvFile = new File(args[0]);
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        MappingIterator<Map<String,String>> it = mapper.readerFor(Map.class)
                .with(schema)
                .readValues(csvFile);
        ArrayList<Event> entries = new ArrayList<>();
        ArrayList<String> users = new ArrayList<>();
        DateTimeFormatter f = DateTimeFormat.forPattern("MM/dd/YYYY h:mm:ss a");
        int records = 0;
        while (it.hasNext()) {
            records++;
            Map<String,String> rowAsMap = it.next();
            DateTime dateTime = f.parseDateTime(rowAsMap.get("Log Date").toUpperCase());
            users.add(rowAsMap.get("User (Origin)"));
            Event.eventType type;
            if(rowAsMap.get("Common Event").contains("Logoff")) {
                type = Event.eventType.EVENT_LOGOFF;
            } else if(rowAsMap.get("Common Event").contains("Logon")) {
                type = Event.eventType.EVENT_LOGON;
            } else {
                System.out.println(rowAsMap.get("Common Event"));
                throw new Exception();
            }
            Event currentEvent = new Event(rowAsMap.get("User (Origin)"), dateTime, type);
            entries.add(currentEvent);
        }
        int sessions = 0;
        Set<String> temp = new HashSet<>(users);
        String[] uq = temp.toArray(new String[temp.size()]);
        HashMap<String, List> arrayOfStuff = new HashMap<>();
        for (String user:uq) {
            List<Event> thisEvents = entries.stream().filter(c -> c.user.equals(user)).sorted(Comparator.comparingLong(o -> o.dt.getMillis())).collect(Collectors.toList());
            List<Session> userSessions = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            for(Event e : thisEvents) {
                if(e.type.equals(Event.eventType.EVENT_LOGOFF)){
                    sb.append("0");

                }else if(e.type.equals(Event.eventType.EVENT_LOGON)){
                    sb.append("1");

                }else{
                    throw new Exception();
                }
            }
            Matcher m = Pattern.compile("1+0+")
                    .matcher(sb.toString());
            while (m.find()) {
                int begin = m.start();
                int end = m.start() + m.group().length()-1;
                Session sessTemp = new Session(thisEvents.get(begin), thisEvents.get(end));
                userSessions.add(sessTemp);
                sessions++;
            }
            arrayOfStuff.put(user, userSessions);

        }
        StringBuilder htmlFile = new StringBuilder();
        htmlFile.append("<!doctype html>\n<html>\n<head>\n<title>VPN Report</title>\n</head>\n<body>\n<h1>VPN Use Report</h1>\n<hr />\n<br />\n");
        for(Map.Entry<String, List> entry : arrayOfStuff.entrySet()) {
            String key = entry.getKey();
            List<Session> value = entry.getValue();
            if (value.size() > 0){
                htmlFile.append("<h2>User ").append(key).append("</h2>\n");
                htmlFile.append("<table style=\"width:100%\">\n" +
                        "<tr>\n" +
                        "<th align=\"left\">Time Started</th>\n" +
                        "<th align=\"left\">Time Ended</th>\n" +
                        "<th align=\"left\">Duration (Minutes, Rounded)</th>\n" +
                        "</tr>\n");
                int timeSum = 0;
                for (Session e : value) {
                    htmlFile.append("<tr>\n")
                            .append("<td>").append(e.start.dt.toString("MM/dd/yyyy HH:mm:ss")).append("</td>\n")
                            .append("<td>").append(e.end.dt.toString("MM/dd/yyyy HH:mm:ss")).append("</td>\n")
                            .append("<td>").append(e.getMillisBetween() / (1000 * 60)).append("</td>\n")
                            .append("</tr>\n");
                    timeSum += e.getMillisBetween() / 1000;
                }
                htmlFile.append("</table>\n<br />\n");
                htmlFile.append("Total Time (Minutes, Rounded) - ").append(timeSum / 60).append("\n<hr />\n");
            }
        }
        htmlFile.append("</body>\n</html>");
        System.out.println("Parsed " + records + " entries, Found " + sessions +  " sessions from " + uq.length + " users.");

        File file = new File("report.html");
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(htmlFile.toString());
        } finally {
            if (writer != null) writer.close();
        }

    }
}
