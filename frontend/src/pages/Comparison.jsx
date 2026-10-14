import React, { useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import Layout from '../components/Layout'
import SimilarityBadge from '../components/SimilarityBadge'
import { getComparison } from '../services/analysisService'
import { extractErrorMessage } from '../services/api'

/**
 * Wraps words that appear in `terms` with <mark> so the teacher can quickly
 * spot the overlapping vocabulary between the two submissions. This is a
 * simple, readable highlight - not a full diff algorithm - by design.
 */
function highlight(text, terms) {
  if (!terms || terms.length === 0) {
    return text
  }
  const termSet = new Set(terms.map((t) => t.toLowerCase()))
  const parts = text.split(/(\s+)/)

  return parts.map((part, idx) => {
    const cleaned = part.toLowerCase().replace(/[^a-z0-9]/g, '')
    if (cleaned && termSet.has(cleaned)) {
      return <mark className="match" key={idx}>{part}</mark>
    }
    return <React.Fragment key={idx}>{part}</React.Fragment>
  })
}

const LEVEL_CLASS = { LOW: 'low', MODERATE: 'moderate', HIGH: 'high' }

export default function Comparison() {
  const { resultId } = useParams()
  const [comparison, setComparison] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    getComparison(resultId)
      .then(setComparison)
      .catch((err) => setError(extractErrorMessage(err, 'Could not load this comparison')))
      .finally(() => setLoading(false))
  }, [resultId])

  const highlighted1 = useMemo(
    () => comparison && highlight(comparison.content1, comparison.commonSignificantTerms),
    [comparison]
  )
  const highlighted2 = useMemo(
    () => comparison && highlight(comparison.content2, comparison.commonSignificantTerms),
    [comparison]
  )

  return (
    <Layout>
      {loading && <p className="text-muted">Loading...</p>}
      {error && <div className="alert alert-error">{error}</div>}

      {comparison && (
        <>
          <div className="page-header">
            <div>
              <h1>Comparison</h1>
              <p>{comparison.assignmentTitle}</p>
            </div>
            <Link className="btn btn-secondary" to={`/teacher/assignments/${comparison.assignmentId}/results`}>Back to results</Link>
          </div>

          <div className="comparison-header">
            <div className={`similarity-dial ${LEVEL_CLASS[comparison.similarityLevel]}`}>
              <span className="value">{comparison.similarityPercentage.toFixed(0)}%</span>
              <SimilarityBadge level={comparison.similarityLevel} />
            </div>
            <div className="text-muted">
              {comparison.student1Name} &harr; {comparison.student2Name}
            </div>
          </div>

          <div className="disclaimer-box">{comparison.disclaimer}</div>

          <div className="compare-grid">
            <div className="card submission-pane">
              <h4>{comparison.student1Name}'s Submission</h4>
              <div className="submission-text">{highlighted1}</div>
            </div>
            <div className="card submission-pane">
              <h4>{comparison.student2Name}'s Submission</h4>
              <div className="submission-text">{highlighted2}</div>
            </div>
          </div>
        </>
      )}
    </Layout>
  )
}
