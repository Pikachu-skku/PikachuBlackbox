package pikachu.blackbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PopupActivity extends Dialog {

    public static final String POLICE_PHONE_NUMBER = "010";

    ProgressHandler handler;

    Long last_time = System.currentTimeMillis() + 1000 * 60 * 60 * 6;
    Long now_time = System.currentTimeMillis();

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    TextView date_view;
    TextView reason_view;

    Context context;

    public PopupActivity(@NonNull Context context, String contents){

        super(context);

        this.context = context;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_popup);

        handler = new ProgressHandler();

        date_view = findViewById(R.id.date); // 남은 시간

        reason_view = findViewById(R.id.reason); // 이유

        reason_view.setText(contents);

        date_view.setText(sdf.format(new Date(1000 * 60 * 60 * 6)));

        runTime();

        Button police_btn = findViewById(R.id.police);

        Button safe_btn = findViewById(R.id.safe);

        police_btn.setOnClickListener(view -> {
            date_view.setText("경찰에게 신고합니다.");
            callPolice();
            police_btn.setVisibility(View.INVISIBLE);
        });

        safe_btn.setOnClickListener(view -> {
            handler.removeCallbacksAndMessages(null);
            //TODO 비밀번호 물어보게 하기
            dismiss();
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return event.getAction() != MotionEvent.ACTION_OUTSIDE;
    }

    @Override
    public void onBackPressed() {
    }

    public void callPolice(){
        context.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + POLICE_PHONE_NUMBER)));
    }

    public void runTime(){
        Thread thread = new Thread(() -> {
            while(true){
                now_time = System.currentTimeMillis();

                Message message = handler.obtainMessage();
                handler.sendMessage(message);

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    class ProgressHandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            long left_time = last_time - now_time;
            if(left_time <= 0){
                date_view.setText("경찰에게 신고합니다.");
                callPolice();
           }else{
                date_view.setText(sdf.format(new Date(left_time)));
            }
        }
    }
}