package br.com.luansilveira.todolist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.TableUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import br.com.luansilveira.todolist.db.DB;
import br.com.luansilveira.todolist.db.Model.Pendencia;
import br.com.luansilveira.todolist.utils.HttpRequest;
import br.com.luansilveira.todolist.utils.JSON;

public class MainActivity extends AppCompatActivity {

    public static final String BROADCAST_ATUALIZAR_LISTA = "br.com.luansilveira.todolist.BROADCAST_ATUALIZAR_LISTA";
    private static final int REQUEST_PENDENCIA = 0xFF;
    private ListView listView;
    private ListPendenciasAdapter adapter;
    private List<Pendencia> listPendencias;
    private TextView txtVazio;
    private Dao<Pendencia, Integer> daoPendencias;
    private BroadcastReceiver serverUpdateReceiver;

    private MenuItem menuSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        txtVazio = findViewById(R.id.txtVazio);
        listPendencias = new ArrayList<>();

        try {
            carregarLista();


            adapter = new ListPendenciasAdapter(this, listPendencias);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener((parent, view, position, id) -> {
                Pendencia pendencia = (Pendencia) parent.getItemAtPosition(position);
                startActivityForResult(new Intent(this, PendenciaActivity.class).putExtra("pendencia", pendencia), REQUEST_PENDENCIA);
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.serverUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(getClass().getSimpleName(), "Atualizando lista...");
                atualizarLista();
            }
        };

        registerReceiver(this.serverUpdateReceiver, new IntentFilter(BROADCAST_ATUALIZAR_LISTA));

        sincronizarPendenciasServidor();
    }

    public void buttonMaisClick(View view) {
        startActivityForResult(new Intent(this, PendenciaActivity.class), REQUEST_PENDENCIA);
    }

    private void carregarLista() throws SQLException {
        daoPendencias = DB.get(this).getDao(Pendencia.class);
        listPendencias = daoPendencias.queryForEq("deleted", false);
        mostrarLista();
        ordenarListaPorData();
    }

    private void mostrarLista() {
        boolean vazio = listPendencias.size() == 0;
        txtVazio.setVisibility(vazio ? View.VISIBLE : View.GONE);
        listView.setVisibility(!vazio ? View.VISIBLE : View.GONE);
    }

    private void ordenarListaPorData() {
        Collections.sort(listPendencias, (o1, o2) -> o2.getDataHora().compareTo(o1.getDataHora()));
    }

    private synchronized void atualizarLista() {
        try {
            List<Pendencia> list = daoPendencias.queryForEq("deleted", false);
            this.listPendencias.clear();
            this.listPendencias.addAll(list);
            ordenarListaPorData();
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
                            this.listPendencias.add(JSON.parseJsonToObject(obj, Pendencia.class));
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
}