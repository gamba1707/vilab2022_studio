package com.example.omata.vilab_studio;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class sab_fragment extends Fragment {
    public static sab_fragment newInstance(String packagename){
        // Fragemnt01 インスタンス生成
        sab_fragment fragment = new sab_fragment ();
        // Bundle にパラメータを設定
        Bundle barg = new Bundle();
        barg.putString("PackageName", packagename);
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
        PackageManager pm=getActivity().getPackageManager();
        getActivity().getApplicationInfo();
        Bundle args = getArguments();
        if(args != null ){
            String packageName = args.getString("PackageName");//もらってきたパッケージ名
            TextView textView = view.findViewById(R.id.fragment_appnametext);
            TextView installtextView = view.findViewById(R.id.fragment_installtext);
            ImageView iconView=view.findViewById(R.id.fragment_icon);
            //日付の形式を日本語に
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy'年'MM'月'dd'日'E'曜日'");
            //このアプリをインストールしたインストーラー名を取得
            String installer=pm.getInstallerPackageName(packageName);
            System.out.println(installer);
            String installername="";


            try {
                //アプリのアイコン取得
                Drawable icon=pm.getApplicationIcon(packageName);
                //パッケージ名を渡してアプリ名を取得
                String applicationLabel = pm.getApplicationLabel(pm.getApplicationInfo(packageName,PackageManager.GET_META_DATA)).toString();
                //インストール名を日本語の表示名に変換(パッケージ名を渡してアプリ名（擬似的）を得る)
                if(installer!=null)installername=pm.getApplicationLabel(pm.getApplicationInfo(installer,PackageManager.GET_META_DATA)).toString();
                else installername="取得できないところ";
                //インストール日時取得
                Date dateFirstInstallTime = new Date(pm.getPackageInfo(packageName,PackageManager.GET_META_DATA).firstInstallTime);
                installtextView.setText(sdf.format(dateFirstInstallTime)+"に\n"+installername+"からインストール");
                textView.setText(applicationLabel);
                iconView.setImageDrawable(icon);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();

            }

            System.out.println("sub:"+packageName);

        }

    }


}
