package pikachu.blackbox;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static String tel_id_file_name = "tel_id.dat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button exit_btn = (Button) findViewById(R.id.exit);

        Button police = (Button) findViewById(R.id.police);

        exit_btn.setOnClickListener(view -> {
            Intent intent = new Intent(this, MapSetting.class);
            startActivity(intent);
        });

        police.setOnClickListener(view -> {

        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(! new File(tel_id_file_name).exists()){ // 텔레그램 아이디 없을 경우 회원가입 창으로 넘기기
            Intent intent = new Intent(this, TelegramLoginActivity.class);
            startActivity(intent);
        }

    }
}
