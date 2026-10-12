export function saveBlobResponse(response, fallbackName) {
  const disposition = response.headers?.["content-disposition"] || "";
  const match = /filename\*?=(?:UTF-8'')?"?([^";]+)"?/i.exec(disposition);
  const filename = match ? decodeURIComponent(match[1]) : fallbackName;
  const url = URL.createObjectURL(new Blob([response.data], { type: "application/pdf" }));
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}

// With responseType "blob", error bodies arrive as a Blob; read the JSON message out of it.
export async function blobErrorMessage(err) {
  try {
    const data = err?.response?.data;
    if (data instanceof Blob) {
      const parsed = JSON.parse(await data.text());
      if (parsed.message) return parsed.message;
    }
  } catch {
    /* fall through */
  }
  return err?.message || "Could not download the PDF.";
}