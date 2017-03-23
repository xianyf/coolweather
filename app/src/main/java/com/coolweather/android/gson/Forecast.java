package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**  (5) 数组中每一项代表未来一天的天气
        "daily_forecast" :[
                              {
                                  "date":"2017-01-01",
                                  "cond":{"txt_d":"阵雨"},
                                  "tmp":{"max":"34", "min":"27"}
                               },
                              {
                                  "date":"2017-01-02",
                                  "cond":{"txt_d":"多云"},
                                  "tmp":{"max":"30", "min":"17"}
                               },
                               ...
                            ]

 */
public class Forecast {

    public String date;


    @SerializedName("cond")
    public More more;


    @SerializedName("tmp")
    public Temperature temperature;



    public class More {

        @SerializedName("txt_d")
        public String info;

    }


    public class Temperature {

        public String max;

        public String min;

    }



}
