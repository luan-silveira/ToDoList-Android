package br.com.luansilveira.todolist;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import br.com.luansilveira.todolist.db.DB;
import br.com.luansilveira.todolist.db.Model.Pendencia;

public class OnBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        List<Pendencia> pendencias = this.carregarPendenciasAgendads(context);
        if (pendencias == null) return;

        for (Pendencia p : pendencias) {
            PendenciaManager.programarHorarioLembrete(context, p);
        }

        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(context, MainActivity.ID_CANAL_NOTIFICACAO_LEMBRETE);
        } else {
            builder = new NotificationCompat.Builder(context);
        }

        Notification notification = builder.setContentTitle("ToDoList")
                .setContentText("Boot completo - pendências reagendadas.")
                .setSmallIcon(R.drawable.ic_alarm)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true).build();

        NotificationManagerCompat.from(context).notify(-1, notification);
    }

    private List<Pendencia> carregarPendenciasAgendads(Context context) {
        try {
            Dao<Pendencia, ?> daoPendencias = DB.get(context).getDao(Pendencia.class);
            return daoPendencias.query(daoPendencias.queryBuilder().where().ge("data_lembrete", new Date()).prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
