package view_inspector.ui.dialog.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import com.f2prateek.rx.preferences.Preference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import view_inspector.R;
import view_inspector.ViewInspector;
import view_inspector.dagger.qualifier.ViewFilter;
import view_inspector.dagger.qualifier.ViewSuspects;
import view_inspector.util.ViewUtil;

public class ViewFilterAdapter extends BaseAdapter {
  private final LayoutInflater mLayoutInflater;
  private final ListView mListView;

  @Inject @ViewSuspects List<View> viewSuspects;
  @Inject @ViewFilter Preference<Set<String>> viewFilterSet;

  private List<String> mViewClassList = new ArrayList<>();
  private Set<String> mViewFilter;

  public ViewFilterAdapter(Context context, ListView mListView) {
    ViewInspector.runtimeComponentMap.get(((ContextThemeWrapper) context).getBaseContext())
        .inject(this);
    mLayoutInflater = LayoutInflater.from(context);

    this.mListView = mListView;

    for (View view : viewSuspects) {
      if (!mViewClassList.contains(ViewUtil.getClassName(view))) {
        mViewClassList.add(ViewUtil.getClassName(view));
      }
    }
    mViewFilter = viewFilterSet.get();

    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CheckedTextView textView = (CheckedTextView) view;
        if (textView.isChecked()) {
          textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
          mViewFilter.add(mViewClassList.get(position));
        } else {
          textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
          mViewFilter.remove(mViewClassList.get(position));
        }
      }
    });
  }

  @Override public int getCount() {
    return mViewClassList.size();
  }

  @Override public Object getItem(int position) {
    return mViewClassList.get(position);
  }

  @Override public long getItemId(int position) {
    return position;
  }

  @Override public View getView(final int position, View convertView, ViewGroup parent) {
    ViewHolder viewHolder;
    if (convertView == null) {
      convertView =
          mLayoutInflater.inflate(R.layout.view_inspector_set_view_filter_listitem, parent, false);
      viewHolder = new ViewHolder();
      viewHolder.text1 = (CheckedTextView) convertView.findViewById(R.id.text1);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }

    String viewClass = mViewClassList.get(position);
    viewHolder.text1.setText(viewClass);
    if (mViewFilter.contains(viewClass)) {
      mListView.setItemChecked(position, true);
      viewHolder.text1.setPaintFlags(
          viewHolder.text1.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    } else {
      mListView.setItemChecked(position, false);
      viewHolder.text1.setPaintFlags(
          viewHolder.text1.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
    }
    return convertView;
  }

  class ViewHolder {
    public CheckedTextView text1;
  }
}