package view_inspector.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import javax.inject.Inject;
import view_inspector.R;
import view_inspector.ViewInspector;
import view_inspector.profile.ProfileUtil;
import view_inspector.ui.ProfileProgressbar;

public class ProfileSettingDialog extends BaseDialog {
  private static final int SAMPLE_MIN_VAL = 1;
  private static final int SAMPLE_MAX_VAL = 100;
  private static final int SAMPLE_STEP_SIZE = 10;

  @Inject ProfileUtil profileUtil;
  @Inject ProfileProgressbar profileProgressbar;

  private int mProfileSample;

  public ProfileSettingDialog(Context context) {
    super(context);
    ViewInspector.runtimeComponentMap.get(((ContextThemeWrapper) context).getBaseContext())
        .inject(this);

    setTitle("Profile Setting");

    setView(View.inflate(context, R.layout.view_inspector_profile_setting, null));

    setButton(BUTTON_NEGATIVE, "Cancel", new OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        restoreOpenedMenu();
      }
    });

    setButton(BUTTON_POSITIVE, "Ok", new OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        toolbar.showProgressbar();
        profileUtil.profile(mProfileSample, toolbar);
        profileProgressbar.setSamples(mProfileSample);
        mMenu = null;
        dismiss();
        toolbar.toggleToolbar();
      }
    });
  }

  @Override public void onAttachedToWindow() {
    super.onAttachedToWindow();

    SeekBar seekBar = (SeekBar) findViewById(R.id.seekbar);
    seekBar.setMax(SAMPLE_MAX_VAL);
    seekBar.setProgress(profileProgressbar.getSamples());
    final TextView sampleTextView = (TextView) findViewById(R.id.sample);
    sampleTextView.setText(String.valueOf(profileProgressbar.getSamples()));
    mProfileSample = profileProgressbar.getSamples();

    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mProfileSample = progress / SAMPLE_STEP_SIZE * SAMPLE_STEP_SIZE;
        if (mProfileSample == 0) mProfileSample = SAMPLE_MIN_VAL;
        seekBar.setProgress(mProfileSample);
        sampleTextView.setText(String.valueOf(mProfileSample));
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });
  }

  @Override protected void onStop() {
  }
}
