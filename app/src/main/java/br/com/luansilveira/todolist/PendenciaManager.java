package br.com.luansilveira.todolist;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import br.com.luansilveira.todolist.db.Model.Pendencia;

public class PendenciaManager {

    public static void programarHorarioLembrete(Context context, Pendencia pendencia) {
        programarHorarioLembrete(context, pendencia, false);
    }

    public static void programarHorarioLembrete(Context context, Pendencia pendencia, boolean cancelar) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, LembreteReceiver.class);
        intent.putExtra("pendencia", pendencia);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, pendencia.getId(), intent, 0);
        if (cancelar) manager.cancel(pendingIntent);
        else
            manager.setExact(AlarmManager.RTC_WAKEUP, pendencia.getDataLembrete().getTime(), pendingIntent);
    }

    public static void cancelarLembrete(Context context, Pendencia pendencia) {
        programarHorarioLembrete(context, pendencia, true);
    }
}
