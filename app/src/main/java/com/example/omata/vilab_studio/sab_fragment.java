package com.example.omata.vilab_studio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static androidx.core.content.ContextCompat.getSystemService;


public class sab_fragment extends Fragment {
    public static sab_fragment newInstance(String packagename, ArrayList<String> permissionGroup, boolean non_playstore) {
        // Fragemnt01 インスタンス生成
        sab_fragment fragment = new sab_fragment();
        // Bundle にパラメータを設定
        Bundle barg = new Bundle();
        barg.putString("PackageName", packagename);
        barg.putStringArrayList("permissionGroup", permissionGroup);
        barg.putBoolean("non_playstore", non_playstore);
        fragment.setArguments(barg);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.sub_fragment,
                container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PackageManager pm = getActivity().getPackageManager();
        getActivity().getApplicationInfo();
        Bundle args = getArguments();

        if (args != null) {
            String packageName = args.getString("PackageName");//もらってきたパッケージ名
            ArrayList<String> permissionGroup = args.getStringArrayList("permissionGroup");
            boolean non_storeapp = args.getBoolean("non_playstore");
            TextView textView = view.findViewById(R.id.fragment_appnametext);
            TextView installtextView = view.findViewById(R.id.fragment_installtext);
            ImageView iconView = view.findViewById(R.id.fragment_icon);
            //日付の形式を日本語に
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy'年'MM'月'dd'日'E'曜日'");
            //このアプリをインストールしたインストーラー名を取得
            String installer = pm.getInstallerPackageName(packageName);
            System.out.println(installer);
            String installername = "";


            try {
                //アプリのアイコン取得
                Drawable icon = pm.getApplicationIcon(packageName);
                //パッケージ名を渡してアプリ名を取得
                String applicationLabel = pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString();
                //インストール名を日本語の表示名に変換(パッケージ名を渡してアプリ名（擬似的）を得る)
                if (installer != null)
                    installername = pm.getApplicationLabel(pm.getApplicationInfo(installer, PackageManager.GET_META_DATA)).toString();
                else installername = "取得できないところ";
                //インストール日時取得
                Date dateFirstInstallTime = new Date(pm.getPackageInfo(packageName, PackageManager.GET_META_DATA).firstInstallTime);
                installtextView.setText(sdf.format(dateFirstInstallTime) + "に\n" + installername + "からインストール");
                textView.setText(applicationLabel);
                iconView.setImageDrawable(icon);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            System.out.println("sub:" + packageName);


            //権限の色変更（権限を持っていないときは#fafafa、持っていてかつ無効（許可されていない）の場合は#bb86fc、許可されている場合は#ff4444）
            //野良アプリだった場合
            if (non_storeapp) {
                Button store = view.findViewById(R.id.fragment_non_playstore);
                store.setBackgroundColor(Color.parseColor("#ff4444"));
                store.setTextColor(Color.parseColor("#000000"));
            }
            //もし、パーミッショングループを持っている
            if (!permissionGroup.isEmpty()) {
                //そのアプリのパーミッショングループにそれが含まれていれば
                if (permissionGroup.contains("android.permission-group.CALENDAR")) {
                    //格子状に配置されている（ボタン）を取得（ボタンの方が画像とテキストを置きやすかったからボタン）
                    Button calender = view.findViewById(R.id.fragment_calender);
                    //もし、カレンダーの権限が実際に付与されていれば色を赤にする
                    //グループのやつならどれでもいいので代表して一個
                    if (pm.checkPermission("android.permission.READ_CALENDAR", packageName) == PackageManager.PERMISSION_GRANTED)
                        calender.setBackgroundColor(Color.parseColor("#ff4444"));
                        //許可されていなければ紫色にする
                    else calender.setBackgroundColor(Color.parseColor("#bb86fc"));
                    //そのテキストの色を濃くする
                    calender.setTextColor(Color.parseColor("#000000"));
                }
                //カメラの権限
                if (permissionGroup.contains("android.permission-group.CAMERA")) {
                    Button camera = view.findViewById(R.id.fragment_camera);
                    if (pm.checkPermission("android.permission.CAMERA", packageName) == PackageManager.PERMISSION_GRANTED)
                        camera.setBackgroundColor(Color.parseColor("#ff4444"));
                    else camera.setBackgroundColor(Color.parseColor("#bb86fc"));
                    camera.setTextColor(Color.parseColor("#000000"));
                }
                //連絡先の権限
                if (permissionGroup.contains("android.permission-group.CONTACTS")) {
                    Button contact = view.findViewById(R.id.fragment_contact);
                    if (pm.checkPermission("android.permission.READ_CONTACTS", packageName) == PackageManager.PERMISSION_GRANTED)
                        contact.setBackgroundColor(Color.parseColor("#ff4444"));
                    else contact.setBackgroundColor(Color.parseColor("#bb86fc"));
                    contact.setTextColor(Color.parseColor("#000000"));
                }
                //位置情報の権限
                if (permissionGroup.contains("android.permission-group.LOCATION")) {
                    Button place = view.findViewById(R.id.fragment_place);
                    if (pm.checkPermission("android.permission.ACCESS_FINE_LOCATIONS", packageName) == PackageManager.PERMISSION_GRANTED)
                        place.setBackgroundColor(Color.parseColor("#ff4444"));
                    else place.setBackgroundColor(Color.parseColor("#bb86fc"));
                    place.setTextColor(Color.parseColor("#000000"));
                }
                //マイクの権限
                if (permissionGroup.contains("android.permission-group.MICROPHONE")) {
                    Button mic = view.findViewById(R.id.fragment_mic);
                    if (pm.checkPermission("android.permission.RECORD_AUDIO", packageName) == PackageManager.PERMISSION_GRANTED)
                        mic.setBackgroundColor(Color.parseColor("#ff4444"));
                    else mic.setBackgroundColor(Color.parseColor("#bb86fc"));
                    mic.setTextColor(Color.parseColor("#000000"));
                }
                //電話機能の権限
                if (permissionGroup.contains("android.permission-group.PHONE")) {
                    Button phone = view.findViewById(R.id.fragment_phone);
                    if (pm.checkPermission("android.permission.CALL_PHONE", packageName) == PackageManager.PERMISSION_GRANTED)
                        phone.setBackgroundColor(Color.parseColor("#ff4444"));
                    else phone.setBackgroundColor(Color.parseColor("#bb86fc"));
                    phone.setTextColor(Color.parseColor("#000000"));
                }
                //SMS（Cメール）の権限
                if (permissionGroup.contains("android.permission-group.SMS")) {
                    Button sms = view.findViewById(R.id.fragment_sms);
                    if (pm.checkPermission("android.permission.READ_SMS", packageName) == PackageManager.PERMISSION_GRANTED)
                        sms.setBackgroundColor(Color.parseColor("#ff4444"));
                    else sms.setBackgroundColor(Color.parseColor("#bb86fc"));
                    sms.setTextColor(Color.parseColor("#000000"));
                }
                //ストレージに関する権限
                if (permissionGroup.contains("android.permission-group.STORAGE")) {
                    System.out.println(pm.checkPermission("Manifest.permission.STORAGE", packageName) == PackageManager.PERMISSION_GRANTED);
                    Button storage = view.findViewById(R.id.fragment_storage);
                    if (pm.checkPermission("android.permission.READ_EXTERNAL_STORAGE", packageName) == PackageManager.PERMISSION_GRANTED)
                        storage.setBackgroundColor(Color.parseColor("#ff4444"));
                    else storage.setBackgroundColor(Color.parseColor("#bb86fc"));
                    storage.setTextColor(Color.parseColor("#000000"));
                }
                //センサーに関する権限
                if (permissionGroup.contains("android.permission-group.SENSORS")) {
                    Button sensor = view.findViewById(R.id.fragment_sensor);
                    if (pm.checkPermission("android.permission.BODY_SENSORS", packageName) == PackageManager.PERMISSION_GRANTED)
                        sensor.setBackgroundColor(Color.parseColor("#ff4444"));
                    else sensor.setBackgroundColor(Color.parseColor("#bb86fc"));
                    sensor.setTextColor(Color.parseColor("#000000"));
                }
                //常時バックグラウンド許可に関する権限
                if (permissionGroup.contains("android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS")) {
                    //なんか省電力除外だけPowerManagerが情報を持っているらしい（権限というよりシステム寄りというか設定に近いから？）
                    PowerManager powerManager = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
                    Button background = view.findViewById(R.id.fragment_background);
                    //Android6.0以降の機能のため
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        System.out.println("back" + powerManager.isIgnoringBatteryOptimizations(packageName));
                        if (powerManager.isIgnoringBatteryOptimizations(packageName))
                            background.setBackgroundColor(Color.parseColor("#ff4444"));
                        else background.setBackgroundColor(Color.parseColor("#bb86fc"));
                    }else{
                        background.setText("OS対象外！");
                    }
                    background.setTextColor(Color.parseColor("#000000"));
                }

            }
        }

    }


}
