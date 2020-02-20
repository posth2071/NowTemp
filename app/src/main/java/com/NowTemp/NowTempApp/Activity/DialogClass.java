package com.NowTemp.NowTempApp.Activity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.NowTemp.NowTempApp.Activity.MainActivity;
import com.NowTemp.NowTempApp.NetworkStatus;
import com.NowTemp.NowTempApp.R;


public class DialogClass extends Dialog implements View.OnClickListener{
    private final int[] DIALOG_LAYOUT = new int[]{R.layout.dialog_search, R.layout.dialog_network};
    private int type;
    private int network_type;

    private Context context;

    private EditText search_editText;
    private Button search_cancel;

    public DialogClass(@NonNull Context context, int type, int network_type) {
        super(context);
        this.context = context;
        this.type = type;
        if(type == 1){
            this.network_type = network_type;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setContentView(DIALOG_LAYOUT[type]);
        setCancelable(false);

        switch (type) {
            case 0:
                search_editText = findViewById(R.id.search_editText);
                search_editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
                search_editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            String searchName = search_editText.getText().toString();

                            // 입력주소값이 공백이거나 미입력인 경우
                            if ((searchName.compareTo("") == 0) || (searchName == null)) {
                                Toast.makeText(context, "주소 미입력", Toast.LENGTH_SHORT).show();
                            } else {
                                TMRequest tmRequest = new TMRequest(context, 1, searchName);
                                tmRequest.execute();
                                cancel();
                            }
                        }
                        return false;
                    }
                });
                search_cancel = findViewById(R.id.search_cancel);
                search_cancel.setOnClickListener(this);
                break;

            case 1:
                Button network_cacel = findViewById(R.id.network_cacnel);
                network_cacel.setOnClickListener(this);

                Button network_retry = findViewById(R.id.network_retry);
                network_retry.setOnClickListener(this);
                break;

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.search_cancel:
                cancel();
                break;
            case R.id.network_cacnel:
                Log.d("네트워크", "Cancel 클릭 network_type - "+network_type);
                if(network_type == 1){
                    Log.d("네트워크", "Cancel 클릭 if문 통과");
                    ActivityCompat.finishAffinity(MainActivity.activity);
                    System.exit(0);
                } else {
                    Log.d("네트워크", "Cancel 클릭 if문 미통과");
                    dismiss();
                }
                break;

            case R.id.network_retry:
                Log.d("네트워크", "Retry 클릭 network_type - "+network_type);
                dismiss();
                NetworkStatus.Check_NetworkStatus(context, network_type);
        }
    }

    private void setDialog(){

    }
}
