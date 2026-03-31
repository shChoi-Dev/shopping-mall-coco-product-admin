import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchWithAuth } from '../../utils/api';
import '../../css/admin/AdminProductList.css';
import Spinner from '../../components/admin/Spinner';
import Pagination from '../../components/admin/Pagination';
import { getImageUrl } from '../../utils/api';

const AdminEventList = () => {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  useEffect(() => {
    loadEvents();
  }, []);

  const loadEvents = async () => {
    setLoading(true);
    try {
      const response = await fetch(`${process.env.REACT_APP_API_URL}/api/events`);
      if (response.ok) {
        const data = await response.json();
        const sortedData = data.sort((a, b) => b.eventNo - a.eventNo); // 최신순
        setEvents(sortedData);
      }
    } catch (error) {
      console.error('이벤트 로딩 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (eventNo) => {
    if (window.confirm('정말 이 이벤트를 삭제하시겠습니까?')) {
      try {
        const response = await fetchWithAuth(`/admin/events/${eventNo}`, {
          method: 'DELETE'
        });
        if (response.ok) {
          alert('삭제되었습니다.');
          loadEvents();
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
  const currentEvents = events.slice(indexOfFirstItem, indexOfLastItem);
  const totalPages = Math.ceil(events.length / itemsPerPage);

  const handlePageChange = (pageNumber) => {
    setCurrentPage(pageNumber);
  };

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
            <h2 style={{ margin: 0, fontSize: '24px' }}>이벤트 관리</h2>
          </div>

          <div style={{ backgroundColor: '#fff', padding: '30px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <h3 style={{ margin: 0, fontSize: '18px' }}>진행 중인 이벤트 목록</h3>
              <button
                onClick={() => navigate('/admin/events/new')}
                style={{ padding: '8px 16px', backgroundColor: '#333', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
              >
                + 이벤트 등록
              </button>
            </div>

            <table className="product-table" style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'center' }}>
              <thead style={{ backgroundColor: '#f9f9f9', borderTop: '2px solid #333', borderBottom: '1px solid #ddd' }}>
                <tr>
                  <th style={{ padding: '12px 15px', width: '5%' }}>번호</th>
                  <th style={{ width: '15%' }}>배너</th> {/* 이벤트 썸네일 노출 */}
                  <th style={{ width: '35%' }}>제목</th>
                  <th style={{ width: '20%' }}>이벤트 기간</th>
                  <th style={{ width: '10%' }}>조회수</th>
                  <th style={{ width: '15%' }}>관리</th>
                </tr>
              </thead>
              <tbody>
                {currentEvents.length === 0 ? (
                  <tr><td colSpan="6" style={{ padding: '50px', color: '#666' }}>등록된 이벤트가 없습니다.</td></tr>
                ) : (
                  currentEvents.map((event) => (
                    <tr key={event.eventNo} style={{ borderBottom: '1px solid #eee' }}>
                      <td style={{ padding: '15px' }}>{event.eventNo}</td>
                      <td>
                        {event.thumbnailUrl ? (
                          <img src={getImageUrl(event.thumbnailUrl)} alt="썸네일" style={{ width: '80px', height: '40px', objectFit: 'cover', borderRadius: '4px' }} />
                        ) : (
                          <span style={{ fontSize: '12px', color: '#999' }}>이미지 없음</span>
                        )}
                      </td>
                      <td style={{ textAlign: 'left', fontWeight: '500' }}>{event.title}</td>
                      <td style={{ fontSize: '13px', color: '#555' }}>
                        {new Date(event.startDate).toLocaleDateString()} ~ <br />
                        {new Date(event.endDate).toLocaleDateString()}
                      </td>
                      <td style={{ color: '#555' }}>{event.viewCount}</td>
                      <td>
                        <button
                          onClick={() => navigate(`/admin/events/edit/${event.eventNo}`)}
                          style={{ padding: '6px 12px', backgroundColor: '#fff', color: '#1890ff', border: '1px solid #1890ff', borderRadius: '4px', cursor: 'pointer', fontSize: '13px', marginRight: '5px' }}
                        >수정</button>

                        <button
                          onClick={() => handleDelete(event.eventNo)}
                          style={{ padding: '6px 12px', backgroundColor: '#fff', color: '#ff4d4f', border: '1px solid #ff4d4f', borderRadius: '4px', cursor: 'pointer', fontSize: '13px' }}
                        >삭제</button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>

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

export default AdminEventList;