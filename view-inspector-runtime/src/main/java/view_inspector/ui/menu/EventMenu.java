package view_inspector.ui.menu;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import com.f2prateek.rx.preferences.Preference;
import javax.inject.Inject;
import view_inspector.R;
import view_inspector.ViewInspector;
import view_inspector.dagger.qualifier.ProbeMeasures;
import view_inspector.dagger.qualifier.ShowMeasureCount;
import view_inspector.probe.ViewInspectorInterceptor;
import view_inspector.ui.ViewInspectorToolbar;
import view_inspector.ui.dialog.BaseDialog;
import view_inspector.ui.dialog.ProfileSettingDialog;
import view_inspector.ui.dialog.SetViewRootDialog;

public class EventMenu extends BaseMenu implements SetViewRootDialog.ViewRootSelectListener {
  private final LinearLayout mSubmenuLayout;
  private final Context mContext;
  private final Switch mProbeMeasureSwitch;

  @Inject @ProbeMeasures Preference<Boolean> probeMeasures;
  @Inject @ShowMeasureCount Preference<Boolean> showMeasureCount;
  @Inject ViewInspectorInterceptor interceptor;
  @Inject ViewInspectorToolbar toolbar;
  @Inject WindowManager windowManager;

  @SuppressWarnings("ConstantConditions") public EventMenu(final Context context) {
    super(context);
    ViewInspector.runtimeComponentMap.get(context).inject(this);

    mContext = context;
    inflate(context, R.layout.view_inspector_event_menu, this);

    mSubmenuLayout = (LinearLayout) findViewById(R.id.submenu);
    if (probeMeasures.get()) {
      showSubMenu();
    }

    mProbeMeasureSwitch = (Switch) findViewById(R.id.probe_measure_switch);
    mProbeMeasureSwitch.setChecked(probeMeasures.get());
    mProbeMeasureSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        probeMeasures.set(isChecked);
        if (isChecked) {
          showSubMenu();
        } else {
          mSubmenuLayout.setVisibility(GONE);
        }
      }
    });

    Switch showMeasureCountSwitch = (Switch) findViewById(R.id.show_measure_count);
    showMeasureCountSwitch.setChecked(showMeasureCount.get());
    showMeasureCountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        showMeasureCount.set(isChecked);
      }
    });

    LinearLayout setViewRoot = (LinearLayout) findViewById(R.id.view_root_layout);
    setViewRoot.setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        new SetViewRootDialog(
            new ContextThemeWrapper(mContext, BaseDialog.getDialogTheme(mContext)),
            EventMenu.this).show();
      }
    });

    View profilingViewTree = findViewById(R.id.profile_view_tree);
    profilingViewTree.setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        new ProfileSettingDialog(
            new ContextThemeWrapper(mContext, BaseDialog.getDialogTheme(mContext))).show();
      }
    });
  }

  @Override public void onViewRootSelected(View view) {
    interceptor.setViewRoot(view);
    mProbeMeasureSwitch.setChecked(true);
  }

  private void showSubMenu() {
    mSubmenuLayout.setVisibility(VISIBLE);
    ObjectAnimator animator = ObjectAnimator.ofFloat(mSubmenuLayout, "alpha", 0f, 1f);
    animator.setDuration(500).start();
  }
}
