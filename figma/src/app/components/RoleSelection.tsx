import { useState } from "react";
import { useNavigate } from "react-router";
import {
  Box,
  Container,
  Typography,
  Card,
  CardContent,
} from "@mui/material";
import { Person, Visibility, HealthAndSafety } from "@mui/icons-material";

export function RoleSelection() {
  const navigate = useNavigate();

  const handleSeniorMode = () => {
    // Zapisz wybraną rolę i przekieruj do logowania
    localStorage.setItem("healthAppSelectedRole", "senior");
    navigate("/login");
  };

  const handleAgentMode = () => {
    // Zapisz wybraną rolę i przekieruj do logowania
    localStorage.setItem("healthAppSelectedRole", "agent");
    navigate("/login");
  };

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
      }}
    >
      <Container maxWidth="sm">
        <Box sx={{ textAlign: "center", mb: 5 }}>
          <HealthAndSafety
            sx={{
              fontSize: 140,
              color: "white",
              mb: 3,
              filter: "drop-shadow(0 4px 8px rgba(0,0,0,0.3))",
            }}
          />
          <Typography
            variant="h3"
            component="h1"
            sx={{
              color: "white",
              fontWeight: 700,
              mb: 2,
              textShadow: "0 2px 4px rgba(0,0,0,0.3)",
            }}
          >
            Zdrowie dla Seniorów
          </Typography>
          <Typography
            variant="h5"
            sx={{
              color: "rgba(255,255,255,0.95)",
              fontWeight: 400,
              mb: 4,
            }}
          >
            Kim jesteś?
          </Typography>
        </Box>

        <Box sx={{ display: "flex", flexDirection: "column", gap: 3 }}>
          <Card
            sx={{
              cursor: "pointer",
              transition: "all 0.3s",
              "&:hover": {
                transform: "translateY(-6px)",
                boxShadow: "0 16px 40px rgba(0,0,0,0.4)",
              },
              "&:active": {
                transform: "translateY(-3px)",
              },
              boxShadow: "0 8px 24px rgba(0,0,0,0.3)",
              borderRadius: "20px",
            }}
            onClick={handleSeniorMode}
          >
            <CardContent
              sx={{
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                gap: 2.5,
                py: 6,
                backgroundColor: "#4CAF50",
                color: "white",
                "&:last-child": { pb: 6 },
              }}
            >
              <Person sx={{ fontSize: 110 }} />
              <Box sx={{ textAlign: "center" }}>
                <Typography variant="h3" component="div" sx={{ fontWeight: 700, mb: 1.5 }}>
                  Pacjent
                </Typography>
                <Typography variant="h6" sx={{ fontSize: "20px", opacity: 0.95, fontWeight: 400 }}>
                  Będę dodawać swoje pomiary zdrowotne
                </Typography>
              </Box>
            </CardContent>
          </Card>

          <Card
            sx={{
              cursor: "pointer",
              transition: "all 0.3s",
              "&:hover": {
                transform: "translateY(-6px)",
                boxShadow: "0 16px 40px rgba(0,0,0,0.4)",
              },
              "&:active": {
                transform: "translateY(-3px)",
              },
              boxShadow: "0 8px 24px rgba(0,0,0,0.3)",
              borderRadius: "20px",
            }}
            onClick={handleAgentMode}
          >
            <CardContent
              sx={{
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                gap: 2.5,
                py: 6,
                backgroundColor: "#2196F3",
                color: "white",
                "&:last-child": { pb: 6 },
              }}
            >
              <Visibility sx={{ fontSize: 110 }} />
              <Box sx={{ textAlign: "center" }}>
                <Typography variant="h3" component="div" sx={{ fontWeight: 700, mb: 1.5 }}>
                  Opiekun
                </Typography>
                <Typography variant="h6" sx={{ fontSize: "20px", opacity: 0.95, fontWeight: 400 }}>
                  Będę monitorować zdrowie pacjenta
                </Typography>
              </Box>
            </CardContent>
          </Card>
        </Box>

        <Box sx={{ textAlign: "center", mt: 5 }}>
          <Typography variant="body1" sx={{ color: "rgba(255,255,255,0.8)" }}>
            Aplikacja stworzona z myślą o seniorach
          </Typography>
          <Typography variant="body2" sx={{ color: "rgba(255,255,255,0.6)", mt: 0.5 }}>
            Wersja 1.0.0 • 2026
          </Typography>
        </Box>
      </Container>
    </Box>
  );
}