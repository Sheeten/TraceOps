import { useEffect, useState } from "react";

import {
  getProjects,
  getServices,
  getProjectInfo,
  getCodeGraph,
  uploadProject,
} from "./services/api";

import CodeDependencyGraph from "./components/CodeDependencyGraph";
import IncidentAnalysis from "./components/IncidentAnalysis";

function App() {
  const [page, setPage] = useState("project");

  const [projects, setProjects] = useState([]);
  const [services, setServices] = useState([]);
  const [selectedProject, setSelectedProject] = useState("");
  const [selectedService, setSelectedService] = useState("");

  const [projectInfo, setProjectInfo] = useState(null);
  const [codeGraph, setCodeGraph] = useState([]);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const [darkMode, setDarkMode] = useState(() => {
    return localStorage.getItem("traceops-theme") !== "light";
  });

  useEffect(() => {
    loadProjects();
  }, []);

  useEffect(() => {
    localStorage.setItem(
      "traceops-theme",
      darkMode ? "dark" : "light"
    );
  }, [darkMode]);

  const loadProjects = async () => {
    try {
      const response = await getProjects();
      setProjects(response.data);
    } catch (error) {
      console.error(error);
      setError("Could not load projects");
    }
  };

  const handleUpload = async (event) => {
    const file = event.target.files[0];

    if (!file) return;

    if (!file.name.toLowerCase().endsWith(".zip")) {
      setError("Please upload a ZIP file");
      return;
    }

    try {
      setError("");
      setLoading(true);

      await uploadProject(file);

      alert("🎉 Project uploaded successfully!");

      await loadProjects();
    } catch (error) {
      console.error(error);
      setError("Could not upload project");
    } finally {
      setLoading(false);
    }
  };

  const handleProjectChange = async (projectName) => {
    setSelectedProject(projectName);
    setSelectedService("");
    setProjectInfo(null);
    setCodeGraph([]);
    setServices([]);

    if (!projectName) return;

    try {
      setError("");
      setLoading(true);

      const response = await getServices(projectName);
      const projectServices = response.data;

      setServices(projectServices);

      if (projectServices.length === 1) {
        const moduleName = projectServices[0];

        setSelectedService(moduleName);

        await loadServiceData(
          projectName,
          moduleName
        );
      }
    } catch (error) {
      console.error(error);
      setError("Could not load services");
    } finally {
      setLoading(false);
    }
  };

  const loadServiceData = async (
    projectName,
    moduleName
  ) => {
    if (!moduleName || !projectName) return;

    try {
      setError("");

      const infoResponse = await getProjectInfo(
        projectName,
        moduleName
      );

      setProjectInfo(infoResponse.data);

      const graphResponse = await getCodeGraph(
        projectName,
        moduleName
      );

      console.log(
        "🔥 CODE GRAPH RESPONSE:",
        graphResponse.data
      );

      setCodeGraph(graphResponse.data);
    } catch (error) {
      console.error(
        "❌ GRAPH ERROR:",
        error
      );

      console.error(
        "❌ BACKEND RESPONSE:",
        error.response?.data
      );

      setError(
        "Could not load project information"
      );
    }
  };

  const handleServiceChange = async (moduleName) => {
    setSelectedService(moduleName);
    setProjectInfo(null);
    setCodeGraph([]);

    if (!moduleName || !selectedProject) return;

    try {
      setError("");
      setLoading(true);

      await loadServiceData(
        selectedProject,
        moduleName
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className={
        darkMode
          ? "dark min-h-screen bg-[#020617] text-white"
          : "min-h-screen bg-slate-100 text-slate-900"
      }
    >
      <div className="min-h-screen bg-slate-100 transition-colors dark:bg-[#020617]">

        {/* HEADER */}
        <header className="sticky top-0 z-50 border-b border-slate-200/70 bg-white/90 backdrop-blur-xl dark:border-slate-800 dark:bg-slate-950/90">
          <div className="mx-auto flex max-w-[1600px] items-center justify-between px-6 py-5">

            <div className="flex items-center gap-4">

              <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-gradient-to-br from-blue-500 to-purple-600 text-2xl shadow-lg shadow-blue-500/20">
                🔎
              </div>

              <div>
                <h1 className="text-2xl font-black tracking-tight">
                  Trace<span className="text-blue-500">Ops</span>
                </h1>

                <p className="text-sm text-slate-500 dark:text-slate-400">
                  🧠 Project intelligence & incident analysis
                </p>
              </div>

            </div>

            <div className="flex items-center gap-3">

              <div className="hidden rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1.5 text-xs font-bold text-emerald-600 sm:block dark:border-emerald-900 dark:bg-emerald-950/30 dark:text-emerald-400">
                ● System Ready
              </div>

              <button
                onClick={() => setDarkMode(!darkMode)}
                className="rounded-xl border border-slate-300 bg-slate-100 px-4 py-2.5 text-sm font-bold transition hover:-translate-y-0.5 hover:shadow-md dark:border-slate-700 dark:bg-slate-800"
              >
                {darkMode ? "☀️ Light" : "🌙 Dark"}
              </button>

            </div>
          </div>
        </header>

        {/* NAVIGATION */}
        <nav className="border-b border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-950">
          <div className="mx-auto flex max-w-[1600px] gap-2 px-6">

            <button
              onClick={() => setPage("project")}
              className={`relative px-6 py-4 text-sm font-bold transition ${
                page === "project"
                  ? "text-blue-500"
                  : "text-slate-500 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white"
              }`}
            >
              🧩 Project Analysis

              {page === "project" && (
                <span className="absolute bottom-0 left-0 h-0.5 w-full rounded-full bg-blue-500" />
              )}
            </button>

            <button
              onClick={() => setPage("incident")}
              className={`relative px-6 py-4 text-sm font-bold transition ${
                page === "incident"
                  ? "text-red-500"
                  : "text-slate-500 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white"
              }`}
            >
              🚨 Incident Analysis

              {page === "incident" && (
                <span className="absolute bottom-0 left-0 h-0.5 w-full rounded-full bg-red-500" />
              )}
            </button>

          </div>
        </nav>

        <main className="mx-auto max-w-[1600px] px-6 py-10">

          {/* ERROR */}
          {error && (
            <div className="mb-6 flex items-center gap-3 rounded-2xl border border-red-300 bg-red-50 p-4 font-medium text-red-700 shadow-sm dark:border-red-900 dark:bg-red-950/30 dark:text-red-400">

              🚨

              <span>{error}</span>

              <button
                onClick={() => setError("")}
                className="ml-auto text-lg"
              >
                ✕
              </button>

            </div>
          )}

          {/* PROJECT PAGE */}
          {page === "project" && (
            <div>

              {/* TITLE */}
              <div className="mb-8">

                <div className="mb-3 inline-flex items-center gap-2 rounded-full border border-blue-200 bg-blue-50 px-3 py-1 text-xs font-bold text-blue-600 dark:border-blue-900 dark:bg-blue-950/30 dark:text-blue-400">
                  ✨ CODE INTELLIGENCE
                </div>

                <h2 className="text-4xl font-black tracking-tight">
                  Project Analysis
                </h2>

                <p className="mt-2 text-slate-500 dark:text-slate-400">
                  🔬 Upload your Spring Boot project and explore its architecture.
                </p>

              </div>

              {/* UPLOAD */}
              <div className="group mb-8 overflow-hidden rounded-3xl border border-slate-200 bg-white p-6 shadow-sm transition hover:border-blue-400 hover:shadow-xl hover:shadow-blue-500/5 dark:border-slate-800 dark:bg-slate-900">

                <div className="mb-5 flex items-center gap-3">

                  <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-100 text-xl dark:bg-blue-950/40">
                    📦
                  </div>

                  <div>
                    <h3 className="font-bold">
                      Upload Project
                    </h3>

                    <p className="text-xs text-slate-500">
                      ZIP files only
                    </p>
                  </div>

                </div>

                <label className="flex cursor-pointer flex-col items-center justify-center rounded-2xl border-2 border-dashed border-slate-300 bg-slate-50 px-6 py-14 transition hover:border-blue-500 hover:bg-blue-50 dark:border-slate-700 dark:bg-slate-950 dark:hover:border-blue-500 dark:hover:bg-blue-950/20">

                  <div className="mb-4 text-6xl transition duration-300 group-hover:-translate-y-2 group-hover:rotate-6">
                    📦
                  </div>

                  <span className="text-lg font-bold">
                    Choose Project ZIP
                  </span>

                  <span className="mt-2 text-sm text-slate-500 dark:text-slate-400">
                    Drag & drop your Spring Boot project here
                  </span>

                  <span className="mt-4 rounded-xl bg-blue-600 px-5 py-2.5 text-sm font-bold text-white shadow-lg shadow-blue-500/20">
                    🚀 Browse Files
                  </span>

                  <input
                    type="file"
                    accept=".zip"
                    onChange={handleUpload}
                    className="hidden"
                  />

                </label>

              </div>

              {/* SELECTORS */}
              <div
                className={`mb-8 grid gap-6 ${
                  services.length > 1
                    ? "md:grid-cols-2"
                    : "md:grid-cols-1"
                }`}
              >

                <SelectorCard
                  emoji="📁"
                  title="Project"
                  description="Choose a project to inspect"
                >

                  <select
                    value={selectedProject}
                    onChange={(e) =>
                      handleProjectChange(e.target.value)
                    }
                    className="w-full rounded-xl border border-slate-300 bg-slate-50 px-4 py-3 font-semibold outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 dark:border-slate-700 dark:bg-slate-800"
                  >

                    <option value="">
                      Select Project
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

                </SelectorCard>

                {services.length > 1 && (
                  <SelectorCard
                    emoji="🧩"
                    title="Service / Module"
                    description="Choose the module to analyze"
                  >

                    <select
                      value={selectedService}
                      onChange={(e) =>
                        handleServiceChange(e.target.value)
                      }
                      disabled={!selectedProject}
                      className="w-full rounded-xl border border-slate-300 bg-slate-50 px-4 py-3 font-semibold outline-none transition focus:border-purple-500 focus:ring-4 focus:ring-purple-500/10 disabled:opacity-50 dark:border-slate-700 dark:bg-slate-800"
                    >

                      <option value="">
                        Select Service
                      </option>

                      {services.map((service) => (
                        <option
                          key={service}
                          value={service}
                        >
                          {service}
                        </option>
                      ))}

                    </select>

                  </SelectorCard>
                )}

              </div>

              {/* ANALYZING */}
              {services.length === 1 &&
                selectedService && (
                  <div className="mb-8 flex items-center gap-3 rounded-2xl border border-blue-200 bg-blue-50 p-4 text-blue-700 dark:border-blue-900 dark:bg-blue-950/30 dark:text-blue-400">

                    <span className="animate-pulse text-xl">
                      🔍
                    </span>

                    <span>
                      Analyzing module{" "}
                      <strong>
                        {selectedService}
                      </strong>
                    </span>

                  </div>
                )}

              {/* LOADING */}
              {loading && (
                <div className="mb-8 flex flex-col items-center justify-center rounded-2xl border border-blue-200 bg-blue-50 p-8 dark:border-blue-900 dark:bg-blue-950/30">

                  <div className="mb-3 animate-spin text-4xl">
                    ⚙️
                  </div>

                  <p className="font-bold text-blue-600 dark:text-blue-400">
                    Analyzing your project...
                  </p>

                </div>
              )}

              {/* PROJECT OVERVIEW */}
              {projectInfo && (
                <div className="mb-8">

                  <div className="mb-5">
                    <div className="mb-2 inline-flex rounded-full bg-blue-100 px-3 py-1 text-xs font-bold text-blue-600 dark:bg-blue-950/30 dark:text-blue-400">
                      📊 PROJECT OVERVIEW
                    </div>

                    <h2 className="text-3xl font-black">
                      Architecture Summary
                    </h2>

                    <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
                      Quick overview of the analyzed project.
                    </p>
                  </div>

                  <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">

                    <OverviewCard
                      emoji="📦"
                      title="Project"
                      value={projectInfo.projectName}
                      description="Analyzed project"
                    />

                    <OverviewCard
                      emoji="🧩"
                      title="Classes"
                      value={codeGraph.length > 0
                        ? new Set(
                            codeGraph.flatMap((edge) => [
                              edge.source,
                              edge.target,
                            ])
                          ).size
                        : 0}
                      description="Classes in dependency graph"
                    />

                    <OverviewCard
                      emoji="🔗"
                      title="Relations"
                      value={codeGraph.length}
                      description="Detected relationships"
                    />

                    <OverviewCard
                      emoji="📚"
                      title="Dependencies"
                      value={
                        projectInfo.dependencies?.length || 0
                      }
                      description="Project dependencies"
                    />

                  </div>

                </div>
              )}

              {/* PROJECT INFO */}
              {projectInfo && (
                <div className="mb-10 rounded-3xl border border-slate-200 bg-white p-7 shadow-sm dark:border-slate-800 dark:bg-slate-900">

                  <div className="mb-7 flex items-center gap-3">

                    <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-purple-100 text-xl dark:bg-purple-950/40">
                      🧠
                    </div>

                    <div>

                      <h2 className="text-2xl font-black">
                        Project Intelligence
                      </h2>

                      <p className="text-sm text-slate-500">
                        Technical information detected from your project
                      </p>

                    </div>

                  </div>

                  <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">

                    <InfoCard
                      emoji="📦"
                      title="Project"
                      value={projectInfo.projectName}
                    />

                    <InfoCard
                      emoji="🆔"
                      title="Group ID"
                      value={projectInfo.groupId}
                    />

                    <InfoCard
                      emoji="🏷️"
                      title="Artifact ID"
                      value={projectInfo.artifactId}
                    />

                    <InfoCard
                      emoji="☕"
                      title="Java Version"
                      value={projectInfo.javaVersion}
                    />

                    <InfoCard
                      emoji="🌱"
                      title="Spring Boot"
                      value={projectInfo.springBootVersion}
                    />

                  </div>

                  <div className="mt-8">

                    <h3 className="mb-4 font-bold">
                      🔗 Dependencies
                    </h3>

                    <div className="flex flex-wrap gap-2">

                      {projectInfo.dependencies?.map(
                        (dependency) => (
                          <span
                            key={dependency}
                            className="rounded-xl border border-slate-200 bg-slate-100 px-4 py-2 text-xs font-semibold text-slate-700 transition hover:-translate-y-0.5 hover:border-blue-400 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300"
                          >
                            📌 {dependency}
                          </span>
                        )
                      )}

                    </div>

                  </div>

                </div>
              )}

              {/* GRAPH */}
              {codeGraph.length > 0 && (
                <div>

                  <div className="mb-5 flex items-end justify-between">

                    <div>

                      <div className="mb-2 inline-flex rounded-full bg-purple-100 px-3 py-1 text-xs font-bold text-purple-600 dark:bg-purple-950/30 dark:text-purple-400">
                        🕸️ ARCHITECTURE MAP
                      </div>

                      <h2 className="text-3xl font-black">
                        Code Dependency Graph
                      </h2>

                      <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
                        Click any class to inspect its relationships.
                      </p>

                    </div>

                    <div className="hidden rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm font-bold dark:border-slate-800 dark:bg-slate-900 md:block">
                      🧩 {codeGraph.length} Relationships
                    </div>

                  </div>

                  <CodeDependencyGraph graph={codeGraph} />

                </div>
              )}

            </div>
          )}

          {/* INCIDENT PAGE */}
          {page === "incident" && (
            <IncidentAnalysis />
          )}

        </main>

      </div>
    </div>
  );
}

function SelectorCard({
  emoji,
  title,
  description,
  children,
}) {
  return (
    <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm transition hover:-translate-y-1 hover:shadow-lg dark:border-slate-800 dark:bg-slate-900">

      <div className="mb-4 flex items-center gap-3">

        <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-slate-100 text-xl dark:bg-slate-800">
          {emoji}
        </div>

        <div>

          <h3 className="font-bold">
            {title}
          </h3>

          <p className="text-xs text-slate-500">
            {description}
          </p>

        </div>

      </div>

      {children}

    </div>
  );
}

function OverviewCard({
  emoji,
  title,
  value,
  description,
}) {
  return (
    <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm transition hover:-translate-y-1 hover:border-blue-400 hover:shadow-lg dark:border-slate-800 dark:bg-slate-900">

      <div className="mb-4 flex items-center justify-between">

        <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-slate-100 text-xl dark:bg-slate-800">
          {emoji}
        </div>

        <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-bold text-slate-500 dark:bg-slate-800 dark:text-slate-400">
          {title}
        </span>

      </div>

      <p className="break-words text-2xl font-black">
        {value || "0"}
      </p>

      <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">
        {description}
      </p>

    </div>
  );
}

function InfoCard({
  emoji,
  title,
  value,
}) {
  return (
    <div className="group rounded-2xl border border-slate-200 bg-slate-50 p-5 transition hover:-translate-y-1 hover:border-blue-400 hover:shadow-lg dark:border-slate-700 dark:bg-slate-800/70">

      <div className="mb-3 flex items-center gap-2">

        <span className="text-xl">
          {emoji}
        </span>

        <p className="text-xs font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400">
          {title}
        </p>

      </div>

      <p className="break-words font-bold text-slate-900 dark:text-white">
        {value || "N/A"}
      </p>

    </div>
  );
}

export default App;