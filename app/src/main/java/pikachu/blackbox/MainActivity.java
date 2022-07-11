package pikachu.blackbox;

import static pikachu.blackbox.PopupActivity.POLICE_PHONE_NUMBER;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    public static String tel_id_file_name = "tel_id.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(! new File(this.getFilesDir(), tel_id_file_name).exists()){ // 텔레그램 아이디 없을 경우 회원가입 창으로 넘기기
            Intent intent = new Intent(this, TelegramLoginActivity.class);
            startActivity(intent);
            finish();
        }

        Button exit_btn = findViewById(R.id.exit);

        Button police = findViewById(R.id.police);

        exit_btn.setOnClickListener(view -> {
            Intent intent = new Intent(this, MapSetting.class);
            startActivity(intent);
            finish();
        });

        police.setOnClickListener(view -> {
            startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + POLICE_PHONE_NUMBER)));
        });

    }
}
