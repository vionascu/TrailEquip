package com.trailequip.trail.domain.model;

import jakarta.persistence.*;

/**
 * TrailMarking represents the OSMC (OpenStreetMap Cycling) symbol standard for trail markings.
 * OSMC Format: "background:foreground_symbol"
 * Example: "blue:blue_stripe", "red:red_triangle", "yellow:yellow_cross"
 */
@Entity
@Table(name = "trail_markings", uniqueConstraints = {
    @UniqueConstraint(columnNames = "osmc_symbol")
})
public class TrailMarking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String osmc_symbol;  // Full OSMC symbol string

    @Enumerated(EnumType.STRING)
    private MarkingColor color;  // BLUE, RED, YELLOW, GREEN, WHITE, ORANGE, BLACK, PURPLE

    @Enumerated(EnumType.STRING)
    private MarkingShape shape;  // STRIPE, TRIANGLE, CROSS, DOT, RECTANGLE, ARCH, NONE

    @Column(length = 20)
    private String hexColor;  // e.g., "#0000FF" for blue

    @Column(columnDefinition = "TEXT")
    private String description;  // "Blue striped main trail", etc.

    // ===== ENUMS =====

    public enum MarkingColor {
        BLUE("#0000FF"),
        RED("#FF0000"),
        YELLOW("#FFFF00"),
        GREEN("#00AA00"),
        WHITE("#FFFFFF"),
        ORANGE("#FFA500"),
        BLACK("#000000"),
        PURPLE("#800080");

        private final String hex;

        MarkingColor(String hex) {
            this.hex = hex;
        }

        public String getHex() {
            return hex;
        }
    }

    public enum MarkingShape {
        STRIPE("━"),      // Horizontal stripe
        TRIANGLE("▲"),    // Triangle/pyramid
        CROSS("✛"),       // Plus sign cross
        DOT("●"),         // Circle dot
        RECTANGLE("■"),   // Square rectangle
        ARCH("⌢"),        // Arc
        NONE("");         // No symbol

        private final String symbol;

        MarkingShape(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }
    }

    // ===== CONSTRUCTORS =====

    public TrailMarking() {}

    public TrailMarking(String osmc_symbol, MarkingColor color, MarkingShape shape) {
        this.osmc_symbol = osmc_symbol;
        this.color = color;
        this.shape = shape;
        this.hexColor = color.getHex();
        this.description = color + " " + shape;
    }

    // ===== GETTERS =====

    public Long getId() {
        return id;
    }

    public String getOsmcSymbol() {
        return osmc_symbol;
    }

    public MarkingColor getColor() {
        return color;
    }

    public MarkingShape getShape() {
        return shape;
    }

    public String getHexColor() {
        return hexColor;
    }

    public String getDescription() {
        return description;
    }

    // ===== SETTERS =====

    public void setOsmcSymbol(String osmc_symbol) {
        this.osmc_symbol = osmc_symbol;
    }

    public void setColor(MarkingColor color) {
        this.color = color;
        this.hexColor = color.getHex();
    }

    public void setShape(MarkingShape shape) {
        this.shape = shape;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
