import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchWithoutAuth } from '../../utils/api';
import '../../css/NoticePage.css';

function NoticePage() {
  const navigate = useNavigate();
  const [notices, setNotices] = useState([]);

  useEffect(() => {
    const fetchNotices = async () => {
      try {
        const response = await fetchWithoutAuth('/notices');
        if (response.ok) {
          const data = await response.json();
          // 최신 글이 위로 오도록 번호 내림차순 정렬
          const sortedData = data.sort((a, b) => b.noticeNo - a.noticeNo);
          setNotices(sortedData);
        }
      } catch (error) {
        console.error('공지사항 로딩 실패:', error);
      }
    };

    fetchNotices();
  }, []);

  return (
    <div className="notice-container">
      <div className="notice-header">
        <h1>공지사항</h1>
        <p>Coco의 새로운 소식과 안내를 확인하세요.</p>
      </div>

      <div className="notice-list">
        {/* 헤더 */}
        <div className="notice-list-header" style={{ display: 'flex', borderBottom: '2px solid #000', padding: '15px 10px', fontWeight: 'bold' }}>
          <span style={{ width: '10%', textAlign: 'center' }}>No</span>
          <span style={{ width: '60%', textAlign: 'center' }}>제목</span>
          <span style={{ width: '20%', textAlign: 'center' }}>작성일</span>
          <span style={{ width: '10%', textAlign: 'center' }}>조회수</span>
        </div>

        {/* 리스트 아이템 */}
        {notices.length === 0 ? (
          <div style={{ padding: '50px', textAlign: 'center' }}>등록된 공지사항이 없습니다.</div>
        ) : (
          notices.map((notice) => (
            <div 
              key={notice.noticeNo} 
              className="notice-item"
              onClick={() => navigate(`/notices/${notice.noticeNo}`)}
              style={{ cursor: 'pointer', display: 'flex', borderBottom: '1px solid #ddd', padding: '15px 10px' }}
            >
              <span style={{ width: '10%', textAlign: 'center' }}>{notice.noticeNo}</span>
              <span style={{ width: '60%', textAlign: 'left', fontWeight: '500' }}>{notice.title}</span>
              <span style={{ width: '20%', textAlign: 'center', color: '#666' }}>{new Date(notice.createdAt).toLocaleDateString()}</span>
              <span style={{ width: '10%', textAlign: 'center', color: '#666' }}>{notice.viewCount}</span>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default NoticePage;