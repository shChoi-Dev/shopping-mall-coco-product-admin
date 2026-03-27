import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";

const NoticeDetailPage = () => {
    const { noticeNo } = useParams(); // 주소창에서 공지사항 번호(noticeNo)를 가져옴
    const navigate = useNavigate();
    const [notice, setNotice] = useState(null);

    useEffect(() => {
        const fetchNoticeDetail = async () => {
            try {
                const response = await fetch(`${process.env.REACT_APP_API_URL}/api/notices/${noticeNo}`, {
                    credentials: 'include' // 쿠키 포함 허락
                });
                if (response.ok) {
                    const data = await response.json();
                    setNotice(data);
                } else {
                    alert('존재하지 않거나 삭제된 공지사항입니다.');
                    navigate('/notices'); // 실패 시 목록으로 돌아감
                }
            } catch (error) {
                console.error('공지사항 상세 로딩 실패:', error);
            }
        };

        fetchNoticeDetail();
    }, [noticeNo, navigate]);

    // 스켈레톤 UI
    if (!notice) {
    return (
      <div style={{ maxWidth: '800px', margin: '0 auto', padding: '50px 20px', minHeight: '600px' }}>
        <style>
          {`
            @keyframes pulse {
              0%, 100% { opacity: 1; }
              50% { opacity: 0.4; }
            }
            .skeleton {
              background-color: #f0f0f0;
              border-radius: 4px;
              animation: pulse 1.5s ease-in-out infinite;
            }
          `}
        </style>
        
        <h2 style={{ textAlign: 'center', marginBottom: '40px', fontWeight: 'bold' }}>공지사항</h2>
        
        <div style={{ borderBottom: '2px solid #000', paddingBottom: '20px', marginBottom: '30px' }}>
          {/* 제목 스켈레톤 */}
          <div className="skeleton" style={{ width: '60%', height: '36px', marginBottom: '15px' }}></div>
          {/* 날짜 및 조회수 스켈레톤 */}
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <div className="skeleton" style={{ width: '150px', height: '16px' }}></div>
            <div className="skeleton" style={{ width: '80px', height: '16px' }}></div>
          </div>
        </div>

        {/* 본문 내용 스켈레톤 */}
        <div className="skeleton" style={{ width: '100%', height: '200px', marginBottom: '20px', borderRadius: '8px' }}></div>
        <div className="skeleton" style={{ width: '95%', height: '20px', marginBottom: '10px' }}></div>
        <div className="skeleton" style={{ width: '85%', height: '20px' }}></div>
      </div>
    );
  }

    return (
        <div style={{ maxWidth: '800px', margin: '0 auto', padding: '50px 20px', minHeight: '600px' }}>

            {/* 공지사항 타이틀 및 메타 정보 */}
            <h2 style={{ textAlign: 'center', marginBottom: '40px', fontWeight: 'bold' }}>공지사항</h2>

            <div style={{ borderBottom: '2px solid #000', paddingBottom: '20px', marginBottom: '30px' }}>
                <h3 style={{ fontSize: '24px', margin: '0 0 15px 0' }}>{notice.title}</h3>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: '#666', fontSize: '14px' }}>
                    <span>작성일 : {new Date(notice.createdAt).toLocaleDateString()}</span>
                    <span>조회수 : {notice.viewCount}</span>
                </div>
            </div>

            {/* 공지사항 본문 */}
            <div
                style={{ fontSize: '16px', lineHeight: '1.8', paddingBottom: '50px', borderBottom: '1px solid #eee' }}
                dangerouslySetInnerHTML={{ __html: notice.content }}
            />

            {/* 하단 목록 버튼 */}
            <div style={{ textAlign: 'center', marginTop: '40px' }}>
                <button
                    onClick={() => navigate('/notices')}
                    style={{ padding: '12px 40px', backgroundColor: '#000', color: '#fff', border: 'none', fontSize: '16px', cursor: 'pointer' }}
                >
                    목록으로
                </button>
            </div>
        </div>
    );
};

export default NoticeDetailPage;