package main;

import java.awt.Color;

public class ColorUtils {

    /**
     * Gibt die bestmögliche Kontrastfarbe zurück.
     * Für farbige Werte wird die HSB-Komplementärfarbe genutzt.
     * Für Grau/Schwarz/Weiß oder sehr entsättigte Farben wird automatisch
     * Schwarz oder Weiß für optimalen Kontrast gewählt.
     */
    public static Color getOptimalContrastColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("Color darf nicht null sein.");
        }

        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);

        boolean isGray =
                hsb[1] < 0.15f ||              // kaum Sättigung
                (color.getRed() == color.getGreen() &&
                 color.getGreen() == color.getBlue()); // echtes Grau

        if (isGray) {
            // Für Graustufen: optimalen Schwarz/Weiß-Kontrast wählen
            return getBestBlackOrWhiteContrast(color);
        }

        // Farbiger Wert: HSB-Komplementärfarbe
        float complementaryHue = (hsb[0] + 0.5f) % 1.0f;

        return Color.getHSBColor(complementaryHue, hsb[1], hsb[2]);
    }

    /**
     * Berechnet automatisch, ob Schwarz oder Weiß besseren Kontrast liefert.
     */
    private static Color getBestBlackOrWhiteContrast(Color color) {
        double brightness = calculateRelativeLuminance(color);

        // Wenn die Farbe hell ist → Schwarz
        // Wenn die Farbe dunkel ist → Weiß
        return (brightness > 0.5) ? Color.BLACK : Color.WHITE;
    }

    /**
     * Relative Helligkeit gemäß WCAG 2.0.
     */
    private static double calculateRelativeLuminance(Color c) {
        double r = c.getRed()   / 255.0;
        double g = c.getGreen() / 255.0;
        double b = c.getBlue()  / 255.0;

        r = (r <= 0.03928) ? (r / 12.92) : Math.pow((r + 0.055) / 1.055, 2.4);
        g = (g <= 0.03928) ? (g / 12.92) : Math.pow((g + 0.055) / 1.055, 2.4);
        b = (b <= 0.03928) ? (b / 12.92) : Math.pow((b + 0.055) / 1.055, 2.4);

        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }


    // --- DEMO ---
    public static void main(String[] args) {
        Color[] testColors = {
                new Color(100, 150, 200), // blau
                new Color(50, 200, 50),   // grün
                Color.WHITE,
                Color.BLACK,
                Color.GRAY,
                new Color(230, 230, 230), // hellgrau
                new Color(25, 25, 25)     // dunkelgrau
        };

        for (Color c : testColors) {
            System.out.printf("Original: %s -> Kontrast: %s%n",
                    c, getOptimalContrastColor(c));
        }
    }
}
