import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import Layout from '../components/Layout'
import { getAssignments } from '../services/assignmentService'
import { extractErrorMessage } from '../services/api'

export default function StudentDashboard() {
  const [assignments, setAssignments] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getAssignments()
      .then(setAssignments)
      .catch((err) => setError(extractErrorMessage(err, 'Could not load assignments')))
      .finally(() => setLoading(false))
  }, [])

  return (
    <Layout>
      <div className="page-header">
        <div>
          <h1>My Assignments</h1>
          <p>Assignments available to you across your courses.</p>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {loading && <p className="text-muted">Loading...</p>}

      {!loading && assignments.length === 0 && (
        <div className="card empty-state">No assignments have been posted yet.</div>
      )}

      <div className="assignment-list">
        {assignments.map((a) => {
          const isPastDeadline = new Date(a.deadline) < new Date()
          return (
            <div className="assignment-row" key={a.id}>
              <div>
                <h3>{a.title}</h3>
                <div className="assignment-meta">
                  <span>By {a.teacherName}</span>
                  <span>Due {new Date(a.deadline).toLocaleString()}</span>
                  {a.submittedByCurrentStudent ? (
                    <span className="badge badge-low">Submitted</span>
                  ) : isPastDeadline ? (
                    <span className="badge badge-high">Deadline passed</span>
                  ) : (
                    <span className="badge badge-neutral">Not submitted</span>
                  )}
                </div>
              </div>
              <div className="assignment-row-actions">
                <Link className="btn btn-secondary btn-sm" to={`/student/assignments/${a.id}`}>Open</Link>
              </div>
            </div>
          )
        })}
      </div>
    </Layout>
  )
}
