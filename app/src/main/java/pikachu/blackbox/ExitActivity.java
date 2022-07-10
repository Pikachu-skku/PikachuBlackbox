package pikachu.blackbox;

import static pikachu.blackbox.PopupActivity.POLICE_PHONE_NUMBER;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Button;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class ExitActivity extends AppCompatActivity {

    public static LatLng final_lat;

    public static Location start_lat;

    public static LocationManager locationManager;

    public static DatabaseReference GPSsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exit);


        final_lat = (LatLng) getIntent().getExtras().get("lat");

        try {

            FileInputStream fis = openFileInput("myFile.dat");
            DataInputStream dis = new DataInputStream(fis);

            String tel_id = dis.readUTF();


            GPSsRef = FirebaseDatabase.getInstance().getReference().child("GPSs").child(tel_id);

            dis.close();

        }catch (IOException e){
            e.printStackTrace();
        }

        GPSsRef.child("status").setValue(true);

        Button done = findViewById(R.id.done);
        
        done.setOnClickListener(view -> {
            //TODO 비밀번호 물어보게 하기

            Intent intent1 = new Intent(getApplicationContext(), DangerCheckingService.class);

            stopService(intent1);

            GPSsRef.child("status").setValue(false);
            GPSsRef.child("GPS").setValue(new Double[] {0.0, 0.0});
            GPSsRef.child("last_time").setValue(0);
        });

        Button police = findViewById(R.id.police);

        police.setOnClickListener(view -> {

            callPolice();

        });

        if (ContextCompat.checkSelfPermission( this,android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions(
                    this,
                    new String [] { android.Manifest.permission.ACCESS_COARSE_LOCATION },
                    11
            );
        }


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        start_lat = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        Intent intent = new Intent(getApplicationContext(), DangerCheckingService.class);

        startService(intent);

    }

    public void callPolice(){
        startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + POLICE_PHONE_NUMBER)));
    }
}