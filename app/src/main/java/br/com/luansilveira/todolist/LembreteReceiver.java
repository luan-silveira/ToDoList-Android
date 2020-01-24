package br.com.luansilveira.todolist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import br.com.luansilveira.todolist.db.Model.Pendencia;
import br.com.luansilveira.todolist.utils.Notify;

public class LembreteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Pendencia pendencia = (Pendencia) intent.getSerializableExtra("pendencia");
        Notify.from(context).criarNotificacao(intent, 0, pendencia.getTitulo(), "", MainActivity.ID_CANAL_NOTIFICACAO_LEMBRETE, true);
    }
}
