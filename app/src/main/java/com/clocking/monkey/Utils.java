package com.clocking.monkey;

public class Utils {
    static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "SQLiteDB";
    //Hora máxima de salida
    static final String HOUR_MAX = "22:00";

    //minutos mínimos entre entrada y salida para no poner comentario
    static final int MINUTES_MIN = 10;

    //Código para encontrar NFC
    static final String NFC_KEY = "M9Spr0aclI";

    static protected final String TAG = "TEST_BEACON_TAG";
    static protected final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    static protected final int REQUEST_ENABLE_BLUETOOTH = 1;
    static protected final long DEFAULT_SCAN_PERIOD_MS = 10000;
    static protected final String REGION_ID = "MyRegion";

    //Identificadores del ebeacon
    static protected final String NAMESPACE_ID = "0x00000000000000000000";
    static protected final String INSTANCE_ID = "0x000000000000";

}
