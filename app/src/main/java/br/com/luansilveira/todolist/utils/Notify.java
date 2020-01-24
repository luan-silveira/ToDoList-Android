package br.com.luansilveira.todolist.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * Classe utilizada para implementar notificações no Android.
 */
public class Notify {

    private NotificationManager manager;
    private Context context;

    public Notify(Context context) {
        this.context = context;
        this.manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static Notify from(Context context) {
        return new Notify(context);
    }

    /**
     * Função utilizada para criar uma notificação e mostrá-la na barra de notificações.
     *
     * @param contentIntent
     * @param icon
     * @param title
     * @param text
     */
    public void criarNotificacao(Intent contentIntent, int icon, CharSequence title, CharSequence text) {
        this.criarNotificacao(contentIntent, icon, title, text, null);
    }

    public void criarNotificacao(Intent contentIntent, int icon, CharSequence title, CharSequence text, String id_canal) {
        this.criarNotificacao(contentIntent, icon, title, text, id_canal, false);
    }

    public void criarNotificacao(Intent contentIntent, int icon, CharSequence title, CharSequence text, String id_canal, boolean highPriority) {
        int id = 1;

        PendingIntent p = getPendingIntent(id, contentIntent, context);

        NotificationCompat.Builder notificacao = null;
        if (id_canal != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificacao = new NotificationCompat.Builder(context, id_canal);
            } else {
                notificacao = new NotificationCompat.Builder(context);
            }
        }
        notificacao.setSmallIcon(icon);
        notificacao.setContentTitle(title);
        notificacao.setContentText(text);
        notificacao.setContentIntent(p);
        if (highPriority) {
            notificacao.setPriority(Notification.PRIORITY_MAX);
            notificacao.setVibrate(new long[]{250, 250});
        }

        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        nm.notify(id, notificacao.build());
    }

    public NotificationManager getManager() {
        return manager;
    }

    private PendingIntent getPendingIntent(int id, Intent intent, Context context) {
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(intent.getComponent());
        stackBuilder.addNextIntent(intent);

        PendingIntent p = stackBuilder.getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT);

        return p;
    }

    /**
     * Função utilizada para criar um canal de notificação, obrigatório a partir da versão 8 (Oreo) do Android.
     *
     * @param id          String representando o ID do canal
     * @param nome        Nome do canal
     * @param importancia Importância do canal. Deve ser uma das constantes da classe {@link NotificationManager}:
     * @return
     */

    public NotificationChannel criarCanalNotificacao(String id, CharSequence nome, int importancia) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(id, nome, importancia);
            this.manager.createNotificationChannel(canal);

            return canal;
        } else {
            return null;
        }
    }

}
