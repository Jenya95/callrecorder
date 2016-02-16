package com.talentcodeworks.callrecorder;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
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
    private TelephonyManager manager;
    protected LocationManager locationManager;
    public double latitude=0;
    public double longitude=0;
    public String nameOfFile = "";
    public String netType = "";
    public String IMEI = "";
    public String phoneType = "";
    public int cellId = 0;
    public int signalStrength = 0;


    public CollectionOfRecords(Context _context, String prefix) {
        context = _context;
        manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        fillFields(prefix);
    }

    public void fillFields(String s)
    {

        latitude = getLatitude();
        longitude = getLongitude();
        nameOfFile = setName(s);
        netType = getNetType();
        IMEI = getIMEINumber();
        phoneType = getPhoneType();
        cellId = getCellId();
        signalStrength = getSignalStrengthInDbm();
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

    public int getCellId() {
        GsmCellLocation cl = (GsmCellLocation) manager.getCellLocation();
        int cellId = cl.getCid();
        return cellId;
    }

    public int getSignalStrengthInDbm() { //TODO допилить силу сигнала

       /* CellInfoGsm cellinfogsm = (CellInfoGsm)manager.getAllCellInfo().get(0);
        CellSignalStrengthGsm cellSignalStrengthGsm = cellinfogsm.getCellSignalStrength();
        int signalStrenght = cellSignalStrengthGsm.getDbm();
        */
        return 0;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public String setName(String a)
    {
        return a;
    }
}
