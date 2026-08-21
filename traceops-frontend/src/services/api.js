import axios from "axios";

const API = axios.create({
  baseURL: "https://traceops-xis6.onrender.com/api",
});

export const getProjects = () => {
  return API.get("/incidents/projects");
};

export const createProject = (project) => {
  return API.post(
    "/incidents/projects",
    project
  );
};

export const uploadProject = (file) => {
  const formData = new FormData();

  formData.append("file", file);

  return API.post(
    "/projects/upload",
    formData
  );
};

export const getServices = (
  projectName
) => {
  return API.get(
    `/projects/${projectName}/services`
  );
};

export const getProjectInfo = (
  projectName,
  moduleName
) => {
  return API.get(
    `/projects/${projectName}/${moduleName}/info`
  );
};

export const getCodeGraph = (
  projectName,
  moduleName
) => {
  return API.get(
    `/incidents/projects/${projectName}/${moduleName}/code-graph`
  );
};

export const getIncident = (
  incidentId
) => {
  return API.get(
    `/incidents/${incidentId}`
  );
};

export const getImpact = (
  incidentId
) => {
  return API.get(
    `/incidents/${incidentId}/impact`
  );
};

export const getRca = (
  incidentId
) => {
  return API.get(
    `/incidents/${incidentId}/rca`
  );
};

export const getServiceGraph = (
  incidentId
) => {
  return API.get(
    `/incidents/${incidentId}/graph`
  );
};

export const getIncidents = () => {
  return API.get("/incidents");
};

export const getProjectIncidents = (
  projectName
) => {
  return API.get(
    `/incidents/projects/${projectName}/incidents`
  );
};

export const getIncidentImpact = (
  projectName,
  className,
  methodName
) => {
  return API.get(
    `/incidents/projects/${projectName}/incidents/${className}/impact`,
    {
      params: {
        methodName,
      },
    }
  );
};

export const getIncidentRca = (
  projectName,
  className,
  methodName
) => {
  return API.get(
    `/incidents/projects/${projectName}/incidents/${className}/rca`,
    {
      params: {
        methodName,
      },
    }
  );
};