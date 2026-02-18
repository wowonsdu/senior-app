import { useState, useEffect } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  IconButton,
  Box,
  Typography,
  Fab,
} from "@mui/material";
import { Close, Mic, MicOff, Save } from "@mui/icons-material";

type MeasurementType = "cukier" | "insulina" | "ciśnienie" | "tętno";

interface VoiceInputModalProps {
  type: MeasurementType;
  onClose: () => void;
  onSave: (value: string) => void;
}

export function VoiceInputModal({ type, onClose, onSave }: VoiceInputModalProps) {
  const [isListening, setIsListening] = useState(false);
  const [transcript, setTranscript] = useState("");
  const [recognition, setRecognition] = useState<SpeechRecognition | null>(null);

  useEffect(() => {
    if ('webkitSpeechRecognition' in window || 'SpeechRecognition' in window) {
      const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
      const recognitionInstance = new SpeechRecognition();
      recognitionInstance.continuous = false;
      recognitionInstance.interimResults = true;
      recognitionInstance.lang = 'pl-PL';

      recognitionInstance.onresult = (event: any) => {
        const current = event.resultIndex;
        const transcriptText = event.results[current][0].transcript;
        setTranscript(transcriptText);
      };

      recognitionInstance.onerror = (event: any) => {
        console.error('Speech recognition error:', event.error);
        setIsListening(false);
      };

      recognitionInstance.onend = () => {
        setIsListening(false);
      };

      setRecognition(recognitionInstance);
    }

    return () => {
      if (recognition) {
        recognition.stop();
      }
    };
  }, []);

  const toggleListening = () => {
    if (!recognition) {
      alert('Przeglądarka nie obsługuje rozpoznawania mowy. Wpisz wartość ręcznie.');
      return;
    }

    if (isListening) {
      recognition.stop();
      setIsListening(false);
    } else {
      recognition.start();
      setIsListening(true);
    }
  };

  const handleSave = () => {
    if (transcript.trim()) {
      onSave(transcript.trim());
    }
  };

  const getLabel = () => {
    switch (type) {
      case "cukier":
        return "Poziom cukru";
      case "insulina":
        return "Dawka insuliny";
      case "ciśnienie":
        return "Ciśnienie krwi";
      case "tętno":
        return "Tętno";
    }
  };

  const getPlaceholder = () => {
    switch (type) {
      case "cukier":
        return "np. 120";
      case "insulina":
        return "np. 10 jednostek";
      case "ciśnienie":
        return "np. 120 na 80";
      case "tętno":
        return "np. 75";
    }
  };

  return (
    <Dialog 
      open={true} 
      onClose={onClose} 
      maxWidth="sm" 
      fullWidth
      PaperProps={{
        sx: {
          borderRadius: "16px",
          m: 2,
        }
      }}
    >
      <DialogTitle sx={{ 
        display: "flex", 
        justifyContent: "space-between", 
        alignItems: "center",
        fontSize: "1.5rem",
        fontWeight: 500,
      }}>
        {getLabel()}
        <IconButton onClick={onClose} edge="end">
          <Close />
        </IconButton>
      </DialogTitle>

      <DialogContent sx={{ pt: 2 }}>
        <TextField
          fullWidth
          variant="outlined"
          value={transcript}
          onChange={(e) => setTranscript(e.target.value)}
          placeholder={getPlaceholder()}
          sx={{
            mb: 4,
            "& .MuiOutlinedInput-root": {
              fontSize: "1.5rem",
            },
            "& .MuiOutlinedInput-input": {
              py: 2,
            }
          }}
        />

        <Box sx={{ 
          display: "flex", 
          flexDirection: "column", 
          alignItems: "center", 
          gap: 3,
          mb: 2,
        }}>
          <Fab
            color={isListening ? "error" : "default"}
            onClick={toggleListening}
            sx={{
              width: 120,
              height: 120,
              boxShadow: "0 4px 8px rgba(0,0,0,0.3)",
              animation: isListening ? "pulse 1.5s infinite" : "none",
              "@keyframes pulse": {
                "0%, 100%": {
                  transform: "scale(1)",
                },
                "50%": {
                  transform: "scale(1.05)",
                },
              },
            }}
          >
            {isListening ? (
              <MicOff sx={{ fontSize: 56 }} />
            ) : (
              <Mic sx={{ fontSize: 56 }} />
            )}
          </Fab>

          {isListening && (
            <Box
              sx={{
                backgroundColor: "#f44336",
                color: "white",
                px: 3,
                py: 1.5,
                borderRadius: "24px",
                display: "flex",
                alignItems: "center",
                gap: 1,
              }}
            >
              <Typography variant="h6" sx={{ fontWeight: 500 }}>
                🎤 Słucham...
              </Typography>
            </Box>
          )}
        </Box>
      </DialogContent>

      <DialogActions sx={{ px: 3, pb: 3 }}>
        <Button
          fullWidth
          variant="contained"
          color="success"
          size="large"
          startIcon={<Save />}
          onClick={handleSave}
          disabled={!transcript.trim()}
          sx={{
            py: 1.5,
            fontSize: "1.25rem",
            fontWeight: 500,
            textTransform: "none",
            boxShadow: "0 2px 4px rgba(0,0,0,0.2)",
          }}
        >
          Zapisz
        </Button>
      </DialogActions>
    </Dialog>
  );
}