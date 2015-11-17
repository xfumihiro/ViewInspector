package view_inspector.ui;

import android.content.Context;
import com.jakewharton.scalpel.ScalpelFrameLayout;
import javax.inject.Inject;
import view_inspector.R;
import view_inspector.ViewInspector;
import view_inspector.dagger.scope.PerActivity;

@PerActivity public class ViewInspectorScalpelLayout extends ScalpelFrameLayout {

  @Inject public ViewInspectorScalpelLayout(Context context) {
    super(context);
    setTag(getResources().getString(R.string.view_inspector_view_tag));
  }

  @Override protected void onDetachedFromWindow() {
    ViewInspector.viewRoot = null;
    super.onDetachedFromWindow();
  }
}
