package view_inspector.dagger;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import dagger.Module;
import dagger.Provides;
import java.util.ArrayList;
import java.util.List;
import view_inspector.dagger.qualifier.ViewSuspects;
import view_inspector.dagger.scope.PerActivity;

@Module public class ActivityModule {
  private final Activity activity;
  private List<View> viewSuspects;

  public ActivityModule(Activity activity) {
    this.activity = activity;
    this.viewSuspects = new ArrayList<>();
  }

  @Provides @PerActivity Activity provideActivity() {
    return this.activity;
  }

  @Provides @PerActivity Context provideActivityContext() {
    return this.activity;
  }

  @Provides @PerActivity @ViewSuspects List<View> provideViewSuspects() {
    return viewSuspects;
  }
}
