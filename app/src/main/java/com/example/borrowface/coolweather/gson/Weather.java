package com.example.borrowface.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by borrowface on 2017/12/27.
 */

public class Weather {
    public String status;
    public Basic basic;
    public Now now;
    public AQI aqi;
    public Suggestion suggestion;
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;

}
