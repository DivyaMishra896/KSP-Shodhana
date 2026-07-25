import { useState } from "react";
import { useWorkspaceStore } from "@/stores/workspaceStore";
import { RoleSelector } from "@/components/security/RoleSelector";
import ClassifiedVaultModal from "@/features/workspace/components/ClassifiedVaultModal";
import type { VisualizationType } from "@/types/domain";

export default function WorkspaceHeader() {
  const { clearWorkspace, isQuerying, setActiveTab, activeVisualizations, setActiveVisualizations } = useWorkspaceStore();
  const [isVaultModalOpen, setIsVaultModalOpen] = useState(false);

  const handleHomeClick = () => {
    setActiveTab("dashboard");
    clearWorkspace();
  };

  const toggleVis = (type: VisualizationType) => {
    if (activeVisualizations.includes(type)) {
      setActiveVisualizations(activeVisualizations.filter((t) => t !== type));
    } else {
      setActiveVisualizations([...activeVisualizations, type]);
    }
  };

  return (
    <>
      <header className="flex h-[64px] items-center justify-between px-4 sm:px-6 border-b border-[var(--color-border)]/50 bg-[var(--color-surface)] shrink-0 min-w-0 gap-2">
        {/* Title + Subtitle */}
        <button
          onClick={handleHomeClick}
          aria-label="Return to Home Dashboard"
          title="Return to Home Dashboard"
          className="min-w-0 shrink text-left cursor-pointer group flex flex-col justify-center"
        >
          <h1 className="font-serif text-sm sm:text-base font-extrabold tracking-tight text-[var(--color-text)] leading-tight truncate group-hover:text-[var(--color-primary)] transition-colors">
            KSP Shodhana Workspace
          </h1>
          <p className="text-[10px] font-semibold text-[var(--color-text-muted)] mt-0.5 truncate hidden md:block">
            AI Crime Intelligence & Multi-Hop Network Workspace
          </p>
        </button>

        {/* Quick Visualization Toggles - Styled to match App Design System */}
        <div className="hidden xl:flex items-center gap-1 bg-[#F3EFE6] p-1 rounded-full border border-[var(--color-border)] shrink-0">
          <button
            onClick={() => toggleVis("network_graph")}
            className={`flex items-center gap-1.5 px-3 py-1 text-[11px] font-semibold rounded-full transition-all duration-200 cursor-pointer ${
              activeVisualizations.includes("network_graph")
                ? "bg-[var(--color-primary)] text-white shadow-xs font-bold"
                : "text-[var(--color-text-muted)] hover:text-[var(--color-text)] hover:bg-black/5"
            }`}
          >
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" className="w-3.5 h-3.5 shrink-0">
              <path d="M13 4.5a2.5 2.5 0 1 1-5 0 2.5 2.5 0 0 1 5 0ZM15 10a2.5 2.5 0 1 1-5 0 2.5 2.5 0 0 1 5 0ZM7 15.5a2.5 2.5 0 1 1-5 0 2.5 2.5 0 0 1 5 0Z" />
            </svg>
            <span>Network</span>
          </button>
          <button
            onClick={() => toggleVis("heatmap")}
            className={`flex items-center gap-1.5 px-3 py-1 text-[11px] font-semibold rounded-full transition-all duration-200 cursor-pointer ${
              activeVisualizations.includes("heatmap")
                ? "bg-[var(--color-primary)] text-white shadow-xs font-bold"
                : "text-[var(--color-text-muted)] hover:text-[var(--color-text)] hover:bg-black/5"
            }`}
          >
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" className="w-3.5 h-3.5 shrink-0">
              <path fillRule="evenodd" d="M9.69 18.933l.003.001C9.89 19.02 10 19 10 19s.11.02.308-.066l.002-.001.006-.003.018-.008a5.741 5.741 0 00.281-.14c.186-.096.446-.24.757-.433a11.168 11.168 0 002.37-2.023c.96-1.077 1.83-2.39 2.18-3.793.364-1.46.126-2.907-.638-4.048-.766-1.143-1.956-1.89-3.284-2.184A6.29 6.29 0 0010 6.25c-.945 0-1.854.218-2.67.611-1.328.294-2.518 1.04-3.284 2.184-.764 1.141-1.002 2.588-.638 4.048.35 1.403 1.22 2.716 2.18 3.793a11.168 11.168 0 002.37 2.023c.31.193.57.337.757.433l.281.14.018.008.006.003zM10 11.25a1.25 1.25 0 100-2.5 1.25 1.25 0 000 2.5z" clipRule="evenodd" />
            </svg>
            <span>Heatmap</span>
          </button>
          <button
            onClick={() => toggleVis("sociological_insights")}
            className={`flex items-center gap-1.5 px-3 py-1 text-[11px] font-semibold rounded-full transition-all duration-200 cursor-pointer ${
              activeVisualizations.includes("sociological_insights")
                ? "bg-[var(--color-primary)] text-white shadow-xs font-bold"
                : "text-[var(--color-text-muted)] hover:text-[var(--color-text)] hover:bg-black/5"
            }`}
          >
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" className="w-3.5 h-3.5 shrink-0">
              <path d="M15.5 2A1.5 1.5 0 0014 3.5v13a1.5 1.5 0 001.5 1.5h1a1.5 1.5 0 001.5-1.5v-13A1.5 1.5 0 0016.5 2h-1zM9.5 6A1.5 1.5 0 008 7.5v9a1.5 1.5 0 001.5 1.5h1a1.5 1.5 0 001.5-1.5v-9A1.5 1.5 0 0010.5 6h-1zM3.5 10A1.5 1.5 0 002 11.5v5A1.5 1.5 0 003.5 18h1A1.5 1.5 0 006 16.5v-5A1.5 1.5 0 004.5 10h-1z" />
            </svg>
            <span>Sociological</span>
          </button>
        </div>

        {/* Role Switcher + Status + Actions */}
        <div className="flex items-center gap-2 shrink-0">
          <RoleSelector />

          <button
            onClick={() => setIsVaultModalOpen(true)}
            title="Test RBAC Gate Access (/api/v1/admin/unredacted-dossier/1)"
            className="hidden sm:flex items-center gap-1.5 rounded-lg border border-amber-600/30 bg-amber-500/10 px-2.5 py-1.5 text-[11px] font-bold text-amber-900 transition-all hover:bg-amber-500/20 active:scale-[0.97] cursor-pointer whitespace-nowrap shrink-0"
          >
            <span>🔒</span>
            <span>Test RBAC Gate</span>
          </button>

        {isQuerying && (
          <div className="hidden sm:flex items-center gap-1.5 rounded-lg border border-[var(--color-secondary)]/30 bg-[var(--color-secondary)]/10 px-2.5 py-1.5 text-[11px] font-bold text-[var(--color-secondary)] whitespace-nowrap shrink-0">
            <span className="relative flex h-2 w-2">
              <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-[var(--color-secondary)] opacity-75"></span>
              <span className="relative inline-flex h-2 w-2 rounded-full bg-[var(--color-secondary)]"></span>
            </span>
            Analyzing...
          </div>
        )}
        <button
          onClick={() => window.open("/api/proxy/api/v1/reports/1/preview", "_blank")}
          aria-label="Export official police case dossier preview"
          title="Export official police case dossier preview"
          className="flex items-center gap-1.5 rounded-lg border border-[var(--color-primary)]/30 bg-[var(--color-primary)]/10 px-3 py-1.5 text-[11px] font-bold text-[var(--color-primary)] transition-all duration-200 hover:bg-[var(--color-primary)]/20 active:scale-[0.97] cursor-pointer whitespace-nowrap shrink-0"
        >
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" className="w-3.5 h-3.5 shrink-0">
            <path d="M13.75 7h-3v5.296l1.943-2.048a.75.75 0 0 1 1.114 1.004l-3.25 3.5a.75.75 0 0 1-1.114 0l-3.25-3.5a.75.75 0 1 1 1.114-1.004l1.943 2.048V7h-3A2.25 2.25 0 0 0 4 9.25v7.5A2.25 2.25 0 0 0 6.25 19h7.5A2.25 2.25 0 0 0 16 16.75v-7.5A2.25 2.25 0 0 0 13.75 7Z" />
            <path d="M10 1a.75.75 0 0 1 .75.75v5.5a.75.75 0 0 1-1.5 0v-5.5A.75.75 0 0 1 10 1Z" />
          </svg>
          <span className="whitespace-nowrap hidden sm:inline">Export Dossier</span>
        </button>
        <button
          onClick={clearWorkspace}
          aria-label="Start a new investigation session"
          title="Start a new investigation session"
          className="flex items-center gap-1.5 rounded-lg bg-[var(--color-primary)] px-3 py-1.5 text-[11px] font-bold text-white shadow-sm transition-all duration-200 hover:bg-[var(--color-primary-hover)] active:scale-[0.97] cursor-pointer whitespace-nowrap shrink-0"
        >
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" className="w-3.5 h-3.5 shrink-0">
            <path d="M10.75 4.75a.75.75 0 0 0-1.5 0v4.5h-4.5a.75.75 0 0 0 0 1.5h4.5v4.5a.75.75 0 0 0 1.5 0v-4.5h4.5a.75.75 0 0 0 0-1.5h-4.5v-4.5Z" />
          </svg>
          <span className="whitespace-nowrap hidden sm:inline">New Session</span>
        </button>
      </div>

      <ClassifiedVaultModal
        isOpen={isVaultModalOpen}
        onClose={() => setIsVaultModalOpen(false)}
      />
    </header>
  </>
  );
}
