package br.com.luansilveira.todolist;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.TableUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import br.com.luansilveira.todolist.db.DB;
import br.com.luansilveira.todolist.db.Model.Pendencia;
import br.com.luansilveira.todolist.utils.HttpRequest;
import br.com.luansilveira.todolist.utils.JSON;
import br.com.luansilveira.todolist.utils.Notify;

public class MainActivity extends AppCompatActivity implements AbsListView.MultiChoiceModeListener {

    public static final String BROADCAST_ATUALIZAR_LISTA = "br.com.luansivleira.todolist.BROADCAST_ATUALIZAR_LISTA";
    public static final String ID_CANAL_NOTIFICACAO_LEMBRETE = "notificacao_lembrete";
    private static final int REQUEST_PENDENCIA = 0xFF;

    private ListView listView;
    private ListPendenciasAdapter2 adapter;
    private List<Pendencia> listPendencias;
    private List<Pendencia> listPendenciasSync;
    private TextView txtVazio;
    private Dao<Pendencia, Integer> daoPendencias;

    private MenuItem menuSync;

    private int intLastItemVisibleList = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        listView.setMultiChoiceModeListener(this);
        txtVazio = findViewById(R.id.txtVazio);
        listPendencias = new ArrayList<>();

        try {
            carregarLista();

            adapter = new ListPendenciasAdapter2(this, this.listPendencias);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener((parent, view, position, id) -> {
                Pendencia pendencia = (Pendencia) parent.getItemAtPosition(position);
                startActivityForResult(new Intent(this, PendenciaActivity.class).putExtra("pendencia", pendencia), REQUEST_PENDENCIA);
            });

            LinearLayout layoutHeader = findViewById(R.id.layoutHeaderData);
            TextView txtHeaderData = layoutHeader.findViewById(R.id.txtData);

            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (firstVisibleItem == intLastItemVisibleList) return;

                    Object obj = adapter.getItem(firstVisibleItem);
                    if (obj instanceof ListPendenciasAdapter2.ListSeparator) {
                        txtHeaderData.setText(((ListPendenciasAdapter2.ListSeparator) obj).getDescricao());
                        intLastItemVisibleList = firstVisibleItem;
                    } else {
                        if (firstVisibleItem < intLastItemVisibleList) {
                            do {
                                obj = adapter.getItem(firstVisibleItem--);
                                if (obj instanceof ListPendenciasAdapter2.ListSeparator) {
                                    txtHeaderData.setText(((ListPendenciasAdapter2.ListSeparator) obj).getDescricao());
                                    intLastItemVisibleList = firstVisibleItem;
                                    break;
                                }
                            } while (intLastItemVisibleList > 0);
                        }
                    }
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(getClass().getSimpleName(), "Atualizando lista...");
                atualizarLista();
                sincronizarPendenciasServidor();
            }
        }, new IntentFilter(BROADCAST_ATUALIZAR_LISTA));

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (isOnline(MainActivity.this)) {
                    sincronizarPendenciasServidor();
                }
            }
        }, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        sincronizarPendenciasServidor();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Notify.from(this).criarCanalNotificacao(ID_CANAL_NOTIFICACAO_LEMBRETE, "Notificação de lembrete", NotificationManager.IMPORTANCE_MAX);
        }
    }

    private boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    public void buttonMaisClick(View view) {
        startActivityForResult(new Intent(this, PendenciaActivity.class), REQUEST_PENDENCIA);
    }

    private void carregarLista() throws SQLException {
        daoPendencias = DB.get(this).getDao(Pendencia.class);
        this.listPendencias = daoPendencias.queryForAll();
    }

    private void mostrarLista() {
        boolean vazio = listPendencias.size() == 0;
        txtVazio.setVisibility(vazio ? View.VISIBLE : View.GONE);
        listView.setVisibility(!vazio ? View.VISIBLE : View.GONE);
    }

    /**
     * Ordena a lista por data e filtra os itens não excluídos;
     */
//    private void filtrarEOrdenarLista() {
//        //-- Mostra apenas os itens que não estão marcados como excluídos.
//        listPendencias.clear();
//        for (Pendencia p : listPendenciasSync) {
//            if (!p.isDeleted()) listPendencias.add(p);
//        }
//
//        //-- Ordena por data
//        Collections.sort(listPendencias, (o1, o2) -> o2.getDataHora().compareTo(o1.getDataHora()));
//    }

    private synchronized void atualizarLista() {
        try {
            List<Pendencia> list = daoPendencias.queryForAll();
            this.listPendencias.clear();
            this.listPendencias.addAll(list);
            adapter.notifyDataSetChanged();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        mostrarLista();
    }

    private void sincronizarPendenciasServidor() {
        Log.i(getClass().getSimpleName(), "Sincronizando lista...");
        if (menuSync != null) menuSync.setEnabled(false);

        JSONArray jsonArray = new JSONArray();

        try {

            for (Pendencia pendencia : listPendencias) {
                JSONObject obj = JSON.parseObj(pendencia);
                jsonArray.put(obj);
            }

            Log.i(getClass().getSimpleName(), "JSON sincronização -->\n" + jsonArray.toString());
            HttpRequest.post(getString(R.string.url_sync_pendencias)).appendData("pendencias", jsonArray).setRequestDoneListener(result -> {
                try {
                    if (result.has("erro")) {
                        Log.i(getClass().getSimpleName(), "Erro ao sincronizar");
                    } else {
                        this.listPendencias.clear();
                        for (Iterator<JSONObject> iterator = result.iterator(JSONObject.class); iterator.hasNext(); ) {
                            JSONObject obj = iterator.next();
                            Pendencia pendencia = JSON.parseJsonToObject(obj, Pendencia.class);
                            if (pendencia != null && pendencia.hasLembrete() && pendencia.getDataLembrete().after(new Date())) {
                                PendenciaManager.programarHorarioLembrete(MainActivity.this, pendencia);
                            }
                            this.listPendencias.add(pendencia);
                        }
                        TableUtils.clearTable(DB.get(this).getConnectionSource(), Pendencia.class);
                        daoPendencias.create(listPendencias);

                        atualizarLista();
                    }
                } catch (JSONException | SQLException e) {
                    e.printStackTrace();
                }
            }).setRequestFailListener(error -> Log.i(getClass().getSimpleName(), "Erro ao sincronizar\n"))
                    .setPostExecuteListener(() -> {
                        if (menuSync != null) menuSync.setEnabled(true);
                    })
                    .send();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void excluirPendencias(ActionMode mode, List<Pendencia> pendencias) {
        new AlertDialog.Builder(this).setTitle("Excluir")
                .setMessage("Deseja excluir os registros selecionados?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    try {
                        for (Pendencia p : pendencias) {
                            p.setDeleted(true);
                            daoPendencias.update(p);
                        }
                        excluirPendenciasServidor(pendencias);
                        mode.finish();
                        atualizarLista();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }).setNegativeButton("Não", null).show();
    }

    private void excluirPendenciasServidor(List<Pendencia> pendencias) {
        JSONArray array = new JSONArray();
        for (Pendencia p : pendencias) {
            array.put(p.getId());
        }

        HttpRequest.post(getString(R.string.url_excluir_pendencias)).appendData("ids", array)
                .setRequestDoneListener(result -> {
                    try {
                        if (result.has("sucesso")) {
                            daoPendencias.delete(pendencias);
                            atualizarLista();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }).send();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_PENDENCIA && resultCode == RESULT_OK) {
            atualizarLista();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menuSync = menu.findItem(R.id.menuSync);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuSync) {
            Toast.makeText(this, "Sincronizando dados ...", Toast.LENGTH_SHORT).show();
            sincronizarPendenciasServidor();
        }

        return true;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        getMenuInflater().inflate(R.menu.list_select_menu, menu);
        adapter.setActionMode(true);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (item.getItemId() == R.id.menuDelete) {
            excluirPendencias(mode, adapter.getSelectedItems());
        }

        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        adapter.setActionMode(false);
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        if (adapter.getItem(position) instanceof ListPendenciasAdapter2.ListSeparator) return;
        adapter.setSelection(position, checked);
        int count = adapter.getCountSelected();
        mode.setTitle(count + " selecionado" + (count > 1 ? "s" : ""));
    }
}
