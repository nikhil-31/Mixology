package com.capstone.nik.mixology.di.applicationComponent;

import com.capstone.nik.mixology.Fragments.FragmentAlcoholic;
import com.capstone.nik.mixology.Fragments.FragmentCocktail;
import com.capstone.nik.mixology.Fragments.FragmentCocktailGlass;
import com.capstone.nik.mixology.Fragments.FragmentDetails;
import com.capstone.nik.mixology.Fragments.FragmentDrink;
import com.capstone.nik.mixology.Fragments.FragmentGin;
import com.capstone.nik.mixology.Fragments.FragmentHighballGlass;
import com.capstone.nik.mixology.Fragments.FragmentNonAlcoholic;
import com.capstone.nik.mixology.Fragments.FragmentOrdinaryDrink;
import com.capstone.nik.mixology.Fragments.FragmentRandomixer;
import com.capstone.nik.mixology.Fragments.FragmentVodka;
import com.capstone.nik.mixology.di.module.ApplicationModule;
import com.capstone.nik.mixology.services.MyGcmJobService;
import com.capstone.nik.mixology.services.MyJobService;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by nik on 1/27/2018.
 */
@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

  void inject(FragmentAlcoholic target);

  void inject(FragmentCocktail target);

  void inject(FragmentCocktailGlass target);

  void inject(FragmentDetails target);

  void inject(FragmentGin target);

  void inject(FragmentHighballGlass target);

  void inject(FragmentNonAlcoholic target);

  void inject(FragmentOrdinaryDrink target);

  void inject(FragmentRandomixer target);

  void inject(FragmentVodka target);

  void inject(MyGcmJobService target);

  void inject(MyJobService target);

  void inject(FragmentDrink target);
}
