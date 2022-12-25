package com.example.omata.vilab_studio;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class sub_fragment_permission_all extends Fragment {
    public static sub_fragment_permission_all newInstance() {
        // Fragemnt01 インスタンス生成
        sub_fragment_permission_all fragment = new sub_fragment_permission_all();
        // Bundle にパラメータを設定
        Bundle barg = new Bundle();
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
        getActivity().getApplicationInfo();
        Bundle args = getArguments();

        if (args != null) {

        }

    }


}
