package com.NowTemp.NowTempApp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.NowTemp.NowTempApp.Activity.DialogClass;
import com.NowTemp.NowTempApp.Activity.MainActivity;

public class NetworkStatus {
    public static final int TYPE_NOT_CONNECTED = 0;
    public static final int TYPE_MOBILE = 1;
    public static final int TYPE_WIFI = 2;

    /*
        네트워크 연결상태를 얻기 위한 메소드
            반환값 - (0) 미연결 (1) 모바일데이터 (2) 와이파이
     */
    public static int getConnectivity_Status(Context context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // 네트워크 연결상태 얻어오기 - 연결안되었다면 null 반환
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        // 연결된 상태라면 (null이 아니라면)
        if(networkInfo != null) {
            // 연결된 네트워크 종류 얻기
            int type = networkInfo.getType();
            // 모바일 네트워크에 연결된 상태라면 - int 1 반환
            if (type == ConnectivityManager.TYPE_MOBILE) {
                return TYPE_MOBILE;
                // 와이파이에 연결된 상태라면 - int 2 반환
            } else if (type == ConnectivityManager.TYPE_WIFI) {
                return TYPE_WIFI;
            }
        }
        // 연결이 되지않았다면 - int 0 반환
        return TYPE_NOT_CONNECTED;
    }

    public static void Check_NetworkStatus(final Context context, final int network_type){
         if (getConnectivity_Status(context) == 0) {
            Log.d("다이얼로그", "DialogClass 생성자 함수 실행");
            new DialogClass(context, 1, network_type).show();
            Log.d("다이얼로그", "DialogClass.show()");
        } else {
            // 인터넷 상태가 연결되어 있는 경우
            switch (network_type){
                case 1:        // 어플 첫실행인경우
                    MainActivity.GETLastLocation(context);
                    break;
                case 2:
                    new DialogClass(context, 0, network_type).show();
                    break;

            }

        }
    }
}