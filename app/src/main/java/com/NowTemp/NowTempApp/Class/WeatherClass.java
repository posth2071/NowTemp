package com.NowTemp.NowTempApp.Class;

import android.util.Log;

public class WeatherClass {
    private final String FORMAT_TEMP = "%s\u00B0";
    private final String FORMAT_WIND = "%sm/s";
    private final String FORMAT_HUMIDITY = "%s%%";

    double[] coordinate;

    String nowTemp = "";        // 현재기온
    String maxTemp = "";        // 최저기온
    String minTemp = "";        // 최고기온

    String humidity = "";       // 습도
    String wind = "";          // 풍속
    String weather_ID = "";     // 구름상태
    int weather_image = 0;

    public void WeatherClass(){ }

    public void setNowTemp(String nowTemp) {
        double test = Double.parseDouble(nowTemp);
        Log.d("변환", "온도 double변환"+test);
        int temp = (int)test;
        Log.d("변환", "온도 int변환"+temp);
        this.nowTemp = String.format(FORMAT_TEMP, String.valueOf(temp));
    }
    public String getNowTemp() { return nowTemp; }

    public void setMaxTemp(String maxTemp) {
        this.maxTemp = String.format(FORMAT_TEMP, maxTemp);
    }
    public String getMaxTemp() { return maxTemp; }

    public void setMinTemp(String minTemp) {
        this.minTemp = String.format(FORMAT_TEMP, minTemp);
    }
    public String getMinTemp() { return minTemp; }

    public void setHumidity(String humidity) {
        this.humidity = String.format(FORMAT_HUMIDITY, humidity);
    }
    public String getHumidity() { return humidity; }

    public void setWeather_ID(String value) {
        int id = Integer.parseInt(value);
        if(id < 300){
            weather_ID = "번개";
            weather_image = 0;
        }
        else if(id < 500){
            weather_ID = "이슬비";
            weather_image = 1;
        }
        else if(id < 600){
            weather_ID = "비";
            weather_image = 2;
        }
        else if(id < 700){
            weather_ID = "눈";
            weather_image = 3;
        }
        else if(id == 800){
            weather_ID = "맑음";
            weather_image = 4;
        }
        else {
            weather_ID = "구름";
            weather_image = 5;
        }
    }
    public String getWeather_ID() { return weather_ID; }

    public void setWind(String wind) {
        this.wind = String.format(FORMAT_WIND, wind);
    }
    public String getWind() { return wind; }

    public int getWeather_image() {
        return weather_image;
    }

    public double[] getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(double[] coordinate) {
        this.coordinate = coordinate;
    }

    public String getInfo(){
        String info = String.format(" \n\tWeatherClass\n\t\t조회 좌표 X [%f], Y [%f]\n\t\t현재기온 [%s] 최고기온 [%s] 최저기온 [%s]\n\t\t구름상태 [%s] 풍속 [%s] 습도 [%s]",
                coordinate[0], coordinate[1], nowTemp, maxTemp, minTemp, weather_ID, wind, humidity);
        return info;
    }
}
