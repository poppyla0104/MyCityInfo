The program takes a city name as an input, then communicates with 3 different RESTful APIs to get 3 different types of information: the city’s weather, air quality and holidays. The program will call each API, provide the city name to Weather API and Air Quality API to get the information as a JSON Object to display. Program will take the country of the city provided in the Weather JSON Object to call the Holiday API to get holiday JSON information and display the information.

Instruction:
- Unzip the external library json-simple-1.1.jar: 
      unzip json-simple-1.1.jar
- The program takes the city's name from the command line argument. Compile the program: javac MyCity.java
- There are a couple ways to request the city to execute the program:
      - java MyCity tokyo or java MyCity "tokyo"
      - For the cities what has apostrophe such as Saint John's, put the city name in the following format: java MyCity "Saint John's" or java MyCity Saint John/'s
