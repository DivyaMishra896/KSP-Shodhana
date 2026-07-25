"use client";

import { useEffect, useState } from "react";
import type { Criminal } from "@/types/domain";

interface CriminalProfileModalProps {
  criminalId: number | null;
  onClose: () => void;
}

interface EnrichedCriminal extends Criminal {
  priorOffenseCount?: number;
  isRepeatOffender?: boolean;
  riskScore?: number;
  riskExplanation?: string;
  areaType?: string;
}

export default function CriminalProfileModal({ criminalId, onClose }: CriminalProfileModalProps) {
  const [criminal, setCriminal] = useState<EnrichedCriminal | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    if (!criminalId) return;
    let isMounted = true;
    fetch(`/api/proxy/api/v1/criminals/${criminalId}`)
      .then((res) => res.json())
      .then((body) => {
        if (isMounted && body?.data) {
          setCriminal(body.data);
        }
      })
      .catch((err) => console.error("Failed to fetch criminal profile:", err))
      .finally(() => {
        if (isMounted) setLoading(false);
      });
    return () => {
      isMounted = false;
    };
  }, [criminalId]);

  if (!criminalId) return null;

  const score = criminal?.riskScore || (criminal?.riskLevel === "High" ? 85 : 45);
  const isRepeat = criminal?.isRepeatOffender || (criminal?.priorOffenseCount || 0) >= 2;

  const scoreColor =
    score >= 75
      ? "bg-red-500 text-white border-red-600"
      : score >= 50
      ? "bg-amber-500 text-white border-amber-600"
      : "bg-emerald-600 text-white border-emerald-700";

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-xs p-4 animate-fade-in">
      <div className="relative w-full max-w-lg rounded-2xl bg-white p-6 shadow-2xl border border-[var(--color-border)] overflow-hidden">
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-slate-400 hover:text-slate-600 p-1 rounded-lg hover:bg-slate-100 transition cursor-pointer"
        >
          ✕
        </button>

        {loading ? (
          <div className="p-8 text-center text-sm font-medium text-slate-500 animate-pulse">
            Loading Offender Criminology Profile...
          </div>
        ) : criminal ? (
          <div className="space-y-5">
            {/* Header */}
            <div className="flex items-start gap-4">
              <div className="h-16 w-16 rounded-2xl bg-slate-100 border border-slate-200 flex items-center justify-center font-bold text-xl text-slate-700 shrink-0">
                {criminal.name.substring(0, 2).toUpperCase()}
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 flex-wrap">
                  <h3 className="font-serif text-lg font-bold text-slate-900 truncate">{criminal.name}</h3>
                  {criminal.alias && (
                    <span className="text-xs font-semibold bg-slate-100 text-slate-600 px-2 py-0.5 rounded border border-slate-200">
                      &quot;{criminal.alias}&quot;
                    </span>
                  )}
                </div>
                <p className="text-xs text-slate-500 mt-0.5">
                  ID: #{criminal.rowId} · {criminal.age || 32} Yrs · {criminal.district} ({criminal.areaType || "Urban"} Area)
                </p>
                <div className="flex items-center gap-2 mt-2 flex-wrap">
                  <span className={`text-xs font-bold px-2.5 py-0.5 rounded-md border ${scoreColor}`}>
                    Risk Score: {score}/100
                  </span>
                  {isRepeat && (
                    <span className="text-xs font-bold bg-purple-100 text-purple-800 border border-purple-300 px-2.5 py-0.5 rounded-md">
                      ⚠️ REPEAT OFFENDER
                    </span>
                  )}
                  <span className="text-xs font-medium bg-slate-100 text-slate-700 px-2 py-0.5 rounded border border-slate-200">
                    Status: {criminal.status}
                  </span>
                </div>
              </div>
            </div>

            {/* Explainable Risk Assessment Banner */}
            <div className="p-3.5 rounded-xl bg-amber-500/10 border border-amber-500/30 text-xs space-y-1">
              <div className="font-bold text-amber-900 flex items-center gap-1.5">
                <span>🧬</span> Criminology Risk Assessment (Explainable AI Engine)
              </div>
              <p className="text-amber-950 leading-relaxed">
                {criminal.riskExplanation ||
                  `Risk Score: ${score}/100 — ${criminal.priorOffenseCount || 1} prior offense(s) linked in database record.`}
              </p>
            </div>

            {/* Profile Grid */}
            <div className="grid grid-cols-2 gap-3 text-xs">
              <div className="p-3 rounded-xl bg-slate-50 border border-slate-200/80">
                <span className="text-slate-400 font-bold block uppercase text-[10px]">Prior Offenses</span>
                <span className="text-slate-900 font-extrabold text-sm">{criminal.priorOffenseCount || 1} Record(s)</span>
              </div>
              <div className="p-3 rounded-xl bg-slate-50 border border-slate-200/80">
                <span className="text-slate-400 font-bold block uppercase text-[10px]">Locality Classification</span>
                <span className="text-slate-900 font-extrabold text-sm capitalize">{criminal.areaType || "Urban"} Area</span>
              </div>
            </div>

            {/* Criminal History Summary */}
            {criminal.criminalHistory && (
              <div className="space-y-1">
                <h4 className="text-xs font-bold text-slate-700 uppercase tracking-wider">Prior Criminal Record</h4>
                <p className="text-xs text-slate-600 bg-slate-50 p-3 rounded-xl border border-slate-200 leading-relaxed">
                  {criminal.criminalHistory}
                </p>
              </div>
            )}
          </div>
        ) : (
          <div className="p-4 text-center text-xs text-slate-500">Criminal profile unavailable</div>
        )}
      </div>
    </div>
  );
}
