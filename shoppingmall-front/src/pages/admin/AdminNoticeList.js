import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchWithAuth } from '../../utils/api';
import '../../css/admin/AdminProductList.css';

const AdminNoticeList = () => {
    const [notices, setNotices] = useState([]);
    const navigate = useNavigate();

    useEffect(() => {
        loadNotices();
    }, []);

    const loadNotices = async () => {
        try {
            const response = await fetch(`${process.env.REACT_APP_API_URL}/api/notices`);
            if (response.ok) {
                const data = await response.json();
                const sortedData = data.sort((a, b) => b.noticeNo - a.noticeNo); // 최신글이 위로
                setNotices(sortedData);
            }
        } catch (error) {
            console.error('공지사항 로딩 실패:', error);
        }
    };

    const handleDelete = async (noticeNo) => {
        if (window.confirm('정말 이 공지사항을 삭제하시겠습니까?')) {
            try {
                const response = await fetchWithAuth(`/admin/notices/${noticeNo}`, {
                    method: 'DELETE'
                });
                if (response.ok) {
                    alert('삭제되었습니다.');
                    loadNotices();
                } else {
                    alert('삭제에 실패했습니다.');
                }
            } catch (error) {
                console.error('삭제 에러:', error);
            }
        }
    };

return (
    <div className="admin-layout">
      <div className="admin-content">
        
        {/* 공지사항 메인 타이틀 */}
        <div style={{ marginBottom: '20px', borderBottom: '1px solid #333', paddingBottom: '15px' }}>
          <h2 style={{ margin: 0, fontSize: '24px' }}>공지사항 관리</h2>
        </div>

        {/* 공지사항 목록 */}
        <div style={{ backgroundColor: '#fff', padding: '30px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>
          
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
            <h3 style={{ margin: 0, fontSize: '18px' }}>공지사항 목록</h3>
            <button 
              onClick={() => navigate('/admin/notices/new')}
              style={{ padding: '8px 16px', backgroundColor: '#333', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
            >
              + 공지사항 작성
            </button>
          </div>

          <table className="product-table" style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'center' }}>
            <thead style={{ backgroundColor: '#f9f9f9', borderTop: '2px solid #333', borderBottom: '1px solid #ddd' }}>
              <tr>
                <th style={{ padding: '12px 15px', width: '10%' }}>번호</th>
                <th style={{ width: '45%' }}>제목</th>
                <th style={{ width: '15%' }}>작성일</th>
                <th style={{ width: '15%' }}>조회수</th>
                <th style={{ width: '15%' }}>관리</th>
              </tr>
            </thead>
            <tbody>
              {notices.length === 0 ? (
                <tr><td colSpan="5" style={{ padding: '50px', color: '#666' }}>등록된 공지사항이 없습니다.</td></tr>
              ) : (
                notices.map((notice) => (
                  <tr key={notice.noticeNo} style={{ borderBottom: '1px solid #eee' }}>
                    <td style={{ padding: '15px' }}>{notice.noticeNo}</td>
                    <td style={{ textAlign: 'left', fontWeight: '500' }}>{notice.title}</td>
                    <td style={{ color: '#555' }}>{new Date(notice.createdAt).toLocaleDateString()}</td>
                    <td style={{ color: '#555' }}>{notice.viewCount}</td>
                    <td>
                      <button 
                        onClick={() => handleDelete(notice.noticeNo)}
                        style={{ padding: '6px 12px', backgroundColor: '#fff', color: '#ff4d4f', border: '1px solid #ff4d4f', borderRadius: '4px', cursor: 'pointer', fontSize: '13px' }}
                      >삭제</button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

      </div>
    </div>
  );
};

export default AdminNoticeList;