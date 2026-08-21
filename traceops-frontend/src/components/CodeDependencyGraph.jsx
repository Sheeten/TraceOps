import { useEffect, useMemo, useState } from "react";

import {
  ReactFlow,
  Background,
  Controls,
  MiniMap,
  useNodesState,
  useEdgesState,
} from "@xyflow/react";

import "@xyflow/react/dist/style.css";

function CodeDependencyGraph({ graph }) {
  const [nodes, setNodes, onNodesChange] =
    useNodesState([]);

  const [edges, setEdges, onEdgesChange] =
    useEdgesState([]);

  const [selectedNode, setSelectedNode] =
    useState(null);

  const [searchText, setSearchText] =
    useState("");

  const getNodeType = (name) => {
    const value = name.toLowerCase();

    if (value.includes("controller")) return "Controller";
    if (value.includes("service")) return "Service";
    if (value.includes("repo")) return "Repository";
    if (value.includes("dao")) return "DAO";
    if (value.includes("dto")) return "DTO";
    if (value.includes("request")) return "Request";
    if (value.includes("exception")) return "Exception";

    return "Model";
  };

  const getNodeEmoji = (type) => {
    const emojis = {
      Controller: "🎮",
      Service: "⚙️",
      Repository: "🗄️",
      DAO: "🔧",
      DTO: "📦",
      Request: "📨",
      Exception: "🚨",
      Model: "🧩",
    };

    return emojis[type] || "🧩";
  };

  const getNodeStyle = (type) => {
    const styles = {
      Controller: {
        background: "#1d4ed8",
        border: "2px solid #60a5fa",
      },

      Service: {
        background: "#047857",
        border: "2px solid #34d399",
      },

      Repository: {
        background: "#9333ea",
        border: "2px solid #c084fc",
      },

      DAO: {
        background: "#b45309",
        border: "2px solid #fbbf24",
      },

      DTO: {
        background: "#7e22ce",
        border: "2px solid #c084fc",
      },

      Request: {
        background: "#be123c",
        border: "2px solid #fb7185",
      },

      Exception: {
        background: "#991b1b",
        border: "2px solid #f87171",
      },

      Model: {
        background: "#374151",
        border: "2px solid #9ca3af",
      },
    };

    return styles[type] || styles.Model;
  };

  const getTypeColor = (type) => {
    const colors = {
      Controller: "#3b82f6",
      Service: "#10b981",
      Repository: "#a855f7",
      DAO: "#f59e0b",
      DTO: "#c084fc",
      Request: "#fb7185",
      Exception: "#ef4444",
      Model: "#9ca3af",
    };

    return colors[type] || "#9ca3af";
  };

  const classNames = useMemo(() => {
    if (!graph) return [];

    const names = [];

    graph.forEach((edge) => {
      if (!names.includes(edge.source)) {
        names.push(edge.source);
      }

      if (!names.includes(edge.target)) {
        names.push(edge.target);
      }
    });

    return names;
  }, [graph]);

  const getLayers = () => ({
    Controller: 0,
    Service: 1,
    Repository: 2,
    DAO: 2,
    DTO: 3,
    Request: 3,
    Model: 3,
    Exception: 3,
  });

  useEffect(() => {
    if (!graph || graph.length === 0) {
      setNodes([]);
      setEdges([]);
      return;
    }

    const layers = getLayers();
    const rowCount = {};

    const newNodes = classNames.map((name) => {
      const type = getNodeType(name);
      const column = layers[type] ?? 3;

      if (!rowCount[column]) {
        rowCount[column] = 0;
      }

      const row = rowCount[column];
      rowCount[column]++;

      const style = getNodeStyle(type);

      return {
        id: name,

        position: {
          x: column * 330,
          y: row * 150,
        },

        draggable: true,

        data: {
          label: (
            <div
              style={{
                textAlign: "center",
                lineHeight: "1.3",
              }}
            >
              <div
                style={{
                  fontSize: "22px",
                  marginBottom: "5px",
                }}
              >
                {getNodeEmoji(type)}
              </div>

              <div
                style={{
                  fontWeight: "800",
                  fontSize: "14px",
                  wordBreak: "break-word",
                }}
              >
                {name}
              </div>

              <div
                style={{
                  marginTop: "5px",
                  fontSize: "10px",
                  opacity: 0.8,
                  textTransform: "uppercase",
                  letterSpacing: "1px",
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
          width: 220,
          minHeight: 100,
          borderRadius: "18px",
          padding: "14px",
          boxShadow:
            "0 10px 30px rgba(0,0,0,0.35)",
          transition: "all 0.2s ease",
        },
      };
    });

    const newEdges = graph.map((edge, index) => {
      const isUses =
        edge.relationship === "USES";

      return {
        id: `code-edge-${index}`,

        source: edge.source,
        target: edge.target,

        label: isUses
          ? "⚡ USES"
          : "🔗 IMPORTS",

        animated: true,

        type: "smoothstep",

        style: {
          stroke: isUses
            ? "#f59e0b"
            : "#64748b",
          strokeWidth: 2.5,
        },

        labelStyle: {
          fill: "#e5e7eb",
          fontSize: 10,
          fontWeight: "800",
        },

        labelBgStyle: {
          fill: "#111827",
          fillOpacity: 0.95,
        },

        labelBgPadding: [7, 4],

        labelBgBorderRadius: 6,
      };
    });

    setNodes(newNodes);
    setEdges(newEdges);
  }, [
    graph,
    classNames,
    setNodes,
    setEdges,
  ]);

  const filteredNames = useMemo(() => {
    if (!searchText.trim()) {
      return classNames;
    }

    return classNames.filter((name) =>
      name
        .toLowerCase()
        .includes(searchText.toLowerCase())
    );
  }, [classNames, searchText]);

  const handleNodeClick = (event, node) => {
    const dependencies = graph.filter(
      (edge) => edge.source === node.id
    );

    const usedBy = graph.filter(
      (edge) => edge.target === node.id
    );

    setSelectedNode({
      name: node.id,
      type: getNodeType(node.id),
      dependencies,
      usedBy,
    });
  };

  const closePanel = () => {
    setSelectedNode(null);
  };

  const displayedNodes = useMemo(() => {
    if (!selectedNode) {
      return nodes;
    }

    const connectedNames = new Set();

    connectedNames.add(selectedNode.name);

    graph.forEach((edge) => {
      if (edge.source === selectedNode.name) {
        connectedNames.add(edge.target);
      }

      if (edge.target === selectedNode.name) {
        connectedNames.add(edge.source);
      }
    });

    return nodes.map((node) => {
      const connected =
        connectedNames.has(node.id);

      return {
        ...node,

        style: {
          ...node.style,

          opacity: connected ? 1 : 0.18,

          transform:
            node.id === selectedNode.name
              ? "scale(1.05)"
              : "scale(1)",

          boxShadow:
            node.id === selectedNode.name
              ? `0 0 0 5px ${getTypeColor(
                  selectedNode.type
                )}55, 0 15px 40px rgba(0,0,0,0.6)`
              : node.style.boxShadow,
        },
      };
    });
  }, [nodes, selectedNode, graph]);

  const displayedEdges = useMemo(() => {
    if (!selectedNode) {
      return edges;
    }

    return edges.map((edge) => {
      const connected =
        edge.source === selectedNode.name ||
        edge.target === selectedNode.name;

      return {
        ...edge,

        animated: connected,

        style: {
          ...edge.style,

          opacity: connected ? 1 : 0.12,

          strokeWidth: connected ? 4 : 1,
        },
      };
    });
  }, [edges, selectedNode]);

  const selectSearchResult = (name) => {
    const dependencies = graph.filter(
      (edge) => edge.source === name
    );

    const usedBy = graph.filter(
      (edge) => edge.target === name
    );

    setSelectedNode({
      name,
      type: getNodeType(name),
      dependencies,
      usedBy,
    });

    setSearchText("");
  };

  return (
    <div
      style={{
        width: "100%",
        height: "720px",
        border: "1px solid #334155",
        borderRadius: "22px",
        overflow: "hidden",
        position: "relative",
        background:
          "radial-gradient(circle at top left, #172554 0%, #0f172a 35%, #020617 100%)",
        boxShadow:
          "0 25px 70px rgba(0,0,0,0.35)",
      }}
    >

      {/* TOP TOOLBAR */}
      <div
        style={{
          position: "absolute",
          top: "15px",
          left: "15px",
          right: "15px",
          zIndex: 20,
          display: "flex",
          alignItems: "center",
          gap: "10px",
          flexWrap: "wrap",
        }}
      >

        {/* SEARCH */}
        <div
          style={{
            background: "#111827",
            border: "1px solid #374151",
            borderRadius: "14px",
            padding: "10px 14px",
            display: "flex",
            alignItems: "center",
            width: "280px",
            boxShadow:
              "0 10px 30px rgba(0,0,0,0.3)",
          }}
        >
          <span
            style={{
              marginRight: "9px",
              fontSize: "18px",
            }}
          >
            🔍
          </span>

          <input
            type="text"
            placeholder="Search class..."
            value={searchText}
            onChange={(e) =>
              setSearchText(e.target.value)
            }
            style={{
              width: "100%",
              background: "transparent",
              border: "none",
              outline: "none",
              color: "white",
              fontSize: "13px",
            }}
          />
        </div>

        {/* STATS */}
        <StatBadge
          emoji="🧩"
          value={classNames.length}
          label="Classes"
        />

        <StatBadge
          emoji="🔗"
          value={graph.length}
          label="Relations"
        />

        {selectedNode && (
          <button
            onClick={closePanel}
            style={{
              marginLeft: "auto",
              border: "1px solid #475569",
              background: "#1e293b",
              color: "white",
              borderRadius: "12px",
              padding: "10px 14px",
              cursor: "pointer",
              fontWeight: "700",
            }}
          >
            ✕ Clear Selection
          </button>
        )}
      </div>

      {/* SEARCH RESULTS */}
      {searchText && (
        <div
          style={{
            position: "absolute",
            top: "68px",
            left: "15px",
            width: "280px",
            maxHeight: "260px",
            overflowY: "auto",
            background: "#111827",
            border: "1px solid #374151",
            borderRadius: "14px",
            zIndex: 30,
            boxShadow:
              "0 20px 40px rgba(0,0,0,0.5)",
          }}
        >
          {filteredNames.length === 0 ? (
            <div
              style={{
                padding: "16px",
                color: "#9ca3af",
                fontSize: "13px",
              }}
            >
              😕 No classes found
            </div>
          ) : (
            filteredNames.map((name) => (
              <button
                key={name}
                onClick={() =>
                  selectSearchResult(name)
                }
                style={{
                  display: "block",
                  width: "100%",
                  textAlign: "left",
                  padding: "12px 14px",
                  background: "transparent",
                  border: "none",
                  borderBottom:
                    "1px solid #1f2937",
                  color: "white",
                  cursor: "pointer",
                }}
              >
                <div
                  style={{
                    fontWeight: "700",
                  }}
                >
                  {getNodeEmoji(
                    getNodeType(name)
                  )}{" "}
                  {name}
                </div>

                <span
                  style={{
                    display: "block",
                    marginTop: "4px",
                    fontSize: "10px",
                    color: getTypeColor(
                      getNodeType(name)
                    ),
                  }}
                >
                  {getNodeType(name)}
                </span>
              </button>
            ))
          )}
        </div>
      )}

      {/* GRAPH */}
      <ReactFlow
        nodes={displayedNodes}
        edges={displayedEdges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onNodeClick={handleNodeClick}
        fitView
        fitViewOptions={{
          padding: 0.2,
        }}
        minZoom={0.2}
        maxZoom={2}
        defaultEdgeOptions={{
          type: "smoothstep",
        }}
      >
        <Background
          gap={25}
          size={1}
          color="#1e293b"
        />

        <Controls
          position="bottom-right"
          showInteractive={false}
        />

        <MiniMap
          position="top-right"
          nodeColor={(node) =>
            getTypeColor(
              getNodeType(node.id)
            )
          }
          maskColor="rgba(2,6,23,0.75)"
          style={{
            background: "#111827",
            border: "1px solid #374151",
            borderRadius: "14px",
          }}
        />
      </ReactFlow>

      {/* LEGEND */}
      <div
        style={{
          position: "absolute",
          bottom: "15px",
          left: "15px",
          zIndex: 20,
          background: "#111827e8",
          backdropFilter: "blur(12px)",
          border: "1px solid #374151",
          borderRadius: "14px",
          padding: "13px",
          color: "white",
          boxShadow:
            "0 10px 30px rgba(0,0,0,0.4)",
        }}
      >
        <div
          style={{
            fontWeight: "800",
            fontSize: "11px",
            marginBottom: "9px",
          }}
        >
          🎨 CLASS TYPES
        </div>

        <div
          style={{
            display: "flex",
            gap: "9px",
            flexWrap: "wrap",
            maxWidth: "470px",
          }}
        >
          {[
            "Controller",
            "Service",
            "Repository",
            "DAO",
            "DTO",
            "Model",
            "Exception",
          ].map((type) => (
            <div
              key={type}
              style={{
                display: "flex",
                alignItems: "center",
                gap: "5px",
                fontSize: "10px",
                color: "#d1d5db",
              }}
            >
              <span>
                {getNodeEmoji(type)}
              </span>

              {type}
            </div>
          ))}
        </div>
      </div>

      {/* DETAILS PANEL */}
      {selectedNode && (
        <div
          style={{
            position: "absolute",
            top: "82px",
            right: "20px",
            width: "340px",
            maxHeight: "570px",
            overflowY: "auto",
            background:
              "linear-gradient(145deg,#111827,#0f172a)",
            color: "white",
            border:
              "1px solid #475569",
            borderRadius: "20px",
            padding: "22px",
            boxShadow:
              "0 25px 60px rgba(0,0,0,0.7)",
            zIndex: 25,
            backdropFilter: "blur(15px)",
          }}
        >

          {/* CLOSE */}
          <button
            onClick={closePanel}
            style={{
              position: "absolute",
              top: "14px",
              right: "14px",
              background: "#1f2937",
              border: "1px solid #374151",
              color: "#d1d5db",
              width: "32px",
              height: "32px",
              borderRadius: "9px",
              cursor: "pointer",
            }}
          >
            ✕
          </button>

          {/* HEADER */}
          <div
            style={{
              paddingRight: "40px",
            }}
          >
            <div
              style={{
                fontSize: "28px",
                marginBottom: "7px",
              }}
            >
              {getNodeEmoji(
                selectedNode.type
              )}
            </div>

            <div
              style={{
                fontSize: "10px",
                color: "#94a3b8",
                textTransform: "uppercase",
                letterSpacing: "1px",
              }}
            >
              Selected Class
            </div>

            <h3
              style={{
                margin: "5px 0 0",
                fontSize: "20px",
                wordBreak: "break-word",
              }}
            >
              {selectedNode.name}
            </h3>
          </div>

          {/* TYPE */}
          <div
            style={{
              display: "inline-block",
              marginTop: "13px",
              padding: "6px 11px",
              borderRadius: "8px",
              background: getTypeColor(
                selectedNode.type
              ),
              fontSize: "11px",
              fontWeight: "800",
            }}
          >
            {selectedNode.type}
          </div>

          {/* STATS */}
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "1fr 1fr",
              gap: "10px",
              marginTop: "20px",
            }}
          >
            <DetailStat
              emoji="📥"
              value={
                selectedNode.dependencies.length
              }
              label="Dependencies"
            />

            <DetailStat
              emoji="📤"
              value={
                selectedNode.usedBy.length
              }
              label="Used By"
            />
          </div>

          {/* DEPENDENCIES */}
          <SectionTitle emoji="📥">
            Dependencies
          </SectionTitle>

          {selectedNode.dependencies.length === 0 ? (
            <EmptyMessage>
              💤 No dependencies
            </EmptyMessage>
          ) : (
            selectedNode.dependencies.map(
              (dependency, index) => (
                <DependencyItem
                  key={index}
                  emoji="➡️"
                  color="#3b82f6"
                  name={dependency.target}
                  relationship={
                    dependency.relationship
                  }
                />
              )
            )
          )}

          {/* USED BY */}
          <SectionTitle emoji="📤">
            Used By
          </SectionTitle>

          {selectedNode.usedBy.length === 0 ? (
            <EmptyMessage>
              💤 Nothing depends on this class
            </EmptyMessage>
          ) : (
            selectedNode.usedBy.map(
              (dependency, index) => (
                <DependencyItem
                  key={index}
                  emoji="⬅️"
                  color="#10b981"
                  name={dependency.source}
                  relationship={
                    dependency.relationship
                  }
                />
              )
            )
          )}
        </div>
      )}
    </div>
  );
}

function StatBadge({
  emoji,
  value,
  label,
}) {
  return (
    <div
      style={{
        background: "#1e293b",
        border: "1px solid #334155",
        color: "#e2e8f0",
        borderRadius: "12px",
        padding: "9px 13px",
        fontSize: "12px",
        fontWeight: "700",
      }}
    >
      {emoji} {value} {label}
    </div>
  );
}

function DetailStat({
  emoji,
  value,
  label,
}) {
  return (
    <div
      style={{
        background: "#1f2937",
        borderRadius: "12px",
        padding: "13px",
        textAlign: "center",
      }}
    >
      <div
        style={{
          fontSize: "20px",
        }}
      >
        {emoji}
      </div>

      <div
        style={{
          fontSize: "20px",
          fontWeight: "800",
          marginTop: "3px",
        }}
      >
        {value}
      </div>

      <div
        style={{
          fontSize: "10px",
          color: "#9ca3af",
        }}
      >
        {label}
      </div>
    </div>
  );
}

function SectionTitle({
  emoji,
  children,
}) {
  return (
    <h4
      style={{
        marginTop: "24px",
        marginBottom: "10px",
        fontSize: "14px",
      }}
    >
      {emoji} {children}
    </h4>
  );
}

function DependencyItem({
  emoji,
  color,
  name,
  relationship,
}) {
  return (
    <div
      style={{
        padding: "11px",
        marginBottom: "7px",
        background: "#1f2937",
        borderRadius: "10px",
        borderLeft: `3px solid ${color}`,
      }}
    >
      <div
        style={{
          fontWeight: "700",
          fontSize: "13px",
        }}
      >
        {emoji} {name}
      </div>

      <div
        style={{
          color: "#9ca3af",
          fontSize: "10px",
          marginTop: "4px",
        }}
      >
        {relationship}
      </div>
    </div>
  );
}

function EmptyMessage({ children }) {
  return (
    <p
      style={{
        color: "#9ca3af",
        fontSize: "12px",
        background: "#0f172a",
        borderRadius: "10px",
        padding: "10px",
      }}
    >
      {children}
    </p>
  );
}

export default CodeDependencyGraph;