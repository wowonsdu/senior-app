import { useState, useEffect } from "react";
import { useNavigate } from "react-router";
import {
  Box,
  Container,
  Typography,
  IconButton,
  Card,
  CardContent,
  TextField,
  Button,
  Switch,
  FormControlLabel,
  Divider,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Alert,
} from "@mui/material";
import {
  ArrowBack,
  Opacity,
  LocalHospital,
  Favorite,
  MonitorHeart,
  ExpandMore,
  Save,
  NotificationsActive,
  Person,
  PersonAdd,
  Sms,
  Email,
  Notifications,
} from "@mui/icons-material";

interface AlertConfig {
  enabled: boolean;
  lowCritical?: number;
  highCritical?: number;
  rapidChangeEnabled: boolean;
  rapidChangePercent?: number;
  rapidChangeMeasurements?: number;
  assignedAgents?: string[]; // Lista ID opiekunów
  notificationChannels?: {
    sms: boolean;
    email: boolean;
    inApp: boolean;
  };
}

interface AlertSettings {
  cukier: AlertConfig;
  insulina: AlertConfig;
  ciśnienie: AlertConfig;
  tętno: AlertConfig;
}

interface Agent {
  id: string;
  name: string;
  phone: string;
  email: string;
  accessCode: string;
  relation: string;
  type: "agent" | "doctor";
  specialization?: string;
}

const defaultSettings: AlertSettings = {
  cukier: {
    enabled: true,
    lowCritical: 70,
    highCritical: 180,
    rapidChangeEnabled: true,
    rapidChangePercent: 20,
    rapidChangeMeasurements: 3,
    assignedAgents: [],
    notificationChannels: {
      sms: true,
      email: true,
      inApp: true,
    },
  },
  insulina: {
    enabled: true,
    lowCritical: 5,
    highCritical: 15,
    rapidChangeEnabled: true,
    rapidChangePercent: 30,
    rapidChangeMeasurements: 3,
    assignedAgents: [],
    notificationChannels: {
      sms: true,
      email: true,
      inApp: true,
    },
  },
  ciśnienie: {
    enabled: true,
    lowCritical: 90,
    highCritical: 140,
    rapidChangeEnabled: true,
    rapidChangePercent: 15,
    rapidChangeMeasurements: 3,
    assignedAgents: [],
    notificationChannels: {
      sms: true,
      email: true,
      inApp: true,
    },
  },
  tętno: {
    enabled: true,
    lowCritical: 60,
    highCritical: 100,
    rapidChangeEnabled: true,
    rapidChangePercent: 25,
    rapidChangeMeasurements: 3,
    assignedAgents: [],
    notificationChannels: {
      sms: true,
      email: true,
      inApp: true,
    },
  },
};

export function AlertSettings() {
  const navigate = useNavigate();
  const [settings, setSettings] = useState<AlertSettings>(defaultSettings);
  const [saved, setSaved] = useState(false);
  const [agents, setAgents] = useState<Agent[]>([]);

  useEffect(() => {
    // Załaduj zapisane ustawienia
    const savedSettings = localStorage.getItem("healthAlertSettings");
    if (savedSettings) {
      setSettings(JSON.parse(savedSettings));
    }

    // Załaduj listę opiekunów
    const savedAgents = localStorage.getItem("healthAgents");
    if (savedAgents) {
      setAgents(JSON.parse(savedAgents));
    }
  }, []);

  const handleSave = () => {
    localStorage.setItem("healthAlertSettings", JSON.stringify(settings));
    setSaved(true);
    setTimeout(() => setSaved(false), 3000);
  };

  const handleToggle = (type: keyof AlertSettings, field: keyof AlertConfig) => {
    setSettings((prev) => ({
      ...prev,
      [type]: {
        ...prev[type],
        [field]: !prev[type][field],
      },
    }));
  };

  const handleChange = (type: keyof AlertSettings, field: keyof AlertConfig, value: number) => {
    setSettings((prev) => ({
      ...prev,
      [type]: {
        ...prev[type],
        [field]: value,
      },
    }));
  };

  const handleToggleAgent = (type: keyof AlertSettings, agentId: string) => {
    setSettings((prev) => {
      const currentAgents = prev[type].assignedAgents || [];
      const newAgents = currentAgents.includes(agentId)
        ? currentAgents.filter((id) => id !== agentId)
        : [...currentAgents, agentId];

      return {
        ...prev,
        [type]: {
          ...prev[type],
          assignedAgents: newAgents,
        },
      };
    });
  };

  const handleAssignAllAgents = (type: keyof AlertSettings) => {
    setSettings((prev) => ({
      ...prev,
      [type]: {
        ...prev[type],
        assignedAgents: agents.map((agent) => agent.id),
      },
    }));
  };

  const handleClearAllAgents = (type: keyof AlertSettings) => {
    setSettings((prev) => ({
      ...prev,
      [type]: {
        ...prev[type],
        assignedAgents: [],
      },
    }));
  };

  const handleToggleChannel = (type: keyof AlertSettings, channel: "sms" | "email" | "inApp") => {
    setSettings((prev) => ({
      ...prev,
      [type]: {
        ...prev[type],
        notificationChannels: {
          ...prev[type].notificationChannels,
          [channel]: !prev[type].notificationChannels?.[channel],
        },
      },
    }));
  };

  const handleEnableAllChannels = (type: keyof AlertSettings) => {
    setSettings((prev) => ({
      ...prev,
      [type]: {
        ...prev[type],
        notificationChannels: {
          sms: true,
          email: true,
          inApp: true,
        },
      },
    }));
  };

  const handleDisableAllChannels = (type: keyof AlertSettings) => {
    setSettings((prev) => ({
      ...prev,
      [type]: {
        ...prev[type],
        notificationChannels: {
          sms: false,
          email: false,
          inApp: false,
        },
      },
    }));
  };

  const measurements = [
    {
      type: "cukier" as keyof AlertSettings,
      label: "Poziom cukru",
      icon: Opacity,
      color: "#2196F3",
      unit: "mg/dL",
      description: "Glikemia - poziom glukozy we krwi",
    },
    {
      type: "insulina" as keyof AlertSettings,
      label: "Dawka insuliny",
      icon: LocalHospital,
      color: "#4CAF50",
      unit: "jednostek",
      description: "Ilość podanej insuliny",
    },
    {
      type: "ciśnienie" as keyof AlertSettings,
      label: "Ciśnienie krwi",
      icon: Favorite,
      color: "#F44336",
      unit: "mmHg",
      description: "Ciśnienie skurczowe (górna wartość)",
    },
    {
      type: "tętno" as keyof AlertSettings,
      label: "Tętno",
      icon: MonitorHeart,
      color: "#9C27B0",
      unit: "bpm",
      description: "Częstość uderzeń serca na minutę",
    },
  ];

  return (
    <Box sx={{ backgroundColor: "#f5f5f5", minHeight: "100vh", pb: 2 }}>
      {/* Header */}
      <Box
        sx={{
          backgroundColor: "#FF9800",
          color: "white",
          py: 2,
          px: 2,
          display: "flex",
          alignItems: "center",
          gap: 2,
          boxShadow: "0 2px 4px rgba(0,0,0,0.2)",
        }}
      >
        <IconButton onClick={() => navigate("/home/ustawienia")} sx={{ color: "white" }}>
          <ArrowBack sx={{ fontSize: 32 }} />
        </IconButton>
        <Box sx={{ flex: 1, textAlign: "center", mr: 6 }}>
          <Typography variant="h5" component="h1" sx={{ fontWeight: 500 }}>
            Alerty i Powiadomienia
          </Typography>
          <Typography variant="body2" sx={{ opacity: 0.9, mt: 0.5 }}>
            Konfiguracja wartości krytycznych
          </Typography>
        </Box>
      </Box>

      <Container maxWidth="md" sx={{ mt: 3, px: 2 }}>
        {/* Alert po zapisaniu */}
        {saved && (
          <Alert severity="success" sx={{ mb: 3, fontSize: "16px" }}>
            ✓ Ustawienia zostały zapisane
          </Alert>
        )}

        {/* Info */}
        <Card sx={{ mb: 3, boxShadow: "0 2px 8px rgba(0,0,0,0.15)", backgroundColor: "#fff3cd" }}>
          <CardContent>
            <Box sx={{ display: "flex", alignItems: "center", gap: 2, mb: 1 }}>
              <NotificationsActive sx={{ fontSize: 32, color: "#FF9800" }} />
              <Typography variant="h6" sx={{ fontWeight: 600 }}>
                Jak działają alerty?
              </Typography>
            </Box>
            <Typography variant="body2" sx={{ color: "#856404", lineHeight: 1.6 }}>
              System automatycznie monitoruje wszystkie pomiary i powiadamia opiekunów gdy:
            </Typography>
            <ul style={{ marginTop: "8px", marginBottom: 0, color: "#856404" }}>
              <li><Typography variant="body2">Wartość jest <strong>krytycznie niska</strong> lub <strong>krytycznie wysoka</strong></Typography></li>
              <li><Typography variant="body2">Nastąpiła <strong>gwałtowna zmiana</strong> w ostatnich pomiarach</Typography></li>
            </ul>
          </CardContent>
        </Card>

        {/* Ustawienia dla każdego typu pomiaru */}
        {measurements.map((measurement) => {
          const config = settings[measurement.type];
          const MeasurementIcon = measurement.icon;

          return (
            <Accordion
              key={measurement.type}
              sx={{
                mb: 2,
                boxShadow: "0 2px 8px rgba(0,0,0,0.15)",
                "&:before": { display: "none" },
              }}
            >
              <AccordionSummary
                expandIcon={<ExpandMore />}
                sx={{
                  backgroundColor: config.enabled ? measurement.color : "#e0e0e0",
                  color: "white",
                  "&:hover": {
                    backgroundColor: config.enabled ? measurement.color : "#d0d0d0",
                  },
                }}
              >
                <Box sx={{ display: "flex", alignItems: "center", gap: 2, flex: 1 }}>
                  <MeasurementIcon sx={{ fontSize: 32 }} />
                  <Box>
                    <Typography variant="h6" sx={{ fontWeight: 500 }}>
                      {measurement.label}
                    </Typography>
                    <Typography variant="body2" sx={{ opacity: 0.9, fontSize: "13px" }}>
                      {measurement.description}
                    </Typography>
                  </Box>
                </Box>
              </AccordionSummary>
              <AccordionDetails sx={{ p: 3 }}>
                {/* Włącz/Wyłącz alerty */}
                <FormControlLabel
                  control={
                    <Switch
                      checked={config.enabled}
                      onChange={() => handleToggle(measurement.type, "enabled")}
                      color="primary"
                    />
                  }
                  label={
                    <Typography variant="body1" sx={{ fontWeight: 500 }}>
                      Włącz alerty dla {measurement.label.toLowerCase()}
                    </Typography>
                  }
                  sx={{ mb: 3 }}
                />

                {config.enabled && (
                  <>
                    <Divider sx={{ mb: 3 }} />

                    {/* Wartości krytyczne */}
                    <Typography variant="h6" sx={{ mb: 2, fontWeight: 600, color: measurement.color }}>
                      Wartości krytyczne
                    </Typography>
                    
                    <Box sx={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 2, mb: 3 }}>
                      <TextField
                        label={`Krytycznie niska (${measurement.unit})`}
                        type="number"
                        value={config.lowCritical || ""}
                        onChange={(e) => handleChange(measurement.type, "lowCritical", parseFloat(e.target.value))}
                        fullWidth
                        InputProps={{
                          sx: { fontSize: "18px" },
                        }}
                        InputLabelProps={{
                          sx: { fontSize: "15px" },
                        }}
                        helperText="Wartość poniżej tej granicy"
                      />
                      <TextField
                        label={`Krytycznie wysoka (${measurement.unit})`}
                        type="number"
                        value={config.highCritical || ""}
                        onChange={(e) => handleChange(measurement.type, "highCritical", parseFloat(e.target.value))}
                        fullWidth
                        InputProps={{
                          sx: { fontSize: "18px" },
                        }}
                        InputLabelProps={{
                          sx: { fontSize: "15px" },
                        }}
                        helperText="Wartość powyżej tej granicy"
                      />
                    </Box>

                    <Divider sx={{ mb: 3 }} />

                    {/* Gwałtowne zmiany */}
                    <Typography variant="h6" sx={{ mb: 2, fontWeight: 600, color: measurement.color }}>
                      Gwałtowne zmiany
                    </Typography>

                    <FormControlLabel
                      control={
                        <Switch
                          checked={config.rapidChangeEnabled}
                          onChange={() => handleToggle(measurement.type, "rapidChangeEnabled")}
                          color="primary"
                        />
                      }
                      label={
                        <Typography variant="body1" sx={{ fontWeight: 500 }}>
                          Monitoruj gwałtowne zmiany
                        </Typography>
                      }
                      sx={{ mb: 2 }}
                    />

                    {config.rapidChangeEnabled && (
                      <Box sx={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 2, mb: 2 }}>
                        <TextField
                          label="Zmiana o (%)"
                          type="number"
                          value={config.rapidChangePercent || ""}
                          onChange={(e) => handleChange(measurement.type, "rapidChangePercent", parseFloat(e.target.value))}
                          fullWidth
                          InputProps={{
                            sx: { fontSize: "18px" },
                          }}
                          InputLabelProps={{
                            sx: { fontSize: "15px" },
                          }}
                          helperText="Procent zmiany"
                        />
                        <TextField
                          label="W ciągu pomiarów"
                          type="number"
                          value={config.rapidChangeMeasurements || ""}
                          onChange={(e) => handleChange(measurement.type, "rapidChangeMeasurements", parseInt(e.target.value))}
                          fullWidth
                          InputProps={{
                            sx: { fontSize: "18px" },
                            inputProps: { min: 2, max: 10 },
                          }}
                          InputLabelProps={{
                            sx: { fontSize: "15px" },
                          }}
                          helperText="Liczba ostatnich pomiarów"
                        />
                      </Box>
                    )}

                    {config.rapidChangeEnabled && (
                      <Alert severity="info" sx={{ fontSize: "14px" }}>
                        <Typography variant="body2" sx={{ fontWeight: 500, mb: 0.5 }}>
                          Przykład:
                        </Typography>
                        <Typography variant="body2">
                          Alert pojawi się gdy zmiana wyniesie <strong>{config.rapidChangePercent}%</strong> lub więcej w ciągu ostatnich <strong>{config.rapidChangeMeasurements} pomiarów</strong>
                        </Typography>
                      </Alert>
                    )}

                    <Divider sx={{ my: 3 }} />

                    {/* Kanały powiadomień */}
                    <Typography variant="h6" sx={{ mb: 2, fontWeight: 600, color: measurement.color }}>
                      Kanały powiadomień
                    </Typography>

                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                      Wybierz sposoby w jakie opiekunowie otrzymają powiadomienia:
                    </Typography>

                    <Box sx={{ display: "flex", gap: 2, mb: 2, flexWrap: "wrap" }}>
                      <Button
                        size="small"
                        variant="outlined"
                        onClick={() => handleEnableAllChannels(measurement.type)}
                        sx={{ fontSize: "13px", textTransform: "none" }}
                      >
                        Włącz wszystkie
                      </Button>
                      <Button
                        size="small"
                        variant="outlined"
                        onClick={() => handleDisableAllChannels(measurement.type)}
                        sx={{ fontSize: "13px", textTransform: "none" }}
                      >
                        Wyłącz wszystkie
                      </Button>
                    </Box>

                    <Box
                      sx={{
                        display: "grid",
                        gridTemplateColumns: { xs: "1fr", sm: "1fr 1fr 1fr" },
                        gap: 2,
                        mb: 2,
                      }}
                    >
                      {/* SMS */}
                      <Card
                        sx={{
                          cursor: "pointer",
                          border: config.notificationChannels?.sms ? `2px solid ${measurement.color}` : "2px solid #e0e0e0",
                          backgroundColor: config.notificationChannels?.sms ? `${measurement.color}15` : "white",
                          transition: "all 0.2s",
                          "&:hover": {
                            transform: "translateY(-2px)",
                            boxShadow: "0 4px 8px rgba(0,0,0,0.15)",
                          },
                          "&:active": {
                            transform: "scale(0.98)",
                          },
                        }}
                        onClick={() => handleToggleChannel(measurement.type, "sms")}
                      >
                        <CardContent sx={{ p: 2, textAlign: "center" }}>
                          <Sms
                            sx={{
                              fontSize: 48,
                              color: config.notificationChannels?.sms ? measurement.color : "#9e9e9e",
                              mb: 1,
                            }}
                          />
                          <Typography variant="body1" sx={{ fontWeight: 600, mb: 0.5 }}>
                            SMS
                          </Typography>
                          <Typography variant="caption" color="text.secondary" sx={{ fontSize: "12px" }}>
                            Wiadomość tekstowa
                          </Typography>
                          <Box sx={{ mt: 1 }}>
                            <Switch
                              checked={config.notificationChannels?.sms || false}
                              onChange={() => {}}
                              sx={{
                                "& .MuiSwitch-switchBase.Mui-checked": {
                                  color: measurement.color,
                                },
                                "& .MuiSwitch-switchBase.Mui-checked + .MuiSwitch-track": {
                                  backgroundColor: measurement.color,
                                },
                              }}
                            />
                          </Box>
                        </CardContent>
                      </Card>

                      {/* Email */}
                      <Card
                        sx={{
                          cursor: "pointer",
                          border: config.notificationChannels?.email ? `2px solid ${measurement.color}` : "2px solid #e0e0e0",
                          backgroundColor: config.notificationChannels?.email ? `${measurement.color}15` : "white",
                          transition: "all 0.2s",
                          "&:hover": {
                            transform: "translateY(-2px)",
                            boxShadow: "0 4px 8px rgba(0,0,0,0.15)",
                          },
                          "&:active": {
                            transform: "scale(0.98)",
                          },
                        }}
                        onClick={() => handleToggleChannel(measurement.type, "email")}
                      >
                        <CardContent sx={{ p: 2, textAlign: "center" }}>
                          <Email
                            sx={{
                              fontSize: 48,
                              color: config.notificationChannels?.email ? measurement.color : "#9e9e9e",
                              mb: 1,
                            }}
                          />
                          <Typography variant="body1" sx={{ fontWeight: 600, mb: 0.5 }}>
                            Email
                          </Typography>
                          <Typography variant="caption" color="text.secondary" sx={{ fontSize: "12px" }}>
                            Wiadomość mailowa
                          </Typography>
                          <Box sx={{ mt: 1 }}>
                            <Switch
                              checked={config.notificationChannels?.email || false}
                              onChange={() => {}}
                              sx={{
                                "& .MuiSwitch-switchBase.Mui-checked": {
                                  color: measurement.color,
                                },
                                "& .MuiSwitch-switchBase.Mui-checked + .MuiSwitch-track": {
                                  backgroundColor: measurement.color,
                                },
                              }}
                            />
                          </Box>
                        </CardContent>
                      </Card>

                      {/* W aplikacji */}
                      <Card
                        sx={{
                          cursor: "pointer",
                          border: config.notificationChannels?.inApp ? `2px solid ${measurement.color}` : "2px solid #e0e0e0",
                          backgroundColor: config.notificationChannels?.inApp ? `${measurement.color}15` : "white",
                          transition: "all 0.2s",
                          "&:hover": {
                            transform: "translateY(-2px)",
                            boxShadow: "0 4px 8px rgba(0,0,0,0.15)",
                          },
                          "&:active": {
                            transform: "scale(0.98)",
                          },
                        }}
                        onClick={() => handleToggleChannel(measurement.type, "inApp")}
                      >
                        <CardContent sx={{ p: 2, textAlign: "center" }}>
                          <Notifications
                            sx={{
                              fontSize: 48,
                              color: config.notificationChannels?.inApp ? measurement.color : "#9e9e9e",
                              mb: 1,
                            }}
                          />
                          <Typography variant="body1" sx={{ fontWeight: 600, mb: 0.5 }}>
                            W aplikacji
                          </Typography>
                          <Typography variant="caption" color="text.secondary" sx={{ fontSize: "12px" }}>
                            Push notification
                          </Typography>
                          <Box sx={{ mt: 1 }}>
                            <Switch
                              checked={config.notificationChannels?.inApp || false}
                              onChange={() => {}}
                              sx={{
                                "& .MuiSwitch-switchBase.Mui-checked": {
                                  color: measurement.color,
                                },
                                "& .MuiSwitch-switchBase.Mui-checked + .MuiSwitch-track": {
                                  backgroundColor: measurement.color,
                                },
                              }}
                            />
                          </Box>
                        </CardContent>
                      </Card>
                    </Box>

                    {/* Podsumowanie wybranych kanałów */}
                    {(config.notificationChannels?.sms || config.notificationChannels?.email || config.notificationChannels?.inApp) && (
                      <Alert severity="success" sx={{ fontSize: "14px", mb: 3 }}>
                        <Typography variant="body2">
                          Powiadomienia będą wysyłane przez: {" "}
                          <strong>
                            {[
                              config.notificationChannels?.sms && "SMS",
                              config.notificationChannels?.email && "Email",
                              config.notificationChannels?.inApp && "Aplikację",
                            ]
                              .filter(Boolean)
                              .join(", ")}
                          </strong>
                        </Typography>
                      </Alert>
                    )}

                    {!config.notificationChannels?.sms && !config.notificationChannels?.email && !config.notificationChannels?.inApp && (
                      <Alert severity="warning" sx={{ fontSize: "14px", mb: 3 }}>
                        <Typography variant="body2" sx={{ fontWeight: 500 }}>
                          Uwaga: Nie wybrano żadnego kanału powiadomień
                        </Typography>
                        <Typography variant="body2">
                          Opiekunowie nie otrzymają alertów dla {measurement.label.toLowerCase()}
                        </Typography>
                      </Alert>
                    )}

                    <Divider sx={{ my: 3 }} />

                    {/* Przypisani opiekunowie */}
                    <Typography variant="h6" sx={{ mb: 2, fontWeight: 600, color: measurement.color }}>
                      Powiadomienia dla opiekunów
                    </Typography>

                    {agents.length === 0 ? (
                      <Alert severity="warning" sx={{ fontSize: "14px" }}>
                        <Typography variant="body2" sx={{ fontWeight: 500, mb: 0.5 }}>
                          Brak dodanych opiekunów
                        </Typography>
                        <Typography variant="body2">
                          Aby przypisać opiekunów do powiadomień, najpierw dodaj ich w sekcji "Moi Agenci"
                        </Typography>
                        <Button
                          size="small"
                          variant="contained"
                          onClick={() => navigate("/home/agenci")}
                          sx={{
                            mt: 2,
                            fontSize: "13px",
                            textTransform: "none",
                            backgroundColor: measurement.color,
                            "&:hover": {
                              backgroundColor: measurement.color,
                              opacity: 0.9,
                            },
                          }}
                        >
                          Przejdź do "Moi Agenci"
                        </Button>
                      </Alert>
                    ) : (
                      <>
                        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                          Wybierz opiekunów, którzy otrzymają powiadomienia o alertach dla {measurement.label.toLowerCase()}:
                        </Typography>

                        <Box sx={{ display: "flex", gap: 2, mb: 2 }}>
                          <Button
                            size="small"
                            variant="outlined"
                            onClick={() => handleAssignAllAgents(measurement.type)}
                            sx={{ fontSize: "13px", textTransform: "none" }}
                          >
                            Zaznacz wszystkich
                          </Button>
                          <Button
                            size="small"
                            variant="outlined"
                            onClick={() => handleClearAllAgents(measurement.type)}
                            sx={{ fontSize: "13px", textTransform: "none" }}
                          >
                            Odznacz wszystkich
                          </Button>
                        </Box>

                        <Box
                          sx={{
                            display: "grid",
                            gridTemplateColumns: { xs: "1fr", sm: "1fr 1fr" },
                            gap: 2,
                          }}
                        >
                          {agents.map((agent) => {
                            const isAssigned = (config.assignedAgents || []).includes(agent.id);
                            
                            return (
                              <Card
                                key={agent.id}
                                sx={{
                                  cursor: "pointer",
                                  border: isAssigned ? `2px solid ${measurement.color}` : "2px solid #e0e0e0",
                                  backgroundColor: isAssigned ? `${measurement.color}15` : "white",
                                  transition: "all 0.2s",
                                  "&:hover": {
                                    transform: "translateY(-2px)",
                                    boxShadow: "0 4px 8px rgba(0,0,0,0.15)",
                                  },
                                  "&:active": {
                                    transform: "scale(0.98)",
                                  },
                                }}
                                onClick={() => handleToggleAgent(measurement.type, agent.id)}
                              >
                                <CardContent sx={{ p: 2 }}>
                                  <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
                                    <Switch
                                      checked={isAssigned}
                                      onChange={() => {}}
                                      sx={{
                                        "& .MuiSwitch-switchBase.Mui-checked": {
                                          color: measurement.color,
                                        },
                                        "& .MuiSwitch-switchBase.Mui-checked + .MuiSwitch-track": {
                                          backgroundColor: measurement.color,
                                        },
                                      }}
                                    />
                                    <Box sx={{ flex: 1 }}>
                                      <Typography variant="body1" sx={{ fontWeight: 600, fontSize: "16px" }}>
                                        {agent.name}
                                      </Typography>
                                      <Typography variant="body2" color="text.secondary" sx={{ fontSize: "13px" }}>
                                        {agent.relation}
                                      </Typography>
                                      <Typography variant="caption" color="text.secondary" sx={{ fontSize: "12px" }}>
                                        {agent.phone}
                                      </Typography>
                                    </Box>
                                    <Person
                                      sx={{
                                        fontSize: 32,
                                        color: agent.type === "doctor" ? "#2196F3" : "#4CAF50",
                                      }}
                                    />
                                  </Box>
                                </CardContent>
                              </Card>
                            );
                          })}
                        </Box>

                        {(config.assignedAgents || []).length > 0 && (
                          <Alert severity="success" sx={{ mt: 2, fontSize: "14px" }}>
                            <Typography variant="body2">
                              <strong>{(config.assignedAgents || []).length}</strong> {(config.assignedAgents || []).length === 1 ? "opiekun" : "opiekunów"} otrzyma powiadomienia o alertach dla {measurement.label.toLowerCase()}
                            </Typography>
                          </Alert>
                        )}
                      </>
                    )}
                  </>
                )}
              </AccordionDetails>
            </Accordion>
          );
        })}

        {/* Przycisk zapisz */}
        <Button
          fullWidth
          variant="contained"
          size="large"
          startIcon={<Save />}
          onClick={handleSave}
          sx={{
            mt: 3,
            py: 2,
            fontSize: "1.2rem",
            fontWeight: 500,
            backgroundColor: "#4CAF50",
            "&:hover": {
              backgroundColor: "#45a049",
            },
          }}
        >
          Zapisz ustawienia
        </Button>
      </Container>
    </Box>
  );
}