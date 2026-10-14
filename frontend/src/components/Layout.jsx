import React from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Layout({ children }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  const isTeacher = user?.role === 'TEACHER'

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-brand">Similarity<span>Check</span></div>
        <div className="sidebar-subtitle"></div>

        {isTeacher ? (
          <>
            <NavLink to="/teacher/dashboard" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>Dashboard</NavLink>
            <NavLink to="/teacher/assignments" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>Assignments</NavLink>
            <NavLink to="/teacher/assignments/new" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>Create Assignment</NavLink>
          </>
        ) : (
          <>
            <NavLink to="/student/dashboard" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>My Assignments</NavLink>
          </>
        )}

        <div className="sidebar-footer">
          <div className="sidebar-user">{user?.name}</div>
          <div className="sidebar-role">{user?.role?.toLowerCase()}</div>
          <button className="btn btn-secondary btn-sm btn-block" onClick={handleLogout}>Log out</button>
        </div>
      </aside>

      <main className="main-content">{children}</main>
    </div>
  )
}
