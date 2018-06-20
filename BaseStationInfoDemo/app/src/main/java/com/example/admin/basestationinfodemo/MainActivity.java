package com.example.admin.basestationinfodemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellInfo;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BaseStationInfo";

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getBaseStationInfo();

        getNetType();

    }

    private void getNetType() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            int type = networkInfo.getType();

//            int type, int subtype, String typeName, String subtypeName
            Log.i(TAG, "getNetType: networkInfo.type -- "+networkInfo.getType());
            Log.i(TAG, "getNetType: networkInfo.subtype -- "+networkInfo.getSubtype());
            Log.i(TAG, "getNetType: networkInfo.typeName -- "+networkInfo.getTypeName());
            Log.i(TAG, "getNetType: networkInfo.subtypeName -- "+networkInfo.getSubtypeName());
            Log.i(TAG, "getNetType: networkInfo.reason -- "+networkInfo.getReason());
            Log.i(TAG, "getNetType: networkInfo.extraInfo -- "+networkInfo.getExtraInfo());

            if (type == ConnectivityManager.TYPE_WIFI) {
                Log.i(TAG, "getNetType: wifi类型");
            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                Log.i(TAG, "getNetType:数据类型");
            }
        }else{
            if(networkInfo == null){
                Log.i(TAG, "getNetType: networkInfo为null");
            }
            if(networkInfo != null&&!networkInfo.isConnected()){
                Log.i(TAG, "getNetType: networkInfo没有网络连接");
            }

        }
    }


    /** 
      * 功能描述：通过手机信号获取基站信息 
      * # 通过TelephonyManager 获取lac:mcc:mnc:cell-id 
      * # MCC，Mobile Country Code，移动国家代码（中国的为460）； 
      * # MNC，Mobile Network Code，移动网络号码（中国移动为00，中国联通为01，中国电信为02）；  
      * # LAC，Location Area Code，位置区域码； 
      * # CID，Cell Identity，基站编号； 
      * # BSSS，Base station signal strength，基站信号强度。 
      * 
      */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void getBaseStationInfo() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {//这里可以写个对话框之类的项向用户解释为什么要申请权限，并在对话框的确认键后续再次申请权限
            } else {
                //申请权限，字符串数组内是一个或多个要申请的权限，1是申请权限结果的返回参数，在onRequestPermissionsResult可以得知申请结果
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,}, 1);
            }
        }
        // 获取网络类型  
        int type = telephonyManager.getNetworkType();
        Log.i(TAG, "getBaseStationInfo: getNetworkType -- "+type);

        //46000 中国移动（GSM） 46001 中国联通（GSM） 46002 中国移动（TD-S） 46003 中国电信（CDMA） 46004 空（似乎是专门用来做测试的） 46005 中国电信（CDMA） 46006 中国联通（WCDMA） 46007 中国移动（TD-S）46008 46009 46010 46011 中国电信（FDD-LTE）
        String  networkOperator = telephonyManager.getNetworkOperator();
        Log.i(TAG, "getBaseStationInfo: getNetworkOperator -- " + networkOperator);
//        int mcc = Integer.parseInt(networkOperator.substring(0,3));
//        int mnc = Integer.parseInt(networkOperator.substring(3,5));
//        Log.i(TAG, "getBaseStationInfo: mcc -- " + mcc );
//        Log.i(TAG, "getBaseStationInfo: mnc -- " + mnc );

        // 获取所有的基站信息,这个方法是代替getCellLocation的。但是在久的手机上，可能会返回null
        List<CellInfo> cellList = telephonyManager.getAllCellInfo();
        Log.i(TAG, "getBaseStationInfo: getAllCellInfo -- " + cellList);
        if(cellList!=null && cellList.size()>0){
            for (CellInfo info :cellList ){
                Log.i(TAG, "getBaseStationInfo: info -- "+info);
            }
        }


        // 中国移动和中国联通获取LAC、CID的方式 
        GsmCellLocation gsmCellLocation = (GsmCellLocation) telephonyManager.getCellLocation();
        int lac = gsmCellLocation.getLac();

        int cid = gsmCellLocation.getCid();
        Log.i(TAG, "getBaseStationInfo: GsmCellLocation -- " + gsmCellLocation);
        Log.i(TAG, "getBaseStationInfo: getCellLocation  lac -- " + lac);
        Log.i(TAG, "getBaseStationInfo: getCellLocation  cid -- " + cid);

//        // 中国电信获取LAC,CID的方式
//        CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) telephonyManager.getCellLocation();
//        int dianxinlac = cdmaCellLocation.getNetworkId();
//        int dianxincid = cdmaCellLocation.getBaseStationId();
//
//        Log.i(TAG, "getBaseStationInfo: CdmaCellLocation -- " + cdmaCellLocation);
//        Log.i(TAG, "getBaseStationInfo: getCellLocation  dianxinlac -- " + dianxinlac);
//        Log.i(TAG, "getBaseStationInfo: getCellLocation  dianxincid -- " + dianxincid);

        // 获取邻区基站信息  
        List<NeighboringCellInfo> infoList = telephonyManager.getNeighboringCellInfo();
        Log.i(TAG, "getBaseStationInfo: getNeighboringCellInfo -- " + infoList);
        StringBuffer sb  = new StringBuffer("总数： "+infoList.size()+"\n");
        for (NeighboringCellInfo info : infoList){
            sb.append(" LAC : "+info.getLac());
            sb.append(" CID : "+info.getCid());
            sb.append(" BSSS : "+info.getRssi() +"\n");
        }
        Log.i(TAG, "getBaseStationInfo: infoList -- "+sb);


        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        Log.i(TAG, "getBaseStationInfo: getScanResults -- "+wifiManager.getScanResults());

    }
}
