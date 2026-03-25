import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const EventPage = () => {
  const [events, setEvents] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        const response = await fetch(`${process.env.REACT_APP_API_URL}/api/events`);
        if (response.ok) {
          const data = await response.json();
          // 최신 이벤트가 먼저 오도록 정렬
          const sortedData = data.sort((a, b) => b.eventNo - a.eventNo);
          setEvents(sortedData);
        }
      } catch (error) {
        console.error('이벤트 로딩 실패:', error);
      }
    };
    fetchEvents();
  }, []);

  // 현재 시간과 비교하여 자동으로 뱃지 상태를 반환하는 함수
  const getEventStatus = (start, end) => {
    const now = new Date();
    const startDate = new Date(start);
    const endDate = new Date(end);

    if (now < startDate) {
      // 진행 예정 (White & Black)
      return { text: '진행 예정', bg: '#fff', color: '#333', border: '#333' };
    } else if (now > endDate) {
      // 종료 (Grey)
      return { text: '종료', bg: '#f5f5f5', color: '#8c8c8c', border: '#d9d9d9' };
    } else {
      // 진행 중 (Black & White)
      return { text: '진행 중', bg: '#111', color: '#fff', border: '#111' };
    }
  };

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '50px 20px', minHeight: '600px' }}>
      
      <div style={{ textAlign: 'center', marginBottom: '50px' }}>
        <h2 style={{ fontSize: '32px', fontWeight: 'bold', marginBottom: '10px' }}>EVENT</h2>
        <p style={{ color: '#666' }}>Coco의 특별한 혜택과 이벤트를 만나보세요.</p>
      </div>

      {/* 카드를 바둑판 형태 */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '30px' }}>
        {events.length === 0 ? (
          <div style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '100px 0', color: '#666' }}>
            진행 중인 이벤트가 없습니다.
          </div>
        ) : (
          events.map(event => {
            const status = getEventStatus(event.startDate, event.endDate); // 뱃지 상태 계산

            return (
              <div 
                key={event.eventNo} 
                onClick={() => navigate(`/events/${event.eventNo}`)}
                style={{ cursor: 'pointer', border: '1px solid #eee', borderRadius: '8px', overflow: 'hidden', transition: 'transform 0.2s', boxShadow: '0 2px 8px rgba(0,0,0,0.05)' }}
                // 마우스 올리면 카드가 위로 살짝 떠오르는 애니메이션
                onMouseEnter={(e) => e.currentTarget.style.transform = 'translateY(-5px)'}
                onMouseLeave={(e) => e.currentTarget.style.transform = 'translateY(0)'}
              >
                {/* 썸네일 이미지 */}
                <div style={{ width: '100%', height: '200px', backgroundColor: '#f9f9f9', overflow: 'hidden' }}>
                  {event.thumbnailUrl ? (
                    <img src={`${process.env.REACT_APP_API_URL || 'http://localhost:8080'}${event.thumbnailUrl}`} alt="이벤트 썸네일" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                  ) : (
                    <div style={{ width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#bbb' }}>No Image</div>
                  )}
                </div>
                
                {/* 텍스트 정보 */}
                <div style={{ padding: '20px' }}>
                  
                  {/* 자동 계산된 뱃지 출력 */}
                  <span style={{ display: 'inline-block', padding: '4px 10px', fontSize: '12px', fontWeight: 'bold', borderRadius: '4px', backgroundColor: status.bg, color: status.color, border: `1px solid ${status.border}`, marginBottom: '10px' }}>
                    {status.text}
                  </span>
                  
                  <h3 style={{ fontSize: '18px', margin: '0 0 10px 0', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    {event.title}
                  </h3>
                  <p style={{ margin: 0, fontSize: '13px', color: '#666' }}>
                    {new Date(event.startDate).toLocaleDateString()} ~ {new Date(event.endDate).toLocaleDateString()}
                  </p>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};

export default EventPage;