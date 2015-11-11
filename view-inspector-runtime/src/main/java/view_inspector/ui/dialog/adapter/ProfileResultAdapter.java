package view_inspector.ui.dialog.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;
import javax.inject.Inject;
import view_inspector.R;
import view_inspector.ViewInspector;
import view_inspector.dagger.qualifier.ViewSuspects;
import view_inspector.database.ViewProfile;
import view_inspector.util.ViewUtil;

public class ProfileResultAdapter extends BaseAdapter {
  private final LayoutInflater mLayoutInflater;
  private final double mAvgMeasure;
  private final double mAvgLayout;
  private final double mAvgDraw;

  @Inject @ViewSuspects List<View> viewSuspects;
  @Inject SQLiteOpenHelper db;

  public ProfileResultAdapter(Context context) {
    ViewInspector.runtimeComponentMap.get(((ContextThemeWrapper) context).getBaseContext())
        .inject(this);
    mLayoutInflater = LayoutInflater.from(context);

    String avgsql =
        "select avg(" + ViewProfile.MEASURE_DURATION + "), " + "avg(" + ViewProfile.LAYOUT_DURATION
            + "), " + "avg(" + ViewProfile.DRAW_DURATION + ") " + "from " + ViewProfile.TABLE;
    Cursor cursor = db.getReadableDatabase().rawQuery(avgsql, null);
    cursor.moveToNext();
    mAvgMeasure = cursor.getLong(0) / 1000.0;
    mAvgLayout = cursor.getLong(1) / 1000.0;
    mAvgDraw = cursor.getLong(2) / 1000.0;
    cursor.close();
  }

  @Override public int getCount() {
    return viewSuspects.size();
  }

  @Override public Object getItem(int position) {
    return null;
  }

  @Override public long getItemId(int position) {
    return 0;
  }

  @Override public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder viewHolder;
    if (convertView == null) {
      convertView =
          mLayoutInflater.inflate(R.layout.view_inspector_profile_result_listitem, parent, false);
      viewHolder = new ViewHolder();
      viewHolder.viewClass = (TextView) convertView.findViewById(R.id.view_class);
      viewHolder.viewId = (TextView) convertView.findViewById(R.id.view_id);
      viewHolder.measureTime = (TextView) convertView.findViewById(R.id.column1);
      viewHolder.layoutTime = (TextView) convertView.findViewById(R.id.column2);
      viewHolder.drawTime = (TextView) convertView.findViewById(R.id.column3);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }

    View view = viewSuspects.get(position);
    viewHolder.viewClass.setText(ViewUtil.getClassName(view));
    viewHolder.viewId.setText(ViewUtil.getSimpleViewId(view));

    String sql =
        "select avg(" + ViewProfile.MEASURE_DURATION + "), " + "avg(" + ViewProfile.LAYOUT_DURATION
            + "), " + "avg(" + ViewProfile.DRAW_DURATION + ") " + " from " + ViewProfile.TABLE
            + " where " + ViewProfile.VIEW_HASHCODE + " = ?";
    Cursor cursor = db.getReadableDatabase()
        .rawQuery(sql, new String[] { Integer.toHexString(view.hashCode()) });
    if (cursor.moveToNext()) {
      double measure = cursor.getInt(0) / 1000.0;
      double layout = cursor.getInt(1) / 1000.0;
      double draw = cursor.getInt(2) / 1000.0;

      viewHolder.measureTime.setText(String.format("%4.2f", measure));
      viewHolder.layoutTime.setText(String.format("%4.2f", layout));
      viewHolder.drawTime.setText(String.format("%4.2f", draw));

      if (measure > mAvgMeasure) {
        viewHolder.measureTime.setBackgroundColor(Color.RED);
      } else if (measure == mAvgMeasure) {
        viewHolder.measureTime.setBackgroundColor(Color.YELLOW);
      } else {
        viewHolder.measureTime.setBackgroundColor(Color.TRANSPARENT);
      }

      if (layout > mAvgLayout) {
        viewHolder.layoutTime.setBackgroundColor(Color.RED);
      } else if (layout == mAvgLayout) {
        viewHolder.layoutTime.setBackgroundColor(Color.YELLOW);
      } else {
        viewHolder.layoutTime.setBackgroundColor(Color.TRANSPARENT);
      }

      if (draw > mAvgDraw) {
        viewHolder.drawTime.setBackgroundColor(Color.RED);
      } else if (draw == mAvgDraw) {
        viewHolder.drawTime.setBackgroundColor(Color.YELLOW);
      } else {
        viewHolder.drawTime.setBackgroundColor(Color.TRANSPARENT);
      }
    }
    cursor.close();

    return convertView;
  }

  class ViewHolder {
    TextView viewClass;
    TextView viewId;
    TextView measureTime;
    TextView layoutTime;
    TextView drawTime;
  }
}
