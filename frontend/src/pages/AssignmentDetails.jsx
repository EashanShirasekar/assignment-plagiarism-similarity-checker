import React, { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import Layout from '../components/Layout'
import { useAuth } from '../context/AuthContext'
import { getAssignment } from '../services/assignmentService'
import { analyzeAssignment } from '../services/analysisService'
import { extractErrorMessage } from '../services/api'

export default function AssignmentDetails() {
  const { id } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()

  const [assignment, setAssignment] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [analyzing, setAnalyzing] = useState(false)

  useEffect(() => {
    setLoading(true)
    getAssignment(id)
      .then(setAssignment)
      .catch((err) => setError(extractErrorMessage(err, 'Could not load this assignment')))
      .finally(() => setLoading(false))
  }, [id])

  async function handleAnalyze() {
    setAnalyzing(true)
    setError('')
    try {
      await analyzeAssignment(id)
      navigate(`/teacher/assignments/${id}/results`)
    } catch (err) {
      setError(extractErrorMessage(err, 'Could not run the similarity analysis'))
    } finally {
      setAnalyzing(false)
    }
  }

  const isTeacher = user?.role === 'TEACHER'
  const isPastDeadline = assignment && new Date(assignment.deadline) < new Date()

  return (
    <Layout>
      {loading && <p className="text-muted">Loading...</p>}
      {error && <div className="alert alert-error">{error}</div>}

      {assignment && (
        <>
          <div className="page-header">
            <div>
              <h1>{assignment.title}</h1>
              <p>
                Due {new Date(assignment.deadline).toLocaleString()}
                {isPastDeadline ? ' · Deadline has passed' : ''}
                {isTeacher ? ` · by ${assignment.teacherName}` : ''}
              </p>
            </div>
          </div>

          <div className="card">
            <h2>Description</h2>
            <p>{assignment.description}</p>
          </div>

          {isTeacher ? (
            <div className="card">
              <h2>Teacher actions</h2>
              <p className="text-muted">
                {assignment.submissionCount} submission{assignment.submissionCount === 1 ? '' : 's'} received so far.
              </p>
              <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
                <Link className="btn btn-secondary" to={`/teacher/assignments/${id}/submissions`}>View Submissions</Link>
                <Link className="btn btn-secondary" to={`/teacher/assignments/${id}/results`}>View Similarity Results</Link>
                <button
                  className="btn btn-primary"
                  onClick={handleAnalyze}
                  disabled={analyzing || assignment.submissionCount < 2}
                  title={assignment.submissionCount < 2 ? 'Need at least 2 submissions to analyze' : ''}
                >
                  {analyzing ? 'Analyzing...' : 'Analyze Similarity'}
                </button>
              </div>
              {assignment.submissionCount < 2 && (
                <p className="field-hint" style={{ marginTop: 10 }}>
                  At least 2 submissions are needed before you can run a similarity analysis.
                </p>
              )}
            </div>
          ) : (
            <div className="card">
              <h2>Your submission</h2>
              {assignment.submittedByCurrentStudent ? (
                <div className="alert alert-success">You have already submitted this assignment.</div>
              ) : isPastDeadline ? (
                <div className="alert alert-error">The deadline for this assignment has passed. Submissions are closed.</div>
              ) : (
                <Link className="btn btn-primary" to={`/student/assignments/${id}/submit`}>Submit Assignment</Link>
              )}
            </div>
          )}
        </>
      )}
    </Layout>
  )
}
