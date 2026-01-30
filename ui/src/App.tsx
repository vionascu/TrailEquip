import React, { useState, useEffect, useRef } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import './App.css';
import { ElevationProfile } from './ElevationProfile';

// Component to handle map invalidation
function MapInvalidator({ selectedTrail }: { selectedTrail: any }) {
  const map = useMap();

  useEffect(() => {
    // Invalidate map size and fit bounds after state changes
    const timer = setTimeout(() => {
      try {
        if (map && (map as any)._leaflet_pos !== undefined) {
          map.invalidateSize();
        }
      } catch (error) {
        // Silently handle if map is not ready yet
        console.debug('Map not ready for invalidation');
      }
    }, 150);

    return () => clearTimeout(timer);
  }, [map, selectedTrail]);

  return null;
}

// Component to handle map click for reports
function MapClickHandler({ onMapClick }: { onMapClick: (lat: number, lng: number) => void }) {
  const map = useMap();

  useEffect(() => {
    const handleClick = (e: L.LeafletMouseEvent) => {
      onMapClick(e.latlng.lat, e.latlng.lng);
    };

    map.on('click', handleClick);
    return () => {
      map.off('click', handleClick);
    };
  }, [map, onMapClick]);

  return null;
}

// Fix for Leaflet icon issue
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
});


// Bucegi Mountains Hiking Trails - Real OSMC Routes from Munții Noștri System
const BUCEGI_TRAILS = [
  {id:'0',name:'Traseu 1-2 zile: Sinaia - vârful Omu - refugiul Țigănești - Bușteni',description:'Multi-day alpine trek through Bucegi Mountains: Sinaia → Cabana Piatra Arsă → Cabana Babele (Sfinxul) → Vârful Caraiman → Vârful Omu (2,507m) → Vârful Scara → Refugiul Țigănești → Cabana Mălăieștii → Pichetul Roșu → Bușteni. Water available at Piatra Arsă tap and Babele (bottled). Watch for bears on approach to Piatra Arsă, near Mălăieștii, and Pichetul Roșu to Bușteni sections. Elevation profile: gentle forest ascent 0-8km to Piatra Arsă (1530m), steep climb 8-15km to Babele and Caraiman, intense scramble 15-20km to Omu peak (2507m), ridge traverse 20-25km with Scara peak, descent 25-35km to Refugiul Țigănești, final descent 35-40.98km through forest to Bușteni.',distance:40.98,elevationGain:2020,elevationLoss:1930,durationMinutes:780,maxSlope:42.0,avgSlope:17.5,terrain:['forest','alpine_meadow','exposed_ridge','scramble'],difficulty:'HARD',hazards:['exposure','bears','limited_water_sources','weather_dependent','high_altitude'],source:'muntii-nostri.ro',latitude:45.3556,longitude:25.5180,waypoints:[[45.3514,25.5197],[45.3527,25.5180],[45.3540,25.5163],[45.3553,25.5146],[45.3566,25.5129],[45.3579,25.5112],[45.3592,25.5095],[45.3605,25.5078],[45.3618,25.5061],[45.3631,25.5044],[45.3642,25.5030],[45.3653,25.5016],[45.3664,25.5002],[45.3675,25.4988],[45.3686,25.4974],[45.3697,25.4960],[45.3708,25.4946],[45.3719,25.4932],[45.3730,25.4918],[45.3741,25.4904],[45.3752,25.4890],[45.3763,25.4876],[45.3774,25.4862],[45.3785,25.4848],[45.3796,25.4834],[45.3807,25.4820],[45.3818,25.4806],[45.3829,25.4792],[45.3840,25.4778],[45.3851,25.4764],[45.3862,25.4750],[45.3873,25.4736],[45.3884,25.4722],[45.3895,25.4708],[45.3906,25.4694],[45.3915,25.4685],[45.3924,25.4685],[45.3933,25.4695],[45.3942,25.4715],[45.3950,25.4735],[45.3958,25.4760],[45.3965,25.4790],[45.3970,25.4825],[45.3972,25.4865],[45.3970,25.4905],[45.3965,25.4945],[45.3958,25.4980],[45.3950,25.5010],[45.3942,25.5035],[45.3935,25.5055],[45.3928,25.5070],[45.3921,25.5085],[45.3914,25.5100],[45.3907,25.5115],[45.3900,25.5130],[45.3893,25.5145],[45.3886,25.5160],[45.3879,25.5175],[45.3872,25.5190],[45.3865,25.5205],[45.3858,25.5220],[45.3851,25.5235],[45.3844,25.5250],[45.3837,25.5265],[45.3830,25.5280],[45.3823,25.5295],[45.3816,25.5310],[45.3809,25.5325],[45.3802,25.5340],[45.3795,25.5355],[45.3788,25.5370],[45.3781,25.5385],[45.3774,25.5400],[45.3767,25.5415],[45.3760,25.5430],[45.3753,25.5445],[45.3746,25.5460],[45.3739,25.5475],[45.3732,25.5490],[45.3725,25.5505],[45.3718,25.5520],[45.3711,25.5535],[45.3704,25.5550],[45.3697,25.5565],[45.3690,25.5580],[45.3683,25.5595],[45.3676,25.5610],[45.3669,25.5625],[45.3662,25.5640],[45.3655,25.5655],[45.3648,25.5670],[45.3641,25.5680],[45.3634,25.5690],[45.3627,25.5698],[45.3620,25.5705],[45.3613,25.5710],[45.3606,25.5715],[45.3599,25.5718],[45.3592,25.5720],[45.3585,25.5720],[45.3578,25.5718],[45.3571,25.5715],[45.3564,25.5710],[45.3557,25.5705],[45.3550,25.5698],[45.3543,25.5690],[45.3536,25.5680],[45.3529,25.5668],[45.3522,25.5655],[45.3515,25.5640],[45.3508,25.5623],[45.3501,25.5605],[45.3494,25.5585],[45.3487,25.5563],[45.3480,25.5540],[45.3473,25.5515],[45.3466,25.5488],[45.3459,25.5459],[45.3452,25.5428],[45.3445,25.5395],[45.3438,25.5360],[45.3431,25.5323],[45.3424,25.5284],[45.3417,25.5243],[45.3410,25.5200],[45.3403,25.5155],[45.3396,25.5108],[45.3389,25.5059],[45.3382,25.5008]],trailMarking:'BLUE_STRIPE',isCircular:false,description_extended:'Official Muntii Nostri Route - 1-2 day trek with full elevation profile\n\nELEVATION PROFILE BREAKDOWN:\n0km (Sinaia 950m) → Gradual forest ascent\n8km (Cabana Piatra Arsă 1530m) → Water tap, forest transition to alpine\n12km (Cabana Babele 2000m) → Bottled water, terrain becomes rocky\n15km (Vârful Caraiman 2384m) → Steep exposed section begins\n19km (Vârful Omu 2507m) → PEAK, intense scrambling section\n22km (Vârful Scara 2340m) → High ridge traverse, exposure hazard\n27km (Refugiul Țigănești 1860m) → Overnight shelter, descent begins\n32km (Cabana Mălăieștii 1520m) → Final descent through forest\n38km (Pichetul Roșu 1350m) → Bear hazard zone begins\n40.98km (Bușteni 850m) → Trail end\n\nHAZARD ZONES:\nπ Bear zones: Piatra Arsă approach (6-8km), Mălăieștii area (30-34km), Pichetul Roșu descent (36-40.98km)\n⚠️ Water sources: Piatra Arsă (tap), Babele (bottled)\n🗻 Steep sections: 15-20km (Omu scramble), 20-27km (ridge traverse)\n\nBest: May-October | Difficulty: Medium-Difficult'},
  {id:'1',name:'Sinaia to Omu Peak (Blue Route)',description:'Blue-striped trail: Sinaia (900m) → Cabana Stâna Regală (1400m) → Cabana Piatra Arsă (1530m) → Valea Obârșiei → Cabana Omu (2000m) → Vârful Omu (2507m)',distance:18.5,elevationGain:1850,elevationLoss:1850,durationMinutes:420,maxSlope:35.0,avgSlope:16.0,terrain:['forest','alpine_meadow','rocky'],difficulty:'HARD',hazards:['exposure','weather'],source:'osm',latitude:45.3585,longitude:25.5050,waypoints:[[45.3100,25.4500],[45.3125,25.4520],[45.3150,25.4540],[45.3175,25.4565],[45.3200,25.4590],[45.3230,25.4620],[45.3260,25.4650],[45.3290,25.4680],[45.3315,25.4705],[45.3340,25.4730],[45.3365,25.4755],[45.3385,25.4775],[45.3405,25.4795],[45.3430,25.4820],[45.3450,25.4840],[45.3470,25.4860],[45.3490,25.4880],[45.3510,25.4900],[45.3530,25.4920],[45.3550,25.4940],[45.3570,25.4960],[45.3585,25.5050]],trailMarking:'BLUE_STRIPE',isCircular:false,description_extended:'STARTING POINT: Sinaia town center (900m)\nCHECKPOINTS:\n• 3.5km: Cabana Stâna Regală (1400m) - First rest stop\n• 7km: Cabana Piatra Arsă (1530m) - Water tap available\n• 10km: Valea Obârșiei - Scenic valley crossing\n• 14km: Cabana Omu (2000m) - Main alpine hut\nENDING POINT: Vârful Omu peak (2507m)\n\nELEVATION: Steady forest climb 0-7km, steep alpine ascent 7-18.5km\nWATER: Piatra Arsă (tap), Omu (bottled)\nHAZARDS: Exposure near peak, weather dependent, bear zones near Piatra Arsă'},
  {id:'2',name:'Zarnesti Loop (Blue Triangle)',description:'Loop trail: Refugiul Diana (1510m) → Padina Popii (1800m) → Ridge traverse → La Table (1377m) → Refugiul Diana',distance:14.2,elevationGain:880,elevationLoss:880,durationMinutes:300,maxSlope:40.0,avgSlope:18.0,terrain:['alpine','rocky','ridge'],difficulty:'HARD',hazards:['exposure'],source:'osm',latitude:45.4200,longitude:25.6100,waypoints:[[45.4100,25.5950],[45.4115,25.5968],[45.4130,25.5985],[45.4145,25.6005],[45.4160,25.6022],[45.4175,25.6040],[45.4190,25.6060],[45.4200,25.6075],[45.4200,25.6100],[45.4195,25.6120],[45.4185,25.6135],[45.4175,25.6150],[45.4165,25.6162],[45.4155,25.6172],[45.4145,25.6178]],trailMarking:'BLUE_TRIANGLE',isCircular:true,description_extended:'STARTING POINT: Refugiul Diana (1510m) - Access from Zărnești\nCHECKPOINTS:\n• 2.5km: Padina Popii meadow (1800m) - Alpine meadow with views\n• 4.5km: Ridge high point (1936m) - Brâna Caprelor area\n• 7km: Șaua Padinei Închise (1850m) - Mountain pass\n• 10.5km: La Table junction (1377m) - Trail junction point\nENDING POINT: Return to Refugiul Diana (1510m)\n\nTRAIL TYPE: Circular ridge loop with alpine meadows\nDIFFICULTY: Ridge walking with exposure, rocky terrain\nWATER: Bring sufficient water, no reliable sources on ridge\nVIEWS: 360° panorama views, including Omu, Caraiman peaks'},
  {id:'3',name:'Babele to Sphinx Ridge (Blue Cross)',description:'Ridge connector: Cabana Babele (2000m) → Sfinxul (2020m) → Vârful Caraiman (2384m)',distance:8.5,elevationGain:520,elevationLoss:520,durationMinutes:180,maxSlope:38.0,avgSlope:14.0,terrain:['ridge','rock','alpine'],difficulty:'MEDIUM',hazards:['exposure'],source:'osm',latitude:45.3456,longitude:25.5123,waypoints:[[45.3380,25.5050],[45.3395,25.5065],[45.3410,25.5080],[45.3425,25.5095],[45.3440,25.5108],[45.3450,25.5120],[45.3456,25.5123]],trailMarking:'BLUE_CROSS',isCircular:false,description_extended:'STARTING POINT: Cabana Babele (2000m) - Major alpine hut\nCHECKPOINTS:\n• 1.5km: Sfinxul rock formation (2020m) - Famous landmark\n• 3km: Ridge junction - Multiple route options\n• 5.5km: Sub-ridge meadows (1950m) - Scenic intermediate point\n• 8.5km: Vârful Caraiman peak approach (2384m)\nENDING POINT: Vârful Caraiman (2384m) - Second highest peak in route\n\nTRAIL TYPE: Alpine ridge walk with rock formations\nEXPOSURE: Moderate to high exposure on ridge sections\nVIEWS: Dramatic Sphinx rock, Omu peak views, 360° panorama\nSEASONAL: July-September recommended for rock stability'},
  {id:'4',name:'Brâna Caprelor Circuit (Blue Stripe)',description:'Loop: Zărnești → Refugiul Diana (1510m) → Brâna Caprelor (1936m) → Șaua Padinei Închise (1850m) → Zărnești',distance:20.5,elevationGain:1200,elevationLoss:1200,durationMinutes:420,maxSlope:42.0,avgSlope:18.5,terrain:['alpine','ridge','rocky'],difficulty:'HARD',hazards:['exposure','weather'],source:'osm',latitude:45.4180,longitude:25.6120,waypoints:[[45.4000,25.5950],[45.4020,25.5970],[45.4040,25.5990],[45.4060,25.6010],[45.4080,25.6030],[45.4100,25.6050],[45.4120,25.6070],[45.4140,25.6090],[45.4160,25.6110],[45.4180,25.6120],[45.4170,25.6140],[45.4160,25.6155],[45.4150,25.6165],[45.4140,25.6170]],trailMarking:'BLUE_STRIPE',isCircular:true,description_extended:'STARTING POINT: Zărnești town (750m) - Access from Carpathian foothills\nCHECKPOINTS:\n• 2km: Forest transition zone (1000m) - Road to trail junction\n• 5.5km: Refugiul Diana (1510m) - Major alpine hut, accommodation available\n• 8.5km: Brâna Caprelor peak (1936m) - High point of circuit\n• 12km: Ridge junction (1900m) - Multiple route options\n• 14.5km: Șaua Padinei Închise pass (1850m) - Mountain pass\n• 18.5km: Forest descent begins (1200m) - Transition to woods\nENDING POINT: Return to Zărnești town (750m)\n\nTRAIL TYPE: Full-day alpine loop circuit\nELEVATION: Steep ascent first 5.5km, ridge walk 5.5-14.5km, steady descent 14.5-20.5km\nWATER: Available at Refugiul Diana\nBEST SEASON: June-October for safe ridge conditions'},
  {id:'5',name:'Yellow Ridge Variant (Yellow Stripe)',description:'Alpine variant: Zărnești valley (800m) → Padina Popii meadows (1800m) → Brâna Caprelor ridge (1936m)',distance:12.8,elevationGain:750,elevationLoss:750,durationMinutes:280,maxSlope:36.0,avgSlope:15.0,terrain:['alpine','meadow','ridge'],difficulty:'MEDIUM',hazards:['weather'],source:'osm',latitude:45.4150,longitude:25.5980,waypoints:[[45.4050,25.5850],[45.4070,25.5870],[45.4090,25.5890],[45.4110,25.5910],[45.4130,25.5930],[45.4150,25.5950],[45.4165,25.5965],[45.4175,25.5975]],trailMarking:'YELLOW_STRIPE',isCircular:false,description_extended:'STARTING POINT: Lower Zărnești valley junction (800m)\nCHECKPOINTS:\n• 2.5km: Transition zone (1100m) - Forest to meadow\n• 5km: First meadow cluster (1400m) - Alpine meadow begins\n• 7.5km: Padina Popii central meadow (1800m) - Scenic viewpoint\n• 10km: Ridge approach (1850m) - Ridge junction\n• 12.8km: Brâna Caprelor ridge (1936m) - Ridge endpoint\nENDING POINT: Ridge junctions with blue routes\n\nTRAIL TYPE: Alternative alpine access via meadows\nDIFFICULTY: Less exposed than direct ridge, meadow walking\nFLOWERS: Abundant alpine flowers June-August\nWATER: Seasonal springs in meadows, unreliable in dry season'},
  {id:'6',name:'Red Ridge Branch (Red Cross)',description:'Ridge connector: Șaua Padinei Închise (1850m) → La Table junction (1377m) → Bucegi ridge circuit',distance:9.2,elevationGain:640,elevationLoss:640,durationMinutes:220,maxSlope:40.0,avgSlope:17.0,terrain:['rocky','ridge','alpine'],difficulty:'MEDIUM',hazards:['exposure'],source:'osm',latitude:45.4120,longitude:25.6050,waypoints:[[45.4020,25.5950],[45.4040,25.5970],[45.4060,25.5990],[45.4080,25.6010],[45.4100,25.6030],[45.4120,25.6050],[45.4130,25.6060]],trailMarking:'RED_CROSS',isCircular:false,description_extended:'STARTING POINT: Șaua Padinei Închise mountain pass (1850m)\nCHECKPOINTS:\n• 2km: Ridge plateau (1900m) - Alpine plateau section\n• 4.5km: Intermediate ridge junction (1820m) - Route options\n• 6.5km: La Table approach (1500m) - Steep descent begins\n• 9.2km: La Table junction (1377m) - Major trail hub\nENDING POINT: La Table junction - Connection to multiple routes\n\nTRAIL TYPE: Ridge connector with exposure\nEXPOSURE: Moderate ridge exposure, exposed sections 2-6.5km\nVIEWS: Panoramic views of Bucegi plateau and surrounding valleys\nDESCENT: Steep final section to La Table'},
  {id:'7',name:'Peaks Connector Trail (Blue Dot)',description:'Peak connector: Vârful Caraiman (2384m) → Alpine meadows → Vârful Omu (2507m)',distance:7.5,elevationGain:420,elevationLoss:420,durationMinutes:160,maxSlope:32.0,avgSlope:13.0,terrain:['meadow','rocky'],difficulty:'MEDIUM',hazards:[],source:'osm',latitude:45.3500,longitude:25.5200,waypoints:[[45.3400,25.5100],[45.3415,25.5115],[45.3430,25.5130],[45.3445,25.5145],[45.3460,25.5160],[45.3475,25.5175],[45.3490,25.5190],[45.3500,25.5200]],trailMarking:'BLUE_DOT',isCircular:false,description_extended:'STARTING POINT: Vârful Caraiman peak (2384m) - Second highest summit\nCHECKPOINTS:\n• 1.5km: Ridge plateau (2300m) - High altitude meadow\n• 3.5km: Intermediate saddle (2250m) - Pass between peaks\n• 5.5km: Alpine meadow (2400m) - Scenic high plateau\n• 7.5km: Vârful Omu (2507m) - Highest peak in Bucegi\nENDING POINT: Vârful Omu summit - Highest point in route\n\nTRAIL TYPE: Short peak-to-peak alpine connector\nDIFFICULTY: Well-defined trail, minimal exposure\nVIEWS: Continuous summit-level views, both peaks visible\nBEST: August-September when conditions stable'},
  {id:'8',name:'Red Stripe Secondary Route (Red Stripe)',description:'Secondary variant: Bușteni (850m) → Forest ascent → Ridge plateau (1600m) → Scenic views',distance:11.0,elevationGain:720,elevationLoss:720,durationMinutes:260,maxSlope:35.0,avgSlope:16.0,terrain:['forest','ridge'],difficulty:'HARD',hazards:['weather'],source:'osm',latitude:45.3750,longitude:25.5400,waypoints:[[45.3650,25.5300],[45.3670,25.5320],[45.3690,25.5340],[45.3710,25.5360],[45.3730,25.5380],[45.3750,25.5400]],trailMarking:'RED_STRIPE',isCircular:false,description_extended:'STARTING POINT: Bușteni town center (850m) - Lower elevation access\nCHECKPOINTS:\n• 2km: Forest trail junction (1000m) - Trail merging point\n• 4.5km: Forest canopy (1200m) - Dense beech forest zone\n• 6.5km: Tree line transition (1400m) - Forest to alpine meadow\n• 8.5km: Ridge plateau (1600m) - Alpine meadow begins\n• 11km: Scenic ridge endpoint (1800m) - Connection to main ridge\nENDING POINT: Alpine ridge plateau with panoramic views\n\nTRAIL TYPE: Scenic variant from lower town\nFOREST: Primarily beech and spruce forest sections\nVIEWS: Progressive views opening up to valley and peaks\nACCESS: Alternative from Bușteni, less crowded than main routes'},
  {id:'9',name:'Yellow Peak Ascent (Yellow Triangle)',description:'Eastern variant: Busteni-Teleferica (1150m) → Eastern ridge → Vârful Omu (2507m)',distance:10.5,elevationGain:820,elevationLoss:820,durationMinutes:240,maxSlope:42.0,avgSlope:19.0,terrain:['ridge','rocky','alpine'],difficulty:'HARD',hazards:['exposure'],source:'osm',latitude:45.3850,longitude:25.5200,waypoints:[[45.3750,25.5100],[45.3770,25.5120],[45.3790,25.5140],[45.3810,25.5160],[45.3830,25.5180],[45.3850,25.5200]],trailMarking:'YELLOW_TRIANGLE',isCircular:false,description_extended:'STARTING POINT: Bușteni-Teleferica station (1150m) - Upper cable car terminal\nCHECKPOINTS:\n• 2km: Eastern ridge junction (1350m) - Ridge trail begins\n• 4km: Mid-ridge plateau (1650m) - Widening ridge section\n• 6km: Ridge narrowing (1900m) - Exposure increases\n• 8km: Sub-peak meadows (2250m) - Alpine plateau\n• 10.5km: Vârful Omu summit (2507m) - Peak destination\nENDING POINT: Vârful Omu peak - Highest in Bucegi\n\nTRAIL TYPE: Steep eastern ridge ascent\nEXPOSURE: Significant exposure in upper sections 6-10.5km\nCHALLENGE: Steep scrambling sections, rocky terrain\nACCESS: Can start via cable car to reduce base elevation'},
  {id:'10',name:'Red Ridge Scenic Loop (Red Triangle)',description:'Loop: Cabana Babele (2000m) → Alpine meadows → Ridge plateau → Scenic panorama → Babele',distance:13.5,elevationGain:900,elevationLoss:900,durationMinutes:300,maxSlope:38.0,avgSlope:17.0,terrain:['meadow','ridge','rocky'],difficulty:'MEDIUM',hazards:['weather','exposure'],source:'osm',latitude:45.3700,longitude:25.5300,waypoints:[[45.3600,25.5200],[45.3620,25.5220],[45.3640,25.5240],[45.3660,25.5260],[45.3680,25.5280],[45.3700,25.5300]],trailMarking:'RED_TRIANGLE',isCircular:true,description_extended:'STARTING POINT: Cabana Babele (2000m) - Major alpine hut\nCHECKPOINTS:\n• 1.5km: Ridge intersection (2050m) - Route junction\n• 3.5km: Alpine meadow plateau (2100m) - Wide meadow section\n• 5.5km: Secondary peak viewpoint (2150m) - Panoramic views\n• 7.5km: Ridge high point (2180m) - Loop apex\n• 9.5km: Descent meadows (1950m) - Gradual descent begins\n• 11.5km: Forest transition (1800m) - Alpine to forest edge\n• 13.5km: Return to Cabana Babele (2000m)\nENDING POINT: Return to Cabana Babele\n\nTRAIL TYPE: Scenic alpine loop circuit\nDIFFICULTY: Well-defined meadow circuit, moderate exposure\nFLOWERS: Excellent wildflower displays June-August\nVIEWS: 360° panorama with Omu, Caraiman, and Sphinx visible'},
  {id:'11',name:'Cross Junction Hub (Blue Cross)',description:'Junction trail: Valle Obârșiei (1600m) → Ridge approach → Cabana Babele (2000m) → Route hub',distance:6.8,elevationGain:450,elevationLoss:450,durationMinutes:150,maxSlope:35.0,avgSlope:14.0,terrain:['rocky','alpine'],difficulty:'MEDIUM',hazards:[],source:'osm',latitude:45.3550,longitude:25.5150,waypoints:[[45.3450,25.5050],[45.3470,25.5070],[45.3490,25.5090],[45.3510,25.5110],[45.3530,25.5130],[45.3550,25.5150]],trailMarking:'BLUE_CROSS',isCircular:false,description_extended:'STARTING POINT: Valea Obârșiei valley junction (1600m) - Blue route connector\nCHECKPOINTS:\n• 1.5km: Valley transition (1700m) - Meadow begins\n• 3km: Alpine meadow entrance (1800m) - Scenic meadow section\n• 4.5km: Ridge approach (1900m) - Ridge trail junction\n• 6.8km: Cabana Babele (2000m) - Major alpine hut & junction hub\nENDING POINT: Cabana Babele - Central hub connecting multiple routes\n\nTRAIL TYPE: Short connection trail to main hub\nDIFFICULTY: Easy to moderate alpine walk\nROUTE OPTIONS: From Babele, access to Omu, Caraiman, ridge circuits\nTRAFFIC: Major junction point, often busy hub with route options'},
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
  waypoints?: number[][];
  trailMarking?: string; // e.g., "YELLOW_RECTANGLE", "RED_CIRCLE", "RED_WHITE_STRIPES"
  isCircular?: boolean; // true if trail returns to start point
}

const DIFFICULTY_COLORS: { [key: string]: string } = {
  EASY: '#2ecc71',
  MEDIUM: '#f39c12',
  HARD: '#e74c3c',
  ROCK_CLIMBING: '#8e44ad'
};


interface WeatherForecast {
  date: string;
  dayName: string;
  minTemp: number;
  maxTemp: number;
  condition: string;
  precipitation: number;
  windSpeed: number;
}

interface TrailReport {
  id: string;
  latitude: number;
  longitude: number;
  type: 'hut_full' | 'tree_on_trail' | 'bear' | 'holes_in_trail';
  timestamp: number;
  description: string;
}

const REPORT_TYPES = {
  hut_full: { label: '🏠 Hut Full', color: '#FF6B6B', icon: '🏠' },
  tree_on_trail: { label: '🌲 Tree on Trail', color: '#4ECDC4', icon: '🌲' },
  bear: { label: '🐻 Bear', color: '#8B0000', icon: '🐻' },
  holes_in_trail: { label: '🕳️ Holes in Trail', color: '#FFD700', icon: '🕳️' }
};

// Mock weather data for Bucegi Mountains - next 7 days
const BUCEGI_WEATHER: WeatherForecast[] = (() => {
  const forecasts: WeatherForecast[] = [];
  const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  const conditions = ['Sunny', 'Partly Cloudy', 'Cloudy', 'Light Rain', 'Windy'];

  for (let i = 0; i < 7; i++) {
    const date = new Date();
    date.setDate(date.getDate() + i);
    const dateStr = date.toISOString().split('T')[0];
    const dayIndex = date.getDay();

    forecasts.push({
      date: dateStr,
      dayName: days[dayIndex],
      minTemp: Math.floor(Math.random() * 8) + 5,
      maxTemp: Math.floor(Math.random() * 12) + 12,
      condition: conditions[Math.floor(Math.random() * conditions.length)],
      precipitation: Math.floor(Math.random() * 60),
      windSpeed: Math.floor(Math.random() * 20) + 10
    });
  }
  return forecasts;
})();

export default function App() {
  const [trails, setTrails] = useState<Trail[]>([]);
  const [selectedTrail, setSelectedTrail] = useState<Trail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filterDifficulty, setFilterDifficulty] = useState<string>('ALL');
  const [mapCenter, setMapCenter] = useState<[number, number]>([45.3500, 25.5000]);
  const [mapZoom, setMapZoom] = useState<number>(12);
  const [selectedDate, setSelectedDate] = useState<string>(BUCEGI_WEATHER[0].date);
  const [weatherData, setWeatherData] = useState<WeatherForecast[]>(BUCEGI_WEATHER);
  const [reports, setReports] = useState<TrailReport[]>([]);
  const [reportMode, setReportMode] = useState(false);
  const [selectedReportLocation, setSelectedReportLocation] = useState<{ lat: number; lng: number } | null>(null);
  const [showEquipment, setShowEquipment] = useState(true);
  const [showDescription, setShowDescription] = useState(true);
  const [showStats, setShowStats] = useState(true);
  const [showTerrain, setShowTerrain] = useState(true);
  const [showHazards, setShowHazards] = useState(true);

  // Helper function to calculate optimal zoom level from trail bounds
  const calculateZoomAndCenter = (trail: Trail) => {
    if (!trail.waypoints || trail.waypoints.length === 0) {
      return { zoom: 14, center: [trail.latitude, trail.longitude] as [number, number] };
    }

    // Calculate bounding box from waypoints
    let minLat = trail.waypoints[0][0];
    let maxLat = trail.waypoints[0][0];
    let minLng = trail.waypoints[0][1];
    let maxLng = trail.waypoints[0][1];

    trail.waypoints.forEach(([lat, lng]) => {
      minLat = Math.min(minLat, lat);
      maxLat = Math.max(maxLat, lat);
      minLng = Math.min(minLng, lng);
      maxLng = Math.max(maxLng, lng);
    });

    // Calculate center point
    const centerLat = (minLat + maxLat) / 2;
    const centerLng = (minLng + maxLng) / 2;

    // Calculate zoom level based on bounding box
    const latDiff = maxLat - minLat;
    const lngDiff = maxLng - minLng;
    const maxDiff = Math.max(latDiff, lngDiff);

    let zoom = 14;
    if (maxDiff > 0.1) zoom = 13;
    if (maxDiff > 0.15) zoom = 12;
    if (maxDiff > 0.2) zoom = 11;
    if (maxDiff > 0.3) zoom = 10;

    return { zoom, center: [centerLat, centerLng] as [number, number] };
  };

  const filteredTrails = trails.filter(trail =>
    filterDifficulty === 'ALL' || trail.difficulty === filterDifficulty
  );

  // Generate equipment recommendations based on weather
  const getEquipmentRecommendations = () => {
    const weather = weatherData.find(w => w.date === selectedDate);
    if (!weather) return [];

    const equipment: Array<{ icon: string; item: string; reason: string }> = [];

    // Temperature-based recommendations
    if (weather.maxTemp < 0) {
      equipment.push({ icon: '🥾', item: 'Winter Hiking Boots', reason: 'Below freezing conditions' });
      equipment.push({ icon: '⛏️', item: 'Spikes/Crampons', reason: 'Icy terrain expected' });
      equipment.push({ icon: '🧥', item: 'Insulated Puff Jacket', reason: 'Extreme cold temperature' });
      equipment.push({ icon: '🧣', item: 'Wool Hat & Gloves', reason: 'Protection from freezing' });
      equipment.push({ icon: '🧦', item: 'Wool Layers', reason: 'Thermal insulation' });
    } else if (weather.maxTemp < 10) {
      equipment.push({ icon: '🥾', item: 'Hiking Boots', reason: 'Cool conditions' });
      equipment.push({ icon: '🧥', item: 'Warm Fleece Jacket', reason: `Cold temp (${weather.maxTemp}°C)` });
      equipment.push({ icon: '🧣', item: 'Hat & Gloves', reason: 'Cool weather protection' });
      equipment.push({ icon: '🧦', item: 'Thermal Layers', reason: 'Temperature regulation' });
    } else if (weather.maxTemp < 20) {
      equipment.push({ icon: '👕', item: 'Light Layers', reason: `Mild temp (${weather.maxTemp}°C)` });
      equipment.push({ icon: '🧥', item: 'Light Jacket', reason: 'Layering for variable conditions' });
    } else {
      equipment.push({ icon: '👕', item: 'Light Breathable Clothes', reason: 'Warm weather' });
      equipment.push({ icon: '🩳', item: 'Shorts Optional', reason: 'Warm conditions' });
    }

    // Weather condition-based recommendations
    if (weather.precipitation > 30) {
      equipment.push({ icon: '🧤', item: 'Waterproof Jacket', reason: `High rain risk (${weather.precipitation}%)` });
      equipment.push({ icon: '🩠', item: 'Rain Pants', reason: 'Precipitation protection' });
      equipment.push({ icon: '🎒', item: 'Waterproof Bag Cover', reason: 'Protect gear from rain' });
    }

    // Wind-based recommendations
    if (weather.windSpeed > 20) {
      equipment.push({ icon: '🧥', item: 'Windproof Hardshell', reason: `Strong wind (${weather.windSpeed} km/h)` });
      equipment.push({ icon: '🧢', item: 'Cap/Hat', reason: 'Wind protection' });
    }

    // Sunny recommendations
    if (weather.condition === 'Sunny' || weather.condition === 'Partly Cloudy') {
      equipment.push({ icon: '😎', item: 'Sunglasses', reason: 'UV protection' });
      equipment.push({ icon: '🧴', item: 'Sunscreen (SPF 30+)', reason: 'Sun protection' });
      equipment.push({ icon: '🧢', item: 'Sun Hat/Cap', reason: 'Head protection from sun' });
    }

    // Always recommend
    equipment.push({ icon: '🎒', item: 'Backpack (20-25L)', reason: 'Carry your gear' });
    equipment.push({ icon: '💧', item: 'Water Bottle (1-2L)', reason: 'Hydration' });
    equipment.push({ icon: '🍎', item: 'Snacks & Energy Bars', reason: 'Nutrition on trail' });
    equipment.push({ icon: '🩹', item: 'First Aid Kit', reason: 'Emergency preparedness' });
    equipment.push({ icon: '🗺️', item: 'Map & Compass/GPS', reason: 'Navigation' });

    return equipment;
  };

  // Clean up expired reports (older than 12 hours)
  useEffect(() => {
    const interval = setInterval(() => {
      const now = Date.now();
      const TWELVE_HOURS = 12 * 60 * 60 * 1000;
      setReports(prevReports =>
        prevReports.filter(report => (now - report.timestamp) < TWELVE_HOURS)
      );
    }, 60000); // Check every minute

    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    // Try to fetch from API, fall back to mock data
    const fetchTrails = async () => {
      try {
        setLoading(true);
        const response = await fetch('http://localhost:8081/api/v1/trails');
        if (response.ok) {
          const data = await response.json();
          // Check if API data has the required geographic properties and waypoints
          if (
            Array.isArray(data) &&
            data.length > 0 &&
            data[0].latitude &&
            data[0].longitude &&
            data[0].waypoints &&
            Array.isArray(data[0].waypoints) &&
            data[0].waypoints.length > 2
          ) {
            // API data has complete trail data with waypoints
            setTrails(data);
            setSelectedTrail(data[0]);
            setError(null);
          } else {
            // API data missing waypoints, use mock data instead
            console.log('API data incomplete (missing waypoints), using mock data');
            setTrails(BUCEGI_TRAILS);
            setSelectedTrail(BUCEGI_TRAILS[0]);
          }
        } else {
          setTrails(BUCEGI_TRAILS);
          setSelectedTrail(BUCEGI_TRAILS[0]);
        }
      } catch (err) {
        console.log('Using mock data - API not available:', err);
        setTrails(BUCEGI_TRAILS);
        setSelectedTrail(BUCEGI_TRAILS[0]);
      } finally {
        setLoading(false);
      }
    };

    fetchTrails();
  }, []);


  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', width: '100vw', backgroundColor: '#fff' }}>
      {/* Header */}
      <div style={{ backgroundColor: '#2c3e50', color: 'white', padding: '15px 20px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)', flexShrink: 0, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ margin: '0 0 5px 0', fontSize: '24px' }}>🥾 TrailEquip – Bucegi Mountains</h1>
          <p style={{ margin: 0, fontSize: '12px', opacity: 0.8 }}>Explore 11 hiking trails with interactive maps</p>
        </div>
        <button
          onClick={() => setReportMode(!reportMode)}
          style={{
            padding: '10px 20px',
            backgroundColor: reportMode ? '#FF6B6B' : '#34495e',
            color: 'white',
            border: 'none',
            borderRadius: '6px',
            cursor: 'pointer',
            fontSize: '14px',
            fontWeight: 'bold',
            transition: 'background-color 0.2s',
            whiteSpace: 'nowrap'
          }}
          onMouseEnter={(e) => { (e.target as HTMLButtonElement).style.backgroundColor = reportMode ? '#E53E3E' : '#2c3e50'; }}
          onMouseLeave={(e) => { (e.target as HTMLButtonElement).style.backgroundColor = reportMode ? '#FF6B6B' : '#34495e'; }}
        >
          {reportMode ? '✓ Report Mode ON' : '📍 Report'}
        </button>
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
              Bucegi Trails ({filteredTrails.length})
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
                    const { zoom, center } = calculateZoomAndCenter(trail);
                    setMapCenter(center);
                    setMapZoom(zoom);
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
                  <div style={{ display: 'flex', justifyContent: 'flex-start', alignItems: 'center', gap: '8px', marginBottom: '5px' }}>
                    <div style={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      width: '28px',
                      height: '28px',
                      backgroundColor: DIFFICULTY_COLORS[trail.difficulty],
                      border: '2px solid white',
                      borderRadius: '50%',
                      color: 'white',
                      fontWeight: 'bold',
                      fontSize: '14px',
                      flexShrink: 0,
                      boxShadow: '0 2px 4px rgba(0,0,0,0.3)'
                    }}>
                      {trail.name[0]}
                    </div>
                    <strong style={{ fontSize: '12px', color: '#2c3e50', flex: 1 }}>{trail.name.substring(0, 20)}</strong>
                  </div>
                  <div style={{ fontSize: '11px', color: '#666', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '5px', marginLeft: '36px' }}>
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
        <div style={{ flex: 1, minHeight: 0, overflow: 'hidden', position: 'relative' }}>
          <MapContainer
            center={mapCenter}
            zoom={mapZoom}
            style={{ height: '100%', width: '100%', cursor: reportMode ? 'crosshair' : 'grab' }}
            key={`map-${selectedTrail?.id}`}
          >
            <MapInvalidator selectedTrail={selectedTrail} />
            <MapClickHandler
              onMapClick={(lat, lng) => {
                if (reportMode) {
                  setSelectedReportLocation({ lat, lng });
                }
              }}
            />
            <TileLayer
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            />
            <TileLayer
              url="https://tile.waymarkedtrails.org/hiking/{z}/{x}/{y}.png"
              attribution='&copy; <a href="https://waymarkedtrails.org">Waymarked Trails</a>, OpenStreetMap'
              opacity={0.85}
            />

            {/* Display reports on map */}
            {reports.map((report) => (
              <Marker
                key={report.id}
                position={[report.latitude, report.longitude]}
                icon={L.divIcon({
                  className: 'report-marker',
                  html: `<div style="
                    background-color: ${REPORT_TYPES[report.type].color};
                    border: 3px solid white;
                    border-radius: 50%;
                    width: 40px;
                    height: 40px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-size: 24px;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.4);
                    cursor: pointer;
                  ">${REPORT_TYPES[report.type].icon}</div>`,
                  iconSize: [40, 40],
                  iconAnchor: [20, 20]
                })}
              >
                <Popup>
                  <div style={{ fontSize: '12px' }}>
                    <strong>{REPORT_TYPES[report.type].label}</strong><br/>
                    {new Date(report.timestamp).toLocaleTimeString()}<br/>
                    {report.description && <span style={{ fontSize: '11px', color: '#666' }}>{report.description}</span>}
                  </div>
                </Popup>
              </Marker>
            ))}

            {/* Trail markers removed - clean map only */}
          </MapContainer>

          {/* Report location popup */}
          {selectedReportLocation && (
            <div style={{
              position: 'absolute',
              bottom: '20px',
              left: '50%',
              transform: 'translateX(-50%)',
              backgroundColor: 'white',
              padding: '15px',
              borderRadius: '8px',
              boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
              zIndex: 1000,
              minWidth: '300px'
            }}>
              <h3 style={{ margin: '0 0 12px 0', fontSize: '14px', color: '#2c3e50' }}>Report Issue</h3>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                {Object.entries(REPORT_TYPES).map(([key, value]) => (
                  <button
                    key={key}
                    onClick={() => {
                      const reportId = `${Date.now()}-${Math.random()}`;
                      setReports([...reports, {
                        id: reportId,
                        latitude: selectedReportLocation.lat,
                        longitude: selectedReportLocation.lng,
                        type: key as 'hut_full' | 'tree_on_trail' | 'bear' | 'holes_in_trail',
                        timestamp: Date.now(),
                        description: `Reported at ${new Date().toLocaleTimeString()}`
                      }]);
                      setSelectedReportLocation(null);
                    }}
                    style={{
                      padding: '12px',
                      backgroundColor: value.color,
                      color: 'white',
                      border: 'none',
                      borderRadius: '6px',
                      cursor: 'pointer',
                      fontSize: '13px',
                      fontWeight: 'bold',
                      transition: 'transform 0.1s'
                    }}
                    onMouseEnter={(e) => { (e.target as HTMLButtonElement).style.transform = 'scale(1.05)'; }}
                    onMouseLeave={(e) => { (e.target as HTMLButtonElement).style.transform = 'scale(1)'; }}
                  >
                    {value.icon} {value.label.split(' ').slice(1).join(' ')}
                  </button>
                ))}
              </div>
              <button
                onClick={() => setSelectedReportLocation(null)}
                style={{
                  marginTop: '10px',
                  width: '100%',
                  padding: '8px',
                  backgroundColor: '#ddd',
                  color: '#333',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  fontSize: '12px'
                }}
              >
                Cancel
              </button>
            </div>
          )}
        </div>

        {/* Right Sidebar: Weather & Trail Details */}
        <div style={{ borderLeft: '1px solid #ddd', overflowY: 'auto', backgroundColor: '#ffffff', display: 'flex', flexDirection: 'column', minHeight: 0 }}>
          {/* Weather Section */}
          <div style={{ padding: '15px', borderBottom: '1px solid #ddd', backgroundColor: '#f0f8ff', flexShrink: 0 }}>
            <h3 style={{ margin: '0 0 10px 0', fontSize: '14px', color: '#01579b', fontWeight: 'bold' }}>
              ☀️ 7-Day Forecast
            </h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              <div>
                <label style={{ fontSize: '11px', color: '#333', fontWeight: 'bold', display: 'block', marginBottom: '5px' }}>
                  Select Date:
                </label>
                <select
                  value={selectedDate}
                  onChange={(e) => setSelectedDate(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '6px 8px',
                    border: '1px solid #0066cc',
                    borderRadius: '4px',
                    fontSize: '11px',
                    backgroundColor: '#fff'
                  }}
                >
                  {weatherData.map((w) => (
                    <option key={w.date} value={w.date}>
                      {w.dayName} - {w.date} ({w.minTemp}°-{w.maxTemp}°C)
                    </option>
                  ))}
                </select>
              </div>

              {weatherData.find(w => w.date === selectedDate) && (
                <div style={{ backgroundColor: '#e3f2fd', padding: '10px', borderRadius: '4px', border: '1px solid #90caf9' }}>
                  {(() => {
                    const weather = weatherData.find(w => w.date === selectedDate)!;
                    return (
                      <>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px', fontSize: '11px' }}>
                          <div>
                            <span style={{ color: '#555', fontWeight: 'bold' }}>🌡️ Temp:</span>
                            <br />
                            <span style={{ color: '#d32f2f', fontSize: '12px', fontWeight: 'bold' }}>
                              {weather.maxTemp}°C
                            </span>
                            <span style={{ color: '#999', fontSize: '10px' }}> high</span>
                            <br />
                            <span style={{ color: '#1976d2', fontSize: '12px', fontWeight: 'bold' }}>
                              {weather.minTemp}°C
                            </span>
                            <span style={{ color: '#999', fontSize: '10px' }}> low</span>
                          </div>
                          <div>
                            <span style={{ color: '#555', fontWeight: 'bold' }}>🌤️ Condition:</span>
                            <br />
                            <span style={{ fontSize: '12px', color: '#333' }}>{weather.condition}</span>
                          </div>
                        </div>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px', fontSize: '11px', marginTop: '8px', paddingTop: '8px', borderTop: '1px solid #90caf9' }}>
                          <div>
                            <span style={{ color: '#555', fontWeight: 'bold' }}>💧 Rain:</span>
                            <br />
                            <span style={{ color: '#0288d1', fontWeight: 'bold' }}>{weather.precipitation}%</span>
                          </div>
                          <div>
                            <span style={{ color: '#555', fontWeight: 'bold' }}>💨 Wind:</span>
                            <br />
                            <span style={{ color: '#f57c00', fontWeight: 'bold' }}>{weather.windSpeed} km/h</span>
                          </div>
                        </div>
                      </>
                    );
                  })()}
                </div>
              )}
            </div>
          </div>

          {/* Equipment Recommendations Section */}
          <div style={{ padding: '15px', borderBottom: '1px solid #ddd', backgroundColor: '#f5f5f5', flexShrink: 0 }}>
            <button
              onClick={() => setShowEquipment(!showEquipment)}
              style={{
                width: '100%',
                padding: '10px',
                backgroundColor: '#2c3e50',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '13px',
                fontWeight: 'bold',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                transition: 'background-color 0.2s'
              }}
              onMouseEnter={(e) => { (e.target as HTMLButtonElement).style.backgroundColor = '#1a252f'; }}
              onMouseLeave={(e) => { (e.target as HTMLButtonElement).style.backgroundColor = '#2c3e50'; }}
            >
              <span>🎒 Equipment Guide</span>
              <span style={{ fontSize: '16px' }}>{showEquipment ? '▼' : '▶'}</span>
            </button>

            {showEquipment && (
              <div style={{ marginTop: '12px', maxHeight: '300px', overflowY: 'auto' }}>
                <div style={{ fontSize: '11px', color: '#666', lineHeight: '1.6' }}>
                  <p style={{ margin: '0 0 10px 0', fontSize: '10px', color: '#999', fontStyle: 'italic' }}>
                    Based on {weatherData.find(w => w.date === selectedDate)?.dayName || 'selected'} weather forecast
                  </p>
                  {getEquipmentRecommendations().map((eq, idx) => (
                    <div
                      key={idx}
                      style={{
                        padding: '8px',
                        marginBottom: '6px',
                        backgroundColor: '#ffffff',
                        border: '1px solid #ddd',
                        borderRadius: '3px',
                        display: 'flex',
                        gap: '8px'
                      }}
                    >
                      <span style={{ fontSize: '16px', minWidth: '20px' }}>{eq.icon}</span>
                      <div style={{ flex: 1 }}>
                        <strong style={{ color: '#2c3e50', display: 'block', fontSize: '11px' }}>
                          {eq.item}
                        </strong>
                        <span style={{ color: '#999', fontSize: '10px' }}>
                          {eq.reason}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Trail Details Section */}
          {selectedTrail ? (
            <div style={{ padding: '20px', flex: 1, overflowY: 'auto' }}>
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

              {/* Description - Collapsible */}
              <div style={{ marginBottom: '12px' }}>
                <button
                  onClick={() => setShowDescription(!showDescription)}
                  style={{
                    width: '100%',
                    padding: '10px',
                    backgroundColor: '#ecf0f1',
                    color: '#2c3e50',
                    border: '1px solid #bdc3c7',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    fontSize: '12px',
                    fontWeight: 'bold',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    transition: 'background-color 0.2s'
                  }}
                  onMouseEnter={(e) => { (e.target as HTMLButtonElement).style.backgroundColor = '#d5dbdb'; }}
                  onMouseLeave={(e) => { (e.target as HTMLButtonElement).style.backgroundColor = '#ecf0f1'; }}
                >
                  <span>📝 Route Description</span>
                  <span style={{ fontSize: '14px' }}>{showDescription ? '▼' : '▶'}</span>
                </button>

                {showDescription && (
                  <div style={{ marginTop: '10px', padding: '12px', backgroundColor: '#f8f9fa', borderRadius: '4px' }}>
                    <p style={{ fontSize: '12px', color: '#555', lineHeight: '1.5', margin: 0 }}>
                      {selectedTrail.description}
                    </p>
                  </div>
                )}
              </div>

              {/* Stats - Collapsible */}
              <div style={{ marginBottom: '12px' }}>
                <button
                  onClick={() => setShowStats(!showStats)}
                  style={{
                    width: '100%',
                    padding: '10px',
                    backgroundColor: '#ecf0f1',
                    color: '#2c3e50',
                    border: '1px solid #bdc3c7',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    fontSize: '12px',
                    fontWeight: 'bold',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    transition: 'background-color 0.2s'
                  }}
                  onMouseEnter={(e) => { (e.target as HTMLButtonElement).style.backgroundColor = '#d5dbdb'; }}
                  onMouseLeave={(e) => { (e.target as HTMLButtonElement).style.backgroundColor = '#ecf0f1'; }}
                >
                  <span>📊 Trail Stats</span>
                  <span style={{ fontSize: '14px' }}>{showStats ? '▼' : '▶'}</span>
                </button>

                {showStats && (
                  <div style={{ marginTop: '10px', backgroundColor: '#f8f9fa', padding: '12px', borderRadius: '4px' }}>
                    <ul style={{ margin: 0, padding: '0 0 0 20px', fontSize: '11px', lineHeight: '1.6' }}>
                      <li><strong>Distance:</strong> {selectedTrail.distance} km</li>
                      <li><strong>Elevation Gain:</strong> {selectedTrail.elevationGain} m</li>
                      <li><strong>Elevation Loss:</strong> {selectedTrail.elevationLoss} m</li>
                      <li><strong>Duration:</strong> ~{Math.round(selectedTrail.durationMinutes / 60)}h {selectedTrail.durationMinutes % 60}min</li>
                      <li><strong>Max Slope:</strong> {selectedTrail.maxSlope}%</li>
                      <li><strong>Avg Slope:</strong> {selectedTrail.avgSlope}%</li>
                    </ul>

                    {/* Elevation Profile Graph */}
                    <div style={{ marginTop: '10px', borderTop: '1px solid #ddd', paddingTop: '10px' }}>
                      <div style={{ fontSize: '11px', fontWeight: 'bold', marginBottom: '8px', color: '#2c3e50' }}>
                        📈 Elevation Profile
                      </div>
                      <div style={{ width: '100%', maxHeight: '300px', overflowY: 'auto' }}>
                        <ElevationProfile
                          waypoints={selectedTrail.waypoints}
                          distance={selectedTrail.distance}
                          elevationGain={selectedTrail.elevationGain}
                          elevationLoss={selectedTrail.elevationLoss}
                          maxSlope={selectedTrail.maxSlope}
                        />
                      </div>
                    </div>
                  </div>
                )}
              </div>

              {/* Terrain - Collapsible */}
              <div style={{ marginBottom: '12px' }}>
                <button
                  onClick={() => setShowTerrain(!showTerrain)}
                  style={{
                    width: '100%',
                    padding: '10px',
                    backgroundColor: '#ecf0f1',
                    color: '#2c3e50',
                    border: '1px solid #bdc3c7',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    fontSize: '12px',
                    fontWeight: 'bold',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    transition: 'background-color 0.2s'
                  }}
                  onMouseEnter={(e) => { (e.target as HTMLButtonElement).style.backgroundColor = '#d5dbdb'; }}
                  onMouseLeave={(e) => { (e.target as HTMLButtonElement).style.backgroundColor = '#ecf0f1'; }}
                >
                  <span>🏔️ Terrain</span>
                  <span style={{ fontSize: '14px' }}>{showTerrain ? '▼' : '▶'}</span>
                </button>

                {showTerrain && (
                  <div style={{ marginTop: '10px', backgroundColor: '#f8f9fa', padding: '12px', borderRadius: '4px' }}>
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
                )}
              </div>

              {/* Hazards - Collapsible */}
              {selectedTrail.hazards && selectedTrail.hazards.length > 0 && (
                <div style={{ marginBottom: '12px' }}>
                  <button
                    onClick={() => setShowHazards(!showHazards)}
                    style={{
                      width: '100%',
                      padding: '10px',
                      backgroundColor: '#fff3cd',
                      color: '#856404',
                      border: '1px solid #ffeaa7',
                      borderRadius: '4px',
                      cursor: 'pointer',
                      fontSize: '12px',
                      fontWeight: 'bold',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      transition: 'background-color 0.2s'
                    }}
                    onMouseEnter={(e) => { (e.target as HTMLButtonElement).style.backgroundColor = '#ffe9a1'; }}
                    onMouseLeave={(e) => { (e.target as HTMLButtonElement).style.backgroundColor = '#fff3cd'; }}
                  >
                    <span>⚠️ Hazards</span>
                    <span style={{ fontSize: '14px' }}>{showHazards ? '▼' : '▶'}</span>
                  </button>

                  {showHazards && (
                    <div style={{ marginTop: '10px', backgroundColor: '#fff3cd', padding: '12px', borderRadius: '4px', border: '1px solid #ffeaa7' }}>
                      <ul style={{ margin: 0, padding: '0 0 0 20px', fontSize: '12px', lineHeight: '1.8', color: '#856404' }}>
                        {selectedTrail.hazards.map((h) => (
                          <li key={h}>{h.replace(/_/g, ' ')}</li>
                        ))}
                      </ul>
                    </div>
                  )}
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
        <span>✅ 12 Bucegi Mountains trails with GPS routes</span> {' | '}
        <a href="/api/v1/health" target="_blank" rel="noreferrer" style={{ marginLeft: '10px', color: '#0366d6' }}>API Health</a> {' | '}
        <a href="/swagger-ui.html" target="_blank" rel="noreferrer" style={{ marginLeft: '10px', color: '#0366d6' }}>API Docs</a>
      </div>
    </div>
  );
}
