package zfleming.solarispm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AddSubtaskPopup extends Activity{
    EditText subtaskNameTxt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addsubtaskpopupactivity);
        subtaskNameTxt = (EditText) findViewById(R.id.subtaskNameTxt);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = (int)(dm.widthPixels*.8), height = (int)(dm.heightPixels*.8);
        getWindow().setLayout(width,height);
    }

    public void submitSubtaskName(View view) {
        String name = subtaskNameTxt.getText().toString();
        if (!name.isEmpty()) {
            SubTask s = new SubTask(name);
            Intent intent = new Intent();
            intent.putExtra("New Subtask", (Parcelable) s);
            setResult(RESULT_OK,intent);
            Toast.makeText(AddSubtaskPopup.this, "Subtask has been added.", Toast.LENGTH_LONG).show();
            finish();

        } else {
            Toast.makeText(AddSubtaskPopup.this, "Don't leave field blank", Toast.LENGTH_LONG).show();

        }
    }
}
