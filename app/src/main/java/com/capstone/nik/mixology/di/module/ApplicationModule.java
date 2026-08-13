package com.capstone.nik.mixology.di.module;

import android.app.Application;
import android.content.Context;

import com.capstone.nik.mixology.Network.CocktailService;
import com.capstone.nik.mixology.Network.CocktailURLs;
import com.capstone.nik.mixology.data.DrinkDao;
import com.capstone.nik.mixology.data.MixologyDatabase;
import com.capstone.nik.mixology.repository.DrinkRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class ApplicationModule {

    private final Application application;

    public ApplicationModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Context provideApplication() {
        return application;
    }

    @Provides
    @Singleton
    MixologyDatabase provideDatabase(Context context) {
        return MixologyDatabase.create(context);
    }

    @Provides
    @Singleton
    DrinkDao provideDrinkDao(MixologyDatabase database) {
        return database.drinkDao();
    }

    @Provides
    @Singleton
    CocktailService provideCocktailService() {
        return new Retrofit.Builder()
                .baseUrl(CocktailURLs.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CocktailService.class);
    }

    @Provides
    @Singleton
    DrinkRepository provideDrinkRepository(DrinkDao dao, CocktailService service, Context context) {
        return new DrinkRepository(dao, service, context);
    }
}
