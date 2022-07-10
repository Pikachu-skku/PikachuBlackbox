package pikachu.blackbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class TelegramLoginActivity extends AppCompatActivity {

    DatabaseReference RegisterRef = FirebaseDatabase.getInstance().getReference().child("REGISTER");

    DatabaseReference TeleRef;

    DatabaseReference SentCodeRef;

    DatabaseReference StatusRef;

    Boolean added_event_listen = false; // 파이어베이스 이벤트 리스너가 이미 추가된 상황인지 파악

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telegram_login);

        EditText telegram_id = (EditText) findViewById(R.id.telegram_id);

        Button id_button = (Button) findViewById(R.id.telegram_id_button);

        EditText telegram_code = (EditText) findViewById(R.id.register_code);

        Button code_button = (Button) findViewById(R.id.register_code_button);

        LinearLayout code_layout = (LinearLayout) findViewById(R.id.code_layout);

        id_button.setOnClickListener(view -> { // 아이디 입력시
            String tel_id = telegram_id.getText().toString();
            TeleRef = RegisterRef.child(tel_id).push();

            StatusRef = TeleRef.child("status").push();

            StatusRef.setValue(false);

            TeleRef.child("code").push().setValue(0);

            SentCodeRef = TeleRef.child("sent_code").push();

            SentCodeRef.setValue(0);

            code_layout.setVisibility(View.VISIBLE);
        });


        code_button.setOnClickListener(view -> { // 인증번호 입력시
            Integer sending_code = Integer.valueOf(telegram_code.getText().toString());
            TeleRef.child("sent_code").setValue(sending_code);
            telegram_code.setText("잠시 대기");

            if (! added_event_listen) {

                SentCodeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Integer sent_code = dataSnapshot.getValue(Integer.class);
                        if (sent_code == -1) {
                            telegram_code.setText("인증 번호 오류");
                            SentCodeRef.setValue(0);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

                StatusRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Boolean status = dataSnapshot.getValue(Boolean.class);

                        if (status) {
                            //인증 완료

                            try { // 텔레그램 아이디 저장
                                FileOutputStream fos = openFileOutput(MainActivity.tel_id_file_name, MODE_PRIVATE);
                                DataOutputStream dos = new DataOutputStream(fos);

                                dos.writeUTF(TeleRef.getKey());
                                dos.flush();
                                dos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            Intent intent = new Intent(TelegramLoginActivity.this, MainActivity.class);
                            startActivity(intent);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

                added_event_listen = true;
            }
        });
    }


}
