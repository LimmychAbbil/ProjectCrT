package net.lim.telegram;

import java.io.*;
import java.net.URL;

public class TokenReader {
    private static final String TOKEN_RESOURCE_NAME = "/bot.token";

    public static String getBotToken() {
        return readToken();
    }

    private static String readToken() {
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

    private static String checkTokenFileExistInResources() {
        URL resourceUrl = TokenReader.class.getResource(TOKEN_RESOURCE_NAME);
        if (resourceUrl == null) throw new IllegalStateException("There's no " + TOKEN_RESOURCE_NAME + " file in the resources");
        return resourceUrl.getFile();
    }


}
