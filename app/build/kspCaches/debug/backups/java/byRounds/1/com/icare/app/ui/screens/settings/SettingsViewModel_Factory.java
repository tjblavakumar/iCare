package com.icare.app.ui.screens.settings;

import com.icare.app.data.repository.AuthRepository;
import com.icare.app.data.repository.ConnectionRepository;
import com.icare.app.data.repository.ContactDiscoveryRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<ConnectionRepository> connectionRepositoryProvider;

  private final Provider<ContactDiscoveryRepository> contactDiscoveryRepositoryProvider;

  public SettingsViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<ConnectionRepository> connectionRepositoryProvider,
      Provider<ContactDiscoveryRepository> contactDiscoveryRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.connectionRepositoryProvider = connectionRepositoryProvider;
    this.contactDiscoveryRepositoryProvider = contactDiscoveryRepositoryProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(authRepositoryProvider.get(), connectionRepositoryProvider.get(), contactDiscoveryRepositoryProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<ConnectionRepository> connectionRepositoryProvider,
      Provider<ContactDiscoveryRepository> contactDiscoveryRepositoryProvider) {
    return new SettingsViewModel_Factory(authRepositoryProvider, connectionRepositoryProvider, contactDiscoveryRepositoryProvider);
  }

  public static SettingsViewModel newInstance(AuthRepository authRepository,
      ConnectionRepository connectionRepository,
      ContactDiscoveryRepository contactDiscoveryRepository) {
    return new SettingsViewModel(authRepository, connectionRepository, contactDiscoveryRepository);
  }
}
