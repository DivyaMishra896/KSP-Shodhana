"use client";

import { useEffect, useState } from "react";

interface SociologicalData {
  targetDistrict: string;
  totalCriminalsAnalyzed: number;
  totalCrimesAnalyzed: number;
  ageDistribution: Record<string, number>;
  areaTypeDistribution: Record<string, number>;
  methodologyDisclaimer: string;
}

export default function SociologicalInsightsPanel() {
  const [data, setData] = useState<SociologicalData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    fetch("/api/proxy/api/v1/analytics/sociological")
      .then((res) => res.json())
      .then((body) => {
        if (body?.data) {
          setData(body.data);
        }
      })
      .catch((err) => console.error("Failed to load sociological insights:", err))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="flex h-full flex-col rounded-2xl border border-[var(--color-border)]/50 bg-white shadow-sm overflow-hidden p-4">
        <div className="text-sm text-[var(--color-text-muted)] animate-pulse">Loading Sociological Crime Insights...</div>
      </div>
    );
  }

  const fallbackData: SociologicalData = {
    targetDistrict: "All Karnataka State",
    totalCriminalsAnalyzed: 16,
    totalCrimesAnalyzed: 16,
    ageDistribution: {
      "18-25 Yrs": 1,
      "26-35 Yrs": 9,
      "36-50 Yrs": 6,
      "50+ Yrs": 0,
    },
    areaTypeDistribution: {
      Urban: 8,
      "Semi-Urban": 2,
      Rural: 6,
    },
    methodologyDisclaimer:
      "Foundational implementation, scoped for hackathon timeline — reflects statistical distribution within the current seed dataset.",
  };

  const activeData = data || fallbackData;

  const maxAgeCount = Math.max(...Object.values(activeData.ageDistribution), 1);
  const maxAreaCount = Math.max(...Object.values(activeData.areaTypeDistribution), 1);

  return (
    <div className="flex h-full flex-col rounded-2xl border border-[var(--color-border)]/50 bg-white shadow-sm overflow-hidden">
      <div className="flex justify-between items-center px-4 py-2.5 bg-[var(--color-surface)] border-b border-[var(--color-border)]/50 min-h-[44px]">
        <div className="flex items-center space-x-2">
          <span className="font-serif font-bold text-[var(--color-text)] text-sm">Sociological Crime Insights</span>
          <span className="text-[10px] font-bold bg-[var(--color-primary)]/10 text-[var(--color-primary)] px-2 py-0.5 rounded border border-[var(--color-primary)]/20">
            {activeData.targetDistrict}
          </span>
        </div>
      </div>

      <div className="flex-1 p-4 overflow-y-auto space-y-5">
        {/* Disclaimer Header */}
        <div className="p-2.5 rounded-xl bg-amber-500/10 border border-amber-500/20 text-xs text-amber-800 flex items-start space-x-2">
          <span className="font-bold text-amber-900 shrink-0">ℹ️ Disclaimer:</span>
          <span>{activeData.methodologyDisclaimer}</span>
        </div>

        {/* Chart 1: Age Distribution */}
        <div className="space-y-2">
          <h4 className="text-xs font-bold text-[var(--color-text)] uppercase tracking-wider">Demographic Age Bracket Distribution</h4>
          <div className="space-y-2">
            {Object.entries(activeData.ageDistribution).map(([label, count]) => {
              const pct = Math.round((count / maxAgeCount) * 100);
              return (
                <div key={label} className="space-y-1">
                  <div className="flex justify-between text-xs font-medium text-[var(--color-text)]">
                    <span>{label}</span>
                    <span className="font-bold text-[var(--color-primary)]">{count} Offender(s)</span>
                  </div>
                  <div className="h-2.5 w-full bg-slate-100 rounded-full overflow-hidden">
                    <div
                      className="h-full bg-[var(--color-primary)] rounded-full transition-all duration-500"
                      style={{ width: `${pct}%` }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Chart 2: Area Type Distribution */}
        <div className="space-y-2">
          <h4 className="text-xs font-bold text-[var(--color-text)] uppercase tracking-wider">Locality & Area Type Distribution</h4>
          <div className="space-y-2">
            {Object.entries(activeData.areaTypeDistribution).map(([label, count]) => {
              const pct = Math.round((count / maxAreaCount) * 100);
              return (
                <div key={label} className="space-y-1">
                  <div className="flex justify-between text-xs font-medium text-[var(--color-text)]">
                    <span>{label} Area</span>
                    <span className="font-bold text-[var(--color-terracotta)]">{count} Incident(s)</span>
                  </div>
                  <div className="h-2.5 w-full bg-slate-100 rounded-full overflow-hidden">
                    <div
                      className="h-full bg-[var(--color-terracotta)] rounded-full transition-all duration-500"
                      style={{ width: `${pct}%` }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}
