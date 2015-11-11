package view_inspector.ui.menu;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import com.f2prateek.rx.preferences.Preference;
import javax.inject.Inject;
import view_inspector.R;
import view_inspector.ViewInspector;
import view_inspector.dagger.qualifier.Scalpel3D;
import view_inspector.dagger.qualifier.ScalpelShowId;
import view_inspector.dagger.qualifier.ScalpelWireframe;

public class LayerMenu extends BaseMenu {
  private final LinearLayout mSubmenuLayout;

  @Inject @Scalpel3D Preference<Boolean> scalpel3d;
  @Inject @ScalpelWireframe Preference<Boolean> scalpelWireframe;
  @Inject @ScalpelShowId Preference<Boolean> scalpelId;

  @SuppressWarnings("ConstantConditions") public LayerMenu(Context context) {
    super(context);
    ViewInspector.runtimeComponentMap.get(context).inject(this);

    inflate(context, R.layout.view_inspector_layer_menu, this);

    mSubmenuLayout = (LinearLayout) findViewById(R.id.submenu);
    if (scalpel3d.get()) {
      showSubMenu();
    }

    Switch scalpel3dSwitch = (Switch) findViewById(R.id.scalpel_3d_switch);
    scalpel3dSwitch.setChecked(scalpel3d.get());
    scalpel3dSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        scalpel3d.set(isChecked);
        if (isChecked) {
          showSubMenu();
        } else {
          mSubmenuLayout.setVisibility(GONE);
        }
      }
    });

    Switch scalpelWireframeSwitch = (Switch) findViewById(R.id.scalpel_wireframe_switch);
    scalpelWireframeSwitch.setChecked(scalpelWireframe.get());
    scalpelWireframeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        scalpelWireframe.set(isChecked);
      }
    });

    Switch scalpelIdSwitch = (Switch) findViewById(R.id.scalpel_id_switch);
    scalpelIdSwitch.setChecked(scalpelId.get());
    scalpelIdSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        scalpelId.set(isChecked);
      }
    });
  }

  private void showSubMenu() {
    mSubmenuLayout.setVisibility(VISIBLE);
    ObjectAnimator animator = ObjectAnimator.ofFloat(mSubmenuLayout, "alpha", 0f, 1f);
    animator.setDuration(200).start();
  }
}
