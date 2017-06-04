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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.stream.JsonParsingException;

/**
 * This class provides methods to send a request to the specified web server and retrieves/prints the replied data.
 */
public class WebRequest
{
    /**
     * This class represents the JSON data with the last modified time stamp.
     */
    private class TimedData
    {
        JsonStructure data;
        long lastModified;

        /**
         * Constructor: Create an instance of the object.
         *
         * @param data specifies the JSON data.
         * @param lastModified specifies the last modified time stamp.
         */
        public TimedData(JsonStructure data, long lastModified)
        {
            this.data = data;
            this.lastModified = lastModified;
        }   //TimedData

    }   //class TimedData

    /**
     * This class represents a request property with the key/value pair.
     */
    private class RequestProperty
    {
        String key;
        String value;

        /**
         * Constructor: Create an instance of the object.
         *
         * @param key specifies the key of the request property.
         * @param value specifies the value of the request property.
         */
        public RequestProperty(String key, String value)
        {
            this.key = key;
            this.value = value;
        }   //RequestProperty

    }   //class RequestProperty

    private String apiBase;
    private HashMap<String, TimedData> cachedRequests = new HashMap<>();
    private ArrayList<RequestProperty> requestProperties = new ArrayList<>();

    /**
     * Constructor: Create an instance of the object.
     *
     * @param apiBase specifies the API base URL.
     */
    public WebRequest(String apiBase)
    {
        this.apiBase = apiBase;
    }   //WebRequest

    /**
     * This method adds the specified request property to the list.
     *
     * @param key specifies the key of the request property.
     * @param value specifies the value of the request property.
     */
    public void addRequestProperty(String key, String value)
    {
        requestProperties.add(new RequestProperty(key, value));
    }   //addRequestProperty

    /**
     * This method sends the GET request to the web server and returns the replied data if any.
     *
     * @param request specifies the request string.
     * @param header specifies the optional header, null if none.
     * @return replied JSON data, null if request failed.
     */
    public JsonStructure get(String request, String header)
    {
        JsonStructure jsonData = null;
        String urlString = apiBase + "/" + request;

        //
        // Form the URL string.
        //
        if (header != null)
        {
            urlString += header;
        }

        //
        // Check our cache if we have sent the same URL previously and retrieve its last modified time.
        //
        TimedData timedData = cachedRequests.get(urlString);
        long lastModified = timedData != null? timedData.lastModified: 0;

        URL url = null;
        try
        {
            url = new URL(urlString);
        }
        catch (MalformedURLException e)
        {
            System.out.println("Invalid URL <" + urlString + ">.");
            System.out.println(e.getMessage());
        }

        //
        // Open the web connection.
        //
        HttpURLConnection conn = null;
        if (url != null)
        {
            try
            {
                conn = (HttpURLConnection)url.openConnection();
            }
            catch (IOException e)
            {
                System.out.println("Failed to open connection to <" + urlString + ">.");
                System.out.println(e.getMessage());
            }
        }

        if (conn != null)
        {
            for (RequestProperty p: requestProperties)
            {
                conn.addRequestProperty(p.key, p.value);
            }

            //
            // Send the web request. If we have it in our cache, send the last modified time so the web service
            // will give us data back only if it has changed since last modified time. If we don't have it in our
            // cache, last modified time will be zero and the web service will reply with data.
            //
            try 
            {
                System.out.print("Sending request <" + urlString + ">: ");
                conn.setIfModifiedSince(lastModified);
                conn.setRequestMethod("GET");
                System.out.println(conn.getResponseMessage());
                int responseCode = conn.getResponseCode();
                if (responseCode == 200)
                {
                    //
                    // Received "OK" response with data. Update cache with the new data.
                    //
                    try (InputStream is = conn.getInputStream();
                         JsonReader rdr = Json.createReader(is))
                    {
                        jsonData = rdr.read();
                        lastModified = conn.getLastModified();
                        cachedRequests.put(urlString, new TimedData(jsonData, lastModified));
                    }
                    catch (IOException e)
                    {
                        System.out.println("Failed to open input stream.\n" + e.getMessage());
                    }
                    catch (JsonParsingException e)
                    {
                        //
                        // Failed to parse data probably because there is no data.
                        //
                        jsonData = null;
                    }
                }
                else if (responseCode == 304 && timedData != null)
                {
                    //
                    // Received "Not Modified" response with no data, return cached data from last time.
                    //
                    jsonData = timedData.data;
                }
                else
                {
                    System.out.println("Request failed: " + conn.getResponseMessage() + " (" + responseCode + ")");
                }
            }
            catch (IOException e)
            {
                System.out.println("Failed to open connection to <" + urlString + ">.");
                System.out.println(e.getMessage());
            }
        }

        return jsonData;
    }   //get

    /**
     * This method sends the request to the web server and returns the replied data if any.
     *
     * @param request specifies the request string.
     * @return replied JSON data, null if request failed.
     */
    public JsonStructure get(String request)
    {
        return get(request, null);
    }   //get

    /**
     * This method prints the entire structure of the JSON data recursively.
     *
     * @param data specifies the JSON structure.
     */
    public void printData(JsonStructure data)
    {
        if (data != null)
        {
            printValue(null, data, 0, null, null);
        }
    }   //printData

    /**
     * This method prints the entire structure of the JSON data. If key1 is provided, it will only print the values
     * of key1 and key2 if provided. If key1 is null, it will recurse into the JSON object and print all the nested
     * structures.
     *
     * @param data specifies the JSON structure.
     * @param key1 specifies the first key.
     * @param key2 specifies the second key.
     */
    public void printData(JsonStructure data, String key1, String key2)
    {
        if (data != null)
        {
            printValue(null, data, 0, key1, key2);
        }
    }   //printData

    /**
     * This method prints the value of the JSON data. If the value is a JSON object and key1 is provided, it will
     * only print the values of key1 and optionally key2. If key1 is null, it will recurse into the JSON object
     * and print all the nested structures.
     *
     * @param key specifies the key string of the JSON object, can be null if no associated key.
     * @param value specifies the JSON value.
     * @param level specifies the indentation level.
     * @param key1 specifies the first key.
     * @param key2 specifies the second key.
     */
    private void printValue(String key, JsonValue value, int level, String key1, String key2)
    {
        JsonValue.ValueType valueType = value.getValueType();

        switch (valueType)
        {
            case OBJECT:
                JsonObject obj = (JsonObject)value;
                printIndentation(level);
                if (key1 != null)
                {
                    System.out.print(obj.get(key1));
                    if (key2 != null)
                    {
                        System.out.println(": " + obj.get(key2));
                    }
                    else
                    {
                        System.out.println();
                    }
                }
                else
                {
                    if (key != null)
                    {
                        System.out.print(key + ": ");
                    }
                    System.out.println("{");
                    Iterator<String> iterator = obj.keySet().iterator();
                    while (iterator.hasNext())
                    {
                        String childKey = iterator.next();
                        printValue(childKey, obj.get(childKey), level + 1, key1, key2);
                    }
                    printIndentation(level);
                    System.out.println("}");
                }
                break;

            case ARRAY:
                printIndentation(level);
                if (key != null)
                {
                    System.out.print(key + ": ");
                }
                System.out.println("[");
                JsonArray array = (JsonArray)value;
                for (int i = 0; i < array.size(); i++)
                {
                    printValue(null, array.get(i), level + 1, key1, key2);
                }
                printIndentation(level);
                System.out.println("]");
                break;

            default:
                printIndentation(level);
                if (key != null)
                {
                    System.out.print(key + ": ");
                }
                System.out.println(value);
                break;
        }
    }   //printValue

    /**
     * This method indents the line with the specified indentation level.
     *
     * @param level specifies the indentation level.
     */
    private void printIndentation(int level)
    {
        for (int i = 0; i < level; i++)
        {
            System.out.print("    ");
        }
    }   //printIndentation

}   //class WebRequest
