package view_inspector.database;

import android.content.ContentValues;
import android.database.Cursor;
import auto.parcel.AutoParcel;
import rx.functions.Func1;

@AutoParcel public abstract class ViewProfile {
  public static final String TABLE = "view_profile";

  public static final String ID = "_id";
  public static final String VIEW_HASHCODE = "view_hashcode";
  public static final String MEASURE_DURATION = "measure_duration";
  public static final String LAYOUT_DURATION = "layout_duration";
  public static final String DRAW_DURATION = "draw_duration";

  public abstract long id();

  public abstract String viewHashcode();

  public abstract long measureDuration();

  public abstract long layoutDuration();

  public abstract long drawDuration();

  public static final class Builder {
    private final ContentValues values = new ContentValues();

    public Builder id(long id) {
      values.put(ID, id);
      return this;
    }

    public Builder viewHashcode(String viewHashcode) {
      values.put(VIEW_HASHCODE, viewHashcode);
      return this;
    }

    public Builder measureDuration(double measureDuration) {
      values.put(MEASURE_DURATION, measureDuration);
      return this;
    }

    public Builder layoutDuration(double layoutDuration) {
      values.put(LAYOUT_DURATION, layoutDuration);
      return this;
    }

    public Builder drawDuration(double drawDuration) {
      values.put(DRAW_DURATION, drawDuration);
      return this;
    }

    public ContentValues build() {
      return values;
    }
  }
}
