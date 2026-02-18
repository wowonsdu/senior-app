import { useState, useEffect } from "react";
import { useNavigate } from "react-router";
import {
  Box,
  Container,
  Typography,
  IconButton,
  Card,
  CardContent,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Dialog,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Switch,
  FormControlLabel,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  Divider,
  Chip,
} from "@mui/material";
import { ArrowBack, Medication, Add, Delete, Alarm, HealthAndSafety, Person, Edit } from "@mui/icons-material";

interface Medicine {
  id: string;
  name: string;
  dosage: string;
  times: string[];
  timesPerDay: number;
  notificationEnabled: boolean;
  createdAt: string;
}

interface Disease {
  id: string;
  name: string;
  since: string;
  severity: "łagodny" | "średni" | "ciężki";
  createdAt: string;
}

interface PersonalInfo {
  firstName: string;
  lastName: string;
  pesel: string;
  address: string;
  phone: string;
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

export function Settings() {
  const navigate = useNavigate();
  
  // Medicines state
  const [medicines, setMedicines] = useState<Medicine[]>([]);
  const [showMedicineDialog, setShowMedicineDialog] = useState(false);
  const [editMedicineMode, setEditMedicineMode] = useState(false);
  const [editingMedicineId, setEditingMedicineId] = useState<string | null>(null);
  const [medicineName, setMedicineName] = useState("");
  const [medicineDosage, setMedicineDosage] = useState("");
  const [medicineTimes, setMedicineTimes] = useState<string[]>([]);
  const [timesPerDay, setTimesPerDay] = useState(1);
  const [notificationEnabled, setNotificationEnabled] = useState(true);

  // Diseases state
  const [diseases, setDiseases] = useState<Disease[]>([]);
  const [showDiseaseDialog, setShowDiseaseDialog] = useState(false);
  const [editDiseaseMode, setEditDiseaseMode] = useState(false);
  const [editingDiseaseId, setEditingDiseaseId] = useState<string | null>(null);
  const [diseaseName, setDiseaseName] = useState("");
  const [diseaseSince, setDiseaseSince] = useState("");
  const [diseaseSeverity, setDiseaseSeverity] = useState<"łagodny" | "średni" | "ciężki">("średni");

  // Personal info state
  const [personalInfo, setPersonalInfo] = useState<PersonalInfo>({
    firstName: "",
    lastName: "",
    pesel: "",
    address: "",
    phone: "",
  });
  const [showPersonalInfoDialog, setShowPersonalInfoDialog] = useState(false);
  const [tempPersonalInfo, setTempPersonalInfo] = useState<PersonalInfo>(personalInfo);

  // Agents state
  const [agents, setAgents] = useState<Agent[]>([]);

  useEffect(() => {
    // Load medicines
    const existingMedicines = localStorage.getItem("healthMedicines");
    if (existingMedicines) {
      const parsed = JSON.parse(existingMedicines);
      const migrated = parsed.map((medicine: any) => {
        if (medicine.time && !medicine.times) {
          return {
            ...medicine,
            times: [medicine.time],
            timesPerDay: 1,
          };
        }
        return medicine;
      });
      setMedicines(migrated);
    }

    // Load diseases
    const existingDiseases = localStorage.getItem("healthDiseases");
    if (existingDiseases) {
      setDiseases(JSON.parse(existingDiseases));
    }

    // Load personal info
    const existingPersonalInfo = localStorage.getItem("healthPersonalInfo");
    if (existingPersonalInfo) {
      setPersonalInfo(JSON.parse(existingPersonalInfo));
    }

    // Load agents
    const existingAgents = localStorage.getItem("healthAgents");
    if (existingAgents) {
      setAgents(JSON.parse(existingAgents));
    }
  }, []);

  // Medicine handlers
  const handleAddMedicine = () => {
    setEditMedicineMode(false);
    setEditingMedicineId(null);
    setMedicineName("");
    setMedicineDosage("");
    setMedicineTimes([]);
    setTimesPerDay(1);
    setNotificationEnabled(true);
    setShowMedicineDialog(true);
  };

  const handleEditMedicine = (medicine: Medicine) => {
    setEditMedicineMode(true);
    setEditingMedicineId(medicine.id);
    setMedicineName(medicine.name);
    setMedicineDosage(medicine.dosage);
    setMedicineTimes(medicine.times);
    setTimesPerDay(medicine.timesPerDay);
    setNotificationEnabled(medicine.notificationEnabled);
    setShowMedicineDialog(true);
  };

  const handleConfirmMedicine = () => {
    if (!medicineName.trim() || !medicineDosage.trim() || medicineTimes.length === 0) return;

    if (editMedicineMode && editingMedicineId) {
      const updatedMedicines = medicines.map(medicine =>
        medicine.id === editingMedicineId
          ? {
              ...medicine,
              name: medicineName.trim(),
              dosage: medicineDosage.trim(),
              times: medicineTimes,
              timesPerDay,
              notificationEnabled,
            }
          : medicine
      );
      setMedicines(updatedMedicines);
      localStorage.setItem("healthMedicines", JSON.stringify(updatedMedicines));
    } else {
      const newMedicine: Medicine = {
        id: Date.now().toString(),
        name: medicineName.trim(),
        dosage: medicineDosage.trim(),
        times: medicineTimes,
        timesPerDay,
        notificationEnabled,
        createdAt: new Date().toISOString(),
      };

      const updatedMedicines = [...medicines, newMedicine];
      setMedicines(updatedMedicines);
      localStorage.setItem("healthMedicines", JSON.stringify(updatedMedicines));
    }

    setShowMedicineDialog(false);
    setEditMedicineMode(false);
    setEditingMedicineId(null);
    setMedicineName("");
    setMedicineDosage("");
    setMedicineTimes([]);
    setTimesPerDay(1);
    setNotificationEnabled(true);
  };

  const handleDeleteMedicine = () => {
    if (!editingMedicineId) return;

    const updatedMedicines = medicines.filter(medicine => medicine.id !== editingMedicineId);
    setMedicines(updatedMedicines);
    localStorage.setItem("healthMedicines", JSON.stringify(updatedMedicines));

    setShowMedicineDialog(false);
    setEditMedicineMode(false);
    setEditingMedicineId(null);
    setMedicineName("");
    setMedicineDosage("");
    setMedicineTimes([]);
    setTimesPerDay(1);
  };

  // Disease handlers
  const handleAddDisease = () => {
    setEditDiseaseMode(false);
    setEditingDiseaseId(null);
    setDiseaseName("");
    setDiseaseSince("");
    setDiseaseSeverity("średni");
    setShowDiseaseDialog(true);
  };

  const handleEditDisease = (disease: Disease) => {
    setEditDiseaseMode(true);
    setEditingDiseaseId(disease.id);
    setDiseaseName(disease.name);
    setDiseaseSince(disease.since);
    setDiseaseSeverity(disease.severity);
    setShowDiseaseDialog(true);
  };

  const handleConfirmDisease = () => {
    if (!diseaseName.trim() || !diseaseSince.trim()) return;

    if (editDiseaseMode && editingDiseaseId) {
      const updatedDiseases = diseases.map(disease =>
        disease.id === editingDiseaseId
          ? {
              ...disease,
              name: diseaseName.trim(),
              since: diseaseSince.trim(),
              severity: diseaseSeverity,
            }
          : disease
      );
      setDiseases(updatedDiseases);
      localStorage.setItem("healthDiseases", JSON.stringify(updatedDiseases));
    } else {
      const newDisease: Disease = {
        id: Date.now().toString(),
        name: diseaseName.trim(),
        since: diseaseSince.trim(),
        severity: diseaseSeverity,
        createdAt: new Date().toISOString(),
      };

      const updatedDiseases = [...diseases, newDisease];
      setDiseases(updatedDiseases);
      localStorage.setItem("healthDiseases", JSON.stringify(updatedDiseases));
    }

    setShowDiseaseDialog(false);
    setEditDiseaseMode(false);
    setEditingDiseaseId(null);
    setDiseaseName("");
    setDiseaseSince("");
    setDiseaseSeverity("średni");
  };

  const handleDeleteDisease = () => {
    if (!editingDiseaseId) return;

    const updatedDiseases = diseases.filter(disease => disease.id !== editingDiseaseId);
    setDiseases(updatedDiseases);
    localStorage.setItem("healthDiseases", JSON.stringify(updatedDiseases));

    setShowDiseaseDialog(false);
    setEditDiseaseMode(false);
    setEditingDiseaseId(null);
    setDiseaseName("");
    setDiseaseSince("");
  };

  // Personal info handlers
  const handleEditPersonalInfo = () => {
    setTempPersonalInfo(personalInfo);
    setShowPersonalInfoDialog(true);
  };

  const handleConfirmPersonalInfo = () => {
    setPersonalInfo(tempPersonalInfo);
    localStorage.setItem("healthPersonalInfo", JSON.stringify(tempPersonalInfo));
    setShowPersonalInfoDialog(false);
  };

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case "łagodny":
        return "#4CAF50";
      case "średni":
        return "#FF9800";
      case "ciężki":
        return "#F44336";
      default:
        return "#9E9E9E";
    }
  };

  return (
    <Box sx={{ backgroundColor: "#f5f5f5", minHeight: "100vh", pb: 10 }}>
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
          Moje Ustawienia
        </Typography>
      </Box>

      <Container maxWidth="md" sx={{ mt: 2, px: 2 }}>
        {/* O MNIE SECTION */}
        <Typography variant="h5" sx={{ mb: 3, fontWeight: 500, color: "#1976d2" }}>
          O Mnie
        </Typography>

        {!personalInfo.firstName ? (
          <Card sx={{ boxShadow: "0 2px 4px rgba(0,0,0,0.2)", mb: 3 }}>
            <CardContent sx={{ py: 6 }}>
              <Typography variant="h6" align="center" color="text.secondary" sx={{ mb: 2 }}>
                Brak danych osobowych
              </Typography>
              <Typography variant="body1" align="center" color="text.secondary">
                Kliknij przycisk poniżej, aby dodać swoje dane
              </Typography>
            </CardContent>
          </Card>
        ) : (
          <Card sx={{ boxShadow: "0 2px 4px rgba(0,0,0,0.2)", mb: 3 }}>
            <CardContent>
              <Box sx={{ cursor: "pointer" }} onClick={handleEditPersonalInfo}>
                <Box sx={{ display: "flex", alignItems: "center", gap: 2, mb: 2 }}>
                  <Person sx={{ fontSize: 48, color: "#1976d2" }} />
                  <Box>
                    <Typography variant="h6" sx={{ fontWeight: 500 }}>
                      {personalInfo.firstName} {personalInfo.lastName}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      PESEL: {personalInfo.pesel}
                    </Typography>
                  </Box>
                </Box>
                <Divider sx={{ my: 2 }} />
                <Typography variant="body1" sx={{ mb: 1 }}>
                  <strong>Adres:</strong> {personalInfo.address}
                </Typography>
                <Typography variant="body1">
                  <strong>Telefon:</strong> {personalInfo.phone}
                </Typography>
              </Box>
            </CardContent>
          </Card>
        )}

        {/* AGENCI SECTION */}
        {agents.length > 0 && (
          <>
            <Typography variant="h5" sx={{ mb: 3, fontWeight: 500, color: "#1976d2" }}>
              Moi Opiekunowie
            </Typography>

            <Card sx={{ boxShadow: "0 2px 4px rgba(0,0,0,0.2)", mb: 3 }}>
              <CardContent>
                <List sx={{ p: 0 }}>
                  {agents.map((agent, index) => (
                    <ListItem
                      key={agent.id}
                      sx={{
                        backgroundColor: index % 2 === 0 ? "#fafafa" : "white",
                        borderRadius: "8px",
                        mb: 1,
                        border: "1px solid #e0e0e0",
                        py: 2,
                      }}
                    >
                      <ListItemIcon>
                        <Person sx={{ fontSize: 48, color: agent.type === "doctor" ? "#2196F3" : "#4CAF50" }} />
                      </ListItemIcon>
                      <ListItemText
                        primary={agent.name}
                        secondary={
                          <>
                            <Typography component="span" variant="body1" sx={{ display: "block", fontWeight: 500 }}>
                              {agent.relation}
                            </Typography>
                            <Typography component="span" variant="body2" sx={{ display: "block" }}>
                              Tel: {agent.phone}
                            </Typography>
                            {agent.type === "doctor" && agent.specialization && (
                              <Typography component="span" variant="body2" sx={{ display: "block", fontStyle: "italic" }}>
                                {agent.specialization}
                              </Typography>
                            )}
                          </>
                        }
                        primaryTypographyProps={{
                          variant: "h6",
                          sx: { fontWeight: 500 },
                        }}
                        secondaryTypographyProps={{
                          component: "div",
                        }}
                      />
                    </ListItem>
                  ))}
                </List>
              </CardContent>
            </Card>
          </>
        )}

        {/* CHOROBY SECTION */}
        <Typography variant="h5" sx={{ mb: 3, fontWeight: 500, color: "#1976d2" }}>
          Moje Choroby
        </Typography>

        {diseases.length === 0 ? (
          <Card sx={{ boxShadow: "0 2px 4px rgba(0,0,0,0.2)", mb: 3 }}>
            <CardContent sx={{ py: 6 }}>
              <Typography variant="h6" align="center" color="text.secondary" sx={{ mb: 2 }}>
                Brak zarejestrowanych chorób
              </Typography>
              <Typography variant="body1" align="center" color="text.secondary">
                Kliknij przycisk poniżej, aby dodać informację o chorobie
              </Typography>
            </CardContent>
          </Card>
        ) : (
          <Card sx={{ boxShadow: "0 2px 4px rgba(0,0,0,0.2)", mb: 3 }}>
            <CardContent>
              <List sx={{ p: 0 }}>
                {diseases.map((disease, index) => (
                  <ListItem
                    key={disease.id}
                    sx={{
                      backgroundColor: index % 2 === 0 ? "#fafafa" : "white",
                      borderRadius: "8px",
                      mb: 1,
                      border: "1px solid #e0e0e0",
                      cursor: "pointer",
                      py: 2,
                    }}
                    onClick={() => handleEditDisease(disease)}
                  >
                    <ListItemIcon>
                      <HealthAndSafety sx={{ fontSize: 48, color: getSeverityColor(disease.severity) }} />
                    </ListItemIcon>
                    <ListItemText
                      primary={disease.name}
                      secondary={
                        <>
                          <Typography component="span" variant="body1" sx={{ display: "block", fontWeight: 500 }}>
                            Od: {disease.since}
                          </Typography>
                          <Chip
                            label={disease.severity.toUpperCase()}
                            size="small"
                            sx={{
                              mt: 1,
                              backgroundColor: getSeverityColor(disease.severity),
                              color: "white",
                              fontWeight: 600,
                            }}
                          />
                        </>
                      }
                      primaryTypographyProps={{
                        variant: "h6",
                        sx: { fontWeight: 500 },
                      }}
                      secondaryTypographyProps={{
                        component: "div",
                      }}
                    />
                  </ListItem>
                ))}
              </List>
            </CardContent>
          </Card>
        )}

        {/* LEKI SECTION */}
        <Typography variant="h5" sx={{ mb: 3, fontWeight: 500, color: "#1976d2" }}>
          Moje Leki
        </Typography>

        {medicines.length === 0 ? (
          <Card sx={{ boxShadow: "0 2px 4px rgba(0,0,0,0.2)", mb: 3 }}>
            <CardContent sx={{ py: 6 }}>
              <Typography variant="h6" align="center" color="text.secondary" sx={{ mb: 2 }}>
                Brak dodanych leków
              </Typography>
              <Typography variant="body1" align="center" color="text.secondary">
                Kliknij przycisk poniżej, aby dodać pierwszy lek
              </Typography>
            </CardContent>
          </Card>
        ) : (
          <Card sx={{ boxShadow: "0 2px 4px rgba(0,0,0,0.2)", mb: 3 }}>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 2, fontWeight: 500 }}>
                Lista Leków ({medicines.length})
              </Typography>
              <List sx={{ p: 0 }}>
                {medicines.map((medicine, index) => (
                  <ListItem
                    key={medicine.id}
                    onClick={() => handleEditMedicine(medicine)}
                    sx={{
                      backgroundColor: index % 2 === 0 ? "#fafafa" : "white",
                      borderRadius: "8px",
                      mb: 1,
                      border: "1px solid #e0e0e0",
                      py: 2,
                      cursor: "pointer",
                      "&:hover": {
                        backgroundColor: "#e8f4f8",
                        border: "1px solid #1976d2",
                      },
                    }}
                  >
                    <ListItemIcon>
                      <Medication sx={{ fontSize: 48, color: "#E91E63" }} />
                    </ListItemIcon>
                    <ListItemText
                      primary={medicine.name}
                      secondary={
                        <>
                          <Typography component="span" variant="body1" sx={{ display: "block", fontWeight: 500 }}>
                            Dawka: {medicine.dosage}
                          </Typography>
                          <Typography component="span" variant="body1" sx={{ display: "block" }}>
                            <Alarm sx={{ fontSize: 16, verticalAlign: "middle", mr: 0.5 }} />
                            Godziny: {medicine.times.join(", ")}
                          </Typography>
                          <Typography 
                            component="span" 
                            variant="body2" 
                            sx={{ 
                              display: "block", 
                              mt: 0.5,
                              color: medicine.notificationEnabled ? "#4CAF50" : "#9E9E9E",
                              fontWeight: 500,
                            }}
                          >
                            {medicine.notificationEnabled ? "✓ Powiadomienia włączone" : "✗ Powiadomienia wyłączone"}
                          </Typography>
                        </>
                      }
                      primaryTypographyProps={{
                        variant: "h6",
                        sx: { fontWeight: 500 },
                      }}
                      secondaryTypographyProps={{
                        component: "div",
                      }}
                    />
                  </ListItem>
                ))}
              </List>
            </CardContent>
          </Card>
        )}

        {/* KONFIGURACJA SECTION */}
        <Typography variant="h5" sx={{ mb: 3, fontWeight: 500, color: "#1976d2" }}>
          Konfiguracja
        </Typography>

        <Card
          sx={{
            boxShadow: "0 2px 4px rgba(0,0,0,0.2)",
            mb: 3,
            cursor: "pointer",
            transition: "all 0.2s",
            "&:hover": {
              transform: "translateY(-2px)",
              boxShadow: "0 4px 12px rgba(0,0,0,0.25)",
            },
            "&:active": {
              transform: "scale(0.98)",
            },
          }}
          onClick={() => navigate("/home/ustawienia/alerty")}
        >
          <CardContent sx={{ p: 3 }}>
            <Box sx={{ display: "flex", alignItems: "center", gap: 3 }}>
              <Box
                sx={{
                  backgroundColor: "#FF9800",
                  borderRadius: "12px",
                  p: 2,
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                }}
              >
                <Alarm sx={{ fontSize: 48, color: "white" }} />
              </Box>
              <Box sx={{ flex: 1 }}>
                <Typography variant="h6" sx={{ fontWeight: 600, mb: 0.5 }}>
                  Alerty i Powiadomienia
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Ustaw wartości krytyczne dla pomiarów zdrowotnych
                </Typography>
              </Box>
              <ArrowBack sx={{ fontSize: 32, color: "#1976d2", transform: "rotate(180deg)" }} />
            </Box>
          </CardContent>
        </Card>

        {/* PRZYCISKI NA DOLE */}
        <Box
          sx={{
            display: "grid",
            gridTemplateColumns: { xs: "1fr", sm: "1fr 1fr" },
            gap: 2,
            mb: 2,
          }}
        >
          <Card
            sx={{
              cursor: "pointer",
              transition: "all 0.2s",
              "&:active": {
                transform: "scale(0.98)",
              },
              boxShadow: "0 2px 4px rgba(0,0,0,0.2)",
            }}
            onClick={handleEditPersonalInfo}
          >
            <CardContent
              sx={{
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                gap: 2,
                py: 4,
                backgroundColor: "#1976d2",
                color: "white",
                "&:last-child": { pb: 4 },
              }}
            >
              <Edit sx={{ fontSize: 72 }} />
              <Typography variant="h5" component="div" sx={{ fontWeight: 500 }}>
                {personalInfo.firstName ? "Edytuj Dane" : "Dodaj Dane"}
              </Typography>
            </CardContent>
          </Card>

          <Card
            sx={{
              cursor: "pointer",
              transition: "all 0.2s",
              "&:active": {
                transform: "scale(0.98)",
              },
              boxShadow: "0 2px 4px rgba(0,0,0,0.2)",
            }}
            onClick={handleAddDisease}
          >
            <CardContent
              sx={{
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                gap: 2,
                py: 4,
                backgroundColor: "#F44336",
                color: "white",
                "&:last-child": { pb: 4 },
              }}
            >
              <Add sx={{ fontSize: 72 }} />
              <Typography variant="h5" component="div" sx={{ fontWeight: 500 }}>
                Dodaj Chorobę
              </Typography>
            </CardContent>
          </Card>
        </Box>

        <Card
          sx={{
            cursor: "pointer",
            transition: "all 0.2s",
            "&:active": {
              transform: "scale(0.98)",
            },
            boxShadow: "0 2px 4px rgba(0,0,0,0.2)",
          }}
          onClick={handleAddMedicine}
        >
          <CardContent
            sx={{
              display: "flex",
              flexDirection: "column",
              alignItems: "center",
              gap: 2,
              py: 4,
              backgroundColor: "#E91E63",
              color: "white",
              "&:last-child": { pb: 4 },
            }}
          >
            <Add sx={{ fontSize: 72 }} />
            <Typography variant="h5" component="div" sx={{ fontWeight: 500 }}>
              Dodaj Lek
            </Typography>
          </CardContent>
        </Card>
      </Container>

      {/* Dialog dodawania/edycji leku */}
      <Dialog
        open={showMedicineDialog}
        onClose={() => setShowMedicineDialog(false)}
        maxWidth="sm"
        fullWidth
        PaperProps={{
          sx: {
            borderRadius: "12px",
          },
        }}
      >
        <DialogContent sx={{ pt: 4 }}>
          <Typography variant="h5" sx={{ mb: 3, fontWeight: 500 }}>
            {editMedicineMode ? "Edytuj Lek" : "Dodaj Lek"}
          </Typography>
          <Typography variant="body1" sx={{ mb: 3, color: "text.secondary" }}>
            {editMedicineMode ? "Zmień dane leku" : "Podaj nazwę leku, dawkę i godziny przyjmowania"}
          </Typography>
          <TextField
            autoFocus
            fullWidth
            label="Nazwa leku"
            variant="outlined"
            value={medicineName}
            onChange={(e) => setMedicineName(e.target.value)}
            placeholder="np. Acard, Concor"
            sx={{
              mb: 2,
              "& .MuiOutlinedInput-root": {
                fontSize: "18px",
              },
              "& .MuiInputLabel-root": {
                fontSize: "18px",
              },
            }}
          />
          <TextField
            fullWidth
            label="Dawka"
            variant="outlined"
            value={medicineDosage}
            onChange={(e) => setMedicineDosage(e.target.value)}
            placeholder="np. 75mg, 1 tabletka"
            sx={{
              mb: 2,
              "& .MuiOutlinedInput-root": {
                fontSize: "18px",
              },
              "& .MuiInputLabel-root": {
                fontSize: "18px",
              },
            }}
          />
          <FormControl fullWidth sx={{ mb: 3 }}>
            <InputLabel id="times-per-day-label">Ilość godzin przyjmowania dziennie</InputLabel>
            <Select
              labelId="times-per-day-label"
              value={timesPerDay}
              onChange={(e) => setTimesPerDay(Number(e.target.value))}
              label="Ilość godzin przyjmowania dziennie"
              sx={{
                "& .MuiSelect-select": {
                  fontSize: "18px",
                },
              }}
            >
              {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map(num => (
                <MenuItem key={num} value={num}>{num}</MenuItem>
              ))}
            </Select>
          </FormControl>
          {Array.from({ length: timesPerDay }, (_, index) => (
            <TextField
              key={index}
              fullWidth
              label={`Godzina przyjęcia ${index + 1}`}
              variant="outlined"
              type="time"
              value={medicineTimes[index] || ""}
              onChange={(e) => {
                const newTimes = [...medicineTimes];
                newTimes[index] = e.target.value;
                setMedicineTimes(newTimes);
              }}
              InputLabelProps={{
                shrink: true,
              }}
              sx={{
                mb: 2,
                "& .MuiOutlinedInput-root": {
                  fontSize: "18px",
                },
                "& .MuiInputLabel-root": {
                  fontSize: "18px",
                },
              }}
            />
          ))}
          <FormControlLabel
            control={
              <Switch
                checked={notificationEnabled}
                onChange={(e) => setNotificationEnabled(e.target.checked)}
                sx={{
                  "& .MuiSwitch-switchBase.Mui-checked": {
                    color: "#4CAF50",
                  },
                  "& .MuiSwitch-switchBase.Mui-checked + .MuiSwitch-track": {
                    backgroundColor: "#4CAF50",
                  },
                }}
              />
            }
            label={
              <Typography sx={{ fontSize: "18px" }}>
                Powiadamiaj o godzinie przyjęcia
              </Typography>
            }
            sx={{ mb: 2 }}
          />
        </DialogContent>
        <DialogActions sx={{ p: 3, pt: 2, flexDirection: "column", gap: 2 }}>
          <Box sx={{ display: "flex", width: "100%", gap: 2 }}>
            <Button
              onClick={() => setShowMedicineDialog(false)}
              sx={{
                fontSize: "16px",
                px: 3,
                py: 1.5,
                flex: 1,
              }}
            >
              Anuluj
            </Button>
            <Button
              onClick={handleConfirmMedicine}
              variant="contained"
              disabled={!medicineName.trim() || !medicineDosage.trim() || medicineTimes.length === 0}
              sx={{
                fontSize: "16px",
                px: 3,
                py: 1.5,
                flex: 1,
                backgroundColor: "#E91E63",
                "&:hover": {
                  backgroundColor: "#C2185B",
                },
              }}
            >
              {editMedicineMode ? "Zapisz" : "Dodaj"}
            </Button>
          </Box>
          {editMedicineMode && (
            <Button
              variant="text"
              startIcon={<Delete sx={{ fontSize: 18 }} />}
              onClick={handleDeleteMedicine}
              sx={{
                fontSize: "14px",
                py: 1,
                color: "#F44336",
                "&:hover": {
                  backgroundColor: "rgba(244, 67, 54, 0.08)",
                },
              }}
            >
              Usuń lek
            </Button>
          )}
        </DialogActions>
      </Dialog>

      {/* Dialog dodawania/edycji choroby */}
      <Dialog
        open={showDiseaseDialog}
        onClose={() => setShowDiseaseDialog(false)}
        maxWidth="sm"
        fullWidth
        PaperProps={{
          sx: {
            borderRadius: "12px",
          },
        }}
      >
        <DialogContent sx={{ pt: 4 }}>
          <Typography variant="h5" sx={{ mb: 3, fontWeight: 500 }}>
            {editDiseaseMode ? "Edytuj Chorobę" : "Dodaj Chorobę"}
          </Typography>
          <Typography variant="body1" sx={{ mb: 3, color: "text.secondary" }}>
            {editDiseaseMode ? "Zmień dane choroby" : "Podaj nazwę choroby, od kiedy chorujesz i przebieg"}
          </Typography>
          <TextField
            autoFocus
            fullWidth
            label="Nazwa choroby"
            variant="outlined"
            value={diseaseName}
            onChange={(e) => setDiseaseName(e.target.value)}
            placeholder="np. Cukrzyca, Nadciśnienie"
            sx={{
              mb: 2,
              "& .MuiOutlinedInput-root": {
                fontSize: "18px",
              },
              "& .MuiInputLabel-root": {
                fontSize: "18px",
              },
            }}
          />
          <TextField
            fullWidth
            label="Od kiedy chorujesz"
            variant="outlined"
            value={diseaseSince}
            onChange={(e) => setDiseaseSince(e.target.value)}
            placeholder="np. 2020, od 5 lat"
            sx={{
              mb: 2,
              "& .MuiOutlinedInput-root": {
                fontSize: "18px",
              },
              "& .MuiInputLabel-root": {
                fontSize: "18px",
              },
            }}
          />
          <FormControl fullWidth sx={{ mb: 3 }}>
            <InputLabel id="severity-label">Przebieg choroby</InputLabel>
            <Select
              labelId="severity-label"
              value={diseaseSeverity}
              onChange={(e) => setDiseaseSeverity(e.target.value as "łagodny" | "średni" | "ciężki")}
              label="Przebieg choroby"
              sx={{
                "& .MuiSelect-select": {
                  fontSize: "18px",
                },
              }}
            >
              <MenuItem value="łagodny">Łagodny</MenuItem>
              <MenuItem value="średni">Średni</MenuItem>
              <MenuItem value="ciężki">Ciężki</MenuItem>
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions sx={{ p: 3, pt: 2, flexDirection: "column", gap: 2 }}>
          <Box sx={{ display: "flex", width: "100%", gap: 2 }}>
            <Button
              onClick={() => setShowDiseaseDialog(false)}
              sx={{
                fontSize: "16px",
                px: 3,
                py: 1.5,
                flex: 1,
              }}
            >
              Anuluj
            </Button>
            <Button
              onClick={handleConfirmDisease}
              variant="contained"
              disabled={!diseaseName.trim() || !diseaseSince.trim()}
              sx={{
                fontSize: "16px",
                px: 3,
                py: 1.5,
                flex: 1,
                backgroundColor: "#F44336",
                "&:hover": {
                  backgroundColor: "#D32F2F",
                },
              }}
            >
              {editDiseaseMode ? "Zapisz" : "Dodaj"}
            </Button>
          </Box>
          {editDiseaseMode && (
            <Button
              variant="text"
              startIcon={<Delete sx={{ fontSize: 18 }} />}
              onClick={handleDeleteDisease}
              sx={{
                fontSize: "14px",
                py: 1,
                color: "#F44336",
                "&:hover": {
                  backgroundColor: "rgba(244, 67, 54, 0.08)",
                },
              }}
            >
              Usuń chorobę
            </Button>
          )}
        </DialogActions>
      </Dialog>

      {/* Dialog danych osobowych */}
      <Dialog
        open={showPersonalInfoDialog}
        onClose={() => setShowPersonalInfoDialog(false)}
        maxWidth="sm"
        fullWidth
        PaperProps={{
          sx: {
            borderRadius: "12px",
          },
        }}
      >
        <DialogContent sx={{ pt: 4 }}>
          <Typography variant="h5" sx={{ mb: 3, fontWeight: 500 }}>
            Moje Dane Osobowe
          </Typography>
          <Typography variant="body1" sx={{ mb: 3, color: "text.secondary" }}>
            Podaj swoje dane osobowe
          </Typography>
          <TextField
            autoFocus
            fullWidth
            label="Imię"
            variant="outlined"
            value={tempPersonalInfo.firstName}
            onChange={(e) => setTempPersonalInfo({ ...tempPersonalInfo, firstName: e.target.value })}
            placeholder="np. Jan"
            sx={{
              mb: 2,
              "& .MuiOutlinedInput-root": {
                fontSize: "18px",
              },
              "& .MuiInputLabel-root": {
                fontSize: "18px",
              },
            }}
          />
          <TextField
            fullWidth
            label="Nazwisko"
            variant="outlined"
            value={tempPersonalInfo.lastName}
            onChange={(e) => setTempPersonalInfo({ ...tempPersonalInfo, lastName: e.target.value })}
            placeholder="np. Kowalski"
            sx={{
              mb: 2,
              "& .MuiOutlinedInput-root": {
                fontSize: "18px",
              },
              "& .MuiInputLabel-root": {
                fontSize: "18px",
              },
            }}
          />
          <TextField
            fullWidth
            label="PESEL"
            variant="outlined"
            value={tempPersonalInfo.pesel}
            onChange={(e) => setTempPersonalInfo({ ...tempPersonalInfo, pesel: e.target.value })}
            placeholder="np. 12345678901"
            sx={{
              mb: 2,
              "& .MuiOutlinedInput-root": {
                fontSize: "18px",
              },
              "& .MuiInputLabel-root": {
                fontSize: "18px",
              },
            }}
          />
          <TextField
            fullWidth
            label="Adres zamieszkania"
            variant="outlined"
            value={tempPersonalInfo.address}
            onChange={(e) => setTempPersonalInfo({ ...tempPersonalInfo, address: e.target.value })}
            placeholder="np. ul. Kwiatowa 12, 00-001 Warszawa"
            sx={{
              mb: 2,
              "& .MuiOutlinedInput-root": {
                fontSize: "18px",
              },
              "& .MuiInputLabel-root": {
                fontSize: "18px",
              },
            }}
          />
          <TextField
            fullWidth
            label="Telefon"
            variant="outlined"
            value={tempPersonalInfo.phone}
            onChange={(e) => setTempPersonalInfo({ ...tempPersonalInfo, phone: e.target.value })}
            placeholder="np. 123 456 789"
            sx={{
              mb: 2,
              "& .MuiOutlinedInput-root": {
                fontSize: "18px",
              },
              "& .MuiInputLabel-root": {
                fontSize: "18px",
              },
            }}
          />
        </DialogContent>
        <DialogActions sx={{ p: 3, pt: 2 }}>
          <Button
            onClick={() => setShowPersonalInfoDialog(false)}
            sx={{
              fontSize: "16px",
              px: 3,
              py: 1.5,
            }}
          >
            Anuluj
          </Button>
          <Button
            onClick={handleConfirmPersonalInfo}
            variant="contained"
            sx={{
              fontSize: "16px",
              px: 3,
              py: 1.5,
              backgroundColor: "#1976d2",
              "&:hover": {
                backgroundColor: "#1565c0",
              },
            }}
          >
            Zapisz
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}