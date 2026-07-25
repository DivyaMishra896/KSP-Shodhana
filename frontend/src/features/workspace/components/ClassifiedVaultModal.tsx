"use client";

import { useState } from "react";
import { useAuthStore } from "@/features/auth/useAuthStore";
import { apiClient } from "@/lib/api-client";

interface ClassifiedVaultModalProps {
  isOpen: boolean;
  onClose: () => void;
}

interface VaultData {
  classificationLevel: string;
  accessGrantedRole: string;
  firNumber: string;
  unredactedLeadOfficer: string;
  unredactedAadhaarNo: string;
  unredactedPhoneNo: string;
  unredactedOffshoreAccount: string;
  vaultClearanceTimestamp: number;
}

export default function ClassifiedVaultModal({ isOpen, onClose }: ClassifiedVaultModalProps) {
  const { currentRole, jwtToken } = useAuthStore();
  const [data, setData] = useState<VaultData | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(false);

  if (!isOpen) return null;

  const handleTestVaultAccess = async () => {
    setLoading(true);
    setError(null);
    setData(null);

    try {
      // Direct API proxy fetch using current role's Bearer JWT
      const res = await fetch("/api/proxy/api/v1/admin/unredacted-dossier/1", {
        headers: {
          Authorization: `Bearer ${jwtToken}`,
          "Content-Type": "application/json",
        },
      });

      if (!res.ok) {
        if (res.status === 403) {
          setError("HTTP 403 FORBIDDEN — Cryptographic JJWT signature verification succeeded, but access was DENIED because your current role does not possess ROLE_SUPERINTENDENT clearance.");
        } else {
          setError(`HTTP ${res.status} Error accessing classified vault.`);
        }
        return;
      }

      const body = await res.json();
      if (body?.data) {
        setData(body.data);
      } else {
        setError("Invalid response format from admin vault.");
      }
    } catch (err: any) {
      setError(err?.message || "Failed to reach admin vault endpoint.");
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
              Classified Intelligence PII Vault
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

          <p className="text-xs text-[var(--color-text-muted)] leading-relaxed">
            Test the live Spring Security RBAC gate at <code className="bg-black/5 px-1 py-0.5 rounded text-[11px] font-mono text-[var(--color-primary)]">GET /api/v1/admin/unredacted-dossier/1</code>. This route is strictly protected and requires <strong className="text-[var(--color-text)]">ROLE_SUPERINTENDENT</strong>.
          </p>

          <button
            onClick={handleTestVaultAccess}
            disabled={loading}
            className="w-full py-2.5 px-4 rounded-xl bg-[var(--color-primary)] hover:bg-[var(--color-primary-hover)] text-white text-xs font-bold shadow-xs transition-all active:scale-[0.98] cursor-pointer disabled:opacity-50"
          >
            {loading ? "Verifying JJWT Signature & Clearance..." : "Test Vault Access Endpoint"}
          </button>

          {/* Access Granted Box */}
          {data && (
            <div className="p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-xs space-y-2 animate-fade-in">
              <div className="flex items-center space-x-2 text-emerald-800 font-extrabold uppercase tracking-wider text-[11px]">
                <span>✅ ACCESS GRANTED — {data.accessGrantedRole}</span>
              </div>
              <div className="grid grid-cols-2 gap-2 text-[11px] pt-1 border-t border-emerald-500/20 font-medium">
                <div><span className="text-slate-500">Unmasked Aadhaar:</span> <strong className="text-slate-900">{data.unredactedAadhaarNo}</strong></div>
                <div><span className="text-slate-500">Unmasked Phone:</span> <strong className="text-slate-900">{data.unredactedPhoneNo}</strong></div>
                <div className="col-span-2"><span className="text-slate-500">Offshore Account:</span> <strong className="text-slate-900 font-mono">{data.unredactedOffshoreAccount}</strong></div>
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
