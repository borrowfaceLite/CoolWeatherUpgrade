package com.example.borrowface.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.borrowface.coolweather.gson.Forecast;
import com.example.borrowface.coolweather.gson.Weather;
import com.example.borrowface.coolweather.util.HttpUtil;
import com.example.borrowface.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by borrowface on 2018/1/10.
 */

public class WeatherFragment extends Fragment {
    private SwipeRefreshLayout swipeRefresh;
    private String mWeatherId;
    private LinearLayout forecastLayout;
    private TextView degreeText;
    private TextView weatherInfoText;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private int ID;
    public String cityName;
    public String updateTime;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.activity_weather,container,false);
        initViews(view);
        return view;
    }

    public static WeatherFragment newInstance(String weatherId,int ID) {
        WeatherFragment f = new WeatherFragment();
        Bundle args=new Bundle();
        args.putString("weatherId",weatherId);
        args.putInt("ID",ID);
        f.setArguments(args);
        return f;
    }

    private void initViews(View view){
//        titleView = view.findViewById(R.id.degree_text);
        Bundle bundle = getArguments();
//        titleView.setText(bundle == null ? "" : bundle.getString("text"));
        swipeRefresh=view.findViewById(R.id.swipe_refresh);
        forecastLayout=view.findViewById(R.id.forecast_layout);
        degreeText=view.findViewById(R.id.degree_text);
        weatherInfoText=view.findViewById(R.id.weather_info_text);
        aqiText=view.findViewById(R.id.aqi_text);
        pm25Text=view.findViewById(R.id.pm25_text);
        comfortText=view.findViewById(R.id.comfort_text);
        carWashText=view.findViewById(R.id.car_wash_text);
        sportText=view.findViewById(R.id.sport_text);

        mWeatherId=( bundle== null ? "" : bundle.getString("weatherId"));
        ID=(bundle==null?null:bundle.getInt("ID"));
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        swipeRefresh.setRefreshing(true);
        requestWeather(mWeatherId);
    }

    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId +
                "&key=b07a2d1025c544f285347844401bd187";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "获取天气信息失败1", Toast.LENGTH_SHORT)
                                .show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(getActivity()).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(getContext(), "获取天气信息失败2", Toast.LENGTH_SHORT)
                                    .show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "°C";
        String weatherInfo = weather.now.more.info;
        this.cityName=cityName;
        this.updateTime=updateTime;
        if (ID==1){
            MainActivity mainActivity= (MainActivity) getActivity();
            mainActivity.titleCity.setText(cityName);
            mainActivity.updateTime.setText(updateTime);
        }
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.forecast_item,
                    forecastLayout, false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
//        weatherLayout.setVisibility(View.VISIBLE);
    }

}
