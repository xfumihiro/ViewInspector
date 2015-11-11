package view_inspector.ui.menu;

import android.content.Context;
import android.widget.CompoundButton;
import android.widget.Switch;
import com.f2prateek.rx.preferences.Preference;
import javax.inject.Inject;
import view_inspector.R;
import view_inspector.ViewInspector;
import view_inspector.dagger.qualifier.ShowMargin;
import view_inspector.dagger.qualifier.ShowOutline;
import view_inspector.dagger.qualifier.ShowPadding;

public class BoundaryMenu extends BaseMenu {
  @Inject @ShowOutline Preference<Boolean> showOutline;
  @Inject @ShowMargin Preference<Boolean> showMargin;
  @Inject @ShowPadding Preference<Boolean> showPadding;

  @SuppressWarnings("ConstantConditions") public BoundaryMenu(Context context) {
    super(context);
    ViewInspector.runtimeComponentMap.get(context).inject(this);

    inflate(context, R.layout.view_inspector_boundary_menu, this);

    Switch outlineSwitch = (Switch) findViewById(R.id.outline_switch);
    outlineSwitch.setChecked(showOutline.get());
    outlineSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        showOutline.set(isChecked);
      }
    });

    Switch marginSwitch = (Switch) findViewById(R.id.margin_switch);
    marginSwitch.setChecked(showMargin.get());
    marginSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        showMargin.set(isChecked);
      }
    });

    Switch paddingSwitch = (Switch) findViewById(R.id.padding_switch);
    paddingSwitch.setChecked(showPadding.get());
    paddingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        showPadding.set(isChecked);
      }
    });
  }
}
