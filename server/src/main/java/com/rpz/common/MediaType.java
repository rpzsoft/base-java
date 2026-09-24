package com.rpz.common;


import jakarta.activation.MimeTypeParameterList;
import jakarta.activation.MimeTypeParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Refer: https://tools.ietf.org/html/rfc7231#section-3.1.1
 *        https://tools.ietf.org/html/rfc6838
 *
 *        http://www.iana.org/assignments/media-types/media-types.xhtml
 *
 * The type, subtype, and parameter name tokens are case-insensitive. Parameter values might or might not be
 * case-sensitive, depending on the semantics of the parameter name.  The presence or absence of a parameter might be
 * significant to the processing of a media-type, depending on its definition within the media type registry.
 *
 * [RFC2046] specifies that Media Types (formerly known as MIME types) and Media Subtypes will be assigned and listed
 * by the IANA and found here: https://www.iana.org/assignments/media-types/media-types.xhtml
 *
 * FIXME: Following definition is not exhaustive enough
 *
 * allows names of up to 127 characters, implementation limits may make such long names problematic.
 * For this reason, <type-name> and <subtype-name> SHOULD be limited to 64 characters.
 *
 * 
 */
public class MediaType {

    //application/x-www-form-urlencoded
    //multipart/form-data;boundary="boundary"
    /*e.g.
        POST /test.html HTTP/1.1
        Host: example.org
        Content-Type: multipart/form-data;boundary="boundary"

        --boundary
        Content-Disposition: form-data; name="field1"

        value1
        --boundary
        Content-Disposition: form-data; name="field2"; filename="example.txt"

        value2
     */

    public static final MediaType ALL = new MediaType("*", "*");

    public static final MediaType TEXT_ALL = new MediaType("text", "*");
    public static final MediaType TEXT_PLAIN = new MediaType("text", "plain", "; charset=utf-8");
    public static final MediaType TEXT_CSS = new MediaType("text", "css");
    public static final MediaType TEXT_HTML = new MediaType("text", "html", "; charset=utf-8"); //text/html; charset=iso-8859-1
    public static final MediaType TEXT_XML = new MediaType("text", "xml", "; charset=utf-8");
    public static final MediaType TEXT_CSV = new MediaType("text", "csv");
    public static final MediaType TEXT_JS = new MediaType("text", "javascript", "; charset=utf-8");
    public static final MediaType TEXT_WML = new MediaType("text", "vnd.wap.wml");
    public static final MediaType TEXT_SSE = new MediaType("text", "event-stream");//text/event-stream

    public static final MediaType IMG_JPEG = new MediaType("image", "jpeg");
    public static final MediaType IMG_TIFF = new MediaType("image", "gif");
    public static final MediaType IMG_GIT = new MediaType("image", "tiff");
    public static final MediaType IMG_PNG = new MediaType("image", "png");
    public static final MediaType IMG_ICON = new MediaType("image", "vnd.microsoft.icon");
    public static final MediaType IMG_SVG = new MediaType("image", "svg+xml");

    public static final MediaType AUDIO_MPEG = new MediaType("audio", "mpeg");
    public static final MediaType AUDIO_WAV = new MediaType("audio", "x-wav");
    public static final MediaType AUDIO_WMA = new MediaType("audio", "x-ms-wma");

    public static final MediaType VID_JPEG = new MediaType("video", "mpeg");
    public static final MediaType VID_MP4 = new MediaType("video", "mp4");
    public static final MediaType VID_FLV = new MediaType("video", "flv");
    public static final MediaType VID_WEBM = new MediaType("video", "webm");

    public static final MediaType APPLICATION_ALL = new MediaType("application", "*");
    public static final MediaType APPLICATION_OCTET_STREAM = new MediaType("application", "octet-stream");
    public static final MediaType APPLICATION_JSON = new MediaType("application", "json", "; charset=utf-8");
    public static final MediaType APPLICATION_GEO_JSON = new MediaType("application", "geo+json", "; charset=utf-8");
    public static final MediaType APPLICATION_XML = new MediaType("application", "xml", "; charset=utf-8");
    public static final MediaType APPLICATION_ATOM_XML = new MediaType("application", "atom+xml", "; charset=utf-8");
    public static final MediaType APPLICATION_RSS_XML = new MediaType("application", "rss+xml", "; charset=utf-8");
    public static final MediaType APPLICATION_RDF_XML = new MediaType("application", "rdf+xml", "; charset=utf-8");
    public static final MediaType APPLICATION_XHTML = new MediaType("application", "xhtml+xml", "; charset=utf-8");
    public static final MediaType APPLICATION_FORM = new MediaType("application", "x-www-form-urlencoded", "; charset=utf-8");
    public static final MediaType MULTIPART_FORM = new MediaType("multipart", "form-data");//, "; boundary=\"0EA\""

    //Form multi protocol usage as byte for low level & internal usage
    public static final byte RPZ_MT_ANY=0x00;
    public static final byte RPZ_MT_AP_JSON =0x01;		    /* application/json; charset=utf-8 */
    public static final byte RPZ_MT_TXT_PLAIN =0x02;	    /* text/plain; charset=utf-8 */
    public static final byte RPZ_MT_TXT_HTML=0x03;	        /* text/plain; charset=utf-8 */
    public static final byte RPZ_MT_AP_XML =0x04;		    /* TODO: application/xml; charset=utf-8 */
    public static final byte RPZ_MT_AP_OPAQUE =0x05;	    /* application/octet-stream - this represents all other types */
    public static final byte RPZ_MT_AP_ATOM_XML =0x06;	    /* application/atom+xml */
    public static final byte RPZ_MT_AP_RDF_XML =0x07;	    /* application/rdf+xml*/
    public static final byte RPZ_MT_AP_RSS_XML =0x08;	    /* application/rss+xml  */


    String type;
    byte mtype;
    String subtype;
    List<Pair<String, String>> parameters;

    String mainType;
    String str;


    // FIXME: remove
    static final String PUNC = "()<>@,;:/[]?=\\\"";

    static boolean isTokenChar(char c) {
        return ((c > 040) && (c < 0177)) && (PUNC.indexOf(c) < 0);
    }

    /**
     * TODO: Need new algo
     * @param value
     * @return
     */
    static String quote(String value) {
        boolean needsQuotes = false;

        //    check to see if we actually have to quote this thing
        int length = value.length();
        for (int i = 0; (i < length) && !needsQuotes; i++) {
            needsQuotes = !isTokenChar(value.charAt(i));
        }

        if (needsQuotes) {
            StringBuffer buffer = new StringBuffer();
            buffer.ensureCapacity((int) (length * 1.5));

            //    add the initial quote
            buffer.append('"');

            //    add the properly escaped text
            for (int i = 0; i < length; ++i) {
                char c = value.charAt(i);
                if ((c == '\\') || (c == '"'))
                    buffer.append('\\');
                buffer.append(c);
            }

            //    add the closing quote
            buffer.append('"');

            return buffer.toString();
        } else {
            return value;
        }
    }

    public MediaType(String type){
        int loc = type.indexOf("/");
        if(loc < 0) throw new IllegalArgumentException("Invalid MediaType");
        int col = type.indexOf(";");
        this.type = type.substring(0, loc);
        this.subtype = (col > 0) ? type.substring(loc+1, col): type.substring(loc+1);
        mainType = type;
        str = mainType;
        mtype=code(mainType);

    }

    public MediaType(String type, String subtype){
        this(type, subtype, null);
        mtype=code(mainType);

    }
    
    public MediaType(String type, String subtype, String parameters) {
        this.type = type;
        this.subtype = subtype;
        mainType = type + (subtype!=null? "/" + subtype:"");
        str = mainType;
        if (parameters != null) {
            if(this.parameters == null) this.parameters = new ArrayList<>(2);

            MimeTypeParameterList params = null;
            try {
                params = new MimeTypeParameterList(parameters);
            } catch (MimeTypeParseException e) {
                throw new IllegalStateException(e);
            }

            str += parameters;
            Enumeration keys = params.getNames();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                this.parameters.add(new Pair<>(key, quote((String) params.get(key))));
            }
        }
        mtype=code(mainType);
    }

    //FIXME: need to relook at the possible invalid match cases
    public boolean  matches(String type) {
        if(mainType.equals(ALL.mainType)) return true; //any
        if (type == null || type.isEmpty()) return false;
        type = type.toLowerCase();
        String [] prm = type.split(";");
        if(prm.length == 0) return false;
        if(prm.length == 1) return mainType.equals(type);
        if(!mainType.equals(prm[0])) return false;
        if(prm.length > 2) return true; //partial match
        return str.equals(prm[0].trim()+"; "+prm[1].trim())
                || str.equals(prm[0].trim()+";"+prm[1].trim()); //without white space
    }

    public boolean matches(MediaType type) {
        return matches(type.toString());
    }

    //main types only for partial validation - allow determining if the media/content is binary or text
    public boolean mainmatches(String type) {
        if(mainType.equals(ALL.mainType)) return true; //any
        if (type == null || type.isEmpty()) return false;
        type = type.toLowerCase();
        String [] prm = type.split(";");
        if(prm.length == 0) return false;
        if(prm.length == 1 && mainType.equals(type)) return true;
        return type.startsWith(mainType.substring(0, mainType.indexOf("/")+1));
    }

    //FIXME: simple & quick solution for now
    public static boolean isBinary(String type){
        Vld.checkEmpty(type, "Media Type");
        return type.startsWith(APPLICATION_OCTET_STREAM.toString()) || type.startsWith("image/") || type.startsWith("audio/")
                || type.startsWith("video/");
    }

    public static boolean isBinary(MediaType type){
        return type.matches(APPLICATION_OCTET_STREAM) || type.mainmatches("image") || type.mainmatches("audio")
                || type.mainmatches("video");
    }

    public static boolean isFormData(String type){
        Vld.checkEmpty(type, "Media Type");
        return type.startsWith(MULTIPART_FORM.toString());
    }

    public static boolean isFormData(MediaType type){
        return type.matches(MULTIPART_FORM);
    }

    public static boolean isBinary(byte code){
        return code == RPZ_MT_AP_OPAQUE;
    }

    @Override
    public final String toString() {
        if(str == null) {
            StringBuilder mt = new StringBuilder(type);
            mt.append('/').append(subtype);
            if (parameters != null) {
                mt.append(";");
                for (Pair<String, String> p : parameters)
                    mt.append(' ').append(p.key).append('=').append(p.value);
            }

            str =  mt.toString();
        }
        return str;
    }

    public final String str(){
        if(str == null) {
            str = toString();
        }
        return str;
    }

    public static byte[] defaultData(MediaType type){
        if(type.matches(APPLICATION_JSON) || type.matches(APPLICATION_GEO_JSON)) return "{}".getBytes();
        if(type.matches(APPLICATION_XML) || type.matches(TEXT_XML)) return "</>".getBytes();
        return new byte[0];
    }

    public static byte code(String value) {
        int i =  value.indexOf('/');
        if(i > 0){
            i++;
            char ch = value.charAt(i);
            if(ch == 'j') return  MediaType.RPZ_MT_AP_JSON;
            else if(ch == 'p') return  MediaType.RPZ_MT_TXT_PLAIN;
            else if(ch == 'o') return  MediaType.RPZ_MT_AP_OPAQUE;
            else if(ch == 'x') return  MediaType.RPZ_MT_AP_XML;
            else if(ch == 'a') return  MediaType.RPZ_MT_AP_ATOM_XML;
            else if(ch == 'r' && value.charAt(i+1) == 'd') return  MediaType.RPZ_MT_AP_RDF_XML;
            else if(ch == 'r' && value.charAt(i+1) == 's') return  MediaType.RPZ_MT_AP_RSS_XML;
            else if(ch == '*') return  MediaType.RPZ_MT_ANY;
        }else{
            switch (value.substring(0,i)){
                case "text": return  MediaType.RPZ_MT_TXT_PLAIN;
            }
        }
        return  MediaType.RPZ_MT_ANY;
    }

    public static MediaType mtype(byte code) {
        switch (code){
            case  RPZ_MT_ANY: return ALL;
            case  RPZ_MT_AP_JSON: return APPLICATION_JSON;
            case  RPZ_MT_TXT_PLAIN: return TEXT_PLAIN;
            case  RPZ_MT_TXT_HTML: return TEXT_HTML;
            case  RPZ_MT_AP_XML: return APPLICATION_XML;
            case  RPZ_MT_AP_OPAQUE: return APPLICATION_OCTET_STREAM;
            case  RPZ_MT_AP_ATOM_XML: return APPLICATION_ATOM_XML;
            case  RPZ_MT_AP_RDF_XML: return APPLICATION_RDF_XML;
            case  RPZ_MT_AP_RSS_XML: return APPLICATION_RSS_XML;
        }
        return  ALL;
    }

    public boolean  matches(byte code) {
        return mtype == code;
    }
}
