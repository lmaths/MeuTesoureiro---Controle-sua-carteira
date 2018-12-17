package com.rightside.meutesoureiro_controlesuacarteira;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;
import com.rightside.meutesoureiro_controlesuacarteira.activity.CadastroActivity;
import com.rightside.meutesoureiro_controlesuacarteira.activity.LoginActivity;

public class MainActivity extends IntroActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        addSlide(new FragmentSlide.Builder().background(android.R.color.holo_orange_dark).fragment(R.layout.intro_1).build());
        addSlide(new FragmentSlide.Builder().background(android.R.color.holo_orange_dark).fragment(R.layout.intro_2).build());
        addSlide(new FragmentSlide.Builder().background(android.R.color.holo_orange_dark).fragment(R.layout.intro_3).build());
        addSlide(new FragmentSlide.Builder().background(android.R.color.holo_orange_dark).fragment(R.layout.intro_4).build());
        addSlide(new FragmentSlide.Builder().background(android.R.color.holo_orange_dark).fragment(R.layout.intro_cadastro).build());

        setButtonBackVisible(false);
        setButtonNextVisible(false);
    }

    public void btEntrar(View view) {
        startActivity(new Intent(this,LoginActivity.class));
    }

    public void btCadastrar(View view) {
        startActivity(new Intent(this,CadastroActivity.class));
    }
}
