package br.com.luansilveira.todolist;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.luansilveira.todolist.db.Model.Pendencia;
import br.com.luansilveira.todolist.utils.DateCalendar;

public class ListPendenciasAdapter2 extends BaseAdapter {

    private static Locale LOCALE_BR = new Locale("pt", "BR");

    private Context context;


    /**
     * Lista de objetos que incui as pendências não excluídas e os separadores de grupo.
     * Esta lista é a que será mostrada no ListView.
     */
    private List<Object> listItens = new ArrayList<>();


    /**
     * Lista de TODAS as pendências
     */
    private List<Pendencia> listPendencias;


    /**
     * Lista de pendências selecionadas para exclusão (marcadas com checkbox)
     */
    private List<Pendencia> listSelect = new ArrayList<>();

    /**
     * Array de seleção booleana p/ modo de seleção (actionMode)
     */
    private boolean[] selection;


    /**
     * Define se a tela está em modo de seleção
     */
    private boolean actionMode = false;

    public ListPendenciasAdapter2(Context context, List<Pendencia> listPendencias) {
        this.context = context;
        this.listPendencias = listPendencias;
        this.ordenarListaPorData();
        this.adicionarSeparadores();
        this.resetSelection();
    }


    public void resetSelection() {
        this.selection = new boolean[getCount()];
        this.listSelect.clear();
    }

    private void ordenarListaPorData() {
        Collections.sort(this.listPendencias, (o1, o2) -> o2.getDataHora().compareTo(o1.getDataHora()));
    }

    public ListPendenciasAdapter2 setActionMode(boolean actionMode) {
        this.actionMode = actionMode;
        if (!actionMode) resetSelection();
        return this;
    }

    public void setSelection(int position, boolean selected) {
        this.selection[position] = selected;
        Pendencia p = (Pendencia) getItem(position);
        if (selected) listSelect.add(p);
        else listSelect.remove(p);
        notifyDataSetChanged();
    }

    public void selecionarTudo() {
        for (int i = 0; i < getCount(); i++) {
            Object item = getItem(i);
            if (item instanceof Pendencia) {
                selection[i] = true;
                listSelect.add((Pendencia) getItem(i));
            }
        }

        notifyDataSetChanged();
    }


    private void adicionarSeparadores() {
        this.listItens.clear();

        int mes, mesAnterior, ano, anoAnterior;
        DateCalendar today = DateCalendar.today();
        mesAnterior = today.getMonth();
        anoAnterior = today.getYear();

        if (this.listPendencias.size() > 0) {
            this.listItens.add(new ListSeparator(today.getTime()));
            for (Pendencia p : this.listPendencias) {
                if (p.isDeleted()) continue;

                DateCalendar calendar = DateCalendar.fromDate(p.getDataHora());
                mes = calendar.getMonth();
                ano = calendar.getYear();

                if ((mes != mesAnterior) || (ano != anoAnterior)) {
                    this.listItens.add(new ListSeparator(p.getDataHora()));
                    mesAnterior = mes;
                    anoAnterior = ano;
                }

                this.listItens.add(p);
            }
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Object item = getItem(position);

        if (item instanceof ListSeparator) {
            convertView = LayoutInflater.from(this.context).inflate(R.layout.layout_list_pendencias_separator, parent, false);

            TextView txtData = convertView.findViewById(R.id.txtData);
            txtData.setText(((ListSeparator) item).getDescricao());
        } else {
            if (convertView == null || convertView.getId() == R.id.layoutSeparator) {
                convertView = LayoutInflater.from(this.context).inflate(R.layout.layout_list_pendencias, parent, false);
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

            Pendencia pendencia = (Pendencia) item;
            if (pendencia != null) {
                txtTitulo.setText(pendencia.getTitulo());
                String descricao = pendencia.getDescricao();
                if (descricao == null || descricao.trim().isEmpty()) {
                    txtDescricao.setVisibility(View.GONE);
                } else {
                    txtDescricao.setVisibility(View.VISIBLE);
                    txtDescricao.setText(descricao);
                }
                txtDataHora.setText(getTextFromDate(pendencia.getDataHora()));
                if (pendencia.hasLembrete()) {
                    layoutLembrete.setVisibility(View.VISIBLE);
                    txtLembrete.setText(getTextFromDate(pendencia.getDataLembrete()));
                } else {
                    layoutLembrete.setVisibility(View.INVISIBLE);
                }

                imgSync.setImageResource(pendencia.isSync() ? R.drawable.ic_done : R.drawable.ic_sync_wait);
            }
        }

        return convertView;
    }

    private String getTextFromDate(Date date) {
        if (date == null) return null;
        DateCalendar dateCalendar = DateCalendar.fromDate(date);
        String format = (DateFormat.is24HourFormat(this.context) ? "HH:mm" : "h:mm a");
        if (!dateCalendar.isToday()) {
            format = " 'às' " + format;
            if (dateCalendar.isYesterday()) {
                format = "'ontem'" + format;
            } else if (dateCalendar.isTomorrow()) {
                format = "'amanhã'" + format;
            } else {
                format = (dateCalendar.isCurrentWeek() ? "EEEE" : "d MMM" + (dateCalendar.isCurrentYear() ? "" : " yyyy")) + format;
            }
        }

        return new SimpleDateFormat(format, new Locale("pt", "BR")).format(date);
    }

    @Override
    public void notifyDataSetChanged() {
        this.ordenarListaPorData();
        this.adicionarSeparadores();
        super.notifyDataSetChanged();
        if (!this.actionMode) this.selection = new boolean[getCountPendencias()];
    }


    public int getCountSelected() {
        return this.listSelect.size();
    }

    public int getCountPendencias() {
        return this.listPendencias.size();
    }

    public List<Pendencia> getSelectedItems() {
        return this.listSelect;
    }

    @Override
    public int getCount() {
        return this.listItens.size();
    }

    @Override
    public Object getItem(int position) {
        return this.listItens.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public static class ListSeparator {
        String descricao;
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM 'de' yyyy", LOCALE_BR);

        public ListSeparator(Date date) {
            this.descricao = dateFormat.format(date).toUpperCase();
        }

        public String getDescricao() {
            return descricao;
        }
    }

    @Override
    public boolean isEnabled(int position) {
        if (getItem(position) instanceof ListSeparator) return false;
        return super.isEnabled(position);
    }


}
