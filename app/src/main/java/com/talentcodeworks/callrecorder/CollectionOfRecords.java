package com.talentcodeworks.callrecorder;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.gms.common.api.GoogleApiClient;


/**
 * Created by Женя on 14.02.2016.
 * Класс возвращает информацию о статистике совершенного звонка:
 * getIMEINumber() - номер IMEI
 * getNetType() - тип сети передачи данных
 * getPhoneType() - тип сети радиосвязи
 * getLatitude() - широта
 * getLongitude() - долгота
 *
 */
public class CollectionOfRecords {

    private Context context;
    private TelephonyManager manager=(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    private GoogleApiClient mGoogleApiClient;



    public CollectionOfRecords(Context _context) {
        context = _context;
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
            break;
        case 1: return "Phone radio is GSM";
            break;
        case 2: return "Phone radio is CDMA";
            break;
        case 3: return "Phone is via SIP";
            break;
        }
    }

    public double getLatitude()
    {
        return 0;
    }

    public double getLongitude()
    {
        return 0;
    }


}
