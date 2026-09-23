package com.rpz.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class Conf extends Properties{
    private static final Logger logger = LoggerFactory.getLogger(Conf.class);

    public Conf() {
        String conf = "app.properties";
        try {
            load(getRes(conf));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public InputStream getRes(String resource){
        InputStream in = ClassLoader.getSystemResourceAsStream(resource);
        if (in == null) {
            in = getClass().getClassLoader().getResourceAsStream(resource);
            if (in == null) throw new IllegalArgumentException("Resource file [" + resource + "] not found");
        }
        return in;
    }

    public static Conf getInstance(){return new Conf();}

    public static Json getConf(String conf) throws Exception {return Json.fromJson(CONF.getRes(conf));}

    public int getIntProperty(String prop) {
        return Integer.parseInt(getProperty(prop));
    }
    public int getIntProperty(String prop, String defaultValue) {
        return Integer.parseInt(getProperty(prop, defaultValue));
    }

    public long getLongProperty(String prop) {
        return Long.parseLong(getProperty(prop));
    }

    public long getLongProperty(String prop, String defaultValue) {
        return Long.parseLong(getProperty(prop, defaultValue));
    }

    public boolean getBooleanProperty(String prop) {
        return Boolean.parseBoolean(getProperty(prop));
    }

    public static String homePath(){
        String home = CONF.getProperty("app.home");
        String path = new File(home).getAbsolutePath();
        int len = path.length() - 1;
        if(path.charAt(len) == '.') path = path.substring(0, len--);
        return path + (path.charAt(len) == File.separatorChar? "" :File.separatorChar);
    }

    public static String keyPath(){
        return homePath() + "etc" + File.separatorChar + CONF.getProperty("app.ssl.keyfile");
    }

    public static String caPath(){
        return homePath() + "etc" + File.separatorChar + CONF.getProperty("app.ssl.cafile");
    }

    private static final Conf CONF = getInstance();

    public static final String HOME = homePath();

    public static final String HOST = CONF.getProperty("app.host");
    public static final Integer PORT = CONF.getIntProperty("app.port");
    public static final Boolean SSL = CONF.getBooleanProperty("app.ssl");

    static final String WEBAPP_URI; //webapp uri
    static final String REDIRECT_URI; //webapp uri

    static {
        final String WEB_HOST = CONF.getProperty("web.host");
        final String WEB_PORT = CONF.getProperty("web.port");
        WEBAPP_URI = (SSL?"https":"http")+"://"+WEB_HOST + (!Vld.isNull(WEB_PORT)?":"+WEB_PORT:"");
        REDIRECT_URI = WEBAPP_URI + CONF.getProperty("app.oauth.redirect");

        Vld.checkEmpty(HOME, "Home Path");
        Vld.checkNull(PORT, "Port");

        File f = new File(HOME);
        if(!f.exists() && !f.mkdir()){
            throw new IllegalStateException("Home path "+HOME+" cannot be created or permission denied!");
            //Files.createDirectories(Paths.get(home));
        }
        if(!f.isDirectory() || !f.canWrite())
            throw new IllegalStateException("Home path "+HOME+" does not exists or permission denied!");

        logger.info("Home: {}", homePath());
    }

}
