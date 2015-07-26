package com.score.senzors.utils;

import android.util.Log;
import com.score.senzors.exceptions.InvalidQueryException;
import com.score.senzors.pojos.Query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Query parser for android
 * Parse incoming queries and generate Query object
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class QueryParser {

    private static final String TAG = QueryParser.class.getName();

    // define query commands
    // we support only for 3 commands in mobile
    private static List<String> commandList = Arrays.asList("GET", "SHARE", ":SHARE", "DATA");

    /**
     * Parser message and get query object
     * @param message message to be passed
     * @return Query object
     */
    public static Query parse(String message) throws InvalidQueryException {
        Log.d(TAG, "Parse: parsing query " + message);
        // split message and put in linked list
        // we need to user pop operation here
        String[] tokens = message.split(" ");

        // get query parameters
        String command = getCommand(tokens);
        String user = getUser(tokens);
        HashMap<String, String> params = getParams(tokens);
        Log.d(TAG, "Parse: command " + command);
        Log.d(TAG, "Parse: user " + user);
        Log.d(TAG, "Parse: params " + params);

        return new Query(command, user, params);
    }

    /**
     * Get user from given query
     * User should be in first token, otherwise wrong query
     * @param tokens query tokens
     * @return query user
     * @throws InvalidQueryException
     */
    private static String getUser(String[] tokens) throws InvalidQueryException {
        if(tokens[0].startsWith("@"))
            return tokens[0].substring(1, tokens[0].length());

        throw new InvalidQueryException();
    }

    /**
     * Get query command
     * Command should be in second token, otherwise wrong query
     * @param tokens query tokens
     * @return command
     * @throws InvalidQueryException
     */
    private static String getCommand(String[] tokens) throws InvalidQueryException {
        if(commandList.contains(tokens[1]))
            return tokens[1];

        throw new InvalidQueryException();
    }

    /**
     * Get query params
     * Params should from 3rd token(examples below)
     *      @user1 DATA #msg LOGINOK
     *      @user1 DATA #msg LOGINFAIL
     *      @user1 DATA #lat 2.345 #lon 3.44899
     *      @user1 SHARE #gps
     *      @user1 SHARE #tp
     *      @user1 GET #gps
     * @param tokens query tokens
     * @return params
     * @throws InvalidQueryException
     */
    private static HashMap<String, String> getParams(String[] tokens) throws InvalidQueryException {
        HashMap<String, String> paramMap = new HashMap<String, String>();

        for(int i=2; i<tokens.length; i++) {
            if(getCommand(tokens).equalsIgnoreCase("DATA")) {
                // data queries contains params as name value pairs
                // so extract both param and value and store in hash map
                if(tokens[i].contains("#")) {
                    // '#' sign should be there
                    // we remove '#' sing and store in the map
                    String param = tokens[i].substring(1, tokens[i].length());
                    String value = tokens[i+1];
                    paramMap.put(param, value);

                    // jump to two position forward
                    // we already used next token as param value
                    i++;
                } else {
                    // invalid query
                    throw new InvalidQueryException();
                }
            } else {
                // other queries(SHARE and GET) only contains param, no values
                // @user1 SHARE #gps
                // @user1 GET #lat #lon
                // @user1 GET #tp
                // we remove # sing and store in the map
                // map contains entries like
                //  ('lat' 'lat')
                //  ('lon', 'lon')
                String paramValue = tokens[i].substring(1, tokens[i].length());
                paramMap.put(paramValue, paramValue);
            }
        }

        return paramMap;
    }

    /**
     * Generate query sting back from Query object
     * query should contains non empty 'user', 'command' and 'params'
     * @param query Query object
     * @return query string
     */
    public static String getMessage(Query query) {
        Log.d(TAG, "GetMessage: getting message");
        String message = query.getCommand();

        for(String key : query.getParams().keySet()) {
            // iterate over parameter map and add to query
            if (key.equalsIgnoreCase(query.getParams().get(key))) {
                // GET or SHARE query
                // param and value equal since no value to store (SHARE #lat #lon @user1)
                message = message.concat(" ").concat("#").concat(query.getParams().get(key));
            } else {
                // LOGIN or DATA query
                message = message.concat(" ").concat("#").concat(key).concat(" ").concat(query.getParams().get(key));
            }
        }

        // add user at the end if user available
        if(!query.getUser().equals("")) {
            // add user if available
            message = message.concat(" ").concat("@").concat(query.getUser());
        }

        Log.d(TAG, "GetMessage: message " + message);
        return message;
    }

    /*public static void main(String[] args) {
        try {
            String query = "@user1 DATA #lat 2.3434 #lon 34.45454";
            String query1 = "@user1 DATA #msg LOGINOK";
            String query2 = "@user1 SHARE #lat #lon";
            String query3 = "@user1 GET #lat #lon";
            String[] tokens = query.split(" ");
            String[] tokens1 = query1.split(" ");
            String[] tokens2 = query2.split(" ");
            String[] tokens3 = query3.split(" ");

            System.out.println(QueryParser.getUser(tokens));
            System.out.println(QueryParser.getCommand(tokens));
            System.out.println(QueryParser.getParams(tokens));
            System.out.println("----------------");

            System.out.println(QueryParser.getUser(tokens1));
            System.out.println(QueryParser.getCommand(tokens1));
            System.out.println(QueryParser.getParams(tokens1));
            System.out.println("----------------");

            System.out.println(QueryParser.getUser(tokens2));
            System.out.println(QueryParser.getCommand(tokens2));
            System.out.println(QueryParser.getParams(tokens2));
            System.out.println("----------------");

            System.out.println(QueryParser.getUser(tokens3));
            System.out.println(QueryParser.getCommand(tokens3));
            System.out.println(QueryParser.getParams(tokens3));
            System.out.println("----------------");

            HashMap<String, String> valMap4 = new HashMap<String, String>();
            valMap4.put("name", "eranga");
            valMap4.put("key", "123");
            Query query4= new Query("LOGIN", "mysensors", valMap4);
            System.out.println(QueryParser.getMessage(query4));
            System.out.println("----------------");

            HashMap<String, String> valMap5 = new HashMap<String, String>();
            valMap5.put("lat", "lat");
            valMap5.put("lon", "lon");
            Query query5 = new Query("SHARE", "eranga", valMap5);
            System.out.println(QueryParser.getMessage(query5));
            System.out.println("----------------");

            HashMap<String, String> valMap6 = new HashMap<String, String>();
            valMap6.put("lat", "lat");
            valMap6.put("lon", "lon");
            Query query6 = new Query("GET", "user1", valMap6);
            System.out.println(QueryParser.getMessage(query6));
            System.out.println("----------------");

            ArrayList<Sensor> sensors = new ArrayList<Sensor>();
            sensors.add(new Sensor("test", "testsen", "", true, false));
            sensors.add(new Sensor("test1", "testsen", "", true, false));
            sensors.add(new Sensor("test2", "testsen", "", true, false));

            System.out.println("----------------");
            System.out.println(sensors.contains(new Sensor("test", "Testsen", "", true, false)));
            System.out.println(sensors.contains(new Sensor("test", "testsen1", "", true, false)));
            System.out.println("----------------");
        } catch (InvalidQueryException e) {
            System.out.println(e.toString());
        }
    }*/
}
