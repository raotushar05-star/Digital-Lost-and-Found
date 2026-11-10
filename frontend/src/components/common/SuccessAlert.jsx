import React from "react";
import FlashAlert from "./FlashAlert.jsx";

export default function SuccessAlert({ message }) {
  return <FlashAlert message={message} variant="success" />;
}