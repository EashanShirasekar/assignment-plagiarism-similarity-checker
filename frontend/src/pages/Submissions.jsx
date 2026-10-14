import React, { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import Layout from '../components/Layout'
import { getAssignment } from '../services/assignmentService'
import { getSubmissionsForAssignment } from '../services/submissionService'
import { extractErrorMessage } from '../services/api'

export default function Submissions() {
  const { id } = useParams()
  const [assignment, setAssignment] = useState(null)
  const [submissions, setSubmissions] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    Promise.all([getAssignment(id), getSubmissionsForAssignment(id)])
      .then(([assignmentData, submissionData]) => {
        setAssignment(assignmentData)
        setSubmissions(submissionData)
      })
      .catch((err) => setError(extractErrorMessage(err, 'Could not load submissions')))
      .finally(() => setLoading(false))
  }, [id])

  return (
    <Layout>
      <div className="page-header">
        <div>
          <h1>Submissions</h1>
          {assignment && <p>{assignment.title}</p>}
        </div>
        <Link className="btn btn-secondary" to={`/teacher/assignments/${id}`}>Back to assignment</Link>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {loading && <p className="text-muted">Loading...</p>}

      {!loading && submissions.length === 0 && (
        <div className="card empty-state">No submissions yet.</div>
      )}

      <div className="assignment-list">
        {submissions.map((s) => (
          <div className="card" key={s.id}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
              <h3 style={{ margin: 0 }}>{s.studentName}</h3>
              <span className="text-faint" style={{ fontSize: '0.8rem' }}>
                Submitted {new Date(s.submittedAt).toLocaleString()}
              </span>
            </div>
            <p style={{ whiteSpace: 'pre-wrap', margin: 0 }}>{s.content}</p>
          </div>
        ))}
      </div>
    </Layout>
  )
}
