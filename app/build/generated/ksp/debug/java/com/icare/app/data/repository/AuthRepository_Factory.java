package com.icare.app.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AuthRepository_Factory implements Factory<AuthRepository> {
  private final Provider<FirebaseAuth> authProvider;

  private final Provider<FirebaseFirestore> firestoreProvider;

  private final Provider<FirebaseMessaging> messagingProvider;

  public AuthRepository_Factory(Provider<FirebaseAuth> authProvider,
      Provider<FirebaseFirestore> firestoreProvider,
      Provider<FirebaseMessaging> messagingProvider) {
    this.authProvider = authProvider;
    this.firestoreProvider = firestoreProvider;
    this.messagingProvider = messagingProvider;
  }

  @Override
  public AuthRepository get() {
    return newInstance(authProvider.get(), firestoreProvider.get(), messagingProvider.get());
  }

  public static AuthRepository_Factory create(Provider<FirebaseAuth> authProvider,
      Provider<FirebaseFirestore> firestoreProvider,
      Provider<FirebaseMessaging> messagingProvider) {
    return new AuthRepository_Factory(authProvider, firestoreProvider, messagingProvider);
  }

  public static AuthRepository newInstance(FirebaseAuth auth, FirebaseFirestore firestore,
      FirebaseMessaging messaging) {
    return new AuthRepository(auth, firestore, messaging);
  }
}
