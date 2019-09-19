package com.x3noku.daily_maps_android;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;

public class IntroActivity extends AppIntro2 {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SliderPage sliderPage1 = new SliderPage();
        sliderPage1.setTitle(getResources().getString(R.string.first_slide_title));
        sliderPage1.setDescription(getResources().getString(R.string.first_slide_description));
        sliderPage1.setImageDrawable(R.drawable.ic_slide1);
        sliderPage1.setBgColor( ResourcesCompat.getColor(getResources(), R.color.firstSlideColorPrimary, null) );
        addSlide(AppIntroFragment.newInstance(sliderPage1));

        SliderPage sliderPage2 = new SliderPage();
        sliderPage2.setTitle(getResources().getString(R.string.second_slide_title));
        sliderPage2.setDescription(getResources().getString(R.string.second_slide_description));
        sliderPage2.setImageDrawable(R.drawable.ic_slide2);
        sliderPage2.setBgColor( ResourcesCompat.getColor(getResources(), R.color.secondSlideColorPrimary, null) );
        addSlide(AppIntroFragment.newInstance(sliderPage2));

        SliderPage sliderPage3 = new SliderPage();
        sliderPage3.setTitle(getResources().getString(R.string.third_slide_title));
        sliderPage3.setDescription(getResources().getString(R.string.third_slide_description));
        sliderPage3.setImageDrawable(R.drawable.ic_slide3);
        sliderPage3.setBgColor( ResourcesCompat.getColor(getResources(), R.color.thirdSlideColorPrimary, null) );
        addSlide(AppIntroFragment.newInstance(sliderPage3));

    }


    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        startActivity(new Intent(getBaseContext(), FirebaseAuthActivity.class));
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        startActivity(new Intent(getBaseContext(), FirebaseAuthActivity.class));
        finish();
    }
}
