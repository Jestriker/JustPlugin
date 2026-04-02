package org.justme.justPlugin.managers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Loads and serves the web editor HTML from a resource file.
 * The HTML template uses {{AUTH_TOKEN}} and {{PORT}} placeholders
 * that are replaced at runtime. The UI can be edited by modifying
 * src/main/resources/web/editor.html without recompiling Java code.
 */
public final class WebEditorPage {

    private static final Logger LOGGER = Logger.getLogger(WebEditorPage.class.getName());

    /** Cached raw template loaded from resource. Null until first load. */
    private static String cachedTemplate;

    private WebEditorPage() {}

    /**
     * Get the editor HTML with the auth token and port placeholders replaced.
     *
     * @param authToken the authentication token to embed in the page
     * @param port      the web editor port number
     * @return the fully resolved HTML string
     */
    public static String getHtml(String authToken, int port) {
        String template = loadTemplate();
        return template
                .replace("{{AUTH_TOKEN}}", authToken != null ? authToken : "")
                .replace("{{PORT}}", String.valueOf(port));
    }

    /**
     * Backwards-compatible overload used by existing callers.
     * Uses port 0 as a placeholder when the port is not available.
     */
    public static String getHtml(String authToken) {
        return getHtml(authToken, 0);
    }

    /**
     * Load the HTML template from the resource file, caching it for subsequent calls.
     */
    private static String loadTemplate() {
        if (cachedTemplate != null) {
            return cachedTemplate;
        }
        try (InputStream is = WebEditorPage.class.getClassLoader().getResourceAsStream("web/editor.html")) {
            if (is == null) {
                LOGGER.severe("Could not find web/editor.html resource - web editor will not work");
                return "<html><body><h1>Error: editor.html resource not found</h1></body></html>";
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                cachedTemplate = reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load web/editor.html resource", e);
            return "<html><body><h1>Error loading editor</h1></body></html>";
        }
        return cachedTemplate;
    }

    /**
     * Clear the cached template so it will be reloaded on the next call.
     * Useful during development or after resource updates.
     */
    public static void clearCache() {
        cachedTemplate = null;
    }
}
