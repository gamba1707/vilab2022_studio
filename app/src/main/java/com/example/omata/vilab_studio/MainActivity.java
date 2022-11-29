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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //main.xmlの内容を読み込む
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option,menu);
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
                data.permission= new ArrayList<String>();
                data.permissionGroup=new ArrayList<String>();
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
                        data.permission.add(s);//一個ずつ配列からListに入れていく

                        try {
                            permissionInfo = pm.getPermissionInfo(s, 0);
                        } catch (PackageManager.NameNotFoundException e) {

                        }
                        //権限がDangerousであるものは表示
                        //pm.checkPermission(権限名,パッケージ名)でユーザーが許可してるか取得
                        //それでもし0が出るならTrue、-1ならfalseを出力させる（三項演算子を使って）
                        if(Build.VERSION.SDK_INT >= 28){//API28(Android9)以降用
                            if (permissionInfo.getProtection()==PROTECTION_DANGEROUS){
                                System.out.println("groupname:" + permissionInfo.group + "  permname:" + s+"    権限許可状況："+((pm.checkPermission(s,app.packageName) == PackageManager.PERMISSION_GRANTED)?true:false));
                                //もし新しいグループだった場合は登録する
                                if(!data.permissionGroup.contains(permissionInfo.group))data.permissionGroup.add(permissionInfo.group);
                            }
                        }
                        else{//それ以下のバージョン用
                            if (permissionInfo.protectionLevel==PROTECTION_DANGEROUS){
                                System.out.println("groupname:" + permissionInfo.group + "  permname:" + s+"    権限許可状況："+((pm.checkPermission(s,app.packageName) == PackageManager.PERMISSION_GRANTED)?true:false));
                                //もし新しいグループだった場合は登録する
                                if(!data.permissionGroup.contains(permissionInfo.group))data.permissionGroup.add(permissionInfo.group);
                            }

                        }
                    }
                }

                //グループの数繰り返して登録して面積分にする
                for(int i=0;i<data.permissionGroup.size();i++){
                    dataList.add(data);
                }
                //0の場合も一応。。。
                if(data.permissionGroup.size()<=0){
                    dataList.add(data);
                }

                sortdata(dataList);
                System.out.println(data.permission.toString());
                System.out.println("size:"+data.permissionGroup.size());
                System.out.println(data.permissionGroup.toString());
            }

        }

        // リストビューにアプリケーションの一覧を表示する
        setContentView(R.layout.activity);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 4));
        recyclerView.setAdapter(new RecyclerAdapter(getApplicationContext(), dataList));


        //ここからボタン処理
        Button sensorbutton = findViewById(R.id.sensorbutton);
        sensorbutton.setOnClickListener( v -> {
            Toast toast = Toast.makeText(this,"センサーボタンを押しましたね", Toast.LENGTH_SHORT);
            toast.show();
        });
    }

    // アプリケーションデータ格納クラス
    private static class AppData {
        String label;//アプリ名
        Drawable icon;//アプリアイコン
        List<String>permission;//要求権限
        List<String>permissionGroup;
    }

    //ソートしたい
    ArrayList<AppData> sortdata(ArrayList<AppData> appData){
        Collections.sort(appData, new Comparator<AppData>() {
            @Override
            public int compare(AppData a1, AppData a2) {
                String a1name=normalize(a1.label, Normalizer.Form.NFKC);//半角カタカナでアプリ名設定をしている不届き者を変換
                String a2name=normalize(a2.label, Normalizer.Form.NFKC);

                if(a1.label==null||a2.label==null) return 0;
                return a1name.compareToIgnoreCase(a2name);//大文字小文字は区別しない場合ToIgnoreCase
            }
        });
        return appData;
    }


    //ここでリストをViewに設定する
    private static final class RecyclerAdapter extends RecyclerView.Adapter {
        private final Context mContext;
        List<AppData> mdataList = new ArrayList<AppData>();

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
            holder.setIsRecyclable(false);//リサイクルして軽くするという良さは消しているが、これをしないと見るたびに色指定が変わる
            final TextView textItem = (TextView) holder.itemView.findViewById(R.id.label);
            ImageView imageView = (ImageView) holder.itemView.findViewById(R.id.imageView);
            ImageView back = (ImageView) holder.itemView.findViewById(R.id.back);

            textItem.setText(mdataList.get(position).label.toString());
            imageView.setImageDrawable(mdataList.get(position).icon);

            if(mdataList.get(position).permissionGroup.isEmpty()){//もしパーミッショングループをもっていなかったら
                //System.out.println("アプリ名：" + mdataList.get(position).label.toString());
                back.setBackgroundColor(Color.parseColor("#2196F3"));
            }

        }

        @Override
        public int getItemCount() {
            return mdataList.size();
        }

        private static class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView mTextView;
            ImageView imageView;
            ImageView back;

            private ViewHolder(View v) {
                super(v);
                mTextView = (TextView) v.findViewById(R.id.label);
                imageView = (ImageView) v.findViewById(R.id.imageView);
                back = (ImageView) v.findViewById(R.id.back);
            }
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