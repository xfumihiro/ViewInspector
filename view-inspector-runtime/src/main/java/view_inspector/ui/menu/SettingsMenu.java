package view_inspector.ui.menu;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import com.f2prateek.rx.preferences.Preference;
import javax.inject.Inject;
import view_inspector.R;
import view_inspector.ViewInspector;
import view_inspector.dagger.qualifier.LogViewEvents;
import view_inspector.probe.ViewInspectorInterceptor;
import view_inspector.ui.dialog.BaseDialog;
import view_inspector.ui.dialog.SetViewFilterDialog;

public class SettingsMenu extends BaseMenu {
  @Inject @LogViewEvents Preference<Boolean> logViewEvents;
  @Inject WindowManager windowManager;
  @Inject ViewInspectorInterceptor viewInspectorInterceptor;

  public SettingsMenu(final Context context) {
    super(context);
    ViewInspector.runtimeComponentMap.get(context).inject(this);

    inflate(context, R.layout.view_inspector_settings_menu, this);

    Switch logViewEventsSwitch = (Switch) findViewById(R.id.log_view_events_switch);
    logViewEventsSwitch.setChecked(logViewEvents.get());
    logViewEventsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        logViewEvents.set(isChecked);
      }
    });

    View viewFilter = findViewById(R.id.view_filter);
    viewFilter.setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        new SetViewFilterDialog(
            new ContextThemeWrapper(context, BaseDialog.getDialogTheme(context))).show();
      }
    });
  }
}
