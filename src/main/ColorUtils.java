package main;

import java.awt.Color;

public class ColorUtils {

    /**
     * Berechnet eine harmonische Kontrast-/Akzentfarbe.
     * Nutzt:
     *  - Komplementärfarben mit sanfter Sättigungsanpassung
     *  - Analoge Harmonien für sehr entsättigte Farben
     *  - Schwarz/Weiß für Graustufen
     */
    public static Color getHarmonicContrastColor(Color color) {

        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float h = hsb[0];
        float s = hsb[1];
        float b = hsb[2];

        // 1) Graustufen → Schwarz/Weiß für maximalen Kontrast
        if (s < 0.12f) {
            return getBestBlackOrWhiteContrast(color);
        }

        // 2) Sehr geringe Sättigung → analoge harmonische Variation
        if (s < 0.25f) {
            float newHue = (h + 0.08f) % 1f;       // leichte Farbverschiebung
            float newSat = Math.min(1f, s + 0.25f); // etwas kräftiger
            float newBright = adjustBrightness(b);

            return Color.getHSBColor(newHue, newSat, newBright);
        }

        // 3) Normale / kräftige Farben → harmonische Komplementärfarbe
        float compHue = (h + 0.5f) % 1f;          // 180°
        float compSat = adjustSaturation(s);      // harmonischer machen
        float compBright = adjustBrightness(b);   // Helligkeit anpassen

        return Color.getHSBColor(compHue, compSat, compBright);
    }

    private static float adjustSaturation(float s) {
        // starke Sättigung etwas abmildern
        if (s > 0.75f) return s * 0.6f;
        if (s > 0.5f)  return s * 0.8f;
        return s;
    }

    private static float adjustBrightness(float b) {
        // zu dunkle Farben etwas heller machen, zu helle etwas abdunkeln
        if (b < 0.3f) return b + 0.25f;
        if (b > 0.8f) return b - 0.2f;
        return b;
    }

    /**
     * Berechnet automatisch, ob Schwarz oder Weiß den besseren Kontrast ergibt.
     */
    private static Color getBestBlackOrWhiteContrast(Color c) {
        double luminance = calculateRelativeLuminance(c);
        return luminance > 0.5 ? Color.BLACK : Color.WHITE;
    }

    private static double calculateRelativeLuminance(Color c) {
        double r = c.getRed() / 255.0;
        double g = c.getGreen() / 255.0;
        double b = c.getBlue() / 255.0;

        r = (r <= 0.03928) ? (r / 12.92) : Math.pow((r + 0.055) / 1.055, 2.4);
        g = (g <= 0.03928) ? (g / 12.92) : Math.pow((g + 0.055) / 1.055, 2.4);
        b = (b <= 0.03928) ? (b / 12.92) : Math.pow((b + 0.055) / 1.055, 2.4);

        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }
}
