import { useState, useEffect } from "react";
import { useNavigate } from "react-router";
import { Button, Card, CardContent, Container, Typography, Box, Fab, Dialog, DialogTitle, DialogContent, DialogActions, IconButton, Avatar, Chip } from "@mui/material";
import { Opacity, LocalHospital, Favorite, MonitorHeart, History as HistoryIcon, People, Notifications, MedicalServices, Warning, Settings as SettingsIcon, Logout, SwapHoriz } from "@mui/icons-material";
import { VoiceInputModal } from "./VoiceInputModal";

type MeasurementType = "cukier" | "insulina" | "ciśnienie" | "tętno";

export function Home() {
  const navigate = useNavigate();
  const [showModal, setShowModal] = useState(false);
  const [currentType, setCurrentType] = useState<MeasurementType | null>(null);
  const [showEmergencyDialog, setShowEmergencyDialog] = useState(false);
  
  // Pobierz dane użytkownika i wybranego pacjenta
  const userRole = localStorage.getItem("healthAppUserRole") || "";
  const selectedPatientData = localStorage.getItem("healthAppSelectedPatient");
  const selectedPatient = selectedPatientData ? JSON.parse(selectedPatientData) : null;

  // Jeśli opiekun i brak wybranego pacjenta, przekieruj do dashboardu
  useEffect(() => {
    if (userRole === "agent" && !selectedPatient) {
      navigate("/home/caregiver-dashboard", { replace: true });
    }
  }, [userRole, selectedPatient, navigate]);

  const handleEmergencyCall = () => {
    setShowEmergencyDialog(true);
    
    // Dzwoń na 112
    setTimeout(() => {
      window.location.href = "tel:112";
    }, 500);

    // Wyślij SMS do wszystkich agentów i lekarzy
    const agents = localStorage.getItem("healthAgents");
    const doctors = localStorage.getItem("healthDoctors");
    
    const agentsList = agents ? JSON.parse(agents) : [];
    const doctorsList = doctors ? JSON.parse(doctors) : [];
    
    const allContacts = [...agentsList, ...doctorsList];
    
    if (allContacts.length > 0) {
      const message = "PILNE! Wezwałem/am pomoc medyczną (112). Proszę o kontakt.";
      
      // Wysyłanie SMS do każdego kontaktu
      allContacts.forEach((contact: any) => {
        if (contact.phone) {
          const phoneNumber = contact.phone.replace(/\s/g, '');
          // Otwórz aplikację SMS (każdy SMS osobno)
          setTimeout(() => {
            window.open(`sms:${phoneNumber}?body=${encodeURIComponent(message)}`, '_blank');
          }, 100);
        }
      });
    }

    // Zamknij dialog po 3 sekundach
    setTimeout(() => {
      setShowEmergencyDialog(false);
    }, 3000);
  };

  const handleButtonClick = (type: MeasurementType) => {
    setCurrentType(type);
    setShowModal(true);
  };

  const handleSave = (value: string) => {
    if (!currentType) return;

    const measurement = {
      type: currentType,
      value,
      timestamp: new Date().toISOString(),
      // Jeśli jesteś opiekunem, zapisz ID pacjenta
      patientId: userRole === "agent" && selectedPatient ? selectedPatient.id : "self",
    };

    const storageKey = userRole === "agent" && selectedPatient 
      ? `healthMeasurements_${selectedPatient.id}`
      : "healthMeasurements";

    const existing = localStorage.getItem(storageKey);
    const measurements = existing ? JSON.parse(existing) : [];
    measurements.push(measurement);
    localStorage.setItem(storageKey, JSON.stringify(measurements));

    // Sprawdź alerty
    checkAndCreateAlerts(currentType, value, measurements);

    setShowModal(false);
    setCurrentType(null);
  };

  const checkAndCreateAlerts = (type: MeasurementType, value: string, allMeasurements: any[]) => {
    // Załaduj ustawienia alertów
    const alertSettingsData = localStorage.getItem("healthAlertSettings");
    if (!alertSettingsData) return;

    const alertSettings = JSON.parse(alertSettingsData);
    const config = alertSettings[type];

    if (!config || !config.enabled) return;

    // Wyciągnij wartość numeryczną
    let numericValue = 0;
    if (type === "ciśnienie") {
      const match = value.match(/\d+/);
      numericValue = match ? parseInt(match[0]) : 0;
    } else {
      numericValue = parseFloat(value.replace(/[^\d.]/g, "")) || 0;
    }

    if (numericValue === 0) return;

    const alerts: any[] = [];

    // Sprawdź wartości krytyczne
    if (config.lowCritical && numericValue < config.lowCritical) {
      alerts.push({
        id: `${Date.now()}_low`,
        type,
        alertType: "critical_low",
        value,
        numericValue,
        threshold: config.lowCritical,
        timestamp: new Date().toISOString(),
        patientId: userRole === "agent" && selectedPatient ? selectedPatient.id : "self",
        patientName: selectedPatient?.name || "Pacjent",
      });
    }

    if (config.highCritical && numericValue > config.highCritical) {
      alerts.push({
        id: `${Date.now()}_high`,
        type,
        alertType: "critical_high",
        value,
        numericValue,
        threshold: config.highCritical,
        timestamp: new Date().toISOString(),
        patientId: userRole === "agent" && selectedPatient ? selectedPatient.id : "self",
        patientName: selectedPatient?.name || "Pacjent",
      });
    }

    // Sprawdź gwałtowne zmiany
    if (config.rapidChangeEnabled && config.rapidChangePercent && config.rapidChangeMeasurements) {
      const recentMeasurements = allMeasurements
        .filter((m: any) => m.type === type)
        .slice(-config.rapidChangeMeasurements);

      if (recentMeasurements.length >= 2) {
        const firstValue = extractNumericValue(recentMeasurements[0].value, type);
        const lastValue = numericValue;
        const change = Math.abs(((lastValue - firstValue) / firstValue) * 100);

        if (change >= config.rapidChangePercent) {
          alerts.push({
            id: `${Date.now()}_rapid`,
            type,
            alertType: "rapid_change",
            value,
            numericValue,
            change: change.toFixed(1),
            threshold: config.rapidChangePercent,
            measurementCount: recentMeasurements.length,
            timestamp: new Date().toISOString(),
            patientId: userRole === "agent" && selectedPatient ? selectedPatient.id : "self",
            patientName: selectedPatient?.name || "Pacjent",
          });
        }
      }
    }

    // Zapisz alerty
    if (alerts.length > 0) {
      const existingAlerts = localStorage.getItem("healthAlerts");
      const allAlerts = existingAlerts ? JSON.parse(existingAlerts) : [];
      allAlerts.push(...alerts);
      localStorage.setItem("healthAlerts", JSON.stringify(allAlerts));
    }
  };

  const extractNumericValue = (value: string, type: MeasurementType): number => {
    if (type === "ciśnienie") {
      const match = value.match(/\d+/);
      return match ? parseInt(match[0]) : 0;
    }
    return parseFloat(value.replace(/[^\d.]/g, "")) || 0;
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

  const buttons = [
    {
      type: "cukier" as MeasurementType,
      label: "Cukier",
      icon: Opacity,
      color: "#2196F3",
    },
    {
      type: "insulina" as MeasurementType,
      label: "Insulina",
      icon: LocalHospital,
      color: "#4CAF50",
    },
    {
      type: "ciśnienie" as MeasurementType,
      label: "Ciśnienie",
      icon: Favorite,
      color: "#F44336",
    },
    {
      type: "tętno" as MeasurementType,
      label: "Tętno",
      icon: MonitorHeart,
      color: "#9C27B0",
    },
  ];

  return (
    <Box sx={{ backgroundColor: "#f5f5f5", minHeight: "100vh", pb: 2 }}>
      <Box
        sx={{
          backgroundColor: "#1976d2",
          color: "white",
          py: 3,
          px: 2,
          boxShadow: "0 2px 4px rgba(0,0,0,0.2)",
          position: "relative",
        }}
      >
        <Typography variant="h4" component="h1" align="center" sx={{ fontWeight: 500 }}>
          Monitor Zdrowia
        </Typography>
        <IconButton
          onClick={() => {
            localStorage.removeItem("healthAppLoggedIn");
            localStorage.removeItem("healthAppPhone");
            localStorage.removeItem("healthAppLoginDate");
            localStorage.removeItem("healthAppUserRole");
            localStorage.removeItem("healthAppAgentData");
            localStorage.removeItem("healthAppSelectedRole");
            navigate("/");
          }}
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

      <Container maxWidth="md" sx={{ mt: 3, px: 2 }}>
        {/* Banner dla opiekuna - wybrany pacjent */}
        {userRole === "agent" && selectedPatient && (
          <Card
            sx={{
              mb: 3,
              backgroundColor: "#e3f2fd",
              border: "2px solid #2196F3",
              boxShadow: "0 2px 8px rgba(33,150,243,0.3)",
            }}
          >
            <CardContent sx={{ p: 2 }}>
              <Box sx={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
                <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
                  <Avatar
                    sx={{
                      width: 56,
                      height: 56,
                      fontSize: "1.5rem",
                      fontWeight: 700,
                      backgroundColor: getAvatarColor(selectedPatient.id),
                    }}
                  >
                    {getInitials(selectedPatient.name)}
                  </Avatar>
                  <Box>
                    <Typography variant="body2" sx={{ color: "#666", fontSize: "0.9rem", mb: 0.5 }}>
                      Zarządzasz danymi dla:
                    </Typography>
                    <Typography variant="h6" sx={{ fontWeight: 600, color: "#1976d2" }}>
                      {selectedPatient.name}
                    </Typography>
                    {selectedPatient.phone && (
                      <Typography variant="body2" sx={{ color: "#666", fontSize: "0.85rem" }}>
                        {selectedPatient.phone}
                      </Typography>
                    )}
                  </Box>
                </Box>
                <IconButton
                  onClick={() => {
                    localStorage.removeItem("healthAppSelectedPatient");
                    navigate("/home/caregiver-dashboard");
                  }}
                  sx={{
                    backgroundColor: "#2196F3",
                    color: "white",
                    "&:hover": {
                      backgroundColor: "#1976d2",
                    },
                  }}
                >
                  <SwapHoriz sx={{ fontSize: 28 }} />
                </IconButton>
              </Box>
            </CardContent>
          </Card>
        )}

        <Box
          sx={{
            display: "grid",
            gridTemplateColumns: { xs: "1fr", sm: "1fr 1fr" },
            gap: 2,
            mb: 3,
          }}
        >
          {buttons.map((button) => (
            <Card
              key={button.type}
              sx={{
                cursor: "pointer",
                transition: "all 0.2s",
                "&:active": {
                  transform: "scale(0.98)",
                },
                boxShadow: "0 2px 4px rgba(0,0,0,0.2)",
              }}
              onClick={() => handleButtonClick(button.type)}
            >
              <CardContent
                sx={{
                  display: "flex",
                  flexDirection: "column",
                  alignItems: "center",
                  gap: 2,
                  py: 4,
                  backgroundColor: button.color,
                  color: "white",
                  "&:last-child": { pb: 4 },
                }}
              >
                <button.icon sx={{ fontSize: 72 }} />
                <Typography variant="h5" component="div" sx={{ fontWeight: 500 }}>
                  {button.label}
                </Typography>
              </CardContent>
            </Card>
          ))}
        </Box>

        <Box
          sx={{
            display: "grid",
            gridTemplateColumns: { xs: "1fr", sm: "1fr 1fr" },
            gap: 2,
            mb: 3,
          }}
        >
          <Button
            fullWidth
            variant="contained"
            size="large"
            onClick={() => {
              // TODO: Implementacja powiadomień dla agentów
              alert("Powiadomienie wysłane do wszystkich agentów!");
            }}
            sx={{
              py: 4,
              fontSize: "1.4rem",
              fontWeight: 500,
              textTransform: "none",
              boxShadow: "0 2px 4px rgba(0,0,0,0.2)",
              backgroundColor: "#03A9F4",
              "&:hover": {
                backgroundColor: "#0288D1",
              },
              display: "flex",
              flexDirection: "column",
              gap: 0.5,
            }}
          >
            <Notifications sx={{ fontSize: 56, mb: 1 }} />
            <Box sx={{ textAlign: "center", lineHeight: 1.3 }}>
              <Typography sx={{ fontSize: "1.4rem", fontWeight: 500 }}>Powiadom</Typography>
              <Typography sx={{ fontSize: "1.4rem", fontWeight: 500 }}>Agentów</Typography>
            </Box>
          </Button>

          <Button
            fullWidth
            variant="contained"
            size="large"
            onClick={() => {
              // TODO: Implementacja powiadomień dla lekarzy
              alert("Powiadomienie wysłane do wszystkich lekarzy!");
            }}
            sx={{
              py: 4,
              fontSize: "1.4rem",
              fontWeight: 500,
              textTransform: "none",
              boxShadow: "0 2px 4px rgba(0,0,0,0.2)",
              backgroundColor: "#E91E63",
              "&:hover": {
                backgroundColor: "#C2185B",
              },
              display: "flex",
              flexDirection: "column",
              gap: 0.5,
            }}
          >
            <MedicalServices sx={{ fontSize: 56, mb: 1 }} />
            <Box sx={{ textAlign: "center", lineHeight: 1.3 }}>
              <Typography sx={{ fontSize: "1.4rem", fontWeight: 500 }}>Powiadom</Typography>
              <Typography sx={{ fontSize: "1.4rem", fontWeight: 500 }}>Lekarzy</Typography>
            </Box>
          </Button>
        </Box>

        <Button
          fullWidth
          variant="contained"
          size="large"
          onClick={handleEmergencyCall}
          sx={{
            py: 4,
            fontSize: "1.6rem",
            fontWeight: 700,
            textTransform: "none",
            boxShadow: "0 4px 8px rgba(0,0,0,0.3)",
            backgroundColor: "#D32F2F",
            color: "white",
            mb: 3,
            "&:hover": {
              backgroundColor: "#B71C1C",
            },
            "&:active": {
              transform: "scale(0.98)",
            },
            display: "flex",
            flexDirection: "column",
            gap: 1,
          }}
        >
          <Warning sx={{ fontSize: 72 }} />
          <Typography sx={{ fontSize: "1.6rem", fontWeight: 700 }}>
            WEZWIJ POMOC
          </Typography>
        </Button>

        <Button
          fullWidth
          variant="contained"
          size="large"
          startIcon={<HistoryIcon />}
          onClick={() => navigate("/home/historia")}
          sx={{
            py: 2,
            fontSize: "1.25rem",
            fontWeight: 500,
            textTransform: "none",
            boxShadow: "0 2px 4px rgba(0,0,0,0.2)",
            backgroundColor: "#424242",
            "&:hover": {
              backgroundColor: "#616161",
            },
            mb: 2,
          }}
        >
          Zobacz Historię
        </Button>

        <Button
          fullWidth
          variant="contained"
          size="large"
          startIcon={<People />}
          onClick={() => navigate("/home/agenci")}
          sx={{
            py: 2,
            fontSize: "1.25rem",
            fontWeight: 500,
            textTransform: "none",
            boxShadow: "0 2px 4px rgba(0,0,0,0.2)",
            backgroundColor: "#FF9800",
            "&:hover": {
              backgroundColor: "#FB8C00",
            },
            mb: 2,
          }}
        >
          Agenci Monitorujący
        </Button>

        <Button
          fullWidth
          variant="contained"
          size="large"
          startIcon={<SettingsIcon />}
          onClick={() => navigate("/home/ustawienia")}
          sx={{
            py: 2,
            fontSize: "1.25rem",
            fontWeight: 500,
            textTransform: "none",
            boxShadow: "0 2px 4px rgba(0,0,0,0.2)",
            backgroundColor: "#9C27B0",
            "&:hover": {
              backgroundColor: "#7B1FA2",
            },
          }}
        >
          Moje Ustawienia
        </Button>
      </Container>

      {showModal && currentType && (
        <VoiceInputModal
          type={currentType}
          onClose={() => {
            setShowModal(false);
            setCurrentType(null);
          }}
          onSave={handleSave}
        />
      )}

      {/* Dialog potwierdzający wezwanie pomocy */}
      <Dialog
        open={showEmergencyDialog}
        onClose={() => setShowEmergencyDialog(false)}
        maxWidth="sm"
        fullWidth
        PaperProps={{
          sx: {
            borderRadius: "12px",
            backgroundColor: "#ffebee",
          },
        }}
      >
        <DialogContent>
          <Box sx={{ textAlign: "center", py: 3 }}>
            <Warning sx={{ fontSize: 80, color: "#D32F2F", mb: 2 }} />
            <Typography variant="h4" sx={{ mb: 2, fontWeight: 700, color: "#D32F2F" }}>
              Wzywam Pomoc!
            </Typography>
            <Typography variant="h6" sx={{ mb: 2, color: "#C62828" }}>
              Połączenie z numerem 112...
            </Typography>
            <Typography variant="body1" sx={{ color: "#B71C1C" }}>
              Powiadomienia SMS wysłane do wszystkich agentów i lekarzy
            </Typography>
          </Box>
        </DialogContent>
      </Dialog>
    </Box>
  );
}