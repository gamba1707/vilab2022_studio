package com.vilab.vilab_studio;

import static android.content.pm.PermissionInfo.PROTECTION_DANGEROUS;

import static java.text.Normalizer.normalize;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.turingtechnologies.materialscrollbar.AlphabetIndicator;
import com.turingtechnologies.materialscrollbar.DragScrollBar;
import com.turingtechnologies.materialscrollbar.INameableAdapter;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {
    int tapcount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setContentView(R.layout.activity);

        // 端末にインストール済のアプリケーション一覧情報を取得
        List<AppData> dataList = new ArrayList<AppData>();
        final PackageManager pm = getPackageManager();
        final List<ApplicationInfo> installedAppList = pm.getInstalledApplications(0);


        // リストに一覧データを格納する
        for (ApplicationInfo app : installedAppList) {
            //プリインストールされたアプリとシステムアプリ以外を読み込む
            if (!((app.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM)) {
                AppData data = new AppData();
                data.label = app.loadLabel(pm).toString();//表示されているアプリ名
                data.icon = app.loadIcon(pm);//アイコン
                data.packageName = app.packageName;//パッケージ名（com.android～みたいなあまり見ないやつ）
                data.installername = pm.getInstallerPackageName(data.packageName);//インストールした場所（com.android.vending ならGooglePlayストア）
                data.request_permission = new ArrayList<String>();
                data.permissionGroup = new ArrayList<String>();
                data.permission = "";
                List<String> requestPermissions = new ArrayList<String>();
                try {
                    //要求権限がある場合取得して、代入する
                    if (pm.getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS).requestedPermissions != null)
                        requestPermissions = Arrays.asList(pm.getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS).requestedPermissions);
                    data.request_permission = requestPermissions;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                //ここでログにアプリ名と持っている権限を表示している
                if (requestPermissions.size() > 0) {
                    System.out.println("アプリ名：" + data.label);
                    System.out.println("パッケージ名：" + data.packageName);
                    System.out.println("インストール場所：" + data.installername);
                    PermissionInfo permissionInfo = new PermissionInfo();
                    //持っている要求権限の数回る
                    for (String s : requestPermissions) {
                        try {
                            permissionInfo = pm.getPermissionInfo(s, 0);
                        } catch (PackageManager.NameNotFoundException e) {

                        }
                        //ここでそれぞれの権限の状況を確認する
                        System.out.println("groupname:" + permissionInfo.group + "  permname:" + s + "    権限許可状況：" + (pm.checkPermission(s, app.packageName) == PackageManager.PERMISSION_GRANTED));
                        System.out.println(permissionInfo.loadLabel(pm));
                        System.out.println(permissionInfo.loadDescription(pm));

                        //基本的に権限がDangerousであるものは表示
                        //pm.checkPermission(権限名,パッケージ名)でユーザーが許可してるか取得
                        //それでもし0が出るならTrue、-1ならfalseを出力させる（三項演算子を使って）
                        if (Build.VERSION.SDK_INT >= 28) {//API28(Android9)以降用
                            if (permissionInfo.getProtection() == PROTECTION_DANGEROUS) {
                                //もし新しいグループだった場合は登録する
                                if (!data.permissionGroup.contains(permissionInfo.group)&&!permissionInfo.group.equals("android.permission-group.UNDEFINED")){
                                    data.permissionGroup.add(permissionInfo.group);
                                }
                                else if(!data.permissionGroup.contains(perm2group(s.substring(s.lastIndexOf(".") + 1)))&&permissionInfo.group.equals("android.permission-group.UNDEFINED")){
                                    perm2group(s.substring(s.lastIndexOf(".") + 1));
                                    data.permissionGroup.add(perm2group(s.substring(s.lastIndexOf(".") + 1)));
                                }
                                //Dengerousではないが常時バックグラウンド許可の権限を持っていた場合追加
                            } else if (s.equals("android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"))
                                data.permissionGroup.add(s);
                        } else {//それ以下のバージョン用
                            //公式サイトでは1の場合Dengerousパーミッションですよと書いてあるのに
                            //出力させたログを見るとどう見ても4097もDengerousパーミッションなので追加（なんで？）
                            if (permissionInfo.protectionLevel == 4097 || permissionInfo.protectionLevel == 1) {
                                //もし新しいグループだった場合は登録する
                                if (!data.permissionGroup.contains(permissionInfo.group))
                                    data.permissionGroup.add(permissionInfo.group);
                            } else if (s.equals("android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"))
                                data.permissionGroup.add(s);
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
        playstore_check(dataList);
        sortdata(dataList);


        // リストビューにアプリケーションの一覧を表示する
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 4));
        recyclerView.setAdapter(new RecyclerAdapter(getApplicationContext(), dataList));

        //横にあるスクロールバーを取得
        View scrollView = findViewById(R.id.dragScrollBar);
        //文字が表示されるようにする
        if (scrollView instanceof DragScrollBar) {
            DragScrollBar materialScrollBar = (DragScrollBar) scrollView;
            materialScrollBar.setIndicator(new AlphabetIndicator(this), true);
        }
    }

    //受け取った権限名からグループ名を導き出す
    public static String perm2group(String perm){
        switch (perm){
            case "ACTIVITY_RECOGNITION":
                return "android.permission-group.ACTIVITY_RECOGNITION";
            case "READ_CALENDAR":
            case "WRITE_CALENDAR":
                return "android.permission-group.CALENDAR";
            case"READ_CALL_LOG":
            case"WRITE_CALL_LOG":
                if (Build.VERSION.SDK_INT >= 28)return "android.permission-group.CALL_LOG";
                else return "android.permission-group.PHONE";
            case"CAMERA":
                return "android.permission-group.CAMERA";
            case"READ_CONTACTS":
            case"WRITE_CONTACTS":
            case"GET_ACCOUNTS":
                return "android.permission-group.CONTACTS";
            case"ACCESS_FINE_LOCATION":
            case"ACCESS_COARSE_LOCATION":
            case"ACCESS_BACKGROUND_LOCATION":
            case"ACCESS_MEDIA_LOCATION":
                return "android.permission-group.LOCATION";
            case"RECORD_AUDIO":
                return "android.permission-group.MICROPHONE";
            case"BLUETOOTH_ADVERTISE":
            case"BLUETOOTH_CONNECT":
            case"BLUETOOTH_SCAN":
            case"NEARBY_WIFI_DEVICES":
                if (Build.VERSION.SDK_INT >= 31)return "android.permission-group.NEARBY_DEVICES";
                else return "android.permission-group.LOCATION";
            case"POST_NOTIFICATIONS":
                return "android.permission-group.NOTIFICATIONS";
            case"READ_PHONE_STATE":
            case"CALL_PHONE":
            case"ANSWER_PHONE_CALLS":
            case"ADD_VOICEMAIL":
            case"USE_SIP":
            case"READ_PHONE_NUMBERS":
            case"ACCEPT_HANDOVER":
                return "android.permission-group.PHONE";
            case"BODY_SENSORS":
            case "BODY_SENSORS_BACKGROUND":
                return "android.permission-group.SENSORS";
            case"READ_SMS":
            case"RECEIVE_WAP_PUSH":
            case"RECEIVE_MMS":
            case"RECEIVE_SMS":
            case"SEND_SMS":
                return "android.permission-group.SMS";
            case"READ_EXTERNAL_STORAGE":
            case"WRITE_EXTERNAL_STORAGE":
            case"READ_MEDIA_AUDIO":
            case"READ_MEDIA_IMAGES":
            case"READ_MEDIA_VIDEO":
                return "android.permission-group.STORAGE";
            default:
                return perm;
        }
    }

    //ここで渡されたパーミッショングループの数、データを水増しする。（中で書くとdataが一つしか生成できなくてこうするしかなかった）
    public AppData setpermission(int i, MainActivity.AppData data) {
        if (i == 0) {//0個目だけ元々作ってあるしもったいないかなって
            data.permission = data.permissionGroup.get(i);
            return data;
        } else {
            AppData d2 = new MainActivity.AppData();
            d2.label = data.label;
            d2.icon = data.icon;
            d2.packageName = data.packageName;
            d2.installername = data.installername;
            d2.request_permission = data.request_permission;
            d2.permissionGroup = data.permissionGroup;
            d2.permission = data.permissionGroup.get(i);
            return d2;
        }
    }


    // アプリケーションデータ格納クラス
    public static class AppData {
        String label;//アプリ名
        Drawable icon;//アプリアイコン
        String packageName;//パッケージ名
        String installername;//インストールした場所
        boolean non_storeapp;//野良アプリかどうか
        List<String> request_permission;//要求権限
        ArrayList<String> permissionGroup;//パーミッショングループ
        String permission;//かさまし分のそれぞれの権限

    }

    public void playstore_check(List<AppData> appData) {
        //あらかじめどう考えても正規ストアでない物をリストアップしてある
        List<String> non_store_List = new ArrayList<String>(Arrays.asList("com.google.android.packageinstaller", null));
        for (AppData data : appData) {
            //もしインストール先がGooglePlayストアではない場合
            if (data.installername == null || !(data.installername.equals("com.android.vending"))) {
                data.non_storeapp = true;
                //もし、野良アプリリストの中に含まれていれば野良アプリのパッケージ名もそのリストに登録する。（そのアプリからさらにアプリをインストールする可能性があるため）
                if (non_store_List.contains(data.installername))
                    non_store_List.add(data.packageName);
                else data.non_storeapp = false;//人違いでした、、、
            }
        }
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


    //端末に元々ある戻るボタン
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            String delete_fragment = getSupportFragmentManager().findFragmentById(R.id.container).getClass().getSimpleName();
            System.out.println("今消したフラグメント:" + delete_fragment);
            //もし消したフラグメントがsub_faragment（アプリの詳細）だった場合は上のバーにある文字と矢印を変更する
            if (delete_fragment.equals("sub_fragment")) {
                tapcount = 0;
                ActionBar actionBar = getSupportActionBar();
                actionBar.setDisplayHomeAsUpEnabled(false);//矢印非表示
                actionBar.setTitle("アプリの一覧");//上の文字
            }
            //フラグメントを一つ戻す
            getFragmentManager().popBackStack();

            return super.onKeyDown(keyCode, event);
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    //後から追加した上のバーにある矢印の戻るボタン
    @Override
    public boolean onSupportNavigateUp() {
        String delete_fragment = getSupportFragmentManager().findFragmentById(R.id.container).getClass().getSimpleName();
        if (delete_fragment.equals("sub_fragment")) {
            tapcount = 0;
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle("アプリの一覧");
        }
        getSupportFragmentManager().popBackStack();
        System.out.println("onSupportNavigateUp");
        return super.onSupportNavigateUp();
    }

    //ここでリストをViewに設定する
    private final class RecyclerAdapter extends RecyclerView.Adapter implements INameableAdapter {
        private final Context mContext;
        List<AppData> mdataList = new ArrayList<AppData>();//本体
        List<AppData> remove_dataList = new ArrayList<AppData>();//消していったアプリたち
        List<String> remove_permissionList = new ArrayList<String>();//今何の権限を選択しているのか

        private RecyclerAdapter(final Context context, List<AppData> dataList) {
            mContext = context;
            mdataList = dataList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(mContext).inflate(R.layout.activity_main, parent, false);
            return new ViewHolder(view);
        }

        //いろいろ表示を実際に割り当てる場所
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final TextView textItem = (TextView) holder.itemView.findViewById(R.id.label);
            ImageView imageView = (ImageView) holder.itemView.findViewById(R.id.imageView);
            ImageView back = (ImageView) holder.itemView.findViewById(R.id.back);
            final TextView permissiontext = (TextView) holder.itemView.findViewById(R.id.permissiongroup_text);

            //positionは最初の状態から変化しないらしい
            //なので色々更新した後にはその時の状態がとれるholder.getLayoutPosition()にするらしい
            System.out.println(remove_permissionList);
            System.out.println("場所：" + holder.getLayoutPosition() + " アプリ名：" + mdataList.get(holder.getLayoutPosition()).label.toString() + "    権限：" + mdataList.get(holder.getLayoutPosition()).permission.toString());
            //アプリ名
            textItem.setText(mdataList.get(position).label.toString());
            //アプリのアイコン
            imageView.setImageDrawable(mdataList.get(position).icon);
            //Playストアじゃない場合、色を赤にする
            if (mdataList.get(holder.getLayoutPosition()).non_storeapp) {
                back.setBackgroundColor(Color.parseColor("#cc0000"));
            } else {
                //Playストアの場合一旦ピンクにする
                back.setBackgroundColor(Color.parseColor("#F8BBD0"));
            }

            if (mdataList.get(holder.getLayoutPosition()).permissionGroup.isEmpty()) {//もしパーミッショングループをもっていなかったら
                if (mdataList.get(holder.getLayoutPosition()).non_storeapp)
                    back.setBackgroundColor(Color.parseColor("#aa66cc"));//野良だけど権限なしなら紫にしておく
                else back.setBackgroundColor(Color.parseColor("#2196F3"));//Playストアからなら青
                permissiontext.setText("危険な権限なし");
            } else {//危険な権限を持っている場合それを表示する
                permissiontext.setText(permission_e2j(mdataList.get(position).permission));
            }


            //RecycleViewの中のそれぞれのアプリのボタン
            final int p = position;
            String li = String.valueOf(getItemId(position));
            ActionBar actionBar = getSupportActionBar();
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //一回以上押されないようにする
                    if (tapcount <= 0) {
                        tapcount++;
                        actionBar.setDisplayHomeAsUpEnabled(true);//上のバーの戻るボタンを表示
                        actionBar.setTitle("アプリの詳細");//タイトル変更
                        Toast.makeText(v.getContext(), mdataList.get(p).label, Toast.LENGTH_SHORT).show();//クリックしたアプリ名をトースト表示
                        //別画面（Fragment）起動
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                        // BackStackを設定
                        fragmentTransaction.addToBackStack(null);

                        // パラメータを設定
                        //パッケージ名、パーミッショングループ、野良アプリかどうかの情報、要求権限の情報、を渡して画面を起動
                        fragmentTransaction.add(R.id.container,
                                sub_fragment.newInstance(mdataList.get(p).packageName, mdataList.get(p).permissionGroup, mdataList.get(p).non_storeapp, mdataList.get(p).request_permission));
                        fragmentTransaction.commit();

                    }
                }
            });

            //ここから画面下部にあるボタン処理
            //上手いこと上のFragmentは同じ位置にボタンがあるので反応するかもしれないが可能性が低いのでこのままいく
            //ボタンのように見せてCheckBoxを改造したやつなのでクリックしたかどうか
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
            //野良アプリボタン処理
            CheckBox info_checkBox = findViewById(R.id.non_playstorebutton);
            info_checkBox.setOnClickListener(view -> {
                CheckBox c = (CheckBox) view;
                if (c.isChecked()) {
                    System.out.println("ONに変更されました");
                    removeApp("野良アプリ");
                } else {
                    System.out.println("OFFに変更されました");
                    resetApp("野良アプリ");
                }
            });
            //野良アプリボタン処理
            CheckBox background_checkBox = findViewById(R.id.backgroundbutton);
            background_checkBox.setOnClickListener(view -> {
                CheckBox c = (CheckBox) view;
                if (c.isChecked()) {
                    System.out.println("ONに変更されました");
                    removeApp("常時バック許可");
                } else {
                    System.out.println("OFFに変更されました");
                    resetApp("常時バック許可");
                }
            });
            //行動認識ボタン処理(API29)
            CheckBox activity_checkBox = findViewById(R.id.activitybutton);
            activity_checkBox.setOnClickListener(view -> {
                CheckBox c = (CheckBox) view;
                if (c.isChecked()) {
                    System.out.println("ONに変更されました");
                    removeApp("行動認識");
                } else {
                    System.out.println("OFFに変更されました");
                    resetApp("行動認識");
                }
            });
            //通話履歴ボタン処理(API28)
            CheckBox calllog_checkBox = findViewById(R.id.calllogbutton);
            calllog_checkBox.setOnClickListener(view -> {
                CheckBox c = (CheckBox) view;
                if (c.isChecked()) {
                    System.out.println("ONに変更されました");
                    removeApp("通話履歴");
                } else {
                    System.out.println("OFFに変更されました");
                    resetApp("通話履歴");
                }
            });
            //ニアバイデバイスボタン処理（API31）
            CheckBox nearby_checkBox = findViewById(R.id.nearbybutton);
            nearby_checkBox.setOnClickListener(view -> {
                CheckBox c = (CheckBox) view;
                if (c.isChecked()) {
                    System.out.println("ONに変更されました");
                    removeApp("ニアバイデバイス(Bluetooth)");
                } else {
                    System.out.println("OFFに変更されました");
                    resetApp("ニアバイデバイス(Bluetooth)");
                }
            });
        }

        //英語のパーミッショングループ名を日本語に変換するメソッド
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
                case "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS":
                    return "常時バック許可";
            }
            return s.substring(s.lastIndexOf(".") + 1);//おそらくここに来る場合はAndroid純正の権限ではなく独自権限
        }

        //英語のパーミッショングループ名を日本語に変換するメソッド
        //android.permission-group.STORAGE などを ストレージ に変換する
        public String permission_j2e(String s) {
            switch (s) {
                case "身体認識":
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
                case "常時バック許可":
                    return "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS";
            }
            return "取得失敗";//
        }

        //もらった権限の絞り込みを解除する
        public void resetApp(String permissionname) {
            boolean check = true;
            //野良アプリの場合権限ではないので別に用意
            if (permissionname.equals("野良アプリ")) {
                do {//消したアプリの中を確認する
                    for (int i = 0; i < remove_dataList.size(); i++) {
                        //消したアプリが野良アプリではなく、絞り込みが選択されている権限を持っていたら、再表示する
                        if (!remove_dataList.get(i).non_storeapp && remove_dataList.get(i).permissionGroup.containsAll(remove_permissionList)) {
                            mdataList.add(remove_dataList.get(i));
                            remove_dataList.remove(i);
                        }
                    }
                    //一回だとなぜか見逃しがあるので、確認し見逃しがあった場合はもう一度周回する
                    for (int i = 0; i < remove_dataList.size(); i++) {
                        if (!remove_dataList.get(i).non_storeapp && remove_dataList.get(i).permissionGroup.containsAll(remove_permissionList)) {
                            mdataList.add(remove_dataList.get(i));
                            remove_dataList.remove(i);
                            check = true;
                            break;
                        } else {
                            check = false;
                        }
                    }
                    //もうリストの中にデータがない（全て表示した）場合は抜け出す
                    if (remove_dataList.size() <= 0) check = false;
                } while (check);
                sortdata(mdataList);
                notifyDataSetChanged();
            } else {//普通の権限たち
                // チェックボックスのオブジェクトを取得
                CheckBox non_playstore_checkBox = (CheckBox) findViewById(R.id.non_playstorebutton);
                // チェック状態を取得
                boolean non_playstore_checkBoxChecked = non_playstore_checkBox.isChecked();
                remove_permissionList.remove(permission_j2e(permissionname));
                do {
                    //表示されているアプリ分回る
                    for (int i = 0; i < remove_dataList.size(); i++) {
                        if (non_playstore_checkBoxChecked && remove_dataList.get(i).non_storeapp) {
                            //System.out.println("アプリ："+remove_dataList.get(i).label+remove_dataList.get(i).non_storeapp);
                            //もし、その削除したアプリの中にもらった権限が存在しない。尚且つ、消したアプリの権限の中に表示するべき（まだ選択中の）権限なら再表示（その権限を含んでいるという絞り込みを解除したいから）
                            if (!remove_dataList.get(i).permissionGroup.contains(permission_j2e(permissionname)) && remove_dataList.get(i).permissionGroup.containsAll(remove_permissionList)) {
                                System.out.println("アプリ：" + remove_dataList.get(i).label);
                                mdataList.add(remove_dataList.get(i));
                                remove_dataList.remove(i);
                            }
                        } else if (!non_playstore_checkBoxChecked) {
                            //もし、その削除したアプリの中にもらった権限が存在しない。尚且つ、消したアプリの権限の中に表示するべき（まだ選択中の）権限なら再表示（その権限を含んでいるという絞り込みを解除したいから）
                            if (!remove_dataList.get(i).permissionGroup.contains(permission_j2e(permissionname)) && remove_dataList.get(i).permissionGroup.containsAll(remove_permissionList)) {
                                System.out.println("？アプリ：" + remove_dataList.get(i).label);
                                mdataList.add(remove_dataList.get(i));
                                remove_dataList.remove(i);
                            }
                        }
                    }
                    //なぜかすり抜けるやつがあるので確認してまだあるようならもう一度上のを実行する
                    check:
                    for (int i = 0; i < remove_dataList.size(); i++) {
                        if (non_playstore_checkBoxChecked && remove_dataList.get(i).non_storeapp) {
                            //もし、その削除したアプリの中にもらった権限が存在しない。尚且つ、消したアプリの権限の中に表示するべき（まだ選択中の）権限なら再表示（その権限を含んでいるという絞り込みを解除したいから）
                            if (!remove_dataList.get(i).permissionGroup.contains(permission_j2e(permissionname)) && remove_dataList.get(i).permissionGroup.containsAll(remove_permissionList)) {
                                System.out.println("追加アプリ：" + remove_dataList.get(i).label);
                                mdataList.add(remove_dataList.get(i));
                                remove_dataList.remove(i);
                                check = true;
                                break check;
                            } else {
                                check = false;
                            }
                        } else if (!non_playstore_checkBoxChecked) {
                            //もし、その削除したアプリの中にもらった権限が存在しない。尚且つ、消したアプリの権限の中に表示するべき（まだ選択中の）権限なら再表示（その権限を含んでいるという絞り込みを解除したいから）
                            if (!remove_dataList.get(i).permissionGroup.contains(permission_j2e(permissionname)) && remove_dataList.get(i).permissionGroup.containsAll(remove_permissionList)) {
                                System.out.println("？追加アプリ：" + remove_dataList.get(i).label);
                                mdataList.add(remove_dataList.get(i));
                                remove_dataList.remove(i);
                                check = true;
                                break check;
                            } else {
                                check = false;
                            }
                        } else {
                            check = false;
                        }
                    }
                    //もうリストの中にデータがない（全て表示した）場合は抜け出す
                    if (remove_dataList.size() <= 0) check = false;
                } while (check);
                sortdata(mdataList);
                notifyDataSetChanged();
            }

        }

        //権限名を渡すとその権限以外のアプリを削除する
        public void removeApp(String permissionname) {
            boolean check = true;
            if (permissionname.equals("野良アプリ")) {
                do {
                    for (int i = 0; i < mdataList.size(); i++) {
                        if (!mdataList.get(i).non_storeapp) {
                            remove_dataList.add(mdataList.get(i));
                            mdataList.remove(i);
                            notifyItemRemoved(i);
                        }
                    }
                    for (int i = 0; i < mdataList.size(); i++) {
                        if (!mdataList.get(i).non_storeapp) {
                            remove_dataList.add(mdataList.get(i));
                            mdataList.remove(i);
                            notifyItemRemoved(i);
                            check = true;
                            break;
                        } else {
                            check = false;
                        }
                    }
                    //もうリストの中にデータがない（全て削除した）場合は抜け出す
                    if (mdataList.size() <= 0) check = false;
                } while (check);
            } else {
                remove_permissionList.add(permission_j2e(permissionname));
                do {
                    //表示されているアプリ分回る
                    for (int i = 0; i < mdataList.size(); i++) {
                        //もし、そのアプリの中にもらった権限が存在しなければ
                        if (!mdataList.get(i).permissionGroup.contains(permission_j2e(permissionname))) {
                            remove_dataList.add(mdataList.get(i));
                            mdataList.remove(i);
                            notifyItemRemoved(i);
                        }
                    }
                    for (int i = 0; i < mdataList.size(); i++) {
                        //もし、そのアプリの中にもらった権限が存在しなければ
                        if (!mdataList.get(i).permissionGroup.contains(permission_j2e(permissionname))) {
                            remove_dataList.add(mdataList.get(i));
                            mdataList.remove(i);
                            notifyItemRemoved(i);
                            check = true;
                            break;
                        } else {
                            check = false;
                        }
                    }
                    //もうリストの中にデータがない（全て削除した）場合は抜け出す
                    if (mdataList.size() <= 0) check = false;
                } while (check);
            }

        }

        @Override
        public int getItemCount() {
            return mdataList.size();
        }

        //ここでスクロールバーの文字を取得？設定？する
        @Override
        public Character getCharacterForElement(int i) {
            //位置的に普通のリスト表示なら一番上の文字を取ればいいが、グリッド表示なのでそれ用に見やすいように調整
            //一番左上から0,1,2,と増えていくので一番左の列番号を求めてそれの一段下の文字を表示する
            int position = ((i / 4) * 4) + 4;
            AppData appData = mdataList.get(position);
            String kana = normalize(appData.label, Normalizer.Form.NFKC);
            Character c = new Character('#');
            if (kana != null && kana.length() > 0) {
                c = kana.charAt(0);
            }
            return c;
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
            return mdataList.get(position).hashCode();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }
    }
}