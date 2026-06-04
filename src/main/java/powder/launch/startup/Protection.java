package powder.launch.startup;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Lightweight runtime integrity / anti-tamper self-check.
 *
 * In a development environment (classes loaded from a directory) the check is
 * skipped. In a packaged jar it recomputes a SHA-256 over the mod's own class
 * entries and compares it against the value baked into {@code protection.key}
 * at release time. A mismatch means the jar was modified after release, and the
 * mod refuses to initialize its features.
 */
public final class Protection {

    private static boolean checked;
    private static boolean verified;

    // "protection.key" obfuscated with XOR 0x5A so it is not a plain string in the bytecode.
    private static final byte[] KEY_NAME = {
            0x2A, 0x28, 0x35, 0x2E, 0x3F, 0x39, 0x2E, 0x33, 0x35, 0x34, 0x74, 0x31, 0x3F, 0x23
    };

    private Protection() {
    }

    public static boolean verify() {
        if (checked) return verified;
        checked = true;
        verified = check();
        return verified;
    }

    private static String keyName() {
        byte[] out = new byte[KEY_NAME.length];
        for (int i = 0; i < out.length; i++) out[i] = (byte) (KEY_NAME[i] ^ 0x5A);
        return new String(out, StandardCharsets.US_ASCII);
    }

    private static boolean check() {
        try {
            URI location = Protection.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            File file = new File(location);

            if (file.isDirectory() || !file.getName().toLowerCase().endsWith(".jar"))
                return true;

            String expected;
            try (InputStream in = Protection.class.getResourceAsStream("/" + keyName())) {
                if (in == null) return false;
                expected = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
            }

            if (expected.isEmpty() || expected.startsWith("PLACEHOLDER"))
                return true;

            return expected.equalsIgnoreCase(hashClasses(file));
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static String hashClasses(File jar) throws Exception {
        List<String> names = new ArrayList<>();

        try (ZipFile zip = new ZipFile(jar)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith("powder/") && name.endsWith(".class"))
                    names.add(name);
            }
            Collections.sort(names);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (String name : names) {
                try (InputStream in = zip.getInputStream(zip.getEntry(name))) {
                    digest.update(in.readAllBytes());
                }
            }

            StringBuilder builder = new StringBuilder();
            for (byte b : digest.digest()) builder.append(String.format("%02x", b));
            return builder.toString();
        }
    }

}
