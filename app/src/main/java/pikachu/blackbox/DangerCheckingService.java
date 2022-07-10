package pikachu.blackbox;

import static pikachu.blackbox.ExitActivity.GPSsRef;
import static pikachu.blackbox.ExitActivity.locationManager;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import java.util.function.Function;

public class DangerCheckingService extends Service {

    Handler handler = new Handler();

    boolean isRun = false;

    SensorManager SensorManager;

    Sensor GgyroSensor = null;

    SensorEventListener SListen;

    float x, y, z;

    Thread thread = new Thread(() -> {

        long time_firebase = System.currentTimeMillis();

        long time_location = System.currentTimeMillis();

        Location last_loc = null;

        try {
            while (isRun) {


                @SuppressLint("MissingPermission") Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                loc.getLatitude();

                loc.getLongitude();

                Function<Location, Double> distance_f = (location) -> {

                    Double x = location.getLatitude();
                    Double y = location.getLongitude();

                    Double x_1 = ExitActivity.start_lat.getLatitude();
                    Double y_1 = ExitActivity.start_lat.getLongitude();

                    Double x_2 = ExitActivity.final_lat.latitude;
                    Double y_2 = ExitActivity.final_lat.longitude;

                    return Math.abs((x_2 - x_1) * y - (y_2 - y_1) * (x - x_1) - y_1 * (x_2 - x_1)) / Math.pow(Math.pow(x_2 - x_1, 2) + Math.pow(y_2 - y_1,2), 1/2);
                };

                String reason = "";

                //경로 이탈 및 가만히 있는 가를 확인

                if(distance_f.apply(loc) >= 0.01){ // 경로 이탈 확인
                    reason = "지정된 경로와 너무 멉니다.";
                }else{ // 움직임 미감지 확인
                    if(last_loc != null){
                        Double distance = Math.pow(Math.pow(loc.getLatitude() - last_loc.getLatitude(), 2) + Math.pow(loc.getLongitude() - last_loc.getLongitude(),2), 1/2);

                        last_loc = loc;

                        if(System.currentTimeMillis() - time_location >= 60 * 1000){
                            if(distance >= 0.001){
                                reason = "동작 감지 불가";
                            }
                            time_location = System.currentTimeMillis();
                        }
                    }else{
                        last_loc = loc;
                    }
                }


                //TODO 자이로센서 확인


                if (!reason.equals("")){ // 위험 확인 시 팝업 띄우기
                    Bundle bun = new Bundle();
                    bun.putString("reason", reason);

                    Intent intent = new Intent(getApplicationContext(), PopupActivity.class);

                    intent.putExtras(bun);
                    PendingIntent pie = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

                    try {
                        pie.send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }

                // 파이어베이스 데이터 업데이트
                if (System.currentTimeMillis() >= time_firebase + 60 * 1000) {
                    GPSsRef.child("GPS").setValue(new Double[]{loc.getLatitude(), loc.getLongitude()});

                    GPSsRef.child("last_time").setValue(System.currentTimeMillis());

                    time_firebase = System.currentTimeMillis();
                }

                Thread.sleep(30 * 1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    });

    @Override
    public void onCreate() {
        super.onCreate();
        SensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Using the Accelometer
        GgyroSensor = SensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        SListen = new GyroscopeListener();

        SensorManager.registerListener(SListen, GgyroSensor, android.hardware.SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null){
            return START_STICKY;
        }
        isRun = true;

        thread.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        isRun = false;

        try {
            thread.join();
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
}