import React from 'react'

const LABELS = {
  LOW: 'LOW SIMILARITY',
  MODERATE: 'MODERATE SIMILARITY',
  HIGH: 'HIGH SIMILARITY',
}

const CLASSES = {
  LOW: 'badge badge-low',
  MODERATE: 'badge badge-moderate',
  HIGH: 'badge badge-high',
}

export default function SimilarityBadge({ level }) {
  return <span className={CLASSES[level] || 'badge badge-neutral'}>{LABELS[level] || level}</span>
}
