package view_inspector.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import view_inspector.ui.dialog.adapter.ViewRootAdapter;

public class SetViewRootDialog extends BaseDialog {
  private ViewRootSelectListener listener;

  public interface ViewRootSelectListener {
    void onViewRootSelected(View view);
  }

  public SetViewRootDialog(Context context, final ViewRootSelectListener listener) {
    super(context);
    this.listener = listener;

    final ListView listView = new ListView(context);
    listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
    listView.setAdapter(new ViewRootAdapter(context, listView));

    setTitle("Set View Root");
    setView(listView);
    setButton(BUTTON_NEGATIVE, "Cancel", new OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
      }
    });

    setButton(BUTTON_POSITIVE, "Ok", new OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        listener.onViewRootSelected(
            (View) listView.getItemAtPosition(listView.getCheckedItemPosition()));
      }
    });
  }
}
