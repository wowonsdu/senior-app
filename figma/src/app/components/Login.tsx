import { useState, useEffect } from "react";
import { useNavigate } from "react-router";
import {
  Box,
  Container,
  Typography,
  Card,
  CardContent,
  TextField,
  Button,
  Alert,
  IconButton,
  Divider,
} from "@mui/material";
import { Phone, Sms, CheckCircle, VpnKey, ArrowBack } from "@mui/icons-material";

export function Login() {
  const navigate = useNavigate();
  const selectedRole = localStorage.getItem("healthAppSelectedRole") || "";
  
  // Pola wejściowe
  const [phoneField, setPhoneField] = useState("");
  const [codeField, setCodeField] = useState("");
  const [smsCodeField, setSmsCodeField] = useState("");
  const [sentCode, setSentCode] = useState("");
  const [error, setError] = useState("");

  // Sprawdź czy wybrano rolę - jeśli nie, wróć do wyboru
  useEffect(() => {
    if (!selectedRole) {
      navigate("/");
    }
  }, [selectedRole, navigate]);

  // Nie renderuj nic jeśli nie ma wybranej roli (przekierowanie w toku)
  if (!selectedRole) {
    return (
      <Box
        sx={{
          backgroundColor: "#1976d2",
          minHeight: "100vh",
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
        }}
      >
        <Typography variant="h5" sx={{ color: "white" }}>
          Ładowanie...
        </Typography>
      </Box>
    );
  }

  const handleBackToRole = () => {
    localStorage.removeItem("healthAppSelectedRole");
    navigate("/");
  };

  const handleContinue = () => {
    setError("");

    // Sprawdź który tryb został wybrany
    const cleanPhone = phoneField.replace(/\s/g, "");
    const cleanCode = codeField.replace(/\s/g, "");

    // Jeśli wypełniono kod dostępu (6 cyfr)
    if (cleanCode.length === 6 && !cleanPhone) {
      handleLoginWithAccessCode(cleanCode);
      return;
    }

    // Jeśli wypełniono numer telefonu
    if (cleanPhone.length >= 9 && !cleanCode) {
      handleSendCode();
      return;
    }

    // Jeśli wypełniono oba
    if (cleanPhone && cleanCode) {
      setError("Wypełnij tylko jedno pole - numer telefonu LUB kod dostępu");
      return;
    }

    // Jeśli nie wypełniono żadnego
    setError("Wypełnij numer telefonu lub kod dostępu");
  };

  const handleSendCode = () => {
    setError("");

    // Walidacja numeru telefonu
    const cleanPhone = phoneField.replace(/\s/g, "");
    if (cleanPhone.length < 9) {
      setError("Podaj prawidłowy numer telefonu (minimum 9 cyfr)");
      return;
    }

    // Generowanie losowego 6-cyfrowego kodu
    const generatedCode = Math.floor(100000 + Math.random() * 900000).toString();
    setSentCode(generatedCode);

    // W prawdziwej aplikacji tutaj byłoby wywołanie API do wysłania SMS
    console.log(`SMS wysłany na numer ${phoneField} z kodem: ${generatedCode}`);
  };

  const handleVerifyCode = () => {
    setError("");

    if (smsCodeField !== sentCode) {
      setError("Nieprawidłowy kod. Spróbuj ponownie.");
      return;
    }

    // Zapisz stan zalogowania
    localStorage.setItem("healthAppLoggedIn", "true");
    localStorage.setItem("healthAppPhone", phoneField);
    localStorage.setItem("healthAppLoginDate", new Date().toISOString());
    localStorage.setItem("healthAppUserRole", selectedRole);

    // Przekieruj do głównego ekranu
    navigate("/home");
  };

  const handleLoginWithAccessCode = (code: string) => {
    setError("");

    if (code.length !== 6) {
      setError("Kod dostępu musi mieć 6 cyfr");
      return;
    }

    // Sprawdź czy kod istnieje w liście agentów lub lekarzy
    const agents = localStorage.getItem("healthAgents");
    const doctors = localStorage.getItem("healthDoctors");
    const patients = localStorage.getItem("healthPatients");
    
    const agentsList = agents ? JSON.parse(agents) : [];
    const doctorsList = doctors ? JSON.parse(doctors) : [];
    const patientsList = patients ? JSON.parse(patients) : [];
    
    // Dla opiekunów - sprawdź w agentach i lekarzach
    if (selectedRole === "agent") {
      const allContacts = [...agentsList, ...doctorsList];
      const matchingContact = allContacts.find(contact => contact.code === code);

      if (matchingContact) {
        // Kod prawidłowy - zaloguj
        localStorage.setItem("healthAppLoggedIn", "true");
        localStorage.setItem("healthAppLoginDate", new Date().toISOString());
        localStorage.setItem("healthAppUserRole", selectedRole);
        localStorage.setItem("healthAppAgentData", JSON.stringify(matchingContact));
        navigate("/home");
        return;
      }
    }
    
    // Dla pacjentów (seniorów) - sprawdź w pacjentach
    if (selectedRole === "senior") {
      const matchingPatient = patientsList.find((patient: any) => patient.accessCode === code);

      if (matchingPatient) {
        // Kod prawidłowy - zaloguj jako pacjent
        localStorage.setItem("healthAppLoggedIn", "true");
        localStorage.setItem("healthAppLoginDate", new Date().toISOString());
        localStorage.setItem("healthAppUserRole", selectedRole);
        localStorage.setItem("healthAppPatientData", JSON.stringify(matchingPatient));
        navigate("/home");
        return;
      }
    }
    
    setError("Nieprawidłowy kod dostępu. Upewnij się, że kod jest aktywny.");
  };

  const handleResendCode = () => {
    setError("");
    setSmsCodeField("");
    const generatedCode = Math.floor(100000 + Math.random() * 900000).toString();
    setSentCode(generatedCode);
    console.log(`SMS ponownie wysłany na numer ${phoneField} z kodem: ${generatedCode}`);
  };

  const handleReset = () => {
    setPhoneField("");
    setCodeField("");
    setSmsCodeField("");
    setSentCode("");
    setError("");
  };

  const roleLabel = selectedRole === "senior" ? "Pacjent" : "Opiekun";
  const roleColor = selectedRole === "senior" ? "#4CAF50" : "#2196F3";

  return (
    <Box
      sx={{
        backgroundColor: "#1976d2",
        minHeight: "100vh",
        display: "flex",
        flexDirection: "column",
        justifyContent: "center",
        alignItems: "center",
        px: 2,
        py: 4,
      }}
    >
      <Container maxWidth="sm">
        <Box sx={{ textAlign: "center", mb: 3 }}>
          <IconButton
            onClick={handleBackToRole}
            sx={{
              position: "absolute",
              top: 16,
              left: 16,
              color: "white",
              backgroundColor: "rgba(255,255,255,0.2)",
              "&:hover": {
                backgroundColor: "rgba(255,255,255,0.3)",
              },
            }}
          >
            <ArrowBack sx={{ fontSize: 32 }} />
          </IconButton>
          
          <Box
            sx={{
              display: "inline-block",
              backgroundColor: roleColor,
              color: "white",
              px: 4,
              py: 1.5,
              borderRadius: "50px",
              mb: 3,
              fontWeight: 600,
              fontSize: "20px",
              boxShadow: "0 4px 12px rgba(0,0,0,0.3)",
            }}
          >
            {roleLabel}
          </Box>
          
          <Typography
            variant="h4"
            component="h1"
            sx={{
              color: "white",
              fontWeight: 600,
              mb: 1,
              textShadow: "0 2px 4px rgba(0,0,0,0.3)",
            }}
          >
            Zaloguj się
          </Typography>
          <Typography
            variant="body1"
            sx={{
              color: "rgba(255,255,255,0.9)",
              fontSize: "18px",
            }}
          >
            Wprowadź swoje dane
          </Typography>
        </Box>

        <Card
          sx={{
            boxShadow: "0 8px 24px rgba(0,0,0,0.3)",
            borderRadius: "16px",
            mb: 2,
          }}
        >
          <CardContent sx={{ p: 4 }}>
            {!sentCode ? (
              <>
                {/* Początkowy widok - dwa pola */}
                <Box sx={{ textAlign: "center", mb: 4 }}>
                  <Box sx={{ display: "flex", justifyContent: "center", gap: 3, mb: 2 }}>
                    <Phone sx={{ fontSize: 48, color: "#1976d2" }} />
                    <Typography variant="h5" sx={{ alignSelf: "center", color: "#999", fontWeight: 300 }}>
                      lub
                    </Typography>
                    <VpnKey sx={{ fontSize: 48, color: "#2196F3" }} />
                  </Box>
                  <Typography variant="h6" sx={{ fontWeight: 600, mb: 0.5 }}>
                    Wybierz sposób logowania
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Wypełnij jedno z poniższych pól
                  </Typography>
                </Box>

                {error && (
                  <Alert severity="error" sx={{ mb: 3, fontSize: "16px" }}>
                    {error}
                  </Alert>
                )}

                {/* Pole 1: Numer telefonu */}
                <TextField
                  fullWidth
                  label="Numer telefonu"
                  variant="outlined"
                  value={phoneField}
                  onChange={(e) => {
                    setPhoneField(e.target.value);
                    if (e.target.value) setCodeField(""); // Wyczyść drugi field
                    setError("");
                  }}
                  placeholder="np. 123 456 789"
                  type="tel"
                  disabled={!!codeField}
                  sx={{
                    mb: 2,
                    "& .MuiOutlinedInput-root": {
                      fontSize: "20px",
                      py: 1.5,
                    },
                    "& .MuiInputLabel-root": {
                      fontSize: "18px",
                    },
                  }}
                  InputProps={{
                    startAdornment: (
                      <Phone sx={{ mr: 1, color: phoneField ? "#1976d2" : "#999" }} />
                    ),
                  }}
                />

                <Box sx={{ textAlign: "center", mb: 2 }}>
                  <Typography variant="body2" sx={{ color: "#999", fontWeight: 500 }}>
                    — LUB —
                  </Typography>
                </Box>

                {/* Pole 2: Kod dostępu */}
                <TextField
                  fullWidth
                  label="Kod dostępu (6 cyfr)"
                  variant="outlined"
                  value={codeField}
                  onChange={(e) => {
                    const value = e.target.value.replace(/\D/g, "").slice(0, 6);
                    setCodeField(value);
                    if (value) setPhoneField(""); // Wyczyść pierwszy field
                    setError("");
                  }}
                  placeholder="123456"
                  type="tel"
                  disabled={!!phoneField}
                  inputProps={{
                    maxLength: 6,
                    style: { 
                      textAlign: "center", 
                      fontSize: "24px", 
                      letterSpacing: "8px", 
                      fontWeight: 600 
                    },
                  }}
                  sx={{
                    mb: 3,
                    "& .MuiOutlinedInput-root": {
                      py: 1.5,
                    },
                    "& .MuiInputLabel-root": {
                      fontSize: "18px",
                    },
                  }}
                  InputProps={{
                    startAdornment: (
                      <VpnKey sx={{ mr: 1, color: codeField ? "#2196F3" : "#999" }} />
                    ),
                  }}
                />

                {/* Info box */}
                <Box
                  sx={{
                    backgroundColor: "#f5f5f5",
                    border: "1px solid #e0e0e0",
                    borderRadius: "12px",
                    py: 2,
                    px: 3,
                    mb: 3,
                    textAlign: "center",
                  }}
                >
                  <Typography variant="body2" sx={{ color: "#666", fontSize: "14px" }}>
                    💡 <strong>Numer telefonu:</strong> Wyślemy Ci kod SMS
                    <br />
                    💡 <strong>Kod dostępu:</strong> {selectedRole === "senior" 
                      ? "Otrzymany od opiekuna"
                      : "Otrzymany od pacjenta"}
                  </Typography>
                </Box>

                <Button
                  fullWidth
                  variant="contained"
                  size="large"
                  onClick={handleContinue}
                  disabled={!phoneField.trim() && !codeField.trim()}
                  sx={{
                    py: 2.5,
                    fontSize: "20px",
                    fontWeight: 500,
                    backgroundColor: roleColor,
                    "&:hover": {
                      backgroundColor: roleColor,
                      filter: "brightness(0.9)",
                    },
                    "&:disabled": {
                      backgroundColor: "#ccc",
                    },
                  }}
                >
                  Kontynuuj
                </Button>
              </>
            ) : (
              <>
                {/* Widok weryfikacji SMS */}
                <Box sx={{ textAlign: "center", mb: 3 }}>
                  <Sms sx={{ fontSize: 64, color: "#1976d2", mb: 1 }} />
                  <Typography variant="h6" sx={{ fontWeight: 600, mb: 0.5 }}>
                    Wpisz kod z SMS
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Wysłaliśmy kod na:
                  </Typography>
                  <Typography variant="body1" sx={{ fontWeight: 600, color: "#1976d2", mt: 0.5 }}>
                    {phoneField}
                  </Typography>
                </Box>

                {/* DEV MODE - pokaż wysłany kod */}
                <Box
                  sx={{
                    backgroundColor: "#e3f2fd",
                    border: "2px dashed #1976d2",
                    borderRadius: "12px",
                    py: 2,
                    px: 3,
                    mb: 3,
                    textAlign: "center",
                  }}
                >
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                    DEV MODE: Twój kod to:
                  </Typography>
                  <Typography variant="h4" sx={{ fontWeight: 700, color: "#1976d2", letterSpacing: "4px" }}>
                    {sentCode}
                  </Typography>
                </Box>

                {error && (
                  <Alert severity="error" sx={{ mb: 3, fontSize: "16px" }}>
                    {error}
                  </Alert>
                )}

                <TextField
                  fullWidth
                  label="Kod z SMS"
                  variant="outlined"
                  value={smsCodeField}
                  onChange={(e) => setSmsCodeField(e.target.value.replace(/\D/g, "").slice(0, 6))}
                  placeholder="123456"
                  type="tel"
                  autoFocus
                  inputProps={{
                    maxLength: 6,
                    style: { textAlign: "center", fontSize: "28px", letterSpacing: "12px", fontWeight: 700 },
                  }}
                  sx={{
                    mb: 3,
                    "& .MuiOutlinedInput-root": {
                      py: 1.5,
                    },
                    "& .MuiInputLabel-root": {
                      fontSize: "18px",
                    },
                  }}
                  onKeyPress={(e) => {
                    if (e.key === "Enter" && smsCodeField.length === 6) {
                      handleVerifyCode();
                    }
                  }}
                />

                <Button
                  fullWidth
                  variant="contained"
                  size="large"
                  onClick={handleVerifyCode}
                  disabled={smsCodeField.length !== 6}
                  startIcon={<CheckCircle />}
                  sx={{
                    py: 2.5,
                    fontSize: "20px",
                    fontWeight: 500,
                    backgroundColor: roleColor,
                    mb: 2,
                    "&:hover": {
                      backgroundColor: roleColor,
                      filter: "brightness(0.9)",
                    },
                    "&:disabled": {
                      backgroundColor: "#ccc",
                    },
                  }}
                >
                  Potwierdź i zaloguj
                </Button>

                <Box sx={{ display: "flex", gap: 1, justifyContent: "center" }}>
                  <Button
                    variant="text"
                    onClick={handleResendCode}
                    sx={{
                      fontSize: "14px",
                      color: "#1976d2",
                    }}
                  >
                    Wyślij ponownie
                  </Button>
                  <Typography variant="body2" sx={{ alignSelf: "center", color: "text.secondary" }}>
                    •
                  </Typography>
                  <Button
                    variant="text"
                    onClick={handleReset}
                    sx={{
                      fontSize: "14px",
                      color: "#757575",
                    }}
                  >
                    Zmień dane
                  </Button>
                </Box>
              </>
            )}
          </CardContent>
        </Card>

        <Box sx={{ textAlign: "center", mt: 3 }}>
          <Typography variant="body2" sx={{ color: "rgba(255,255,255,0.8)" }}>
            Twoje dane są bezpieczne i chronione
          </Typography>
        </Box>
      </Container>
    </Box>
  );
}