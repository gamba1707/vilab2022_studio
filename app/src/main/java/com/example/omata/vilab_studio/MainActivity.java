package com.example.omata.vilab_studio;

import static android.content.pm.PermissionInfo.PROTECTION_DANGEROUS;

import static java.text.Normalizer.normalize;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {
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

        // 端末にインストール済のアプリケーション一覧情報を取得
        final PackageManager pm = getPackageManager();
        final int flags = PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_DISABLED_COMPONENTS;
        final List<ApplicationInfo> installedAppList = pm.getInstalledApplications(0);
        //とりあえず仮置きで権限を入れて表示（100個も権限ないだろみたいな）
        String[] requestedPermissions = new String[100];


        // リストに一覧データを格納する
        ArrayList<AppData> dataList = new ArrayList<AppData>();
        for (ApplicationInfo app : installedAppList) {
            if (!((app.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM)) {
                AppData data = new AppData();
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
                        //権限がDangerousであるものは表示
                        //pm.checkPermission(権限名,パッケージ名)でユーザーが許可してるか取得
                        //それでもし0が出るならTrue、-1ならfalseを出力させる（三項演算子を使って）
                        if (Build.VERSION.SDK_INT >= 28) {//API28(Android9)以降用
                            if (permissionInfo.getProtection() == PROTECTION_DANGEROUS) {
                                //System.out.println("groupname:" + permissionInfo.group + "  permname:" + s+"    権限許可状況："+((pm.checkPermission(s,app.packageName) == PackageManager.PERMISSION_GRANTED)?true:false));
                                //もし新しいグループだった場合は登録する
                                if (!data.permissionGroup.contains(permissionInfo.group))
                                    data.permissionGroup.add(permissionInfo.group);
                            }
                        } else {//それ以下のバージョン用
                            if (permissionInfo.protectionLevel == PROTECTION_DANGEROUS) {
                                //System.out.println("groupname:" + permissionInfo.group + "  permname:" + s+"    権限許可状況："+((pm.checkPermission(s,app.packageName) == PackageManager.PERMISSION_GRANTED)?true:false));
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
            sortdata(dataList);
        }

        // リストビューにアプリケーションの一覧を表示する
        setContentView(R.layout.activity);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 4));
        recyclerView.setAdapter(new RecyclerAdapter(getApplicationContext(), dataList));

    }

    //ここで渡されたパーミッショングループの数、データを水増しする。（中で書くとdataが一つしか生成できなくてこうするしかなかった）
    public AppData setpermission(int i, AppData data) {
        if (i == 0) {//0個目だけ元々作ってあるしもったいないかなって
            data.permission = data.permissionGroup.get(i);
            return data;
        } else {
            AppData d2 = new AppData();
            d2.label = data.label;
            d2.icon = data.icon;
            d2.request_permission = data.request_permission;
            d2.permissionGroup = data.permissionGroup;
            d2.permission = data.permissionGroup.get(i);
            return d2;
        }
    }

    // アプリケーションデータ格納クラス
    private static class AppData {
        String label;//アプリ名
        Drawable icon;//アプリアイコン
        List<String> request_permission;//要求権限
        List<String> permissionGroup;//パーミッショングループ
        String permission;//かさまし分のそれぞれの権限
    }

    //ソートしたい
    ArrayList<AppData> sortdata(ArrayList<AppData> appData) {
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
        List<AppData> mdataList = new ArrayList<AppData>();
        int count = 0;

        private RecyclerAdapter(final Context context, List<AppData> dataList) {
            mContext = context;
            mdataList = dataList;
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
            CheckBox checkBox = findViewById(R.id.sensorbutton);
            checkBox.setOnClickListener(view -> {
                CheckBox c = (CheckBox)view;
                if (c.isChecked()) {
                    System.out.println("ONに変更されました");
                } else {
                    System.out.println("OFFに変更されました");
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
                    else return "位置(Wi-Fiスキャン,Bluetooth含む)";//旧API端末ではそのまま
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

        public void removeApp(String permissionname,RecyclerView.ViewHolder holder) {

            for (int i = 0; i < mdataList.size(); i++) {
                System.out.println("ID:"+holder.getItemId()+"position:"+holder.getAdapterPosition());
            }
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