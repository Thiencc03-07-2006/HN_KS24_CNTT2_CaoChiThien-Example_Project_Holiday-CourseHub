import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import { Spin } from 'antd';
import { LoadingOutlined } from '@ant-design/icons';

// Auth Pages
import Login from './pages/Login';
import Register from './pages/Register';
import VerifyOtp from './pages/VerifyOtp';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';

// Student / Public Pages
import HomePage from './pages/HomePage';
import CourseCatalog from './pages/CourseCatalog';
import CourseDetail from './pages/CourseDetail';
import StudentDashboard from './pages/StudentDashboard';
import UserProfile from './pages/UserProfile';
import StudentLayout from './pages/StudentLayout';
import WishlistPage from './pages/WishlistPage';
import FavoritePage from './pages/FavoritePage';
import LearningPage from './pages/LearningPage';
import QuizPage from './pages/QuizPage';
import InstructorProfilePage from './pages/InstructorProfilePage';

// Instructor Pages
import InstructorDashboardPage from './pages/InstructorDashboardPage';
import InstructorCourseManagementPage from './pages/InstructorCourseManagementPage';
import InstructorChapterManagementPage from './pages/InstructorChapterManagementPage';
import InstructorLessonManagementPage from './pages/InstructorLessonManagementPage';
import InstructorProfileSettingsPage from './pages/InstructorProfileSettingsPage';
import InstructorReviews from './pages/InstructorReviews';

// Admin Pages
import AdminLayout from './pages/AdminLayout';
import AdminDashboard from './pages/AdminDashboard';
import AdminUsers from './pages/AdminUsers';
import AdminCategories from './pages/AdminCategories';
import AdminCourses from './pages/AdminCourses';
import AdminReviews from './pages/AdminReviews';
import AdminReportsPage from './pages/AdminReportsPage';
import AdminStatisticsPage from './pages/AdminStatisticsPage';

// Error Pages
import { PageNotFound, AccessDenied, ServerError } from './pages/ErrorPages';

// Route Guard for logged-in users
const AuthGuard: React.FC<{ children: React.ReactNode; allowedRoles?: string[] }> = ({
  children,
  allowedRoles,
}) => {
  const { user, loading } = useAuth();

  if (loading) {
    const antIcon = <LoadingOutlined style={{ fontSize: 32, color: 'var(--primary-color)' }} spin />;
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: '#FFFFFF' }}>
        <Spin indicator={antIcon} description="Đang tải CourseHub..." />
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !user.roles.some((role) => allowedRoles.includes(role))) {
    return <Navigate to="/403" replace />;
  }

  return <>{children}</>;
};

// Route Guard for public-only auth pages (redirect to dashboard if logged in)
const PublicOnlyRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, loading } = useAuth();

  if (loading) {
    const antIcon = <LoadingOutlined style={{ fontSize: 32, color: 'var(--primary-color)' }} spin />;
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: '#FFFFFF' }}>
        <Spin indicator={antIcon} />
      </div>
    );
  }

  if (user) {
    if (user.roles.includes('ROLE_ADMIN')) {
      return <Navigate to="/admin/dashboard" replace />;
    }
    return <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
};

const AppRouter: React.FC = () => {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public Catalog Routes */}
        <Route path="/" element={<HomePage />} />
        <Route path="/courses" element={<CourseCatalog />} />
        <Route path="/courses/:slug" element={<CourseDetail />} />
        <Route path="/instructors/public/:instructorId" element={<InstructorProfilePage />} />

        {/* Guest-only Auth Routes */}
        <Route
          path="/login"
          element={
            <PublicOnlyRoute>
              <Login />
            </PublicOnlyRoute>
          }
        />
        <Route
          path="/register"
          element={
            <PublicOnlyRoute>
              <Register />
            </PublicOnlyRoute>
          }
        />
        <Route
          path="/verify-otp"
          element={
            <PublicOnlyRoute>
              <VerifyOtp />
            </PublicOnlyRoute>
          }
        />
        <Route
          path="/forgot-password"
          element={
            <PublicOnlyRoute>
              <ForgotPassword />
            </PublicOnlyRoute>
          }
        />
        <Route
          path="/reset-password"
          element={
            <PublicOnlyRoute>
              <ResetPassword />
            </PublicOnlyRoute>
          }
        />

        {/* Student Protected Routes */}
        <Route
          path="/"
          element={
            <AuthGuard allowedRoles={['ROLE_STUDENT', 'ROLE_INSTRUCTOR']}>
              <StudentLayout />
            </AuthGuard>
          }
        >
          <Route path="dashboard" element={<StudentDashboard />} />
          <Route path="my-courses" element={<StudentDashboard />} />
          <Route path="profile" element={<UserProfile />} />
          <Route path="wishlist" element={<WishlistPage />} />
          <Route path="favorites" element={<FavoritePage />} />
        </Route>

        {/* Learning Player & Quiz are Full Screen/No layout pages */}
        <Route
          path="/learning/course/:courseId"
          element={
            <AuthGuard allowedRoles={['ROLE_STUDENT', 'ROLE_INSTRUCTOR']}>
              <LearningPage />
            </AuthGuard>
          }
        />
        <Route
          path="/quizzes/:quizId/take"
          element={
            <AuthGuard allowedRoles={['ROLE_STUDENT', 'ROLE_INSTRUCTOR']}>
              <QuizPage />
            </AuthGuard>
          }
        />

        {/* Instructor Protected Routes */}
        <Route
          path="/instructor"
          element={
            <AuthGuard allowedRoles={['ROLE_INSTRUCTOR']}>
              <StudentLayout />
            </AuthGuard>
          }
        >
          <Route index element={<Navigate to="dashboard" replace />} />
          <Route path="dashboard" element={<InstructorDashboardPage />} />
          <Route path="courses" element={<InstructorCourseManagementPage />} />
          <Route path="courses/:courseId/chapters" element={<InstructorChapterManagementPage />} />
          <Route path="courses/:courseId/chapters/:chapterId/lessons" element={<InstructorLessonManagementPage />} />
          <Route path="reviews" element={<InstructorReviews />} />
          <Route path="profile" element={<InstructorProfileSettingsPage />} />
        </Route>

        {/* Admin Protected Routes */}
        <Route
          path="/admin"
          element={
            <AuthGuard allowedRoles={['ROLE_ADMIN']}>
              <AdminLayout />
            </AuthGuard>
          }
        >
          <Route index element={<Navigate to="dashboard" replace />} />
          <Route path="dashboard" element={<AdminDashboard />} />
          <Route path="users" element={<AdminUsers />} />
          <Route path="categories" element={<AdminCategories />} />
          <Route path="courses" element={<AdminCourses />} />
          <Route path="reviews" element={<AdminReviews />} />
          <Route path="reports" element={<AdminReportsPage />} />
          <Route path="statistics" element={<AdminStatisticsPage />} />
        </Route>

        {/* Error Routes */}
        <Route path="/403" element={<AccessDenied />} />
        <Route path="/500" element={<ServerError />} />
        <Route path="/404" element={<PageNotFound />} />
        <Route path="*" element={<PageNotFound />} />
      </Routes>
    </BrowserRouter>
  );
};

export default AppRouter;
