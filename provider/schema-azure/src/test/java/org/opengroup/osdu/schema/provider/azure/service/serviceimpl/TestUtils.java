package org.opengroup.osdu.schema.provider.azure.service.serviceimpl;

public class TestUtils {
    private static final String appId = "1234";
    public static final String APPID = "appid";
    public static final String aadIssuer = "https://sts.windows.net";
    public static final String aadIssuerV2 = "https://login.microsoftonline.com";
    public static final String nonAadIssuer = "https://login.abc.com";

    public static String getAppId() {return appId;}
    public static String getAadIssuer() {return aadIssuer;}
    public static String getAadIssuerV2() {return aadIssuerV2;}
    public static String getNonAadIssuer() {return nonAadIssuer;}
}
