package com.icare.app.ui.screens.home;

import com.icare.app.data.repository.StatusRepository;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<StatusRepository> statusRepositoryProvider;

  public HomeViewModel_Factory(Provider<StatusRepository> statusRepositoryProvider) {
    this.statusRepositoryProvider = statusRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(statusRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<StatusRepository> statusRepositoryProvider) {
    return new HomeViewModel_Factory(statusRepositoryProvider);
  }

  public static HomeViewModel newInstance(StatusRepository statusRepository) {
    return new HomeViewModel(statusRepository);
  }
}
