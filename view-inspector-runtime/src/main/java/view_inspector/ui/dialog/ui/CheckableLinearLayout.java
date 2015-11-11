package view_inspector.ui.dialog.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import view_inspector.R;

public class CheckableLinearLayout extends LinearLayout implements Checkable {
  private final CheckedTextView mCheckedTextView;

  public CheckableLinearLayout(Context context) {
    super(context);
    inflate(context, R.layout.view_inspector_checkable_linear_layout, this);
    this.mCheckedTextView = (CheckedTextView) findViewById(R.id.checked_textview);
  }

  public CheckableLinearLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    inflate(context, R.layout.view_inspector_checkable_linear_layout, this);
    this.mCheckedTextView = (CheckedTextView) findViewById(R.id.checked_textview);
  }

  public CheckableLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    inflate(context, R.layout.view_inspector_checkable_linear_layout, this);

    this.mCheckedTextView = (CheckedTextView) findViewById(R.id.checked_textview);
  }

  @Override public void setChecked(boolean checked) {
    mCheckedTextView.setChecked(checked);
  }

  @Override public boolean isChecked() {
    return mCheckedTextView.isChecked();
  }

  @Override public void toggle() {
    mCheckedTextView.toggle();
  }
}
