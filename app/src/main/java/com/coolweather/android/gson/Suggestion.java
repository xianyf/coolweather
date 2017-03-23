package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/** (4)
    "suggestion" : {
                      "comf"  :  {"txt" : "白天天气热 ,有雨,不舒适"},
                     "cw"     :  {"txt" : "不宜洗车"},
                      "sport" :  {"txt" : "室内运动"}
                   }

 */
public class Suggestion {

    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("cw")
    public CarWash carWash;

    public Sport sport;

    public class Comfort {

        @SerializedName("txt")
        public String info;

    }

    public class CarWash {

        @SerializedName("txt")
        public String info;

    }

    public class Sport {

        @SerializedName("txt")
        public String info;

    }

}
