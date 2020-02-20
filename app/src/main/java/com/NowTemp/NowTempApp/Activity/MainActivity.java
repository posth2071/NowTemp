package com.NowTemp.NowTempApp.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.NowTemp.NowTempApp.Class.DustClass;
import com.NowTemp.NowTempApp.Class.FineDustClass;
import com.NowTemp.NowTempApp.Class.WeatherClass;
import com.NowTemp.NowTempApp.NetworkStatus;
import com.NowTemp.NowTempApp.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kr.hyosang.coordinate.CoordPoint;
import kr.hyosang.coordinate.TransCoord;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    /*
    REST API
        1) 공공데이터 포털 OpenAPI (DUST_APIKEY)
                    측정소정보 조회 서비스 -> 근접측정소 목록 조회 (TM좌표 사용 TMRequest)
                    대기오염정보 조회 서비스 ->
        2) OpenWeatherMap API (WEATHER_APIKEY)

     */

    private static final String CAPTURE_PATH = "/CAPTURE_TEST";

    private LocationManager locationManager;

    public static ImageView main_iv_weather;
    public static TextView main_tv_addr, main_tv_temperature, main_tv_highset, main_tv_lowset, main_tv_weatherState;
    public static TextView main_tv_finedust, main_tv_ultra_finedust;
    public static TextView main_wind, main_humidity;
    public static Button main_setting, main_share;
    public static LinearLayout rootLayout;

    public static int[][] img_idSet = new int[2][6];

    public static FineDustClass fineDust = new FineDustClass();
    public static DustClass dust =  new DustClass();;
    public static WeatherClass weather = new WeatherClass();

    String[] permission_list = {
            Manifest.permission.WRITE_CONTACTS
    };

    public Context context;
    public static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        this.activity = this;

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        getImageResource();
        setContentView(R.layout.activity_main);

        // 인터넷 연결상태 확인 후 날씨 / 미세먼지 조회
        NetworkStatus.Check_NetworkStatus(context, 1);

        rootLayout = findViewById(R.id.rootLayout);

        main_tv_addr = findViewById(R.id.main_tv_addr);                 // 측정 주소 ex) 수원시

        main_tv_temperature = findViewById(R.id.main_tv_temperature);      // 현재 기온
        main_tv_highset = findViewById(R.id.main_tv_highset);              // 최고 기온
        main_tv_lowset = findViewById(R.id.main_tv_lowset);                // 최저 기온

        main_iv_weather = findViewById(R.id.main_iv_weather);              // 날씨 상태 이미지 ex) 맑음 / 구름 / 비
        main_tv_weatherState = findViewById(R.id.main_tv_weatherState);    // 날씨 상태 텍스트 ex) 맑음 / 구름 / 비

        main_tv_finedust = findViewById(R.id.main_fineDust);               // 미세먼지 농도 ex) 70㎍/m, 미세먼지 기준 ex) 나쁨 / 좋음 / 보통

        main_tv_ultra_finedust = findViewById(R.id.main_ultra_fineDust);     // 초미세먼지 농도 ex) 70㎍/m, 초미세먼지 기준 ex) 나쁨 / 좋음 / 보통

        main_humidity = findViewById(R.id.main_humidity);                   // 습도 ex) 87%
        main_wind = findViewById(R.id.main_wind);                           // 풍속 ex) 1.37m/s

        main_setting = findViewById(R.id.main_setting);                     // 설정버튼
        main_setting.setOnClickListener(this);

        main_share = findViewById(R.id.main_share);
        main_share.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_setting:
                    NetworkStatus.Check_NetworkStatus(context, 2);
                    //new DialogClass(context, 0, 2).show();
                break;
            case R.id.main_share:
                captureImage();
                break;
        }
    }

    // 화면캡쳐 함수
    public void captureImage(){
        Log.d("테스트", " \n\tcaptureImage 실행");
        // 권한 체크
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("에러","권한체크 if문들어옴");
            // 사용자 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("에러","권한체크 if문들어옴");
            // 사용자 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
        }

        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Dust";  // 저장폴더 경로
        View layout = getWindow().getDecorView().getRootView();                             // 캡쳐할영역(프레임레이아웃)

        File folder = new File(path);
        if(!folder.exists()){       // 저장소 내에 Dust폴더가 있는지
            folder.mkdirs();        // 없으면 생성
            Toast.makeText(context, "폴더가 생성되었습니다.", Toast.LENGTH_SHORT).show();
        }

        // 캡쳐파일 이름 ( Dust-연도-월일-시분초.jpeg )
        String filename = "/Dust-" + new SimpleDateFormat("yyyy-MMdd-HHmmss").format(new Date()) +".jpeg";
        File file = new File(path + filename);

        layout.buildDrawingCache();
        Bitmap captureview = layout.getDrawingCache();

        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(file);
            //fos = new FileOutputStream(path+"/Capture"+day.format(date)+".jpeg");

            captureview.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file)));
            Toast.makeText(context, "저장완료", Toast.LENGTH_SHORT).show();

            fos.flush();
            fos.close();
            layout.destroyDrawingCache();

            // 이미지 SNS전송
            Uri imageUri = FileProvider.getUriForFile(getApplicationContext(), "com.NowTemp.NowTempApp.fileprovider", file);
            sendSNS(imageUri);

        } catch (FileNotFoundException e) {
            Log.d("에러","FileNotFoundException Error \n\t"+e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("에러","IOException Error \n\t"+e.toString());
            e.printStackTrace();
        }
    }

    // 화면 SNS공유
    public void sendSNS(Uri imageUri){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        intent.putExtra(Intent.EXTRA_TEXT, "");     // TEXT 없애면 인스타그램은 전송안됨,
        startActivity(Intent.createChooser(intent, "send"));
    }

    //  ChangeTM, 좌표변환 함수 (위경도 -> TM좌표)
    public static void ChangeTM(Context context, int type, double x, double y) {
        Log.d("테스트", String.format(" \n\tChangeTM 좌표변환 실행\n\t\tx [%f], y [%f]", x, y));
        double[] before_PT = new double[2];
        double[] after_PT = new double[2];
        String before_type ="";
        String after_type = "";

        CoordPoint pt;
        CoordPoint ktmPt;

        switch (type){
            case 1:
                before_type = "위경도";
                after_type = "TM";
                pt = new CoordPoint(x, y);
                //좌표변환 getTransCoord(변환할 좌표, 이전타입, 변환후타입)
                ktmPt = TransCoord.getTransCoord(pt, TransCoord.COORD_TYPE_WGS84, TransCoord.COORD_TYPE_WTM);

                before_PT = new double[]{pt.x, pt.y};
                after_PT = new double[]{ktmPt.x, ktmPt.y};

                fineDust.setCoordinate(before_PT);
                fineDust.setCoordinate_TM(after_PT);
                break;

            case 2:
                before_type = "TM";
                after_type = "위경도";

                ktmPt = new CoordPoint(x, y);
                //좌표변환 getTransCoord(변환할 좌표, 이전타입, 변환후타입)
                pt = TransCoord.getTransCoord(ktmPt, TransCoord.COORD_TYPE_WTM, TransCoord.COORD_TYPE_WGS84);

                before_PT = new double[]{ktmPt.x, ktmPt.y};
                after_PT = new double[]{pt.x, pt.y};

                fineDust.setCoordinate_TM(before_PT);
                fineDust.setCoordinate(after_PT);
                break;
        }

        String message = String.format(" \n\tChangeTM 좌표변환 완료\n\t\t" +
                "기준좌표 '%s'\n\t\t\tx [%f], y [%f]\n\t\t변경좌표 '%s'\n\t\t\tx [%f], y [%f]",
                before_type, before_PT[0], before_PT[1],
                after_type, after_PT[0], after_PT[1]);
        Log.d("테스트", message);

        if(type == 1){
            //변환좌표 중심 주변 측정소 찾기
            try {
                TMRequest tmRequest = new TMRequest(context,0, "");
                tmRequest.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void GETLastLocation(Context context) {
        Log.d("테스트", " \n\tGETLastLocation 실행");

        LocationManager locationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 사용자 권한 요청
            ActivityCompat.requestPermissions(MainActivity.activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        }
        else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location == null){
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            double x = location.getLongitude();
            double y = location.getLatitude();
            Log.d("테스트", String.format(" \n\tGETLastLocation 종료\n\t\tx [%f], y [%f]", x, y));

            ChangeTM(context, 1, x, y);
            //getWeatherData(y, x);
        }
    }

    // 미세먼지, 날씨 이미지리소스 id반환
    public void getImageResource(){
        String img_name;
        Resources res = getResources();
        for(int i=0; i<6; i++){
            img_name = res.getString(res.getIdentifier("image_weather"+(i),"string", getPackageName()));
            img_idSet[0][i] = res.getIdentifier(img_name,"drawable", getPackageName());

            img_name = res.getString(res.getIdentifier("ic_weather"+(i),"string", getPackageName()));
            img_idSet[1][i] = res.getIdentifier(img_name,"drawable", getPackageName());
        }
    }

    public static void setting_value(DustClass dust, FineDustClass fine, WeatherClass weather){
        MainActivity.dust = dust;
        fineDust = fine;
        MainActivity.weather = weather;

        int img_id = weather.getWeather_image();

        main_tv_addr.setText(dust.getAddr());

        main_tv_finedust.setText(MainActivity.dust.getValue("PM10"));
        main_tv_ultra_finedust.setText(MainActivity.dust.getValue("PM25"));

        rootLayout.setBackgroundResource(img_idSet[0][img_id]);
        main_tv_temperature.setText(weather.getNowTemp());
        main_tv_highset.setText(weather.getMaxTemp());
        main_tv_lowset.setText(weather.getMinTemp());

        main_iv_weather.setImageResource(img_idSet[1][img_id]);
        main_tv_weatherState.setText(MainActivity.weather.getWeather_ID());

        main_wind.setText(MainActivity.weather.getWind());
        main_humidity.setText(MainActivity.weather.getHumidity());
    }
}
