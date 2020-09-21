package zfleming.solarispm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SubtaskPopup extends Activity{
    private TextView subtaskName;
    private SubTask mainSubtask;
    private CheckBox yesBox, noBox;
    private Button submit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subtaskpopupactivity);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = (int)(dm.widthPixels*.8), height = (int)(dm.heightPixels*.8);
        getWindow().setLayout(width,height);
        subtaskName = (TextView)findViewById(R.id.subtaskNameTxt);
        yesBox = (CheckBox) findViewById(R.id.yesBox);
        noBox = (CheckBox)findViewById(R.id.noBox);
        submit = (Button) findViewById(R.id.submitResponseBttn);
        mainSubtask = getIntent().getParcelableExtra("SubTask");
        String result = getIntent().getStringExtra("Result");
        if (result.equalsIgnoreCase("complete")) {
            mainSubtask.setSubTaskComplete(true);
            yesBox.setChecked(true);
        } else {
            mainSubtask.setSubTaskComplete(false);
            noBox.setChecked(true);
        }
        subtaskName.setText(mainSubtask.getSubTaskName());
    }

    public void submitResponse(View view) {
        String result = "";
        boolean[] check = {yesBox.isChecked(),noBox.isChecked()};
        if (check[0] != check[1]) {
            if (yesBox.isChecked()) {
                mainSubtask.setSubTaskComplete(true);
                result+="Complete";
            } else if (noBox.isChecked()) {
                mainSubtask.setSubTaskComplete(false);
                result+="Incomplete";
            }
            Intent intent = new Intent();
            intent.putExtra("Subtask", (Parcelable) mainSubtask);
            intent.putExtra("Result", result);
            setResult(RESULT_OK,intent);
            finish();
        } else {
            Toast.makeText(SubtaskPopup.this, "Both can't be checked.", Toast.LENGTH_LONG).show();

        }

    }
}
