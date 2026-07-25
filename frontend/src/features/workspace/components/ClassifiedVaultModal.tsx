"use client";

import { useState } from "react";
import { useAuthStore } from "@/features/auth/useAuthStore";

interface ClassifiedVaultModalProps {
  isOpen: boolean;
  onClose: () => void;
}

interface VaultData {
  crimeId: number;
  classificationLevel: string;
  accessGrantedRole: string;
  firNumber: string;
  crimeType: string;
  district: string;
  policeStation: string;
  unredactedLeadOfficer: string;
  unredactedAadhaarNo: string;
  unredactedPhoneNo: string;
  unredactedOffshoreAccount: string;
  vaultClearanceTimestamp: number;
}

interface PurgeData {
  purgedCrimeId: number;
  purgedFirNumber: string;
  status: string;
  authorizedBy: string;
  timestamp: number;
}

export default function ClassifiedVaultModal({ isOpen, onClose }: ClassifiedVaultModalProps) {
  const { currentRole, jwtToken } = useAuthStore();
  const [data, setData] = useState<VaultData | null>(null);
  const [purgeData, setPurgeData] = useState<PurgeData | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [selectedCrimeId, setSelectedCrimeId] = useState<number>(1);

  if (!isOpen) return null;

  const handleTestVaultAccess = async () => {
    setLoading(true);
    setError(null);
    setData(null);
    setPurgeData(null);

    try {
      const res = await fetch(`/api/proxy/api/v1/admin/unredacted-dossier/${selectedCrimeId}`, {
        headers: {
          Authorization: `Bearer ${jwtToken}`,
          "Content-Type": "application/json",
        },
      });

      if (!res.ok) {
        if (res.status === 403) {
          setError("HTTP 403 FORBIDDEN — Cryptographic JJWT signature verification succeeded, but access was DENIED because your current role does not possess ROLE_SUPERINTENDENT clearance.");
        } else {
          setError(`HTTP ${res.status} Error accessing classified vault for Crime ID ${selectedCrimeId}.`);
        }
        return;
      }

      const body = await res.json();
      if (body?.data) {
        setData(body.data);
      } else {
        setError("Invalid response format from admin vault.");
      }
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : "Failed to reach admin vault endpoint.";
      setError(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  const handleTestPurgeCase = async () => {
    setLoading(true);
    setError(null);
    setData(null);
    setPurgeData(null);

    try {
      const res = await fetch(`/api/proxy/api/v1/admin/purge-case/${selectedCrimeId}`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${jwtToken}`,
          "Content-Type": "application/json",
        },
      });

      if (!res.ok) {
        if (res.status === 403) {
          setError("HTTP 403 FORBIDDEN — Administrative Case Purge DENIED. Requires ROLE_SUPERINTENDENT clearance.");
        } else {
          setError(`HTTP ${res.status} Error purging case ID ${selectedCrimeId}.`);
        }
        return;
      }

      const body = await res.json();
      if (body?.data) {
        setPurgeData(body.data);
      } else {
        setError("Invalid response format from case purge endpoint.");
      }
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : "Failed to execute case purge command.";
      setError(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4 animate-fade-in">
      <div className="w-full max-w-lg rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="flex justify-between items-center px-6 py-4 border-b border-[var(--color-border)]/50 bg-[#2C2C24] text-white">
          <div className="flex items-center space-x-2">
            <span className="text-base">🔒</span>
            <span className="font-serif font-extrabold text-sm tracking-wide text-[#F0EBE5]">
              Classified Intelligence & Admin Vault
            </span>
          </div>
          <button
            onClick={onClose}
            className="text-[#949484] hover:text-white transition-colors cursor-pointer text-lg font-bold"
          >
            ✕
          </button>
        </div>

        {/* Content Body */}
        <div className="p-6 space-y-4">
          <div className="p-3 rounded-xl bg-slate-100 border border-slate-200 text-xs text-slate-700 flex justify-between items-center">
            <div>
              <span className="font-bold block text-[11px] text-slate-500 uppercase tracking-wider">Active Role & JWT Token</span>
              <span className="font-extrabold text-[var(--color-primary)]">{currentRole}</span>
            </div>
            <span className="text-[10px] font-bold bg-emerald-100 text-emerald-800 px-2 py-0.5 rounded border border-emerald-300">
              JWT Signed ✓
            </span>
          </div>

          <div className="flex items-center justify-between gap-3 text-xs">
            <label className="font-bold text-[var(--color-text)] shrink-0">Select Crime Record ID:</label>
            <select
              value={selectedCrimeId}
              onChange={(e) => setSelectedCrimeId(Number(e.target.value))}
              className="flex-1 py-1 px-2.5 rounded-lg border border-[var(--color-border)] bg-white text-xs font-semibold"
            >
              <option value={1}>ID 1 — FIR KA-CR-2024-001 (Bengaluru Robbery)</option>
              <option value={2}>ID 2 — FIR KA-CR-2024-002 (Mysuru Cyber Heist)</option>
              <option value={3}>ID 3 — FIR KA-CR-2024-003 (Mangaluru Narcotics)</option>
              <option value={16}>ID 16 — FIR KA-CR-2024-016 (Hubballi Theft)</option>
            </select>
          </div>

          <div className="grid grid-cols-2 gap-2">
            <button
              onClick={handleTestVaultAccess}
              disabled={loading}
              className="py-2.5 px-3 rounded-xl bg-[var(--color-primary)] hover:bg-[var(--color-primary-hover)] text-white text-xs font-bold shadow-xs transition-all active:scale-[0.98] cursor-pointer disabled:opacity-50"
            >
              {loading ? "Verifying..." : "🔓 Unmask Vault PII"}
            </button>
            <button
              onClick={handleTestPurgeCase}
              disabled={loading}
              className="py-2.5 px-3 rounded-xl bg-red-700 hover:bg-red-800 text-white text-xs font-bold shadow-xs transition-all active:scale-[0.98] cursor-pointer disabled:opacity-50"
            >
              {loading ? "Processing..." : "⚠️ Purge Case Record"}
            </button>
          </div>

          {/* Access Granted Box */}
          {data && (
            <div className="p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-xs space-y-2 animate-fade-in">
              <div className="flex items-center justify-between text-emerald-800 font-extrabold uppercase tracking-wider text-[11px]">
                <span>✅ ACCESS GRANTED — {data.accessGrantedRole}</span>
                <span className="text-[10px] bg-emerald-200 px-1.5 py-0.5 rounded font-mono">{data.firNumber}</span>
              </div>
              <div className="grid grid-cols-2 gap-2 text-[11px] pt-1 border-t border-emerald-500/20 font-medium text-slate-800">
                <div><span className="text-slate-500">Crime Type:</span> <strong>{data.crimeType}</strong></div>
                <div><span className="text-slate-500">District:</span> <strong>{data.district}</strong></div>
                <div><span className="text-slate-500">Unmasked Aadhaar:</span> <strong className="font-mono text-slate-900">{data.unredactedAadhaarNo}</strong></div>
                <div><span className="text-slate-500">Unmasked Phone:</span> <strong className="font-mono text-slate-900">{data.unredactedPhoneNo}</strong></div>
                <div className="col-span-2"><span className="text-slate-500">Lead Officer:</span> <strong>{data.unredactedLeadOfficer}</strong></div>
                <div className="col-span-2"><span className="text-slate-500">Swiss Offshore Account:</span> <strong className="text-slate-900 font-mono">{data.unredactedOffshoreAccount}</strong></div>
              </div>
            </div>
          )}

          {/* Case Purge Success Box */}
          {purgeData && (
            <div className="p-4 rounded-xl bg-amber-500/10 border border-amber-500/30 text-xs space-y-2 animate-fade-in">
              <div className="flex items-center justify-between text-amber-900 font-extrabold uppercase tracking-wider text-[11px]">
                <span>🚨 CASE PURGED & SHA-256 LOGGED</span>
                <span className="text-[10px] bg-amber-200 px-1.5 py-0.5 rounded font-mono">{purgeData.purgedFirNumber}</span>
              </div>
              <div className="text-[11px] pt-1 border-t border-amber-500/20 space-y-1 text-slate-800">
                <p>Purged Crime ID: <strong className="font-mono">{purgeData.purgedCrimeId}</strong></p>
                <p>Authorization: <strong className="text-amber-900">{purgeData.authorizedBy}</strong></p>
                <p className="text-[10px] text-slate-500 italic">Record removed from JPA Repository and logged into SHA-256 WORM Audit Ledger.</p>
              </div>
            </div>
          )}

          {/* Access Denied Box */}
          {error && (
            <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/30 text-xs text-red-800 space-y-1 animate-fade-in">
              <div className="font-extrabold uppercase tracking-wider text-[11px] text-red-900 flex items-center space-x-1">
                <span>🚫 ACCESS DENIED</span>
              </div>
              <p className="leading-relaxed">{error}</p>
              <p className="text-[10px] text-red-700 italic pt-1">
                Tip: Switch your role to <strong>ROLE_SUPERINTENDENT (Vault Admin)</strong> in the top header dropdown and re-test!
              </p>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="px-6 py-3 bg-[var(--color-surface)] border-t border-[var(--color-border)]/50 flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-1.5 rounded-lg bg-slate-200 hover:bg-slate-300 text-slate-800 text-xs font-bold cursor-pointer transition-colors"
          >
            Close Vault Test
          </button>
        </div>
      </div>
    </div>
  );
}
