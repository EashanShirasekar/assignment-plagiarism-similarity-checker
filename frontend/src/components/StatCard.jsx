import React from 'react'

export default function StatCard({ label, value, highlight }) {
  return (
    <div className="stat-card">
      <div className="stat-label">{label}</div>
      <div className={highlight ? 'stat-value high' : 'stat-value'}>{value}</div>
    </div>
  )
}
