package com.NowTemp.NowTempApp.Class;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FineDustClass {
    /*
            Default 서울시청
                위도 y - 37.5662952
                경도 x - 126.9779451
                TM좌표 y - 451862.301323
                TM좌표 x - 198051.511154
         */
    public List<String> addr_list = new ArrayList<>();

    public List<AddrClass> addrclass_list = new ArrayList<>();

    public double[] coordinate = new double[]{126.9779451, 37.5662952};

    public double[] coordinate_TM = new double[]{451862.301323, 198051.511154};

    public static class AddrClass {
        String addr = "";
        String stationName = "";

        public String getAddr() { return addr; }
        public void setAddr(String addr) { this.addr = addr;}
        public String getStationName() { return stationName; }
        public void setStationName(String stationName) { this.stationName = stationName; }
    }

    public void FineDustClass(){ }

    public void setCoordinate(double[] coordinate) {
        this.coordinate = coordinate;
    }

    public double[] getCoordinate() {
        return coordinate;
    }

    public void setCoordinate_TM(double[] coordinate_TM) {
        this.coordinate_TM = coordinate_TM;
    }
    public double[] getCoordinate_TM() {
        return coordinate_TM;
    }

    public void setAddr_list(List<String> addr_list) {
        this.addr_list = addr_list;
    }
    public List<String> getAddr_list() {
        return addr_list;
    }

    public String getInfo(){
        String addr ="";
        String stationName ="";
        for (int i=0; i<addrclass_list.size(); i++){
            AddrClass addrClass = addrclass_list.get(i);
            addr += " "+addrClass.addr;
            stationName += " " +addrClass.stationName;
        }
        return String.format(" \n\tFineDustClass\n\t\taddr [%s]\n\t\tstationName[%s]", addr, stationName);
    }

    public String getResult(){
        String addr ="";
        String stationName ="";
        for (int i=0; i<addrclass_list.size(); i++){
            AddrClass addrClass = addrclass_list.get(i);
            addr += " "+addrClass.addr;
            stationName += " " +addrClass.stationName;
        }
        return String.format(" \n\t\taddr [%s]\n\t\tstationName[%s]", addr, stationName);
    }
}
