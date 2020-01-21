package br.com.luansilveira.todolist;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.luansilveira.todolist.db.Model.Pendencia;
import br.com.luansilveira.todolist.utils.DateCalendar;

public class ListPendenciasAdapter extends ArrayAdapter<Pendencia> {

    private List<Pendencia> listSelect = new ArrayList<>();
    private List<Pendencia> listFiter = new ArrayList<>();
    private boolean[] selection;
    private boolean actionMode = false;

    public ListPendenciasAdapter(@NonNull Context context, @NonNull List<Pendencia> objects) {
        super(context, 0, objects);
        resetSelection();
    }

    public void resetSelection() {
        this.selection = new boolean[getCount()];
        this.listSelect.clear();
    }

    public ListPendenciasAdapter setActionMode(boolean actionMode) {
        this.actionMode = actionMode;
        if (!actionMode) resetSelection();
        return this;
    }

    public void setSelection(int position, boolean selected) {
        this.selection[position] = selected;
        Pendencia p = getItem(position);
        if (selected) listSelect.add(p);
        else listSelect.remove(p);
        notifyDataSetChanged();
    }

    public void selecionarTudo() {
        for (int i = 0; i < getCount(); i++) {
            selection[i] = true;
            listSelect.add(getItem(i));
        }

        notifyDataSetChanged();
    }


    public int getCountSelected() {
        return this.listSelect.size();
    }

    public List<Pendencia> getSelectedItems() {
        return this.listSelect;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_list_pendencias, parent, false);
        }

        CheckBox checkBox = convertView.findViewById(R.id.checkBox);
        checkBox.setVisibility(this.actionMode ? View.VISIBLE : View.GONE);
        if (actionMode) checkBox.setChecked(selection[position]);

        TextView txtTitulo = convertView.findViewById(R.id.txtTitulo);
        TextView txtDescricao = convertView.findViewById(R.id.txtDescricao);
        TextView txtDataHora = convertView.findViewById(R.id.txtDataHora);
        TextView txtLembrete = convertView.findViewById(R.id.txtLembrete);
        ImageView imgSync = convertView.findViewById(R.id.imgSync);
        View layoutLembrete = convertView.findViewById(R.id.layoutLembrete);

        Pendencia pendencia = getItem(position);
        if (pendencia != null) {
            txtTitulo.setText(pendencia.getTitulo());
            txtDescricao.setText(pendencia.getDescricao());
            txtDataHora.setText(getTextFromDate(pendencia.getDataHora()));
            if (pendencia.hasLembrete()) {
                layoutLembrete.setVisibility(View.VISIBLE);
                txtLembrete.setText(getTextFromDate(pendencia.getDataLembrete()));
            } else {
                layoutLembrete.setVisibility(View.INVISIBLE);
            }

            imgSync.setImageResource(pendencia.isSync() ? R.drawable.ic_done : R.drawable.ic_sync_wait);
        }

        return convertView;
    }

    private String getTextFromDate(Date date) {
        if (date == null) return null;
        DateCalendar dateCalendar = DateCalendar.fromDate(date);
        String format = (DateFormat.is24HourFormat(getContext()) ? "HH:mm" : "h:mm a");
        if (!dateCalendar.isToday()) {
            format = " 'às' " + format;
            if (dateCalendar.isYesterday()) {
                format = "'ontem'" + format;
            } else {
                format = (dateCalendar.isCurrentWeek() ? "EEEE" : "d MMM" + (dateCalendar.isCurrentYear() ? "" : " yyyy")) + format;
            }
        }

        return new SimpleDateFormat(format, new Locale("pt", "BR")).format(date);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (!this.actionMode) this.selection = new boolean[getCount()];
    }


}
