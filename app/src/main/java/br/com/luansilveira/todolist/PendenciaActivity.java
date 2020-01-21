package br.com.luansilveira.todolist;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.j256.ormlite.dao.Dao;

import org.json.JSONException;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import br.com.luansilveira.todolist.db.DB;
import br.com.luansilveira.todolist.db.Model.Pendencia;
import br.com.luansilveira.todolist.utils.HttpRequest;

public class PendenciaActivity extends AppCompatActivity {

    EditText edTitulo;
    EditText edDescricao;

    MenuItem menuSalvar;

    private Pendencia pendencia;
    private Dao<Pendencia, Integer> daoPendencias;
    private int result = RESULT_CANCELED;

    private boolean editMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pendencia);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        edTitulo = findViewById(R.id.edTitulo);
        edDescricao = findViewById(R.id.edDescricao);

        this.pendencia = (Pendencia) getIntent().getSerializableExtra("pendencia");
        if (pendencia != null) {
            edTitulo.setText(pendencia.getTitulo());
            edDescricao.setText(pendencia.getDescricao());
        } else {
            this.editMode = true;
            edDescricao.requestFocus();
        }

        TextWatcher textListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setEditMode(true);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        edDescricao.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) setEditMode(true);
        });

        edTitulo.addTextChangedListener(textListener);
        edDescricao.addTextChangedListener(textListener);

        try {
            this.daoPendencias = DB.get(this).getDao(Pendencia.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void salvarPendencia() {
        if (pendencia == null) {
            pendencia = new Pendencia();
        }

        String titulo = edTitulo.getText().toString();
        String descricao = edDescricao.getText().toString();

        if (titulo.isEmpty() || descricao.isEmpty()) {
            Toast.makeText(this, "Impossível salvar nota vazia!", Toast.LENGTH_LONG).show();
            return;
        }

        pendencia.setTitulo(titulo);
        pendencia.setDescricao(descricao);
        pendencia.setDataHora(new Date());
        pendencia.setSync(false);

        try {
            Dao.CreateOrUpdateStatus status = daoPendencias.createOrUpdate(pendencia);
            if (status.isCreated() || status.isUpdated()) {
                result = RESULT_OK;
                Toast.makeText(this, "Salvo", Toast.LENGTH_LONG).show();
                setEditMode(false);

                salvarPendenciaServidor(status.isUpdated());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void salvarPendenciaServidor(boolean update) {
        Log.i(getClass().getSimpleName(), (update ? "Atualizando" : "Criando") + " pendência no servidor");
        HttpRequest.post(getString(update ? R.string.url_atualizar_pendencia : R.string.url_inserir_pendencia))
                .appendData("id", pendencia.getId())
                .appendData("titulo", pendencia.getTitulo())
                .appendData("descricao", pendencia.getDescricao())
                .appendData("data_hora", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(pendencia.getDataHora()))
                .setRequestDoneListener(result1 -> {
                    try {
                        if (result1.has("sucesso")) {
                            Log.i(getClass().getSimpleName(), "Pendência salva");
                            pendencia.setSync(true);
                            daoPendencias.update(pendencia);
                            sendBroadcast(new Intent(MainActivity.BROADCAST_ATUALIZAR_LISTA));
                        } else {
                            Log.i(getClass().getSimpleName(), "Erro ao salvar pendência:\n" + result1.getString("mensagem"));
                        }
                    } catch (SQLException | JSONException e) {
                        e.printStackTrace();
                    }
                }).setRequestFailListener(error -> Log.i(getClass().getSimpleName(), "Erro ao salvar pendência.")).send();
    }

    private void setEditMode(boolean editMode) {
        this.editMode = editMode;
        menuSalvar.setEnabled(editMode);

        if (!editMode) {
            if (edTitulo.hasFocus()) edTitulo.clearFocus();
            else if (edDescricao.hasFocus()) edDescricao.clearFocus();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pendencia_menu, menu);
        this.menuSalvar = menu.findItem(R.id.menuSalvar);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.menuSalvar:
                salvarPendencia();
                break;
        }

        return true;
    }


    @Override
    public void onBackPressed() {
        if (this.editMode) {
            new AlertDialog.Builder(this)
                    .setMessage("Deseja sair?\nSuas alterações serão descartadas.")
                    .setPositiveButton("Descartar", (dialog, which) -> {
                        setResult(RESULT_CANCELED);
                        finish();
                    }).setNegativeButton("Cancelar", null).show();
            return;
        }

        setResult(this.result);
        finish();
    }
}
