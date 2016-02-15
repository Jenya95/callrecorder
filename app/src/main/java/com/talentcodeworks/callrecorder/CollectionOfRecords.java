package com.talentcodeworks.callrecorder;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;


/**
 * Created by Женя on 14.02.2016.
 * Класс возвращает информацию о статистике совершенного звонка:
 * getIMEINumber() - номер IMEI
 * getNetType() - тип сети передачи данных
 * getPhoneType() - тип сети радиосвязи
 * getLatitude() - широта
 * getLongitude() - долгота
 * setName(String, String) - запись имени файла
 */
public class CollectionOfRecords implements LocationListener {

    private Context context;
    private TelephonyManager manager=(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    protected LocationManager locationManager;
    public double latitude=0;
    public double longitude=0;
    public String nameOfFile = "";
    public String netType = "";
    public String IMEI = "";
    public String phoneType = "";


    public CollectionOfRecords(Context _context, String prefix, String suffix) {
        context = _context; //здесь при вызове конструкотра в коллекцию должна добавляться запись
                            //с необходимыми полями
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        latitude = getLatitude();
        longitude = getLongitude();
        nameOfFile = setName(prefix,suffix);
        netType = getNetType();
        IMEI = getIMEINumber();
        phoneType = getPhoneType();
    }

    public String getIMEINumber()
    {
        return manager.getDeviceId();
    }

    public String getNetType()
    {
        int i = manager.getNetworkType();

        switch (i) {
            case 7: return "1xRTT";
            case 4: return "CDMA";
            case 2: return "EDGE";
            case 1: return "GPRS";
            case 8: return "HSDPA";
            case 10: return "HSPA";
            case 15: return "HSPA+";
            case 13: return "LTE";
            case 3: return "UMTS";
            default: return "Unknown";
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
    }

    public String getPhoneType()
    {
        int i = manager.getPhoneType();
        switch (i) {
        case 0: return "No phone radio";
        case 1: return "Phone radio is GSM";
        case 2: return "Phone radio is CDMA";
        case 3: return "Phone is via SIP";
        default: return "Unknown";
        }
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public String setName(String a, String b)
    {
        return a+b;
    }
}