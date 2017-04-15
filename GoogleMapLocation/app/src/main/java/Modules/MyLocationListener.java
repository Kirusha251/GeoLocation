package Modules;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import java.sql.Time;

/**
 * Created by alina on 16.12.2016.
 */

public class MyLocationListener implements LocationListener {

    public void onLocationChanged(Location location) {
        //вызывается когда локация изменилась
        Log.i("MyTag", "onLocationChanged - широта: " + location.getLatitude() + " | долгота: " + location.getLongitude() + " | speed: " + location.getSpeed()
                + " | provider: " + location.getProvider() + " | time: " + new Time(location.getTime()).getSeconds() + "s");
    }

    public void onProviderDisabled(String provider) {
        //вызывается когда провайдер отключается от пользователя
        Log.i("MyTag", "Provider disabled. GPS is off");
    }

    public void onProviderEnabled(String provider) {
        //вызывается когда провайдер включается
        Log.i("MyTag", "Provider enabled. GPS is on");
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        //вызывается при изменении статуса провайдера
        Log.i("MyTag", "Provider " + provider + " status " + status + " changed");
    }

}