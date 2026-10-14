import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Layout from '../components/Layout'
import { createAssignment } from '../services/assignmentService'
import { extractErrorMessage } from '../services/api'

export default function CreateAssignment() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ title: '', description: '', deadline: '' })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      const assignment = await createAssignment({
        title: form.title,
        description: form.description,
        deadline: new Date(form.deadline).toISOString().slice(0, 19),
      })
      navigate(`/teacher/assignments/${assignment.id}`)
    } catch (err) {
      setError(extractErrorMessage(err, 'Could not create assignment'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Layout>
      <div className="page-header">
        <div>
          <h1>Create Assignment</h1>
          <p>Students will be able to see and submit to this assignment immediately.</p>
        </div>
      </div>

      <div className="card" style={{ maxWidth: 560 }}>
        {error && <div className="alert alert-error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="title">Title</label>
            <input
              id="title"
              required
              value={form.title}
              onChange={(e) => setForm({ ...form, title: e.target.value })}
              placeholder="e.g. Introduction to Object-Oriented Programming"
            />
          </div>
          <div className="field">
            <label htmlFor="description">Description / instructions</label>
            <textarea
              id="description"
              required
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
              placeholder="Describe what students need to write about..."
            />
          </div>
          <div className="field">
            <label htmlFor="deadline">Deadline</label>
            <input
              id="deadline"
              type="datetime-local"
              required
              value={form.deadline}
              onChange={(e) => setForm({ ...form, deadline: e.target.value })}
            />
            <div className="field-hint">Must be a date and time in the future.</div>
          </div>
          <button className="btn btn-primary" type="submit" disabled={submitting}>
            {submitting ? 'Creating...' : 'Create Assignment'}
          </button>
        </form>
      </div>
    </Layout>
  )
}
