package br.com.luansilveira.todolist.utils;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Classe utilizada para realizar requisições HTTP para um servidor.</p>
 * <p>Esta classe implementa e abstrai as funcionalidades da classe {@link HttpURLConnection} em uma {@link AsyncTask}.</p>
 *
 * @author Luan Christian Nascimento da Silveira
 */
public class HttpRequest extends AsyncTask<Void, Integer, String> {

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_PATCH = "PATCH";
    public static final String METHOD_DELETE = "DELETE";


    private String url;
    private String method;

    private HttpURLConnection urlConnection;
    private HttpListener httpListener;
    private RequestDoneListener requestDoneListener;
    private RequestFailListener requestFailListener;
    private PreExecuteListener preListener;
    private PostExecuteListener postListener;
    private ProgressUpdateListener progressListener;
    private Map<String, String> headers;
    private JSONObject data;
    private int httpResponseCode = 200;
    private String httpResponseText;
    private boolean erro = false;

    private int timeout = 30;


    /**
     * @param url    URL
     * @param method Método (GET/POST)
     */
    private HttpRequest(String url, String method) {
        this.url = url;
        this.method = method;
    }

    /**
     * Cria uma nova requisição com método GET.
     *
     * @param url URL
     * @return HttpRequest
     */
    public static HttpRequest get(String url) {
        return new HttpRequest(url, METHOD_GET);
    }

    /**
     * Cria uma nova requisição com método POST.
     *
     * @param url URL
     * @return HttpRequest
     */
    public static HttpRequest post(String url) {
        return new HttpRequest(url, METHOD_POST);
    }

    public HttpListener getHttpListener() {
        return httpListener;
    }

    /**
     * Define um {@link HttpListener} que será utilizado para implementar os eventos após o retorno da requisição.
     *
     * @param httpListener listener
     * @return HttpRequest
     */
    public HttpRequest setHttpListener(HttpListener httpListener) {
        this.httpListener = httpListener;
        return this;
    }

    /**
     * Define um {@link RequestDoneListener} que será utilizado para implementar os eventos após a requisição ser concluída com êxito.
     *
     * @param requestDoneListener listener
     * @return HttpRequest
     */
    public HttpRequest setRequestDoneListener(RequestDoneListener requestDoneListener) {
        this.requestDoneListener = requestDoneListener;
        return this;
    }

    /**
     * Define um {@link RequestFailListener} que será utilizado para implementar os eventos após uma falha na requisição.
     *
     * @param requestFailListener listener
     * @return HttpRequest
     */
    public HttpRequest setRequestFailListener(RequestFailListener requestFailListener) {
        this.requestFailListener = requestFailListener;
        return this;
    }

    /**
     * Define um {@link PreExecuteListener} que será utilizado para implementar os eventos antes do envio da requisição.
     * Pode ser utilizado principalmente para desativar componentes visuais, mostrar progressBars, etc.
     *
     * @param listener listener
     * @return HttpRequest
     */
    public HttpRequest setPreExecuteListener(PreExecuteListener listener) {
        this.preListener = listener;
        return this;
    }

    /**
     * Define um {@link PreExecuteListener} que será utilizado para implementar os eventos após o retorno da requisição.
     * Este evento é executado após os listeners {@link RequestDoneListener}, {@link RequestFailListener} e o {@link HttpListener}.
     * Pode ser utilizado principalmente para ativar componentes visuais, esconder progressBars, etc.
     *
     * @param postListener listener
     * @return HttpRequest
     */
    public HttpRequest setPostExecuteListener(PostExecuteListener postListener) {
        this.postListener = postListener;
        return this;
    }

    /**
     * Define um {@link ProgressUpdateListener} que será utilizado para atualizar o progresso da requisição.
     *
     * @param listener listener
     * @return HttpRequest
     */
    public HttpRequest setProgressListener(ProgressUpdateListener listener) {
        this.progressListener = listener;
        return this;
    }

    /**
     * Define o tempo limite (timeout) da requisição. O tempo padrão é de 30s.
     *
     * @param timeout Tempo limite, em segundos
     * @return HttpRequest
     */
    public HttpRequest setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Retorna os cabeçalhos HTTP que foram adicionados à requisição.
     *
     * @return Map
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Adiciona cabeçalhos HTTP à requisição.
     *
     * @param headers {@link Map} que contém as informações dos headers HTTP da requisição.
     * @return HttpRequest
     */
    public HttpRequest setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Retorna os dados em formato JSON que foram adicionados à requisição.
     *
     * @return JSONObject
     */
    public JSONObject getData() {
        return data;
    }

    /**
     * Adiciona dados no formato JSON ao corpo da requisição.
     * Automaticamente a requisição será definida para o tipo {@code application/json}.
     *
     * @param data {@link JSONObject} que contém os dados da requisição.
     * @return HttpRequest
     */
    public HttpRequest setData(JSONObject data) {
        this.data = data;
        return this;
    }

    /**
     * Adiciona dados no formato JSON ao corpo da requisição.
     * Automaticamente a requisição será definida para o tipo {@code application/json}.
     *
     * @param data {@link Map} que contém os dados da requisição. Este será convertido internamente em um {@link JSONObject}.
     * @return HttpRequest
     */
    public HttpRequest setData(Map<String, String> data) {
        this.data = (JSONObject) data;
        return this;
    }

    public HttpRequest setData(Object data) {
        try {
            this.data = JSON.parseObj(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }


    /**
     * Adiciona cabeçalhos ao corpo da requisição.
     *
     * @param key   chave
     * @param value valor
     * @return HttpRequest
     */
    public HttpRequest addHeader(String key, String value) {
        if (this.headers == null) this.headers = new HashMap<>();
        this.headers.put(key, value);
        return this;
    }


    /**
     * Adiciona dados no formato JSON ao corpo da requisição.
     *
     * @param key   chave
     * @param value valor
     * @return HttpRequest
     */
    public HttpRequest appendData(String key, String value) {
        if (this.data == null) this.data = new JSONObject();
        try {
            this.data.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Adiciona dados no formato JSON ao corpo da requisição.
     *
     * @param key   chave
     * @param value valor
     * @return HttpRequest
     */
    public HttpRequest appendData(String key, JSONObject value) {
        if (this.data == null) this.data = new JSONObject();
        try {
            this.data.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Adiciona dados no formato JSON ao corpo da requisição.
     *
     * @param key   chave
     * @param value valor
     * @return HttpRequest
     */
    public HttpRequest appendData(String key, JSONArray value) {
        if (this.data == null) this.data = new JSONObject();
        try {
            this.data.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Adiciona dados no formato JSON ao corpo da requisição.
     *
     * @param key   chave
     * @param value valor
     * @return HttpRequest
     */
    public HttpRequest appendData(String key, Object value) {
        if (this.data == null) this.data = new JSONObject();
        try {
            JSONObject json = JSON.parseObj(value);
            this.data.put(key, json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Adiciona dados no formato JSON ao corpo da requisição.
     *
     * @param key   chave
     * @param value valor
     * @return HttpRequest
     */
    public HttpRequest appendData(String key, int value) {
        if (this.data == null) this.data = new JSONObject();
        try {
            this.data.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Adiciona dados no formato JSON ao corpo da requisição.
     *
     * @param key   chave
     * @param value valor
     * @return HttpRequest
     */
    public HttpRequest appendData(String key, double value) {
        if (this.data == null) this.data = new JSONObject();
        try {
            this.data.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Adiciona dados no formato JSON ao corpo da requisição.
     *
     * @param key   chave
     * @param value valor
     * @return HttpRequest
     */
    public HttpRequest appendData(String key, boolean value) {
        if (this.data == null) this.data = new JSONObject();
        try {
            this.data.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Retorna o código de resposta HTTP do servidor (200, 404, 500, etc.).
     *
     * @return int HTTP response
     */
    public int getHttpResponseCode() {
        return httpResponseCode;
    }

    /**
     * Retorna o texto de resposta HTTP do servidor referente ao código HTTP (200, 404, 500, etc.).
     *
     * @return int HTTP response
     */
    public String getHttpResponseText() {
        return httpResponseText;
    }

    /**
     * Envia a requisição para o servidor.
     *
     * @param listener {@link HttpListener} que será executado após o retorno da requisição
     */
    public void send(HttpListener listener) {
        this.setHttpListener(listener);
        this.execute();
    }


    /**
     * Envia a requisição para o servidor.
     */
    public void send() {
        this.send(null);
    }

    /**
     * Verifica se a requisição está sendo executada ou não.
     *
     * @return Valor booleano ({@code true}/{@code false}) que indica a execução
     */
    public boolean isRunning() {
        return this.getStatus() == Status.RUNNING;
    }

    //Listeners interfaces

    @Override
    protected void onPreExecute() {
        if (this.preListener != null) this.preListener.onRequestPreExecute();
    }

    @Override
    protected String doInBackground(Void... voids) {
        String responseData = null;

        BufferedReader reader = null;
        try {
            URL url = new URL(this.url);
            boolean hasRequestBody = (this.method.equals(METHOD_POST) || this.method.equals(METHOD_PUT));
            int timeout = this.timeout * 1000;
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(timeout);
            urlConnection.setConnectTimeout(timeout);
            urlConnection.setDoInput(true);
            if (hasRequestBody) urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod(this.method);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");

            if (this.headers != null) {
                for (Map.Entry<String, String> header : this.headers.entrySet()) {
                    urlConnection.setRequestProperty(header.getKey(), header.getValue());
                }
            }

            urlConnection.connect();

            if (hasRequestBody && this.data != null) {
                if (this.data.length() > 0) {
                    Writer wr = new OutputStreamWriter(urlConnection.getOutputStream());
                    wr.write(data.toString());
                    wr.flush();
                    wr.close();
                }
            }

            this.httpResponseCode = urlConnection.getResponseCode();
            this.httpResponseText = urlConnection.getResponseMessage();
            this.erro = (this.httpResponseCode >= 400);

            InputStream inputStream;

            if (this.erro) {
                inputStream = urlConnection.getErrorStream();
            } else {
                inputStream = urlConnection.getInputStream();
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String linha;
            StringBuilder builder = new StringBuilder();
            while ((linha = reader.readLine()) != null) {
                builder.append(linha);
                builder.append("\n");
            }


            responseData = builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            this.httpResponseCode = -1;
            this.httpResponseText = e.getMessage();
            this.erro = true;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        return responseData;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (this.progressListener != null) this.progressListener.onRequestProgressUpdate(values[0]);
    }

    @Override
    protected void onPostExecute(String s) {
        JSON obj = null;

        Log.d("HttpRequest", "Retorno JSON:" + s);
        try {
            if (s != null && !s.trim().isEmpty()) obj = new JSON(s);

            if (this.erro) {
                HttpError error = new HttpError(this.httpResponseCode, this.httpResponseText, obj);
                if (this.httpListener != null) this.httpListener.onRequestFail(error);
                if (this.requestFailListener != null) this.requestFailListener.fail(error);
            } else {
                if (this.httpListener != null) this.httpListener.onRequestDone(obj);
                if (this.requestDoneListener != null) this.requestDoneListener.done(obj);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            HttpError error = new HttpError(this.httpResponseCode, this.httpResponseText, null);
            if (this.httpListener != null) this.httpListener.onRequestFail(error);
            if (this.requestFailListener != null) this.requestFailListener.fail(error);
        } finally {
            if (this.postListener != null) this.postListener.onRequestPostExecute();
        }
    }

    @Override
    protected void onCancelled() {
        if (urlConnection != null) {
            urlConnection.disconnect();
        }
    }

    //----

    /**
     * Interface utilizada para implementar ações a serem executadas após o retorno da requisição.<br>
     * Ela possui dois métodos para serem implementados quando a requisição for concluída com êxito ou em caso de falha.
     */
    public interface HttpListener {
        /**
         * Método executado quando há êxito na requisição.
         *
         * @param result Dados JSON.
         */
        void onRequestDone(JSON result);

        /**
         * Método executado quando há falha na requisição.
         *
         * @param error HttpError
         */
        void onRequestFail(HttpError error);
    }


    //--- Métodos da AsyncTask

    /**
     * Interface utilizada para implementar ações a serem executadas após o êxito da requisição.
     */
    public interface RequestDoneListener {
        void done(JSON result);
    }

    /**
     * Interface utilizada para implementar ações a serem executadas após a falha da requisição.
     */
    public interface RequestFailListener {
        void fail(HttpError error);
    }

    /**
     * Interface utilizada para implementar ações a serem executadas antes do envio da requisição.
     */
    public interface PreExecuteListener {
        void onRequestPreExecute();
    }

    /**
     * Interface utilizada para implementar ações a serem executadas após o retorno da requisição.
     */
    public interface PostExecuteListener {
        void onRequestPostExecute();
    }

    public interface ProgressUpdateListener {
        void onRequestProgressUpdate(Integer progress);
    }

    /**
     * Classe utilizada para representar um erro HTTP retornado do servidor.
     */
    public static class HttpError {

        private int statusCode;
        private String textStatus;
        private JSON data;

        public HttpError(int statusCode, String textStatus, JSON data) {
            this.statusCode = statusCode;
            this.textStatus = textStatus;
            this.data = data;
        }

        /**
         * Retorna o código de erro HTTP do servidor (200, 404, 500, etc.).
         *
         * @return int
         */
        public int getStatusCode() {
            return statusCode;
        }

        /**
         * Define o código de erro HTTP do servidor (200, 404, 500, etc.).
         */
        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        /**
         * Retorna o texto de erro referente ao código de erro HTTP do servidor (200, 404, 500, etc.).
         *
         * @return int
         */
        public String getTextStatus() {
            return textStatus;
        }

        /**
         * Define o texto de erro HTTP do servidor.
         */
        public void setTextStatus(String textStatus) {
            this.textStatus = textStatus;
        }

        /**
         * Retorna os dados do erro em formato JSON.
         *
         * @return JSON
         */
        public JSON getData() {
            return data;
        }

        /**
         * Define os dados do erro em formato JSON.
         */
        public void setData(JSON data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return "Status: " + this.statusCode + " - " + (this.textStatus == null ? "" : this.textStatus) + "\n"
                    + (data == null ? "" : data.toString());
        }
    }
}
