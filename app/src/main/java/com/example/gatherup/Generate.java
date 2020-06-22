package com.example.gatherup;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacUtils;

class Generate {
    public static final String PROVISION_TOKEN = "provision";
    private static final long EPOCH_SECONDS = 62167219200l;
    private static final String DELIM = "\0";

    public static String generateProvisionToken(String key, String jid, String expires) throws NumberFormatException {
        String payload = PROVISION_TOKEN + DELIM + jid + DELIM + calculateExpiry(expires) + DELIM ;
        return new String(Base64.encodeBase64(
                (payload + DELIM + HmacUtils.hmacSha384Hex(key, payload)).getBytes()
        ));
    }

    public static String calculateExpiry(String expires) throws NumberFormatException {
        long expiresLong = 0l;
        long currentUnixTimestamp = System.currentTimeMillis() / 1000;
        expiresLong = Long.parseLong(expires);
        return ""+(EPOCH_SECONDS + currentUnixTimestamp + expiresLong);
    }

    private static void printUsageAndExit() {
        System.out.println();
        System.out.println("This script will generate a provision login token from a developer key");
        System.out.println("Options:");
        System.out.println("--key           Developer key supplied with the developer account");
        System.out.println("--appID         ApplicationID supplied with the developer account");
        System.out.println("--userName      Username to generate a token for");
        System.out.println("--vCardFile     Path to the XML file containing a vCard for the user (optional)");
        System.out.println("--expiresInSecs Number of seconds the token will be valid");
        System.out.println();
        System.exit(1);
    }

    public static String generateToken(String key, String appID, String userName, String expiresInSeconds) {

        if (key == null) {
            System.out.println("key not set");
            printUsageAndExit();
        } else if (appID == null) {
            System.out.println("appID not set");
            printUsageAndExit();
        } else if (userName == null) {
            System.out.println("userName not set");
            printUsageAndExit();
        } else if (expiresInSeconds == null) {
            System.out.println("expiresInSecs not set");
            printUsageAndExit();
        }

        // calculate expiration
        String expires = "";
        if (expiresInSeconds != null) {
            expires = calculateExpiry(expiresInSeconds);
        }

        return generateProvisionToken(key, userName + "@" + appID, expires);

    }
}
