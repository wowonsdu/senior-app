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
  Fab,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
} from "@mui/material";
import { ArrowBack, Person, Add, ContentCopy, Delete, Sms, PersonAdd, MedicalServices } from "@mui/icons-material";

interface Agent {
  id: string;
  name: string;
  phone: string;
  email: string;
  code: string;
  createdAt: string;
}

interface Doctor {
  id: string;
  name: string;
  specialization: string;
  phone: string;
  email: string;
  code: string;
  createdAt: string;
}

export function Agents() {
  const navigate = useNavigate();
  const [agents, setAgents] = useState<Agent[]>([]);
  const [doctors, setDoctors] = useState<Doctor[]>([]);
  const [showAddDialog, setShowAddDialog] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [addingType, setAddingType] = useState<"agent" | "doctor">("agent");
  const [newAgentName, setNewAgentName] = useState("");
  const [newAgentPhone, setNewAgentPhone] = useState("");
  const [newAgentEmail, setNewAgentEmail] = useState("");
  const [newSpecialization, setNewSpecialization] = useState("");
  const [newAgentCode, setNewAgentCode] = useState("");
  const [showCodeDialog, setShowCodeDialog] = useState(false);
  const [copiedCode, setCopiedCode] = useState(false);

  useEffect(() => {
    const existingAgents = localStorage.getItem("healthAgents");
    if (existingAgents) {
      setAgents(JSON.parse(existingAgents));
    }
    
    const existingDoctors = localStorage.getItem("healthDoctors");
    if (existingDoctors) {
      setDoctors(JSON.parse(existingDoctors));
    }
  }, []);

  const generateCode = () => {
    return Math.floor(100000 + Math.random() * 900000).toString();
  };

  const handleAddAgent = () => {
    setEditMode(false);
    setEditingId(null);
    setNewAgentName("");
    setNewAgentPhone("");
    setNewAgentEmail("");
    setNewSpecialization("");
    setNewAgentCode("");
    setAddingType("agent");
    setShowAddDialog(true);
  };

  const handleAddDoctor = () => {
    setEditMode(false);
    setEditingId(null);
    setNewAgentName("");
    setNewAgentPhone("");
    setNewAgentEmail("");
    setNewSpecialization("");
    setNewAgentCode("");
    setAddingType("doctor");
    setShowAddDialog(true);
  };

  const handleEditAgent = (agent: Agent) => {
    setEditMode(true);
    setEditingId(agent.id);
    setNewAgentName(agent.name);
    setNewAgentPhone(agent.phone);
    setNewAgentEmail(agent.email);
    setNewSpecialization("");
    setNewAgentCode(agent.code);
    setAddingType("agent");
    setShowAddDialog(true);
  };

  const handleEditDoctor = (doctor: Doctor) => {
    setEditMode(true);
    setEditingId(doctor.id);
    setNewAgentName(doctor.name);
    setNewAgentPhone(doctor.phone);
    setNewAgentEmail(doctor.email);
    setNewSpecialization(doctor.specialization);
    setNewAgentCode(doctor.code);
    setAddingType("doctor");
    setShowAddDialog(true);
  };

  const handleConfirmAdd = () => {
    if (!newAgentName.trim()) return;

    if (editMode && editingId) {
      // Tryb edycji
      if (addingType === "agent") {
        const updatedAgents = agents.map(agent =>
          agent.id === editingId
            ? {
                ...agent,
                name: newAgentName.trim(),
                phone: newAgentPhone.trim(),
                email: newAgentEmail.trim(),
              }
            : agent
        );
        setAgents(updatedAgents);
        localStorage.setItem("healthAgents", JSON.stringify(updatedAgents));
      } else {
        const updatedDoctors = doctors.map(doctor =>
          doctor.id === editingId
            ? {
                ...doctor,
                name: newAgentName.trim(),
                phone: newAgentPhone.trim(),
                email: newAgentEmail.trim(),
                specialization: newSpecialization.trim(),
              }
            : doctor
        );
        setDoctors(updatedDoctors);
        localStorage.setItem("healthDoctors", JSON.stringify(updatedDoctors));
      }
      setShowAddDialog(false);
      setEditMode(false);
      setEditingId(null);
      setNewAgentName("");
      setNewAgentPhone("");
      setNewAgentEmail("");
      setNewSpecialization("");
    } else {
      // Tryb dodawania
      const code = generateCode();
      const newAgent: Agent | Doctor = addingType === "agent" ? {
        id: Date.now().toString(),
        name: newAgentName.trim(),
        phone: newAgentPhone.trim(),
        email: newAgentEmail.trim(),
        code: code,
        createdAt: new Date().toISOString(),
      } : {
        id: Date.now().toString(),
        name: newAgentName.trim(),
        phone: newAgentPhone.trim(),
        email: newAgentEmail.trim(),
        specialization: newSpecialization.trim(),
        code: code,
        createdAt: new Date().toISOString(),
      };

      const updatedAgents = addingType === "agent" ? [...agents, newAgent as Agent] : [...doctors, newAgent as Doctor];
      if (addingType === "agent") {
        setAgents(updatedAgents as Agent[]);
        localStorage.setItem("healthAgents", JSON.stringify(updatedAgents));
      } else {
        setDoctors(updatedAgents as Doctor[]);
        localStorage.setItem("healthDoctors", JSON.stringify(updatedAgents));
      }

      setNewAgentCode(code);
      setShowAddDialog(false);
      setShowCodeDialog(true);
      setCopiedCode(false);
    }
  };

  const handleCopyCode = () => {
    navigator.clipboard.writeText(newAgentCode);
    setCopiedCode(true);
    setTimeout(() => setCopiedCode(false), 2000);
  };

  const handleSendSms = () => {
    const message = `Cześć ${newAgentName}! Twój kod dostępu do monitorowania zdrowia: ${newAgentCode}`;
    const phoneNumber = newAgentPhone.replace(/\s/g, ''); // Usuń spacje z numeru
    window.location.href = `sms:${phoneNumber}?body=${encodeURIComponent(message)}`;
  };

  const handleCloseCodeDialog = () => {
    setShowCodeDialog(false);
    setNewAgentCode("");
    setNewAgentName("");
    setNewAgentPhone("");
    setNewAgentEmail("");
    setNewSpecialization("");
  };

  const handleDeleteAgent = () => {
    if (!editingId) return;

    if (addingType === "agent") {
      const updatedAgents = agents.filter(agent => agent.id !== editingId);
      setAgents(updatedAgents);
      localStorage.setItem("healthAgents", JSON.stringify(updatedAgents));
    } else {
      const updatedDoctors = doctors.filter(doctor => doctor.id !== editingId);
      setDoctors(updatedDoctors);
      localStorage.setItem("healthDoctors", JSON.stringify(updatedDoctors));
    }

    setShowAddDialog(false);
    setEditMode(false);
    setEditingId(null);
    setNewAgentName("");
    setNewAgentPhone("");
    setNewAgentEmail("");
    setNewSpecialization("");
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
          Agenci Monitorujący
        </Typography>
      </Box>

      <Container maxWidth="md" sx={{ mt: 2, px: 2 }}>
        {agents.length === 0 ? (
          <Card sx={{ boxShadow: "0 2px 4px rgba(0,0,0,0.2)", mb: 3 }}>
            <CardContent sx={{ py: 6 }}>
              <Typography variant="h6" align="center" color="text.secondary" sx={{ mb: 2 }}>
                Brak dodanych agentów
              </Typography>
              <Typography variant="body1" align="center" color="text.secondary">
                Kliknij przycisk poniżej, aby dodać pierwszego agenta
              </Typography>
            </CardContent>
          </Card>
        ) : (
          <Card sx={{ boxShadow: "0 2px 4px rgba(0,0,0,0.2)", mb: 3 }}>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 2, fontWeight: 500 }}>
                Lista Agentów ({agents.length})
              </Typography>
              <List sx={{ p: 0 }}>
                {agents.map((agent, index) => (
                  <ListItem
                    key={agent.id}
                    onClick={() => handleEditAgent(agent)}
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
                      <Person sx={{ fontSize: 48, color: "#1976d2" }} />
                    </ListItemIcon>
                    <ListItemText
                      primary={agent.name}
                      secondary={
                        <>
                          {agent.phone && (
                            <Typography component="span" variant="body1" sx={{ display: "block" }}>
                              Tel: {agent.phone}
                            </Typography>
                          )}
                          {agent.email && (
                            <Typography component="span" variant="body1" sx={{ display: "block" }}>
                              Email: {agent.email}
                            </Typography>
                          )}
                          <Typography component="span" variant="body1">
                            Kod: {agent.code}
                          </Typography>
                          <Typography component="span" variant="body2" sx={{ display: "block", mt: 0.5 }}>
                            Dodano: {new Date(agent.createdAt).toLocaleDateString(
                              "pl-PL",
                              {
                                day: "2-digit",
                                month: "2-digit",
                                year: "numeric",
                              }
                            )}
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

        {doctors.length === 0 ? (
          <Card sx={{ boxShadow: "0 2px 4px rgba(0,0,0,0.2)", mb: 3 }}>
            <CardContent sx={{ py: 6 }}>
              <Typography variant="h6" align="center" color="text.secondary" sx={{ mb: 2 }}>
                Brak dodanych lekarzy
              </Typography>
              <Typography variant="body1" align="center" color="text.secondary">
                Kliknij przycisk poniżej, aby dodać pierwszego lekarza
              </Typography>
            </CardContent>
          </Card>
        ) : (
          <Card sx={{ boxShadow: "0 2px 4px rgba(0,0,0,0.2)", mb: 3 }}>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 2, fontWeight: 500 }}>
                Lista Lekarzy ({doctors.length})
              </Typography>
              <List sx={{ p: 0 }}>
                {doctors.map((doctor, index) => (
                  <ListItem
                    key={doctor.id}
                    onClick={() => handleEditDoctor(doctor)}
                    sx={{
                      backgroundColor: index % 2 === 0 ? "#fafafa" : "white",
                      borderRadius: "8px",
                      mb: 1,
                      border: "1px solid #e0e0e0",
                      py: 2,
                      cursor: "pointer",
                      "&:hover": {
                        backgroundColor: "#e8f5e9",
                        border: "1px solid #4CAF50",
                      },
                    }}
                  >
                    <ListItemIcon>
                      <Person sx={{ fontSize: 48, color: "#4CAF50" }} />
                    </ListItemIcon>
                    <ListItemText
                      primary={doctor.name}
                      secondary={
                        <>
                          {doctor.specialization && (
                            <Typography component="span" variant="body1" sx={{ display: "block", fontWeight: 500, color: "#4CAF50" }}>
                              {doctor.specialization}
                            </Typography>
                          )}
                          {doctor.phone && (
                            <Typography component="span" variant="body1" sx={{ display: "block" }}>
                              Tel: {doctor.phone}
                            </Typography>
                          )}
                          {doctor.email && (
                            <Typography component="span" variant="body1" sx={{ display: "block" }}>
                              Email: {doctor.email}
                            </Typography>
                          )}
                          <Typography component="span" variant="body1">
                            Kod: {doctor.code}
                          </Typography>
                          <Typography component="span" variant="body2" sx={{ display: "block", mt: 0.5 }}>
                            Dodano: {new Date(doctor.createdAt).toLocaleDateString(
                              "pl-PL",
                              {
                                day: "2-digit",
                                month: "2-digit",
                                year: "numeric",
                              }
                            )}
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

        <Box
          sx={{
            display: "grid",
            gridTemplateColumns: { xs: "1fr", sm: "1fr 1fr" },
            gap: 2,
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
            onClick={handleAddAgent}
          >
            <CardContent
              sx={{
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                gap: 2,
                py: 4,
                backgroundColor: "#4CAF50",
                color: "white",
                "&:last-child": { pb: 4 },
              }}
            >
              <PersonAdd sx={{ fontSize: 72 }} />
              <Typography variant="h5" component="div" sx={{ fontWeight: 500 }}>
                Dodaj Agenta
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
            onClick={handleAddDoctor}
          >
            <CardContent
              sx={{
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                gap: 2,
                py: 4,
                backgroundColor: "#2196F3",
                color: "white",
                "&:last-child": { pb: 4 },
              }}
            >
              <MedicalServices sx={{ fontSize: 72 }} />
              <Typography variant="h5" component="div" sx={{ fontWeight: 500 }}>
                Dodaj Lekarza
              </Typography>
            </CardContent>
          </Card>
        </Box>
      </Container>

      {/* Dialog dodawania agenta */}
      <Dialog
        open={showAddDialog}
        onClose={() => setShowAddDialog(false)}
        maxWidth="sm"
        fullWidth
        PaperProps={{
          sx: {
            borderRadius: "12px",
          },
        }}
      >
        <DialogTitle sx={{ fontSize: "24px", fontWeight: 500 }}>
          {editMode ? `Edytuj ${addingType === "agent" ? "Agenta" : "Lekarza"}` : `Dodaj ${addingType === "agent" ? "Agenta" : "Lekarza"}`}
        </DialogTitle>
        <DialogContent>
          <Typography variant="body1" sx={{ mb: 3, color: "text.secondary" }}>
            {editMode ? "Zmień dane wybranej osoby" : "Podaj nazwę osoby, która będzie monitorować Twoje zdrowie (np. syn, córka, opiekun)"}
          </Typography>
          <TextField
            autoFocus
            fullWidth
            label="Nazwa agenta"
            variant="outlined"
            value={newAgentName}
            onChange={(e) => setNewAgentName(e.target.value)}
            placeholder="np. Janek, Córka Ania"
            sx={{
              mb: 2,
              "& .MuiOutlinedInput-root": {
                fontSize: "18px",
              },
              "& .MuiInputLabel-root": {
                fontSize: "18px",
              },
            }}
            onKeyPress={(e) => {
              if (e.key === "Enter" && newAgentName.trim()) {
                handleConfirmAdd();
              }
            }}
          />
          <TextField
            fullWidth
            label="Numer telefonu"
            variant="outlined"
            value={newAgentPhone}
            onChange={(e) => setNewAgentPhone(e.target.value)}
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
            onKeyPress={(e) => {
              if (e.key === "Enter" && newAgentName.trim()) {
                handleConfirmAdd();
              }
            }}
          />
          <TextField
            fullWidth
            label="Email"
            variant="outlined"
            type="email"
            value={newAgentEmail}
            onChange={(e) => setNewAgentEmail(e.target.value)}
            placeholder="np. jan.kowalski@email.com"
            sx={{
              "& .MuiOutlinedInput-root": {
                fontSize: "18px",
              },
              "& .MuiInputLabel-root": {
                fontSize: "18px",
              },
            }}
            onKeyPress={(e) => {
              if (e.key === "Enter" && newAgentName.trim()) {
                handleConfirmAdd();
              }
            }}
          />
          {addingType === "doctor" && (
            <TextField
              fullWidth
              label="Specjalizacja"
              variant="outlined"
              value={newSpecialization}
              onChange={(e) => setNewSpecialization(e.target.value)}
              placeholder="np. Kardiolog, Neurolog"
              sx={{
                "& .MuiOutlinedInput-root": {
                  fontSize: "18px",
                },
                "& .MuiInputLabel-root": {
                  fontSize: "18px",
                },
              }}
              onKeyPress={(e) => {
                if (e.key === "Enter" && newAgentName.trim()) {
                  handleConfirmAdd();
                }
              }}
            />
          )}
        </DialogContent>
        <DialogActions sx={{ p: 3, pt: 2, flexDirection: "column", gap: 2 }}>
          <Box sx={{ display: "flex", width: "100%", gap: 2 }}>
            <Button
              onClick={() => setShowAddDialog(false)}
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
              onClick={handleConfirmAdd}
              variant="contained"
              disabled={!newAgentName.trim()}
              sx={{
                fontSize: "16px",
                px: 3,
                py: 1.5,
                flex: 1,
                backgroundColor: "#4CAF50",
                "&:hover": {
                  backgroundColor: "#45a049",
                },
              }}
            >
              {editMode ? "Zapisz" : "Dodaj"}
            </Button>
          </Box>
          {editMode && (
            <Button
              variant="text"
              startIcon={<Delete sx={{ fontSize: 18 }} />}
              onClick={handleDeleteAgent}
              sx={{
                fontSize: "14px",
                py: 1,
                color: "#F44336",
                "&:hover": {
                  backgroundColor: "rgba(244, 67, 54, 0.08)",
                },
              }}
            >
              Usuń agenta
            </Button>
          )}
        </DialogActions>
      </Dialog>

      {/* Dialog z kodem */}
      <Dialog
        open={showCodeDialog}
        onClose={handleCloseCodeDialog}
        maxWidth="sm"
        fullWidth
        PaperProps={{
          sx: {
            borderRadius: "12px",
          },
        }}
      >
        <DialogContent>
          <Box sx={{ textAlign: "center", py: 3 }}>
            <Typography variant="h4" sx={{ mb: 4, fontWeight: 600, color: "#1976d2" }}>
              Przekaż poniższy kod agentowi <strong>{newAgentName}</strong>
            </Typography>
            
            <Box
              sx={{
                backgroundColor: "#e3f2fd",
                border: "3px solid #1976d2",
                borderRadius: "12px",
                py: 4,
                px: 3,
                mb: 3,
              }}
            >
              <Typography variant="h3" sx={{ fontWeight: 700, color: "#1976d2", letterSpacing: "8px" }}>
                {newAgentCode}
              </Typography>
            </Box>

            <Box
              sx={{
                backgroundColor: "#fff3e0",
                border: "2px solid #ff9800",
                borderRadius: "8px",
                py: 2,
                px: 3,
                mb: 3,
              }}
            >
              <Typography variant="body1" sx={{ fontWeight: 500, color: "#e65100" }}>
                ⏱️ Kod jest aktywny przez 1 godzinę
              </Typography>
              <Typography variant="body2" sx={{ color: "#ef6c00", mt: 0.5 }}>
                Agent ma 1 godzinę na podłączenie się do systemu
              </Typography>
            </Box>

            {newAgentPhone && (
              <Button
                fullWidth
                variant="contained"
                size="large"
                startIcon={<Sms sx={{ fontSize: 32 }} />}
                onClick={handleSendSms}
                sx={{
                  fontSize: "20px",
                  py: 2.5,
                  mb: 2,
                  backgroundColor: "#4CAF50",
                  "&:hover": {
                    backgroundColor: "#45a049",
                  },
                }}
              >
                Wyślij SMS
              </Button>
            )}

            <Box sx={{ display: "flex", gap: 2, justifyContent: "center", mb: 2 }}>
              <Button
                variant="text"
                startIcon={<ContentCopy />}
                onClick={handleCopyCode}
                sx={{
                  fontSize: "14px",
                  px: 3,
                  py: 1,
                  color: "#1976d2",
                }}
              >
                {copiedCode ? "Skopiowano!" : "Kopiuj kod"}
              </Button>
            </Box>
          </Box>
        </DialogContent>
        <DialogActions sx={{ p: 3, pt: 0, justifyContent: "center" }}>
          <Button
            onClick={handleCloseCodeDialog}
            variant="text"
            sx={{
              fontSize: "16px",
              px: 4,
              py: 1,
              color: "#757575",
            }}
          >
            Zakończ
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}