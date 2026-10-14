import React, { useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import Layout from '../components/Layout'
import SimilarityBadge from '../components/SimilarityBadge'
import { getAssignment } from '../services/assignmentService'
import { getResults, analyzeAssignment } from '../services/analysisService'
import { extractErrorMessage } from '../services/api'

const FILTERS = [
  { key: 'ALL', label: 'All' },
  { key: 'HIGH', label: 'High' },
  { key: 'MODERATE', label: 'Moderate' },
  { key: 'LOW', label: 'Low' },
]

export default function SimilarityResults() {
  const { id } = useParams()
  const [assignment, setAssignment] = useState(null)
  const [results, setResults] = useState([])
  const [filter, setFilter] = useState('ALL')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [analyzing, setAnalyzing] = useState(false)

  function load() {
    setLoading(true)
    Promise.all([getAssignment(id), getResults(id)])
      .then(([assignmentData, resultData]) => {
        setAssignment(assignmentData)
        setResults(resultData)
      })
      .catch((err) => setError(extractErrorMessage(err, 'Could not load similarity results')))
      .finally(() => setLoading(false))
  }

  useEffect(load, [id])

  async function handleReanalyze() {
    setAnalyzing(true)
    setError('')
    try {
      const fresh = await analyzeAssignment(id)
      setResults(fresh)
    } catch (err) {
      setError(extractErrorMessage(err, 'Could not run the similarity analysis'))
    } finally {
      setAnalyzing(false)
    }
  }

  const filtered = useMemo(() => {
    if (filter === 'ALL') return results
    return results.filter((r) => r.similarityLevel === filter)
  }, [results, filter])

  return (
    <Layout>
      <div className="page-header">
        <div>
          <h1>Similarity Results</h1>
          {assignment && <p>{assignment.title}</p>}
        </div>
        <button className="btn btn-primary" onClick={handleReanalyze} disabled={analyzing}>
          {analyzing ? 'Re-analyzing...' : 'Re-run Analysis'}
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {loading && <p className="text-muted">Loading...</p>}

      {!loading && results.length === 0 && (
        <div className="card empty-state">
          No results yet. Go back to the assignment and click "Analyze Similarity" once there are at least 2 submissions.
        </div>
      )}

      {!loading && results.length > 0 && (
        <div className="card">
          <div className="filter-row">
            {FILTERS.map((f) => (
              <button
                key={f.key}
                className={`filter-chip${filter === f.key ? ' active' : ''}`}
                onClick={() => setFilter(f.key)}
              >
                {f.label}
              </button>
            ))}
          </div>

          <table className="data-table">
            <thead>
              <tr>
                <th>Student 1</th>
                <th>Student 2</th>
                <th>Similarity</th>
                <th>Level</th>
                <th>Flagged</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((r) => (
                <tr key={r.id}>
                  <td>{r.student1Name}</td>
                  <td>{r.student2Name}</td>
                  <td>{r.similarityPercentage.toFixed(0)}%</td>
                  <td><SimilarityBadge level={r.similarityLevel} /></td>
                  <td>{r.flagged ? <span className="badge badge-flagged">Yes</span> : <span className="text-faint">No</span>}</td>
                  <td><Link to={`/teacher/results/${r.id}/compare`}>View</Link></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Layout>
  )
}
