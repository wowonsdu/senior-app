import { createBrowserRouter, redirect } from "react-router";
import { Root } from "./components/Root";
import { Login } from "./components/Login";
import { RoleSelection } from "./components/RoleSelection";
import { Home } from "./components/Home";
import { History } from "./components/History";
import { Agents } from "./components/Agents";
import { Settings } from "./components/Settings";
import { PatientSelection } from "./components/PatientSelection";
import { CaregiverDashboard } from "./components/CaregiverDashboard";
import { AlertSettings } from "./components/AlertSettings";

export const router = createBrowserRouter([
  {
    path: "/",
    Component: RoleSelection,
  },
  {
    path: "/role-selection",
    loader: () => redirect("/"),
  },
  {
    path: "/login",
    Component: Login,
  },
  {
    path: "/home",
    Component: Root,
    children: [
      { index: true, Component: Home },
      { path: "caregiver-dashboard", Component: CaregiverDashboard },
      { path: "select-patient", Component: PatientSelection },
      { path: "historia", Component: History },
      { path: "agenci", Component: Agents },
      { path: "ustawienia", Component: Settings },
      { path: "ustawienia/alerty", Component: AlertSettings },
    ],
  },
]);