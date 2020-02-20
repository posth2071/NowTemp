package com.NowTemp.NowTempApp.Class;

import android.app.Application;
import android.content.res.Resources;
import android.media.Image;
import android.util.Log;
import android.widget.ImageView;

public class DustClass extends Application {
    private final String FORMAT_DUST = "%d㎍/m³ %s";

    String addr;        // 주소 이름
    String stationName; // 측정소 이름
    int pm10Value;      // 미세먼지 농도
    String pm10State;   // 미세먼지 상태
    int pm25Value;      // 초미세먼지 농도
    String pm25State;   // 초미세먼지 상태
    int state_image;

    // 빈생성자
    public DustClass() {
    }

    // 생성자 (세팅까지)
    public DustClass(String dataTime, String pm10Value, String pm25Value) {
        if (pm10Value.compareTo("정보없음") != 0) {
            int pm10 = Integer.parseInt(pm10Value);
            int pm25 = Integer.parseInt(pm25Value);
            this.pm10Value = pm10;
            this.pm10State = dustCheck(10, pm10);
            this.pm25Value = pm25;
            this.pm25State = dustCheck(25, pm25);
        } else {
            this.pm10Value = 0;
            this.pm10State = pm10Value;
            this.pm25Value = 0;
            this.pm25State = pm25Value;
        }
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getAddr() {
        return addr;
    }


    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getStationName() {
        return stationName;
    }

    // 미세먼지 설정/반환
    public void setPm10Value(String pm10Value) {
        this.pm10Value = Integer.parseInt(pm10Value);
        this.pm10State = dustCheck(10, Integer.parseInt(pm10Value));
    }

    public int getPm10Value() {
        return pm10Value;
    }

    public String getPm10State() {
        return pm10State;
    }

    // 초미세먼지 설정/반환
    public void setPm25Value(String pm25Value) {
        this.pm25Value = Integer.parseInt(pm25Value);
        this.pm25State = dustCheck(25, Integer.parseInt(pm25Value));
    }

    public int getPm25Value() {
        return pm25Value;
    }

    public String getPm25State() {
        return pm25State;
    }

    //하나씩 설정
    public void setValue(String key, String value) {
        switch (key) {
            case "pm10Value":
                if (value.compareTo("정보없음") == 0) {
                    this.pm10Value = 0;
                    this.pm10State = "-";
                } else {
                    int dust10 = Integer.parseInt(value);
                    this.pm10Value = dust10;
                    this.pm10State = dustCheck(10, dust10);
                }
                break;
            case "pm25Value":
                if (value.compareTo("정보없음") == 0) {
                    this.pm25Value = 0;
                    this.pm25State = "-";
                } else {
                    int dust25 = Integer.parseInt(value);
                    this.pm25Value = dust25;
                    this.pm25State = dustCheck(25, dust25);
                }
                break;
        }
    }

    // 미세먼지 기준 체크
    private String dustCheck(int type, int dustValue) {
        switch (type) {
            case 10:
                if (dustValue <= 30) {
                    return "좋음";
                } else if (dustValue <= 80) {
                    return "보통";
                } else if (dustValue <= 150) {
                    return "나쁨";
                } else {
                    return "매우나쁨";
                }
            case 25:
                if (dustValue <= 15) {
                    return "좋음";
                } else if (dustValue <= 35) {
                    return "보통";
                } else if (dustValue <= 75) {
                    return "나쁨";
                } else {
                    return "매우나쁨";
                }
        }
        return "-";
    }

    // DustClass 정보 전체반환
    public String getInfo(){
        String info = String.format(
                                " \n\tDustClass\n\t\t"+
                                "측정장소 [%s] 측정소 [%s]\n\t\t" +
                                "미세먼지 [%d] %s\n\t\t" +
                                "초미세먼지 [%d] %s",
                                addr,
                                stationName,
                                pm10Value,
                                pm10State,
                                pm25Value,
                                pm25State);
        return info;
    }

    public String getValue(String type){
        String result;
        if(type.compareTo("PM10")==0){
           return String.format(FORMAT_DUST, pm10Value, pm10State);
        } else if(type.compareTo("PM25")==0){
            return String.format(FORMAT_DUST, pm25Value, pm25State);
        } else {
            return String.format(FORMAT_DUST, 0, "정보없음");
        }
    }
}
