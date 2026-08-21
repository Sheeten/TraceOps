import { useEffect, useState } from "react";
import {
  ReactFlow,
  Background,
  Controls,
  MiniMap,
  useNodesState,
  useEdgesState,
} from "@xyflow/react";

import "@xyflow/react/dist/style.css";

function DependencyGraph({ graph }) {
  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  const [selectedNode, setSelectedNode] = useState(null);

  const getNodeType = (name) => {
    if (name.includes("Controller")) return "Controller";
    if (name.includes("Service")) return "Service";
    if (name.includes("Dao")) return "DAO";
    if (name.includes("DTO")) return "DTO";
    if (name.includes("Request")) return "Request";
    if (name.includes("Exception")) return "Exception";
    return "Model";
  };

  const getNodeStyle = (type) => {
    switch (type) {
      case "Controller":
        return {
          background: "#1d4ed8",
          border: "2px solid #60a5fa",
        };

      case "Service":
        return {
          background: "#047857",
          border: "2px solid #34d399",
        };

      case "DAO":
        return {
          background: "#b45309",
          border: "2px solid #fbbf24",
        };

      case "DTO":
        return {
          background: "#7e22ce",
          border: "2px solid #c084fc",
        };

      case "Request":
        return {
          background: "#be123c",
          border: "2px solid #fb7185",
        };

      case "Exception":
        return {
          background: "#991b1b",
          border: "2px solid #f87171",
        };

      default:
        return {
          background: "#374151",
          border: "2px solid #9ca3af",
        };
    }
  };

  const createNodes = () => {
    const services = [];

    graph.forEach((edge) => {
      if (!services.includes(edge.source)) {
        services.push(edge.source);
      }

      if (!services.includes(edge.target)) {
        services.push(edge.target);
      }
    });

    const columns = {
      Controller: 0,
      Service: 1,
      DAO: 2,
      Model: 3,
      DTO: 3,
      Request: 3,
      Exception: 3,
    };

    const rowCount = {};

    return services.map((service) => {
      const type = getNodeType(service);
      const column = columns[type] ?? 3;

      if (!rowCount[type]) {
        rowCount[type] = 0;
      }

      const row = rowCount[type];
      rowCount[type]++;

      const style = getNodeStyle(type);

      return {
        id: service,
        position: {
          x: column * 350,
          y: row * 130,
        },
        data: {
          label: (
            <div>
              <div style={{ fontWeight: "bold" }}>{service}</div>
              <div
                style={{
                  fontSize: "11px",
                  marginTop: "5px",
                  opacity: 0.8,
                }}
              >
                {type}
              </div>
            </div>
          ),
        },
        style: {
          ...style,
          color: "white",
          borderRadius: "10px",
          padding: "12px",
          width: 190,
          textAlign: "center",
          boxShadow: "0 4px 12px rgba(0,0,0,0.3)",
        },
      };
    });
  };

  const createEdges = () => {
    return graph.map((edge, index) => ({
      id: `edge-${index}`,
      source: edge.source,
      target: edge.target,
      label: edge.relationship,
      animated: true,
      style: {
        stroke: edge.relationship === "USES" ? "#f59e0b" : "#64748b",
        strokeWidth: 2,
      },
      labelStyle: {
        fill: "#ffffff",
        fontWeight: "bold",
        fontSize: 10,
      },
      labelBgStyle: {
        fill: "#111827",
      },
    }));
  };

  useEffect(() => {
    if (!graph || graph.length === 0) {
      setNodes([]);
      setEdges([]);
      return;
    }

    setNodes(createNodes());
    setEdges(createEdges());
  }, [graph]);

  const handleNodeClick = (event, node) => {
    const nodeName = node.id;

    const dependencies = graph.filter(
      (edge) => edge.source === nodeName
    );

    const usedBy = graph.filter(
      (edge) => edge.target === nodeName
    );

    setSelectedNode({
      name: nodeName,
      type: getNodeType(nodeName),
      dependencies,
      usedBy,
    });
  };

  return (
    <div
      style={{
        width: "100%",
        height: "650px",
        border: "1px solid #1e293b",
        borderRadius: "12px",
        overflow: "hidden",
        position: "relative",
      }}
    >
      <ReactFlow
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onNodeClick={handleNodeClick}
        fitView
      >
        <Background />
        <Controls />
        <MiniMap />
      </ReactFlow>

      {selectedNode && (
        <div
          style={{
            position: "absolute",
            top: "20px",
            right: "20px",
            width: "300px",
            maxHeight: "550px",
            overflowY: "auto",
            background: "#111827",
            color: "white",
            border: "1px solid #374151",
            borderRadius: "12px",
            padding: "20px",
            boxShadow: "0 10px 30px rgba(0,0,0,0.5)",
            zIndex: 10,
          }}
        >
          <button
            onClick={() => setSelectedNode(null)}
            style={{
              float: "right",
              background: "transparent",
              color: "white",
              border: "none",
              fontSize: "18px",
              cursor: "pointer",
            }}
          >
            ✕
          </button>

          <h3 style={{ marginTop: 0 }}>
            {selectedNode.name}
          </h3>

          <div
            style={{
              display: "inline-block",
              padding: "5px 10px",
              borderRadius: "6px",
              background: "#374151",
              fontSize: "12px",
              marginBottom: "15px",
            }}
          >
            {selectedNode.type}
          </div>

          <h4>Dependencies</h4>

          {selectedNode.dependencies.length === 0 ? (
            <p style={{ color: "#9ca3af" }}>
              No dependencies
            </p>
          ) : (
            selectedNode.dependencies.map((dependency, index) => (
              <div
                key={index}
                style={{
                  padding: "8px",
                  marginBottom: "6px",
                  background: "#1f2937",
                  borderRadius: "6px",
                  fontSize: "13px",
                }}
              >
                <strong>{dependency.target}</strong>
                <div
                  style={{
                    color: "#9ca3af",
                    fontSize: "11px",
                    marginTop: "3px",
                  }}
                >
                  {dependency.relationship}
                </div>
              </div>
            ))
          )}

          <h4>Used By</h4>

          {selectedNode.usedBy.length === 0 ? (
            <p style={{ color: "#9ca3af" }}>
              Nothing depends on this class
            </p>
          ) : (
            selectedNode.usedBy.map((dependency, index) => (
              <div
                key={index}
                style={{
                  padding: "8px",
                  marginBottom: "6px",
                  background: "#1f2937",
                  borderRadius: "6px",
                  fontSize: "13px",
                }}
              >
                <strong>{dependency.source}</strong>
                <div
                  style={{
                    color: "#9ca3af",
                    fontSize: "11px",
                    marginTop: "3px",
                  }}
                >
                  {dependency.relationship}
                </div>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}

export default DependencyGraph;