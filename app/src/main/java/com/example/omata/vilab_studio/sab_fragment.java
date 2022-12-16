package com.example.omata.vilab_studio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

public class sab_fragment extends Fragment {
    public static sab_fragment newInstance(String str){
        // Fragemnt01 インスタンス生成
        sab_fragment fragment = new sab_fragment ();
        // Bundle にパラメータを設定
        Bundle barg = new Bundle();
        barg.putString("PackageName", str);
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


        Bundle args = getArguments();
        if(args != null ){
            String packageName = args.getString("PackageName");
            TextView textView = view.findViewById(R.id.fragment_text);
            textView.setText(packageName);
            System.out.println("sub:"+packageName);
        }

    }

}
