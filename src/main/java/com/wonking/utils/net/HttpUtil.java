package com.wonking.utils.net;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by kewangk on 2017/11/30.
 * 基于新的httpclient框架实现，commons-httpclient已不被支持
 * 实现池化管理,并且支持代理,支持免证书访问https
 * 依赖包:
 * org.apache.httpcomponents.httpclient -核心组件
 * org.apache.httpcomponents.httpcore -同样是核心组件，与实体类相关
 * org.apache.httpcomponents.httpmime -httpmime主要用于构建multi-part/form-data
 */
public class HttpUtil {

    //从连接池中获取连接的超时时间
    private static final int CONNECTION_REQUEST_TIMEOUT=2000;
    //连接上服务器(握手成功)的超时时间,超出该时间抛出connect timeout
    private static final int CONNECTION_TIMEOUT=2000;
    //服务器返回数据(response)的超时时间,超过该时间抛出read timeout
    private static final int SOCKET_TIMEOUT=2000;

    private static final String PROXY_HOST="127.0.0.1";
    private static final int PROXY_PORT=8080;

    //httpclient请求池
    //连接池针对每一个url会保持一个唯一的长连接
    private static final PoolingHttpClientConnectionManager poolManager;

    //请求参数
    private static final RequestConfig requestConfig;

    //代理节点
    private static HttpHost proxy;

    static{
        SSLContext sslContext;
        SSLConnectionSocketFactory scsf;
        try{
            X509TrustManager trustManager=new X509TrustManager() {
                //免去所有客户端验证
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {}
                //免去所有服务端验证
                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {}

                @Override
                public X509Certificate[] getAcceptedIssuers() {return null;}
            };
            sslContext=SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustManager},null);
            scsf=new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        }catch (Exception e){
            scsf=SSLConnectionSocketFactory.getSocketFactory();
        }

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", scsf)
                .build();
        poolManager=new PoolingHttpClientConnectionManager(socketFactoryRegistry);

        //可以在这里设置代理，不过由于我把requestConfig当作全局的参数，有的请求可能不需要代理
        //所以还是针对每一个请求去设置
        requestConfig=RequestConfig.custom()
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                .setConnectTimeout(CONNECTION_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build();

        proxy=new HttpHost(PROXY_HOST, PROXY_PORT);
    }

    private static void init(){}

    //从连接池中取出一个连接
    private static CloseableHttpClient getHttpClient(String url){
        if(needProxy(url)){
            return HttpClients.custom()
                    .setProxy(proxy)
                    .setConnectionManager(poolManager)
                    .setDefaultRequestConfig(requestConfig)
                    .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
                    .build();
        }
        return HttpClients.custom().setConnectionManager(poolManager).setDefaultRequestConfig(requestConfig).build();
    }

    private static boolean needProxy(String url){
        return false;
    }

    //统一get和post请求，执行实际请求操作
    private static String doRequest(HttpClient httpClient, HttpUriRequest request){
        try {
            HttpResponse response=httpClient.execute(request);
            HttpEntity entity=response.getEntity();
            String result=EntityUtils.toString(entity,"UTF-8");
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        } /*finally {
            if(httpClient instanceof CloseableHttpClient){
                try {
                    ((CloseableHttpClient)httpClient).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }*/  //因为是供代码中调用的工具类，往往只会与少数host进行连接，所以不要手动关闭，因为连接池需要保持长连接
        //手动关闭连接,连接池并不能感应到连接已关闭,会抛出这个异常: java.lang.IllegalStateException: Connection pool shut down
        return null;
    }

    //清理连接，如果在一次大量连续请求过后，预期之后一段时间不会再使用，就调用此方法
    public static void clearConnection(){
        //关闭空闲连接，可能内部实现是这样的，从现在开始算起，超过给定时间，就关闭连接
        poolManager.closeIdleConnections(0, TimeUnit.SECONDS);
        //关闭过期连接
        poolManager.closeExpiredConnections();
    }

    public static String get(String url, Map<String,Object> param){
        String queryString=toQueryString(param);
        String finalUrl= queryString==null? url:url+"?"+queryString;
        HttpGet get=new HttpGet(finalUrl);
        CloseableHttpClient httpClient=getHttpClient(url);
        return doRequest(httpClient, get);
    }

    //body是请求体，目前只支持json串
    public static String post(String url, String body){
        HttpPost post=new HttpPost(url);

        //这里不需要自己设置请求头及编码，直接用ContentType可以解决,去看源码就知道了
        /*post.addHeader("Content-Type","application/json");
        ContentType contentType=ContentType.create(body, "UTF-8");
        HttpEntity entity=new StringEntity(body, contentType);*/
        if(StringUtils.isNotBlank(body)){
            HttpEntity entity=new StringEntity(body, ContentType.APPLICATION_JSON);
            post.setEntity(entity);
        }
        CloseableHttpClient httpClient=getHttpClient(url);
        return doRequest(httpClient, post);
    }

    //这里先不实现，因为把查询字符串放到post中不太常用,而且我也讨厌这样用，除非是为了优雅的在地址栏显示地址
    //将查询参数置于body中的请求方式，如：key=value&key1=value1，并且通常会对key和value进行URLEncode编码
    //注：key和value都会编码，连接符=和&符号不会编码，但如果key和value中有这些特殊字符，会被编码
    public static String postKeyValue(String url, Map<String,String> param){
        HttpPost post=new HttpPost(url);
        //name-value参数放到请求体时，一般会进行urlencode编码，可能是大家都遵守的一种协议吧
        //具体到最底层，对于body，必须设置2个属性（对应于一个header），一，数据类型，二，数据编码。如果自己没设置，一定有一种默认属性
        //所以下面这种方式使用StringEntity与UrlEncodedFormEntity效果是一样的
        //其实我更偏向于使用StringEntity，因为这更灵活，对底层细节了解更多
        //StringEntity stringEntity=new StringEntity("body", ContentType.APPLICATION_FORM_URLENCODED.withCharset(Consts.ISO_8859_1));
        UrlEncodedFormEntity entity=new UrlEncodedFormEntity(toNameValuePair(param), Consts.UTF_8);
        post.setEntity(entity);
        CloseableHttpClient httpClient=getHttpClient(url);
        return doRequest(httpClient, post);
    }

    //其实这个https完全可以和http合一起，因为连接池中已经注册了https的免证书访问配置
    public static String httpsGet(String url, Map<String,Object> param){
        return null;
    }
    public static String httpsPost(String url, String body){
        return null;
    }

    private static String toQueryString(Map<String,Object> param){
        StringBuilder sb = null;
        if (param != null && param.size() > 0) {
            sb = new StringBuilder("");
            for(Map.Entry<String,Object> entry : param.entrySet()){
                try {
                    String key = entry.getKey()== null ? "" : entry.getKey();
                    Object value = entry.getValue();
                    sb.append(key).append("=").append(URLEncoder.encode(value.toString(), "UTF-8")).append("&");
                } catch (UnsupportedEncodingException e1) {
                    //should never happen
                }
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb==null?null:sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static List<NameValuePair> toNameValuePair(Map<String,String> param){
        if(param==null || param.size()==0){
            return Collections.EMPTY_LIST;
        }
        List<NameValuePair> result= new ArrayList<>(param.size());
        param.forEach((s,o)->result.add(new BasicNameValuePair(s, o)));
        return result;
    }

    public static void main(String[] args){
        //testNameValuePair();
        testPostKeyValue();
    }

    private static void testPostKeyValue(){
        Map<String,String> param=new HashMap<>();
        param.put("refer","http://e.qq.com/ec/api.php?name=wonking&pwd=123456#index=1");
        param.put("data?","{\"512566\",\"401\"}");
        String url="http://e.qq.com/atlas/7365168/report/order?cid=10499707&sdate=2018-05-18&edate=2018-05-18";
        System.out.println(postKeyValue(url, param));
    }

    private static void testNameValuePair(){
        Map<String,String> param=new HashMap<>();
        param.put("refer","http://e.qq.com/ec/api.php?name=wonking&pwd=123456#index=1");
        param.put("data","{\"512566\",\"401\"}");
        System.out.println(toNameValuePair(param));
    }

    private static void testRunningTime(){
        long start= System.currentTimeMillis();
        String url="http://e.qq.com/ec/api.php";
        String url3="https://ssl.ptlogin2.qq.com/ptqrshow";
        String url4="http://e.qq.com/ec/loginfo.php";
        //String resp=get(url, null);
        int count=10;
        String resp=null;
        while ((count--)>0){
            resp=get(url, null);
            //每次请求完立马释放连接，下次需要重新建立连接
            // 若把clearConnection注释掉，对比前后耗时，连接池的优势立马显现出来，前者耗时几乎是后者2倍
            //clearConnection();
            resp=get(url,null);
            //clearConnection();
            resp=get(url3,null);
            //clearConnection();
            resp=get(url4, null);
            //clearConnection();
            System.out.println(resp);
            //resp=get(url2,null);
            //resp=post(url, null);
        }
        //System.out.println(resp);
        System.out.println("cost time->"+(System.currentTimeMillis()-start));
    }

}
