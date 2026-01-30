import React from 'react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';

interface ElevationProfileProps {
  waypoints?: number[][];
  distance: number;
  elevationGain: number;
  elevationLoss: number;
  maxSlope: number;
}

export const ElevationProfile: React.FC<ElevationProfileProps> = ({
  waypoints,
  distance,
  elevationGain,
  elevationLoss,
  maxSlope,
}) => {
  // Calculate elevation profile data from waypoints
  const profileData = React.useMemo(() => {
    if (!waypoints || waypoints.length < 2) {
      return [];
    }

    const data: Array<{
      distance: number;
      elevation: number;
      distanceLabel: string;
    }> = [];

    let cumulativeDistance = 0;
    let previousLat = waypoints[0][0];
    let previousLng = waypoints[0][1];

    // Estimate elevation from waypoint index and known elevation changes
    // This is a simplified approach since actual elevation isn't in waypoint array
    const startElevation = 850; // Approximate base elevation
    const elevationFraction = elevationGain / distance;

    for (let i = 0; i < waypoints.length; i++) {
      const [lat, lng] = waypoints[i];

      // Calculate distance between waypoints using haversine formula
      if (i > 0) {
        const R = 6371; // Earth's radius in km
        const dLat = ((lat - previousLat) * Math.PI) / 180;
        const dLng = ((lng - previousLng) * Math.PI) / 180;
        const a =
          Math.sin(dLat / 2) * Math.sin(dLat / 2) +
          Math.cos((previousLat * Math.PI) / 180) *
            Math.cos((lat * Math.PI) / 180) *
            Math.sin(dLng / 2) *
            Math.sin(dLng / 2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        cumulativeDistance += R * c;
      }

      // Simulate elevation profile with some variation
      const normalizedProgress = cumulativeDistance / distance;
      const smoothFraction = Math.sin(normalizedProgress * Math.PI);
      const elevation = Math.round(
        startElevation + elevationGain * smoothFraction
      );

      data.push({
        distance: Math.round(cumulativeDistance * 10) / 10,
        elevation: elevation,
        distanceLabel: `${Math.round(cumulativeDistance * 10) / 10}km`,
      });

      previousLat = lat;
      previousLng = lng;
    }

    return data;
  }, [waypoints, distance, elevationGain]);

  if (profileData.length === 0) {
    return (
      <div style={{ padding: '12px', color: '#666', fontSize: '12px' }}>
        Elevation profile data not available
      </div>
    );
  }

  return (
    <div style={{ marginTop: '12px', width: '100%', overflowX: 'auto' }}>
      <ResponsiveContainer width="100%" height={220} minWidth={280}>
        <LineChart
          data={profileData}
          margin={{ top: 10, right: 5, left: 0, bottom: 30 }}
        >
          <CartesianGrid strokeDasharray="3 3" stroke="#e0e0e0" />
          <XAxis
            dataKey="distance"
            stroke="#999"
            tick={{ fontSize: 9 }}
            angle={-45}
            textAnchor="end"
            height={50}
          />
          <YAxis
            stroke="#999"
            tick={{ fontSize: 9 }}
            domain={['dataMin - 50', 'dataMax + 50']}
            width={40}
          />
          <Tooltip
            contentStyle={{
              backgroundColor: '#fff',
              border: '1px solid #ccc',
              borderRadius: '4px',
              fontSize: '10px',
              padding: '6px',
            }}
            formatter={(value: any) => [`${value}m`, 'Elevation']}
            labelFormatter={(label: any) => `${label}km`}
            cursor={{ stroke: '#ff6b6b', strokeWidth: 1 }}
          />
          <Line
            type="monotone"
            dataKey="elevation"
            stroke="#e74c3c"
            dot={false}
            isAnimationActive={false}
            strokeWidth={2}
          />
        </LineChart>
      </ResponsiveContainer>

      {/* Elevation stats under the graph - 2x2 grid */}
      <div
        style={{
          marginTop: '10px',
          display: 'grid',
          gridTemplateColumns: '1fr 1fr',
          gap: '6px',
          fontSize: '10px',
        }}
      >
        <div style={{ backgroundColor: '#fff', padding: '5px', borderRadius: '2px', border: '1px solid #e0e0e0', textAlign: 'center' }}>
          <div style={{ color: '#999', fontSize: '9px', marginBottom: '2px' }}>📈 Gain</div>
          <div style={{ fontWeight: 'bold', color: '#d32f2f', fontSize: '11px' }}>{elevationGain}m</div>
        </div>
        <div style={{ backgroundColor: '#fff', padding: '5px', borderRadius: '2px', border: '1px solid #e0e0e0', textAlign: 'center' }}>
          <div style={{ color: '#999', fontSize: '9px', marginBottom: '2px' }}>📉 Loss</div>
          <div style={{ fontWeight: 'bold', color: '#1976d2', fontSize: '11px' }}>{elevationLoss}m</div>
        </div>
        <div style={{ backgroundColor: '#fff', padding: '5px', borderRadius: '2px', border: '1px solid #e0e0e0', textAlign: 'center' }}>
          <div style={{ color: '#999', fontSize: '9px', marginBottom: '2px' }}>⛏️ Max Slope</div>
          <div style={{ fontWeight: 'bold', color: '#f57c00', fontSize: '11px' }}>{maxSlope}%</div>
        </div>
        <div style={{ backgroundColor: '#fff', padding: '5px', borderRadius: '2px', border: '1px solid #e0e0e0', textAlign: 'center' }}>
          <div style={{ color: '#999', fontSize: '9px', marginBottom: '2px' }}>📍 Distance</div>
          <div style={{ fontWeight: 'bold', color: '#00796b', fontSize: '11px' }}>{distance}km</div>
        </div>
      </div>
    </div>
  );
};
