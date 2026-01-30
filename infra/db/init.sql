-- Enable PostGIS extension for geographic/geometric queries
CREATE EXTENSION IF NOT EXISTS postgis;

-- ===== TRAIL MARKINGS TABLE =====
-- Stores OSMC (OpenStreetMap Cycling) trail marking symbols
CREATE TABLE trail_markings (
  id BIGSERIAL PRIMARY KEY,
  osmc_symbol VARCHAR(100) UNIQUE NOT NULL,
  color VARCHAR(20),
  shape VARCHAR(20),
  hex_color VARCHAR(7),
  description TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_trail_markings_color ON trail_markings(color);
CREATE INDEX idx_trail_markings_shape ON trail_markings(shape);

-- ===== TRAILS TABLE =====
-- Main table for hiking trails with OSM integration
CREATE TABLE trails (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  osm_id BIGINT UNIQUE,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  ref VARCHAR(50),
  distance DECIMAL(10, 2) NOT NULL,
  elevation_gain INTEGER,
  elevation_loss INTEGER,
  duration_minutes INTEGER,
  max_slope DECIMAL(5, 2),
  avg_slope DECIMAL(5, 2),
  max_elevation INTEGER,
  terrain TEXT[],
  difficulty VARCHAR(20),
  hazards TEXT[],
  source VARCHAR(100),
  marking_id BIGINT REFERENCES trail_markings(id),
  geometry GEOMETRY(LineString, 4326),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_trail_osm_id ON trails(osm_id);
CREATE INDEX idx_trail_difficulty ON trails(difficulty);
CREATE INDEX idx_trail_source ON trails(source);
CREATE INDEX idx_trail_geometry ON trails USING GIST(geometry);
CREATE INDEX idx_trail_marking_id ON trails(marking_id);

-- ===== WAYPOINTS TABLE =====
-- Individual waypoints along trails (peaks, shelters, junctions, etc.)
CREATE TABLE trail_waypoints (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  trail_id UUID NOT NULL REFERENCES trails(id) ON DELETE CASCADE,
  osm_node_id BIGINT,
  sequence_order INTEGER NOT NULL,
  latitude DECIMAL(10, 8) NOT NULL,
  longitude DECIMAL(11, 8) NOT NULL,
  elevation INTEGER,
  name VARCHAR(255),
  type VARCHAR(50),
  description TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_trail_id ON trail_waypoints(trail_id);
CREATE INDEX idx_osm_node_id ON trail_waypoints(osm_node_id);
CREATE INDEX idx_waypoint_type ON trail_waypoints(type);

-- ===== TRAIL SEGMENTS TABLE =====
-- Individual OSM ways that compose a complete trail
CREATE TABLE trail_segments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  trail_id UUID NOT NULL REFERENCES trails(id) ON DELETE CASCADE,
  osm_way_id BIGINT NOT NULL,
  sequence_order INTEGER NOT NULL,
  length DECIMAL(10, 2),
  terrain_type VARCHAR(50),
  accessible BOOLEAN DEFAULT TRUE,
  notes TEXT,
  geometry GEOMETRY(LineString, 4326),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_segment_trail_id ON trail_segments(trail_id);
CREATE INDEX idx_segment_osm_way_id ON trail_segments(osm_way_id);
CREATE INDEX idx_segment_terrain_type ON trail_segments(terrain_type);

-- ===== WEATHER CACHE TABLE =====
-- Cached weather forecast data for trail regions
CREATE TABLE weather_cache (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  latitude DECIMAL(10, 8),
  longitude DECIMAL(11, 8),
  start_date DATE,
  end_date DATE,
  forecast_data JSONB,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP
);

CREATE INDEX idx_weather_location_date ON weather_cache(latitude, longitude, start_date);

-- ===== SEED DATA: TRAIL MARKINGS =====
INSERT INTO trail_markings (osmc_symbol, color, shape, hex_color, description) VALUES
('blue:blue_stripe', 'BLUE', 'STRIPE', '#0000FF', 'Blue stripe - main trail'),
('red:red_triangle', 'RED', 'TRIANGLE', '#FF0000', 'Red triangle - difficult route'),
('yellow:yellow_cross', 'YELLOW', 'CROSS', '#FFFF00', 'Yellow cross - secondary trail'),
('green:green_dot', 'GREEN', 'DOT', '#00AA00', 'Green dot - nature trail'),
('white:white_stripe', 'WHITE', 'STRIPE', '#FFFFFF', 'White stripe - unmarked'),
('orange:orange_rectangle', 'ORANGE', 'RECTANGLE', '#FFA500', 'Orange rectangle - alternate route');

-- ===== SEED DATA: TRAILS =====
INSERT INTO trails (id, name, description, distance, elevation_gain, elevation_loss, duration_minutes, max_slope, avg_slope, terrain, difficulty, hazards, source, created_at, updated_at) VALUES
('550e8400-e29b-41d4-a716-446655440001'::uuid, 'Omu Peak Loop', 'Classic route via alpine meadows and exposed ridge', 12.5, 450, 450, 240, 35.2, 12.1, ARRAY['forest', 'alpine_meadow', 'exposed_ridge'], 'MEDIUM', ARRAY['exposure', 'weather_dependent'], 'openstreetmap', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440002'::uuid, 'Sphinx Ridge Scramble', 'Technical scramble with rock climbing sections', 8.3, 680, 680, 320, 65.0, 35.5, ARRAY['scramble', 'exposed_ridge', 'rock'], 'HARD', ARRAY['exposure', 'loose_rock', 'high_altitude'], 'openstreetmap', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440003'::uuid, 'Bulea Lake Forest Walk', 'Easy forested walk with lake views', 6.8, 150, 150, 120, 12.0, 4.5, ARRAY['forest', 'lake'], 'EASY', ARRAY[]::text[], 'openstreetmap', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
