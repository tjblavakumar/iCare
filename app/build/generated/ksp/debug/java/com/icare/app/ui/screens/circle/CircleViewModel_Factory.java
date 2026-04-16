package com.icare.app.ui.screens.circle;

import com.icare.app.data.repository.ConnectionRepository;
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
public final class CircleViewModel_Factory implements Factory<CircleViewModel> {
  private final Provider<ConnectionRepository> connectionRepositoryProvider;

  public CircleViewModel_Factory(Provider<ConnectionRepository> connectionRepositoryProvider) {
    this.connectionRepositoryProvider = connectionRepositoryProvider;
  }

  @Override
  public CircleViewModel get() {
    return newInstance(connectionRepositoryProvider.get());
  }

  public static CircleViewModel_Factory create(
      Provider<ConnectionRepository> connectionRepositoryProvider) {
    return new CircleViewModel_Factory(connectionRepositoryProvider);
  }

  public static CircleViewModel newInstance(ConnectionRepository connectionRepository) {
    return new CircleViewModel(connectionRepository);
  }
}
