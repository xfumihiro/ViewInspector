package view_inspector.dagger;

import dagger.Component;
import view_inspector.ViewInspector;
import view_inspector.dagger.scope.PerActivity;
import view_inspector.probe.ViewInspectorInterceptor;
import view_inspector.ui.ViewInspectorToolbar;
import view_inspector.ui.dialog.BaseDialog;
import view_inspector.ui.dialog.ProfileResultDialog;
import view_inspector.ui.dialog.ProfileSettingDialog;
import view_inspector.ui.dialog.SetViewFilterDialog;
import view_inspector.ui.dialog.adapter.ProfileResultAdapter;
import view_inspector.ui.dialog.adapter.ViewFilterAdapter;
import view_inspector.ui.dialog.adapter.ViewRootAdapter;
import view_inspector.ui.menu.BaseMenu;
import view_inspector.ui.menu.BoundaryMenu;
import view_inspector.ui.menu.EventMenu;
import view_inspector.ui.menu.LayerMenu;
import view_inspector.ui.menu.SettingsMenu;

@PerActivity @Component(dependencies = ApplicationComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {

  void inject(ViewInspector viewInspector);

  void inject(ViewInspectorInterceptor viewInspectorInterceptor);

  void inject(ViewInspectorToolbar viewInspectorToolbar);

  void inject(BaseMenu baseMenu);

  void inject(BoundaryMenu boundaryMenu);

  void inject(LayerMenu layerMenu);

  void inject(EventMenu eventMenu);

  void inject(SettingsMenu settingsMenu);

  void inject(BaseDialog baseDialog);

  void inject(ViewRootAdapter viewRootAdapter);

  void inject(ProfileResultAdapter profileResultAdapter);

  void inject(ProfileResultDialog profileResultDialog);

  void inject(ProfileSettingDialog profileSettingDialog);

  void inject(ViewFilterAdapter viewFilterAdapter);

  void inject(SetViewFilterDialog setViewFilterDialog);
}
