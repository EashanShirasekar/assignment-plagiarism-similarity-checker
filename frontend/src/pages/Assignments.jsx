import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import Layout from '../components/Layout'
import { getAssignments, deleteAssignment } from '../services/assignmentService'
import { extractErrorMessage } from '../services/api'

export default function Assignments() {
  const [assignments, setAssignments] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  function load() {
    setLoading(true)
    getAssignments()
      .then(setAssignments)
      .catch((err) => setError(extractErrorMessage(err, 'Could not load assignments')))
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  async function handleDelete(id) {
    if (!window.confirm('Delete this assignment and all of its submissions? This cannot be undone.')) {
      return
    }
    try {
      await deleteAssignment(id)
      load()
    } catch (err) {
      setError(extractErrorMessage(err, 'Could not delete assignment'))
    }
  }

  return (
    <Layout>
      <div className="page-header">
        <div>
          <h1>Assignments</h1>
          <p>Assignments you have created.</p>
        </div>
        <Link className="btn btn-primary" to="/teacher/assignments/new">Create Assignment</Link>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading && <p className="text-muted">Loading...</p>}

      {!loading && assignments.length === 0 && (
        <div className="card empty-state">You haven't created any assignments yet.</div>
      )}

      <div className="assignment-list">
        {assignments.map((a) => (
          <div className="assignment-row" key={a.id}>
            <div>
              <h3>{a.title}</h3>
              <div className="assignment-meta">
                <span>{a.submissionCount} submission{a.submissionCount === 1 ? '' : 's'}</span>
                <span>Due {new Date(a.deadline).toLocaleString()}</span>
              </div>
            </div>
            <div className="assignment-row-actions">
              <Link className="btn btn-secondary btn-sm" to={`/teacher/assignments/${a.id}`}>Open</Link>
              <button className="btn btn-danger btn-sm" onClick={() => handleDelete(a.id)}>Delete</button>
            </div>
          </div>
        ))}
      </div>
    </Layout>
  )
}
