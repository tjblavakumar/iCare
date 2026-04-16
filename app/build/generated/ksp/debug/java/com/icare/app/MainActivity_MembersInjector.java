package com.icare.app;

import androidx.datastore.core.DataStore;
import androidx.datastore.preferences.core.Preferences;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<DataStore<Preferences>> dataStoreProvider;

  public MainActivity_MembersInjector(Provider<DataStore<Preferences>> dataStoreProvider) {
    this.dataStoreProvider = dataStoreProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<DataStore<Preferences>> dataStoreProvider) {
    return new MainActivity_MembersInjector(dataStoreProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectDataStore(instance, dataStoreProvider.get());
  }

  @InjectedFieldSignature("com.icare.app.MainActivity.dataStore")
  public static void injectDataStore(MainActivity instance, DataStore<Preferences> dataStore) {
    instance.dataStore = dataStore;
  }
}
