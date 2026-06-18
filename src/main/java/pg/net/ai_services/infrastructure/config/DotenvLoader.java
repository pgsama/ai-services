package pg.net.ai_services.infrastructure.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class DotenvLoader {

    private static final String WINDOWS_DIR = "C:/dotenv/ai-services";
    private static final String UNIX_DIR = "/opt/dotenv/ai-services";

    private DotenvLoader() {
    }

    public static void load() {
        Path envPath = resolveEnvPath();
        if (!Files.isRegularFile(envPath)) {
            return;
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(envPath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read .env file at " + envPath, e);
        }

        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int eq = line.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String key = line.substring(0, eq).trim();
            String value = stripQuotes(line.substring(eq + 1).trim());

            if (System.getenv(key) == null && System.getProperty(key) == null) {
                System.setProperty(key, value);
            }
        }
    }

    private static Path resolveEnvPath() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String dir = os.contains("win") ? WINDOWS_DIR : UNIX_DIR;
        return Path.of(dir, ".env");
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2
                && ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'")))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
