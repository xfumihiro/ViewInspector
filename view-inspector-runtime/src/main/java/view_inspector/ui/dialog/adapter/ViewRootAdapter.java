package view_inspector.ui.dialog.adapter;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import view_inspector.R;
import view_inspector.ViewInspector;
import view_inspector.dagger.qualifier.ViewSuspects;
import view_inspector.util.ViewUtil;

public class ViewRootAdapter extends BaseAdapter {
  private final LayoutInflater mLayoutInflater;
  private final ListView mListView;

  @Inject @ViewSuspects List<View> viewSuspects;

  private List<View> mViewGroupList = new ArrayList<>();

  public ViewRootAdapter(Context context, ListView mListView) {
    ViewInspector.runtimeComponentMap.get(((ContextThemeWrapper) context).getBaseContext())
        .inject(this);
    mLayoutInflater = LayoutInflater.from(context);

    this.mListView = mListView;

    for (View view : viewSuspects) {
      if (view instanceof ViewGroup && ViewUtil.getViewLevel(view) > 1) mViewGroupList.add(view);
    }
  }

  @Override public int getCount() {
    return mViewGroupList.size();
  }

  @Override public Object getItem(int position) {
    return mViewGroupList.get(position);
  }

  @Override public long getItemId(int position) {
    return position;
  }

  @Override public View getView(final int position, View convertView, ViewGroup parent) {
    ViewHolder viewHolder;
    if (convertView == null) {
      convertView =
          mLayoutInflater.inflate(R.layout.view_inspector_set_view_root_listitem, parent, false);
      viewHolder = new ViewHolder();
      viewHolder.viewClass = (TextView) convertView.findViewById(R.id.view_class);
      viewHolder.viewId = (TextView) convertView.findViewById(R.id.view_id);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }

    View view = mViewGroupList.get(position);
    viewHolder.viewClass.setText(ViewUtil.getClassName(view));
    viewHolder.viewId.setText(ViewUtil.getSimpleViewId(view));
    if (view.equals(ViewInspector.viewRoot)) mListView.setItemChecked(position, true);
    return convertView;
  }

  class ViewHolder {
    public TextView viewClass;
    public TextView viewId;
  }
}
