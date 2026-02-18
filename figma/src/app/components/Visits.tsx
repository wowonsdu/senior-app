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
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Switch,
  FormControlLabel,
  Chip,
  List,
  ListItem,
  ListItemText,
  ListItemAvatar,
  Avatar,
  Divider,
  Alert,
} from "@mui/material";
import {
  Add,
  Person,
  LocalHospital,
  AccessTime,
  LocationOn,
  Notes,
  CheckCircle,
  PendingActions,
  Edit,
  Delete,
  Sms,
  Notifications as NotificationsIcon,
  CalendarMonth,
} from "@mui/icons-material";

interface Visit {
  id: string;
  patientId: string;
  patientName: string;
  type: "caregiver_visit" | "doctor_appointment";
  title: string;
  description?: string;
  date: string; // YYYY-MM-DD
  time: string; // HH:MM
  location?: string;
  doctorName?: string;
  doctorSpecialization?: string;
  notes?: string;
  reminderSms: boolean;
  reminderInApp: boolean;
  reminderBefore: number; // minuty przed wizytą
  completed: boolean;
  createdBy: string; // caregiver id
  createdAt: string;
}

interface Patient {
  id: string;
  name: string;
  phone?: string;
  addedDate: string;
  accessCode?: string;
}

export function Visits() {
  const navigate = useNavigate();
  const [visits, setVisits] = useState<Visit[]>([]);
  const [patients, setPatients] = useState<Patient[]>([]);
  const [openDialog, setOpenDialog] = useState(false);
  const [editingVisit, setEditingVisit] = useState<Visit | null>(null);
  const [filterType, setFilterType] = useState<"all" | "upcoming" | "completed">("upcoming");

  // Form state
  const [selectedPatient, setSelectedPatient] = useState("");
  const [visitType, setVisitType] = useState<"caregiver_visit" | "doctor_appointment">("caregiver_visit");
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [date, setDate] = useState("");
  const [time, setTime] = useState("");
  const [location, setLocation] = useState("");
  const [doctorName, setDoctorName] = useState("");
  const [doctorSpecialization, setDoctorSpecialization] = useState("");
  const [notes, setNotes] = useState("");
  const [reminderSms, setReminderSms] = useState(true);
  const [reminderInApp, setReminderInApp] = useState(true);
  const [reminderBefore, setReminderBefore] = useState(60); // 60 minut domyślnie

  useEffect(() => {
    loadData();
  }, []);

  const loadData = () => {
    // Załaduj listę pacjentów
    const savedPatients = localStorage.getItem("healthPatients");
    const patientsList = savedPatients ? JSON.parse(savedPatients) : [];
    setPatients(patientsList);

    // Załaduj wizyty
    const savedVisits = localStorage.getItem("healthVisits");
    const visitsList = savedVisits ? JSON.parse(savedVisits) : [];
    setVisits(visitsList);
  };

  const handleOpenDialog = (visit?: Visit) => {
    if (visit) {
      setEditingVisit(visit);
      setSelectedPatient(visit.patientId);
      setVisitType(visit.type);
      setTitle(visit.title);
      setDescription(visit.description || "");
      setDate(visit.date);
      setTime(visit.time);
      setLocation(visit.location || "");
      setDoctorName(visit.doctorName || "");
      setDoctorSpecialization(visit.doctorSpecialization || "");
      setNotes(visit.notes || "");
      setReminderSms(visit.reminderSms);
      setReminderInApp(visit.reminderInApp);
      setReminderBefore(visit.reminderBefore);
    } else {
      setEditingVisit(null);
      resetForm();
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setEditingVisit(null);
    resetForm();
  };

  const resetForm = () => {
    setSelectedPatient("");
    setVisitType("caregiver_visit");
    setTitle("");
    setDescription("");
    setDate("");
    setTime("");
    setLocation("");
    setDoctorName("");
    setDoctorSpecialization("");
    setNotes("");
    setReminderSms(true);
    setReminderInApp(true);
    setReminderBefore(60);
  };

  const handleSave = () => {
    if (!selectedPatient || !title || !date || !time) {
      alert("Wypełnij wszystkie wymagane pola");
      return;
    }

    const patient = patients.find((p) => p.id === selectedPatient);
    if (!patient) return;

    const caregiverData = localStorage.getItem("healthAppAgentData");
    const caregiver = caregiverData ? JSON.parse(caregiverData) : null;

    const newVisit: Visit = {
      id: editingVisit ? editingVisit.id : Date.now().toString(),
      patientId: selectedPatient,
      patientName: patient.name,
      type: visitType,
      title,
      description,
      date,
      time,
      location,
      doctorName: visitType === "doctor_appointment" ? doctorName : undefined,
      doctorSpecialization: visitType === "doctor_appointment" ? doctorSpecialization : undefined,
      notes,
      reminderSms,
      reminderInApp,
      reminderBefore,
      completed: editingVisit ? editingVisit.completed : false,
      createdBy: caregiver?.id || "",
      createdAt: editingVisit ? editingVisit.createdAt : new Date().toISOString(),
    };

    const updatedVisits = editingVisit
      ? visits.map((v) => (v.id === editingVisit.id ? newVisit : v))
      : [...visits, newVisit];

    localStorage.setItem("healthVisits", JSON.stringify(updatedVisits));
    setVisits(updatedVisits);
    handleCloseDialog();
  };

  const handleDelete = (visitId: string) => {
    if (confirm("Czy na pewno chcesz usunąć tę wizytę?")) {
      const updatedVisits = visits.filter((v) => v.id !== visitId);
      localStorage.setItem("healthVisits", JSON.stringify(updatedVisits));
      setVisits(updatedVisits);
    }
  };

  const handleToggleCompleted = (visitId: string) => {
    const updatedVisits = visits.map((v) =>
      v.id === visitId ? { ...v, completed: !v.completed } : v
    );
    localStorage.setItem("healthVisits", JSON.stringify(updatedVisits));
    setVisits(updatedVisits);
  };

  const getFilteredVisits = () => {
    const now = new Date();
    const today = now.toISOString().split("T")[0];

    return visits
      .filter((visit) => {
        if (filterType === "completed") return visit.completed;
        if (filterType === "upcoming") return !visit.completed && visit.date >= today;
        return true;
      })
      .sort((a, b) => {
        // Sortuj po dacie i czasie
        const dateA = new Date(`${a.date}T${a.time}`);
        const dateB = new Date(`${b.date}T${b.time}`);
        return dateA.getTime() - dateB.getTime();
      });
  };

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr + "T00:00:00");
    return date.toLocaleDateString("pl-PL", {
      weekday: "long",
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  const isUpcoming = (visit: Visit) => {
    const now = new Date();
    const visitDateTime = new Date(`${visit.date}T${visit.time}`);
    const diffMs = visitDateTime.getTime() - now.getTime();
    const diffHours = diffMs / (1000 * 60 * 60);
    return diffHours <= 24 && diffHours > 0;
  };

  const filteredVisits = getFilteredVisits();

  return (
    <>
      <Box sx={{ mb: 3 }}>
        {/* Filtry */}
        <Box sx={{ display: "flex", gap: 2, mb: 3, flexWrap: "wrap" }}>
          <Button
            variant={filterType === "upcoming" ? "contained" : "outlined"}
            onClick={() => setFilterType("upcoming")}
            sx={{ fontSize: "15px", textTransform: "none", flex: 1 }}
          >
            Nadchodzące
          </Button>
          <Button
            variant={filterType === "all" ? "contained" : "outlined"}
            onClick={() => setFilterType("all")}
            sx={{ fontSize: "15px", textTransform: "none", flex: 1 }}
          >
            Wszystkie
          </Button>
          <Button
            variant={filterType === "completed" ? "contained" : "outlined"}
            onClick={() => setFilterType("completed")}
            sx={{ fontSize: "15px", textTransform: "none", flex: 1 }}
          >
            Zakończone
          </Button>
        </Box>

        {/* Przycisk dodawania */}
        <Button
          fullWidth
          variant="contained"
          size="large"
          startIcon={<Add />}
          onClick={() => handleOpenDialog()}
          sx={{
            py: 2,
            fontSize: "1.1rem",
            fontWeight: 500,
            backgroundColor: "#4CAF50",
            "&:hover": {
              backgroundColor: "#45a049",
            },
          }}
        >
          Zaplanuj nową wizytę
        </Button>
      </Box>

      {/* Lista wizyt */}
      {filteredVisits.length === 0 ? (
        <Box sx={{ textAlign: "center", py: 6 }}>
          <CalendarMonth sx={{ fontSize: 120, color: "#ccc", mb: 3 }} />
          <Typography variant="h5" sx={{ mb: 2, fontWeight: 500, color: "#666" }}>
            {filterType === "upcoming" && "Brak nadchodzących wizyt"}
            {filterType === "completed" && "Brak zakończonych wizyt"}
            {filterType === "all" && "Brak zaplanowanych wizyt"}
          </Typography>
          <Typography variant="body1" sx={{ mb: 4, color: "#999" }}>
            {filterType === "upcoming" && "Zaplanuj wizytę dla swoich podopiecznych"}
            {filterType === "completed" && "Zakończone wizyty pojawią się tutaj"}
            {filterType === "all" && "Kliknij przycisk powyżej aby dodać pierwszą wizytę"}
          </Typography>
        </Box>
      ) : (
        <List sx={{ p: 0 }}>
          {filteredVisits.map((visit) => (
            <Card
              key={visit.id}
              sx={{
                mb: 2,
                boxShadow: "0 2px 8px rgba(0,0,0,0.15)",
                border: isUpcoming(visit) && !visit.completed ? "2px solid #FF9800" : "none",
                backgroundColor: visit.completed ? "#f5f5f5" : "white",
              }}
            >
              <CardContent sx={{ p: 3 }}>
                <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", mb: 2 }}>
                  <Box sx={{ flex: 1 }}>
                    <Box sx={{ display: "flex", alignItems: "center", gap: 1, mb: 1 }}>
                      <Typography variant="h6" sx={{ fontWeight: 600 }}>
                        {visit.title}
                      </Typography>
                      {isUpcoming(visit) && !visit.completed && (
                        <Chip label="Za 24h" size="small" color="warning" sx={{ fontWeight: 600 }} />
                      )}
                      {visit.completed && (
                        <Chip
                          icon={<CheckCircle />}
                          label="Zakończone"
                          size="small"
                          color="success"
                          sx={{ fontWeight: 600 }}
                        />
                      )}
                    </Box>
                    <Chip
                      label={visit.type === "caregiver_visit" ? "Wizyta opiekuna" : "Wizyta u lekarza"}
                      size="small"
                      sx={{
                        backgroundColor: visit.type === "caregiver_visit" ? "#2196F3" : "#9C27B0",
                        color: "white",
                        fontSize: "12px",
                        mb: 1,
                      }}
                    />
                  </Box>
                  <Box sx={{ display: "flex", gap: 1 }}>
                    <IconButton size="small" onClick={() => handleOpenDialog(visit)}>
                      <Edit sx={{ fontSize: 20 }} />
                    </IconButton>
                    <IconButton size="small" onClick={() => handleDelete(visit.id)} color="error">
                      <Delete sx={{ fontSize: 20 }} />
                    </IconButton>
                  </Box>
                </Box>

                <Box sx={{ display: "flex", flexDirection: "column", gap: 1.5 }}>
                  <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                    <Person sx={{ fontSize: 20, color: "#666" }} />
                    <Typography variant="body2" sx={{ fontWeight: 500 }}>
                      {visit.patientName}
                    </Typography>
                  </Box>

                  <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                    <CalendarMonth sx={{ fontSize: 20, color: "#666" }} />
                    <Typography variant="body2">{formatDate(visit.date)}</Typography>
                  </Box>

                  <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                    <AccessTime sx={{ fontSize: 20, color: "#666" }} />
                    <Typography variant="body2">{visit.time}</Typography>
                  </Box>

                  {visit.location && (
                    <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                      <LocationOn sx={{ fontSize: 20, color: "#666" }} />
                      <Typography variant="body2">{visit.location}</Typography>
                    </Box>
                  )}

                  {visit.doctorName && (
                    <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                      <LocalHospital sx={{ fontSize: 20, color: "#666" }} />
                      <Typography variant="body2">
                        {visit.doctorName}
                        {visit.doctorSpecialization && ` - ${visit.doctorSpecialization}`}
                      </Typography>
                    </Box>
                  )}

                  {visit.description && (
                    <Box sx={{ mt: 1 }}>
                      <Typography variant="body2" color="text.secondary">
                        {visit.description}
                      </Typography>
                    </Box>
                  )}

                  {visit.notes && (
                    <Box sx={{ display: "flex", alignItems: "flex-start", gap: 1, mt: 1 }}>
                      <Notes sx={{ fontSize: 20, color: "#666" }} />
                      <Typography variant="body2" color="text.secondary">
                        {visit.notes}
                      </Typography>
                    </Box>
                  )}

                  {/* Powiadomienia */}
                  <Box sx={{ display: "flex", gap: 1, mt: 1, flexWrap: "wrap" }}>
                    {visit.reminderSms && (
                      <Chip
                        icon={<Sms />}
                        label={`SMS ${visit.reminderBefore} min przed`}
                        size="small"
                        variant="outlined"
                      />
                    )}
                    {visit.reminderInApp && (
                      <Chip
                        icon={<NotificationsIcon />}
                        label={`Powiadomienie ${visit.reminderBefore} min przed`}
                        size="small"
                        variant="outlined"
                      />
                    )}
                  </Box>

                  {/* Przycisk zakończ/wznów */}
                  <Button
                    fullWidth
                    variant={visit.completed ? "outlined" : "contained"}
                    onClick={() => handleToggleCompleted(visit.id)}
                    sx={{
                      mt: 2,
                      textTransform: "none",
                      fontSize: "15px",
                    }}
                  >
                    {visit.completed ? "Przywróć wizytę" : "Oznacz jako zakończoną"}
                  </Button>
                </Box>
              </CardContent>
            </Card>
          ))}
        </List>
      )}

      {/* Dialog dodawania/edycji wizyty */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ fontWeight: 600, fontSize: "1.5rem" }}>
          {editingVisit ? "Edytuj wizytę" : "Zaplanuj nową wizytę"}
        </DialogTitle>
        <DialogContent>
          <Box sx={{ display: "flex", flexDirection: "column", gap: 3, mt: 2 }}>
            {/* Wybór pacjenta */}
            <FormControl fullWidth required>
              <InputLabel>Pacjent</InputLabel>
              <Select
                value={selectedPatient}
                onChange={(e) => setSelectedPatient(e.target.value)}
                label="Pacjent"
              >
                {patients.map((patient) => (
                  <MenuItem key={patient.id} value={patient.id}>
                    {patient.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            {/* Typ wizyty */}
            <FormControl fullWidth required>
              <InputLabel>Typ wizyty</InputLabel>
              <Select
                value={visitType}
                onChange={(e) => setVisitType(e.target.value as "caregiver_visit" | "doctor_appointment")}
                label="Typ wizyty"
              >
                <MenuItem value="caregiver_visit">Wizyta opiekuna do pacjenta</MenuItem>
                <MenuItem value="doctor_appointment">Wizyta pacjenta u lekarza</MenuItem>
              </Select>
            </FormControl>

            {/* Tytuł */}
            <TextField
              label="Tytuł wizyty"
              required
              fullWidth
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="np. Kontrola stanu zdrowia"
            />

            {/* Opis */}
            <TextField
              label="Opis"
              fullWidth
              multiline
              rows={2}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Opcjonalny opis wizyty"
            />

            {/* Data i czas */}
            <Box sx={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 2 }}>
              <TextField
                label="Data"
                type="date"
                required
                fullWidth
                value={date}
                onChange={(e) => setDate(e.target.value)}
                InputLabelProps={{ shrink: true }}
              />
              <TextField
                label="Godzina"
                type="time"
                required
                fullWidth
                value={time}
                onChange={(e) => setTime(e.target.value)}
                InputLabelProps={{ shrink: true }}
              />
            </Box>

            {/* Lokalizacja */}
            <TextField
              label="Lokalizacja"
              fullWidth
              value={location}
              onChange={(e) => setLocation(e.target.value)}
              placeholder="Adres wizyty"
            />

            {/* Pola dla wizyty u lekarza */}
            {visitType === "doctor_appointment" && (
              <>
                <TextField
                  label="Imię i nazwisko lekarza"
                  fullWidth
                  value={doctorName}
                  onChange={(e) => setDoctorName(e.target.value)}
                  placeholder="Dr Jan Kowalski"
                />
                <TextField
                  label="Specjalizacja"
                  fullWidth
                  value={doctorSpecialization}
                  onChange={(e) => setDoctorSpecialization(e.target.value)}
                  placeholder="Kardiolog"
                />
              </>
            )}

            {/* Notatki */}
            <TextField
              label="Notatki"
              fullWidth
              multiline
              rows={2}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Dodatkowe informacje"
            />

            <Divider />

            {/* Powiadomienia */}
            <Typography variant="h6" sx={{ fontWeight: 600, color: "#2196F3" }}>
              Powiadomienia o wizycie
            </Typography>

            <Alert severity="info" sx={{ fontSize: "14px" }}>
              Wybierz kanały i czas powiadomienia przed wizytą
            </Alert>

            <FormControlLabel
              control={<Switch checked={reminderSms} onChange={(e) => setReminderSms(e.target.checked)} />}
              label={
                <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                  <Sms />
                  <Typography>Powiadomienie SMS</Typography>
                </Box>
              }
            />

            <FormControlLabel
              control={
                <Switch checked={reminderInApp} onChange={(e) => setReminderInApp(e.target.checked)} />
              }
              label={
                <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                  <NotificationsIcon />
                  <Typography>Powiadomienie w aplikacji</Typography>
                </Box>
              }
            />

            <FormControl fullWidth>
              <InputLabel>Przypomnij przed wizytą</InputLabel>
              <Select
                value={reminderBefore}
                onChange={(e) => setReminderBefore(Number(e.target.value))}
                label="Przypomnij przed wizytą"
              >
                <MenuItem value={15}>15 minut</MenuItem>
                <MenuItem value={30}>30 minut</MenuItem>
                <MenuItem value={60}>1 godzinę</MenuItem>
                <MenuItem value={120}>2 godziny</MenuItem>
                <MenuItem value={1440}>1 dzień</MenuItem>
                <MenuItem value={2880}>2 dni</MenuItem>
              </Select>
            </FormControl>
          </Box>
        </DialogContent>
        <DialogActions sx={{ p: 3, pt: 2 }}>
          <Button onClick={handleCloseDialog} sx={{ fontSize: "15px", textTransform: "none" }}>
            Anuluj
          </Button>
          <Button
            onClick={handleSave}
            variant="contained"
            sx={{
              fontSize: "15px",
              textTransform: "none",
              backgroundColor: "#4CAF50",
              "&:hover": { backgroundColor: "#45a049" },
            }}
          >
            {editingVisit ? "Zapisz zmiany" : "Dodaj wizytę"}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}