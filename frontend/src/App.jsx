import React from 'react'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import PrivateRoute from './components/PrivateRoute'

import Login from './pages/Login'
import Register from './pages/Register'
import TeacherDashboard from './pages/TeacherDashboard'
import StudentDashboard from './pages/StudentDashboard'
import Assignments from './pages/Assignments'
import CreateAssignment from './pages/CreateAssignment'
import AssignmentDetails from './pages/AssignmentDetails'
import SubmitAssignment from './pages/SubmitAssignment'
import Submissions from './pages/Submissions'
import SimilarityResults from './pages/SimilarityResults'
import Comparison from './pages/Comparison'

function HomeRedirect() {
  const { user, loading } = useAuth()
  if (loading) return null
  if (!user) return <Navigate to="/login" replace />
  return <Navigate to={user.role === 'TEACHER' ? '/teacher/dashboard' : '/student/dashboard'} replace />
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<HomeRedirect />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Teacher routes */}
          <Route path="/teacher/dashboard" element={<PrivateRoute role="TEACHER"><TeacherDashboard /></PrivateRoute>} />
          <Route path="/teacher/assignments" element={<PrivateRoute role="TEACHER"><Assignments /></PrivateRoute>} />
          <Route path="/teacher/assignments/new" element={<PrivateRoute role="TEACHER"><CreateAssignment /></PrivateRoute>} />
          <Route path="/teacher/assignments/:id" element={<PrivateRoute role="TEACHER"><AssignmentDetails /></PrivateRoute>} />
          <Route path="/teacher/assignments/:id/submissions" element={<PrivateRoute role="TEACHER"><Submissions /></PrivateRoute>} />
          <Route path="/teacher/assignments/:id/results" element={<PrivateRoute role="TEACHER"><SimilarityResults /></PrivateRoute>} />
          <Route path="/teacher/results/:resultId/compare" element={<PrivateRoute role="TEACHER"><Comparison /></PrivateRoute>} />

          {/* Student routes */}
          <Route path="/student/dashboard" element={<PrivateRoute role="STUDENT"><StudentDashboard /></PrivateRoute>} />
          <Route path="/student/assignments/:id" element={<PrivateRoute role="STUDENT"><AssignmentDetails /></PrivateRoute>} />
          <Route path="/student/assignments/:id/submit" element={<PrivateRoute role="STUDENT"><SubmitAssignment /></PrivateRoute>} />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}
