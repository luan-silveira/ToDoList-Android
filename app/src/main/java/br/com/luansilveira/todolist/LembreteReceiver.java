package br.com.luansilveira.todolist;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import br.com.luansilveira.todolist.db.Model.Pendencia;

public class LembreteReceiver extends BroadcastReceiver {

    private static int id = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        Pendencia pendencia = (Pendencia) intent.getSerializableExtra("pendencia");

        Intent intentPendencia = new Intent(context, PendenciaActivity.class).putExtras(intent);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intentPendencia, 0);
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(context, MainActivity.ID_CANAL_NOTIFICACAO_LEMBRETE);
        } else {
            builder = new NotificationCompat.Builder(context);
        }

        Notification notification = builder.setContentTitle(pendencia.getTitulo())
                .setContentIntent(pIntent)
                .setContentText("ToDoList - Novo Lembrete")
                .setSmallIcon(R.drawable.ic_alarm)
                .setPriority(Notification.PRIORITY_MAX)
                .setVibrate(new long[]{250, 250}).build();

        NotificationManagerCompat.from(context).notify(id++, notification);
    }
}
