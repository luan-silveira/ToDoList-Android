package br.com.luansilveira.todolist.db.Model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.Date;

import br.com.luansilveira.todolist.utils.JSON;

@DatabaseTable(tableName = "pendencias")
public class Pendencia implements Serializable {

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    private int id;
    @DatabaseField
    private String titulo;
    @DatabaseField
    private String descricao;
    @DatabaseField(columnName = "data_hora")
    @JSON.JSONFieldName("data_hora")
    private Date dataHora;
    @DatabaseField(columnName = "data_lembrete")
    @JSON.JSONFieldName("data_lembrete")
    private Date dataLembrete;
    @DatabaseField
    private boolean sync;
    @DatabaseField
    private boolean deleted;

    public Pendencia() {
    }

    public Pendencia(String titulo) {
        this.titulo = titulo;
    }

    public int getId() {
        return id;
    }

    public Pendencia setId(int id) {
        this.id = id;
        return this;
    }

    public String getTitulo() {
        return titulo;
    }

    public Pendencia setTitulo(String titulo) {
        this.titulo = titulo;
        return this;
    }

    public String getDescricao() {
        return descricao;
    }

    public Pendencia setDescricao(String descricao) {
        this.descricao = descricao;
        return this;
    }

    public Date getDataHora() {
        return dataHora;
    }

    public Pendencia setDataHora(Date dataHora) {
        this.dataHora = dataHora;
        return this;
    }

    public Date getDataLembrete() {
        return dataLembrete;
    }

    public Pendencia setDataLembrete(Date dataLembrete) {
        this.dataLembrete = dataLembrete;
        return this;
    }

    public boolean hasLembrete() {
        return this.dataLembrete != null;
    }

    public boolean isSync() {
        return sync;
    }

    public Pendencia setSync(boolean sync) {
        this.sync = sync;
        return this;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Pendencia setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }
}
