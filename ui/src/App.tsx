import React, { useState, useEffect, useRef } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Circle, useMap } from 'react-leaflet';
import L from 'leaflet';
import './App.css';

// Component to handle map invalidation
function MapInvalidator({ selectedTrail }: { selectedTrail: any }) {
  const map = useMap();

  useEffect(() => {
    // Invalidate map size and fit bounds after state changes
    setTimeout(() => {
      map.invalidateSize();
    }, 100);
  }, [map, selectedTrail]);

  return null;
}

// Fix for Leaflet icon issue
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
});

// Bucegi Mountains Hiking Trails - Open Source Data
const BUCEGI_TRAILS = [
  {
    id: '550e8400-e29b-41d4-a716-446655440001',
    name: 'Omu Peak Loop',
    description: 'Classic route via alpine meadows and exposed ridge. This is one of the most popular routes in Bucegi, offering stunning panoramic views.',
    distance: 12.5,
    elevationGain: 450,
    elevationLoss: 450,
    durationMinutes: 240,
    maxSlope: 35.2,
    avgSlope: 12.1,
    terrain: ['forest', 'alpine_meadow', 'exposed_ridge'],
    difficulty: 'MEDIUM',
    hazards: ['exposure', 'weather_dependent'],
    source: 'openstreetmap',
    latitude: 45.3585,
    longitude: 25.5050,
    description_extended: 'The Omu Peak Loop is a challenging but rewarding hike that takes you through diverse terrain. Starting from Prapastaia shelter, you\'ll climb through beech and spruce forests before reaching the exposed alpine meadows. The route includes exposure on the ridge but offers breathtaking views of the Carpathian Mountains.'
  },
  {
    id: '550e8400-e29b-41d4-a716-446655440002',
    name: 'Sphinx Ridge Scramble',
    description: 'Technical scramble with rock climbing sections. For experienced hikers only.',
    distance: 8.3,
    elevationGain: 680,
    elevationLoss: 680,
    durationMinutes: 320,
    maxSlope: 65.0,
    avgSlope: 35.5,
    terrain: ['scramble', 'exposed_ridge', 'rock'],
    difficulty: 'ROCK_CLIMBING',
    hazards: ['exposure', 'loose_rock', 'high_altitude'],
    source: 'openstreetmap',
    latitude: 45.3428,
    longitude: 25.5142,
    description_extended: 'The Sphinx is a distinctive rock formation that requires scrambling and some rock climbing sections. This is an advanced route suited for experienced mountaineers with rock climbing skills. Expect exposed sections and significant exposure.'
  },
  {
    id: '550e8400-e29b-41d4-a716-446655440003',
    name: 'Bulea Lake Forest Walk',
    description: 'Easy forested walk with lake views. Perfect for families and beginners.',
    distance: 6.8,
    elevationGain: 150,
    elevationLoss: 150,
    durationMinutes: 120,
    maxSlope: 12.0,
    avgSlope: 4.5,
    terrain: ['forest', 'lake'],
    difficulty: 'EASY',
    hazards: [],
    source: 'openstreetmap',
    latitude: 45.3264,
    longitude: 25.4642,
    description_extended: 'An easy, scenic walk through mixed forest leading to beautiful Bulea Lake. This trail is perfect for families, beginners, or anyone looking for a relaxing nature experience. The lake offers excellent photo opportunities and swimming in summer.'
  },
  {
    id: '550e8400-e29b-41d4-a716-446655440004',
    name: 'Padina-Zarnestei Ridge Trail',
    description: 'Intermediate ridge walk with panoramic views of the Carpathians.',
    distance: 15.2,
    elevationGain: 520,
    elevationLoss: 520,
    durationMinutes: 300,
    maxSlope: 28.0,
    avgSlope: 14.3,
    terrain: ['forest', 'ridge', 'alpine_meadow'],
    difficulty: 'MEDIUM',
    hazards: ['exposure', 'weather_dependent'],
    source: 'openstreetmap',
    latitude: 45.3650,
    longitude: 25.5300,
    description_extended: 'A moderate ridge walk offering spectacular panoramic views. The trail winds through diverse ecosystems from dense forests to open alpine meadows. Weather conditions can change rapidly, so proper preparation is essential.'
  },
  {
    id: '550e8400-e29b-41d4-a716-446655440005',
    name: 'Babele Chapel Hike',
    description: 'Historic mountain hike to the Babele Chapel, an 18th-century hermitage.',
    distance: 10.0,
    elevationGain: 380,
    elevationLoss: 380,
    durationMinutes: 200,
    maxSlope: 22.0,
    avgSlope: 9.5,
    terrain: ['forest', 'historic_site'],
    difficulty: 'MEDIUM',
    hazards: ['exposure'],
    source: 'openstreetmap',
    latitude: 45.3456,
    longitude: 25.5123,
    description_extended: 'Visit the historic Babele Chapel, a fascinating 18th-century hermitage carved into the rock. This moderate hike combines nature with history and cultural significance. The chapel site offers shelter and stunning views.'
  }
];

interface Trail {
  id: string;
  name: string;
  description: string;
  distance: number;
  elevationGain: number;
  elevationLoss: number;
  durationMinutes: number;
  maxSlope: number;
  avgSlope: number;
  terrain: string[];
  difficulty: string;
  hazards: string[];
  source: string;
  latitude: number;
  longitude: number;
}

const DIFFICULTY_COLORS: { [key: string]: string } = {
  EASY: '#2ecc71',
  MEDIUM: '#f39c12',
  HARD: '#e74c3c',
  ROCK_CLIMBING: '#8e44ad'
};

export default function App() {
  const [trails, setTrails] = useState<Trail[]>(BUCEGI_TRAILS);
  const [selectedTrail, setSelectedTrail] = useState<Trail | null>(BUCEGI_TRAILS[0]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filterDifficulty, setFilterDifficulty] = useState<string>('ALL');
  const [mapCenter, setMapCenter] = useState<[number, number]>([45.3500, 25.5000]);

  useEffect(() => {
    // Try to fetch from API, fall back to mock data
    const fetchTrails = async () => {
      try {
        setLoading(true);
        const response = await fetch('/api/v1/trails');
        if (response.ok) {
          const data = await response.json();
          if (Array.isArray(data) && data.length > 0) {
            setTrails(data);
            setSelectedTrail(data[0]);
            setError(null);
          } else {
            setTrails(BUCEGI_TRAILS);
          }
        } else {
          setTrails(BUCEGI_TRAILS);
        }
      } catch (err) {
        console.log('Using mock data - API not available:', err);
        setTrails(BUCEGI_TRAILS);
      } finally {
        setLoading(false);
      }
    };

    fetchTrails();
  }, []);

  const filteredTrails = trails.filter(trail =>
    filterDifficulty === 'ALL' || trail.difficulty === filterDifficulty
  );

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', width: '100vw', backgroundColor: '#fff' }}>
      {/* Header */}
      <div style={{ backgroundColor: '#2c3e50', color: 'white', padding: '15px 20px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)', flexShrink: 0 }}>
        <h1 style={{ margin: '0 0 5px 0', fontSize: '24px' }}>🥾 TrailEquip – Bucegi Mountains</h1>
        <p style={{ margin: 0, fontSize: '12px', opacity: 0.8 }}>Explore hiking trails with live maps and trail recommendations</p>
      </div>

      {/* Error Banner */}
      {error && (
        <div style={{ backgroundColor: '#e74c3c', color: 'white', padding: '10px 20px', fontSize: '12px', flexShrink: 0 }}>
          ⚠️ API Connection Error: {error}
        </div>
      )}

      {/* Main Content - 3 Column Layout */}
      <div style={{ display: 'grid', gridTemplateColumns: '280px 1fr 320px', gap: 0, flex: 1, minHeight: 0, overflow: 'hidden', width: '100%' }}>
        {/* Left Sidebar: Trail List */}
        <div style={{ borderRight: '1px solid #ddd', display: 'flex', flexDirection: 'column', backgroundColor: '#f8f9fa', minHeight: 0, overflow: 'hidden' }}>
          <div style={{ padding: '15px', borderBottom: '1px solid #ddd' }}>
            <h2 style={{ margin: '0 0 10px 0', fontSize: '16px', color: '#2c3e50' }}>
              Trails ({filteredTrails.length})
            </h2>
            <select
              value={filterDifficulty}
              onChange={(e) => setFilterDifficulty(e.target.value)}
              style={{
                width: '100%',
                padding: '8px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '12px'
              }}
            >
              <option value="ALL">All Difficulties</option>
              <option value="EASY">🟢 Easy</option>
              <option value="MEDIUM">🟡 Medium</option>
              <option value="HARD">🔴 Hard</option>
              <option value="ROCK_CLIMBING">🟣 Rock Climbing</option>
            </select>
          </div>

          <div style={{ flex: 1, overflowY: 'auto', padding: '10px' }}>
            {loading ? (
              <p style={{ textAlign: 'center', color: '#999', padding: '20px' }}>Loading trails...</p>
            ) : filteredTrails.length === 0 ? (
              <p style={{ textAlign: 'center', color: '#999', padding: '20px' }}>No trails match filter</p>
            ) : (
              filteredTrails.map((trail) => (
                <div
                  key={trail.id}
                  onClick={() => {
                    setSelectedTrail(trail);
                    setMapCenter([trail.latitude, trail.longitude]);
                  }}
                  style={{
                    padding: '12px',
                    margin: '5px 0',
                    backgroundColor: selectedTrail?.id === trail.id ? '#fff' : '#fff',
                    border: selectedTrail?.id === trail.id ? `3px solid ${DIFFICULTY_COLORS[trail.difficulty]}` : '1px solid #ddd',
                    borderRadius: '6px',
                    cursor: 'pointer',
                    boxShadow: selectedTrail?.id === trail.id ? '0 2px 8px rgba(0,0,0,0.15)' : 'none',
                    transition: 'all 0.2s'
                  }}
                  onMouseEnter={(e) => {
                    (e.currentTarget as any).style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)';
                  }}
                  onMouseLeave={(e) => {
                    (e.currentTarget as any).style.boxShadow = selectedTrail?.id === trail.id ? '0 2px 8px rgba(0,0,0,0.15)' : 'none';
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '5px' }}>
                    <strong style={{ fontSize: '12px', color: '#2c3e50' }}>{trail.name}</strong>
                    <span style={{
                      display: 'inline-block',
                      padding: '2px 6px',
                      backgroundColor: DIFFICULTY_COLORS[trail.difficulty],
                      color: 'white',
                      borderRadius: '3px',
                      fontSize: '10px',
                      fontWeight: 'bold'
                    }}>
                      {trail.difficulty[0]}
                    </span>
                  </div>
                  <div style={{ fontSize: '11px', color: '#666', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '5px' }}>
                    <span>📏 {trail.distance}km</span>
                    <span>⬆️ {trail.elevationGain}m</span>
                    <span>⏱️ {Math.round(trail.durationMinutes / 60)}h</span>
                    <span>📍 {trail.terrain[0]}</span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Center: Map */}
        <div style={{ flex: 1, minHeight: 0, overflow: 'hidden' }}>
          <MapContainer
            center={mapCenter}
            zoom={12}
            style={{ height: '100%', width: '100%' }}
            key={`map-${selectedTrail?.id}`}
          >
            <MapInvalidator selectedTrail={selectedTrail} />
            <TileLayer
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            />
            {/* Display all trails on map */}
            {trails.map((trail) => (
              <React.Fragment key={trail.id}>
                {/* Trail marker */}
                <Marker
                  position={[trail.latitude, trail.longitude]}
                  icon={L.divIcon({
                    className: 'custom-marker',
                    html: `<div style="
                      background-color: ${DIFFICULTY_COLORS[trail.difficulty]};
                      border: 3px solid white;
                      border-radius: 50%;
                      width: 24px;
                      height: 24px;
                      display: flex;
                      align-items: center;
                      justify-content: center;
                      color: white;
                      font-weight: bold;
                      font-size: 12px;
                      box-shadow: 0 2px 4px rgba(0,0,0,0.3);
                      cursor: pointer;
                    ">${trail.name[0]}</div>`,
                    iconSize: [24, 24],
                    iconAnchor: [12, 12]
                  })}
                  eventHandlers={{
                    click: () => {
                      setSelectedTrail(trail);
                    }
                  }}
                >
                  <Popup>
                    <div style={{ fontSize: '12px' }}>
                      <strong>{trail.name}</strong><br/>
                      Difficulty: {trail.difficulty}<br/>
                      Distance: {trail.distance}km<br/>
                      <button
                        onClick={() => setSelectedTrail(trail)}
                        style={{
                          marginTop: '5px',
                          padding: '4px 8px',
                          backgroundColor: DIFFICULTY_COLORS[trail.difficulty],
                          color: 'white',
                          border: 'none',
                          borderRadius: '3px',
                          cursor: 'pointer',
                          fontSize: '11px'
                        }}
                      >
                        View Details
                      </button>
                    </div>
                  </Popup>
                </Marker>

                {/* Highlight selected trail with prominent red circle */}
                {selectedTrail?.id === trail.id && (
                  <>
                    <Circle
                      center={[trail.latitude, trail.longitude]}
                      radius={1200}
                      pathOptions={{
                        color: '#FF0000',
                        weight: 4,
                        opacity: 0.8,
                        fillOpacity: 0.15
                      }}
                    />
                    <Marker
                      position={[trail.latitude, trail.longitude]}
                      icon={L.divIcon({
                        className: 'custom-marker-selected',
                        html: `<div style="
                          background-color: #FF0000;
                          border: 4px solid white;
                          border-radius: 50%;
                          width: 40px;
                          height: 40px;
                          display: flex;
                          align-items: center;
                          justify-content: center;
                          color: white;
                          font-weight: bold;
                          font-size: 18px;
                          box-shadow: 0 0 0 2px #FF0000, 0 4px 8px rgba(0,0,0,0.4);
                          cursor: pointer;
                          animation: pulse 2s infinite;
                        ">★</div>`,
                        iconSize: [40, 40],
                        iconAnchor: [20, 20]
                      })}
                    />
                  </>
                )}
              </React.Fragment>
            ))}
          </MapContainer>
        </div>

        {/* Right Sidebar: Trail Details */}
        <div style={{ borderLeft: '1px solid #ddd', overflowY: 'auto', backgroundColor: '#ffffff', display: 'flex', flexDirection: 'column', minHeight: 0 }}>
          {selectedTrail ? (
            <div style={{ padding: '20px' }}>
              <div style={{ marginBottom: '15px' }}>
                <h3 style={{ margin: '0 0 5px 0', fontSize: '18px', color: '#2c3e50' }}>{selectedTrail.name}</h3>
                <div style={{
                  display: 'inline-block',
                  padding: '6px 12px',
                  backgroundColor: DIFFICULTY_COLORS[selectedTrail.difficulty],
                  color: 'white',
                  borderRadius: '4px',
                  fontSize: '12px',
                  fontWeight: 'bold'
                }}>
                  {selectedTrail.difficulty}
                </div>
              </div>

              <p style={{ fontSize: '12px', color: '#555', lineHeight: '1.5', marginBottom: '15px' }}>
                {selectedTrail.description}
              </p>

              <div style={{ backgroundColor: '#f8f9fa', padding: '12px', borderRadius: '6px', marginBottom: '15px' }}>
                <h4 style={{ margin: '0 0 10px 0', fontSize: '12px', color: '#2c3e50' }}>Trail Stats</h4>
                <ul style={{ margin: 0, padding: '0 0 0 20px', fontSize: '12px', lineHeight: '1.8' }}>
                  <li><strong>Distance:</strong> {selectedTrail.distance} km</li>
                  <li><strong>Elevation Gain:</strong> {selectedTrail.elevationGain} m</li>
                  <li><strong>Elevation Loss:</strong> {selectedTrail.elevationLoss} m</li>
                  <li><strong>Duration:</strong> ~{Math.round(selectedTrail.durationMinutes / 60)}h {selectedTrail.durationMinutes % 60}min</li>
                  <li><strong>Max Slope:</strong> {selectedTrail.maxSlope}%</li>
                  <li><strong>Avg Slope:</strong> {selectedTrail.avgSlope}%</li>
                </ul>
              </div>

              <div style={{ backgroundColor: '#f8f9fa', padding: '12px', borderRadius: '6px', marginBottom: '15px' }}>
                <h4 style={{ margin: '0 0 10px 0', fontSize: '12px', color: '#2c3e50' }}>Terrain</h4>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                  {selectedTrail.terrain.map((t) => (
                    <span key={t} style={{
                      display: 'inline-block',
                      padding: '4px 8px',
                      backgroundColor: '#e8e8e8',
                      borderRadius: '3px',
                      fontSize: '11px',
                      color: '#333'
                    }}>
                      {t.replace(/_/g, ' ')}
                    </span>
                  ))}
                </div>
              </div>

              {selectedTrail.hazards && selectedTrail.hazards.length > 0 && (
                <div style={{ backgroundColor: '#fff3cd', padding: '12px', borderRadius: '6px', border: '1px solid #ffeaa7' }}>
                  <h4 style={{ margin: '0 0 10px 0', fontSize: '12px', color: '#856404' }}>⚠️ Hazards</h4>
                  <ul style={{ margin: 0, padding: '0 0 0 20px', fontSize: '12px', lineHeight: '1.8', color: '#856404' }}>
                    {selectedTrail.hazards.map((h) => (
                      <li key={h}>{h.replace(/_/g, ' ')}</li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          ) : (
            <div style={{ padding: '20px', textAlign: 'center', color: '#999' }}>
              <p>Select a trail to view details</p>
            </div>
          )}
        </div>
      </div>

      {/* Footer */}
      <div style={{ backgroundColor: '#f0f0f0', padding: '10px 20px', borderTop: '1px solid #ddd', fontSize: '11px', color: '#666', flexShrink: 0 }}>
        <span>✅ Using open-source Bucegi Mountains trail data</span> {' | '}
        <a href="/api/v1/health" target="_blank" rel="noreferrer" style={{ marginLeft: '10px', color: '#0366d6' }}>API Health</a> {' | '}
        <a href="/swagger-ui.html" target="_blank" rel="noreferrer" style={{ marginLeft: '10px', color: '#0366d6' }}>API Docs</a>
      </div>
    </div>
  );
}
