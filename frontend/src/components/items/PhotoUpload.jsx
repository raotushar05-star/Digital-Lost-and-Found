import React, { useRef, useState } from "react";

/**
 * Simple file-select + preview + upload button. The parent supplies an
 * `onUpload(file)` handler (calling the relevant lost/found photo endpoint)
 * so this component stays transport-agnostic.
 */
export default function PhotoUpload({ onUpload, uploading, uploadedPhotos = [] }) {
  const inputRef = useRef(null);
  const [preview, setPreview] = useState(null);
  const [selectedFile, setSelectedFile] = useState(null);

  const handleSelect = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setSelectedFile(file);
    setPreview(URL.createObjectURL(file));
  };

  const handleUploadClick = async () => {
    if (!selectedFile) return;
    await onUpload(selectedFile);
    setSelectedFile(null);
    setPreview(null);
    if (inputRef.current) inputRef.current.value = "";
  };

  return (
    <div>
      <div className="d-flex flex-wrap gap-2 mb-2">
        {uploadedPhotos.map((p) => (
          <img
            key={p.photoId}
            src={p.fileUrl}
            alt="Uploaded"
            style={{ width: 84, height: 84, objectFit: "cover", borderRadius: "var(--radius-sm)", border: "1px solid var(--line)" }}
          />
        ))}
        {preview && (
          <img
            src={preview}
            alt="Preview"
            style={{ width: 84, height: 84, objectFit: "cover", borderRadius: "var(--radius-sm)", border: "2px solid var(--brass)" }}
          />
        )}
      </div>
      <div className="d-flex gap-2">
        <input
          ref={inputRef}
          type="file"
          accept="image/png,image/jpeg,image/webp"
          className="form-control form-control-sm"
          onChange={handleSelect}
        />
        <button
          type="button"
          className="btn btn-sm btn-outline-primary text-nowrap"
          disabled={!selectedFile || uploading}
          onClick={handleUploadClick}
        >
          {uploading ? "Uploading…" : "Upload photo"}
        </button>
      </div>
      <div className="text-muted-soft mt-1" style={{ fontSize: "0.75rem" }}>
        JPG, PNG or WEBP, up to 10MB.
      </div>
    </div>
  );
}
