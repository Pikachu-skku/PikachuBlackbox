package pikachu.blackbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class MapSetting extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    GoogleMap g_map;

    LatLng lat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_setting);


        SupportMapFragment MapFragment = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map));

        MapFragment.getMapAsync(this);

        Button done_btn = (Button) findViewById(R.id.done);

        done_btn.setOnClickListener(view -> {
            Intent intent = new Intent(this, ExitActivity.class);
            intent.putExtra("lat", lat);
            startActivity(intent);
        });

    }

    @Override
    public void onMapReady(@NonNull final GoogleMap map){
        g_map = map;
        g_map.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        // latLng.latitude (위도) 와 latLng.longitude (경도) 를 활용하자
        lat = latLng;
    }
}