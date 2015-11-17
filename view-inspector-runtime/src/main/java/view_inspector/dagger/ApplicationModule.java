package view_inspector.dagger;

import android.app.Application;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.WindowManager;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import dagger.Module;
import dagger.Provides;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Singleton;
import view_inspector.R;
import view_inspector.dagger.qualifier.BypassInterceptor;
import view_inspector.dagger.qualifier.LogViewEvents;
import view_inspector.dagger.qualifier.ProbeMeasures;
import view_inspector.dagger.qualifier.Profiling;
import view_inspector.dagger.qualifier.Scalpel3D;
import view_inspector.dagger.qualifier.ScalpelShowId;
import view_inspector.dagger.qualifier.ScalpelWireframe;
import view_inspector.dagger.qualifier.ShowMargin;
import view_inspector.dagger.qualifier.ShowMeasureCount;
import view_inspector.dagger.qualifier.ShowOutline;
import view_inspector.dagger.qualifier.ShowPadding;
import view_inspector.dagger.qualifier.ViewFilter;
import view_inspector.dagger.qualifier.ViewTag;
import view_inspector.database.DbOpenHelper;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.WINDOW_SERVICE;

@Module public class ApplicationModule {
  private final Application application;

  public ApplicationModule(Application application) {
    this.application = application;
  }

  @Provides @Singleton Application provideApplicationContext() {
    return this.application;
  }

  @Provides @Singleton SharedPreferences provideSharedPreferences(Application app) {
    return app.getSharedPreferences("view-inspector", MODE_PRIVATE);
  }

  @Provides @Singleton RxSharedPreferences provideRxSharedPreferences(SharedPreferences prefs) {
    return RxSharedPreferences.create(prefs);
  }

  @Provides @Singleton @ShowOutline Preference<Boolean> provideShowOutlineFlag(
      RxSharedPreferences prefs) {
    return prefs.getBoolean("showOutline", false);
  }

  @Provides @Singleton @ShowMargin Preference<Boolean> provideShowMarginFlag(
      RxSharedPreferences prefs) {
    return prefs.getBoolean("showMargin", false);
  }

  @Provides @Singleton @ShowPadding Preference<Boolean> provideShowPaddingFlag(
      RxSharedPreferences prefs) {
    return prefs.getBoolean("showPadding", false);
  }

  @Provides @Singleton @BypassInterceptor Preference<Boolean> provideBypassInterceptorFlag(
      RxSharedPreferences prefs) {
    return prefs.getBoolean("bypassInterceptor", false);
  }

  @Provides @Singleton @ProbeMeasures Preference<Boolean> provideProbeMeasuresFlag(
      RxSharedPreferences prefs) {
    return prefs.getBoolean("probeMeasures", false);
  }

  @Provides @Singleton @Profiling Preference<Boolean> provideProfilingFlag(
      RxSharedPreferences prefs) {
    return prefs.getBoolean("profiling", false);
  }

  @Provides @Singleton @LogViewEvents Preference<Boolean> provideLogViewEventsFlag(
      RxSharedPreferences prefs) {
    return prefs.getBoolean("logViewEvents", false);
  }

  @Provides @Singleton @Scalpel3D Preference<Boolean> provideScalpel3dFlag(
      RxSharedPreferences prefs) {
    return prefs.getBoolean("scalpel3d", false);
  }

  @Provides @Singleton @ScalpelWireframe Preference<Boolean> provideScalpelWireframeFlag(
      RxSharedPreferences prefs) {
    return prefs.getBoolean("scalpelWireframe", false);
  }

  @Provides @Singleton @ScalpelShowId Preference<Boolean> provideScalpelIdFlag(
      RxSharedPreferences prefs) {
    return prefs.getBoolean("scalpelId", false);
  }

  @Provides @Singleton @ShowMeasureCount Preference<Boolean> provideShowMeasureCountFlag(
      RxSharedPreferences prefs) {
    return prefs.getBoolean("showMeasureCount", false);
  }

  @Provides @Singleton @ViewFilter Preference<Set<String>> provideViewFilterSet(
      RxSharedPreferences prefs) {
    return prefs.getStringSet("viewFilter", new HashSet<String>());
  }

  @Provides @Singleton SQLiteOpenHelper provideDbOpenHelper(Application application) {
    return new DbOpenHelper(application);
  }

  @Provides @Singleton WindowManager provideWindowManager() {
    return (WindowManager) application.getSystemService(WINDOW_SERVICE);
  }

  @Provides @Singleton @ViewTag String provideViewTag() {
    return application.getResources().getString(R.string.view_inspector_view_tag);
  }
}
