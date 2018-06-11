package com.example.borrowface.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by borrowface on 2018/1/10.
 */

public class CityList extends DataSupport {
    private String cityListName;
    private String weatherListId;

    public String getCityListName() {
        return cityListName;
    }

    public void setCityListName(String cityListName) {
        this.cityListName = cityListName;
    }

    public String getWeatherListId() {
        return weatherListId;
    }

    public void setWeatherListId(String weatherListId) {
        this.weatherListId = weatherListId;
    }
}
