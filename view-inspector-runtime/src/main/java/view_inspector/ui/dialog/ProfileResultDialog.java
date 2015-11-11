package view_inspector.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ListView;
import javax.inject.Inject;
import view_inspector.R;
import view_inspector.ViewInspector;
import view_inspector.database.ViewProfile;
import view_inspector.ui.dialog.adapter.ProfileResultAdapter;

public class ProfileResultDialog extends BaseDialog {
  private final Context mContext;
  @Inject SQLiteOpenHelper db;

  public ProfileResultDialog(Context context, int samples) {
    super(context);
    ViewInspector.runtimeComponentMap.get(((ContextThemeWrapper) context).getBaseContext())
        .inject(this);

    mContext = context;

    setView(View.inflate(context, R.layout.view_inspector_profile_result, null));

    setTitle(samples > 1 ? "Profile Results (" + samples + " samples)" : "Profile Results");

    setButton(BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        restoreOpenedMenu();
      }
    });
  }

  @Override public void onAttachedToWindow() {
    super.onAttachedToWindow();

    ListView listView = (ListView) findViewById(R.id.listview);
    listView.setAdapter(new ProfileResultAdapter(mContext));
  }

  @Override public void onDetachedFromWindow() {
    // clear database
    db.getWritableDatabase().delete(ViewProfile.TABLE, null, null);

    super.onDetachedFromWindow();
  }

  @Override public void onBackPressed() {
    super.onBackPressed();
    restoreOpenedMenu();
  }
}
