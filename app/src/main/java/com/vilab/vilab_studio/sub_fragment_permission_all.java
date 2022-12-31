package com.vilab.vilab_studio;

import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class sub_fragment_permission_all extends Fragment {
    public static sub_fragment_permission_all newInstance(String packagename, ArrayList<String> request) {
        // Fragemnt01 インスタンス生成
        sub_fragment_permission_all fragment = new sub_fragment_permission_all();
        // Bundle にパラメータを設定
        Bundle barg = new Bundle();
        barg.putString("PackageName", packagename);
        barg.putStringArrayList("request_permissions", request);
        fragment.setArguments(barg);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.sub_fragment_permissions,
                container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PackageManager pm = getActivity().getPackageManager();
        PermissionInfo permissionInfo = new PermissionInfo();
        ArrayList<String> request_permissions = new ArrayList<>();
        Bundle args = getArguments();

        if (args != null) {
            String packageName = args.getString("PackageName");
            request_permissions = args.getStringArrayList("request_permissions");
            List<Permissions> dataList = new ArrayList<Permissions>();
            ArrayList<Map<String, Object>> listData = new ArrayList<>();
            //受け取った要求権限の数回る
            for (String s : request_permissions) {
                try {
                    permissionInfo = pm.getPermissionInfo(s, 0);
                    Permissions permissions = new Permissions();
                    permissions.permissions_name = permissionInfo.loadLabel(pm);//日本語の権限名
                    permissions.permission_GRANTED = "この権限は" + permission_GRANTED(s, packageName);//許可されているかどうかの文章
                    permissions.permission_detail = permissionInfo.loadDescription(pm);//格納されている説明文
                    if (!(permissionInfo.group ==null) &&permissionInfo.group.equals("android.permission-group.UNDEFINED"))
                        permissions.grorp = MainActivity.perm2group(s.substring(s.lastIndexOf(".") + 1));
                    else permissions.grorp=permissionInfo.group;
                    //アイコンを設定
                    if (permissions.grorp != null)
                        permissions.icon = drawable_permissionicon(permissions.grorp);
                    else permissions.icon = drawable_permissionicon("null");
                    //PermissionsクラスのデータとしてdataListに追加
                    dataList.add(permissions);
                } catch (PackageManager.NameNotFoundException e) {
                    //e.printStackTrace();
                }
            }
            //中身をパーミッショングループに属しているあいうえお順に並べ替える
            sortdata(dataList);
            //中身を並べ替えたのでListViewに登録していく
            for (int i = 0; i < dataList.size(); i++) {
                Map<String, Object> item = new HashMap<>();
                item.put("permissionname", dataList.get(i).permissions_name);
                item.put("permission_granted", dataList.get(i).permission_GRANTED);
                item.put("permission_detail", dataList.get(i).permission_detail);
                item.put("permissionGrurp", dataList.get(i).grorp);
                item.put("icon", dataList.get(i).icon);
                listData.add(item);
            }
            ListView list = view.findViewById(R.id.fragment2_permission_list);
            list.setAdapter(new SimpleAdapter(
                    getContext(),
                    listData,
                    R.layout.permission_list,
                    new String[]{"permissionname", "permission_granted", "permission_detail", "icon"},
                    new int[]{R.id.name, R.id.permission_GRANTED, R.id.detail, R.id.Premission_image}
            ));
        }

    }

    //許可されているかどうかのメソッド（権限名とパッケージ名から）
    public String permission_GRANTED(String permname, String packagename) {
        if (getActivity().getPackageManager().checkPermission(permname, packagename) == PackageManager.PERMISSION_GRANTED) {
            return "許可されています。";
        } else {
            return "許可されていません。";
        }
    }

    //もらった権限名から画像を割り当てる（resのdrawable内にあるデータ）
    public int drawable_permissionicon(String permission) {
        switch (permission) {
            case "android.permission-group.ACTIVITY_RECOGNITION":
                return R.drawable.fragmentperm_activity;
            case "android.permission-group.CALENDAR":
                return R.drawable.fragmentperm_calender;
            case "android.permission-group.CALL_LOG":
                return R.drawable.fragmentperm_calllog;
            case "android.permission-group.CAMERA":
                return R.drawable.fragmentperm_camera;
            case "android.permission-group.CONTACTS":
                return R.drawable.fragmentperm_contacts;
            case "android.permission-group.LOCATION":
                return R.drawable.fragmentperm_place;
            case "android.permission-group.MICROPHONE":
                return R.drawable.fragmentperm_mic;
            case "android.permission-group.NEARBY_DEVICES"://API31
                return R.drawable.fragmentperm_bluetooth;
            case "android.permission-group.NOTIFICATIONS":
                return R.drawable.fragmentperm_notifications_24;
            case "android.permission-group.PHONE":
                return R.drawable.fragmentperm_phone;
            case "android.permission-group.READ_MEDIA_AURAL"://API33
                return R.drawable.fragmentperm_audio_file_24;
            case "android.permission-group.READ_MEDIA_VISUAL"://API33
                return R.drawable.fragmentperm_media_24;
            case "android.permission-group.SENSORS":
                return R.drawable.fragmentperm_activity;
            case "android.permission-group.SMS":
                return R.drawable.fragmentperm_sms;
            case "android.permission-group.STORAGE":
                return R.drawable.fragmentperm_storage;
            case "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS":
                return R.drawable.fragmentperm_power_settings_new_24;
            default:
                return R.drawable.fragmentperm_device_information;
        }
    }


    // アプリケーションデータ格納クラス
    public static class Permissions {
        CharSequence permissions_name;
        CharSequence permission_detail;
        CharSequence permission_GRANTED;
        String grorp;
        int icon;

    }

    //ソートしたい
    List<Permissions> sortdata(List<Permissions> appData) {
        Collections.sort(appData, new Comparator<Permissions>() {
            @Override
            public int compare(Permissions p1, Permissions p2) {
                //p1のグループ名がnullでp2はグループ名がある場合はp2を上に行かせたいので1にする
                if (p1.grorp == null && p2.grorp != null) return 1;
                //p1のグループ名があってp2のグループ名がない場合はp2を下に行かせたいので-1にする
                if (p1.grorp != null && p2.grorp == null) return -1;
                //もうない場合はそれでいい（0を出力）
                if (p1 == null && p2 == null || p1.grorp == null || p2.grorp == null) return 0;
                return p1.grorp.compareToIgnoreCase(p2.grorp);
            }
        });
        return appData;
    }

}
