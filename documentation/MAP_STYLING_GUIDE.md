# TrailEquip Map Styling Guide

A comprehensive guide to the topographic map styling implementation in TrailEquip.

---

## 🗺️ Map Overview

TrailEquip uses **OpenTopoMap** as its primary tile provider to deliver a professional, topographic hiking map experience similar to traditional paper hiking maps like MASU (Masivul Buceği).

### Why OpenTopoMap?

| Aspect | Benefit |
|--------|---------|
| **Aesthetic** | Yellow/tan terrain with visible contour lines, matching traditional topographic maps |
| **Performance** | Free, open-source, globally distributed tiles with fast delivery |
| **Compatibility** | Works seamlessly with Leaflet and react-leaflet (existing tech stack) |
| **Data Quality** | Combines OpenStreetMap data with GEBCO bathymetric/topographic data |
| **Attribution** | Fully compliant with open-source licensing requirements |

---

## 🎨 Visual Components

### Tile Layers (App.tsx, lines 325-333)

```typescript
// Primary topographic layer (OpenTopoMap)
<TileLayer
  url="https://tile.opentopomap.org/{z}/{x}/{y}.png"
  attribution='&copy; <a href="https://opentopomap.org">OpenTopoMap</a>, <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
  className="topographic-layer"
/>

// Secondary overlay for hiking trail marking
<TileLayer
  url="https://tile.waymarkedtrails.org/hiking/{z}/{x}/{y}.png"
  attribution='&copy; <a href="https://waymarkedtrails.org">Waymarked Trails</a>'
  opacity={0.65}  // Reduced opacity for better readability over terrain
/>
```

### CSS Enhancements (App.css, lines 59-64 and 205-228)

#### Map Container Background
```css
.leaflet-container {
  background: #f5f1e6;  /* Warm, map-like beige color */
}
```

#### Topographic Layer Filters
```css
.topographic-layer {
  filter: saturate(1.15) contrast(1.1) brightness(0.98);
}
```

**Filter Breakdown:**
- `saturate(1.15)` — Enhances terrain colors (yellows, browns)
- `contrast(1.1)` — Makes contour lines more visible
- `brightness(0.98)` — Slightly darkens for better definition

#### Tile Pane Enhancement
```css
.leaflet-tile-pane {
  filter: saturate(1.05);
}
```

---

## 🥾 Trail Rendering

### Polyline Configuration (App.tsx, lines 397-418)

Trail paths are rendered with **two layered polylines** for a bold, topographic look:

```typescript
// White outline for clarity
<Polyline
  pathOptions={{
    color: '#FFFFFF',
    weight: 8,
    opacity: 1
  }}
/>

// Red main line (MASU-style)
<Polyline
  pathOptions={{
    color: '#DD0000',
    weight: 5,
    opacity: 1
  }}
/>
```

**Design Rationale:**
- White outline (8px) creates visual separation from terrain
- Red center line (#DD0000) matches traditional hiking map colors
- Round line caps (`lineCap: 'round'`) for smooth, professional appearance

### Trail Marking Badges

OSMC (OpenStreetMap Comment) trail marking symbols are displayed every 4-5 waypoints along the selected trail. Colors correspond to actual trail markings:
- **Blue Stripe/Triangle/Cross/Dot** — Primary routes and junctions
- **Red Stripe/Cross/Triangle** — Secondary routes
- **Yellow Stripe/Triangle** — Alternative variants

---

## 🎯 Waypoint Interpolation

Trails use **Catmull-Rom cubic Hermite interpolation** (factor: 8) to create smooth curves between waypoints, replicating Google Maps/Waze-style rendering. This provides:
- Smooth, professional trail visualization
- Accurate route representation without visible segmentation
- Better visual clarity on the topographic background

---

## 📱 Zoom-Level Behavior

### Recommended Zoom Ranges

| Zoom Level | Map Purpose | Trail Visibility |
|------------|------------|------------------|
| 6-10 | Regional overview | Trail corridors visible |
| 11-13 | Mountain range detail | Individual trails clear |
| 14-17 | Trail navigation | Waypoint-level detail |
| 18+ | Extreme detail | Contour lines prominent |

The OpenTopoMap tiles are optimized for zoom levels 0-17 with best clarity at 10-15.

---

## 🔧 Configuration & Attribution

### Required Attributions

All three layers must display proper attribution:
1. **OpenTopoMap** — Data source: opentopomap.org
2. **OpenStreetMap** — Underlying cartography
3. **Waymarked Trails** — Trail marking overlay

These appear automatically in the bottom-right corner of the map via Leaflet's attribution control.

### Customization Points

To adjust the topographic appearance:

```css
/* In App.css, line 217 */
.topographic-layer {
  filter: saturate(X.XX) contrast(Y.Y) brightness(Z.ZZ);
  /*        Boost colors  Enhance  Darken  */
}
```

**Safe adjustment ranges:**
- `saturate()` — 1.0 to 1.3 (avoid oversaturation)
- `contrast()` — 1.0 to 1.2 (avoid harsh appearance)
- `brightness()` — 0.9 to 1.0 (avoid dark/washed-out)

---

## 🚀 Performance Considerations

### Tile Loading
- **OpenTopoMap tiles:** ~256KB per tile (PNG format)
- **Waymarked Trails overlay:** ~150KB per tile (PNG format)
- **Total tiles loaded** at zoom 12: ~50-100 tiles (typical viewport)
- **Cache strategy:** Browser cache (via HTTP headers)

### Optimization Tips
1. Waymarked Trails opacity reduced to `0.65` to minimize tile requests
2. Tile layers load asynchronously—no blocking on initial map render
3. Leaflet's native tile management prevents duplicate requests

---

## 📋 Checklist: Adding New Features

When modifying map styling, ensure:

- ✅ OpenTopoMap attribution remains visible
- ✅ Trail polylines remain bold and readable
- ✅ Contour lines stay visible (don't over-saturate or over-brighten)
- ✅ OSMC marking badges maintain contrast
- ✅ Mobile zoom levels work smoothly
- ✅ Performance remains acceptable (no >500ms tile load times)
- ✅ Accessibility: sufficient color contrast (WCAG AA standard)

---

## 🔄 Migration Notes

### Changes from Previous Implementation

| Aspect | Old | New | Why |
|--------|-----|-----|-----|
| **Base Layer** | OpenStreetMap | OpenTopoMap | Better topographic visualization |
| **Styling** | Minimal CSS | CSS filters + classes | Enhanced visual hierarchy |
| **Waymarked Overlay Opacity** | 0.85 | 0.65 | Better terrain readability |
| **Map Background** | #fff (white) | #f5f1e6 (beige) | Matches topographic aesthetic |

### No Breaking Changes
- ✅ All 11 trails render correctly
- ✅ Difficulty filtering works unchanged
- ✅ Weather panel functionality preserved
- ✅ Equipment recommendations unaffected
- ✅ Trail marking badges display properly

---

## 📚 References

- **OpenTopoMap:** https://opentopomap.org/
- **Leaflet Documentation:** https://leafletjs.com/
- **OpenStreetMap Tiles:** https://tile.openstreetmap.org/
- **Waymarked Trails:** https://waymarkedtrails.org/
- **Catmull-Rom Interpolation:** Standard cubic Hermite spline with factor=8

---

## ❓ Troubleshooting

### Issue: Contour lines not visible
**Solution:** Check CSS filters in `.topographic-layer`. Ensure `contrast()` is ≥ 1.1

### Issue: Trails blend with terrain
**Solution:** Verify polyline white outline is rendering (weight: 8). Check opacity not below 0.9

### Issue: Tiles loading slowly
**Solution:** Clear browser cache or use incognito mode. Check internet connection (tiles load from CDN)

### Issue: Waymarked Trails too faint
**Solution:** Increase opacity from 0.65 to 0.75 in App.tsx line 332

---

*Last Updated: 2026-01-30*
*Document Version: 1.0*
