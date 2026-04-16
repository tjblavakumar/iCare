package com.icare.app.ui.screens.circle;

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
public final class ContactHistoryViewModel_Factory implements Factory<ContactHistoryViewModel> {
  private final Provider<StatusRepository> statusRepositoryProvider;

  public ContactHistoryViewModel_Factory(Provider<StatusRepository> statusRepositoryProvider) {
    this.statusRepositoryProvider = statusRepositoryProvider;
  }

  @Override
  public ContactHistoryViewModel get() {
    return newInstance(statusRepositoryProvider.get());
  }

  public static ContactHistoryViewModel_Factory create(
      Provider<StatusRepository> statusRepositoryProvider) {
    return new ContactHistoryViewModel_Factory(statusRepositoryProvider);
  }

  public static ContactHistoryViewModel newInstance(StatusRepository statusRepository) {
    return new ContactHistoryViewModel(statusRepository);
  }
}
