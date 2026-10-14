import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import Layout from '../components/Layout'
import StatCard from '../components/StatCard'
import SimilarityBadge from '../components/SimilarityBadge'
import { getAssignments } from '../services/assignmentService'
import { getTeacherDashboardStats, getResults } from '../services/analysisService'

export default function TeacherDashboard() {
  const [stats, setStats] = useState(null)
  const [recentResults, setRecentResults] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false

    async function load() {
      try {
        const [statsData, assignments] = await Promise.all([
          getTeacherDashboardStats(),
          getAssignments(),
        ])

        // Pull a handful of the most recent similarity results across all
        // of this teacher's assignments for the "Recent Similarity Results" panel.
        const resultLists = await Promise.all(
          assignments.map((a) => getResults(a.id).catch(() => []))
        )
        const merged = resultLists
          .flat()
          .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
          .slice(0, 5)

        if (!cancelled) {
          setStats(statsData)
          setRecentResults(merged)
        }
      } catch (err) {
        if (!cancelled) setError('Could not load dashboard data.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => { cancelled = true }
  }, [])

  return (
    <Layout>
      <div className="page-header">
        <div>
          <h1>Teacher Dashboard</h1>
          <p>Overview of your assignments and similarity checks.</p>
        </div>
        <Link className="btn btn-primary" to="/teacher/assignments/new">Create Assignment</Link>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {!loading && stats && (
        <div className="stat-grid">
          <StatCard label="Assignments" value={stats.totalAssignments} />
          <StatCard label="Submissions" value={stats.totalSubmissions} />
          <StatCard label="Flagged Pairs" value={stats.flaggedPairs} highlight={stats.flaggedPairs > 0} />
          <StatCard label="Highest Similarity" value={`${stats.highestSimilarity.toFixed(0)}%`} highlight={stats.highestSimilarity >= 70} />
        </div>
      )}

      <div className="card">
        <h2>Recent Similarity Results</h2>
        {loading && <p className="text-muted">Loading...</p>}
        {!loading && recentResults.length === 0 && (
          <div className="empty-state">
            No similarity results yet. Open an assignment and click "Analyze Similarity" once students have submitted their work.
          </div>
        )}
        {!loading && recentResults.length > 0 && (
          <table className="data-table">
            <thead>
              <tr>
                <th>Students</th>
                <th>Similarity</th>
                <th>Level</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {recentResults.map((r) => (
                <tr key={r.id}>
                  <td>{r.student1Name} &harr; {r.student2Name}</td>
                  <td>{r.similarityPercentage.toFixed(0)}%</td>
                  <td><SimilarityBadge level={r.similarityLevel} /></td>
                  <td><Link to={`/teacher/results/${r.id}/compare`}>View</Link></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </Layout>
  )
}
