import React, { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import Layout from '../components/Layout'
import { getAssignment } from '../services/assignmentService'
import { submitAssignment } from '../services/submissionService'
import { extractErrorMessage } from '../services/api'

export default function SubmitAssignment() {
  const { id } = useParams()
  const navigate = useNavigate()

  const [assignment, setAssignment] = useState(null)
  const [content, setContent] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    getAssignment(id)
      .then((data) => {
        setAssignment(data)
        if (data.submittedByCurrentStudent) {
          navigate(`/student/assignments/${id}`, { replace: true })
        }
      })
      .catch((err) => setError(extractErrorMessage(err, 'Could not load this assignment')))
  }, [id, navigate])

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    if (!content.trim()) {
      setError('Submission content cannot be empty.')
      return
    }
    setSubmitting(true)
    try {
      await submitAssignment({ assignmentId: Number(id), content })
      navigate(`/student/assignments/${id}`)
    } catch (err) {
      setError(extractErrorMessage(err, 'Could not submit your assignment'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Layout>
      <div className="page-header">
        <div>
          <h1>Submit Assignment</h1>
          {assignment && <p>{assignment.title} · due {new Date(assignment.deadline).toLocaleString()}</p>}
        </div>
      </div>

      <div className="card" style={{ maxWidth: 700 }}>
        {error && <div className="alert alert-error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="content">Your answer</label>
            <textarea
              id="content"
              style={{ minHeight: 240 }}
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="Type or paste your assignment answer here..."
            />
            <div className="field-hint">
              This text is compared against your classmates' submissions to check for high textual similarity.
            </div>
          </div>
          <button className="btn btn-primary" type="submit" disabled={submitting}>
            {submitting ? 'Submitting...' : 'Submit'}
          </button>
        </form>
      </div>
    </Layout>
  )
}
