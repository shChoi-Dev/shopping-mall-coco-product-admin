import React from 'react';
import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { logout } from '../../utils/api';
import '../../css/admin/AdminLayout.css';

function AdminLayout() {
  const location = useLocation();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  // 현재 메뉴 활성화 확인
  const isActive = (path) => {
    if (path === '/admin') {
      return location.pathname === '/admin';
    }
    return location.pathname.startsWith(path);
  };

  return (
    <div className="admin-wrapper">
      {/* 사이드바 */}
      <nav className="admin-sidebar">
        <h1 className="admin-logo"><Link to="/admin/products">Coco 관리자</Link></h1>
        <ul className="admin-nav-list">
          <li>
            <Link to="/admin/products" className={`admin-nav-item ${isActive('/admin/products') ? 'active' : ''}`}>
              상품 관리
            </Link>
          </li>
          <li>
            <Link to="/admin/categories" className={`admin-nav-item ${isActive('/admin/categories') ? 'active' : ''}`}>
              카테고리 관리
            </Link>
          </li>
          <li>
            <Link to="/admin/members" className={`admin-nav-item ${isActive('/admin/members') ? 'active' : ''}`}>
              회원 관리
            </Link>
          </li>
          <li>
            <Link to="/admin/orders" className={`admin-nav-item ${isActive('/admin/orders') ? 'active' : ''}`}>
              주문 관리
            </Link>
          </li>
          <li>
            <Link to="/admin/notices" className={`admin-nav-item ${isActive('/admin/notices') ? 'active' : ''}`}>
              공지사항 관리
            </Link>
          </li>
        </ul>
      </nav>

      {/* 우측 메인 영역 */}
      <div className="admin-page-wrapper">
        <header className="admin-top-header">
          <Link to="/" className="header-link-btn">
            🏠 쇼핑몰 메인
          </Link>
          <span className="admin-welcome-msg">관리자(admin)님</span>
          <button onClick={handleLogout} className="header-logout-btn">
            로그아웃
          </button>
        </header>

        <main className="admin-content">
          <Outlet />
        </main>

        {/* 관리자용 미니 Footer */}
        <footer style={{ 
            textAlign: 'center', 
            padding: '20px', 
            color: '#888', 
            fontSize: '12px',
            borderTop: '1px solid #eee' 
        }}>
          Copyright © 2025 Coco Admin System. All rights reserved.
        </footer>

      </div>
      <ToastContainer autoClose={2000} position="bottom-right" />
    </div>
  );
}

export default AdminLayout;