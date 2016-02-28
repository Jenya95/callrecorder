package com.talentcodeworks.callrecorder;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;


/**
 * Created by Женя on 14.02.2016.
 * Класс содержит поля:
 * IMEI - номер IMEI
 * netType - тип сети передачи данных
 * phoneType - тип сети радиосвязи
 * latitude - широта
 * longitude - долгота
 * cellId - ID базовой станции
 * setName(String, String) - запись имени файла
 * myPhoneNumber - номер устройства, на котором запущено приложжение
 * externalPhoneNumber - номер устройства, с которым был разговор
 */
public class CollectionOfRecords {

    private Context context;
    private TelephonyManager manager;
    public double latitude=0;
    public double longitude=0;
    public String nameOfFile = "";
    public String netType = "";
    public String IMEI = "";
    public String phoneType = "";
    public String myPhoneNumber = "";
    public String externalPhoneNumber = "";
    public int cellId = 0;
    public int signalStrength  = 0;


    public CollectionOfRecords(Context _context, String prefix) {
        context = _context;
        manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        fillFields(prefix);
    }

    public void fillFields(String s)
    {
        _getLocation();
        nameOfFile = setName(s);
        netType = getNetType();
        IMEI = getIMEINumber();
        phoneType = getPhoneType();
        cellId = getCellId();
        signalStrength = getSignalStrengthInDbm();
        myPhoneNumber = getPhoneNumber();
        externalPhoneNumber = getExternalPhoneNumber();
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
        return signalStrength;
    }

    private void _getLocation() {
        try {
            latitude=MyLocationListener.imHere.getLatitude();
            longitude=MyLocationListener.imHere.getLongitude();
        } catch (NullPointerException e) {
            latitude = -1.0;
            longitude = -1.0;
        }
    }


    public String setName(String a)
    {
        return a;
    }

    public String getPhoneNumber()
    {
        String mPhoneNumber = manager.getLine1Number();
        return mPhoneNumber;
    }

    public String getExternalPhoneNumber()
    {
        String phNumber="";
        try {
            Uri contacts = CallLog.Calls.CONTENT_URI;
            Cursor managedCursor = context.getContentResolver().query(
                    contacts, null, null, null, null);
            int number = managedCursor.getColumnIndex( CallLog.Calls.NUMBER );
            if( managedCursor.moveToFirst()) {
                phNumber = managedCursor.getString( number );
            }
            managedCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return phNumber;
    }

    }
