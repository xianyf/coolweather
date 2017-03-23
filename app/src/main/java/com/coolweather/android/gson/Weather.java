package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**  (0)
 * 在Weather类中.对Basic AQI Now Suggestion Forecast 类进行引用 ,因为 daily_forecast 中包含一个数组,
 * 因此使用了 List集合来引用Forecast类, 另外,返回的天气数据中还有一项 status : ok 或 fail ,别漏了
 *
 * 返回的数据大致格式
    {
          "HeWeather" :[
                           {
                                "status" :"ok",
                                "basic"  :{},
                                "aqi"    :{},
                                "now"    :{},
                                "suggestion":{},
                                "daily_forecast":[]
                           }
                      ]

    }
 */
public class Weather {

    public String status;

    public Basic basic;

    public AQI aqi;

    public Now now;

    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;

}
