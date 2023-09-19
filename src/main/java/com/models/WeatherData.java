package com.models;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import com.google.gson.annotations.SerializedName;

public class WeatherData {
    private String id;
    private String name;
    private String state;

    @SerializedName("time_zone")
    private String timeZone;

    @SerializedName("lat")
    private double latitude;

    @SerializedName("lon")
    private double longitude;

    @SerializedName("local_date_time")
    private String localDateTime;

    @SerializedName("local_date_time_full")
    private String localDateTimeFull;

    @SerializedName("air_temp")
    private double airTemperature;

    @SerializedName("apparent_t")
    private double apparentTemperature;

    @SerializedName("cloud")
    private String cloudCondition;

    @SerializedName("dewpt")
    private double dewPoint;

    @SerializedName("press")
    private double pressure;

    @SerializedName("rel_hum")
    private int relativeHumidity;

    @SerializedName("wind_dir")
    private String windDirection;

    @SerializedName("wind_spd_kmh")
    private int windSpeedKmh;

    @SerializedName("wind_spd_kt")
    private int windSpeedKt;

    public WeatherData(String id, String name, String state, String timeZone, double latitude, double longitude,
            String localDateTime, String localDateTimeFull, double airTemperature, double apparentTemperature,
            String cloudCondition, double dewPoint, double pressure, int relativeHumidity, String windDirection,
            int windSpeedKmh, int windSpeedKt) {

        this.id = id;
        this.name = name;
        this.state = state;
        this.timeZone = timeZone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.localDateTime = localDateTime;
        this.localDateTimeFull = localDateTimeFull;
        this.airTemperature = airTemperature;
        this.apparentTemperature = apparentTemperature;
        this.cloudCondition = cloudCondition;
        this.dewPoint = dewPoint;
        this.pressure = pressure;
        this.relativeHumidity = relativeHumidity;
        this.windDirection = windDirection;
        this.windSpeedKmh = windSpeedKmh;
        this.windSpeedKt = windSpeedKt;
    }

    public WeatherData(String id) {
        this.id = id;
        this.name = null;
        this.state = null;
        this.timeZone = null;
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.localDateTime = null;
        this.localDateTimeFull = null;
        this.airTemperature = 0.0;
        this.apparentTemperature = 0.0;
        this.cloudCondition = null;
        this.dewPoint = 0.0;
        this.pressure = 0.0;
        this.relativeHumidity = 0;
        this.windDirection = null;
        this.windSpeedKmh = 0;
        this.windSpeedKt = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(String localDateTime) {
        this.localDateTime = localDateTime;
    }

    public String getLocalDateTimeFull() {
        return localDateTimeFull;
    }

    public void setLocalDateTimeFull(String localDateTimeFull) {
        this.localDateTimeFull = localDateTimeFull;
    }

    public double getAirTemperature() {
        return airTemperature;
    }

    public void setAirTemperature(double airTemperature) {
        this.airTemperature = airTemperature;
    }

    public double getApparentTemperature() {
        return apparentTemperature;
    }

    public void setApparentTemperature(double apparentTemperature) {
        this.apparentTemperature = apparentTemperature;
    }

    public String getCloudCondition() {
        return cloudCondition;
    }

    public void setCloudCondition(String cloudCondition) {
        this.cloudCondition = cloudCondition;
    }

    public double getDewPoint() {
        return dewPoint;
    }

    public void setDewPoint(double dewPoint) {
        this.dewPoint = dewPoint;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public int getRelativeHumidity() {
        return relativeHumidity;
    }

    public void setRelativeHumidity(int relativeHumidity) {
        this.relativeHumidity = relativeHumidity;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

    public int getWindSpeedKmh() {
        return windSpeedKmh;
    }

    public void setWindSpeedKmh(int windSpeedKmh) {
        this.windSpeedKmh = windSpeedKmh;
    }

    public int getWindSpeedKt() {
        return windSpeedKt;
    }

    public void setWindSpeedKt(int windSpeedKt) {
        this.windSpeedKt = windSpeedKt;
    }

    public void showWeatherData() {
        System.out.println("ID: " + id);
        System.out.println("Name: " + name);
        System.out.println("State: " + state);
        System.out.println("TimeZone: " + timeZone);
        System.out.println("Latitude: " + latitude);
        System.out.println("Longitude: " + longitude);
        System.out.println("LocalDateTime: " + localDateTime);
        System.out.println("LocalDateTimeFull: " + localDateTimeFull);
        System.out.println("AirTemperature: " + airTemperature);
        System.out.println("ApparentTemperature: " + apparentTemperature);
        System.out.println("CloudCondition: " + cloudCondition);
        System.out.println("DewPoint: " + dewPoint);
        System.out.println("Pressure: " + pressure);
        System.out.println("RelativeHumidity: " + relativeHumidity);
        System.out.println("WindDirection: " + windDirection);
        System.out.println("WindSpeedKmh: " + windSpeedKmh);
        System.out.println("WindSpeedKt: " + windSpeedKt);
    }

    // TODO: unit test
    public static WeatherData readFileAndParse(String fileLocation) {
        HashMap<String, String> map = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileLocation))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length >= 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    map.put(key, value);
                }
            }
        } catch (IOException e) {
            System.err.println("File located at " + fileLocation + "reading error " + e.getMessage());
            return null;
        }

        // Construct WeatherData object based on the map
        return new WeatherData(
                map.get("id"),
                map.get("name"),
                map.get("state"),
                map.get("time_zone"),
                Double.parseDouble(map.get("lat")),
                Double.parseDouble(map.get("lon")),
                map.get("local_date_time"),
                map.get("local_date_time_full"),
                Double.parseDouble(map.get("air_temp")),
                Double.parseDouble(map.get("apparent_t")),
                map.get("cloud"),
                Double.parseDouble(map.get("dewpt")),
                Double.parseDouble(map.get("press")),
                Integer.parseInt(map.get("rel_hum")),
                map.get("wind_dir"),
                Integer.parseInt(map.get("wind_spd_kmh")),
                Integer.parseInt(map.get("wind_spd_kt")));
    }
}
