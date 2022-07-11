package pikachu.blackbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapSetting extends AppCompatActivity {

    LatLng lat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_setting);


        SupportMapFragment MapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));

        MapFragment.getMapAsync(map -> {
            // latLng.latitude (위도) 와 latLng.longitude (경도) 를 활용하자
            map.setOnMapClickListener(latLng -> {
                lat = latLng;
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(latLng.latitude + ":" + lat.longitude);
                map.clear();
                map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                map.addMarker(markerOptions);
            });
        });

        Button done_btn = findViewById(R.id.done);

        done_btn.setOnClickListener(view -> {
            Intent intent = new Intent(this, ExitActivity.class);
            intent.putExtra("lat", lat);
            intent.putExtra("isNew", true);
            startActivity(intent);
            finish();
        });

    }
}