import { useState, useEffect } from "react";
import { useNavigate } from "react-router";
import {
  Box,
  Container,
  Typography,
  IconButton,
  Tabs,
  Tab,
  Card,
  CardContent,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Paper,
} from "@mui/material";
import { ArrowBack, Opacity, LocalHospital, Favorite, MonitorHeart } from "@mui/icons-material";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts";

type MeasurementType = "cukier" | "insulina" | "ciśnienie" | "tętno";

interface Measurement {
  type: MeasurementType;
  value: string;
  timestamp: string;
}

export function History() {
  const navigate = useNavigate();
  const [measurements, setMeasurements] = useState<Measurement[]>([]);
  const [selectedTabs, setSelectedTabs] = useState<number[]>([0]);
  const [timeRangeIndex, setTimeRangeIndex] = useState<number>(7); // Domyślnie "Wszystko"
  
  // Pobierz dane użytkownika i wybranego pacjenta
  const userRole = localStorage.getItem("healthAppUserRole") || "";
  const selectedPatientData = localStorage.getItem("healthAppSelectedPatient");
  const selectedPatient = selectedPatientData ? JSON.parse(selectedPatientData) : null;

  const timeRanges = [
    { label: "Dziś", hours: -1 }, // -1 oznacza "od początku dzisiejszego dnia"
    { label: "72h", hours: 72 },
    { label: "Tydzień", hours: 24 * 7 },
    { label: "2 tyg.", hours: 24 * 14 },
    { label: "Miesiąc", hours: 24 * 30 },
    { label: "Kwartał", hours: 24 * 90 },
    { label: "Rok", hours: 24 * 365 },
    { label: "Wszystko", hours: Infinity },
  ];

  useEffect(() => {
    // Określ klucz do localStorage w zależności od roli
    const storageKey = userRole === "agent" && selectedPatient 
      ? `healthMeasurements_${selectedPatient.id}`
      : "healthMeasurements";
    
    const existing = localStorage.getItem(storageKey);
    if (existing) {
      const loadedMeasurements = JSON.parse(existing);
      setMeasurements(loadedMeasurements);
      
      // Jeśli opiekun ogląda historię pacjenta, oznacz wszystkie jako przeczytane
      if (userRole === "agent" && selectedPatient) {
        const readMeasurements = localStorage.getItem("healthReadMeasurements");
        const readIds = readMeasurements ? JSON.parse(readMeasurements) : [];
        
        const newReadIds = loadedMeasurements.map((m: any) => `${selectedPatient.id}_${m.timestamp}`);
        const updatedIds = [...new Set([...readIds, ...newReadIds])];
        localStorage.setItem("healthReadMeasurements", JSON.stringify(updatedIds));
      }
    } else {
      // Dodaj przykładowe dane dla demonstracji
      const now = new Date();
      const sampleData: Measurement[] = [
        // Cukier - ostatnie 7 dni
        { type: "cukier", value: "95", timestamp: new Date(now.getTime() - 6 * 24 * 60 * 60 * 1000).toISOString() },
        { type: "cukier", value: "110", timestamp: new Date(now.getTime() - 5 * 24 * 60 * 60 * 1000).toISOString() },
        { type: "cukier", value: "105", timestamp: new Date(now.getTime() - 4 * 24 * 60 * 60 * 1000).toISOString() },
        { type: "cukier", value: "98", timestamp: new Date(now.getTime() - 3 * 24 * 60 * 60 * 1000).toISOString() },
        { type: "cukier", value: "115", timestamp: new Date(now.getTime() - 2 * 24 * 60 * 60 * 1000).toISOString() },
        { type: "cukier", value: "102", timestamp: new Date(now.getTime() - 1 * 24 * 60 * 60 * 1000).toISOString() },
        { type: "cukier", value: "108", timestamp: now.toISOString() },
        
        // Insulina
        { type: "insulina", value: "8 jednostek", timestamp: new Date(now.getTime() - 6 * 24 * 60 * 60 * 1000).toISOString() },
        { type: "insulina", value: "10 jednostek", timestamp: new Date(now.getTime() - 5 * 24 * 60 * 60 * 1000).toISOString() },
        { type: "insulina", value: "9 jednostek", timestamp: new Date(now.getTime() - 4 * 24 * 60 * 60 * 1000).toISOString() },
        { type: "insulina", value: "8 jednostek", timestamp: new Date(now.getTime() - 3 * 24 * 60 * 60 * 1000).toISOString() },
        { type: "insulina", value: "11 jednostek", timestamp: new Date(now.getTime() - 2 * 24 * 60 * 60 * 1000).toISOString() },
        { type: "insulina", value: "9 jednostek", timestamp: new Date(now.getTime() - 1 * 24 * 60 * 60 * 1000).toISOString() },
        { type: "insulina", value: "10 jednostek", timestamp: now.toISOString() },
        
        // Ciśnienie
        { type: "ciśnienie", value: "120 na 80", timestamp: new Date(now.getTime() - 6 * 24 * 60 * 60 * 1000).toISOString() },
        { type: "ciśnienie", value: "125 na 82", timestamp: new Date(now.getTime() - 5 * 24 * 60 * 60 * 1000).toISOString() },
        { type: "ciśnienie", value: "118 na 78", timestamp: new Date(now.getTime() - 4 * 24 * 60 * 60 * 1000).toISOString() },
        { type: "ciśnienie", value: "122 na 81", timestamp: new Date(now.getTime() - 3 * 24 * 60 * 60 * 1000).toISOString() },
        { type: "ciśnienie", value: "130 na 85", timestamp: new Date(now.getTime() - 2 * 24 * 60 * 60 * 1000).toISOString() },
        { type: "ciśnienie", value: "119 na 79", timestamp: new Date(now.getTime() - 1 * 24 * 60 * 60 * 1000).toISOString() },
        { type: "ciśnienie", value: "123 na 80", timestamp: now.toISOString() },
        
        // Tętno
        { type: "tętno", value: "72", timestamp: new Date(now.getTime() - 6 * 24 * 60 * 60 * 1000).toISOString() },
        { type: "tętno", value: "75", timestamp: new Date(now.getTime() - 5 * 24 * 60 * 60 * 1000).toISOString() },
        { type: "tętno", value: "70", timestamp: new Date(now.getTime() - 4 * 24 * 60 * 60 * 1000).toISOString() },
        { type: "tętno", value: "68", timestamp: new Date(now.getTime() - 3 * 24 * 60 * 60 * 1000).toISOString() },
        { type: "tętno", value: "78", timestamp: new Date(now.getTime() - 2 * 24 * 60 * 60 * 1000).toISOString() },
        { type: "tętno", value: "73", timestamp: new Date(now.getTime() - 1 * 24 * 60 * 60 * 1000).toISOString() },
        { type: "tętno", value: "74", timestamp: now.toISOString() },
      ];
      
      localStorage.setItem(storageKey, JSON.stringify(sampleData));
      setMeasurements(sampleData);
    }
  }, []);

  const tabs = [
    { type: "cukier" as MeasurementType, label: "Cukier", icon: Opacity, color: "#2196F3" },
    { type: "insulina" as MeasurementType, label: "Insulina", icon: LocalHospital, color: "#4CAF50" },
    { type: "ciśnienie" as MeasurementType, label: "Ciśnienie", icon: Favorite, color: "#F44336" },
    { type: "tętno" as MeasurementType, label: "Tętno", icon: MonitorHeart, color: "#9C27B0" },
  ];

  const toggleTab = (index: number) => {
    setSelectedTabs((prev) => {
      if (prev.includes(index)) {
        // Jeśli już jest wybrany, usuń go (ale zawsze zostaw przynajmniej jeden)
        if (prev.length > 1) {
          return prev.filter((i) => i !== index);
        }
        return prev;
      } else {
        // Dodaj do wybranych
        return [...prev, index].sort();
      }
    });
  };

  const getFilteredMeasurements = (type: MeasurementType) => {
    const now = new Date();
    const currentRange = timeRanges[timeRangeIndex];
    
    return measurements.filter((m) => {
      if (m.type !== type) return false;
      
      const measurementDate = new Date(m.timestamp);
      
      // Specjalny przypadek dla "Dziś" - od początku dzisiejszego dnia
      if (currentRange.hours === -1) {
        const startOfToday = new Date(now);
        startOfToday.setHours(0, 0, 0, 0);
        return measurementDate >= startOfToday && measurementDate <= now;
      }
      
      // Dla innych przedziałów czasowych
      const rangeMillis = currentRange.hours * 60 * 60 * 1000;
      const timeDiff = now.getTime() - measurementDate.getTime();
      
      return rangeMillis === Infinity || timeDiff <= rangeMillis;
    });
  };

  const getChartData = (type: MeasurementType) => {
    const filtered = getFilteredMeasurements(type);
    return filtered.map((m) => {
      const date = new Date(m.timestamp);
      let numericValue = 0;
      if (type === "ciśnienie") {
        const match = m.value.match(/\d+/);
        numericValue = match ? parseInt(match[0]) : 0;
      } else {
        numericValue = parseFloat(m.value.replace(/[^\d.]/g, "")) || 0;
      }
      
      return {
        date: date.toLocaleDateString("pl-PL", { day: "2-digit", month: "2-digit", hour: "2-digit", minute: "2-digit" }),
        value: numericValue,
        fullValue: m.value,
      };
    });
  };

  const getUnit = (type: MeasurementType) => {
    switch (type) {
      case "cukier":
        return "mg/dL";
      case "insulina":
        return "j";
      case "ciśnienie":
        return "mmHg";
      case "tętno":
        return "bpm";
    }
  };

  const getCombinedChartData = () => {
    // Zbierz wszystkie unikalne timestampy
    const allTimestamps = new Set<string>();
    selectedTabs.forEach((tabIndex) => {
      const type = tabs[tabIndex].type;
      const filtered = getFilteredMeasurements(type);
      filtered.forEach((m) => allTimestamps.add(m.timestamp));
    });

    // Sortuj timestampy
    const sortedTimestamps = Array.from(allTimestamps).sort();

    // Stwórz dane dla wykresu
    return sortedTimestamps.map((timestamp) => {
      const date = new Date(timestamp);
      const dataPoint: any = {
        date: date.toLocaleDateString("pl-PL", { day: "2-digit", month: "2-digit", hour: "2-digit", minute: "2-digit" }),
        timestamp,
      };

      selectedTabs.forEach((tabIndex) => {
        const type = tabs[tabIndex].type;
        const measurement = measurements.find(
          (m) => m.type === type && m.timestamp === timestamp
        );

        if (measurement) {
          let numericValue = 0;
          if (type === "ciśnienie") {
            const match = measurement.value.match(/\d+/);
            numericValue = match ? parseInt(match[0]) : 0;
          } else {
            numericValue = parseFloat(measurement.value.replace(/[^\d.]/g, "")) || 0;
          }
          dataPoint[type] = numericValue;
          dataPoint[`${type}_full`] = measurement.value;
        }
      });

      return dataPoint;
    });
  };

  const getAllSelectedMeasurements = () => {
    const allMeasurements: (Measurement & { tabIndex: number })[] = [];
    selectedTabs.forEach((tabIndex) => {
      const type = tabs[tabIndex].type;
      const filtered = getFilteredMeasurements(type);
      filtered.forEach((m) => {
        allMeasurements.push({ ...m, tabIndex });
      });
    });
    return allMeasurements.sort((a, b) => 
      new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
    );
  };

  const hasAnyData = selectedTabs.some((tabIndex) => {
    const type = tabs[tabIndex].type;
    return getFilteredMeasurements(type).length > 0;
  });

  const chartData = getCombinedChartData();
  const listData = getAllSelectedMeasurements();

  return (
    <Box sx={{ backgroundColor: "#f5f5f5", minHeight: "100vh", pb: 2 }}>
      <Box
        sx={{
          backgroundColor: "#1976d2",
          color: "white",
          py: 2,
          px: 2,
          display: "flex",
          alignItems: "center",
          gap: 2,
          boxShadow: "0 2px 4px rgba(0,0,0,0.2)",
        }}
      >
        <IconButton onClick={() => navigate("/home")} sx={{ color: "white" }}>
          <ArrowBack sx={{ fontSize: 32 }} />
        </IconButton>
        <Typography variant="h5" component="h1" sx={{ fontWeight: 500, flex: 1, textAlign: "center", mr: 6 }}>
          Historia Pomiarów
        </Typography>
      </Box>

      <Container maxWidth="md" sx={{ mt: 2, px: 2 }}>
        <Box
          sx={{
            display: "grid",
            gridTemplateColumns: "1fr 1fr",
            gap: 2,
            mb: 2,
          }}
        >
          {tabs.map((tab, index) => (
            <Card
              key={tab.type}
              onClick={() => toggleTab(index)}
              sx={{
                cursor: "pointer",
                transition: "all 0.2s",
                "&:active": {
                  transform: "scale(0.98)",
                },
                boxShadow: selectedTabs.includes(index)
                  ? "0 4px 8px rgba(0,0,0,0.3)" 
                  : "0 2px 4px rgba(0,0,0,0.2)",
                border: selectedTabs.includes(index) ? `3px solid ${tab.color}` : "3px solid transparent",
              }}
            >
              <CardContent
                sx={{
                  display: "flex",
                  flexDirection: "column",
                  alignItems: "center",
                  gap: 1,
                  py: 3,
                  backgroundColor: selectedTabs.includes(index) ? tab.color : "white",
                  color: selectedTabs.includes(index) ? "white" : tab.color,
                  "&:last-child": { pb: 3 },
                }}
              >
                <tab.icon sx={{ fontSize: 56 }} />
                <Typography variant="h6" component="div" sx={{ fontWeight: 500 }}>
                  {tab.label}
                </Typography>
              </CardContent>
            </Card>
          ))}
        </Box>

        <Card sx={{ mt: 2, boxShadow: "0 2px 4px rgba(0,0,0,0.2)" }}>
          <CardContent>
            <Typography variant="h6" sx={{ mb: 3, fontWeight: 500 }}>
              Przedział czasowy
            </Typography>
            <Box sx={{ px: 2 }}>
              <Typography variant="h5" align="center" color="primary" sx={{ mb: 2, fontWeight: 500 }}>
                {timeRanges[timeRangeIndex].label}
              </Typography>
              <Box sx={{ position: "relative", pb: 3 }}>
                {/* Suwak */}
                <Box
                  sx={{
                    position: "relative",
                    height: "60px",
                    display: "flex",
                    alignItems: "center",
                  }}
                >
                  <Box
                    sx={{
                      position: "absolute",
                      top: "50%",
                      left: 0,
                      right: 0,
                      height: "8px",
                      backgroundColor: "#e0e0e0",
                      borderRadius: "4px",
                      transform: "translateY(-50%)",
                    }}
                  />
                  <Box
                    sx={{
                      position: "absolute",
                      top: "50%",
                      left: 0,
                      width: `${(timeRangeIndex / (timeRanges.length - 1)) * 100}%`,
                      height: "8px",
                      backgroundColor: "#1976d2",
                      borderRadius: "4px",
                      transform: "translateY(-50%)",
                    }}
                  />
                  {timeRanges.map((range, index) => (
                    <Box
                      key={index}
                      onClick={() => setTimeRangeIndex(index)}
                      sx={{
                        position: "absolute",
                        left: `${(index / (timeRanges.length - 1)) * 100}%`,
                        top: "50%",
                        transform: "translate(-50%, -50%)",
                        width: timeRangeIndex === index ? "32px" : "24px",
                        height: timeRangeIndex === index ? "32px" : "24px",
                        backgroundColor: timeRangeIndex === index ? "#1976d2" : "white",
                        border: `3px solid ${timeRangeIndex === index ? "#1976d2" : "#bdbdbd"}`,
                        borderRadius: "50%",
                        cursor: "pointer",
                        transition: "all 0.2s",
                        zIndex: timeRangeIndex === index ? 2 : 1,
                        "&:hover": {
                          transform: "translate(-50%, -50%) scale(1.1)",
                        },
                        "&:active": {
                          transform: "translate(-50%, -50%) scale(0.95)",
                        },
                      }}
                    />
                  ))}
                </Box>
                {/* Etykiety */}
                <Box
                  sx={{
                    position: "relative",
                    display: "flex",
                    justifyContent: "space-between",
                    mt: 1,
                  }}
                >
                  {timeRanges.map((range, index) => (
                    <Typography
                      key={index}
                      variant="body2"
                      sx={{
                        position: "absolute",
                        left: `${(index / (timeRanges.length - 1)) * 100}%`,
                        transform: "translateX(-50%)",
                        fontSize: timeRangeIndex === index ? "16px" : "13px",
                        fontWeight: timeRangeIndex === index ? 600 : 400,
                        color: timeRangeIndex === index ? "#1976d2" : "#757575",
                        whiteSpace: "nowrap",
                      }}
                    >
                      {range.label}
                    </Typography>
                  ))}
                </Box>
              </Box>
            </Box>
          </CardContent>
        </Card>

        {hasAnyData ? (
          <Card sx={{ mb: 2, boxShadow: "0 2px 4px rgba(0,0,0,0.2)" }}>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 2, fontWeight: 500 }}>
                Wykres
              </Typography>
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" tick={{ fontSize: 11 }} />
                  <YAxis tick={{ fontSize: 14 }} />
                  <Tooltip 
                    contentStyle={{ fontSize: "14px" }}
                    formatter={(value: any, name: any, props: any) => {
                      const fullValueKey = `${name}_full`;
                      const fullValue = props.payload[fullValueKey];
                      const tab = tabs.find(t => t.type === name);
                      return [fullValue || value, tab?.label || name];
                    }}
                  />
                  <Legend />
                  {selectedTabs.map((tabIndex) => {
                    const tab = tabs[tabIndex];
                    return (
                      <Line 
                        key={tab.type}
                        type="monotone" 
                        dataKey={tab.type}
                        stroke={tab.color}
                        strokeWidth={3}
                        name={tab.type}
                        connectNulls
                      />
                    );
                  })}
                </LineChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        ) : (
          <Card sx={{ mb: 2, boxShadow: "0 2px 4px rgba(0,0,0,0.2)" }}>
            <CardContent>
              <Typography variant="h6" align="center" color="text.secondary">
                Brak danych dla wybranych kategorii
              </Typography>
            </CardContent>
          </Card>
        )}

        <Card sx={{ boxShadow: "0 2px 4px rgba(0,0,0,0.2)" }}>
          <CardContent>
            <Typography variant="h6" sx={{ mb: 2, fontWeight: 500 }}>
              Lista Pomiarów
            </Typography>
            {listData.length > 0 ? (
              <List sx={{ p: 0 }}>
                {listData.map((measurement, index) => {
                  const tab = tabs[measurement.tabIndex];
                  const MeasurementIcon = tab.icon;
                  return (
                    <ListItem
                      key={index}
                      sx={{
                        backgroundColor: index % 2 === 0 ? "#fafafa" : "white",
                        borderRadius: "8px",
                        mb: 1,
                        border: "1px solid #e0e0e0",
                      }}
                    >
                      <ListItemIcon>
                        <MeasurementIcon sx={{ fontSize: 40, color: tab.color }} />
                      </ListItemIcon>
                      <ListItemText
                        primary={`${tab.label}: ${measurement.value} ${getUnit(measurement.type)}`}
                        secondary={new Date(measurement.timestamp).toLocaleString("pl-PL", {
                          day: "2-digit",
                          month: "2-digit",
                          year: "numeric",
                          hour: "2-digit",
                          minute: "2-digit",
                        })}
                        primaryTypographyProps={{
                          variant: "h6",
                          sx: { fontWeight: 500 }
                        }}
                        secondaryTypographyProps={{
                          variant: "body1"
                        }}
                      />
                    </ListItem>
                  );
                })}
              </List>
            ) : (
              <Typography variant="body1" color="text.secondary" align="center" sx={{ py: 2 }}>
                Brak pomiarów
              </Typography>
            )}
          </CardContent>
        </Card>
      </Container>
    </Box>
  );
}