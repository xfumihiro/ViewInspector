package view_inspector.profile;

import android.database.sqlite.SQLiteOpenHelper;
import android.view.View;
import android.view.ViewDebug;
import com.f2prateek.rx.preferences.Preference;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Queue;
import javax.inject.Inject;
import view_inspector.ViewInspector;
import view_inspector.dagger.qualifier.Profiling;
import view_inspector.dagger.scope.PerActivity;
import view_inspector.database.ViewProfile;

@PerActivity public final class ProfileUtil {

  @Inject SQLiteOpenHelper db;
  @Inject @Profiling Preference<Boolean> profiling;

  @Inject public ProfileUtil() {
  }

  public interface ProfileResultListener {
    void onProfileDone();

    void onProgress(int step);
  }

  public void profile(final int samples, final ProfileResultListener listener) {
    new Thread(new Runnable() {
      @SuppressWarnings("TryWithIdenticalCatches") @Override public void run() {
        profiling.set(true);

        for (int i = 0; i < samples; i++) {
          ByteArrayOutputStream outputStream = null;
          try {
            final Method dumpMethod =
                ViewDebug.class.getDeclaredMethod("dump", View.class, boolean.class, boolean.class,
                    OutputStream.class);
            dumpMethod.setAccessible(true);
            final Method profileMethod =
                ViewDebug.class.getDeclaredMethod("profile", View.class, OutputStream.class,
                    String.class);
            profileMethod.setAccessible(true);
            View viewRootView = ViewInspector.viewRoot.getRootView();
            String parameter = viewRootView.getClass().getName() + "@" + Integer.toHexString(
                viewRootView.hashCode());
            outputStream = new ByteArrayOutputStream();
            dumpMethod.invoke(null, viewRootView, false, false, outputStream);
            profileMethod.invoke(null, viewRootView, outputStream, parameter);
          } catch (NoSuchMethodException e) {
            e.printStackTrace();
          } catch (InvocationTargetException e) {
            e.printStackTrace();
          } catch (IllegalAccessException e) {
            e.printStackTrace();
          } finally {
            if (outputStream != null) {
              String[] strings = outputStream.toString().split("\\n");
              Queue<String> hashcodeQueue = new LinkedList<>();
              boolean hashcodeStored = false;
              for (String str : strings) {
                if (!hashcodeStored && !str.equals("DONE.")) {
                  // store the hashcodes
                  hashcodeQueue.add(str.split("@")[1].replaceAll("\\s", ""));
                } else {
                  hashcodeStored = true;
                  if (!str.equals("DONE.")) {
                    // store the profiles
                    String[] profiles = str.split(" ");
                    db.getWritableDatabase()
                        .insert(ViewProfile.TABLE, null,
                            new ViewProfile.Builder().viewHashcode(hashcodeQueue.poll())
                                .measureDuration(Long.parseLong(profiles[0]) / 1000.0)
                                .layoutDuration(Long.parseLong(profiles[1]) / 1000.0)
                                .drawDuration(Long.parseLong(profiles[2]) / 1000.0)
                                .build());
                  }
                }
              }
            }
          }
          listener.onProgress(i);
        }

        profiling.set(false);
        listener.onProfileDone();
      }
    }).start();
  }
}
