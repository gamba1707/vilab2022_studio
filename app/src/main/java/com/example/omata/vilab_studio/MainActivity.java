package com.example.omata.vilab_studio;

import static android.content.pm.PermissionInfo.PROTECTION_DANGEROUS;

import static java.text.Normalizer.normalize;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Serializable {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //main.xmlの内容を読み込む
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setContentView(R.layout.activity);
        // 端末にインストール済のアプリケーション一覧情報を取得
        List<AppData> dataList = new ArrayList<AppData>();
        final PackageManager pm = getPackageManager();
        final int flags = PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_DISABLED_COMPONENTS;
        final List<ApplicationInfo> installedAppList = pm.getInstalledApplications(0);
        //とりあえず仮置きで権限を入れて表示（100個も権限ないだろみたいな）
        String[] requestedPermissions = new String[100];


        // リストに一覧データを格納する
        for (ApplicationInfo app : installedAppList) {
            if (!((app.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM)) {
                MainActivity.AppData data = new MainActivity.AppData();
                data.label = app.loadLabel(pm).toString();
                data.icon = app.loadIcon(pm);
                data.request_permission = new ArrayList<String>();
                data.permissionGroup = new ArrayList<String>();
                data.permission = "";
                try {
                    requestedPermissions = pm.getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS).requestedPermissions;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                //ここでログにアプリ名と持っている権限を表示している
                if (requestedPermissions != null) {
                    System.out.println("アプリ名：" + data.label);
                    PermissionInfo permissionInfo = new PermissionInfo();
                    for (String s : requestedPermissions) {
                        data.request_permission.add(s);//一個ずつ配列からListに入れていく

                        try {
                            permissionInfo = pm.getPermissionInfo(s, 0);
                        } catch (PackageManager.NameNotFoundException e) {

                        }

                        //System.out.println("groupname:" + permissionInfo.group +"   level:"+permissionInfo.protectionLevel+ "  permname:" + s+"    権限許可状況："+((pm.checkPermission(s,app.packageName) == PackageManager.PERMISSION_GRANTED)?true:false));

                        //権限がDangerousであるものは表示
                        //pm.checkPermission(権限名,パッケージ名)でユーザーが許可してるか取得
                        //それでもし0が出るならTrue、-1ならfalseを出力させる（三項演算子を使って）
                        if (Build.VERSION.SDK_INT >= 28) {//API28(Android9)以降用
                            if (permissionInfo.getProtection() == PROTECTION_DANGEROUS) {
                                System.out.println("groupname:" + permissionInfo.group + "  permname:" + s + "    権限許可状況：" + ((pm.checkPermission(s, app.packageName) == PackageManager.PERMISSION_GRANTED) ? true : false));
                                //もし新しいグループだった場合は登録する
                                if (!data.permissionGroup.contains(permissionInfo.group))
                                    data.permissionGroup.add(permissionInfo.group);
                            }
                        } else {//それ以下のバージョン用
                            if (permissionInfo.protectionLevel == 4097 || permissionInfo.protectionLevel == 1) {
                                System.out.println("groupname:" + permissionInfo.group + "  permname:" + s + "    権限許可状況：" + ((pm.checkPermission(s, app.packageName) == PackageManager.PERMISSION_GRANTED) ? true : false));
                                //もし新しいグループだった場合は登録する
                                if (!data.permissionGroup.contains(permissionInfo.group))
                                    data.permissionGroup.add(permissionInfo.group);
                            }
                        }
                    }

                }

                //グループの数繰り返して登録して面積分にする
                for (int i = 0; i < data.permissionGroup.size(); i++) {
                    dataList.add(setpermission(i, data));//dataを使いまわして登録するとそれぞれのアプリに権限設定ができないため
                }
                //0の場合も一応。。。
                if (data.permissionGroup.size() <= 0) {
                    dataList.add(data);
                }
                System.out.println("size:" + data.permissionGroup.size());
                System.out.println(data.permissionGroup.toString());
                System.out.println(data.permission);
            }
        }
        sortdata(dataList);


        // リストビューにアプリケーションの一覧を表示する
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 4));
        recyclerView.setAdapter(new RecyclerAdapter(getApplicationContext(), dataList));
    }

    //ここで渡されたパーミッショングループの数、データを水増しする。（中で書くとdataが一つしか生成できなくてこうするしかなかった）
    public AppData setpermission(int i, MainActivity.AppData data) {
        if (i == 0) {//0個目だけ元々作ってあるしもったいないかなって
            data.permission = data.permissionGroup.get(i);
            return data;
        } else {
            MainActivity.AppData d2 = new MainActivity.AppData();
            d2.label = data.label;
            d2.icon = data.icon;
            d2.request_permission = data.request_permission;
            d2.permissionGroup = data.permissionGroup;
            d2.permission = data.permissionGroup.get(i);
            return d2;
        }
    }


    // アプリケーションデータ格納クラス
    public static class AppData implements Serializable {
        String label;//アプリ名
        Drawable icon;//アプリアイコン
        List<String> request_permission;//要求権限
        List<String> permissionGroup;//パーミッショングループ
        String permission;//かさまし分のそれぞれの権限

    }

    //ソートしたい
    List<AppData> sortdata(List<AppData> appData) {
        Collections.sort(appData, new Comparator<AppData>() {
            @Override
            public int compare(AppData a1, AppData a2) {
                String a1name = normalize(a1.label, Normalizer.Form.NFKC);//半角カタカナでアプリ名設定をしている不届き者を変換
                String a2name = normalize(a2.label, Normalizer.Form.NFKC);
                if (a1.label == null || a2.label == null) return 0;
                return a1name.compareToIgnoreCase(a2name);//大文字小文字は区別しない場合ToIgnoreCase
            }
        });
        return appData;
    }


    //ここでリストをViewに設定する
    private final class RecyclerAdapter extends RecyclerView.Adapter {
        private final Context mContext;
        List<AppData> mdataList = new ArrayList<AppData>();//本体
        List<AppData> mdataList_copy = new ArrayList<AppData>();//本体
        List<AppData> remove_dataList = new ArrayList<AppData>();
        List<String> remove_permissionList = new ArrayList<String>();
        int count = 0;

        private RecyclerAdapter(final Context context, List<AppData> dataList) {
            mContext = context;
            mdataList = dataList;
            mdataList_copy = dataList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(mContext).inflate(R.layout.activity_main, parent, false);
            return new ViewHolder(view);
        }


        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            //holder.setIsRecyclable(false);//リサイクルして軽くするという良さは消しているが、これをしないと見るたびに色指定が変わる
            final TextView textItem = (TextView) holder.itemView.findViewById(R.id.label);
            ImageView imageView = (ImageView) holder.itemView.findViewById(R.id.imageView);
            ImageView back = (ImageView) holder.itemView.findViewById(R.id.back);
            final TextView permissiontext = (TextView) holder.itemView.findViewById(R.id.permissiongroup_text);
            //System.out.println("場所は"+position+" アプリ名：" + mdataList.get(position).label.toString());

            System.out.println("場所：" + position + " アプリ名：" + mdataList.get(position).label.toString() + "    権限：" + mdataList.get(position).permission.toString());

            textItem.setText(mdataList.get(position).label.toString());
            imageView.setImageDrawable(mdataList.get(position).icon);


            if (mdataList.get(position).permissionGroup.isEmpty()) {//もしパーミッショングループをもっていなかったら
                back.setBackgroundColor(Color.parseColor("#2196F3"));
                permissiontext.setText("危険な権限なし");
            } else {//危険な権限を持っている場合それを表示する
                permissiontext.setText(permission_e2j(mdataList.get(position).permission));
            }

            //ここからボタン処理
            //センサーボタン処理
            CheckBox sensor_checkBox = findViewById(R.id.sensorbutton);
            sensor_checkBox.setOnClickListener(view -> {
                CheckBox c = (CheckBox) view;
                if (c.isChecked()) {
                    System.out.println("ONに変更されました");
                    removeApp("センサー");
                } else {
                    System.out.println("OFFに変更されました");
                    resetApp("センサー");
                }
            });
            //カレンダーボタン処理
            CheckBox calender_checkBox = findViewById(R.id.calenderbutton);
            calender_checkBox.setOnClickListener(view -> {
                CheckBox c = (CheckBox) view;
                if (c.isChecked()) {
                    System.out.println("ONに変更されました");
                    removeApp("カレンダー");
                } else {
                    System.out.println("OFFに変更されました");
                    resetApp("カレンダー");
                }
            });
            //カメラボタン処理
            CheckBox camera_checkBox = findViewById(R.id.camerabutton);
            camera_checkBox.setOnClickListener(view -> {
                CheckBox c = (CheckBox) view;
                if (c.isChecked()) {
                    System.out.println("ONに変更されました");
                    removeApp("カメラ");
                } else {
                    System.out.println("OFFに変更されました");
                    resetApp("カメラ");
                }
            });
            //連絡先ボタン処理
            CheckBox contact_checkBox = findViewById(R.id.contactsbutton);
            contact_checkBox.setOnClickListener(view -> {
                CheckBox c = (CheckBox) view;
                if (c.isChecked()) {
                    System.out.println("ONに変更されました");
                    removeApp("連絡先");
                } else {
                    System.out.println("OFFに変更されました");
                    resetApp("連絡先");
                }
            });
            //位置情報ボタン処理
            CheckBox place_checkBox = findViewById(R.id.placebutton);
            place_checkBox.setOnClickListener(view -> {
                CheckBox c = (CheckBox) view;
                if (c.isChecked()) {
                    System.out.println("ONに変更されました");
                    removeApp("位置情報");
                } else {
                    System.out.println("OFFに変更されました");
                    resetApp("位置情報");
                }
            });
            //マイクボタン処理
            CheckBox mic_checkBox = findViewById(R.id.micbutton);
            mic_checkBox.setOnClickListener(view -> {
                CheckBox c = (CheckBox) view;
                if (c.isChecked()) {
                    System.out.println("ONに変更されました");
                    removeApp("マイク");
                } else {
                    System.out.println("OFFに変更されました");
                    resetApp("マイク");
                }
            });
            //電話ボタン処理
            CheckBox phone_checkBox = findViewById(R.id.phonebutton);
            phone_checkBox.setOnClickListener(view -> {
                CheckBox c = (CheckBox) view;
                if (c.isChecked()) {
                    System.out.println("ONに変更されました");
                    removeApp("電話機能");
                } else {
                    System.out.println("OFFに変更されました");
                    resetApp("電話機能");
                }
            });
            //smsボタン処理
            CheckBox sms_checkBox = findViewById(R.id.smsbutton);
            sms_checkBox.setOnClickListener(view -> {
                CheckBox c = (CheckBox) view;
                if (c.isChecked()) {
                    System.out.println("ONに変更されました");
                    removeApp("SMS(Cメール)");
                } else {
                    System.out.println("OFFに変更されました");
                    resetApp("SMS(Cメール)");
                }
            });
            //ストレージボタン処理
            CheckBox storage_checkBox = findViewById(R.id.storagebutton);
            storage_checkBox.setOnClickListener(view -> {
                CheckBox c = (CheckBox) view;
                if (c.isChecked()) {
                    System.out.println("ONに変更されました");
                    removeApp("ストレージ");
                } else {
                    System.out.println("OFFに変更されました");
                    resetApp("ストレージ");
                }
            });
            //その他ボタン処理
            CheckBox info_checkBox = findViewById(R.id.infobutton);
            info_checkBox.setOnClickListener(view -> {
                CheckBox c = (CheckBox) view;
                if (c.isChecked()) {
                    System.out.println("ONに変更されました");
                    removeApp("その他");
                } else {
                    System.out.println("OFFに変更されました");
                    resetApp("その他");
                }
            });
        }

        //英語のパーミッショングループ名を日本語に変換する
        //android.permission-group.STORAGE などを ストレージ に変換する
        public String permission_e2j(String s) {
            switch (s) {
                case "android.permission-group.ACTIVITY_RECOGNITION":
                    return "行動認識";
                case "android.permission-group.CALENDAR":
                    return "カレンダー";
                case "android.permission-group.CALL_LOG":
                    return "通話履歴";
                case "android.permission-group.CAMERA":
                    return "カメラ";
                case "android.permission-group.CONTACTS":
                    return "連絡先";
                case "android.permission-group.LOCATION":
                    if (Build.VERSION.SDK_INT >= 31)
                        return "位置情報";//API31以降ではWi-FiスキャンやBluetoothは含まれなくなる
                    else return "位置(Wi-Fi,Bluetooth)";//旧API端末ではそのまま
                case "android.permission-group.MICROPHONE":
                    return "マイク";
                case "android.permission-group.NEARBY_DEVICES"://API31
                    return "ニアバイデバイス(Bluetooth)";
                case "android.permission-group.NOTIFICATIONS":
                    return "通知";
                case "android.permission-group.PHONE":
                    return "電話機能";
                case "android.permission-group.READ_MEDIA_AURAL"://API33
                    return "オーディオ読み取り";
                case "android.permission-group.READ_MEDIA_VISUAL"://API33
                    return "画像・動画読み取り";
                case "android.permission-group.SENSORS":
                    return "センサー";
                case "android.permission-group.SMS":
                    return "SMS(Cメール)";
                case "android.permission-group.STORAGE":
                    return "ストレージ";
            }
            return s.substring(s.lastIndexOf(".") + 1);//おそらくここに来る場合はAndroid純正の権限ではなく独自権限
        }

        //英語のパーミッショングループ名を日本語に変換する
        //android.permission-group.STORAGE などを ストレージ に変換する
        public String permission_j2e(String s) {
            switch (s) {
                case "行動認識":
                    return "android.permission-group.ACTIVITY_RECOGNITION";
                case "カレンダー":
                    return "android.permission-group.CALENDAR";
                case "通話履歴":
                    return "android.permission-group.CALL_LOG";
                case "カメラ":
                    return "android.permission-group.CAMERA";
                case "連絡先":
                    return "android.permission-group.CONTACTS";
                case "位置情報":
                    return "android.permission-group.LOCATION";
                case "位置(Wi-Fi,Bluetooth)":
                    return "android.permission-group.LOCATION";
                case "マイク":
                    return "android.permission-group.MICROPHONE";
                case "ニアバイデバイス(Bluetooth)"://API31
                    return "android.permission-group.NEARBY_DEVICES";
                case "通知":
                    return "android.permission-group.NOTIFICATIONS";
                case "電話機能":
                    return "android.permission-group.PHONE";
                case "オーディオ読み取り"://API33
                    return "android.permission-group.READ_MEDIA_AURAL";
                case "画像・動画読み取り"://API33
                    return "android.permission-group.READ_MEDIA_VISUAL";
                case "センサー":
                    return "android.permission-group.SENSORS";
                case "SMS(Cメール)":
                    return "android.permission-group.SMS";
                case "ストレージ":
                    return "android.permission-group.STORAGE";
            }
            return "取得失敗";//
        }

        public void resetApp(String permissionname) {
            remove_permissionList.remove(permission_j2e(permissionname));
            boolean ok = false;
            do {
                for (int i = 0; i < remove_dataList.size(); i++) {
                    //もしそのアプリの権限が今消している権限ではなかったら
                    if (!(remove_permissionList.contains(remove_dataList.get(i).permission))) {
                        mdataList.add(remove_dataList.get(i));
                        System.out.println("追加 アプリ：" + remove_dataList.get(i).label + "権限名：" + remove_dataList.get(i).permission);
                        remove_dataList.remove(i);
                    }
                }
                check:
                for (int i = 0; i < remove_dataList.size(); i++) {
                    if (remove_permissionList.contains(remove_dataList.get(i).permission))
                        ok = true;
                    else {
                        ok = false;
                        System.out.println("追加 アプリ：" + remove_dataList.get(i).label + "権限名：" + remove_dataList.get(i).permission);
                        mdataList.add(remove_dataList.get(i));
                        remove_dataList.remove(i);
                        break check;//もし指定した権限以外が残っていた場合はもう一度やり直し
                    }
                }
                if (remove_dataList.isEmpty()) ok = true;
            } while (!ok);
            sortdata(mdataList);
            notifyDataSetChanged();
        }

        //権限名を渡すとその権限以外のアプリを削除する
        public void removeApp(String permissionname) {
            //もし一つ目の権限選択の場合
            if (remove_permissionList.isEmpty()) {
                remove_permissionList.add(permission_j2e(permissionname));
                boolean ok = false;
                do {
                    //アプリの数ループして、指定された権限があれば削除していく
                    for (int i = 0; i < getItemCount(); i++) {
                        if (!mdataList.get(i).permission.equals(permission_j2e(permissionname))) {
                            //System.out.println("アプリ："+ra.mdataList.get(i).label+"権限名："+ra.mdataList.get(i).permission);
                            System.out.println("削除 アプリ：" + mdataList.get(i).label + "権限名：" + mdataList.get(i).permission);
                            remove_dataList.add(mdataList.get(i));
                            mdataList.remove(i);
                            notifyItemRemoved(i);
                        }
                    }
                    //なぜか消えないアプリがあるので確かめて消し忘れがある場合もう一度消す作業を行う
                    check:
                    for (int i = 0; i < getItemCount(); i++) {
                        if (mdataList.get(i).permission.equals(permission_j2e(permissionname)))
                            ok = true;
                        else {
                            ok = false;
                            System.out.println("削除 アプリ：" + mdataList.get(i).label + "権限名：" + mdataList.get(i).permission);
                            remove_dataList.add(mdataList.get(i));
                            mdataList.remove(i);
                            notifyItemRemoved(i);
                            break check;//もし指定した権限以外が残っていた場合はもう一度やり直し
                        }
                    }
                    if (getItemCount() <= 0) ok = true;
                } while (!ok);//trueな限りdoを回り続けるという罠
                for (int i = 0; i < getItemCount(); i++) {
                    System.out.println("アプリ：" + mdataList.get(i).label + "権限名：" + mdataList.get(i).permission);
                }
            }
            //もし複数選択している場合はそのアプリの隣に表示
            else {
                boolean ok = false;
                remove_permissionList.add(permission_j2e(permissionname));
                int index = -1;
                System.out.println("remove_permissionList:" + remove_permissionList);
                do{
                    //アプリの数ループして、指定された権限があれば削除していく
                    for (int i = 0; i < remove_dataList.size(); i++) {
                        System.out.println("appname:"+remove_dataList.get(i).label+"equals:"+remove_dataList.get(i).permission.equals(permission_j2e(permissionname))+"    group:"+remove_dataList.get(i).permissionGroup+"contain:"+remove_dataList.get(i).permissionGroup.containsAll(remove_permissionList));
                        //権限が一致する場合戻ってこーい
                        if (remove_dataList.get(i).permission.equals(permission_j2e(permissionname)) && remove_dataList.get(i).permissionGroup.containsAll(remove_permissionList)) {
                            index = index_calculation(remove_dataList.get(i).label);


                            if (index != -1) {
                                System.out.println("復元 position:" + index + " アプリ：" + remove_dataList.get(i).label + "権限名：" + remove_dataList.get(i).permission);
                                mdataList.add(index, remove_dataList.get(i));
                                notifyItemInserted(index);
                                remove_dataList.remove(i);
                            }
                        }
                    }
                    for (int i = 0; i < mdataList.size(); i++) {
                        System.out.println("appname:" + mdataList.get(i).label + "    group:" + mdataList.get(i).permissionGroup + "contain:" + mdataList.get(i).permissionGroup.containsAll(remove_permissionList));
                        if (!mdataList.get(i).permissionGroup.containsAll(remove_permissionList)) {
                            System.out.println("2つ目以降削除 アプリ：" + mdataList.get(i).label + "権限名：" + mdataList.get(i).permission);
                            remove_dataList.add(mdataList.get(i));
                            mdataList.remove(i);
                            notifyItemRemoved(i);
                        }
                    }
                    //確認
                    for (int i = 0; i < mdataList.size(); i++) {
                        //System.out.println("appname:" + mdataList.get(i).label + "    group:" + mdataList.get(i).permissionGroup + "contain:" + mdataList.get(i).permissionGroup.containsAll(remove_permissionList));
                        if (!mdataList.get(i).permissionGroup.containsAll(remove_permissionList)) {
                            ok=false;
                            System.out.println("2つ目以降削除 アプリ：" + mdataList.get(i).label + "権限名：" + mdataList.get(i).permission);
                            remove_dataList.add(mdataList.get(i));
                            mdataList.remove(i);
                            notifyItemRemoved(i);
                        }else{
                            ok=true;
                        }
                    }
                    if(mdataList.size()<=0)ok=true;
                }while(!ok);



                for (int i = 0; i < getItemCount(); i++) {
                    System.out.println("アプリ：" + mdataList.get(i).label + "権限名：" + mdataList.get(i).permission);
                }

            }
        }

        public int index_calculation(String s) {
            for (int i = 0; i < mdataList.size(); i++) {
                if (mdataList.get(i).label.equals(s)) return i;
            }
            return -1;
        }


        @Override
        public int getItemCount() {
            return mdataList.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView mTextView;
            ImageView imageView;
            ImageView back;
            TextView permissiontext;

            private ViewHolder(View v) {
                super(v);
                mTextView = (TextView) v.findViewById(R.id.label);
                imageView = (ImageView) v.findViewById(R.id.imageView);
                back = (ImageView) v.findViewById(R.id.back);
                permissiontext = (TextView) v.findViewById(R.id.permissiongroup_text);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

    }


//
//    // アプリケーションのラベルとアイコンを表示するためのアダプタークラス
//    private static class AppListAdapter extends ArrayAdapter<AppData> {
//
//        private final LayoutInflater mInflater;
//
//        public AppListAdapter(Context context, List<AppData> dataList) {
//            super(context, R.layout.activity_main);
//            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            addAll(dataList);
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//
//            ViewHolder holder = new ViewHolder();
//
//            if (convertView == null) {
//                convertView = mInflater.inflate(R.layout.activity_main, parent, false);
//                holder.textLabel = (TextView) convertView.findViewById(R.id.label);
//                holder.imageIcon = (ImageView) convertView.findViewById(R.id.icon);
//                convertView.setTag(holder);
//            } else {
//                holder = (ViewHolder) convertView.getTag();
//            }
//
//            // 表示データを取得
//            final AppData data = getItem(position);
//            // ラベルとアイコンをリストビューに設定
//            holder.textLabel.setText(data.label);
//            holder.imageIcon.setImageDrawable(data.icon);
//
//            return convertView;
//        }
//    }
//
//    // ビューホルダー
//    private static class ViewHolder {
//        TextView textLabel;
//        ImageView imageIcon;
//    }
}