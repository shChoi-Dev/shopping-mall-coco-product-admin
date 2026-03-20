import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchWithAuth } from '../../utils/api';
import '../../css/admin/AdminProductList.css';
import Spinner from '../../components/admin/Spinner';
import Pagination from '../../components/admin/Pagination';

const AdminNoticeList = () => {
    const [notices, setNotices] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    // 페이지네이션 추가
    const [currentPage, setCurrentPage] = useState(1);
    const itemsPerPage = 10; // 한 페이지당 보여줄 글 개수

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
        } finally {
            setLoading(false); // 통신이 끝나면 무조건 로딩 끄기
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

    const indexOfLastItem = currentPage * itemsPerPage;
    const indexOfFirstItem = indexOfLastItem - itemsPerPage;
    const currentNotices = notices.slice(indexOfFirstItem, indexOfLastItem); // 현재 페이지에 보여줄 데이터
    const totalPages = Math.ceil(notices.length / itemsPerPage);

    const handlePageChange = (pageNumber) => {
        setCurrentPage(pageNumber);
    };

    // 로딩 중일 때 Spinner 컴포넌트 출력
    if (loading) {
        return (
            <div className="admin-layout">
                <div className="admin-content" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '600px' }}>
                    <Spinner />
                </div>
            </div>
        );
    }

    return (
    <div className="admin-layout">
      <div className="admin-content">
        
        <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
          
          <div style={{ marginBottom: '20px', borderBottom: '1px solid #333', paddingBottom: '15px' }}>
            <h2 style={{ margin: 0, fontSize: '24px' }}>공지사항 관리</h2>
          </div>

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
                {currentNotices.length === 0 ? (
                  <tr><td colSpan="5" style={{ padding: '50px', color: '#666' }}>등록된 공지사항이 없습니다.</td></tr>
                ) : (
                  currentNotices.map((notice) => (
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

            {/* 페이지네이션 */}
            {totalPages > 1 && (
              <Pagination 
                currentPage={currentPage} 
                totalPages={totalPages} 
                onPageChange={handlePageChange} 
              />
            )}

          </div>
        </div>

      </div>
    </div>
  );
};

export default AdminNoticeList;