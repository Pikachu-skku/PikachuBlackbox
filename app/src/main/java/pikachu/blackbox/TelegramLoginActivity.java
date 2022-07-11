package pikachu.blackbox;

import static pikachu.blackbox.MainActivity.tel_id_file_name;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class TelegramLoginActivity extends AppCompatActivity {

    DatabaseReference RegisterRef = FirebaseDatabase.getInstance().getReference().child("REGISTER");

    DatabaseReference TeleRef;

    Boolean added_event_listen = false; // 파이어베이스 이벤트 리스너가 이미 추가된 상황인지 파악

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telegram_login);

        EditText telegram_id = findViewById(R.id.telegram_id);

        Button id_button = findViewById(R.id.telegram_id_button);

        EditText telegram_code =  findViewById(R.id.register_code);

        Button code_button = findViewById(R.id.register_code_button);

        LinearLayout code_layout = findViewById(R.id.code_layout);

        id_button.setOnClickListener(view -> { // 아이디 입력시
            String tel_id = telegram_id.getText().toString();

            Map<String, Object> childData = new HashMap<>();

            childData.put("status", false);
            childData.put("code", 0);
            childData.put("sent_code", 0);

            Map<String, Object> childUpdate = new HashMap<>();

            childUpdate.put(tel_id, childData);

            RegisterRef.updateChildren(childUpdate);

            TeleRef = RegisterRef.child(tel_id);

            code_layout.setVisibility(View.VISIBLE);
        });


        code_button.setOnClickListener(view -> { // 인증번호 입력시
            Integer sending_code = Integer.valueOf(telegram_code.getText().toString());
            TeleRef.child("sent_code").setValue(sending_code);
            Toast.makeText(this, "잠시 대기", Toast.LENGTH_SHORT).show();

            if (! added_event_listen) {
                Toast.makeText(this, "보여줘", Toast.LENGTH_SHORT).show();

                TeleRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Boolean status = snapshot.child("status").getValue(Boolean.class);

                        if (status) {
                            //인증 완료

                            try { // 텔레그램 아이디 저장
                                Toast.makeText(TelegramLoginActivity.this, System.getProperty("user.dir"), Toast.LENGTH_SHORT).show();

                                File file = new File(TelegramLoginActivity.this.getFilesDir(), tel_id_file_name);

                                if (! file.exists()){
                                    Toast.makeText(TelegramLoginActivity.this, "GO", Toast.LENGTH_SHORT).show();
                                    file.createNewFile();
                                }

                                FileOutputStream stream = new FileOutputStream(file);

                                stream.write(TeleRef.getKey().getBytes(StandardCharsets.UTF_8));

                                stream.close();


                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            Toast.makeText(TelegramLoginActivity.this, "2", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(TelegramLoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();

                        }else{
                            Integer sent_code = snapshot.child("sent_code").getValue(Integer.class);
                            if (sent_code == -1) {
                                Toast.makeText(TelegramLoginActivity.this, "인증 번호 오류", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                added_event_listen = true;
            }
        });
    }


}
