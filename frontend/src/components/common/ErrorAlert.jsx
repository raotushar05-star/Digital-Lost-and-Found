import React from "react";
import FlashAlert from "./FlashAlert.jsx";

export default function ErrorAlert({ message }) {
  return <FlashAlert message={message} variant="danger" />;
}