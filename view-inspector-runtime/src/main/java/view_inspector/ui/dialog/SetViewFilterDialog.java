package view_inspector.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import android.widget.AbsListView;
import android.widget.ListView;
import javax.inject.Inject;
import view_inspector.ViewInspector;
import view_inspector.probe.ViewInspectorInterceptor;
import view_inspector.ui.dialog.adapter.ViewFilterAdapter;

public class SetViewFilterDialog extends BaseDialog {
  @Inject ViewInspectorInterceptor interceptor;

  public SetViewFilterDialog(Context context) {
    super(context);
    ViewInspector.runtimeComponentMap.get(((ContextThemeWrapper) context).getBaseContext())
        .inject(this);

    setCanceledOnTouchOutside(false);
    final ListView listView = new ListView(context);
    listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
    listView.setAdapter(new ViewFilterAdapter(context, listView));

    setTitle("Set View Filter");
    setView(listView);

    setButton(BUTTON_POSITIVE, "Ok", new OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        interceptor.invalidateScalpelAndSuspects();
        restoreOpenedMenu();
      }
    });
  }

  @Override public void onBackPressed() {
    super.onBackPressed();
    interceptor.invalidateScalpelAndSuspects();
  }
}
