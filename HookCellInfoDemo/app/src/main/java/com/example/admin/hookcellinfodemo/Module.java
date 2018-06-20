package com.example.admin.hookcellinfodemo;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Module implements IXposedHookLoadPackage {

    private static final String TAG = "CellModule";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if(loadPackageParam.packageName.equals("com.tencent.mm") || loadPackageParam.packageName.equals("com.example.admin.basestationinfodemo")
                ||loadPackageParam.packageName.equals("com.baidu.BaiduMap")){

            // 修改基站信息
            XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", loadPackageParam.classLoader, "getCellLocation", new getCellLocationMethodHook());

            // 修改 WiFi.getScanResults, 否则 wifi 热点暴露真实位置，返回空list，模拟的就是关闭wifi的情况
            XposedHelpers.findAndHookMethod("android.net.wifi.WifiManager", loadPackageParam.classLoader, "getScanResults", new getScanResultsMethodHook() );

            // 联通4g ： networkInfo -- [type: MOBILE[LTE], state: CONNECTED/CONNECTED, reason: connected, extra: 3gnet, roaming: false, failover: false, isAvailable: true]
            // 移动4g ： networkInfo -- [type: MOBILE[LTE], state: CONNECTED/CONNECTED, reason: connected, extra: cmnet, roaming: false, failover: false, isAvailable: true]
            // 单纯的hook这个不行，因为生成NetworkInfo的函数不止一个（比如 getAllNetworkInfo ），可能会被绕过
            // XposedHelpers.findAndHookMethod("android.net.ConnectivityManager", loadPackageParam.classLoader, "getActiveNetworkInfo",new getActiveNetworkInfoMethodHook());


            // 直接hook NerworkInfo的成员变量的get方法修改成员变量值(只需修改6个，其他的wifi和4g的值是一样的)
            XposedHelpers.findAndHookMethod("android.net.NetworkInfo",loadPackageParam.classLoader,"getTypeName",new getTypeNameMethodHook());

            // 修改当前网络类型，0为TYPE_MOBILE ， 1 为TYPE_WIFI（注意要指定包名hook，不要全局hook，否则会没有网络。此时是有wifi的标志的，但是获取网络的类型是数据）
            XposedHelpers.findAndHookMethod("android.net.NetworkInfo", loadPackageParam.classLoader, "getType",new getTypeMethodHook());

            XposedHelpers.findAndHookMethod("android.net.NetworkInfo",loadPackageParam.classLoader,"getSubtype",new getSubtypeMethodHook());

            XposedHelpers.findAndHookMethod("android.net.NetworkInfo",loadPackageParam.classLoader,"getSubtypeName",new getSubtypeNameMethodHook());

            XposedHelpers.findAndHookMethod("android.net.NetworkInfo",loadPackageParam.classLoader,"getReason",new getReasonMethodHook());

            XposedHelpers.findAndHookMethod("android.net.NetworkInfo",loadPackageParam.classLoader,"getExtraInfo",new getExtraInfoMethodHook());

            // telephonyManager.getNetworkType())
            XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", loadPackageParam.classLoader, "getNetworkType", new getNetworkTypeMethodHook());


        }

    }

    private class getCellLocationMethodHook extends XC_MethodHook{
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            Log.i(TAG, "getCellLocation  beforeHookedMethod: ");
            GsmCellLocation gsmCellLocation = new GsmCellLocation();
            // TODO 替换基站值
            gsmCellLocation.setLacAndCid(10016,87066502);
            param.setResult(gsmCellLocation);
        }
    }

    private class getScanResultsMethodHook extends XC_MethodHook{
        @Override
        protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
            Log.i(TAG, "getScanResults  beforeHookedMethod: ");
            List<ScanResult> list = new ArrayList<>();
            param.setResult(list);
        }
    }

    private class getActiveNetworkInfoMethodHook extends XC_MethodHook{
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
            Log.i(TAG, "getActiveNetworkInfo  beforeHookedMethod: ");
            //    getNetType: networkInfo.type -- 0
            //    getNetType: networkInfo.subtype -- 13
            //    getNetType: networkInfo.typeName -- MOBILE
            //    getNetType: networkInfo.subtypeName -- LTE


//                    ConnectivityManager connectivityManager = (ConnectivityManager)
//                            mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
//                    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
//
//                    Class<NetworkInfo> clazz = NetworkInfo.class;
////                    NetworkInfo networkInfo = clazz.newInstance();
//
//                    Field typeField = clazz.getDeclaredField("mNetworkType");
//                    typeField.setAccessible(true);
//                    typeField.setInt(networkInfo,0);
//                    typeField.setAccessible(false);
//
//                    Field subtypeField = clazz.getDeclaredField("mSubtype");
//                    subtypeField.setAccessible(true);
//                    subtypeField.setInt(networkInfo,13);
//                    subtypeField.setAccessible(false);
//
//                    Field typeNameField = clazz.getDeclaredField("mTypeName");
//                    typeNameField.setAccessible(true);
//                    typeNameField.set(networkInfo,"MOBILE");
//                    typeNameField.setAccessible(false);
//
//                    Field subtypeNameField = clazz.getDeclaredField("mSubtypeName");
//                    subtypeNameField.setAccessible(true);
//                    subtypeNameField.set(networkInfo,"LTE");
//                    subtypeNameField.setAccessible(false);
//
//                    // 枚举类型
//                    Field stateField = clazz.getDeclaredField("mState");
//                    stateField.setAccessible(true);
//                    stateField.set(networkInfo, NetworkInfo.State.CONNECTED);
//                    stateField.setAccessible(false);
//
//                    Field detailedStateField = clazz.getDeclaredField("mDetailedState");
//                    detailedStateField.setAccessible(true);
//                    detailedStateField.set(networkInfo, NetworkInfo.State.CONNECTED);
//                    detailedStateField.setAccessible(false);
//
//                    Field reasonField = clazz.getDeclaredField("mReason");
//                    reasonField.setAccessible(true);
//                    reasonField.set(networkInfo,"connected");
//                    reasonField.setAccessible(false);
//
//
//                    Field extraInfoField = clazz.getDeclaredField("mExtraInfo");
//                    extraInfoField.setAccessible(true);
//                    extraInfoField.set(networkInfo,"3gnet");
//                    extraInfoField.setAccessible(false);
//
//                    Field roamingField = clazz.getDeclaredField("mIsRoaming");
//                    roamingField.setAccessible(true);
//                    roamingField.setBoolean(networkInfo,false);
//                    roamingField.setAccessible(false);
//
//                    Field failoverField = clazz.getDeclaredField("mIsFailover");
//                    failoverField.setAccessible(true);
//                    failoverField.setBoolean(networkInfo,false);
//                    failoverField.setAccessible(false);
//
//                    Field availableField = clazz.getDeclaredField("mIsAvailable");
//                    availableField.setAccessible(true);
//                    availableField.setBoolean(networkInfo,true);
//                    availableField.setAccessible(false);

//                    NetworkInfo networkInfo = new NetworkInfo();
//                    NetworkInfo networkInfo = new NetworkInfo(ConnectivityManager.TYPE_MOBILE, 13, "MOBILE", "LTE");

            Parcelable.Creator<NetworkInfo> creator =  NetworkInfo.CREATOR;
            Parcel parcel = Parcel.obtain();
            parcel.writeInt(0);                      // mNetworkType
            parcel.writeInt(13);                      // mSubtype
            parcel.writeString("MOBILE");           // mTypeName
            parcel.writeString("LTE");              // mSubtypeName
            parcel.writeString("CONNECTED" );       // mState.name()
            parcel.writeString("CONNECTED");       // mDetailedState.name()
            parcel.writeInt(0);                     // mIsFailover(int)
            parcel.writeInt(1);                     // mIsAvailable
            parcel.writeInt(0);                     // mIsRoaming
            parcel.writeString("connected");       // mReason
            parcel.writeString("cmnet");           // mExtraInfo
//                    dest.writeInt(mNetworkType);
//                    dest.writeInt(mSubtype);
//                    dest.writeString(mTypeName);
//                    dest.writeString(mSubtypeName);
//                    dest.writeString(mState.name());
//                    dest.writeString(mDetailedState.name());
//                    dest.writeInt(mIsFailover ? 1 : 0);
//                    dest.writeInt(mIsAvailable ? 1 : 0);
//                    dest.writeInt(mIsRoaming ? 1 : 0);
//                    dest.writeString(mReason);
//                    dest.writeString(mExtraInfo);
            parcel.setDataPosition(0);

//            int netType = in.readInt();
//            int subtype = in.readInt();
//            String typeName = in.readString();
//            String subtypeName = in.readString();
//            NetworkInfo netInfo = new NetworkInfo(netType, subtype, typeName, subtypeName);
//            netInfo.mState = NetworkInfo.State.valueOf(in.readString());
//            netInfo.mDetailedState = NetworkInfo.DetailedState.valueOf(in.readString());
//            netInfo.mIsFailover = in.readInt() != 0;
//            netInfo.mIsAvailable = in.readInt() != 0;
//            netInfo.mIsRoaming = in.readInt() != 0;
//            netInfo.mReason = in.readString();
//            netInfo.mExtraInfo = in.readString();

            NetworkInfo networkInfo = creator.createFromParcel(parcel);
            // 这里很奇怪的是mReason和mExtraInfo没有读取到，都为null
            Log.i(TAG, "beforeHookedMethod:  networkInfo -- "+networkInfo);
            Log.i(TAG, "beforeHookedMethod:  networkInfo.getTypeName() -- "+networkInfo.getTypeName());
            Log.i(TAG, "beforeHookedMethod:  networkInfo.isRoaming() -- "+networkInfo.isRoaming());
            Log.i(TAG, "beforeHookedMethod:  networkInfo.isAvailable() -- "+networkInfo.isAvailable());
            Log.i(TAG, "beforeHookedMethod:  networkInfo.isFailover() -- "+networkInfo.isFailover());
            Log.i(TAG, "beforeHookedMethod:  networkInfo.getReason() -- "+networkInfo.getReason());
            Log.i(TAG, "beforeHookedMethod:  networkInfo.getExtraInfo() -- "+networkInfo.getExtraInfo());
            param.setResult(networkInfo);
        }
    }


    private class getTypeMethodHook extends XC_MethodHook{
        @Override
        protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
            Log.i(TAG, "getType  beforeHookedMethod: ");
            param.setResult(ConnectivityManager.TYPE_MOBILE);
        }
    }

    private class getNetworkTypeMethodHook extends XC_MethodHook{
        @Override
        protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
            Log.i(TAG, "getNetworkType  beforeHookedMethod: ");
            param.setResult(13);
        }
    }


    private class getTypeNameMethodHook extends XC_MethodHook {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            Log.i(TAG, "getTypeName beforeHookedMethod: ");
            param.setResult("MOBILE");
        }
    }

    private class getSubtypeMethodHook extends XC_MethodHook {
        @Override
        protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
            Log.i(TAG, "getSubtype beforeHookedMethod: ");
            param.setResult(13);
        }
    }


    private class getSubtypeNameMethodHook extends XC_MethodHook {
        @Override
        protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
            Log.i(TAG, "getSubtypeName beforeHookedMethod: ");
            param.setResult("LTE");
        }
    }

    private class getReasonMethodHook extends XC_MethodHook {
        @Override
        protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
            Log.i(TAG, "getReason beforeHookedMethod: ");
            param.setResult("connected");
        }
    }

    private class getExtraInfoMethodHook extends XC_MethodHook {
        @Override
        protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
            Log.i(TAG, "getExtraInfo beforeHookedMethod: ");
            param.setResult("cmnet");
        }
    }
}
