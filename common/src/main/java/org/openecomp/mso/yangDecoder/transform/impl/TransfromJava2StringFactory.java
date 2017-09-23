package org.openecomp.mso.yangDecoder.transform.impl;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.openecomp.mso.yangDecoder.transform.api.ITransformJava2StringService;

/**
 * Created by 10112215 on 2017/3/24.
 */
public class TransfromJava2StringFactory {

    private static TransformJava2JsonServiceImpl java2jsonService = (new TransformJava2JsonFactory()).getJava2jsonService();

    private static TransformJava2XMLServiceImpl java2XMLService/* = (new TransformJava2XMLFactory()).getJava2xmlService()*/;

    public static boolean isXML(String value) {
        try {
            DocumentHelper.parseText(value);
        } catch (DocumentException e) {
            return false;
        }
        return true;
    }

    public static ITransformJava2StringService getJava2StringService(String input) throws Exception {
        ITransformJava2StringService java2jsonService;
        if (isXML(input)) {
            java2jsonService = getJava2XMLService();
        } else {
            java2jsonService = getJava2jsonService();
        }
        return java2jsonService;
    }

    public static void init() {
        // do no shit for static initialization
    }

    public static ITransformJava2StringService getJava2jsonService() throws Exception {
        if (java2jsonService == null) {
            TransformJava2JsonFactory transformJava2JsonFactory = new TransformJava2JsonFactory();
            java2jsonService = transformJava2JsonFactory.getJava2jsonService();
        }
        return java2jsonService;
    }

    public static ITransformJava2StringService getJava2XMLService() throws Exception {
        if (java2XMLService == null) {
            TransformJava2XMLFactory transformJava2XMLFactory = new TransformJava2XMLFactory();
            java2XMLService = transformJava2XMLFactory.getJava2xmlService();
        }
        return java2XMLService;
    }

}
