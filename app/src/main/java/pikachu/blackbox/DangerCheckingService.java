package pikachu.blackbox;

import static pikachu.blackbox.ExitActivity.GPSsRef;
import static pikachu.blackbox.ExitActivity.final_lat;
import static pikachu.blackbox.ExitActivity.locationManager;
import static pikachu.blackbox.ExitActivity.start_lat;
import static pikachu.blackbox.MainActivity.tel_id_file_name;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.Function;

public class DangerCheckingService extends Service {

    boolean isRun = false;

    SensorManager SensorManager;
    Sensor GgyroSensor = null;
    SensorEventListener SListen;
    float x, y, z;

    BluetoothAdapter mBluetoothAdapter;

    EmergencyCheckingThread thread;

    @Override
    public void onCreate() {
        super.onCreate();

        SensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Using the Accelometer
        GgyroSensor = SensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        SListen = new GyroscopeListener();

        SensorManager.registerListener(SListen, GgyroSensor, android.hardware.SensorManager.SENSOR_DELAY_UI);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "??????????????? ???????????? ?????? ???????????????.", Toast.LENGTH_LONG).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "??????????????? ????????? ?????? ?????? ????????????.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("?????????", "?????? ?????????.");
        if (intent == null) {
            return START_STICKY;
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Location start_lat = (Location) intent.getExtras().get("start_lat");

        LatLng final_lat = (LatLng) intent.getExtras().get("final_lat");

        String tel_id = null;

        try {

            File file = new File(this.getFilesDir(), tel_id_file_name);

            int length = (int) file.length();

            // ?????? ??????

            byte[] bytes = new byte[length];

            FileInputStream in = new FileInputStream(file);

            in.read(bytes);

            in.close();

            tel_id = new String(bytes);

        } catch (IOException e) {
            e.printStackTrace();
        }

        FirebaseApp.initializeApp(this);

        GPSsRef = FirebaseDatabase.getInstance().getReference().child("GPSs/" + tel_id);


        Intent testIntent = new Intent(getApplicationContext(), ExitActivity.class);
        testIntent.putExtra("isNew", false);
        PendingIntent pendingIntent
                = PendingIntent.getActivity(this, 0, testIntent, PendingIntent.FLAG_CANCEL_CURRENT);


        NotificationChannel channel = new NotificationChannel("channel", "play!!",
                NotificationManager.IMPORTANCE_DEFAULT);

        // Notification??? ?????? ??????
        NotificationManager mNotificationManager = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
        mNotificationManager.createNotificationChannel(channel);

        // Notification ??????
        NotificationCompat.Builder notification
                = new NotificationCompat.Builder(getApplicationContext(), "channel")
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle("?????? ?????????")
                .setContentIntent(pendingIntent)
                .setContentText("?????? ??????");

        // id ?????? 0?????? ??? ????????? ???????????? ??????.
        mNotificationManager.notify(1, notification.build());
        // foreground?????? ??????
        startForeground(1, notification.build());

        Log.v("????????????????????????", start_lat.toString());

        isRun = true;

        thread = new EmergencyCheckingThread(locationManager, start_lat, final_lat, SensorManager, GgyroSensor, SListen, GPSsRef, mBluetoothAdapter);

        thread.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        isRun = false;

        try {
            thread.join();
            thread.interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SensorManager.unregisterListener(SListen);
    }

    private class GyroscopeListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }

    private class EmergencyCheckingThread extends Thread {

        LocationManager locationManager;
        Location start_lat;
        LatLng final_lat;

        SensorManager SensorManager;
        Sensor GgyroSensor;
        SensorEventListener SListen;
        float x, y, z;
        DatabaseReference GPSsRef;

        BluetoothAdapter mBluetoothAdapter;

        public EmergencyCheckingThread(
                LocationManager locationManager,
                Location start_lat,
                LatLng final_lat,
                SensorManager SensorManager,
                Sensor GgyroSensor,
                SensorEventListener SListen,
                DatabaseReference GPSsRef,
                BluetoothAdapter mBluetoothAdapter
        ) {
            this.locationManager = locationManager;
            this.start_lat = start_lat;
            this.final_lat = final_lat;
            this.SensorManager = SensorManager;
            this.GgyroSensor = GgyroSensor;
            this.SListen = SListen;
            this.GPSsRef = GPSsRef;
            this.mBluetoothAdapter = mBluetoothAdapter;
        }


        @Override
        public void run() {
            long time_firebase = System.currentTimeMillis();

            long time_location = System.currentTimeMillis();

            Location last_loc = null;

            try {
                while (isRun) {

                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    Double x_11 = start_lat.getLatitude();
                    Double y_11 = start_lat.getLongitude();

                    Double x_12 = final_lat.latitude;
                    Double y_12 = final_lat.longitude;

                    Double distance_standard_move = Math.pow(Math.pow(x_12 - x_11, 2) + Math.pow(y_12 - y_11, 2) , 1/2) + 0.5 / 111;  // ????????? ?????? ???????
                    Double distance_standard_notmove = 0.01 / 111; // ????????? ??? ???????????? ???????

                    Function<Location, Double> distance_f = (location) -> {

                        Double x = location.getLatitude();
                        Double y = location.getLongitude();

                        Double x_1 = start_lat.getLatitude();
                        Double y_1 = start_lat.getLongitude();

                        Double x_2 = final_lat.latitude;
                        Double y_2 = final_lat.longitude;

                        return Math.pow(Math.pow(x - x_1, 2) + Math.pow(y - y_1, 2) , 1/2) + Math.pow(Math.pow(x - x_2, 2) + Math.pow(y - y_2, 2) , 1/2);
                    };

                    String reason = "";

                    //?????? ?????? ??? ????????? ?????? ?????? ??????

                    if (distance_f.apply(loc) >= distance_standard_move) { // ?????? ?????? ??????
                        reason = "????????? ????????? ?????? ?????????.";
                    } else { // ????????? ????????? ??????
                        if (last_loc != null) {
                            Double distance = Math.pow(Math.pow(loc.getLatitude() - last_loc.getLatitude(), 2) + Math.pow(loc.getLongitude() - last_loc.getLongitude(), 2), 1 / 2);

                            last_loc = loc;

                            if (System.currentTimeMillis() - time_location >= 60 * 1000) {
                                if (distance >= distance_standard_notmove) {
                                    reason = "?????? ?????? ??????";
                                }
                                time_location = System.currentTimeMillis();
                            }
                        } else {
                            last_loc = loc;
                        }
                    }


                    //TODO ??????????????? ??????

                    //???????????? ??????
                    if (mBluetoothAdapter != null) {
                        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();

                        if (devices.size() > 0) {
                            for (BluetoothDevice device : devices) {
                                String[] a = device.getName().split("-");
                                if (a[0].equals("Pikachu")) {
                                    loc.setLatitude(Double.parseDouble(a[1]));
                                    loc.setLatitude(Double.parseDouble(a[2]));
                                    break;
                                }
                            }
                        }
                    }

                    if (!reason.equals("")){ // ?????? ?????? ??? ?????? ?????????
                        PopupActivity ac = new PopupActivity(getApplicationContext(), reason);
                        ac.show();
                    }

                    // ?????????????????? ????????? ????????????
                    if (System.currentTimeMillis() >= time_firebase +  1000) {
                        ArrayList<Double> now_loc = new ArrayList<>();
                        now_loc.add(loc.getLatitude());
                        now_loc.add(loc.getLongitude());
                        GPSsRef.child("GPS").setValue(now_loc);

                        GPSsRef.child("last_time").setValue(System.currentTimeMillis());

                        time_firebase = System.currentTimeMillis();
                    }

                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}