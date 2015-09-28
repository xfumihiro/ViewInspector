package view_inspector.probe.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.view.View;
import android.view.ViewGroup;

public final class DrawUtil {
  private static final int VIEW_COLOR = 0xFF3366FF;
  private static final int CONTAINER_COLOR = 0xFF2AFF80;
  private static final int MARGIN_COLOR = 0x88FFAAAA;
  private static final int PADDING_COLOR = 0x883366FF;

  private static final float BORDER_WIDTH_DP = 2.5f;
  private static final float BORDER_SIZE_RATIO = 0.2f;
  private static final float MAX_BORDER_SIZE_DP = 15;

  private static final int NO_OVERMEASURE = 0xFF999999;
  private static final int OVERMEASURE_1x = 0xFFAAAAFF;
  private static final int OVERMEASURE_2x = 0xFF2AFF80;
  private static final int OVERMEASURE_3x = 0xFFFFAAAA;
  private static final int OVERMEASURE_4x = 0xFFFF0000;

  private static final int TEXT_OFFSET_DP = 2;
  private static final int TEXT_SIZE_DP = 10;

  public static void drawOutline(Context context, View view, Canvas canvas) {
    int width = view.getWidth();
    int height = view.getHeight();
    final int color;
    if (view instanceof ViewGroup) {
      color = CONTAINER_COLOR;
    } else {
      color = VIEW_COLOR;
    }

    final float density = context.getResources().getDisplayMetrics().density;
    float borderWidth = BORDER_WIDTH_DP * density;
    float maxBorderSize = MAX_BORDER_SIZE_DP * density;

    Paint borderPaint = new Paint();
    borderPaint.setStrokeWidth(borderWidth);
    borderPaint.setColor(color);

    final float lineWidth = Math.min(maxBorderSize, width * BORDER_SIZE_RATIO);
    final float lineHeight = Math.min(maxBorderSize, height * BORDER_SIZE_RATIO);

    canvas.drawLine(0, 0, lineWidth, 0, borderPaint);
    canvas.drawLine(0, 0, 0, lineHeight, borderPaint);

    canvas.drawLine(0, height, 0, height - lineHeight, borderPaint);
    canvas.drawLine(0, height, lineWidth, height, borderPaint);

    canvas.drawLine(width, 0, width, lineHeight, borderPaint);
    canvas.drawLine(width, 0, width - lineWidth, 0, borderPaint);

    canvas.drawLine(width, height, width, height - lineHeight, borderPaint);
    canvas.drawLine(width, height, width - lineWidth, height, borderPaint);
  }

  public static void drawMargin(View view, Canvas canvas) {
    if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
      int width = view.getWidth();
      int height = view.getHeight();
      ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

      int lMargin = mlp.leftMargin;
      int tMargin = mlp.topMargin;
      int rMargin = mlp.rightMargin;
      int bMargin = mlp.bottomMargin;

      // extend canvas
      Rect newRect = canvas.getClipBounds();
      int dx = -Math.max(mlp.leftMargin, mlp.rightMargin);
      int dy = -Math.max(mlp.topMargin, mlp.bottomMargin);
      newRect.inset(dx, dy);
      canvas.clipRect(newRect, Region.Op.REPLACE);

      Rect lRect = new Rect(-lMargin, 0, 0, height);
      Rect ltRect = new Rect(-lMargin, -tMargin, 0, 0);
      Rect tRect = new Rect(0, -tMargin, width, 0);
      Rect trRect = new Rect(width, -tMargin, width + rMargin, 0);
      Rect rRect = new Rect(width, 0, width + rMargin, height);
      Rect rbRect = new Rect(width, height, width + rMargin, height + bMargin);
      Rect bRect = new Rect(0, height, width, height + bMargin);
      Rect blRect = new Rect(-lMargin, height, 0, height + bMargin);
      Paint paint = new Paint();
      paint.setColor(MARGIN_COLOR);
      canvas.drawRect(lRect, paint);
      canvas.drawRect(ltRect, paint);
      canvas.drawRect(tRect, paint);
      canvas.drawRect(trRect, paint);
      canvas.drawRect(rRect, paint);
      canvas.drawRect(rbRect, paint);
      canvas.drawRect(bRect, paint);
      canvas.drawRect(blRect, paint);
    }
  }

  public static void drawPadding(View view, Canvas canvas) {
    int width = view.getWidth();
    int height = view.getHeight();
    int lPad = view.getPaddingLeft();
    int tPad = view.getPaddingTop();
    int rPad = view.getPaddingRight();
    int bPad = view.getPaddingBottom();

    Rect lRect = new Rect(0, 0, lPad, height);
    Rect tRect = new Rect(lPad, 0, width - rPad, tPad);
    Rect rRect = new Rect(width - rPad, 0, width, height);
    Rect bRect = new Rect(lPad, height - bPad, width - rPad, height);
    Paint paint = new Paint();
    paint.setColor(PADDING_COLOR);
    canvas.drawRect(lRect, paint);
    canvas.drawRect(tRect, paint);
    canvas.drawRect(rRect, paint);
    canvas.drawRect(bRect, paint);
  }

  public static void drawMeasures(View view, Canvas canvas, Integer measureCount) {

    final int color;
    switch (measureCount) {
      case 0:
      case 1:
        color = NO_OVERMEASURE;
        break;

      case 2:
        color = OVERMEASURE_1x;
        break;

      case 3:
        color = OVERMEASURE_2x;
        break;

      case 4:
        color = OVERMEASURE_3x;
        break;

      default:
        color = OVERMEASURE_4x;
        break;
    }

    if (measureCount > 1) {
      final int tintColor =
          Color.argb(150, Color.red(color), Color.green(color), Color.blue(color));
      Rect rect = new Rect();
      view.getDrawingRect(rect);
      Paint paint = new Paint();
      paint.setColor(tintColor);
      canvas.drawRect(rect, paint);
    }
  }

  public static void drawMeasureCount(Context context, View view, Canvas canvas,
      Integer measureCount) {
    final float density = context.getResources().getDisplayMetrics().density;
    final float textSize = TEXT_SIZE_DP * density;
    final float textOffset = TEXT_OFFSET_DP * density;

    Paint textPaint = new Paint();
    textPaint.setColor(0xFF888888);
    textPaint.setTextSize(textSize);
    textPaint.setShadowLayer(1, -1, 1, 0xFF000000);
    String measureStr = "measure: " + measureCount + "x";
    canvas.drawText(measureStr, textOffset, view.getHeight() - textOffset, textPaint);
  }

  private DrawUtil() {
    throw new AssertionError("No instances.");
  }
}
