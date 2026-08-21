import { useEffect, useState } from "react";

import {
  getProjects,
  getProjectIncidents,
  getIncidentImpact,
  getIncidentRca,
} from "../services/api";

function IncidentAnalysis() {
  const [projects, setProjects] = useState([]);
  const [projectName, setProjectName] = useState("");
  const [incidents, setIncidents] = useState([]);

  const [loadingProjects, setLoadingProjects] =
    useState(false);

  const [loadingAnalysis, setLoadingAnalysis] =
    useState(false);

  const [impactLoading, setImpactLoading] =
    useState(null);

  const [rcaLoading, setRcaLoading] =
    useState(null);

  const [impactData, setImpactData] = useState({});
  const [rcaData, setRcaData] = useState({});

  const [error, setError] = useState("");

  useEffect(() => {
    loadProjects();
  }, []);

  const loadProjects = async () => {
    try {
      setLoadingProjects(true);
      setError("");

      const response = await getProjects();

      setProjects(response.data);
    } catch (error) {
      console.error(error);
      setError(
        "Could not load uploaded projects"
      );
    } finally {
      setLoadingProjects(false);
    }
  };

  const analyzeProject = async () => {
    if (!projectName) {
      setError("Please select a project");
      return;
    }

    try {
      setLoadingAnalysis(true);
      setError("");

      setIncidents([]);
      setImpactData({});
      setRcaData({});

      const response =
        await getProjectIncidents(
          projectName
        );

      setIncidents(response.data);
    } catch (error) {
      console.error(error);
      setError("Could not analyze project");
    } finally {
      setLoadingAnalysis(false);
    }
  };

  const analyzeImpact = async (
    incident,
    index
  ) => {
    try {
      setImpactLoading(index);
      setError("");

      const response =
        await getIncidentImpact(
          projectName,
          incident.className,
          incident.methodName
        );

      setImpactData((previous) => ({
        ...previous,
        [index]: response.data,
      }));
    } catch (error) {
      console.error(error);
      setError("Could not analyze impact");
    } finally {
      setImpactLoading(null);
    }
  };

  const analyzeRca = async (
    incident,
    index
  ) => {
    try {
      setRcaLoading(index);
      setError("");

      const response =
        await getIncidentRca(
          projectName,
          incident.className,
          incident.methodName
        );

      setRcaData((previous) => ({
        ...previous,
        [index]: response.data,
      }));
    } catch (error) {
      console.error(error);
      setError(
        "Could not analyze root cause"
      );
    } finally {
      setRcaLoading(null);
    }
  };

  const getIncidentTitle = (type) => {
    if (type === "RUNTIME_ERROR")
      return "Runtime Error";

    if (type === "POTENTIAL_EXCEPTION")
      return "Potential Exception";

    if (type === "ERROR_MESSAGE")
      return "Error Message";

    return type;
  };

  const getIncidentEmoji = (type) => {
    if (type === "RUNTIME_ERROR")
      return "💥";

    if (type === "POTENTIAL_EXCEPTION")
      return "⚠️";

    if (type === "ERROR_MESSAGE")
      return "🔴";

    return "🚨";
  };

  const getIncidentDescription = (type) => {
    if (type === "RUNTIME_ERROR") {
      return "An error-like runtime message was detected in the project.";
    }

    if (type === "POTENTIAL_EXCEPTION") {
      return "This code can throw an exception, but TraceOps cannot confirm that the exception actually occurred.";
    }

    if (type === "ERROR_MESSAGE") {
      return "An error-like message was detected in the project.";
    }

    return "TraceOps detected a possible issue in the project.";
  };

  return (
    <div className="w-full">

      {/* HEADER */}
      <div className="mb-8">

        <div className="mb-3 inline-flex items-center gap-2 rounded-full border border-red-200 bg-red-50 px-3 py-1 text-xs font-bold text-red-600 dark:border-red-900 dark:bg-red-950/30 dark:text-red-400">
          🚨 INCIDENT CENTER
        </div>

        <h2 className="text-4xl font-black tracking-tight">
          Incident Analysis
        </h2>

        <p className="mt-2 text-slate-500 dark:text-slate-400">
          🔬 Detect possible errors, understand their impact,
          and identify root causes.
        </p>
      </div>

      {/* PROJECT SELECTOR */}
      <div className="mb-8 rounded-3xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900">

        <div className="mb-5 flex items-center gap-3">
          <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-red-100 text-xl dark:bg-red-950/30">
            🎯
          </div>

          <div>
            <h3 className="font-bold">
              Select Project
            </h3>

            <p className="text-xs text-slate-500">
              Choose the project you want to scan
            </p>
          </div>
        </div>

        <div className="flex flex-col gap-4 md:flex-row">

          <select
            value={projectName}
            onChange={(e) => {
              setProjectName(e.target.value);
              setIncidents([]);
              setImpactData({});
              setRcaData({});
              setError("");
            }}
            disabled={loadingProjects}
            className="flex-1 rounded-xl border border-slate-300 bg-slate-50 px-4 py-3 font-semibold outline-none transition focus:border-red-500 focus:ring-4 focus:ring-red-500/10 dark:border-slate-700 dark:bg-slate-800"
          >
            <option value="">
              {loadingProjects
                ? "⏳ Loading Projects..."
                : "📁 Select Project"}
            </option>

            {projects.map((project) => (
              <option
                key={project}
                value={project}
              >
                {project}
              </option>
            ))}
          </select>

          <button
            onClick={analyzeProject}
            disabled={
              !projectName ||
              loadingAnalysis
            }
            className="rounded-xl bg-gradient-to-r from-red-500 to-orange-500 px-7 py-3 font-bold text-white shadow-lg shadow-red-500/20 transition hover:-translate-y-0.5 hover:shadow-xl disabled:cursor-not-allowed disabled:opacity-50"
          >
            {loadingAnalysis
              ? "🔍 Scanning..."
              : "🔍 Analyze Project"}
          </button>
        </div>
      </div>

      {/* ERROR */}
      {error && (
        <div className="mb-6 flex items-center gap-3 rounded-2xl border border-red-300 bg-red-50 p-4 text-red-700 dark:border-red-900 dark:bg-red-950/30 dark:text-red-400">
          🚨 {error}
        </div>
      )}

      {/* LOADING */}
      {loadingAnalysis && (
        <div className="mb-8 rounded-3xl border border-blue-200 bg-blue-50 p-10 text-center dark:border-blue-900 dark:bg-blue-950/30">

          <div className="mb-4 animate-pulse text-5xl">
            🕵️
          </div>

          <h3 className="text-xl font-black text-blue-600 dark:text-blue-400">
            Scanning {projectName}
          </h3>

          <p className="mt-2 text-sm text-slate-500">
            TraceOps is inspecting your project...
          </p>
        </div>
      )}

      {/* NO ISSUES */}
      {!loadingAnalysis &&
        incidents.length === 0 &&
        projectName &&
        !error && (
          <div className="rounded-3xl border border-emerald-300 bg-gradient-to-br from-emerald-50 to-teal-50 p-12 text-center dark:border-emerald-800 dark:from-emerald-950/30 dark:to-teal-950/20">

            <div className="mb-5 text-7xl">
              🎉
            </div>

            <h3 className="text-3xl font-black text-emerald-600 dark:text-emerald-400">
              No Issues Detected
            </h3>

            <p className="mx-auto mt-3 max-w-xl text-slate-600 dark:text-slate-300">
              ✨ No known errors or potential exceptions
              were detected in{" "}
              <strong>{projectName}</strong>.
            </p>

            <div className="mt-6 inline-flex rounded-full bg-emerald-100 px-5 py-2 text-sm font-bold text-emerald-700 dark:bg-emerald-900/40 dark:text-emerald-400">
              🟢 Project looks healthy
            </div>
          </div>
        )}

      {/* INCIDENTS */}
      {incidents.length > 0 && (
        <div className="mt-10">

          {/* SUMMARY */}
          <div className="mb-6 grid gap-4 md:grid-cols-3">

            <SummaryCard
              emoji="🚨"
              title="Issues"
              value={incidents.length}
              description="Possible issues detected"
            />

            <SummaryCard
              emoji="⚠️"
              title="Review"
              value={incidents.length}
              description="Require investigation"
            />

            <SummaryCard
              emoji="🔍"
              title="Project"
              value={projectName}
              description="Currently analyzed"
            />
          </div>

          {/* TITLE */}
          <div className="mb-5">
            <h2 className="text-3xl font-black">
              Detected Issues
            </h2>

            <p className="mt-1 text-sm text-slate-500">
              {incidents.length} possible issue
              {incidents.length !== 1
                ? "s"
                : ""}{" "}
              detected
            </p>
          </div>

          {/* ISSUE CARDS */}
          {incidents.map(
            (incident, index) => (
              <div
                key={index}
                className="mb-7 overflow-hidden rounded-3xl border border-red-200 bg-white shadow-sm transition hover:-translate-y-1 hover:shadow-xl dark:border-slate-800 dark:bg-slate-900"
              >

                {/* TOP */}
                <div className="border-b border-slate-200 bg-gradient-to-r from-red-50 to-orange-50 p-6 dark:border-slate-800 dark:from-red-950/20 dark:to-orange-950/10">

                  <div className="mb-4 flex items-start justify-between gap-4">

                    <div className="flex items-center gap-3">
                      <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-red-100 text-2xl dark:bg-red-950/50">
                        {getIncidentEmoji(
                          incident.type
                        )}
                      </div>

                      <div>
                        <span className="rounded-full bg-red-100 px-3 py-1 text-xs font-black uppercase text-red-700 dark:bg-red-950/60 dark:text-red-400">
                          {incident.type}
                        </span>

                        <h3 className="mt-2 text-2xl font-black text-red-600 dark:text-red-400">
                          {getIncidentTitle(
                            incident.type
                          )}
                        </h3>
                      </div>
                    </div>

                    <div className="hidden rounded-full bg-red-100 px-3 py-1 text-xs font-bold text-red-600 md:block dark:bg-red-950/50">
                      ⚠️ Review
                    </div>
                  </div>

                  <p className="max-w-4xl text-slate-600 dark:text-slate-300">
                    {getIncidentDescription(
                      incident.type
                    )}
                  </p>
                </div>

                {/* INFORMATION */}
                <div className="p-6">

                  <div className="grid gap-4 md:grid-cols-2">

                    <CodeInfo
                      emoji="🧩"
                      title="Class"
                      value={
                        incident.className ||
                        "Unknown"
                      }
                    />

                    <CodeInfo
                      emoji="⚙️"
                      title="Method"
                      value={
                        incident.methodName ||
                        "Unknown"
                      }
                    />

                    <CodeInfo
                      emoji="💬"
                      title="Message"
                      value={incident.message}
                      full
                    />

                    <CodeInfo
                      emoji="📄"
                      title="File"
                      value={incident.file}
                      full
                    />

                    <CodeInfo
                      emoji="📍"
                      title="Line"
                      value={incident.line}
                    />

                  </div>

                  {/* BUTTONS */}
                  {incident.className &&
                    incident.methodName && (
                      <div className="mt-7 flex flex-wrap gap-3">

                        <ActionButton
                          color="blue"
                          onClick={() =>
                            analyzeImpact(
                              incident,
                              index
                            )
                          }
                          loading={
                            impactLoading ===
                            index
                          }
                        >
                          🎯 Analyze Impact
                        </ActionButton>

                        <ActionButton
                          color="purple"
                          onClick={() =>
                            analyzeRca(
                              incident,
                              index
                            )
                          }
                          loading={
                            rcaLoading ===
                            index
                          }
                        >
                          🧠 Analyze RCA
                        </ActionButton>

                      </div>
                    )}

                  {/* IMPACT */}
                  {impactData[index] && (
                    <div className="mt-8 rounded-3xl border border-blue-200 bg-gradient-to-br from-blue-50 to-indigo-50 p-7 dark:border-blue-900 dark:from-blue-950/30 dark:to-indigo-950/20">

                      <AnalysisHeading
                        emoji="🎯"
                        title="Impact Analysis"
                        color="blue"
                      />

                      <div className="rounded-2xl bg-white p-6 text-center shadow-sm dark:bg-slate-900">

                        <p className="text-xs font-bold uppercase tracking-widest text-slate-500">
                          Affected Method
                        </p>

                        <p className="mt-2 break-words text-xl font-black">
                          {
                            impactData[index]
                              .className
                          }
                          .
                          {
                            impactData[index]
                              .methodName
                          }
                          ()
                        </p>
                      </div>

                      <div className="mt-7 grid gap-6 md:grid-cols-2">

                        <AnalysisList
                          emoji="📥"
                          title="Dependencies"
                          items={
                            impactData[index]
                              .dependencies
                          }
                          empty="No dependencies detected."
                        />

                        <AnalysisList
                          emoji="📤"
                          title="Used By"
                          items={
                            impactData[index]
                              .usedBy
                          }
                          empty="Nothing currently depends on this method."
                          blue
                        />

                      </div>
                    </div>
                  )}

                  {/* RCA */}
                  {rcaData[index] && (
                    <div className="mt-8 rounded-3xl border border-purple-300 bg-gradient-to-br from-purple-50 to-fuchsia-50 p-7 dark:border-purple-800 dark:from-purple-950/30 dark:to-fuchsia-950/20">

                      <AnalysisHeading
                        emoji="🧠"
                        title="Root Cause Analysis"
                        color="purple"
                      />

                      <div className="grid gap-5 md:grid-cols-2">

                        <RcaItem
                          emoji="🔴"
                          title="Issue"
                          value={
                            rcaData[index]
                              .issue
                          }
                        />

                        <RcaItem
                          emoji="🔍"
                          title="Likely Cause"
                          value={
                            rcaData[index]
                              .likelyCause
                          }
                        />

                        <RcaItem
                          emoji="⚡"
                          title="Trigger"
                          value={
                            rcaData[index]
                              .trigger
                          }
                        />

                        <RcaItem
                          emoji="💡"
                          title="Suggested Resolution"
                          value={
                            rcaData[index]
                              .suggestedResolution
                          }
                        />

                      </div>
                    </div>
                  )}

                </div>
              </div>
            )
          )}
        </div>
      )}
    </div>
  );
}

function SummaryCard({
  emoji,
  title,
  value,
  description,
}) {
  return (
    <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm transition hover:-translate-y-1 hover:shadow-lg dark:border-slate-800 dark:bg-slate-900">

      <div className="mb-4 flex items-center justify-between">
        <span className="text-3xl">
          {emoji}
        </span>

        <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-bold dark:bg-slate-800">
          {title}
        </span>
      </div>

      <p className="truncate text-2xl font-black">
        {value}
      </p>

      <p className="mt-1 text-xs text-slate-500">
        {description}
      </p>
    </div>
  );
}

function CodeInfo({
  emoji,
  title,
  value,
  full,
}) {
  return (
    <div
      className={`rounded-2xl bg-slate-50 p-5 dark:bg-slate-800/70 ${
        full ? "md:col-span-2" : ""
      }`}
    >
      <div className="mb-2 flex items-center gap-2">
        <span>{emoji}</span>

        <span className="text-xs font-black uppercase tracking-wider text-slate-500">
          {title}
        </span>
      </div>

      <p className="break-words text-sm font-semibold text-slate-800 dark:text-slate-200">
        {value || "Unknown"}
      </p>
    </div>
  );
}

function ActionButton({
  children,
  onClick,
  loading,
  color,
}) {
  const classes =
    color === "purple"
      ? "bg-purple-600 hover:bg-purple-700 shadow-purple-500/20"
      : "bg-blue-600 hover:bg-blue-700 shadow-blue-500/20";

  return (
    <button
      onClick={onClick}
      disabled={loading}
      className={`rounded-xl px-5 py-3 font-bold text-white shadow-lg transition hover:-translate-y-0.5 disabled:cursor-not-allowed disabled:opacity-50 ${classes}`}
    >
      {loading
        ? "⏳ Analyzing..."
        : children}
    </button>
  );
}

function AnalysisHeading({
  emoji,
  title,
  color,
}) {
  const textColor =
    color === "purple"
      ? "text-purple-600 dark:text-purple-400"
      : "text-blue-600 dark:text-blue-400";

  return (
    <div className="mb-6 flex items-center gap-3">
      <span className="text-3xl">
        {emoji}
      </span>

      <h3
        className={`text-2xl font-black ${textColor}`}
      >
        {title}
      </h3>
    </div>
  );
}

function AnalysisList({
  emoji,
  title,
  items,
  empty,
  blue,
}) {
  return (
    <div>
      <h4 className="mb-3 font-black">
        {emoji} {title}
      </h4>

      {!items || items.length === 0 ? (
        <p className="rounded-xl bg-white p-4 text-sm text-slate-500 dark:bg-slate-900">
          💤 {empty}
        </p>
      ) : (
        <div className="flex flex-wrap gap-2">
          {items.map((item, index) => (
            <span
              key={index}
              className={`rounded-xl px-4 py-2 text-sm font-bold ${
                blue
                  ? "bg-blue-600 text-white"
                  : "bg-slate-200 text-slate-700 dark:bg-slate-700 dark:text-slate-200"
              }`}
            >
              {blue ? "📌" : "🔗"} {item}
            </span>
          ))}
        </div>
      )}
    </div>
  );
}

function RcaItem({
  emoji,
  title,
  value,
}) {
  return (
    <div className="rounded-2xl bg-white p-5 shadow-sm dark:bg-slate-900">

      <div className="mb-2 flex items-center gap-2">
        <span className="text-xl">
          {emoji}
        </span>

        <p className="font-black text-purple-600 dark:text-purple-400">
          {title}
        </p>
      </div>

      <p className="break-words text-sm leading-6 text-slate-700 dark:text-slate-300">
        {value ||
          "No information available."}
      </p>
    </div>
  );
}

export default IncidentAnalysis;