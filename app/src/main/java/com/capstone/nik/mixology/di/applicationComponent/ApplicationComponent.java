package com.capstone.nik.mixology.di.applicationComponent;

import com.capstone.nik.mixology.Network.CocktailService;
import com.capstone.nik.mixology.di.module.ApplicationModule;
import com.capstone.nik.mixology.repository.DrinkRepository;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    CocktailService cocktailService();

    DrinkRepository drinkRepository();
}
