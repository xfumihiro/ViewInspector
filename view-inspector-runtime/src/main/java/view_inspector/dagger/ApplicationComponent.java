package view_inspector.dagger;

import android.database.sqlite.SQLiteOpenHelper;
import android.view.WindowManager;
import com.f2prateek.rx.preferences.Preference;
import dagger.Component;
import java.util.Set;
import javax.inject.Singleton;
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

@Singleton @Component(modules = ApplicationModule.class) public interface ApplicationComponent {

  // expose to sub components
  @ShowOutline Preference<Boolean> provideShowOutlineFlag();

  @ShowMargin Preference<Boolean> provideShowMarginFlag();

  @ShowPadding Preference<Boolean> provideShowPaddingFlag();

  @BypassInterceptor Preference<Boolean> provideBypassProbeFlag();

  @ProbeMeasures Preference<Boolean> provideProbeMeasuresFlag();

  @ShowMeasureCount Preference<Boolean> provideShowMeasureCountFlag();

  @Profiling Preference<Boolean> provideProfilingFlag();

  @LogViewEvents Preference<Boolean> provideLogViewEventsFlag();

  @Scalpel3D Preference<Boolean> provideScalpel3dFlag();

  @ScalpelWireframe Preference<Boolean> provideScalpelWireframeFlag();

  @ScalpelShowId Preference<Boolean> provideScalpelidFlag();

  SQLiteOpenHelper provideDbOpenHelper();

  WindowManager provideWindowManager();

  @ViewTag String provideViewTag();

  @ViewFilter Preference<Set<String>> provideViewFilterSet();
}
