"use client";

import { createContext, useContext, useState, useEffect, useCallback } from "react";
import { LATEST_VERSION, type Version } from "@/data/versions";

interface VersionContextValue {
  version: Version;
  setVersion: (v: Version) => void;
}

const VersionContext = createContext<VersionContextValue>({
  version: LATEST_VERSION as Version,
  setVersion: () => {},
});

export function VersionProvider({ children }: { children: React.ReactNode }) {
  const [version, setVersionState] = useState<Version>(LATEST_VERSION as Version);
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    try {
      const stored = localStorage.getItem("jp-version");
      if (stored) {
        setVersionState(stored as Version);
      }
    } catch {
      // ignore
    }
  }, []);

  const setVersion = useCallback((v: Version) => {
    setVersionState(v);
    try {
      localStorage.setItem("jp-version", v);
    } catch {
      // ignore
    }
  }, []);

  // Avoid hydration mismatch by not rendering version-dependent content until mounted
  // The context still provides LATEST_VERSION on server, which is the default
  return (
    <VersionContext.Provider value={{ version: mounted ? version : (LATEST_VERSION as Version), setVersion }}>
      {children}
    </VersionContext.Provider>
  );
}

export function useVersion() {
  return useContext(VersionContext);
}
