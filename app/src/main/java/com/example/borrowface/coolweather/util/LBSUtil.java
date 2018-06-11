package com.example.borrowface.coolweather.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.example.borrowface.coolweather.db.City;
import com.example.borrowface.coolweather.db.County;
import com.example.borrowface.coolweather.db.Province;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by hasee on 2018/1/2.
 */

public class LBSUtil {

    private static final String TAG = "test";

    private ProgressDialog progressDialog;
    private String provinceName;
    private String cityName;
    private String countyName;
    private int provinceId;
    private List<Province> provinces;
    private List<City> cities;
    private List<County> counties;
    private int cityId;
    private String weatherId;
    public Context context;
    private LBSCallback lbsCallback;

    public LBSUtil(String provinceName, String cityName, String countyName, Context context,
                   final LBSCallback lbsCallback) {
        this.provinceName = provinceName;
        this.cityName = cityName;
        this.countyName = countyName;
        this.context = context;
        this.lbsCallback=lbsCallback;
    }

    public String getWeatherId() {
        queryProvinces(provinceName);
        queryCities(cityName);
        queryCounties(countyName);
        return weatherId;
    }

    private void queryProvinces(String provinceName) {
        provinces = DataSupport.where("provinceName = ?", provinceName)
                .find(Province.class);
        if (provinces.size() > 0) {
            for (Province p : provinces) {
                provinceId = p.getProvinceCode();
            }
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromService(address, "province");
        }
    }

    private void queryCounties(String countyName) {
        counties = DataSupport.where("countyName = ?", countyName)
                .find(County.class);
        if (counties.size() > 0) {
            for (County c : counties) {
                weatherId = c.getWeatherId();
                lbsCallback.onGetLoction(weatherId);
                Log.d(TAG, weatherId);
            }
        } else {
            String address = "http://guolin.tech/api/china/" + provinceId + "/" + cityId;
            queryFromService(address, "county");
        }

    }

    private void queryCities(String cityName) {
        cities = DataSupport.where("cityName= ? ", cityName)
                .find(City.class);
        if (cities.size() > 0) {
            for (City c : cities) {
                cityId = c.getCityCode();
            }
        } else {
            String address = "http://guolin.tech/api/china/" + provinceId;
            queryFromService(address, "city");
        }
    }

    private void queryFromService(String address, final String type) {
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(context, "加载失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, provinceId);
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, cityId);
                }
                if (result) {
                    if ("province".equals(type)) {
                        queryProvinces(provinceName);
                    } else if ("city".equals(type)) {
                        queryCities(cityName);
                    } else if ("county".equals(type)) {
                        queryCounties(countyName);
                    }
                }
            }
        });
    }
}
