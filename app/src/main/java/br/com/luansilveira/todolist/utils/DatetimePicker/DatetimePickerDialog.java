package br.com.luansilveira.todolist.utils.DatetimePicker;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import br.com.luansilveira.todolist.R;
import br.com.luansilveira.todolist.utils.DateCalendar;

public class DatetimePickerDialog extends Dialog implements View.OnClickListener {

    private TextView txtData;
    private TextView txtHora;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private DateCalendar dateCalendar;

    private OnDateConfirmListener listener;

    public DatetimePickerDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_datetime_picker);

        TabHost tabHost = findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec spec1 = tabHost.newTabSpec("tab1");
        spec1.setContent(R.id.tab1);
        spec1.setIndicator("Data");
        tabHost.addTab(spec1);

        TabHost.TabSpec spec2 = tabHost.newTabSpec("tab2");
        spec2.setContent(R.id.tab2);
        spec2.setIndicator("Horário");
        tabHost.addTab(spec2);


        this.dateCalendar = DateCalendar.now();
        this.txtData = findViewById(R.id.txtData);
        this.txtHora = findViewById(R.id.txtHora);
        setTextDataHora();

        DatePicker datePicker = findViewById(R.id.datePicker);
        datePicker.setMinDate(this.dateCalendar.getTimeMillis());
        datePicker.init(dateCalendar.getYear(), dateCalendar.getMonth(), dateCalendar.getDay(), (view, year, monthOfYear, dayOfMonth) -> {
            this.dateCalendar.setYear(year).setMonth(monthOfYear).setDay(dayOfMonth);
            setTextDataHora();
        });

        TimePicker timePicker = findViewById(R.id.timePicker);
        timePicker.setIs24HourView(DateFormat.is24HourFormat(getContext()));
        timePicker.setCurrentHour(dateCalendar.getHour());
        timePicker.setCurrentMinute(dateCalendar.getMinute());
        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            this.dateCalendar.setHour(hourOfDay).setMinute(minute);
            setTextDataHora();
        });

        Button btSalvar = findViewById(R.id.btSalvar);
        Button btCancelar = findViewById(R.id.btCancelar);
        btCancelar.setOnClickListener(v -> dismiss());
        btSalvar.setOnClickListener(this);
    }

    private void setTextDataHora() {
        this.txtData.setText(this.dateFormat.format(this.dateCalendar.getDate()));
        this.txtHora.setText(this.timeFormat.format(this.dateCalendar.getDate()));
    }

    public DatetimePickerDialog setOnConfirmDateListener(OnDateConfirmListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public void show() {
        super.show();
        this.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onClick(View v) {
        if (this.listener != null) this.listener.onDateConfirm(this.dateCalendar.getDate());
        dismiss();
    }

    public interface OnDateConfirmListener {
        void onDateConfirm(Date date);
    }
}
