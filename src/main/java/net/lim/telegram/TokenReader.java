package net.lim.telegram;

import java.io.*;
import java.net.URL;

public class TokenReader {
    private static final String TOKEN_RESOURCE_NAME = "/bot.token";

    public static String getBotToken() {
        return readToken();
    }

    private static String readToken() {
        String token = getTokenFromSystemProp();
        if (token == null) {
            token = getTokenFromSystemEnv();
        }
        if (token == null) {
            token = getTokenFromResourceFile();
        }
        return token;
    }

    private static String getTokenFromResourceFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(checkTokenFileExistInResources()))) {
            String line = null;
            while (reader.ready()) {
                line = reader.readLine();
            }
            return line;
        } catch (FileNotFoundException ignored) {
            //should not be here
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private static String getTokenFromSystemProp() {
        return System.getProperty("bot.token.flag");
    }

    private static String getTokenFromSystemEnv() {
        return System.getenv("BOT_TOKEN");
    }

    private static String checkTokenFileExistInResources() {
        URL resourceUrl = TokenReader.class.getResource(TOKEN_RESOURCE_NAME);
        if (resourceUrl == null) throw new IllegalStateException("There's no " + TOKEN_RESOURCE_NAME + " file in the resources");
        return resourceUrl.getFile();
    }


}
