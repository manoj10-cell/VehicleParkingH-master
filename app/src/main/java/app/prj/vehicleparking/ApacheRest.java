package app.prj.vehicleparking;
/*
 * Create by Veerabadhran
 * Add internet permission in AndroidManifest.in
 * <uses-permission android:name="android.permission.INTERNET" />
 * add this repository in build.gradle which located in app
 * implementation 'org.jbundle.util.osgi.wrapped:org.jbundle.util.osgi.wrapped.org.apache.http.client:4.1.2'
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ApacheRest {

    private ProgressDialog progress;
    private ArrayList<NameValuePair> queryParams, formParams, headers;
    private OnResponseListener onResponseListener;
    private HttpResponse httpResponse;
    private Exception exception;
    private String url;
    private boolean showProgress;
    private @Method int method;
    private boolean isSuccess;

    public ApacheRest(@NonNull String url) {
        this(null, Method.GET, url);
    }

    public ApacheRest(Context context, @NonNull String url) {
        this(context, Method.GET, url);
    }

    public ApacheRest(@Method int method, @NonNull String url) {
        this(null, method, url);
    }

    public ApacheRest(Context context, @Method int method, @NonNull String url) {
        this.url = url;
        this.method = method;
        queryParams = new ArrayList<NameValuePair>();
        formParams = new ArrayList<NameValuePair>();
        headers = new ArrayList<NameValuePair>();
        if (context != null) {
            progress = new ProgressDialog(context);
            progress.setIndeterminate(true);
        }
    }

    public ApacheRest addQueryParam(@NonNull String name, @NonNull String value) {
        queryParams.add(new BasicNameValuePair(name, value));
        return this;
    }



    public ApacheRest addFormParam(@NonNull String name, @NonNull String value) {
        formParams.add(new BasicNameValuePair(name, value));
        return this;
    }

    public ApacheRest addFormParam(@NonNull Map<String, String> formParam) {
        for (String key : formParam.keySet()) {
            if (key != null) formParams.add(new BasicNameValuePair(key, formParam.get(key)));
        }
        return this;
    }

    public ApacheRest addHeader(@NonNull String name, @NonNull String value) {
        headers.add(new BasicNameValuePair(name, value));
        return this;
    }

    public ApacheRest addHeader(@NonNull Map<String, String> header) {
        for (String key : header.keySet()) {
            if (key != null) headers.add(new BasicNameValuePair(key, header.get(key)));
        }
        return this;
    }

    public ApacheRest showProgress(boolean showProgress) {
        this.showProgress = showProgress;
        return this;
    }

    public ApacheRest setOnResponseListener(OnResponseListener onResponseListener) {
        this.onResponseListener = onResponseListener;
        return this;
    }

    public void connect() {
        new AsyncHttpTask().execute();
    }

    public void connectSync() {
        executeHttpResponse(executeHttpRequest());
    }

    private String executeHttpRequest() {
        HttpClient client = null;
        //client=new DefaultHttpClient(); //http

        //https {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SelfSSLSocketFactory sf = new SelfSSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            client = new DefaultHttpClient(ccm, params);
        } catch (Exception ex) {
            client = new DefaultHttpClient();
        }
        // } https
        try {
            //build Uri with URL & Get parameters
            String uri = queryParams.isEmpty() ? url : url+"?" + URLEncodedUtils.format(queryParams, HTTP.UTF_8);

            HttpUriRequest uriRequest = null;
            if (method == Method.DELETE && formParams.isEmpty()) {
                HttpDelete request = new HttpDelete(uri);
                uriRequest = request;
            } else if (method == Method.HEAD && formParams.isEmpty()) {
                HttpHead request = new HttpHead(uri);
                uriRequest = request;
            } else if (method == Method.OPTIONS && formParams.isEmpty()) {
                HttpOptions request = new HttpOptions(uri);
                uriRequest = request;
            } else if (method == Method.TRACE && formParams.isEmpty()) {
                HttpTrace request = new HttpTrace(uri);
                uriRequest = request;
            } else if (method == Method.GET && formParams.isEmpty()) {
                HttpGet request = new HttpGet(uri);
                uriRequest = request;
            } else if (method == Method.PUT) {
                HttpPut request = new HttpPut(uri);
                if (!formParams.isEmpty())
                    request.setEntity(new UrlEncodedFormEntity(formParams, HTTP.UTF_8));
                uriRequest = request;
            } else if (method == Method.POST) {
                HttpPost request = new HttpPost(uri);
                if (!formParams.isEmpty())
                    request.setEntity(new UrlEncodedFormEntity(formParams, HTTP.UTF_8));
                uriRequest = request;
            } else {
                HttpPost request = new HttpPost(uri);
                request.setEntity(new UrlEncodedFormEntity(formParams, HTTP.UTF_8));
                uriRequest = request;
            }
            //add headers
            for (NameValuePair h : headers) {
                uriRequest.addHeader(h.getName(), h.getValue());
            }
            Log.i("ul",uri);
            httpResponse = client.execute(uriRequest);
            String content = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
            isSuccess = true;
            return content;
        } catch (IOException ex) {
            exception = ex;
            client.getConnectionManager().shutdown();
            ex.printStackTrace();
            isSuccess = false;
            return null;
        }
    }

    private void executeHttpResponse(String responseContent) {
        if (onResponseListener != null) {
            if (isSuccess) {
                onResponseListener.onResponse(responseContent, httpResponse);
            } else {
                onResponseListener.onError(exception);
            }
        }
    }

    public @interface Method {
        int GET = 0;
        int POST = 1;
        int PUT = 2;
        int DELETE = 3;
        int HEAD = 4;
        int OPTIONS = 5;
        int TRACE = 6;
    }

    public interface OnResponseListener {
        void onResponse(String responseContent, HttpResponse httpResponse);

        void onError(Exception error);
    }

    private class AsyncHttpTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progress != null && showProgress) progress.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            return executeHttpRequest();
        }

        @Override
        protected void onPostExecute(String result) {
            if (progress != null && showProgress) progress.dismiss();
            executeHttpResponse(result);
        }
    }

    private class SelfSSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public SelfSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(truststore);
            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            sslContext.init(null, new TrustManager[]{tm}, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }
}

