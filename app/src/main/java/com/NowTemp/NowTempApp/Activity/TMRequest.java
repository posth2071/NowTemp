package com.NowTemp.NowTempApp.Activity;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.NowTemp.NowTempApp.Activity.MainActivity;
import com.NowTemp.NowTempApp.Class.DustClass;
import com.NowTemp.NowTempApp.Class.FineDustClass;
import com.NowTemp.NowTempApp.Class.WeatherClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

// TMRequest - TM좌표 기준 주변 측정소 찾기
class TMRequest extends AsyncTask<Double, String, Integer> {

    public static final String DUST_APIKEY = "QirI3%2BNELvsWCGN6oiO02NYzsw35XonkvH%2BhDZf5ZjDYsw4Lf%2BH0QwrJLdAE9D5Bk7npwPHGUnHwHlNofFGC0g%3D%3D";
    //TM기준좌표 조회 (읍면동 이름으로 검색)
    public static String TM_STATION_URL = "http://openapi.airkorea.or.kr/openapi/services/rest/MsrstnInfoInqireSvc/getTMStdrCrdnt";
    //근접측정소 목록 조회 (TM좌표이용 검색)
    private static String TMREQUEST_URL = "http://openapi.airkorea.or.kr/openapi/services/rest/MsrstnInfoInqireSvc/getNearbyMsrstnList?tmX=%f&tmY=%f&ServiceKey=%s&_returnType=json";

    // 측정소별 실시간 측정정보 조회 API 주소
    private static String DUST_API_URL = "http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty";

    public static final String WEATHER_APIKEY = "a25cbca9a26d466181dce6882d2ad88e";
    public static final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&units=metric&appid=%s";

    private final String[] TAGNAME = {"pm10Value", "pm25Value"};

    DustClass dust = MainActivity.dust;
    FineDustClass fineDust = MainActivity.fineDust;
    WeatherClass weather = MainActivity.weather;

    private int type = 0;
    private String searchName = "";

    private Context context;

    public TMRequest(Context context, int type, String searchName){
        this.context = context;
        this.type = type;
        this.searchName = searchName;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        Toast.makeText(context, values[0], Toast.LENGTH_SHORT).show();
        super.onProgressUpdate(values);
    }

    @Override
    protected Integer doInBackground(Double... doubles) {
        Log.d("테스트", String.format(" \n\tdoInBackground 실행, type [%d], searchName [%s]", type, searchName));
        //Log.d("테스트", " \n\tTMRequest 실행_TM좌표 근접측정소 찾기\n\t\tx - " + tmXY[0] + ", y - " + tmXY[1]);
        try {
            String api_url = "";
            URL url;
            HttpURLConnection conn;

            // type == 1 일때 (주소명 검색일경우)
            if(type == 1){
                // TM_STATION_URL 검색 (TM기준좌표 조회 - 읍면동 검색)
                StringBuilder urlBuilder = new StringBuilder(TM_STATION_URL); /*URL*/
                urlBuilder.append("?" + URLEncoder.encode("ServiceKey","UTF-8") + String.format("=%s", DUST_APIKEY)); /*Service Key*/
                urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("10", "UTF-8")); /*한 페이지 결과 수*/
                urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지 번호*/
                urlBuilder.append("&" + URLEncoder.encode("umdName","UTF-8") + "=" + URLEncoder.encode(searchName, "UTF-8")); /*읍면동명*/

                Log.d("테스트", String.format(" \n\tTM_STATION_API TM기준좌표 조회\n\t\t검색주소 [%s]\n\t\tURL [%s]",searchName, urlBuilder.toString()));

                url = new URL(urlBuilder.toString());

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-type", "application/json");
                conn.connect();

                api_station(conn);
            }

            double[] tmXY = fineDust.getCoordinate_TM();
            api_url = String.format(TMREQUEST_URL, tmXY[0], tmXY[1], DUST_APIKEY);
            Log.d("테스트", String.format(" \n\tTM_REQUEST API 근접측정소 목록 조회\n\t\tTM좌표 - tmX [%f], tmY [%f]\n\t\tURL [%s]",tmXY[0], tmXY[1], api_url));

            url = new URL(api_url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");
            conn.connect();

            api_tmrequest(conn);

             /*
                미세먼지 지수 측정 시작
                추출할 TAG목록
                addr - 측정장소
                dataTime - 측정시간 ex) 2016-04-20 14:00
                pm10Value - 미세먼지(PM10) 농도 (단위 : ㎍/㎥)
                pm25Value - 초미세먼지(PM2.5) 농도 (단위 : ㎍/㎥)
             */
            int index = 0;
            double[] values;
            do {
                Log.d("테스트", " \n\tDUST API, do~while문 진입 index - "+index);
                if (index == 3) {
                    Log.d("테스트", " \n\tDUST API 실패, 근접 측정소 3개, 측정값 미존재");
                    break;
                }

                FineDustClass.AddrClass addrClass = fineDust.addrclass_list.get(index);
                String addr = addrClass.getAddr();
                String stationName = addrClass.getStationName();


                StringBuilder urlBuilder = new StringBuilder(DUST_API_URL); /*URL*/
                urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + String.format("=%s", DUST_APIKEY)); /*Service Key*/
                urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("10", "UTF-8")); /*한 페이지 결과 수*/
                urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지 번호*/
                urlBuilder.append("&" + URLEncoder.encode("stationName", "UTF-8") + "=" + URLEncoder.encode(stationName, "UTF-8")); /*측정소 이름*/
                urlBuilder.append("&" + URLEncoder.encode("dataTerm", "UTF-8") + "=" + URLEncoder.encode("DAILY", "UTF-8")); /*요청 데이터기간 (하루 : DAILY, 한달 : MONTH, 3달 : 3MONTH)*/
                urlBuilder.append("&" + URLEncoder.encode("ver", "UTF-8") + "=" + URLEncoder.encode("1.3", "UTF-8")); /*버전별 상세 결과 참고문서 참조*/

                url = new URL(urlBuilder.toString());
                Log.d("테스트", String.format(" \n\tDUST API 측정소별 측정값 조회\n\t\t측정소이름 [%s]\n\t\tURL [%s]",stationName, api_url));

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-type", "application/json");

                values = api_dust(conn, addr, stationName);
                index += 1;
                Log.d("테스트", " \n\tDUST API while 검사");
            } while ((values[0] == 0) && (values[1] == 0));


            // OpenweatherMap API 실행 - 날씨 정보
            double[] coordinate = fineDust.getCoordinate();
            api_url = String.format(WEATHER_URL, coordinate[1], coordinate[0], WEATHER_APIKEY);
            Log.d("테스트", String.format(" \n\tWEATHER API 날씨정보 조회\n\t\t위경도 좌표 x [%f] y [%f]\n\t\tURL [%s]",coordinate[1], coordinate[0], api_url));

            url = new URL(api_url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            if(api_weather(conn, coordinate)==1){
                cancel(true);
            }

            conn.disconnect();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.d("예외", "에러 UnsupportedEncodingException - " + e.getMessage() +", "+e.toString());
            publishProgress("EncodingException 예외");
            cancel(true);
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.d("예외", "에러 IOException - " + ex.getMessage() +", "+ex.toString());
            publishProgress("IOException 예외");
            cancel(true);
        } catch (Exception et) {
            et.printStackTrace();
            Log.d("예외", "에러 Exception - " + et.getMessage() +", "+et.toString());
            publishProgress("Exception 예외");
            cancel(true);
        }
        return 0;
    }
    @Override
    protected void onPostExecute(Integer integer) {
        Log.d("테스트", "onPostExecute 실행 결과 0정상종료 ["+ integer +"]");

        StringBuilder builder = new StringBuilder().append(fineDust.getInfo()).append(dust.getInfo()).append(weather.getInfo());
        Log.d("테스트", builder.toString());

        MainActivity.setting_value(dust, fineDust, weather);

        cancel(true);
        super.onPostExecute(integer);
    }

    private void api_station(HttpURLConnection conn) throws IOException, ParserConfigurationException, SAXException {
        // REST API 응답코드 확인
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(conn.getInputStream(), "UTF-8");

            NodeList nodeList = doc.getElementsByTagName("item");
            if(nodeList.getLength()!=0){
                Node nodetest = nodeList.item(0);
                Element ele = (Element) ((Element) nodetest).getElementsByTagName("tmX").item(0);
                double tmX = Double.parseDouble(ele.getChildNodes().item(0).getNodeValue());

                ele = (Element) ((Element) nodetest).getElementsByTagName("tmY").item(0);
                double tmY = Double.parseDouble(ele.getChildNodes().item(0).getNodeValue());

                Log.d("테스트", String.format(" \n\tTM_STATION_API TM기준좌표 결과\n\t\t검색주소 [%s]\n\t\tTM좌표 tmX [%f], tmY[%f]",searchName, tmX, tmY));

                MainActivity.ChangeTM(context,2, tmX, tmY);
            } else {
                conn.disconnect();
                Log.d("테스트",String.format(" \n\tTM_STATION_API 검색결과 미존재\n\t\t결과개수 [%d]", nodeList.getLength()));
                publishProgress("주소 미존재");

                cancel(true);
            }
        } else {
            // REST API 응답이 에러인경우
            conn.disconnect();
            Log.d("테스트",String.format(" \n\tTM_STATION REST API 응답에러 Code [%d]", conn.getResponseCode()));
            publishProgress("TM기준좌표 조회\n응답 에러");

            cancel(true);
        }
    }

    private void api_tmrequest(HttpURLConnection conn) throws IOException, JSONException {
        BufferedReader rd;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();

        Log.d("테스트", String.format(" \n\tTM_REQUEST API 근접측정소 목록 조회\n\t\t응답내용 [%s]",sb));

        JSONArray jsonArray = new JSONObject(sb.toString()).getJSONArray("list");
        List<FineDustClass.AddrClass> addr_list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            FineDustClass.AddrClass addrClass = new FineDustClass.AddrClass();
            String addr = jsonArray.getJSONObject(i).optString("addr").split(" ")[1];
            addrClass.setAddr(addr);

            String stationName = jsonArray.getJSONObject(i).optString("stationName");
            addrClass.setStationName(stationName);

            addr_list.add(addrClass);
            //list.add(jsonArray.getJSONObject(i).optString("addr"));
        }
        fineDust.addrclass_list.clear();
        fineDust.addrclass_list.addAll(addr_list);

        Log.d("테스트", String.format(" \n\tTM_REQUEST API 근접측정소 목록 결과%s", fineDust.getResult()));
    }

    private double[] api_dust(HttpURLConnection conn, String addr, String stationName) throws IOException, ParserConfigurationException, SAXException {
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(conn.getInputStream(), "UTF-8");

            // <item> 태그를 기준으로 나누기 - List반환
            NodeList nodeList = doc.getElementsByTagName("item");
            Element ele;

            // API 결과값이 하나라도 존재하는지 체크
            if (nodeList.getLength() != 0) {
                Node nodetest = nodeList.item(0);
                dust.setAddr(addr);
                dust.setStationName(stationName);
                Log.d("테스트", String.format("Addr [%s] stationName [%s]", addr, stationName));
                for (int i = 0; i < TAGNAME.length; i++) {
                    ele = (Element) ((Element) nodetest).getElementsByTagName(TAGNAME[i]).item(0);
                    String result = ele.getChildNodes().item(0).getNodeValue();
                     if ((result.compareTo("-") == 0) || (result.isEmpty())) {
                         dust.setValue(TAGNAME[i], "정보없음");
                    } else {
                         dust.setValue(TAGNAME[i], result);
                    }
                }
                Log.d("테스트", String.format(" \n\tDUST API 측정소별 측정값 조회 결과\n\t\t장소 [%s] 측정소이름 [%s]\n\t\tpm10 [%d], pm25 [%d]", addr, stationName,dust.getPm10Value(), dust.getPm25Value()));
            } else {
                Log.d("테스트", String.format(" \n\tDUST API '%s'측정소 측정값 미존재", stationName));

                dust.setAddr(stationName);
                dust.setValue(TAGNAME[0], "정보없음");
                dust.setValue(TAGNAME[1], "정보없음");
            }
        } else {
            Log.d("테스트", String.format(" \n\tDUST API 측정소별 측정값 조회\n\t\t응답결과 에러_Code [%d]",conn.getResponseCode()));
            // 응답결과 에러인경우
            dust.setAddr(addr);
            dust.setStationName(stationName);
            dust.setValue(TAGNAME[0], "정보없음");
            dust.setValue(TAGNAME[1], "정보없음");
        }
        return new double[]{dust.getPm10Value(), dust.getPm25Value()};
    }

    private int api_weather(HttpURLConnection conn, double[] coordinate) throws IOException, JSONException {
          // REST 응답결과 확인 HTTP_OK 정상응답일 경우
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream is = conn.getInputStream();
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader in = new BufferedReader(reader);

            String readed;
            JSONObject jsonObject = new JSONObject();
            while ((readed = in.readLine()) != null) {
                jsonObject = new JSONObject(readed);
            }
            Log.d("테스트", String.format(" \n\tWEATHER API 날씨정보 조회 결과%s", jsonObject.toString()));

            weather.setCoordinate(coordinate);

            weather.setWeather_ID(jsonObject.getJSONArray("weather").getJSONObject(0).getString("id"));

            JSONObject main_Object = jsonObject.getJSONObject("main");

            weather.setNowTemp(main_Object.getString("temp"));
            weather.setMinTemp(main_Object.getString("temp_min"));
            weather.setMaxTemp(main_Object.getString("temp_max"));

            weather.setHumidity(main_Object.getString("humidity"));
            JSONObject wind_Object = jsonObject.getJSONObject("wind");
            weather.setWind(wind_Object.getString("speed"));

            Log.d("테스트", String.format(" \n\tWEATHER API 날씨정보 조회 결과\n\t\t%s", weather.getInfo()));
            return 0;
        } else{
            // REST 응답결과 에러인 경우
            Log.d("테스트", String.format(" \n\tWEATHER API 날씨정보 조회 실패\n\t\t에러 code [%d]", conn.getResponseCode()));
            return 1;
        }
    }
}