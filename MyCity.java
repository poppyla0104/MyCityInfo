/* Poppy La
** File name: MyCity.java
** Instructor: Dr.Dimpsey
** Date: 10/29/2021
** Course: CSS436
** Program 2: REST
**   Java application which takes as input the name of a city and provides information 
**   about the weather for that city as well as city air pollution and hollidays.  
*/


import java.io.IOException;
import java.net.http.*;
import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.util.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class MyCity {
   public static String city = "", country = "";
   public static String weatherKey = WEATHERKEY;
   public static String holidayKey = SECRETKEY;
   public static String weatherBody = "", holidayBody = "", airBody = "";

   public static void main (String[] args) throws IOException {
      if(args.length < 1) {
         System.out.println("Error: Valid city name needs to be input. Program is terminated.");
         System.exit(0);
      }

      // get the city from command line argument
      for(String i : args) 
         city += i +" ";
         
		city = city.strip();
      
      try {
         // Process and print out the 1st API data (weather api)
         String weatherApi = String.format("https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s", city.replaceAll("\\s", "%20"), weatherKey);
         processAPI(weatherApi,1);
         JSONObject weather = (JSONObject) new JSONParser().parse(weatherBody);     // get weather JSON object
         printWeather(weather);

         // Process and print out the 2nd API data (air pollution api)
         String airApi = String.format("https://air-quality-by-api-ninjas.p.rapidapi.com/v1/airquality?city=%s", city.replaceAll("\\s", "%20"));
         processAPI(airApi, 2);
         JSONObject air = (JSONObject) new JSONParser().parse(airBody);             // get air pollution JSON object
         printAirPolution(air);

         // Process and print out the 3rd API data (holiday api)
         country = ((JSONObject)weather.get("sys")).get("country").toString();
         String holidayApi = String.format("https://public-holiday.p.rapidapi.com/%d/%s", 2021, country);
         processAPI(holidayApi,3);
         JSONArray holiday = (JSONArray) new JSONParser().parse(holidayBody);       // get holiday JSON object
         printHoliday(holiday);

      } catch (IOException e) {
         System.out.println("Invalid city.");

      } catch (Exception e) {
         System.out.println(e.toString());}
   }


   // check the api URL respond status
   public static int checkStatus(String api, int apiNumber) throws URISyntaxException, IOException, Exception {
      int respond = 0;

      // for weather api
      if (apiNumber == 1) {
         URL myURL = new URL(api);
         HttpURLConnection connection = (HttpURLConnection)myURL.openConnection();
         connection.setRequestMethod("GET");
         connection.connect();
         respond = connection.getResponseCode();

      // for air pollution api
      } else if (apiNumber == 2) {
         HttpRequest request = HttpRequest.newBuilder().uri(URI.create(api)).header("x-rapidapi-host", "air-quality-by-api-ninjas.p.rapidapi.com").header("x-rapidapi-key", SECRETKEY).method("GET", HttpRequest.BodyPublishers.noBody()).build();
         HttpResponse<String> apiRespond = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
         respond = apiRespond.statusCode();
         airBody = apiRespond.body();              // store api's JSON body as string
         
      // for holiday api
      } else if (apiNumber == 3) {
         HttpRequest request = HttpRequest.newBuilder().uri(URI.create(api)).header("x-rapidapi-host", "public-holiday.p.rapidapi.com").header("x-rapidapi-key", SECRETKEY).method("GET", HttpRequest.BodyPublishers.noBody()).build();
         HttpResponse<String> apiRespond = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
         respond = apiRespond.statusCode();
         holidayBody = apiRespond.body();          // store api's JSON body as string
      }
      return respond;
   }


   // process the api URL
   public static void processAPI(String api, int apiNumber) throws URISyntaxException, IOException, Exception {
      int status = checkStatus(api, apiNumber);    // check respond status

      // back-off and wait if server is busy
      if (status == 429 || status >=500) {
         int count = 1;
         int wait = 2000;     
         // user has 5 retry to call the server, wait 2 seconds first
         // then increase 2 more seconds every retry
         while ((status == 429 || status >=500) && count <= 6) {
            Thread.sleep(wait * count);
            status = checkStatus(api, apiNumber);
            count++;
         }
         
         // Print time out  if server still busy after 5 tries
         if (count == 6 && (status == 429 || status >=500))
            System.out.println("Time out.");
      } 
      
      // throw exception if server can not be connected after 5 tries or error happens
      if (status >= 400)
         throw new IOException();
      
      // get weather JSON body as string
      // air and holiday JSON body were already stored when checkStatus() was called
      if (apiNumber == 1)
         weatherBody = processWeather(new URL(api));
   }


   // process to get weather JSON body
   public static String processWeather(URL myURL) throws URISyntaxException, IOException, Exception {
      HttpURLConnection connection = (HttpURLConnection)myURL.openConnection();
      connection.setRequestProperty("Content-Type", "application/json; utf-8");
      BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream)connection.getContent()));

      String line = "";
      while((line = reader.readLine()) != null)
         weatherBody += line;
   
      return weatherBody;
   }


   // convert kelvin degree to fahrenheit degree
   public static int getFahrenheit(String kelvin) {
      return (int)(((Double.valueOf(kelvin) - 273) * 9/5) + 32);
   }


   // print city's weather data
   public static void printWeather(JSONObject data) {
      JSONObject weather = (JSONObject) ((JSONArray)data.get("weather")).get(0);
      System.out.println("----------------------------------------------------------------------------------");
      System.out.println(String.format("%s WEATHER: %s", city.toUpperCase(), weather.get("main").toString().toUpperCase()));
      System.out.println("----------------------------------------------------------------------------------");
      System.out.println("Weather details: \t" + weather.get("description"));
      System.out.println("Temperature : \t\t" + (getFahrenheit(((JSONObject)data.get("main")).get("temp").toString())) + " F");
      System.out.println("Feels like: \t\t" + (getFahrenheit(((JSONObject)data.get("main")).get("feels_like").toString())) + " F");
      System.out.println("Lowest temperature: \t" + (getFahrenheit(((JSONObject)data.get("main")).get("temp_min").toString())) + " F");
      System.out.println("Highest temperature: \t" + (getFahrenheit(((JSONObject)data.get("main")).get("temp_max").toString())) + " F");
      System.out.println("Pressure: \t\t" + (((JSONObject)data.get("main")).get("pressure")) + " hPa");
      System.out.println("Humidity: \t\t" + (((JSONObject)data.get("main")).get("humidity")) + " %");
   }


   // print city's air pollution data
   public static void printAirPolution(JSONObject air) {
      String quality = "";
      int aqi = Integer.parseInt(air.get("overall_aqi").toString());
      if (aqi <= 50) {
         quality = "Good"; 
      } else if (aqi <= 100) {
         quality = "Moderate"; 
      } else if (aqi <= 150) {
         quality = "Unhealthy for sensitive groups"; 
      } else if (aqi <= 200) {
         quality = "Unhealthy"; 
      } else if (aqi <= 300) {
         quality = "Very unhealthy"; 
      } else if (aqi <= 500) {
         quality = "Hazadous"; 
      } 

      System.out.println("----------------------------------------------------------------------------------");
      System.out.println(String.format("%s AIR QUALITY: %s", city.toUpperCase(), quality.toUpperCase()));
      System.out.println("----------------------------------------------------------------------------------");
      System.out.println(String.format("Component: CO\t concentration(μg/m3): %-10s aqi: %s", ((JSONObject)air.get("CO")).get("concentration"), ((JSONObject)air.get("CO")).get("aqi")));
      System.out.println(String.format("Component: NO2\t concentration(μg/m3): %-10s aqi: %s", ((JSONObject)air.get("NO2")).get("concentration"), ((JSONObject)air.get("NO2")).get("aqi")));
      System.out.println(String.format("Component: O3\t concentration(μg/m3): %-10s aqi: %s", ((JSONObject)air.get("O3")).get("concentration"), ((JSONObject)air.get("O3")).get("aqi")));
      System.out.println(String.format("Component: SO2\t concentration(μg/m3): %-10s aqi: %s", ((JSONObject)air.get("SO2")).get("concentration"), ((JSONObject)air.get("SO2")).get("aqi")));
      System.out.println(String.format("Component: PM2.5 concentration(μg/m3): %-10s aqi: %s", ((JSONObject)air.get("PM2.5")).get("concentration"), ((JSONObject)air.get("PM2.5")).get("aqi")));
      System.out.println(String.format("Component: PM10\t concentration(μg/m3): %-10s aqi: %s", ((JSONObject)air.get("PM10")).get("concentration"), ((JSONObject)air.get("PM10")).get("aqi")));
   }


   // print city's holiday data
   public static void printHoliday(JSONArray holidays) {
      System.out.println("----------------------------------------------------------------------------------");
      System.out.println(String.format("%s HOLIDAYS WITH LOCAL NAMES", city.toUpperCase()));
      System.out.println("----------------------------------------------------------------------------------");
      for (int i = 0; i < holidays.size(); i++) {
         JSONObject holiday = (JSONObject) holidays.get(i);
         System.out.println(String.format("Date: %-15s Name: %-30s Local name: %s", holiday.get("date"), holiday.get("name"), holiday.get("localName")));
      }   
   }
   
}