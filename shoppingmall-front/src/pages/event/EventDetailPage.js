import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';

const EventDetailPage = () => {
    const { eventNo } = useParams();
    const navigate = useNavigate();
    const [event, setEvent] = useState(null);

    useEffect(() => {
        const fetchEventDetail = async () => {
            try {
                const response = await fetch(`${process.env.REACT_APP_API_URL}/api/events/${eventNo}`, {
                    credentials: 'include' // 벡엔드와 쿠키를 주고 받음
                });
                if (response.ok) {
                    const data = await response.json();
                    setEvent(data);
                } else {
                    alert('존재하지 않거나 삭제된 이벤트입니다.');
                    navigate('/events');
                }
            } catch (error) {
                console.error('이벤트 상세 로딩 실패:', error);
            }
        };
        fetchEventDetail();
    }, [eventNo, navigate]);

    if (!event) return <div style={{ textAlign: 'center', padding: '100px' }}>로딩 중...</div>;

    // 상세 페이지에서도 똑같이 뱃지를 계산해서 보여줌
    const getEventStatus = (start, end) => {
        const now = new Date();
        const startDate = new Date(start);
        const endDate = new Date(end);

        if (now < startDate) {
            return { text: '진행 예정', bg: '#fff', color: '#333', border: '#333' };
        } else if (now > endDate) {
            return { text: '종료', bg: '#f5f5f5', color: '#8c8c8c', border: '#d9d9d9' };
        } else {
            return { text: '진행 중', bg: '#111', color: '#fff', border: '#111' };
        }
    };

    const status = getEventStatus(event.startDate, event.endDate);

    return (
        <div style={{ maxWidth: '800px', margin: '0 auto', padding: '50px 20px', minHeight: '600px' }}>

            <h2 style={{ textAlign: 'center', marginBottom: '40px', fontWeight: 'bold' }}>EVENT</h2>

            <div style={{ borderBottom: '2px solid #000', paddingBottom: '20px', marginBottom: '30px' }}>

                {/* 뱃지 출력 */}
                <div style={{ marginBottom: '15px' }}>
                    <span style={{ display: 'inline-block', padding: '4px 10px', fontSize: '12px', fontWeight: 'bold', borderRadius: '4px', backgroundColor: status.bg, color: status.color, border: `1px solid ${status.border}` }}>
                        {status.text}
                    </span>
                </div>

                <h3 style={{ fontSize: '26px', margin: '0 0 15px 0' }}>{event.title}</h3>

                <div style={{ display: 'flex', justifyContent: 'space-between', color: '#666', fontSize: '14px' }}>
                    <span>이벤트 기간 : {new Date(event.startDate).toLocaleDateString()} ~ {new Date(event.endDate).toLocaleDateString()}</span>
                    <span>조회수 : {event.viewCount}</span>
                </div>
            </div>

            {/* 에디터 본문 출력 (dangerouslySetInnerHTML) */}
            <div
                style={{ fontSize: '16px', lineHeight: '1.8', paddingBottom: '50px', borderBottom: '1px solid #eee' }}
                dangerouslySetInnerHTML={{ __html: event.content }}
            />

            <div style={{ textAlign: 'center', marginTop: '40px' }}>
                <button
                    onClick={() => navigate('/events')}
                    style={{ padding: '12px 40px', backgroundColor: '#000', color: '#fff', border: 'none', borderRadius: '4px', fontSize: '16px', cursor: 'pointer', fontWeight: 'bold' }}
                >
                    목록으로
                </button>
            </div>

        </div>
    );
};

export default EventDetailPage;