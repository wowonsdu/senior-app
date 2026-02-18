import { useState, useEffect } from "react";
import { useNavigate } from "react-router";
import {
  Box,
  Container,
  Typography,
  Card,
  CardContent,
  Button,
  IconButton,
  Avatar,
  Chip,
  List,
  ListItem,
  ListItemText,
  ListItemAvatar,
  Badge,
  Tabs,
  Tab,
} from "@mui/material";
import {
  Logout,
  TrendingUp,
  TrendingDown,
  Notifications,
  People,
  Dashboard as DashboardIcon,
  Opacity,
  LocalHospital,
  Favorite,
  MonitorHeart,
  Circle,
  CalendarMonth,
} from "@mui/icons-material";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, Cell } from "recharts";
import { Visits } from "./Visits";

interface Patient {
  id: string;
  name: string;
  phone?: string;
  addedDate: string;
  accessCode?: string;
}

interface Measurement {
  type: string;
  value: string;
  timestamp: string;
  patientId: string;
}

interface PatientChange {
  patientId: string;
  patientName: string;
  type: string;
  change: number;
  changePercent: number;
  trend: "up" | "down" | "stable";
  currentValue: number;
  previousValue: number;
}

interface UnreadMeasurement {
  id: string;
  patientId: string;
  patientName: string;
  type: string;
  value: string;
  timestamp: string;
}

interface Alert {
  id: string;
  type: string;
  alertType: string;
  value: string;
  numericValue: number;
  threshold?: number;
  change?: string;
  measurementCount?: number;
  timestamp: string;
  patientId: string;
  patientName: string;
  read?: boolean;
}

export function CaregiverDashboard() {
  const navigate = useNavigate();
  const [currentTab, setCurrentTab] = useState<"dashboard" | "profiles" | "visits">("dashboard");
  const [patients, setPatients] = useState<Patient[]>([]);
  const [patientChanges, setPatientChanges] = useState<PatientChange[]>([]);
  const [unreadMeasurements, setUnreadMeasurements] = useState<UnreadMeasurement[]>([]);
  const [unreadCounts, setUnreadCounts] = useState<{ [key: string]: number }>({});
  const [alerts, setAlerts] = useState<Alert[]>([]);

  const userRole = localStorage.getItem("healthAppUserRole") || "";

  useEffect(() => {
    if (userRole !== "agent") {
      navigate("/home", { replace: true });
      return;
    }

    loadData();
  }, [userRole, navigate]);

  const loadData = () => {
    // Załaduj listę pacjentów
    const savedPatients = localStorage.getItem("healthPatients");
    const patientsList = savedPatients ? JSON.parse(savedPatients) : [];
    setPatients(patientsList);

    // Załaduj dane o nieodczytanych pomiarach
    const readMeasurements = localStorage.getItem("healthReadMeasurements");
    const readIds = readMeasurements ? JSON.parse(readMeasurements) : [];

    // Analizuj pomiary dla każdego pacjenta
    const changes: PatientChange[] = [];
    const unread: UnreadMeasurement[] = [];
    const counts: { [key: string]: number } = {};

    patientsList.forEach((patient: Patient) => {
      const storageKey = `healthMeasurements_${patient.id}`;
      const measurements = localStorage.getItem(storageKey);
      
      if (measurements) {
        const measurementsList: Measurement[] = JSON.parse(measurements);
        
        // Policz nieodczytane
        let unreadCount = 0;
        measurementsList.forEach((m: Measurement) => {
          const measurementId = `${patient.id}_${m.timestamp}`;
          if (!readIds.includes(measurementId)) {
            unreadCount++;
            unread.push({
              id: measurementId,
              patientId: patient.id,
              patientName: patient.name,
              type: m.type,
              value: m.value,
              timestamp: m.timestamp,
            });
          }
        });
        
        counts[patient.id] = unreadCount;

        // Analizuj zmiany dla każdego typu pomiaru
        const types = ["cukier", "insulina", "ciśnienie", "tętno"];
        types.forEach((type) => {
          const typeMeasurements = measurementsList
            .filter((m: Measurement) => m.type === type)
            .sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());

          if (typeMeasurements.length >= 2) {
            const latest = parseFloat(typeMeasurements[0].value);
            const previous = parseFloat(typeMeasurements[1].value);
            
            if (!isNaN(latest) && !isNaN(previous)) {
              const change = latest - previous;
              const changePercent = (change / previous) * 100;
              const trend = Math.abs(changePercent) < 5 ? "stable" : (change > 0 ? "up" : "down");

              changes.push({
                patientId: patient.id,
                patientName: patient.name,
                type,
                change,
                changePercent,
                trend,
                currentValue: latest,
                previousValue: previous,
              });
            }
          }
        });
      }
    });

    // Sortuj zmiany według wartości bezwzględnej zmiany procentowej (malejąco)
    changes.sort((a, b) => Math.abs(b.changePercent) - Math.abs(a.changePercent));

    // Sortuj nieodczytane od najnowszych
    unread.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());

    setPatientChanges(changes);
    setUnreadMeasurements(unread);
    setUnreadCounts(counts);
  };

  const handleLogout = () => {
    localStorage.removeItem("healthAppLoggedIn");
    localStorage.removeItem("healthAppPhone");
    localStorage.removeItem("healthAppLoginDate");
    localStorage.removeItem("healthAppUserRole");
    localStorage.removeItem("healthAppAgentData");
    localStorage.removeItem("healthAppSelectedRole");
    localStorage.removeItem("healthAppSelectedPatient");
    navigate("/");
  };

  const handleSelectPatient = (patient: Patient) => {
    localStorage.setItem("healthAppSelectedPatient", JSON.stringify(patient));
    navigate("/home");
  };

  const handleMarkAsRead = (measurementId: string) => {
    const readMeasurements = localStorage.getItem("healthReadMeasurements");
    const readIds = readMeasurements ? JSON.parse(readMeasurements) : [];
    
    if (!readIds.includes(measurementId)) {
      readIds.push(measurementId);
      localStorage.setItem("healthReadMeasurements", JSON.stringify(readIds));
      loadData(); // Odśwież dane
    }
  };

  const handleMarkAllAsRead = () => {
    const allIds = unreadMeasurements.map(m => m.id);
    const readMeasurements = localStorage.getItem("healthReadMeasurements");
    const readIds = readMeasurements ? JSON.parse(readMeasurements) : [];
    const updatedIds = [...new Set([...readIds, ...allIds])];
    localStorage.setItem("healthReadMeasurements", JSON.stringify(updatedIds));
    loadData();
  };

  const getInitials = (name: string) => {
    const parts = name.split(" ");
    if (parts.length >= 2) {
      return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  };

  const getAvatarColor = (id: string) => {
    const colors = ["#2196F3", "#4CAF50", "#FF9800", "#9C27B0", "#F44336", "#00BCD4"];
    const index = parseInt(id) % colors.length;
    return colors[index];
  };

  const getMeasurementIcon = (type: string) => {
    switch (type) {
      case "cukier": return <Opacity sx={{ fontSize: 24 }} />;
      case "insulina": return <LocalHospital sx={{ fontSize: 24 }} />;
      case "ciśnienie": return <Favorite sx={{ fontSize: 24 }} />;
      case "tętno": return <MonitorHeart sx={{ fontSize: 24 }} />;
      default: return <Circle sx={{ fontSize: 24 }} />;
    }
  };

  const getMeasurementColor = (type: string) => {
    switch (type) {
      case "cukier": return "#2196F3";
      case "insulina": return "#4CAF50";
      case "ciśnienie": return "#F44336";
      case "tętno": return "#9C27B0";
      default: return "#757575";
    }
  };

  const formatTimestamp = (timestamp: string) => {
    const date = new Date(timestamp);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return "przed chwilą";
    if (diffMins < 60) return `${diffMins} min temu`;
    if (diffHours < 24) return `${diffHours} godz. temu`;
    if (diffDays < 7) return `${diffDays} dni temu`;
    
    return date.toLocaleDateString("pl-PL", { day: "2-digit", month: "2-digit", year: "numeric" });
  };

  // Przygotuj dane do wykresu (top 10 zmian)
  const chartData = patientChanges.slice(0, 10).map((change) => ({
    name: `${change.patientName.split(" ")[0]} - ${change.type}`,
    zmiana: parseFloat(change.changePercent.toFixed(1)),
    fill: change.trend === "up" ? "#F44336" : "#4CAF50",
  }));

  return (
    <Box sx={{ backgroundColor: "#f5f5f5", minHeight: "100vh", pb: 2 }}>
      {/* Header */}
      <Box
        sx={{
          backgroundColor: "#2196F3",
          color: "white",
          py: 3,
          px: 2,
          boxShadow: "0 2px 4px rgba(0,0,0,0.2)",
          position: "relative",
        }}
      >
        <Typography variant="h4" component="h1" align="center" sx={{ fontWeight: 500 }}>
          Panel Opiekuna
        </Typography>
        <Typography variant="body1" align="center" sx={{ mt: 1, opacity: 0.9 }}>
          Zarządzaj swoimi podopiecznymi
        </Typography>
        <IconButton
          onClick={handleLogout}
          sx={{
            position: "absolute",
            top: 8,
            right: 8,
            color: "white",
            backgroundColor: "rgba(255,255,255,0.2)",
            "&:hover": {
              backgroundColor: "rgba(255,255,255,0.3)",
            },
          }}
        >
          <Logout sx={{ fontSize: 28 }} />
        </IconButton>
      </Box>

      {/* Tabs */}
      <Box sx={{ borderBottom: 1, borderColor: "divider", backgroundColor: "white" }}>
        <Container maxWidth="md">
          <Tabs
            value={currentTab}
            onChange={(_, value) => setCurrentTab(value)}
            centered
            sx={{
              "& .MuiTab-root": {
                fontSize: "16px",
                fontWeight: 500,
                py: 2,
              },
            }}
          >
            <Tab
              icon={<DashboardIcon />}
              iconPosition="start"
              label={
                <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                  Dashboard
                  {unreadMeasurements.length > 0 && (
                    <Chip
                      label={unreadMeasurements.length}
                      size="small"
                      color="error"
                      sx={{ height: 20, fontSize: "12px" }}
                    />
                  )}
                </Box>
              }
              value="dashboard"
            />
            <Tab icon={<People />} iconPosition="start" label="Podopieczni" value="profiles" />
            <Tab icon={<CalendarMonth />} iconPosition="start" label="Wizyty" value="visits" />
          </Tabs>
        </Container>
      </Box>

      <Container maxWidth="md" sx={{ mt: 3, px: 2 }}>
        {currentTab === "dashboard" ? (
          // Dashboard View
          <>
            {patients.length === 0 ? (
              <Box sx={{ textAlign: "center", py: 6 }}>
                <People sx={{ fontSize: 120, color: "#ccc", mb: 3 }} />
                <Typography variant="h5" sx={{ mb: 2, fontWeight: 500, color: "#666" }}>
                  Brak podopiecznych
                </Typography>
                <Typography variant="body1" sx={{ mb: 4, color: "#999" }}>
                  Dodaj osoby, które chcesz monitorować
                </Typography>
                <Button
                  variant="contained"
                  size="large"
                  startIcon={<People />}
                  onClick={() => setCurrentTab("profiles")}
                  sx={{
                    py: 2,
                    px: 4,
                    fontSize: "1.2rem",
                    fontWeight: 500,
                    backgroundColor: "#2196F3",
                    "&:hover": {
                      backgroundColor: "#1976d2",
                    },
                  }}
                >
                  Przejdź do listy podopiecznych
                </Button>
              </Box>
            ) : (
              <>
                {/* Sekcja wykresów zmian */}
                <Card sx={{ mb: 3, boxShadow: "0 2px 8px rgba(0,0,0,0.15)" }}>
                  <CardContent sx={{ p: 3 }}>
                    <Box sx={{ display: "flex", alignItems: "center", gap: 1, mb: 2 }}>
                      <TrendingUp sx={{ fontSize: 28, color: "#2196F3" }} />
                      <Typography variant="h6" sx={{ fontWeight: 600 }}>
                        Największe zmiany pomiarów
                      </Typography>
                    </Box>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                      Porównanie ostatnich dwóch pomiarów dla każdego parametru
                    </Typography>

                    {patientChanges.length === 0 ? (
                      <Box sx={{ textAlign: "center", py: 4 }}>
                        <Typography variant="body1" color="text.secondary">
                          Brak wystarczających danych do analizy
                        </Typography>
                        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                          Potrzeba co najmniej 2 pomiary tego samego typu
                        </Typography>
                      </Box>
                    ) : (
                      <ResponsiveContainer width="100%" height={300}>
                        <BarChart data={chartData} layout="horizontal">
                          <CartesianGrid strokeDasharray="3 3" />
                          <XAxis type="number" unit="%" />
                          <YAxis dataKey="name" type="category" width={120} style={{ fontSize: "12px" }} />
                          <Tooltip
                            formatter={(value: number) => `${value > 0 ? "+" : ""}${value.toFixed(1)}%`}
                            labelStyle={{ fontWeight: 600 }}
                          />
                          <Bar dataKey="zmiana" radius={[0, 4, 4, 0]}>
                            {chartData.map((entry, index) => (
                              <Cell key={`cell-${index}`} fill={entry.fill} />
                            ))}
                          </Bar>
                        </BarChart>
                      </ResponsiveContainer>
                    )}
                  </CardContent>
                </Card>

                {/* Sekcja nieodczytanych powiadomień */}
                <Card sx={{ mb: 3, boxShadow: "0 2px 8px rgba(0,0,0,0.15)" }}>
                  <CardContent sx={{ p: 3 }}>
                    <Box sx={{ display: "flex", alignItems: "center", justifyContent: "space-between", mb: 2 }}>
                      <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                        <Notifications sx={{ fontSize: 28, color: "#FF9800" }} />
                        <Typography variant="h6" sx={{ fontWeight: 600 }}>
                          Nowe pomiary
                        </Typography>
                        {unreadMeasurements.length > 0 && (
                          <Chip
                            label={unreadMeasurements.length}
                            size="small"
                            color="error"
                            sx={{ fontWeight: 600 }}
                          />
                        )}
                      </Box>
                      {unreadMeasurements.length > 0 && (
                        <Button
                          size="small"
                          onClick={handleMarkAllAsRead}
                          sx={{ fontSize: "13px", textTransform: "none" }}
                        >
                          Oznacz wszystkie jako przeczytane
                        </Button>
                      )}
                    </Box>

                    {unreadMeasurements.length === 0 ? (
                      <Box sx={{ textAlign: "center", py: 4 }}>
                        <Typography variant="body1" color="text.secondary">
                          🎉 Wszystkie pomiary przeczytane!
                        </Typography>
                        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                          Nie masz nowych powiadomień
                        </Typography>
                      </Box>
                    ) : (
                      <List sx={{ p: 0 }}>
                        {unreadMeasurements.map((measurement) => (
                          <ListItem
                            key={measurement.id}
                            sx={{
                              border: "1px solid #e0e0e0",
                              borderRadius: "8px",
                              mb: 1,
                              backgroundColor: "#fffde7",
                              "&:hover": {
                                backgroundColor: "#fff9c4",
                              },
                            }}
                            secondaryAction={
                              <Button
                                size="small"
                                onClick={() => handleMarkAsRead(measurement.id)}
                                sx={{ fontSize: "12px", textTransform: "none" }}
                              >
                                Przeczytane
                              </Button>
                            }
                          >
                            <ListItemAvatar>
                              <Avatar
                                sx={{
                                  backgroundColor: getMeasurementColor(measurement.type),
                                  width: 48,
                                  height: 48,
                                }}
                              >
                                {getMeasurementIcon(measurement.type)}
                              </Avatar>
                            </ListItemAvatar>
                            <ListItemText
                              primary={
                                <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                                  <Typography variant="body1" sx={{ fontWeight: 600 }}>
                                    {measurement.patientName}
                                  </Typography>
                                  <Chip
                                    label={measurement.type}
                                    size="small"
                                    sx={{
                                      backgroundColor: getMeasurementColor(measurement.type),
                                      color: "white",
                                      fontSize: "11px",
                                      height: 20,
                                    }}
                                  />
                                </Box>
                              }
                              secondary={
                                <Box>
                                  <Typography variant="body2" sx={{ fontWeight: 500 }}>
                                    Wartość: {measurement.value}
                                  </Typography>
                                  <Typography variant="caption" color="text.secondary">
                                    {formatTimestamp(measurement.timestamp)}
                                  </Typography>
                                </Box>
                              }
                            />
                          </ListItem>
                        ))}
                      </List>
                    )}
                  </CardContent>
                </Card>
              </>
            )}
          </>
        ) : currentTab === "visits" ? (
          // Visits View
          <Visits />
        ) : (
          // Profiles View
          <>
            {patients.length === 0 ? (
              <Box sx={{ textAlign: "center", py: 6 }}>
                <People sx={{ fontSize: 120, color: "#ccc", mb: 3 }} />
                <Typography variant="h5" sx={{ mb: 2, fontWeight: 500, color: "#666" }}>
                  Brak podopiecznych
                </Typography>
                <Typography variant="body1" sx={{ mb: 4, color: "#999" }}>
                  Dodaj osobę, którą chcesz monitorować
                </Typography>
                <Button
                  variant="contained"
                  size="large"
                  onClick={() => navigate("/home/select-patient")}
                  sx={{
                    py: 2,
                    px: 4,
                    fontSize: "1.2rem",
                    fontWeight: 500,
                    backgroundColor: "#2196F3",
                    "&:hover": {
                      backgroundColor: "#1976d2",
                    },
                  }}
                >
                  Dodaj podopiecznego
                </Button>
              </Box>
            ) : (
              <>
                <Typography variant="h6" sx={{ mb: 3, color: "#666", fontWeight: 500 }}>
                  Wybierz osobę, aby zarządzać jej danymi zdrowotnymi:
                </Typography>
                <Box
                  sx={{
                    display: "grid",
                    gridTemplateColumns: { xs: "1fr", sm: "1fr 1fr" },
                    gap: 3,
                  }}
                >
                  {patients.map((patient) => (
                    <Card
                      key={patient.id}
                      sx={{
                        cursor: "pointer",
                        transition: "all 0.2s",
                        position: "relative",
                        "&:hover": {
                          transform: "translateY(-4px)",
                          boxShadow: "0 8px 16px rgba(0,0,0,0.2)",
                        },
                        "&:active": {
                          transform: "scale(0.98)",
                        },
                        boxShadow: "0 2px 8px rgba(0,0,0,0.15)",
                      }}
                      onClick={() => handleSelectPatient(patient)}
                    >
                      {unreadCounts[patient.id] > 0 && (
                        <Badge
                          badgeContent={unreadCounts[patient.id]}
                          color="error"
                          sx={{
                            position: "absolute",
                            top: 16,
                            right: 16,
                            "& .MuiBadge-badge": {
                              fontSize: "14px",
                              fontWeight: 700,
                              height: 28,
                              minWidth: 28,
                              borderRadius: "14px",
                            },
                          }}
                        >
                          <Notifications sx={{ fontSize: 32, color: "#FF9800" }} />
                        </Badge>
                      )}
                      <CardContent
                        sx={{
                          display: "flex",
                          flexDirection: "column",
                          alignItems: "center",
                          gap: 2,
                          py: 4,
                        }}
                      >
                        <Avatar
                          sx={{
                            width: 80,
                            height: 80,
                            fontSize: "2rem",
                            fontWeight: 700,
                            backgroundColor: getAvatarColor(patient.id),
                          }}
                        >
                          {getInitials(patient.name)}
                        </Avatar>
                        <Typography variant="h5" sx={{ fontWeight: 500, textAlign: "center" }}>
                          {patient.name}
                        </Typography>
                        {patient.phone && (
                          <Typography variant="body2" color="text.secondary">
                            {patient.phone}
                          </Typography>
                        )}
                        <Button
                          variant="contained"
                          size="large"
                          fullWidth
                          sx={{
                            mt: 1,
                            py: 1.5,
                            fontSize: "1.1rem",
                            fontWeight: 500,
                            backgroundColor: "#2196F3",
                            "&:hover": {
                              backgroundColor: "#1976d2",
                            },
                          }}
                        >
                          Wybierz
                        </Button>
                      </CardContent>
                    </Card>
                  ))}
                </Box>

                <Button
                  fullWidth
                  variant="outlined"
                  size="large"
                  onClick={() => navigate("/home/select-patient")}
                  sx={{
                    mt: 3,
                    py: 2,
                    fontSize: "1.1rem",
                    fontWeight: 500,
                    borderColor: "#2196F3",
                    color: "#2196F3",
                    "&:hover": {
                      borderColor: "#1976d2",
                      backgroundColor: "#e3f2fd",
                    },
                  }}
                >
                  + Dodaj nowego podopiecznego
                </Button>
              </>
            )}
          </>
        )}
      </Container>
    </Box>
  );
}