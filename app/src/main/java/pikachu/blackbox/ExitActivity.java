package pikachu.blackbox;

import static pikachu.blackbox.MainActivity.tel_id_file_name;
import static pikachu.blackbox.PopupActivity.POLICE_PHONE_NUMBER;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.PendingIntent;
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
import android.os.StrictMode;
import android.util.Log;
import android.widget.Button;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ExitActivity extends AppCompatActivity {

    public static LatLng final_lat;

    public static Location start_lat;

    public static LocationManager locationManager;

    public static DatabaseReference GPSsRef;

    public static Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exit);

        String tel_id = null;

        try {

            File file = new File(this.getFilesDir(), tel_id_file_name);

            int length = (int) file.length();

            byte[] bytes = new byte[length];

            FileInputStream in = new FileInputStream(file);

            in.read(bytes);

            in.close();

            tel_id = new String(bytes);

        }catch (IOException e){
            e.printStackTrace();
        }

        GPSsRef = FirebaseDatabase.getInstance().getReference().child("GPSs/" + tel_id);

        GPSsRef.child("status").setValue(true);

        GPSsRef.child("disconnected").setValue(false);

        Button done = findViewById(R.id.done);

        done.setOnClickListener(view -> {
            //TODO 비밀번호 물어보게 하기

            if (intent == null){
                intent = new Intent(getApplicationContext(), DangerCheckingService.class);
            }

            stopService(intent);

            GPSsRef.child("status").setValue(false);
            ArrayList<Double> now_loc = new ArrayList<>();
            now_loc.add(0.0);
            now_loc.add(0.0);
            GPSsRef.child("GPS").setValue(now_loc);
            GPSsRef.child("last_time").setValue(0);

            Intent intent2 = new Intent(ExitActivity.this, MainActivity.class);
            startActivity(intent2);
            finish();
        });

        Button police = findViewById(R.id.police);

        police.setOnClickListener(view -> {
            PopupActivity ac = new PopupActivity(ExitActivity.this, "응급상황");
            ac.show();
        });

        if(! getIntent().getExtras().getBoolean("isNew"))
        //if(true)
            return;


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        start_lat = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        final_lat = (LatLng) getIntent().getExtras().get("lat");

        intent = new Intent(getApplicationContext(), DangerCheckingService.class);

        intent.putExtra("start_lat", start_lat);

        intent.putExtra("final_lat", final_lat);

        startService(intent);

    }

    public void callPolice(){
        startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + POLICE_PHONE_NUMBER)));
    }
}