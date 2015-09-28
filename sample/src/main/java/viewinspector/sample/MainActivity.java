package viewinspector.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    View view4 = findViewById(R.id.view4);
    view4.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Toast.makeText(MainActivity.this, "clicked!", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override protected void onResume() {
    super.onResume();
  }

  @Override protected void onPause() {
    super.onPause();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
  }
}
