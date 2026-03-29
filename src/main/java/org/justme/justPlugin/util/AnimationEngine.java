package org.justme.justPlugin.util;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitTask;
import org.justme.justPlugin.JustPlugin;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides text animation effects for scoreboard and tab list.
 * Animations are registered by name and rendered per-frame.
 * <p>
 * Supported animation types:
 * <ul>
 *   <li><b>rainbow_gradient</b> - Cycling rainbow gradient across the text</li>
 *   <li><b>typing</b> - Text revealed one character at a time</li>
 *   <li><b>sweep_right</b> - Highlight color sweeps left-to-right</li>
 *   <li><b>sweep_left</b> - Highlight color sweeps right-to-left</li>
 *   <li><b>wave</b> - Sine-wave color ripple across characters</li>
 * </ul>
 * <p>
 * Usage in scoreboard/tab: {@code {anim:name}} placeholder.
 */
public class AnimationEngine {

    private final JustPlugin plugin;
    private final Map<String, AnimationDef> animations = new HashMap<>();
    private int globalTick = 0;
    private BukkitTask tickTask;

    public AnimationEngine(JustPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register a named animation.
     *
     * @param name   unique name (referenced as {anim:name})
     * @param type   animation type: rainbow_gradient, typing, sweep_right, sweep_left, wave
     * @param text   the text to animate
     * @param speed  ticks per frame advance (lower = faster)
     * @param color1 primary color hex (optional, used by sweep types)
     * @param color2 highlight/secondary color hex (optional)
     */
    public void register(String name, String type, String text, int speed, String color1, String color2) {
        animations.put(name.toLowerCase(), new AnimationDef(type.toLowerCase(), text, Math.max(1, speed), color1, color2));
    }

    /** Remove all registered animations. */
    public void clear() {
        animations.clear();
    }

    /** Start the global tick timer. Call once after all animations are registered. */
    public void start() {
        if (tickTask != null) tickTask.cancel();
        tickTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> globalTick++, 1L, 1L);
    }

    /** Stop the tick timer. */
    public void stop() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
    }

    /**
     * Render the current frame for a named animation.
     * @return the MiniMessage-formatted string for this frame, or the raw name if not found
     */
    public String render(String name) {
        AnimationDef def = animations.get(name.toLowerCase());
        if (def == null) return "{anim:" + name + "}";

        int frame = globalTick / def.speed;
        String text = def.text;
        if (text == null || text.isEmpty()) return "";

        return switch (def.type) {
            case "rainbow_gradient" -> renderRainbowGradient(text, frame);
            case "typing" -> renderTyping(text, frame);
            case "sweep_right" -> renderSweep(text, frame, false, def.color1, def.color2);
            case "sweep_left" -> renderSweep(text, frame, true, def.color1, def.color2);
            case "wave" -> renderWave(text, frame, def.color1, def.color2);
            default -> text;
        };
    }

    /**
     * Resolve all {anim:xxx} placeholders in the input string.
     */
    public String resolveAll(String input) {
        if (input == null || !input.contains("{anim:")) return input;

        StringBuilder sb = new StringBuilder(input.length());
        int len = input.length();
        int i = 0;
        while (i < len) {
            int start = input.indexOf("{anim:", i);
            if (start == -1) {
                sb.append(input, i, len);
                break;
            }
            sb.append(input, i, start);
            int end = input.indexOf('}', start);
            if (end == -1) {
                sb.append(input, start, len);
                break;
            }
            String animName = input.substring(start + 6, end); // after "{anim:"
            sb.append(render(animName));
            i = end + 1;
        }
        return sb.toString();
    }

    /** Check if any animations are registered. */
    public boolean hasAnimations() {
        return !animations.isEmpty();
    }

    /**
     * Load animations from a YAML config section.
     * Expected format:
     * <pre>
     * animations:
     *   my_rainbow:
     *     type: rainbow_gradient
     *     text: "Server"
     *     speed: 2
     *     color1: "#ff0000"
     *     color2: "#00ff00"
     * </pre>
     */
    public void loadAnimations(ConfigurationSection section) {
        clear();
        if (section == null) return;
        for (String name : section.getKeys(false)) {
            ConfigurationSection anim = section.getConfigurationSection(name);
            if (anim == null) continue;
            String type = anim.getString("type", "rainbow_gradient");
            String text = anim.getString("text", "");
            int speed = anim.getInt("speed", 2);
            String color1 = anim.getString("color1", "#aaaaaa");
            String color2 = anim.getString("color2", "#ffffff");
            register(name, type, text, speed, color1, color2);
        }
    }

    // ==================== Animation Renderers ====================

    /**
     * Rainbow gradient - cycles the hue offset each frame, creating a moving rainbow.
     */
    private String renderRainbowGradient(String text, int frame) {
        int len = text.length();
        if (len == 0) return "";

        StringBuilder sb = new StringBuilder();
        float hueOffset = (frame % 360) / 360f;

        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                sb.append(' ');
                continue;
            }
            float hue = (hueOffset + (float) i / Math.max(len, 1)) % 1.0f;
            Color color = Color.getHSBColor(hue, 0.8f, 1.0f);
            sb.append(String.format("<color:#%02x%02x%02x>%c</color>", color.getRed(), color.getGreen(), color.getBlue(), c));
        }
        return sb.toString();
    }

    /**
     * Typing animation - reveals one character per frame, then holds, then resets.
     * Full text is visible for the same duration as the typing phase.
     */
    private String renderTyping(String text, int frame) {
        int len = text.length();
        int totalCycle = len * 2 + 10; // type in + hold + pause
        int pos = frame % totalCycle;

        int visibleChars;
        if (pos < len) {
            visibleChars = pos + 1; // typing phase
        } else {
            visibleChars = len; // hold phase
        }

        String visible = text.substring(0, visibleChars);
        // Pad with dark gray dots to maintain width stability
        int padding = len - visibleChars;
        StringBuilder sb = new StringBuilder(visible);
        if (padding > 0) {
            sb.append("<dark_gray>");
            sb.append(".".repeat(Math.min(3, padding))); // max 3 dots as cursor indicator
            sb.append("</dark_gray>");
        }
        return sb.toString();
    }

    /**
     * Sweep - one character is highlighted while the rest stay in the base color.
     * The highlight sweeps across the text.
     */
    private String renderSweep(String text, int frame, boolean reverse, String baseHex, String highlightHex) {
        int len = text.length();
        if (len == 0) return "";

        String base = (baseHex != null && !baseHex.isEmpty()) ? baseHex : "#aaaaaa";
        String highlight = (highlightHex != null && !highlightHex.isEmpty()) ? highlightHex : "#ffffff";

        int pos = frame % len;
        if (reverse) pos = len - 1 - pos;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                sb.append(' ');
                continue;
            }
            if (i == pos) {
                sb.append("<color:").append(highlight).append("><bold>").append(c).append("</bold></color>");
            } else {
                // Glow effect: adjacent chars get a lighter color
                int dist = Math.abs(i - pos);
                if (dist == 1) {
                    sb.append("<color:").append(highlight).append(">").append(c).append("</color>");
                } else {
                    sb.append("<color:").append(base).append(">").append(c).append("</color>");
                }
            }
        }
        return sb.toString();
    }

    /**
     * Wave - sine-wave color ripple. Each character gets a phase-offset gradient
     * between two colors, creating a wave pattern that moves across the text.
     */
    private String renderWave(String text, int frame, String color1Hex, String color2Hex) {
        int len = text.length();
        if (len == 0) return "";

        Color c1, c2;
        try {
            c1 = Color.decode(color1Hex != null && !color1Hex.isEmpty() ? color1Hex : "#00aaff");
            c2 = Color.decode(color2Hex != null && !color2Hex.isEmpty() ? color2Hex : "#00ffaa");
        } catch (Exception e) {
            c1 = new Color(0, 170, 255);
            c2 = new Color(0, 255, 170);
        }

        StringBuilder sb = new StringBuilder();
        double waveSpeed = 0.3;
        double waveLength = 4.0;

        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                sb.append(' ');
                continue;
            }
            // Sine wave: 0.0 to 1.0 oscillation per character
            double phase = Math.sin((i / waveLength) + (frame * waveSpeed)) * 0.5 + 0.5;

            int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * phase);
            int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * phase);
            int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * phase);
            r = Math.max(0, Math.min(255, r));
            g = Math.max(0, Math.min(255, g));
            b = Math.max(0, Math.min(255, b));

            sb.append(String.format("<color:#%02x%02x%02x>%c</color>", r, g, b, c));
        }
        return sb.toString();
    }

    // ==================== Data ====================

    private record AnimationDef(String type, String text, int speed, String color1, String color2) {}
}



