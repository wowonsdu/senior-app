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
  Fab,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
} from "@mui/material";
import { 
  Add, 
  Logout, 
  PersonAdd, 
  Close,
  AccountCircle,
} from "@mui/icons-material";

interface Patient {
  id: string;
  name: string;
  phone?: string;
  addedDate: string;
  accessCode?: string;
}

export function PatientSelection() {
  const navigate = useNavigate();
  const [patients, setPatients] = useState<Patient[]>([]);
  const [showAddDialog, setShowAddDialog] = useState(false);
  const [showCodeDialog, setShowCodeDialog] = useState(false);
  const [newPatientName, setNewPatientName] = useState("");
  const [newPatientPhone, setNewPatientPhone] = useState("");
  const [generatedCode, setGeneratedCode] = useState("");
  const [generatedPatientName, setGeneratedPatientName] = useState("");
  const userRole = localStorage.getItem("healthAppUserRole") || "";

  // Załaduj listę pacjentów
  useEffect(() => {
    const savedPatients = localStorage.getItem("healthPatients");
    if (savedPatients) {
      setPatients(JSON.parse(savedPatients));
    }
  }, []);

  // Jeśli nie jesteś opiekunem, przekieruj do home
  useEffect(() => {
    if (userRole !== "agent") {
      navigate("/home", { replace: true });
    }
  }, [userRole, navigate]);

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
    // Zapisz wybranego pacjenta
    localStorage.setItem("healthAppSelectedPatient", JSON.stringify(patient));
    
    // Przekieruj do głównego ekranu
    navigate("/home");
  };

  const handleAddPatient = () => {
    if (!newPatientName.trim()) {
      return;
    }

    // Generuj 6-cyfrowy kod dostępu
    const accessCode = Math.floor(100000 + Math.random() * 900000).toString();

    const newPatient: Patient = {
      id: Date.now().toString(),
      name: newPatientName.trim(),
      phone: newPatientPhone.trim() || undefined,
      addedDate: new Date().toISOString(),
      accessCode: accessCode,
    };

    const updatedPatients = [...patients, newPatient];
    setPatients(updatedPatients);
    localStorage.setItem("healthPatients", JSON.stringify(updatedPatients));

    // Jeśli podano numer telefonu, symuluj wysłanie SMS
    if (newPatientPhone.trim()) {
      console.log(`SMS wysłany na numer ${newPatientPhone} z kodem dostępu: ${accessCode}`);
      setGeneratedCode(accessCode);
      setGeneratedPatientName(newPatientName.trim());
      setShowCodeDialog(true);
    }

    // Resetuj formularz
    setNewPatientName("");
    setNewPatientPhone("");
    setShowAddDialog(false);
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
          Wybierz Podopiecznego
        </Typography>
        <Typography variant="body1" align="center" sx={{ mt: 1, opacity: 0.9 }}>
          Panel Opiekuna
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

      <Container maxWidth="md" sx={{ mt: 4, px: 2 }}>
        {patients.length === 0 ? (
          // Brak pacjentów - pokaż onboarding
          <Box sx={{ textAlign: "center", py: 6 }}>
            <AccountCircle sx={{ fontSize: 120, color: "#ccc", mb: 3 }} />
            <Typography variant="h5" sx={{ mb: 2, fontWeight: 500, color: "#666" }}>
              Brak podopiecznych
            </Typography>
            <Typography variant="body1" sx={{ mb: 4, color: "#999" }}>
              Dodaj osobę, którą chcesz monitorować
            </Typography>
            <Button
              variant="contained"
              size="large"
              startIcon={<PersonAdd />}
              onClick={() => setShowAddDialog(true)}
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
          // Lista pacjentów
          <Box>
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
          </Box>
        )}
      </Container>

      {/* FAB - dodaj nowego pacjenta */}
      {patients.length > 0 && (
        <Fab
          color="primary"
          aria-label="add"
          onClick={() => setShowAddDialog(true)}
          sx={{
            position: "fixed",
            bottom: 24,
            right: 24,
            width: 72,
            height: 72,
            backgroundColor: "#2196F3",
            "&:hover": {
              backgroundColor: "#1976d2",
            },
          }}
        >
          <Add sx={{ fontSize: 40 }} />
        </Fab>
      )}

      {/* Dialog dodawania pacjenta */}
      <Dialog
        open={showAddDialog}
        onClose={() => {
          setShowAddDialog(false);
          setNewPatientName("");
          setNewPatientPhone("");
        }}
        maxWidth="sm"
        fullWidth
        PaperProps={{
          sx: {
            borderRadius: "16px",
          },
        }}
      >
        <DialogTitle sx={{ pb: 1 }}>
          <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
            <Typography variant="h6" sx={{ fontWeight: 600 }}>
              Dodaj podopiecznego
            </Typography>
            <IconButton
              onClick={() => {
                setShowAddDialog(false);
                setNewPatientName("");
                setNewPatientPhone("");
              }}
            >
              <Close />
            </IconButton>
          </Box>
        </DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
          <TextField
            fullWidth
            label="Imię i nazwisko *"
            variant="outlined"
            value={newPatientName}
            onChange={(e) => setNewPatientName(e.target.value)}
            placeholder="np. Jan Kowalski"
            sx={{
              mb: 3,
              "& .MuiOutlinedInput-root": {
                fontSize: "18px",
              },
              "& .MuiInputLabel-root": {
                fontSize: "16px",
              },
            }}
            autoFocus
          />
          <TextField
            fullWidth
            label="Numer telefonu"
            variant="outlined"
            value={newPatientPhone}
            onChange={(e) => setNewPatientPhone(e.target.value)}
            placeholder="np. 123 456 789"
            type="tel"
            sx={{
              mb: 2,
              "& .MuiOutlinedInput-root": {
                fontSize: "18px",
              },
              "& .MuiInputLabel-root": {
                fontSize: "16px",
              },
            }}
          />
          <Box
            sx={{
              backgroundColor: "#e3f2fd",
              border: "1px solid #2196F3",
              borderRadius: "8px",
              p: 2,
              mb: 2,
            }}
          >
            <Typography variant="body2" sx={{ fontSize: "14px", color: "#1976d2", fontWeight: 500 }}>
              💡 Podaj numer telefonu
            </Typography>
            <Typography variant="body2" sx={{ fontSize: "13px", color: "#666", mt: 0.5 }}>
              Jeśli podasz numer telefonu, automatycznie wyślemy podopiecznemu SMS z 6-cyfrowym kodem dostępu, którym będzie mógł się zalogować do aplikacji.
            </Typography>
          </Box>
          <Typography variant="body2" color="text.secondary" sx={{ fontSize: "14px" }}>
            * Pole wymagane
          </Typography>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 3 }}>
          <Button
            onClick={() => {
              setShowAddDialog(false);
              setNewPatientName("");
              setNewPatientPhone("");
            }}
            sx={{ fontSize: "16px" }}
          >
            Anuluj
          </Button>
          <Button
            variant="contained"
            onClick={handleAddPatient}
            disabled={!newPatientName.trim()}
            sx={{
              fontSize: "16px",
              px: 3,
              backgroundColor: "#2196F3",
              "&:hover": {
                backgroundColor: "#1976d2",
              },
            }}
          >
            Dodaj
          </Button>
        </DialogActions>
      </Dialog>

      {/* Dialog kodu dostępu */}
      <Dialog
        open={showCodeDialog}
        onClose={() => {
          setShowCodeDialog(false);
          setGeneratedCode("");
          setGeneratedPatientName("");
        }}
        maxWidth="sm"
        fullWidth
        PaperProps={{
          sx: {
            borderRadius: "16px",
            backgroundColor: "#f5f5f5",
          },
        }}
      >
        <DialogTitle sx={{ pb: 1, pt: 3 }}>
          <Box sx={{ textAlign: "center" }}>
            <Box
              sx={{
                width: 80,
                height: 80,
                borderRadius: "50%",
                backgroundColor: "#4CAF50",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                margin: "0 auto 16px",
              }}
            >
              <Typography variant="h3" sx={{ color: "white" }}>
                ✓
              </Typography>
            </Box>
            <Typography variant="h5" sx={{ fontWeight: 600, mb: 1 }}>
              Podopieczny dodany!
            </Typography>
            <Typography variant="body2" color="text.secondary">
              SMS z kodem został wysłany
            </Typography>
          </Box>
        </DialogTitle>
        <DialogContent sx={{ pt: 3, pb: 2 }}>
          <Card
            sx={{
              backgroundColor: "white",
              boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
              mb: 2,
            }}
          >
            <CardContent sx={{ textAlign: "center", py: 3 }}>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2, fontSize: "15px" }}>
                Kod dostępu dla:
              </Typography>
              <Typography variant="h6" sx={{ fontWeight: 600, mb: 3, color: "#1976d2" }}>
                {generatedPatientName}
              </Typography>
              <Box
                sx={{
                  backgroundColor: "#e3f2fd",
                  border: "2px dashed #2196F3",
                  borderRadius: "12px",
                  py: 3,
                  px: 2,
                  mb: 2,
                }}
              >
                <Typography
                  variant="h3"
                  sx={{
                    fontWeight: 700,
                    color: "#1976d2",
                    letterSpacing: "8px",
                    fontFamily: "monospace",
                  }}
                >
                  {generatedCode}
                </Typography>
              </Box>
              <Typography variant="body2" sx={{ fontSize: "13px", color: "#666", lineHeight: 1.6 }}>
                📱 SMS z tym kodem został wysłany na podany numer telefonu
                <br />
                🔐 Podopieczny może użyć tego kodu do zalogowania się w aplikacji
              </Typography>
            </CardContent>
          </Card>
          
          <Box
            sx={{
              backgroundColor: "#fff3cd",
              border: "1px solid #ffc107",
              borderRadius: "8px",
              p: 2,
            }}
          >
            <Typography variant="body2" sx={{ fontSize: "13px", color: "#856404", fontWeight: 500 }}>
              💡 Wskazówka
            </Typography>
            <Typography variant="body2" sx={{ fontSize: "12px", color: "#856404", mt: 0.5 }}>
              Zapisz ten kod w bezpiecznym miejscu. Podopieczny będzie go potrzebował przy pierwszym logowaniu.
            </Typography>
          </Box>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 3 }}>
          <Button
            fullWidth
            variant="contained"
            size="large"
            onClick={() => {
              setShowCodeDialog(false);
              setGeneratedCode("");
              setGeneratedPatientName("");
            }}
            sx={{
              fontSize: "16px",
              py: 1.5,
              backgroundColor: "#2196F3",
              "&:hover": {
                backgroundColor: "#1976d2",
              },
            }}
          >
            Rozumiem
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}